package chirp.format;

import chirp.datetime.DateTime;
import chirp.session.Session;
import chirp.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent template formatter — the fluent counterpart to
 * {@link Format#fmt(String, Object...)}.
 *
 * <h2>Why it exists</h2>
 * <p>{@code fmt(...)} is fine for one-shot interpolation. {@code Formatter}
 * is for templates you reuse or that need per-key transforms ("format this
 * one as currency, that one as percent, and pad the ID to 5 characters").
 * It's the <b>Builder</b> and <b>Decorator</b> patterns in one class —
 * the same operation chain you write is the one that gets executed.</p>
 *
 * <h2>Aliases</h2>
 * <ul>
 *   <li>{@code with} / {@code set} / {@code put} / {@code bind}</li>
 *   <li>{@code format} / {@code result} / {@code render} / {@code build} /
 *       {@code asString} / {@code get}</li>
 *   <li>{@code currency} / {@code money} / {@code asCurrency}</li>
 *   <li>{@code percent} / {@code pct} / {@code asPercent}</li>
 *   <li>{@code decimal} / {@code fixed} / {@code asDecimal}</li>
 *   <li>{@code upper} / {@code uppercase} / {@code toUpper}</li>
 *   <li>{@code lower} / {@code lowercase} / {@code toLower}</li>
 * </ul>
 *
 * <pre>{@code
 * String message = Formatter
 *     .of("Dear {name}, your order #{orderId} was {amount}, shipped {shipDate}.")
 *     .with("name", "Saad")
 *     .with("orderId", 42)
 *     .with("amount", 1234.5)
 *     .with("shipDate", LocalDate.now())
 *     .padLeft("orderId", 6, '0')
 *     .currency("amount")
 *     .date("shipDate", "MMM d, yyyy")
 *     .format();
 * // "Dear Saad, your order #000042 was $1,234.50, shipped Oct 31, 2026."
 * }</pre>
 *
 * <p>Every method is null-safe; null values interpolate to empty strings.
 * {@link #with(Map)} and {@link #withSession()} bulk-bind in one call.</p>
 */
public final class Formatter {

    private final String template;
    private final Map<String, String> values = new LinkedHashMap<>();

    private Formatter(String template) {
        this.template = template == null ? "" : template;
    }

    // ============================================================== factories

    public static Formatter of(String template) { return new Formatter(template); }
    public static Formatter on(String template) { return of(template); }
    public static Formatter create(String template) { return of(template); }
    public static Formatter template(String template) { return of(template); }

    // ============================================================== bind

    public Formatter with(String key, Object value) {
        if (key != null && value != null) values.put(key, String.valueOf(value));
        return this;
    }
    public Formatter set(String key, Object value) { return with(key, value); }
    public Formatter put(String key, Object value) { return with(key, value); }
    public Formatter bind(String key, Object value) { return with(key, value); }

    /** Bulk-bind from a map. */
    public Formatter with(Map<String, ?> map) {
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) with(e.getKey(), e.getValue());
        }
        return this;
    }
    public Formatter putAll(Map<String, ?> map) { return with(map); }

    /** Bulk-bind the current {@link Session}'s fields. */
    public Formatter withSession() {
        Session s = Session.me();
        with("name", s.name());
        with("roll", s.rollNumber());
        with("batch", s.batch());
        with("degree", s.degree());
        with("section", s.section());
        with("email", s.email());
        with("fullName", s.name());
        return this;
    }

    // ============================================================== numeric formatting

    public Formatter currency(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.currency(d));
        }
        return this;
    }
    public Formatter money(String key) { return currency(key); }
    public Formatter asCurrency(String key) { return currency(key); }

    public Formatter rupees(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.rupees(d));
        }
        return this;
    }
    public Formatter pkr(String key) { return rupees(key); }

    public Formatter percent(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.percent(d));
        }
        return this;
    }
    public Formatter pct(String key) { return percent(key); }
    public Formatter asPercent(String key) { return percent(key); }

    public Formatter decimal(String key, int places) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.decimal(d, places));
        }
        return this;
    }
    public Formatter fixed(String key, int places) { return decimal(key, places); }
    public Formatter asDecimal(String key, int places) { return decimal(key, places); }

    // ============================================================== string transforms

    public Formatter upper(String key) {
        if (values.containsKey(key)) values.put(key, Format.upper(values.get(key)));
        return this;
    }
    public Formatter uppercase(String key) { return upper(key); }
    public Formatter toUpper(String key) { return upper(key); }

    public Formatter lower(String key) {
        if (values.containsKey(key)) values.put(key, Format.lower(values.get(key)));
        return this;
    }
    public Formatter lowercase(String key) { return lower(key); }
    public Formatter toLower(String key) { return lower(key); }

    public Formatter capitalize(String key) {
        if (values.containsKey(key)) values.put(key, Text.capitalize(values.get(key)));
        return this;
    }
    public Formatter title(String key) {
        if (values.containsKey(key)) values.put(key, Text.titleCase(values.get(key)));
        return this;
    }

    public Formatter trim(String key) {
        if (values.containsKey(key)) {
            String v = values.get(key);
            values.put(key, v == null ? "" : v.trim());
        }
        return this;
    }

    public Formatter truncate(String key, int max) {
        if (values.containsKey(key)) values.put(key, Text.truncate(values.get(key), max));
        return this;
    }

    public Formatter mask(String key, int visible) {
        if (values.containsKey(key)) values.put(key, Text.mask(values.get(key), visible));
        return this;
    }

    public Formatter slug(String key) {
        if (values.containsKey(key)) values.put(key, Text.slugify(values.get(key)));
        return this;
    }

    // ============================================================== padding

    public Formatter padLeft(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.padLeft(values.get(key), width));
        return this;
    }
    public Formatter padRight(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.padRight(values.get(key), width));
        return this;
    }
    public Formatter center(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.center(values.get(key), width));
        return this;
    }

    /** Pad a value on the left with a specific fill character. */
    public Formatter padLeft(String key, int width, char fill) {
        if (!values.containsKey(key)) return this;
        String v = values.get(key);
        if (v == null) v = "";
        while (v.length() < width) v = fill + v;
        values.put(key, v);
        return this;
    }

    /** Pad a value on the right with a specific fill character. */
    public Formatter padRight(String key, int width, char fill) {
        if (!values.containsKey(key)) return this;
        String v = values.get(key);
        if (v == null) v = "";
        StringBuilder sb = new StringBuilder(v);
        while (sb.length() < width) sb.append(fill);
        values.put(key, sb.toString());
        return this;
    }

    // ============================================================== date/time formatting

    public Formatter date(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        // If the value was already a LocalDate/LocalDateTime when bound, we
        // stored it as a string. Try to parse-and-reformat.
        String v = values.get(key);
        LocalDate d = DateTime.parseDate(v);
        if (d != null) values.put(key, DateTime.format(d, pattern));
        return this;
    }

    public Formatter humanDate(String key) {
        if (!values.containsKey(key)) return this;
        LocalDate d = DateTime.parseDate(values.get(key));
        if (d != null) values.put(key, DateTime.humanize(d));
        return this;
    }

    public Formatter time(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        LocalTime t = DateTime.parseTime(values.get(key));
        if (t != null) values.put(key, DateTime.format(t, pattern));
        return this;
    }

    public Formatter dateTime(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        LocalDateTime dt = DateTime.parseDateTime(values.get(key));
        if (dt != null) values.put(key, DateTime.format(dt, pattern));
        return this;
    }

    // ============================================================== domain

    public Formatter cnic(String key) {
        if (values.containsKey(key)) values.put(key, Format.cnic(values.get(key)));
        return this;
    }

    public Formatter pakistaniPhone(String key) {
        if (values.containsKey(key)) values.put(key, Format.pakistaniPhone(values.get(key)));
        return this;
    }

    // ============================================================== defaults

    /** Provides a fallback when the value is missing or empty. */
    public Formatter defaultValue(String key, String fallback) {
        if (key == null) return this;
        String current = values.get(key);
        if (current == null || current.isEmpty()) {
            values.put(key, fallback == null ? "" : fallback);
        }
        return this;
    }
    public Formatter orElse(String key, String fallback) { return defaultValue(key, fallback); }
    public Formatter fallback(String key, String fallback) { return defaultValue(key, fallback); }

    // ============================================================== terminals

    /** Runs the interpolation and returns the final string. */
    public String format() {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    public String result() { return format(); }
    public String render() { return format(); }
    public String build() { return format(); }
    public String asString() { return format(); }
    public String get() { return format(); }

    /** @return a copy of the currently-bound values (immutable). */
    public Map<String, String> values() {
        return Map.copyOf(values);
    }

    @Override
    public String toString() { return format(); }

    // ============================================================== helpers

    private static Double parseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
