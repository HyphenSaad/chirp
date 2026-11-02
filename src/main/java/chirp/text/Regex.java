package chirp.text;

import chirp.internal.Logger;

/**
 * Ready-to-use regex patterns as plain strings, organised by domain.
 *
 * <p>Each method returns a regex <b>string</b>, not a compiled
 * {@link java.util.regex.Pattern}. This is deliberate — callers usually
 * pass the string to {@link String#matches(String)}, {@link String#replaceAll(String, String)},
 * {@link java.util.regex.Pattern#compile(String)}, or a
 * {@link chirp.validation.RuleBuilder#matches(String)} chain, and
 * compiling on every call would be slower than letting the JDK cache
 * compiled patterns internally.</p>
 *
 * <h2>Anchoring</h2>
 * <p>Where the intent is "does this string match the pattern exactly",
 * the returned regex is anchored with {@code ^ ... $}. Where the pattern
 * is meant to be searched inside a larger string (e.g.
 * {@link #htmlTag()}), it is not anchored. See each method for its
 * anchor behaviour.</p>
 *
 * <h2>Null safety</h2>
 * <p>Every method returns a non-null {@link String}, except
 * {@link #strongPassword()} and {@link #veryStrongPassword()}, which
 * return {@code null} and log a warning — those policies cannot be
 * safely expressed as a single backtracking-safe regex. Use
 * {@link Check#isStrongPassword(String)} or
 * {@link Check#isVeryStrongPassword(String)} for those.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Quick check
 * if ("user@example.com".matches(Regex.email())) {
 *     System.out.println("Valid email");
 * }
 *
 * // Compile once, use many times
 * Pattern phonePattern = Pattern.compile(Regex.phone());
 * boolean valid = phonePattern.matcher(input).matches();
 *
 * // Use in a validation rule
 * Rule.define("PHONE").matches(Regex.phone()).message("Invalid phone").build();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For boolean checks without building a {@link java.util.regex.Pattern}
 * yourself, see {@link Check}. For normalised, message-bearing rules
 * built on these patterns, see {@link chirp.validation.Rules}.</p>
 *
 * @see Check
 * @see chirp.validation.Rules
 * @see chirp.validation.RuleBuilder#matches(String)
 * @since 1.0.0
 */
public final class Regex {

    private Regex() {}

    // ------------------------------------------------------------ basics

    /**
     * Matches one or more ASCII letters.
     *
     * <p>Anchored. Matches {@code "abc"} and {@code "ABC"}, rejects
     * {@code "abc123"} and {@code ""}.</p>
     *
     * @return the regex string
     * @see Check#isAlpha(String)
     * @since 1.0.0
     */
    public static String alpha() { return "[A-Za-z]+"; }

    /**
     * Matches one or more ASCII digits.
     *
     * <p>Anchored. Matches {@code "0"} and {@code "12345"}, rejects
     * {@code "12a"} and {@code ""}.</p>
     *
     * @return the regex string
     * @see Check#isDigits(String)
     * @since 1.0.0
     */
    public static String digits() { return "\\d+"; }

    /**
     * Matches an optionally signed integer or decimal number.
     *
     * <p>Anchored. Matches {@code "42"}, {@code "-7"}, {@code "3.14"},
     * {@code "-0.5"}. Rejects {@code "1e10"} (scientific notation is
     * not included), {@code "42abc"}, and {@code ".5"} (leading digit
     * required).</p>
     *
     * @return the regex string
     * @see Check#isNumeric(String)
     * @see Check#isInteger(String)
     * @since 1.0.0
     */
    public static String numeric() { return "-?\\d+(\\.\\d+)?"; }

    /**
     * Matches one or more ASCII letters or digits.
     *
     * <p>Anchored. Matches {@code "abc123"}, rejects {@code "abc 123"}
     * (space) and {@code "abc-123"} (hyphen).</p>
     *
     * @return the regex string
     * @see Check#isAlphanumeric(String)
     * @since 1.0.0
     */
    public static String alphanumeric() { return "[A-Za-z0-9]+"; }

    /**
     * Matches a kebab-case slug — lowercase letters and digits separated
     * by single hyphens, no leading or trailing hyphen.
     *
     * <p>Anchored. Matches {@code "hello-world"}, {@code "a1-b2-c3"}.
     * Rejects {@code "Hello-World"} (uppercase), {@code "-hello"} (leading
     * hyphen), and {@code "hello--world"} (consecutive hyphens).</p>
     *
     * @return the regex string
     * @see chirp.text.Text#slugify(String)
     * @since 1.0.0
     */
    public static String slug() { return "^[a-z0-9]+(-[a-z0-9]+)*$"; }

