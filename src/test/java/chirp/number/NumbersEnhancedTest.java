package chirp.number;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the expanded Numbers API — aliases, new methods, key-value
 * statistics/format/range, and interop with Format and NumberChain.
 */
class NumbersEnhancedTest {

    // ============================================================== aliases

    @Test
    void randomAliases() {
        for (int i = 0; i < 50; i++) {
            assertTrue(Numbers.random(1, 10) >= 1);
            assertTrue(Numbers.rand(1, 10) >= 1);
            assertTrue(Numbers.randomInt(1, 10) >= 1);
            assertTrue(Numbers.randomBetween(1, 10) >= 1);
            assertTrue(Numbers.randomFrom(1, 10) >= 1);
        }
    }

    @Test
    void randomItemAliases() {
        String[] array = {"a", "b", "c"};
        assertNotNull(Numbers.randomItem(array));
        assertNotNull(Numbers.pick(array));
        assertNotNull(Numbers.randomItem(List.of(1, 2, 3)));
    }

    @Test
    void randomItemEmptyReturnsNull() {
        assertNull(Numbers.randomItem(new String[0]));
        assertNull(Numbers.randomItem(List.of()));
    }

    @Test
    void clampAliases() {
        assertEquals(5, Numbers.clamp(10, 0, 5));
        assertEquals(5, Numbers.limit(10, 0, 5));
        assertEquals(5, Numbers.bound(10, 0, 5));
        assertEquals(5, Numbers.constrain(10, 0, 5));
    }

    @Test
    void wrap() {
        // [0, 5] is inclusive (like clamp) — a cycle of 6 values
        assertEquals(4, Numbers.wrap(10, 0, 5));
        assertEquals(5, Numbers.wrap(11, 0, 5));
        assertEquals(5, Numbers.wrap(-1, 0, 5));
    }

    // ============================================================== basics

    @Test
    void absFamily() {
        assertEquals(5, Numbers.abs(-5));
        assertEquals(5L, Numbers.abs(-5L));
        assertEquals(5.0, Numbers.abs(-5.0), 0.0);
    }

    @Test
    void signFamily() {
        assertEquals(1, Numbers.sign(42));
        assertEquals(-1, Numbers.sign(-42));
        assertEquals(0, Numbers.sign(0));
        assertEquals(1, Numbers.signum(42));
        assertEquals(-1, Numbers.signOf(-1));
    }

    @Test
    void modAlwaysNonNegative() {
        assertEquals(1, Numbers.mod(7, 3));
        assertEquals(2, Numbers.mod(-7, 3));    // -7 = 3 * -3 + 2, so 2 (Java's % gives -1)
        assertEquals(0, Numbers.mod(0, 5));
        assertEquals(0, Numbers.mod(5, 0));     // division by zero → 0
    }

    @Test
    void powFamily() {
        assertEquals(8.0, Numbers.pow(2, 3), 0.001);
        assertEquals(8.0, Numbers.power(2, 3), 0.001);
        assertEquals(8, Numbers.powInt(2, 3));
    }

    @Test
    void sqrtFamily() {
        assertEquals(3.0, Numbers.sqrt(9), 0.001);
        assertEquals(3.0, Numbers.squareRoot(9), 0.001);
        assertEquals(0.0, Numbers.sqrt(-1), 0.001);
    }

    @Test
    void cbrtFamily() {
        assertEquals(3.0, Numbers.cbrt(27), 0.001);
        assertEquals(3.0, Numbers.cubeRoot(27), 0.001);
    }

    @Test
    void hypotAndDistance() {
        assertEquals(5.0, Numbers.hypot(3, 4), 0.001);
        assertEquals(5.0, Numbers.distance(0, 0, 3, 4), 0.001);
    }

    @Test
    void logFamily() {
        assertEquals(1.0, Numbers.log10(10), 0.001);
        assertEquals(0.0, Numbers.log(0), 0.001);
        assertEquals(0.0, Numbers.log(-1), 0.001);
        assertEquals(3.0, Numbers.log2(8), 0.001);
        assertEquals(3.0, Numbers.logBase(8, 2), 0.001);
    }

    @Test
    void roundingFamily() {
        assertEquals(3, Numbers.floor(3.9));
        assertEquals(4, Numbers.ceil(3.1));
        assertEquals(4, Numbers.roundToInt(3.6));
        assertEquals(4L, Numbers.roundToLong(3.6));
    }

    @Test
    void isInteger() {
        assertTrue(Numbers.isInteger(42));
        assertTrue(Numbers.isInteger(42L));
        assertTrue(Numbers.isInteger(42.0));
        assertFalse(Numbers.isInteger(42.5));
        assertFalse(Numbers.isInteger("42"));
        assertTrue(Numbers.isWhole(42));
    }

