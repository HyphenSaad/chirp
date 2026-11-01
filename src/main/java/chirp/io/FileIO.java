package chirp.io;

import chirp.internal.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Safe file and directory operations.
 *
 * <h2>Naming</h2>
 * <p>{@code readAll / read}; {@code readLines / lines}; {@code write / save};
 * {@code append / addContent}; {@code createDirectory / createDirectories /
 * mkdir / mkdirs / ensureDirectory / ensureDir}; {@code deleteDirectory /
 * removeDirectory / rmDir}; {@code countFiles / countFilesIn};
 * {@code directorySize / dirSize}; {@code listDirectories / listDirs};
 * {@code copyDirectory / copyDir}; {@code moveDirectory / moveDir};
 * {@code rename / renameFile}.</p>
 *
 * <p>Every method returns a safe default on failure — never throws a checked
 * exception.</p>
 */
public final class FileIO {

    private FileIO() {}

    // ============================================================== file read

    public static String readAll(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot read " + path);
            return "";
        }
    }
    public static String read(String path) { return readAll(path); }

    public static List<String> readLines(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot read " + path);
            return new ArrayList<>();
        }
    }
    public static List<String> lines(String path) { return readLines(path); }

    // ============================================================== file write

    public static boolean write(String path, String content) {
        try {
            Files.writeString(Path.of(path), content);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot write " + path);
            return false;
        }
    }
    public static boolean save(String path, String content) { return write(path, content); }

    public static boolean append(String path, String content) {
        try {
            Files.writeString(Path.of(path), content,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot append to " + path);
            return false;
        }
    }
    public static boolean addContent(String path, String content) { return append(path, content); }

    // ============================================================== file state

    public static boolean exists(String path) { return Files.exists(Path.of(path)); }
    public static boolean existsFile(String path) { return Files.exists(Path.of(path)); }
    public static boolean isFile(String path) { return Files.isRegularFile(Path.of(path)); }
    public static boolean isDirectory(String path) { return Files.isDirectory(Path.of(path)); }
    public static boolean isDir(String path) { return isDirectory(path); }

    public static long sizeInBytes(String path) {
        try {
            return Files.size(Path.of(path));
        } catch (IOException e) {
            return -1;
        }
    }
    public static long size(String path) { return sizeInBytes(path); }

    public static boolean delete(String path) {
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot delete " + path);
            return false;
        }
    }
    public static boolean remove(String path) { return delete(path); }

    public static boolean copy(String source, String destination) {
        try {
            Files.copy(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy " + source + " to " + destination);
            return false;
        }
    }

    public static List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(Path.of(directory))) {
            stream.filter(Files::isRegularFile)
                    .forEach(p -> names.add(p.getFileName().toString()));
        } catch (IOException e) {
            Logger.error("Cannot list files in " + directory);
        }
        return names;
    }
    public static List<String> ls(String directory) { return listFiles(directory); }

    public static int countLines(String path) { return readLines(path).size(); }
    public static int lineCount(String path) { return countLines(path); }

    // ============================================================== directory ops

    public static boolean createDirectory(String path) {
        try {
            Files.createDirectories(Path.of(path));
            return true;
        } catch (IOException e) {
            Logger.error("Cannot create directory: " + path);
            return false;
        }
    }
    public static boolean createDirectories(String path) { return createDirectory(path); }
    public static boolean mkdir(String path) { return createDirectory(path); }
    public static boolean mkdirs(String path) { return createDirectory(path); }
    public static boolean ensureDirectory(String path) { return createDirectory(path); }
    public static boolean ensureDir(String path) { return createDirectory(path); }

    public static boolean deleteDirectory(String path) {
        Path dir = Path.of(path);
        if (!Files.exists(dir)) return true;
        if (!Files.isDirectory(dir)) {
            Logger.error("Not a directory: " + path);
            return false;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {}
            });
            return true;
        } catch (IOException e) {
            Logger.error("Cannot delete directory: " + path);
            return false;
        }
    }
    public static boolean removeDirectory(String path) { return deleteDirectory(path); }
    public static boolean rmDir(String path) { return deleteDirectory(path); }

    public static int countFiles(String directory) {
        try (var stream = Files.list(Path.of(directory))) {
            return (int) stream.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }
    public static int countFilesIn(String directory) { return countFiles(directory); }

    public static int countAllFiles(String directory) {
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            return (int) walk.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }
    public static int countFilesRecursive(String directory) { return countAllFiles(directory); }

    public static long directorySize(String directory) {
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            long total = 0L;
            for (Path p : walk.filter(Files::isRegularFile).toList()) {
                try {
                    total += Files.size(p);
                } catch (IOException ignored) {}
            }
            return total;
        } catch (IOException e) {
            return 0L;
        }
    }
    public static long dirSize(String directory) { return directorySize(directory); }

    public static List<String> listDirectories(String directory) {
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(Path.of(directory))) {
            stream.filter(Files::isDirectory)
                    .forEach(p -> names.add(p.getFileName().toString()));
        } catch (IOException e) {
            Logger.error("Cannot list " + directory);
        }
        return names;
    }
    public static List<String> listDirs(String directory) { return listDirectories(directory); }

    public static boolean isEmpty(String directory) {
        try (var stream = Files.list(Path.of(directory))) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
    public static boolean isEmptyDir(String directory) { return isEmpty(directory); }

    // ============================================================== search

    public static List<String> findFiles(String directory, String glob) {
        List<String> result = new ArrayList<>();
        if (glob == null) return result;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> matcher.matches(p.getFileName()))
                    .forEach(p -> result.add(p.toString()));
        } catch (IOException e) {
            Logger.error("Cannot walk " + directory);
        }
        return result;
    }

    public static List<String> findFilesByExtension(String directory, String extension) {
        if (extension == null || extension.isBlank()) return List.of();
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return findFiles(directory, "*." + ext);
    }
    public static List<String> findFilesOfType(String directory, String ext) {
        return findFilesByExtension(directory, ext);
    }

    // ============================================================== directory copy / move

    public static boolean copyDirectory(String source, String destination) {
        Path src = Path.of(source);
        Path dst = Path.of(destination);
        if (!Files.isDirectory(src)) {
            Logger.error("Source is not a directory: " + source);
            return false;
        }
        try (Stream<Path> walk = Files.walk(src)) {
            for (Path p : walk.toList()) {
                Path target = dst.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(target);
                else {
                    Files.createDirectories(target.getParent());
                    Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy directory " + source + " to " + destination);
            return false;
        }
    }
    public static boolean copyDir(String source, String destination) { return copyDirectory(source, destination); }

    public static boolean moveDirectory(String source, String destination) {
        boolean copied = copyDirectory(source, destination);
        if (!copied) return false;
        return deleteDirectory(source);
    }
    public static boolean moveDir(String source, String destination) { return moveDirectory(source, destination); }

    public static boolean rename(String source, String destination) {
        try {
            Files.move(Path.of(source), Path.of(destination),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot rename " + source + " → " + destination);
            return false;
        }
    }
    public static boolean renameFile(String source, String destination) { return rename(source, destination); }

    // ============================================================== path helpers

    public static String extension(String path) { return TextFileIO.extension(path); }
    public static String ext(String path) { return extension(path); }
    public static String suffix(String path) { return extension(path); }
    public static String fileName(String path) { return TextFileIO.fileName(path); }
    public static String name(String path) { return fileName(path); }
    public static String baseName(String path) { return TextFileIO.baseName(path); }
    public static String stem(String path) { return baseName(path); }
    public static String directoryOf(String path) { return TextFileIO.directoryOf(path); }
    public static String dirOf(String path) { return directoryOf(path); }
    public static String parentOf(String path) { return directoryOf(path); }

    // ============================================================== new: environment

    /** Current working directory. */
    public static String workingDir() {
        return System.getProperty("user.dir", ".");
    }
    public static String cwd() { return workingDir(); }

    /** User's home directory. */
    public static String homeDir() {
        return System.getProperty("user.home", ".");
    }
    public static String home() { return homeDir(); }

    /** System temp directory. */
    public static String tempDir() {
        return System.getProperty("java.io.tmpdir", ".");
    }

    /** Create a temporary file — deleted only if the caller does so. */
    public static String tempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix == null ? "chirp" : prefix,
                    suffix == null ? ".tmp" : suffix).toString();
        } catch (IOException e) {
            Logger.error("Cannot create temp file: " + e.getMessage());
            return "";
        }
    }
    public static String tempFile() { return tempFile("chirp", ".tmp"); }

    /** Create a temporary directory. */
    public static String tempDirectory() {
        try {
            return Files.createTempDirectory("chirp").toString();
        } catch (IOException e) {
            Logger.error("Cannot create temp directory: " + e.getMessage());
            return "";
        }
    }
    public static String tempDirCreate() { return tempDirectory(); }

    // ============================================================== new: touch

    /** Creates an empty file if it does not exist. */
    public static boolean touch(String path) {
        try {
            if (!Files.exists(Path.of(path))) Files.createFile(Path.of(path));
            return true;
        } catch (IOException e) {
            Logger.error("Cannot touch " + path);
            return false;
        }
    }
}
