package chirp.mail;

import java.util.List;

/**
 * Outcome of an email send attempt.
 *
 * <h2>Naming</h2>
 * <p>{@code success / isSuccess / isOk / succeeded / ok / good};
 * {@code isFailure / isFailed / failed / bad};
 * {@code message / summary / reason};
 * {@code durationMs / elapsed / took};
 * {@code rawMessage / mime / raw};
 * {@code recipients / to}; {@code smtpLog / log / dialog};
 * {@code hasLog / logged}.</p>
 *
 * <p>The record's field list is stable — <b>no</b> fields are added or removed
 * from earlier versions. New accessors are pure derivations.</p>
 */
public record EmailResult(
        boolean success,
        String message,
        long durationMs,
        String rawMessage,
        List<String> recipients,
        List<String> smtpLog) {

    // ============================================================== factories

    /** Constructs an error result. */
    public static EmailResult failure(String message) {
        return new EmailResult(false, message, 0L, "", List.of(), List.of());
    }
    public static EmailResult failed(String message) { return failure(message); }

    // ============================================================== status

    public boolean isSuccess() { return success; }
    public boolean isOk() { return success; }
    public boolean succeeded() { return success; }
    public boolean ok() { return success; }
    public boolean good() { return success; }

    public boolean isFailure() { return !success; }
    public boolean isFailed() { return !success; }
    public boolean failed() { return !success; }
    public boolean bad() { return !success; }

    // ============================================================== message

    public String summary() {
        return (success ? "OK" : "FAIL") + " — " + message
                + " (" + durationMs + "ms, " + recipients.size() + " recipients)";
    }
    public String reason() { return message; }

    // ============================================================== timing

    public long elapsed() { return durationMs; }
    public long took() { return durationMs; }
    public long timeMs() { return durationMs; }

    // ============================================================== raw MIME

    public String mime() { return rawMessage; }
    public String raw() { return rawMessage; }
    public String asMime() { return rawMessage; }
    public boolean hasMime() { return rawMessage != null && !rawMessage.isEmpty(); }

    // ============================================================== recipients

    public List<String> to() { return recipients; }
    public int recipientCount() { return recipients.size(); }

    public String firstRecipient() {
        return recipients.isEmpty() ? null : recipients.get(0);
    }

    // ============================================================== SMTP log

    public List<String> log() { return smtpLog; }
    public List<String> dialog() { return smtpLog; }
    public boolean hasLog() { return smtpLog != null && !smtpLog.isEmpty(); }
    public boolean logged() { return hasLog(); }
    public int smtpLineCount() { return smtpLog == null ? 0 : smtpLog.size(); }

    @Override
    public String toString() { return summary(); }
}
