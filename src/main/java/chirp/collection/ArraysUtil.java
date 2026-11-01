package chirp.collection;

import chirp.console.Console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * 1D, 2D, and 3D array helpers — null-safe, returning empty arrays instead of
 * throwing.
 *
 * <h2>Naming</h2>
 * <p>Every public method has 2–4 aliases. {@code sum / total / sumOf};
 * {@code average / avg / mean}; {@code max / largest / maxOf};
 * {@code min / smallest / minOf}; {@code contains / includes / has};
 * {@code indexOf / findIndex / firstIndexOf};
 * {@code reverse / reversed / flipped};
 * {@code sorted / sortAscending / ascending};
 * {@code sortedDescending / sortDescending / descending};
 * {@code distinct / unique / dedupe}.</p>
 *
 * <h2>Fluent chains</h2>
 * <p>{@link #of(int[])} opens an {@link ArrayChain} for chaining.</p>
 */
public final class ArraysUtil {

    private ArraysUtil() {}

    // ============================================================== chains

    public static ArrayChain of(int[] array) { return ArrayChain.of(array); }
    public static ArrayChain on(int[] array) { return ArrayChain.of(array); }
    public static ArrayChain from(int[] array) { return ArrayChain.of(array); }
    public static ArrayChain start(int[] array) { return ArrayChain.of(array); }

    public static ArrayChain chain(int[] array) { return ArrayChain.of(array); }

    // ============================================================== creation

    public static int[] intArray(int size, int value) {
        if (size <= 0) return new int[0];
        int[] result = new int[size];
        Arrays.fill(result, value);
        return result;
    }
    public static int[] filled(int size, int value) { return intArray(size, value); }
    public static int[] newFilled(int size, int value) { return intArray(size, value); }

    public static int[] zeros(int size) { return intArray(size, 0); }
    public static int[] ones(int size) { return intArray(size, 1); }

    public static int[] intArray(int... values) {
        return values != null ? values.clone() : new int[0];
    }
    public static int[] ofInts(int... values) { return intArray(values); }
    public static int[] fromInts(int... values) { return intArray(values); }

    public static int[][] matrix(int rows, int cols, int value) {
        if (rows <= 0 || cols <= 0) return new int[0][0];
        int[][] matrix = new int[rows][cols];
        for (int[] row : matrix) Arrays.fill(row, value);
        return matrix;
    }
    public static int[][] grid(int rows, int cols, int value) { return matrix(rows, cols, value); }
    public static int[][] filledMatrix(int rows, int cols, int value) { return matrix(rows, cols, value); }

    public static int[][] randomMatrix(int rows, int cols, int min, int max) {
        if (rows <= 0 || cols <= 0) return new int[0][0];
        if (min > max) { int t = min; min = max; max = t; }
        int[][] m = new int[rows][cols];
        java.util.Random rnd = new java.util.Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                m[r][c] = rnd.nextInt(max - min + 1) + min;
            }
        }
        return m;
    }

    /** Array of consecutive integers {@code [start, start+1, …, end-1]}. */
    public static int[] rangeArray(int start, int end) {
        if (end <= start) return new int[0];
        int[] result = new int[end - start];
        for (int i = 0; i < result.length; i++) result[i] = start + i;
        return result;
    }
    public static int[] sequence(int start, int end) { return rangeArray(start, end); }
    public static int[] to(int start, int end) { return rangeArray(start, end); }

    public static int[] rangeArray(int start, int end, int step) {
        if (step == 0) return new int[0];
        List<Integer> list = new ArrayList<>();
        if (step > 0) for (int i = start; i < end; i += step) list.add(i);
        else for (int i = start; i > end; i += step) list.add(i);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    // ============================================================== aggregation

    public static int sum(int[] array) {
        if (array == null) return 0;
        int total = 0;
        for (int v : array) total += v;
        return total;
    }
    public static int total(int[] array) { return sum(array); }
    public static int sumOf(int[] array) { return sum(array); }

    public static long sum(long[] array) {
        if (array == null) return 0L;
        long total = 0L;
        for (long v : array) total += v;
        return total;
    }

    public static double sum(double[] array) {
        if (array == null) return 0.0;
        double total = 0.0;
        for (double v : array) total += v;
        return total;
    }

    public static double average(int[] array) {
        if (array == null || array.length == 0) return 0;
        return (double) sum(array) / array.length;
    }
    public static double avg(int[] array) { return average(array); }
    public static double mean(int[] array) { return average(array); }

    public static int max(int[] array) {
        if (array == null || array.length == 0) return 0;
        int best = array[0];
        for (int v : array) if (v > best) best = v;
        return best;
    }
    public static int largest(int[] array) { return max(array); }
    public static int maxOf(int[] array) { return max(array); }

    public static int min(int[] array) {
        if (array == null || array.length == 0) return 0;
        int best = array[0];
        for (int v : array) if (v < best) best = v;
        return best;
    }
    public static int smallest(int[] array) { return min(array); }
    public static int minOf(int[] array) { return min(array); }

    public static int count(int[] array, int target) {
        if (array == null) return 0;
        int total = 0;
        for (int v : array) if (v == target) total++;
        return total;
    }
    public static int countOf(int[] array, int target) { return count(array, target); }
    public static int occurrences(int[] array, int target) { return count(array, target); }

    // ============================================================== searching

    public static boolean contains(int[] array, int target) {
        return indexOf(array, target) != -1;
    }
    public static boolean includes(int[] array, int target) { return contains(array, target); }
    public static boolean has(int[] array, int target) { return contains(array, target); }

    public static int indexOf(int[] array, int target) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) if (array[i] == target) return i;
        return -1;
    }
    public static int findIndex(int[] array, int target) { return indexOf(array, target); }
    public static int firstIndexOf(int[] array, int target) { return indexOf(array, target); }

    public static int lastIndexOf(int[] array, int target) {
        if (array == null) return -1;
        for (int i = array.length - 1; i >= 0; i--) if (array[i] == target) return i;
        return -1;
    }
    public static int lastFindIndex(int[] array, int target) { return lastIndexOf(array, target); }

    /** Binary search — array must be sorted. Returns -1 when missing. */
    public static int binarySearch(int[] array, int target) {
        if (array == null) return -1;
        int result = Arrays.binarySearch(array, target);
        return result < 0 ? -1 : result;
    }
    public static int binarySearch(int[] array, int target, int from, int to) {
        if (array == null) return -1;
        int lo = Math.max(0, from), hi = Math.min(array.length, to);
        if (lo >= hi) return -1;
        int result = Arrays.binarySearch(array, lo, hi, target);
        return result < 0 ? -1 : result;
    }

    /** @return first element matching predicate, or 0 when none. */
    public static int find(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return 0;
        for (int v : array) if (predicate.test(v)) return v;
        return 0;
    }
    public static int findOr(int[] array, IntPredicate predicate, int fallback) {
        if (array == null || predicate == null) return fallback;
        for (int v : array) if (predicate.test(v)) return v;
        return fallback;
    }

    public static boolean anyMatch(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return false;
        for (int v : array) if (predicate.test(v)) return true;
        return false;
    }
    public static boolean any(int[] array, IntPredicate predicate) { return anyMatch(array, predicate); }

    public static boolean allMatch(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return false;
        for (int v : array) if (!predicate.test(v)) return false;
        return true;
    }
    public static boolean all(int[] array, IntPredicate predicate) { return allMatch(array, predicate); }

    public static boolean noneMatch(int[] array, IntPredicate predicate) {
        return !anyMatch(array, predicate);
    }
    public static boolean none(int[] array, IntPredicate predicate) { return noneMatch(array, predicate); }

    public static boolean hasDuplicates(int[] array) {
        if (array == null) return false;
        Set<Integer> seen = new HashSet<>();
        for (int v : array) if (!seen.add(v)) return true;
        return false;
    }
    public static boolean hasRepeats(int[] array) { return hasDuplicates(array); }

    public static boolean isEmpty(int[] array) { return array == null || array.length == 0; }
    public static boolean notEmpty(int[] array) { return !isEmpty(array); }
    public static boolean hasElements(int[] array) { return notEmpty(array); }

    public static boolean isSorted(int[] array) {
        if (array == null || array.length <= 1) return true;
        for (int i = 1; i < array.length; i++) if (array[i] < array[i - 1]) return false;
        return true;
    }

    public static boolean isSortedDescending(int[] array) {
        if (array == null || array.length <= 1) return true;
        for (int i = 1; i < array.length; i++) if (array[i] > array[i - 1]) return false;
        return true;
    }

    // ============================================================== transforms

    public static int[] reverse(int[] array) {
        if (array == null) return new int[0];
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[array.length - 1 - i];
        return result;
    }
    public static int[] reversed(int[] array) { return reverse(array); }
    public static int[] flipped(int[] array) { return reverse(array); }

    public static int[] sorted(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        Arrays.sort(copy);
        return copy;
    }
    public static int[] sort(int[] array) { return sorted(array); }
    public static int[] ascending(int[] array) { return sorted(array); }
    public static int[] sortAscending(int[] array) { return sorted(array); }

    public static int[] sortedDescending(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        Arrays.sort(copy);
        int[] result = new int[copy.length];
        for (int i = 0; i < copy.length; i++) result[i] = copy[copy.length - 1 - i];
        return result;
    }
    public static int[] descending(int[] array) { return sortedDescending(array); }
    public static int[] sortDescending(int[] array) { return sortedDescending(array); }

    public static int[] distinct(int[] array) {
        if (array == null) return new int[0];
        List<Integer> seen = new ArrayList<>();
        for (int v : array) if (!seen.contains(v)) seen.add(v);
        int[] result = new int[seen.size()];
        for (int i = 0; i < seen.size(); i++) result[i] = seen.get(i);
        return result;
    }
    public static int[] unique(int[] array) { return distinct(array); }
    public static int[] dedupe(int[] array) { return distinct(array); }

    public static int[] rotateRight(int[] array, int steps) {
        if (array == null || array.length == 0) return new int[0];
        int n = array.length;
        int shift = ((steps % n) + n) % n;
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[(i + shift) % n] = array[i];
        return result;
    }
    public static int[] rotate(int[] array, int steps) { return rotateRight(array, steps); }
    public static int[] shifted(int[] array, int steps) { return rotateRight(array, steps); }

    public static int[] rotateLeft(int[] array, int steps) {
        return rotateRight(array, -steps);
    }
    public static int[] rotateBack(int[] array, int steps) { return rotateLeft(array, steps); }
    public static int[] counterRotate(int[] array, int steps) { return rotateLeft(array, steps); }

    public static int[] shuffle(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        List<Integer> list = new ArrayList<>();
        for (int v : copy) list.add(v);
        Collections.shuffle(list);
        for (int i = 0; i < copy.length; i++) copy[i] = list.get(i);
        return copy;
    }
    public static int[] randomized(int[] array) { return shuffle(array); }
    public static int[] mixed(int[] array) { return shuffle(array); }

    public static int[] subArray(int[] array, int from, int to) {
        if (array == null) return new int[0];
        int start = Math.max(0, from);
        int end = Math.min(array.length, to);
        if (start >= end) return new int[0];
        return Arrays.copyOfRange(array, start, end);
    }
    public static int[] slice(int[] array, int from, int to) { return subArray(array, from, to); }
    public static int[] rangeOf(int[] array, int from, int to) { return subArray(array, from, to); }

    /** First {@code n} elements. */
    public static int[] take(int[] array, int n) {
        if (array == null || n <= 0) return new int[0];
        return subArray(array, 0, Math.min(n, array.length));
    }
    public static int[] head(int[] array, int n) { return take(array, n); }
    public static int[] firstN(int[] array, int n) { return take(array, n); }

    /** Elements after the first {@code n}. */
    public static int[] drop(int[] array, int n) {
        if (array == null || n >= array.length) return new int[0];
        return subArray(array, Math.max(0, n), array.length);
    }
    public static int[] skip(int[] array, int n) { return drop(array, n); }

    /** Last {@code n} elements. */
    public static int[] lastN(int[] array, int n) {
        if (array == null || n <= 0) return new int[0];
        return subArray(array, Math.max(0, array.length - n), array.length);
    }
    public static int[] tail(int[] array, int n) { return lastN(array, n); }

    public static int[] concat(int[] first, int[] second) {
        int[] a = first == null ? new int[0] : first;
        int[] b = second == null ? new int[0] : second;
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public static int[] merge(int[] first, int[] second) { return concat(first, second); }
    public static int[] join(int[] first, int[] second) { return concat(first, second); }

    public static int[] clone(int[] array) {
        return array == null ? new int[0] : array.clone();
    }
    public static int[] copyOf(int[] array) { return clone(array); }

    // ============================================================== higher-order

    public static int[] filter(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return new int[0];
        List<Integer> result = new ArrayList<>();
        for (int v : array) if (predicate.test(v)) result.add(v);
        return result.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] keep(int[] array, IntPredicate predicate) { return filter(array, predicate); }
    public static int[] select(int[] array, IntPredicate predicate) { return filter(array, predicate); }

    public static int[] reject(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return new int[0];
        return filter(array, v -> !predicate.test(v));
    }
    public static int[] exclude(int[] array, IntPredicate predicate) { return reject(array, predicate); }

    public static int[] map(int[] array, IntUnaryOperator mapper) {
        if (array == null || mapper == null) return new int[0];
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) result[i] = mapper.applyAsInt(array[i]);
        return result;
    }
    public static int[] transform(int[] array, IntUnaryOperator mapper) { return map(array, mapper); }

    // ============================================================== set operations

    /** Elements that appear in <b>both</b> arrays. */
    public static int[] intersection(int[] first, int[] second) {
        if (first == null || second == null) return new int[0];
        Set<Integer> s2 = new HashSet<>();
        for (int v : second) s2.add(v);
        List<Integer> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int v : first) {
            if (s2.contains(v) && seen.add(v)) result.add(v);
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] intersect(int[] first, int[] second) { return intersection(first, second); }
    public static int[] common(int[] first, int[] second) { return intersection(first, second); }

    /** Elements that appear in at least one array (deduped). */
    public static int[] union(int[] first, int[] second) {
        if (first == null) return distinct(second);
        if (second == null) return distinct(first);
        return distinct(concat(first, second));
    }
    public static int[] mergeDistinct(int[] first, int[] second) { return union(first, second); }

    /** Elements in {@code first} that are not in {@code second}. */
    public static int[] difference(int[] first, int[] second) {
        if (first == null) return new int[0];
        if (second == null) return distinct(first);
        Set<Integer> s2 = new HashSet<>();
        for (int v : second) s2.add(v);
        List<Integer> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int v : first) {
            if (!s2.contains(v) && seen.add(v)) result.add(v);
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] except(int[] first, int[] second) { return difference(first, second); }
    public static int[] minus(int[] first, int[] second) { return difference(first, second); }

    // ============================================================== split / chunks

    /** Splits array into chunks of at most {@code size} elements. */
    public static List<int[]> chunk(int[] array, int size) {
        if (array == null || size <= 0) return List.of();
        List<int[]> chunks = new ArrayList<>();
        for (int i = 0; i < array.length; i += size) {
            int end = Math.min(i + size, array.length);
            chunks.add(Arrays.copyOfRange(array, i, end));
        }
        return chunks;
    }
    public static List<int[]> chunks(int[] array, int size) { return chunk(array, size); }
    public static List<int[]> partitionBySize(int[] array, int size) { return chunk(array, size); }

    /** Splits into two lists: matching and non-matching predicate. */
    public static SplitResult<Integer> partition(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) {
            return new SplitResult<>(List.of(), List.of());
        }
        List<Integer> matching = new ArrayList<>();
        List<Integer> nonMatching = new ArrayList<>();
        for (int v : array) {
            if (predicate.test(v)) matching.add(v);
            else nonMatching.add(v);
        }
        return new SplitResult<>(matching, nonMatching);
    }

    /** Two-way split — pairs matching/non-matching. */
    public record SplitResult<T>(List<T> matching, List<T> nonMatching) {
        public List<T> matchingItems() { return matching; }
        public List<T> nonMatchingItems() { return nonMatching; }
    }

    // ============================================================== matrix ops

    public static int[][] transpose(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0][0];
        int[][] result = new int[matrix[0].length][matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                result[c][r] = matrix[r][c];
            }
        }
        return result;
    }

    public static int[] row(int[][] matrix, int rowIndex) {
        if (matrix == null || rowIndex < 0 || rowIndex >= matrix.length) return new int[0];
        return matrix[rowIndex].clone();
    }
    public static int[] getRow(int[][] matrix, int rowIndex) { return row(matrix, rowIndex); }

    public static int[] col(int[][] matrix, int colIndex) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int[] result = new int[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            result[r] = (colIndex >= 0 && colIndex < matrix[r].length) ? matrix[r][colIndex] : 0;
        }
        return result;
    }
    public static int[] column(int[][] matrix, int colIndex) { return col(matrix, colIndex); }
    public static int[] getCol(int[][] matrix, int colIndex) { return col(matrix, colIndex); }

    public static int sumRow(int[][] matrix, int rowIndex) { return sum(row(matrix, rowIndex)); }
    public static int sumCol(int[][] matrix, int colIndex) { return sum(col(matrix, colIndex)); }

    public static int[] diagonal(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int size = Math.min(matrix.length, matrix[0].length);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = matrix[i][i];
        return result;
    }
    public static int[] mainDiagonal(int[][] matrix) { return diagonal(matrix); }

    public static int[] antiDiagonal(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int rows = matrix.length, cols = matrix[0].length;
        int size = Math.min(rows, cols);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = matrix[i][cols - 1 - i];
        return result;
    }
    public static int[] secondaryDiagonal(int[][] matrix) { return antiDiagonal(matrix); }

    public static int[] flatten(int[][] matrix) {
        if (matrix == null) return new int[0];
        List<Integer> flat = new ArrayList<>();
        for (int[] row : matrix) {
            if (row != null) for (int v : row) flat.add(v);
        }
        return flat.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] flattened(int[][] matrix) { return flatten(matrix); }

    // ============================================================== 3D

    public static int[][][] cube(int size, int value) {
        if (size <= 0) return new int[0][0][0];
        int[][][] cube = new int[size][size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                Arrays.fill(cube[i][j], value);
        return cube;
    }

    public static void printCube(int[][][] cube) {
        if (cube == null) { Console.println("null"); return; }
        for (int layer = 0; layer < cube.length; layer++) {
            Console.println("Layer " + layer + ":");
            for (int[] row : cube[layer]) Console.printArray(row);
        }
    }
}
