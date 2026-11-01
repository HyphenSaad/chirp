package chirp.ui;

import javafx.scene.Node;

/**
 * Right-click menu.
 *
 * <pre>{@code
 * ContextMenu cm = ContextMenu.of()
 *     .item("Copy",  () -> Console.info("copy"))
 *     .item("Paste", () -> Console.info("paste"))
 *     .separator()
 *     .itemDanger("Delete", () -> Console.warn("delete"));
 *
 * cm.attachTo(someNode);
 * }</pre>
 */
public class ContextMenu extends javafx.scene.control.ContextMenu {

    private ContextMenu() {}

    public static ContextMenu of() { return new ContextMenu(); }

    public ContextMenu item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /** Item with red text — for destructive actions like Delete / Remove. */
    public ContextMenu itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    public ContextMenu separator() {
        getItems().add(new javafx.scene.control.SeparatorMenuItem());
        return this;
    }

    /** Wires this menu to fire on right-click over the node. */
    public ContextMenu attachTo(Node node) {
        if (node != null) {
            node.setOnContextMenuRequested(e -> show(node, e.getScreenX(), e.getScreenY()));
        }
        return this;
    }
}