    // ============================================================== trigonometry

    @Test
    void trig() {
        assertEquals(0.0, Numbers.sin(0), 0.001);
        assertEquals(1.0, Numbers.cos(0), 0.001);
        assertEquals(0.0, Numbers.tan(0), 0.001);
        assertEquals(0.0, Numbers.asin(0), 0.001);
        assertEquals(Math.PI, Numbers.toRadians(180), 0.001);
        assertEquals(180.0, Numbers.toDegrees(Math.PI), 0.001);
    }

    // ============================================================== number theory

    @Test
    void armstrongNumbers() {
        assertTrue(Numbers.isArmstrong(153));
        assertTrue(Numbers.isArmstrong(370));
        assertTrue(Numbers.isArmstrong(371));
        assertTrue(Numbers.isArmstrong(407));
        assertFalse(Numbers.isArmstrong(100));
        assertFalse(Numbers.isArmstrong(-1));
        assertTrue(Numbers.isNarcissistic(153));
    }

    @Test
    void palindromeNumbers() {
        assertTrue(Numbers.isPalindromeNumber(121));
        assertTrue(Numbers.isPalindromeNumber(12321));
        assertTrue(Numbers.isPalindromeNumber(0));
        assertFalse(Numbers.isPalindromeNumber(123));
        assertFalse(Numbers.isPalindromeNumber(-121));
        assertTrue(Numbers.isNumericPalindrome(121));
    }

    @Test
    void perfectNumbers() {
        assertTrue(Numbers.isPerfect(6));
        assertTrue(Numbers.isPerfect(28));
        assertTrue(Numbers.isPerfect(496));
        assertFalse(Numbers.isPerfect(100));
        assertFalse(Numbers.isPerfect(1));
    }

    @Test
    void divisors() {
        assertEquals(List.of(1, 2, 3, 4, 6, 12), Numbers.divisors(12));
        assertEquals(List.of(1, 2, 3, 4, 6, 12), Numbers.factors(12));
        assertEquals(List.of(), Numbers.divisors(0));
        assertEquals(List.of(), Numbers.divisors(-5));
    }

    @Test
    void countDivisorsAliases() {
        assertEquals(6, Numbers.countDivisors(12));
        assertEquals(28, Numbers.sumDivisors(12));
    }

    @Test
    void perfectCube() {
        assertTrue(Numbers.isPerfectCube(27));
        assertTrue(Numbers.isPerfectCube(64));
        assertFalse(Numbers.isPerfectCube(10));
        assertTrue(Numbers.isCube(27));
    }

    @Test
    void gcdAliases() {
        assertEquals(6, Numbers.gcd(48, 18));
        assertEquals(6, Numbers.hcf(48, 18));
    }

    @Test
    void factorialAliases() {
        assertEquals(120L, Numbers.factorial(5));
        assertEquals(120L, Numbers.fact(5));
    }

    // ============================================================== digits

    @Test
    void productDigits() {
        assertEquals(120, Numbers.productDigits(456));
        assertEquals(0, Numbers.productDigits(0));
        assertEquals(120, Numbers.digitProduct(456));
    }

    @Test
    void firstLastDigit() {
        assertEquals(1, Numbers.firstDigit(12345));
        assertEquals(5, Numbers.lastDigit(12345));
        assertEquals(1, Numbers.firstDigit(-12345));
        assertEquals(5, Numbers.lastDigit(-12345));
    }

    // ============================================================== bases

    @Test
    void baseHelpers() {
        assertEquals("1111", Numbers.binary(15));
        assertEquals("f", Numbers.hex(15));
        assertEquals("17", Numbers.octal(15));
        assertEquals("ff", Numbers.toBase(255, 16));
        assertEquals(255, Numbers.fromBase("ff", 16));
    }

    @Test
    void fromBaseInvalidInputs() {
        assertNull(Numbers.fromBase(null, 10));
        assertNull(Numbers.fromBase("abc", 2));
        assertNull(Numbers.fromBase("ff", 100));  // out of range
    }

    // ============================================================== parsing

    @Test
    void parseAliases() {
        assertEquals(42, Numbers.parseInt("42"));
        assertEquals(42, Numbers.toInt("42"));
        assertEquals(42, Numbers.asInt("42"));
        assertEquals(42, Numbers.intOrNull("42"));
        assertEquals(42, Numbers.tryParseInt("42"));
    }

