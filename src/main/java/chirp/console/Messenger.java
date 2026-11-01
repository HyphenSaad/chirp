package chirp.console;

/**
 * Auto-prefixed messages with optional stderr logging.
 *
 * <p>Every method returns {@code void} and never throws. The four
 * methods are thin wrappers over {@link Console}'s semantic printers
 * that optionally mirror the message to {@code stderr}.</p>
 *
 * <h2>What "optional stderr logging" means</h2>
 * <p>When {@link #setLogToStderr(boolean)} is on, every warning and
 * error is echoed to {@code stderr} in addition to the friendly
 * console line — useful when the caller is redirecting stderr to a log
 * file. Success and info messages are not mirrored — they are not
 * things a log file needs. Off by default.</p>
 *
 * <h2>Why this exists separate from {@link Console}</h2>
 * <p>{@code Console} is the student-facing API. {@code Messenger} is
 * the internal one: library code (readers, validators, loaders) calls
 * {@code Messenger} so that a single toggle can route warnings and
 * errors to stderr when the calling application wants that. Students
 * never need to call it directly.</p>
 *
 * <h2>Related</h2>
 * <p>For the friendly console printers, see {@link Console}. For the
 * library's structured diagnostics channel, see
 * {@link chirp.internal.Logger}.</p>
 *
 * @see Console
 * @see chirp.internal.Logger
 * @since 1.0.0
 */
public final class Messenger {

    private static boolean logToStderr = false;

    private Messenger() {}

    /**
     * Enables or disables stderr mirroring for warnings and errors.
     *
     * @param enabled whether to echo warnings and errors to stderr
     * @see #isLogToStderr()
     * @see chirp.config.Config#logToStderr(boolean)
     * @since 1.0.0
     */
    public static void setLogToStderr(boolean enabled) { logToStderr = enabled; }

    /**
     * Checks whether stderr mirroring is enabled.
     *
     * @return {@code true} if warnings and errors are echoed to stderr
     * @see #setLogToStderr(boolean)
     * @since 1.0.0
     */
    public static boolean isLogToStderr() { return logToStderr; }

    /**
     * Prints a success message — see {@link Console#success(String)}.
     *
     * @param message the message
     * @see Console#success(String)
     * @since 1.0.0
     */
    public static void success(String message) { Console.success(message); }

    /**
     * Prints an informational message — see {@link Console#info(String)}.
     *
     * @param message the message
     * @see Console#info(String)
     * @since 1.0.0
     */
    public static void info(String message) { Console.info(message); }

    /**
     * Prints a warning. When {@link #isLogToStderr()} is on, also writes
     * {@code "[LOG/WARN] <message>"} to stderr.
     *
     * @param message the message
     * @see Console#warn(String)
     * @see #setLogToStderr(boolean)
     * @since 1.0.0
     */
    public static void warn(String message) {
        Console.warn(message);
        if (logToStderr) System.err.println("[LOG/WARN] " + message);
    }

    /**
     * Prints an error. When {@link #isLogToStderr()} is on, also writes
     * {@code "[LOG/ERROR] <message>"} to stderr.
     *
     * @param message the message
     * @see Console#error(String)
     * @see #setLogToStderr(boolean)
     * @since 1.0.0
     */
    public static void error(String message) {
        Console.error(message);
        if (logToStderr) System.err.println("[LOG/ERROR] " + message);
    }
}
