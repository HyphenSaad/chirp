package chirp.io;

import chirp.internal.Logger;
import chirp.internal.Trace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file operations — every common mode, aliased.
 *
 * <h2>Naming</h2>
 * <p>{@code readAll / read / readText / load}; {@code readLines / lines /
 * getLines / asLines}; {@code write / save / saveAll / writeText};
 * {@code append / addLine / addLines}; {@code prepend / addFirst};
 * {@code head / firstN}; {@code tail / lastN}; {@code grep / search / findInFile};
 * {@code extension / ext / suffix}; {@code fileName / name / justName};
 * {@code baseName / stem / nameWithoutExtension};
 * {@code directoryOf / dirOf / folderOf / parentOf}.</p>
 *
 * <h2>Fluent chains</h2>
 * <p>{@code TextFile.of(path)} returns a fluent {@link TextFile} — useful
 * for repeated access to the same file.</p>
 *
 * <p>Every method returns a safe default on failure and prints a friendly
 * error message. Never throws a checked exception.</p>
 */
public final class TextFileIO {

    private TextFileIO() {}

    // ============================================================== chains

    public static TextFile of(String path) { return TextFile.of(path); }
    public static TextFile open(String path) { return TextFile.of(path); }
    public static TextFile at(String path) { return TextFile.of(path); }
    public static TextFile on(String path) { return TextFile.of(path); }

    // ============================================================== read

    public static String readAll(String path) {
        Trace.enter("file", "readAll", path);
        try {
            String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
            Trace.debug("file", "read {} chars from {}", content.length(), path);
            Trace.exit("file", "readAll", content.length() + " chars");
            return content;
        } catch (IOException e) {
            Trace.warn("file", "readAll failed: " + e.getMessage());
            Logger.error("Cannot read " + path);
            return "";
        }
    }
    public static String read(String path) { return readAll(path); }
    public static String readText(String path) { return readAll(path); }
    public static String load(String path) { return readAll(path); }
    public static String loadText(String path) { return readAll(path); }

    public static List<String> readLines(String path) {
        Trace.enter("file", "readLines", path);
        try {
            List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
            Trace.debug("file", "read {} lines from {}", lines.size(), path);
            Trace.exit("file", "readLines", lines.size() + " lines");
            return lines;
        } catch (IOException e) {
            Trace.warn("file", "readLines failed: " + e.getMessage());
            Logger.error("Cannot read " + path);
            return new ArrayList<>();
        }
    }
    public static List<String> lines(String path) { return readLines(path); }
    public static List<String> getLines(String path) { return readLines(path); }
    public static List<String> asLines(String path) { return readLines(path); }

    public static List<String> head(String path, int count) {
        List<String> lines = readLines(path);
        if (count <= 0 || count >= lines.size()) return lines;
        return new ArrayList<>(lines.subList(0, count));
    }
    public static List<String> firstN(String path, int count) { return head(path, count); }

    public static List<String> tail(String path, int count) {
        List<String> lines = readLines(path);
        if (count <= 0 || count >= lines.size()) return lines;
        return new ArrayList<>(lines.subList(lines.size() - count, lines.size()));
    }
    public static List<String> lastN(String path, int count) { return tail(path, count); }

    public static String firstLine(String path) {
        List<String> lines = readLines(path);
        return lines.isEmpty() ? "" : lines.get(0);
    }
    public static String head1(String path) { return firstLine(path); }

    public static String lastLine(String path) {
        List<String> lines = readLines(path);
        return lines.isEmpty() ? "" : lines.get(lines.size() - 1);
    }
    public static String tail1(String path) { return lastLine(path); }

    /** Line at 0-based index — returns empty string when out of range. */
    public static String line(String path, int index) {
        List<String> lines = readLines(path);
        if (index < 0 || index >= lines.size()) return "";
        return lines.get(index);
    }
    public static String lineAt(String path, int index) { return line(path, index); }
    public static String getLine(String path, int index) { return line(path, index); }

    /** Lines {@code [from, to)} — 0-based, to exclusive. */
    public static List<String> slice(String path, int from, int to) {
        List<String> lines = readLines(path);
        int start = Math.max(0, from);
        int end = Math.min(lines.size(), to);
        if (start >= end) return new ArrayList<>();
        return new ArrayList<>(lines.subList(start, end));
    }
    public static List<String> linesBetween(String path, int from, int to) { return slice(path, from, to); }

