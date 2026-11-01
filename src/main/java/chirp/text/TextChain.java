package chirp.text;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Fluent text transformer — a live demonstration of the <b>Decorator design
 * pattern</b>: every method returns a new {@code TextChain} wrapping the
 * previous one, so transformations compose without mutating state.
 *
 * <pre>{@code
 * String result = TextChain.of("hello world")
 *     .capitalize()
 *     .reverse()
 *     .append("!")
 *     .get();
 * // → "Dlrow olleh!"
 * }</pre>
 *
 * <p>Every method is null-safe; null inputs propagate through unchanged.
 * Chains are immutable and thread-safe. Case transformations use
 * {@link Locale#ROOT} so results are identical across locales.</p>
 */
public final class TextChain {

    private final java.util.function.Supplier<String> source;

    private TextChain(java.util.function.Supplier<String> source) {
        this.source = source;
    }

    // -------------------------------------------------------------- factories

    public static TextChain of(String text) { return new TextChain(() -> text); }
    public static TextChain of(java.util.function.Supplier<String> s) { return new TextChain(s); }
    public static TextChain from(String text) { return of(text); }
    public static TextChain wrap(String text) { return of(text); }

    // -------------------------------------------------------------- transforms

    public TextChain capitalize() { return new TextChain(() -> Text.capitalize(source.get())); }
    public TextChain titleCase() { return new TextChain(() -> Text.titleCase(source.get())); }
    public TextChain toUpperCase() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.toUpperCase(Locale.ROOT);
        });
    }
    public TextChain upper() { return toUpperCase(); }

    public TextChain toLowerCase() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.toLowerCase(Locale.ROOT);
        });
    }
    public TextChain lower() { return toLowerCase(); }

    public TextChain trim() {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v.trim();
        });
    }

    public TextChain reverse() { return new TextChain(() -> Text.reverse(source.get())); }
    public TextChain reverseText() { return reverse(); }

    public TextChain removeSpaces() { return new TextChain(() -> Text.removeSpaces(source.get())); }

    public TextChain padLeft(int w) { return new TextChain(() -> Text.padLeft(source.get(), w)); }
    public TextChain padRight(int w) { return new TextChain(() -> Text.padRight(source.get(), w)); }
    public TextChain center(int w) { return new TextChain(() -> Text.center(source.get(), w)); }

    public TextChain truncate(int max) { return new TextChain(() -> Text.truncate(source.get(), max)); }

    public TextChain append(String suffix) {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : v + suffix;
        });
    }
    public TextChain prepend(String prefix) {
        return new TextChain(() -> {
            String v = source.get();
            return v == null ? null : prefix + v;
        });
    }
    public TextChain surround(String prefix, String suffix) {
        return new TextChain(() -> {
            String v = source.get();
            if (v == null) return null;
            return prefix + v + suffix;
        });
    }

    /** Replaces all occurrences of {@code target} with {@code replacement}. */
    public TextChain replace(String target, String replacement) {
        return new TextChain(() -> {
            String v = source.get();
            if (v == null || target == null) return v;
            return v.replace(target, replacement == null ? "" : replacement);
        });
    }

    /** Custom decorator step — the raw Decorator-pattern demonstration. */
    public TextChain then(UnaryOperator<String> decorator) {
        if (decorator == null) return this;
        return new TextChain(() -> decorator.apply(source.get()));
    }
    public TextChain map(UnaryOperator<String> f) { return then(f); }
    public TextChain apply(UnaryOperator<String> f) { return then(f); }

    // -------------------------------------------------------------- terminal

    public String get() { return source.get(); }
    public String value() { return source.get(); }
    public String result() { return source.get(); }
    
    @Override
    public String toString() { return source.get(); }
}
