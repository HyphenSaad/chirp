package chirp.ui;

import chirp.text.Check;
import chirp.validation.Rule;

import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.input.KeyCode;

/**
 * Single-line text field with validation.
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code of / create / empty}; {@code textField}.</p>
 *
 * <p>Setters: {@code text / value / setText}; {@code placeholder / prompt /
 * hint}; {@code disabled / enabled}; {@code fullWidth / grow}; {@code onEnter /
 * onReturn}; {@code onChange / onInput / onEdit}.</p>
 *
 * <p>Validators: {@code required / mandatory}; {@code email / isEmail / mail};
 * {@code phone / telephone}; {@code url / link}; {@code numeric / number / decimal};
 * {@code minLength / atLeast / min}; {@code maxLength / atMost / max};
 * {@code lengthBetween / length / size}; {@code validate / rule / matches}.</p>
 *
 * <p>Status: {@code isValid / ok / valid}; {@code check / verify};
 * {@code error / message / getError}; {@code onValidationChanged / onValid /
 * onInvalid}.</p>
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

    public static TextField of() { return new TextField(""); }
    public static TextField of(String text) { return new TextField(text); }
    public static TextField create() { return new TextField(""); }
    public static TextField empty() { return new TextField(""); }
    public static TextField textField() { return new TextField(""); }
    public static TextField textField(String t) { return new TextField(t); }

    // ============================================================== text

    public TextField text(String t) { setText(t == null ? "" : t); return this; }
    public TextField value(String t) { return text(t); }
    public TextField setTextValue(String t) { return text(t); }

    public String value() { return getText() == null ? "" : getText(); }

    // ============================================================== placeholder

    public TextField placeholder(String p) { setPromptText(p == null ? "" : p); return this; }
    public TextField prompt(String p) { return placeholder(p); }
    public TextField hint(String p) { return placeholder(p); }

    // ============================================================== disabled / enabled

    public TextField disabled(boolean d) { setDisable(d); return this; }
    public TextField enabled(boolean e) { return disabled(!e); }

    public TextField fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
    public TextField grow() { return fullWidth(); }

    // ============================================================== enter

    public TextField onEnter(Runnable r) {
        if (r != null) setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) r.run(); });
        return this;
    }
    public TextField onReturn(Runnable r) { return onEnter(r); }
    public TextField onEnterPressed(Runnable r) { return onEnter(r); }

    // ============================================================== change

    public TextField onChange(Consumer<String> consumer) {
        if (consumer != null) {
            textProperty().addListener((obs, o, n) -> consumer.accept(n == null ? "" : n));
        }
        return this;
    }
    public TextField onInput(Consumer<String> c) { return onChange(c); }
    public TextField onEdit(Consumer<String> c) { return onChange(c); }

    // ============================================================== validators

    public TextField required() {
        return validate(s -> s != null && !s.isBlank(), "This field is required");
    }
    public TextField mandatory() { return required(); }

    public TextField email() { return validate(Check::isEmail, "Invalid email address"); }
    public TextField isEmail() { return email(); }
    public TextField mail() { return email(); }

    public TextField phone() { return validate(Check::isPhone, "Invalid phone number"); }
    public TextField telephone() { return phone(); }

    public TextField url() { return validate(Check::isURL, "Invalid URL"); }
    public TextField link() { return url(); }

    public TextField numeric() { return validate(Check::isNumeric, "Must be a number"); }
    public TextField number() { return numeric(); }
    public TextField decimal() { return numeric(); }

    public TextField minLength(int n) {
        return validate(s -> s != null && s.length() >= n,
                "Must be at least " + n + " characters");
    }
    public TextField atLeast(int n) { return minLength(n); }
    public TextField min(int n) { return minLength(n); }

    public TextField maxLength(int n) {
        return validate(s -> s == null || s.length() <= n,
                "Must be at most " + n + " characters");
    }
    public TextField atMost(int n) { return maxLength(n); }
    public TextField max(int n) { return maxLength(n); }

    public TextField lengthBetween(int min, int max) {
        return validate(
                s -> s != null && s.length() >= min && s.length() <= max,
                "Must be between " + min + " and " + max + " characters");
    }
    public TextField length(int min, int max) { return lengthBetween(min, max); }
    public TextField size(int min, int max) { return lengthBetween(min, max); }

    public TextField validate(Predicate<String> predicate, String message) {
        this.validator = predicate;
        this.validatorMessage = message == null ? "Invalid value" : message;
        if (predicate != null) runValidation();
        return this;
    }
    public TextField rule(Predicate<String> predicate, String message) { return validate(predicate, message); }
    public TextField matches(Predicate<String> predicate, String message) { return validate(predicate, message); }

    public TextField validate(Rule rule) {
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }
    public TextField rule(Rule rule) { return validate(rule); }

    public TextField validate(String ruleName) {
        Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) validate(rule::test, rule.message());
        return this;
    }
    public TextField rule(String ruleName) { return validate(ruleName); }

    // ============================================================== status

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
    public boolean valid() { return isValid(); }
    public boolean ok() { return isValid(); }

    /** Forces a validation pass now and returns the resulting validity. */
    public boolean check() {
        runValidation();
        return isValid();
    }
    public boolean verify() { return check(); }

    public String error() {
        return isValid() ? null : validatorMessage;
    }
    public String message() { return error(); }
    public String getError() { return error(); }

    public TextField onValidationChanged(Consumer<Boolean> listener) {
        this.onValidationChanged = listener;
        if (listener != null) listener.accept(isValid());
        return this;
    }
    public TextField onValid(Runnable r) {
        return onValidationChanged(v -> { if (v && r != null) r.run(); });
    }
    public TextField onInvalid(Runnable r) {
        return onValidationChanged(v -> { if (!v && r != null) r.run(); });
    }

    public TextField clearValidation() {
        this.validator = null;
        this.validatorMessage = null;
        getStyleClass().remove("invalid");
        return this;
    }
}
