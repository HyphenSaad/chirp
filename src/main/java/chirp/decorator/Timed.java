package chirp.decorator;

import java.time.Duration;

/**
 * Result wrapper returned by {@code Decorator.timed(Supplier)} — carries both
 * the operation's return value and the wall-clock duration it took.
 *
 * @param value    the result of the decorated operation
 * @param duration elapsed time (measured with {@code System.nanoTime()})
 * @param <T>      result type
 */
public record Timed<T>(T value, Duration duration) {

    public T result() { return value; }
    public T get() { return value; }
    public T value() { return value; }

    public long millis() { return duration == null ? 0 : duration.toMillis(); }
    public long nanos() { return duration == null ? 0 : duration.toNanos(); }
    public double seconds() { return duration == null ? 0 : duration.toMillis() / 1000.0; }
}
