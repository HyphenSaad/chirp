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
 * Console printing, semantic messages, tables, progress bars, and separators.
 *
 * <h2>Naming</h2>
 * <p>Every semantic / decorative method has 2–5 aliases:
 * {@code success / ok / done / good / passed};
 * {@code error / fail / err / failure};
 * {@code warn / warning / caution / alert};
 * {@code info / information / note / hint};
 * {@code title / heading / header / section};
 * {@code line / hr / sep / divider};
 * {@code blank / nl / newline};
 * {@code clearScreen / clear / cls}.</p>
 *
 * <h2>Two modes for printing</h2>
 * <ul>
 *   <li><b>Raw</b> — {@code print / println / printf}. No lifecycle guard.</li>
 *   <li><b>Decorated</b> — semantic / decorative methods. Require
 *       {@code Chirp.start()} via the lifecycle guard.</li>
 * </ul>
 *
 * <h2>Style helpers</h2>
 * <p>{@code bold / strong}, {@code underline / ul}, {@code red / green / ... }
 * return styled strings, no lifecycle needed — you can build styled strings
 * even before start (useful for test snapshots).</p>
 */
public final class Console {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Console() {}

    // ============================================================== raw printing

    public static void print(Object value) { System.out.print(value); }
    public static void println(Object value) { System.out.println(value); }
    public static void println() { System.out.println(); }
    public static void printf(String format, Object... args) { System.out.printf(format, args); }
    public static void print(char[] chars) { System.out.print(chars); }
    public static void println(char[] chars) { System.out.println(chars); }
    public static void printf(String fmt, Object arg) { System.out.printf(fmt, arg); }
    public static void printLine(Object value) { println(value); }

    /** Prints a list using its {@code toString}. No guard, no styling. */
    public static void printList(List<?> list) {
        println(list);
    }
    public static void printValues(List<?> list) { println(list); }

    /** Prints each element of a list on its own line. No guard. */
    public static void printEach(List<?> list) {
        if (list == null) return;
        for (Object item : list) println(item);
    }

    /** Prints an array of items separated by a delimiter. No guard. */
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
    public static void printRow(Object[] items) { printRow(items, " "); }

    // ============================================================== semantic messages (guarded)

