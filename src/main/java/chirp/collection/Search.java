package chirp.collection;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Text searching — strict, loose, whole-word, regex, and fuzzy.
 *
 * <p>Every method accepts {@code null} inputs and returns {@code false}
 * rather than throwing, so callers can search defensively without
 * pre-checks. All comparisons use {@link java.util.Locale#ROOT} for
 * case-insensitivity, so behaviour is identical on a Turkish-locale
 * machine where the default locale would produce the dotless {@code ı}.</p>
 *
 * <h2>Two meanings of "contains"</h2>
 * <p>This is the one naming trap in Chirp's search API, and it exists
 * because natural English is ambiguous:</p>
 * <ul>
 *   <li>{@link #contains(String, String)} — <b>case-insensitive</b>
 *       substring search. {@code contains("Hello", "ELL")} is
 *       {@code true}. This is the default because most real-world searches
 *       want to ignore case.</li>
 *   <li>{@link #containsStrict(String, String)} — <b>case-sensitive</b>
 *       substring search. {@code containsStrict("Hello", "ELL")} is
 *       {@code false}. Use this when case actually matters.</li>
 * </ul>
 * <p>The rule matches the broader design: the short name
 * ({@code contains}) is the friendly default; the explicit name
 * ({@code containsStrict}) is when you want exactness. The same pattern
 * applies to {@link #startsWith(String, String)} vs.
 * {@link #startsWithStrict(String, String)}.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Equality</b> — {@link #exact(String, String)},
 *       {@link #exactIgnoreCase(String, String)}</li>
 *   <li><b>Substring</b> — {@link #contains(String, String)},
 *       {@link #containsStrict(String, String)},
 *       {@link #containsAny(String, String...)},
 *       {@link #containsAll(String, String...)}</li>
 *   <li><b>Prefix / suffix</b> — {@link #startsWith(String, String)},
 *       {@link #endsWith(String, String)},
 *       {@link #startsWithStrict(String, String)},
 *       {@link #endsWithStrict(String, String)}</li>
 *   <li><b>Regex</b> — {@link #matches(String, String)} (full match),
 *       {@link #find(String, String)} (partial match)</li>
 *   <li><b>Whole word</b> — {@link #wholeWord(String, String)}</li>
 *   <li><b>Fuzzy</b> — {@link #fuzzy(String, String, int)},
 *       {@link #levenshtein(String, String)}</li>
 *   <li><b>Composite</b> — {@link #matcher(String, String, Object...)}
 *       with {@link Key} options</li>
 *   <li><b>Bulk</b> — {@link #anyContains(List, String)},
 *       {@link #filter(List, String)}</li>
 * </ul>
 *
 * <h2>Case-insensitivity in detail</h2>
 * <p>All non-strict methods use
 * {@link java.util.Locale#ROOT}, not {@link String#equalsIgnoreCase(String)}.
 * This means:</p>
 * <ul>
 *   <li>{@code "I".equalsIgnoreCase("i")} is {@code true} (Unicode rules)</li>
 *   <li>{@code Search.contains("I", "i")} is also {@code true}, but
 *       {@code "İ".toLowerCase(Locale.ROOT)} produces {@code "i̇"} (two
 *       codepoints), which is the correct Unicode behaviour</li>
 * </ul>
 * <p>The practical difference only matters for locale-specific casing
 * (Turkish İ/i, German ß/ss), where {@code Locale.ROOT} gives the
 * standard, portable answer.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Search.contains("Hello World", "world");     // true  (case-insensitive)
 * Search.containsStrict("Hello World", "world"); // false (case-sensitive)
 * Search.startsWith("Hello", "he");            // true
 * Search.endsWith("Hello", "LO");              // true
 * Search.matches("abc123", "[a-z]+\\d+");      // true
 * Search.wholeWord("Hello World", "World");    // true
 * Search.wholeWord("HelloWorld", "World");     // false (not a whole word)
 * Search.fuzzy("colour", "color", 2);          // true
 * Search.levenshtein("kitten", "sitting");     // 3
 * }</pre>
 *
 * <h2>When to use this vs. {@link Filter}</h2>
 * <p>{@code Search} is for <b>text-in-text</b> operations — is a substring
 * in a string, does a string match a pattern. {@link Filter} is for
 * <b>predicates on lists</b> — take a list and return a smaller list. They
 * compose: {@code Search.filter} is literally a {@code Filter.where} with
 * a pre-applied "contains" predicate.</p>
 *
 * <h2>Related</h2>
 * <p>For boolean checks against well-known formats (email, phone, IP), see
 * {@link chirp.text.Check}. For ready-to-use regex strings, see
 * {@link chirp.text.Regex}. For fuzzy similarity scoring (0.0–1.0 instead
 * of edit distance), see {@link chirp.text.Text#similarity(String, String)}.</p>
 *
 * @see Filter
 * @see chirp.text.Check
 * @see chirp.text.Regex
 * @see chirp.text.Text
 * @since 1.0.0
 */
public final class Search {

    /**
     * Composite-check keys for the key-value {@link #matcher(String, String, Object...)}
     * method.
     *
     * <p>Each key expects a specific value type; the matcher skips keys
     * with the wrong type rather than throwing.</p>
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Expects a {@link Boolean}. Case-sensitive matching when true. */
        CASE_SENSITIVE,
        /** Expects a {@link Boolean}. Whole-word matching when true. */
        WHOLE_WORD,
        /** Expects an {@link Integer}. Fuzzy matching with this max distance. */
        MAX_DISTANCE
    }

    private Search() {}

    // ============================================================== equality

    /**
     * Checks whether two strings are exactly equal (case-sensitive).
     *
     * @param text the text to search in (may be null)
     * @param query the text to match (may be null)
     * @return {@code true} if equal and non-null
     * @see #equals(String, String)
     * @see #sameAs(String, String)
     * @see #exactIgnoreCase(String, String)
     * @since 1.0.0
     */
    public static boolean exact(String text, String query) {
        if (text == null || query == null) return false;
        return text.equals(query);
    }

    /**
     * Alias of {@link #exact(String, String)} — see that method for full
     * documentation.
     *
     * @param text the text to search in
     * @param query the text to match
     * @return {@code true} if equal
     * @see #exact(String, String)
     * @since 1.0.0
     */
    public static boolean equals(String text, String query) { return exact(text, query); }

    /**
     * Alias of {@link #exact(String, String)} — see that method for full
     * documentation.
     *
     * @param text the text to search in
     * @param query the text to match
     * @return {@code true} if equal
     * @see #exact(String, String)
     * @since 1.0.0
     */
    public static boolean sameAs(String text, String query) { return exact(text, query); }

    /**
     * Checks whether two strings are equal ignoring case.
     *
     * <p>Uses {@link String#equalsIgnoreCase(String)}, which follows
     * Unicode case rules — behaviour is identical on every locale, unlike
     * the {@code toLowerCase()} comparison that would be affected by a
     * Turkish locale.</p>
     *
     * @param text the text to search in (may be null)
     * @param query the text to match (may be null)
     * @return {@code true} if case-insensitively equal
     * @see #equalsIgnoreCase(String, String)
     * @see #exact(String, String)
     * @since 1.0.0
     */
    public static boolean exactIgnoreCase(String text, String query) {
        if (text == null || query == null) return false;
        return text.equalsIgnoreCase(query);
    }

    /**
     * Alias of {@link #exactIgnoreCase(String, String)} — see that method
     * for full documentation.
     *
     * @param text the text to search in
     * @param query the text to match
     * @return {@code true} if case-insensitively equal
     * @see #exactIgnoreCase(String, String)
     * @since 1.0.0
     */
    public static boolean equalsIgnoreCase(String text, String query) { return exactIgnoreCase(text, query); }

    // ============================================================== contains

    /**
     * Checks whether the text contains the query as a substring,
     * <b>ignoring case</b>.
     *
     * <pre>{@code
     * Search.contains("Hello World", "world");   // true
     * Search.contains("Hello World", "WORLD");   // true
     * Search.contains("Hello World", "xyz");     // false
     * }</pre>
     *
     * @param text the text to search in (may be null)
     * @param query the substring to find (may be null)
     * @return {@code true} if present (case-insensitive)
     * @see #containsIgnoreCase(String, String)
     * @see #looseContains(String, String)
     * @see #includes(String, String)
     * @see #has(String, String)
     * @see #containsStrict(String, String)
     * @since 1.0.0
     */
    public static boolean contains(String text, String query) {
        if (text == null || query == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    /**
     * Alias of {@link #contains(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param query the substring to find
     * @return {@code true} if present (case-insensitive)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean containsIgnoreCase(String text, String query) { return contains(text, query); }

    /**
     * Alias of {@link #contains(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param query the substring to find
     * @return {@code true} if present (case-insensitive)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean looseContains(String text, String query) { return contains(text, query); }

    /**
     * Alias of {@link #contains(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param query the substring to find
     * @return {@code true} if present (case-insensitive)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean includes(String text, String query) { return contains(text, query); }

    /**
     * Alias of {@link #contains(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param query the substring to find
     * @return {@code true} if present (case-insensitive)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean has(String text, String query) { return contains(text, query); }

    /**
     * Checks whether the text contains the query as a substring,
     * <b>respecting case</b>.
     *
     * @param text the text to search in (may be null)
     * @param query the substring to find (may be null)
     * @return {@code true} if present (case-sensitive)
     * @see #exactContains(String, String)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean containsStrict(String text, String query) {
        if (text == null || query == null) return false;
        return text.contains(query);
    }

    /**
     * Alias of {@link #containsStrict(String, String)} — see that method
     * for full documentation.
     *
     * @param text the text to search in
     * @param query the substring to find
     * @return {@code true} if present (case-sensitive)
     * @see #containsStrict(String, String)
     * @since 1.0.0
     */
    public static boolean exactContains(String text, String query) { return containsStrict(text, query); }

    /**
     * Checks whether the text contains <b>any</b> of the given queries
     * (case-insensitive).
     *
     * @param text the text to search in (may be null)
     * @param queries the substrings to check (may be null or empty)
     * @return {@code true} if any query is present
     * @see #containsAll(String, String...)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean containsAny(String text, String... queries) {
        if (text == null || queries == null) return false;
        for (String q : queries) if (contains(text, q)) return true;
        return false;
    }

    /**
     * Checks whether the text contains <b>all</b> of the given queries
     * (case-insensitive).
     *
     * @param text the text to search in (may be null)
     * @param queries the substrings to check (may be null or empty)
     * @return {@code true} if all queries are present
     * @see #containsAny(String, String...)
     * @see #contains(String, String)
     * @since 1.0.0
     */
    public static boolean containsAll(String text, String... queries) {
        if (text == null || queries == null) return false;
        for (String q : queries) if (!contains(text, q)) return false;
        return true;
    }

    // ============================================================== prefix / suffix

    /**
     * Checks whether the text starts with the prefix, ignoring case.
     *
     * @param text the text to check (may be null)
     * @param prefix the prefix to test (may be null)
     * @return {@code true} if the text starts with the prefix
     * @see #beginsWith(String, String)
     * @see #prefix(String, String)
     * @see #startsWithStrict(String, String)
     * @see #endsWith(String, String)
     * @since 1.0.0
     */
    public static boolean startsWith(String text, String prefix) {
        if (text == null || prefix == null) return false;
        return text.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    /**
     * Alias of {@link #startsWith(String, String)} — see that method for
     * full documentation.
     *
     * @param text the text to check
     * @param prefix the prefix to test
     * @return {@code true} if the text starts with the prefix
     * @see #startsWith(String, String)
     * @since 1.0.0
     */
    public static boolean beginsWith(String text, String prefix) { return startsWith(text, prefix); }

    /**
     * Alias of {@link #startsWith(String, String)} — see that method for
     * full documentation.
     *
     * @param text the text to check
     * @param prefix the prefix to test
     * @return {@code true} if the text starts with the prefix
     * @see #startsWith(String, String)
     * @since 1.0.0
     */
    public static boolean prefix(String text, String prefix) { return startsWith(text, prefix); }

    /**
     * Checks whether the text starts with the prefix, respecting case.
     *
     * @param text the text to check (may be null)
     * @param prefix the prefix to test (may be null)
     * @return {@code true} if the text starts with the prefix (case-sensitive)
     * @see #startsWith(String, String)
     * @since 1.0.0
     */
    public static boolean startsWithStrict(String text, String prefix) {
        if (text == null || prefix == null) return false;
        return text.startsWith(prefix);
    }

    /**
     * Checks whether the text ends with the suffix, ignoring case.
     *
     * @param text the text to check (may be null)
     * @param suffix the suffix to test (may be null)
     * @return {@code true} if the text ends with the suffix
     * @see #suffix(String, String)
     * @see #endsWithStrict(String, String)
     * @see #startsWith(String, String)
     * @since 1.0.0
     */
    public static boolean endsWith(String text, String suffix) {
        if (text == null || suffix == null) return false;
        return text.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT));
    }

    /**
     * Alias of {@link #endsWith(String, String)} — see that method for
     * full documentation.
     *
     * @param text the text to check
     * @param suffix the suffix to test
     * @return {@code true} if the text ends with the suffix
     * @see #endsWith(String, String)
     * @since 1.0.0
     */
    public static boolean suffix(String text, String suffix) { return endsWith(text, suffix); }

    /**
     * Checks whether the text ends with the suffix, respecting case.
     *
     * @param text the text to check (may be null)
     * @param suffix the suffix to test (may be null)
     * @return {@code true} if the text ends with the suffix (case-sensitive)
     * @see #endsWith(String, String)
     * @since 1.0.0
     */
    public static boolean endsWithStrict(String text, String suffix) {
        if (text == null || suffix == null) return false;
        return text.endsWith(suffix);
    }

    // ============================================================== regex

    /**
     * Checks whether the text <b>fully matches</b> the given regex — the
     * entire string must match, not just a portion.
     *
     * <pre>{@code
     * Search.matches("abc123", "[a-z]+\\d+");   // true
     * Search.matches("abc123xyz", "[a-z]+\\d+"); // false (extra chars at end)
     * }</pre>
     *
     * @param text the text to match (may be null)
     * @param regex the pattern (may be null)
     * @return {@code true} if the entire text matches
     * @see #regex(String, String)
     * @see #matchesRegex(String, String)
     * @see #pattern(String, String)
     * @see #find(String, String)
     * @since 1.0.0
     */
    public static boolean matches(String text, String regex) {
        if (text == null || regex == null) return false;
        return Pattern.compile(regex).matcher(text).matches();
    }

    /**
     * Alias of {@link #matches(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param text the text to match
     * @param regex the pattern
     * @return {@code true} if fully matched
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public static boolean regex(String text, String regex) { return matches(text, regex); }

    /**
     * Alias of {@link #matches(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param text the text to match
     * @param regex the pattern
     * @return {@code true} if fully matched
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public static boolean matchesRegex(String text, String regex) { return matches(text, regex); }

    /**
     * Alias of {@link #matches(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param text the text to match
     * @param regex the pattern
     * @return {@code true} if fully matched
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public static boolean pattern(String text, String regex) { return matches(text, regex); }

    /**
     * Checks whether the text <b>contains a match</b> for the regex
     * anywhere — partial match, not full.
     *
     * <pre>{@code
     * Search.find("abc123xyz", "\\d+");   // true
     * Search.matches("abc123xyz", "\\d+"); // false (not full match)
     * }</pre>
     *
     * @param text the text to search (may be null)
     * @param regex the pattern (may be null)
     * @return {@code true} if a match is found anywhere in the text
     * @see #containsPattern(String, String)
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public static boolean find(String text, String regex) {
        if (text == null || regex == null) return false;
        return Pattern.compile(regex).matcher(text).find();
    }

    /**
     * Alias of {@link #find(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param text the text to search
     * @param regex the pattern
     * @return {@code true} if a match is found anywhere
     * @see #find(String, String)
     * @since 1.0.0
     */
    public static boolean containsPattern(String text, String regex) { return find(text, regex); }

    // ============================================================== whole word

    /**
     * Checks whether the word appears in the text as a <b>whole word</b> —
     * bounded by non-word characters ({@code \w} on each side).
     *
     * <p>Case-insensitive by default. For case-sensitive whole-word
     * matching, use {@link #wholeWord(String, String, boolean)} with
     * {@code true}.</p>
     *
     * <pre>{@code
     * Search.wholeWord("Hello World", "World");    // true
     * Search.wholeWord("HelloWorld", "World");     // false (no boundary)
     * Search.wholeWord("Hello, World!", "World");  // true (punctuation is a boundary)
     * }</pre>
     *
     * @param text the text to search in (may be null)
     * @param word the word to find (may be null or empty)
     * @return {@code true} if the word is present as a whole word
     * @see #exactWord(String, String)
     * @see #wordMatch(String, String)
     * @see #wholeWord(String, String, boolean)
     * @since 1.0.0
     */
    public static boolean wholeWord(String text, String word) {
        return wholeWord(text, word, false);
    }

    /**
     * Alias of {@link #wholeWord(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param word the word to find
     * @return {@code true} if present as a whole word
     * @see #wholeWord(String, String)
     * @since 1.0.0
     */
    public static boolean exactWord(String text, String word) { return wholeWord(text, word); }

    /**
     * Alias of {@link #wholeWord(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to search in
     * @param word the word to find
     * @return {@code true} if present as a whole word
     * @see #wholeWord(String, String)
     * @since 1.0.0
     */
    public static boolean wordMatch(String text, String word) { return wholeWord(text, word); }

    /**
     * Checks whether the word appears as a whole word, with explicit
     * case-sensitivity control.
     *
     * <p>Uses possessive quantifier lookarounds ({@code (?<!\w)} and
     * {@code (?!\w)}) to detect word boundaries. The query is wrapped in
     * {@link Pattern#quote(String)} so regex metacharacters in the word
     * are treated literally.</p>
     *
     * @param text the text to search in (may be null)
     * @param word the word to find (may be null or empty)
     * @param caseSensitive whether matching respects case
     * @return {@code true} if present as a whole word
     * @see #wholeWord(String, String)
     * @since 1.0.0
     */
    public static boolean wholeWord(String text, String word, boolean caseSensitive) {
        if (text == null || word == null || word.isEmpty()) {
            Trace.trace("search", "wholeWord: null/empty input");
            return false;
        }
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            String pattern = "(?<!\\w)" + Pattern.quote(word) + "(?!\\w)";
            return Pattern.compile(pattern, flags).matcher(text).find();
        } catch (RuntimeException e) {
            Trace.warn("search", "wholeWord failed: " + e.getMessage());
            return false;
        }
    }

    // ============================================================== fuzzy

    /**
     * Checks whether the query is within {@code maxDistance} edits of the
     * text — a fuzzy match based on {@link #levenshtein(String, String)}.
     *
     * <p>Case-insensitive on both sides before computing the distance, so
     * {@code fuzzy("Colour", "color", 2)} is {@code true}.</p>
     *
     * <pre>{@code
     * Search.fuzzy("colour", "color", 1);           // true  (1 edit)
     * Search.fuzzy("kitten", "sitting", 3);         // true  (3 edits)
     * Search.fuzzy("hello", "world", 2);            // false (4 edits)
     * Search.fuzzy("anything", "xyz", 0);           // false (not identical)
     * }</pre>
     *
     * @param text the text to compare (may be null)
     * @param query the query to compare (may be null)
     * @param maxDistance the maximum allowed edit distance (must be &ge; 0)
     * @return {@code true} if the edit distance is &le; {@code maxDistance}
     * @see #fuzzyMatch(String, String, int)
     * @see #similarTo(String, String, int)
     * @see #approx(String, String, int)
     * @see #levenshtein(String, String)
     * @since 1.0.0
     */
    public static boolean fuzzy(String text, String query, int maxDistance) {
        if (text == null || query == null || maxDistance < 0) return false;
        return levenshtein(text.toLowerCase(Locale.ROOT), query.toLowerCase(Locale.ROOT)) <= maxDistance;
    }

    /**
     * Alias of {@link #fuzzy(String, String, int)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to compare
     * @param query the query to compare
     * @param maxDistance the maximum allowed edit distance
     * @return {@code true} if within the distance
     * @see #fuzzy(String, String, int)
     * @since 1.0.0
     */
    public static boolean fuzzyMatch(String text, String query, int maxDistance) {
        return fuzzy(text, query, maxDistance);
    }

    /**
     * Alias of {@link #fuzzy(String, String, int)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to compare
     * @param query the query to compare
     * @param maxDistance the maximum allowed edit distance
     * @return {@code true} if within the distance
     * @see #fuzzy(String, String, int)
     * @since 1.0.0
     */
    public static boolean similarTo(String text, String query, int maxDistance) {
        return fuzzy(text, query, maxDistance);
    }

    /**
     * Alias of {@link #fuzzy(String, String, int)} — see that method for
     * full documentation and examples.
     *
     * @param text the text to compare
     * @param query the query to compare
     * @param maxDistance the maximum allowed edit distance
     * @return {@code true} if within the distance
     * @see #fuzzy(String, String, int)
     * @since 1.0.0
     */
    public static boolean approx(String text, String query, int maxDistance) {
        return fuzzy(text, query, maxDistance);
    }

    /**
     * Computes the Levenshtein (edit) distance between two strings — the
     * minimum number of single-character insertions, deletions, or
     * substitutions needed to transform one into the other.
     *
     * <p>Uses the iterative two-array algorithm — {@code O(m·n)} time,
     * {@code O(n)} space, no recursion. Null arguments are treated as
     * empty strings.</p>
     *
     * <pre>{@code
     * Search.levenshtein("kitten", "sitting");   // 3
     * Search.levenshtein("hello", "hello");      // 0
     * Search.levenshtein("cat", "cats");         // 1
     * Search.levenshtein("", "hello");           // 5
     * Search.levenshtein(null, null);            // 0
     * }</pre>
     *
     * @param a the first string (may be null)
     * @param b the second string (may be null)
     * @return the edit distance (non-negative)
     * @see #editDistance(String, String)
     * @see #distance(String, String)
     * @see #fuzzy(String, String, int)
     * @see chirp.text.Text#levenshtein(String, String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #levenshtein(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param a the first string
     * @param b the second string
     * @return the edit distance
     * @see #levenshtein(String, String)
     * @since 1.0.0
     */
    public static int editDistance(String a, String b) { return levenshtein(a, b); }

    /**
     * Alias of {@link #levenshtein(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param a the first string
     * @param b the second string
     * @return the edit distance
     * @see #levenshtein(String, String)
     * @since 1.0.0
     */
    public static int distance(String a, String b) { return levenshtein(a, b); }

    // ============================================================== composite (key-value)

    /**
     * Composite matcher that combines case-sensitivity, whole-word, and
     * fuzzy matching in a single call.
     *
     * <p>The rules, in priority order:</p>
     * <ol>
     *   <li>If {@link Key#WHOLE_WORD} is {@code true}, do whole-word
     *       matching (respecting {@link Key#CASE_SENSITIVE})</li>
     *   <li>Else, if {@link Key#MAX_DISTANCE} is &gt; 0, do fuzzy
     *       matching with that distance</li>
     *   <li>Else, do plain substring matching (respecting
     *       {@link Key#CASE_SENSITIVE})</li>
     * </ol>
     *
     * <p>Unknown keys and wrong value types are silently skipped — the
     * method is safe to call with any mixture of valid and invalid
     * arguments.</p>
     *
     * <pre>{@code
     * // Default — case-insensitive substring
     * Search.matcher("Hello World", "world");    // true
     *
     * // Case-sensitive
     * Search.matcher("Hello World", "world",
     *     Search.Key.CASE_SENSITIVE, true);      // false
     *
     * // Whole-word, case-sensitive
     * Search.matcher("Hello World", "World",
     *     Search.Key.WHOLE_WORD, true,
     *     Search.Key.CASE_SENSITIVE, true);      // true
     *
     * // Fuzzy match with distance 2
     * Search.matcher("colour", "color",
     *     Search.Key.MAX_DISTANCE, 2);           // true
     * }</pre>
     *
     * @param text the text to search in (may be null)
     * @param query the query to find (may be null)
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return {@code true} if the query matches according to the configured rules
     * @see #match(String, String, Object...)
     * @see Key
     * @since 1.0.0
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

    /**
     * Alias of {@link #matcher(String, String, Object...)} — see that
     * method for full documentation and examples.
     *
     * @param text the text to search in
     * @param query the query to find
     * @param keyValues alternating {@link Key} → value pairs
     * @return {@code true} if the query matches
     * @see #matcher(String, String, Object...)
     * @since 1.0.0
     */
    public static boolean match(String text, String query, Object... keyValues) {
        return matcher(text, query, keyValues);
    }

    // ============================================================== bulk

    /**
     * Checks whether any string in the list contains the query
     * (case-insensitive).
     *
     * @param items the list of strings (may be null)
     * @param query the substring to look for (may be null)
     * @return {@code true} if any item contains the query
     * @see #filter(List, String)
     * @since 1.0.0
     */
    public static boolean anyContains(List<String> items, String query) {
        if (items == null) return false;
        for (String item : items) if (contains(item, query)) return true;
        return false;
    }

    /**
     * Filters the list, keeping only items that contain the query
     * (case-insensitive).
     *
     * <p>Non-string items are converted to strings via
     * {@link String#valueOf(Object)} before the check — so a list of
     * {@code Integer}s can be filtered by their string form.</p>
     *
     * <pre>{@code
     * Search.filter(List.of("apple", "banana", "apricot"), "ap");
     * // [apple, apricot]
     * }</pre>
     *
     * @param items the list to filter (may be null)
     * @param query the substring to look for (may be null)
     * @param <T> the element type
     * @return a new filtered list
     * @see #search(List, String)
     * @see #anyContains(List, String)
     * @see Filter#where(List, Predicate)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #filter(List, String)} — see that method for full
     * documentation and examples.
     *
     * @param items the list to filter
     * @param query the substring to look for
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, String)
     * @since 1.0.0
     */
    public static <T> List<T> search(List<T> items, String query) { return filter(items, query); }

    /**
     * Filters the list by an arbitrary predicate — a convenience
     * alternative to {@link Filter#where(List, Predicate)} for callers who
     * already import {@code Search}.
     *
     * @param items the list to filter (may be null)
     * @param predicate the test (may be null — returns empty)
     * @param <T> the element type
     * @return a new filtered list
     * @see #filter(List, String)
     * @see Filter#where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) return result;
        for (T item : items) if (predicate.test(item)) result.add(item);
        return result;
    }
}
