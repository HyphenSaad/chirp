package chirp.number;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {

    @Test
    void nullInputReturnsZeros() {
        Numbers.Statistics stats = Numbers.statistics(null);
        assertEquals(0L, stats.count());
        assertEquals(0L, stats.sum());
        assertEquals(0.0, stats.average());
        assertEquals(0, stats.min());
        assertEquals(0, stats.max());
        assertEquals(0.0, stats.stdDev());
        assertEquals(0.0, stats.median());
        assertEquals(0, stats.mode());
    }

    @Test
    void emptyInputReturnsZeros() {
        Numbers.Statistics stats = Numbers.statistics(new int[0]);
        assertEquals(0L, stats.count());
    }

    @Test
    void singleElement() {
        Numbers.Statistics stats = Numbers.statistics(new int[]{42});
        assertEquals(1L, stats.count());
        assertEquals(42L, stats.sum());
        assertEquals(42.0, stats.average());
        assertEquals(42, stats.min());
        assertEquals(42, stats.max());
        assertEquals(0.0, stats.stdDev());
        assertEquals(42.0, stats.median());
        assertEquals(42, stats.mode());
    }

    @Test
    void simpleSet() {
        // {1, 2, 3, 4, 5}
        Numbers.Statistics stats = Numbers.statistics(new int[]{1, 2, 3, 4, 5});
        assertEquals(5L, stats.count());
        assertEquals(15L, stats.sum());
        assertEquals(3.0, stats.average());
        assertEquals(1, stats.min());
        assertEquals(5, stats.max());
        assertEquals(3.0, stats.median());
        assertEquals(Math.sqrt(2), stats.stdDev(), 0.0001); // population variance = 2
    }

    @Test
    void evenCountMedianIsAverageOfMiddles() {
        // {1, 2, 3, 4} -> median = 2.5
        Numbers.Statistics stats = Numbers.statistics(new int[]{1, 2, 3, 4});
        assertEquals(2.5, stats.median());
    }

    @Test
    void modeIsMostFrequent() {
        // {1, 2, 2, 3, 3, 3, 4} -> mode = 3
        Numbers.Statistics stats = Numbers.statistics(new int[]{1, 2, 2, 3, 3, 3, 4});
        assertEquals(3, stats.mode());
    }

    @Test
    void modeTieBreaksSmallest() {
        // {5, 5, 3, 3} -> both appear twice; 3 is smaller
        Numbers.Statistics stats = Numbers.statistics(new int[]{5, 5, 3, 3});
        assertEquals(3, stats.mode());
    }

    @Test
    void allSameValuesHaveZeroStdDev() {
        Numbers.Statistics stats = Numbers.statistics(new int[]{7, 7, 7, 7});
        assertEquals(0.0, stats.stdDev());
    }

    @Test
    void negativeNumbers() {
        Numbers.Statistics stats = Numbers.statistics(new int[]{-3, -1, -2});
        assertEquals(-6L, stats.sum());
        assertEquals(-2.0, stats.average());
        assertEquals(-3, stats.min());
        assertEquals(-1, stats.max());
    }

    @Test
    void largerSetStdDev() {
        // {2, 4, 4, 4, 5, 5, 7, 9} -> mean=5, population variance=4, stdDev=2
        Numbers.Statistics stats = Numbers.statistics(new int[]{2, 4, 4, 4, 5, 5, 7, 9});
        assertEquals(5.0, stats.average());
        assertEquals(2.0, stats.stdDev(), 0.0001);
        assertEquals(4.5, stats.median(), 0.0001);
    }
}