    @Test
    void parseWithDefault() {
        assertEquals(0, Numbers.parseInt("abc", 0));
        assertEquals(42, Numbers.parseIntOr("abc", 42));
        assertEquals(42, Numbers.parseInt("42", 0));
    }

    @Test
    void parseDoubleAliases() {
        assertEquals(3.14, Numbers.parseDouble("3.14"), 0.001);
        assertEquals(3.14, Numbers.toDouble("3.14"), 0.001);
        assertEquals(3.14, Numbers.asDouble("3.14"), 0.001);
        assertEquals(3.14, Numbers.doubleOrNull("3.14"), 0.001);
    }

    @Test
    void parseLong() {
        assertEquals(1234567890L, Numbers.parseLong("1234567890"));
        assertNull(Numbers.parseLong("not a number"));
    }

    // ============================================================== formatting

    @Test
    void formatBasic() {
        assertEquals("3.14", Numbers.format(3.14159, 2));
        assertEquals("3.1", Numbers.fixed(3.14159, 1));
    }

    @Test
    void formatKeyValue() {
        assertEquals("3.14", Numbers.format(3.14159, Numbers.Key.DECIMALS, 2));
        assertEquals("1,234.50", Numbers.format(1234.5,
                Numbers.Key.DECIMALS, 2, Numbers.Key.THOUSANDS, true));
    }

    @Test
    void formatKeyValueNullsAreSafe() {
        assertDoesNotThrow(() -> Numbers.format(1.5, (Object[]) null));
        assertDoesNotThrow(() -> Numbers.format(1.5, Numbers.Key.DECIMALS, null));
        assertDoesNotThrow(() -> Numbers.format(1.5, "not a key", 2));
    }

    @Test
    void abbreviate() {
        assertEquals("999", Numbers.abbreviate(999));
        assertEquals("1K", Numbers.abbreviate(1_000));
        assertEquals("1.5K", Numbers.abbreviate(1_500));
        assertEquals("1M", Numbers.abbreviate(1_000_000));
        assertEquals("1.5M", Numbers.abbreviate(1_500_000));
        assertEquals("1B", Numbers.abbreviate(1_000_000_000));
        assertEquals("2.5B", Numbers.abbreviate(2_500_000_000L));
        assertEquals("1T", Numbers.abbreviate(1_000_000_000_000L));
        assertEquals("-1.5K", Numbers.abbreviate(-1_500));
        assertEquals("1.5K", Numbers.compact(1_500));
    }

    @Test
    void fileSize() {
        assertEquals("512 B", Numbers.fileSize(512));
        assertEquals("1.0 KB", Numbers.fileSize(1024));
        assertEquals("1.5 KB", Numbers.fileSize(1536));
        assertEquals("1.0 MB", Numbers.fileSize(1024L * 1024));
        assertEquals("2.5 GB", Numbers.fileSize(2560L * 1024 * 1024));  // 2560 MB = 2.5 GB
        assertEquals("0 B", Numbers.fileSize(-1));
        assertEquals("1.0 KB", Numbers.humanSize(1024));
    }

    @Test
    void thousands() {
        assertEquals("1,234", Numbers.thousands(1_234));
        assertEquals("1,234,567", Numbers.thousands(1_234_567));
        assertEquals("-1,234", Numbers.thousands(-1_234));
        assertEquals("1,234", Numbers.withCommas(1_234));
        assertEquals("1,234", Numbers.grouped(1_234));
    }

    @Test
    void roman() {
        assertEquals("I", Numbers.roman(1));
        assertEquals("IV", Numbers.roman(4));
        assertEquals("IX", Numbers.roman(9));
        assertEquals("XIV", Numbers.roman(14));
        assertEquals("MCMXCIV", Numbers.roman(1994));
        assertEquals("MMMCMXCIX", Numbers.roman(3999));
        assertEquals("0", Numbers.roman(0));
        assertEquals("4000", Numbers.roman(4000));
        assertEquals("I", Numbers.toRoman(1));
    }

    @Test
    void ordinalAliases() {
        assertEquals("1st", Numbers.ordinal(1));
        assertEquals("2nd", Numbers.ordinal(2));
        assertEquals("11th", Numbers.ordinal(11));
    }

    // ============================================================== ranges

    @Test
    void rangeKeyValue() {
        assertEquals(List.of(0, 2, 4, 6, 8),
                Numbers.range(0, 10, Numbers.Key.STEP, 2));
        assertEquals(List.of(0, 1, 2, 3, 4),
                Numbers.range(0, 5));
    }

