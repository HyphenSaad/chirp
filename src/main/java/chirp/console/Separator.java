package chirp.console;

import chirp.internal.Ansi;
import chirp.internal.Capabilities;

import java.util.Locale;
import java.util.Map;

/**
 * Configurable decorative horizontal separator.
 *
 * <p>A {@code Separator} renders a horizontal line with an optional
 * label, alignment, colour, and width. Every aspect is configurable via
 * a fluent {@link Builder} or a key-value API. The result is a styled
 * string — call {@link #render()} to get it, or {@link #print()} to
 * write it to the console.</p>
 *
 * <h2>The five aspects</h2>
 * <table border="1">
 *   <caption>Separator configuration options</caption>
 *   <tr><th>Aspect</th><th>Options</th><th>Default</th></tr>
 *   <tr><td>Width</td><td>any positive integer</td>
 *       <td>{@link #DEFAULT_WIDTH} (40)</td></tr>
 *   <tr><td>Style</td><td>{@link Style}</td><td>{@code SOLID}</td></tr>
 *   <tr><td>Align</td><td>{@link Align}</td><td>{@code FULL}</td></tr>
 *   <tr><td>Color</td><td>{@link Color}</td><td>{@code DEFAULT}</td></tr>
 *   <tr><td>Label</td><td>any string</td><td>{@code ""}</td></tr>
 * </table>
 * <p>Plus {@code indent} (leading spaces) and {@code customChar} for the
 * {@code CUSTOM} style.</p>
 *
 * <h2>Four ways to create</h2>
 *
 * <h3>1. Fluent builder</h3>
 * <pre>{@code
 * Console.line(
 *     Separator.builder()
 *         .width(60)
 *         .style(Separator.Style.DOUBLE)
 *         .centered()
 *         .label("Section 1")
 *         .build()
 * );
 * }</pre>
 *
 * <h3>2. Static factory shortcuts</h3>
 * <pre>{@code
 * Separator.width(60).print();          // 60-char solid line
 * Separator.doubleLine().print();       // double line at default width
 * Separator.starLine().print();         // star line
 * }</pre>
 *
 * <h3>3. Key-value API</h3>
 * <pre>{@code
 * Separator.from(Separator.Key.WIDTH, 60, Separator.Key.STYLE, Separator.Style.DASHED)
 *     .print();
 * }</pre>
 *
 * <h3>4. Direct from Console</h3>
 * <pre>{@code
 * Console.line();
 * Console.line(60);
 * Console.line(Separator.Style.DOUBLE);
 * Console.line(60, Separator.Style.DOUBLE);
 * }</pre>
 *
 * <h2>Unicode vs ASCII</h2>
 * <p>Uses Unicode box-drawing characters when the terminal supports
 * them ({@code ─ ═ ╌ ┄ ✻}), falling back to ASCII ({@code - = - . *})
 * otherwise. The fallback is automatic — no code changes needed.</p>
 *
 * <h2>Related</h2>
 * <p>For the full Console API that wraps this class, see
 * {@link Console#line()} and friends.</p>
 *
 * @see Console
 * @see Console#line()
 * @see Console#line(int)
 * @see Console#line(Separator.Style)
 * @see Console#line(int, Separator.Style)
 * @since 1.0.0
 */
public final class Separator {

    // ============================================================== enums

    /**
     * Character set used to draw the separator line.
     *
     * @since 1.0.0
     */
    public enum Style {
        /** Solid line — {@code ─} or {@code -}. */
        SOLID,
        /** Double line — {@code ═} or {@code =}. */
        DOUBLE,
        /** Dashed line — {@code ╌} or {@code -}. */
        DASHED,
        /** Dotted line — {@code ┄} or {@code .}. */
        DOTTED,
        /** Star line — {@code ✻} or {@code *}. */
        STAR,
        /** Equals line — same as DOUBLE. */
        EQUALS,
        /** Tilde line — {@code ~}. */
        TILDE,
        /** Arrow line — {@code →} or {@code >}. */
        ARROW,
        /** Solid ASCII line — {@code -}. */
        ASCII_SOLID,
        /** Dashed ASCII line — {@code -}. */
        ASCII_DASHED,
        /** Dotted ASCII line — {@code .}. */
        ASCII_DOTTED,
        /** Use {@code customChar}. */
        CUSTOM
    }

    /**
     * How to position the label within the line.
     *
     * @since 1.0.0
     */
    public enum Align {
        /** Label flush left, line to the right. */
        LEFT,
        /** Label centred. */
        CENTER,
        /** Label flush right, line to the left. */
        RIGHT,
        /** Label replaces the line entirely (or is centred in place). */
        FULL
    }

