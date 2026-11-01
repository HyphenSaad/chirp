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
 * Fluent string reader — the most feature-rich reader, with length
 * checks, format validators, allow-lists, and rule-engine integration.
 *
 * <p>Created via {@link Input#readLine()}. For the immediate form, use
 * {@link Input#readLine(String)}.</p>
 *
 * <h2>What the fluent mode adds</h2>
 * <ul>
 *   <li><b>Blank check</b> — {@link #notBlank()} / {@link #required()} /
 *       {@link #mandatory()} / {@link #nonEmpty()}</li>
 *   <li><b>Length</b> — {@link #minLength(int)}, {@link #maxLength(int)},
 *       {@link #lengthBetween(int, int)}</li>
 *   <li><b>Format</b> — {@link #email()}, {@link #phone()}, {@link #url()},
 *       {@link #matches(String, String)}, {@link #regex(String)},
 *       {@link #oneOf(String...)}</li>
 *   <li><b>Rule engine</b> — {@link #rule(String)}, {@link #rule(Rule)}</li>
 *   <li><b>Custom</b> — {@link #custom(Predicate, String)}</li>
 *   <li><b>Default</b> — {@link #defaultValue(String)}</li>
 *   <li><b>Help</b> — {@link #help(String)}</li>
 *   <li><b>Strict mode</b> — the three categories BLANK, LENGTH, FORMAT
 *       can each be covered or skipped independently</li>
 * </ul>
 *
 * <h2>Strict mode — three categories, three warnings</h2>
 * <p>String readers are the only ones with <b>three</b> independent
 * strict-warning categories:</p>
 * <ul>
 *   <li><b>BLANK</b> — silence with {@link #notBlank()} or
 *       {@link #skipBlankWarning()}</li>
 *   <li><b>LENGTH</b> — silence with {@link #minLength(int)},
 *       {@link #maxLength(int)}, {@link #lengthBetween(int, int)}, or
 *       {@link #skipLengthWarning()}</li>
 *   <li><b>FORMAT</b> — silence with {@link #email()},
 *       {@link #phone()}, {@link #url()}, {@link #matches(String, String)},
 *       {@link #oneOf(String...)}, {@link #rule(String)}, or
 *       {@link #skipFormatWarning()}</li>
 * </ul>
 * <p>Missing all three produces all three warnings. Covering all three
 * produces none.</p>
 *
 * <h2>Example — a validated username field</h2>
 * <pre>{@code
 * String name = Input.readString()
 *     .label("Username")
 *     .notBlank()
 *     .minLength(3)
 *     .maxLength(20)
 *     .matches("[a-zA-Z0-9_]+", "Only letters, digits, and underscores")
 *     .read();
 * }</pre>
 *
 * <h2>Example — a domain-specific email field</h2>
 * <pre>{@code
 * String email = Input.readString()
 *     .label("Email")
 *     .email()
 *     .read();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the rule engine that {@link #rule(String)} consults, see
 * {@link Rule} and {@link Rules}. For the boolean checks that back the
 * validators ({@link #email()}, etc.), see {@link Check} and
 * {@link Regex}.</p>
 *
 * @see Input#readLine()
 * @see Input#readLine(String)
 * @see Rule
 * @see Rules
 * @see Check
 * @since 1.0.0
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

    // ============================================================== factories

    /**
     * Creates a string reader with the default label.
     *
     * @return a new reader
     * @see #of()
     * @see #ask()
     * @see #prompt()
     * @see #prompt(String)
     * @since 1.0.0
     */
    public static StringReader create() { return new StringReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static StringReader of() { return new StringReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static StringReader ask() { return new StringReader(); }

    /**
     * Alias of {@link #create()} — see that method for full documentation.
     *
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static StringReader prompt() { return new StringReader(); }

    /**
     * Creates a string reader with a custom label.
     *
     * @param label the prompt label
     * @return a new reader
     * @see #create()
     * @since 1.0.0
     */
    public static StringReader prompt(String label) { return new StringReader(label); }

    /**
     * Sets the prompt label.
     *
     * @param label the label (null → no-op)
     * @return this reader, for chaining
     * @see #message(String)
     * @see #question(String)
     * @see #withLabel(String)
     * @since 1.0.0
     */
    public StringReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public StringReader message(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public StringReader question(String label) { return label(label); }

    /**
     * Alias of {@link #label(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return this reader
     * @see #label(String)
     * @since 1.0.0
     */
    public StringReader withLabel(String label) { return label(label); }

    // -------------------------------------------------------- validators

    /**
     * Requires a non-blank value. Covers the BLANK strict category.
     *
     * @return this reader, for chaining
     * @see #required()
     * @see #mandatory()
     * @see #nonEmpty()
     * @since 1.0.0
     */
    public StringReader notBlank() {
        applied.add(ValidationCategory.BLANK);
        this.minLength = max(minLength, 1);
        Trace.debug("reader", "configure: notBlank");
        return this;
    }

    /**
     * Alias of {@link #notBlank()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #notBlank()
     * @since 1.0.0
     */
    public StringReader required() { return notBlank(); }

    /**
     * Alias of {@link #notBlank()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #notBlank()
     * @since 1.0.0
     */
    public StringReader mandatory() { return notBlank(); }

    /**
     * Alias of {@link #notBlank()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #notBlank()
     * @since 1.0.0
     */
    public StringReader nonEmpty() { return notBlank(); }

    /**
     * Sets the minimum length. Covers the LENGTH strict category.
     *
     * @param value the minimum length
     * @return this reader, for chaining
     * @see #atLeast(int)
     * @see #maxLength(int)
     * @see #lengthBetween(int, int)
     * @since 1.0.0
     */
    public StringReader minLength(int value) {
        this.minLength = value;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: minLength({})", value);
        return this;
    }

    /**
     * Alias of {@link #minLength(int)} — see that method for full
     * documentation.
     *
     * @param value the minimum length
     * @return this reader
     * @see #minLength(int)
     * @since 1.0.0
     */
    public StringReader atLeast(int value) { return minLength(value); }

    /**
     * Sets the maximum length. Covers the LENGTH strict category.
     *
     * @param value the maximum length
     * @return this reader, for chaining
     * @see #atMost(int)
     * @see #minLength(int)
     * @since 1.0.0
     */
    public StringReader maxLength(int value) {
        this.maxLength = value;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: maxLength({})", value);
        return this;
    }

    /**
     * Alias of {@link #maxLength(int)} — see that method for full
     * documentation.
     *
     * @param value the maximum length
     * @return this reader
     * @see #maxLength(int)
     * @since 1.0.0
     */
    public StringReader atMost(int value) { return maxLength(value); }

    /**
     * Sets both minimum and maximum length in one call. Covers the
     * LENGTH strict category.
     *
     * @param lo the minimum length
     * @param hi the maximum length
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public StringReader lengthBetween(int lo, int hi) {
        this.minLength = lo;
        this.maxLength = hi;
        applied.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: lengthBetween({}, {})", lo, hi);
        return this;
    }

    /**
     * Requires the input to match a regex. Covers the FORMAT strict
     * category.
     *
     * @param regex the pattern (must match the entire input)
     * @param errorMessage the error message shown on failure
     * @return this reader, for chaining
     * @see #regex(String)
     * @since 1.0.0
     */
    public StringReader matches(String regex, String errorMessage) {
        this.regex = regex;
        this.regexError = errorMessage;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: matches({})", regex);
        return this;
    }

    /**
     * Alias of {@link #matches(String, String)} with a generic error
     * message — see that method for full documentation.
     *
     * @param regex the pattern
     * @return this reader
     * @see #matches(String, String)
     * @since 1.0.0
     */
    public StringReader regex(String regex) { return matches(regex, "Invalid format."); }

    /**
     * Restricts the input to one of the given options. Covers the
     * FORMAT strict category.
     *
     * @param options the allowed values
     * @return this reader, for chaining
     * @see #allowlist(String...)
     * @since 1.0.0
     */
    public StringReader oneOf(String... options) {
        this.allowlist = Set.of(options);
        this.allowlistError = "Value must be one of: " + String.join(", ", options);
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: oneOf({} options)", options == null ? 0 : options.length);
        return this;
    }

    /**
     * Alias of {@link #oneOf(String...)} — see that method for full
     * documentation.
     *
     * @param options the allowed values
     * @return this reader
     * @see #oneOf(String...)
     * @since 1.0.0
     */
    public StringReader allowlist(String... options) { return oneOf(options); }

    /**
     * Requires a valid email address. Covers the FORMAT strict
     * category.
     *
     * @return this reader, for chaining
     * @see chirp.text.Check#isEmail(String)
     * @since 1.0.0
     */
    public StringReader email() {
        this.asEmail = true;
        this.regex = Regex.email();
        this.regexError = "That doesn't look like an email address.";
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: email");
        return this;
    }

    /**
     * Requires a valid phone number. Covers the FORMAT strict category.
     *
     * @return this reader, for chaining
     * @see chirp.text.Check#isPhone(String)
     * @since 1.0.0
     */
    public StringReader phone() {
        this.asPhone = true;
        this.regex = Regex.phone();
        this.regexError = "That doesn't look like a phone number.";
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: phone");
        return this;
    }

    /**
     * Requires a valid URL. Covers the FORMAT strict category.
     *
     * @return this reader, for chaining
     * @see chirp.text.Check#isURL(String)
     * @since 1.0.0
     */
    public StringReader url() {
        this.asURL = true;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: url");
        return this;
    }

    // ------------------------------------------------------------ rule engine

    /**
     * Requires the input to pass the named rule from the
     * {@link Rules} registry. Covers the FORMAT strict category.
     *
     * <p>Unknown rule names throw {@link IllegalArgumentException} at
     * configuration time — the caller should know they misspelled the
     * name before the user is prompted.</p>
     *
     * @param ruleName the registered rule name
     * @return this reader, for chaining
     * @throws IllegalArgumentException if no rule with that name is registered
     * @see #rule(Rule)
     * @since 1.0.0
     */
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

    /**
     * Requires the input to pass the given {@link Rule}. Covers the
     * FORMAT strict category.
     *
     * @param rule the rule (null → no-op)
     * @return this reader, for chaining
     * @see #rule(String)
     * @since 1.0.0
     */
    public StringReader rule(Rule rule) {
        if (rule == null) return this;
        this.rule = rule;
        applied.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: rule({})", rule.name());
        return this;
    }

    /**
     * Attaches a custom validator with its own error message.
     *
     * @param validator the validator (null → no custom check)
     * @param error the error message
     * @return this reader, for chaining
     * @since 1.0.0
     */
    public StringReader custom(Predicate<String> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        applied.add(ValidationCategory.CUSTOM);
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    // ------------------------------------------------------------ defaults

    /**
     * Sets a default value used when the user presses Enter without
     * typing.
     *
     * @param value the default value
     * @return this reader, for chaining
     * @see #defaulting(String)
     * @see #orElse(String)
     * @since 1.0.0
     */
    public StringReader defaultValue(String value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }

    /**
     * Alias of {@link #defaultValue(String)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(String)
     * @since 1.0.0
     */
    public StringReader defaulting(String value) { return defaultValue(value); }

    /**
     * Alias of {@link #defaultValue(String)} — see that method for full
     * documentation.
     *
     * @param value the default value
     * @return this reader
     * @see #defaultValue(String)
     * @since 1.0.0
     */
    public StringReader orElse(String value) { return defaultValue(value); }

    /**
     * Sets help text shown before the prompt.
     *
     * @param text the help text
     * @return this reader
     * @see #hint(String)
     * @since 1.0.0
     */
    public StringReader help(String text) { this.helpText = text; return this; }

    /**
     * Alias of {@link #help(String)} — see that method for full
     * documentation.
     *
     * @param text the help text
     * @return this reader
     * @see #help(String)
     * @since 1.0.0
     */
    public StringReader hint(String text) { return help(text); }

    // -------------------------------------------------- strict-mode controls

    /**
     * Silences all strict-mode warnings for this reader.
     *
     * @return this reader
     * @see #skipBlankWarning()
     * @see #skipLengthWarning()
     * @see #skipFormatWarning()
     * @since 1.0.0
     */
    public StringReader lenient() { this.lenient = true; Trace.debug("reader", "configure: lenient"); return this; }

    /**
     * Alias of {@link #lenient()} — see that method for full
     * documentation.
     *
     * @return this reader
     * @see #lenient()
     * @since 1.0.0
     */
    public StringReader noStrict() { return lenient(); }

    /**
     * Silences only the BLANK warning — useful when the field can
     * legitimately be empty.
     *
     * @return this reader
     * @since 1.0.0
     */
    public StringReader skipBlankWarning() {
        skipped.add(ValidationCategory.BLANK);
        Trace.debug("reader", "configure: skipBlankWarning");
        return this;
    }

    /**
     * Silences only the LENGTH warning.
     *
     * @return this reader
     * @since 1.0.0
     */
    public StringReader skipLengthWarning() {
        skipped.add(ValidationCategory.LENGTH);
        Trace.debug("reader", "configure: skipLengthWarning");
        return this;
    }

    /**
     * Silences only the FORMAT warning — useful when the field's format
     * is genuinely free-form.
     *
     * @return this reader
     * @since 1.0.0
     */
    public StringReader skipFormatWarning() {
        skipped.add(ValidationCategory.FORMAT);
        Trace.debug("reader", "configure: skipFormatWarning");
        return this;
    }

    // ------------------------------------------------------------ terminal

    /**
     * Reads a string. Loops until the input passes every configured
     * check. Requires {@code Chirp.start()}.
     *
     * @return the parsed string
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #get()
     * @see #value()
     * @see #parse()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed string
     * @see #read()
     * @since 1.0.0
     */
    public String get() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed string
     * @see #read()
     * @since 1.0.0
     */
    public String value() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the parsed string
     * @see #read()
     * @since 1.0.0
     */
    public String parse() { return read(); }
}
