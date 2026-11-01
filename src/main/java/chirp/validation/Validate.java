package chirp.validation;

import chirp.text.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluent multi-error validator — collects <b>all</b> errors instead of
 * stopping at the first one.
 *
 * <p>Where {@link Rule} answers "did this value pass?", {@code Validate}
 * answers "what are all the reasons this value didn't pass?". It chains
 * independent checks and collects every failure into a single
 * {@link ValidationResult} at the end.</p>
 *
 * <h2>Unlabeled vs. labeled</h2>
 * <p>Two entry points:</p>
 * <ul>
 *   <li>{@link #that(String)} — {@link ValidationResult#errors()} contains
 *       the raw failure messages.</li>
 *   <li>{@link #of(String, String)} — {@link ValidationResult#errors()}
 *       contains each failure prefixed with {@code label + ": "} so that
 *       multiple fields can be merged without ambiguity.</li>
 * </ul>
 *
 * <pre>{@code
 * // Unlabeled — errors: ["Name is required", "Too short"]
 * ValidationResult r1 = Validate.that("")
 *     .notBlank("Name is required")
 *     .minLength(3, "Too short")
 *     .validate();
 *
 * // Labeled — errors: ["Name: Name is required", "Name: Too short"]
 * ValidationResult r2 = Validate.of("Name", "")
 *     .notBlank("Name is required")
 *     .minLength(3, "Too short")
 *     .validate();
 * }</pre>
 *
 * <h2>When to use this vs. {@link Rule}</h2>
 * <ul>
 *   <li>Single rule, single answer? → {@link Rule} or
 *       {@link chirp.text.Check}</li>
 *   <li>Multiple rules, need all reasons at once? → this class</li>
 * </ul>
 *
 * <h2>Every check is independent</h2>
 * <p>Unlike {@link Rule#allOf(Rule...)}, which stops at the first failure,
 * {@code Validate} runs <b>every</b> check regardless of earlier failures.
 * This is what you want for a form where the user should see all the
 * problems in one submission rather than one per attempt.</p>
 *
 * <h2>Merging results across fields</h2>
 * <pre>{@code
 * ValidationResult nameResult  = Validate.of("Name",  name ).notBlank("required").validate();
 * ValidationResult emailResult = Validate.of("Email", email).notBlank("required").isEmail("invalid").validate();
 *
 * ValidationResult combined = nameResult.merge(emailResult);
 * if (combined.hasErrors()) {
 *     combined.errors().forEach(this::error);
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the resulting error collection, see {@link ValidationResult}.
 * For single rules, see {@link Rule}. For registry lookup, see
 * {@link Rules}. For one-shot boolean checks, see
 * {@link chirp.text.Check}.</p>
 *
 * @see ValidationResult
 * @see Rule
 * @see Rules
 * @see chirp.text.Check
 * @since 1.0.0
 */
public final class Validate {

    private final String value;
    private final String label;
    private final List<String> errors = new ArrayList<>();

    private Validate(String value) {
        this(value, null);
    }

    private Validate(String value, String label) {
        this.value = value;
        this.label = label;
    }

    // ============================================================== entry points

    /**
     * Opens an unlabeled validator on the value.
     *
     * @param value the value to validate (may be null)
     * @return a new {@link Validate}
     * @see #on(String)
     * @see #value(String)
     * @see #check(String)
     * @see #field(String)
     * @see #forValue(String)
     * @see #of(String)
     * @see #of(String, String)
     * @since 1.0.0
     */
    public static Validate that(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate on(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate value(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate check(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate field(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate forValue(String value) { return new Validate(value); }

    /**
     * Alias of {@link #that(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to validate
     * @return a new {@link Validate}
     * @see #that(String)
     * @since 1.0.0
     */
    public static Validate of(String value) { return new Validate(value); }

    /**
     * Opens a <b>labeled</b> validator — every collected error will be
     * prefixed with {@code label + ": "}.
     *
     * <pre>{@code
     * ValidationResult r = Validate.of("Email", "bad")
     *     .notBlank("required")
     *     .isEmail("invalid")
     *     .validate();
     * // r.firstError() → "Email: invalid"
     * }</pre>
     *
     * @param label the field label to prefix errors with (may be null)
     * @param value the value to validate (may be null)
     * @return a new labeled {@link Validate}
     * @see #onField(String, String)
     * @see #fieldOf(String, String)
     * @see #of(String)
     * @since 1.0.0
     */
    public static Validate of(String label, String value) {
        return new Validate(value, label);
    }

    /**
     * Alias of {@link #of(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param label the field label
     * @param value the value to validate
     * @return a new labeled {@link Validate}
     * @see #of(String, String)
     * @since 1.0.0
     */
    public static Validate onField(String label, String value) { return of(label, value); }

    /**
     * Alias of {@link #of(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param label the field label
     * @param value the value to validate
     * @return a new labeled {@link Validate}
     * @see #of(String, String)
     * @since 1.0.0
     */
    public static Validate fieldOf(String label, String value) { return of(label, value); }

    // ============================================================== null / blank

    /**
     * Requires the value to be non-blank (non-null, non-empty, and
     * containing at least one non-whitespace character).
     *
     * @param message the failure message
     * @return this validator, for chaining
     * @see #nonEmpty(String)
     * @see #required(String)
     * @since 1.0.0
     */
    public Validate notBlank(String message) {
        if (value == null || value.isBlank()) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #notBlank(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #notBlank(String)
     * @since 1.0.0
     */
    public Validate nonEmpty(String message) { return notBlank(message); }

    /**
     * Alias of {@link #notBlank(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #notBlank(String)
     * @since 1.0.0
     */
    public Validate required(String message) { return notBlank(message); }

    /**
     * Requires the value to be non-null (empty strings pass).
     *
     * @param message the failure message
     * @return this validator
     * @see #notBlank(String)
     * @since 1.0.0
     */
    public Validate notNull(String message) {
        if (value == null) errors.add(message);
        return this;
    }

    // ============================================================== length

    /**
     * Requires the value's length to be at most {@code max}.
     *
     * @param max the maximum length
     * @param message the failure message
     * @return this validator
     * @see #atMost(int, String)
     * @see #maxLen(int, String)
     * @see #minLength(int, String)
     * @since 1.0.0
     */
    public Validate maxLength(int max, String message) {
        if (value != null && value.length() > max) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #maxLength(int, String)} — see that method for full
     * documentation.
     *
     * @param max the maximum length
     * @param message the failure message
     * @return this validator
     * @see #maxLength(int, String)
     * @since 1.0.0
     */
    public Validate atMost(int max, String message) { return maxLength(max, message); }

    /**
     * Alias of {@link #maxLength(int, String)} — see that method for full
     * documentation.
     *
     * @param max the maximum length
     * @param message the failure message
     * @return this validator
     * @see #maxLength(int, String)
     * @since 1.0.0
     */
    public Validate maxLen(int max, String message) { return maxLength(max, message); }

    /**
     * Requires the value's length to be at least {@code min}.
     *
     * @param min the minimum length
     * @param message the failure message
     * @return this validator
     * @see #atLeast(int, String)
     * @see #minLen(int, String)
     * @see #maxLength(int, String)
     * @since 1.0.0
     */
    public Validate minLength(int min, String message) {
        if (value != null && value.length() < min) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #minLength(int, String)} — see that method for full
     * documentation.
     *
     * @param min the minimum length
     * @param message the failure message
     * @return this validator
     * @see #minLength(int, String)
     * @since 1.0.0
     */
    public Validate atLeast(int min, String message) { return minLength(min, message); }

    /**
     * Alias of {@link #minLength(int, String)} — see that method for full
     * documentation.
     *
     * @param min the minimum length
     * @param message the failure message
     * @return this validator
     * @see #minLength(int, String)
     * @since 1.0.0
     */
    public Validate minLen(int min, String message) { return minLength(min, message); }

    /**
     * Requires the value's length to be in {@code [min, max]}.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @param message the failure message
     * @return this validator
     * @see #lengthInRange(int, int, String)
     * @since 1.0.0
     */
    public Validate lengthBetween(int min, int max, String message) {
        if (value != null && (value.length() < min || value.length() > max)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #lengthBetween(int, int, String)} — see that method
     * for full documentation.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @param message the failure message
     * @return this validator
     * @see #lengthBetween(int, int, String)
     * @since 1.0.0
     */
    public Validate lengthInRange(int min, int max, String message) { return lengthBetween(min, max, message); }

    // ============================================================== numeric range

    /**
     * Requires the value to be a number in {@code [min, max]} (inclusive).
     * Non-numeric values fail with the given message.
     *
     * @param min the minimum (inclusive)
     * @param max the maximum (inclusive)
     * @param message the failure message
     * @return this validator
     * @see #range(int, int, String)
     * @since 1.0.0
     */
    public Validate between(int min, int max, String message) {
        if (value == null) { errors.add(message); return this; }
        try {
            int n = Integer.parseInt(value.trim());
            if (n < min || n > max) errors.add(message);
        } catch (NumberFormatException e) {
            errors.add(message);
        }
        return this;
    }

    /**
     * Alias of {@link #between(int, int, String)} — see that method for
     * full documentation.
     *
     * @param min the minimum (inclusive)
     * @param max the maximum (inclusive)
     * @param message the failure message
     * @return this validator
     * @see #between(int, int, String)
     * @since 1.0.0
     */
    public Validate range(int min, int max, String message) { return between(min, max, message); }

    /**
     * Requires the value to be a number at least {@code minimum}.
     *
     * @param minimum the minimum
     * @param message the failure message
     * @return this validator
     * @since 1.0.0
     */
    public Validate min(int minimum, String message) {
        if (value == null) { errors.add(message); return this; }
        try {
            int n = Integer.parseInt(value.trim());
            if (n < minimum) errors.add(message);
        } catch (NumberFormatException e) {
            errors.add(message);
        }
        return this;
    }

    /**
     * Requires the value to be a number at most {@code maximum}.
     *
     * @param maximum the maximum
     * @param message the failure message
     * @return this validator
     * @since 1.0.0
     */
    public Validate max(int maximum, String message) {
        if (value == null) { errors.add(message); return this; }
        try {
            int n = Integer.parseInt(value.trim());
            if (n > maximum) errors.add(message);
        } catch (NumberFormatException e) {
            errors.add(message);
        }
        return this;
    }

    // ============================================================== format checks

    /**
     * Requires the value to fully match the given regex.
     *
     * @param regex the pattern
     * @param message the failure message
     * @return this validator
     * @see #pattern(String, String)
     * @since 1.0.0
     */
    public Validate matches(String regex, String message) {
        if (value != null && !value.matches(regex)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #matches(String, String)} — see that method for full
     * documentation.
     *
     * @param regex the pattern
     * @param message the failure message
     * @return this validator
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public Validate pattern(String regex, String message) { return matches(regex, message); }

    /**
     * Requires the value to be a valid email address.
     *
     * @param message the failure message
     * @return this validator
     * @see #email(String)
     * @see #looksLikeEmail(String)
     * @see Check#isEmail(String)
     * @since 1.0.0
     */
    public Validate isEmail(String message) {
        if (value != null && !Check.isEmail(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #isEmail(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isEmail(String)
     * @since 1.0.0
     */
    public Validate email(String message) { return isEmail(message); }

    /**
     * Alias of {@link #isEmail(String)} — see that method for full
     * documentation.
     *
     * @param msg the failure message
     * @return this validator
     * @see #isEmail(String)
     * @since 1.0.0
     */
    public Validate looksLikeEmail(String msg) { return isEmail(msg); }

    /**
     * Requires the value to be a valid phone number.
     *
     * @param message the failure message
     * @return this validator
     * @see #phone(String)
     * @see Check#isPhone(String)
     * @since 1.0.0
     */
    public Validate isPhone(String message) {
        if (value != null && !Check.isPhone(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #isPhone(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isPhone(String)
     * @since 1.0.0
     */
    public Validate phone(String message) { return isPhone(message); }

    /**
     * Requires the value to be numeric.
     *
     * @param message the failure message
     * @return this validator
     * @see #numeric(String)
     * @see #isNumber(String)
     * @see Check#isNumeric(String)
     * @since 1.0.0
     */
    public Validate isNumeric(String message) {
        if (value != null && !Check.isNumeric(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #isNumeric(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isNumeric(String)
     * @since 1.0.0
     */
    public Validate numeric(String message) { return isNumeric(message); }

    /**
     * Alias of {@link #isNumeric(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isNumeric(String)
     * @since 1.0.0
     */
    public Validate isNumber(String message) { return isNumeric(message); }

    /**
     * Requires the value to be an integer.
     *
     * @param message the failure message
     * @return this validator
     * @see #isInt(String)
     * @see Check#isInteger(String)
     * @since 1.0.0
     */
    public Validate isInteger(String message) {
        if (value != null && !Check.isInteger(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #isInteger(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isInteger(String)
     * @since 1.0.0
     */
    public Validate isInt(String message) { return isInteger(message); }

    /**
     * Requires the value to be a valid URL.
     *
     * @param message the failure message
     * @return this validator
     * @see #isUrl(String)
     * @see Check#isURL(String)
     * @since 1.0.0
     */
    public Validate isURL(String message) {
        if (value != null && !Check.isURL(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #isURL(String)} — see that method for full
     * documentation.
     *
     * @param message the failure message
     * @return this validator
     * @see #isURL(String)
     * @since 1.0.0
     */
    public Validate isUrl(String message) { return isURL(message); }

    /**
     * Requires the value to be one of the given options (case-sensitive).
     *
     * @param message the failure message
     * @param options the allowed values
     * @return this validator
     * @see #oneOf(String, String...)
     * @since 1.0.0
     */
    public Validate isOneOf(String message, String... options) {
        if (value != null) {
            boolean ok = false;
            if (options != null) {
                for (String o : options) if (value.equals(o)) { ok = true; break; }
            }
            if (!ok) errors.add(message);
        }
        return this;
    }

    /**
     * Alias of {@link #isOneOf(String, String...)} — see that method for
     * full documentation.
     *
     * @param message the failure message
     * @param options the allowed values
     * @return this validator
     * @see #isOneOf(String, String...)
     * @since 1.0.0
     */
    public Validate oneOf(String message, String... options) { return isOneOf(message, options); }

    // ============================================================== rule integration

    /**
     * Applies a rule from the {@link Rules} registry by name. Unknown
     * rule names fail the validation.
     *
     * @param ruleName the registry name
     * @param message the failure message
     * @return this validator
     * @see #rule(String, String)
     * @see #against(Rule, String)
     * @see Rules
     * @since 1.0.0
     */
    public Validate is(String ruleName, String message) {
        Rule rule = Rules.get(ruleName);
        if (value != null && (rule == null || !rule.test(value))) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #is(String, String)} — see that method for full
     * documentation.
     *
     * @param ruleName the registry name
     * @param message the failure message
     * @return this validator
     * @see #is(String, String)
     * @since 1.0.0
     */
    public Validate rule(String ruleName, String message) { return is(ruleName, message); }

    /**
     * Deprecated overload kept for symmetry with the {@code is} method
     * name — the boolean flag is ignored. Use {@link #is(String, String)}
     * directly.
     *
     * @param ruleName the registry name
     * @param message the failure message
     * @param useRule ignored
     * @return this validator
     * @deprecated use {@link #is(String, String)} — the boolean was never
     * honoured and will be removed in a future version
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public Validate matches(String ruleName, String message, boolean useRule) {
        if (useRule) return is(ruleName, message);
        return this;
    }

    /**
     * Applies a {@link Rule} object directly.
     *
     * @param rule the rule (null → skips this check)
     * @param message the failure message
     * @return this validator
     * @see #using(Rule, String)
     * @see #is(String, String)
     * @since 1.0.0
     */
    public Validate against(Rule rule, String message) {
        if (value != null && rule != null && !rule.test(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #against(Rule, String)} — see that method for full
     * documentation.
     *
     * @param rule the rule
     * @param message the failure message
     * @return this validator
     * @see #against(Rule, String)
     * @since 1.0.0
     */
    public Validate using(Rule rule, String message) { return against(rule, message); }

    // ============================================================== custom

    /**
     * Adds a failure when the condition is {@code false}. Note the
     * inverted-sounding name — {@code custom} means "add an error unless
     * this passes", not "add an error if this passes".
     *
     * <pre>{@code
     * Validate.that(input)
     *     .custom(input.length() <= 100, "Too long")
     *     .validate();
     * }</pre>
     *
     * @param condition the condition that must be {@code true}
     * @param messageWhenInvalid the message to add when condition is false
     * @return this validator
     * @see #failIf(boolean, String)
     * @since 1.0.0
     */
    public Validate custom(boolean condition, String messageWhenInvalid) {
        if (!condition) errors.add(messageWhenInvalid);
        return this;
    }

    /**
     * Alias of {@link #custom(boolean, String)} — see that method for
     * full documentation.
     *
     * @param condition the condition that must be {@code true}
     * @param message the failure message
     * @return this validator
     * @see #custom(boolean, String)
     * @since 1.0.0
     */
    public Validate failIf(boolean condition, String message) { return custom(condition, message); }

    /**
     * Adds a failure when the predicate does not pass.
     *
     * @param predicate the test (null → skips this check)
     * @param message the failure message
     * @return this validator
     * @see #when(Predicate, String)
     * @since 1.0.0
     */
    public Validate satisfies(Predicate<String> predicate, String message) {
        if (predicate != null && !predicate.test(value)) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #satisfies(Predicate, String)} — see that method for
     * full documentation.
     *
     * @param predicate the test
     * @param message the failure message
     * @return this validator
     * @see #satisfies(Predicate, String)
     * @since 1.0.0
     */
    public Validate when(Predicate<String> predicate, String message) { return satisfies(predicate, message); }

    /**
     * Manually adds an error message.
     *
     * @param message the message to add (null → ignored)
     * @return this validator
     * @see #error(String)
     * @since 1.0.0
     */
    public Validate addError(String message) {
        if (message != null) errors.add(message);
        return this;
    }

    /**
     * Alias of {@link #addError(String)} — see that method for full
     * documentation.
     *
     * @param message the message to add
     * @return this validator
     * @see #addError(String)
     * @since 1.0.0
     */
    public Validate error(String message) { return addError(message); }

    // ============================================================== terminal

    /**
     * Returns the {@link ValidationResult} for this validator.
     *
     * <p>If the validator was created via {@link #of(String, String)}, the
     * label is prefixed to every error in the result — {@code "Email: required"}.
     * Otherwise, the errors are added to the result verbatim.</p>
     *
     * @return the immutable result
     * @see #result()
     * @see #build()
     * @see #done()
     * @since 1.0.0
     */
    public ValidationResult validate() {
        if (label == null) return new ValidationResult(errors);
        return new ValidationResult(errors.stream().map(e -> label + ": " + e).toList());
    }

    /**
     * Alias of {@link #validate()} — see that method for full
     * documentation.
     *
     * @return the result
     * @see #validate()
     * @since 1.0.0
     */
    public ValidationResult result() { return validate(); }

    /**
     * Alias of {@link #validate()} — see that method for full
     * documentation.
     *
     * @return the result
     * @see #validate()
     * @since 1.0.0
     */
    public ValidationResult build() { return validate(); }

    /**
     * Alias of {@link #validate()} — see that method for full
     * documentation.
     *
     * @return the result
     * @see #validate()
     * @since 1.0.0
     */
    public ValidationResult done() { return validate(); }
}
