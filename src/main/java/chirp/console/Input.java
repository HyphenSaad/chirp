package chirp.console;

import chirp.console.input.BooleanReader;
import chirp.console.input.CharReader;
import chirp.console.input.DateReader;
import chirp.console.input.DateTimeReader;
import chirp.console.input.DoubleReader;
import chirp.console.input.IntReader;
import chirp.console.input.StringReader;
import chirp.console.input.TimeReader;
import chirp.datetime.DateTime;
import chirp.internal.EaseException;
import chirp.internal.Strict;
import chirp.internal.Trace;
import chirp.internal.ValidationCategory;
import chirp.startup.ChirpLifecycle;
import chirp.stats.Stats;
import chirp.text.Check;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Prompted, looping, exception-free input.
 *
 * <p>The other most-used class in Chirp. Wraps {@link System#in} with
 * retry-on-invalid-input, built-in validation, and a fluent builder
 * for the cases where the caller needs more control.</p>
 *
 * <h2>Two modes</h2>
 *
 * <h3>1. Immediate — pass a prompt, get the value</h3>
 * <p>{@link #readInt(String)}, {@link #readLine(String)},
 * {@link #readYesNo(String)}, and the other {@code readXxx(String)}
 * overloads. One call gives one answer, validated against the family's
 * default rules:</p>
 * <pre>{@code
 * int    age  = Input.readInt("Age", 1, 130);   // loops until valid
 * String name = Input.readLine("Name");          // any non-empty line
 * boolean ok  = Input.readYesNo("Continue?");    // y/n only
 * }</pre>
 *
 * <h3>2. Fluent — omit the prompt, chain a builder</h3>
 * <p>{@link #readInt()}, {@link #readLine()}, {@link #readDate()}, and
 * the other no-arg overloads return a builder that supports
 * customisation — label, min/max, custom validator, help text, default
 * value, and strict-mode opt-out — before {@code .read()}:</p>
 * <pre>{@code
 * int age = Input.readInt()
 *     .label("Age")
 *     .between(1, 130)
 *     .help("Enter your age in years")
 *     .read();
 *
 * String email = Input.readLine()
 *     .label("Email")
 *     .email()
 *     .read();
 * }</pre>
 *
 * <p>The fluent mode is what the reader types use under the hood — the
 * immediate methods are one-line wrappers that create a builder, call
 * one or two setters, and invoke {@code .read()}. Students move from
 * immediate to fluent as their validation needs grow.</p>
 *
 * <h2>Naming — the alias families</h2>
 * <p>Every read family has multiple names so students can write whichever
 * name comes to mind. For {@code int} alone there are nine aliases for
 * the immediate form: {@code readInt / readInteger / takeInt / getInt /
 * askInt / promptInt / scanInt / readNumber / readWhole}. All nine do
 * exactly the same thing — the aliases are pure delegates.</p>
 *
 * <p>The same pattern applies to every family:</p>
 * <ul>
 *   <li>String — {@code readLine / readString / readText / takeString /
 *       takeLine / getString / getLine / askLine / askString /
 *       inputLine / inputString / scanLine / scanString}</li>
 *   <li>Double — {@code readDouble / readDecimal / readFloat /
 *       takeDouble / getDouble / askDouble / inputDouble}</li>
 *   <li>Yes/no — {@code readYesNo / readBool / readConfirm / confirm /
 *       askConfirm / askYesNo / takeYesNo / inputYesNo}</li>
 *   <li>Char — {@code readChar / readLetter / readSingleChar /
 *       takeChar / getChar / askChar}</li>
 *   <li>Int range — {@code readIntRange / readBoundedInt /
 *       readIntBetween}</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <p>Every read is guarded by {@link ChirpLifecycle#requireStarted()},
 * so a student who forgets {@code Chirp.start()} gets a clear error
 * pointing at their first line of {@code main}. The one exception is
 * {@link #readRaw(String)}, which is the internal primitive used by
 * the reader builders — no guard, no warnings, returns the raw line.</p>
 *
 * <h2>Strict mode</h2>
 * <p>When strict mode is on (the default in interactive console mode),
 * each immediate reader warns to {@code stderr} the first time it is
 * used with a given prompt without validation. This teaches the habit
 * without blocking anything. Fluent-mode readers never warn — the
 * caller opted into the fluent API, which exists for explicit control.
 * Disable strict warnings entirely with
 * {@link chirp.config.Config#disableStrictWarnings()} or
 * {@code -Dchirp.strict=false}.</p>
 *
 * <h2>Encoding</h2>
 * <p>Both {@code System.in} and any custom {@link InputStream} set via
 * {@link #setInputStream(InputStream)} are decoded as UTF-8
 * explicitly. This is important on Windows, where the default charset
 * may be CP1252 or CP437 even when the terminal is rendering UTF-8.</p>
 *
 * <h2>Example — reading a form</h2>
 * <pre>{@code
 * String name  = Input.readLine("Name");
 * String email = Input.readEmail("Email");
 * int    age   = Input.readInt("Age", 1, 130);
 * boolean ok   = Input.readYesNo("Submit?");
 *
 * if (ok) {
 *     Console.success("Submitted for " + name);
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For output, see {@link Console}. For the individual reader builders,
 * see {@link IntReader}, {@link StringReader}, {@link DoubleReader},
 * {@link BooleanReader}, {@link CharReader}, {@link DateReader},
 * {@link TimeReader}, {@link DateTimeReader}.</p>
 *
 * @see Console
 * @see IntReader
 * @see StringReader
 * @see DoubleReader
 * @see BooleanReader
 * @see CharReader
 * @see DateReader
 * @see TimeReader
 * @see DateTimeReader
 * @see chirp.validation.Rule
 * @since 1.0.0
 */
public final class Input {

    private static Scanner SCANNER = new Scanner(System.in, StandardCharsets.UTF_8);

    private Input() {}

    // ============================================================== test hooks

    /**
     * Redirects input to a custom {@link InputStream} — for tests. The
     * stream is read as UTF-8.
     *
     * @param in the new input stream
     * @see #resetInputStream()
     * @since 1.0.0
     */
    public static void setInputStream(InputStream in) { SCANNER = new Scanner(in, StandardCharsets.UTF_8); }

    /**
     * Restores input to {@code System.in}. The canonical use is in a
     * test's {@code @AfterEach} to avoid cross-test interference.
     *
     * @see #setInputStream(InputStream)
     * @since 1.0.0
     */
    public static void resetInputStream() { SCANNER = new Scanner(System.in, StandardCharsets.UTF_8); }

    static String nextLine() {
        try {
            if (!SCANNER.hasNextLine()) {
                Trace.warn("input", "input stream ended unexpectedly");
                throw new EaseException("Input stream ended unexpectedly.");
            }
            String line = SCANNER.nextLine();
            Stats.recordLine(line.length());
            return line.trim();
        } catch (NoSuchElementException e) {
            Trace.warn("input", "NoSuchElementException caught: " + e.getMessage());
            throw new EaseException("Input stream ended unexpectedly.", e);
        }
    }

    /**
     * Reads a raw line, printing the prompt first. <b>Internal primitive</b>
     * — no lifecycle guard, no strict warnings, no retry.
     *
     * <p>Used internally by every reader builder. Exposed publicly so
     * that other classes (and tests) can read a line without triggering
     * the reader machinery. Most students should use
     * {@link #readLine(String)} or the fluent readers instead.</p>
     *
     * @param prompt the prompt text (a ": " is appended automatically)
     * @return the line, trimmed
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String readRaw(String prompt) {
        System.out.print(prompt + ": ");
        return nextLine();
    }

    // ============================================================== int

    /**
     * Reads a whole number. Retries on non-numeric input until a valid
     * number is entered. Requires {@code Chirp.start()}.
     *
     * <p>In strict mode, warns once per prompt if no range is applied.
     * For a range-validated variant, use
     * {@link #readInt(String, int, int)} — or the fluent builder via
     * {@link #readInt()}.</p>
     *
     * <pre>{@code
     * int age = Input.readInt("Age");
     * // Age: > twenty
     * // Please enter a whole number.
     * // Age: > 20
     * }</pre>
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readInt(String, int, int)
     * @see #readInt()
     * @see #readPositiveInt(String)
     * @see #readNonNegativeInt(String)
     * @since 1.0.0
     */
    public static int readInt(String prompt) {
        ChirpLifecycle.requireStarted();
        Trace.enter("input", "readInt", "prompt=" + prompt);
        Strict.warnOnce("int", prompt, ValidationCategory.RANGE);
        int value = readIntRaw(prompt);
        Trace.exit("input", "readInt", value);
        return value;
    }

    /**
     * Reads a whole number within the inclusive range {@code [min, max]}.
     * Retries on non-numeric or out-of-range input. Requires
     * {@code Chirp.start()}.
     *
     * <p>No strict-mode warning — the range is the point.</p>
     *
     * <pre>{@code
     * int age = Input.readInt("Age", 1, 130);
     * }</pre>
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value (inclusive)
     * @param max the maximum accepted value (inclusive)
     * @return the parsed integer
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readInt(String)
     * @see #readIntRange(String, int, int)
     * @since 1.0.0
     */
    public static int readInt(String prompt, int min, int max) {
        ChirpLifecycle.requireStarted();
        Trace.enter("input", "readInt", "prompt=" + prompt, "min=" + min, "max=" + max);
        while (true) {
            int value = readIntRaw(prompt);
            Trace.decision("input", "value(" + value + ") >= min(" + min + ")", value >= min);
            Trace.decision("input", "value(" + value + ") <= max(" + max + ")", value <= max);
            if (value >= min && value <= max) {
                Trace.exit("input", "readInt", value);
                return value;
            }
            Messenger.error("Please enter a value between " + min + " and " + max + ".");
        }
    }

    private static int readIntRaw(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readRaw(prompt));
            } catch (NumberFormatException e) {
                Messenger.error("Please enter a whole number.");
            }
        }
    }

    /**
     * Opens a fluent {@link IntReader} for building a custom int prompt.
     * Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * int age = Input.readInt()
     *     .label("Age")
     *     .between(1, 130)
     *     .help("Enter your age in years")
     *     .read();
     * }</pre>
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readInt(String)
     * @see #readInt(String, int, int)
     * @see IntReader
     * @since 1.0.0
     */
    public static IntReader readInt() {
        ChirpLifecycle.requireStarted();
        Trace.trace("input", "readInt() -> IntReader (fluent)");
        return IntReader.create();
    }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int readInteger(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int readInteger(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader readInteger() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int takeInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int takeInt(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader takeInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int getInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int getInt(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader getInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int askInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int askInt(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader askInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int inputInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int inputInt(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader inputInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int promptInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader promptInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int scanInt(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader scanInt() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int readNumber(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader readNumber() { return readInt(); }

    /**
     * Alias of {@link #readInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param prompt the prompt text
     * @return the parsed integer
     * @see #readInt(String)
     * @since 1.0.0
     */
    public static int readWhole(String prompt) { return readInt(prompt); }

    /**
     * Alias of {@link #readInt()} — see that method for full
     * documentation and examples.
     *
     * @return a new {@code IntReader}
     * @see #readInt()
     * @since 1.0.0
     */
    public static IntReader readWhole() { return readInt(); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int readIntRange(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int readBoundedInt(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Alias of {@link #readInt(String, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed integer
     * @see #readInt(String, int, int)
     * @since 1.0.0
     */
    public static int readIntBetween(String prompt, int min, int max) { return readInt(prompt, min, max); }

    /**
     * Reads a strictly positive integer (≥ 1). Retries until valid.
     *
     * @param prompt the prompt text
     * @return the positive integer
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readNonNegativeInt(String)
     * @see #readNonPositiveInt(String)
     * @since 1.0.0
     */
    public static int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Alias of {@link #readPositiveInt(String)} — see that method for
     * full documentation.
     *
     * @param prompt the prompt text
     * @return the positive integer
     * @see #readPositiveInt(String)
     * @since 1.0.0
     */
    public static int readPositiveNumber(String prompt) { return readPositiveInt(prompt); }

    /**
     * Reads a non-negative integer (≥ 0). Retries until valid.
     *
     * @param prompt the prompt text
     * @return the non-negative integer
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readPositiveInt(String)
     * @since 1.0.0
     */
    public static int readNonNegativeInt(String prompt) {
        return readInt(prompt, 0, Integer.MAX_VALUE);
    }

    /**
     * Alias of {@link #readPositiveInt(String)} — see that method for
     * full documentation. The name expresses "natural numbers" —
     * strictly positive integers — which is one of the many names
     * teachers use.
     *
     * @param prompt the prompt text
     * @return the positive integer
     * @see #readPositiveInt(String)
     * @since 1.0.0
     */
    public static int readNatural(String prompt) { return readPositiveInt(prompt); }

    /**
     * Reads a non-positive integer (≤ 0). Retries until valid.
     *
     * @param prompt the prompt text
     * @return the non-positive integer
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readNonNegativeInt(String)
     * @since 1.0.0
     */
    public static int readNonPositiveInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, 0);
    }

    // ============================================================== double

    /**
     * Reads a decimal number. Retries on non-numeric input. Requires
     * {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDouble(String, double, double)
     * @see #readDouble()
     * @see #readPositiveDouble(String)
     * @since 1.0.0
     */
    public static double readDouble(String prompt) {
        ChirpLifecycle.requireStarted();
        Strict.warnOnce("double", prompt, ValidationCategory.RANGE);
        return readDoubleRaw(prompt);
    }

    /**
     * Reads a decimal number within the inclusive range {@code [min, max]}.
     * Retries on non-numeric or out-of-range input.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed double
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double readDouble(String prompt, double min, double max) {
        ChirpLifecycle.requireStarted();
        while (true) {
            double value = readDoubleRaw(prompt);
            if (value >= min && value <= max) return value;
            Messenger.error("Please enter a value between " + min + " and " + max + ".");
        }
    }

    private static double readDoubleRaw(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readRaw(prompt));
            } catch (NumberFormatException e) {
                Messenger.error("Please enter a decimal number.");
            }
        }
    }

    /**
     * Opens a fluent {@link DoubleReader} for building a custom decimal
     * prompt.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDouble(String)
     * @see DoubleReader
     * @since 1.0.0
     */
    public static DoubleReader readDouble() {
        ChirpLifecycle.requireStarted();
        return DoubleReader.create();
    }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double readDecimal(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader readDecimal() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double readFloat(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader readFloat() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double takeDouble(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader takeDouble() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double getDouble(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader getDouble() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double askDouble(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader askDouble() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see #readDouble(String)
     * @since 1.0.0
     */
    public static double inputDouble(String prompt) { return readDouble(prompt); }

    /**
     * Alias of {@link #readDouble()} — see that method for full
     * documentation.
     *
     * @return a new {@code DoubleReader}
     * @see #readDouble()
     * @since 1.0.0
     */
    public static DoubleReader inputDouble() { return readDouble(); }

    /**
     * Alias of {@link #readDouble(String, double, double)} — see that
     * method for full documentation.
     *
     * @param prompt the prompt text
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the parsed double
     * @see #readDouble(String, double, double)
     * @since 1.0.0
     */
    public static double readDoubleRange(String prompt, double min, double max) {
        return readDouble(prompt, min, max);
    }

    /**
     * Reads a strictly positive double (> 0). Retries until valid.
     *
     * @param prompt the prompt text
     * @return the positive double
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @since 1.0.0
     */
    public static double readPositiveDouble(String prompt) {
        return readDouble(prompt, 0.0000001, Double.MAX_VALUE);
    }

    // ============================================================== string

    /**
     * Reads one line of text. Retries on empty input only if the caller
     * validated — the immediate form accepts any line, including empty.
     * Requires {@code Chirp.start()}.
     *
     * <p>In strict mode, warns once per prompt if no blank/length/format
     * validation is applied. Use the fluent form via {@link #readLine()}
     * for validation.</p>
     *
     * @param prompt the prompt text
     * @return the line, trimmed
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readLine()
     * @see #readNonEmpty(String)
     * @see #readEmail(String)
     * @since 1.0.0
     */
    public static String readLine(String prompt) {
        ChirpLifecycle.requireStarted();
        Strict.warnOnce("string", prompt,
                ValidationCategory.BLANK, ValidationCategory.LENGTH, ValidationCategory.FORMAT);
        return readRaw(prompt);
    }

    /**
     * Opens a fluent {@link StringReader} for building a custom text
     * prompt with validation (not blank, min/max length, regex, email,
     * phone, URL, custom rule, etc.).
     *
     * <pre>{@code
     * String email = Input.readLine()
     *     .label("Email")
     *     .email()
     *     .read();
     * }</pre>
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readLine(String)
     * @see StringReader
     * @since 1.0.0
     */
    public static StringReader readLine() {
        ChirpLifecycle.requireStarted();
        return StringReader.create();
    }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String readString(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader readString() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String readText(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader readText() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String takeString(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader takeString() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String takeLine(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader takeLine() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String getString(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader getString() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String getLine(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader getLine() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String askLine(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader askLine() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String askString(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader askString() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String inputLine(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader inputLine() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String inputString(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine()} — see that method for full
     * documentation.
     *
     * @return a new {@code StringReader}
     * @see #readLine()
     * @since 1.0.0
     */
    public static StringReader inputString() { return readLine(); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String scanLine(String prompt) { return readLine(prompt); }

    /**
     * Alias of {@link #readLine(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the line
     * @see #readLine(String)
     * @since 1.0.0
     */
    public static String scanString(String prompt) { return readLine(prompt); }

    /**
     * Reads a non-empty line — retries until the user types at least one
     * character. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the non-empty line
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readLine(String)
     * @see #readRequired(String)
     * @since 1.0.0
     */
    public static String readNonEmpty(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (!value.isEmpty()) return value;
            Messenger.error("This field cannot be empty.");
        }
    }

    /**
     * Alias of {@link #readNonEmpty(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the non-empty line
     * @see #readNonEmpty(String)
     * @since 1.0.0
     */
    public static String readRequired(String prompt) { return readNonEmpty(prompt); }

    /**
     * Alias of {@link #readNonEmpty(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the non-empty line
     * @see #readNonEmpty(String)
     * @since 1.0.0
     */
    public static String readLineNonEmpty(String prompt) { return readNonEmpty(prompt); }

    // ============================================================== boolean

    /**
     * Reads a yes/no answer. Accepts only {@code y/yes} and {@code n/no}
     * (case-insensitive) — retries on anything else. Requires
     * {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return true for yes, false for no
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readBoolean()
     * @see #readConfirm(String)
     * @since 1.0.0
     */
    public static boolean readYesNo(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt + " (y/n)").toLowerCase(Locale.ROOT);
            if (value.equals("y") || value.equals("yes")) return true;
            if (value.equals("n") || value.equals("no")) return false;
            Messenger.error("Please type y or n.");
        }
    }

    /**
     * Opens a fluent {@link BooleanReader} for building a custom yes/no
     * prompt with a default value and custom validator.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readYesNo(String)
     * @see BooleanReader
     * @since 1.0.0
     */
    public static BooleanReader readBoolean() {
        ChirpLifecycle.requireStarted();
        return BooleanReader.create();
    }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean readBool(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readBoolean()} — see that method for full
     * documentation.
     *
     * @return a new {@code BooleanReader}
     * @see #readBoolean()
     * @since 1.0.0
     */
    public static BooleanReader readBool() { return readBoolean(); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean readConfirm(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean confirm(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean askConfirm(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean askYesNo(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean takeYesNo(String prompt) { return readYesNo(prompt); }

    /**
     * Alias of {@link #readYesNo(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the boolean answer
     * @see #readYesNo(String)
     * @since 1.0.0
     */
    public static boolean inputYesNo(String prompt) { return readYesNo(prompt); }

    // ============================================================== char

    /**
     * Reads exactly one character — retries if the user types zero or
     * more than one. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the single character
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readCharacter()
     * @since 1.0.0
     */
    public static char readChar(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (value.length() == 1) return value.charAt(0);
            Messenger.error("Please enter exactly one character.");
        }
    }

    /**
     * Opens a fluent {@link CharReader} for building a custom
     * single-character prompt with an allow-list and custom validator.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readChar(String)
     * @see CharReader
     * @since 1.0.0
     */
    public static CharReader readCharacter() {
        ChirpLifecycle.requireStarted();
        return CharReader.create();
    }

    /**
     * Alias of {@link #readChar(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the single character
     * @see #readChar(String)
     * @since 1.0.0
     */
    public static char readLetter(String prompt) { return readChar(prompt); }

    /**
     * Alias of {@link #readChar(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the single character
     * @see #readChar(String)
     * @since 1.0.0
     */
    public static char readSingleChar(String prompt) { return readChar(prompt); }

    /**
     * Alias of {@link #readChar(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the single character
     * @see #readChar(String)
     * @since 1.0.0
     */
    public static char takeChar(String prompt) { return readChar(prompt); }

    /**
     * Alias of {@link #readChar(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the single character
     * @see #readChar(String)
     * @since 1.0.0
     */
    public static char getChar(String prompt) { return readChar(prompt); }

    /**
     * Alias of {@link #readChar(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the single character
     * @see #readChar(String)
     * @since 1.0.0
     */
    public static char askChar(String prompt) { return readChar(prompt); }

    // ============================================================== password

    /**
     * Reads a password without echoing the characters (when running in a
     * real terminal — falls back to visible input when not). Requires
     * {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the password
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #askPassword(String)
     * @since 1.0.0
     */
    public static String readPassword(String prompt) {
        ChirpLifecycle.requireStarted();
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt + ": ");
            return chars == null ? "" : new String(chars);
        }
        return readRaw(prompt);
    }

    /**
     * Alias of {@link #readPassword(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the password
     * @see #readPassword(String)
     * @since 1.0.0
     */
    public static String askPassword(String prompt) { return readPassword(prompt); }

    /**
     * Alias of {@link #readPassword(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the password
     * @see #readPassword(String)
     * @since 1.0.0
     */
    public static String getPassword(String prompt) { return readPassword(prompt); }

    /**
     * Alias of {@link #readPassword(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the password
     * @see #readPassword(String)
     * @since 1.0.0
     */
    public static String inputPassword(String prompt) { return readPassword(prompt); }

    // ============================================================== domain

    /**
     * Reads an email address — retries until the input passes
     * {@link Check#isEmail(String)}. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the valid email
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readLine()
     * @see Check#isEmail(String)
     * @see chirp.validation.Rules#email()
     * @since 1.0.0
     */
    public static String readEmail(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isEmail(value)) return value;
            Messenger.error("That doesn't look like an email address.");
        }
    }

    /**
     * Reads a phone number — retries until the input passes
     * {@link Check#isPhone(String)}. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the valid phone number
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see Check#isPhone(String)
     * @since 1.0.0
     */
    public static String readPhone(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isPhone(value)) return value;
            Messenger.error("That doesn't look like a phone number.");
        }
    }

    /**
     * Reads a URL — retries until the input passes
     * {@link Check#isURL(String)}. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the valid URL
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readUrl(String)
     * @see Check#isURL(String)
     * @since 1.0.0
     */
    public static String readURL(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isURL(value)) return value;
            Messenger.error("That doesn't look like a URL.");
        }
    }

    /**
     * Alias of {@link #readURL(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid URL
     * @see #readURL(String)
     * @since 1.0.0
     */
    public static String readUrl(String prompt) { return readURL(prompt); }

    /**
     * Alias of {@link #readEmail(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid email
     * @see #readEmail(String)
     * @since 1.0.0
     */
    public static String askEmail(String prompt) { return readEmail(prompt); }

    /**
     * Alias of {@link #readPhone(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid phone
     * @see #readPhone(String)
     * @since 1.0.0
     */
    public static String askPhone(String prompt) { return readPhone(prompt); }

    /**
     * Alias of {@link #readURL(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid URL
     * @see #readURL(String)
     * @since 1.0.0
     */
    public static String askURL(String prompt) { return readURL(prompt); }

    /**
     * Alias of {@link #readEmail(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid email
     * @see #readEmail(String)
     * @since 1.0.0
     */
    public static String takeEmail(String prompt) { return readEmail(prompt); }

    /**
     * Alias of {@link #readEmail(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the valid email
     * @see #readEmail(String)
     * @since 1.0.0
     */
    public static String getEmail(String prompt) { return readEmail(prompt); }

    // ============================================================== date / time

    /**
     * Reads a date — retries until the input parses as a
     * {@link LocalDate}. Accepts ISO, DMY, MDY, and common named-month
     * formats — see {@link chirp.datetime.DateTime#parseDate(String)}
     * for the full list. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the parsed date
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDate()
     * @see DateReader
     * @see chirp.datetime.DateTime#parseDate(String)
     * @since 1.0.0
     */
    public static LocalDate readDate(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalDate date = DateTime.parseDate(value);
            if (date != null) return date;
            Messenger.error("Please enter a valid date (e.g. 2026-10-31).");
        }
    }

    /**
     * Opens a fluent {@link DateReader} for building a custom date prompt
     * with min/max range validation.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDate(String)
     * @see DateReader
     * @since 1.0.0
     */
    public static DateReader readDate() {
        ChirpLifecycle.requireStarted();
        return DateReader.create();
    }

    /**
     * Reads a time — retries until the input parses as a
     * {@link LocalTime}. Accepts 24-hour and 12-hour formats. Requires
     * {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the parsed time
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readTime()
     * @see TimeReader
     * @since 1.0.0
     */
    public static LocalTime readTime(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalTime time = DateTime.parseTime(value);
            if (time != null) return time;
            Messenger.error("Please enter a valid time (e.g. 14:30).");
        }
    }

    /**
     * Opens a fluent {@link TimeReader} for building a custom time
     * prompt with min/max range validation.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readTime(String)
     * @see TimeReader
     * @since 1.0.0
     */
    public static TimeReader readTime() {
        ChirpLifecycle.requireStarted();
        return TimeReader.create();
    }

    /**
     * Reads a date-and-time — retries until the input parses as a
     * {@link LocalDateTime}. Requires {@code Chirp.start()}.
     *
     * @param prompt the prompt text
     * @return the parsed date-time
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDateTime()
     * @see DateTimeReader
     * @since 1.0.0
     */
    public static LocalDateTime readDateTime(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalDateTime dt = DateTime.parseDateTime(value);
            if (dt != null) return dt;
            Messenger.error("Please enter a valid date and time.");
        }
    }

    /**
     * Opens a fluent {@link DateTimeReader} for building a custom
     * date-time prompt with min/max range validation.
     *
     * @return a new builder
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readDateTime(String)
     * @see DateTimeReader
     * @since 1.0.0
     */
    public static DateTimeReader readDateTime() {
        ChirpLifecycle.requireStarted();
        return DateTimeReader.create();
    }

    /**
     * Alias of {@link #readDate(String)} — see that method for full
     * documentation.
     *
     * @param prompt the prompt text
     * @return the parsed date
     * @see #readDate(String)
     * @since 1.0.0
     */
    public static LocalDate readDay(String prompt) { return readDate(prompt); }

    // ============================================================== choice

    /**
     * Presents a numbered list of options and reads the user's choice as
     * an integer index. Returns the <b>text</b> of the selected option
     * (not the index). Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * String plan = Input.readChoice("Pick a plan",
     *     "Free", "Pro", "Enterprise");
     * }</pre>
     *
     * @param prompt the header shown above the options
     * @param options the choices (must contain at least one)
     * @return the selected option text
     * @throws EaseException if {@code options} is null or empty
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readChoiceIndex(String, String...)
     * @see #choose(String, String...)
     * @since 1.0.0
     */
    public static String readChoice(String prompt, String... options) {
        ChirpLifecycle.requireStarted();
        if (options == null || options.length == 0) {
            throw new EaseException("readChoice requires at least one option.");
        }
        Console.println(prompt);
        for (int i = 0; i < options.length; i++) {
            Console.println("  " + (i + 1) + ") " + options[i]);
        }
        int choice = readInt("Your choice", 1, options.length);
        return options[choice - 1];
    }

    /**
     * Alias of {@link #readChoice(String, String...)} — see that method
     * for full documentation and examples.
     *
     * @param prompt the prompt
     * @param options the choices
     * @return the selected option
     * @see #readChoice(String, String...)
     * @since 1.0.0
     */
    public static String choose(String prompt, String... options) { return readChoice(prompt, options); }

    /**
     * Alias of {@link #readChoice(String, String...)} — see that method
     * for full documentation and examples.
     *
     * @param prompt the prompt
     * @param options the choices
     * @return the selected option
     * @see #readChoice(String, String...)
     * @since 1.0.0
     */
    public static String pickOption(String prompt, String... options) { return readChoice(prompt, options); }

    /**
     * Alias of {@link #readChoice(String, String...)} — see that method
     * for full documentation and examples.
     *
     * @param prompt the prompt
     * @param options the choices
     * @return the selected option
     * @see #readChoice(String, String...)
     * @since 1.0.0
     */
    public static String readMenu(String prompt, String... options) { return readChoice(prompt, options); }

    /**
     * Alias of {@link #readChoice(String, String...)} — see that method
     * for full documentation and examples.
     *
     * @param prompt the prompt
     * @param options the choices
     * @return the selected option
     * @see #readChoice(String, String...)
     * @since 1.0.0
     */
    public static String readOption(String prompt, String... options) { return readChoice(prompt, options); }

    /**
     * Presents a numbered list of options and returns the selected
     * option's zero-based index.
     *
     * @param prompt the header shown above the options
     * @param options the choices (must contain at least one)
     * @return the selected index (0-based)
     * @throws EaseException if {@code options} is null or empty
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readChoice(String, String...)
     * @see #readChoiceNumber(String, String...)
     * @since 1.0.0
     */
    public static int readChoiceIndex(String prompt, String... options) {
        ChirpLifecycle.requireStarted();
        if (options == null || options.length == 0) {
            throw new EaseException("readChoiceIndex requires at least one option.");
        }
        Console.println(prompt);
        for (int i = 0; i < options.length; i++) {
            Console.println("  " + (i + 1) + ") " + options[i]);
        }
        return readInt("Your choice", 1, options.length) - 1;
    }

    /**
     * Alias of {@link #readChoiceIndex(String, String...)} returning
     * the one-based index instead.
     *
     * @param prompt the prompt
     * @param options the choices
     * @return the selected index (1-based)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #readChoiceIndex(String, String...)
     * @since 1.0.0
     */
    public static int readChoiceNumber(String prompt, String... options) {
        return readChoiceIndex(prompt, options) + 1;
    }
}
