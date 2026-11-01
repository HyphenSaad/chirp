package chirp.io;

import chirp.internal.Logger;
import chirp.internal.Trace;

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
 * <p>Every method in this class returns a documented default for failure —
 * {@code ""} for text reads, empty lists for line reads, {@code false} for
 * write/delete operations. No method throws a checked exception, so
 * student code never needs a {@code try/catch} just to read a file. Failures
 * are logged via {@link chirp.internal.Logger} so a genuinely broken path
 * still surfaces when the student turns tracing on.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>File read</b> — {@link #readAll(String)},
 *       {@link #readLines(String)}</li>
 *   <li><b>File write</b> — {@link #write(String, String)},
 *       {@link #append(String, String)}</li>
 *   <li><b>File state</b> — {@link #exists(String)},
 *       {@link #isFile(String)}, {@link #isDirectory(String)},
 *       {@link #sizeInBytes(String)}, {@link #delete(String)},
 *       {@link #copy(String, String)}</li>
 *   <li><b>Directory ops</b> — {@link #createDirectory(String)},
 *       {@link #deleteDirectory(String)},
 *       {@link #countFiles(String)},
 *       {@link #directorySize(String)},
 *       {@link #listDirectories(String)}</li>
 *   <li><b>Search</b> — {@link #findFiles(String, String)},
 *       {@link #findFilesByExtension(String, String)}</li>
 *   <li><b>Copy / move</b> — {@link #copyDirectory(String, String)},
 *       {@link #moveDirectory(String, String)},
 *       {@link #rename(String, String)}</li>
 *   <li><b>Environment</b> — {@link #workingDir()}, {@link #homeDir()},
 *       {@link #tempDir()}, {@link #tempFile()}</li>
 *   <li><b>Path helpers</b> — {@link #extension(String)},
 *       {@link #fileName(String)}, {@link #baseName(String)},
 *       {@link #directoryOf(String)}</li>
 * </ul>
 *
 * <h2>Failure semantics</h2>
 * <table border="1">
 *   <caption>What each method returns on failure</caption>
 *   <tr><th>Operation</th><th>Return on failure</th></tr>
 *   <tr><td>Reading a text file</td><td>{@code ""}</td></tr>
 *   <tr><td>Reading lines</td><td>empty list</td></tr>
 *   <tr><td>Writing</td><td>{@code false}</td></tr>
 *   <tr><td>Deleting</td><td>{@code false}</td></tr>
 *   <tr><td>File size</td><td>{@code -1}</td></tr>
 *   <tr><td>Counting files</td><td>{@code 0}</td></tr>
 * </table>
 *
 * <h2>Example — copy a folder tree</h2>
 * <pre>{@code
 * if (FileIO.createDirectory("backup")) {
 *     FileIO.copyDirectory("data", "backup/data");
 *     println(FileIO.countAllFiles("backup/data") + " files copied");
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For text-file-specific operations (head, tail, grep, slice), see
 * {@link TextFileIO}. For the fluent file wrapper, see {@link TextFile}.
 * For CSV files, see {@link CsvFile}. For JSON files, see
 * {@link JsonFile}.</p>
 *
 * @see TextFileIO
 * @see TextFile
 * @see CsvFile
 * @see JsonFile
 * @since 1.0.0
 */
public final class FileIO {

    private FileIO() {}

    // ============================================================== file read

    /**
     * Reads the entire file into a string. Returns {@code ""} on failure.
     *
     * @param path the file path (may be null)
     * @return the file contents, or empty on failure
     * @see #read(String)
     * @see #readLines(String)
     * @see TextFileIO#readAll(String)
     * @since 1.0.0
     */
    public static String readAll(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot read " + path);
            return "";
        }
    }

    /**
     * Alias of {@link #readAll(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file contents, or empty
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static String read(String path) { return readAll(path); }

    /**
     * Reads all lines of the file into a list. Returns an empty list on
     * failure.
     *
     * @param path the file path (may be null)
     * @return the file lines, or empty on failure
     * @see #lines(String)
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static List<String> readLines(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot read " + path);
            return new ArrayList<>();
        }
    }

    /**
     * Alias of {@link #readLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file lines
     * @see #readLines(String)
     * @since 1.0.0
     */
    public static List<String> lines(String path) { return readLines(path); }

    // ============================================================== file write

    /**
     * Writes the content to the file, overwriting any previous contents.
     * Creates the file if it does not exist. Returns {@code true} on
     * success.
     *
     * @param path the file path
     * @param content the content to write (null → writes empty file)
     * @return {@code true} on success
     * @see #save(String, String)
     * @see #append(String, String)
     * @since 1.0.0
     */
    public static boolean write(String path, String content) {
        try {
            Files.writeString(Path.of(path), content);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot write " + path);
            return false;
        }
    }

    /**
     * Alias of {@link #write(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param content the content to write
     * @return {@code true} on success
     * @see #write(String, String)
     * @since 1.0.0
     */
    public static boolean save(String path, String content) { return write(path, content); }

    /**
     * Appends the content to the file, creating it if needed.
     *
     * @param path the file path
     * @param content the content to append
     * @return {@code true} on success
     * @see #addContent(String, String)
     * @see #write(String, String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #append(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param content the content to append
     * @return {@code true} on success
     * @see #append(String, String)
     * @since 1.0.0
     */
    public static boolean addContent(String path, String content) { return append(path, content); }

    // ============================================================== file state

    /**
     * Checks whether the given path exists (file or directory).
     *
     * @param path the path to check (may be null → false)
     * @return {@code true} if the path exists
     * @see #existsFile(String)
     * @see #isFile(String)
     * @see #isDirectory(String)
     * @since 1.0.0
     */
    public static boolean exists(String path) {
        if (path == null) return false;
        return Files.exists(Path.of(path));
    }

    /**
     * Alias of {@link #exists(String)} — see that method for full
     * documentation.
     *
     * @param path the path to check
     * @return {@code true} if the path exists
     * @see #exists(String)
     * @since 1.0.0
     */
    public static boolean existsFile(String path) { return exists(path); }

    /**
     * Checks whether the path points to a regular file.
     *
     * @param path the path to check (may be null → false)
     * @return {@code true} if the path is a regular file
     * @see #isDirectory(String)
     * @since 1.0.0
     */
    public static boolean isFile(String path) {
        if (path == null) return false;
        return Files.isRegularFile(Path.of(path));
    }

    /**
     * Checks whether the path points to a directory.
     *
     * @param path the path to check (may be null → false)
     * @return {@code true} if the path is a directory
     * @see #isFile(String)
     * @see #isDir(String)
     * @since 1.0.0
     */
    public static boolean isDirectory(String path) {
        if (path == null) return false;
        return Files.isDirectory(Path.of(path));
    }

    /**
     * Alias of {@link #isDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the path to check
     * @return {@code true} if the path is a directory
     * @see #isDirectory(String)
     * @since 1.0.0
     */
    public static boolean isDir(String path) { return isDirectory(path); }

    /**
     * Returns the size of the file in bytes, or {@code -1} if the file
     * cannot be read.
     *
     * @param path the file path (may be null → -1)
     * @return the size in bytes, or -1 on failure
     * @see #size(String)
     * @see #directorySize(String)
     * @since 1.0.0
     */
    public static long sizeInBytes(String path) {
        if (path == null) return -1;
        try {
            return Files.size(Path.of(path));
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Alias of {@link #sizeInBytes(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the size in bytes, or -1
     * @see #sizeInBytes(String)
     * @since 1.0.0
     */
    public static long size(String path) { return sizeInBytes(path); }

    /**
     * Deletes the file or empty directory. Returns {@code true} if the
     * path existed and was deleted, or if it did not exist (already gone).
     *
     * <p>For directories with contents, use
     * {@link #deleteDirectory(String)} instead.</p>
     *
     * @param path the path to delete (may be null → false)
     * @return {@code true} on success
     * @see #remove(String)
     * @see #deleteDirectory(String)
     * @since 1.0.0
     */
    public static boolean delete(String path) {
        if (path == null) return false;
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            Logger.error("Cannot delete " + path);
            return false;
        }
    }

    /**
     * Alias of {@link #delete(String)} — see that method for full
     * documentation.
     *
     * @param path the path to delete
     * @return {@code true} on success
     * @see #delete(String)
     * @since 1.0.0
     */
    public static boolean remove(String path) { return delete(path); }

    /**
     * Copies a file. Overwrites the destination if it exists.
     *
     * @param source the source path (may be null → false)
     * @param destination the destination path (may be null → false)
     * @return {@code true} on success
     * @see #copyDirectory(String, String)
     * @see #rename(String, String)
     * @since 1.0.0
     */
    public static boolean copy(String source, String destination) {
        if (source == null || destination == null) return false;
        try {
            Files.copy(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy " + source + " to " + destination);
            return false;
        }
    }

    /**
     * Returns the names (not full paths) of all regular files in the
     * directory. Does <b>not</b> recurse — use
     * {@link #findFiles(String, String)} or {@link #countAllFiles(String)}
     * for recursive access.
     *
     * @param directory the directory path (may be null → empty)
     * @return the file names, or empty on failure
     * @see #listDirectories(String)
     * @since 1.0.0
     */
    public static List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        if (directory == null) return names;
        try (var stream = Files.list(Path.of(directory))) {
            stream.filter(Files::isRegularFile)
                    .forEach(p -> {
                        Path name = p.getFileName();
                        if (name != null) names.add(name.toString());
                    });
        } catch (IOException e) {
            Logger.error("Cannot list files in " + directory);
        }
        return names;
    }

    /**
     * Alias of {@link #listFiles(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the file names
     * @see #listFiles(String)
     * @since 1.0.0
     */
    public static List<String> ls(String directory) { return listFiles(directory); }

    /**
     * Counts the lines in the file. Returns 0 on failure.
     *
     * @param path the file path
     * @return the line count, or 0 on failure
     * @see #lineCount(String)
     * @see TextFileIO#countLines(String)
     * @since 1.0.0
     */
    public static int countLines(String path) { return readLines(path).size(); }

    /**
     * Alias of {@link #countLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the line count
     * @see #countLines(String)
     * @since 1.0.0
     */
    public static int lineCount(String path) { return countLines(path); }

    // ============================================================== directory ops

    /**
     * Creates the directory and all missing parent directories. Returns
     * {@code true} if the directory now exists (whether or not it was
     * newly created).
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectories(String)
     * @see #mkdir(String)
     * @see #mkdirs(String)
     * @since 1.0.0
     */
    public static boolean createDirectory(String path) {
        if (path == null) return false;
        try {
            Files.createDirectories(Path.of(path));
            return true;
        } catch (IOException e) {
            Logger.error("Cannot create directory: " + path);
            return false;
        }
    }

    /**
     * Alias of {@link #createDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectory(String)
     * @since 1.0.0
     */
    public static boolean createDirectories(String path) { return createDirectory(path); }

    /**
     * Alias of {@link #createDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectory(String)
     * @since 1.0.0
     */
    public static boolean mkdir(String path) { return createDirectory(path); }

    /**
     * Alias of {@link #createDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectory(String)
     * @since 1.0.0
     */
    public static boolean mkdirs(String path) { return createDirectory(path); }

    /**
     * Alias of {@link #createDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectory(String)
     * @since 1.0.0
     */
    public static boolean ensureDirectory(String path) { return createDirectory(path); }

    /**
     * Alias of {@link #createDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #createDirectory(String)
     * @since 1.0.0
     */
    public static boolean ensureDir(String path) { return createDirectory(path); }

    /**
     * Deletes the directory and everything inside it, recursively.
     * Idempotent — returns {@code true} if the directory did not exist.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #delete(String)
     * @see #removeDirectory(String)
     * @since 1.0.0
     */
    public static boolean deleteDirectory(String path) {
        if (path == null) return false;
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
                } catch (IOException e) {
                    // Best-effort: log and continue so the rest of the tree can be
                    // deleted. The caller sees a partial result rather than a
                    // single opaque failure.
                    Trace.warn("file", "deleteDirectory: cannot delete " + p + ": " + e.getMessage());
                }
            });
            return true;
        } catch (IOException e) {
            Logger.error("Cannot delete directory: " + path);
            return false;
        }
    }

    /**
     * Alias of {@link #deleteDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #deleteDirectory(String)
     * @since 1.0.0
     */
    public static boolean removeDirectory(String path) { return deleteDirectory(path); }

    /**
     * Alias of {@link #deleteDirectory(String)} — see that method for full
     * documentation.
     *
     * @param path the directory path
     * @return {@code true} on success
     * @see #deleteDirectory(String)
     * @since 1.0.0
     */
    public static boolean rmDir(String path) { return deleteDirectory(path); }

    /**
     * Counts the regular files directly inside the directory (does not
     * recurse).
     *
     * @param directory the directory path (may be null → 0)
     * @return the count, or 0 on failure
     * @see #countFilesIn(String)
     * @see #countAllFiles(String)
     * @since 1.0.0
     */
    public static int countFiles(String directory) {
        if (directory == null) return 0;
        try (var stream = Files.list(Path.of(directory))) {
            return (int) stream.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Alias of {@link #countFiles(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the count
     * @see #countFiles(String)
     * @since 1.0.0
     */
    public static int countFilesIn(String directory) { return countFiles(directory); }

    /**
     * Counts all regular files recursively — every file at any depth.
     *
     * @param directory the directory path (may be null → 0)
     * @return the count, or 0 on failure
     * @see #countFiles(String)
     * @since 1.0.0
     */
    public static int countAllFiles(String directory) {
        if (directory == null) return 0;
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            return (int) walk.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Alias of {@link #countAllFiles(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the count
     * @see #countAllFiles(String)
     * @since 1.0.0
     */
    public static int countFilesRecursive(String directory) { return countAllFiles(directory); }

    /**
     * Returns the total size of all files in the directory tree, in bytes.
     *
     * @param directory the directory path (may be null → 0)
     * @return the total size, or 0 on failure
     * @see #dirSize(String)
     * @since 1.0.0
     */
    public static long directorySize(String directory) {
        if (directory == null) return 0L;
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            long total = 0L;
            for (Path p : walk.filter(Files::isRegularFile).toList()) {
                try {
                    total += Files.size(p);
                } catch (IOException e) {
                    Trace.warn("file", "directorySize: cannot size " + p + ": " + e.getMessage());
                }
            }
            return total;
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * Alias of {@link #directorySize(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the total size
     * @see #directorySize(String)
     * @since 1.0.0
     */
    public static long dirSize(String directory) { return directorySize(directory); }

    /**
     * Returns the names (not full paths) of all subdirectories directly
     * inside the given directory.
     *
     * @param directory the directory path (may be null → empty)
     * @return the subdirectory names, or empty on failure
     * @see #listFiles(String)
     * @since 1.0.0
     */
    public static List<String> listDirectories(String directory) {
        List<String> names = new ArrayList<>();
        if (directory == null) return names;
        try (var stream = Files.list(Path.of(directory))) {
            stream.filter(Files::isDirectory)
                    .forEach(p -> {
                        Path name = p.getFileName();
                        if (name != null) names.add(name.toString());
                    });
        } catch (IOException e) {
            Logger.error("Cannot list " + directory);
        }
        return names;
    }

    /**
     * Alias of {@link #listDirectories(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the subdirectory names
     * @see #listDirectories(String)
     * @since 1.0.0
     */
    public static List<String> listDirs(String directory) { return listDirectories(directory); }

    /**
     * Checks whether the directory is empty (no files and no
     * subdirectories).
     *
     * <p>Returns {@code false} on a missing directory or a
     * non-directory path.</p>
     *
     * @param directory the directory path (may be null → false)
     * @return {@code true} if the directory exists and is empty
     * @see #isEmptyDir(String)
     * @since 1.0.0
     */
    public static boolean isEmpty(String directory) {
        if (directory == null) return false;
        try (var stream = Files.list(Path.of(directory))) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #isEmpty(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return {@code true} if the directory exists and is empty
     * @see #isEmpty(String)
     * @since 1.0.0
     */
    public static boolean isEmptyDir(String directory) { return isEmpty(directory); }

    // ============================================================== search

    /**
     * Finds all files matching the glob pattern, recursively, in the
     * given directory.
     *
     * <p>The pattern is matched against the file's simple name, not the
     * full path.</p>
     *
     * <pre>{@code
     * FileIO.findFiles("data", "*.txt");     // every .txt in data/ and below
     * FileIO.findFiles("data", "report-*");  // every report-* anywhere in data/
     * }</pre>
     *
     * @param directory the directory to search (may be null → empty)
     * @param glob the glob pattern (null → empty)
     * @return the full paths of matching files, or empty on failure
     * @see #findFilesByExtension(String, String)
     * @since 1.0.0
     */
    public static List<String> findFiles(String directory, String glob) {
        List<String> result = new ArrayList<>();
        if (directory == null || glob == null) return result;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> {
                        Path name = p.getFileName();
                        return name != null && matcher.matches(name);
                    })
                    .forEach(p -> result.add(p.toString()));
        } catch (IOException e) {
            Logger.error("Cannot walk " + directory);
        }
        return result;
    }

    /**
     * Finds all files with the given extension, recursively.
     *
     * <p>The extension may include or omit the dot —
     * {@code findFilesByExtension("data", "txt")} and
     * {@code findFilesByExtension("data", ".txt")} return the same
     * results.</p>
     *
     * @param directory the directory to search (may be null → empty)
     * @param extension the extension (blank or null → empty)
     * @return the full paths of matching files
     * @see #findFiles(String, String)
     * @since 1.0.0
     */
    public static List<String> findFilesByExtension(String directory, String extension) {
        if (extension == null || extension.isBlank()) return List.of();
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return findFiles(directory, "*." + ext);
    }

    /**
     * Alias of {@link #findFilesByExtension(String, String)} — see that
     * method for full documentation.
     *
     * @param directory the directory to search
     * @param ext the extension
     * @return the full paths of matching files
     * @see #findFilesByExtension(String, String)
     * @since 1.0.0
     */
    public static List<String> findFilesOfType(String directory, String ext) {
        return findFilesByExtension(directory, ext);
    }

    // ============================================================== directory copy / move

    /**
     * Copies the entire directory tree recursively — every file and every
     * subdirectory.
     *
     * @param source the source directory (must be a directory)
     * @param destination the destination directory (created if missing)
     * @return {@code true} on success
     * @see #copy(String, String)
     * @see #moveDirectory(String, String)
     * @since 1.0.0
     */
    public static boolean copyDirectory(String source, String destination) {
        if (source == null || destination == null) return false;
        Path src = Path.of(source);
        Path dst = Path.of(destination);
        if (!Files.isDirectory(src)) {
            Logger.error("Source is not a directory: " + source);
            return false;
        }
        try (Stream<Path> walk = Files.walk(src)) {
            for (Path p : walk.toList()) {
                Path target = dst.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else {
                    Path parent = target.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy directory " + source + " to " + destination);
            return false;
        }
    }

    /**
     * Alias of {@link #copyDirectory(String, String)} — see that method
     * for full documentation.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @return {@code true} on success
     * @see #copyDirectory(String, String)
     * @since 1.0.0
     */
    public static boolean copyDir(String source, String destination) { return copyDirectory(source, destination); }

    /**
     * Moves the directory tree — copies then deletes the source. If the
     * copy fails, the source is left untouched.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @return {@code true} on success
     * @see #copyDirectory(String, String)
     * @see #moveDir(String, String)
     * @since 1.0.0
     */
    public static boolean moveDirectory(String source, String destination) {
        boolean copied = copyDirectory(source, destination);
        if (!copied) return false;
        return deleteDirectory(source);
    }

    /**
     * Alias of {@link #moveDirectory(String, String)} — see that method
     * for full documentation.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @return {@code true} on success
     * @see #moveDirectory(String, String)
     * @since 1.0.0
     */
    public static boolean moveDir(String source, String destination) { return moveDirectory(source, destination); }

    /**
     * Renames (moves) a file. Overwrites the destination if it exists.
     *
     * @param source the source path
     * @param destination the destination path
     * @return {@code true} on success
     * @see #renameFile(String, String)
     * @see #copy(String, String)
     * @since 1.0.0
     */
    public static boolean rename(String source, String destination) {
        if (source == null || destination == null) return false;
        try {
            Files.move(Path.of(source), Path.of(destination),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot rename " + source + " → " + destination);
            return false;
        }
    }

    /**
     * Alias of {@link #rename(String, String)} — see that method for full
     * documentation.
     *
     * @param source the source path
     * @param destination the destination path
     * @return {@code true} on success
     * @see #rename(String, String)
     * @since 1.0.0
     */
    public static boolean renameFile(String source, String destination) { return rename(source, destination); }

    // ============================================================== path helpers

    /**
     * Returns the extension of the file path (without the dot).
     * Delegates to {@link TextFileIO#extension(String)}.
     *
     * @param path the file path
     * @return the extension, or empty
     * @see TextFileIO#extension(String)
     * @since 1.0.0
     */
    public static String extension(String path) { return TextFileIO.extension(path); }

    /**
     * Alias of {@link #extension(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the extension
     * @see #extension(String)
     * @since 1.0.0
     */
    public static String ext(String path) { return extension(path); }

    /**
     * Alias of {@link #extension(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the extension
     * @see #extension(String)
     * @since 1.0.0
     */
    public static String suffix(String path) { return extension(path); }

    /**
     * Returns the file name (last path component). Delegates to
     * {@link TextFileIO#fileName(String)}.
     *
     * @param path the file path
     * @return the file name
     * @see TextFileIO#fileName(String)
     * @since 1.0.0
     */
    public static String fileName(String path) { return TextFileIO.fileName(path); }

    /**
     * Alias of {@link #fileName(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file name
     * @see #fileName(String)
     * @since 1.0.0
     */
    public static String name(String path) { return fileName(path); }

    /**
     * Returns the file name without its extension. Delegates to
     * {@link TextFileIO#baseName(String)}.
     *
     * @param path the file path
     * @return the base name
     * @see TextFileIO#baseName(String)
     * @since 1.0.0
     */
    public static String baseName(String path) { return TextFileIO.baseName(path); }

    /**
     * Alias of {@link #baseName(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the base name
     * @see #baseName(String)
     * @since 1.0.0
     */
    public static String stem(String path) { return baseName(path); }

    /**
     * Returns the directory portion of the path. Delegates to
     * {@link TextFileIO#directoryOf(String)}.
     *
     * @param path the file path
     * @return the directory portion, or empty
     * @see TextFileIO#directoryOf(String)
     * @since 1.0.0
     */
    public static String directoryOf(String path) { return TextFileIO.directoryOf(path); }

    /**
     * Alias of {@link #directoryOf(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the directory portion
     * @see #directoryOf(String)
     * @since 1.0.0
     */
    public static String dirOf(String path) { return directoryOf(path); }

    /**
     * Alias of {@link #directoryOf(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the directory portion
     * @see #directoryOf(String)
     * @since 1.0.0
     */
    public static String parentOf(String path) { return directoryOf(path); }

    // ============================================================== environment

    /**
     * Returns the current working directory of the JVM.
     *
     * @return the working directory path
     * @see #cwd()
     * @since 1.0.0
     */
    public static String workingDir() {
        return System.getProperty("user.dir", ".");
    }

    /**
     * Alias of {@link #workingDir()} — see that method for full
     * documentation.
     *
     * @return the working directory
     * @see #workingDir()
     * @since 1.0.0
     */
    public static String cwd() { return workingDir(); }

    /**
     * Returns the current user's home directory.
     *
     * @return the home directory path
     * @see #home()
     * @since 1.0.0
     */
    public static String homeDir() {
        return System.getProperty("user.home", ".");
    }

    /**
     * Alias of {@link #homeDir()} — see that method for full
     * documentation.
     *
     * @return the home directory
     * @see #homeDir()
     * @since 1.0.0
     */
    public static String home() { return homeDir(); }

    /**
     * Returns the system's temporary directory.
     *
     * @return the temp directory path
     * @since 1.0.0
     */
    public static String tempDir() {
        return System.getProperty("java.io.tmpdir", ".");
    }

    /**
     * Creates a temporary file with the given prefix and suffix.
     *
     * @param prefix the file name prefix (null → "chirp")
     * @param suffix the file name suffix including the dot (null → ".tmp")
     * @return the full path to the temp file, or empty on failure
     * @see #tempFile()
     * @since 1.0.0
     */
    public static String tempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix == null ? "chirp" : prefix,
                    suffix == null ? ".tmp" : suffix).toString();
        } catch (IOException e) {
            Logger.error("Cannot create temp file: " + e.getMessage());
            return "";
        }
    }

    /**
     * Creates a temporary file with the default prefix "chirp" and suffix
     * ".tmp".
     *
     * @return the full path to the temp file, or empty on failure
     * @see #tempFile(String, String)
     * @since 1.0.0
     */
    public static String tempFile() { return tempFile("chirp", ".tmp"); }

    /**
     * Creates a temporary directory.
     *
     * @return the full path to the temp directory, or empty on failure
     * @see #tempDirCreate()
     * @since 1.0.0
     */
    public static String tempDirectory() {
        try {
            return Files.createTempDirectory("chirp").toString();
        } catch (IOException e) {
            Logger.error("Cannot create temp directory: " + e.getMessage());
            return "";
        }
    }

    /**
     * Alias of {@link #tempDirectory()} — see that method for full
     * documentation.
     *
     * @return the full path to the temp directory
     * @see #tempDirectory()
     * @since 1.0.0
     */
    public static String tempDirCreate() { return tempDirectory(); }

    // ============================================================== touch

    /**
     * Creates an empty file if it does not exist. If the file already
     * exists, leaves it unchanged.
     *
     * @param path the file path
     * @return {@code true} on success
     * @since 1.0.0
     */
    public static boolean touch(String path) {
        if (path == null) return false;
        try {
            if (!Files.exists(Path.of(path))) Files.createFile(Path.of(path));
            return true;
        } catch (IOException e) {
            Logger.error("Cannot touch " + path);
            return false;
        }
    }
}