    public static void success(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.green() + Glyphs.success() + " " + msg + Ansi.reset());
    }
    public static void ok(String msg) { success(msg); }
    public static void done(String msg) { success(msg); }
    public static void good(String msg) { success(msg); }
    public static void passed(String msg) { success(msg); }

    public static void error(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.red() + Glyphs.error() + " " + msg + Ansi.reset());
    }
    public static void fail(String msg) { error(msg); }
    public static void err(String msg) { error(msg); }
    public static void failure(String msg) { error(msg); }

    public static void warn(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.yellow() + Glyphs.warn() + " " + msg + Ansi.reset());
    }
    public static void warning(String msg) { warn(msg); }
    public static void caution(String msg) { warn(msg); }
    public static void alert(String msg) { warn(msg); }

    public static void info(String msg) {
        ChirpLifecycle.requireStarted();
        println(Ansi.cyan() + Glyphs.info() + " " + msg + Ansi.reset());
    }
    public static void information(String msg) { info(msg); }
    public static void note(String msg) { info(msg); }
    public static void hint(String msg) { info(msg); }

    // ============================================================== decorative (guarded)

    public static void line() {
        ChirpLifecycle.requireStarted();
        println(Glyphs.thickHLine(Separator.DEFAULT_WIDTH));
    }
    public static void hr() { line(); }
    public static void sep() { line(); }
    public static void divider() { line(); }

    public static void line(int width) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().width(width).build().render());
    }
    public static void hr(int width) { line(width); }

    public static void line(Separator.Style style) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().style(style).build().render());
    }

    public static void line(int width, Separator.Style style) {
        ChirpLifecycle.requireStarted();
        println(Separator.builder().width(width).style(style).build().render());
    }

    public static void line(Separator separator) {
        ChirpLifecycle.requireStarted();
        if (separator != null) println(separator.render());
    }

    public static Separator.Builder separator() { return Separator.builder(); }

    public static void blank() { println(); }
    public static void nl() { blank(); }
    public static void newline() { blank(); }
    public static void emptyLine() { blank(); }

    public static void title(String text) {
        ChirpLifecycle.requireStarted();
        String bar = Glyphs.thickHLine(Separator.DEFAULT_WIDTH);
        println(Ansi.cyan() + bar + Ansi.reset());
        println(Ansi.cyan() + Glyphs.vertical() + " " + text + Ansi.reset());
        println(Ansi.cyan() + bar + Ansi.reset());
    }
    public static void heading(String text) { title(text); }
    public static void header(String text) { title(text); }
    public static void section(String text) { title(text); }

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
    public static void stamp() { watermark(); }
    public static void signature() { watermark(); }

    // ============================================================== style helpers (no guard)

    public static String bold(String text) { return Ansi.bold() + text + Ansi.reset(); }
    public static String strong(String text) { return bold(text); }

    public static String underline(String text) { return Ansi.underline() + text + Ansi.reset(); }
    public static String ul(String text) { return underline(text); }

    public static String red(String text) { return Ansi.red() + text + Ansi.reset(); }
    public static String green(String text) { return Ansi.green() + text + Ansi.reset(); }
    public static String yellow(String text) { return Ansi.yellow() + text + Ansi.reset(); }
    public static String blue(String text) { return Ansi.blue() + text + Ansi.reset(); }
    public static String cyan(String text) { return Ansi.cyan() + text + Ansi.reset(); }
    public static String magenta(String text) { return Ansi.magenta() + text + Ansi.reset(); }
    public static String gray(String text) { return Ansi.gray() + text + Ansi.reset(); }
    public static String grey(String text) { return gray(text); }
    public static String white(String text) { return Ansi.white() + text + Ansi.reset(); }
    public static String dim(String text) { return Ansi.dim() + text + Ansi.reset(); }

    public static String color(String text, String ansiCode) {
        if (text == null) return null;
        return ansiCode + text + Ansi.reset();
    }

    public static String quote(String text) { return text == null ? "" : "\"" + text + "\""; }
    public static String singleQuote(String text) { return text == null ? "" : "'" + text + "'"; }
    public static String parenthesize(String text) { return text == null ? "" : "(" + text + ")"; }
    public static String brackets(String text) { return text == null ? "" : "[" + text + "]"; }
    public static String angleBrackets(String text) { return text == null ? "" : "<" + text + ">"; }
    public static String braces(String text) { return text == null ? "" : "{" + text + "}"; }

    public static String timestamp() { return LocalTime.now().format(TIMESTAMP); }
    public static String time() { return timestamp(); }

    // ============================================================== arrays / matrices (guarded)

    public static void printArray(int[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }
    public static void printArray(double[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }
    public static void printArray(boolean[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }
    public static void printArray(char[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }
    public static void printArray(Object[] a) { ChirpLifecycle.requireStarted(); println(Arrays.toString(a)); }

    public static void showArray(int[] a) { printArray(a); }
    public static void showArray(double[] a) { printArray(a); }
    public static void showArray(String[] a) { printArray((Object[]) a); }
    public static void displayArray(int[] a) { printArray(a); }

    public static void printMatrix(int[][] m) {
        ChirpLifecycle.requireStarted();
        if (m == null) { println("null"); return; }
        for (int[] row : m) printArray(row);
    }
    public static void printMatrix(String[][] m) {
        ChirpLifecycle.requireStarted();
        if (m == null) { println("null"); return; }
        for (String[] row : m) printArray(row);
    }
    public static void showMatrix(int[][] m) { printMatrix(m); }
    public static void showMatrix(String[][] m) { printMatrix(m); }

    // ============================================================== table (guarded)

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
    public static void grid(List<String[]> rows, String... headers) { table(rows, headers); }

    private static void printTableRow(String[] cells, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.length && cells[i] != null ? cells[i] : "";
            printf("%-" + (widths[i] + 2) + "s", cell);
        }
        println();
    }

    // ============================================================== progress (guarded)

    public static void progress(int current, int total) {
        ChirpLifecycle.requireStarted();
        if (total <= 0) return;
        int percent = (int) (100.0 * current / total);
        int filled = percent / 5;
        String bar = "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + percent + "%";
        print("\r" + bar);
        if (current >= total) println();
    }
    public static void bar(int current, int total) { progress(current, total); }
    public static void progressBar(int current, int total) { progress(current, total); }

    // ============================================================== screen (guarded)

    public static void clearScreen() {
        ChirpLifecycle.requireStarted();
        clearScreenInternal();
    }
    public static void clear() { clearScreen(); }
    public static void cls() { clearScreen(); }

    /** Internal — used by {@link chirp.startup.Banner} during startup. */
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
