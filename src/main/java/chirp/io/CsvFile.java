package chirp.io;

import chirp.internal.Logger;
import chirp.internal.Trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * CSV file operations with full RFC 4180 quoting support and a typed
 * table view.
 *
 * <p>Handles the tricky parts of CSV that most "split on comma" code gets
 * wrong: quoted fields containing commas, escaped quotes inside quoted
 * fields, and embedded newlines. Round-tripping any of these through
 * {@link #write(String, List)} and {@link #read(String)} produces the
 * same data that went in.</p>
 *
 * <h2>Four ways to read a CSV file</h2>
 * <table border="1">
 *   <caption>Reading a CSV file — pick the shape that matches your use</caption>
 *   <tr><th>Method</th><th>Returns</th><th>When to use</th></tr>
 *   <tr><td>{@link #read(String)}</td><td>{@code List<List<String>>}</td>
 *       <td>Positional access by column index</td></tr>
 *   <tr><td>{@link #readAsRecords(String)}</td>
 *       <td>{@code List<Map<String, String>>}</td>
 *       <td>Access by column name</td></tr>
 *   <tr><td>{@link #readHeaders(String)}</td>
 *       <td>{@code List<String>}</td>
 *       <td>Just the header row</td></tr>
 *   <tr><td>{@link #table(String)}</td><td>{@link CsvTable}</td>
 *       <td>Both headers and rows, in one typed record</td></tr>
 * </table>
 *
 * <h2>RFC 4180 quoting rules</h2>
 * <ul>
 *   <li>A field containing a comma, a quote, or a newline is wrapped in
 *       double quotes</li>
 *   <li>A literal quote inside a quoted field is escaped as {@code ""}</li>
 *   <li>A leading space is preserved as-is (not trimmed)</li>
 * </ul>
 * <p>{@link #write(String, List)} applies these rules automatically;
 * {@link #read(String)} inverts them.</p>
 *
 * <h2>Example — read and use a table</h2>
 * <pre>{@code
 * CsvFile.CsvTable t = CsvFile.table("students.csv");
 *
 * println("Columns: " + t.headers());
 * println("Rows   : " + t.rowCount());
 *
 * // Access by column name
 * List<String> names = t.column("name");
 *
 * // Filter, then write the filtered version back
 * CsvTable topStudents = t.filter(row -> Integer.parseInt(row.get("score")) >= 90);
 * CsvFile.writeTable("top.csv", topStudents);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For general file operations, see {@link FileIO}. For text files,
 * see {@link TextFileIO}. For JSON, see {@link JsonFile}.</p>
 *
 * @see TextFileIO
 * @see JsonFile
 * @see FileIO
 * @since 1.0.0
 */
public final class CsvFile {

    /**
     * Key enum for the key-value write overload — controls the field
     * delimiter.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** The delimiter character — {@code ','} by default. */
        DELIMITER,
        /** Reserved for future use. */
        HEADERS,
        /** Reserved for future use. */
        QUOTE
    }

    /**
     * A CSV table — headers plus typed records, all in one immutable
     * record.
     *
     * <p>Both fields are defensive-copied in the canonical constructor:
     * {@code headers} becomes an immutable list, {@code records} becomes
     * an immutable list of immutable {@link LinkedHashMap}s (order
     * preserved). Callers cannot mutate the table by keeping a reference
     * to the lists/maps they passed to the constructor.</p>
     *
     * <p>Unlike a raw {@code List<Map<String, String>>}, {@code CsvTable}
     * carries the header order, so a round-trip write preserves the
     * original column ordering even if the maps were assembled in a
     * different order.</p>
     *
     * @param headers the column headers, in order (may be null → empty)
     * @param records the row records (may be null → empty)
     * @see CsvFile#table(String)
     * @since 1.0.0
     */
    public record CsvTable(List<String> headers, List<Map<String, String>> records) {

        public CsvTable {
            headers = headers == null ? List.of() : List.copyOf(headers);
            if (records == null) {
                records = List.of();
            } else {
                List<Map<String, String>> safe = new ArrayList<>(records.size());
                for (Map<String, String> row : records) {
                    safe.add(row == null
                            ? Map.of()
                            : Collections.unmodifiableMap(new LinkedHashMap<>(row)));
                }
                records = Collections.unmodifiableList(safe);
            }
        }

        /**
         * Returns the number of data rows (excludes the header row).
         *
         * @return the row count
         * @see #columnCount()
         * @see #isEmpty()
         * @since 1.0.0
         */
        public int rowCount() { return records.size(); }

        /**
         * Returns the number of columns (from the header row).
         *
         * @return the column count
         * @see #rowCount()
         * @since 1.0.0
         */
        public int columnCount() { return headers.size(); }

        /**
         * Checks whether the table has no data rows.
         *
         * @return {@code true} if there are no records
         * @see #rowCount()
         * @since 1.0.0
         */
        public boolean isEmpty() { return records.isEmpty(); }

        /**
         * Returns all values from a single column, in row order.
         *
         * <p>Missing values (when a row does not contain the column key)
         * are returned as empty string, not null.</p>
         *
         * <pre>{@code
         * List<String> names = table.column("name");
         * }</pre>
         *
         * @param name the column header
         * @return the column values, or empty if the column does not exist
         * @since 1.0.0
         */
        public List<String> column(String name) {
            List<String> result = new ArrayList<>();
            for (Map<String, String> row : records) {
                result.add(row.getOrDefault(name, ""));
            }
            return result;
        }

        /**
         * Returns a new table containing only the rows that pass the
         * predicate.
         *
         * @param predicate the row filter
         * @return a new filtered table
         * @since 1.0.0
         */
        public CsvTable filter(Predicate<Map<String, String>> predicate) {
            List<Map<String, String>> kept = new ArrayList<>();
            for (Map<String, String> row : records) if (predicate.test(row)) kept.add(row);
            return new CsvTable(headers, kept);
        }

        /**
         * Returns a new table with rows sorted lexicographically by the
         * given column.
         *
         * @param column the column to sort by
         * @return a new sorted table
         * @since 1.0.0
         */
        public CsvTable sortBy(String column) {
            List<Map<String, String>> sorted = new ArrayList<>(records);
            sorted.sort((a, b) -> {
                String va = a.getOrDefault(column, "");
                String vb = b.getOrDefault(column, "");
                return va.compareTo(vb);
            });
            return new CsvTable(headers, sorted);
        }
    }

    private CsvFile() {}

    // ============================================================== read

    /**
     * Reads the CSV file as a list of rows, using {@code ','} as the
     * delimiter.
     *
     * <p>Returns the header row as the first element, followed by the
     * data rows. Empty rows are preserved as single-empty-cell rows.</p>
     *
     * @param path the file path (may be null → empty)
     * @return the rows, or empty on failure
     * @see #read(String, char)
     * @see #load(String)
     * @see #readCsv(String)
     * @see #parseFile(String)
     * @see #table(String)
     * @since 1.0.0
     */
    public static List<List<String>> read(String path) {
        return read(path, ',');
    }

    /**
     * Alias of {@link #read(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the rows
     * @see #read(String)
     * @since 1.0.0
     */
    public static List<List<String>> load(String path) { return read(path); }

    /**
     * Alias of {@link #read(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the rows
     * @see #read(String)
     * @since 1.0.0
     */
    public static List<List<String>> readCsv(String path) { return read(path); }

    /**
     * Alias of {@link #read(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the rows
     * @see #read(String)
     * @since 1.0.0
     */
    public static List<List<String>> parseFile(String path) { return read(path); }

    /**
     * Reads the CSV file with a custom delimiter — tab-separated values
     * ({@code '\t'}), pipe-separated ({@code '|'}), and so on.
     *
     * <p>Quoting rules apply regardless of delimiter — a {@code '\t'}
     * inside a quoted field is preserved as a literal tab character.</p>
     *
     * @param path the file path (may be null → empty)
     * @param delimiter the field separator character
     * @return the rows, or empty on failure
     * @see #read(String)
     * @since 1.0.0
     */
    public static List<List<String>> read(String path, char delimiter) {
        Trace.enter("csv", "read", path, "delimiter='" + delimiter + "'");
        List<List<String>> rows = new ArrayList<>();
        String content = TextFileIO.readAll(path);
        if (content.isEmpty()) {
            Trace.debug("csv", "read: empty file {}", path);
            return rows;
        }
        for (List<String> row : parse(content, delimiter)) {
            rows.add(row);
        }
        Trace.debug("csv", "read: {} rows from {}", rows.size(), path);
        Trace.exit("csv", "read", rows.size() + " rows");
        return rows;
    }

    /**
     * Reads the CSV file and returns only the data rows (skips the
     * header). Returns empty for a null or empty file.
     *
     * @param path the file path (may be null → empty)
     * @return the data rows without the header
     * @see #readWithHeader(String)
     * @see #bodyRows(String)
     * @see #readHeaders(String)
     * @since 1.0.0
     */
    public static List<List<String>> readWithHeaders(String path) {
        List<List<String>> rows = read(path);
        if (rows.isEmpty()) return rows;
        return new ArrayList<>(rows.subList(1, rows.size()));
    }

    /**
     * Alias of {@link #readWithHeaders(String)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @return the data rows without the header
     * @see #readWithHeaders(String)
     * @since 1.0.0
     */
    public static List<List<String>> readWithHeader(String path) { return readWithHeaders(path); }

    /**
     * Alias of {@link #readWithHeaders(String)} — see that method for
     * full documentation.
     *
     * @param path the file path
     * @return the data rows without the header
     * @see #readWithHeaders(String)
     * @since 1.0.0
     */
    public static List<List<String>> bodyRows(String path) { return readWithHeaders(path); }

    /**
     * Reads only the header row from the CSV file.
     *
     * @param path the file path (may be null → empty)
     * @return the header row, or empty for null / empty / unreadable
     * @see #headers(String)
     * @see #columns(String)
     * @see #getHeaders(String)
     * @see #readWithHeaders(String)
     * @since 1.0.0
     */
    public static List<String> readHeaders(String path) {
        List<List<String>> rows = read(path);
        return rows.isEmpty() ? new ArrayList<>() : rows.get(0);
    }

    /**
     * Alias of {@link #readHeaders(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the header row
     * @see #readHeaders(String)
     * @since 1.0.0
     */
    public static List<String> headers(String path) { return readHeaders(path); }

    /**
     * Alias of {@link #readHeaders(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the header row
     * @see #readHeaders(String)
     * @since 1.0.0
     */
    public static List<String> columns(String path) { return readHeaders(path); }

    /**
     * Alias of {@link #readHeaders(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the header row
     * @see #readHeaders(String)
     * @since 1.0.0
     */
    public static List<String> getHeaders(String path) { return readHeaders(path); }

    /**
     * Reads the CSV file as a list of row maps, keyed by column name.
     *
     * <p>Missing cells (when a row is shorter than the header) are filled
     * with empty string. Extra cells (when a row is longer than the
     * header) are silently dropped.</p>
     *
     * @param path the file path (may be null → empty)
     * @return the row maps, or empty on failure
     * @see #records(String)
     * @see #asRecords(String)
     * @see #readRecords(String)
     * @see #table(String)
     * @since 1.0.0
     */
    public static List<Map<String, String>> readAsRecords(String path) {
        List<List<String>> rows = read(path);
        if (rows.isEmpty()) return new ArrayList<>();

        List<String> headers = rows.get(0);
        List<Map<String, String>> records = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> record = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                record.put(headers.get(c), c < row.size() ? row.get(c) : "");
            }
            records.add(record);
        }
        Trace.debug("csv", "readAsRecords: {} records, {} headers", records.size(), headers.size());
        return records;
    }

    /**
     * Alias of {@link #readAsRecords(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the row maps
     * @see #readAsRecords(String)
     * @since 1.0.0
     */
    public static List<Map<String, String>> records(String path) { return readAsRecords(path); }

    /**
     * Alias of {@link #readAsRecords(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the row maps
     * @see #readAsRecords(String)
     * @since 1.0.0
     */
    public static List<Map<String, String>> asRecords(String path) { return readAsRecords(path); }

    /**
     * Alias of {@link #readAsRecords(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the row maps
     * @see #readAsRecords(String)
     * @since 1.0.0
     */
    public static List<Map<String, String>> readRecords(String path) { return readAsRecords(path); }

    /**
     * Reads a CSV file as a typed {@link CsvTable} — headers plus records
     * in one call.
     *
     * <p>Preferred over {@link #readHeaders(String)} + {@link #readAsRecords(String)}
     * when you need both, because the two are guaranteed to be consistent.</p>
     *
     * @param path the file path (may be null → empty table)
     * @return the table, or empty on failure
     * @see CsvTable
     * @see #asTable(String)
     * @since 1.0.0
     */
    public static CsvTable table(String path) {
        List<List<String>> rows = read(path);
        if (rows.isEmpty()) return new CsvTable(new ArrayList<>(), new ArrayList<>());

        List<String> headers = rows.get(0);
        List<Map<String, String>> records = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> record = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                record.put(headers.get(c), c < row.size() ? row.get(c) : "");
            }
            records.add(record);
        }
        return new CsvTable(headers, records);
    }

    /**
     * Alias of {@link #table(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the table
     * @see #table(String)
     * @since 1.0.0
     */
    public static CsvTable asTable(String path) { return table(path); }

    /**
     * Returns a single column's values from across all rows.
     *
     * @param path the file path
     * @param name the column header
     * @return the column values, or empty on failure
     * @see CsvTable#column(String)
     * @since 1.0.0
     */
    public static List<String> column(String path, String name) {
        CsvTable t = table(path);
        return t.column(name);
    }

    /**
     * Returns the number of data rows (excludes the header).
     *
     * @param path the file path
     * @return the row count, or 0 on failure
     * @see #columnCount(String)
     * @since 1.0.0
     */
    public static int rowCount(String path) {
        List<List<String>> rows = read(path);
        return Math.max(0, rows.size() - 1);
    }

    /**
     * Returns the number of columns (from the header row).
     *
     * @param path the file path
     * @return the column count, or 0 on failure
     * @see #rowCount(String)
     * @since 1.0.0
     */
    public static int columnCount(String path) {
        List<String> headers = readHeaders(path);
        return headers.size();
    }

    // ============================================================== write

    /**
     * Writes the rows to the CSV file, using {@code ','} as the
     * delimiter.
     *
     * <p>Fields containing commas, quotes, or newlines are automatically
     * quoted and escaped. Round-trip through {@link #read(String)}
     * produces the same data that went in.</p>
     *
     * @param path the file path
     * @param rows the rows to write
     * @return {@code true} on success
     * @see #write(String, List, char)
     * @see #save(String, List)
     * @see #writeCsv(String, List)
     * @see #saveCsv(String, List)
     * @see #writeTable(String, CsvTable)
     * @since 1.0.0
     */
    public static boolean write(String path, List<List<String>> rows) {
        return write(path, rows, ',');
    }

    /**
     * Alias of {@link #write(String, List)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @param rows the rows to write
     * @return {@code true} on success
     * @see #write(String, List)
     * @since 1.0.0
     */
    public static boolean save(String path, List<List<String>> rows) { return write(path, rows); }

    /**
     * Alias of {@link #write(String, List)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @param rows the rows to write
     * @return {@code true} on success
     * @see #write(String, List)
     * @since 1.0.0
     */
    public static boolean writeCsv(String path, List<List<String>> rows) { return write(path, rows); }

    /**
     * Alias of {@link #write(String, List)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @param rows the rows to write
     * @return {@code true} on success
     * @see #write(String, List)
     * @since 1.0.0
     */
    public static boolean saveCsv(String path, List<List<String>> rows) { return write(path, rows); }

    /**
     * Writes the rows to the CSV file with a custom delimiter.
     *
     * @param path the file path
     * @param rows the rows to write
     * @param delimiter the field separator character
     * @return {@code true} on success
     * @see #write(String, List)
     * @since 1.0.0
     */
    public static boolean write(String path, List<List<String>> rows, char delimiter) {
        Trace.enter("csv", "write", path, "rows=" + (rows == null ? 0 : rows.size()));
        StringBuilder sb = new StringBuilder();
        if (rows != null) {
            for (List<String> row : rows) {
                if (row == null) continue;
                for (int i = 0; i < row.size(); i++) {
                    if (i > 0) sb.append(delimiter);
                    sb.append(escape(row.get(i), delimiter));
                }
                sb.append('\n');
            }
        }
        boolean success = TextFileIO.write(path, sb.toString());
        Trace.exit("csv", "write", success);
        return success;
    }

    /**
     * Writes the rows to the CSV file with key-value options — allows a
     * custom delimiter via {@link Key#DELIMITER}.
     *
     * <pre>{@code
     * CsvFile.write("data.tsv", rows, CsvFile.Key.DELIMITER, '\t');
     * }</pre>
     *
     * @param path the file path
     * @param rows the rows to write
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return {@code true} on success
     * @see #write(String, List, char)
     * @since 1.0.0
     */
    public static boolean write(String path, List<List<String>> rows, Object... keyValues) {
        char delimiter = ',';
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.DELIMITER && keyValues[i + 1] instanceof Character c) {
                    delimiter = c;
                }
            }
        }
        return write(path, rows, delimiter);
    }

    /**
     * Alias of {@link #write(String, List, Object...)} — see that method
     * for full documentation.
     *
     * @param path the file path
     * @param rows the rows to write
     * @param keyValues alternating key-value pairs
     * @return {@code true} on success
     * @see #write(String, List, Object...)
     * @since 1.0.0
     */
    public static boolean save(String path, List<List<String>> rows, Object... keyValues) {
        return write(path, rows, keyValues);
    }

    /**
     * Appends a single row to the CSV file. Creates the file if it does
     * not exist — you'll need to write the header row first yourself if
     * you want a header.
     *
     * @param path the file path
     * @param row the row to append
     * @return {@code true} on success
     * @see #append(String, List, char)
     * @see #addRow(String, List)
     * @see #appendRow(String, List)
     * @since 1.0.0
     */
    public static boolean append(String path, List<String> row) {
        return append(path, row, ',');
    }

    /**
     * Alias of {@link #append(String, List)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param row the row to append
     * @return {@code true} on success
     * @see #append(String, List)
     * @since 1.0.0
     */
    public static boolean addRow(String path, List<String> row) { return append(path, row); }

    /**
     * Alias of {@link #append(String, List)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param row the row to append
     * @return {@code true} on success
     * @see #append(String, List)
     * @since 1.0.0
     */
    public static boolean appendRow(String path, List<String> row) { return append(path, row); }

    /**
     * Appends a single row with a custom delimiter.
     *
     * @param path the file path
     * @param row the row to append
     * @param delimiter the field separator character
     * @return {@code true} on success
     * @see #append(String, List)
     * @since 1.0.0
     */
    public static boolean append(String path, List<String> row, char delimiter) {
        if (row == null) return false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(escape(row.get(i), delimiter));
        }
        sb.append('\n');
        try {
            Files.writeString(Path.of(path), sb.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Trace.debug("csv", "append: {} cells to {}", row.size(), path);
            return true;
        } catch (IOException e) {
            Trace.warn("csv", "append failed: " + e.getMessage());
            Logger.error("Cannot append to " + path);
            return false;
        }
    }

    /**
     * Writes a list of records (keyed by header) to the CSV file. The
     * headers define both the column order and which keys are read from
     * each record.
     *
     * @param path the file path
     * @param records the records to write (may be null → just the header)
     * @param headers the column headers, in order
     * @return {@code true} on success
     * @see #saveRecords(String, List, List)
     * @see #writeTable(String, CsvTable)
     * @since 1.0.0
     */
    public static boolean writeRecords(String path, List<Map<String, String>> records, List<String> headers) {
        if (headers == null || headers.isEmpty()) return false;
        List<List<String>> rows = new ArrayList<>();
        rows.add(headers);
        if (records != null) {
            for (Map<String, String> record : records) {
                List<String> row = new ArrayList<>();
                for (String header : headers) {
                    row.add(record.getOrDefault(header, ""));
                }
                rows.add(row);
            }
        }
        return write(path, rows);
    }

    /**
     * Alias of {@link #writeRecords(String, List, List)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param r the records to write
     * @param h the headers
     * @return {@code true} on success
     * @see #writeRecords(String, List, List)
     * @since 1.0.0
     */
    public static boolean saveRecords(String path, List<Map<String, String>> r, List<String> h) {
        return writeRecords(path, r, h);
    }

    /**
     * Writes a {@link CsvTable} to the CSV file — one call instead of
     * {@code writeRecords(path, t.records(), t.headers())}.
     *
     * @param path the file path
     * @param t the table to write (null → false)
     * @return {@code true} on success
     * @see CsvTable
     * @see #writeRecords(String, List, List)
     * @since 1.0.0
     */
    public static boolean writeTable(String path, CsvTable t) {
        if (t == null) return false;
        return writeRecords(path, t.records(), t.headers());
    }

    // ============================================================== helpers

    static List<List<String>> parse(String text, char delimiter) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == delimiter) {
                row.add(field.toString());
                field.setLength(0);
            } else if (c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else if (c == '\r') {
                // skip — handled by the \n branch on the next character
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    static String escape(String value, char delimiter) {
        if (value == null) return "";
        boolean needsQuotes = value.indexOf(delimiter) >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (!needsQuotes) return value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
