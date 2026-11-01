package chirp.mail;

/**
 * Observer for the email-send lifecycle.
 *
 * <p>Every method is a default no-op, so a student implements only the
 * hooks they need. All callbacks run on the thread that invoked
 * {@code send()} — safe to touch UI state from JavaFX event handlers
 * without extra synchronisation.</p>
 *
 * <h2>Listener exceptions are swallowed</h2>
 * <p>A listener that throws does not break the send. The exception is
 * recorded — for the SMTP dialogue listener — in the
 * {@link EmailResult#smtpLog()} so the failure surfaces without
 * preventing the send from completing. For the other hooks, the
 * exception is logged via {@link chirp.internal.Trace} when tracing is
 * enabled, and the send continues.</p>
 *
 * <h2>The lifecycle</h2>
 * <p>A successful send triggers, in order:</p>
 * <ol>
 *   <li>{@link #onStart(EmailRequest)} — once, after validation</li>
 *   <li>{@link #onConnect(String, int)} — once, before the SMTP banner</li>
 *   <li>{@link #onSmtpEvent(String)} — once per SMTP dialogue line,
 *       only when verbose is on</li>
 *   <li>{@link #onDelivered(EmailResult)} — once, on SMTP acceptance</li>
 *   <li>{@link #onDisconnect()} — once, when the socket closes</li>
 *   <li>{@link #onComplete(EmailResult)} — once, at the end</li>
 * </ol>
 * <p>A failed send triggers:</p>
 * <ol>
 *   <li>{@code onStart}</li>
 *   <li>zero or more {@code onSmtpEvent}</li>
 *   <li>{@link #onError(Exception)} — once, with the failure</li>
 *   <li>{@code onComplete}</li>
 * </ol>
 * <p>On a permanent SMTP rejection (5xx status), {@link #onBounce(EmailResult)}
 * fires in addition to {@code onError}. On a retryable failure
 * (transport or 4xx), {@link #onRetry(int, Exception)} fires before
 * each retry.</p>
 *
 * <h2>Example — a logger</h2>
 * <pre>{@code
 * Email.builder()
 *     .host("smtp.gmail.com")
 *     // ... rest of the config ...
 *     .listener(new EmailListener() {
 *         @Override
 *         public void onStart(EmailRequest r) {
 *             println("→ Sending to " + r.to().size() + " recipients");
 *         }
 *         @Override
 *         public void onDelivered(EmailResult r) {
 *             println("✓ Delivered in " + r.durationMs() + "ms");
 *         }
 *         @Override
 *         public void onError(Exception e) {
 *             println("✗ " + e.getMessage());
 *         }
 *     })
 *     .send();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the request builder that accepts listeners, see
 * {@link EmailRequest#listener(EmailListener)}.</p>
 *
 * @see EmailRequest
 * @see EmailResult
 * @since 1.0.0
 */
public interface EmailListener {

    /**
     * Called at the start of every send, after validation succeeds.
     *
     * @param request the request being sent
     * @since 1.0.0
     */
    default void onStart(EmailRequest request) {}

    /**
     * Called once per SMTP dialogue line, only when verbose is on. Useful
     * for showing the raw conversation in a debug pane.
     *
     * @param dialogLine the SMTP line (client or server)
     * @since 1.0.0
     */
    default void onSmtpEvent(String dialogLine) {}

    /**
     * Called at the end of every send, whether success or failure.
     *
     * @param result the outcome
     * @since 1.0.0
     */
    default void onComplete(EmailResult result) {}

    /**
     * Called when the send fails.
     *
     * @param error the underlying exception
     * @since 1.0.0
     */
    default void onError(Exception error) {}

    // ---------------------------------------------------------- expanded

    /**
     * Called once, before the SMTP connection opens.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @since 1.0.0
     */
    default void onConnect(String host, int port) {}

    /**
     * Called when the socket closes, whatever the outcome.
     *
     * @since 1.0.0
     */
    default void onDisconnect() {}

    /**
     * Called when SMTP accepts the message — the last moment before the
     * connection closes.
     *
     * @param result the successful outcome
     * @since 1.0.0
     */
    default void onDelivered(EmailResult result) {}

    /**
     * Called when SMTP rejects the message permanently (5xx response).
     * This is more specific than {@link #onError(Exception)} — a bounce
     * means the server responded, but rejected.
     *
     * @param result the failure outcome
     * @since 1.0.0
     */
    default void onBounce(EmailResult result) {}

    /**
     * Called before each retry attempt in
     * {@link EmailRequest#sendWithRetry(int)}. N attempts give at most
     * {@code N - 1} calls.
     *
     * @param attempt the 1-based number of the attempt that just failed
     * @param cause the reason the previous attempt failed
     * @since 1.0.0
     */
    default void onRetry(int attempt, Exception cause) {}
}
