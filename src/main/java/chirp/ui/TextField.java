package chirp.ui;

import chirp.text.Check;
import chirp.validation.Rule;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;

/**
 * Fluent single-line text field with built-in validation.
 *
 * <p>The raw text input wrapper. For most forms, prefer {@link Field},
 * which composes a text field with a label, error message, and character
 * counter. Use {@code TextField} when you need the input by itself —
 * inside a custom layout, a table cell, or a {@link FormBuilder} field
 * that brings its own label.</p>
 *
 * <h2>What this wrapper adds over {@code new TextField()}</h2>
 * <ul>
 *   <li>Shorthand validators — {@link #required()}, {@link #email()},
 *       {@link #minLength(int)}, and so on</li>
 *   <li>Live validation — the {@code invalid} CSS class appears on
 *       failure as the user types, no wiring needed</li>
 *   <li>Chaining — every setter returns {@code this}</li>
 *   <li>Null-safe setters — {@code text(null)} becomes {@code ""} rather
 *       than a {@code NullPointerException}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TextField email = TextField.of()
 *     .placeholder("you@example.com")
 *     .email()
 *     .onEnter(() -> submit());
 *
 * if (email.isValid()) {
 *     String value = email.value();
 * }
 * }</pre>
 *
 * <h2>Validator semantics</h2>
 * <p>Each call to a validator method <b>replaces</b> the previous one —
 * a text field has exactly one active validator. To combine checks,
 * pass a composite predicate to {@link #validate(Predicate, String)}:</p>
 * <pre>{@code
 * field.validate(s -> s.length() >= 3 && s.matches("[a-z]+"),
 *     "At least 3 lowercase letters");
 * }</pre>
 *
 * <h2>Relationship to {@link Field}</h2>
 * <p>{@code TextField} is the raw input. {@link Field} is the composite
 * that wraps a {@code TextField} (or a {@link PasswordField} or a
 * {@link TextArea}) with a label, error row, and counter. Use {@code Field}
 * for form inputs; use {@code TextField} for anything else.</p>
 *
 * <h2>Related</h2>
 * <p>For the composite, see {@link Field}. For multi-line input, see
 * {@link TextArea}. For masked input, see {@link PasswordField}. For
 * the validation primitives, see {@link chirp.validation.Rule} and
 * {@link chirp.validation.Rules}.</p>
 *
 * @see Field
 * @see TextArea
 * @see PasswordField
 * @see chirp.validation.Rule
 * @since 1.0.0
 */
public class TextField extends javafx.scene.control.TextField {

    private Predicate<String> validator;
    private String validatorMessage;
    private Consumer<Boolean> onValidationChanged;

    private TextField(String text) {
        super(text == null ? "" : text);
        textProperty().addListener((obs, oldV, newV) -> {
            if (validator != null) runValidation();
        });
    }

    // ============================================================== factories

    /**
     * Creates an empty text field.
     *
     * @return a new text field
     * @see #of(String)
     * @see #create()
     * @see #empty()
     * @see #textField()
     * @since 1.0.0
     */
    public static TextField of() { return new TextField(""); }

    /**
     * Creates a text field pre-filled with the given text.
     *
     * @param text the initial text (null → "")
     * @return a new text field
     * @see #of()
     * @since 1.0.0
     */
    public static TextField of(String text) { return new TextField(text); }

    /**
     * Alias of {@link #of()} — see that method for full documentation
     * and examples.
     *
     * @return a new empty text field
     * @see #of()
     * @since 1.0.0
     */
    public static TextField create() { return new TextField(""); }

    /**
     * Alias of {@link #of()} — see that method for full documentation
     * and examples.
     *
     * @return a new empty text field
     * @see #of()
     * @since 1.0.0
     */
    public static TextField empty() { return new TextField(""); }

