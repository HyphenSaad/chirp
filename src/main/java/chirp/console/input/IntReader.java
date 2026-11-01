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
 * Fluent integer reader. Emits trace events on the {@code reader} category.
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

    public static IntReader create() { return new IntReader(); }
    public static IntReader of() { return new IntReader(); }
    public static IntReader ask() { return new IntReader(); }
    public static IntReader prompt() { return new IntReader(); }
    public static IntReader prompt(String label) { return new IntReader(label); }
    public static IntReader input() { return new IntReader(); }

    // --------------------------------------------------------- configuration

    public IntReader label(String label) {
        if (label != null) this.label = label;
        Trace.debug("reader", "configure: label({})", label);
        return this;
    }
    public IntReader message(String label) { return label(label); }
    public IntReader question(String label) { return label(label); }
    public IntReader text(String label) { return label(label); }
    public IntReader withLabel(String label) { return label(label); }

    public IntReader min(int value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }
    public IntReader atLeast(int value) { return min(value); }
    public IntReader lowest(int value) { return min(value); }
    public IntReader greaterThanOrEqual(int value) { return min(value); }

    public IntReader max(int value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }
    public IntReader atMost(int value) { return max(value); }
    public IntReader highest(int value) { return max(value); }
    public IntReader lessThanOrEqual(int value) { return max(value); }

    public IntReader between(int lo, int hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }
    public IntReader range(int lo, int hi) { return between(lo, hi); }
    public IntReader inRange(int lo, int hi) { return between(lo, hi); }
    public IntReader from(int lo, int hi) { return between(lo, hi); }

    public IntReader positive() {
        applied.add(ValidationCategory.RANGE);
        this.min = 1;
        Trace.debug("reader", "configure: positive");
        return this;
    }
    public IntReader negative() {
        applied.add(ValidationCategory.RANGE);
        this.max = -1;
        Trace.debug("reader", "configure: negative");
        return this;
    }
    public IntReader nonNegative() {
        applied.add(ValidationCategory.RANGE);
        this.min = 0;
        Trace.debug("reader", "configure: nonNegative");
        return this;
    }
    public IntReader nonPositive() {
        applied.add(ValidationCategory.RANGE);
        this.max = 0;
        Trace.debug("reader", "configure: nonPositive");
        return this;
    }
    public IntReader natural() { return positive(); }

    public IntReader even() {
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: even");
        return custom(n -> n % 2 == 0, "Must be even");
    }
    public IntReader odd() {
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: odd");
        return custom(n -> n % 2 != 0, "Must be odd");
    }

    public IntReader defaultValue(int value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public IntReader defaulting(int value) { return defaultValue(value); }
    public IntReader orElse(int value) { return defaultValue(value); }
    public IntReader fallback(int value) { return defaultValue(value); }

    public IntReader help(String text) { this.helpText = text; return this; }
    public IntReader hint(String text) { return help(text); }
    public IntReader note(String text) { return help(text); }

    public IntReader custom(Predicate<Integer> validator, String errorMessage) {
        this.customValidator = validator;
        this.customError = errorMessage;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }
    public IntReader validate(Predicate<Integer> validator, String errorMessage) {
        return custom(validator, errorMessage);
    }
    public IntReader when(Predicate<Integer> validator, String errorMessage) {
        return custom(validator, errorMessage);
    }

    public IntReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }
    public IntReader noStrict() { return lenient(); }
    public IntReader skipRangeWarning() {
        skipped.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: skipRangeWarning");
        return this;
    }

    // ------------------------------------------------------------ terminal

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

    public int get() { return read(); }
    public int value() { return read(); }
    public int parse() { return read(); }
}
