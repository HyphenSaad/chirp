package chirp.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered JSON array — {@code [value, ...]}.
 *
 * <p>Elements are stored in insertion order and serialised in that
 * order. Two arrays are equal when their element lists are equal
 * (order-sensitive — the opposite of {@link JsonObject}'s equality).</p>
 *
 * <h2>Boundary-safe access</h2>
 * <p>{@link #get(int)} returns {@link JsonNull#INSTANCE} for an
 * out-of-range index — never throws, never returns null. This means
 * {@code arr.get(999).asString()} returns {@code null} rather than
 * throwing {@link IndexOutOfBoundsException}.</p>
 *
 * <h2>Fluent building</h2>
 * <pre>{@code
 * JsonArray tags = Json.array()
 *     .add("oop")
 *     .add("java")
 *     .add("chirp");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the object type, see {@link JsonObject}. For the base value
 * type, see {@link JsonValue}. For the parser and serialiser entry
 * points, see {@link Json}.</p>
 *
 * @see JsonObject
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonArray implements JsonValue {

    private final List<JsonValue> elements = new ArrayList<>();

    /**
     * Constructs an empty array. Usually you should use
     * {@link Json#array()} instead.
     *
     * @see Json#array()
     * @since 1.0.0
     */
    public JsonArray() {}

    /**
     * Factory — an empty array.
     *
     * @return a new empty {@code JsonArray}
     * @see #empty()
     * @see Json#array()
     * @since 1.0.0
     */
    public static JsonArray of() { return new JsonArray(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new empty {@code JsonArray}
     * @see #of()
     * @since 1.0.0
     */
    public static JsonArray empty() { return new JsonArray(); }

    /**
     * Constructs an array from a Java list.
     *
     * @param source the source list (may be null → empty array)
     * @return a new {@code JsonArray}
     * @since 1.0.0
     */
    public static JsonArray from(List<?> source) {
        JsonArray array = new JsonArray();
        if (source != null) {
            for (Object item : source) {
                array.add(fromJava(item));
            }
        }
        return array;
    }

    /**
     * Constructs an array from varargs Java values.
     *
     * @param values the values (may be null → empty array)
     * @return a new {@code JsonArray}
     * @since 1.0.0
     */
    public static JsonArray of(Object... values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (Object value : values) array.add(fromJava(value));
        }
        return array;
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
     * Returns the number of elements in the array.
     *
     * @return the element count
     * @see #isEmpty()
     * @since 1.0.0
     */
    public int size() { return elements.size(); }

    /**
     * Checks whether the array has no elements.
     *
     * @return {@code true} if empty
     * @see #size()
     * @since 1.0.0
     */
    public boolean isEmpty() { return elements.isEmpty(); }

    /**
     * Returns an unmodifiable view of the underlying list.
     *
     * @return the unmodifiable element list
     * @since 1.0.0
     */
    public List<JsonValue> asList() { return Collections.unmodifiableList(elements); }

    /**
     * Returns the element at the given 0-based index, or
     * {@link JsonNull#INSTANCE} if out of range.
     *
     * @param index the 0-based index
     * @return the element, or {@code JsonNull.INSTANCE}
     * @since 1.0.0
     */
    public JsonValue get(int index) {
        if (index < 0 || index >= elements.size()) return JsonNull.INSTANCE;
        return elements.get(index);
    }

    /**
     * Returns the element at the given index as a string.
     *
     * @param index the 0-based index
     * @return the string value, or null
     * @since 1.0.0
     */
    public String getString(int index) { return get(index).asString(); }

    /**
     * Returns the element at the given index as an int.
     *
     * @param index the 0-based index
     * @return the int value
     * @throws JsonException if the element is not numeric
     * @since 1.0.0
     */
    public int getInt(int index) { return get(index).asInt(); }

    /**
     * Returns the element at the given index as a long.
     *
     * @param index the 0-based index
     * @return the long value
     * @throws JsonException if the element is not numeric
     * @since 1.0.0
     */
    public long getLong(int index) { return get(index).asLong(); }

    /**
     * Returns the element at the given index as a double.
     *
     * @param index the 0-based index
     * @return the double value
     * @throws JsonException if the element is not numeric
     * @since 1.0.0
     */
    public double getDouble(int index) { return get(index).asDouble(); }

    /**
     * Returns the element at the given index as a boolean.
     *
     * @param index the 0-based index
     * @return the boolean value
     * @throws JsonException if the element is not a boolean
     * @since 1.0.0
     */
    public boolean getBoolean(int index) { return get(index).asBoolean(); }

    /**
     * Returns the element at the given index as a nested object.
     *
     * @param index the 0-based index
     * @return the object, or empty
     * @since 1.0.0
     */
    public JsonObject getObject(int index) { return get(index).asObject(); }

    /**
     * Returns the element at the given index as a nested array.
     *
     * @param index the 0-based index
     * @return the array, or empty
     * @since 1.0.0
     */
    public JsonArray getArray(int index) { return get(index).asArray(); }

    // --------------------------------------------------------------- mutators

    /**
     * Adds a string element.
     *
     * @param value the value (null → JSON null)
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(String value) {
        elements.add(value == null ? JsonNull.INSTANCE : new JsonString(value));
        return this;
    }

    /**
     * Adds an int element.
     *
     * @param value the value
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(int value) { elements.add(new JsonNumber(Integer.toString(value))); return this; }

    /**
     * Adds a long element.
     *
     * @param value the value
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(long value) { elements.add(new JsonNumber(Long.toString(value))); return this; }

    /**
     * Adds a double element.
     *
     * @param value the value
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(double value) { elements.add(JsonNumber.of(value)); return this; }

    /**
     * Adds a boolean element.
     *
     * @param value the value
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(boolean value) { elements.add(JsonBoolean.of(value)); return this; }

    /**
     * Adds a JSON value element.
     *
     * @param value the value (null → JSON null)
     * @return this array
     * @since 1.0.0
     */
    public JsonArray add(JsonValue value) {
        elements.add(value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    /**
     * Adds a JSON null element.
     *
     * @return this array
     * @since 1.0.0
     */
    public JsonArray addNull() { elements.add(JsonNull.INSTANCE); return this; }

    /**
     * Removes the element at the given index (if present).
     *
     * @param index the 0-based index
     * @return this array
     * @since 1.0.0
     */
    public JsonArray removeAt(int index) {
        if (index >= 0 && index < elements.size()) elements.remove(index);
        return this;
    }

    /**
     * Removes all elements.
     *
     * @return this array
     * @since 1.0.0
     */
    public JsonArray clear() { elements.clear(); return this; }

    // ------------------------------------------------------------ conversion

    /**
     * Returns the compact JSON text for this array.
     *
     * @return the compact JSON
     * @see #toJson(boolean)
     * @since 1.0.0
     */
    @Override
    public String toJson() { return JsonWriter.write(this, false); }

    /**
     * Returns the JSON text for this array — compact or pretty-printed.
     *
     * @param pretty whether to pretty-print
     * @return the JSON text
     * @see #toJson()
     * @since 1.0.0
     */
    @Override
    public String toJson(boolean pretty) { return JsonWriter.write(this, pretty); }

    /**
     * Returns the compact JSON text for this array.
     *
     * @return the compact JSON
     * @since 1.0.0
     */
    @Override
    public String toString() { return toJson(); }

    /**
     * Two arrays are equal when their element lists are equal —
     * order-sensitive, unlike {@link JsonObject}.
     *
     * @param other the array to compare
     * @return {@code true} if the element lists are equal
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonArray array && array.elements.equals(elements);
    }

    /**
     * Hash code matching {@link #equals(Object)}.
     *
     * @return the hash code
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return elements.hashCode(); }
}
