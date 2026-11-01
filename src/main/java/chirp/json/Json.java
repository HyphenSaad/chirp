package chirp.json;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Facade for the JSON API — one import, everything you need.
 *
 * <p>{@code Json} is the entry point for parsing, building, and
 * serialising JSON. The type model ({@link JsonObject},
 * {@link JsonArray}, and their primitive siblings) is documented in
 * each type; this class just provides convenient static methods.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Parse</b> — {@link #parse(String)},
 *       {@link #parseObject(String)}, {@link #parseArray(String)}</li>
 *   <li><b>Build</b> — {@link #object()}, {@link #array()}</li>
 *   <li><b>Serialise</b> — {@link #write(JsonValue)},
 *       {@link #compact(JsonValue)}, {@link #pretty(JsonValue)}</li>
 *   <li><b>Validate</b> — {@link #isValid(String)}</li>
 *   <li><b>File helpers</b> — {@link #readFile(String)},
 *       {@link #writeFile(String, JsonValue, boolean)}</li>
 * </ul>
 *
 * <h2>Example — parse and access</h2>
 * <pre>{@code
 * import chirp.json.Json;
 *
 * JsonObject o = Json.parseObject("{ \"name\": \"Saad\", \"age\": 20 }");
 * String    n = o.getString("name");    // "Saad"
 * int       a = o.getInt("age");        // 20
 * }</pre>
 *
 * <h2>Example — build and serialise</h2>
 * <pre>{@code
 * JsonObject obj = Json.object()
 *     .put("name", "Saad")
 *     .put("age", 20);
 *
 * String compact = obj.toJson();       // {"name":"Saad","age":20}
 * String pretty  = Json.pretty(obj);   // multi-line, indented
 * }</pre>
 *
 * <h2>Example — parse an array</h2>
 * <pre>{@code
 * JsonArray arr = Json.parseArray("[1, 2, 3]");
 * int second = arr.getInt(1);   // 2
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For file-level operations on JSON, see
 * {@link chirp.io.JsonFile}. For the parser and writer internals, see
 * {@link JsonParser} and {@link JsonWriter} — those are documented for
 * completeness but rarely used directly.</p>
 *
 * @see JsonObject
 * @see JsonArray
 * @see JsonValue
 * @see JsonParser
 * @see JsonWriter
 * @see chirp.io.JsonFile
 * @since 1.0.0
 */
public final class Json {

    private Json() {}

    // ---------------------------------------------------------------- parse

    /**
     * Parses the given JSON text into a {@link JsonValue}.
     *
     * <p>The top-level value may be any JSON type — object, array,
     * string, number, boolean, or null.</p>
     *
     * @param text the JSON text (may be null → {@link JsonException})
     * @return the parsed value
     * @throws JsonException if the text is not valid JSON
     * @see #parseObject(String)
     * @see #parseArray(String)
     * @since 1.0.0
     */
    public static JsonValue parse(String text) { return JsonParser.parse(text); }

    /**
     * Parses the given JSON text into a {@link JsonObject}.
     *
     * @param text the JSON text (may be null → {@link JsonException})
     * @return the parsed object
     * @throws JsonException if the text is not valid JSON, or the
     * top-level value is not an object
     * @see #parse(String)
     * @see #parseArray(String)
     * @since 1.0.0
     */
    public static JsonObject parseObject(String text) { return JsonParser.parseObject(text); }

    /**
     * Parses the given JSON text into a {@link JsonArray}.
     *
     * @param text the JSON text (may be null → {@link JsonException})
     * @return the parsed array
     * @throws JsonException if the text is not valid JSON, or the
     * top-level value is not an array
     * @see #parse(String)
     * @see #parseObject(String)
     * @since 1.0.0
     */
    public static JsonArray parseArray(String text) { return JsonParser.parseArray(text); }

    // ---------------------------------------------------------------- build

    /**
     * Factory — a new empty JSON object.
     *
     * @return a new {@code JsonObject}
     * @see JsonObject#of()
     * @since 1.0.0
     */
    public static JsonObject object() { return new JsonObject(); }

    /**
     * Factory — a new empty JSON array.
     *
     * @return a new {@code JsonArray}
     * @see JsonArray#of()
     * @since 1.0.0
     */
    public static JsonArray array() { return new JsonArray(); }

    // ------------------------------------------------------------- serialize

    /**
     * Serialises the given JSON value to compact text (no whitespace).
     *
     * @param value the JSON value (null → {@code "null"})
     * @return the compact JSON text
     * @see #compact(JsonValue)
     * @see #pretty(JsonValue)
     * @see #write(JsonValue, boolean)
     * @since 1.0.0
     */
    public static String write(JsonValue value) { return JsonWriter.write(value, false); }

    /**
     * Alias of {@link #write(JsonValue)} — see that method for full
     * documentation.
     *
     * @param value the JSON value
     * @return the compact JSON text
     * @see #write(JsonValue)
     * @since 1.0.0
     */
    public static String compact(JsonValue value) { return JsonWriter.write(value, false); }

    /**
     * Serialises the given JSON value to pretty-printed text
     * (two-space indentation, one value per line).
     *
     * @param value the JSON value (null → {@code "null"})
     * @return the pretty-printed JSON text
     * @see #write(JsonValue)
     * @since 1.0.0
     */
    public static String pretty(JsonValue value) { return JsonWriter.write(value, true); }

    /**
     * Serialises the given JSON value — compact or pretty-printed,
     * chosen by the boolean.
     *
     * @param value the JSON value
     * @param pretty whether to pretty-print
     * @return the JSON text
     * @see #write(JsonValue)
     * @see #pretty(JsonValue)
     * @since 1.0.0
     */
    public static String write(JsonValue value, boolean pretty) { return JsonWriter.write(value, pretty); }

    /**
     * Checks whether the given text parses as valid JSON.
     *
     * @param text the text to validate (may be null → false)
     * @return {@code true} if the text parses as JSON
     * @see #parse(String)
     * @since 1.0.0
     */
    public static boolean isValid(String text) {
        try {
            JsonParser.parse(text);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ----------------------------------------------------------- single-use file helpers

    /**
     * Reads and parses a JSON file.
     *
     * <p>Unlike {@link chirp.io.JsonFile#read(String)}, this method
     * <b>throws</b> {@link JsonException} on failure rather than
     * returning null. Use this when a missing or malformed file is a bug
     * to be surfaced, not a case to be handled silently.</p>
     *
     * @param path the file path
     * @return the parsed value
     * @throws JsonException if the file cannot be read or its content is
     * not valid JSON
     * @see chirp.io.JsonFile#read(String)
     * @since 1.0.0
     */
    public static JsonValue readFile(String path) {
        try {
            return JsonParser.parse(Files.readString(Path.of(path)));
        } catch (Exception e) {
            throw new JsonException("Cannot read JSON from " + path, e);
        }
    }

    /**
     * Writes the JSON value to a file.
     *
     * @param path the file path
     * @param value the JSON value
     * @param prettyPrint whether to pretty-print
     * @return {@code true} on success, {@code false} on any IO failure
     * @see chirp.io.JsonFile#write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean writeFile(String path, JsonValue value, boolean prettyPrint) {
        try {
            Files.writeString(Path.of(path), JsonWriter.write(value, prettyPrint));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
