package chirp.console;

/**
 * Toggleable debug logging to {@code stderr}.
 *
 * <p>A minimal, opt-in debug channel. All calls are no-ops until
 * {@link #enable()} is invoked. When enabled, {@link #log(String)} and
 * {@link #logf(String, Object...)} prefix each message with
 * {@code "[DEBUG] "} and write to {@code System.err}.</p>
 *
 * <h2>Why this exists separate from {@link chirp.internal.Trace}</h2>
 * <p>{@link chirp.internal.Trace} is the library's own structured
 * tracer — it emits on stderr with levels, categories, timestamps, and
 * glyphs, and is off by default. {@code Debug} is a simpler channel
 * for student code: a single on/off toggle, a fixed prefix, no levels,
 * no categories. Students use it for their own debugging output:</p>
 * <pre>{@code
 * Debug.enable();
 * Debug.log("Entering main loop");
 * Debug.logf("Iteration %d of %d", i, total);
 * }</pre>
 *
 * <h2>Toggle via property or code</h2>
 * <p>Turn on with code:</p>
 * <pre>{@code
 * Debug.enable();    // or Debug.disable(); or Debug.isEnabled();
 * }</pre>
 * <p>Or, more commonly, from the {@link chirp.config.Config} facade:</p>
 * <pre>{@code
 * Config.enableDebug();
 * }</pre>
 * <p>Off by default. When off, every call is a single-branch return.</p>
 *
 * <h2>Related</h2>
 * <p>For the library's own structured trace output, see
 * {@link chirp.internal.Trace}. For the console-friendly
 * semantic printers, see {@link Console}.</p>
 *
 * @see Console
 * @see chirp.config.Config#enableDebug()
 * @see chirp.internal.Trace
 * @since 1.0.0
 */
public final class Debug {

    private static boolean enabled = false;

    private Debug() {}

    /**
     * Enables debug logging. Subsequent {@link #log(String)} and
     * {@link #logf(String, Object...)} calls will print to stderr.
     *
     * @see #disable()
     * @see #isEnabled()
     * @see chirp.config.Config#enableDebug()
     * @since 1.0.0
     */
    public static void enable() { enabled = true; }

    /**
     * Disables debug logging. Subsequent calls are no-ops.
     *
     * @see #enable()
     * @since 1.0.0
     */
    public static void disable() { enabled = false; }

    /**
     * Checks whether debug logging is currently enabled.
     *
     * @return {@code true} if enabled
     * @see #enable()
     * @see #disable()
     * @since 1.0.0
     */
    public static boolean isEnabled() { return enabled; }

    /**
     * Logs a message to stderr when enabled. A no-op when disabled.
     *
     * @param message the message (null → prints "null")
     * @see #logf(String, Object...)
     * @since 1.0.0
     */
    public static void log(String message) {
        if (!enabled) return;
        System.err.println("[DEBUG] " + message);
    }

    /**
     * Logs a formatted message to stderr when enabled. A no-op when
     * disabled. Uses {@code System.err.printf} format rules.
     *
     * <pre>{@code
     * Debug.logf("Iteration %d of %d, took %d ms", i, total, millis);
     * }</pre>
     *
     * @param format the printf-style format string
     * @param args the format arguments
     * @see #log(String)
     * @since 1.0.0
     */
    public static void logf(String format, Object... args) {
        if (!enabled) return;
        System.err.printf("[DEBUG] " + format + "%n", args);
    }
}
