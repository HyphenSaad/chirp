package chirp.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * Immutable fluent chain over an {@code int[]}.
 *
 * <p><b>Naming note:</b> {@code of(int...)} and {@code from(int...)} accept
 * either a list of ints or an {@code int[]} — Java treats both identically
 * for varargs.</p>
 */
public final class ArrayChain {

    private final int[] array;

    private ArrayChain(int[] array) {
        this.array = array == null ? new int[0] : array;
    }

    // Factories — varargs form works with both `of(1, 2, 3)` and `of(arr)`
    public static ArrayChain of(int... values) { return new ArrayChain(values); }
    public static ArrayChain on(int... values) { return of(values); }
    public static ArrayChain from(int... values) { return of(values); }
    public static ArrayChain start(int... values) { return of(values); }
    public static ArrayChain ofRange(int start, int end) { return new ArrayChain(ArraysUtil.rangeArray(start, end)); }

    // ============================================================== transforms

    public ArrayChain filter(IntPredicate predicate) { return new ArrayChain(ArraysUtil.filter(array, predicate)); }
    public ArrayChain keep(IntPredicate predicate) { return filter(predicate); }
    public ArrayChain select(IntPredicate predicate) { return filter(predicate); }

    public ArrayChain reject(IntPredicate predicate) { return new ArrayChain(ArraysUtil.reject(array, predicate)); }
    public ArrayChain exclude(IntPredicate predicate) { return reject(predicate); }
    public ArrayChain removeIf(IntPredicate predicate) { return reject(predicate); }

    public ArrayChain map(IntUnaryOperator mapper) { return new ArrayChain(ArraysUtil.map(array, mapper)); }
    public ArrayChain transform(IntUnaryOperator mapper) { return map(mapper); }

    public ArrayChain sorted() { return new ArrayChain(ArraysUtil.sorted(array)); }
    public ArrayChain sort() { return sorted(); }
    public ArrayChain ascending() { return sorted(); }
    public ArrayChain ascendingOrder() { return sorted(); }

    public ArrayChain descending() { return new ArrayChain(ArraysUtil.sortedDescending(array)); }
    public ArrayChain sortedDesc() { return descending(); }
    public ArrayChain reverseSorted() { return descending(); }

    public ArrayChain reverse() { return new ArrayChain(ArraysUtil.reverse(array)); }
    public ArrayChain reversed() { return reverse(); }
    public ArrayChain flipped() { return reverse(); }

    public ArrayChain distinct() { return new ArrayChain(ArraysUtil.distinct(array)); }
    public ArrayChain unique() { return distinct(); }
    public ArrayChain dedupe() { return distinct(); }

    public ArrayChain shuffle() { return new ArrayChain(ArraysUtil.shuffle(array)); }
    public ArrayChain randomized() { return shuffle(); }
    public ArrayChain mixed() { return shuffle(); }

    public ArrayChain rotate(int steps) { return new ArrayChain(ArraysUtil.rotateRight(array, steps)); }
    public ArrayChain rotateRight(int steps) { return rotate(steps); }
    public ArrayChain rotateLeft(int steps) { return new ArrayChain(ArraysUtil.rotateLeft(array, steps)); }

    public ArrayChain slice(int from, int to) { return new ArrayChain(ArraysUtil.subArray(array, from, to)); }
    public ArrayChain subArray(int from, int to) { return slice(from, to); }
    public ArrayChain rangeOf(int from, int to) { return slice(from, to); }

    public ArrayChain take(int n) { return new ArrayChain(ArraysUtil.take(array, n)); }
    public ArrayChain head(int n) { return take(n); }
    public ArrayChain limit(int n) { return take(n); }

    public ArrayChain drop(int n) { return new ArrayChain(ArraysUtil.drop(array, n)); }
    public ArrayChain skip(int n) { return drop(n); }
    public ArrayChain tail(int n) { return new ArrayChain(ArraysUtil.lastN(array, n)); }

    public ArrayChain concat(int[] other) { return new ArrayChain(ArraysUtil.concat(array, other)); }
    public ArrayChain merge(int[] other) { return concat(other); }
    public ArrayChain append(int[] other) { return concat(other); }

    public ArrayChain concat(ArrayChain other) {
        return other == null ? this : new ArrayChain(ArraysUtil.concat(array, other.array));
    }

    // ============================================================== terminals

    public int[] toArray() { return array.clone(); }
    public int[] asIntArray() { return toArray(); }
    public int[] get() { return toArray(); }
    public int[] value() { return toArray(); }

    public List<Integer> asList() {
        List<Integer> list = new ArrayList<>(array.length);
        for (int v : array) list.add(v);
        return list;
    }
    public List<Integer> toList() { return asList(); }

    public int count() { return array.length; }
    public int length() { return array.length; }
    public int size() { return array.length; }
    public boolean isEmpty() { return array.length == 0; }
    public boolean isNotEmpty() { return array.length > 0; }

    public int sum() { return ArraysUtil.sum(array); }
    public int total() { return sum(); }
    public double average() { return ArraysUtil.average(array); }
    public double avg() { return average(); }
    public double mean() { return average(); }

    public int max() { return ArraysUtil.max(array); }
    public int min() { return ArraysUtil.min(array); }
    public int largest() { return max(); }
    public int smallest() { return min(); }

    public int first() { return array.length == 0 ? 0 : array[0]; }
    public int last() { return array.length == 0 ? 0 : array[array.length - 1]; }
    public int firstOr(int d) { return array.length == 0 ? d : array[0]; }
    public int lastOr(int d) { return array.length == 0 ? d : array[array.length - 1]; }

    public boolean contains(int target) { return ArraysUtil.contains(array, target); }
    public int indexOf(int target) { return ArraysUtil.indexOf(array, target); }
    public int lastIndexOf(int t) { return ArraysUtil.lastIndexOf(array, t); }

    public boolean hasDuplicates() { return ArraysUtil.hasDuplicates(array); }
    public boolean isSorted() { return ArraysUtil.isSorted(array); }

    public String join(String delimiter) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public String join(String delimiter, String prefix, String suffix) {
        if (array.length == 0) return prefix + suffix;
        return prefix + join(delimiter) + suffix;
    }

    @Override
    public String toString() { return Arrays.toString(array); }
}
