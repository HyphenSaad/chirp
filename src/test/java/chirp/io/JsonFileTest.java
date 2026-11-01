package chirp.io;

import chirp.json.JsonObject;
import chirp.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileTest {

    @TempDir
    Path dir;

    @Test
    void writePrettyAndRead() {
        String p = dir.resolve("c.json").toString();
        JsonObject o = new JsonObject().put("name", "Saad").put("age", 20);
        assertTrue(JsonFile.writePretty(p, o));
        JsonObject back = JsonFile.readObject(p);
        assertEquals(o, back);
    }

    @Test
    void writeCompact() {
        String p = dir.resolve("c.json").toString();
        JsonFile.writeCompact(p, new JsonObject().put("a", 1));
        String text = TextFileIO.readAll(p);
        assertFalse(text.contains("\n"));
    }

    @Test
    void readMissingReturnsEmptyObject() {
        JsonObject o = JsonFile.readObject(dir.resolve("no.json").toString());
        assertNotNull(o);
        assertTrue(o.isEmpty());
    }

    @Test
    void readMissingArrayReturnsEmptyArray() {
        assertTrue(JsonFile.readArray(dir.resolve("no.json").toString()).isEmpty());
    }

    @Test
    void readMalformedReturnsEmpty() {
        String p = dir.resolve("bad.json").toString();
        TextFileIO.write(p, "{ not valid }");
        assertTrue(JsonFile.readObject(p).isEmpty());
    }

    @Test
    void readObjectFromArrayReturnsEmpty() {
        String p = dir.resolve("arr.json").toString();
        TextFileIO.write(p, "[1,2,3]");
        assertTrue(JsonFile.readObject(p).isEmpty());
    }

    @Test
    void readArrayFromObjectReturnsEmpty() {
        String p = dir.resolve("obj.json").toString();
        TextFileIO.write(p, "{\"a\":1}");
        assertTrue(JsonFile.readArray(p).isEmpty());
    }

    @Test
    void readValueReturnsNullOnFailure() {
        JsonValue value = JsonFile.read(dir.resolve("absent.json").toString());
        assertNull(value);
    }

    @Test
    void unicodeRoundTrip() {
        String p = dir.resolve("u.json").toString();
        JsonObject o = new JsonObject().put("greeting", "مرحبا");
        assertTrue(JsonFile.writePretty(p, o));
        assertEquals("مرحبا", JsonFile.readObject(p).getString("greeting"));
    }
}
