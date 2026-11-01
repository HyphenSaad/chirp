package chirp.json;

/**
 * A JSON string literal — a sequence of characters enclosed in double
 * quotes in the JSON text.
 *
 * <p>{@link #value()} returns the unescaped Java string. When serialised
 * back to JSON, quotes, backslashes, and control characters are escaped
 * automatically.</p>
 *
 * <h2>Related</h2>
 * <p>For the base value type, see {@link JsonValue}. For the parser and
 * serialiser entry points, see {@link Json}.</p>
 *
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonString implements JsonValue {

    private final String value;

    /**
     * Constructs a JSON string wrapping the given Java string. A null
     * argument is treated as the empty string — use
     * {@link JsonNull#INSTANCE} for a genuine JSON null.
     *
     * @param value the Java string (null → "")
     * @see #of(String)
     * @since 1.0.0
     */
    public JsonString(String value) {
        this.value = value == null ? "" : value;
    }

    /**
     * Factory — a JSON string wrapping the given Java string.
     *
     * @param value the Java string (null → "")
     * @return a new {@code JsonString}
     * @see #JsonString(String)
     * @since 1.0.0
     */
    public static JsonString of(String value) { return new JsonString(value); }

    /**
     * Returns the unescaped Java string value.
     *
     * @return the value
     * @since 1.0.0
     */
    public String value() { return value; }

    /**
     * Returns the JSON-escaped text — quoted and escaped.
     *
     * @return the JSON text
     * @since 1.0.0
     */
    @Override
    public String toJson() { return JsonWriter.quote(value); }

    /**
     * Returns the unescaped value — matching {@link #value()}, not the
     * JSON text. This is the conventional behaviour for
     * {@code toString()} on a value type.
     *
     * @return the value
     * @since 1.0.0
     */
    @Override
    public String toString() { return value; }

    /**
     * Two JSON strings are equal when their values are equal.
     *
     * @param other the string to compare
     * @return {@code true} if the values are equal
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonString string && string.value.equals(value);
    }

    /**
     * Hash code matching {@link #equals(Object)}.
     *
     * @return the hash code
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return value.hashCode(); }
}
