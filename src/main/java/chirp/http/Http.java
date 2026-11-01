package chirp.http;

import chirp.internal.Trace;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;

/**
 * Static facade for the HTTP module — factory methods for request
 * builders, immediate one-call helpers, connectivity tests, and shared
 * configuration.
 *
 * <p>Two ways to make a request:</p>
 * <ul>
 *   <li><b>Fluent</b> — {@code Http.get(url)}, {@code Http.post(url)}
 *       and friends return a {@link HttpRequest} that you configure
 *       (headers, body, timeout) and then call {@code .send()} on.</li>
 *   <li><b>Immediate</b> — {@code Http.fetch(url)},
 *       {@code Http.fetchJson(url)}, {@code Http.getJson(url)},
 *       {@code Http.postJson(url, body)} send straight away and return
 *       an {@link HttpResponse} in one call.</li>
 * </ul>
 *
 * <h2>Example — the two forms are equivalent</h2>
 * <pre>{@code
 * // Fluent — configure then send
 * HttpResponse r1 = Http.get("https://api.example.com/user")
 *     .header("Accept", "application/json")
 *     .send();
 *
 * // Immediate — same result in one call
 * HttpResponse r2 = Http.fetch("https://api.example.com/user");
 * }</pre>
 *
 * <h2>Connectivity testing</h2>
 * <p>{@link #isOnline()} opens a socket to three well-known public DNS
 * servers (1.1.1.1, 8.8.8.8, 208.67.222.222) on port 53 and returns
 * {@code true} if any of them responds within 1 second. The result is
 * cached for one call — use {@link #isOnline(long)} with a cache TTL for
 * repeated checks.</p>
 *
 * <p>{@link #canReach(String, int)} tests a specific host:port pair.
 * {@link #ping(String)} is a URL-aware convenience — parses the URL,
 * derives the port (80, 443, or explicit), and calls
 * {@code canReach}.</p>
 *
 * <h2>Shared configuration</h2>
 * <ul>
 *   <li>{@link #setDefaultTimeout(Duration)} — the timeout used when a
 *       request does not set one explicitly (default 30s)</li>
 *   <li>{@link #setDefaultListener(HttpListener)} — the listener used
 *       when a request does not set one explicitly</li>
 *   <li>{@link #reset()} — clears all shared state. Useful for tests
 *       that need a clean slate.</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For the request builder, see {@link HttpRequest}. For the response
 * record, see {@link HttpResponse}. For status classification helpers,
 * see {@link HttpResponse#isSuccess()} and friends.</p>
 *
 * @see HttpRequest
 * @see HttpResponse
 * @see HttpMethod
 * @see HttpException
 * @since 1.0.0
 */
public final class Http {

