package chirp.format;

import chirp.datetime.DateTime;
import chirp.number.Numbers;
import chirp.session.Session;
import chirp.text.Text;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Formatting helpers — one place for interpolation, currency, percent,
 * plural, list, ordinal, size, duration, and domain-specific identifiers.
 *
 * <p>Every method uses {@link java.util.Locale#ENGLISH} internally, so
 * output is identical on every machine — a Turkish-locale student gets the
 * same "1,234.50" as an English one. The <i>only</i> method that varies by
 * locale is {@link #currency(double, Object...)} when a custom
 * {@link Key#LOCALE} is explicitly passed.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Interpolation</b> — {@link #fmt(String, Object...)},
 *       {@link #fmtWithSession(String)}</li>
 *   <li><b>Currency</b> — {@link #currency(double)},
 *       {@link #rupees(double)},
 *       {@link #currencyOf(double, String)}</li>
 *   <li><b>Percent</b> — {@link #percent(double)},
 *       {@link #percent(double, int)}</li>
 *   <li><b>Decimals</b> — {@link #decimal(double, int)}</li>
 *   <li><b>Plurals</b> — {@link #plural(int, String, String)},
 *       {@link #quantify(int, String)}, {@link #items(int)}</li>
 *   <li><b>Lists</b> — {@link #list(List, String)},
 *       {@link #list(List, String, String)}</li>
 *   <li><b>Ordinals</b> — {@link #ordinal(int)},
 *       {@link #roman(int)}</li>
 *   <li><b>File sizes</b> — {@link #bytes(long)},
 *       {@link #fileSize(long)}</li>
 *   <li><b>Durations</b> — {@link #duration(long)},
 *       {@link #duration(Duration)}</li>
 *   <li><b>Text</b> — {@link #pad(String, int)},
 *       {@link #title(String)}, {@link #mask(String, int)},
 *       {@link #truncate(String, int)}</li>
 *   <li><b>Pakistan domain</b> — {@link #cnic(String)},
 *       {@link #pakistaniPhone(String)}</li>
 *   <li><b>Names</b> — {@link #fullName(String, String)},
 *       {@link #initials(String)}</li>
 *   <li><b>Fluent chains</b> — {@link #on(String)} → {@link Formatter}</li>
 * </ul>
 *
 * <h2>Three ways to format</h2>
 * <table border="1">
 *   <caption>Choose the right formatting API for the job</caption>
 *   <tr><th>API</th><th>When to use</th></tr>
 *   <tr><td>{@code Format.fmt(...)}</td><td>One-shot interpolation</td></tr>
 *   <tr><td>{@code Format.on(...).with(...)}</td><td>Reusable template with per-key transforms</td></tr>
 *   <tr><td>{@code String.format(...)}</td><td>Plain positional format strings</td></tr>
 * </table>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Format.fmt("Hello, {name}!", "name", "Saad");       // "Hello, Saad!"
 * Format.currency(1234.5);                             // "$1,234.50"
 * Format.percent(92.5);                                // "92.50%"
 * Format.percent(92.5, 1);                             // "92.5%"
 * Format.plural(1, "item", "items");                   // "1 item"
 * Format.plural(5, "item", "items");                   // "5 items"
 * Format.list(List.of("a","b","c"), ", ", ", and ");   // "a, b, and c"
 * Format.ordinal(1);                                   // "1st"
 * Format.roman(4);                                     // "IV"
 * Format.bytes(1536);                                  // "1.5 KB"
 * Format.duration(3_665_000L);                         // "1h 1m 5s"
 * Format.cnic("3520212345671");                        // "35202-1234567-1"
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For fluent templates, see {@link Formatter}. For static text
 * transforms, see {@link chirp.text.Text}. For the numeric equivalents
 * (ordinals, file sizes), see {@link chirp.number.Numbers}.</p>
 *
 * @see Formatter
 * @see Text
 * @see Numbers
 * @see DateTime
 * @see Session
 * @since 1.0.0
 */
public final class Format {

    // ============================================================== enums

    /**
     * Key enum for the key-value overloads in this module.
     *
     * <p>Only three methods consume these — {@link #currency(double, Object...)},
     * {@link #percent(double, Object...)}, and {@link #duration(long, Object...)}.
     * All other methods take plain arguments.</p>
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Number of decimal places (0 or more). */
        DECIMALS,
        /** The {@link Locale} to use for formatting. */
        LOCALE,
        /** The currency symbol to place before the number. */
        SYMBOL,
        /** Field width for padded text — reserved for future use. */
        WIDTH,
        /** Fill character for padded text — reserved for future use. */
        PAD_CHAR,
        /** Text alignment for padded output — reserved for future use. */
        ALIGN,
        /** Overall style (SHORT, LONG, MEDIUM) — used by duration. */
        STYLE,
        /** Prefix for formatted output — reserved for future use. */
        PREFIX,
        /** Suffix for formatted output — reserved for future use. */
        SUFFIX
    }

    /**
     * Overall style of formatted output.
     *
     * <p>Currently used only by {@link #duration(long, Object...)} and
     * {@link #currency(double, Object...)} — {@link #SHORT} produces a
     * compact form, {@link #LONG} the verbose form, {@link #MEDIUM} is
     * reserved.</p>
     *
     * @since 1.0.0
     */
    public enum Style { SHORT, LONG, MEDIUM }

    /**
     * Alignment for padded text.
     *
     * <p>Reserved for future use — currently no method on {@code Format}
     * takes an alignment argument. Padding on {@link Text} is the
     * underlying primitive.</p>
     *
     * @since 1.0.0
     */
    public enum Align { LEFT, CENTER, RIGHT }

    private Format() {}

    // ============================================================== factories for chains

    /**
     * Opens a fluent {@link Formatter} on the template — the same
     * behaviour as {@link #fmt(String, Object...)} but reusable and
     * chainable.
     *
     * <pre>{@code
     * String msg = Format.on("Hi {name}, you have {n} messages")
     *     .with("name", "Saad")
     *     .with("n", 5)
     *     .format();
     * // "Hi Saad, you have 5 messages"
     * }</pre>
     *
     * @param template the template with {@code {key}} placeholders (may be null)
     * @return a new {@code Formatter} wrapping {@code template}
     * @see Formatter
     * @see #of(String)
     * @see #template(String)
     * @see #formatter(String)
     * @since 1.0.0
     */
    public static Formatter on(String template) { return Formatter.of(template); }

    /**
     * Alias of {@link #on(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #on(String)
     * @since 1.0.0
     */
    public static Formatter of(String template) { return Formatter.of(template); }

    /**
     * Alias of {@link #on(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #on(String)
     * @since 1.0.0
     */
    public static Formatter template(String template) { return Formatter.of(template); }

    /**
     * Alias of {@link #on(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #on(String)
     * @since 1.0.0
     */
    public static Formatter formatter(String template) { return Formatter.of(template); }

    // ============================================================== fmt (interpolation)

    /**
     * Replaces {@code {key}} placeholders in the template with the given
     * key/value pairs.
     *
     * <p>Pairs are alternating — key, value, key, value, … An odd number
     * of arguments is ignored from the last unmatched key onward.</p>
     *
     * <pre>{@code
     * Format.fmt("Hello, {name}! You are {age}.",
     *     "name", "Saad", "age", 20);
     * // "Hello, Saad! You are 20."
     *
     * Format.fmt("Hi, {name}!", "name", "Ali");
     * // "Hi, Ali!"
     *
     * Format.fmt("No placeholders here");
     * // "No placeholders here"
     * }</pre>
     *
     * <p>Values are converted with {@link String#valueOf(Object)} — so
     * {@code null} becomes the string {@code "null"}. If you want empty
     * string for {@code null}, wrap values in {@link Formatter#defaultValue}
     * or filter them out at the call site.</p>
     *
     * @param template the template (may be null — returns empty)
     * @param keyValuePairs alternating key/value pairs (may be null)
     * @return the interpolated string
     * @see #format(String, Object...)
     * @see #interpolate(String, Object...)
     * @see #fmt(String, Map)
     * @see #on(String)
     * @since 1.0.0
     */
    public static String fmt(String template, Object... keyValuePairs) {
        if (template == null) return "";
        if (keyValuePairs == null || keyValuePairs.length == 0) return template;

        String result = template;
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            String key = String.valueOf(keyValuePairs[i]);
            String value = String.valueOf(keyValuePairs[i + 1]);
            result = result.replace("{" + key + "}", value);
        }
        return result;
    }

    /**
     * Alias of {@link #fmt(String, Object...)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @param kv alternating key/value pairs
     * @return the interpolated string
     * @see #fmt(String, Object...)
     * @since 1.0.0
     */
    public static String format(String template, Object... kv) { return fmt(template, kv); }

    /**
     * Alias of {@link #fmt(String, Object...)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @param kv alternating key/value pairs
     * @return the interpolated string
     * @see #fmt(String, Object...)
     * @since 1.0.0
     */
    public static String interpolate(String template, Object... kv) { return fmt(template, kv); }

    /**
     * Replaces {@code {key}} placeholders in the template with the given
     * map's key/value pairs.
     *
     * <pre>{@code
     * Format.fmt("Hi {name}", Map.of("name", "Saad"));   // "Hi Saad"
     * }</pre>
     *
     * @param template the template (may be null — returns empty)
     * @param values the map of substitutions (may be null — returns template unchanged)
     * @return the interpolated string
     * @see #fmt(String, Object...)
     * @since 1.0.0
     */
    public static String fmt(String template, Map<String, Object> values) {
        if (template == null) return "";
        if (values == null) return template;
        String result = template;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return result;
    }

    /**
     * Alias of {@link #fmt(String, Map)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @param values the map of substitutions
     * @return the interpolated string
     * @see #fmt(String, Map)
     * @since 1.0.0
     */
    public static String format(String template, Map<String, Object> values) { return fmt(template, values); }

    /**
     * Interpolates the current {@link Session}'s fields into a template.
     *
     * <p>Supported placeholders: {@code {name}}, {@code {roll}},
     * {@code {batch}}, {@code {degree}}, {@code {section}},
     * {@code {email}}. A field that is not set in the Session interpolates
     * to the empty string, not to a NullPointerException.</p>
     *
     * <pre>{@code
     * Session.me().name("Saad").rollNumber("2024-CS-001");
     * Format.fmtWithSession("Student: {name} ({roll})");
     * // "Student: Saad (2024-CS-001)"
     *
     * // Unset fields interpolate to ""
     * Format.fmtWithSession("Section: {section}");   // "Section: "
     * }</pre>
     *
     * @param template the template (may be null — returns empty)
     * @return the interpolated string
     * @see #formatWithSession(String)
     * @see #sessionFormat(String)
     * @see Session
     * @see Formatter#withSession()
     * @since 1.0.0
     */
    public static String fmtWithSession(String template) {
        if (template == null) return "";
        Session s = Session.me();
        return template
                .replace("{name}", valueOr(s.name(), ""))
                .replace("{roll}", valueOr(s.rollNumber(), ""))
                .replace("{batch}", valueOr(s.batch(), ""))
                .replace("{degree}", valueOr(s.degree(), ""))
                .replace("{section}", valueOr(s.section(), ""))
                .replace("{email}", valueOr(s.email(), ""));
    }

    /**
     * Alias of {@link #fmtWithSession(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return the interpolated string
     * @see #fmtWithSession(String)
     * @since 1.0.0
     */
    public static String formatWithSession(String template) { return fmtWithSession(template); }

    /**
     * Alias of {@link #fmtWithSession(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return the interpolated string
     * @see #fmtWithSession(String)
     * @since 1.0.0
     */
    public static String sessionFormat(String template) { return fmtWithSession(template); }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    // ============================================================== currency

    /**
     * Formats the amount as US dollars — {@code "$1,234.50"}.
     *
     * <p>Always two decimal places, always thousands separators, always
     * the dollar sign. For a custom symbol, use
     * {@link #currencyOf(double, String)} or
     * {@link #currency(double, Object...)} with {@link Key#SYMBOL}.</p>
     *
     * <pre>{@code
     * Format.currency(1234.5);     // "$1,234.50"
     * Format.currency(0);          // "$0.00"
     * Format.currency(-50);        // "$-50.00"
     * }</pre>
     *
     * @param amount the amount
     * @return the formatted string
     * @see #dollars(double)
     * @see #usd(double)
     * @see #money(double)
     * @see #rupees(double)
     * @see #currencyOf(double, String)
     * @since 1.0.0
     */
    public static String currency(double amount) { return String.format(Locale.ENGLISH, "$%,.2f", amount); }

    /**
     * Alias of {@link #currency(double)} — see that method for full
     * documentation and examples.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #currency(double)
     * @since 1.0.0
     */
    public static String dollars(double amount) { return currency(amount); }

    /**
     * Alias of {@link #currency(double)} — see that method for full
     * documentation and examples.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #currency(double)
     * @since 1.0.0
     */
    public static String usd(double amount) { return currency(amount); }

    /**
     * Alias of {@link #currency(double)} — see that method for full
     * documentation and examples.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #currency(double)
     * @since 1.0.0
     */
    public static String money(double amount) { return currency(amount); }

    /**
     * Formats the amount as currency with key-value options — allows a
     * custom symbol, custom decimals, and custom locale.
     *
     * <pre>{@code
     * Format.currency(1234.5, Format.Key.SYMBOL, "€");           // "€1,234.50"
     * Format.currency(1234.5, Format.Key.DECIMALS, 0);            // "$1,235"
     * Format.currency(1234.5, Format.Key.SYMBOL, "Rs.",
     *     Format.Key.DECIMALS, 0);                                // "Rs.1,235"
     * Format.currency(1234.5, Format.Key.STYLE, Format.Style.SHORT);
     * // "$1234.50"  (no thousands grouping)
     * }</pre>
     *
     * <p>Null keys and wrong value types are silently skipped — the
     * defaults ({@code symbol="$"}, {@code decimals=2},
     * {@code locale=ENGLISH}, {@code grouped=true}) apply.</p>
     *
     * @param amount the amount
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return the formatted string
     * @see #currency(double)
     * @see #money(double, Object...)
     * @since 1.0.0
     */
    public static String currency(double amount, Object... keyValues) {
        String symbol = "$";
        int decimals = 2;
        Locale locale = Locale.ENGLISH;
        boolean useGrouping = true;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case SYMBOL -> {
                        if (v instanceof String s) symbol = s;
                    }
                    case DECIMALS -> {
                        if (v instanceof Integer n && n >= 0) decimals = n;
                    }
                    case LOCALE -> {
                        if (v instanceof Locale l) locale = l;
                    }
                    case STYLE -> {
                        if (v instanceof Style st) useGrouping = st != Style.SHORT;
                    }
                    default -> {
                        /* skip */ }
                }
            }
        }
        String pattern = (useGrouping ? "%,." : "%.") + decimals + "f";
        return symbol + String.format(locale, pattern, amount);
    }

    /**
     * Alias of {@link #currency(double, Object...)} — see that method for
     * full documentation and examples.
     *
     * @param amount the amount
     * @param keyValues alternating {@link Key} → value pairs
     * @return the formatted string
     * @see #currency(double, Object...)
     * @since 1.0.0
     */
    public static String money(double amount, Object... keyValues) { return currency(amount, keyValues); }

    /**
     * Formats the amount as Pakistani rupees — {@code "Rs.1,234.50"}.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #pkr(double)
     * @see #pakRupees(double)
     * @see #currency(double)
     * @since 1.0.0
     */
    public static String rupees(double amount) { return String.format(Locale.ENGLISH, "Rs.%,.2f", amount); }

    /**
     * Alias of {@link #rupees(double)} — see that method for full
     * documentation.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #rupees(double)
     * @since 1.0.0
     */
    public static String pkr(double amount) { return rupees(amount); }

    /**
     * Alias of {@link #rupees(double)} — see that method for full
     * documentation.
     *
     * @param amount the amount
     * @return the formatted string
     * @see #rupees(double)
     * @since 1.0.0
     */
    public static String pakRupees(double amount) { return rupees(amount); }

    /**
     * Formats the amount with an arbitrary currency symbol.
     *
     * @param amount the amount
     * @param symbol the currency symbol to place before the number (may be
     * null — defaults to {@code "$"})
     * @return the formatted string
     * @see #moneyOf(double, String)
     * @see #currency(double)
     * @since 1.0.0
     */
    public static String currencyOf(double amount, String symbol) {
        if (symbol == null) symbol = "$";
        return symbol + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    /**
     * Alias of {@link #currencyOf(double, String)} — see that method for
     * full documentation.
     *
     * @param amount the amount
     * @param symbol the currency symbol
     * @return the formatted string
     * @see #currencyOf(double, String)
     * @since 1.0.0
     */
    public static String moneyOf(double amount, String symbol) { return currencyOf(amount, symbol); }

    // ============================================================== percent

    /**
     * Formats the value as a percentage with two decimal places —
     * {@code "92.50%"}.
     *
     * <p>The value is already in percent — {@code 92.5} produces
     * {@code "92.50%"}, not {@code "0.925%"}. This matches the
     * {@link Numbers#percent(double, double)} convention where values
     * are in 0–100.</p>
     *
     * @param value the value in percent (0–100)
     * @return the formatted string
     * @see #pct(double)
     * @see #percentage(double)
     * @see #percent(double, int)
     * @since 1.0.0
     */
    public static String percent(double value) { return String.format(Locale.ENGLISH, "%.2f%%", value); }

    /**
     * Alias of {@link #percent(double)} — see that method for full
     * documentation.
     *
     * @param value the value in percent
     * @return the formatted string
     * @see #percent(double)
     * @since 1.0.0
     */
    public static String pct(double value) { return percent(value); }

    /**
     * Alias of {@link #percent(double)} — see that method for full
     * documentation.
     *
     * @param value the value in percent
     * @return the formatted string
     * @see #percent(double)
     * @since 1.0.0
     */
    public static String percentage(double value) { return percent(value); }

    /**
     * Formats the value as a percentage with the specified decimal places.
     *
     * <pre>{@code
     * Format.percent(92.5, 0);   // "93%"
     * Format.percent(92.5, 1);   // "92.5%"
     * Format.percent(92.5, 3);   // "92.500%"
     * }</pre>
     *
     * @param value the value in percent
     * @param places the number of decimal places (negative → 2)
     * @return the formatted string
     * @see #percent(double)
     * @see #pct(double, int)
     * @since 1.0.0
     */
    public static String percent(double value, int places) {
        if (places < 0) places = 2;
        return String.format(Locale.ENGLISH, "%." + places + "f%%", value);
    }

    /**
     * Alias of {@link #percent(double, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value in percent
     * @param places the number of decimal places
     * @return the formatted string
     * @see #percent(double, int)
     * @since 1.0.0
     */
    public static String pct(double value, int places) { return percent(value, places); }

    /**
     * Formats the value as a percentage with key-value options.
     *
     * @param value the value in percent
     * @param keyValues alternating {@link Key} → value pairs (only
     * {@link Key#DECIMALS} is consumed)
     * @return the formatted string
     * @see #percent(double, int)
     * @since 1.0.0
     */
    public static String percent(double value, Object... keyValues) {
        int places = 2;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.DECIMALS && keyValues[i + 1] instanceof Integer n && n >= 0) places = n;
            }
        }
        return percent(value, places);
    }

    // ============================================================== decimal

    /**
     * Formats the value with the specified number of decimal places —
     * always using the decimal point, never a locale-specific separator.
     *
     * <pre>{@code
     * Format.decimal(3.14159, 2);   // "3.14"
     * Format.decimal(3.0, 2);       // "3.00"
     * Format.decimal(1234.5, 0);    // "1235"
     * }</pre>
     *
     * @param value the value to format
     * @param places the number of decimal places (negative → 2)
     * @return the formatted string
     * @see #fixed(double, int)
     * @see #toFixed(double, int)
     * @see Numbers#format(double, int)
     * @since 1.0.0
     */
    public static String decimal(double value, int places) {
        if (places < 0) places = 2;
        return String.format(Locale.ENGLISH, "%." + places + "f", value);
    }

    /**
     * Alias of {@link #decimal(double, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to format
     * @param places the number of decimal places
     * @return the formatted string
     * @see #decimal(double, int)
     * @since 1.0.0
     */
    public static String fixed(double value, int places) { return decimal(value, places); }

    /**
     * Alias of {@link #decimal(double, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to format
     * @param places the number of decimal places
     * @return the formatted string
     * @see #decimal(double, int)
     * @since 1.0.0
     */
    public static String toFixed(double value, int places) { return decimal(value, places); }

    /**
     * Alias of {@link #decimal(double, int)} — see that method for full
     * documentation and examples.
     *
     * @param v the value to format
     * @param p the number of decimal places
     * @return the formatted string
     * @see #decimal(double, int)
     * @since 1.0.0
     */
    public static String fixedDecimal(double v, int p) { return decimal(v, p); }

    // ============================================================== plural

    /**
     * Returns {@code count} followed by the correct plural form.
     *
     * <p>Uses {@code count == 1} to select singular vs. plural. Zero takes
     * the plural form ("0 items", not "0 item"), which matches English
     * grammar conventions.</p>
     *
     * <pre>{@code
     * Format.plural(1, "item", "items");   // "1 item"
     * Format.plural(5, "item", "items");   // "5 items"
     * Format.plural(0, "item", "items");   // "0 items"
     * Format.plural(-1, "item", "items");  // "-1 items"  (negative takes plural)
     * }</pre>
     *
     * @param count the count
     * @param singular the singular form
     * @param plural the plural form
     * @return the formatted string
     * @see #countOf(int, String, String)
     * @see #quantify(int, String)
     * @since 1.0.0
     */
    public static String plural(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    /**
     * Alias of {@link #plural(int, String, String)} — see that method for
     * full documentation and examples.
     *
     * @param count the count
     * @param singular the singular form
     * @param plural the plural form
     * @return the formatted string
     * @see #plural(int, String, String)
     * @since 1.0.0
     */
    public static String countOf(int count, String singular, String plural) {
        return plural(count, singular, plural);
    }

    /**
     * Adds a plural form automatically by appending "s" to the singular.
     *
     * <p>Works for regular English plurals — "cat"/"cats", "book"/"books".
     * Does <b>not</b> handle irregular plurals ("child"/"children",
     * "person"/"people", "mouse"/"mice"). For those, use
     * {@link #plural(int, String, String)} with both forms explicitly.</p>
     *
     * <pre>{@code
     * Format.quantify(1, "book");     // "1 book"
     * Format.quantify(5, "book");     // "5 books"
     * Format.quantify(1, "child");    // "1 child"
     * Format.quantify(5, "child");    // "5 childs"  ← WRONG, use plural() instead
     * }</pre>
     *
     * @param count the count
     * @param singular the singular form
     * @return the formatted string with auto-pluralised noun
     * @see #plural(int, String, String)
     * @see #countOf(int, String)
     * @since 1.0.0
     */
    public static String quantify(int count, String singular) {
        return plural(count, singular, singular + "s");
    }

    /**
     * Alias of {@link #quantify(int, String)} — see that method for full
     * documentation and examples.
     *
     * @param count the count
     * @param singular the singular form
     * @return the formatted string
     * @see #quantify(int, String)
     * @since 1.0.0
     */
    public static String countOf(int count, String singular) { return quantify(count, singular); }

    /**
     * Formats a count as "{@code N item(s)}" using the correct plural.
     *
     * @param count the count
     * @return "{@code 1 item}" or "{@code N items}"
     * @see #files(int)
     * @see #lines(int)
     * @see #chars(int)
     * @since 1.0.0
     */
    public static String items(int count) { return plural(count, "item", "items"); }

    /**
     * Formats a count as "{@code N file(s)}" using the correct plural.
     *
     * @param count the count
     * @return "{@code 1 file}" or "{@code N files}"
     * @see #items(int)
     * @since 1.0.0
     */
    public static String files(int count) { return plural(count, "file", "files"); }

    /**
     * Formats a count as "{@code N line(s)}" using the correct plural.
     *
     * @param count the count
     * @return "{@code 1 line}" or "{@code N lines}"
     * @see #items(int)
     * @since 1.0.0
     */
    public static String lines(int count) { return plural(count, "line", "lines"); }

    /**
     * Formats a count as "{@code N char(s)}" using the correct plural.
     *
     * @param count the count
     * @return "{@code 1 char}" or "{@code N chars}"
     * @see #items(int)
     * @since 1.0.0
     */
    public static String chars(int count) { return plural(count, "char", "chars"); }

    // ============================================================== lists

    /**
     * Joins a list's elements with a single delimiter.
     *
     * @param items the list (may be null — returns empty)
     * @param delimiter the separator (may be null — treated as empty)
     * @param <T> the element type
     * @return the joined string
     * @see #join(List, String)
     * @see #list(List, String, String)
     * @see chirp.collection.Lists#join(List, String)
     * @since 1.0.0
     */
    public static <T> String list(List<T> items, String delimiter) {
        if (items == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    /**
     * Alias of {@link #list(List, String)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param delimiter the separator
     * @param <T> the element type
     * @return the joined string
     * @see #list(List, String)
     * @since 1.0.0
     */
    public static <T> String join(List<T> items, String delimiter) { return list(items, delimiter); }

    /**
     * Alias of {@link #list(List, String)} — see that method for full
     * documentation.
     *
     * @param items the list
     * @param delimiter the separator
     * @param <T> the element type
     * @return the joined string
     * @see #list(List, String)
     * @since 1.0.0
     */
    public static <T> String itemsList(List<T> items, String delimiter) { return list(items, delimiter); }

    /**
     * Alias of {@link #list(List, String)} with a comma-space delimiter —
     * see that method for full documentation.
     *
     * @param items the list
     * @param <T> the element type
     * @return the comma-separated string
     * @see #list(List, String)
     * @since 1.0.0
     */
    public static <T> String comma(List<T> items) { return list(items, ", "); }

    /**
     * Joins a list's elements with a different delimiter for the last
     * element — the classic "a, b, and c" pattern.
     *
     * <pre>{@code
     * Format.list(List.of("a", "b", "c"), ", ", ", and ");   // "a, b, and c"
     * Format.list(List.of("a", "b"), ", ", " and ");          // "a and b"
     * Format.list(List.of("a"), ", ", " and ");               // "a"
     * }</pre>
     *
     * @param items the list (may be null — returns empty)
     * @param delimiter the separator for non-last elements
     * @param lastDelimiter the separator for the last element
     * @param <T> the element type
     * @return the joined string
     * @see #list(List, String)
     * @see #listWithLast(List, String, String)
     * @since 1.0.0
     */
    public static <T> String list(List<T> items, String delimiter, String lastDelimiter) {
        if (items == null || items.isEmpty()) return "";
        if (items.size() == 1) return String.valueOf(items.get(0));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(i == items.size() - 1 ? lastDelimiter : delimiter);
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    /**
     * Alias of {@link #list(List, String, String)} — see that method for
     * full documentation and examples.
     *
     * @param items the list
     * @param d the regular delimiter
     * @param last the last-element delimiter
     * @param <T> the element type
     * @return the joined string
     * @see #list(List, String, String)
     * @since 1.0.0
     */
    public static <T> String listWithLast(List<T> items, String d, String last) { return list(items, d, last); }

    // ============================================================== ordinal / roman

    /**
     * Returns the ordinal for the number — {@code "1st"}, {@code "2nd"},
     * {@code "3rd"}, {@code "4th"}, etc.
     *
     * <p>Delegates to {@link Numbers#ordinal(int)}, which handles the
     * 11th/12th/13th exception correctly (including 111th, 112th, 113th
     * and their negatives).</p>
     *
     * @param n the number
     * @return the ordinal string
     * @see Numbers#ordinal(int)
     * @see #nth(int)
     * @see #roman(int)
     * @since 1.0.0
     */
    public static String ordinal(int n) { return Numbers.ordinal(n); }

    /**
     * Alias of {@link #ordinal(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the ordinal string
     * @see #ordinal(int)
     * @since 1.0.0
     */
    public static String nth(int n) { return ordinal(n); }

    /**
     * Returns the Roman numeral for the integer. Delegates to
     * {@link Numbers#roman(int)} — supports 1–3999, and out-of-range
     * values return the decimal string.
     *
     * @param n the number
     * @return the Roman numeral or the decimal string for out-of-range
     * @see Numbers#roman(int)
     * @see #ordinal(int)
     * @since 1.0.0
     */
    public static String roman(int n) { return Numbers.roman(n); }

    // ============================================================== bytes / filesize

    /**
     * Returns the human-readable file size. Delegates to
     * {@link Numbers#fileSize(long)}.
     *
     * @param count the number of bytes
     * @return the human-readable size
     * @see Numbers#fileSize(long)
     * @see #fileSize(long)
     * @see #humanSize(long)
     * @since 1.0.0
     */
    public static String bytes(long count) { return Numbers.fileSize(count); }

    /**
     * Alias of {@link #bytes(long)} — see that method for full
     * documentation.
     *
     * @param count the number of bytes
     * @return the human-readable size
     * @see #bytes(long)
     * @since 1.0.0
     */
    public static String fileSize(long count) { return Numbers.fileSize(count); }

    /**
     * Alias of {@link #bytes(long)} — see that method for full
     * documentation.
     *
     * @param count the number of bytes
     * @return the human-readable size
     * @see #bytes(long)
     * @since 1.0.0
     */
    public static String humanSize(long count) { return Numbers.fileSize(count); }

    // ============================================================== duration

    /**
     * Formats a duration in milliseconds as a compact human-readable
     * string.
     *
     * <pre>{@code
     * Format.duration(0);            // "0ms"
     * Format.duration(500);          // "500ms"
     * Format.duration(5000);         // "5s"
     * Format.duration(65000);        // "1m 5s"
     * Format.duration(3_665_000L);   // "1h 1m 5s"
     * Format.duration(86_400_000L);  // "1d"
     * Format.duration(-1);           // "0ms"
     * }</pre>
     *
     * @param millis the duration in milliseconds (negative → "0ms")
     * @return the formatted string
     * @see #humanDuration(long)
     * @see #timeSpan(long)
     * @see #duration(Duration)
     * @see #duration(long, Object...)
     * @since 1.0.0
     */
    public static String duration(long millis) {
        if (millis < 0) return "0ms";
        if (millis < 1000) return millis + "ms";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d" + (hours % 24 > 0 ? " " + (hours % 24) + "h" : "");
        }
        if (hours > 0) {
            StringBuilder sb = new StringBuilder(hours + "h");
            if (minutes % 60 > 0) sb.append(' ').append(minutes % 60).append('m');
            if (seconds % 60 > 0) sb.append(' ').append(seconds % 60).append('s');
            return sb.toString();
        }
        if (minutes > 0) {
            StringBuilder sb = new StringBuilder(minutes + "m");
            if (seconds % 60 > 0) sb.append(' ').append(seconds % 60).append('s');
            return sb.toString();
        }
        return seconds + "s";
    }

    /**
     * Alias of {@link #duration(long)} — see that method for full
     * documentation and examples.
     *
     * @param millis the duration in milliseconds
     * @return the formatted string
     * @see #duration(long)
     * @since 1.0.0
     */
    public static String humanDuration(long millis) { return duration(millis); }

    /**
     * Alias of {@link #duration(long)} — see that method for full
     * documentation and examples.
     *
     * @param millis the duration in milliseconds
     * @return the formatted string
     * @see #duration(long)
     * @since 1.0.0
     */
    public static String timeSpan(long millis) { return duration(millis); }

    /**
     * Formats a {@link java.time.Duration} using the same rules as
     * {@link #duration(long)}.
     *
     * @param d the duration (may be null — returns "0ms")
     * @return the formatted string
     * @see #duration(long)
     * @see #humanDuration(Duration)
     * @since 1.0.0
     */
    public static String duration(Duration d) {
        if (d == null) return "0ms";
        return duration(d.toMillis());
    }

    /**
     * Alias of {@link #duration(Duration)} — see that method for full
     * documentation.
     *
     * @param d the duration
     * @return the formatted string
     * @see #duration(Duration)
     * @since 1.0.0
     */
    public static String humanDuration(Duration d) { return duration(d); }

    /**
     * Formats a duration with key-value options — allows a short style
     * ({@code "5s"}, {@code "1m"}, {@code "1h"}, {@code "1d"}) instead of
     * the long form.
     *
     * @param millis the duration in milliseconds
     * @param keyValues alternating {@link Key} → value pairs (only
     * {@link Key#STYLE} is consumed)
     * @return the formatted string
     * @see #duration(long)
     * @see Style
     * @since 1.0.0
     */
    public static String duration(long millis, Object... keyValues) {
        Style style = Style.LONG;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.STYLE && keyValues[i + 1] instanceof Style s) style = s;
            }
        }
        if (style == Style.SHORT) {
            if (millis < 1000) return millis + "ms";
            long s = millis / 1000;
            if (s < 60) return s + "s";
            long m = s / 60;
            if (m < 60) return m + "m";
            long h = m / 60;
            if (h < 24) return h + "h";
            return (h / 24) + "d";
        }
        return duration(millis);
    }

    // ============================================================== text delegation

    /**
     * Right-pads the value with spaces to reach the given width. Delegates
     * to {@link Text#padRight(String, int)}.
     *
     * @param value the string to pad (may be null — treated as empty)
     * @param width the target width
     * @return the padded string
     * @see Text#padRight(String, int)
     * @see #padLeft(String, int)
     * @see #padRight(String, int)
     * @see #center(String, int)
     * @since 1.0.0
     */
    public static String pad(String value, int width) { return Text.padRight(value, width); }

    /**
     * Left-pads the value with spaces. Delegates to
     * {@link Text#padLeft(String, int)}.
     *
     * @param value the string to pad
     * @param width the target width
     * @return the padded string
     * @see Text#padLeft(String, int)
     * @see #pad(String, int)
     * @since 1.0.0
     */
    public static String padLeft(String value, int width) { return Text.padLeft(value, width); }

    /**
     * Right-pads the value with spaces. Delegates to
     * {@link Text#padRight(String, int)}.
     *
     * @param value the string to pad
     * @param width the target width
     * @return the padded string
     * @see Text#padRight(String, int)
     * @see #pad(String, int)
     * @since 1.0.0
     */
    public static String padRight(String value, int width) { return Text.padRight(value, width); }

    /**
     * Centers the value within the given width. Delegates to
     * {@link Text#center(String, int)}.
     *
     * @param value the string to center
     * @param width the target width
     * @return the centered string
     * @see Text#center(String, int)
     * @see #pad(String, int)
     * @since 1.0.0
     */
    public static String center(String value, int width) { return Text.center(value, width); }

    /**
     * Alias of {@link #pad(String, int)} — see that method for full
     * documentation.
     *
     * @param value the string to pad
     * @param width the target width
     * @return the padded string
     * @see #pad(String, int)
     * @since 1.0.0
     */
    public static String padded(String value, int width) { return Text.padRight(value, width); }

    /**
     * Converts the value to title case. Delegates to
     * {@link Text#titleCase(String)}.
     *
     * @param value the string to transform (may be null)
     * @return the title-cased string, or null
     * @see Text#titleCase(String)
     * @see #capitalize(String)
     * @since 1.0.0
     */
    public static String title(String value) { return Text.titleCase(value); }

    /**
     * Capitalises the first letter and lowercases the rest. Delegates to
     * {@link Text#capitalize(String)}.
     *
     * @param v the string to transform (may be null)
     * @return the capitalised string, or null
     * @see Text#capitalize(String)
     * @see #title(String)
     * @since 1.0.0
     */
    public static String capitalize(String v) { return Text.capitalize(v); }

    /**
     * Converts the value to uppercase using {@link Locale#ROOT}.
     *
     * @param v the string to transform (may be null)
     * @return the uppercase string, or null
     * @see #lower(String)
     * @since 1.0.0
     */
    public static String upper(String v) { return v == null ? null : v.toUpperCase(Locale.ROOT); }

    /**
     * Converts the value to lowercase using {@link Locale#ROOT}.
     *
     * @param v the string to transform (may be null)
     * @return the lowercase string, or null
     * @see #upper(String)
     * @since 1.0.0
     */
    public static String lower(String v) { return v == null ? null : v.toLowerCase(Locale.ROOT); }

    /**
     * Masks all but the last {@code visible} characters with asterisks.
     * Delegates to {@link Text#mask(String, int)}.
     *
     * @param value the string to mask
     * @param visible the number of trailing characters to keep
     * @return the masked string
     * @see Text#mask(String, int)
     * @since 1.0.0
     */
    public static String mask(String value, int visible) { return Text.mask(value, visible); }

    /**
     * Converts the value to a URL-safe slug. Delegates to
     * {@link Text#slugify(String)}.
     *
     * @param value the string to transform
     * @return the slug
     * @see Text#slugify(String)
     * @since 1.0.0
     */
    public static String slug(String value) { return Text.slugify(value); }

    /**
     * Truncates with an ellipsis if longer than {@code max}. Delegates to
     * {@link Text#truncate(String, int)}.
     *
     * @param v the string to truncate
     * @param max the maximum output length
     * @return the truncated string
     * @see Text#truncate(String, int)
     * @since 1.0.0
     */
    public static String truncate(String v, int max) { return Text.truncate(v, max); }

    // ============================================================== domain (PK)

    /**
     * Formats a Pakistani CNIC with dashes — {@code "35202-1234567-1"}.
     *
     * <p>Strips non-digits then inserts dashes at the standard positions
     * (5-7-1). If the input does not have 13 digits, returns the input
     * unchanged rather than throwing.</p>
     *
     * <pre>{@code
     * Format.cnic("3520212345671");     // "35202-1234567-1"
     * Format.cnic("35202-1234567-1");   // "35202-1234567-1"  (idempotent)
     * Format.cnic("incomplete");         // "incomplete"  (unchanged)
     * }</pre>
     *
     * @param raw the raw CNIC (may be null)
     * @return the formatted CNIC, or the input unchanged if it has the
     * wrong number of digits
     * @see #formatCnic(String)
     * @see chirp.text.Check#isPakistaniCnic(String)
     * @since 1.0.0
     */
    public static String cnic(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 13) return raw;
        return digits.substring(0, 5) + "-" + digits.substring(5, 12) + "-" + digits.substring(12);
    }

    /**
     * Alias of {@link #cnic(String)} — see that method for full
     * documentation and examples.
     *
     * @param raw the raw CNIC
     * @return the formatted CNIC
     * @see #cnic(String)
     * @since 1.0.0
     */
    public static String formatCnic(String raw) { return cnic(raw); }

    /**
     * Formats a Pakistani mobile number in the international form —
     * {@code "+92 300 1234567"}.
     *
     * <p>Strips non-digits then normalises to the {@code +92 XXX XXXXXXX}
     * form. If the input does not have 10 digits after normalisation,
     * returns the input unchanged.</p>
     *
     * <pre>{@code
     * Format.pakistaniPhone("0300-1234567");       // "+92 300 1234567"
     * Format.pakistaniPhone("+92 300 1234567");   // "+92 300 1234567"
     * Format.pakistaniPhone("00923001234567");    // "+92 300 1234567"
     * Format.pakistaniPhone("invalid");            // "invalid"
     * }</pre>
     *
     * @param raw the raw phone number (may be null)
     * @return the formatted phone number, or the input unchanged if invalid
     * @see #pkPhone(String)
     * @see chirp.text.Check#isPakistaniPhone(String)
     * @since 1.0.0
     */
    public static String pakistaniPhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("0092")) digits = digits.substring(4);
        else if (digits.startsWith("92") && digits.length() > 10) digits = digits.substring(2);
        else if (digits.startsWith("0") && digits.length() == 11) digits = digits.substring(1);
        if (digits.length() != 10) return raw;
        return "+92 " + digits.substring(0, 3) + " " + digits.substring(3);
    }

    /**
     * Alias of {@link #pakistaniPhone(String)} — see that method for full
     * documentation and examples.
     *
     * @param raw the raw phone number
     * @return the formatted phone number
     * @see #pakistaniPhone(String)
     * @since 1.0.0
     */
    public static String pkPhone(String raw) { return pakistaniPhone(raw); }

    // ============================================================== date/time delegation

    /**
     * Formats a date using the human long form. Delegates to
     * {@link DateTime#humanize(java.time.LocalDate)}.
     *
     * @param d the date
     * @return the humanized string
     * @see DateTime#humanize(java.time.LocalDate)
     * @see #date(java.time.LocalDate, String)
     * @since 1.0.0
     */
    public static String date(java.time.LocalDate d) { return DateTime.humanize(d); }

    /**
     * Formats a date using a custom pattern. Delegates to
     * {@link DateTime#format(java.time.LocalDate, String)}.
     *
     * @param d the date
     * @param pattern the format pattern
     * @return the formatted string
     * @see DateTime#format(java.time.LocalDate, String)
     * @since 1.0.0
     */
    public static String date(java.time.LocalDate d, String pattern) { return DateTime.format(d, pattern); }

    /**
     * Formats a time using the human 12-hour form. Delegates to
     * {@link DateTime#humanize(java.time.LocalTime)}.
     *
     * @param t the time
     * @return the formatted string
     * @see DateTime#humanize(java.time.LocalTime)
     * @since 1.0.0
     */
    public static String time(java.time.LocalTime t) { return DateTime.humanize(t); }

    /**
     * Formats a date-time using the human medium form. Delegates to
     * {@link DateTime#humanize(java.time.LocalDateTime)}.
     *
     * @param dt the date-time
     * @return the formatted string
     * @see DateTime#humanize(java.time.LocalDateTime)
     * @since 1.0.0
     */
    public static String dateTime(java.time.LocalDateTime dt) { return DateTime.humanize(dt); }

    // ============================================================== names

    /**
     * Joins first and last names with a space, handling nulls gracefully.
     *
     * <pre>{@code
     * Format.fullName("Saad", "Mansoor");   // "Saad Mansoor"
     * Format.fullName("Saad", null);        // "Saad"
     * Format.fullName(null, "Mansoor");     // "Mansoor"
     * Format.fullName(null, null);          // ""
     * }</pre>
     *
     * @param first the first name (may be null)
     * @param last the last name (may be null)
     * @return the joined name
     * @see #initials(String)
     * @since 1.0.0
     */
    public static String fullName(String first, String last) {
        if (first == null && last == null) return "";
        if (first == null) return last;
        if (last == null) return first;
        return first.trim() + " " + last.trim();
    }

    /**
     * Returns the uppercase initials of the name. Delegates to
     * {@link Text#initials(String)}.
     *
     * @param fullName the name
     * @return the initials
     * @see Text#initials(String)
     * @see #fullName(String, String)
     * @since 1.0.0
     */
    public static String initials(String fullName) {
        return Text.initials(fullName);
    }
}
