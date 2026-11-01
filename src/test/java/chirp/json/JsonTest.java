package chirp.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    // ---------------------------------------------------------- primitives
    @Test
    void nullLiteral() { assertEquals("null", JsonNull.INSTANCE.toJson()); }
    @Test
    void trueLiteral() { assertEquals("true", JsonBoolean.TRUE.toJson()); }
    @Test
    void stringQuoted() { assertEquals("\"hi\"", new JsonString("hi").toJson()); }
    @Test
    void stringEscaped() { assertEquals("\"a\\\"b\"", new JsonString("a\"b").toJson()); }
    @Test
    void newlineEscaped() { assertEquals("\"a\\nb\"", new JsonString("a\nb").toJson()); }
    @Test
    void numberPreserved() { assertEquals("3.14", new JsonNumber("3.14").toJson()); }
    @Test
    void numberEquals() { assertEquals(new JsonNumber("42"), new JsonNumber("42")); }

    // ---------------------------------------------------------- parse
    @Test
    void parseObject() {
        JsonObject o = Json.parseObject("{ \"a\": 1, \"b\": \"x\" }");
        assertEquals(1, o.getInt("a"));
        assertEquals("x", o.getString("b"));
    }

    @Test
    void parseArray() {
        JsonArray a = Json.parseArray("[1, 2, 3]");
        assertEquals(3, a.size());
        assertEquals(2, a.getInt(1));
    }

    @Test
    void parseNested() {
        JsonObject o = Json.parseObject("{\"arr\":[true,false],\"obj\":{\"k\":null}}");
        assertTrue(o.getArray("arr").getBoolean(0));
        assertTrue(o.getObject("obj").get("k").isNull());
    }

    @Test
    void parseUnicode() {
        JsonString s = (JsonString) Json.parse("\"\\u00e9\"");
        assertEquals("é", s.value());
    }

    @Test
    void parseInvalidThrows() {
        assertThrows(JsonException.class, () -> Json.parse("{"));
        assertThrows(JsonException.class, () -> Json.parse(""));
        assertThrows(JsonException.class, () -> Json.parse("{} trailing"));
        assertThrows(JsonException.class, () -> Json.parse("{\"a\" 1}"));
    }

    // ---------------------------------------------------------- build
    @Test
    void buildObject() {
        JsonObject o = Json.object()
                .put("name", "Saad")
                .put("age", 20)
                .put("active", true);
        assertEquals("Saad", o.getString("name"));
        assertEquals(20, o.getInt("age"));
        assertTrue(o.getBoolean("active"));
    }

    @Test
    void buildArray() {
        JsonArray a = Json.array().add("x").add(1).add(true).addNull();
        assertEquals("x", a.getString(0));
        assertEquals(1, a.getInt(1));
        assertTrue(a.getBoolean(2));
        assertTrue(a.get(3).isNull());
    }

    @Test
    void objectFluentChaining() {
        JsonObject o = Json.object()
                .put("a", 1)
                .put("b", 2)
                .remove("a")
                .put("c", 3);
        assertFalse(o.has("a"));
        assertEquals(3, o.getInt("c"));
    }

    // ---------------------------------------------------------- write
    @Test
    void compactOutput() {
        String text = Json.write(Json.object().put("a", 1).put("b", 2));
        assertFalse(text.contains("\n"));
    }

    @Test
    void prettyOutput() {
        String text = Json.pretty(Json.object().put("a", 1));
        assertTrue(text.contains("\n"));
        assertTrue(text.contains("  "));
    }

    // ---------------------------------------------------------- round-trip
    @Test
    void roundTrip() {
        JsonObject original = Json.object()
                .put("name", "Saad")
                .put("arr", Json.array().add(1).add(2).add("three"));
        String text = Json.pretty(original);
        JsonObject parsed = Json.parseObject(text);
        assertEquals(original, parsed);
    }

    // ---------------------------------------------------------- validity
    @Test
    void validTrue() { assertTrue(Json.isValid("{\"a\":1}")); }
    @Test
    void validFalse() { assertFalse(Json.isValid("nope")); }

    // ---------------------------------------------------------- type safety
    @Test
    void wrongTypeThrows() {
        JsonObject o = Json.object().put("a", "str");
        assertThrows(JsonException.class, () -> o.getInt("a"));
    }

    // ---------------------------------------------------------- map/list builders
    @Test
    void fromJavaMap() {
        JsonObject o = JsonObject.from(java.util.Map.of("k", 1));
        assertEquals(1, o.getInt("k"));
    }

    @Test
    void fromJavaList() {
        JsonArray a = JsonArray.from(java.util.List.of(1, 2, 3));
        assertEquals(3, a.size());
    }
}
