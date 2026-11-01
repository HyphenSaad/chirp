package chirp.io;

import chirp.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileEnhancedTest {

    @TempDir
    Path dir;

    // ============================================================== read aliases

    @Test
    void readAliases() throws Exception {
        Path p = dir.resolve("d.json");
        java.nio.file.Files.writeString(p, "{\"a\":1}");
        assertEquals(1, JsonFile.read(p.toString()).asObject().getInt("a"));
        assertEquals(1, JsonFile.load(p.toString()).asObject().getInt("a"));
        assertEquals(1, JsonFile.parseJson(p.toString()).asObject().getInt("a"));
    }

    @Test
    void readObjectAliases() throws Exception {
        Path p = dir.resolve("d.json");
        java.nio.file.Files.writeString(p, "{\"x\":5}");
        assertEquals(5, JsonFile.readObject(p.toString()).getInt("x"));
        assertEquals(5, JsonFile.loadObject(p.toString()).getInt("x"));
        assertEquals(5, JsonFile.asObject(p.toString()).getInt("x"));
        assertEquals(5, JsonFile.readObj(p.toString()).getInt("x"));
    }

    @Test
    void readArrayAliases() throws Exception {
        Path p = dir.resolve("a.json");
        java.nio.file.Files.writeString(p, "[1,2,3]");
        assertEquals(3, JsonFile.readArray(p.toString()).size());
        assertEquals(3, JsonFile.loadArray(p.toString()).size());
        assertEquals(3, JsonFile.asArray(p.toString()).size());
        assertEquals(3, JsonFile.readArr(p.toString()).size());
    }

    // ============================================================== write aliases

    @Test
    void writeAliases() {
        String p = dir.resolve("o.json").toString();
        JsonObject o = new JsonObject().put("k", 42);
        assertTrue(JsonFile.write(p, o));
        assertTrue(JsonFile.save(p, o));
        assertTrue(JsonFile.saveJSON(p, o));
        assertTrue(JsonFile.saveFile(p, o));
    }

    @Test
    void writeCompactAliases() {
        String p = dir.resolve("c.json").toString();
        JsonObject o = new JsonObject().put("k", 42);
        assertTrue(JsonFile.writeCompact(p, o));
        assertTrue(JsonFile.saveCompact(p, o));
        assertTrue(JsonFile.writeMinified(p, o));
        assertFalse(TextFileIO.readAll(p).contains("\n"));
    }

    @Test
    void writePrettyAliases() {
        String p = dir.resolve("p.json").toString();
        JsonObject o = new JsonObject().put("k", 42);
        assertTrue(JsonFile.writePretty(p, o));
        assertTrue(JsonFile.savePretty(p, o));
        assertTrue(JsonFile.writeFormatted(p, o));
        assertTrue(TextFileIO.readAll(p).contains("\n"));
    }

    // ============================================================== key-value

    @Test
    void writeKeyValuePretty() {
        String p = dir.resolve("kv.json").toString();
        JsonObject o = new JsonObject().put("a", 1);
        assertTrue(JsonFile.write(p, o, JsonFile.Key.PRETTY, true));
        assertTrue(TextFileIO.readAll(p).contains("\n"));
    }

    @Test
    void writeKeyValueNullsSafe() {
        String p = dir.resolve("kv.json").toString();
        JsonObject o = new JsonObject().put("a", 1);
        assertDoesNotThrow(() -> JsonFile.write(p, o, (Object[]) null));
    }

    // ============================================================== string returns

    @Test
    void toCompactString() {
        JsonObject o = new JsonObject().put("k", 42);
        String s = JsonFile.toCompactString(o);
        assertFalse(s.contains("\n"));
        assertTrue(s.contains("\"k\""));
    }

    @Test
    void toPrettyString() {
        JsonObject o = new JsonObject().put("k", 42);
        String s = JsonFile.toPrettyString(o);
        assertTrue(s.contains("\n"));
    }

    // ============================================================== validity

    @Test
    void isValid() throws Exception {
        Path good = dir.resolve("good.json");
        Path bad = dir.resolve("bad.json");
        java.nio.file.Files.writeString(good, "{\"a\":1}");
        java.nio.file.Files.writeString(bad, "{invalid");
        assertTrue(JsonFile.isValid(good.toString()));
        assertFalse(JsonFile.isValid(bad.toString()));
        assertFalse(JsonFile.isValid(dir.resolve("nope.json").toString()));
        assertTrue(JsonFile.validate(good.toString()));
    }

    // ============================================================== filesystem

    @Test
    void existsDelete() {
        String p = dir.resolve("d.json").toString();
        assertFalse(JsonFile.exists(p));
        JsonFile.write(p, new JsonObject().put("x", 1));
        assertTrue(JsonFile.exists(p));
        assertTrue(JsonFile.delete(p));
        assertFalse(JsonFile.exists(p));
    }

    // ============================================================== merge

    @Test
    void mergeTwoObjects() {
        String a = dir.resolve("a.json").toString();
        String b = dir.resolve("b.json").toString();
        JsonFile.write(a, new JsonObject().put("a", 1).put("b", 2));
        JsonFile.write(b, new JsonObject().put("b", 99).put("c", 3));
        JsonObject merged = JsonFile.merge(a, b);
        assertEquals(1, merged.getInt("a"));
        assertEquals(99, merged.getInt("b"));  // b overrides a
        assertEquals(3, merged.getInt("c"));
    }

    @Test
    void mergeAlias() {
        String a = dir.resolve("a.json").toString();
        String b = dir.resolve("b.json").toString();
        JsonFile.write(a, new JsonObject().put("x", 1));
        JsonFile.write(b, new JsonObject().put("y", 2));
        JsonObject merged = JsonFile.mergeObjects(a, b);
        assertEquals(1, merged.getInt("x"));
        assertEquals(2, merged.getInt("y"));
    }
}
