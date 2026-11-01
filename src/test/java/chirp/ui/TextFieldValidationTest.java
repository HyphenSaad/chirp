package chirp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests validation integration — the bridge between the UI module and
 * {@link chirp.text.Check} / {@link chirp.validation.Rules}.
 */
class TextFieldValidationTest {

    // ---------------------------------------------------------- required

    @Test
    void requiredFailsOnEmpty() {
        TextField f = TextField.of().required();
        assertFalse(f.isValid());
        assertNotNull(f.error());
    }

    @Test
    void requiredPassesWithContent() {
        TextField f = TextField.of().required().text("hello");
        assertTrue(f.isValid());
        assertNull(f.error());
    }

    @Test
    void requiredFailsOnBlank() {
        TextField f = TextField.of().required().text("   ");
        assertFalse(f.isValid());
    }

    // ---------------------------------------------------------- shorthands

    @Test
    void emailRejectsInvalid() {
        TextField f = TextField.of().email().text("not-an-email");
        assertFalse(f.isValid());
    }

    @Test
    void emailAcceptsValid() {
        TextField f = TextField.of().email().text("user@example.com");
        assertTrue(f.isValid());
    }

    @Test
    void phoneRejectsInvalid() {
        TextField f = TextField.of().phone().text("abc");
        assertFalse(f.isValid());
    }

    @Test
    void urlRejectsInvalid() {
        TextField f = TextField.of().url().text("not a url");
        assertFalse(f.isValid());
    }

    @Test
    void numericRejectsInvalid() {
        TextField f = TextField.of().numeric().text("abc");
        assertFalse(f.isValid());
    }

    @Test
    void numericAcceptsValid() {
        TextField f = TextField.of().numeric().text("42.5");
        assertTrue(f.isValid());
    }

    // ---------------------------------------------------------- custom predicate

    @Test
    void customPredicate() {
        TextField f = TextField.of()
                .validate(s -> s != null && s.length() >= 3, "too short");
        f.text("ab");
        assertFalse(f.isValid());
        assertEquals("too short", f.error());
        f.text("abcd");
        assertTrue(f.isValid());
    }

    // ---------------------------------------------------------- rules registry

    @Test
    void namedRuleWorks() {
        TextField f = TextField.of().validate("PK_CNIC");
        f.text("35202-1234567-1");
        assertTrue(f.isValid());
        f.text("invalid");
        assertFalse(f.isValid());
    }

    @Test
    void namedRuleEmail() {
        TextField f = TextField.of().validate("EMAIL");
        f.text("user@example.com");
        assertTrue(f.isValid());
    }

    // ---------------------------------------------------------- chaining

    @Test
    void multipleValidatorsLastWins() {
        TextField f = TextField.of()
                .required()
                .email();
        f.text("not-email");
        assertFalse(f.isValid());   // last validator is email
    }

    @Test
    void checkForcesEvaluation() {
        TextField f = TextField.of().required();
        f.text("");
        assertFalse(f.check());
        f.text("hello");
        assertTrue(f.check());
    }

    @Test
    void clearValidationRemovesValidator() {
        TextField f = TextField.of().required().text("");
        assertFalse(f.isValid());
        f.clearValidation();
        assertTrue(f.isValid());
    }

    // ---------------------------------------------------------- invalid CSS class

    @Test
    void invalidAddsCssClass() {
        TextField f = TextField.of().required();
        assertTrue(f.getStyleClass().contains("invalid"));
    }

    @Test
    void validRemovesCssClass() {
        TextField f = TextField.of().required().text("hello");
        assertFalse(f.getStyleClass().contains("invalid"));
    }

    // ---------------------------------------------------------- select

    @Test
    void selectRequiredFailsEmpty() {
        Select s = Select.of("A", "B").required();
        assertFalse(s.isValid());
        s.selected("A");
        assertTrue(s.isValid());
    }

    // ---------------------------------------------------------- checkbox

    @Test
    void checkboxRequiredFailsUnchecked() {
        Checkbox c = Checkbox.of("Agree").required();
        assertFalse(c.isValid());
        c.selected(true);
        assertTrue(c.isValid());
    }
}
