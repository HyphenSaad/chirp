package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileIOAliasesTest {

    @TempDir
    Path dir;

    // ============================================================== file read/write aliases

    @Test
    void readWriteAliases() {
        String p = dir.resolve("f.txt").toString();
        assertTrue(FileIO.write(p, "hello"));
        assertTrue(FileIO.save(p, "hello2"));
        assertEquals("hello2", FileIO.readAll(p));
        assertEquals("hello2", FileIO.read(p));
    }

    @Test
    void readLinesAliases() throws Exception {
        Path p = dir.resolve("f.txt");
        Files.write(p, List.of("a", "b", "c"));
        assertEquals(3, FileIO.readLines(p.toString()).size());
        assertEquals(3, FileIO.lines(p.toString()).size());
    }

    @Test
    void appendAlias() {
        String p = dir.resolve("f.txt").toString();
        FileIO.write(p, "one\n");
        assertTrue(FileIO.append(p, "two\n"));
        assertTrue(FileIO.addContent(p, "three\n"));
        assertTrue(FileIO.readAll(p).contains("three"));
    }

    // ============================================================== directory aliases

    @Test
    void createDirectoryAliases() {
        String p = dir.resolve("sub").toString();
        assertTrue(FileIO.createDirectory(p));
        assertTrue(FileIO.createDirectories(p));
        assertTrue(FileIO.mkdir(p));
        assertTrue(FileIO.mkdirs(p));
        assertTrue(FileIO.ensureDirectory(p));
        assertTrue(FileIO.ensureDir(p));
    }

    @Test
    void deleteDirectoryAliases() throws Exception {
        Path p = dir.resolve("sub");
        Files.createDirectories(p);
        assertTrue(FileIO.deleteDirectory(p.toString()));
        Files.createDirectories(p);
        assertTrue(FileIO.removeDirectory(p.toString()));
        Files.createDirectories(p);
        assertTrue(FileIO.rmDir(p.toString()));
        assertFalse(Files.exists(p));
    }

    @Test
    void countAliases() throws Exception {
        Files.writeString(dir.resolve("a.txt"), "x");
        Files.writeString(dir.resolve("b.txt"), "y");
        assertEquals(2, FileIO.countFiles(dir.toString()));
        assertEquals(2, FileIO.countFilesIn(dir.toString()));
        assertEquals(2, FileIO.countAllFiles(dir.toString()));
        assertEquals(2, FileIO.countFilesRecursive(dir.toString()));
    }

    @Test
    void listAliases() throws Exception {
        Files.createDirectories(dir.resolve("sub1"));
        Files.createDirectories(dir.resolve("sub2"));
        assertEquals(2, FileIO.listDirectories(dir.toString()).size());
        assertEquals(2, FileIO.listDirs(dir.toString()).size());
    }

    // ============================================================== size aliases

    @Test
    void sizeAliases() throws Exception {
        Files.writeString(dir.resolve("a.txt"), "hello");
        assertEquals(5, FileIO.sizeInBytes(dir.resolve("a.txt").toString()));
        assertEquals(5, FileIO.size(dir.resolve("a.txt").toString()));
        assertEquals(5, FileIO.directorySize(dir.toString()));
        assertEquals(5, FileIO.dirSize(dir.toString()));
    }

    // ============================================================== environment

    @Test
    void workingDir() {
        assertNotNull(FileIO.workingDir());
        assertEquals(FileIO.workingDir(), FileIO.cwd());
    }

    @Test
    void homeDir() {
        assertNotNull(FileIO.homeDir());
        assertEquals(FileIO.homeDir(), FileIO.home());
    }

    @Test
    void tempDir() {
        assertNotNull(FileIO.tempDir());
    }

    @Test
    void tempFile() {
        String p = FileIO.tempFile();
        assertNotNull(p);
        assertFalse(p.isEmpty());
        Files.exists(Path.of(p));
        try {
            Files.deleteIfExists(Path.of(p));
        } catch (Exception ignored) {}
    }

    @Test
    void tempFileWithPrefixSuffix() {
        String p = FileIO.tempFile("test", ".log");
        assertNotNull(p);
        assertTrue(p.endsWith(".log"));
        try {
            Files.deleteIfExists(Path.of(p));
        } catch (Exception ignored) {}
    }

    @Test
    void tempDirectory() {
        String p = FileIO.tempDirectory();
        assertNotNull(p);
        assertTrue(Files.isDirectory(Path.of(p)));
        try {
            Files.deleteIfExists(Path.of(p));
        } catch (Exception ignored) {}
    }

    // ============================================================== touch

    @Test
    void touchCreatesFile() {
        String p = dir.resolve("new.txt").toString();
        assertFalse(Files.exists(Path.of(p)));
        assertTrue(FileIO.touch(p));
        assertTrue(Files.exists(Path.of(p)));
    }

    @Test
    void touchExistingFileIsNoOp() {
        String p = dir.resolve("existing.txt").toString();
        FileIO.write(p, "content");
        assertTrue(FileIO.touch(p));
        assertEquals("content", FileIO.readAll(p));
    }

    // ============================================================== rename aliases

    @Test
    void renameAliases() {
        String src = dir.resolve("old.txt").toString();
        String dst = dir.resolve("new.txt").toString();
        FileIO.write(src, "x");
        assertTrue(FileIO.rename(src, dst));
        assertFalse(Files.exists(Path.of(src)));
        assertTrue(Files.exists(Path.of(dst)));
    }

    @Test
    void renameFileAlias() {
        String src = dir.resolve("a.txt").toString();
        String dst = dir.resolve("b.txt").toString();
        FileIO.write(src, "x");
        assertTrue(FileIO.renameFile(src, dst));
        assertTrue(Files.exists(Path.of(dst)));
    }

    // ============================================================== copy/move aliases

    @Test
    void copyDirectoryAlias() throws Exception {
        Path src = dir.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("a.txt"), "hello");
        Path dst = dir.resolve("dst");
        assertTrue(FileIO.copyDir(src.toString(), dst.toString()));
        assertEquals("hello", Files.readString(dst.resolve("a.txt")));
    }

    @Test
    void moveDirectoryAlias() throws Exception {
        Path src = dir.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("a.txt"), "hello");
        Path dst = dir.resolve("dst");
        assertTrue(FileIO.moveDir(src.toString(), dst.toString()));
        assertFalse(Files.exists(src));
        assertTrue(Files.exists(dst.resolve("a.txt")));
    }

    // ============================================================== path helper aliases

    @Test
    void pathHelpers() {
        assertEquals("txt", FileIO.extension("a.txt"));
        assertEquals("txt", FileIO.ext("a.txt"));
        assertEquals("txt", FileIO.suffix("a.txt"));
        assertEquals("a.txt", FileIO.fileName("path/a.txt"));
        assertEquals("a.txt", FileIO.name("path/a.txt"));
        assertEquals("a", FileIO.baseName("path/a.txt"));
        assertEquals("a", FileIO.stem("path/a.txt"));
        assertEquals("path", FileIO.directoryOf("path/a.txt"));
        assertEquals("path", FileIO.dirOf("path/a.txt"));
        assertEquals("path", FileIO.parentOf("path/a.txt"));
    }

    // ============================================================== isEmpty

    @Test
    void isEmptyAliases() {
        assertTrue(FileIO.isEmpty(dir.toString()));
        assertTrue(FileIO.isEmptyDir(dir.toString()));
    }

    // ============================================================== isDir

    @Test
    void isDirAlias() {
        assertTrue(FileIO.isDir(dir.toString()));
        assertFalse(FileIO.isDir(dir.resolve("not-a-dir").toString()));
    }
}
