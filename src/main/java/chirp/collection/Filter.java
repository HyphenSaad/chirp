package chirp.collection;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predicate-based filtering of lists — null-safe and empty-safe.
 *
 * <h2>Naming</h2>
 * <p>Filter-returning: {@code where / filter / keep / select / all / apply};
 * {@code reject / exclude / drop / not / removeIf}.</p>
 * <p>Boolean: {@code anyMatch / allMatch / noneMatch}; {@code count / countWhere}.</p>
 *
 * <p><b>Note:</b> the boolean "all match" check is named
 * {@link #allMatch} — it is not {@code all}. The name {@code all} returns a
 * filtered {@code List}, matching the pattern of {@code where}, {@code filter},
 * {@code keep}, {@code select}.</p>
 */
public final class Filter {

    private Filter() {}

    // ============================================================== filter-returning

    public static <T> List<T> where(List<T> items, Predicate<T> predicate) {
        Trace.trace("filter", "where(items={})", items == null ? 0 : items.size());
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) return result;
        for (T item : items) if (predicate.test(item)) result.add(item);
        return result;
    }
    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) { return where(items, predicate); }
    public static <T> List<T> keep(List<T> items, Predicate<T> predicate) { return where(items, predicate); }
    public static <T> List<T> select(List<T> items, Predicate<T> predicate) { return where(items, predicate); }
    public static <T> List<T> all(List<T> items, Predicate<T> predicate) { return where(items, predicate); }
    public static <T> List<T> apply(List<T> items, Predicate<T> predicate) { return where(items, predicate); }

    public static <T> List<T> reject(List<T> items, Predicate<T> predicate) {
        if (predicate == null) return items == null ? new ArrayList<>() : new ArrayList<>(items);
        return where(items, predicate.negate());
    }
    public static <T> List<T> exclude(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }
    public static <T> List<T> drop(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }
    public static <T> List<T> not(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }
    public static <T> List<T> removeIf(List<T> items, Predicate<T> predicate) { return reject(items, predicate); }

    public static <T, R> List<T> where(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        Trace.trace("filter", "where(extractor, predicate, items={})",
                items == null ? 0 : items.size());
        List<T> result = new ArrayList<>();
        if (items == null || extractor == null || predicate == null) return result;
        for (T item : items) if (predicate.test(extractor.apply(item))) result.add(item);
        return result;
    }
    public static <T, R> List<T> whereField(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        return where(items, extractor, predicate);
    }
    public static <T, R> List<T> filterBy(List<T> items, Function<T, R> extractor, Predicate<R> predicate) {
        return where(items, extractor, predicate);
    }

    // ============================================================== set-like

    public static <T> List<T> nonNull(List<T> items) {
        List<T> result = new ArrayList<>();
        if (items == null) return result;
        for (T item : items) if (item != null) result.add(item);
        return result;
    }
    public static <T> List<T> notNull(List<T> items) { return nonNull(items); }
    public static <T> List<T> removeNulls(List<T> items) { return nonNull(items); }

    public static <T> List<T> distinct(List<T> items) {
        List<T> result = new ArrayList<>();
        if (items == null) return result;
        Set<T> seen = new LinkedHashSet<>();
        for (T item : items) if (seen.add(item)) result.add(item);
        return result;
    }
    public static <T> List<T> unique(List<T> items) { return distinct(items); }
    public static <T> List<T> dedupe(List<T> items) { return distinct(items); }

    // ============================================================== boolean predicates

    public static <T> boolean anyMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return false;
        for (T item : items) if (predicate.test(item)) return true;
        return false;
    }
    public static <T> boolean any(List<T> items, Predicate<T> predicate) { return anyMatch(items, predicate); }

    public static <T> boolean allMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return false;
        for (T item : items) if (!predicate.test(item)) return false;
        return true;
    }
    public static <T> boolean everyMatch(List<T> items, Predicate<T> predicate) { return allMatch(items, predicate); }
    public static <T> boolean allSatisfy(List<T> items, Predicate<T> predicate) { return allMatch(items, predicate); }

    public static <T> boolean noneMatch(List<T> items, Predicate<T> predicate) {
        return !anyMatch(items, predicate);
    }
    public static <T> boolean none(List<T> items, Predicate<T> predicate) { return noneMatch(items, predicate); }

    public static <T> long count(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return 0;
        long n = 0;
        for (T item : items) if (predicate.test(item)) n++;
        return n;
    }
    public static <T> long countWhere(List<T> items, Predicate<T> predicate) { return count(items, predicate); }

    // ============================================================== first / last / limit

    public static <T> T first(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(0);
    }
    public static <T> T firstOr(List<T> items, T fallback) {
        if (items == null || items.isEmpty()) return fallback;
        return items.get(0);
    }

    public static <T> T last(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(items.size() - 1);
    }
    public static <T> T lastOr(List<T> items, T fallback) {
        if (items == null || items.isEmpty()) return fallback;
        return items.get(items.size() - 1);
    }

    public static <T> T firstMatch(List<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) return null;
        for (T item : items) if (predicate.test(item)) return item;
        return null;
    }
    public static <T> T find(List<T> items, Predicate<T> predicate) { return firstMatch(items, predicate); }
    public static <T> T firstWhere(List<T> items, Predicate<T> p) { return firstMatch(items, p); }

    public static <T> List<T> limit(List<T> items, int n) {
        if (items == null || n <= 0) return new ArrayList<>();
        return new ArrayList<>(items.subList(0, Math.min(n, items.size())));
    }
    public static <T> List<T> take(List<T> items, int n) { return limit(items, n); }
    public static <T> List<T> firstN(List<T> items, int n) { return limit(items, n); }

    public static <T> List<T> skip(List<T> items, int n) {
        if (items == null || n >= items.size()) return new ArrayList<>();
        return new ArrayList<>(items.subList(Math.max(0, n), items.size()));
    }
    public static <T> List<T> dropN(List<T> items, int n) { return skip(items, n); }

    // ============================================================== split / partition

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
    public static <T> Split<T> split(List<T> items, Predicate<T> predicate) { return partition(items, predicate); }

    public record Split<T>(List<T> matching, List<T> nonMatching) {
        public List<T> matchingItems() { return matching; }
        public List<T> nonMatchingItems() { return nonMatching; }
    }

    // ============================================================== grouping

    public static <T, K> Map<K, List<T>> groupBy(List<T> items, Function<T, K> keyFn) {
        if (items == null || keyFn == null) return new LinkedHashMap<>();
        Map<K, List<T>> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFn.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }
    public static <T, K> Map<K, List<T>> group(List<T> items, Function<T, K> keyFn) { return groupBy(items, keyFn); }
    public static <T, K> Map<K, List<T>> groupByKey(List<T> items, Function<T, K> keyFn) {
        return groupBy(items, keyFn);
    }

    // ============================================================== merge / flatten

    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        List<T> result = new ArrayList<>();
        if (lists != null) {
            for (List<T> list : lists) if (list != null) result.addAll(list);
        }
        return result;
    }
    /** Alias of {@link #merge}; forwarding the array is safe because merge only reads it. */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> concat(List<T>... lists) { return merge(lists); }

    public static <T> List<T> flatten(List<List<T>> lists) {
        List<T> result = new ArrayList<>();
        if (lists == null) return result;
        for (List<T> inner : lists) if (inner != null) result.addAll(inner);
        return result;
    }
    public static <T> List<T> flat(List<List<T>> lists) { return flatten(lists); }
}
