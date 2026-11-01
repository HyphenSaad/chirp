package chirp.startup;

import chirp.config.Config;
import chirp.internal.Logger;
import chirp.io.FileIO;
import chirp.io.TextFileIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Library use is silent and never needs {@code start()}; interactive console
 * mode is switched on by {@code Chirp.start()}; explicit -Dchirp.* wins.
 */
class StartupModesTest {

    private String savedStrict;

    @BeforeEach
    void setup() {
        savedStrict = System.getProperty("chirp.strict");
        ChirpLifecycle.shutdown();
    }

    @AfterEach
    void teardown() {
        ChirpLifecycle.shutdown();
        if (savedStrict == null) System.clearProperty("chirp.strict");
        else System.setProperty("chirp.strict", savedStrict);
        Config.reset();
    }

    @Test
    void silentStartKeepsDiagnosticsOnThePlatformLogger() {
        ChirpLifecycle.start(StartupConfig.silent());
        assertTrue(ChirpLifecycle.isStarted());
        assertFalse(Logger.hasConsoleSink());
    }

    @Test
    void consoleMessagesInstallTheConsoleSinkUntilShutdown() {
        ChirpLifecycle.start(StartupConfig.builder().silent().consoleMessages(true).build());
        assertTrue(Logger.hasConsoleSink());
        ChirpLifecycle.shutdown();
        assertFalse(Logger.hasConsoleSink());
    }

    @Test
    void withoutAPropertyTheStartupConfigDecidesStrictHints() {
        System.clearProperty("chirp.strict");
        ChirpLifecycle.start(StartupConfig.builder().silent().strictWarnings(true).build());
        assertTrue(Config.isStrictWarningsEnabled());
        ChirpLifecycle.shutdown();
        ChirpLifecycle.start(StartupConfig.silent());
        assertFalse(Config.isStrictWarningsEnabled());
    }

    @Test
    void explicitSystemPropertyWinsOverTheStartupConfig() {
        System.setProperty("chirp.strict", "false");
        ChirpLifecycle.start(StartupConfig.builder().silent().strictWarnings(true).build());
        assertFalse(Config.isStrictWarningsEnabled());
        ChirpLifecycle.shutdown();

        System.setProperty("chirp.strict", "true");
        ChirpLifecycle.start(StartupConfig.silent());
        assertTrue(Config.isStrictWarningsEnabled());
    }

    @Test
    void resetRestoresTheQuietLibraryDefaults() {
        System.clearProperty("chirp.strict");
        Config.enableStrictWarnings();
        Config.enableStats();
        Config.reset();
        assertFalse(Config.isStrictWarningsEnabled());
        assertFalse(Config.isStatsEnabled());
    }

    @Test
    void fileFailuresDoNotRequireStartAndNeverThrow() {
        assertFalse(ChirpLifecycle.isStarted());
        String missing = "chirp-missing-" + System.nanoTime() + ".txt";
        assertDoesNotThrow(() -> FileIO.readAll(missing));
        assertDoesNotThrow(() -> FileIO.readLines(missing));
        assertDoesNotThrow(() -> TextFileIO.readAll(missing));
    }
}
