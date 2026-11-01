package chirp.decorator;

import chirp.internal.Trace;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Functional decorators for wrapping operations with cross-cutting concerns.
 *
 * <h2>Retry overloads</h2>
 * <ul>
 *   <li>{@code retry(int)} — returns a {@link Retry} builder to configure</li>
 *   <li>{@code retry(int, Supplier<T>)} — runs with {@code attempts}</li>
 *   <li>{@code retry(int, Runnable)} — runs with {@code attempts}</li>
 *   <li>{@code retry(Supplier<T>)} — one-shot with default policy</li>
 *   <li>{@code retry(Runnable)} — one-shot with default policy</li>
 * </ul>
 */
public final class Decorator {

    public enum Key {
        RETRIES, BACKOFF_MS, EXPONENTIAL, TIMEOUT_MS, LABEL, MEASURE, VERBOSE, FALLBACK
    }

    private Decorator() {}

    // ============================================================== timing

    public static Duration timed(Runnable action) {
        if (action == null) return Duration.ZERO;
        long start = System.nanoTime();
        try {
            action.run();
        } finally {}
        return Duration.ofNanos(System.nanoTime() - start);
    }
    public static Duration measure(Runnable action) { return timed(action); }
    public static Duration withTimer(Runnable action) { return timed(action); }
    public static Duration elapsed(Runnable action) { return timed(action); }

    public static <T> Timed<T> timed(Supplier<T> supplier) {
        if (supplier == null) return new Timed<>(null, Duration.ZERO);
        long start = System.nanoTime();
        T value = supplier.get();
        return new Timed<>(value, Duration.ofNanos(System.nanoTime() - start));
    }
    public static <T> Timed<T> measure(Supplier<T> supplier) { return timed(supplier); }
    public static <T> Timed<T> withTimer(Supplier<T> supplier) { return timed(supplier); }
    public static <T> Timed<T> elapsed(Supplier<T> supplier) { return timed(supplier); }

    // ============================================================== retry (builder)

    public static Retry retry(int attempts) { return Retry.of(attempts); }
    public static Retry retried(int attempts) { return Retry.of(attempts); }
    public static Retry withRetry(int attempts) { return Retry.of(attempts); }

    // ============================================================== retry (executors)

    public static void retry(Runnable action) { Retry.defaults().run(action); }
    public static <T> T retry(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }
    public static <T> T retried(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }
    public static <T> T withRetry(Supplier<T> supplier) { return Retry.defaults().execute(supplier); }

    // ============================================================== retry (int + executor) — NEW

    public static <T> T retry(int attempts, Supplier<T> supplier) {
        return Retry.of(attempts, Duration.ofMillis(500)).execute(supplier);
    }
    public static void retry(int attempts, Runnable action) {
        Retry.of(attempts, Duration.ofMillis(500)).run(action);
    }

    // ============================================================== safe

    public static <T> Optional<T> safe(Supplier<T> supplier) {
        if (supplier == null) return Optional.empty();
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable t) {
            Trace.warn("decorator", "safe() swallowed: {}", t.getMessage());
            return Optional.empty();
        }
    }
    public static <T> Optional<T> safely(Supplier<T> supplier) { return safe(supplier); }
    public static <T> Optional<T> attempt(Supplier<T> supplier) { return safe(supplier); }
    public static <T> Optional<T> suppressing(Supplier<T> supplier) { return safe(supplier); }

    public static boolean safe(Runnable action) {
        if (action == null) return true;
        try {
            action.run();
            return true;
        } catch (Throwable t) {
            Trace.warn("decorator", "safe() swallowed: {}", t.getMessage());
            return false;
        }
    }

    // ============================================================== cache

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T cached(String key, Supplier<T> supplier) {
        if (key == null || supplier == null) return supplier == null ? null : supplier.get();
        Object hit = CACHE.get(key);
        if (hit != null) return (T) hit;
        T value = supplier.get();
        if (value != null) CACHE.put(key, value);
        return value;
    }
    public static <T> T memoized(String key, Supplier<T> supplier) { return cached(key, supplier); }
    public static <T> T memoize(String key, Supplier<T> supplier) { return cached(key, supplier); }

    public static void invalidate(String key) { if (key != null) CACHE.remove(key); }
    public static void clearCache() { CACHE.clear(); }
    public static int cacheSize() { return CACHE.size(); }

    // ============================================================== logged

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
    public static void logging(String label, Runnable action) { logged(label, action); }
    public static void withLogging(String label, Runnable action) { logged(label, action); }

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
    public static <T> T logging(String label, Supplier<T> supplier) { return logged(label, supplier); }
    public static <T> T withLogging(String label, Supplier<T> supplier) { return logged(label, supplier); }

    // ============================================================== measured

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

    public static <T> T withFallback(Supplier<T> supplier, T fallback) {
        if (supplier == null) return fallback;
        try {
            return supplier.get();
        } catch (Throwable t) {
            Trace.warn("decorator", "withFallback caught: {}", t.getMessage());
            return fallback;
        }
    }
    public static <T> T fallback(Supplier<T> supplier, T fallback) { return withFallback(supplier, fallback); }
    public static <T> T orElse(Supplier<T> supplier, T fallback) { return withFallback(supplier, fallback); }

    public static <T> T withFallback(Supplier<T> primary, Supplier<T> secondary, T lastResort) {
        try {
            if (primary != null) return primary.get();
        } catch (Throwable t1) {
            Trace.warn("decorator", "primary failed: {}", t1.getMessage());
            try {
                if (secondary != null) return secondary.get();
            } catch (Throwable t2) {
                Trace.warn("decorator", "secondary failed: {}", t2.getMessage());
            }
        }
        return lastResort;
    }

    // ============================================================== key-value runner

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
