package chirp.ui;

import javafx.geometry.Orientation;

/**
 * Fluent horizontal or vertical separator.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Separator.hr();   // horizontal line
 * Separator.vr();   // vertical line
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a labelled separator in the console, see
 * {@link chirp.console.Separator}. For a separator in a menu, see
 * {@link MenuBar#separator()}.</p>
 *
 * @see chirp.console.Separator
 * @since 1.0.0
 */
public class Separator extends javafx.scene.control.Separator {

    /**
     * Creates a horizontal separator — a horizontal line.
     *
     * @return a new horizontal separator
     * @see #vr()
     * @since 1.0.0
     */
    public static Separator hr() { return new Separator(Orientation.HORIZONTAL); }

    /**
     * Creates a vertical separator — a vertical line.
     *
     * @return a new vertical separator
     * @see #hr()
     * @since 1.0.0
     */
    public static Separator vr() { return new Separator(Orientation.VERTICAL); }

    /**
     * Constructs a separator with the given orientation. Usually use the
     * static factories instead.
     *
     * @param o the orientation
     * @since 1.0.0
     */
    public Separator(Orientation o) { super(o); }
}
