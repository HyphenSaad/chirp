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
 * Fluent double reader. Emits trace events on the {@code reader} category.
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

    public static DoubleReader create() { return new DoubleReader(); }
    public static DoubleReader of() { return new DoubleReader(); }
    public static DoubleReader ask() { return new DoubleReader(); }
    public static DoubleReader prompt() { return new DoubleReader(); }
    public static DoubleReader prompt(String label) { return new DoubleReader(label); }

    public DoubleReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public DoubleReader message(String label) { return label(label); }
    public DoubleReader question(String label) { return label(label); }
    public DoubleReader withLabel(String label) { return label(label); }

    public DoubleReader min(double value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }
    public DoubleReader atLeast(double value) { return min(value); }
    public DoubleReader lowest(double value) { return min(value); }

    public DoubleReader max(double value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }
    public DoubleReader atMost(double value) { return max(value); }
    public DoubleReader highest(double value) { return max(value); }

    public DoubleReader between(double lo, double hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }
    public DoubleReader range(double lo, double hi) { return between(lo, hi); }
    public DoubleReader inRange(double lo, double hi) { return between(lo, hi); }

    public DoubleReader positive() {
        applied.add(ValidationCategory.RANGE);
        this.min = Double.MIN_VALUE;
        Trace.debug("reader", "configure: positive");
        return this;
    }
    public DoubleReader negative() {
        applied.add(ValidationCategory.RANGE);
        this.max = -Double.MIN_VALUE;
        Trace.debug("reader", "configure: negative");
        return this;
    }
    public DoubleReader nonNegative() {
        applied.add(ValidationCategory.RANGE);
        this.min = 0.0;
        Trace.debug("reader", "configure: nonNegative");
        return this;
    }
    public DoubleReader nonPositive() {
        applied.add(ValidationCategory.RANGE);
        this.max = 0.0;
        Trace.debug("reader", "configure: nonPositive");
        return this;
    }

    public DoubleReader decimals(int places) {
        this.decimals = places;
        Trace.debug("reader", "configure: decimals({})", places);
        return this;
    }
    public DoubleReader places(int places) { return decimals(places); }

    public DoubleReader defaultValue(double value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public DoubleReader defaulting(double value) { return defaultValue(value); }
    public DoubleReader orElse(double value) { return defaultValue(value); }

    public DoubleReader help(String text) { this.helpText = text; return this; }
    public DoubleReader hint(String text) { return help(text); }

    public DoubleReader custom(Predicate<Double> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }
    public DoubleReader validate(Predicate<Double> validator, String error) { return custom(validator, error); }

    public DoubleReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }
    public DoubleReader noStrict() { return lenient(); }
    public DoubleReader skipRangeWarning() {
        skipped.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: skipRangeWarning");
        return this;
    }

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

    public double get() { return read(); }
    public double value() { return read(); }
    public double parse() { return read(); }
}
