package chirp.ui;

import java.util.function.Consumer;

/**
 * Toggle button — stays pressed when clicked, useful for radio-like
 * groups or on/off switches.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ToggleGroup group = new ToggleGroup();
 * ToggleButton.primary("Bold").group(group);
 * ToggleButton.primary("Italic").group(group);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a regular push button, see {@link Button}. For a checkbox, see
 * {@link Checkbox}.</p>
 *
 * @see Button
 * @see Checkbox
 * @since 1.0.0
 */
public class ToggleButton extends javafx.scene.control.ToggleButton {

    private ToggleButton(String text) { super(text == null ? "" : text); }

    /**
     * Creates a plain toggle button.
     *
     * @param text the button text (null → "")
     * @return a new toggle button
     * @see #primary(String)
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static ToggleButton of(String text) { return new ToggleButton(text); }

    /**
     * Creates a primary (accent-coloured) toggle button.
     *
     * @param text the button text
     * @return a new primary toggle button
     * @since 1.0.0
     */
    public static ToggleButton primary(String text) { return new ToggleButton(text).variant("primary"); }

    /**
     * Creates a secondary (grey) toggle button.
     *
     * @param text the button text
     * @return a new secondary toggle button
     * @since 1.0.0
     */
    public static ToggleButton secondary(String text) { return new ToggleButton(text).variant("secondary"); }

    /**
     * Creates a success (green) toggle button.
     *
     * @param text the button text
     * @return a new success toggle button
     * @since 1.0.0
     */
    public static ToggleButton success(String text) { return new ToggleButton(text).variant("success"); }

    /**
     * Creates a danger (red) toggle button.
     *
     * @param text the button text
     * @return a new danger toggle button
     * @since 1.0.0
     */
    public static ToggleButton danger(String text) { return new ToggleButton(text).variant("danger"); }

    /**
     * Adds a CSS class for the given variant.
     *
     * @param v the variant class name (null or blank → no-op)
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    /**
     * Sets the selected state.
     *
     * @param s whether the button is pressed
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton selected(boolean s) { setSelected(s); return this; }

    /**
     * Assigns the button to a toggle group.
     *
     * @param tg the toggle group (null → removes the assignment)
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton group(javafx.scene.control.ToggleGroup tg) { setToggleGroup(tg); return this; }

    /**
     * Registers a click handler.
     *
     * @param r the handler (null → no-op)
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton onClick(Runnable r) { if (r != null) setOnAction(e -> r.run()); return this; }

    /**
     * Registers a listener for selected-state changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton onChange(Consumer<Boolean> consumer) {
        if (consumer != null) selectedProperty().addListener((obs, o, n) -> consumer.accept(n));
        return this;
    }

    /**
     * Makes the button fill the available horizontal space.
     *
     * @return this button
     * @since 1.0.0
     */
    public ToggleButton fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
