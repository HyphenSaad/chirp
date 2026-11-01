package chirp.validation;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest {

    @Test
    void builderCreatesWorkingRule() {
        Rule rule = Rule.define("TEST")
                .trim()
                .stripAll("[\\s\\-]")
                .matches("\\d{4}")
                .message("Must be 4 digits")
                .build();
        assertTrue(rule.test("  1234  "));
        assertTrue(rule.test("12-34"));
        assertFalse(rule.test("123"));
    }

    @Test
    void optionalAllowsBlank() {
        Rule required = Rule.define("R").trim().minLength(3).message("x").build();
        Rule optional = required.optional();
        assertFalse(required.test(""));
        assertTrue(optional.test(""));
        assertTrue(optional.test("  "));
    }

    @Test
    void normalizeAppliesNormalizer() {
        Rule rule = Rule.define("N").stripNonDigits().matches("\\d+").message("x").build();
        assertEquals("123", rule.normalize(" 12a3 "));
    }

    @Test
    void firstFailureReturnsMessage() {
        Rule rule = Rule.define("M").matches("^[a-z]+$").message("lowercase only").build();
        Optional<String> result = rule.firstFailure("ABC");
        assertTrue(result.isPresent());
        assertEquals("lowercase only", result.get());
    }

    @Test
    void allOfComposition() {
        Rule lower = Rule.define("L").matches("[a-z]+").message("lower").build();
        Rule longer = Rule.define("N").minLength(3).message("long").build();
        Rule combined = Rule.allOf(lower, longer);
        assertTrue(combined.test("abc"));
        assertFalse(combined.test("AB"));
        assertFalse(combined.test("ab"));
    }

    @Test
    void anyOfComposition() {
        Rule digits = Rule.define("D").matches("\\d+").message("d").build();
        Rule alpha = Rule.define("A").matches("[a-z]+").message("a").build();
        Rule either = Rule.anyOf(digits, alpha);
        assertTrue(either.test("123"));
        assertTrue(either.test("abc"));
        assertFalse(either.test("!!!"));
    }

    @Test
    void notComposition() {
        Rule digits = Rule.define("D").matches("\\d+").message("d").build();
        Rule notDigits = Rule.not(digits);
        assertFalse(notDigits.test("123"));
        assertTrue(notDigits.test("abc"));
    }

    @Test
    void withMessageOverrides() {
        Rule rule = Rule.define("X").matches("\\d+").message("original").build();
        Rule replaced = rule.withMessage("custom message");
        assertEquals("custom message", replaced.firstFailure("abc").orElseThrow());
    }

    @Test
    void invalidNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> Rule.define(""));
        assertThrows(IllegalArgumentException.class, () -> Rule.define(null));
    }

    @Test
    void emptyPredicatesThrowsOnBuild() {
        assertThrows(IllegalStateException.class, () -> Rule.define("E").build());
    }
}
