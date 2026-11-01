package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.paint.Color;

/**
 * Color picker dropdown with the standard JavaFX palette.
 *
 * <p>Wraps {@link javafx.scene.control.ColorPicker} with fluent setters
 * and a {@code toHex} helper for converting the picked colour to a CSS
 * hex string — the format the theme CSS files expect.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ColorPicker picker = ColorPicker.of(Color.web("#6366f1"))
 *     .onChange(c -> UiStyle.apply(UiStyle.Key.PRIMARY_COLOR, ColorPicker.toHex(c)));
 * }</pre>
 *
 * <h2>The hex helper</h2>
 * <p>JavaFX's {@code Color} does not have a public {@code toHex()}
 * method. {@link #toHex(Color)} formats the red, green, and blue
 * components as {@code #rrggbb}, matching CSS hex notation. This is
 * what makes the picker useful for live re-styling — the picked colour
 * can be handed straight to {@link UiStyle#apply(Object...)} or set on
 * a CSS class.</p>
 *
 * <h2>Related</h2>
 * <p>For theme-level colour configuration, see {@link UiStyle}. For
 * full theme selection (light/dark/auto), see {@link UiTheme}.</p>
 *
 * @see UiStyle
 * @see UiTheme
 * @since 1.0.0
 */
public class ColorPicker extends javafx.scene.control.ColorPicker {

    private ColorPicker() {}

    /**
     * Creates a color picker with the default initial colour (white).
     *
     * @return a new color picker
     * @see #of(Color)
     * @since 1.0.0
     */
    public static ColorPicker of() { return new ColorPicker(); }

    /**
     * Creates a color picker with a custom initial colour.
     *
     * @param initial the initial colour (null → default)
     * @return a new color picker
     * @see #of()
     * @since 1.0.0
     */
    public static ColorPicker of(Color initial) { ColorPicker p = new ColorPicker(); p.setValue(initial); return p; }

    /**
     * Sets the current colour.
     *
     * @param c the colour (null → no-op)
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public ColorPicker value(Color c) { setValue(c); return this; }

    /**
     * Registers a listener for colour changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public ColorPicker onChange(Consumer<Color> consumer) {
        if (consumer != null) {
            valueProperty().addListener((obs, o, n) -> { if (n != null) consumer.accept(n); });
        }
        return this;
    }

    /**
     * Converts a JavaFX {@link Color} to a CSS hex string like
     * {@code "#6366f1"}.
     *
     * <p>The alpha channel is dropped — the standard {@code #rrggbb}
     * format does not include it. If you need alpha, format it manually
     * with {@code String.format("#%02x%02x%02x%02x", ...)}.</p>
     *
     * @param c the colour (null → {@code "#000000"})
     * @return the hex string
     * @since 1.0.0
     */
    public static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
