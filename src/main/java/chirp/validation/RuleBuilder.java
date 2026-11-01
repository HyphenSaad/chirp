package chirp.validation;

import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Fluent builder for {@link Rule}.
 *
 * <h2>Naming</h2>
 * <p>Every setter has 2–4 aliases covering common English synonyms:</p>
 * <ul>
 *   <li>{@code message} / {@code errMsg} / {@code errorMessage} / {@code failMessage}</li>
 *   <li>{@code matches} / {@code pattern} / {@code matchesRegex} / {@code regex}</li>
 *   <li>{@code startsWith} / {@code beginsWith} / {@code prefix}</li>
 *   <li>{@code minLength} / {@code atLeast} / {@code minLen}</li>
 * </ul>
 *
 * <h2>Key-value</h2>
 * <p>{@link #set(Rule.Key, Object)} lets every builder method be driven from
 * {@link Rule#from(Object...)}. Missing or wrongly-typed values are silently
 * skipped.</p>
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
        this.name = name.trim().toUpperCase();
    }

    // ============================================================== meta

    public RuleBuilder description(String value) { this.description = value; return this; }
    public RuleBuilder describe(String value) { return description(value); }

    public RuleBuilder message(String value) { if (value != null) this.message = value; return this; }
    public RuleBuilder errMsg(String value) { return message(value); }
    public RuleBuilder errorMessage(String value) { return message(value); }
    public RuleBuilder failMessage(String value) { return message(value); }

    public RuleBuilder required() { this.required = true; return this; }
    public RuleBuilder mandatory() { return required(); }
    public RuleBuilder mustHave() { return required(); }

    public RuleBuilder optional() { this.required = false; return this; }
    public RuleBuilder mayBeEmpty() { return optional(); }
    public RuleBuilder nullable() { return optional(); }

    // ============================================================== normalizers

    public RuleBuilder normalize(UnaryOperator<String> normalizer) {
        if (normalizer != null) this.normalizer = compose(this.normalizer, normalizer);
        return this;
    }

    public RuleBuilder trim() { return normalize(String::trim); }
    public RuleBuilder trimmed() { return trim(); }

    public RuleBuilder stripSpaces() { return normalize(s -> s.replaceAll("\\s+", "")); }
    public RuleBuilder removeSpaces() { return stripSpaces(); }
    public RuleBuilder noSpaces() { return stripSpaces(); }

    public RuleBuilder stripNonDigits() { return normalize(s -> s.replaceAll("\\D", "")); }
    public RuleBuilder digitsOnly() { return stripNonDigits(); }

    public RuleBuilder stripAll(String regex) {
        Objects.requireNonNull(regex, "regex");
        return normalize(s -> s.replaceAll(regex, ""));
    }
    public RuleBuilder strip(String regex) { return stripAll(regex); }

    public RuleBuilder lowerCase() { return normalize(String::toLowerCase); }
    public RuleBuilder toLower() { return lowerCase(); }

    public RuleBuilder upperCase() { return normalize(String::toUpperCase); }
    public RuleBuilder toUpper() { return upperCase(); }

    // ============================================================== predicates

    public RuleBuilder matches(String regex) {
        Objects.requireNonNull(regex, "regex");
        Pattern pattern = Pattern.compile(regex);
        predicates.add(value -> pattern.matcher(value).matches());
        return this;
    }
    public RuleBuilder pattern(String regex) { return matches(regex); }
    public RuleBuilder matchesRegex(String regex) { return matches(regex); }
    public RuleBuilder regex(String regex) { return matches(regex); }

    public RuleBuilder startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        predicates.add(value -> value.startsWith(prefix));
        return this;
    }
    public RuleBuilder beginsWith(String prefix) { return startsWith(prefix); }
    public RuleBuilder prefix(String prefix) { return startsWith(prefix); }

    public RuleBuilder endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix");
        predicates.add(value -> value.endsWith(suffix));
        return this;
    }
    public RuleBuilder suffix(String suffix) { return endsWith(suffix); }

    public RuleBuilder contains(String fragment) {
        Objects.requireNonNull(fragment, "fragment");
        predicates.add(value -> value.contains(fragment));
        return this;
    }
    public RuleBuilder includes(String fragment) { return contains(fragment); }

    public RuleBuilder minLength(int length) { predicates.add(v -> v.length() >= length); return this; }
    public RuleBuilder atLeast(int length) { return minLength(length); }
    public RuleBuilder minLen(int length) { return minLength(length); }

    public RuleBuilder maxLength(int length) { predicates.add(v -> v.length() <= length); return this; }
    public RuleBuilder atMost(int length) { return maxLength(length); }
    public RuleBuilder maxLen(int length) { return maxLength(length); }

    public RuleBuilder lengthBetween(int min, int max) {
        predicates.add(v -> v.length() >= min && v.length() <= max);
        return this;
    }
    public RuleBuilder lengthInRange(int min, int max) { return lengthBetween(min, max); }

    /** The value must equal one of the given options. Case-sensitive. */
    public RuleBuilder oneOf(String... options) {
        Objects.requireNonNull(options, "options");
        List<String> set = Arrays.asList(options);
        predicates.add(set::contains);
        return this;
    }
    public RuleBuilder in(String... options) { return oneOf(options); }
    public RuleBuilder oneOfIgnoreCase(String... options) {
        Objects.requireNonNull(options, "options");
        List<String> set = Arrays.stream(options).map(String::toLowerCase).toList();
        predicates.add(v -> set.contains(v.toLowerCase()));
        return this;
    }

    public RuleBuilder satisfies(Predicate<String> predicate) {
        predicates.add(Objects.requireNonNull(predicate, "predicate"));
        return this;
    }
    public RuleBuilder custom(Predicate<String> predicate) { return satisfies(predicate); }
    public RuleBuilder with(Predicate<String> predicate) { return satisfies(predicate); }

    // ============================================================== key-value

    /**
     * Applies the given key-value to the builder. Missing keys and wrong
     * value types are silently skipped.
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
    public Rule create() { return build(); }
    public Rule done() { return build(); }

    private static UnaryOperator<String> compose(UnaryOperator<String> first,
            UnaryOperator<String> second) {
        return value -> value == null ? null : second.apply(first.apply(value));
    }
}
