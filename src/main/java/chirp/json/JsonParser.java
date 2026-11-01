package chirp.json;

import chirp.internal.Trace;

/**
 * Recursive-descent JSON parser — zero external dependencies.
 *
 * <p>The implementation behind {@link Json#parse(String)},
 * {@link Json#parseObject(String)}, and {@link Json#parseArray(String)}.
 * Most callers should use the facade; this class is public so that
 * tests can exercise the parser directly and so that a caller who needs
 * a {@link JsonObject} or {@link JsonArray} at the top level can call
 * the specialised methods without re-checking the type.</p>
 *
 * <h2>What it parses</h2>
 * <p>The full JSON grammar from RFC 8259 — objects, arrays, strings,
 * numbers, {@code true}, {@code false}, {@code null} — with:</p>
 * <ul>
 *   <li><b>Recursive descent</b> — no lookahead tables, no regex,
 *       just one method per grammar rule. Straightforward to read and
 *       extend.</li>
 *   <li><b>Correct escape handling</b> — {@code \" \\ \/ \b \f \n \r \t}
 *       and {@code \}{@code uXXXX} Unicode escapes are decoded.</li>
 *   <li><b>Correct number parsing</b> — the raw token is preserved
 *       (in {@link JsonNumber}) so large integers do not lose
 *       precision on round-trip.</li>
 *   <li><b>Strict validation</b> — trailing content after the top-level
 *       value, control characters inside strings, and malformed escapes
 *       all throw {@link JsonException} with a position that points at
 *       the offending character.</li>
 * </ul>
 *
 * <h2>What it does not parse</h2>
 * <ul>
 *   <li><b>Comments</b> — JSON does not allow them. Input with
*       {@code //} or {@code multi-line-comment} throws.</li>
 *   <li><b>Trailing commas</b> — JSON does not allow them. Input with
 *       {@code [1, 2, 3,]} throws.</li>
 *   <li><b>Single quotes</b> — JSON requires double quotes for strings.
 *       Input with {@code {'a': 1}} throws.</li>
 *   <li><b>NaN / Infinity</b> — not JSON numbers. Use the string form
 *       {@code "NaN"} if you need to convey those.</li>
 * </ul>
 *
 * <h2>Trace integration</h2>
 * <p>Entry and exit are logged to the {@code json} trace category with
 * input length and root type respectively. Errors are logged at WARN
 * level before throwing. Deep recursion is <b>not</b> traced — a
 * deeply nested object would otherwise flood the trace output with one
 * line per level. The entry/exit/error lines are enough to diagnose a
 * parse failure without turning trace into a JSON dump.</p>
 *
 * <h2>Example — parse the whole document</h2>
 * <pre>{@code
 * JsonValue value = JsonParser.parse("{ \"name\": \"Saad\", \"age\": 20 }");
 * JsonObject obj = value.asObject();
 * System.out.println(obj.getString("name"));  // "Saad"
 * }</pre>
 *
 * <h2>Example — parse into a typed top-level</h2>
 * <pre>{@code
 * JsonObject config = JsonParser.parseObject(configText);
 * JsonArray items = JsonParser.parseArray(itemsText);
 * // Both throw JsonException with a clear message if the root type
 * // does not match — useful for fail-fast validation.
 * }</pre>
 *
 * <h2>Error messages</h2>
 * <p>Every error includes the byte position where the parse failed:
 * {@code "Expected string key at position 12"}, {@code "Unterminated
 * string"}, {@code "Unexpected trailing content at position 47"}. The
 * position points at the first character that does not match the
 * expected grammar — good enough to eyeball the input and see what is
 * wrong.</p>
 *
 * @see Json
 * @see JsonValue
 * @see JsonObject
 * @see JsonArray
 * @see JsonWriter
 * @since 1.0.0
 */
public final class JsonParser {

    private final String input;
    private int pos;

    private JsonParser(String input) { this.input = input; }