    @Test
    void rangeAliases() {
        assertEquals(List.of(0, 1, 2, 3, 4), Numbers.to(0, 5));
        assertEquals(List.of(0, 1, 2, 3, 4), Numbers.span(0, 5));
        assertEquals(List.of(0, 1, 2, 3, 4), Numbers.upto(0, 5));
    }

    @Test
    void rangeInclusiveAliases() {
        assertEquals(List.of(1, 2, 3, 4, 5), Numbers.rangeInclusive(1, 5));
        assertEquals(List.of(1, 2, 3, 4, 5), Numbers.rangeClosed(1, 5));
    }

    @Test
    void rangeArray() {
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, Numbers.rangeArray(0, 5));
        assertArrayEquals(new int[]{0, 2, 4}, Numbers.rangeArray(0, 6, 2));
    }

    // ============================================================== aggregation

    @Test
    void sumAliases() {
        int[] v = {1, 2, 3, 4, 5};
        assertEquals(15, Numbers.sum(v));
        assertEquals(15, Numbers.total(v));
        assertEquals(15, Numbers.sumOf(v));
    }

    @Test
    void sumRange() {
        assertEquals(3, Numbers.sum(new int[]{1, 2, 3, 4, 5}, 0, 2));
        assertEquals(12, Numbers.sum(new int[]{1, 2, 3, 4, 5}, 2, 5));
    }

    @Test
    void sumDouble() {
        assertEquals(6.0, Numbers.sum(new double[]{1.5, 2.5, 2.0}), 0.001);
    }

    @Test
    void averageAliases() {
        int[] v = {1, 2, 3, 4, 5};
        assertEquals(3.0, Numbers.average(v), 0.001);
        assertEquals(3.0, Numbers.mean(v), 0.001);
        assertEquals(3.0, Numbers.avg(v), 0.001);
        assertEquals(3.0, Numbers.averageOf(v), 0.001);
    }

    @Test
    void maxMinAliases() {
        int[] v = {3, 1, 4, 1, 5};
        assertEquals(5, Numbers.max(v));
        assertEquals(5, Numbers.maxOf(v));
        assertEquals(1, Numbers.min(v));
        assertEquals(1, Numbers.minOf(v));
    }

    @Test
    void countAndContainsAliases() {
        int[] v = {1, 2, 1, 3, 1};
        assertEquals(3, Numbers.count(v, 1));
        assertEquals(3, Numbers.countOf(v, 1));
        assertTrue(Numbers.contains(v, 3));
        assertTrue(Numbers.has(v, 3));
        assertFalse(Numbers.contains(v, 99));
    }

    // ============================================================== statistics

    @Test
    void medianDirect() {
        assertEquals(3.0, Numbers.median(new int[]{1, 2, 3, 4, 5}), 0.001);
        assertEquals(2.5, Numbers.median(new int[]{1, 2, 3, 4}), 0.001);
        assertEquals(0.0, Numbers.median(null), 0.0);
    }

    @Test
    void modeDirect() {
        assertEquals(1, Numbers.mode(new int[]{1, 1, 2, 3}));
    }

    @Test
    void sampleStdDev() {
        // Sample stddev of {2, 4, 4, 4, 5, 5, 7, 9} — population is 2.0, sample is ~2.138
        double sample = Numbers.sampleStdDev(new int[]{2, 4, 4, 4, 5, 5, 7, 9});
        assertTrue(sample > 2.0 && sample < 2.5, "Expected ~2.138, got " + sample);
    }

    @Test
    void varianceFamily() {
        double v = Numbers.variance(new int[]{1, 2, 3, 4, 5});
        assertEquals(2.0, v, 0.001);  // population variance
        double sv = Numbers.sampleVariance(new int[]{1, 2, 3, 4, 5});
        assertEquals(2.5, sv, 0.001); // sample variance
    }

    @Test
    void statisticsWithSampleOption() {
        int[] data = {2, 4, 4, 4, 5, 5, 7, 9};
        Numbers.Statistics pop = Numbers.statistics(data);
        Numbers.Statistics smp = Numbers.statistics(data, Numbers.Key.SAMPLE, true);
        assertTrue(smp.stdDev() > pop.stdDev());
    }

    // ============================================================== percent

    @Test
    void percentAliases() {
        assertEquals(25.0, Numbers.percent(25, 100), 0.001);
        assertEquals(25.0, Numbers.percentage(25, 100), 0.001);
        assertEquals(25.0, Numbers.pct(25, 100), 0.001);
        assertEquals(0.0, Numbers.percent(0, 0), 0.001);
    }

    @Test
    void applyPercent() {
        assertEquals(25.0, Numbers.applyPercent(100, 25), 0.001);
    }
}
