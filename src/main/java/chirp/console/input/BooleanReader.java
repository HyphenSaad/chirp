package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Trace;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent boolean reader — a configurable y/n prompt.
 *
 * <p>Created via {@link Input#readBoolean()}. For the immediate form
 * (one call, no configuration), use
 * {@link Input#readYesNo(String)} instead.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>A custom label — {@link #label(String)}</li>
 *   <li>A default value for empty input —
 *       {@link #defaultValue(boolean)}</li>
 *   <li>A custom validator with its own error message —
 *       {@link #custom(Predicate, String)}</li>
 *   <li>Help text shown before the prompt —
 *       {@link #help(String)}</li>
 *   <li>Strict-mode opt-out — {@link #lenient()} (no-op for this
 *       reader, kept for API symmetry)</li>
 * </ul>
 *
 * <h2>What counts as yes / no</h2>
 * <p>Yes: {@code y, yes, true, 1, yeah, yep, ok}. No:
 * {@code n, no, false, 0, nah, nope}. Case-insensitive
 * ({@code Locale.ROOT} — {@code "Y"} works on every locale).</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * boolean send = Input.readBoolean()
 *     .label("Send the report?")
 *     .defaultValue(false)
 *     .help("Pressing Enter without an answer will default to No")
 *     .read();
 * }</pre>
 *
 * <h2>Strict mode</h2>
 * <p>Boolean readers never emit strict warnings — a y/n answer has no
 * range, length, blank, or format dimension to be strict about.
 * {@link #lenient()} exists for API symmetry only.</p>
 *
 * <h2>Related</h2>
 * <p>For the immediate form, see {@link Input#readYesNo(String)}. For
 * the general input facade, see {@link Input}.</p>
 *
 * @see Input#readBoolean()
 * @see Input#readYesNo(String)
 * @see Input
 * @since 1.0.0
 */
public final class BooleanReader {

    private String label = "Continue";
    private Boolean defaultValue;
    private String helpText;
    private Predicate<Boolean> customValidator;
    private String customError;

    private static final Set<String> TRUTHY = Set.of("y", "yes", "true", "1", "yeah", "yep", "ok");
    private static final Set<String> FALSY = Set.of("n", "no", "false", "0", "nah", "nope");

    private BooleanReader() {}
    private BooleanReader(String label) { if (label != null) this.label = label; }

    // ============================================================== factories

    /**
     * Creates a boolean reader with the default label "Continue".
     *
     * @return a new reader
     * @see #of()
     * @see #ask()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static BooleanReader create() { return new BooleanReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static BooleanReader of() { return new BooleanReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static BooleanReader ask() { return new BooleanReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static BooleanReader prompt() { return new BooleanReader(); }

    /**
     * Creates a boolean reader with a custom label.
     *
     * @param label the prompt label (null → "Continue")
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static BooleanReader prompt(String label) { return new BooleanReader(label); }

    // ============================================================== configuration

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @see #message(String)
     * @see #question(String)
     * @see #withLabel(String)
     * @since 1.0.0
     */
    public BooleanReader label(String label) {
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
    public BooleanReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public BooleanReader question(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public BooleanReader withLabel(String label) { return label(label); }

    /**
     * Sets a default value used when the user presses Enter without
     * typing. When no default is set, empty input is rejected with an
     * error message.
     *
     * @param value the default value
     * @return this reader, for chaining
     * @see #defaulting(boolean)
     * @see #orElse(boolean)
     * @since 1.0.0
     */
    public BooleanReader defaultValue(boolean value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Alias of {@link #defaultValue(boolean)} — see that method for
     * full documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(boolean)
     * @since 1.0.0
     */
    public BooleanReader defaulting(boolean value) { return defaultValue(value); }

    /**
     * Alias of {@link #defaultValue(boolean)} — see that method for
     * full documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(boolean)
     * @since 1.0.0
     */
    public BooleanReader orElse(boolean value) { return defaultValue(value); }

    /**
     * Sets help text shown to the user before the prompt. Use it for
     * hints about what the answer means.
     *
     * @param text the help text (null → no help)
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public BooleanReader help(String text) { this.helpText = text; return this; }

    /**
     * Attaches a custom validator that runs on the parsed boolean value.
     * The error message is shown when the predicate returns
     * {@code false}. Useful for confirming a specific answer — e.g.
     * "you must answer yes to continue".
     *
     * @param validator the validator (null → no custom check)
     * @param error the error message shown on failure
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public BooleanReader custom(Predicate<Boolean> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    /**
     * No-op — kept for fluent-API symmetry with the other readers.
     * BooleanReader never emits strict warnings, so there is nothing to
     * relax.
     *
     * @return this reader
     * @see #noStrict()
     * @since 1.0.0
     */
    public BooleanReader lenient() { return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public BooleanReader noStrict() { return lenient(); }

    // ============================================================== terminal

    /**
     * Reads a boolean value. Loops until the input parses as yes / no
     * (or a default is provided and the input is empty). Requires
     * {@code Chirp.start()}.
     *
     * @return the parsed boolean
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @see #parse()
     * @since 1.0.0
     */
    public boolean read() {
        Trace.enter("reader", "BooleanReader.read", "label=" + label);
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label + " (y/n)").toLowerCase(Locale.ROOT);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "BooleanReader.read", defaultValue);
                return defaultValue;
            }

            Boolean value = null;
            if (TRUTHY.contains(raw)) value = Boolean.TRUE;
            else if (FALSY.contains(raw)) value = Boolean.FALSE;

            if (value == null) {
                Trace.validation("reader", "boolean parse", false, "not y/yes/true/1 or n/no/false/0");
                Messenger.error("Please answer y or n.");
                continue;
            }
            if (customValidator != null && !customValidator.test(value)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid answer.");
                continue;
            }
            Trace.exit("reader", "BooleanReader.read", value);
            return value;
        }
    }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed boolean
     * @see #read()
     * @since 1.0.0
     */
    public boolean get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed boolean
     * @see #read()
     * @since 1.0.0
     */
    public boolean value() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed boolean
     * @see #read()
     * @since 1.0.0
     */
    public boolean parse() { return read(); }
}
