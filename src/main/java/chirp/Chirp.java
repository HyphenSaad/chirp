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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Single-import facade — plus the {@link #start()} entry point that every
 * student programme needs.
 *
 * <p>
 * Every Chirp method is exposed here as a static delegate with 2–4 aliases.
 * Students only need:
 * </p>
 *
 * <pre>{@code
 * import static chirp.Chirp.*;
 * }</pre>
 */
public final class Chirp {

    private Chirp() {
    }

    // ================================================================ lifecycle

    public static void start() {
        ChirpLifecycle.start(StartupConfig.defaults());
    }

    public static void start(StartupConfig config) {
        ChirpLifecycle.start(config);
    }

    public static void start(Consumer<StartupConfig.Builder> customizer) {
        StartupConfig.Builder builder = StartupConfig.builder();
        if (customizer != null)
            customizer.accept(builder);
        ChirpLifecycle.start(builder.build());
    }

    public static void start(String... flags) {
        StartupConfig.Builder builder = StartupConfig.builder();
        if (flags != null) {
            for (String flag : flags) {
                if (flag == null)
                    continue;
                switch (flag.toLowerCase(Locale.ROOT)) {
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

    public static void init() {
        start();
    }

    public static void boot() {
        start();
    }

    public static void setup() {
        start();
    }

    public static void begin() {
        start();
    }

    public static void initialize() {
        start();
    }

    public static void startChirp() {
        start();
    }

    public static void bootChirp() {
        start();
    }

    public static void beginChirp() {
        start();
    }

    public static void initChirp() {
        start();
    }

    public static void setupChirp() {
        start();
    }

    public static void initializeChirp() {
        start();
    }

    public static void init(StartupConfig c) {
        start(c);
    }

    public static void boot(StartupConfig c) {
        start(c);
    }

    public static void setup(StartupConfig c) {
        start(c);
    }

    public static void init(String... flags) {
        start(flags);
    }

    public static void boot(String... flags) {
        start(flags);
    }

    public static boolean isStarted() {
        return ChirpLifecycle.isStarted();
    }

    public static boolean hasStarted() {
        return ChirpLifecycle.isStarted();
    }

    // ================================================================ input — int

    public static int readInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static int readInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader readInt() {
        return Input.readInt();
    }

    public static int readInteger(String prompt) {
        return Input.readInt(prompt);
    }

    public static int readInteger(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader readInteger() {
        return Input.readInt();
    }

    public static int takeInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static int takeInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader takeInt() {
        return Input.readInt();
    }

    public static int getInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static int getInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader getInt() {
        return Input.readInt();
    }

    public static int askInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static int askInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader askInt() {
        return Input.readInt();
    }

    public static int inputInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static int inputInt(String prompt, int min, int max) {
        return Input.readInt(prompt, min, max);
    }

    public static IntReader inputInt() {
        return Input.readInt();
    }

    public static int promptInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static IntReader promptInt() {
        return Input.readInt();
    }

    public static int scanInt(String prompt) {
        return Input.readInt(prompt);
    }

    public static IntReader scanInt() {
        return Input.readInt();
    }

    public static int readNumber(String prompt) {
        return Input.readInt(prompt);
    }

    public static IntReader readNumber() {
        return Input.readInt();
    }

    public static int readWhole(String prompt) {
        return Input.readInt(prompt);
    }

    public static IntReader readWhole() {
        return Input.readInt();
    }

    public static int readPositiveInt(String prompt) {
        return Input.readPositiveInt(prompt);
    }

    public static int readNonNegativeInt(String prompt) {
        return Input.readNonNegativeInt(prompt);
    }

    // ================================================================ input —
    // double

    public static double readDouble(String prompt) {
        return Input.readDouble(prompt);
    }

    public static double readDouble(String prompt, double lo, double hi) {
        return Input.readDouble(prompt, lo, hi);
    }

    public static DoubleReader readDouble() {
        return Input.readDouble();
    }

    public static double readDecimal(String prompt) {
        return Input.readDouble(prompt);
    }

    public static DoubleReader readDecimal() {
        return Input.readDouble();
    }

    public static double readFloat(String prompt) {
        return Input.readDouble(prompt);
    }

    public static DoubleReader readFloat() {
        return Input.readDouble();
    }

    public static double takeDouble(String prompt) {
        return Input.readDouble(prompt);
    }

    public static DoubleReader takeDouble() {
        return Input.readDouble();
    }

    public static double getDouble(String prompt) {
        return Input.readDouble(prompt);
    }

    public static DoubleReader getDouble() {
        return Input.readDouble();
    }

    public static double askDouble(String prompt) {
        return Input.readDouble(prompt);
    }

    public static DoubleReader askDouble() {
        return Input.readDouble();
    }

    // ================================================================ input —
    // string

    public static String readLine(String prompt) {
        return Input.readLine(prompt);
    }

    public static StringReader readLine() {
        return Input.readLine();
    }

    public static String readString(String prompt) {
        return Input.readLine(prompt);
    }

    public static StringReader readString() {
        return Input.readLine();
    }

    public static String readText(String prompt) {
        return Input.readLine(prompt);
    }

    public static StringReader readText() {
        return Input.readLine();
    }

    public static String takeString(String prompt) {
        return Input.readLine(prompt);
    }

    public static String takeLine(String prompt) {
        return Input.readLine(prompt);
    }

    public static String getString(String prompt) {
        return Input.readLine(prompt);
    }

    public static String getLine(String prompt) {
        return Input.readLine(prompt);
    }

    public static String askLine(String prompt) {
        return Input.readLine(prompt);
    }

    public static String askString(String prompt) {
        return Input.readLine(prompt);
    }

    public static String inputLine(String prompt) {
        return Input.readLine(prompt);
    }

    public static String inputString(String prompt) {
        return Input.readLine(prompt);
    }

    public static String readNonEmpty(String prompt) {
        return Input.readNonEmpty(prompt);
    }

    public static String readRequired(String prompt) {
        return Input.readNonEmpty(prompt);
    }

    // ================================================================ input —
    // boolean / char

    public static boolean readYesNo(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static BooleanReader readBoolean() {
        return Input.readBoolean();
    }

    public static boolean readBool(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static boolean readConfirm(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static boolean confirm(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static boolean askConfirm(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static boolean askYesNo(String prompt) {
        return Input.readYesNo(prompt);
    }

    public static char readChar(String prompt) {
        return Input.readChar(prompt);
    }

    public static CharReader readCharacter() {
        return Input.readCharacter();
    }

    public static char readLetter(String prompt) {
        return Input.readChar(prompt);
    }

    public static char readSingleChar(String prompt) {
        return Input.readChar(prompt);
    }

    // ================================================================ input —
    // domain

    public static String readPassword(String prompt) {
        return Input.readPassword(prompt);
    }

    public static String readEmail(String prompt) {
        return Input.readEmail(prompt);
    }

    public static String readPhone(String prompt) {
        return Input.readPhone(prompt);
    }

    public static String readURL(String prompt) {
        return Input.readURL(prompt);
    }

    public static String readUrl(String prompt) {
        return Input.readURL(prompt);
    }

    public static String readChoice(String prompt, String... options) {
        return Input.readChoice(prompt, options);
    }

    public static String choose(String prompt, String... options) {
        return Input.readChoice(prompt, options);
    }

    public static int readChoiceIndex(String prompt, String... opt) {
        return Input.readChoiceIndex(prompt, opt);
    }

    // ================================================================ input — date
    // / time

    public static LocalDate readDate(String prompt) {
        return Input.readDate(prompt);
    }

    public static DateReader readDate() {
        return Input.readDate();
    }

    public static LocalTime readTime(String prompt) {
        return Input.readTime(prompt);
    }

    public static TimeReader readTime() {
        return Input.readTime();
    }

    public static LocalDateTime readDateTime(String prompt) {
        return Input.readDateTime(prompt);
    }

    public static DateTimeReader readDateTime() {
        return Input.readDateTime();
    }

    // ================================================================ output

    public static void print(Object value) {
        Console.print(value);
    }

    public static void println(Object value) {
        Console.println(value);
    }

    public static void println() {
        Console.println();
    }

    public static void printf(String fmt, Object... args) {
        Console.printf(fmt, args);
    }

    public static void success(String msg) {
        Console.success(msg);
    }

    public static void ok(String msg) {
        Console.ok(msg);
    }

    public static void done(String msg) {
        Console.done(msg);
    }

    public static void error(String msg) {
        Console.error(msg);
    }

    public static void fail(String msg) {
        Console.fail(msg);
    }

    public static void warn(String msg) {
        Console.warn(msg);
    }

    public static void warning(String msg) {
        Console.warning(msg);
    }

    public static void info(String msg) {
        Console.info(msg);
    }

    public static void note(String msg) {
        Console.note(msg);
    }

    public static void title(String text) {
        Console.title(text);
    }

    public static void heading(String text) {
        Console.heading(text);
    }

    public static void header(String text) {
        Console.header(text);
    }

    public static void section(String text) {
        Console.section(text);
    }

    public static void line() {
        Console.line();
    }

    public static void hr() {
        Console.hr();
    }

    public static void line(int width) {
        Console.line(width);
    }

    public static void hr(int width) {
        Console.hr(width);
    }

    public static void line(chirp.console.Separator.Style style) {
        Console.line(style);
    }

    public static void line(int width, chirp.console.Separator.Style style) {
        Console.line(width, style);
    }

    public static chirp.console.Separator.Builder separator() {
        return Console.separator();
    }

    public static void blank() {
        Console.blank();
    }

    public static void nl() {
        Console.nl();
    }

    public static void watermark() {
        Console.watermark();
    }

    public static void stamp() {
        Console.stamp();
    }

    public static void clearScreen() {
        Console.clearScreen();
    }

    public static void clear() {
        Console.clear();
    }

    public static void cls() {
        Console.cls();
    }

    public static String bold(String text) {
        return Console.bold(text);
    }

    public static String underline(String text) {
        return Console.underline(text);
    }

    public static String red(String text) {
        return Console.red(text);
    }

    public static String green(String text) {
        return Console.green(text);
    }

    public static String yellow(String text) {
        return Console.yellow(text);
    }

    public static String blue(String text) {
        return Console.blue(text);
    }

    public static String cyan(String text) {
        return Console.cyan(text);
    }

    public static String magenta(String text) {
        return Console.magenta(text);
    }

    public static String gray(String text) {
        return Console.gray(text);
    }

    public static void printArray(int[] a) {
        Console.printArray(a);
    }

    public static void printArray(double[] a) {
        Console.printArray(a);
    }

    public static void printArray(boolean[] a) {
        Console.printArray(a);
    }

    public static void printArray(String[] a) {
        Console.printArray((Object[]) a);
    }

    public static void printArray(Object[] a) {
        Console.printArray(a);
    }

    public static void printMatrix(int[][] m) {
        Console.printMatrix(m);
    }

    public static void printMatrix(String[][] m) {
        Console.printMatrix(m);
    }

    public static void table(List<String[]> rows, String... headers) {
        Console.table(rows, headers);
    }

    public static void grid(List<String[]> rows, String... headers) {
        Console.table(rows, headers);
    }

    public static void progress(int current, int total) {
        Console.progress(current, total);
    }

    public static void bar(int current, int total) {
        Console.progress(current, total);
    }

    // ================================================================ format

    public static String fmt(String template, Object... kv) {
        return Format.fmt(template, kv);
    }

    public static String format(String template, Object... kv) {
        return Format.fmt(template, kv);
    }

    public static String fmt(String template, Map<String, Object> v) {
        return Format.fmt(template, v);
    }

    public static String fmtWithSession(String template) {
        return Format.fmtWithSession(template);
    }

    public static String sessionFormat(String template) {
        return Format.fmtWithSession(template);
    }

    public static chirp.format.Formatter formatter(String template) {
        return Format.formatter(template);
    }

    public static String currency(double amount) {
        return Format.currency(amount);
    }

    public static String dollars(double amount) {
        return Format.currency(amount);
    }

    public static String usd(double amount) {
        return Format.currency(amount);
    }

    public static String money(double amount) {
        return Format.currency(amount);
    }

    public static String rupees(double amount) {
        return Format.rupees(amount);
    }

    public static String pkr(double amount) {
        return Format.rupees(amount);
    }

    public static String currencyOf(double amount, String symbol) {
        return Format.currencyOf(amount, symbol);
    }

    public static String percent(double value) {
        return Format.percent(value);
    }

    public static String pct(double value) {
        return Format.percent(value);
    }

    public static String percent(double value, int places) {
        return Format.percent(value, places);
    }

    public static String decimal(double value, int places) {
        return Format.decimal(value, places);
    }

    public static String fixed(double value, int places) {
        return Format.decimal(value, places);
    }

    public static String plural(int n, String s, String p) {
        return Format.plural(n, s, p);
    }

    public static String quantify(int n, String singular) {
        return Format.quantify(n, singular);
    }

    public static String items(int n) {
        return Format.items(n);
    }

    public static String files(int n) {
        return Format.files(n);
    }

    public static String lines(int n) {
        return Format.lines(n);
    }

    public static String chars(int n) {
        return Format.chars(n);
    }

    public static <T> String list(List<T> l, String d) {
        return Format.list(l, d);
    }

    public static <T> String list(List<T> l, String d, String last) {
        return Format.list(l, d, last);
    }

    public static <T> String join(List<T> l, String d) {
        return Format.list(l, d);
    }

    public static String ordinal(int n) {
        return Format.ordinal(n);
    }

    public static String nth(int n) {
        return Format.ordinal(n);
    }

    public static String roman(int n) {
        return Format.roman(n);
    }

    public static String bytes(long count) {
        return Format.bytes(count);
    }

    public static String fileSize(long count) {
        return Format.bytes(count);
    }

    public static String duration(long millis) {
        return Format.duration(millis);
    }

    public static String humanDuration(long millis) {
        return Format.duration(millis);
    }

    public static String duration(Duration d) {
        return Format.duration(d);
    }

    public static String cnic(String raw) {
        return Format.cnic(raw);
    }

    public static String pakistaniPhone(String raw) {
        return Format.pakistaniPhone(raw);
    }

    public static String pkPhone(String raw) {
        return Format.pakistaniPhone(raw);
    }

    // ================================================================ date / time

    public static LocalDate today() {
        return DateTime.today();
    }

    public static LocalDate nowDate() {
        return DateTime.today();
    }

    public static LocalDate yesterday() {
        return DateTime.yesterday();
    }

    public static LocalDate tomorrow() {
        return DateTime.tomorrow();
    }

    public static LocalTime nowTime() {
        return DateTime.nowTime();
    }

    public static LocalDateTime now() {
        return DateTime.now();
    }

    public static LocalDateTime nowDateTime() {
        return DateTime.now();
    }

    public static chirp.datetime.DateChain dateChain(LocalDate d) {
        return DateTime.of(d);
    }

    public static chirp.datetime.DateChain dateChain(String d) {
        return DateTime.of(d);
    }

    public static LocalDate parseDate(String s) {
        return DateTime.parseDate(s);
    }

    public static LocalDate readDateValue(String s) {
        return DateTime.parseDate(s);
    }

    public static LocalDate dateFrom(String s) {
        return DateTime.parseDate(s);
    }

    public static LocalDate parseDate(String s, String pattern) {
        return DateTime.parseDate(s, pattern);
    }

    public static LocalTime parseTime(String s) {
        return DateTime.parseTime(s);
    }

    public static LocalDateTime parseDateTime(String s) {
        return DateTime.parseDateTime(s);
    }

    public static String humanizeDate(LocalDate d) {
        return DateTime.humanize(d);
    }

    public static String humanizeShortDate(LocalDate d) {
        return DateTime.humanizeShort(d);
    }

    public static String humanizeTime(LocalTime t) {
        return DateTime.humanize(t);
    }

    public static String humanizeDateTime(LocalDateTime dt) {
        return DateTime.humanize(dt);
    }

    public static String relativeDate(LocalDate d) {
        return DateTime.timeAgo(d);
    }

    public static String relativeDateTime(LocalDateTime dt) {
        return DateTime.timeAgo(dt);
    }

    public static String timeAgo(LocalDate d) {
        return DateTime.timeAgo(d);
    }

    public static String timeAgo(LocalDateTime dt) {
        return DateTime.timeAgo(dt);
    }

    public static String formatDate(LocalDate d, String p) {
        return DateTime.format(d, p);
    }

    public static String formatTime(LocalTime t, String p) {
        return DateTime.format(t, p);
    }

    public static String formatDateTime(LocalDateTime dt, String p) {
        return DateTime.format(dt, p);
    }

    public static String formatDateISO(LocalDate d) {
        return DateTime.formatISO(d);
    }

    public static LocalDate addDays(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }

    public static LocalDate plusDays(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }

    public static LocalDate daysAfter(LocalDate d, int n) {
        return DateTime.addDays(d, n);
    }

    public static LocalDate subtractDays(LocalDate d, int n) {
        return DateTime.subtractDays(d, n);
    }

    public static LocalDate minusDays(LocalDate d, int n) {
        return DateTime.subtractDays(d, n);
    }

    public static LocalDate addMonths(LocalDate d, int n) {
        return DateTime.addMonths(d, n);
    }

    public static LocalDate subtractMonths(LocalDate d, int n) {
        return DateTime.subtractMonths(d, n);
    }

    public static LocalDate addYears(LocalDate d, int n) {
        return DateTime.addYears(d, n);
    }

    public static LocalDate subtractYears(LocalDate d, int n) {
        return DateTime.subtractYears(d, n);
    }

    public static LocalDate addBusinessDays(LocalDate d, int n) {
        return DateTime.addBusinessDays(d, n);
    }

    public static LocalDate startOfWeek(LocalDate d) {
        return DateTime.startOfWeek(d);
    }

    public static LocalDate endOfWeek(LocalDate d) {
        return DateTime.endOfWeek(d);
    }

    public static LocalDate startOfMonth(LocalDate d) {
        return DateTime.startOfMonth(d);
    }

    public static LocalDate endOfMonth(LocalDate d) {
        return DateTime.endOfMonth(d);
    }

    public static LocalDate startOfYear(LocalDate d) {
        return DateTime.startOfYear(d);
    }

    public static LocalDate endOfYear(LocalDate d) {
        return DateTime.endOfYear(d);
    }

    public static LocalDate startOfQuarter(LocalDate d) {
        return DateTime.startOfQuarter(d);
    }

    public static LocalDate endOfQuarter(LocalDate d) {
        return DateTime.endOfQuarter(d);
    }

    public static long daysBetween(LocalDate a, LocalDate b) {
        return DateTime.daysBetween(a, b);
    }

    public static long diffDays(LocalDate a, LocalDate b) {
        return DateTime.daysBetween(a, b);
    }

    public static long monthsBetween(LocalDate a, LocalDate b) {
        return DateTime.monthsBetween(a, b);
    }

    public static long yearsBetween(LocalDate a, LocalDate b) {
        return DateTime.yearsBetween(a, b);
    }

    public static int businessDaysBetween(LocalDate a, LocalDate b) {
        return DateTime.businessDaysBetween(a, b);
    }

    public static int businessDaysUntil(LocalDate a, LocalDate b) {
        return DateTime.businessDaysBetween(a, b);
    }

    public static int age(LocalDate birth) {
        return DateTime.age(birth);
    }

    public static int calculateAge(LocalDate birth) {
        return DateTime.age(birth);
    }

    public static String dayName(LocalDate d) {
        return DateTime.dayName(d);
    }

    public static String monthName(LocalDate d) {
        return DateTime.monthName(d);
    }

    public static int dayOfWeek(LocalDate d) {
        return DateTime.dayOfWeek(d);
    }

    public static int dayOfYear(LocalDate d) {
        return DateTime.dayOfYear(d);
    }

    public static int quarter(LocalDate d) {
        return DateTime.quarter(d);
    }

    public static int daysInMonth(LocalDate d) {
        return DateTime.daysInMonth(d);
    }

    public static boolean isWeekend(LocalDate d) {
        return DateTime.isWeekend(d);
    }

    public static boolean isWeekday(LocalDate d) {
        return DateTime.isWeekday(d);
    }

    public static boolean isToday(LocalDate d) {
        return DateTime.isToday(d);
    }

    public static boolean isPast(LocalDate d) {
        return DateTime.isPast(d);
    }

    public static boolean isFuture(LocalDate d) {
        return DateTime.isFuture(d);
    }

    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return DateTime.isSameMonth(a, b);
    }

    public static boolean isSameYear(LocalDate a, LocalDate b) {
        return DateTime.isSameYear(a, b);
    }

    public static boolean isLeapYear(int year) {
        return DateTime.isLeapYear(year);
    }

    public static LocalDate minDate(LocalDate a, LocalDate b) {
        return DateTime.min(a, b);
    }

    public static LocalDate maxDate(LocalDate a, LocalDate b) {
        return DateTime.max(a, b);
    }

    public static long toEpochMillis(LocalDateTime dt) {
        return DateTime.toEpochMillis(dt);
    }

    public static LocalDateTime fromEpochMillis(long ms) {
        return DateTime.fromEpochMillis(ms);
    }

    // ================================================================ numbers

    public static chirp.number.NumberChain numberChain(int n) {
        return Numbers.of(n);
    }

    public static chirp.number.NumberChain numberChain(double n) {
        return Numbers.of(n);
    }

    public static chirp.number.NumberChain numberOf(int n) {
        return Numbers.of(n);
    }

    public static chirp.number.NumberChain numberOf(double n) {
        return Numbers.of(n);
    }

    public static int random(int min, int max) {
        return Numbers.random(min, max);
    }

    public static int rand(int min, int max) {
        return Numbers.random(min, max);
    }

    public static int randomInt(int min, int max) {
        return Numbers.random(min, max);
    }

    public static int randomBetween(int min, int max) {
        return Numbers.random(min, max);
    }

    public static double randomDouble(double min, double max) {
        return Numbers.randomDouble(min, max);
    }

    public static boolean randomBoolean() {
        return Numbers.randomBoolean();
    }

    public static int clamp(int v, int min, int max) {
        return Numbers.clamp(v, min, max);
    }

    public static double clamp(double v, double min, double max) {
        return Numbers.clamp(v, min, max);
    }

    public static int wrap(int v, int min, int max) {
        return Numbers.wrap(v, min, max);
    }

    public static int abs(int n) {
        return Numbers.abs(n);
    }

    public static double abs(double n) {
        return Numbers.abs(n);
    }

    public static int sign(int n) {
        return Numbers.sign(n);
    }

    public static int mod(int a, int b) {
        return Numbers.mod(a, b);
    }

    public static double pow(double b, double e) {
        return Numbers.pow(b, e);
    }

    public static double sqrt(double n) {
        return Numbers.sqrt(n);
    }

    public static double cbrt(double n) {
        return Numbers.cbrt(n);
    }

    public static double hypot(double a, double b) {
        return Numbers.hypot(a, b);
    }

    public static double log(double n) {
        return Numbers.log(n);
    }

    public static double log10(double n) {
        return Numbers.log10(n);
    }

    public static double log2(double n) {
        return Numbers.log2(n);
    }

    public static double exp(double n) {
        return Numbers.exp(n);
    }

    public static int floor(double n) {
        return Numbers.floor(n);
    }

    public static int ceil(double n) {
        return Numbers.ceil(n);
    }

    public static int roundToInt(double n) {
        return Numbers.roundToInt(n);
    }

    public static long roundToLong(double n) {
        return Numbers.roundToLong(n);
    }

    public static double sin(double r) {
        return Numbers.sin(r);
    }

    public static double cos(double r) {
        return Numbers.cos(r);
    }

    public static double tan(double r) {
        return Numbers.tan(r);
    }

    public static double toRadians(double d) {
        return Numbers.toRadians(d);
    }

    public static double toDegrees(double r) {
        return Numbers.toDegrees(r);
    }

    public static boolean isEven(int n) {
        return Numbers.isEven(n);
    }

    public static boolean isOdd(int n) {
        return Numbers.isOdd(n);
    }

    public static boolean isPrime(int n) {
        return Numbers.isPrime(n);
    }

    public static List<Integer> primesUpTo(int n) {
        return Numbers.primesUpTo(n);
    }

    public static int nextPrime(int n) {
        return Numbers.nextPrime(n);
    }

    public static long factorial(int n) {
        return Numbers.factorial(n);
    }

    public static long fact(int n) {
        return Numbers.factorial(n);
    }

    public static int fibonacci(int n) {
        return Numbers.fibonacci(n);
    }

    public static int fib(int n) {
        return Numbers.fibonacci(n);
    }

    public static int gcd(int a, int b) {
        return Numbers.gcd(a, b);
    }

    public static int lcm(int a, int b) {
        return Numbers.lcm(a, b);
    }

    public static int sumDigits(int n) {
        return Numbers.sumDigits(n);
    }

    public static int productDigits(int n) {
        return Numbers.productDigits(n);
    }

    public static int countDigits(int n) {
        return Numbers.countDigits(n);
    }

    public static int reverseDigits(int n) {
        return Numbers.reverseDigits(n);
    }

    public static int firstDigit(int n) {
        return Numbers.firstDigit(n);
    }

    public static int lastDigit(int n) {
        return Numbers.lastDigit(n);
    }

    public static String toBinary(int n) {
        return Numbers.toBinary(n);
    }

    public static String toHex(int n) {
        return Numbers.toHex(n);
    }

    public static String toOctal(int n) {
        return Numbers.toOctal(n);
    }

    public static Integer fromBinary(String s) {
        return Numbers.fromBinary(s);
    }

    public static Integer fromHex(String s) {
        return Numbers.fromHex(s);
    }

    public static boolean isArmstrong(int n) {
        return Numbers.isArmstrong(n);
    }

    public static boolean isPalindromeNumber(int n) {
        return Numbers.isPalindromeNumber(n);
    }

    public static boolean isPerfect(int n) {
        return Numbers.isPerfect(n);
    }

    public static boolean isPerfectSquare(int n) {
        return Numbers.isPerfectSquare(n);
    }

    public static boolean isPerfectCube(int n) {
        return Numbers.isPerfectCube(n);
    }

    public static List<Integer> divisors(int n) {
        return Numbers.divisors(n);
    }

    public static int countDivisors(int n) {
        return Numbers.countDivisors(n);
    }

    public static int sumDivisors(int n) {
        return Numbers.sumDivisors(n);
    }

    public static double round(double v, int d) {
        return Numbers.round(v, d);
    }

    public static String numFmt(double v, int decimals) {
        return Numbers.format(v, decimals);
    }

    public static Integer parseInt(String s) {
        return Numbers.parseInt(s);
    }

    public static Double parseDouble(String s) {
        return Numbers.parseDouble(s);
    }

    public static double percentOf(double part, double whole) {
        return Numbers.percent(part, whole);
    }

    public static List<Integer> range(int start, int end) {
        return Numbers.range(start, end);
    }

    public static List<Integer> range(int start, int end, int step) {
        return Numbers.range(start, end, step);
    }

    public static List<Integer> rangeInclusive(int start, int end) {
        return Numbers.rangeInclusive(start, end);
    }

    public static Numbers.Statistics statistics(int[] values) {
        return Numbers.statistics(values);
    }

    public static double median(int[] a) {
        return Numbers.median(a);
    }

    public static int mode(int[] a) {
        return Numbers.mode(a);
    }

    public static double stdDev(int[] a) {
        return Numbers.stdDev(a);
    }

    public static double variance(int[] a) {
        return Numbers.variance(a);
    }

    public static String abbreviate(long n) {
        return Numbers.abbreviate(n);
    }

    public static String thousands(long n) {
        return Numbers.thousands(n);
    }

    public static String romanNumeral(int n) {
        return Numbers.roman(n);
    }

    // ================================================================ arrays

    public static ArrayChain arrayChain(int[] a) {
        return ArraysUtil.of(a);
    }

    public static ArrayChain arrayOf(int... a) {
        return ArrayChain.of(a);
    }

    public static int sum(int[] a) {
        return ArraysUtil.sum(a);
    }

    public static int total(int[] a) {
        return ArraysUtil.sum(a);
    }

    public static double sum(double[] a) {
        return ArraysUtil.sum(a);
    }

    public static double average(int[] a) {
        return ArraysUtil.average(a);
    }

    public static double mean(int[] a) {
        return ArraysUtil.average(a);
    }

    public static int max(int[] a) {
        return ArraysUtil.max(a);
    }

    public static int largest(int[] a) {
        return ArraysUtil.max(a);
    }

    public static int min(int[] a) {
        return ArraysUtil.min(a);
    }

    public static int smallest(int[] a) {
        return ArraysUtil.min(a);
    }

    public static int count(int[] a, int v) {
        return ArraysUtil.count(a, v);
    }

    public static boolean contains(int[] a, int v) {
        return ArraysUtil.contains(a, v);
    }

    public static int indexOf(int[] a, int v) {
        return ArraysUtil.indexOf(a, v);
    }

    public static int lastIndexOf(int[] a, int v) {
        return ArraysUtil.lastIndexOf(a, v);
    }

    public static boolean hasDuplicates(int[] a) {
        return ArraysUtil.hasDuplicates(a);
    }

    public static boolean isSorted(int[] a) {
        return ArraysUtil.isSorted(a);
    }

    public static int[] reverse(int[] a) {
        return ArraysUtil.reverse(a);
    }

    public static int[] sorted(int[] a) {
        return ArraysUtil.sorted(a);
    }

    public static int[] sortedDescending(int[] a) {
        return ArraysUtil.sortedDescending(a);
    }

    public static int[] distinct(int[] a) {
        return ArraysUtil.distinct(a);
    }

    public static int[] rotateRight(int[] a, int steps) {
        return ArraysUtil.rotateRight(a, steps);
    }

    public static int[] rotateLeft(int[] a, int steps) {
        return ArraysUtil.rotateLeft(a, steps);
    }

    public static int[] shuffle(int[] a) {
        return ArraysUtil.shuffle(a);
    }

    public static int[] subArray(int[] a, int from, int to) {
        return ArraysUtil.subArray(a, from, to);
    }

    public static int[] take(int[] a, int n) {
        return ArraysUtil.take(a, n);
    }

    public static int[] drop(int[] a, int n) {
        return ArraysUtil.drop(a, n);
    }

    public static int[] concat(int[] a, int[] b) {
        return ArraysUtil.concat(a, b);
    }

    public static int[] merge(int[] a, int[] b) {
        return ArraysUtil.concat(a, b);
    }

    public static int[] filter(int[] a, java.util.function.IntPredicate p) {
        return ArraysUtil.filter(a, p);
    }

    public static int[] map(int[] a, java.util.function.IntUnaryOperator f) {
        return ArraysUtil.map(a, f);
    }

    public static int[] intersection(int[] a, int[] b) {
        return ArraysUtil.intersection(a, b);
    }

    public static int[] union(int[] a, int[] b) {
        return ArraysUtil.union(a, b);
    }

    public static int[] difference(int[] a, int[] b) {
        return ArraysUtil.difference(a, b);
    }

    public static int[][] transpose(int[][] m) {
        return ArraysUtil.transpose(m);
    }

    public static int[] row(int[][] m, int r) {
        return ArraysUtil.row(m, r);
    }

    public static int[] col(int[][] m, int c) {
        return ArraysUtil.col(m, c);
    }

    public static int[] diagonal(int[][] m) {
        return ArraysUtil.diagonal(m);
    }

    public static int[] antiDiagonal(int[][] m) {
        return ArraysUtil.antiDiagonal(m);
    }

    public static int[] flatten(int[][] m) {
        return ArraysUtil.flatten(m);
    }

    public static int[][] matrix(int rows, int cols, int value) {
        return ArraysUtil.matrix(rows, cols, value);
    }

    public static int[] intArray(int size, int value) {
        return ArraysUtil.intArray(size, value);
    }

    public static int[] intArray(int... values) {
        return ArraysUtil.intArray(values);
    }

    public static int[] zeros(int n) {
        return ArraysUtil.zeros(n);
    }

    public static int[] ones(int n) {
        return ArraysUtil.ones(n);
    }

    public static int[] rangeArray(int start, int end) {
        return ArraysUtil.rangeArray(start, end);
    }

    // ================================================================ search /
    // filter / page

    public static boolean contains(String text, String q) {
        return Search.contains(text, q);
    }

    public static boolean containsIgnoreCase(String t, String q) {
        return Search.containsIgnoreCase(t, q);
    }

    public static boolean containsStrict(String text, String q) {
        return Search.containsStrict(text, q);
    }

    public static boolean startsWith(String text, String q) {
        return Search.startsWith(text, q);
    }

    public static boolean endsWith(String text, String q) {
        return Search.endsWith(text, q);
    }

    public static boolean wholeWord(String text, String word) {
        return Search.wholeWord(text, word);
    }

    public static boolean fuzzyMatch(String text, String q, int maxD) {
        return Search.fuzzy(text, q, maxD);
    }

    public static int levenshtein(String a, String b) {
        return Search.levenshtein(a, b);
    }

    public static <T> List<T> filter(List<T> items, Predicate<T> p) {
        return Filter.where(items, p);
    }

    public static <T> List<T> where(List<T> items, Predicate<T> p) {
        return Filter.where(items, p);
    }

    public static <T> List<T> reject(List<T> items, Predicate<T> p) {
        return Filter.reject(items, p);
    }

    public static <T> List<T> unique(List<T> items) {
        return Filter.distinct(items);
    }

    public static <T> List<T> distinct(List<T> items) {
        return Filter.distinct(items);
    }

    public static <T> Page<T> page(List<T> items, int page, int size) {
        return Pagination.paginate(items, page, size);
    }

    public static <T> Page<T> firstPage(List<T> items, int size) {
        return Pagination.firstPage(items, size);
    }

    public static <T> Page<T> lastPage(List<T> items, int size) {
        return Pagination.lastPage(items, size);
    }

    public static <T> List<Page<T>> allPages(List<T> items, int size) {
        return Pagination.allPages(items, size);
    }

    // ================================================================ text

    public static TextChain chain(String s) {
        return Text.chain(s);
    }

    public static TextChain textChain(String s) {
        return Text.chain(s);
    }

    public static boolean isBlank(String s) {
        return Text.isBlank(s);
    }

    public static boolean notBlank(String s) {
        return Text.isNotBlank(s);
    }

    public static boolean isEmpty(String s) {
        return Text.isEmpty(s);
    }

    public static boolean hasText(String s) {
        return Text.hasText(s);
    }

    public static String capitalize(String s) {
        return Text.capitalize(s);
    }

    public static String capitalise(String s) {
        return Text.capitalise(s);
    }

    public static String titleCase(String s) {
        return Text.titleCase(s);
    }

    public static String toCamelCase(String s) {
        return Text.toCamelCase(s);
    }

    public static String toPascalCase(String s) {
        return Text.toPascalCase(s);
    }

    public static String toSnakeCase(String s) {
        return Text.toSnakeCase(s);
    }

    public static String toKebabCase(String s) {
        return Text.toKebabCase(s);
    }

    public static String swapCase(String s) {
        return Text.swapCase(s);
    }

    public static String reverseText(String s) {
        return Text.reverse(s);
    }

    public static String reverseWords(String s) {
        return Text.reverseWords(s);
    }

    public static String truncate(String s, int max) {
        return Text.truncate(s, max);
    }

    public static String padLeft(String s, int w) {
        return Text.padLeft(s, w);
    }

    public static String padRight(String s, int w) {
        return Text.padRight(s, w);
    }

    public static String center(String s, int w) {
        return Text.center(s, w);
    }

    public static String repeat(String s, int n) {
        return Text.repeat(s, n);
    }

    public static boolean isPalindrome(String s) {
        return Text.isPalindrome(s);
    }

    public static boolean isAnagram(String a, String b) {
        return Text.isAnagram(a, b);
    }

    public static String mask(String s, int visible) {
        return Text.mask(s, visible);
    }

    public static String slugify(String s) {
        return Text.slugify(s);
    }

    public static String slug(String s) {
        return Text.slugify(s);
    }

    public static double similarity(String a, String b) {
        return Text.similarity(a, b);
    }

    public static int countWords(String s) {
        return Text.countWords(s);
    }

    public static String initials(String s) {
        return Text.initials(s);
    }

    public static String join(String d, Object... parts) {
        return Text.join(d, parts);
    }

    public static String removeSpaces(String s) {
        return Text.removeSpaces(s);
    }

    public static String removePunctuation(String s) {
        return Text.removePunctuation(s);
    }

    public static String extractDigits(String s) {
        return Text.extractDigits(s);
    }

    public static String extractLetters(String s) {
        return Text.extractLetters(s);
    }

    public static String substringBefore(String s, String m) {
        return Text.substringBefore(s, m);
    }

    public static String substringAfter(String s, String m) {
        return Text.substringAfter(s, m);
    }

    public static <T> List<T> filterByCheck(List<T> items, Predicate<T> p) {
        if (items == null || p == null)
            return List.of();
        return items.stream().filter(p).toList();
    }

    // ================================================================ checks

    public static boolean isEmail(String s) {
        return Check.isEmail(s);
    }

    public static boolean isPhone(String s) {
        return Check.isPhone(s);
    }

    public static boolean isURL(String s) {
        return Check.isURL(s);
    }

    public static boolean isUrl(String s) {
        return Check.isURL(s);
    }

    public static boolean isNumeric(String s) {
        return Check.isNumeric(s);
    }

    public static boolean isInteger(String s) {
        return Check.isInteger(s);
    }

    public static boolean isAlpha(String s) {
        return Check.isAlpha(s);
    }

    public static boolean isDigits(String s) {
        return Check.isDigits(s);
    }

    public static boolean isAlphanumeric(String s) {
        return Check.isAlphanumeric(s);
    }

    public static boolean isHexColor(String s) {
        return Check.isHexColor(s);
    }

    public static boolean isIPAddress(String s) {
        return Check.isIPAddress(s);
    }

    public static boolean isUUID(String s) {
        return Check.isUUID(s);
    }

    public static boolean isUsername(String s) {
        return Check.isUsername(s);
    }

    public static boolean isPakistaniPhone(String s) {
        return Check.isPakistaniPhone(s);
    }

    public static boolean isPakistaniCnic(String s) {
        return Check.isPakistaniCnic(s);
    }

    public static boolean isStrongPassword(String s) {
        return Check.isStrongPassword(s);
    }

    public static boolean isVeryStrongPassword(String s) {
        return Check.isVeryStrongPassword(s);
    }

    public static boolean isAdult(int age) {
        return Check.isAdult(age);
    }

    public static boolean isValidGrade(int g) {
        return Check.isValidGrade(g);
    }

    public static boolean isLeap(int y) {
        return Check.isLeapYear(y);
    }

    public static boolean isJSON(String s) {
        return Check.isJSON(s);
    }

    public static boolean isNumericPalindrome(int n) {
        return Numbers.isPalindromeNumber(n);
    }

    public static boolean is(String value, String ruleName) {
        return Check.is(value, ruleName);
    }

    public static boolean is(String value, Rule rule) {
        return Check.is(value, rule);
    }

    // ================================================================ decorators

    public static Duration timed(Runnable action) {
        return Decorator.timed(action);
    }

    public static Duration measure(Runnable action) {
        return Decorator.timed(action);
    }

    public static <T> Timed<T> timed(Supplier<T> supplier) {
        return Decorator.timed(supplier);
    }

    public static <T> Optional<T> safe(Supplier<T> supplier) {
        return Decorator.safe(supplier);
    }

    public static boolean safe(Runnable action) {
        return Decorator.safe(action);
    }

    public static <T> T cached(String key, Supplier<T> supplier) {
        return Decorator.cached(key, supplier);
    }

    public static void invalidate(String key) {
        Decorator.invalidate(key);
    }

    public static void clearCache() {
        Decorator.clearCache();
    }

    public static void logged(String label, Runnable action) {
        Decorator.logged(label, action);
    }

    public static <T> T logged(String label, Supplier<T> supplier) {
        return Decorator.logged(label, supplier);
    }

    public static <T> T withFallback(Supplier<T> s, T fallback) {
        return Decorator.withFallback(s, fallback);
    }

    public static <T> T fallback(Supplier<T> s, T fallback) {
        return Decorator.withFallback(s, fallback);
    }

    public static Retry retry(int attempts) {
        return Decorator.retry(attempts);
    }

    public static void retry(Runnable action) {
        Decorator.retry(action);
    }

    public static <T> T retry(Supplier<T> supplier) {
        return Decorator.retry(supplier);
    }

    // ================================================================ session /
    // rules

    public static Session session() {
        return Session.me();
    }

    public static Rule getRule(String name) {
        return Rules.get(name);
    }

    public static Rule requireRule(String name) {
        return Rules.require(name);
    }

    public static Rule registerRule(Rule rule) {
        return Rules.register(rule);
    }

    public static boolean hasRule(String name) {
        return Rules.has(name);
    }

    public static Rule pkMobile() {
        return Rules.pakistaniMobile();
    }

    public static Rule pkCnic() {
        return Rules.pakistaniCnic();
    }

    public static ValidationResult validate(String value, String ruleName) {
        Rule rule = Rules.get(ruleName);
        if (rule == null)
            return new ValidationResult(List.of("No rule named '" + ruleName + "'"));
        return rule.test(value)
                ? new ValidationResult(List.of())
                : new ValidationResult(List.of(rule.message()));
    }

    // ================================================================ http

    public static HttpRequest get(String url) {
        return Http.get(url);
    }

    public static HttpRequest getRequest(String url) {
        return Http.getRequest(url);
    }

    public static HttpRequest post(String url) {
        return Http.post(url);
    }

    public static HttpRequest put(String url) {
        return Http.put(url);
    }

    public static HttpRequest delete(String url) {
        return Http.delete(url);
    }

    public static HttpRequest del(String url) {
        return Http.delete(url);
    }

    public static HttpRequest patch(String url) {
        return Http.patch(url);
    }

    public static HttpRequest request(HttpMethod method, String url) {
        return Http.request(method, url);
    }

    public static HttpResponse fetch(String url) {
        return Http.fetch(url);
    }

    public static HttpResponse fetchJson(String url) {
        return Http.fetchJson(url);
    }

    public static HttpResponse getJson(String url) {
        return Http.getJson(url);
    }

    public static HttpResponse postJson(String url, String body) {
        return Http.postJson(url, body);
    }

    public static HttpResponse download(String url, String savePath) {
        return Http.download(url, savePath);
    }

    public static boolean isOnline() {
        return Http.isOnline();
    }

    public static boolean hasInternet() {
        return Http.isOnline();
    }

    public static boolean isConnected() {
        return Http.isOnline();
    }

    public static boolean canReach(String host, int port) {
        return Http.canReach(host, port);
    }

    public static boolean ping(String url) {
        return Http.ping(url);
    }

    // ================================================================ email

    public static EmailRequest email() {
        return Email.builder();
    }

    public static EmailRequest mail() {
        return Email.builder();
    }

    public static EmailRequest composeEmail() {
        return Email.builder();
    }

    public static EmailResult sendEmail(Object... kv) {
        return Email.send(kv);
    }

    public static EmailResult sendMail(Object... kv) {
        return Email.send(kv);
    }

    public static EmailResult quickSend(String host, String from, String to,
            String subj, String body, String pw) {
        return Email.quickSend(host, from, to, subj, body, pw);
    }

    public static EmailResult sendWithEnv(String host, String from, String to,
            String subj, String body,
            String envFile, String envKey) {
        return Email.sendWithEnv(host, from, to, subj, body, envFile, envKey);
    }

    public static boolean canConnect(String host, int port) {
        return Email.canConnect(host, port);
    }

    // ================================================================ file
    // management

    public static boolean mkdir(String path) {
        return FileIO.createDirectory(path);
    }

    public static boolean mkdirs(String path) {
        return FileIO.createDirectory(path);
    }

    public static boolean createDirectory(String path) {
        return FileIO.createDirectory(path);
    }

    public static boolean deleteDir(String path) {
        return FileIO.deleteDirectory(path);
    }

    public static boolean removeDir(String path) {
        return FileIO.deleteDirectory(path);
    }

    public static int countFiles(String directory) {
        return FileIO.countFiles(directory);
    }

    public static int countAllFiles(String directory) {
        return FileIO.countAllFiles(directory);
    }

    public static long dirSize(String directory) {
        return FileIO.directorySize(directory);
    }

    public static List<String> findFiles(String directory, String g) {
        return FileIO.findFiles(directory, g);
    }

    public static List<String> findFilesByExtension(String d, String e) {
        return FileIO.findFilesByExtension(d, e);
    }

    public static boolean isEmptyDir(String directory) {
        return FileIO.isEmpty(directory);
    }

    public static List<String> listDirectories(String d) {
        return FileIO.listDirectories(d);
    }

    public static String readFile(String path) {
        return TextFileIO.readAll(path);
    }

    public static List<String> readFileLines(String path) {
        return TextFileIO.readLines(path);
    }

    public static boolean writeFile(String path, String content) {
        return TextFileIO.write(path, content);
    }

    public static boolean appendFile(String path, String content) {
        return TextFileIO.append(path, content);
    }

    public static chirp.io.TextFile textFile(String path) {
        return TextFileIO.of(path);
    }

    public static CsvFile.CsvTable csvTable(String path) {
        return CsvFile.table(path);
    }

    public static chirp.json.JsonObject readJson(String path) {
        return JsonFile.readObject(path);
    }

    // ================================================================ console
    // extras

    public static Loader loader(String label) {
        return Loader.start(label);
    }

    public static Menu menu(String title) {
        return new Menu(title);
    }

    // ================================================================ misc

    public static void exit(int code) {
        System.exit(code);
    }

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
