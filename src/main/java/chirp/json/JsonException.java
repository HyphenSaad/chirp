package chirp.json;

/**
 * Thrown when JSON input is malformed, or when a value is used with the
 * wrong type.
 *
 * <p>Extends {@link RuntimeException}, so it does not need to be
 * declared in method signatures. Every public method in the
 * {@code chirp.json} package either returns a value or throws this
 * exception — no other exception type escapes the package.</p>
 *
 * <h2>Common causes</h2>
 * <ul>
 *   <li><b>Parse errors</b> — malformed JSON at parse time.
 *       {@link Json#parse(String)} and {@link JsonParser#parse(String)}
 *       throw when the input does not conform to the JSON grammar.</li>
 *   <li><b>Type errors</b> — calling a typed getter
 *       ({@code asInt}, {@code asBoolean}, {@code asObject}, ...) on a
 *       {@link JsonValue} whose runtime type does not match.
 *       For example {@code new JsonString("abc").asInt()} throws
 *       because a string is not a number.</li>
 *   <li><b>Construction errors</b> — passing an invalid number
 *       literal to {@link JsonNumber#JsonNumber(String)}, or an
 *       infinite/NaN value to {@link JsonNumber#of(double)}.</li>
 * </ul>
 *
 * <h2>Typical catch</h2>
 * <pre>{@code
 * try {
 *     JsonObject config = Json.parseObject(jsonText);
 *     int port = config.getInt("port");
 *     startServer(port);
 * } catch (JsonException e) {
 *     System.err.println("Invalid config: " + e.getMessage());
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the facade that hides this class from most callers, see
 * {@link Json}. For value type-checking helpers that avoid throwing,
 * see {@link JsonValue#isObject()}, {@link JsonValue#isString()}, and
 * the other {@code is*} methods.</p>
 *
 * @see Json
 * @see JsonParser
 * @see JsonValue
 * @since 1.0.0
 */
public class JsonException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new JSON exception with the given message.
     *
     * @param message the detail message
     * @since 1.0.0
     */
    public JsonException(String message) {
        super(message);
    }

    /**
     * Constructs a new JSON exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     * @since 1.0.0
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
