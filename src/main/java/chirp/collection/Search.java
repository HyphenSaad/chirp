package chirp.collection;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Text searching — strict, loose, whole-word, regex, and fuzzy.
 *
 * <h2>Naming</h2>
 * <p>{@code exact / equals / sameAs}; {@code contains / includes / has};
 * {@code containsIgnoreCase / looseContains}; {@code containsStrict / exactContains};
 * {@code startsWith / beginsWith / prefix}; {@code endsWith / suffix};
 * {@code matches / regex / matchesRegex}; {@code wholeWord / exactWord / wordMatch};
 * {@code fuzzy / fuzzyMatch / similarTo / approx}.</p>
 *
 * <h2>Key-value</h2>
 * <p>{@link Key} enum drives the composite checks: {@code
 * Search.matches(text, query, Key.CASE_SENSITIVE, false, Key.WHOLE_WORD, true)}.</p>
 */
public final class Search {

    /** Composite-check keys. */
    public enum Key { CASE_SENSITIVE, WHOLE_WORD, MAX_DISTANCE }

    private Search() {}

    // ============================================================== equality

    public static boolean exact(String text, String query) {
        if (text == null || query == null) return false;
        return text.equals(query);
    }
    public static boolean equals(String text, String query) { return exact(text, query); }
    public static boolean sameAs(String text, String query) { return exact(text, query); }

    public static boolean exactIgnoreCase(String text, String query) {
        if (text == null || query == null) return false;
        return text.equalsIgnoreCase(query);
    }
    public static boolean equalsIgnoreCase(String text, String query) { return exactIgnoreCase(text, query); }

    // ============================================================== contains

    public static boolean contains(String text, String query) {
        if (text == null || query == null) return false;
        return text.toLowerCase().contains(query.toLowerCase());
    }
    public static boolean containsIgnoreCase(String text, String query) { return contains(text, query); }
    public static boolean looseContains(String text, String query) { return contains(text, query); }
    public static boolean includes(String text, String query) { return contains(text, query); }
    public static boolean has(String text, String query) { return contains(text, query); }

    public static boolean containsStrict(String text, String query) {
        if (text == null || query == null) return false;
        return text.contains(query);
    }
    public static boolean exactContains(String text, String query) { return containsStrict(text, query); }

    public static boolean containsAny(String text, String... queries) {
        if (text == null || queries == null) return false;
        for (String q : queries) if (contains(text, q)) return true;
        return false;
    }
    public static boolean containsAll(String text, String... queries) {
        if (text == null || queries == null) return false;
        for (String q : queries) if (!contains(text, q)) return false;
        return true;
    }

    // ============================================================== prefix / suffix

    public static boolean startsWith(String text, String prefix) {
        if (text == null || prefix == null) return false;
        return text.toLowerCase().startsWith(prefix.toLowerCase());
    }
    public static boolean beginsWith(String text, String prefix) { return startsWith(text, prefix); }
    public static boolean prefix(String text, String prefix) { return startsWith(text, prefix); }

    public static boolean startsWithStrict(String text, String prefix) {
        if (text == null || prefix == null) return false;
        return text.startsWith(prefix);
    }

    public static boolean endsWith(String text, String suffix) {
        if (text == null || suffix == null) return false;
        return text.toLowerCase().endsWith(suffix.toLowerCase());
    }
    public static boolean suffix(String text, String suffix) { return endsWith(text, suffix); }

    public static boolean endsWithStrict(String text, String suffix) {
        if (text == null || suffix == null) return false;
        return text.endsWith(suffix);
    }

    // ============================================================== regex

    public static boolean matches(String text, String regex) {
        if (text == null || regex == null) return false;
        return Pattern.compile(regex).matcher(text).matches();
    }
    public static boolean regex(String text, String regex) { return matches(text, regex); }
    public static boolean matchesRegex(String text, String regex) { return matches(text, regex); }
    public static boolean pattern(String text, String regex) { return matches(text, regex); }

    public static boolean find(String text, String regex) {
        if (text == null || regex == null) return false;
        return Pattern.compile(regex).matcher(text).find();
    }
    public static boolean containsPattern(String text, String regex) { return find(text, regex); }