    // ============================================================== write

    public static boolean write(String path, String content) {
        Trace.enter("file", "write", path, "bytes=" + (content == null ? 0 : content.length()));
        try {
            Files.writeString(Path.of(path), content == null ? "" : content);
            Trace.exit("file", "write", true);
            return true;
        } catch (IOException e) {
            Trace.warn("file", "write failed: " + e.getMessage());
            Logger.error("Cannot write " + path);
            return false;
        }
    }
    public static boolean save(String path, String content) { return write(path, content); }
    public static boolean saveAll(String path, String content) { return write(path, content); }
    public static boolean writeText(String path, String content) { return write(path, content); }

    public static boolean writeLines(String path, List<String> lines) {
        Trace.enter("file", "writeLines", path, "lines=" + (lines == null ? 0 : lines.size()));
        try {
            Files.write(Path.of(path), lines == null ? List.of() : lines);
            Trace.exit("file", "writeLines", true);
            return true;
        } catch (IOException e) {
            Trace.warn("file", "writeLines failed: " + e.getMessage());
            Logger.error("Cannot write " + path);
            return false;
        }
    }
    public static boolean saveLines(String path, List<String> lines) { return writeLines(path, lines); }
    public static boolean writeAllLines(String path, List<String> l) { return writeLines(path, l); }

