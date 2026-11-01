package chirp.validation;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Phase-2 test suite — alias parity, key-value factory, composite
 * factories, fluent required/optional, and every accessor.
 */
class RuleEnhancedTest {

    // ============================================================== factory aliases

    @Test
    void defineAliases() {
        Rule a = Rule.define("T").matches("\\d+").message("x").build();
        Rule b = Rule.of("T").matches("\\d+").message("x").build();
        Rule c = Rule.named("T").matches("\\d+").message("x").build();
        Rule d = Rule.create("T").matches("\\d+").message("x").build();
        assertTrue(a.test("123"));
        assertTrue(b.test("123"));
        assertTrue(c.test("123"));
        assertTrue(d.test("123"));
    }

    // ============================================================== test aliases

    @Test
    void testAliases() {
        Rule r = Rule.define("T").matches("\\d+").message("x").build();
        assertTrue(r.test("123"));
        assertTrue(r.passes("123"));
        assertTrue(r.isValid("123"));
        assertTrue(r.accepts("123"));
        assertTrue(r.ok("123"));
        assertFalse(r.fails("123"));
        assertFalse(r.isInvalid("123"));
        assertFalse(r.rejects("123"));
    }

    @Test
    void firstFailureAliases() {
        Rule r = Rule.define("T").matches("\\d+").message("digits only").build();
        Optional<String> a = r.firstFailure("abc");
        Optional<String> b = r.validate("abc");
        Optional<String> c = r.check("abc");
        Optional<String> d = r.problem("abc");
        Optional<String> e = r.firstProblem("abc");
        assertEquals(a, b);
        assertEquals(a, c);
        assertEquals(a, d);
        assertEquals(a, e);
        assertEquals("digits only", a.orElseThrow());
    }

    // ============================================================== composite factories

    @Test
    void compositeFactoryAliases() {
        Rule r1 = Rule.define("A").minLength(3).message("min 3").build();
        Rule r2 = Rule.define("B").maxLength(10).message("max 10").build();

        assertTrue(Rule.allOf(r1, r2).test("hello"));
        assertTrue(Rule.all(r1, r2).test("hello"));
        assertTrue(Rule.every(r1, r2).test("hello"));
        assertTrue(Rule.compose(r1, r2).test("hello"));
        assertTrue(Rule.requireAll(r1, r2).test("hello"));
    }

    @Test
    void anyAliases() {
        Rule r1 = Rule.define("A").matches("\\d+").message("d").build();
        Rule r2 = Rule.define("B").matches("[a-z]+").message("l").build();

        assertTrue(Rule.anyOf(r1, r2).test("123"));
        assertTrue(Rule.anyOf(r1, r2).test("abc"));
        assertTrue(Rule.any(r1, r2).test("123"));
        assertTrue(Rule.atLeastOne(r1, r2).test("abc"));
        assertTrue(Rule.requireAny(r1, r2).test("123"));
    }

    // ============================================================== none

    @Test
    void nonePassesWhenNoneMatch() {
        Rule digits = Rule.define("D").matches("\\d+").message("d").build();
        Rule spaces = Rule.define("S").contains(" ").message("s").build();
        Rule clean = Rule.none(digits, spaces);
        assertTrue(clean.test("abc"));
        assertFalse(clean.test("123"));
        assertFalse(clean.test("has space"));
    }

    @Test
    void noneOfAlias() {
        Rule r = Rule.define("R").contains("bad").message("bad").build();
        assertEquals(Rule.none(r).test("good"), Rule.noneOf(r).test("good"));
        assertEquals(Rule.none(r).test("bad"), Rule.excluding(r).test("bad"));
    }

    // ============================================================== atLeast

    @Test
    void atLeastPassesWithEnough() {
        Rule upper = Rule.define("UPPER").matches(".*[A-Z].*").message("upper").build();
        Rule lower = Rule.define("LOWER").matches(".*[a-z].*").message("lower").build();
        Rule digit = Rule.define("DIGIT").matches(".*\\d.*").message("digit").build();
        Rule symbol = Rule.define("SYMBOL").matches(".*[^A-Za-z0-9].*").message("symbol").build();

        Rule atLeast3 = Rule.atLeast(3, upper, lower, digit, symbol);

        assertTrue(atLeast3.test("Abc1"));       // upper + lower + digit = 3
        assertTrue(atLeast3.test("Abc1!"));      // 4 → also ≥ 3
        assertFalse(atLeast3.test("abc1"));      // lower + digit = 2
        assertFalse(atLeast3.test("abc"));       // 1
    }

    @Test
    void atLeastAliases() {
        Rule r1 = Rule.define("A").matches("\\d+").message("d").build();
        Rule r2 = Rule.define("B").matches("[a-z]+").message("l").build();
        assertEquals(Rule.atLeast(1, r1, r2).test("123"),
                Rule.atLeastN(1, r1, r2).test("123"));
        assertEquals(Rule.atLeast(1, r1, r2).test("abc"),
                Rule.minimumOf(1, r1, r2).test("abc"));
    }

    @Test
    void atLeastZeroAlwaysPasses() {
        Rule r = Rule.atLeast(0, Rule.define("X").matches("\\d+").build());
        assertTrue(r.test("anything"));
    }

