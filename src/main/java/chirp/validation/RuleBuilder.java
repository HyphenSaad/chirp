package chirp.validation;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Fluent builder for {@link Rule}.
 *
 * <p>Every setter returns {@code this}, so calls chain naturally. The
 * terminal {@link #build()} creates an immutable {@link Rule} with all
 * the accumulated configuration.</p>
 *
 * <h2>What the builder can configure</h2>
 * <ul>
 *   <li><b>Metadata</b> — description, message, required flag</li>
 *   <li><b>Normalisers</b> — trim, strip spaces, strip non-digits,
 *       lowercase, uppercase, custom</li>
 *   <li><b>Predicates</b> — matches(regex), startsWith, endsWith,
 *       contains, minLength, maxLength, lengthBetween, oneOf,
 *       satisfies(custom)</li>
 * </ul>
 * <p>Every setter accepts the natural verb plus 2–4 aliases.</p>
 *
 * <h2>Multiple predicates = AND</h2>
 * <p>Every call to a predicate method ({@code matches}, {@code minLength},
 * etc.) <b>adds</b> a check. The value must satisfy all of them for the
 * rule to pass. This matches the intuition of "the value must be at
 * least 3 characters AND at most 20 AND start with a letter".</p>
 *
 * <pre>{@code
 * Rule username = Rule.define("USERNAME")
 *     .trim()
 *     .minLength(3)
 *     .maxLength(20)
 *     .matches("^[a-zA-Z][a-zA-Z0-9_]*$")
 *     .message("3-20 characters, letters/digits/underscore, must start with a letter")
 *     .build();
 * }</pre>
 *
 * <h2>Key-value application</h2>
 * <p>{@link #set(Rule.Key, Object)} allows every builder method to be
 * driven from {@link Rule#from(Object...)}. Missing keys and wrong
 * value types are silently skipped — this makes {@code from(...)}
 * resilient to partial input.</p>
 *
 * <h2>Example — building a numeric rule</h2>
 * <pre>{@code
 * Rule score = Rule.define("SCORE")
 *     .description("Student score")
 *     .trim()
 *     .matches("\\d+")
 *     .satisfies(s -> {
 *         int n = Integer.parseInt(s);
 *         return n >= 0 && n <= 100;
 *     })
 *     .message("Score must be 0–100")
 *     .build();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the built rule, see {@link Rule}. For factory shortcuts that
 * bypass the builder, see {@link Rule#simple(String, String, java.util.function.Function, UnaryOperator, boolean)}
 * and {@link Rule#from(Object...)}.</p>
 *
 * @see Rule
 * @see Rules
 * @since 1.0.0
 */
public final class RuleBuilder {

    private final String name;
    private String description;
    private String message = "Invalid value.";
    private boolean required = true;
    private UnaryOperator<String> normalizer = UnaryOperator.identity();
    private final List<Predicate<String>> predicates = new ArrayList<>();

    RuleBuilder(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rule name must not be blank");
        }
        this.name = name.trim().toUpperCase(Locale.ROOT);
    }

    // ============================================================== meta

    /**
     * Sets a human-readable description for the rule.
     *
     * @param value the description (may be null)
     * @return this builder
     * @see #describe(String)
     * @since 1.0.0
     */
    public RuleBuilder description(String value) { this.description = value; return this; }

    /**
     * Alias of {@link #description(String)} — see that method for full
     * documentation.
     *
     * @param value the description
     * @return this builder
     * @see #description(String)
     * @since 1.0.0
     */
    public RuleBuilder describe(String value) { return description(value); }

    /**
     * Sets the failure message shown when the rule fails.
     *
     * @param value the message (null → keeps current default "Invalid value.")
     * @return this builder
     * @see #errMsg(String)
     * @see #errorMessage(String)
     * @see #failMessage(String)
     * @since 1.0.0
     */
    public RuleBuilder message(String value) { if (value != null) this.message = value; return this; }

    /**
     * Alias of {@link #message(String)} — see that method for full
     * documentation.
     *
     * @param value the failure message
     * @return this builder
     * @see #message(String)
     * @since 1.0.0
     */
    public RuleBuilder errMsg(String value) { return message(value); }

    /**
     * Alias of {@link #message(String)} — see that method for full
     * documentation.
     *
     * @param value the failure message
     * @return this builder
     * @see #message(String)
     * @since 1.0.0
     */
    public RuleBuilder errorMessage(String value) { return message(value); }

    /**
     * Alias of {@link #message(String)} — see that method for full
     * documentation.
     *
     * @param value the failure message
     * @return this builder
     * @see #message(String)
     * @since 1.0.0
     */
    public RuleBuilder failMessage(String value) { return message(value); }

    /**
     * Marks the rule as required — blank input fails it. This is the
     * default; calling this method makes the intent explicit.
     *
     * @return this builder
     * @see #mandatory()
     * @see #mustHave()
     * @see #optional()
     * @since 1.0.0
     */
    public RuleBuilder required() { this.required = true; return this; }

    /**
     * Alias of {@link #required()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #required()
     * @since 1.0.0
     */
    public RuleBuilder mandatory() { return required(); }

    /**
     * Alias of {@link #required()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #required()
     * @since 1.0.0
     */
    public RuleBuilder mustHave() { return required(); }

    /**
     * Marks the rule as optional — blank input passes it.
     *
     * @return this builder
     * @see #mayBeEmpty()
     * @see #nullable()
     * @see #required()
     * @since 1.0.0
     */
    public RuleBuilder optional() { this.required = false; return this; }

    /**
     * Alias of {@link #optional()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #optional()
     * @since 1.0.0
     */
    public RuleBuilder mayBeEmpty() { return optional(); }

    /**
     * Alias of {@link #optional()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #optional()
     * @since 1.0.0
     */
    public RuleBuilder nullable() { return optional(); }

    // ============================================================== normalizers

    /**
     * Adds a custom normaliser. Multiple calls compose — the last
     * normaliser runs after the previous one.
     *
     * @param normalizer the transform (null → no-op)
     * @return this builder
     * @see #trim()
     * @see #stripSpaces()
     * @see #stripNonDigits()
     * @see #lowerCase()
     * @see #upperCase()
     * @since 1.0.0
     */
    public RuleBuilder normalize(UnaryOperator<String> normalizer) {
        if (normalizer != null) this.normalizer = compose(this.normalizer, normalizer);
        return this;
    }

    /**
     * Trims leading and trailing whitespace before validation.
     *
     * @return this builder
     * @see #trimmed()
     * @see #normalize(UnaryOperator)
     * @since 1.0.0
     */
    public RuleBuilder trim() { return normalize(String::trim); }

    /**
     * Alias of {@link #trim()} — see that method for full documentation.
     *
     * @return this builder
     * @see #trim()
     * @since 1.0.0
     */
    public RuleBuilder trimmed() { return trim(); }

    /**
     * Removes all whitespace (including internal) before validation.
     *
     * @return this builder
     * @see #removeSpaces()
     * @see #noSpaces()
     * @since 1.0.0
     */
    public RuleBuilder stripSpaces() { return normalize(s -> s.replaceAll("\\s+", "")); }

    /**
     * Alias of {@link #stripSpaces()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #stripSpaces()
     * @since 1.0.0
     */
    public RuleBuilder removeSpaces() { return stripSpaces(); }

    /**
     * Alias of {@link #stripSpaces()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #stripSpaces()
     * @since 1.0.0
     */
    public RuleBuilder noSpaces() { return stripSpaces(); }

    /**
     * Removes all non-digit characters before validation. Useful for
     * phone numbers and CNICs that arrive with dashes or spaces.
     *
     * @return this builder
     * @see #digitsOnly()
     * @since 1.0.0
     */
    public RuleBuilder stripNonDigits() { return normalize(s -> s.replaceAll("\\D", "")); }

    /**
     * Alias of {@link #stripNonDigits()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #stripNonDigits()
     * @since 1.0.0
     */
    public RuleBuilder digitsOnly() { return stripNonDigits(); }

    /**
     * Removes all characters matching the given regex before validation.
     *
     * @param regex the regex to strip (null → throws)
     * @return this builder
     * @throws NullPointerException if {@code regex} is null
     * @see #strip(String)
     * @since 1.0.0
     */
    public RuleBuilder stripAll(String regex) {
        Objects.requireNonNull(regex, "regex");
        return normalize(s -> s.replaceAll(regex, ""));
    }

    /**
     * Alias of {@link #stripAll(String)} — see that method for full
     * documentation.
     *
     * @param regex the regex to strip
     * @return this builder
     * @see #stripAll(String)
     * @since 1.0.0
     */
    public RuleBuilder strip(String regex) { return stripAll(regex); }

    /**
     * Converts the value to lowercase before validation.
     *
     * @return this builder
     * @see #toLower()
     * @since 1.0.0
     */
    public RuleBuilder lowerCase() { return normalize(s -> s.toLowerCase(Locale.ROOT)); }

    /**
     * Alias of {@link #lowerCase()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #lowerCase()
     * @since 1.0.0
     */
    public RuleBuilder toLower() { return lowerCase(); }

    /**
     * Converts the value to uppercase before validation.
     *
     * @return this builder
     * @see #toUpper()
     * @since 1.0.0
     */
    public RuleBuilder upperCase() { return normalize(s -> s.toUpperCase(Locale.ROOT)); }

    /**
     * Alias of {@link #upperCase()} — see that method for full
     * documentation.
     *
     * @return this builder
     * @see #upperCase()
     * @since 1.0.0
     */
    public RuleBuilder toUpper() { return upperCase(); }

    // ============================================================== predicates

    /**
     * Requires the normalised value to fully match the given regex.
     *
     * @param regex the pattern (null → throws)
     * @return this builder
     * @throws NullPointerException if {@code regex} is null
     * @see #pattern(String)
     * @see #matchesRegex(String)
     * @see #regex(String)
     * @since 1.0.0
     */
    public RuleBuilder matches(String regex) {
        Objects.requireNonNull(regex, "regex");
        Pattern pattern = Pattern.compile(regex);
        predicates.add(value -> pattern.matcher(value).matches());
        return this;
    }

    /**
     * Alias of {@link #matches(String)} — see that method for full
     * documentation.
     *
     * @param regex the pattern
     * @return this builder
     * @see #matches(String)
     * @since 1.0.0
     */
    public RuleBuilder pattern(String regex) { return matches(regex); }

    /**
     * Alias of {@link #matches(String)} — see that method for full
     * documentation.
     *
     * @param regex the pattern
     * @return this builder
     * @see #matches(String)
     * @since 1.0.0
     */
    public RuleBuilder matchesRegex(String regex) { return matches(regex); }

    /**
     * Alias of {@link #matches(String)} — see that method for full
     * documentation.
     *
     * @param regex the pattern
     * @return this builder
     * @see #matches(String)
     * @since 1.0.0
     */
    public RuleBuilder regex(String regex) { return matches(regex); }

    /**
     * Requires the normalised value to start with the given prefix.
     *
     * @param prefix the required prefix (null → throws)
     * @return this builder
     * @see #beginsWith(String)
     * @see #prefix(String)
     * @since 1.0.0
     */
    public RuleBuilder startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        predicates.add(value -> value.startsWith(prefix));
        return this;
    }

    /**
     * Alias of {@link #startsWith(String)} — see that method for full
     * documentation.
     *
     * @param prefix the required prefix
     * @return this builder
     * @see #startsWith(String)
     * @since 1.0.0
     */
    public RuleBuilder beginsWith(String prefix) { return startsWith(prefix); }

    /**
     * Alias of {@link #startsWith(String)} — see that method for full
     * documentation.
     *
     * @param prefix the required prefix
     * @return this builder
     * @see #startsWith(String)
     * @since 1.0.0
     */
    public RuleBuilder prefix(String prefix) { return startsWith(prefix); }

    /**
     * Requires the normalised value to end with the given suffix.
     *
     * @param suffix the required suffix (null → throws)
     * @return this builder
     * @see #suffix(String)
     * @since 1.0.0
     */
    public RuleBuilder endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix");
        predicates.add(value -> value.endsWith(suffix));
        return this;
    }

    /**
     * Alias of {@link #endsWith(String)} — see that method for full
     * documentation.
     *
     * @param suffix the required suffix
     * @return this builder
     * @see #endsWith(String)
     * @since 1.0.0
     */
    public RuleBuilder suffix(String suffix) { return endsWith(suffix); }

    /**
     * Requires the normalised value to contain the given substring.
     *
     * @param fragment the required substring (null → throws)
     * @return this builder
     * @see #includes(String)
     * @since 1.0.0
     */
    public RuleBuilder contains(String fragment) {
        Objects.requireNonNull(fragment, "fragment");
        predicates.add(value -> value.contains(fragment));
        return this;
    }

    /**
     * Alias of {@link #contains(String)} — see that method for full
     * documentation.
     *
     * @param fragment the required substring
     * @return this builder
     * @see #contains(String)
     * @since 1.0.0
     */
    public RuleBuilder includes(String fragment) { return contains(fragment); }

    /**
     * Requires the normalised value to have at least {@code length}
     * characters.
     *
     * @param length the minimum length
     * @return this builder
     * @see #atLeast(int)
     * @see #minLen(int)
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public RuleBuilder minLength(int length) { predicates.add(v -> v.length() >= length); return this; }

    /**
     * Alias of {@link #minLength(int)} — see that method for full
     * documentation.
     *
     * @param length the minimum length
     * @return this builder
     * @see #minLength(int)
     * @since 1.0.0
     */
    public RuleBuilder atLeast(int length) { return minLength(length); }

    /**
     * Alias of {@link #minLength(int)} — see that method for full
     * documentation.
     *
     * @param length the minimum length
     * @return this builder
     * @see #minLength(int)
     * @since 1.0.0
     */
    public RuleBuilder minLen(int length) { return minLength(length); }

    /**
     * Requires the normalised value to have at most {@code length}
     * characters.
     *
     * @param length the maximum length
     * @return this builder
     * @see #atMost(int)
     * @see #maxLen(int)
     * @see #minLength(int)
     * @since 1.0.0
     */
    public RuleBuilder maxLength(int length) { predicates.add(v -> v.length() <= length); return this; }

    /**
     * Alias of {@link #maxLength(int)} — see that method for full
     * documentation.
     *
     * @param length the maximum length
     * @return this builder
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public RuleBuilder atMost(int length) { return maxLength(length); }

    /**
     * Alias of {@link #maxLength(int)} — see that method for full
     * documentation.
     *
     * @param length the maximum length
     * @return this builder
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public RuleBuilder maxLen(int length) { return maxLength(length); }

    /**
     * Requires the normalised value's length to be in
     * {@code [min, max]} (inclusive).
     *
     * @param min the minimum length
     * @param max the maximum length
     * @return this builder
     * @see #lengthInRange(int, int)
     * @see #minLength(int)
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public RuleBuilder lengthBetween(int min, int max) {
        predicates.add(v -> v.length() >= min && v.length() <= max);
        return this;
    }

    /**
     * Alias of {@link #lengthBetween(int, int)} — see that method for
     * full documentation.
     *
     * @param min the minimum length
     * @param max the maximum length
     * @return this builder
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public RuleBuilder lengthInRange(int min, int max) { return lengthBetween(min, max); }

    /**
     * Requires the normalised value to equal one of the given options
     * (case-sensitive).
     *
     * @param options the allowed values (null → throws)
     * @return this builder
     * @see #in(String...)
     * @see #oneOfIgnoreCase(String...)
     * @since 1.0.0
     */
    public RuleBuilder oneOf(String... options) {
        Objects.requireNonNull(options, "options");
        List<String> set = Arrays.asList(options);
        predicates.add(set::contains);
        return this;
    }

    /**
     * Alias of {@link #oneOf(String...)} — see that method for full
     * documentation.
     *
     * @param options the allowed values
     * @return this builder
     * @see #oneOf(String...)
     * @since 1.0.0
     */
    public RuleBuilder in(String... options) { return oneOf(options); }

    /**
     * Requires the normalised value to equal one of the given options,
     * ignoring case.
     *
     * @param options the allowed values (null → throws)
     * @return this builder
     * @see #oneOf(String...)
     * @since 1.0.0
     */
    public RuleBuilder oneOfIgnoreCase(String... options) {
        Objects.requireNonNull(options, "options");
        List<String> set = Arrays.stream(options)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
        predicates.add(v -> set.contains(v.toLowerCase(Locale.ROOT)));
        return this;
    }

    /**
     * Requires the normalised value to satisfy an arbitrary predicate.
     *
     * @param predicate the check (null → throws)
     * @return this builder
     * @throws NullPointerException if {@code predicate} is null
     * @see #custom(Predicate)
     * @see #with(Predicate)
     * @since 1.0.0
     */
    public RuleBuilder satisfies(Predicate<String> predicate) {
        predicates.add(Objects.requireNonNull(predicate, "predicate"));
        return this;
    }

    /**
     * Alias of {@link #satisfies(Predicate)} — see that method for full
     * documentation.
     *
     * @param predicate the check
     * @return this builder
     * @see #satisfies(Predicate)
     * @since 1.0.0
     */
    public RuleBuilder custom(Predicate<String> predicate) { return satisfies(predicate); }

    /**
     * Alias of {@link #satisfies(Predicate)} — see that method for full
     * documentation.
     *
     * @param predicate the check
     * @return this builder
     * @see #satisfies(Predicate)
     * @since 1.0.0
     */
    public RuleBuilder with(Predicate<String> predicate) { return satisfies(predicate); }

    // ============================================================== key-value

    /**
     * Applies a key-value pair. Missing keys and wrong value types are
     * silently skipped — this makes {@link Rule#from(Object...)}
     * resilient to partial or malformed input.
     *
     * @param key the key (null → no-op)
     * @param value the value (null → no-op)
     * @return this builder
     * @see Rule#from(Object...)
     * @see Rule.Key
     * @since 1.0.0
     */
    public RuleBuilder set(Rule.Key key, Object value) {
        if (key == null || value == null) return this;
        switch (key) {
            case DESCRIPTION -> {
                if (value instanceof String s) description(s);
            }
            case MESSAGE -> {
                if (value instanceof String s) message(s);
            }
            case REQUIRED -> {
                if (value instanceof Boolean b) {
                    if (b) required();
                    else optional();
                }
            }
            case PATTERN -> {
                if (value instanceof String s) matches(s);
            }
            case STARTS_WITH -> {
                if (value instanceof String s) startsWith(s);
            }
            case ENDS_WITH -> {
                if (value instanceof String s) endsWith(s);
            }
            case CONTAINS -> {
                if (value instanceof String s) contains(s);
            }
            case MIN_LENGTH -> {
                if (value instanceof Integer n) minLength(n);
            }
            case MAX_LENGTH -> {
                if (value instanceof Integer n) maxLength(n);
            }
            case ONE_OF -> {
                if (value instanceof String[] arr) oneOf(arr);
            }
            case TRIM -> {
                if (Boolean.TRUE.equals(value)) trim();
            }
            case STRIP_SPACES -> {
                if (Boolean.TRUE.equals(value)) stripSpaces();
            }
            case STRIP_NON_DIGITS -> {
                if (Boolean.TRUE.equals(value)) stripNonDigits();
            }
            case LOWER_CASE -> {
                if (Boolean.TRUE.equals(value)) lowerCase();
            }
            case UPPER_CASE -> {
                if (Boolean.TRUE.equals(value)) upperCase();
            }
            default -> {
                /* NAME is consumed by Rule.from; ignore here */ }
        }
        return this;
    }

    // ============================================================== build

    /**
     * Produces the immutable {@link Rule} from the accumulated
     * configuration.
     *
     * @return a new {@code Rule}
     * @throws IllegalStateException if no predicate was added — a rule
     * with no checks would accept everything, which is almost always a
     * mistake
     * @see #create()
     * @see #done()
     * @since 1.0.0
     */
    public Rule build() {
        if (predicates.isEmpty()) {
            Trace.warn("rule", "build failed: rule '{}' has no predicates", name);
            throw new IllegalStateException("Rule '" + name + "' has no predicates");
        }
        List<Predicate<String>> checks = List.copyOf(predicates);
        Trace.debug("rule", "build({}) with {} predicates", name, checks.size());
        return new Rule(name, description, message, required, normalizer, value -> {
            for (Predicate<String> check : checks) {
                if (!check.test(value)) return java.util.Optional.of(message);
            }
            return java.util.Optional.empty();
        });
    }

    /**
     * Alias of {@link #build()} — see that method for full documentation.
     *
     * @return a new {@code Rule}
     * @see #build()
     * @since 1.0.0
     */
    public Rule create() { return build(); }

    /**
     * Alias of {@link #build()} — see that method for full documentation.
     *
     * @return a new {@code Rule}
     * @see #build()
     * @since 1.0.0
     */
    public Rule done() { return build(); }

    private static UnaryOperator<String> compose(UnaryOperator<String> first,
            UnaryOperator<String> second) {
        return value -> value == null ? null : second.apply(first.apply(value));
    }
}
