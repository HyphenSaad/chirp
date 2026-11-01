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
 * Runtime statistics for the current program: elapsed time, heap usage,
 * process CPU load, and how much input Chirp has read.
 *
 * <p>When enabled ({@code Chirp.start()} with the default startup configuration,
 * {@link chirp.config.Config#stats(boolean)}, or {@code -Dchirp.stats=true}),
 * a short summary is printed automatically when the JVM shuts down. It is
 * off by default, so plain library use prints nothing.</p>
 *
 * <h2>Keystroke counting — honest limitation</h2>
 * <p>A console application cannot count raw physical keystrokes portably:
 * {@code System.in} is line-buffered by the OS. Chirp therefore counts every
 * <em>line</em> and <em>character</em> consumed through its own readers —
 * the data a beginner actually cares about.</p>
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

    public static boolean isEnabled() { return enabled; }

    public static void enable() {
        enabled = true;
        installHook();
    }

    public static void disable() { enabled = false; }

    public static void installHook() {
        if (!HOOK_INSTALLED.compareAndSet(false, true)) return;
        Runtime.getRuntime().addShutdownHook(new Thread(Stats::report, "chirp-stats"));
    }

    public static void reportNow() { printSummary(); }

    public static void reset() {
        START_NANOS.set(System.nanoTime());
        LINES.set(0);
        CHARS.set(0);
        READS.set(0);
    }

    // ------------------------------------------------------------- recording

    /**
     * Records one line read from the user.
     *
     * <p><strong>Internal API</strong> — called by {@link chirp.console.Input}.</p>
     */
    public static void recordLine(int characters) {
        LINES.incrementAndGet();
        READS.incrementAndGet();
        if (characters > 0) CHARS.addAndGet(characters);
    }

    // ------------------------------------------------------------- snapshot

    public record StatsSnapshot(
            Duration elapsed,
            long heapUsedBytes,
            long heapTotalBytes,
            long heapMaxBytes,
            double processCpuPercent,
            long linesRead,
            long charactersRead,
            long readCalls) {

        public double elapsedSeconds() { return elapsed.toMillis() / 1000.0; }
        public double heapUsedMegabytes() { return heapUsedBytes / 1_048_576.0; }
        public double heapTotalMegabytes() { return heapTotalBytes / 1_048_576.0; }
        public double heapMaxMegabytes() { return heapMaxBytes / 1_048_576.0; }
        public boolean cpuAvailable() { return processCpuPercent >= 0; }
    }

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