    // ============================================================== whole word

    public static boolean wholeWord(String text, String word) {
        return wholeWord(text, word, false);
    }
    public static boolean exactWord(String text, String word) { return wholeWord(text, word); }
    public static boolean wordMatch(String text, String word) { return wholeWord(text, word); }

    public static boolean wholeWord(String text, String word, boolean caseSensitive) {
        if (text == null || word == null || word.isEmpty()) {
            Trace.trace("search", "wholeWord: null/empty input");
            return false;
        }
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            String pattern = "(?<!\\w)" + Pattern.quote(word) + "(?!\\w)";
            return Pattern.compile(pattern, flags).matcher(text).find();
        } catch (Exception e) {
            Trace.warn("search", "wholeWord failed: " + e.getMessage());
            return false;
        }
    }

    // ============================================================== fuzzy

    public static boolean fuzzy(String text, String query, int maxDistance) {
        if (text == null || query == null || maxDistance < 0) return false;
        return levenshtein(text.toLowerCase(), query.toLowerCase()) <= maxDistance;
    }
    public static boolean fuzzyMatch(String text, String query, int maxDistance) {
        return fuzzy(text, query, maxDistance);
    }
    public static boolean similarTo(String text, String query, int maxDistance) {
        return fuzzy(text, query, maxDistance);
    }
    public static boolean approx(String text, String query, int maxDistance) { return fuzzy(text, query, maxDistance); }

    public static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int m = a.length(), n = b.length();
        if (m == 0) return n;
        if (n == 0) return m;

        Trace.trace("search", "levenshtein(m={}, n={})", m, n);

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev;
            prev = curr;
            curr = t;
        }
        return prev[n];
    }
    public static int editDistance(String a, String b) { return levenshtein(a, b); }
    public static int distance(String a, String b) { return levenshtein(a, b); }

    // ============================================================== composite (key-value)

    /**
     * Composite matcher — combines strictness, whole-word, and fuzzy checks.
     *
     * <p>Rules: if {@code WHOLE_WORD} is true, only whole-word matching is
     * applied (case-sensitivity comes from {@code CASE_SENSITIVE}). Otherwise,
     * if {@code MAX_DISTANCE} &gt; 0, fuzzy matching is used. Otherwise, plain
     * substring matching with case-sensitivity from {@code CASE_SENSITIVE}.</p>
     */
    public static boolean matcher(String text, String query, Object... keyValues) {
        if (text == null || query == null) return false;

        boolean caseSensitive = false;
        boolean wholeWord = false;
        int maxDistance = 0;

        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case CASE_SENSITIVE -> {
                        if (v instanceof Boolean b) caseSensitive = b;
                    }
                    case WHOLE_WORD -> {
                        if (v instanceof Boolean b) wholeWord = b;
                    }
                    case MAX_DISTANCE -> {
                        if (v instanceof Integer n && n >= 0) maxDistance = n;
                    }
                }
            }
        }

        if (wholeWord) return wholeWord(text, query, caseSensitive);
        if (maxDistance > 0) return fuzzy(text, query, maxDistance);
        if (caseSensitive) return containsStrict(text, query);
        return contains(text, query);
    }

    public static boolean match(String text, String query, Object... keyValues) {
        return matcher(text, query, keyValues);
    }

    // ============================================================== bulk

    public static boolean anyContains(List<String> items, String query) {
        if (items == null) return false;
        for (String item : items) if (contains(item, query)) return true;
        return false;
    }

    public static <T> List<T> filter(List<T> items, String query) {
        Trace.trace("search", "filter(items={}, query=\"{}\")",
                items == null ? 0 : items.size(), query);
        List<T> result = new ArrayList<>();
        if (items == null || query == null) return result;
        for (T item : items) {
            if (item != null && contains(String.valueOf(item), query)) result.add(item);
        }
        Trace.debug("search", "filter matched {} items", result.size());
        return result;
    }
    public static <T> List<T> search(List<T> items, String query) { return filter(items, query); }

    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) return result;
        for (T item : items) if (predicate.test(item)) result.add(item);
        return result;
    }
}
