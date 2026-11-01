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
 * Fluent date reader — parse a {@link LocalDate} with optional range
 * validation and strict-mode warnings.
 *
 * <p>Created via {@link Input#readDate()}. For the immediate form, use
 * {@link Input#readDate(String)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li><b>Range validation</b> — {@link #min(LocalDate)},
 *       {@link #max(LocalDate)}, {@link #between(LocalDate, LocalDate)}</li>
 *   <li><b>Relative bounds</b> — {@link #past()}, {@link #future()}</li>
 *   <li><b>Default value</b> — {@link #defaultValue(LocalDate)}</li>
 *   <li><b>Help text</b> — {@link #help(String)}</li>
 *   <li><b>Strict-mode controls</b> — {@link #lenient()} silences the
 *       range warning entirely; {@link #skipRangeWarning()} silences
 *       just the range warning</li>
 * </ul>
 *
 * <h2>Strict mode</h2>
 * <p>When strict mode is on (the default in interactive console mode),
 * the reader emits a {@code [STRICT]} hint the first time it runs
 * without a range check:
 * <code>"Reading 'Date' as date: no RANGE validation. Consider
 * .between(min, max), .past(), .future(), ..."</code>.</p>
 * <p>Applying any of the range methods silences the warning. So does
 * {@link #skipRangeWarning()}, for cases where range genuinely does not
 * apply.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * LocalDate dob = Input.readDate()
 *     .label("Date of birth")
 *     .between(LocalDate.of(1950, 1, 1), LocalDate.now())
 *     .read();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the immediate form, see {@link Input#readDate(String)}. For
 * dates with time-of-day, see {@link DateTimeReader}. For time-only,
 * see {@link TimeReader}.</p>
 *
 * @see Input#readDate()
 * @see Input#readDate(String)
 * @see DateTimeReader
 * @see TimeReader
 * @since 1.0.0
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

    // ============================================================== factories

    /**
     * Creates a date reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static DateReader create() { return new DateReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateReader of() { return new DateReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateReader prompt() { return new DateReader(); }

    /**
     * Creates a date reader with a custom label.
     *
     * @param label the prompt label (null → default)
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static DateReader prompt(String label) { return new DateReader(label); }

    // ============================================================== configuration

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @see #message(String)
     * @see #withLabel(String)
     * @since 1.0.0
     */
    public DateReader label(String label) {
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
    public DateReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public DateReader withLabel(String label) { return label(label); }

    /**
     * Sets the minimum accepted date (inclusive). Applying any range
     * method silences the strict-mode warning.
     *
     * @param value the minimum date (null → no minimum)
     * @return this reader, for chaining
     * @see #after(LocalDate)
     * @see #atLeast(LocalDate)
     * @see #max(LocalDate)
     * @see #between(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public DateReader min(LocalDate value) {
        this.min = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: min({})", value);
        return this;
    }

    /**
     * Alias of {@link #min(LocalDate)} — see that method for full
     * documentation.
     *
     * @param value the minimum date
     * @return this reader
     * @see #min(LocalDate)
     * @since 1.0.0
     */
    public DateReader after(LocalDate value) { return min(value); }

    /**
     * Alias of {@link #min(LocalDate)} — see that method for full
     * documentation.
     *
     * @param value the minimum date
     * @return this reader
     * @see #min(LocalDate)
     * @since 1.0.0
     */
    public DateReader atLeast(LocalDate value) { return min(value); }

    /**
     * Sets the maximum accepted date (inclusive).
     *
     * @param value the maximum date (null → no maximum)
     * @return this reader, for chaining
     * @see #before(LocalDate)
     * @see #atMost(LocalDate)
     * @see #min(LocalDate)
     * @since 1.0.0
     */
    public DateReader max(LocalDate value) {
        this.max = value;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: max({})", value);
        return this;
    }

    /**
     * Alias of {@link #max(LocalDate)} — see that method for full
     * documentation.
     *
     * @param value the maximum date
     * @return this reader
     * @see #max(LocalDate)
     * @since 1.0.0
     */
    public DateReader before(LocalDate value) { return max(value); }

    /**
     * Alias of {@link #max(LocalDate)} — see that method for full
     * documentation.
     *
     * @param value the maximum date
     * @return this reader
     * @see #max(LocalDate)
     * @since 1.0.0
     */
    public DateReader atMost(LocalDate value) { return max(value); }

    /**
     * Sets both minimum and maximum in one call (inclusive on both
     * ends).
     *
     * @param lo the minimum date
     * @param hi the maximum date
     * @return this reader, for chaining
     * @see #inRange(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public DateReader between(LocalDate lo, LocalDate hi) {
        this.min = lo;
        this.max = hi;
        applied.add(ValidationCategory.RANGE);
        Trace.debug("reader", "configure: between({}, {})", lo, hi);
        return this;
    }

    /**
     * Alias of {@link #between(LocalDate, LocalDate)} — see that method
     * for full documentation.
     *
     * @param lo the minimum date
     * @param hi the maximum date
     * @return this reader
     * @see #between(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public DateReader inRange(LocalDate lo, LocalDate hi) { return between(lo, hi); }

    /**
     * Convenience — the maximum accepted date is today (only past dates
     * are valid). Equivalent to {@code max(LocalDate.now())}.
     *
     * @return this reader
     * @see #future()
     * @since 1.0.0
     */
    public DateReader past() { return max(LocalDate.now()); }

    /**
     * Convenience — the minimum accepted date is tomorrow (only future
     * dates are valid). Equivalent to
     * {@code min(LocalDate.now().plusDays(1))}.
     *
     * @return this reader
     * @see #past()
     * @since 1.0.0
     */
    public DateReader future() { return min(LocalDate.now().plusDays(1)); }

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default date
     * @return this reader, for chaining
     * @see #defaulting(LocalDate)
     * @since 1.0.0
     */
    public DateReader defaultValue(LocalDate value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Alias of {@link #defaultValue(LocalDate)} — see that method for
     * full documentation.
     *
     * @param value the default date
     * @return this reader
     * @see #defaultValue(LocalDate)
     * @since 1.0.0
     */
    public DateReader defaulting(LocalDate value) { return defaultValue(value); }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text
     * @return this reader
     * @since 1.0.0
     */
    public DateReader help(String text) { this.helpText = text; return this; }

    /**
     * Alias of {@link #help(String)} — see that method for full
     * documentation.
     *
     * @param text the help text
     * @return this reader
     * @see #help(String)
     * @since 1.0.0
     */
    public DateReader hint(String text) { return help(text); }

    /**
     * Silences <b>all</b> strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipRangeWarning()
     * @since 1.0.0
     */
    public DateReader lenient() { this.lenient = true; return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public DateReader noStrict() { return lenient(); }

    /**
     * Silences only the range warning, leaving other strict warnings
     * (if any) intact. Use this when a range genuinely does not apply.
     *
     * @return this reader, for chaining
     * @see #lenient()
     * @since 1.0.0
     */
    public DateReader skipRangeWarning() { skipped.add(ValidationCategory.RANGE); return this; }

    // ============================================================== terminal

    /**
     * Reads a date. Loops until the input parses as a
     * {@link LocalDate} and passes the range validation (if set).
     * Accepts the formats documented on
     * {@link chirp.datetime.DateTime#parseDate(String)}. Requires
     * {@code Chirp.start()}.
     *
     * @return the parsed date
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed date
     * @see #read()
     * @since 1.0.0
     */
    public LocalDate get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed date
     * @see #read()
     * @since 1.0.0
     */
    public LocalDate value() { return read(); }
}
