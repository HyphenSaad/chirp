package chirp.json;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Facade for the JSON API. One import, everything you need.
 *
 * <pre>{@code
 * import chirp.json.Json;
 *
 * JsonObject o = Json.parseObject("{ \"name\": \"Saad\", \"age\": 20 }");
 * String    n = o.getString("name");
 *
 * JsonObject built = Json.object()
 *     .put("name", "Saad")
 *     .put("age", 20);
 *
 * String pretty = built.toJson(true);
 * }</pre>
 */
public final class Json {

    private Json() {}

    // ---------------------------------------------------------------- parse
    public static JsonValue parse(String text) { return JsonParser.parse(text); }
    public static JsonObject parseObject(String text) { return JsonParser.parseObject(text); }
    public static JsonArray parseArray(String text) { return JsonParser.parseArray(text); }

    // ---------------------------------------------------------------- build
    public static JsonObject object() { return new JsonObject(); }
    public static JsonArray array() { return new JsonArray(); }

    // ------------------------------------------------------------- serialize
    public static String write(JsonValue value) { return JsonWriter.write(value, false); }
    public static String compact(JsonValue value) { return JsonWriter.write(value, false); }
    public static String pretty(JsonValue value) { return JsonWriter.write(value, true); }
    public static String write(JsonValue value, boolean p) { return JsonWriter.write(value, p); }

    /** @return {@code true} when the input parses as valid JSON */
    public static boolean isValid(String text) {
        try {
            JsonParser.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------------------------------------------------- single-use file helpers
    public static JsonValue readFile(String path) {
        try {
            return JsonParser.parse(Files.readString(Path.of(path)));
        } catch (Exception e) {
            throw new JsonException("Cannot read JSON from " + path, e);
        }
    }

    public static boolean writeFile(String path, JsonValue value, boolean prettyPrint) {
        try {
            Files.writeString(Path.of(path), JsonWriter.write(value, prettyPrint));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
