package chirp.ui;

import javafx.geometry.Orientation;

/**
 * Fluent horizontal or vertical separator.
 */
public class Separator extends javafx.scene.control.Separator {

    public static Separator hr() { return new Separator(Orientation.HORIZONTAL); }
    public static Separator vr() { return new Separator(Orientation.VERTICAL); }

    public Separator(Orientation o) { super(o); }
}
