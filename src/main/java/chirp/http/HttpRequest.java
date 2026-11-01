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
 * Mutable request builder that also sends itself.
 *
 * <h2>Naming</h2>
 * <p>{@code header / withHeader / addHeader}; {@code json / withJson / bodyJson};
 * {@code body / withBody / setBody / content}; {@code query / param / withParam};
 * {@code bearer / auth / withBearer / token}; {@code basicAuth / authBasic};
 * {@code send / execute / call / fire / make}; {@code sendOrThrow / executeOrThrow};
 * {@code sendAsync / sendAsync / execAsync}; {@code sendWithRetry / execWithRetry}.</p>
 *
 * <h2>Key-value factory</h2>
 * <p>{@link Http.Key} drives {@code Http.of(Key.METHOD, GET, Key.URL, "...", ...)}.</p>
 *
 * <h2>Retry integration</h2>
 * <p>{@link #sendWithRetry(int)} combines this request with
 * {@link chirp.decorator.Retry} — retries on transport failures only (5xx
 * errors are returned to the caller, not retried, since retrying a 500 is
 * usually wrong for a beginner).</p>
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
    private boolean spinner;
    private boolean verbose = Boolean.parseBoolean(System.getProperty("chirp.http.verbose", "false"));
    private String saveRaw;
    private HttpListener listener;

    // ============================================================== constructors

    public HttpRequest() {}
    public HttpRequest(HttpMethod method, String url) {
        this.method = method == null ? HttpMethod.GET : method;
        this.url = url;
    }
    public HttpRequest(String url) {
        this.method = HttpMethod.GET;
        this.url = url;
    }

    // ============================================================== factories

    public static HttpRequest of(HttpMethod method, String url) {
        return new HttpRequest(method, url);
    }
    public static HttpRequest get(String url) { return new HttpRequest(HttpMethod.GET, url); }
    public static HttpRequest post(String url) { return new HttpRequest(HttpMethod.POST, url); }
    public static HttpRequest put(String url) { return new HttpRequest(HttpMethod.PUT, url); }
    public static HttpRequest delete(String url) { return new HttpRequest(HttpMethod.DELETE, url); }
    public static HttpRequest patch(String url) { return new HttpRequest(HttpMethod.PATCH, url); }
    public static HttpRequest head(String url) { return new HttpRequest(HttpMethod.HEAD, url); }
    public static HttpRequest options(String url) { return new HttpRequest(HttpMethod.OPTIONS, url); }

    /** Key-value factory — {@code HttpRequest.from(Key.METHOD, GET, Key.URL, "...")}. */
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

    public HttpRequest url(String url) { this.url = url; return this; }
    public HttpRequest endpoint(String url) { return url(url); }
    public HttpRequest to(String url) { return url(url); }

    public HttpRequest method(HttpMethod method) {
        this.method = method == null ? HttpMethod.GET : method;
        return this;
    }
    public HttpRequest verb(HttpMethod method) { return method(method); }

    public HttpRequest header(String k, String v) {
        if (k != null && v != null) headers.put(k, v);
        return this;
    }
    public HttpRequest withHeader(String k, String v) { return header(k, v); }
    public HttpRequest addHeader(String k, String v) { return header(k, v); }

    public HttpRequest headers(Map<String, String> m) {
        if (m != null) headers.putAll(m);
        return this;
    }

    public HttpRequest body(String body) { this.body = body; return this; }
    public HttpRequest withBody(String body) { return body(body); }
    public HttpRequest setBody(String body) { return body(body); }
    public HttpRequest content(String body) { return body(body); }

    public HttpRequest json(String body) {
        this.body = body;
        this.headers.putIfAbsent("Content-Type", "application/json");
        return this;
    }
    public HttpRequest withJson(String body) { return json(body); }
    public HttpRequest bodyJson(String body) { return json(body); }
    public HttpRequest jsonBody(String body) { return json(body); }

    public HttpRequest form(String formEncoded) {
        this.body = formEncoded;
        this.headers.putIfAbsent("Content-Type", "application/x-www-form-urlencoded");
        return this;
    }
    public HttpRequest formBody(String formEncoded) { return form(formEncoded); }

    public HttpRequest bearer(String token) {
        if (token != null) headers.put("Authorization", "Bearer " + token);
        return this;
    }
    public HttpRequest auth(String token) { return bearer(token); }
    public HttpRequest withBearer(String tok) { return bearer(tok); }
    public HttpRequest token(String tok) { return bearer(tok); }

    public HttpRequest basicAuth(String user, String password) {
        if (user != null && password != null) {
            String encoded = java.util.Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encoded);
        }
        return this;
    }
    public HttpRequest authBasic(String user, String password) { return basicAuth(user, password); }

    public HttpRequest query(String k, String v) {
        if (k != null && v != null) queryParams.add(Map.entry(k, v));
        return this;
    }
    public HttpRequest param(String k, String v) { return query(k, v); }
    public HttpRequest withParam(String k, String v) { return query(k, v); }

    public HttpRequest timeout(Duration d) { if (d != null && !d.isNegative()) this.timeout = d; return this; }
    public HttpRequest timeoutSeconds(int s) { return timeout(Duration.ofSeconds(Math.max(1, s))); }
    public HttpRequest timeoutMillis(int ms) { return timeout(Duration.ofMillis(Math.max(100, ms))); }

    public HttpRequest spinner(boolean enabled) { this.spinner = enabled; return this; }
    public HttpRequest withSpinner() { return spinner(true); }
    public HttpRequest noSpinner() { return spinner(false); }

    public HttpRequest verbose(boolean enabled) { this.verbose = enabled; return this; }
    public HttpRequest withVerbose() { return verbose(true); }
    public HttpRequest log() { return verbose(true); }

    /** Save the raw response body to this path after the call completes. */
    public HttpRequest saveRaw(String path) {
        this.saveRaw = path;
        return this;
    }
    public HttpRequest saveTo(String path) { return saveRaw(path); }

    public HttpRequest listener(HttpListener l) { this.listener = l; return this; }
    public HttpRequest onEvent(HttpListener l) { return listener(l); }

    // ============================================================== accessors

    public HttpMethod method() { return method; }
    public String url() { return url; }
    public Map<String, String> headers() { return Map.copyOf(headers); }
    public Duration timeout() { return timeout; }
    public String body() { return body; }

    // ============================================================== send

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
                } catch (Exception ignored) {}
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
    public HttpResponse execute() { return send(); }
    public HttpResponse call() { return send(); }
    public HttpResponse fire() { return send(); }
    public HttpResponse make() { return send(); }

    /** Sends and throws {@link HttpException} on transport failure or non-2xx status. */
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
    public HttpResponse executeOrThrow() { return sendOrThrow(); }

    public CompletableFuture<HttpResponse> sendAsync() {
        return CompletableFuture.supplyAsync(this::send);
    }
    public CompletableFuture<HttpResponse> execAsync() { return sendAsync(); }

    /**
     * Sends with retries — retries only on transport errors (connection
     * refused, DNS failure, timeout). HTTP error responses (4xx/5xx) are
     * returned to the caller unretried — students usually want to see those.
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
                if (listener != null && current < maxAttempts) try {
                    listener.onRetry(this, current, cause);
                } catch (Exception ignored) {}
                throw cause;
            }
            return r;
        });
    }
    public HttpResponse execWithRetry(int attempts) { return sendWithRetry(attempts); }

    /** Key-value send — {@code send(Key.TIMEOUT, 10)}. */
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
        } catch (Exception ignored) {}
    }
    private void notifyComplete(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onComplete(r);
        } catch (Exception ignored) {}
    }
    private void notifySuccess(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onSuccess(r);
        } catch (Exception ignored) {}
    }
    private void notifyFailure(Throwable t) {
        if (listener == null) return;
        try {
            listener.onFailure(this, t);
        } catch (Exception ignored) {}
    }
    private void notifyRedirect(HttpResponse r) {
        if (listener == null) return;
        try {
            listener.onRedirect(r);
        } catch (Exception ignored) {}
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

    /** Test hook — clears the shared client so tests can reset state. */
    public static void resetClient() {
        synchronized (CLIENT_LOCK) {
            CLIENT = null;
        }
    }

    // ============================================================== status text

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
