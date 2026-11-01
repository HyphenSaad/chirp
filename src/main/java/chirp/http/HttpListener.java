package chirp.http;

/**
 * Observer for HTTP request lifecycle events.
 *
 * <p>Every method is default no-op, so students implement only what they need.
 * Called on the thread that invoked {@code send()} — safe to touch UI state
 * without extra synchronisation. Exceptions thrown by the listener are
 * swallowed.</p>
 */
public interface HttpListener {

    /**
     * Called at the start of every {@code send()}, before the network is touched —
     * also when the request then fails validation (e.g. a missing or invalid URL).
     */
    default void onStart(HttpRequest request) {}

    /** Called after a response arrives, whether success or error. */
    default void onComplete(HttpResponse response) {}

    /** Called when the response status is 2xx. */
    default void onSuccess(HttpResponse response) {}

    /** Called when the response status is not 2xx or a transport error occurred. */
    default void onFailure(HttpRequest request, Throwable error) {}

    /**
     * Called before each retry attempt (when using {@code sendWithRetry}) — so
     * {@code N} attempts give at most {@code N - 1} calls. {@code attempt} is the
     * 1-based number of the attempt that just failed; {@code cause} describes why.
     */
    default void onRetry(HttpRequest request, int attempt, Throwable cause) {}

    /** Called when the response is a 3xx redirect (before following). */
    default void onRedirect(HttpResponse response) {}
}
