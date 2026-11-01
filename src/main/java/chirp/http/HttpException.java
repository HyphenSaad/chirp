package chirp.http;

/**
 * Thrown by {@link HttpRequest#sendOrThrow()} when a request fails.
 *
 * <p>The default {@link HttpRequest#send()} <b>never</b> throws — it
 * returns an {@link HttpResponse} whose {@link HttpResponse#isError()}
 * is {@code true}. This lets students make network calls without
 * writing a {@code try/catch}. {@code sendOrThrow} is for the cases
 * where failure must be an exception — e.g. inside a stream pipeline
 * that would otherwise silently drop failed elements.</p>
 *
 * <h2>Two kinds of failure</h2>
 * <p>{@code HttpException} distinguishes two cases because the caller
 * often needs to handle them differently:</p>
 * <ul>
 *   <li><b>Transport error</b> — the request never reached the server
 *       (DNS failure, connection refused, TLS handshake failed, timeout).
 *       {@link #status()} returns {@code -1} and
 *       {@link #isTransportError()} returns {@code true}.</li>
 *   <li><b>Status error</b> — the server responded with a 4xx or 5xx
 *       status. {@link #status()} returns the HTTP status code and
 *       {@link #isStatusError()} returns {@code true}.</li>
 * </ul>
 *
 * <h2>Example — retry on transport error only</h2>
 * <pre>{@code
 * for (int attempt = 0; attempt < 3; attempt++) {
 *     try {
 *         HttpResponse r = Http.get(url).sendOrThrow();
 *         return r.text();
 *     } catch (HttpException e) {
 *         if (e.isStatusError()) {
 *             throw e;   // 4xx/5xx — don't retry
 *         }
 *         // transport error — retry
 *     }
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the graceful (non-throwing) path, see
 * {@link HttpRequest#send()}. For the response record, see
 * {@link HttpResponse}.</p>
 *
 * @see HttpRequest#send()
 * @see HttpRequest#sendOrThrow()
 * @see HttpResponse
 * @since 1.0.0
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String body;

    /**
     * Constructs a transport-level exception with no status code.
     *
     * @param message the failure message
     * @since 1.0.0
     */
    public HttpException(String message) { this(message, -1, ""); }

    /**
     * Constructs a transport-level exception with a response body (rare
     * — usually there is no body when transport fails).
     *
     * @param message the failure message
     * @param body the body, if any
     * @since 1.0.0
     */
    public HttpException(String message, String body) { this(message, -1, body); }

    /**
     * Constructs an exception with an explicit status code and body.
     *
     * @param message the failure message
     * @param status the HTTP status, or -1 for transport errors
     * @param body the response body (may be empty)
     * @since 1.0.0
     */
    public HttpException(String message, int status, String body) {
        super(message);
        this.status = status;
        this.body = body == null ? "" : body;
    }

    /**
     * Constructs an exception wrapping a lower-level cause.
     *
     * @param message the failure message
     * @param cause the underlying exception
     * @since 1.0.0
     */
    public HttpException(String message, Throwable cause) {
        super(message, cause);
        this.status = -1;
        this.body = "";
    }

    /**
     * Returns the HTTP status code, or {@code -1} for transport errors.
     *
     * @return the status code
     * @see #statusCode()
     * @see #code()
     * @since 1.0.0
     */
    public int status() { return status; }

    /**
     * Alias of {@link #status()} — see that method for full documentation.
     *
     * @return the status code
     * @see #status()
     * @since 1.0.0
     */
    public int statusCode() { return status; }

    /**
     * Alias of {@link #status()} — see that method for full documentation.
     *
     * @return the status code
     * @see #status()
     * @since 1.0.0
     */
    public int code() { return status; }

    /**
     * Returns the response body, or empty string if there was none.
     *
     * @return the body
     * @see #response()
     * @see #raw()
     * @since 1.0.0
     */
    public String body() { return body; }

    /**
     * Alias of {@link #body()} — see that method for full documentation.
     *
     * @return the body
     * @see #body()
     * @since 1.0.0
     */
    public String response() { return body; }

    /**
     * Alias of {@link #body()} — see that method for full documentation.
     *
     * @return the body
     * @see #body()
     * @since 1.0.0
     */
    public String raw() { return body; }

    /**
     * Checks whether the failure was a transport-level error — the
     * request never reached the server.
     *
     * @return {@code true} if the failure was transport-level
     * @see #isStatusError()
     * @since 1.0.0
     */
    public boolean isTransportError() {
        return status == -1;
    }

    /**
     * Checks whether the failure was a status-level error — the server
     * responded with a 4xx or 5xx status.
     *
     * @return {@code true} if the failure was status-level
     * @see #isTransportError()
     * @since 1.0.0
     */
    public boolean isStatusError() {
        return status >= 400;
    }
}
