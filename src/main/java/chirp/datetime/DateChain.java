package chirp.datetime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Immutable fluent chain over a {@link LocalDate}. Every method returns a new
 * {@code DateChain} — a live demonstration of the <b>Decorator pattern</b> and
 * the <b>Builder pattern</b> in one class.
 *
 * <h2>Aliases per operation</h2>
 * <ul>
 *   <li>{@code addDays / plusDays / daysAfter}</li>
 *   <li>{@code subtractDays / minusDays / daysBefore}</li>
 *   <li>{@code startOfMonth / firstOfMonth}, {@code endOfMonth / lastOfMonth}</li>
 *   <li>{@code asDate / toDate / get}</li>
 * </ul>
 *
 * <pre>{@code
 * LocalDate endOfNextMonth = DateTime.of(LocalDate.now())
 *     .addMonths(1)
 *     .endOfMonth()
 *     .asDate();
 * }</pre>
 */
public final class DateChain {

    private final LocalDate date;

    private DateChain(LocalDate date) {
        this.date = date == null ? LocalDate.now() : date;
    }

    // ============================================================== factories

    public static DateChain of(LocalDate date) { return new DateChain(date); }
    public static DateChain on(LocalDate date) { return of(date); }
    public static DateChain from(LocalDate date) { return of(date); }
    public static DateChain start(LocalDate date) { return of(date); }

    public static DateChain of(LocalDateTime dt) { return new DateChain(dt == null ? null : dt.toLocalDate()); }
    public static DateChain on(LocalDateTime dt) { return of(dt); }

    public static DateChain of(String dateString) { return new DateChain(DateTime.parseDate(dateString)); }
    public static DateChain parse(String dateString) { return of(dateString); }

    public static DateChain today() { return of(LocalDate.now()); }
    public static DateChain now() { return of(LocalDate.now()); }

    public static DateChain ofEpochMillis(long ms) { return of(DateTime.dateFromEpochMillis(ms)); }

    // ============================================================== arithmetic (add)

    public DateChain addDays(int n) { return new DateChain(date.plusDays(n)); }
    public DateChain plusDays(int n) { return addDays(n); }
    public DateChain daysAfter(int n) { return addDays(n); }

    public DateChain addWeeks(int n) { return new DateChain(date.plusWeeks(n)); }
    public DateChain addMonths(int n) { return new DateChain(date.plusMonths(n)); }
    public DateChain plusMonths(int n) { return addMonths(n); }
    public DateChain addYears(int n) { return new DateChain(date.plusYears(n)); }
    public DateChain plusYears(int n) { return addYears(n); }

    public DateChain addBusinessDays(int n) { return new DateChain(DateTime.addBusinessDays(date, n)); }
    public DateChain businessDaysAfter(int n) { return addBusinessDays(n); }

    // ============================================================== arithmetic (subtract)

    public DateChain subtractDays(int n) { return new DateChain(date.minusDays(n)); }
    public DateChain minusDays(int n) { return subtractDays(n); }
    public DateChain daysBefore(int n) { return subtractDays(n); }

    public DateChain subtractWeeks(int n) { return new DateChain(date.minusWeeks(n)); }
    public DateChain subtractMonths(int n) { return new DateChain(date.minusMonths(n)); }
    public DateChain minusMonths(int n) { return subtractMonths(n); }
    public DateChain subtractYears(int n) { return new DateChain(date.minusYears(n)); }

    // ============================================================== setters

    public DateChain withDay(int day) { return new DateChain(date.withDayOfMonth(clampDay(day))); }
    public DateChain dayOfMonth(int day) { return withDay(day); }

    public DateChain withMonth(int month) { return new DateChain(date.withMonth(Math.max(1, Math.min(12, month)))); }
    public DateChain month(int month) { return withMonth(month); }

    public DateChain withYear(int year) { return new DateChain(date.withYear(year)); }
    public DateChain year(int year) { return withYear(year); }

    private int clampDay(int day) { return Math.max(1, Math.min(date.lengthOfMonth(), day)); }

    // ============================================================== snapping

    public DateChain startOfWeek() { return new DateChain(DateTime.startOfWeek(date)); }
    public DateChain endOfWeek() { return new DateChain(DateTime.endOfWeek(date)); }
    public DateChain startOfMonth() { return new DateChain(DateTime.startOfMonth(date)); }
    public DateChain endOfMonth() { return new DateChain(DateTime.endOfMonth(date)); }
    public DateChain startOfYear() { return new DateChain(DateTime.startOfYear(date)); }
    public DateChain endOfYear() { return new DateChain(DateTime.endOfYear(date)); }
    public DateChain startOfQuarter() { return new DateChain(DateTime.startOfQuarter(date)); }
    public DateChain endOfQuarter() { return new DateChain(DateTime.endOfQuarter(date)); }

