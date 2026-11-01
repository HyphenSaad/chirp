package chirp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @AfterEach
    void cleanup() {
        Config.reset();
        Config.disableStats(); // keep the exit summary out of test output
    }

    @Test
    void debugToggle() {
        Config.disableDebug();
        assertFalse(Config.isDebugEnabled());
        Config.enableDebug();
        assertTrue(Config.isDebugEnabled());
    }

    @Test
    void colorToggleDoesNotThrow() {
        assertDoesNotThrow(Config::disableColor);
        assertDoesNotThrow(Config::enableColor);
    }

    @Test
    void strictToggle() {
        Config.disableStrictWarnings();
        assertFalse(Config.isStrictWarningsEnabled());
        Config.enableStrictWarnings();
        assertTrue(Config.isStrictWarningsEnabled());
    }

    @Test
    void statsToggle() {
        Config.disableStats();
        assertFalse(Config.isStatsEnabled());
        Config.enableStats();
        assertTrue(Config.isStatsEnabled());
    }

    @Test
    void stderrLoggingToggle() {
        Config.disableStderrLogging();
        assertFalse(chirp.console.Messenger.isLogToStderr());
        Config.enableStderrLogging();
        assertTrue(chirp.console.Messenger.isLogToStderr());
    }
}
