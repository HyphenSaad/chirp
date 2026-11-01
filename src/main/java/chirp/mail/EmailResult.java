package chirp.mail;

import java.util.List;

/**
 * Outcome of an email send attempt.
 *
 * <p>A {@code record} carrying the six pieces of information every caller
 * needs after {@link EmailRequest#send()}: whether the send succeeded,
 * what happened, how long it took, the raw MIME message that was sent,
 * who the recipients were, and — when verbose mode is on — the full SMTP
 * dialogue.</p>
 *
 * <h2>The three pieces you'll actually use</h2>
 * <ul>
 *   <li>{@link #isSuccess()} — did it work?</li>
 *   <li>{@link #message()} — if not, why?</li>
 *   <li>{@link #durationMs()} — how long did it take?</li>
 * </ul>
 *
 * <h2>The other three — for debugging and logging</h2>
 * <ul>
 *   <li>{@link #rawMessage()} — the full MIME message (headers + body +
 *       attachments). Useful for logging what was actually sent.</li>
 *   <li>{@link #recipients()} — the resolved recipient list.</li>
 *   <li>{@link #smtpLog()} — the SMTP dialogue, when verbose was on.
 *       Empty otherwise.</li>
 * </ul>
 *
 * <h2>Immutability</h2>
 * <p>The two list fields are defensively copied in the canonical
 * constructor. The result is fully immutable — safe to pass to a
 * {@link chirp.mail.EmailListener} without fear that later code will
 * mutate the lists.</p>
 *
 * <h2>Example — the standard error-handling flow</h2>
 * <pre>{@code
 * EmailResult r = Email.builder()
 *     .host("smtp.gmail.com")
 *     .from("me@gmail.com").to("you@example.com")
 *     .subject("Hello").text("Hi!")
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")
 *     .send();
 *
 * if (r.isSuccess()) {
 *     println("Sent in " + r.durationMs() + "ms to " + r.recipients());
 * } else {
 *     println("Failed: " + r.message());
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the send methods that produce this record, see
 * {@link EmailRequest#send()} and {@link Email#send(Object...)}. For
 * the throw-only variant, see {@link EmailException}.</p>
 *
 * @param success whether the send succeeded
 * @param message a human-readable summary ("Sent to 2 recipients",
 * "Cannot reach SMTP server", etc.)
 * @param durationMs how long the send took, in milliseconds
 * @param rawMessage the raw MIME message that was sent (empty for
 * pre-validation failures and dry runs)
 * @param recipients the resolved recipient addresses (empty for
 * pre-configuration failures)
 * @param smtpLog the full SMTP dialogue lines (empty unless verbose)
 * @see EmailRequest#send()
 * @see EmailRequest#sendOrThrow()
 * @see EmailException
 * @since 1.0.0
 */
