package chirp.internal;

import chirp.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the internal {@link Trace} helper — toggles, levels, categories,
 * structured helpers, format safety, and counter semantics.
 */
class TraceTest {

    private final ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
    private PrintStream originalErr;

    @BeforeEach
    void setup() {
        originalErr = System.err;
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
        Trace.reset();
    }

    @AfterEach
    void teardown() {
        System.setErr(originalErr);
        Trace.reset();
        Config.reset();
    }

    private String err() { return capturedErr.toString(StandardCharsets.UTF_8); }
    private long lines() { return err().lines().count(); }

    // ============================================================== toggles

    @Test
    void disabledByDefault() { assertFalse(Trace.isEnabled()); }

    @Test
    void enableDisable() {
        Trace.enable();
        assertTrue(Trace.isEnabled());
        Trace.disable();
        assertFalse(Trace.isEnabled());
    }

    @Test
    void silentWhenDisabled() {
        Trace.disable();
        Trace.trace("reader", "should not print");
        Trace.debug("reader", "should not print");
        Trace.info("reader", "should not print");
        Trace.warn("reader", "should not print");
        assertEquals(0, lines());
    }

    // ============================================================== levels

    @Test
    void defaultLevelIsTrace() {
        Trace.enable();
        assertEquals(Trace.Level.TRACE, Trace.getLevel());
    }

    @Test
    void debugLevelSuppressesTraceLines() {
        Trace.enable();
        Trace.setLevel(Trace.Level.DEBUG);
        Trace.trace("reader", "hidden");
        Trace.debug("reader", "visible");
        String err = err();
        assertFalse(err.contains("hidden"), err);
        assertTrue(err.contains("visible"), err);
    }

    @Test
    void infoLevelSuppressesDebugAndTrace() {
        Trace.enable();
        Trace.setLevel(Trace.Level.INFO);
        Trace.trace("x", "TRACE-LEVEL");
        Trace.debug("x", "DEBUG-LEVEL");
        Trace.info("x", "INFO-LEVEL");
        String err = err();

        // TRACE and DEBUG lines should not appear as message bodies
        assertFalse(err.contains("TRACE-LEVEL"), err);
        assertFalse(err.contains("DEBUG-LEVEL"), err);
        // INFO line is the only one that carries the message
        assertTrue(err.contains("INFO-LEVEL"), err);
    }

    // ============================================================== categories

    @Test
    void categoryFiltering() {
        Trace.enable();
        Trace.enableOnly("reader");
        Trace.debug("reader", "shown");
        Trace.debug("rule", "hidden");
        String err = err();
        assertTrue(err.contains("shown"), err);
        assertFalse(err.contains("hidden"), err);
    }

    @Test
    void enableCategoryMethod() {
        Trace.enable();
        Trace.enableOnly();
        Trace.enableCategory("reader");
        Trace.debug("reader", "shown");
        Trace.debug("rule", "hidden");
        String err = err();
        assertTrue(err.contains("shown"));
        assertFalse(err.contains("hidden"));
    }

    @Test
    void enableAllCategoriesRestoresFullOutput() {
        Trace.enable();
        Trace.enableOnly("reader");
        Trace.enableAllCategories();
        Trace.debug("anything", "shown");
        assertTrue(err().contains("shown"));
    }

    // ============================================================== structured API

    @Test
    void enterExitProduceExpectedLines() {
        Trace.enable();
        Trace.enter("reader", "IntReader.read", "label=Age", "min=1");
        Trace.exit("reader", "IntReader.read", 25);
        String err = err();
        assertTrue(err.contains("enter IntReader.read(label=Age, min=1)"), err);
        assertTrue(err.contains("exit  IntReader.read => 25"), err);
    }

    @Test
    void decisionFormatsBoolean() {
        Trace.enable();
        Trace.decision("reader", "RANGE applied", true);
        assertTrue(err().contains("decision: RANGE applied => TRUE"));
    }

    @Test
    void valueFormatsQuotedStrings() {
        Trace.enable();
        Trace.value("reader", "label", "Enter name");
        assertTrue(err().contains("value: label = \"Enter name\""), err());
    }

    @Test
    void validationPassOutcome() {
        Trace.enable();
        Trace.validation("reader", "RANGE", true, null);
        assertTrue(err().contains("validation: RANGE => PASS"), err());
    }

    @Test
    void validationFailOutcomeIncludesReason() {
        Trace.enable();
        Trace.validation("reader", "RANGE", false, "below min");
        assertTrue(err().contains("validation: RANGE => FAIL (below min)"), err());
    }

    // ============================================================== format safety

    @Test
    void placeholderReplacement() {
        Trace.enable();
        Trace.debug("reader", "value={} threshold={}", 5, 10);
        assertTrue(err().contains("value=5 threshold=10"), err());
    }

    @Test
    void extraPlaceholdersAllowed() {
        Trace.enable();
        Trace.debug("reader", "a={} b={} c={}", 1, 2);
        assertTrue(err().contains("a=1 b=2 c={}"), err());
    }

    @Test
    void throwsFromToStringAreCaught() {
        Trace.enable();
        Object bad = new Object() {
            @Override
            public String toString() { throw new RuntimeException("boom"); }
        };
        assertDoesNotThrow(() -> Trace.value("reader", "obj", bad));
        assertTrue(err().contains("<unprintable"), err());
    }

    @Test
    void longStringsAreTruncated() {
        Trace.enable();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('x');
        Trace.value("reader", "blob", sb.toString());
        assertTrue(err().contains("..."), err());
    }

    // ============================================================== counter

    @Test
    void counterIncrementsPerLine() {
        Trace.enable();
        Trace.debug("x", "1");
        Trace.debug("x", "2");
        Trace.debug("x", "3");
        String err = err();
        assertTrue(err.contains("#0001"), err);
        assertTrue(err.contains("#0002"), err);
        assertTrue(err.contains("#0003"), err);
    }

    @Test
    void resetClearsCounter() {
        Trace.enable();
        Trace.debug("x", "one");
        Trace.reset();
        Trace.enable();
        Trace.debug("x", "two");
        assertTrue(err().contains("#0001"), err());
    }

    // ============================================================== null safety

    @Test
    void nullMessageDoesNotThrow() {
        Trace.enable();
        assertDoesNotThrow(() -> Trace.debug("x", (String) null));
    }

    @Test
    void nullCategoryDoesNotThrow() {
        Trace.enable();
        assertDoesNotThrow(() -> Trace.debug(null, "msg"));
    }

    @Test
    void nullArgsAreHandled() {
        Trace.enable();
        assertDoesNotThrow(() -> Trace.debug("x", "a={} b={}", null, 42));
        assertTrue(err().contains("a=null b=42"), err());
    }

    // ============================================================== known categories

    @Test
    void knownCategoriesAreListed() {
        assertTrue(Trace.KNOWN_CATEGORIES.contains("reader"));
        assertTrue(Trace.KNOWN_CATEGORIES.contains("rule"));
        assertTrue(Trace.KNOWN_CATEGORIES.contains("input"));
    }
}
