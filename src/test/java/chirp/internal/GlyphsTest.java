package chirp.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlyphsTest {

    @AfterEach
    void reset() {
        Glyphs.resetToAuto();
        Capabilities.setUnicodeOverride(null);
    }

    @Test
    void defaultStyleAutoDetects() {
        assertNotNull(Glyphs.getStyle());
    }

    @Test
    void settingStyleChangesSymbols() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        assertEquals("OK", Glyphs.success());
        assertEquals("ERROR", Glyphs.error());
        assertEquals("WARN", Glyphs.warn());
        assertEquals("INFO", Glyphs.info());

        Glyphs.setStyle(Glyphs.Style.UNICODE_FANCY);
        assertEquals("✓", Glyphs.success());
        assertEquals("✘", Glyphs.error());
        assertEquals("⚠", Glyphs.warn());
        assertEquals("ℹ", Glyphs.info());
    }

    @Test
    void unicodeBasicStyle() {
        Glyphs.setStyle(Glyphs.Style.UNICODE_BASIC);
        assertEquals("+", Glyphs.success());
        assertEquals("x", Glyphs.error());
    }

    @Test
    void asciiStyleHasNoMultibyteChars() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        assertTrue(Glyphs.success().chars().allMatch(c -> c < 128));
        assertTrue(Glyphs.error().chars().allMatch(c -> c < 128));
        assertTrue(Glyphs.hLine(3).chars().allMatch(c -> c < 128));
    }

    @Test
    void fancyStyleHasBoxDrawing() {
        Glyphs.setStyle(Glyphs.Style.UNICODE_FANCY);
        assertEquals("─", String.valueOf(Glyphs.hLine(1).charAt(0)));
        assertEquals("═", String.valueOf(Glyphs.thickHLine(1).charAt(0)));
    }

    @Test
    void asciiFallbackForBoxDrawing() {
        Glyphs.setStyle(Glyphs.Style.ASCII);
        assertEquals("-", Glyphs.hLine(1));
        assertEquals("=", Glyphs.thickHLine(1));
        assertEquals("+", Glyphs.topLeft());
    }

    @Test
    void hLineHandlesNegative() {
        assertEquals("", Glyphs.hLine(-1));
        assertEquals("", Glyphs.hLine(0));
    }

    @Test
    void setStyleNullIsIgnored() {
        Glyphs.Style before = Glyphs.getStyle();
        Glyphs.setStyle(null);
        assertEquals(before, Glyphs.getStyle());
    }
}
