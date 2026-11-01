package chirp.ui;

import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Rich tooltip with configurable delay and wrap. Attach to any node.
 *
 * <pre>{@code
 * Tooltip.of("Delete this file").wrap().attachTo(deleteButton);
 * }</pre>
 */
public class Tooltip extends javafx.scene.control.Tooltip {

    private Tooltip(String text) { super(text == null ? "" : text); }

    public static Tooltip of(String text) { return new Tooltip(text); }

    public Tooltip wrap() {
        setWrapText(true);
        setMaxWidth(320);
        return this;
    }

    public Tooltip showDelay(double ms) {
        setShowDelay(Duration.millis(Math.max(0, ms)));
        return this;
    }

    public Tooltip hideDelay(double ms) {
        setHideDelay(Duration.millis(Math.max(0, ms)));
        return this;
    }

    public Tooltip attachTo(Node node) {
        if (node != null) javafx.scene.control.Tooltip.install(node, this);
        return this;
    }
}
