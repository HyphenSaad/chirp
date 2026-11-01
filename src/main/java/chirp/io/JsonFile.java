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
 * Friendly JSON-file operations.
 */
public final class JsonFile {

    public enum Key { PRETTY, INDENT }

    private JsonFile() {}

    // ============================================================== read

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
    public static JsonValue load(String path) { return read(path); }
    public static JsonValue parseJson(String path) { return read(path); }

    public static JsonObject readObject(String path) {
        JsonValue value = read(path);
        if (value instanceof JsonObject object) return object;
        if (value != null) Logger.error(path + " does not contain a JSON object");
        return new JsonObject();
    }
    public static JsonObject loadObject(String path) { return readObject(path); }
    public static JsonObject asObject(String path) { return readObject(path); }
    public static JsonObject readObj(String path) { return readObject(path); }

    public static JsonArray readArray(String path) {
        JsonValue value = read(path);
        if (value instanceof JsonArray array) return array;
        if (value != null) Logger.error(path + " does not contain a JSON array");
        return new JsonArray();
    }
    public static JsonArray loadArray(String path) { return readArray(path); }
    public static JsonArray asArray(String path) { return readArray(path); }
    public static JsonArray readArr(String path) { return readArray(path); }

    // ============================================================== write

    public static boolean write(String path, JsonValue value) { return write(path, value, false); }
    public static boolean writeCompact(String path, JsonValue value) { return write(path, value, false); }
    public static boolean writePretty(String path, JsonValue value) { return write(path, value, true); }

    public static boolean save(String path, JsonValue value) { return write(path, value); }
    public static boolean saveJSON(String path, JsonValue value) { return write(path, value); }
    public static boolean saveFile(String path, JsonValue value) { return write(path, value); }

    public static boolean saveCompact(String path, JsonValue value) { return writeCompact(path, value); }
    public static boolean writeMinified(String path, JsonValue value) { return writeCompact(path, value); }

    public static boolean savePretty(String path, JsonValue value) { return writePretty(path, value); }

    /** Alias — kept for API symmetry with {@code saveCompact}. */
    public static boolean writeFormatted(String path, JsonValue v) {
        return writePretty(path, v);
    }

    /** Key-value overload — {@code write(path, value, Key.PRETTY, true)}. */
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

    public static String toCompactString(JsonValue value) { return Json.write(value, false); }
    public static String toPrettyString(JsonValue value) { return Json.write(value, true); }

    // ============================================================== helpers

    public static boolean isValid(String path) {
        if (!Files.exists(Path.of(path))) return false;
        return read(path) != null;
    }
    public static boolean validate(String path) { return isValid(path); }

    public static boolean exists(String path) { return Files.exists(Path.of(path)); }

    public static boolean delete(String path) {
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            return false;
        }
    }
    public static boolean remove(String path) { return delete(path); }

    public static JsonObject merge(String firstPath, String secondPath) {
        JsonObject a = readObject(firstPath);
        JsonObject b = readObject(secondPath);
        JsonObject merged = new JsonObject();
        for (var e : a.asMap().entrySet()) merged.put(e.getKey(), e.getValue());
        for (var e : b.asMap().entrySet()) merged.put(e.getKey(), e.getValue());
        return merged;
    }
    public static JsonObject mergeObjects(String firstPath, String secondPath) { return merge(firstPath, secondPath); }
}
