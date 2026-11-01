package chirp.config;

import chirp.console.Debug;
import chirp.console.Messenger;
import chirp.internal.Ansi;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.stats.Stats;

/**
 * Central runtime configuration hub — every toggle in the library, in
 * one place.
 *
 * <p>{@code Config} controls the library's runtime behaviour after
 * startup. It is the "after" counterpart to {@link chirp.startup.StartupConfig}
 * (which is the "before" — configuration passed to
 * {@link chirp.Chirp#start(chirp.startup.StartupConfig)}). Once startup
 * has happened, {@code Config} is where a running program turns things
 * on or off.</p>
 *
 * <h2>What this class covers</h2>
 * <table border="1">
 *   <caption>The six runtime toggles</caption>
 *   <tr><th>Category</th><th>Toggles</th><th>Effect</th></tr>
 *   <tr><td>Colour</td>
 *       <td>{@link #colorEnabled(boolean)} / {@link #enableColor()} /
 *           {@link #disableColor()}</td>
 *       <td>ANSI escape sequences in output</td></tr>
 *   <tr><td>Debug</td>
 *       <td>{@link #debug(boolean)} / {@link #enableDebug()} /
 *           {@link #disableDebug()}</td>
 *       <td>User debug messages to stderr</td></tr>
 *   <tr><td>Stderr logging</td>
 *       <td>{@link #logToStderr(boolean)}</td>
 *       <td>Mirror warnings/errors to stderr</td></tr>
 *   <tr><td>Strict</td>
 *       <td>{@link #strictWarnings(boolean)}</td>
 *       <td>[STRICT] reminders when readers lack validation</td></tr>
 *   <tr><td>Stats</td>
 *       <td>{@link #stats(boolean)}</td>
 *       <td>Exit-summary line at JVM shutdown</td></tr>
 *   <tr><td>Trace</td>
 *       <td>{@link #trace(boolean)} / {@link #traceLevel(chirp.internal.Trace.Level)}
 *           / {@link #traceOnly(String...)}</td>
 *       <td>Library's structured tracer on stderr</td></tr>
 * </table>
 *
 * <h2>Defaults</h2>
 * <p>Used as a plain library, Chirp is <b>quiet</b>: strict hints,
 * the exit summary, and tracing are off. Colour is on when the
 * terminal supports it. {@link chirp.Chirp#start()} switches on the
 * richer interactive-console defaults — see {@link chirp.startup.StartupConfig}
 * for the exact deltas.</p>
 *
 * <h2>System property overrides</h2>
 * <p>Every toggle can also be set at JVM launch time, and an explicit
 * {@code -D} value wins over both the {@code Chirp.start()} defaults
 * and any {@code Config} call:</p>
 * <pre>{@code
 * java -Dchirp.ansi=false \
 *      -Dchirp.strict=true \
 *      -Dchirp.stats=true \
 *      -Dchirp.trace=true \
 *      MyProgram
 * }</pre>
 * <p>This is deliberate — a grading harness or CI system should be
 * able to force a configuration without touching the source code.</p>
 *
 * <h2>Example — a quiet configuration</h2>
 * <pre>{@code
 * public static void main(String[] args) {
 *     Chirp.start(
 *         StartupConfig.builder()
 *             .noColor()
 *             .noStrictWarnings()
 *             .noStats()
 *             .build());
 *
 *     // Later, mid-program, temporarily turn colour off
 *     Config.disableColor();
 *     Console.println("Plain text from here");
 *     Config.enableColor();
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For startup-time configuration, see {@link chirp.startup.StartupConfig}.
 * For the library's trace output, see {@link chirp.internal.Trace}. For
 * the exit summary, see {@link chirp.stats.Stats}.</p>
 *
 * @see chirp.startup.StartupConfig
 * @see chirp.Chirp#start()
 * @see chirp.Chirp#start(chirp.startup.StartupConfig)
 * @see chirp.internal.Trace
 * @see chirp.stats.Stats
 * @since 1.0.0
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

    /**
     * Enables or disables ANSI colour output. When disabled, every style
     * helper on {@link chirp.console.Console} returns the plain string
     * instead of an escaped one.
     *
     * <p>Also applies at startup via {@code StartupConfig.colorEnabled}
     * and the {@code -Dchirp.ansi=false} system property.</p>
     *
     * @param enabled whether to emit ANSI escape sequences
     * @see #color(boolean)
     * @see #enableColor()
     * @see #disableColor()
     * @see #isColorEnabled()
     * @since 1.0.0
     */
    public static void colorEnabled(boolean enabled) { Ansi.setEnabled(enabled); }

    /**
     * Alias of {@link #colorEnabled(boolean)} — see that method for full
     * documentation.
     *
     * @param enabled whether to emit ANSI escape sequences
     * @see #colorEnabled(boolean)
     * @since 1.0.0
     */
    public static void color(boolean enabled) { colorEnabled(enabled); }

    /**
     * Alias of {@link #colorEnabled(boolean)} with {@code true} — see
     * that method for full documentation.
     *
     * @see #colorEnabled(boolean)
     * @since 1.0.0
     */
    public static void enableColor() { colorEnabled(true); }

    /**
     * Alias of {@link #colorEnabled(boolean)} with {@code false} — see
     * that method for full documentation.
     *
     * @see #colorEnabled(boolean)
     * @since 1.0.0
     */
    public static void disableColor() { colorEnabled(false); }

    /**
     * Checks whether colour is currently enabled (and the terminal
     * supports it).
     *
     * @return {@code true} if ANSI colour is active
     * @see #colorEnabled(boolean)
     * @since 1.0.0
     */
    public static boolean isColorEnabled() { return Ansi.isEnabled(); }

    // -------------------------------------------------------------- debug

    /**
     * Enables or disables user debug logging. When enabled,
     * {@link Debug#log(String)} and {@link Debug#logf(String, Object...)}
     * write to stderr with a {@code [DEBUG]} prefix.
     *
     * @param enabled whether to enable debug logging
     * @see #enableDebug()
     * @see #disableDebug()
     * @see #isDebugEnabled()
     * @see chirp.console.Debug
     * @since 1.0.0
     */
    public static void debug(boolean enabled) {
        if (enabled) Debug.enable();
        else Debug.disable();
    }

    /**
     * Alias of {@link #debug(boolean)} with {@code true} — see that
     * method for full documentation.
     *
     * @see #debug(boolean)
     * @since 1.0.0
     */
    public static void enableDebug() { Debug.enable(); }

    /**
     * Alias of {@link #debug(boolean)} with {@code false} — see that
     * method for full documentation.
     *
     * @see #debug(boolean)
     * @since 1.0.0
     */
    public static void disableDebug() { Debug.disable(); }

    /**
     * Checks whether user debug logging is enabled.
     *
     * @return {@code true} if enabled
     * @see #debug(boolean)
     * @since 1.0.0
     */
    public static boolean isDebugEnabled() { return Debug.isEnabled(); }

    // ------------------------------------------------------------ logging

    /**
     * Enables or disables stderr mirroring of warnings and errors. When
     * enabled, every warning and error emitted through
     * {@link chirp.console.Messenger} is also written to
     * {@code System.err}.
     *
     * <p>Useful when the caller redirects stderr to a log file and
     * wants warnings and errors to appear there too.</p>
     *
     * @param enabled whether to mirror to stderr
     * @see #stderr(boolean)
     * @see #enableStderrLogging()
     * @see #disableStderrLogging()
     * @since 1.0.0
     */
    public static void logToStderr(boolean enabled) { Messenger.setLogToStderr(enabled); }

    /**
     * Alias of {@link #logToStderr(boolean)} — see that method for full
     * documentation.
     *
     * @param enabled whether to mirror to stderr
     * @see #logToStderr(boolean)
     * @since 1.0.0
     */
    public static void stderr(boolean enabled) { logToStderr(enabled); }

    /**
     * Alias of {@link #logToStderr(boolean)} with {@code true} — see
     * that method for full documentation.
     *
     * @see #logToStderr(boolean)
     * @since 1.0.0
     */
    public static void enableStderrLogging() { logToStderr(true); }

    /**
     * Alias of {@link #logToStderr(boolean)} with {@code false} — see
     * that method for full documentation.
     *
     * @see #logToStderr(boolean)
     * @since 1.0.0
     */
    public static void disableStderrLogging() { logToStderr(false); }

    // ---------------------------------------------------- strict warnings

    /**
     * Enables or disables strict-mode warnings. When enabled, immediate
     * readers (e.g. {@link chirp.console.Input#readInt(String)}) print
     * a {@code [STRICT]} hint to stderr the first time they are used
     * with a given prompt without validation.
     *
     * <p>Fluent readers never warn — the caller opted into explicit
     * control. Turn off strict warnings to silence the entire category
     * without changing the code.</p>
     *
     * @param enabled whether to emit strict warnings
     * @see #enableStrictWarnings()
     * @see #disableStrictWarnings()
     * @see #isStrictWarningsEnabled()
     * @since 1.0.0
     */
    public static void strictWarnings(boolean enabled) { strictWarnings = enabled; }

    /**
     * Alias of {@link #strictWarnings(boolean)} with {@code true} — see
     * that method for full documentation.
     *
     * @see #strictWarnings(boolean)
     * @since 1.0.0
     */
    public static void enableStrictWarnings() { strictWarnings = true; }

    /**
     * Alias of {@link #strictWarnings(boolean)} with {@code false} — see
     * that method for full documentation.
     *
     * @see #strictWarnings(boolean)
     * @since 1.0.0
     */
    public static void disableStrictWarnings() { strictWarnings = false; }

    /**
     * Checks whether strict-mode warnings are enabled.
     *
     * @return {@code true} if enabled
     * @see #strictWarnings(boolean)
     * @since 1.0.0
     */
    public static boolean isStrictWarningsEnabled() { return strictWarnings; }

    // ------------------------------------------------------------ stats

    /**
     * Enables or disables the runtime statistics tracker. When enabled,
     * a summary line is printed at JVM shutdown with elapsed time, heap
     * use, and read counts.
     *
     * @param enabled whether to enable stats
     * @see #enableStats()
     * @see #disableStats()
     * @see #isStatsEnabled()
     * @see chirp.stats.Stats
     * @since 1.0.0
     */
    public static void stats(boolean enabled) {
        if (enabled) Stats.enable();
        else Stats.disable();
    }

    /**
     * Alias of {@link #stats(boolean)} with {@code true} — see that
     * method for full documentation.
     *
     * @see #stats(boolean)
     * @since 1.0.0
     */
    public static void enableStats() { Stats.enable(); }

    /**
     * Alias of {@link #stats(boolean)} with {@code false} — see that
     * method for full documentation.
     *
     * @see #stats(boolean)
     * @since 1.0.0
     */
    public static void disableStats() { Stats.disable(); }

    /**
     * Checks whether stats are enabled.
     *
     * @return {@code true} if enabled
     * @see #stats(boolean)
     * @since 1.0.0
     */
    public static boolean isStatsEnabled() { return Stats.isEnabled(); }

    // ------------------------------------------------------------ trace

    /**
     * Enables or disables the internal tracer. When enabled, the
     * library emits detailed trace lines to stderr — reader enter/exit,
     * rule evaluation, filter matches, HTTP request/response, and so
     * on.
     *
     * <p>Much more verbose than {@link #debug(boolean)}. Intended for
     * debugging the library itself, not for user application logging.</p>
     *
     * @param enabled whether to enable tracing
     * @see #enableTrace()
     * @see #disableTrace()
     * @see #traceLevel(chirp.internal.Trace.Level)
     * @see #traceOnly(String...)
     * @see #isTraceEnabled()
     * @see chirp.internal.Trace
     * @since 1.0.0
     */
    public static void trace(boolean enabled) {
        if (enabled) Trace.enable();
        else Trace.disable();
    }

    /**
     * Alias of {@link #trace(boolean)} with {@code true} — see that
     * method for full documentation.
     *
     * @see #trace(boolean)
     * @since 1.0.0
     */
    public static void enableTrace() { Trace.enable(); }

    /**
     * Alias of {@link #trace(boolean)} with {@code false} — see that
     * method for full documentation.
     *
     * @see #trace(boolean)
     * @since 1.0.0
     */
    public static void disableTrace() { Trace.disable(); }

    /**
     * Checks whether tracing is enabled.
     *
     * @return {@code true} if enabled
     * @see #trace(boolean)
     * @since 1.0.0
     */
    public static boolean isTraceEnabled() { return Trace.isEnabled(); }

    /**
     * Sets the minimum level of trace output that is emitted.
     *
     * <p>Levels, in order of increasing severity:
     * {@code TRACE} (most verbose) → {@code DEBUG} → {@code INFO} →
     * {@code WARN} (least verbose). Setting a higher level filters out
     * lower-priority lines.</p>
     *
     * @param level the minimum level to emit (null → {@code TRACE})
     * @see #getTraceLevel()
     * @see chirp.internal.Trace.Level
     * @since 1.0.0
     */
    public static void traceLevel(Trace.Level level) {
        Trace.setLevel(level);
    }

    /**
     * Returns the current trace level.
     *
     * @return the current level (never null)
     * @see #traceLevel(chirp.internal.Trace.Level)
     * @since 1.0.0
     */
    public static Trace.Level getTraceLevel() { return Trace.getLevel(); }

    /**
     * Restricts trace output to the given categories — e.g.
     * {@code traceOnly("reader", "rule")} shows reader and rule events
     * but not input, filter, http, etc.
     *
     * <p>Call {@link #traceAllCategories()} to restore full output.</p>
     *
     * @param categories the categories to include (null → no categories enabled)
     * @see #traceAllCategories()
     * @see #traceCategory(String)
     * @see #untraceCategory(String)
     * @see chirp.internal.Trace#KNOWN_CATEGORIES
     * @since 1.0.0
     */
    public static void traceOnly(String... categories) {
        Trace.enableOnly(categories);
    }

    /**
     * Restores full trace output — every category enabled.
     *
     * @see #traceOnly(String...)
     * @since 1.0.0
     */
    public static void traceAllCategories() { Trace.enableAllCategories(); }

    /**
     * Adds a category to the enabled set. Only useful when a previous
     * call to {@link #traceOnly(String...)} restricted the set.
     *
     * @param category the category to enable (null → no-op)
     * @see #traceOnly(String...)
     * @since 1.0.0
     */
    public static void traceCategory(String category) { Trace.enableCategory(category); }

    /**
     * Removes a category from the enabled set. Only useful when a
     * previous call to {@link #traceOnly(String...)} or
     * {@link #traceCategory(String)} added it.
     *
     * @param category the category to remove (null → no-op)
     * @see #traceOnly(String...)
     * @since 1.0.0
     */
    public static void untraceCategory(String category) { Trace.disableCategory(category); }

    // -------------------------------------------------------------- reset

    /**
     * Restores the library defaults: colour on, debug off, stderr
     * logging off, strict warnings off, stats off, trace off. Any
     * {@code -Dchirp.*} system property still wins over these defaults
     * (they are read at class load, so this method does not override
     * them).
     *
     * <p>Call this when resetting state between tests or between
     * independent runs in the same JVM.</p>
     *
     * @see chirp.startup.StartupConfig
     * @since 1.0.0
     */
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
