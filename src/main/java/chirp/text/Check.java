package chirp.text;

import chirp.validation.Rule;
import chirp.validation.Rules;
import chirp.validation.Validate;

import java.util.regex.Pattern;

/**
 * Boolean validations for every common input format.
 *
 * <p>This class answers one question per method: "is this input valid?" Every
 * method returns {@code false} for {@code null} input, so no call site ever
 * needs a null check before validating.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Null / blank</b> — {@link #isNull(Object)}, {@link #isBlank(String)},
 *       {@link #isEmpty(String)}</li>
 *   <li><b>Format</b> — {@link #isEmail(String)}, {@link #isPhone(String)},
 *       {@link #isURL(String)}, {@link #isIPAddress(String)},
 *       {@link #isUUID(String)}, {@link #isUsername(String)},
 *       {@link #isHexColor(String)}, {@link #isNumeric(String)},
 *       {@link #isInteger(String)}, {@link #isJSON(String)},
 *       {@link #isDate(String, String)}</li>
 *   <li><b>Content</b> — {@link #containsDigit(String)}, {@link #containsLetter(String)},
 *       {@link #containsSpecialChar(String)}, {@link #startsWithDigit(String)},
 *       {@link #endsWithDigit(String)}</li>
 *   <li><b>Pakistan</b> — {@link #isPakistaniPhone(String)}, {@link #isPakistaniCnic(String)}</li>
 *   <li><b>Range</b> — {@link #inRange(int, int, int)}, {@link #between(int, int, int)}</li>
 *   <li><b>Length</b> — {@link #minLength(String, int)}, {@link #maxLength(String, int)},
 *       {@link #lengthBetween(String, int, int)}</li>
 *   <li><b>Password</b> — {@link #isStrongPassword(String)},
 *       {@link #isVeryStrongPassword(String)}</li>
 *   <li><b>Domain</b> — {@link #isAdult(int)}, {@link #isValidGrade(int)},
 *       {@link #isLeapYear(int)}</li>
 *   <li><b>Rule engine</b> — {@link #is(String, String)}, {@link #is(String, Rule)}</li>
 * </ul>
 *
 * <h2>Aliases</h2>
 * <p>Every method has 1–4 aliases, so a reader can use whichever name reads
 * most naturally. For example, {@code isEmail}, {@code looksLikeEmail},
 * {@code validEmail}, and {@code isEmailAddress} all return the same boolean
 * for the same input — the aliases are pure delegates, not separate
 * implementations.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * if (Check.isEmail(input)) {
 *     System.out.println("Valid email");
 * }
 *
 * if (Check.isStrongPassword(pwd)) {
 *     System.out.println("Password meets requirements");
 * }
 *
 * if (!Check.isBlank(name) && Check.minLength(name, 2)) {
 *     System.out.println("Hello, " + name);
 * }
 * }</pre>
 *
 * <h2>When to use this vs. {@link Rule} vs. {@link Validate}</h2>
 * <ul>
 *   <li>Need a yes/no answer? → <b>this class</b></li>
 *   <li>Need to reuse a rule across fields, with a custom message? → {@link Rule}</li>
 *   <li>Need to collect all errors at once? → {@link Validate}</li>
 *   <li>Need a raw regex string? → {@link Regex}</li>
 * </ul>
 *
 * <h2>Note on the email regex</h2>
 * <p>{@link #isEmail(String)} uses a linear-time regex that resists
 * catastrophic backtracking on adversarial input (see the private
 * {@link #emailPattern()} method). Other patterns in this class are
 * simple, anchored, and safe for typical inputs. For untrusted input
 * larger than ~10 KB, prefer {@link Rule} with a composed rule, which
 * also enforces length limits before regex evaluation.</p>
 *
 * @see Rule
 * @see Rules
 * @see Validate
 * @see Regex
 * @since 1.0.0
 */
public final class Check {

