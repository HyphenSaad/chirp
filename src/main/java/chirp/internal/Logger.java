package chirp.internal;

import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;

/**
 * The library's single diagnostics channel for warnings and errors
 * that a method reports instead of throwing.
 *
 * <p><b>Internal API.</b> Do not use directly. Every user-facing
 * operation that can fail without throwing calls this class on the
 * caller's behalf — a file that cannot be read, a network call that
 * times out, a missing config key. The messages surface in one of two
 * places depending on how the library is being used:</p>
 *
 * <table border="1">
 *   <caption>Where diagnostics go</caption>
 *   <tr><th>Mode</th><th>Destination</th></tr>
 *   <tr><td><b>Interactive console</b> — {@code Chirp.start()} ran and
 *       the default startup config was used</td>
 *       <td>Friendly, coloured console lines via a <b>console sink</b>
 *           installed by {@link chirp.startup.ChirpLifecycle}</td></tr>
 *   <tr><td><b>Library use</b> — no {@code Chirp.start()}</td>
 *       <td>The JDK platform logger
 *           ({@link System#getLogger(String) System.getLogger("chirp")})</td></tr>
 * </table>
 *
 * <h2>Why two destinations</h2>
 * <p>A student writing a simple program has no logging framework
 * configured — they should see the message on the console. A production
 * application using Chirp as a library has its own logging framework
 * (SLF4J, Logback, JUL); routing Chirp's warnings through the JDK
 * platform logger means those warnings get captured by whatever the
 * application already has wired up, with the appropriate level, format,
 * and destination. Same message, same code path, right destination in
 * each context.</p>
 *
 * <h2>The console sink mechanism</h2>
 * <p>{@link chirp.startup.ChirpLifecycle} calls
 * {@link #installConsoleSink(BiConsumer)} during startup if
 * {@link chirp.startup.StartupConfig#consoleMessages()} is true (the
 * default). The sink is a {@code BiConsumer<Level, String>} that routes
 * errors and warnings to the appropriate {@code Console} method.
 * {@link chirp.startup.ChirpLifecycle#shutdown()} calls
 * {@link #clearConsoleSink()} to restore library mode.</p>
 *
 * <h2>Never throws</h2>
 * <p>If the sink throws (should not happen — {@code Console} is trivial
 * — but defense in depth), the message falls back to the platform
 * logger. Logging must never be a source of errors.</p>
 *
 * @see chirp.startup.ChirpLifecycle
 * @see chirp.startup.StartupConfig#consoleMessages()
 * @see chirp.config.Config
 * @since 1.0.0
 */
public final class Logger {

    private static final System.Logger PLATFORM = System.getLogger("chirp");

    private static volatile BiConsumer<Level, String> consoleSink;

    private Logger() {}

    /**
     * Installs a console sink that receives warnings and errors instead
     * of the platform logger. Called by {@link chirp.startup.ChirpLifecycle}
     * during startup.
     *
     * @param sink the sink (null → no-op; equivalent to clearing)
     * @see #clearConsoleSink()
     * @since 1.0.0
     */
    public static void installConsoleSink(BiConsumer<Level, String> sink) {
        consoleSink = sink;
    }

    /**
     * Removes the console sink, restoring library mode where messages
     * go to the platform logger. Called by
     * {@link chirp.startup.ChirpLifecycle#shutdown()}.
     *
     * @see #installConsoleSink(BiConsumer)
     * @since 1.0.0
     */
    public static void clearConsoleSink() {
        consoleSink = null;
    }

    /**
     * Whether messages currently go to the console instead of the
     * platform logger.
     *
     * @return {@code true} if a console sink is installed
     * @since 1.0.0
     */
    public static boolean hasConsoleSink() {
        return consoleSink != null;
    }

    /**
     * Reports a warning. In interactive console mode, appears as a
     * yellow {@code WARN} line; in library mode, logged at
     * {@link Level#WARNING}.
     *
     * @param message the message (null → empty message)
     * @since 1.0.0
     */
    public static void warn(String message) { log(Level.WARNING, message, null); }

    /**
     * Reports an error. In interactive console mode, appears as a red
     * {@code ERROR} line; in library mode, logged at
     * {@link Level#ERROR}.
     *
     * @param message the message (null → empty message)
     * @since 1.0.0
     */
    public static void error(String message) { log(Level.ERROR, message, null); }

    /**
     * Reports an error with an associated cause. In interactive
     * console mode, only the message appears (the cause is not
     * rendered — console output is for humans); in library mode, the
     * cause is passed to the platform logger.
     *
     * @param message the message
     * @param cause the underlying cause (may be null)
     * @since 1.0.0
     */
    public static void error(String message, Throwable cause) { log(Level.ERROR, message, cause); }

    private static void log(Level level, String message, Throwable cause) {
        String text = message == null ? "" : message;
        BiConsumer<Level, String> sink = consoleSink;
        if (sink != null) {
            try {
                sink.accept(level, text);
                return;
            } catch (RuntimeException sinkFailure) {
                // A broken console must never hide the message — fall through
                // to the platform logger. Trace the sink failure for diagnosis.
                Trace.debug("logger", "console sink threw: {}", sinkFailure.getMessage());
            }
        }
        if (cause == null) PLATFORM.log(level, text);
        else PLATFORM.log(level, text, cause);
    }
}
