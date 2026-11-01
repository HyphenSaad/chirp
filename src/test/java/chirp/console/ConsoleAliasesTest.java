package chirp.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleAliasesTest {

    // ---------------------------------------------------------- semantic aliases

    @Test
    void successAliases() {
        assertDoesNotThrow(() -> Console.success("x"));
        assertDoesNotThrow(() -> Console.ok("x"));
        assertDoesNotThrow(() -> Console.done("x"));
        assertDoesNotThrow(() -> Console.good("x"));
        assertDoesNotThrow(() -> Console.passed("x"));
    }

    @Test
    void errorAliases() {
        assertDoesNotThrow(() -> Console.error("x"));
        assertDoesNotThrow(() -> Console.fail("x"));
        assertDoesNotThrow(() -> Console.err("x"));
        assertDoesNotThrow(() -> Console.failure("x"));
    }

    @Test
    void warnAliases() {
        assertDoesNotThrow(() -> Console.warn("x"));
        assertDoesNotThrow(() -> Console.warning("x"));
        assertDoesNotThrow(() -> Console.caution("x"));
        assertDoesNotThrow(() -> Console.alert("x"));
    }

    @Test
    void infoAliases() {
        assertDoesNotThrow(() -> Console.info("x"));
        assertDoesNotThrow(() -> Console.information("x"));
        assertDoesNotThrow(() -> Console.note("x"));
        assertDoesNotThrow(() -> Console.hint("x"));
    }

    // ---------------------------------------------------------- decorative aliases

    @Test
    void lineAliases() {
        assertDoesNotThrow(() -> Console.line());
        assertDoesNotThrow(() -> Console.hr());
        assertDoesNotThrow(() -> Console.sep());
        assertDoesNotThrow(() -> Console.divider());
    }

    @Test
    void titleAliases() {
        assertDoesNotThrow(() -> Console.title("T"));
        assertDoesNotThrow(() -> Console.heading("T"));
        assertDoesNotThrow(() -> Console.header("T"));
        assertDoesNotThrow(() -> Console.section("T"));
    }

    @Test
    void blankAliases() {
        assertDoesNotThrow(() -> Console.blank());
        assertDoesNotThrow(() -> Console.nl());
        assertDoesNotThrow(() -> Console.newline());
        assertDoesNotThrow(() -> Console.emptyLine());
    }

    @Test
    void watermarkAliases() {
        chirp.session.Session.me().clear();
        assertDoesNotThrow(() -> Console.watermark());
        assertDoesNotThrow(() -> Console.stamp());
        assertDoesNotThrow(() -> Console.signature());
    }

    // ---------------------------------------------------------- style helpers

    @Test
    void boldAliases() {
        assertEquals(Console.bold("x"), Console.strong("x"));
    }

    @Test
    void underlineAliases() {
        assertEquals(Console.underline("x"), Console.ul("x"));
    }

    @Test
    void quoteFamily() {
        assertEquals("\"abc\"", Console.quote("abc"));
        assertEquals("'abc'", Console.singleQuote("abc"));
        assertEquals("(abc)", Console.parenthesize("abc"));
        assertEquals("[abc]", Console.brackets("abc"));
        assertEquals("<abc>", Console.angleBrackets("abc"));
        assertEquals("{abc}", Console.braces("abc"));
        assertEquals("", Console.quote(null));
    }

    @Test
    void timestamp() {
        assertNotNull(Console.timestamp());
        assertEquals(Console.timestamp().length(), Console.time().length());
    }

    // ---------------------------------------------------------- list / row

    @Test
    void printListAliases() {
        assertDoesNotThrow(() -> Console.printList(java.util.List.of("a", "b")));
        assertDoesNotThrow(() -> Console.printValues(java.util.List.of("a", "b")));
    }

    @Test
    void printEachDoesNotThrow() {
        assertDoesNotThrow(() -> Console.printEach(java.util.List.of("a", "b", "c")));
        assertDoesNotThrow(() -> Console.printEach(null));
    }

    @Test
    void printRow() {
        assertDoesNotThrow(() -> Console.printRow(new Object[]{"a", "b", "c"}, ", "));
        assertDoesNotThrow(() -> Console.printRow(new Object[]{"a", "b"}));
        assertDoesNotThrow(() -> Console.printRow(null));
    }

    // ---------------------------------------------------------- arrays aliases

    @Test
    void showArrayAliases() {
        assertDoesNotThrow(() -> Console.showArray(new int[]{1, 2, 3}));
        assertDoesNotThrow(() -> Console.showArray(new String[]{"a", "b"}));
        assertDoesNotThrow(() -> Console.displayArray(new int[]{1}));
    }

    @Test
    void showMatrix() {
        assertDoesNotThrow(() -> Console.showMatrix(new int[][]{{1, 2}, {3, 4}}));
    }

    // ---------------------------------------------------------- progress aliases

    @Test
    void progressAliases() {
        assertDoesNotThrow(() -> Console.progress(1, 2));
        assertDoesNotThrow(() -> Console.bar(1, 2));
        assertDoesNotThrow(() -> Console.progressBar(1, 2));
    }

    // ---------------------------------------------------------- clear aliases

    @Test
    void clearAliases() {
        // Just make sure they don't throw — clearing a test terminal is harmless
        assertDoesNotThrow(() -> Console.clear());
        assertDoesNotThrow(() -> Console.cls());
    }
}
