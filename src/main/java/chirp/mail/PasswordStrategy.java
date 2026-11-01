package chirp.mail;

/**
 * Source of the SMTP password.
 *
 * <p>Chirp never stores passwords in source code by default — students
 * pick one of three strategies. In order of preference:</p>
 *
 * <h2>{@link #ENV} — recommended</h2>
 * <p>The password is read from a {@code .env} file (usually gitignored)
 * at send time. Nothing is hardcoded in the source code.</p>
 * <pre>{@code
 * // .env file:
 * SMTP_PASSWORD=my-app-specific-password
 *
 * // In Java:
 * Email.builder()
 *     // ...
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")
 *     .send();
 * }</pre>
 *
 * <h2>{@link #PROMPT} — good for interactive scripts</h2>
 * <p>The user is prompted for the password at send time. Nothing touches
 * the filesystem. Suitable for scripts that will be run interactively
 * (not from cron).</p>
 * <pre>{@code
 * Email.builder()
 *     // ...
 *     .promptPassword()
 *     .send();
 * }</pre>
 *
 * <h2>{@link #DIRECT} — discouraged</h2>
 * <p>The password is in the source code. Chirp prints a warning when
 * this strategy is used. Only acceptable for a one-off test; never for
 * anything that will be committed to version control.</p>
 * <pre>{@code
 * Email.builder()
 *     // ...
 *     .password("my-password")   // ← prints a warning
 *     .send();
 * }</pre>
 *
 * <h2>Default</h2>
 * <p>{@link EmailRequest} defaults to {@code ENV} when no strategy is
 * chosen. If the {@code .env} file is not found at send time, the
 * request fails with a clear message explaining what to do.</p>
 *
 * <h2>Related</h2>
 * <p>For the fluent configuration methods, see
 * {@link EmailRequest#passwordFromEnv(String, String)},
 * {@link EmailRequest#promptPassword()}, and
 * {@link EmailRequest#password(String)}.</p>
 *
 * @see EmailRequest
 * @see EnvLoader
 * @since 1.0.0
 */
public enum PasswordStrategy {
    /**
     * Provided directly in source code. Discouraged — emits a warning.
     */
    DIRECT,

    /**
     * Prompted at send time — read from stdin without echo if possible.
     */
    PROMPT,

    /**
     * Read from a {@code .env} file. Recommended. Requires
     * {@code ENV_FILE} and {@code ENV_KEY}.
     */
    ENV
}
