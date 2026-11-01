package chirp.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderEnhancedTest {

    @TempDir
    Path dir;

    private String write(String content) throws Exception {
        Path p = dir.resolve(".env");
        Files.writeString(p, content);
        return p.toString();
    }

    // ============================================================== load aliases

    @Test
    void loadAliases() throws Exception {
        String p = write("KEY=value\n");
        assertEquals("value", EnvLoader.load(p).get("KEY"));
        assertEquals("value", EnvLoader.read(p).get("KEY"));
        assertEquals("value", EnvLoader.parse(p).get("KEY"));
        assertEquals("value", EnvLoader.open(p).get("KEY"));
        assertEquals("value", EnvLoader.fromFile(p).get("KEY"));
    }

    // ============================================================== loadFromString

    @Test
    void loadFromString() {
        Map<String, String> env = EnvLoader.loadFromString("KEY=value\nOTHER=thing\n");
        assertEquals("value", env.get("KEY"));
        assertEquals("thing", env.get("OTHER"));
    }

    @Test
    void loadFromStringAlias() {
        Map<String, String> env = EnvLoader.fromString("A=1\n");
        assertEquals("1", env.get("A"));
    }

    @Test
    void loadFromStringNullReturnsEmpty() {
        assertTrue(EnvLoader.loadFromString(null).isEmpty());
    }

    // ============================================================== get aliases

    @Test
    void getAliases() throws Exception {
        String p = write("SMTP_PASSWORD=secret\n");
        assertEquals("secret", EnvLoader.get(p, "SMTP_PASSWORD"));
        assertEquals("secret", EnvLoader.value(p, "SMTP_PASSWORD"));
        assertEquals("secret", EnvLoader.lookup(p, "SMTP_PASSWORD"));
    }

    // ============================================================== getOrDefault

    @Test
    void getOrDefaultReturnsValueWhenPresent() throws Exception {
        String p = write("KEY=value\n");
        assertEquals("value", EnvLoader.getOrDefault(p, "KEY", "fallback"));
    }

    @Test
    void getOrDefaultReturnsFallbackWhenMissing() throws Exception {
        String p = write("A=1\n");
        assertEquals("fallback", EnvLoader.getOrDefault(p, "B", "fallback"));
    }

    @Test
    void getOrDefaultReturnsFallbackOnMissingFile() {
        assertEquals("fallback",
                EnvLoader.getOrDefault(dir.resolve("missing.env").toString(), "KEY", "fallback"));
    }

    @Test
    void getOrDefaultAliases() throws Exception {
        String p = write("A=1\n");
        assertEquals("fb", EnvLoader.defaultValue(p, "B", "fb"));
        assertEquals("fb", EnvLoader.orElse(p, "B", "fb"));
    }

    // ============================================================== has aliases

    @Test
    void hasAliases() throws Exception {
        String p = write("KEY=value\nEMPTY=\n");
        assertTrue(EnvLoader.has(p, "KEY"));
        assertTrue(EnvLoader.contains(p, "KEY"));
        assertTrue(EnvLoader.exists(p, "KEY"));

        assertFalse(EnvLoader.has(p, "MISSING"));
        assertFalse(EnvLoader.has(p, "EMPTY"));   // empty value
    }

    @Test
    void hasNullsSafe() {
        assertFalse(EnvLoader.has(null, "KEY"));
        assertFalse(EnvLoader.has("path", null));
    }

    // ============================================================== all aliases

    @Test
    void allAliases() throws Exception {
        String p = write("A=1\nB=2\n");
        assertEquals(EnvLoader.all(p), EnvLoader.allEntries(p));
        assertEquals(EnvLoader.all(p), EnvLoader.map(p));
        assertEquals(EnvLoader.all(p), EnvLoader.asMap(p));
        assertEquals(2, EnvLoader.all(p).size());
    }

    // ============================================================== error reasons

    @Test
    void missingFileThrowsMissingConfig() {
        EmailException e = assertThrows(EmailException.class,
                () -> EnvLoader.load(dir.resolve("missing.env").toString()));
        assertTrue(e.isMissingConfig());
    }

    @Test
    void invalidLineThrowsValidation() throws Exception {
        String p = write("just-no-equals\n");
        EmailException e = assertThrows(EmailException.class, () -> EnvLoader.load(p));
        assertTrue(e.isValidationError());
    }

    @Test
    void missingKeyThrowsMissingConfig() throws Exception {
        String p = write("A=1\n");
        EmailException e = assertThrows(EmailException.class,
                () -> EnvLoader.get(p, "B"));
        assertTrue(e.isMissingConfig());
    }
}
