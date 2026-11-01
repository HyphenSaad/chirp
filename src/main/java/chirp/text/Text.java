package chirp.text;

import java.util.Locale;

/**
 * Null-safe string utilities for every common text transformation.
 *
 * <p>The most-used class in Chirp. Every method here is <b>null-safe</b>:
 * no call ever throws {@link NullPointerException} for a {@code null}
 * argument. Instead, each method returns a documented default — usually
 * {@code null} for "return the same string back", {@code ""} for "return
 * an empty string", {@code 0} for counts, and {@code false} for
 * predicates.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Null / blank checks</b> — {@link #isBlank(String)},
 *       {@link #isEmpty(String)}, {@link #isNotBlank(String)}</li>
 *   <li><b>Case conversion</b> — {@link #capitalize(String)},
 *       {@link #titleCase(String)}, {@link #toCamelCase(String)},
 *       {@link #toPascalCase(String)}, {@link #toSnakeCase(String)},
 *       {@link #toKebabCase(String)}, {@link #swapCase(String)}</li>
 *   <li><b>Simple transforms</b> — {@link #reverse(String)},
 *       {@link #repeat(String, int)}, {@link #padLeft(String, int)},
 *       {@link #padRight(String, int)}, {@link #center(String, int)},
 *       {@link #truncate(String, int)}, {@link #wrap(String, int)}</li>
 *   <li><b>Counting</b> — {@link #countChar(String, char)},
 *       {@link #countWords(String)},
 *       {@link #countSubstrings(String, String)}</li>
 *   <li><b>Content</b> — {@link #isPalindrome(String)},
 *       {@link #isAnagram(String, String)},
 *       {@link #removePunctuation(String)},
 *       {@link #extractDigits(String)},
 *       {@link #slugify(String)},
 *       {@link #mask(String, int)}</li>
 *   <li><b>Similarity</b> — {@link #similarity(String, String)},
 *       {@link #levenshtein(String, String)}</li>
 * </ul>
 *
 * <h2>Aliases</h2>
 * <p>Every method has 1–4 aliases covering British spellings
 * ({@code capitalise}), common English synonyms ({@code cut / shorten /
 * abbreviate}), and the equivalent-framework name in other languages
 * ({@code camelCase}, {@code snakeCase}). The aliases are pure delegates —
 * not separate implementations — so behaviour is identical.</p>
 *
 * <h2>Null-safety contract</h2>
 * <p>The behaviour of every method for {@code null} is documented on the
 * method itself. The design is:</p>
 * <ul>
 *   <li>Transforms that <b>produce a new string</b> return {@code null}
 *       when the input was {@code null} — {@link #reverse(String)},
 *       {@link #capitalize(String)}, {@link #swapCase(String)}</li>
 *   <li>Transforms that <b>produce a fixed-shape output</b> (padding,
 *       truncation) return the input unchanged (which is {@code null})</li>
 *   <li>Predicates return {@code false} for {@code null} —
 *       {@link #isBlank(String)}, {@link #isPalindrome(String)}</li>
 *   <li>Counters return {@code 0} for {@code null} —
 *       {@link #countChar(String, char)}</li>
 *   <li>Extractors that build a new string return {@code null} for
 *       {@code null} — {@link #extractDigits(String)}</li>
 * </ul>
 *
 * <h2>Locale</h2>
 * <p>Every case transformation uses {@link java.util.Locale#ROOT}, so
 * results are identical on a Turkish-locale machine where
 * {@code "I".toLowerCase()} would otherwise produce the dotless
 * {@code "ı"}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Text.capitalize("hello");                // "Hello"
 * Text.titleCase("hello world");            // "Hello World"
 * Text.toCamelCase("hello world");          // "helloWorld"
 * Text.toSnakeCase("HelloWorld");           // "hello_world"
 * Text.reverse("abc");                      // "cba"
 * Text.padLeft("42", 5);                    // "   42"
 * Text.truncate("very long text", 10);      // "very lon…"
 * Text.isPalindrome("A man a plan a canal Panama"); // true
 * Text.slugify("Hello, World!");            // "hello-world"
 * Text.similarity("kitten", "sitting");     // ~0.571
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For fluent chaining, see {@link TextChain}. For boolean format
 * validations (email, phone, etc.), see {@link Check}. For pre-built
 * regex strings, see {@link Regex}.</p>
 *
 * @see TextChain
 * @see Check
 * @see Regex
 * @since 1.0.0
 */
public final class Text {

    private Text() {}

    // ============================================================== factories

    /**
     * Wraps a string in a fluent {@link TextChain} for chained
     * transformations.
     *
     * <p>Every method on {@code TextChain} returns a new chain wrapping the
     * previous one, so a chain is immutable and thread-safe. The wrapped
     * string is not evaluated until {@link TextChain#get()} is called.
     * See {@link TextChain} for the full chain API.</p>
     *
     * <pre>{@code
     * String result = Text.chain("  hello world  ")
     *     .trim()
     *     .titleCase()
     *     .append("!")
     *     .get();
     * // "Hello World!"
     * }</pre>
     *
     * @param text the string to wrap (may be null)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see TextChain
     * @see #of(String)
     * @see #on(String)
     * @since 1.0.0
     */
    public static TextChain chain(String text) {
        return TextChain.of(text);
    }

    /**
     * Alias of {@link #chain(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the string to wrap (may be null)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see #chain(String)
     * @since 1.0.0
     */
    public static TextChain of(String text) { return TextChain.of(text); }

    /**
     * Alias of {@link #chain(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the string to wrap (may be null)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see #chain(String)
     * @since 1.0.0
     */
    public static TextChain on(String text) { return TextChain.of(text); }

