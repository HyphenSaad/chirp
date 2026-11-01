package chirp.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the expanded RuleBuilder: aliases, oneOf, set() key-value,
 * and every new predicate.
 */
class RuleBuilderEnhancedTest {

    // ---------------------------------------------------------- aliases

    @Test
    void messageAliases() {
        Rule a = Rule.define("T").matches("\\d+").message("msg").build();
        Rule b = Rule.define("T").matches("\\d+").errMsg("msg").build();
        Rule c = Rule.define("T").matches("\\d+").errorMessage("msg").build();
        Rule d = Rule.define("T").matches("\\d+").failMessage("msg").build();
        assertEquals(a.message(), b.message());
        assertEquals(a.message(), c.message());
        assertEquals(a.message(), d.message());
    }

    @Test
    void requiredAliases() {
        Rule r = Rule.define("T").matches("\\d+").mandatory().build();
        assertTrue(r.isRequired());
    }

    @Test
    void regexAliases() {
        Rule a = Rule.define("A").matches("\\d+").message("d").build();
        Rule b = Rule.define("B").pattern("\\d+").message("d").build();
        Rule c = Rule.define("C").matchesRegex("\\d+").message("d").build();
        Rule d = Rule.define("D").regex("\\d+").message("d").build();
        assertTrue(a.test("123"));
        assertTrue(b.test("123"));
        assertTrue(c.test("123"));
        assertTrue(d.test("123"));
    }

    @Test
    void startsWithAliases() {
        assertTrue(Rule.define("A").startsWith("http").message("x").build().test("https://x"));
        assertTrue(Rule.define("B").beginsWith("http").message("x").build().test("https://x"));
        assertTrue(Rule.define("C").prefix("http").message("x").build().test("https://x"));
    }

    @Test
    void minLengthAliases() {
        Rule r = Rule.define("T").minLength(3).message("x").build();
        assertTrue(r.test("abc"));
        assertFalse(r.test("ab"));
        assertFalse(Rule.define("U").atLeast(3).message("x").build().test("ab"));
        assertFalse(Rule.define("V").minLen(3).message("x").build().test("ab"));
    }

    @Test
    void trimAliases() {
        assertTrue(Rule.define("T").trim().matches("[a-z]+").message("x").build().test("  abc  "));
        assertTrue(Rule.define("U").trimmed().matches("[a-z]+").message("x").build().test("  abc  "));
    }

    @Test
    void stripNonDigitsAlias() {
        assertTrue(Rule.define("T").stripNonDigits().matches("\\d+").message("x").build().test("a1b2c3"));
        assertTrue(Rule.define("U").digitsOnly().matches("\\d+").message("x").build().test("a1b2c3"));
    }

    @Test
    void caseAliases() {
        assertTrue(Rule.define("T").lowerCase().matches("[a-z]+").message("x").build().test("ABC"));
        assertTrue(Rule.define("U").toLower().matches("[a-z]+").message("x").build().test("ABC"));
        assertTrue(Rule.define("V").upperCase().matches("[A-Z]+").message("x").build().test("abc"));
        assertTrue(Rule.define("W").toUpper().matches("[A-Z]+").message("x").build().test("abc"));
    }

    // ---------------------------------------------------------- oneOf

    @Test
    void oneOfBasic() {
        Rule r = Rule.define("COLOR").oneOf("red", "green", "blue").message("invalid color").build();
        assertTrue(r.test("red"));
        assertTrue(r.test("green"));
        assertFalse(r.test("yellow"));
    }

    @Test
    void oneOfIn() {
        Rule r = Rule.define("SIZE").in("S", "M", "L").message("invalid size").build();
        assertTrue(r.test("M"));
        assertFalse(r.test("XL"));
    }

    @Test
    void oneOfIgnoreCase() {
        Rule r = Rule.define("COLOR").oneOfIgnoreCase("Red", "Green").message("x").build();
        assertTrue(r.test("RED"));
        assertTrue(r.test("green"));
        assertFalse(r.test("BLUE"));
    }

    // ---------------------------------------------------------- set (key-value)

    @Test
    void setAppliesMessage() {
        Rule r = Rule.define("T")
                .matches("\\d+")
                .set(Rule.Key.MESSAGE, "custom")
                .build();
        assertEquals("custom", r.firstFailure("abc").orElseThrow());
    }

    @Test
    void setAppliesRequired() {
        Rule r = Rule.define("T").matches(".*").set(Rule.Key.REQUIRED, false).build();
        assertFalse(r.isRequired());
    }

    @Test
    void setAppliesMinLength() {
        Rule r = Rule.define("T")
                .set(Rule.Key.MIN_LENGTH, 5)
                .set(Rule.Key.PATTERN, "\\d+")
                .build();
        assertFalse(r.test("123"));
        assertTrue(r.test("12345"));
    }

    @Test
    void setIgnoresNullValue() {
        assertDoesNotThrow(() -> Rule.define("T")
                .matches("\\d+")
                .set(Rule.Key.MESSAGE, null)
                .build());
    }

    @Test
    void setIgnoresWrongType() {
        Rule r = Rule.define("T")
                .matches("\\d+")
                .set(Rule.Key.MIN_LENGTH, "not a number")
                .build();
        assertNotNull(r);
    }

    // ---------------------------------------------------------- lengthBetween

    @Test
    void lengthBetween() {
        Rule r = Rule.define("T").lengthBetween(3, 8).message("x").build();
        assertTrue(r.test("abc"));
        assertTrue(r.test("abcdefgh"));
        assertFalse(r.test("ab"));
        assertFalse(r.test("waytooolong"));
    }
}
