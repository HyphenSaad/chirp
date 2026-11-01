package chirp.ui;

import java.util.function.Consumer;

/**
 * Fluent checkbox. Supports {@link #required()} to require the box be checked.
 */
public class Checkbox extends javafx.scene.control.CheckBox {

    private boolean required = false;

    private Checkbox(String label) {
        super(label == null ? "" : label);
        selectedProperty().addListener((obs, o, n) -> {
            if (required) runValidation();
        });
    }

    public static Checkbox of() { return new Checkbox(""); }
    public static Checkbox of(String label) { return new Checkbox(label); }
    public static Checkbox checked(String l) { return new Checkbox(l).selected(true); }

    public Checkbox label(String l) { setText(l == null ? "" : l); return this; }
    public Checkbox selected(boolean on) { setSelected(on); return this; }
    public Checkbox disabled(boolean d) { setDisable(d); return this; }

    public Checkbox onChange(Consumer<Boolean> consumer) {
        if (consumer != null) {
            selectedProperty().addListener((obs, o, n) -> consumer.accept(n));
        }
        return this;
    }

    /** Requires the checkbox to be selected. */
    public Checkbox required() {
        this.required = true;
        runValidation();
        return this;
    }

    public void runValidation() {
        if (!required) return;
        if (isSelected()) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    public boolean isValid() { return !required || isSelected(); }
}
