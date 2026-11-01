package chirp.json;

/**
 * Base type for every JSON value. Instances are one of
 * {@link JsonObject}, {@link JsonArray}, {@link JsonString},
 * {@link JsonNumber}, {@link JsonBoolean}, or {@link JsonNull}.
 *
 * <p>Written against Java 17 — uses {@code instanceof} chains rather than
 * pattern-matching {@code switch} (which requires Java 21).</p>
 */
public sealed interface JsonValue
        permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {

    /** @return compact JSON text */
    String toJson();

    /** @return compact or pretty-printed JSON text */
    default String toJson(boolean pretty) {
        return JsonWriter.write(this, pretty);
    }

    default boolean isNull() { return this instanceof JsonNull; }
    default boolean isObject() { return this instanceof JsonObject; }
    default boolean isArray() { return this instanceof JsonArray; }
    default boolean isString() { return this instanceof JsonString; }
    default boolean isNumber() { return this instanceof JsonNumber; }
    default boolean isBoolean() { return this instanceof JsonBoolean; }

    default JsonObject asObject() {
        if (this instanceof JsonObject object) return object;
        throw new JsonException("Expected an object but found " + typeName());
    }

    default JsonArray asArray() {
        if (this instanceof JsonArray array) return array;
        throw new JsonException("Expected an array but found " + typeName());
    }

    default String asString() {
        if (this instanceof JsonString s) return s.value();
        if (this instanceof JsonNumber n) return n.raw();
        if (this instanceof JsonBoolean b) return Boolean.toString(b.value());
        if (this instanceof JsonNull) return null;
        if (this instanceof JsonObject o) return o.toJson();
        if (this instanceof JsonArray a) return a.toJson();
        return null; // unreachable — sealed hierarchy
    }

    default double asDouble() {
        if (this instanceof JsonNumber number) return number.asDouble();
        if (this instanceof JsonString string) {
            try {
                return Double.parseDouble(string.value());
            } catch (NumberFormatException e) {
                throw new JsonException("Not a number: " + string.value());
            }
        }
        throw new JsonException("Expected a number but found " + typeName());
    }

    default int asInt() { return (int) asDouble(); }
    default long asLong() { return (long) asDouble(); }

    default boolean asBoolean() {
        if (this instanceof JsonBoolean bool) return bool.value();
        throw new JsonException("Expected a boolean but found " + typeName());
    }

    /** Package-private helper for error messages. */
    private String typeName() {
        if (this instanceof JsonObject) return "object";
        if (this instanceof JsonArray) return "array";
        if (this instanceof JsonString) return "string";
        if (this instanceof JsonNumber) return "number";
        if (this instanceof JsonBoolean) return "boolean";
        if (this instanceof JsonNull) return "null";
        return "unknown";
    }
}
