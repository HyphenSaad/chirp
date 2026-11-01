package chirp.internal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Internal tracer — shows exactly what the library is doing when enabled.
 *
 * <p>Each level gets its own colour and glyph so trace output is
 * visually scannable:</p>
 * <pre>
 *   [TRACE] ·  dim grey
 *   [DEBUG] ◆  cyan
 *   [INFO]  ℹ  green
 *   [WARN]  ⚠  yellow
 * </pre>
 */
public final class Trace {

    public enum Level { TRACE, DEBUG, INFO, WARN }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final Set<String> ENABLED_CATEGORIES = ConcurrentHashMap.newKeySet();

    public static final List<String> KNOWN_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "input", "reader", "strict", "rule", "search", "filter", "page",
            "text", "numbers", "format", "datetime", "file", "csv", "json",
            "session", "config", "stats", "console", "menu", "startup"));

    private static volatile boolean enabled = Boolean.parseBoolean(System.getProperty("chirp.trace", "false"));
    private static volatile Level minimumLevel = Level.TRACE;
    private static volatile boolean allCategoriesEnabled = true;

    private Trace() {}

    // ---------------------------------------------------------------- toggles
    public static boolean isEnabled() { return enabled; }
    public static void enable() { enabled = true; }
    public static void disable() { enabled = false; }
    public static Level getLevel() { return minimumLevel; }
    public static void setLevel(Level level) { minimumLevel = level == null ? Level.TRACE : level; }

    public static void reset() {
        enabled = false;
        minimumLevel = Level.TRACE;
        allCategoriesEnabled = true;
        ENABLED_CATEGORIES.clear();
        COUNTER.set(0);
    }

    public static void enableCategory(String category) {
        if (category != null) ENABLED_CATEGORIES.add(category.toLowerCase());
    }
    public static void disableCategory(String category) {
        if (category != null) ENABLED_CATEGORIES.remove(category.toLowerCase());
    }

    public static void enableOnly(String... categories) {
        allCategoriesEnabled = false;
        ENABLED_CATEGORIES.clear();
        if (categories != null) for (String c : categories) if (c != null) ENABLED_CATEGORIES.add(c.toLowerCase());
    }

    public static void enableAllCategories() {
        allCategoriesEnabled = true;
        ENABLED_CATEGORIES.clear();
    }

    public static Set<String> activeCategories() {
        if (allCategoriesEnabled) return Collections.unmodifiableSet(new LinkedHashSet<>(KNOWN_CATEGORIES));
        return Collections.unmodifiableSet(new LinkedHashSet<>(ENABLED_CATEGORIES));
    }

    // ---------------------------------------------------------------- log API
    public static void trace(String category, String message) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, message);
    }
    public static void trace(String category, String format, Object... args) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, safeFormat(format, args));
    }

    public static void debug(String category, String message) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, message);
    }
    public static void debug(String category, String format, Object... args) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, safeFormat(format, args));
    }

    public static void info(String category, String message) {
        if (!shouldLog(category, Level.INFO)) return;
        write(Level.INFO, category, message);
    }
    public static void info(String category, String format, Object... args) {
        if (!shouldLog(category, Level.INFO)) return;
        write(Level.INFO, category, safeFormat(format, args));
    }

    public static void warn(String category, String message) {
        if (!shouldLog(category, Level.WARN)) return;
        write(Level.WARN, category, message);
    }
    public static void warn(String category, String format, Object... args) {
        if (!shouldLog(category, Level.WARN)) return;
        write(Level.WARN, category, safeFormat(format, args));
    }

    // ---------------------------------------------------------------- structured
    public static void enter(String category, String method, Object... args) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "enter " + safe(method) + "(" + joinArgsPlain(args) + ")");
    }

    public static void exit(String category, String method, Object result) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "exit  " + safe(method) + " => " + safeArg(result));
    }

    public static void exit(String category, String method) {
        if (!shouldLog(category, Level.TRACE)) return;
        write(Level.TRACE, category, "exit  " + safe(method));
    }

    public static void decision(String category, String question, boolean result) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, "decision: " + safe(question) + " => " + (result ? "TRUE" : "FALSE"));
    }

    public static void value(String category, String name, Object value) {
        if (!shouldLog(category, Level.DEBUG)) return;
        write(Level.DEBUG, category, "value: " + safe(name) + " = " + safeArg(value));
    }

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
        return category != null && ENABLED_CATEGORIES.contains(category.toLowerCase());
    }

    private static void write(Level level, String category, String message) {
        long id = COUNTER.incrementAndGet();
        String time = LocalTime.now().format(TIME);
        String cat = (category == null ? "general" : category).toLowerCase();
        if (cat.length() > 8) cat = cat.substring(0, 8);

        // Colour + glyph per level
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

        // Prepend glyph
        String rendered = colour + glyph + " " + reset + line;

        synchronized (System.err) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            return "<unprintable: " + e.getClass().getSimpleName() + ">";
        }
    }
}
