package chirp.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An ordered JSON object — {@code {"key": value, ...}}.
 *
 * <p>Insertion order is preserved when the object is serialised, so the
 * output is deterministic. Two objects are considered equal by map
 * equality (order-insensitive), matching JSON's value semantics.</p>
 *
 * <h2>Null-safety policy</h2>
 * <p>{@link #get(String)} returns {@link JsonNull#INSTANCE} for missing
 * keys — never Java {@code null}. This means chained reads like
 * {@code obj.getObject("a").getString("b")} are safe: if {@code "a"}
 * doesn't exist, {@code getObject} returns an empty {@code JsonObject}
 * and {@code getString} returns {@code null}. No exception, no NPE.</p>
 *
 * <p>To distinguish "key absent" from "key present with null value", use
 * {@link #has(String)}:</p>
 * <ul>
 *   <li>{@code obj.has("x") == false} → key absent</li>
 *   <li>{@code obj.has("x") == true && obj.get("x").isNull()} → key
 *       present, value is JSON null</li>
 * </ul>
 *
 * <h2>Defaults</h2>
 * <p>Every typed getter has an overload with a default value — useful
 * when reading config files where the field may be absent:</p>
 * <pre>{@code
 * String host = obj.getString("host", "localhost");
 * int    port = obj.getInt("port", 8080);
 * }</pre>
 * <p>The defaults version returns the supplied default when the key is
 * missing <b>or</b> when the value is JSON null. It does not return the
 * default when the key exists but has the wrong type — that case throws
 * {@link JsonException}, because a type mismatch is a bug, not a
 * missing value.</p>
 *
 * <h2>Fluent building</h2>
 * <pre>{@code
 * JsonObject obj = Json.object()
 *     .put("name", "Saad")
 *     .put("age", 20)
 *     .put("active", true)
 *     .putNull("deletedAt");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the array type, see {@link JsonArray}. For the base value type,
 * see {@link JsonValue}. For the parser and serialiser entry points, see
 * {@link Json}.</p>
 *
 * @see JsonArray
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonObject implements JsonValue {

    private final Map<String, JsonValue> members = new LinkedHashMap<>();

    /**
     * Constructs an empty object. Usually you should use
     * {@link Json#object()} instead.
     *
     * @see Json#object()
     * @since 1.0.0
     */
    public JsonObject() {}

    /**
     * Factory — an empty object.
     *
     * @return a new empty {@code JsonObject}
     * @see #empty()
     * @see Json#object()
     * @since 1.0.0
     */
    public static JsonObject of() { return new JsonObject(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new empty {@code JsonObject}
     * @see #of()
     * @since 1.0.0
     */
    public static JsonObject empty() { return new JsonObject(); }

    /**
     * Constructs an object from a map of Java values.
     *
     * <p>Supported value types: {@code null}, {@link JsonValue},
     * {@code String}, {@code Boolean}, {@code Integer}, {@code Long},
     * {@code Double}, {@code Float}. Any other type is converted via
     * {@link String#valueOf(Object)}.</p>
     *
     * @param map the source map (may be null → empty object)
     * @return a new {@code JsonObject}
     * @since 1.0.0
     */
    public static JsonObject from(Map<String, ?> map) {
        JsonObject object = new JsonObject();
        if (map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                object.put(entry.getKey(), fromJava(entry.getValue()));
            }
        }
        return object;
    }

    private static JsonValue fromJava(Object value) {
        if (value == null) return JsonNull.INSTANCE;
        if (value instanceof JsonValue json) return json;
        if (value instanceof String s) return new JsonString(s);
        if (value instanceof Boolean b) return JsonBoolean.of(b);
        if (value instanceof Integer i) return new JsonNumber(Integer.toString(i));
        if (value instanceof Long l) return new JsonNumber(Long.toString(l));
        if (value instanceof Double d) return JsonNumber.of(d);
        if (value instanceof Float f) return JsonNumber.of(f.doubleValue());
        return new JsonString(String.valueOf(value));
    }

    // ------------------------------------------------------------- accessors

    /**
     * Returns the number of members in the object.
     *
     * @return the member count
     * @see #isEmpty()
     * @since 1.0.0
     */
    public int size() { return members.size(); }

    /**
     * Checks whether the object has no members.
     *
     * @return {@code true} if empty
     * @see #size()
     * @since 1.0.0
     */
    public boolean isEmpty() { return members.isEmpty(); }

    /**
     * Checks whether the given key is present in the object.
     *
     * <p>Returns {@code true} even when the value is JSON null — this is
     * how you distinguish "key absent" from "key present with null".</p>
     *
     * @param key the key to check (may be null)
     * @return {@code true} if the key is present
     * @since 1.0.0
     */
    public boolean has(String key) { return members.containsKey(key); }

    /**
     * Returns the set of keys in this object, in insertion order.
     *
     * @return the unmodifiable key set
     * @see #asMap()
     * @since 1.0.0
     */
    public Set<String> keys() { return Collections.unmodifiableSet(members.keySet()); }

    /**
     * Returns an unmodifiable view of the underlying map.
     *
     * @return the unmodifiable map
     * @see #keys()
     * @since 1.0.0
     */
    public Map<String, JsonValue> asMap() { return Collections.unmodifiableMap(members); }

    /**
     * Returns the value at the given key, or {@link JsonNull#INSTANCE}
     * if the key is absent.
     *
     * @param key the key (may be null → JsonNull)
     * @return the value, or {@code JsonNull.INSTANCE}
     * @since 1.0.0
     */
    public JsonValue get(String key) {
        JsonValue value = members.get(key);
        return value != null ? value : JsonNull.INSTANCE;
    }

    /**
     * Returns the value at the given key as a string, or {@code null}
     * if the value is JSON null.
     *
     * @param key the key
     * @return the string value, or null
     * @see #getString(String, String)
     * @since 1.0.0
     */
    public String getString(String key) { return get(key).asString(); }

    /**
     * Returns the value at the given key as an int.
     *
     * @param key the key
     * @return the int value
     * @throws JsonException if the value is not numeric
     * @since 1.0.0
     */
    public int getInt(String key) { return get(key).asInt(); }

    /**
     * Returns the value at the given key as a long.
     *
     * @param key the key
     * @return the long value
     * @throws JsonException if the value is not numeric
     * @since 1.0.0
     */
    public long getLong(String key) { return get(key).asLong(); }

    /**
     * Returns the value at the given key as a double.
     *
     * @param key the key
     * @return the double value
     * @throws JsonException if the value is not numeric
     * @since 1.0.0
     */
    public double getDouble(String key) { return get(key).asDouble(); }

    /**
     * Returns the value at the given key as a boolean.
     *
     * @param key the key
     * @return the boolean value
     * @throws JsonException if the value is not a boolean
     * @since 1.0.0
     */
    public boolean getBoolean(String key) { return get(key).asBoolean(); }

    /**
     * Returns the value at the given key as a nested object.
     *
     * <p>Returns an empty {@code JsonObject} when the value is not an
     * object, so chained reads are always safe.</p>
     *
     * @param key the key
     * @return the object, or empty
     * @since 1.0.0
     */
    public JsonObject getObject(String key) { return get(key).asObject(); }

    /**
     * Returns the value at the given key as a nested array.
     *
     * <p>Returns an empty {@code JsonArray} when the value is not an
     * array.</p>
     *
     * @param key the key
     * @return the array, or empty
     * @since 1.0.0
     */
    public JsonArray getArray(String key) { return get(key).asArray(); }

    /**
     * Returns the string value at the given key, or {@code defaultValue}
     * if the key is absent or the value is JSON null.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the string value, or the fallback
     * @see #getString(String)
     * @since 1.0.0
     */
    public String getString(String key, String defaultValue) {
        return has(key) && !get(key).isNull() ? getString(key) : defaultValue;
    }

    /**
     * Returns the int value at the given key, or {@code defaultValue} if
     * the key is absent or the value is JSON null.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the int value, or the fallback
     * @see #getInt(String)
     * @since 1.0.0
     */
    public int getInt(String key, int defaultValue) {
        return has(key) && !get(key).isNull() ? getInt(key) : defaultValue;
    }

    /**
     * Returns the long value at the given key, or {@code defaultValue} if
     * the key is absent or the value is JSON null.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the long value, or the fallback
     * @see #getLong(String)
     * @since 1.0.0
     */
    public long getLong(String key, long defaultValue) {
        return has(key) && !get(key).isNull() ? getLong(key) : defaultValue;
    }

    /**
     * Returns the double value at the given key, or {@code defaultValue}
     * if the key is absent or the value is JSON null.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the double value, or the fallback
     * @see #getDouble(String)
     * @since 1.0.0
     */
    public double getDouble(String key, double defaultValue) {
        return has(key) && !get(key).isNull() ? getDouble(key) : defaultValue;
    }

    /**
     * Returns the boolean value at the given key, or {@code defaultValue}
     * if the key is absent or the value is JSON null.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the boolean value, or the fallback
     * @see #getBoolean(String)
     * @since 1.0.0
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return has(key) && !get(key).isNull() ? getBoolean(key) : defaultValue;
    }

    // --------------------------------------------------------------- setters

    /**
     * Sets a string value. Returns this object, so calls chain.
     *
     * @param key the key
     * @param value the value (null → JSON null)
     * @return this object
     * @see #put(String, int)
     * @see #put(String, JsonValue)
     * @since 1.0.0
     */
    public JsonObject put(String key, String value) {
        members.put(key, value == null ? JsonNull.INSTANCE : new JsonString(value));
        return this;
    }

    /**
     * Sets an int value.
     *
     * @param key the key
     * @param value the value
     * @return this object
     * @see #put(String, String)
     * @since 1.0.0
     */
    public JsonObject put(String key, int value) {
        members.put(key, new JsonNumber(Integer.toString(value)));
        return this;
    }

    /**
     * Sets a long value.
     *
     * @param key the key
     * @param value the value
     * @return this object
     * @since 1.0.0
     */
    public JsonObject put(String key, long value) {
        members.put(key, new JsonNumber(Long.toString(value)));
        return this;
    }

    /**
     * Sets a double value.
     *
     * @param key the key
     * @param value the value
     * @return this object
     * @since 1.0.0
     */
    public JsonObject put(String key, double value) { members.put(key, JsonNumber.of(value)); return this; }

    /**
     * Sets a boolean value.
     *
     * @param key the key
     * @param value the value
     * @return this object
     * @since 1.0.0
     */
    public JsonObject put(String key, boolean value) { members.put(key, JsonBoolean.of(value)); return this; }

    /**
     * Sets a JSON value.
     *
     * @param key the key
     * @param value the value (null → JSON null)
     * @return this object
     * @see #put(String, String)
     * @since 1.0.0
     */
    public JsonObject put(String key, JsonValue value) {
        members.put(key, value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    /**
     * Sets the given key to JSON null.
     *
     * @param key the key
     * @return this object
     * @since 1.0.0
     */
    public JsonObject putNull(String key) { members.put(key, JsonNull.INSTANCE); return this; }

    /**
     * Removes the given key (if present).
     *
     * @param key the key to remove
     * @return this object
     * @since 1.0.0
     */
    public JsonObject remove(String key) { members.remove(key); return this; }

    /**
     * Removes all members.
     *
     * @return this object
     * @since 1.0.0
     */
    public JsonObject clear() { members.clear(); return this; }

    // ------------------------------------------------------------ conversion

    /**
     * Returns the compact JSON text for this object.
     *
     * @return the compact JSON
     * @see #toJson(boolean)
     * @since 1.0.0
     */
    @Override
    public String toJson() { return JsonWriter.write(this, false); }

    /**
     * Returns the JSON text for this object — compact or pretty-printed.
     *
     * @param pretty whether to pretty-print
     * @return the JSON text
     * @see #toJson()
     * @since 1.0.0
     */
    @Override
    public String toJson(boolean pretty) { return JsonWriter.write(this, pretty); }

    /**
     * Returns the compact JSON text for this object.
     *
     * @return the compact JSON
     * @see #toJson()
     * @since 1.0.0
     */
    @Override
    public String toString() { return toJson(); }

    /**
     * Two objects are equal when their member maps are equal — order
     * insensitive, as JSON specifies.
     *
     * @param other the object to compare
     * @return {@code true} if the maps are equal
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonObject object && object.members.equals(members);
    }

    /**
     * Hash code matching {@link #equals(Object)}.
     *
     * @return the hash code
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return members.hashCode(); }
}
