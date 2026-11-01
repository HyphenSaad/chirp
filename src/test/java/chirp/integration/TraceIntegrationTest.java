package chirp.integration;

import chirp.config.Config;
import chirp.console.Input;
import chirp.internal.Trace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that exercise trace + strict + stats together.
 * Verifies that toggles behave independently and output is not lost or
 * doubled.
 */
class TraceIntegrationTest {

    private final ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    private PrintStream originalErr;
    private PrintStream originalOut;

    @BeforeEach
    void setup() {
        originalErr = System.err;
        originalOut = System.out;
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        Trace.reset();
        Config.reset();
    }

    @AfterEach
    void teardown() {
        System.setErr(originalErr);
        System.setOut(originalOut);
        Trace.reset();
        Config.reset();
        Input.resetInputStream();
    }

    private void feed(String input) {
        Input.setInputStream(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private String err() { return capturedErr.toString(StandardCharsets.UTF_8); }

    @Test
    void traceAndStrictCanCoexist() {
        Config.enableTrace();
        Config.enableStrictWarnings();
        StrictReset();
        feed("Saad\n");
        Input.readString().label("Name").read();

        String err = err();
        assertTrue(err.contains("[STRICT]"), "strict warnings should appear");
        assertTrue(err.contains("[TRACE]") || err.contains("[DEBUG]"),
                "trace lines should appear");
    }

    @Test
    void disablingStrictDoesNotAffectTrace() {
        Config.enableTrace();
        Config.disableStrictWarnings();
        feed("Saad\n");
        Input.readString().label("Name").read();

        String err = err();
        assertFalse(err.contains("[STRICT]"), "no strict warnings expected");
        assertTrue(err.contains("enter StringReader.read"), "trace should still fire");
    }

    @Test
    void disablingTraceDoesNotAffectStrict() {
        Config.disableTrace();
        Config.enableStrictWarnings();
        StrictReset();
        feed("Saad\n");
        Input.readString().label("Name").read();

        String err = err();
        assertTrue(err.contains("[STRICT]"), "strict warnings should still fire");
        assertFalse(err.contains("enter StringReader.read"), "no trace lines");
    }

    @Test
    void traceCategoryFilteringWorksAcrossModules() {
        Config.enableTrace();
        Config.traceOnly("reader", "rule");
        feed("user@example.com\n");
        Input.readString().label("Email").email().read();

        String err = err();
        assertTrue(err.contains("[reader"), "reader category should be visible");
        assertFalse(err.contains("[input "), "input category should be filtered out");
    }

    private static void StrictReset() {
        // Clear the warnOnce throttle so the test's label doesn't linger from
        // a previous test run.
        try {
            Class<?> strictClass = Class.forName("chirp.internal.Strict");
            strictClass.getMethod("reset").invoke(null);
        } catch (Exception ignored) {}
    }
}
