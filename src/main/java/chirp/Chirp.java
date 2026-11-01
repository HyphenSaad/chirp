package chirp;

import chirp.collection.ArrayChain;
import chirp.collection.ArraysUtil;
import chirp.collection.Filter;
import chirp.collection.Page;
import chirp.collection.Pagination;
import chirp.collection.Search;
import chirp.console.Console;
import chirp.console.Input;
import chirp.console.Loader;
import chirp.console.Menu;
import chirp.console.input.BooleanReader;
import chirp.console.input.CharReader;
import chirp.console.input.DateReader;
import chirp.console.input.DateTimeReader;
import chirp.console.input.DoubleReader;
import chirp.console.input.IntReader;
import chirp.console.input.StringReader;
import chirp.console.input.TimeReader;
import chirp.datetime.DateTime;
import chirp.decorator.Decorator;
import chirp.decorator.Retry;
import chirp.decorator.Timed;
import chirp.format.Format;
import chirp.http.Http;
import chirp.http.HttpMethod;
import chirp.http.HttpRequest;
import chirp.http.HttpResponse;
import chirp.io.CsvFile;
import chirp.io.FileIO;
import chirp.io.JsonFile;
import chirp.io.TextFileIO;
import chirp.mail.Email;
import chirp.mail.EmailRequest;
import chirp.mail.EmailResult;
import chirp.number.Numbers;
import chirp.session.Session;
import chirp.startup.ChirpLifecycle;
import chirp.startup.StartupConfig;
import chirp.text.Check;
import chirp.text.Text;
import chirp.text.TextChain;
import chirp.validation.Rule;
import chirp.validation.Rules;
import chirp.validation.ValidationResult;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Chirp's single-import facade — one class that exposes the whole
 * library.
 *
 * <p>Students write one static import and get everything:</p>
 * <pre>{@code
 * import static chirp.Chirp.*;
 *
 * public class Main {
 *     public static void main(String[] args) {
 *         start();
 *         int age = readInt("Age", 1, 130);
 *         success("Hello, " + readLine("Name") + "! You are " + age + ".");
 *         exit(0);
 *     }
 * }
 * }</pre>
 *
 * <h2>What this class is (and is not)</h2>
 * <p>{@code Chirp} is a <b>facade</b>. Every one of its ~700 methods
 * delegates to a canonical implementation in one of the library's
 * modules. The class is a convenience routing layer, not a separate
 * implementation — the behaviour of {@code Chirp.readInt(prompt)} is
 * byte-for-byte identical to {@code Input.readInt(prompt)}.</p>
 *
 * <p><b>Use the facade for teaching and quick programs.</b> For larger
 * applications, use the module classes directly — they are cleaner at
 * scale, have full Javadoc with examples, and can be
 * unit-tested in isolation.</p>
 *
 * <h2>The 22 sections</h2>
 * <p>Every section delegates to one module. Click a class to see the
 * full documentation of the canonical implementations:</p>
 * <table border="1">
 *   <caption>Section → module delegation map</caption>
 *   <tr><th>Section</th><th>Delegates to</th></tr>
 *   <tr><td>lifecycle</td><td>{@link ChirpLifecycle},
 *       {@link StartupConfig}</td></tr>
 *   <tr><td>input — int / double / string / boolean / char /
 *       domain / date-time</td><td>{@link Input}, and the fluent
 *       readers in {@code chirp.console.input}</td></tr>
 *   <tr><td>output</td><td>{@link Console}</td></tr>
 *   <tr><td>format</td><td>{@link Format}</td></tr>
 *   <tr><td>date / time</td><td>{@link DateTime}</td></tr>
 *   <tr><td>numbers</td><td>{@link Numbers}</td></tr>
 *   <tr><td>arrays</td><td>{@link ArraysUtil}</td></tr>
 *   <tr><td>search / filter / page</td><td>{@link Search},
 *       {@link Filter}, {@link Pagination}</td></tr>
 *   <tr><td>text</td><td>{@link Text}</td></tr>
 *   <tr><td>checks</td><td>{@link Check}</td></tr>
 *   <tr><td>decorators</td><td>{@link Decorator}</td></tr>
 *   <tr><td>session / rules</td><td>{@link Session}, {@link Rules}</td></tr>
 *   <tr><td>http</td><td>{@link Http}</td></tr>
 *   <tr><td>email</td><td>{@link Email}</td></tr>
 *   <tr><td>file management</td><td>{@link FileIO},
 *       {@link TextFileIO}, {@link CsvFile}, {@link JsonFile}</td></tr>
 *   <tr><td>console extras</td><td>{@link Loader}, {@link Menu}</td></tr>
 *   <tr><td>misc</td><td>native {@code System} calls wrapped for
 *       convenience</td></tr>
 * </table>
 *
 * <h2>Documentation strategy</h2>
 * <p>Because every method is a delegation, the full Javadoc (examples,
 * edge cases, null behaviour) lives in the canonical implementation.
 * Each method here carries a one-line summary and a {@code @see} tag
 * pointing at the canonical method. In any IDE, Ctrl+Click or F3 on
 * {@code Chirp.readInt} navigates straight to {@code Input.readInt} —
 * where the full documentation waits.</p>
 *
 * <p>The handful of methods that <b>do</b> have logic —
 * {@link #start(String...) start(String...)},
 * {@link #start(Consumer) start(Consumer)},
 * {@link #validate(String, String) validate(...)},
 * {@link #filterByCheck filterByCheck(...)},
 * {@link #sleep(int) sleep(...)} — carry full Javadoc here because
 * there is no canonical implementation to link to.</p>
 *
 * <h2>Aliases — the point of the class</h2>
 * <p>Every canonical method has 2–4 aliases. {@code readInt},
 * {@code readInteger}, {@code takeInt}, {@code getInt}, {@code askInt},
 * {@code promptInt}, {@code scanInt}, {@code readNumber},
 * {@code readWhole} all call the same underlying
 * {@code Input.readInt(...)}. The aliases exist so a student can write
 * whichever name comes to mind without an IDE lookup. In this Javadoc,
 * the first occurrence of each behavior gets the canonical
 * documentation; aliases below it get a one-line {@code @see} to the
 * canonical.</p>
 *
 * <h2>Related</h2>
 * <p>For the modules that this facade exposes, see the links above. For
 * the lifecycle wrapper, see {@link ChirpLifecycle}. For configuration
 * of colours, strict warnings, tracing, and stats, see
 * {@link chirp.config.Config}. For the interactive startup sequence,
 * see {@link StartupConfig}.</p>
 *
 * @see ChirpLifecycle
 * @see StartupConfig
 * @see chirp.config.Config
 * @see chirp.console.Input
 * @see chirp.console.Console
 * @see chirp.console.Menu
 * @see chirp.text.Text
 * @see chirp.number.Numbers
 * @see chirp.collection.ArraysUtil
 * @since 1.0.0
 */
public final class Chirp {

    private Chirp() {
    }

    // ================================================================ lifecycle

    /**
     * Starts Chirp with the default startup configuration — banner,
     * strict warnings, exit summary, friendly console messages. See
     * {@link ChirpLifecycle#start(StartupConfig)} for the full startup
     * sequence.
     *
     * @see #start(StartupConfig)
     * @see #start(String...)
     * @see #start(Consumer)
     * @since 1.0.0
     */
    public static void start() {
        ChirpLifecycle.start(StartupConfig.defaults());
    }

    /**
     * Starts Chirp with a specific configuration object. See
     * {@link ChirpLifecycle#start(StartupConfig)} for the full startup
     * sequence, and {@link StartupConfig.Builder} for the configuration
     * options.
     *
     * @param config the startup configuration
     * @see #start()
     * @see StartupConfig
     * @since 1.0.0
     */
    public static void start(StartupConfig config) {
        ChirpLifecycle.start(config);
    }

    /**
     * Starts Chirp, letting the caller customise the configuration
     * builder before it is finalised. A null customizer is treated as
     * "no customisation" — default configuration applies.
     *
     * @param customizer a lambda that mutates the builder (null → no-op)
     * @see #start()
     * @see #start(StartupConfig)
     * @since 1.0.0
     */
    public static void start(Consumer<StartupConfig.Builder> customizer) {
        StartupConfig.Builder builder = StartupConfig.builder();
        if (customizer != null)
            customizer.accept(builder);
        ChirpLifecycle.start(builder.build());
    }

    /**
     * Starts Chirp with command-line flags. Recognised flags (case-insensitive, null-safe):
     * <ul>
     *   <li>{@code noheader / nobanner / silent / quiet} — no banner</li>
     *   <li>{@code header / banner} — with banner</li>
     *   <li>{@code stats} / {@code nostats / withoutstats} — exit summary on/off</li>
     *   <li>{@code color / colour} / {@code nocolor / nocolour / plain} — ANSI colour on/off</li>
     *   <li>{@code unicode} / {@code nounicode / ascii} — glyph style</li>
     *   <li>{@code trace / debug} — enable trace mode</li>
     *   <li>{@code strict} / {@code lenient} — strict warnings on/off</li>
     * </ul>
     * <p>Unknown flags are silently ignored. A null or empty flag array
     * produces the default configuration.</p>
     *
     * @param flags the flags (null → default configuration)
     * @see #start()
     * @see #start(StartupConfig)
     * @since 1.0.0
     */
    public static void start(String... flags) {
        StartupConfig.Builder builder = StartupConfig.builder();
        if (flags != null) {
            for (String flag : flags) {
                if (flag == null)
                    continue;
                switch (flag.toLowerCase()) {
                    case "noheader", "nobanner", "silent", "quiet" -> builder.noHeader();
                    case "header", "banner" -> builder.withHeader();
                    case "stats" -> builder.withStats();
                    case "nostats", "withoutstats" -> builder.noStats();
                    case "color", "colour" -> builder.withColor();
                    case "nocolor", "nocolour", "plain" -> builder.noColor();
                    case "unicode" -> builder.withUnicode();
                    case "nounicode", "ascii" -> builder.noUnicode();
                    case "trace", "debug" -> builder.withTrace();
                    case "strict" -> builder.withStrictWarnings();
                    case "lenient" -> builder.noStrictWarnings();
                    default -> {
                    }
                }
            }
        }
        ChirpLifecycle.start(builder.build());
    }

    /** @see #start() */
    public static void init() { start(); }
    /** @see #start() */
    public static void boot() { start(); }
    /** @see #start() */
    public static void setup() { start(); }
    /** @see #start() */
    public static void begin() { start(); }
    /** @see #start() */
    public static void initialize() { start(); }
    /** @see #start() */
    public static void startChirp() { start(); }
    /** @see #start() */
    public static void bootChirp() { start(); }
    /** @see #start() */
    public static void beginChirp() { start(); }
    /** @see #start() */
    public static void initChirp() { start(); }
    /** @see #start() */
    public static void setupChirp() { start(); }
    /** @see #start() */
    public static void initializeChirp() { start(); }
    /** @see #start(StartupConfig) */
    public static void init(StartupConfig c) { start(c); }
    /** @see #start(StartupConfig) */
    public static void boot(StartupConfig c) { start(c); }
    /** @see #start(StartupConfig) */
    public static void setup(StartupConfig c) { start(c); }
    /** @see #start(String...) */
    public static void init(String... flags) { start(flags); }
    /** @see #start(String...) */
    public static void boot(String... flags) { start(flags); }

    /**
     * Whether {@link #start()} (or an alias) has already run. See
     * {@link ChirpLifecycle#isStarted()}.
     *
     * @return {@code true} if Chirp has started
     * @see ChirpLifecycle#isStarted()
     * @since 1.0.0
     */
    public static boolean isStarted() {
        return ChirpLifecycle.isStarted();
    }

    /** @see #isStarted() */
    public static boolean hasStarted() { return ChirpLifecycle.isStarted(); }

    // ================================================================ input — int

    /**
     * Reads an {@code int} with a prompt. Delegates to
     * {@link Input#readInt(String)}.
     *
     * @param prompt the prompt text
     * @return the parsed int
     * @see Input#readInt(String)
     * @since 1.0.0
     */
    public static int readInt(String prompt) { return Input.readInt(prompt); }

    /**
     * Reads an {@code int} in the given inclusive range. Delegates to
     * {@link Input#readInt(String, int, int)}.
     *
     * @param prompt the prompt text
     * @param min the minimum (inclusive)
     * @param max the maximum (inclusive)
     * @return the parsed int
     * @see Input#readInt(String, int, int)
     * @since 1.0.0
     */
    public static int readInt(String prompt, int min, int max) { return Input.readInt(prompt, min, max); }

    /**
     * Opens a fluent {@link IntReader} for chained validation. Delegates
     * to {@link Input#readInt()}.
     *
     * @return a new IntReader
     * @see Input#readInt()
     * @since 1.0.0
     */
    public static IntReader readInt() { return Input.readInt(); }

    /** @see #readInt(String) */
    public static int readInteger(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt(String, int, int) */
    public static int readInteger(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }
    /** @see #readInt() */
    public static IntReader readInteger() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int takeInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt(String, int, int) */
    public static int takeInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }
    /** @see #readInt() */
    public static IntReader takeInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int getInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt(String, int, int) */
    public static int getInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }
    /** @see #readInt() */
    public static IntReader getInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int askInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt(String, int, int) */
    public static int askInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }
    /** @see #readInt() */
    public static IntReader askInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int inputInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt(String, int, int) */
    public static int inputInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }
    /** @see #readInt() */
    public static IntReader inputInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int promptInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt() */
    public static IntReader promptInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int scanInt(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt() */
    public static IntReader scanInt() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int readNumber(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt() */
    public static IntReader readNumber() { return Input.readInt(); }
    /** @see #readInt(String) */
    public static int readWhole(String prompt) { return Input.readInt(prompt); }
    /** @see #readInt() */
    public static IntReader readWhole() { return Input.readInt(); }

    /** @see Input#readPositiveInt(String) */
    public static int readPositiveInt(String prompt) {
        return Input.readPositiveInt(prompt);
    }
    /** @see Input#readNonNegativeInt(String) */
    public static int readNonNegativeInt(String prompt) {
        return Input.readNonNegativeInt(prompt);
    }

    // ================================================================ input — double

    /**
     * Reads a {@code double} with a prompt. Delegates to
     * {@link Input#readDouble(String)}.
     *
     * @param prompt the prompt text
     * @return the parsed double
     * @see Input#readDouble(String)
     * @since 1.0.0
     */
    public static double readDouble(String prompt) { return Input.readDouble(prompt); }

    /**
     * Reads a {@code double} in the given inclusive range. Delegates to
     * {@link Input#readDouble(String, double, double)}.
     *
     * @param prompt the prompt text
     * @param lo the minimum (inclusive)
     * @param hi the maximum (inclusive)
     * @return the parsed double
     * @see Input#readDouble(String, double, double)
     * @since 1.0.0
     */
    public static double readDouble(String prompt, double lo, double hi) { return Input.readDouble(prompt, lo, hi); }

    /**
     * Opens a fluent {@link DoubleReader}. Delegates to
     * {@link Input#readDouble()}.
     *
     * @return a new DoubleReader
     * @see Input#readDouble()
     * @since 1.0.0
     */
    public static DoubleReader readDouble() { return Input.readDouble(); }

    /** @see #readDouble(String) */
    public static double readDecimal(String prompt) { return Input.readDouble(prompt); }
    /** @see #readDouble() */
    public static DoubleReader readDecimal() { return Input.readDouble(); }
    /** @see #readDouble(String) */
    public static double readFloat(String prompt) { return Input.readDouble(prompt); }
    /** @see #readDouble() */
    public static DoubleReader readFloat() { return Input.readDouble(); }
    /** @see #readDouble(String) */
    public static double takeDouble(String prompt) { return Input.readDouble(prompt); }
    /** @see #readDouble() */
    public static DoubleReader takeDouble() { return Input.readDouble(); }
    /** @see #readDouble(String) */
    public static double getDouble(String prompt) { return Input.readDouble(prompt); }
    /** @see #readDouble() */
    public static DoubleReader getDouble() { return Input.readDouble(); }
    /** @see #readDouble(String) */
    public static double askDouble(String prompt) { return Input.readDouble(prompt); }
    /** @see #readDouble() */
    public static DoubleReader askDouble() { return Input.readDouble(); }

    // ================================================================ input — string

    /**
     * Reads a line of text with a prompt. Delegates to
     * {@link Input#readLine(String)}.
     *
     * @param prompt the prompt text
     * @return the trimmed input line
     * @see Input#readLine(String)
     * @since 1.0.0
     */
    public static String readLine(String prompt) { return Input.readLine(prompt); }

    /**
     * Opens a fluent {@link StringReader}. Delegates to
     * {@link Input#readLine()}.
     *
     * @return a new StringReader
     * @see Input#readLine()
     * @since 1.0.0
     */
    public static StringReader readLine() { return Input.readLine(); }

    /** @see #readLine(String) */
    public static String readString(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine() */
    public static StringReader readString() { return Input.readLine(); }
    /** @see #readLine(String) */
    public static String readText(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine() */
    public static StringReader readText() { return Input.readLine(); }
    /** @see #readLine(String) */
    public static String takeString(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String takeLine(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String getString(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String getLine(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String askLine(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String askString(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String inputLine(String prompt) { return Input.readLine(prompt); }
    /** @see #readLine(String) */
    public static String inputString(String prompt) { return Input.readLine(prompt); }

    /** @see Input#readNonEmpty(String) */
    public static String readNonEmpty(String prompt) {
        return Input.readNonEmpty(prompt);
    }
    /** @see #readNonEmpty(String) */
    public static String readRequired(String prompt) {
        return Input.readNonEmpty(prompt);
    }

    // ================================================================ input — boolean / char

    /**
     * Reads a yes/no answer with a prompt. Delegates to
     * {@link Input#readYesNo(String)}.
     *
     * @param prompt the prompt text
     * @return {@code true} for yes, {@code false} for no
     * @see Input#readYesNo(String)
     * @since 1.0.0
     */
    public static boolean readYesNo(String prompt) { return Input.readYesNo(prompt); }

    /**
     * Opens a fluent {@link BooleanReader}. Delegates to
     * {@link Input#readBoolean()}.
     *
     * @return a new BooleanReader
     * @see Input#readBoolean()
     * @since 1.0.0
     */
    public static BooleanReader readBoolean() { return Input.readBoolean(); }

    /** @see #readYesNo(String) */
    public static boolean readBool(String prompt) { return Input.readYesNo(prompt); }
    /** @see #readYesNo(String) */
    public static boolean readConfirm(String prompt) { return Input.readYesNo(prompt); }
    /** @see #readYesNo(String) */
    public static boolean confirm(String prompt) { return Input.readYesNo(prompt); }
    /** @see #readYesNo(String) */
    public static boolean askConfirm(String prompt) { return Input.readYesNo(prompt); }
    /** @see #readYesNo(String) */
    public static boolean askYesNo(String prompt) { return Input.readYesNo(prompt); }

    /** @see Input#readChar(String) */
    public static char readChar(String prompt) { return Input.readChar(prompt); }
    /** @see Input#readCharacter() */
    public static CharReader readCharacter() { return Input.readCharacter(); }
    /** @see #readChar(String) */
    public static char readLetter(String prompt) { return Input.readChar(prompt); }
    /** @see #readChar(String) */
    public static char readSingleChar(String prompt) { return Input.readChar(prompt); }

    // ================================================================ input — domain

    /** @see Input#readPassword(String) */
    public static String readPassword(String prompt) {
        return Input.readPassword(prompt);
    }
    /** @see Input#readEmail(String) */
    public static String readEmail(String prompt) { return Input.readEmail(prompt); }
    /** @see Input#readPhone(String) */
    public static String readPhone(String prompt) { return Input.readPhone(prompt); }
    /** @see Input#readURL(String) */
    public static String readURL(String prompt) { return Input.readURL(prompt); }
    /** @see #readURL(String) */
    public static String readUrl(String prompt) { return Input.readURL(prompt); }

    /** @see Input#readChoice(String, String...) */
    public static String readChoice(String prompt, String... options) {
        return Input.readChoice(prompt, options);
    }
    /** @see #readChoice(String, String...) */
    public static String choose(String prompt, String... options) {
        return Input.readChoice(prompt, options);
    }
    /** @see Input#readChoiceIndex(String, String...) */
    public static int readChoiceIndex(String prompt, String... opt) {
        return Input.readChoiceIndex(prompt, opt);
    }

    // ================================================================ input — date / time

    /** @see Input#readDate(String) */
    public static LocalDate readDate(String prompt) { return Input.readDate(prompt); }
    /** @see Input#readDate() */
    public static DateReader readDate() { return Input.readDate(); }
    /** @see Input#readTime(String) */
    public static LocalTime readTime(String prompt) { return Input.readTime(prompt); }
    /** @see Input#readTime() */
    public static TimeReader readTime() { return Input.readTime(); }
    /** @see Input#readDateTime(String) */
    public static LocalDateTime readDateTime(String prompt) {
        return Input.readDateTime(prompt);
    }
    /** @see Input#readDateTime() */
    public static DateTimeReader readDateTime() { return Input.readDateTime(); }

    // ================================================================ output

    /** @see Console#print(Object) */
    public static void print(Object value) { Console.print(value); }
    /** @see Console#println(Object) */
    public static void println(Object value) { Console.println(value); }
    /** @see Console#println() */
    public static void println() { Console.println(); }
    /** @see Console#printf(String, Object...) */
    public static void printf(String fmt, Object... args) {
        Console.printf(fmt, args);
    }

    /** @see Console#success(String) */
    public static void success(String msg) { Console.success(msg); }
    /** @see #success(String) */
    public static void ok(String msg) { Console.success(msg); }
    /** @see #success(String) */
    public static void done(String msg) { Console.success(msg); }
    /** @see Console#error(String) */
    public static void error(String msg) { Console.error(msg); }
    /** @see #error(String) */
    public static void fail(String msg) { Console.error(msg); }
    /** @see Console#warn(String) */
    public static void warn(String msg) { Console.warn(msg); }
    /** @see #warn(String) */
    public static void warning(String msg) { Console.warn(msg); }
    /** @see Console#info(String) */
    public static void info(String msg) { Console.info(msg); }
    /** @see #info(String) */
    public static void note(String msg) { Console.info(msg); }

    /** @see Console#title(String) */
    public static void title(String text) { Console.title(text); }
    /** @see #title(String) */
    public static void heading(String text) { Console.title(text); }
    /** @see #title(String) */
    public static void header(String text) { Console.title(text); }
    /** @see #title(String) */
    public static void section(String text) { Console.title(text); }

    /** @see Console#line() */
    public static void line() { Console.line(); }
    /** @see #line() */
    public static void hr() { Console.line(); }
    /** @see Console#line(int) */
    public static void line(int width) { Console.line(width); }
    /** @see #line(int) */
    public static void hr(int width) { Console.line(width); }

    /** @see Console#line(chirp.console.Separator.Style) */
    public static void line(chirp.console.Separator.Style style) {
        Console.line(style);
    }
    /** @see Console#line(int, chirp.console.Separator.Style) */
    public static void line(int width, chirp.console.Separator.Style style) {
        Console.line(width, style);
    }
    /** @see Console#separator() */
    public static chirp.console.Separator.Builder separator() {
        return Console.separator();
    }

    /** @see Console#blank() */
    public static void blank() { Console.blank(); }
    /** @see #blank() */
    public static void nl() { Console.blank(); }

    /** @see Console#watermark() */
    public static void watermark() { Console.watermark(); }
    /** @see #watermark() */
    public static void stamp() { Console.watermark(); }

    /** @see Console#clearScreen() */
    public static void clearScreen() { Console.clearScreen(); }
    /** @see #clearScreen() */
    public static void clear() { Console.clearScreen(); }
    /** @see #clearScreen() */
    public static void cls() { Console.clearScreen(); }

    /** @see Console#bold(String) */
    public static String bold(String text) { return Console.bold(text); }
    /** @see Console#underline(String) */
    public static String underline(String text) { return Console.underline(text); }
    /** @see Console#red(String) */
    public static String red(String text) { return Console.red(text); }
    /** @see Console#green(String) */
    public static String green(String text) { return Console.green(text); }
    /** @see Console#yellow(String) */
    public static String yellow(String text) { return Console.yellow(text); }
    /** @see Console#blue(String) */
    public static String blue(String text) { return Console.blue(text); }
    /** @see Console#cyan(String) */
    public static String cyan(String text) { return Console.cyan(text); }
    /** @see Console#magenta(String) */
    public static String magenta(String text) { return Console.magenta(text); }
    /** @see Console#gray(String) */
    public static String gray(String text) { return Console.gray(text); }

    /** @see Console#printArray(int[]) */
    public static void printArray(int[] a) { Console.printArray(a); }
    /** @see Console#printArray(double[]) */
    public static void printArray(double[] a) { Console.printArray(a); }
    /** @see Console#printArray(boolean[]) */
    public static void printArray(boolean[] a) { Console.printArray(a); }
    /** @see Console#printArray(Object[]) */
    public static void printArray(String[] a) {
        Console.printArray((Object[]) a);
    }
    /** @see Console#printArray(Object[]) */
    public static void printArray(Object[] a) { Console.printArray(a); }

    /** @see Console#printMatrix(int[][]) */
    public static void printMatrix(int[][] m) { Console.printMatrix(m); }
    /** @see Console#printMatrix(String[][]) */
    public static void printMatrix(String[][] m) { Console.printMatrix(m); }

    /** @see Console#table(List, String...) */
    public static void table(List<String[]> rows, String... headers) {
        Console.table(rows, headers);
    }
    /** @see #table(List, String...) */
    public static void grid(List<String[]> rows, String... headers) {
        Console.table(rows, headers);
    }

    /** @see Console#progress(int, int) */
    public static void progress(int current, int total) {
        Console.progress(current, total);
    }
    /** @see #progress(int, int) */
    public static void bar(int current, int total) { Console.progress(current, total); }

    // ================================================================ format

    /** @see Format#fmt(String, Object...) */
    public static String fmt(String template, Object... kv) {
        return Format.fmt(template, kv);
    }
    /** @see #fmt(String, Object...) */
    public static String format(String template, Object... kv) {
        return Format.fmt(template, kv);
    }
    /** @see Format#fmt(String, Map) */
    public static String fmt(String template, Map<String, Object> v) {
        return Format.fmt(template, v);
    }

    /** @see Format#fmtWithSession(String) */
    public static String fmtWithSession(String template) {
        return Format.fmtWithSession(template);
    }
    /** @see #fmtWithSession(String) */
    public static String sessionFormat(String template) {
        return Format.fmtWithSession(template);
    }

    /** @see Format#formatter(String) */
    public static chirp.format.Formatter formatter(String template) {
        return Format.formatter(template);
    }

    /** @see Format#currency(double) */
    public static String currency(double amount) { return Format.currency(amount); }
    /** @see #currency(double) */
    public static String dollars(double amount) { return Format.currency(amount); }
    /** @see #currency(double) */
    public static String usd(double amount) { return Format.currency(amount); }
    /** @see #currency(double) */
    public static String money(double amount) { return Format.currency(amount); }

    /** @see Format#rupees(double) */
    public static String rupees(double amount) { return Format.rupees(amount); }
    /** @see #rupees(double) */
    public static String pkr(double amount) { return Format.rupees(amount); }
    /** @see Format#currencyOf(double, String) */
    public static String currencyOf(double amount, String symbol) {
        return Format.currencyOf(amount, symbol);
    }

    /** @see Format#percent(double) */
    public static String percent(double value) { return Format.percent(value); }
    /** @see #percent(double) */
    public static String pct(double value) { return Format.percent(value); }
    /** @see Format#percent(double, int) */
    public static String percent(double value, int places) {
        return Format.percent(value, places);
    }

    /** @see Format#decimal(double, int) */
    public static String decimal(double value, int places) {
        return Format.decimal(value, places);
    }
    /** @see #decimal(double, int) */
    public static String fixed(double value, int places) {
        return Format.decimal(value, places);
    }

    /** @see Format#plural(int, String, String) */
    public static String plural(int n, String s, String p) {
        return Format.plural(n, s, p);
    }
    /** @see Format#quantify(int, String) */
    public static String quantify(int n, String singular) {
        return Format.quantify(n, singular);
    }
    /** @see Format#items(int) */
    public static String items(int n) { return Format.items(n); }
    /** @see Format#files(int) */
    public static String files(int n) { return Format.files(n); }
    /** @see Format#lines(int) */
    public static String lines(int n) { return Format.lines(n); }
    /** @see Format#chars(int) */
    public static String chars(int n) { return Format.chars(n); }

    /** @see Format#list(List, String) */
    public static <T> String list(List<T> l, String d) {
        return Format.list(l, d);
    }
    /** @see Format#list(List, String, String) */
    public static <T> String list(List<T> l, String d, String last) {
        return Format.list(l, d, last);
    }
    /** @see Format#list(List, String) */
    public static <T> String join(List<T> l, String d) {
        return Format.list(l, d);
    }

    /** @see Format#ordinal(int) */
    public static String ordinal(int n) { return Format.ordinal(n); }
    /** @see #ordinal(int) */
    public static String nth(int n) { return Format.ordinal(n); }
    /** @see Format#roman(int) */
    public static String roman(int n) { return Format.roman(n); }

    /** @see Format#bytes(long) */
    public static String bytes(long count) { return Format.bytes(count); }
    /** @see #bytes(long) */
    public static String fileSize(long count) { return Format.bytes(count); }

    /** @see Format#duration(long) */
    public static String duration(long millis) { return Format.duration(millis); }
    /** @see #duration(long) */
    public static String humanDuration(long millis) { return Format.duration(millis); }
    /** @see Format#duration(Duration) */
    public static String duration(Duration d) { return Format.duration(d); }

    /** @see Format#cnic(String) */
    public static String cnic(String raw) { return Format.cnic(raw); }
    /** @see Format#pakistaniPhone(String) */
    public static String pakistaniPhone(String raw) {
        return Format.pakistaniPhone(raw);
    }
    /** @see #pakistaniPhone(String) */
    public static String pkPhone(String raw) { return Format.pakistaniPhone(raw); }

    // ================================================================ date / time

    /** @see DateTime#today() */
    public static LocalDate today() { return DateTime.today(); }
    /** @see #today() */
    public static LocalDate nowDate() { return DateTime.today(); }
    /** @see DateTime#yesterday() */
    public static LocalDate yesterday() { return DateTime.yesterday(); }
    /** @see DateTime#tomorrow() */
    public static LocalDate tomorrow() { return DateTime.tomorrow(); }
    /** @see DateTime#nowTime() */
    public static LocalTime nowTime() { return DateTime.nowTime(); }
    /** @see DateTime#now() */
    public static LocalDateTime now() { return DateTime.now(); }
    /** @see #now() */
    public static LocalDateTime nowDateTime() { return DateTime.now(); }

    /** @see DateTime#of(LocalDate) */
    public static chirp.datetime.DateChain dateChain(LocalDate d) {
        return DateTime.of(d);
    }
    /** @see DateTime#of(String) */
    public static chirp.datetime.DateChain dateChain(String d) { return DateTime.of(d); }

    /** @see DateTime#parseDate(String) */
    public static LocalDate parseDate(String s) { return DateTime.parseDate(s); }
    /** @see #parseDate(String) */
    public static LocalDate readDateValue(String s) { return DateTime.parseDate(s); }
    /** @see #parseDate(String) */
    public static LocalDate dateFrom(String s) { return DateTime.parseDate(s); }
    /** @see DateTime#parseDate(String, String) */
    public static LocalDate parseDate(String s, String pattern) {
        return DateTime.parseDate(s, pattern);
    }
    /** @see DateTime#parseTime(String) */
    public static LocalTime parseTime(String s) { return DateTime.parseTime(s); }
    /** @see DateTime#parseDateTime(String) */
    public static LocalDateTime parseDateTime(String s) {
        return DateTime.parseDateTime(s);
    }

    /** @see DateTime#humanize(LocalDate) */
    public static String humanizeDate(LocalDate d) {
        return DateTime.humanize(d);
    }
    /** @see DateTime#humanizeShort(LocalDate) */
    public static String humanizeShortDate(LocalDate d) {
        return DateTime.humanizeShort(d);
    }
    /** @see DateTime#humanize(LocalTime) */
    public static String humanizeTime(LocalTime t) {
        return DateTime.humanize(t);
    }
    /** @see DateTime#humanize(LocalDateTime) */
    public static String humanizeDateTime(LocalDateTime dt) {
        return DateTime.humanize(dt);
    }

    /** @see DateTime#timeAgo(LocalDate) */
    public static String relativeDate(LocalDate d) {
        return DateTime.timeAgo(d);
    }
    /** @see DateTime#timeAgo(LocalDateTime) */
    public static String relativeDateTime(LocalDateTime dt) {
        return DateTime.timeAgo(dt);
    }
    /** @see DateTime#timeAgo(LocalDate) */
    public static String timeAgo(LocalDate d) { return DateTime.timeAgo(d); }
    /** @see DateTime#timeAgo(LocalDateTime) */
    public static String timeAgo(LocalDateTime dt) {
        return DateTime.timeAgo(dt);
    }

    /** @see DateTime#format(LocalDate, String) */
    public static String formatDate(LocalDate d, String p) {
        return DateTime.format(d, p);
    }
    /** @see DateTime#format(LocalTime, String) */
    public static String formatTime(LocalTime t, String p) {
        return DateTime.format(t, p);
    }
    /** @see DateTime#format(LocalDateTime, String) */
    public static String formatDateTime(LocalDateTime dt, String p) {
        return DateTime.format(dt, p);
    }
    /** @see DateTime#formatISO(LocalDate) */
    public static String formatDateISO(LocalDate d) {
        return DateTime.formatISO(d);
    }

    /** @see DateTime#addDays(LocalDate, int) */
    public static LocalDate addDays(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }
    /** @see #addDays(LocalDate, int) */
    public static LocalDate plusDays(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }
    /** @see #addDays(LocalDate, int) */
    public static LocalDate daysAfter(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }
    /** @see DateTime#subtractDays(LocalDate, int) */
    public static LocalDate subtractDays(LocalDate d, int n) {
        return DateTime.subtractDays(d, n);
    }
    /** @see #subtractDays(LocalDate, int) */
    public static LocalDate minusDays(LocalDate d, int n) {
        return DateTime.subtractDays(d, n);
    }

    /** @see DateTime#addMonths(LocalDate, int) */
    public static LocalDate addMonths(LocalDate d, int n) {
        return DateTime.addMonths(d, n);
    }
    /** @see DateTime#subtractMonths(LocalDate, int) */
    public static LocalDate subtractMonths(LocalDate d, int n) {
        return DateTime.subtractMonths(d, n);
    }
    /** @see DateTime#addYears(LocalDate, int) */
    public static LocalDate addYears(LocalDate d, int n) {
        return DateTime.addYears(d, n);
    }
    /** @see DateTime#subtractYears(LocalDate, int) */
    public static LocalDate subtractYears(LocalDate d, int n) {
        return DateTime.subtractYears(d, n);
    }

    /** @see DateTime#addBusinessDays(LocalDate, int) */
    public static LocalDate addBusinessDays(LocalDate d, int n) {
        return DateTime.addBusinessDays(d, n);
    }

    /** @see DateTime#startOfWeek(LocalDate) */
    public static LocalDate startOfWeek(LocalDate d) {
        return DateTime.startOfWeek(d);
    }
    /** @see DateTime#endOfWeek(LocalDate) */
    public static LocalDate endOfWeek(LocalDate d) {
        return DateTime.endOfWeek(d);
    }
    /** @see DateTime#startOfMonth(LocalDate) */
    public static LocalDate startOfMonth(LocalDate d) {
        return DateTime.startOfMonth(d);
    }
    /** @see DateTime#endOfMonth(LocalDate) */
    public static LocalDate endOfMonth(LocalDate d) {
        return DateTime.endOfMonth(d);
    }
    /** @see DateTime#startOfYear(LocalDate) */
    public static LocalDate startOfYear(LocalDate d) {
        return DateTime.startOfYear(d);
    }
    /** @see DateTime#endOfYear(LocalDate) */
    public static LocalDate endOfYear(LocalDate d) {
        return DateTime.endOfYear(d);
    }
    /** @see DateTime#startOfQuarter(LocalDate) */
    public static LocalDate startOfQuarter(LocalDate d) {
        return DateTime.startOfQuarter(d);
    }
    /** @see DateTime#endOfQuarter(LocalDate) */
    public static LocalDate endOfQuarter(LocalDate d) {
        return DateTime.endOfQuarter(d);
    }

    /** @see DateTime#daysBetween(LocalDate, LocalDate) */
    public static long daysBetween(LocalDate a, LocalDate b) {
        return DateTime.daysBetween(a, b);
    }
    /** @see #daysBetween(LocalDate, LocalDate) */
    public static long diffDays(LocalDate a, LocalDate b) {
        return DateTime.daysBetween(a, b);
    }
    /** @see DateTime#monthsBetween(LocalDate, LocalDate) */
    public static long monthsBetween(LocalDate a, LocalDate b) {
        return DateTime.monthsBetween(a, b);
    }
    /** @see DateTime#yearsBetween(LocalDate, LocalDate) */
    public static long yearsBetween(LocalDate a, LocalDate b) {
        return DateTime.yearsBetween(a, b);
    }
    /** @see DateTime#businessDaysBetween(LocalDate, LocalDate) */
    public static int businessDaysBetween(LocalDate a, LocalDate b) {
        return DateTime.businessDaysBetween(a, b);
    }
    /** @see #businessDaysBetween(LocalDate, LocalDate) */
    public static int businessDaysUntil(LocalDate a, LocalDate b) {
        return DateTime.businessDaysBetween(a, b);
    }

    /** @see DateTime#age(LocalDate) */
    public static int age(LocalDate birth) { return DateTime.age(birth); }
    /** @see #age(LocalDate) */
    public static int calculateAge(LocalDate birth) { return DateTime.age(birth); }

    /** @see DateTime#dayName(LocalDate) */
    public static String dayName(LocalDate d) { return DateTime.dayName(d); }
    /** @see DateTime#monthName(LocalDate) */
    public static String monthName(LocalDate d) {
        return DateTime.monthName(d);
    }
    /** @see DateTime#dayOfWeek(LocalDate) */
    public static int dayOfWeek(LocalDate d) { return DateTime.dayOfWeek(d); }
    /** @see DateTime#dayOfYear(LocalDate) */
    public static int dayOfYear(LocalDate d) { return DateTime.dayOfYear(d); }
    /** @see DateTime#quarter(LocalDate) */
    public static int quarter(LocalDate d) { return DateTime.quarter(d); }
    /** @see DateTime#daysInMonth(LocalDate) */
    public static int daysInMonth(LocalDate d) {
        return DateTime.daysInMonth(d);
    }

    /** @see DateTime#isWeekend(LocalDate) */
    public static boolean isWeekend(LocalDate d) {
        return DateTime.isWeekend(d);
    }
    /** @see DateTime#isWeekday(LocalDate) */
    public static boolean isWeekday(LocalDate d) {
        return DateTime.isWeekday(d);
    }
    /** @see DateTime#isToday(LocalDate) */
    public static boolean isToday(LocalDate d) { return DateTime.isToday(d); }
    /** @see DateTime#isPast(LocalDate) */
    public static boolean isPast(LocalDate d) { return DateTime.isPast(d); }
    /** @see DateTime#isFuture(LocalDate) */
    public static boolean isFuture(LocalDate d) { return DateTime.isFuture(d); }
    /** @see DateTime#isSameMonth(LocalDate, LocalDate) */
    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return DateTime.isSameMonth(a, b);
    }
    /** @see DateTime#isSameYear(LocalDate, LocalDate) */
    public static boolean isSameYear(LocalDate a, LocalDate b) {
        return DateTime.isSameYear(a, b);
    }
    /** @see DateTime#isLeapYear(int) */
    public static boolean isLeapYear(int year) { return DateTime.isLeapYear(year); }

    /** @see DateTime#min(LocalDate, LocalDate) */
    public static LocalDate minDate(LocalDate a, LocalDate b) {
        return DateTime.min(a, b);
    }
    /** @see DateTime#max(LocalDate, LocalDate) */
    public static LocalDate maxDate(LocalDate a, LocalDate b) {
        return DateTime.max(a, b);
    }

    /** @see DateTime#toEpochMillis(LocalDateTime) */
    public static long toEpochMillis(LocalDateTime dt) {
        return DateTime.toEpochMillis(dt);
    }
    /** @see DateTime#fromEpochMillis(long) */
    public static LocalDateTime fromEpochMillis(long ms) {
        return DateTime.fromEpochMillis(ms);
    }

    // ================================================================ numbers

    /** @see Numbers#of(int) */
    public static chirp.number.NumberChain numberChain(int n) { return Numbers.of(n); }
    /** @see Numbers#of(double) */
    public static chirp.number.NumberChain numberChain(double n) { return Numbers.of(n); }
    /** @see Numbers#of(int) */
    public static chirp.number.NumberChain numberOf(int n) { return Numbers.of(n); }
    /** @see Numbers#of(double) */
    public static chirp.number.NumberChain numberOf(double n) { return Numbers.of(n); }

    /** @see Numbers#random(int, int) */
    public static int random(int min, int max) { return Numbers.random(min, max); }
    /** @see #random(int, int) */
    public static int rand(int min, int max) { return Numbers.random(min, max); }
    /** @see #random(int, int) */
    public static int randomInt(int min, int max) { return Numbers.random(min, max); }
    /** @see #random(int, int) */
    public static int randomBetween(int min, int max) { return Numbers.random(min, max); }
    /** @see Numbers#randomDouble(double, double) */
    public static double randomDouble(double min, double max) {
        return Numbers.randomDouble(min, max);
    }
    /** @see Numbers#randomBoolean() */
    public static boolean randomBoolean() { return Numbers.randomBoolean(); }

    /** @see Numbers#clamp(int, int, int) */
    public static int clamp(int v, int min, int max) {
        return Numbers.clamp(v, min, max);
    }
    /** @see Numbers#clamp(double, double, double) */
    public static double clamp(double v, double min, double max) {
        return Numbers.clamp(v, min, max);
    }
    /** @see Numbers#wrap(int, int, int) */
    public static int wrap(int v, int min, int max) {
        return Numbers.wrap(v, min, max);
    }

    /** @see Numbers#abs(int) */
    public static int abs(int n) { return Numbers.abs(n); }
    /** @see Numbers#abs(double) */
    public static double abs(double n) { return Numbers.abs(n); }
    /** @see Numbers#sign(int) */
    public static int sign(int n) { return Numbers.sign(n); }
    /** @see Numbers#mod(int, int) */
    public static int mod(int a, int b) { return Numbers.mod(a, b); }

    /** @see Numbers#pow(double, double) */
    public static double pow(double b, double e) { return Numbers.pow(b, e); }
    /** @see Numbers#sqrt(double) */
    public static double sqrt(double n) { return Numbers.sqrt(n); }
    /** @see Numbers#cbrt(double) */
    public static double cbrt(double n) { return Numbers.cbrt(n); }
    /** @see Numbers#hypot(double, double) */
    public static double hypot(double a, double b) {
        return Numbers.hypot(a, b);
    }
    /** @see Numbers#log(double) */
    public static double log(double n) { return Numbers.log(n); }
    /** @see Numbers#log10(double) */
    public static double log10(double n) { return Numbers.log10(n); }
    /** @see Numbers#log2(double) */
    public static double log2(double n) { return Numbers.log2(n); }
    /** @see Numbers#exp(double) */
    public static double exp(double n) { return Numbers.exp(n); }

    /** @see Numbers#floor(double) */
    public static int floor(double n) { return Numbers.floor(n); }
    /** @see Numbers#ceil(double) */
    public static int ceil(double n) { return Numbers.ceil(n); }
    /** @see Numbers#roundToInt(double) */
    public static int roundToInt(double n) { return Numbers.roundToInt(n); }
    /** @see Numbers#roundToLong(double) */
    public static long roundToLong(double n) { return Numbers.roundToLong(n); }

    /** @see Numbers#sin(double) */
    public static double sin(double r) { return Numbers.sin(r); }
    /** @see Numbers#cos(double) */
    public static double cos(double r) { return Numbers.cos(r); }
    /** @see Numbers#tan(double) */
    public static double tan(double r) { return Numbers.tan(r); }
    /** @see Numbers#toRadians(double) */
    public static double toRadians(double d) { return Numbers.toRadians(d); }
    /** @see Numbers#toDegrees(double) */
    public static double toDegrees(double r) { return Numbers.toDegrees(r); }

    /** @see Numbers#isEven(int) */
    public static boolean isEven(int n) { return Numbers.isEven(n); }
    /** @see Numbers#isOdd(int) */
    public static boolean isOdd(int n) { return Numbers.isOdd(n); }
    /** @see Numbers#isPrime(int) */
    public static boolean isPrime(int n) { return Numbers.isPrime(n); }
    /** @see Numbers#primesUpTo(int) */
    public static List<Integer> primesUpTo(int n) { return Numbers.primesUpTo(n); }
    /** @see Numbers#nextPrime(int) */
    public static int nextPrime(int n) { return Numbers.nextPrime(n); }

    /** @see Numbers#factorial(int) */
    public static long factorial(int n) { return Numbers.factorial(n); }
    /** @see #factorial(int) */
    public static long fact(int n) { return Numbers.factorial(n); }
    /** @see Numbers#fibonacci(int) */
    public static int fibonacci(int n) { return Numbers.fibonacci(n); }
    /** @see #fibonacci(int) */
    public static int fib(int n) { return Numbers.fibonacci(n); }
    /** @see Numbers#gcd(int, int) */
    public static int gcd(int a, int b) { return Numbers.gcd(a, b); }
    /** @see Numbers#lcm(int, int) */
    public static int lcm(int a, int b) { return Numbers.lcm(a, b); }

    /** @see Numbers#sumDigits(int) */
    public static int sumDigits(int n) { return Numbers.sumDigits(n); }
    /** @see Numbers#productDigits(int) */
    public static int productDigits(int n) { return Numbers.productDigits(n); }
    /** @see Numbers#countDigits(int) */
    public static int countDigits(int n) { return Numbers.countDigits(n); }
    /** @see Numbers#reverseDigits(int) */
    public static int reverseDigits(int n) { return Numbers.reverseDigits(n); }
    /** @see Numbers#firstDigit(int) */
    public static int firstDigit(int n) { return Numbers.firstDigit(n); }
    /** @see Numbers#lastDigit(int) */
    public static int lastDigit(int n) { return Numbers.lastDigit(n); }

    /** @see Numbers#toBinary(int) */
    public static String toBinary(int n) { return Numbers.toBinary(n); }
    /** @see Numbers#toHex(int) */
    public static String toHex(int n) { return Numbers.toHex(n); }
    /** @see Numbers#toOctal(int) */
    public static String toOctal(int n) { return Numbers.toOctal(n); }
    /** @see Numbers#fromBinary(String) */
    public static Integer fromBinary(String s) { return Numbers.fromBinary(s); }
    /** @see Numbers#fromHex(String) */
    public static Integer fromHex(String s) { return Numbers.fromHex(s); }

    /** @see Numbers#isArmstrong(int) */
    public static boolean isArmstrong(int n) { return Numbers.isArmstrong(n); }
    /** @see Numbers#isPalindromeNumber(int) */
    public static boolean isPalindromeNumber(int n) {
        return Numbers.isPalindromeNumber(n);
    }
    /** @see Numbers#isPerfect(int) */
    public static boolean isPerfect(int n) { return Numbers.isPerfect(n); }
    /** @see Numbers#isPerfectSquare(int) */
    public static boolean isPerfectSquare(int n) {
        return Numbers.isPerfectSquare(n);
    }
    /** @see Numbers#isPerfectCube(int) */
    public static boolean isPerfectCube(int n) {
        return Numbers.isPerfectCube(n);
    }

    /** @see Numbers#divisors(int) */
    public static List<Integer> divisors(int n) { return Numbers.divisors(n); }
    /** @see Numbers#countDivisors(int) */
    public static int countDivisors(int n) { return Numbers.countDivisors(n); }
    /** @see Numbers#sumDivisors(int) */
    public static int sumDivisors(int n) { return Numbers.sumDivisors(n); }

    /** @see Numbers#round(double, int) */
    public static double round(double v, int d) { return Numbers.round(v, d); }
    /** @see Numbers#format(double, int) */
    public static String numFmt(double v, int decimals) {
        return Numbers.format(v, decimals);
    }

    /** @see Numbers#parseInt(String) */
    public static Integer parseInt(String s) { return Numbers.parseInt(s); }
    /** @see Numbers#parseDouble(String) */
    public static Double parseDouble(String s) { return Numbers.parseDouble(s); }
    /** @see Numbers#percent(double, double) */
    public static double percentOf(double part, double whole) {
        return Numbers.percent(part, whole);
    }

    /** @see Numbers#range(int, int) */
    public static List<Integer> range(int start, int end) {
        return Numbers.range(start, end);
    }
    /** @see Numbers#range(int, int, int) */
    public static List<Integer> range(int start, int end, int step) {
        return Numbers.range(start, end, step);
    }
    /** @see Numbers#rangeInclusive(int, int) */
    public static List<Integer> rangeInclusive(int start, int end) {
        return Numbers.rangeInclusive(start, end);
    }

    /** @see Numbers#statistics(int[]) */
    public static Numbers.Statistics statistics(int[] values) {
        return Numbers.statistics(values);
    }
    /** @see Numbers#median(int[]) */
    public static double median(int[] a) { return Numbers.median(a); }
    /** @see Numbers#mode(int[]) */
    public static int mode(int[] a) { return Numbers.mode(a); }
    /** @see Numbers#stdDev(int[]) */
    public static double stdDev(int[] a) { return Numbers.stdDev(a); }
    /** @see Numbers#variance(int[]) */
    public static double variance(int[] a) { return Numbers.variance(a); }

    /** @see Numbers#abbreviate(long) */
    public static String abbreviate(long n) { return Numbers.abbreviate(n); }
    /** @see Numbers#thousands(long) */
    public static String thousands(long n) { return Numbers.thousands(n); }
    /** @see Numbers#roman(int) */
    public static String romanNumeral(int n) { return Numbers.roman(n); }

    // ================================================================ arrays

    /** @see ArraysUtil#of(int[]) */
    public static ArrayChain arrayChain(int[] a) { return ArraysUtil.of(a); }
    /** @see ArrayChain#of(int...) */
    public static ArrayChain arrayOf(int... a) { return ArrayChain.of(a); }

    /** @see ArraysUtil#sum(int[]) */
    public static int sum(int[] a) { return ArraysUtil.sum(a); }
    /** @see #sum(int[]) */
    public static int total(int[] a) { return ArraysUtil.sum(a); }
    /** @see ArraysUtil#sum(double[]) */
    public static double sum(double[] a) { return ArraysUtil.sum(a); }
    /** @see ArraysUtil#average(int[]) */
    public static double average(int[] a) { return ArraysUtil.average(a); }
    /** @see #average(int[]) */
    public static double mean(int[] a) { return ArraysUtil.average(a); }
    /** @see ArraysUtil#max(int[]) */
    public static int max(int[] a) { return ArraysUtil.max(a); }
    /** @see #max(int[]) */
    public static int largest(int[] a) { return ArraysUtil.max(a); }
    /** @see ArraysUtil#min(int[]) */
    public static int min(int[] a) { return ArraysUtil.min(a); }
    /** @see #min(int[]) */
    public static int smallest(int[] a) { return ArraysUtil.min(a); }
    /** @see ArraysUtil#count(int[], int) */
    public static int count(int[] a, int v) { return ArraysUtil.count(a, v); }
    /** @see ArraysUtil#contains(int[], int) */
    public static boolean contains(int[] a, int v) {
        return ArraysUtil.contains(a, v);
    }
    /** @see ArraysUtil#indexOf(int[], int) */
    public static int indexOf(int[] a, int v) {
        return ArraysUtil.indexOf(a, v);
    }
    /** @see ArraysUtil#lastIndexOf(int[], int) */
    public static int lastIndexOf(int[] a, int v) {
        return ArraysUtil.lastIndexOf(a, v);
    }
    /** @see ArraysUtil#hasDuplicates(int[]) */
    public static boolean hasDuplicates(int[] a) {
        return ArraysUtil.hasDuplicates(a);
    }
    /** @see ArraysUtil#isSorted(int[]) */
    public static boolean isSorted(int[] a) { return ArraysUtil.isSorted(a); }

    /** @see ArraysUtil#reverse(int[]) */
    public static int[] reverse(int[] a) { return ArraysUtil.reverse(a); }
    /** @see ArraysUtil#sorted(int[]) */
    public static int[] sorted(int[] a) { return ArraysUtil.sorted(a); }
    /** @see ArraysUtil#sortedDescending(int[]) */
    public static int[] sortedDescending(int[] a) {
        return ArraysUtil.sortedDescending(a);
    }
    /** @see ArraysUtil#distinct(int[]) */
    public static int[] distinct(int[] a) { return ArraysUtil.distinct(a); }
    /** @see ArraysUtil#rotateRight(int[], int) */
    public static int[] rotateRight(int[] a, int steps) {
        return ArraysUtil.rotateRight(a, steps);
    }
    /** @see ArraysUtil#rotateLeft(int[], int) */
    public static int[] rotateLeft(int[] a, int steps) {
        return ArraysUtil.rotateLeft(a, steps);
    }
    /** @see ArraysUtil#shuffle(int[]) */
    public static int[] shuffle(int[] a) { return ArraysUtil.shuffle(a); }
    /** @see ArraysUtil#subArray(int[], int, int) */
    public static int[] subArray(int[] a, int from, int to) {
        return ArraysUtil.subArray(a, from, to);
    }
    /** @see ArraysUtil#take(int[], int) */
    public static int[] take(int[] a, int n) { return ArraysUtil.take(a, n); }
    /** @see ArraysUtil#drop(int[], int) */
    public static int[] drop(int[] a, int n) { return ArraysUtil.drop(a, n); }
    /** @see ArraysUtil#concat(int[], int[]) */
    public static int[] concat(int[] a, int[] b) {
        return ArraysUtil.concat(a, b);
    }
    /** @see #concat(int[], int[]) */
    public static int[] merge(int[] a, int[] b) { return ArraysUtil.concat(a, b); }

    /** @see ArraysUtil#filter(int[], java.util.function.IntPredicate) */
    public static int[] filter(int[] a, java.util.function.IntPredicate p) {
        return ArraysUtil.filter(a, p);
    }
    /** @see ArraysUtil#map(int[], java.util.function.IntUnaryOperator) */
    public static int[] map(int[] a, java.util.function.IntUnaryOperator f) {
        return ArraysUtil.map(a, f);
    }

    /** @see ArraysUtil#intersection(int[], int[]) */
    public static int[] intersection(int[] a, int[] b) {
        return ArraysUtil.intersection(a, b);
    }
    /** @see ArraysUtil#union(int[], int[]) */
    public static int[] union(int[] a, int[] b) {
        return ArraysUtil.union(a, b);
    }
    /** @see ArraysUtil#difference(int[], int[]) */
    public static int[] difference(int[] a, int[] b) {
        return ArraysUtil.difference(a, b);
    }

    /** @see ArraysUtil#transpose(int[][]) */
    public static int[][] transpose(int[][] m) {
        return ArraysUtil.transpose(m);
    }
    /** @see ArraysUtil#row(int[][], int) */
    public static int[] row(int[][] m, int r) { return ArraysUtil.row(m, r); }
    /** @see ArraysUtil#col(int[][], int) */
    public static int[] col(int[][] m, int c) { return ArraysUtil.col(m, c); }
    /** @see ArraysUtil#diagonal(int[][]) */
    public static int[] diagonal(int[][] m) { return ArraysUtil.diagonal(m); }
    /** @see ArraysUtil#antiDiagonal(int[][]) */
    public static int[] antiDiagonal(int[][] m) {
        return ArraysUtil.antiDiagonal(m);
    }
    /** @see ArraysUtil#flatten(int[][]) */
    public static int[] flatten(int[][] m) { return ArraysUtil.flatten(m); }

    /** @see ArraysUtil#matrix(int, int, int) */
    public static int[][] matrix(int rows, int cols, int value) {
        return ArraysUtil.matrix(rows, cols, value);
    }
    /** @see ArraysUtil#intArray(int, int) */
    public static int[] intArray(int size, int value) {
        return ArraysUtil.intArray(size, value);
    }
    /** @see ArraysUtil#intArray(int...) */
    public static int[] intArray(int... values) {
        return ArraysUtil.intArray(values);
    }
    /** @see ArraysUtil#zeros(int) */
    public static int[] zeros(int n) { return ArraysUtil.zeros(n); }
    /** @see ArraysUtil#ones(int) */
    public static int[] ones(int n) { return ArraysUtil.ones(n); }
    /** @see ArraysUtil#rangeArray(int, int) */
    public static int[] rangeArray(int start, int end) {
        return ArraysUtil.rangeArray(start, end);
    }

    // ================================================================ search / filter / page

    /** @see Search#contains(String, String) */
    public static boolean contains(String text, String q) {
        return Search.contains(text, q);
    }
    /** @see Search#containsIgnoreCase(String, String) */
    public static boolean containsIgnoreCase(String t, String q) {
        return Search.containsIgnoreCase(t, q);
    }
    /** @see Search#containsStrict(String, String) */
    public static boolean containsStrict(String text, String q) {
        return Search.containsStrict(text, q);
    }
    /** @see Search#startsWith(String, String) */
    public static boolean startsWith(String text, String q) {
        return Search.startsWith(text, q);
    }
    /** @see Search#endsWith(String, String) */
    public static boolean endsWith(String text, String q) {
        return Search.endsWith(text, q);
    }
    /** @see Search#wholeWord(String, String) */
    public static boolean wholeWord(String text, String word) {
        return Search.wholeWord(text, word);
    }
    /** @see Search#fuzzy(String, String, int) */
    public static boolean fuzzyMatch(String text, String q, int maxD) {
        return Search.fuzzy(text, q, maxD);
    }
    /** @see Search#levenshtein(String, String) */
    public static int levenshtein(String a, String b) {
        return Search.levenshtein(a, b);
    }

    /** @see Filter#where(List, Predicate) */
    public static <T> List<T> filter(List<T> items, Predicate<T> p) {
        return Filter.where(items, p);
    }
    /** @see #filter(List, Predicate) */
    public static <T> List<T> where(List<T> items, Predicate<T> p) {
        return Filter.where(items, p);
    }
    /** @see Filter#reject(List, Predicate) */
    public static <T> List<T> reject(List<T> items, Predicate<T> p) {
        return Filter.reject(items, p);
    }
    /** @see Filter#distinct(List) */
    public static <T> List<T> unique(List<T> items) { return Filter.distinct(items); }
    /** @see #unique(List) */
    public static <T> List<T> distinct(List<T> items) { return Filter.distinct(items); }

    /** @see Pagination#paginate(List, int, int) */
    public static <T> Page<T> page(List<T> items, int page, int size) {
        return Pagination.paginate(items, page, size);
    }
    /** @see Pagination#firstPage(List, int) */
    public static <T> Page<T> firstPage(List<T> items, int size) {
        return Pagination.firstPage(items, size);
    }
    /** @see Pagination#lastPage(List, int) */
    public static <T> Page<T> lastPage(List<T> items, int size) {
        return Pagination.lastPage(items, size);
    }
    /** @see Pagination#allPages(List, int) */
    public static <T> List<Page<T>> allPages(List<T> items, int size) {
        return Pagination.allPages(items, size);
    }

    // ================================================================ text

    /** @see Text#chain(String) */
    public static TextChain chain(String s) { return Text.chain(s); }
    /** @see #chain(String) */
    public static TextChain textChain(String s) { return Text.chain(s); }

    /** @see Text#isBlank(String) */
    public static boolean isBlank(String s) { return Text.isBlank(s); }
    /** @see Text#isNotBlank(String) */
    public static boolean notBlank(String s) { return Text.isNotBlank(s); }
    /** @see Text#isEmpty(String) */
    public static boolean isEmpty(String s) { return Text.isEmpty(s); }
    /** @see Text#hasText(String) */
    public static boolean hasText(String s) { return Text.hasText(s); }

    /** @see Text#capitalize(String) */
    public static String capitalize(String s) { return Text.capitalize(s); }
    /** @see #capitalize(String) */
    public static String capitalise(String s) { return Text.capitalize(s); }
    /** @see Text#titleCase(String) */
    public static String titleCase(String s) { return Text.titleCase(s); }
    /** @see Text#toCamelCase(String) */
    public static String toCamelCase(String s) { return Text.toCamelCase(s); }
    /** @see Text#toPascalCase(String) */
    public static String toPascalCase(String s) { return Text.toPascalCase(s); }
    /** @see Text#toSnakeCase(String) */
    public static String toSnakeCase(String s) { return Text.toSnakeCase(s); }
    /** @see Text#toKebabCase(String) */
    public static String toKebabCase(String s) { return Text.toKebabCase(s); }
    /** @see Text#swapCase(String) */
    public static String swapCase(String s) { return Text.swapCase(s); }

    /** @see Text#reverse(String) */
    public static String reverseText(String s) { return Text.reverse(s); }
    /** @see Text#reverseWords(String) */
    public static String reverseWords(String s) { return Text.reverseWords(s); }
    /** @see Text#truncate(String, int) */
    public static String truncate(String s, int max) {
        return Text.truncate(s, max);
    }
    /** @see Text#padLeft(String, int) */
    public static String padLeft(String s, int w) { return Text.padLeft(s, w); }
    /** @see Text#padRight(String, int) */
    public static String padRight(String s, int w) { return Text.padRight(s, w); }
    /** @see Text#center(String, int) */
    public static String center(String s, int w) { return Text.center(s, w); }
    /** @see Text#repeat(String, int) */
    public static String repeat(String s, int n) { return Text.repeat(s, n); }

    /** @see Text#isPalindrome(String) */
    public static boolean isPalindrome(String s) { return Text.isPalindrome(s); }
    /** @see Text#isAnagram(String, String) */
    public static boolean isAnagram(String a, String b) {
        return Text.isAnagram(a, b);
    }
    /** @see Text#mask(String, int) */
    public static String mask(String s, int visible) { return Text.mask(s, visible); }
    /** @see Text#slugify(String) */
    public static String slugify(String s) { return Text.slugify(s); }
    /** @see #slugify(String) */
    public static String slug(String s) { return Text.slugify(s); }
    /** @see Text#similarity(String, String) */
    public static double similarity(String a, String b) {
        return Text.similarity(a, b);
    }

    /** @see Text#countWords(String) */
    public static int countWords(String s) { return Text.countWords(s); }
    /** @see Text#initials(String) */
    public static String initials(String s) { return Text.initials(s); }
    /** @see Text#join(String, Object...) */
    public static String join(String d, Object... parts) {
        return Text.join(d, parts);
    }
    /** @see Text#removeSpaces(String) */
    public static String removeSpaces(String s) { return Text.removeSpaces(s); }
    /** @see Text#removePunctuation(String) */
    public static String removePunctuation(String s) {
        return Text.removePunctuation(s);
    }
    /** @see Text#extractDigits(String) */
    public static String extractDigits(String s) { return Text.extractDigits(s); }
    /** @see Text#extractLetters(String) */
    public static String extractLetters(String s) {
        return Text.extractLetters(s);
    }
    /** @see Text#substringBefore(String, String) */
    public static String substringBefore(String s, String m) {
        return Text.substringBefore(s, m);
    }
    /** @see Text#substringAfter(String, String) */
    public static String substringAfter(String s, String m) {
        return Text.substringAfter(s, m);
    }

    /**
     * Filters a list by a predicate — delegates to
     * {@link Filter#where(List, Predicate)} with null-safety on both
     * arguments. This method <b>does</b> have logic (unlike most
     * delegations) because it is the last-call convenience for callers
     * that pass a null list and expect an empty result rather than an
     * exception.
     *
     * @param items the list to filter (null → empty list)
     * @param p the predicate (null → empty list)
     * @param <T> the element type
     * @return the filtered list, never null
     * @see Filter#where(List, Predicate)
     * @since 1.0.0
     */
    public static <T> List<T> filterByCheck(List<T> items, Predicate<T> p) {
        if (items == null || p == null)
            return List.of();
        return items.stream().filter(p).toList();
    }

    // ================================================================ checks

    /** @see Check#isEmail(String) */
    public static boolean isEmail(String s) { return Check.isEmail(s); }
    /** @see Check#isPhone(String) */
    public static boolean isPhone(String s) { return Check.isPhone(s); }
    /** @see Check#isURL(String) */
    public static boolean isURL(String s) { return Check.isURL(s); }
    /** @see #isURL(String) */
    public static boolean isUrl(String s) { return Check.isURL(s); }
    /** @see Check#isNumeric(String) */
    public static boolean isNumeric(String s) { return Check.isNumeric(s); }
    /** @see Check#isInteger(String) */
    public static boolean isInteger(String s) { return Check.isInteger(s); }
    /** @see Check#isAlpha(String) */
    public static boolean isAlpha(String s) { return Check.isAlpha(s); }
    /** @see Check#isDigits(String) */
    public static boolean isDigits(String s) { return Check.isDigits(s); }
    /** @see Check#isAlphanumeric(String) */
    public static boolean isAlphanumeric(String s) {
        return Check.isAlphanumeric(s);
    }
    /** @see Check#isHexColor(String) */
    public static boolean isHexColor(String s) { return Check.isHexColor(s); }
    /** @see Check#isIPAddress(String) */
    public static boolean isIPAddress(String s) { return Check.isIPAddress(s); }
    /** @see Check#isUUID(String) */
    public static boolean isUUID(String s) { return Check.isUUID(s); }
    /** @see Check#isUsername(String) */
    public static boolean isUsername(String s) { return Check.isUsername(s); }
    /** @see Check#isPakistaniPhone(String) */
    public static boolean isPakistaniPhone(String s) {
        return Check.isPakistaniPhone(s);
    }
    /** @see Check#isPakistaniCnic(String) */
    public static boolean isPakistaniCnic(String s) {
        return Check.isPakistaniCnic(s);
    }
    /** @see Check#isStrongPassword(String) */
    public static boolean isStrongPassword(String s) {
        return Check.isStrongPassword(s);
    }
    /** @see Check#isVeryStrongPassword(String) */
    public static boolean isVeryStrongPassword(String s) {
        return Check.isVeryStrongPassword(s);
    }
    /** @see Check#isAdult(int) */
    public static boolean isAdult(int age) { return Check.isAdult(age); }
    /** @see Check#isValidGrade(int) */
    public static boolean isValidGrade(int g) { return Check.isValidGrade(g); }
    /** @see Check#isLeapYear(int) */
    public static boolean isLeap(int y) { return Check.isLeapYear(y); }
    /** @see Check#isJSON(String) */
    public static boolean isJSON(String s) { return Check.isJSON(s); }
    /** @see Numbers#isPalindromeNumber(int) */
    public static boolean isNumericPalindrome(int n) {
        return Numbers.isPalindromeNumber(n);
    }

    /** @see Check#is(String, String) */
    public static boolean is(String value, String ruleName) {
        return Check.is(value, ruleName);
    }
    /** @see Check#is(String, Rule) */
    public static boolean is(String value, Rule rule) {
        return Check.is(value, rule);
    }

    // ================================================================ decorators

    /** @see Decorator#timed(Runnable) */
    public static Duration timed(Runnable action) {
        return Decorator.timed(action);
    }
    /** @see #timed(Runnable) */
    public static Duration measure(Runnable action) { return Decorator.timed(action); }
    /** @see Decorator#timed(Supplier) */
    public static <T> Timed<T> timed(Supplier<T> supplier) {
        return Decorator.timed(supplier);
    }

    /** @see Decorator#safe(Supplier) */
    public static <T> Optional<T> safe(Supplier<T> supplier) {
        return Decorator.safe(supplier);
    }
    /** @see Decorator#safe(Runnable) */
    public static boolean safe(Runnable action) { return Decorator.safe(action); }

    /** @see Decorator#cached(String, Supplier) */
    public static <T> T cached(String key, Supplier<T> supplier) {
        return Decorator.cached(key, supplier);
    }
    /** @see Decorator#invalidate(String) */
    public static void invalidate(String key) { Decorator.invalidate(key); }
    /** @see Decorator#clearCache() */
    public static void clearCache() { Decorator.clearCache(); }

    /** @see Decorator#logged(String, Runnable) */
    public static void logged(String label, Runnable action) {
        Decorator.logged(label, action);
    }
    /** @see Decorator#logged(String, Supplier) */
    public static <T> T logged(String label, Supplier<T> supplier) {
        return Decorator.logged(label, supplier);
    }

    /** @see Decorator#withFallback(Supplier, Object) */
    public static <T> T withFallback(Supplier<T> s, T fallback) {
        return Decorator.withFallback(s, fallback);
    }
    /** @see #withFallback(Supplier, Object) */
    public static <T> T fallback(Supplier<T> s, T fallback) {
        return Decorator.withFallback(s, fallback);
    }

    /** @see Decorator#retry(int) */
    public static Retry retry(int attempts) { return Decorator.retry(attempts); }
    /** @see Decorator#retry(Runnable) */
    public static void retry(Runnable action) { Decorator.retry(action); }
    /** @see Decorator#retry(Supplier) */
    public static <T> T retry(Supplier<T> supplier) {
        return Decorator.retry(supplier);
    }

    // ================================================================ session / rules

    /** @see Session#me() */
    public static Session session() { return Session.me(); }

    /** @see Rules#get(String) */
    public static Rule getRule(String name) { return Rules.get(name); }
    /** @see Rules#require(String) */
    public static Rule requireRule(String name) { return Rules.require(name); }
    /** @see Rules#register(Rule) */
    public static Rule registerRule(Rule rule) { return Rules.register(rule); }
    /** @see Rules#has(String) */
    public static boolean hasRule(String name) { return Rules.has(name); }
    /** @see Rules#pakistaniMobile() */
    public static Rule pkMobile() { return Rules.pakistaniMobile(); }
    /** @see Rules#pakistaniCnic() */
    public static Rule pkCnic() { return Rules.pakistaniCnic(); }

    /**
     * Validates a value against a registered rule, returning the result
     * as a {@link ValidationResult}. Unlike a raw
     * {@code Rules.get(name).test(value)} check, this returns a
     * structured result that can be rendered or inspected without a
     * boolean conversion.
     *
     * <p>Returns a failure result with a clear message when the rule
     * name is not registered — this is a design choice, not a bug:
     * "validating against a rule that does not exist" is a validation
     * failure, not a programming error worth throwing on.</p>
     *
     * @param value the value to validate
     * @param ruleName the registered rule name
     * @return a {@link ValidationResult} containing the outcome
     * @see Rules#get(String)
     * @see ValidationResult
     * @since 1.0.0
     */
    public static ValidationResult validate(String value, String ruleName) {
        Rule rule = Rules.get(ruleName);
        if (rule == null)
            return new ValidationResult(List.of("No rule named '" + ruleName + "'"));
        return rule.test(value)
                ? new ValidationResult(List.of())
                : new ValidationResult(List.of(rule.message()));
    }

    // ================================================================ http

    /** @see Http#get(String) */
    public static HttpRequest get(String url) { return Http.get(url); }
    /** @see Http#getRequest(String) */
    public static HttpRequest getRequest(String url) { return Http.getRequest(url); }
    /** @see Http#post(String) */
    public static HttpRequest post(String url) { return Http.post(url); }
    /** @see Http#put(String) */
    public static HttpRequest put(String url) { return Http.put(url); }
    /** @see Http#delete(String) */
    public static HttpRequest delete(String url) { return Http.delete(url); }
    /** @see #delete(String) */
    public static HttpRequest del(String url) { return Http.delete(url); }
    /** @see Http#patch(String) */
    public static HttpRequest patch(String url) { return Http.patch(url); }
    /** @see Http#request(HttpMethod, String) */
    public static HttpRequest request(HttpMethod method, String url) {
        return Http.request(method, url);
    }

    /** @see Http#fetch(String) */
    public static HttpResponse fetch(String url) { return Http.fetch(url); }
    /** @see Http#fetchJson(String) */
    public static HttpResponse fetchJson(String url) { return Http.fetchJson(url); }
    /** @see Http#getJson(String) */
    public static HttpResponse getJson(String url) { return Http.getJson(url); }
    /** @see Http#postJson(String, String) */
    public static HttpResponse postJson(String url, String body) {
        return Http.postJson(url, body);
    }
    /** @see Http#download(String, String) */
    public static HttpResponse download(String url, String savePath) {
        return Http.download(url, savePath);
    }

    /** @see Http#isOnline() */
    public static boolean isOnline() { return Http.isOnline(); }
    /** @see #isOnline() */
    public static boolean hasInternet() { return Http.isOnline(); }
    /** @see #isOnline() */
    public static boolean isConnected() { return Http.isOnline(); }
    /** @see Http#canReach(String, int) */
    public static boolean canReach(String host, int port) {
        return Http.canReach(host, port);
    }
    /** @see Http#ping(String) */
    public static boolean ping(String url) { return Http.ping(url); }

    // ================================================================ email

    /** @see Email#builder() */
    public static EmailRequest email() { return Email.builder(); }
    /** @see #email() */
    public static EmailRequest mail() { return Email.builder(); }
    /** @see #email() */
    public static EmailRequest composeEmail() { return Email.builder(); }

    /** @see Email#send(Object...) */
    public static EmailResult sendEmail(Object... kv) { return Email.send(kv); }
    /** @see #sendEmail(Object...) */
    public static EmailResult sendMail(Object... kv) { return Email.send(kv); }

    /** @see Email#quickSend(String, String, String, String, String, String) */
    public static EmailResult quickSend(String host, String from, String to,
            String subj, String body, String pw) {
        return Email.quickSend(host, from, to, subj, body, pw);
    }

    /** @see Email#sendWithEnv(String, String, String, String, String, String, String) */
    public static EmailResult sendWithEnv(String host, String from, String to,
            String subj, String body,
            String envFile, String envKey) {
        return Email.sendWithEnv(host, from, to, subj, body, envFile, envKey);
    }

    /** @see Email#canConnect(String, int) */
    public static boolean canConnect(String host, int port) {
        return Email.canConnect(host, port);
    }

    // ================================================================ file management

    /** @see FileIO#createDirectory(String) */
    public static boolean mkdir(String path) {
        return FileIO.createDirectory(path);
    }
    /** @see #mkdir(String) */
    public static boolean mkdirs(String path) { return FileIO.createDirectory(path); }
    /** @see #mkdir(String) */
    public static boolean createDirectory(String path) {
        return FileIO.createDirectory(path);
    }

    /** @see FileIO#deleteDirectory(String) */
    public static boolean deleteDir(String path) {
        return FileIO.deleteDirectory(path);
    }
    /** @see #deleteDir(String) */
    public static boolean removeDir(String path) { return FileIO.deleteDirectory(path); }

    /** @see FileIO#countFiles(String) */
    public static int countFiles(String directory) {
        return FileIO.countFiles(directory);
    }
    /** @see FileIO#countAllFiles(String) */
    public static int countAllFiles(String directory) {
        return FileIO.countAllFiles(directory);
    }
    /** @see FileIO#directorySize(String) */
    public static long dirSize(String directory) {
        return FileIO.directorySize(directory);
    }

    /** @see FileIO#findFiles(String, String) */
    public static List<String> findFiles(String directory, String g) {
        return FileIO.findFiles(directory, g);
    }
    /** @see FileIO#findFilesByExtension(String, String) */
    public static List<String> findFilesByExtension(String d, String e) {
        return FileIO.findFilesByExtension(d, e);
    }
    /** @see FileIO#isEmpty(String) */
    public static boolean isEmptyDir(String directory) {
        return FileIO.isEmpty(directory);
    }
    /** @see FileIO#listDirectories(String) */
    public static List<String> listDirectories(String d) {
        return FileIO.listDirectories(d);
    }

    /** @see TextFileIO#readAll(String) */
    public static String readFile(String path) {
        return TextFileIO.readAll(path);
    }
    /** @see TextFileIO#readLines(String) */
    public static List<String> readFileLines(String path) {
        return TextFileIO.readLines(path);
    }
    /** @see TextFileIO#write(String, String) */
    public static boolean writeFile(String path, String content) {
        return TextFileIO.write(path, content);
    }
    /** @see TextFileIO#append(String, String) */
    public static boolean appendFile(String path, String content) {
        return TextFileIO.append(path, content);
    }

    /** @see TextFileIO#of(String) */
    public static chirp.io.TextFile textFile(String path) {
        return TextFileIO.of(path);
    }
    /** @see CsvFile#table(String) */
    public static CsvFile.CsvTable csvTable(String path) {
        return CsvFile.table(path);
    }
    /** @see JsonFile#readObject(String) */
    public static chirp.json.JsonObject readJson(String path) {
        return JsonFile.readObject(path);
    }

    // ================================================================ console extras

    /** @see Loader#start(String) */
    public static Loader loader(String label) { return Loader.start(label); }
    /** @see Menu#Menu(String) */
    public static Menu menu(String title) { return new Menu(title); }

    // ================================================================ misc

    /**
     * Exits the JVM with the given status code. Delegates to
     * {@link System#exit(int)}.
     *
     * <p>Wrapped here because students reach for {@code exit(0)} at the
     * end of a program far more often than they type
     * {@code System.exit(0)}, and importing {@code System} for one call
     * is noise. Non-zero codes signal abnormal termination in POSIX
     * shells — use {@code 0} for success, {@code 1} for a generic
     * failure, {@code 2} for a usage error.</p>
     *
     * @param code the process exit code ({@code 0} = success)
     * @see System#exit(int)
     * @since 1.0.0
     */
    public static void exit(int code) {
        System.exit(code);
    }

    /**
     * Sleeps the current thread for the given duration, silently
     * handling interruption. Delegates to {@link Thread#sleep(long)}.
     *
     * <p>If the sleep is interrupted, the interrupt flag is restored
     * (via {@link Thread#interrupt()}) so the caller can detect the
     * interruption — but no exception is thrown. This matches the
     * student-facing expectation that "sleep" just pauses, without a
     * checked exception to declare.</p>
     *
     * <p>Zero or negative durations are a no-op (not an exception), so
     * {@code sleep(0)} and {@code sleep(-100)} are safe calls.</p>
     *
     * @param ms the duration in milliseconds (≤ 0 → no-op)
     * @see Thread#sleep(long)
     * @since 1.0.0
     */
    public static void sleep(int ms) {
        if (ms <= 0)
            return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
