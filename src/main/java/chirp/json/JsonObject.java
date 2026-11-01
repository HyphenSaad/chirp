package chirp.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An ordered JSON object — {@code { "key": value, ... }}.
 *
 * <p>Insertion order is preserved when writing. Equality is map-based
 * (order-insensitive), as JSON specifies.</p>
 *
 * <pre>{@code
 * JsonObject o = new JsonObject()
 *     .put("name", "Saad")
 *     .put("age", 20)
 *     .put("active", true);
 * }</pre>
 */
public final class JsonObject implements JsonValue {

    private final Map<String, JsonValue> members = new LinkedHashMap<>();

    public JsonObject() {}

    public static JsonObject of() { return new JsonObject(); }
    public static JsonObject empty() { return new JsonObject(); }

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

    public int size() { return members.size(); }
    public boolean isEmpty() { return members.isEmpty(); }
    public boolean has(String key) { return members.containsKey(key); }
    public Set<String> keys() { return Collections.unmodifiableSet(members.keySet()); }
    public Map<String, JsonValue> asMap() { return Collections.unmodifiableMap(members); }

    public JsonValue get(String key) {
        JsonValue value = members.get(key);
        return value != null ? value : JsonNull.INSTANCE;
    }

    public String getString(String key) { return get(key).asString(); }
    public int getInt(String key) { return get(key).asInt(); }
    public long getLong(String key) { return get(key).asLong(); }
    public double getDouble(String key) { return get(key).asDouble(); }
    public boolean getBoolean(String key) { return get(key).asBoolean(); }
    public JsonObject getObject(String key) { return get(key).asObject(); }
    public JsonArray getArray(String key) { return get(key).asArray(); }

    public String getString(String key, String defaultValue) {
        return has(key) && !get(key).isNull() ? getString(key) : defaultValue;
    }
    public int getInt(String key, int defaultValue) {
        return has(key) && !get(key).isNull() ? getInt(key) : defaultValue;
    }
    public long getLong(String key, long defaultValue) {
        return has(key) && !get(key).isNull() ? getLong(key) : defaultValue;
    }
    public double getDouble(String key, double defaultValue) {
        return has(key) && !get(key).isNull() ? getDouble(key) : defaultValue;
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return has(key) && !get(key).isNull() ? getBoolean(key) : defaultValue;
    }

    // --------------------------------------------------------------- setters

    public JsonObject put(String key, String value) {
        members.put(key, value == null ? JsonNull.INSTANCE : new JsonString(value));
        return this;
    }
    public JsonObject put(String key, int value) {
        members.put(key, new JsonNumber(Integer.toString(value)));
        return this;
    }
    public JsonObject put(String key, long value) {
        members.put(key, new JsonNumber(Long.toString(value)));
        return this;
    }
    public JsonObject put(String key, double value) { members.put(key, JsonNumber.of(value)); return this; }
    public JsonObject put(String key, boolean value) { members.put(key, JsonBoolean.of(value)); return this; }

    public JsonObject put(String key, JsonValue value) {
        members.put(key, value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    public JsonObject putNull(String key) { members.put(key, JsonNull.INSTANCE); return this; }
    public JsonObject remove(String key) { members.remove(key); return this; }
    public JsonObject clear() { members.clear(); return this; }

    // ------------------------------------------------------------ conversion

    @Override
    public String toJson() { return JsonWriter.write(this, false); }
    @Override
    public String toJson(boolean pretty) { return JsonWriter.write(this, pretty); }
    @Override
    public String toString() { return toJson(); }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonObject object && object.members.equals(members);
    }
    @Override
    public int hashCode() { return members.hashCode(); }
}
