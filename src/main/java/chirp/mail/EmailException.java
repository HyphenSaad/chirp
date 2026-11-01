package chirp.mail;

/**
 * Thrown by {@link EmailRequest#sendOrThrow()} and other mail operations
 * when a send fails.
 *
 * <p>The exception carries a {@link Reason} that lets callers branch on
 * failure kind without parsing the message text. This is essential for
 * retry logic — a {@code TRANSPORT} error is often worth retrying; an
 * {@code AUTH} error never is (the credentials will still be wrong on
 * the next attempt); a {@code VALIDATION} error indicates a bug in the
 * caller, not a transient condition.</p>
 *
 * <h2>The five reasons</h2>
 * <table border="1">
 *   <caption>Failure categories</caption>
 *   <tr><th>Reason</th><th>Meaning</th><th>Retry?</th></tr>
 *   <tr><td>{@link Reason#TRANSPORT}</td>
 *       <td>Network or SMTP connectivity — cannot reach host, TLS
 *           handshake failed, timeout</td>
 *       <td>Often yes</td></tr>
 *   <tr><td>{@link Reason#AUTH}</td>
 *       <td>Credentials rejected by the server</td>
 *       <td>No</td></tr>
 *   <tr><td>{@link Reason#VALIDATION}</td>
 *       <td>Bad input (invalid email, missing attachment file, etc.)</td>
 *       <td>No — fix the input</td></tr>
 *   <tr><td>{@link Reason#MISSING_CONFIG}</td>
 *       <td>A required field was not set (host, from, body, password)</td>
 *       <td>No — fix the configuration</td></tr>
 *   <tr><td>{@link Reason#UNKNOWN}</td>
 *       <td>Uncategorised failure</td>
 *       <td>Depends</td></tr>
 * </table>
 *
 * <h2>Factory methods</h2>
 * <p>Prefer the factory methods over the raw constructor when the
 * category is known:</p>
 * <ul>
 *   <li>{@link #transport(String, Throwable)}</li>
 *   <li>{@link #auth(String)}</li>
 *   <li>{@link #validation(String)}</li>
 *   <li>{@link #missingConfig(String)}</li>
 * </ul>
 *
 * <h2>Example — retry only on transport errors</h2>
 * <pre>{@code
 * for (int attempt = 0; attempt < 3; attempt++) {
 *     try {
 *         Email.builder()...sendOrThrow();
 *         break;
 *     } catch (EmailException e) {
 *         if (!e.isTransportError()) throw e;
 *         // transport error — retry
 *     }
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the graceful (non-throwing) path, see
 * {@link EmailRequest#send()}. For the outcome record, see
 * {@link EmailResult}.</p>
 *
 * @see EmailRequest#send()
 * @see EmailRequest#sendOrThrow()
 * @see EmailResult
 * @since 1.0.0
 */
