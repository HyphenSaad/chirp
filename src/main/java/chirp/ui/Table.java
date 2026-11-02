package chirp.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Fluent data table — columns defined at construction or via fluent
 * column builders, rows added by {@link #row(Object...)}.
 *
 * <h2>Two ways to create a table</h2>
 *
 * <h3>1. Simple — all-text headers</h3>
 * <p>When every cell is a plain string and lexical sorting is fine:</p>
 * <pre>{@code
 * Table t = Table.of("Name", "Email")
 *         .row("Saad", "saad@example.com")
 *         .row("Alice", "alice@example.com");
 * }</pre>
 *
 * <h3>2. Typed — per-column type</h3>
 * <p>When a column contains numbers, currency, or dates, and you want
 * numeric sorting and right-alignment:</p>
 * <pre>{@code
 * Table t = Table.empty()
 *         .textColumn("Name")
 *         .numberColumn("Score")
 *         .currencyColumn("Fee")
 *         .dateColumn("Enrolled")
 *         .row("Saad", 95, "Rs.1,000.00", "2026-01-15")
 *         .row("Alice", 78, "Rs.2,500.00", "2025-09-20");
 * }</pre>
 *
 * <h2>What column types change</h2>
 * <ul>
 *   <li><b>Sorting</b> — {@code NUMBER} and {@code CURRENCY} sort
 *       numerically (100 &gt; 20), not lexicographically (which would
 *       put "100" before "20"). {@code DATE} sorts chronologically when
 *       cells are ISO {@code yyyy-mm-dd} strings.</li>
 *   <li><b>Alignment</b> — {@code NUMBER} and {@code CURRENCY} are
 *       right-aligned so digits line up. {@code TEXT} and {@code DATE}
 *       are left-aligned.</li>
 * </ul>
 * <p>{@code TEXT} is the default — same behaviour as {@code Table.of(headers)}.</p>
 *
 * <h2>Row and table height</h2>
 * <p>{@link #rowHeight(double)} sets the cell height, which must match
 * the value the table uses for virtualization. {@link #height(double)}
 * sets the total table height (min + pref), so a VBox parent cannot
 * shrink the table below that size. To size a table for N rows, use
 * {@code .height(rowHeight * N + 30)} — the extra 30px accounts for the
 * column header row.</p>
 *
 * <h2>Row contents</h2>
 * <p>Each row is a varargs list of {@code Object} values. Short rows
 * are padded with empty cells. Whatever you pass is converted with
 * {@code String.valueOf(...)} — format the value the way you want it
 * displayed. The column type affects <em>sorting</em> and
 * <em>alignment</em>, not display.</p>
 *
 * <h2>Related</h2>
 * <p>For a two-column key/value list, see {@link ListView}. For a
 * hierarchical table, see {@link TreeTableView}.</p>
 *
 * @see ListView
 * @see TreeTableView
 * @since 1.0.0
 */
public class Table extends TableView<ObservableList<String>> {

    /**
     * The kind of data in a column — determines sort order and alignment.
     *
     * @since 1.0.0
     */
    public enum ColumnType { TEXT, NUMBER, CURRENCY, DATE }

    private final List<String> headers = new ArrayList<>();

    private Table() {}

    // ============================================================ factories

    /**
     * Creates a table where every column is a {@link ColumnType#TEXT}
     * column. For typed columns, use {@link #empty()} and then fluent
     * {@code column(...)} methods.
     *
     * @param columns the column header labels (null → no columns)
     * @return a new table with text columns
     * @see #empty()
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public static Table of(String... columns) {
        Table t = new Table();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        if (columns != null) {
            for (String name : columns) {
                if (name != null) t.column(name, ColumnType.TEXT);
            }
        }
        return t;
    }

    /**
     * Creates a table with no columns, ready for fluent column builders.
     *
     * @return a new table with no columns
     * @see #of(String...)
     * @since 1.0.0
     */
    public static Table empty() {
        Table t = new Table();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return t;
    }

    // ============================================================ columns

    /**
     * Adds a {@link ColumnType#TEXT} column with the default width.
     *
     * @param name the header name (null → ignored)
     * @return {@code this} for chaining
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public Table column(String name) { return column(name, ColumnType.TEXT); }

    /**
     * Adds a column of the given type with the default width.
     *
     * @param name the header name (null → ignored)
     * @param type the column type (null → {@link ColumnType#TEXT})
     * @return {@code this} for chaining
     * @see #column(String, ColumnType, double)
     * @since 1.0.0
     */
    public Table column(String name, ColumnType type) { return column(name, type, 200); }

    /**
     * Adds a column of the given type and width.
     *
     * <p>The column type drives the sort comparator (numeric for
     * NUMBER/CURRENCY, chronological for DATE) and the cell alignment
     * (right for NUMBER/CURRENCY, left otherwise).</p>
     *
     * @param name the header name (null → ignored)
     * @param type the column type (null → {@link ColumnType#TEXT})
     * @param width the preferred width in pixels
     * @return {@code this} for chaining
     * @see ColumnType
     * @since 1.0.0
     */
    public Table column(String name, ColumnType type, double width) {
        if (name == null) return this;
        ColumnType effectiveType = type == null ? ColumnType.TEXT : type;
        int idx = headers.size();

        TableColumn<ObservableList<String>, String> col = new TableColumn<>(name);
        col.setCellValueFactory(data -> {
            List<String> row = data.getValue();
            return new SimpleStringProperty(idx < row.size() ? row.get(idx) : "");
        });
        col.setPrefWidth(width);

        switch (effectiveType) {
            case NUMBER, CURRENCY -> {
                // Both are right-aligned and sort numerically. CURRENCY differs
                // only in that its cells are expected to look like "Rs.1,234.50";
                // the numeric comparator handles the currency prefix and
                // thousands separators transparently, so behaviour is identical.
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
                col.setComparator(numericComparator());
            }
            case DATE -> {
                col.setStyle("-fx-alignment: CENTER-LEFT;");
                col.setComparator(dateComparator());
            }
            case TEXT -> {
                col.setStyle("-fx-alignment: CENTER-LEFT;");
            }
        }

        getColumns().add(col);
        headers.add(name);
        return this;
    }

    /**
     * Alias of {@code column(name, ColumnType.TEXT)} — see that method
     * for full documentation.
     *
     * @param name the header name
     * @return {@code this} for chaining
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public Table textColumn(String name) { return column(name, ColumnType.TEXT); }

    /**
     * Alias of {@code column(name, ColumnType.NUMBER)} — see that method
     * for full documentation.
     *
     * <p>Shortcut for the most common typed column. Cells are right-aligned
     * and sort numerically, so {@code "100"} sorts after {@code "20"}.</p>
     *
     * @param name the header name
     * @return {@code this} for chaining
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public Table numberColumn(String name) { return column(name, ColumnType.NUMBER); }

    /**
     * Alias of {@code column(name, ColumnType.CURRENCY)} — see that method
     * for full documentation.
     *
     * <p>Cells are right-aligned and sort numerically, ignoring
     * {@code Rs.}, {@code $}, and thousands separators.</p>
     *
     * @param name the header name
     * @return {@code this} for chaining
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public Table currencyColumn(String name) { return column(name, ColumnType.CURRENCY); }

    /**
     * Alias of {@code column(name, ColumnType.DATE)} — see that method
     * for full documentation.
     *
     * <p>Cells are left-aligned and sort chronologically when they are
     * ISO {@code yyyy-mm-dd} strings. Other formats fall back to
     * lexicographic order.</p>
     *
     * @param name the header name
     * @return {@code this} for chaining
     * @see #column(String, ColumnType)
     * @since 1.0.0
     */
    public Table dateColumn(String name) { return column(name, ColumnType.DATE); }

    // ============================================================ rows

    /**
     * Adds a row of cells. Values are converted to strings.
     *
     * @param cells the cell values for this row
     * @return this table, for chaining
     * @see #rows(List)
     * @since 1.0.0
     */
    public Table row(Object... cells) {
        int columnCount = getColumns().size();
        int cellCount = cells == null ? 0 : cells.length;
        int total = Math.max(columnCount, cellCount);
        ObservableList<String> r = FXCollections.observableArrayList();
        for (int i = 0; i < total; i++) {
            Object c = i < cellCount ? cells[i] : null;
            r.add(c == null ? "" : String.valueOf(c));
        }
        getItems().add(r);
        return this;
    }

    /**
     * Adds multiple rows. Each inner list is one row.
     *
     * @param rows the rows to add (null → no-op)
     * @return this table, for chaining
     * @see #row(Object...)
     * @since 1.0.0
     */
    public Table rows(List<? extends List<?>> rows) {
        if (rows != null) for (List<?> r : rows) row(r.toArray());
        return this;
    }

    /**
     * Removes all rows.
     *
     * @return this table
     * @since 1.0.0
     */
    public Table clearAll() { getItems().clear(); return this; }

    /**
     * Sets the cell height. Required for the table to virtualize
     * correctly — without it, large tables may render incorrectly.
     *
     * @param h the cell height in pixels
     * @return this table
     * @since 1.0.0
     */
    public Table rowHeight(double h) {
        setFixedCellSize(h);
        return this;
    }

    /**
     * Sets the total table height (both preferred and minimum, so the
     * table cannot be shrunk below this size).
     *
     * @param h the height in pixels
     * @return this table
     * @since 1.0.0
     */
    public Table height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * Applies alternating row striping via inline CSS.
     *
     * @return this table
     * @since 1.0.0
     */
    public Table striped() {
        setStyle("-fx-control-inner-background-alt: derive(-fx-control-inner-background, -5%);");
        return this;
    }

    /**
     * Makes the table fill the available horizontal space.
     *
     * @return this table
     * @since 1.0.0
     */
    public Table fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Returns the currently-selected row, or null if nothing is selected.
     *
     * @return the selected row, or null
     * @since 1.0.0
     */
    public List<String> selectedRow() { return getSelectionModel().getSelectedItem(); }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     * @since 1.0.0
     */
    public int rowCount() { return getItems().size(); }

    // ============================================================ comparators

    /**
     * Comparator that parses both sides as numbers, ignoring currency
     * prefixes, thousands separators, and any other non-numeric
     * characters. Unparseable cells sort as {@code 0}.
     */
    private static Comparator<String> numericComparator() {
        return (a, b) -> {
            double da = parseNumber(a);
            double db = parseNumber(b);
            return Double.compare(da, db);
        };
    }

    /**
     * Comparator that parses both sides as ISO dates. Valid dates sort
     * chronologically; invalid or empty cells sort after valid ones.
     */
    private static Comparator<String> dateComparator() {
        return (a, b) -> {
            LocalDate da = tryParseDate(a);
            LocalDate db = tryParseDate(b);
            if (da == null && db == null) return a.compareTo(b);
            if (da == null) return 1;    // invalid dates sort to the end
            if (db == null) return -1;
            return da.compareTo(db);
        };
    }

    /**
     * Extracts a numeric value from a string that may contain a currency
     * symbol, a leading dot (from {@code "Rs."} or {@code "$."}), thousands
     * separators, a minus sign, or a mix of these.
     *
     * <p>The last decimal separator wins when both {@code ,} and {@code .}
     * appear — standard for the {@code Rs.1,234.50} and
     * {@code $1,234.50} formats.</p>
     */
    private static double parseNumber(String s) {
        if (s == null || s.isBlank()) return 0;

        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c) || c == '.' || c == ',' || c == '-') sb.append(c);
        }
        String cleaned = sb.toString();

        // Drop leading dots from currency prefixes like "Rs." or "$."
        while (cleaned.startsWith(".")) cleaned = cleaned.substring(1);
        if (cleaned.isEmpty()) return 0;

        boolean hasComma = cleaned.contains(",");
        long dotCount = cleaned.chars().filter(c -> c == '.').count();

        if (hasComma && dotCount > 0) {
            // Standard format — comma thousands, dot decimal
            cleaned = cleaned.replace(",", "");
        } else if (hasComma) {
            // Only commas — last one is decimal if 1–2 digits follow, else thousands
            int lastComma = cleaned.lastIndexOf(',');
            int after = cleaned.length() - lastComma - 1;
            if (after > 0 && after <= 2) {
                cleaned = cleaned.substring(0, lastComma).replace(",", "")
                        + "." + cleaned.substring(lastComma + 1);
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (dotCount > 1) {
            // Multiple dots without commas — last dot is decimal
            int lastDot = cleaned.lastIndexOf('.');
            cleaned = cleaned.substring(0, lastDot).replace(".", "")
                    + cleaned.substring(lastDot);
        }

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses {@code s} as an ISO {@code yyyy-mm-dd} date. Returns
     * {@code null} for anything else — the comparator treats {@code null}
     * as "sort to the end".
     */
    private static LocalDate tryParseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