    /**
     * Colour of the rendered line.
     *
     * @since 1.0.0
     */
    public enum Color { DEFAULT, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, GRAY }

    /**
     * Keys for the key-value {@link #from(Object...)} API.
     *
     * @since 1.0.0
     */
    public enum Key { WIDTH, STYLE, ALIGN, COLOR, LABEL, INDENT, CUSTOM_CHAR }

    /**
     * Default width in characters when no width is set.
     */
    public static final int DEFAULT_WIDTH = 40;

    private final int width;
    private final Style style;
    private final Align align;
    private final Color color;
    private final String label;
    private final int indent;
    private final char customChar;

    private Separator(Builder b) {
        this.width = Math.max(1, b.width);
        this.style = b.style != null ? b.style : Style.SOLID;
        this.align = b.align != null ? b.align : Align.FULL;
        this.color = b.color != null ? b.color : Color.DEFAULT;
        this.label = b.label == null ? "" : b.label;
        this.indent = Math.max(0, b.indent);
        this.customChar = b.customChar;
    }

    // ============================================================== factories

    /**
     * Returns a new builder — the primary way to construct a separator.
     *
     * <pre>{@code
     * Separator s = Separator.builder()
     *     .width(60)
     *     .style(Separator.Style.DOUBLE)
     *     .centered()
     *     .label("Section 1")
     *     .build();
     * s.print();
     * }</pre>
     *
     * @return a new builder
     * @see #of()
     * @see #create()
     * @see #from(Object...)
     * @since 1.0.0
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation and examples.
     *
     * @return a new builder
     * @see #builder()
     * @since 1.0.0
     */
    public static Builder of() { return new Builder(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation and examples.
     *
     * @return a new builder
     * @see #builder()
     * @since 1.0.0
     */
    public static Builder create() { return new Builder(); }

    /**
     * Shortcut — a solid line of the given width.
     *
     * @param w the width
     * @return the rendered separator
     * @since 1.0.0
     */
    public static Separator width(int w) { return builder().width(w).build(); }

    /**
     * Shortcut — a solid line at the default width.
     *
     * @return the rendered separator
     * @since 1.0.0
     */
    public static Separator line() { return builder().build(); }

    /**
     * Shortcut — a solid line at the default width.
     *
     * @return the rendered separator
     * @see #doubleLine()
     * @see #dashedLine()
     * @see #dottedLine()
     * @see #starLine()
     * @since 1.0.0
     */
    public static Separator solid() { return builder().style(Style.SOLID).build(); }

    /**
     * Shortcut — a double line at the default width.
     *
     * @return the rendered separator
     * @see #solid()
     * @since 1.0.0
     */
    public static Separator doubleLine() { return builder().style(Style.DOUBLE).build(); }

    /**
     * Shortcut — a dashed line at the default width.
     *
     * @return the rendered separator
     * @see #solid()
     * @since 1.0.0
     */
    public static Separator dashedLine() { return builder().style(Style.DASHED).build(); }

    /**
     * Shortcut — a dotted line at the default width.
     *
     * @return the rendered separator
     * @see #solid()
     * @since 1.0.0
     */
    public static Separator dottedLine() { return builder().style(Style.DOTTED).build(); }

    /**
     * Shortcut — a star line at the default width.
     *
     * @return the rendered separator
     * @see #solid()
     * @since 1.0.0
     */
    public static Separator starLine() { return builder().style(Style.STAR).build(); }

    // ============================================================== key-value

    /**
     * Constructs a separator from alternating {@link Key} → value pairs.
     *
     * <p>Unknown keys and wrong value types are silently skipped, so the
     * call is resilient to partial configuration.</p>
     *
     * <pre>{@code
     * Separator.from(
     *     Separator.Key.WIDTH, 60,
     *     Separator.Key.STYLE, Separator.Style.DOUBLE,
     *     Separator.Key.LABEL, "Section"
     * ).print();
     * }</pre>
     *
     * @param keyValuePairs alternating Key → value pairs (null → default separator)
     * @return the rendered separator
     * @see #from(Map)
     * @see #builder()
     * @since 1.0.0
     */
    public static Separator from(Object... keyValuePairs) {
        Builder b = builder();
        if (keyValuePairs == null) return b.build();
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            if (key == null) continue;
            String name = key instanceof Key k ? k.name() : String.valueOf(key).toUpperCase(Locale.ROOT);
            switch (name) {
                case "WIDTH" -> {
                    if (value instanceof Integer n) b.width(n);
                }
                case "STYLE" -> {
                    if (value instanceof Style s) b.style(s);
                }
                case "ALIGN" -> {
                    if (value instanceof Align a) b.align(a);
                }
                case "COLOR" -> {
                    if (value instanceof Color c) b.color(c);
                }
                case "LABEL" -> {
                    if (value instanceof String s) b.label(s);
                }
                case "INDENT" -> {
                    if (value instanceof Integer n) b.indent(n);
                }
                case "CUSTOM_CHAR" -> {
                    if (value instanceof Character c) b.customChar(c);
                }
                default -> {
                    /* ignore */ }
            }
        }
        return b.build();
    }

    /**
     * Constructs a separator from a map of configuration. The map's keys
     * are the enum constant names lowercased ({@code "width"},
     * {@code "style"}, {@code "align"}, {@code "color"},
     * {@code "label"}, {@code "indent"}, {@code "char"}).
     *
     * @param config the configuration map (null → default separator)
     * @return the rendered separator
     * @see #from(Object...)
     * @since 1.0.0
     */
    public static Separator from(Map<String, Object> config) {
        Builder b = builder();
        if (config == null) return b.build();
        if (config.get("width") instanceof Integer i) b.width(i);
        if (config.get("style") instanceof Style s) b.style(s);
        if (config.get("align") instanceof Align a) b.align(a);
        if (config.get("color") instanceof Color c) b.color(c);
        if (config.get("label") instanceof String s) b.label(s);
        if (config.get("indent") instanceof Integer i) b.indent(i);
        if (config.get("char") instanceof Character c) b.customChar(c);
        return b.build();
    }

    // ============================================================== accessors

    /**
     * Returns the configured width.
     *
     * @return the width in characters
     * @since 1.0.0
     */
    public int width() { return width; }

    /**
     * Returns the configured style.
     *
     * @return the style
     * @since 1.0.0
     */
    public Style style() { return style; }

    /**
     * Returns the configured alignment.
     *
     * @return the alignment
     * @since 1.0.0
     */
    public Align align() { return align; }

    /**
     * Returns the configured colour.
     *
     * @return the colour
     * @since 1.0.0
     */
    public Color color() { return color; }

    /**
     * Returns the configured label (empty string if none).
     *
     * @return the label
     * @since 1.0.0
     */
    public String label() { return label; }

    /**
     * Returns the configured indent (leading spaces).
     *
     * @return the indent
     * @since 1.0.0
     */
    public int indent() { return indent; }

    // ============================================================== render

    /**
     * Renders the separator as a string — line character(s), colour, and
     * label combined into the final output. No trailing newline.
     *
     * @return the rendered string
     * @see #print()
     * @since 1.0.0
     */
    public String render() {
        String token = resolveToken(style);
        String prefix = " ".repeat(indent);
        int innerWidth = width;

        String core;
        if (label.isEmpty() || align == Align.FULL) {
            core = token.repeat(innerWidth);
            if (!label.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int remaining = innerWidth;
                int left = (innerWidth - label.length()) / 2;
                if (left > 0) { sb.append(token.repeat(left)); remaining -= left; }
                if (label.length() <= remaining) { sb.append(label); remaining -= label.length(); }
                if (remaining > 0) sb.append(token.repeat(remaining));
                core = sb.toString();
            }
        } else {
            switch (align) {
                case LEFT -> core = label + token.repeat(Math.max(0, innerWidth - label.length()));
                case CENTER -> {
                    int total = Math.max(0, innerWidth - label.length());
                    int l = total / 2, r = total - l;
                    core = token.repeat(l) + label + token.repeat(r);
                }
                case RIGHT -> core = token.repeat(Math.max(0, innerWidth - label.length())) + label;
                default -> core = token.repeat(innerWidth);
            }
        }
        return prefix + colorCode() + core + Ansi.reset();
    }

    /**
     * Prints the separator followed by a newline. Requires
     * {@code Chirp.start()}.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #render()
     * @see #println()
     * @see #display()
     * @since 1.0.0
     */
    public void print() {
        chirp.startup.ChirpLifecycle.requireStarted();
        System.out.println(render());
    }

    /**
     * Alias of {@link #print()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #print()
     * @since 1.0.0
     */
    public void println() { print(); }

    /**
     * Alias of {@link #print()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #print()
     * @since 1.0.0
     */
    public void display() { print(); }

    /**
     * Returns the rendered string — matches {@link #render()}.
     *
     * @return the rendered string
     * @since 1.0.0
     */
    @Override
    public String toString() { return render(); }

    // ============================================================== helpers

    private String resolveToken(Style style) {
        boolean unicode = Capabilities.unicode();
        return switch (style) {
            case SOLID -> unicode ? "─" : "-";
            case DOUBLE -> unicode ? "═" : "=";
            case DASHED -> unicode ? "╌" : "-";
            case DOTTED -> unicode ? "┄" : ".";
            case STAR -> unicode ? "✻" : "*";
            case EQUALS -> unicode ? "═" : "=";
            case TILDE -> "~";
            case ARROW -> unicode ? "→" : ">";
            case ASCII_SOLID -> "-";
            case ASCII_DASHED -> "-";
            case ASCII_DOTTED -> ".";
            case CUSTOM -> String.valueOf(customChar);
        };
    }

    private String colorCode() {
        return switch (color) {
            case DEFAULT -> "";
            case RED -> Ansi.red();
            case GREEN -> Ansi.green();
            case YELLOW -> Ansi.yellow();
            case BLUE -> Ansi.blue();
            case MAGENTA -> Ansi.magenta();
            case CYAN -> Ansi.cyan();
            case WHITE -> Ansi.white();
            case GRAY -> Ansi.gray();
        };
    }

    /**
     * Returns the terminal width, or 80 if it cannot be determined.
     * Checks the {@code COLUMNS} environment variable first.
     *
     * @return the terminal width in characters
     * @since 1.0.0
     */
    public static int terminalWidth() {
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try {
                int w = Integer.parseInt(cols.trim());
                if (w > 0) return w;
            } catch (NumberFormatException ignored) {}
        }
        return 80;
    }