    /**
     * Parses a complete JSON value from the given text. The text must
     * contain exactly one value — trailing whitespace is allowed, any
     * other trailing content throws.
     *
     * @param text the JSON text (must not be null or empty)
     * @return the parsed value
     * @throws JsonException if {@code text} is null, empty, or malformed
     * @see #parseObject(String)
     * @see #parseArray(String)
     * @since 1.0.0
     */
    public static JsonValue parse(String text) {
        Trace.trace("json", "parse(input.len={})", text == null ? 0 : text.length());
        if (text == null) {
            Trace.warn("json", "parse: null input");
            throw new JsonException("Input is null");
        }
        JsonParser parser = new JsonParser(text);
        parser.skipWhitespace();
        if (parser.pos >= text.length()) {
            Trace.warn("json", "parse: empty input");
            throw new JsonException("Empty JSON input");
        }
        JsonValue value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.pos < text.length()) {
            Trace.warn("json", "parse: trailing content at position {}", parser.pos);
            throw new JsonException("Unexpected trailing content at position " + parser.pos);
        }
        Trace.debug("json", "parse: root type = {}", typeName(value));
        return value;
    }

    /**
     * Parses a JSON object from the given text. The root value must be
     * an object — a top-level array, string, number, boolean, or
     * {@code null} throws.
     *
     * @param text the JSON text
     * @return the parsed object
     * @throws JsonException if the input is malformed or the root is
     * not an object
     * @see #parse(String)
     * @see #parseArray(String)
     * @since 1.0.0
     */
    public static JsonObject parseObject(String text) {
        JsonValue value = parse(text);
        if (!(value instanceof JsonObject object)) {
            Trace.warn("json", "parseObject: root is not an object");
            throw new JsonException("Expected an object at the top level");
        }
        return object;
    }

    /**
     * Parses a JSON array from the given text. The root value must be
     * an array — a top-level object, string, number, boolean, or
     * {@code null} throws.
     *
     * @param text the JSON text
     * @return the parsed array
     * @throws JsonException if the input is malformed or the root is
     * not an array
     * @see #parse(String)
     * @see #parseObject(String)
     * @since 1.0.0
     */
    public static JsonArray parseArray(String text) {
        JsonValue value = parse(text);
        if (!(value instanceof JsonArray array)) {
            Trace.warn("json", "parseArray: root is not an array");
            throw new JsonException("Expected an array at the top level");
        }
        return array;
    }

    private static String typeName(JsonValue value) {
        if (value instanceof JsonObject) return "object";
        if (value instanceof JsonArray) return "array";
        if (value instanceof JsonString) return "string";
        if (value instanceof JsonNumber) return "number";
        if (value instanceof JsonBoolean) return "boolean";
        if (value instanceof JsonNull) return "null";
        return "unknown";
    }

    // ---------------------------------------------------------------- parsing

    private JsonValue parseValue() {
        skipWhitespace();
        if (pos >= input.length()) throw new JsonException("Unexpected end of input");
        char c = input.charAt(pos);
        switch (c) {
            case '{' :
                return parseObjectValue();
            case '[' :
                return parseArrayValue();
            case '"' :
                return new JsonString(parseString());
            case 't' :
                return parseLiteral("true", JsonBoolean.TRUE);
            case 'f' :
                return parseLiteral("false", JsonBoolean.FALSE);
            case 'n' :
                return parseLiteral("null", JsonNull.INSTANCE);
            default :
                return parseNumber();
        }
    }

    private JsonObject parseObjectValue() {
        JsonObject object = new JsonObject();
        pos++;
        skipWhitespace();
        if (peek() == '}') { pos++; return object; }

        while (true) {
            skipWhitespace();
            if (peek() != '"') throw new JsonException("Expected string key at position " + pos);
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonValue value = parseValue();
            object.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == '}') { pos++; return object; }
            throw new JsonException("Expected ',' or '}' at position " + pos);
        }
    }

    private JsonArray parseArrayValue() {
        JsonArray array = new JsonArray();
        pos++;
        skipWhitespace();
        if (peek() == ']') { pos++; return array; }

        while (true) {
            JsonValue value = parseValue();
            array.add(value);
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == ']') { pos++; return array; }
            throw new JsonException("Expected ',' or ']' at position " + pos);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= input.length()) throw new JsonException("Unterminated escape");
                char esc = input.charAt(pos++);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > input.length()) throw new JsonException("Truncated unicode escape");
                        String hex = input.substring(pos, pos + 4);
                        pos += 4;
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw new JsonException("Invalid unicode escape: " + hex);
                        }
                    }
                    default -> throw new JsonException("Invalid escape: \\" + esc);
                }
            } else if (c < 0x20) {
                throw new JsonException("Control character in string at position " + (pos - 1));
            } else {
                sb.append(c);
            }
        }
        throw new JsonException("Unterminated string");
    }

    private JsonValue parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;

        if (pos >= input.length()) throw new JsonException("Unexpected end of number");
        if (input.charAt(pos) == '0') {
            pos++;
        } else if (isDigit(peek())) {
            while (pos < input.length() && isDigit(input.charAt(pos))) pos++;
        } else {
            throw new JsonException("Expected digit at position " + pos);
        }

        if (pos < input.length() && input.charAt(pos) == '.') {
            pos++;
            if (pos >= input.length() || !isDigit(input.charAt(pos))) {
                throw new JsonException("Expected digit after decimal point");
            }
            while (pos < input.length() && isDigit(input.charAt(pos))) pos++;
        }

        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
            if (pos >= input.length() || !isDigit(input.charAt(pos))) {
                throw new JsonException("Expected digit in exponent");
            }
            while (pos < input.length() && isDigit(input.charAt(pos))) pos++;
        }

        return new JsonNumber(input.substring(start, pos));
    }

    private JsonValue parseLiteral(String literal, JsonValue value) {
        if (!input.startsWith(literal, pos)) {
            throw new JsonException("Expected '" + literal + "' at position " + pos);
        }
        pos += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    private char peek() {
        if (pos >= input.length()) throw new JsonException("Unexpected end of input");
        return input.charAt(pos);
    }

    private void expect(char expected) {
        if (pos >= input.length() || input.charAt(pos) != expected) {
            throw new JsonException("Expected '" + expected + "' at position " + pos);
        }
        pos++;
    }

    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
}
