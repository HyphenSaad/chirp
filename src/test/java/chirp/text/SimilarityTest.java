package chirp.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityTest {

    @Test
    void identicalStringsHaveFullSimilarity() {
        assertEquals(1.0, Text.similarity("hello", "hello"), 0.0001);
    }

    @Test
    void differentStringsHaveLowerSimilarity() {
        assertEquals(2.0 / 3.0, Text.similarity("abc", "abd"), 0.0001);
    }

    @Test
    void nullAndNullIsFull() {
        assertEquals(1.0, Text.similarity(null, null), 0.0001);
    }

    @Test
    void oneNullIsZero() {
        assertEquals(0.0, Text.similarity(null, "abc"), 0.0001);
        assertEquals(0.0, Text.similarity("abc", null), 0.0001);
    }

    @Test
    void emptyStringsAreFull() {
        assertEquals(1.0, Text.similarity("", ""), 0.0001);
    }

    @Test
    void emptyAndNonEmptyIsZero() {
        assertEquals(0.0, Text.similarity("", "abc"), 0.0001);
        assertEquals(0.0, Text.similarity("abc", ""), 0.0001);
    }

    @Test
    void completelyDifferentStrings() {
        assertEquals(0.0, Text.similarity("abc", "xyz"), 0.0001);
    }

    @Test
    void oneEditDistance() {
        assertEquals(0.75, Text.similarity("abcd", "abce"), 0.0001);
    }

    @Test
    void caseSensitive() {
        assertNotEquals(1.0, Text.similarity("Hello", "hello"), 0.0001);
    }

    @Test
    void longerStringsWithSmallDifference() {
        String a = "The quick brown fox";
        String b = "The quick brown fix";   // one substitution
        double sim = Text.similarity(a, b);
        assertTrue(sim > 0.9 && sim < 1.0, "Expected > 0.9 but got " + sim);
    }
}
