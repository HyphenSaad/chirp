package chirp.http;

import chirp.decorator.Retry;
import chirp.internal.Trace;
import chirp.io.TextFileIO;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent HTTP request builder that also sends itself.
 *
 * <p>Every configuration method returns {@code this}, so a request is
 * configured by chaining, then dispatched with {@code send()} or one of
 * its variants. A request is <b>mutable</b> — reusing the same builder
 * for two sends reuses the accumulated headers, body, and options.</p>
 *
 * <h2>Example — the shape of a request</h2>
 * <pre>{@code
 * HttpResponse r = Http.get("https://api.example.com/users")
 *     .header("Accept", "application/json")
 *     .bearer(token)
 *     .timeoutSeconds(10)
 *     .send();
 * }</pre>
 *
 * <h2>Four ways to send</h2>
 * <ul>
 *   <li>{@link #send()} — graceful. Returns an {@link HttpResponse}
 *       whether success or failure. Never throws.</li>
 *   <li>{@link #sendOrThrow()} — throws {@link HttpException} on
 *       transport errors or non-2xx responses. Use this in stream
 *       pipelines where a silent failure would be worse.</li>
 *   <li>{@link #sendAsync()} — returns a
 *       {@link java.util.concurrent.CompletableFuture CompletableFuture}
 *       that completes with the response. The underlying
 *       {@code send()} runs on a background thread.</li>
 *   <li>{@link #sendWithRetry(int)} — retries on <b>transport errors
 *       only</b> (connection refused, DNS failure, timeout). HTTP error
 *       statuses (4xx, 5xx) are returned to the caller unretried —
 *       retrying a 404 or a 500 rarely helps.</li>
 * </ul>
 *
 * <h2>Listener notifications</h2>
 * <p>If a {@link HttpListener} is attached, the request notifies it on
 * every lifecycle event: {@code onStart}, {@code onSuccess},
 * {@code onFailure}, {@code onComplete}, {@code onRetry} (on retry),
 * {@code onRedirect} (on 3xx). Listener exceptions are logged and
 * swallowed — a broken UI listener never breaks the request.</p>
 *
 * <h2>Building the URL</h2>
 * <p>Query parameters added via {@link #query(String, String)} are
 * URL-encoded and appended when the request is sent. If the base URL
 * already has a {@code ?}, an {@code &} is used; otherwise a {@code ?}.
 * Fragments ({@code #}) and existing query strings are preserved.</p>
 *
 * <h2>Spinner</h2>
 * <p>{@link #withSpinner()} shows a terminal spinner while the request
 * is in flight. Useful for slow requests — has no effect on the
 * response. The spinner is automatically stopped before the response
 * is returned.</p>
 *
 * <h2>Verbose mode</h2>
 * <p>{@link #withVerbose()} logs the request URL and response status to
 * {@link chirp.internal.Trace} on every call. Equivalent to the
 * {@code -Dchirp.http.verbose=true} system property.</p>
 *
 * <h2>Related</h2>
 * <p>For the static factory methods, see {@link Http}. For the response
 * record, see {@link HttpResponse}.</p>
 *
 * @see Http
 * @see HttpResponse
 * @see HttpListener
 * @see HttpException
 * @see Retry
 * @since 1.0.0
 */
public final class HttpRequest {

    private static volatile HttpClient CLIENT;
    private static final Object CLIENT_LOCK = new Object();

    private HttpMethod method = HttpMethod.GET;
    private String url;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<Map.Entry<String, String>> queryParams = new ArrayList<>();
    private String body;
    private Duration timeout = Duration.ofSeconds(30);
    private volatile boolean spinner;
    private volatile boolean verbose = Boolean.parseBoolean(System.getProperty("chirp.http.verbose", "false"));
    private String saveRaw;
    private HttpListener listener;

    // ============================================================== constructors

    /**
     * Constructs a bare request with no method or URL. Callers normally
     * use the static factories ({@link #get(String)},
     * {@link #post(String)}, or {@link Http#get(String)}).
     *
     * @see #get(String)
     * @see #post(String)
     * @see Http#get(String)
     * @since 1.0.0
     */
    public HttpRequest() {}

    /**
     * Constructs a request with an explicit method and URL.
     *
     * @param method the HTTP method (null → GET)
     * @param url the target URL (may be null — the request will fail on send)
     * @see #HttpRequest(String)
     * @see #of(HttpMethod, String)
     * @since 1.0.0
     */
    public HttpRequest(HttpMethod method, String url) {
        this.method = method == null ? HttpMethod.GET : method;
        this.url = url;
    }

    /**
     * Constructs a GET request for the given URL.
     *
     * @param url the target URL
     * @see #HttpRequest(HttpMethod, String)
     * @since 1.0.0
     */
    public HttpRequest(String url) {
        this.method = HttpMethod.GET;
        this.url = url;
    }

    // ============================================================== factories

    /**
     * Factory — a request with an explicit method and URL.
     *
     * @param method the HTTP method (null → GET)
     * @param url the target URL
     * @return a new request
     * @see #HttpRequest(HttpMethod, String)
     * @since 1.0.0
     */
    public static HttpRequest of(HttpMethod method, String url) {
        return new HttpRequest(method, url);
    }

    /**
     * Factory — a GET request.
     *
     * @param url the target URL
     * @return a new GET request
     * @see Http#get(String)
     * @since 1.0.0
     */
    public static HttpRequest get(String url) { return new HttpRequest(HttpMethod.GET, url); }

    /**
     * Factory — a POST request.
     *
     * @param url the target URL
     * @return a new POST request
     * @see Http#post(String)
     * @since 1.0.0
     */
    public static HttpRequest post(String url) { return new HttpRequest(HttpMethod.POST, url); }

    /**
     * Factory — a PUT request.
     *
     * @param url the target URL
     * @return a new PUT request
     * @see Http#put(String)
     * @since 1.0.0
     */
    public static HttpRequest put(String url) { return new HttpRequest(HttpMethod.PUT, url); }

    /**
     * Factory — a DELETE request.
     *
     * @param url the target URL
     * @return a new DELETE request
     * @see Http#delete(String)
     * @since 1.0.0
     */
    public static HttpRequest delete(String url) { return new HttpRequest(HttpMethod.DELETE, url); }

    /**
     * Factory — a PATCH request.
     *
     * @param url the target URL
     * @return a new PATCH request
     * @see Http#patch(String)
     * @since 1.0.0
     */
    public static HttpRequest patch(String url) { return new HttpRequest(HttpMethod.PATCH, url); }

    /**
     * Factory — a HEAD request.
     *
     * @param url the target URL
     * @return a new HEAD request
     * @see Http#head(String)
     * @since 1.0.0
     */
    public static HttpRequest head(String url) { return new HttpRequest(HttpMethod.HEAD, url); }

    /**
     * Factory — an OPTIONS request.
     *
     * @param url the target URL
     * @return a new OPTIONS request
     * @see Http#options(String)
     * @since 1.0.0
     */
    public static HttpRequest options(String url) { return new HttpRequest(HttpMethod.OPTIONS, url); }

    /**
     * Factory — a request from key-value pairs.
     *
     * <pre>{@code
     * HttpRequest r = HttpRequest.from(
     *     Http.Key.METHOD, HttpMethod.POST,
     *     Http.Key.URL, "https://api.example.com",
     *     Http.Key.TIMEOUT, 10);
     * }</pre>
     *
     * @param keyValues alternating {@link Http.Key} → value pairs
     * @return a new request
     * @see Http#from(Object...)
     * @since 1.0.0
     */
    public static HttpRequest from(Object... keyValues) {
        HttpRequest req = new HttpRequest();
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                Object key = keyValues[i], val = keyValues[i + 1];
                if (key == Http.Key.METHOD && val instanceof HttpMethod m) req.method(m);
                else if (key == Http.Key.URL && val instanceof String s) req.url(s);
                else if (key == Http.Key.TIMEOUT && val instanceof Integer n) req.timeoutSeconds(n);
            }
        }
        return req;
    }

    // ============================================================== configuration

    /**
     * Sets the URL. Returns this request, for chaining.
     *
     * @param url the target URL
     * @return this request
     * @see #endpoint(String)
     * @see #to(String)
     * @since 1.0.0
     */
    public HttpRequest url(String url) { this.url = url; return this; }

    /**
     * Alias of {@link #url(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return this request
     * @see #url(String)
     * @since 1.0.0
     */
    public HttpRequest endpoint(String url) { return url(url); }

    /**
     * Alias of {@link #url(String)} — see that method for full
     * documentation.
     *
     * @param url the target URL
     * @return this request
     * @see #url(String)
     * @since 1.0.0
     */
    public HttpRequest to(String url) { return url(url); }

    /**
     * Sets the HTTP method.
     *
     * @param method the method (null → GET)
     * @return this request
     * @see #verb(HttpMethod)
     * @since 1.0.0
     */
    public HttpRequest method(HttpMethod method) {
        this.method = method == null ? HttpMethod.GET : method;
        return this;
    }

    /**
     * Alias of {@link #method(HttpMethod)} — see that method for full
     * documentation.
     *
     * @param method the HTTP method
     * @return this request
     * @see #method(HttpMethod)
     * @since 1.0.0
     */
    public HttpRequest verb(HttpMethod method) { return method(method); }

    /**
     * Sets a single request header. Replaces any existing header with
     * the same name.
     *
     * @param k the header name (null → no-op)
     * @param v the header value (null → no-op)
     * @return this request
     * @see #withHeader(String, String)
     * @see #addHeader(String, String)
     * @see #headers(Map)
     * @since 1.0.0
     */
    public HttpRequest header(String k, String v) {
        if (k != null && v != null) headers.put(k, v);
        return this;
    }

    /**
     * Alias of {@link #header(String, String)} — see that method for
     * full documentation.
     *
     * @param k the header name
     * @param v the header value
     * @return this request
     * @see #header(String, String)
     * @since 1.0.0
     */
    public HttpRequest withHeader(String k, String v) { return header(k, v); }

    /**
     * Alias of {@link #header(String, String)} — see that method for
     * full documentation.
     *
     * @param k the header name
     * @param v the header value
     * @return this request
     * @see #header(String, String)
     * @since 1.0.0
     */
    public HttpRequest addHeader(String k, String v) { return header(k, v); }

    /**
     * Adds all headers from the given map.
     *
     * @param m the map of headers (null → no-op)
     * @return this request
     * @see #header(String, String)
     * @since 1.0.0
     */
    public HttpRequest headers(Map<String, String> m) {
        if (m != null) headers.putAll(m);
        return this;
    }

    /**
     * Sets the request body as plain text.
     *
     * @param body the body (null → no body)
     * @return this request
     * @see #withBody(String)
     * @see #setBody(String)
     * @see #content(String)
     * @see #json(String)
     * @since 1.0.0
     */
    public HttpRequest body(String body) { this.body = body; return this; }

    /**
     * Alias of {@link #body(String)} — see that method for full
     * documentation.
     *
     * @param body the request body
     * @return this request
     * @see #body(String)
     * @since 1.0.0
     */
    public HttpRequest withBody(String body) { return body(body); }

    /**
     * Alias of {@link #body(String)} — see that method for full
     * documentation.
     *
     * @param body the request body
     * @return this request
     * @see #body(String)
     * @since 1.0.0
     */
    public HttpRequest setBody(String body) { return body(body); }

    /**
     * Alias of {@link #body(String)} — see that method for full
     * documentation.
     *
     * @param body the request body
     * @return this request
     * @see #body(String)
     * @since 1.0.0
     */
    public HttpRequest content(String body) { return body(body); }

    /**
     * Sets the request body as JSON and the {@code Content-Type} header
     * to {@code application/json}.
     *
     * @param body the JSON body
     * @return this request
     * @see #withJson(String)
     * @see #bodyJson(String)
     * @see #jsonBody(String)
     * @see #body(String)
     * @since 1.0.0
     */
    public HttpRequest json(String body) {
        this.body = body;
        this.headers.putIfAbsent("Content-Type", "application/json");
        return this;
    }

    /**
     * Alias of {@link #json(String)} — see that method for full
     * documentation.
     *
     * @param body the JSON body
     * @return this request
     * @see #json(String)
     * @since 1.0.0
     */
    public HttpRequest withJson(String body) { return json(body); }

    /**
     * Alias of {@link #json(String)} — see that method for full
     * documentation.
     *
     * @param body the JSON body
     * @return this request
     * @see #json(String)
     * @since 1.0.0
     */
    public HttpRequest bodyJson(String body) { return json(body); }

    /**
     * Alias of {@link #json(String)} — see that method for full
     * documentation.
     *
     * @param body the JSON body
     * @return this request
     * @see #json(String)
     * @since 1.0.0
     */
    public HttpRequest jsonBody(String body) { return json(body); }

    /**
     * Sets the body as form-encoded data and the {@code Content-Type}
     * header to {@code application/x-www-form-urlencoded}.
     *
     * @param formEncoded the pre-encoded form body (caller must encode
     * via {@link URLEncoder})
     * @return this request
     * @see #formBody(String)
     * @see #body(String)
     * @since 1.0.0
     */
    public HttpRequest form(String formEncoded) {
        this.body = formEncoded;
        this.headers.putIfAbsent("Content-Type", "application/x-www-form-urlencoded");
        return this;
    }

    /**
     * Alias of {@link #form(String)} — see that method for full
     * documentation.
     *
     * @param formEncoded the form-encoded body
     * @return this request
     * @see #form(String)
     * @since 1.0.0
     */
    public HttpRequest formBody(String formEncoded) { return form(formEncoded); }

    /**
     * Sets the {@code Authorization: Bearer ...} header.
     *
     * @param token the bearer token (null → no-op)
     * @return this request
     * @see #auth(String)
     * @see #withBearer(String)
     * @see #token(String)
     * @see #basicAuth(String, String)
     * @since 1.0.0
     */
    public HttpRequest bearer(String token) {
        if (token != null) headers.put("Authorization", "Bearer " + token);
        return this;
    }

    /**
     * Alias of {@link #bearer(String)} — see that method for full
     * documentation.
     *
     * @param token the bearer token
     * @return this request
     * @see #bearer(String)
     * @since 1.0.0
     */
    public HttpRequest auth(String token) { return bearer(token); }

    /**
     * Alias of {@link #bearer(String)} — see that method for full
     * documentation.
     *
     * @param tok the bearer token
     * @return this request
     * @see #bearer(String)
     * @since 1.0.0
     */
    public HttpRequest withBearer(String tok) { return bearer(tok); }

    /**
     * Alias of {@link #bearer(String)} — see that method for full
     * documentation.
     *
     * @param tok the bearer token
     * @return this request
     * @see #bearer(String)
     * @since 1.0.0
     */
    public HttpRequest token(String tok) { return bearer(tok); }

    /**
     * Sets the {@code Authorization: Basic ...} header from a
     * username/password pair.
     *
     * @param user the username (null → no-op)
     * @param password the password (null → no-op)
     * @return this request
     * @see #authBasic(String, String)
     * @see #bearer(String)
     * @since 1.0.0
     */
    public HttpRequest basicAuth(String user, String password) {
        if (user != null && password != null) {
            String encoded = java.util.Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encoded);
        }
        return this;
    }

    /**
     * Alias of {@link #basicAuth(String, String)} — see that method for
     * full documentation.
     *
     * @param user the username
     * @param password the password
     * @return this request
     * @see #basicAuth(String, String)
     * @since 1.0.0
     */
    public HttpRequest authBasic(String user, String password) { return basicAuth(user, password); }

    /**
     * Adds a query parameter. Encoded when the request is sent, so
     * special characters are handled correctly.
     *
     * @param k the parameter name (null → no-op)
     * @param v the parameter value (null → no-op)
     * @return this request
     * @see #param(String, String)
     * @see #withParam(String, String)
     * @since 1.0.0
     */
    public HttpRequest query(String k, String v) {
        if (k != null && v != null) queryParams.add(Map.entry(k, v));
        return this;
    }

    /**
     * Alias of {@link #query(String, String)} — see that method for full
     * documentation.
     *
     * @param k the parameter name
     * @param v the parameter value
     * @return this request
     * @see #query(String, String)
     * @since 1.0.0
     */
    public HttpRequest param(String k, String v) { return query(k, v); }

    /**
     * Alias of {@link #query(String, String)} — see that method for full
     * documentation.
     *
     * @param k the parameter name
     * @param v the parameter value
     * @return this request
     * @see #query(String, String)
     * @since 1.0.0
     */
    public HttpRequest withParam(String k, String v) { return query(k, v); }

    /**
     * Sets the request timeout. Negative durations are ignored.
     *
     * @param d the timeout (null or negative → no change)
     * @return this request
     * @see #timeoutSeconds(int)
     * @see #timeoutMillis(int)
     * @since 1.0.0
     */
    public HttpRequest timeout(Duration d) { if (d != null && !d.isNegative()) this.timeout = d; return this; }

    /**
     * Sets the request timeout in seconds (clamped to ≥ 1).
     *
     * @param s the timeout in seconds
     * @return this request
     * @see #timeout(Duration)
     * @since 1.0.0
     */
    public HttpRequest timeoutSeconds(int s) { return timeout(Duration.ofSeconds(Math.max(1, s))); }

    /**
     * Sets the request timeout in milliseconds (clamped to ≥ 100).
     *
     * @param ms the timeout in milliseconds
     * @return this request
     * @see #timeout(Duration)
     * @since 1.0.0
     */
    public HttpRequest timeoutMillis(int ms) { return timeout(Duration.ofMillis(Math.max(100, ms))); }

    /**
     * Enables or disables the terminal spinner for this request.
     *
     * @param enabled whether to show the spinner
     * @return this request
     * @see #withSpinner()
     * @see #noSpinner()
     * @since 1.0.0
     */
    public HttpRequest spinner(boolean enabled) { this.spinner = enabled; return this; }

    /**
     * Enables the terminal spinner — see {@link #spinner(boolean)}.
     *
     * @return this request
     * @see #spinner(boolean)
     * @since 1.0.0
     */
    public HttpRequest withSpinner() { return spinner(true); }

    /**
     * Disables the terminal spinner.
     *
     * @return this request
     * @see #spinner(boolean)
     * @since 1.0.0
     */
    public HttpRequest noSpinner() { return spinner(false); }

    /**
     * Enables or disables verbose logging to {@link Trace} on every
     * request/response.
     *
     * @param enabled whether to log
     * @return this request
     * @see #withVerbose()
     * @see #log()
     * @since 1.0.0
     */
    public HttpRequest verbose(boolean enabled) { this.verbose = enabled; return this; }

    /**
     * Enables verbose logging — see {@link #verbose(boolean)}.
     *
     * @return this request
     * @see #verbose(boolean)
     * @since 1.0.0
     */
    public HttpRequest withVerbose() { return verbose(true); }

    /**
     * Alias of {@link #withVerbose()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #withVerbose()
     * @since 1.0.0
     */
    public HttpRequest log() { return verbose(true); }

    /**
     * Saves the raw response body to the given file path after a
     * successful request.
     *
     * @param path the save path (null → no save)
     * @return this request
     * @see #saveTo(String)
     * @since 1.0.0
     */
    public HttpRequest saveRaw(String path) {
        this.saveRaw = path;
        return this;
    }

    /**
     * Alias of {@link #saveRaw(String)} — see that method for full
     * documentation.
     *
     * @param path the save path
     * @return this request
     * @see #saveRaw(String)
     * @since 1.0.0
     */
    public HttpRequest saveTo(String path) { return saveRaw(path); }

    /**
     * Attaches a listener for lifecycle events.
     *
     * @param l the listener (null → clears any previously attached listener)
     * @return this request
     * @see #onEvent(HttpListener)
     * @see HttpListener
     * @since 1.0.0
     */
    public HttpRequest listener(HttpListener l) { this.listener = l; return this; }

    /**
     * Alias of {@link #listener(HttpListener)} — see that method for
     * full documentation.
     *
     * @param l the listener
     * @return this request
     * @see #listener(HttpListener)
     * @since 1.0.0
     */
    public HttpRequest onEvent(HttpListener l) { return listener(l); }

    // ============================================================== accessors

    /**
     * Returns the HTTP method.
     *
     * @return the method
     * @since 1.0.0
     */
    public HttpMethod method() { return method; }

    /**
     * Returns the URL.
     *
     * @return the URL
     * @since 1.0.0
     */
    public String url() { return url; }

    /**
     * Returns an unmodifiable view of the current headers.
     *
     * @return the headers map
     * @since 1.0.0
     */
    public Map<String, String> headers() { return Map.copyOf(headers); }

    /**
     * Returns the current timeout.
     *
     * @return the timeout
     * @since 1.0.0
     */
    public Duration timeout() { return timeout; }

    /**
     * Returns the current body, or null if no body was set.
     *
     * @return the body, or null
     * @since 1.0.0
     */
    public String body() { return body; }

    // ============================================================== send

    /**
     * Sends the request and returns the response. Never throws —
     * transport errors come back as an {@link HttpResponse} with
     * {@link HttpResponse#hasTransportError()} true.
     *
     * <p>Notifies the listener (if any) on start, complete, success or
     * failure, and redirect. Stops the spinner (if enabled) before
     * returning.</p>
     *
     * @return the response (never null)
     * @see #sendOrThrow()
     * @see #sendAsync()
     * @see #sendWithRetry(int)
     * @see #execute()
     * @since 1.0.0
     */
    public HttpResponse send() {
        long start = System.nanoTime();
        Spinner sp = spinner ? Spinner.start(describe()) : null;

        try {
            notifyStart();

            if (url == null || url.isBlank()) {
                HttpResponse response = HttpResponse.error(method, "?", Duration.ZERO,
                        "URL is required");
                return notifyEarlyFailure(response, new HttpException(response.statusText()));
            }

            URI uri;
            try {
                uri = buildUri();
            } catch (Exception e) {
                return notifyEarlyFailure(HttpResponse.error(method, url, Duration.ZERO,
                        "Invalid URL: " + e.getMessage()), e);
            }

            if (verbose) Trace.debug("http", "request: {} {}", method, uri);

            java.net.http.HttpRequest request = buildJdkRequest(uri);

            java.net.http.HttpResponse<String> raw = client().send(
                    request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

            HttpResponse response = new HttpResponse(
                    method, url, raw.statusCode(), statusText(raw.statusCode()),
                    raw.body(), raw.headers().map(), elapsed);

            if (verbose)
                Trace.debug("http", "response: {} {}ms",
                        response.status(), response.durationMillis());

            notifyComplete(response);
            if (response.isRedirect()) notifyRedirect(response);

            if (response.isSuccess()) notifySuccess(response);
            else notifyFailure(null);

            if (saveRaw != null) {
                try {
                    TextFileIO.write(saveRaw, response.text());
                } catch (RuntimeException e) {
                    Trace.warn("http", "saveRaw failed: " + e.getMessage());
                }
            }

            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
            HttpResponse response = HttpResponse.error(method, url, elapsed,
                    "Request was interrupted");
            notifyFailure(e);
            notifyComplete(response);
            return response;
        } catch (Exception e) {
            Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
            HttpResponse response = HttpResponse.error(method, url, elapsed,
                    "Request failed: " + e.getMessage());
            notifyFailure(e);
            notifyComplete(response);
            return response;
        } finally {
            if (sp != null) sp.stop();
        }
    }

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the response
     * @see #send()
     * @since 1.0.0
     */
    public HttpResponse execute() { return send(); }

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the response
     * @see #send()
     * @since 1.0.0
     */
    public HttpResponse call() { return send(); }

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the response
     * @see #send()
     * @since 1.0.0
     */
    public HttpResponse fire() { return send(); }

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the response
     * @see #send()
     * @since 1.0.0
     */
    public HttpResponse make() { return send(); }

    /**
     * Sends the request and throws {@link HttpException} on transport
     * failure or non-2xx response.
     *
     * <p>The exception carries the status code and body, so a caller can
     * distinguish "bad request" (4xx) from "server error" (5xx) from
     * "network down" (transport error with status -1).</p>
     *
     * @return the successful response
     * @throws HttpException if the request fails
     * @see #send()
     * @see #executeOrThrow()
     * @since 1.0.0
     */
    public HttpResponse sendOrThrow() {
        HttpResponse response = send();
        if (response.hasTransportError()) {
            throw new HttpException(response.statusText(), response.body());
        }
        if (!response.isSuccess()) {
            throw new HttpException("HTTP " + response.status() + " " + response.statusText(),
                    response.status(), response.body());
        }
        return response;
    }

    /**
     * Alias of {@link #sendOrThrow()} — see that method for full
     * documentation and examples.
     *
     * @return the successful response
     * @throws HttpException if the request fails
     * @see #sendOrThrow()
     * @since 1.0.0
     */
    public HttpResponse executeOrThrow() { return sendOrThrow(); }

    /**
     * Sends the request asynchronously — returns a
     * {@link java.util.concurrent.CompletableFuture CompletableFuture}
     * that completes with the response. The actual {@code send()} call
     * runs on a background thread.
     *
     * @return a future that completes with the response
     * @see #send()
     * @see #execAsync()
     * @since 1.0.0
     */
    public CompletableFuture<HttpResponse> sendAsync() {
        return CompletableFuture.supplyAsync(this::send);
    }

    /**
     * Alias of {@link #sendAsync()} — see that method for full
     * documentation.
     *
     * @return a future that completes with the response
     * @see #sendAsync()
     * @since 1.0.0
     */
    public CompletableFuture<HttpResponse> execAsync() { return sendAsync(); }

    /**
     * Sends with retries — retries on <b>transport errors only</b>
     * (connection refused, DNS failure, timeout). HTTP error statuses
     * (4xx, 5xx) are returned to the caller unretried.
     *
     * <p>Uses {@link Retry} internally with a 500ms backoff between
     * attempts. If the request still fails after all attempts, the last
     * {@link HttpException} is rethrown.</p>
     *
     * @param attempts the total number of attempts (≥ 1)
     * @return the successful response
     * @throws HttpException if all attempts fail at the transport level
     * @see #execWithRetry(int)
     * @see #send()
     * @since 1.0.0
     */
    public HttpResponse sendWithRetry(int attempts) {
        int maxAttempts = Math.max(1, attempts);
        AtomicInteger attempt = new AtomicInteger();
        return Retry.of(maxAttempts, Duration.ofMillis(500)).execute(() -> {
            int current = attempt.incrementAndGet();
            HttpResponse r = send();
            if (r.hasTransportError()) {
                HttpException cause = new HttpException(r.statusText());
                // onRetry fires only when another attempt will actually follow
                if (listener != null && current < maxAttempts) {
                    try {
                        listener.onRetry(this, current, cause);
                    } catch (RuntimeException e) {
                        Trace.warn("http", "onRetry listener threw: " + e.getMessage());
                    }
                }
                throw cause;
            }
            return r;
        });
    }

    /**
     * Alias of {@link #sendWithRetry(int)} — see that method for full
     * documentation and examples.
     *
     * @param attempts the number of attempts
     * @return the successful response
     * @throws HttpException if all attempts fail
     * @see #sendWithRetry(int)
     * @since 1.0.0
     */
    public HttpResponse execWithRetry(int attempts) { return sendWithRetry(attempts); }

    /**
     * Sends the request with key-value options — allows overriding the
     * timeout or the retry count in one call.
     *
     * <pre>{@code
     * Http.get(url).send(Http.Key.TIMEOUT, 5);
     * Http.get(url).send(Http.Key.RETRIES, 3);
     * }</pre>
     *
     * @param keyValues alternating {@link Http.Key} → value pairs
     * @return the response
     * @see #send()
     * @see #sendWithRetry(int)
     * @since 1.0.0
     */
    public HttpResponse send(Object... keyValues) {
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                Object key = keyValues[i], val = keyValues[i + 1];
                if (key == Http.Key.TIMEOUT && val instanceof Integer n) timeoutSeconds(n);
                else if (key == Http.Key.RETRIES && val instanceof Integer n) {
                    return sendWithRetry(n);
                }
            }
        }
        return send();
    }

    // ============================================================== internals

    private URI buildUri() {
        StringBuilder sb = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            sb.append(url.contains("?") ? '&' : '?');
            for (int i = 0; i < queryParams.size(); i++) {
                if (i > 0) sb.append('&');
                Map.Entry<String, String> e = queryParams.get(i);
                sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            }
        }
        return URI.create(sb.toString());
    }

    private java.net.http.HttpRequest buildJdkRequest(URI uri) {
        java.net.http.HttpRequest.Builder b = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout);

        if (body != null) {
            b.method(method.name(),
                    java.net.http.HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            b.method(method.name(), java.net.http.HttpRequest.BodyPublishers.noBody());
        }

        for (Map.Entry<String, String> e : headers.entrySet()) {
            b.header(e.getKey(), e.getValue());
        }
        return b.build();
    }

    private String describe() {
        return "HTTP " + method + " " + (url == null ? "?" : url);
    }

    private void notifyStart() {
        if (listener == null) return;
        try {
            listener.onStart(this);
        } catch (RuntimeException e) {
            Trace.warn("http", "onStart listener threw: " + e.getMessage());
        }
    }
    private void notifyComplete(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onComplete(r);
        } catch (RuntimeException e) {
            Trace.warn("http", "onComplete listener threw: " + e.getMessage());
        }
    }
    private void notifySuccess(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onSuccess(r);
        } catch (RuntimeException e) {
            Trace.warn("http", "onSuccess listener threw: " + e.getMessage());
        }
    }
    private void notifyFailure(Throwable t) {
        if (listener == null) return;
        try {
            listener.onFailure(this, t);
        } catch (RuntimeException e) {
            Trace.warn("http", "onFailure listener threw: " + e.getMessage());
        }
    }
    private void notifyRedirect(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onRedirect(r);
        } catch (RuntimeException e) {
            Trace.warn("http", "onRedirect listener threw: " + e.getMessage());
        }
    }
    /** Reports a request that failed before reaching the network (missing or invalid URL). */
    private HttpResponse notifyEarlyFailure(HttpResponse r, Throwable cause) {
        notifyFailure(cause);
        notifyComplete(r);
        return r;
    }

    // ============================================================== shared client

    private static HttpClient client() {
        if (CLIENT == null) {
            synchronized (CLIENT_LOCK) {
                if (CLIENT == null) {
                    CLIENT = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                }
            }
        }
        return CLIENT;
    }

    /**
     * Test hook — clears the shared HTTP client so tests can reset state.
     *
     * @see Http#reset()
     * @since 1.0.0
     */
    public static void resetClient() {
        synchronized (CLIENT_LOCK) {
            CLIENT = null;
        }
    }

    // ============================================================== status text

    /**
     * Returns a human-readable label for the given HTTP status code.
     *
     * <p>Known codes get their standard labels ({@code 200 → "OK"},
     * {@code 404 → "Not Found"}); unknown codes get a category label
     * ({@code "Client Error"} for 4xx, {@code "Server Error"} for 5xx).</p>
     *
     * @param code the HTTP status code
     * @return the label, or empty for an unrecognised code
     * @since 1.0.0
     */
    public static String statusText(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> code >= 200 && code < 300
                    ? "Success"
                    : code >= 400 && code < 500
                            ? "Client Error"
                            : code >= 500
                                    ? "Server Error"
                                    : "";
        };
    }
}
