package chirp.config;

import chirp.console.Debug;
import chirp.console.Messenger;
import chirp.internal.Ansi;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.stats.Stats;

/**
 * Central configuration hub for the whole library.
 *
 * <pre>{@code
 * Config.colorEnabled(false);
 * Config.debug(true);              // user-facing debug messages
 * Config.strictWarnings(false);    // silence [STRICT] reminders
 * Config.stats(false);             // no exit summary
 * Config.trace(true);              // full internal trace (stderr)
 * Config.traceLevel(Trace.Level.DEBUG);
 * Config.traceOnly("reader", "rule");
 * }</pre>
 *
 * <p><b>Defaults.</b> Used as a plain library, Chirp is quiet: strict hints,
 * the exit summary and tracing are off. {@code Chirp.start()} switches on the
 * interactive console experience (see {@link chirp.startup.StartupConfig}).</p>
 *
 * <p>Every toggle can also be set at JVM launch time, and an explicit value
 * wins over the startup configuration:
 * {@code -Dchirp.ansi=false -Dchirp.strict=true -Dchirp.stats=true -Dchirp.trace=true}.</p>
 */
public final class Config {

    private static volatile boolean strictWarnings = defaultStrictWarnings();

    static {
        if (Boolean.parseBoolean(System.getProperty("chirp.stats", "false"))) {
            Stats.enable();
        }
        if (Boolean.parseBoolean(System.getProperty("chirp.trace", "false"))) {
            Trace.enable();
        }
    }

    private static boolean defaultStrictWarnings() {
        return Boolean.parseBoolean(System.getProperty("chirp.strict", "false"));
    }

    private static boolean defaultStats() {
        return Boolean.parseBoolean(System.getProperty("chirp.stats", "false"));
    }

    private Config() {}

    // ------------------------------------------------------------- colour
    public static void colorEnabled(boolean enabled) { Ansi.setEnabled(enabled); }
    public static void color(boolean enabled) { colorEnabled(enabled); }
    public static void enableColor() { colorEnabled(true); }
    public static void disableColor() { colorEnabled(false); }
    public static boolean isColorEnabled() { return Ansi.isEnabled(); }

    // -------------------------------------------------------------- debug
    public static void debug(boolean enabled) {
        if (enabled) Debug.enable();
        else Debug.disable();
    }
    public static void enableDebug() { Debug.enable(); }
    public static void disableDebug() { Debug.disable(); }
    public static boolean isDebugEnabled() { return Debug.isEnabled(); }

    // ------------------------------------------------------------ logging
    public static void logToStderr(boolean enabled) { Messenger.setLogToStderr(enabled); }
    public static void stderr(boolean enabled) { logToStderr(enabled); }
    public static void enableStderrLogging() { logToStderr(true); }
    public static void disableStderrLogging() { logToStderr(false); }

    // ---------------------------------------------------- strict warnings
    public static void strictWarnings(boolean enabled) { strictWarnings = enabled; }
    public static void enableStrictWarnings() { strictWarnings = true; }
    public static void disableStrictWarnings() { strictWarnings = false; }
    public static boolean isStrictWarningsEnabled() { return strictWarnings; }

    // ------------------------------------------------------------ stats
    public static void stats(boolean enabled) {
        if (enabled) Stats.enable();
        else Stats.disable();
    }
    public static void enableStats() { Stats.enable(); }
    public static void disableStats() { Stats.disable(); }
    public static boolean isStatsEnabled() { return Stats.isEnabled(); }

    // ------------------------------------------------------------ trace
    /** Enables / disables the internal tracer. */
    public static void trace(boolean enabled) {
        if (enabled) Trace.enable();
        else Trace.disable();
    }
    public static void enableTrace() { Trace.enable(); }
    public static void disableTrace() { Trace.disable(); }
    public static boolean isTraceEnabled() { return Trace.isEnabled(); }

    /** Sets the minimum level shown by the tracer. */
    public static void traceLevel(Trace.Level level) {
        Trace.setLevel(level);
    }
    public static Trace.Level getTraceLevel() { return Trace.getLevel(); }

    /** Restrict trace output to the given categories. */
    public static void traceOnly(String... categories) {
        Trace.enableOnly(categories);
    }
    public static void traceAllCategories() { Trace.enableAllCategories(); }
    public static void traceCategory(String category) { Trace.enableCategory(category); }
    public static void untraceCategory(String category) { Trace.disableCategory(category); }

    // -------------------------------------------------------------- reset
    /** Restores the library defaults (quiet unless overridden by -Dchirp.* properties). */
    public static void reset() {
        enableColor();
        disableDebug();
        disableStderrLogging();
        strictWarnings(defaultStrictWarnings());
        stats(defaultStats());
        disableTrace();
        Strict.reset();
        Stats.reset();
        Trace.reset();
    }
}
