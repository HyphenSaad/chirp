package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Trace;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent single-character reader — accepts exactly one character, with
 * optional allow-list and custom validator.
 *
 * <p>Created via {@link Input#readCharacter()}. For the immediate form,
 * use {@link Input#readChar(String)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>An allow-list — {@link #oneOf(char...)} restricts the answer to
 *       a specific set (A/B/C/D, Y/N, etc.)</li>
 *   <li>A custom validator — {@link #custom(Predicate, String)}</li>
 *   <li>Help text — {@link #help(String)}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * char grade = Input.readCharacter()
 *     .label("Grade")
 *     .oneOf('A', 'B', 'C', 'D', 'F')
 *     .read();
 * }</pre>
 *
 * <h2>Strict mode</h2>
 * <p>Char readers never emit strict warnings — a single character has
 * no range, length, blank, or format dimension to be strict about.
 * {@link #lenient()} exists for API symmetry only.</p>
 *
 * <h2>Related</h2>
 * <p>For the immediate form, see {@link Input#readChar(String)}. For
 * reading multi-character text, see {@link StringReader}.</p>
 *
 * @see Input#readCharacter()
 * @see Input#readChar(String)
 * @see StringReader
 * @since 1.0.0
 */
public final class CharReader {

    private String label = "Enter a single character";
    private Set<Character> allowlist;
    private String helpText;
    private Predicate<Character> customValidator;
    private String customError;

    private CharReader() {}
    private CharReader(String label) { if (label != null) this.label = label; }

    // ============================================================== factories

    /**
     * Creates a char reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #ask()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static CharReader create() { return new CharReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static CharReader of() { return new CharReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static CharReader ask() { return new CharReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static CharReader prompt() { return new CharReader(); }

    /**
     * Creates a char reader with a custom label.
     *
     * @param label the prompt label (null → default)
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static CharReader prompt(String label) { return new CharReader(label); }

    // ============================================================== configuration

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @see #message(String)
     * @see #withLabel(String)
     * @since 1.0.0
     */
    public CharReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public CharReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public CharReader withLabel(String label) { return label(label); }

    /**
     * Restricts the accepted characters to the given allow-list. Input
     * outside the set is rejected with an error listing the allowed
     * characters.
     *
     * <pre>{@code
     * Input.readCharacter()
     *     .label("Continue?")
     *     .oneOf('Y', 'N', 'Q')
     *     .read();
     * }</pre>
     *
     * @param chars the allowed characters
     * @return this reader, for chaining
     * @see #custom(Predicate, String)
     * @since 1.0.0
     */
    public CharReader oneOf(char... chars) {
        this.allowlist = new HashSet<>();
        for (char c : chars) this.allowlist.add(c);
        Trace.debug("reader", "configure: oneOf({} chars)", chars.length);
        return this;
    }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text (null → no help)
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public CharReader help(String text) { this.helpText = text; return this; }

    /**
     * Attaches a custom validator with its own error message. Runs after
     * the allow-list check (if any).
     *
     * @param validator the validator (null → no custom check)
     * @param error the error message
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public CharReader custom(Predicate<Character> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    /**
     * No-op — kept for fluent-API symmetry with the other readers.
     * CharReader never emits strict warnings.
     *
     * @return this reader
     * @see #noStrict()
     * @since 1.0.0
     */
    public CharReader lenient() { return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public CharReader noStrict() { return lenient(); }

    // ============================================================== terminal

    /**
     * Reads one character. Loops until the input is exactly one
     * character and passes the allow-list and custom validator (if
     * set). Requires {@code Chirp.start()}.
     *
     * @return the parsed character
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @see #parse()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed character
     * @see #read()
     * @since 1.0.0
     */
    public char get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed character
     * @see #read()
     * @since 1.0.0
     */
    public char value() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed character
     * @see #read()
     * @since 1.0.0
     */
    public char parse() { return read(); }
}
