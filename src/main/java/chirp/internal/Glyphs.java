package chirp.internal;

/**
 * Symbol provider with graceful Unicode → ASCII fallback.
 *
 * <p><b>Internal API.</b> Do not use directly. The library's own code
 * calls {@code Glyphs.success()}, {@code Glyphs.error()}, and friends
 * when building console output. Students configure the style via
 * {@link chirp.startup.StartupConfig.Builder#noUnicode()} or
 * {@code -Dchirp.ascii=true}.</p>
 *
 * <h2>Three styles</h2>
 * <table border="1">
 *   <caption>Glyph styles and their typical usage</caption>
 *   <tr><th>Style</th><th>Example symbols</th><th>When</th></tr>
 *   <tr><td>{@code UNICODE_FANCY}</td>
 *       <td>✓ ✘ ⚠ ℹ ─ ═ │ ┌ ┐</td>
 *       <td>Modern terminals — default when {@link Capabilities#unicode()}
 *           returns true</td></tr>
 *   <tr><td>{@code UNICODE_BASIC}</td>
 *       <td>+ x ! i • → │</td>
 *       <td>Very old terminals that render some glyphs but not
 *           box-drawing characters</td></tr>
 *   <tr><td>{@code ASCII}</td>
 *       <td>OK ERROR WARN INFO * -> |</td>
 *       <td>Non-Unicode consoles — default when {@link Capabilities#unicode()}
 *           returns false</td></tr>
 * </table>
 *
 * <h2>Automatic selection</h2>
 * <p>Style is chosen once at class load from
 * {@link Capabilities#unicode()}. The three
 * {@link #setStyle(Style)}, {@link #resetToAuto()}, and
 * {@link #getStyle()} methods let the startup code override the
 * automatic choice (e.g. {@code -Dchirp.ascii=true} forces ASCII).</p>
 *
 * <h2>The bare token contract</h2>
 * <p>Every symbol method returns a <b>bare token</b> with no
 * surrounding brackets — {@code Glyphs.success()} returns {@code "✓"}
 * or {@code "OK"}, not {@code "[✓]"} or {@code "[OK]"}. This is
 * deliberate: {@link chirp.console.Console} and the trace writer add
 * their own brackets so the same symbol can be reused in different
 * contexts (bracketed prefix in console messages, unbracketed inline
 * in traces, etc.).</p>
 *
 * <h2>Line-drawing helpers</h2>
 * <p>{@link #hLine(int)} and {@link #thickHLine(int)} return N
 * repetitions of the appropriate horizontal-line character, so a
 * caller writing {@code Glyphs.hLine(40)} gets 40 dashes in ASCII mode
 * or 40 box-drawing horizontal lines in Unicode mode — and the
 * surrounding code does not need to check the style.</p>
 *
 * @see Capabilities
 * @see chirp.console.Console
 * @see chirp.console.Separator
 * @since 1.0.0
 */
public final class Glyphs {

    /**
     * The three glyph styles. Selected automatically from
     * {@link Capabilities#unicode()} unless overridden.
     */
    public enum Style { UNICODE_FANCY, UNICODE_BASIC, ASCII }

    private static volatile Style style = autoDetect();

    private Glyphs() {}

    private static Style autoDetect() {
        return Capabilities.unicode() ? Style.UNICODE_FANCY : Style.ASCII;
    }

    /**
     * Returns the current style.
     *
     * @return the active style
     * @see #setStyle(Style)
     * @since 1.0.0
     */
    public static Style getStyle() { return style; }

    /**
     * Sets the style explicitly. A null argument is ignored.
     *
     * @param newStyle the new style
     * @see #resetToAuto()
     * @since 1.0.0
     */
    public static void setStyle(Style newStyle) { if (newStyle != null) style = newStyle; }

    /**
     * Resets the style to the automatic choice from
     * {@link Capabilities#unicode()}.
     *
     * @since 1.0.0
     */
    public static void resetToAuto() { style = autoDetect(); }

    // ---------------------------------------------------------- semantic

    /**
     * Returns the "success" glyph — {@code "✓"} in fancy Unicode,
     * {@code "+"} in basic Unicode, {@code "OK"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String success() {
        return switch (style) {
            case UNICODE_FANCY -> "✓";
            case UNICODE_BASIC -> "+";
            case ASCII -> "OK";
        };
    }

    /** Alias of {@link #success()} */
    public static String ok() { return success(); }
    /** Alias of {@link #success()} */
    public static String check() { return success(); }
    /** Alias of {@link #success()} */
    public static String done() { return success(); }

    /**
     * Returns the "error" glyph — {@code "✘"} in fancy Unicode,
     * {@code "x"} in basic Unicode, {@code "ERROR"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String error() {
        return switch (style) {
            case UNICODE_FANCY -> "✘";
            case UNICODE_BASIC -> "x";
            case ASCII -> "ERROR";
        };
    }

    /** Alias of {@link #error()} */
    public static String fail() { return error(); }
    /** Alias of {@link #error()} */
    public static String cross() { return error(); }

    /**
     * Returns the "warning" glyph — {@code "⚠"} in fancy Unicode,
     * {@code "!"} in basic Unicode, {@code "WARN"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String warn() {
        return switch (style) {
            case UNICODE_FANCY -> "⚠";
            case UNICODE_BASIC -> "!";
            case ASCII -> "WARN";
        };
    }

    /** Alias of {@link #warn()} */
    public static String alert() { return warn(); }
    /** Alias of {@link #warn()} */
    public static String caution() { return warn(); }