    /*
     * Practical (not full RFC 5322) email syntax, the single source of truth
     * for every email check in the library (Regex.email(), the EMAIL rule,
     * Validate, UI fields and console readers all use it):
     *   - local part: 1-64 chars of RFC 5322 atext, dot-separated, with no
     *     leading, trailing or consecutive dots;
     *   - domain: two or more labels of letters, digits and hyphens, each
     *     1-63 chars and never starting or ending with a hyphen;
     *   - top-level domain: 2-63 letters, or a punycode label ("xn--...");
     *   - at most 254 characters overall.
     * Quoted local parts, IP-literal domains, single-label hosts and
     * non-ASCII (unencoded IDN) addresses are rejected.
     *
     * Possessive quantifiers ("++" / "*+") are load-bearing: they prevent
     * the engine from exploring alternate ways to split a run of atext
     * characters, so the total work stays linear in the input length.
     */
    private static final String EMAIL_ATOM = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]";
    private static final String EMAIL_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    private static final String EMAIL_TLD = "(?:[A-Za-z]{2,63}|xn--[A-Za-z0-9](?:[A-Za-z0-9-]{0,57}[A-Za-z0-9])?)";
    private static final String EMAIL_REGEX = "^(?=.{1,254}\\z)(?=.{1,64}@)"
            + EMAIL_ATOM + "++"
            + "(?:\\." + EMAIL_ATOM + "++)*+"
            + "@(?:" + EMAIL_LABEL + "\\.)++"
            + EMAIL_TLD + "\\z";
    private static final Pattern EMAIL = Pattern.compile(EMAIL_REGEX);

    private Check() {}

    /**
     * The email regex shared with {@link Regex#email()} and the
     * {@code EMAIL} rule. Package-private so other classes in this package
     * can reuse the same pattern without exposing it as public API.
     *
     * @return the compiled-safe email regex as a string
     * @see #isEmail(String)
     * @see Regex#email()
     * @since 1.0.0
     */
    static String emailPattern() { return EMAIL_REGEX; }

    // ============================================================== null / blank

    /**
     * Checks whether the given object is {@code null}.
     *
     * @param o the object to test (may be null)
     * @return {@code true} if {@code o} is {@code null}, {@code false} otherwise
     * @see #notNull(Object)
     * @since 1.0.0
     */
    public static boolean isNull(Object o) { return o == null; }

    /**
     * Checks whether the given object is not {@code null}.
     *
     * @param o the object to test (may be null)
     * @return {@code true} if {@code o} is not {@code null}, {@code false} otherwise
     * @see #isNull(Object)
     * @since 1.0.0
     */
    public static boolean notNull(Object o) { return o != null; }

    /**
     * Checks whether the given string is {@code null}, empty, or contains
     * only whitespace.
     *
     * <p>This is the stricter version of {@link #isEmpty(String)} — a string
     * of spaces or tabs is blank but not empty.</p>
     *
     * <pre>{@code
     * Check.isBlank(null);      // true
     * Check.isBlank("");        // true
     * Check.isBlank("   ");     // true
     * Check.isBlank(" hello "); // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is {@code null}, empty, or
     * whitespace-only; {@code false} otherwise
     * @see #isEmpty(String)
     * @see #notBlank(String)
     * @since 1.0.0
     */
    public static boolean isBlank(String s) { return s == null || s.isBlank(); }

    /**
     * Checks whether the given string is not blank — i.e. contains at
     * least one non-whitespace character.
     *
     * <p>Alias of {@code !isBlank(s)} — see {@link #isBlank(String)} for the
     * definition of "blank".</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} contains at least one non-whitespace
     * character; {@code false} otherwise (including when {@code s} is null)
     * @see #isBlank(String)
     * @since 1.0.0
     */
    public static boolean notBlank(String s) { return !isBlank(s); }

    /**
     * Checks whether the given string is {@code null} or has zero length.
     *
     * <p>Unlike {@link #isBlank(String)}, a string of spaces is <b>not</b>
     * empty. Use this method when you specifically need to distinguish
     * "no characters at all" from "only whitespace".</p>
     *
     * <pre>{@code
     * Check.isEmpty(null);  // true
     * Check.isEmpty("");    // true
     * Check.isEmpty("   "); // false — blank but not empty
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is {@code null} or has length 0;
     * {@code false} otherwise
     * @see #isBlank(String)
     * @since 1.0.0
     */
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }

    /**
     * Checks whether the given string has at least one character.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and has length &gt; 0;
     * {@code false} otherwise
     * @see #isEmpty(String)
     * @since 1.0.0
     */
    public static boolean notEmpty(String s) { return !isEmpty(s); }

    /**
     * Checks whether the given string has at least one character.
     *
     * <p>Alias of {@link #notEmpty(String)}.</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and non-empty;
     * {@code false} otherwise
     * @see #notEmpty(String)
     * @since 1.0.0
     */
    public static boolean hasValue(String s) { return !isEmpty(s); }

    /**
     * Checks whether the given string has at least one character.
     *
     * <p>Alias of {@link #notEmpty(String)}.</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and non-empty;
     * {@code false} otherwise
     * @see #notEmpty(String)
     * @since 1.0.0
     */
    public static boolean isPresent(String s) { return !isEmpty(s); }

    // ============================================================== format

    /**
     * Checks whether the given string is a valid email address.
     *
     * <p>Uses a practical RFC 5322 subset — the addresses humans actually
     * type — rather than the full grammar. Quoted local parts,
     * IP-literal domains, and single-label hosts are rejected.</p>
     *
     * <pre>{@code
     * Check.isEmail("user@example.com");    // true
     * Check.isEmail("a.b+c@x.co");          // true
     * Check.isEmail("saad@umt.edu.pk");     // true
     * Check.isEmail("user@");               // false
     * Check.isEmail("a@b");                 // false
     * Check.isEmail(null);                  // false
     * }</pre>
     *
     * <p>See {@link Regex#email()} for the raw pattern and
     * {@link #emailPattern()} for the details of what it accepts.</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid email; {@code false}
     * otherwise (including when {@code s} is null)
     * @see #looksLikeEmail(String)
     * @see #validEmail(String)
     * @see #isEmailAddress(String)
     * @see Regex#email()
     * @see Rules#email()
     * @since 1.0.0
     */
    public static boolean isEmail(String s) { return s != null && EMAIL.matcher(s).matches(); }

    /**
     * Alias of {@link #isEmail(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid email; {@code false} otherwise
     * @see #isEmail(String)
     * @since 1.0.0
     */
    public static boolean looksLikeEmail(String s) { return isEmail(s); }

    /**
     * Alias of {@link #isEmail(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid email; {@code false} otherwise
     * @see #isEmail(String)
     * @since 1.0.0
     */
    public static boolean validEmail(String s) { return isEmail(s); }

    /**
     * Alias of {@link #isEmail(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid email; {@code false} otherwise
     * @see #isEmail(String)
     * @since 1.0.0
     */
    public static boolean isEmailAddress(String s) { return isEmail(s); }

    /**
     * Checks whether the given string is a valid international phone number
     * in E.164 style — 7 to 15 digits, optional leading {@code +}.
     *
     * <p>Whitespace, hyphens, and parentheses are <b>not</b> stripped before
     * validation. If your input may contain them (e.g. {@code "0300-1234567"}),
     * strip them first or use {@link #isPakistaniPhone(String)}, which
     * normalises before validating.</p>
     *
     * <pre>{@code
     * Check.isPhone("+9234567890");  // true
     * Check.isPhone("03001234567");  // true
     * Check.isPhone("0300-1234567"); // false — contains hyphen
     * Check.isPhone("123");          // false — too short
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid international phone
     * number; {@code false} otherwise
     * @see #isPhoneNumber(String)
     * @see #validPhone(String)
     * @see #isPakistaniPhone(String)
     * @see Regex#phone()
     * @since 1.0.0
     */
    public static boolean isPhone(String s) { return s != null && s.matches("^\\+?[0-9]{7,15}$"); }

    /**
     * Alias of {@link #isPhone(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid international phone
     * number; {@code false} otherwise
     * @see #isPhone(String)
     * @since 1.0.0
     */
    public static boolean isPhoneNumber(String s) { return isPhone(s); }

    /**
     * Alias of {@link #isPhone(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid international phone
     * number; {@code false} otherwise
     * @see #isPhone(String)
     * @since 1.0.0
     */
    public static boolean validPhone(String s) { return isPhone(s); }

    /**
     * Checks whether the given string contains only ASCII letters
     * ({@code A–Z}, {@code a–z}).
     *
     * <pre>{@code
     * Check.isAlpha("hello");  // true
     * Check.isAlpha("Hello");  // true
     * Check.isAlpha("hi 42");  // false — contains space and digits
     * Check.isAlpha("");       // false — no letters
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and consists only of
     * ASCII letters; {@code false} otherwise
     * @see #isAlphabetic(String)
     * @see #isLettersOnly(String)
     * @see Regex#alpha()
     * @since 1.0.0
     */
    public static boolean isAlpha(String s) { return s != null && s.matches("[A-Za-z]+"); }

    /**
     * Alias of {@link #isAlpha(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} consists only of ASCII letters;
     * {@code false} otherwise
     * @see #isAlpha(String)
     * @since 1.0.0
     */
    public static boolean isAlphabetic(String s) { return isAlpha(s); }

    /**
     * Alias of {@link #isAlpha(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} consists only of ASCII letters;
     * {@code false} otherwise
     * @see #isAlpha(String)
     * @since 1.0.0
     */
    public static boolean isLettersOnly(String s) { return isAlpha(s); }

    /**
     * Checks whether the given string contains only ASCII digits
     * ({@code 0–9}).
     *
     * <pre>{@code
     * Check.isDigits("123");   // true
     * Check.isDigits("12a");   // false
     * Check.isDigits("12 34"); // false — contains space
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and consists only of
     * digits; {@code false} otherwise
     * @see #isDigitsOnly(String)
     * @see Regex#digits()
     * @since 1.0.0
     */
    public static boolean isDigits(String s) { return s != null && s.matches("\\d+"); }

    /**
     * Alias of {@link #isDigits(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} consists only of digits;
     * {@code false} otherwise
     * @see #isDigits(String)
     * @since 1.0.0
     */
    public static boolean isDigitsOnly(String s) { return isDigits(s); }

    /**
     * Checks whether the given string contains only ASCII letters and
     * digits ({@code A–Z}, {@code a–z}, {@code 0–9}).
     *
     * <pre>{@code
     * Check.isAlphanumeric("abc123");  // true
     * Check.isAlphanumeric("abc 123"); // false — contains space
     * Check.isAlphanumeric("abc-123"); // false — contains hyphen
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-null and consists only of
     * letters and digits; {@code false} otherwise
     * @see #isAlphanum(String)
     * @see Regex#alphanumeric()
     * @since 1.0.0
     */
    public static boolean isAlphanumeric(String s) { return s != null && s.matches("[A-Za-z0-9]+"); }

    /**
     * Alias of {@link #isAlphanumeric(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} consists only of letters and
     * digits; {@code false} otherwise
     * @see #isAlphanumeric(String)
     * @since 1.0.0
     */
    public static boolean isAlphanum(String s) { return isAlphanumeric(s); }

    /**
     * Checks whether the given string is a {@code #RRGGBB} hex colour.
     *
     * <p>Accepts both lowercase ({@code #ff00aa}) and uppercase
     * ({@code #FF00AA}) hex digits. Does <b>not</b> accept the
     * three-character short form ({@code #F0A}) — use
     * {@link Regex#hexColorShort()} for that.</p>
     *
     * <pre>{@code
     * Check.isHexColor("#FF00AA"); // true
     * Check.isHexColor("#ff00aa"); // true
     * Check.isHexColor("FF00AA");  // false — missing '#'
     * Check.isHexColor("#F0A");    // false — 3-digit form not accepted
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid six-digit hex colour;
     * {@code false} otherwise
     * @see #isHex(String)
     * @see Regex#hexColor()
     * @since 1.0.0
     */
    public static boolean isHexColor(String s) { return s != null && s.matches("#[0-9A-Fa-f]{6}"); }

    /**
     * Alias of {@link #isHexColor(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid six-digit hex colour;
     * {@code false} otherwise
     * @see #isHexColor(String)
     * @since 1.0.0
     */
    public static boolean isHex(String s) { return isHexColor(s); }

    /**
     * Checks whether the given string can be parsed as a
     * {@code double} — i.e. is a valid number, integer or decimal.
     *
     * <p>This delegates to {@link Double#parseDouble(String)} on the
     * trimmed input. It accepts {@code "42"}, {@code "3.14"},
     * {@code "1e10"}, and {@code "-0.5"}. It rejects {@code "42abc"},
     * {@code "  "} (after trim, empty), and {@code "NaN"} — well, actually
     * {@code Double.parseDouble} accepts {@code "NaN"} and {@code "Infinity"},
     * which most callers do <b>not</b> want. Use {@link #isInteger(String)}
     * if you need an integer or a dedicated parser if you need to exclude
     * NaN/Infinity.</p>
     *
     * <pre>{@code
     * Check.isNumeric("42");    // true
     * Check.isNumeric("3.14");  // true
     * Check.isNumeric("-0.5");  // true
     * Check.isNumeric("42abc"); // false
     * Check.isNumeric(null);    // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as a {@code double};
     * {@code false} otherwise
     * @see #isNumber(String)
     * @see #isDecimal(String)
     * @see #isInteger(String)
     * @see Regex#numeric()
     * @since 1.0.0
     */
    public static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #isNumeric(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as a {@code double};
     * {@code false} otherwise
     * @see #isNumeric(String)
     * @since 1.0.0
     */
    public static boolean isNumber(String s) { return isNumeric(s); }

    /**
     * Alias of {@link #isNumeric(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as a {@code double};
     * {@code false} otherwise
     * @see #isNumeric(String)
     * @since 1.0.0
     */
    public static boolean isDecimal(String s) { return isNumeric(s); }

    /**
     * Checks whether the given string can be parsed as an {@code int}.
     *
     * <p>Unlike {@link #isNumeric(String)}, this rejects decimals
     * ({@code "3.14"}) and scientific notation ({@code "1e10"}), and
     * accepts negatives.</p>
     *
     * <pre>{@code
     * Check.isInteger("42");    // true
     * Check.isInteger("-7");    // true
     * Check.isInteger("3.14");  // false
     * Check.isInteger("42abc"); // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as an {@code int};
     * {@code false} otherwise
     * @see #isInt(String)
     * @see #isNumeric(String)
     * @since 1.0.0
     */
    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #isInteger(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as an {@code int};
     * {@code false} otherwise
     * @see #isInteger(String)
     * @since 1.0.0
     */
    public static boolean isInt(String s) { return isInteger(s); }

    /**
     * Checks whether the given string is a valid URL with an
     * {@code http}, {@code https}, or {@code ftp} scheme.
     *
     * <p>Accepts query strings and paths, but <b>not</b> bare hostnames
     * ({@code "example.com"}) — the scheme is required. See
     * {@link Regex#url()} for the exact pattern.</p>
     *
     * <pre>{@code
     * Check.isURL("https://example.com");           // true
     * Check.isURL("http://test.org/path");          // true
     * Check.isURL("ftp://files.example.com/data");  // true
     * Check.isURL("example.com");                   // false — no scheme
     * Check.isURL("not-a-url");                     // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid URL; {@code false} otherwise
     * @see #isUrl(String)
     * @see #isWebURL(String)
     * @see #isLink(String)
     * @see Regex#url()
     * @since 1.0.0
     */
    public static boolean isURL(String s) {
        return s != null && s.matches("^(https?|ftp)://[\\w.-]+(:\\d+)?(/.*)?$");
    }

    /**
     * Alias of {@link #isURL(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid URL; {@code false} otherwise
     * @see #isURL(String)
     * @since 1.0.0
     */
    public static boolean isUrl(String s) { return isURL(s); }

    /**
     * Alias of {@link #isURL(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid URL; {@code false} otherwise
     * @see #isURL(String)
     * @since 1.0.0
     */
    public static boolean isWebURL(String s) { return isURL(s); }

    /**
     * Alias of {@link #isURL(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid URL; {@code false} otherwise
     * @see #isURL(String)
     * @since 1.0.0
     */
    public static boolean isLink(String s) { return isURL(s); }

    /**
     * Checks whether the given string is a valid IPv4 address.
     *
     * <p>Each of the four octets must be 0–255. Does not accept IPv6
     * addresses — see {@link Regex#ipv6()} if you need those.</p>
     *
     * <pre>{@code
     * Check.isIPAddress("192.168.1.1"); // true
     * Check.isIPAddress("0.0.0.0");     // true
     * Check.isIPAddress("999.1.1.1");   // false — octet > 255
     * Check.isIPAddress("::1");         // false — IPv6
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid IPv4 address;
     * {@code false} otherwise
     * @see #isIP(String)
     * @see #isIPv4(String)
     * @see Regex#ipAddress()
     * @since 1.0.0
     */
    public static boolean isIPAddress(String s) {
        if (s == null) return false;
        return s.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    }

    /**
     * Alias of {@link #isIPAddress(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid IPv4 address;
     * {@code false} otherwise
     * @see #isIPAddress(String)
     * @since 1.0.0
     */
    public static boolean isIP(String s) { return isIPAddress(s); }

    /**
     * Alias of {@link #isIPAddress(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid IPv4 address;
     * {@code false} otherwise
     * @see #isIPAddress(String)
     * @since 1.0.0
     */
    public static boolean isIPv4(String s) { return isIPAddress(s); }

    /**
     * Checks whether the given string is a canonical UUID in the
     * {@code 8-4-4-4-12} hex format.
     *
     * <pre>{@code
     * Check.isUUID("550e8400-e29b-41d4-a716-446655440000"); // true
     * Check.isUUID("not-a-uuid");                           // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid UUID; {@code false} otherwise
     * @see #isUuid(String)
     * @see Regex#uuid()
     * @since 1.0.0
     */
    public static boolean isUUID(String s) {
        return s != null && s.matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    /**
     * Alias of {@link #isUUID(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid UUID; {@code false} otherwise
     * @see #isUUID(String)
     * @since 1.0.0
     */
    public static boolean isUuid(String s) { return isUUID(s); }

    /**
     * Checks whether the given string is a valid username — 3 to 20
     * characters of ASCII letters, digits, or underscores.
     *
     * <pre>{@code
     * Check.isUsername("saad_mansoor");   // true
     * Check.isUsername("ab");              // false — too short
     * Check.isUsername("user name");       // false — contains space
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid username; {@code false} otherwise
     * @see Regex#username()
     * @see Rules#username()
     * @since 1.0.0
     */
    public static boolean isUsername(String s) {
        return s != null && s.matches("^[A-Za-z0-9_]{3,20}$");
    }

    /**
     * Checks whether the given string can be parsed as a date using the
     * supplied {@link java.time.format.DateTimeFormatter} pattern.
     *
     * <p>Only validates that the string matches the pattern's syntax —
     * it does <b>not</b> verify calendar validity. For example,
     * {@code "2026-02-30"} matches the pattern {@code "yyyy-MM-dd"} and
     * will return {@code true}, even though February 30 does not exist.
     * Use {@link chirp.datetime.DateTime#parseDate(String)} for full
     * validation (it returns {@code null} on unparseable or invalid
     * dates).</p>
     *
     * @param s the string to test (may be null)
     * @param pattern the {@link java.time.format.DateTimeFormatter} pattern
     * (may be null — returns {@code false})
     * @return {@code true} if {@code s} matches the pattern; {@code false}
     * otherwise
     * @see chirp.datetime.DateTime#parseDate(String)
     * @see chirp.datetime.DateTime#parseDate(String, String)
     * @since 1.0.0
     */
    public static boolean isDate(String s, String pattern) {
        if (s == null || pattern == null) return false;
        try {
            java.time.format.DateTimeFormatter.ofPattern(pattern).parse(s);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Checks whether the given string is valid JSON.
     *
     * <p>Delegates to {@link chirp.json.Json#parse(String)} — anything that
     * parser accepts (objects, arrays, primitives, {@code null}) is valid.</p>
     *
     * <pre>{@code
     * Check.isJSON("{\"a\":1}");    // true
     * Check.isJSON("[1,2,3]");      // true
     * Check.isJSON("not json");     // false
     * Check.isJSON(null);           // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as JSON; {@code false} otherwise
     * @see #isJson(String)
     * @see #validJson(String)
     * @see chirp.json.Json#parse(String)
     * @since 1.0.0
     */
    public static boolean isJSON(String s) {
        if (s == null) return false;
        try {
            chirp.json.Json.parse(s);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #isJSON(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as JSON; {@code false} otherwise
     * @see #isJSON(String)
     * @since 1.0.0
     */
    public static boolean isJson(String s) { return isJSON(s); }

    /**
     * Alias of {@link #isJSON(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} parses as JSON; {@code false} otherwise
     * @see #isJSON(String)
     * @since 1.0.0
     */
    public static boolean validJson(String s) { return isJSON(s); }

    // ============================================================== content

    /**
     * Checks whether the given string contains at least one digit
     * (any script — {@link Character#isDigit(char)}).
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} contains a digit; {@code false} otherwise
     * @see #containsLetter(String)
     * @since 1.0.0
     */
    public static boolean containsDigit(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) if (Character.isDigit(c)) return true;
        return false;
    }

    /**
     * Checks whether the given string contains at least one letter
     * (any script — {@link Character#isLetter(char)}).
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} contains a letter; {@code false} otherwise
     * @see #containsDigit(String)
     * @since 1.0.0
     */
    public static boolean containsLetter(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c)) return true;
        return false;
    }

    /**
     * Checks whether the given string contains at least one character
     * that is neither a letter, a digit, nor whitespace.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} contains a special character;
     * {@code false} otherwise
     * @see #isStrongPassword(String)
     * @since 1.0.0
     */
    public static boolean containsSpecialChar(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) return true;
        }
        return false;
    }

    /**
     * Checks whether the given string starts with a digit.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-empty and starts with a
     * digit; {@code false} otherwise
     * @see #endsWithDigit(String)
     * @since 1.0.0
     */
    public static boolean startsWithDigit(String s) {
        return s != null && !s.isEmpty() && Character.isDigit(s.charAt(0));
    }

    /**
     * Checks whether the given string ends with a digit.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is non-empty and ends with a
     * digit; {@code false} otherwise
     * @see #startsWithDigit(String)
     * @since 1.0.0
     */
    public static boolean endsWithDigit(String s) {
        return s != null && !s.isEmpty() && Character.isDigit(s.charAt(s.length() - 1));
    }

    // ============================================================== Pakistan

    /**
     * Checks whether the given string is a valid Pakistani mobile number.
     *
     * <p>Accepts the common formats — {@code 0300-1234567},
     * {@code 0300 1234567}, {@code +92 300 1234567},
     * {@code +923001234567}, {@code 00923001234567}, and
     * {@code 923001234567} — by stripping separators before matching.
     * The operator prefix must be 3xx where xx is 00–99 but the leading
     * digit after 3 is 0–6, so government/reserved ranges are rejected.</p>
     *
     * <p>Delegates to {@link Rules#pakistaniMobile()}. For full control
     * over the message or the normaliser, construct the {@link Rule}
     * yourself with {@link Rule#define(String)}.</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid Pakistani mobile
     * number; {@code false} otherwise
     * @see #isPakistaniCnic(String)
     * @see Rules#pakistaniMobile()
     * @see Regex#pakistaniMobile()
     * @since 1.0.0
     */
    public static boolean isPakistaniPhone(String s) { return Rules.pakistaniMobile().test(s); }

    /**
     * Checks whether the given string is a valid Pakistani CNIC
     * (Computerised National Identity Card number).
     *
     * <p>Validates the 13-digit CNIC — with or without separators.
     * Accepted forms: {@code 35202-1234567-1}, {@code 3520212345671},
     * {@code "35202 1234567 1"}. The first digit must be 1–8 (see
     * {@link Rules#cnicProvince(String)} for the province encoding).</p>
     *
     * <p>Delegates to {@link Rules#pakistaniCnic()}.</p>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} is a valid Pakistani CNIC;
     * {@code false} otherwise
     * @see #isPakistaniPhone(String)
     * @see Rules#pakistaniCnic()
     * @see Regex#pakistaniCnic()
     * @since 1.0.0
     */
    public static boolean isPakistaniCnic(String s) { return Rules.pakistaniCnic().test(s); }

    // ============================================================== rules

    /**
     * Applies the named rule from the {@link Rules} registry.
     *
     * <p>Returns {@code false} if the rule is not registered. For a version
     * that throws on unknown names, use {@link Rules#require(String)}.</p>
     *
     * <pre>{@code
     * Check.is("0300-1234567", "PK_MOBILE");  // true
     * Check.is("user@example.com", "EMAIL");  // true
     * Check.is("bogus", "PK_CNIC");           // false
     * Check.is("bogus", "DOES_NOT_EXIST");    // false
     * }</pre>
     *
     * @param value the value to test (may be null)
     * @param ruleName the rule's registry name (may be null)
     * @return {@code true} if the rule exists and passes; {@code false} otherwise
     * @see #is(String, Rule)
     * @see Rules#get(String)
     * @see Rules#test(String, String)
     * @since 1.0.0
     */
    public static boolean is(String value, String ruleName) {
        Rule rule = Rules.get(ruleName);
        return rule != null && rule.test(value);
    }

    /**
     * Applies a {@link Rule} object directly.
     *
     * @param value the value to test (may be null)
     * @param rule the rule to apply (may be null — returns {@code false})
     * @return {@code true} if the rule passes; {@code false} otherwise
     * @see #is(String, String)
     * @see Rule#test(String)
     * @since 1.0.0
     */
    public static boolean is(String value, Rule rule) {
        return rule != null && rule.test(value);
    }

    // ============================================================== range

    /**
     * Checks whether the given integer lies in the inclusive range
     * {@code [min, max]}.
     *
     * <pre>{@code
     * Check.inRange(50, 0, 100);    // true
     * Check.inRange(0, 0, 100);     // true
     * Check.inRange(100, 0, 100);   // true
     * Check.inRange(101, 0, 100);   // false
     * Check.inRange(-1, 0, 100);    // false
     * }</pre>
     *
     * @param n the value to test
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return {@code true} if {@code min <= n <= max}; {@code false} otherwise
     * @see #inRange(double, double, double)
     * @see #between(int, int, int)
     * @since 1.0.0
     */
    public static boolean inRange(int n, int min, int max) { return n >= min && n <= max; }

    /**
     * Checks whether the given {@code double} lies in the inclusive range
     * {@code [min, max]}.
     *
     * @param n the value to test
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return {@code true} if {@code min <= n <= max}; {@code false} otherwise
     * @see #inRange(int, int, int)
     * @since 1.0.0
     */
    public static boolean inRange(double n, double min, double max) { return n >= min && n <= max; }

    /**
     * Alias of {@link #inRange(int, int, int)} — see that method for full
     * documentation and examples.
     *
     * @param n the value to test
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return {@code true} if {@code min <= n <= max}; {@code false} otherwise
     * @see #inRange(int, int, int)
     * @since 1.0.0
     */
    public static boolean between(int n, int min, int max) { return inRange(n, min, max); }

    // ============================================================== length

    /**
     * Checks whether the given string's length is at least {@code len}.
     *
     * @param s the string to test (may be null — returns {@code false})
     * @param len the minimum length
     * @return {@code true} if {@code s} has at least {@code len} characters;
     * {@code false} otherwise
     * @see #maxLength(String, int)
     * @see #lengthBetween(String, int, int)
     * @since 1.0.0
     */
    public static boolean minLength(String s, int len) { return s != null && s.length() >= len; }

    /**
     * Checks whether the given string's length is at most {@code len}.
     *
     * @param s the string to test (may be null — returns {@code false})
     * @param len the maximum length
     * @return {@code true} if {@code s} has at most {@code len} characters;
     * {@code false} otherwise
     * @see #minLength(String, int)
     * @see #lengthBetween(String, int, int)
     * @since 1.0.0
     */
    public static boolean maxLength(String s, int len) { return s != null && s.length() <= len; }

    /**
     * Checks whether the given string's length is exactly {@code len}.
     *
     * @param s the string to test (may be null — returns {@code false})
     * @param len the exact length
     * @return {@code true} if {@code s} has exactly {@code len} characters;
     * {@code false} otherwise
     * @since 1.0.0
     */
    public static boolean hasLength(String s, int len) { return s != null && s.length() == len; }

    /**
     * Checks whether the given string's length lies in the inclusive range
     * {@code [min, max]}.
     *
     * @param s the string to test (may be null — returns {@code false})
     * @param min the minimum length (inclusive)
     * @param max the maximum length (inclusive)
     * @return {@code true} if the length is in range; {@code false} otherwise
     * @see #minLength(String, int)
     * @see #maxLength(String, int)
     * @since 1.0.0
     */
    public static boolean lengthBetween(String s, int min, int max) {
        return s != null && s.length() >= min && s.length() <= max;
    }

    // ============================================================== password

    /**
     * Checks whether the given string is a "strong" password — at least
     * 8 characters, containing an uppercase letter, a lowercase letter,
     * and a digit.
     *
     * <p>This is a baseline policy for demos and exercises, not a
     * security-grade check. It does not look for common passwords, does
     * not consult a dictionary, and does not enforce a complexity entropy
     * threshold. For production, use a dedicated password strength
     * library (e.g. zxcvbn).</p>
     *
     * <pre>{@code
     * Check.isStrongPassword("Abc12345");  // true
     * Check.isStrongPassword("weak");      // false — no upper, no digit
     * Check.isStrongPassword("Abcdefgh");  // false — no digit
     * Check.isStrongPassword(null);        // false
     * }</pre>
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} meets the policy; {@code false} otherwise
     * @see #isVeryStrongPassword(String)
     * @see Rules#strongPassword()
     * @since 1.0.0
     */
    public static boolean isStrongPassword(String s) {
        return s != null && s.length() >= 8
                && s.matches(".*[A-Z].*")
                && s.matches(".*[a-z].*")
                && s.matches(".*[0-9].*");
    }

    /**
     * Deprecated overload — the boolean parameter is ignored. Use
     * {@link #isStrongPassword(String)} instead.
     *
     * <p>This overload is kept for binary compatibility only. It was an
     * early API that took an unused flag. It will be removed in a future
     * major version.</p>
     *
     * @param s the string to test (may be null)
     * @param ignored unused — exists only to distinguish the overload
     * @return {@code true} if {@code s} meets the policy; {@code false} otherwise
     * @see #isStrongPassword(String)
     * @since 1.0.0
     * @deprecated use {@link #isStrongPassword(String)} — this overload
     * adds nothing and will be removed in 2.0
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static boolean isStrongPassword(String s, boolean ignored) { return isStrongPassword(s); }

    /**
     * Checks whether the given string is a "very strong" password —
     * everything {@link #isStrongPassword(String)} requires, plus at least
     * one non-alphanumeric character.
     *
     * @param s the string to test (may be null)
     * @return {@code true} if {@code s} meets the stricter policy;
     * {@code false} otherwise
     * @see #isStrongPassword(String)
     * @since 1.0.0
     */
    public static boolean isVeryStrongPassword(String s) {
        return isStrongPassword(s) && s.matches(".*[^A-Za-z0-9].*");
    }

    // ============================================================== domain

    /**
     * Checks whether the given age is that of an adult (18 or older).
     *
     * @param age the age to test
     * @return {@code true} if {@code age >= 18}; {@code false} otherwise
     * @see #isValidGrade(int)
     * @since 1.0.0
     */
    public static boolean isAdult(int age) { return age >= 18; }

    /**
     * Checks whether the given grade is in the valid range 0–100
     * (inclusive).
     *
     * @param grade the grade to test
     * @return {@code true} if {@code 0 <= grade <= 100}; {@code false} otherwise
     * @see #inRange(int, int, int)
     * @see #isValidSemester(int)
     * @since 1.0.0
     */
    public static boolean isValidGrade(int grade) { return inRange(grade, 0, 100); }

    /**
     * Checks whether the given semester number is valid — 1 through 8
     * (inclusive), matching the standard eight-semester degree structure.
     *
     * @param sem the semester number to test
     * @return {@code true} if {@code 1 <= sem <= 8}; {@code false} otherwise
     * @see #inRange(int, int, int)
     * @since 1.0.0
     */
    public static boolean isValidSemester(int sem) { return inRange(sem, 1, 8); }

    /**
     * Checks whether the given year is a leap year in the Gregorian
     * calendar.
     *
     * <p>The rule is: divisible by 4, and divisible by 400 unless also
     * divisible by 100. So 1900 is <b>not</b> a leap year, but 2000 is.</p>
     *
     * <pre>{@code
     * Check.isLeapYear(2024); // true
     * Check.isLeapYear(2023); // false
     * Check.isLeapYear(1900); // false
     * Check.isLeapYear(2000); // true
     * }</pre>
     *
     * @param year the year to test
     * @return {@code true} if {@code year} is a leap year; {@code false} otherwise
     * @see chirp.datetime.DateTime#isLeapYear(int)
     * @since 1.0.0
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // ============================================================== anagram

    /**
     * Checks whether the two strings are anagrams — same letters, same
     * counts, ignoring case and non-alphanumeric characters.
     *
     * <p>Delegates to {@link Text#isAnagram(String, String)}.</p>
     *
     * <pre>{@code
     * Check.isAnagram("listen", "silent");  // true
     * Check.isAnagram("Hello", "World");    // false
     * Check.isAnagram("A man", "a plan");   // false — different letters
     * }</pre>
     *
     * @param a the first string (may be null — returns {@code false})
     * @param b the second string (may be null — returns {@code false})
     * @return {@code true} if the strings are anagrams; {@code false} otherwise
     * @see Text#isAnagram(String, String)
     * @since 1.0.0
     */
    public static boolean isAnagram(String a, String b) { return Text.isAnagram(a, b); }

    // ============================================================== weekend

    /**
     * Checks whether the given date falls on a weekend under the default
     * weekend definition (Saturday and Sunday).
     *
     * <p>Delegates to {@link chirp.datetime.DateTime#isWeekend(java.time.LocalDate)}.
     * For custom weekend definitions (e.g. Friday–Saturday in many
     * Middle Eastern countries), use
     * {@link chirp.datetime.DateTime#isWeekend(java.time.LocalDate, java.time.DayOfWeek...)}
     * directly.</p>
     *
     * @param date the date to test (may be null — returns {@code false})
     * @return {@code true} if {@code date} is a Saturday or Sunday;
     * {@code false} otherwise
     * @see chirp.datetime.DateTime#isWeekend(java.time.LocalDate)
     * @see #isWeekday(java.time.LocalDate)
     * @since 1.0.0
     */
    public static boolean isWeekend(java.time.LocalDate date) {
        return chirp.datetime.DateTime.isWeekend(date);
    }

    /**
     * Checks whether the given date falls on a weekday under the default
     * weekend definition (Saturday and Sunday).
     *
     * <p>Delegates to {@link chirp.datetime.DateTime#isWeekday(java.time.LocalDate)}.
     * Equivalent to {@code !isWeekend(date)} for a non-null date, but the
     * null behaviour is different: {@code null} returns {@code false}
     * (not {@code true}) so that "unknown" is not mistaken for "weekday".</p>
     *
     * @param date the date to test (may be null — returns {@code false})
     * @return {@code true} if {@code date} is Monday–Friday;
     * {@code false} otherwise
     * @see chirp.datetime.DateTime#isWeekday(java.time.LocalDate)
     * @see #isWeekend(java.time.LocalDate)
     * @since 1.0.0
     */
    public static boolean isWeekday(java.time.LocalDate date) {
        return chirp.datetime.DateTime.isWeekday(date);
    }
}
