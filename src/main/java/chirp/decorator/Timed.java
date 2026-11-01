package chirp.decorator;

import java.time.Duration;

/**
 * A value paired with the time it took to produce.
 *
 * <p>Returned by {@link Decorator#timed(java.util.function.Supplier)}
 * and {@link Decorator#measured(java.util.function.Supplier)}. Carries
 * both the operation's result and its wall-clock duration so the
 * caller can inspect the value and the timing in one object.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Timed<JsonObject> result = Decorator.timed(() -> Http.get(url).send().object());
 *
 * if (result.millis() > 1000) {
 *     System.out.println("Slow request: " + result.millis() + "ms");
 * }
 *
 * JsonObject user = result.value();
 * }</pre>
 *
 * <h2>Time units</h2>
 * <p>The duration is measured with {@link System#nanoTime()}, so
 * {@link #nanos()} gives nanosecond precision and {@link #millis()} /
 * {@link #seconds()} give the more familiar units. Wall-clock time is
 * measured accurately even if the system clock changes mid-operation.</p>
 *
 * <h2>Related</h2>
 * <p>For one-off timing without a value, use
 * {@link Decorator#timed(Runnable)}, which returns a {@link Duration}
 * directly.</p>
 *
 * @param value the value produced by the operation (may be null)
 * @param duration the elapsed time
 * @param <T> the value's type
 * @see Decorator#timed(java.util.function.Supplier)
 * @see Decorator#measured(java.util.function.Supplier)
 * @since 1.0.0
 */
public record Timed<T>(T value, Duration duration) {

    /**
     * Alias of {@link #value()} — see that component for full
     * documentation.
     *
     * @return the value
     * @see #value()
     * @since 1.0.0
     */
    public T result() { return value; }

    /**
     * Alias of {@link #value()} — see that component for full
     * documentation.
     *
     * @return the value
     * @see #value()
     * @since 1.0.0
     */
    public T get() { return value; }

    /**
     * Alias of {@link #value()} — see that component for full
     * documentation.
     *
     * @return the value
     * @see #value()
     * @since 1.0.0
     */
    public T value() { return value; }

    /**
     * Returns the elapsed time in milliseconds. Zero for a null
     * duration.
     *
     * @return the milliseconds
     * @see #nanos()
     * @see #seconds()
     * @since 1.0.0
     */
    public long millis() { return duration == null ? 0 : duration.toMillis(); }

    /**
     * Returns the elapsed time in nanoseconds. Zero for a null
     * duration.
     *
     * @return the nanoseconds
     * @see #millis()
     * @since 1.0.0
     */
    public long nanos() { return duration == null ? 0 : duration.toNanos(); }

    /**
     * Returns the elapsed time in seconds (as a double for fractional
     * precision). Zero for a null duration.
     *
     * @return the seconds
     * @see #millis()
     * @since 1.0.0
     */
    public double seconds() { return duration == null ? 0 : duration.toMillis() / 1000.0; }
}
