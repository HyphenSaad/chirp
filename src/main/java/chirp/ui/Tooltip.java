package chirp.ui;

import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Rich tooltip — a small popup that appears on hover.
 *
 * <p>Adds wrap, delay, and {@link #attachTo(Node)} helpers over the
 * JavaFX {@link javafx.scene.control.Tooltip}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Tooltip.of("Delete this file permanently").wrap().attachTo(deleteButton);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the button shortcut to attach a simple tooltip, see
 * {@link Button#tooltip(String)}.</p>
 *
 * @see Button#tooltip(String)
 * @since 1.0.0
 */
public class Tooltip extends javafx.scene.control.Tooltip {

    private Tooltip(String text) { super(text == null ? "" : text); }

    /**
     * Creates a tooltip with the given text.
     *
     * @param text the tooltip text (null → "")
     * @return a new tooltip
     * @since 1.0.0
     */
    public static Tooltip of(String text) { return new Tooltip(text); }

    /**
     * Enables word wrap with a 320px maximum width.
     *
     * @return this tooltip, for chaining
     * @since 1.0.0
     */
    public Tooltip wrap() {
        setWrapText(true);
        setMaxWidth(320);
        return this;
    }

    /**
     * Sets the delay before the tooltip appears.
     *
     * @param ms the delay in milliseconds
     * @return this tooltip, for chaining
     * @since 1.0.0
     */
    public Tooltip showDelay(double ms) {
        setShowDelay(Duration.millis(Math.max(0, ms)));
        return this;
    }

    /**
     * Sets the delay before the tooltip hides.
     *
     * @param ms the delay in milliseconds
     * @return this tooltip, for chaining
     * @since 1.0.0
     */
    public Tooltip hideDelay(double ms) {
        setHideDelay(Duration.millis(Math.max(0, ms)));
        return this;
    }

    /**
     * Attaches this tooltip to a node — the tooltip appears when the
     * cursor hovers over the node.
     *
     * @param node the target node (null → no-op)
     * @return this tooltip, for chaining
     * @since 1.0.0
     */
    public Tooltip attachTo(Node node) {
        if (node != null) javafx.scene.control.Tooltip.install(node, this);
        return this;
    }
}
