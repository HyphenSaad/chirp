package chirp.ui;

import java.util.function.Consumer;

/**
 * Fluent numeric slider with optional tick marks.
 *
 * <p>A thin wrapper around {@link javafx.scene.control.Slider} that
 * adds fluent setters, a range/initial-value factory, and a
 * value-change listener.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Slider volume = Slider.of(0, 100, 50)
 *     .showTicks(true)
 *     .blockIncrement(10)
 *     .onChange(v -> setVolume(v));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For numeric input with typed entry and increment buttons, see
 * {@link Spinner}.</p>
 *
 * @see Spinner
 * @since 1.0.0
 */
public class Slider extends javafx.scene.control.Slider {

    private Slider() { super(0, 100, 50); }
    private Slider(double min, double max, double initial) { super(min, max, initial); }

    /**
     * Creates a slider with the range {@code [0, 100]} and initial value
     * {@code 50}.
     *
     * @return a new slider
     * @see #of(double, double, double)
     * @since 1.0.0
     */
    public static Slider of() { return new Slider(); }

    /**
     * Creates a slider with a custom range and initial value.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param v the initial value
     * @return a new slider
     * @see #of()
     * @since 1.0.0
     */
    public static Slider of(double min, double max, double v) { return new Slider(min, max, v); }

    /**
     * Sets the range — both the minimum and maximum.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @return this slider, for chaining
     * @since 1.0.0
     */
    public Slider range(double min, double max) { setMin(min); setMax(max); return this; }

    /**
     * Sets the current value.
     *
     * @param v the new value
     * @return this slider, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public Slider value(double v) { setValue(v); return this; }

    /**
     * Returns the current value.
     *
     * <p>Mirrors the {@code value(double)} setter — the standard
     * {@code value()} getter/setter pair used by every Chirp control.</p>
     *
     * @return the current value
     * @see #value(double)
     * @since 1.0.0
     */
    public double value() { return getValue(); }

    /**
     * Enables or disables tick marks — the small dots or dashes along
     * the track that indicate positions.
     *
     * @param show whether to show ticks
     * @return this slider, for chaining
     * @since 1.0.0
     */
    public Slider showTicks(boolean show) { setShowTickMarks(show); setShowTickLabels(show); return this; }

    /**
     * Sets the increment when the user clicks on the track (not on the
     * thumb).
     *
     * @param step the increment
     * @return this slider, for chaining
     * @since 1.0.0
     */
    public Slider blockIncrement(double step) { setBlockIncrement(step); return this; }

    /**
     * Makes the slider fill the available horizontal space.
     *
     * @return this slider
     * @since 1.0.0
     */
    public Slider fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a listener for value changes.
     *
     * @param c the listener (null → no-op)
     * @return this slider, for chaining
     * @since 1.0.0
     */
    public Slider onChange(Consumer<Double> c) {
        if (c != null) valueProperty().addListener((obs, o, n) -> c.accept(n.doubleValue()));
        return this;
    }
}
