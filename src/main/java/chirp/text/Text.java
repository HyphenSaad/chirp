package chirp.text;

import java.util.Locale;

/**
 * Null-safe string utilities.
 *
 * <h2>Naming</h2>
 * <p>Every method has 2–4 aliases covering common English spellings
 * (British/American) and synonyms. Aliases are pure delegates — the same
 * behaviour, just a different name in your code.</p>
 *
 * <h2>Null safety</h2>
 * <p>No method throws on {@code null}. Each returns a documented default
 * (usually {@code null}, {@code ""}, {@code 0}, or {@code false}).</p>
 *
 * <h2>Locale</h2>
 * <p>Every case transformation uses {@link Locale#ROOT}. This makes results
 * identical on a Turkish-locale machine, where {@code "I".toLowerCase()}
 * produces the dotless {@code "ı"} with the default locale but {@code "i"}
 * with {@link Locale#ROOT}.</p>
 */
public final class Text {

    private Text() {}

    // ============================================================== factories

    /** Creates a fluent chainer wrapping the given text. */
    public static TextChain chain(String text) {
        return TextChain.of(text);
    }
    public static TextChain of(String text) { return TextChain.of(text); }
    public static TextChain on(String text) { return TextChain.of(text); }

    // ============================================================== checks

    public static boolean isBlank(String s) { return s == null || s.isBlank(); }
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean isNotBlank(String s) { return !isBlank(s); }
    public static boolean isNotEmpty(String s) { return !isEmpty(s); }
    public static boolean hasText(String s) { return isNotBlank(s); }

    // ============================================================== case

    public static String capitalize(String s) {
        if (isBlank(s)) return s;
        String trimmed = s.trim();
        return Character.toUpperCase(trimmed.charAt(0))
                + trimmed.substring(1).toLowerCase(Locale.ROOT);
    }
    public static String capitalise(String s) { return capitalize(s); }
    public static String toCapitalized(String s) { return capitalize(s); }
    public static String upperFirst(String s) { return capitalize(s); }

