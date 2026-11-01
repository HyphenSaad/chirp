package chirp.io;

import chirp.internal.Logger;
import chirp.json.Json;
import chirp.json.JsonArray;
import chirp.json.JsonException;
import chirp.json.JsonObject;
import chirp.json.JsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File-level helpers for reading and writing JSON documents.
 *
 * <p>Thin wrapper around {@link chirp.json.Json} that adds file I/O and
 * the failure-with-friendly-message behaviour used everywhere in
 * {@code chirp.io}: read methods return an empty object or array rather
 * than throwing, write methods return {@code false} on failure.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Read raw value</b> — {@link #read(String)}</li>
 *   <li><b>Read as object</b> — {@link #readObject(String)}</li>
 *   <li><b>Read as array</b> — {@link #readArray(String)}</li>
 *   <li><b>Write</b> — {@link #write(String, JsonValue)},
 *       {@link #writePretty(String, JsonValue)},
 *       {@link #writeCompact(String, JsonValue)}</li>
 *   <li><b>Validate</b> — {@link #isValid(String)}</li>
 *   <li><b>Merge</b> — {@link #merge(String, String)}</li>
 *   <li><b>Filesystem</b> — {@link #exists(String)}, {@link #delete(String)}</li>
 * </ul>
 *
 * <h2>What happens on failure</h2>
 * <p>{@link #read(String)} returns {@code null} for a missing or
 * malformed file. {@link #readObject(String)} returns an <b>empty</b>
 * {@link JsonObject} (never {@code null}) — so chained {@code get(...)}
 * calls work without null-checks. Same for {@link #readArray(String)}.
 * This is the same "empty object rather than throw" policy as the rest
 * of {@code chirp.io}.</p>
 *
 * <h2>Pretty vs. compact</h2>
 * <ul>
 *   <li>{@link #writePretty(String, JsonValue)} — human-readable, with
 *       indentation. Use this for config files and hand-editable data.</li>
 *   <li>{@link #writeCompact(String, JsonValue)} — single line, no
 *       whitespace. Use this for network payloads and machine-generated
 *       data.</li>
 *   <li>{@link #write(String, JsonValue)} — alias of {@code writeCompact}.</li>
 * </ul>
 *
 * <h2>Example — read a config file and update it</h2>
 * <pre>{@code
 * JsonObject config = JsonFile.readObject("config.json");
 *
 * // Read with defaults
 * String host = config.getString("host", "localhost");
 * int    port = config.getInt("port", 8080);
 *
 * // Update and write back pretty-printed
 * config.put("port", port + 1);
 * JsonFile.writePretty("config.json", config);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the JSON value model (JsonObject, JsonArray, etc.), see
 * {@link chirp.json.Json}. For general file operations, see
 * {@link FileIO}. For CSV files, see {@link CsvFile}.</p>
 *
 * @see chirp.json.Json
 * @see chirp.json.JsonObject
 * @see chirp.json.JsonArray
 * @see CsvFile
 * @see FileIO
 * @since 1.0.0
 */
public final class JsonFile {

    /**
     * Key enum for the key-value write overload.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Whether to pretty-print the output — {@link Boolean}. */
        PRETTY,
        /** Reserved for future use. */
        INDENT
    }

    private JsonFile() {}

    // ============================================================== read

    /**
     * Reads and parses the JSON file, returning the raw
     * {@link JsonValue} or {@code null} on failure.
     *
     * <p>Use this when the top-level value could be any JSON type
     * (object, array, string, number, boolean, or null). For the common
     * cases, use {@link #readObject(String)} or {@link #readArray(String)}
     * — they never return {@code null}.</p>
     *
     * @param path the file path (may be null → null)
     * @return the parsed value, or {@code null} on failure
     * @see #load(String)
     * @see #parseJson(String)
     * @see #readObject(String)
     * @see #readArray(String)
     * @since 1.0.0
     */
    public static JsonValue read(String path) {
        try {
            return Json.parse(Files.readString(Path.of(path)));
        } catch (IOException e) {
            Logger.error("Cannot read JSON from " + path);
            return null;
        } catch (JsonException e) {
            Logger.error("Malformed JSON in " + path + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Alias of {@link #read(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the parsed value, or null
     * @see #read(String)
     * @since 1.0.0
     */
    public static JsonValue load(String path) { return read(path); }

    /**
     * Alias of {@link #read(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the parsed value, or null
     * @see #read(String)
     * @since 1.0.0
     */
    public static JsonValue parseJson(String path) { return read(path); }

    /**
     * Reads the JSON file as an object. Returns an <b>empty</b>
     * {@link JsonObject} on failure — never null.
     *
     * <p>The empty-object return is deliberate: chained {@code get(...)}
     * calls work without null-checks. {@code readObject("missing.json").getString("key")}
     * returns {@code null} (a missing value) rather than throwing from a
     * null reference.</p>
     *
     * @param path the file path (may be null → empty)
     * @return the parsed object, or empty on failure
     * @see #loadObject(String)
     * @see #asObject(String)
     * @see #readObj(String)
     * @see #read(String)
     * @since 1.0.0
     */
    public static JsonObject readObject(String path) {
        JsonValue value = read(path);
        if (value instanceof JsonObject object) return object;
        if (value != null) Logger.error(path + " does not contain a JSON object");
        return new JsonObject();
    }

    /**
     * Alias of {@link #readObject(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed object, or empty
     * @see #readObject(String)
     * @since 1.0.0
     */
    public static JsonObject loadObject(String path) { return readObject(path); }

    /**
     * Alias of {@link #readObject(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed object, or empty
     * @see #readObject(String)
     * @since 1.0.0
     */
    public static JsonObject asObject(String path) { return readObject(path); }

    /**
     * Alias of {@link #readObject(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed object, or empty
     * @see #readObject(String)
     * @since 1.0.0
     */
    public static JsonObject readObj(String path) { return readObject(path); }

    /**
     * Reads the JSON file as an array. Returns an <b>empty</b>
     * {@link JsonArray} on failure — never null.
     *
     * @param path the file path (may be null → empty)
     * @return the parsed array, or empty on failure
     * @see #loadArray(String)
     * @see #asArray(String)
     * @see #readArr(String)
     * @see #read(String)
     * @since 1.0.0
     */
    public static JsonArray readArray(String path) {
        JsonValue value = read(path);
        if (value instanceof JsonArray array) return array;
        if (value != null) Logger.error(path + " does not contain a JSON array");
        return new JsonArray();
    }

    /**
     * Alias of {@link #readArray(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the parsed array, or empty
     * @see #readArray(String)
     * @since 1.0.0
     */
    public static JsonArray loadArray(String path) { return readArray(path); }

    /**
     * Alias of {@link #readArray(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the parsed array, or empty
     * @see #readArray(String)
     * @since 1.0.0
     */
    public static JsonArray asArray(String path) { return readArray(path); }

    /**
     * Alias of {@link #readArray(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the parsed array, or empty
     * @see #readArray(String)
     * @since 1.0.0
     */
    public static JsonArray readArr(String path) { return readArray(path); }

    // ============================================================== write

    /**
     * Writes the JSON value to the file in compact form (no whitespace,
     * no newlines).
     *
     * <p>Use this for machine-generated payloads. For hand-editable
     * config files, use {@link #writePretty(String, JsonValue)}.</p>
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #writeCompact(String, JsonValue)
     * @see #writePretty(String, JsonValue)
     * @see #write(String, JsonValue, boolean)
     * @since 1.0.0
     */
    public static boolean write(String path, JsonValue value) { return write(path, value, false); }

    /**
     * Alias of {@link #write(String, JsonValue)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean writeCompact(String path, JsonValue value) { return write(path, value, false); }

    /**
     * Writes the JSON value to the file in pretty-printed form (two-space
     * indentation, one value per line).
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #savePretty(String, JsonValue)
     * @see #writeFormatted(String, JsonValue)
     * @see #write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean writePretty(String path, JsonValue value) { return write(path, value, true); }

    /**
     * Alias of {@link #write(String, JsonValue)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean save(String path, JsonValue value) { return write(path, value); }

    /**
     * Alias of {@link #write(String, JsonValue)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean saveJSON(String path, JsonValue value) { return write(path, value); }

    /**
     * Alias of {@link #write(String, JsonValue)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean saveFile(String path, JsonValue value) { return write(path, value); }

    /**
     * Alias of {@link #writeCompact(String, JsonValue)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #writeCompact(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean saveCompact(String path, JsonValue value) { return writeCompact(path, value); }

    /**
     * Alias of {@link #writeCompact(String, JsonValue)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #writeCompact(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean writeMinified(String path, JsonValue value) { return writeCompact(path, value); }

    /**
     * Alias of {@link #writePretty(String, JsonValue)} — see that method
     * for full documentation.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see #writePretty(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean savePretty(String path, JsonValue value) { return writePretty(path, value); }

    /**
     * Alias of {@link #writePretty(String, JsonValue)} — see that method
     * for full documentation.
     *
     * @param path the file path
     * @param v the JSON value
     * @return {@code true} on success
     * @see #writePretty(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean writeFormatted(String path, JsonValue v) { return writePretty(path, v); }

    /**
     * Writes the JSON value with key-value options — allows toggling
     * pretty vs. compact via {@link Key#PRETTY}.
     *
     * <pre>{@code
     * JsonFile.write("out.json", value, JsonFile.Key.PRETTY, true);
     * }</pre>
     *
     * @param path the file path
     * @param value the JSON value
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @see #write(String, JsonValue, boolean)
     * @since 1.0.0
     */
    public static boolean write(String path, JsonValue value, Object... keyValues) {
        boolean pretty = false;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.PRETTY && keyValues[i + 1] instanceof Boolean b) {
                    pretty = b;
                }
            }
        }
        return write(path, value, pretty);
    }

    /**
     * Writes the JSON value with an explicit pretty-print flag.
     *
     * @param path the file path
     * @param value the JSON value
     * @param pretty whether to pretty-print
     * @return {@code true} on success
     * @see #write(String, JsonValue)
     * @see #writePretty(String, JsonValue)
     * @see #writeCompact(String, JsonValue)
     * @since 1.0.0
     */
    public static boolean write(String path, JsonValue value, boolean pretty) {
        try {
            Files.writeString(Path.of(path), Json.write(value, pretty));
            return true;
        } catch (IOException e) {
            Logger.error("Cannot write " + path);
            return false;
        }
    }

    // ============================================================== string returns

    /**
     * Serialises the JSON value to a compact string — useful for logging
     * or inline JSON.
     *
     * @param value the JSON value
     * @return the compact JSON string
     * @see Json#compact(JsonValue)
     * @since 1.0.0
     */
    public static String toCompactString(JsonValue value) { return Json.write(value, false); }

    /**
     * Serialises the JSON value to a pretty-printed string.
     *
     * @param value the JSON value
     * @return the pretty-printed JSON string
     * @see Json#pretty(JsonValue)
     * @since 1.0.0
     */
    public static String toPrettyString(JsonValue value) { return Json.write(value, true); }

    // ============================================================== helpers

    /**
     * Checks whether the file exists and parses as valid JSON.
     *
     * @param path the file path (may be null → false)
     * @return {@code true} if the file exists and contains valid JSON
     * @see #validate(String)
     * @see #exists(String)
     * @since 1.0.0
     */
    public static boolean isValid(String path) {
        if (path == null || !Files.exists(Path.of(path))) return false;
        return read(path) != null;
    }

    /**
     * Alias of {@link #isValid(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return {@code true} if the file exists and contains valid JSON
     * @see #isValid(String)
     * @since 1.0.0
     */
    public static boolean validate(String path) { return isValid(path); }

    /**
     * Checks whether the file exists (regardless of content).
     *
     * @param path the file path (may be null → false)
     * @return {@code true} if the file exists
     * @see #isValid(String)
     * @since 1.0.0
     */
    public static boolean exists(String path) {
        if (path == null) return false;
        return Files.exists(Path.of(path));
    }

    /**
     * Deletes the JSON file.
     *
     * @param path the file path (may be null → false)
     * @return {@code true} if the file existed and was deleted
     * @see #remove(String)
     * @since 1.0.0
     */
    public static boolean delete(String path) {
        if (path == null) return false;
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #delete(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return {@code true} on success
     * @see #delete(String)
     * @since 1.0.0
     */
    public static boolean remove(String path) { return delete(path); }

    /**
     * Merges the JSON objects from two files into a single object. Keys
     * from the second file override the first on collision.
     *
     * <pre>{@code
     * // a.json = {"a": 1, "b": 2}
     * // b.json = {"b": 99, "c": 3}
     * JsonObject merged = JsonFile.merge("a.json", "b.json");
     * // {"a": 1, "b": 99, "c": 3}
     * }</pre>
     *
     * @param firstPath the first file (lower priority)
     * @param secondPath the second file (higher priority)
     * @return the merged object, or empty on failure
     * @see #mergeObjects(String, String)
     * @since 1.0.0
     */
    public static JsonObject merge(String firstPath, String secondPath) {
        JsonObject a = readObject(firstPath);
        JsonObject b = readObject(secondPath);
        JsonObject merged = new JsonObject();
        for (var e : a.asMap().entrySet()) merged.put(e.getKey(), e.getValue());
        for (var e : b.asMap().entrySet()) merged.put(e.getKey(), e.getValue());
        return merged;
    }

    /**
     * Alias of {@link #merge(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param firstPath the first file
     * @param secondPath the second file
     * @return the merged object
     * @see #merge(String, String)
     * @since 1.0.0
     */
    public static JsonObject mergeObjects(String firstPath, String secondPath) {
        return merge(firstPath, secondPath);
    }
}
