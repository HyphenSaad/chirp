package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.paint.Color;

/**
 * Color picker dropdown with the standard JavaFX palette.
 *
 * <pre>{@code
 * ColorPicker cp = ColorPicker.of(Color.web("#6366f1"))
 *     .onChange(c -> Ui.style(UiStyle.Key.PRIMARY_COLOR, toHex(c)));
 * }</pre>
 */
public class ColorPicker extends javafx.scene.control.ColorPicker {

    private ColorPicker() {}

    public static ColorPicker of() { return new ColorPicker(); }
    public static ColorPicker of(Color initial) { ColorPicker p = new ColorPicker(); p.setValue(initial); return p; }

    public ColorPicker value(Color c) { setValue(c); return this; }

    public ColorPicker onChange(Consumer<Color> consumer) {
        if (consumer != null) {
            valueProperty().addListener((obs, o, n) -> { if (n != null) consumer.accept(n); });
        }
        return this;
    }

    /** Converts a JavaFX Color to {@code #rrggbb} — handy for CSS. */
    public static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
