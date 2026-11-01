package chirp.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderTest {

    @TempDir
    Path dir;

    private String write(String content) throws Exception {
        Path f = dir.resolve(".env");
        Files.writeString(f, content);
        return f.toString();
    }

    @Test
    void parsesSimpleKeyValue() throws Exception {
        Map<String, String> env = EnvLoader.load(write("KEY=value\nOTHER=thing\n"));
        assertEquals("value", env.get("KEY"));
        assertEquals("thing", env.get("OTHER"));
    }

    @Test
    void ignoresCommentsAndBlankLines() throws Exception {
        Map<String, String> env = EnvLoader.load(write(
                "# comment\n\nKEY=value\n  \n# another\n"));
        assertEquals(1, env.size());
        assertEquals("value", env.get("KEY"));
    }

    @Test
    void doubleQuotedValueUnescapes() throws Exception {
        Map<String, String> env = EnvLoader.load(write("K=\"a\\nb\"\n"));
        assertEquals("a\nb", env.get("K"));
    }

    @Test
    void singleQuotedValueIsLiteral() throws Exception {
        Map<String, String> env = EnvLoader.load(write("K='a\\nb'\n"));
        assertEquals("a\\nb", env.get("K"));
    }

    @Test
    void whitespaceInUnquotedValuesPreserved() throws Exception {
        Map<String, String> env = EnvLoader.load(write("K=a b c\n"));
        assertEquals("a b c", env.get("K"));
    }

    @Test
    void getReturnsRequiredKey() throws Exception {
        String path = write("SMTP_PASSWORD=secret\n");
        assertEquals("secret", EnvLoader.get(path, "SMTP_PASSWORD"));
    }

    @Test
    void getThrowsOnMissingKey() throws Exception {
        String path = write("A=1\n");
        assertThrows(EmailException.class, () -> EnvLoader.get(path, "B"));
    }

    @Test
    void getThrowsOnEmptyValue() throws Exception {
        String path = write("K=\n");
        assertThrows(EmailException.class, () -> EnvLoader.get(path, "K"));
    }

    @Test
    void loadThrowsOnMissingFile() {
        assertThrows(EmailException.class,
                () -> EnvLoader.load(dir.resolve("nope.env").toString()));
    }

    @Test
    void loadThrowsOnInvalidLine() throws Exception {
        String path = write("just-a-line-without-equals\n");
        assertThrows(EmailException.class, () -> EnvLoader.load(path));
    }

    @Test
    void loadThrowsOnInvalidKey() throws Exception {
        String path = write("123BAD=value\n");
        assertThrows(EmailException.class, () -> EnvLoader.load(path));
    }

    @Test
    void loadThrowsOnUnterminatedQuote() throws Exception {
        String path = write("K=\"unterminated\n");
        assertThrows(EmailException.class, () -> EnvLoader.load(path));
    }
}
