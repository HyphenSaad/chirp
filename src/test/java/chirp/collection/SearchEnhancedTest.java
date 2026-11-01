package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchEnhancedTest {

    // ---------------------------------------------------------- equality aliases

    @Test
    void exactAliases() {
        assertTrue(Search.exact("Hello", "Hello"));
        assertTrue(Search.equals("Hello", "Hello"));
        assertTrue(Search.sameAs("Hello", "Hello"));
        assertFalse(Search.exact("Hello", "hello"));
    }

    // ---------------------------------------------------------- contains aliases

    @Test
    void containsAliases() {
        assertTrue(Search.contains("Hello World", "world"));
        assertTrue(Search.looseContains("Hello World", "world"));
        assertTrue(Search.includes("Hello World", "world"));
        assertTrue(Search.has("Hello World", "world"));
        assertTrue(Search.containsIgnoreCase("Hello World", "world"));
    }

    @Test
    void containsStrictAliases() {
        assertFalse(Search.containsStrict("Hello World", "world"));
        assertFalse(Search.exactContains("Hello World", "world"));
        assertTrue(Search.containsStrict("Hello World", "World"));
    }

    @Test
    void containsAnyAll() {
        assertTrue(Search.containsAny("Hello World", "foo", "world"));
        assertFalse(Search.containsAny("Hello World", "foo", "bar"));
        assertTrue(Search.containsAll("Hello World", "Hello", "World"));
        assertFalse(Search.containsAll("Hello World", "Hello", "foo"));
    }

    // ---------------------------------------------------------- prefix/suffix aliases

    @Test
    void startsWithAliases() {
        assertTrue(Search.startsWith("Hello", "he"));
        assertTrue(Search.beginsWith("Hello", "he"));
        assertTrue(Search.prefix("Hello", "he"));
    }

    @Test
    void endsWithAliases() {
        assertTrue(Search.endsWith("Hello", "LO"));
        assertTrue(Search.suffix("Hello", "LO"));
    }

    // ---------------------------------------------------------- regex aliases

    @Test
    void regexAliases() {
        assertTrue(Search.matches("abc123", "[a-z]+\\d+"));
        assertTrue(Search.regex("abc123", "[a-z]+\\d+"));
        assertTrue(Search.matchesRegex("abc123", "[a-z]+\\d+"));
        assertTrue(Search.pattern("abc123", "[a-z]+\\d+"));
    }

    // ---------------------------------------------------------- whole word aliases

    @Test
    void wholeWordAliases() {
        assertTrue(Search.wholeWord("Hello World", "World"));
        assertTrue(Search.exactWord("Hello World", "World"));
        assertTrue(Search.wordMatch("Hello World", "World"));
    }

    // ---------------------------------------------------------- fuzzy aliases

    @Test
    void fuzzyAliases() {
        assertTrue(Search.fuzzy("colour", "color", 2));
        assertTrue(Search.fuzzyMatch("colour", "color", 2));
        assertTrue(Search.similarTo("colour", "color", 2));
        assertTrue(Search.approx("colour", "color", 2));
    }

    @Test
    void levenshteinAliases() {
        assertEquals(1, Search.levenshtein("hello", "hallo"));
        assertEquals(1, Search.editDistance("hello", "hallo"));
        assertEquals(1, Search.distance("hello", "hallo"));
    }

    // ---------------------------------------------------------- composite key-value

    @Test
    void compositeDefault() {
        // Default: loose contains
        assertTrue(Search.matcher("Hello World", "world"));
        assertTrue(Search.matcher("HelloWorld", "world"));  // it's loose contains → still true
    }

    @Test
    void compositeCaseSensitive() {
        assertTrue(Search.matcher("Hello World", "World",
                Search.Key.CASE_SENSITIVE, true));
        assertFalse(Search.matcher("Hello World", "world",
                Search.Key.CASE_SENSITIVE, true));
    }

    @Test
    void compositeWholeWord() {
        assertTrue(Search.matcher("Hello World", "World",
                Search.Key.WHOLE_WORD, true));
        assertFalse(Search.matcher("HelloWorld", "World",
                Search.Key.WHOLE_WORD, true));
    }

    @Test
    void compositeWholeWordCaseSensitive() {
        assertTrue(Search.matcher("Hello World", "World",
                Search.Key.WHOLE_WORD, true,
                Search.Key.CASE_SENSITIVE, true));
        assertFalse(Search.matcher("Hello World", "world",
                Search.Key.WHOLE_WORD, true,
                Search.Key.CASE_SENSITIVE, true));
    }

    @Test
    void compositeFuzzy() {
        assertTrue(Search.matcher("colour", "color",
                Search.Key.MAX_DISTANCE, 1));
        assertFalse(Search.matcher("completely-different", "xyz",
                Search.Key.MAX_DISTANCE, 2));
    }

    @Test
    void compositeNullsSafe() {
        assertFalse(Search.matcher(null, "x"));
        assertFalse(Search.matcher("x", null));
        assertDoesNotThrow(() -> Search.matcher("x", "y", (Object[]) null));
    }

    @Test
    void compositeAlias() {
        assertEquals(Search.matcher("Hello", "ell"), Search.match("Hello", "ell"));
    }

    // ---------------------------------------------------------- bulk

    @Test
    void filterBulk() {
        var result = Search.filter(List.of("apple", "banana", "apricot"), "ap");
        assertEquals(2, result.size());
    }

    @Test
    void searchAlias() {
        var a = Search.filter(List.of("apple", "banana"), "ap");
        var b = Search.search(List.of("apple", "banana"), "ap");
        assertEquals(a, b);
    }

    @Test
    void anyContains() {
        assertTrue(Search.anyContains(List.of("apple", "banana"), "nan"));
        assertFalse(Search.anyContains(List.of("apple", "banana"), "xyz"));
    }
}
