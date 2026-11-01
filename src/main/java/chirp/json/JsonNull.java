package chirp.json;

/**
 * The JSON null literal.
 *
 * <p>There is exactly one instance, {@link #INSTANCE}, which is returned
 * from {@link JsonObject#get(String)} and
 * {@link JsonArray#get(int)} when the requested key or index is absent.
 * This is how missing values are represented without returning Java
 * {@code null}.</p>
 *
 * <p>To check whether a value is JSON null, use
 * {@link JsonValue#isNull()} — never compare with {@code == null},
 * because a JSON null is a real object, not a null reference.</p>
 *
 * <h2>Related</h2>
 * <p>For the base value type, see {@link JsonValue}. For the parser and
 * serialiser entry points, see {@link Json}.</p>
 *
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonNull implements JsonValue {

    /** The single JSON null instance. */
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    /**
     * Returns the JSON text — {@code "null"}.
     *
     * @return the JSON text
     * @since 1.0.0
     */
    @Override
    public String toJson() { return "null"; }

    /**
     * Returns the JSON text — {@code "null"}.
     *
     * @return the JSON text
     * @since 1.0.0
     */
    @Override
    public String toString() { return "null"; }

    /**
     * All {@code JsonNull} instances are equal (there is only one).
     *
     * @param other the object to compare
     * @return {@code true} if {@code other} is a {@code JsonNull}
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) { return other instanceof JsonNull; }

    /**
     * Constant hash code.
     *
     * @return 0
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return 0; }
}
