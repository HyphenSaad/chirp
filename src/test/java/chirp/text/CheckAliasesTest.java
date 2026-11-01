package chirp.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckAliasesTest {

    // ---------------------------------------------------------- email aliases
    @Test
    void emailAliases() {
        assertTrue(Check.looksLikeEmail("user@example.com"));
        assertTrue(Check.validEmail("user@example.com"));
        assertTrue(Check.isEmailAddress("user@example.com"));
        assertFalse(Check.looksLikeEmail("not-an-email"));
    }

    // ---------------------------------------------------------- phone aliases
    @Test
    void phoneAliases() {
        assertTrue(Check.isPhoneNumber("+923001234567"));
        assertTrue(Check.validPhone("+923001234567"));
    }

    // ---------------------------------------------------------- alpha aliases
    @Test
    void alphaAliases() {
        assertTrue(Check.isLettersOnly("abc"));
        assertTrue(Check.isAlphabetic("abc"));
    }

    // ---------------------------------------------------------- digits aliases
    @Test
    void digitsAliases() {
        assertTrue(Check.isDigitsOnly("123"));
    }

    // ---------------------------------------------------------- alphanumeric aliases
    @Test
    void alphanumericAliases() {
        assertTrue(Check.isAlphanum("abc123"));
    }

    // ---------------------------------------------------------- hex aliases
    @Test
    void hexAliases() {
        assertTrue(Check.isHex("#FF00AA"));
    }

    // ---------------------------------------------------------- numeric aliases
    @Test
    void numericAliases() {
        assertTrue(Check.isNumber("42.5"));
        assertTrue(Check.isDecimal("42.5"));
        assertTrue(Check.isInteger("42"));
        assertTrue(Check.isInt("42"));
    }

    // ---------------------------------------------------------- url aliases
    @Test
    void urlAliases() {
        assertTrue(Check.isUrl("https://example.com"));
        assertTrue(Check.isWebURL("https://example.com"));
        assertTrue(Check.isLink("https://example.com"));
    }

    // ---------------------------------------------------------- ip aliases
    @Test
    void ipAliases() {
        assertTrue(Check.isIP("192.168.1.1"));
        assertTrue(Check.isIPv4("192.168.1.1"));
    }

    // ---------------------------------------------------------- uuid aliases
    @Test
    void uuidAliases() {
        assertTrue(Check.isUuid("550e8400-e29b-41d4-a716-446655440000"));
    }

    // ---------------------------------------------------------- json aliases
    @Test
    void jsonAliases() {
        assertTrue(Check.isJSON("{\"a\":1}"));
        assertTrue(Check.isJson("{\"a\":1}"));
        assertTrue(Check.validJson("{\"a\":1}"));
        assertFalse(Check.isJSON("not json"));
        assertFalse(Check.isJSON(null));
    }

    // ---------------------------------------------------------- content checks

    @Test
    void containsDigit() {
        assertTrue(Check.containsDigit("abc123"));
        assertFalse(Check.containsDigit("abc"));
        assertFalse(Check.containsDigit(null));
    }

    @Test
    void containsLetter() {
        assertTrue(Check.containsLetter("123abc"));
        assertFalse(Check.containsLetter("123"));
        assertFalse(Check.containsLetter(null));
    }

    @Test
    void containsSpecialChar() {
        assertTrue(Check.containsSpecialChar("abc!"));
        assertFalse(Check.containsSpecialChar("abc123"));
        assertFalse(Check.containsSpecialChar(null));
    }

    @Test
    void startsWithDigit() {
        assertTrue(Check.startsWithDigit("1abc"));
        assertFalse(Check.startsWithDigit("abc1"));
        assertFalse(Check.startsWithDigit(null));
        assertFalse(Check.startsWithDigit(""));
    }

    @Test
    void endsWithDigit() {
        assertTrue(Check.endsWithDigit("abc1"));
        assertFalse(Check.endsWithDigit("1abc"));
        assertFalse(Check.endsWithDigit(null));
        assertFalse(Check.endsWithDigit(""));
    }

    // ---------------------------------------------------------- anagram
    @Test
    void isAnagramCheck() {
        assertTrue(Check.isAnagram("listen", "silent"));
        assertFalse(Check.isAnagram("hello", "world"));
    }

    // ---------------------------------------------------------- weekend
    @Test
    void weekendCheck() {
        assertTrue(Check.isWeekend(java.time.LocalDate.of(2026, 10, 31)));  // Saturday
        assertFalse(Check.isWeekend(java.time.LocalDate.of(2026, 10, 30))); // Friday
        assertTrue(Check.isWeekday(java.time.LocalDate.of(2026, 10, 30)));
    }
}
