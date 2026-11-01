package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextFileTest {

    @TempDir
    Path dir;

    @Test
    void writeAndRead() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        assertTrue(f.write("hello"));
        assertEquals("hello", f.read());
    }

    @Test
    void writeLines() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        assertTrue(f.writeLines(List.of("a", "b", "c")));
        assertEquals(3, f.readLines().size());
    }

    @Test
    void append() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        f.write("one\n");
        f.append("two\n");
        f.appendLine("three");
        assertTrue(f.read().contains("three"));
    }

    @Test
    void headTail() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        f.writeLines(List.of("1", "2", "3", "4", "5"));
        assertEquals(List.of("1", "2"), f.head(2));
        assertEquals(List.of("4", "5"), f.tail(2));
    }

    @Test
    void stats() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        f.write("one two\nthree four\n");
        assertEquals(2, f.countLines());
        assertEquals(4, f.countWords());
    }

    @Test
    void grep() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        f.writeLines(List.of("apple", "banana", "apple pie"));
        assertEquals(2, f.grep("apple").size());
    }

    @Test
    void pathInfo() {
        String p = dir.resolve("myfile.txt").toString();
        TextFile f = TextFile.of(p);
        assertEquals("myfile.txt", f.name());
        assertEquals("myfile", f.baseName());
        assertEquals("txt", f.extension());
    }

    @Test
    void existsAndDelete() {
        String p = dir.resolve("f.txt").toString();
        TextFile f = TextFile.of(p);
        assertFalse(f.exists());
        f.write("x");
        assertTrue(f.exists());
        assertTrue(f.delete());
        assertFalse(f.exists());
    }

    @Test
    void chainFactories() {
        assertNotNull(TextFile.open("a"));
        assertNotNull(TextFile.at("a"));
        assertNotNull(TextFile.on("a"));
    }

    @Test
    void nullPathIsSafe() {
        TextFile f = TextFile.of(null);
        assertEquals("", f.path());
        assertFalse(f.exists());
    }
}
