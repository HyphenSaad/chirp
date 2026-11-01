package chirp.ui;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;

/**
 * Clickable link that opens a URL in the system browser.
 *
 * <p>Wraps {@link javafx.scene.control.Hyperlink} and adds a
 * cross-platform browser launcher — {@link #to(String, String)} builds
 * a link that opens the URL on click, and {@link #browse(String)} is
 * the raw launcher that works from any context.</p>
 *
 * <h2>Two ways to create a link</h2>
 *
 * <h3>1. Factory — URL attached to the link</h3>
 * <pre>{@code
 * Hyperlink.to("Visit GitHub", "https://github.com/hyphensaad/chirp");
 * }</pre>
 *
 * <h3>2. Manual — callback controls the action</h3>
 * <pre>{@code
 * Hyperlink.of("Learn more").onClick(() -> browse("https://example.com"));
 * }</pre>
 *
 * <h2>Cross-platform browser launch</h2>
 * <p>{@link #browse(String)} first tries {@link Desktop#browse(URI)}
 * on any platform that supports it. If that is unavailable — a
 * headless server, a restricted JavaFX applet, or an exotic desktop
 * environment — it falls back to launching the platform's native
 * opener: {@code rundll32 url.dll,FileProtocolHandler} on Windows,
 * {@code open} on macOS, {@code xdg-open} on Linux.</p>
 *
 * <p>All launch paths are silent on failure. A broken browser launch
 * on a machine with no browser should not throw inside a UI handler —
 * the caller can still show their own message if they want.</p>
 *
 * <h2>Related</h2>
 * <p>For a styled label that behaves like a link but is really a
 * button, see {@link Button#link(String)}. For navigation inside the
 * app, see {@link Breadcrumb}.</p>
 *
 * @see Button#link(String)
 * @see Breadcrumb
 * @since 1.0.0
 */
public class Hyperlink extends javafx.scene.control.Hyperlink {

    private Hyperlink(String text) { super(text == null ? "" : text); }

    /**
     * Creates a hyperlink with the given text. Clicking it does nothing
     * until you attach a handler via {@link #onClick(Runnable)} or use
     * {@link #to(String, String)} instead.
     *
     * @param text the link text (null → "")
     * @return a new hyperlink
     * @see #to(String, String)
     * @since 1.0.0
     */
    public static Hyperlink of(String text) { return new Hyperlink(text); }

    /**
     * Creates a hyperlink that opens the given URL in the system
     * browser on click.
     *
     * @param text the link text (null → "")
     * @param url the URL to open (null → no-op on click)
     * @return a new hyperlink
     * @see #of(String)
     * @see #browse(String)
     * @since 1.0.0
     */
    public static Hyperlink to(String text, String url) {
        Hyperlink h = new Hyperlink(text);
        if (url != null) {
            h.setOnAction(e -> browse(url));
        }
        return h;
    }

    /**
     * Registers a click handler. Replaces any previous handler.
     *
     * @param r the click handler (null → no-op)
     * @return this hyperlink, for chaining
     * @since 1.0.0
     */
    public Hyperlink onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }

    /**
     * Opens the given URL in the system browser.
     *
     * <p>Silent on failure — a machine with no browser, an invalid URL,
     * or a restricted environment all result in no action, not an
     * exception.</p>
     *
     * @param url the URL to open (null or blank → no-op)
     * @since 1.0.0
     */
    public static void browse(String url) {
        if (url == null || url.isBlank()) return;
        try {
            URI uri = URI.create(url);
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                return;
            }
            // Fallback — OS-specific launchers
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder pb;
            if (os.contains("win")) pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            else if (os.contains("mac")) pb = new ProcessBuilder("open", url);
            else pb = new ProcessBuilder("xdg-open", url);
            pb.start();
        } catch (java.io.IOException | RuntimeException ignored) {
            // Silent — the caller can still show a message if needed.
        }
    }
}
