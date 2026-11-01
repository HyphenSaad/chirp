package chirp.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateEnhancedTest {

    // ---------------------------------------------------------- labeled entry

    @Test
    void labeledOf() {
        ValidationResult r = Validate.of("Email", "bad")
                .notBlank("required")
                .isEmail("invalid")
                .validate();
        assertTrue(r.hasErrors());
        assertEquals("Email: invalid", r.firstError());
    }

    @Test
    void labeledOnField() {
        ValidationResult r = Validate.onField("Name", "")
                .notBlank("required")
                .validate();
        assertEquals("Name: required", r.firstError());
    }

    @Test
    void unlabeledOf() {
        ValidationResult r = Validate.of("bad")
                .isEmail("invalid")
                .validate();
        assertEquals("invalid", r.firstError());
    }

    // ---------------------------------------------------------- entry aliases

    @Test
    void entryAliases() {
        assertTrue(Validate.that("x").notBlank("e").validate().isValid());
        assertTrue(Validate.on("x").notBlank("e").validate().isValid());
        assertTrue(Validate.value("x").notBlank("e").validate().isValid());
        assertTrue(Validate.check("x").notBlank("e").validate().isValid());
        assertTrue(Validate.field("x").notBlank("e").validate().isValid());
        assertTrue(Validate.forValue("x").notBlank("e").validate().isValid());
        assertTrue(Validate.of("x").notBlank("e").validate().isValid());
    }

    // ---------------------------------------------------------- check aliases

    @Test
    void checkAliases() {
        assertFalse(Validate.that("").nonEmpty("e").validate().isValid());
        assertFalse(Validate.that("").required("e").validate().isValid());
        assertTrue(Validate.that("x").notNull("e").validate().isValid());
    }

    @Test
    void lengthAliases() {
        assertFalse(Validate.that("ab").atMost(1, "too long").validate().isValid());
        assertFalse(Validate.that("ab").maxLen(1, "too long").validate().isValid());
        assertFalse(Validate.that("abc").atLeast(5, "too short").validate().isValid());
        assertFalse(Validate.that("abc").minLen(5, "too short").validate().isValid());
        assertFalse(Validate.that("abc").lengthInRange(5, 10, "invalid").validate().isValid());
    }

    @Test
    void emailAliases() {
        assertFalse(Validate.that("bad").email("invalid").validate().isValid());
        assertFalse(Validate.that("bad").looksLikeEmail("invalid").validate().isValid());
    }

    @Test
    void phoneAliases() {
        assertFalse(Validate.that("abc").phone("invalid").validate().isValid());
    }

    @Test
    void numericAliases() {
        assertFalse(Validate.that("abc").numeric("invalid").validate().isValid());
        assertFalse(Validate.that("abc").isNumber("invalid").validate().isValid());
    }

    @Test
    void integerAliases() {
        assertFalse(Validate.that("abc").isInt("invalid").validate().isValid());
        assertTrue(Validate.that("42").isInteger("invalid").validate().isValid());
    }

    @Test
    void urlAliases() {
        assertFalse(Validate.that("not a url").isUrl("invalid").validate().isValid());
    }

    // ---------------------------------------------------------- numeric range

    @Test
    void numericBetween() {
        assertTrue(Validate.that("25").between(1, 130, "out of range").validate().isValid());
        assertFalse(Validate.that("200").between(1, 130, "out of range").validate().isValid());
        assertFalse(Validate.that("abc").between(1, 130, "out of range").validate().isValid());
        assertFalse(Validate.that(null).between(1, 130, "out of range").validate().isValid());
    }

    @Test
    void numericMinMax() {
        assertTrue(Validate.that("50").min(1, "too low").max(100, "too high").validate().isValid());
        assertFalse(Validate.that("0").min(1, "too low").validate().isValid());
        assertFalse(Validate.that("101").max(100, "too high").validate().isValid());
    }

    // ---------------------------------------------------------- oneOf

    @Test
    void oneOfCheck() {
        assertTrue(Validate.that("red").isOneOf("invalid", "red", "green", "blue").validate().isValid());
        assertFalse(Validate.that("yellow").isOneOf("invalid", "red", "green", "blue").validate().isValid());
        assertFalse(Validate.that("red").oneOf("invalid", "green", "blue").validate().isValid());
    }

    // ---------------------------------------------------------- rule integration

    @Test
    void byRuleName() {
        assertTrue(Validate.that("user@example.com").is("EMAIL", "invalid").validate().isValid());
        assertFalse(Validate.that("not-an-email").is("EMAIL", "invalid").validate().isValid());
    }

    @Test
    void byRuleNameAlias() {
        assertTrue(Validate.that("user@example.com").rule("EMAIL", "invalid").validate().isValid());
    }

    @Test
    void byRuleObject() {
        Rule r = Rules.email();
        assertTrue(Validate.that("user@example.com").against(r, "invalid").validate().isValid());
        assertFalse(Validate.that("bad").against(r, "invalid").validate().isValid());
        assertTrue(Validate.that("user@example.com").using(r, "invalid").validate().isValid());
    }

    // ---------------------------------------------------------- custom checks

    @Test
    void customAliases() {
        assertFalse(Validate.that("x").failIf(1 == 2, "always fails").validate().isValid());
        assertTrue(Validate.that("x").custom(1 == 1, "passes").validate().isValid());
    }

    @Test
    void satisfiesAliases() {
        assertTrue(Validate.that("hi").satisfies(s -> s.length() < 5, "too long").validate().isValid());
        assertFalse(Validate.that("hi").when(s -> s.length() > 5, "too short").validate().isValid());
    }

    @Test
    void addErrorAliases() {
        ValidationResult r1 = Validate.that("x").addError("manual").validate();
        ValidationResult r2 = Validate.that("x").error("manual").validate();
        assertEquals("manual", r1.firstError());
        assertEquals("manual", r2.firstError());
    }

    // ---------------------------------------------------------- collects all errors

    @Test
    void collectsMultipleErrors() {
        ValidationResult r = Validate.of("Email", "")
                .notBlank("required")
                .minLength(3, "min 3")
                .isEmail("invalid")
                .validate();
        assertEquals(3, r.count());
        assertEquals("Email: required", r.firstError());
    }
}
