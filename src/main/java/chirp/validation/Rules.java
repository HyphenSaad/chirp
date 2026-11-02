package chirp.validation;

import chirp.internal.Trace;
import chirp.text.Check;
import chirp.text.Regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry of named validation rules — built-in and user-defined.
 *
 * <p>Once a rule is registered here, it can be applied from anywhere by
 * name — {@link #get(String)} returns the {@link Rule}, and
 * {@link #test(String, String)} answers the one-shot "does this value
 * pass this rule" question. The registry is populated at class-load time
 * with a full set of common rules ({@code EMAIL}, {@code NAME}, {@code URL},
 * {@code USERNAME}, all the Pakistani formats, and so on).</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Registry API</b> — {@link #register(Rule)}, {@link #get(String)},
 *       {@link #has(String)}, {@link #names()}, {@link #reset()}</li>
 *   <li><b>One-shot testing</b> — {@link #test(String, String)},
 *       {@link #is(String, String)}</li>
 *   <li><b>Built-in shortcuts</b> — {@link #email()}, {@link #name()},
 *       {@link #pakistaniMobile()}, {@link #pakistaniCnic()},
 *       {@link #username()}, {@link #url()}, {@link #strongPassword()},
 *       {@link #uuid()}, {@link #hexColor()}</li>
 *   <li><b>Domain helper</b> — {@link #cnicProvince(String)}</li>
 * </ul>
 *
 * <h2>Built-in rules</h2>
 * <table border="1">
 *   <caption>Ready-to-use rules registered at startup</caption>
 *   <tr><th>Registry name</th><th>What it validates</th></tr>
 *   <tr><td>{@code EMAIL}</td><td>Standard email syntax (RFC 5322 subset)</td></tr>
 *   <tr><td>{@code NAME}</td><td>Person name (Unicode, spaces/hyphens/apostrophes)</td></tr>
 *   <tr><td>{@code PHONE_INTL}</td><td>International phone (7–15 digits, optional +)</td></tr>
 *   <tr><td>{@code USERNAME}</td><td>3–20 letters/digits/underscore</td></tr>
 *   <tr><td>{@code STRONG_PASSWORD}</td><td>8+ chars with upper/lower/digit</td></tr>
 *   <tr><td>{@code URL}</td><td>{@code http://}, {@code https://}, or {@code ftp://}</td></tr>
 *   <tr><td>{@code UUID}</td><td>Canonical 8-4-4-4-12 hex UUID</td></tr>
 *   <tr><td>{@code HEX_COLOR}</td><td>{@code #RRGGBB} hex colour</td></tr>
 *   <tr><td>{@code PK_MOBILE}</td><td>Pakistani mobile in any common format</td></tr>
 *   <tr><td>{@code PK_CNIC}</td><td>Pakistani CNIC (13 digits, province 1–8)</td></tr>
 *   <tr><td>{@code PK_LANDLINE}</td><td>Pakistani landline</td></tr>
 *   <tr><td>{@code PK_POSTAL_CODE}</td><td>5-digit postal code</td></tr>
 *   <tr><td>{@code PK_PASSPORT}</td><td>2 letters + 7 digits</td></tr>
 *   <tr><td>{@code PK_NTN}</td><td>7-digit National Tax Number</td></tr>
 *   <tr><td>{@code PK_IBAN}</td><td>24-character IBAN starting {@code PK}</td></tr>
 *   <tr><td>{@code PK_VEHICLE_PLATE}</td><td>Vehicle registration plate</td></tr>
 * </table>
 *
 * <h2>Registering a custom rule</h2>
 * <pre>{@code
 * Rules.register(
 *     Rule.define("EMPLOYEE_ID")
 *         .stripNonDigits()
 *         .matches("^\\d{6}$")
 *         .message("Employee ID must be 6 digits")
 *         .build());
 *
 * // Now usable from anywhere by name
 * Check.is("123456", "EMPLOYEE_ID");      // true
 * Rules.test("123", "EMPLOYEE_ID");        // false
 * }</pre>
 *
 * <h2>Case-insensitive lookup</h2>
 * <p>Rule names are normalised to uppercase on registration and on
 * lookup, so {@code Rules.get("pk_mobile")},
 * {@code Rules.get("PK_MOBILE")}, and
 * {@code Rules.get("Pk_Mobile")} all return the same rule.</p>
 *
 * <h2>Normalization</h2>
 * <p>Rules that describe a value with well-known formatting
 * ({@code EMAIL}, {@code NAME}, {@code PK_CNIC}, {@code PK_MOBILE}) carry
 * normalizers that trim or strip separators before applying the pattern.
 * This makes them more forgiving than the raw pattern exposed by
 * {@link Regex} — {@code Rules.name().test("  Saad  ")} returns
 * {@code true}, whereas {@code Check.isName("  Saad  ")} returns
 * {@code false} because {@code Check} uses the raw pattern without
 * trimming.</p>
 *
 * <h2>Thread safety</h2>
 * <p>Backed by a {@link java.util.concurrent.ConcurrentHashMap}, so
 * registration and lookup are safe from multiple threads. This matters
 * in JavaFX applications where rules may be registered from event
 * handlers while other threads are validating.</p>
 *
 * <h2>Related</h2>
 * <p>For the rule type itself, see {@link Rule}. For the fluent builder,
 * see {@link RuleBuilder}. For multi-error validation, see
 * {@link Validate}. For boolean checks without lookup, see
 * {@link chirp.text.Check}.</p>
 *
 * @see Rule
 * @see RuleBuilder
 * @see Validate
 * @see chirp.text.Check
 * @see chirp.text.Regex
 * @since 1.0.0
 */
public final class Rules {

    private static final Map<String, Rule> REGISTRY = new ConcurrentHashMap<>();

    static {
        registerBuiltins();
        Trace.debug("rule", "builtin rules registered: {}", REGISTRY.size());
    }

    private Rules() {}

    // ============================================================== registry API

    /**
     * Registers (or replaces) a rule.
     *
     * <p>The rule name is uppercased before storage. If a rule with the
     * same name already exists, it is replaced — the previous rule is
     * returned so callers can restore it if desired.</p>
     *
     * @param rule the rule to register (null → no-op)
     * @return the previous rule with this name, or {@code null} if none
     * @see #add(Rule)
     * @see #put(Rule)
     * @see #define(Rule)
     * @see #get(String)
     * @see #reset()
     * @since 1.0.0
     */
    public static Rule register(Rule rule) {
        if (rule == null || rule.name() == null) return null;
        Rule previous = REGISTRY.put(rule.name().toUpperCase(Locale.ROOT), rule);
        Trace.debug("rule", "registered '{}' (previous={})",
                rule.name(), previous == null ? "none" : "replaced");
        return previous;
    }

    /**
     * Alias of {@link #register(Rule)} — see that method for full
     * documentation and examples.
     *
     * @param rule the rule to register
     * @return the previous rule, or null
     * @see #register(Rule)
     * @since 1.0.0
     */
    public static Rule add(Rule rule) { return register(rule); }

    /**
     * Alias of {@link #register(Rule)} — see that method for full
     * documentation and examples.
     *
     * @param rule the rule to register
     * @return the previous rule, or null
     * @see #register(Rule)
     * @since 1.0.0
     */
    public static Rule put(Rule rule) { return register(rule); }

    /**
     * Alias of {@link #register(Rule)} — see that method for full
     * documentation and examples.
     *
     * @param rule the rule to register
     * @return the previous rule, or null
     * @see #register(Rule)
     * @since 1.0.0
     */
    public static Rule define(Rule rule) { return register(rule); }

    /**
     * Builds a rule from key-value pairs and registers it.
     *
     * <p>Convenience for one-liner registrations:</p>
     * <pre>{@code
     * Rules.register(
     *     Rule.Key.NAME, "MY_RULE",
     *     Rule.Key.PATTERN, "\\d+",
     *     Rule.Key.MESSAGE, "must be digits");
     * }</pre>
     *
     * @param keyValues alternating {@link Rule.Key} → value pairs
     * @return the newly registered rule
     * @see #register(Rule)
     * @see Rule#from(Object...)
     * @since 1.0.0
     */
    public static Rule register(Object... keyValues) {
        Rule rule = Rule.from(keyValues);
        register(rule);
        return rule;
    }

    /**
     * Alias of {@link #register(Object...)} — see that method for full
     * documentation and examples.
     *
     * @param keyValues alternating key-value pairs
     * @return the newly registered rule
     * @see #register(Object...)
     * @since 1.0.0
     */
    public static Rule define(Object... keyValues) {
        return register(keyValues);
    }

    /**
     * Retrieves a rule by name. Returns {@code null} if absent.
     *
     * <p>Lookup is case-insensitive — {@code "pk_mobile"} and
     * {@code "PK_MOBILE"} return the same rule.</p>
     *
     * @param name the rule name (may be null)
     * @return the rule, or {@code null} if not registered
     * @see #find(String)
     * @see #lookup(String)
     * @see #rule(String)
     * @see #require(String)
     * @since 1.0.0
     */
    public static Rule get(String name) {
        if (name == null) return null;
        Rule rule = REGISTRY.get(name.toUpperCase(Locale.ROOT));
        Trace.trace("rule", "lookup '{}' -> {}", name, rule == null ? "missing" : rule.name());
        return rule;
    }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return the rule, or null
     * @see #get(String)
     * @since 1.0.0
     */
    public static Rule find(String name) { return get(name); }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return the rule, or null
     * @see #get(String)
     * @since 1.0.0
     */
    public static Rule lookup(String name) { return get(name); }

    /**
     * Alias of {@link #get(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return the rule, or null
     * @see #get(String)
     * @since 1.0.0
     */
    public static Rule rule(String name) { return get(name); }

    /**
     * Retrieves a rule by name, throwing if absent.
     *
     * @param name the rule name
     * @return the rule
     * @throws IllegalArgumentException if the rule is not registered
     * @see #get(String)
     * @see #mustGet(String)
     * @since 1.0.0
     */
    public static Rule require(String name) {
        Rule rule = get(name);
        if (rule == null) {
            Trace.warn("rule", "require('{}') failed: not registered", name);
            throw new IllegalArgumentException("No rule named '" + name + "'");
        }
        return rule;
    }

    /**
     * Alias of {@link #require(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return the rule
     * @throws IllegalArgumentException if the rule is not registered
     * @see #require(String)
     * @since 1.0.0
     */
    public static Rule mustGet(String name) { return require(name); }

    /**
     * Checks whether a rule with this name is registered.
     *
     * @param name the rule name (may be null)
     * @return {@code true} if registered
     * @see #exists(String)
     * @see #contains(String)
     * @since 1.0.0
     */
    public static boolean has(String name) {
        return name != null && REGISTRY.containsKey(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Alias of {@link #has(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return {@code true} if registered
     * @see #has(String)
     * @since 1.0.0
     */
    public static boolean exists(String name) { return has(name); }

    /**
     * Alias of {@link #has(String)} — see that method for full
     * documentation.
     *
     * @param name the rule name
     * @return {@code true} if registered
     * @see #has(String)
     * @since 1.0.0
     */
    public static boolean contains(String name) { return has(name); }

    /**
     * Returns the set of registered rule names, in registration order.
     *
     * @return the names (never null)
     * @see #listAll()
     * @see #registered()
     * @see #allNames()
     * @since 1.0.0
     */
    public static Set<String> names() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(REGISTRY.keySet()));
    }

    /**
     * Alias of {@link #names()} — see that method for full documentation.
     *
     * @return the registered rule names
     * @see #names()
     * @since 1.0.0
     */
    public static Set<String> listAll() { return names(); }

    /**
     * Alias of {@link #names()} — see that method for full documentation.
     *
     * @return the registered rule names
     * @see #names()
     * @since 1.0.0
     */
    public static Set<String> registered() { return names(); }

    /**
     * Alias of {@link #names()} — see that method for full documentation.
     *
     * @return the registered rule names
     * @see #names()
     * @since 1.0.0
     */
    public static Set<String> allNames() { return names(); }

    /**
     * Removes all rules and re-registers the built-ins. Useful for test
     * isolation.
     *
     * @see #clear()
     * @since 1.0.0
     */
    public static void reset() {
        REGISTRY.clear();
        registerBuiltins();
        Trace.debug("rule", "reset to builtins ({})", REGISTRY.size());
    }

    /**
     * Alias of {@link #reset()} — see that method for full documentation.
     *
     * @see #reset()
     * @since 1.0.0
     */
    public static void clear() { reset(); }

    /**
     * Returns multiple rules by name, throwing if any is missing.
     *
     * @param names the rule names
     * @return the resolved rules
     * @throws IllegalArgumentException if any name is not registered
     * @see #require(String)
     * @since 1.0.0
     */
    public static List<Rule> all(String... names) {
        if (names == null) return List.of();
        List<Rule> result = new ArrayList<>(names.length);
        for (String n : names) result.add(require(n));
        return result;
    }

    /**
     * One-shot rule application — returns {@code false} for unknown rule
     * names rather than throwing.
     *
     * @param value the value to test (may be null)
     * @param ruleName the rule name (may be null → false)
     * @return {@code true} if the rule exists and passes
     * @see #check(String, String)
     * @see #is(String, String)
     * @since 1.0.0
     */
    public static boolean test(String value, String ruleName) {
        Rule rule = get(ruleName);
        return rule != null && rule.test(value);
    }

    /**
     * Alias of {@link #test(String, String)} — see that method for full
     * documentation.
     *
     * @param value the value to test
     * @param ruleName the rule name
     * @return {@code true} if the rule passes
     * @see #test(String, String)
     * @since 1.0.0
     */
    public static boolean check(String value, String ruleName) { return test(value, ruleName); }

    /**
     * Alias of {@link #test(String, String)} — see that method for full
     * documentation.
     *
     * @param value the value to test
     * @param ruleName the rule name
     * @return {@code true} if the rule passes
     * @see #test(String, String)
     * @since 1.0.0
     */
    public static boolean is(String value, String ruleName) { return test(value, ruleName); }

    // ============================================================== built-in shortcuts

    /**
     * Returns the built-in {@code EMAIL} rule.
     *
     * @return the email rule
     * @see #emailAddress()
     * @see #emailRule()
     * @see chirp.text.Check#isEmail(String)
     * @since 1.0.0
     */
    public static Rule email() { return require("EMAIL"); }

    /**
     * Alias of {@link #email()} — see that method for full documentation.
     *
     * @return the email rule
     * @see #email()
     * @since 1.0.0
     */
    public static Rule emailAddress() { return require("EMAIL"); }

    /**
     * Alias of {@link #email()} — see that method for full documentation.
     *
     * @return the email rule
     * @see #email()
     * @since 1.0.0
     */
    public static Rule emailRule() { return require("EMAIL"); }

    /**
     * Returns the built-in {@code NAME} rule — a Unicode-aware person-name
     * check equivalent to {@link Check#isName(String)}, plus a
     * {@code .trim()} normalizer so surrounding whitespace is accepted.
     *
     * <pre>{@code
     * Rules.name().test("Saad");         // true
     * Rules.name().test("Saad Mansoor"); // true
     * Rules.name().test("O'Brien");      // true
     * Rules.name().test("  Saad  ");     // true  (trim)
     * Rules.name().test("Saad123");      // false (digit)
     * Rules.name().test("Saad  M");      // false (double space)
     * }</pre>
     *
     * @return the {@code NAME} rule (never null)
     * @see #personName()
     * @see #nameRule()
     * @see Check#isName(String)
     * @see Regex#name()
     * @since 1.0.0
     */
    public static Rule name() { return require("NAME"); }

    /**
     * Alias of {@link #name()} — see that method for full
     * documentation and examples.
     *
     * @return the {@code NAME} rule
     * @see #name()
     * @since 1.0.0
     */
    public static Rule personName() { return require("NAME"); }

    /**
     * Alias of {@link #name()} — see that method for full
     * documentation and examples.
     *
     * @return the {@code NAME} rule
     * @see #name()
     * @since 1.0.0
     */
    public static Rule nameRule() { return require("NAME"); }

    /**
     * Returns the built-in {@code PHONE_INTL} rule.
     *
     * @return the international phone rule
     * @since 1.0.0
     */
    public static Rule internationalPhone() { return require("PHONE_INTL"); }

    /**
     * Alias of {@link #internationalPhone()} — see that method for full
     * documentation.
     *
     * @return the international phone rule
     * @see #internationalPhone()
     * @since 1.0.0
     */
    public static Rule phoneIntl() { return require("PHONE_INTL"); }

    /**
     * Alias of {@link #internationalPhone()} — see that method for full
     * documentation.
     *
     * @return the international phone rule
     * @see #internationalPhone()
     * @since 1.0.0
     */
    public static Rule phoneInternational() { return require("PHONE_INTL"); }

    /**
     * Alias of {@link #internationalPhone()} — see that method for full
     * documentation.
     *
     * @return the international phone rule
     * @see #internationalPhone()
     * @since 1.0.0
     */
    public static Rule globalPhone() { return require("PHONE_INTL"); }

    /**
     * Returns the built-in {@code USERNAME} rule.
     *
     * @return the username rule
     * @since 1.0.0
     */
    public static Rule username() { return require("USERNAME"); }

    /**
     * Alias of {@link #username()} — see that method for full
     * documentation.
     *
     * @return the username rule
     * @see #username()
     * @since 1.0.0
     */
    public static Rule userName() { return require("USERNAME"); }

    /**
     * Alias of {@link #username()} — see that method for full
     * documentation.
     *
     * @return the username rule
     * @see #username()
     * @since 1.0.0
     */
    public static Rule login() { return require("USERNAME"); }

    /**
     * Returns the built-in {@code STRONG_PASSWORD} rule.
     *
     * @return the strong-password rule
     * @since 1.0.0
     */
    public static Rule strongPassword() { return require("STRONG_PASSWORD"); }

    /**
     * Alias of {@link #strongPassword()} — see that method for full
     * documentation.
     *
     * @return the strong-password rule
     * @see #strongPassword()
     * @since 1.0.0
     */
    public static Rule strongPwd() { return require("STRONG_PASSWORD"); }

    /**
     * Alias of {@link #strongPassword()} — see that method for full
     * documentation.
     *
     * @return the strong-password rule
     * @see #strongPassword()
     * @since 1.0.0
     */
    public static Rule password() { return require("STRONG_PASSWORD"); }

    /**
     * Returns the built-in {@code URL} rule.
     *
     * @return the URL rule
     * @since 1.0.0
     */
    public static Rule url() { return require("URL"); }

    /**
     * Alias of {@link #url()} — see that method for full documentation.
     *
     * @return the URL rule
     * @see #url()
     * @since 1.0.0
     */
    public static Rule webUrl() { return require("URL"); }

    /**
     * Alias of {@link #url()} — see that method for full documentation.
     *
     * @return the URL rule
     * @see #url()
     * @since 1.0.0
     */
    public static Rule link() { return require("URL"); }

    /**
     * Returns the built-in {@code UUID} rule.
     *
     * @return the UUID rule
     * @since 1.0.0
     */
    public static Rule uuid() { return require("UUID"); }

    /**
     * Alias of {@link #uuid()} — see that method for full documentation.
     *
     * @return the UUID rule
     * @see #uuid()
     * @since 1.0.0
     */
    public static Rule guid() { return require("UUID"); }

    /**
     * Returns the built-in {@code HEX_COLOR} rule.
     *
     * @return the hex colour rule
     * @since 1.0.0
     */
    public static Rule hexColor() { return require("HEX_COLOR"); }

    /**
     * Alias of {@link #hexColor()} — see that method for full
     * documentation.
     *
     * @return the hex colour rule
     * @see #hexColor()
     * @since 1.0.0
     */
    public static Rule hexColour() { return require("HEX_COLOR"); }

    /**
     * Alias of {@link #hexColor()} — see that method for full
     * documentation.
     *
     * @return the hex colour rule
     * @see #hexColor()
     * @since 1.0.0
     */
    public static Rule colorHex() { return require("HEX_COLOR"); }

    // ------------------------------------------------------ Pakistan rules

    /**
     * Returns the built-in {@code PK_MOBILE} rule.
     *
     * @return the Pakistani mobile rule
     * @see chirp.text.Check#isPakistaniPhone(String)
     * @since 1.0.0
     */
    public static Rule pakistaniMobile() { return require("PK_MOBILE"); }

    /**
     * Alias of {@link #pakistaniMobile()} — see that method for full
     * documentation.
     *
     * @return the Pakistani mobile rule
     * @see #pakistaniMobile()
     * @since 1.0.0
     */
    public static Rule pkMobile() { return require("PK_MOBILE"); }

    /**
     * Alias of {@link #pakistaniMobile()} — see that method for full
     * documentation.
     *
     * @return the Pakistani mobile rule
     * @see #pakistaniMobile()
     * @since 1.0.0
     */
    public static Rule pkPhone() { return require("PK_MOBILE"); }

    /**
     * Alias of {@link #pakistaniMobile()} — see that method for full
     * documentation.
     *
     * @return the Pakistani mobile rule
     * @see #pakistaniMobile()
     * @since 1.0.0
     */
    public static Rule pakistanMobile() { return require("PK_MOBILE"); }

    /**
     * Alias of {@link #pakistaniMobile()} — see that method for full
     * documentation.
     *
     * @return the Pakistani mobile rule
     * @see #pakistaniMobile()
     * @since 1.0.0
     */
    public static Rule pkMobileNumber() { return require("PK_MOBILE"); }

    /**
     * Returns the built-in {@code PK_CNIC} rule.
     *
     * @return the Pakistani CNIC rule
     * @see chirp.text.Check#isPakistaniCnic(String)
     * @see #cnicProvince(String)
     * @since 1.0.0
     */
    public static Rule pakistaniCnic() { return require("PK_CNIC"); }

    /**
     * Alias of {@link #pakistaniCnic()} — see that method for full
     * documentation.
     *
     * @return the Pakistani CNIC rule
     * @see #pakistaniCnic()
     * @since 1.0.0
     */
    public static Rule pkCnic() { return require("PK_CNIC"); }

    /**
     * Alias of {@link #pakistaniCnic()} — see that method for full
     * documentation.
     *
     * @return the Pakistani CNIC rule
     * @see #pakistaniCnic()
     * @since 1.0.0
     */
    public static Rule pkIdCard() { return require("PK_CNIC"); }

    /**
     * Alias of {@link #pakistaniCnic()} — see that method for full
     * documentation.
     *
     * @return the Pakistani CNIC rule
     * @see #pakistaniCnic()
     * @since 1.0.0
     */
    public static Rule pakistanCnic() { return require("PK_CNIC"); }

    /**
     * Returns the built-in {@code PK_LANDLINE} rule.
     *
     * @return the Pakistani landline rule
     * @since 1.0.0
     */
    public static Rule pakistaniLandline() { return require("PK_LANDLINE"); }

    /**
     * Alias of {@link #pakistaniLandline()} — see that method for full
     * documentation.
     *
     * @return the Pakistani landline rule
     * @see #pakistaniLandline()
     * @since 1.0.0
     */
    public static Rule pkLandline() { return require("PK_LANDLINE"); }

    /**
     * Alias of {@link #pakistaniLandline()} — see that method for full
     * documentation.
     *
     * @return the Pakistani landline rule
     * @see #pakistaniLandline()
     * @since 1.0.0
     */
    public static Rule pkPhoneLandline() { return require("PK_LANDLINE"); }

    /**
     * Returns the built-in {@code PK_POSTAL_CODE} rule.
     *
     * @return the Pakistani postal code rule
     * @since 1.0.0
     */
    public static Rule pakistaniPostalCode() { return require("PK_POSTAL_CODE"); }

    /**
     * Alias of {@link #pakistaniPostalCode()} — see that method for full
     * documentation.
     *
     * @return the Pakistani postal code rule
     * @see #pakistaniPostalCode()
     * @since 1.0.0
     */
    public static Rule pkPostalCode() { return require("PK_POSTAL_CODE"); }

    /**
     * Alias of {@link #pakistaniPostalCode()} — see that method for full
     * documentation.
     *
     * @return the Pakistani postal code rule
     * @see #pakistaniPostalCode()
     * @since 1.0.0
     */
    public static Rule pkZip() { return require("PK_POSTAL_CODE"); }

    /**
     * Returns the built-in {@code PK_PASSPORT} rule.
     *
     * @return the Pakistani passport rule
     * @since 1.0.0
     */
    public static Rule pakistaniPassport() { return require("PK_PASSPORT"); }

    /**
     * Alias of {@link #pakistaniPassport()} — see that method for full
     * documentation.
     *
     * @return the Pakistani passport rule
     * @see #pakistaniPassport()
     * @since 1.0.0
     */
    public static Rule pkPassport() { return require("PK_PASSPORT"); }

    /**
     * Returns the built-in {@code PK_NTN} rule.
     *
     * @return the Pakistani NTN rule
     * @since 1.0.0
     */
    public static Rule pakistaniNtn() { return require("PK_NTN"); }

    /**
     * Alias of {@link #pakistaniNtn()} — see that method for full
     * documentation.
     *
     * @return the Pakistani NTN rule
     * @see #pakistaniNtn()
     * @since 1.0.0
     */
    public static Rule pkNtn() { return require("PK_NTN"); }

    /**
     * Alias of {@link #pakistaniNtn()} — see that method for full
     * documentation.
     *
     * @return the Pakistani NTN rule
     * @see #pakistaniNtn()
     * @since 1.0.0
     */
    public static Rule pkTaxNumber() { return require("PK_NTN"); }

    /**
     * Returns the built-in {@code PK_IBAN} rule.
     *
     * @return the Pakistani IBAN rule
     * @since 1.0.0
     */
    public static Rule pakistaniIban() { return require("PK_IBAN"); }

    /**
     * Alias of {@link #pakistaniIban()} — see that method for full
     * documentation.
     *
     * @return the Pakistani IBAN rule
     * @see #pakistaniIban()
     * @since 1.0.0
     */
    public static Rule pkIban() { return require("PK_IBAN"); }

    /**
     * Returns the built-in {@code PK_VEHICLE_PLATE} rule.
     *
     * @return the Pakistani vehicle plate rule
     * @since 1.0.0
     */
    public static Rule pakistaniVehiclePlate() { return require("PK_VEHICLE_PLATE"); }

    /**
     * Alias of {@link #pakistaniVehiclePlate()} — see that method for full
     * documentation.
     *
     * @return the Pakistani vehicle plate rule
     * @see #pakistaniVehiclePlate()
     * @since 1.0.0
     */
    public static Rule pkVehiclePlate() { return require("PK_VEHICLE_PLATE"); }

    /**
     * Alias of {@link #pakistaniVehiclePlate()} — see that method for full
     * documentation.
     *
     * @return the Pakistani vehicle plate rule
     * @see #pakistaniVehiclePlate()
     * @since 1.0.0
     */
    public static Rule pkPlate() { return require("PK_VEHICLE_PLATE"); }

    // ============================================================== domain helper

    /**
     * Returns the Pakistani province that issued the given CNIC, or
     * {@code null} if the CNIC is invalid.
     *
     * <p>The first digit of the CNIC encodes the province:</p>
     * <table border="1">
     *   <caption>CNIC province encoding</caption>
     *   <tr><th>Digit</th><th>Province</th></tr>
     *   <tr><td>1</td><td>Khyber Pakhtunkhwa</td></tr>
     *   <tr><td>2, 4</td><td>Sindh</td></tr>
     *   <tr><td>3, 5</td><td>Punjab</td></tr>
     *   <tr><td>6</td><td>Balochistan</td></tr>
     *   <tr><td>7</td><td>Azad Jammu and Kashmir</td></tr>
     *   <tr><td>8</td><td>Gilgit-Baltistan</td></tr>
     * </table>
     *
     * <pre>{@code
     * Rules.cnicProvince("35202-1234567-1");   // "Punjab"
     * Rules.cnicProvince("42201-1234567-2");   // "Sindh"
     * Rules.cnicProvince("invalid");            // null
     * }</pre>
     *
     * @param cnic the CNIC (may be null or invalid)
     * @return the province name, or {@code null} if the CNIC is invalid
     * @see #pakistaniCnic()
     * @since 1.0.0
     */
    public static String cnicProvince(String cnic) {
        Rule rule = pakistaniCnic();
        if (!rule.test(cnic)) return null;
        String digits = rule.normalize(cnic);
        return switch (digits.charAt(0)) {
            case '1' -> "Khyber Pakhtunkhwa";
            case '2' -> "Sindh";
            case '3' -> "Punjab";
            case '4' -> "Sindh";
            case '5' -> "Punjab";
            case '6' -> "Balochistan";
            case '7' -> "Azad Jammu and Kashmir";
            case '8' -> "Gilgit-Baltistan";
            default -> null;
        };
    }

    // ============================================================== registration

    private static void registerBuiltins() {
        register(Rule.define("EMAIL")
                .description("Email address")
                .trim()
                .matches(Regex.email())
                .message("Enter a valid email address.")
                .build());

        register(Rule.define("NAME")
                .description("Person name")
                .trim()
                .matches(Regex.name())
                .message("Enter a valid name (letters, spaces, hyphens, apostrophes only).")
                .build());

        register(Rule.define("PHONE_INTL")
                .description("International phone number")
                .stripAll("[\\s\\-()]")
                .matches("^\\+?[0-9]{7,15}$")
                .message("Enter a valid phone number (7–15 digits, optional leading +).")
                .build());

        register(Rule.define("USERNAME")
                .description("Username")
                .trim()
                .matches("^[A-Za-z0-9_]{3,20}$")
                .message("Use 3–20 letters, digits, or underscores.")
                .build());

        register(Rule.define("STRONG_PASSWORD")
                .description("Strong password")
                .satisfies(Check::isStrongPassword)
                .message("Use at least 8 characters with upper case, lower case, and a digit.")
                .build());

        register(Rule.define("URL")
                .description("URL")
                .trim()
                .matches(Regex.url())
                .message("Enter a valid URL starting with http:// or https://.")
                .build());

        register(Rule.define("UUID")
                .description("UUID")
                .trim()
                .matches(Regex.uuid())
                .message("Enter a valid UUID.")
                .build());

        register(Rule.define("HEX_COLOR")
                .description("Hex colour")
                .trim()
                .matches(Regex.hexColor())
                .message("Enter a colour like #FF00AA.")
                .build());

        register(Rule.define("PK_MOBILE")
                .description("Pakistani mobile number")
                .stripAll("[\\s\\-()]")
                .matches("^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$")
                .message("Enter a valid Pakistani mobile (e.g. 0300-1234567 or +92 300 1234567).")
                .build());

        register(Rule.define("PK_CNIC")
                .description("Pakistani CNIC")
                .stripNonDigits()
                .matches("^[1-8]\\d{12}$")
                .message("Enter a valid 13-digit CNIC (e.g. 35202-1234567-1).")
                .build());

        register(Rule.define("PK_LANDLINE")
                .description("Pakistani landline")
                .stripAll("[\\s\\-()]")
                .matches("^0[2-9]\\d{8,9}$")
                .message("Enter a valid Pakistani landline (e.g. 042-1234567 or 021-12345678).")
                .build());

        register(Rule.define("PK_POSTAL_CODE")
                .description("Pakistani postal code")
                .stripSpaces()
                .matches("^\\d{5}$")
                .message("Enter a 5-digit Pakistani postal code.")
                .build());

        register(Rule.define("PK_PASSPORT")
                .description("Pakistani passport")
                .trim()
                .upperCase()
                .matches("^[A-Z]{2}\\d{7}$")
                .message("Enter a valid Pakistani passport number (e.g. AB1234567).")
                .build());

        register(Rule.define("PK_NTN")
                .description("Pakistani NTN")
                .stripSpaces()
                .matches("^\\d{7}(-\\d)?$")
                .message("Enter a valid Pakistani NTN (7 digits, optional -check).")
                .build());

        register(Rule.define("PK_IBAN")
                .description("Pakistani IBAN")
                .trim()
                .upperCase()
                .stripSpaces()
                .matches("^PK\\d{2}[A-Z]{4}[A-Z0-9]{16}$")
                .message("Enter a valid Pakistani IBAN (24 characters starting with PK).")
                .build());

        register(Rule.define("PK_VEHICLE_PLATE")
                .description("Pakistani vehicle registration plate")
                .trim()
                .upperCase()
                .matches("^[A-Z]{2,4}[- ]?\\d{3,5}$")
                .message("Enter a valid Pakistani plate (e.g. ABC-1234 or LEB-12345).")
                .build());
    }
}