    /**
     * Returns the "info" glyph — {@code "ℹ"} in fancy Unicode,
     * {@code "i"} in basic Unicode, {@code "INFO"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String info() {
        return switch (style) {
            case UNICODE_FANCY -> "ℹ";
            case UNICODE_BASIC -> "i";
            case ASCII -> "INFO";
        };
    }

    /** Alias of {@link #info()} */
    public static String note() { return info(); }
    /** Alias of {@link #info()} */
    public static String tip() { return info(); }

    /**
     * Returns the "debug" glyph — {@code "◆"} in fancy Unicode,
     * {@code "o"} in basic Unicode, {@code "DEBUG"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String debug() {
        return switch (style) {
            case UNICODE_FANCY -> "◆";
            case UNICODE_BASIC -> "o";
            case ASCII -> "DEBUG";
        };
    }

    /** Alias of {@link #debug()} */
    public static String bug() { return debug(); }

    /**
     * Returns the "trace" glyph — {@code "·"} in fancy Unicode,
     * {@code "."} in basic Unicode, {@code "TRACE"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String trace() {
        return switch (style) {
            case UNICODE_FANCY -> "·";
            case UNICODE_BASIC -> ".";
            case ASCII -> "TRACE";
        };
    }

    /** Alias of {@link #trace()} */
    public static String log() { return trace(); }

    // ---------------------------------------------------------- decorative

    /**
     * Returns the bullet / point glyph — {@code "•"} in fancy Unicode,
     * {@code "*"} otherwise.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String bullet() {
        return switch (style) {
            case UNICODE_FANCY -> "•";
            case UNICODE_BASIC -> "*";
            case ASCII -> "*";
        };
    }

    /** Alias of {@link #bullet()} */
    public static String point() { return bullet(); }
    /** Alias of {@link #bullet()} */
    public static String dash() { return bullet(); }

    /**
     * Returns the "right arrow" glyph — {@code "→"} in fancy Unicode,
     * {@code "->"} in ASCII.
     *
     * @return the bare symbol token
     * @since 1.0.0
     */
    public static String arrow() {
        return switch (style) {
            case UNICODE_FANCY -> "→";
            case UNICODE_BASIC -> ">";
            case ASCII -> "->";
        };
    }

    /** Alias of {@link #arrow()} */
    public static String right() { return arrow(); }
    /** Alias of {@link #arrow()} */
    public static String next() { return arrow(); }

    /** @return the left arrow glyph, or {@code "<-"} in ASCII */
    public static String arrowLeft() {
        return switch (style) {
            case UNICODE_FANCY -> "←";
            case UNICODE_BASIC -> "<";
            case ASCII -> "<-";
        };
    }

    /** @return the up arrow glyph, or {@code "^"} in ASCII */
    public static String arrowUp() {
        return switch (style) {
            case UNICODE_FANCY -> "↑";
            case UNICODE_BASIC -> "^";
            case ASCII -> "^";
        };
    }

    /** @return the down arrow glyph, or {@code "v"} in ASCII */
    public static String arrowDown() {
        return switch (style) {
            case UNICODE_FANCY -> "↓";
            case UNICODE_BASIC -> "v";
            case ASCII -> "v";
        };
    }

    // ---------------------------------------------------------- box drawing

    /**
     * Returns a horizontal line of N repetitions of the appropriate
     * character — {@code "─"} in fancy Unicode, {@code "-"} otherwise.
     *
     * @param n the length (negative → empty string)
     * @return the repeated line
     * @see #thickHLine(int)
     * @since 1.0.0
     */
    public static String hLine(int n) {
        String ch = style == Style.UNICODE_FANCY ? "─" : "-";
        return ch.repeat(Math.max(0, n));
    }

    /**
     * Returns a thick horizontal line of N repetitions of the
     * appropriate character — {@code "═"} in fancy Unicode,
     * {@code "="} otherwise.
     *
     * @param n the length (negative → empty string)
     * @return the repeated line
     * @see #hLine(int)
     * @since 1.0.0
     */
    public static String thickHLine(int n) {
        String ch = style == Style.UNICODE_FANCY ? "═" : "=";
        return ch.repeat(Math.max(0, n));
    }

    /** @return the top-left corner glyph, or {@code "+"} in ASCII */
    public static String topLeft() {
        return style == Style.UNICODE_FANCY ? "┌" : "+";
    }
    /** @return the top-right corner glyph, or {@code "+"} in ASCII */
    public static String topRight() {
        return style == Style.UNICODE_FANCY ? "┐" : "+";
    }
    /** @return the bottom-left corner glyph, or {@code "+"} in ASCII */
    public static String bottomLeft() {
        return style == Style.UNICODE_FANCY ? "└" : "+";
    }
    /** @return the bottom-right corner glyph, or {@code "+"} in ASCII */
    public static String bottomRight() {
        return style == Style.UNICODE_FANCY ? "┘" : "+";
    }
    /** @return the vertical bar glyph, or {@code "|"} in ASCII */
    public static String vertical() {
        return style == Style.UNICODE_FANCY ? "│" : "|";
    }
}
