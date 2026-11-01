package chirp.startup;

/**
 * Immutable configuration object passed to
 * {@link ChirpLifecycle#start(StartupConfig)} — the "before startup"
 * counterpart to {@link chirp.config.Config} (the "after startup"
 * runtime toggles).
 *
 * <p>Configure the startup experience — banner, colours, stats,
 * strict-warnings, unicode, screen-clearing — then hand the finished
 * object to {@code Chirp.start(config)}. The object is immutable; use
 * the {@link Builder} to construct it, or one of the ready-made
 * presets.</p>
 *
 * <h2>Presets</h2>
 * <ul>
 *   <li>{@link #defaults()} — the interactive console experience:
 *       banner, run summary at exit, strict hints, friendly
 *       warnings/errors, screen clear on startup</li>
 *   <li>{@link #silent()} — nothing extra: no banner, no stats, no
 *       hints, no screen clearing, warnings/errors go to
 *       {@link System.Logger} instead of the console</li>
 * </ul>
 *
 * <h2>Example — customise the banner and disable stats</h2>
 * <pre>{@code
 * Chirp.start(
 *     StartupConfig.builder()
 *         .headerText("CS-102 — Data Structures")
 *         .noStats()
 *         .build());
 * }</pre>
 *
 * <h2>Example — full control</h2>
 * <pre>{@code
 * Chirp.start(
 *     StartupConfig.builder()
 *         .withHeader()
 *         .headerText("Demo")
 *         .withStats()
 *         .withColor()
 *         .withUnicode()
 *         .withStrictWarnings()
 *         .withTrace()              // debug mode
 *         .noClearScreen()
 *         .build());
 * }</pre>
 *
 * <h2>System property interaction</h2>
 * <p>An explicit {@code -Dchirp.*} system property wins over the
 * corresponding {@code StartupConfig} field. For example,
 * {@code -Dchirp.strict=false} disables strict warnings even when
 * {@link Builder#withStrictWarnings()} is set. This lets a grading
 * harness silence the hints without modifying student code.</p>
 *
 * <h2>Related</h2>
 * <p>For the lifecycle gate that consumes this object, see
 * {@link ChirpLifecycle}. For the banner renderer, see {@link Banner}.
 * For runtime toggles after startup, see {@link chirp.config.Config}.</p>
 *
 * @see ChirpLifecycle
 * @see Banner
 * @see chirp.config.Config
 * @see chirp.Chirp#start(StartupConfig)
 * @see chirp.Chirp#start(Consumer)
 * @since 1.0.0
 */
public final class StartupConfig {

    private final boolean showHeader;
    private final boolean stats;
    private final boolean colorEnabled;
    private final boolean unicodePreferred;
    private final boolean strictWarnings;
    private final boolean trace;
    private final boolean clearScreen;
    private final boolean consoleMessages;
    private final String headerText;
    private final long sampleIntervalMs;

    private StartupConfig(Builder b) {
        this.showHeader = b.showHeader;
        this.stats = b.stats;
        this.colorEnabled = b.colorEnabled;
        this.unicodePreferred = b.unicodePreferred;
        this.strictWarnings = b.strictWarnings;
        this.trace = b.trace;
        this.clearScreen = b.clearScreen;
        this.consoleMessages = b.consoleMessages;
        this.headerText = b.headerText;
        this.sampleIntervalMs = b.sampleIntervalMs;
    }

    /**
     * Checks whether the startup banner is shown.
     *
     * @return {@code true} if the banner is shown
     * @see Builder#withHeader()
     * @see Builder#noHeader()
     * @since 1.0.0
     */
    public boolean showHeader() { return showHeader; }

    /**
     * Checks whether the exit-time statistics summary is enabled.
     *
     * @return {@code true} if stats are enabled
     * @see Builder#withStats()
     * @see Builder#noStats()
     * @see chirp.stats.Stats
     * @since 1.0.0
     */
    public boolean stats() { return stats; }

    /**
     * Checks whether ANSI colour is enabled for startup output.
     *
     * @return {@code true} if colour is enabled
     * @see Builder#withColor()
     * @see Builder#noColor()
     * @since 1.0.0
     */
    public boolean colorEnabled() { return colorEnabled; }

    /**
     * Checks whether Unicode glyphs are preferred over ASCII fallbacks.
     *
     * @return {@code true} if Unicode is preferred
     * @see Builder#withUnicode()
     * @see Builder#ascii()
     * @since 1.0.0
     */
    public boolean unicodePreferred() { return unicodePreferred; }

    /**
     * Checks whether strict-mode warnings are enabled.
     *
     * @return {@code true} if strict warnings are enabled
     * @see Builder#withStrictWarnings()
     * @see Builder#noStrictWarnings()
     * @since 1.0.0
     */
    public boolean strictWarnings() { return strictWarnings; }

