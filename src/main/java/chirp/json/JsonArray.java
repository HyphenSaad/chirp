package chirp.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered JSON array — {@code [ value, ... ]}.
 *
 * <pre>{@code
 * JsonArray tags = new JsonArray()
 *     .add("oop").add("java").add("chirp");
 * }</pre>
 */
public final class JsonArray implements JsonValue {

    private final List<JsonValue> elements = new ArrayList<>();

    public JsonArray() {}

    public static JsonArray of() { return new JsonArray(); }
    public static JsonArray empty() { return new JsonArray(); }

    public static JsonArray from(List<?> source) {
        JsonArray array = new JsonArray();
        if (source != null) {
            for (Object item : source) {
                array.add(fromJava(item));
            }
        }
        return array;
    }

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

    public int size() { return elements.size(); }
    public boolean isEmpty() { return elements.isEmpty(); }
    public List<JsonValue> asList() { return Collections.unmodifiableList(elements); }

    public JsonValue get(int index) {
        if (index < 0 || index >= elements.size()) return JsonNull.INSTANCE;
        return elements.get(index);
    }

    public String getString(int index) { return get(index).asString(); }
    public int getInt(int index) { return get(index).asInt(); }
    public long getLong(int index) { return get(index).asLong(); }
    public double getDouble(int index) { return get(index).asDouble(); }
    public boolean getBoolean(int index) { return get(index).asBoolean(); }
    public JsonObject getObject(int index) { return get(index).asObject(); }
    public JsonArray getArray(int index) { return get(index).asArray(); }

    // --------------------------------------------------------------- mutators

    public JsonArray add(String value) {
        elements.add(value == null ? JsonNull.INSTANCE : new JsonString(value));
        return this;
    }
    public JsonArray add(int value) { elements.add(new JsonNumber(Integer.toString(value))); return this; }
    public JsonArray add(long value) { elements.add(new JsonNumber(Long.toString(value))); return this; }
    public JsonArray add(double value) { elements.add(JsonNumber.of(value)); return this; }
    public JsonArray add(boolean value) { elements.add(JsonBoolean.of(value)); return this; }

    public JsonArray add(JsonValue value) {
        elements.add(value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    public JsonArray addNull() { elements.add(JsonNull.INSTANCE); return this; }
    public JsonArray removeAt(int index) {
        if (index >= 0 && index < elements.size()) elements.remove(index);
        return this;
    }
    public JsonArray clear() { elements.clear(); return this; }

    // ------------------------------------------------------------ conversion

    @Override
    public String toJson() { return JsonWriter.write(this, false); }
    @Override
    public String toJson(boolean pretty) { return JsonWriter.write(this, pretty); }
    @Override
    public String toString() { return toJson(); }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonArray array && array.elements.equals(elements);
    }
    @Override
    public int hashCode() { return elements.hashCode(); }
}
