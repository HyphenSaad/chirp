package chirp.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateTest {

    @Test
    void validInput() {
        ValidationResult r = Validate.that("hello")
                .notBlank("empty")
                .maxLength(10, "too long")
                .validate();
        assertFalse(r.hasErrors());
        assertTrue(r.isValid());
    }

    @Test
    void collectsAllErrors() {
        ValidationResult r = Validate.that("")
                .notBlank("empty")
                .minLength(5, "too short")
                .validate();
        assertTrue(r.hasErrors());
        assertEquals(2, r.errors().size());
        assertEquals("empty", r.firstError());
    }

    @Test
    void regexMatch() {
        ValidationResult r = Validate.that("abc123")
                .matches("[a-z]+", "letters only")
                .validate();
        assertTrue(r.hasErrors());
    }

    @Test
    void emailValidation() {
        ValidationResult r = Validate.that("not-an-email")
                .isEmail("bad email")
                .validate();
        assertTrue(r.hasErrors());
    }

    @Test
    void joinMessages() {
        ValidationResult r = Validate.that("")
                .notBlank("a")
                .minLength(5, "b")
                .validate();
        assertEquals("a | b", r.join(" | "));
    }

    @Test
    void custom() {
        ValidationResult r = Validate.that("x")
                .custom(1 == 2, "impossible")
                .validate();
        assertTrue(r.hasErrors());
    }
}
