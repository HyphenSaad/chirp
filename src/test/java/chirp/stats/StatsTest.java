package chirp.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsTest {

    @AfterEach
    void reset() { Stats.reset(); }

    @Test
    void resetZeroesCounters() {
        Stats.recordLine(10);
        Stats.recordLine(5);
        Stats.reset();
        assertEquals(0, Stats.snapshot().linesRead());
        assertEquals(0, Stats.snapshot().charactersRead());
    }

    @Test
    void recordsLinesAndCharacters() {
        Stats.reset();
        Stats.recordLine(4);
        Stats.recordLine(3);
        Stats.recordLine(0);
        Stats.StatsSnapshot snap = Stats.snapshot();
        assertEquals(3, snap.linesRead());
        assertEquals(7, snap.charactersRead());
        assertEquals(3, snap.readCalls());
    }

    @Test
    void snapshotHasPositiveElapsed() {
        Stats.reset();
        Stats.StatsSnapshot snap = Stats.snapshot();
        assertTrue(snap.elapsedSeconds() >= 0);
        assertTrue(snap.heapTotalBytes() > 0);
        assertTrue(snap.heapMaxBytes() > 0);
    }

    @Test
    void cpuReflectiveValueIsWellFormed() {
        double cpu = Stats.snapshot().processCpuPercent();
        assertTrue(cpu >= -1.0, "cpu=" + cpu);
    }

    @Test
    void reportNowDoesNotThrow() {
        assertDoesNotThrow(Stats::reportNow);
    }
}
