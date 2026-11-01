package chirp.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards against regression of the missing-varargs bug that broke the build
 * in the previous delivery. Every log level is exercised with format strings
 * and arguments.
 */
class TraceVarargsTest {

    private final ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
    private PrintStream originalErr;

    @BeforeEach
    void setup() {
        originalErr = System.err;
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
        Trace.reset();
        Trace.enable();
    }

    @AfterEach
    void teardown() {
        System.setErr(originalErr);
        Trace.reset();
    }

    private String err() { return capturedErr.toString(StandardCharsets.UTF_8); }

    @Test
    void traceWithArgs() {
        Trace.trace("reader", "value={} threshold={}", 5, 10);
        assertTrue(err().contains("value=5 threshold=10"), err());
    }

    @Test
    void debugWithArgs() {
        Trace.debug("reader", "x={}", 1);
        assertTrue(err().contains("x=1"), err());
    }

    @Test
    void infoWithArgs() {
        Trace.info("config", "level={}", "DEBUG");
        assertTrue(err().contains("level=\"DEBUG\""), err());
    }

    @Test
    void warnWithArgs() {
        Trace.warn("reader", "unknown rule: {}", "FOO");
        assertTrue(err().contains("unknown rule: \"FOO\""), err());
    }

    @Test
    void warnWithIntArg() {
        Trace.warn("json", "position {}", 42);
        assertTrue(err().contains("position 42"), err());
    }

    @Test
    void warnWithNoArgs() {
        Trace.warn("reader", "plain message");
        assertTrue(err().contains("plain message"), err());
    }

    @Test
    void allLevelsAcceptVarargs() {
        assertDoesNotThrow(() -> Trace.trace("x", "{}", 1));
        assertDoesNotThrow(() -> Trace.debug("x", "{}", 2));
        assertDoesNotThrow(() -> Trace.info("x", "{}", 3));
        assertDoesNotThrow(() -> Trace.warn("x", "{}", 4));
    }

    @Test
    void extraPlaceholdersRemain() {
        Trace.warn("x", "a={} b={} c={}", 1);
        assertTrue(err().contains("a=1 b={} c={}"), err());
    }

    @Test
    void fewerPlaceholdersThanArgs() {
        Trace.warn("x", "a={}", 1, 2, 3);
        assertTrue(err().contains("a=1"), err());
    }
}
