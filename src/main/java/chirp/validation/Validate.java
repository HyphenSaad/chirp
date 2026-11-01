package chirp.validation;

import chirp.text.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluent validator that collects <b>all</b> errors instead of stopping at the
 * first one.
 *
 * <h2>Naming</h2>
 * <p>Entry points: {@code that} / {@code on} / {@code value} / {@code check}
 * / {@code field} / {@code forValue} / {@code of}.</p>
 *
 * <h2>Labeled validation</h2>
 * <p>{@link #of(String, String)} takes a <em>label</em> as the first argument,
 * so error messages automatically carry the field name:
 * {@code Validate.of("Email", email).isEmail("Invalid")} produces the error
 * {@code "Email: Invalid"}.</p>
 *
 * <pre>{@code
 * ValidationResult r = Validate.of("Email", email)
 *     .notBlank("required")
 *     .isEmail("invalid")
 *     .validate();
 * // r.firstError() → "Email: required"
 * }</pre>
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

    public static Validate that(String value) { return new Validate(value); }
    public static Validate on(String value) { return new Validate(value); }
    public static Validate value(String value) { return new Validate(value); }
    public static Validate check(String value) { return new Validate(value); }
    public static Validate field(String value) { return new Validate(value); }
    public static Validate forValue(String value) { return new Validate(value); }
    public static Validate of(String value) { return new Validate(value); }

    /** Labeled entry point — errors get prefixed with {@code label + ": "}. */
    public static Validate of(String label, String value) {
        return new Validate(value, label);
    }
    public static Validate onField(String label, String value) { return of(label, value); }
    public static Validate fieldOf(String label, String value) { return of(label, value); }

    // ============================================================== null / blank

    public Validate notBlank(String message) {
        if (value == null || value.isBlank()) errors.add(message);
        return this;
    }
    public Validate nonEmpty(String message) { return notBlank(message); }
    public Validate required(String message) { return notBlank(message); }
    public Validate notNull(String message) {
        if (value == null) errors.add(message);
        return this;
    }

    // ============================================================== length

    public Validate maxLength(int max, String message) {
        if (value != null && value.length() > max) errors.add(message);
        return this;
    }
    public Validate atMost(int max, String message) { return maxLength(max, message); }
    public Validate maxLen(int max, String message) { return maxLength(max, message); }

    public Validate minLength(int min, String message) {
        if (value != null && value.length() < min) errors.add(message);
        return this;
    }
    public Validate atLeast(int min, String message) { return minLength(min, message); }
    public Validate minLen(int min, String message) { return minLength(min, message); }

    public Validate lengthBetween(int min, int max, String message) {
        if (value != null && (value.length() < min || value.length() > max)) errors.add(message);
        return this;
    }
    public Validate lengthInRange(int min, int max, String message) { return lengthBetween(min, max, message); }

    // ============================================================== numeric range

    /** Value must be a number in the given range (inclusive). Non-numeric counts as error. */
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
    public Validate range(int min, int max, String message) { return between(min, max, message); }

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

    public Validate matches(String regex, String message) {
        if (value != null && !value.matches(regex)) errors.add(message);
        return this;
    }
    public Validate pattern(String regex, String message) { return matches(regex, message); }

    public Validate isEmail(String message) {
        if (value != null && !Check.isEmail(value)) errors.add(message);
        return this;
    }
    public Validate email(String message) { return isEmail(message); }
    public Validate looksLikeEmail(String msg) { return isEmail(msg); }

    public Validate isPhone(String message) {
        if (value != null && !Check.isPhone(value)) errors.add(message);
        return this;
    }
    public Validate phone(String message) { return isPhone(message); }

    public Validate isNumeric(String message) {
        if (value != null && !Check.isNumeric(value)) errors.add(message);
        return this;
    }
    public Validate numeric(String message) { return isNumeric(message); }
    public Validate isNumber(String message) { return isNumeric(message); }

    public Validate isInteger(String message) {
        if (value != null && !Check.isInteger(value)) errors.add(message);
        return this;
    }
    public Validate isInt(String message) { return isInteger(message); }

    public Validate isURL(String message) {
        if (value != null && !Check.isURL(value)) errors.add(message);
        return this;
    }
    public Validate isUrl(String message) { return isURL(message); }

    /** Value must equal one of the given options. */
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
    public Validate oneOf(String message, String... options) { return isOneOf(message, options); }

    // ============================================================== rule integration

    public Validate is(String ruleName, String message) {
        Rule rule = Rules.get(ruleName);
        if (value != null && (rule == null || !rule.test(value))) errors.add(message);
        return this;
    }
    public Validate rule(String ruleName, String message) { return is(ruleName, message); }
    public Validate matches(String ruleName, String message, boolean useRule) {
        if (useRule) return is(ruleName, message);
        return this;
    }

    public Validate against(Rule rule, String message) {
        if (value != null && rule != null && !rule.test(value)) errors.add(message);
        return this;
    }
    public Validate using(Rule rule, String message) { return against(rule, message); }

    // ============================================================== custom

    public Validate custom(boolean condition, String messageWhenInvalid) {
        if (!condition) errors.add(messageWhenInvalid);
        return this;
    }
    public Validate failIf(boolean condition, String message) { return custom(condition, message); }

    public Validate satisfies(Predicate<String> predicate, String message) {
        if (predicate != null && !predicate.test(value)) errors.add(message);
        return this;
    }
    public Validate when(Predicate<String> predicate, String message) { return satisfies(predicate, message); }

    /** Manually adds an error message. */
    public Validate addError(String message) {
        if (message != null) errors.add(message);
        return this;
    }
    public Validate error(String message) { return addError(message); }

    // ============================================================== terminal

    public ValidationResult validate() {
        if (label == null) return new ValidationResult(errors);
        // Prefix each error with the label
        return new ValidationResult(errors.stream().map(e -> label + ": " + e).toList());
    }
    public ValidationResult result() { return validate(); }
    public ValidationResult build() { return validate(); }
    public ValidationResult done() { return validate(); }
}
