package chirp.ui;

import java.awt.Desktop;
import java.net.URI;

/**
 * Clickable link that opens URLs in the system browser.
 *
 * <p>Uses {@link java.awt.Desktop#browse(URI)} on all desktop platforms
 * (Windows/macOS/Linux). Silent fallback if the environment cannot open
 * a browser — no exception thrown, no UI break.</p>
 *
 * <pre>{@code
 * Hyperlink.to("Visit GitHub", "https://github.com/hyphensaad/chirp");
 * Hyperlink.of("Learn more").onClick(() -> Ui.browse("https://…"));
 * }</pre>
 */
public class Hyperlink extends javafx.scene.control.Hyperlink {

    private Hyperlink(String text) { super(text == null ? "" : text); }

    public static Hyperlink of(String text) { return new Hyperlink(text); }

    /** Creates a link that opens {@code url} in the browser on click. */
    public static Hyperlink to(String text, String url) {
        Hyperlink h = new Hyperlink(text);
        if (url != null) {
            h.setOnAction(e -> browse(url));
        }
        return h;
    }

    public Hyperlink onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }

    /** Opens the given URL in the system browser. Silent on failure. */
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
            String os = System.getProperty("os.name", "").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            else if (os.contains("mac")) pb = new ProcessBuilder("open", url);
            else pb = new ProcessBuilder("xdg-open", url);
            pb.start();
        } catch (Exception ignored) {
            // Silent — the caller can still show a message if needed.
        }
    }
}
