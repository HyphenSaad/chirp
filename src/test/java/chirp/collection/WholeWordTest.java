package chirp.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WholeWordTest {

    @Test
    void exactWordMatch() {
        assertTrue(Search.wholeWord("Hello World", "World"));
        assertTrue(Search.wholeWord("Hello World", "Hello"));
    }

    @Test
    void caseInsensitiveByDefault() {
        assertTrue(Search.wholeWord("Hello World", "world"));
        assertTrue(Search.wholeWord("Hello World", "HELLO"));
    }

    @Test
    void caseSensitiveVariant() {
        assertTrue(Search.wholeWord("Hello World", "Hello", true));
        assertFalse(Search.wholeWord("Hello World", "hello", true));
    }

    @Test
    void noMatchInsideWord() {
        assertFalse(Search.wholeWord("HelloWorld", "World"));
        assertFalse(Search.wholeWord("HelloWorld", "Hello"));
    }

    @Test
    void punctuationBoundaries() {
        assertTrue(Search.wholeWord("Hello, World!", "World"));
        assertTrue(Search.wholeWord("Hello, World!", "Hello"));
    }

    @Test
    void nullInputs() {
        assertFalse(Search.wholeWord(null, "x"));
        assertFalse(Search.wholeWord("abc", null));
    }

    @Test
    void emptyWord() {
        assertFalse(Search.wholeWord("abc", ""));
    }

    /**
     * A multi-word query is treated as a phrase bounded by word characters on
     * each side. The phrase "Hello World" is bounded at start-of-string and
     * end-of-string, so it matches — and the individual words inside it also
     * match as whole words.
     */
    @Test
    void multiWordSearch() {
        assertTrue(Search.wholeWord("Hello World", "Hello World"));
        assertTrue(Search.wholeWord("Hello World", "Hello"));
        assertTrue(Search.wholeWord("Hello World", "World"));
    }

    /**
     * Queries with non-word punctuation on their edges (like {@code (program)})
     * match when bounded by non-word characters outside. This is exactly the
     * case that motivated the lookaround-based implementation.
     */
    @Test
    void specialCharsInWord() {
        // Regex metacharacters in the query are treated as literal text.
        assertFalse(Search.wholeWord("abc", "(.*)"));
        // A query bounded by punctuation matches when surrounded by non-word chars.
        assertTrue(Search.wholeWord("code (program)", "(program)"));
        // Mid-word occurrence does not match.
        assertFalse(Search.wholeWord("abc(program)xyz", "(program)"));
    }

    @Test
    void wordAtStartAndEnd() {
        assertTrue(Search.wholeWord("start middle end", "start"));
        assertTrue(Search.wholeWord("start middle end", "end"));
    }
}
