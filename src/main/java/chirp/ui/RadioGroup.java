package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Vertical radio-button group — single-selection from a small fixed
 * set, with optional {@code required()} validation.
 *
 * <p>Every {@link RadioButton} in the group shares one
 * {@link ToggleGroup}, so selecting one automatically deselects the
 * others — the standard JavaFX mechanism, wrapped in a fluent API.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * RadioGroup plan = RadioGroup.of("Free", "Pro", "Enterprise")
 *     .selected("Pro")
 *     .required()
 *     .onChange(v -> System.out.println("Selected: " + v));
 *
 * if (plan.isValid()) {
 *     String chosen = plan.value();
 * }
 * }</pre>
 *
 * <h2>Radio buttons vs dropdown</h2>
 * <p>Use a {@code RadioGroup} when the option set is small (≤ 5) and
 * visibility of every option matters — a "Plan" selector in a signup
 * form, a "Priority" selector in a bug tracker. Use {@link Select} when
 * the option set is larger or when search is needed.</p>
 *
 * <h2>Related</h2>
 * <p>For a dropdown (many options), see {@link Select}. For a
 * toggle-style button that stays pressed, see {@link ToggleButton}.</p>
 *
 * @see Select
 * @see ToggleButton
 * @since 1.0.0
 */
public class RadioGroup extends VBox {

    private final ToggleGroup group = new ToggleGroup();
    private Consumer<String> onChange;
    private boolean required = false;

    private RadioGroup() {
        super(6);
    }

    /**
     * Creates a radio group with the given options.
     *
     * @param options the option labels (null → empty group)
     * @return a new radio group
     * @since 1.0.0
     */
    public static RadioGroup of(String... options) {
        RadioGroup g = new RadioGroup();
        if (options != null) {
            for (String o : options) if (o != null) g.add(o);
        }
        return g;
    }

    /**
     * Adds a radio button with the given option label.
     *
     * @param option the label (null → no-op)
     * @return this group, for chaining
     * @since 1.0.0
     */
    public RadioGroup add(String option) {
        RadioButton rb = new RadioButton(option);
        rb.setToggleGroup(group);
        rb.setUserData(option);
        rb.selectedProperty().addListener((obs, o, now) -> {
            if (now && onChange != null) onChange.accept(option);
            if (now && required) runValidation();
        });
        getChildren().add(rb);
        return this;
    }

    /**
     * Selects the option with the given label.
     *
     * @param value the option label (null → no-op)
     * @return this group, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public RadioGroup selected(String value) {
        if (value != null) {
            group.getToggles().stream()
                    .filter(t -> value.equals(t.getUserData()))
                    .findFirst()
                    .ifPresent(t -> t.setSelected(true));
        }
        return this;
    }

    /**
     * Registers a listener for the selected option.
     *
     * @param consumer the listener (null → no-op)
     * @return this group, for chaining
     * @since 1.0.0
     */
    public RadioGroup onChange(Consumer<String> consumer) {
        this.onChange = consumer;
        return this;
    }

    /**
     * Enables or disables the group.
     *
     * @param d whether to disable
     * @return this group
     * @since 1.0.0
     */
    public RadioGroup disabled(boolean d) {
        setDisable(d);
        return this;
    }

    /**
     * Returns the selected option label, or null if none is selected.
     *
     * @return the selected label, or null
     * @see #selected(String)
     * @since 1.0.0
     */
    public String value() {
        Toggle t = group.getSelectedToggle();
        return t == null ? null : (String) t.getUserData();
    }

    /**
     * Requires one option to be selected.
     *
     * @return this group, for chaining
     * @since 1.0.0
     */
    public RadioGroup required() {
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
        if (group.getSelectedToggle() != null) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    /**
     * Checks whether the current selection passes the validator.
     *
     * @return {@code true} if valid (or not required)
     * @since 1.0.0
     */
    public boolean isValid() {
        return !required || group.getSelectedToggle() != null;
    }
}
