package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvEnhancedTest {

    @TempDir
    Path dir;

    private String writeSample() throws Exception {
        Path p = dir.resolve("data.csv");
        CsvFile.write(p.toString(), List.of(
                List.of("name", "score", "grade"),
                List.of("Saad", "95", "A"),
                List.of("Ali", "88", "B"),
                List.of("Chan", "95", "A")));
        return p.toString();
    }

    // ============================================================== read aliases

    @Test
    void readAliases() throws Exception {
        String p = writeSample();
        assertEquals(4, CsvFile.read(p).size());
        assertEquals(4, CsvFile.load(p).size());
        assertEquals(4, CsvFile.readCsv(p).size());
        assertEquals(4, CsvFile.parseFile(p).size());
    }

    @Test
    void readHeadersAliases() throws Exception {
        String p = writeSample();
        assertEquals(List.of("name", "score", "grade"), CsvFile.readHeaders(p));
        assertEquals(List.of("name", "score", "grade"), CsvFile.headers(p));
        assertEquals(List.of("name", "score", "grade"), CsvFile.columns(p));
        assertEquals(List.of("name", "score", "grade"), CsvFile.getHeaders(p));
    }

    @Test
    void readAsRecordsAliases() throws Exception {
        String p = writeSample();
        assertEquals(3, CsvFile.readAsRecords(p).size());
        assertEquals(3, CsvFile.records(p).size());
        assertEquals(3, CsvFile.asRecords(p).size());
        assertEquals(3, CsvFile.readRecords(p).size());
    }

    // ============================================================== table view

    @Test
    void tableReturnsCsvTable() throws Exception {
        String p = writeSample();
        CsvFile.CsvTable t = CsvFile.table(p);
        assertEquals(3, t.headers().size());
        assertEquals(3, t.rowCount());
        assertEquals(3, t.columnCount());
        assertFalse(t.isEmpty());
    }

    @Test
    void tableColumn() throws Exception {
        String p = writeSample();
        CsvFile.CsvTable t = CsvFile.table(p);
        assertEquals(List.of("Saad", "Ali", "Chan"), t.column("name"));
        assertEquals(List.of("95", "88", "95"), t.column("score"));
    }

    @Test
    void tableFilter() throws Exception {
        String p = writeSample();
        CsvFile.CsvTable t = CsvFile.table(p).filter(r -> "A".equals(r.get("grade")));
        assertEquals(2, t.rowCount());
    }

    @Test
    void tableSortBy() throws Exception {
        String p = writeSample();
        CsvFile.CsvTable t = CsvFile.table(p).sortBy("name");
        assertEquals("Ali", t.records().get(0).get("name"));
        assertEquals("Chan", t.records().get(1).get("name"));
        assertEquals("Saad", t.records().get(2).get("name"));
    }

    @Test
    void columnDirect() throws Exception {
        String p = writeSample();
        assertEquals(List.of("Saad", "Ali", "Chan"), CsvFile.column(p, "name"));
    }

    @Test
    void rowColumnCount() throws Exception {
        String p = writeSample();
        assertEquals(3, CsvFile.rowCount(p));
        assertEquals(3, CsvFile.columnCount(p));
    }

    // ============================================================== write aliases

    @Test
    void writeAliases() {
        String p = dir.resolve("out.csv").toString();
        List<List<String>> rows = List.of(List.of("a", "b"));
        assertTrue(CsvFile.write(p, rows));
        assertTrue(CsvFile.save(p, rows));
        assertTrue(CsvFile.writeCsv(p, rows));
        assertTrue(CsvFile.saveCsv(p, rows));
    }

    @Test
    void appendAliases() {
        String p = dir.resolve("app.csv").toString();
        CsvFile.write(p, List.of(List.of("a", "b")));
        CsvFile.append(p, List.of("c", "d"));
        CsvFile.addRow(p, List.of("e", "f"));
        CsvFile.appendRow(p, List.of("g", "h"));
        assertEquals(4, CsvFile.read(p).size());
    }

    // ============================================================== key-value

    @Test
    void writeWithDelimiterKeyValue() {
        String p = dir.resolve("tab.tsv").toString();
        CsvFile.write(p, List.of(List.of("a", "b")), CsvFile.Key.DELIMITER, '\t');
        assertTrue(TextFileIO.readAll(p).contains("\t"));
    }

    @Test
    void writeWithNullKeyValueDoesNotThrow() {
        String p = dir.resolve("safe.csv").toString();
        assertDoesNotThrow(() -> CsvFile.write(p, List.of(List.of("a")), (Object[]) null));
    }

    @Test
    void writeWithWrongTypeSkipped() {
        String p = dir.resolve("safe.csv").toString();
        assertDoesNotThrow(() -> CsvFile.write(p, List.of(List.of("a")), CsvFile.Key.DELIMITER, "not a char"));
    }

    // ============================================================== writeRecords

    @Test
    void writeRecordsAliases() {
        String p = dir.resolve("wr.csv").toString();
        List<Map<String, String>> recs = List.of(
                Map.of("name", "Saad", "score", "95"));
        assertTrue(CsvFile.writeRecords(p, recs, List.of("name", "score")));
        assertEquals(1, CsvFile.rowCount(p));
    }

    @Test
    void writeTable() throws Exception {
        String p1 = writeSample();
        CsvFile.CsvTable t = CsvFile.table(p1);
        String p2 = dir.resolve("copy.csv").toString();
        assertTrue(CsvFile.writeTable(p2, t));
        assertEquals(CsvFile.rowCount(p1), CsvFile.rowCount(p2));
    }
}
