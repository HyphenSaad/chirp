package chirp.collection;

import chirp.console.Console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Convenience operations on {@code List<Integer>} (and generic lists where
 * appropriate) — null-safe and empty-safe throughout.
 *
 * <p>This class is the list analogue of {@link ArraysUtil}. Where that
 * works with {@code int[]}, this works with {@code List<Integer>}. The
 * numeric aggregations ({@code sum}, {@code average}, {@code max},
 * {@code min}) require {@code List<Integer>} because they need an integer
 * value; the transforms ({@code sorted}, {@code distinct}, {@code filter},
 * {@code map}, {@code take}, {@code drop}) are generic on {@code List<T>}
 * so they work for any element type.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Numeric aggregation</b> — {@link #sum(List)},
 *       {@link #average(List)}, {@link #max(List)},
 *       {@link #min(List)}, {@link #count(List, Integer)}</li>
 *   <li><b>Search</b> — {@link #contains(List, Object)},
 *       {@link #indexOf(List, Object)},
 *       {@link #hasDuplicates(List)}</li>
 *   <li><b>Sorting</b> — {@link #sorted(List)},
 *       {@link #sortedDescending(List)}, {@link #reverse(List)}</li>
 *   <li><b>Set-like</b> — {@link #distinct(List)}</li>
 *   <li><b>Slicing</b> — {@link #take(List, int)},
 *       {@link #drop(List, int)}, {@link #tail(List, int)}</li>
 *   <li><b>Filtering</b> — {@link #filter(List, Predicate)},
 *       {@link #reject(List, Predicate)}</li>
 *   <li><b>Mapping</b> — {@link #map(List, Function)}</li>
 *   <li><b>Conversion</b> — {@link #toArray(List)},
 *       {@link #fromArray(int[])}</li>
 *   <li><b>Joining</b> — {@link #join(List, String)}</li>
 *   <li><b>Printing</b> — {@link #print(List)}</li>
 * </ul>
 *
 * <h2>Aliases</h2>
 * <p>Every method has 2–4 aliases so callers can use whichever verb reads
 * most naturally. The alias patterns mirror {@link ArraysUtil}:
 * {@code sum / total / sumOf}, {@code average / avg / mean},
 * {@code max / largest / maxOf}, {@code min / smallest / minOf},
 * {@code sorted / sort / ascending / sortAscending},
 * {@code distinct / unique / dedupe},
 * {@code reverse / reversed / flipped}.</p>
 *
 * <h2>Null safety</h2>
 * <p>Every method returns a documented default for {@code null} input —
 * never throws. Aggregations return {@code 0}, transforms return an empty
 * list (mutable, not {@link List#of()}), predicates return {@code false}
 * or {@code true} depending on the question. See each method for the
 * specific behavior.</p>
 *
 * <p><b>Note on mutability:</b> transform methods return a new mutable
 * {@link ArrayList} rather than an immutable list. This is deliberate —
 * callers who want to modify the result (e.g. {@code .add(...)}) can do so
 * without wrapping. The input list is never modified.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * List<Integer> scores = List.of(85, 92, 78, 95, 88, 72);
 *
 * println("Sum    : " + Lists.sum(scores));         // 510
 * println("Avg    : " + Lists.average(scores));     // 85.0
 * println("Max    : " + Lists.max(scores));         // 95
 * println("Sorted : " + Lists.sorted(scores));      // [72, 78, 85, 88, 92, 95]
 *
 * List<Integer> adults = Lists.filter(ages, age -> age >= 18);
 * List<String>  names  = Lists.map(students, Student::getName);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For array-based equivalents, see {@link ArraysUtil}. For
 * predicate-based filtering with more advanced helpers, see
 * {@link Filter}. For search helpers (Levenshtein, fuzzy matching), see
 * {@link Search}.</p>
 *
 * @see ArraysUtil
 * @see Filter
 * @see Search
 * @since 1.0.0
 */
public final class Lists {

    private Lists() {}

    // ============================================================== aggregation

    /**
     * Returns the sum of the list, skipping any {@code null} elements.
     * {@code null} list or empty → {@code 0}.
     *
     * @param list the list to sum (may be null)
     * @return the sum, or 0
     * @see #total(List)
     * @see #sumOf(List)
     * @see #average(List)
     * @see ArraysUtil#sum(int[])
     * @since 1.0.0
     */
    public static int sum(List<Integer> list) {
        if (list == null) return 0;
        int total = 0;
        for (Integer v : list) if (v != null) total += v;
        return total;
    }

    /**
     * Alias of {@link #sum(List)} — see that method for full
     * documentation.
     *
     * @param list the list to sum
     * @return the sum
     * @see #sum(List)
     * @since 1.0.0
     */
    public static int total(List<Integer> list) { return sum(list); }

    /**
     * Alias of {@link #sum(List)} — see that method for full
     * documentation.
     *
     * @param list the list to sum
     * @return the sum
     * @see #sum(List)
     * @since 1.0.0
     */
    public static int sumOf(List<Integer> list) { return sum(list); }

    /**
     * Returns the arithmetic mean of the list, skipping null elements.
     *
     * <p>The denominator is the list's {@code size()}, not the count of
     * non-null elements — so a list of {@code [1, null, 3]} averages as
     * {@code 4 / 3 = 1.33}, not {@code 4 / 2 = 2.0}. This is consistent
     * with treating nulls as absent values occupying a slot.</p>
     *
     * @param list the list (may be null)
     * @return the mean, or 0 for null or empty
     * @see #avg(List)
     * @see #mean(List)
     * @see #sum(List)
     * @since 1.0.0
     */
    public static double average(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        return (double) sum(list) / list.size();
    }

    /**
     * Alias of {@link #average(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the mean
     * @see #average(List)
     * @since 1.0.0
     */
    public static double avg(List<Integer> list) { return average(list); }

    /**
     * Alias of {@link #average(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the mean
     * @see #average(List)
     * @since 1.0.0
     */
    public static double mean(List<Integer> list) { return average(list); }

    /**
     * Returns the maximum value, skipping null elements.
     *
     * @param list the list (may be null)
     * @return the maximum, or 0 for null, empty, or all-null
     * @see #largest(List)
     * @see #maxOf(List)
     * @see #min(List)
     * @since 1.0.0
     */
    public static int max(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        int best = Integer.MIN_VALUE;
        for (Integer v : list) if (v != null && v > best) best = v;
        return best == Integer.MIN_VALUE ? 0 : best;
    }

    /**
     * Alias of {@link #max(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the maximum
     * @see #max(List)
     * @since 1.0.0
     */
    public static int largest(List<Integer> list) { return max(list); }

    /**
     * Alias of {@link #max(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the maximum
     * @see #max(List)
     * @since 1.0.0
     */
    public static int maxOf(List<Integer> list) { return max(list); }

    /**
     * Returns the minimum value, skipping null elements.
     *
     * @param list the list (may be null)
     * @return the minimum, or 0 for null, empty, or all-null
     * @see #smallest(List)
     * @see #minOf(List)
     * @see #max(List)
     * @since 1.0.0
     */
    public static int min(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        int best = Integer.MAX_VALUE;
        for (Integer v : list) if (v != null && v < best) best = v;
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /**
     * Alias of {@link #min(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the minimum
     * @see #min(List)
     * @since 1.0.0
     */
    public static int smallest(List<Integer> list) { return min(list); }

    /**
     * Alias of {@link #min(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return the minimum
     * @see #min(List)
     * @since 1.0.0
     */
    public static int minOf(List<Integer> list) { return min(list); }

    /**
     * Counts occurrences of the target value, skipping nulls.
     *
     * @param list the list (may be null)
     * @param target the value to count (may be null)
     * @return the count, or 0
     * @see #countOf(List, Integer)
     * @see #occurrences(List, Integer)
     * @since 1.0.0
     */
    public static int count(List<Integer> list, Integer target) {
        if (list == null) return 0;
        int total = 0;
        for (Integer v : list) if (v != null && v.equals(target)) total++;
        return total;
    }

    /**
     * Alias of {@link #count(List, Integer)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param target the value to count
     * @return the count
     * @see #count(List, Integer)
     * @since 1.0.0
     */
    public static int countOf(List<Integer> list, Integer target) { return count(list, target); }

    /**
     * Alias of {@link #count(List, Integer)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param target the value to count
     * @return the count
     * @see #count(List, Integer)
     * @since 1.0.0
     */
    public static int occurrences(List<Integer> list, Integer target) { return count(list, target); }

    // ============================================================== search

    /**
     * Checks whether the list contains the target value.
     *
     * <p>Uses {@link List#contains(Object)} which relies on
     * {@link Object#equals(Object)}. A {@code null} target returns
     * {@code true} if the list itself contains a null element, matching
     * the JDK behavior.</p>
     *
     * @param list the list (may be null)
     * @param target the value to find (may be null)
     * @return {@code true} if present
     * @see #includes(List, Object)
     * @see #has(List, Object)
     * @see #indexOf(List, Object)
     * @since 1.0.0
     */
    public static boolean contains(List<?> list, Object target) {
        if (list == null) return false;
        return list.contains(target);
    }

    /**
     * Alias of {@link #contains(List, Object)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param target the value to find
     * @return {@code true} if present
     * @see #contains(List, Object)
     * @since 1.0.0
     */
    public static boolean includes(List<?> list, Object target) { return contains(list, target); }

    /**
     * Alias of {@link #contains(List, Object)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param target the value to find
     * @return {@code true} if present
     * @see #contains(List, Object)
     * @since 1.0.0
     */
    public static boolean has(List<?> list, Object target) { return contains(list, target); }

    /**
     * Returns the index of the first occurrence of the target, or
     * {@code -1}.
     *
     * @param list the list (may be null)
     * @param target the value to find
     * @param <T> the element type
     * @return the 0-based index, or -1
     * @see #lastIndexOf(List, Object)
     * @since 1.0.0
     */
    public static <T> int indexOf(List<T> list, T target) {
        if (list == null) return -1;
        return list.indexOf(target);
    }

    /**
     * Returns the index of the last occurrence of the target, or
     * {@code -1}.
     *
     * @param list the list (may be null)
     * @param target the value to find
     * @param <T> the element type
     * @return the 0-based index, or -1
     * @see #indexOf(List, Object)
     * @since 1.0.0
     */
    public static <T> int lastIndexOf(List<T> list, T target) {
        if (list == null) return -1;
        return list.lastIndexOf(target);
    }

    /**
     * Checks whether the list is {@code null} or has zero elements.
     *
     * @param list the list (may be null)
     * @param <T> the element type
     * @return {@code true} if null or empty
     * @see #notEmpty(List)
     * @since 1.0.0
     */
    public static <T> boolean isEmpty(List<T> list) { return list == null || list.isEmpty(); }

    /**
     * Alias of {@code !isEmpty(list)} — see {@link #isEmpty(List)}.
     *
     * @param list the list
     * @param <T> the element type
     * @return {@code true} if non-null and non-empty
     * @see #isEmpty(List)
     * @since 1.0.0
     */
    public static <T> boolean notEmpty(List<T> list) { return !isEmpty(list); }

    /**
     * Alias of {@link #notEmpty(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param <T> the element type
     * @return {@code true} if non-empty
     * @see #notEmpty(List)
     * @since 1.0.0
     */
    public static <T> boolean hasElements(List<T> list) { return notEmpty(list); }

    /**
     * Checks whether the list contains any duplicate values. Uses
     * {@link Object#equals(Object)} for comparison.
     *
     * @param list the list (may be null)
     * @return {@code true} if any value appears more than once
     * @see #hasRepeats(List)
     * @see #distinct(List)
     * @since 1.0.0
     */
    public static boolean hasDuplicates(List<Integer> list) {
        if (list == null) return false;
        Set<Integer> seen = new HashSet<>();
        for (Integer v : list) if (!seen.add(v)) return true;
        return false;
    }

    /**
     * Alias of {@link #hasDuplicates(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return {@code true} if any duplicate is present
     * @see #hasDuplicates(List)
     * @since 1.0.0
     */
    public static boolean hasRepeats(List<Integer> list) { return hasDuplicates(list); }

    // ============================================================== transforms

    /**
     * Returns a new list with the elements sorted in ascending order,
     * skipping nulls.
     *
     * @param list the list (may be null)
     * @return a new sorted list, or empty for null
     * @see #sort(List)
     * @see #ascending(List)
     * @see #sortAscending(List)
     * @see #sortedDescending(List)
     * @since 1.0.0
     */
    public static List<Integer> sorted(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        copy.removeIf(v -> v == null);
        Collections.sort(copy);
        return copy;
    }

    /**
     * Alias of {@link #sorted(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new sorted list
     * @see #sorted(List)
     * @since 1.0.0
     */
    public static List<Integer> sort(List<Integer> list) { return sorted(list); }

    /**
     * Alias of {@link #sorted(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new sorted list
     * @see #sorted(List)
     * @since 1.0.0
     */
    public static List<Integer> ascending(List<Integer> list) { return sorted(list); }

    /**
     * Alias of {@link #sorted(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new sorted list
     * @see #sorted(List)
     * @since 1.0.0
     */
    public static List<Integer> sortAscending(List<Integer> list) { return sorted(list); }

    /**
     * Returns a new list with the elements sorted in descending order,
     * skipping nulls.
     *
     * @param list the list (may be null)
     * @return a new descending-sorted list, or empty for null
     * @see #descending(List)
     * @see #sortDescending(List)
     * @see #sorted(List)
     * @since 1.0.0
     */
    public static List<Integer> sortedDescending(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        copy.removeIf(v -> v == null);
        copy.sort(Comparator.reverseOrder());
        return copy;
    }

    /**
     * Alias of {@link #sortedDescending(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new descending-sorted list
     * @see #sortedDescending(List)
     * @since 1.0.0
     */
    public static List<Integer> descending(List<Integer> list) { return sortedDescending(list); }

    /**
     * Alias of {@link #sortedDescending(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new descending-sorted list
     * @see #sortedDescending(List)
     * @since 1.0.0
     */
    public static List<Integer> sortDescending(List<Integer> list) { return sortedDescending(list); }

    /**
     * Returns a new list with the elements in reverse order.
     *
     * @param list the list (may be null)
     * @return a new reversed list, or empty for null
     * @see #reversed(List)
     * @see #flipped(List)
     * @since 1.0.0
     */
    public static List<Integer> reverse(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }

    /**
     * Alias of {@link #reverse(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new reversed list
     * @see #reverse(List)
     * @since 1.0.0
     */
    public static List<Integer> reversed(List<Integer> list) { return reverse(list); }

    /**
     * Alias of {@link #reverse(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new reversed list
     * @see #reverse(List)
     * @since 1.0.0
     */
    public static List<Integer> flipped(List<Integer> list) { return reverse(list); }

    /**
     * Returns a new list with duplicates removed, preserving first-seen
     * order. Works for any element type.
     *
     * <pre>{@code
     * Lists.distinct(List.of(1, 2, 1, 3, 2));         // [1, 2, 3]
     * Lists.distinct(List.of("a", "b", "a", "c"));    // [a, b, c]
     * }</pre>
     *
     * @param list the list (may be null)
     * @param <T> the element type
     * @return a new deduped list, or empty for null
     * @see #unique(List)
     * @see #dedupe(List)
     * @see #hasDuplicates(List)
     * @since 1.0.0
     */
    public static <T> List<T> distinct(List<T> list) {
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    /**
     * Alias of {@link #distinct(List)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param <T> the element type
     * @return a new deduped list
     * @see #distinct(List)
     * @since 1.0.0
     */
    public static <T> List<T> unique(List<T> list) { return distinct(list); }

    /**
     * Alias of {@link #distinct(List)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param <T> the element type
     * @return a new deduped list
     * @see #distinct(List)
     * @since 1.0.0
     */
    public static <T> List<T> dedupe(List<T> list) { return distinct(list); }

    /**
     * Returns a new list with only the first {@code n} elements.
     *
     * @param list the list (may be null)
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list, or empty if null or {@code n <= 0}
     * @see #head(List, int)
     * @see #firstN(List, int)
     * @see #limit(List, int)
     * @see #drop(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> take(List<T> list, int n) {
        if (list == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(list.subList(0, Math.min(n, list.size())));
    }

    /**
     * Alias of {@link #take(List, int)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list with the first {@code n} elements
     * @see #take(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> head(List<T> list, int n) { return take(list, n); }

    /**
     * Alias of {@link #take(List, int)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list with the first {@code n} elements
     * @see #take(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> firstN(List<T> list, int n) { return take(list, n); }

    /**
     * Alias of {@link #take(List, int)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list with the first {@code n} elements
     * @see #take(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> limit(List<T> list, int n) { return take(list, n); }

    /**
     * Returns a new list skipping the first {@code n} elements.
     *
     * @param list the list (may be null)
     * @param n the number of elements to skip
     * @param <T> the element type
     * @return a new list, or empty if null or {@code n >= size}
     * @see #skip(List, int)
     * @see #take(List, int)
     * @see #tail(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> drop(List<T> list, int n) {
        if (list == null || n >= list.size()) return new ArrayList<>();
        return new ArrayList<>(list.subList(Math.max(0, n), list.size()));
    }

    /**
     * Alias of {@link #drop(List, int)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param n the number of elements to skip
     * @param <T> the element type
     * @return a new list with the remaining elements
     * @see #drop(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> skip(List<T> list, int n) { return drop(list, n); }

    /**
     * Returns a new list with only the last {@code n} elements.
     *
     * @param list the list (may be null)
     * @param n the number of trailing elements
     * @param <T> the element type
     * @return a new list, or empty if null or {@code n <= 0}
     * @see #lastN(List, int)
     * @see #take(List, int)
     * @see #drop(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> tail(List<T> list, int n) {
        if (list == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(list.subList(Math.max(0, list.size() - n), list.size()));
    }

    /**
     * Alias of {@link #tail(List, int)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param n the number of trailing elements
     * @param <T> the element type
     * @return a new list with the last {@code n} elements
     * @see #tail(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> lastN(List<T> list, int n) { return tail(list, n); }

    // ============================================================== filter / map

    /**
     * Returns a new list containing only the elements matching the
     * predicate.
     *
     * <pre>{@code
     * Lists.filter(List.of(1, 2, 3, 4, 5), v -> v % 2 == 0);   // [2, 4]
     * }</pre>
     *
     * @param list the list (may be null)
     * @param predicate the test (may be null — returns empty)
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @see #keep(List, Predicate)
     * @see #select(List, Predicate)
     * @see #reject(List, Predicate)
     * @see Filter#where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (list == null || predicate == null) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (T item : list) if (predicate.test(item)) result.add(item);
        return result;
    }

    /**
     * Alias of {@link #filter(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> where(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }

    /**
     * Alias of {@link #filter(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> keep(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }

    /**
     * Alias of {@link #filter(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> select(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }

    /**
     * Returns a new list containing only the elements <b>not</b> matching
     * the predicate.
     *
     * @param list the list (may be null)
     * @param predicate the test to negate (may be null — returns a copy
     * of the input list)
     * @param <T> the element type
     * @return a new filtered list
     * @see #exclude(List, Predicate)
     * @see #filter(List, Predicate)
     * @see Filter#reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> reject(List<T> list, Predicate<T> predicate) {
        if (predicate == null) return list == null ? new ArrayList<>() : new ArrayList<>(list);
        return filter(list, predicate.negate());
    }

    /**
     * Alias of {@link #reject(List, Predicate)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> exclude(List<T> list, Predicate<T> predicate) { return reject(list, predicate); }

    /**
     * Applies the function to every element, returning a new list of
     * results. Works for any source/target type pair.
     *
     * <pre>{@code
     * List<String> names = Lists.map(students, Student::getName);
     * List<Integer> squares = Lists.map(List.of(1, 2, 3), n -> n * n);
     * }</pre>
     *
     * @param list the list (may be null)
     * @param mapper the function (may be null — returns empty)
     * @param <T> the source element type
     * @param <R> the target element type
     * @return a new list with the mapped values
     * @see #transform(List, Function)
     * @see #mapped(List, Function)
     * @see Filter
     * @since 1.0.0
     */
    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        if (list == null || mapper == null) return new ArrayList<>();
        List<R> result = new ArrayList<>(list.size());
        for (T item : list) result.add(mapper.apply(item));
        return result;
    }

    /**
     * Alias of {@link #map(List, Function)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param mapper the function
     * @param <T> the source element type
     * @param <R> the target element type
     * @return a new list with the mapped values
     * @see #map(List, Function)
     * @since 1.0.0
     */
    public static <T, R> List<R> transform(List<T> list, Function<T, R> mapper) { return map(list, mapper); }

    /**
     * Alias of {@link #map(List, Function)} — see that method for full
     * documentation and examples.
     *
     * @param list the list
     * @param mapper the function
     * @param <T> the source element type
     * @param <R> the target element type
     * @return a new list with the mapped values
     * @see #map(List, Function)
     * @since 1.0.0
     */
    public static <T, R> List<R> mapped(List<T> list, Function<T, R> mapper) { return map(list, mapper); }

    // ============================================================== conversion

    /**
     * Converts a {@code List<Integer>} to an {@code int[]}. Null elements
     * become {@code 0} in the resulting array.
     *
     * @param list the list (may be null)
     * @return a new array, or empty for null
     * @see #asIntArray(List)
     * @see #fromArray(int[])
     * @see ArraysUtil
     * @since 1.0.0
     */
    public static int[] toArray(List<Integer> list) {
        if (list == null) return new int[0];
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Integer v = list.get(i);
            result[i] = v == null ? 0 : v;
        }
        return result;
    }

    /**
     * Alias of {@link #toArray(List)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @return a new array
     * @see #toArray(List)
     * @since 1.0.0
     */
    public static int[] asIntArray(List<Integer> list) { return toArray(list); }

    /**
     * Converts an {@code int[]} to a new mutable {@code List<Integer>}.
     *
     * @param array the array (may be null)
     * @return a new list, or empty for null
     * @see #ofInts(int[])
     * @see #toArray(List)
     * @since 1.0.0
     */
    public static List<Integer> fromArray(int[] array) {
        List<Integer> list = new ArrayList<>();
        if (array != null) for (int v : array) list.add(v);
        return list;
    }

    /**
     * Alias of {@link #fromArray(int[])} — see that method for full
     * documentation.
     *
     * @param array the array
     * @return a new list
     * @see #fromArray(int[])
     * @since 1.0.0
     */
    public static List<Integer> ofInts(int[] array) { return fromArray(array); }

    // ============================================================== join / print

    /**
     * Joins the list's elements with a delimiter, using each element's
     * {@link Object#toString()}.
     *
     * @param list the list (may be null)
     * @param delimiter the separator (may be null — treated as empty)
     * @return the joined string, or empty for null list
     * @see #joinWith(List, String)
     * @see chirp.format.Format#list(List, String)
     * @since 1.0.0
     */
    public static String join(List<?> list, String delimiter) {
        if (list == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /**
     * Alias of {@link #join(List, String)} — see that method for full
     * documentation.
     *
     * @param list the list
     * @param delimiter the separator
     * @return the joined string
     * @see #join(List, String)
     * @since 1.0.0
     */
    public static String joinWith(List<?> list, String delimiter) { return join(list, delimiter); }

    /**
     * Prints the list to {@link System#out} via
     * {@link Console#println(Object)}. No guard, no styling — works even
     * before {@code Chirp.start()}.
     *
     * @param list the list to print (may be null — prints "null")
     * @param <T> the element type
     * @see #show(List)
     * @see Console#println(Object)
     * @since 1.0.0
     */
    public static <T> void print(List<T> list) {
        Console.println(list);
    }

    /**
     * Alias of {@link #print(List)} — see that method for full
     * documentation.
     *
     * @param list the list to print
     * @param <T> the element type
     * @see #print(List)
     * @since 1.0.0
     */
    public static <T> void show(List<T> list) { print(list); }
}
