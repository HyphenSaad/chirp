package chirp.internal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Structured tracer — the library's internal diagnostic output.
 *
 * <p><b>Reachable from application code.</b> Despite the "internal"
 * package, {@code Trace} is what turns on when a student enables trace
 * mode via {@link chirp.config.Config#trace(boolean) Config.trace(true)},
 * {@code -Dchirp.trace=true}, or {@code Chirp.start("trace")}. The
 * documentation below is what a student needs to read the trace output
 * — it does not describe how to call {@code Trace} directly (that is
 * the library's job), only how to interpret what appears.</p>
 *
 * <h2>When tracing helps</h2>
 * <p>A student asks: "why did my {@code readInt().between(1, 130)}
 * accept the input 200?" or "why did my email validator reject a valid
 * address?" Turning on trace mode makes the library's decision points
 * visible:</p>
 * <pre>{@code
 * · [TRACE] ·  #0001 14:32:11.482 [reader  ] enter IntReader.read(label=Age, min=1, max=130)
 * ◆ [DEBUG] ·  #0002 14:32:12.103 [reader  ] value: raw input = "200"
 * ◆ [DEBUG] ·  #0003 14:32:12.104 [reader  ] value: parsed int = 200
 * ◆ [DEBUG] ·  #0004 14:32:12.104 [reader  ] validation: RANGE max(130) => FAIL (value 200 above max)
 * }</pre>
 * <p>Each line has the same shape:</p>
 * <pre>
 *   GLYPH [LEVEL] ·  #COUNTER TIME [CATEGORY] MESSAGE
 * </pre>
 *
 * <h2>Four levels</h2>
 * <table border="1">
 *   <caption>Trace levels and their typical content</caption>
 *   <tr><th>Level</th><th>Glyph</th><th>Typical content</th></tr>
 *   <tr><td>{@code TRACE}</td><td>{@code ·} dim grey</td>
 *       <td>Method entry and exit — "enter IntReader.read(...)",
 *           "exit IntReader.read => 42"</td></tr>
 *   <tr><td>{@code DEBUG}</td><td>{@code ◆} cyan</td>
 *       <td>Decisions and values — "value: parsed int = 42",
 *           "decision: RANGE min(1) => TRUE"</td></tr>
 *   <tr><td>{@code INFO}</td><td>{@code ℹ} green</td>
 *       <td>Milestones — "Chirp started successfully"</td></tr>
 *   <tr><td>{@code WARN}</td><td>{@code ⚠} yellow</td>
 *       <td>Non-fatal surprises — "load failed", "input stream ended"</td></tr>
 * </table>
 *
 * <h2>Categories — filter the noise</h2>
 * <p>Every trace line is tagged with a category. A student debugging
 * input can restrict trace to just the categories they care about:</p>
 * <pre>{@code
 * Config.trace(true);
 * Config.traceOnly("reader", "rule");
 * // now only reader- and rule-category lines appear
 * }</pre>
 * <p>The full category list is in {@link #KNOWN_CATEGORIES}: input,
 * reader, rule, search, text, numbers, format, datetime, file, csv,
 * json, session, config, stats, console, menu, startup, loader,
 * http.</p>
 *
 * <h2>Two channels</h2>
 * <p>Every line goes to <b>stderr</b>, not stdout. This is deliberate:
 * a student's {@code System.out.println} output and Chirp's trace
 * output stay on separate streams, so the trace can be redirected
 * ({@code java Main 2> trace.log}) without losing the program's own
 * output.</p>
 *
 * <h2>Colour</h2>
 * <p>Uses the same {@link Ansi} sequences as the rest of the library.
 * On a non-colour terminal the glyphs and bracket tags still
 * distinguish levels, so the trace remains readable without colour.</p>
 *
 * @see chirp.config.Config#trace(boolean)
 * @see chirp.config.Config#traceLevel(Level)
 * @see chirp.config.Config#traceOnly(String...)
 * @see Ansi
 * @since 1.0.0
 */
public final class Trace {

    /**
     * The four trace levels — {@code TRACE} (verbose), {@code DEBUG}
     * (decisions), {@code INFO} (milestones), {@code WARN} (surprises).
     * Compared by ordinal — setting the minimum level to {@code DEBUG}
     * hides {@code TRACE} lines but keeps {@code DEBUG}, {@code INFO},
     * and {@code WARN}.
     */
    public enum Level { TRACE, DEBUG, INFO, WARN }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final Set<String> ENABLED_CATEGORIES = ConcurrentHashMap.newKeySet();

    /** Private lock — stderr serialization without exposing {@code System.err}. */
    private static final Object WRITE_LOCK = new Object();

    /**
     * The full list of trace categories the library uses. Useful for
     * {@link chirp.config.Config#traceOnly(String...)} — a student can
     * pick from this list to filter the trace output.
     */
    public static final List<String> KNOWN_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "input", "reader", "strict", "rule", "search", "filter", "page",
            "text", "numbers", "format", "datetime", "file", "csv", "json",
            "session", "config", "stats", "console", "menu", "startup", "loader", "http"));

    private static volatile boolean enabled = Boolean.parseBoolean(System.getProperty("chirp.trace", "false"));
    private static volatile Level minimumLevel = Level.TRACE;
    private static volatile boolean allCategoriesEnabled = true;

    private Trace() {}

    // ---------------------------------------------------------------- toggles
    /** @return whether tracing is currently enabled */
    public static boolean isEnabled() { return enabled; }
    /** Enables tracing. Called by {@link chirp.config.Config#enableTrace()}. */
    public static void enable() {
        enabled = true;
    }
    /** Disables tracing. Called by {@link chirp.config.Config#disableTrace()}. */
    public static void disable() {
        enabled = false;
    }
    /** @return the minimum enabled level */
    public static Level getLevel() { return minimumLevel; }
    /**
     * Sets the minimum enabled level. A {@code null} argument resets to
     * {@code TRACE}.
     *
     * @param level the minimum level
     * @see chirp.config.Config#traceLevel(Level)
     */
    public static void setLevel(Level level) { minimumLevel = level == null ? Level.TRACE : level; }

    /**
     * Resets all trace state to the compile-time defaults: disabled,
     * all levels, all categories.
     */
    public static void reset() {
        enabled = false;
        minimumLevel = Level.TRACE;
        allCategoriesEnabled = true;
        ENABLED_CATEGORIES.clear();
        COUNTER.set(0);
    }

    /** @param category the category to enable (null → no-op) */
    public static void enableCategory(String category) {
        if (category != null) ENABLED_CATEGORIES.add(category.toLowerCase(Locale.ROOT));
    }
    /** @param category the category to disable (null → no-op) */
    public static void disableCategory(String category) {
        if (category != null) ENABLED_CATEGORIES.remove(category.toLowerCase(Locale.ROOT));
    }

    /**
     * Restricts tracing to the given categories. Any category not
     * listed is suppressed.
     *
     * @param categories the categories to keep (null → keeps nothing)
     * @see chirp.config.Config#traceOnly(String...)
     */
    public static void enableOnly(String... categories) {
        allCategoriesEnabled = false;
        ENABLED_CATEGORIES.clear();
        if (categories != null)
            for (String c : categories) if (c != null) ENABLED_CATEGORIES.add(c.toLowerCase(Locale.ROOT));
    }

    /** Restores all categories (the default). */
    public static void enableAllCategories() {
        allCategoriesEnabled = true;
        ENABLED_CATEGORIES.clear();
    }

    /** @return the set of currently-active categories */
    public static Set<String> activeCategories() {
        if (allCategoriesEnabled) return Collections.unmodifiableSet(new LinkedHashSet<>(KNOWN_CATEGORIES));
        return Collections.unmodifiableSet(new LinkedHashSet<>(ENABLED_CATEGORIES));
    }

    // ---------------------------------------------------------------- log API
    /**
     * Logs at TRACE level.
     *
     * @param category the category
     * @param message the message
     */
    public static void trace(String category, String message) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, message);
    }
    /**
     * Logs at TRACE level with {@code {}} placeholder substitution.
     *
     * @param category the category
     * @param format the format string
     * @param args the format arguments
     */
    public static void trace(String category, String format, Object... args) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, safeFormat(format, args));
    }

    /**
     * Logs at DEBUG level.
     *
     * @param category the category
     * @param message the message
     */
    public static void debug(String category, String message) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, message);
    }
    /**
     * Logs at DEBUG level with {@code {}} placeholder substitution.
     *
     * @param category the category
     * @param format the format string
     * @param args the format arguments
     */
    public static void debug(String category, String format, Object... args) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, safeFormat(format, args));
    }

    /**
     * Logs at INFO level.
     *
     * @param category the category
     * @param message the message
     */
    public static void info(String category, String message) {
        if (!shouldLog(category, Level.INFO)) return;
        write(Level.INFO, category, message);
    }
    /**
     * Logs at INFO level with {@code {}} placeholder substitution.
     *
     * @param category the category
     * @param format the format string
     * @param args the format arguments
     */
    public static void info(String category, String format, Object... args) {
        if (!shouldLog(category, Level.INFO)) return;
        write(Level.INFO, category, safeFormat(format, args));
    }

    /**
     * Logs at WARN level.
     *
     * @param category the category
     * @param message the message
     */
    public static void warn(String category, String message) {
        if (!shouldLog(category, Level.WARN)) return;
        write(Level.WARN, category, message);
    }
    /**
     * Logs at WARN level with {@code {}} placeholder substitution.
     *
     * @param category the category
     * @param format the format string
     * @param args the format arguments
     */
    public static void warn(String category, String format, Object... args) {
        if (!shouldLog(category, Level.WARN)) return;
        write(Level.WARN, category, safeFormat(format, args));
    }

    // ---------------------------------------------------------------- structured
    /**
     * Logs a method-entry event — the "enter method(args)" trace line.
     *
     * @param category the category
     * @param method the method name
     * @param args the arguments (formatted into the message)
     */
    public static void enter(String category, String method, Object... args) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "enter " + safe(method) + "(" + joinArgsPlain(args) + ")");
    }

    /**
     * Logs a method-exit event with a return value.
     *
     * @param category the category
     * @param method the method name
     * @param result the returned value
     */
    public static void exit(String category, String method, Object result) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "exit  " + safe(method) + " => " + safeArg(result));
    }

    /**
     * Logs a method-exit event without a return value.
     *
     * @param category the category
     * @param method the method name
     */
    public static void exit(String category, String method) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "exit  " + safe(method));
    }

    /**
     * Logs a boolean decision — the "decision: X => TRUE" trace line.
     *
     * @param category the category
     * @param question the question being decided
     * @param result the outcome
     */
    public static void decision(String category, String question, boolean result) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, "decision: " + safe(question) + " => " + (result ? "TRUE" : "FALSE"));
    }

    /**
     * Logs a named value.
     *
     * @param category the category
     * @param name the name
     * @param value the value
     */
    public static void value(String category, String name, Object value) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, "value: " + safe(name) + " = " + safeArg(value));
    }

    /**
     * Logs a validation outcome — the "validation: field => PASS/FAIL"
     * trace line, optionally with a failure reason.
     *
     * @param category the category
     * @param field the field being validated
     * @param passed whether the validation passed
     * @param reason the failure reason (null when passed)
     */
    public static void validation(String category, String field, boolean passed, String reason) {
        if (!shouldLog(category, Level.DEBUG)) return;
        String outcome = passed ? "PASS" : "FAIL";
        String tail = (reason == null || reason.isEmpty()) ? "" : " (" + reason + ")";
        write(Level.DEBUG, category, "validation: " + safe(field) + " => " + outcome + tail);
    }

    // ---------------------------------------------------------------- internals
    private static boolean shouldLog(String category, Level level) {
        if (!enabled) return false;
        if (level.ordinal() < minimumLevel.ordinal()) return false;
        if (allCategoriesEnabled) return true;
        return category != null && ENABLED_CATEGORIES.contains(category.toLowerCase(Locale.ROOT));
    }

    private static void write(Level level, String category, String message) {
        long id = COUNTER.incrementAndGet();
        String time = LocalTime.now().format(TIME);
        String cat = (category == null ? "general" : category).toLowerCase(Locale.ROOT);
        if (cat.length() > 8) cat = cat.substring(0, 8);

        String colour;
        String glyph;
        switch (level) {
            case TRACE -> {
                colour = Ansi.gray();
                glyph = Glyphs.trace();
            }
            case DEBUG -> {
                colour = Ansi.cyan();
                glyph = Glyphs.debug();
            }
            case INFO -> {
                colour = Ansi.green();
                glyph = Glyphs.info();
            }
            case WARN -> {
                colour = Ansi.yellow();
                glyph = Glyphs.warn();
            }
            default -> {
                colour = Ansi.reset();
                glyph = "?";
            }
        }
        String reset = Ansi.reset();

        String line = String.format("%s[%s]%s %s#%04d%s %s%s%s [%s%-8s%s] %s",
                colour, level, reset,
                Ansi.dim(), id, reset,
                colour, time, reset,
                colour, cat, reset,
                message);

        String rendered = colour + glyph + " " + reset + line;

        synchronized (WRITE_LOCK) {
            System.err.println(rendered);
        }
    }

    private static String safeFormat(String format, Object... args) {
        if (format == null) return "";
        if (args == null || args.length == 0) return format;
        try {
            StringBuilder sb = new StringBuilder();
            int argIndex = 0;
            int i = 0;
            while (i < format.length()) {
                if (format.charAt(i) == '{' && i + 1 < format.length() && format.charAt(i + 1) == '}') {
                    sb.append(argIndex < args.length ? safeArg(args[argIndex]) : "{}");
                    argIndex++;
                    i += 2;
                } else {
                    sb.append(format.charAt(i));
                    i++;
                }
            }
            return sb.toString();
        } catch (RuntimeException e) {
            return "<format error: " + e.getClass().getSimpleName() + "> " + format;
        }
    }

    private static String joinArgsPlain(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(plainArg(args[i]));
        }
        return sb.toString();
    }

    private static String plainArg(Object arg) {
        if (arg == null) return "null";
        try {
            String text = String.valueOf(arg);
            return text.length() > 60 ? text.substring(0, 57) + "..." : text;
        } catch (RuntimeException e) {
            return "<unprintable: " + e.getClass().getSimpleName() + ">";
        }
    }

    private static String safe(String text) { return text == null ? "null" : text; }

    private static String safeArg(Object arg) {
        if (arg == null) return "null";
        if (arg instanceof String text) {
            return "\"" + (text.length() > 60 ? text.substring(0, 57) + "..." : text) + "\"";
        }
        if (arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?>) return String.valueOf(arg);
        try {
            String text = String.valueOf(arg);
            return text.length() > 80 ? text.substring(0, 77) + "..." : text;
        } catch (RuntimeException e) {
            return "<unprintable: " + e.getClass().getSimpleName() + ">";
        }
    }
}