    /**
     * Checks whether the internal tracer is enabled at startup.
     *
     * @return {@code true} if tracing is enabled
     * @see Builder#withTrace()
     * @see Builder#noTrace()
     * @since 1.0.0
     */
    public boolean trace() { return trace; }

    /**
     * Checks whether the console is cleared before the banner is
     * (optionally) printed.
     *
     * @return {@code true} if the screen is cleared
     * @see Builder#withClearScreen()
     * @see Builder#noClearScreen()
     * @since 1.0.0
     */
    public boolean clearScreen() {
        return clearScreen;
    }

    /**
     * Checks whether library warnings and errors are shown as friendly
     * console lines (via {@link chirp.console.Messenger}) rather than
     * going to {@link System.Logger}.
     *
     * @return {@code true} if console messages are enabled
     * @see Builder#withConsoleMessages()
     * @see Builder#noConsoleMessages()
     * @see chirp.internal.Logger
     * @since 1.0.0
     */
    public boolean consoleMessages() {
        return consoleMessages;
    }

    /**
     * Returns the header text (or {@code "Welcome!"} by default).
     *
     * @return the header text
     * @see Builder#headerText(String)
     * @since 1.0.0
     */
    public String headerText() { return headerText; }

    /**
     * Returns the interval between background stat samples, in
     * milliseconds.
     *
     * @return the interval in milliseconds
     * @see Builder#sampleIntervalMs(long)
     * @see chirp.stats.StatsSampler
     * @since 1.0.0
     */
    public long sampleIntervalMs() { return sampleIntervalMs; }

    // ---------------------------------------------------------- presets

    /**
     * The default startup configuration — the interactive-console
     * experience with banner, stats, strict warnings, colour, unicode,
     * screen clear, and console messages all on.
     *
     * @return the default configuration
     * @see #silent()
     * @since 1.0.0
     */
    public static StartupConfig defaults() { return builder().build(); }

    /**
     * A silent startup configuration — no banner, no stats, no strict
     * warnings, no screen clearing, no console messages. Warnings and
     * errors go to {@link System.Logger}.
     *
     * <p>The configuration to use for tests, library consumers, and
     * CI environments.</p>
     *
     * @return the silent configuration
     * @see #quiet()
     * @see #minimal()
     * @see #testing()
     * @see #forTesting()
     * @since 1.0.0
     */
    public static StartupConfig silent() { return builder().silent().build(); }

    /**
     * Alias of {@link #silent()} — see that method for full
     * documentation.
     *
     * @return the silent configuration
     * @see #silent()
     * @since 1.0.0
     */
    public static StartupConfig quiet() { return silent(); }

    /**
     * Alias of {@link #silent()} — see that method for full
     * documentation.
     *
     * @return the silent configuration
     * @see #silent()
     * @since 1.0.0
     */
    public static StartupConfig minimal() { return silent(); }

    /**
     * Alias of {@link #silent()} — see that method for full
     * documentation.
     *
     * @return the silent configuration
     * @see #silent()
     * @since 1.0.0
     */
    public static StartupConfig testing() { return silent(); }

    /**
     * Alias of {@link #silent()} — see that method for full
     * documentation.
     *
     * @return the silent configuration
     * @see #silent()
     * @since 1.0.0
     */
    public static StartupConfig forTesting() { return silent(); }

    /**
     * Returns a fresh {@link Builder} for fluent configuration.
     *
     * @return a new builder
     * @since 1.0.0
     */
    public static Builder builder() { return new Builder(); }

    // ---------------------------------------------------------- builder

    /**
     * Fluent builder for {@link StartupConfig}. Every setter returns the
     * builder, so calls chain. Call {@link #build()} to get the
     * immutable configuration.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private boolean showHeader = true;
        private boolean stats = true;
        private boolean colorEnabled = true;
        private boolean unicodePreferred = true;
        private boolean strictWarnings = true;
        private boolean trace = false;
        private boolean clearScreen = true;
        private boolean consoleMessages = true;
        private String headerText = "Welcome!";
        private long sampleIntervalMs = 1000L;

        // ---- header

        /**
         * Enables or disables the startup banner.
         *
         * @param v whether to show the banner
         * @return this builder
         * @see #withHeader()
         * @see #noHeader()
         * @since 1.0.0
         */
        public Builder showHeader(boolean v) { this.showHeader = v; return this; }

        /**
         * Alias of {@link #showHeader(boolean)} with {@code true} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #showHeader(boolean)
         * @since 1.0.0
         */
        public Builder withHeader() { return showHeader(true); }

