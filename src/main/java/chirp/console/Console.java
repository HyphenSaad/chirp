package chirp.console;

import chirp.internal.Ansi;
import chirp.internal.Glyphs;
import chirp.session.Session;
import chirp.startup.ChirpLifecycle;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Console printing — raw, semantic, and decorative.
 *
 * <p>The most-used class in Chirp. Students use it to print messages,
 * format tables, draw separators, and style text with colour. It is a
 * thin, null-safe wrapper over {@link System#out} with three distinct
 * roles.</p>
 *
 * <h2>The three roles — different rules apply to each</h2>
 *
 * <h3>1. Raw printing — no lifecycle guard</h3>
 * <p>{@link #print(Object)}, {@link #println(Object)},
 * {@link #println()}, {@link #printf(String, Object...)},
 * {@link #printEach(List)}, {@link #printRow(Object[], String)}. These
 * pass through to {@code System.out} unchanged. They work before
 * {@code Chirp.start()} — useful for the first line of a {@code main}
 * method, before Chirp is initialised:</p>
 * <pre>{@code
 * public static void main(String[] args) {
 *     Console.println("Starting up…");   // raw — works before start()
 *     Chirp.start();
 *     Console.success("Ready.");        // semantic — requires start()
 * }
 * }</pre>
 *
 * <h3>2. Semantic printing — guarded</h3>
 * <p>{@link #success(String)}, {@link #error(String)}, {@link #warn(String)},
 * {@link #info(String)}, {@link #title(String)}, plus the decorative
 * methods ({@link #line()}, {@link #blank()}, {@link #clearScreen()}).
 * These prepend a coloured symbol (✓ ✗ ⚠ ℹ) and, on some terminals,
 * apply colour. They require {@code Chirp.start()} so that the student
 * sees a clear error if they forgot to call it:</p>
 * <pre>{@code
 * Console.success("Saved");       // ✓ Saved
 * Console.error("File missing");  // ✗ File missing
 * Console.warn("Low disk");       // ⚠ Low disk
 * Console.info("Ready");          // ℹ Ready
 * }</pre>
 *
 * <h3>3. Style helpers — pure functions</h3>
 * <p>{@link #bold(String)}, {@link #red(String)}, {@link #underline(String)},
 * and friends return a styled string. They do <b>not</b> print — the
 * caller decides when and where. Because they return a value rather
 * than writing to the console, they do not require
 * {@code Chirp.start()} — you can build styled strings at any point,
 * including before startup and in unit tests:</p>
 * <pre>{@code
 * String error = Console.red("Connection failed");
 * String heading = Console.bold("Results:");
 *
 * // Later, once Chirp is started, print them:
 * Console.println(error);
 * }</pre>
 *
 * <p>If ANSI colour is unavailable (Windows without VT support,
 * {@code NO_COLOR} environment variable set, {@code -Dchirp.ansi=false}),
 * every style helper returns the plain string. Callers never need to
 * check.</p>
 *
 * <h2>Aliases</h2>
 * <p>Every semantic method has 2–5 aliases so students can write
 * whichever name comes to mind. {@link #success(String)} has
 * {@code ok / done / good / passed}; {@link #error(String)} has
 * {@code fail / err / failure}; {@link #warn(String)} has
 * {@code warning / caution / alert}; {@link #info(String)} has
 * {@code information / note / hint}. The aliases are pure delegates —
 * identical output.</p>
 *
 * <h2>Null safety</h2>
 * <p>Every printing method accepts {@code null} and prints
 * {@code "null"} — that matches {@code System.out.println}'s behaviour.
 * Every style helper returns {@code null} for {@code null} input, so a
 * styled string propagates through a chain correctly.</p>
 *
 * <h2>Tables and arrays</h2>
 * <p>{@link #printArray(Object[])} and its primitive overloads print
 * using {@link Arrays#toString}. {@link #printMatrix(int[][])} prints
 * one row per line. {@link #table(List, String...)} renders a
 * column-aligned ASCII table with configurable headers.</p>
 *
 * <h2>Progress bars</h2>
 * <p>{@link #progress(int, int)} overwrites the current terminal line
 * with a percentage bar — call it repeatedly in a loop and it will
 * animate in place. Uses {@code \r} (carriage return) for the overwrite.</p>
 *
 * <h2>Separators</h2>
 * <p>{@link #line()} draws a horizontal separator using the
 * {@link Separator} builder. Full styling (width, character, label,
 * alignment, colour) is available via {@link #separator()} which returns
 * a {@link Separator.Builder}:</p>
 * <pre>{@code
 * Console.line();                                  // default 40-char line
 * Console.line(60);                                // 60 chars
 * Console.line(60, Separator.Style.DOUBLE);        // 60 chars of ═
 * Console.separator().width(60).centered().label("Section").println();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For prompted input, see {@link Input}. For a spinner during long
 * operations, see {@link Loader}. For interactive menus, see
 * {@link Menu}. For a configurable separator, see {@link Separator}.</p>
 *
 * @see Input
 * @see Loader
 * @see Menu
 * @see Separator
 * @see Messenger
 * @see chirp.internal.Ansi
 * @see chirp.internal.Glyphs
 * @since 1.0.0
 */
public final class Console {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Console() {}

    // ============================================================== raw printing

    /**
     * Prints a value to {@code System.out} without a newline. No lifecycle
     * guard, no styling. {@code null} prints as {@code "null"}.
     *
     * @param value the value to print (may be null)
     * @see #println(Object)
     * @see #printf(String, Object...)
     * @since 1.0.0
     */
    public static void print(Object value) { System.out.print(value); }

    /**
     * Prints a value to {@code System.out} followed by a newline. No
     * lifecycle guard, no styling. {@code null} prints as
     * {@code "null"}.
     *
     * <p>The most common raw-print method — equivalent to
     * {@code System.out.println(value)}.</p>
     *
     * @param value the value to print (may be null)
     * @see #print(Object)
     * @see #println()
     * @since 1.0.0
     */
    public static void println(Object value) { System.out.println(value); }

    /**
     * Prints a blank line. No lifecycle guard.
     *
     * @see #blank()
     * @see #println(Object)
     * @since 1.0.0
     */
    public static void println() { System.out.println(); }

    /**
     * Formats and prints via {@code System.out.printf}. No lifecycle guard.
     *
     * <pre>{@code
     * Console.printf("Hello, %s! You are %d.%n", "Saad", 20);
     * }</pre>
     *
     * @param format the {@link java.util.Formatter} format string
     * @param args the format arguments
     * @see #println(Object)
     * @see chirp.format.Format#fmt(String, Object...)
     * @since 1.0.0
     */
    public static void printf(String format, Object... args) { System.out.printf(format, args); }

    /**
     * Prints a {@code char[]} without a newline. No lifecycle guard.
     *
     * @param chars the characters to print (may be null → prints "null")
     * @see #println(char[])
     * @since 1.0.0
     */
    public static void print(char[] chars) { System.out.print(chars); }

    /**
     * Prints a {@code char[]} followed by a newline. No lifecycle guard.
     *
     * @param chars the characters to print (may be null → prints "null")
     * @see #print(char[])
     * @since 1.0.0
     */
    public static void println(char[] chars) { System.out.println(chars); }

    /**
     * Formats and prints with a single argument — a convenience overload
     * that avoids varargs allocation for the common single-value case.
     *
     * @param fmt the format string
     * @param arg the single argument
     * @see #printf(String, Object...)
     * @since 1.0.0
     */
    public static void printf(String fmt, Object arg) { System.out.printf(fmt, arg); }

    /**
     * Alias of {@link #println(Object)} — see that method for full
     * documentation.
     *
     * @param value the value to print
     * @see #println(Object)
     * @since 1.0.0
     */
    public static void printLine(Object value) { println(value); }

    /**
     * Prints a list using its {@code toString} — {@code "[a, b, c]"}.
     * No lifecycle guard.
     *
     * @param list the list to print (may be null)
     * @see #printEach(List)
     * @see #printRow(Object[], String)
     * @since 1.0.0
     */
    public static void printList(List<?> list) {
        println(list);
    }

    /**
     * Alias of {@link #printList(List)} — see that method for full
     * documentation.
     *
     * @param list the list to print
     * @see #printList(List)
     * @since 1.0.0
     */
    public static void printValues(List<?> list) { println(list); }

    /**
     * Prints each element of a list on its own line. No lifecycle guard.
     * A null list is a no-op (not even a blank line).
     *
     * <pre>{@code
     * Console.printEach(List.of("apple", "banana", "cherry"));
     * // apple
     * // banana
     * // cherry
     * }</pre>
     *
     * @param list the list to print (null → no-op)
     * @see #printList(List)
     * @since 1.0.0
     */
    public static void printEach(List<?> list) {
        if (list == null) return;
        for (Object item : list) println(item);
    }

    /**
     * Prints an array of values separated by a delimiter. No lifecycle
     * guard. A null array is a no-op.
     *
     * <pre>{@code
     * Console.printRow(new Object[]{"a", "b", "c"}, ", ");
     * // a, b, c
     * }</pre>
     *
     * @param items the values to print (null → no-op)
     * @param delimiter the separator between values (null → " ")
     * @see #printRow(Object[])
     * @see #printArray(Object[])
     * @since 1.0.0
     */
    public static void printRow(Object[] items, String delimiter) {
        if (items == null) return;
        String d = delimiter == null ? " " : delimiter;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(d);
            sb.append(items[i]);
        }
        println(sb);
    }

    /**
     * Alias of {@link #printRow(Object[], String)} with a space
     * delimiter — see that method for full documentation.
     *
     * @param items the values to print
     * @see #printRow(Object[], String)
     * @since 1.0.0
     */
    public static void printRow(Object[] items) { printRow(items, " "); }

    // ============================================================== semantic messages (guarded)

    /**
     * Prints a green success message with a ✓ prefix. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.success("File saved");    // ✓ File saved
     * }</pre>
     *
     * @param msg the message to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #error(String)
     * @see #warn(String)
     * @see #info(String)
     * @see #ok(String)
     * @see #done(String)
     * @see #good(String)
     * @see #passed(String)
     * @since 1.0.0
     */
    public static void success(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.green() + Glyphs.success() + " " + msg + Ansi.reset());
    }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @since 1.0.0
     */
    public static void ok(String msg) { success(msg); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @since 1.0.0
     */
    public static void done(String msg) { success(msg); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @since 1.0.0
     */
    public static void good(String msg) { success(msg); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @since 1.0.0
     */
    public static void passed(String msg) { success(msg); }

    /**
     * Prints a red error message with a ✗ prefix. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.error("File not found");   // ✗ File not found
     * }</pre>
     *
     * @param msg the message to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @see #fail(String)
     * @see #err(String)
     * @see #failure(String)
     * @since 1.0.0
     */
    public static void error(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.red() + Glyphs.error() + " " + msg + Ansi.reset());
    }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #error(String)
     * @since 1.0.0
     */
    public static void fail(String msg) { error(msg); }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #error(String)
     * @since 1.0.0
     */
    public static void err(String msg) { error(msg); }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #error(String)
     * @since 1.0.0
     */
    public static void failure(String msg) { error(msg); }

    /**
     * Prints a yellow warning message with a ⚠ prefix. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.warn("Disk space low");    // ⚠ Disk space low
     * }</pre>
     *
     * @param msg the message to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @see #error(String)
     * @see #warning(String)
     * @see #caution(String)
     * @see #alert(String)
     * @since 1.0.0
     */
    public static void warn(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.yellow() + Glyphs.warn() + " " + msg + Ansi.reset());
    }

    /**
     * Alias of {@link #warn(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #warn(String)
     * @since 1.0.0
     */
    public static void warning(String msg) { warn(msg); }

    /**
     * Alias of {@link #warn(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #warn(String)
     * @since 1.0.0
     */
    public static void caution(String msg) { warn(msg); }

    /**
     * Alias of {@link #warn(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #warn(String)
     * @since 1.0.0
     */
    public static void alert(String msg) { warn(msg); }

    /**
     * Prints a cyan informational message with an ℹ prefix. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.info("Ready");             // ℹ Ready
     * }</pre>
     *
     * @param msg the message to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #success(String)
     * @see #error(String)
     * @see #information(String)
     * @see #note(String)
     * @see #hint(String)
     * @since 1.0.0
     */
    public static void info(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.cyan() + Glyphs.info() + " " + msg + Ansi.reset());
    }

    /**
     * Alias of {@link #info(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #info(String)
     * @since 1.0.0
     */
    public static void information(String msg) { info(msg); }

    /**
     * Alias of {@link #info(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #info(String)
     * @since 1.0.0
     */
    public static void note(String msg) { info(msg); }

    /**
     * Alias of {@link #info(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #info(String)
     * @since 1.0.0
     */
    public static void hint(String msg) { info(msg); }

    // ============================================================== decorative (guarded)

    /**
     * Prints a thick horizontal separator of {@link Separator#DEFAULT_WIDTH}
     * characters. Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.line();   // ════════════════════════════════════════════════
     * }</pre>
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line(int)
     * @see #line(Separator.Style)
     * @see #line(int, Separator.Style)
     * @see #separator()
     * @see #hr()
     * @see #sep()
     * @see #divider()
     * @since 1.0.0
     */
    public static void line() {
        ChirpLifecycle.requireStarted();
        println(Glyphs.thickHLine(Separator.DEFAULT_WIDTH));
    }

    /**
     * Alias of {@link #line()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @since 1.0.0
     */
    public static void hr() { line(); }

    /**
     * Alias of {@link #line()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @since 1.0.0
     */
    public static void sep() { line(); }

    /**
     * Alias of {@link #line()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @since 1.0.0
     */
    public static void divider() { line(); }

    /**
     * Prints a horizontal separator of the given width using the default
     * style. Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.line(60);   // 60-character line
     * }</pre>
     *
     * @param width the width in characters
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @see #line(int, Separator.Style)
     * @see #separator()
     * @since 1.0.0
     */
    public static void line(int width) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().width(width).build().render());
    }

    /**
     * Alias of {@link #line(int)} — see that method for full
     * documentation.
     *
     * @param width the width in characters
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line(int)
     * @since 1.0.0
     */
    public static void hr(int width) { line(width); }

    /**
     * Prints a horizontal separator of the given style using the default
     * width. Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.line(Separator.Style.DOUBLE);   // ═════════════════════════
     * Console.line(Separator.Style.DASHED);   // ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
     * }</pre>
     *
     * @param style the line style (null → default solid)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @see #line(int, Separator.Style)
     * @see Separator.Style
     * @since 1.0.0
     */
    public static void line(Separator.Style style) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().style(style).build().render());
    }

    /**
     * Prints a horizontal separator of the given width and style. Requires
     * {@code Chirp.start()}.
     *
     * @param width the width in characters
     * @param style the line style (null → default solid)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @see #line(int)
     * @see #line(Separator.Style)
     * @see #separator()
     * @since 1.0.0
     */
    public static void line(int width, Separator.Style style) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().width(width).style(style).build().render());
    }

    /**
     * Prints a pre-configured {@link Separator}. Requires
     * {@code Chirp.start()}.
     *
     * @param separator the separator (null → no-op)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #line()
     * @see Separator
     * @since 1.0.0
     */
    public static void line(Separator separator) {
        ChirpLifecycle.requireStarted();
        if (separator != null) println(separator.render());
    }

    /**
     * Returns a fresh {@link Separator.Builder} for fluent separator
     * configuration. Does not print — call {@link Separator.Builder#build()}
     * and {@link Separator#print()} (or {@link Console#line(Separator)})
     * to render.
     *
     * <pre>{@code
     * Console.separator()
     *     .width(60)
     *     .style(Separator.Style.DOUBLE)
     *     .centered()
     *     .label("Section")
     *     .build()
     *     .print();
     * }</pre>
     *
     * @return a new builder
     * @see Separator
     * @see Separator.Builder
     * @since 1.0.0
     */
    public static Separator.Builder separator() { return Separator.builder(); }

    /**
     * Prints a blank line. No lifecycle guard (unlike the other
     * decorative methods — a blank line is always safe).
     *
     * @see #println()
     * @see #nl()
     * @see #newline()
     * @see #emptyLine()
     * @since 1.0.0
     */
    public static void blank() { println(); }

    /**
     * Alias of {@link #blank()} — see that method for full
     * documentation.
     *
     * @see #blank()
     * @since 1.0.0
     */
    public static void nl() { blank(); }

    /**
     * Alias of {@link #blank()} — see that method for full
     * documentation.
     *
     * @see #blank()
     * @since 1.0.0
     */
    public static void newline() { blank(); }

    /**
     * Alias of {@link #blank()} — see that method for full
     * documentation.
     *
     * @see #blank()
     * @since 1.0.0
     */
    public static void emptyLine() { blank(); }

    /**
     * Prints a boxed title with horizontal separator bars above and
     * below. Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.title("Results");
     * // ════════════════════════════════════════
     * // │ Results
     * // ════════════════════════════════════════
     * }</pre>
     *
     * @param text the title text (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #heading(String)
     * @see #header(String)
     * @see #section(String)
     * @since 1.0.0
     */
    public static void title(String text) {
        ChirpLifecycle.requireStarted();
        String bar = Glyphs.thickHLine(Separator.DEFAULT_WIDTH);
        println(Ansi.cyan() + bar + Ansi.reset());
        println(Ansi.cyan() + Glyphs.vertical() + " " + text + Ansi.reset());
        println(Ansi.cyan() + bar + Ansi.reset());
    }

    /**
     * Alias of {@link #title(String)} — see that method for full
     * documentation.
     *
     * @param text the title text
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #title(String)
     * @since 1.0.0
     */
    public static void heading(String text) { title(text); }

    /**
     * Alias of {@link #title(String)} — see that method for full
     * documentation.
     *
     * @param text the title text
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #title(String)
     * @since 1.0.0
     */
    public static void header(String text) { title(text); }

    /**
     * Alias of {@link #title(String)} — see that method for full
     * documentation.
     *
     * @param text the title text
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #title(String)
     * @since 1.0.0
     */
    public static void section(String text) { title(text); }

    /**
     * Prints the current {@link Session} information as a dim
     * watermark block. Typically called at the end of a screenshot to
     * identify the student. A no-op if the Session has no fields set.
     * Requires {@code Chirp.start()}.
     *
     * <pre>{@code
     * Session.me()
     *     .name("Saad Mansoor")
     *     .rollNumber("2024-CS-001");
     * Console.watermark();
     * // ────────────────────────────────────────────────────
     * // Name: Saad Mansoor
     * // Roll: 2024-CS-001
     * // ────────────────────────────────────────────────────
     * }</pre>
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #stamp()
     * @see #signature()
     * @see Session
     * @since 1.0.0
     */
    public static void watermark() {
        ChirpLifecycle.requireStarted();
        String summary = Session.me().summary();
        if (summary.isBlank()) return;
        String bar = Glyphs.hLine(Separator.DEFAULT_WIDTH);
        println(Ansi.dim() + bar + Ansi.reset());
        for (String line : summary.stripTrailing().split("\n")) {
            println(Ansi.dim() + line + Ansi.reset());
        }
        println(Ansi.dim() + bar + Ansi.reset());
    }

    /**
     * Alias of {@link #watermark()} — see that method for full
     * documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #watermark()
     * @since 1.0.0
     */
    public static void stamp() { watermark(); }

    /**
     * Alias of {@link #watermark()} — see that method for full
     * documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #watermark()
     * @since 1.0.0
     */
    public static void signature() { watermark(); }

    // ============================================================== style helpers (no guard)

    /**
     * Wraps the text in ANSI bold. Returns {@code null} for null input.
     * No lifecycle guard — the result is a string, not a print call.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #strong(String)
     * @see chirp.internal.Ansi
     * @since 1.0.0
     */
    public static String bold(String text) { return Ansi.bold() + text + Ansi.reset(); }

    /**
     * Alias of {@link #bold(String)} — see that method for full
     * documentation.
     *
     * @param text the text to style
     * @return the styled string
     * @see #bold(String)
     * @since 1.0.0
     */
    public static String strong(String text) { return bold(text); }

    /**
     * Wraps the text in ANSI underline. Returns {@code null} for null
     * input. No lifecycle guard.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #ul(String)
     * @since 1.0.0
     */
    public static String underline(String text) { return Ansi.underline() + text + Ansi.reset(); }

    /**
     * Alias of {@link #underline(String)} — see that method for full
     * documentation.
     *
     * @param text the text to style
     * @return the styled string
     * @see #underline(String)
     * @since 1.0.0
     */
    public static String ul(String text) { return underline(text); }

    /**
     * Wraps the text in red foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String red(String text) { return Ansi.red() + text + Ansi.reset(); }

    /**
     * Wraps the text in green foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String green(String text) { return Ansi.green() + text + Ansi.reset(); }

    /**
     * Wraps the text in yellow foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String yellow(String text) { return Ansi.yellow() + text + Ansi.reset(); }

    /**
     * Wraps the text in blue foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String blue(String text) { return Ansi.blue() + text + Ansi.reset(); }

    /**
     * Wraps the text in cyan foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String cyan(String text) { return Ansi.cyan() + text + Ansi.reset(); }

    /**
     * Wraps the text in magenta foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String magenta(String text) { return Ansi.magenta() + text + Ansi.reset(); }

    /**
     * Wraps the text in gray foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #grey(String)
     * @since 1.0.0
     */
    public static String gray(String text) { return Ansi.gray() + text + Ansi.reset(); }

    /**
     * Alias of {@link #gray(String)} using the British spelling
     * {@code grey}. See that method for full documentation.
     *
     * @param text the text to style
     * @return the styled string
     * @see #gray(String)
     * @since 1.0.0
     */
    public static String grey(String text) { return gray(text); }

    /**
     * Wraps the text in white foreground. Returns {@code null} for null
     * input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @see #color(String, String)
     * @since 1.0.0
     */
    public static String white(String text) { return Ansi.white() + text + Ansi.reset(); }

    /**
     * Wraps the text in dim (reduced intensity). Returns {@code null}
     * for null input.
     *
     * @param text the text to style (may be null)
     * @return the styled string, or {@code null}
     * @since 1.0.0
     */
    public static String dim(String text) { return Ansi.dim() + text + Ansi.reset(); }

    /**
     * Wraps the text with an arbitrary ANSI escape code. Returns
     * {@code null} for null input. Use this for escape codes the
     * convenience methods don't cover — e.g. 24-bit colour via
     * {@link chirp.internal.Ansi#rgb(int, int, int)}.
     *
     * <pre>{@code
     * String orange = Console.color("Hi", Ansi.rgb(255, 128, 0));
     * }</pre>
     *
     * @param text the text to style (may be null)
     * @param ansiCode the ANSI escape prefix (from {@link Ansi})
     * @return the styled string, or {@code null}
     * @see Ansi
     * @since 1.0.0
     */
    public static String color(String text, String ansiCode) {
        if (text == null) return null;
        return ansiCode + text + Ansi.reset();
    }

    /**
     * Wraps the text in double quotes. Returns empty string for null
     * input (not {@code "null"} — an empty result reads better than a
     * quoted null).
     *
     * @param text the text to quote (may be null)
     * @return the quoted text, or ""
     * @see #singleQuote(String)
     * @since 1.0.0
     */
    public static String quote(String text) { return text == null ? "" : "\"" + text + "\""; }

    /**
     * Wraps the text in single quotes. Returns empty string for null
     * input.
     *
     * @param text the text to quote (may be null)
     * @return the quoted text, or ""
     * @see #quote(String)
     * @since 1.0.0
     */
    public static String singleQuote(String text) { return text == null ? "" : "'" + text + "'"; }

    /**
     * Wraps the text in parentheses. Returns empty string for null
     * input.
     *
     * @param text the text to wrap (may be null)
     * @return the wrapped text, or ""
     * @since 1.0.0
     */
    public static String parenthesize(String text) { return text == null ? "" : "(" + text + ")"; }

    /**
     * Wraps the text in square brackets. Returns empty string for null
     * input.
     *
     * @param text the text to wrap (may be null)
     * @return the wrapped text, or ""
     * @since 1.0.0
     */
    public static String brackets(String text) { return text == null ? "" : "[" + text + "]"; }

    /**
     * Wraps the text in angle brackets. Returns empty string for null
     * input.
     *
     * @param text the text to wrap (may be null)
     * @return the wrapped text, or ""
     * @since 1.0.0
     */
    public static String angleBrackets(String text) { return text == null ? "" : "<" + text + ">"; }

    /**
     * Wraps the text in curly braces. Returns empty string for null
     * input.
     *
     * @param text the text to wrap (may be null)
     * @return the wrapped text, or ""
     * @since 1.0.0
     */
    public static String braces(String text) { return text == null ? "" : "{" + text + "}"; }

    /**
     * Returns the current time formatted as {@code HH:mm:ss}.
     *
     * @return the timestamp string
     * @see #time()
     * @since 1.0.0
     */
    public static String timestamp() { return LocalTime.now().format(TIMESTAMP); }

    /**
     * Alias of {@link #timestamp()} — see that method for full
     * documentation.
     *
     * @return the timestamp string
     * @see #timestamp()
     * @since 1.0.0
     */
    public static String time() { return timestamp(); }

    // ============================================================== arrays / matrices (guarded)

    /**
     * Prints an {@code int[]} using {@link Arrays#toString(int[])}.
     * Requires {@code Chirp.start()}.
     *
     * @param a the array to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(Object[])
     * @see #showArray(int[])
     * @since 1.0.0
     */
    public static void printArray(int[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    /**
     * Prints a {@code double[]} using {@link Arrays#toString(double[])}.
     * Requires {@code Chirp.start()}.
     *
     * @param a the array to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(int[])
     * @since 1.0.0
     */
    public static void printArray(double[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    /**
     * Prints a {@code boolean[]} using {@link Arrays#toString(boolean[])}.
     * Requires {@code Chirp.start()}.
     *
     * @param a the array to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @since 1.0.0
     */
    public static void printArray(boolean[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    /**
     * Prints a {@code char[]} using {@link Arrays#toString(char[])}.
     * Requires {@code Chirp.start()}.
     *
     * @param a the array to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @since 1.0.0
     */
    public static void printArray(char[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    /**
     * Prints an {@code Object[]} using {@link Arrays#toString(Object[])}.
     * Requires {@code Chirp.start()}.
     *
     * @param a the array to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(int[])
     * @see #printEach(List)
     * @since 1.0.0
     */
    public static void printArray(Object[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    /**
     * Alias of {@link #printArray(int[])} — see that method for full
     * documentation.
     *
     * @param a the array to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(int[])
     * @since 1.0.0
     */
    public static void showArray(int[] a) { printArray(a); }

    /**
     * Alias of {@link #printArray(double[])} — see that method for full
     * documentation.
     *
     * @param a the array to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(double[])
     * @since 1.0.0
     */
    public static void showArray(double[] a) { printArray(a); }

    /**
     * Alias of {@link #printArray(Object[])} for {@code String[]} — see
     * that method for full documentation.
     *
     * @param a the array to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(Object[])
     * @since 1.0.0
     */
    public static void showArray(String[] a) { printArray((Object[]) a); }

    /**
     * Alias of {@link #printArray(int[])} — see that method for full
     * documentation.
     *
     * @param a the array to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printArray(int[])
     * @since 1.0.0
     */
    public static void displayArray(int[] a) { printArray(a); }

    /**
     * Prints a 2D {@code int[][]} matrix, one row per line. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Console.printMatrix(new int[][]{{1, 2}, {3, 4}});
     * // [1, 2]
     * // [3, 4]
     * }</pre>
     *
     * @param m the matrix to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printMatrix(String[][])
     * @see #showMatrix(int[][])
     * @since 1.0.0
     */
    public static void printMatrix(int[][] m) {
        ChirpLifecycle.requireStarted();
        if (m == null) { println("null"); return; }
        for (int[] row : m) printArray(row);
    }

    /**
     * Prints a 2D {@code String[][]} matrix, one row per line. Requires
     * {@code Chirp.start()}.
     *
     * @param m the matrix to print (may be null → prints "null")
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printMatrix(int[][])
     * @since 1.0.0
     */
    public static void printMatrix(String[][] m) {
        ChirpLifecycle.requireStarted();
        if (m == null) { println("null"); return; }
        for (String[] row : m) printArray(row);
    }

    /**
     * Alias of {@link #printMatrix(int[][])} — see that method for full
     * documentation.
     *
     * @param m the matrix to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printMatrix(int[][])
     * @since 1.0.0
     */
    public static void showMatrix(int[][] m) { printMatrix(m); }

    /**
     * Alias of {@link #printMatrix(String[][])} — see that method for
     * full documentation.
     *
     * @param m the matrix to print
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #printMatrix(String[][])
     * @since 1.0.0
     */
    public static void showMatrix(String[][] m) { printMatrix(m); }

    // ============================================================== table (guarded)

    /**
     * Prints a column-aligned ASCII table with headers. Requires
     * {@code Chirp.start()}.
     *
     * <p>Each row is a {@code String[]}; the header array defines the
     * column names. Column widths are computed from the widest cell in
     * each column. A null row is skipped; a row shorter than the header
     * is padded with blanks.</p>
     *
     * <pre>{@code
     * List<String[]> rows = List.of(
     *     new String[]{"Saad", "95", "A"},
     *     new String[]{"Ali",  "88", "B"});
     * Console.table(rows, "Name", "Score", "Grade");
     * // Name  Score  Grade
     * // ----  -----  -----
     * // Saad  95     A
     * // Ali   88     B
     * }</pre>
     *
     * @param rows the row data (may be null or empty → no-op)
     * @param headers the column headers (may be null or empty → no-op)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #grid(List, String...)
     * @since 1.0.0
     */
    public static void table(List<String[]> rows, String... headers) {
        ChirpLifecycle.requireStarted();
        if (rows == null || headers == null || headers.length == 0) return;
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) widths[i] = headers[i] == null ? 0 : headers[i].length();
        for (String[] row : rows) {
            if (row == null) continue;
            for (int i = 0; i < headers.length && i < row.length; i++) {
                if (row[i] != null) widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        printTableRow(headers, widths);
        for (int w : widths) print(Glyphs.hLine(w + 2));
        println();
        for (String[] row : rows) {
            if (row == null) continue;
            printTableRow(row, widths);
        }
    }

    /**
     * Alias of {@link #table(List, String...)} — see that method for
     * full documentation and examples.
     *
     * @param rows the row data
     * @param headers the column headers
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #table(List, String...)
     * @since 1.0.0
     */
    public static void grid(List<String[]> rows, String... headers) { table(rows, headers); }

    private static void printTableRow(String[] cells, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.length && cells[i] != null ? cells[i] : "";
            printf("%-" + (widths[i] + 2) + "s", cell);
        }
        println();
    }

    // ============================================================== progress (guarded)

    /**
     * Renders a progress bar by overwriting the current terminal line
     * with a percentage indicator. Requires {@code Chirp.start()}.
     *
     * <p>Call repeatedly in a loop — the {@code \r} character moves the
     * cursor back to the line start, so the bar animates in place. When
     * {@code current >= total}, a newline is printed so the next output
     * goes on a fresh line.</p>
     *
     * <pre>{@code
     * for (int i = 0; i <= 100; i++) {
     *     Console.progress(i, 100);
     *     Thread.sleep(50);
     * }
     * // [####################] 100%
     * }</pre>
     *
     * @param current the current progress (0 to total)
     * @param total the total (0 → no-op)
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #bar(int, int)
     * @see #progressBar(int, int)
     * @see chirp.console.Loader
     * @since 1.0.0
     */
    public static void progress(int current, int total) {
        ChirpLifecycle.requireStarted();
        if (total <= 0) return;
        int percent = (int) (100.0 * current / total);
        int filled = percent / 5;
        String bar = "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + percent + "%";
        print("\r" + bar);
        if (current >= total) println();
    }

    /**
     * Alias of {@link #progress(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param current the current progress
     * @param total the total
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #progress(int, int)
     * @since 1.0.0
     */
    public static void bar(int current, int total) { progress(current, total); }

    /**
     * Alias of {@link #progress(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param current the current progress
     * @param total the total
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #progress(int, int)
     * @since 1.0.0
     */
    public static void progressBar(int current, int total) { progress(current, total); }

    // ============================================================== screen (guarded)

    /**
     * Clears the terminal screen. Requires {@code Chirp.start()}.
     *
     * <p>Uses ANSI escape codes when supported, otherwise delegates to
     * the platform command ({@code cls} on Windows, {@code clear}
     * elsewhere). A silent no-op if neither works.</p>
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #clear()
     * @see #cls()
     * @see #clearScreenInternal()
     * @since 1.0.0
     */
    public static void clearScreen() {
        ChirpLifecycle.requireStarted();
        clearScreenInternal();
    }

    /**
     * Alias of {@link #clearScreen()} — see that method for full
     * documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #clearScreen()
     * @since 1.0.0
     */
    public static void clear() { clearScreen(); }

    /**
     * Alias of {@link #clearScreen()} — see that method for full
     * documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #clearScreen()
     * @since 1.0.0
     */
    public static void cls() { clearScreen(); }

    /**
     * Clears the terminal screen without a lifecycle guard. Used
     * internally by {@link chirp.startup.Banner} during startup —
     * exposed publicly because other startup machinery needs it and
     * duplicating the platform detection would be worse.
     *
     * @see #clearScreen()
     * @since 1.0.0
     */
    public static void clearScreenInternal() {
        if (Ansi.isSupported()) {
            print("\033[H\033[2J\033[3J");
            System.out.flush();
            return;
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            ProcessBuilder builder;
            if (os.contains("win")) builder = new ProcessBuilder("cmd", "/c", "cls");
            else builder = new ProcessBuilder("clear");
            builder.inheritIO().start().waitFor();
        } catch (IOException e) {
            print("\033[H\033[2J");
            System.out.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
