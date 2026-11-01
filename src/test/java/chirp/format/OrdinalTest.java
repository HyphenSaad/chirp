package chirp.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdinalTest {

    @Test
    void basicOrdinals() {
        assertEquals("1st", Format.ordinal(1));
        assertEquals("2nd", Format.ordinal(2));
        assertEquals("3rd", Format.ordinal(3));
        assertEquals("4th", Format.ordinal(4));
        assertEquals("5th", Format.ordinal(5));
        assertEquals("6th", Format.ordinal(6));
        assertEquals("7th", Format.ordinal(7));
        assertEquals("8th", Format.ordinal(8));
        assertEquals("9th", Format.ordinal(9));
        assertEquals("10th", Format.ordinal(10));
    }

    @Test
    void teensAreAllTh() {
        assertEquals("11th", Format.ordinal(11));
        assertEquals("12th", Format.ordinal(12));
        assertEquals("13th", Format.ordinal(13));
        assertEquals("14th", Format.ordinal(14));
        assertEquals("15th", Format.ordinal(15));
        assertEquals("19th", Format.ordinal(19));
    }

    @Test
    void twentiesRange() {
        assertEquals("20th", Format.ordinal(20));
        assertEquals("21st", Format.ordinal(21));
        assertEquals("22nd", Format.ordinal(22));
        assertEquals("23rd", Format.ordinal(23));
        assertEquals("24th", Format.ordinal(24));
        assertEquals("30th", Format.ordinal(30));
    }

    @Test
    void hundredsRange() {
        assertEquals("100th", Format.ordinal(100));
        assertEquals("101st", Format.ordinal(101));
        assertEquals("102nd", Format.ordinal(102));
        assertEquals("103rd", Format.ordinal(103));
        assertEquals("110th", Format.ordinal(110));
        assertEquals("111th", Format.ordinal(111));
        assertEquals("112th", Format.ordinal(112));
        assertEquals("113th", Format.ordinal(113));
        assertEquals("121st", Format.ordinal(121));
    }

    @Test
    void zeroIsTh() {
        assertEquals("0th", Format.ordinal(0));
    }

    @Test
    void negativeNumbersGetSameSuffix() {
        assertEquals("-1st", Format.ordinal(-1));
        assertEquals("-2nd", Format.ordinal(-2));
        assertEquals("-3rd", Format.ordinal(-3));
        assertEquals("-4th", Format.ordinal(-4));
        assertEquals("-11th", Format.ordinal(-11));
        assertEquals("-21st", Format.ordinal(-21));
    }
}
