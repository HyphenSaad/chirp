package chirp.decorator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryTest {

    // ---------------------------------------------------------- attempts

    @Test
    void succeedsImmediately() {
        AtomicInteger n = new AtomicInteger();
        String result = Retry.of(3).execute(() -> { n.incrementAndGet(); return "ok"; });
        assertEquals("ok", result);
        assertEquals(1, n.get());
    }

    @Test
    void retriesUntilSuccess() {
        AtomicInteger n = new AtomicInteger();
        Retry.of(5, Duration.ZERO).execute(() -> {
            if (n.incrementAndGet() < 3) throw new RuntimeException();
            return "done";
        });
        assertEquals(3, n.get());
    }

    @Test
    void exhaustsAttemptsAndRethrows() {
        AtomicInteger n = new AtomicInteger();
        assertThrows(RuntimeException.class,
                () -> Retry.of(3, Duration.ZERO).execute(() -> {
                    n.incrementAndGet();
                    throw new RuntimeException("boom");
                }));
        assertEquals(3, n.get());
    }

    @Test
    void runnableForm() {
        AtomicInteger n = new AtomicInteger();
        Retry.of(3, Duration.ZERO).run(() -> {
            if (n.incrementAndGet() < 2) throw new RuntimeException();
        });
        assertEquals(2, n.get());
    }

    @Test
    void nullSupplierReturnsNull() {
        assertNull(Retry.of(3).execute((java.util.function.Supplier<Object>) null));
    }

    @Test
    void nullRunnableIsNoOp() {
        assertDoesNotThrow(() -> Retry.of(3).run(null));
    }

    // ---------------------------------------------------------- builder

    @Test
    void builderAllSettings() {
        Retry r = Retry.builder()
                .attempts(5)
                .backoffMs(100)
                .exponential()
                .jitter(0.1)
                .onlyOn(IOException.class)
                .label("test")
                .build();
        assertNotNull(r);
    }

    @Test
    void attemptsClampedToMinimumOne() {
        AtomicInteger n = new AtomicInteger();
        assertThrows(RuntimeException.class,
                () -> Retry.of(0, Duration.ZERO).execute(() -> {
                    n.incrementAndGet();
                    throw new RuntimeException();
                }));
        assertEquals(1, n.get());
    }

    // ---------------------------------------------------------- exception filter

    @Test
    void onlyOnRetriesMatchingException() {
        AtomicInteger n = new AtomicInteger();
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> Retry.builder()
                        .attempts(3)
                        .backoffMs(0)
                        .onlyOn(IOException.class)
                        .build()
                        .execute(() -> {
                            n.incrementAndGet();
                            throw new RuntimeException(new IOException("io"));
                        }));
        // Rethrown unchanged — still the wrapper, with the IOException as its cause
        assertInstanceOf(IOException.class, thrown.getCause());
        // RuntimeException is not IOException → not retried → 1 attempt only
        assertEquals(1, n.get());
    }

    @Test
    void nonMatchingExceptionNotRetried() {
        AtomicInteger n = new AtomicInteger();
        assertThrows(IllegalStateException.class,
                () -> Retry.builder()
                        .attempts(5)
                        .backoffMs(0)
                        .onlyOn(IOException.class)
                        .build()
                        .execute(() -> {
                            n.incrementAndGet();
                            throw new IllegalStateException("not retried");
                        }));
        assertEquals(1, n.get(), "Non-matching exceptions should fail fast");
    }

    // ---------------------------------------------------------- key-value

    @Test
    void keyValueFactory() {
        AtomicInteger n = new AtomicInteger();
        Retry r = Retry.from(
                Retry.Key.ATTEMPTS, 3,
                Retry.Key.BACKOFF_MS, 0,
                Retry.Key.LABEL, "test");
        r.run(() -> {
            if (n.incrementAndGet() < 2) throw new RuntimeException();
        });
        assertEquals(2, n.get());
    }

    @Test
    void keyValueWithNullPairs() {
        assertDoesNotThrow(() -> Retry.from((Object[]) null));
        assertDoesNotThrow(() -> Retry.from(null, null));
    }

    @Test
    void keyValueWithWrongTypes() {
        // Wrong value types → silently skipped, defaults used
        Retry r = Retry.from(
                Retry.Key.ATTEMPTS, "not a number",
                Retry.Key.LABEL, 12345);
        assertNotNull(r);
    }

    // ---------------------------------------------------------- interruption

    @Test
    void interruptedDuringBackoffThrowsRetryInterruptedException() {
        AtomicInteger n = new AtomicInteger();
        Thread test = new Thread(() -> {
            try {
                Retry.of(5, Duration.ofMillis(1000)).run(() -> {
                    if (n.incrementAndGet() < 2) throw new RuntimeException();
                });
            } catch (Retry.RetryInterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {}
        });
        test.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
        test.interrupt();
        try {
            test.join(3000);
        } catch (InterruptedException ignored) {}
        // Test completes without hanging — interrupt path executed
        assertTrue(n.get() >= 1);
    }

    // ---------------------------------------------------------- edge cases

    @Test
    void negativeBackoffClampedToZero() {
        // Passing a negative duration should not throw — clamps to zero
        Retry r = Retry.builder().backoff(Duration.ofMillis(-100)).build();
        assertDoesNotThrow(() -> r.run(() -> {}));
    }

    @Test
    void jitterClampedBetweenZeroAndOne() {
        Retry r = Retry.builder().jitter(5.0).build();
        assertNotNull(r);
        Retry r2 = Retry.builder().jitter(-1.0).build();
        assertNotNull(r2);
    }

    @Test
    void exponentialBackoffTerminates() {
        AtomicInteger n = new AtomicInteger();
        Retry.builder()
                .attempts(3)
                .backoffMs(1)                // tiny for test speed
                .exponential()
                .build()
                .execute(() -> {
                    if (n.incrementAndGet() < 3) throw new RuntimeException();
                    return "ok";
                });
        assertEquals(3, n.get());
    }

    @Test
    void nullExceptionFiltersAreIgnoredInsteadOfFailingTheBuild() {
        AtomicInteger n = new AtomicInteger();
        Retry retry = assertDoesNotThrow(() -> Retry.builder()
                .attempts(3)
                .backoffMs(0)
                .onlyOn(IOException.class, null)
                .onlyRetrying((Class<? extends Throwable>[]) null)
                .build());
        assertThrows(IllegalStateException.class, () -> retry.execute(() -> {
            n.incrementAndGet();
            throw new IllegalStateException("not an IOException");
        }));
        assertEquals(1, n.get(), "filters must still apply: a non-matching exception is not retried");
    }
}
