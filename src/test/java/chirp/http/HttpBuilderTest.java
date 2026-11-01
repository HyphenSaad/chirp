package chirp.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that can run without a real network — they verify the builder,
 * response model, and configuration logic.
 */
class HttpBuilderTest {

    // ---------------------------------------------------- factories

    @Test
    void getCreatesGetRequest() {
        HttpRequest r = Http.get("https://example.com");
        assertEquals(HttpMethod.GET, r.method());
        assertEquals("https://example.com", r.url());
    }

    @Test
    void postCreatesPostRequest() {
        assertEquals(HttpMethod.POST, Http.post("https://example.com").method());
    }

    @Test
    void putCreatesPutRequest() {
        assertEquals(HttpMethod.PUT, Http.put("https://example.com").method());
    }

    @Test
    void deleteCreatesDeleteRequest() {
        assertEquals(HttpMethod.DELETE, Http.delete("https://example.com").method());
    }

    @Test
    void patchCreatesPatchRequest() {
        assertEquals(HttpMethod.PATCH, Http.patch("https://example.com").method());
    }

    @Test
    void requestCreatesAnyMethod() {
        assertEquals(HttpMethod.HEAD, Http.request(HttpMethod.HEAD, "https://x.com").method());
    }

    // ---------------------------------------------------- chaining

    @Test
    void headerChain() {
        HttpRequest r = Http.get("https://x.com")
                .header("Accept", "application/json")
                .header("X-Test", "yes");
        assertEquals(2, r.headers().size());
        assertEquals("application/json", r.headers().get("Accept"));
    }

    @Test
    void jsonSetsContentType() {
        HttpRequest r = Http.post("https://x.com").json("{}");
        assertEquals("application/json", r.headers().get("Content-Type"));
        assertEquals("{}", r.body());
    }

    @Test
    void bearerAuth() {
        HttpRequest r = Http.get("https://x.com").bearer("token123");
        assertEquals("Bearer token123", r.headers().get("Authorization"));
    }

    @Test
    void basicAuth() {
        HttpRequest r = Http.get("https://x.com").basicAuth("user", "pass");
        assertTrue(r.headers().get("Authorization").startsWith("Basic "));
    }

    @Test
    void timeoutChain() {
        HttpRequest r = Http.get("https://x.com").timeoutSeconds(5);
        assertEquals(Duration.ofSeconds(5), r.timeout());
    }

    @Test
    void timeoutClampedToPositive() {
        HttpRequest r = Http.get("https://x.com").timeoutSeconds(-3);
        assertEquals(Duration.ofSeconds(1), r.timeout());
    }

    @Test
    void negativeDurationIgnored() {
        HttpRequest r = Http.get("https://x.com").timeout(Duration.ofSeconds(-1));
        assertEquals(Duration.ofSeconds(30), r.timeout());
    }

    // ---------------------------------------------------- error cases (no network)

    @Test
    void nullUrlReturnsErrorResponse() {
        HttpResponse r = Http.get(null).send();
        assertTrue(r.hasTransportError());
    }

    @Test
    void blankUrlReturnsErrorResponse() {
        HttpResponse r = Http.get("   ").send();
        assertTrue(r.hasTransportError());
    }

    @Test
    void invalidUrlReturnsErrorResponse() {
        HttpResponse r = Http.get("not a url").send();
        assertTrue(r.hasTransportError());
    }

    @Test
    void sendNeverThrows() {
        assertDoesNotThrow(() -> Http.get("http://no-such-host-xyz-12345.invalid")
                .timeoutSeconds(2)
                .send());
    }

    // ---------------------------------------------------- response model

    @Test
    void statusTextKnownCodes() {
        assertEquals("OK", HttpRequest.statusText(200));
        assertEquals("Not Found", HttpRequest.statusText(404));
        assertEquals("Internal Server Error", HttpRequest.statusText(500));
    }

    @Test
    void errorResponseFactories() {
        HttpResponse r = HttpResponse.error(HttpMethod.GET, "url",
                Duration.ofMillis(10), "boom");
        assertTrue(r.hasTransportError());
        assertTrue(r.isError());
        assertFalse(r.isSuccess());
        assertEquals(-1, r.status());
        assertEquals("boom", r.statusText());
        assertEquals("", r.text());
    }

    @Test
    void responseIsSuccessFor2xx() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK",
                "hello", java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(r.isSuccess());
        assertFalse(r.isError());
        assertEquals("hello", r.text());
    }

    @Test
    void responseTextNeverNull() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK",
                null, java.util.Map.of(), Duration.ofMillis(5));
        assertEquals("", r.text());
    }

    @Test
    void responseJsonParsesValidBody() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK",
                "{\"a\":1}", java.util.Map.of(), Duration.ofMillis(5));
        assertEquals(1, r.asObject().getInt("a"));
    }

    @Test
    void responseJsonReturnsNullInstanceOnInvalidBody() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK",
                "not json", java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(r.asJson().isNull());
        assertTrue(r.asObject().isEmpty());
    }

    @Test
    void responseHeaderLookupIsCaseInsensitive() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK", "",
                java.util.Map.of("Content-Type", java.util.List.of("application/json")),
                Duration.ofMillis(5));
        assertEquals("application/json", r.header("content-type"));
        assertEquals("application/json", r.header("CONTENT-TYPE"));
    }

    @Test
    void responseHeaderAllReturnsList() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK", "",
                java.util.Map.of("Set-Cookie", java.util.List.of("a=1", "b=2")),
                Duration.ofMillis(5));
        assertEquals(2, r.headerAll("set-cookie").size());
    }

    @Test
    void responseSummaryIncludesStatusAndUrl() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "url", 200, "OK", "",
                java.util.Map.of(), Duration.ofMillis(42));
        String s = r.summary();
        assertTrue(s.contains("GET"));
        assertTrue(s.contains("200"));
        assertTrue(s.contains("42ms"));
    }

    // ---------------------------------------------------- connectivity (offline-safe)

    @Test
    void canReachRejectsNulls() {
        assertFalse(Http.canReach(null, 80));
        assertFalse(Http.canReach("example.com", -1));
    }

    @Test
    void canReachReturnsFalseQuicklyForBadHost() {
        long start = System.nanoTime();
        boolean result = Http.canReach("no-such-host-xyz-12345.invalid", 80, 500);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertFalse(result);
        assertTrue(elapsedMs < 5_000, "Should return within 5s, took " + elapsedMs + "ms");
    }
}
