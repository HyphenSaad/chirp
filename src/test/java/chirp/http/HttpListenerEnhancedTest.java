package chirp.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HttpListenerEnhancedTest {

    @Test
    void allHooksHaveDefaults() {
        // Every hook must be safe to omit — verify by implementing nothing
        HttpListener noop = new HttpListener() {};
        HttpRequest req = Http.get(null);
        HttpResponse resp = HttpResponse.error(HttpMethod.GET, "u",
                Duration.ZERO, "err");

        assertDoesNotThrow(() -> noop.onStart(req));
        assertDoesNotThrow(() -> noop.onComplete(resp));
        assertDoesNotThrow(() -> noop.onSuccess(resp));
        assertDoesNotThrow(() -> noop.onFailure(req, null));
        assertDoesNotThrow(() -> noop.onRetry(req, 1, null));
        assertDoesNotThrow(() -> noop.onRedirect(resp));
    }

    @Test
    void listenerReceivesStartAndFailureForBadUrl() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicBoolean completed = new AtomicBoolean(false);

        Http.get(null).listener(new HttpListener() {
            @Override
            public void onStart(HttpRequest request) { started.set(true); }
            @Override
            public void onFailure(HttpRequest request, Throwable t) { failed.set(true); }
            @Override
            public void onComplete(HttpResponse response) { completed.set(true); }
        }).send();

        assertTrue(started.get(), "onStart should fire even for null URL");
        assertTrue(failed.get() || completed.get(),
                "onFailure or onComplete should fire on error");
    }

    @Test
    void retryHookIsCalledOnTransportRetry() {
        AtomicInteger retries = new AtomicInteger();

        Http.get(null).listener(new HttpListener() {
            @Override
            public void onRetry(HttpRequest req, int attempt, Throwable cause) {
                retries.incrementAndGet();
            }
        });

        // Directly call the retry-aware path — it triggers onRetry on transport failures.
        // A null URL fails every attempt, so sendWithRetry throws once attempts run out.
        assertThrows(HttpException.class, () -> Http.get(null).listener(new HttpListener() {
            @Override
            public void onRetry(HttpRequest req, int attempt, Throwable cause) {
                retries.incrementAndGet();
            }
        }).sendWithRetry(3));

        // 3 attempts → onRetry fires twice (before attempt 2 and before attempt 3)
        assertEquals(2, retries.get());
    }
}
