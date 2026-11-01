package chirp.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the newly-added methods in Text: swapCase, isUpperCase,
 * isLowerCase, reverseWords, isAnagram, removePunctuation, removeDigits,
 * extractDigits, extractLetters, substringBefore/After, countSubstrings,
 * toDotCase, thatCheck.
 */
class TextNewFeaturesTest {

    // ---------------------------------------------------------- swapCase

    @Test
    void swapCaseBasic() {
        assertEquals("hELLO wORLD", Text.swapCase("Hello World"));
    }

    @Test
    void swapCasePreservesNonLetters() {
        assertEquals("123-ABC", Text.swapCase("123-abc"));
    }

    @Test
    void swapCaseNull() {
        assertNull(Text.swapCase(null));
    }

    // ---------------------------------------------------------- isUpperCase / isLowerCase

    @Test
    void isUpperCaseBasic() {
        assertTrue(Text.isUpperCase("HELLO"));
        assertFalse(Text.isUpperCase("Hello"));
        assertFalse(Text.isUpperCase(""));
        assertFalse(Text.isUpperCase(null));
    }

    @Test
    void isUpperCaseIgnoresNonLetters() {
        assertTrue(Text.isUpperCase("HELLO 123!"));
    }

    @Test
    void isLowerCaseBasic() {
        assertTrue(Text.isLowerCase("hello"));
        assertFalse(Text.isLowerCase("Hello"));
        assertFalse(Text.isLowerCase(null));
    }

    @Test
    void isLowerCaseIgnoresNonLetters() {
        assertTrue(Text.isLowerCase("hello 123!"));
    }

    // ---------------------------------------------------------- reverseWords

    @Test
    void reverseWordsBasic() {
        assertEquals("c b a", Text.reverseWords("a b c"));
    }

    @Test
    void reverseWordsMultipleSpaces() {
        assertEquals("c b a", Text.reverseWords("a    b    c"));
    }

    @Test
    void reverseWordsSingleWord() {
        assertEquals("hello", Text.reverseWords("hello"));
    }

    @Test
    void reverseWordsNull() {
        assertNull(Text.reverseWords(null));
    }

    @Test
    void reverseWordsEmpty() {
        assertEquals("", Text.reverseWords(""));
    }

    // ---------------------------------------------------------- isAnagram

    @Test
    void isAnagramBasic() {
        assertTrue(Text.isAnagram("listen", "silent"));
        assertTrue(Text.isAnagram("Astronomer", "Moon starer"));
        assertTrue(Text.isAnagram("Eleven plus two", "Twelve plus one"));
    }

    @Test
    void isAnagramDifferentLengths() {
        assertFalse(Text.isAnagram("ab", "abc"));
    }

    @Test
    void isAnagramNull() {
        assertFalse(Text.isAnagram(null, "abc"));
        assertFalse(Text.isAnagram("abc", null));
    }

    @Test
    void isAnagramNotAnagram() {
        assertFalse(Text.isAnagram("hello", "world"));
    }

    // ---------------------------------------------------------- removePunctuation

    @Test
    void removePunctuationBasic() {
        assertEquals("Hello World", Text.removePunctuation("Hello, World!"));
    }

    @Test
    void removePunctuationNull() {
        assertNull(Text.removePunctuation(null));
    }

    // ---------------------------------------------------------- removeDigits

    @Test
    void removeDigitsBasic() {
        assertEquals("abc", Text.removeDigits("a1b2c3"));
    }

    @Test
    void removeDigitsNull() {
        assertNull(Text.removeDigits(null));
    }

    // ---------------------------------------------------------- extractDigits

    @Test
    void extractDigitsBasic() {
        assertEquals("123", Text.extractDigits("a1b2c3"));
    }

    @Test
    void extractDigitsNull() {
        assertNull(Text.extractDigits(null));
    }

    // ---------------------------------------------------------- extractLetters

    @Test
    void extractLettersBasic() {
        assertEquals("abc", Text.extractLetters("a1b2c3!@#"));
    }

    @Test
    void extractLettersNull() {
        assertNull(Text.extractLetters(null));
    }

    // ---------------------------------------------------------- substringBefore

    @Test
    void substringBeforeBasic() {
        assertEquals("user", Text.substringBefore("user@example.com", "@"));
    }

    @Test
    void substringBeforeMissing() {
        assertEquals("user@example.com", Text.substringBefore("user@example.com", "!"));
    }

    @Test
    void substringBeforeNull() {
        assertNull(Text.substringBefore(null, "@"));
        assertEquals("text", Text.substringBefore("text", null));
    }

    // ---------------------------------------------------------- substringAfter

    @Test
    void substringAfterBasic() {
        assertEquals("example.com", Text.substringAfter("user@example.com", "@"));
    }

    @Test
    void substringAfterMissing() {
        assertEquals("", Text.substringAfter("user@example.com", "!"));
    }

    @Test
    void substringAfterNull() {
        assertNull(Text.substringAfter(null, "@"));
        assertEquals("text", Text.substringAfter("text", null));
    }

    // ---------------------------------------------------------- countSubstrings

    @Test
    void countSubstringsBasic() {
        assertEquals(3, Text.countSubstrings("aaa", "a"));
        assertEquals(2, Text.countSubstrings("abcabc", "abc"));
        assertEquals(0, Text.countSubstrings("abc", "xyz"));
    }

    @Test
    void countSubstringsNull() {
        assertEquals(0, Text.countSubstrings(null, "a"));
        assertEquals(0, Text.countSubstrings("abc", null));
        assertEquals(0, Text.countSubstrings("abc", ""));
    }

    @Test
    void countSubstringsNonOverlapping() {
        // "aaa" contains "aa" only once when counting non-overlapping
        assertEquals(1, Text.countSubstrings("aaa", "aa"));
    }

    // ---------------------------------------------------------- toDotCase

    @Test
    void toDotCaseBasic() {
        assertEquals("hello.world", Text.toDotCase("hello world"));
        assertEquals("hello.world", Text.toDotCase("hello_world"));
        assertEquals("hello.world", Text.toDotCase("hello-world"));
    }

    @Test
    void toDotCaseNull() {
        assertNull(Text.toDotCase(null));
    }

    // ---------------------------------------------------------- thatCheck

    @Test
    void thatCheckFiltersByCheck() {
        List<String> items = List.of("a@b.co", "not-email", "x@example.net");
        List<String> valid = Text.thatCheck(items, Check::isEmail);
        assertEquals(2, valid.size());
    }

    @Test
    void thatCheckNullItems() {
        assertTrue(Text.thatCheck(null, Check::isEmail).isEmpty());
    }

    @Test
    void thatCheckNullPredicate() {
        assertTrue(Text.thatCheck(List.of("a"), null).isEmpty());
    }

    @Test
    void filterByCheckAlias() {
        List<String> items = List.of("a@b.co", "not-email");
        assertEquals(
                Text.thatCheck(items, Check::isEmail),
                Text.filterByCheck(items, Check::isEmail));
    }
}