    // ============================================================== builder

    /**
     * Fluent builder for {@link Separator}. Every setter returns the
     * builder, so calls chain. Call {@link #build()} to get the
     * immutable separator.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private int width = DEFAULT_WIDTH;
        private Style style = Style.SOLID;
        private Align align = Align.FULL;
        private Color color = Color.DEFAULT;
        private String label = "";
        private int indent = 0;
        private char customChar = '-';

        // Width
        /**
         * Sets the line width in characters.
         *
         * @param width the width (clamped to ≥ 1 in the built separator)
         * @return this builder
         * @since 1.0.0
         */
        public Builder width(int width) { this.width = width; return this; }

        /**
         * Alias of {@link #width(int)} — see that method for full
         * documentation.
         *
         * @param width the width
         * @return this builder
         * @see #width(int)
         * @since 1.0.0
         */
        public Builder length(int width) { return width(width); }

        /**
         * Alias of {@link #width(int)} — see that method for full
         * documentation.
         *
         * @param width the width
         * @return this builder
         * @see #width(int)
         * @since 1.0.0
         */
        public Builder size(int width) { return width(width); }

        /**
         * Sets the width to match the current terminal.
         *
         * @return this builder
         * @see #terminalWidth()
         * @since 1.0.0
         */
        public Builder toTerminalWidth() { return width(terminalWidth()); }

