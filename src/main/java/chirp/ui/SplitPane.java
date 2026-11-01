package chirp.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Resizable two-panel splitter — the user drags the divider to resize.
 *
 * <p>Use this for side-by-side panels where the user controls the
 * relative widths (file tree + editor, list + detail view). Both panels
 * are visible simultaneously; the divider position is user-adjustable.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SplitPane.horizontal()
 *     .add(leftPanel, rightPanel)
 *     .dividerPosition(0.35);   // left panel takes 35% initially
 * }</pre>
 *
 * <h2>Two orientations</h2>
 * <ul>
 *   <li>{@link #horizontal()} — divider is vertical, panels side by side</li>
 *   <li>{@link #vertical()} — divider is horizontal, panels stacked</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For tabs (one view at a time), see {@link TabPane}. For
 * fixed-width side-by-side, see {@link Row}.</p>
 *
 * @see TabPane
 * @see Row
 * @since 1.0.0
 */
public class SplitPane extends javafx.scene.control.SplitPane {

    /**
     * Creates an empty split pane (horizontal by default).
     *
     * @return a new split pane
     * @see #horizontal()
     * @see #vertical()
     * @since 1.0.0
     */
    public static SplitPane of() { return new SplitPane(); }

    /**
     * Creates a horizontal split pane (side by side).
     *
     * @return a new horizontal split pane
     * @see #vertical()
     * @since 1.0.0
     */
    public static SplitPane horizontal() { return new SplitPane().orient(Orientation.HORIZONTAL); }

    /**
     * Creates a vertical split pane (stacked).
     *
     * @return a new vertical split pane
     * @see #horizontal()
     * @since 1.0.0
     */
    public static SplitPane vertical() { return new SplitPane().orient(Orientation.VERTICAL); }

    /**
     * Sets the split orientation.
     *
     * @param o the orientation
     * @return this pane, for chaining
     * @since 1.0.0
     */
    public SplitPane orient(Orientation o) { setOrientation(o); return this; }

    /**
     * Adds panels to the split. Adding two panels makes a two-way split;
     * adding three makes a three-way split.
     *
     * @param nodes the panel contents
     * @return this pane, for chaining
     * @since 1.0.0
     */
    public SplitPane add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getItems().add(n);
        }
        return this;
    }

    /**
     * Sets the initial divider position as a fraction of the total
     * width/height. Clamped to {@code [0.05, 0.95]} so a panel cannot
     * be entirely hidden at startup.
     *
     * @param fraction the divider position (0.0 = all left, 1.0 = all right)
     * @return this pane, for chaining
     * @since 1.0.0
     */
    public SplitPane dividerPosition(double fraction) {
        double f = Math.max(0.05, Math.min(0.95, fraction));
        setDividerPositions(f);
        return this;
    }

    /**
     * Sets the preferred and minimum height.
     *
     * @param h the height in pixels
     * @return this pane
     * @since 1.0.0
     */
    public SplitPane height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    /**
     * Makes the split pane fill the available horizontal space.
     *
     * @return this pane
     * @since 1.0.0
     */
    public SplitPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
