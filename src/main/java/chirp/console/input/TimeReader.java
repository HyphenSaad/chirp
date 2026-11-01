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
 * Fluent time reader — parse a {@link LocalTime} with optional range
 * validation.
 *
 * <p>Created via {@link Input#readTime()}. For the immediate form, use
 * {@link Input#readTime(String)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>Range validation — {@link #min(LocalTime)},
 *       {@link #max(LocalTime)}, {@link #between(LocalTime, LocalTime)}</li>
 *   <li>Default value — {@link #defaultValue(LocalTime)}</li>
 *   <li>Help text — {@link #help(String)}</li>
 *   <li>Strict-mode controls — {@link #lenient()},
 *       {@link #skipRangeWarning()}</li>
 * </ul>
 *
 * <h2>Accepted formats</h2>
 * <p>24-hour ({@code 14:30}, {@code 14:30:15}), 12-hour with AM/PM
 * ({@code 2:30 PM}), and single-digit hour ({@code 9:15}). See
 * {@link chirp.datetime.DateTime#parseTime(String)} for the exact list.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * LocalTime meeting = Input.readTime()
 *     .label("Meeting time")
 *     .between(LocalTime.of(9, 0), LocalTime.of(17, 0))
 *     .read();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For date-time input, see {@link DateTimeReader}. For date-only,
 * see {@link DateReader}.</p>
 *
 * @see Input#readTime()
 * @see Input#readTime(String)
 * @see DateReader
 * @see DateTimeReader
 * @since 1.0.0
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

    /**
     * Creates a time reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static TimeReader create() { return new TimeReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static TimeReader of() { return new TimeReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static TimeReader prompt() { return new TimeReader(); }

    /**
     * Creates a time reader with a custom label.
     *
     * @param label the prompt label
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static TimeReader prompt(String label) { return new TimeReader(label); }

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public TimeReader label(String label) {
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
    public TimeReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public TimeReader withLabel(String label) { return label(label); }

    /**
     * Sets the minimum accepted time (inclusive).
     *
     * @param value the minimum time
     * @return this reader, for chaining
     * @see #max(LocalTime)
     * @see #between(LocalTime, LocalTime)
     * @since 1.0.0
     */
    public TimeReader min(LocalTime value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }

    /**
     * Alias of {@link #min(LocalTime)} — see that method for full
     * documentation.
     *
     * @param value the minimum time
     * @return this reader
     * @see #min(LocalTime)
     * @since 1.0.0
     */
    public TimeReader after(LocalTime value) { return min(value); }

    /**
     * Alias of {@link #min(LocalTime)} — see that method for full
     * documentation.
     *
     * @param value the minimum time
     * @return this reader
     * @see #min(LocalTime)
     * @since 1.0.0
     */
    public TimeReader atLeast(LocalTime value) { return min(value); }

    /**
     * Sets the maximum accepted time (inclusive).
     *
     * @param value the maximum time
     * @return this reader, for chaining
     * @see #min(LocalTime)
     * @since 1.0.0
     */
    public TimeReader max(LocalTime value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }

    /**
     * Alias of {@link #max(LocalTime)} — see that method for full
     * documentation.
     *
     * @param value the maximum time
     * @return this reader
     * @see #max(LocalTime)
     * @since 1.0.0
     */
    public TimeReader before(LocalTime value) { return max(value); }

    /**
     * Alias of {@link #max(LocalTime)} — see that method for full
     * documentation.
     *
     * @param value the maximum time
     * @return this reader
     * @see #max(LocalTime)
     * @since 1.0.0
     */
    public TimeReader atMost(LocalTime value) { return max(value); }

    /**
     * Sets both minimum and maximum in one call (inclusive on both
     * ends).
     *
     * @param lo the minimum time
     * @param hi the maximum time
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public TimeReader between(LocalTime lo, LocalTime hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default time
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public TimeReader defaultValue(LocalTime value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text
     * @return this reader
     * @since 1.0.0
     */
    public TimeReader help(String text) { this.helpText = text; return this; }

    /**
     * Silences all strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipRangeWarning()
     * @since 1.0.0
     */
    public TimeReader lenient() { this.lenient = true; return this; }

    /**
     * Silences only the range warning.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public TimeReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    /**
     * Reads a time. Loops until the input parses and passes the range
     * check (if any). Requires {@code Chirp.start()}.
     *
     * @return the parsed time
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed time
     * @see #read()
     * @since 1.0.0
     */
    public LocalTime get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed time
     * @see #read()
     * @since 1.0.0
     */
    public LocalTime value() { return read(); }
}
