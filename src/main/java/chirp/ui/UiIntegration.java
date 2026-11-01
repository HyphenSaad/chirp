package chirp.ui;

import chirp.text.Check;
import chirp.validation.Rule;
import chirp.validation.ValidationResult;

/**
 * Bridge from the UI module to the validation system.
 *
 * <p>{@code UiIntegration} exists because {@link Field},
 * {@link TextField}, and {@link FormBuilder} need to talk to
 * {@link chirp.validation.Rule}, {@link chirp.validation.Rules},
 * {@link chirp.text.Check}, and {@link ValidationResult} — but a
 * student writing {@code chirp.ui} code should not have to import
 * three other packages to attach validation. Every method here
 * delegates to the right place, so a UI-only program has one import.</p>
 *
 * <h2>Three services</h2>
 *
 * <h3>1. Bind a field to a rule</h3>
 * <p>{@link #bindRule(Field, String)} attaches a registry rule to a
 * {@link Field} by name — equivalent to
 * {@code field.validate(Rules.get(ruleName))} but without the import:</p>
 * <pre>{@code
 * Field email = UiIntegration.bindRule(Field.of("Email"), "EMAIL");
 * // now email.isValid() reflects the EMAIL rule
 * }</pre>
 *
 * <h3>2. Build a field already validated</h3>
 * <p>{@link #ruleField(String, String)} combines a label and a registry
 * rule in one call. {@link #checkField(String, java.util.function.Predicate, String)}
 * is the same idea but takes a {@link Check}-style predicate instead of
 * a registered rule:</p>
 * <pre>{@code
 * // With a registered rule
 * Field phone = UiIntegration.ruleField("Phone", "PK_MOBILE");
 *
 * // With a Check predicate + custom message
 * Field email = UiIntegration.checkField("Email", Check::isEmail, "Invalid email");
 * }</pre>
 *
 * <h3>3. Turn a {@link ValidationResult} into UI</h3>
 * <p>{@link #errorsFrom(ValidationResult)} renders each error as a
 * red {@link Label} in a vertical {@link Stack}. {@link #summaryOf(ValidationResult)}
 * renders only the first error, or an empty label if the result has no
 * errors:</p>
 * <pre>{@code
 * ValidationResult result = Validate.of("Email", input)
 *     .notBlank("required")
 *     .isEmail("invalid")
 *     .validate();
 *
 * if (result.hasErrors()) {
 *     root.getChildren().addAll(UiIntegration.errorsFrom(result).getChildren());
 * }
 * }</pre>
 *
 * <h2>Why a dedicated class</h2>
 * <p>Because "how do I show validation errors from a
 * {@code ValidationResult} in my UI?" and "how do I attach a registry
 * rule to a {@code Field}?" are the two questions every student asks
 * at the same moment. Grouping them here means the answer is one
 * search away.</p>
 *
 * <h2>Related</h2>
 * <p>For the field composite, see {@link Field}. For the fluent form
 * builder, see {@link FormBuilder}. For the validation primitives, see
 * {@link chirp.validation.Rule}, {@link chirp.validation.Rules}, and
 * {@link ValidationResult}. For the raw boolean checks, see
 * {@link chirp.text.Check}.</p>
 *
 * @see Field
 * @see FormBuilder
 * @see chirp.validation.Rule
 * @see chirp.validation.Rules
 * @see chirp.validation.Validate
 * @see ValidationResult
 * @see Check
 * @since 1.0.0
 */
public final class UiIntegration {

    private UiIntegration() {}

    // ============================================================== field ↔ rule

    /**
     * Binds a {@link Field} to a rule from the {@link chirp.validation.Rules}
     * registry by name. A no-op when the rule is not registered — the
     * field keeps whatever validator it had.
     *
     * <pre>{@code
     * Field cnic = UiIntegration.bindRule(Field.of("CNIC"), "PK_CNIC");
     * }</pre>
     *
     * @param field the field to bind (null → null)
     * @param ruleName the rule name from the registry (may be null or unknown → no-op)
     * @return the same field, for chaining
     * @see #applyRule(Field, String)
     * @see #bindToRule(Field, String)
     * @see #bindRule(Field, Rule)
     * @since 1.0.0
     */
    public static Field bindRule(Field field, String ruleName) {
        if (field == null) return null;
        Rule rule = chirp.validation.Rules.get(ruleName);
        if (rule != null) field.validate(rule);
        return field;
    }

