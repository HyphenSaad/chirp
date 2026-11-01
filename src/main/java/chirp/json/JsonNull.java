package chirp.json;

/** The JSON {@code null} literal. */
public final class JsonNull implements JsonValue {

    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override
    public String toJson() { return "null"; }
    @Override
    public String toString() { return "null"; }
    @Override
    public boolean equals(Object other) { return other instanceof JsonNull; }
    @Override
    public int hashCode() { return 0; }
}
