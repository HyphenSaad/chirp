package chirp.ui;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableColumnTypeTest {

    // ============================================================ factories

    @Test
    void ofCreatesTextColumns() {
        Table t = Table.of("Name", "Email");
        assertEquals(2, t.getColumns().size());
    }

    @Test
    void ofSkipsNullHeaders() {
        Table t = Table.of("Name", null, "Email");
        assertEquals(2, t.getColumns().size());
    }

    @Test
    void ofWithEmptyHeaders() {
        Table t = Table.of();
        assertEquals(0, t.getColumns().size());
    }

    @Test
    void ofWithNullArray() {
        Table t = Table.of((String[]) null);
        assertEquals(0, t.getColumns().size());
    }

    @Test
    void emptyCreatesTableWithNoColumns() {
        Table t = Table.empty();
        assertEquals(0, t.getColumns().size());
        assertEquals(0, t.rowCount());
    }

    // ============================================================ typed column factories

    @Test
    void textColumnAddsColumn() {
        Table t = Table.empty().textColumn("Name");
        assertEquals(1, t.getColumns().size());
        assertEquals("Name", t.getColumns().get(0).getText());
    }

    @Test
    void numberColumnAddsColumn() {
        Table t = Table.empty().numberColumn("Score");
        assertEquals(1, t.getColumns().size());
        assertEquals("Score", t.getColumns().get(0).getText());
    }

    @Test
    void currencyColumnAddsColumn() {
        Table t = Table.empty().currencyColumn("Fee");
        assertEquals(1, t.getColumns().size());
        assertEquals("Fee", t.getColumns().get(0).getText());
    }

    @Test
    void dateColumnAddsColumn() {
        Table t = Table.empty().dateColumn("Enrolled");
        assertEquals(1, t.getColumns().size());
        assertEquals("Enrolled", t.getColumns().get(0).getText());
    }

    @Test
    void columnWithExplicitType() {
        Table t = Table.empty()
                .column("Name", Table.ColumnType.TEXT)
                .column("Score", Table.ColumnType.NUMBER)
                .column("Fee", Table.ColumnType.CURRENCY)
                .column("Date", Table.ColumnType.DATE);
        assertEquals(4, t.getColumns().size());
    }

    @Test
    void columnWithWidth() {
        Table t = Table.empty().column("Name", Table.ColumnType.TEXT, 250);
        assertEquals(250.0, t.getColumns().get(0).getPrefWidth(), 0.01);
    }

    @Test
    void columnWithNullNameIgnored() {
        Table t = Table.empty().column(null, Table.ColumnType.TEXT);
        assertEquals(0, t.getColumns().size());
    }

    @Test
    void columnWithNullTypeDefaultsToText() {
        Table t = Table.empty().column("Name", null);
        assertEquals(1, t.getColumns().size());
    }

    @Test
    void textColumnSortsLexicographically() {
        // JavaFX supplies a default String comparator for TEXT columns — no
        // override applied. Verify it is alphabetic, not numeric.
        Table t = Table.empty().textColumn("Name");
        Comparator<String> cmp = comparatorFor(t);
        assertNotNull(cmp, "JavaFX supplies a default String comparator");
        assertTrue(cmp.compare("100", "20") < 0, "lexicographic: '100' < '20'");
        assertTrue(cmp.compare("Apple", "Banana") < 0);
        assertTrue(cmp.compare("apple", "Banana") < 0,
                "JavaFX's default String comparator is case-insensitive (Collator)");
    }

    @Test
    void columnWithNullTypeDefaultsToLexicographicSort() {
        // A null ColumnType falls back to TEXT, which keeps the default
        // JavaFX String comparator — no numeric override applied.
        Table t = Table.empty().column("Name", null);
        Comparator<String> cmp = comparatorFor(t);
        assertNotNull(cmp, "JavaFX supplies a default String comparator");
        assertTrue(cmp.compare("100", "20") < 0,
                "null type must not switch to numeric sort — '100' should still sort before '20'");
    }

    // ============================================================ alignment

    @Test
    void numberColumnIsRightAligned() {
        Table t = Table.empty().numberColumn("Score");
        assertTrue(t.getColumns().get(0).getStyle().contains("CENTER-RIGHT"));
    }

    @Test
    void currencyColumnIsRightAligned() {
        Table t = Table.empty().currencyColumn("Fee");
        assertTrue(t.getColumns().get(0).getStyle().contains("CENTER-RIGHT"));
    }

    @Test
    void textColumnIsLeftAligned() {
        Table t = Table.empty().textColumn("Name");
        assertTrue(t.getColumns().get(0).getStyle().contains("CENTER-LEFT"));
    }

    @Test
    void dateColumnIsLeftAligned() {
        Table t = Table.empty().dateColumn("Enrolled");
        assertTrue(t.getColumns().get(0).getStyle().contains("CENTER-LEFT"));
    }

    // ============================================================ numeric sort

    @Test
    void numberColumnSortsNumerically() {
        Table t = Table.empty().numberColumn("Score");
        Comparator<String> cmp = comparatorFor(t);
        assertNotNull(cmp);
        assertTrue(cmp.compare("20", "100") < 0, "20 should sort before 100");
        assertTrue(cmp.compare("100", "20") > 0, "100 should sort after 20");
        assertTrue(cmp.compare("3", "20") < 0);
        assertEquals(0, cmp.compare("20", "20"));
    }

    @Test
    void numberColumnHandlesEmptyCells() {
        Table t = Table.empty().numberColumn("Score");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("", "5") < 0, "empty treated as 0, sorts first");
        assertEquals(0, cmp.compare("", "0"));
    }

    @Test
    void numberColumnHandlesNonNumericCells() {
        Table t = Table.empty().numberColumn("Score");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("abc", "5") < 0, "abc treated as 0");
        assertEquals(0, cmp.compare("abc", ""));
    }

    @Test
    void numberColumnHandlesNegatives() {
        Table t = Table.empty().numberColumn("Score");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("-10", "5") < 0);
        assertTrue(cmp.compare("-10", "0") < 0);
        assertTrue(cmp.compare("-5", "-10") > 0);
    }

    // ============================================================ currency sort

    @Test
    void currencyColumnSortsNumerically() {
        Table t = Table.empty().currencyColumn("Fee");
        Comparator<String> cmp = comparatorFor(t);
        assertNotNull(cmp);
        assertTrue(cmp.compare("Rs.500.00", "Rs.1,000.00") < 0);
        assertTrue(cmp.compare("Rs.1,000.00", "Rs.500.00") > 0);
    }

    @Test
    void currencyColumnIgnoresThousandsSeparator() {
        Table t = Table.empty().currencyColumn("Fee");
        Comparator<String> cmp = comparatorFor(t);
        assertEquals(0, cmp.compare("Rs.1,234.50", "Rs.1,234.50"));
        assertTrue(cmp.compare("Rs.1,234.50", "Rs.2,000.00") < 0);
    }

    @Test
    void currencyColumnHandlesDifferentSymbols() {
        Table t = Table.empty().currencyColumn("Fee");
        Comparator<String> cmp = comparatorFor(t);
        assertEquals(0, cmp.compare("$1,000.00", "Rs.1,000.00"));
        assertTrue(cmp.compare("$500.00", "$1,000.00") < 0);
    }

    @Test
    void currencyColumnWithoutDecimals() {
        Table t = Table.empty().currencyColumn("Fee");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("Rs.1,234", "Rs.2,000") < 0);
        assertEquals(0, cmp.compare("Rs.1,234", "Rs.1,234"));
    }

    @Test
    void currencyColumnHandlesNegatives() {
        Table t = Table.empty().currencyColumn("Amount");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("-Rs.500.00", "Rs.500.00") < 0);
    }

    // ============================================================ date sort

    @Test
    void dateColumnSortsChronologically() {
        Table t = Table.empty().dateColumn("Enrolled");
        Comparator<String> cmp = comparatorFor(t);
        assertNotNull(cmp);
        assertTrue(cmp.compare("2025-01-15", "2026-01-15") < 0);
        assertTrue(cmp.compare("2026-01-15", "2025-01-15") > 0);
        assertEquals(0, cmp.compare("2026-01-15", "2026-01-15"));
    }

    @Test
    void dateColumnHandlesInvalidDates() {
        Table t = Table.empty().dateColumn("Enrolled");
        Comparator<String> cmp = comparatorFor(t);
        // Invalid sorts after valid
        assertTrue(cmp.compare("abc", "2026-01-15") > 0);
        assertTrue(cmp.compare("2026-01-15", "abc") < 0);
        // Empty sorts after valid
        assertTrue(cmp.compare("", "2026-01-15") > 0);
        // Two invalid → falls back to string comparison
        assertTrue(cmp.compare("abc", "xyz") < 0);
    }

    @Test
    void dateColumnSortsSameYearDifferentMonths() {
        Table t = Table.empty().dateColumn("D");
        Comparator<String> cmp = comparatorFor(t);
        assertTrue(cmp.compare("2026-01-31", "2026-02-01") < 0);
    }

    // ============================================================ rows

    @Test
    void rowAppendsRow() {
        Table t = Table.of("A", "B").row(1, 2);
        assertEquals(1, t.rowCount());
    }

    @Test
    void rowWithNullCells() {
        Table t = Table.of("A", "B").row(null, null);
        assertEquals(1, t.rowCount());
        assertEquals("", t.getItems().get(0).get(0));
        assertEquals("", t.getItems().get(0).get(1));
    }

    @Test
    void rowWithFewerCellsPadded() {
        Table t = Table.of("A", "B", "C").row(1);
        assertEquals(3, t.getItems().get(0).size());
        assertEquals("1", t.getItems().get(0).get(0));
        assertEquals("", t.getItems().get(0).get(1));
        assertEquals("", t.getItems().get(0).get(2));
    }

    @Test
    void rowsAppendsMany() {
        Table t = Table.of("A").rows(List.of(List.of("x"), List.of("y")));
        assertEquals(2, t.rowCount());
    }

    @Test
    void rowsWithNullListNoOp() {
        Table t = Table.of("A");
        t.rows(null);
        assertEquals(0, t.rowCount());
    }

    @Test
    void clearAllRemovesRows() {
        Table t = Table.of("A").row(1).row(2).clearAll();
        assertEquals(0, t.rowCount());
        assertEquals(1, t.getColumns().size());
    }

    @Test
    void rowCountMatchesAppendCount() {
        Table t = Table.of("A").row(1).row(2).row(3);
        assertEquals(3, t.rowCount());
    }

    // ============================================================ backwards compatibility

    @Test
    void originalApiStillWorks() {
        Table t = Table.of("Name", "Subject", "Score")
                .row("Saad", "Math", 95)
                .row("Alice", "Physics", 88);
        assertEquals(2, t.rowCount());
        assertEquals("Saad", t.getItems().get(0).get(0));
        assertEquals("95", t.getItems().get(0).get(2));
    }

    // ============================================================ helper

    @SuppressWarnings("unchecked")
    private static Comparator<String> comparatorFor(Table t) {
        Object comparator = t.getColumns().get(0).getComparator();
        return comparator instanceof Comparator
                ? (Comparator<String>) comparator
                : null;
    }
}
