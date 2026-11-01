package chirp.internal;

import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;

/**
 * The library's single diagnostics channel for warnings and errors that a
 * method reports instead of throwing (for example "Cannot read data.txt").
 *
 * <p>Where a message goes depends on how Chirp is being used:</p>
 * <ul>
 *   <li><b>Interactive console</b>: once {@code Chirp.start()} has run, the startup
 *       code installs a console sink, so messages appear as friendly,
 *       coloured console lines.</li>
 *   <li><b>Library use</b>: without a sink, messages go to the JDK platform
 *       logger ({@link System#getLogger(String) System.getLogger("chirp")}),
 *       so applications can route, filter or silence them like any other
 *       library's logs. Nothing is printed directly.</li>
 * </ul>
 *
 * <p>This class does not depend on the console package; the sink is
 * injected by the startup code. Logging never throws: if the sink fails,
 * the message falls back to the platform logger.</p>
 *
 * <p><strong>Internal API — do not use directly.</strong></p>
 */
public final class Logger {

    private static final System.Logger PLATFORM = System.getLogger("chirp");

    private static volatile BiConsumer<Level, String> consoleSink;

    private Logger() {}

    /** Routes messages to the console (interactive console mode). Called by the startup code. */
    public static void installConsoleSink(BiConsumer<Level, String> sink) {
        consoleSink = sink;
    }

    /** Routes messages back to the platform logger (library mode). */
    public static void clearConsoleSink() {
        consoleSink = null;
    }

    /** Whether messages currently go to the console instead of the platform logger. */
    public static boolean hasConsoleSink() {
        return consoleSink != null;
    }

    public static void warn(String message) { log(Level.WARNING, message, null); }

    public static void error(String message) { log(Level.ERROR, message, null); }

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
