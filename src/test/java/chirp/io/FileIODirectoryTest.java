package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileIODirectoryTest {

    @TempDir
    Path dir;

    // ---------------------------------------------------- creation

    @Test
    void createDirectory() {
        String path = dir.resolve("sub").toString();
        assertTrue(FileIO.createDirectory(path));
        assertTrue(Files.isDirectory(Path.of(path)));
    }

    @Test
    void createNestedDirectories() {
        String path = dir.resolve("a/b/c").toString();
        assertTrue(FileIO.createDirectory(path));
        assertTrue(Files.isDirectory(Path.of(path)));
    }

    @Test
    void createExistingIsIdempotent() {
        String path = dir.resolve("sub").toString();
        FileIO.createDirectory(path);
        assertTrue(FileIO.createDirectory(path));
    }

    @Test
    void mkdirAliasesWork() {
        String path = dir.resolve("alias").toString();
        assertTrue(FileIO.mkdir(path));
        assertTrue(FileIO.mkdirs(path));
        assertTrue(FileIO.ensureDirectory(path));
    }

    // ---------------------------------------------------- counting

    @Test
    void countFiles() throws Exception {
        Files.writeString(dir.resolve("a.txt"), "x");
        Files.writeString(dir.resolve("b.txt"), "y");
        Files.writeString(dir.resolve("c.txt"), "z");
        FileIO.createDirectory(dir.resolve("sub").toString());
        Files.writeString(dir.resolve("sub/inner.txt"), "w");

        assertEquals(3, FileIO.countFiles(dir.toString()));
        assertEquals(4, FileIO.countAllFiles(dir.toString()));
    }

    @Test
    void countEmptyDirectory() {
        assertEquals(0, FileIO.countFiles(dir.toString()));
        assertEquals(0, FileIO.countAllFiles(dir.toString()));
    }

    @Test
    void countMissingDirectoryReturnsZero() {
        assertEquals(0, FileIO.countFiles(dir.resolve("nope").toString()));
        assertEquals(0, FileIO.countAllFiles(dir.resolve("nope").toString()));
    }

    // ---------------------------------------------------- size

    @Test
    void directorySizeSumsAllFiles() throws Exception {
        Files.writeString(dir.resolve("a.txt"), "12345");           // 5 bytes
        Files.writeString(dir.resolve("b.txt"), "12");              // 2 bytes
        FileIO.createDirectory(dir.resolve("sub").toString());
        Files.writeString(dir.resolve("sub/c.txt"), "123");         // 3 bytes
        assertEquals(10L, FileIO.directorySize(dir.toString()));
    }

    @Test
    void directorySizeEmptyIsZero() {
        assertEquals(0L, FileIO.directorySize(dir.toString()));
    }

    @Test
    void directorySizeMissingIsZero() {
        assertEquals(0L, FileIO.directorySize(dir.resolve("nope").toString()));
    }

    // ---------------------------------------------------- listing

    @Test
    void listDirectoriesReturnsSubdirsOnly() throws Exception {
        FileIO.createDirectory(dir.resolve("one").toString());
        FileIO.createDirectory(dir.resolve("two").toString());
        Files.writeString(dir.resolve("file.txt"), "x");

        List<String> subdirs = FileIO.listDirectories(dir.toString());
        assertEquals(2, subdirs.size());
        assertTrue(subdirs.contains("one"));
        assertTrue(subdirs.contains("two"));
        assertFalse(subdirs.contains("file.txt"));
    }

    @Test
    void isEmptyTrueForEmptyDir() {
        assertTrue(FileIO.isEmpty(dir.toString()));
    }

    @Test
    void isEmptyFalseWhenFilesPresent() throws Exception {
        Files.writeString(dir.resolve("x.txt"), "x");
        assertFalse(FileIO.isEmpty(dir.toString()));
    }

    @Test
    void isEmptyFalseForMissingDir() {
        assertFalse(FileIO.isEmpty(dir.resolve("nope").toString()));
    }

    // ---------------------------------------------------- find by glob

    @Test
    void findFilesByGlob() throws Exception {
        Files.writeString(dir.resolve("a.txt"), "x");
        Files.writeString(dir.resolve("b.txt"), "y");
        Files.writeString(dir.resolve("c.pdf"), "z");

        List<String> txt = FileIO.findFiles(dir.toString(), "*.txt");
        assertEquals(2, txt.size());

        List<String> all = FileIO.findFiles(dir.toString(), "*");
        assertEquals(3, all.size());
    }

    @Test
    void findFilesByExtension() throws Exception {
        Files.writeString(dir.resolve("a.pdf"), "x");
        Files.writeString(dir.resolve("b.PDF"), "y");
        Files.writeString(dir.resolve("c.txt"), "z");

        List<String> pdf = FileIO.findFilesByExtension(dir.toString(), "pdf");
        assertEquals(2, pdf.size());

        assertEquals(2, FileIO.findFilesByExtension(dir.toString(), ".pdf").size());
    }

    @Test
    void findFilesNullGlobReturnsEmpty() {
        assertTrue(FileIO.findFiles(dir.toString(), null).isEmpty());
    }

    @Test
    void findFilesEmptyExtensionReturnsEmpty() {
        assertTrue(FileIO.findFilesByExtension(dir.toString(), "").isEmpty());
        assertTrue(FileIO.findFilesByExtension(dir.toString(), null).isEmpty());
    }

    // ---------------------------------------------------- copy directory

    @Test
    void copyDirectoryRecursive() throws Exception {
        Path src = dir.resolve("src");
        Files.createDirectories(src.resolve("nested"));
        Files.writeString(src.resolve("a.txt"), "hello");
        Files.writeString(src.resolve("nested/b.txt"), "world");

        Path dst = dir.resolve("dst");
        assertTrue(FileIO.copyDirectory(src.toString(), dst.toString()));

        assertEquals("hello", Files.readString(dst.resolve("a.txt")));
        assertEquals("world", Files.readString(dst.resolve("nested/b.txt")));
    }

    @Test
    void copyDirectoryRejectsNonDirectory() throws Exception {
        Path file = dir.resolve("f.txt");
        Files.writeString(file, "x");
        assertFalse(FileIO.copyDirectory(file.toString(), dir.resolve("out").toString()));
    }

    @Test
    void copyDirectoryMissingSourceFails() {
        assertFalse(FileIO.copyDirectory(
                dir.resolve("nope").toString(),
                dir.resolve("dst").toString()));
    }

    // ---------------------------------------------------- move directory

    @Test
    void moveDirectoryRelocatesTree() throws Exception {
        Path src = dir.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("a.txt"), "hello");

        Path dst = dir.resolve("dst");
        assertTrue(FileIO.moveDirectory(src.toString(), dst.toString()));

        assertFalse(Files.exists(src));
        assertEquals("hello", Files.readString(dst.resolve("a.txt")));
    }

    // ---------------------------------------------------- rename

    @Test
    void renameFile() throws Exception {
        Path file = dir.resolve("old.txt");
        Files.writeString(file, "x");
        Path newPath = dir.resolve("new.txt");

        assertTrue(FileIO.rename(file.toString(), newPath.toString()));
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(newPath));
    }

    @Test
    void renameMissingFileFails() {
        assertFalse(FileIO.rename(
                dir.resolve("nope.txt").toString(),
                dir.resolve("x.txt").toString()));
    }

    // ---------------------------------------------------- delete directory

    @Test
    void deleteDirectoryRemovesEverything() throws Exception {
        Path src = dir.resolve("tree");
        Files.createDirectories(src.resolve("nested"));
        Files.writeString(src.resolve("a.txt"), "x");
        Files.writeString(src.resolve("nested/b.txt"), "y");

        assertTrue(FileIO.deleteDirectory(src.toString()));
        assertFalse(Files.exists(src));
    }

    @Test
    void deleteDirectoryIsIdempotent() {
        assertTrue(FileIO.deleteDirectory(dir.resolve("nope").toString()));
    }

    @Test
    void deleteDirectoryRejectsFile() throws Exception {
        Path file = dir.resolve("f.txt");
        Files.writeString(file, "x");
        assertFalse(FileIO.deleteDirectory(file.toString()));
        assertTrue(Files.exists(file));
    }

    @Test
    void removeDirectoryAlias() throws Exception {
        Path src = dir.resolve("tree");
        Files.createDirectories(src);
        assertTrue(FileIO.removeDirectory(src.toString()));
        assertFalse(Files.exists(src));
    }

    // ---------------------------------------------------- isFile / isDirectory

    @Test
    void isFileAndIsDirectory() throws Exception {
        Path file = dir.resolve("f.txt");
        Files.writeString(file, "x");
        Path subdir = dir.resolve("sub");
        Files.createDirectories(subdir);

        assertTrue(FileIO.isFile(file.toString()));
        assertFalse(FileIO.isDirectory(file.toString()));
        assertTrue(FileIO.isDirectory(subdir.toString()));
        assertFalse(FileIO.isFile(subdir.toString()));
    }

    @Test
    void isFileFalseForMissing() {
        assertFalse(FileIO.isFile(dir.resolve("nope").toString()));
        assertFalse(FileIO.isDirectory(dir.resolve("nope").toString()));
    }
}
