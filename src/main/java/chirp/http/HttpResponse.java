package chirp.http;

import chirp.json.Json;
import chirp.json.JsonNull;
import chirp.json.JsonObject;
import chirp.json.JsonValue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of an HTTP call — status, body, headers, timing.
 *
 * <p>Every call to {@link HttpRequest#send()} returns one of these,
 * whether the request succeeded or failed. The only question the caller
 * needs to ask is {@link #isSuccess()}, and even that is optional —
 * {@link #text()} always returns a valid string (empty on error), so a
 * caller who doesn't care about the outcome can just read
 * {@code response.text()} and get whatever came back.</p>
 *
 * <h2>Reading the status</h2>
 * <table border="1">
 *   <caption>Status classification — pick the check that matches your case</caption>
 *   <tr><th>Method</th><th>True when</th><th>Use for</th></tr>
 *   <tr><td>{@link #isSuccess()}</td><td>2xx</td><td>Normal happy path</td></tr>
 *   <tr><td>{@link #isError()}</td><td>Not 2xx, or transport error</td>
 *       <td>"Did anything go wrong?"</td></tr>
 *   <tr><td>{@link #isClientError()}</td><td>4xx</td>
 *       <td>Distinguish "bad request" from "server down"</td></tr>
 *   <tr><td>{@link #isServerError()}</td><td>5xx</td>
 *       <td>Retry these; don't retry 4xx</td></tr>
 *   <tr><td>{@link #isRedirect()}</td><td>3xx</td>
 *       <td>Rarely needed — the client follows redirects by default</td></tr>
 *   <tr><td>{@link #hasTransportError()}</td><td>Status is -1</td>
 *       <td>The request never reached the server</td></tr>
 * </table>
 *
 * <h2>Reading the body</h2>
 * <p>{@link #text()} returns the response body as a string, or
 * {@code ""} when there is no body (transport error, 204, HEAD request).
 * It <b>never</b> returns null, so {@code response.text().length()}
 * and friends are always safe.</p>
 *
 * <p>{@link #object()} parses the body as JSON and returns a
 * {@link JsonObject}. If the body is not a JSON object, returns an empty
 * {@code JsonObject} — never null, never throws. This is how you write:</p>
 * <pre>{@code
 * String name = Http.get(url).send().object().getString("name");
 * }</pre>
 * <p>If the body isn't valid JSON, {@code name} becomes {@code null}
 * (because {@code getString} on an empty object returns null) — no
 * exception, no crash. Compare {@link #json()} to distinguish
 * "parse failed" (returns {@code JsonNull.INSTANCE}) from "no body"
 * (also returns {@code JsonNull.INSTANCE}) if you need to.</p>
 *
 * <h2>Reading headers</h2>
 * <p>{@link #header(String)} returns the first value for a header,
 * case-insensitively. {@link #headerAll(String)} returns every value
 * (some headers like {@code Set-Cookie} have multiple).</p>
 *
 * <h2>Immutability</h2>
 * <p>The headers map and its inner lists are defensively copied in the
 * canonical constructor. The body is a string (immutable). The response
 * record is therefore fully immutable — no caller can mutate the
 * underlying data after construction.</p>
 *
 * <h2>Example — typical usage</h2>
 * <pre>{@code
 * HttpResponse r = Http.get("https://api.example.com/user/1").send();
 *
 * if (r.isSuccess()) {
 *     JsonObject user = r.object();
 *     String name = user.getString("name");
 *     println("User: " + name + " (fetched in " + r.durationMillis() + "ms)");
 * } else if (r.hasTransportError()) {
 *     println("Network unavailable: " + r.statusText());
 * } else {
 *     println("Server returned " + r.status() + ": " + r.text());
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the request builder that produces this record, see
 * {@link HttpRequest}. For the throw-on-error variant, see
 * {@link HttpException}.</p>
 *
 * @param method the HTTP method used for the request
 * @param url the URL that was requested
 * @param status the HTTP status code, or -1 for transport errors
 * @param statusText a human-readable status label ({@code "OK"},
 * {@code "Not Found"}, or the transport error message)
 * @param body the response body as a string (never null — empty for no
 * body)
 * @param headers the response headers (never null — empty for none)
 * @param duration the elapsed time between send and response
 * @see HttpRequest
 * @see HttpException
 * @since 1.0.0
 */
public record HttpResponse(
        HttpMethod method,
        String url,
        int status,
        String statusText,
        String body,
        Map<String, List<String>> headers,
        Duration duration) {

    public HttpResponse {
        headers = headers == null ? Map.of() : deepCopy(headers);
    }

    private static Map<String, List<String>> deepCopy(Map<String, List<String>> headers) {
        Map<String, List<String>> copy = new LinkedHashMap<>(headers.size());
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            List<String> values = e.getValue();
            copy.put(e.getKey(), values == null ? List.of() : List.copyOf(values));
        }
        return Collections.unmodifiableMap(copy);
    }

    // ============================================================== factories

    /**
     * Constructs an error response for a failed call.
     *
     * <p>Most callers should not construct responses directly — use
     * {@link HttpRequest#send()} or one of its variants. This factory is
     * public so custom clients can construct one on failure without
     * calling the network.</p>
     *
     * @param method the HTTP method
     * @param url the URL that was attempted (may be null → "?")
     * @param duration the elapsed time
     * @param message the failure message
     * @return a new error response with status -1
     * @since 1.0.0
     */
    public static HttpResponse error(HttpMethod method, String url,
            Duration duration, String message) {
        return new HttpResponse(method, url, -1,
                message == null ? "Network error" : message,
                "", Collections.emptyMap(), duration);
    }

    /**
     * Alias of {@link #error(HttpMethod, String, Duration, String)} —
     * see that method for full documentation.
     *
     * @param method the HTTP method
     * @param url the URL that was attempted
     * @param duration the elapsed time
     * @param message the failure message
     * @return a new error response
     * @see #error(HttpMethod, String, Duration, String)
     * @since 1.0.0
     */
    public static HttpResponse failed(HttpMethod method, String url,
            Duration duration, String message) {
        return error(method, url, duration, message);
    }

    // ============================================================== status

    /**
     * Checks whether the response is a 2xx success.
     *
     * @return {@code true} for 2xx
     * @see #isError()
     * @see #isOk()
     * @see #is2xx()
     * @since 1.0.0
     */
    public boolean isSuccess() { return status >= 200 && status < 300; }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean isOk() { return isSuccess(); }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean is2xx() { return isSuccess(); }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean succeeded() { return isSuccess(); }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean ok() { return isSuccess(); }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean good() { return isSuccess(); }

    /**
     * Checks whether the response is anything other than 2xx — including
     * transport errors (status -1).
     *
     * @return {@code true} if not 2xx
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean isError() { return status < 200 || status >= 400; }

    /**
     * Alias of {@link #isError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if not 2xx
     * @see #isError()
     * @since 1.0.0
     */
    public boolean isFailed() { return isError(); }

    /**
     * Alias of {@link #isError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if not 2xx
     * @see #isError()
     * @since 1.0.0
     */
    public boolean failed() { return isError(); }

    /**
     * Alias of {@link #isError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if not 2xx
     * @see #isError()
     * @since 1.0.0
     */
    public boolean bad() { return isError(); }

    /**
     * Checks whether the response is a 4xx client error.
     *
     * @return {@code true} for 4xx
     * @see #isServerError()
     * @see #is4xx()
     * @since 1.0.0
     */
    public boolean isClientError() { return status >= 400 && status < 500; }

    /**
     * Alias of {@link #isClientError()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 4xx
     * @see #isClientError()
     * @since 1.0.0
     */
    public boolean is4xx() { return isClientError(); }

    /**
     * Checks whether the response is a 5xx server error.
     *
     * @return {@code true} for 5xx
     * @see #isClientError()
     * @see #is5xx()
     * @since 1.0.0
     */
    public boolean isServerError() { return status >= 500; }

    /**
     * Alias of {@link #isServerError()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 5xx
     * @see #isServerError()
     * @since 1.0.0
     */
    public boolean is5xx() { return isServerError(); }

    /**
     * Checks whether the response is a 3xx redirect.
     *
     * @return {@code true} for 3xx
     * @see #is3xx()
     * @since 1.0.0
     */
    public boolean isRedirect() { return status >= 300 && status < 400; }

    /**
     * Alias of {@link #isRedirect()} — see that method for full
     * documentation.
     *
     * @return {@code true} for 3xx
     * @see #isRedirect()
     * @since 1.0.0
     */
    public boolean is3xx() { return isRedirect(); }

    /**
     * Checks whether the request failed at the transport layer — DNS
     * failure, connection refused, TLS error, timeout, and so on. The
     * request never reached the server.
     *
     * @return {@code true} if the status is -1
     * @see #isError()
     * @see #isTransportError()
     * @since 1.0.0
     */
    public boolean hasTransportError() { return status == -1; }

    /**
     * Alias of {@link #hasTransportError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if the status is -1
     * @see #hasTransportError()
     * @since 1.0.0
     */
    public boolean isTransportError() { return hasTransportError(); }

    /**
     * Returns the status code — the record's {@code status} component.
     *
     * @return the status code
     * @see #code()
     * @since 1.0.0
     */
    public int statusCode() { return status; }

    /**
     * Alias of {@link #statusCode()} — see that method for full
     * documentation.
     *
     * @return the status code
     * @see #statusCode()
     * @since 1.0.0
     */
    public int code() { return status; }

    // ============================================================== body

    /**
     * Returns the response body as a string. Never null — returns
     * {@code ""} when there is no body.
     *
     * @return the body
     * @see #body()
     * @see #raw()
     * @see #content()
     * @see #data()
     * @since 1.0.0
     */
    public String text() { return body == null ? "" : body; }

    /**
     * Alias of {@link #text()} — see that method for full documentation.
     *
     * @return the body
     * @see #text()
     * @since 1.0.0
     */
    public String body() { return text(); }

    /**
     * Alias of {@link #text()} — see that method for full documentation.
     *
     * @return the body
     * @see #text()
     * @since 1.0.0
     */
    public String raw() { return text(); }

    /**
     * Alias of {@link #text()} — see that method for full documentation.
     *
     * @return the body
     * @see #text()
     * @since 1.0.0
     */
    public String content() { return text(); }

    /**
     * Alias of {@link #text()} — see that method for full documentation.
     *
     * @return the body
     * @see #text()
     * @since 1.0.0
     */
    public String data() { return text(); }

    /**
     * Checks whether the response has a non-empty body.
     *
     * @return {@code true} if the body is non-empty
     * @see #length()
     * @since 1.0.0
     */
    public boolean hasBody() { return !text().isEmpty(); }

    /**
     * Alias of {@link #hasBody()} — see that method for full
     * documentation.
     *
     * @return {@code true} if the body is non-empty
     * @see #hasBody()
     * @since 1.0.0
     */
    public boolean notEmpty() { return hasBody(); }

    /**
     * Returns the length of the body in characters.
     *
     * @return the character count
     * @see #byteLength()
     * @since 1.0.0
     */
    public int length() { return text().length(); }

    /**
     * Returns the length of the body in UTF-8 bytes.
     *
     * @return the byte count
     * @see #length()
     * @since 1.0.0
     */
    public int byteLength() { return text().getBytes(StandardCharsets.UTF_8).length; }

    // ============================================================== JSON

    /**
     * Parses the body as JSON. Returns {@link JsonNull#INSTANCE} when
     * the body is empty or not valid JSON.
     *
     * @return the parsed JSON value, or {@code JsonNull.INSTANCE}
     * @see #object()
     * @see #asJson()
     * @since 1.0.0
     */
    public JsonValue json() {
        if (body == null || body.isBlank()) return JsonNull.INSTANCE;
        try {
            return Json.parse(body);
        } catch (RuntimeException e) {
            return JsonNull.INSTANCE;
        }
    }

    /**
     * Alias of {@link #json()} — see that method for full documentation.
     *
     * @return the parsed JSON value
     * @see #json()
     * @since 1.0.0
     */
    public JsonValue asJson() { return json(); }

    /**
     * Alias of {@link #json()} — see that method for full documentation.
     *
     * @return the parsed JSON value
     * @see #json()
     * @since 1.0.0
     */
    public JsonValue toJson() { return json(); }

    /**
     * Parses the body as a JSON object. Returns an <b>empty</b>
     * {@code JsonObject} when the body is empty, malformed, or not an
     * object.
     *
     * <p>Empty-object rather than null is deliberate — it lets you
     * chain {@code response.object().getString("field")} without
     * null-checks, matching the null-safety policy of
     * {@link chirp.json.JsonObject}.</p>
     *
     * @return the parsed object, or empty
     * @see #json()
     * @see #asObject()
     * @see #toObject()
     * @see #toJsonObject()
     * @since 1.0.0
     */
    public JsonObject object() {
        JsonValue value = json();
        return value instanceof JsonObject obj ? obj : new JsonObject();
    }

    /**
     * Alias of {@link #object()} — see that method for full
     * documentation.
     *
     * @return the parsed object
     * @see #object()
     * @since 1.0.0
     */
    public JsonObject asObject() { return object(); }

    /**
     * Alias of {@link #object()} — see that method for full
     * documentation.
     *
     * @return the parsed object
     * @see #object()
     * @since 1.0.0
     */
    public JsonObject toObject() { return object(); }

    /**
     * Alias of {@link #object()} — see that method for full
     * documentation.
     *
     * @return the parsed object
     * @see #object()
     * @since 1.0.0
     */
    public JsonObject toJsonObject() { return object(); }

    // ============================================================== headers

    /**
     * Returns the first value for the given header, or {@code null} if
     * absent. Case-insensitive.
     *
     * @param name the header name (may be null → null)
     * @return the first value, or null
     * @see #headerAll(String)
     * @since 1.0.0
     */
    public String header(String name) {
        if (name == null || headers == null) return null;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                List<String> values = entry.getValue();
                return values != null && !values.isEmpty() ? values.get(0) : null;
            }
        }
        return null;
    }

    /**
     * Alias of {@link #header(String)} — see that method for full
     * documentation.
     *
     * @param name the header name
     * @return the first value, or null
     * @see #header(String)
     * @since 1.0.0
     */
    public String headerFirst(String name) { return header(name); }

    /**
     * Alias of {@link #header(String)} — see that method for full
     * documentation.
     *
     * @param name the header name
     * @return the first value, or null
     * @see #header(String)
     * @since 1.0.0
     */
    public String firstHeader(String name) { return header(name); }

    /**
     * Returns all values for the given header, or an empty list if
     * absent. Case-insensitive.
     *
     * @param name the header name (may be null → empty)
     * @return the values, or empty
     * @see #header(String)
     * @since 1.0.0
     */
    public List<String> headerAll(String name) {
        if (name == null || headers == null) return List.of();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue() == null ? List.of() : List.copyOf(entry.getValue());
            }
        }
        return List.of();
    }

    /**
     * Alias of {@link #headerAll(String)} — see that method for full
     * documentation.
     *
     * @param name the header name
     * @return the values
     * @see #headerAll(String)
     * @since 1.0.0
     */
    public List<String> headers(String name) { return headerAll(name); }

    /**
     * Returns the {@code Content-Type} header, or {@code null}.
     *
     * @return the content type
     * @since 1.0.0
     */
    public String contentType() { return header("Content-Type"); }

    /**
     * Returns the {@code Content-Length} header, or {@code null}.
     *
     * @return the content length
     * @since 1.0.0
     */
    public String contentLength() { return header("Content-Length"); }

    // ============================================================== timing

    /**
     * Returns the elapsed time in milliseconds. Zero for a null duration.
     *
     * @return the elapsed time
     * @see #elapsed()
     * @see #took()
     * @see #timeMs()
     * @since 1.0.0
     */
    public long durationMillis() { return duration == null ? 0 : duration.toMillis(); }

    /**
     * Alias of {@link #durationMillis()} — see that method for full
     * documentation.
     *
     * @return the elapsed time
     * @see #durationMillis()
     * @since 1.0.0
     */
    public long elapsed() { return durationMillis(); }

    /**
     * Alias of {@link #durationMillis()} — see that method for full
     * documentation.
     *
     * @return the elapsed time
     * @see #durationMillis()
     * @since 1.0.0
     */
    public long took() { return durationMillis(); }

    /**
     * Alias of {@link #durationMillis()} — see that method for full
     * documentation.
     *
     * @return the elapsed time
     * @see #durationMillis()
     * @since 1.0.0
     */
    public long timeMs() { return durationMillis(); }

    // ============================================================== display

    /**
     * Returns a short summary string — {@code "GET https://... => 200 OK (42ms, 1234 bytes)"}.
     *
     * @return the summary
     * @see #describe()
     * @see #info()
     * @since 1.0.0
     */
    public String summary() {
        return method + " " + url + " => " + status + " " + statusText
                + " (" + durationMillis() + "ms, " + text().length() + " bytes)";
    }

    /**
     * Alias of {@link #summary()} — see that method for full
     * documentation.
     *
     * @return the summary
     * @see #summary()
     * @since 1.0.0
     */
    public String describe() { return summary(); }

    /**
     * Alias of {@link #summary()} — see that method for full
     * documentation.
     *
     * @return the summary
     * @see #summary()
     * @since 1.0.0
     */
    public String info() { return summary(); }

    /**
     * Returns the summary — see {@link #summary()}.
     *
     * @return the summary
     * @since 1.0.0
     */
    @Override
    public String toString() { return summary(); }
}
