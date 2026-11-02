package chirp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldNumericGettersTest {

    // ============================================================ intValue — success

    @Test
    void intValueParsesInteger() {
        Field f = Field.of("Age").value("25");
        assertEquals(25, f.intValue());
    }

    @Test
    void intValueHandlesNegative() {
        Field f = Field.of("Age").value("-7");
        assertEquals(-7, f.intValue());
    }

    @Test
    void intValueTrimsWhitespace() {
        Field f = Field.of("Age").value("  42  ");
        assertEquals(42, f.intValue());
    }

    // ============================================================ intValue — failure

    @Test
    void intValueThrowsOnEmpty() {
        Field f = Field.of("Age").value("");
        assertThrows(NumberFormatException.class, f::intValue);
    }

    @Test
    void intValueThrowsOnNonNumeric() {
        Field f = Field.of("Age").value("abc");
        assertThrows(NumberFormatException.class, f::intValue);
    }

    @Test
    void intValueThrowsOnDecimal() {
        Field f = Field.of("Age").value("3.14");
        assertThrows(NumberFormatException.class, f::intValue);
    }

    // ============================================================ doubleValue — success

    @Test
    void doubleValueParsesDecimal() {
        Field f = Field.of("Price").value("3.14");
        assertEquals(3.14, f.doubleValue(), 0.001);
    }

    @Test
    void doubleValueParsesInteger() {
        Field f = Field.of("Price").value("42");
        assertEquals(42.0, f.doubleValue(), 0.001);
    }

    @Test
    void doubleValueHandlesNegative() {
        Field f = Field.of("Price").value("-1.5");
        assertEquals(-1.5, f.doubleValue(), 0.001);
    }

    // ============================================================ doubleValue — failure

    @Test
    void doubleValueThrowsOnEmpty() {
        Field f = Field.of("Price").value("");
        assertThrows(NumberFormatException.class, f::doubleValue);
    }

    @Test
    void doubleValueThrowsOnNonNumeric() {
        Field f = Field.of("Price").value("abc");
        assertThrows(NumberFormatException.class, f::doubleValue);
    }

    // ============================================================ intValueOr

    @Test
    void intValueOrReturnsValueOnSuccess() {
        Field f = Field.of("Age").value("42");
        assertEquals(42, f.intValueOr(0));
    }

    @Test
    void intValueOrReturnsFallbackOnEmpty() {
        Field f = Field.of("Age").value("");
        assertEquals(-1, f.intValueOr(-1));
    }

    @Test
    void intValueOrReturnsFallbackOnInvalid() {
        Field f = Field.of("Age").value("abc");
        assertEquals(-1, f.intValueOr(-1));
    }

    @Test
    void intValueOrFallsBackWithZero() {
        Field f = Field.of("Age");
        assertEquals(0, f.intValueOr(0));
    }

    // ============================================================ doubleValueOr

    @Test
    void doubleValueOrReturnsValueOnSuccess() {
        Field f = Field.of("Price").value("3.14");
        assertEquals(3.14, f.doubleValueOr(0.0), 0.001);
    }

    @Test
    void doubleValueOrReturnsFallbackOnEmpty() {
        Field f = Field.of("Price").value("");
        assertEquals(-1.0, f.doubleValueOr(-1.0), 0.001);
    }

    @Test
    void doubleValueOrReturnsFallbackOnInvalid() {
        Field f = Field.of("Price").value("abc");
        assertEquals(-1.0, f.doubleValueOr(-1.0), 0.001);
    }

    // ============================================================ optionalIntValue

    @Test
    void optionalIntValuePresentOnSuccess() {
        Field f = Field.of("Age").value("42");
        assertTrue(f.optionalIntValue().isPresent());
        assertEquals(42, f.optionalIntValue().getAsInt());
    }

    @Test
    void optionalIntValueEmptyOnEmpty() {
        Field f = Field.of("Age").value("");
        assertTrue(f.optionalIntValue().isEmpty());
    }

    @Test
    void optionalIntValueEmptyOnInvalid() {
        Field f = Field.of("Age").value("abc");
        assertTrue(f.optionalIntValue().isEmpty());
    }

    @Test
    void optionalIntValueOrElse() {
        Field f = Field.of("Age");
        assertEquals(-1, f.optionalIntValue().orElse(-1));
    }

    // ============================================================ optionalDoubleValue

    @Test
    void optionalDoubleValuePresentOnSuccess() {
        Field f = Field.of("Price").value("3.14");
        assertTrue(f.optionalDoubleValue().isPresent());
        assertEquals(3.14, f.optionalDoubleValue().getAsDouble(), 0.001);
    }

    @Test
    void optionalDoubleValueEmptyOnEmpty() {
        Field f = Field.of("Price");
        assertTrue(f.optionalDoubleValue().isEmpty());
    }

    @Test
    void optionalDoubleValueEmptyOnInvalid() {
        Field f = Field.of("Price").value("abc");
        assertTrue(f.optionalDoubleValue().isEmpty());
    }

    // ============================================================ interaction with validator

    @Test
    void intValueSucceedsWhenNumericValidatorPasses() {
        Field f = Field.of("Age").numeric().value("25");
        assertTrue(f.isValid());
        assertEquals(25, f.intValue());
    }

    @Test
    void intValueThrowsWithoutNumericValidatorOnBadInput() {
        // No numeric() — the field accepts any text, so intValue() throws on non-numeric
        Field f = Field.of("Age").value("hello");
        assertThrows(NumberFormatException.class, f::intValue);
    }

    @Test
    void numericValidatorFailsOnDecimalForInt() {
        // numeric() accepts decimals; intValue() rejects them — that's fine,
        // the caller picks the getter that matches their data shape
        Field f = Field.of("Value").numeric().value("3.14");
        assertTrue(f.isValid());
        assertThrows(NumberFormatException.class, f::intValue);
        assertEquals(3.14, f.doubleValue(), 0.001);
    }

    // ============================================================ live typing simulation

    @Test
    void intValueOrHandlesProgressiveTyping() {
        Field f = Field.of("Age");
        f.value("");
        assertEquals(0, f.intValueOr(0));
        f.value("4");
        assertEquals(4, f.intValueOr(0));
        f.value("42");
        assertEquals(42, f.intValueOr(0));
    }

    @Test
    void optionalIntValueHandlesProgressiveTyping() {
        Field f = Field.of("Age");
        f.value("");
        assertTrue(f.optionalIntValue().isEmpty());
        f.value("4");
        assertTrue(f.optionalIntValue().isPresent());
        assertEquals(4, f.optionalIntValue().getAsInt());
    }

    // ============================================================ value() symmetry

    @Test
    void valueGetterMatchesSetter() {
        Field f = Field.of("Age").value("42");
        assertEquals("42", f.value());
    }

    @Test
    void valueSetterAcceptsNull() {
        Field f = Field.of("Age").value("42").value(null);
        assertEquals("", f.value());
    }

    @Test
    void numericGettersUseCurrentValue() {
        Field f = Field.of("Age").value("10");
        assertEquals(10, f.intValue());
        f.value("20");
        assertEquals(20, f.intValue());
    }

    // ============================================================ empty field defaults

    @Test
    void emptyFieldIntValueOrZero() {
        Field f = Field.of("Age");
        assertEquals(0, f.intValueOr(0));
    }

    @Test
    void emptyFieldOptionalIntEmpty() {
        Field f = Field.of("Age");
        assertTrue(f.optionalIntValue().isEmpty());
    }

    @Test
    void emptyFieldDoubleValueOrZero() {
        Field f = Field.of("Price");
        assertEquals(0.0, f.doubleValueOr(0.0), 0.001);
    }

    // ============================================================ chaining

    @Test
    void valueSetterIsChainable() {
        Field f = Field.of("Age").required().numeric().value("25");
        assertEquals("25", f.value());
        assertTrue(f.isValid());
    }
}
