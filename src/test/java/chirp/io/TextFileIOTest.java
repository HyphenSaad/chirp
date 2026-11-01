package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextFileIOTest {

    @TempDir
    Path dir;

    @Test
    void writeAndRead() {
        String p = dir.resolve("f.txt").toString();
        assertTrue(TextFileIO.write(p, "hello"));
        assertEquals("hello", TextFileIO.readAll(p));
    }

    @Test
    void readMissingReturnsEmptyString() {
        assertEquals("", TextFileIO.readAll(dir.resolve("absent.txt").toString()));
    }

    @Test
    void readLinesMissingReturnsEmptyList() {
        assertTrue(TextFileIO.readLines(dir.resolve("absent.txt").toString()).isEmpty());
    }

    @Test
    void nullContentWritesEmptyString() {
        String p = dir.resolve("n.txt").toString();
        assertTrue(TextFileIO.write(p, null));
        assertEquals("", TextFileIO.readAll(p));
    }

    @Test
    void appendAddsContent() {
        String p = dir.resolve("a.txt").toString();
        TextFileIO.write(p, "one\n");
        TextFileIO.appendLine(p, "two");
        assertTrue(TextFileIO.readAll(p).contains("two"));
    }

    @Test
    void prependPutsTextFirst() {
        String p = dir.resolve("pr.txt").toString();
        TextFileIO.write(p, "world");
        TextFileIO.prepend(p, "hello ");
        assertEquals("hello world", TextFileIO.readAll(p));
    }

    @Test
    void headAndTail() {
        String p = dir.resolve("lines.txt").toString();
        TextFileIO.writeLines(p, List.of("a", "b", "c", "d", "e"));
        assertEquals(List.of("a", "b"), TextFileIO.head(p, 2));
        assertEquals(List.of("d", "e"), TextFileIO.tail(p, 2));
    }

    @Test
    void firstAndLastLine() {
        String p = dir.resolve("fl.txt").toString();
        TextFileIO.writeLines(p, List.of("first", "middle", "last"));
        assertEquals("first", TextFileIO.firstLine(p));
        assertEquals("last", TextFileIO.lastLine(p));
    }

    @Test
    void countLinesAndWords() {
        String p = dir.resolve("wc.txt").toString();
        TextFileIO.write(p, "one two\nthree four five\n");
        assertEquals(2, TextFileIO.countLines(p));
        assertEquals(5, TextFileIO.countWords(p));
    }

    @Test
    void grepAndLineNumbers() {
        String p = dir.resolve("g.txt").toString();
        TextFileIO.writeLines(p, List.of("alpha", "beta", "alphabet", "gamma"));
        assertEquals(List.of("alpha", "alphabet"), TextFileIO.grep(p, "alpha"));
        assertEquals(List.of(1, 3), TextFileIO.lineNumbersContaining(p, "alpha"));
    }

    @Test
    void replaceInFile() {
        String p = dir.resolve("r.txt").toString();
        TextFileIO.write(p, "hello world");
        TextFileIO.replaceInFile(p, "world", "Chirp");
        assertEquals("hello Chirp", TextFileIO.readAll(p));
    }

    @Test
    void copyAndMove() {
        String src = dir.resolve("src.txt").toString();
        String dst = dir.resolve("dst.txt").toString();
        TextFileIO.write(src, "data");
        assertTrue(TextFileIO.copy(src, dst));
        assertEquals("data", TextFileIO.readAll(dst));
    }

    @Test
    void existsAndDelete() {
        String p = dir.resolve("d.txt").toString();
        assertFalse(TextFileIO.exists(p));
        TextFileIO.write(p, "x");
        assertTrue(TextFileIO.exists(p));
        assertTrue(TextFileIO.delete(p));
        assertFalse(TextFileIO.exists(p));
    }

    @Test
    void extensionAndFileName() {
        assertEquals("txt", TextFileIO.extension("a.txt"));
        assertEquals("", TextFileIO.extension("no-ext"));
        assertEquals("a.txt", TextFileIO.fileName("path/to/a.txt"));
        assertEquals("a", TextFileIO.baseName("path/to/a.txt"));
        assertEquals("path/to", TextFileIO.directoryOf("path/to/a.txt"));
    }

    @Test
    void listFilesOnMissingDirReturnsEmpty() {
        assertTrue(TextFileIO.listFiles(dir.resolve("does-not-exist").toString()).isEmpty());
    }
}
