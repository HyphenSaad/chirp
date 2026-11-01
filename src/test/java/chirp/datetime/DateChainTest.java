package chirp.datetime;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateChainTest {

    // ============================================================== factories

    @Test
    void factoriesAllWork() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(d, DateChain.of(d).asDate());
        assertEquals(d, DateChain.on(d).asDate());
        assertEquals(d, DateChain.from(d).asDate());
        assertEquals(d, DateChain.start(d).asDate());
    }

    @Test
    void nullFallsBackToToday() {
        assertEquals(LocalDate.now(), DateChain.of((LocalDate) null).asDate());
    }

    @Test
    void fromDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 31, 14, 30);
        assertEquals(LocalDate.of(2026, 10, 31), DateChain.of(dt).asDate());
    }

    @Test
    void fromString() {
        assertEquals(LocalDate.of(2026, 10, 31), DateChain.of("2026-10-31").asDate());
        assertEquals(LocalDate.now(), DateChain.of("not-a-date").asDate());
    }

    @Test
    void fromEpochMillis() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        long ms = DateTime.toEpochMillis(d);
        assertEquals(d, DateChain.ofEpochMillis(ms).asDate());
    }

    // ============================================================== add / subtract

    @Test
    void addDays() {
        assertEquals(LocalDate.of(2026, 11, 5),
                DateChain.of(LocalDate.of(2026, 10, 31)).addDays(5).asDate());
        assertEquals(LocalDate.of(2026, 11, 5),
                DateChain.of(LocalDate.of(2026, 10, 31)).plusDays(5).asDate());
        assertEquals(LocalDate.of(2026, 11, 5),
                DateChain.of(LocalDate.of(2026, 10, 31)).daysAfter(5).asDate());
    }

    @Test
    void subtractDays() {
        assertEquals(LocalDate.of(2026, 10, 26),
                DateChain.of(LocalDate.of(2026, 10, 31)).subtractDays(5).asDate());
        assertEquals(LocalDate.of(2026, 10, 26),
                DateChain.of(LocalDate.of(2026, 10, 31)).minusDays(5).asDate());
        assertEquals(LocalDate.of(2026, 10, 26),
                DateChain.of(LocalDate.of(2026, 10, 31)).daysBefore(5).asDate());
    }

    @Test
    void addWeeksMonthsYears() {
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(LocalDate.of(2026, 11, 7), DateChain.of(d).addWeeks(1).asDate());
        assertEquals(LocalDate.of(2026, 11, 30), DateChain.of(d).addMonths(1).asDate());
        assertEquals(LocalDate.of(2027, 10, 31), DateChain.of(d).addYears(1).asDate());
    }

    @Test
    void addBusinessDaysChain() {
        // Friday 2026-10-30 + 1 business day → Monday 2026-11-02
        LocalDate friday = LocalDate.of(2026, 10, 30);
        assertEquals(LocalDate.of(2026, 11, 2), DateChain.of(friday).addBusinessDays(1).asDate());
    }

    @Test
    void chainedArithmetic() {
        LocalDate result = DateChain.of(LocalDate.of(2026, 1, 1))
                .addMonths(2)
                .addDays(15)
                .subtractWeeks(1)
                .asDate();
        // 1 Jan + 2 months = 1 Mar; + 15 days = 16 Mar; - 1 week = 9 Mar
        assertEquals(LocalDate.of(2026, 3, 9), result);
    }

    // ============================================================== setters

    @Test
    void withDay() {
        assertEquals(LocalDate.of(2026, 10, 15),
                DateChain.of(LocalDate.of(2026, 10, 31)).withDay(15).asDate());
    }

    @Test
    void withDayClampsToMonthLength() {
        assertEquals(LocalDate.of(2026, 2, 28),
                DateChain.of(LocalDate.of(2026, 2, 10)).withDay(31).asDate());
    }

    @Test
    void withMonth() {
        assertEquals(LocalDate.of(2026, 11, 15),
                DateChain.of(LocalDate.of(2026, 10, 15)).withMonth(11).asDate());
    }

    @Test
    void withYear() {
        assertEquals(LocalDate.of(2027, 10, 15),
                DateChain.of(LocalDate.of(2026, 10, 15)).withYear(2027).asDate());
    }

    // ============================================================== snapping

    @Test
    void snapToMonth() {
        LocalDate d = LocalDate.of(2026, 10, 15);
        assertEquals(LocalDate.of(2026, 10, 1), DateChain.of(d).startOfMonth().asDate());
        assertEquals(LocalDate.of(2026, 10, 31), DateChain.of(d).endOfMonth().asDate());
    }

    @Test
    void snapToYear() {
        LocalDate d = LocalDate.of(2026, 10, 15);
        assertEquals(LocalDate.of(2026, 1, 1), DateChain.of(d).startOfYear().asDate());
        assertEquals(LocalDate.of(2026, 12, 31), DateChain.of(d).endOfYear().asDate());
    }

    @Test
    void snapToWeek() {
        // Sat 2026-10-31: week starts Mon 2026-10-26, ends Sun 2026-11-01
        LocalDate d = LocalDate.of(2026, 10, 31);
        assertEquals(LocalDate.of(2026, 10, 26), DateChain.of(d).startOfWeek().asDate());
        assertEquals(LocalDate.of(2026, 11, 1), DateChain.of(d).endOfWeek().asDate());
    }

    @Test
    void snapToQuarter() {
        LocalDate d = LocalDate.of(2026, 11, 15);
        assertEquals(LocalDate.of(2026, 10, 1), DateChain.of(d).startOfQuarter().asDate());
        assertEquals(LocalDate.of(2026, 12, 31), DateChain.of(d).endOfQuarter().asDate());
    }

    // ============================================================== next / previous

    @Test
    void nextMonday() {
        // Sat 2026-10-31 → next Monday is 2026-11-02
        assertEquals(LocalDate.of(2026, 11, 2),
                DateChain.of(LocalDate.of(2026, 10, 31)).nextMonday().asDate());
    }

    @Test
    void nextFriday() {
        // Sat 2026-10-31 → next Friday is 2026-11-06
        assertEquals(LocalDate.of(2026, 11, 6),
                DateChain.of(LocalDate.of(2026, 10, 31)).nextFriday().asDate());
    }

    @Test
    void genericNext() {
        assertEquals(LocalDate.of(2026, 11, 4),
                DateChain.of(LocalDate.of(2026, 10, 31)).next(DayOfWeek.WEDNESDAY).asDate());
    }

    @Test
    void previous() {
        // Sat 2026-10-31 → previous Friday is 2026-10-30
        assertEquals(LocalDate.of(2026, 10, 30),
                DateChain.of(LocalDate.of(2026, 10, 31)).previous(DayOfWeek.FRIDAY).asDate());
    }

    // ============================================================== predicates

    @Test
    void predicateAliases() {
        LocalDate today = LocalDate.now();
        assertTrue(DateChain.of(today).isToday());
        assertFalse(DateChain.of(today).isPast());
        assertFalse(DateChain.of(today).isFuture());

        assertTrue(DateChain.of(today.plusDays(1)).isFuture());
        assertTrue(DateChain.of(today.minusDays(1)).isPast());

        LocalDate sat = LocalDate.of(2026, 10, 31);
        assertTrue(DateChain.of(sat).isWeekend());
        assertTrue(DateChain.of(sat.minusDays(1)).isWeekday());
    }

    @Test
    void leapYearChain() {
        assertTrue(DateChain.of(LocalDate.of(2024, 6, 1)).isLeapYear());
        assertFalse(DateChain.of(LocalDate.of(2026, 6, 1)).isLeapYear());
    }

    @Test
    void isSameMonthAs() {
        LocalDate a = LocalDate.of(2026, 10, 1);
        LocalDate b = LocalDate.of(2026, 10, 31);
        assertTrue(DateChain.of(a).isSameMonthAs(b));
        assertTrue(DateChain.of(a).isSameYearAs(b));
    }

    // ============================================================== terminals

    @Test
    void asDate() {
        assertEquals(LocalDate.of(2026, 10, 31),
                DateChain.of(LocalDate.of(2026, 10, 31)).asDate());
    }

    @Test
    void asDateTime() {
        assertEquals(LocalDateTime.of(2026, 10, 31, 0, 0),
                DateChain.of(LocalDate.of(2026, 10, 31)).asDateTime());
    }

    @Test
    void atTime() {
        assertEquals(LocalDateTime.of(2026, 10, 31, 14, 30),
                DateChain.of(LocalDate.of(2026, 10, 31)).atTime(14, 30));
    }

    @Test
    void formattedTerminal() {
        assertEquals("31/10/2026",
                DateChain.of(LocalDate.of(2026, 10, 31)).formatted("dd/MM/yyyy"));
    }

    @Test
    void asHuman() {
        String result = DateChain.of(LocalDate.of(2026, 10, 31)).asHuman();
        assertTrue(result.contains("Saturday"));
    }

    @Test
    void asHumanShort() {
        String result = DateChain.of(LocalDate.of(2026, 10, 31)).asHumanShort();
        assertTrue(result.contains("Oct"));
    }

    @Test
    void asISO() {
        assertEquals("2026-10-31", DateChain.of(LocalDate.of(2026, 10, 31)).asISO());
    }

    // ============================================================== info

    @Test
    void infoTerminals() {
        DateChain c = DateChain.of(LocalDate.of(2026, 10, 31));
        assertEquals(6, c.dayOfWeek());       // 2026-10-31 is Saturday
        assertEquals("Saturday", c.dayName());
        assertEquals("Sat", c.shortDayName());
        assertEquals("October", c.monthName());
        assertEquals("Oct", c.shortMonthName());
        assertEquals(4, c.quarter());         // Q4
        assertEquals(31, c.daysInMonth());
    }

    // ============================================================== immutability

    @Test
    void chainIsImmutable() {
        DateChain original = DateChain.of(LocalDate.of(2026, 10, 31));
        DateChain modified = original.addDays(5);
        assertEquals(LocalDate.of(2026, 10, 31), original.asDate());
        assertEquals(LocalDate.of(2026, 11, 5), modified.asDate());
    }

    // ============================================================== integration

    @Test
    void integrationWithDateTime() {
        // Use DateChain where DateTime methods work
        LocalDate now = LocalDate.now();
        DateChain chain = DateChain.of(now);

        // Chain to end of month, then verify with DateTime predicate
        LocalDate eom = chain.endOfMonth().asDate();
        assertTrue(DateTime.isSameMonth(now, eom));
    }
}
