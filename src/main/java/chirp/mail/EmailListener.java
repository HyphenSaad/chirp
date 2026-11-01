package chirp.mail;

/**
 * Observer for the email send lifecycle.
 *
 * <p>Every method is a default no-op, so students implement only what they
 * need. Called on the thread that invoked {@code send()} — safe to touch UI
 * state without extra synchronisation. Exceptions thrown by the listener are
 * swallowed.</p>
 *
 * <p>Aliases and expanded hooks:</p>
 * <ul>
 *   <li>{@code onStart} — same as before</li>
 *   <li>{@code onComplete} — same as before</li>
 *   <li>{@code onError} — same as before</li>
 *   <li>{@code onConnect(host, port)} — fires once before the SMTP banner</li>
 *   <li>{@code onDisconnect()} — fires when the socket closes</li>
 *   <li>{@code onDelivered(result)} — fires when SMTP accepts the message</li>
 *   <li>{@code onBounce(result)} — fires when SMTP rejects permanently (5xx)</li>
 *   <li>{@code onRetry(attempt, cause)} — fires before each retry attempt</li>
 * </ul>
 */
public interface EmailListener {

    default void onStart(EmailRequest request) {}
    default void onSmtpEvent(String dialogLine) {}
    default void onComplete(EmailResult result) {}
    default void onError(Exception error) {}

    // ---------------------------------------------------------- expanded

    default void onConnect(String host, int port) {}
    default void onDisconnect() {}
    default void onDelivered(EmailResult result) {}
    default void onBounce(EmailResult result) {}
    default void onRetry(int attempt, Exception cause) {}
}
