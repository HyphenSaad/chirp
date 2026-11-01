package chirp.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpEnhancedTest {

    @BeforeEach
    void setup() { Http.reset(); }
    @AfterEach
    void teardown() { Http.reset(); }

    // ============================================================== method aliases

    @Test
    void parseAliases() {
        assertEquals(HttpMethod.GET, HttpMethod.parse("get"));
        assertEquals(HttpMethod.POST, HttpMethod.from("post"));
        assertEquals(HttpMethod.PUT, HttpMethod.of("put"));
        assertEquals(HttpMethod.GET, HttpMethod.valueOfSafe("unknown"));
    }

    @Test
    void methodPredicates() {
        assertTrue(HttpMethod.GET.isSafe());
        assertTrue(HttpMethod.HEAD.isSafe());
        assertFalse(HttpMethod.POST.isSafe());

        assertTrue(HttpMethod.POST.hasBody());
        assertTrue(HttpMethod.PUT.hasBody());
        assertFalse(HttpMethod.GET.hasBody());

        assertTrue(HttpMethod.GET.returnsBody());
        assertFalse(HttpMethod.HEAD.returnsBody());
    }

    // ============================================================== fluent factories

    @Test
    void fluentFactories() {
        assertEquals(HttpMethod.GET, Http.get("u").method());
        assertEquals(HttpMethod.GET, Http.getRequest("u").method());
        assertEquals(HttpMethod.GET, Http.builder("u").method());
        assertEquals(HttpMethod.GET, Http.newRequest("u").method());
        assertEquals(HttpMethod.POST, Http.post("u").method());
        assertEquals(HttpMethod.POST, Http.submit("u").method());
        assertEquals(HttpMethod.PUT, Http.put("u").method());
        assertEquals(HttpMethod.PUT, Http.update("u").method());
        assertEquals(HttpMethod.DELETE, Http.delete("u").method());
        assertEquals(HttpMethod.DELETE, Http.del("u").method());
        assertEquals(HttpMethod.DELETE, Http.remove("u").method());
        assertEquals(HttpMethod.PATCH, Http.patch("u").method());
        assertEquals(HttpMethod.HEAD, Http.head("u").method());
        assertEquals(HttpMethod.OPTIONS, Http.options("u").method());
    }

    @Test
    void keyValueFactory() {
        HttpRequest r = Http.from(
                Http.Key.METHOD, HttpMethod.POST,
                Http.Key.URL, "https://api.example.com",
                Http.Key.TIMEOUT, 10);
        assertEquals(HttpMethod.POST, r.method());
        assertEquals("https://api.example.com", r.url());
        assertEquals(Duration.ofSeconds(10), r.timeout());
    }

    @Test
    void keyValueFactoryNullsSafe() {
        assertDoesNotThrow(() -> Http.from((Object[]) null));
        assertDoesNotThrow(() -> Http.from(Http.Key.URL, null));
        assertDoesNotThrow(() -> Http.from(Http.Key.METHOD, "not a method"));
    }

    // ============================================================== immediate GET

    @Test
    void fetchReturnsResponse() {
        // Null URL always fails → error response (not exception)
        HttpResponse r = Http.fetch(null);
        assertTrue(r.hasTransportError());
        assertTrue(r.isError());
    }

    @Test
    void fetchAliasesReturnResponse() {
        assertTrue(Http.fetch(null).hasTransportError());
        assertTrue(Http.fetchJson(null).hasTransportError());
        assertTrue(Http.getJson(null).hasTransportError());
    }

    // ============================================================== request chaining

    @Test
    void headerAliases() {
        HttpRequest r = Http.get("u")
                .header("A", "1")
                .withHeader("B", "2")
                .addHeader("C", "3");
        assertEquals(3, r.headers().size());
    }

    @Test
    void bodyAliases() {
        assertEquals("{}", Http.post("u").body("{}").body());
        assertEquals("{}", Http.post("u").withBody("{}").body());
        assertEquals("{}", Http.post("u").setBody("{}").body());
        assertEquals("{}", Http.post("u").content("{}").body());
    }

    @Test
    void jsonSetsContentType() {
        HttpRequest r = Http.post("u").json("{}");
        assertEquals("application/json", r.headers().get("Content-Type"));
        assertEquals("{}", r.body());
    }

    @Test
    void bearerAliases() {
        assertEquals("Bearer tok", Http.get("u").bearer("tok").headers().get("Authorization"));
        assertEquals("Bearer tok", Http.get("u").auth("tok").headers().get("Authorization"));
        assertEquals("Bearer tok", Http.get("u").withBearer("tok").headers().get("Authorization"));
        assertEquals("Bearer tok", Http.get("u").token("tok").headers().get("Authorization"));
    }

    @Test
    void basicAuthAliases() {
        assertTrue(Http.get("u").basicAuth("user", "pass")
                .headers().get("Authorization").startsWith("Basic "));
        assertTrue(Http.get("u").authBasic("user", "pass")
                .headers().get("Authorization").startsWith("Basic "));
    }

    @Test
    void timeoutAliases() {
        assertEquals(Duration.ofSeconds(5), Http.get("u").timeoutSeconds(5).timeout());
        assertEquals(Duration.ofMillis(500), Http.get("u").timeoutMillis(500).timeout());
    }

    // ============================================================== send aliases

    @Test
    void nullUrlSafe() {
        assertTrue(Http.get(null).send().hasTransportError());
        assertTrue(Http.get(null).execute().hasTransportError());
        assertTrue(Http.get(null).call().hasTransportError());
        assertTrue(Http.get(null).fire().hasTransportError());
        assertTrue(Http.get(null).make().hasTransportError());
    }

    @Test
    void sendKeyValue() {
        assertTrue(Http.get(null).send(Http.Key.TIMEOUT, 5).hasTransportError());
    }

    @Test
    void sendWithRetry() {
        assertThrows(RuntimeException.class,
                () -> Http.get(null).sendWithRetry(2));
    }

    @Test
    void sendWithRetryAlias() {
        assertThrows(RuntimeException.class,
                () -> Http.get(null).execWithRetry(2));
    }

    // ============================================================== response aliases

    @Test
    void responseBodyAliases() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "u", 200, "OK",
                "hello", java.util.Map.of(), Duration.ofMillis(5));
        assertEquals("hello", r.text());
        assertEquals("hello", r.body());
        assertEquals("hello", r.raw());
        assertEquals("hello", r.content());
        assertEquals("hello", r.data());
    }

    @Test
    void responseStatusAliases() {
        HttpResponse ok = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "",
                java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(ok.isSuccess());
        assertTrue(ok.isOk());
        assertTrue(ok.is2xx());
        assertTrue(ok.succeeded());
        assertTrue(ok.ok());
        assertTrue(ok.good());

        HttpResponse err = new HttpResponse(HttpMethod.GET, "u", 404, "Not Found", "",
                java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(err.isError());
        assertTrue(err.isFailed());
        assertTrue(err.is4xx());
        assertTrue(err.isClientError());

        HttpResponse server = new HttpResponse(HttpMethod.GET, "u", 500, "Server Error", "",
                java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(server.isServerError());
        assertTrue(server.is5xx());

        HttpResponse redirect = new HttpResponse(HttpMethod.GET, "u", 302, "Found", "",
                java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(redirect.isRedirect());
        assertTrue(redirect.is3xx());
    }

    @Test
    void responseTimingAliases() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "",
                java.util.Map.of(), Duration.ofMillis(42));
        assertEquals(42, r.durationMillis());
        assertEquals(42, r.elapsed());
        assertEquals(42, r.took());
        assertEquals(42, r.timeMs());
    }

    @Test
    void responseJsonAliases() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "u", 200, "OK",
                "{\"a\":1}", java.util.Map.of(), Duration.ofMillis(5));
        assertEquals(1, r.json().asObject().getInt("a"));
        assertEquals(1, r.asJson().asObject().getInt("a"));
        assertEquals(1, r.object().getInt("a"));
        assertEquals(1, r.asObject().getInt("a"));
        assertEquals(1, r.toObject().getInt("a"));
        assertEquals(1, r.toJsonObject().getInt("a"));
    }

    @Test
    void responseHeaderAliases() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "",
                java.util.Map.of("Content-Type", java.util.List.of("application/json")),
                Duration.ofMillis(5));
        assertEquals("application/json", r.header("content-type"));
        assertEquals("application/json", r.headerFirst("Content-Type"));
        assertEquals("application/json", r.firstHeader("CONTENT-TYPE"));
        assertEquals("application/json", r.contentType());
        assertEquals(1, r.headerAll("content-type").size());
        assertEquals(1, r.headers("Content-Type").size());
    }

    @Test
    void responseDisplay() {
        HttpResponse r = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "",
                java.util.Map.of(), Duration.ofMillis(42));
        assertTrue(r.summary().contains("200"));
        assertEquals(r.summary(), r.describe());
        assertEquals(r.summary(), r.info());
    }

    @Test
    void responseHasBody() {
        HttpResponse empty = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "",
                java.util.Map.of(), Duration.ofMillis(5));
        assertFalse(empty.hasBody());
        assertEquals(0, empty.length());

        HttpResponse full = new HttpResponse(HttpMethod.GET, "u", 200, "OK", "hi",
                java.util.Map.of(), Duration.ofMillis(5));
        assertTrue(full.hasBody());
        assertEquals(2, full.length());
        assertEquals(2, full.byteLength());
    }

    // ============================================================== http exception

    @Test
    void httpExceptionAliases() {
        HttpException e = new HttpException("msg", 404, "not found");
        assertEquals(404, e.status());
        assertEquals(404, e.statusCode());
        assertEquals(404, e.code());
        assertEquals("not found", e.body());
        assertEquals("not found", e.response());
        assertEquals("not found", e.raw());
        assertTrue(e.isStatusError());
        assertFalse(e.isTransportError());
    }

    @Test
    void httpExceptionTransport() {
        HttpException e = new HttpException("msg", "body");
        assertEquals(-1, e.status());
        assertTrue(e.isTransportError());
        assertFalse(e.isStatusError());
    }

    // ============================================================== connectivity

    @Test
    void canReachNulls() {
        assertFalse(Http.canReach(null, 80));
        assertFalse(Http.canReach("example.com", -1));
    }

    @Test
    void canReachQuickFailOnBadHost() {
        long start = System.nanoTime();
        boolean r = Http.canReach("no-such-host-xyz-12345.invalid", 80, 500);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertFalse(r);
        assertTrue(elapsedMs < 5000);
    }

    @Test
    void isOnlineKeyValue() {
        assertDoesNotThrow(() -> Http.isOnline(Http.Key.TIMEOUT, 200));
        assertDoesNotThrow(() -> Http.isOnline(Http.Key.TIMEOUT, 200, Http.Key.CACHE_TTL, 500));
    }

    @Test
    void isOnlineNullSafe() {
        assertDoesNotThrow(() -> Http.isOnline((Object[]) null));
        assertDoesNotThrow(() -> Http.isOnline(Http.Key.TIMEOUT, null));
    }

    @Test
    void cacheCanBeCleared() {
        assertDoesNotThrow(Http::clearCache);
    }

    @Test
    void reachabilityAliases() {
        assertFalse(Http.canReach("no-such-host-xyz.invalid", 80, 200));
        assertFalse(Http.isReachable("no-such-host-xyz.invalid", 80));
        assertFalse(Http.reachable("no-such-host-xyz.invalid", 80));
        assertFalse(Http.pingHost("no-such-host-xyz.invalid", 80));
    }

    @Test
    void pingNulls() {
        assertFalse(Http.ping(null));
        assertFalse(Http.isUp(null));
        assertFalse(Http.responds(null));
    }

    // ============================================================== download

    @Test
    void downloadWithBadUrlReturnsError() {
        assertTrue(Http.download(null, "x.txt").hasTransportError());
        assertTrue(Http.downloadTo(null, "x.txt").hasTransportError());
        assertTrue(Http.fetchFile(null, "x.txt").hasTransportError());
    }

    // ============================================================== immediate POST

    @Test
    void postJsonAliases() {
        assertTrue(Http.postJson("http://no-such-host-xyz.invalid", "{}").hasTransportError());
        assertTrue(Http.submitJson("http://no-such-host-xyz.invalid", "{}").hasTransportError());
    }
}
