package chirp.ui;

import java.util.function.Consumer;

/**
 * Fluent numeric input with plus/minus increment buttons.
 *
 * <p>Wraps a {@link javafx.scene.control.Spinner} specialized to
 * {@code Integer} values, so a student can write
 * {@code Spinner.of(1, 120, 25)} without dealing with the generic
 * {@code SpinnerValueFactory<Integer>} that JavaFX requires. The value
 * is always an {@code int} at the API boundary.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Spinner age = Spinner.of(1, 120, 25)
 *     .onChange(v -> System.out.println("Age: " + v));
 * }</pre>
 *
 * <h2>Editable vs button-only</h2>
 * <p>By default the text portion of the spinner is editable — the user
 * can type a value or click the arrows. Set {@code setEditable(false)}
 * (via the underlying control) if you want button-only input; the
 * fluent wrapper does not expose that because it is rarely the right
 * choice.</p>
 *
 * <h2>Related</h2>
 * <p>For a slider-style numeric input, see {@link Slider}. For a
 * pure text field with numeric validation, see {@link TextField}.</p>
 *
 * @see Slider
 * @see TextField
 * @since 1.0.0
 */
public class Spinner extends javafx.scene.control.Spinner<Integer> {

    private Spinner(int min, int max, int initial) {
        super(min, max, initial);
        setEditable(true);
    }

    /**
     * Creates a spinner with the range {@code [min, max]} and initial
     * value {@code min}.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @return a new spinner
     * @see #of(int, int, int)
     * @since 1.0.0
     */
    public static Spinner of(int min, int max) { return new Spinner(min, max, min); }

    /**
     * Creates a spinner with the range {@code [min, max]} and a custom
     * initial value.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @param initial the initial value
     * @return a new spinner
     * @see #of(int, int)
     * @since 1.0.0
     */
    public static Spinner of(int min, int max, int initial) { return new Spinner(min, max, initial); }

    /**
     * Returns the current value as a primitive {@code int} (0 if the
     * underlying value is somehow null — the spinner guarantees it is
     * never null in practice).
     *
     * @return the current value
     * @see #value(int)
     * @since 1.0.0
     */
    public int value() { return getValue() == null ? 0 : getValue(); }

    /**
     * Sets the current value.
     *
     * @param v the new value
     * @return this spinner, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public Spinner value(int v) { getValueFactory().setValue(v); return this; }

    /**
     * Makes the spinner fill the available horizontal space.
     *
     * @return this spinner
     * @since 1.0.0
     */
    public Spinner fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a listener for value changes.
     *
     * @param c the listener (null → no-op)
     * @return this spinner, for chaining
     * @since 1.0.0
     */
    public Spinner onChange(Consumer<Integer> c) {
        if (c != null) {
            valueProperty().addListener((obs, o, n) -> { if (n != null) c.accept(n); });
        }
        return this;
    }
}
