package chirp.startup;

import chirp.internal.Ansi;
import chirp.internal.Glyphs;

/**
 * Startup header — box-bounded TUI-style banner with version and link.
 *
 * <p>Every visible line is exactly the same width, and the total width is
 * computed from the longest line so the box never breaks. Uses
 * {@link Glyphs} for box-drawing characters so the same banner renders in
 * unicode-fancy, unicode-basic, and plain-ASCII modes without breaking
 * alignment.</p>
 */
public final class Banner {

    public static final String VERSION = "1.0.0";
    public static final String GITHUB = "github.com/hyphensaad/chirp";
    public static final String AUTHOR = "Saad Mansoor (HyphenSaad)";

    private static final String SEPARATOR = " | ";

    private Banner() {}

    public static void print(StartupConfig config) {
        if (config == null || config.clearScreen()) {
            try {
                chirp.console.Console.clearScreenInternal();
            } catch (Exception ignored) {}
        }
        System.out.print(render(config));
        System.out.flush();
    }

    public static String render(StartupConfig config) {
        String subtitle = (config != null
                && config.headerText() != null
                && !config.headerText().isBlank())
                        ? config.headerText()
                        : "A friendly Java toolkit";

        String title = "C H I R P";
        String versionLine = "v" + VERSION + SEPARATOR + GITHUB;

        // Inner width — computed from longest content line, min 44 chars
        int inner = 44;
        inner = Math.max(inner, title.length() + 6);
        inner = Math.max(inner, subtitle.length() + 6);
        inner = Math.max(inner, versionLine.length() + 6);
        inner = Math.max(inner, AUTHOR.length() + 6);

        String cyan = Ansi.cyan();
        String bold = Ansi.bold();
        String gray = Ansi.gray();
        String dim = Ansi.dim();
        String reset = Ansi.reset();

        String top = Glyphs.topLeft() + Glyphs.hLine(inner) + Glyphs.topRight();
        String bottom = Glyphs.bottomLeft() + Glyphs.hLine(inner) + Glyphs.bottomRight();
        String v = Glyphs.vertical();

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("  ").append(cyan).append(top).append(reset).append('\n');

        sb.append(emptyRow(v, inner, cyan, reset));

        sb.append(row(v, title, bold + title + reset, inner, cyan, reset));

        sb.append(row(v, subtitle, subtitle, inner, cyan, reset));

        sb.append(emptyRow(v, inner, cyan, reset));

        sb.append(row(v, versionLine, gray + versionLine + reset, inner, cyan, reset));

        sb.append(row(v, AUTHOR, dim + AUTHOR + reset, inner, cyan, reset));

        sb.append(emptyRow(v, inner, cyan, reset));

        sb.append("  ").append(cyan).append(bottom).append(reset).append('\n');
        sb.append('\n');
        return sb.toString();
    }

    // ---------------------------------------------------------------- rows

    private static String row(String v, String plain, String styled,
            int inner, String cyan, String reset) {
        // Row layout: "  " + v + "  " + content + pad + "  " + v
        // Visible-after-indent width = 1 + 2 + content + pad + 2 + 1 = inner + 2
        int pad = Math.max(0, inner - plain.length() - 4);
        return "  " + cyan + v + reset
                + "  " + styled + " ".repeat(pad) + "  "
                + cyan + v + reset + "\n";
    }

    private static String emptyRow(String v, int inner, String cyan, String reset) {
        return row(v, "", "", inner, cyan, reset);
    }
}
