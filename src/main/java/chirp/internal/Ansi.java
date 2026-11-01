package chirp.internal;

/**
 * ANSI escape sequences — the low-level colour and styling primitives
 * used throughout the library.
 *
 * <p><b>Internal API.</b> Do not use directly. For styled strings a
 * student can call from their own code, use the style helpers in
 * {@link chirp.console.Console}:
 * {@link chirp.console.Console#bold(String) Console.bold(...)},
 * {@link chirp.console.Console#red(String) Console.red(...)}, and so
 * on. This class is the raw source of the escape sequences that those
 * helpers wrap.</p>
 *
 * <h2>Graceful degradation</h2>
 * <p>Every method returns {@code ""} (empty string) when ANSI is not
 * available — a plain Windows console without ANSI support, a redirected
 * stream, or when the user has set {@code NO_COLOR} or
 * {@code -Dchirp.ansi=false}. This means callers can concatenate the
 * results unconditionally without checking, and the styled string
 * becomes a plain string in those environments:</p>
 * <pre>{@code
 * // Works regardless of ANSI availability:
 * System.out.println(Ansi.red() + "Error!" + Ansi.reset());
 * // → coloured "Error!" on capable terminals, plain "Error!" otherwise
 * }</pre>
 *
 * <h2>Detection</h2>
 * <p>{@link Capabilities#ansi()} determines availability once, at class
 * load, based on the {@code NO_COLOR} env var, the {@code TERM} env var,
 * {@code -Dchirp.ansi}, and platform heuristics. {@link #isSupported()}
 * reports that fixed result; {@link #isEnabled()} additionally reflects
 * whether colour has been toggled off at runtime via
 * {@link chirp.config.Config#colorEnabled(boolean) Config.colorEnabled(false)}.</p>
 *
 * <h2>True colour</h2>
 * <p>{@link #rgb(int, int, int)} and {@link #bgRgb(int, int, int)}
 * emit 24-bit true-colour escapes when the terminal supports them
 * (detected via {@code COLORTERM} or {@code TERM=*-256color}).
 * {@link Capabilities#trueColor()} reports the fixed result.</p>
 *
 * @see Capabilities
 * @see chirp.console.Console
 * @see chirp.config.Config#colorEnabled(boolean)
 * @since 1.0.0
 */
public final class Ansi {

    private static volatile boolean enabled = Capabilities.ansi();

    private Ansi() {}

    /**
     * Whether the terminal supports ANSI sequences at all — fixed at
     * class load by {@link Capabilities#ansi()}, unaffected by the
     * runtime colour toggle.
     *
     * @return {@code true} if ANSI sequences are supported
     * @see #isEnabled()
     * @since 1.0.0
     */
    public static boolean isSupported() { return Capabilities.ansi(); }

    /**
     * Whether colour output is currently active — the terminal supports
     * ANSI ({@link #isSupported()}) <b>and</b> colour has not been
     * disabled at runtime via
     * {@link chirp.config.Config#colorEnabled(boolean)}.
     *
     * @return {@code true} if styled output will actually be emitted
     * @see #isSupported()
     * @see #setEnabled(boolean)
     * @since 1.0.0
     */
    public static boolean isEnabled() { return enabled && Capabilities.ansi(); }

    /**
     * Enables or disables colour output. Disabling is a no-op on
     * terminals that do not support ANSI anyway.
     *
     * @param value whether to enable colour
     * @see #isEnabled()
     * @see chirp.config.Config#colorEnabled(boolean)
     * @since 1.0.0
     */
    public static void setEnabled(boolean value) {
        enabled = value && Capabilities.ansi();
    }

    private static String esc(String code) {
        return isEnabled() ? code : "";
    }

    // --------------------------------------------------- basic sequences

    /** @return the ANSI reset sequence, or {@code ""} if colour is off */
    public static String reset() {
        return esc("\u001B[0m");
    }
    /** @return the ANSI bold sequence, or {@code ""} if colour is off */
    public static String bold() {
        return esc("\u001B[1m");
    }
    /** @return the ANSI dim sequence, or {@code ""} if colour is off */
    public static String dim() {
        return esc("\u001B[2m");
    }
    /** @return the ANSI italic sequence, or {@code ""} if colour is off */
    public static String italic() {
        return esc("\u001B[3m");
    }
    /** @return the ANSI underline sequence, or {@code ""} if colour is off */
    public static String underline() {
        return esc("\u001B[4m");
    }
    /** @return the ANSI blink sequence, or {@code ""} if colour is off */
    public static String blink() {
        return esc("\u001B[5m");
    }
    /** @return the ANSI reverse-video sequence, or {@code ""} if colour is off */
    public static String reverse() {
        return esc("\u001B[7m");
    }

    // --------------------------------------------------- foreground colours

