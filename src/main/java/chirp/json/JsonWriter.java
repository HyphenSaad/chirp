package chirp.json;

import chirp.internal.Trace;

/**
 * Serialises a {@link JsonValue} tree to compact or pretty-printed JSON
 * text.
 *
 * <p>The implementation behind {@link Json#write(JsonValue)},
 * {@link Json#pretty(JsonValue)}, and every {@code toJson()} method on
 * the value types. Most callers should use the facade; this class is
 * public so that tests can exercise the writer directly and so that a
 * caller who needs to serialise a specific format can do so without
 * round-tripping through the value's {@code toJson()}.</p>
 *
 * <h2>Two output styles</h2>
 * <table border="1">
 *   <caption>Compact vs pretty-printed output</caption>
 *   <tr><th>Style</th><th>Output</th></tr>
 *   <tr><td><b>Compact</b> ({@code write(v, false)})</td>
 *       <td>{@code {"name":"Saad","age":20,"tags":["dev","ops"]}}</td></tr>
 *   <tr><td><b>Pretty</b> ({@code write(v, true)})</td>
 *       <td>{@code \{\n  "name": "Saad",\n  "age": 20,\n  ...\n\}}</td></tr>
 * </table>
 * <p>The two styles produce byte-identical output when re-parsed — the
 * pretty version just adds whitespace and indentation. Compact output
 * is the default for the {@code toJson()} methods on value types;
 * pretty is opt-in via the {@code toJson(true)} overload.</p>
 *
 * <h2>Indentation</h2>
 * <p>Pretty output uses two spaces per level. There is no configuration
 * for the indent character or width — two spaces is the JSON standard
 * and matches what every other library emits.</p>
 *
 * <h2>String escaping</h2>
 * <p>Every string is quoted and every control character is escaped
 * correctly:</p>
 * <ul>
 *   <li>{@code \"} — double quote</li>
 *   <li>{@code \\} — backslash</li>
 *   <li>{@code \b \f \n \r \t} — the named control escapes</li>
 *   <li>{@code \}{@code uXXXX} — any other char below 0x20</li>
 * </ul>
 * <p>Non-ASCII printable characters (emoji, accented letters, CJK) are
 * emitted as-is (not escaped), so the output is readable when the file
 * is opened in a UTF-8 editor. JSON allows them unescaped.</p>
 *
 * <h2>Number formatting</h2>
 * <p>{@link JsonNumber} preserves the raw token, so a number written as
 * {@code 3.14} reads back as {@code 3.14}, not {@code 3.1400000000000001}.
 * Large integers ({@code 9223372036854775807}) also round-trip exactly.
 * {@code NaN} and {@code Infinity} are not representable in JSON — the
 * {@link JsonNumber#of(double)} factory throws on those.</p>
 *
 * <h2>Trace integration</h2>
 * <p>Entry (type + pretty flag) and exit (character count) are logged
 * to the {@code json} category. The individual value writes are not
 * traced — a large tree would flood the output.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * JsonObject obj = Json.object().put("name", "Saad").put("age", 20);
 *
 * // Compact
 * String compact = JsonWriter.write(obj, false);
 * // {"name":"Saad","age":20}
 *
 * // Pretty-printed
 * String pretty = JsonWriter.write(obj, true);
 * // {
 * //   "name": "Saad",
 * //   "age": 20
 * // }
 * }</pre>
 *
 * <h2>Direct quote helper</h2>
 * <p>{@link #quote(String)} returns a quoted, escaped JSON string for a
 * raw Java string. Useful for building small JSON fragments by hand
 * without going through a full {@link JsonValue} tree.</p>
 *
 * @see Json
 * @see JsonValue
 * @see JsonParser
 * @since 1.0.0
 */
public final class JsonWriter {

    private JsonWriter() {}

    /**
     * Serialises a {@link JsonValue} to JSON text.
     *
     * @param value the value to serialise (null → {@code "null"})
     * @param pretty {@code true} for pretty-printed output, {@code false}
     * for compact
     * @return the JSON text
     * @see Json#write(JsonValue)
     * @see Json#pretty(JsonValue)
     * @since 1.0.0
     */
    public static String write(JsonValue value, boolean pretty) {
        Trace.trace("json", "write(type={}, pretty={})", typeName(value), pretty);
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value, pretty, 0);
        Trace.debug("json", "write produced {} chars", sb.length());
        return sb.toString();
    }

    private static String typeName(JsonValue value) {
        if (value == null) return "null";
        if (value instanceof JsonObject) return "object";
        if (value instanceof JsonArray) return "array";
        if (value instanceof JsonString) return "string";
        if (value instanceof JsonNumber) return "number";
        if (value instanceof JsonBoolean) return "boolean";
        if (value instanceof JsonNull) return "null";
        return "unknown";
    }

    private static void writeValue(StringBuilder sb, JsonValue value, boolean pretty, int indent) {
        if (value instanceof JsonNull) {
            sb.append("null");
        } else if (value instanceof JsonBoolean b) {
            sb.append(b.value());
        } else if (value instanceof JsonString s) {
            quote(sb, s.value());
        } else if (value instanceof JsonNumber n) {
            sb.append(n.raw());
        } else if (value instanceof JsonArray a) {
            writeArray(sb, a, pretty, indent);
        } else if (value instanceof JsonObject o) {
            writeObject(sb, o, pretty, indent);
        } else {
            sb.append("null");
        }
    }

    private static void writeArray(StringBuilder sb, JsonArray array, boolean pretty, int indent) {
        if (array.isEmpty()) { sb.append("[]"); return; }
        sb.append('[');
        if (pretty) sb.append('\n');

        int index = 0;
        for (JsonValue element : array.asList()) {
            if (pretty) indentTo(sb, indent + 1);
            writeValue(sb, element, pretty, indent + 1);
            if (++index < array.size()) sb.append(',');
            if (pretty) sb.append('\n');
        }
        if (pretty) indentTo(sb, indent);
        sb.append(']');
    }

    private static void writeObject(StringBuilder sb, JsonObject object, boolean pretty, int indent) {
        if (object.isEmpty()) { sb.append("{}"); return; }
        sb.append('{');
        if (pretty) sb.append('\n');

        int index = 0;
        for (var entry : object.asMap().entrySet()) {
            if (pretty) indentTo(sb, indent + 1);
            quote(sb, entry.getKey());
            sb.append(pretty ? ": " : ":");
            writeValue(sb, entry.getValue(), pretty, indent + 1);
            if (++index < object.size()) sb.append(',');
            if (pretty) sb.append('\n');
        }
        if (pretty) indentTo(sb, indent);
        sb.append('}');
    }

    private static void indentTo(StringBuilder sb, int level) {
        sb.append("  ".repeat(level));
    }

    /**
     * Returns a quoted, escaped JSON string for the given text. Useful
     * for building small JSON fragments by hand.
     *
     * <pre>{@code
     * String fragment = "{\"msg\":" + JsonWriter.quote("line1\nline2") + "}";
     * // {"msg":"line1\nline2"}
     * }</pre>
     *
     * @param text the text to quote (null → {@code "null"})
     * @return the quoted string
     * @since 1.0.0
     */
    public static String quote(String text) {
        StringBuilder sb = new StringBuilder();
        quote(sb, text);
        return sb.toString();
    }

    /**
     * Appends a quoted, escaped JSON string to the given builder.
     *
     * @param sb the target builder
     * @param text the text to quote (null → appends {@code "null"})
     * @since 1.0.0
     */
    public static void quote(StringBuilder sb, String text) {
        if (text == null) { sb.append("null"); return; }
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }
}
