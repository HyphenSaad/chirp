package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Trace;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent single-character reader. Uses {@link Input#readRaw(String)} internally.
 *
 * <p><b>Strict mode:</b> a single character has no range, length, blank, or
 * format dimension to be strict about — so this reader never emits strict
 * warnings. {@link #lenient()} exists purely for API symmetry with the
 * other readers and is a documented no-op.</p>
 */
public final class CharReader {

    private String label = "Enter a single character";
    private Set<Character> allowlist;
    private String helpText;
    private Predicate<Character> customValidator;
    private String customError;

    private CharReader() {}
    private CharReader(String label) { if (label != null) this.label = label; }

    public static CharReader create() { return new CharReader(); }
    public static CharReader of() { return new CharReader(); }
    public static CharReader ask() { return new CharReader(); }
    public static CharReader prompt() { return new CharReader(); }
    public static CharReader prompt(String label) { return new CharReader(label); }

    public CharReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public CharReader message(String label) { return label(label); }
    public CharReader withLabel(String label) { return label(label); }

    public CharReader oneOf(char... chars) {
        this.allowlist = new HashSet<>();
        for (char c : chars) this.allowlist.add(c);
        Trace.debug("reader", "configure: oneOf({} chars)", chars.length);
        return this;
    }

    public CharReader help(String text) { this.helpText = text; return this; }

    public CharReader custom(Predicate<Character> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    /**
     * No-op — kept for fluent-API symmetry with the other readers.
     * CharReader never emits strict warnings, so there is nothing to relax.
     */
    public CharReader lenient() { return this; }
    public CharReader noStrict() { return lenient(); }

    public char read() {
        Trace.enter("reader", "CharReader.read", "label=" + label);
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.length() != 1) {
                Trace.validation("reader", "single char", false, "length=" + raw.length());
                Messenger.error("Please enter exactly one character.");
                continue;
            }
            char value = raw.charAt(0);
            if (allowlist != null && !allowlist.contains(value)) {
                Trace.validation("reader", "allowlist", false, "char '" + value + "' not allowed");
                Messenger.error("Character must be one of: " + allowlist);
                continue;
            }
            if (customValidator != null && !customValidator.test(value)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid character.");
                continue;
            }
            Trace.exit("reader", "CharReader.read", value);
            return value;
        }
    }

    public char get() { return read(); }
    public char value() { return read(); }
    public char parse() { return read(); }
}
