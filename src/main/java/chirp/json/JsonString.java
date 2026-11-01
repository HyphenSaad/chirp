package chirp.json;

/** A JSON string literal. */
public final class JsonString implements JsonValue {

    private final String value;

    public JsonString(String value) {
        this.value = value == null ? "" : value;
    }

    public static JsonString of(String value) { return new JsonString(value); }

    public String value() { return value; }

    @Override
    public String toJson() { return JsonWriter.quote(value); }
    @Override
    public String toString() { return value; }
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonString string && string.value.equals(value);
    }
    @Override
    public int hashCode() { return value.hashCode(); }
}
