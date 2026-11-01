package chirp.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Horizontal toolbar for quick actions — save / undo / redo buttons,
 * search fields, view toggles.
 *
 * <p>A thin wrapper around {@link javafx.scene.control.ToolBar} that adds
 * fluent {@code add} and a vertical separator factory. Use a toolbar
 * when you need a persistent row of quick actions above or below the
 * main content.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ToolBar.of().add(
 *     Button.primary("Save"),
 *     Button.secondary("Cancel"),
 *     ToolBar.separator(),
 *     Button.outline("Undo"),
 *     Button.outline("Redo"));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a menu-style dropdown bar, see {@link MenuBar}. For a right-click
 * menu, see {@link ContextMenu}.</p>
 *
 * @see MenuBar
 * @see ContextMenu
 * @since 1.0.0
 */
public class ToolBar extends javafx.scene.control.ToolBar {

    /**
     * Creates an empty toolbar.
     *
     * @return a new toolbar
     * @since 1.0.0
     */
    public static ToolBar of() { return new ToolBar(); }

    /**
     * Adds nodes to the toolbar.
     *
     * @param nodes the toolbar items
     * @return this toolbar, for chaining
     * @since 1.0.0
     */
    public ToolBar add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getItems().add(n);
        }
        return this;
    }

    /**
     * Creates a vertical separator to visually group toolbar buttons.
     *
     * @return a new vertical separator
     * @since 1.0.0
     */
    public static javafx.scene.control.Separator separator() {
        return new javafx.scene.control.Separator(Orientation.VERTICAL);
    }
}
