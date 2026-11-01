package chirp.http;

/**
 * Thrown by {@link HttpRequest#sendOrThrow()} when a request fails.
 *
 * <p>The graceful {@link HttpRequest#send()} never throws — it returns a
 * response whose {@link HttpResponse#isError()} is {@code true}.</p>
 *
 * <p>Aliases: {@code status / statusCode / code}; {@code body / response / raw}.</p>
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String body;

    public HttpException(String message) { this(message, -1, ""); }
    public HttpException(String message, String body) { this(message, -1, body); }
    public HttpException(String message, int status, String body) {
        super(message);
        this.status = status;
        this.body = body == null ? "" : body;
    }
    public HttpException(String message, Throwable cause) {
        super(message, cause);
        this.status = -1;
        this.body = "";
    }

    public int status() { return status; }
    public int statusCode() { return status; }
    public int code() { return status; }

    public String body() { return body; }
    public String response() { return body; }
    public String raw() { return body; }

    /** True when the failure was a transport-level error (never reached server). */
    public boolean isTransportError() {
        return status == -1;
    }

    /** True when the failure was a status-level error (server responded with 4xx/5xx). */
    public boolean isStatusError() {
        return status >= 400;
    }
}
