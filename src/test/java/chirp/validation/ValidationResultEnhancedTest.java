package chirp.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ValidationResult}.
 *
 * <p>
 * <b>Important pattern:</b> every {@code assertTrue(...)} and
 * {@code assertFalse(...)} call passes a <b>boolean</b> — never a
 * {@link ValidationResult} object. Always read the status via an
 * instance accessor such as {@code .isValid()}, {@code .hasErrors()},
 * or {@code .isInvalid()}.
 * </p>
 *
 * <p>
 * In particular, do <b>not</b> write
 * {@code assertTrue(ValidationResult.valid())} — that static factory
 * returns a {@code ValidationResult}, not a {@code boolean}. Write
 * {@code assertTrue(ValidationResult.valid().isValid())} instead.
 * </p>
 */
class ValidationResultEnhancedTest {

    // ============================================================== factories

    @Test
    void validFactory() {
        assertTrue(ValidationResult.valid().isValid());
        assertTrue(ValidationResult.success().isValid());
        assertTrue(ValidationResult.empty().isValid());
        assertTrue(ValidationResult.clean().isValid());
    }

    @Test
    void ofFactory() {
        ValidationResult r = ValidationResult.of("err1", "err2");
        assertEquals(2, r.count());
    }

    @Test
    void errorsFactory() {
        ValidationResult r = ValidationResult.errors("err1");
        assertEquals(1, r.count());
    }

    // ============================================================== status aliases

    @Test
    void statusAliasesEmpty() {
        ValidationResult empty = ValidationResult.valid();
        assertFalse(empty.hasErrors());
        assertFalse(empty.hasError());
        assertFalse(empty.isInvalid());
        assertFalse(empty.isNotValid());
        assertFalse(empty.failed());

        assertTrue(empty.isValid());
        // assertTrue(empty.valid());
        assertTrue(empty.ok());
        assertTrue(empty.passed());
        assertTrue(empty.isEmpty());
    }

    @Test
    void statusAliasesInvalid() {
        ValidationResult bad = ValidationResult.of("e");
        assertTrue(bad.hasErrors());
        assertTrue(bad.isInvalid());
        assertTrue(bad.failed());
        assertFalse(bad.isValid());
    }

    // ============================================================== accessor
    // aliases

    @Test
    void listAccessorAliases() {
        ValidationResult r = ValidationResult.of("a", "b", "c");
        assertEquals(List.of("a", "b", "c"), r.errors());
        assertEquals(List.of("a", "b", "c"), r.all());
        assertEquals(List.of("a", "b", "c"), r.messages());
        assertEquals(List.of("a", "b", "c"), r.asList());
        assertEquals(List.of("a", "b", "c"), r.getErrors());
    }

    @Test
    void streamAccessor() {
        ValidationResult r = ValidationResult.of("a", "b");
        assertEquals(2, r.stream().count());
        assertEquals(2, r.toStream().count());
    }

    @Test
    void countAliases() {
        ValidationResult r = ValidationResult.of("a", "b");
        assertEquals(2, r.count());
        assertEquals(2, r.errorCount());
        assertEquals(2, r.size());
        assertEquals(2, r.length());
    }

    // ============================================================== first / last

    @Test
    void firstAliases() {
        ValidationResult r = ValidationResult.of("first", "second");
        assertEquals("first", r.firstError());
        assertEquals("first", r.first());
        assertEquals("first", r.firstMessage());
    }

    @Test
    void lastAliases() {
        ValidationResult r = ValidationResult.of("first", "last");
        assertEquals("last", r.lastError());
        assertEquals("last", r.last());
    }

    @Test
    void firstOrDefault() {
        assertEquals("x", ValidationResult.of().firstOrDefault("x"));
        assertEquals("y", ValidationResult.of("y", "z").firstOrDefault("x"));
    }

    // ============================================================== formatting

    @Test
    void joinAliases() {
        ValidationResult r = ValidationResult.of("a", "b", "c");
        assertEquals("a | b | c", r.join(" | "));
        assertEquals("a | b | c", r.joined(" | "));
        assertEquals("a; b; c", r.toLine());
        assertEquals("a | b | c", r.oneLine());
    }

    @Test
    void printDoesNotThrow() {
        assertDoesNotThrow(() -> ValidationResult.of("err").print());
        assertDoesNotThrow(() -> ValidationResult.of("err").print("[CUSTOM] "));
        assertDoesNotThrow(() -> ValidationResult.of("err").printAll());
    }

    // ============================================================== merge

    @Test
    void mergeTwoResults() {
        ValidationResult a = ValidationResult.of("e1");
        ValidationResult b = ValidationResult.of("e2", "e3");
        ValidationResult merged = a.merge(b);
        assertEquals(3, merged.count());
        assertEquals(List.of("e1", "e2", "e3"), merged.errors());
    }

    @Test
    void mergeWithEmpty() {
        ValidationResult a = ValidationResult.of("e1");
        ValidationResult empty = ValidationResult.valid();
        assertEquals(1, a.merge(empty).count());
        assertEquals(1, empty.merge(a).count());
    }

    @Test
    void mergeNull() {
        ValidationResult a = ValidationResult.of("e1");
        assertEquals(a, a.merge(null));
    }

    @Test
    void mergeAliases() {
        ValidationResult a = ValidationResult.of("x");
        ValidationResult b = ValidationResult.of("y");
        assertEquals(2, a.and(b).count());
        assertEquals(2, a.plus(b).count());
    }

    // ============================================================== toString

    @Test
    void toStringForValid() {
        assertTrue(ValidationResult.valid().toString().contains("valid"));
    }

    @Test
    void toStringForInvalid() {
        assertTrue(ValidationResult.of("e").toString().contains("e"));
    }
}
