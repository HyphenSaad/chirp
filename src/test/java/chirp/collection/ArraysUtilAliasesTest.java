package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArraysUtilAliasesTest {

    private final int[] sample = {3, 1, 4, 1, 5, 9, 2, 6};

    // ---------------------------------------------------------- aggregation aliases

    @Test
    void sumAliases() {
        assertEquals(ArraysUtil.sum(sample), ArraysUtil.total(sample));
        assertEquals(ArraysUtil.sum(sample), ArraysUtil.sumOf(sample));
    }

    @Test
    void avgAliases() {
        assertEquals(ArraysUtil.average(sample), ArraysUtil.avg(sample), 0.0001);
        assertEquals(ArraysUtil.average(sample), ArraysUtil.mean(sample), 0.0001);
    }

    @Test
    void maxAliases() {
        assertEquals(ArraysUtil.max(sample), ArraysUtil.largest(sample));
        assertEquals(ArraysUtil.max(sample), ArraysUtil.maxOf(sample));
    }

    @Test
    void minAliases() {
        assertEquals(ArraysUtil.min(sample), ArraysUtil.smallest(sample));
        assertEquals(ArraysUtil.min(sample), ArraysUtil.minOf(sample));
    }

    @Test
    void countAliases() {
        assertEquals(ArraysUtil.count(sample, 1), ArraysUtil.countOf(sample, 1));
        assertEquals(ArraysUtil.count(sample, 1), ArraysUtil.occurrences(sample, 1));
    }

    // ---------------------------------------------------------- search aliases

    @Test
    void containsAliases() {
        assertTrue(ArraysUtil.contains(sample, 5));
        assertTrue(ArraysUtil.includes(sample, 5));
        assertTrue(ArraysUtil.has(sample, 5));
    }

    @Test
    void indexAliases() {
        assertEquals(ArraysUtil.indexOf(sample, 9), ArraysUtil.findIndex(sample, 9));
        assertEquals(ArraysUtil.indexOf(sample, 9), ArraysUtil.firstIndexOf(sample, 9));
    }

    // ---------------------------------------------------------- transform aliases

    @Test
    void reverseAliases() {
        assertArrayEquals(ArraysUtil.reverse(sample), ArraysUtil.reversed(sample));
        assertArrayEquals(ArraysUtil.reverse(sample), ArraysUtil.flipped(sample));
    }

    @Test
    void sortedAliases() {
        assertArrayEquals(ArraysUtil.sorted(sample), ArraysUtil.sort(sample));
        assertArrayEquals(ArraysUtil.sorted(sample), ArraysUtil.ascending(sample));
        assertArrayEquals(ArraysUtil.sorted(sample), ArraysUtil.sortAscending(sample));
    }

    @Test
    void sortedDescendingAliases() {
        assertArrayEquals(ArraysUtil.sortedDescending(sample), ArraysUtil.descending(sample));
        assertArrayEquals(ArraysUtil.sortedDescending(sample), ArraysUtil.sortDescending(sample));
    }

    @Test
    void distinctAliases() {
        int[] withDupes = {1, 2, 1, 3, 2};
        assertArrayEquals(ArraysUtil.distinct(withDupes), ArraysUtil.unique(withDupes));
        assertArrayEquals(ArraysUtil.distinct(withDupes), ArraysUtil.dedupe(withDupes));
    }

    @Test
    void rotateAliases() {
        assertArrayEquals(ArraysUtil.rotateRight(sample, 2), ArraysUtil.rotate(sample, 2));
        assertArrayEquals(ArraysUtil.rotateRight(sample, 2), ArraysUtil.shifted(sample, 2));
        assertArrayEquals(ArraysUtil.rotateLeft(sample, 2), ArraysUtil.rotateBack(sample, 2));
        assertArrayEquals(ArraysUtil.rotateLeft(sample, 2), ArraysUtil.counterRotate(sample, 2));
    }

    @Test
    void sliceAliases() {
        assertArrayEquals(ArraysUtil.subArray(sample, 1, 4), ArraysUtil.slice(sample, 1, 4));
        assertArrayEquals(ArraysUtil.subArray(sample, 1, 4), ArraysUtil.rangeOf(sample, 1, 4));
    }

    @Test
    void takeDropAliases() {
        assertArrayEquals(ArraysUtil.take(sample, 3), ArraysUtil.head(sample, 3));
        assertArrayEquals(ArraysUtil.take(sample, 3), ArraysUtil.firstN(sample, 3));
        assertArrayEquals(ArraysUtil.drop(sample, 3), ArraysUtil.skip(sample, 3));
        assertArrayEquals(ArraysUtil.drop(sample, 3), ArraysUtil.tail(sample, 5));  // tail(5) = last 5
    }

    // ---------------------------------------------------------- new methods

    @Test
    void findAndMatch() {
        assertEquals(4, ArraysUtil.find(sample, v -> v % 2 == 0));  // first even
        assertTrue(ArraysUtil.anyMatch(sample, v -> v > 5));
        assertFalse(ArraysUtil.allMatch(sample, v -> v > 3));
        assertTrue(ArraysUtil.noneMatch(sample, v -> v < 0));
    }

    @Test
    void filterMap() {
        assertArrayEquals(new int[]{4, 2, 6}, ArraysUtil.filter(sample, v -> v % 2 == 0));
        assertArrayEquals(new int[]{3, 1, 4, 1, 5, 9, 2, 6},
                ArraysUtil.filter(sample, v -> v > 0));
        assertArrayEquals(new int[]{6, 2, 8, 2, 10, 18, 4, 12},
                ArraysUtil.map(sample, v -> v * 2));
    }

    @Test
    void reject() {
        assertArrayEquals(new int[]{3, 1, 1, 5, 9}, ArraysUtil.reject(sample, v -> v % 2 == 0));
        assertArrayEquals(ArraysUtil.reject(sample, v -> v > 0),
                ArraysUtil.exclude(sample, v -> v > 0));
    }

    @Test
    void lastIndexOf() {
        int[] arr = {1, 2, 3, 2, 4};
        assertEquals(3, ArraysUtil.lastIndexOf(arr, 2));
        assertEquals(-1, ArraysUtil.lastIndexOf(arr, 99));
    }

    @Test
    void binarySearch() {
        int[] sorted = {1, 2, 3, 4, 5};
        assertEquals(2, ArraysUtil.binarySearch(sorted, 3));
        assertEquals(-1, ArraysUtil.binarySearch(sorted, 99));
    }

    @Test
    void setOperations() {
        int[] a = {1, 2, 3, 4, 5};
        int[] b = {3, 4, 5, 6, 7};
        assertArrayEquals(new int[]{3, 4, 5}, ArraysUtil.intersection(a, b));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, ArraysUtil.union(a, b));
        assertArrayEquals(new int[]{1, 2}, ArraysUtil.difference(a, b));
    }

    @Test
    void setAliases() {
        int[] a = {1, 2, 3};
        int[] b = {2, 3, 4};
        assertArrayEquals(ArraysUtil.intersection(a, b), ArraysUtil.intersect(a, b));
        assertArrayEquals(ArraysUtil.intersection(a, b), ArraysUtil.common(a, b));
        assertArrayEquals(ArraysUtil.difference(a, b), ArraysUtil.except(a, b));
        assertArrayEquals(ArraysUtil.difference(a, b), ArraysUtil.minus(a, b));
    }

    @Test
    void chunks() {
        List<int[]> chunks = ArraysUtil.chunk(sample, 3);
        assertEquals(3, chunks.size());
        assertArrayEquals(new int[]{3, 1, 4}, chunks.get(0));
        assertArrayEquals(new int[]{1, 5, 9}, chunks.get(1));
        assertArrayEquals(new int[]{2, 6}, chunks.get(2));
    }

    @Test
    void partition() {
        var result = ArraysUtil.partition(sample, v -> v % 2 == 0);
        assertEquals(3, result.matching().size());     // 4, 2, 6
        assertEquals(5, result.nonMatching().size());  // 3, 1, 1, 5, 9
    }

    @Test
    void rangeArray() {
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, ArraysUtil.rangeArray(0, 5));
        assertArrayEquals(new int[]{0, 2, 4}, ArraysUtil.rangeArray(0, 6, 2));
        assertArrayEquals(new int[]{0, 1, 2, 3}, ArraysUtil.sequence(0, 4));
    }

    @Test
    void zerosOnes() {
        assertArrayEquals(new int[]{0, 0, 0}, ArraysUtil.zeros(3));
        assertArrayEquals(new int[]{1, 1, 1}, ArraysUtil.ones(3));
    }

    @Test
    void isSorted() {
        assertTrue(ArraysUtil.isSorted(new int[]{1, 2, 3}));
        assertFalse(ArraysUtil.isSorted(new int[]{1, 3, 2}));
        assertTrue(ArraysUtil.isSortedDescending(new int[]{3, 2, 1}));
    }

    @Test
    void nullSafety() {
        assertEquals(0, ArraysUtil.sum((int[]) null));
        assertTrue(ArraysUtil.isEmpty((int[]) null));
        assertFalse(ArraysUtil.contains((int[]) null, 1));
        assertArrayEquals(new int[0], ArraysUtil.reverse((int[]) null));
    }
}