        /**
         * Alias of {@link #toTerminalWidth()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #toTerminalWidth()
         * @since 1.0.0
         */
        public Builder fullWidth() { return toTerminalWidth(); }

        /**
         * Alias of {@link #toTerminalWidth()} — see that method for full
         * documentation.
         *
         * @return this builder
         * @see #toTerminalWidth()
         * @since 1.0.0
         */
        public Builder terminal() { return toTerminalWidth(); }

        // Style
        /**
         * Sets the line style.
         *
         * @param style the style (null → SOLID)
         * @return this builder
         * @since 1.0.0
         */
        public Builder style(Style style) { this.style = style; return this; }

        /**
         * Alias of {@link #style(Style)} — see that method for full
         * documentation.
         *
         * @param style the style
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder type(Style style) { return style(style); }

        /**
         * Alias of {@link #style(Style)} — see that method for full
         * documentation.
         *
         * @param style the style
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder using(Style style) { return style(style); }

        /**
         * Sets the style to SOLID.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder thin() { return style(Style.SOLID); }

        /**
         * Sets the style to DOUBLE.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder thick() { return style(Style.DOUBLE); }

        /**
         * Sets the style to DASHED.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder dashed() { return style(Style.DASHED); }

        /**
         * Sets the style to DOTTED.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder dotted() { return style(Style.DOTTED); }

        /**
         * Sets the style to STAR.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder stars() { return style(Style.STAR); }

        /**
         * Sets the style to TILDE.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder tildes() { return style(Style.TILDE); }

        /**
         * Sets the style to ARROW.
         *
         * @return this builder
         * @see #style(Style)
         * @since 1.0.0
         */
        public Builder arrows() { return style(Style.ARROW); }

