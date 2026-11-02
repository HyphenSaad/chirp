package chirp.ui;

import java.util.function.Consumer;

/**
 * Fluent checkbox with optional {@code required()} validation.
 *
 * <p>The {@code required()} mode is useful for "I agree to the terms"
 * checkboxes — a form that submits without it would violate the user's
 * intent. When required, the checkbox carries the {@code invalid} CSS
 * class until it is checked.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Checkbox agree = Checkbox.of("I agree to the terms").required();
 *
 * if (agree.isValid()) {
 *     submit();
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a single-select radio-button group, see {@link RadioGroup}.
 * For a toggle-style button, see {@link ToggleButton}.</p>
 *
 * @see RadioGroup
 * @see ToggleButton
 * @since 1.0.0
 */
public class Checkbox extends javafx.scene.control.CheckBox {

    private boolean required = false;

    private Checkbox(String label) {
        super(label == null ? "" : label);
        selectedProperty().addListener((obs, o, n) -> {
            if (required) runValidation();
        });
    }

    /**
     * Creates a checkbox with no label.
     *
     * @return a new checkbox
     * @see #of(String)
     * @since 1.0.0
     */
    public static Checkbox of() { return new Checkbox(""); }

    /**
     * Creates a checkbox with the given label.
     *
     * @param label the label text (null → "")
     * @return a new checkbox
     * @see #of()
     * @see #checked(String)
     * @since 1.0.0
     */
    public static Checkbox of(String label) { return new Checkbox(label); }

    /**
     * Creates a pre-checked checkbox with the given label.
     *
     * @param l the label
     * @return a new checked checkbox
     * @see #of(String)
     * @since 1.0.0
     */
    public static Checkbox checked(String l) { return new Checkbox(l).selected(true); }

    /**
     * Sets the label text.
     *
     * @param l the text (null → "")
     * @return this checkbox, for chaining
     * @since 1.0.0
     */
    public Checkbox label(String l) { setText(l == null ? "" : l); return this; }

    /**
     * Sets the checked state.
     *
     * @param on whether the checkbox is checked
     * @return this checkbox, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public Checkbox selected(boolean on) { setSelected(on); return this; }

    /**
     * Returns whether the checkbox is currently checked.
     *
     * <p>Mirrors the {@code selected(boolean)} setter — the standard
     * {@code value()} getter/setter pair used by every Chirp control.</p>
     *
     * @return {@code true} if checked, {@code false} otherwise
     * @see #selected(boolean)
     * @since 1.0.0
     */
    public boolean value() { return isSelected(); }

    /**
     * Enables or disables the checkbox.
     *
     * @param d whether to disable
     * @return this checkbox
     * @since 1.0.0
     */
    public Checkbox disabled(boolean d) { setDisable(d); return this; }

    /**
     * Registers a listener for the checked state.
     *
     * @param consumer the listener (null → no-op)
     * @return this checkbox, for chaining
     * @since 1.0.0
     */
    public Checkbox onChange(Consumer<Boolean> consumer) {
        if (consumer != null) {
            selectedProperty().addListener((obs, o, n) -> consumer.accept(n));
        }
        return this;
    }

    /**
     * Requires the checkbox to be checked. The {@code invalid} CSS
     * class appears until it is.
     *
     * @return this checkbox, for chaining
     * @since 1.0.0
     */
    public Checkbox required() {
        this.required = true;
        runValidation();
        return this;
    }

    /**
     * Runs the validator now.
     *
     * @since 1.0.0
     */
    public void runValidation() {
        if (!required) return;
        if (isSelected()) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    /**
     * Checks whether the current checked state passes the validator.
     *
     * @return {@code true} if valid (or not required)
     * @since 1.0.0
     */
    public boolean isValid() { return !required || isSelected(); }
}
