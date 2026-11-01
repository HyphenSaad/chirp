package chirp.startup;

import chirp.internal.Ansi;
import chirp.internal.Glyphs;

/**
 * The startup banner — a box-bounded TUI-style header printed by
 * {@link ChirpLifecycle#start(StartupConfig)}.
 *
 * <p>Renders the library name, version, link, and author in a Unicode
 * box that automatically adapts to the terminal's Unicode support.
 * Every visible line is exactly the same width, and the total width is
 * computed from the longest content line so the box never breaks, even
 * when a custom header text is supplied.</p>
 *
 * <h2>Example — the default banner</h2>
 * <pre>
 * ╭────────────────────────────────────────────╮
 * │                                            │
 * │             C H I R P                      │
 * │                                            │
 * │         A friendly Java toolkit            │
 * │                                            │
 * │   v1.0.0 | github.com/hyphensaad/chirp     │
 * │   Saad Mansoor (HyphenSaad)                │
 * │                                            │
 * ╰────────────────────────────────────────────╯
 * </pre>
 *
 * <p>(Rendered with ASCII fallback in a non-Unicode terminal — same
 * layout, different glyphs.)</p>
 *
 * <h2>Customisation</h2>
 * <p>The banner respects three configuration fields from
 * {@link StartupConfig}:</p>
 * <ul>
 *   <li>{@link StartupConfig#showHeader()} — print the banner at all</li>
 *   <li>{@link StartupConfig#headerText()} — replace the default
 *       "A friendly Java toolkit" subtitle</li>
 *   <li>{@link StartupConfig#unicodePreferred()} — use Unicode box
 *       glyphs vs ASCII</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For the full startup sequence, see
 * {@link ChirpLifecycle#start(StartupConfig)}. For the configuration
 * object, see {@link StartupConfig}.</p>
 *
 * @see StartupConfig
 * @see ChirpLifecycle#start(StartupConfig)
 * @since 1.0.0
 */
public final class Banner {

    /** The library version. */
    public static final String VERSION = "1.0.0";

    /** The GitHub repository URL. */
    public static final String GITHUB = "github.com/hyphensaad/chirp";

    /** The author attribution. */
    public static final String AUTHOR = "Saad Mansoor (HyphenSaad)";

    private static final String SEPARATOR = " | ";

    private Banner() {}

    /**
     * Clears the screen (if configured) and prints the rendered banner
     * to {@code System.out}. Called by
     * {@link ChirpLifecycle#start(StartupConfig)} — students never
     * invoke this directly.
     *
     * @param config the startup configuration (null → clear screen)
     * @see #render(StartupConfig)
     * @since 1.0.0
     */
    public static void print(StartupConfig config) {
        if (config == null || config.clearScreen()) {
            try {
                chirp.console.Console.clearScreenInternal();
            } catch (RuntimeException ignored) {}
        }
        System.out.print(render(config));
        System.out.flush();
    }

    /**
     * Renders the banner as a string without printing it. Useful for
     * tests and for embedding the banner in custom output.
     *
     * <p>The returned string starts and ends with a blank line so that
     * it composes cleanly with surrounding output.</p>
     *
     * @param config the startup configuration (null → {@code "Welcome!"} subtitle)
     * @return the rendered banner
     * @see #print(StartupConfig)
     * @since 1.0.0
     */
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
