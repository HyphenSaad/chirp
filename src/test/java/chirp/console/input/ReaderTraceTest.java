package chirp.console.input;

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
 * Verifies that the fluent readers emit trace output when trace is enabled.
 * Each test toggles trace on, runs the reader with a scripted input, then
 * asserts on the shape of the trace.
 */
class ReaderTraceTest {

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
        Config.enableStrictWarnings();
        Config.disableStrictWarnings(); // keep strict noise out of trace tests
        Trace.enable();
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

    // -------------------------------------------------------------- int
    @Test
    void intReaderTracesEnterExit() {
        feed("25\n");
        int value = Input.readInt().label("Age").between(1, 130).read();
        assertEquals(25, value);
        String err = err();
        assertTrue(err.contains("enter IntReader.read"), err);
        assertTrue(err.contains("exit  IntReader.read => 25"), err);
    }

    @Test
    void intReaderTracesParsedValue() {
        feed("42\n");
        Input.readInt().label("Qty").between(1, 100).read();
        assertTrue(err().contains("value: parsed int = 42"), err());
    }

    @Test
    void intReaderTracesRangeDecision() {
        feed("25\n");
        Input.readInt().label("Age").between(1, 130).read();
        assertTrue(err().contains("RANGE min(1)"), err());
        assertTrue(err().contains("RANGE max(130)"), err());
    }

    @Test
    void intReaderTracesInvalidRetry() {
        feed("abc\n25\n");
        Input.readInt().label("Age").between(1, 130).read();
        String err = err();
        assertTrue(err.contains("int parse"), err);
        assertTrue(err.contains("FAIL"), err);
    }

    // -------------------------------------------------------------- string
    @Test
    void stringReaderTracesEnterExit() {
        feed("Saad\n");
        String name = Input.readString().label("Name").notBlank().atLeast(2).atMost(50).skipFormatWarning().read();
        assertEquals("Saad", name);
        assertTrue(err().contains("enter StringReader.read"), err());
        assertTrue(err().contains("exit  StringReader.read"), err());
    }

    @Test
    void stringReaderTracesBlankFailure() {
        feed("\nabc\n");
        Input.readString().label("Name").notBlank().skipLengthWarning().skipFormatWarning().read();
        assertTrue(err().contains("BLANK"), err());
        assertTrue(err().contains("FAIL"), err());
    }

    @Test
    void stringReaderTracesRuleEvaluation() {
        feed("0300-1234567\n");
        Input.readString().label("Phone").rule("PK_MOBILE").notBlank().skipLengthWarning().read();
        String err = err();
        assertTrue(err.contains("PK_MOBILE"), err);
        assertTrue(err.contains("PASS"), err);
    }

    // -------------------------------------------------------------- double
    @Test
    void doubleReaderTracesEnterExit() {
        feed("3.14\n");
        double pi = Input.readDouble().label("Pi").between(0, 10).read();
        assertEquals(3.14, pi, 0.0001);
        assertTrue(err().contains("enter DoubleReader.read"), err());
        assertTrue(err().contains("exit  DoubleReader.read"), err());
    }

    // -------------------------------------------------------------- boolean
    @Test
    void booleanReaderTracesDecision() {
        feed("y\n");
        boolean result = Input.readBoolean().label("Continue").read();
        assertTrue(result);
        assertTrue(err().contains("exit  BooleanReader.read => true"), err());
    }

    // -------------------------------------------------------------- configure events
    @Test
    void configureCallsAreTraced() {
        feed("25\n");
        Input.readInt().label("Age").between(1, 130).read();
        String err = err();
        assertTrue(err.contains("configure: between(1, 130)"), err);
        assertTrue(err.contains("configure: label"), err);
    }

    // -------------------------------------------------------------- disabled trace
    @Test
    void disabledTraceProducesNothing() {
        Trace.disable();
        feed("25\n");
        Input.readInt().label("Age").between(1, 130).read();
        assertFalse(err().contains("[TRACE]"), err());
        assertFalse(err().contains("[DEBUG]"), err());
    }
}
