package chirp.console.input;

import chirp.config.Config;
import chirp.console.Input;
import chirp.internal.Strict;
import chirp.internal.ValidationCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for strict-warning behaviour of the fluent readers and
 * the immediate input methods.
 *
 * <p>Each test redirects {@code System.err} and {@code System.out}, feeds a
 * scripted input stream via {@link Input#setInputStream}, invokes a reader,
 * then asserts on the exact number and content of {@code [STRICT]} lines.</p>
 *
 * <p><b>Regression coverage.</b> These tests were written to reproduce the
 * bug where a fully-validated fluent reader leaked warnings from the
 * {@code Input.readLine} primitive that the reader called internally. With
 * the fix in place, every reader now uses {@link Input#readRaw(String)}
 * internally and therefore emits only its own warnings.</p>
 */
class ReaderStrictnessTest {

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
        Config.enableStrictWarnings();
        Strict.reset();
    }

    @AfterEach
    void teardown() {
        System.setErr(originalErr);
        System.setOut(originalOut);
        Input.resetInputStream();
        Config.enableStrictWarnings();
        Strict.reset();
    }

    // ----------------------------------------------------------------- helpers

    private void feed(String input) {
        Input.setInputStream(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private long strictCount() {
        return capturedErr.toString(StandardCharsets.UTF_8)
                .lines()
                .filter(l -> l.startsWith("[STRICT]"))
                .count();
    }

    private String stderr() { return capturedErr.toString(StandardCharsets.UTF_8); }
    private String stdout() { return capturedOut.toString(StandardCharsets.UTF_8); }

    // ============================================================== the reported bug

    /**
     * The exact scenario from the bug report: every category is either
     * covered or explicitly skipped, so <b>zero</b> warnings must appear.
     */
    @Test
    void userScenarioWithFullValidationEmitsZeroWarnings() {
        feed("Saad Mansoor\n");
        String name = Input.readString()
                .label("Enter Your Name")
                .atLeast(3)
                .atMost(30)
                .notBlank()
                .skipFormatWarning()
                .read();

        assertEquals("Saad Mansoor", name);
        assertEquals(0, strictCount(),
                "Expected no [STRICT] warnings but got:\n" + stderr());
    }

    @Test
    void userScenarioPromptIsPrintedCorrectly() {
        feed("HyphenSaad\n");
        Input.readString()
                .label("Enter Your Name")
                .atLeast(3)
                .atMost(30)
                .notBlank()
                .skipFormatWarning()
                .read();

        String out = stdout();
        assertTrue(out.contains("Enter Your Name: "), "Prompt missing: " + out);
        assertFalse(out.contains("Enter Your Name: : "), "Double colon: " + out);
    }

    // ============================================================== fluent string

    @Test
    void fluentStringWithNothingAppliedEmitsThreeWarnings() {
        feed("Saad\n");
        Input.readString().label("Name").read();
        String err = stderr();
        assertTrue(err.contains("no BLANK validation"), err);
        assertTrue(err.contains("no LENGTH validation"), err);
        assertTrue(err.contains("no FORMAT validation"), err);
        assertEquals(3, strictCount(), "Expected exactly 3 [STRICT] lines:\n" + err);
    }

    @Test
    void fluentStringWithNotBlankOnlyEmitsLengthAndFormatWarnings() {
        feed("Saad\n");
        Input.readString().label("Name").notBlank().read();
        String err = stderr();
        assertFalse(err.contains("no BLANK validation"), err);
        assertTrue(err.contains("no LENGTH validation"), err);
        assertTrue(err.contains("no FORMAT validation"), err);
        assertEquals(2, strictCount());
    }

    @Test
    void fluentStringWithLengthOnlyEmitsBlankAndFormatWarnings() {
        feed("Saad\n");
        Input.readString().label("Name").atLeast(3).atMost(30).read();
        String err = stderr();
        assertTrue(err.contains("no BLANK validation"), err);
        assertFalse(err.contains("no LENGTH validation"), err);
        assertTrue(err.contains("no FORMAT validation"), err);
        assertEquals(2, strictCount());
    }

    @Test
    void fluentStringWithSkipFormatOnlyEmitsBlankAndLengthWarnings() {
        feed("Saad\n");
        Input.readString().label("Name").skipFormatWarning().read();
        String err = stderr();
        assertTrue(err.contains("no BLANK validation"), err);
        assertTrue(err.contains("no LENGTH validation"), err);
        assertFalse(err.contains("no FORMAT validation"), err);
        assertEquals(2, strictCount());
    }

    @Test
    void fluentStringWithEmailAppliedSkipsFormatWarning() {
        feed("user@example.com\n");
        String email = Input.readString().label("Email").email().read();
        assertEquals("user@example.com", email);
        assertFalse(stderr().contains("no FORMAT validation"), stderr());
    }

    @Test
    void fluentStringWithRegexSkipsFormatWarning() {
        feed("ABC-123\n");
        String code = Input.readString().label("Code").regex("[A-Z]{3}-\\d{3}").read();
        assertEquals("ABC-123", code);
        assertFalse(stderr().contains("no FORMAT validation"), stderr());
    }

    @Test
    void fluentStringWithRuleAppliedSkipsFormatWarning() {
        feed("0300-1234567\n");
        String phone = Input.readString().label("Phone").rule("PK_MOBILE").read();
        assertEquals("0300-1234567", phone);
        assertFalse(stderr().contains("no FORMAT validation"), stderr());
    }

    @Test
    void fluentStringWithLenientEmitsNothing() {
        feed("anything\n");
        Input.readString().label("X").lenient().read();
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void fluentStringWithAllThreeCoveredEmitsNothing() {
        feed("Hello World\n");
        Input.readString()
                .label("Greeting")
                .notBlank()
                .atLeast(1)
                .atMost(100)
                .regex("[A-Za-z ]+")
                .read();
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void fluentStringWarnsOncePerInstanceOnly() {
        feed("one\ntwo\n");
        StringReader reader = Input.readString().label("Name");
        reader.read();
        long afterFirst = strictCount();
        reader.read();
        assertEquals(afterFirst, strictCount(),
                "Second .read() on same reader instance must not re-warn");
    }

    // ============================================================== fluent int

    @Test
    void fluentIntWithRangeEmitsZeroWarnings() {
        feed("25\n");
        int age = Input.readInt().label("Age").between(1, 130).read();
        assertEquals(25, age);
        assertEquals(0, strictCount(),
                "Expected no [STRICT] warnings but got:\n" + stderr());
    }

    @Test
    void fluentIntWithoutRangeEmitsOnlyRangeWarning() {
        feed("25\n");
        Input.readInt().label("Age").read();
        String err = stderr();
        assertTrue(err.contains("no RANGE validation"), err);
        assertFalse(err.contains("no BLANK validation"), err);
        assertFalse(err.contains("no LENGTH validation"), err);
        assertFalse(err.contains("no FORMAT validation"), err);
        assertEquals(1, strictCount());
    }

    @Test
    void fluentIntWithPositiveEmitsZeroWarnings() {
        feed("42\n");
        int n = Input.readInt().label("Qty").positive().read();
        assertEquals(42, n);
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void fluentIntWithSkipRangeWarningEmitsNothing() {
        feed("42\n");
        Input.readInt().label("Qty").skipRangeWarning().read();
        assertEquals(0, strictCount(), stderr());
    }

    // ============================================================== fluent double

    @Test
    void fluentDoubleWithRangeEmitsZeroWarnings() {
        feed("3.14\n");
        double pi = Input.readDouble().label("Pi").between(0, 10).read();
        assertEquals(3.14, pi, 0.0001);
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void fluentDoubleWithoutRangeEmitsOnlyRangeWarning() {
        feed("1.5\n");
        Input.readDouble().label("X").read();
        assertEquals(1, strictCount());
        assertTrue(stderr().contains("no RANGE validation"), stderr());
    }

    // ============================================================== fluent date

    @Test
    void fluentDateWithRangeEmitsZeroWarnings() {
        feed("2026-10-31\n");
        LocalDate d = Input.readDate()
                .label("Dob")
                .between(LocalDate.of(1950, 1, 1), LocalDate.of(2100, 1, 1))
                .read();
        assertEquals(LocalDate.of(2026, 10, 31), d);
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void fluentDateWithoutRangeEmitsOnlyRangeWarning() {
        feed("2026-10-31\n");
        Input.readDate().label("D").read();
        assertEquals(1, strictCount());
    }

    // ============================================================== boolean / char

    @Test
    void booleanReaderEmitsNoStringWarnings() {
        feed("y\n");
        boolean result = Input.readBoolean().label("Continue").read();
        assertTrue(result);
        assertEquals(0, strictCount(),
                "BooleanReader must not emit string strict warnings:\n" + stderr());
    }

    @Test
    void booleanReaderPromptHasSingleColon() {
        feed("y\n");
        Input.readBoolean().label("Continue").read();
        String out = stdout();
        assertTrue(out.contains("Continue (y/n): "),
                "Expected 'Continue (y/n): ' in prompt, got: " + out);
        assertFalse(out.contains("Continue (y/n): : "),
                "Double colon bug — prompt was: " + out);
    }

    @Test
    void charReaderEmitsNoStringWarnings() {
        feed("A\n");
        char c = Input.readCharacter().label("Grade").read();
        assertEquals('A', c);
        assertEquals(0, strictCount(),
                "CharReader must not emit string strict warnings:\n" + stderr());
    }

    // ============================================================== immediate

    @Test
    void immediateReadLineEmitsThreeWarningsOncePerLabel() {
        feed("Saad\n");
        Input.readLine("Name");
        String err = stderr();
        assertTrue(err.contains("no BLANK validation"), err);
        assertTrue(err.contains("no LENGTH validation"), err);
        assertTrue(err.contains("no FORMAT validation"), err);
        assertEquals(3, strictCount());
    }

    @Test
    void immediateReadLineTwiceWithSameLabelWarnsOnce() {
        feed("Saad\nAli\n");
        Input.readLine("Name");
        Input.readLine("Name");
        assertEquals(3, strictCount(),
                "Warnings should be throttled per (type,label):\n" + stderr());
    }

    @Test
    void immediateReadLineTwiceWithDifferentLabelsWarnsTwice() {
        feed("Saad\nAli\n");
        Input.readLine("Name");
        Input.readLine("Surname");
        assertEquals(6, strictCount(),
                "Different labels should each warn once:\n" + stderr());
    }

    @Test
    void immediateReadIntWithRangeEmitsZeroWarnings() {
        feed("25\n");
        Input.readInt("Age", 1, 130);
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void immediateReadIntWithoutRangeEmitsOnlyRangeWarning() {
        feed("25\n");
        Input.readInt("Age");
        assertEquals(1, strictCount(), stderr());
        assertTrue(stderr().contains("no RANGE validation"), stderr());
    }

    // ============================================================== mixed use

    @Test
    void immediateAndFluentWithDifferentLabelsDoNotInterfere() {
        feed("Saad\nAli\n");
        Input.readLine("First");
        Input.readString()
                .label("Second")
                .notBlank()
                .atLeast(1)
                .atMost(50)
                .skipFormatWarning()
                .read();
        assertEquals(3, strictCount(),
                "Only the immediate readLine should warn:\n" + stderr());
    }

    // ============================================================== global toggle

    @Test
    void strictWarningsCanBeDisabledGlobally() {
        Config.disableStrictWarnings();
        feed("Saad\n");
        Input.readString().label("Name").read();
        assertEquals(0, strictCount(), stderr());
    }

    @Test
    void reEnablingStrictWarningsRestoresBehaviour() {
        Config.disableStrictWarnings();
        Config.enableStrictWarnings();
        Strict.reset();
        feed("Saad\n");
        Input.readString().label("Name").read();
        assertEquals(3, strictCount());
    }

    // ============================================================== Strict internals

    @Test
    void emitIsNotThrottled() {
        Strict.emit("string", "A", ValidationCategory.BLANK);
        Strict.emit("string", "A", ValidationCategory.BLANK);
        Strict.emit("string", "A", ValidationCategory.BLANK);
        assertEquals(3, strictCount(),
                "emit must fire every time — it is used by per-instance readers:\n" + stderr());
    }

    @Test
    void warnOnceThrottlesByTypeAndLabel() {
        Strict.warnOnce("string", "A", ValidationCategory.BLANK);
        Strict.warnOnce("string", "A", ValidationCategory.BLANK);
        Strict.warnOnce("string", "A", ValidationCategory.BLANK);
        assertEquals(1, strictCount(),
                "warnOnce should throttle per (type,label):\n" + stderr());
    }

    @Test
    void warnOnceWithMultipleCategoriesEmitsAll() {
        Strict.warnOnce("string", "A",
                ValidationCategory.BLANK,
                ValidationCategory.LENGTH,
                ValidationCategory.FORMAT);
        assertEquals(3, strictCount(), stderr());
    }

    @Test
    void resetClearsThrottle() {
        Strict.warnOnce("string", "A", ValidationCategory.BLANK);
        Strict.reset();
        Strict.warnOnce("string", "A", ValidationCategory.BLANK);
        assertEquals(2, strictCount(),
                "Strict.reset() should clear the throttle:\n" + stderr());
    }

    @Test
    void emitIsSilentWhenConfigDisabled() {
        Config.disableStrictWarnings();
        Strict.emit("string", "X", ValidationCategory.BLANK);
        Strict.warnOnce("string", "Y", ValidationCategory.BLANK);
        assertEquals(0, strictCount());
    }

    // ============================================================== readRaw contract

    @Test
    void readRawDoesNotWarn() {
        feed("anything\n");
        Input.readRaw("X");
        assertEquals(0, strictCount(),
                "readRaw must never emit warnings — it is the reader primitive:\n" + stderr());
    }

    @Test
    void readRawPrintsThePromptExactly() {
        feed("hello\n");
        String value = Input.readRaw("Your prompt");
        assertEquals("hello", value);
        assertEquals("Your prompt: ", stdout());
    }

    // ============================================================== edge cases

    @Test
    void readerWithDefaultValueStillWarnsIfNoValidation() {
        feed("\n");
        Input.readString().label("X").defaultValue("fallback").read();
        assertTrue(strictCount() > 0,
                "Even with a default value, missing validation should warn:\n" + stderr());
    }

    @Test
    void readerHonoursDefaultValueForBlankInput() {
        feed("\n");
        String result = Input.readString().label("X").defaultValue("fallback").read();
        assertEquals("fallback", result);
    }
}
