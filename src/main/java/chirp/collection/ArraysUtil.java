package chirp.collection;

import chirp.console.Console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * 1D, 2D, and 3D array helpers — null-safe and empty-safe throughout.
 *
 * <p>Every method in this class returns a documented default for
 * {@code null} or empty input, never throws. Aggregations return {@code 0},
 * transforms return an empty array, predicates return {@code false} or
 * {@code true} depending on the question being asked (see the method docs).
 * This means callers can chain operations without pre-checking whether the
 * array is present.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Creation</b> — {@link #intArray(int, int)},
 *       {@link #matrix(int, int, int)},
 *       {@link #rangeArray(int, int)},
 *       {@link #zeros(int)}, {@link #ones(int)},
 *       {@link #randomMatrix(int, int, int, int)}</li>
 *   <li><b>Aggregation</b> — {@link #sum(int[])},
 *       {@link #average(int[])}, {@link #max(int[])},
 *       {@link #min(int[])}, {@link #count(int[], int)}</li>
 *   <li><b>Search</b> — {@link #contains(int[], int)},
 *       {@link #indexOf(int[], int)}, {@link #binarySearch(int[], int)},
 *       {@link #find(int[], IntPredicate)}, {@link #anyMatch(int[], IntPredicate)}</li>
 *   <li><b>Transforms</b> — {@link #reverse(int[])},
 *       {@link #sorted(int[])}, {@link #distinct(int[])},
 *       {@link #rotateRight(int[], int)}, {@link #shuffle(int[])},
 *       {@link #take(int[], int)}, {@link #drop(int[], int)}</li>
 *   <li><b>Higher-order</b> — {@link #filter(int[], IntPredicate)},
 *       {@link #map(int[], IntUnaryOperator)}</li>
 *   <li><b>Set operations</b> — {@link #intersection(int[], int[])},
 *       {@link #union(int[], int[])}, {@link #difference(int[], int[])}</li>
 *   <li><b>Chunking</b> — {@link #chunk(int[], int)},
 *       {@link #partition(int[], IntPredicate)}</li>
 *   <li><b>Matrix</b> — {@link #transpose(int[][])},
 *       {@link #row(int[][], int)}, {@link #col(int[][], int)},
 *       {@link #diagonal(int[][])}, {@link #flatten(int[][])}</li>
 *   <li><b>3D</b> — {@link #cube(int, int)},
 *       {@link #printCube(int[][][])}</li>
 *   <li><b>Fluent chains</b> — {@link #of(int[])} → {@link ArrayChain}</li>
 * </ul>
 *
 * <h2>Aliases</h2>
 * <p>Every public method has 2–4 aliases so a reader can use whichever
 * verb reads most naturally. Aggregations ({@code sum / total / sumOf}),
 * searches ({@code contains / includes / has}), transforms
 * ({@code reverse / reversed / flipped}), and sorts
 * ({@code sorted / sortAscending / ascending}) all produce identical
 * results — they are pure delegates, not separate implementations.</p>
 *
 * <h2>Null and empty input</h2>
 * <table border="1">
 *   <caption>Behavior for null/empty inputs</caption>
 *   <tr><th>Method family</th><th>Return for null/empty</th></tr>
 *   <tr><td>Aggregation ({@code sum}, {@code max}, …)</td><td>{@code 0}</td></tr>
 *   <tr><td>Search ({@code contains}, {@code indexOf}, …)</td><td>{@code false} / {@code -1}</td></tr>
 *   <tr><td>Transforms ({@code reverse}, {@code sorted}, …)</td><td>empty array</td></tr>
 *   <tr><td>Predicates ({@code isEmpty}, {@code isSorted})</td><td>{@code true}</td></tr>
 *   <tr><td>Chunks / partition / matrix ops</td><td>empty list / empty matrix</td></tr>
 * </table>
 *
 * <h2>Immutability</h2>
 * <p>Transforms always return a <b>new</b> array; the input array is
 * never modified. This is deliberate — an array passed into a Chirp
 * method is safe to reuse afterwards. For fluent chains, see
 * {@link ArrayChain}, which adds fluent method chaining on top of the
 * same transforms.</p>
 *
 * <h2>Example — a typical aggregation pipeline</h2>
 * <pre>{@code
 * int[] scores = {85, 92, 78, 95, 88, 72};
 *
 * println("Count  : " + scores.length);
 * println("Sum    : " + sum(scores));
 * println("Average: " + Format.decimal(average(scores), 2));
 * println("Max    : " + max(scores));
 * println("Min    : " + min(scores));
 * println("Sorted : " + java.util.Arrays.toString(sorted(scores)));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For fluent chaining, see {@link ArrayChain}. For list-based helpers
 * (List&lt;Integer&gt; instead of int[]), see {@link Lists}. For
 * filtering and search, see {@link Filter} and {@link Search}.</p>
 *
 * @see ArrayChain
 * @see Lists
 * @see Filter
 * @see Search
 * @see chirp.number.Numbers
 * @since 1.0.0
 */
public final class ArraysUtil {

    private ArraysUtil() {}

    // ============================================================== chains

    /**
     * Opens a fluent {@link ArrayChain} over the array.
     *
     * <pre>{@code
     * int[] result = ArraysUtil.of(nums)
     *     .filter(v -> v > 0)
     *     .sorted()
     *     .take(5)
     *     .toArray();
     * }</pre>
     *
     * @param array the array to wrap (may be null — treated as empty)
     * @return a new {@code ArrayChain} wrapping {@code array}
     * @see ArrayChain
     * @see #on(int[])
     * @see #chain(int[])
     * @since 1.0.0
     */
    public static ArrayChain of(int[] array) { return ArrayChain.of(array); }

    /**
     * Alias of {@link #of(int[])} — see that method for full
     * documentation and examples.
     *
     * @param array the array to wrap (may be null)
     * @return a new {@code ArrayChain}
     * @see #of(int[])
     * @since 1.0.0
     */
    public static ArrayChain on(int[] array) { return ArrayChain.of(array); }

    /**
     * Alias of {@link #of(int[])} — see that method for full
     * documentation and examples.
     *
     * @param array the array to wrap (may be null)
     * @return a new {@code ArrayChain}
     * @see #of(int[])
     * @since 1.0.0
     */
    public static ArrayChain from(int[] array) { return ArrayChain.of(array); }

    /**
     * Alias of {@link #of(int[])} — see that method for full
     * documentation and examples.
     *
     * @param array the array to wrap (may be null)
     * @return a new {@code ArrayChain}
     * @see #of(int[])
     * @since 1.0.0
     */
    public static ArrayChain start(int[] array) { return ArrayChain.of(array); }

    /**
     * Alias of {@link #of(int[])} — see that method for full
     * documentation and examples.
     *
     * @param array the array to wrap (may be null)
     * @return a new {@code ArrayChain}
     * @see #of(int[])
     * @since 1.0.0
     */
    public static ArrayChain chain(int[] array) { return ArrayChain.of(array); }

    // ============================================================== creation

    /**
     * Creates an array of the given size filled with the same value.
     *
     * <pre>{@code
     * ArraysUtil.intArray(5, 42);   // [42, 42, 42, 42, 42]
     * ArraysUtil.intArray(0, 42);   // []  (no exception)
     * }</pre>
     *
     * @param size the array length (&le; 0 returns empty)
     * @param value the fill value
     * @return a new filled array
     * @see #filled(int, int)
     * @see #zeros(int)
     * @see #ones(int)
     * @since 1.0.0
     */
    public static int[] intArray(int size, int value) {
        if (size <= 0) return new int[0];
        int[] result = new int[size];
        Arrays.fill(result, value);
        return result;
    }

    /**
     * Alias of {@link #intArray(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param size the array length
     * @param value the fill value
     * @return a new filled array
     * @see #intArray(int, int)
     * @since 1.0.0
     */
    public static int[] filled(int size, int value) { return intArray(size, value); }

    /**
     * Alias of {@link #intArray(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param size the array length
     * @param value the fill value
     * @return a new filled array
     * @see #intArray(int, int)
     * @since 1.0.0
     */
    public static int[] newFilled(int size, int value) { return intArray(size, value); }

    /**
     * Returns an array of the given size filled with {@code 0}.
     *
     * @param size the array length (&le; 0 returns empty)
     * @return a new array of zeros
     * @see #ones(int)
     * @see #intArray(int, int)
     * @since 1.0.0
     */
    public static int[] zeros(int size) { return intArray(size, 0); }

    /**
     * Returns an array of the given size filled with {@code 1}.
     *
     * @param size the array length (&le; 0 returns empty)
     * @return a new array of ones
     * @see #zeros(int)
     * @see #intArray(int, int)
     * @since 1.0.0
     */
    public static int[] ones(int size) { return intArray(size, 1); }

    /**
     * Clones the given varargs into a defensive array.
     *
     * <p>This overload is deliberately distinct from
     * {@link #intArray(int, int)} — Java binds a call like
     * {@code intArray(1, 2, 3)} to this one, and {@code intArray(5, 42)}
     * to the fill form, based on the number of arguments.</p>
     *
     * @param values the values to wrap (may be null)
     * @return a defensive copy, or empty if {@code values} is null
     * @see #ofInts(int...)
     * @since 1.0.0
     */
    public static int[] intArray(int... values) {
        return values != null ? values.clone() : new int[0];
    }

    /**
     * Alias of {@link #intArray(int...)} — see that method for full
     * documentation.
     *
     * @param values the values to wrap
     * @return a defensive copy
     * @see #intArray(int...)
     * @since 1.0.0
     */
    public static int[] ofInts(int... values) { return intArray(values); }

    /**
     * Alias of {@link #intArray(int...)} — see that method for full
     * documentation.
     *
     * @param values the values to wrap
     * @return a defensive copy
     * @see #intArray(int...)
     * @since 1.0.0
     */
    public static int[] fromInts(int... values) { return intArray(values); }

    /**
     * Creates a rectangular 2D matrix filled with the same value.
     *
     * @param rows the number of rows (&le; 0 returns {@code new int[0][0]})
     * @param cols the number of columns (&le; 0 returns {@code new int[0][0]})
     * @param value the fill value
     * @return a new filled matrix
     * @see #grid(int, int, int)
     * @see #randomMatrix(int, int, int, int)
     * @since 1.0.0
     */
    public static int[][] matrix(int rows, int cols, int value) {
        if (rows <= 0 || cols <= 0) return new int[0][0];
        int[][] matrix = new int[rows][cols];
        for (int[] row : matrix) Arrays.fill(row, value);
        return matrix;
    }

    /**
     * Alias of {@link #matrix(int, int, int)} — see that method for full
     * documentation.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param value the fill value
     * @return a new filled matrix
     * @see #matrix(int, int, int)
     * @since 1.0.0
     */
    public static int[][] grid(int rows, int cols, int value) { return matrix(rows, cols, value); }

    /**
     * Alias of {@link #matrix(int, int, int)} — see that method for full
     * documentation.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param value the fill value
     * @return a new filled matrix
     * @see #matrix(int, int, int)
     * @since 1.0.0
     */
    public static int[][] filledMatrix(int rows, int cols, int value) { return matrix(rows, cols, value); }

    /**
     * Creates a rectangular 2D matrix of random values in {@code [min, max]}.
     *
     * <p>If {@code min > max}, the bounds are swapped. Uses
     * {@link java.util.concurrent.ThreadLocalRandom} — thread-safe,
     * non-cryptographic.</p>
     *
     * @param rows the number of rows (&le; 0 returns empty matrix)
     * @param cols the number of columns (&le; 0 returns empty matrix)
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a new random matrix
     * @see #matrix(int, int, int)
     * @since 1.0.0
     */
    public static int[][] randomMatrix(int rows, int cols, int min, int max) {
        if (rows <= 0 || cols <= 0) return new int[0][0];
        int lo = min;
        int hi = max;
        if (lo > hi) { int t = lo; lo = hi; hi = t; }
        int[][] m = new int[rows][cols];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                m[r][c] = rnd.nextInt(lo, hi + 1);
            }
        }
        return m;
    }

    /**
     * Returns an array of consecutive integers {@code [start, start+1, …,
     * end-1]}.
     *
     * <pre>{@code
     * ArraysUtil.rangeArray(0, 5);   // [0, 1, 2, 3, 4]
     * ArraysUtil.rangeArray(5, 0);   // []  (end must be > start)
     * }</pre>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the sequence, or empty if {@code end <= start}
     * @see #rangeArray(int, int, int)
     * @see #sequence(int, int)
     * @see chirp.number.Numbers#range(int, int)
     * @since 1.0.0
     */
    public static int[] rangeArray(int start, int end) {
        if (end <= start) return new int[0];
        int[] result = new int[end - start];
        for (int i = 0; i < result.length; i++) result[i] = start + i;
        return result;
    }

    /**
     * Alias of {@link #rangeArray(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the sequence
     * @see #rangeArray(int, int)
     * @since 1.0.0
     */
    public static int[] sequence(int start, int end) { return rangeArray(start, end); }

    /**
     * Alias of {@link #rangeArray(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the sequence
     * @see #rangeArray(int, int)
     * @since 1.0.0
     */
    public static int[] to(int start, int end) { return rangeArray(start, end); }

    /**
     * Returns a stepped sequence of integers — {@code [start, start+step,
     * …]} up to (not including) {@code end}.
     *
     * <p>Positive and negative steps are both supported. A zero step
     * returns an empty array.</p>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @param step the increment (must be non-zero)
     * @return the stepped sequence
     * @see #rangeArray(int, int)
     * @since 1.0.0
     */
    public static int[] rangeArray(int start, int end, int step) {
        if (step == 0) return new int[0];
        List<Integer> list = new ArrayList<>();
        if (step > 0) for (int i = start; i < end; i += step) list.add(i);
        else for (int i = start; i > end; i += step) list.add(i);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    // ============================================================== aggregation

    /**
     * Returns the sum of the array. {@code null} or empty → {@code 0}.
     *
     * @param array the array to sum (may be null)
     * @return the sum
     * @see #total(int[])
     * @see #sumOf(int[])
     * @see #sum(double[])
     * @see #average(int[])
     * @since 1.0.0
     */
    public static int sum(int[] array) {
        if (array == null) return 0;
        int total = 0;
        for (int v : array) total += v;
        return total;
    }

    /**
     * Alias of {@link #sum(int[])} — see that method for full
     * documentation.
     *
     * @param array the array to sum
     * @return the sum
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static int total(int[] array) { return sum(array); }

    /**
     * Alias of {@link #sum(int[])} — see that method for full
     * documentation.
     *
     * @param array the array to sum
     * @return the sum
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static int sumOf(int[] array) { return sum(array); }

    /**
     * Returns the sum of the long array.
     *
     * @param array the array to sum (may be null)
     * @return the sum, or 0 for null or empty
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static long sum(long[] array) {
        if (array == null) return 0L;
        long total = 0L;
        for (long v : array) total += v;
        return total;
    }

    /**
     * Returns the sum of the double array.
     *
     * @param array the array to sum (may be null)
     * @return the sum, or 0.0 for null or empty
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static double sum(double[] array) {
        if (array == null) return 0.0;
        double total = 0.0;
        for (double v : array) total += v;
        return total;
    }

    /**
     * Returns the arithmetic mean of the array.
     *
     * @param array the array (may be null)
     * @return the mean, or 0 for null or empty
     * @see #avg(int[])
     * @see #mean(int[])
     * @see #sum(int[])
     * @see #max(int[])
     * @since 1.0.0
     */
    public static double average(int[] array) {
        if (array == null || array.length == 0) return 0;
        return (double) sum(array) / array.length;
    }

    /**
     * Alias of {@link #average(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the mean
     * @see #average(int[])
     * @since 1.0.0
     */
    public static double avg(int[] array) { return average(array); }

    /**
     * Alias of {@link #average(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the mean
     * @see #average(int[])
     * @since 1.0.0
     */
    public static double mean(int[] array) { return average(array); }

    /**
     * Returns the maximum value in the array.
     *
     * @param array the array (may be null)
     * @return the maximum, or 0 for null or empty
     * @see #largest(int[])
     * @see #maxOf(int[])
     * @see #min(int[])
     * @since 1.0.0
     */
    public static int max(int[] array) {
        if (array == null || array.length == 0) return 0;
        int best = array[0];
        for (int v : array) if (v > best) best = v;
        return best;
    }

    /**
     * Alias of {@link #max(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the maximum
     * @see #max(int[])
     * @since 1.0.0
     */
    public static int largest(int[] array) { return max(array); }

    /**
     * Alias of {@link #max(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the maximum
     * @see #max(int[])
     * @since 1.0.0
     */
    public static int maxOf(int[] array) { return max(array); }

    /**
     * Returns the minimum value in the array.
     *
     * @param array the array (may be null)
     * @return the minimum, or 0 for null or empty
     * @see #smallest(int[])
     * @see #minOf(int[])
     * @see #max(int[])
     * @since 1.0.0
     */
    public static int min(int[] array) {
        if (array == null || array.length == 0) return 0;
        int best = array[0];
        for (int v : array) if (v < best) best = v;
        return best;
    }

    /**
     * Alias of {@link #min(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the minimum
     * @see #min(int[])
     * @since 1.0.0
     */
    public static int smallest(int[] array) { return min(array); }

    /**
     * Alias of {@link #min(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the minimum
     * @see #min(int[])
     * @since 1.0.0
     */
    public static int minOf(int[] array) { return min(array); }

    /**
     * Counts occurrences of the target value.
     *
     * @param array the array (may be null)
     * @param target the value to count
     * @return the count, or 0 for null
     * @see #countOf(int[], int)
     * @see #occurrences(int[], int)
     * @since 1.0.0
     */
    public static int count(int[] array, int target) {
        if (array == null) return 0;
        int total = 0;
        for (int v : array) if (v == target) total++;
        return total;
    }

    /**
     * Alias of {@link #count(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to count
     * @return the count
     * @see #count(int[], int)
     * @since 1.0.0
     */
    public static int countOf(int[] array, int target) { return count(array, target); }

    /**
     * Alias of {@link #count(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to count
     * @return the count
     * @see #count(int[], int)
     * @since 1.0.0
     */
    public static int occurrences(int[] array, int target) { return count(array, target); }

    // ============================================================== searching

    /**
     * Checks whether the array contains the target value.
     *
     * @param array the array (may be null)
     * @param target the value to find
     * @return {@code true} if present, {@code false} for null array or missing value
     * @see #includes(int[], int)
     * @see #has(int[], int)
     * @see #indexOf(int[], int)
     * @since 1.0.0
     */
    public static boolean contains(int[] array, int target) {
        return indexOf(array, target) != -1;
    }

    /**
     * Alias of {@link #contains(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to find
     * @return {@code true} if present
     * @see #contains(int[], int)
     * @since 1.0.0
     */
    public static boolean includes(int[] array, int target) { return contains(array, target); }

    /**
     * Alias of {@link #contains(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to find
     * @return {@code true} if present
     * @see #contains(int[], int)
     * @since 1.0.0
     */
    public static boolean has(int[] array, int target) { return contains(array, target); }

    /**
     * Returns the index of the first occurrence of the target, or {@code -1}.
     *
     * @param array the array (may be null)
     * @param target the value to find
     * @return the 0-based index, or {@code -1} if absent or null array
     * @see #findIndex(int[], int)
     * @see #firstIndexOf(int[], int)
     * @see #lastIndexOf(int[], int)
     * @since 1.0.0
     */
    public static int indexOf(int[] array, int target) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) if (array[i] == target) return i;
        return -1;
    }

    /**
     * Alias of {@link #indexOf(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to find
     * @return the index or -1
     * @see #indexOf(int[], int)
     * @since 1.0.0
     */
    public static int findIndex(int[] array, int target) { return indexOf(array, target); }

    /**
     * Alias of {@link #indexOf(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to find
     * @return the index or -1
     * @see #indexOf(int[], int)
     * @since 1.0.0
     */
    public static int firstIndexOf(int[] array, int target) { return indexOf(array, target); }

    /**
     * Returns the index of the last occurrence of the target, or {@code -1}.
     *
     * @param array the array (may be null)
     * @param target the value to find
     * @return the index or -1
     * @see #lastFindIndex(int[], int)
     * @see #indexOf(int[], int)
     * @since 1.0.0
     */
    public static int lastIndexOf(int[] array, int target) {
        if (array == null) return -1;
        for (int i = array.length - 1; i >= 0; i--) if (array[i] == target) return i;
        return -1;
    }

    /**
     * Alias of {@link #lastIndexOf(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param target the value to find
     * @return the index or -1
     * @see #lastIndexOf(int[], int)
     * @since 1.0.0
     */
    public static int lastFindIndex(int[] array, int target) { return lastIndexOf(array, target); }

    /**
     * Binary search — <b>array must be sorted</b> ascending. Returns the
     * index of the target, or {@code -1} if absent.
     *
     * <p>Unlike {@link java.util.Arrays#binarySearch(int[], int)} this
     * method does not return the insertion point (as a negative number)
     * — it just returns {@code -1} for missing values, which is what most
     * callers want.</p>
     *
     * @param array the sorted array (may be null)
     * @param target the value to find
     * @return the 0-based index, or {@code -1} if absent or null array
     * @see #indexOf(int[], int)
     * @since 1.0.0
     */
    public static int binarySearch(int[] array, int target) {
        if (array == null) return -1;
        int result = Arrays.binarySearch(array, target);
        return result < 0 ? -1 : result;
    }

    /**
     * Binary search within a subrange of the sorted array.
     *
     * @param array the sorted array (may be null)
     * @param target the value to find
     * @param from the starting index (inclusive, clamped to 0)
     * @param to the ending index (exclusive, clamped to length)
     * @return the 0-based index, or {@code -1} if absent
     * @see #binarySearch(int[], int)
     * @since 1.0.0
     */
    public static int binarySearch(int[] array, int target, int from, int to) {
        if (array == null) return -1;
        int lo = Math.max(0, from), hi = Math.min(array.length, to);
        if (lo >= hi) return -1;
        int result = Arrays.binarySearch(array, lo, hi, target);
        return result < 0 ? -1 : result;
    }

    /**
     * Returns the first element matching the predicate, or {@code 0} when
     * no element matches.
     *
     * <p>Use {@link #findOr(int[], IntPredicate, int)} if {@code 0} is a
     * meaningful value in the array.</p>
     *
     * @param array the array (may be null)
     * @param predicate the test (may be null — returns 0)
     * @return the first matching value, or 0
     * @see #findOr(int[], IntPredicate, int)
     * @see #anyMatch(int[], IntPredicate)
     * @see java.util.OptionalInt
     * @since 1.0.0
     */
    public static int find(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return 0;
        for (int v : array) if (predicate.test(v)) return v;
        return 0;
    }

    /**
     * Returns the first element matching the predicate, or
     * {@code fallback} when no element matches.
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @param fallback the value to return on no match
     * @return the first matching value, or the fallback
     * @see #find(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int findOr(int[] array, IntPredicate predicate, int fallback) {
        if (array == null || predicate == null) return fallback;
        for (int v : array) if (predicate.test(v)) return v;
        return fallback;
    }

    /**
     * Checks whether any element matches the predicate.
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return {@code true} if at least one element matches
     * @see #any(int[], IntPredicate)
     * @see #allMatch(int[], IntPredicate)
     * @see #noneMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean anyMatch(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return false;
        for (int v : array) if (predicate.test(v)) return true;
        return false;
    }

    /**
     * Alias of {@link #anyMatch(int[], IntPredicate)} — see that method
     * for full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return {@code true} if any element matches
     * @see #anyMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean any(int[] array, IntPredicate predicate) { return anyMatch(array, predicate); }

    /**
     * Checks whether all elements match the predicate. Returns
     * {@code false} for a {@code null} array or {@code null} predicate.
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return {@code true} if every element matches
     * @see #all(int[], IntPredicate)
     * @see #anyMatch(int[], IntPredicate)
     * @see #noneMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean allMatch(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return false;
        for (int v : array) if (!predicate.test(v)) return false;
        return true;
    }

    /**
     * Alias of {@link #allMatch(int[], IntPredicate)} — see that method
     * for full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return {@code true} if every element matches
     * @see #allMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean all(int[] array, IntPredicate predicate) { return allMatch(array, predicate); }

    /**
     * Checks whether no element matches the predicate. Returns
     * {@code true} for a {@code null} array (vacuously none match).
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return {@code true} if no element matches
     * @see #none(int[], IntPredicate)
     * @see #anyMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean noneMatch(int[] array, IntPredicate predicate) {
        return !anyMatch(array, predicate);
    }

    /**
     * Alias of {@link #noneMatch(int[], IntPredicate)} — see that method
     * for full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return {@code true} if no element matches
     * @see #noneMatch(int[], IntPredicate)
     * @since 1.0.0
     */
    public static boolean none(int[] array, IntPredicate predicate) { return noneMatch(array, predicate); }

    /**
     * Checks whether the array has any duplicate values.
     *
     * @param array the array (may be null)
     * @return {@code true} if any value appears more than once
     * @see #hasRepeats(int[])
     * @see #distinct(int[])
     * @since 1.0.0
     */
    public static boolean hasDuplicates(int[] array) {
        if (array == null) return false;
        Set<Integer> seen = new HashSet<>();
        for (int v : array) if (!seen.add(v)) return true;
        return false;
    }

    /**
     * Alias of {@link #hasDuplicates(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return {@code true} if any duplicate value is present
     * @see #hasDuplicates(int[])
     * @since 1.0.0
     */
    public static boolean hasRepeats(int[] array) { return hasDuplicates(array); }

    /**
     * Checks whether the array is {@code null} or has zero length.
     *
     * @param array the array (may be null)
     * @return {@code true} if null or empty
     * @see #notEmpty(int[])
     * @since 1.0.0
     */
    public static boolean isEmpty(int[] array) { return array == null || array.length == 0; }

    /**
     * Alias of {@code !isEmpty(array)} — see {@link #isEmpty(int[])}.
     *
     * @param array the array
     * @return {@code true} if non-null and length &gt; 0
     * @see #isEmpty(int[])
     * @since 1.0.0
     */
    public static boolean notEmpty(int[] array) { return !isEmpty(array); }

    /**
     * Alias of {@link #notEmpty(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return {@code true} if non-empty
     * @see #notEmpty(int[])
     * @since 1.0.0
     */
    public static boolean hasElements(int[] array) { return notEmpty(array); }

    /**
     * Checks whether the array is sorted in ascending order.
     *
     * <p>Returns {@code true} for {@code null} or single-element arrays
     * (vacuously sorted).</p>
     *
     * @param array the array (may be null)
     * @return {@code true} if non-decreasing
     * @see #isSortedDescending(int[])
     * @see #sorted(int[])
     * @since 1.0.0
     */
    public static boolean isSorted(int[] array) {
        if (array == null || array.length <= 1) return true;
        for (int i = 1; i < array.length; i++) if (array[i] < array[i - 1]) return false;
        return true;
    }

    /**
     * Checks whether the array is sorted in descending order.
     *
     * @param array the array (may be null)
     * @return {@code true} if non-increasing
     * @see #isSorted(int[])
     * @see #sortedDescending(int[])
     * @since 1.0.0
     */
    public static boolean isSortedDescending(int[] array) {
        if (array == null || array.length <= 1) return true;
        for (int i = 1; i < array.length; i++) if (array[i] > array[i - 1]) return false;
        return true;
    }

    // ============================================================== transforms

    /**
     * Returns a reversed copy of the array. Input is not modified.
     *
     * @param array the array (may be null)
     * @return the reversed copy, or empty for null
     * @see #reversed(int[])
     * @see #flipped(int[])
     * @since 1.0.0
     */
    public static int[] reverse(int[] array) {
        if (array == null) return new int[0];
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[array.length - 1 - i];
        return result;
    }

    /**
     * Alias of {@link #reverse(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the reversed copy
     * @see #reverse(int[])
     * @since 1.0.0
     */
    public static int[] reversed(int[] array) { return reverse(array); }

    /**
     * Alias of {@link #reverse(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the reversed copy
     * @see #reverse(int[])
     * @since 1.0.0
     */
    public static int[] flipped(int[] array) { return reverse(array); }

    /**
     * Returns a sorted copy of the array in ascending order. Input is not
     * modified.
     *
     * @param array the array (may be null)
     * @return the sorted copy, or empty for null
     * @see #sort(int[])
     * @see #ascending(int[])
     * @see #sortedDescending(int[])
     * @since 1.0.0
     */
    public static int[] sorted(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        Arrays.sort(copy);
        return copy;
    }

    /**
     * Alias of {@link #sorted(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the sorted copy
     * @see #sorted(int[])
     * @since 1.0.0
     */
    public static int[] sort(int[] array) { return sorted(array); }

    /**
     * Alias of {@link #sorted(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the sorted copy
     * @see #sorted(int[])
     * @since 1.0.0
     */
    public static int[] ascending(int[] array) { return sorted(array); }

    /**
     * Alias of {@link #sorted(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the sorted copy
     * @see #sorted(int[])
     * @since 1.0.0
     */
    public static int[] sortAscending(int[] array) { return sorted(array); }

    /**
     * Returns a sorted copy of the array in descending order.
     *
     * @param array the array (may be null)
     * @return the descending-sorted copy
     * @see #descending(int[])
     * @see #sortDescending(int[])
     * @see #sorted(int[])
     * @since 1.0.0
     */
    public static int[] sortedDescending(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        Arrays.sort(copy);
        int[] result = new int[copy.length];
        for (int i = 0; i < copy.length; i++) result[i] = copy[copy.length - 1 - i];
        return result;
    }

    /**
     * Alias of {@link #sortedDescending(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the descending-sorted copy
     * @see #sortedDescending(int[])
     * @since 1.0.0
     */
    public static int[] descending(int[] array) { return sortedDescending(array); }

    /**
     * Alias of {@link #sortedDescending(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the descending-sorted copy
     * @see #sortedDescending(int[])
     * @since 1.0.0
     */
    public static int[] sortDescending(int[] array) { return sortedDescending(array); }

    /**
     * Returns a copy of the array with duplicates removed, preserving the
     * first-seen order.
     *
     * @param array the array (may be null)
     * @return the deduped array
     * @see #unique(int[])
     * @see #dedupe(int[])
     * @see #hasDuplicates(int[])
     * @since 1.0.0
     */
    public static int[] distinct(int[] array) {
        if (array == null) return new int[0];
        List<Integer> seen = new ArrayList<>();
        for (int v : array) if (!seen.contains(v)) seen.add(v);
        int[] result = new int[seen.size()];
        for (int i = 0; i < seen.size(); i++) result[i] = seen.get(i);
        return result;
    }

    /**
     * Alias of {@link #distinct(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the deduped array
     * @see #distinct(int[])
     * @since 1.0.0
     */
    public static int[] unique(int[] array) { return distinct(array); }

    /**
     * Alias of {@link #distinct(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the deduped array
     * @see #distinct(int[])
     * @since 1.0.0
     */
    public static int[] dedupe(int[] array) { return distinct(array); }

    /**
     * Rotates the array to the right by {@code steps} positions, wrapping
     * around. Negative steps rotate left.
     *
     * <pre>{@code
     * ArraysUtil.rotateRight(new int[]{1, 2, 3, 4, 5}, 2);   // [4, 5, 1, 2, 3]
     * ArraysUtil.rotateRight(new int[]{1, 2, 3}, 5);         // [2, 3, 1]  (wraps)
     * }</pre>
     *
     * @param array the array (may be null)
     * @param steps the number of positions (negative rotates left)
     * @return the rotated copy
     * @see #rotateLeft(int[], int)
     * @see #rotate(int[], int)
     * @since 1.0.0
     */
    public static int[] rotateRight(int[] array, int steps) {
        if (array == null || array.length == 0) return new int[0];
        int n = array.length;
        int shift = ((steps % n) + n) % n;
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[(i + shift) % n] = array[i];
        return result;
    }

    /**
     * Alias of {@link #rotateRight(int[], int)} — see that method for full
     * documentation and examples.
     *
     * @param array the array
     * @param steps the number of positions
     * @return the rotated copy
     * @see #rotateRight(int[], int)
     * @since 1.0.0
     */
    public static int[] rotate(int[] array, int steps) { return rotateRight(array, steps); }

    /**
     * Alias of {@link #rotateRight(int[], int)} — see that method for full
     * documentation and examples.
     *
     * @param array the array
     * @param steps the number of positions
     * @return the rotated copy
     * @see #rotateRight(int[], int)
     * @since 1.0.0
     */
    public static int[] shifted(int[] array, int steps) { return rotateRight(array, steps); }

    /**
     * Rotates the array to the left by {@code steps} positions.
     *
     * @param array the array (may be null)
     * @param steps the number of positions
     * @return the rotated copy
     * @see #rotateRight(int[], int)
     * @since 1.0.0
     */
    public static int[] rotateLeft(int[] array, int steps) {
        return rotateRight(array, -steps);
    }

    /**
     * Alias of {@link #rotateLeft(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param steps the number of positions
     * @return the rotated copy
     * @see #rotateLeft(int[], int)
     * @since 1.0.0
     */
    public static int[] rotateBack(int[] array, int steps) { return rotateLeft(array, steps); }

    /**
     * Alias of {@link #rotateLeft(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param steps the number of positions
     * @return the rotated copy
     * @see #rotateLeft(int[], int)
     * @since 1.0.0
     */
    public static int[] counterRotate(int[] array, int steps) { return rotateLeft(array, steps); }

    /**
     * Returns a shuffled copy of the array. Uses
     * {@link java.util.Collections#shuffle(List)} — not cryptographic.
     *
     * @param array the array (may be null)
     * @return the shuffled copy
     * @see #randomized(int[])
     * @see #mixed(int[])
     * @since 1.0.0
     */
    public static int[] shuffle(int[] array) {
        if (array == null) return new int[0];
        int[] copy = array.clone();
        List<Integer> list = new ArrayList<>();
        for (int v : copy) list.add(v);
        Collections.shuffle(list);
        for (int i = 0; i < copy.length; i++) copy[i] = list.get(i);
        return copy;
    }

    /**
     * Alias of {@link #shuffle(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the shuffled copy
     * @see #shuffle(int[])
     * @since 1.0.0
     */
    public static int[] randomized(int[] array) { return shuffle(array); }

    /**
     * Alias of {@link #shuffle(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the shuffled copy
     * @see #shuffle(int[])
     * @since 1.0.0
     */
    public static int[] mixed(int[] array) { return shuffle(array); }

    /**
     * Returns the sub-array from index {@code from} (inclusive) to
     * {@code to} (exclusive). Out-of-bounds indices are clamped.
     *
     * @param array the array (may be null)
     * @param from the start index
     * @param to the end index (exclusive)
     * @return the sub-array, or empty if range is invalid
     * @see #slice(int[], int, int)
     * @see #rangeOf(int[], int, int)
     * @see #take(int[], int)
     * @see #drop(int[], int)
     * @since 1.0.0
     */
    public static int[] subArray(int[] array, int from, int to) {
        if (array == null) return new int[0];
        int start = Math.max(0, from);
        int end = Math.min(array.length, to);
        if (start >= end) return new int[0];
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Alias of {@link #subArray(int[], int, int)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param from the start index
     * @param to the end index
     * @return the sub-array
     * @see #subArray(int[], int, int)
     * @since 1.0.0
     */
    public static int[] slice(int[] array, int from, int to) { return subArray(array, from, to); }

    /**
     * Alias of {@link #subArray(int[], int, int)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param from the start index
     * @param to the end index
     * @return the sub-array
     * @see #subArray(int[], int, int)
     * @since 1.0.0
     */
    public static int[] rangeOf(int[] array, int from, int to) { return subArray(array, from, to); }

    /**
     * Returns the first {@code n} elements.
     *
     * @param array the array (may be null)
     * @param n the number of elements
     * @return the first {@code n} elements, or the whole array if shorter
     * @see #head(int[], int)
     * @see #firstN(int[], int)
     * @see #drop(int[], int)
     * @see #lastN(int[], int)
     * @since 1.0.0
     */
    public static int[] take(int[] array, int n) {
        if (array == null || n <= 0) return new int[0];
        return subArray(array, 0, Math.min(n, array.length));
    }

    /**
     * Alias of {@link #take(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param n the number of elements
     * @return the first {@code n} elements
     * @see #take(int[], int)
     * @since 1.0.0
     */
    public static int[] head(int[] array, int n) { return take(array, n); }

    /**
     * Alias of {@link #take(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param n the number of elements
     * @return the first {@code n} elements
     * @see #take(int[], int)
     * @since 1.0.0
     */
    public static int[] firstN(int[] array, int n) { return take(array, n); }

    /**
     * Returns all elements after skipping the first {@code n}.
     *
     * @param array the array (may be null)
     * @param n the number of elements to skip
     * @return the remaining elements
     * @see #skip(int[], int)
     * @see #take(int[], int)
     * @since 1.0.0
     */
    public static int[] drop(int[] array, int n) {
        if (array == null || n >= array.length) return new int[0];
        return subArray(array, Math.max(0, n), array.length);
    }

    /**
     * Alias of {@link #drop(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param n the number of elements to skip
     * @return the remaining elements
     * @see #drop(int[], int)
     * @since 1.0.0
     */
    public static int[] skip(int[] array, int n) { return drop(array, n); }

    /**
     * Returns the last {@code n} elements.
     *
     * @param array the array (may be null)
     * @param n the number of trailing elements
     * @return the last {@code n} elements
     * @see #tail(int[], int)
     * @see #take(int[], int)
     * @see #drop(int[], int)
     * @since 1.0.0
     */
    public static int[] lastN(int[] array, int n) {
        if (array == null || n <= 0) return new int[0];
        return subArray(array, Math.max(0, array.length - n), array.length);
    }

    /**
     * Alias of {@link #lastN(int[], int)} — see that method for full
     * documentation.
     *
     * @param array the array
     * @param n the number of trailing elements
     * @return the last {@code n} elements
     * @see #lastN(int[], int)
     * @since 1.0.0
     */
    public static int[] tail(int[] array, int n) { return lastN(array, n); }

    /**
     * Concatenates two arrays into one.
     *
     * @param first the first array (may be null — treated as empty)
     * @param second the second array (may be null — treated as empty)
     * @return the concatenation
     * @see #merge(int[], int[])
     * @see #join(int[], int[])
     * @since 1.0.0
     */
    public static int[] concat(int[] first, int[] second) {
        int[] a = first == null ? new int[0] : first;
        int[] b = second == null ? new int[0] : second;
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Alias of {@link #concat(int[], int[])} — see that method for full
     * documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the concatenation
     * @see #concat(int[], int[])
     * @since 1.0.0
     */
    public static int[] merge(int[] first, int[] second) { return concat(first, second); }

    /**
     * Alias of {@link #concat(int[], int[])} — see that method for full
     * documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the concatenation
     * @see #concat(int[], int[])
     * @since 1.0.0
     */
    public static int[] join(int[] first, int[] second) { return concat(first, second); }

    /**
     * Returns a defensive clone of the array.
     *
     * @param array the array (may be null)
     * @return the clone, or empty for null
     * @see #copyOf(int[])
     * @since 1.0.0
     */
    public static int[] clone(int[] array) {
        return array == null ? new int[0] : array.clone();
    }

    /**
     * Alias of {@link #clone(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return the clone
     * @see #clone(int[])
     * @since 1.0.0
     */
    public static int[] copyOf(int[] array) { return clone(array); }

    // ============================================================== higher-order

    /**
     * Returns a new array containing only the elements matching the
     * predicate.
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return the filtered array
     * @see #keep(int[], IntPredicate)
     * @see #select(int[], IntPredicate)
     * @see #reject(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] filter(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return new int[0];
        List<Integer> result = new ArrayList<>();
        for (int v : array) if (predicate.test(v)) result.add(v);
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Alias of {@link #filter(int[], IntPredicate)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return the filtered array
     * @see #filter(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] keep(int[] array, IntPredicate predicate) { return filter(array, predicate); }

    /**
     * Alias of {@link #filter(int[], IntPredicate)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return the filtered array
     * @see #filter(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] select(int[] array, IntPredicate predicate) { return filter(array, predicate); }

    /**
     * Returns a new array containing only the elements <b>not</b> matching
     * the predicate.
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return the filtered array
     * @see #exclude(int[], IntPredicate)
     * @see #filter(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] reject(int[] array, IntPredicate predicate) {
        if (array == null || predicate == null) return new int[0];
        return filter(array, v -> !predicate.test(v));
    }

    /**
     * Alias of {@link #reject(int[], IntPredicate)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param predicate the test
     * @return the filtered array
     * @see #reject(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] exclude(int[] array, IntPredicate predicate) { return reject(array, predicate); }

    /**
     * Applies the function to every element, returning a new array.
     *
     * @param array the array (may be null)
     * @param mapper the transform
     * @return the mapped array
     * @see #transform(int[], IntUnaryOperator)
     * @see #filter(int[], IntPredicate)
     * @since 1.0.0
     */
    public static int[] map(int[] array, IntUnaryOperator mapper) {
        if (array == null || mapper == null) return new int[0];
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) result[i] = mapper.applyAsInt(array[i]);
        return result;
    }

    /**
     * Alias of {@link #map(int[], IntUnaryOperator)} — see that method for
     * full documentation.
     *
     * @param array the array
     * @param mapper the transform
     * @return the mapped array
     * @see #map(int[], IntUnaryOperator)
     * @since 1.0.0
     */
    public static int[] transform(int[] array, IntUnaryOperator mapper) { return map(array, mapper); }

    // ============================================================== set operations

    /**
     * Returns elements that appear in <b>both</b> arrays (deduped, in
     * first-array order).
     *
     * @param first the first array (may be null)
     * @param second the second array (may be null)
     * @return the intersection, or empty if either is null
     * @see #intersect(int[], int[])
     * @see #common(int[], int[])
     * @see #union(int[], int[])
     * @see #difference(int[], int[])
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #intersection(int[], int[])} — see that method for
     * full documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the intersection
     * @see #intersection(int[], int[])
     * @since 1.0.0
     */
    public static int[] intersect(int[] first, int[] second) { return intersection(first, second); }

    /**
     * Alias of {@link #intersection(int[], int[])} — see that method for
     * full documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the intersection
     * @see #intersection(int[], int[])
     * @since 1.0.0
     */
    public static int[] common(int[] first, int[] second) { return intersection(first, second); }

    /**
     * Returns elements that appear in at least one array, deduped.
     *
     * @param first the first array (may be null)
     * @param second the second array (may be null)
     * @return the union
     * @see #mergeDistinct(int[], int[])
     * @see #intersection(int[], int[])
     * @see #difference(int[], int[])
     * @since 1.0.0
     */
    public static int[] union(int[] first, int[] second) {
        if (first == null) return distinct(second);
        if (second == null) return distinct(first);
        return distinct(concat(first, second));
    }

    /**
     * Alias of {@link #union(int[], int[])} — see that method for full
     * documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the union
     * @see #union(int[], int[])
     * @since 1.0.0
     */
    public static int[] mergeDistinct(int[] first, int[] second) { return union(first, second); }

    /**
     * Returns elements in {@code first} that are <b>not</b> in
     * {@code second}, deduped.
     *
     * @param first the first array (may be null)
     * @param second the second array (may be null)
     * @return the difference
     * @see #except(int[], int[])
     * @see #minus(int[], int[])
     * @see #intersection(int[], int[])
     * @see #union(int[], int[])
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #difference(int[], int[])} — see that method for
     * full documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the difference
     * @see #difference(int[], int[])
     * @since 1.0.0
     */
    public static int[] except(int[] first, int[] second) { return difference(first, second); }

    /**
     * Alias of {@link #difference(int[], int[])} — see that method for
     * full documentation.
     *
     * @param first the first array
     * @param second the second array
     * @return the difference
     * @see #difference(int[], int[])
     * @since 1.0.0
     */
    public static int[] minus(int[] first, int[] second) { return difference(first, second); }

    // ============================================================== split / chunks

    /**
     * Splits the array into consecutive chunks of at most {@code size}
     * elements. The last chunk may be shorter.
     *
     * <pre>{@code
     * ArraysUtil.chunk(new int[]{1, 2, 3, 4, 5}, 2);
     * // [[1, 2], [3, 4], [5]]
     * }</pre>
     *
     * @param array the array (may be null)
     * @param size the chunk size (&le; 0 returns empty list)
     * @return the list of chunks
     * @see #chunks(int[], int)
     * @see #partitionBySize(int[], int)
     * @since 1.0.0
     */
    public static List<int[]> chunk(int[] array, int size) {
        if (array == null || size <= 0) return List.of();
        List<int[]> chunks = new ArrayList<>();
        for (int i = 0; i < array.length; i += size) {
            int end = Math.min(i + size, array.length);
            chunks.add(Arrays.copyOfRange(array, i, end));
        }
        return chunks;
    }

    /**
     * Alias of {@link #chunk(int[], int)} — see that method for full
     * documentation and examples.
     *
     * @param array the array
     * @param size the chunk size
     * @return the list of chunks
     * @see #chunk(int[], int)
     * @since 1.0.0
     */
    public static List<int[]> chunks(int[] array, int size) { return chunk(array, size); }

    /**
     * Alias of {@link #chunk(int[], int)} — see that method for full
     * documentation and examples.
     *
     * @param array the array
     * @param size the chunk size
     * @return the list of chunks
     * @see #chunk(int[], int)
     * @since 1.0.0
     */
    public static List<int[]> partitionBySize(int[] array, int size) { return chunk(array, size); }

    /**
     * Splits the array into two lists: matching and non-matching elements.
     *
     * <p>Each element is tested exactly once. Order is preserved within
     * each list.</p>
     *
     * <pre>{@code
     * var result = ArraysUtil.partition(new int[]{1, 2, 3, 4, 5}, v -> v % 2 == 0);
     * result.matching();     // [2, 4]
     * result.nonMatching();  // [1, 3, 5]
     * }</pre>
     *
     * @param array the array (may be null)
     * @param predicate the test
     * @return a {@link SplitResult} holding both lists
     * @see SplitResult
     * @see #chunk(int[], int)
     * @since 1.0.0
     */
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

    /**
     * A two-way split of a sequence — one list for elements matching a
     * predicate and one for elements that do not.
     *
     * <p>Both lists are wrapped in
     * {@link java.util.Collections#unmodifiableList} in the canonical
     * constructor, so a caller who retains a reference to the lists that
     * were passed in cannot mutate the record's internals.</p>
     *
     * @param matching the elements that passed the predicate
     * @param nonMatching the elements that failed the predicate
     * @param <T> the element type
     * @see #partition(int[], IntPredicate)
     * @see Filter.Split
     * @since 1.0.0
     */
    public record SplitResult<T>(List<T> matching, List<T> nonMatching) {
        public SplitResult {
            matching = matching == null
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(matching));
            nonMatching = nonMatching == null
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(nonMatching));
        }
        /**
         * Alias of {@link #matching()} — see that method for full
         * documentation.
         *
         * @return the matching elements
         */
        public List<T> matchingItems() { return matching; }
        /**
         * Alias of {@link #nonMatching()} — see that method for full
         * documentation.
         *
         * @return the non-matching elements
         */
        public List<T> nonMatchingItems() { return nonMatching; }
    }

    // ============================================================== matrix ops

    /**
     * Returns the transpose of the matrix — rows become columns and vice
     * versa.
     *
     * @param matrix the source matrix (may be null)
     * @return the transposed matrix, or {@code new int[0][0]}
     * @since 1.0.0
     */
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

    /**
     * Returns a copy of the specified row.
     *
     * @param matrix the matrix (may be null)
     * @param rowIndex the row index
     * @return the row, or empty if out of bounds
     * @see #getRow(int[][], int)
     * @see #col(int[][], int)
     * @since 1.0.0
     */
    public static int[] row(int[][] matrix, int rowIndex) {
        if (matrix == null || rowIndex < 0 || rowIndex >= matrix.length) return new int[0];
        return matrix[rowIndex].clone();
    }

    /**
     * Alias of {@link #row(int[][], int)} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @param rowIndex the row index
     * @return the row
     * @see #row(int[][], int)
     * @since 1.0.0
     */
    public static int[] getRow(int[][] matrix, int rowIndex) { return row(matrix, rowIndex); }

    /**
     * Returns the specified column as an array.
     *
     * <p>Ragged matrices are handled — missing cells are treated as
     * {@code 0}.</p>
     *
     * @param matrix the matrix (may be null)
     * @param colIndex the column index
     * @return the column, or empty if matrix is null or has no rows
     * @see #column(int[][], int)
     * @see #getCol(int[][], int)
     * @see #row(int[][], int)
     * @since 1.0.0
     */
    public static int[] col(int[][] matrix, int colIndex) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int[] result = new int[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            result[r] = (colIndex >= 0 && colIndex < matrix[r].length) ? matrix[r][colIndex] : 0;
        }
        return result;
    }

    /**
     * Alias of {@link #col(int[][], int)} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @param colIndex the column index
     * @return the column
     * @see #col(int[][], int)
     * @since 1.0.0
     */
    public static int[] column(int[][] matrix, int colIndex) { return col(matrix, colIndex); }

    /**
     * Alias of {@link #col(int[][], int)} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @param colIndex the column index
     * @return the column
     * @see #col(int[][], int)
     * @since 1.0.0
     */
    public static int[] getCol(int[][] matrix, int colIndex) { return col(matrix, colIndex); }

    /**
     * Returns the sum of the specified row.
     *
     * @param matrix the matrix
     * @param rowIndex the row index
     * @return the row sum, or 0 if invalid
     * @see #row(int[][], int)
     * @see #sumCol(int[][], int)
     * @since 1.0.0
     */
    public static int sumRow(int[][] matrix, int rowIndex) { return sum(row(matrix, rowIndex)); }

    /**
     * Returns the sum of the specified column.
     *
     * @param matrix the matrix
     * @param colIndex the column index
     * @return the column sum
     * @see #col(int[][], int)
     * @see #sumRow(int[][], int)
     * @since 1.0.0
     */
    public static int sumCol(int[][] matrix, int colIndex) { return sum(col(matrix, colIndex)); }

    /**
     * Returns the main diagonal (top-left to bottom-right).
     *
     * @param matrix the matrix (may be null)
     * @return the diagonal, or empty
     * @see #mainDiagonal(int[][])
     * @see #antiDiagonal(int[][])
     * @since 1.0.0
     */
    public static int[] diagonal(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int size = Math.min(matrix.length, matrix[0].length);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = matrix[i][i];
        return result;
    }

    /**
     * Alias of {@link #diagonal(int[][])} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @return the diagonal
     * @see #diagonal(int[][])
     * @since 1.0.0
     */
    public static int[] mainDiagonal(int[][] matrix) { return diagonal(matrix); }

    /**
     * Returns the anti-diagonal (top-right to bottom-left).
     *
     * @param matrix the matrix (may be null)
     * @return the anti-diagonal, or empty
     * @see #secondaryDiagonal(int[][])
     * @see #diagonal(int[][])
     * @since 1.0.0
     */
    public static int[] antiDiagonal(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0];
        int rows = matrix.length, cols = matrix[0].length;
        int size = Math.min(rows, cols);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = matrix[i][cols - 1 - i];
        return result;
    }

    /**
     * Alias of {@link #antiDiagonal(int[][])} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @return the anti-diagonal
     * @see #antiDiagonal(int[][])
     * @since 1.0.0
     */
    public static int[] secondaryDiagonal(int[][] matrix) { return antiDiagonal(matrix); }

    /**
     * Flattens the matrix into a single row-major array.
     *
     * @param matrix the matrix (may be null)
     * @return the flattened array
     * @see #flattened(int[][])
     * @since 1.0.0
     */
    public static int[] flatten(int[][] matrix) {
        if (matrix == null) return new int[0];
        List<Integer> flat = new ArrayList<>();
        for (int[] row : matrix) {
            if (row != null) for (int v : row) flat.add(v);
        }
        return flat.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Alias of {@link #flatten(int[][])} — see that method for full
     * documentation.
     *
     * @param matrix the matrix
     * @return the flattened array
     * @see #flatten(int[][])
     * @since 1.0.0
     */
    public static int[] flattened(int[][] matrix) { return flatten(matrix); }

    // ============================================================== 3D

    /**
     * Creates a cubic 3D array ({@code size × size × size}) filled with
     * the same value.
     *
     * @param size the edge length (&le; 0 returns {@code new int[0][0][0]})
     * @param value the fill value
     * @return the new 3D array
     * @since 1.0.0
     */
    public static int[][][] cube(int size, int value) {
        if (size <= 0) return new int[0][0][0];
        int[][][] cube = new int[size][size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                Arrays.fill(cube[i][j], value);
        return cube;
    }

    /**
     * Prints the 3D array layer by layer, using {@link Console} for
     * aligned output. Handles {@code null} gracefully.
     *
     * @param cube the cube to print (may be null)
     * @since 1.0.0
     */
    public static void printCube(int[][][] cube) {
        if (cube == null) { Console.println("null"); return; }
        for (int layer = 0; layer < cube.length; layer++) {
            Console.println("Layer " + layer + ":");
            for (int[] row : cube[layer]) Console.printArray(row);
        }
    }
}
