package chirp.console;

/**
 * Auto-prefixed messages with optional stderr logging.
 * <p>All methods return {@code void} and never throw.</p>
 */
public final class Messenger {

    private static boolean logToStderr = false;

    private Messenger() {}

    public static void setLogToStderr(boolean enabled) { logToStderr = enabled; }
    public static boolean isLogToStderr() { return logToStderr; }

    public static void success(String message) { Console.success(message); }
    public static void info(String message) { Console.info(message); }

    public static void warn(String message) {
        Console.warn(message);
        if (logToStderr) System.err.println("[LOG/WARN] " + message);
    }

    public static void error(String message) {
        Console.error(message);
        if (logToStderr) System.err.println("[LOG/ERROR] " + message);
    }
}
