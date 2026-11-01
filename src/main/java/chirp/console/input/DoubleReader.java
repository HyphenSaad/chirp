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
 * Fluent double reader — a configurable decimal-number prompt with range
 * validation, decimal precision, and custom predicates.
 *
 * <p>Created via {@link Input#readDouble()}. For the immediate form, use
 * {@link Input#readDouble(String)} or
 * {@link Input#readDouble(String, double, double)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>Range validation — {@link #min(double)}, {@link #max(double)},
 *       {@link #between(double, double)}</li>
 *   <li>Sign shortcuts — {@link #positive()}, {@link #negative()},
 *       {@link #nonNegative()}, {@link #nonPositive()}</li>
 *   <li>Decimal precision — {@link #decimals(int)} rounds the parsed
 *       value to N places</li>
 *   <li>Default value — {@link #defaultValue(double)}</li>
 *   <li>Custom validator — {@link #custom(Predicate, String)}</li>
 *   <li>Help text — {@link #help(String)}</li>
 *   <li>Strict-mode controls — {@link #lenient()},
 *       {@link #skipRangeWarning()}</li>
 * </ul>
 *
 * <h2>Sign shortcuts</h2>
 * <p>The four sign shortcuts set the appropriate min or max with a
 * tiny epsilon — "positive" means strictly {@code > 0}, "non-negative"
 * means {@code >= 0}. This matches what students expect from the words
 * rather than what a naive {@code min(0)} check would produce.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * double price = Input.readDouble()
 *     .label("Price")
 *     .positive()
 *     .decimals(2)
 *     .read();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For integer input, see {@link IntReader}. For the immediate form,
 * see {@link Input#readDouble(String)}.</p>
 *
 * @see Input#readDouble()
 * @see Input#readDouble(String)
 * @see IntReader
 * @since 1.0.0
 */
public final class DoubleReader {

    private String label = "Enter a decimal number";
    private Double min;
    private Double max;
    private Double defaultValue;
    private String helpText;
    private int decimals = -1;
    private Predicate<Double> customValidator;
    private String customError;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private DoubleReader() {}
    private DoubleReader(String label) { if (label != null) this.label = label; }

    // ============================================================== factories

    /**
     * Creates a double reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #ask()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static DoubleReader create() { return new DoubleReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DoubleReader of() { return new DoubleReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DoubleReader ask() { return new DoubleReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DoubleReader prompt() { return new DoubleReader(); }

    /**
     * Creates a double reader with a custom label.
     *
     * @param label the prompt label
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DoubleReader prompt(String label) { return new DoubleReader(label); }

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
    public DoubleReader label(String label) {
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
    public DoubleReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public DoubleReader question(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public DoubleReader withLabel(String label) { return label(label); }

    /**
     * Sets the minimum accepted value (inclusive).
     *
     * @param value the minimum value
     * @return this reader, for chaining
     * @see #max(double)
     * @see #between(double, double)
     * @since 1.0.0
     */
    public DoubleReader min(double value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }

    /**
     * Alias of {@link #min(double)} — see that method for full
     * documentation.
     *
     * @param value the minimum value
     * @return this reader
     * @see #min(double)
     * @since 1.0.0
     */
    public DoubleReader atLeast(double value) { return min(value); }

    /**
     * Alias of {@link #min(double)} — see that method for full
     * documentation.
     *
     * @param value the minimum value
     * @return this reader
     * @see #min(double)
     * @since 1.0.0
     */
    public DoubleReader lowest(double value) { return min(value); }

    /**
     * Sets the maximum accepted value (inclusive).
     *
     * @param value the maximum value
     * @return this reader, for chaining
     * @see #min(double)
     * @since 1.0.0
     */
    public DoubleReader max(double value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }

    /**
     * Alias of {@link #max(double)} — see that method for full
     * documentation.
     *
     * @param value the maximum value
     * @return this reader
     * @see #max(double)
     * @since 1.0.0
     */
    public DoubleReader atMost(double value) { return max(value); }

    /**
     * Alias of {@link #max(double)} — see that method for full
     * documentation.
     *
     * @param value the maximum value
     * @return this reader
     * @see #max(double)
     * @since 1.0.0
     */
    public DoubleReader highest(double value) { return max(value); }

    /**
     * Sets both minimum and maximum in one call (inclusive on both
     * ends).
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader, for chaining
     * @see #range(double, double)
     * @see #inRange(double, double)
     * @since 1.0.0
     */
    public DoubleReader between(double lo, double hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    /**
     * Alias of {@link #between(double, double)} — see that method for
     * full documentation.
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader
     * @see #between(double, double)
     * @since 1.0.0
     */
    public DoubleReader range(double lo, double hi) { return between(lo, hi); }

    /**
     * Alias of {@link #between(double, double)} — see that method for
     * full documentation.
     *
     * @param lo the minimum value
     * @param hi the maximum value
     * @return this reader
     * @see #between(double, double)
     * @since 1.0.0
     */
    public DoubleReader inRange(double lo, double hi) { return between(lo, hi); }

    /**
     * Requires a strictly positive value ({@code > 0}). Uses a small
     * epsilon so the boundary is exclusive.
     *
     * @return this reader, for chaining
     * @see #negative()
     * @see #nonNegative()
     * @since 1.0.0
     */
    public DoubleReader positive() {
        applied.add(ValidationCategory.RANGE);
        this.min = Double.MIN_VALUE;
        Trace.debug("reader", "configure: positive");
        return this;
    }

    /**
     * Requires a strictly negative value ({@code < 0}).
     *
     * @return this reader, for chaining
     * @see #positive()
     * @since 1.0.0
     */
    public DoubleReader negative() {
        applied.add(ValidationCategory.RANGE);
        this.max = -Double.MIN_VALUE;
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
    public DoubleReader nonNegative() {
        applied.add(ValidationCategory.RANGE);
        this.min = 0.0;
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
    public DoubleReader nonPositive() {
        applied.add(ValidationCategory.RANGE);
        this.max = 0.0;
        Trace.debug("reader", "configure: nonPositive");
        return this;
    }

    /**
     * Rounds the parsed value to the given number of decimal places.
     *
     * @param places the number of decimal places (≥ 0)
     * @return this reader, for chaining
     * @see #places(int)
     * @since 1.0.0
     */
    public DoubleReader decimals(int places) {
        this.decimals = places;
        Trace.debug("reader", "configure: decimals({})", places);
        return this;
    }

    /**
     * Alias of {@link #decimals(int)} — see that method for full
     * documentation.
     *
     * @param places the number of decimal places
     * @return this reader
     * @see #decimals(int)
     * @since 1.0.0
     */
    public DoubleReader places(int places) { return decimals(places); }

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default value
     * @return this reader, for chaining
     * @see #defaulting(double)
     * @see #orElse(double)
     * @since 1.0.0
     */
    public DoubleReader defaultValue(double value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Alias of {@link #defaultValue(double)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(double)
     * @since 1.0.0
     */
    public DoubleReader defaulting(double value) { return defaultValue(value); }

    /**
     * Alias of {@link #defaultValue(double)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(double)
     * @since 1.0.0
     */
    public DoubleReader orElse(double value) { return defaultValue(value); }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text
     * @return this reader
     * @see #hint(String)
     * @since 1.0.0
     */
    public DoubleReader help(String text) { this.helpText = text; return this; }

    /**
     * Alias of {@link #help(String)} — see that method for full
     * documentation.
     *
     * @param text the help text
     * @return this reader
     * @see #help(String)
     * @since 1.0.0
     */
    public DoubleReader hint(String text) { return help(text); }

    /**
     * Attaches a custom validator with its own error message. Runs after
     * the range check (if any).
     *
     * @param validator the validator (null → no custom check)
     * @param error the error message
     * @return this reader, for chaining
     * @see #validate(Predicate, String)
     * @since 1.0.0
     */
    public DoubleReader custom(Predicate<Double> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    /**
     * Alias of {@link #custom(Predicate, String)} — see that method for
     * full documentation.
     *
     * @param validator the validator
     * @param error the error message
     * @return this reader
     * @see #custom(Predicate, String)
     * @since 1.0.0
     */
    public DoubleReader validate(Predicate<Double> validator, String error) { return custom(validator, error); }

    /**
     * Silences all strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipRangeWarning()
     * @since 1.0.0
     */
    public DoubleReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public DoubleReader noStrict() { return lenient(); }

    /**
     * Silences only the range warning.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public DoubleReader skipRangeWarning() {
        skipped.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: skipRangeWarning");
        return this;
    }

    // ============================================================== terminal

    /**
     * Reads a double. Loops until the input parses as a {@code double}
     * and passes the range and custom checks (if any). Requires
     * {@code Chirp.start()}.
     *
     * @return the parsed double
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @see #parse()
     * @since 1.0.0
     */
    public double read() {
        Trace.enter("reader", "DoubleReader.read", "label=" + label, "min=" + min, "max=" + max);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "DoubleReader.read", defaultValue);
                return defaultValue;
            }

            double value;
            try {
                value = Double.parseDouble(raw);
                Trace.value("reader", "parsed double", value);
            } catch (NumberFormatException e) {
                Trace.validation("reader", "double parse", false, "not a number");
                Messenger.error("Please enter a decimal number.");
                continue;
            }
            if (min != null && value < min) {
                Trace.validation("reader", "RANGE min(" + min + ")", false, "value " + value + " below min");
                Messenger.error("Must be at least " + min + ".");
                continue;
            }
            if (max != null && value > max) {
                Trace.validation("reader", "RANGE max(" + max + ")", false, "value " + value + " above max");
                Messenger.error("Must be at most " + max + ".");
                continue;
            }
            if (customValidator != null && !customValidator.test(value)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid value.");
                continue;
            }
            if (decimals >= 0) {
                double factor = Math.pow(10, decimals);
                double rounded = Math.round(value * factor) / factor;
                Trace.debug("reader", "rounding {} -> {}", value, rounded);
                value = rounded;
            }
            Trace.validation("reader", "all checks", true, null);
            Trace.exit("reader", "DoubleReader.read", value);
            return value;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) { Trace.debug("reader", "strict skipped (lenient)"); return; }
        boolean covered = applied.contains(ValidationCategory.RANGE) || skipped.contains(ValidationCategory.RANGE);
        Trace.decision("reader", "RANGE applied or skipped", covered);
        if (!covered) Strict.emit("double", label, ValidationCategory.RANGE);
    }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed double
     * @see #read()
     * @since 1.0.0
     */
    public double get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed double
     * @see #read()
     * @since 1.0.0
     */
    public double value() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed double
     * @see #read()
     * @since 1.0.0
     */
    public double parse() { return read(); }
}
