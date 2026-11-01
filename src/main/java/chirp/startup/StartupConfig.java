package chirp.startup;

/**
 * Immutable configuration object passed to {@link ChirpLifecycle#start}.
 *
 * <p>Build one with the fluent {@link Builder}, or use a preset:</p>
 * <pre>{@code
 * StartupConfig.defaults();   // interactive console: banner, run summary, [STRICT] hints, friendly messages
 * StartupConfig.silent();     // nothing extra: no banner, stats, hints, screen clearing;
 *                             // warnings/errors go to System.Logger("chirp")
 *
 * StartupConfig.builder()
 *     .noHeader()
 *     .withStats()
 *     .noColor()
 *     .headerText("CS-102 — Data Structures")
 *     .build();
 * }</pre>
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

    public boolean showHeader() { return showHeader; }
    public boolean stats() { return stats; }
    public boolean colorEnabled() { return colorEnabled; }
    public boolean unicodePreferred() { return unicodePreferred; }
    public boolean strictWarnings() { return strictWarnings; }
    public boolean trace() { return trace; }
    /** Whether {@code start()} clears the console before (optionally) printing the banner. */
    public boolean clearScreen() {
        return clearScreen;
    }
    /** Whether library warnings/errors are shown as friendly console lines (else System.Logger). */
    public boolean consoleMessages() {
        return consoleMessages;
    }
    public String headerText() { return headerText; }
    public long sampleIntervalMs() { return sampleIntervalMs; }

    // ---------------------------------------------------------- presets

    public static StartupConfig defaults() { return builder().build(); }
    public static StartupConfig silent() { return builder().silent().build(); }
    public static StartupConfig quiet() { return silent(); }
    public static StartupConfig minimal() { return silent(); }
    public static StartupConfig testing() { return silent(); }
    public static StartupConfig forTesting() { return silent(); }

    public static Builder builder() { return new Builder(); }

    // ---------------------------------------------------------- builder

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
        public Builder showHeader(boolean v) { this.showHeader = v; return this; }
        public Builder withHeader() { return showHeader(true); }
        public Builder noHeader() { return showHeader(false); }
        public Builder hideHeader() { return showHeader(false); }
        public Builder noBanner() { return showHeader(false); }

        /** No banner, stats, [STRICT] hints, screen clearing or console messages. */
        public Builder silent() {
            return showHeader(false).stats(false).strictWarnings(false).clearScreen(false).consoleMessages(false);
        }
        public Builder quiet() { return silent(); }

        // ---- screen clearing
        public Builder clearScreen(boolean v) { this.clearScreen = v; return this; }
        public Builder withClearScreen() { return clearScreen(true); }
        public Builder noClearScreen() { return clearScreen(false); }
        public Builder keepScreen() { return clearScreen(false); }

        // ---- friendly console messages (otherwise System.Logger)
        public Builder consoleMessages(boolean v) { this.consoleMessages = v; return this; }
        public Builder friendlyMessages(boolean v) { return consoleMessages(v); }
        public Builder withConsoleMessages() { return consoleMessages(true); }
        public Builder noConsoleMessages() { return consoleMessages(false); }

        // ---- stats
        public Builder stats(boolean v) { this.stats = v; return this; }
        public Builder withStats() { return stats(true); }
        public Builder noStats() { return stats(false); }
        public Builder withoutStats() { return stats(false); }

        // ---- colour
        public Builder color(boolean v) { this.colorEnabled = v; return this; }
        public Builder colour(boolean v) { return color(v); }
        public Builder withColor() { return color(true); }
        public Builder noColor() { return color(false); }
        public Builder noColour() { return color(false); }
        public Builder plain() { return color(false); }

        // ---- unicode
        public Builder unicode(boolean v) { this.unicodePreferred = v; return this; }
        public Builder withUnicode() { return unicode(true); }
        public Builder noUnicode() { return unicode(false); }
        public Builder ascii() { return unicode(false); }

        // ---- strict warnings
        public Builder strictWarnings(boolean v) { this.strictWarnings = v; return this; }
        public Builder withStrictWarnings() { return strictWarnings(true); }
        public Builder noStrictWarnings() { return strictWarnings(false); }
        public Builder lenient() { return strictWarnings(false); }

        // ---- trace
        public Builder trace(boolean v) { this.trace = v; return this; }
        public Builder withTrace() { return trace(true); }
        public Builder noTrace() { return trace(false); }
        public Builder debug() { return trace(true); }

        // ---- header text
        public Builder headerText(String text) { if (text != null) this.headerText = text; return this; }
        public Builder title(String text) { return headerText(text); }

        // ---- interval
        public Builder sampleIntervalMs(long ms) {
            this.sampleIntervalMs = Math.max(100L, ms);
            return this;
        }

        public StartupConfig build() { return new StartupConfig(this); }
    }
}
