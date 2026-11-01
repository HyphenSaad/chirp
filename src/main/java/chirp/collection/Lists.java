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
 * generic) — null-safe and empty-safe.
 *
 * <h2>Naming</h2>
 * <p>Aliases parallel {@link ArraysUtil}: {@code sum / total / sumOf};
 * {@code average / avg / mean}; {@code max / largest / maxOf};
 * {@code min / smallest / minOf}; {@code sorted / sortAscending};
 * {@code sortedDescending / sortDescending}; {@code distinct / unique / dedupe};
 * {@code reverse / reversed / flipped}.</p>
 */
public final class Lists {

    private Lists() {}

    // ============================================================== aggregation

    public static int sum(List<Integer> list) {
        if (list == null) return 0;
        int total = 0;
        for (Integer v : list) if (v != null) total += v;
        return total;
    }
    public static int total(List<Integer> list) { return sum(list); }
    public static int sumOf(List<Integer> list) { return sum(list); }

    public static double average(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        return (double) sum(list) / list.size();
    }
    public static double avg(List<Integer> list) { return average(list); }
    public static double mean(List<Integer> list) { return average(list); }

    public static int max(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        int best = Integer.MIN_VALUE;
        for (Integer v : list) if (v != null && v > best) best = v;
        return best == Integer.MIN_VALUE ? 0 : best;
    }
    public static int largest(List<Integer> list) { return max(list); }
    public static int maxOf(List<Integer> list) { return max(list); }

    public static int min(List<Integer> list) {
        if (list == null || list.isEmpty()) return 0;
        int best = Integer.MAX_VALUE;
        for (Integer v : list) if (v != null && v < best) best = v;
        return best == Integer.MAX_VALUE ? 0 : best;
    }
    public static int smallest(List<Integer> list) { return min(list); }
    public static int minOf(List<Integer> list) { return min(list); }

    public static int count(List<Integer> list, Integer target) {
        if (list == null) return 0;
        int total = 0;
        for (Integer v : list) if (v != null && v.equals(target)) total++;
        return total;
    }
    public static int countOf(List<Integer> list, Integer target) { return count(list, target); }
    public static int occurrences(List<Integer> list, Integer target) { return count(list, target); }

    // ============================================================== search

    public static boolean contains(List<?> list, Object target) {
        if (list == null) return false;
        return list.contains(target);
    }
    public static boolean includes(List<?> list, Object target) { return contains(list, target); }
    public static boolean has(List<?> list, Object target) { return contains(list, target); }

    public static <T> int indexOf(List<T> list, T target) {
        if (list == null) return -1;
        return list.indexOf(target);
    }
    public static <T> int lastIndexOf(List<T> list, T target) {
        if (list == null) return -1;
        return list.lastIndexOf(target);
    }

    public static <T> boolean isEmpty(List<T> list) { return list == null || list.isEmpty(); }
    public static <T> boolean notEmpty(List<T> list) { return !isEmpty(list); }
    public static <T> boolean hasElements(List<T> list) { return notEmpty(list); }

    public static boolean hasDuplicates(List<Integer> list) {
        if (list == null) return false;
        Set<Integer> seen = new HashSet<>();
        for (Integer v : list) if (!seen.add(v)) return true;
        return false;
    }
    public static boolean hasRepeats(List<Integer> list) { return hasDuplicates(list); }

    // ============================================================== transforms

    public static List<Integer> sorted(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        copy.removeIf(v -> v == null);
        Collections.sort(copy);
        return copy;
    }
    public static List<Integer> sort(List<Integer> list) { return sorted(list); }
    public static List<Integer> ascending(List<Integer> list) { return sorted(list); }
    public static List<Integer> sortAscending(List<Integer> list) { return sorted(list); }

    public static List<Integer> sortedDescending(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        copy.removeIf(v -> v == null);
        copy.sort(Comparator.reverseOrder());
        return copy;
    }
    public static List<Integer> descending(List<Integer> list) { return sortedDescending(list); }
    public static List<Integer> sortDescending(List<Integer> list) { return sortedDescending(list); }

    public static List<Integer> reverse(List<Integer> list) {
        if (list == null) return new ArrayList<>();
        List<Integer> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }
    public static List<Integer> reversed(List<Integer> list) { return reverse(list); }
    public static List<Integer> flipped(List<Integer> list) { return reverse(list); }

    public static <T> List<T> distinct(List<T> list) {
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
    public static <T> List<T> unique(List<T> list) { return distinct(list); }
    public static <T> List<T> dedupe(List<T> list) { return distinct(list); }

    public static <T> List<T> take(List<T> list, int n) {
        if (list == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(list.subList(0, Math.min(n, list.size())));
    }
    public static <T> List<T> head(List<T> list, int n) { return take(list, n); }
    public static <T> List<T> firstN(List<T> list, int n) { return take(list, n); }
    public static <T> List<T> limit(List<T> list, int n) { return take(list, n); }

    public static <T> List<T> drop(List<T> list, int n) {
        if (list == null || n >= list.size()) return new ArrayList<>();
        return new ArrayList<>(list.subList(Math.max(0, n), list.size()));
    }
    public static <T> List<T> skip(List<T> list, int n) { return drop(list, n); }
    public static <T> List<T> tail(List<T> list, int n) {
        if (list == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(list.subList(Math.max(0, list.size() - n), list.size()));
    }
    public static <T> List<T> lastN(List<T> list, int n) { return tail(list, n); }

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (list == null || predicate == null) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (T item : list) if (predicate.test(item)) result.add(item);
        return result;
    }
    public static <T> List<T> where(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }
    public static <T> List<T> keep(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }
    public static <T> List<T> select(List<T> list, Predicate<T> predicate) { return filter(list, predicate); }

    public static <T> List<T> reject(List<T> list, Predicate<T> predicate) {
        if (predicate == null) return list == null ? new ArrayList<>() : new ArrayList<>(list);
        return filter(list, predicate.negate());
    }
    public static <T> List<T> exclude(List<T> list, Predicate<T> predicate) { return reject(list, predicate); }

    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        if (list == null || mapper == null) return new ArrayList<>();
        List<R> result = new ArrayList<>(list.size());
        for (T item : list) result.add(mapper.apply(item));
        return result;
    }
    public static <T, R> List<R> transform(List<T> list, Function<T, R> mapper) { return map(list, mapper); }
    public static <T, R> List<R> mapped(List<T> list, Function<T, R> mapper) { return map(list, mapper); }

    // ============================================================== conversion

    public static int[] toArray(List<Integer> list) {
        if (list == null) return new int[0];
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Integer v = list.get(i);
            result[i] = v == null ? 0 : v;
        }
        return result;
    }
    public static int[] asIntArray(List<Integer> list) { return toArray(list); }

    public static List<Integer> fromArray(int[] array) {
        List<Integer> list = new ArrayList<>();
        if (array != null) for (int v : array) list.add(v);
        return list;
    }
    public static List<Integer> ofInts(int[] array) { return fromArray(array); }

    // ============================================================== join

    public static String join(List<?> list, String delimiter) {
        if (list == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(list.get(i));
        }
        return sb.toString();
    }
    public static String joinWith(List<?> list, String delimiter) { return join(list, delimiter); }

    public static <T> void print(List<T> list) {
        Console.println(list);
    }
    public static <T> void show(List<T> list) { print(list); }
}