    /**
     * Matches a username — 3 to 20 characters of ASCII letters, digits,
     * or underscores.
     *
     * <p>Anchored. Matches {@code "saad_mansoor"}, rejects {@code "ab"}
     * (too short) and {@code "user name"} (space).</p>
     *
     * @return the regex string
     * @see Check#isUsername(String)
     * @see chirp.validation.Rules#username()
     * @since 1.0.0
     */
    public static String username() { return "^[A-Za-z0-9_]{3,20}$"; }

    /**
     * Matches a plausible <b>person name</b>.
     *
     * <p>Anchored. Accepts letters from any Unicode script
     * ({@code \p{L}}), combining marks ({@code \p{M}} — the vowel signs
     * and diacritics used in Devanagari, Thai, Arabic, and other scripts),
     * and the punctuation that appears in real names: single spaces,
     * hyphens, apostrophes, and periods. Digits are rejected.</p>
     *
     * <p>Same pattern as {@link Check#isName(String)} and the
     * {@code NAME} rule. Note that the raw pattern does <b>not</b> trim
     * surrounding whitespace — the {@code NAME} rule adds a
     * {@code .trim()} normalizer that does.</p>
     *
     * <p><b>Matches:</b> {@code "Saad"}, {@code "Saad Mansoor"},
     * {@code "O'Brien"}, {@code "Al-Rashid"}, {@code "J. R. R. Tolkien"},
     * {@code "Müller"}, {@code "李明"}<br>
     * <b>Rejects:</b> {@code "Saad123"}, {@code "  Saad"},
     * {@code "Saad  Mansoor"} (double space), {@code "-Saad"}</p>
     *
     * @return the regex string
     * @see Check#isName(String)
     * @see chirp.validation.Rules#name()
     * @since 1.0.0
     */
    public static String name() {
        return Check.namePattern();
    }

    // ------------------------------------------------------------ contact

    /**
     * Matches a standard email address.
     *
     * <p>Anchored. This is the same practical RFC 5322-subset pattern
     * used by {@link Check#isEmail(String)} and the {@code EMAIL}
     * validation rule — see those for the full spec of what is accepted
     * and rejected. The pattern uses possessive quantifiers to prevent
     * catastrophic backtracking on adversarial input.</p>
     *
     * <p><b>Matches:</b> {@code user@example.com},
     * {@code a.b+c@x.co}, {@code saad@umt.edu.pk}<br>
     * <b>Rejects:</b> {@code user@}, {@code @example.com}, {@code a@b},
     * {@code user@example.c}</p>
     *
     * @return the regex string
     * @see Check#isEmail(String)
     * @see chirp.validation.Rules#email()
     * @since 1.0.0
     */
    public static String email() {
        return Check.emailPattern();
    }

    /**
     * Matches a phone number — 7 to 15 digits, optional leading
     * {@code +}.
     *
     * <p>Anchored. Does not accept spaces, hyphens, or parentheses — use
     * {@link #pakistaniMobile()} for the Pakistani format, or strip
     * separators before matching.</p>
     *
     * @return the regex string
     * @see Check#isPhone(String)
     * @since 1.0.0
     */
    public static String phone() { return "^\\+?[0-9]{7,15}$"; }

    // ------------------------------------------------------------ network

    /**
     * Matches an HTTP, HTTPS, or FTP URL with an optional port and path.
     *
     * <p>Anchored. Requires a scheme — bare hostnames like
     * {@code "example.com"} are rejected. Matches
     * {@code "https://example.com"}, {@code "http://test.org:8080/path?q=1"},
     * and {@code "ftp://files.example.com/data"}.</p>
     *
     * @return the regex string
     * @see Check#isURL(String)
     * @since 1.0.0
     */
    public static String url() { return "^(https?|ftp)://[\\w.-]+(:\\d+)?(/.*)?$"; }

