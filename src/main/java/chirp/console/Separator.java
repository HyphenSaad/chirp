package chirp.console;

import chirp.internal.Ansi;
import chirp.internal.Capabilities;

import java.util.Map;

/**
 * Configurable decorative separator.
 *
 * <p>Every option is a named enum value so IDE autocomplete guides the user.
 * Combine any options with method chaining or key-value pairs.</p>
 *
 * <pre>{@code
 * Console.line(60);                             // quick
 * Console.line(Separator.Style.DOUBLE);
 * Console.line(60, Separator.Style.DOUBLE);
 * Console.line(Separator.builder().width(60).doubleLine().build());
 * Console.line(Separator.from(Separator.Key.WIDTH, 60, Separator.Key.STYLE, Separator.Style.DASHED));
 * }</pre>
 */
public final class Separator {

    // ============================================================== enums

    public enum Style {
        SOLID, DOUBLE, DASHED, DOTTED, STAR, EQUALS, TILDE, ARROW,
        ASCII_SOLID, ASCII_DASHED, ASCII_DOTTED, CUSTOM
    }
    public enum Align { LEFT, CENTER, RIGHT, FULL }
    public enum Color { DEFAULT, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, GRAY }
    public enum Key { WIDTH, STYLE, ALIGN, COLOR, LABEL, INDENT, CUSTOM_CHAR }

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

    public static Builder builder() { return new Builder(); }
    public static Builder of() { return new Builder(); }
    public static Builder create() { return new Builder(); }

    public static Separator width(int w) { return builder().width(w).build(); }
    public static Separator line() { return builder().build(); }
    public static Separator solid() { return builder().style(Style.SOLID).build(); }
    public static Separator doubleLine() { return builder().style(Style.DOUBLE).build(); }
    public static Separator dashedLine() { return builder().style(Style.DASHED).build(); }
    public static Separator dottedLine() { return builder().style(Style.DOTTED).build(); }
    public static Separator starLine() { return builder().style(Style.STAR).build(); }

    // ============================================================== key-value

    public static Separator from(Object... keyValuePairs) {
        Builder b = builder();
        if (keyValuePairs == null) return b.build();
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            if (key == null) continue;
            String name = key instanceof Key k ? k.name() : String.valueOf(key).toUpperCase();
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

    public int width() { return width; }
    public Style style() { return style; }
    public Align align() { return align; }
    public Color color() { return color; }
    public String label() { return label; }
    public int indent() { return indent; }

    // ============================================================== render

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

    public void print() {
        chirp.startup.ChirpLifecycle.requireStarted();
        System.out.println(render());
    }
    public void println() { print(); }
    public void display() { print(); }

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

    public static final class Builder {
        private int width = DEFAULT_WIDTH;
        private Style style = Style.SOLID;
        private Align align = Align.FULL;
        private Color color = Color.DEFAULT;
        private String label = "";
        private int indent = 0;
        private char customChar = '-';

        // Width
        public Builder width(int width) { this.width = width; return this; }
        public Builder length(int width) { return width(width); }
        public Builder size(int width) { return width(width); }
        public Builder toTerminalWidth() { return width(terminalWidth()); }
        public Builder fullWidth() { return toTerminalWidth(); }
        public Builder terminal() { return toTerminalWidth(); }

        // Style
        public Builder style(Style style) { this.style = style; return this; }
        public Builder type(Style style) { return style(style); }
        public Builder using(Style style) { return style(style); }
        public Builder thin() { return style(Style.SOLID); }
        public Builder thick() { return style(Style.DOUBLE); }
        public Builder dashed() { return style(Style.DASHED); }
        public Builder dotted() { return style(Style.DOTTED); }
        public Builder stars() { return style(Style.STAR); }
        public Builder tildes() { return style(Style.TILDE); }
        public Builder arrows() { return style(Style.ARROW); }

        // Alignment
        public Builder align(Align align) { this.align = align; return this; }
        public Builder alignment(Align align) { return align(align); }
        public Builder centered() { return align(Align.CENTER); }
        public Builder centre() { return align(Align.CENTER); }
        public Builder left() { return align(Align.LEFT); }
        public Builder right() { return align(Align.RIGHT); }
        public Builder fill() { return align(Align.FULL); }

        // Colour
        public Builder color(Color color) { this.color = color; return this; }
        public Builder colour(Color color) { return color(color); }
        public Builder colored(Color color) { return color(color); }

        // Label
        public Builder label(String label) { this.label = label == null ? "" : label; return this; }
        public Builder text(String label) { return label(label); }
        public Builder title(String label) { return label(label); }
        public Builder content(String label) { return label(label); }
        public Builder withLabel(String label) { return label(label); }

        // Indent
        public Builder indent(int indent) { this.indent = indent; return this; }
        public Builder offset(int indent) { return indent(indent); }

        // Custom char
        public Builder customChar(char ch) { this.customChar = ch; this.style = Style.CUSTOM; return this; }
        public Builder char_(char ch) { return customChar(ch); }

        public Separator build() { return new Separator(this); }
    }
}
