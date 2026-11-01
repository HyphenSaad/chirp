package chirp.datetime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Immutable fluent chainer over a {@link LocalDate}.
 *
 * <p>{@code DateChain} wraps a single {@code LocalDate}. Every method
 * returns a <b>new</b> chain wrapping a different date — so the chain is
 * immutable and thread-safe, and the date inside a chain instance never
 * changes after construction.</p>
 *
 * <h2>Why this class exists</h2>
 * <p>Chirp's {@link DateTime} class exposes date arithmetic as static
 * methods, fine for a single operation:</p>
 * <pre>{@code
 * LocalDate endOfNextMonth = DateTime.endOfMonth(
 *     DateTime.addMonths(DateTime.today(), 1));
 * }</pre>
 * <p>…but hard to read once there are more than two operations. {@code
 * DateChain} lets the same date manipulations read left-to-right:</p>
 * <pre>{@code
 * LocalDate endOfNextMonth = DateTime.of(LocalDate.now())
 *     .addMonths(1)
 *     .endOfMonth()
 *     .asDate();
 * }</pre>
 *
 * <h2>The Decorator pattern</h2>
 * <p>{@code DateChain} demonstrates the <b>Decorator</b> design pattern
 * in the same structure as {@link chirp.text.TextChain},
 * {@link chirp.number.NumberChain}, and {@link chirp.collection.ArrayChain}
 * — each method returns a new wrapper. The chain itself is immutable;
 * the wrapped {@code LocalDate} is a Java 8 value type and is already
 * immutable, so no defensive copying is needed at the boundary.</p>
 *
 * <h2>Null safety</h2>
 * <p>{@link #of(LocalDate)} treats a {@code null} date as "today" — so a
 * chain is <b>always</b> initialized to a valid date and never becomes
 * {@code null} after construction. Every transform returns a new chain
 * wrapping a valid date; every terminal returns a valid
 * {@code LocalDate} or string. There is no null-propagation in this
 * module — a chain that starts from a null date behaves as if it started
 * from today.</p>
 *
 * <p><b>Why the "null = today" choice?</b> Date arithmetic has no natural
 * "return null back" behaviour the way {@code TextChain} does. Returning
 * today is the safest interpretation: a chain left uninitialised still
 * has a working state, and a student can never crash the program by
 * forgetting to pass a date.</p>
 *
 * <h2>Day-navigation helpers</h2>
 * <p>Finding "the next Monday" or "the previous Friday" without writing
 * {@link TemporalAdjusters} calls is a common task. {@code DateChain}
 * exposes every day of the week:</p>
 * <ul>
 *   <li>{@code nextMonday}, {@code nextTuesday}, … {@code nextSunday}</li>
 *   <li>{@code previousMonday}, {@code previousFriday}, {@code previousSunday}</li>
 *   <li>Generic: {@code next(DayOfWeek)}, {@code previous(DayOfWeek)}</li>
 * </ul>
 * <p>Also {@code nextOrSame(day)} and {@code previousOrSame(day)}, which
 * return the current date if it already falls on the given day.</p>
 *
 * <h2>Example — invoice due date</h2>
 * <pre>{@code
 * LocalDate invoiceDate = LocalDate.of(2026, 10, 31);   // Saturday
 * LocalDate dueDate = DateTime.of(invoiceDate)
 *     .addBusinessDays(30)
 *     .nextMonday()
 *     .asDate();
 * // 30 business days after Sat 31 Oct 2026, then shifted to the
 * // following Monday if it lands on a weekend.
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For static operations, see {@link DateTime}. For the fluent text
 * equivalent, see {@link chirp.text.TextChain}. For fluent numeric
 * chains, see {@link chirp.number.NumberChain}. For fluent array
 * chains, see {@link chirp.collection.ArrayChain}.</p>
 *
 * @see DateTime
 * @see chirp.text.TextChain
 * @see chirp.number.NumberChain
 * @see chirp.collection.ArrayChain
 * @since 1.0.0
 */
public final class DateChain {

    private final LocalDate date;

    private DateChain(LocalDate date) {
        this.date = date == null ? LocalDate.now() : date;
    }

    // ============================================================== factories

    /**
     * Wraps the date in a new chain.
     *
     * @param date the date to wrap (may be null — treated as today)
     * @return a new {@code DateChain} wrapping {@code date}
     * @see #on(LocalDate)
     * @see #from(LocalDate)
     * @see #start(LocalDate)
     * @see #of(LocalDateTime)
     * @see #of(String)
     * @see #today()
     * @since 1.0.0
     */
    public static DateChain of(LocalDate date) { return new DateChain(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain on(LocalDate date) { return of(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain from(LocalDate date) { return of(date); }

    /**
     * Alias of {@link #of(LocalDate)} — see that method for full
     * documentation.
     *
     * @param date the date to wrap
     * @return a new {@code DateChain}
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain start(LocalDate date) { return of(date); }

    /**
     * Wraps the date part of a date-time.
     *
     * @param dt the date-time (may be null — treated as today)
     * @return a new {@code DateChain} wrapping the date part
     * @see #on(LocalDateTime)
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DateChain of(LocalDateTime dt) { return new DateChain(dt == null ? null : dt.toLocalDate()); }

    /**
     * Alias of {@link #of(LocalDateTime)} — see that method for full
     * documentation.
     *
     * @param dt the date-time
     * @return a new {@code DateChain}
     * @see #of(LocalDateTime)
     * @since 1.0.0
     */
    public static DateChain on(LocalDateTime dt) { return of(dt); }

    /**
     * Parses the string and wraps the result. If the string is
     * unparseable, falls back to today — no exception is thrown.
     *
     * <pre>{@code
     * DateTime.of("2026-10-31").addDays(1).asDate();   // 2026-11-01
     * DateTime.of("not-a-date").asDate();               // today
     * }</pre>
     *
     * @param dateString the date string (may be null or invalid)
     * @return a new {@code DateChain} wrapping the parsed date, or today
     * if parsing fails
     * @see DateTime#parseDate(String)
     * @see #parse(String)
     * @since 1.0.0
     */
    public static DateChain of(String dateString) { return new DateChain(DateTime.parseDate(dateString)); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param dateString the date string
     * @return a new {@code DateChain}
     * @see #of(String)
     * @since 1.0.0
     */
    public static DateChain parse(String dateString) { return of(dateString); }

    /**
     * Opens a chain on today's date.
     *
     * @return a new {@code DateChain} wrapping today
     * @see #now()
     * @since 1.0.0
     */
    public static DateChain today() { return of(LocalDate.now()); }

    /**
     * Alias of {@link #today()} — see that method for full documentation.
     *
     * @return a new {@code DateChain} wrapping today
     * @see #today()
     * @since 1.0.0
     */
    public static DateChain now() { return of(LocalDate.now()); }

    /**
     * Opens a chain on the date corresponding to the given epoch
     * milliseconds.
     *
     * @param ms the epoch milliseconds
     * @return a new {@code DateChain} wrapping the corresponding date
     * @see DateTime#dateFromEpochMillis(long)
     * @since 1.0.0
     */
    public static DateChain ofEpochMillis(long ms) { return of(DateTime.dateFromEpochMillis(ms)); }

    // ============================================================== arithmetic (add)

    /**
     * Adds {@code n} days. Negative values subtract.
     *
     * @param n the number of days to add
     * @return a new chain with the shifted date
     * @see #plusDays(int)
     * @see #daysAfter(int)
     * @see #subtractDays(int)
     * @see DateTime#addDays(LocalDate, int)
     * @since 1.0.0
     */
    public DateChain addDays(int n) { return new DateChain(date.plusDays(n)); }

    /**
     * Alias of {@link #addDays(int)} — see that method for full
     * documentation.
     *
     * @param n the number of days to add
     * @return a new chain with the shifted date
     * @see #addDays(int)
     * @since 1.0.0
     */
    public DateChain plusDays(int n) { return addDays(n); }

    /**
     * Alias of {@link #addDays(int)} — see that method for full
     * documentation.
     *
     * @param n the number of days to add
     * @return a new chain with the shifted date
     * @see #addDays(int)
     * @since 1.0.0
     */
    public DateChain daysAfter(int n) { return addDays(n); }

    /**
     * Adds {@code n} weeks.
     *
     * @param n the number of weeks to add
     * @return a new chain with the shifted date
     * @see #addWeeks(int)
     * @since 1.0.0
     */
    public DateChain addWeeks(int n) { return new DateChain(date.plusWeeks(n)); }

    /**
     * Adds {@code n} months. The day-of-month is clamped to the target
     * month's length — Jan 31 + 1 month = Feb 28 (or 29 in a leap year).
     *
     * @param n the number of months to add
     * @return a new chain with the shifted date
     * @see #plusMonths(int)
     * @see #subtractMonths(int)
     * @see DateTime#addMonths(LocalDate, int)
     * @since 1.0.0
     */
    public DateChain addMonths(int n) { return new DateChain(date.plusMonths(n)); }

    /**
     * Alias of {@link #addMonths(int)} — see that method for full
     * documentation.
     *
     * @param n the number of months to add
     * @return a new chain with the shifted date
     * @see #addMonths(int)
     * @since 1.0.0
     */
    public DateChain plusMonths(int n) { return addMonths(n); }

    /**
     * Adds {@code n} years. Feb 29 in a leap year shifts to Feb 28 in a
     * non-leap year.
     *
     * @param n the number of years to add
     * @return a new chain with the shifted date
     * @see #plusYears(int)
     * @see #subtractYears(int)
     * @see DateTime#addYears(LocalDate, int)
     * @since 1.0.0
     */
    public DateChain addYears(int n) { return new DateChain(date.plusYears(n)); }

    /**
     * Alias of {@link #addYears(int)} — see that method for full
     * documentation.
     *
     * @param n the number of years to add
     * @return a new chain with the shifted date
     * @see #addYears(int)
     * @since 1.0.0
     */
    public DateChain plusYears(int n) { return addYears(n); }

    /**
     * Adds N business days, skipping Saturday and Sunday.
     *
     * @param n the number of business days (negative counts backwards)
     * @return a new chain with the shifted date
     * @see #businessDaysAfter(int)
     * @see DateTime#addBusinessDays(LocalDate, int)
     * @since 1.0.0
     */
    public DateChain addBusinessDays(int n) { return new DateChain(DateTime.addBusinessDays(date, n)); }

    /**
     * Alias of {@link #addBusinessDays(int)} — see that method for full
     * documentation.
     *
     * @param n the number of business days
     * @return a new chain with the shifted date
     * @see #addBusinessDays(int)
     * @since 1.0.0
     */
    public DateChain businessDaysAfter(int n) { return addBusinessDays(n); }

    // ============================================================== arithmetic (subtract)

    /**
     * Subtracts {@code n} days.
     *
     * @param n the number of days to subtract
     * @return a new chain with the shifted date
     * @see #minusDays(int)
     * @see #daysBefore(int)
     * @see #addDays(int)
     * @since 1.0.0
     */
    public DateChain subtractDays(int n) { return new DateChain(date.minusDays(n)); }

    /**
     * Alias of {@link #subtractDays(int)} — see that method for full
     * documentation.
     *
     * @param n the number of days to subtract
     * @return a new chain with the shifted date
     * @see #subtractDays(int)
     * @since 1.0.0
     */
    public DateChain minusDays(int n) { return subtractDays(n); }

    /**
     * Alias of {@link #subtractDays(int)} — see that method for full
     * documentation.
     *
     * @param n the number of days to subtract
     * @return a new chain with the shifted date
     * @see #subtractDays(int)
     * @since 1.0.0
     */
    public DateChain daysBefore(int n) { return subtractDays(n); }

    /**
     * Subtracts {@code n} weeks.
     *
     * @param n the number of weeks to subtract
     * @return a new chain with the shifted date
     * @see #addWeeks(int)
     * @since 1.0.0
     */
    public DateChain subtractWeeks(int n) { return new DateChain(date.minusWeeks(n)); }

    /**
     * Subtracts {@code n} months.
     *
     * @param n the number of months to subtract
     * @return a new chain with the shifted date
     * @see #addMonths(int)
     * @see #minusMonths(int)
     * @since 1.0.0
     */
    public DateChain subtractMonths(int n) { return new DateChain(date.minusMonths(n)); }

    /**
     * Alias of {@link #subtractMonths(int)} — see that method for full
     * documentation.
     *
     * @param n the number of months to subtract
     * @return a new chain with the shifted date
     * @see #subtractMonths(int)
     * @since 1.0.0
     */
    public DateChain minusMonths(int n) { return subtractMonths(n); }

    /**
     * Subtracts {@code n} years.
     *
     * @param n the number of years to subtract
     * @return a new chain with the shifted date
     * @see #addYears(int)
     * @since 1.0.0
     */
    public DateChain subtractYears(int n) { return new DateChain(date.minusYears(n)); }

    // ============================================================== setters

    /**
     * Returns a new chain with the day-of-month set to {@code day}. The
     * day is clamped to the current month's length — setting day 31 in
     * February gives day 28 (or 29 in a leap year).
     *
     * @param day the day-of-month
     * @return a new chain with the adjusted date
     * @see #dayOfMonth(int)
     * @since 1.0.0
     */
    public DateChain withDay(int day) { return new DateChain(date.withDayOfMonth(clampDay(day))); }

    /**
     * Alias of {@link #withDay(int)} — see that method for full
     * documentation.
     *
     * @param day the day-of-month
     * @return a new chain with the adjusted date
     * @see #withDay(int)
     * @since 1.0.0
     */
    public DateChain dayOfMonth(int day) { return withDay(day); }

    /**
     * Returns a new chain with the month set to {@code month} (1–12).
     * Values outside that range are clamped.
     *
     * @param month the month (1 = January, 12 = December)
     * @return a new chain with the adjusted date
     * @see #month(int)
     * @since 1.0.0
     */
    public DateChain withMonth(int month) { return new DateChain(date.withMonth(Math.max(1, Math.min(12, month)))); }

    /**
     * Alias of {@link #withMonth(int)} — see that method for full
     * documentation.
     *
     * @param month the month
     * @return a new chain with the adjusted date
     * @see #withMonth(int)
     * @since 1.0.0
     */
    public DateChain month(int month) { return withMonth(month); }

    /**
     * Returns a new chain with the year set to {@code year}.
     *
     * @param year the year
     * @return a new chain with the adjusted date
     * @see #year(int)
     * @since 1.0.0
     */
    public DateChain withYear(int year) { return new DateChain(date.withYear(year)); }

    /**
     * Alias of {@link #withYear(int)} — see that method for full
     * documentation.
     *
     * @param year the year
     * @return a new chain with the adjusted date
     * @see #withYear(int)
     * @since 1.0.0
     */
    public DateChain year(int year) { return withYear(year); }

    private int clampDay(int day) { return Math.max(1, Math.min(date.lengthOfMonth(), day)); }

    // ============================================================== snapping

    /**
     * Snaps to the Monday of the current week.
     *
     * @return a new chain with the Monday of that week
     * @see #endOfWeek()
     * @see DateTime#startOfWeek(LocalDate)
     * @since 1.0.0
     */
    public DateChain startOfWeek() { return new DateChain(DateTime.startOfWeek(date)); }

    /**
     * Snaps to the Sunday of the current week.
     *
     * @return a new chain with the Sunday of that week
     * @see #startOfWeek()
     * @since 1.0.0
     */
    public DateChain endOfWeek() { return new DateChain(DateTime.endOfWeek(date)); }

    /**
     * Snaps to the first day of the current month.
     *
     * @return a new chain with the first of the month
     * @see #firstOfMonth()
     * @see #endOfMonth()
     * @since 1.0.0
     */
    public DateChain startOfMonth() { return new DateChain(DateTime.startOfMonth(date)); }

    /**
     * Snaps to the last day of the current month.
     *
     * @return a new chain with the last of the month
     * @see #lastOfMonth()
     * @see #startOfMonth()
     * @since 1.0.0
     */
    public DateChain endOfMonth() { return new DateChain(DateTime.endOfMonth(date)); }

    /**
     * Snaps to January 1 of the current year.
     *
     * @return a new chain with January 1 of that year
     * @see #firstOfYear()
     * @see #endOfYear()
     * @since 1.0.0
     */
    public DateChain startOfYear() { return new DateChain(DateTime.startOfYear(date)); }

    /**
     * Snaps to December 31 of the current year.
     *
     * @return a new chain with December 31 of that year
     * @see #lastOfYear()
     * @see #startOfYear()
     * @since 1.0.0
     */
    public DateChain endOfYear() { return new DateChain(DateTime.endOfYear(date)); }

    /**
     * Snaps to the first day of the current quarter.
     *
     * @return a new chain with the first of the quarter
     * @see #endOfQuarter()
     * @since 1.0.0
     */
    public DateChain startOfQuarter() { return new DateChain(DateTime.startOfQuarter(date)); }

    /**
     * Snaps to the last day of the current quarter.
     *
     * @return a new chain with the last of the quarter
     * @see #startOfQuarter()
     * @since 1.0.0
     */
    public DateChain endOfQuarter() { return new DateChain(DateTime.endOfQuarter(date)); }

    /**
     * Alias of {@link #startOfMonth()} — see that method for full
     * documentation.
     *
     * @return a new chain with the first of the month
     * @see #startOfMonth()
     * @since 1.0.0
     */
    public DateChain firstOfMonth() { return startOfMonth(); }

    /**
     * Alias of {@link #endOfMonth()} — see that method for full
     * documentation.
     *
     * @return a new chain with the last of the month
     * @see #endOfMonth()
     * @since 1.0.0
     */
    public DateChain lastOfMonth() { return endOfMonth(); }

    /**
     * Alias of {@link #startOfYear()} — see that method for full
     * documentation.
     *
     * @return a new chain with January 1
     * @see #startOfYear()
     * @since 1.0.0
     */
    public DateChain firstOfYear() { return startOfYear(); }

    /**
     * Alias of {@link #endOfYear()} — see that method for full
     * documentation.
     *
     * @return a new chain with December 31
     * @see #endOfYear()
     * @since 1.0.0
     */
    public DateChain lastOfYear() { return endOfYear(); }

    // ============================================================== next / previous

    /**
     * Returns the date of the next occurrence of the given day-of-week,
     * strictly after the current date.
     *
     * @param day the day-of-week
     * @return a new chain with the next occurrence of that day
     * @see #nextOrSame(DayOfWeek)
     * @see #previous(DayOfWeek)
     * @since 1.0.0
     */
    public DateChain next(DayOfWeek day) { return new DateChain(date.with(TemporalAdjusters.next(day))); }

    /**
     * Returns the next occurrence of the day-of-week, or the current
     * date if it already falls on that day.
     *
     * @param day the day-of-week
     * @return a new chain with the next-or-same occurrence
     * @see #next(DayOfWeek)
     * @since 1.0.0
     */
    public DateChain nextOrSame(DayOfWeek day) {
        return new DateChain(date.with(TemporalAdjusters.nextOrSame(day)));
    }

    /**
     * Returns the date of the previous occurrence of the given
     * day-of-week, strictly before the current date.
     *
     * @param day the day-of-week
     * @return a new chain with the previous occurrence
     * @see #previousOrSame(DayOfWeek)
     * @see #next(DayOfWeek)
     * @since 1.0.0
     */
    public DateChain previous(DayOfWeek day) { return new DateChain(date.with(TemporalAdjusters.previous(day))); }

    /**
     * Returns the previous occurrence of the day-of-week, or the current
     * date if it already falls on that day.
     *
     * @param day the day-of-week
     * @return a new chain with the previous-or-same occurrence
     * @see #previous(DayOfWeek)
     * @since 1.0.0
     */
    public DateChain previousOrSame(DayOfWeek day) {
        return new DateChain(date.with(TemporalAdjusters.previousOrSame(day)));
    }

    /**
     * Advances to the next Monday.
     *
     * @return a new chain with the next Monday
     * @see #previousMonday()
     * @since 1.0.0
     */
    public DateChain nextMonday() { return next(DayOfWeek.MONDAY); }

    /**
     * Advances to the next Tuesday.
     *
     * @return a new chain with the next Tuesday
     * @since 1.0.0
     */
    public DateChain nextTuesday() { return next(DayOfWeek.TUESDAY); }

    /**
     * Advances to the next Wednesday.
     *
     * @return a new chain with the next Wednesday
     * @since 1.0.0
     */
    public DateChain nextWednesday() { return next(DayOfWeek.WEDNESDAY); }

    /**
     * Advances to the next Thursday.
     *
     * @return a new chain with the next Thursday
     * @since 1.0.0
     */
    public DateChain nextThursday() { return next(DayOfWeek.THURSDAY); }

    /**
     * Advances to the next Friday.
     *
     * @return a new chain with the next Friday
     * @since 1.0.0
     */
    public DateChain nextFriday() { return next(DayOfWeek.FRIDAY); }

    /**
     * Advances to the next Saturday.
     *
     * @return a new chain with the next Saturday
     * @since 1.0.0
     */
    public DateChain nextSaturday() { return next(DayOfWeek.SATURDAY); }

    /**
     * Advances to the next Sunday.
     *
     * @return a new chain with the next Sunday
     * @since 1.0.0
     */
    public DateChain nextSunday() { return next(DayOfWeek.SUNDAY); }

    /**
     * Rewinds to the previous Monday.
     *
     * @return a new chain with the previous Monday
     * @see #nextMonday()
     * @since 1.0.0
     */
    public DateChain previousMonday() { return previous(DayOfWeek.MONDAY); }

    /**
     * Rewinds to the previous Friday.
     *
     * @return a new chain with the previous Friday
     * @since 1.0.0
     */
    public DateChain previousFriday() { return previous(DayOfWeek.FRIDAY); }

    /**
     * Rewinds to the previous Sunday.
     *
     * @return a new chain with the previous Sunday
     * @since 1.0.0
     */
    public DateChain previousSunday() { return previous(DayOfWeek.SUNDAY); }

    // ============================================================== predicates

    /**
     * Checks whether the current date falls on Saturday or Sunday.
     *
     * @return {@code true} if the date is on a weekend
     * @see #isWeekday()
     * @see DateTime#isWeekend(LocalDate)
     * @since 1.0.0
     */
    public boolean isWeekend() { return DateTime.isWeekend(date); }

    /**
     * Checks whether the current date falls on a weekday (Monday–Friday).
     *
     * @return {@code true} if the date is on a weekday
     * @see #isWeekend()
     * @since 1.0.0
     */
    public boolean isWeekday() { return DateTime.isWeekday(date); }

    /**
     * Checks whether the current date is today.
     *
     * @return {@code true} if today
     * @see DateTime#isToday(LocalDate)
     * @since 1.0.0
     */
    public boolean isToday() { return DateTime.isToday(date); }

    /**
     * Checks whether the current date is strictly before today.
     *
     * @return {@code true} if past
     * @see #isFuture()
     * @since 1.0.0
     */
    public boolean isPast() { return DateTime.isPast(date); }

    /**
     * Checks whether the current date is strictly after today.
     *
     * @return {@code true} if future
     * @see #isPast()
     * @since 1.0.0
     */
    public boolean isFuture() { return DateTime.isFuture(date); }

    /**
     * Checks whether the current date is yesterday.
     *
     * @return {@code true} if yesterday
     * @see DateTime#isYesterday(LocalDate)
     * @since 1.0.0
     */
    public boolean isYesterday() { return DateTime.isYesterday(date); }

    /**
     * Checks whether the current date is tomorrow.
     *
     * @return {@code true} if tomorrow
     * @see DateTime#isTomorrow(LocalDate)
     * @since 1.0.0
     */
    public boolean isTomorrow() { return DateTime.isTomorrow(date); }

    /**
     * Checks whether the current date is before the given date.
     *
     * @param other the comparison date (may be null — returns false)
     * @return {@code true} if strictly before
     * @see #isAfter(LocalDate)
     * @see DateTime#isBefore(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public boolean isBefore(LocalDate other) { return DateTime.isBefore(date, other); }

    /**
     * Checks whether the current date is after the given date.
     *
     * @param other the comparison date (may be null — returns false)
     * @return {@code true} if strictly after
     * @see #isBefore(LocalDate)
     * @since 1.0.0
     */
    public boolean isAfter(LocalDate other) { return DateTime.isAfter(date, other); }

    /**
     * Checks whether the current date is the same day as the given date.
     *
     * @param other the comparison date
     * @return {@code true} if equal
     * @see #isSameMonthAs(LocalDate)
     * @see #isSameYearAs(LocalDate)
     * @since 1.0.0
     */
    public boolean isSameDay(LocalDate other) { return DateTime.isSameDay(date, other); }

    /**
     * Checks whether the current date is in the same month of the same
     * year as the given date.
     *
     * @param other the comparison date
     * @return {@code true} if same month/year
     * @see #isSameYearAs(LocalDate)
     * @since 1.0.0
     */
    public boolean isSameMonthAs(LocalDate other) { return DateTime.isSameMonth(date, other); }

    /**
     * Checks whether the current date is in the same year as the given
     * date.
     *
     * @param other the comparison date
     * @return {@code true} if same year
     * @see #isSameMonthAs(LocalDate)
     * @since 1.0.0
     */
    public boolean isSameYearAs(LocalDate other) { return DateTime.isSameYear(date, other); }

    /**
     * Checks whether the current date is in the same ISO week
     * (Monday–Sunday) as the given date.
     *
     * @param other the comparison date
     * @return {@code true} if same week
     * @see DateTime#isSameWeek(LocalDate, LocalDate)
     * @since 1.0.0
     */
    public boolean isSameWeekAs(LocalDate other) { return DateTime.isSameWeek(date, other); }

    /**
     * Checks whether the year of the current date is a leap year.
     *
     * @return {@code true} if leap
     * @see DateTime#isLeapYear(int)
     * @since 1.0.0
     */
    public boolean isLeapYear() { return DateTime.isLeapYear(date.getYear()); }

    // ============================================================== terminals

    /**
     * Returns the wrapped date.
     *
     * @return the wrapped {@code LocalDate}
     * @see #toDate()
     * @see #get()
     * @see #value()
     * @see #asDateTime()
     * @since 1.0.0
     */
    public LocalDate asDate() { return date; }

    /**
     * Alias of {@link #asDate()} — see that method for full
     * documentation.
     *
     * @return the wrapped {@code LocalDate}
     * @see #asDate()
     * @since 1.0.0
     */
    public LocalDate toDate() { return date; }

    /**
     * Alias of {@link #asDate()} — see that method for full
     * documentation.
     *
     * @return the wrapped {@code LocalDate}
     * @see #asDate()
     * @since 1.0.0
     */
    public LocalDate get() { return date; }

    /**
     * Alias of {@link #asDate()} — see that method for full
     * documentation.
     *
     * @return the wrapped {@code LocalDate}
     * @see #asDate()
     * @since 1.0.0
     */
    public LocalDate value() { return date; }

    /**
     * Returns the date as a {@code LocalDateTime} at start of day —
     * {@code 00:00:00}.
     *
     * @return the date-time at start of day
     * @see #atStartOfDay()
     * @see #atTime(int, int)
     * @since 1.0.0
     */
    public LocalDateTime asDateTime() { return date.atStartOfDay(); }

    /**
     * Alias of {@link #asDateTime()} — see that method for full
     * documentation.
     *
     * @return the date-time at start of day
     * @see #asDateTime()
     * @since 1.0.0
     */
    public LocalDateTime atStartOfDay() { return date.atStartOfDay(); }

    /**
     * Returns the date as a {@code LocalDateTime} at the given time.
     *
     * @param hour the hour (0–23)
     * @param minute the minute (0–59)
     * @return the date-time
     * @see #asDateTime()
     * @since 1.0.0
     */
    public LocalDateTime atTime(int hour, int minute) { return date.atTime(hour, minute); }

    /**
     * Returns a human-readable long form — {@code "Saturday, October 31, 2026"}.
     *
     * @return the humanized string
     * @see #asHumanShort()
     * @see #asRelative()
     * @see #asISO()
     * @since 1.0.0
     */
    public String asString() { return DateTime.humanize(date); }

    /**
     * Alias of {@link #asString()} — see that method for full
     * documentation.
     *
     * @return the humanized string
     * @see #asString()
     * @since 1.0.0
     */
    public String asHuman() { return DateTime.humanize(date); }

    /**
     * Returns a short human-readable form — {@code "Oct 31, 2026"}.
     *
     * @return the short humanized string
     * @see #asString()
     * @since 1.0.0
     */
    public String asHumanShort() { return DateTime.humanizeShort(date); }

    /**
     * Returns the relative time description — {@code "5m ago"},
     * {@code "yesterday"}, etc.
     *
     * @return the relative description
     * @see DateTime#timeAgo(LocalDate)
     * @since 1.0.0
     */
    public String asRelative() { return DateTime.timeAgo(date); }

    /**
     * Returns the ISO 8601 form — {@code "2026-10-31"}.
     *
     * @return the ISO string
     * @see DateTime#formatISO(LocalDate)
     * @since 1.0.0
     */
    public String asISO() { return DateTime.formatISO(date); }

    /**
     * Formats the date with the given pattern.
     *
     * @param pattern the format pattern
     * @return the formatted string
     * @see DateTime#format(LocalDate, String)
     * @since 1.0.0
     */
    public String formatted(String pattern) { return DateTime.format(date, pattern); }

    /**
     * Alias of {@link #formatted(String)} — see that method for full
     * documentation.
     *
     * @param pattern the format pattern
     * @return the formatted string
     * @see #formatted(String)
     * @since 1.0.0
     */
    public String format(String pattern) { return formatted(pattern); }

    /**
     * Returns the epoch milliseconds for this date at start of day in the
     * JVM's default time zone.
     *
     * @return the epoch milliseconds
     * @see DateTime#toEpochMillis(LocalDate)
     * @since 1.0.0
     */
    public long asEpochMillis() { return DateTime.toEpochMillis(date); }

    /**
     * Alias of {@link #asEpochMillis()} — see that method for full
     * documentation.
     *
     * @return the epoch milliseconds
     * @see #asEpochMillis()
     * @since 1.0.0
     */
    public long asTimestamp() { return asEpochMillis(); }

    // ============================================================== info

    /**
     * Returns the day-of-week as an integer — 1 for Monday, 7 for Sunday.
     *
     * @return the day-of-week value (1–7)
     * @see #dayName()
     * @since 1.0.0
     */
    public int dayOfWeek() { return DateTime.dayOfWeek(date); }

    /**
     * Returns the full day-of-week name — {@code "Saturday"}.
     *
     * @return the day name
     * @see #shortDayName()
     * @since 1.0.0
     */
    public String dayName() { return DateTime.dayName(date); }

    /**
     * Returns the abbreviated day-of-week name — {@code "Sat"}.
     *
     * @return the short day name
     * @see #dayName()
     * @since 1.0.0
     */
    public String shortDayName() { return DateTime.shortDayName(date); }

    /**
     * Returns the day-of-year — 1 for January 1, 365 for December 31
     * (366 in a leap year).
     *
     * @return the day-of-year
     * @see DateTime#dayOfYear(LocalDate)
     * @since 1.0.0
     */
    public int dayOfYear() { return DateTime.dayOfYear(date); }

    /**
     * Returns the ISO 8601 week number — 1–53.
     *
     * @return the ISO week number
     * @see DateTime#weekNumber(LocalDate)
     * @since 1.0.0
     */
    public int weekNumber() { return DateTime.weekNumber(date); }

    /**
     * Returns the quarter — 1 for Jan–Mar, 4 for Oct–Dec.
     *
     * @return the quarter (1–4)
     * @see DateTime#quarter(LocalDate)
     * @since 1.0.0
     */
    public int quarter() { return DateTime.quarter(date); }

    /**
     * Returns the number of days in the current month.
     *
     * @return the days in the month
     * @see DateTime#daysInMonth(LocalDate)
     * @since 1.0.0
     */
    public int daysInMonth() { return DateTime.daysInMonth(date); }

    /**
     * Returns the full month name — {@code "October"}.
     *
     * @return the month name
     * @see #shortMonthName()
     * @since 1.0.0
     */
    public String monthName() { return DateTime.monthName(date); }

    /**
     * Returns the abbreviated month name — {@code "Oct"}.
     *
     * @return the short month name
     * @see #monthName()
     * @since 1.0.0
     */
    public String shortMonthName() { return DateTime.shortMonthName(date); }

    /**
     * Returns the age in years from the current date to today.
     *
     * @return the age in years
     * @see DateTime#age(LocalDate)
     * @since 1.0.0
     */
    public int age() { return DateTime.age(date); }

    /**
     * Checks whether the given chain wraps the same date as this one.
     *
     * @param other the other chain (may be null — returns false)
     * @return {@code true} if the wrapped dates are equal
     * @since 1.0.0
     */
    public boolean isSameAs(DateChain other) {
        return other != null && this.date.equals(other.date);
    }

    /**
     * Alias of {@link #asString()} — see that method for full
     * documentation.
     *
     * @return the humanized string
     * @see #asString()
     * @since 1.0.0
     */
    @Override
    public String toString() { return asHuman(); }
}