public class EmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Failure category — lets callers branch without parsing the
     * message.
     *
     * @since 1.0.0
     */
    public enum Reason {
        /** Network or SMTP connectivity issue. */
        TRANSPORT,
        /** Bad credentials or authentication rejected. */
        AUTH,
        /** Invalid input data (bad email, bad address, missing file, …). */
        VALIDATION,
        /** Required field not set (host, from, recipient, body, …). */
        MISSING_CONFIG,
        /** Default fallback for uncategorised failures. */
        UNKNOWN
    }

    private final Reason reason;

    /**
     * Constructs an exception with {@link Reason#UNKNOWN}.
     *
     * @param message the failure message
     * @since 1.0.0
     */
    public EmailException(String message) {
        this(Reason.UNKNOWN, message, null);
    }

    /**
     * Constructs an exception with {@link Reason#UNKNOWN}, wrapping a
     * cause.
     *
     * @param message the failure message
     * @param cause the underlying exception
     * @since 1.0.0
     */
    public EmailException(String message, Throwable cause) {
        this(Reason.UNKNOWN, message, cause);
    }

    /**
     * Constructs an exception with an explicit reason.
     *
     * @param reason the failure category (null → UNKNOWN)
     * @param message the failure message
     * @since 1.0.0
     */
    public EmailException(Reason reason, String message) {
        this(reason, message, null);
    }

    /**
     * Constructs an exception with an explicit reason, wrapping a cause.
     *
     * @param reason the failure category (null → UNKNOWN)
     * @param message the failure message
     * @param cause the underlying exception
     * @since 1.0.0
     */
    public EmailException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason == null ? Reason.UNKNOWN : reason;
    }

    // ============================================================== factories

    /**
     * Factory — a {@link Reason#TRANSPORT} failure.
     *
     * @param message the failure message
     * @param cause the underlying exception (may be null)
     * @return a new exception with reason TRANSPORT
     * @since 1.0.0
     */
    public static EmailException transport(String message, Throwable cause) {
        return new EmailException(Reason.TRANSPORT, message, cause);
    }

    /**
     * Factory — an {@link Reason#AUTH} failure.
     *
     * @param message the failure message
     * @return a new exception with reason AUTH
     * @since 1.0.0
     */
    public static EmailException auth(String message) {
        return new EmailException(Reason.AUTH, message);
    }

    /**
     * Factory — a {@link Reason#VALIDATION} failure.
     *
     * @param message the failure message
     * @return a new exception with reason VALIDATION
     * @since 1.0.0
     */
    public static EmailException validation(String message) {
        return new EmailException(Reason.VALIDATION, message);
    }

    /**
     * Factory — a {@link Reason#MISSING_CONFIG} failure.
     *
     * @param message the failure message
     * @return a new exception with reason MISSING_CONFIG
     * @since 1.0.0
     */
    public static EmailException missingConfig(String message) {
        return new EmailException(Reason.MISSING_CONFIG, message);
    }

    // ============================================================== accessors

    /**
     * Returns the failure category.
     *
     * @return the reason
     * @see #category()
     * @see #getReason()
     * @since 1.0.0
     */
    public Reason reason() { return reason; }

    /**
     * Alias of {@link #reason()} — see that method for full
     * documentation.
     *
     * @return the reason
     * @see #reason()
     * @since 1.0.0
     */
    public Reason category() { return reason; }

    /**
     * Alias of {@link #reason()} — see that method for full
     * documentation.
     *
     * @return the reason
     * @see #reason()
     * @since 1.0.0
     */
    public Reason getReason() { return reason; }

    /**
     * Checks whether this was a transport failure.
     *
     * @return {@code true} if reason is TRANSPORT
     * @see #isNetworkError()
     * @since 1.0.0
     */
    public boolean isTransportError() { return reason == Reason.TRANSPORT; }

    /**
     * Alias of {@link #isTransportError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if reason is TRANSPORT
     * @see #isTransportError()
     * @since 1.0.0
     */
    public boolean isNetworkError() { return isTransportError(); }

    /**
     * Checks whether this was an authentication failure.
     *
     * @return {@code true} if reason is AUTH
     * @see #isAuthenticationError()
     * @see #isCredentialsError()
     * @since 1.0.0
     */
    public boolean isAuthError() { return reason == Reason.AUTH; }

    /**
     * Alias of {@link #isAuthError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if reason is AUTH
     * @see #isAuthError()
     * @since 1.0.0
     */
    public boolean isAuthenticationError() { return isAuthError(); }

    /**
     * Alias of {@link #isAuthError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if reason is AUTH
     * @see #isAuthError()
     * @since 1.0.0
     */
    public boolean isCredentialsError() { return isAuthError(); }

    /**
     * Checks whether this was a validation failure.
     *
     * @return {@code true} if reason is VALIDATION
     * @see #isInvalid()
     * @since 1.0.0
     */
    public boolean isValidationError() { return reason == Reason.VALIDATION; }

    /**
     * Alias of {@link #isValidationError()} — see that method for full
     * documentation.
     *
     * @return {@code true} if reason is VALIDATION
     * @see #isValidationError()
     * @since 1.0.0
     */
    public boolean isInvalid() { return isValidationError(); }

    /**
     * Checks whether this was a missing-configuration failure.
     *
     * @return {@code true} if reason is MISSING_CONFIG
     * @see #isMissingConfiguration()
     * @since 1.0.0
     */
    public boolean isMissingConfig() { return reason == Reason.MISSING_CONFIG; }

    /**
     * Alias of {@link #isMissingConfig()} — see that method for full
     * documentation.
     *
     * @return {@code true} if reason is MISSING_CONFIG
     * @see #isMissingConfig()
     * @since 1.0.0
     */
    public boolean isMissingConfiguration() { return isMissingConfig(); }
}
