package chirp.console;

import chirp.internal.Capabilities;
import chirp.internal.Glyphs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SeparatorTest {

    @BeforeEach
    void setup() {
        Capabilities.setUnicodeOverride(false);   // ASCII mode for predictable tests
        Glyphs.setStyle(Glyphs.Style.ASCII);
    }

    @AfterEach
    void teardown() {
        Capabilities.setUnicodeOverride(null);
        Glyphs.resetToAuto();
    }

    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    // ---------------------------------------------------- defaults

    @Test
    void defaultWidthIsForty() {
        String s = stripAnsi(Separator.builder().build().render());
        assertEquals(Separator.DEFAULT_WIDTH, s.length());
    }

    @Test
    void customWidth() {
        assertEquals(10, stripAnsi(Separator.builder().width(10).build().render()).length());
    }

    // ---------------------------------------------------- styles

    @Test
    void solidStyle() {
        String s = stripAnsi(Separator.builder().width(5).style(Separator.Style.SOLID).build().render());
        assertEquals("-----", s);
    }

    @Test
    void doubleStyle() {
        String s = stripAnsi(Separator.builder().width(5).style(Separator.Style.DOUBLE).build().render());
        assertEquals("=====", s);
    }

    @Test
    void asciiSolidStyle() {
        String s = stripAnsi(Separator.builder().width(5).style(Separator.Style.ASCII_SOLID).build().render());
        assertEquals("-----", s);
    }

    @Test
    void customChar() {
        String s = stripAnsi(Separator.builder().width(5).customChar('#').build().render());
        assertEquals("#####", s);
    }

    // ---------------------------------------------------- alignment

    @Test
    void centerAlign() {
        String s = stripAnsi(Separator.builder()
                .width(11).style(Separator.Style.SOLID)
                .align(Separator.Align.CENTER).label("AB").build().render());
        assertEquals(11, s.length());
        assertTrue(s.contains("AB"));
    }

    @Test
    void leftAlign() {
        String s = stripAnsi(Separator.builder()
                .width(10).style(Separator.Style.SOLID)
                .align(Separator.Align.LEFT).label("AB").build().render());
        assertEquals(10, s.length());
        assertTrue(s.startsWith("AB"));
    }

    @Test
    void rightAlign() {
        String s = stripAnsi(Separator.builder()
                .width(10).style(Separator.Style.SOLID)
                .align(Separator.Align.RIGHT).label("AB").build().render());
        assertEquals(10, s.length());
        assertTrue(s.endsWith("AB"));
    }

    // ---------------------------------------------------- indent

    @Test
    void indentAddsLeadingSpaces() {
        String s = stripAnsi(Separator.builder().width(5).indent(3).build().render());
        assertEquals(8, s.length());
        assertTrue(s.startsWith("   "));
    }

    // ---------------------------------------------------- key-value

    @Test
    void fromKeyValue() {
        Separator s = Separator.from(
                Separator.Key.WIDTH, 7,
                Separator.Key.STYLE, Separator.Style.DOUBLE);
        assertEquals(7, stripAnsi(s.render()).length());
        assertTrue(stripAnsi(s.render()).startsWith("="));
    }

    @Test
    void fromMap() {
        Separator s = Separator.from(Map.of(
                "width", 7,
                "style", Separator.Style.DOUBLE));
        assertEquals(7, stripAnsi(s.render()).length());
    }

    @Test
    void fromKeyValueIgnoresUnknownKeys() {
        Separator s = Separator.from(
                Separator.Key.WIDTH, 5,
                "unknown_key", 999);
        assertEquals(5, stripAnsi(s.render()).length());
    }

    // ---------------------------------------------------- null safety

    @Test
    void nullValuesInKeyValueAreIgnored() {
        Separator s = Separator.from(
                Separator.Key.WIDTH, null,
                Separator.Key.STYLE, Separator.Style.DOUBLE);
        assertEquals(Separator.DEFAULT_WIDTH, stripAnsi(s.render()).length());
    }

    @Test
    void negativeWidthClampedToOne() {
        Separator s = Separator.builder().width(-5).build();
        assertEquals(1, stripAnsi(s.render()).length());
    }

    // ---------------------------------------------------- unicode mode

    @Test
    void unicodeModeUsesBoxChar() {
        Capabilities.setUnicodeOverride(true);
        Glyphs.setStyle(Glyphs.Style.UNICODE_FANCY);
        String s = stripAnsi(Separator.builder()
                .width(3).style(Separator.Style.SOLID).build().render());
        assertEquals("───", s);
    }

    @Test
    void asciiModeWhenUnicodeUnavailable() {
        // Already ASCII in setUp
        String s = stripAnsi(Separator.builder()
                .width(3).style(Separator.Style.SOLID).build().render());
        assertEquals("---", s);
    }

    // ---------------------------------------------------- terminal width fallback

    @Test
    void terminalWidthIsPositive() {
        assertTrue(Separator.terminalWidth() > 0);
    }
}
