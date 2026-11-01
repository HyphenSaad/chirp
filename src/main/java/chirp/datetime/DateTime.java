package chirp.datetime;

import chirp.internal.Trace;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

/**
 * Date, time, and date-time helpers — null-safe and never throws.
 *
 * <p>Every method accepts {@code null} input and returns a documented
 * default rather than throwing {@link NullPointerException} or
 * {@link DateTimeParseException}. The parse methods return {@code null} on
 * failure so callers can use a single {@code if (date != null)} check
 * instead of a {@code try/catch}.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Current date/time</b> — {@link #today()}, {@link #now()},
 *       {@link #yesterday()}, {@link #tomorrow()}</li>
 *   <li><b>Parsing</b> — {@link #parseDate(String)},
 *       {@link #parseTime(String)}, {@link #parseDateTime(String)}</li>
 *   <li><b>Formatting</b> — {@link #format(LocalDate, String)},
 *       {@link #humanize(LocalDate)},
 *       {@link #formatISO(LocalDate)}</li>
 *   <li><b>Arithmetic</b> — {@link #addDays(LocalDate, int)},
 *       {@link #subtractDays(LocalDate, int)},
 *       {@link #addMonths(LocalDate, int)},
 *       {@link #addBusinessDays(LocalDate, int)}</li>
 *   <li><b>Snapping</b> — {@link #startOfMonth(LocalDate)},
 *       {@link #endOfWeek(LocalDate)},
 *       {@link #startOfQuarter(LocalDate)}</li>
 *   <li><b>Differences</b> — {@link #daysBetween(LocalDate, LocalDate)},
 *       {@link #businessDaysBetween(LocalDate, LocalDate)},
 *       {@link #age(LocalDate)}</li>
 *   <li><b>Names and digits</b> — {@link #dayName(LocalDate)},
 *       {@link #monthName(LocalDate)},
 *       {@link #quarter(LocalDate)}</li>
 *   <li><b>Comparison</b> — {@link #isBefore(LocalDate, LocalDate)},
 *       {@link #isWeekend(LocalDate)},
 *       {@link #isBetween(LocalDate, LocalDate, LocalDate)}</li>
 *   <li><b>Relative time</b> — {@link #timeAgo(LocalDate)}</li>
 *   <li><b>Epoch conversion</b> — {@link #toEpochMillis(LocalDateTime)},
 *       {@link #fromEpochMillis(long)}</li>
 *   <li><b>Fluent chains</b> — {@link #of(LocalDate)} → {@link DateChain}</li>
 * </ul>
 *
 * <h2>Multi-format parsing</h2>
 * <p>{@link #parseDate(String)} tries every common date format in order:
 * {@code yyyy-MM-dd}, {@code dd/MM/yyyy}, {@code dd-MM-yyyy},
 * {@code MM/dd/yyyy}, {@code MM-dd-yyyy}, {@code d MMM yyyy},
 * {@code d MMMM yyyy}, {@code MMM d, yyyy}, {@code MMMM d, yyyy}. The
 * first format that parses successfully wins — everything else is silently
 * skipped. This means {@code "31/10/2026"} and {@code "Oct 31, 2026"} and
 * {@code "2026-10-31"} all return the same {@link LocalDate}.</p>
 *
 * <p>For an explicit pattern, use the two-argument overload
 * ({@link #parseDate(String, String)}) or the key-value form
 * ({@link #parseDate(String, Object...)} with {@link Key#PATTERN}).</p>
 *
 * <h2>Locale</h2>
 * <p>All formatting uses {@link java.util.Locale#ENGLISH}, so
 * {@code "31 Oct 2026"} parses identically on a Turkish-locale machine.
 * The English month names are the ones {@link #humanize(LocalDate)} emits.
 * For locale-specific output, use the key-value {@code humanize} with
 * {@link Key#LOCALE}.</p>
 *
 * <h2>Time zones</h2>
 * <p>All methods operate in the JVM's default time zone. This is
 * deliberate — a student running locally wants their local dates, not
 * UTC. For a specific zone, use the JDK's
 * {@link java.time.ZonedDateTime} directly.</p>
 *
 * <h2>Example — a small birthday calculator</h2>
 * <pre>{@code
 * LocalDate birth = readDate("Date of birth");
 * int age = DateTime.age(birth);
 * println("You are " + age + " years old.");
 * println("That's " + DateTime.daysBetween(birth, DateTime.today())
 *       + " days, or " + DateTime.businessDaysBetween(birth, DateTime.today())
 *       + " working days.");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For fluent chaining, see {@link DateChain}. For time-of-day parsing
 * and formatting, use the {@code LocalTime} overloads. For duration
 * humanisation, see {@link chirp.format.Format#duration(long)}.</p>
 *
 * @see DateChain
 * @see chirp.format.Format
 * @since 1.0.0
 */
public final class DateTime {

    // ============================================================== enums

    /**
     * Key enum for key-value overloads in this module.
     *
     * <p>Consumed by {@link #parseDate(String, Object...)},
     * {@link #humanize(LocalDate, Object...)},
     * {@link #timeAgo(LocalDateTime, Object...)}. All other methods take
     * plain arguments.</p>
     *
     * @since 1.0.0
     */
    public enum Key {
        /** A {@link String} format pattern for parsing or formatting. */
        PATTERN,
        /** A {@link Locale} for parsing or formatting. */
        LOCALE,
        /** A {@link Style} for humanize output. */
        STYLE,
        /** A {@link LocalDateTime} reference for relative-time output. */
        REFERENCE,
        /** Custom weekend days as {@code DayOfWeek...}. */
        WEEKEND_DAYS,
        /** Custom business days as {@code DayOfWeek...}. */
        BUSINESS_DAYS,
        /** Whether the range is inclusive — reserved for future use. */
        INCLUSIVE
    }

    /**
     * Style of a human-readable date/time string.
     *
     * @since 1.0.0
     */
    public enum Style {
        /** {@code "Oct 31, 2026"} for dates, {@code "2:30 PM"} for times. */
        SHORT,
        /** {@code "Oct 31, 2026 14:30"} for date-times. */
        MEDIUM,
        /** {@code "Saturday, October 31, 2026"} for dates. */
        LONG,
        /** {@code "Saturday, October 31, 2026 at 2:30 PM"} for date-times. */
        FULL,
        /** ISO 8601 — {@code "2026-10-31"}. */
        ISO,
        /** Relative — {@code "5m ago"}, {@code "yesterday"}, {@code "in 3d"}. */
        RELATIVE
    }

