package chirp.internal;

/**
 * Symbol provider with graceful Unicode → ASCII fallback.
 *
 * <p>Three styles are available:</p>
 * <ul>
 *   <li>{@link Style#UNICODE_FANCY} — pretty symbols (default when unicode is available)</li>
 *   <li>{@link Style#UNICODE_BASIC} — minimal unicode symbols for very old terminals</li>
 *   <li>{@link Style#ASCII}         — plain ASCII tokens (default when unicode is unavailable)</li>
 * </ul>
 *
 * <p>Selected automatically from {@link Capabilities}. Override with
 * {@link #setStyle(Style)} or via {@code StartupConfig.noUnicode()}.</p>
 *
 * <p>Every symbol method returns a bare token without surrounding
 * brackets — {@code Console} and {@code Trace} add the brackets so the same
 * symbol can be reused in different contexts.</p>
 */
public final class Glyphs {

    public enum Style { UNICODE_FANCY, UNICODE_BASIC, ASCII }

    private static volatile Style style = autoDetect();

    private Glyphs() {}

    private static Style autoDetect() {
        return Capabilities.unicode() ? Style.UNICODE_FANCY : Style.ASCII;
    }

    public static Style getStyle() { return style; }
    public static void setStyle(Style newStyle) { if (newStyle != null) style = newStyle; }
    public static void resetToAuto() { style = autoDetect(); }

    // ---------------------------------------------------------- semantic

    public static String success() {
        return switch (style) {
            case UNICODE_FANCY -> "✓";
            case UNICODE_BASIC -> "+";
            case ASCII -> "OK";
        };
    }
    public static String ok() { return success(); }
    public static String check() { return success(); }
    public static String done() { return success(); }

    public static String error() {
        return switch (style) {
            case UNICODE_FANCY -> "✘";
            case UNICODE_BASIC -> "x";
            case ASCII -> "ERROR";
        };
    }
    public static String fail() { return error(); }
    public static String cross() { return error(); }

    public static String warn() {
        return switch (style) {
            case UNICODE_FANCY -> "⚠";
            case UNICODE_BASIC -> "!";
            case ASCII -> "WARN";
        };
    }
    public static String alert() { return warn(); }
    public static String caution() { return warn(); }

    public static String info() {
        return switch (style) {
            case UNICODE_FANCY -> "ℹ";
            case UNICODE_BASIC -> "i";
            case ASCII -> "INFO";
        };
    }
    public static String note() { return info(); }
    public static String tip() { return info(); }

    public static String debug() {
        return switch (style) {
            case UNICODE_FANCY -> "◆";
            case UNICODE_BASIC -> "o";
            case ASCII -> "DEBUG";
        };
    }
    public static String bug() { return debug(); }

    public static String trace() {
        return switch (style) {
            case UNICODE_FANCY -> "·";
            case UNICODE_BASIC -> ".";
            case ASCII -> "TRACE";
        };
    }
    public static String log() { return trace(); }

    // ---------------------------------------------------------- decorative

    public static String bullet() {
        return switch (style) {
            case UNICODE_FANCY -> "•";
            case UNICODE_BASIC -> "*";
            case ASCII -> "*";
        };
    }
    public static String point() { return bullet(); }
    public static String dash() { return bullet(); }

    public static String arrow() {
        return switch (style) {
            case UNICODE_FANCY -> "→";
            case UNICODE_BASIC -> ">";
            case ASCII -> "->";
        };
    }
    public static String right() { return arrow(); }
    public static String next() { return arrow(); }

    public static String arrowLeft() {
        return switch (style) {
            case UNICODE_FANCY -> "←";
            case UNICODE_BASIC -> "<";
            case ASCII -> "<-";
        };
    }
    public static String arrowUp() {
        return switch (style) {
            case UNICODE_FANCY -> "↑";
            case UNICODE_BASIC -> "^";
            case ASCII -> "^";
        };
    }
    public static String arrowDown() {
        return switch (style) {
            case UNICODE_FANCY -> "↓";
            case UNICODE_BASIC -> "v";
            case ASCII -> "v";
        };
    }

    // ---------------------------------------------------------- box drawing

    public static String hLine(int n) {
        String ch = style == Style.UNICODE_FANCY ? "─" : "-";
        return ch.repeat(Math.max(0, n));
    }

    public static String thickHLine(int n) {
        String ch = style == Style.UNICODE_FANCY ? "═" : "=";
        return ch.repeat(Math.max(0, n));
    }

    public static String topLeft() { return style == Style.UNICODE_FANCY ? "┌" : "+"; }
    public static String topRight() { return style == Style.UNICODE_FANCY ? "┐" : "+"; }
    public static String bottomLeft() { return style == Style.UNICODE_FANCY ? "└" : "+"; }
    public static String bottomRight() { return style == Style.UNICODE_FANCY ? "┘" : "+"; }
    public static String vertical() { return style == Style.UNICODE_FANCY ? "│" : "|"; }
}
