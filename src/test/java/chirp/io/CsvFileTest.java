package chirp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvFileTest {

    @TempDir
    Path dir;

    @Test
    void roundTripSimple() {
        String p = dir.resolve("s.csv").toString();
        List<List<String>> rows = List.of(
                List.of("a", "b"),
                List.of("c", "d"));
        assertTrue(CsvFile.write(p, rows));
        assertEquals(rows, CsvFile.read(p));
    }

    @Test
    void quotesAndCommasInFields() {
        String p = dir.resolve("q.csv").toString();
        List<List<String>> rows = List.of(
                List.of("hello, world", "42"),
                List.of("quote \" inside", "ok"));
        assertTrue(CsvFile.write(p, rows));
        assertEquals(rows, CsvFile.read(p));
    }

    @Test
    void embeddedNewlineInField() {
        String p = dir.resolve("nl.csv").toString();
        List<List<String>> rows = List.of(
                List.of("line1\nline2", "x"),
                List.of("y", "z"));
        assertTrue(CsvFile.write(p, rows));
        List<List<String>> read = CsvFile.read(p);
        assertEquals(2, read.size());
        assertEquals("line1\nline2", read.get(0).get(0));
        assertEquals("x", read.get(0).get(1));
    }

    @Test
    void emptyCellsPreserved() {
        String p = dir.resolve("e.csv").toString();
        List<List<String>> rows = List.of(List.of("", "b", ""));
        CsvFile.write(p, rows);
        assertEquals(rows, CsvFile.read(p));
    }

    @Test
    void customDelimiter() {
        String p = dir.resolve("t.tsv").toString();
        List<List<String>> rows = List.of(List.of("a", "b"), List.of("c", "d"));
        CsvFile.write(p, rows, '\t');
        assertEquals(rows, CsvFile.read(p, '\t'));
    }

    @Test
    void readHeaders() {
        String p = dir.resolve("h.csv").toString();
        CsvFile.write(p, List.of(
                List.of("name", "score"),
                List.of("Saad", "95")));
        assertEquals(List.of("name", "score"), CsvFile.readHeaders(p));
    }

    @Test
    void readWithHeadersSkipsFirstRow() {
        String p = dir.resolve("wh.csv").toString();
        CsvFile.write(p, List.of(
                List.of("k1", "k2"),
                List.of("v1", "v2")));
        List<List<String>> body = CsvFile.readWithHeaders(p);
        assertEquals(1, body.size());
        assertEquals(List.of("v1", "v2"), body.get(0));
    }

    @Test
    void readAsRecordsWithHeaders() {
        String p = dir.resolve("rec.csv").toString();
        CsvFile.write(p, List.of(
                List.of("name", "score"),
                List.of("Saad", "95"),
                List.of("Ali", "88")));
        List<Map<String, String>> records = CsvFile.readAsRecords(p);
        assertEquals(2, records.size());
        assertEquals("Saad", records.get(0).get("name"));
        assertEquals("95", records.get(0).get("score"));
    }

    @Test
    void writeRecordsRoundTrip() {
        String p = dir.resolve("wr.csv").toString();
        List<Map<String, String>> records = List.of(
                Map.of("name", "Saad", "score", "95"),
                Map.of("name", "Ali", "score", "88"));
        assertTrue(CsvFile.writeRecords(p, records, List.of("name", "score")));
        List<Map<String, String>> read = CsvFile.readAsRecords(p);
        assertEquals(2, read.size());
        assertEquals("Saad", read.get(0).get("name"));
    }

    @Test
    void appendRow() {
        String p = dir.resolve("ap.csv").toString();
        CsvFile.write(p, List.of(List.of("a", "b")));
        CsvFile.append(p, List.of("c", "d"));
        List<List<String>> rows = CsvFile.read(p);
        assertEquals(2, rows.size());
        assertEquals(List.of("c", "d"), rows.get(1));
    }

    @Test
    void missingFileReturnsEmptyList() {
        assertTrue(CsvFile.read(dir.resolve("nope.csv").toString()).isEmpty());
    }

    @Test
    void blankLineProducesSingleEmptyCell() {
        String p = dir.resolve("bl.csv").toString();
        TextFileIO.write(p, "a,b\n\nc,d\n");
        List<List<String>> rows = CsvFile.read(p);
        assertEquals(3, rows.size());
        assertEquals(List.of(""), rows.get(1));
    }
}
