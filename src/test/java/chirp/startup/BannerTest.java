package chirp.startup;

import chirp.internal.Capabilities;
import chirp.internal.Glyphs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the banner renders with correctly aligned borders in every
 * glyph mode, and that it never contains characters that would misrender on
 * a non-UTF-8 console when ASCII mode is active.
 */
class BannerTest {

    @AfterEach
    void reset() {
        Glyphs.resetToAuto();
        Capabilities.setUnicodeOverride(null);
    }

    // ------------------------------------------------------------ helpers

    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    private static int visualLength(String line) {
        return stripAnsi(line).length();
    }

    private static List<String> visibleLines(String banner) {
        return banner.lines().filter(l -> !l.isBlank()).toList();
    }

    // ------------------------------------------------------------ alignment

    @Test
    void unicodeFancyRowsAreAligned() {
        Glyphs.setStyle(Glyphs.Style.UNICODE_FANCY);
        String banner = Banner.render(StartupConfig.defaults());
        assertRowsAligned(banner);
    }

    @Test
    void unicodeBasicRowsAreAligned() {
        Glyphs.setStyle(Glyphs.Style.UNICODE_BASIC);
        String banner = Banner.render(StartupConfig.defaults());
        assertRowsAligned(banner);
    }

    @Test
    void asciiRowsAreAligned() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        String banner = Banner.render(StartupConfig.defaults());
        assertRowsAligned(banner);
    }

    @Test
    void longSubtitleStillAligned() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        StartupConfig cfg = StartupConfig.builder()
                .headerText("A fairly long subtitle used to stress width calculation")
                .build();
        String banner = Banner.render(cfg);
        assertRowsAligned(banner);
    }

    // ------------------------------------------------------------ ASCII purity

    @Test
    void asciiModeContainsNoMultibyteChars() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        String banner = Banner.render(StartupConfig.defaults());
        String visible = stripAnsi(banner);
        for (int i = 0; i < visible.length(); i++) {
            char c = visible.charAt(i);
            if (c == '\n' || c == '\r' || c == ' ' || c == '\t') continue;
            assertTrue(c < 128,
                    "ASCII mode must contain only ASCII, but found U+"
                            + String.format("%04X", (int) c) + " at " + i);
        }
    }

    // ------------------------------------------------------------ content

    @Test
    void defaultBannerContainsVersion() {
        String banner = Banner.render(StartupConfig.defaults());
        assertTrue(banner.contains("v" + Banner.VERSION), banner);
    }

    @Test
    void defaultBannerContainsGithubLink() {
        String banner = Banner.render(StartupConfig.defaults());
        assertTrue(banner.contains(Banner.GITHUB), banner);
    }

    @Test
    void customSubtitleAppears() {
        StartupConfig cfg = StartupConfig.builder()
                .headerText("CS-102 Data Structures")
                .build();
        String banner = Banner.render(cfg);
        assertTrue(banner.contains("CS-102 Data Structures"), banner);
    }

    // ------------------------------------------------------------ null safety

    @Test
    void nullConfigDoesNotThrow() {
        assertDoesNotThrow(() -> Banner.render(null));
    }

    @Test
    void emptySubtitleUsesDefault() {
        StartupConfig cfg = StartupConfig.builder().headerText("   ").build();
        String banner = Banner.render(cfg);
        assertTrue(banner.contains("A friendly Java toolkit"), banner);
    }

    // ------------------------------------------------------------ helper

    private static void assertRowsAligned(String banner) {
        List<String> lines = visibleLines(banner);
        assertFalse(lines.isEmpty(), "Banner must have visible lines");

        int expected = visualLength(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            int actual = visualLength(lines.get(i));
            assertEquals(expected, actual,
                    "Line " + i + " has length " + actual + " but expected " + expected
                            + "\nLine: [" + stripAnsi(lines.get(i)) + "]");
        }
    }
}
