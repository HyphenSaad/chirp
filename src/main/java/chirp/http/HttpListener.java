package chirp.http;

/**
 * Observer for the HTTP request lifecycle.
 *
 * <p>Every method is a default no-op, so a student implements only the
 * hooks they need. All callbacks run on the thread that invoked
 * {@code send()} — safe to touch UI state from JavaFX event handlers
 * without extra synchronisation.</p>
 *
 * <h2>Listener exceptions are swallowed</h2>
 * <p>A listener that throws does not break the request — the exception
 * is logged via {@link chirp.internal.Trace#warn(String, String)} when
 * tracing is on, and the request continues. This is deliberate: a broken
 * UI listener should not prevent the network call from completing.</p>
 *
 * <h2>The lifecycle</h2>
 * <p>A successful request triggers, in order:</p>
 * <ol>
 *   <li>{@link #onStart(HttpRequest)} — once, at the start</li>
 *   <li>{@link #onRetry(HttpRequest, int, Throwable)} — zero or more
 *       times, only on transport retry (see
 *       {@link HttpRequest#sendWithRetry(int)})</li>
 *   <li>{@link #onSuccess(HttpResponse)} — once, on a 2xx response</li>
 *   <li>{@link #onComplete(HttpResponse)} — once, on any response</li>
 * </ol>
 * <p>A failed request triggers:</p>
 * <ol>
 *   <li>{@code onStart}</li>
 *   <li>zero or more {@code onRetry}</li>
 *   <li>{@link #onFailure(HttpRequest, Throwable)} — once, on any error</li>
 *   <li>{@code onComplete}</li>
 * </ol>
 * <p>Redirects trigger {@link #onRedirect(HttpResponse)} between
 * {@code onStart} and the final {@code onComplete}.</p>
 *
 * <h2>Example — a logging listener</h2>
 * <pre>{@code
 * Http.get(url)
 *     .listener(new HttpListener() {
 *         @Override
 *         public void onStart(HttpRequest r) {
 *             println("→ GET " + r.url());
 *         }
 *         @Override
 *         public void onSuccess(HttpResponse r) {
 *             println("← " + r.status() + " (" + r.durationMillis() + "ms)");
 *         }
 *         @Override
 *         public void onFailure(HttpRequest r, Throwable e) {
 *             println("✗ " + e.getMessage());
 *         }
 *     })
 *     .send();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the request builder that accepts listeners, see
 * {@link HttpRequest#listener(HttpListener)}.</p>
 *
 * @see HttpRequest
 * @see HttpResponse
 * @since 1.0.0
 */
public interface HttpListener {

    /**
     * Called at the start of every {@code send()}, before the network is
     * touched — including when the request then fails validation (e.g.
     * missing URL).
     *
     * @param request the request being sent
     * @since 1.0.0
     */
    default void onStart(HttpRequest request) {}

    /**
     * Called after a response arrives, whether success or error.
     *
     * @param response the response (may be an error response)
     * @since 1.0.0
     */
    default void onComplete(HttpResponse response) {}

    /**
     * Called when the response status is 2xx.
     *
     * @param response the successful response
     * @since 1.0.0
     */
    default void onSuccess(HttpResponse response) {}

    /**
     * Called when the response status is not 2xx, or a transport error
     * occurred.
     *
     * @param request the request that failed
     * @param error the underlying error (may be null for status errors —
     * the response carries the status)
     * @since 1.0.0
     */
    default void onFailure(HttpRequest request, Throwable error) {}

    /**
     * Called before each retry attempt in
     * {@link HttpRequest#sendWithRetry(int)}. N attempts give at most
     * {@code N - 1} calls.
     *
     * @param request the request being retried
     * @param attempt the 1-based number of the attempt that just failed
     * @param cause the reason the previous attempt failed
     * @since 1.0.0
     */
    default void onRetry(HttpRequest request, int attempt, Throwable cause) {}

    /**
     * Called when the response is a 3xx redirect, before any further
     * redirects are followed. This fires once per redirect hop.
     *
     * @param response the redirect response
     * @since 1.0.0
     */
    default void onRedirect(HttpResponse response) {}
}
