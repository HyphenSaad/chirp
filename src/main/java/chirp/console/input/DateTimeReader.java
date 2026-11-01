package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.datetime.DateTime;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fluent date-time reader. Emits trace events on the {@code reader} category.
 */
public final class DateTimeReader {

    private String label = "Enter a date and time";
    private LocalDateTime min;
    private LocalDateTime max;
    private LocalDateTime defaultValue;
    private String helpText;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private DateTimeReader() {}
    private DateTimeReader(String label) { if (label != null) this.label = label; }

    public static DateTimeReader create() { return new DateTimeReader(); }
    public static DateTimeReader of() { return new DateTimeReader(); }
    public static DateTimeReader prompt() { return new DateTimeReader(); }
    public static DateTimeReader prompt(String label) { return new DateTimeReader(label); }

    public DateTimeReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public DateTimeReader message(String label) { return label(label); }
    public DateTimeReader withLabel(String label) { return label(label); }

    public DateTimeReader min(LocalDateTime value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }
    public DateTimeReader after(LocalDateTime value) { return min(value); }
    public DateTimeReader atLeast(LocalDateTime value) { return min(value); }
    public DateTimeReader max(LocalDateTime value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }
    public DateTimeReader before(LocalDateTime value) { return max(value); }
    public DateTimeReader atMost(LocalDateTime value) { return max(value); }
    public DateTimeReader between(LocalDateTime lo, LocalDateTime hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }
    public DateTimeReader past() { return max(LocalDateTime.now()); }
    public DateTimeReader future() { return min(LocalDateTime.now().plusSeconds(1)); }

    public DateTimeReader defaultValue(LocalDateTime value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public DateTimeReader help(String text) { this.helpText = text; return this; }

    public DateTimeReader lenient() { this.lenient = true; return this; }
    public DateTimeReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    public LocalDateTime read() {
        Trace.enter("reader", "DateTimeReader.read", "label=" + label);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.exit("reader", "DateTimeReader.read", defaultValue);
                return defaultValue;
            }

            LocalDateTime dateTime = DateTime.parseDateTime(raw);
            if (dateTime == null) {
                Trace.validation("reader", "datetime parse", false, "unparseable");
                Messenger.error("Please enter a valid date and time (e.g. 2026-10-31 14:30).");
                continue;
            }
            Trace.value("reader", "parsed datetime", dateTime);

            if (min != null && dateTime.isBefore(min)) {
                Trace.validation("reader", "RANGE min(" + min + ")", false, "datetime before min");
                Messenger.error("Must be on or after " + DateTime.humanize(min) + ".");
                continue;
            }
            if (max != null && dateTime.isAfter(max)) {
                Trace.validation("reader", "RANGE max(" + max + ")", false, "datetime after max");
                Messenger.error("Must be on or before " + DateTime.humanize(max) + ".");
                continue;
            }
            Trace.exit("reader", "DateTimeReader.read", dateTime);
            return dateTime;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) return;
        boolean covered = applied.contains(ValidationCategory.RANGE) || skipped.contains(ValidationCategory.RANGE);
        Trace.decision("reader", "RANGE applied or skipped", covered);
        if (!covered) Strict.emit("datetime", label, ValidationCategory.RANGE);
    }

    public LocalDateTime get() { return read(); }
    public LocalDateTime value() { return read(); }
}
