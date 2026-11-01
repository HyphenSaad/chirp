package chirp.io;

import java.util.List;

/**
 * Fluent file wrapper — immutable, delegates every operation to
 * {@link TextFileIO}.
 *
 * <p>Useful when the same path is accessed repeatedly and you don't want
 * to repeat the path string in every call. The wrapper holds only the
 * path; every operation re-reads or re-writes the file, so external
 * changes to the file are always visible.</p>
 *
 * <h2>Example — the before and after</h2>
 * <pre>{@code
 * // Without TextFile — path repeated in every call
 * TextFileIO.write("config.txt", "debug=true\n");
 * TextFileIO.append("config.txt", "port=8080\n");
 * List<String> lines = TextFileIO.readLines("config.txt");
 *
 * // With TextFile — path named once
 * TextFile config = TextFile.of("config.txt");
 * config.write("debug=true\n");
 * config.appendLine("port=8080");
 * List<String> lines = config.readLines();
 * }</pre>
 *
 * <h2>Delegation, not caching</h2>
 * <p>Every method on {@code TextFile} delegates to the corresponding
 * {@link TextFileIO} method. There is <b>no</b> in-memory cache — if
 * another process changes the file between two calls, the second call
 * sees the new content. This is the correct behaviour for a
 * teaching-library file wrapper; caching would silently surprise
 * students.</p>
 *
 * <h2>Fluent chains</h2>
 * <pre>{@code
 * TextFile.of("log.txt")
 *     .appendLine("[" + Format.timestamp() + "] Starting")
 *     .appendLine("[" + Format.timestamp() + "] Done");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the static text-file operations, see {@link TextFileIO}. For
 * general file operations, see {@link FileIO}.</p>
 *
 * @see TextFileIO
 * @see FileIO
 * @since 1.0.0
 */
public final class TextFile {

    private final String path;

    private TextFile(String path) {
        this.path = path == null ? "" : path;
    }

    // ============================================================== factories