    /** @return the ANSI red foreground sequence, or {@code ""} */
    public static String red() {
        return esc("\u001B[31m");
    }
    /** @return the ANSI green foreground sequence, or {@code ""} */
    public static String green() {
        return esc("\u001B[32m");
    }
    /** @return the ANSI yellow foreground sequence, or {@code ""} */
    public static String yellow() {
        return esc("\u001B[33m");
    }
    /** @return the ANSI blue foreground sequence, or {@code ""} */
    public static String blue() {
        return esc("\u001B[34m");
    }
    /** @return the ANSI magenta foreground sequence, or {@code ""} */
    public static String magenta() {
        return esc("\u001B[35m");
    }
    /** @return the ANSI cyan foreground sequence, or {@code ""} */
    public static String cyan() {
        return esc("\u001B[36m");
    }
    /** @return the ANSI white foreground sequence, or {@code ""} */
    public static String white() {
        return esc("\u001B[37m");
    }
    /** @return the ANSI gray (bright black) foreground sequence, or {@code ""} */
    public static String gray() {
        return esc("\u001B[90m");
    }
    /** Alias of {@link #gray()} — British spelling. */
    public static String grey() { return gray(); }
    /** @return the ANSI black foreground sequence, or {@code ""} */
    public static String black() {
        return esc("\u001B[30m");
    }

    // --------------------------------------------------- bright colours

    /** @return the ANSI bright red sequence, or {@code ""} */
    public static String brightRed() {
        return esc("\u001B[91m");
    }
    /** @return the ANSI bright green sequence, or {@code ""} */
    public static String brightGreen() {
        return esc("\u001B[92m");
    }
    /** @return the ANSI bright yellow sequence, or {@code ""} */
    public static String brightYellow() {
        return esc("\u001B[93m");
    }
    /** @return the ANSI bright blue sequence, or {@code ""} */
    public static String brightBlue() {
        return esc("\u001B[94m");
    }
    /** @return the ANSI bright magenta sequence, or {@code ""} */
    public static String brightMagenta() {
        return esc("\u001B[95m");
    }
    /** @return the ANSI bright cyan sequence, or {@code ""} */
    public static String brightCyan() {
        return esc("\u001B[96m");
    }
    /** @return the ANSI bright white sequence, or {@code ""} */
    public static String brightWhite() {
        return esc("\u001B[97m");
    }

    // --------------------------------------------------- 24-bit true colour

    /**
     * Emits a 24-bit true-colour foreground escape
     * ({@code ESC[38;2;R;G;Bm}). Returns {@code ""} when the terminal
     * does not advertise true-colour support ({@link Capabilities#trueColor()}),
     * so callers get plain text on legacy terminals.
     *
     * @param r red component (0–255, clamped)
     * @param g green component (0–255, clamped)
     * @param b blue component (0–255, clamped)
     * @return the escape sequence, or {@code ""}
     * @see Capabilities#trueColor()
     * @since 1.0.0
     */
    public static String rgb(int r, int g, int b) {
        if (!Capabilities.trueColor()) return "";
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return esc("\u001B[38;2;" + r + ";" + g + ";" + b + "m");
    }

    /**
     * Emits a 24-bit true-colour background escape
     * ({@code ESC[48;2;R;G;Bm}). Returns {@code ""} when the terminal
     * does not advertise true-colour support.
     *
     * @param r red component (0–255, clamped)
     * @param g green component (0–255, clamped)
     * @param b blue component (0–255, clamped)
     * @return the escape sequence, or {@code ""}
     * @see #rgb(int, int, int)
     * @since 1.0.0
     */
    public static String bgRgb(int r, int g, int b) {
        if (!Capabilities.trueColor()) return "";
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return esc("\u001B[48;2;" + r + ";" + g + ";" + b + "m");
    }

    /** @return the ANSI black background sequence, or {@code ""} */
    public static String bgBlack() {
        return esc("\u001B[40m");
    }
    /** @return the ANSI red background sequence, or {@code ""} */
    public static String bgRed() {
        return esc("\u001B[41m");
    }
    /** @return the ANSI green background sequence, or {@code ""} */
    public static String bgGreen() {
        return esc("\u001B[42m");
    }
    /** @return the ANSI yellow background sequence, or {@code ""} */
    public static String bgYellow() {
        return esc("\u001B[43m");
    }
    /** @return the ANSI blue background sequence, or {@code ""} */
    public static String bgBlue() {
        return esc("\u001B[44m");
    }
    /** @return the ANSI magenta background sequence, or {@code ""} */
    public static String bgMagenta() {
        return esc("\u001B[45m");
    }
    /** @return the ANSI cyan background sequence, or {@code ""} */
    public static String bgCyan() {
        return esc("\u001B[46m");
    }
    /** @return the ANSI white background sequence, or {@code ""} */
    public static String bgWhite() {
        return esc("\u001B[47m");
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
