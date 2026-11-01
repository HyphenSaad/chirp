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
 * CSV file operations with full RFC 4180 quoting support and a typed table
 * view.
 *
 * <h2>Naming</h2>
 * <p>{@code read / load / readCsv / parseFile}; {@code readWithHeaders /
 * readWithHeader / bodyRows}; {@code readHeaders / headers / columns /
 * getHeaders}; {@code readAsRecords / records / asRecords / readRecords};
 * {@code write / save / writeCsv / saveCsv}; {@code append / addRow /
 * appendRow}; {@code writeRecords / saveRecords / writeAll}.</p>
 *
 * <h2>Table view</h2>
 * <p>{@link #table(String)} returns a {@link CsvTable} record with
 * {@code headers} and {@code records} — easier than juggling two return
 * types.</p>
 */
public final class CsvFile {

    public enum Key { DELIMITER, HEADERS, QUOTE }

    /**
     * A CSV table — headers plus typed records.
     *
     * <p>Both fields are defensive-copied in the canonical constructor:
     * {@code headers} becomes an immutable list, {@code records} becomes an
     * immutable list of immutable {@link LinkedHashMap}s (order preserved).
     * Callers can no longer mutate the table by keeping a reference to the
     * lists/maps they passed to the constructor.</p>
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

        public int rowCount() { return records.size(); }
        public int columnCount() { return headers.size(); }
        public boolean isEmpty() { return records.isEmpty(); }

        /** Returns a column's values from every row, in order. */
        public List<String> column(String name) {
            List<String> result = new ArrayList<>();
            for (Map<String, String> row : records) {
                result.add(row.getOrDefault(name, ""));
            }
            return result;
        }

        /** Filters rows by a predicate on the whole record. */
        public CsvTable filter(Predicate<Map<String, String>> predicate) {
            List<Map<String, String>> kept = new ArrayList<>();
            for (Map<String, String> row : records) if (predicate.test(row)) kept.add(row);
            return new CsvTable(headers, kept);
        }

        /** Sorts rows by a column, lexicographically. */
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

    public static List<List<String>> read(String path) {
        return read(path, ',');
    }
    public static List<List<String>> load(String path) { return read(path); }
    public static List<List<String>> readCsv(String path) { return read(path); }
    public static List<List<String>> parseFile(String path) { return read(path); }

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

    public static List<List<String>> readWithHeaders(String path) {
        List<List<String>> rows = read(path);
        if (rows.isEmpty()) return rows;
        return new ArrayList<>(rows.subList(1, rows.size()));
    }
    public static List<List<String>> readWithHeader(String path) { return readWithHeaders(path); }
    public static List<List<String>> bodyRows(String path) { return readWithHeaders(path); }

    public static List<String> readHeaders(String path) {
        List<List<String>> rows = read(path);
        return rows.isEmpty() ? new ArrayList<>() : rows.get(0);
    }
    public static List<String> headers(String path) { return readHeaders(path); }
    public static List<String> columns(String path) { return readHeaders(path); }
    public static List<String> getHeaders(String path) { return readHeaders(path); }

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
    public static List<Map<String, String>> records(String path) { return readAsRecords(path); }
    public static List<Map<String, String>> asRecords(String path) { return readAsRecords(path); }
    public static List<Map<String, String>> readRecords(String path) { return readAsRecords(path); }

    /**
     * Reads a CSV file as a typed {@link CsvTable} — headers + records in one
     * call. Preferred over {@link #readHeaders} + {@link #readAsRecords} when
     * you need both.
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
    public static CsvTable asTable(String path) { return table(path); }

    /** Returns a single column's values across all rows. */
    public static List<String> column(String path, String name) {
        CsvTable t = table(path);
        return t.column(name);
    }

    /** Number of data rows (excludes header). */
    public static int rowCount(String path) {
        List<List<String>> rows = read(path);
        return Math.max(0, rows.size() - 1);
    }

    /** Number of columns (from header row). */
    public static int columnCount(String path) {
        List<String> headers = readHeaders(path);
        return headers.size();
    }

    // ============================================================== write

    public static boolean write(String path, List<List<String>> rows) {
        return write(path, rows, ',');
    }
    public static boolean save(String path, List<List<String>> rows) { return write(path, rows); }
    public static boolean writeCsv(String path, List<List<String>> rows) { return write(path, rows); }
    public static boolean saveCsv(String path, List<List<String>> rows) { return write(path, rows); }

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

    /** Key-value overload — {@code write(path, rows, Key.DELIMITER, '\t')}. */
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
    public static boolean save(String path, List<List<String>> rows, Object... keyValues) {
        return write(path, rows, keyValues);
    }

    public static boolean append(String path, List<String> row) {
        return append(path, row, ',');
    }
    public static boolean addRow(String path, List<String> row) { return append(path, row); }
    public static boolean appendRow(String path, List<String> row) { return append(path, row); }

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
    public static boolean saveRecords(String path, List<Map<String, String>> r, List<String> h) {
        return writeRecords(path, r, h);
    }
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
                // skip
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
