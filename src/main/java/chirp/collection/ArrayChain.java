package chirp.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * Immutable fluent chainer over an {@code int[]}.
 *
 * <p>{@code ArrayChain} wraps a private {@code int[]} copy. Every
 * transform returns a <b>new</b> chain wrapping a new array — so the chain
 * is immutable and thread-safe, and the array inside a chain instance
 * never changes after construction.</p>
 *
 * <h2>Two ways to build a chain</h2>
 * <pre>{@code
 * // 1. From an existing array
 * int[] original = {5, 3, 8, 1, 9, 2};
 * ArrayChain chain = ArrayChain.of(original);
 *
 * // 2. From individual values (varargs)
 * ArrayChain chain2 = ArrayChain.of(5, 3, 8, 1, 9, 2);
 * }</pre>
 *
 * <p>Both forms accept the same input because Java treats varargs
 * identically to an array at the call site. A call like
 * {@code of(nums)} passes the array as-is; {@code of(1, 2, 3)} creates a
 * new array of those values.</p>
 *
 * <h2>The Decorator pattern — value-based, not lazy</h2>
 * <p>{@code ArrayChain} demonstrates the <b>Decorator</b> design pattern
 * in the same structure as {@link chirp.text.TextChain} and
 * {@link chirp.number.NumberChain} — each method returns a new wrapper.
 * But there is one crucial difference: array chains are <b>value-based</b>,
 * not lazy.</p>
 *
 * <p>When you call {@code .filter(...)}, the filtering happens
 * <b>immediately</b>. The new chain is constructed around the resulting
 * array. This means:</p>
 * <ul>
 *   <li>Each intermediate step allocates one array — a chain of 5
 *       transforms allocates 5 arrays.</li>
 *   <li>A chain held across multiple reads does not re-compute on each
 *       terminal call.</li>
 *   <li>Failing fast: a {@code NullPointerException} from a user-supplied
 *       predicate throws at the exact line of the chain, not at the
 *       terminal call.</li>
 * </ul>
 *
 * <p>Why value-based rather than lazy? Because array filtering and
 * mapping allocate new arrays per step anyway — there is no "lazy win"
 * to deferring the computation. The lazy model that works well for
 * single-value chains (text, numbers) does not help when each step
 * already produces a new array. Value-based keeps the mental model
 * simpler: what you see at each step is the actual array.</p>
 *
 * <h2>Immutability</h2>
 * <p>{@link #toArray()} returns a <b>defensive copy</b> of the internal
 * array. Callers who mutate the returned array cannot affect the chain,
 * and callers who mutate the original input array cannot affect the
 * chain either — the constructor copies on entry.</p>
 *
 * <pre>{@code
 * int[] original = {1, 2, 3};
 * ArrayChain chain = ArrayChain.of(original);
 *
 * original[0] = 999;          // mutating the source does not change the chain
 * chain.toArray();            // still [1, 2, 3]
 *
 * int[] result = chain.toArray();
 * result[0] = 888;            // mutating the result does not change the chain
 * chain.toArray();            // still [1, 2, 3]
 * }</pre>
 *
 * <h2>Null safety</h2>
 * <p>{@link #of(int[])} treats {@code null} as empty. Every transform
 * returns a new chain (never {@code null}), and every terminal returns a
 * safe primitive or an empty array/list. A chain is never {@code null}
 * after construction.</p>
 *
 * <h2>Example — a typical pipeline</h2>
 * <pre>{@code
 * int[] result = ArrayChain.of(5, -3, 8, 0, 9, 2)
 *     .filter(v -> v > 0)       // [5, 8, 9, 2]
 *     .sorted()                 // [2, 5, 8, 9]
 *     .take(3)                  // [2, 5, 8]
 *     .toArray();
 * }</pre>
 *
 * <h2>When to use a chain vs. static methods</h2>
 * <p>Static methods on {@link ArraysUtil} are fine for one or two
 * operations:</p>
 * <pre>{@code
 * int max = ArraysUtil.max(ArraysUtil.filter(nums, v -> v > 0));
 * }</pre>
 * <p>A chain reads left-to-right and scales better once there are three
 * or more operations. Both produce identical results for the same
 * sequence of transformations.</p>
 *
 * <h2>Related</h2>
 * <p>For static operations, see {@link ArraysUtil}. For the fluent text
 * equivalent, see {@link chirp.text.TextChain}. For fluent numeric
 * chains, see {@link chirp.number.NumberChain}. For fluent date chains,
 * see {@link chirp.datetime.DateChain}.</p>
 *
 * @see ArraysUtil
 * @see chirp.text.TextChain
 * @see chirp.number.NumberChain
 * @see chirp.datetime.DateChain
 * @since 1.0.0
 */
public final class ArrayChain {

    private final int[] array;

    /**
     * Internal constructor — copies the input array defensively.
     * Package-private; callers use {@link #of(int...)} and its aliases.
     *
     * @param array the source array (may be null — treated as empty)
     */
    private ArrayChain(int[] array) {
        this.array = array == null ? new int[0] : array;
    }

    // ============================================================== factories

    /**
     * Creates a chain from the values or the array.
     *
     * <p>Because Java treats varargs and arrays identically at the call
     * site, this single signature handles both forms:</p>
     * <pre>{@code
     * ArrayChain.of(1, 2, 3);              // varargs
     * ArrayChain.of(new int[]{1, 2, 3});   // array
     * }</pre>
     *
     * @param values the values or array (may be null — treated as empty)
     * @return a new chain wrapping a defensive copy of {@code values}
     * @see #on(int...)
     * @see #from(int...)
     * @see #start(int...)
     * @see #ofRange(int, int)
     * @since 1.0.0
     */
    public static ArrayChain of(int... values) { return new ArrayChain(values); }

    /**
     * Alias of {@link #of(int...)} — see that method for full
     * documentation and examples.
     *
     * @param values the values or array (may be null)
     * @return a new chain
     * @see #of(int...)
     * @since 1.0.0
     */
    public static ArrayChain on(int... values) { return of(values); }

    /**
     * Alias of {@link #of(int...)} — see that method for full
     * documentation and examples.
     *
     * @param values the values or array (may be null)
     * @return a new chain
     * @see #of(int...)
     * @since 1.0.0
     */
    public static ArrayChain from(int... values) { return of(values); }

    /**
     * Alias of {@link #of(int...)} — see that method for full
     * documentation and examples.
     *
     * @param values the values or array (may be null)
     * @return a new chain
     * @see #of(int...)
     * @since 1.0.0
     */
    public static ArrayChain start(int... values) { return of(values); }

    /**
     * Creates a chain from a range of consecutive integers
     * {@code [start, start+1, …, end-1]}.
     *
     * <pre>{@code
     * ArrayChain.ofRange(0, 5).toArray();   // [0, 1, 2, 3, 4]
     * }</pre>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return a new chain wrapping the range
     * @see ArraysUtil#rangeArray(int, int)
     * @since 1.0.0
     */
    public static ArrayChain ofRange(int start, int end) {
        return new ArrayChain(ArraysUtil.rangeArray(start, end));
    }

    // ============================================================== transforms

    /**
     * Keeps only the elements matching the predicate.
     *
     * <pre>{@code
     * ArrayChain.of(3, 1, 4, 5, 6).filter(v -> v % 2 == 0).toArray();
     * // [4, 6]
     * }</pre>
     *
     * @param predicate the test (may be null — returns an empty chain)
     * @return a new chain with the filtered elements
     * @see #keep(IntPredicate)
     * @see #select(IntPredicate)
     * @see #reject(IntPredicate)
     * @see ArraysUtil#filter(int[], IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain filter(IntPredicate predicate) { return new ArrayChain(ArraysUtil.filter(array, predicate)); }

    /**
     * Alias of {@link #filter(IntPredicate)} — see that method for full
     * documentation and examples.
     *
     * @param predicate the test
     * @return a new chain with the filtered elements
     * @see #filter(IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain keep(IntPredicate predicate) { return filter(predicate); }

    /**
     * Alias of {@link #filter(IntPredicate)} — see that method for full
     * documentation and examples.
     *
     * @param predicate the test
     * @return a new chain with the filtered elements
     * @see #filter(IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain select(IntPredicate predicate) { return filter(predicate); }

    /**
     * Keeps only the elements that do <b>not</b> match the predicate.
     *
     * @param predicate the test to negate
     * @return a new chain with the rejected elements removed
     * @see #exclude(IntPredicate)
     * @see #removeIf(IntPredicate)
     * @see #filter(IntPredicate)
     * @see ArraysUtil#reject(int[], IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain reject(IntPredicate predicate) { return new ArrayChain(ArraysUtil.reject(array, predicate)); }

    /**
     * Alias of {@link #reject(IntPredicate)} — see that method for full
     * documentation.
     *
     * @param predicate the test to negate
     * @return a new chain with the rejected elements removed
     * @see #reject(IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain exclude(IntPredicate predicate) { return reject(predicate); }

    /**
     * Alias of {@link #reject(IntPredicate)} — see that method for full
     * documentation.
     *
     * @param predicate the test to negate
     * @return a new chain with the rejected elements removed
     * @see #reject(IntPredicate)
     * @since 1.0.0
     */
    public ArrayChain removeIf(IntPredicate predicate) { return reject(predicate); }

    /**
     * Applies the function to every element, returning a new chain with
     * the mapped values.
     *
     * <pre>{@code
     * ArrayChain.of(1, 2, 3).map(v -> v * 10).toArray();
     * // [10, 20, 30]
     * }</pre>
     *
     * @param mapper the transform (may be null — returns an empty chain)
     * @return a new chain with the mapped elements
     * @see #transform(IntUnaryOperator)
     * @see ArraysUtil#map(int[], IntUnaryOperator)
     * @since 1.0.0
     */
    public ArrayChain map(IntUnaryOperator mapper) { return new ArrayChain(ArraysUtil.map(array, mapper)); }

    /**
     * Alias of {@link #map(IntUnaryOperator)} — see that method for full
     * documentation and examples.
     *
     * @param mapper the transform
     * @return a new chain with the mapped elements
     * @see #map(IntUnaryOperator)
     * @since 1.0.0
     */
    public ArrayChain transform(IntUnaryOperator mapper) { return map(mapper); }

    /**
     * Sorts the elements in ascending order.
     *
     * @return a new chain with the sorted elements
     * @see #sort()
     * @see #ascending()
     * @see #ascendingOrder()
     * @see #descending()
     * @see ArraysUtil#sorted(int[])
     * @since 1.0.0
     */
    public ArrayChain sorted() { return new ArrayChain(ArraysUtil.sorted(array)); }

    /**
     * Alias of {@link #sorted()} — see that method for full documentation.
     *
     * @return a new chain with the sorted elements
     * @see #sorted()
     * @since 1.0.0
     */
    public ArrayChain sort() { return sorted(); }

    /**
     * Alias of {@link #sorted()} — see that method for full documentation.
     *
     * @return a new chain with the sorted elements
     * @see #sorted()
     * @since 1.0.0
     */
    public ArrayChain ascending() { return sorted(); }

    /**
     * Alias of {@link #sorted()} — see that method for full documentation.
     *
     * @return a new chain with the sorted elements
     * @see #sorted()
     * @since 1.0.0
     */
    public ArrayChain ascendingOrder() { return sorted(); }

    /**
     * Sorts the elements in descending order.
     *
     * @return a new chain with the descending-sorted elements
     * @see #sortedDesc()
     * @see #reverseSorted()
     * @see #sorted()
     * @see ArraysUtil#sortedDescending(int[])
     * @since 1.0.0
     */
    public ArrayChain descending() { return new ArrayChain(ArraysUtil.sortedDescending(array)); }

    /**
     * Alias of {@link #descending()} — see that method for full
     * documentation.
     *
     * @return a new chain with the descending-sorted elements
     * @see #descending()
     * @since 1.0.0
     */
    public ArrayChain sortedDesc() { return descending(); }

    /**
     * Alias of {@link #descending()} — see that method for full
     * documentation.
     *
     * @return a new chain with the descending-sorted elements
     * @see #descending()
     * @since 1.0.0
     */
    public ArrayChain reverseSorted() { return descending(); }

    /**
     * Reverses the order of the elements.
     *
     * @return a new chain with the reversed elements
     * @see #reversed()
     * @see #flipped()
     * @see ArraysUtil#reverse(int[])
     * @since 1.0.0
     */
    public ArrayChain reverse() { return new ArrayChain(ArraysUtil.reverse(array)); }

    /**
     * Alias of {@link #reverse()} — see that method for full
     * documentation.
     *
     * @return a new chain with the reversed elements
     * @see #reverse()
     * @since 1.0.0
     */
    public ArrayChain reversed() { return reverse(); }

    /**
     * Alias of {@link #reverse()} — see that method for full
     * documentation.
     *
     * @return a new chain with the reversed elements
     * @see #reverse()
     * @since 1.0.0
     */
    public ArrayChain flipped() { return reverse(); }

    /**
     * Removes duplicate values, preserving first-seen order.
     *
     * @return a new chain with duplicates removed
     * @see #unique()
     * @see #dedupe()
     * @see ArraysUtil#distinct(int[])
     * @since 1.0.0
     */
    public ArrayChain distinct() { return new ArrayChain(ArraysUtil.distinct(array)); }

    /**
     * Alias of {@link #distinct()} — see that method for full
     * documentation.
     *
     * @return a new chain with duplicates removed
     * @see #distinct()
     * @since 1.0.0
     */
    public ArrayChain unique() { return distinct(); }

    /**
     * Alias of {@link #distinct()} — see that method for full
     * documentation.
     *
     * @return a new chain with duplicates removed
     * @see #distinct()
     * @since 1.0.0
     */
    public ArrayChain dedupe() { return distinct(); }

    /**
     * Returns a shuffled copy of the chain.
     *
     * @return a new chain with shuffled elements
     * @see #randomized()
     * @see #mixed()
     * @see ArraysUtil#shuffle(int[])
     * @since 1.0.0
     */
    public ArrayChain shuffle() { return new ArrayChain(ArraysUtil.shuffle(array)); }

    /**
     * Alias of {@link #shuffle()} — see that method for full
     * documentation.
     *
     * @return a new chain with shuffled elements
     * @see #shuffle()
     * @since 1.0.0
     */
    public ArrayChain randomized() { return shuffle(); }

    /**
     * Alias of {@link #shuffle()} — see that method for full
     * documentation.
     *
     * @return a new chain with shuffled elements
     * @see #shuffle()
     * @since 1.0.0
     */
    public ArrayChain mixed() { return shuffle(); }

    /**
     * Rotates the elements to the right by {@code steps} positions.
     *
     * @param steps the number of positions (negative rotates left)
     * @return a new chain with rotated elements
     * @see #rotateRight(int)
     * @see #rotateLeft(int)
     * @see ArraysUtil#rotateRight(int[], int)
     * @since 1.0.0
     */
    public ArrayChain rotate(int steps) { return new ArrayChain(ArraysUtil.rotateRight(array, steps)); }

    /**
     * Alias of {@link #rotate(int)} — see that method for full
     * documentation.
     *
     * @param steps the number of positions
     * @return a new chain with rotated elements
     * @see #rotate(int)
     * @since 1.0.0
     */
    public ArrayChain rotateRight(int steps) { return rotate(steps); }

    /**
     * Rotates the elements to the left by {@code steps} positions.
     *
     * @param steps the number of positions
     * @return a new chain with rotated elements
     * @see #rotate(int)
     * @see ArraysUtil#rotateLeft(int[], int)
     * @since 1.0.0
     */
    public ArrayChain rotateLeft(int steps) { return new ArrayChain(ArraysUtil.rotateLeft(array, steps)); }

    /**
     * Returns a slice from index {@code from} (inclusive) to {@code to}
     * (exclusive).
     *
     * @param from the start index
     * @param to the end index (exclusive)
     * @return a new chain with the sliced elements
     * @see #subArray(int, int)
     * @see #rangeOf(int, int)
     * @see ArraysUtil#subArray(int[], int, int)
     * @since 1.0.0
     */
    public ArrayChain slice(int from, int to) { return new ArrayChain(ArraysUtil.subArray(array, from, to)); }

    /**
     * Alias of {@link #slice(int, int)} — see that method for full
     * documentation.
     *
     * @param from the start index
     * @param to the end index
     * @return a new chain with the sliced elements
     * @see #slice(int, int)
     * @since 1.0.0
     */
    public ArrayChain subArray(int from, int to) { return slice(from, to); }

    /**
     * Alias of {@link #slice(int, int)} — see that method for full
     * documentation.
     *
     * @param from the start index
     * @param to the end index
     * @return a new chain with the sliced elements
     * @see #slice(int, int)
     * @since 1.0.0
     */
    public ArrayChain rangeOf(int from, int to) { return slice(from, to); }

    /**
     * Returns a chain containing only the first {@code n} elements.
     *
     * @param n the number of leading elements
     * @return a new chain with the first {@code n} elements
     * @see #head(int)
     * @see #limit(int)
     * @see #drop(int)
     * @see ArraysUtil#take(int[], int)
     * @since 1.0.0
     */
    public ArrayChain take(int n) { return new ArrayChain(ArraysUtil.take(array, n)); }

    /**
     * Alias of {@link #take(int)} — see that method for full
     * documentation.
     *
     * @param n the number of leading elements
     * @return a new chain with the first {@code n} elements
     * @see #take(int)
     * @since 1.0.0
     */
    public ArrayChain head(int n) { return take(n); }

    /**
     * Alias of {@link #take(int)} — see that method for full
     * documentation.
     *
     * @param n the number of leading elements
     * @return a new chain with the first {@code n} elements
     * @see #take(int)
     * @since 1.0.0
     */
    public ArrayChain limit(int n) { return take(n); }

    /**
     * Returns a chain skipping the first {@code n} elements.
     *
     * @param n the number of elements to skip
     * @return a new chain with the remaining elements
     * @see #skip(int)
     * @see #take(int)
     * @see ArraysUtil#drop(int[], int)
     * @since 1.0.0
     */
    public ArrayChain drop(int n) { return new ArrayChain(ArraysUtil.drop(array, n)); }

    /**
     * Alias of {@link #drop(int)} — see that method for full
     * documentation.
     *
     * @param n the number of elements to skip
     * @return a new chain with the remaining elements
     * @see #drop(int)
     * @since 1.0.0
     */
    public ArrayChain skip(int n) { return drop(n); }

    /**
     * Returns a chain containing only the last {@code n} elements.
     *
     * <p>Note the naming contrast with {@link #drop(int)}: {@code tail(n)}
     * returns the <b>last</b> {@code n} elements, not the elements after
     * the first {@code n}. This matches the UNIX {@code tail} command.</p>
     *
     * @param n the number of trailing elements
     * @return a new chain with the last {@code n} elements
     * @see ArraysUtil#lastN(int[], int)
     * @since 1.0.0
     */
    public ArrayChain tail(int n) { return new ArrayChain(ArraysUtil.lastN(array, n)); }

    /**
     * Concatenates another array onto the end of this chain.
     *
     * @param other the array to append (may be null — treated as empty)
     * @return a new chain with the concatenated elements
     * @see #merge(int[])
     * @see #append(int[])
     * @see #concat(ArrayChain)
     * @see ArraysUtil#concat(int[], int[])
     * @since 1.0.0
     */
    public ArrayChain concat(int[] other) { return new ArrayChain(ArraysUtil.concat(array, other)); }

    /**
     * Alias of {@link #concat(int[])} — see that method for full
     * documentation.
     *
     * @param other the array to append
     * @return a new chain with the concatenated elements
     * @see #concat(int[])
     * @since 1.0.0
     */
    public ArrayChain merge(int[] other) { return concat(other); }

    /**
     * Alias of {@link #concat(int[])} — see that method for full
     * documentation.
     *
     * @param other the array to append
     * @return a new chain with the concatenated elements
     * @see #concat(int[])
     * @since 1.0.0
     */
    public ArrayChain append(int[] other) { return concat(other); }

    /**
     * Concatenates another chain onto the end of this one.
     *
     * @param other the chain to append (may be null — returns this chain
     * unchanged)
     * @return a new chain with the concatenated elements
     * @see #concat(int[])
     * @since 1.0.0
     */
    public ArrayChain concat(ArrayChain other) {
        return other == null ? this : new ArrayChain(ArraysUtil.concat(array, other.array));
    }

    // ============================================================== terminals

    /**
     * Returns a defensive copy of the internal array.
     *
     * @return a new array with the chain's elements
     * @see #asIntArray()
     * @see #get()
     * @see #value()
     * @since 1.0.0
     */
    public int[] toArray() { return array.clone(); }

    /**
     * Alias of {@link #toArray()} — see that method for full
     * documentation.
     *
     * @return a new array with the chain's elements
     * @see #toArray()
     * @since 1.0.0
     */
    public int[] asIntArray() { return toArray(); }

    /**
     * Alias of {@link #toArray()} — see that method for full
     * documentation.
     *
     * @return a new array with the chain's elements
     * @see #toArray()
     * @since 1.0.0
     */
    public int[] get() { return toArray(); }

    /**
     * Alias of {@link #toArray()} — see that method for full
     * documentation.
     *
     * @return a new array with the chain's elements
     * @see #toArray()
     * @since 1.0.0
     */
    public int[] value() { return toArray(); }

    /**
     * Returns the elements as a {@link List} of {@link Integer}.
     *
     * @return a new mutable list containing the chain's elements
     * @see #toList()
     * @see #toArray()
     * @since 1.0.0
     */
    public List<Integer> asList() {
        List<Integer> list = new ArrayList<>(array.length);
        for (int v : array) list.add(v);
        return list;
    }

    /**
     * Alias of {@link #asList()} — see that method for full
     * documentation.
     *
     * @return a new mutable list containing the chain's elements
     * @see #asList()
     * @since 1.0.0
     */
    public List<Integer> toList() { return asList(); }

    /**
     * Returns the number of elements in the chain.
     *
     * @return the element count
     * @see #length()
     * @see #size()
     * @see #isEmpty()
     * @since 1.0.0
     */
    public int count() { return array.length; }

    /**
     * Alias of {@link #count()} — see that method for full documentation.
     *
     * @return the element count
     * @see #count()
     * @since 1.0.0
     */
    public int length() { return array.length; }

    /**
     * Alias of {@link #count()} — see that method for full documentation.
     *
     * @return the element count
     * @see #count()
     * @since 1.0.0
     */
    public int size() { return array.length; }

    /**
     * Checks whether the chain has no elements.
     *
     * @return {@code true} if the chain is empty
     * @see #isNotEmpty()
     * @see #count()
     * @since 1.0.0
     */
    public boolean isEmpty() { return array.length == 0; }

    /**
     * Checks whether the chain has at least one element.
     *
     * @return {@code true} if the chain is not empty
     * @see #isEmpty()
     * @since 1.0.0
     */
    public boolean isNotEmpty() { return array.length > 0; }

    /**
     * Returns the sum of the elements.
     *
     * @return the sum
     * @see #total()
     * @see #average()
     * @see ArraysUtil#sum(int[])
     * @since 1.0.0
     */
    public int sum() { return ArraysUtil.sum(array); }

    /**
     * Alias of {@link #sum()} — see that method for full documentation.
     *
     * @return the sum
     * @see #sum()
     * @since 1.0.0
     */
    public int total() { return sum(); }

    /**
     * Returns the arithmetic mean of the elements.
     *
     * @return the mean, or 0 if the chain is empty
     * @see #avg()
     * @see #mean()
     * @see #sum()
     * @see ArraysUtil#average(int[])
     * @since 1.0.0
     */
    public double average() { return ArraysUtil.average(array); }

    /**
     * Alias of {@link #average()} — see that method for full
     * documentation.
     *
     * @return the mean
     * @see #average()
     * @since 1.0.0
     */
    public double avg() { return average(); }

    /**
     * Alias of {@link #average()} — see that method for full
     * documentation.
     *
     * @return the mean
     * @see #average()
     * @since 1.0.0
     */
    public double mean() { return average(); }

    /**
     * Returns the maximum element.
     *
     * @return the maximum, or 0 if empty
     * @see #min()
     * @see #largest()
     * @see #smallest()
     * @see ArraysUtil#max(int[])
     * @since 1.0.0
     */
    public int max() { return ArraysUtil.max(array); }

    /**
     * Returns the minimum element.
     *
     * @return the minimum, or 0 if empty
     * @see #max()
     * @see ArraysUtil#min(int[])
     * @since 1.0.0
     */
    public int min() { return ArraysUtil.min(array); }

    /**
     * Alias of {@link #max()} — see that method for full documentation.
     *
     * @return the maximum
     * @see #max()
     * @since 1.0.0
     */
    public int largest() { return max(); }

    /**
     * Alias of {@link #min()} — see that method for full documentation.
     *
     * @return the minimum
     * @see #min()
     * @since 1.0.0
     */
    public int smallest() { return min(); }

    /**
     * Returns the first element, or {@code 0} if empty.
     *
     * @return the first element
     * @see #firstOr(int)
     * @see #last()
     * @since 1.0.0
     */
    public int first() { return array.length == 0 ? 0 : array[0]; }

    /**
     * Returns the last element, or {@code 0} if empty.
     *
     * @return the last element
     * @see #lastOr(int)
     * @see #first()
     * @since 1.0.0
     */
    public int last() { return array.length == 0 ? 0 : array[array.length - 1]; }

    /**
     * Returns the first element, or {@code d} if empty.
     *
     * @param d the fallback value
     * @return the first element, or the fallback
     * @see #first()
     * @since 1.0.0
     */
    public int firstOr(int d) { return array.length == 0 ? d : array[0]; }

    /**
     * Returns the last element, or {@code d} if empty.
     *
     * @param d the fallback value
     * @return the last element, or the fallback
     * @see #last()
     * @since 1.0.0
     */
    public int lastOr(int d) { return array.length == 0 ? d : array[array.length - 1]; }

    /**
     * Checks whether the chain contains the target value.
     *
     * @param target the value to find
     * @return {@code true} if present
     * @see #indexOf(int)
     * @see #lastIndexOf(int)
     * @see ArraysUtil#contains(int[], int)
     * @since 1.0.0
     */
    public boolean contains(int target) { return ArraysUtil.contains(array, target); }

    /**
     * Returns the index of the first occurrence of the target.
     *
     * @param target the value to find
     * @return the index, or -1 if absent
     * @see #lastIndexOf(int)
     * @see #contains(int)
     * @see ArraysUtil#indexOf(int[], int)
     * @since 1.0.0
     */
    public int indexOf(int target) { return ArraysUtil.indexOf(array, target); }

    /**
     * Returns the index of the last occurrence of the target.
     *
     * @param t the value to find
     * @return the index, or -1 if absent
     * @see #indexOf(int)
     * @see ArraysUtil#lastIndexOf(int[], int)
     * @since 1.0.0
     */
    public int lastIndexOf(int t) { return ArraysUtil.lastIndexOf(array, t); }

    /**
     * Checks whether the chain has any duplicate values.
     *
     * @return {@code true} if any value appears more than once
     * @see #distinct()
     * @see ArraysUtil#hasDuplicates(int[])
     * @since 1.0.0
     */
    public boolean hasDuplicates() { return ArraysUtil.hasDuplicates(array); }

    /**
     * Checks whether the elements are in ascending order.
     *
     * @return {@code true} if sorted ascending
     * @see #sorted()
     * @see ArraysUtil#isSorted(int[])
     * @since 1.0.0
     */
    public boolean isSorted() { return ArraysUtil.isSorted(array); }

    /**
     * Joins the elements into a single string with the given delimiter.
     *
     * <pre>{@code
     * ArrayChain.of(1, 2, 3).join(", ");   // "1, 2, 3"
     * ArrayChain.of().join(", ");          // ""
     * }</pre>
     *
     * @param delimiter the separator to insert between elements
     * @return the joined string
     * @see #join(String, String, String)
     * @since 1.0.0
     */
    public String join(String delimiter) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * Joins the elements into a single string with a delimiter, wrapped
     * in a prefix and suffix.
     *
     * <pre>{@code
     * ArrayChain.of(1, 2, 3).join(", ", "[", "]");   // "[1, 2, 3]"
     * ArrayChain.of().join(", ", "[", "]");          // "[]"
     * }</pre>
     *
     * @param delimiter the separator to insert between elements
     * @param prefix the string to place before the first element
     * @param suffix the string to place after the last element
     * @return the joined string
     * @see #join(String)
     * @since 1.0.0
     */
    public String join(String delimiter, String prefix, String suffix) {
        if (array.length == 0) return prefix + suffix;
        return prefix + join(delimiter) + suffix;
    }

    /**
     * Alias of {@link Arrays#toString(int[])} applied to the chain's
     * contents — returns the same bracket-comma format used by
     * {@link java.util.Arrays#toString(int[])}.
     *
     * @return the string representation of the elements
     * @see #join(String)
     * @since 1.0.0
     */
    @Override
    public String toString() { return Arrays.toString(array); }
}
