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
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Immediate</b> — pass a prompt: {@code int n = readInt("Age");}</li>
 *   <li><b>Fluent</b> — omit the prompt: {@code int n = readInt().label("Age").between(1,130).read();}</li>
 * </ul>
 *
 * <h2>Naming — one canonical + many aliases</h2>
 * <p>Every read family has 6–10 aliases so students can write whichever name
 * comes to mind: {@code readInt / readInteger / takeInt / getInt / askInt /
 * promptInt / scanInt / readNumber / readWhole}; {@code readLine / readString /
 * takeLine / getLine / askLine / inputLine}; {@code readYesNo / readBool /
 * readConfirm / askConfirm / takeYesNo}.</p>
 *
 * <p>Every read is guarded by {@link ChirpLifecycle#requireStarted()}.
 * {@link #readRaw(String)} is the low-level primitive used internally —
 * no guard, no warnings.</p>
 *
 * <p><b>Encoding:</b> {@code System.in} and any custom {@link InputStream}
 * are decoded as UTF-8 explicitly. Relying on the platform default charset
 * would silently mis-decode non-ASCII prompts and answers on Windows
 * consoles whose codepage is not UTF-8.</p>
 *
 * <p><b>Locale:</b> case-insensitive y/n parsing uses {@link Locale#ROOT} so
 * Turkish-locale machines treat {@code "Y"} the same as everywhere else.</p>
 */
public final class Input {

    private static Scanner SCANNER = new Scanner(System.in, StandardCharsets.UTF_8);

    private Input() {}

    // ============================================================== test hooks

    public static void setInputStream(InputStream in) { SCANNER = new Scanner(in, StandardCharsets.UTF_8); }
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

    /** Low-level primitive — no guard, no warnings. */
    public static String readRaw(String prompt) {
        System.out.print(prompt + ": ");
        return nextLine();
    }

    // ============================================================== int

    public static int readInt(String prompt) {
        ChirpLifecycle.requireStarted();
        Trace.enter("input", "readInt", "prompt=" + prompt);
        Strict.warnOnce("int", prompt, ValidationCategory.RANGE);
        int value = readIntRaw(prompt);
        Trace.exit("input", "readInt", value);
        return value;
    }

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

    public static IntReader readInt() {
        ChirpLifecycle.requireStarted();
        Trace.trace("input", "readInt() -> IntReader (fluent)");
        return IntReader.create();
    }

    public static int readInteger(String prompt) { return readInt(prompt); }
    public static int readInteger(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static IntReader readInteger() { return readInt(); }
    public static int takeInt(String prompt) { return readInt(prompt); }
    public static int takeInt(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static IntReader takeInt() { return readInt(); }
    public static int getInt(String prompt) { return readInt(prompt); }
    public static int getInt(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static IntReader getInt() { return readInt(); }
    public static int askInt(String prompt) { return readInt(prompt); }
    public static int askInt(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static IntReader askInt() { return readInt(); }
    public static int inputInt(String prompt) { return readInt(prompt); }
    public static int inputInt(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static IntReader inputInt() { return readInt(); }
    public static int promptInt(String prompt) { return readInt(prompt); }
    public static IntReader promptInt() { return readInt(); }
    public static int scanInt(String prompt) { return readInt(prompt); }
    public static IntReader scanInt() { return readInt(); }
    public static int readNumber(String prompt) { return readInt(prompt); }
    public static IntReader readNumber() { return readInt(); }
    public static int readWhole(String prompt) { return readInt(prompt); }
    public static IntReader readWhole() { return readInt(); }

    // ---------------------------------------------------------- int range helpers
    public static int readIntRange(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static int readBoundedInt(String prompt, int min, int max) { return readInt(prompt, min, max); }
    public static int readIntBetween(String prompt, int min, int max) { return readInt(prompt, min, max); }

    public static int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }
    public static int readPositiveNumber(String prompt) { return readPositiveInt(prompt); }

    public static int readNonNegativeInt(String prompt) {
        return readInt(prompt, 0, Integer.MAX_VALUE);
    }
    public static int readNatural(String prompt) { return readPositiveInt(prompt); }

    public static int readNonPositiveInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, 0);
    }

    // ============================================================== double

    public static double readDouble(String prompt) {
        ChirpLifecycle.requireStarted();
        Strict.warnOnce("double", prompt, ValidationCategory.RANGE);
        return readDoubleRaw(prompt);
    }

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

    public static DoubleReader readDouble() {
        ChirpLifecycle.requireStarted();
        return DoubleReader.create();
    }

    public static double readDecimal(String prompt) { return readDouble(prompt); }
    public static DoubleReader readDecimal() { return readDouble(); }
    public static double readFloat(String prompt) { return readDouble(prompt); }
    public static DoubleReader readFloat() { return readDouble(); }
    public static double takeDouble(String prompt) { return readDouble(prompt); }
    public static DoubleReader takeDouble() { return readDouble(); }
    public static double getDouble(String prompt) { return readDouble(prompt); }
    public static DoubleReader getDouble() { return readDouble(); }
    public static double askDouble(String prompt) { return readDouble(prompt); }
    public static DoubleReader askDouble() { return readDouble(); }
    public static double inputDouble(String prompt) { return readDouble(prompt); }
    public static DoubleReader inputDouble() { return readDouble(); }

    public static double readDoubleRange(String prompt, double min, double max) {
        return readDouble(prompt, min, max);
    }

    public static double readPositiveDouble(String prompt) {
        return readDouble(prompt, 0.0000001, Double.MAX_VALUE);
    }

    // ============================================================== string

    public static String readLine(String prompt) {
        ChirpLifecycle.requireStarted();
        Strict.warnOnce("string", prompt,
                ValidationCategory.BLANK, ValidationCategory.LENGTH, ValidationCategory.FORMAT);
        return readRaw(prompt);
    }

    public static StringReader readLine() {
        ChirpLifecycle.requireStarted();
        return StringReader.create();
    }

    public static String readString(String prompt) { return readLine(prompt); }
    public static StringReader readString() { return readLine(); }
    public static String readText(String prompt) { return readLine(prompt); }
    public static StringReader readText() { return readLine(); }
    public static String takeString(String prompt) { return readLine(prompt); }
    public static StringReader takeString() { return readLine(); }
    public static String takeLine(String prompt) { return readLine(prompt); }
    public static StringReader takeLine() { return readLine(); }
    public static String getString(String prompt) { return readLine(prompt); }
    public static StringReader getString() { return readLine(); }
    public static String getLine(String prompt) { return readLine(prompt); }
    public static StringReader getLine() { return readLine(); }
    public static String askLine(String prompt) { return readLine(prompt); }
    public static StringReader askLine() { return readLine(); }
    public static String askString(String prompt) { return readLine(prompt); }
    public static StringReader askString() { return readLine(); }
    public static String inputLine(String prompt) { return readLine(prompt); }
    public static StringReader inputLine() { return readLine(); }
    public static String inputString(String prompt) { return readLine(prompt); }
    public static StringReader inputString() { return readLine(); }
    public static String scanLine(String prompt) { return readLine(prompt); }
    public static String scanString(String prompt) { return readLine(prompt); }

    public static String readNonEmpty(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (!value.isEmpty()) return value;
            Messenger.error("This field cannot be empty.");
        }
    }
    public static String readRequired(String prompt) { return readNonEmpty(prompt); }
    public static String readLineNonEmpty(String prompt) { return readNonEmpty(prompt); }

    // ============================================================== boolean

    public static boolean readYesNo(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt + " (y/n)").toLowerCase(Locale.ROOT);
            if (value.equals("y") || value.equals("yes")) return true;
            if (value.equals("n") || value.equals("no")) return false;
            Messenger.error("Please type y or n.");
        }
    }

    public static BooleanReader readBoolean() {
        ChirpLifecycle.requireStarted();
        return BooleanReader.create();
    }

    public static boolean readBool(String prompt) { return readYesNo(prompt); }
    public static BooleanReader readBool() { return readBoolean(); }
    public static boolean readConfirm(String prompt) { return readYesNo(prompt); }
    public static boolean confirm(String prompt) { return readYesNo(prompt); }
    public static boolean askConfirm(String prompt) { return readYesNo(prompt); }
    public static boolean askYesNo(String prompt) { return readYesNo(prompt); }
    public static boolean takeYesNo(String prompt) { return readYesNo(prompt); }
    public static boolean inputYesNo(String prompt) { return readYesNo(prompt); }

    // ============================================================== char

    public static char readChar(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (value.length() == 1) return value.charAt(0);
            Messenger.error("Please enter exactly one character.");
        }
    }

    public static CharReader readCharacter() {
        ChirpLifecycle.requireStarted();
        return CharReader.create();
    }

    public static char readLetter(String prompt) { return readChar(prompt); }
    public static char readSingleChar(String prompt) { return readChar(prompt); }
    public static char takeChar(String prompt) { return readChar(prompt); }
    public static char getChar(String prompt) { return readChar(prompt); }
    public static char askChar(String prompt) { return readChar(prompt); }

    // ============================================================== password

    public static String readPassword(String prompt) {
        ChirpLifecycle.requireStarted();
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt + ": ");
            return chars == null ? "" : new String(chars);
        }
        return readRaw(prompt);
    }
    public static String askPassword(String prompt) { return readPassword(prompt); }
    public static String getPassword(String prompt) { return readPassword(prompt); }
    public static String inputPassword(String prompt) { return readPassword(prompt); }

    // ============================================================== domain

    public static String readEmail(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isEmail(value)) return value;
            Messenger.error("That doesn't look like an email address.");
        }
    }

    public static String readPhone(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isPhone(value)) return value;
            Messenger.error("That doesn't look like a phone number.");
        }
    }

    public static String readURL(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            if (Check.isURL(value)) return value;
            Messenger.error("That doesn't look like a URL.");
        }
    }

    public static String readUrl(String prompt) { return readURL(prompt); }
    public static String askEmail(String prompt) { return readEmail(prompt); }
    public static String askPhone(String prompt) { return readPhone(prompt); }
    public static String askURL(String prompt) { return readURL(prompt); }
    public static String takeEmail(String prompt) { return readEmail(prompt); }
    public static String getEmail(String prompt) { return readEmail(prompt); }

    // ============================================================== date / time

    public static LocalDate readDate(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalDate date = DateTime.parseDate(value);
            if (date != null) return date;
            Messenger.error("Please enter a valid date (e.g. 2026-10-31).");
        }
    }
    public static DateReader readDate() {
        ChirpLifecycle.requireStarted();
        return DateReader.create();
    }

    public static LocalTime readTime(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalTime time = DateTime.parseTime(value);
            if (time != null) return time;
            Messenger.error("Please enter a valid time (e.g. 14:30).");
        }
    }
    public static TimeReader readTime() {
        ChirpLifecycle.requireStarted();
        return TimeReader.create();
    }

    public static LocalDateTime readDateTime(String prompt) {
        ChirpLifecycle.requireStarted();
        while (true) {
            String value = readRaw(prompt);
            LocalDateTime dt = DateTime.parseDateTime(value);
            if (dt != null) return dt;
            Messenger.error("Please enter a valid date and time.");
        }
    }
    public static DateTimeReader readDateTime() {
        ChirpLifecycle.requireStarted();
        return DateTimeReader.create();
    }

    public static LocalDate readDay(String prompt) { return readDate(prompt); }

    // ============================================================== choice

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

    public static String choose(String prompt, String... options) { return readChoice(prompt, options); }
    public static String pickOption(String prompt, String... options) { return readChoice(prompt, options); }
    public static String readMenu(String prompt, String... options) { return readChoice(prompt, options); }
    public static String readOption(String prompt, String... options) { return readChoice(prompt, options); }

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
    public static int readChoiceNumber(String prompt, String... options) {
        return readChoiceIndex(prompt, options) + 1;
    }
}
