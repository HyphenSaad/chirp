package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Vertical radio group. Single-selection from a set of options.
 *
 * <pre>{@code
 * RadioGroup plan = RadioGroup.of("Free", "Pro", "Enterprise")
 *     .selected("Pro")
 *     .onChange(v -> Console.info("Plan: " + v));
 * }</pre>
 */
public class RadioGroup extends VBox {

    private final ToggleGroup group = new ToggleGroup();
    private Consumer<String> onChange;
    private boolean required = false;

    private RadioGroup() {
        super(6);
    }

    // ---------------------------------------------------------- factories

    public static RadioGroup of(String... options) {
        RadioGroup g = new RadioGroup();
        if (options != null) {
            for (String o : options) if (o != null) g.add(o);
        }
        return g;
    }

    // ---------------------------------------------------------- chaining

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

    public RadioGroup selected(String value) {
        if (value != null) {
            group.getToggles().stream()
                    .filter(t -> value.equals(t.getUserData()))
                    .findFirst()
                    .ifPresent(t -> t.setSelected(true));
        }
        return this;
    }

    public RadioGroup onChange(Consumer<String> consumer) {
        this.onChange = consumer;
        return this;
    }

    public RadioGroup disabled(boolean d) {
        setDisable(d);
        return this;
    }

    /** @return the selected option, or {@code null}. */
    public String value() {
        Toggle t = group.getSelectedToggle();
        return t == null ? null : (String) t.getUserData();
    }

    // ---------------------------------------------------------- validation

    public RadioGroup required() {
        this.required = true;
        runValidation();
        return this;
    }

    public void runValidation() {
        if (!required) return;
        if (group.getSelectedToggle() != null) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    public boolean isValid() {
        return !required || group.getSelectedToggle() != null;
    }
}
