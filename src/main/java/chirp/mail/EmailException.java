package chirp.mail;

/**
 * Thrown by {@link EmailRequest#sendOrThrow()} and other mail operations
 * when a request fails.
 *
 * <h2>Naming</h2>
 * <p>{@code reason / category / cause}; {@code isTransportError / isNetworkError};
 * {@code isAuthError / isAuthenticationError / isCredentialsError};
 * {@code isValidationError / isInvalid};
 * {@code isMissingConfig / isMissingConfiguration}.</p>
 *
 * <h2>Factories</h2>
 * <p>{@code transport(msg, cause)}, {@code auth(msg)},
 * {@code validation(msg)}, {@code missingConfig(msg)} — each sets the
 * appropriate {@link Reason} so callers can branch on failure kind.</p>
 */
public class EmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Failure category — lets callers branch without parsing messages. */
    public enum Reason {
        TRANSPORT,       // network / SMTP connectivity issue
        AUTH,            // bad credentials or authentication rejected
        VALIDATION,      // invalid input data (bad email, bad address, …)
        MISSING_CONFIG,  // required field not set (host, from, recipient, …)
        UNKNOWN          // default fallback
    }

    private final Reason reason;

    public EmailException(String message) {
        this(Reason.UNKNOWN, message, null);
    }
    public EmailException(String message, Throwable cause) {
        this(Reason.UNKNOWN, message, cause);
    }
    public EmailException(Reason reason, String message) {
        this(reason, message, null);
    }
    public EmailException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason == null ? Reason.UNKNOWN : reason;
    }

    // ============================================================== factories

    public static EmailException transport(String message, Throwable cause) {
        return new EmailException(Reason.TRANSPORT, message, cause);
    }
    public static EmailException auth(String message) {
        return new EmailException(Reason.AUTH, message);
    }
    public static EmailException validation(String message) {
        return new EmailException(Reason.VALIDATION, message);
    }
    public static EmailException missingConfig(String message) {
        return new EmailException(Reason.MISSING_CONFIG, message);
    }

    // ============================================================== accessors

    public Reason reason() { return reason; }
    public Reason category() { return reason; }
    public Reason getReason() { return reason; }

    public boolean isTransportError() { return reason == Reason.TRANSPORT; }
    public boolean isNetworkError() { return isTransportError(); }

    public boolean isAuthError() { return reason == Reason.AUTH; }
    public boolean isAuthenticationError() { return isAuthError(); }
    public boolean isCredentialsError() { return isAuthError(); }

    public boolean isValidationError() { return reason == Reason.VALIDATION; }
    public boolean isInvalid() { return isValidationError(); }

    public boolean isMissingConfig() { return reason == Reason.MISSING_CONFIG; }
    public boolean isMissingConfiguration() { return isMissingConfig(); }
}
