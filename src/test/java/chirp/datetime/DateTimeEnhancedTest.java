package chirp.datetime;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeEnhancedTest {

    // ============================================================== aliases

    @Test
    void todayAliases() {
        LocalDate today = DateTime.today();
        assertEquals(today, DateTime.currentDate());
        assertEquals(today, DateTime.nowDate());
    }

    @Test
    void yesterdayAndTomorrow() {
        assertEquals(DateTime.today().minusDays(1), DateTime.yesterday());
        assertEquals(DateTime.today().plusDays(1), DateTime.tomorrow());
        assertEquals(DateTime.yesterday(), DateTime.dayBefore());
        assertEquals(DateTime.tomorrow(), DateTime.dayAfter());
    }

    @Test
    void parseAliases() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(d, DateTime.parseDate("2026-10-31"));
        assertEquals(d, DateTime.readDate("2026-10-31"));
        assertEquals(d, DateTime.dateFrom("2026-10-31"));
        assertEquals(d, DateTime.toDate("2026-10-31"));
        assertEquals(d, DateTime.tryParseDate("2026-10-31"));
    }

    @Test
    void leapYearAliases() {
        assertTrue(DateTime.isLeapYear(2024));
        assertTrue(DateTime.leapYear(2024));
        assertTrue(DateTime.isLeap(2024));
    }

    @Test
    void addDaysAliases() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(LocalDate.of(2026, 11, 5), DateTime.addDays(d, 5));
        assertEquals(LocalDate.of(2026, 11, 5), DateTime.plusDays(d, 5));
        assertEquals(LocalDate.of(2026, 11, 5), DateTime.daysAfter(d, 5));
    }

    @Test
    void subtractDaysAliases() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(LocalDate.of(2026, 10, 26), DateTime.subtractDays(d, 5));
        assertEquals(LocalDate.of(2026, 10, 26), DateTime.minusDays(d, 5));
        assertEquals(LocalDate.of(2026, 10, 26), DateTime.daysBefore(d, 5));
    }

    @Test
    void daysBetweenAliases() {
        LocalDate a = LocalDate.of(2026, 10, 1);
        LocalDate b = LocalDate.of(2026, 11, 1);
        assertEquals(31, DateTime.daysBetween(a, b));
        assertEquals(31, DateTime.diffDays(a, b));
        assertEquals(31, DateTime.daysDiff(a, b));
        assertEquals(31, DateTime.spanDays(a, b));
    }

    // ============================================================== key-value

    @Test
    void parseWithPatternKeyValue() {
        LocalDate d = DateTime.parseDate("31/10/2026", DateTime.Key.PATTERN, "dd/MM/yyyy");
        assertEquals(LocalDate.of(2026, 10, 31), d);
    }

    @Test
    void parseKeyValueNullsAreSafe() {
        assertDoesNotThrow(() -> DateTime.parseDate("x", (Object[]) null));
        assertDoesNotThrow(() -> DateTime.parseDate("x", DateTime.Key.PATTERN, null));
    }

    @Test
    void humanizeStyle() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertTrue(DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.SHORT).contains("Oct"));
        assertTrue(DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.LONG).contains("Saturday"));
        assertEquals("2026-10-31", DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.ISO));
    }

    @Test
    void humanizeDefaultStyleIsLong() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(DateTime.humanize(d), DateTime.humanize(d, DateTime.Key.STYLE, DateTime.Style.LONG));
    }

    @Test
    void configurableWeekend() {
        LocalDate friday = LocalDate.of(2026, 10, 30);
        // Default weekend (Sat/Sun) — Friday is a weekday
        assertFalse(DateTime.isWeekend(friday));
        // Custom weekend (Fri/Sat) — Friday is weekend
        assertTrue(DateTime.isWeekend(friday, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
    }

    // ============================================================== snapping

    @Test
    void startOfWeek() {
        // 2026-10-31 is Saturday; Monday of that week is 2026-10-26
        assertEquals(LocalDate.of(2026, 10, 26), DateTime.startOfWeek(LocalDate.of(2026, 10, 31)));
    }

    @Test
    void endOfWeek() {
        // Sunday of the week containing Sat 2026-10-31 is 2026-11-01
        assertEquals(LocalDate.of(2026, 11, 1), DateTime.endOfWeek(LocalDate.of(2026, 10, 31)));
    }

    @Test
    void startOfMonth() {
        assertEquals(LocalDate.of(2026, 10, 1), DateTime.startOfMonth(LocalDate.of(2026, 10, 31)));
    }

    @Test
    void endOfMonth() {
        assertEquals(LocalDate.of(2026, 10, 31), DateTime.endOfMonth(LocalDate.of(2026, 10, 15)));
        assertEquals(LocalDate.of(2026, 2, 28), DateTime.endOfMonth(LocalDate.of(2026, 2, 1)));
        assertEquals(LocalDate.of(2024, 2, 29), DateTime.endOfMonth(LocalDate.of(2024, 2, 1))); // leap year
    }

    @Test
    void startEndOfYear() {
        assertEquals(LocalDate.of(2026, 1, 1), DateTime.startOfYear(LocalDate.of(2026, 10, 31)));
        assertEquals(LocalDate.of(2026, 12, 31), DateTime.endOfYear(LocalDate.of(2026, 10, 31)));
    }

    @Test
    void startEndOfQuarter() {
        // Oct is Q4 (Oct-Dec)
        assertEquals(LocalDate.of(2026, 10, 1), DateTime.startOfQuarter(LocalDate.of(2026, 11, 15)));
        assertEquals(LocalDate.of(2026, 12, 31), DateTime.endOfQuarter(LocalDate.of(2026, 11, 15)));
    }

    // ============================================================== addBusinessDays

    @Test
    void addBusinessDaysSkipsWeekend() {
        // Friday 2026-10-30 + 1 business day = Monday 2026-11-02
        LocalDate friday = LocalDate.of(2026, 10, 30);
        assertEquals(LocalDate.of(2026, 11, 2), DateTime.addBusinessDays(friday, 1));
    }

    @Test
    void addBusinessDaysNegative() {
        // Monday 2026-11-02 - 1 business day = Friday 2026-10-30
        LocalDate monday = LocalDate.of(2026, 11, 2);
        assertEquals(LocalDate.of(2026, 10, 30), DateTime.addBusinessDays(monday, -1));
    }

    // ============================================================== names / digits

    @Test
    void dayOfYear() {
        assertEquals(1, DateTime.dayOfYear(LocalDate.of(2026, 1, 1)));
        assertEquals(365, DateTime.dayOfYear(LocalDate.of(2026, 12, 31)));
        assertEquals(366, DateTime.dayOfYear(LocalDate.of(2024, 12, 31)));  // leap
    }

    @Test
    void quarter() {
        assertEquals(1, DateTime.quarter(LocalDate.of(2026, 1, 15)));
        assertEquals(2, DateTime.quarter(LocalDate.of(2026, 5, 15)));
        assertEquals(3, DateTime.quarter(LocalDate.of(2026, 8, 15)));
        assertEquals(4, DateTime.quarter(LocalDate.of(2026, 10, 15)));
    }

    @Test
    void daysInMonth() {
        assertEquals(31, DateTime.daysInMonth(LocalDate.of(2026, 10, 1)));
        assertEquals(28, DateTime.daysInMonth(LocalDate.of(2026, 2, 1)));
        assertEquals(29, DateTime.daysInMonth(LocalDate.of(2024, 2, 1)));
        assertEquals(31, DateTime.daysInMonth(2026, 10));
        assertEquals(0, DateTime.daysInMonth(2026, 13));
    }

    @Test
    void daysInYear() {
        assertEquals(365, DateTime.daysInYear(2026));
        assertEquals(366, DateTime.daysInYear(2024));
    }

    @Test
    void weekNumberReturnsPositive() {
        assertTrue(DateTime.weekNumber(LocalDate.of(2026, 10, 31)) > 0);
    }

    // ============================================================== comparisons

    @Test
    void isSameMonthYearWeek() {
        LocalDate a = LocalDate.of(2026, 10, 1);
        LocalDate b = LocalDate.of(2026, 10, 31);
        LocalDate c = LocalDate.of(2026, 11, 1);
        LocalDate d = LocalDate.of(2025, 10, 15);

        assertTrue(DateTime.isSameMonth(a, b));
        assertFalse(DateTime.isSameMonth(a, c));
        assertTrue(DateTime.isSameYear(a, b));
        assertFalse(DateTime.isSameYear(a, d));
        assertTrue(DateTime.isSameWeek(b, c));    // Sat 10-31 and Sun 11-01: same Mon-Sun week
        assertFalse(DateTime.isSameWeek(a, c));   // weeks of Mon 09-28 and Mon 10-26
    }

    @Test
    void todayPastFuture() {
        assertTrue(DateTime.isToday(DateTime.today()));
        assertTrue(DateTime.isPast(DateTime.today().minusDays(1)));
        assertTrue(DateTime.isFuture(DateTime.today().plusDays(1)));
        assertFalse(DateTime.isPast(DateTime.today().plusDays(1)));
    }

    @Test
    void yesterdayTomorrow() {
        assertTrue(DateTime.isYesterday(DateTime.yesterday()));
        assertTrue(DateTime.isTomorrow(DateTime.tomorrow()));
        assertFalse(DateTime.isYesterday(DateTime.today()));
    }

    @Test
    void compare() {
        LocalDate a = LocalDate.of(2026, 10, 1);
        LocalDate b = LocalDate.of(2026, 10, 31);
        assertTrue(DateTime.compare(a, b) < 0);
        assertTrue(DateTime.compare(b, a) > 0);
        assertEquals(0, DateTime.compare(a, a));
        assertEquals(-1, DateTime.compare(null, a));
        assertEquals(1, DateTime.compare(a, null));
        assertEquals(0, DateTime.compare(null, null));
    }

    @Test
    void minMax() {
        LocalDate a = LocalDate.of(2026, 10, 1);
        LocalDate b = LocalDate.of(2026, 10, 31);
        assertEquals(a, DateTime.min(a, b));
        assertEquals(b, DateTime.max(a, b));
        assertEquals(a, DateTime.min(a, null));
        assertEquals(a, DateTime.max(a, null));
    }

    @Test
    void isBetween() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        assertTrue(DateTime.isBetween(LocalDate.of(2026, 10, 15), start, end));
        assertTrue(DateTime.isBetween(start, start, end));
        assertTrue(DateTime.isBetween(end, start, end));
        assertFalse(DateTime.isBetween(LocalDate.of(2026, 11, 1), start, end));
    }

    // ============================================================== timeAgo

    @Test
    void timeAgoJustNow() {
        assertTrue(DateTime.timeAgo(LocalDateTime.now()).contains("just now"));
    }

    @Test
    void timeAgoMinutes() {
        String result = DateTime.timeAgo(LocalDateTime.now().minusMinutes(5));
        assertTrue(result.contains("5m ago"), result);
    }

    @Test
    void timeAgoHours() {
        String result = DateTime.timeAgo(LocalDateTime.now().minusHours(3));
        assertTrue(result.contains("3h ago"), result);
    }

    @Test
    void timeAgoYesterday() {
        String result = DateTime.timeAgo(LocalDateTime.now().minusDays(1).minusHours(1));
        assertTrue(result.contains("yesterday"), result);
    }

    @Test
    void timeAgoFuture() {
        String result = DateTime.timeAgo(LocalDateTime.now().plusMinutes(5));
        assertTrue(result.contains("in 5m"), result);
    }

    @Test
    void timeAgoWithReference() {
        LocalDateTime ref = LocalDateTime.of(2026, 10, 31, 12, 0);
        LocalDateTime actual = LocalDateTime.of(2026, 10, 31, 11, 55);
        String result = DateTime.timeAgo(actual, DateTime.Key.REFERENCE, ref);
        assertTrue(result.contains("5m ago"), result);
    }

    // ============================================================== epoch

    @Test
    void epochRoundTrip() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 31, 14, 30, 15);
        long ms = DateTime.toEpochMillis(dt);
        LocalDateTime back = DateTime.fromEpochMillis(ms);
        assertEquals(dt, back);
    }

    @Test
    void toEpochSeconds() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 31, 14, 30, 15);
        long s1 = DateTime.toEpochSeconds(dt);
        long s2 = DateTime.toEpochMillis(dt) / 1000;
        assertEquals(s1, s2);
    }

    @Test
    void nullEpochReturnsZero() {
        assertEquals(0, DateTime.toEpochMillis((LocalDateTime) null));
        assertEquals(0, DateTime.toEpochMillis((LocalDate) null));
    }

    // ============================================================== age bracket

    @Test
    void ageBracket() {
        assertEquals("infant", DateTime.ageBracket(1));
        assertEquals("child", DateTime.ageBracket(8));
        assertEquals("teenager", DateTime.ageBracket(15));
        assertEquals("adult", DateTime.ageBracket(30));
        assertEquals("senior", DateTime.ageBracket(70));
        assertEquals("unknown", DateTime.ageBracket(-5));
    }

    @Test
    void ageFromString() {
        LocalDate birth = DateTime.today().minusYears(25);
        assertEquals(25, DateTime.ageFromString(birth.toString()));
        assertEquals(-1, DateTime.ageFromString("nope"));
        assertEquals(-1, DateTime.ageFromString(null));
    }

    // ============================================================== edge cases

    @Test
    void nullInputsAreSafe() {
        assertNull(DateTime.parseDate(null));
        assertNull(DateTime.parseTime(null));
        assertNull(DateTime.parseDateTime(null));
        assertEquals("", DateTime.humanize((LocalDate) null));
        assertEquals("", DateTime.humanize((LocalDateTime) null));
        assertEquals("", DateTime.dayName(null));
        assertEquals(0, DateTime.daysBetween(null, null));
        assertFalse(DateTime.isWeekend(null));
        assertFalse(DateTime.isBefore(null, null));
    }
}
