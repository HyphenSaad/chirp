package chirp.console;

/**
 * Toggleable debug logging. Disabled by default — all calls are no-ops until
 * {@link #enable()} is invoked.
 */
public final class Debug {

    private static boolean enabled = false;

    private Debug() {}

    public static void enable() { enabled = true; }
    public static void disable() { enabled = false; }
    public static boolean isEnabled() { return enabled; }

    public static void log(String message) {
        if (!enabled) return;
        System.err.println("[DEBUG] " + message);
    }

    public static void logf(String format, Object... args) {
        if (!enabled) return;
        System.err.printf("[DEBUG] " + format + "%n", args);
    }
}
