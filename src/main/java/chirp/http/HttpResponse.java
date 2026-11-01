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
 * <h2>Naming</h2>
 * <p>{@code text / body / raw / content / data}; {@code json / asJson / toJson};
 * {@code object / asObject / toObject / toJsonObject};
 * {@code isSuccess / isOk / succeeded / ok / good};
 * {@code isError / isFailed / failed / bad};
 * {@code isClientError / is4xx}; {@code isServerError / is5xx};
 * {@code hasTransportError / isTransportError};
 * {@code header / headerFirst / firstHeader}; {@code headerAll / headers};
 * {@code durationMillis / elapsed / took / timeMs};
 * {@code summary / describe / info}.</p>
 *
 * <p><b>Encoding:</b> {@link #byteLength()} encodes with {@link StandardCharsets#UTF_8}
 * to match the charset used by {@code HttpRequest.send()} when building the
 * request body. Relying on the platform default would report different lengths
 * on machines with a non-UTF-8 default charset.</p>
 *
 * <p><b>Immutability:</b> the {@code headers} map and its inner lists are
 * defensively copied in the canonical constructor, so callers cannot mutate
 * the response by retaining a reference to the map they passed in. Iteration
 * order is preserved (the JDK's HTTP client uses an insertion-ordered map).</p>
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

    /** Constructs an error response for a failed call. */
    public static HttpResponse error(HttpMethod method, String url,
            Duration duration, String message) {
        return new HttpResponse(method, url, -1,
                message == null ? "Network error" : message,
                "", Collections.emptyMap(), duration);
    }
    public static HttpResponse failed(HttpMethod method, String url,
            Duration duration, String message) {
        return error(method, url, duration, message);
    }

    // ============================================================== status

    public boolean isSuccess() { return status >= 200 && status < 300; }
    public boolean isOk() { return isSuccess(); }
    public boolean is2xx() { return isSuccess(); }
    public boolean succeeded() { return isSuccess(); }
    public boolean ok() { return isSuccess(); }
    public boolean good() { return isSuccess(); }

    public boolean isError() { return status < 200 || status >= 400; }
    public boolean isFailed() { return isError(); }
    public boolean failed() { return isError(); }
    public boolean bad() { return isError(); }

    public boolean isClientError() { return status >= 400 && status < 500; }
    public boolean is4xx() { return isClientError(); }
    public boolean isServerError() { return status >= 500; }
    public boolean is5xx() { return isServerError(); }
    public boolean isRedirect() { return status >= 300 && status < 400; }
    public boolean is3xx() { return isRedirect(); }

    public boolean hasTransportError() { return status == -1; }
    public boolean isTransportError() { return hasTransportError(); }

    public int statusCode() { return status; }
    public int code() { return status; }

    // ============================================================== body

    public String text() { return body == null ? "" : body; }
    public String body() { return text(); }
    public String raw() { return text(); }
    public String content() { return text(); }
    public String data() { return text(); }

    public boolean hasBody() { return !text().isEmpty(); }
    public boolean notEmpty() { return hasBody(); }
    public int length() { return text().length(); }
    public int byteLength() { return text().getBytes(StandardCharsets.UTF_8).length; }

    // ============================================================== JSON

    public JsonValue json() {
        if (body == null || body.isBlank()) return JsonNull.INSTANCE;
        try {
            return Json.parse(body);
        } catch (RuntimeException e) {
            return JsonNull.INSTANCE;
        }
    }
    public JsonValue asJson() { return json(); }
    public JsonValue toJson() { return json(); }

    public JsonObject object() {
        JsonValue value = json();
        return value instanceof JsonObject obj ? obj : new JsonObject();
    }
    public JsonObject asObject() { return object(); }
    public JsonObject toObject() { return object(); }
    public JsonObject toJsonObject() { return object(); }

    // ============================================================== headers

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
    public String headerFirst(String name) { return header(name); }
    public String firstHeader(String name) { return header(name); }

    public List<String> headerAll(String name) {
        if (name == null || headers == null) return List.of();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue() == null ? List.of() : List.copyOf(entry.getValue());
            }
        }
        return List.of();
    }
    public List<String> headers(String name) { return headerAll(name); }

    public String contentType() { return header("Content-Type"); }
    public String contentLength() { return header("Content-Length"); }

    // ============================================================== timing

    public long durationMillis() { return duration == null ? 0 : duration.toMillis(); }
    public long elapsed() { return durationMillis(); }
    public long took() { return durationMillis(); }
    public long timeMs() { return durationMillis(); }

    // ============================================================== display

    public String summary() {
        return method + " " + url + " => " + status + " " + statusText
                + " (" + durationMillis() + "ms, " + text().length() + " bytes)";
    }
    public String describe() { return summary(); }
    public String info() { return summary(); }

    @Override
    public String toString() { return summary(); }
}
