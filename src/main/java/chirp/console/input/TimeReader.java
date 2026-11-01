package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.datetime.DateTime;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fluent time reader. Emits trace events on the {@code reader} category.
 */
public final class TimeReader {

    private String label = "Enter a time";
    private LocalTime min;
    private LocalTime max;
    private LocalTime defaultValue;
    private String helpText;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private TimeReader() {}
    private TimeReader(String label) { if (label != null) this.label = label; }

    public static TimeReader create() { return new TimeReader(); }
    public static TimeReader of() { return new TimeReader(); }
    public static TimeReader prompt() { return new TimeReader(); }
    public static TimeReader prompt(String label) { return new TimeReader(label); }

    public TimeReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public TimeReader message(String label) { return label(label); }
    public TimeReader withLabel(String label) { return label(label); }

    public TimeReader min(LocalTime value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }
    public TimeReader after(LocalTime value) { return min(value); }
    public TimeReader atLeast(LocalTime value) { return min(value); }
    public TimeReader max(LocalTime value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }
    public TimeReader before(LocalTime value) { return max(value); }
    public TimeReader atMost(LocalTime value) { return max(value); }
    public TimeReader between(LocalTime lo, LocalTime hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    public TimeReader defaultValue(LocalTime value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public TimeReader help(String text) { this.helpText = text; return this; }

    public TimeReader lenient() { this.lenient = true; return this; }
    public TimeReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    public LocalTime read() {
        Trace.enter("reader", "TimeReader.read", "label=" + label);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.exit("reader", "TimeReader.read", defaultValue);
                return defaultValue;
            }

            LocalTime time = DateTime.parseTime(raw);
            if (time == null) {
                Trace.validation("reader", "time parse", false, "unparseable");
                Messenger.error("Please enter a valid time (e.g. 14:30 or 2:30 PM).");
                continue;
            }
            Trace.value("reader", "parsed time", time);

            if (min != null && time.isBefore(min)) {
                Trace.validation("reader", "RANGE min(" + min + ")", false, "time before min");
                Messenger.error("Time must be on or after " + DateTime.humanize(min) + ".");
                continue;
            }
            if (max != null && time.isAfter(max)) {
                Trace.validation("reader", "RANGE max(" + max + ")", false, "time after max");
                Messenger.error("Time must be on or before " + DateTime.humanize(max) + ".");
                continue;
            }
            Trace.exit("reader", "TimeReader.read", time);
            return time;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) return;
        boolean covered = applied.contains(ValidationCategory.RANGE) || skipped.contains(ValidationCategory.RANGE);
        Trace.decision("reader", "RANGE applied or skipped", covered);
        if (!covered) Strict.emit("time", label, ValidationCategory.RANGE);
    }

    public LocalTime get() { return read(); }
    public LocalTime value() { return read(); }
}
