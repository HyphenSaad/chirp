package chirp.json;

/** The JSON {@code true} / {@code false} literals. */
public final class JsonBoolean implements JsonValue {

    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) { this.value = value; }

    public static JsonBoolean of(boolean value) { return value ? TRUE : FALSE; }

    public boolean value() { return value; }

    @Override
    public String toJson() { return Boolean.toString(value); }
    @Override
    public String toString() { return Boolean.toString(value); }
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonBoolean bool && bool.value == value;
    }
    @Override
    public int hashCode() { return Boolean.hashCode(value); }
}
