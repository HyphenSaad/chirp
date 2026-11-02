package chirp.ui;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Composite form field — label + input + error message + character
 * counter, all wired for validation.
 *
 * <p>The single most useful composite in the UI module. Instead of
 * assembling a label, a text field, an error label, and a counter
 * yourself, declare one {@code Field} and get all four with live
 * validation feedback, automatic error display, and counter updates.</p>
 *
 * <h2>Three variants</h2>
 * <table border="1">
 *   <caption>Field kinds and their behaviour</caption>
 *   <tr><th>Factory</th><th>Input</th><th>Extra feature</th></tr>
 *   <tr><td>{@link #of(String)}</td><td>Single-line text</td>
 *       <td>—</td></tr>
 *   <tr><td>{@link #password(String)}</td><td>Masked text</td>
 *       <td>Built-in Show/Hide toggle</td></tr>
 *   <tr><td>{@link #textarea(String)}</td><td>Multi-line text</td>
 *       <td>Configurable row count</td></tr>
 * </table>
 *
 * <h2>The typical field</h2>
 * <pre>{@code
 * Field name = Field.of("Name")
 *     .required()
 *     .minLength(2)
 *     .maxLength(50)
 *     .fullWidth();
 *
 * Field email = Field.of("Email").email().fullWidth();
 *
 * Field password = Field.password("Password")
 *     .minLength(8)
 *     .hint("At least 8 characters");
 * }</pre>
 *
 * <h2>Reading the value</h2>
 * <p>The input is text — {@link #value()} returns the current string.
 * For numeric fields, use one of the typed getters:</p>
 * <ul>
 *   <li><b>{@link #intValue()}</b> / <b>{@link #doubleValue()}</b> — throw
 *       {@link NumberFormatException} when the current text is not
 *       parseable. Use after {@link #isValid()} returned {@code true}.</li>
 *   <li><b>{@link #intValueOr(int)}</b> / <b>{@link #doubleValueOr(double)}</b>
 *       — return a fallback when parsing fails.</li>
 *   <li><b>{@link #optionalIntValue()}</b> /
 *       <b>{@link #optionalDoubleValue()}</b> — return
 *       {@link OptionalInt} / {@link OptionalDouble}. Use when "no value"
 *       is a legitimate state.</li>
 * </ul>
 *
 * <h2>What validation does</h2>
 * <p>Every field can carry a validator — a predicate that decides
 * whether the current value is valid. When the user types:</p>
 * <ul>
 *   <li>A valid value hides the error and shows the hint (if set)</li>
 *   <li>An invalid value shows the error message in red, applies the
 *       {@code invalid} CSS class to the input's border, and displays
 *       the failure message</li>
 *   <li>An optional character counter appears in the bottom-right corner
 *       (visible only when {@code minLength} or {@code maxLength} is
 *       set)</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For raw single-line inputs without the composite wrapper, see
 * {@link TextField}. For raw masked inputs, see {@link PasswordField}.
 * For the form that orchestrates multiple fields, see
 * {@link FormBuilder}.</p>
 *
 * @see TextField
 * @see PasswordField
 * @see TextArea
 * @see FormBuilder
 * @see chirp.validation.Rule
 * @see chirp.validation.Rules
 * @since 1.0.0
 */
public class Field extends VBox {

    private enum Kind { TEXT, PASSWORD, TEXTAREA }

    private final Label labelLabel = new Label();
    private final Label errorLabel = new Label();
    private final Label counterLabel = new Label();
    private final HBox bottomRow = new HBox(8);
    private final StackPane inputWrapper = new StackPane();

    // Underlying input control
    private final TextInputControl input;

    // Password-only state
    private PasswordField passwordField;
    private javafx.scene.control.TextField plainPasswordField;
    private boolean passwordVisible = false;
    private javafx.scene.control.Button toggleButton;

    // State
    private int maxLength = -1;
    private int minLength = -1;
    private String hintText;
    private boolean showCounter = true;
    private boolean showError = true;

    private Predicate<String> validator;
    private String validatorMessage;

    // ============================================================ factories

    /**
     * Creates a single-line text field with the given label.
     *
     * @param label the label text (null → no label)
     * @return a new field
     * @see #text(String)
     * @see #empty()
     * @see #password(String)
     * @see #textarea(String)
     * @since 1.0.0
     */
    public static Field of(String label) { return new Field(Kind.TEXT, label); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param label the label text
     * @return a new field
     * @see #of(String)
     * @since 1.0.0
     */
    public static Field text(String label) {
        return new Field(Kind.TEXT, label);
    }

    /**
     * Creates a single-line text field with no label. Useful when the
     * label is rendered separately (e.g. in a table row).
     *
     * @return a new field
     * @see #of(String)
     * @since 1.0.0
     */
    public static Field empty() { return new Field(Kind.TEXT, null); }

    /**
     * Creates a masked password field with the given label and a
     * built-in Show/Hide toggle button. Clicking the button reveals the
     * password in plain text.
     *
     * @param label the label text (null → no label)
     * @return a new password field
     * @see #password()
     * @see #of(String)
     * @since 1.0.0
     */
    public static Field password(String label) {
        return new Field(Kind.PASSWORD, label);
    }

    /**
     * Alias of {@link #password(String)} with no label — see that
     * method for full documentation.
     *
     * @return a new password field
     * @see #password(String)
     * @since 1.0.0
     */
    public static Field password() { return new Field(Kind.PASSWORD, null); }

    /**
     * Creates a multi-line text area with the given label. Default row
     * count is 4; use {@link #rows(int)} to change it.
     *
     * @param label the label text (null → no label)
     * @return a new textarea field
     * @see #textarea()
     * @see #rows(int)
     * @since 1.0.0
     */
    public static Field textarea(String label) {
        return new Field(Kind.TEXTAREA, label);
    }

    /**
     * Alias of {@link #textarea(String)} with no label — see that
     * method for full documentation.
     *
     * @return a new textarea field
     * @see #textarea(String)
     * @since 1.0.0
     */
    public static Field textarea() {
        return new Field(Kind.TEXTAREA, null);
    }

    // ============================================================ constructor

    private Field(Kind kind, String label) {
        super(6);

        // --- label ---
        if (label != null && !label.isBlank()) {
            labelLabel.setText(label);
            labelLabel.getStyleClass().add("field-label");
            getChildren().add(labelLabel);
        }

        // --- input ---
        input = switch (kind) {
            case TEXT -> TextField.of();
            case TEXTAREA -> TextArea.of().rows(4);
            case PASSWORD -> {
                passwordField = PasswordField.of();
                plainPasswordField = new javafx.scene.control.TextField();
                plainPasswordField.setVisible(false);
                plainPasswordField.setManaged(false);
                yield passwordField;
            }
        };

        if (kind == Kind.PASSWORD) {
            passwordField.setMaxWidth(Double.MAX_VALUE);
            plainPasswordField.setMaxWidth(Double.MAX_VALUE);
            inputWrapper.getChildren().addAll(plainPasswordField, passwordField);

            toggleButton = buildToggleButton();
            StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleButton, new Insets(0, 6, 0, 0));
            inputWrapper.getChildren().add(toggleButton);
        } else {
            inputWrapper.getChildren().add(input);
        }

        VBox.setVgrow(inputWrapper, Priority.ALWAYS);
        getChildren().add(inputWrapper);

        // --- bottom row: error on left, counter on right ---
        errorLabel.getStyleClass().add("field-error");
        counterLabel.getStyleClass().add("field-counter");
        counterLabel.setMinWidth(Region.USE_PREF_SIZE);

        bottomRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(errorLabel, Priority.ALWAYS);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        bottomRow.getChildren().addAll(errorLabel, counterLabel);
        bottomRow.setPadding(new Insets(2, 0, 0, 0));
        getChildren().add(bottomRow);

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        counterLabel.setVisible(false);
        counterLabel.setManaged(false);

        // Wire text changes
        input.textProperty().addListener((obs, o, n) -> {
            updateError();
            updateCounter();
        });
        if (kind == Kind.PASSWORD && plainPasswordField != null) {
            plainPasswordField.textProperty().addListener((obs, o, n) -> {
                updateError();
                updateCounter();
            });
        }

        updateError();
        updateCounter();
    }

    private javafx.scene.control.Button buildToggleButton() {
        javafx.scene.control.Button b = new javafx.scene.control.Button("Show");
        b.getStyleClass().add("password-toggle");
        b.setFocusTraversable(false);
        b.setMinWidth(Region.USE_PREF_SIZE);
        b.setOnAction(e -> togglePassword());
        return b;
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        String currentValue = input.getText();

        if (passwordVisible) {
            plainPasswordField.setText(currentValue);
            plainPasswordField.setVisible(true);
            plainPasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggleButton.setText("Hide");
        } else {
            passwordField.setText(currentValue);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            plainPasswordField.setVisible(false);
            plainPasswordField.setManaged(false);
            toggleButton.setText("Show");
        }
    }

    // ============================================================ validation

    /**
     * Requires a non-blank value.
     *
     * @return this field, for chaining
     * @see #validate(Predicate, String)
     * @since 1.0.0
     */
    public Field required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    /**
     * Requires a valid email address, using
     * {@link chirp.text.Check#isEmail(String)}.
     *
     * @return this field, for chaining
     * @see chirp.text.Check#isEmail(String)
     * @since 1.0.0
     */
    public Field email() {
        return validate(chirp.text.Check::isEmail, "Invalid email address");
    }

    /**
     * Requires a valid phone number, using
     * {@link chirp.text.Check#isPhone(String)}.
     *
     * @return this field, for chaining
     * @see chirp.text.Check#isPhone(String)
     * @since 1.0.0
     */
    public Field phone() {
        return validate(chirp.text.Check::isPhone, "Invalid phone number");
    }

    /**
     * Requires a valid URL, using {@link chirp.text.Check#isURL(String)}.
     *
     * @return this field, for chaining
     * @see chirp.text.Check#isURL(String)
     * @since 1.0.0
     */
    public Field url() {
        return validate(chirp.text.Check::isURL, "Invalid URL");
    }

    /**
     * Requires a numeric value, using
     * {@link chirp.text.Check#isNumeric(String)}.
     *
     * @return this field, for chaining
     * @see chirp.text.Check#isNumeric(String)
     * @see #intValue()
     * @see #doubleValue()
     * @since 1.0.0
     */
    public Field numeric() {
        return validate(chirp.text.Check::isNumeric, "Must be a number");
    }

    /**
     * Requires a minimum length. Also enables the character counter,
     * which shows {@code "N / min M"} when no {@code maxLength} is set.
     *
     * @param n the minimum length (inclusive)
     * @return this field, for chaining
     * @see #maxLength(int)
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public Field minLength(int n) {
        this.minLength = n;
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    /**
     * Requires a maximum length. Also enables the character counter,
     * which shows {@code "N / M"}.
     *
     * @param n the maximum length (inclusive)
     * @return this field, for chaining
     * @see #minLength(int)
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public Field maxLength(int n) {
        this.maxLength = n;
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    /**
     * Requires the length to fall in the inclusive range
     * {@code [min, max]}. Enables the character counter.
     *
     * @param min the minimum length (inclusive)
     * @param max the maximum length (inclusive)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field lengthBetween(int min, int max) {
        this.minLength = min;
        this.maxLength = max;
        return validate(
                s -> s != null && s.length() >= min && s.length() <= max,
                "Must be between " + min + " and " + max + " characters");
    }

    /**
     * Sets a custom predicate validator. The message is shown when the
     * predicate returns {@code false}.
     *
     * <p>Each call to {@code validate} <b>replaces</b> the previous
     * validator — a field has exactly one active validator at a time.
     * To combine, use a composite predicate.</p>
     *
     * @param predicate the validator predicate
     * @param message the error message shown on failure
     * @return this field, for chaining
     * @see #validate(chirp.validation.Rule)
     * @see #validate(String)
     * @since 1.0.0
     */
    public Field validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        updateError();
        return this;
    }

    /**
     * Sets a {@link chirp.validation.Rule} as the validator.
     *
     * @param rule the rule (null → no-op)
     * @return this field, for chaining
     * @see #validate(String)
     * @see #validate(Predicate, String)
     * @since 1.0.0
     */
    public Field validate(chirp.validation.Rule rule) {
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    /**
     * Sets a rule from the {@link chirp.validation.Rules} registry by
     * name. Unknown rule names are silently ignored.
     *
     * @param ruleName the registered rule name (null or unknown → no-op)
     * @return this field, for chaining
     * @see #validate(chirp.validation.Rule)
     * @since 1.0.0
     */
    public Field validate(String ruleName) {
        chirp.validation.Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    // ============================================================ display options

    /**
     * Sets the placeholder (prompt text) shown when the input is empty.
     *
     * @param p the placeholder (null → empty)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field placeholder(String p) {
        input.setPromptText(p == null ? "" : p);
        if (plainPasswordField != null) plainPasswordField.setPromptText(p == null ? "" : p);
        return this;
    }

    /**
     * Sets a hint shown when the input is valid. Hidden when there is
     * an error. Useful for format reminders ("Format: YYYY-MM-DD") that
     * the user should see until they make a mistake.
     *
     * @param text the hint text (null → no hint)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field hint(String text) {
        this.hintText = text;
        updateError();
        return this;
    }

    /**
     * Enables or disables the character counter in the bottom-right
     * corner.
     *
     * @param show whether to show the counter
     * @return this field, for chaining
     * @see #showCounter()
     * @see #noCounter()
     * @since 1.0.0
     */
    public Field counter(boolean show) {
        this.showCounter = show;
        updateCounter();
        return this;
    }

    /**
     * Alias of {@link #counter(boolean)} with {@code true} — see that
     * method for full documentation.
     *
     * @return this field
     * @see #counter(boolean)
     * @since 1.0.0
     */
    public Field showCounter() { return counter(true); }

    /**
     * Alias of {@link #counter(boolean)} with {@code false} — see that
     * method for full documentation.
     *
     * @return this field
     * @see #counter(boolean)
     * @since 1.0.0
     */
    public Field noCounter() { return counter(false); }

    /**
     * Enables or disables the error message row. Useful when the caller
     * wants to render errors elsewhere (e.g. in a form-level summary).
     *
     * @param show whether to show errors
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field showError(boolean show) {
        this.showError = show;
        updateError();
        return this;
    }

    /**
     * Alias of {@link #showError(boolean)} with {@code false} — see
     * that method for full documentation.
     *
     * @return this field
     * @see #showError(boolean)
     * @since 1.0.0
     */
    public Field noError() { return showError(false); }

    /**
     * Sets the number of visible rows (only meaningful for textarea
     * fields; a no-op on text and password fields).
     *
     * @param r the row count
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field rows(int r) {
        if (input instanceof TextArea ta) ta.rows(r);
        return this;
    }

    /**
     * Registers a submit callback for textarea fields — fires when the
     * user presses Ctrl+Enter (or Cmd+Enter). No-op for text and
     * password fields.
     *
     * @param r the submit callback (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field onSubmit(Runnable r) {
        if (input instanceof TextArea ta) ta.onSubmit(r);
        return this;
    }

    /**
     * Registers an Enter-key callback — fires when the user presses
     * Enter in a text or password field. No-op for textarea fields
     * (Enter inserts a newline there).
     *
     * @param r the enter callback (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field onEnter(Runnable r) {
        if (input instanceof TextField tf) tf.onEnter(r);
        if (input instanceof PasswordField pf) pf.onEnter(r);
        return this;
    }

    /**
     * Registers a callback for validation state transitions — fires
     * whenever the field goes from valid to invalid or vice versa.
     *
     * @param listener the listener (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field onValidationChanged(Consumer<Boolean> listener) {
        if (listener == null) return this;
        if (input instanceof TextField tf) tf.onValidationChanged(listener);
        if (input instanceof PasswordField pf) pf.onValidationChanged(listener);
        if (input instanceof TextArea ta) ta.onValidationChanged(listener);
        listener.accept(isValid());
        return this;
    }

    /**
     * Makes the field fill the available horizontal space.
     *
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        input.setMaxWidth(Double.MAX_VALUE);
        if (plainPasswordField != null) plainPasswordField.setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /**
     * Enables or disables the entire field — label, input, error, and
     * counter all become non-interactive.
     *
     * @param d whether to disable
     * @return this field, for chaining
     * @since 1.0.0
     */
    public Field disabled(boolean d) {
        setDisable(d);
        return this;
    }

    // ============================================================ accessors

    /**
     * Returns the current value. For password fields, returns the
     * currently-visible text (masked or plain) — the underlying value is
     * the same either way.
     *
     * @return the value (never null — returns "" for empty)
     * @see #value(String)
     * @since 1.0.0
     */
    public String value() {
        if (passwordVisible && plainPasswordField != null) {
            return plainPasswordField.getText() == null ? "" : plainPasswordField.getText();
        }
        return input.getText() == null ? "" : input.getText();
    }

    /**
     * Sets the value programmatically. Triggers validation and counter
     * updates.
     *
     * @param v the new value (null → "")
     * @return this field, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public Field value(String v) {
        input.setText(v == null ? "" : v);
        if (plainPasswordField != null) plainPasswordField.setText(v == null ? "" : v);
        return this;
    }

    /**
     * Parses the current text as an {@code int}.
     *
     * <p>Throws {@link NumberFormatException} when the text is not a
     * valid integer — empty field, letters, or mid-typing state. Only
     * call this when you already know the value parses (typically right
     * after {@link #isValid()} returned {@code true}).</p>
     *
     * @return the parsed integer
     * @throws NumberFormatException if the text is not a valid integer
     * @see #intValueOr(int)
     * @see #optionalIntValue()
     * @see #doubleValue()
     * @since 1.0.0
     */
    public int intValue() { return Integer.parseInt(value().trim()); }

    /**
     * Parses the current text as a {@code double}.
     *
     * <p>Throws {@link NumberFormatException} when the text is not a
     * valid number.</p>
     *
     * @return the parsed double
     * @throws NumberFormatException if the text is not a valid number
     * @see #doubleValueOr(double)
     * @see #optionalDoubleValue()
     * @see #intValue()
     * @since 1.0.0
     */
    public double doubleValue() { return Double.parseDouble(value().trim()); }

    /**
     * Parses the current text as an {@code int}, returning {@code
     * fallback} on failure.
     *
     * @param fallback the value to return when parsing fails
     * @return the parsed integer, or {@code fallback}
     * @see #intValue()
     * @see #optionalIntValue()
     * @since 1.0.0
     */
    public int intValueOr(int fallback) {
        try {
            return Integer.parseInt(value().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Parses the current text as a {@code double}, returning {@code
     * fallback} on failure.
     *
     * @param fallback the value to return when parsing fails
     * @return the parsed double, or {@code fallback}
     * @see #doubleValue()
     * @see #optionalDoubleValue()
     * @since 1.0.0
     */
    public double doubleValueOr(double fallback) {
        try {
            return Double.parseDouble(value().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Parses the current text as an {@code int}, returning an empty
     * {@link OptionalInt} when parsing fails.
     *
     * @return the parsed integer, or {@link OptionalInt#empty()}
     * @see #intValue()
     * @see #intValueOr(int)
     * @since 1.0.0
     */
    public OptionalInt optionalIntValue() {
        try {
            return OptionalInt.of(Integer.parseInt(value().trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Parses the current text as a {@code double}, returning an empty
     * {@link OptionalDouble} when parsing fails.
     *
     * @return the parsed double, or {@link OptionalDouble#empty()}
     * @see #doubleValue()
     * @see #doubleValueOr(double)
     * @since 1.0.0
     */
    public OptionalDouble optionalDoubleValue() {
        try {
            return OptionalDouble.of(Double.parseDouble(value().trim()));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    /**
     * Checks whether the current value passes the validator.
     *
     * @return {@code true} if valid (or if there is no validator)
     * @see #error()
     * @since 1.0.0
     */
    public boolean isValid() {
        if (validator == null) return true;
        return validator.test(value());
    }

    /**
     * Returns the current error message, or {@code null} if valid.
     *
     * @return the error message, or null
     * @see #isValid()
     * @since 1.0.0
     */
    public String error() { return isValid() ? null : validatorMessage; }

    /**
     * Returns the underlying input control. Use this when you need a
     * JavaFX-specific method that {@code Field} does not wrap.
     *
     * @return the underlying control
     * @since 1.0.0
     */
    public TextInputControl input() {
        if (passwordVisible && plainPasswordField != null) return plainPasswordField;
        return input;
    }

    // ============================================================ internal updates

    private void updateError() {
        if (!showError) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            input.getStyleClass().remove("invalid");
            if (plainPasswordField != null) plainPasswordField.getStyleClass().remove("invalid");
            return;
        }

        boolean valid = isValid();

        if (valid) {
            input.getStyleClass().remove("invalid");
            if (plainPasswordField != null) plainPasswordField.getStyleClass().remove("invalid");

            if (hintText != null && !hintText.isBlank()) {
                errorLabel.setText(hintText);
                errorLabel.getStyleClass().remove("field-error");
                if (!errorLabel.getStyleClass().contains("field-hint"))
                    errorLabel.getStyleClass().add("field-hint");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        } else {
            if (!input.getStyleClass().contains("invalid"))
                input.getStyleClass().add("invalid");
            if (plainPasswordField != null
                    && !plainPasswordField.getStyleClass().contains("invalid"))
                plainPasswordField.getStyleClass().add("invalid");

            errorLabel.getStyleClass().remove("field-hint");
            if (!errorLabel.getStyleClass().contains("field-error"))
                errorLabel.getStyleClass().add("field-error");
            errorLabel.setText(validatorMessage == null ? "Invalid value" : validatorMessage);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void updateCounter() {
        if (!showCounter || (maxLength <= 0 && minLength <= 0)) {
            counterLabel.setVisible(false);
            counterLabel.setManaged(false);
            return;
        }

        int len = value().length();

        if (maxLength > 0) {
            counterLabel.setText(len + "/" + maxLength);
            boolean over = len > maxLength;
            counterLabel.getStyleClass().remove("over");
            if (over) counterLabel.getStyleClass().add("over");
        } else {
            counterLabel.setText(len + " / min " + minLength);
            counterLabel.getStyleClass().remove("over");
        }

        counterLabel.setVisible(true);
        counterLabel.setManaged(true);
    }
}