    public DateChain firstOfMonth() { return startOfMonth(); }
    public DateChain lastOfMonth() { return endOfMonth(); }
    public DateChain firstOfYear() { return startOfYear(); }
    public DateChain lastOfYear() { return endOfYear(); }

    // ============================================================== next / previous

    public DateChain next(DayOfWeek day) { return new DateChain(date.with(TemporalAdjusters.next(day))); }
    public DateChain nextOrSame(DayOfWeek day) { return new DateChain(date.with(TemporalAdjusters.nextOrSame(day))); }
    public DateChain previous(DayOfWeek day) { return new DateChain(date.with(TemporalAdjusters.previous(day))); }
    public DateChain previousOrSame(DayOfWeek day) {
        return new DateChain(date.with(TemporalAdjusters.previousOrSame(day)));
    }

    public DateChain nextMonday() { return next(DayOfWeek.MONDAY); }
    public DateChain nextTuesday() { return next(DayOfWeek.TUESDAY); }
    public DateChain nextWednesday() { return next(DayOfWeek.WEDNESDAY); }
    public DateChain nextThursday() { return next(DayOfWeek.THURSDAY); }
    public DateChain nextFriday() { return next(DayOfWeek.FRIDAY); }
    public DateChain nextSaturday() { return next(DayOfWeek.SATURDAY); }
    public DateChain nextSunday() { return next(DayOfWeek.SUNDAY); }

    public DateChain previousMonday() { return previous(DayOfWeek.MONDAY); }
    public DateChain previousFriday() { return previous(DayOfWeek.FRIDAY); }
    public DateChain previousSunday() { return previous(DayOfWeek.SUNDAY); }

    // ============================================================== predicates

    public boolean isWeekend() { return DateTime.isWeekend(date); }
    public boolean isWeekday() { return DateTime.isWeekday(date); }
    public boolean isToday() { return DateTime.isToday(date); }
    public boolean isPast() { return DateTime.isPast(date); }
    public boolean isFuture() { return DateTime.isFuture(date); }
    public boolean isYesterday() { return DateTime.isYesterday(date); }
    public boolean isTomorrow() { return DateTime.isTomorrow(date); }

    public boolean isBefore(LocalDate other) { return DateTime.isBefore(date, other); }
    public boolean isAfter(LocalDate other) { return DateTime.isAfter(date, other); }
    public boolean isSameDay(LocalDate other) { return DateTime.isSameDay(date, other); }

    public boolean isSameMonthAs(LocalDate other) { return DateTime.isSameMonth(date, other); }
    public boolean isSameYearAs(LocalDate other) { return DateTime.isSameYear(date, other); }
    public boolean isSameWeekAs(LocalDate other) { return DateTime.isSameWeek(date, other); }

    public boolean isLeapYear() { return DateTime.isLeapYear(date.getYear()); }

    // ============================================================== terminals

    public LocalDate asDate() { return date; }
    public LocalDate toDate() { return date; }
    public LocalDate get() { return date; }
    public LocalDate value() { return date; }

    public LocalDateTime asDateTime() { return date.atStartOfDay(); }
    public LocalDateTime atStartOfDay() { return date.atStartOfDay(); }
    public LocalDateTime atTime(int hour, int minute) { return date.atTime(hour, minute); }

    public String asString() { return DateTime.humanize(date); }
    public String asHuman() { return DateTime.humanize(date); }
    public String asHumanShort() { return DateTime.humanizeShort(date); }
    public String asRelative() { return DateTime.timeAgo(date); }
    public String asISO() { return DateTime.formatISO(date); }

    public String formatted(String pattern) { return DateTime.format(date, pattern); }
    public String format(String pattern) { return formatted(pattern); }

    public long asEpochMillis() { return DateTime.toEpochMillis(date); }
    public long asTimestamp() { return asEpochMillis(); }

    // ============================================================== info

    public int dayOfWeek() { return DateTime.dayOfWeek(date); }
    public String dayName() { return DateTime.dayName(date); }
    public String shortDayName() { return DateTime.shortDayName(date); }
    public int dayOfYear() { return DateTime.dayOfYear(date); }
    public int weekNumber() { return DateTime.weekNumber(date); }
    public int quarter() { return DateTime.quarter(date); }
    public int daysInMonth() { return DateTime.daysInMonth(date); }

    public String monthName() { return DateTime.monthName(date); }
    public String shortMonthName() { return DateTime.shortMonthName(date); }

    public int age() { return DateTime.age(date); }

    public boolean isSameAs(DateChain other) {
        return other != null && this.date.equals(other.date);
    }

    @Override
    public String toString() { return asHuman(); }
}
