package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;
import chirp.text.Check;
import chirp.text.Regex;
import chirp.validation.Rule;
import chirp.validation.Rules;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent string reader with built-in validators and rule-engine support.
 *
 * <p>Uses {@link Input#readRaw(String)} internally so the reader's own
 * category tracking is the only source of strict warnings — the reason a
 * fully-validated reader produces zero warnings even though the lower-level
 * {@code Input.readLine} would emit them.</p>
 */
public final class StringReader {

    private String label = "Enter a value";
    private Integer minLength;
    private Integer maxLength;
    private String regex;
    private String regexError;
    private Set<String> allowlist;
    private String allowlistError;
    private String defaultValue;
    private String helpText;
    private Predicate<String> customValidator;
    private String customError;
    private boolean asEmail;
    private boolean asPhone;
    private boolean asURL;
    private Rule rule;

    private boolean lenient = false;
    private boolean warnedOnce = false;
    private final Set<ValidationCategory> applied = EnumSet.noneOf(ValidationCategory.class);
    private final Set<ValidationCategory> skipped = EnumSet.noneOf(ValidationCategory.class);

    private StringReader() {}
    private StringReader(String label) { if (label != null) this.label = label; }

    public static StringReader create() { return new StringReader(); }
    public static StringReader of() { return new StringReader(); }
    public static StringReader ask() { return new StringReader(); }
    public static StringReader prompt() { return new StringReader(); }
    public static StringReader prompt(String label) { return new StringReader(label); }

    public StringReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public StringReader message(String label) { return label(label); }
    public StringReader question(String label) { return label(label); }
    public StringReader withLabel(String label) { return label(label); }

    // -------------------------------------------------------- validators
    public StringReader notBlank() {
        applied.add(ValidationCategory.BLANK);
        this.minLength = max(minLength, 1);
        Trace.debug("reader", "configure: notBlank");
        return this;
    }
    public StringReader required() { return notBlank(); }
    public StringReader mandatory() { return notBlank(); }
    public StringReader nonEmpty() { return notBlank(); }

    public StringReader minLength(int value) {
        this.minLength = value;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: minLength({})", value);
        return this;
    }
    public StringReader atLeast(int value) { return minLength(value); }

    public StringReader maxLength(int value) {
        this.maxLength = value;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: maxLength({})", value);
        return this;
    }
    public StringReader atMost(int value) { return maxLength(value); }

    public StringReader lengthBetween(int lo, int hi) {
        this.minLength = lo;
        this.maxLength = hi;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: lengthBetween({}, {})", lo, hi);
        return this;
    }

    public StringReader matches(String regex, String errorMessage) {
        this.regex = regex;
        this.regexError = errorMessage;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: matches({})", regex);
        return this;
    }
    public StringReader regex(String regex) { return matches(regex, "Invalid format."); }

    public StringReader oneOf(String... options) {
        this.allowlist = Set.of(options);
        this.allowlistError = "Value must be one of: " + String.join(", ", options);
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: oneOf({} options)", options == null ? 0 : options.length);
        return this;
    }
    public StringReader allowlist(String... options) { return oneOf(options); }

    public StringReader email() {
        this.asEmail = true;
        this.regex = Regex.email();
        this.regexError = "That doesn't look like an email address.";
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: email");
        return this;
    }
    public StringReader phone() {
        this.asPhone = true;
        this.regex = Regex.phone();
        this.regexError = "That doesn't look like a phone number.";
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: phone");
        return this;
    }
    public StringReader url() {
        this.asURL = true;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: url");
        return this;
    }

    // ------------------------------------------------------------ rule engine
    public StringReader rule(String ruleName) {
        Rule resolved = Rules.get(ruleName);
        if (resolved == null) {
            Trace.warn("reader", "unknown rule: {}", ruleName);
            throw new IllegalArgumentException("No rule named '" + ruleName + "'");
        }
        this.rule = resolved;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: rule({})", ruleName);
        return this;
    }

    public StringReader rule(Rule rule) {
        if (rule == null) return this;
        this.rule = rule;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: rule({})", rule.name());
        return this;
    }

    public StringReader custom(Predicate<String> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    // ------------------------------------------------------------ defaults
    public StringReader defaultValue(String value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public StringReader defaulting(String value) { return defaultValue(value); }
    public StringReader orElse(String value) { return defaultValue(value); }

    public StringReader help(String text) { this.helpText = text; return this; }
    public StringReader hint(String text) { return help(text); }

    // -------------------------------------------------- strict-mode controls
    public StringReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }
    public StringReader noStrict() { return lenient(); }
    public StringReader skipBlankWarning() {
        skipped.add(ValidationCategory.BLANK);
        Trace.debug("reader", "configure: skipBlankWarning");
        return this;
    }
    public StringReader skipLengthWarning() {
        skipped.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: skipLengthWarning");
        return this;
    }
    public StringReader skipFormatWarning() {
        skipped.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: skipFormatWarning");
        return this;
    }

    // ------------------------------------------------------------ terminal
    public String read() {
        Trace.enter("reader", "StringReader.read", "label=" + label,
                "minLength=" + minLength, "maxLength=" + maxLength);
        warnIfStrict();
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "StringReader.read", defaultValue);
                return defaultValue;
            }

            if (applied.contains(ValidationCategory.BLANK) && raw.isBlank()) {
                Trace.validation("reader", "BLANK", false, "empty input");
                Messenger.error("This field cannot be empty.");
                continue;
            }
            if (minLength != null && raw.length() < minLength) {
                Trace.validation("reader", "LENGTH min(" + minLength + ")", false, "length=" + raw.length());
                Messenger.error("Must be at least " + minLength + " characters.");
                continue;
            }
            if (maxLength != null && raw.length() > maxLength) {
                Trace.validation("reader", "LENGTH max(" + maxLength + ")", false, "length=" + raw.length());
                Messenger.error("Must be at most " + maxLength + " characters.");
                continue;
            }
            if (allowlist != null && !allowlist.contains(raw)) {
                Trace.validation("reader", "allowlist", false, "value not in " + allowlist);
                Messenger.error(allowlistError);
                continue;
            }
            if (asEmail && !Check.isEmail(raw)) {
                Trace.validation("reader", "email format", false, raw);
                Messenger.error("That doesn't look like an email address.");
                continue;
            }
            if (asPhone && !Check.isPhone(raw)) {
                Trace.validation("reader", "phone format", false, raw);
                Messenger.error("That doesn't look like a phone number.");
                continue;
            }
            if (asURL && !Check.isURL(raw)) {
                Trace.validation("reader", "url format", false, raw);
                Messenger.error("That doesn't look like a URL.");
                continue;
            }
            if (regex != null && !raw.matches(regex)) {
                Trace.validation("reader", "regex match", false, "pattern=" + regex);
                Messenger.error(regexError != null ? regexError : "Invalid format.");
                continue;
            }
            if (rule != null) {
                Optional<String> failure = rule.firstFailure(raw);
                boolean passed = failure.isEmpty();
                Trace.validation("reader", "rule(" + rule.name() + ")", passed, passed ? null : failure.get());
                if (!passed) {
                    Messenger.error(failure.get());
                    continue;
                }
            }
            if (customValidator != null && !customValidator.test(raw)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid value.");
                continue;
            }
            Trace.validation("reader", "all checks", true, null);
            Trace.exit("reader", "StringReader.read", raw);
            return raw;
        }
    }

    private void warnIfStrict() {
        if (warnedOnce) return;
        warnedOnce = true;
        if (lenient) { Trace.debug("reader", "strict skipped (lenient)"); return; }
        warnIfMissing(ValidationCategory.BLANK);
        warnIfMissing(ValidationCategory.LENGTH);
        warnIfMissing(ValidationCategory.FORMAT);
    }

    private void warnIfMissing(ValidationCategory category) {
        if (applied.contains(category) || skipped.contains(category)) {
            Trace.decision("reader", category + " covered", true);
            return;
        }
        Trace.decision("reader", category + " covered", false);
        Strict.emit("string", label, category);
    }

    private static int max(Integer a, int b) { return a == null ? b : Math.max(a, b); }

    public String get() { return read(); }
    public String value() { return read(); }
    public String parse() { return read(); }
}
