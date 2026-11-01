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
 * plural, list, ordinal, size, duration, and domain identifiers.
 *
 * <h2>Naming</h2>
 * <p>Every method has 2–4 aliases. {@code fmt / format / interpolate};
 * {@code currency / dollars / usd / money}; {@code rupees / pkr / pakRupees};
 * {@code percent / pct / percentage}; {@code decimal / fixed / toFixed};
 * {@code bytes / fileSize / humanSize}; {@code duration / humanDuration /
 * timeSpan}; {@code pad / padLeft / padRight / center}.</p>
 *
 * <h2>Key-value (IDE autocomplete)</h2>
 * <p>Multi-option methods accept alternating {@link Key} → value pairs.
 * Missing values and wrong types are silently skipped; documented defaults
 * apply.</p>
 *
 * <pre>{@code
 * Format.money(1234.5, Format.Key.SYMBOL, "Rs.", Format.Key.DECIMALS, 0);
 * Format.percent(0.875, Format.Key.DECIMALS, 1);   // "87.5%"
 * Format.duration(Duration.ofSeconds(90));         // "1m 30s"
 * }</pre>
 *
 * <h2>Fluent chains</h2>
 * <p>{@link #on(String)} opens a {@link Formatter} — the same behaviour as
 * {@code fmt(...)} but fluent and reusable.</p>
 *
 * <pre>{@code
 * String msg = Format.on("Hi {name}, you have {n} messages")
 *     .with("name", "Saad")
 *     .with("n", 5)
 *     .plural("n", "message", "messages")
 *     .format();
 * // "Hi Saad, you have 5 messages"
 * }</pre>
 */
public final class Format {

    // ============================================================== enums

    /** Keys for key-value overloads in this module. */
    public enum Key {
        DECIMALS, LOCALE, SYMBOL, WIDTH, PAD_CHAR, ALIGN, STYLE, PREFIX, SUFFIX
    }

    /** Overall style of the formatted output. */
    public enum Style { SHORT, LONG, MEDIUM }

    /** Alignment for padded text. */
    public enum Align { LEFT, CENTER, RIGHT }

    private Format() {}

    // ============================================================== factories for chains

    public static Formatter on(String template) { return Formatter.of(template); }
    public static Formatter of(String template) { return Formatter.of(template); }
    public static Formatter template(String template) { return Formatter.of(template); }
    public static Formatter formatter(String template) { return Formatter.of(template); }

    // ============================================================== fmt (interpolation)

    /**
     * Replaces {@code {key}} placeholders in {@code template} with the given
     * key/value pairs.
     * Aliases: {@link #format}, {@link #interpolate}.
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
    public static String format(String template, Object... kv) { return fmt(template, kv); }
    public static String interpolate(String template, Object... kv) { return fmt(template, kv); }

    public static String fmt(String template, Map<String, Object> values) {
        if (template == null) return "";
        if (values == null) return template;
        String result = template;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return result;
    }
    public static String format(String template, Map<String, Object> values) { return fmt(template, values); }

    /**
     * Interpolates the current {@link Session}'s fields into a template.
     * Supported placeholders: {@code {name}}, {@code {roll}}, {@code {batch}},
     * {@code {degree}}, {@code {section}}, {@code {email}}.
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
    public static String formatWithSession(String template) { return fmtWithSession(template); }
    public static String sessionFormat(String template) { return fmtWithSession(template); }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    // ============================================================== currency

    public static String currency(double amount) { return String.format(Locale.ENGLISH, "$%,.2f", amount); }
    public static String dollars(double amount) { return currency(amount); }
    public static String usd(double amount) { return currency(amount); }
    public static String money(double amount) { return currency(amount); }

    /** Key-value currency: {@code Format.money(v, Key.SYMBOL, "Rs.", Key.DECIMALS, 0)}. */
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
                        // WIDTH, PAD_CHAR, ALIGN, PREFIX, SUFFIX are not used for
                        // currency formatting; reserved for other key-value APIs.
                    }
                }
            }
        }
        String pattern = (useGrouping ? "%,." : "%.") + decimals + "f";
        return symbol + String.format(locale, pattern, amount);
    }
    public static String money(double amount, Object... keyValues) { return currency(amount, keyValues); }

    public static String rupees(double amount) { return String.format(Locale.ENGLISH, "Rs.%,.2f", amount); }
    public static String pkr(double amount) { return rupees(amount); }
    public static String pakRupees(double amount) { return rupees(amount); }

    public static String currencyOf(double amount, String symbol) {
        if (symbol == null) symbol = "$";
        return symbol + String.format(Locale.ENGLISH, "%,.2f", amount);
    }
    public static String moneyOf(double amount, String symbol) { return currencyOf(amount, symbol); }

    // ============================================================== percent

    public static String percent(double value) { return String.format(Locale.ENGLISH, "%.2f%%", value); }
    public static String pct(double value) { return percent(value); }
    public static String percentage(double value) { return percent(value); }

    public static String percent(double value, int places) {
        if (places < 0) places = 2;
        return String.format(Locale.ENGLISH, "%." + places + "f%%", value);
    }
    public static String pct(double value, int places) { return percent(value, places); }

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

    public static String decimal(double value, int places) {
        if (places < 0) places = 2;
        return String.format(Locale.ENGLISH, "%." + places + "f", value);
    }
    public static String fixed(double value, int places) { return decimal(value, places); }
    public static String toFixed(double value, int places) { return decimal(value, places); }
    public static String fixedDecimal(double v, int p) { return decimal(v, p); }

    // ============================================================== plural

    public static String plural(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }
    public static String countOf(int count, String singular, String plural) { return plural(count, singular, plural); }

    /** Auto-adds an "s" for count != 1. */
    public static String quantify(int count, String singular) {
        return plural(count, singular, singular + "s");
    }
    public static String countOf(int count, String singular) { return quantify(count, singular); }

    public static String items(int count) { return plural(count, "item", "items"); }
    public static String files(int count) { return plural(count, "file", "files"); }
    public static String lines(int count) { return plural(count, "line", "lines"); }
    public static String chars(int count) { return plural(count, "char", "chars"); }

    // ============================================================== lists

    public static <T> String list(List<T> items, String delimiter) {
        if (items == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(items.get(i));
        }
        return sb.toString();
    }
    public static <T> String join(List<T> items, String delimiter) { return list(items, delimiter); }
    public static <T> String itemsList(List<T> items, String delimiter) { return list(items, delimiter); }
    public static <T> String comma(List<T> items) { return list(items, ", "); }

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
    public static <T> String listWithLast(List<T> items, String d, String last) { return list(items, d, last); }

    // ============================================================== ordinal / roman

    public static String ordinal(int n) { return Numbers.ordinal(n); }
    public static String nth(int n) { return ordinal(n); }

    public static String roman(int n) { return Numbers.roman(n); }

    // ============================================================== bytes / filesize

    public static String bytes(long count) { return Numbers.fileSize(count); }
    public static String fileSize(long count) { return Numbers.fileSize(count); }
    public static String humanSize(long count) { return Numbers.fileSize(count); }

    // ============================================================== duration

    /** Human duration from milliseconds: "500ms", "5s", "1m 5s", "1h 1m 5s", "1d". */
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
    public static String humanDuration(long millis) { return duration(millis); }
    public static String timeSpan(long millis) { return duration(millis); }

    public static String duration(Duration d) {
        if (d == null) return "0ms";
        return duration(d.toMillis());
    }
    public static String humanDuration(Duration d) { return duration(d); }

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

    public static String pad(String value, int width) { return Text.padRight(value, width); }
    public static String padLeft(String value, int width) { return Text.padLeft(value, width); }
    public static String padRight(String value, int width) { return Text.padRight(value, width); }
    public static String center(String value, int width) { return Text.center(value, width); }

    public static String padded(String value, int width) { return Text.padRight(value, width); }

    public static String title(String value) { return Text.titleCase(value); }
    public static String capitalize(String v) { return Text.capitalize(v); }
    public static String upper(String v) { return v == null ? null : v.toUpperCase(Locale.ROOT); }
    public static String lower(String v) { return v == null ? null : v.toLowerCase(Locale.ROOT); }

    public static String mask(String value, int visible) { return Text.mask(value, visible); }
    public static String slug(String value) { return Text.slugify(value); }
    public static String truncate(String v, int max) { return Text.truncate(v, max); }

    // ============================================================== domain (PK)

    public static String cnic(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 13) return raw;
        return digits.substring(0, 5) + "-" + digits.substring(5, 12) + "-" + digits.substring(12);
    }
    public static String formatCnic(String raw) { return cnic(raw); }

    public static String pakistaniPhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("0092")) digits = digits.substring(4);
        else if (digits.startsWith("92") && digits.length() > 10) digits = digits.substring(2);
        else if (digits.startsWith("0") && digits.length() == 11) digits = digits.substring(1);
        if (digits.length() != 10) return raw;
        return "+92 " + digits.substring(0, 3) + " " + digits.substring(3);
    }
    public static String pkPhone(String raw) { return pakistaniPhone(raw); }

    // ============================================================== date/time delegation

    public static String date(java.time.LocalDate d) { return DateTime.humanize(d); }
    public static String date(java.time.LocalDate d, String pattern) { return DateTime.format(d, pattern); }
    public static String time(java.time.LocalTime t) { return DateTime.humanize(t); }
    public static String dateTime(java.time.LocalDateTime dt) { return DateTime.humanize(dt); }

    // ============================================================== names

    public static String fullName(String first, String last) {
        if (first == null && last == null) return "";
        if (first == null) return last;
        if (last == null) return first;
        return first.trim() + " " + last.trim();
    }

    public static String initials(String fullName) {
        return Text.initials(fullName);
    }
}
