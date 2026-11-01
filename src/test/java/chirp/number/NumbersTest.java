package chirp.number;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NumbersTest {

    @Test
    void round() { assertEquals(3.14, Numbers.round(3.14159, 2), 0.0001); }
    @Test
    void parseIntInvalid() { assertNull(Numbers.parseInt("abc")); assertNull(Numbers.parseInt(null)); }
    @Test
    void parseDoubleValid() { assertEquals(3.14, Numbers.parseDouble("3.14"), 0.0001); }

    @Test
    void clamp() {
        assertEquals(5, Numbers.clamp(10, 0, 5));
        assertEquals(0, Numbers.clamp(-5, 0, 5));
    }

    @Test
    void range() { assertEquals(List.of(1, 2, 3, 4), Numbers.range(1, 5)); }
    @Test
    void rangeWithStep() { assertEquals(List.of(1, 3, 5), Numbers.range(1, 6, 2)); }
    @Test
    void rangeInclusive() { assertEquals(List.of(1, 2, 3, 4, 5), Numbers.rangeInclusive(1, 5)); }

    @Test
    void randomWithinBounds() {
        for (int i = 0; i < 100; i++) {
            int v = Numbers.random(1, 10);
            assertTrue(v >= 1 && v <= 10);
        }
    }

    @Test
    void isPrime() {
        assertTrue(Numbers.isPrime(7));
        assertTrue(Numbers.isPrime(2));
        assertFalse(Numbers.isPrime(1));
        assertFalse(Numbers.isPrime(9));
    }

    @Test
    void primesUpTo() { assertEquals(List.of(2, 3, 5, 7), Numbers.primesUpTo(10)); }
    @Test
    void nextPrime() { assertEquals(11, Numbers.nextPrime(7)); }

    @Test
    void factorial() {
        assertEquals(120L, Numbers.factorial(5));
        assertEquals(1L, Numbers.factorial(0));
    }

    @Test
    void fibonacci() {
        assertEquals(0, Numbers.fibonacci(0));
        assertEquals(1, Numbers.fibonacci(1));
        assertEquals(55, Numbers.fibonacci(10));
    }

    @Test
    void gcd() {
        assertEquals(6, Numbers.gcd(48, 18));
        assertEquals(5, Numbers.gcd(-10, 15));
    }

    @Test
    void lcm() { assertEquals(12, Numbers.lcm(4, 6)); }
    @Test
    void sumDigits() { assertEquals(15, Numbers.sumDigits(12345)); }
    @Test
    void reverseDigits() { assertEquals(321, Numbers.reverseDigits(123)); }

    @Test
    void isPerfectSquare() {
        assertTrue(Numbers.isPerfectSquare(16));
        assertFalse(Numbers.isPerfectSquare(15));
    }

    @Test
    void baseConversions() {
        assertEquals("1111", Numbers.toBinary(15));
        assertEquals("f", Numbers.toHex(15));
        assertEquals(15, Numbers.fromBinary("1111"));
        assertEquals(15, Numbers.fromHex("f"));
    }

    @Test
    void percent() { assertEquals(25.0, Numbers.percent(25, 100), 0.001); }
}