        /**
         * Alias of {@link #showHeader(boolean)} with {@code false} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #showHeader(boolean)
         * @since 1.0.0
         */
        public Builder noHeader() { return showHeader(false); }

        /**
         * Alias of {@link #noHeader()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noHeader()
         * @since 1.0.0
         */
        public Builder hideHeader() { return showHeader(false); }

        /**
         * Alias of {@link #noHeader()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noHeader()
         * @since 1.0.0
         */
        public Builder noBanner() { return showHeader(false); }

        /**
         * Configures the "silent" preset — no banner, no stats, no
         * strict warnings, no screen clearing, no console messages.
         *
         * @return this builder
         * @see #quiet()
         * @since 1.0.0
         */
        public Builder silent() {
            return showHeader(false).stats(false).strictWarnings(false).clearScreen(false).consoleMessages(false);
        }

        /**
         * Alias of {@link #silent()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #silent()
         * @since 1.0.0
         */
        public Builder quiet() { return silent(); }

        // ---- screen clearing

        /**
         * Enables or disables clearing the terminal screen before the
         * banner is printed.
         *
         * @param v whether to clear
         * @return this builder
         * @see #withClearScreen()
         * @see #noClearScreen()
         * @since 1.0.0
         */
        public Builder clearScreen(boolean v) { this.clearScreen = v; return this; }

        /**
         * Alias of {@link #clearScreen(boolean)} with {@code true} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #clearScreen(boolean)
         * @since 1.0.0
         */
        public Builder withClearScreen() { return clearScreen(true); }

        /**
         * Alias of {@link #clearScreen(boolean)} with {@code false} —
         * see that method for full documentation.
         *
         * @return this builder
         * @see #clearScreen(boolean)
         * @since 1.0.0
         */
        public Builder noClearScreen() { return clearScreen(false); }

        /**
         * Alias of {@link #noClearScreen()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noClearScreen()
         * @since 1.0.0
         */
        public Builder keepScreen() { return clearScreen(false); }

        // ---- friendly console messages

        /**
         * Enables or disables friendly console messages for library
         * warnings and errors. When disabled, they go to
         * {@link System.Logger} instead of the console.
         *
         * @param v whether to route messages to the console
         * @return this builder
         * @see #withConsoleMessages()
         * @see #noConsoleMessages()
         * @since 1.0.0
         */
        public Builder consoleMessages(boolean v) { this.consoleMessages = v; return this; }

        /**
         * Alias of {@link #consoleMessages(boolean)} — see that method
         * for full documentation.
         *
         * @param v whether to route messages to the console
         * @return this builder
         * @see #consoleMessages(boolean)
         * @since 1.0.0
         */
        public Builder friendlyMessages(boolean v) { return consoleMessages(v); }

        /**
         * Alias of {@link #consoleMessages(boolean)} with {@code true} —
         * see that method for full documentation.
         *
         * @return this builder
         * @see #consoleMessages(boolean)
         * @since 1.0.0
         */
        public Builder withConsoleMessages() { return consoleMessages(true); }

        /**
         * Alias of {@link #consoleMessages(boolean)} with {@code false}
         * — see that method for full documentation.
         *
         * @return this builder
         * @see #consoleMessages(boolean)
         * @since 1.0.0
         */
        public Builder noConsoleMessages() { return consoleMessages(false); }

        // ---- stats

        /**
         * Enables or disables the exit-time statistics summary.
         *
         * @param v whether to enable stats
         * @return this builder
         * @see #withStats()
         * @see #noStats()
         * @since 1.0.0
         */
        public Builder stats(boolean v) { this.stats = v; return this; }

        /**
         * Alias of {@link #stats(boolean)} with {@code true} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #stats(boolean)
         * @since 1.0.0
         */
        public Builder withStats() { return stats(true); }

        /**
         * Alias of {@link #stats(boolean)} with {@code false} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #stats(boolean)
         * @since 1.0.0
         */
        public Builder noStats() { return stats(false); }

        /**
         * Alias of {@link #noStats()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noStats()
         * @since 1.0.0
         */
        public Builder withoutStats() { return stats(false); }

        // ---- colour

        /**
         * Enables or disables ANSI colour for startup output.
         *
         * @param v whether to enable colour
         * @return this builder
         * @see #withColor()
         * @see #noColor()
         * @since 1.0.0
         */
        public Builder color(boolean v) { this.colorEnabled = v; return this; }

        /**
         * Alias of {@link #color(boolean)} using the British spelling
         * {@code colour} — see that method for full documentation.
         *
         * @param v whether to enable colour
         * @return this builder
         * @see #color(boolean)
         * @since 1.0.0
         */
        public Builder colour(boolean v) { return color(v); }

