package chirp.validation;

import chirp.internal.Trace;
import chirp.text.Check;
import chirp.text.Regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of reusable validation rules — built-in and user-defined.
 *
 * <h2>Naming</h2>
 * <p>Every built-in and every lookup method has 2–4 aliases:
 * {@code get} / {@code find} / {@code lookup} / {@code rule};
 * {@code has} / {@code contains} / {@code exists};
 * {@code pakistaniMobile} / {@code pkMobile} / {@code pkPhone} / {@code pakistanMobile}.</p>
 */
public final class Rules {

    private static final Map<String, Rule> REGISTRY = new ConcurrentHashMap<>();

    static {
        registerBuiltins();
        Trace.debug("rule", "builtin rules registered: {}", REGISTRY.size());
    }

    private Rules() {}

    // ============================================================== registry API

    /** Registers (or replaces) a rule. Returns the previous rule, or {@code null}. */
    public static Rule register(Rule rule) {
        if (rule == null || rule.name() == null) return null;
        Rule previous = REGISTRY.put(rule.name().toUpperCase(), rule);
        Trace.debug("rule", "registered '{}' (previous={})",
                rule.name(), previous == null ? "none" : "replaced");
        return previous;
    }
    public static Rule add(Rule rule) { return register(rule); }
    public static Rule put(Rule rule) { return register(rule); }
    public static Rule define(Rule rule) { return register(rule); }

    /**
     * Key-value registration — builds the rule with {@code Rule.from(...)} and
     * registers it like {@link #register(Rule)}. Returns the newly registered
     * rule (the caller has no other reference to it).
     */
    public static Rule register(Object... keyValues) {
        Rule rule = Rule.from(keyValues);
        register(rule);
        return rule;
    }
    public static Rule define(Object... keyValues) {
        return register(keyValues);
    }

    /** Retrieves a rule by name. Returns {@code null} when absent. */
    public static Rule get(String name) {
        if (name == null) return null;
        Rule rule = REGISTRY.get(name.toUpperCase());
        Trace.trace("rule", "lookup '{}' -> {}", name, rule == null ? "missing" : rule.name());
        return rule;
    }
    public static Rule find(String name) { return get(name); }
    public static Rule lookup(String name) { return get(name); }
    public static Rule rule(String name) { return get(name); }

    /** Retrieves a rule, or throws if absent. */
    public static Rule require(String name) {
        Rule rule = get(name);
        if (rule == null) {
            Trace.warn("rule", "require('{}') failed: not registered", name);
            throw new IllegalArgumentException("No rule named '" + name + "'");
        }
        return rule;
    }
    public static Rule mustGet(String name) { return require(name); }

    public static boolean has(String name) {
        return name != null && REGISTRY.containsKey(name.toUpperCase());
    }
    public static boolean exists(String name) { return has(name); }
    public static boolean contains(String name) { return has(name); }

    public static Set<String> names() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(REGISTRY.keySet()));
    }
    public static Set<String> listAll() { return names(); }
    public static Set<String> registered() { return names(); }
    public static Set<String> allNames() { return names(); }

    public static void reset() {
        REGISTRY.clear();
        registerBuiltins();
        Trace.debug("rule", "reset to builtins ({})", REGISTRY.size());
    }
    public static void clear() { reset(); }

    /** Fetches multiple rules by name, throwing if any is missing. */
    public static List<Rule> all(String... names) {
        if (names == null) return List.of();
        List<Rule> result = new ArrayList<>(names.length);
        for (String n : names) result.add(require(n));
        return result;
    }

    /** One-shot test — applies the named rule to the value. Unknown names → {@code false}. */
    public static boolean test(String value, String ruleName) {
        Rule rule = get(ruleName);
        return rule != null && rule.test(value);
    }
    public static boolean check(String value, String ruleName) { return test(value, ruleName); }
    public static boolean is(String value, String ruleName) { return test(value, ruleName); }

    // ============================================================== built-in shortcuts

    // Email
    public static Rule email() { return require("EMAIL"); }
    public static Rule emailAddress() { return require("EMAIL"); }
    public static Rule emailRule() { return require("EMAIL"); }

    // International phone
    public static Rule internationalPhone() { return require("PHONE_INTL"); }
    public static Rule phoneIntl() { return require("PHONE_INTL"); }
    public static Rule phoneInternational() { return require("PHONE_INTL"); }
    public static Rule globalPhone() { return require("PHONE_INTL"); }

    // Username
    public static Rule username() { return require("USERNAME"); }
    public static Rule userName() { return require("USERNAME"); }
    public static Rule login() { return require("USERNAME"); }

    // Strong password
    public static Rule strongPassword() { return require("STRONG_PASSWORD"); }
    public static Rule strongPwd() { return require("STRONG_PASSWORD"); }
    public static Rule password() { return require("STRONG_PASSWORD"); }

    // URL
    public static Rule url() { return require("URL"); }
    public static Rule webUrl() { return require("URL"); }
    public static Rule link() { return require("URL"); }

    // UUID
    public static Rule uuid() { return require("UUID"); }
    public static Rule guid() { return require("UUID"); }

    // Hex colour
    public static Rule hexColor() { return require("HEX_COLOR"); }
    public static Rule hexColour() { return require("HEX_COLOR"); }
    public static Rule colorHex() { return require("HEX_COLOR"); }

    // ------------------------------------------------------ Pakistan rules

    public static Rule pakistaniMobile() { return require("PK_MOBILE"); }
    public static Rule pkMobile() { return require("PK_MOBILE"); }
    public static Rule pkPhone() { return require("PK_MOBILE"); }
    public static Rule pakistanMobile() { return require("PK_MOBILE"); }
    public static Rule pkMobileNumber() { return require("PK_MOBILE"); }

    public static Rule pakistaniCnic() { return require("PK_CNIC"); }
    public static Rule pkCnic() { return require("PK_CNIC"); }
    public static Rule pkIdCard() { return require("PK_CNIC"); }
    public static Rule pakistanCnic() { return require("PK_CNIC"); }

    public static Rule pakistaniLandline() { return require("PK_LANDLINE"); }
    public static Rule pkLandline() { return require("PK_LANDLINE"); }
    public static Rule pkPhoneLandline() { return require("PK_LANDLINE"); }

    public static Rule pakistaniPostalCode() { return require("PK_POSTAL_CODE"); }
    public static Rule pkPostalCode() { return require("PK_POSTAL_CODE"); }
    public static Rule pkZip() { return require("PK_POSTAL_CODE"); }

    public static Rule pakistaniPassport() { return require("PK_PASSPORT"); }
    public static Rule pkPassport() { return require("PK_PASSPORT"); }

    public static Rule pakistaniNtn() { return require("PK_NTN"); }
    public static Rule pkNtn() { return require("PK_NTN"); }
    public static Rule pkTaxNumber() { return require("PK_NTN"); }

    public static Rule pakistaniIban() { return require("PK_IBAN"); }
    public static Rule pkIban() { return require("PK_IBAN"); }

    public static Rule pakistaniVehiclePlate() { return require("PK_VEHICLE_PLATE"); }
    public static Rule pkVehiclePlate() { return require("PK_VEHICLE_PLATE"); }
    public static Rule pkPlate() { return require("PK_VEHICLE_PLATE"); }

    // ============================================================== domain helper

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