    // ============================================================== formats

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));

    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH));

    private static final List<DateTimeFormatter> DATETIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH));

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy",
            Locale.ENGLISH);
    private static final DateTimeFormatter MEDIUM_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm",
            Locale.ENGLISH);
    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a",
            Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DayOfWeek[] DEFAULT_WEEKEND = {DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

    private DateTime() {}

    // ============================================================== now / today

    /**
     * Returns today's date in the JVM's default time zone.
     *
     * @return today's date
     * @see #currentDate()
     * @see #nowDate()
     * @see #now()
     * @see #nowTime()
     * @since 1.0.0
     */
    public static LocalDate today() { return LocalDate.now(); }

    /**
     * Alias of {@link #today()} — see that method for full documentation.
     *
     * @return today's date
     * @see #today()
     * @since 1.0.0
     */
    public static LocalDate currentDate() { return today(); }

    /**
     * Alias of {@link #today()} — see that method for full documentation.
     *
     * @return today's date
     * @see #today()
     * @since 1.0.0
     */
    public static LocalDate nowDate() { return today(); }

    /**
     * Returns yesterday's date — {@code today() - 1 day}.
     *
     * @return yesterday's date
     * @see #dayBefore()
     * @see #tomorrow()
     * @see #today()
     * @since 1.0.0
     */
    public static LocalDate yesterday() { return LocalDate.now().minusDays(1); }

    /**
     * Returns tomorrow's date — {@code today() + 1 day}.
     *
     * @return tomorrow's date
     * @see #dayAfter()
     * @see #yesterday()
     * @see #today()
     * @since 1.0.0
     */
    public static LocalDate tomorrow() { return LocalDate.now().plusDays(1); }

    /**
     * Alias of {@link #yesterday()} — see that method for full
     * documentation.
     *
     * @return yesterday's date
     * @see #yesterday()
     * @since 1.0.0
     */
    public static LocalDate dayBefore() { return yesterday(); }

    /**
     * Alias of {@link #tomorrow()} — see that method for full
     * documentation.
     *
     * @return tomorrow's date
     * @see #tomorrow()
     * @since 1.0.0
     */
    public static LocalDate dayAfter() { return tomorrow(); }

    /**
     * Returns the current time of day with nanosecond precision stripped
     * to seconds — {@code 14:30:15}.
     *
     * @return the current time
     * @see #currentTime()
     * @see #now()
     * @see #today()
     * @since 1.0.0
     */
    public static LocalTime nowTime() { return LocalTime.now().withNano(0); }

    /**
     * Alias of {@link #nowTime()} — see that method for full
     * documentation.
     *
     * @return the current time
     * @see #nowTime()
     * @since 1.0.0
     */
    public static LocalTime currentTime() { return nowTime(); }

    /**
     * Returns the current date and time with nanosecond precision stripped
     * to seconds.
     *
     * @return the current date and time
     * @see #current()
     * @see #nowDateTime()
     * @see #today()
     * @see #nowTime()
     * @since 1.0.0
     */
    public static LocalDateTime now() { return LocalDateTime.now().withNano(0); }

    /**
     * Alias of {@link #now()} — see that method for full documentation.
     *
     * @return the current date and time
     * @see #now()
     * @since 1.0.0
     */
    public static LocalDateTime current() { return now(); }

    /**
     * Alias of {@link #now()} — see that method for full documentation.
     *
     * @return the current date and time
     * @see #now()
     * @since 1.0.0
     */
    public static LocalDateTime nowDateTime() { return now(); }

    // ============================================================== chains

    /**
     * Opens a fluent {@link DateChain} wrapping the date.
     *
     * <pre>{@code
     * LocalDate endOfNextMonth = DateTime.of(LocalDate.now())
     *     .addMonths(1)
     *     .endOfMonth()
     *     .asDate();
     * }</pre>
     *
     * @param date the date to wrap (may be null — falls back to today)
     * @return a new {@code DateChain} wrapping {@code date}
     * @see DateChain
     * @see #on(LocalDate)
     * @see #chain(LocalDate)
     * @see #of(LocalDateTime)
     * @since 1.0.0
     */
    public static DateChain of(LocalDate date) { return DateChain.of(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation and examples.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain on(LocalDate date) { return DateChain.of(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation and examples.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain chain(LocalDate date) { return DateChain.of(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation and examples.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain from(LocalDate date) { return DateChain.of(date); }

    /**
     * Wraps the date-time's date part in a {@link DateChain}.
     *
     * @param dateTime the date-time (may be null — falls back to today)
     * @return a new {@code DateChain} wrapping {@code dateTime.toLocalDate()}
     * @see DateChain
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain of(LocalDateTime dateTime) { return DateChain.of(dateTime); }

    /**
     * Alias of {@link #of(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param dateTime the date-time
     * @return a new {@code DateChain}
     * @see #of(LocalDateTime)
     * @since 1.0.0
     */
    public static DateChain on(LocalDateTime dateTime) { return DateChain.of(dateTime); }

    /**
     * Parses the string into a {@code DateChain}, or falls back to today
     * if the string is unparseable.
     *
     * @param dateString the date string (may be null or invalid)
     * @return a new {@code DateChain} wrapping the parsed date, or today
     * if parsing fails
     * @see #parseDate(String)
     * @see DateChain#of(String)
     * @since 1.0.0
     */
    public static DateChain of(String dateString) { return DateChain.of(dateString); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param dateString the date string
     * @return a new {@code DateChain}
     * @see #of(String)
     * @since 1.0.0
     */
    public static DateChain parse(String dateString) { return DateChain.of(dateString); }

    /**
     * Opens a {@link DateChain} wrapping today's date.
     *
     * @return a new {@code DateChain} wrapping {@link #today()}
     * @see DateChain
     * @see #nowChain()
     * @since 1.0.0
     */
    public static DateChain todayChain() { return DateChain.of(today()); }

    /**
     * Opens a {@link DateChain} wrapping the current date-time's date part.
     *
     * @return a new {@code DateChain} wrapping {@link #now()}'s date
     * @see DateChain
     * @see #todayChain()
     * @since 1.0.0
     */
    public static DateChain nowChain() { return DateChain.of(now()); }

    // ============================================================== parsing

    /**
     * Parses a date string, trying every common format until one succeeds.
     *
     * <p>Formats attempted, in order: {@code yyyy-MM-dd},
     * {@code dd/MM/yyyy}, {@code dd-MM-yyyy}, {@code MM/dd/yyyy},
     * {@code MM-dd-yyyy}, {@code d MMM yyyy}, {@code d MMMM yyyy},
     * {@code MMM d, yyyy}, {@code MMMM d, yyyy}.</p>
     *
     * <pre>{@code
     * DateTime.parseDate("2026-10-31");    // LocalDate.of(2026, 10, 31)
     * DateTime.parseDate("31/10/2026");    // LocalDate.of(2026, 10, 31)
     * DateTime.parseDate("Oct 31, 2026");  // LocalDate.of(2026, 10, 31)
     * DateTime.parseDate("not-a-date");    // null
     * DateTime.parseDate(null);            // null
     * DateTime.parseDate("");              // null
     * }</pre>
     *
     * @param text the date string (may be null or blank)
     * @return the parsed date, or {@code null} if unparseable
     * @see #readDate(String)
     * @see #dateFrom(String)
     * @see #parseDate(String, String)
     * @see #parseDate(String, Object...)
     * @since 1.0.0
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return null;
        String trimmed = text.trim();
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {}
        }
        Trace.debug("datetime", "parseDate failed for \"{}\"", trimmed);
        return null;
    }

    /**
     * Alias of {@link #parseDate(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the date string
     * @return the parsed date, or null
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static LocalDate readDate(String text) { return parseDate(text); }

    /**
     * Alias of {@link #parseDate(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the date string
     * @return the parsed date, or null
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static LocalDate dateFrom(String text) { return parseDate(text); }

    /**
     * Alias of {@link #parseDate(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the date string
     * @return the parsed date, or null
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static LocalDate toDate(String text) { return parseDate(text); }

    /**
     * Alias of {@link #parseDate(String)} — see that method for full
     * documentation and examples.
     *
     * @param text the date string
     * @return the parsed date, or null
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static LocalDate tryParseDate(String text) { return parseDate(text); }

    /**
     * Parses a date string using an explicit {@link DateTimeFormatter}
     * pattern.
     *
     * @param text the date string (may be null)
     * @param pattern the format pattern (may be null — returns null)
     * @return the parsed date, or {@code null} if unparseable
     * @see #parseDate(String)
     * @see #parseDate(String, Object...)
     * @since 1.0.0
     */
    public static LocalDate parseDate(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date string using a key-value pattern.
     *
     * <p>Accepts {@link Key#PATTERN} as a {@link String} and
     * {@link Key#LOCALE} as a {@link Locale}. If no pattern is given, falls
     * back to {@link #parseDate(String)} (multi-format).</p>
     *
     * <pre>{@code
     * DateTime.parseDate("10-31-2026", DateTime.Key.PATTERN, "MM-dd-yyyy");
     * // LocalDate.of(2026, 10, 31)
     * }</pre>
     *
     * @param text the date string (may be null)
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return the parsed date, or {@code null} if unparseable
     * @see #parseDate(String)
     * @see #parseDate(String, String)
     * @since 1.0.0
     */
    public static LocalDate parseDate(String text, Object... keyValues) {
        String pattern = null;
        Locale locale = Locale.ENGLISH;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case PATTERN -> {
                        if (v instanceof String s) pattern = s;
                    }
                    case LOCALE -> {
                        if (v instanceof Locale l) locale = l;
                    }
                    default -> {
                        // Other keys are irrelevant for date parsing — ignore them.
                    }
                }
            }
        }
        if (pattern == null) return parseDate(text);
        try {
            return LocalDate.parse(text == null ? "" : text.trim(),
                    DateTimeFormatter.ofPattern(pattern, locale));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Parses a time string, trying every common format until one succeeds.
     *
     * <p>Formats attempted: {@code HH:mm}, {@code HH:mm:ss}, {@code H:mm},
     * {@code h:mm a}, {@code h:mm:ss a}.</p>
     *
     * @param text the time string (may be null or blank)
     * @return the parsed time, or {@code null} if unparseable
     * @see #readTime(String)
     * @see #timeFrom(String)
     * @see #parseTime(String, String)
     * @since 1.0.0
     */
    public static LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) return null;
        String trimmed = text.trim();
        for (DateTimeFormatter format : TIME_FORMATS) {
            try {
                return LocalTime.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    /**
     * Alias of {@link #parseTime(String)} — see that method for full
     * documentation.
     *
     * @param text the time string
     * @return the parsed time, or null
     * @see #parseTime(String)
     * @since 1.0.0
     */
    public static LocalTime readTime(String text) { return parseTime(text); }

    /**
     * Alias of {@link #parseTime(String)} — see that method for full
     * documentation.
     *
     * @param text the time string
     * @return the parsed time, or null
     * @see #parseTime(String)
     * @since 1.0.0
     */
    public static LocalTime timeFrom(String text) { return parseTime(text); }

    /**
     * Alias of {@link #parseTime(String)} — see that method for full
     * documentation.
     *
     * @param text the time string
     * @return the parsed time, or null
     * @see #parseTime(String)
     * @since 1.0.0
     */
    public static LocalTime toTime(String text) { return parseTime(text); }

    /**
     * Parses a time string using an explicit pattern.
     *
     * @param text the time string (may be null)
     * @param pattern the format pattern (may be null)
     * @return the parsed time, or {@code null}
     * @see #parseTime(String)
     * @since 1.0.0
     */
    public static LocalTime parseTime(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalTime.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date-time string, trying every common format until one
     * succeeds, then falling back to date + time concatenation.
     *
     * @param text the date-time string (may be null or blank)
     * @return the parsed date-time, or {@code null} if unparseable
     * @see #readDateTime(String)
     * @see #dateTimeFrom(String)
     * @see #parseDateTime(String, String)
     * @since 1.0.0
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        String trimmed = text.trim();

        for (DateTimeFormatter format : DATETIME_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {}
        }

        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace > 0) {
            LocalDate date = parseDate(trimmed.substring(0, lastSpace));
            LocalTime time = parseTime(trimmed.substring(lastSpace + 1));
            if (date != null && time != null) return LocalDateTime.of(date, time);
        }
        return null;
    }

    /**
     * Alias of {@link #parseDateTime(String)} — see that method for full
     * documentation.
     *
     * @param text the date-time string
     * @return the parsed date-time, or null
     * @see #parseDateTime(String)
     * @since 1.0.0
     */
    public static LocalDateTime readDateTime(String text) { return parseDateTime(text); }

    /**
     * Alias of {@link #parseDateTime(String)} — see that method for full
     * documentation.
     *
     * @param text the date-time string
     * @return the parsed date-time, or null
     * @see #parseDateTime(String)
     * @since 1.0.0
     */
    public static LocalDateTime dateTimeFrom(String text) { return parseDateTime(text); }

    /**
     * Alias of {@link #parseDateTime(String)} — see that method for full
     * documentation.
     *
     * @param text the date-time string
     * @return the parsed date-time, or null
     * @see #parseDateTime(String)
     * @since 1.0.0
     */
    public static LocalDateTime toDateTime(String text) { return parseDateTime(text); }

    /**
     * Parses a date-time string using an explicit pattern.
     *
     * @param text the date-time string (may be null)
     * @param pattern the format pattern (may be null)
     * @return the parsed date-time, or {@code null}
     * @see #parseDateTime(String)
     * @since 1.0.0
     */
    public static LocalDateTime parseDateTime(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalDateTime.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ============================================================== formatting

    /**
     * Formats a date with the given pattern.
     *
     * <pre>{@code
     * DateTime.format(LocalDate.of(2026, 10, 31), "dd/MM/yyyy");
     * // "31/10/2026"
     * }</pre>
     *
     * @param date the date (may be null)
     * @param pattern the format pattern (may be null)
     * @return the formatted string, or empty for null input
     * @see #formatISO(LocalDate)
     * @see #humanize(LocalDate)
     * @since 1.0.0
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) return "";
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    /**
     * Formats a time with the given pattern.
     *
     * @param time the time (may be null)
     * @param pattern the format pattern (may be null)
     * @return the formatted string, or empty for null input
     * @see #humanize(LocalTime)
     * @since 1.0.0
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null || pattern == null) return "";
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    /**
     * Formats a date-time with the given pattern.
     *
     * @param dateTime the date-time (may be null)
     * @param pattern the format pattern (may be null)
     * @return the formatted string, or empty for null input
     * @see #humanize(LocalDateTime)
     * @since 1.0.0
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    /**
     * Formats a date as an ISO 8601 string — {@code "2026-10-31"}.
     *
     * @param date the date (may be null)
     * @return the ISO string, or empty for null
     * @see #format(LocalDate, String)
     * @since 1.0.0
     */
    public static String formatISO(LocalDate date) { return date == null ? "" : ISO_DATE.format(date); }

    /**
     * Formats a date-time as an ISO 8601 string.
     *
     * @param dt the date-time (may be null)
     * @return the ISO string, or empty for null
     * @see #formatISO(LocalDate)
     * @since 1.0.0
     */
    public static String formatISO(LocalDateTime dt) { return dt == null ? "" : ISO_DATETIME.format(dt); }

    // ============================================================== humanize

    /**
     * Returns a human-readable long form of the date —
     * {@code "Saturday, October 31, 2026"}.
     *
     * @param date the date (may be null)
     * @return the humanized string, or empty for null
     * @see #humanize(LocalDate, Object...)
     * @see #humanizeShort(LocalDate)
     * @see #humanFormat(LocalDate)
     * @see #friendly(LocalDate)
     * @since 1.0.0
     */
    public static String humanize(LocalDate date) {
        return date == null ? "" : LONG_DATE.format(date);
    }

    /**
     * Alias of {@link #humanize(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the humanized string
     * @see #humanize(LocalDate)
     * @since 1.0.0
     */
    public static String humanFormat(LocalDate date) { return humanize(date); }

    /**
     * Alias of {@link #humanize(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the humanized string
     * @see #humanize(LocalDate)
     * @since 1.0.0
     */
    public static String friendly(LocalDate date) { return humanize(date); }

    /**
     * Returns a human-readable date using the specified {@link Style}.
     *
     * <pre>{@code
     * LocalDate d = LocalDate.of(2026, 10, 31);
     * DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.SHORT);
     * // "Oct 31, 2026"
     * DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.LONG);
     * // "Saturday, October 31, 2026"
     * DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.ISO);
     * // "2026-10-31"
     * DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.RELATIVE);
     * // "5m ago"  (or similar, depends on today)
     * }</pre>
     *
     * @param date the date (may be null)
     * @param keyValues alternating {@link Key} → value pairs (only
     * {@link Key#STYLE} is consumed)
     * @return the humanized string
     * @see #humanize(LocalDate)
     * @see Style
     * @since 1.0.0
     */
    public static String humanize(LocalDate date, Object... keyValues) {
        if (date == null) return "";
        Style style = Style.LONG;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.STYLE && keyValues[i + 1] instanceof Style s) style = s;
            }
        }
        return switch (style) {
            case SHORT -> SHORT_DATE.format(date);
            case MEDIUM -> MEDIUM_DATE.format(date.atStartOfDay());
            case LONG -> LONG_DATE.format(date);
            case FULL -> FULL_DATE.format(date.atStartOfDay());
            case ISO -> ISO_DATE.format(date);
            case RELATIVE -> timeAgo(date);
        };
    }

    /**
     * Returns the short human form of the date — {@code "Oct 31, 2026"}.
     *
     * @param date the date (may be null)
     * @return the short humanized string, or empty for null
     * @see #humanize(LocalDate)
     * @since 1.0.0
     */
    public static String humanizeShort(LocalDate date) {
        return date == null ? "" : SHORT_DATE.format(date);
    }

    /**
     * Returns the short human form of the time — {@code "2:30 PM"}.
     *
     * @param time the time (may be null)
     * @return the humanized string, or empty for null
     * @see #humanize(LocalDateTime)
     * @since 1.0.0
     */
    public static String humanize(LocalTime time) {
        return time == null ? "" : SHORT_TIME.format(time);
    }

    /**
     * Returns a human-readable form of the date-time —
     * {@code "Oct 31, 2026 14:30"}.
     *
     * @param dateTime the date-time (may be null)
     * @return the humanized string, or empty for null
     * @see #humanize(LocalDate)
     * @see #humanize(LocalTime)
     * @since 1.0.0
     */
    public static String humanize(LocalDateTime dateTime) {
        return dateTime == null ? "" : MEDIUM_DATE.format(dateTime);
    }

    // ============================================================== validity

    /**
     * Checks whether the given year/month/day is a valid calendar date.
     *
     * <p>Correctly handles leap years — {@code isValidDate(2024, 2, 29)}
     * is {@code true}, {@code isValidDate(2026, 2, 29)} is {@code false}.</p>
     *
     * @param year the year
     * @param month the month (1–12)
     * @param day the day (1–31, further constrained by month/year)
     * @return {@code true} if the triple forms a valid date
     * @see #isValidDate(String)
     * @see #isLeapYear(int)
     * @since 1.0.0
     */
    public static boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Checks whether the given hour/minute/second is a valid time of day.
     *
     * @param hour the hour (0–23)
     * @param minute the minute (0–59)
     * @param second the second (0–59)
     * @return {@code true} if the triple forms a valid time
     * @since 1.0.0
     */
    public static boolean isValidTime(int hour, int minute, int second) {
        return hour >= 0 && hour <= 23
                && minute >= 0 && minute <= 59
                && second >= 0 && second <= 59;
    }

    /**
     * Checks whether the year is a leap year under the Gregorian calendar.
     *
     * <p>1900 is <b>not</b> a leap year (divisible by 100, not by 400).
     * 2000 <b>is</b> a leap year (divisible by 400).</p>
     *
     * @param year the year
     * @return {@code true} if the year is a leap year
     * @see #leapYear(int)
     * @see #isLeap(int)
     * @since 1.0.0
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Alias of {@link #isLeapYear(int)} — see that method for full
     * documentation.
     *
     * @param year the year
     * @return {@code true} if leap
     * @see #isLeapYear(int)
     * @since 1.0.0
     */
    public static boolean leapYear(int year) { return isLeapYear(year); }

    /**
     * Alias of {@link #isLeapYear(int)} — see that method for full
     * documentation.
     *
     * @param year the year
     * @return {@code true} if leap
     * @see #isLeapYear(int)
     * @since 1.0.0
     */
    public static boolean isLeap(int year) { return isLeapYear(year); }

    /**
     * Checks whether the string can be parsed as a date by
     * {@link #parseDate(String)}.
     *
     * @param text the string to test (may be null)
     * @return {@code true} if parseable
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static boolean isValidDate(String text) {
        return parseDate(text) != null;
    }

    /**
     * Checks whether the string can be parsed as a date with the given
     * pattern.
     *
     * @param text the string to test (may be null)
     * @param pattern the pattern to test against
     * @return {@code true} if parseable
     * @see #parseDate(String, String)
     * @since 1.0.0
     */
    public static boolean isValidDate(String text, String pattern) {
        return parseDate(text, pattern) != null;
    }

    // ============================================================== arithmetic

    /**
     * Adds the given number of days. Negative values subtract.
     *
     * <pre>{@code
     * DateTime.addDays(LocalDate.of(2026, 10, 31), 10);    // 2026-11-10
     * DateTime.addDays(LocalDate.of(2026, 10, 31), -5);    // 2026-10-26
     * }</pre>
     *
     * @param date the starting date (may be null)
     * @param days the number of days to add (negative subtracts)
     * @return the shifted date, or null if {@code date} is null
     * @see #plusDays(LocalDate, int)
     * @see #daysAfter(LocalDate, int)
     * @see #subtractDays(LocalDate, int)
     * @see #addBusinessDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate addDays(LocalDate date, int days) { return date == null ? null : date.plusDays(days); }

    /**
     * Alias of {@link #addDays(LocalDate, int)} — see that method for full
     * documentation and examples.
     *
     * @param date the starting date
     * @param days the number of days to add
     * @return the shifted date
     * @see #addDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate plusDays(LocalDate date, int days) { return addDays(date, days); }

    /**
     * Alias of {@link #addDays(LocalDate, int)} — see that method for full
     * documentation and examples.
     *
     * @param date the starting date
     * @param days the number of days to add
     * @return the shifted date
     * @see #addDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate daysAfter(LocalDate date, int days) { return addDays(date, days); }

    /**
     * Subtracts the given number of days. Negative values add.
     *
     * @param date the starting date (may be null)
     * @param days the number of days to subtract
     * @return the shifted date, or null if {@code date} is null
     * @see #minusDays(LocalDate, int)
     * @see #daysBefore(LocalDate, int)
     * @see #addDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate subtractDays(LocalDate date, int days) {
        return date == null ? null : date.minusDays(days);
    }

    /**
     * Alias of {@link #subtractDays(LocalDate, int)} — see that method for
     * full documentation.
     *
     * @param date the starting date
     * @param days the number of days to subtract
     * @return the shifted date
     * @see #subtractDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate minusDays(LocalDate date, int days) { return subtractDays(date, days); }

    /**
     * Alias of {@link #subtractDays(LocalDate, int)} — see that method for
     * full documentation.
     *
     * @param date the starting date
     * @param days the number of days to subtract
     * @return the shifted date
     * @see #subtractDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate daysBefore(LocalDate date, int days) { return subtractDays(date, days); }

    /**
     * Adds the given number of weeks. Negative values subtract.
     *
     * @param date the starting date (may be null)
     * @param weeks the number of weeks to add
     * @return the shifted date, or null
     * @see #subtractWeeks(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate addWeeks(LocalDate date, int weeks) {
        return date == null ? null : date.plusWeeks(weeks);
    }

    /**
     * Subtracts the given number of weeks.
     *
     * @param date the starting date (may be null)
     * @param weeks the number of weeks to subtract
     * @return the shifted date, or null
     * @see #addWeeks(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate subtractWeeks(LocalDate date, int weeks) {
        return date == null ? null : date.minusWeeks(weeks);
    }

    /**
     * Adds the given number of months. The day-of-month is clamped to the
     * target month's length — {@code 2026-01-31 + 1 month = 2026-02-28},
     * not an exception.
     *
     * @param date the starting date (may be null)
     * @param months the number of months to add
     * @return the shifted date, or null
     * @see #plusMonths(LocalDate, int)
     * @see #subtractMonths(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        return date == null ? null : date.plusMonths(months);
    }

    /**
     * Alias of {@link #addMonths(LocalDate, int)} — see that method for
     * full documentation.
     *
     * @param date the starting date
     * @param months the number of months to add
     * @return the shifted date
     * @see #addMonths(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate plusMonths(LocalDate date, int months) { return addMonths(date, months); }

    /**
     * Subtracts the given number of months.
     *
     * @param date the starting date (may be null)
     * @param months the number of months to subtract
     * @return the shifted date, or null
     * @see #addMonths(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate subtractMonths(LocalDate date, int months) {
        return date == null ? null : date.minusMonths(months);
    }

    /**
     * Adds the given number of years. February 29 in a leap year shifts
     * to February 28 in a non-leap year.
     *
     * @param date the starting date (may be null)
     * @param years the number of years to add
     * @return the shifted date, or null
     * @see #plusYears(LocalDate, int)
     * @see #subtractYears(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate addYears(LocalDate date, int years) {
        return date == null ? null : date.plusYears(years);
    }

    /**
     * Alias of {@link #addYears(LocalDate, int)} — see that method for
     * full documentation.
     *
     * @param date the starting date
     * @param years the number of years to add
     * @return the shifted date
     * @see #addYears(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate plusYears(LocalDate date, int years) { return addYears(date, years); }

    /**
     * Subtracts the given number of years.
     *
     * @param date the starting date (may be null)
     * @param years the number of years to subtract
     * @return the shifted date, or null
     * @see #addYears(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate subtractYears(LocalDate date, int years) {
        return date == null ? null : date.minusYears(years);
    }

    /**
     * Adds N business days, skipping Saturday and Sunday.
     *
     * <p>Negative {@code businessDays} counts backwards. Business days are
     * counted inclusively of the endpoint — {@code addBusinessDays(Friday, 1)}
     * returns Monday (Saturday and Sunday are skipped).</p>
     *
     * @param date the starting date (may be null)
     * @param businessDays the number of business days to add (negative counts backwards)
     * @return the shifted date, or null
     * @see #addBusinessDays(LocalDate, int, DayOfWeek...)
     * @see #businessDaysAfter(LocalDate, int)
     * @see #businessDaysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static LocalDate addBusinessDays(LocalDate date, int businessDays) {
        if (date == null) return null;
        int remaining = Math.abs(businessDays);
        LocalDate cursor = date;
        int step = businessDays >= 0 ? 1 : -1;
        while (remaining > 0) {
            cursor = cursor.plusDays(step);
            DayOfWeek d = cursor.getDayOfWeek();
            if (d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY) remaining--;
        }
        return cursor;
    }

    /**
     * Adds N business days with a custom weekend definition.
     *
     * <pre>{@code
     * // Friday-Saturday weekend (many Middle Eastern countries)
     * DateTime.addBusinessDays(date, 3, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
     * }</pre>
     *
     * @param date the starting date (may be null)
     * @param businessDays the number of business days (negative counts backwards)
     * @param weekend the days to skip (null or empty → Saturday/Sunday)
     * @return the shifted date, or null
     * @see #addBusinessDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate addBusinessDays(LocalDate date, int businessDays, DayOfWeek... weekend) {
        if (date == null) return null;
        DayOfWeek[] week = weekend == null || weekend.length == 0 ? DEFAULT_WEEKEND : weekend;
        int remaining = Math.abs(businessDays);
        LocalDate cursor = date;
        int step = businessDays >= 0 ? 1 : -1;
        while (remaining > 0) {
            cursor = cursor.plusDays(step);
            if (!isWeekend(cursor, week)) remaining--;
        }
        return cursor;
    }

    /**
     * Alias of {@link #addBusinessDays(LocalDate, int)} — see that method
     * for full documentation.
     *
     * @param date the starting date
     * @param days the number of business days to add
     * @return the shifted date
     * @see #addBusinessDays(LocalDate, int)
     * @since 1.0.0
     */
    public static LocalDate businessDaysAfter(LocalDate date, int days) { return addBusinessDays(date, days); }

    /**
     * Adds a {@link Duration} to the date-time.
     *
     * @param dt the starting date-time (may be null)
     * @param d the duration to add (may be null — returns dt unchanged)
     * @return the shifted date-time, or null
     * @since 1.0.0
     */
    public static LocalDateTime addDuration(LocalDateTime dt, Duration d) {
        return dt == null || d == null ? null : dt.plus(d);
    }

    // ============================================================== snapping

    /**
     * Snaps to the Monday of the week containing the date. Weeks start on
     * Monday (ISO-8601 convention).
     *
     * @param date the date (may be null)
     * @return the Monday of that week, or null
     * @see #endOfWeek(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate startOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Snaps to the Sunday of the week containing the date.
     *
     * @param date the date (may be null)
     * @return the Sunday of that week, or null
     * @see #startOfWeek(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate endOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Snaps to the first day of the month containing the date.
     *
     * @param date the date (may be null)
     * @return the first of the month, or null
     * @see #firstOfMonth(LocalDate)
     * @see #endOfMonth(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date == null ? null : date.withDayOfMonth(1);
    }

    /**
     * Alias of {@link #startOfMonth(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the first of the month
     * @see #startOfMonth(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate firstOfMonth(LocalDate date) { return startOfMonth(date); }

    /**
     * Snaps to the last day of the month containing the date.
     *
     * @param date the date (may be null)
     * @return the last day of the month, or null
     * @see #lastOfMonth(LocalDate)
     * @see #startOfMonth(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Alias of {@link #endOfMonth(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the last day of the month
     * @see #endOfMonth(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate lastOfMonth(LocalDate date) { return endOfMonth(date); }

    /**
     * Snaps to January 1 of the year containing the date.
     *
     * @param date the date (may be null)
     * @return January 1 of that year, or null
     * @see #firstOfYear(LocalDate)
     * @see #endOfYear(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date == null ? null : date.withDayOfYear(1);
    }

    /**
     * Alias of {@link #startOfYear(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return January 1 of that year
     * @see #startOfYear(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate firstOfYear(LocalDate date) { return startOfYear(date); }

    /**
     * Snaps to December 31 of the year containing the date.
     *
     * @param date the date (may be null)
     * @return December 31 of that year, or null
     * @see #lastOfYear(LocalDate)
     * @see #startOfYear(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date == null ? null : date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * Alias of {@link #endOfYear(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return December 31 of that year
     * @see #endOfYear(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate lastOfYear(LocalDate date) { return endOfYear(date); }

    /**
     * Snaps to the first day of the quarter (Jan/Apr/Jul/Oct 1).
     *
     * @param date the date (may be null)
     * @return the first day of the quarter, or null
     * @see #endOfQuarter(LocalDate)
     * @see #quarter(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate startOfQuarter(LocalDate date) {
        if (date == null) return null;
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), month, 1);
    }

    /**
     * Snaps to the last day of the quarter (Mar/Jun/Sep/Dec 31).
     *
     * @param date the date (may be null)
     * @return the last day of the quarter, or null
     * @see #startOfQuarter(LocalDate)
     * @since 1.0.0
     */
    public static LocalDate endOfQuarter(LocalDate date) {
        if (date == null) return null;
        return endOfMonth(startOfQuarter(date).plusMonths(2));
    }

    // ============================================================== differences

    /**
     * Returns the number of days between two dates — {@code to - from}.
     *
     * <p>Result is negative if {@code to < from}.</p>
     *
     * @param from the start date (may be null — returns 0)
     * @param to the end date (may be null — returns 0)
     * @return the signed day difference
     * @see #diffDays(LocalDate, LocalDate)
     * @see #daysDiff(LocalDate, LocalDate)
     * @see #spanDays(LocalDate, LocalDate)
     * @see #businessDaysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long daysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.DAYS.between(from, to);
    }

    /**
     * Alias of {@link #daysBetween(LocalDate, LocalDate)} — see that
     * method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed day difference
     * @see #daysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long diffDays(LocalDate from, LocalDate to) { return daysBetween(from, to); }

    /**
     * Alias of {@link #daysBetween(LocalDate, LocalDate)} — see that
     * method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed day difference
     * @see #daysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long daysDiff(LocalDate from, LocalDate to) { return daysBetween(from, to); }

    /**
     * Alias of {@link #daysBetween(LocalDate, LocalDate)} — see that
     * method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed day difference
     * @see #daysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long spanDays(LocalDate from, LocalDate to) { return daysBetween(from, to); }

    /**
     * Returns the number of complete weeks between two dates.
     *
     * @param from the start date (may be null)
     * @param to the end date (may be null)
     * @return the signed week difference
     * @since 1.0.0
     */
    public static long weeksBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.WEEKS.between(from, to);
    }

    /**
     * Returns the number of complete months between two dates.
     *
     * @param from the start date (may be null)
     * @param to the end date (may be null)
     * @return the signed month difference
     * @see #diffMonths(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long monthsBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.MONTHS.between(from, to);
    }

    /**
     * Alias of {@link #monthsBetween(LocalDate, LocalDate)} — see that
     * method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed month difference
     * @see #monthsBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long diffMonths(LocalDate from, LocalDate to) { return monthsBetween(from, to); }

    /**
     * Returns the number of complete years between two dates.
     *
     * @param from the start date (may be null)
     * @param to the end date (may be null)
     * @return the signed year difference
     * @see #diffYears(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long yearsBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.YEARS.between(from, to);
    }

    /**
     * Alias of {@link #yearsBetween(LocalDate, LocalDate)} — see that
     * method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed year difference
     * @see #yearsBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static long diffYears(LocalDate from, LocalDate to) { return yearsBetween(from, to); }

    /**
     * Returns the period (years, months, days) between two dates.
     *
     * @param from the start date (may be null — returns {@link Period#ZERO})
     * @param to the end date (may be null — returns {@link Period#ZERO})
     * @return the period
     * @since 1.0.0
     */
    public static Period periodBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return Period.ZERO;
        return Period.between(from, to);
    }

    /**
     * Returns the duration between two date-times.
     *
     * @param from the start date-time (may be null — returns
     * {@link Duration#ZERO})
     * @param to the end date-time (may be null — returns {@link Duration#ZERO})
     * @return the duration
     * @see #diff(LocalDateTime, LocalDateTime)
     * @since 1.0.0
     */
    public static Duration durationBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return Duration.ZERO;
        return Duration.between(from, to);
    }

    /**
     * Alias of {@link #durationBetween(LocalDateTime, LocalDateTime)} — see
     * that method for full documentation.
     *
     * @param from the start date-time
     * @param to the end date-time
     * @return the duration
     * @see #durationBetween(LocalDateTime, LocalDateTime)
     * @since 1.0.0
     */
    public static Duration diff(LocalDateTime from, LocalDateTime to) { return durationBetween(from, to); }

    /**
     * Returns the age in years from the birthdate to today.
     *
     * @param birthDate the birthdate (may be null — returns 0)
     * @return the age in years
     * @see #calculateAge(LocalDate)
     * @see #age(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static int age(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Alias of {@link #age(LocalDate)} — see that method for full
     * documentation.
     *
     * @param birthDate the birthdate
     * @return the age in years
     * @see #age(LocalDate)
     * @since 1.0.0
     */
    public static int calculateAge(LocalDate birthDate) { return age(birthDate); }

    /**
     * Alias of {@link #age(LocalDate)} — see that method for full
     * documentation.
     *
     * @param birthDate the birthdate
     * @return the age in years
     * @see #age(LocalDate)
     * @since 1.0.0
     */
    public static int getAge(LocalDate birthDate) { return age(birthDate); }

    /**
     * Returns the age in years as of the specified date — useful for
     * computing historical ages or testing.
     *
     * @param birthDate the birthdate (may be null — returns 0)
     * @param onDate the reference date (may be null — returns 0)
     * @return the age in years
     * @see #age(LocalDate)
     * @since 1.0.0
     */
    public static int age(LocalDate birthDate, LocalDate onDate) {
        if (birthDate == null || onDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, onDate);
    }

    /**
     * Alias of {@link #age(LocalDate, LocalDate)} — see that method for
     * full documentation.
     *
     * @param birthDate the birthdate
     * @param onDate the reference date
     * @return the age in years
     * @see #age(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static int calculateAge(LocalDate birthDate, LocalDate onDate) { return age(birthDate, onDate); }

    // ============================================================== business days

    /**
     * Returns the number of business days (Mon–Fri) between two dates,
     * not including the end date. Result is negative if {@code to < from}.
     *
     * @param from the start date (may be null — returns 0)
     * @param to the end date (may be null — returns 0)
     * @return the signed business-day count
     * @see #businessDaysUntil(LocalDate, LocalDate)
     * @see #businessDaysBetween(LocalDate, LocalDate, DayOfWeek...)
     * @see #addBusinessDays(LocalDate, int)
     * @since 1.0.0
     */
    public static int businessDaysBetween(LocalDate from, LocalDate to) {
        return businessDaysBetween(from, to, DEFAULT_WEEKEND);
    }

    /**
     * Alias of {@link #businessDaysBetween(LocalDate, LocalDate)} — see
     * that method for full documentation.
     *
     * @param from the start date
     * @param to the end date
     * @return the signed business-day count
     * @see #businessDaysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static int businessDaysUntil(LocalDate from, LocalDate to) { return businessDaysBetween(from, to); }

    /**
     * Returns the number of business days between two dates with a custom
     * weekend definition.
     *
     * <p>Efficient implementation: full weeks contribute {@code 7 - weekend.length}
     * business days each, then the remaining partial week is counted
     * day-by-day.</p>
     *
     * @param from the start date (may be null — returns 0)
     * @param to the end date (may be null — returns 0)
     * @param weekend the days to skip (null or empty → Saturday/Sunday)
     * @return the signed business-day count
     * @see #businessDaysBetween(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static int businessDaysBetween(LocalDate from, LocalDate to, DayOfWeek... weekend) {
        if (from == null || to == null) {
            Trace.trace("datetime", "businessDaysBetween: null input -> 0");
            return 0;
        }
        if (from.isAfter(to)) {
            Trace.trace("datetime", "businessDaysBetween: reversed order");
            return -businessDaysBetween(to, from, weekend);
        }
        DayOfWeek[] week = weekend == null || weekend.length == 0 ? DEFAULT_WEEKEND : weekend;

        int totalDays = (int) ChronoUnit.DAYS.between(from, to);
        if (totalDays == 0) return 0;

        int fullWeeks = totalDays / 7;
        int businessDays = fullWeeks * (7 - week.length);

        LocalDate cursor = from.plusDays((long) fullWeeks * 7);
        int remainder = totalDays - fullWeeks * 7;
        for (int i = 0; i < remainder; i++) {
            if (!isWeekend(cursor, week)) businessDays++;
            cursor = cursor.plusDays(1);
        }
        return businessDays;
    }

    // ============================================================== names / digits

    /**
     * Returns the full day-of-week name — {@code "Saturday"}.
     *
     * @param date the date (may be null — returns empty)
     * @return the day name
     * @see #shortDayName(LocalDate)
     * @since 1.0.0
     */
    public static String dayName(LocalDate date) {
        if (date == null) return "";
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
    }

    /**
     * Returns the abbreviated day-of-week name — {@code "Sat"}.
     *
     * @param date the date (may be null — returns empty)
     * @return the short day name
     * @see #dayName(LocalDate)
     * @since 1.0.0
     */
    public static String shortDayName(LocalDate date) {
        if (date == null) return "";
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }

    /**
     * Returns the full month name — {@code "October"}.
     *
     * @param date the date (may be null — returns empty)
     * @return the month name
     * @see #shortMonthName(LocalDate)
     * @since 1.0.0
     */
    public static String monthName(LocalDate date) {
        if (date == null) return "";
        return date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
    }

    /**
     * Returns the abbreviated month name — {@code "Oct"}.
     *
     * @param date the date (may be null — returns empty)
     * @return the short month name
     * @see #monthName(LocalDate)
     * @since 1.0.0
     */
    public static String shortMonthName(LocalDate date) {
        if (date == null) return "";
        return date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }

    /**
     * Returns the day-of-week as an integer — 1 for Monday, 7 for Sunday.
     *
     * @param date the date (may be null — returns 0)
     * @return the day-of-week value (1–7)
     * @since 1.0.0
     */
    public static int dayOfWeek(LocalDate date) {
        if (date == null) return 0;
        return date.getDayOfWeek().getValue();
    }

    /**
     * Returns the day-of-year — 1 for January 1, 365 for December 31
     * (366 in a leap year).
     *
     * @param date the date (may be null — returns 0)
     * @return the day-of-year
     * @since 1.0.0
     */
    public static int dayOfYear(LocalDate date) {
        return date == null ? 0 : date.getDayOfYear();
    }

    /**
     * Returns the ISO 8601 week number — 1–53.
     *
     * @param date the date (may be null — returns 0)
     * @return the ISO week number
     * @see #weekOfYear(LocalDate)
     * @since 1.0.0
     */
    public static int weekNumber(LocalDate date) {
        if (date == null) return 0;
        return date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    }

    /**
     * Alias of {@link #weekNumber(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the ISO week number
     * @see #weekNumber(LocalDate)
     * @since 1.0.0
     */
    public static int weekOfYear(LocalDate date) { return weekNumber(date); }

    /**
     * Returns the quarter — 1 for Jan–Mar, 4 for Oct–Dec.
     *
     * @param date the date (may be null — returns 0)
     * @return the quarter (1–4)
     * @see #startOfQuarter(LocalDate)
     * @see #endOfQuarter(LocalDate)
     * @since 1.0.0
     */
    public static int quarter(LocalDate date) {
        return date == null ? 0 : (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * Returns the number of days in the month of the given date.
     *
     * @param date the date (may be null — returns 0)
     * @return the days in that month
     * @see #daysInMonth(int, int)
     * @since 1.0.0
     */
    public static int daysInMonth(LocalDate date) {
        return date == null ? 0 : date.lengthOfMonth();
    }

    /**
     * Returns the number of days in the given month, or 0 for an invalid
     * year/month combination.
     *
     * @param year the year
     * @param month the month (1–12)
     * @return the days in the month, or 0 for invalid month
     * @see #daysInMonth(LocalDate)
     * @since 1.0.0
     */
    public static int daysInMonth(int year, int month) {
        if (!isValidDate(year, month, 1)) return 0;
        return LocalDate.of(year, month, 1).lengthOfMonth();
    }

    /**
     * Returns the number of days in the year — 366 for leap years, 365
     * otherwise.
     *
     * @param year the year
     * @return the days in the year
     * @see #isLeapYear(int)
     * @since 1.0.0
     */
    public static int daysInYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    // ============================================================== comparisons

    /**
     * Checks whether {@code a} is strictly before {@code b}.
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if a &lt; b
     * @see #isAfter(LocalDate, LocalDate)
     * @see #isSameDay(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isBefore(LocalDate a, LocalDate b) { return a != null && b != null && a.isBefore(b); }

    /**
     * Checks whether {@code a} is strictly after {@code b}.
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if a &gt; b
     * @see #isBefore(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isAfter(LocalDate a, LocalDate b) { return a != null && b != null && a.isAfter(b); }

    /**
     * Checks whether the two dates are the same day.
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if equal
     * @see #isSameMonth(LocalDate, LocalDate)
     * @see #isSameYear(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isSameDay(LocalDate a, LocalDate b) { return a != null && a.equals(b); }

    /**
     * Alias of {@link #isSameDay(LocalDate, LocalDate)} — see that method
     * for full documentation.
     *
     * @param a the first date
     * @param b the second date
     * @return {@code true} if equal
     * @see #isSameDay(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isSameDate(LocalDate a, LocalDate b) { return isSameDay(a, b); }

    /**
     * Checks whether the two dates are in the same month of the same year.
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if same year and month
     * @see #isSameYear(LocalDate, LocalDate)
     * @see #isSameWeek(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return a != null && b != null
                && a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    /**
     * Checks whether the two dates are in the same year.
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if same year
     * @see #isSameMonth(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static boolean isSameYear(LocalDate a, LocalDate b) {
        return a != null && b != null && a.getYear() == b.getYear();
    }

    /**
     * Checks whether the two dates are in the same ISO week (Monday–Sunday).
     *
     * @param a the first date (may be null — returns false)
     * @param b the second date (may be null — returns false)
     * @return {@code true} if same week
     * @see #startOfWeek(LocalDate)
     * @since 1.0.0
     */
    public static boolean isSameWeek(LocalDate a, LocalDate b) {
        if (a == null || b == null) return false;
        return startOfWeek(a).equals(startOfWeek(b));
    }

    /**
     * Checks whether the date falls on Saturday or Sunday.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if Saturday or Sunday
     * @see #isWeekend(LocalDate, DayOfWeek...)
     * @see #isWeekday(LocalDate)
     * @since 1.0.0
     */
    public static boolean isWeekend(LocalDate date) {
        return isWeekend(date, DEFAULT_WEEKEND);
    }

    /**
     * Alias of {@link #isWeekend(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return {@code true} if on the default weekend
     * @see #isWeekend(LocalDate)
     * @since 1.0.0
     */
    public static boolean onWeekend(LocalDate date) { return isWeekend(date); }

    /**
     * Checks whether the date falls on a custom weekend.
     *
     * <pre>{@code
     * // Friday-Saturday weekend
     * DateTime.isWeekend(date, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
     * }</pre>
     *
     * @param date the date (may be null — returns false)
     * @param weekend the days considered weekend (null or empty →
     * Saturday/Sunday)
     * @return {@code true} if on the custom weekend
     * @see #isWeekend(LocalDate)
     * @since 1.0.0
     */
    public static boolean isWeekend(LocalDate date, DayOfWeek... weekend) {
        if (date == null) return false;
        DayOfWeek[] week = weekend == null || weekend.length == 0 ? DEFAULT_WEEKEND : weekend;
        DayOfWeek d = date.getDayOfWeek();
        for (DayOfWeek w : week) if (w == d) return true;
        return false;
    }

    /**
     * Checks whether the date is <b>not</b> on the default weekend.
     *
     * <p>Returns {@code false} for a {@code null} date — "unknown" is not
     * "weekday".</p>
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if Monday–Friday
     * @see #isWeekend(LocalDate)
     * @since 1.0.0
     */
    public static boolean isWeekday(LocalDate date) { return date != null && !isWeekend(date); }

    /**
     * Alias of {@link #isWeekday(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return {@code true} if weekday
     * @see #isWeekday(LocalDate)
     * @since 1.0.0
     */
    public static boolean onWeekday(LocalDate date) { return isWeekday(date); }

    /**
     * Checks whether the date equals today.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if today
     * @see #isYesterday(LocalDate)
     * @see #isTomorrow(LocalDate)
     * @since 1.0.0
     */
    public static boolean isToday(LocalDate date) { return date != null && date.equals(LocalDate.now()); }

    /**
     * Checks whether the date is strictly before today.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if past
     * @see #isFuture(LocalDate)
     * @since 1.0.0
     */
    public static boolean isPast(LocalDate date) { return date != null && date.isBefore(LocalDate.now()); }

    /**
     * Checks whether the date is strictly after today.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if future
     * @see #isPast(LocalDate)
     * @since 1.0.0
     */
    public static boolean isFuture(LocalDate date) { return date != null && date.isAfter(LocalDate.now()); }

    /**
     * Checks whether the date is yesterday.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if yesterday
     * @see #isToday(LocalDate)
     * @see #isTomorrow(LocalDate)
     * @since 1.0.0
     */
    public static boolean isYesterday(LocalDate date) { return date != null && date.equals(yesterday()); }

    /**
     * Checks whether the date is tomorrow.
     *
     * @param date the date (may be null — returns false)
     * @return {@code true} if tomorrow
     * @see #isToday(LocalDate)
     * @see #isYesterday(LocalDate)
     * @since 1.0.0
     */
    public static boolean isTomorrow(LocalDate date) { return date != null && date.equals(tomorrow()); }

    /**
     * Compares two dates lexicographically. Null sorts first.
     *
     * @param a the first date (may be null)
     * @param b the second date (may be null)
     * @return negative, zero, or positive — matching {@link LocalDate#compareTo(LocalDate)}
     * @see #min(LocalDate, LocalDate)
     * @see #max(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static int compare(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    /**
     * Returns the earlier of the two dates. Null is treated as +∞ — so
     * {@code min(null, date)} returns {@code date}.
     *
     * @param a the first date (may be null)
     * @param b the second date (may be null)
     * @return the earlier date, or {@code null} if both are null
     * @see #minDate(LocalDate, LocalDate)
     * @see #max(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static LocalDate min(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    /**
     * Alias of {@link #min(LocalDate, LocalDate)} — see that method for
     * full documentation.
     *
     * @param a the first date
     * @param b the second date
     * @return the earlier date
     * @see #min(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static LocalDate minDate(LocalDate a, LocalDate b) { return min(a, b); }

    /**
     * Returns the later of the two dates. Null is treated as −∞ — so
     * {@code max(null, date)} returns {@code date}.
     *
     * @param a the first date (may be null)
     * @param b the second date (may be null)
     * @return the later date, or {@code null} if both are null
     * @see #maxDate(LocalDate, LocalDate)
     * @see #min(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static LocalDate max(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    /**
     * Alias of {@link #max(LocalDate, LocalDate)} — see that method for
     * full documentation.
     *
     * @param a the first date
     * @param b the second date
     * @return the later date
     * @see #max(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public static LocalDate maxDate(LocalDate a, LocalDate b) { return max(a, b); }

    /**
     * Checks whether the date falls in the inclusive range
     * {@code [start, end]}.
     *
     * @param date the date to test (may be null — returns false)
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     * @return {@code true} if {@code start <= date <= end}
     * @since 1.0.0
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) return false;
        return !date.isBefore(start) && !date.isAfter(end);
    }

    // ============================================================== relative time

    /**
     * Returns the relative time description for the date — {@code "5m ago"},
     * {@code "2h ago"}, {@code "yesterday"}.
     *
     * @param date the date (may be null — returns empty)
     * @return the relative description
     * @see #timeAgo(LocalDateTime)
     * @see #timeAgo(LocalDateTime, Object...)
     * @see #relative(LocalDate)
     * @since 1.0.0
     */
    public static String timeAgo(LocalDate date) {
        if (date == null) return "";
        return timeAgo(date.atStartOfDay());
    }

    /**
     * Returns the relative time description for the date-time.
     *
     * <p>Bands: {@code "just now"} (&lt;60s), {@code "Nm ago"} (&lt;60m),
     * {@code "Nh ago"} (&lt;24h), {@code "yesterday"} (1d),
     * {@code "Nd ago"} (&lt;7d), {@code "Nw ago"} (&lt;5w),
     * {@code "Nmo ago"} (&lt;12mo), {@code "Ny ago"}.</p>
     *
     * @param dateTime the date-time (may be null — returns empty)
     * @return the relative description
     * @see #timeAgo(LocalDate)
     * @see #relativeTime(LocalDateTime)
     * @since 1.0.0
     */
    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        Duration diff = Duration.between(dateTime, LocalDateTime.now());
        long seconds = diff.getSeconds();
        boolean past = seconds >= 0;
        long abs = Math.abs(seconds);

        if (abs < 60) return past ? "just now" : "in a moment";
        long minutes = abs / 60;
        if (minutes < 60) return past ? minutes + "m ago" : "in " + minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return past ? hours + "h ago" : "in " + hours + "h";
        long days = hours / 24;
        if (days == 1) return past ? "yesterday" : "tomorrow";
        if (days < 7) return past ? days + "d ago" : "in " + days + "d";
        long weeks = days / 7;
        if (weeks < 5) return past ? weeks + "w ago" : "in " + weeks + "w";
        long months = days / 30;
        if (months < 12) return past ? months + "mo ago" : "in " + months + "mo";
        long years = days / 365;
        return past ? years + "y ago" : "in " + years + "y";
    }

    /**
     * Alias of {@link #timeAgo(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param dateTime the date-time
     * @return the relative description
     * @see #timeAgo(LocalDateTime)
     * @since 1.0.0
     */
    public static String relativeTime(LocalDateTime dateTime) { return timeAgo(dateTime); }

    /**
     * Alias of {@link #timeAgo(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date
     * @return the relative description
     * @see #timeAgo(LocalDate)
     * @since 1.0.0
     */
    public static String relative(LocalDate date) { return timeAgo(date); }

    /**
     * Returns the relative time description with an explicit reference
     * date-time — useful for testing.
     *
     * @param dateTime the date-time (may be null — returns empty)
     * @param keyValues alternating {@link Key} → value pairs (only
     * {@link Key#REFERENCE} as a {@link LocalDateTime} is consumed)
     * @return the relative description
     * @see #timeAgo(LocalDateTime)
     * @since 1.0.0
     */
    public static String timeAgo(LocalDateTime dateTime, Object... keyValues) {
        if (dateTime == null) return "";
        LocalDateTime reference = LocalDateTime.now();
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.REFERENCE && keyValues[i + 1] instanceof LocalDateTime ref) {
                    reference = ref;
                }
            }
        }
        long seconds = Duration.between(dateTime, reference).getSeconds();
        boolean past = seconds >= 0;
        long abs = Math.abs(seconds);

        if (abs < 60) return past ? "just now" : "in a moment";
        long minutes = abs / 60;
        if (minutes < 60) return past ? minutes + "m ago" : "in " + minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return past ? hours + "h ago" : "in " + hours + "h";
        long days = hours / 24;
        if (days == 1) return past ? "yesterday" : "tomorrow";
        if (days < 7) return past ? days + "d ago" : "in " + days + "d";
        long weeks = days / 7;
        if (weeks < 5) return past ? weeks + "w ago" : "in " + weeks + "w";
        long months = days / 30;
        if (months < 12) return past ? months + "mo ago" : "in " + months + "mo";
        long years = days / 365;
        return past ? years + "y ago" : "in " + years + "y";
    }

    // ============================================================== epoch / instant

    /**
     * Converts the date-time to epoch milliseconds in the JVM's default
     * time zone.
     *
     * @param dateTime the date-time (may be null — returns 0)
     * @return the epoch milliseconds
     * @see #toEpochMillis(LocalDate)
     * @see #fromEpochMillis(long)
     * @since 1.0.0
     */
    public static long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) return 0;
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts the date (at start of day) to epoch milliseconds in the
     * JVM's default time zone.
     *
     * @param date the date (may be null — returns 0)
     * @return the epoch milliseconds
     * @since 1.0.0
     */
    public static long toEpochMillis(LocalDate date) {
        if (date == null) return 0;
        return toEpochMillis(date.atStartOfDay());
    }

    /**
     * Converts the date-time to epoch seconds in the JVM's default time
     * zone.
     *
     * @param dateTime the date-time (may be null — returns 0)
     * @return the epoch seconds
     * @since 1.0.0
     */
    public static long toEpochSeconds(LocalDateTime dateTime) {
        if (dateTime == null) return 0;
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * Converts epoch milliseconds back to a date-time in the JVM's default
     * time zone.
     *
     * @param millis the epoch milliseconds
     * @return the date-time
     * @see #dateFromEpochMillis(long)
     * @since 1.0.0
     */
    public static LocalDateTime fromEpochMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * Converts epoch milliseconds to a date (dropping the time part).
     *
     * @param millis the epoch milliseconds
     * @return the date
     * @see #fromEpochMillis(long)
     * @since 1.0.0
     */
    public static LocalDate dateFromEpochMillis(long millis) {
        return fromEpochMillis(millis).toLocalDate();
    }

    // ============================================================== integration

    /**
     * Returns the age bracket label for a given age — {@code "infant"},
     * {@code "child"}, {@code "teenager"}, {@code "adult"}, {@code "senior"}.
     *
     * <pre>{@code
     * DateTime.ageBracket(0);    // "infant"
     * DateTime.ageBracket(8);    // "child"
     * DateTime.ageBracket(15);   // "teenager"
     * DateTime.ageBracket(30);   // "adult"
     * DateTime.ageBracket(70);   // "senior"
     * DateTime.ageBracket(-1);   // "unknown"
     * }</pre>
     *
     * @param age the age in years
     * @return the bracket label
     * @see #age(LocalDate)
     * @see #ageFromString(String)
     * @since 1.0.0
     */
    public static String ageBracket(int age) {
        if (age < 0) return "unknown";
        if (age < 2) return "infant";
        if (age < 13) return "child";
        if (age < 20) return "teenager";
        if (age < 60) return "adult";
        return "senior";
    }

    /**
     * Parses the birthdate and returns the age in years — {@code -1} if
     * the string is unparseable.
     *
     * @param birthDate the birthdate string (may be null or invalid)
     * @return the age, or -1 if the string is unparseable
     * @see #age(LocalDate)
     * @see #parseDate(String)
     * @since 1.0.0
     */
    public static int ageFromString(String birthDate) {
        LocalDate d = parseDate(birthDate);
        return d == null ? -1 : age(d);
    }
}
