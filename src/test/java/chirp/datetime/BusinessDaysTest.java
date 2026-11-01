package chirp.datetime;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDaysTest {

    // 2026-10-30 = Friday, 2026-10-31 = Saturday, 2026-11-01 = Sunday,
    // 2026-11-02 = Monday

    @Test
    void nullInputsReturnZero() {
        assertEquals(0, DateTime.businessDaysBetween(null, LocalDate.now()));
        assertEquals(0, DateTime.businessDaysBetween(LocalDate.now(), null));
        assertEquals(0, DateTime.businessDaysBetween(null, null));
    }

    @Test
    void sameDayIsZero() {
        LocalDate monday = LocalDate.of(2026, 11, 2);
        assertEquals(0, DateTime.businessDaysBetween(monday, monday));
    }

    @Test
    void fridayToMondayCountsOneDay() {
        // Fri (in) .. Mon (out) = only Friday counts -> 1
        LocalDate friday = LocalDate.of(2026, 10, 30);
        LocalDate monday = LocalDate.of(2026, 11, 2);
        assertEquals(1, DateTime.businessDaysBetween(friday, monday));
    }

    @Test
    void saturdayToSundayIsZero() {
        LocalDate saturday = LocalDate.of(2026, 10, 31);
        LocalDate sunday = LocalDate.of(2026, 11, 1);
        assertEquals(0, DateTime.businessDaysBetween(saturday, sunday));
    }

    @Test
    void oneFullWeekIsFive() {
        // Mon in, next Mon out -> Mon, Tue, Wed, Thu, Fri => 5
        LocalDate monday = LocalDate.of(2026, 11, 2);
        LocalDate nextMonday = LocalDate.of(2026, 11, 9);
        assertEquals(5, DateTime.businessDaysBetween(monday, nextMonday));
    }

    @Test
    void twoFullWeeksIsTen() {
        LocalDate start = LocalDate.of(2026, 11, 2);
        LocalDate end = LocalDate.of(2026, 11, 16);
        assertEquals(10, DateTime.businessDaysBetween(start, end));
    }

    @Test
    void reversedOrderReturnsNegative() {
        LocalDate monday = LocalDate.of(2026, 11, 2);
        LocalDate friday = LocalDate.of(2026, 10, 30);
        assertEquals(-1, DateTime.businessDaysBetween(monday, friday));
    }

    /**
     * Nov 1 2026 is Sunday; Dec 1 2026 is Tuesday. Nov 1 .. Nov 30 inclusive
     * (Dec 1 exclusive) is 30 days. Days of week:
     * <pre>
     *   Sun Mon Tue Wed Thu Fri Sat
     *     1   2   3   4   5   6   7  → 5 weekdays
     *     8   9  10  11  12  13  14  → 5 weekdays
     *    15  16  17  18  19  20  21  → 5 weekdays
     *    22  23  24  25  26  27  28  → 5 weekdays
     *    29  30                      → 1 weekday
     * </pre>
     * Total = 5 + 5 + 5 + 5 + 1 = <b>21</b>.
     */
    @Test
    void monthSpan() {
        LocalDate nov1 = LocalDate.of(2026, 11, 1);
        LocalDate dec1 = LocalDate.of(2026, 12, 1);
        assertEquals(21, DateTime.businessDaysBetween(nov1, dec1));
    }
}
