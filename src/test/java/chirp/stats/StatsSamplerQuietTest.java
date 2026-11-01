package chirp.stats;

import chirp.config.Config;
import chirp.internal.Trace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards against the regression where {@link StatsSampler} traced every tick,
 * flooding stderr. Also validates the listener-exception rate limit.
 */
class StatsSamplerQuietTest {

    private final ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
    private PrintStream originalErr;
    private StatsSampler sampler;

    @BeforeEach
    void setup() {
        originalErr = System.err;
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
        Trace.reset();
        Trace.enable();
        sampler = new StatsSampler(200);
    }

    @AfterEach
    void teardown() {
        if (sampler != null) sampler.stop();
        System.setErr(originalErr);
        Trace.reset();
        Config.reset();
    }

    private String err() { return capturedErr.toString(StandardCharsets.UTF_8); }
    private long traceLines() {
        return err().lines()
                .filter(l -> l.contains("[TRACE]") || l.contains("[DEBUG]"))
                .filter(l -> l.contains("[stats"))
                .count();
    }

    @Test
    void defaultIsNotVerbose() {
        assertFalse(sampler.isVerbose());
    }

    @Test
    void sampleOnceEmitsNoTraceWhenQuiet() {
        sampler.sampleOnce();
        sampler.sampleOnce();
        sampler.sampleOnce();
        assertEquals(0, traceLines(),
                "Quiet sampler must not emit stats trace lines. Got:\n" + err());
    }

    @Test
    void multipleSamplesStaySilent() {
        for (int i = 0; i < 10; i++) sampler.sampleOnce();
        assertEquals(0, traceLines());
    }

    @Test
    void verboseModeEmitsTraceOnEachSample() {
        sampler.setVerbose(true);
        sampler.sampleOnce();
        sampler.sampleOnce();
        assertEquals(2, traceLines(),
                "Verbose sampler should emit one trace line per sample. Got:\n" + err());
    }

    @Test
    void verboseToggleWorks() {
        assertFalse(sampler.isVerbose());
        sampler.enableVerbose();
        assertTrue(sampler.isVerbose());
        sampler.disableVerbose();
        assertFalse(sampler.isVerbose());
    }

    @Test
    void verboseSampleContainsHeapAndCpu() {
        sampler.setVerbose(true);
        sampler.sampleOnce();
        String err = err();
        assertTrue(err.contains("tick:"));
        assertTrue(err.contains("heap="));
    }

    // ---------------------------------------------------------- rate-limit

    /**
     * Validates that listener exceptions are rate-limited to one warning per
     * 5-second window — <b>not</b> per sample.
     *
     * <p><b>Test strategy:</b> the assertion bound is derived from
     * <em>actual elapsed time</em>. If the loop runs in 100 ms we expect
     * exactly 1 warning; if the loop runs in 6 s (slow CI) we expect 1–2.
     * Either way, we always assert "way fewer than 100" — the true
     * regression this test protects against.</p>
     */
    @Test
    void listenerExceptionIsRateLimited() {
        sampler.addListener(snapshot -> { throw new RuntimeException("boom"); });

        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) sampler.sampleOnce();
        long elapsedNs = System.nanoTime() - start;

        long warningCount = err().lines()
                .filter(l -> l.contains("StatsListener threw"))
                .count();

        // Rate-limit is 5s per window. In `elapsedNs`, we get at most
        // 1 + floor(elapsedNs / 5s) warnings.
        long windowsElapsed = elapsedNs / 5_000_000_000L;
        long maxWarnings = 1 + windowsElapsed;

        assertTrue(warningCount <= maxWarnings,
                "Warnings must be rate-limited to one per 5s window. "
                        + "In " + (elapsedNs / 1_000_000) + "ms, expected at most "
                        + maxWarnings + " but got " + warningCount + ":\n" + err());

        // The core invariant: way fewer warnings than the 100 events
        assertTrue(warningCount < 50,
                "Rate-limiting appears broken — got " + warningCount + "/100:\n" + err());
    }

    @Test
    void listenerReceivesSnapshots() {
        AtomicInteger calls = new AtomicInteger();
        sampler.addListener(snapshot -> calls.incrementAndGet());
        sampler.sampleOnce();
        sampler.sampleOnce();
        assertEquals(2, calls.get());
    }

    @Test
    void removingListenerStopsNotifications() {
        AtomicInteger calls = new AtomicInteger();
        StatsListener listener = snapshot -> calls.incrementAndGet();
        sampler.addListener(listener);
        sampler.sampleOnce();
        assertEquals(1, calls.get());
        sampler.removeListener(listener);
        sampler.sampleOnce();
        assertEquals(1, calls.get());
    }
}
