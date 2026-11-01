package chirp.ui;

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
 * Composite field: {@code label + input + error/hint + counter}, all wired
 * for full validation integration.
 *
 * <h2>Three factories</h2>
 * <ul>
 *   <li>{@code Field.of(label)} / {@code Field.text(label)} — single-line</li>
 *   <li>{@code Field.password(label)} — masked, with built-in Show/Hide toggle</li>
 *   <li>{@code Field.textarea(label)} — multi-line</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Field name  = Field.of("Name").required().minLength(2).maxLength(50).fullWidth();
 * Field email = Field.of("Email").email().fullWidth();
 * Field pwd   = Field.password("Password").minLength(8)
 *                     .hint("At least 8 characters");
 *
 * String  v  = name.value();
 * boolean ok = name.isValid();
 * String  e  = name.error();
 * }</pre>
 */
public class Field extends VBox {

    private enum Kind { TEXT, PASSWORD, TEXTAREA }

    private final Kind kind;
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

    /** Text field with the given label. */
    public static Field of(String label) { return new Field(Kind.TEXT, label); }
    /** Static factory — text field with the given label. */
    public static Field text(String label) {
        return new Field(Kind.TEXT, label);
    }
    /** Text field without a label. */
    public static Field empty() { return new Field(Kind.TEXT, null); }

    /** Password field with the given label and built-in Show/Hide toggle. */
    public static Field password(String label) {
        return new Field(Kind.PASSWORD, label);
    }
    /** Password field without a label. */
    public static Field password() { return new Field(Kind.PASSWORD, null); }

    /** Multi-line text area with the given label. */
    public static Field textarea(String label) {
        return new Field(Kind.TEXTAREA, label);
    }
    /** Multi-line text area without a label. */
    public static Field textarea() {
        return new Field(Kind.TEXTAREA, null);
    }

    // ============================================================ constructor

    private Field(Kind kind, String label) {
        super(6);
        this.kind = kind;

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
        String currentValue = input.getText();   // ← was getText()

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

    public Field required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }

    public Field email() {
        return validate(chirp.text.Check::isEmail, "Invalid email address");
    }

    public Field phone() {
        return validate(chirp.text.Check::isPhone, "Invalid phone number");
    }

    public Field url() {
        return validate(chirp.text.Check::isURL, "Invalid URL");
    }

    public Field numeric() {
        return validate(chirp.text.Check::isNumeric, "Must be a number");
    }

    public Field minLength(int n) {
        this.minLength = n;
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }

    public Field maxLength(int n) {
        this.maxLength = n;
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }

    public Field lengthBetween(int min, int max) {
        this.minLength = min;
        this.maxLength = max;
        return validate(
                s -> s != null && s.length() >= min && s.length() <= max,
                "Must be between " + min + " and " + max + " characters");
    }

    public Field validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        updateError();
        return this;
    }

    public Field validate(chirp.validation.Rule rule) {
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    public Field validate(String ruleName) {
        chirp.validation.Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }

    // ============================================================ display options

    public Field placeholder(String p) {
        input.setPromptText(p == null ? "" : p);
        if (plainPasswordField != null) plainPasswordField.setPromptText(p == null ? "" : p);
        return this;
    }

    public Field hint(String text) {
        this.hintText = text;
        updateError();
        return this;
    }

    public Field counter(boolean show) {
        this.showCounter = show;
        updateCounter();
        return this;
    }
    public Field showCounter() { return counter(true); }
    public Field noCounter() { return counter(false); }

    public Field showError(boolean show) {
        this.showError = show;
        updateError();
        return this;
    }
    public Field noError() { return showError(false); }

    public Field rows(int r) {
        if (input instanceof TextArea ta) ta.rows(r);
        return this;
    }

    public Field onSubmit(Runnable r) {
        if (input instanceof TextArea ta) ta.onSubmit(r);
        return this;
    }

    public Field onEnter(Runnable r) {
        if (input instanceof TextField tf) tf.onEnter(r);
        if (input instanceof PasswordField pf) pf.onEnter(r);
        return this;
    }

    public Field onValidationChanged(Consumer<Boolean> listener) {
        if (listener == null) return this;
        if (input instanceof TextField tf) tf.onValidationChanged(listener);
        if (input instanceof PasswordField pf) pf.onValidationChanged(listener);
        if (input instanceof TextArea ta) ta.onValidationChanged(listener);
        listener.accept(isValid());
        return this;
    }

    public Field fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        input.setMaxWidth(Double.MAX_VALUE);
        if (plainPasswordField != null) plainPasswordField.setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    public Field disabled(boolean d) {
        setDisable(d);
        return this;
    }

    // ============================================================ accessors

    public String value() {
        if (passwordVisible && plainPasswordField != null) {
            return plainPasswordField.getText() == null ? "" : plainPasswordField.getText();
        }
        return input.getText() == null ? "" : input.getText();
    }

    public Field value(String v) {
        input.setText(v == null ? "" : v);
        if (plainPasswordField != null) plainPasswordField.setText(v == null ? "" : v);
        return this;
    }

    public boolean isValid() {
        if (validator == null) return true;
        return validator.test(value());
    }

    public String error() { return isValid() ? null : validatorMessage; }

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
