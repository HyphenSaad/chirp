package chirp.http;

import chirp.internal.Trace;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;

/**
 * HTTP facade — request factories, connectivity, and configuration.
 *
 * <h2>Two kinds of GET helpers</h2>
 * <ul>
 *   <li><b>Fluent</b> — {@code get}, {@code getRequest}, {@code request},
 *       {@code builder} — return a {@link HttpRequest} to chain before sending.</li>
 *   <li><b>Immediate</b> — {@code fetch}, {@code fetchJson}, {@code getJson}
 *       — send straight away and return a {@link HttpResponse}.</li>
 * </ul>
 *
 * <pre>{@code
 * // Fluent — build a request, then send
 * HttpResponse r1 = Http.get("https://api.example.com")
 *     .header("Accept", "application/json")
 *     .send();
 *
 * // Immediate — one call
 * HttpResponse r2 = Http.fetch("https://api.example.com");
 *
 * // Both produce the same result.
 * }</pre>
 */
public final class Http {

    /** Keys for the key-value overloads. */
    public enum Key {
        TIMEOUT, CACHE_TTL, RETRIES, URL, METHOD
    }

    private static volatile Duration defaultTimeout = Duration.ofSeconds(30);
    private static volatile HttpListener defaultListener;

    // Simple volatile cache for isOnline
    private static volatile long lastOnlineCheck = 0;
    private static volatile boolean lastOnlineResult = false;

    private Http() {}

    // ============================================================== fluent request factories

    public static HttpRequest get(String url) { return new HttpRequest(HttpMethod.GET, url); }
    public static HttpRequest getRequest(String u) { return get(u); }
    public static HttpRequest builder(String u) { return get(u); }
    public static HttpRequest newRequest(String u) { return get(u); }

    public static HttpRequest post(String url) { return new HttpRequest(HttpMethod.POST, url); }
    public static HttpRequest submit(String url) { return post(url); }

    public static HttpRequest put(String url) { return new HttpRequest(HttpMethod.PUT, url); }
    public static HttpRequest update(String url) { return put(url); }

    public static HttpRequest delete(String url) { return new HttpRequest(HttpMethod.DELETE, url); }
    public static HttpRequest del(String url) { return delete(url); }
    public static HttpRequest remove(String url) { return delete(url); }

    public static HttpRequest patch(String url) { return new HttpRequest(HttpMethod.PATCH, url); }
    public static HttpRequest head(String url) { return new HttpRequest(HttpMethod.HEAD, url); }
    public static HttpRequest options(String url) { return new HttpRequest(HttpMethod.OPTIONS, url); }

    public static HttpRequest request(HttpMethod method, String url) {
        return new HttpRequest(method, url);
    }
    public static HttpRequest make(HttpMethod method, String url) { return request(method, url); }

    public static HttpRequest from(Object... keyValues) {
        return HttpRequest.from(keyValues);
    }

    // ============================================================== immediate GET

    /** One-shot GET — sends immediately and returns the response. */
    public static HttpResponse fetch(String url) {
        return get(url).send();
    }

    /** Alias of {@link #fetch(String)} — expresses intent that the body is JSON. */
    public static HttpResponse fetchJson(String url) {
        return fetch(url);
    }
    public static HttpResponse getJson(String url) { return fetch(url); }

    // ============================================================== immediate POST

    public static HttpResponse postJson(String url, String jsonBody) {
        return post(url).json(jsonBody).send();
    }
    public static HttpResponse submitJson(String url, String body) { return postJson(url, body); }

    // ============================================================== download

    public static HttpResponse download(String url, String savePath) {
        HttpResponse r = get(url).send();
        if (r.isSuccess() && r.hasBody() && savePath != null) {
            chirp.io.TextFileIO.write(savePath, r.text());
        }
        return r;
    }
    public static HttpResponse downloadTo(String url, String savePath) { return download(url, savePath); }
    public static HttpResponse fetchFile(String url, String savePath) { return download(url, savePath); }

    // ============================================================== connectivity

    public static boolean isOnline() { return checkOnline(1000, 0); }
    public static boolean hasInternet() { return isOnline(); }
    public static boolean isConnected() { return isOnline(); }
    public static boolean online() { return isOnline(); }
    public static boolean connected() { return isOnline(); }

    public static boolean isOnline(long cacheTtlMs) { return checkOnline(1000, cacheTtlMs); }

    public static boolean isOnline(Object... keyValues) {
        int timeout = 1000;
        long cache = 0;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                if (v instanceof Integer n) {
                    if (k == Key.TIMEOUT) timeout = Math.max(100, n);
                    else if (k == Key.CACHE_TTL) cache = Math.max(0, n);
                }
            }
        }
        return checkOnline(timeout, cache);
    }

    private static boolean checkOnline(int timeout, long cache) {
        long now = System.currentTimeMillis();
        if (cache > 0 && now - lastOnlineCheck < cache) return lastOnlineResult;
        boolean result = canReach("1.1.1.1", 53, timeout)
                || canReach("8.8.8.8", 53, timeout)
                || canReach("208.67.222.222", 53, timeout);
        lastOnlineResult = result;
        lastOnlineCheck = now;
        return result;
    }

    public static boolean canReach(String host, int port) { return canReach(host, port, 2000); }
    public static boolean isReachable(String host, int port) { return canReach(host, port); }
    public static boolean reachable(String host, int port) { return canReach(host, port); }
    public static boolean pingHost(String host, int port) { return canReach(host, port); }

    public static boolean canReach(String host, int port, int timeoutMs) {
        if (host == null || port < 0) return false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.max(100, timeoutMs));
            return true;
        } catch (Exception e) {
            Trace.debug("http", "canReach({}:{}) failed: {}", host, port, e.getMessage());
            return false;
        }
    }

    public static boolean ping(String url) { return pingUrl(url); }
    public static boolean isUp(String url) { return pingUrl(url); }
    public static boolean responds(String url) { return pingUrl(url); }

    private static boolean pingUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = new URI(url);
            int port = uri.getPort() > 0
                    ? uri.getPort()
                    : "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            return canReach(uri.getHost(), port);
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================== configuration

    public static Duration getDefaultTimeout() { return defaultTimeout; }
    public static void setDefaultTimeout(Duration d) { if (d != null) defaultTimeout = d; }
    public static void defaultTimeoutSeconds(int s) { defaultTimeout = Duration.ofSeconds(Math.max(1, s)); }

    public static HttpListener getDefaultListener() { return defaultListener; }
    public static void setDefaultListener(HttpListener l) { defaultListener = l; }

    public static void clearCache() {
        lastOnlineCheck = 0;
        lastOnlineResult = false;
    }

    public static void reset() {
        HttpRequest.resetClient();
        defaultTimeout = Duration.ofSeconds(30);
        defaultListener = null;
        clearCache();
    }
}
