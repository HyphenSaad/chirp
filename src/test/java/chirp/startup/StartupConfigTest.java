package chirp.startup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StartupConfigTest {

    @Test
    void defaultsAreRich() {
        StartupConfig c = StartupConfig.defaults();
        assertTrue(c.showHeader());
        assertTrue(c.stats());
        assertTrue(c.colorEnabled());
        assertTrue(c.unicodePreferred());
        assertTrue(c.strictWarnings());
        assertFalse(c.trace());
        assertEquals("Welcome!", c.headerText());
        assertTrue(c.sampleIntervalMs() > 0);
    }

    @Test
    void silentIsQuiet() {
        StartupConfig c = StartupConfig.silent();
        assertFalse(c.showHeader());
        assertFalse(c.stats());
    }

    @Test
    void builderFlags() {
        StartupConfig c = StartupConfig.builder()
                .noHeader()
                .noStats()
                .noColor()
                .noUnicode()
                .noStrictWarnings()
                .headerText("My Course")
                .build();
        assertFalse(c.showHeader());
        assertFalse(c.stats());
        assertFalse(c.colorEnabled());
        assertFalse(c.unicodePreferred());
        assertFalse(c.strictWarnings());
        assertEquals("My Course", c.headerText());
    }

    @Test
    void sampleIntervalClampedToMinimum() {
        StartupConfig c = StartupConfig.builder().sampleIntervalMs(1).build();
        assertTrue(c.sampleIntervalMs() >= 100);
    }

    @Test
    void fluentAliasesWork() {
        StartupConfig c = StartupConfig.builder()
                .quiet()
                .plain()
                .ascii()
                .lenient()
                .build();
        assertFalse(c.showHeader());
        assertFalse(c.colorEnabled());
        assertFalse(c.unicodePreferred());
        assertFalse(c.strictWarnings());
    }

    @Test
    void defaultsEnableTheInteractiveConsoleExtras() {
        StartupConfig c = StartupConfig.defaults();
        assertTrue(c.clearScreen());
        assertTrue(c.consoleMessages());
    }

    @Test
    void silentTurnsOffEveryUnrequestedOutput() {
        for (StartupConfig c : new StartupConfig[]{StartupConfig.silent(), StartupConfig.quiet(),
                StartupConfig.minimal(), StartupConfig.testing(), StartupConfig.forTesting(),
                StartupConfig.builder().silent().build(), StartupConfig.builder().quiet().build()}) {
            assertFalse(c.showHeader());
            assertFalse(c.stats());
            assertFalse(c.strictWarnings());
            assertFalse(c.clearScreen());
            assertFalse(c.consoleMessages());
        }
    }

    @Test
    void silentCanBeSelectivelyReEnabled() {
        StartupConfig c = StartupConfig.builder().silent().withHeader().consoleMessages(true).build();
        assertTrue(c.showHeader());
        assertTrue(c.consoleMessages());
        assertFalse(c.stats());
        assertFalse(c.clearScreen());
    }

    @Test
    void clearScreenAndConsoleMessageAliasesAreEquivalent() {
        assertFalse(StartupConfig.builder().clearScreen(false).build().clearScreen());
        assertFalse(StartupConfig.builder().noClearScreen().build().clearScreen());
        assertFalse(StartupConfig.builder().keepScreen().build().clearScreen());
        assertTrue(StartupConfig.builder().keepScreen().withClearScreen().build().clearScreen());

        assertFalse(StartupConfig.builder().consoleMessages(false).build().consoleMessages());
        assertFalse(StartupConfig.builder().friendlyMessages(false).build().consoleMessages());
        assertFalse(StartupConfig.builder().noConsoleMessages().build().consoleMessages());
        assertTrue(StartupConfig.builder().noConsoleMessages().withConsoleMessages().build().consoleMessages());
    }
}
