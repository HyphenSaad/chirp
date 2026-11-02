package chirp.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Multi-line text input with validation.
 *
 * <p>The multi-line counterpart to {@link TextField}. Same validation
 * API, plus configurable row count and Ctrl+Enter submit (Enter alone
 * inserts a newline, as the user expects from a text area).</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TextArea notes = TextArea.of()
 *     .placeholder("Enter notes…")
 *     .rows(6)
 *     .maxLength(500)
 *     .onSubmit(() -> save(notes.text()));
 * }</pre>
 *
 * <h2>Enter vs Ctrl+Enter</h2>
 * <p>Pressing Enter inserts a newline. Pressing Ctrl+Enter (or
 * Cmd+Enter on macOS) fires the {@link #onSubmit(Runnable)} callback.
 * This is the standard UX for text areas — form submission does not
 * conflict with adding new lines.</p>
 *
 * <h2>Related</h2>
 * <p>For single-line input, see {@link TextField}. For the composite
 * with label and error, see {@link Field#textarea(String)}.</p>
 *
 * @see TextField
 * @see Field#textarea(String)
 * @since 1.0.0
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

    /**
     * Creates an empty text area.
     *
     * @return a new text area
     * @see #of(String)
     * @see #empty()
     * @since 1.0.0
     */
    public static TextArea of() { return new TextArea(""); }

    /**
     * Creates a text area pre-filled with the given text.
     *
     * @param text the initial text (null → "")
     * @return a new text area
     * @see #of()
     * @since 1.0.0
     */
    public static TextArea of(String text) { return new TextArea(text); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new empty text area
     * @see #of()
     * @since 1.0.0
     */
    public static TextArea empty() { return new TextArea(""); }

    /**
     * Sets the text.
     *
     * @param t the text (null → "")
     * @return this text area, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public TextArea text(String t) { setText(t == null ? "" : t); return this; }

    /**
     * Returns the text currently in the area. Never {@code null}.
     *
     * <p>Mirrors the {@code text(String)} setter — the standard
     * {@code value()} getter used by every Chirp text input.</p>
     *
     * @return the current text, or {@code ""}
     * @see #text(String)
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public String value() { return getText() == null ? "" : getText(); }

    /**
     * Registers a callback that fires every time the text changes — on
     * every keystroke, paste, or delete.
     *
     * @param consumer the callback (may be null — ignored)
     * @return {@code this} for chaining
     * @see #value()
     * @since 1.0.0
     */
    public TextArea onChange(Consumer<String> consumer) {
        if (consumer != null) {
            textProperty().addListener((obs, o, n) -> consumer.accept(n == null ? "" : n));
        }
        return this;
    }

    /**
     * Alias of {@link #onChange(Consumer)}.
     *
     * @param c the callback
     * @return {@code this} for chaining
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public TextArea onInput(Consumer<String> c) { return onChange(c); }

    /**
     * Alias of {@link #onChange(Consumer)}.
     *
     * @param c the callback
     * @return {@code this} for chaining
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public TextArea onEdit(Consumer<String> c) { return onChange(c); }

    /**
     * Sets the placeholder.
     *
     * @param p the placeholder (null → "")
     * @return this text area
     * @since 1.0.0
     */
    public TextArea placeholder(String p) { setPromptText(p == null ? "" : p); return this; }

    /**
     * Sets the number of visible rows.
     *
     * @param r the row count (minimum 1)
     * @return this text area
     * @since 1.0.0
     */
    public TextArea rows(int r) { setPrefRowCount(Math.max(1, r)); return this; }

    /**
     * Enables or disables the text area.
     *
     * @param d whether to disable
     * @return this text area
     * @since 1.0.0
     */
    public TextArea disabled(boolean d) { setDisable(d); return this; }

    /**
     * Makes the text area fill the available horizontal space.
     *
     * @return this text area
     * @since 1.0.0
     */
    public TextArea fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a Ctrl+Enter (Cmd+Enter) submit callback. Enter alone
     * inserts a newline, not a submit.
     *
     * @param r the callback (null → no-op)
     * @return this text area, for chaining
     * @since 1.0.0
     */
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

    /**
     * Requires a non-blank value.
     *
     * @return this text area, for chaining
     * @since 1.0.0
     */
    public TextArea required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    /**
     * Requires a minimum length.
     *
     * @param n the minimum length
     * @return this text area
     * @since 1.0.0
     */
    public TextArea minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    /**
     * Requires a maximum length.
     *
     * @param n the maximum length
     * @return this text area
     * @since 1.0.0
     */
    public TextArea maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    /**
     * Sets a custom predicate validator.
     *
     * @param predicate the validator
     * @param message the error message
     * @return this text area
     * @since 1.0.0
     */
    public TextArea validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        if (predicate != null) runValidation();
        return this;
    }

    /**
     * Runs the validator now.
     *
     * @since 1.0.0
     */
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

    /**
     * Checks whether the current text passes the validator.
     *
     * @return {@code true} if valid (or no validator)
     * @since 1.0.0
     */
    public boolean isValid() {
        if (validator == null) return true;
        String v = getText() == null ? "" : getText();
        return validator.test(v);
    }

    /**
     * Returns the error message, or null if valid.
     *
     * @return the error message, or null
     * @since 1.0.0
     */
    public String error() { return isValid() ? null : validatorMessage; }

    /**
     * Forces a validation pass and returns the current validity.
     *
     * @return {@code true} if valid
     * @since 1.0.0
     */
    public boolean check() {
        runValidation();
        return isValid();
    }

    /**
     * Registers a listener for validation state transitions.
     *
     * @param listener the listener (null → no-op)
     * @return this text area
     * @since 1.0.0
     */
    public TextArea onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }
}
