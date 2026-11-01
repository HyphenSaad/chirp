package chirp.ui;

import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;

/**
 * Hierarchical table — a tree where each level also renders as a table row.
 *
 * <pre>{@code
 * TreeTableView tt = TreeTableView.of("Root")
 *     .add("Sales", "2024")
 *     .add("Sales", "2025")
 *     .add("Marketing", "Content");
 * }</pre>
 */
public class TreeTableView extends javafx.scene.control.TreeTableView<String> {

    private TreeTableView() {
        setShowRoot(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TreeTableColumn<String, String> col = new TreeTableColumn<>("Item");
        col.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue() == null ? "" : p.getValue().getValue()));
        col.setPrefWidth(300);
        getColumns().add(col);
    }

    public static TreeTableView of(String rootLabel) {
        TreeTableView tv = new TreeTableView();
        TreeItem<String> root = new TreeItem<>(rootLabel == null ? "Root" : rootLabel);
        root.setExpanded(true);
        tv.setRoot(root);
        return tv;
    }

    public TreeTableView add(String... path) {
        if (path == null) return this;
        TreeItem<String> current = getRoot();
        for (String segment : path) {
            if (segment == null) continue;
            TreeItem<String> child = findChild(current, segment);
            if (child == null) {
                child = new TreeItem<>(segment);
                current.getChildren().add(child);
            }
            current = child;
        }
        return this;
    }

    public TreeTableView expandAll() { expandRec(getRoot()); return this; }
    public TreeTableView height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public TreeTableView fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public TreeTableView onChange(Consumer<String> consumer) {
        if (consumer != null) {
            getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n != null) consumer.accept(n.getValue());
            });
        }
        return this;
    }

    private void expandRec(TreeItem<String> item) {
        if (item == null) return;
        item.setExpanded(true);
        for (TreeItem<String> child : item.getChildren()) expandRec(child);
    }

    private TreeItem<String> findChild(TreeItem<String> parent, String label) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (label.equals(child.getValue())) return child;
        }
        return null;
    }
}
