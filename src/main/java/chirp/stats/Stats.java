package chirp.stats;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Runtime statistics for the current program — elapsed time, heap
 * usage, process CPU load, and how much input Chirp has read.
 *
 * <p>When enabled, a short summary is printed automatically at JVM
 * shutdown. Off by default — plain library use prints nothing.
 * {@link chirp.Chirp#start()} turns it on by default in interactive
 * console mode; {@link chirp.Chirp#start(StartupConfig)} with
 * {@code .noStats()} turns it off; {@code -Dchirp.stats=true} forces it
 * on regardless of configuration.</p>
 *
 * <h2>What the summary contains</h2>
 * <pre>
 * ════════════════════════════════════════════════════
 *  Chirp run summary
 * ════════════════════════════════════════════════════
 *  Started        : Nov 1, 2026 4:30:00 PM
 *  Elapsed        : 2.351 s
 *  Heap used      : 42.5 MB / 64.0 MB (max 6076.0 MB)
 *  Process CPU    : 8.9 %
 *  Lines read     : 12
 *  Characters read: 348
 *  Read calls     : 12
 * ════════════════════════════════════════════════════
 * </pre>
 *
 * <h2>Reading the stats manually</h2>
 * <p>{@link #snapshot()} returns a {@link StatsSnapshot} record with
 * every field the summary prints, so a caller can build their own
 * report or feed the numbers to a monitoring system:</p>
 * <pre>{@code
 * Stats.StatsSnapshot s = Stats.snapshot();
 * System.out.println("Program used " + s.heapUsedMegabytes() + "MB");
 * }</pre>
 *
 * <h2>Keystroke counting — honest limitation</h2>
 * <p>A console application cannot count raw physical keystrokes
 * portably: {@code System.in} is line-buffered by the OS. Chirp
 * therefore counts every <em>line</em> and <em>character</em> consumed
 * through its own readers — the data a beginner actually cares about.</p>
 *
 * <h2>Enabling and disabling</h2>
 * <ul>
 *   <li>{@link chirp.Chirp#start()} — on by default</li>
 *   <li>{@link chirp.Chirp#start(StartupConfig)} with
 *       {@code .noStats()} — off</li>
 *   <li>{@link chirp.config.Config#stats(boolean)} — toggle at runtime</li>
 *   <li>{@code -Dchirp.stats=true} or {@code -Dchirp.stats=false} — system
 *       property override</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For the background sampler thread that populates periodic
 * snapshots, see {@link StatsSampler}. For the shutdown summary logic,
 * see {@link #reportNow()}.</p>
 *
 * @see StatsSampler
 * @see Stats.StatsSnapshot
 * @see chirp.config.Config#stats(boolean)
 * @since 1.0.0
 */
public final class Stats {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm:ss a")
            .withZone(ZoneId.systemDefault());

    private static final AtomicLong START_NANOS = new AtomicLong(System.nanoTime());
    private static final Instant START_INSTANT = Instant.now();
    private static final AtomicLong LINES = new AtomicLong();
    private static final AtomicLong CHARS = new AtomicLong();
    private static final AtomicLong READS = new AtomicLong();
    private static final AtomicBoolean HOOK_INSTALLED = new AtomicBoolean(false);
    private static volatile boolean enabled = Boolean.parseBoolean(System.getProperty("chirp.stats", "false"));

    private Stats() {}

    // ------------------------------------------------------------ lifecycle

    /**
     * Checks whether stats collection is enabled.
     *
     * @return {@code true} if enabled
     * @see #enable()
     * @see #disable()
     * @since 1.0.0
     */
    public static boolean isEnabled() { return enabled; }

    /**
     * Enables stats collection and installs the JVM shutdown hook that
     * prints the summary. The hook is installed only once per JVM —
     * subsequent calls are safe no-ops for the hook installation.
     *
     * @see #disable()
     * @see #installHook()
     * @since 1.0.0
     */
    public static void enable() {
        enabled = true;
        installHook();
    }

    /**
     * Disables stats collection. The summary is not printed at
     * shutdown.
     *
     * @see #enable()
     * @since 1.0.0
     */
    public static void disable() { enabled = false; }

    /**
     * Installs the shutdown hook that prints the summary — idempotent.
     * Called by {@link #enable()}; exposed for advanced use when the
     * caller wants to install the hook before enabling collection.
     *
     * @see #enable()
     * @since 1.0.0
     */
    public static void installHook() {
        if (!HOOK_INSTALLED.compareAndSet(false, true)) return;
        Runtime.getRuntime().addShutdownHook(new Thread(Stats::report, "chirp-stats"));
    }

    /**
     * Prints the summary immediately, without waiting for JVM shutdown.
     * Useful for tests and for programs that want to print the summary
     * themselves before exiting.
     *
     * @see #snapshot()
     * @since 1.0.0
     */
    public static void reportNow() { printSummary(); }

    /**
     * Resets the elapsed-time baseline and zeroes every counter.
     * Useful in tests when the caller wants each test to see clean
     * numbers.
     *
     * @see #snapshot()
     * @since 1.0.0
     */
    public static void reset() {
        START_NANOS.set(System.nanoTime());
        LINES.set(0);
        CHARS.set(0);
        READS.set(0);
    }

    // ------------------------------------------------------------- recording

    /**
     * Records one line read from the user. Called by
     * {@link chirp.console.Input} after every successful read —
     * students never invoke this directly.
     *
     * <p><strong>Internal API.</strong></p>
     *
     * @param characters the number of characters in the line
     * @see chirp.console.Input#readRaw(String)
     * @since 1.0.0
     */
    public static void recordLine(int characters) {
        LINES.incrementAndGet();
        READS.incrementAndGet();
        if (characters > 0) CHARS.addAndGet(characters);
    }

    // ------------------------------------------------------------- snapshot

    /**
     * An immutable snapshot of the current statistics at a point in
     * time. Every field is already computed and ready to read — no
     * further method calls needed to interpret the numbers.
     *
     * @param elapsed wall-clock time since stats collection started
     * @param heapUsedBytes bytes currently in use in the heap
     * @param heapTotalBytes the current heap size
     * @param heapMaxBytes the maximum heap size the JVM will allow
     * @param processCpuPercent process CPU load (0–100), or -1 if
     * unavailable
     * @param linesRead number of lines read through {@link chirp.console.Input}
     * @param charactersRead total characters across all reads
     * @param readCalls number of {@link #recordLine(int)} calls
     * @see Stats#snapshot()
     * @since 1.0.0
     */
    public record StatsSnapshot(
            Duration elapsed,
            long heapUsedBytes,
            long heapTotalBytes,
            long heapMaxBytes,
            double processCpuPercent,
            long linesRead,
            long charactersRead,
            long readCalls) {

        /**
         * Returns the elapsed time in fractional seconds.
         *
         * @return the elapsed seconds
         * @since 1.0.0
         */
        public double elapsedSeconds() { return elapsed.toMillis() / 1000.0; }

        /**
         * Returns the heap used in megabytes.
         *
         * @return the megabytes
         * @since 1.0.0
         */
        public double heapUsedMegabytes() { return heapUsedBytes / 1_048_576.0; }

        /**
         * Returns the heap total in megabytes.
         *
         * @return the megabytes
         * @since 1.0.0
         */
        public double heapTotalMegabytes() { return heapTotalBytes / 1_048_576.0; }

        /**
         * Returns the heap max in megabytes.
         *
         * @return the megabytes
         * @since 1.0.0
         */
        public double heapMaxMegabytes() { return heapMaxBytes / 1_048_576.0; }

        /**
         * Checks whether CPU percentage is available.
         *
         * @return {@code true} if {@link #processCpuPercent()} is valid
         * @since 1.0.0
         */
        public boolean cpuAvailable() { return processCpuPercent >= 0; }
    }

    /**
     * Captures a snapshot of the current statistics.
     *
     * <pre>{@code
     * Stats.StatsSnapshot s = Stats.snapshot();
     * System.out.printf("Used %.1f MB in %.2f seconds%n",
     *     s.heapUsedMegabytes(), s.elapsedSeconds());
     * }</pre>
     *
     * @return the snapshot
     * @see StatsSnapshot
     * @since 1.0.0
     */
    public static StatsSnapshot snapshot() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long total = runtime.totalMemory();
        long max = runtime.maxMemory();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - START_NANOS.get());
        return new StatsSnapshot(elapsed, used, total, max, processCpuPercent(),
                LINES.get(), CHARS.get(), READS.get());
    }

    // --------------------------------------------------------------- report

    private static void report() {
        if (!enabled) return;
        printSummary();
    }

    private static void printSummary() {
        StatsSnapshot s = snapshot();
        String bar = "=".repeat(52);
        StringBuilder out = new StringBuilder();
        out.append('\n').append(bar).append('\n');
        out.append(" Chirp run summary\n");
        out.append(bar).append('\n');
        out.append(String.format(" Started        : %s%n", TIME_FORMAT.format(START_INSTANT)));
        out.append(String.format(" Elapsed        : %.3f s%n", s.elapsedSeconds()));
        out.append(String.format(" Heap used      : %s / %s (max %s)%n",
                mb(s.heapUsedBytes()), mb(s.heapTotalBytes()), mb(s.heapMaxBytes())));
        out.append(String.format(" Process CPU    : %s%n",
                s.cpuAvailable() ? String.format("%.1f %%", s.processCpuPercent()) : "n/a"));
        out.append(String.format(" Lines read     : %d%n", s.linesRead()));
        out.append(String.format(" Characters read: %d%n", s.charactersRead()));
        out.append(String.format(" Read calls     : %d%n", s.readCalls()));
        out.append(bar);
        System.out.println(out);
    }

    private static String mb(long bytes) {
        return String.format("%.1f MB", bytes / 1_048_576.0);
    }

    private static double processCpuPercent() {
        try {
            OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            Class<?> sunBean = Class.forName("com.sun.management.OperatingSystemMXBean");
            if (!sunBean.isInstance(bean)) return -1;

            Method method = sunBean.getMethod("getProcessCpuLoad");
            Object value = method.invoke(bean);
            if (!(value instanceof Double load) || load < 0) return -1;
            return load * 100.0;
        } catch (ReflectiveOperationException | LinkageError e) {
            return -1;
        }
    }
}
