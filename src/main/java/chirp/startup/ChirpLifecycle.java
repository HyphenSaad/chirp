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
 * The lifecycle gate — manages the "Chirp has started" flag, applies
 * startup configuration, and guards features that require startup.
 *
 * <p>Applications do not call this class directly. The public entry
 * points are {@link chirp.Chirp#start()},
 * {@link chirp.Chirp#start(StartupConfig)}, and
 * {@link chirp.Chirp#start(java.util.function.Consumer)}. This class is
 * documented for advanced users who need to understand the machinery —
 * or who need to shut down and restart Chirp in the same JVM (e.g.
 * integration tests).</p>
 *
 * <h2>The three states</h2>
 * <p>{@code ChirpLifecycle} is a state machine:</p>
 * <ul>
 *   <li>{@link State#UNSTARTED} — no {@code start()} yet. Feature
 *       methods throw {@link ChirpNotStartedException}.</li>
 *   <li>{@link State#STARTING} — inside {@code start()}, before the
 *       flag is set. The banner and other startup helpers can run
 *       during this phase because the gate is temporarily open.</li>
 *   <li>{@link State#STARTED} — {@code start()} has completed. Feature
 *       methods work normally.</li>
 * </ul>
 *
 * <h2>What {@code start()} does, in order</h2>
 * <ol>
 *   <li>Captures the {@link StartupConfig} (or uses {@code defaults()})</li>
 *   <li>Applies glyph style (Unicode vs ASCII)</li>
 *   <li>Applies colour setting</li>
 *   <li>Applies strict warnings setting (system property wins)</li>
 *   <li>Applies trace setting (system property wins)</li>
 *   <li>Installs or clears the console messages sink</li>
 *   <li>Clears the screen (unless disabled)</li>
 *   <li>Prints the banner (unless disabled)</li>
 *   <li>Enables {@link Stats} and starts the sampler (unless disabled)</li>
 *   <li>Records {@code startedAt} and transitions to {@link State#STARTED}</li>
 * </ol>
 *
 * <h2>Idempotency</h2>
 * <p>Calling {@code start()} a second time is a no-op — a warning is
 * emitted via {@link Trace} and the existing state is preserved. This
 * is deliberate: students whose {@code main()} is called from a test
 * harness should not accidentally re-run startup.</p>
 *
 * <h2>Shutdown</h2>
 * <p>{@link #shutdown()} stops the sampler thread and resets the state.
 * Used by integration tests between classes; production applications
 * normally never call it.</p>
 *
 * <h2>Related</h2>
 * <p>For configuration at startup, see {@link StartupConfig}. For the
 * runtime toggles after startup, see {@link chirp.config.Config}. For
 * the entry point that students use, see {@link chirp.Chirp#start()}.</p>
 *
 * @see StartupConfig
 * @see Banner
 * @see Stats
 * @see StatsSampler
 * @see chirp.Chirp#start()
 * @see chirp.config.Config
 * @since 1.0.0
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

    /** Private lock — serializes lifecycle transitions without exposing the class monitor. */
    private static final Object LIFECYCLE_LOCK = new Object();

    private static volatile State state = State.UNSTARTED;
    private static volatile StartupConfig config;
    private static volatile Instant startedAt;
    private static volatile StatsSampler sampler;
    private static volatile boolean permitDuringStartup = false;

    private ChirpLifecycle() {}

    // ------------------------------------------------------------- start

    /**
     * Starts Chirp with the given configuration. A no-op if already
     * started. Transitions through {@link State#STARTING} and then
     * {@link State#STARTED}, applying every configuration setting in
     * the documented order.
     *
     * <p>Called via {@link chirp.Chirp#start(StartupConfig)} — students
     * do not invoke this directly. Exposed publicly so integration
     * tests and non-standard entry points (e.g. a servlet context
     * listener) can start Chirp explicitly.</p>
     *
     * @param cfg the startup configuration (null → {@link StartupConfig#defaults()})
     * @see #shutdown()
     * @since 1.0.0
     */
    public static void start(StartupConfig cfg) {
        synchronized (LIFECYCLE_LOCK) {
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
                        } catch (RuntimeException e) {
                            Trace.warn("config", "clearScreen failed: " + e.getMessage());
                        }
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

    /**
     * Checks whether Chirp has started — the state is
     * {@link State#STARTED}.
     *
     * @return {@code true} if started
     * @see chirp.Chirp#isStarted()
     * @since 1.0.0
     */
    public static boolean isStarted() { return state == State.STARTED; }

    /**
     * Returns the active startup configuration, or null before
     * {@link #start(StartupConfig)} has been called.
     *
     * @return the configuration, or null
     * @since 1.0.0
     */
    public static StartupConfig config() { return config; }

    /**
     * Returns the instant at which {@code start()} completed, or null
     * before startup.
     *
     * @return the start timestamp, or null
     * @see chirp.stats.Stats#snapshot()
     * @since 1.0.0
     */
    public static Instant startedAt() { return startedAt; }

    /**
     * Returns the background stats sampler, or null if stats are
     * disabled or startup has not happened.
     *
     * @return the sampler, or null
     * @see StatsSampler
     * @since 1.0.0
     */
    public static StatsSampler sampler() { return sampler; }

    // ------------------------------------------------------------- guard

    /**
     * Checks that Chirp has been started. Throws
     * {@link ChirpNotStartedException} with a message pointing the
     * caller at the fix if it has not — a friendly error instead of a
     * silent {@code NullPointerException} three lines later.
     *
     * <p>Also permits the call during the startup sequence itself
     * (banner printing, stats init) so the library's own startup code
     * can use the same guarded features it exposes to students.</p>
     *
     * @throws ChirpNotStartedException if Chirp is not started and not
     * in the middle of starting
     * @see chirp.ChirpNotStartedException
     * @since 1.0.0
     */
    public static void requireStarted() {
        if (state == State.STARTED || permitDuringStartup) return;
        throw new ChirpNotStartedException(STARTUP_HINT);
    }

    // ------------------------------------------------------------- shutdown

    /**
     * Stops the sampler thread and resets the lifecycle to
     * {@link State#UNSTARTED}. Used by integration tests between classes
     * — production applications normally never call it, because the JVM
     * shutdown hook handles the actual cleanup.
     *
     * <p>After {@code shutdown()}, a subsequent {@code start()} runs the
     * full startup sequence again — useful for testing that each run is
     * independent.</p>
     *
     * @see #start(StartupConfig)
     * @since 1.0.0
     */
    public static void shutdown() {
        synchronized (LIFECYCLE_LOCK) {
            if (sampler != null) {
                sampler.stop();
                sampler = null;
            }
            Logger.clearConsoleSink();
            state = State.UNSTARTED;
            startedAt = null;
        }
    }
}
