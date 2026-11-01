package chirp.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Fluent data table built on {@link TableView}.
 *
 * <p><b>Row height and table height</b> are both explicit:
 * {@link #rowHeight(double)} sets cell size, {@link #height(double)} sets the
 * table's total pixel height (min + pref, so it never shrinks below this size).
 * Twelve rows at 32 px need {@code .height(32 * 12 + 30)} for the header.</p>
 */
public class Table extends TableView<ObservableList<String>> {

    private final List<String> headers = new ArrayList<>();

    private Table() {}

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

    public Table rows(List<? extends List<?>> rows) {
        if (rows != null) for (List<?> r : rows) row(r.toArray());
        return this;
    }

    public Table clearAll() { getItems().clear(); return this; }

    /** Sets cell height in pixels — must be set for the table to virtualize correctly. */
    public Table rowHeight(double h) {
        setFixedCellSize(h);
        return this;
    }

    /** Sets the total table height in pixels. Forces at least this height. */
    public Table height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public Table striped() {
        setStyle("-fx-control-inner-background-alt: derive(-fx-control-inner-background, -5%);");
        return this;
    }

    public Table fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public List<String> selectedRow() { return getSelectionModel().getSelectedItem(); }
    public int rowCount() { return getItems().size(); }
}
