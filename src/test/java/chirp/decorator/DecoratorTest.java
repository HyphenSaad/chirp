package chirp.decorator;

import chirp.internal.Trace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase-1 decorator tests — every public method with explicit casts where
 * needed to disambiguate Supplier from Runnable (throw-only lambdas).
 */
class DecoratorTest {

    @BeforeEach
    void setup() { Decorator.clearCache(); Trace.reset(); }
    @AfterEach
    void tearDown() { Decorator.clearCache(); Trace.reset(); }

    // ============================================================== timed (runnable)

    @Test
    void timedReturnsDuration() {
        Duration d = Decorator.timed(() -> {});
        assertNotNull(d);
        assertTrue(d.toNanos() >= 0);
    }

    @Test
    void timedWithNullRunnableReturnsZero() {
        assertEquals(Duration.ZERO, Decorator.timed((Runnable) null));
    }

    @Test
    void timedAliasesMatch() {
        assertEquals(Duration.ZERO, Decorator.measure((Runnable) null));
        assertEquals(Duration.ZERO, Decorator.withTimer((Runnable) null));
        assertEquals(Duration.ZERO, Decorator.elapsed((Runnable) null));
    }

    @Test
    void timedRethrowsException() {
        assertThrows(RuntimeException.class,
                () -> Decorator.timed(() -> { throw new RuntimeException("boom"); }));
    }

    // ============================================================== timed (supplier)

    @Test
    void timedSupplierReturnsValueAndDuration() {
        Timed<Integer> result = Decorator.timed(() -> 42);
        assertEquals(42, result.value());
        assertTrue(result.millis() >= 0);
    }

    @Test
    void timedNullSupplier() {
        Timed<Object> result = Decorator.timed((Supplier<Object>) null);
        assertNull(result.value());
        assertEquals(Duration.ZERO, result.duration());
    }

    // ============================================================== retry

    @Test
    void retrySucceedsFirstTime() {
        AtomicInteger attempts = new AtomicInteger();
        String result = Decorator.retry(3, () -> {
            attempts.incrementAndGet();
            return "ok";
        });
        assertEquals("ok", result);
        assertEquals(1, attempts.get());
    }

