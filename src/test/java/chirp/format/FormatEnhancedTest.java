package chirp.format;

import chirp.session.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatEnhancedTest {

    @BeforeEach
    void clearSession() { Session.me().clear(); }
    @AfterEach
    void resetSession() { Session.me().clear(); }

    // ============================================================== fmt aliases

    @Test
    void fmtAliases() {
        assertEquals("Hello Ali",
                Format.fmt("Hello {name}", "name", "Ali"));
        assertEquals("Hello Ali",
                Format.format("Hello {name}", "name", "Ali"));
        assertEquals("Hello Ali",
                Format.interpolate("Hello {name}", "name", "Ali"));
    }

    @Test
    void fmtNullTemplate() {
        assertEquals("", Format.fmt(null, "k", "v"));
    }

    @Test
    void fmtWithSessionAliases() {
        Session.me().name("Saad").rollNumber("001");
        assertEquals("Saad (001)", Format.fmtWithSession("{name} ({roll})"));
        assertEquals("Saad (001)", Format.formatWithSession("{name} ({roll})"));
        assertEquals("Saad (001)", Format.sessionFormat("{name} ({roll})"));
    }

    // ============================================================== currency

    @Test
    void currencyAliases() {
        assertEquals("$1,234.50", Format.currency(1234.5));
        assertEquals("$1,234.50", Format.dollars(1234.5));
        assertEquals("$1,234.50", Format.usd(1234.5));
        assertEquals("$1,234.50", Format.money(1234.5));
    }

    @Test
    void currencyKeyValueSymbol() {
        assertEquals("Rs.1,234.50",
                Format.currency(1234.5, Format.Key.SYMBOL, "Rs."));
        assertEquals("€1,234.50",
                Format.currency(1234.5, Format.Key.SYMBOL, "€"));
    }

    @Test
    void currencyKeyValueDecimals() {
        assertEquals("$1,235",   // rounds half-up, same as currencyKeyValueCombined
                Format.currency(1234.5, Format.Key.DECIMALS, 0));
        assertEquals("$1,234.500",
                Format.currency(1234.5, Format.Key.DECIMALS, 3));
    }

    @Test
    void currencyKeyValueCombined() {
        assertEquals("Rs.1,235",
                Format.currency(1234.5, Format.Key.SYMBOL, "Rs.", Format.Key.DECIMALS, 0));
    }

    @Test
    void currencyKeyValueNullsAreSafe() {
        assertDoesNotThrow(() -> Format.currency(1.5, (Object[]) null));
        assertDoesNotThrow(() -> Format.currency(1.5, Format.Key.SYMBOL, null));
        assertDoesNotThrow(() -> Format.currency(1.5, "not a key", "x"));
    }

    @Test
    void rupeesAliases() {
        assertTrue(Format.rupees(1234.5).contains("1,234.50"));
        assertTrue(Format.pkr(1234.5).contains("1,234.50"));
        assertTrue(Format.pakRupees(1234.5).contains("1,234.50"));
    }

    @Test
    void currencyOf() {
        assertEquals("€1,234.50", Format.currencyOf(1234.5, "€"));
        assertEquals("$1,234.50", Format.currencyOf(1234.5, null));
    }

    // ============================================================== percent

    @Test
    void percentAliases() {
        assertEquals("92.50%", Format.percent(92.5));
        assertEquals("92.50%", Format.pct(92.5));
        assertEquals("92.50%", Format.percentage(92.5));
    }

    @Test
    void percentPlaces() {
        assertEquals("92.5%", Format.percent(92.5, 1));
        assertEquals("92.500%", Format.percent(92.5, 3));
        assertEquals("92.50%", Format.percent(92.5, -1));  // clamped to 2
    }

    @Test
    void percentKeyValue() {
        assertEquals("92.5%", Format.percent(92.5, Format.Key.DECIMALS, 1));
    }

    // ============================================================== decimal

    @Test
    void decimalAliases() {
        assertEquals("3.14", Format.decimal(3.14159, 2));
        assertEquals("3.14", Format.fixed(3.14159, 2));
        assertEquals("3.14", Format.toFixed(3.14159, 2));
        assertEquals("3.14", Format.fixedDecimal(3.14159, 2));
    }

    @Test
    void decimalNegativePlacesClamped() {
        assertEquals("3.14", Format.decimal(3.14159, -1));
    }

    // ============================================================== plural / quantify

    @Test
    void pluralAndQuantify() {
        assertEquals("1 item", Format.plural(1, "item", "items"));
        assertEquals("3 items", Format.plural(3, "item", "items"));
        assertEquals("0 items", Format.plural(0, "item", "items"));
    }

    @Test
    void quantifyAutoS() {
        assertEquals("1 book", Format.quantify(1, "book"));
        assertEquals("5 books", Format.quantify(5, "book"));
        assertEquals("0 books", Format.quantify(0, "book"));
    }

    @Test
    void countOfAliases() {
        assertEquals("1 item", Format.countOf(1, "item", "items"));
        assertEquals("5 books", Format.countOf(5, "book"));
    }

    // ============================================================== list / join

    @Test
    void listAliases() {
        assertEquals("a, b, c", Format.list(List.of("a", "b", "c"), ", "));
        assertEquals("a, b, c", Format.join(List.of("a", "b", "c"), ", "));
        assertEquals("a, b, c", Format.itemsList(List.of("a", "b", "c"), ", "));
        assertEquals("a, b, c", Format.comma(List.of("a", "b", "c")));
    }

    @Test
    void listWithLast() {
        assertEquals("A, B, and C",
                Format.list(List.of("A", "B", "C"), ", ", ", and "));
        assertEquals("A and B",
                Format.list(List.of("A", "B"), ", ", " and "));
        assertEquals("A", Format.list(List.of("A"), ", ", " and "));
    }

    @Test
    void listNulls() {
        assertEquals("", Format.list(null, ", "));
        assertEquals("", Format.list(List.of(), ", "));
    }

    // ============================================================== ordinal

    @Test
    void ordinalAliases() {
        assertEquals("1st", Format.ordinal(1));
        assertEquals("1st", Format.nth(1));
        assertEquals("2nd", Format.nth(2));
        assertEquals("11th", Format.nth(11));
    }

    @Test
    void roman() {
        assertEquals("IV", Format.roman(4));
        assertEquals("MCMXCIV", Format.roman(1994));
    }

    // ============================================================== bytes / filesize

    @Test
    void bytesAliases() {
        assertEquals("512 B", Format.bytes(512));
        assertEquals("1.0 KB", Format.fileSize(1024));
        assertEquals("1.0 KB", Format.humanSize(1024));
    }

    // ============================================================== duration

    @Test
    void durationBasic() {
        assertEquals("0ms", Format.duration(0));
        assertEquals("500ms", Format.duration(500));
        assertEquals("5s", Format.duration(5000));
        assertEquals("1m 5s", Format.duration(65000));
        assertEquals("1h 1m 5s", Format.duration(3_665_000L));
        assertEquals("1d", Format.duration(86_400_000L));
        assertEquals("2d", Format.duration(2 * 86_400_000L));
    }

    @Test
    void durationAliases() {
        assertEquals("5s", Format.humanDuration(5000));
        assertEquals("5s", Format.timeSpan(5000));
    }

    @Test
    void durationFromDurationObject() {
        assertEquals("1m 30s", Format.duration(Duration.ofSeconds(90)));
        assertEquals("0ms", Format.duration((Duration) null));
    }

    @Test
    void durationNegativeReturnsZero() {
        assertEquals("0ms", Format.duration(-1));
    }

    @Test
    void durationShortStyle() {
        assertEquals("5s", Format.duration(5000, Format.Key.STYLE, Format.Style.SHORT));
        assertEquals("1m", Format.duration(65000, Format.Key.STYLE, Format.Style.SHORT));
        assertEquals("1h", Format.duration(3_665_000L, Format.Key.STYLE, Format.Style.SHORT));
        assertEquals("1d", Format.duration(86_400_000L, Format.Key.STYLE, Format.Style.SHORT));
    }

    // ============================================================== text delegation

    @Test
    void padAliases() {
        assertEquals("hi   ", Format.pad("hi", 5));
        assertEquals("hi   ", Format.padRight("hi", 5));
        assertEquals("   hi", Format.padLeft("hi", 5));
        assertEquals(" hi  ", Format.center("hi", 5));
        assertEquals("hi   ", Format.padded("hi", 5));
    }

    @Test
    void textTransformAliases() {
        assertEquals("Hello World", Format.title("hello world"));
        assertEquals("Hello", Format.capitalize("hello"));
        assertEquals("HELLO", Format.upper("hello"));
        assertEquals("hello", Format.lower("HELLO"));
    }

    @Test
    void maskSlugTruncate() {
        assertEquals("***4567", Format.mask("1234567", 4));
        assertEquals("hello-world", Format.slug("Hello World!"));
        assertEquals("abcd…", Format.truncate("abcdefg", 5));
    }

    // ============================================================== domain PK

    @Test
    void cnic() {
        assertEquals("35202-1234567-1", Format.cnic("3520212345671"));
        assertEquals("incomplete", Format.cnic("incomplete"));
        assertEquals("35202-1234567-1", Format.formatCnic("3520212345671"));
    }

    @Test
    void pakistaniPhone() {
        assertEquals("+92 300 1234567", Format.pakistaniPhone("0300-1234567"));
        assertEquals("+92 300 1234567", Format.pkPhone("+92 300 1234567"));
        assertEquals("invalid", Format.pakistaniPhone("invalid"));
    }

    // ============================================================== names

    @Test
    void fullName() {
        assertEquals("Saad Mansoor", Format.fullName("Saad", "Mansoor"));
        assertEquals("Saad", Format.fullName("Saad", null));
        assertEquals("Mansoor", Format.fullName(null, "Mansoor"));
        assertEquals("", Format.fullName(null, null));
    }

    @Test
    void initials() {
        assertEquals("SM", Format.initials("Saad Mansoor"));
    }
}
