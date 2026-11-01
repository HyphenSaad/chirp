package chirp.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Horizontal quick-action bar.
 *
 * <pre>{@code
 * ToolBar tb = ToolBar.of().add(
 *     Button.primary("Save"),
 *     Button.secondary("Cancel"),
 *     ToolBar.separator(),
 *     Button.outline("Undo")
 * );
 * }</pre>
 */
public class ToolBar extends javafx.scene.control.ToolBar {

    public static ToolBar of() { return new ToolBar(); }

    public ToolBar add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getItems().add(n);
        }
        return this;
    }

    /** Vertical separator between groups of toolbar buttons. */
    public static javafx.scene.control.Separator separator() {
        return new javafx.scene.control.Separator(Orientation.VERTICAL);
    }
}
