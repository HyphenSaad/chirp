package chirp.json;

import chirp.internal.Trace;

/**
 * Recursive-descent JSON parser — zero external dependencies.
 *
 * <p>Trace integration: entry point logs input length, exit logs the root
 * value type; errors are logged to {@code json} category. Deep recursion is
 * not traced by default to avoid overwhelming output.</p>
 */
public final class JsonParser {

    private final String input;
    private int pos;

    private JsonParser(String input) { this.input = input; }

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

    public static JsonObject parseObject(String text) {
        JsonValue value = parse(text);
        if (!(value instanceof JsonObject object)) {
            Trace.warn("json", "parseObject: root is not an object");
            throw new JsonException("Expected an object at the top level");
        }
        return object;
    }

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
                          throw new JsonException("Invalid unicode escape: " + hex, e);
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
