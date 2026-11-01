package chirp.json;

/**
 * The base type for every JSON value.
 *
 * <p>{@code JsonValue} is a <b>sealed interface</b> — the six
 * implementations are the entirety of the JSON data model: object, array,
 * string, number, boolean, and null. Nothing else can implement it, which
 * means a {@code switch} on a {@code JsonValue} can be exhaustive without
 * a default case (once Java 21 pattern matching is available; until then
 * the {@code instanceof} chains below serve the same purpose).</p>
 *
 * <h2>The six subtypes</h2>
 * <table border="1">
 *   <caption>JSON value types</caption>
 *   <tr><th>Type</th><th>Represents</th><th>Example</th></tr>
 *   <tr><td>{@link JsonObject}</td><td>Key-value map</td>
 *       <td>{@code {"a":1,"b":2}}</td></tr>
 *   <tr><td>{@link JsonArray}</td><td>Ordered list</td>
 *       <td>{@code [1,2,3]}</td></tr>
 *   <tr><td>{@link JsonString}</td><td>String literal</td>
 *       <td>{@code "hello"}</td></tr>
 *   <tr><td>{@link JsonNumber}</td><td>Number</td>
 *       <td>{@code 42}, {@code 3.14}</td></tr>
 *   <tr><td>{@link JsonBoolean}</td><td>Boolean</td>
 *       <td>{@code true}, {@code false}</td></tr>
 *   <tr><td>{@link JsonNull}</td><td>Null literal</td>
 *       <td>{@code null}</td></tr>
 * </table>
 *
 * <h2>Type predicates</h2>
 * <p>Every value answers "am I a ..." with one of the
 * {@code isXxx()} methods: {@link #isObject()}, {@link #isArray()},
 * {@link #isString()}, {@link #isNumber()}, {@link #isBoolean()},
 * {@link #isNull()}. Use these to branch on the type without casting.</p>
 *
 * <h2>Type conversions</h2>
 * <p>{@link #asObject()}, {@link #asArray()}, {@link #asString()},
 * {@link #asDouble()}, {@link #asInt()}, {@link #asLong()},
 * {@link #asBoolean()} convert to the corresponding Java type.</p>
 *
 * <p>The conversions are <b>strict</b> by default: {@code asObject()}
 * on a {@code JsonArray} throws {@link JsonException}. The exception is
 * string-to-number coercion, which is allowed because
 * {@link #asDouble()} on a {@code JsonString} that contains a number
 * (e.g. {@code "42"}) returns the number rather than throwing — this
 * matches what most JSON consumers expect from loosely-typed APIs.</p>
 *
 * <h2>Serialisation</h2>
 * <p>{@link #toJson()} returns compact JSON (no whitespace).
 * {@link #toJson(boolean)} returns either compact or pretty-printed
 * JSON, chosen by the boolean.</p>
 *
 * <h2>Null handling</h2>
 * <p>A missing value in a JSON object — for example,
 * {@code obj.get("missing")} — returns {@link JsonNull#INSTANCE}, not
 * Java {@code null}. This means chained calls never throw a
 * {@link NullPointerException}. Use {@link #isNull()} to test for JSON
 * null, or compare with {@code obj.has("key")} to distinguish "the key
 * exists with value null" from "the key is absent".</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * JsonValue value = Json.parse("{\"name\":\"Saad\",\"age\":20}");
 *
 * if (value.isObject()) {
 *     JsonObject obj = value.asObject();
 *     String name = obj.getString("name");     // "Saad"
 *     int age    = obj.getInt("age");          // 20
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the parser and serialiser entry points, see {@link Json}. For
 * the object and array types, see {@link JsonObject} and
 * {@link JsonArray}.</p>
 *
 * @see Json
 * @see JsonObject
 * @see JsonArray
 * @see JsonString
 * @see JsonNumber
 * @see JsonBoolean
 * @see JsonNull
 * @since 1.0.0
 */
