package chirp.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;

/**
 * Masked password input with the same validation API as
 * {@link TextField}.
 *
 * <p>{@code PasswordField} shares the exact same validator semantics as
 * {@code TextField} — the only difference is that the underlying JavaFX
 * control masks the characters. Everything else (validation predicates,
 * error messages, the {@code invalid} CSS class, the on-enter callback)
 * works identically.</p>
 *
 * <h2>Show/Hide toggle</h2>
 * <p>This raw {@code PasswordField} does <b>not</b> have a Show/Hide
 * toggle. If you want that UX, use {@link Field#password(String)} — the
 * composite version includes a built-in toggle button.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * PasswordField pwd = PasswordField.of()
 *     .placeholder("Password")
 *     .minLength(8);
 *
 * if (pwd.isValid()) {
 *     authenticate(user.value(), pwd.text());
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the composite with Show/Hide toggle, see
 * {@link Field#password(String)}. For the raw text field, see
 * {@link TextField}.</p>
 *
 * @see Field#password(String)
 * @see TextField
 * @since 1.0.0
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

    /**
     * Creates an empty password field.
     *
     * @return a new password field
     * @see #of(String)
     * @since 1.0.0
     */
    public static PasswordField of() { return new PasswordField(); }

    /**
     * Creates a password field pre-filled with the given text.
     *
     * @param text the initial text (null → "")
     * @return a new password field
     * @see #of()
     * @since 1.0.0
     */
    public static PasswordField of(String text) { PasswordField p = new PasswordField(); p.setText(text); return p; }

    /**
     * Sets the text.
     *
     * @param t the text (null → "")
     * @return this field, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public PasswordField text(String t) { setText(t == null ? "" : t); return this; }

    /**
     * Returns the text currently in the field. Never {@code null}.
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
    public PasswordField onChange(Consumer<String> consumer) {
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
    public PasswordField onInput(Consumer<String> c) { return onChange(c); }

    /**
     * Sets the placeholder (prompt text).
     *
     * @param p the placeholder (null → "")
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField placeholder(String p) { setPromptText(p == null ? "" : p); return this; }

    /**
     * Enables or disables the field.
     *
     * @param d whether to disable
     * @return this field
     * @since 1.0.0
     */
    public PasswordField disabled(boolean d) { setDisable(d); return this; }

    /**
     * Makes the field fill the available horizontal space.
     *
     * @return this field
     * @since 1.0.0
     */
    public PasswordField fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a callback for the Enter key.
     *
     * @param r the callback (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField onEnter(Runnable r) {
        if (r != null) setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) r.run(); });
        return this;
    }

    // ---------------------------------------------------------- validation

    /**
     * Requires a non-blank value.
     *
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    /**
     * Requires a minimum length.
     *
     * @param n the minimum length
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    /**
     * Requires a maximum length.
     *
     * @param n the maximum length
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    /**
     * Sets a custom predicate validator. Replaces any previous
     * validator.
     *
     * @param predicate the validator
     * @param message the error message
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField validate(Predicate<String> predicate, String message) {
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
     * @return this field, for chaining
     * @since 1.0.0
     */
    public PasswordField onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }
}