    @Test
    void retrySucceedsAfterFailures() {
        AtomicInteger attempts = new AtomicInteger();
        String result = Decorator.retry(3, () -> {
            int n = attempts.incrementAndGet();
            if (n < 3) throw new RuntimeException("not yet");
            return "finally";
        });
        assertEquals("finally", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void retryExhaustedRethrows() {
        AtomicInteger attempts = new AtomicInteger();
        // Explicit cast — throw-only lambda is ambiguous between Supplier/Runnable.
        assertThrows(RuntimeException.class,
                () -> Decorator.retry(2, (Supplier<Object>) () -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("always");
                }));
        assertEquals(2, attempts.get());
    }

    @Test
    void retryNullSupplierReturnsNull() {
        assertNull(Decorator.retry((Supplier<Object>) null));
    }

    // ============================================================== safe

    @Test
    void safeReturnsValue() {
        Optional<String> result = Decorator.safe(() -> "hi");
        assertTrue(result.isPresent());
        assertEquals("hi", result.get());
    }

    @Test
    void safeSwallowsException() {
        Optional<Object> result = Decorator.safe(
                (Supplier<Object>) () -> { throw new RuntimeException("boom"); });
        assertTrue(result.isEmpty());
    }

    @Test
    void safeSwallowsNPE() {
        Optional<Object> result = Decorator.safe(
                (Supplier<Object>) () -> { throw new NullPointerException(); });
        assertTrue(result.isEmpty());
    }

    @Test
    void safeRunnableReturnsTrueOnSuccess() {
        assertTrue(Decorator.safe(() -> {}));
    }

    @Test
    void safeRunnableReturnsFalseOnFailure() {
        // Explicit cast — avoids the ambiguity for throw-only lambdas.
        assertFalse(Decorator.safe((Runnable) () -> { throw new RuntimeException(); }));
    }

    @Test
    void safeNullSupplier() {
        assertTrue(Decorator.safe((Supplier<Object>) null).isEmpty());
    }

    // ============================================================== cache

    @Test
    void cacheCachesFirstResult() {
        AtomicInteger calls = new AtomicInteger();
        String a = Decorator.cached("k1", () -> { calls.incrementAndGet(); return "v"; });
        String b = Decorator.cached("k1", () -> { calls.incrementAndGet(); return "v"; });
        assertEquals("v", a);
        assertEquals("v", b);
        assertEquals(1, calls.get());
    }

    @Test
    void cacheDifferentKeysDontCollide() {
        String a = Decorator.cached("a", () -> "one");
        String b = Decorator.cached("b", () -> "two");
        assertEquals("one", a);
        assertEquals("two", b);
    }

    @Test
    void cacheInvalidate() {
        AtomicInteger calls = new AtomicInteger();
        Decorator.cached("k1", () -> { calls.incrementAndGet(); return "v"; });
        Decorator.invalidate("k1");
        Decorator.cached("k1", () -> { calls.incrementAndGet(); return "v"; });
        assertEquals(2, calls.get());
    }

    @Test
    void cacheClear() {
        Decorator.cached("x", () -> 1);
        Decorator.clearCache();
        assertEquals(0, Decorator.cacheSize());
    }

    @Test
    void cacheNullValueNotCached() {
        AtomicInteger calls = new AtomicInteger();
        Decorator.cached("k1", () -> { calls.incrementAndGet(); return null; });
        Decorator.cached("k1", () -> { calls.incrementAndGet(); return null; });
        assertEquals(2, calls.get());
    }

    @Test
    void cacheNullKeyCallsSupplierEachTime() {
        AtomicInteger calls = new AtomicInteger();
        Decorator.cached(null, () -> calls.incrementAndGet());
        Decorator.cached(null, () -> calls.incrementAndGet());
        assertEquals(2, calls.get());
    }

    // ============================================================== logged

    @Test
    void loggedDoesNotThrow() {
        assertDoesNotThrow(() -> Decorator.logged("test", () -> {}));
    }

    @Test
    void loggedNullIsNoOp() {
        assertDoesNotThrow(() -> Decorator.logged("test", (Runnable) null));
    }

    @Test
    void loggedSupplierReturnsValue() {
        assertEquals("hello", Decorator.logged("test", () -> "hello"));
    }

    // ============================================================== measured

    @Test
    void measuredReturnsDuration() {
        Duration d = Decorator.measured(() -> {});
        assertNotNull(d);
    }

    @Test
    void measuredSupplierReturnsValue() {
        Timed<Integer> result = Decorator.measured(() -> 7);
        assertEquals(7, result.value());
    }

    @Test
    void measuredNullDoesNotThrow() {
        assertEquals(Duration.ZERO, Decorator.measured((Runnable) null));
    }

    // ============================================================== withFallback

    @Test
    void withFallbackReturnsValueWhenSuccess() {
        assertEquals("value", Decorator.withFallback(() -> "value", "fallback"));
    }

    @Test
    void withFallbackReturnsFallbackOnException() {
        assertEquals("fallback",
                Decorator.withFallback((Supplier<String>) () -> { throw new RuntimeException("x"); },
                        "fallback"));
    }

    @Test
    void withFallbackNullSupplier() {
        assertEquals("fallback",
                Decorator.withFallback((Supplier<String>) null, "fallback"));
    }

    @Test
    void withTwoFallbacks() {
        String result = Decorator.withFallback(
                (Supplier<String>) () -> { throw new RuntimeException(); },
                () -> "secondary",
                "last");
        assertEquals("secondary", result);
    }

    @Test
    void withTwoFallbacksLastResort() {
        String result = Decorator.withFallback(
                (Supplier<String>) () -> { throw new RuntimeException(); },
                () -> {
                    throw new RuntimeException();
                },
                "last");
        assertEquals("last", result);
    }

    // ============================================================== key-value runner

    @Test
    void keyValueRunsAction() {
        AtomicInteger calls = new AtomicInteger();
        Decorator.run(calls::incrementAndGet,
                Decorator.Key.RETRIES, 1,
                Decorator.Key.LABEL, "test");
        assertEquals(1, calls.get());
    }

    @Test
    void keyValueWithNullsIsSafe() {
        assertDoesNotThrow(() -> Decorator.run(null));
        assertDoesNotThrow(() -> Decorator.run(() -> {}, (Object[]) null));
    }
}
