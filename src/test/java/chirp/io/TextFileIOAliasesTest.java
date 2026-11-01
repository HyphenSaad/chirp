package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextFileIOAliasesTest {

    @TempDir
    Path dir;

    private String file(String name, String content) throws Exception {
        Path p = dir.resolve(name);
        java.nio.file.Files.writeString(p, content);
        return p.toString();
    }

    // ============================================================== read aliases

    @Test
    void readAllAliases() throws Exception {
        String p = file("f.txt", "hello");
        assertEquals("hello", TextFileIO.readAll(p));
        assertEquals("hello", TextFileIO.read(p));
        assertEquals("hello", TextFileIO.readText(p));
        assertEquals("hello", TextFileIO.load(p));
        assertEquals("hello", TextFileIO.loadText(p));
    }

    @Test
    void readLinesAliases() throws Exception {
        String p = file("f.txt", "a\nb\nc");
        assertEquals(3, TextFileIO.readLines(p).size());
        assertEquals(3, TextFileIO.lines(p).size());
        assertEquals(3, TextFileIO.getLines(p).size());
        assertEquals(3, TextFileIO.asLines(p).size());
    }

    // ============================================================== write aliases

    @Test
    void writeAliases() {
        String p = dir.resolve("out.txt").toString();
        assertTrue(TextFileIO.write(p, "x"));
        assertTrue(TextFileIO.save(p, "y"));
        assertTrue(TextFileIO.saveAll(p, "z"));
        assertTrue(TextFileIO.writeText(p, "w"));
        assertEquals("w", TextFileIO.readAll(p));
    }

    @Test
    void appendAliases() {
        String p = dir.resolve("app.txt").toString();
        TextFileIO.write(p, "one\n");
        assertTrue(TextFileIO.append(p, "two\n"));
        assertTrue(TextFileIO.add(p, "three\n"));
        assertTrue(TextFileIO.addText(p, "four\n"));
        assertTrue(TextFileIO.readAll(p).contains("four"));
    }

    // ============================================================== head/tail aliases

    @Test
    void headTailAliases() throws Exception {
        String p = file("h.txt", "1\n2\n3\n4\n5");
        assertEquals(List.of("1", "2"), TextFileIO.head(p, 2));
        assertEquals(List.of("1", "2"), TextFileIO.firstN(p, 2));
        assertEquals(List.of("4", "5"), TextFileIO.tail(p, 2));
        assertEquals(List.of("4", "5"), TextFileIO.lastN(p, 2));
    }

    // ============================================================== grep aliases

    @Test
    void grepAliases() throws Exception {
        String p = file("g.txt", "apple\nbanana\napple pie\n");
        assertEquals(2, TextFileIO.grep(p, "apple").size());
        assertEquals(2, TextFileIO.search(p, "apple").size());
        assertEquals(2, TextFileIO.findInFile(p, "apple").size());
        assertEquals(2, TextFileIO.matchingLines(p, "apple").size());
    }

    // ============================================================== new methods

    @Test
    void lineByIndex() throws Exception {
        String p = file("i.txt", "a\nb\nc");
        assertEquals("a", TextFileIO.line(p, 0));
        assertEquals("b", TextFileIO.lineAt(p, 1));
        assertEquals("c", TextFileIO.getLine(p, 2));
        assertEquals("", TextFileIO.line(p, 99));
    }

    @Test
    void slice() throws Exception {
        String p = file("s.txt", "1\n2\n3\n4\n5");
        assertEquals(List.of("2", "3"), TextFileIO.slice(p, 1, 3));
        assertEquals(List.of("2", "3"), TextFileIO.linesBetween(p, 1, 3));
    }

    @Test
    void countAliases() throws Exception {
        String p = file("c.txt", "one two three\nfour five\n");
        assertEquals(2, TextFileIO.countLines(p));
        assertEquals(2, TextFileIO.lineCount(p));
        assertEquals(2, TextFileIO.numLines(p));
        assertEquals(5, TextFileIO.countWords(p));
        assertEquals(5, TextFileIO.wordCount(p));
        assertEquals(TextFileIO.countChars(p), TextFileIO.charCount(p));
    }

    // ============================================================== path helpers

    @Test
    void pathHelpers() {
        assertEquals("txt", TextFileIO.extension("f.txt"));
        assertEquals("txt", TextFileIO.ext("f.txt"));
        assertEquals("txt", TextFileIO.suffix("f.txt"));
        assertEquals("f.txt", TextFileIO.fileName("path/to/f.txt"));
        assertEquals("f.txt", TextFileIO.name("path/to/f.txt"));
        assertEquals("f", TextFileIO.baseName("path/to/f.txt"));
        assertEquals("f", TextFileIO.stem("path/to/f.txt"));
        assertEquals("path/to", TextFileIO.directoryOf("path/to/f.txt"));
        assertEquals("path/to", TextFileIO.dirOf("path/to/f.txt"));
        assertEquals("path/to", TextFileIO.parentOf("path/to/f.txt"));
    }

    // ============================================================== humanSize

    @Test
    void humanSize() throws Exception {
        String p = file("big.txt", "x".repeat(2048));
        assertTrue(TextFileIO.humanSize(p).contains("KB"), TextFileIO.humanSize(p));
    }

    // ============================================================== integration bridges

    @Test
    void readJsonBridge() throws Exception {
        String p = file("data.json", "{\"a\":1}");
        var obj = TextFileIO.readJson(p);
        assertEquals(1, obj.getInt("a"));
    }

    @Test
    void writeJsonBridge() {
        String p = dir.resolve("out.json").toString();
        chirp.json.JsonObject obj = new chirp.json.JsonObject().put("x", 5);
        assertTrue(TextFileIO.writeJson(p, obj));
        assertTrue(TextFileIO.readAll(p).contains("\"x\""));
    }

    @Test
    void readCsvBridge() throws Exception {
        String p = file("data.csv", "a,b\n1,2\n");
        assertEquals(2, TextFileIO.readCsv(p).size());
    }

    @Test
    void writeCsvBridge() {
        String p = dir.resolve("out.csv").toString();
        assertTrue(TextFileIO.writeCsv(p, List.of(List.of("a", "b"))));
        assertTrue(TextFileIO.readAll(p).contains("a,b"));
    }

    // ============================================================== chain factory

    @Test
    void chainFactories() {
        assertNotNull(TextFileIO.of("a.txt"));
        assertNotNull(TextFileIO.open("a.txt"));
        assertNotNull(TextFileIO.at("a.txt"));
        assertNotNull(TextFileIO.on("a.txt"));
    }
}