    public static String titleCase(String s) {
        if (isBlank(s)) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split("\\s+")) sb.append(capitalize(w)).append(' ');
        return sb.toString().trim();
    }
    public static String toTitleCase(String s) { return titleCase(s); }
    public static String headlineCase(String s) { return titleCase(s); }

    public static String toCamelCase(String s) {
        if (isBlank(s)) return s;
        String[] parts = s.trim().split("[\\s_-]+");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < parts.length; i++) sb.append(capitalize(parts[i]));
        return sb.toString();
    }
    public static String camelCase(String s) { return toCamelCase(s); }
    public static String toCamel(String s) { return toCamelCase(s); }

    public static String toPascalCase(String s) {
        if (isBlank(s)) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split("[\\s_-]+")) sb.append(capitalize(w));
        return sb.toString();
    }
    public static String pascalCase(String s) { return toPascalCase(s); }
    public static String toClassName(String s) { return toPascalCase(s); }

    public static String toSnakeCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s-]+", "_").toLowerCase(Locale.ROOT);
    }
    public static String snakeCase(String s) { return toSnakeCase(s); }
    public static String toUnderscoreCase(String s) { return toSnakeCase(s); }

    public static String toKebabCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s_]+", "-").toLowerCase(Locale.ROOT);
    }
    public static String kebabCase(String s) { return toKebabCase(s); }
    public static String toDashCase(String s) { return toKebabCase(s); }

    public static String toDotCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s_-]+", ".").toLowerCase(Locale.ROOT);
    }

    /** Swaps case of every letter — "Hello" → "hELLO". */
    public static String swapCase(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append(Character.toLowerCase(c));
            else if (Character.isLowerCase(c)) sb.append(Character.toUpperCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }

    /** @return true if every letter is uppercase. */
    public static boolean isUpperCase(String s) {
        if (isBlank(s)) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c) && !Character.isUpperCase(c)) return false;
        return true;
    }

    /** @return true if every letter is lowercase. */
    public static boolean isLowerCase(String s) {
        if (isBlank(s)) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c) && !Character.isLowerCase(c)) return false;
        return true;
    }

    // ============================================================== simple

    public static String reverse(String s) {
        return s == null ? null : new StringBuilder(s).reverse().toString();
    }
    public static String reverseText(String s) { return reverse(s); }
    public static String flip(String s) { return reverse(s); }

    /** Reverses the order of words but not letters: "a b c" → "c b a". */
    public static String reverseWords(String s) {
        if (isBlank(s)) return s;
        String[] parts = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public static String repeat(String s, int times) {
        if (s == null) return null;
        if (times <= 0) return "";
        return s.repeat(times);
    }

    public static String padLeft(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        return " ".repeat(width - v.length()) + v;
    }
    public static String leftPad(String s, int width) { return padLeft(s, width); }
    public static String padStart(String s, int width) { return padLeft(s, width); }

    public static String padRight(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        return v + " ".repeat(width - v.length());
    }
    public static String rightPad(String s, int width) { return padRight(s, width); }
    public static String padEnd(String s, int width) { return padRight(s, width); }

    public static String center(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        int total = width - v.length();
        int left = total / 2;
        int right = total - left;
        return " ".repeat(left) + v + " ".repeat(right);
    }
    public static String centre(String s, int width) { return center(s, width); }
    public static String padCenter(String s, int w) { return center(s, w); }

    public static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        if (max <= 1) return "…";
        return s.substring(0, max - 1) + "…";
    }
    public static String cut(String s, int max) { return truncate(s, max); }
    public static String shorten(String s, int max) { return truncate(s, max); }
    public static String abbreviate(String s, int max) { return truncate(s, max); }

    public static String truncateMiddle(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        int keep = (max - 1) / 2;
        return s.substring(0, keep) + "…" + s.substring(s.length() - keep);
    }

    public static String wrap(String s, int width) {
        if (isBlank(s)) return s;
        StringBuilder sb = new StringBuilder();
        int lineLen = 0;
        for (String word : s.trim().split("\\s+")) {
            if (lineLen + word.length() + 1 > width && lineLen > 0) {
                sb.append('\n');
                lineLen = 0;
            }
            if (lineLen > 0) { sb.append(' '); lineLen++; }
            sb.append(word);
            lineLen += word.length();
        }
        return sb.toString();
    }
    public static String wrapText(String s, int width) { return wrap(s, width); }
    public static String wordWrap(String s, int width) { return wrap(s, width); }

    public static String indent(String s, int spaces) {
        if (s == null) return null;
        String pad = " ".repeat(spaces);
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n", -1)) sb.append(pad).append(line).append('\n');
        return sb.toString().stripTrailing();
    }

    public static String dedent(String s) {
        if (isBlank(s)) return s;
        String[] lines = s.split("\n", -1);
        int minIndent = Integer.MAX_VALUE;
        for (String line : lines) {
            if (line.isBlank()) continue;
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') indent++;
            minIndent = Math.min(minIndent, indent);
        }
        if (minIndent == Integer.MAX_VALUE || minIndent == 0) return s;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line.length() >= minIndent ? line.substring(minIndent) : line).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    // ============================================================== counts

    public static int countChar(String s, char c) {
        if (s == null) return 0;
        int n = 0;
        for (char ch : s.toCharArray()) if (ch == c) n++;
        return n;
    }
    public static int countOccurrences(String s, char c) { return countChar(s, c); }

    public static int countWords(String s) {
        if (isBlank(s)) return 0;
        return s.trim().split("\\s+").length;
    }
    public static int wordCount(String s) { return countWords(s); }

    public static int countSubstrings(String s, String sub) {
        if (s == null || sub == null || sub.isEmpty()) return 0;
        int count = 0, idx = 0;
        while ((idx = s.indexOf(sub, idx)) != -1) { count++; idx += sub.length(); }
        return count;
    }

    // ============================================================== transformations

    public static boolean isPalindrome(String s) {
        if (isBlank(s)) return false;
        String clean = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return clean.equals(new StringBuilder(clean).reverse().toString());
    }

    /** @return true if the two strings are anagrams (ignoring case and non-letters). */
    public static boolean isAnagram(String a, String b) {
        if (a == null || b == null) return false;
        String na = a.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        String nb = b.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (na.length() != nb.length()) return false;
        char[] ca = na.toCharArray();
        char[] cb = nb.toCharArray();
        java.util.Arrays.sort(ca);
        java.util.Arrays.sort(cb);
        return java.util.Arrays.equals(ca, cb);
    }

    public static String removeSpaces(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }
    public static String removeWhitespace(String s) { return removeSpaces(s); }
    public static String stripSpaces(String s) { return removeSpaces(s); }

    /** Removes all punctuation characters. */
    public static String removePunctuation(String s) {
        return s == null ? null : s.replaceAll("[\\p{Punct}]", "");
    }

    /** Removes all ASCII digits. */
    public static String removeDigits(String s) {
        return s == null ? null : s.replaceAll("\\d", "");
    }

    /** Keeps only ASCII digits. */
    public static String extractDigits(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    /** Keeps only letters (ASCII). */
    public static String extractLetters(String s) {
        return s == null ? null : s.replaceAll("[^A-Za-z]", "");
    }

    public static String initials(String name) {
        if (isBlank(name)) return "";
        StringBuilder sb = new StringBuilder();
        for (String w : name.trim().split("\\s+")) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
        }
        return sb.toString();
    }
    public static String getInitials(String name) { return initials(name); }
    public static String acronym(String name) { return initials(name); }

    public static String join(String delimiter, Object... parts) {
        if (parts == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public static String mask(String s, int visible) {
        if (s == null) return null;
        if (s.length() <= visible) return "*".repeat(s.length());
        return "*".repeat(s.length() - visible) + s.substring(s.length() - visible);
    }

    public static String slugify(String s) {
        if (isBlank(s)) return "";
        return s.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
    public static String toSlug(String s) { return slugify(s); }

    public static String between(String s, String start, String end) {
        if (s == null || start == null || end == null) return null;
        int i = s.indexOf(start);
        if (i < 0) return null;
        int j = s.indexOf(end, i + start.length());
        if (j < 0) return null;
        return s.substring(i + start.length(), j);
    }
    public static String substringBetween(String s, String start, String end) { return between(s, start, end); }
    public static String extractBetween(String s, String start, String end) { return between(s, start, end); }

    public static String substringBefore(String s, String marker) {
        if (s == null || marker == null) return s;
        int i = s.indexOf(marker);
        return i < 0 ? s : s.substring(0, i);
    }

    public static String substringAfter(String s, String marker) {
        if (s == null || marker == null) return s;
        int i = s.indexOf(marker);
        return i < 0 ? "" : s.substring(i + marker.length());
    }

    // ============================================================== similarity

    public static double similarity(String a, String b) {
        if (a == null && b == null) return 1.0;
        if (a == null || b == null) return 0.0;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshtein(a, b);
        return 1.0 - (double) distance / maxLen;
    }

    public static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int m = a.length();
        int n = b.length();
        if (m == 0) return n;
        if (n == 0) return m;

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[n];
    }
    public static int editDistance(String a, String b) { return levenshtein(a, b); }

    // ============================================================== integration

    /**
     * Filters a list of strings by a {@link Check} predicate.
     * Integration point — no need to remember which package holds which check.
     *
     * <pre>{@code
     * List<String> valid = Text.thatCheck(emails, Check::isEmail);
     * }</pre>
     */
    public static java.util.List<String> thatCheck(java.util.List<String> items,
            java.util.function.Predicate<String> check) {
        if (items == null || check == null) return java.util.List.of();
        return items.stream().filter(check).toList();
    }
    public static java.util.List<String> filterByCheck(java.util.List<String> items,
            java.util.function.Predicate<String> check) {
        return thatCheck(items, check);
    }
}
