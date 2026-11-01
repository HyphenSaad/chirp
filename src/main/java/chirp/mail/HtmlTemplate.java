package chirp.mail;

import chirp.io.TextFileIO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML template with declared placeholders.
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code of / from / load / read / open / inline / raw};
 * placeholder: {@code placeholder / set / bind / with / fill};
 * bulk placeholder: {@code placeholders / setAll / bindAll / fillAll};
 * render: {@code render / format / build / toString};
 * verify: {@code verify / validate / check};
 * safe render: {@code renderOrDefault / renderOr / renderIfValid}.</p>
 *
 * <h2>Placeholder syntax</h2>
 * <p>Supports both {@code {{name}}} and {@code ${name}}. A template can mix
 * both styles.</p>
 *
 * <h2>Strict validation (existing behavior preserved)</h2>
 * <ul>
 *   <li>File must exist and be non-empty</li>
 *   <li>Content must look like HTML</li>
 *   <li>Every declared placeholder must appear at least once</li>
 *   <li>Every placeholder in the source must be declared</li>
 * </ul>
 *
 * <p>The new safe-render methods return a fallback / Optional instead of
 * throwing.</p>
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

    public static HtmlTemplate of(String path) { return load(path); }
    public static HtmlTemplate from(String path) { return load(path); }
    public static HtmlTemplate read(String path) { return load(path); }
    public static HtmlTemplate open(String path) { return load(path); }

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

    /** Wraps a raw HTML string — useful for tests and inline templates. */
    public static HtmlTemplate raw(String html) {
        return inline(html);
    }
    public static HtmlTemplate inline(String html) {
        if (html == null || html.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "HTML content is required");
        }
        return new HtmlTemplate("<inline>", html);
    }

    // ============================================================== placeholder

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
    public HtmlTemplate placeholder(String name, String value) { return placeholder(name, (Object) value); }
    public HtmlTemplate set(String name, Object value) { return placeholder(name, value); }
    public HtmlTemplate bind(String name, Object value) { return placeholder(name, value); }
    public HtmlTemplate with(String name, Object value) { return placeholder(name, value); }
    public HtmlTemplate fill(String name, Object value) { return placeholder(name, value); }

    public HtmlTemplate placeholders(Map<String, ?> map) {
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) placeholder(e.getKey(), e.getValue());
        }
        return this;
    }
    public HtmlTemplate setAll(Map<String, ?> map) { return placeholders(map); }
    public HtmlTemplate bindAll(Map<String, ?> map) { return placeholders(map); }
    public HtmlTemplate fillAll(Map<String, ?> map) { return placeholders(map); }

    // ============================================================== render

    /** Validates and returns the rendered HTML — throws on any issue. */
    public String render() {
        verify();
        String result = source;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    public String format() { return render(); }
    public String build() { return render(); }
    @Override
    public String toString() { return render(); }

    /** Renders with fallback — returns {@code fallback} on any validation error. */
    public String renderOrDefault(String fallback) {
        try {
            return render();
        } catch (EmailException e) {
            return fallback == null ? "" : fallback;
        }
    }
    public String renderOr(String fallback) { return renderOrDefault(fallback); }

    /** Renders safely — returns {@link Optional#empty()} on error. */
    public Optional<String> renderIfValid() {
        try {
            return Optional.of(render());
        } catch (EmailException e) {
            return Optional.empty();
        }
    }
    public Optional<String> tryRender() { return renderIfValid(); }

    // ============================================================== verify

    /** Verifies the template without rendering — throws on any issue. */
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
    public void validate() { verify(); }
    public void check() { verify(); }

    // ============================================================== accessors

    public String path() { return path; }
    public String source() { return source; }
    public String raw() { return source; }
    public Map<String, String> values() { return Map.copyOf(values); }

    /** Placeholder name → occurrence count. */
    public Map<String, Integer> discovered() {
        return discoverPlaceholders(source);
    }
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
