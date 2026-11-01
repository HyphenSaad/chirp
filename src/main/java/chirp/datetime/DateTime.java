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
 * Date, time, and date-time utilities — null-safe and never throws.
 *
 * <h2>Naming</h2>
 * <p>Every method has 2–4 aliases: {@code today / currentDate / nowDate};
 * {@code parseDate / readDate / dateFrom / toDate}; {@code humanize / friendly
 * / humanFormat}; {@code daysBetween / diffDays / daysDiff / spanDays};
 * {@code addDays / plusDays / daysAfter}.</p>
 *
 * <h2>Key-value</h2>
 * <p>Methods that take multiple options (parsing, formatting, weekend
 * definition) accept alternating {@link Key} → value pairs. Missing values
 * and wrong types are silently skipped.</p>
 *
 * <pre>{@code
 * DateTime.parseDate("31/10/2026");                                // dd/MM/yyyy
 * DateTime.parseDate("10-31-2026", DateTime.Key.PATTERN, "MM-dd-yyyy"); // explicit
 * DateTime.humanize(LocalDate.now(), DateTime.Key.STYLE, DateTime.Style.SHORT);
 * DateTime.isWeekend(date, DateTime.Key.WEEKEND_DAYS,
 *     new DayOfWeek[]{DayOfWeek.FRIDAY, DayOfWeek.SATURDAY});
 * }</pre>
 *
 * <h2>Fluent chains</h2>
 * <p>{@link #of(LocalDate)} opens a {@link DateChain} — immutable and
 * composable: {@code DateTime.of(today).addMonths(1).endOfMonth().asDate()}.</p>
 */
public final class DateTime {

    // ============================================================== enums

    /** Keys for the key-value overloads in this module. */
    public enum Key {
        PATTERN, LOCALE, STYLE, REFERENCE, WEEKEND_DAYS, BUSINESS_DAYS, INCLUSIVE
    }

    /** Style of a human-readable date/time string. */
    public enum Style { SHORT, MEDIUM, LONG, FULL, ISO, RELATIVE }

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

    public static LocalDate today() { return LocalDate.now(); }
    public static LocalDate currentDate() { return today(); }
    public static LocalDate nowDate() { return today(); }

    public static LocalDate yesterday() { return LocalDate.now().minusDays(1); }
    public static LocalDate tomorrow() { return LocalDate.now().plusDays(1); }
    public static LocalDate dayBefore() { return yesterday(); }
    public static LocalDate dayAfter() { return tomorrow(); }

    public static LocalTime nowTime() { return LocalTime.now().withNano(0); }
    public static LocalTime currentTime() { return nowTime(); }

    public static LocalDateTime now() { return LocalDateTime.now().withNano(0); }
    public static LocalDateTime current() { return now(); }
    public static LocalDateTime nowDateTime() { return now(); }

    // ============================================================== chains

    public static DateChain of(LocalDate date) { return DateChain.of(date); }
    public static DateChain on(LocalDate date) { return DateChain.of(date); }
    public static DateChain chain(LocalDate date) { return DateChain.of(date); }
    public static DateChain from(LocalDate date) { return DateChain.of(date); }

    public static DateChain of(LocalDateTime dateTime) { return DateChain.of(dateTime); }
    public static DateChain on(LocalDateTime dateTime) { return DateChain.of(dateTime); }

    public static DateChain of(String dateString) { return DateChain.of(dateString); }
    public static DateChain parse(String dateString) { return DateChain.of(dateString); }

    public static DateChain todayChain() { return DateChain.of(today()); }
    public static DateChain nowChain() { return DateChain.of(now()); }

    // ============================================================== parsing

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
    public static LocalDate readDate(String text) { return parseDate(text); }
    public static LocalDate dateFrom(String text) { return parseDate(text); }
    public static LocalDate toDate(String text) { return parseDate(text); }
    public static LocalDate tryParseDate(String text) { return parseDate(text); }

    public static LocalDate parseDate(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Key-value: {@code parseDate(text, Key.PATTERN, "dd/MM/yyyy", Key.LOCALE, Locale.US)}. */
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
                }
            }
        }
        if (pattern == null) return parseDate(text);
        try {
            return LocalDate.parse(text == null ? "" : text.trim(),
                    DateTimeFormatter.ofPattern(pattern, locale));
        } catch (Exception e) {
            return null;
        }
    }

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
    public static LocalTime readTime(String text) { return parseTime(text); }
    public static LocalTime timeFrom(String text) { return parseTime(text); }
    public static LocalTime toTime(String text) { return parseTime(text); }

    public static LocalTime parseTime(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalTime.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

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
    public static LocalDateTime readDateTime(String text) { return parseDateTime(text); }
    public static LocalDateTime dateTimeFrom(String text) { return parseDateTime(text); }
    public static LocalDateTime toDateTime(String text) { return parseDateTime(text); }

    public static LocalDateTime parseDateTime(String text, String pattern) {
        if (text == null || pattern == null) return null;
        try {
            return LocalDateTime.parse(text.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ============================================================== formatting

    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) return "";
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }
    public static String format(LocalTime time, String pattern) {
        if (time == null || pattern == null) return "";
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    public static String formatISO(LocalDate date) { return date == null ? "" : ISO_DATE.format(date); }
    public static String formatISO(LocalDateTime dt) { return dt == null ? "" : ISO_DATETIME.format(dt); }

    // ============================================================== humanize (with key-value style)

    public static String humanize(LocalDate date) {
        return date == null ? "" : LONG_DATE.format(date);
    }
    public static String humanFormat(LocalDate date) { return humanize(date); }
    public static String friendly(LocalDate date) { return humanize(date); }

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

    public static String humanizeShort(LocalDate date) {
        return date == null ? "" : SHORT_DATE.format(date);
    }

    public static String humanize(LocalTime time) {
        return time == null ? "" : SHORT_TIME.format(time);
    }

    public static String humanize(LocalDateTime dateTime) {
        return dateTime == null ? "" : MEDIUM_DATE.format(dateTime);
    }

    // ============================================================== validity

    public static boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTime(int hour, int minute, int second) {
        return hour >= 0 && hour <= 23
                && minute >= 0 && minute <= 59
                && second >= 0 && second <= 59;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    public static boolean leapYear(int year) { return isLeapYear(year); }
    public static boolean isLeap(int year) { return isLeapYear(year); }

    public static boolean isValidDate(String text) {
        return parseDate(text) != null;
    }
    public static boolean isValidDate(String text, String pattern) {
        return parseDate(text, pattern) != null;
    }

    // ============================================================== arithmetic

    public static LocalDate addDays(LocalDate date, int days) { return date == null ? null : date.plusDays(days); }
    public static LocalDate plusDays(LocalDate date, int days) { return addDays(date, days); }
    public static LocalDate daysAfter(LocalDate date, int days) { return addDays(date, days); }

    public static LocalDate subtractDays(LocalDate date, int days) {
        return date == null ? null : date.minusDays(days);
    }
    public static LocalDate minusDays(LocalDate date, int days) { return subtractDays(date, days); }
    public static LocalDate daysBefore(LocalDate date, int days) { return subtractDays(date, days); }

    public static LocalDate addWeeks(LocalDate date, int weeks) { return date == null ? null : date.plusWeeks(weeks); }
    public static LocalDate subtractWeeks(LocalDate date, int weeks) {
        return date == null ? null : date.minusWeeks(weeks);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return date == null ? null : date.plusMonths(months);
    }
    public static LocalDate plusMonths(LocalDate date, int months) { return addMonths(date, months); }
    public static LocalDate subtractMonths(LocalDate date, int months) {
        return date == null ? null : date.minusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, int years) { return date == null ? null : date.plusYears(years); }
    public static LocalDate plusYears(LocalDate date, int years) { return addYears(date, years); }
    public static LocalDate subtractYears(LocalDate date, int years) {
        return date == null ? null : date.minusYears(years);
    }

    /** Adds N business days (Mon–Fri), skipping weekends. */
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
    public static LocalDate businessDaysAfter(LocalDate date, int days) { return addBusinessDays(date, days); }

    public static LocalDateTime addDuration(LocalDateTime dt, Duration d) {
        return dt == null || d == null ? null : dt.plus(d);
    }

    // ============================================================== snapping

    public static LocalDate startOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    public static LocalDate endOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date == null ? null : date.withDayOfMonth(1);
    }
    public static LocalDate firstOfMonth(LocalDate date) { return startOfMonth(date); }

    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    public static LocalDate lastOfMonth(LocalDate date) { return endOfMonth(date); }

    public static LocalDate startOfYear(LocalDate date) {
        return date == null ? null : date.withDayOfYear(1);
    }
    public static LocalDate firstOfYear(LocalDate date) { return startOfYear(date); }

    public static LocalDate endOfYear(LocalDate date) {
        return date == null ? null : date.withDayOfYear(date.lengthOfYear());
    }
    public static LocalDate lastOfYear(LocalDate date) { return endOfYear(date); }

    public static LocalDate startOfQuarter(LocalDate date) {
        if (date == null) return null;
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), month, 1);
    }
    public static LocalDate endOfQuarter(LocalDate date) {
        if (date == null) return null;
        return endOfMonth(startOfQuarter(date).plusMonths(2));
    }

    // ============================================================== differences

    public static long daysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.DAYS.between(from, to);
    }
    public static long diffDays(LocalDate from, LocalDate to) { return daysBetween(from, to); }
    public static long daysDiff(LocalDate from, LocalDate to) { return daysBetween(from, to); }
    public static long spanDays(LocalDate from, LocalDate to) { return daysBetween(from, to); }

    public static long weeksBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.WEEKS.between(from, to);
    }

    public static long monthsBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.MONTHS.between(from, to);
    }
    public static long diffMonths(LocalDate from, LocalDate to) { return monthsBetween(from, to); }

    public static long yearsBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.YEARS.between(from, to);
    }
    public static long diffYears(LocalDate from, LocalDate to) { return yearsBetween(from, to); }

    public static Period periodBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return Period.ZERO;
        return Period.between(from, to);
    }

    public static Duration durationBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return Duration.ZERO;
        return Duration.between(from, to);
    }
    public static Duration diff(LocalDateTime from, LocalDateTime to) { return durationBetween(from, to); }

    public static int age(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
    public static int calculateAge(LocalDate birthDate) { return age(birthDate); }
    public static int getAge(LocalDate birthDate) { return age(birthDate); }

    public static int age(LocalDate birthDate, LocalDate onDate) {
        if (birthDate == null || onDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, onDate);
    }
    public static int calculateAge(LocalDate birthDate, LocalDate onDate) { return age(birthDate, onDate); }

    // ============================================================== business days

    public static int businessDaysBetween(LocalDate from, LocalDate to) {
        return businessDaysBetween(from, to, DEFAULT_WEEKEND);
    }
    public static int businessDaysUntil(LocalDate from, LocalDate to) { return businessDaysBetween(from, to); }

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

    public static String dayName(LocalDate date) {
        if (date == null) return "";
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
    }
    public static String shortDayName(LocalDate date) {
        if (date == null) return "";
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }

    public static String monthName(LocalDate date) {
        if (date == null) return "";
        return date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
    }
    public static String shortMonthName(LocalDate date) {
        if (date == null) return "";
        return date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }

    public static int dayOfWeek(LocalDate date) {
        if (date == null) return 0;
        return date.getDayOfWeek().getValue();    // 1 = Monday … 7 = Sunday
    }

    public static int dayOfYear(LocalDate date) {
        return date == null ? 0 : date.getDayOfYear();
    }

    public static int weekNumber(LocalDate date) {
        if (date == null) return 0;
        return date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    }
    public static int weekOfYear(LocalDate date) { return weekNumber(date); }

    public static int quarter(LocalDate date) {
        return date == null ? 0 : (date.getMonthValue() - 1) / 3 + 1;
    }

    public static int daysInMonth(LocalDate date) {
        return date == null ? 0 : date.lengthOfMonth();
    }
    public static int daysInMonth(int year, int month) {
        if (!isValidDate(year, month, 1)) return 0;
        return LocalDate.of(year, month, 1).lengthOfMonth();
    }
    public static int daysInYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    // ============================================================== comparisons

    public static boolean isBefore(LocalDate a, LocalDate b) { return a != null && b != null && a.isBefore(b); }
    public static boolean isAfter(LocalDate a, LocalDate b) { return a != null && b != null && a.isAfter(b); }
    public static boolean isSameDay(LocalDate a, LocalDate b) { return a != null && a.equals(b); }
    public static boolean isSameDate(LocalDate a, LocalDate b) { return isSameDay(a, b); }

    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return a != null && b != null
                && a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    public static boolean isSameYear(LocalDate a, LocalDate b) {
        return a != null && b != null && a.getYear() == b.getYear();
    }

    public static boolean isSameWeek(LocalDate a, LocalDate b) {
        if (a == null || b == null) return false;
        return startOfWeek(a).equals(startOfWeek(b));
    }

    public static boolean isWeekend(LocalDate date) {
        return isWeekend(date, DEFAULT_WEEKEND);
    }
    public static boolean onWeekend(LocalDate date) { return isWeekend(date); }

    public static boolean isWeekend(LocalDate date, DayOfWeek... weekend) {
        if (date == null) return false;
        DayOfWeek[] week = weekend == null || weekend.length == 0 ? DEFAULT_WEEKEND : weekend;
        DayOfWeek d = date.getDayOfWeek();
        for (DayOfWeek w : week) if (w == d) return true;
        return false;
    }

    public static boolean isWeekday(LocalDate date) { return date != null && !isWeekend(date); }
    public static boolean onWeekday(LocalDate date) { return isWeekday(date); }

    public static boolean isToday(LocalDate date) { return date != null && date.equals(LocalDate.now()); }
    public static boolean isPast(LocalDate date) { return date != null && date.isBefore(LocalDate.now()); }
    public static boolean isFuture(LocalDate date) { return date != null && date.isAfter(LocalDate.now()); }
    public static boolean isYesterday(LocalDate date) { return date != null && date.equals(yesterday()); }
    public static boolean isTomorrow(LocalDate date) { return date != null && date.equals(tomorrow()); }

    public static int compare(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    public static LocalDate min(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }
    public static LocalDate minDate(LocalDate a, LocalDate b) { return min(a, b); }

    public static LocalDate max(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }
    public static LocalDate maxDate(LocalDate a, LocalDate b) { return max(a, b); }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) return false;
        return !date.isBefore(start) && !date.isAfter(end);
    }

    // ============================================================== relative time

    public static String timeAgo(LocalDate date) {
        if (date == null) return "";
        return timeAgo(date.atStartOfDay());
    }
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
    public static String relativeTime(LocalDateTime dateTime) { return timeAgo(dateTime); }
    public static String relative(LocalDate date) { return timeAgo(date); }

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

    public static long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) return 0;
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static long toEpochMillis(LocalDate date) {
        if (date == null) return 0;
        return toEpochMillis(date.atStartOfDay());
    }
    public static long toEpochSeconds(LocalDateTime dateTime) {
        if (dateTime == null) return 0;
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public static LocalDateTime fromEpochMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }
    public static LocalDate dateFromEpochMillis(long millis) {
        return fromEpochMillis(millis).toLocalDate();
    }

    // ============================================================== integration

    /** Age bracket — integration with {@code Check} predicates. */
    public static String ageBracket(int age) {
        if (age < 0) return "unknown";
        if (age < 2) return "infant";
        if (age < 13) return "child";
        if (age < 20) return "teenager";
        if (age < 60) return "adult";
        return "senior";
    }

    /** Age from a birth-date string, or -1 if unparseable. */
    public static int ageFromString(String birthDate) {
        LocalDate d = parseDate(birthDate);
        return d == null ? -1 : age(d);
    }
}