public record EmailResult(
        boolean success,
        String message,
        long durationMs,
        String rawMessage,
        List<String> recipients,
        List<String> smtpLog) {

    public EmailResult {
        recipients = recipients == null ? List.of() : List.copyOf(recipients);
        smtpLog = smtpLog == null ? List.of() : List.copyOf(smtpLog);
    }

    // ============================================================== factories

    /**
     * Constructs an error result with the given message. No timing, no
     * MIME, no recipients.
     *
     * <p>Useful for early-validation failures where none of the
     * information is available yet.</p>
     *
     * @param message the failure message
     * @return a new failure result
     * @see #failed(String)
     * @since 1.0.0
     */
    public static EmailResult failure(String message) {
        return new EmailResult(false, message, 0L, "", List.of(), List.of());
    }

    /**
     * Alias of {@link #failure(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return a new failure result
     * @see #failure(String)
     * @since 1.0.0
     */
    public static EmailResult failed(String message) { return failure(message); }

    // ============================================================== status

    /**
     * Checks whether the send succeeded.
     *
     * @return {@code true} on success
     * @see #isFailure()
     * @since 1.0.0
     */
    public boolean isSuccess() { return success; }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} on success
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean isOk() { return success; }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} on success
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean succeeded() { return success; }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} on success
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean ok() { return success; }

    /**
     * Alias of {@link #isSuccess()} — see that method for full
     * documentation.
     *
     * @return {@code true} on success
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean good() { return success; }

    /**
     * Checks whether the send failed.
     *
     * @return {@code true} on failure
     * @see #isSuccess()
     * @since 1.0.0
     */
    public boolean isFailure() { return !success; }

    /**
     * Alias of {@link #isFailure()} — see that method for full
     * documentation.
     *
     * @return {@code true} on failure
     * @see #isFailure()
     * @since 1.0.0
     */
    public boolean isFailed() { return !success; }

    /**
     * Alias of {@link #isFailure()} — see that method for full
     * documentation.
     *
     * @return {@code true} on failure
     * @see #isFailure()
     * @since 1.0.0
     */
    public boolean failed() { return !success; }

    /**
     * Alias of {@link #isFailure()} — see that method for full
     * documentation.
     *
     * @return {@code true} on failure
     * @see #isFailure()
     * @since 1.0.0
     */
    public boolean bad() { return !success; }

    // ============================================================== message

    /**
     * Returns a short summary — {@code "OK — Sent to 2 recipients (42ms, 2 recipients)"}.
     *
     * @return the summary
     * @see #reason()
     * @since 1.0.0
     */
    public String summary() {
        return (success ? "OK" : "FAIL") + " — " + message
                + " (" + durationMs + "ms, " + recipients.size() + " recipients)";
    }

    /**
     * Alias of {@link #message()} — the record component itself.
     *
     * @return the message
     * @see #message()
     * @since 1.0.0
     */
    public String reason() { return message; }

    // ============================================================== timing

    /**
     * Alias of {@link #durationMs()} — see that component for full
     * documentation.
     *
     * @return the elapsed time in milliseconds
     * @see #durationMs()
     * @since 1.0.0
     */
    public long elapsed() { return durationMs; }

    /**
     * Alias of {@link #durationMs()} — see that component for full
     * documentation.
     *
     * @return the elapsed time in milliseconds
     * @see #durationMs()
     * @since 1.0.0
     */
    public long took() { return durationMs; }

    /**
     * Alias of {@link #durationMs()} — see that component for full
     * documentation.
     *
     * @return the elapsed time in milliseconds
     * @see #durationMs()
     * @since 1.0.0
     */
    public long timeMs() { return durationMs; }

    // ============================================================== raw MIME

    /**
     * Alias of {@link #rawMessage()} — see that component for full
     * documentation.
     *
     * @return the MIME message
     * @see #rawMessage()
     * @since 1.0.0
     */
    public String mime() { return rawMessage; }

    /**
     * Alias of {@link #rawMessage()} — see that component for full
     * documentation.
     *
     * @return the MIME message
     * @see #rawMessage()
     * @since 1.0.0
     */
    public String raw() { return rawMessage; }

    /**
     * Alias of {@link #rawMessage()} — see that component for full
     * documentation.
     *
     * @return the MIME message
     * @see #rawMessage()
     * @since 1.0.0
     */
    public String asMime() { return rawMessage; }

    /**
     * Checks whether the result carries a MIME message — true when the
     * send reached the MIME-building stage, whether or not it then
     * succeeded.
     *
     * @return {@code true} if there is a non-empty raw message
     * @since 1.0.0
     */
    public boolean hasMime() { return rawMessage != null && !rawMessage.isEmpty(); }

    // ============================================================== recipients

    /**
     * Alias of {@link #recipients()} — see that component for full
     * documentation.
     *
     * @return the recipients
     * @see #recipients()
     * @since 1.0.0
     */
    public List<String> to() { return recipients; }

    /**
     * Returns the number of recipients.
     *
     * @return the recipient count
     * @since 1.0.0
     */
    public int recipientCount() { return recipients.size(); }

    /**
     * Returns the first recipient, or {@code null} if the list is empty.
     *
     * @return the first recipient, or null
     * @since 1.0.0
     */
    public String firstRecipient() {
        return recipients.isEmpty() ? null : recipients.get(0);
    }

    // ============================================================== SMTP log

    /**
     * Alias of {@link #smtpLog()} — see that component for full
     * documentation.
     *
     * @return the SMTP dialogue
     * @see #smtpLog()
     * @since 1.0.0
     */
    public List<String> log() { return smtpLog; }

    /**
     * Alias of {@link #smtpLog()} — see that component for full
     * documentation.
     *
     * @return the SMTP dialogue
     * @see #smtpLog()
     * @since 1.0.0
     */
    public List<String> dialog() { return smtpLog; }

    /**
     * Checks whether the result carries a non-empty SMTP dialogue.
     *
     * @return {@code true} if there is at least one logged line
     * @since 1.0.0
     */
    public boolean hasLog() { return smtpLog != null && !smtpLog.isEmpty(); }

    /**
     * Alias of {@link #hasLog()} — see that method for full
     * documentation.
     *
     * @return {@code true} if there is at least one logged line
     * @see #hasLog()
     * @since 1.0.0
     */
    public boolean logged() { return hasLog(); }

    /**
     * Returns the number of SMTP dialogue lines logged.
     *
     * @return the line count
     * @since 1.0.0
     */
    public int smtpLineCount() { return smtpLog == null ? 0 : smtpLog.size(); }

    /**
     * Returns the summary — see {@link #summary()}.
     *
     * @return the summary
     * @since 1.0.0
     */
    @Override
    public String toString() { return summary(); }
}
