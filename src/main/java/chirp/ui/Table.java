package chirp.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Fluent data table — columns defined at construction, rows added by
 * {@link #row(Object...)}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Table.of("Name", "Score", "Grade")
 *     .row("Saad", 95, "A")
 *     .row("Ali", 88, "B")
 *     .rowHeight(32)
 *     .height(32 * 3 + 30);   // 3 rows + header
 * }</pre>
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
 * are padded with empty cells; excess cells are truncated. Numbers,
 * booleans, and any type with a {@code toString} work as cell values —
 * they are converted to strings on render.</p>
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

    private final List<String> headers = new ArrayList<>();

    private Table() {}

    /**
     * Creates a table with the given column headers.
     *
     * @param columns the column header labels (null → no columns)
     * @return a new table
     * @since 1.0.0
     */
    public static Table of(String... columns) {
        Table t = new Table();
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                String name = columns[i];
                if (name == null) continue;
                int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(name);
                col.setCellValueFactory(data -> {
                    List<String> row = data.getValue();
                    String val = idx < row.size() ? row.get(idx) : "";
                    return new SimpleStringProperty(val);
                });
                col.setPrefWidth(200);
                t.getColumns().add(col);
                t.headers.add(name);
            }
        }
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return t;
    }

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
}
