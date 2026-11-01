package chirp.stats;

/**
 * Observer for periodic stats snapshots taken by
 * {@link StatsSampler}.
 *
 * <p>A functional interface — implement {@link #onSample(Stats.StatsSnapshot)}
 * as a lambda when attaching to a sampler:</p>
 * <pre>{@code
 * StatsSampler sampler = ChirpLifecycle.sampler();
 * if (sampler != null) {
 *     sampler.addListener(snapshot -> {
 *         if (snapshot.heapUsedMegabytes() > 500) {
 *             System.err.println("Heap over 500 MB!");
 *         }
 *     });
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 * <p>Invoked on the sampler's background thread
 * ({@code "chirp-stats-sampler"}), once per sample interval. Implementations
 * should be quick and thread-safe — do not block on user I/O or
 * networking inside a listener. A long-running listener delays every
 * subsequent sample.</p>
 *
 * <h2>Exception handling</h2>
 * <p>Any exception thrown by a listener is caught by the sampler and
 * logged via {@link chirp.internal.Trace#warn(String, String)}, with the
 * warning rate-limited to once every 5 seconds so a misbehaving listener
 * cannot flood the log. The sampler continues after a listener throws.</p>
 *
 * <h2>Related</h2>
 * <p>For the snapshots themselves, see {@link Stats.StatsSnapshot}.
 * For the sampler that notifies listeners, see {@link StatsSampler}.
 * For enabling stats, see {@link chirp.config.Config#stats(boolean)}.</p>
 *
 * @see StatsSampler
 * @see Stats.StatsSnapshot
 * @since 1.0.0
 */
@FunctionalInterface
public interface StatsListener {

    /**
     * Called once per sample interval with a fresh
     * {@link Stats.StatsSnapshot}.
     *
     * @param snapshot the snapshot at the moment of the sample
     * @since 1.0.0
     */
    void onSample(Stats.StatsSnapshot snapshot);
}
