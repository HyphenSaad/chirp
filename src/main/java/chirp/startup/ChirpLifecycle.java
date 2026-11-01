package chirp.startup;

import chirp.ChirpNotStartedException;
import chirp.config.Config;
import chirp.console.Messenger;
import chirp.internal.Glyphs;
import chirp.internal.Logger;
import chirp.internal.Trace;
import chirp.stats.Stats;
import chirp.stats.StatsSampler;

import java.time.Instant;

/**
 * The lifecycle gate. Manages the "started" flag, applies startup
 * configuration, spawns the stats sampler thread, and guards against use
 * before {@code Chirp.start()} has been called.
 *
 * <p>Applications do not use this class directly. The public entry point is
 * {@link chirp.Chirp#start()}.</p>
 */
public final class ChirpLifecycle {

    private enum State { UNSTARTED, STARTING, STARTED }

    private static final String STARTUP_HINT = """
            Chirp has not been started yet.

            Add this as the FIRST line of your main method:

                Chirp.start();

            Or with configuration:

                Chirp.start(
                    StartupConfig.builder()
                        .noHeader()
                        .withStats()
                        .build()
                );

            Other entry points (all equivalent): init(), boot(), setup(),
            begin(), initialize(). Learn more at:
                https://github.com/hyphensaad/chirp
            """;

    private static volatile State state = State.UNSTARTED;
    private static volatile StartupConfig config;
    private static volatile Instant startedAt;
    private static volatile StatsSampler sampler;
    private static volatile boolean permitDuringStartup = false;

    private ChirpLifecycle() {}

    // ------------------------------------------------------------- start

    public static synchronized void start(StartupConfig cfg) {
        if (state == State.STARTED) {
            Trace.warn("config", "Chirp is already started; ignoring second start()");
            return;
        }
        if (state == State.STARTING) return;

        state = State.STARTING;
        try {
            config = cfg != null ? cfg : StartupConfig.defaults();

            applyGlyphStyle(config);
            applyColor(config);
            applyStrictWarnings(config);
            applyTrace(config);
            applyConsoleMessages(config);

            permitDuringStartup = true;
            try {
                if (config.showHeader()) {
                    Banner.print(config);
                } else if (config.clearScreen()) {
                    try {
                        chirp.console.Console.clearScreenInternal();
                    } catch (Exception ignored) {}
                }
            } finally {
                permitDuringStartup = false;
            }

            if (flag("chirp.stats", config.stats())) {
                Stats.enable();
                sampler = new StatsSampler(config.sampleIntervalMs());
                sampler.start();
            } else {
                Stats.disable();
            }

            startedAt = Instant.now();
            state = State.STARTED;
            Trace.info("config", "Chirp started successfully");
        } catch (RuntimeException e) {
            state = State.UNSTARTED;
            throw e;
        }
    }

    private static void applyGlyphStyle(StartupConfig cfg) {
        if (!cfg.unicodePreferred()) Glyphs.setStyle(Glyphs.Style.ASCII);
        else Glyphs.resetToAuto();
    }

    private static void applyColor(StartupConfig cfg) {
        if (cfg.colorEnabled()) Config.enableColor();
        else Config.disableColor();
    }

    private static void applyStrictWarnings(StartupConfig cfg) {
        if (flag("chirp.strict", cfg.strictWarnings())) Config.enableStrictWarnings();
        else Config.disableStrictWarnings();
    }

    private static void applyTrace(StartupConfig cfg) {
        if (flag("chirp.trace", cfg.trace())) Config.enableTrace();
    }

    /** Interactive console mode shows library warnings/errors as friendly console lines. */
    private static void applyConsoleMessages(StartupConfig cfg) {
        if (!cfg.consoleMessages()) {
            Logger.clearConsoleSink();
            return;
        }
        Logger.installConsoleSink((level, message) -> {
            if (level == System.Logger.Level.ERROR) Messenger.error(message);
            else Messenger.warn(message);
        });
    }

    /**
     * An explicit {@code -Dchirp.<name>=true|false} on the command line wins over
     * the startup configuration, as documented in {@link Config}.
     */
    private static boolean flag(String property, boolean configured) {
        String value = System.getProperty(property);
        return value == null || value.isBlank() ? configured : Boolean.parseBoolean(value.trim());
    }

    // ------------------------------------------------------------- access

    public static boolean isStarted() { return state == State.STARTED; }
    public static StartupConfig config() { return config; }
    public static Instant startedAt() { return startedAt; }

    public static StatsSampler sampler() { return sampler; }

    // ------------------------------------------------------------- guard

    /**
     * Throws {@link ChirpNotStartedException} when a feature is used before
     * {@code Chirp.start()}. Allows use during startup itself (banner output,
     * stats init) and after startup.
     */
    public static void requireStarted() {
        if (state == State.STARTED || permitDuringStartup) return;
        throw new ChirpNotStartedException(STARTUP_HINT);
    }

    // ------------------------------------------------------------- shutdown

    /**
     * Gracefully stops the sampler thread and resets lifecycle. Used by tests
     * between classes; applications normally never call it.
     */
    public static synchronized void shutdown() {
        if (sampler != null) {
            sampler.stop();
            sampler = null;
        }
        Logger.clearConsoleSink();
        state = State.UNSTARTED;
        startedAt = null;
    }
}
