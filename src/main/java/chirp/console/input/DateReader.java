package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.datetime.DateTime;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fluent date reader. Emits trace events on the {@code reader} category.
 */
public final class DateReader {

    private String label = "Enter a date";
    private LocalDate min;
    private LocalDate max;
    private LocalDate defaultValue;
    private String helpText;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private DateReader() {}
    private DateReader(String label) { if (label != null) this.label = label; }

    public static DateReader create() { return new DateReader(); }
    public static DateReader of() { return new DateReader(); }
    public static DateReader prompt() { return new DateReader(); }
    public static DateReader prompt(String label) { return new DateReader(label); }

    public DateReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public DateReader message(String label) { return label(label); }
    public DateReader withLabel(String label) { return label(label); }

    public DateReader min(LocalDate value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }
    public DateReader after(LocalDate value) { return min(value); }
    public DateReader atLeast(LocalDate value) { return min(value); }
    public DateReader max(LocalDate value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }
    public DateReader before(LocalDate value) { return max(value); }
    public DateReader atMost(LocalDate value) { return max(value); }
    public DateReader between(LocalDate lo, LocalDate hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }
    public DateReader inRange(LocalDate lo, LocalDate hi) { return between(lo, hi); }
    public DateReader past() { return max(LocalDate.now()); }
    public DateReader future() { return min(LocalDate.now().plusDays(1)); }

    public DateReader defaultValue(LocalDate value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public DateReader defaulting(LocalDate value) { return defaultValue(value); }

    public DateReader help(String text) { this.helpText = text; return this; }
    public DateReader hint(String text) { return help(text); }

    public DateReader lenient() { this.lenient = true; return this; }
    public DateReader noStrict() { return lenient(); }
    public DateReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    public LocalDate read() {
        Trace.enter("reader", "DateReader.read", "label=" + label, "min=" + min, "max=" + max);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "DateReader.read", defaultValue);
                return defaultValue;
            }

            LocalDate date = DateTime.parseDate(raw);
            if (date == null) {
                Trace.validation("reader", "date parse", false, "unparseable");
                Messenger.error("Please enter a valid date (e.g. 2026-10-31 or 31/10/2026).");
                continue;
            }
            Trace.value("reader", "parsed date", date);

            if (min != null && date.isBefore(min)) {
                Trace.validation("reader", "RANGE min(" + min + ")", false, "date before min");
                Messenger.error("Date must be on or after " + DateTime.humanizeShort(min) + ".");
                continue;
            }
            if (max != null && date.isAfter(max)) {
                Trace.validation("reader", "RANGE max(" + max + ")", false, "date after max");
                Messenger.error("Date must be on or before " + DateTime.humanizeShort(max) + ".");
                continue;
            }
            Trace.exit("reader", "DateReader.read", date);
            return date;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) { Trace.debug("reader", "strict skipped (lenient)"); return; }
        boolean covered = applied.contains(ValidationCategory.RANGE) || skipped.contains(ValidationCategory.RANGE);
        Trace.decision("reader", "RANGE applied or skipped", covered);
        if (!covered) Strict.emit("date", label, ValidationCategory.RANGE);
    }

    public LocalDate get() { return read(); }
    public LocalDate value() { return read(); }
}
