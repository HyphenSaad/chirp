package chirp.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArraysUtilTest {

    @Test
    void sumHandlesNull() { assertEquals(0, ArraysUtil.sum((int[]) null)); }
    @Test
    void sumBasic() { assertEquals(6, ArraysUtil.sum(new int[]{1, 2, 3})); }
    @Test
    void sumDouble() { assertEquals(6.0, ArraysUtil.sum(new double[]{1.5, 2.5, 2.0}), 0.001); }
    @Test
    void maxHandlesEmpty() { assertEquals(0, ArraysUtil.max(new int[0])); }
    @Test
    void maxBasic() { assertEquals(9, ArraysUtil.max(new int[]{5, 3, 9, 1})); }
    @Test
    void minBasic() { assertEquals(1, ArraysUtil.min(new int[]{5, 3, 9, 1})); }
    @Test
    void countValue() { assertEquals(2, ArraysUtil.count(new int[]{1, 2, 1}, 1)); }

    @Test
    void containsValue() { assertTrue(ArraysUtil.contains(new int[]{1, 2, 3}, 2)); }
    @Test
    void indexOfMissing() { assertEquals(-1, ArraysUtil.indexOf(new int[]{1, 2}, 5)); }

    @Test
    void hasDuplicates() {
        assertTrue(ArraysUtil.hasDuplicates(new int[]{1, 2, 1}));
        assertFalse(ArraysUtil.hasDuplicates(new int[]{1, 2, 3}));
    }

    @Test
    void reverse() { assertArrayEquals(new int[]{3, 2, 1}, ArraysUtil.reverse(new int[]{1, 2, 3})); }
    @Test
    void sorted() { assertArrayEquals(new int[]{1, 2, 3}, ArraysUtil.sorted(new int[]{3, 1, 2})); }
    @Test
    void sortedDescending() {
        assertArrayEquals(new int[]{3, 2, 1}, ArraysUtil.sortedDescending(new int[]{3, 1, 2}));
    }
    @Test
    void distinct() {
        assertArrayEquals(new int[]{1, 2, 3}, ArraysUtil.distinct(new int[]{1, 2, 2, 3, 1}));
    }

    @Test
    void rotateRight() {
        assertArrayEquals(new int[]{4, 5, 1, 2, 3}, ArraysUtil.rotateRight(new int[]{1, 2, 3, 4, 5}, 2));
    }
    @Test
    void rotateLeft() {
        assertArrayEquals(new int[]{3, 4, 5, 1, 2}, ArraysUtil.rotateLeft(new int[]{1, 2, 3, 4, 5}, 2));
    }

    @Test
    void subArray() {
        assertArrayEquals(new int[]{2, 3}, ArraysUtil.subArray(new int[]{1, 2, 3, 4}, 1, 3));
    }

    @Test
    void concat() {
        assertArrayEquals(new int[]{1, 2, 3, 4},
                ArraysUtil.concat(new int[]{1, 2}, new int[]{3, 4}));
    }

    @Test
    void transpose() {
        int[][] m = {{1, 2}, {3, 4}};
        int[][] t = ArraysUtil.transpose(m);
        assertArrayEquals(new int[]{1, 3}, t[0]);
        assertArrayEquals(new int[]{2, 4}, t[1]);
    }

    @Test
    void diagonal() {
        assertArrayEquals(new int[]{1, 4}, ArraysUtil.diagonal(new int[][]{{1, 2}, {3, 4}}));
    }

    @Test
    void antiDiagonal() {
        assertArrayEquals(new int[]{2, 3}, ArraysUtil.antiDiagonal(new int[][]{{1, 2}, {3, 4}}));
    }

    @Test
    void flatten() {
        assertArrayEquals(new int[]{1, 2, 3, 4},
                ArraysUtil.flatten(new int[][]{{1, 2}, {3, 4}}));
    }

    @Test
    void sumRowCol() {
        int[][] m = {{1, 2}, {3, 4}};
        assertEquals(3, ArraysUtil.sumRow(m, 0));
        assertEquals(4, ArraysUtil.sumCol(m, 0));
    }

    @Test
    void matrix() {
        int[][] m = ArraysUtil.matrix(2, 2, 7);
        assertEquals(7, m[0][0]);
        assertEquals(7, m[1][1]);
    }
}