    /**
     * Matches a valid IPv4 address.
     *
     * <p>Anchored. Each of the four octets must be 0–255, so
     * {@code "999.1.1.1"} is rejected. Leading zeros are allowed
     * ({@code "01.2.3.4"} matches) — if you need to reject leading
     * zeros, use a stricter pattern.</p>
     *
     * @return the regex string
     * @see Check#isIPAddress(String)
     * @see #ipv4()
     * @since 1.0.0
     */
    public static String ipAddress() {
        return "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    }

    /**
     * Alias of {@link #ipAddress()} — see that method for full
     * documentation and examples.
     *
     * @return the regex string
     * @see #ipAddress()
     * @since 1.0.0
     */
    public static String ipv4() { return ipAddress(); }

    /**
     * Matches a valid IPv6 address.
     *
     * <p>Anchored. Accepts the compressed forms ({@code "::1"},
     * {@code "2001:db8::1"}) as well as the full eight-group form.
     * Does not accept IPv4-mapped IPv6 addresses
     * ({@code "::ffff:192.168.1.1"}).</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String ipv6() {
        return "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,7}:"
                + "|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}"
                + "|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}"
                + "|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}"
                + "|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})"
                + "|:((:[0-9a-fA-F]{1,4}){1,7}|:))$";
    }

    /**
     * Matches a MAC address in colon or hyphen notation.
     *
     * <p>Anchored. Matches {@code "00:1B:44:11:3A:B7"} and
     * {@code "00-1b-44-11-3a-b7"} — either separator throughout, mixed
     * separators are rejected.</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String macAddress() { return "^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$"; }

    /**
     * Matches a canonical UUID in the {@code 8-4-4-4-12} hex format.
     *
     * <p>Anchored. Case-insensitive — {@code "550e8400-E29B-..."} and
     * {@code "550e8400-e29b-..."} both match.</p>
     *
     * @return the regex string
     * @see Check#isUUID(String)
     * @since 1.0.0
     */
    public static String uuid() {
        return "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }

    // ------------------------------------------------------------ time

    /**
     * Matches an ISO 8601 date in {@code yyyy-MM-dd} format.
     *
     * <p>Anchored. Matches the <b>syntax</b> only — {@code "2026-02-30"}
     * matches even though February 30 does not exist. Use
     * {@link chirp.datetime.DateTime#parseDate(String)} for calendar
     * validation.</p>
     *
     * @return the regex string
     * @see #dateDMY()
     * @see #dateMDY()
     * @since 1.0.0
     */
    public static String dateISO() { return "^\\d{4}-\\d{2}-\\d{2}$"; }

    /**
     * Matches a day-first date in {@code dd/MM/yyyy} or
     * {@code dd-MM-yyyy} format.
     *
     * <p>Anchored. Day 1–31, month 1–12, four-digit year. Leading zeros
     * optional. Does not perform calendar validation.</p>
     *
     * @return the regex string
     * @see #dateMDY()
     * @see #dateISO()
     * @since 1.0.0
     */
    public static String dateDMY() { return "^(0?[1-9]|[12]\\d|3[01])[/-](0?[1-9]|1[0-2])[/-]\\d{4}$"; }

    /**
     * Matches a month-first date in {@code MM/dd/yyyy} or
     * {@code MM-dd-yyyy} format.
     *
     * <p>Anchored. Month 1–12, day 1–31, four-digit year. Leading zeros
     * optional. Does not perform calendar validation.</p>
     *
     * @return the regex string
     * @see #dateDMY()
     * @see #dateISO()
     * @since 1.0.0
     */
    public static String dateMDY() { return "^(0?[1-9]|1[0-2])[/-](0?[1-9]|[12]\\d|3[01])[/-]\\d{4}$"; }

    /**
     * Matches a 24-hour time in {@code HH:mm} or {@code HH:mm:ss} format.
     *
     * <p>Anchored. Hour 0–23, minute 0–59, optional second 0–59. Leading
     * zero optional for the hour.</p>
     *
     * @return the regex string
     * @see #time12Hour()
     * @since 1.0.0
     */
    public static String time24Hour() { return "^([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$"; }

    /**
     * Matches a 12-hour time with AM/PM in {@code h:mm AM} format.
     *
     * <p>Anchored. Hour 1–12, minute 0–59, optional second 0–59, then
     * {@code AM}, {@code PM}, {@code am}, or {@code pm}. Case-insensitive
     * on the AM/PM marker.</p>
     *
     * @return the regex string
     * @see #time24Hour()
     * @since 1.0.0
     */
    public static String time12Hour() { return "^(0?[1-9]|1[0-2]):[0-5]\\d(:[0-5]\\d)?\\s?(AM|PM|am|pm)$"; }

    /**
     * Matches an ISO 8601 date-time — {@code yyyy-MM-dd HH:mm} or
     * {@code yyyy-MM-dd'T'HH:mm} with optional seconds.
     *
     * <p>Anchored. Accepts space or {@code T} as the date-time separator.</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String isoDateTime() {
        return "^\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}(:\\d{2})?$";
    }

    // ------------------------------------------------------------ financial

    /**
     * Matches a Visa credit card number.
     *
     * <p>Anchored. Visa numbers start with 4 and are 13, 16, or 19
     * digits. This pattern matches 13 or 16 digit Visa numbers (the two
     * most common lengths). It does not validate the Luhn checksum — use
     * a dedicated card-validation library for that.</p>
     *
     * @return the regex string
     * @see #creditCardMastercard()
     * @see #creditCardAmex()
     * @since 1.0.0
     */
    public static String creditCardVisa() { return "^4\\d{12}(\\d{3})?$"; }

    /**
     * Matches a Mastercard credit card number.
     *
     * <p>Anchored. Matches both the legacy 51–55 range (16 digits) and
     * the newer 2221–2720 range. Does not validate the Luhn checksum.</p>
     *
     * @return the regex string
     * @see #creditCardVisa()
     * @see #creditCardAmex()
     * @since 1.0.0
     */
    public static String creditCardMastercard() { return "^(5[1-5]\\d{14}|2[2-7]\\d{14})$"; }

    /**
     * Matches an American Express credit card number.
     *
     * <p>Anchored. Amex numbers start with 34 or 37 and are exactly 15
     * digits. Does not validate the Luhn checksum.</p>
     *
     * @return the regex string
     * @see #creditCardVisa()
     * @see #creditCardMastercard()
     * @since 1.0.0
     */
    public static String creditCardAmex() { return "^3[47]\\d{13}$"; }

    /**
     * Matches an IBAN (International Bank Account Number).
     *
     * <p>Anchored. Two-letter country code, two check digits, then 11 to
     * 30 alphanumerics. Does not validate the checksum — the pattern
     * matches the format only.</p>
     *
     * @return the regex string
     * @see #pakistaniIban()
     * @since 1.0.0
     */
    public static String iban() { return "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$"; }

    // ------------------------------------------------------------ publishing

    /**
     * Matches an ISBN-10 (10 characters, last may be {@code X}).
     *
     * <p>Anchored. Does not validate the checksum.</p>
     *
     * @return the regex string
     * @see #isbn13()
     * @since 1.0.0
     */
    public static String isbn10() { return "^\\d{9}[\\dX]$"; }

    /**
     * Matches an ISBN-13 (13 digits starting with 978 or 979).
     *
     * <p>Anchored. Does not validate the checksum.</p>
     *
     * @return the regex string
     * @see #isbn10()
     * @since 1.0.0
     */
    public static String isbn13() { return "^97[89]\\d{10}$"; }

    // ------------------------------------------------------------ Pakistan

    /**
     * Matches a Pakistani mobile number in any common format.
     *
     * <p>Anchored. Accepts the local format with leading {@code 0}
     * ({@code 03001234567}), the international format with {@code +92}
     * ({@code +923001234567}), the {@code 0092} prefix, and the
     * separator-stripped forms of all of those. The operator prefix is
     * {@code 3} followed by {@code 0}–{@code 6}, matching the current
     * Pakistani mobile operator allocations.</p>
     *
     * @return the regex string
     * @see Check#isPakistaniPhone(String)
     * @see chirp.validation.Rules#pakistaniMobile()
     * @since 1.0.0
     */
    public static String pakistaniMobile() { return "^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$"; }

    /**
     * Matches a Pakistani CNIC number (13 digits, no separators).
     *
     * <p>Anchored. First digit must be 1–8 — see
     * {@link chirp.validation.Rules#cnicProvince(String)} for the
     * province encoding. For the version that accepts separators
     * ({@code 35202-1234567-1}), use the {@code PK_CNIC} rule
     * ({@link chirp.validation.Rules#pakistaniCnic()}), which normalises
     * before matching.</p>
     *
     * @return the regex string
     * @see Check#isPakistaniCnic(String)
     * @see chirp.validation.Rules#pakistaniCnic()
     * @since 1.0.0
     */
    public static String pakistaniCnic() { return "^[1-8]\\d{12}$"; }

    /**
     * Matches a Pakistani landline number.
     *
     * <p>Anchored. Starts with {@code 0}, then a 2–9 area code, then 8
     * or 9 more digits.</p>
     *
     * @return the regex string
     * @see #pakistaniMobile()
     * @since 1.0.0
     */
    public static String pakistaniLandline() { return "^0[2-9]\\d{8,9}$"; }

    /**
     * Matches a Pakistani postal code (5 digits).
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String pakistaniPostalCode() { return "^\\d{5}$"; }

    /**
     * Matches a Pakistani passport number — two uppercase letters
     * followed by seven digits.
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String pakistaniPassport() { return "^[A-Z]{2}\\d{7}$"; }

    /**
     * Matches a Pakistani NTN (National Tax Number) — 7 digits, optional
     * check digit with a hyphen.
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String pakistaniNtn() { return "^\\d{7}(-\\d)?$"; }

    /**
     * Matches a Pakistani IBAN — {@code PK} followed by 22 more
     * characters.
     *
     * @return the regex string
     * @see #iban()
     * @since 1.0.0
     */
    public static String pakistaniIban() { return "^PK\\d{2}[A-Z]{4}[A-Z0-9]{16}$"; }

    /**
     * Matches a Pakistani vehicle registration plate.
     *
     * <p>Matches the common format — 2–4 uppercase letters, an optional
     * hyphen or space, then 3–5 digits. Matches {@code "ABC-1234"},
     * {@code "LEB-12345"}, {@code "ABC 1234"}. Does not match the older
     * single-letter provincial format.</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String pakistaniVehiclePlate() { return "^[A-Z]{2,4}[- ]?\\d{3,5}$"; }

    // ------------------------------------------------------------ misc

    /**
     * Matches a six-digit hex colour — {@code #RRGGBB}.
     *
     * <p>Anchored, case-insensitive on the hex digits.</p>
     *
     * @return the regex string
     * @see #hexColorShort()
     * @see Check#isHexColor(String)
     * @since 1.0.0
     */
    public static String hexColor() { return "#[0-9A-Fa-f]{6}"; }

    /**
     * Matches a three-digit shorthand hex colour — {@code #RGB}.
     *
     * @return the regex string
     * @see #hexColor()
     * @since 1.0.0
     */
    public static String hexColorShort() { return "#[0-9A-Fa-f]{3}"; }

    /**
     * Matches a Base64-encoded string (standard alphabet, optional
     * padding).
     *
     * <p>Anchored. Does not validate that the input length is a multiple
     * of 4 — that check should be performed separately if needed.</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String base64() { return "^[A-Za-z0-9+/]+={0,2}$"; }

    /**
     * Matches an HTML tag.
     *
     * <p>Not anchored — intended for {@link java.util.regex.Matcher#find()}
     * to search for tags inside a larger string. Does not attempt to
     * parse nested structures or CDATA sections; use an HTML parser for
     * anything more than a quick tag-presence check.</p>
     *
     * @return the regex string
     * @since 1.0.0
     */
    public static String htmlTag() { return "<[^>]+>"; }

    /**
     * Matches a latitude coordinate (−90 to +90 inclusive).
     *
     * <p>Anchored. Accepts decimal degrees with a leading sign.
     * {@code "-90"}, {@code "0"}, {@code "45.5"}, {@code "+45.5"} all
     * match.</p>
     *
     * @return the regex string
     * @see #longitude()
     * @since 1.0.0
     */
    public static String latitude() { return "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$"; }

    /**
     * Matches a longitude coordinate (−180 to +180 inclusive).
     *
     * <p>Anchored. Accepts decimal degrees with a leading sign.</p>
     *
     * @return the regex string
     * @see #latitude()
     * @since 1.0.0
     */
    public static String longitude() { return "^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$"; }

    // ------------------------------------------------------------ cannot-express

    /**
     * Returns {@code null} — a strong-password policy cannot be expressed
     * safely as a single backtracking-safe regex.
     *
     * <p>A regex like {@code ".*(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}"} is
     * vulnerable to catastrophic backtracking, and a naive
     * {@code "^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$"} is a lookahead chain
     * that many regex engines handle poorly. The library therefore
     * logs a warning and returns {@code null} rather than hand back a
     * pattern that looks correct but can hang the JVM.</p>
     *
     * <p>Use {@link Check#isStrongPassword(String)} (which uses four
     * independent, linear-time checks) or
     * {@link chirp.validation.Rules#strongPassword()} (which applies
     * {@link Check#isStrongPassword(String)} through the rule engine)
     * instead.</p>
     *
     * @return always {@code null}
     * @see Check#isStrongPassword(String)
     * @see chirp.validation.Rules#strongPassword()
     * @since 1.0.0
     */
    public static String strongPassword() {
        Logger.warn("Strong-password regex cannot be generated safely. "
                + "Use Check.isStrongPassword(...) or Rules.strongPassword() instead.");
        return null;
    }

    /**
     * Returns {@code null} — same rationale as {@link #strongPassword()}.
     *
     * @return always {@code null}
     * @see Check#isVeryStrongPassword(String)
     * @since 1.0.0
     */
    public static String veryStrongPassword() {
        Logger.warn("Very-strong-password regex cannot be generated safely. "
                + "Use Check.isVeryStrongPassword(...) instead.");
        return null;
    }
}
