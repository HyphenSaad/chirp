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
 * Fluent date-time reader — parse a {@link LocalDateTime} with optional
 * range validation.
 *
 * <p>Created via {@link Input#readDateTime()}. For the immediate form,
 * use {@link Input#readDateTime(String)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li>Range validation — {@link #min(LocalDateTime)},
 *       {@link #max(LocalDateTime)},
 *       {@link #between(LocalDateTime, LocalDateTime)}</li>
 *   <li>Relative bounds — {@link #past()}, {@link #future()}</li>
 *   <li>Default value — {@link #defaultValue(LocalDateTime)}</li>
 *   <li>Help text — {@link #help(String)}</li>
 *   <li>Strict-mode controls — {@link #lenient()},
 *       {@link #skipRangeWarning()}</li>
 * </ul>
 *
 * <h2>Accepted formats</h2>
 * <p>{@code 2026-10-31 14:30}, {@code 2026-10-31T14:30},
 * {@code 31/10/2026 14:30}, {@code 31/10/2026 14:30:15}, and the
 * friendly {@code Oct 31, 2026 2:30 PM}. See
 * {@link chirp.datetime.DateTime#parseDateTime(String)} for the exact
 * list.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * LocalDateTime deadline = Input.readDateTime()
 *     .label("Deadline")
 *     .future()
 *     .read();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For date-only, see {@link DateReader}. For time-only, see
 * {@link TimeReader}.</p>
 *
 * @see Input#readDateTime()
 * @see Input#readDateTime(String)
 * @see DateReader
 * @see TimeReader
 * @since 1.0.0
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

    /**
     * Creates a date-time reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static DateTimeReader create() { return new DateTimeReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateTimeReader of() { return new DateTimeReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateTimeReader prompt() { return new DateTimeReader(); }

    /**
     * Creates a date-time reader with a custom label.
     *
     * @param label the prompt label
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateTimeReader prompt(String label) { return new DateTimeReader(label); }

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public DateTimeReader label(String label) {
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
    public DateTimeReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public DateTimeReader withLabel(String label) { return label(label); }

    /**
     * Sets the minimum accepted date-time (inclusive).
     *
     * @param value the minimum
     * @return this reader, for chaining
     * @see #max(LocalDateTime)
     * @see #between(LocalDateTime, LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader min(LocalDateTime value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }

    /**
     * Alias of {@link #min(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param value the minimum
     * @return this reader
     * @see #min(LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader after(LocalDateTime value) { return min(value); }

    /**
     * Alias of {@link #min(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param value the minimum
     * @return this reader
     * @see #min(LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader atLeast(LocalDateTime value) { return min(value); }

    /**
     * Sets the maximum accepted date-time (inclusive).
     *
     * @param value the maximum
     * @return this reader, for chaining
     * @see #min(LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader max(LocalDateTime value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }

    /**
     * Alias of {@link #max(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param value the maximum
     * @return this reader
     * @see #max(LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader before(LocalDateTime value) { return max(value); }

    /**
     * Alias of {@link #max(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param value the maximum
     * @return this reader
     * @see #max(LocalDateTime)
     * @since 1.0.0
     */
    public DateTimeReader atMost(LocalDateTime value) { return max(value); }

    /**
     * Sets both minimum and maximum in one call.
     *
     * @param lo the minimum
     * @param hi the maximum
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public DateTimeReader between(LocalDateTime lo, LocalDateTime hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    /**
     * Convenience — maximum is now (only past date-times accepted).
     *
     * @return this reader
     * @see #future()
     * @since 1.0.0
     */
    public DateTimeReader past() { return max(LocalDateTime.now()); }

    /**
     * Convenience — minimum is one second from now (only future
     * date-times accepted).
     *
     * @return this reader
     * @see #past()
     * @since 1.0.0
     */
    public DateTimeReader future() { return min(LocalDateTime.now().plusSeconds(1)); }

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public DateTimeReader defaultValue(LocalDateTime value) {
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
    public DateTimeReader help(String text) { this.helpText = text; return this; }

    /**
     * Silences all strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipRangeWarning()
     * @since 1.0.0
     */
    public DateTimeReader lenient() { this.lenient = true; return this; }

    /**
     * Silences only the range warning.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public DateTimeReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    /**
     * Reads a date-time. Loops until the input parses and passes the
     * range check (if any). Requires {@code Chirp.start()}.
     *
     * @return the parsed date-time
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed date-time
     * @see #read()
     * @since 1.0.0
     */
    public LocalDateTime get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed date-time
     * @see #read()
     * @since 1.0.0
     */
    public LocalDateTime value() { return read(); }
}
