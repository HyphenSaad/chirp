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
 * Fluent template formatter — the reusable, chainable counterpart to
 * {@link Format#fmt(String, Object...)}.
 *
 * <h2>Why this class exists</h2>
 * <p>{@code Format.fmt(...)} is fine for one-shot interpolation. But when
 * a template needs per-key transforms — this key as currency, that key as
 * percent, the ID zero-padded, the date in a specific format — the
 * {@code fmt(...)} calls become a soup of nested {@code Format.currency}
 * calls. {@code Formatter} turns that into a linear chain:</p>
 *
 * <pre>{@code
 * // Without Formatter — the value pipeline is buried inside the interpolation call
 * String raw = "Dear {name}, your order #{id} was {amount}, shipped {date}.";
 * String message = raw
 *     .replace("{name}", "Saad")
 *     .replace("{id}", String.format("%06d", 42))
 *     .replace("{amount}", Format.currency(1234.5))
 *     .replace("{date}", DateTime.format(shipDate, "MMM d, yyyy"));
 *
 * // With Formatter — the chain reads as a sequence of intentional steps
 * String message = Formatter
 *     .of("Dear {name}, your order #{id} was {amount}, shipped {date}.")
 *     .with("name", "Saad")
 *     .with("id", 42)
 *     .with("amount", 1234.5)
 *     .with("date", shipDate)
 *     .padLeft("id", 6, '0')
 *     .currency("amount")
 *     .date("date", "MMM d, yyyy")
 *     .format();
 * }</pre>
 *
 * <h2>The Builder + Decorator pattern</h2>
 * <p>{@code Formatter} is a live demonstration of two design patterns at
 * once:</p>
 * <ul>
 *   <li><b>Builder</b> — every {@code .with(...)} call accumulates state;
 *       the terminal {@code .format()} produces the final string.</li>
 *   <li><b>Decorator</b> — every transform method ({@code .currency},
 *       {@code .padLeft}, {@code .upper}, …) wraps the stored string with
 *       a transformation, following the Decorator pattern.</li>
 * </ul>
 * <p>The combination produces a chain where <b>every step is a real
 * transformation</b> — you can read the order of operations left-to-right
 * and reason about each one independently.</p>
 *
 * <h2>Chain order matters</h2>
 * <p>Unlike {@link Format#fmt(String, Object...)} where order does not
 * matter, the order of {@code Formatter} transforms <b>does</b> matter.</p>
 * <ul>
 *   <li>{@code .padLeft("id", 6, '0').currency("id")} — parses {@code "000042"}
 *       as a double, fails silently, string is unchanged.</li>
 *   <li>{@code .currency("id").padLeft("id", 6, '0')} — pads the currency
 *       string {@code "$42.00"} to 6 characters, giving {@code " $42.00"}.</li>
 * </ul>
 * <p>The {@code .currency} transform is a <b>no-op</b> if the value is
 * not parseable as a number. That is deliberate: chaining
 * {@code .currency("missing")} on a key that was never set does not throw,
 * it just skips the transform. This makes {@code Formatter} safe to use
 * with default-valued keys.</p>
 *
 * <h2>Null safety</h2>
 * <ul>
 *   <li>Binding a {@code null} value via {@code .with(...)} is a no-op —
 *       the key retains its previous value (or remains unset).</li>
 *   <li>Transforms on missing keys are no-ops.</li>
 *   <li>Transforms on non-parseable values are no-ops.</li>
 *   <li>A {@code null} template formats as empty string.</li>
 * </ul>
 *
 * <h2>Example — full order confirmation</h2>
 * <pre>{@code
 * String message = Formatter
 *     .of("Dear {name}, your order #{id} was {amount}, shipped {date}.")
 *     .with("name", "Saad")
 *     .with("id", 42)
 *     .with("amount", 1234.5)
 *     .with("date", LocalDate.of(2026, 10, 31))
 *     .padLeft("id", 6, '0')
 *     .currency("amount")
 *     .date("date", "MMM d, yyyy")
 *     .format();
 * // "Dear Saad, your order #000042 was $1,234.50, shipped Oct 31, 2026."
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the underlying formatting primitives, see {@link Format}. For
 * static interpolation, see {@link Format#fmt(String, Object...)}. For
 * the session-aware factory, see {@link Format#on(String)}.</p>
 *
 * @see Format
 * @see Session
 * @since 1.0.0
 */
public final class Formatter {

    private final String template;
    private final Map<String, String> values = new LinkedHashMap<>();

    private Formatter(String template) {
        this.template = template == null ? "" : template;
    }

    // ============================================================== factories

    /**
     * Opens a new formatter on the template.
     *
     * @param template the template with {@code {key}} placeholders (may be null)
     * @return a new {@code Formatter} wrapping {@code template}
     * @see Format#on(String)
     * @see #on(String)
     * @see #create(String)
     * @see #template(String)
     * @since 1.0.0
     */
    public static Formatter of(String template) { return new Formatter(template); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #of(String)
     * @since 1.0.0
     */
    public static Formatter on(String template) { return of(template); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #of(String)
     * @since 1.0.0
     */
    public static Formatter create(String template) { return of(template); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param template the template
     * @return a new {@code Formatter}
     * @see #of(String)
     * @since 1.0.0
     */
    public static Formatter template(String template) { return of(template); }

    // ============================================================== bind

    /**
     * Binds a value to a key. The value is converted with
     * {@link String#valueOf(Object)} when bound.
     *
     * <p>A {@code null} value is a no-op — the key retains its previous
     * value (or remains unset). This lets you chain {@code .with(...)}
     * calls unconditionally even when optional values are absent.</p>
     *
     * @param key the placeholder name (may be null — no-op)
     * @param value the value to bind (may be null — no-op)
     * @return this formatter, for chaining
     * @see #set(String, Object)
     * @see #put(String, Object)
     * @see #bind(String, Object)
     * @see #with(Map)
     * @see #withSession()
     * @since 1.0.0
     */
    public Formatter with(String key, Object value) {
        if (key != null && value != null) values.put(key, String.valueOf(value));
        return this;
    }

    /**
     * Alias of {@link #with(String, Object)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @param value the value to bind
     * @return this formatter
     * @see #with(String, Object)
     * @since 1.0.0
     */
    public Formatter set(String key, Object value) { return with(key, value); }

    /**
     * Alias of {@link #with(String, Object)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @param value the value to bind
     * @return this formatter
     * @see #with(String, Object)
     * @since 1.0.0
     */
    public Formatter put(String key, Object value) { return with(key, value); }

    /**
     * Alias of {@link #with(String, Object)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @param value the value to bind
     * @return this formatter
     * @see #with(String, Object)
     * @since 1.0.0
     */
    public Formatter bind(String key, Object value) { return with(key, value); }

    /**
     * Bulk-binds values from a map.
     *
     * <p>Iterates the map and calls {@link #with(String, Object)} for each
     * entry — {@code null} values are silently skipped, same as
     * {@code with}.</p>
     *
     * @param map the map of substitutions (may be null — no-op)
     * @return this formatter, for chaining
     * @see #with(String, Object)
     * @see #putAll(Map)
     * @since 1.0.0
     */
    public Formatter with(Map<String, ?> map) {
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) with(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Alias of {@link #with(Map)} — see that method for full
     * documentation.
     *
     * @param map the map of substitutions
     * @return this formatter
     * @see #with(Map)
     * @since 1.0.0
     */
    public Formatter putAll(Map<String, ?> map) { return with(map); }

    /**
     * Bulk-binds the current {@link Session}'s fields.
     *
     * <p>Binds {@code name}, {@code roll}, {@code batch}, {@code degree},
     * {@code section}, {@code email}, and {@code fullName} (as an alias of
     * name) from {@link Session#me()}. Unset Session fields are simply not
     * bound — the placeholder remains unsubstituted in the output.</p>
     *
     * <pre>{@code
     * Session.me().name("Saad").rollNumber("2024-CS-001");
     * String msg = Formatter
     *     .of("Student {name} ({roll})")
     *     .withSession()
     *     .format();
     * // "Student Saad (2024-CS-001)"
     * }</pre>
     *
     * @return this formatter, for chaining
     * @see Session
     * @see Format#fmtWithSession(String)
     * @since 1.0.0
     */
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

    /**
     * Formats the value bound to the key as US currency —
     * {@code "$1,234.50"}.
     *
     * <p>If the value is not parseable as a number, this is a no-op (the
     * string is left unchanged). If the key is not bound, this is also a
     * no-op.</p>
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #money(String)
     * @see #asCurrency(String)
     * @see #rupees(String)
     * @see Format#currency(double)
     * @since 1.0.0
     */
    public Formatter currency(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.currency(d));
        }
        return this;
    }

    /**
     * Alias of {@link #currency(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #currency(String)
     * @since 1.0.0
     */
    public Formatter money(String key) { return currency(key); }

    /**
     * Alias of {@link #currency(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #currency(String)
     * @since 1.0.0
     */
    public Formatter asCurrency(String key) { return currency(key); }

    /**
     * Formats the value bound to the key as Pakistani rupees —
     * {@code "Rs.1,234.50"}.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #pkr(String)
     * @see #currency(String)
     * @see Format#rupees(double)
     * @since 1.0.0
     */
    public Formatter rupees(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.rupees(d));
        }
        return this;
    }

    /**
     * Alias of {@link #rupees(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #rupees(String)
     * @since 1.0.0
     */
    public Formatter pkr(String key) { return rupees(key); }

    /**
     * Formats the value bound to the key as a percentage —
     * {@code "92.50%"}.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #pct(String)
     * @see #asPercent(String)
     * @see Format#percent(double)
     * @since 1.0.0
     */
    public Formatter percent(String key) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.percent(d));
        }
        return this;
    }

    /**
     * Alias of {@link #percent(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #percent(String)
     * @since 1.0.0
     */
    public Formatter pct(String key) { return percent(key); }

    /**
     * Alias of {@link #percent(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #percent(String)
     * @since 1.0.0
     */
    public Formatter asPercent(String key) { return percent(key); }

    /**
     * Formats the value bound to the key as a fixed-decimal number.
     *
     * @param key the placeholder name
     * @param places the number of decimal places
     * @return this formatter, for chaining
     * @see #fixed(String, int)
     * @see #asDecimal(String, int)
     * @see Format#decimal(double, int)
     * @since 1.0.0
     */
    public Formatter decimal(String key, int places) {
        if (values.containsKey(key)) {
            Double d = parseDouble(values.get(key));
            if (d != null) values.put(key, Format.decimal(d, places));
        }
        return this;
    }

    /**
     * Alias of {@link #decimal(String, int)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @param places the number of decimal places
     * @return this formatter
     * @see #decimal(String, int)
     * @since 1.0.0
     */
    public Formatter fixed(String key, int places) { return decimal(key, places); }

    /**
     * Alias of {@link #decimal(String, int)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @param places the number of decimal places
     * @return this formatter
     * @see #decimal(String, int)
     * @since 1.0.0
     */
    public Formatter asDecimal(String key, int places) { return decimal(key, places); }

    // ============================================================== string transforms

    /**
     * Uppercases the value bound to the key (uses
     * {@link java.util.Locale#ROOT}).
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #uppercase(String)
     * @see #toUpper(String)
     * @see #lower(String)
     * @since 1.0.0
     */
    public Formatter upper(String key) {
        if (values.containsKey(key)) values.put(key, Format.upper(values.get(key)));
        return this;
    }

    /**
     * Alias of {@link #upper(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #upper(String)
     * @since 1.0.0
     */
    public Formatter uppercase(String key) { return upper(key); }

    /**
     * Alias of {@link #upper(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #upper(String)
     * @since 1.0.0
     */
    public Formatter toUpper(String key) { return upper(key); }

    /**
     * Lowercases the value bound to the key (uses
     * {@link java.util.Locale#ROOT}).
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #lowercase(String)
     * @see #toLower(String)
     * @see #upper(String)
     * @since 1.0.0
     */
    public Formatter lower(String key) {
        if (values.containsKey(key)) values.put(key, Format.lower(values.get(key)));
        return this;
    }

    /**
     * Alias of {@link #lower(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #lower(String)
     * @since 1.0.0
     */
    public Formatter lowercase(String key) { return lower(key); }

    /**
     * Alias of {@link #lower(String)} — see that method for full
     * documentation.
     *
     * @param key the placeholder name
     * @return this formatter
     * @see #lower(String)
     * @since 1.0.0
     */
    public Formatter toLower(String key) { return lower(key); }

    /**
     * Capitalises the first letter and lowercases the rest of the value
     * bound to the key.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #title(String)
     * @see Text#capitalize(String)
     * @since 1.0.0
     */
    public Formatter capitalize(String key) {
        if (values.containsKey(key)) values.put(key, Text.capitalize(values.get(key)));
        return this;
    }

    /**
     * Converts the value to title case.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #capitalize(String)
     * @see Text#titleCase(String)
     * @since 1.0.0
     */
    public Formatter title(String key) {
        if (values.containsKey(key)) values.put(key, Text.titleCase(values.get(key)));
        return this;
    }

    /**
     * Trims leading and trailing whitespace from the value.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @since 1.0.0
     */
    public Formatter trim(String key) {
        if (values.containsKey(key)) {
            String v = values.get(key);
            values.put(key, v == null ? "" : v.trim());
        }
        return this;
    }

    /**
     * Truncates the value to at most {@code max} characters, appending
     * an ellipsis if truncation occurred.
     *
     * @param key the placeholder name
     * @param max the maximum output length
     * @return this formatter, for chaining
     * @see Text#truncate(String, int)
     * @since 1.0.0
     */
    public Formatter truncate(String key, int max) {
        if (values.containsKey(key)) values.put(key, Text.truncate(values.get(key), max));
        return this;
    }

    /**
     * Masks all but the last {@code visible} characters of the value.
     *
     * @param key the placeholder name
     * @param visible the number of trailing characters to keep
     * @return this formatter, for chaining
     * @see Text#mask(String, int)
     * @since 1.0.0
     */
    public Formatter mask(String key, int visible) {
        if (values.containsKey(key)) values.put(key, Text.mask(values.get(key), visible));
        return this;
    }

    /**
     * Converts the value to a URL-safe slug.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see Text#slugify(String)
     * @since 1.0.0
     */
    public Formatter slug(String key) {
        if (values.containsKey(key)) values.put(key, Text.slugify(values.get(key)));
        return this;
    }

    // ============================================================== padding

    /**
     * Left-pads the value with spaces to reach {@code width} characters.
     *
     * @param key the placeholder name
     * @param width the target width
     * @return this formatter, for chaining
     * @see #padRight(String, int)
     * @see #center(String, int)
     * @see #padLeft(String, int, char)
     * @see Text#padLeft(String, int)
     * @since 1.0.0
     */
    public Formatter padLeft(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.padLeft(values.get(key), width));
        return this;
    }

    /**
     * Right-pads the value with spaces to reach {@code width} characters.
     *
     * @param key the placeholder name
     * @param width the target width
     * @return this formatter, for chaining
     * @see #padLeft(String, int)
     * @see Text#padRight(String, int)
     * @since 1.0.0
     */
    public Formatter padRight(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.padRight(values.get(key), width));
        return this;
    }

    /**
     * Centers the value within {@code width} characters.
     *
     * @param key the placeholder name
     * @param width the target width
     * @return this formatter, for chaining
     * @see #padLeft(String, int)
     * @see Text#center(String, int)
     * @since 1.0.0
     */
    public Formatter center(String key, int width) {
        if (values.containsKey(key)) values.put(key, Text.center(values.get(key), width));
        return this;
    }

    /**
     * Left-pads the value with a specific fill character to reach
     * {@code width} characters.
     *
     * <p>Commonly used for zero-padding IDs:
     * {@code .padLeft("id", 6, '0')} produces {@code "000042"} from
     * {@code "42"}.</p>
     *
     * @param key the placeholder name
     * @param width the target width
     * @param fill the padding character
     * @return this formatter, for chaining
     * @see #padLeft(String, int)
     * @since 1.0.0
     */
    public Formatter padLeft(String key, int width, char fill) {
        if (!values.containsKey(key)) return this;
        String v = values.get(key);
        if (v == null) v = "";
        while (v.length() < width) v = fill + v;
        values.put(key, v);
        return this;
    }

    /**
     * Right-pads the value with a specific fill character to reach
     * {@code width} characters.
     *
     * @param key the placeholder name
     * @param width the target width
     * @param fill the padding character
     * @return this formatter, for chaining
     * @see #padRight(String, int)
     * @since 1.0.0
     */
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

    /**
     * Reformats the value bound to the key as a date with the given
     * pattern.
     *
     * <p>The value is parsed via {@link DateTime#parseDate(String)} first.
     * If it does not parse as a date, this is a no-op.</p>
     *
     * @param key the placeholder name
     * @param pattern the output format pattern
     * @return this formatter, for chaining
     * @see #humanDate(String)
     * @see #time(String, String)
     * @see #dateTime(String, String)
     * @see DateTime#format(LocalDate, String)
     * @since 1.0.0
     */
    public Formatter date(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        String v = values.get(key);
        LocalDate d = DateTime.parseDate(v);
        if (d != null) values.put(key, DateTime.format(d, pattern));
        return this;
    }

    /**
     * Reformats the value as a human-readable date.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see #date(String, String)
     * @see DateTime#humanize(LocalDate)
     * @since 1.0.0
     */
    public Formatter humanDate(String key) {
        if (!values.containsKey(key)) return this;
        LocalDate d = DateTime.parseDate(values.get(key));
        if (d != null) values.put(key, DateTime.humanize(d));
        return this;
    }

    /**
     * Reformats the value as a time with the given pattern.
     *
     * @param key the placeholder name
     * @param pattern the output format pattern
     * @return this formatter, for chaining
     * @see #dateTime(String, String)
     * @see DateTime#format(LocalTime, String)
     * @since 1.0.0
     */
    public Formatter time(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        LocalTime t = DateTime.parseTime(values.get(key));
        if (t != null) values.put(key, DateTime.format(t, pattern));
        return this;
    }

    /**
     * Reformats the value as a date-time with the given pattern.
     *
     * @param key the placeholder name
     * @param pattern the output format pattern
     * @return this formatter, for chaining
     * @see #date(String, String)
     * @see #time(String, String)
     * @see DateTime#format(LocalDateTime, String)
     * @since 1.0.0
     */
    public Formatter dateTime(String key, String pattern) {
        if (!values.containsKey(key)) return this;
        LocalDateTime dt = DateTime.parseDateTime(values.get(key));
        if (dt != null) values.put(key, DateTime.format(dt, pattern));
        return this;
    }

    // ============================================================== domain

    /**
     * Reformats the value as a Pakistani CNIC with dashes.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see Format#cnic(String)
     * @since 1.0.0
     */
    public Formatter cnic(String key) {
        if (values.containsKey(key)) values.put(key, Format.cnic(values.get(key)));
        return this;
    }

    /**
     * Reformats the value as a Pakistani mobile number.
     *
     * @param key the placeholder name
     * @return this formatter, for chaining
     * @see Format#pakistaniPhone(String)
     * @since 1.0.0
     */
    public Formatter pakistaniPhone(String key) {
        if (values.containsKey(key)) values.put(key, Format.pakistaniPhone(values.get(key)));
        return this;
    }

    // ============================================================== defaults

    /**
     * Provides a fallback for a key whose value is missing or empty.
     *
     * <p>Runs immediately when called. If the key is unbound, or bound to
     * an empty string, the fallback becomes the value. If the key already
     * has a non-empty value, the fallback is ignored.</p>
     *
     * <pre>{@code
     * Formatter.of("Hi {name}")
     *     .defaultValue("name", "friend")
     *     .format();     // "Hi friend"
     *
     * Formatter.of("Hi {name}")
     *     .with("name", "Saad")
     *     .defaultValue("name", "friend")
     *     .format();     // "Hi Saad"
     * }</pre>
     *
     * @param key the placeholder name (may be null — no-op)
     * @param fallback the fallback value (may be null — treated as empty)
     * @return this formatter, for chaining
     * @see #orElse(String, String)
     * @see #fallback(String, String)
     * @since 1.0.0
     */
    public Formatter defaultValue(String key, String fallback) {
        if (key == null) return this;
        String current = values.get(key);
        if (current == null || current.isEmpty()) {
            values.put(key, fallback == null ? "" : fallback);
        }
        return this;
    }

    /**
     * Alias of {@link #defaultValue(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param key the placeholder name
     * @param fallback the fallback value
     * @return this formatter
     * @see #defaultValue(String, String)
     * @since 1.0.0
     */
    public Formatter orElse(String key, String fallback) { return defaultValue(key, fallback); }

    /**
     * Alias of {@link #defaultValue(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param key the placeholder name
     * @param fallback the fallback value
     * @return this formatter
     * @see #defaultValue(String, String)
     * @since 1.0.0
     */
    public Formatter fallback(String key, String fallback) { return defaultValue(key, fallback); }

    // ============================================================== terminals

    /**
     * Runs the interpolation and returns the final string.
     *
     * <p>Every bound key is substituted. Keys that were never bound remain
     * as literal {@code {key}} in the output — this makes missing values
     * visible rather than silently dropping them.</p>
     *
     * @return the interpolated string
     * @see #result()
     * @see #render()
     * @see #build()
     * @see #asString()
     * @see #get()
     * @see #toString()
     * @since 1.0.0
     */
    public String format() {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
    public String result() { return format(); }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
    public String render() { return format(); }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
    public String build() { return format(); }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
    public String asString() { return format(); }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
    public String get() { return format(); }

    /**
     * Returns a snapshot of the currently-bound values.
     *
     * <p>The returned map is an immutable copy — subsequent
     * {@code .with(...)} calls do not affect it.</p>
     *
     * @return an immutable snapshot of the bindings
     * @since 1.0.0
     */
    public Map<String, String> values() {
        return Map.copyOf(values);
    }

    /**
     * Alias of {@link #format()} — see that method for full documentation.
     *
     * @return the interpolated string
     * @see #format()
     * @since 1.0.0
     */
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