    // ============================================================== checks

    /**
     * Checks whether the given string is {@code null}, empty, or contains
     * only whitespace.
     *
     * <pre>{@code
     * Text.isBlank(null);       // true
     * Text.isBlank("");         // true
     * Text.isBlank("   ");      // true
     * Text.isBlank("  hello "); // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is null, empty, or whitespace-only
     * @see #isEmpty(String)
     * @see Check#isBlank(String)
     * @since 1.0.0
     */
    public static boolean isBlank(String s) { return s == null || s.isBlank(); }

    /**
     * Checks whether the given string is {@code null} or has zero length.
     *
     * <p>Unlike {@link #isBlank(String)}, a string of spaces is <b>not</b>
     * empty. Use this when you need to distinguish "no characters" from
     * "only whitespace".</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is null or has length 0
     * @see #isBlank(String)
     * @since 1.0.0
     */
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }

    /**
     * Alias of {@code !isBlank(s)} — see {@link #isBlank(String)} for the
     * definition of "blank".
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} contains at least one non-whitespace
     * character; {@code false} otherwise (including for {@code null})
     * @see #isBlank(String)
     * @since 1.0.0
     */
    public static boolean isNotBlank(String s) { return !isBlank(s); }

    /**
     * Alias of {@code !isEmpty(s)} — see {@link #isEmpty(String)}.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and has length &gt; 0
     * @see #isEmpty(String)
     * @since 1.0.0
     */
    public static boolean isNotEmpty(String s) { return !isEmpty(s); }

    /**
     * Alias of {@link #isNotBlank(String)} — see {@link #isBlank(String)}.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is not blank
     * @see #isNotBlank(String)
     * @since 1.0.0
     */
    public static boolean hasText(String s) { return isNotBlank(s); }

    // ============================================================== case

    /**
     * Capitalises the first letter of the string and lowercases the rest.
     *
     * <p>The input is trimmed before capitalisation, so leading and trailing
     * whitespace is removed. Returns {@code null} for {@code null} input.</p>
     *
     * <pre>{@code
     * Text.capitalize("hello");    // "Hello"
     * Text.capitalize("HELLO");    // "Hello"
     * Text.capitalize("  hello  ");// "Hello"
     * Text.capitalize("");         // ""
     * }</pre>
     *
     * @param s the string to capitalise (may be null)
     * @return the capitalised string, or {@code null} if {@code s} is null
     * @see #capitalise(String)
     * @see #toCapitalized(String)
     * @see #upperFirst(String)
     * @see #titleCase(String)
     * @since 1.0.0
     */
    public static String capitalize(String s) {
        if (isBlank(s)) return s;
        String trimmed = s.trim();
        return Character.toUpperCase(trimmed.charAt(0))
                + trimmed.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Alias of {@link #capitalize(String)} using the British spelling
     * {@code capitalise}. See that method for full documentation and
     * examples.
     *
     * @param s the string to capitalise (may be null)
     * @return the capitalised string, or {@code null} if {@code s} is null
     * @see #capitalize(String)
     * @since 1.0.0
     */
    public static String capitalise(String s) { return capitalize(s); }

    /**
     * Alias of {@link #capitalize(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to capitalise (may be null)
     * @return the capitalised string, or {@code null} if {@code s} is null
     * @see #capitalize(String)
     * @since 1.0.0
     */
    public static String toCapitalized(String s) { return capitalize(s); }

    /**
     * Alias of {@link #capitalize(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to capitalise (may be null)
     * @return the capitalised string, or {@code null} if {@code s} is null
     * @see #capitalize(String)
     * @since 1.0.0
     */
    public static String upperFirst(String s) { return capitalize(s); }

    /**
     * Capitalises every word in the string and lowercases the rest.
     *
     * <p>Words are separated by one or more whitespace characters. The
     * input is trimmed first. Returns {@code null} for {@code null}.</p>
     *
     * <pre>{@code
     * Text.titleCase("hello world");   // "Hello World"
     * Text.titleCase("HELLO WORLD");   // "Hello World"
     * Text.titleCase("hello  world");  // "Hello World"
     * }</pre>
     *
     * @param s the string to title-case (may be null)
     * @return the title-cased string, or {@code null} if {@code s} is null
     * @see #toTitleCase(String)
     * @see #headlineCase(String)
     * @see #capitalize(String)
     * @since 1.0.0
     */
    public static String titleCase(String s) {
        if (isBlank(s)) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split("\\s+")) sb.append(capitalize(w)).append(' ');
        return sb.toString().trim();
    }

    /**
     * Alias of {@link #titleCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to title-case (may be null)
     * @return the title-cased string
     * @see #titleCase(String)
     * @since 1.0.0
     */
    public static String toTitleCase(String s) { return titleCase(s); }

    /**
     * Alias of {@link #titleCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to title-case (may be null)
     * @return the title-cased string
     * @see #titleCase(String)
     * @since 1.0.0
     */
    public static String headlineCase(String s) { return titleCase(s); }

    /**
     * Converts the string to lower camelCase.
     *
     * <p>Words can be separated by whitespace, hyphens, or underscores.
     * The first word is lowercased entirely; each subsequent word is
     * capitalised. Returns {@code null} for {@code null}.</p>
     *
     * <pre>{@code
     * Text.toCamelCase("hello world");    // "helloWorld"
     * Text.toCamelCase("hello_world");    // "helloWorld"
     * Text.toCamelCase("hello-world");    // "helloWorld"
     * Text.toCamelCase("HelloWorld");     // "helloWorld"  (best-effort)
     * }</pre>
     *
     * @param s the string to convert (may be null)
     * @return the camelCase string, or {@code null} if {@code s} is null
     * @see #camelCase(String)
     * @see #toCamel(String)
     * @see #toPascalCase(String)
     * @since 1.0.0
     */
    public static String toCamelCase(String s) {
        if (isBlank(s)) return s;
        String[] parts = s.trim().split("[\\s_-]+");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < parts.length; i++) sb.append(capitalize(parts[i]));
        return sb.toString();
    }

    /**
     * Alias of {@link #toCamelCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the camelCase string
     * @see #toCamelCase(String)
     * @since 1.0.0
     */
    public static String camelCase(String s) { return toCamelCase(s); }

    /**
     * Alias of {@link #toCamelCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the camelCase string
     * @see #toCamelCase(String)
     * @since 1.0.0
     */
    public static String toCamel(String s) { return toCamelCase(s); }

    /**
     * Converts the string to UpperCamelCase / PascalCase.
     *
     * <p>Words separated by whitespace, hyphens, or underscores are each
     * capitalised and concatenated. Often used for class names. Returns
     * {@code null} for {@code null}.</p>
     *
     * <pre>{@code
     * Text.toPascalCase("hello world");   // "HelloWorld"
     * Text.toPascalCase("hello_world");   // "HelloWorld"
     * Text.toPascalCase("hello-world");   // "HelloWorld"
     * }</pre>
     *
     * @param s the string to convert (may be null)
     * @return the PascalCase string, or {@code null} if {@code s} is null
     * @see #pascalCase(String)
     * @see #toClassName(String)
     * @see #toCamelCase(String)
     * @since 1.0.0
     */
    public static String toPascalCase(String s) {
        if (isBlank(s)) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split("[\\s_-]+")) sb.append(capitalize(w));
        return sb.toString();
    }

    /**
     * Alias of {@link #toPascalCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the PascalCase string
     * @see #toPascalCase(String)
     * @since 1.0.0
     */
    public static String pascalCase(String s) { return toPascalCase(s); }

    /**
     * Alias of {@link #toPascalCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the PascalCase string
     * @see #toPascalCase(String)
     * @since 1.0.0
     */
    public static String toClassName(String s) { return toPascalCase(s); }

    /**
     * Converts the string to snake_case — words separated by single
     * underscores, all lowercase.
     *
     * <p>Whitespace and hyphens become underscores. Returns {@code null}
     * for {@code null}.</p>
     *
     * <pre>{@code
     * Text.toSnakeCase("Hello World");    // "hello_world"
     * Text.toSnakeCase("Hello-World");    // "hello_world"
     * Text.toSnakeCase("HELLO WORLD");    // "hello_world"
     * }</pre>
     *
     * @param s the string to convert (may be null)
     * @return the snake_case string, or {@code null} if {@code s} is null
     * @see #snakeCase(String)
     * @see #toUnderscoreCase(String)
     * @see #toKebabCase(String)
     * @since 1.0.0
     */
    public static String toSnakeCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s-]+", "_").toLowerCase(Locale.ROOT);
    }

    /**
     * Alias of {@link #toSnakeCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the snake_case string
     * @see #toSnakeCase(String)
     * @since 1.0.0
     */
    public static String snakeCase(String s) { return toSnakeCase(s); }

    /**
     * Alias of {@link #toSnakeCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the snake_case string
     * @see #toSnakeCase(String)
     * @since 1.0.0
     */
    public static String toUnderscoreCase(String s) { return toSnakeCase(s); }

    /**
     * Converts the string to kebab-case — words separated by single
     * hyphens, all lowercase.
     *
     * <p>Whitespace and underscores become hyphens. Returns {@code null}
     * for {@code null}.</p>
     *
     * <pre>{@code
     * Text.toKebabCase("Hello World");    // "hello-world"
     * Text.toKebabCase("hello_world");    // "hello-world"
     * }</pre>
     *
     * @param s the string to convert (may be null)
     * @return the kebab-case string, or {@code null} if {@code s} is null
     * @see #kebabCase(String)
     * @see #toDashCase(String)
     * @see #toSnakeCase(String)
     * @since 1.0.0
     */
    public static String toKebabCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s_]+", "-").toLowerCase(Locale.ROOT);
    }

    /**
     * Alias of {@link #toKebabCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the kebab-case string
     * @see #toKebabCase(String)
     * @since 1.0.0
     */
    public static String kebabCase(String s) { return toKebabCase(s); }

    /**
     * Alias of {@link #toKebabCase(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to convert (may be null)
     * @return the kebab-case string
     * @see #toKebabCase(String)
     * @since 1.0.0
     */
    public static String toDashCase(String s) { return toKebabCase(s); }

    /**
     * Converts the string to dot.case — words separated by single periods,
     * all lowercase. Commonly used for property keys and configuration
     * names.
     *
     * @param s the string to convert (may be null)
     * @return the dot.case string, or {@code null} if {@code s} is null
     * @since 1.0.0
     */
    public static String toDotCase(String s) {
        if (isBlank(s)) return s;
        return s.trim().replaceAll("[\\s_-]+", ".").toLowerCase(Locale.ROOT);
    }

    /**
     * Swaps the case of every letter — uppercase becomes lowercase and
     * vice versa. Non-letters are unchanged.
     *
     * <pre>{@code
     * Text.swapCase("Hello World");   // "hELLO wORLD"
     * Text.swapCase("123-ABC");       // "123-abc"
     * }</pre>
     *
     * @param s the string to swap (may be null)
     * @return the case-swapped string, or {@code null} if {@code s} is null
     * @since 1.0.0
     */
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

    /**
     * Checks whether every letter in the string is uppercase. Non-letter
     * characters are ignored.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if every letter is uppercase; {@code false} for
     * null, blank, or any string containing a lowercase letter
     * @see #isLowerCase(String)
     * @since 1.0.0
     */
    public static boolean isUpperCase(String s) {
        if (isBlank(s)) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c) && !Character.isUpperCase(c)) return false;
        return true;
    }

    /**
     * Checks whether every letter in the string is lowercase. Non-letter
     * characters are ignored.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if every letter is lowercase; {@code false} for
     * null, blank, or any string containing an uppercase letter
     * @see #isUpperCase(String)
     * @since 1.0.0
     */
    public static boolean isLowerCase(String s) {
        if (isBlank(s)) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c) && !Character.isLowerCase(c)) return false;
        return true;
    }

    // ============================================================== simple

    /**
     * Reverses the characters of the string.
     *
     * <pre>{@code
     * Text.reverse("abc");   // "cba"
     * Text.reverse("hello"); // "olleh"
     * }</pre>
     *
     * @param s the string to reverse (may be null)
     * @return the reversed string, or {@code null} if {@code s} is null
     * @see #reverseText(String)
     * @see #flip(String)
     * @see #reverseWords(String)
     * @since 1.0.0
     */
    public static String reverse(String s) {
        return s == null ? null : new StringBuilder(s).reverse().toString();
    }

    /**
     * Alias of {@link #reverse(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to reverse (may be null)
     * @return the reversed string
     * @see #reverse(String)
     * @since 1.0.0
     */
    public static String reverseText(String s) { return reverse(s); }

    /**
     * Alias of {@link #reverse(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to reverse (may be null)
     * @return the reversed string
     * @see #reverse(String)
     * @since 1.0.0
     */
    public static String flip(String s) { return reverse(s); }

    /**
     * Reverses the <b>order of words</b>, keeping each word's characters
     * intact.
     *
     * <pre>{@code
     * Text.reverseWords("a b c");      // "c b a"
     * Text.reverseWords("hello world");// "world hello"
     * }</pre>
     *
     * @param s the string to transform (may be null)
     * @return the word-reversed string, or {@code null} if {@code s} is null
     * @see #reverse(String)
     * @since 1.0.0
     */
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

    /**
     * Repeats the string {@code times} times.
     *
     * <p>Returns {@code ""} (empty string) for {@code times <= 0} and
     * {@code null} for {@code null} input.</p>
     *
     * <pre>{@code
     * Text.repeat("ab", 3);   // "ababab"
     * Text.repeat("x", 0);    // ""
     * Text.repeat("x", -1);   // ""
     * }</pre>
     *
     * @param s the string to repeat (may be null)
     * @param times how many times to repeat
     * @return the repeated string, or {@code null} if {@code s} is null
     * @since 1.0.0
     */
    public static String repeat(String s, int times) {
        if (s == null) return null;
        if (times <= 0) return "";
        return s.repeat(times);
    }

    /**
     * Pads the string on the left with spaces to reach {@code width}
     * characters.
     *
     * <p>If the string is already at least {@code width} characters, it is
     * returned unchanged. Returns {@code ""} padded if the input is
     * {@code null}.</p>
     *
     * <pre>{@code
     * Text.padLeft("42", 5);    // "   42"
     * Text.padLeft("hello", 3); // "hello"  (already wide enough)
     * Text.padLeft(null, 4);    // "    "
     * }</pre>
     *
     * @param s the string to pad (may be null)
     * @param width the target width in characters
     * @return the padded string
     * @see #leftPad(String, int)
     * @see #padStart(String, int)
     * @see #padRight(String, int)
     * @see #center(String, int)
     * @since 1.0.0
     */
    public static String padLeft(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        return " ".repeat(width - v.length()) + v;
    }

    /**
     * Alias of {@link #padLeft(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to pad (may be null)
     * @param width the target width
     * @return the padded string
     * @see #padLeft(String, int)
     * @since 1.0.0
     */
    public static String leftPad(String s, int width) { return padLeft(s, width); }

    /**
     * Alias of {@link #padLeft(String, int)} — see that method for full
     * documentation and examples. Uses the JavaScript naming convention.
     *
     * @param s the string to pad (may be null)
     * @param width the target width
     * @return the padded string
     * @see #padLeft(String, int)
     * @since 1.0.0
     */
    public static String padStart(String s, int width) { return padLeft(s, width); }

    /**
     * Pads the string on the right with spaces to reach {@code width}
     * characters.
     *
     * <pre>{@code
     * Text.padRight("42", 5);   // "42   "
     * Text.padRight(null, 4);   // "    "
     * }</pre>
     *
     * @param s the string to pad (may be null)
     * @param width the target width in characters
     * @return the padded string
     * @see #rightPad(String, int)
     * @see #padEnd(String, int)
     * @see #padLeft(String, int)
     * @see #center(String, int)
     * @since 1.0.0
     */
    public static String padRight(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        return v + " ".repeat(width - v.length());
    }

    /**
     * Alias of {@link #padRight(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to pad (may be null)
     * @param width the target width
     * @return the padded string
     * @see #padRight(String, int)
     * @since 1.0.0
     */
    public static String rightPad(String s, int width) { return padRight(s, width); }

    /**
     * Alias of {@link #padRight(String, int)} — see that method for full
     * documentation and examples. Uses the JavaScript naming convention.
     *
     * @param s the string to pad (may be null)
     * @param width the target width
     * @return the padded string
     * @see #padRight(String, int)
     * @since 1.0.0
     */
    public static String padEnd(String s, int width) { return padRight(s, width); }

    /**
     * Pads the string on both sides with spaces to reach {@code width}
     * characters. When the total padding is odd, the extra space goes on
     * the right.
     *
     * <pre>{@code
     * Text.center("hi", 6);   // "  hi  "
     * Text.center("hi", 5);   // " hi  "
     * }</pre>
     *
     * @param s the string to center (may be null)
     * @param width the target width in characters
     * @return the centered string
     * @see #centre(String, int)
     * @see #padCenter(String, int)
     * @see #padLeft(String, int)
     * @see #padRight(String, int)
     * @since 1.0.0
     */
    public static String center(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) return v;
        int total = width - v.length();
        int left = total / 2;
        int right = total - left;
        return " ".repeat(left) + v + " ".repeat(right);
    }

    /**
     * Alias of {@link #center(String, int)} using the British spelling
     * {@code centre}. See that method for full documentation and examples.
     *
     * @param s the string to center (may be null)
     * @param width the target width
     * @return the centered string
     * @see #center(String, int)
     * @since 1.0.0
     */
    public static String centre(String s, int width) { return center(s, width); }

    /**
     * Alias of {@link #center(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to center (may be null)
     * @param w the target width
     * @return the centered string
     * @see #center(String, int)
     * @since 1.0.0
     */
    public static String padCenter(String s, int w) { return center(s, w); }

    /**
     * Truncates the string to at most {@code max} characters, appending
     * an ellipsis ({@code …}) if truncation occurred. The ellipsis is
     * included in the {@code max} length.
     *
     * <pre>{@code
     * Text.truncate("abcdef", 10);   // "abcdef"  (no change)
     * Text.truncate("abcdef", 4);    // "abc…"
     * Text.truncate("abcdef", 1);    // "…"
     * }</pre>
     *
     * @param s the string to truncate (may be null)
     * @param max the maximum output length in characters
     * @return the truncated string, or {@code null} if {@code s} is null
     * @see #cut(String, int)
     * @see #shorten(String, int)
     * @see #abbreviate(String, int)
     * @see #truncateMiddle(String, int)
     * @since 1.0.0
     */
    public static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        if (max <= 1) return "…";
        return s.substring(0, max - 1) + "…";
    }

    /**
     * Alias of {@link #truncate(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to truncate (may be null)
     * @param max the maximum output length
     * @return the truncated string
     * @see #truncate(String, int)
     * @since 1.0.0
     */
    public static String cut(String s, int max) { return truncate(s, max); }

    /**
     * Alias of {@link #truncate(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to truncate (may be null)
     * @param max the maximum output length
     * @return the truncated string
     * @see #truncate(String, int)
     * @since 1.0.0
     */
    public static String shorten(String s, int max) { return truncate(s, max); }

    /**
     * Alias of {@link #truncate(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to truncate (may be null)
     * @param max the maximum output length
     * @return the truncated string
     * @see #truncate(String, int)
     * @since 1.0.0
     */
    public static String abbreviate(String s, int max) { return truncate(s, max); }

    /**
     * Truncates the string from the middle, keeping the beginning and end
     * visible and inserting an ellipsis. Useful for file paths and URLs
     * where both ends are informative.
     *
     * <pre>{@code
     * Text.truncateMiddle("/very/long/path/to/file.txt", 20);
     * // "/very/lon…o/file.txt"  (approximately — preserves both ends)
     * }</pre>
     *
     * @param s the string to truncate (may be null)
     * @param max the maximum output length in characters
     * @return the truncated string, or {@code null} if {@code s} is null
     * @see #truncate(String, int)
     * @since 1.0.0
     */
    public static String truncateMiddle(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        int keep = (max - 1) / 2;
        return s.substring(0, keep) + "…" + s.substring(s.length() - keep);
    }

    /**
     * Word-wraps the string at approximately {@code width} characters per
     * line, breaking only at existing spaces. Newline characters
     * ({@code \n}) are inserted at wrap points.
     *
     * <pre>{@code
     * Text.wrap("one two three four five", 10);
     * // "one two\nthree four\nfive"
     * }</pre>
     *
     * @param s the string to wrap (may be null)
     * @param width the target line width in characters
     * @return the wrapped string, or {@code null} if {@code s} is null
     * @see #wrapText(String, int)
     * @see #wordWrap(String, int)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #wrap(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to wrap (may be null)
     * @param width the target line width
     * @return the wrapped string
     * @see #wrap(String, int)
     * @since 1.0.0
     */
    public static String wrapText(String s, int width) { return wrap(s, width); }

    /**
     * Alias of {@link #wrap(String, int)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to wrap (may be null)
     * @param width the target line width
     * @return the wrapped string
     * @see #wrap(String, int)
     * @since 1.0.0
     */
    public static String wordWrap(String s, int width) { return wrap(s, width); }

    /**
     * Indents every line of the string by {@code spaces} leading spaces.
     * Trailing blank line is removed.
     *
     * <pre>{@code
     * Text.indent("a\nb", 2);
     * // "  a\n  b"
     * }</pre>
     *
     * @param s the string to indent (may be null)
     * @param spaces the number of leading spaces to add per line
     * @return the indented string, or {@code null} if {@code s} is null
     * @see #dedent(String)
     * @since 1.0.0
     */
    public static String indent(String s, int spaces) {
        if (s == null) return null;
        String pad = " ".repeat(spaces);
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n", -1)) sb.append(pad).append(line).append('\n');
        return sb.toString().stripTrailing();
    }

    /**
     * Removes the common leading whitespace from every non-blank line of
     * the string. The opposite of {@link #indent(String, int)}. Blank
     * lines are preserved.
     *
     * <pre>{@code
     * Text.dedent("    a\n    b\n");  // "a\nb"
     * }</pre>
     *
     * @param s the string to dedent (may be null)
     * @return the dedented string, or {@code null} if {@code s} is null
     * @see #indent(String, int)
     * @since 1.0.0
     */
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

    /**
     * Counts occurrences of the given character in the string.
     *
     * @param s the string to search (may be null)
     * @param c the character to count
     * @return the number of times {@code c} appears; {@code 0} if
     * {@code s} is null
     * @see #countOccurrences(String, char)
     * @see #countWords(String)
     * @see #countSubstrings(String, String)
     * @since 1.0.0
     */
    public static int countChar(String s, char c) {
        if (s == null) return 0;
        int n = 0;
        for (char ch : s.toCharArray()) if (ch == c) n++;
        return n;
    }

    /**
     * Alias of {@link #countChar(String, char)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to search (may be null)
     * @param c the character to count
     * @return the number of times {@code c} appears
     * @see #countChar(String, char)
     * @since 1.0.0
     */
    public static int countOccurrences(String s, char c) { return countChar(s, c); }

    /**
     * Counts words in the string — sequences of non-whitespace characters
     * separated by whitespace.
     *
     * @param s the string to count words in (may be null)
     * @return the number of words; {@code 0} if {@code s} is null or blank
     * @see #wordCount(String)
     * @see #countChar(String, char)
     * @since 1.0.0
     */
    public static int countWords(String s) {
        if (isBlank(s)) return 0;
        return s.trim().split("\\s+").length;
    }

    /**
     * Alias of {@link #countWords(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to count words in (may be null)
     * @return the number of words
     * @see #countWords(String)
     * @since 1.0.0
     */
    public static int wordCount(String s) { return countWords(s); }

    /**
     * Counts non-overlapping occurrences of a substring.
     *
     * <pre>{@code
     * Text.countSubstrings("aaa", "a");     // 3
     * Text.countSubstrings("abcabc", "abc");// 2
     * Text.countSubstrings("aaa", "aa");    // 1  (non-overlapping)
     * }</pre>
     *
     * @param s the string to search (may be null)
     * @param sub the substring to find (may be null or empty)
     * @return the number of occurrences; {@code 0} if any argument is null
     * or if {@code sub} is empty
     * @since 1.0.0
     */
    public static int countSubstrings(String s, String sub) {
        if (s == null || sub == null || sub.isEmpty()) return 0;
        int count = 0, idx = 0;
        while ((idx = s.indexOf(sub, idx)) != -1) { count++; idx += sub.length(); }
        return count;
    }

    // ============================================================== transformations

    /**
     * Checks whether the string is a palindrome — the same forwards and
     * backwards, ignoring case and non-alphanumeric characters.
     *
     * <pre>{@code
     * Text.isPalindrome("racecar");                        // true
     * Text.isPalindrome("A man a plan a canal Panama");    // true
     * Text.isPalindrome("hello");                          // false
     * Text.isPalindrome("");                               // false
     * }</pre>
     *
     * @param s the string to check (may be null)
     * @return {@code true} if {@code s} is a palindrome; {@code false} for
     * null or blank input
     * @see #isAnagram(String, String)
     * @since 1.0.0
     */
    public static boolean isPalindrome(String s) {
        if (isBlank(s)) return false;
        String clean = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return clean.equals(new StringBuilder(clean).reverse().toString());
    }

    /**
     * Checks whether two strings are anagrams of each other — same letters,
     * same counts, ignoring case and non-alphanumeric characters.
     *
     * <pre>{@code
     * Text.isAnagram("listen", "silent");                  // true
     * Text.isAnagram("Hello", "World");                    // false
     * Text.isAnagram("Dormitory", "dirty room");           // true (spaces ignored)
     * }</pre>
     *
     * @param a the first string (may be null)
     * @param b the second string (may be null)
     * @return {@code true} if the strings are anagrams; {@code false} if
     * either argument is null or the cleaned strings differ in length
     * @see #isPalindrome(String)
     * @since 1.0.0
     */
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

    /**
     * Removes all whitespace characters (spaces, tabs, newlines) from
     * the string.
     *
     * @param s the string to process (may be null)
     * @return the string with all whitespace removed, or {@code null} if
     * {@code s} is null
     * @see #removeWhitespace(String)
     * @see #stripSpaces(String)
     * @since 1.0.0
     */
    public static String removeSpaces(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }

    /**
     * Alias of {@link #removeSpaces(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to process (may be null)
     * @return the string with all whitespace removed
     * @see #removeSpaces(String)
     * @since 1.0.0
     */
    public static String removeWhitespace(String s) { return removeSpaces(s); }

    /**
     * Alias of {@link #removeSpaces(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to process (may be null)
     * @return the string with all whitespace removed
     * @see #removeSpaces(String)
     * @since 1.0.0
     */
    public static String stripSpaces(String s) { return removeSpaces(s); }

    /**
     * Removes all punctuation characters (defined by Java's
     * {@code \p{Punct}} character class — the ASCII punctuation set is
     * {@code !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~}).
     *
     * @param s the string to process (may be null)
     * @return the string with punctuation removed, or {@code null} if
     * {@code s} is null
     * @see #removeDigits(String)
     * @since 1.0.0
     */
    public static String removePunctuation(String s) {
        return s == null ? null : s.replaceAll("[\\p{Punct}]", "");
    }

    /**
     * Removes all ASCII digits from the string.
     *
     * @param s the string to process (may be null)
     * @return the string with digits removed, or {@code null} if
     * {@code s} is null
     * @see #extractDigits(String)
     * @since 1.0.0
     */
    public static String removeDigits(String s) {
        return s == null ? null : s.replaceAll("\\d", "");
    }

    /**
     * Extracts only the ASCII digits from the string, discarding
     * everything else. Opposite of {@link #removeDigits(String)}.
     *
     * <pre>{@code
     * Text.extractDigits("a1b2c3");   // "123"
     * Text.extractDigits("+92 300-1234567"); // "923001234567"
     * }</pre>
     *
     * @param s the string to process (may be null)
     * @return the extracted digits, or {@code null} if {@code s} is null
     * @see #removeDigits(String)
     * @see #extractLetters(String)
     * @since 1.0.0
     */
    public static String extractDigits(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    /**
     * Extracts only the ASCII letters from the string, discarding
     * everything else.
     *
     * @param s the string to process (may be null)
     * @return the extracted letters, or {@code null} if {@code s} is null
     * @see #extractDigits(String)
     * @since 1.0.0
     */
    public static String extractLetters(String s) {
        return s == null ? null : s.replaceAll("[^A-Za-z]", "");
    }

    /**
     * Returns the uppercase initials of the string's words.
     *
     * <pre>{@code
     * Text.initials("saad mansoor");      // "SM"
     * Text.initials("John Ronald Tolkien");// "JRT"
     * }</pre>
     *
     * @param name the name to derive initials from (may be null)
     * @return the initials, or {@code ""} if the input is null or blank
     * @see #getInitials(String)
     * @see #acronym(String)
     * @since 1.0.0
     */
    public static String initials(String name) {
        if (isBlank(name)) return "";
        StringBuilder sb = new StringBuilder();
        for (String w : name.trim().split("\\s+")) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * Alias of {@link #initials(String)} — see that method for full
     * documentation and examples.
     *
     * @param name the name to derive initials from (may be null)
     * @return the initials
     * @see #initials(String)
     * @since 1.0.0
     */
    public static String getInitials(String name) { return initials(name); }

    /**
     * Alias of {@link #initials(String)} — see that method for full
     * documentation and examples.
     *
     * @param name the name to derive initials from (may be null)
     * @return the initials
     * @see #initials(String)
     * @since 1.0.0
     */
    public static String acronym(String name) { return initials(name); }

    /**
     * Joins the given parts into a single string, separated by
     * {@code delimiter}.
     *
     * @param delimiter the separator to place between parts (may be null
     * — {@code null} is treated as empty)
     * @param parts the values to join (may be null — returns empty)
     * @return the joined string
     * @see java.util.StringJoiner
     * @since 1.0.0
     */
    public static String join(String delimiter, Object... parts) {
        if (parts == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    /**
     * Masks all but the last {@code visible} characters of the string
     * with asterisks. Commonly used for credit cards and phone numbers.
     *
     * <pre>{@code
     * Text.mask("1234567890123456", 4);   // "************3456"
     * Text.mask("1234567", 4);             // "***4567"
     * Text.mask("1234", 4);                // "****"  (nothing to keep)
     * }</pre>
     *
     * @param s the string to mask (may be null)
     * @param visible the number of trailing characters to leave unmasked
     * @return the masked string, or {@code null} if {@code s} is null
     * @since 1.0.0
     */
    public static String mask(String s, int visible) {
        if (s == null) return null;
        if (s.length() <= visible) return "*".repeat(s.length());
        return "*".repeat(s.length() - visible) + s.substring(s.length() - visible);
    }

    /**
     * Converts the string to a URL-safe slug — lowercase, alphanumeric
     * with single hyphens as separators, no leading or trailing hyphens.
     *
     * <pre>{@code
     * Text.slugify("Hello, World!");           // "hello-world"
     * Text.slugify("  A  B  C  ");              // "a-b-c"
     * Text.slugify("Café Olé");                 // "caf-ol"
     * }</pre>
     *
     * <p>Note that non-ASCII letters are dropped, not transliterated. For
     * accent preservation, pre-normalise with
     * {@link java.text.Normalizer} first.</p>
     *
     * @param s the string to slugify (may be null)
     * @return the slug, or {@code ""} if the input is null or blank
     * @see #toSlug(String)
     * @see Regex#slug()
     * @since 1.0.0
     */
    public static String slugify(String s) {
        if (isBlank(s)) return "";
        return s.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Alias of {@link #slugify(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to slugify (may be null)
     * @return the slug
     * @see #slugify(String)
     * @since 1.0.0
     */
    public static String toSlug(String s) { return slugify(s); }

    /**
     * Extracts the substring strictly between two markers.
     *
     * <p>If either marker is missing from the string, returns {@code null}.
     * Markers are searched left-to-right: the {@code start} marker is
     * located first, then {@code end} is searched starting after it.</p>
     *
     * <pre>{@code
     * Text.between("start-middle-end", "start-", "-end");  // "middle"
     * Text.between("no markers here", "start-", "-end");   // null
     * }</pre>
     *
     * @param s the string to search (may be null)
     * @param start the opening marker (may be null)
     * @param end the closing marker (may be null)
     * @return the substring between the markers, or {@code null} if any
     * argument is null or the markers are not found in order
     * @see #substringBetween(String, String, String)
     * @see #extractBetween(String, String, String)
     * @see #substringBefore(String, String)
     * @see #substringAfter(String, String)
     * @since 1.0.0
     */
    public static String between(String s, String start, String end) {
        if (s == null || start == null || end == null) return null;
        int i = s.indexOf(start);
        if (i < 0) return null;
        int j = s.indexOf(end, i + start.length());
        if (j < 0) return null;
        return s.substring(i + start.length(), j);
    }

    /**
     * Alias of {@link #between(String, String, String)} — see that method
     * for full documentation and examples.
     *
     * @param s the string to search (may be null)
     * @param start the opening marker (may be null)
     * @param end the closing marker (may be null)
     * @return the substring between the markers
     * @see #between(String, String, String)
     * @since 1.0.0
     */
    public static String substringBetween(String s, String start, String end) { return between(s, start, end); }

    /**
     * Alias of {@link #between(String, String, String)} — see that method
     * for full documentation and examples.
     *
     * @param s the string to search (may be null)
     * @param start the opening marker (may be null)
     * @param end the closing marker (may be null)
     * @return the substring between the markers
     * @see #between(String, String, String)
     * @since 1.0.0
     */
    public static String extractBetween(String s, String start, String end) { return between(s, start, end); }

    /**
     * Returns the part of the string before the first occurrence of
     * {@code marker}.
     *
     * <pre>{@code
     * Text.substringBefore("user@example.com", "@");   // "user"
     * Text.substringBefore("no marker", "@");          // "no marker"
     * }</pre>
     *
     * @param s the string to split (may be null)
     * @param marker the marker to search for
     * @return the substring before the marker; the whole string if the
     * marker is not found; {@code null} if either argument is null
     * @see #substringAfter(String, String)
     * @since 1.0.0
     */
    public static String substringBefore(String s, String marker) {
        if (s == null || marker == null) return s;
        int i = s.indexOf(marker);
        return i < 0 ? s : s.substring(0, i);
    }

    /**
     * Returns the part of the string after the first occurrence of
     * {@code marker}.
     *
     * <pre>{@code
     * Text.substringAfter("user@example.com", "@");   // "example.com"
     * Text.substringAfter("no marker", "@");          // ""
     * }</pre>
     *
     * @param s the string to split (may be null)
     * @param marker the marker to search for
     * @return the substring after the marker; {@code ""} if the marker is
     * not found; {@code null} if either argument is null
     * @see #substringBefore(String, String)
     * @since 1.0.0
     */
    public static String substringAfter(String s, String marker) {
        if (s == null || marker == null) return s;
        int i = s.indexOf(marker);
        return i < 0 ? "" : s.substring(i + marker.length());
    }

    // ============================================================== similarity

    /**
     * Computes a similarity score between two strings based on Levenshtein
     * distance, normalised to {@code [0.0, 1.0]}.
     *
     * <p>{@code 1.0} means the strings are identical; {@code 0.0} means
     * they have nothing in common (every character differs). The
     * normalisation is {@code 1 - (editDistance / maxLength)}.</p>
     *
     * <pre>{@code
     * Text.similarity("hello", "hello");       // 1.0
     * Text.similarity("kitten", "sitting");    // ~0.571
     * Text.similarity("abc", "xyz");           // 0.0
     * Text.similarity(null, null);             // 1.0  (both null = identical)
     * }</pre>
     *
     * @param a the first string (may be null)
     * @param b the second string (may be null)
     * @return the similarity score in {@code [0.0, 1.0]}; {@code 1.0} if
     * both are null; {@code 0.0} if only one is null
     * @see #levenshtein(String, String)
     * @see chirp.collection.Search#fuzzy(String, String, int)
     * @since 1.0.0
     */
    public static double similarity(String a, String b) {
        if (a == null && b == null) return 1.0;
        if (a == null || b == null) return 0.0;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshtein(a, b);
        return 1.0 - (double) distance / maxLen;
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
     * Text.levenshtein("kitten", "sitting");   // 3
     * Text.levenshtein("hello", "hello");      // 0
     * Text.levenshtein("cat", "cats");         // 1
     * Text.levenshtein("", "hello");           // 5
     * }</pre>
     *
     * @param a the first string (may be null — treated as empty)
     * @param b the second string (may be null — treated as empty)
     * @return the edit distance (non-negative)
     * @see #similarity(String, String)
     * @see #editDistance(String, String)
     * @see chirp.collection.Search#levenshtein(String, String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #levenshtein(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param a the first string (may be null)
     * @param b the second string (may be null)
     * @return the edit distance
     * @see #levenshtein(String, String)
     * @since 1.0.0
     */
    public static int editDistance(String a, String b) { return levenshtein(a, b); }

    // ============================================================== integration

    /**
     * Filters a list of strings by a {@link Check}-style predicate.
     *
     * <p>Convenience bridge so a caller does not need to import both
     * {@code chirp.text.Text} and {@code chirp.collection.Filter} for a
     * single-line filter operation.</p>
     *
     * <pre>{@code
     * List<String> valid = Text.thatCheck(emails, Check::isEmail);
     * List<String> shortOnes = Text.thatCheck(names, s -> s.length() < 10);
     * }</pre>
     *
     * @param items the list of strings to filter (may be null — returns
     * empty list)
     * @param check the predicate to test each item with (may be null —
     * returns empty list)
     * @return a new list containing the items that passed the check
     * @see #filterByCheck(java.util.List, java.util.function.Predicate)
     * @see Check
     * @see chirp.collection.Filter
     * @since 1.0.0
     */
    public static java.util.List<String> thatCheck(java.util.List<String> items,
            java.util.function.Predicate<String> check) {
        if (items == null || check == null) return java.util.List.of();
        return items.stream().filter(check).toList();
    }

    /**
     * Alias of {@link #thatCheck(java.util.List, java.util.function.Predicate)}
     * — see that method for full documentation and examples.
     *
     * @param items the list of strings to filter (may be null)
     * @param check the predicate to test each item with (may be null)
     * @return a new list containing the matching items
     * @see #thatCheck(java.util.List, java.util.function.Predicate)
     * @since 1.0.0
     */
    public static java.util.List<String> filterByCheck(java.util.List<String> items,
            java.util.function.Predicate<String> check) {
        return thatCheck(items, check);
    }
}
