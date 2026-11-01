package chirp.text;

import chirp.validation.Rule;
import chirp.validation.Rules;

import java.util.regex.Pattern;

/**
 * Boolean validations. Every method returns {@code false} for {@code null}
 * and never throws.
 *
 * <h2>Aliases</h2>
 * <p>Where a natural synonym exists, multiple names delegate to the same
 * check. {@code isEmail} / {@code looksLikeEmail} / {@code validEmail} all
 * return the same boolean.</p>
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
     */
    private static final String EMAIL_ATOM = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+";
    private static final String EMAIL_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    private static final String EMAIL_TLD = "(?:[A-Za-z]{2,63}|xn--[A-Za-z0-9](?:[A-Za-z0-9-]{0,57}[A-Za-z0-9])?)";
    private static final String EMAIL_REGEX = "^(?=.{1,254}\\z)(?=.{1,64}@)"
            + EMAIL_ATOM + "(?:\\." + EMAIL_ATOM + ")*"
            + "@(?:" + EMAIL_LABEL + "\\.)+" + EMAIL_TLD + "\\z";
    private static final Pattern EMAIL = Pattern.compile(EMAIL_REGEX);

    private Check() {}

    /** The email regex shared with {@link Regex#email()}. */
    static String emailPattern() { return EMAIL_REGEX; }

    // ============================================================== null / blank

    public static boolean isNull(Object o) { return o == null; }
    public static boolean notNull(Object o) { return o != null; }
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    public static boolean notBlank(String s) { return !isBlank(s); }
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean notEmpty(String s) { return !isEmpty(s); }
    public static boolean hasValue(String s) { return !isEmpty(s); }
    public static boolean isPresent(String s) { return !isEmpty(s); }

    // ============================================================== format

    public static boolean isEmail(String s) { return s != null && EMAIL.matcher(s).matches(); }
    public static boolean looksLikeEmail(String s) { return isEmail(s); }
    public static boolean validEmail(String s) { return isEmail(s); }
    public static boolean isEmailAddress(String s) { return isEmail(s); }

    public static boolean isPhone(String s) { return s != null && s.matches("^\\+?[0-9]{7,15}$"); }
    public static boolean isPhoneNumber(String s) { return isPhone(s); }
    public static boolean validPhone(String s) { return isPhone(s); }

    public static boolean isAlpha(String s) { return s != null && s.matches("[A-Za-z]+"); }
    public static boolean isAlphabetic(String s) { return isAlpha(s); }
    public static boolean isLettersOnly(String s) { return isAlpha(s); }

    public static boolean isDigits(String s) { return s != null && s.matches("\\d+"); }
    public static boolean isDigitsOnly(String s) { return isDigits(s); }

    public static boolean isAlphanumeric(String s) { return s != null && s.matches("[A-Za-z0-9]+"); }
    public static boolean isAlphanum(String s) { return isAlphanumeric(s); }

    public static boolean isHexColor(String s) { return s != null && s.matches("#[0-9A-Fa-f]{6}"); }
    public static boolean isHex(String s) { return isHexColor(s); }

    public static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean isNumber(String s) { return isNumeric(s); }
    public static boolean isDecimal(String s) { return isNumeric(s); }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean isInt(String s) { return isInteger(s); }

    public static boolean isURL(String s) {
        return s != null && s.matches("^(https?|ftp)://[\\w.-]+(:\\d+)?(/.*)?$");
    }
    public static boolean isUrl(String s) { return isURL(s); }
    public static boolean isWebURL(String s) { return isURL(s); }
    public static boolean isLink(String s) { return isURL(s); }

    public static boolean isIPAddress(String s) {
        if (s == null) return false;
        return s.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    }
    public static boolean isIP(String s) { return isIPAddress(s); }
    public static boolean isIPv4(String s) { return isIPAddress(s); }

    public static boolean isUUID(String s) {
        return s != null && s.matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
    public static boolean isUuid(String s) { return isUUID(s); }

    public static boolean isUsername(String s) {
        return s != null && s.matches("^[A-Za-z0-9_]{3,20}$");
    }

    public static boolean isDate(String s, String pattern) {
        if (s == null || pattern == null) return false;
        try {
            java.time.format.DateTimeFormatter.ofPattern(pattern).parse(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJSON(String s) {
        if (s == null) return false;
        try {
            chirp.json.Json.parse(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean isJson(String s) { return isJSON(s); }
    public static boolean validJson(String s) { return isJSON(s); }

    // ============================================================== content

    public static boolean containsDigit(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) if (Character.isDigit(c)) return true;
        return false;
    }

    public static boolean containsLetter(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) if (Character.isLetter(c)) return true;
        return false;
    }

    public static boolean containsSpecialChar(String s) {
        if (s == null) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) return true;
        }
        return false;
    }

    public static boolean startsWithDigit(String s) {
        return s != null && !s.isEmpty() && Character.isDigit(s.charAt(0));
    }

    public static boolean endsWithDigit(String s) {
        return s != null && !s.isEmpty() && Character.isDigit(s.charAt(s.length() - 1));
    }

    // ============================================================== Pakistan

    public static boolean isPakistaniPhone(String s) { return Rules.pakistaniMobile().test(s); }
    public static boolean isPakistaniCnic(String s) { return Rules.pakistaniCnic().test(s); }

    // ============================================================== rules

    public static boolean is(String value, String ruleName) {
        Rule rule = Rules.get(ruleName);
        return rule != null && rule.test(value);
    }

    public static boolean is(String value, Rule rule) {
        return rule != null && rule.test(value);
    }

    // ============================================================== range

    public static boolean inRange(int n, int min, int max) { return n >= min && n <= max; }
    public static boolean inRange(double n, double min, double max) { return n >= min && n <= max; }
    public static boolean between(int n, int min, int max) { return inRange(n, min, max); }

    // ============================================================== length

    public static boolean minLength(String s, int len) { return s != null && s.length() >= len; }
    public static boolean maxLength(String s, int len) { return s != null && s.length() <= len; }
    public static boolean hasLength(String s, int len) { return s != null && s.length() == len; }
    public static boolean lengthBetween(String s, int min, int max) {
        return s != null && s.length() >= min && s.length() <= max;
    }

    // ============================================================== password

    public static boolean isStrongPassword(String s) {
        return s != null && s.length() >= 8
                && s.matches(".*[A-Z].*")
                && s.matches(".*[a-z].*")
                && s.matches(".*[0-9].*");
    }
    public static boolean isStrongPassword(String s, boolean ignored) { return isStrongPassword(s); }

    public static boolean isVeryStrongPassword(String s) {
        return isStrongPassword(s) && s.matches(".*[^A-Za-z0-9].*");
    }

    // ============================================================== domain

    public static boolean isAdult(int age) { return age >= 18; }
    public static boolean isValidGrade(int grade) { return inRange(grade, 0, 100); }
    public static boolean isValidSemester(int sem) { return inRange(sem, 1, 8); }
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // ============================================================== anagram

    public static boolean isAnagram(String a, String b) { return Text.isAnagram(a, b); }

    // ============================================================== weekend

    public static boolean isWeekend(java.time.LocalDate date) { return chirp.datetime.DateTime.isWeekend(date); }
    public static boolean isWeekday(java.time.LocalDate date) { return chirp.datetime.DateTime.isWeekday(date); }
}
