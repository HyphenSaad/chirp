package chirp.text;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Immutable fluent chainer for text transformations.
 *
 * <p>{@code TextChain} wraps a {@link java.util.function.Supplier Supplier}
 * around the source string. Every transformation method returns a <b>new</b>
 * chain that wraps the previous one — so the chain itself is immutable and
 * thread-safe, and the actual string value is not computed until a
 * terminal method ({@link #get()}, {@link #value()}, {@link #result()},
 * or {@link #toString()}) is called.</p>
 *
 * <h2>Why this class exists</h2>
 * <p>{@link Text} exposes each transform as a static method, which is fine
 * for a single operation:</p>
 * <pre>{@code
 * String result = Text.titleCase(Text.reverse(Text.truncate(s, 20)));
 * }</pre>
 * <p>…but becomes hard to read once there are more than two or three
 * nested calls. {@code TextChain} lets the same transformations read
 * left-to-right:</p>
 * <pre>{@code
 * String result = TextChain.of(s)
 *     .truncate(20)
 *     .reverse()
 *     .titleCase()
 *     .get();
 * }</pre>
 *
 * <h2>The Decorator pattern</h2>
 * <p>{@code TextChain} is a live demonstration of the <b>Decorator</b>
 * design pattern: each method wraps the current behaviour (the existing
 * supplier) with a new behaviour, producing a new object without
 * modifying the previous one. The structure is:</p>
 * <pre>
 *   TextChain.of("  hello  ")
 *       ↓ supplier = () -> "  hello  "
 *       .trim()
 *       ↓ supplier = () -> previous.get().trim()
 *       .titleCase()
 *       ↓ supplier = () -> Text.titleCase(previous.get())
 *       .get()  → "Hello"
 * </pre>
 * <p>Every step captures the previous supplier and defers to it. Nothing
 * executes until {@code get()}. This is the standard "lazy decorator"
 * pattern — the same structure used by Java's {@code Stream},
 * {@code Optional}, and {@code CompletableFuture}.</p>
 *
 * <h2>Null safety</h2>
 * <p>Every transformation is <b>null-propagating</b>: if the underlying
 * string is {@code null}, every transformation returns {@code null} and
 * the terminal method returns {@code null}. This means you can chain
 * unconditionally without checking for {@code null} between steps:</p>
 * <pre>{@code
 * String result = TextChain.of(null)
 *     .capitalize()
 *     .reverse()
 *     .append("!")
 *     .get();       // null, not "!…"
 * }</pre>
 * <p>{@link #append(String)} and {@link #prepend(String)} short-circuit on
 * a {@code null} chain rather than appending to the string "null". This
 * is the one place where the "return the same value back" rule applies
 * even for methods that would otherwise produce a non-null result.</p>
 *
 * <h2>Example — chain vs. nested</h2>
 * <pre>{@code
 * String input = "  hello world  ";
 *
 * // Without a chain (nested, right-to-left reading)
 * String a = Text.titleCase(Text.reverse(Text.capitalize(input.trim())));
 * // "Dlrow Olleh"
 *
 * // With a chain (left-to-right, readable)
 * String b = TextChain.of(input)
 *     .trim()
 *     .capitalize()
 *     .reverse()
 *     .titleCase()
 *     .get();
 * // "Dlrow Olleh"
 * }</pre>
 *
 * <h2>Custom steps</h2>
 * <p>{@link #then(UnaryOperator)} and its aliases {@link #map(UnaryOperator)}
 * and {@link #apply(UnaryOperator)} let you insert an arbitrary string
 * transform into the chain — useful for logic that is not part of
 * {@code Text}'s built-in operations:</p>
 * <pre>{@code
 * String result = TextChain.of("hello")
 *     .then(s -> s.repeat(2))
 *     .then(String::toUpperCase)
 *     .get();
 * // "HELLOHELLO"
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the underlying operations, see {@link Text}. For fluent numeric
 * chains, see {@link chirp.number.NumberChain}. For fluent array chains,
 * see {@link chirp.collection.ArrayChain}. For fluent date chains, see
 * {@link chirp.datetime.DateChain}.</p>
 *
 * @see Text
 * @see chirp.number.NumberChain
 * @see chirp.collection.ArrayChain
 * @see chirp.datetime.DateChain
 * @since 1.0.0
 */
public final class TextChain {

    private final java.util.function.Supplier<String> source;

    private TextChain(java.util.function.Supplier<String> source) {
        this.source = source;
    }

    // -------------------------------------------------------------- factories

    /**
     * Wraps a string literal in a new chain.
     *
     * <p>Every call to {@link #get()} on the returned chain returns the
     * same string — there is nothing to evaluate. For a chain that
     * re-evaluates on every {@code get()}, use
     * {@link #of(java.util.function.Supplier)} instead.</p>
     *
     * @param text the string to wrap (may be null — null propagates
     * through every transform)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see #of(java.util.function.Supplier)
     * @see #from(String)
     * @see #wrap(String)
     * @since 1.0.0
     */
    public static TextChain of(String text) { return new TextChain(() -> text); }

    /**
     * Wraps a supplier in a new chain. The supplier is called every time
     * a terminal method runs, so if the underlying value changes between
     * {@code get()} calls, the chain re-computes.
     *
     * <pre>{@code
     * AtomicInteger counter = new AtomicInteger();
     * TextChain chain = TextChain.of(() -> "value-" + counter.incrementAndGet());
     * chain.get();    // "value-1"
     * chain.get();    // "value-2"
     * }</pre>
     *
     * @param s the supplier of the string value (may be null — treated as
     * a supplier that returns {@code null})
     * @return a new {@code TextChain} wrapping {@code s}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextChain of(java.util.function.Supplier<String> s) { return new TextChain(s); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the string to wrap (may be null)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextChain from(String text) { return of(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the string to wrap (may be null)
     * @return a new {@code TextChain} wrapping {@code text}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextChain wrap(String text) { return of(text); }

    // -------------------------------------------------------------- transforms

    /**
     * Appends a step that capitalises the first letter and lowercases the
     * rest. Delegates to {@link Text#capitalize(String)}.
     *
     * @return a new chain with the capitalize step appended
     * @see Text#capitalize(String)
     * @see #titleCase()
     * @since 1.0.0
     */
    public TextChain capitalize() { return new TextChain(() -> Text.capitalize(source.get())); }

    /**
     * Appends a step that title-cases every word. Delegates to
     * {@link Text#titleCase(String)}.
     *
     * @return a new chain with the title-case step appended
     * @see Text#titleCase(String)
     * @see #capitalize()
     * @since 1.0.0
     */
    public TextChain titleCase() { return new TextChain(() -> Text.titleCase(source.get())); }

    /**
     * Appends a step that uppercases the whole string using
     * {@link java.util.Locale#ROOT} — locale-independent, so Turkish-locale
     * machines behave the same as everywhere else.
     *
     * @return a new chain with the uppercase step appended
     * @see #upper()
     * @see #toLowerCase()
     * @since 1.0.0
     */
    public TextChain toUpperCase() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.toUpperCase(Locale.ROOT);
        });
    }

    /**
     * Alias of {@link #toUpperCase()} — see that method for full
     * documentation.
     *
     * @return a new chain with the uppercase step appended
     * @see #toUpperCase()
     * @since 1.0.0
     */
    public TextChain upper() { return toUpperCase(); }

    /**
     * Appends a step that lowercases the whole string using
     * {@link java.util.Locale#ROOT} — locale-independent.
     *
     * @return a new chain with the lowercase step appended
     * @see #lower()
     * @see #toUpperCase()
     * @since 1.0.0
     */
    public TextChain toLowerCase() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.toLowerCase(Locale.ROOT);
        });
    }

    /**
     * Alias of {@link #toLowerCase()} — see that method for full
     * documentation.
     *
     * @return a new chain with the lowercase step appended
     * @see #toLowerCase()
     * @since 1.0.0
     */
    public TextChain lower() { return toLowerCase(); }

    /**
     * Appends a step that trims leading and trailing whitespace.
     *
     * @return a new chain with the trim step appended
     * @since 1.0.0
     */
    public TextChain trim() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.trim();
        });
    }

    /**
     * Appends a step that reverses the characters. Delegates to
     * {@link Text#reverse(String)}.
     *
     * @return a new chain with the reverse step appended
     * @see Text#reverse(String)
     * @see #reverseText()
     * @since 1.0.0
     */
    public TextChain reverse() { return new TextChain(() -> Text.reverse(source.get())); }

    /**
     * Alias of {@link #reverse()} — see that method for full
     * documentation.
     *
     * @return a new chain with the reverse step appended
     * @see #reverse()
     * @since 1.0.0
     */
    public TextChain reverseText() { return reverse(); }

    /**
     * Appends a step that removes all whitespace. Delegates to
     * {@link Text#removeSpaces(String)}.
     *
     * @return a new chain with the remove-whitespace step appended
     * @see Text#removeSpaces(String)
     * @since 1.0.0
     */
    public TextChain removeSpaces() { return new TextChain(() -> Text.removeSpaces(source.get())); }

    /**
     * Appends a step that left-pads with spaces to {@code w} characters.
     * Delegates to {@link Text#padLeft(String, int)}.
     *
     * @param w the target width
     * @return a new chain with the pad step appended
     * @see Text#padLeft(String, int)
     * @see #padRight(int)
     * @see #center(int)
     * @since 1.0.0
     */
    public TextChain padLeft(int w) { return new TextChain(() -> Text.padLeft(source.get(), w)); }

    /**
     * Appends a step that right-pads with spaces to {@code w} characters.
     * Delegates to {@link Text#padRight(String, int)}.
     *
     * @param w the target width
     * @return a new chain with the pad step appended
     * @see Text#padRight(String, int)
     * @see #padLeft(int)
     * @see #center(int)
     * @since 1.0.0
     */
    public TextChain padRight(int w) { return new TextChain(() -> Text.padRight(source.get(), w)); }

    /**
     * Appends a step that centers within {@code w} characters with spaces
     * on both sides. Delegates to {@link Text#center(String, int)}.
     *
     * @param w the target width
     * @return a new chain with the center step appended
     * @see Text#center(String, int)
     * @see #padLeft(int)
     * @see #padRight(int)
     * @since 1.0.0
     */
    public TextChain center(int w) { return new TextChain(() -> Text.center(source.get(), w)); }

    /**
     * Appends a step that truncates to at most {@code max} characters,
     * with an ellipsis if truncation occurred. Delegates to
     * {@link Text#truncate(String, int)}.
     *
     * @param max the maximum output length
     * @return a new chain with the truncate step appended
     * @see Text#truncate(String, int)
     * @since 1.0.0
     */
    public TextChain truncate(int max) { return new TextChain(() -> Text.truncate(source.get(), max)); }

    /**
     * Appends a suffix to the string.
     *
     * <p><b>Null short-circuit:</b> if the underlying string is
     * {@code null}, this step returns {@code null} rather than the
     * literal string {@code "null"} plus the suffix. This preserves the
     * null-propagating contract of the whole chain.</p>
     *
     * @param suffix the string to append (may be null — treated as empty)
     * @return a new chain with the append step appended
     * @see #prepend(String)
     * @see #surround(String, String)
     * @since 1.0.0
     */
    public TextChain append(String suffix) {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v + suffix;
        });
    }

    /**
     * Appends a prefix before the string.
     *
     * <p>Null short-circuit: {@code null} propagates unchanged.</p>
     *
     * @param prefix the string to prepend (may be null — treated as empty)
     * @return a new chain with the prepend step appended
     * @see #append(String)
     * @see #surround(String, String)
     * @since 1.0.0
     */
    public TextChain prepend(String prefix) {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : prefix + v;
        });
    }

    /**
     * Wraps the string with the given prefix and suffix — e.g. to place it
     * inside brackets, quotes, or HTML tags.
     *
     * <pre>{@code
     * TextChain.of("hello").surround("[", "]").get();    // "[hello]"
     * TextChain.of("hi").surround("<b>", "</b>").get();  // "<b>hi</b>"
     * }</pre>
     *
     * @param prefix the string to put before (may be null — treated as empty)
     * @param suffix the string to put after (may be null — treated as empty)
     * @return a new chain with the surround step appended
     * @see #append(String)
     * @see #prepend(String)
     * @since 1.0.0
     */
    public TextChain surround(String prefix, String suffix) {
        return new TextChain(() -> {
            String v = source.get();
            if (v == null) return null;
            return prefix + v + suffix;
        });
    }

    /**
     * Replaces all literal occurrences of {@code target} with
     * {@code replacement}. Uses {@link String#replace(CharSequence, CharSequence)}
     * — plain-text, not regex.
     *
     * @param target the string to find (may be null — no replacement
     * happens, the string is returned unchanged)
     * @param replacement the string to substitute (may be null — treated
     * as empty string, effectively removing the target)
     * @return a new chain with the replace step appended
     * @since 1.0.0
     */
    public TextChain replace(String target, String replacement) {
        return new TextChain(() -> {
            String v = source.get();
            if (v == null || target == null) return v;
            return v.replace(target, replacement == null ? "" : replacement);
        });
    }

    /**
     * Appends an arbitrary decorator step — the raw Decorator-pattern
     * demonstration. The supplied function receives the current string
     * and returns a new one.
     *
     * <pre>{@code
     * String result = TextChain.of("hello")
     *     .then(s -> s + "!")
     *     .then(String::toUpperCase)
     *     .get();     // "HELLO!"
     * }</pre>
     *
     * @param decorator the function to apply (may be null — the step is
     * ignored, and the chain is returned unchanged)
     * @return a new chain with the custom step appended
     * @see #map(UnaryOperator)
     * @see #apply(UnaryOperator)
     * @since 1.0.0
     */
    public TextChain then(UnaryOperator<String> decorator) {
        if (decorator == null) return this;
        return new TextChain(() -> decorator.apply(source.get()));
    }

    /**
     * Alias of {@link #then(UnaryOperator)} — see that method for full
     * documentation and examples.
     *
     * @param f the function to apply
     * @return a new chain with the custom step appended
     * @see #then(UnaryOperator)
     * @since 1.0.0
     */
    public TextChain map(UnaryOperator<String> f) { return then(f); }

    /**
     * Alias of {@link #then(UnaryOperator)} — see that method for full
     * documentation and examples.
     *
     * @param f the function to apply
     * @return a new chain with the custom step appended
     * @see #then(UnaryOperator)
     * @since 1.0.0
     */
    public TextChain apply(UnaryOperator<String> f) { return then(f); }

    // -------------------------------------------------------------- terminal

    /**
     * Evaluates the chain and returns the final string.
     *
     * <p>Calling this method more than once on the same chain instance
     * re-evaluates the whole chain. If the original source was a
     * supplier that returns different values on each call, the results
     * will differ between calls.</p>
     *
     * @return the transformed string, or {@code null} if the original
     * source was {@code null} or any transform propagated {@code null}
     * @see #value()
     * @see #result()
     * @see #toString()
     * @since 1.0.0
     */
    public String get() { return source.get(); }

    /**
     * Alias of {@link #get()} — see that method for full documentation.
     *
     * @return the transformed string
     * @see #get()
     * @since 1.0.0
     */
    public String value() { return source.get(); }

    /**
     * Alias of {@link #get()} — see that method for full documentation.
     *
     * @return the transformed string
     * @see #get()
     * @since 1.0.0
     */
    public String result() { return source.get(); }

    /**
     * Alias of {@link #get()} — see that method for full documentation.
     * Provided so a chain can be used directly in string concatenation,
     * logging, and {@code String.valueOf(...)} calls.
     *
     * @return the transformed string, or {@code null} if the chain
     * evaluates to {@code null}
     * @see #get()
     * @since 1.0.0
     */
    @Override
    public String toString() { return source.get(); }
}
