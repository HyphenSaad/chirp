package chirp.collection;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predicate-based filtering of {@code List<T>} — null-safe and empty-safe
 * throughout.
 *
 * <p>Where {@link Lists} is the list equivalent of {@link ArraysUtil} for
 * integers, {@code Filter} is the generic predicate helper: it works for
 * {@code List<T>} of any element type and adds features that don't fit
 * the numeric aggregations — partition, grouping, flattening, merge.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Filter-returning</b> — {@link #where(List, Predicate)},
 *       {@link #reject(List, Predicate)},
 *       {@link #nonNull(List)}, {@link #distinct(List)}</li>
 *   <li><b>Boolean predicates</b> — {@link #anyMatch(List, Predicate)},
 *       {@link #allMatch(List, Predicate)},
 *       {@link #noneMatch(List, Predicate)},
 *       {@link #count(List, Predicate)}</li>
 *   <li><b>First / last / limit</b> — {@link #first(List)},
 *       {@link #last(List)}, {@link #firstMatch(List, Predicate)},
 *       {@link #limit(List, int)}, {@link #skip(List, int)}</li>
 *   <li><b>Partition</b> — {@link #partition(List, Predicate)}</li>
 *   <li><b>Grouping</b> — {@link #groupBy(List, Function)}</li>
 *   <li><b>Merge / flatten</b> — {@link #merge(List[])},
 *       {@link #flatten(List)}</li>
 * </ul>
 *
 * <h2>The name {@code all} — an intentional awkwardness</h2>
 * <p>Chirp has two methods that both read "check all elements" but do
 * different things:</p>
 * <ul>
 *   <li>{@link #all(List, Predicate)} returns a <b>filtered List</b> of
 *       elements that passed the predicate — matching the pattern of
 *       {@code where / filter / keep / select}.</li>
 *   <li>{@link #allMatch(List, Predicate)} returns a <b>boolean</b> —
 *       {@code true} if every element passes the predicate.</li>
 * </ul>
 * <p>This is deliberate. The filter-returning methods are the primary
 * use case, so they get the short names ({@code all}, {@code any},
 * {@code none}). The boolean checks get the verbose names
 * ({@code allMatch}, {@code anyMatch}, {@code noneMatch}) to match the
 * {@link java.util.stream.Stream} naming convention.</p>
 *
 * <p>If you want the "did every element pass" boolean, use
 * {@link #allMatch(List, Predicate)} — <b>not</b> {@link #all(List, Predicate)}.
 * {@code allMatch} is the boolean; {@code all} returns a list.</p>
 *
 * <h2>Null safety</h2>
 * <p>Every method returns a documented default for {@code null} input.
 * Filter-returning methods return an empty (mutable) list, boolean
 * methods return {@code false}, count methods return {@code 0},
 * first/last return {@code null} (or the provided fallback).</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);
 *
 * List<Integer> evens = Filter.where(nums, n -> n % 2 == 0);   // [2, 4, 6]
 * List<Integer> odds  = Filter.reject(nums, n -> n % 2 == 0);  // [1, 3, 5]
 *
 * boolean hasLarge  = Filter.anyMatch(nums, n -> n > 5);       // true
 * boolean allPos    = Filter.allMatch(nums, n -> n > 0);       // true
 * boolean noneSmall = Filter.noneMatch(nums, n -> n < 0);      // true
 *
 * var split = Filter.partition(nums, n -> n > 3);
 * split.matching();     // [4, 5, 6]
 * split.nonMatching();  // [1, 2, 3]
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For numeric list operations (sum, average, sort), see {@link Lists}.
 * For array-based filtering, see {@link ArraysUtil}. For text search
 * within a list of strings, see {@link Search}.</p>
 *
 * @see Lists
 * @see ArraysUtil
 * @see Search
 * @since 1.0.0
 */
public final class Filter {

    private Filter() {}

    // ============================================================== filter-returning

    /**
     * Returns a new list containing only the elements matching the
     * predicate.
     *
     * <pre>{@code
     * Filter.where(List.of(1, 2, 3, 4), n -> n % 2 == 0);   // [2, 4]
     * }</pre>
     *
     * @param items the list (may be null)
     * @param predicate the test (may be null — returns empty)
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, Predicate)
     * @see #keep(List, Predicate)
     * @see #select(List, Predicate)
     * @see #all(List, Predicate)
     * @see #apply(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> where(List<T> items, Predicate<T> predicate) {
        Trace.trace("filter", "where(items={})", items == null ? 0 : items.size());
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) return result;
        for (T item : items) if (predicate.test(item)) result.add(item);
        return result;
    }

    /**
     * Alias of {@link #where(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    /**
     * Alias of {@link #where(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> keep(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    /**
     * Alias of {@link #where(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> select(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    /**
     * Alias of {@link #where(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * <p><b>Note:</b> despite the name, this method returns a <b>List</b>,
     * not a {@code boolean}. For the "did every element pass" boolean,
     * use {@link #allMatch(List, Predicate)}. The naming choice is
     * documented at the class level.</p>
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @see #allMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> all(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    /**
     * Alias of {@link #where(List, Predicate)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a new filtered list
     * @see #where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> apply(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    /**
     * Returns a new list containing only the elements <b>not</b> matching
     * the predicate.
     *
     * <p>Implemented as {@code where(items, predicate.negate())}. A null
     * predicate returns a copy of the input (nothing is rejected).</p>
     *
     * @param items the list (may be null)
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #exclude(List, Predicate)
     * @see #not(List, Predicate)
     * @see #removeIf(List, Predicate)
     * @see #drop(List, Predicate)
     * @see #where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> reject(List<T> items, Predicate<T> predicate) {
        if (predicate == null) return items == null ? new ArrayList<>() : new ArrayList<>(items);
        return where(items, predicate.negate());
    }

    /**
     * Alias of {@link #reject(List, Predicate)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> exclude(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }

    /**
     * Alias of {@link #reject(List, Predicate)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> drop(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }

    /**
     * Alias of {@link #reject(List, Predicate)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> not(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }

    /**
     * Alias of {@link #reject(List, Predicate)} — see that method for full
     * documentation. Not to be confused with {@link java.util.Collection#removeIf(Predicate)}
     * — this returns a new list and does not modify the input.
     *
     * @param items the list
     * @param predicate the test to negate
     * @param <T> the element type
     * @return a new filtered list
     * @see #reject(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> removeIf(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }

    /**
     * Filters a list by extracting a value from each element and testing
     * the extracted value. Useful when the predicate needs a derived
     * property rather than the element itself.
     *
     * <pre>{@code
     * record Person(String name, int age) {}
     * List<Person> people = ...;
     * List<Person> adults = Filter.where(people, Person::age, age -> age >= 18);
     * }</pre>
     *
     * @param items the list (may be null)
     * @param extractor the property to extract from each element (may be
     * null — returns empty)
     * @param predicate the test on the extracted value (may be null —
     * returns empty)
     * @param <T> the element type
     * @param <R> the extracted property type
     * @return a new filtered list
     * @see #whereField(List, Function, Predicate)
     * @see #filterBy(List, Function, Predicate)
     * @since 1.0.0
     */
    public static <T, R> List<T> where(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        Trace.trace("filter", "where(extractor, predicate, items={})",
                items == null ? 0 : items.size());
        List<T> result = new ArrayList<>();
        if (items == null || extractor == null || predicate == null) return result;
        for (T item : items) if (predicate.test(extractor.apply(item))) result.add(item);
        return result;
    }

    /**
     * Alias of {@link #where(List, Function, Predicate)} — see that method
     * for full documentation and examples.
     *
     * @param items the list
     * @param extractor the property to extract
     * @param predicate the test on the extracted value
     * @param <T> the element type
     * @param <R> the extracted property type
     * @return a new filtered list
     * @see #where(List, Function, Predicate)
     * @since 1.0.0
     */
    public static <T, R> List<T> whereField(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        return where(items, extractor, predicate);
    }

    /**
     * Alias of {@link #where(List, Function, Predicate)} — see that method
     * for full documentation and examples.
     *
     * @param items the list
     * @param extractor the property to extract
     * @param predicate the test on the extracted value
     * @param <T> the element type
     * @param <R> the extracted property type
     * @return a new filtered list
     * @see #where(List, Function, Predicate)
     * @since 1.0.0
     */
    public static <T, R> List<T> filterBy(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        return where(items, extractor, predicate);
    }

    // ============================================================== set-like

    /**
     * Removes {@code null} elements from the list.
     *
     * @param items the list (may be null)
     * @param <T> the element type
     * @return a new list with no nulls
     * @see #notNull(List)
     * @see #removeNulls(List)
     * @since 1.0.0
     */
    public static <T> List<T> nonNull(List<T> items) {
        List<T> result = new ArrayList<>();
        if (items == null) return result;
        for (T item : items) if (item != null) result.add(item);
        return result;
    }

    /**
     * Alias of {@link #nonNull(List)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param <T> the element type
     * @return a new list with no nulls
     * @see #nonNull(List)
     * @since 1.0.0
     */
    public static <T> List<T> notNull(List<T> items) { return nonNull(items); }

    /**
     * Alias of {@link #nonNull(List)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param <T> the element type
     * @return a new list with no nulls
     * @see #nonNull(List)
     * @since 1.0.0
     */
    public static <T> List<T> removeNulls(List<T> items) { return nonNull(items); }

    /**
     * Returns a new list with duplicates removed, preserving first-seen
     * order. Uses {@link LinkedHashSet} internally.
     *
     * @param items the list (may be null)
     * @param <T> the element type
     * @return a new deduped list
     * @see #unique(List)
     * @see #dedupe(List)
     * @since 1.0.0
     */
    public static <T> List<T> distinct(List<T> items) {
        List<T> result = new ArrayList<>();
        if (items == null) return result;
        Set<T> seen = new LinkedHashSet<>();
        for (T item : items) if (seen.add(item)) result.add(item);
        return result;
    }

    /**
     * Alias of {@link #distinct(List)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param <T> the element type
     * @return a new deduped list
     * @see #distinct(List)
     * @since 1.0.0
     */
    public static <T> List<T> unique(List<T> items) { return distinct(items); }

    /**
     * Alias of {@link #distinct(List)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param <T> the element type
     * @return a new deduped list
     * @see #distinct(List)
     * @since 1.0.0
     */
    public static <T> List<T> dedupe(List<T> items) { return distinct(items); }

    // ============================================================== boolean predicates

    /**
     * Checks whether at least one element matches the predicate.
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if any element matches
     * @see #any(List, Predicate)
     * @see #allMatch(List, Predicate)
     * @see #noneMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean anyMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return false;
        for (T item : items) if (predicate.test(item)) return true;
        return false;
    }

    /**
     * Alias of {@link #anyMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if any element matches
     * @see #anyMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean any(List<T> items, Predicate<T> predicate) { return anyMatch(items, predicate); }

    /**
     * Checks whether every element matches the predicate.
     *
     * <p>Returns {@code false} for a {@code null} list or {@code null}
     * predicate — the "unknown" case is treated as "not satisfied" rather
     * than "vacuously satisfied".</p>
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if all elements match
     * @see #everyMatch(List, Predicate)
     * @see #allSatisfy(List, Predicate)
     * @see #anyMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean allMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return false;
        for (T item : items) if (!predicate.test(item)) return false;
        return true;
    }

    /**
     * Alias of {@link #allMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if all elements match
     * @see #allMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean everyMatch(List<T> items, Predicate<T> predicate) {
        return allMatch(items, predicate);
    }

    /**
     * Alias of {@link #allMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if all elements match
     * @see #allMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean allSatisfy(List<T> items, Predicate<T> predicate) {
        return allMatch(items, predicate);
    }

    /**
     * Checks whether no element matches the predicate.
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if no element matches
     * @see #none(List, Predicate)
     * @see #anyMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean noneMatch(List<T> items, Predicate<T> predicate) {
        return !anyMatch(items, predicate);
    }

    /**
     * Alias of {@link #noneMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return {@code true} if no element matches
     * @see #noneMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> boolean none(List<T> items, Predicate<T> predicate) { return noneMatch(items, predicate); }

    /**
     * Returns the number of elements matching the predicate.
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return the count, or 0
     * @see #countWhere(List, Predicate)
     * @since 1.0.0
     */
    public static <T> long count(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return 0;
        long n = 0;
        for (T item : items) if (predicate.test(item)) n++;
        return n;
    }

    /**
     * Alias of {@link #count(List, Predicate)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return the count
     * @see #count(List, Predicate)
     * @since 1.0.0
     */
    public static <T> long countWhere(List<T> items, Predicate<T> predicate) { return count(items, predicate); }

    // ============================================================== first / last / limit

    /**
     * Returns the first element, or {@code null} if the list is null or
     * empty.
     *
     * @param items the list (may be null)
     * @param <T> the element type
     * @return the first element, or null
     * @see #firstOr(List, Object)
     * @see #last(List)
     * @see #firstMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> T first(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(0);
    }

    /**
     * Returns the first element, or {@code fallback} if the list is null
     * or empty.
     *
     * @param items the list (may be null)
     * @param fallback the value to return on empty
     * @param <T> the element type
     * @return the first element or the fallback
     * @see #first(List)
     * @since 1.0.0
     */
    public static <T> T firstOr(List<T> items, T fallback) {
        if (items == null || items.isEmpty()) return fallback;
        return items.get(0);
    }

    /**
     * Returns the last element, or {@code null} if the list is null or
     * empty.
     *
     * @param items the list (may be null)
     * @param <T> the element type
     * @return the last element, or null
     * @see #lastOr(List, Object)
     * @see #first(List)
     * @since 1.0.0
     */
    public static <T> T last(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(items.size() - 1);
    }

    /**
     * Returns the last element, or {@code fallback} if the list is null
     * or empty.
     *
     * @param items the list (may be null)
     * @param fallback the value to return on empty
     * @param <T> the element type
     * @return the last element or the fallback
     * @see #last(List)
     * @since 1.0.0
     */
    public static <T> T lastOr(List<T> items, T fallback) {
        if (items == null || items.isEmpty()) return fallback;
        return items.get(items.size() - 1);
    }

    /**
     * Returns the first element matching the predicate, or {@code null}
     * if no element matches.
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return the first matching element, or null
     * @see #find(List, Predicate)
     * @see #firstWhere(List, Predicate)
     * @see #first(List)
     * @since 1.0.0
     */
    public static <T> T firstMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return null;
        for (T item : items) if (predicate.test(item)) return item;
        return null;
    }

    /**
     * Alias of {@link #firstMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return the first matching element, or null
     * @see #firstMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> T find(List<T> items, Predicate<T> predicate) { return firstMatch(items, predicate); }

    /**
     * Alias of {@link #firstMatch(List, Predicate)} — see that method for
     * full documentation.
     *
     * @param items the list
     * @param p the test
     * @param <T> the element type
     * @return the first matching element, or null
     * @see #firstMatch(List, Predicate)
     * @since 1.0.0
     */
    public static <T> T firstWhere(List<T> items, Predicate<T> p) { return firstMatch(items, p); }

    /**
     * Returns a new list with only the first {@code n} elements.
     *
     * @param items the list (may be null)
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list, or empty if null or {@code n <= 0}
     * @see #take(List, int)
     * @see #firstN(List, int)
     * @see #skip(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> limit(List<T> items, int n) {
        if (items == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(items.subList(0, Math.min(n, items.size())));
    }

    /**
     * Alias of {@link #limit(List, int)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list with the first {@code n} elements
     * @see #limit(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> take(List<T> items, int n) { return limit(items, n); }

    /**
     * Alias of {@link #limit(List, int)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param n the number of leading elements
     * @param <T> the element type
     * @return a new list with the first {@code n} elements
     * @see #limit(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> firstN(List<T> items, int n) { return limit(items, n); }

    /**
     * Returns a new list skipping the first {@code n} elements.
     *
     * @param items the list (may be null)
     * @param n the number of elements to skip
     * @param <T> the element type
     * @return a new list, or empty if null or {@code n >= size}
     * @see #dropN(List, int)
     * @see #limit(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> skip(List<T> items, int n) {
        if (items == null || n >= items.size()) return new ArrayList<>();
        return new ArrayList<>(items.subList(Math.max(0, n), items.size()));
    }

    /**
     * Alias of {@link #skip(List, int)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param n the number of elements to skip
     * @param <T> the element type
     * @return a new list with the remaining elements
     * @see #skip(List, int)
     * @since 1.0.0
     */
    public static <T> List<T> dropN(List<T> items, int n) { return skip(items, n); }

    // ============================================================== split / partition

    /**
     * Splits the list into two lists: matching and non-matching elements.
     *
     * <pre>{@code
     * var split = Filter.partition(List.of(1, 2, 3, 4, 5), n -> n % 2 == 0);
     * split.matching();     // [2, 4]
     * split.nonMatching();  // [1, 3, 5]
     * }</pre>
     *
     * @param items the list (may be null)
     * @param predicate the test
     * @param <T> the element type
     * @return a {@link Split} holding both lists
     * @see Split
     * @see #split(List, Predicate)
     * @see ArraysUtil#partition(int[], java.util.function.IntPredicate)
     * @since 1.0.0
     */
    public static <T> Split<T> partition(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) {
            return new Split<>(new ArrayList<>(), new ArrayList<>());
        }
        List<T> matching = new ArrayList<>();
        List<T> nonMatching = new ArrayList<>();
        for (T item : items) {
            if (predicate.test(item)) matching.add(item);
            else nonMatching.add(item);
        }
        return new Split<>(matching, nonMatching);
    }

    /**
     * Alias of {@link #partition(List, Predicate)} — see that method for
     * full documentation and examples.
     *
     * @param items the list
     * @param predicate the test
     * @param <T> the element type
     * @return a {@link Split} holding both lists
     * @see #partition(List, Predicate)
     * @since 1.0.0
     */
    public static <T> Split<T> split(List<T> items, Predicate<T> predicate) { return partition(items, predicate); }

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
     * @see #partition(List, Predicate)
     * @see ArraysUtil.SplitResult
     * @since 1.0.0
     */
    public record Split<T>(List<T> matching, List<T> nonMatching) {
        public Split {
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

    // ============================================================== grouping

    /**
     * Groups elements by a key extracted from each one. The result is a
     * {@link LinkedHashMap} preserving first-seen key order.
     *
     * <pre>{@code
     * record Person(String name, String city) {}
     * List<Person> people = ...;
     * Map<String, List<Person>> byCity = Filter.groupBy(people, Person::city);
     * }</pre>
     *
     * @param items the list (may be null)
     * @param keyFn the key extractor (may be null — returns empty map)
     * @param <T> the element type
     * @param <K> the key type
     * @return a new map from key to list of elements
     * @see #group(List, Function)
     * @see #groupByKey(List, Function)
     * @since 1.0.0
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> items, Function<T, K> keyFn) {
        if (items == null || keyFn == null) return new LinkedHashMap<>();
        Map<K, List<T>> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFn.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    /**
     * Alias of {@link #groupBy(List, Function)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param keyFn the key extractor
     * @param <T> the element type
     * @param <K> the key type
     * @return a new grouped map
     * @see #groupBy(List, Function)
     * @since 1.0.0
     */
    public static <T, K> Map<K, List<T>> group(List<T> items, Function<T, K> keyFn) { return groupBy(items, keyFn); }

    /**
     * Alias of {@link #groupBy(List, Function)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param keyFn the key extractor
     * @param <T> the element type
     * @param <K> the key type
     * @return a new grouped map
     * @see #groupBy(List, Function)
     * @since 1.0.0
     */
    public static <T, K> Map<K, List<T>> groupByKey(List<T> items, Function<T, K> keyFn) {
        return groupBy(items, keyFn);
    }

    // ============================================================== merge / flatten

    /**
     * Concatenates several lists into one, skipping null lists.
     *
     * <p>Uses {@code @SafeVarargs} so the compiler does not warn about
     * creating a generic varargs array at the call site.</p>
     *
     * @param lists the lists to merge (may be null)
     * @param <T> the element type
     * @return a new list with all elements in order
     * @see #concat(List[])
     * @see #flatten(List)
     * @since 1.0.0
     */
    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        List<T> result = new ArrayList<>();
        if (lists != null) {
            for (List<T> list : lists) if (list != null) result.addAll(list);
        }
        return result;
    }

    /**
     * Alias of {@link #merge(List[])} — see that method for full
     * documentation. Forwards the varargs array, which is safe because
     * {@code merge} only reads it.
     *
     * @param lists the lists to merge
     * @param <T> the element type
     * @return a new list with all elements in order
     * @see #merge(List[])
     * @since 1.0.0
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> concat(List<T>... lists) { return merge(lists); }

    /**
     * Flattens a list of lists into a single list. Skips null inner lists.
     *
     * <pre>{@code
     * List<List<Integer>> nested = List.of(
     *     List.of(1, 2),
     *     List.of(3, 4, 5));
     * Filter.flatten(nested);   // [1, 2, 3, 4, 5]
     * }</pre>
     *
     * @param lists the list of lists (may be null)
     * @param <T> the element type
     * @return a new flat list
     * @see #flat(List)
     * @see #merge(List[])
     * @since 1.0.0
     */
    public static <T> List<T> flatten(List<List<T>> lists) {
        List<T> result = new ArrayList<>();
        if (lists == null) return result;
        for (List<T> inner : lists) if (inner != null) result.addAll(inner);
        return result;
    }

    /**
     * Alias of {@link #flatten(List)} — see that method for full
     * documentation and examples.
     *
     * @param lists the list of lists
     * @param <T> the element type
     * @return a new flat list
     * @see #flatten(List)
     * @since 1.0.0
     */
    public static <T> List<T> flat(List<List<T>> lists) { return flatten(lists); }
}
