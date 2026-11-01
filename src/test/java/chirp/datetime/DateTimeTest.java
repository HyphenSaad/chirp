package chirp.datetime;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeTest {

    // ---------------------------------------------------------- parsing
    @Test
    void parseIsoDate() { assertEquals(LocalDate.of(2026, 10, 31), DateTime.parseDate("2026-10-31")); }
    @Test
    void parseDmyDate() { assertEquals(LocalDate.of(2026, 10, 31), DateTime.parseDate("31/10/2026")); }
    @Test
    void parseDmyHyphen() { assertEquals(LocalDate.of(2026, 10, 31), DateTime.parseDate("31-10-2026")); }
    @Test
    void parseMonthName() { assertEquals(LocalDate.of(2026, 10, 31), DateTime.parseDate("31 Oct 2026")); }
    @Test
    void parseShortName() { assertEquals(LocalDate.of(2026, 10, 31), DateTime.parseDate("Oct 31, 2026")); }
    @Test
    void parseInvalidReturnsNull() { assertNull(DateTime.parseDate("not-a-date")); }
    @Test
    void parseNullReturnsNull() { assertNull(DateTime.parseDate(null)); }

    @Test
    void parseTime24() { assertEquals(LocalTime.of(14, 30), DateTime.parseTime("14:30")); }
    @Test
    void parseTime12() { assertEquals(LocalTime.of(14, 30), DateTime.parseTime("2:30 PM")); }
    @Test
    void parseTimeWithSeconds() { assertEquals(LocalTime.of(14, 30, 15), DateTime.parseTime("14:30:15")); }
    @Test
    void parseTimeInvalid() { assertNull(DateTime.parseTime("25:00")); }

    @Test
    void parseDateTimeIso() {
        assertEquals(LocalDateTime.of(2026, 10, 31, 14, 30),
                DateTime.parseDateTime("2026-10-31 14:30"));
    }
    @Test
    void parseDateTimeIsoT() {
        assertEquals(LocalDateTime.of(2026, 10, 31, 14, 30),
                DateTime.parseDateTime("2026-10-31T14:30"));
    }
    @Test
    void parseDateTimeInvalid() { assertNull(DateTime.parseDateTime("nope")); }

    // ---------------------------------------------------------- format
    @Test
    void formatDate() {
        assertEquals("31/10/2026",
                DateTime.format(LocalDate.of(2026, 10, 31), "dd/MM/yyyy"));
    }
    @Test
    void formatTime() {
        assertEquals("02:30 PM",
                DateTime.format(LocalTime.of(14, 30), "hh:mm a"));
    }

    // ---------------------------------------------------------- humanize
    @Test
    void humanizeDate() {
        assertEquals("Saturday, October 31, 2026",
                DateTime.humanize(LocalDate.of(2026, 10, 31)));
    }
    @Test
    void humanizeShortDate() {
        assertEquals("Oct 31, 2026",
                DateTime.humanizeShort(LocalDate.of(2026, 10, 31)));
    }
    @Test
    void humanizeTime() {
        assertEquals("2:30 PM",
                DateTime.humanize(LocalTime.of(14, 30)));
    }

    // ---------------------------------------------------------- validity
    @Test
    void isValidDate() {
        assertTrue(DateTime.isValidDate(2026, 10, 31));
        assertFalse(DateTime.isValidDate(2026, 13, 1));
        assertFalse(DateTime.isValidDate(2026, 2, 30));
        assertFalse(DateTime.isValidDate(2026, 1, 0));
    }
    @Test
    void isValidTime() {
        assertTrue(DateTime.isValidTime(23, 59, 59));
        assertFalse(DateTime.isValidTime(24, 0, 0));
        assertFalse(DateTime.isValidTime(0, 60, 0));
    }
    @Test
    void isLeapYear() {
        assertTrue(DateTime.isLeapYear(2024));
        assertFalse(DateTime.isLeapYear(2023));
        assertFalse(DateTime.isLeapYear(1900));
        assertTrue(DateTime.isLeapYear(2000));
    }

    // ---------------------------------------------------------- arithmetic
    @Test
    void addDays() {
        assertEquals(LocalDate.of(2026, 11, 10),
                DateTime.addDays(LocalDate.of(2026, 10, 31), 10));
    }
    @Test
    void addMonths() {
        assertEquals(LocalDate.of(2027, 1, 31),
                DateTime.addMonths(LocalDate.of(2026, 10, 31), 3));
    }
    @Test
    void addYears() {
        assertEquals(LocalDate.of(2030, 10, 31),
                DateTime.addYears(LocalDate.of(2026, 10, 31), 4));
    }

    // ---------------------------------------------------------- difference
    @Test
    void daysBetween() {
        assertEquals(31, DateTime.daysBetween(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1)));
    }
    @Test
    void age() {
        LocalDate birth = LocalDate.now().minusYears(25);
        assertEquals(25, DateTime.age(birth));
    }

    // ---------------------------------------------------------- name
    @Test
    void dayName() {
        assertEquals("Saturday", DateTime.dayName(LocalDate.of(2026, 10, 31)));
    }
    @Test
    void monthName() {
        assertEquals("October", DateTime.monthName(LocalDate.of(2026, 10, 31)));
    }

    // ---------------------------------------------------------- weekend
    @Test
    void weekendCheck() {
        assertTrue(DateTime.isWeekend(LocalDate.of(2026, 10, 31)));   // Saturday
        assertFalse(DateTime.isWeekend(LocalDate.of(2026, 10, 30)));  // Friday
    }
}
