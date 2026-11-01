package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileIOTest {

    @TempDir
    Path tempDir;

    @Test
    void writeAndRead() {
        String file = tempDir.resolve("test.txt").toString();
        assertTrue(FileIO.write(file, "hello"));
        assertEquals("hello", FileIO.readAll(file));
    }

    @Test
    void readMissingFileReturnsEmpty() {
        assertEquals("", FileIO.readAll(tempDir.resolve("nope.txt").toString()));
    }

    @Test
    void append() {
        String file = tempDir.resolve("a.txt").toString();
        FileIO.write(file, "line1\n");
        FileIO.append(file, "line2\n");
        assertTrue(FileIO.readAll(file).contains("line2"));
    }

    @Test
    void existsAndDelete() {
        String file = tempDir.resolve("d.txt").toString();
        assertFalse(FileIO.exists(file));
        FileIO.write(file, "x");
        assertTrue(FileIO.exists(file));
        assertTrue(FileIO.delete(file));
        assertFalse(FileIO.exists(file));
    }

    @Test
    void countLines() {
        String file = tempDir.resolve("wc.txt").toString();
        FileIO.write(file, "a\nb\nc\n");
        assertEquals(3, FileIO.countLines(file));
    }

    @Test
    void readLinesFromMissingReturnsEmptyList() {
        assertTrue(FileIO.readLines(tempDir.resolve("nope.txt").toString()).isEmpty());
    }

    @Test
    void copyRoundTrip() {
        String src = tempDir.resolve("src.txt").toString();
        String dst = tempDir.resolve("dst.txt").toString();
        FileIO.write(src, "data");
        assertTrue(FileIO.copy(src, dst));
        assertEquals("data", FileIO.readAll(dst));
    }
}
