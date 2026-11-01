package chirp.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Multi-line text area with validation. Enter inserts a newline (native);
 * Ctrl+Enter (or Cmd+Enter) fires {@link #onSubmit(Runnable)}.
 */
public class TextArea extends javafx.scene.control.TextArea {

    private Predicate<String> validator;
    private String validatorMessage;
    private Consumer<Boolean> onValidationChanged;

    private TextArea(String text) {
        super(text == null ? "" : text);
        setWrapText(true);
        textProperty().addListener((obs, o, n) -> {
            if (validator != null) runValidation();
        });
    }

    public static TextArea of() { return new TextArea(""); }
    public static TextArea of(String text) { return new TextArea(text); }
    public static TextArea empty() { return new TextArea(""); }

    public TextArea text(String t) { setText(t == null ? "" : t); return this; }
    public TextArea placeholder(String p) { setPromptText(p == null ? "" : p); return this; }
    public TextArea rows(int r) { setPrefRowCount(Math.max(1, r)); return this; }
    public TextArea disabled(boolean d) { setDisable(d); return this; }
    public TextArea fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public TextArea onSubmit(Runnable r) {
        if (r != null) {
            addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ENTER
                        && (e.isControlDown() || e.isMetaDown())) {
                    e.consume();
                    r.run();
                }
            });
        }
        return this;
    }

    // ---------------------------------------------------------- validation

    public TextArea required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    public TextArea minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    public TextArea maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    public TextArea validate(Predicate<String> predicate, String message) {
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

    public TextArea onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }
}