    /**
     * Alias of {@link #bindRule(Field, String)} — see that method for
     * full documentation and examples.
     *
     * @param field the field to bind
     * @param ruleName the rule name
     * @return the same field
     * @see #bindRule(Field, String)
     * @since 1.0.0
     */
    public static Field applyRule(Field field, String ruleName) { return bindRule(field, ruleName); }

    /**
     * Alias of {@link #bindRule(Field, String)} — see that method for
     * full documentation and examples.
     *
     * @param field the field to bind
     * @param ruleName the rule name
     * @return the same field
     * @see #bindRule(Field, String)
     * @since 1.0.0
     */
    public static Field bindToRule(Field field, String ruleName) { return bindRule(field, ruleName); }

    /**
     * Binds a {@link Field} to a {@link Rule} object directly. A no-op
     * when the rule is null — the field keeps whatever validator it
     * had.
     *
     * @param field the field to bind (null → null)
     * @param rule the rule to apply (null → no-op)
     * @return the same field
     * @see #applyRule(Field, Rule)
     * @see #bindRule(Field, String)
     * @since 1.0.0
     */
    public static Field bindRule(Field field, Rule rule) {
        if (field == null) return null;
        if (rule != null) field.validate(rule);
        return field;
    }

    /**
     * Alias of {@link #bindRule(Field, Rule)} — see that method for
     * full documentation.
     *
     * @param field the field to bind
     * @param rule the rule to apply
     * @return the same field
     * @see #bindRule(Field, Rule)
     * @since 1.0.0
     */
    public static Field applyRule(Field field, Rule rule) { return bindRule(field, rule); }

    /**
     * Creates a {@link Field} with a label and a rule from the registry
     * — combines the two most common steps of building a validated
     * field into one call.
     *
     * <pre>{@code
     * Field cnic = UiIntegration.ruleField("CNIC", "PK_CNIC");
     * Field email = UiIntegration.ruleField("Email", "EMAIL");
     * }</pre>
     *
     * @param label the field label
     * @param ruleName the registry rule name (may be null or unknown → no validation attached)
     * @return a new field
     * @see #createRuleField(String, String)
     * @see #checkField(String, java.util.function.Predicate, String)
     * @see #bindRule(Field, String)
     * @since 1.0.0
     */
    public static Field ruleField(String label, String ruleName) {
        Field f = Field.of(label);
        return bindRule(f, ruleName);
    }

    /**
     * Alias of {@link #ruleField(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param label the field label
     * @param ruleName the rule name
     * @return a new field
     * @see #ruleField(String, String)
     * @since 1.0.0
     */
    public static Field createRuleField(String label, String ruleName) { return ruleField(label, ruleName); }

    /**
     * Creates a {@link Field} with a label and a {@link Check}-style
     * predicate. Useful when the validation logic is not a registered
     * rule — e.g. a one-off check for a specific field.
     *
     * <pre>{@code
     * Field email = UiIntegration.checkField(
     *     "Email", Check::isEmail, "Invalid email address");
     * Field shortName = UiIntegration.checkField(
     *     "Nickname", s -> s.length() <= 20, "Too long");
     * }</pre>
     *
     * @param label the field label
     * @param check the predicate (null → no validation attached)
     * @param errorMessage the message shown when the predicate fails
     * @return a new field
     * @see #fieldFromCheck(String, java.util.function.Predicate, String)
     * @see #ruleField(String, String)
     * @see Check
     * @since 1.0.0
     */
    public static Field checkField(String label,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        Field f = Field.of(label);
        if (check != null) f.validate(check::test, errorMessage);
        return f;
    }

