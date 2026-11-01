package chirp.json;

import chirp.internal.Trace;

/**
 * Serialises a {@link JsonValue} to compact or pretty-printed JSON.
 */
public final class JsonWriter {

    private JsonWriter() {}

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

    public static String quote(String text) {
        StringBuilder sb = new StringBuilder();
        quote(sb, text);
        return sb.toString();
    }

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
