package chirp.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchTest {

    @Test
    void exactStrict() { assertTrue(Search.exact("Hello", "Hello")); assertFalse(Search.exact("Hello", "hello")); }
    @Test
    void exactLoose() { assertTrue(Search.exactIgnoreCase("Hello", "hello")); }
    @Test
    void containsLoose() { assertTrue(Search.contains("Hello World", "world")); }
    @Test
    void containsStrict() { assertFalse(Search.containsStrict("Hello World", "world")); }

    @Test
    void startsWith() { assertTrue(Search.startsWith("Hello", "he")); }
    @Test
    void endsWith() { assertTrue(Search.endsWith("Hello", "LO")); }

    @Test
    void regexMatch() { assertTrue(Search.matches("ABC123", "[A-Z]+\\d+")); }
    @Test
    void regexNoMatch() { assertFalse(Search.matches("abc", "\\d+")); }

    @Test
    void levenshteinSame() { assertEquals(0, Search.levenshtein("hello", "hello")); }
    @Test
    void levenshteinOne() { assertEquals(1, Search.levenshtein("hello", "hallo")); }
    @Test
    void levenshteinInsert() { assertEquals(1, Search.levenshtein("cat", "cats")); }
    @Test
    void levenshteinEmpty() { assertEquals(5, Search.levenshtein("", "hello")); }
    @Test
    void levenshteinNull() { assertEquals(0, Search.levenshtein(null, null)); }

    @Test
    void fuzzyTrue() { assertTrue(Search.fuzzy("colour", "color", 2)); }
    @Test
    void fuzzyFalse() { assertFalse(Search.fuzzy("completely-different", "xyz", 2)); }

    @Test
    void nullSafe() {
        assertFalse(Search.contains(null, "x"));
        assertFalse(Search.contains("x", null));
        assertFalse(Search.exact(null, null));
    }

    @Test
    void anyContains() {
        assertTrue(Search.anyContains(java.util.List.of("apple", "banana"), "nan"));
        assertFalse(Search.anyContains(java.util.List.of("apple", "banana"), "xyz"));
    }

    @Test
    void filterList() {
        var result = Search.filter(java.util.List.of("apple", "banana", "apricot"), "ap");
        assertEquals(2, result.size());
    }
}