    @Test
    void atLeastTooManyAlwaysFails() {
        Rule r = Rule.atLeast(3, Rule.define("X").matches("\\d+").build());
        assertFalse(r.test("123"));
    }

    // ============================================================== and / or / andNot

    @Test
    void andCombines() {
        Rule r1 = Rule.define("A").minLength(3).message("min 3").build();
        Rule r2 = Rule.define("B").maxLength(5).message("max 5").build();
        Rule combined = r1.and(r2);
        assertTrue(combined.test("abc"));
        assertFalse(combined.test("ab"));
        assertFalse(combined.test("abcdef"));
    }

    @Test
    void orCombines() {
        Rule r1 = Rule.define("A").matches("\\d+").message("d").build();
        Rule r2 = Rule.define("B").matches("[a-z]+").message("l").build();
        Rule combined = r1.or(r2);
        assertTrue(combined.test("123"));
        assertTrue(combined.test("abc"));
        assertFalse(combined.test("!@#"));
    }

    @Test
    void andNotCombines() {
        Rule lengthRule = Rule.define("LEN").minLength(3).message("min 3").build();
        Rule noDigits = Rule.define("NODIG").matches(".*\\d.*").message("no digits").build();

        Rule combined = lengthRule.andNot(noDigits);
        assertTrue(combined.test("abc"));       // len ≥ 3 and no digits
        assertFalse(combined.test("ab"));       // too short
        assertFalse(combined.test("abc1"));     // has digit
    }

    // ============================================================== key-value factory

    @Test
    void fromWithPattern() {
        Rule r = Rule.from(
                Rule.Key.NAME, "TEST",
                Rule.Key.PATTERN, "\\d+",
                Rule.Key.MESSAGE, "must be digits");
        assertTrue(r.test("123"));
        assertFalse(r.test("abc"));
        assertEquals("must be digits", r.firstFailure("abc").orElseThrow());
    }

    @Test
    void fromWithMinLength() {
        Rule r = Rule.from(
                Rule.Key.NAME, "TEST",
                Rule.Key.MIN_LENGTH, 5,
                Rule.Key.MESSAGE, "too short");
        assertTrue(r.test("hello"));
        assertFalse(r.test("hi"));
    }

    @Test
    void fromWithMultipleKeys() {
        Rule r = Rule.from(
                Rule.Key.NAME, "COMPLEX",
                Rule.Key.MIN_LENGTH, 3,
                Rule.Key.MAX_LENGTH, 10,
                Rule.Key.REQUIRED, true,
                Rule.Key.MESSAGE, "invalid");
        assertTrue(r.test("hello"));
        assertFalse(r.test("hi"));
        assertFalse(r.test("waytoolongvalue"));
    }

    @Test
    void fromRequiresName() {
        assertThrows(IllegalArgumentException.class,
                () -> Rule.from(Rule.Key.PATTERN, "\\d+"));
    }

    @Test
    void fromNullIsSafe() {
        assertThrows(IllegalArgumentException.class,
                () -> Rule.from((Object[]) null));
    }

    @Test
    void fromUnknownKeysIgnored() {
        Rule r = Rule.from(
                Rule.Key.NAME, "T",
                Rule.Key.PATTERN, "\\d+");
        assertNotNull(r);
    }

    @Test
    void fromWrongValueTypesIgnored() {
        Rule r = Rule.from(
                Rule.Key.NAME, "T",
                Rule.Key.MIN_LENGTH, "not a number",  // ignored
                Rule.Key.PATTERN, "\\d+");
        assertTrue(r.test("1"));                   // min length ignored
    }

    // ============================================================== accessor aliases

    @Test
    void accessorAliases() {
        Rule r = Rule.define("MY_RULE").matches("\\d+").message("digits").build();
        assertEquals("MY_RULE", r.name());
        assertEquals("MY_RULE", r.getName());
        assertEquals("MY_RULE", r.label());
        assertEquals("digits", r.message());
        assertEquals("digits", r.getMessage());
        assertTrue(r.isRequired());
    }

    // ============================================================== required / optional fluent

    @Test
    void optionalAliases() {
        Rule r = Rule.define("R").trim().matches("[a-z]+").message("x").build();
        assertTrue(r.required().isRequired());
        assertFalse(r.optional().isRequired());
        assertFalse(r.mayBeEmpty().isRequired());
        assertFalse(r.nullable().isRequired());
    }

    @Test
    void requiredReturnsNewRule() {
        Rule base = Rule.define("R").matches("[a-z]+").message("x").build();
        Rule opted = base.optional();
        assertFalse(opted.isRequired());
        assertTrue(base.isRequired());      // base unchanged
    }

    @Test
    void mandatoryAlias() {
        Rule r = Rule.define("R").matches("[a-z]+").message("x").build().optional();
        assertFalse(r.isRequired());
        assertTrue(r.mandatory().isRequired());
    }

    @Test
    void requiredIsFluent() {
        // required() returns a Rule (fluent), not a boolean
        Rule r = Rule.define("R").matches("[a-z]+").message("x").build().optional();
        Rule after = r.required();
        assertTrue(after.isRequired());
        assertFalse(r.isRequired());        // r itself untouched
    }
}
