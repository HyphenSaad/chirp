package chirp.stats;

import chirp.internal.Trace;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Background sampler thread — captures heap, CPU, and thread counts at
 * a fixed interval, tracks peaks, and notifies listeners.
 *
 * <p>Started by {@link chirp.startup.ChirpLifecycle#start(chirp.startup.StartupConfig)}
 * when stats are enabled. Runs on a daemon thread named
 * {@code "chirp-stats-sampler"} — does not prevent JVM exit.</p>
 *
 * <h2>Silent by default</h2>
 * <p>The sampler does <b>not</b> trace per-tick output by default.
 * Turning that on would flood stderr during interactive input, which
 * is the opposite of what a friendly library should do. Enable verbose
 * ticks explicitly via {@link #setVerbose(boolean)} or
 * {@code -Dchirp.stats.verbose=true}. Start and stop events are still
 * logged once.</p>
 *
 * <h2>Listener exception rate-limiting</h2>
 * <p>A misbehaving listener would otherwise flood the log at the
 * sampler's tick rate (once per second by default). The sampler
 * therefore rate-limits its listener-failure warning to once every
 * five seconds, so a broken listener shows up in the log without
 * overwhelming it.</p>
 *
 * <h2>Example — custom listener</h2>
 * <pre>{@code
 * StatsSampler sampler = ChirpLifecycle.sampler();
 * if (sampler != null) {
 *     sampler.addListener(snapshot -> {
 *         if (snapshot.heapUsedMegabytes() > 500) {
 *             System.err.println("Heap over 500MB!");
 *         }
 *     });
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the snapshots themselves, see {@link Stats.StatsSnapshot}. For
 * the facade over {@link Stats}, see {@link Stats}. For the lifecycle
 * that spawns this thread, see
 * {@link chirp.startup.ChirpLifecycle#start(chirp.startup.StartupConfig)}.</p>
 *
 * @see Stats
 * @see Stats.StatsSnapshot
 * @see StatsListener
 * @see chirp.startup.ChirpLifecycle#sampler()
 * @since 1.0.0
 */
public final class StatsSampler {

    private static final long LISTENER_WARN_INTERVAL_NANOS = 5_000_000_000L;

    private final long intervalMs;
    private final ScheduledExecutorService executor;
    private final CopyOnWriteArrayList<StatsListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong peakHeapBytes = new AtomicLong();
    private final AtomicLong totalSamples = new AtomicLong();
    private final AtomicLong lastListenerWarnNs = new AtomicLong();

    private volatile Stats.StatsSnapshot lastSnapshot;
    private volatile boolean verbose = Boolean.parseBoolean(System.getProperty("chirp.stats.verbose", "false"));

    /**
     * Constructs a sampler with the given interval. Does not start it —
     * call {@link #start()} explicitly.
     *
     * @param intervalMs the sampling interval in milliseconds (clamped to ≥ 100)
     * @see #start()
     * @since 1.0.0
     */
    public StatsSampler(long intervalMs) {
        this.intervalMs = Math.max(100L, intervalMs);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chirp-stats-sampler");
            t.setDaemon(true);
            return t;
        });
    }

    // ----------------------------------------------------------- lifecycle

    /**
     * Starts the sampling thread. Samples are taken every
     * {@code intervalMs} milliseconds until {@link #stop()}.
     *
     * @see #stop()
     * @since 1.0.0
     */
    public void start() {
        executor.scheduleAtFixedRate(this::tick, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        Trace.debug("stats", "StatsSampler started at {}ms interval", intervalMs);
    }

    /**
     * Stops the sampling thread. Any pending samples are cancelled —
     * the sampler becomes inert.
     *
     * @see #start()
     * @since 1.0.0
     */
    public void stop() {
        executor.shutdownNow();
        Trace.debug("stats", "StatsSampler stopped after {} samples", totalSamples.get());
    }

    // ----------------------------------------------------------- verbose

    /**
     * Checks whether per-tick trace output is enabled. Default:
     * {@code false}.
     *
     * @return {@code true} if verbose
     * @see #setVerbose(boolean)
     * @see #enableVerbose()
     * @see #disableVerbose()
     * @since 1.0.0
     */
    public boolean isVerbose() { return verbose; }

    /**
     * Enables or disables per-tick trace output.
     *
     * @param value whether to enable verbose ticks
     * @see #isVerbose()
     * @since 1.0.0
     */
    public void setVerbose(boolean value) { this.verbose = value; }

    /**
     * Alias of {@link #setVerbose(boolean)} with {@code true} — see
     * that method for full documentation.
     *
     * @see #setVerbose(boolean)
     * @since 1.0.0
     */
    public void enableVerbose() { this.verbose = true; }

    /**
     * Alias of {@link #setVerbose(boolean)} with {@code false} — see
     * that method for full documentation.
     *
     * @see #setVerbose(boolean)
     * @since 1.0.0
     */
    public void disableVerbose() { this.verbose = false; }

    // ----------------------------------------------------------- listeners

    /**
     * Registers a listener that will be called on every sample. Listener
     * exceptions are rate-limited to one warning per five seconds.
     *
     * @param listener the listener (null → no-op)
     * @see #removeListener(StatsListener)
     * @since 1.0.0
     */
    public void addListener(StatsListener listener) {
        if (listener != null) listeners.add(listener);
    }

    /**
     * Unregisters a previously-added listener.
     *
     * @param listener the listener (null → no-op)
     * @see #addListener(StatsListener)
     * @since 1.0.0
     */
    public void removeListener(StatsListener listener) {
        if (listener != null) listeners.remove(listener);
    }

    // ----------------------------------------------------------- accessors

    /**
     * Returns the most recent snapshot taken by the sampler, or null
     * before the first sample.
     *
     * @return the last snapshot, or null
     * @since 1.0.0
     */
    public Stats.StatsSnapshot lastSnapshot() { return lastSnapshot; }

    /**
     * Returns the peak heap usage seen since the sampler started, in
     * bytes.
     *
     * @return the peak heap bytes
     * @since 1.0.0
     */
    public long peakHeapBytes() { return peakHeapBytes.get(); }

    /**
     * Returns the number of samples taken since the sampler started.
     *
     * @return the sample count
     * @since 1.0.0
     */
    public long totalSamples() { return totalSamples.get(); }

    /**
     * Returns the sampling interval in milliseconds.
     *
     * @return the interval
     * @since 1.0.0
     */
    public long intervalMs() { return intervalMs; }

    // ----------------------------------------------------------- testing hook

    /**
     * Runs one sample synchronously — used by unit tests.
     *
     * <p><strong>Package-private.</strong></p>
     *
     * @since 1.0.0
     */
    void sampleOnce() { tick(); }

    // ----------------------------------------------------------- internals

    private void tick() {
        try {
            Stats.StatsSnapshot snapshot = Stats.snapshot();
            lastSnapshot = snapshot;
            totalSamples.incrementAndGet();
            peakHeapBytes.accumulateAndGet(snapshot.heapUsedBytes(), Math::max);

            if (verbose) traceTick(snapshot);

            notifyListeners(snapshot);
        } catch (RuntimeException e) {
            Trace.warn("stats", "StatsSampler tick failed: " + e.getMessage());
        }
    }

    private void traceTick(Stats.StatsSnapshot snapshot) {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        Trace.trace("stats", "tick: heap={}MB cpu={} threads={}/{}",
                String.format("%.1f", snapshot.heapUsedMegabytes()),
                snapshot.cpuAvailable()
                        ? String.format("%.1f%%", snapshot.processCpuPercent())
                        : "n/a",
                threads.getThreadCount(), threads.getPeakThreadCount());
    }

    private void notifyListeners(Stats.StatsSnapshot snapshot) {
        if (listeners.isEmpty()) return;
        for (StatsListener listener : listeners) {
            try {
                listener.onSample(snapshot);
            } catch (RuntimeException e) {
                warnListenerException(e);
            }
        }
    }

    private void warnListenerException(RuntimeException e) {
        long now = System.nanoTime();
        long last = lastListenerWarnNs.get();
        if (now - last > LISTENER_WARN_INTERVAL_NANOS
                && lastListenerWarnNs.compareAndSet(last, now)) {
            Trace.warn("stats", "StatsListener threw: " + e.getMessage());
        }
    }
}
