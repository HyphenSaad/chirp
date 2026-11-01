package chirp.mail;

import chirp.io.TextFileIO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML email template with declared placeholders.
 *
 * <p>A template is an HTML document with named placeholders embedded in
 * the markup. You declare each placeholder with a value, then call
 * {@link #render()} to produce the final HTML. This is how a
 * personalised email is built without string concatenation, so the
 * template stays maintainable and the code stays free of
 * {@code "<p>" + name + "</p>"}.</p>
 *
 * <h2>Placeholder syntax</h2>
 * <p>Two styles are supported, and they can be mixed in the same
 * template:</p>
 * <ul>
 *   <li>{@code {{name}}} — Mustache-style</li>
 *   <li>{@code ${name}} — shell/JSP-style</li>
 * </ul>
 *
 * <pre>{@code
 * <p>Hello {{name}},</p>
 * <p>Your order ${orderId} has shipped.</p>
 * }</pre>
 *
 * <p>Placeholder names must be alphanumeric (letters, digits,
 * underscores) — anything else throws {@link EmailException} with a
 * message pointing at the offending name.</p>
 *
 * <h2>Strict validation</h2>
 * <p>Chirp's email templates are validated strictly, because a
 * half-rendered email reaching a user is worse than a loud failure at
 * development time. {@link #render()} throws when:</p>
 * <ul>
 *   <li>The content doesn't look like HTML (no tags detected)</li>
 *   <li>A declared placeholder never appears in the template — this is
 *       almost always a typo in the code, and the message lists which
 *       placeholders the template actually contains</li>
 *   <li>The template uses a placeholder that wasn't declared — the
 *       message names the missing declaration and shows the exact
 *       {@code .placeholder(...)} call to add</li>
 * </ul>
 *
 * <p>If throwing isn't what the caller wants — for example, a preview
 * screen that should show a fallback rather than a stack trace — the
 * safe-render methods return an {@code Optional} or a fallback
 * string:</p>
 * <ul>
 *   <li>{@link #renderOrDefault(String)} — returns the fallback on any
 *       validation error</li>
 *   <li>{@link #renderIfValid()} — returns {@link Optional#empty()} on
 *       any validation error</li>
 * </ul>
 *
 * <h2>Example — load, declare, render</h2>
 * <pre>{@code
 * // File: welcome.html
 * //   <h1>Welcome, {{name}}!</h1>
 * //   <p>Your account is ready. Login at ${loginUrl}.</p>
 *
 * HtmlTemplate template = HtmlTemplate.of("welcome.html")
 *     .placeholder("name", "Saad")
 *     .placeholder("loginUrl", "https://app.example.com");
 *
 * String html = template.render();
 * }</pre>
 *
 * <h2>Example — from a raw string (for tests)</h2>
 * <pre>{@code
 * HtmlTemplate inline = HtmlTemplate.raw("<p>Hi {{name}}</p>")
 *     .placeholder("name", "Ali");
 * // inline.render() == "<p>Hi Ali</p>"
 * }</pre>
 *
 * <h2>Integration with {@link EmailRequest}</h2>
 * <p>{@code EmailRequest} accepts a template directly:
 * {@code Email.builder().template(path).send()}. Or with a
 * pre-configured template:
 * {@code Email.builder().htmlTemplate(template).send()}. When the email
 * has both {@code .text(...)} and a template, the rendered HTML goes in
 * the {@code text/html} part and the plain text goes in the
 * {@code text/plain} part — see {@link EmailRequest} for the MIME
 * details.</p>
 *
 * <h2>Related</h2>
 * <p>For the request that consumes this template, see
 * {@link EmailRequest#htmlTemplate(HtmlTemplate)}. For a plain HTML
 * string without placeholders, use {@link EmailRequest#html(String)}.</p>
 *
 * @see EmailRequest
 * @see EmailException
 * @since 1.0.0
 */
public final class HtmlTemplate {

    private static final Pattern BRACED = Pattern.compile("\\{\\{(\\w+)}}");
    private static final Pattern DOLLARED = Pattern.compile("\\$\\{(\\w+)}");
    private static final Pattern HTML_TAG = Pattern.compile("<\\s*([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>");

    private final String path;
    private final String source;
    private final Map<String, String> values = new LinkedHashMap<>();

    private HtmlTemplate(String path, String source) {
        this.path = path;
        this.source = source;
    }

    // ============================================================== factories

    /**
     * Loads a template from a file. The file must exist, be readable, and
     * contain at least one HTML tag and a non-empty body.
     *
     * @param path the file path (may be null → throws)
     * @return the loaded template
     * @throws EmailException if the file is missing, empty, or unreadable
     * @see #load(String)
     * @see #raw(String)
     * @see #inline(String)
     * @since 1.0.0
     */
    public static HtmlTemplate of(String path) { return load(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the loaded template
     * @see #of(String)
     * @since 1.0.0
     */
    public static HtmlTemplate from(String path) { return load(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the loaded template
     * @see #of(String)
     * @since 1.0.0
     */
    public static HtmlTemplate read(String path) { return load(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the loaded template
     * @see #of(String)
     * @since 1.0.0
     */
    public static HtmlTemplate open(String path) { return load(path); }

    /**
     * Loads a template from a file. See {@link #of(String)} for the
     * canonical description — this is the underlying implementation the
     * aliases delegate to.
     *
     * @param path the file path (may be null or blank → throws)
     * @return the loaded template
     * @throws EmailException if the file is missing, empty, or unreadable
     * @see #of(String)
     * @since 1.0.0
     */
    public static HtmlTemplate load(String path) {
        if (path == null || path.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "HTML template path is required");
        }
        // TextFileIO.readAll never returns null: it returns "" on failure.
        String content = TextFileIO.readAll(path);
        if (content.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "HTML template is empty or missing: " + path);
        }
        return new HtmlTemplate(path, content);
    }

    /**
     * Wraps a raw HTML string as a template. Useful for tests and for
     * templates that are assembled programmatically rather than read
     * from a file.
     *
     * @param html the HTML content (null or blank → throws)
     * @return the template
     * @throws EmailException if {@code html} is null or blank
     * @see #inline(String)
     * @see #of(String)
     * @since 1.0.0
     */
    public static HtmlTemplate raw(String html) {
        return inline(html);
    }

    /**
     * Alias of {@link #raw(String)} — see that method for full
     * documentation and examples.
     *
     * @param html the HTML content
     * @return the template
     * @see #raw(String)
     * @since 1.0.0
     */
    public static HtmlTemplate inline(String html) {
        if (html == null || html.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "HTML content is required");
        }
        return new HtmlTemplate("<inline>", html);
    }

    // ============================================================== placeholder

    /**
     * Declares a placeholder value. Every placeholder in the template
     * must be declared before {@link #render()} will succeed.
     *
     * <p>The value is converted to a string via {@code String.valueOf};
     * a null value becomes the empty string. Numbers and dates work as
     * expected but formatting should be applied by the caller —
     * {@code String.format("%.2f", 3.14)} rather than relying on
     * {@code Double.toString}.</p>
     *
     * <pre>{@code
     * template
     *     .placeholder("name", "Saad")
     *     .placeholder("count", 5)
     *     .placeholder("price", String.format("$%,.2f", 1234.5));
     * }</pre>
     *
     * @param name the placeholder name (alphanumeric; null or blank → throws)
     * @param value the value (null → empty string)
     * @return this template, for chaining
     * @throws EmailException if the name is null, blank, or contains
     * characters other than letters, digits, and underscores
     * @see #set(String, Object)
     * @see #bind(String, Object)
     * @see #with(String, Object)
     * @see #fill(String, Object)
     * @see #placeholders(Map)
     * @since 1.0.0
     */
    public HtmlTemplate placeholder(String name, Object value) {
        if (name == null || name.isBlank()) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Placeholder name is required");
        }
        if (!name.matches("\\w+")) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Invalid placeholder name '" + name
                            + "' — must be letters/digits/underscore");
        }
        values.put(name, value == null ? "" : String.valueOf(value));
        return this;
    }

    /**
     * Overload of {@link #placeholder(String, Object)} — see that method
     * for full documentation. This version is chosen for {@code String}
     * arguments so the compiler does not box.
     *
     * @param name the placeholder name
     * @param value the value (null → empty string)
     * @return this template
     * @see #placeholder(String, Object)
     * @since 1.0.0
     */
    public HtmlTemplate placeholder(String name, String value) { return placeholder(name, (Object) value); }

    /**
     * Alias of {@link #placeholder(String, Object)} — see that method for
     * full documentation and examples.
     *
     * @param name the placeholder name
     * @param value the value
     * @return this template
     * @see #placeholder(String, Object)
     * @since 1.0.0
     */
    public HtmlTemplate set(String name, Object value) { return placeholder(name, value); }

    /**
     * Alias of {@link #placeholder(String, Object)} — see that method for
     * full documentation and examples.
     *
     * @param name the placeholder name
     * @param value the value
     * @return this template
     * @see #placeholder(String, Object)
     * @since 1.0.0
     */
    public HtmlTemplate bind(String name, Object value) { return placeholder(name, value); }

    /**
     * Alias of {@link #placeholder(String, Object)} — see that method for
     * full documentation and examples.
     *
     * @param name the placeholder name
     * @param value the value
     * @return this template
     * @see #placeholder(String, Object)
     * @since 1.0.0
     */
    public HtmlTemplate with(String name, Object value) { return placeholder(name, value); }

    /**
     * Alias of {@link #placeholder(String, Object)} — see that method for
     * full documentation and examples.
     *
     * @param name the placeholder name
     * @param value the value
     * @return this template
     * @see #placeholder(String, Object)
     * @since 1.0.0
     */
    public HtmlTemplate fill(String name, Object value) { return placeholder(name, value); }

    /**
     * Declares multiple placeholders from a map in one call.
     *
     * <pre>{@code
     * Map<String, Object> values = Map.of(
     *     "name", "Saad",
     *     "orderId", 42,
     *     "amount", String.format("$%,.2f", 1234.5));
     * template.placeholders(values);
     * }</pre>
     *
     * @param map the map of name → value (null → no-op)
     * @return this template, for chaining
     * @see #placeholder(String, Object)
     * @see #setAll(Map)
     * @see #bindAll(Map)
     * @see #fillAll(Map)
     * @since 1.0.0
     */
    public HtmlTemplate placeholders(Map<String, ?> map) {
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) placeholder(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Alias of {@link #placeholders(Map)} — see that method for full
     * documentation and examples.
     *
     * @param map the map of name → value
     * @return this template
     * @see #placeholders(Map)
     * @since 1.0.0
     */
    public HtmlTemplate setAll(Map<String, ?> map) { return placeholders(map); }

    /**
     * Alias of {@link #placeholders(Map)} — see that method for full
     * documentation and examples.
     *
     * @param map the map of name → value
     * @return this template
     * @see #placeholders(Map)
     * @since 1.0.0
     */
    public HtmlTemplate bindAll(Map<String, ?> map) { return placeholders(map); }

    /**
     * Alias of {@link #placeholders(Map)} — see that method for full
     * documentation and examples.
     *
     * @param map the map of name → value
     * @return this template
     * @see #placeholders(Map)
     * @since 1.0.0
     */
    public HtmlTemplate fillAll(Map<String, ?> map) { return placeholders(map); }

    // ============================================================== render

    /**
     * Validates the template and returns the rendered HTML.
     *
     * <p>Validation is strict — see the class-level doc for the exact
     * rules. If any rule fails, throws {@link EmailException} with a
     * message that names the offending placeholder or the reason the
     * content was rejected. Use {@link #renderOrDefault(String)} or
     * {@link #renderIfValid()} when throwing isn't appropriate.</p>
     *
     * @return the rendered HTML
     * @throws EmailException if the template has undeclared or unused
     * placeholders, or does not look like HTML
     * @see #renderOrDefault(String)
     * @see #renderIfValid()
     * @see #verify()
     * @since 1.0.0
     */
    public String render() {
        verify();
        String result = source;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Alias of {@link #render()} — see that method for full documentation
     * and examples.
     *
     * @return the rendered HTML
     * @see #render()
     * @since 1.0.0
     */
    public String format() { return render(); }

    /**
     * Alias of {@link #render()} — see that method for full documentation
     * and examples.
     *
     * @return the rendered HTML
     * @see #render()
     * @since 1.0.0
     */
    public String build() { return render(); }

    /**
     * Alias of {@link #render()} — see that method for full documentation
     * and examples.
     *
     * @return the rendered HTML
     * @see #render()
     * @since 1.0.0
     */
    @Override
    public String toString() { return render(); }

    /**
     * Renders with a fallback — returns {@code fallback} on any
     * validation error instead of throwing.
     *
     * <p>Useful for preview screens, logging, and any place where a
     * visible error string is preferred over a stack trace. The fallback
     * itself is returned as-is — it is not HTML-escaped, so a caller who
     * uses untrusted strings should escape them first.</p>
     *
     * @param fallback the string to return on error (null → empty)
     * @return the rendered HTML, or the fallback on error
     * @see #render()
     * @see #renderOr(String)
     * @since 1.0.0
     */
    public String renderOrDefault(String fallback) {
        try {
            return render();
        } catch (EmailException e) {
            return fallback == null ? "" : fallback;
        }
    }

    /**
     * Alias of {@link #renderOrDefault(String)} — see that method for
     * full documentation and examples.
     *
     * @param fallback the fallback string
     * @return the rendered HTML, or the fallback
     * @see #renderOrDefault(String)
     * @since 1.0.0
     */
    public String renderOr(String fallback) { return renderOrDefault(fallback); }

    /**
     * Renders safely — returns {@link Optional#empty()} on any validation
     * error instead of throwing.
     *
     * <p>Idiomatic when the caller already deals with optionality:</p>
     * <pre>{@code
     * template.renderIfValid().ifPresentOrElse(
     *     html -> println("Rendered " + html.length() + " chars"),
     *     () -> warn("Template invalid"));
     * }</pre>
     *
     * @return the rendered HTML, or empty on error
     * @see #render()
     * @see #tryRender()
     * @since 1.0.0
     */
    public Optional<String> renderIfValid() {
        try {
            return Optional.of(render());
        } catch (EmailException e) {
            return Optional.empty();
        }
    }

    /**
     * Alias of {@link #renderIfValid()} — see that method for full
     * documentation and examples.
     *
     * @return the rendered HTML, or empty
     * @see #renderIfValid()
     * @since 1.0.0
     */
    public Optional<String> tryRender() { return renderIfValid(); }

    // ============================================================== verify

    /**
     * Validates the template without rendering it — throws on any issue.
     *
     * <p>Same rules as {@link #render()}, but does not perform the string
     * substitution. Useful for validating a template at load time, so
     * that a "missing placeholder" error surfaces before the send
     * method is called.</p>
     *
     * @throws EmailException if the template has undeclared or unused
     * placeholders, or does not look like HTML
     * @see #render()
     * @see #validate()
     * @see #check()
     * @since 1.0.0
     */
    public void verify() {
        if (!isHtml(source)) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Template does not look like HTML (no tags found): " + path);
        }

        Map<String, Integer> found = discoverPlaceholders(source);

        for (String name : values.keySet()) {
            if (!found.containsKey(name)) {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Declared placeholder '" + name + "' not found in " + path
                                + ". Available in template: " + found.keySet());
            }
        }
        for (String name : found.keySet()) {
            if (!values.containsKey(name)) {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Undeclared placeholder '" + name + "' found in " + path
                                + ". Declare it with .placeholder(\"" + name + "\", value)");
            }
        }
    }

    /**
     * Alias of {@link #verify()} — see that method for full documentation
     * and examples.
     *
     * @throws EmailException if the template is invalid
     * @see #verify()
     * @since 1.0.0
     */
    public void validate() { verify(); }

    /**
     * Alias of {@link #verify()} — see that method for full documentation
     * and examples.
     *
     * @throws EmailException if the template is invalid
     * @see #verify()
     * @since 1.0.0
     */
    public void check() { verify(); }

    // ============================================================== accessors

    /**
     * Returns the template path (or {@code "<inline>"} for templates
     * created via {@link #raw(String)}).
     *
     * @return the path
     * @see #source()
     * @since 1.0.0
     */
    public String path() { return path; }

    /**
     * Returns the raw template source, before any rendering.
     *
     * @return the source
     * @see #raw()
     * @see #path()
     * @since 1.0.0
     */
    public String source() { return source; }

    /**
     * Alias of {@link #source()} — see that method for full
     * documentation.
     *
     * @return the source
     * @see #source()
     * @since 1.0.0
     */
    public String raw() { return source; }

    /**
     * Returns the declared placeholder values as an unmodifiable map.
     *
     * @return the values
     * @see #discovered()
     * @since 1.0.0
     */
    public Map<String, String> values() { return Map.copyOf(values); }

    /**
     * Returns a map of placeholder name → occurrence count, showing what
     * placeholders the template actually uses. Useful for validation
     * messages and debugging.
     *
     * @return the discovered placeholders and their counts
     * @see #values()
     * @see #placeholdersFound()
     * @since 1.0.0
     */
    public Map<String, Integer> discovered() {
        return discoverPlaceholders(source);
    }

    /**
     * Alias of {@link #discovered()} — see that method for full
     * documentation.
     *
     * @return the discovered placeholders
     * @see #discovered()
     * @since 1.0.0
     */
    public Map<String, Integer> placeholdersFound() { return discovered(); }

    // ============================================================== helpers

    private static boolean isHtml(String s) {
        return HTML_TAG.matcher(s).find();
    }

    private static Map<String, Integer> discoverPlaceholders(String s) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        count(s, BRACED, counts);
        count(s, DOLLARED, counts);
        return counts;
    }

    private static void count(String s, Pattern p, Map<String, Integer> counts) {
        Matcher m = p.matcher(s);
        while (m.find()) {
            counts.merge(m.group(1), 1, Integer::sum);
        }
    }
}
