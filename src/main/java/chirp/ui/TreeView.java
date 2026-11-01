package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

/**
 * Hierarchical tree view with fluent path-based insertion.
 *
 * <pre>{@code
 * TreeView t = TreeView.of("Categories")
 *     .add("Fruits", "Apple")
 *     .add("Fruits", "Banana")
 *     .add("Vegetables", "Carrot");
 * }</pre>
 */
public class TreeView extends javafx.scene.control.TreeView<String> {

    private TreeView() {}

    /** Creates a tree with the given root label. */
    public static TreeView of(String rootLabel) {
        TreeView tv = new TreeView();
        TreeItem<String> root = new TreeItem<>(rootLabel == null ? "Root" : rootLabel);
        root.setExpanded(true);
        tv.setRoot(root);
        tv.setShowRoot(true);
        return tv;
    }

    public static TreeView of() { return of("Root"); }

    /**
     * Inserts a path of segments. Missing intermediate nodes are created.
     * Duplicate segments under the same parent are merged.
     */
    public TreeView add(String... path) {
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

    public TreeView expandAll() {
        expandRecursive(getRoot());
        return this;
    }

    public TreeView height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    public TreeView fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public TreeView onChange(Consumer<String> consumer) {
        if (consumer != null) {
            getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n != null) consumer.accept(n.getValue());
            });
        }
        return this;
    }

    private void expandRecursive(TreeItem<String> item) {
        if (item == null) return;
        item.setExpanded(true);
        for (TreeItem<String> child : item.getChildren()) expandRecursive(child);
    }

    private TreeItem<String> findChild(TreeItem<String> parent, String label) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (label.equals(child.getValue())) return child;
        }
        return null;
    }
}