    /**
     * Alias of {@link #of()} — see that method for full documentation
     * and examples.
     *
     * @return a new empty text field
     * @see #of()
     * @since 1.0.0
     */
    public static TextField textField() { return new TextField(""); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param t the initial text
     * @return a new text field
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextField textField(String t) { return new TextField(t); }

    // ============================================================== text

    /**
     * Sets the text. Triggers validation.
     *
     * @param t the text (null → "")
     * @return this field, for chaining
     * @see #value(String)
     * @since 1.0.0
     */
    public TextField text(String t) { setText(t == null ? "" : t); return this; }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this field
     * @see #text(String)
     * @since 1.0.0
     */
    public TextField value(String t) { return text(t); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this field
     * @see #text(String)
     * @since 1.0.0
     */
    public TextField setTextValue(String t) { return text(t); }

    /**
     * Returns the current text, or "" if empty.
     *
     * @return the text (never null)
     * @see #text(String)
     * @since 1.0.0
     */
    public String value() { return getText() == null ? "" : getText(); }

    // ============================================================== placeholder

    /**
     * Sets the placeholder (prompt text) shown when the input is empty.
     *
     * @param p the placeholder (null → "")
     * @return this field, for chaining
     * @see #prompt(String)
     * @see #hint(String)
     * @since 1.0.0
     */
    public TextField placeholder(String p) { setPromptText(p == null ? "" : p); return this; }

    /**
     * Alias of {@link #placeholder(String)} — see that method for full
     * documentation.
     *
     * @param p the placeholder
     * @return this field
     * @see #placeholder(String)
     * @since 1.0.0
     */
    public TextField prompt(String p) { return placeholder(p); }

    /**
     * Alias of {@link #placeholder(String)} — see that method for full
     * documentation.
     *
     * @param p the placeholder
     * @return this field
     * @see #placeholder(String)
     * @since 1.0.0
     */
    public TextField hint(String p) { return placeholder(p); }

    // ============================================================== disabled / enabled

    /**
     * Enables or disables the field.
     *
     * @param d whether to disable
     * @return this field
     * @see #enabled(boolean)
     * @since 1.0.0
     */
    public TextField disabled(boolean d) { setDisable(d); return this; }

    /**
     * Alias of {@link #disabled(boolean)} with the inverse — see that
     * method for full documentation.
     *
     * @param e whether to enable
     * @return this field
     * @see #disabled(boolean)
     * @since 1.0.0
     */
    public TextField enabled(boolean e) { return disabled(!e); }

    /**
     * Makes the field fill the available horizontal space.
     *
     * @return this field
     * @see #grow()
     * @since 1.0.0
     */
    public TextField fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #fullWidth()
     * @since 1.0.0
     */
    public TextField grow() { return fullWidth(); }

    // ============================================================== enter

    /**
     * Registers a callback for the Enter key.
     *
     * @param r the callback (null → no-op)
     * @return this field, for chaining
     * @see #onReturn(Runnable)
     * @see #onEnterPressed(Runnable)
     * @since 1.0.0
     */
    public TextField onEnter(Runnable r) {
        if (r != null) setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) r.run(); });
        return this;
    }

    /**
     * Alias of {@link #onEnter(Runnable)} — see that method for full
     * documentation.
     *
     * @param r the callback
     * @return this field
     * @see #onEnter(Runnable)
     * @since 1.0.0
     */
    public TextField onReturn(Runnable r) { return onEnter(r); }

    /**
     * Alias of {@link #onEnter(Runnable)} — see that method for full
     * documentation.
     *
     * @param r the callback
     * @return this field
     * @see #onEnter(Runnable)
     * @since 1.0.0
     */
    public TextField onEnterPressed(Runnable r) { return onEnter(r); }

    // ============================================================== change

    /**
     * Registers a listener for text changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this field, for chaining
     * @see #onInput(Consumer)
     * @see #onEdit(Consumer)
     * @since 1.0.0
     */
    public TextField onChange(Consumer<String> consumer) {
        if (consumer != null) {
            textProperty().addListener((obs, o, n) -> consumer.accept(n == null ? "" : n));
        }
        return this;
    }

    /**
     * Alias of {@link #onChange(Consumer)} — see that method for full
     * documentation.
     *
     * @param c the listener
     * @return this field
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public TextField onInput(Consumer<String> c) { return onChange(c); }

    /**
     * Alias of {@link #onChange(Consumer)} — see that method for full
     * documentation.
     *
     * @param c the listener
     * @return this field
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public TextField onEdit(Consumer<String> c) { return onChange(c); }

    // ============================================================== validators

    /**
     * Requires a non-blank value.
     *
     * @return this field, for chaining
     * @see #mandatory()
     * @since 1.0.0
     */
    public TextField required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    /**
     * Alias of {@link #required()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #required()
     * @since 1.0.0
     */
    public TextField mandatory() { return required(); }

    /**
     * Requires a valid email — see {@link chirp.text.Check#isEmail(String)}.
     *
     * @return this field, for chaining
     * @see #isEmail()
     * @see #mail()
     * @since 1.0.0
     */
    public TextField email() { return validate(Check::isEmail, "Invalid email address"); }

    /**
     * Alias of {@link #email()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #email()
     * @since 1.0.0
     */
    public TextField isEmail() { return email(); }

    /**
     * Alias of {@link #email()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #email()
     * @since 1.0.0
     */
    public TextField mail() { return email(); }

    /**
     * Requires a valid phone number — see
     * {@link chirp.text.Check#isPhone(String)}.
     *
     * @return this field, for chaining
     * @see #telephone()
     * @since 1.0.0
     */
    public TextField phone() { return validate(Check::isPhone, "Invalid phone number"); }

    /**
     * Alias of {@link #phone()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #phone()
     * @since 1.0.0
     */
    public TextField telephone() { return phone(); }

    /**
     * Requires a valid URL — see {@link chirp.text.Check#isURL(String)}.
     *
     * @return this field, for chaining
     * @see #link()
     * @since 1.0.0
     */
    public TextField url() { return validate(Check::isURL, "Invalid URL"); }

    /**
     * Alias of {@link #url()} — see that method for full documentation.
     *
     * @return this field
     * @see #url()
     * @since 1.0.0
     */
    public TextField link() { return url(); }

    /**
     * Requires a numeric value — see
     * {@link chirp.text.Check#isNumeric(String)}.
     *
     * @return this field, for chaining
     * @see #number()
     * @see #decimal()
     * @since 1.0.0
     */
    public TextField numeric() { return validate(Check::isNumeric, "Must be a number"); }

    /**
     * Alias of {@link #numeric()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #numeric()
     * @since 1.0.0
     */
    public TextField number() { return numeric(); }

    /**
     * Alias of {@link #numeric()} — see that method for full
     * documentation.
     *
     * @return this field
     * @see #numeric()
     * @since 1.0.0
     */
    public TextField decimal() { return numeric(); }

    /**
     * Requires a minimum length.
     *
     * @param n the minimum length
     * @return this field, for chaining
     * @see #atLeast(int)
     * @see #min(int)
     * @since 1.0.0
     */
    public TextField minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    /**
     * Alias of {@link #minLength(int)} — see that method for full
     * documentation.
     *
     * @param n the minimum length
     * @return this field
     * @see #minLength(int)
     * @since 1.0.0
     */
    public TextField atLeast(int n) { return minLength(n); }

    /**
     * Alias of {@link #minLength(int)} — see that method for full
     * documentation.
     *
     * @param n the minimum length
     * @return this field
     * @see #minLength(int)
     * @since 1.0.0
     */
    public TextField min(int n) { return minLength(n); }

    /**
     * Requires a maximum length.
     *
     * @param n the maximum length
     * @return this field, for chaining
     * @see #atMost(int)
     * @see #max(int)
     * @since 1.0.0
     */
    public TextField maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    /**
     * Alias of {@link #maxLength(int)} — see that method for full
     * documentation.
     *
     * @param n the maximum length
     * @return this field
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public TextField atMost(int n) { return maxLength(n); }

    /**
     * Alias of {@link #maxLength(int)} — see that method for full
     * documentation.
     *
     * @param n the maximum length
     * @return this field
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public TextField max(int n) { return maxLength(n); }

    /**
     * Requires the length to fall in the inclusive range {@code [min, max]}.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @return this field, for chaining
     * @see #length(int, int)
     * @see #size(int, int)
     * @since 1.0.0
     */
    public TextField lengthBetween(int min, int max) {
        return validate(
                s -> s != null && s.length() >= min && s.length() <= max,
                "Must be between " + min + " and " + max + " characters");
    }

    /**
     * Alias of {@link #lengthBetween(int, int)} — see that method for
     * full documentation.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @return this field
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public TextField length(int min, int max) { return lengthBetween(min, max); }

    /**
     * Alias of {@link #lengthBetween(int, int)} — see that method for
     * full documentation.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @return this field
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public TextField size(int min, int max) { return lengthBetween(min, max); }

    /**
     * Sets a custom predicate validator. Replaces any previous
     * validator.
     *
     * @param predicate the validator
     * @param message the error message shown on failure
     * @return this field, for chaining
     * @see #validate(Rule)
     * @see #validate(String)
     * @since 1.0.0
     */
    public TextField validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        if (predicate != null) runValidation();
        return this;
    }

    /**
     * Alias of {@link #validate(Predicate, String)} — see that method
     * for full documentation.
     *
     * @param predicate the validator
     * @param message the error message
     * @return this field
     * @see #validate(Predicate, String)
     * @since 1.0.0
     */
    public TextField rule(Predicate<String> predicate, String message) { return validate(predicate, message); }

    /**
     * Alias of {@link #validate(Predicate, String)} — see that method
     * for full documentation.
     *
     * @param predicate the validator
     * @param message the error message
     * @return this field
     * @see #validate(Predicate, String)
     * @since 1.0.0
     */
    public TextField matches(Predicate<String> predicate, String message) { return validate(predicate, message); }

    /**
     * Sets a {@link Rule} as the validator.
     *
     * @param rule the rule (null → no-op)
     * @return this field, for chaining
     * @see #rule(Rule)
     * @since 1.0.0
     */
    public TextField validate(Rule rule) {
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    /**
     * Alias of {@link #validate(Rule)} — see that method for full
     * documentation.
     *
     * @param rule the rule
     * @return this field
     * @see #validate(Rule)
     * @since 1.0.0
     */
    public TextField rule(Rule rule) { return validate(rule); }

    /**
     * Sets a registered rule by name. Unknown names are silently
     * ignored.
     *
     * @param ruleName the rule name
     * @return this field, for chaining
     * @see #rule(String)
     * @since 1.0.0
     */
    public TextField validate(String ruleName) {
        Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    /**
     * Alias of {@link #validate(String)} — see that method for full
     * documentation.
     *
     * @param ruleName the rule name
     * @return this field
     * @see #validate(String)
     * @since 1.0.0
     */
    public TextField rule(String ruleName) { return validate(ruleName); }

    // ============================================================== status

    /**
     * Runs the validator now — normally called automatically by the
     * text-change listener, but available for forcing a check.
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
     * @return {@code true} if valid (or if there is no validator)
     * @see #valid()
     * @see #ok()
     * @since 1.0.0
     */
    public boolean isValid() {
        if (validator == null) return true;
        String v = getText() == null ? "" : getText();
        return validator.test(v);
    }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation.
     *
     * @return {@code true} if valid
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean valid() { return isValid(); }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation.
     *
     * @return {@code true} if valid
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean ok() { return isValid(); }

    /**
     * Forces a validation pass and returns the current validity.
     *
     * @return {@code true} if valid
     * @see #verify()
     * @since 1.0.0
     */
    public boolean check() {
        runValidation();
        return isValid();
    }

    /**
     * Alias of {@link #check()} — see that method for full
     * documentation.
     *
     * @return {@code true} if valid
     * @see #check()
     * @since 1.0.0
     */
    public boolean verify() { return check(); }

    /**
     * Returns the error message, or null if valid.
     *
     * @return the error message, or null
     * @see #message()
     * @see #getError()
     * @since 1.0.0
     */
    public String error() {
        return isValid() ? null : validatorMessage;
    }

    /**
     * Alias of {@link #error()} — see that method for full
     * documentation.
     *
     * @return the error message, or null
     * @see #error()
     * @since 1.0.0
     */
    public String message() { return error(); }

    /**
     * Alias of {@link #error()} — see that method for full
     * documentation.
     *
     * @return the error message, or null
     * @see #error()
     * @since 1.0.0
     */
    public String getError() { return error(); }

    /**
     * Registers a listener for validation state transitions.
     *
     * @param listener the listener (null → no-op)
     * @return this field, for chaining
     * @see #onValid(Runnable)
     * @see #onInvalid(Runnable)
     * @since 1.0.0
     */
    public TextField onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }

    /**
     * Registers a callback that fires only when the state becomes valid.
     *
     * @param r the callback (null → no-op)
     * @return this field
     * @see #onValidationChanged(Consumer)
     * @since 1.0.0
     */
    public TextField onValid(Runnable r) {
        return onValidationChanged(v -> { if (v && r != null) r.run(); });
    }

    /**
     * Registers a callback that fires only when the state becomes
     * invalid.
     *
     * @param r the callback (null → no-op)
     * @return this field
     * @see #onValidationChanged(Consumer)
     * @since 1.0.0
     */
    public TextField onInvalid(Runnable r) {
        return onValidationChanged(v -> { if (!v && r != null) r.run(); });
    }

    /**
     * Removes any active validator and clears the {@code invalid} CSS
     * class.
     *
     * @return this field, for chaining
     * @since 1.0.0
     */
    public TextField clearValidation() {
        this.validator = null;
        this.validatorMessage = null;
        getStyleClass().remove("invalid");
        return this;
    }
}
