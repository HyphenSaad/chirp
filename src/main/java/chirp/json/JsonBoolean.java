package chirp.json;

/**
 * A JSON boolean — {@code true} or {@code false}.
 *
 * <p>Two singletons are provided: {@link #TRUE} and {@link #FALSE}. Use
 * {@link #of(boolean)} to select the right one.</p>
 *
 * <h2>Related</h2>
 * <p>For the base value type, see {@link JsonValue}. For the parser and
 * serialiser entry points, see {@link Json}.</p>
 *
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonBoolean implements JsonValue {

    /** The JSON {@code true} literal. */
    public static final JsonBoolean TRUE = new JsonBoolean(true);

    /** The JSON {@code false} literal. */
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) { this.value = value; }

    /**
     * Factory — returns {@link #TRUE} or {@link #FALSE}.
     *
     * @param value the boolean
     * @return the corresponding singleton
     * @since 1.0.0
     */
    public static JsonBoolean of(boolean value) { return value ? TRUE : FALSE; }

    /**
     * Returns the Java boolean value.
     *
     * @return the value
     * @since 1.0.0
     */
    public boolean value() { return value; }

    /**
     * Returns the JSON text — {@code "true"} or {@code "false"}.
     *
     * @return the JSON text
     * @since 1.0.0
     */
    @Override
    public String toJson() { return Boolean.toString(value); }

    /**
     * Returns the JSON text — {@code "true"} or {@code "false"}.
     *
     * @return the JSON text
     * @since 1.0.0
     */
    @Override
    public String toString() { return Boolean.toString(value); }

    /**
     * Two JSON booleans are equal when their values are equal.
     *
     * @param other the boolean to compare
     * @return {@code true} if the values are equal
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonBoolean bool && bool.value == value;
    }

    /**
     * Hash code matching {@link #equals(Object)}.
     *
     * @return the hash code
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return Boolean.hashCode(value); }
}
