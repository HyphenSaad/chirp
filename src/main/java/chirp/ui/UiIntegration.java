package chirp.ui;

import chirp.text.Check;
import chirp.validation.Rule;
import chirp.validation.ValidationResult;

/**
 * Glue between Chirp's other modules and the UI — the "one import" layer
 * for cross-module UI work.
 *
 * <pre>{@code
 * // Bind a Field directly to a Check / Rule / registry rule:
 * Field f = UiIntegration.bindRule(Field.of("CNIC"), "PK_CNIC");
 *
 * // Turn a ValidationResult into visible error Labels:
 * Stack errors = UiIntegration.errorsFrom(result);
 *
 * // Build a form field that already knows about a rule by name:
 * Field emailField = UiIntegration.ruleField("Email", "EMAIL");
 * }</pre>
 *
 * <h2>Naming</h2>
 * <p>{@code bindRule / applyRule / bindToRule}; {@code ruleField / createRuleField};
 * {@code errorsFrom / resultToLabels / showErrors}; {@code fieldFromCheck /
 * checkField}.</p>
 */
public final class UiIntegration {

    private UiIntegration() {}

    // ============================================================== field ↔ rule

    /** Binds a Field to a rule from the global registry (by name). */
    public static Field bindRule(Field field, String ruleName) {
        if (field == null) return null;
        Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) field.validate(rule);
        return field;
    }
    public static Field applyRule(Field field, String ruleName) { return bindRule(field, ruleName); }
    public static Field bindToRule(Field field, String ruleName) { return bindRule(field, ruleName); }

    /** Binds a Field to a Rule object. */
    public static Field bindRule(Field field, Rule rule) {
        if (field == null) return null;
        if (rule != null) field.validate(rule);
        return field;
    }
    public static Field applyRule(Field field, Rule rule) { return bindRule(field, rule); }

    /** Creates a Field with a label + registry rule by name. */
    public static Field ruleField(String label, String ruleName) {
        Field f = Field.of(label);
        return bindRule(f, ruleName);
    }
    public static Field createRuleField(String label, String ruleName) { return ruleField(label, ruleName); }

    /** Creates a Field with a label + a {@link Check} predicate. */
    public static Field checkField(String label,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        Field f = Field.of(label);
        if (check != null) f.validate(check::test, errorMessage);
        return f;
    }
    public static Field fieldFromCheck(String label,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        return checkField(label, check, errorMessage);
    }

    // ============================================================== validation → labels

    /**
     * Turns a {@link ValidationResult} into a vertical stack of error labels.
     * Empty when the result has no errors.
     */
    public static Stack errorsFrom(ValidationResult result) {
        Stack s = Stack.v(4);
        if (result == null || !result.hasErrors()) return s;
        for (String error : result.errors()) s.add(Label.error(error));
        return s;
    }
    public static Stack resultToLabels(ValidationResult result) { return errorsFrom(result); }
    public static Stack showErrors(ValidationResult result) { return errorsFrom(result); }

    /**
     * Renders a one-line summary of the first error, or an empty Label
     * when valid.
     */
    public static Label summaryOf(ValidationResult result) {
        if (result == null || !result.hasErrors()) return Label.of("");
        return Label.error(result.firstError());
    }

    // ============================================================== text field checks

    /** Attaches a {@link Check} method to a {@link TextField}. */
    public static TextField bindCheck(TextField field,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        if (field != null && check != null) field.validate(check::test, errorMessage);
        return field;
    }
    public static TextField applyCheck(TextField field,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        return bindCheck(field, check, errorMessage);
    }
}
