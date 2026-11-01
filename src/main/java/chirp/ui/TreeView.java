package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

/**
 * Hierarchical tree view with fluent path-based insertion.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TreeView.of("Categories")
 *     .add("Fruits", "Apple")
 *     .add("Fruits", "Banana")
 *     .add("Vegetables", "Carrot");
 * }</pre>
 *
 * <h2>Path-based add</h2>
 * <p>{@link #add(String...)} takes a path of segments and creates any
 * missing intermediate nodes. Duplicate segments under the same parent
 * are merged — adding {@code ("Fruits", "Apple")} and
 * {@code ("Fruits", "Banana")} produces a single {@code Fruits} node
 * with two children.</p>
 *
 * <h2>Related</h2>
 * <p>For a flat list, see {@link ListView}. For a tree with extra
 * columns, see {@link TreeTableView}.</p>
 *
 * @see ListView
 * @see TreeTableView
 * @since 1.0.0
 */
public class TreeView extends javafx.scene.control.TreeView<String> {

    private TreeView() {}

    /**
     * Creates a tree view with the given root label. The root is
     * expanded by default.
     *
     * @param rootLabel the root label (null → "Root")
     * @return a new tree view
     * @see #of()
     * @since 1.0.0
     */
    public static TreeView of(String rootLabel) {
        TreeView tv = new TreeView();
        TreeItem<String> root = new TreeItem<>(rootLabel == null ? "Root" : rootLabel);
        root.setExpanded(true);
        tv.setRoot(root);
        tv.setShowRoot(true);
        return tv;
    }

    /**
     * Alias of {@link #of(String)} with root label "Root" — see that
     * method for full documentation.
     *
     * @return a new tree view
     * @see #of(String)
     * @since 1.0.0
     */
    public static TreeView of() { return of("Root"); }

    /**
     * Inserts a path of segments. Missing intermediate nodes are created.
     * Duplicate segments under the same parent are merged.
     *
     * @param path the path (null → no-op)
     * @return this tree view, for chaining
     * @since 1.0.0
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

    /**
     * Expands every node in the tree.
     *
     * @return this tree view
     * @since 1.0.0
     */
    public TreeView expandAll() {
        expandRecursive(getRoot());
        return this;
    }

    /**
     * Sets the tree height — both preferred and minimum.
     *
     * @param h the height in pixels
     * @return this tree view
     * @since 1.0.0
     */
    public TreeView height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    /**
     * Makes the tree fill the available horizontal space.
     *
     * @return this tree view
     * @since 1.0.0
     */
    public TreeView fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a listener for selection changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this tree view
     * @since 1.0.0
     */
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
