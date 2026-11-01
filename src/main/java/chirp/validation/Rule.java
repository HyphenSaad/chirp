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
 * <p>Every rule has a name, a normaliser (a transform applied to the
 * input before validation), a checker (the predicate that decides
 * validity), a message (what to show when the rule fails), and a
 * required flag (whether empty input fails the rule). Instances are
 * immutable — every mutator returns a new {@code Rule}.</p>
 *
 * <h2>Building a rule</h2>
 * <p>Use {@link #define(String)} to open a {@link RuleBuilder}, chain the
 * configuration, then call {@link RuleBuilder#build()}:</p>
 * <pre>{@code
 * Rule pkMobile = Rule.define("PK_MOBILE")
 *     .description("Pakistani mobile")
 *     .stripAll("[\\s\\-()]")
 *     .matches("^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$")
 *     .message("Enter a valid Pakistani mobile")
 *     .build();
 * }</pre>
 * <p>Or build one from key-value pairs with {@link #from(Object...)}.</p>
 *
 * <h2>Two ways to check a value</h2>
 * <ul>
 *   <li><b>Boolean:</b> {@link #test(String)} — {@code true} if the
 *       input passes the rule. Aliases: {@code passes}, {@code isValid},
 *       {@code accepts}, {@code ok}.</li>
 *   <li><b>Reason:</b> {@link #firstFailure(String)} — returns an
 *       {@link Optional} that is empty on success, or contains the
 *       failure message on failure. Useful when you need to tell the
 *       user <i>why</i> the value was rejected.</li>
 * </ul>
 *
 * <h2>Composing rules</h2>
 * <p>Rules combine with five operators, each a static factory:</p>
 * <ul>
 *   <li>{@link #allOf(Rule...)} — AND (all must pass)</li>
 *   <li>{@link #anyOf(Rule...)} — OR (at least one must pass)</li>
 *   <li>{@link #not(Rule)} — NOT (inverts a rule)</li>
 *   <li>{@link #atLeast(int, Rule...)} — N-of-M (at least N must pass)</li>
 *   <li>{@link #none(Rule...)} — NONE (no rule may pass)</li>
 * </ul>
 * <p>Also available as instance methods: {@code rule.and(other)},
 * {@code rule.or(other)}, {@code rule.andNot(other)}.</p>
 *
 * <h2>Naming — the required / mandatory trap</h2>
 * <p>Two methods read similarly but do completely different things:</p>
 * <ul>
 *   <li>{@link #required()} — <b>fluent mutator</b>. Returns a new
 *       {@code Rule} that requires non-blank input. Alias:
 *       {@link #mandatory()}.</li>
 *   <li>{@link #isRequired()} — <b>boolean accessor</b>. Returns
 *       {@code true} if this rule currently requires non-blank input.</li>
 * </ul>
 * <p>Use {@code rule.required()} to get a stricter copy; use
 * {@code rule.isRequired()} to check the current state. The naming
 * difference between {@code required} and {@code isRequired} is what
 * disambiguates them.</p>
 *
 * <h2>Example — compose a password rule</h2>
 * <pre>{@code
 * Rule hasUpper  = Rule.define("UPPER").matches(".*[A-Z].*").message("Need an uppercase").build();
 * Rule hasLower  = Rule.define("LOWER").matches(".*[a-z].*").message("Need a lowercase").build();
 * Rule hasDigit  = Rule.define("DIGIT").matches(".*\\d.*").message("Need a digit").build();
 * Rule longEnough = Rule.define("LEN").minLength(8).message("Need 8+ chars").build();
 *
 * Rule strongPwd = Rule.allOf(hasUpper, hasLower, hasDigit, longEnough);
 * strongPwd.test("Abc12345");    // true
 * strongPwd.firstFailure("abc"); // Optional["Need an uppercase"]  (first failure)
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the fluent builder, see {@link RuleBuilder}. For the global
 * registry of named rules, see {@link Rules}. For multi-error validation
 * across fields, see {@link Validate}.</p>
 *
 * @see RuleBuilder
 * @see Rules
 * @see Validate
 * @see chirp.text.Check
 * @since 1.0.0
 */
public final class Rule {

    /**
     * Key enum for the key-value factory {@link #from(Object...)}.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** The rule's unique name (required). */
        NAME,
        /** A human-readable description. */
        DESCRIPTION,
        /** The message shown when validation fails. */
        MESSAGE,
        /** Whether blank input fails the rule. */
        REQUIRED,
        /** A regex the normalised value must match. */
        PATTERN,
        /** A prefix the normalised value must start with. */
        STARTS_WITH,
        /** A suffix the normalised value must end with. */
        ENDS_WITH,
        /** A substring the normalised value must contain. */
        CONTAINS,
        /** Minimum length. */
        MIN_LENGTH,
        /** Maximum length. */
        MAX_LENGTH,
        /** A set of allowed values (as {@code String[]}). */
        ONE_OF,
        /** Trim whitespace from both ends. */
        TRIM,
        /** Strip all whitespace. */
        STRIP_SPACES,
        /** Strip all non-digit characters. */
        STRIP_NON_DIGITS,
        /** Lowercase the value. */
        LOWER_CASE,
        /** Uppercase the value. */
        UPPER_CASE
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

    /**
     * Opens a fluent {@link RuleBuilder} for a new rule.
     *
     * <p>The name is uppercased and trimmed on entry, so "email" and
     * "  EMAIL  " produce the same rule name.</p>
     *
     * @param name the rule's unique name (non-null, non-blank)
     * @return a new {@link RuleBuilder}
     * @throws IllegalArgumentException if the name is null or blank
     * @see RuleBuilder
     * @see #of(String)
     * @see #named(String)
     * @see #create(String)
     * @see #from(Object...)
     * @since 1.0.0
     */
    public static RuleBuilder define(String name) {
        Trace.debug("rule", "define({})", name);
        return new RuleBuilder(name);
    }

    /**
     * Alias of {@link #define(String)} — see that method for full
     * documentation and examples.
     *
     * @param name the rule name
     * @return a new {@link RuleBuilder}
     * @see #define(String)
     * @since 1.0.0
     */
    public static RuleBuilder of(String name) { return define(name); }

    /**
     * Alias of {@link #define(String)} — see that method for full
     * documentation and examples.
     *
     * @param name the rule name
     * @return a new {@link RuleBuilder}
     * @see #define(String)
     * @since 1.0.0
     */
    public static RuleBuilder named(String name) { return define(name); }

    /**
     * Alias of {@link #define(String)} — see that method for full
     * documentation and examples.
     *
     * @param name the rule name
     * @return a new {@link RuleBuilder}
     * @see #define(String)
     * @since 1.0.0
     */
    public static RuleBuilder create(String name) { return define(name); }

    /**
     * Creates a rule from a single predicate function with a fixed
     * message, normaliser, and required flag — a shortcut for rules
     * that don't need a builder.
     *
     * <pre>{@code
     * Rule evenNumber = Rule.simple(
     *     "EVEN",
     *     "Must be an even number",
     *     s -> s.matches("\\d+") && Integer.parseInt(s) % 2 == 0,
     *     UnaryOperator.identity(),
     *     true);
     * }</pre>
     *
     * @param name the rule name
     * @param message the failure message
     * @param predicate the test (returns {@code true} when the value is valid)
     * @param normalizer a pre-validation transform (null → identity)
     * @param required whether blank input fails the rule
     * @return the constructed rule
     * @see #define(String)
     * @since 1.0.0
     */
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

    /**
     * Creates a rule from key-value pairs — a concise alternative to
     * the builder.
     *
     * <p>{@link Key#NAME} is required; all other keys are optional.
     * Missing keys and wrong value types are silently skipped.</p>
     *
     * <pre>{@code
     * Rule r = Rule.from(
     *     Rule.Key.NAME, "MY_RULE",
     *     Rule.Key.PATTERN, "\\d+",
     *     Rule.Key.MESSAGE, "must be digits");
     * }</pre>
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return the constructed rule
     * @throws IllegalArgumentException if {@link Key#NAME} is not supplied
     * @see #define(String)
     * @since 1.0.0
     */
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

    /**
     * Creates a rule that passes only when <b>every</b> supplied rule
     * passes (logical AND).
     *
     * <p>Failures are reported in the order the rules were given — the
     * first failing rule's message becomes the composite's failure
     * message.</p>
     *
     * <pre>{@code
     * Rule upper = Rule.define("UPPER").matches(".*[A-Z].*").message("Need uppercase").build();
     * Rule lower = Rule.define("LOWER").matches(".*[a-z].*").message("Need lowercase").build();
     * Rule both  = Rule.allOf(upper, lower);
     *
     * both.test("Hello");  // true
     * both.test("HELLO");  // false  (fails lower)
     * }</pre>
     *
     * @param rules the rules to AND together (may be empty — always passes)
     * @return the composite rule
     * @see #all(Rule...)
     * @see #every(Rule...)
     * @see #compose(Rule...)
     * @see #requireAll(Rule...)
     * @see #anyOf(Rule...)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #allOf(Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param rules the rules to AND
     * @return the composite rule
     * @see #allOf(Rule...)
     * @since 1.0.0
     */
    public static Rule all(Rule... rules) { return allOf(rules); }

    /**
     * Alias of {@link #allOf(Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param rules the rules to AND
     * @return the composite rule
     * @see #allOf(Rule...)
     * @since 1.0.0
     */
    public static Rule every(Rule... rules) { return allOf(rules); }

    /**
     * Alias of {@link #allOf(Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param rules the rules to AND
     * @return the composite rule
     * @see #allOf(Rule...)
     * @since 1.0.0
     */
    public static Rule compose(Rule... rules) { return allOf(rules); }

    /**
     * Alias of {@link #allOf(Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param rules the rules to AND
     * @return the composite rule
     * @see #allOf(Rule...)
     * @since 1.0.0
     */
    public static Rule requireAll(Rule... rules) { return allOf(rules); }

    /**
     * Creates a rule that passes when <b>at least one</b> supplied rule
     * passes (logical OR).
     *
     * @param rules the rules to OR together (may be empty — always fails)
     * @return the composite rule
     * @see #any(Rule...)
     * @see #atLeastOne(Rule...)
     * @see #requireAny(Rule...)
     * @see #allOf(Rule...)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #anyOf(Rule...)} — see that method for full
     * documentation.
     *
     * @param rules the rules to OR
     * @return the composite rule
     * @see #anyOf(Rule...)
     * @since 1.0.0
     */
    public static Rule any(Rule... rules) { return anyOf(rules); }

    /**
     * Alias of {@link #anyOf(Rule...)} — see that method for full
     * documentation.
     *
     * @param rules the rules to OR
     * @return the composite rule
     * @see #anyOf(Rule...)
     * @since 1.0.0
     */
    public static Rule atLeastOne(Rule... rules) { return anyOf(rules); }

    /**
     * Alias of {@link #anyOf(Rule...)} — see that method for full
     * documentation.
     *
     * @param rules the rules to OR
     * @return the composite rule
     * @see #anyOf(Rule...)
     * @since 1.0.0
     */
    public static Rule requireAny(Rule... rules) { return anyOf(rules); }

    /**
     * Creates a rule that passes only when <b>none</b> of the supplied
     * rules pass (logical NOR).
     *
     * <p>Equivalent to {@code not(anyOf(rules))}. Useful for exclusion
     * lists — "must not match any of these patterns".</p>
     *
     * @param rules the rules to exclude
     * @return the composite rule
     * @see #noneOf(Rule...)
     * @see #excluding(Rule...)
     * @see #not(Rule)
     * @since 1.0.0
     */
    public static Rule none(Rule... rules) {
        Objects.requireNonNull(rules, "rules");
        return not(anyOf(rules));
    }

    /**
     * Alias of {@link #none(Rule...)} — see that method for full
     * documentation.
     *
     * @param rules the rules to exclude
     * @return the composite rule
     * @see #none(Rule...)
     * @since 1.0.0
     */
    public static Rule noneOf(Rule... rules) { return none(rules); }

    /**
     * Alias of {@link #none(Rule...)} — see that method for full
     * documentation.
     *
     * @param rules the rules to exclude
     * @return the composite rule
     * @see #none(Rule...)
     * @since 1.0.0
     */
    public static Rule excluding(Rule... rules) { return none(rules); }

    /**
     * Creates a rule that passes when at least {@code minimum} of the
     * supplied rules pass (N-of-M).
     *
     * <p>Useful for password strength: "must satisfy at least 3 of these
     * 4 criteria".</p>
     *
     * <pre>{@code
     * Rule strongPwd = Rule.atLeast(3,
     *     Rule.define("UPPER").matches(".*[A-Z].*").message("upper").build(),
     *     Rule.define("LOWER").matches(".*[a-z].*").message("lower").build(),
     *     Rule.define("DIGIT").matches(".*\\d.*").message("digit").build(),
     *     Rule.define("SYMBOL").matches(".*[^A-Za-z0-9].*").message("symbol").build());
     *
     * strongPwd.test("Abc1");   // true  (upper, lower, digit = 3)
     * strongPwd.test("abc1");   // false (lower, digit = 2)
     * }</pre>
     *
     * @param minimum the minimum number of rules that must pass (≤ 0 →
     * always passes; &gt; rule count → always fails)
     * @param rules the rules to check
     * @return the composite rule
     * @see #atLeastN(int, Rule...)
     * @see #minimumOf(int, Rule...)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #atLeast(int, Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param n the minimum number of rules that must pass
     * @param r the rules to check
     * @return the composite rule
     * @see #atLeast(int, Rule...)
     * @since 1.0.0
     */
    public static Rule atLeastN(int n, Rule... r) { return atLeast(n, r); }

    /**
     * Alias of {@link #atLeast(int, Rule...)} — see that method for full
     * documentation and examples.
     *
     * @param n the minimum number of rules that must pass
     * @param r the rules to check
     * @return the composite rule
     * @see #atLeast(int, Rule...)
     * @since 1.0.0
     */
    public static Rule minimumOf(int n, Rule... r) { return atLeast(n, r); }

    /**
     * Creates the logical negation of a rule — passes when the original
     * fails and vice versa.
     *
     * @param rule the rule to negate
     * @return the negated rule
     * @see #negate(Rule)
     * @see #inverse(Rule)
     * @see #none(Rule...)
     * @since 1.0.0
     */
    public static Rule not(Rule rule) {
        Objects.requireNonNull(rule, "rule");
        return new Rule("NOT_" + rule.name(), "Negation of " + rule.name(),
                "This value is not allowed.", true, UnaryOperator.identity(),
                value -> rule.test(value) ? Optional.of("This value is not allowed.") : Optional.empty());
    }

    /**
     * Alias of {@link #not(Rule)} — see that method for full
     * documentation.
     *
     * @param rule the rule to negate
     * @return the negated rule
     * @see #not(Rule)
     * @since 1.0.0
     */
    public static Rule negate(Rule rule) { return not(rule); }

    /**
     * Alias of {@link #not(Rule)} — see that method for full
     * documentation.
     *
     * @param rule the rule to negate
     * @return the negated rule
     * @see #not(Rule)
     * @since 1.0.0
     */
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

    /**
     * Returns the rule's unique name — uppercased and trimmed.
     *
     * @return the name
     * @see #getName()
     * @see #label()
     * @since 1.0.0
     */
    public String name() { return name; }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the name
     * @see #name()
     * @since 1.0.0
     */
    public String getName() { return name; }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the name
     * @see #name()
     * @since 1.0.0
     */
    public String label() { return name; }

    /**
     * Returns the rule's human-readable description, or its name if no
     * description was set.
     *
     * @return the description (never null)
     * @see #getDescription()
     * @since 1.0.0
     */
    public String description() { return description != null ? description : name; }

    /**
     * Alias of {@link #description()} — see that method for full
     * documentation.
     *
     * @return the description
     * @see #description()
     * @since 1.0.0
     */
    public String getDescription() { return description(); }

    /**
     * Returns the message shown when the rule fails.
     *
     * @return the failure message
     * @see #getMessage()
     * @since 1.0.0
     */
    public String message() { return message; }

    /**
     * Alias of {@link #message()} — see that method for full
     * documentation.
     *
     * @return the failure message
     * @see #message()
     * @since 1.0.0
     */
    public String getMessage() { return message; }

    /**
     * Checks whether blank input fails the rule.
     *
     * <p>This is the <b>boolean accessor</b> — for the fluent mutator
     * that makes the rule required, use {@link #required()}.</p>
     *
     * @return {@code true} if the rule requires non-blank input
     * @see #required()
     * @since 1.0.0
     */
    public boolean isRequired() { return required; }

    // ============================================================== normalization

    /**
     * Applies the rule's normaliser to the input. Null input is
     * returned unchanged.
     *
     * <pre>{@code
     * Rule pkMobile = Rules.pakistaniMobile();
     * pkMobile.normalize("0300-1234 567");   // "03001234567"
     * }</pre>
     *
     * @param input the value to normalize (may be null)
     * @return the normalised value, or null
     * @see #normalized(String)
     * @since 1.0.0
     */
    public String normalize(String input) {
        return input == null ? null : normalizer.apply(input);
    }

    /**
     * Alias of {@link #normalize(String)} — see that method for full
     * documentation.
     *
     * @param input the value to normalize
     * @return the normalised value
     * @see #normalize(String)
     * @since 1.0.0
     */
    public String normalized(String input) { return normalize(input); }

    // ============================================================== evaluation

    /**
     * Applies the rule and returns the first failure reason, or an empty
     * {@link Optional} on success.
     *
     * <p>Runs the normaliser first, then the checker on the normalised
     * value. Blank input fails if the rule is required; blank input
     * passes otherwise.</p>
     *
     * @param input the value to check (may be null)
     * @return {@link Optional#empty()} on pass, or the failure message
     * @see #firstFailure(String)
     * @see #validate(String)
     * @see #test(String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #firstFailure(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@link Optional#empty()} on pass, or the failure message
     * @see #firstFailure(String)
     * @since 1.0.0
     */
    public Optional<String> validate(String input) { return firstFailure(input); }

    /**
     * Alias of {@link #firstFailure(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@link Optional#empty()} on pass, or the failure message
     * @see #firstFailure(String)
     * @since 1.0.0
     */
    public Optional<String> check(String input) { return firstFailure(input); }

    /**
     * Alias of {@link #firstFailure(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@link Optional#empty()} on pass, or the failure message
     * @see #firstFailure(String)
     * @since 1.0.0
     */
    public Optional<String> problem(String input) { return firstFailure(input); }

    /**
     * Alias of {@link #firstFailure(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@link Optional#empty()} on pass, or the failure message
     * @see #firstFailure(String)
     * @since 1.0.0
     */
    public Optional<String> firstProblem(String input) { return firstFailure(input); }

    /**
     * Checks whether the value passes the rule. Returns {@code true} on
     * pass, {@code false} on any failure.
     *
     * <p>Equivalent to {@code firstFailure(input).isEmpty()}. Use
     * {@link #firstFailure(String)} if you need the reason.</p>
     *
     * @param input the value to check (may be null)
     * @return {@code true} on pass
     * @see #passes(String)
     * @see #isValid(String)
     * @see #accepts(String)
     * @see #ok(String)
     * @see #firstFailure(String)
     * @since 1.0.0
     */
    public boolean test(String input) { return firstFailure(input).isEmpty(); }

    /**
     * Alias of {@link #test(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on pass
     * @see #test(String)
     * @since 1.0.0
     */
    public boolean passes(String input) { return test(input); }

    /**
     * Alias of {@link #test(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on pass
     * @see #test(String)
     * @since 1.0.0
     */
    public boolean isValid(String input) { return test(input); }

    /**
     * Alias of {@link #test(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on pass
     * @see #test(String)
     * @since 1.0.0
     */
    public boolean accepts(String input) { return test(input); }

    /**
     * Alias of {@link #test(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on pass
     * @see #test(String)
     * @since 1.0.0
     */
    public boolean ok(String input) { return test(input); }

    /**
     * Inverse of {@link #test(String)} — {@code true} when the value
     * fails.
     *
     * @param input the value to check
     * @return {@code true} on failure
     * @see #isInvalid(String)
     * @see #rejects(String)
     * @see #test(String)
     * @since 1.0.0
     */
    public boolean fails(String input) { return !test(input); }

    /**
     * Alias of {@link #fails(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on failure
     * @see #fails(String)
     * @since 1.0.0
     */
    public boolean isInvalid(String input) { return !test(input); }

    /**
     * Alias of {@link #fails(String)} — see that method for full
     * documentation.
     *
     * @param input the value to check
     * @return {@code true} on failure
     * @see #fails(String)
     * @since 1.0.0
     */
    public boolean rejects(String input) { return !test(input); }

    // ============================================================== fluent composition

    /**
     * Returns a new rule requiring both this rule and the other to pass
     * (AND). Equivalent to {@code Rule.allOf(this, other)}.
     *
     * @param other the rule to require alongside this one (null → this rule unchanged)
     * @return the combined rule
     * @see #allOf(Rule...)
     * @see #or(Rule)
     * @see #andNot(Rule)
     * @since 1.0.0
     */
    public Rule and(Rule other) {
        if (other == null) return this;
        return allOf(this, other);
    }

    /**
     * Returns a new rule requiring this rule or the other to pass (OR).
     * Equivalent to {@code Rule.anyOf(this, other)}.
     *
     * @param other the alternate rule (null → this rule unchanged)
     * @return the combined rule
     * @see #anyOf(Rule...)
     * @see #and(Rule)
     * @since 1.0.0
     */
    public Rule or(Rule other) {
        if (other == null) return this;
        return anyOf(this, other);
    }

    /**
     * Returns a new rule requiring this rule to pass and the other to
     * fail (A AND NOT B). Useful for exclusion: "valid email but not on
     * this domain".
     *
     * @param other the rule to forbid (null → this rule unchanged)
     * @return the combined rule
     * @see #and(Rule)
     * @see #not(Rule)
     * @since 1.0.0
     */
    public Rule andNot(Rule other) {
        if (other == null) return this;
        return allOf(this, not(other));
    }

    // ============================================================== fluent mutators

    /**
     * Returns a copy of this rule that accepts blank input.
     *
     * <p>Every other property (name, description, message, normaliser,
     * checker) is preserved; only the {@code required} flag changes.</p>
     *
     * @return a copy that accepts blank input
     * @see #mayBeEmpty()
     * @see #nullable()
     * @see #required()
     * @since 1.0.0
     */
    public Rule optional() {
        return new Rule(name, description, message, false, normalizer, checker);
    }

    /**
     * Alias of {@link #optional()} — see that method for full
     * documentation.
     *
     * @return an optional copy
     * @see #optional()
     * @since 1.0.0
     */
    public Rule mayBeEmpty() { return optional(); }

    /**
     * Alias of {@link #optional()} — see that method for full
     * documentation.
     *
     * @return an optional copy
     * @see #optional()
     * @since 1.0.0
     */
    public Rule nullable() { return optional(); }

    /**
     * Returns a copy of this rule that requires non-blank input.
     *
     * <p>Every other property is preserved; only the {@code required}
     * flag changes. To check whether a rule is currently required, use
     * {@link #isRequired()} (the boolean accessor) — not this method.</p>
     *
     * @return a copy that requires non-blank input
     * @see #mandatory()
     * @see #optional()
     * @see #isRequired()
     * @since 1.0.0
     */
    public Rule required() {
        return new Rule(name, description, message, true, normalizer, checker);
    }

    /**
     * Alias of {@link #required()} — see that method for full
     * documentation.
     *
     * @return a required copy
     * @see #required()
     * @since 1.0.0
     */
    public Rule mandatory() { return required(); }

    /**
     * Returns a copy of this rule with a different failure message.
     *
     * @param newMessage the replacement message (null → keeps current)
     * @return a copy with the new message
     * @see #message(String)
     * @since 1.0.0
     */
    public Rule withMessage(String newMessage) {
        String effective = newMessage == null ? message : newMessage;
        return new Rule(name, description, effective, required, normalizer,
                value -> {
                    Optional<String> failure = checker.apply(value);
                    return failure.map(ignored -> effective);
                });
    }

    /**
     * Alias of {@link #withMessage(String)} — see that method for full
     * documentation.
     *
     * @param newMessage the replacement message
     * @return a copy with the new message
     * @see #withMessage(String)
     * @since 1.0.0
     */
    public Rule message(String newMessage) { return withMessage(newMessage); }

    /**
     * Returns a copy of this rule with an additional normaliser step.
     * The additional normaliser is applied <b>after</b> the existing one.
     *
     * @param extra the extra normaliser (null → this rule unchanged)
     * @return a copy with the combined normaliser
     * @since 1.0.0
     */
    public Rule withNormalizer(UnaryOperator<String> extra) {
        if (extra == null) return this;
        UnaryOperator<String> combined = v -> v == null ? null : extra.apply(normalizer.apply(v));
        return new Rule(name, description, message, required, combined, checker);
    }

    // ============================================================== display

    /**
     * Returns a short representation — the rule name and required flag.
     *
     * @return the representation
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "Rule[" + name + (required ? ", required" : ", optional") + "]";
    }
}
