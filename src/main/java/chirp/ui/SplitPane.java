package chirp.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Resizable two-pane splitter. Drag the divider to resize.
 *
 * <pre>{@code
 * SplitPane sp = SplitPane.horizontal()
 *     .add(leftPanel, rightPanel)
 *     .dividerPosition(0.35);
 * }</pre>
 */
public class SplitPane extends javafx.scene.control.SplitPane {

    public static SplitPane of() { return new SplitPane(); }
    public static SplitPane horizontal() { return new SplitPane().orient(Orientation.HORIZONTAL); }
    public static SplitPane vertical() { return new SplitPane().orient(Orientation.VERTICAL); }

    public SplitPane orient(Orientation o) { setOrientation(o); return this; }

    public SplitPane add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getItems().add(n);
        }
        return this;
    }

    /** Sets the divider position as a fraction of total width/height (0.0 – 1.0). */
    public SplitPane dividerPosition(double fraction) {
        double f = Math.max(0.05, Math.min(0.95, fraction));
        setDividerPositions(f);
        return this;
    }

    public SplitPane height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    public SplitPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
