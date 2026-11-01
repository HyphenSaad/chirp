package chirp.validation;

import chirp.internal.Trace;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A reusable, composable validation rule.
 *
 * <h2>Naming</h2>
 * <p>{@code test / passes / isValid / accepts / ok}; {@code firstFailure /
 * validate / check / problem}; {@code allOf / all / every / compose /
 * requireAll}; {@code anyOf / any / atLeastOne / requireAny};
 * {@code not / negate}; {@code optional / mayBeEmpty / nullable};
 * {@code required / mandatory} (fluent); {@code isRequired} (boolean).</p>
 *
 * <p><b>Note:</b> the boolean accessor is {@link #isRequired()}, never
 * {@code required()}. The method {@link #required()} returns a <b>Rule</b>
 * (fluent), and {@link #mandatory()} is an alias for it.</p>
 */
public final class Rule {

    /** Key enum for the key-value factory {@link #from(Object...)}. */
    public enum Key {
        NAME, DESCRIPTION, MESSAGE, REQUIRED,
        PATTERN, STARTS_WITH, ENDS_WITH, CONTAINS,
        MIN_LENGTH, MAX_LENGTH, ONE_OF,
        TRIM, STRIP_SPACES, STRIP_NON_DIGITS, LOWER_CASE, UPPER_CASE
    }

    private final String name;
    private final String description;
    private final String message;
    private final boolean required;
    private final UnaryOperator<String> normalizer;
    private final Function<String, Optional<String>> checker;

    Rule(String name,
            String description,
            String message,
            boolean required,
            UnaryOperator<String> normalizer,
            Function<String, Optional<String>> checker) {
        this.name = name;
        this.description = description;
        this.message = message;
        this.required = required;
        this.normalizer = normalizer == null ? UnaryOperator.identity() : normalizer;
        this.checker = Objects.requireNonNull(checker, "checker");
    }

    // ============================================================== factories

    public static RuleBuilder define(String name) {
        Trace.debug("rule", "define({})", name);
        return new RuleBuilder(name);
    }
    public static RuleBuilder of(String name) { return define(name); }
    public static RuleBuilder named(String name) { return define(name); }
    public static RuleBuilder create(String name) { return define(name); }

    public static Rule simple(String name,
            String message,
            Function<String, Boolean> predicate,
            UnaryOperator<String> normalizer,
            boolean required) {
        return new Rule(name, null, message, required,
                normalizer,
                value -> Boolean.TRUE.equals(predicate.apply(value))
                        ? Optional.empty()
                        : Optional.of(message));
    }

    public static Rule from(Object... keyValues) {
        if (keyValues == null) {
            throw new IllegalArgumentException("Rule.from(...) requires Key.NAME");
        }
        String name = null;
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (keyValues[i] == Key.NAME && keyValues[i + 1] instanceof String s) {
                name = s;
            }
        }
        if (name == null) {
            throw new IllegalArgumentException("Rule.from(...) requires Key.NAME");
        }
        RuleBuilder builder = define(name);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (!(keyValues[i] instanceof Key k) || k == Key.NAME) continue;
            builder.set(k, keyValues[i + 1]);
        }
        return builder.build();
    }

    // ============================================================== composite factories

    public static Rule allOf(Rule... rules) {
        Objects.requireNonNull(rules, "rules");
        if (rules.length == 0) return alwaysTrue("ALL_EMPTY");
        boolean anyRequired = Arrays.stream(rules).anyMatch(Rule::isRequired);
        Trace.debug("rule", "allOf({} rules)", rules.length);
        return new Rule("ALL", "All rules must pass", "One or more rules failed.",
                anyRequired, UnaryOperator.identity(), value -> {
                    for (Rule rule : rules) {
                        Optional<String> failure = rule.firstFailure(value);
                        if (failure.isPresent()) return failure;
                    }
                    return Optional.empty();
                });
    }
    public static Rule all(Rule... rules) { return allOf(rules); }
    public static Rule every(Rule... rules) { return allOf(rules); }
    public static Rule compose(Rule... rules) { return allOf(rules); }
    public static Rule requireAll(Rule... rules) { return allOf(rules); }

    public static Rule anyOf(Rule... rules) {
        Objects.requireNonNull(rules, "rules");
        if (rules.length == 0) return alwaysFalse("ANY_EMPTY");
        boolean anyRequired = Arrays.stream(rules).anyMatch(Rule::isRequired);
        Trace.debug("rule", "anyOf({} rules)", rules.length);
        return new Rule("ANY", "At least one rule must pass",
                "None of the accepted formats matched.",
                anyRequired, UnaryOperator.identity(), value -> {
                    for (Rule rule : rules) {
                        if (rule.test(value)) return Optional.empty();
                    }
                    return Optional.of("None of the accepted formats matched.");
                });
    }
    public static Rule any(Rule... rules) { return anyOf(rules); }
    public static Rule atLeastOne(Rule... rules) { return anyOf(rules); }
    public static Rule requireAny(Rule... rules) { return anyOf(rules); }

    public static Rule none(Rule... rules) {
        Objects.requireNonNull(rules, "rules");
        return not(anyOf(rules));
    }
    public static Rule noneOf(Rule... rules) { return none(rules); }
    public static Rule excluding(Rule... rules) { return none(rules); }

    public static Rule atLeast(int minimum, Rule... rules) {
        Objects.requireNonNull(rules, "rules");
        if (minimum <= 0) return alwaysTrue("AT_LEAST_ZERO");
        if (minimum > rules.length) return alwaysFalse("AT_LEAST_TOO_MANY");
        boolean anyRequired = Arrays.stream(rules).anyMatch(Rule::isRequired);
        Trace.debug("rule", "atLeast({}, {} rules)", minimum, rules.length);
        String msg = "At least " + minimum + " of the " + rules.length + " conditions must pass.";
        return new Rule("AT_LEAST", msg, msg, anyRequired, UnaryOperator.identity(), value -> {
            int passed = 0;
            for (Rule rule : rules) {
                if (rule.test(value)) {
                    passed++;
                    if (passed >= minimum) return Optional.empty();
                }
            }
            return Optional.of(msg);
        });
    }
    public static Rule atLeastN(int n, Rule... r) { return atLeast(n, r); }
    public static Rule minimumOf(int n, Rule... r) { return atLeast(n, r); }

    public static Rule not(Rule rule) {
        Objects.requireNonNull(rule, "rule");
        return new Rule("NOT_" + rule.name(), "Negation of " + rule.name(),
                "This value is not allowed.", true, UnaryOperator.identity(),
                value -> rule.test(value) ? Optional.of("This value is not allowed.") : Optional.empty());
    }
    public static Rule negate(Rule rule) { return not(rule); }
    public static Rule inverse(Rule rule) { return not(rule); }

    private static Rule alwaysTrue(String name) {
        return new Rule(name, "Always passes", "", true, UnaryOperator.identity(),
                value -> Optional.empty());
    }
    private static Rule alwaysFalse(String name) {
        return new Rule(name, "Always fails", "Rule never passes", true,
                UnaryOperator.identity(), value -> Optional.of("Rule never passes"));
    }

    // ============================================================== accessors

    public String name() { return name; }
    public String getName() { return name; }
    public String label() { return name; }

    public String description() { return description != null ? description : name; }
    public String getDescription() { return description(); }

    public String message() { return message; }
    public String getMessage() { return message; }

    /** Boolean accessor. The fluent form is {@link #required()}. */
    public boolean isRequired() { return required; }

    // ============================================================== normalization

    public String normalize(String input) {
        return input == null ? null : normalizer.apply(input);
    }
    public String normalized(String input) { return normalize(input); }

    // ============================================================== evaluation

    public Optional<String> firstFailure(String input) {
        String normalized = normalize(input);
        Trace.trace("rule", "firstFailure(name={}) input=\"{}\"", name, normalized);

        if (normalized == null || normalized.isBlank()) {
            boolean result = required;
            Trace.decision("rule", "blank input requires failure", result);
            return required ? Optional.of("This field is required.") : Optional.empty();
        }
        Optional<String> result = checker.apply(normalized);
        Trace.decision("rule", "rule '" + name + "' passes", result.isEmpty());
        return result;
    }
    public Optional<String> validate(String input) { return firstFailure(input); }
    public Optional<String> check(String input) { return firstFailure(input); }
    public Optional<String> problem(String input) { return firstFailure(input); }
    public Optional<String> firstProblem(String input) { return firstFailure(input); }

    public boolean test(String input) { return firstFailure(input).isEmpty(); }
    public boolean passes(String input) { return test(input); }
    public boolean isValid(String input) { return test(input); }
    public boolean accepts(String input) { return test(input); }
    public boolean ok(String input) { return test(input); }

    public boolean fails(String input) { return !test(input); }
    public boolean isInvalid(String input) { return !test(input); }
    public boolean rejects(String input) { return !test(input); }

    // ============================================================== fluent composition

    public Rule and(Rule other) {
        if (other == null) return this;
        return allOf(this, other);
    }

    public Rule or(Rule other) {
        if (other == null) return this;
        return anyOf(this, other);
    }

    public Rule andNot(Rule other) {
        if (other == null) return this;
        return allOf(this, not(other));
    }

    // ============================================================== fluent mutators

    /** Returns a copy that accepts blank input. */
    public Rule optional() {
        return new Rule(name, description, message, false, normalizer, checker);
    }
    public Rule mayBeEmpty() { return optional(); }
    public Rule nullable() { return optional(); }

    /** Returns a copy that requires non-blank input (fluent). */
    public Rule required() {
        return new Rule(name, description, message, true, normalizer, checker);
    }
    /** Alias for {@link #required()} — fluent. */
    public Rule mandatory() { return required(); }

    public Rule withMessage(String newMessage) {
        String effective = newMessage == null ? message : newMessage;
        return new Rule(name, description, effective, required, normalizer,
                value -> {
                    Optional<String> failure = checker.apply(value);
                    return failure.map(ignored -> effective);
                });
    }
    public Rule message(String newMessage) { return withMessage(newMessage); }

    public Rule withNormalizer(UnaryOperator<String> extra) {
        if (extra == null) return this;
        UnaryOperator<String> combined = v -> v == null ? null : extra.apply(normalizer.apply(v));
        return new Rule(name, description, message, required, combined, checker);
    }

    // ============================================================== display

    @Override
    public String toString() {
        return "Rule[" + name + (required ? ", required" : ", optional") + "]";
    }
}
