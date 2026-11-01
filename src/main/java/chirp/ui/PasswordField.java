package chirp.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;

/**
 * Password field with the same validation API as {@link TextField}.
 *
 * <p><b>Note:</b> for most cases use {@link Field#password(String)} which adds
 * a label, counter, error area, and Show/Hide toggle automatically.</p>
 */
public class PasswordField extends javafx.scene.control.PasswordField {

    private Predicate<String> validator;
    private String validatorMessage;
    private Consumer<Boolean> onValidationChanged;

    private PasswordField() {
        // JavaFX PasswordField has only a no-arg constructor.
        textProperty().addListener((obs, o, n) -> {
            if (validator != null) runValidation();
        });
    }

    public static PasswordField of() { return new PasswordField(); }
    public static PasswordField of(String text) { PasswordField p = new PasswordField(); p.setText(text); return p; }

    public PasswordField text(String t) { setText(t == null ? "" : t); return this; }
    public PasswordField placeholder(String p) { setPromptText(p == null ? "" : p); return this; }
    public PasswordField disabled(boolean d) { setDisable(d); return this; }
    public PasswordField fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public PasswordField onEnter(Runnable r) {
        if (r != null) setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) r.run(); });
        return this;
    }

    // ---------------------------------------------------------- validation

    public PasswordField required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    public PasswordField minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    public PasswordField maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    public PasswordField validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        if (predicate != null) runValidation();
        return this;
    }

    public void runValidation() {
        if (validator == null) return;
        boolean wasValid = isValid();
        String v = getText() == null ? "" : getText();
        if (validator.test(v)) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
        boolean nowValid = isValid();
        if (wasValid != nowValid && onValidationChanged != null) {
            onValidationChanged.accept(nowValid);
        }
    }

    public boolean isValid() {
        if (validator == null) return true;
        String v = getText() == null ? "" : getText();
        return validator.test(v);
    }

    public String error() { return isValid() ? null : validatorMessage; }

    /** Forces a validation pass now and returns the resulting validity. */
    public boolean check() {
        runValidation();
        return isValid();
    }

    public PasswordField onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }
}
