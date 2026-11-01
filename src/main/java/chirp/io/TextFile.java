package chirp.io;

import java.util.List;

/**
 * Fluent file wrapper — immutable, delegates every operation to
 * {@link TextFileIO}. Useful when the same path is accessed repeatedly.
 *
 * <pre>{@code
 * TextFile.of("config.txt")
 *     .append("debug=true")
 *     .append("port=8080");
 *
 * TextFile.of("config.txt")
 *     .readLines()
 *     .stream()
 *     .filter(l -> l.startsWith("debug="))
 *     .forEach(chirp.console.Console::println);
 * }</pre>
 */
public final class TextFile {

    private final String path;

    private TextFile(String path) {
        this.path = path == null ? "" : path;
    }

    // ============================================================== factories

    public static TextFile of(String path) { return new TextFile(path); }
    public static TextFile open(String path) { return of(path); }
    public static TextFile at(String path) { return of(path); }
    public static TextFile on(String path) { return of(path); }

    // ============================================================== read

    public String read() { return TextFileIO.readAll(path); }
    public String readAll() { return read(); }
    public String readText() { return read(); }

    public List<String> readLines() { return TextFileIO.readLines(path); }
    public List<String> lines() { return readLines(); }
    public List<String> getLines() { return readLines(); }

    public List<String> head(int n) { return TextFileIO.head(path, n); }
    public List<String> firstN(int n) { return head(n); }
    public List<String> tail(int n) { return TextFileIO.tail(path, n); }
    public List<String> lastN(int n) { return tail(n); }

    public String firstLine() { return TextFileIO.firstLine(path); }
    public String lastLine() { return TextFileIO.lastLine(path); }

    public String line(int index) { return TextFileIO.line(path, index); }
    public List<String> slice(int from, int to) { return TextFileIO.slice(path, from, to); }

    // ============================================================== write

    public boolean write(String content) { return TextFileIO.write(path, content); }
    public boolean save(String content) { return write(content); }
    public boolean saveAll(String content) { return write(content); }

    public boolean writeLines(List<String> lines) { return TextFileIO.writeLines(path, lines); }
    public boolean saveLines(List<String> lines) { return writeLines(lines); }

    public boolean append(String content) { return TextFileIO.append(path, content); }
    public boolean add(String content) { return append(content); }

    public boolean appendLine(String line) { return TextFileIO.appendLine(path, line); }
    public boolean addLine(String line) { return appendLine(line); }

    public boolean appendLines(List<String> lines) { return TextFileIO.appendLines(path, lines); }
    public boolean prepend(String content) { return TextFileIO.prepend(path, content); }
    public boolean prependLine(String line) { return TextFileIO.prependLine(path, line); }

    // ============================================================== stats

    public int countLines() { return TextFileIO.countLines(path); }
    public int lineCount() { return countLines(); }
    public int numLines() { return countLines(); }
    public int countChars() { return TextFileIO.countChars(path); }
    public int charCount() { return countChars(); }
    public int countWords() { return TextFileIO.countWords(path); }
    public int wordCount() { return countWords(); }

    public long sizeInBytes() { return TextFileIO.sizeInBytes(path); }
    public long size() { return sizeInBytes(); }
    public String humanSize() { return TextFileIO.humanSize(path); }
    public String formattedSize() { return humanSize(); }

    // ============================================================== search

    public List<String> grep(String keyword) { return TextFileIO.grep(path, keyword); }
    public List<String> search(String keyword) { return grep(keyword); }
    public List<String> findInFile(String kw) { return grep(kw); }

    public List<Integer> lineNumbersContaining(String keyword) {
        return TextFileIO.lineNumbersContaining(path, keyword);
    }
    public List<Integer> findLineNumbers(String kw) { return lineNumbersContaining(kw); }

    public boolean contains(String keyword) { return TextFileIO.containsText(path, keyword); }
    public boolean containsText(String keyword) { return contains(keyword); }

    public String replace(String target, String replacement) {
        return TextFileIO.replaceInFile(path, target, replacement);
    }

    // ============================================================== filesystem

    public boolean exists() { return TextFileIO.exists(path); }
    public boolean delete() { return TextFileIO.delete(path); }
    public boolean remove() { return delete(); }

    public boolean copy(String destination) { return TextFileIO.copy(path, destination); }
    public boolean move(String destination) { return TextFileIO.move(path, destination); }
    public boolean rename(String destination) { return move(destination); }

    // ============================================================== info

    public String path() { return path; }
    public String getPath() { return path; }
    public String name() { return TextFileIO.fileName(path); }
    public String fileName() { return name(); }
    public String baseName() { return TextFileIO.baseName(path); }
    public String stem() { return baseName(); }
    public String extension() { return TextFileIO.extension(path); }
    public String ext() { return extension(); }
    public String directory() { return TextFileIO.directoryOf(path); }
    public String parent() { return directory(); }

    // ============================================================== integration

    public chirp.json.JsonObject asJson() { return TextFileIO.readJson(path); }
    public List<List<String>> asCsv() { return TextFileIO.readCsv(path); }

    public boolean writeJson(chirp.json.JsonValue value) { return TextFileIO.writeJson(path, value); }
    public boolean writeCsv(List<List<String>> rows) { return TextFileIO.writeCsv(path, rows); }

    @Override
    public String toString() { return "TextFile[" + path + "]"; }
}