    /**
     * Alias of {@link #checkField(String, java.util.function.Predicate, String)}
     * — see that method for full documentation and examples.
     *
     * @param label the field label
     * @param check the predicate
     * @param errorMessage the error message
     * @return a new field
     * @see #checkField(String, java.util.function.Predicate, String)
     * @since 1.0.0
     */
    public static Field fieldFromCheck(String label,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        return checkField(label, check, errorMessage);
    }

    // ============================================================== validation → labels

    /**
     * Renders each error in a {@link ValidationResult} as a red
     * {@link Label} in a vertical {@link Stack}.
     *
     * <p>A {@code null} or error-free result produces an empty stack
     * (not a null stack), so the caller can add it to a container
     * without a null check:</p>
     * <pre>{@code
     * root.add(UiIntegration.errorsFrom(result));   // empty when valid
     * }</pre>
     *
     * @param result the validation result (null → empty stack)
     * @return a vertical stack of red error labels
     * @see #resultToLabels(ValidationResult)
     * @see #showErrors(ValidationResult)
     * @see #summaryOf(ValidationResult)
     * @since 1.0.0
     */
    public static Stack errorsFrom(ValidationResult result) {
        Stack s = Stack.v(4);
        if (result == null || !result.hasErrors()) return s;
        for (String error : result.errors()) s.add(Label.error(error));
        return s;
    }

    /**
     * Alias of {@link #errorsFrom(ValidationResult)} — see that method
     * for full documentation and examples.
     *
     * @param result the validation result
     * @return the stack of error labels
     * @see #errorsFrom(ValidationResult)
     * @since 1.0.0
     */
    public static Stack resultToLabels(ValidationResult result) { return errorsFrom(result); }

    /**
     * Alias of {@link #errorsFrom(ValidationResult)} — see that method
     * for full documentation and examples.
     *
     * @param result the validation result
     * @return the stack of error labels
     * @see #errorsFrom(ValidationResult)
     * @since 1.0.0
     */
    public static Stack showErrors(ValidationResult result) { return errorsFrom(result); }

    /**
     * Renders a one-line summary of the first error, or an empty
     * {@link Label} when the result has no errors.
     *
     * <p>Useful for showing a single error summary above a form rather
     * than rendering every error — the per-field errors already do
     * that.</p>
     *
     * @param result the validation result (null → empty label)
     * @return a red label with the first error, or an empty label
     * @see #errorsFrom(ValidationResult)
     * @since 1.0.0
     */
    public static Label summaryOf(ValidationResult result) {
        if (result == null || !result.hasErrors()) return Label.of("");
        return Label.error(result.firstError());
    }

    // ============================================================== text field checks

    /**
     * Attaches a {@link Check} predicate to a {@link TextField} — the
     * direct-{@code TextField} equivalent of
     * {@link #checkField(String, java.util.function.Predicate, String)}
     * for callers who already have a {@code TextField} rather than a
     * {@code Field} composite.
     *
     * @param field the text field to bind (null → null)
     * @param check the predicate (null → no-op)
     * @param errorMessage the error message
     * @return the same field, for chaining
     * @see #applyCheck(TextField, java.util.function.Predicate, String)
     * @see #checkField(String, java.util.function.Predicate, String)
     * @since 1.0.0
     */
    public static TextField bindCheck(TextField field,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        if (field != null && check != null) field.validate(check::test, errorMessage);
        return field;
    }

    /**
     * Alias of {@link #bindCheck(TextField, java.util.function.Predicate, String)}
     * — see that method for full documentation and examples.
     *
     * @param field the text field
     * @param check the predicate
     * @param errorMessage the error message
     * @return the same field
     * @see #bindCheck(TextField, java.util.function.Predicate, String)
     * @since 1.0.0
     */
    public static TextField applyCheck(TextField field,
            java.util.function.Predicate<String> check,
            String errorMessage) {
        return bindCheck(field, check, errorMessage);
    }
}