    /**
     * Key enum for the key-value {@link #isOnline(Object...)} overload.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Timeout in milliseconds for the connectivity test — {@link Integer}. */
        TIMEOUT,
        /** Cache TTL in milliseconds — {@link Integer}. Zero means no caching. */
        CACHE_TTL,
        /** Reserved for future use. */
        RETRIES,
        /** The URL of the request — {@link String}. */
        URL,
        /** The HTTP method — {@link HttpMethod}. */
        METHOD
    }

    private static volatile Duration defaultTimeout = Duration.ofSeconds(30);
    private static volatile HttpListener defaultListener;

    // Simple volatile cache for isOnline
    private static volatile long lastOnlineCheck = 0;
    private static volatile boolean lastOnlineResult = false;

    private Http() {}

    // ============================================================== fluent request factories

    /**
     * Opens a fluent {@link HttpRequest} for a GET.
     *
     * @param url the target URL (may be null — request will fail on send)
     * @return a new {@code HttpRequest} configured for GET
     * @see #getRequest(String)
     * @see #builder(String)
     * @see #newRequest(String)
     * @see #post(String)
     * @see #fetch(String)
     * @since 1.0.0
     */
    public static HttpRequest get(String url) { return new HttpRequest(HttpMethod.GET, url); }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation and examples.
     *
     * @param u the target URL
     * @return a new GET request
     * @see #get(String)
     * @since 1.0.0
     */
    public static HttpRequest getRequest(String u) { return get(u); }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation and examples.
     *
     * @param u the target URL
     * @return a new GET request
     * @see #get(String)
     * @since 1.0.0
     */
    public static HttpRequest builder(String u) { return get(u); }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation and examples.
     *
     * @param u the target URL
     * @return a new GET request
     * @see #get(String)
     * @since 1.0.0
     */
    public static HttpRequest newRequest(String u) { return get(u); }

    /**
     * Opens a fluent {@link HttpRequest} for a POST.
     *
     * @param url the target URL
     * @return a new POST request
     * @see #submit(String)
     * @see #postJson(String, String)
     * @since 1.0.0
     */
    public static HttpRequest post(String url) { return new HttpRequest(HttpMethod.POST, url); }

    /**
     * Alias of {@link #post(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return a new POST request
     * @see #post(String)
     * @since 1.0.0
     */
    public static HttpRequest submit(String url) { return post(url); }

    /**
     * Opens a fluent {@link HttpRequest} for a PUT.
     *
     * @param url the target URL
     * @return a new PUT request
     * @see #update(String)
     * @since 1.0.0
     */
    public static HttpRequest put(String url) { return new HttpRequest(HttpMethod.PUT, url); }

    /**
     * Alias of {@link #put(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return a new PUT request
     * @see #put(String)
     * @since 1.0.0
     */
    public static HttpRequest update(String url) { return put(url); }

    /**
     * Opens a fluent {@link HttpRequest} for a DELETE.
     *
     * @param url the target URL
     * @return a new DELETE request
     * @see #del(String)
     * @see #remove(String)
     * @since 1.0.0
     */
    public static HttpRequest delete(String url) { return new HttpRequest(HttpMethod.DELETE, url); }

    /**
     * Alias of {@link #delete(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return a new DELETE request
     * @see #delete(String)
     * @since 1.0.0
     */
    public static HttpRequest del(String url) { return delete(url); }

    /**
     * Alias of {@link #delete(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return a new DELETE request
     * @see #delete(String)
     * @since 1.0.0
     */
    public static HttpRequest remove(String url) { return delete(url); }

    /**
     * Opens a fluent {@link HttpRequest} for a PATCH.
     *
     * @param url the target URL
     * @return a new PATCH request
     * @since 1.0.0
     */
    public static HttpRequest patch(String url) { return new HttpRequest(HttpMethod.PATCH, url); }

    /**
     * Opens a fluent {@link HttpRequest} for a HEAD.
     *
     * @param url the target URL
     * @return a new HEAD request
     * @since 1.0.0
     */
    public static HttpRequest head(String url) { return new HttpRequest(HttpMethod.HEAD, url); }

    /**
     * Opens a fluent {@link HttpRequest} for an OPTIONS.
     *
     * @param url the target URL
     * @return a new OPTIONS request
     * @since 1.0.0
     */
    public static HttpRequest options(String url) { return new HttpRequest(HttpMethod.OPTIONS, url); }

    /**
     * Opens a fluent {@link HttpRequest} for an arbitrary method.
     *
     * @param method the HTTP method (null → GET)
     * @param url the target URL
     * @return a new request
     * @see #make(HttpMethod, String)
     * @since 1.0.0
     */
    public static HttpRequest request(HttpMethod method, String url) {
        return new HttpRequest(method, url);
    }

    /**
     * Alias of {@link #request(HttpMethod, String)} — see that method for
     * full documentation.
     *
     * @param method the HTTP method
     * @param url the target URL
     * @return a new request
     * @see #request(HttpMethod, String)
     * @since 1.0.0
     */
    public static HttpRequest make(HttpMethod method, String url) { return request(method, url); }

    /**
     * Constructs a request from key-value pairs.
     *
     * <pre>{@code
     * HttpRequest r = Http.from(
     *     Http.Key.METHOD, HttpMethod.POST,
     *     Http.Key.URL, "https://api.example.com/users",
     *     Http.Key.TIMEOUT, 10);
     * }</pre>
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return a new request
     * @see HttpRequest#from(Object...)
     * @since 1.0.0
     */
    public static HttpRequest from(Object... keyValues) {
        return HttpRequest.from(keyValues);
    }

    // ============================================================== immediate GET

    /**
     * Sends a GET immediately and returns the response.
     *
     * <p>Equivalent to {@code Http.get(url).send()} but reads as a single
     * action. Returns an error response rather than throwing for
     * transport failures — see {@link HttpResponse#hasTransportError()}.</p>
     *
     * @param url the target URL
     * @return the response (never null)
     * @see #fetchJson(String)
     * @see #getJson(String)
     * @see HttpRequest#send()
     * @since 1.0.0
     */
    public static HttpResponse fetch(String url) {
        return get(url).send();
    }

    /**
     * Alias of {@link #fetch(String)} — see that method for full
     * documentation. The name expresses intent that the response body is
     * JSON, but the fetch behaviour is identical.
     *
     * @param url the target URL
     * @return the response
     * @see #fetch(String)
     * @since 1.0.0
     */
    public static HttpResponse fetchJson(String url) {
        return fetch(url);
    }

    /**
     * Alias of {@link #fetch(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return the response
     * @see #fetch(String)
     * @since 1.0.0
     */
    public static HttpResponse getJson(String url) { return fetch(url); }

    // ============================================================== immediate POST

    /**
     * Sends a POST with a JSON body immediately and returns the response.
     *
     * @param url the target URL
     * @param jsonBody the JSON body to send
     * @return the response
     * @see #submitJson(String, String)
     * @see HttpRequest#json(String)
     * @since 1.0.0
     */
    public static HttpResponse postJson(String url, String jsonBody) {
        return post(url).json(jsonBody).send();
    }

    /**
     * Alias of {@link #postJson(String, String)} — see that method for
     * full documentation.
     *
     * @param url the target URL
     * @param body the JSON body
     * @return the response
     * @see #postJson(String, String)
     * @since 1.0.0
     */
    public static HttpResponse submitJson(String url, String body) { return postJson(url, body); }

    // ============================================================== download

    /**
     * Downloads a URL and saves the response body to the given path.
     *
     * <p>Fetches the URL, and if the response is a success with a
     * non-empty body, writes it to {@code savePath}. Returns the response
     * regardless — the caller can check {@code response.isSuccess()} to
     * see whether the write happened.</p>
     *
     * @param url the target URL
     * @param savePath the file path to write to (may be null — no write)
     * @return the response
     * @see #downloadTo(String, String)
     * @see #fetchFile(String, String)
     * @since 1.0.0
     */
    public static HttpResponse download(String url, String savePath) {
        HttpResponse r = get(url).send();
        if (r.isSuccess() && r.hasBody() && savePath != null) {
            chirp.io.TextFileIO.write(savePath, r.text());
        }
        return r;
    }

    /**
     * Alias of {@link #download(String, String)} — see that method for
     * full documentation.
     *
     * @param url the target URL
     * @param savePath the save path
     * @return the response
     * @see #download(String, String)
     * @since 1.0.0
     */
    public static HttpResponse downloadTo(String url, String savePath) { return download(url, savePath); }

    /**
     * Alias of {@link #download(String, String)} — see that method for
     * full documentation.
     *
     * @param url the target URL
     * @param savePath the save path
     * @return the response
     * @see #download(String, String)
     * @since 1.0.0
     */
    public static HttpResponse fetchFile(String url, String savePath) { return download(url, savePath); }

    // ============================================================== connectivity

    /**
     * Checks whether the internet appears to be reachable, by opening a
     * socket to one of three public DNS servers (1.1.1.1, 8.8.8.8,
     * 208.67.222.222) on port 53 with a 1-second timeout.
     *
     * <p>The result is <b>not</b> cached by default — every call makes
     * the socket attempt. For repeated checks, use
     * {@link #isOnline(long)} with a cache TTL.</p>
     *
     * @return {@code true} if any of the DNS servers responds
     * @see #isOnline(long)
     * @see #isOnline(Object...)
     * @see #hasInternet()
     * @see #canReach(String, int)
     * @since 1.0.0
     */
    public static boolean isOnline() { return checkOnline(1000, 0); }

    /**
     * Alias of {@link #isOnline()} — see that method for full
     * documentation.
     *
     * @return {@code true} if online
     * @see #isOnline()
     * @since 1.0.0
     */
    public static boolean hasInternet() { return isOnline(); }

    /**
     * Alias of {@link #isOnline()} — see that method for full
     * documentation.
     *
     * @return {@code true} if online
     * @see #isOnline()
     * @since 1.0.0
     */
    public static boolean isConnected() { return isOnline(); }

    /**
     * Alias of {@link #isOnline()} — see that method for full
     * documentation.
     *
     * @return {@code true} if online
     * @see #isOnline()
     * @since 1.0.0
     */
    public static boolean online() { return isOnline(); }

    /**
     * Alias of {@link #isOnline()} — see that method for full
     * documentation.
     *
     * @return {@code true} if online
     * @see #isOnline()
     * @since 1.0.0
     */
    public static boolean connected() { return isOnline(); }

    /**
     * Checks whether the internet is reachable, caching the result for
     * the given duration.
     *
     * <pre>{@code
     * // Cache for 30 seconds — repeated calls in a loop hit the cache
     * if (Http.isOnline(30_000)) { /* ... *\/ }
     * }</pre>
     *
     * @param cacheTtlMs the cache time-to-live in milliseconds (0 → no caching)
     * @return {@code true} if online
     * @see #isOnline()
     * @since 1.0.0
     */
    public static boolean isOnline(long cacheTtlMs) { return checkOnline(1000, cacheTtlMs); }

    /**
     * Checks whether the internet is reachable, with key-value options
     * for timeout and caching.
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return {@code true} if online
     * @see #isOnline()
     * @see #isOnline(long)
     * @since 1.0.0
     */
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

    /**
     * Checks whether the given host:port pair is reachable, using the
     * default 2-second timeout.
     *
     * @param host the host name (may be null → false)
     * @param port the port number
     * @return {@code true} if the socket connects successfully
     * @see #canReach(String, int, int)
     * @since 1.0.0
     */
    public static boolean canReach(String host, int port) { return canReach(host, port, 2000); }

    /**
     * Alias of {@link #canReach(String, int)} — see that method for full
     * documentation.
     *
     * @param host the host name
     * @param port the port
     * @return {@code true} if reachable
     * @see #canReach(String, int)
     * @since 1.0.0
     */
    public static boolean isReachable(String host, int port) { return canReach(host, port); }

    /**
     * Alias of {@link #canReach(String, int)} — see that method for full
     * documentation.
     *
     * @param host the host name
     * @param port the port
     * @return {@code true} if reachable
     * @see #canReach(String, int)
     * @since 1.0.0
     */
    public static boolean reachable(String host, int port) { return canReach(host, port); }

    /**
     * Alias of {@link #canReach(String, int)} — see that method for full
     * documentation.
     *
     * @param host the host name
     * @param port the port
     * @return {@code true} if reachable
     * @see #canReach(String, int)
     * @since 1.0.0
     */
    public static boolean pingHost(String host, int port) { return canReach(host, port); }

    /**
     * Checks whether the given host:port pair is reachable within the
     * specified timeout.
     *
     * @param host the host name (may be null → false)
     * @param port the port number (negative → false)
     * @param timeoutMs the connect timeout in milliseconds
     * @return {@code true} if the socket connects successfully
     * @see #canReach(String, int)
     * @since 1.0.0
     */
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

    /**
     * Checks whether the given URL is reachable, using the URL's host and
     * derived port (443 for HTTPS, 80 for HTTP, or the explicit port if
     * one is present).
     *
     * @param url the URL (may be null → false)
     * @return {@code true} if the URL is reachable
     * @see #isUp(String)
     * @see #responds(String)
     * @since 1.0.0
     */
    public static boolean ping(String url) { return pingUrl(url); }

    /**
     * Alias of {@link #ping(String)} — see that method for full
     * documentation.
     *
     * @param url the URL
     * @return {@code true} if reachable
     * @see #ping(String)
     * @since 1.0.0
     */
    public static boolean isUp(String url) { return pingUrl(url); }

    /**
     * Alias of {@link #ping(String)} — see that method for full
     * documentation.
     *
     * @param url the URL
     * @return {@code true} if reachable
     * @see #ping(String)
     * @since 1.0.0
     */
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

    /**
     * Returns the shared default timeout.
     *
     * @return the timeout (never null)
     * @see #setDefaultTimeout(Duration)
     * @since 1.0.0
     */
    public static Duration getDefaultTimeout() { return defaultTimeout; }

    /**
     * Sets the shared default timeout — the timeout used by requests
     * that don't set one explicitly.
     *
     * @param d the timeout (null → no change)
     * @see #defaultTimeoutSeconds(int)
     * @since 1.0.0
     */
    public static void setDefaultTimeout(Duration d) { if (d != null) defaultTimeout = d; }

    /**
     * Sets the shared default timeout in seconds.
     *
     * @param s the timeout in seconds (clamped to ≥ 1)
     * @see #setDefaultTimeout(Duration)
     * @since 1.0.0
     */
    public static void defaultTimeoutSeconds(int s) { defaultTimeout = Duration.ofSeconds(Math.max(1, s)); }

    /**
     * Returns the shared default listener, or null if none is set.
     *
     * @return the default listener, or null
     * @see #setDefaultListener(HttpListener)
     * @since 1.0.0
     */
    public static HttpListener getDefaultListener() { return defaultListener; }

    /**
     * Sets the shared default listener — the listener used by requests
     * that don't set one explicitly.
     *
     * @param l the listener (may be null to clear)
     * @since 1.0.0
     */
    public static void setDefaultListener(HttpListener l) { defaultListener = l; }

    /**
     * Clears the {@code isOnline} cache.
     *
     * @see #isOnline(long)
     * @since 1.0.0
     */
    public static void clearCache() {
        lastOnlineCheck = 0;
        lastOnlineResult = false;
    }

    /**
     * Resets all shared state — clears the shared HTTP client, restores
     * the default timeout to 30 seconds, clears the default listener,
     * and clears the connectivity cache. Useful for test isolation.
     *
     * @see #clearCache()
     * @since 1.0.0
     */
    public static void reset() {
        HttpRequest.resetClient();
        defaultTimeout = Duration.ofSeconds(30);
        defaultListener = null;
        clearCache();
    }
}