        /**
         * Alias of {@link #color(boolean)} with {@code true} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #color(boolean)
         * @since 1.0.0
         */
        public Builder withColor() { return color(true); }

        /**
         * Alias of {@link #color(boolean)} with {@code false} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #color(boolean)
         * @since 1.0.0
         */
        public Builder noColor() { return color(false); }

        /**
         * Alias of {@link #noColor()} using the British spelling
         * {@code colour} — see that method for full documentation.
         *
         * @return this builder
         * @see #noColor()
         * @since 1.0.0
         */
        public Builder noColour() { return color(false); }

        /**
         * Alias of {@link #noColor()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noColor()
         * @since 1.0.0
         */
        public Builder plain() { return color(false); }

        // ---- unicode

        /**
         * Enables or disables Unicode glyphs. When disabled, ASCII
         * fallbacks are used.
         *
         * @param v whether to prefer Unicode
         * @return this builder
         * @see #withUnicode()
         * @see #ascii()
         * @since 1.0.0
         */
        public Builder unicode(boolean v) { this.unicodePreferred = v; return this; }

        /**
         * Alias of {@link #unicode(boolean)} with {@code true} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #unicode(boolean)
         * @since 1.0.0
         */
        public Builder withUnicode() { return unicode(true); }

        /**
         * Alias of {@link #unicode(boolean)} with {@code false} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #unicode(boolean)
         * @since 1.0.0
         */
        public Builder noUnicode() { return unicode(false); }

        /**
         * Alias of {@link #noUnicode()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #noUnicode()
         * @since 1.0.0
         */
        public Builder ascii() { return unicode(false); }

        // ---- strict warnings

        /**
         * Enables or disables strict-mode warnings.
         *
         * @param v whether to enable strict warnings
         * @return this builder
         * @see #withStrictWarnings()
         * @see #noStrictWarnings()
         * @since 1.0.0
         */
        public Builder strictWarnings(boolean v) { this.strictWarnings = v; return this; }

        /**
         * Alias of {@link #strictWarnings(boolean)} with {@code true} —
         * see that method for full documentation.
         *
         * @return this builder
         * @see #strictWarnings(boolean)
         * @since 1.0.0
         */
        public Builder withStrictWarnings() { return strictWarnings(true); }

        /**
         * Alias of {@link #strictWarnings(boolean)} with {@code false} —
         * see that method for full documentation.
         *
         * @return this builder
         * @see #strictWarnings(boolean)
         * @since 1.0.0
         */
        public Builder noStrictWarnings() { return strictWarnings(false); }

        /**
         * Alias of {@link #noStrictWarnings()} — see that method for
         * full documentation.
         *
         * @return this builder
         * @see #noStrictWarnings()
         * @since 1.0.0
         */
        public Builder lenient() { return strictWarnings(false); }

        // ---- trace

        /**
         * Enables or disables the internal tracer at startup.
         *
         * @param v whether to enable tracing
         * @return this builder
         * @see #withTrace()
         * @see #noTrace()
         * @since 1.0.0
         */
        public Builder trace(boolean v) { this.trace = v; return this; }

        /**
         * Alias of {@link #trace(boolean)} with {@code true} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #trace(boolean)
         * @since 1.0.0
         */
        public Builder withTrace() { return trace(true); }

        /**
         * Alias of {@link #trace(boolean)} with {@code false} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #trace(boolean)
         * @since 1.0.0
         */
        public Builder noTrace() { return trace(false); }

        /**
         * Alias of {@link #withTrace()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #withTrace()
         * @since 1.0.0
         */
        public Builder debug() { return trace(true); }

        // ---- header text

        /**
         * Sets the banner text. Default: {@code "Welcome!"}.
         *
         * @param text the header text (null → no-op)
         * @return this builder
         * @see #title(String)
         * @since 1.0.0
         */
        public Builder headerText(String text) { if (text != null) this.headerText = text; return this; }

        /**
         * Alias of {@link #headerText(String)} — see that method for
         * full documentation.
         *
         * @param text the header text
         * @return this builder
         * @see #headerText(String)
         * @since 1.0.0
         */
        public Builder title(String text) { return headerText(text); }

        // ---- interval

        /**
         * Sets the interval between background stat samples, in
         * milliseconds. Clamped to at least 100 ms.
         *
         * @param ms the interval in milliseconds
         * @return this builder
         * @see chirp.stats.StatsSampler
         * @since 1.0.0
         */
        public Builder sampleIntervalMs(long ms) {
            this.sampleIntervalMs = Math.max(100L, ms);
            return this;
        }

        /**
         * Builds the immutable startup configuration.
         *
         * @return the built configuration
         * @since 1.0.0
         */
        public StartupConfig build() { return new StartupConfig(this); }
    }
}
