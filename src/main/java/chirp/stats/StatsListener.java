package chirp.stats;

/**
 * Observer for periodic stats snapshots taken by
 * {@link StatsSampler}.
 *
 * <p>Invoked once per sample interval on the sampler's background thread;
 * implementations should be quick and thread-safe. Any exception is caught
 * and logged via {@link chirp.internal.Trace#warn} so a buggy listener does
 * not kill the sampler.</p>
 */
@FunctionalInterface
public interface StatsListener {

    void onSample(Stats.StatsSnapshot snapshot);
}
