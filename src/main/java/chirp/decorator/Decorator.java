package chirp.decorator;

import chirp.internal.Trace;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Functional decorators — wrap an operation with a cross-cutting
 * concern and return the result unchanged.
 *
 * <p>Each method takes an operation (a {@link Runnable} or a
 * {@link Supplier}) and returns a decorated value or a wrapped result.
 * The operation's behaviour is unchanged; only the concern is added —
 * timing, retry, error tolerance, caching, logging, fallback.</p>
 *
 * <h2>The eight concerns</h2>
 * <table border="1">
 *   <caption>Cross-cutting concerns and their entry points</caption>
 *   <tr><th>Concern</th><th>Method</th><th>Returns</th></tr>
 *   <tr><td>Timing</td><td>{@link #timed(Runnable)}</td>
 *       <td>{@link Duration}</td></tr>
 *   <tr><td>Timing + value</td><td>{@link #timed(Supplier)}</td>
 *       <td>{@link Timed}</td></tr>
 *   <tr><td>Retry</td><td>{@link #retry(int, Supplier)}</td>
 *       <td>the value (or throws after all attempts)</td></tr>
 *   <tr><td>Error tolerance</td><td>{@link #safe(Supplier)}</td>
 *       <td>{@link Optional} — empty on exception</td></tr>
 *   <tr><td>Caching</td><td>{@link #cached(String, Supplier)}</td>
 *       <td>the value (from cache or freshly computed)</td></tr>
 *   <tr><td>Logging</td><td>{@link #logged(String, Runnable)}</td>
 *       <td>nothing (runs the action, emits trace events)</td></tr>
 *   <tr><td>Measurement</td><td>{@link #measured(Runnable)}</td>
 *       <td>{@link Duration} (also logs heap delta when tracing)</td></tr>
 *   <tr><td>Fallback</td><td>{@link #withFallback(Supplier, Object)}</td>
 *       <td>the value or the fallback on exception</td></tr>
 * </table>
 *
 * <h2>Example — composing concerns</h2>
 * <pre>{@code
 * // Time a request
 * Duration took = Decorator.timed(() -> Http.get(url).send());
 *
 * // Try to fetch, get an empty Optional on failure
 * Optional<String> result = Decorator.safe(() -> Http.get(url).send().text());
 *
 * // Retry 3 times on any failure
 * String data = Decorator.retry(3, () -> Http.get(url).send().text());
 *
 * // Cache — first call computes, subsequent calls hit the cache
 * String config = Decorator.cached("config", () -> loadConfig());
 * Decorator.invalidate("config");   // clear for a specific key
 * }</pre>
 *
 * <h2>Retry overloads</h2>
 * <ul>
 *   <li>{@code retry(int)} → returns a {@link Retry} builder (fluent)</li>
 *   <li>{@code retry(int, Supplier)} → runs with {@code attempts}</li>
 *   <li>{@code retry(int, Runnable)} → runs with {@code attempts}</li>
 *   <li>{@code retry(Supplier)} → one-shot with default policy (3 × 500ms)</li>
 *   <li>{@code retry(Runnable)} → one-shot with default policy</li>
 * </ul>
 * <p>For advanced retry (exponential backoff, jitter, exception filters),
 * use the {@link Retry} builder directly.</p>
 *
 * <h2>Caching</h2>
 * <p>{@link #cached(String, Supplier)} uses a process-wide
 * {@link ConcurrentHashMap}. The cache is not bounded — the caller is
 * responsible for calling {@link #invalidate(String)} or
 * {@link #clearCache()} when the underlying data changes. Null values
 * are not cached (a null-returning supplier always recomputes).</p>
 *
 * <h2>Related</h2>
 * <p>For full retry control, see {@link Retry}. For the timing
 * record returned by {@code timed(Supplier)}, see {@link Timed}.</p>
 *
 * @see Retry
 * @see Timed
 * @since 1.0.0
 */
public final class Decorator {

    /**
     * Keys for the key-value {@link #run(Runnable, Object...)} API.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Number of retry attempts — {@link Integer}. */
        RETRIES,
        /** Backoff between attempts in ms — {@link Integer}. */
        BACKOFF_MS,
        /** Whether to use exponential backoff — {@link Boolean}. */
        EXPONENTIAL,
        /** Timeout in ms — {@link Integer}. Reserved. */
        TIMEOUT_MS,
        /** Label for logging — {@link String}. */
        LABEL,
        /** Whether to measure — {@link Boolean}. */
        MEASURE,
        /** Whether to enable verbose tracing — {@link Boolean}. Reserved. */
        VERBOSE,
        /** Fallback value — reserved. */
        FALLBACK
    }

    private Decorator() {}

    // ============================================================== timing

    /**
     * Runs a {@link Runnable} and returns the elapsed wall-clock time.
     *
     * <pre>{@code
     * Duration took = Decorator.timed(() -> Http.get(url).send());
     * System.out.println("Took " + took.toMillis() + "ms");
     * }</pre>
     *
     * @param action the action to time (null → returns Duration.ZERO)
     * @return the elapsed time
     * @see #timed(Supplier)
     * @see #measure(Runnable)
     * @see #withTimer(Runnable)
     * @see #elapsed(Runnable)
     * @since 1.0.0
     */
    public static Duration timed(Runnable action) {
        if (action == null) return Duration.ZERO;
        long start = System.nanoTime();
        try {
            action.run();
        } finally {}
        return Duration.ofNanos(System.nanoTime() - start);
    }

    /**
     * Alias of {@link #timed(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param action the action to time
     * @return the elapsed time
     * @see #timed(Runnable)
     * @since 1.0.0
     */
    public static Duration measure(Runnable action) { return timed(action); }

    /**
     * Alias of {@link #timed(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param action the action to time
     * @return the elapsed time
     * @see #timed(Runnable)
     * @since 1.0.0
     */
    public static Duration withTimer(Runnable action) { return timed(action); }

    /**
     * Alias of {@link #timed(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param action the action to time
     * @return the elapsed time
     * @see #timed(Runnable)
     * @since 1.0.0
     */
    public static Duration elapsed(Runnable action) { return timed(action); }

    /**
     * Runs a {@link Supplier} and returns both its result and the
     * elapsed time.
     *
     * <pre>{@code
     * Timed<JsonObject> result = Decorator.timed(() -> Http.get(url).send().object());
     * System.out.println("Fetched in " + result.millis() + "ms");
     * JsonObject user = result.value();
     * }</pre>
     *
     * @param supplier the supplier to time (null → returns null Timed)
     * @param <T> the return type
     * @return a {@link Timed} wrapping the value and duration
     * @see #timed(Runnable)
     * @see Timed
     * @since 1.0.0
     */
    public static <T> Timed<T> timed(Supplier<T> supplier) {
        if (supplier == null) return new Timed<>(null, Duration.ZERO);
        long start = System.nanoTime();
        T value = supplier.get();
        return new Timed<>(value, Duration.ofNanos(System.nanoTime() - start));
    }

    /**
     * Alias of {@link #timed(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to time
     * @param <T> the return type
     * @return a {@link Timed} wrapping the value and duration
     * @see #timed(Supplier)
     * @since 1.0.0
     */
    public static <T> Timed<T> measure(Supplier<T> supplier) { return timed(supplier); }

    /**
     * Alias of {@link #timed(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to time
     * @param <T> the return type
     * @return a {@link Timed} wrapping the value and duration
     * @see #timed(Supplier)
     * @since 1.0.0
     */
    public static <T> Timed<T> withTimer(Supplier<T> supplier) { return timed(supplier); }

    /**
     * Alias of {@link #timed(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to time
     * @param <T> the return type
     * @return a {@link Timed} wrapping the value and duration
     * @see #timed(Supplier)
     * @since 1.0.0
     */
    public static <T> Timed<T> elapsed(Supplier<T> supplier) { return timed(supplier); }

    // ============================================================== retry (builder)

    /**
     * Returns a fluent {@link Retry} builder pre-configured with the
     * given number of attempts. Use {@link Retry.Builder} methods to
     * add backoff, jitter, or exception filters, then call
     * {@code execute(supplier)} or {@code run(action)}.
     *
     * <pre>{@code
     * Retry.builder()
     *     .attempts(5)
     *     .backoffMs(250)
     *     .exponential()
     *     .onlyOn(IOException.class)
     *     .build()
     *     .execute(() -> fetch());
     * }</pre>
     *
     * @param attempts the number of attempts (≥ 1)
     * @return a new retry builder
     * @see Retry
     * @see #retry(int, Supplier)
     * @see #retry(int, Runnable)
     * @since 1.0.0
     */
    public static Retry retry(int attempts) { return Retry.of(attempts); }

    /**
     * Alias of {@link #retry(int)} — see that method for full
     * documentation and examples.
     *
     * @param attempts the number of attempts
     * @return a new retry builder
     * @see #retry(int)
     * @since 1.0.0
     */
    public static Retry retried(int attempts) { return Retry.of(attempts); }

    /**
     * Alias of {@link #retry(int)} — see that method for full
     * documentation and examples.
     *
     * @param attempts the number of attempts
     * @return a new retry builder
     * @see #retry(int)
     * @since 1.0.0
     */
    public static Retry withRetry(int attempts) { return Retry.of(attempts); }

    // ============================================================== retry (executors)

    /**
     * Runs a {@link Runnable}, retrying up to 3 times with 500ms
     * backoff on any failure.
     *
     * @param action the action to run (null → no-op)
     * @see Retry
     * @see #retry(int)
     * @see #retry(int, Runnable)
     * @since 1.0.0
     */
    public static void retry(Runnable action) { Retry.defaults().run(action); }

    /**
     * Runs a {@link Supplier}, retrying up to 3 times with 500ms
     * backoff on any failure. Returns the successful value.
     *
     * <pre>{@code
     * String data = Decorator.retry(() -> Http.get(url).send().text());
     * }</pre>
     *
     * @param supplier the supplier to run (null → null)
     * @param <T> the return type
     * @return the value, or null for a null supplier
     * @throws RuntimeException after all attempts fail
     * @see #retry(int, Supplier)
     * @since 1.0.0
     */
    public static <T> T retry(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }

    /**
     * Alias of {@link #retry(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value
     * @see #retry(Supplier)
     * @since 1.0.0
     */
    public static <T> T retried(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }

    /**
     * Alias of {@link #retry(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value
     * @see #retry(Supplier)
     * @since 1.0.0
     */
    public static <T> T withRetry(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }

    /**
     * Runs a {@link Supplier}, retrying up to {@code attempts} times
     * with 500ms backoff on any failure.
     *
     * @param attempts the number of attempts (≥ 1)
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value
     * @throws RuntimeException after all attempts fail
     * @see #retry(int, Runnable)
     * @see #retry(int)
     * @see Retry#execute(Supplier)
     * @since 1.0.0
     */
    public static <T> T retry(int attempts, Supplier<T> supplier) {
        return Retry.of(attempts, Duration.ofMillis(500)).execute(supplier);
    }

    /**
     * Runs a {@link Runnable}, retrying up to {@code attempts} times
     * with 500ms backoff on any failure.
     *
     * @param attempts the number of attempts (≥ 1)
     * @param action the action to run
     * @throws RuntimeException after all attempts fail
     * @see #retry(int, Supplier)
     * @see #retry(int)
     * @since 1.0.0
     */
    public static void retry(int attempts, Runnable action) {
        Retry.of(attempts, Duration.ofMillis(500)).run(action);
    }

    // ============================================================== safe

    /**
     * Runs a {@link Supplier} and returns its result wrapped in an
     * {@link Optional}. If the supplier throws, returns
     * {@link Optional#empty()} — the exception is logged via
     * {@link Trace#warn(String, String)}.
     *
     * <pre>{@code
     * Optional<String> data = Decorator.safe(() -> Http.get(url).send().text());
     *
     * if (data.isPresent()) {
     *     System.out.println(data.get());
     * } else {
     *     System.out.println("Request failed");
     * }
     * }</pre>
     *
     * @param supplier the supplier to run (null → empty)
     * @param <T> the return type
     * @return the value, or empty on failure
     * @see #safe(Runnable)
     * @see #safely(Supplier)
     * @since 1.0.0
     */
    public static <T> Optional<T> safe(Supplier<T> supplier) {
        if (supplier == null) return Optional.empty();
        try {
            return Optional.ofNullable(supplier.get());
        } catch (RuntimeException t) {
            Trace.warn("decorator", "safe() swallowed: {}", t.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Alias of {@link #safe(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value, or empty on failure
     * @see #safe(Supplier)
     * @since 1.0.0
     */
    public static <T> Optional<T> safely(Supplier<T> supplier) { return safe(supplier); }

    /**
     * Alias of {@link #safe(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value, or empty on failure
     * @see #safe(Supplier)
     * @since 1.0.0
     */
    public static <T> Optional<T> attempt(Supplier<T> supplier) { return safe(supplier); }

    /**
     * Alias of {@link #safe(Supplier)} — see that method for full
     * documentation and examples.
     *
     * @param supplier the supplier to run
     * @param <T> the return type
     * @return the value, or empty on failure
     * @see #safe(Supplier)
     * @since 1.0.0
     */
    public static <T> Optional<T> suppressing(Supplier<T> supplier) { return safe(supplier); }

    /**
     * Runs a {@link Runnable} and returns {@code true} on success,
     * {@code false} on failure. The failure is logged via
     * {@link Trace#warn(String, String)}.
     *
     * @param action the action to run (null → true)
     * @return {@code true} if the action completed without exception
     * @see #safe(Supplier)
     * @since 1.0.0
     */
    public static boolean safe(Runnable action) {
        if (action == null) return true;
        try {
            action.run();
            return true;
        } catch (RuntimeException t) {
            Trace.warn("decorator", "safe() swallowed: {}", t.getMessage());
            return false;
        }
    }

    // ============================================================== cache

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    /**
     * Returns the cached value for the key, or computes it via the
     * supplier and stores it for future calls.
     *
     * <p>Null values are not cached — a null-returning supplier
     * recomputes on every call. Null keys are not cached either.</p>
     *
     * <pre>{@code
     * String config = Decorator.cached("config", () -> loadConfig());
     * // first call: runs loadConfig(), caches the result
     * // subsequent calls: return the cached value
     * }</pre>
     *
     * @param key the cache key (null → supplier runs every time)
     * @param supplier the value supplier (null → null)
     * @param <T> the value type
     * @return the cached or freshly-computed value
     * @see #memoized(String, Supplier)
     * @see #invalidate(String)
     * @see #clearCache()
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T cached(String key, Supplier<T> supplier) {
        if (key == null || supplier == null) return supplier == null ? null : supplier.get();
        Object hit = CACHE.get(key);
        if (hit != null) return (T) hit;
        T value = supplier.get();
        if (value != null) CACHE.put(key, value);
        return value;
    }

    /**
     * Alias of {@link #cached(String, Supplier)} — see that method for
     * full documentation and examples.
     *
     * @param key the cache key
     * @param supplier the value supplier
     * @param <T> the value type
     * @return the cached or freshly-computed value
     * @see #cached(String, Supplier)
     * @since 1.0.0
     */
    public static <T> T memoized(String key, Supplier<T> supplier) { return cached(key, supplier); }

    /**
     * Alias of {@link #cached(String, Supplier)} — see that method for
     * full documentation and examples.
     *
     * @param key the cache key
     * @param supplier the value supplier
     * @param <T> the value type
     * @return the cached or freshly-computed value
     * @see #cached(String, Supplier)
     * @since 1.0.0
     */
    public static <T> T memoize(String key, Supplier<T> supplier) { return cached(key, supplier); }

    /**
     * Removes the given key from the cache. A null key is a no-op.
     *
     * @param key the cache key
     * @see #cached(String, Supplier)
     * @see #clearCache()
     * @since 1.0.0
     */
    public static void invalidate(String key) { if (key != null) CACHE.remove(key); }

    /**
     * Clears every entry from the cache.
     *
     * @see #invalidate(String)
     * @see #cacheSize()
     * @since 1.0.0
     */
    public static void clearCache() { CACHE.clear(); }

    /**
     * Returns the number of cached entries.
     *
     * @return the cache size
     * @since 1.0.0
     */
    public static int cacheSize() { return CACHE.size(); }

    // ============================================================== logged

    /**
     * Runs an action, emitting a debug trace line before and after —
     * {@code "→ label"} and {@code "← label"}. If tracing is disabled,
     * the lines are not emitted (the action still runs).
     *
     * @param label the label to emit (null → "operation")
     * @param action the action to run (null → no-op)
     * @see #logged(String, Supplier)
     * @see #logging(String, Runnable)
     * @see #withLogging(String, Runnable)
     * @since 1.0.0
     */
    public static void logged(String label, Runnable action) {
        if (action == null) return;
        String tag = label == null ? "operation" : label;
        Trace.debug("decorator", "→ {}", tag);
        try {
            action.run();
        } finally {
            Trace.debug("decorator", "← {}", tag);
        }
    }

    /**
     * Alias of {@link #logged(String, Runnable)} — see that method for
     * full documentation.
     *
     * @param label the label
     * @param action the action
     * @see #logged(String, Runnable)
     * @since 1.0.0
     */
    public static void logging(String label, Runnable action) { logged(label, action); }

    /**
     * Alias of {@link #logged(String, Runnable)} — see that method for
     * full documentation.
     *
     * @param label the label
     * @param action the action
     * @see #logged(String, Runnable)
     * @since 1.0.0
     */
    public static void withLogging(String label, Runnable action) { logged(label, action); }

    /**
     * Runs a supplier, emitting a debug trace line before and after.
     * Returns the supplier's result.
     *
     * @param label the label (null → "operation")
     * @param supplier the supplier (null → null)
     * @param <T> the return type
     * @return the supplier's value
     * @see #logged(String, Runnable)
     * @since 1.0.0
     */
    public static <T> T logged(String label, Supplier<T> supplier) {
        if (supplier == null) return null;
        String tag = label == null ? "operation" : label;
        Trace.debug("decorator", "→ {}", tag);
        try {
            return supplier.get();
        } finally {
            Trace.debug("decorator", "← {}", tag);
        }
    }

    /**
     * Alias of {@link #logged(String, Supplier)} — see that method for
     * full documentation.
     *
     * @param label the label
     * @param supplier the supplier
     * @param <T> the return type
     * @return the supplier's value
     * @see #logged(String, Supplier)
     * @since 1.0.0
     */
    public static <T> T logging(String label, Supplier<T> supplier) { return logged(label, supplier); }

    /**
     * Alias of {@link #logged(String, Supplier)} — see that method for
     * full documentation.
     *
     * @param label the label
     * @param supplier the supplier
     * @param <T> the return type
     * @return the supplier's value
     * @see #logged(String, Supplier)
     * @since 1.0.0
     */
    public static <T> T withLogging(String label, Supplier<T> supplier) { return logged(label, supplier); }

    // ============================================================== measured

    /**
     * Runs an action and returns the elapsed time. Additionally emits a
     * debug trace line with both the elapsed time and the heap delta
     * (when tracing is enabled).
     *
     * <p>Use this when you need the heap impact too — for a simple
     * timing measurement, {@link #timed(Runnable)} is faster (no
     * {@code Runtime} calls).</p>
     *
     * @param action the action to measure (null → Duration.ZERO)
     * @return the elapsed time
     * @see #timed(Runnable)
     * @see #measured(Supplier)
     * @since 1.0.0
     */
    public static Duration measured(Runnable action) {
        if (action == null) return Duration.ZERO;
        Runtime rt = Runtime.getRuntime();
        long heapStart = rt.totalMemory() - rt.freeMemory();
        long start = System.nanoTime();
        try {
            action.run();
        } finally {
            long elapsed = System.nanoTime() - start;
            long heapEnd = rt.totalMemory() - rt.freeMemory();
            Trace.debug("decorator", "measured: {}ms, heap Δ {}KB",
                    elapsed / 1_000_000L, (heapEnd - heapStart) / 1024);
        }
        return Duration.ofNanos(System.nanoTime() - start);
    }

    /**
     * Runs a supplier and returns both its value and the elapsed time,
     * emitting a debug trace line with the elapsed time and heap delta.
     *
     * @param supplier the supplier to measure (null → null Timed)
     * @param <T> the return type
     * @return a {@link Timed} wrapping the value and duration
     * @see #measured(Runnable)
     * @see #timed(Supplier)
     * @since 1.0.0
     */
    public static <T> Timed<T> measured(Supplier<T> supplier) {
        if (supplier == null) return new Timed<>(null, Duration.ZERO);
        Runtime rt = Runtime.getRuntime();
        long heapStart = rt.totalMemory() - rt.freeMemory();
        long start = System.nanoTime();
        T value = supplier.get();
        long elapsed = System.nanoTime() - start;
        long heapEnd = rt.totalMemory() - rt.freeMemory();
        Trace.debug("decorator", "measured: {}ms, heap Δ {}KB",
                elapsed / 1_000_000L, (heapEnd - heapStart) / 1024);
        return new Timed<>(value, Duration.ofNanos(elapsed));
    }

    // ============================================================== fallback

    /**
     * Runs a supplier. If it throws, returns the fallback value instead.
     * The exception is logged via {@link Trace#warn(String, String)}.
     *
     * <pre>{@code
     * String host = Decorator.withFallback(
     *     () -> config.getProperty("host"),
     *     "localhost");
     * }</pre>
     *
     * @param supplier the supplier to run (null → fallback)
     * @param fallback the value to return on failure
     * @param <T> the return type
     * @return the supplier's value, or the fallback
     * @see #withFallback(Supplier, Supplier, Object)
     * @see #fallback(Supplier, Object)
     * @see #orElse(Supplier, Object)
     * @since 1.0.0
     */
    public static <T> T withFallback(Supplier<T> supplier, T fallback) {
        if (supplier == null) return fallback;
        try {
            return supplier.get();
        } catch (RuntimeException t) {
            Trace.warn("decorator", "withFallback caught: {}", t.getMessage());
            return fallback;
        }
    }

    /**
     * Alias of {@link #withFallback(Supplier, Object)} — see that method
     * for full documentation and examples.
     *
     * @param supplier the supplier
     * @param fallback the fallback value
     * @param <T> the return type
     * @return the value or fallback
     * @see #withFallback(Supplier, Object)
     * @since 1.0.0
     */
    public static <T> T fallback(Supplier<T> supplier, T fallback) { return withFallback(supplier, fallback); }

    /**
     * Alias of {@link #withFallback(Supplier, Object)} — see that method
     * for full documentation and examples. Matches the
     * {@link Optional#orElse(Object)} naming.
     *
     * @param supplier the supplier
     * @param fallback the fallback value
     * @param <T> the return type
     * @return the value or fallback
     * @see #withFallback(Supplier, Object)
     * @since 1.0.0
     */
    public static <T> T orElse(Supplier<T> supplier, T fallback) { return withFallback(supplier, fallback); }

    /**
     * Tries the primary supplier; on failure, tries the secondary; on
     * second failure, returns the last-resort value.
     *
     * <pre>{@code
     * String host = Decorator.withFallback(
     *     () -> loadFromConfig(),
     *     () -> loadFromEnv(),
     *     "localhost");
     * }</pre>
     *
     * @param primary the primary supplier (null → tries secondary)
     * @param secondary the secondary supplier (null → returns lastResort)
     * @param lastResort the value to return if both fail
     * @param <T> the return type
     * @return the first successful value, or lastResort
     * @see #withFallback(Supplier, Object)
     * @since 1.0.0
     */
    public static <T> T withFallback(Supplier<T> primary, Supplier<T> secondary, T lastResort) {
        try {
            if (primary != null) return primary.get();
        } catch (RuntimeException t1) {
            Trace.warn("decorator", "primary failed: {}", t1.getMessage());
            try {
                if (secondary != null) return secondary.get();
            } catch (RuntimeException t2) {
                Trace.warn("decorator", "secondary failed: {}", t2.getMessage());
            }
        }
        return lastResort;
    }

    // ============================================================== key-value runner

    /**
     * Runs an action with key-value configuration for retries, backoff,
     * and measurement. Returns the elapsed time regardless of outcome.
     *
     * <pre>{@code
     * Decorator.run(
     *     () -> Http.get(url).send(),
     *     Decorator.Key.RETRIES, 3,
     *     Decorator.Key.BACKOFF_MS, 250,
     *     Decorator.Key.LABEL, "fetch");
     * }</pre>
     *
     * @param action the action to run (null → Duration.ZERO)
     * @param keyValues alternating {@link Key} → value pairs
     * @return the elapsed time
     * @see #retry(int)
     * @see #measured(Runnable)
     * @since 1.0.0
     */
    public static Duration run(Runnable action, Object... keyValues) {
        if (action == null) return Duration.ZERO;
        Retry.Builder rb = Retry.builder();
        boolean measure = false;
        String label = null;

        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case RETRIES -> {
                        if (v instanceof Integer n) rb.attempts(n);
                    }
                    case BACKOFF_MS -> {
                        if (v instanceof Integer n) rb.backoffMs(n);
                    }
                    case EXPONENTIAL -> {
                        if (v instanceof Boolean b) rb.exponential(b);
                    }
                    case LABEL -> {
                        if (v instanceof String s) label = s;
                    }
                    case MEASURE -> {
                        if (v instanceof Boolean b) measure = b;
                    }
                    default -> {
                        // TIMEOUT_MS, VERBOSE, FALLBACK are recognised key names
                        // but not yet wired into this helper. Reserved for a
                        // future release; silently ignore for now.
                    }
                }
            }
        }

        final boolean doMeasure = measure;
        final String finalLabel = label;

        Runnable wrapped = () -> {
            if (doMeasure) measured(action);
            else if (finalLabel != null) logged(finalLabel, action);
            else action.run();
        };

        long startNs = System.nanoTime();
        rb.build().run(wrapped);
        return Duration.ofNanos(System.nanoTime() - startNs);
    }
}
