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
 * Background sampler thread — captures heap, CPU, and thread counts at a
 * fixed interval, tracks peaks, and notifies listeners.
 *
 * <p><strong>The sampler is silent by default.</strong> Per-tick trace output
 * is disabled unless explicitly enabled via {@link #setVerbose(boolean)} or
 * {@code -Dchirp.stats.verbose=true}. This avoids flooding stderr during
 * interactive input. Start / stop events are still logged once.</p>
 *
 * <p>Listener exceptions are rate-limited to one warning per five seconds so
 * a misbehaving listener cannot spam the log.</p>
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

    public StatsSampler(long intervalMs) {
        this.intervalMs = Math.max(100L, intervalMs);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chirp-stats-sampler");
            t.setDaemon(true);
            return t;
        });
    }

    // ----------------------------------------------------------- lifecycle

    public void start() {
        executor.scheduleAtFixedRate(this::tick, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        Trace.debug("stats", "StatsSampler started at {}ms interval", intervalMs);
    }

    public void stop() {
        executor.shutdownNow();
        Trace.debug("stats", "StatsSampler stopped after {} samples", totalSamples.get());
    }

    // ----------------------------------------------------------- verbose

    /** Whether per-tick trace output is enabled. Default: false. */
    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean value) { this.verbose = value; }
    public void enableVerbose() { this.verbose = true; }
    public void disableVerbose() { this.verbose = false; }

    // ----------------------------------------------------------- listeners

    public void addListener(StatsListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(StatsListener listener) {
        if (listener != null) listeners.remove(listener);
    }

    // ----------------------------------------------------------- accessors

    public Stats.StatsSnapshot lastSnapshot() { return lastSnapshot; }
    public long peakHeapBytes() { return peakHeapBytes.get(); }
    public long totalSamples() { return totalSamples.get(); }
    public long intervalMs() { return intervalMs; }

    // ----------------------------------------------------------- testing hook

    /** Runs one sample synchronously — used by unit tests. */
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