    /**
     * Opens a fluent wrapper on the path.
     *
     * @param path the file path (may be null — treated as "")
     * @return a new {@code TextFile} wrapping {@code path}
     * @see #open(String)
     * @see #at(String)
     * @see #on(String)
     * @since 1.0.0
     */
    public static TextFile of(String path) { return new TextFile(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile open(String path) { return of(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile at(String path) { return of(path); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return a new {@code TextFile}
     * @see #of(String)
     * @since 1.0.0
     */
    public static TextFile on(String path) { return of(path); }

    // ============================================================== read

    /**
     * Reads the entire file as a string.
     *
     * @return the file content, or empty on failure
     * @see #readAll()
     * @see #readText()
     * @see TextFileIO#readAll(String)
     * @since 1.0.0
     */
    public String read() { return TextFileIO.readAll(path); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the file content
     * @see #read()
     * @since 1.0.0
     */
    public String readAll() { return read(); }

    /**
     * Alias of {@link #read()} — see that method for full documentation.
     *
     * @return the file content
     * @see #read()
     * @since 1.0.0
     */
    public String readText() { return read(); }

    /**
     * Reads the file as a list of lines.
     *
     * @return the lines, or empty on failure
     * @see #lines()
     * @see #getLines()
     * @see TextFileIO#readLines(String)
     * @since 1.0.0
     */
    public List<String> readLines() { return TextFileIO.readLines(path); }

    /**
     * Alias of {@link #readLines()} — see that method for full
     * documentation.
     *
     * @return the lines
     * @see #readLines()
     * @since 1.0.0
     */
    public List<String> lines() { return readLines(); }

    /**
     * Alias of {@link #readLines()} — see that method for full
     * documentation.
     *
     * @return the lines
     * @see #readLines()
     * @since 1.0.0
     */
    public List<String> getLines() { return readLines(); }

    /**
     * Returns the first {@code n} lines.
     *
     * @param n the number of leading lines
     * @return the first {@code n} lines
     * @see #firstN(int)
     * @see #tail(int)
     * @see TextFileIO#head(String, int)
     * @since 1.0.0
     */
    public List<String> head(int n) { return TextFileIO.head(path, n); }

    /**
     * Alias of {@link #head(int)} — see that method for full
     * documentation.
     *
     * @param n the number of leading lines
     * @return the first {@code n} lines
     * @see #head(int)
     * @since 1.0.0
     */
    public List<String> firstN(int n) { return head(n); }

    /**
     * Returns the last {@code n} lines.
     *
     * @param n the number of trailing lines
     * @return the last {@code n} lines
     * @see #lastN(int)
     * @see #head(int)
     * @see TextFileIO#tail(String, int)
     * @since 1.0.0
     */
    public List<String> tail(int n) { return TextFileIO.tail(path, n); }

    /**
     * Alias of {@link #tail(int)} — see that method for full
     * documentation.
     *
     * @param n the number of trailing lines
     * @return the last {@code n} lines
     * @see #tail(int)
     * @since 1.0.0
     */
    public List<String> lastN(int n) { return tail(n); }

    /**
     * Returns the first line of the file.
     *
     * @return the first line, or ""
     * @see #lastLine()
     * @see TextFileIO#firstLine(String)
     * @since 1.0.0
     */
    public String firstLine() { return TextFileIO.firstLine(path); }

    /**
     * Returns the last line of the file.
     *
     * @return the last line, or ""
     * @see #firstLine()
     * @see TextFileIO#lastLine(String)
     * @since 1.0.0
     */
    public String lastLine() { return TextFileIO.lastLine(path); }

    /**
     * Returns the line at the given 0-based index.
     *
     * @param index the 0-based index
     * @return the line, or ""
     * @see TextFileIO#line(String, int)
     * @since 1.0.0
     */
    public String line(int index) { return TextFileIO.line(path, index); }

    /**
     * Returns lines in the range {@code [from, to)}.
     *
     * @param from the start index (inclusive)
     * @param to the end index (exclusive)
     * @return the sliced lines
     * @see TextFileIO#slice(String, int, int)
     * @since 1.0.0
     */
    public List<String> slice(int from, int to) { return TextFileIO.slice(path, from, to); }

    // ============================================================== write

    /**
     * Writes the content to the file, overwriting existing contents.
     *
     * @param content the content to write (null → empty file)
     * @return {@code true} on success
     * @see #save(String)
     * @see #saveAll(String)
     * @see TextFileIO#write(String, String)
     * @since 1.0.0
     */
    public boolean write(String content) { return TextFileIO.write(path, content); }

    /**
     * Alias of {@link #write(String)} — see that method for full
     * documentation.
     *
     * @param content the content to write
     * @return {@code true} on success
     * @see #write(String)
     * @since 1.0.0
     */
    public boolean save(String content) { return write(content); }

    /**
     * Alias of {@link #write(String)} — see that method for full
     * documentation.
     *
     * @param content the content to write
     * @return {@code true} on success
     * @see #write(String)
     * @since 1.0.0
     */
    public boolean saveAll(String content) { return write(content); }

    /**
     * Writes a list of lines to the file, overwriting existing contents.
     *
     * @param lines the lines to write
     * @return {@code true} on success
     * @see #saveLines(List)
     * @see TextFileIO#writeLines(String, List)
     * @since 1.0.0
     */
    public boolean writeLines(List<String> lines) { return TextFileIO.writeLines(path, lines); }

    /**
     * Alias of {@link #writeLines(List)} — see that method for full
     * documentation.
     *
     * @param lines the lines to write
     * @return {@code true} on success
     * @see #writeLines(List)
     * @since 1.0.0
     */
    public boolean saveLines(List<String> lines) { return writeLines(lines); }

    /**
     * Appends the content to the file.
     *
     * @param content the content to append
     * @return {@code true} on success
     * @see #add(String)
     * @see #appendLine(String)
     * @see TextFileIO#append(String, String)
     * @since 1.0.0
     */
    public boolean append(String content) { return TextFileIO.append(path, content); }

    /**
     * Alias of {@link #append(String)} — see that method for full
     * documentation.
     *
     * @param content the content to append
     * @return {@code true} on success
     * @see #append(String)
     * @since 1.0.0
     */
    public boolean add(String content) { return append(content); }

    /**
     * Appends a single line followed by the platform's line separator.
     *
     * @param line the line to append
     * @return {@code true} on success
     * @see #addLine(String)
     * @see TextFileIO#appendLine(String, String)
     * @since 1.0.0
     */
    public boolean appendLine(String line) { return TextFileIO.appendLine(path, line); }

    /**
     * Alias of {@link #appendLine(String)} — see that method for full
     * documentation.
     *
     * @param line the line to append
     * @return {@code true} on success
     * @see #appendLine(String)
     * @since 1.0.0
     */
    public boolean addLine(String line) { return appendLine(line); }

    /**
     * Appends each line of the list.
     *
     * @param lines the lines to append
     * @return {@code true} if all succeeded
     * @see TextFileIO#appendLines(String, List)
     * @since 1.0.0
     */
    public boolean appendLines(List<String> lines) { return TextFileIO.appendLines(path, lines); }

    /**
     * Prepends the content to the file.
     *
     * @param content the content to prepend
     * @return {@code true} on success
     * @see TextFileIO#prepend(String, String)
     * @since 1.0.0
     */
    public boolean prepend(String content) { return TextFileIO.prepend(path, content); }

    /**
     * Prepends a single line.
     *
     * @param line the line to prepend
     * @return {@code true} on success
     * @see TextFileIO#prependLine(String, String)
     * @since 1.0.0
     */
    public boolean prependLine(String line) { return TextFileIO.prependLine(path, line); }

    // ============================================================== stats

    /**
     * Returns the number of lines in the file.
     *
     * @return the line count
     * @see #lineCount()
     * @see #numLines()
     * @see TextFileIO#countLines(String)
     * @since 1.0.0
     */
    public int countLines() { return TextFileIO.countLines(path); }

    /**
     * Alias of {@link #countLines()} — see that method for full
     * documentation.
     *
     * @return the line count
     * @see #countLines()
     * @since 1.0.0
     */
    public int lineCount() { return countLines(); }

    /**
     * Alias of {@link #countLines()} — see that method for full
     * documentation.
     *
     * @return the line count
     * @see #countLines()
     * @since 1.0.0
     */
    public int numLines() { return countLines(); }

    /**
     * Returns the number of characters in the file.
     *
     * @return the character count
     * @see #charCount()
     * @see TextFileIO#countChars(String)
     * @since 1.0.0
     */
    public int countChars() { return TextFileIO.countChars(path); }

    /**
     * Alias of {@link #countChars()} — see that method for full
     * documentation.
     *
     * @return the character count
     * @see #countChars()
     * @since 1.0.0
     */
    public int charCount() { return countChars(); }

    /**
     * Returns the number of words in the file.
     *
     * @return the word count
     * @see #wordCount()
     * @see TextFileIO#countWords(String)
     * @since 1.0.0
     */
    public int countWords() { return TextFileIO.countWords(path); }

    /**
     * Alias of {@link #countWords()} — see that method for full
     * documentation.
     *
     * @return the word count
     * @see #countWords()
     * @since 1.0.0
     */
    public int wordCount() { return countWords(); }

    /**
     * Returns the file size in bytes, or {@code -1} if unreadable.
     *
     * @return the size in bytes
     * @see #size()
     * @see #humanSize()
     * @see TextFileIO#sizeInBytes(String)
     * @since 1.0.0
     */
    public long sizeInBytes() { return TextFileIO.sizeInBytes(path); }

    /**
     * Alias of {@link #sizeInBytes()} — see that method for full
     * documentation.
     *
     * @return the size in bytes
     * @see #sizeInBytes()
     * @since 1.0.0
     */
    public long size() { return sizeInBytes(); }

    /**
     * Returns the file size as a human-readable string — {@code "1.5 KB"}.
     *
     * @return the human-readable size
     * @see TextFileIO#humanSize(String)
     * @since 1.0.0
     */
    public String humanSize() { return TextFileIO.humanSize(path); }

    /**
     * Alias of {@link #humanSize()} — see that method for full
     * documentation.
     *
     * @return the human-readable size
     * @see #humanSize()
     * @since 1.0.0
     */
    public String formattedSize() { return humanSize(); }

    // ============================================================== search

    /**
     * Returns all lines that contain the given keyword.
     *
     * @param keyword the substring to search for
     * @return the matching lines
     * @see #search(String)
     * @see #findInFile(String)
     * @see TextFileIO#grep(String, String)
     * @since 1.0.0
     */
    public List<String> grep(String keyword) { return TextFileIO.grep(path, keyword); }

    /**
     * Alias of {@link #grep(String)} — see that method for full
     * documentation.
     *
     * @param keyword the substring to search for
     * @return the matching lines
     * @see #grep(String)
     * @since 1.0.0
     */
    public List<String> search(String keyword) { return grep(keyword); }

    /**
     * Alias of {@link #grep(String)} — see that method for full
     * documentation.
     *
     * @param kw the substring to search for
     * @return the matching lines
     * @see #grep(String)
     * @since 1.0.0
     */
    public List<String> findInFile(String kw) { return grep(kw); }

    /**
     * Returns the 1-based line numbers of all lines that contain the
     * given keyword.
     *
     * @param keyword the substring to search for
     * @return the matching line numbers
     * @see #findLineNumbers(String)
     * @see TextFileIO#lineNumbersContaining(String, String)
     * @since 1.0.0
     */
    public List<Integer> lineNumbersContaining(String keyword) {
        return TextFileIO.lineNumbersContaining(path, keyword);
    }

    /**
     * Alias of {@link #lineNumbersContaining(String)} — see that method
     * for full documentation.
     *
     * @param kw the substring to search for
     * @return the matching line numbers
     * @see #lineNumbersContaining(String)
     * @since 1.0.0
     */
    public List<Integer> findLineNumbers(String kw) { return lineNumbersContaining(kw); }

    /**
     * Checks whether the file contains the given keyword.
     *
     * @param keyword the substring to search for
     * @return {@code true} if present
     * @see #containsText(String)
     * @see TextFileIO#containsText(String, String)
     * @since 1.0.0
     */
    public boolean contains(String keyword) { return TextFileIO.containsText(path, keyword); }

    /**
     * Alias of {@link #contains(String)} — see that method for full
     * documentation.
     *
     * @param keyword the substring to search for
     * @return {@code true} if present
     * @see #contains(String)
     * @since 1.0.0
     */
    public boolean containsText(String keyword) { return contains(keyword); }

    /**
     * Replaces all occurrences of {@code target} with {@code replacement}
     * and writes the result back.
     *
     * @param target the substring to replace
     * @param replacement the replacement
     * @return the updated content
     * @see TextFileIO#replaceInFile(String, String, String)
     * @since 1.0.0
     */
    public String replace(String target, String replacement) {
        return TextFileIO.replaceInFile(path, target, replacement);
    }

    // ============================================================== filesystem

    /**
     * Checks whether the file exists.
     *
     * @return {@code true} if the file exists
     * @see TextFileIO#exists(String)
     * @since 1.0.0
     */
    public boolean exists() { return TextFileIO.exists(path); }

    /**
     * Deletes the file.
     *
     * @return {@code true} on success
     * @see #remove()
     * @see TextFileIO#delete(String)
     * @since 1.0.0
     */
    public boolean delete() { return TextFileIO.delete(path); }

    /**
     * Alias of {@link #delete()} — see that method for full
     * documentation.
     *
     * @return {@code true} on success
     * @see #delete()
     * @since 1.0.0
     */
    public boolean remove() { return delete(); }

    /**
     * Copies the file to the destination path.
     *
     * @param destination the destination path
     * @return {@code true} on success
     * @see TextFileIO#copy(String, String)
     * @since 1.0.0
     */
    public boolean copy(String destination) { return TextFileIO.copy(path, destination); }

    /**
     * Moves the file to the destination path.
     *
     * @param destination the destination path
     * @return {@code true} on success
     * @see #rename(String)
     * @see TextFileIO#move(String, String)
     * @since 1.0.0
     */
    public boolean move(String destination) { return TextFileIO.move(path, destination); }

    /**
     * Alias of {@link #move(String)} — see that method for full
     * documentation.
     *
     * @param destination the destination path
     * @return {@code true} on success
     * @see #move(String)
     * @since 1.0.0
     */
    public boolean rename(String destination) { return move(destination); }

    // ============================================================== info

    /**
     * Returns the full path stored in this wrapper.
     *
     * @return the path
     * @see #getPath()
     * @since 1.0.0
     */
    public String path() { return path; }

    /**
     * Alias of {@link #path()} — see that method for full documentation.
     *
     * @return the path
     * @see #path()
     * @since 1.0.0
     */
    public String getPath() { return path; }

    /**
     * Returns the file name (last path component).
     *
     * @return the file name
     * @see #fileName()
     * @see TextFileIO#fileName(String)
     * @since 1.0.0
     */
    public String name() { return TextFileIO.fileName(path); }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the file name
     * @see #name()
     * @since 1.0.0
     */
    public String fileName() { return name(); }

    /**
     * Returns the file name without extension.
     *
     * @return the base name
     * @see #stem()
     * @see TextFileIO#baseName(String)
     * @since 1.0.0
     */
    public String baseName() { return TextFileIO.baseName(path); }

    /**
     * Alias of {@link #baseName()} — see that method for full
     * documentation.
     *
     * @return the base name
     * @see #baseName()
     * @since 1.0.0
     */
    public String stem() { return baseName(); }

    /**
     * Returns the file extension (without the dot).
     *
     * @return the extension
     * @see #ext()
     * @see TextFileIO#extension(String)
     * @since 1.0.0
     */
    public String extension() { return TextFileIO.extension(path); }

    /**
     * Alias of {@link #extension()} — see that method for full
     * documentation.
     *
     * @return the extension
     * @see #extension()
     * @since 1.0.0
     */
    public String ext() { return extension(); }

    /**
     * Returns the directory portion of the path.
     *
     * @return the directory
     * @see #parent()
     * @see TextFileIO#directoryOf(String)
     * @since 1.0.0
     */
    public String directory() { return TextFileIO.directoryOf(path); }

    /**
     * Alias of {@link #directory()} — see that method for full
     * documentation.
     *
     * @return the directory
     * @see #directory()
     * @since 1.0.0
     */
    public String parent() { return directory(); }

    // ============================================================== integration

    /**
     * Reads the file as a JSON object. Delegates to
     * {@link TextFileIO#readJson(String)}.
     *
     * @return the JSON object, or empty on failure
     * @see TextFileIO#readJson(String)
     * @since 1.0.0
     */
    public chirp.json.JsonObject asJson() { return TextFileIO.readJson(path); }

    /**
     * Reads the file as CSV rows. Delegates to
     * {@link TextFileIO#readCsv(String)}.
     *
     * @return the CSV rows
     * @see TextFileIO#readCsv(String)
     * @since 1.0.0
     */
    public List<List<String>> asCsv() { return TextFileIO.readCsv(path); }

    /**
     * Writes a JSON value to the file, pretty-printed.
     *
     * @param value the JSON value
     * @return {@code true} on success
     * @see TextFileIO#writeJson(String, chirp.json.JsonValue)
     * @since 1.0.0
     */
    public boolean writeJson(chirp.json.JsonValue value) { return TextFileIO.writeJson(path, value); }

    /**
     * Writes CSV rows to the file.
     *
     * @param rows the CSV rows
     * @return {@code true} on success
     * @see TextFileIO#writeCsv(String, List)
     * @since 1.0.0
     */
    public boolean writeCsv(List<List<String>> rows) { return TextFileIO.writeCsv(path, rows); }

    /**
     * Returns a short representation — {@code "TextFile[path]"}.
     *
     * @return the representation
     * @since 1.0.0
     */
    @Override
    public String toString() { return "TextFile[" + path + "]"; }
}
