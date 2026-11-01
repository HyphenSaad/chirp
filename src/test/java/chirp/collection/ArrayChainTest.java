package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArrayChainTest {

    // ---------------------------------------------------------- factories

    @Test
    void ofIntArray() {
        assertArrayEquals(new int[]{1, 2, 3}, ArrayChain.of(new int[]{1, 2, 3}).toArray());
    }

    @Test
    void ofVarargs() {
        assertArrayEquals(new int[]{1, 2, 3}, ArrayChain.of(1, 2, 3).toArray());
    }

    @Test
    void ofRange() {
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, ArrayChain.ofRange(0, 5).toArray());
    }

    @Test
    void nullArrayBecomesEmpty() {
        assertEquals(0, ArrayChain.of((int[]) null).count());
    }

    // ---------------------------------------------------------- transforms

    @Test
    void filter() {
        assertArrayEquals(new int[]{4, 6},
                ArrayChain.of(3, 1, 4, 5, 6).filter(v -> v % 2 == 0).toArray());
    }

    @Test
    void filterAliases() {
        int[] source = {1, 2, 3, 4};
        assertArrayEquals(new int[]{2, 4}, ArrayChain.of(source).filter(v -> v % 2 == 0).toArray());
        assertArrayEquals(new int[]{2, 4}, ArrayChain.of(source).keep(v -> v % 2 == 0).toArray());
        assertArrayEquals(new int[]{2, 4}, ArrayChain.of(source).select(v -> v % 2 == 0).toArray());
    }

    @Test
    void map() {
        assertArrayEquals(new int[]{2, 4, 6},
                ArrayChain.of(1, 2, 3).map(v -> v * 2).toArray());
    }

    @Test
    void sortedDescending() {
        assertArrayEquals(new int[]{9, 6, 5, 4, 3, 2, 1, 1},
                ArrayChain.of(3, 1, 4, 1, 5, 9, 2, 6).descending().take(8).toArray());
    }

    @Test
    void chainedTransforms() {
        int[] result = ArrayChain.of(new int[]{5, 3, 8, 1, 9, 2})
                .filter(v -> v > 2)
                .sorted()
                .take(3)
                .toArray();
        assertArrayEquals(new int[]{3, 5, 8}, result);
    }

    @Test
    void distinctChain() {
        assertArrayEquals(new int[]{1, 2, 3},
                ArrayChain.of(1, 2, 1, 3, 2).distinct().toArray());
        assertArrayEquals(new int[]{1, 2, 3},
                ArrayChain.of(1, 2, 1, 3, 2).unique().toArray());
        assertArrayEquals(new int[]{1, 2, 3},
                ArrayChain.of(1, 2, 1, 3, 2).dedupe().toArray());
    }

    @Test
    void sliceChain() {
        assertArrayEquals(new int[]{2, 3},
                ArrayChain.of(1, 2, 3, 4).slice(1, 3).toArray());
    }

    @Test
    void rotateChain() {
        assertArrayEquals(new int[]{4, 5, 1, 2, 3},
                ArrayChain.of(1, 2, 3, 4, 5).rotate(2).toArray());
    }

    @Test
    void concatChain() {
        assertArrayEquals(new int[]{1, 2, 3, 4},
                ArrayChain.of(1, 2).concat(new int[]{3, 4}).toArray());
    }

    @Test
    void concatWithChain() {
        ArrayChain a = ArrayChain.of(1, 2);
        ArrayChain b = ArrayChain.of(3, 4);
        assertArrayEquals(new int[]{1, 2, 3, 4}, a.concat(b).toArray());
    }

    // ---------------------------------------------------------- terminals

    @Test
    void countAndEmpty() {
        assertEquals(3, ArrayChain.of(1, 2, 3).count());
        assertTrue(ArrayChain.of().isEmpty());
        assertFalse(ArrayChain.of(1).isEmpty());
    }

    @Test
    void aggregateTerminals() {
        ArrayChain c = ArrayChain.of(1, 2, 3, 4, 5);
        assertEquals(15, c.sum());
        assertEquals(15, c.total());
        assertEquals(3.0, c.average(), 0.001);
        assertEquals(3.0, c.avg(), 0.001);
        assertEquals(3.0, c.mean(), 0.001);
        assertEquals(5, c.max());
        assertEquals(1, c.min());
        assertEquals(5, c.largest());
        assertEquals(1, c.smallest());
    }

    @Test
    void firstLast() {
        ArrayChain c = ArrayChain.of(10, 20, 30);
        assertEquals(10, c.first());
        assertEquals(30, c.last());

        ArrayChain empty = ArrayChain.of();
        assertEquals(0, empty.first());
        assertEquals(0, empty.last());
        assertEquals(99, empty.firstOr(99));
        assertEquals(99, empty.lastOr(99));
    }

    @Test
    void searchTerminals() {
        ArrayChain c = ArrayChain.of(1, 2, 3, 2, 4);
        assertTrue(c.contains(2));
        assertEquals(1, c.indexOf(2));
        assertEquals(3, c.lastIndexOf(2));
    }

    @Test
    void hasDuplicatesAndIsSorted() {
        assertTrue(ArrayChain.of(1, 2, 1).hasDuplicates());
        assertFalse(ArrayChain.of(1, 2, 3).hasDuplicates());
        assertTrue(ArrayChain.of(1, 2, 3).isSorted());
        assertFalse(ArrayChain.of(3, 2, 1).isSorted());
    }

    @Test
    void joinTerminals() {
        assertEquals("1, 2, 3", ArrayChain.of(1, 2, 3).join(", "));
        assertEquals("[1, 2, 3]", ArrayChain.of(1, 2, 3).join(", ", "[", "]"));
    }

    @Test
    void asList() {
        List<Integer> list = ArrayChain.of(1, 2, 3).asList();
        assertEquals(List.of(1, 2, 3), list);
    }

    // ---------------------------------------------------------- immutability

    @Test
    void chainIsImmutable() {
        ArrayChain original = ArrayChain.of(1, 2, 3);
        ArrayChain filtered = original.filter(v -> v > 1);
        assertArrayEquals(new int[]{1, 2, 3}, original.toArray());
        assertArrayEquals(new int[]{2, 3}, filtered.toArray());
    }
}