        // Alignment
        /**
         * Sets the label alignment.
         *
         * @param align the alignment (null → FULL)
         * @return this builder
         * @since 1.0.0
         */
        public Builder align(Align align) { this.align = align; return this; }

        /**
         * Alias of {@link #align(Align)} — see that method for full
         * documentation.
         *
         * @param align the alignment
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder alignment(Align align) { return align(align); }

        /**
         * Alias of {@link #align(Align)} with {@code CENTER} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder centered() { return align(Align.CENTER); }

        /**
         * Alias of {@link #align(Align)} with {@code CENTER} — see that
         * method for full documentation. Uses the British spelling.
         *
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder centre() { return align(Align.CENTER); }

        /**
         * Alias of {@link #align(Align)} with {@code LEFT} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder left() { return align(Align.LEFT); }

        /**
         * Alias of {@link #align(Align)} with {@code RIGHT} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder right() { return align(Align.RIGHT); }

        /**
         * Alias of {@link #align(Align)} with {@code FULL} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #align(Align)
         * @since 1.0.0
         */
        public Builder fill() { return align(Align.FULL); }

        // Colour
        /**
         * Sets the line colour.
         *
         * @param color the colour (null → DEFAULT)
         * @return this builder
         * @since 1.0.0
         */
        public Builder color(Color color) { this.color = color; return this; }

        /**
         * Alias of {@link #color(Color)} using the British spelling
         * {@code colour} — see that method for full documentation.
         *
         * @param color the colour
         * @return this builder
         * @see #color(Color)
         * @since 1.0.0
         */
        public Builder colour(Color color) { return color(color); }

        /**
         * Alias of {@link #color(Color)} — see that method for full
         * documentation.
         *
         * @param color the colour
         * @return this builder
         * @see #color(Color)
         * @since 1.0.0
         */
        public Builder colored(Color color) { return color(color); }

        // Label
        /**
         * Sets the label to display within the line. An empty label
         * (the default) means a solid line with no text.
         *
         * @param label the label text (null → empty)
         * @return this builder
         * @since 1.0.0
         */
        public Builder label(String label) { this.label = label == null ? "" : label; return this; }

        /**
         * Alias of {@link #label(String)} — see that method for full
         * documentation.
         *
         * @param label the label
         * @return this builder
         * @see #label(String)
         * @since 1.0.0
         */
        public Builder text(String label) { return label(label); }

        /**
         * Alias of {@link #label(String)} — see that method for full
         * documentation.
         *
         * @param label the label
         * @return this builder
         * @see #label(String)
         * @since 1.0.0
         */
        public Builder title(String label) { return label(label); }

        /**
         * Alias of {@link #label(String)} — see that method for full
         * documentation.
         *
         * @param label the label
         * @return this builder
         * @see #label(String)
         * @since 1.0.0
         */
        public Builder content(String label) { return label(label); }

        /**
         * Alias of {@link #label(String)} — see that method for full
         * documentation.
         *
         * @param label the label
         * @return this builder
         * @see #label(String)
         * @since 1.0.0
         */
        public Builder withLabel(String label) { return label(label); }

        // Indent
        /**
         * Sets the number of leading spaces.
         *
         * @param indent the indent (negative → 0)
         * @return this builder
         * @since 1.0.0
         */
        public Builder indent(int indent) { this.indent = indent; return this; }

        /**
         * Alias of {@link #indent(int)} — see that method for full
         * documentation.
         *
         * @param indent the indent
         * @return this builder
         * @see #indent(int)
         * @since 1.0.0
         */
        public Builder offset(int indent) { return indent(indent); }

        // Custom char
        /**
         * Sets a custom fill character and switches the style to
         * {@code CUSTOM}.
         *
         * @param ch the fill character
         * @return this builder
         * @since 1.0.0
         */
        public Builder customChar(char ch) { this.customChar = ch; this.style = Style.CUSTOM; return this; }

        /**
         * Alias of {@link #customChar(char)} — see that method for full
         * documentation. Uses the underscore-suffixed name to avoid
         * the Java keyword {@code char}.
         *
         * @param ch the fill character
         * @return this builder
         * @see #customChar(char)
         * @since 1.0.0
         */
        public Builder char_(char ch) { return customChar(ch); }

        /**
         * Builds the immutable separator.
         *
         * @return the built separator
         * @since 1.0.0
         */
        public Separator build() { return new Separator(this); }
    }
}