    public static boolean append(String path, String content) {
        try {
            Files.writeString(Path.of(path), content == null ? "" : content,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot append to " + path);
            return false;
        }
    }
    public static boolean add(String path, String content) { return append(path, content); }
    public static boolean addText(String path, String c) { return append(path, c); }

    public static boolean appendLine(String path, String line) {
        return append(path, (line == null ? "" : line) + System.lineSeparator());
    }
    public static boolean addLine(String path, String line) { return appendLine(path, line); }
    public static boolean appendRow(String path, String line) { return appendLine(path, line); }

    public static boolean appendLines(String path, List<String> lines) {
        if (lines == null) return false;
        boolean ok = true;
        for (String line : lines) if (!appendLine(path, line)) ok = false;
        return ok;
    }
    public static boolean addLines(String path, List<String> lines) { return appendLines(path, lines); }

    public static boolean prepend(String path, String content) {
        String existing = exists(path) ? readAll(path) : "";
        return write(path, (content == null ? "" : content) + existing);
    }
    public static boolean prependLine(String path, String line) {
        return prepend(path, (line == null ? "" : line) + System.lineSeparator());
    }
    public static boolean addFirst(String path, String line) { return prependLine(path, line); }

    // ============================================================== stats

    public static int countLines(String path) { return readLines(path).size(); }
    public static int lineCount(String path) { return countLines(path); }
    public static int numLines(String path) { return countLines(path); }

    public static int countChars(String path) { return readAll(path).length(); }
    public static int charCount(String path) { return countChars(path); }

    public static int countWords(String path) {
        int total = 0;
        for (String line : readLines(path)) {
            if (line.isBlank()) continue;
            total += line.trim().split("\\s+").length;
        }
        return total;
    }
    public static int wordCount(String path) { return countWords(path); }

    public static long sizeInBytes(String path) {
        try {
            long size = Files.size(Path.of(path));
            Trace.debug("file", "sizeInBytes({}) = {}", path, size);
            return size;
        } catch (IOException e) {
            return -1;
        }
    }
    public static long size(String path) { return sizeInBytes(path); }
    public static long fileSize(String path) { return sizeInBytes(path); }

    /** Human-readable file size — "1.5 KB", "2.3 MB". */
    public static String humanSize(String path) {
        long bytes = sizeInBytes(path);
        return chirp.number.Numbers.fileSize(bytes);
    }
    public static String formattedSize(String path) { return humanSize(path); }

    // ============================================================== search

    public static List<String> grep(String path, String keyword) {
        List<String> matches = new ArrayList<>();
        if (keyword == null) return matches;
        for (String line : readLines(path)) {
            if (line.contains(keyword)) matches.add(line);
        }
        Trace.debug("file", "grep({}, \"{}\") -> {} matches", path, keyword, matches.size());
        return matches;
    }
    public static List<String> search(String path, String keyword) { return grep(path, keyword); }
    public static List<String> findInFile(String path, String keyword) { return grep(path, keyword); }
    public static List<String> matchingLines(String path, String kw) { return grep(path, kw); }

    public static List<Integer> lineNumbersContaining(String path, String keyword) {
        List<Integer> result = new ArrayList<>();
        if (keyword == null) return result;
        List<String> lines = readLines(path);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(keyword)) result.add(i + 1);
        }
        return result;
    }
    public static List<Integer> findLineNumbers(String path, String kw) { return lineNumbersContaining(path, kw); }
    public static List<Integer> matchingLineNumbers(String path, String k) { return lineNumbersContaining(path, k); }

    public static boolean containsText(String path, String keyword) {
        if (keyword == null) return false;
        return readAll(path).contains(keyword);
    }

    public static String replaceInFile(String path, String target, String replacement) {
        String content = readAll(path);
        if (target == null || target.isEmpty()) return content;
        String updated = content.replace(target, replacement == null ? "" : replacement);
        write(path, updated);
        return updated;
    }
    public static String replace(String path, String target, String repl) { return replaceInFile(path, target, repl); }
    public static String replaceText(String path, String t, String r) { return replaceInFile(path, t, r); }

    // ============================================================== filesystem

    /**
     * {@code false} for a {@code null} or empty path — {@code Path.of("")}
     * resolves to the working directory, which always exists.
     */
    public static boolean exists(String path) {
        return path != null && !path.isEmpty() && Files.exists(Path.of(path));
    }
    public static boolean existsFile(String path) { return exists(path); }

    public static boolean delete(String path) {
        try {
            boolean deleted = Files.deleteIfExists(Path.of(path));
            Trace.debug("file", "delete({}) = {}", path, deleted);
            return deleted;
        } catch (IOException e) {
            Logger.error("Cannot delete " + path);
            return false;
        }
    }
    public static boolean remove(String path) { return delete(path); }

    public static boolean copy(String source, String destination) {
        try {
            Files.copy(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            Trace.debug("file", "copy({} -> {})", source, destination);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy " + source + " → " + destination);
            return false;
        }
    }

    public static boolean move(String source, String destination) {
        try {
            Files.move(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            Trace.debug("file", "move({} -> {})", source, destination);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot move " + source + " → " + destination);
            return false;
        }
    }
    public static boolean rename(String source, String destination) { return move(source, destination); }

    public static List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(Path.of(directory))) {
            stream.forEach(p -> {
                // Root paths (filesystem roots) have no filename component.
                Path name = p.getFileName();
                if (name != null) names.add(name.toString());
            });
            Trace.debug("file", "listFiles({}) -> {} entries", directory, names.size());
        } catch (IOException e) {
            Logger.error("Cannot list " + directory);
        }
        return names;
    }
    public static List<String> ls(String directory) { return listFiles(directory); }

    // ============================================================== path helpers

    public static String extension(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return "";
        return path.substring(dot + 1);
    }
    public static String ext(String path) { return extension(path); }
    public static String suffix(String path) { return extension(path); }

    public static String fileName(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? path : path.substring(slash + 1);
    }
    public static String name(String path) { return fileName(path); }
    public static String justName(String path) { return fileName(path); }

    public static String baseName(String path) {
        String name = fileName(path);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
    public static String stem(String path) { return baseName(path); }
    public static String nameWithoutExtension(String path) { return baseName(path); }

    public static String directoryOf(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? "" : path.substring(0, slash);
    }
    public static String dirOf(String path) { return directoryOf(path); }
    public static String folderOf(String path) { return directoryOf(path); }
    public static String parentOf(String path) { return directoryOf(path); }

    // ============================================================== integration bridges

    /** Read a file as JSON. Returns empty object on failure. */
    public static chirp.json.JsonObject readJson(String path) {
        return JsonFile.readObject(path);
    }

    /** Write a JSON value to a file (pretty-printed). */
    public static boolean writeJson(String path, chirp.json.JsonValue value) {
        return JsonFile.writePretty(path, value);
    }

    /** Read a file as CSV rows. */
    public static List<List<String>> readCsv(String path) {
        return CsvFile.read(path);
    }

    /** Write CSV rows to a file. */
    public static boolean writeCsv(String path, List<List<String>> rows) {
        return CsvFile.write(path, rows);
    }
}
