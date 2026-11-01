package chirp.internal;

/**
 * ANSI escape sequences — gated by {@link Capabilities#ansi()}.
 *
 * <p>Supports 8 basic colours, bright variants, bold, underline, dim, and
 * 24-bit true colour when the terminal advertises it. When ANSI is
 * unsupported every method returns an empty string, so callers can concatenate
 * unconditionally without checking.</p>
 */
public final class Ansi {

    private static volatile boolean enabled = Capabilities.ansi();

    private Ansi() {}

    public static boolean isSupported() { return Capabilities.ansi(); }
    public static boolean isEnabled() { return enabled && Capabilities.ansi(); }

    public static void setEnabled(boolean value) {
        enabled = value && Capabilities.ansi();
    }

    private static String esc(String code) {
        return isEnabled() ? code : "";
    }

    // --------------------------------------------------- basic sequences
    public static String reset() { return esc("\u001B[0m"); }
    public static String bold() { return esc("\u001B[1m"); }
    public static String dim() { return esc("\u001B[2m"); }
    public static String italic() { return esc("\u001B[3m"); }
    public static String underline() { return esc("\u001B[4m"); }
    public static String blink() { return esc("\u001B[5m"); }
    public static String reverse() { return esc("\u001B[7m"); }

    // --------------------------------------------------- foreground colours
    public static String red() { return esc("\u001B[31m"); }
    public static String green() { return esc("\u001B[32m"); }
    public static String yellow() { return esc("\u001B[33m"); }
    public static String blue() { return esc("\u001B[34m"); }
    public static String magenta() { return esc("\u001B[35m"); }
    public static String cyan() { return esc("\u001B[36m"); }
    public static String white() { return esc("\u001B[37m"); }
    public static String gray() { return esc("\u001B[90m"); }
    public static String grey() { return gray(); }
    public static String black() { return esc("\u001B[30m"); }

    // --------------------------------------------------- bright colours
    public static String brightRed() { return esc("\u001B[91m"); }
    public static String brightGreen() { return esc("\u001B[92m"); }
    public static String brightYellow() { return esc("\u001B[93m"); }
    public static String brightBlue() { return esc("\u001B[94m"); }
    public static String brightMagenta() { return esc("\u001B[95m"); }
    public static String brightCyan() { return esc("\u001B[96m"); }
    public static String brightWhite() { return esc("\u001B[97m"); }

    // --------------------------------------------------- 24-bit true colour
    public static String rgb(int r, int g, int b) {
        if (!Capabilities.trueColor()) return "";
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return esc("\u001B[38;2;" + r + ";" + g + ";" + b + "m");
    }

    public static String bgRgb(int r, int g, int b) {
        if (!Capabilities.trueColor()) return "";
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return esc("\u001B[48;2;" + r + ";" + g + ";" + b + "m");
    }

    public static String bgBlack() { return esc("\u001B[40m"); }
    public static String bgRed() { return esc("\u001B[41m"); }
    public static String bgGreen() { return esc("\u001B[42m"); }
    public static String bgYellow() { return esc("\u001B[43m"); }
    public static String bgBlue() { return esc("\u001B[44m"); }
    public static String bgMagenta() { return esc("\u001B[45m"); }
    public static String bgCyan() { return esc("\u001B[46m"); }
    public static String bgWhite() { return esc("\u001B[47m"); }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
