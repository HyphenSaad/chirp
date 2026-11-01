package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent integer reader — the most-used reader builder, with range
 * validation, sign shortcuts, even/odd checks, and custom predicates.
 *
 * <p>Created via {@link Input#readInt()}. For the immediate form, use
 * {@link Input#readInt(String)} or
 * {@link Input#readInt(String, int, int)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>Range validation — {@link #min(int)}, {@link #max(int)},
 *       {@link #between(int, int)}</li>
 *   <li>Sign shortcuts — {@link #positive()}, {@link #negative()},
 *       {@link #nonNegative()}, {@link #nonPositive()},
 *       {@link #natural()}</li>
 *   <li>Parity shortcuts — {@link #even()}, {@link #odd()}</li>
 *   <li>Default value — {@link #defaultValue(int)}</li>
 *   <li>Custom validator — {@link #custom(Predicate, String)},
 *       {@link #validate(Predicate, String)},
 *       {@link #when(Predicate, String)}</li>
 *   <li>Help text — {@link #help(String)}</li>
 *   <li>Strict-mode controls — {@link #lenient()},
 *       {@link #skipRangeWarning()}</li>
 * </ul>
 *
 * <h2>Example — age with range check</h2>
 * <pre>{@code
 * int age = Input.readInt()
 *     .label("Age")
 *     .between(1, 130)
 *     .help("Enter your age in years")
 *     .read();
 * }</pre>
 *
 * <h2>Example — quantity with parity check</h2>
 * <pre>{@code
 * int qty = Input.readInt()
 *     .label("Quantity")
 *     .positive()
 *     .even()
 *     .read();
 * }</pre>
 *
 * <h2>Strict mode</h2>
 * <p>When strict mode is on, the reader emits a {@code [STRICT]} hint if
 * no range check is applied. Any range or sign shortcut silences it.</p>
 *
 * <h2>Related</h2>
 * <p>For decimal numbers, see {@link DoubleReader}. For the immediate
 * form, see {@link Input#readInt(String)}.</p>
 *
 * @see Input#readInt()
 * @see Input#readInt(String)
 * @see DoubleReader
 * @since 1.0.0
 */
public final class IntReader {

    private String label = "Enter a number";
    private Integer min;
    private Integer max;
    private Integer defaultValue;
    private String helpText;
    private Predicate<Integer> customValidator;
    private String customError;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private IntReader() {}
    private IntReader(String label) { if (label != null) this.label = label; }

    // ============================================================== factories

    /**
     * Creates an int reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #ask()
     * @see #prompt()
     * @see #prompt(String)
     * @see #input()
     * @since 1.0.0
     */
    public static IntReader create() { return new IntReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static IntReader of() { return new IntReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static IntReader ask() { return new IntReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static IntReader prompt() { return new IntReader(); }

    /**
     * Creates an int reader with a custom label.
     *
     * @param label the prompt label
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static IntReader prompt(String label) { return new IntReader(label); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static IntReader input() { return new IntReader(); }

    // --------------------------------------------------------- configuration

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @see #message(String)
     * @see #question(String)
     * @see #text(String)
     * @see #withLabel(String)
     * @since 1.0.0
     */
    public IntReader label(String label) {
        if (label != null) this.label = label;
        Trace.debug("reader", "configure: label({})", label);
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
    public IntReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public IntReader question(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public IntReader text(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public IntReader withLabel(String label) { return label(label); }

    /**
     * Sets the minimum accepted value (inclusive).
     *
     * @param value the minimum value
     * @return this reader, for chaining
     * @see #atLeast(int)
     * @see #lowest(int)
     * @see #greaterThanOrEqual(int)
     * @see #max(int)
     * @since 1.0.0
     */
    public IntReader min(int value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }

    /**
     * Alias of {@link #min(int)} — see that method for full documentation.
     *
     * @param value the minimum value
     * @return this reader
     * @see #min(int)
     * @since 1.0.0
     */
    public IntReader atLeast(int value) { return min(value); }

    /**
     * Alias of {@link #min(int)} — see that method for full documentation.
     *
     * @param value the minimum value
     * @return this reader
     * @see #min(int)
     * @since 1.0.0
     */
    public IntReader lowest(int value) { return min(value); }

    /**
     * Alias of {@link #min(int)} — see that method for full documentation.
     *
     * @param value the minimum value
     * @return this reader
     * @see #min(int)
     * @since 1.0.0
     */
    public IntReader greaterThanOrEqual(int value) { return min(value); }

    /**
     * Sets the maximum accepted value (inclusive).
     *
     * @param value the maximum value
     * @return this reader, for chaining
     * @see #atMost(int)
     * @see #highest(int)
     * @see #lessThanOrEqual(int)
     * @see #min(int)
     * @since 1.0.0
     */
    public IntReader max(int value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }

    /**
     * Alias of {@link #max(int)} — see that method for full documentation.
     *
     * @param value the maximum value
     * @return this reader
     * @see #max(int)
     * @since 1.0.0
     */
    public IntReader atMost(int value) { return max(value); }

    /**
     * Alias of {@link #max(int)} — see that method for full documentation.
     *
     * @param value the maximum value
     * @return this reader
     * @see #max(int)
     * @since 1.0.0
     */
    public IntReader highest(int value) { return max(value); }

    /**
     * Alias of {@link #max(int)} — see that method for full documentation.
     *
     * @param value the maximum value
     * @return this reader
     * @see #max(int)
     * @since 1.0.0
     */
    public IntReader lessThanOrEqual(int value) { return max(value); }

    /**
     * Sets both minimum and maximum in one call (inclusive on both
     * ends).
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader, for chaining
     * @see #range(int, int)
     * @see #inRange(int, int)
     * @see #from(int, int)
     * @since 1.0.0
     */
    public IntReader between(int lo, int hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    /**
     * Alias of {@link #between(int, int)} — see that method for full
     * documentation.
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader
     * @see #between(int, int)
     * @since 1.0.0
     */
    public IntReader range(int lo, int hi) { return between(lo, hi); }

    /**
     * Alias of {@link #between(int, int)} — see that method for full
     * documentation.
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader
     * @see #between(int, int)
     * @since 1.0.0
     */
    public IntReader inRange(int lo, int hi) { return between(lo, hi); }

    /**
     * Alias of {@link #between(int, int)} — see that method for full
     * documentation.
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader
     * @see #between(int, int)
     * @since 1.0.0
     */
    public IntReader from(int lo, int hi) { return between(lo, hi); }

    /**
     * Requires a strictly positive value ({@code >= 1}).
     *
     * @return this reader, for chaining
     * @see #nonNegative()
     * @see #natural()
     * @since 1.0.0
     */
    public IntReader positive() {
        applied.add(ValidationCategory.RANGE);
        this.min = 1;
        Trace.debug("reader", "configure: positive");
        return this;
    }

    /**
     * Requires a strictly negative value ({@code <= -1}).
     *
     * @return this reader, for chaining
     * @see #nonPositive()
     * @since 1.0.0
     */
    public IntReader negative() {
        applied.add(ValidationCategory.RANGE);
        this.max = -1;
        Trace.debug("reader", "configure: negative");
        return this;
    }

    /**
     * Requires a non-negative value ({@code >= 0}).
     *
     * @return this reader, for chaining
     * @see #positive()
     * @since 1.0.0
     */
    public IntReader nonNegative() {
        applied.add(ValidationCategory.RANGE);
        this.min = 0;
        Trace.debug("reader", "configure: nonNegative");
        return this;
    }

    /**
     * Requires a non-positive value ({@code <= 0}).
     *
     * @return this reader, for chaining
     * @see #negative()
     * @since 1.0.0
     */
    public IntReader nonPositive() {
        applied.add(ValidationCategory.RANGE);
        this.max = 0;
        Trace.debug("reader", "configure: nonPositive");
        return this;
    }

    /**
     * Alias of {@link #positive()} — "natural numbers" is a common name
     * for strictly positive integers.
     *
     * @return this reader
     * @see #positive()
     * @since 1.0.0
     */
    public IntReader natural() { return positive(); }

    /**
     * Requires an even value.
     *
     * @return this reader, for chaining
     * @see #odd()
     * @since 1.0.0
     */
    public IntReader even() {
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: even");
        return custom(n -> n % 2 == 0, "Must be even");
    }

    /**
     * Requires an odd value.
     *
     * @return this reader, for chaining
     * @see #even()
     * @since 1.0.0
     */
    public IntReader odd() {
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: odd");
        return custom(n -> n % 2 != 0, "Must be odd");
    }

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default value
     * @return this reader, for chaining
     * @see #defaulting(int)
     * @see #orElse(int)
     * @see #fallback(int)
     * @since 1.0.0
     */
    public IntReader defaultValue(int value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Alias of {@link #defaultValue(int)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(int)
     * @since 1.0.0
     */
    public IntReader defaulting(int value) { return defaultValue(value); }

    /**
     * Alias of {@link #defaultValue(int)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(int)
     * @since 1.0.0
     */
    public IntReader orElse(int value) { return defaultValue(value); }

    /**
     * Alias of {@link #defaultValue(int)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(int)
     * @since 1.0.0
     */
    public IntReader fallback(int value) { return defaultValue(value); }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text
     * @return this reader
     * @see #hint(String)
     * @see #note(String)
     * @since 1.0.0
     */
    public IntReader help(String text) { this.helpText = text; return this; }

    /**
     * Alias of {@link #help(String)} — see that method for full
     * documentation.
     *
     * @param text the help text
     * @return this reader
     * @see #help(String)
     * @since 1.0.0
     */
    public IntReader hint(String text) { return help(text); }

    /**
     * Alias of {@link #help(String)} — see that method for full
     * documentation.
     *
     * @param text the help text
     * @return this reader
     * @see #help(String)
     * @since 1.0.0
     */
    public IntReader note(String text) { return help(text); }

    /**
     * Attaches a custom validator with its own error message.
     *
     * @param validator the validator (null → no custom check)
     * @param errorMessage the error message
     * @return this reader, for chaining
     * @see #validate(Predicate, String)
     * @see #when(Predicate, String)
     * @since 1.0.0
     */
    public IntReader custom(Predicate<Integer> validator, String errorMessage) {
        this.customValidator = validator;
        this.customError = errorMessage;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    /**
     * Alias of {@link #custom(Predicate, String)} — see that method for
     * full documentation.
     *
     * @param validator the validator
     * @param errorMessage the error message
     * @return this reader
     * @see #custom(Predicate, String)
     * @since 1.0.0
     */
    public IntReader validate(Predicate<Integer> validator, String errorMessage) {
        return custom(validator, errorMessage);
    }

    /**
     * Alias of {@link #custom(Predicate, String)} — see that method for
     * full documentation.
     *
     * @param validator the validator
     * @param errorMessage the error message
     * @return this reader
     * @see #custom(Predicate, String)
     * @since 1.0.0
     */
    public IntReader when(Predicate<Integer> validator, String errorMessage) {
        return custom(validator, errorMessage);
    }

    /**
     * Silences all strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipRangeWarning()
     * @since 1.0.0
     */
    public IntReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public IntReader noStrict() { return lenient(); }

    /**
     * Silences only the range warning.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public IntReader skipRangeWarning() {
        skipped.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: skipRangeWarning");
        return this;
    }

    // ------------------------------------------------------------ terminal

    /**
     * Reads an int. Loops until the input parses as an {@code int} and
     * passes the range and custom checks (if any). Requires
     * {@code Chirp.start()}.
     *
     * @return the parsed int
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @see #parse()
     * @since 1.0.0
     */
    public int read() {
        Trace.enter("reader", "IntReader.read", "label=" + label, "min=" + min, "max=" + max);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "IntReader.read", defaultValue);
                return defaultValue;
            }

            int value;
            try {
                value = Integer.parseInt(raw);
                Trace.value("reader", "parsed int", value);
            } catch (NumberFormatException e) {
                Trace.validation("reader", "int parse", false, "not a number");
                Messenger.error("Please enter a whole number.");
                continue;
            }

            if (min != null) {
                boolean pass = value >= min;
                Trace.validation("reader", "RANGE min(" + min + ")", pass,
                        pass ? null : "value " + value + " below min");
                if (!pass) {
                    Messenger.error("Must be at least " + min + ".");
                    continue;
                }
            }
            if (max != null) {
                boolean pass = value <= max;
                Trace.validation("reader", "RANGE max(" + max + ")", pass,
                        pass ? null : "value " + value + " above max");
                if (!pass) {
                    Messenger.error("Must be at most " + max + ".");
                    continue;
                }
            }
            if (customValidator != null && !customValidator.test(value)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid value.");
                continue;
            }
            Trace.validation("reader", "all checks", true, null);
            Trace.exit("reader", "IntReader.read", value);
            return value;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) {
            Trace.debug("reader", "strict skipped (lenient)");
            return;
        }
        boolean appliedOrSkipped = applied.contains(ValidationCategory.RANGE)
                || skipped.contains(ValidationCategory.RANGE);
        Trace.decision("reader", "RANGE applied or skipped", appliedOrSkipped);
        if (!appliedOrSkipped) {
            Strict.emit("int", label, ValidationCategory.RANGE);
        }
    }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed int
     * @see #read()
     * @since 1.0.0
     */
    public int get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed int
     * @see #read()
     * @since 1.0.0
     */
    public int value() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed int
     * @see #read()
     * @since 1.0.0
     */
    public int parse() { return read(); }
}
