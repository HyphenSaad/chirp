package chirp.mail;

/**
 * Where the SMTP password comes from.
 *
 * <p><b>You must choose a strategy.</b> {@code ENV} is the default — but you
 * must still specify the {@code .env} key. Direct passwords are supported
 * for quick tests but produce a warning.</p>
 */
public enum PasswordStrategy {
    /** Provided directly at send time. Discouraged — emits a warning. */
    DIRECT,
    /** Prompted once at send time — read from stdin, no echo if possible. */
    PROMPT,
    /** Read from a {@code .env} file. Recommended. Requires ENV_FILE and ENV_KEY. */
    ENV
}
