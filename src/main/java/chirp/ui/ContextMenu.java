package chirp.ui;

import javafx.scene.Node;

/**
 * Right-click menu — a floating menu that appears at the cursor on
 * right-click.
 *
 * <p>JavaFX's {@code ContextMenu} shows on right-click by default when
 * attached to a node's {@code onContextMenuRequested} handler. Chirp's
 * wrapper adds a fluent {@link #attachTo(Node)} helper and the same
 * {@code item} / {@code separator} / {@code itemDanger} API as
 * {@link MenuButton}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ContextMenu.of()
 *     .item("Copy",  () -> copy())
 *     .item("Paste", () -> paste())
 *     .separator()
 *     .itemDanger("Delete", () -> delete())
 *     .attachTo(someNode);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a top-of-window menu bar, see {@link MenuBar}. For a
 * button-with-dropdown, see {@link MenuButton}.</p>
 *
 * @see MenuBar
 * @see MenuButton
 * @since 1.0.0
 */
public class ContextMenu extends javafx.scene.control.ContextMenu {

    private ContextMenu() {}

    /**
     * Creates an empty context menu.
     *
     * @return a new context menu
     * @since 1.0.0
     */
    public static ContextMenu of() { return new ContextMenu(); }

    /**
     * Adds a menu item with the given label and action.
     *
     * @param label the item label
     * @param action the click action (null → no-op)
     * @return this menu, for chaining
     * @since 1.0.0
     */
    public ContextMenu item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Adds a menu item with red styling — for destructive actions.
     *
     * @param label the item label
     * @param action the click action
     * @return this menu, for chaining
     * @see #item(String, Runnable)
     * @since 1.0.0
     */
    public ContextMenu itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Adds a separator between menu items.
     *
     * @return this menu, for chaining
     * @since 1.0.0
     */
    public ContextMenu separator() {
        getItems().add(new javafx.scene.control.SeparatorMenuItem());
        return this;
    }

    /**
     * Attaches this menu to a node — right-clicking the node shows the
     * menu at the cursor position.
     *
     * @param node the target node (null → no-op)
     * @return this menu, for chaining
     * @since 1.0.0
     */
    public ContextMenu attachTo(Node node) {
        if (node != null) {
            node.setOnContextMenuRequested(e -> show(node, e.getScreenX(), e.getScreenY()));
        }
        return this;
    }
}