public sealed interface JsonValue
        permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {

    /**
     * Returns the compact JSON text for this value.
     *
     * @return the compact JSON
     * @see #toJson(boolean)
     * @see Json#compact(JsonValue)
     * @since 1.0.0
     */
    String toJson();

    /**
     * Returns the JSON text for this value — compact when {@code pretty}
     * is {@code false}, pretty-printed when {@code true}.
     *
     * @param pretty whether to pretty-print
     * @return the JSON text
     * @see #toJson()
     * @see Json#pretty(JsonValue)
     * @since 1.0.0
     */
    default String toJson(boolean pretty) {
        return JsonWriter.write(this, pretty);
    }

    /**
     * Checks whether this value is JSON {@code null}.
     *
     * @return {@code true} if this is {@link JsonNull}
     * @since 1.0.0
     */
    default boolean isNull() { return this instanceof JsonNull; }

    /**
     * Checks whether this value is a JSON object.
     *
     * @return {@code true} if this is {@link JsonObject}
     * @since 1.0.0
     */
    default boolean isObject() { return this instanceof JsonObject; }

    /**
     * Checks whether this value is a JSON array.
     *
     * @return {@code true} if this is {@link JsonArray}
     * @since 1.0.0
     */
    default boolean isArray() { return this instanceof JsonArray; }

    /**
     * Checks whether this value is a JSON string.
     *
     * @return {@code true} if this is {@link JsonString}
     * @since 1.0.0
     */
    default boolean isString() { return this instanceof JsonString; }

    /**
     * Checks whether this value is a JSON number.
     *
     * @return {@code true} if this is {@link JsonNumber}
     * @since 1.0.0
     */
    default boolean isNumber() { return this instanceof JsonNumber; }

    /**
     * Checks whether this value is a JSON boolean.
     *
     * @return {@code true} if this is {@link JsonBoolean}
     * @since 1.0.0
     */
    default boolean isBoolean() { return this instanceof JsonBoolean; }

    /**
     * Converts to a {@link JsonObject} or throws.
     *
     * @return this value, cast to {@code JsonObject}
     * @throws JsonException if this value is not an object
     * @since 1.0.0
     */
    default JsonObject asObject() {
        if (this instanceof JsonObject object) return object;
        throw new JsonException("Expected an object but found " + typeName());
    }

    /**
     * Converts to a {@link JsonArray} or throws.
     *
     * @return this value, cast to {@code JsonArray}
     * @throws JsonException if this value is not an array
     * @since 1.0.0
     */
    default JsonArray asArray() {
        if (this instanceof JsonArray array) return array;
        throw new JsonException("Expected an array but found " + typeName());
    }

    /**
     * Converts to a string.
     *
     * <p>Every type converts to a reasonable string representation:</p>
     * <ul>
     *   <li>{@code JsonString} → its value</li>
     *   <li>{@code JsonNumber} → the raw numeric text</li>
     *   <li>{@code JsonBoolean} → {@code "true"} / {@code "false"}</li>
     *   <li>{@code JsonNull} → {@code null}</li>
     *   <li>{@code JsonObject} / {@code JsonArray} → their compact JSON</li>
     * </ul>
     *
     * <p>So this method never throws — it always produces something
     * printable. Use {@link #isObject()} / {@link #isArray()} to
     * distinguish the compact-JSON case if the caller cares.</p>
     *
     * @return the string representation
     * @since 1.0.0
     */
    default String asString() {
        if (this instanceof JsonString s) return s.value();
        if (this instanceof JsonNumber n) return n.raw();
        if (this instanceof JsonBoolean b) return Boolean.toString(b.value());
        if (this instanceof JsonNull) return null;
        if (this instanceof JsonObject o) return o.toJson();
        if (this instanceof JsonArray a) return a.toJson();
        return null; // unreachable — sealed hierarchy
    }

    /**
     * Converts to a double.
     *
     * <p>Allowed on {@code JsonNumber} (its value) and on
     * {@code JsonString} (parsed as a number). Throws on all other
     * types.</p>
     *
     * @return the double value
     * @throws JsonException if this value cannot be interpreted as a number
     * @see #asInt()
     * @see #asLong()
     * @since 1.0.0
     */
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

    /**
     * Converts to an {@code int}. Truncates the {@code double} value —
     * see {@link #asDouble()} for the conversion rules.
     *
     * @return the int value
     * @throws JsonException if this value cannot be interpreted as a number
     * @since 1.0.0
     */
    default int asInt() { return (int) asDouble(); }

    /**
     * Converts to a {@code long}. Truncates the {@code double} value —
     * see {@link #asDouble()} for the conversion rules.
     *
     * @return the long value
     * @throws JsonException if this value cannot be interpreted as a number
     * @since 1.0.0
     */
    default long asLong() { return (long) asDouble(); }

    /**
     * Converts to a boolean. Allowed only on {@code JsonBoolean}.
     *
     * @return the boolean value
     * @throws JsonException if this value is not a boolean
     * @since 1.0.0
     */
    default boolean asBoolean() {
        if (this instanceof JsonBoolean bool) return bool.value();
        throw new JsonException("Expected a boolean but found " + typeName());
    }

    /**
     * Returns a human-readable type name for error messages.
     *
     * @return the type name
     * @since 1.0.0
     */
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
