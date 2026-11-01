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
 * <p>Adds the text-file-specific operations on top of the general file
 * helpers in {@link FileIO}: head, tail, first/last line, slice, grep,
 * line-number search, and word counts. Every method is <b>null-safe</b>
 * and returns a documented default on failure — no {@code try/catch} is
 * ever needed.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Read</b> — {@link #readAll(String)}, {@link #readLines(String)}</li>
 *   <li><b>Write</b> — {@link #write(String, String)},
 *       {@link #writeLines(String, List)},
 *       {@link #append(String, String)},
 *       {@link #appendLine(String, String)}</li>
 *   <li><b>Head / tail</b> — {@link #head(String, int)},
 *       {@link #tail(String, int)},
 *       {@link #firstLine(String)},
 *       {@link #lastLine(String)}</li>
 *   <li><b>Slice</b> — {@link #line(String, int)},
 *       {@link #slice(String, int, int)}</li>
 *   <li><b>Grep</b> — {@link #grep(String, String)},
 *       {@link #lineNumbersContaining(String, String)},
 *       {@link #containsText(String, String)}</li>
 *   <li><b>Stats</b> — {@link #countLines(String)},
 *       {@link #countWords(String)},
 *       {@link #countChars(String)},
 *       {@link #sizeInBytes(String)},
 *       {@link #humanSize(String)}</li>
 *   <li><b>Search-replace</b> — {@link #replaceInFile(String, String, String)}</li>
 *   <li><b>Filesystem</b> — {@link #exists(String)}, {@link #delete(String)},
 *       {@link #copy(String, String)}, {@link #move(String, String)}</li>
 *   <li><b>Path helpers</b> — {@link #extension(String)},
 *       {@link #fileName(String)},
 *       {@link #baseName(String)},
 *       {@link #directoryOf(String)}</li>
 *   <li><b>Fluent wrapper</b> — {@link #of(String)} → {@link TextFile}</li>
 * </ul>
 *
 * <h2>Failure semantics</h2>
 * <p>Reads return empty string / empty list on failure. Writes return
 * {@code false}. Counts return {@code 0}. Sizes return {@code -1}. This
 * matches {@link FileIO} — the convention is consistent across the whole
 * {@code chirp.io} package.</p>
 *
 * <h2>Example — read the first 5 lines of a log</h2>
 * <pre>{@code
 * List<String> head = TextFileIO.head("app.log", 5);
 * for (String line : head) {
 *     println(line);
 * }
 * }</pre>
 *
 * <h2>Example — search for errors</h2>
 * <pre>{@code
 * List<String> errors = TextFileIO.grep("app.log", "ERROR");
 * List<Integer> lineNumbers = TextFileIO.lineNumbersContaining("app.log", "ERROR");
 * println("Found " + errors.size() + " errors at lines " + lineNumbers);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For general file operations, see {@link FileIO}. For the fluent
 * wrapper (reusable for repeated access to the same path), see
 * {@link TextFile}. For CSV files, see {@link CsvFile}. For JSON files,
 * see {@link JsonFile}.</p>
 *
 * @see FileIO
 * @see TextFile
 * @see CsvFile
 * @see JsonFile
 * @since 1.0.0
 */
public final class TextFileIO {

    private TextFileIO() {}

    // ============================================================== chains

    /**
     * Opens a fluent {@link TextFile} wrapper on the path.
     *
     * @param path the file path (may be null — treated as "")
     * @return a new {@code TextFile} wrapping {@code path}
     * @see TextFile
     * @see #open(String)
     * @see #at(String)
     * @see #on(String)
     * @since 1.0.0
     */
    public static TextFile of(String path) { return TextFile.of(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile open(String path) { return TextFile.of(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile at(String path) { return TextFile.of(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile on(String path) { return TextFile.of(path); }

    // ============================================================== read

    /**
     * Reads the entire file as a string, using UTF-8. Returns {@code ""}
     * on failure.
     *
     * @param path the file path (may be null → "")
     * @return the file content, or empty
     * @see #read(String)
     * @see #readText(String)
     * @see #load(String)
     * @see #readLines(String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #readAll(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file content
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static String read(String path) { return readAll(path); }

    /**
     * Alias of {@link #readAll(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file content
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static String readText(String path) { return readAll(path); }

    /**
     * Alias of {@link #readAll(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file content
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static String load(String path) { return readAll(path); }

    /**
     * Alias of {@link #readAll(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file content
     * @see #readAll(String)
     * @since 1.0.0
     */
    public static String loadText(String path) { return readAll(path); }

    /**
     * Reads the file as a list of lines (UTF-8). Returns an empty list on
     * failure.
     *
     * @param path the file path (may be null → empty)
     * @return the list of lines, or empty
     * @see #lines(String)
     * @see #getLines(String)
     * @see #asLines(String)
     * @see #readAll(String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #readLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the list of lines
     * @see #readLines(String)
     * @since 1.0.0
     */
    public static List<String> lines(String path) { return readLines(path); }

    /**
     * Alias of {@link #readLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the list of lines
     * @see #readLines(String)
     * @since 1.0.0
     */
    public static List<String> getLines(String path) { return readLines(path); }

    /**
     * Alias of {@link #readLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the list of lines
     * @see #readLines(String)
     * @since 1.0.0
     */
    public static List<String> asLines(String path) { return readLines(path); }

    /**
     * Returns the first {@code count} lines of the file. If the file has
     * fewer lines, returns the whole file.
     *
     * @param path the file path (may be null → empty)
     * @param count the number of leading lines (≤ 0 → whole file)
     * @return the first {@code count} lines
     * @see #firstN(String, int)
     * @see #tail(String, int)
     * @see #slice(String, int, int)
     * @since 1.0.0
     */
    public static List<String> head(String path, int count) {
        List<String> lines = readLines(path);
        if (count <= 0 || count >= lines.size()) return lines;
        return new ArrayList<>(lines.subList(0, count));
    }

    /**
     * Alias of {@link #head(String, int)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param count the number of leading lines
     * @return the first {@code count} lines
     * @see #head(String, int)
     * @since 1.0.0
     */
    public static List<String> firstN(String path, int count) { return head(path, count); }

    /**
     * Returns the last {@code count} lines of the file.
     *
     * @param path the file path (may be null → empty)
     * @param count the number of trailing lines (≤ 0 → whole file)
     * @return the last {@code count} lines
     * @see #lastN(String, int)
     * @see #head(String, int)
     * @since 1.0.0
     */
    public static List<String> tail(String path, int count) {
        List<String> lines = readLines(path);
        if (count <= 0 || count >= lines.size()) return lines;
        return new ArrayList<>(lines.subList(lines.size() - count, lines.size()));
    }

    /**
     * Alias of {@link #tail(String, int)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param count the number of trailing lines
     * @return the last {@code count} lines
     * @see #tail(String, int)
     * @since 1.0.0
     */
    public static List<String> lastN(String path, int count) { return tail(path, count); }

    /**
     * Returns the first line of the file, or {@code ""} if the file is
     * empty or unreadable.
     *
     * @param path the file path
     * @return the first line, or ""
     * @see #head1(String)
     * @see #lastLine(String)
     * @see #line(String, int)
     * @since 1.0.0
     */
    public static String firstLine(String path) {
        List<String> lines = readLines(path);
        return lines.isEmpty() ? "" : lines.get(0);
    }

    /**
     * Alias of {@link #firstLine(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the first line
     * @see #firstLine(String)
     * @since 1.0.0
     */
    public static String head1(String path) { return firstLine(path); }

    /**
     * Returns the last line of the file, or {@code ""} if the file is
     * empty or unreadable.
     *
     * @param path the file path
     * @return the last line, or ""
     * @see #tail1(String)
     * @see #firstLine(String)
     * @since 1.0.0
     */
    public static String lastLine(String path) {
        List<String> lines = readLines(path);
        return lines.isEmpty() ? "" : lines.get(lines.size() - 1);
    }

    /**
     * Alias of {@link #lastLine(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the last line
     * @see #lastLine(String)
     * @since 1.0.0
     */
    public static String tail1(String path) { return lastLine(path); }

    /**
     * Returns the line at the given 0-based index, or {@code ""} if the
     * index is out of bounds.
     *
     * @param path the file path
     * @param index the 0-based line index
     * @return the line, or ""
     * @see #lineAt(String, int)
     * @see #getLine(String, int)
     * @see #slice(String, int, int)
     * @since 1.0.0
     */
    public static String line(String path, int index) {
        List<String> lines = readLines(path);
        if (index < 0 || index >= lines.size()) return "";
        return lines.get(index);
    }

    /**
     * Alias of {@link #line(String, int)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param index the 0-based line index
     * @return the line
     * @see #line(String, int)
     * @since 1.0.0
     */
    public static String lineAt(String path, int index) { return line(path, index); }

    /**
     * Alias of {@link #line(String, int)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param index the 0-based line index
     * @return the line
     * @see #line(String, int)
     * @since 1.0.0
     */
    public static String getLine(String path, int index) { return line(path, index); }

    /**
     * Returns the lines in the range {@code [from, to)} — 0-based, with
     * {@code to} exclusive. Out-of-bounds indices are clamped.
     *
     * @param path the file path (may be null → empty)
     * @param from the start index (inclusive)
     * @param to the end index (exclusive)
     * @return the sliced lines
     * @see #linesBetween(String, int, int)
     * @see #head(String, int)
     * @see #tail(String, int)
     * @since 1.0.0
     */
    public static List<String> slice(String path, int from, int to) {
        List<String> lines = readLines(path);
        int start = Math.max(0, from);
        int end = Math.min(lines.size(), to);
        if (start >= end) return new ArrayList<>();
        return new ArrayList<>(lines.subList(start, end));
    }

    /**
     * Alias of {@link #slice(String, int, int)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param from the start index
     * @param to the end index
     * @return the sliced lines
     * @see #slice(String, int, int)
     * @since 1.0.0
     */
    public static List<String> linesBetween(String path, int from, int to) { return slice(path, from, to); }

    // ============================================================== write

    /**
     * Writes the content to the file, overwriting existing contents.
     * Creates the file if it does not exist.
     *
     * @param path the file path
     * @param content the content to write (null → empty file)
     * @return {@code true} on success
     * @see #save(String, String)
     * @see #saveAll(String, String)
     * @see #writeText(String, String)
     * @see #append(String, String)
     * @since 1.0.0
     */
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
     * Alias of {@link #write(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param content the content to write
     * @return {@code true} on success
     * @see #write(String, String)
     * @since 1.0.0
     */
    public static boolean saveAll(String path, String content) { return write(path, content); }

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
    public static boolean writeText(String path, String content) { return write(path, content); }

    /**
     * Writes a list of lines to the file, overwriting existing contents.
     * A trailing newline is <b>not</b> added.
     *
     * @param path the file path
     * @param lines the lines to write (null → empty file)
     * @return {@code true} on success
     * @see #saveLines(String, List)
     * @see #write(String, String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #writeLines(String, List)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param lines the lines to write
     * @return {@code true} on success
     * @see #writeLines(String, List)
     * @since 1.0.0
     */
    public static boolean saveLines(String path, List<String> lines) { return writeLines(path, lines); }

    /**
     * Alias of {@link #writeLines(String, List)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param l the lines to write
     * @return {@code true} on success
     * @see #writeLines(String, List)
     * @since 1.0.0
     */
    public static boolean writeAllLines(String path, List<String> l) { return writeLines(path, l); }

    /**
     * Appends the content to the file. Creates the file if it does not
     * exist.
     *
     * @param path the file path
     * @param content the content to append
     * @return {@code true} on success
     * @see #add(String, String)
     * @see #addText(String, String)
     * @see #appendLine(String, String)
     * @since 1.0.0
     */
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
    public static boolean add(String path, String content) { return append(path, content); }

    /**
     * Alias of {@link #append(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param c the content to append
     * @return {@code true} on success
     * @see #append(String, String)
     * @since 1.0.0
     */
    public static boolean addText(String path, String c) { return append(path, c); }

    /**
     * Appends a single line followed by the platform's line separator.
     *
     * @param path the file path
     * @param line the line to append
     * @return {@code true} on success
     * @see #addLine(String, String)
     * @see #appendRow(String, String)
     * @see #append(String, String)
     * @since 1.0.0
     */
    public static boolean appendLine(String path, String line) {
        return append(path, (line == null ? "" : line) + System.lineSeparator());
    }

    /**
     * Alias of {@link #appendLine(String, String)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param line the line to append
     * @return {@code true} on success
     * @see #appendLine(String, String)
     * @since 1.0.0
     */
    public static boolean addLine(String path, String line) { return appendLine(path, line); }

    /**
     * Alias of {@link #appendLine(String, String)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param line the line to append
     * @return {@code true} on success
     * @see #appendLine(String, String)
     * @since 1.0.0
     */
    public static boolean appendRow(String path, String line) { return appendLine(path, line); }

    /**
     * Appends each line of the list, each followed by the platform's line
     * separator.
     *
     * @param path the file path
     * @param lines the lines to append
     * @return {@code true} if all appends succeeded
     * @see #addLines(String, List)
     * @since 1.0.0
     */
    public static boolean appendLines(String path, List<String> lines) {
        if (lines == null) return false;
        boolean ok = true;
        for (String line : lines) if (!appendLine(path, line)) ok = false;
        return ok;
    }

    /**
     * Alias of {@link #appendLines(String, List)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param lines the lines to append
     * @return {@code true} if all appends succeeded
     * @see #appendLines(String, List)
     * @since 1.0.0
     */
    public static boolean addLines(String path, List<String> lines) { return appendLines(path, lines); }

    /**
     * Prepends the content to the file. Reads the whole file, then writes
     * {@code content + existing}.
     *
     * @param path the file path
     * @param content the content to prepend
     * @return {@code true} on success
     * @see #prependLine(String, String)
     * @see #addFirst(String, String)
     * @since 1.0.0
     */
    public static boolean prepend(String path, String content) {
        String existing = exists(path) ? readAll(path) : "";
        return write(path, (content == null ? "" : content) + existing);
    }

    /**
     * Prepends a single line followed by the platform's line separator.
     *
     * @param path the file path
     * @param line the line to prepend
     * @return {@code true} on success
     * @see #prepend(String, String)
     * @since 1.0.0
     */
    public static boolean prependLine(String path, String line) {
        return prepend(path, (line == null ? "" : line) + System.lineSeparator());
    }

    /**
     * Alias of {@link #prependLine(String, String)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @param line the line to prepend
     * @return {@code true} on success
     * @see #prependLine(String, String)
     * @since 1.0.0
     */
    public static boolean addFirst(String path, String line) { return prependLine(path, line); }

    // ============================================================== stats

    /**
     * Returns the number of lines in the file.
     *
     * @param path the file path (may be null → 0)
     * @return the line count, or 0
     * @see #lineCount(String)
     * @see #numLines(String)
     * @see #countWords(String)
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

    /**
     * Alias of {@link #countLines(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the line count
     * @see #countLines(String)
     * @since 1.0.0
     */
    public static int numLines(String path) { return countLines(path); }

    /**
     * Returns the number of characters in the file (not bytes — for
     * UTF-8 multibyte content, this differs from
     * {@link #sizeInBytes(String)}).
     *
     * @param path the file path
     * @return the character count
     * @see #charCount(String)
     * @since 1.0.0
     */
    public static int countChars(String path) { return readAll(path).length(); }

    /**
     * Alias of {@link #countChars(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the character count
     * @see #countChars(String)
     * @since 1.0.0
     */
    public static int charCount(String path) { return countChars(path); }

    /**
     * Returns the number of words in the file — words separated by
     * whitespace.
     *
     * @param path the file path
     * @return the word count
     * @see #wordCount(String)
     * @since 1.0.0
     */
    public static int countWords(String path) {
        int total = 0;
        for (String line : readLines(path)) {
            if (line.isBlank()) continue;
            total += line.trim().split("\\s+").length;
        }
        return total;
    }

    /**
     * Alias of {@link #countWords(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the word count
     * @see #countWords(String)
     * @since 1.0.0
     */
    public static int wordCount(String path) { return countWords(path); }

    /**
     * Returns the file size in bytes, or {@code -1} if unreadable.
     *
     * @param path the file path (may be null → -1)
     * @return the size in bytes, or -1
     * @see #size(String)
     * @see #fileSize(String)
     * @see #humanSize(String)
     * @since 1.0.0
     */
    public static long sizeInBytes(String path) {
        if (path == null) return -1;
        try {
            long size = Files.size(Path.of(path));
            Trace.debug("file", "sizeInBytes({}) = {}", path, size);
            return size;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Alias of {@link #sizeInBytes(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the size in bytes
     * @see #sizeInBytes(String)
     * @since 1.0.0
     */
    public static long size(String path) { return sizeInBytes(path); }

    /**
     * Alias of {@link #sizeInBytes(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the size in bytes
     * @see #sizeInBytes(String)
     * @since 1.0.0
     */
    public static long fileSize(String path) { return sizeInBytes(path); }

    /**
     * Returns the file size as a human-readable string — {@code "1.5 KB"},
     * {@code "2.3 MB"}.
     *
     * @param path the file path
     * @return the human-readable size
     * @see chirp.number.Numbers#fileSize(long)
     * @see #sizeInBytes(String)
     * @since 1.0.0
     */
    public static String humanSize(String path) {
        long bytes = sizeInBytes(path);
        return chirp.number.Numbers.fileSize(bytes);
    }

    /**
     * Alias of {@link #humanSize(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the human-readable size
     * @see #humanSize(String)
     * @since 1.0.0
     */
    public static String formattedSize(String path) { return humanSize(path); }

    // ============================================================== search

    /**
     * Returns all lines that contain the given keyword (case-sensitive
     * substring match).
     *
     * @param path the file path (may be null → empty)
     * @param keyword the substring to search for (null → empty)
     * @return the matching lines
     * @see #search(String, String)
     * @see #findInFile(String, String)
     * @see #lineNumbersContaining(String, String)
     * @see #containsText(String, String)
     * @since 1.0.0
     */
    public static List<String> grep(String path, String keyword) {
        List<String> matches = new ArrayList<>();
        if (keyword == null) return matches;
        for (String line : readLines(path)) {
            if (line.contains(keyword)) matches.add(line);
        }
        Trace.debug("file", "grep({}, \"{}\") -> {} matches", path, keyword, matches.size());
        return matches;
    }

    /**
     * Alias of {@link #grep(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param keyword the substring to search for
     * @return the matching lines
     * @see #grep(String, String)
     * @since 1.0.0
     */
    public static List<String> search(String path, String keyword) { return grep(path, keyword); }

    /**
     * Alias of {@link #grep(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param keyword the substring to search for
     * @return the matching lines
     * @see #grep(String, String)
     * @since 1.0.0
     */
    public static List<String> findInFile(String path, String keyword) { return grep(path, keyword); }

    /**
     * Alias of {@link #grep(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param kw the substring to search for
     * @return the matching lines
     * @see #grep(String, String)
     * @since 1.0.0
     */
    public static List<String> matchingLines(String path, String kw) { return grep(path, kw); }

    /**
     * Returns the 1-based line numbers of all lines that contain the
     * given keyword.
     *
     * @param path the file path (may be null → empty)
     * @param keyword the substring to search for (null → empty)
     * @return the matching line numbers (1-based)
     * @see #findLineNumbers(String, String)
     * @see #grep(String, String)
     * @since 1.0.0
     */
    public static List<Integer> lineNumbersContaining(String path, String keyword) {
        List<Integer> result = new ArrayList<>();
        if (keyword == null) return result;
        List<String> lines = readLines(path);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(keyword)) result.add(i + 1);
        }
        return result;
    }

    /**
     * Alias of {@link #lineNumbersContaining(String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param kw the substring to search for
     * @return the matching line numbers
     * @see #lineNumbersContaining(String, String)
     * @since 1.0.0
     */
    public static List<Integer> findLineNumbers(String path, String kw) { return lineNumbersContaining(path, kw); }

    /**
     * Alias of {@link #lineNumbersContaining(String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param k the substring to search for
     * @return the matching line numbers
     * @see #lineNumbersContaining(String, String)
     * @since 1.0.0
     */
    public static List<Integer> matchingLineNumbers(String path, String k) { return lineNumbersContaining(path, k); }

    /**
     * Checks whether the file contains the given keyword anywhere.
     *
     * @param path the file path
     * @param keyword the substring to search for (null → false)
     * @return {@code true} if the keyword is present
     * @see #grep(String, String)
     * @since 1.0.0
     */
    public static boolean containsText(String path, String keyword) {
        if (keyword == null) return false;
        return readAll(path).contains(keyword);
    }

    /**
     * Replaces all occurrences of {@code target} with {@code replacement}
     * in the file, and writes the result back. Returns the updated
     * content.
     *
     * @param path the file path
     * @param target the substring to replace (null or empty → no
     * replacement; returns current content)
     * @param replacement the replacement (null → empty string, effectively
     * removing the target)
     * @return the updated content
     * @see #replace(String, String, String)
     * @see #replaceText(String, String, String)
     * @since 1.0.0
     */
    public static String replaceInFile(String path, String target, String replacement) {
        String content = readAll(path);
        if (target == null || target.isEmpty()) return content;
        String updated = content.replace(target, replacement == null ? "" : replacement);
        write(path, updated);
        return updated;
    }

    /**
     * Alias of {@link #replaceInFile(String, String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param target the substring to replace
     * @param repl the replacement
     * @return the updated content
     * @see #replaceInFile(String, String, String)
     * @since 1.0.0
     */
    public static String replace(String path, String target, String repl) {
        return replaceInFile(path, target, repl);
    }

    /**
     * Alias of {@link #replaceInFile(String, String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param t the substring to replace
     * @param r the replacement
     * @return the updated content
     * @see #replaceInFile(String, String, String)
     * @since 1.0.0
     */
    public static String replaceText(String path, String t, String r) { return replaceInFile(path, t, r); }

    // ============================================================== filesystem

    /**
     * Checks whether the file exists. Returns {@code false} for a
     * {@code null} or empty path.
     *
     * <p>Note: {@code Path.of("")} resolves to the working directory,
     * which always exists. Guarding against empty strings prevents a
     * caller who forgot to supply a path from getting a misleading
     * {@code true}.</p>
     *
     * @param path the file path (may be null → false)
     * @return {@code true} if the file exists
     * @see #existsFile(String)
     * @see FileIO#exists(String)
     * @since 1.0.0
     */
    public static boolean exists(String path) {
        return path != null && !path.isEmpty() && Files.exists(Path.of(path));
    }

    /**
     * Alias of {@link #exists(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return {@code true} if the file exists
     * @see #exists(String)
     * @since 1.0.0
     */
    public static boolean existsFile(String path) { return exists(path); }

    /**
     * Deletes the file. Returns {@code true} if the file existed and was
     * deleted, or if it did not exist (already gone).
     *
     * @param path the file path
     * @return {@code true} on success
     * @see #remove(String)
     * @see FileIO#delete(String)
     * @since 1.0.0
     */
    public static boolean delete(String path) {
        if (path == null) return false;
        try {
            boolean deleted = Files.deleteIfExists(Path.of(path));
            Trace.debug("file", "delete({}) = {}", path, deleted);
            return deleted;
        } catch (IOException e) {
            Logger.error("Cannot delete " + path);
            return false;
        }
    }

    /**
     * Alias of {@link #delete(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return {@code true} on success
     * @see #delete(String)
     * @since 1.0.0
     */
    public static boolean remove(String path) { return delete(path); }

    /**
     * Copies the file to the destination path, overwriting if the
     * destination exists.
     *
     * @param source the source path
     * @param destination the destination path
     * @return {@code true} on success
     * @see #move(String, String)
     * @since 1.0.0
     */
    public static boolean copy(String source, String destination) {
        if (source == null || destination == null) return false;
        try {
            Files.copy(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            Trace.debug("file", "copy({} -> {})", source, destination);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot copy " + source + " → " + destination);
            return false;
        }
    }

    /**
     * Moves (renames) the file to the destination path, overwriting if
     * the destination exists.
     *
     * @param source the source path
     * @param destination the destination path
     * @return {@code true} on success
     * @see #rename(String, String)
     * @see #copy(String, String)
     * @since 1.0.0
     */
    public static boolean move(String source, String destination) {
        if (source == null || destination == null) return false;
        try {
            Files.move(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
            Trace.debug("file", "move({} -> {})", source, destination);
            return true;
        } catch (IOException e) {
            Logger.error("Cannot move " + source + " → " + destination);
            return false;
        }
    }

    /**
     * Alias of {@link #move(String, String)} — see that method for full
     * documentation.
     *
     * @param source the source path
     * @param destination the destination path
     * @return {@code true} on success
     * @see #move(String, String)
     * @since 1.0.0
     */
    public static boolean rename(String source, String destination) { return move(source, destination); }

    /**
     * Returns the names (not full paths) of all entries in the directory
     * — files, subdirectories, and everything else.
     *
     * @param directory the directory path (may be null → empty)
     * @return the entry names
     * @see FileIO#listFiles(String)
     * @see FileIO#listDirectories(String)
     * @since 1.0.0
     */
    public static List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        if (directory == null) return names;
        try (var stream = Files.list(Path.of(directory))) {
            stream.forEach(p -> {
                Path name = p.getFileName();
                if (name != null) names.add(name.toString());
            });
            Trace.debug("file", "listFiles({}) -> {} entries", directory, names.size());
        } catch (IOException e) {
            Logger.error("Cannot list " + directory);
        }
        return names;
    }

    /**
     * Alias of {@link #listFiles(String)} — see that method for full
     * documentation.
     *
     * @param directory the directory path
     * @return the entry names
     * @see #listFiles(String)
     * @since 1.0.0
     */
    public static List<String> ls(String directory) { return listFiles(directory); }

    // ============================================================== path helpers

    /**
     * Returns the extension of the file path (without the dot), or
     * {@code ""} if there is no extension.
     *
     * @param path the file path (may be null → "")
     * @return the extension
     * @see #ext(String)
     * @see #suffix(String)
     * @since 1.0.0
     */
    public static String extension(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return "";
        return path.substring(dot + 1);
    }

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
     * Returns the last component of the path — the file name.
     *
     * @param path the file path (may be null → "")
     * @return the file name
     * @see #name(String)
     * @see #justName(String)
     * @since 1.0.0
     */
    public static String fileName(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? path : path.substring(slash + 1);
    }

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
     * Alias of {@link #fileName(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the file name
     * @see #fileName(String)
     * @since 1.0.0
     */
    public static String justName(String path) { return fileName(path); }

    /**
     * Returns the file name without its extension.
     *
     * @param path the file path (may be null → "")
     * @return the base name
     * @see #stem(String)
     * @see #nameWithoutExtension(String)
     * @since 1.0.0
     */
    public static String baseName(String path) {
        String name = fileName(path);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

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
     * Alias of {@link #baseName(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the base name
     * @see #baseName(String)
     * @since 1.0.0
     */
    public static String nameWithoutExtension(String path) { return baseName(path); }

    /**
     * Returns the directory portion of the path, or {@code ""} if the
     * path has no directory component.
     *
     * @param path the file path (may be null → "")
     * @return the directory portion
     * @see #dirOf(String)
     * @see #folderOf(String)
     * @see #parentOf(String)
     * @since 1.0.0
     */
    public static String directoryOf(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? "" : path.substring(0, slash);
    }

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
    public static String folderOf(String path) { return directoryOf(path); }

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

    // ============================================================== integration bridges

    /**
     * Reads the file as a JSON object. Delegates to
     * {@link JsonFile#readObject(String)}.
     *
     * @param path the file path
     * @return the JSON object, or empty on failure
     * @see JsonFile#readObject(String)
     * @since 1.0.0
     */
    public static chirp.json.JsonObject readJson(String path) {
        return JsonFile.readObject(path);
    }

    /**
     * Writes a JSON value to the file, pretty-printed. Delegates to
     * {@link JsonFile#writePretty(String, chirp.json.JsonValue)}.
     *
     * @param path the file path
     * @param value the JSON value
     * @return {@code true} on success
     * @see JsonFile#writePretty(String, chirp.json.JsonValue)
     * @since 1.0.0
     */
    public static boolean writeJson(String path, chirp.json.JsonValue value) {
        return JsonFile.writePretty(path, value);
    }

    /**
     * Reads the file as CSV rows. Delegates to {@link CsvFile#read(String)}.
     *
     * @param path the file path
     * @return the CSV rows
     * @see CsvFile#read(String)
     * @since 1.0.0
     */
    public static List<List<String>> readCsv(String path) {
        return CsvFile.read(path);
    }

    /**
     * Writes CSV rows to the file. Delegates to
     * {@link CsvFile#write(String, List)}.
     *
     * @param path the file path
     * @param rows the CSV rows
     * @return {@code true} on success
     * @see CsvFile#write(String, List)
     * @since 1.0.0
     */
    public static boolean writeCsv(String path, List<List<String>> rows) {
        return CsvFile.write(path, rows);
    }
}
