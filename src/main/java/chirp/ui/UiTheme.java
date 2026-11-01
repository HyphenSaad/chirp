package chirp.ui;

/**
 * Theme options for JavaFX windows. {@code AUTO} detects light/dark from
 * the OS at startup (best-effort — JavaFX has no direct OS query API, so we
 * fall back to LIGHT unless {@code -Dchirp.theme=dark} is set).
 */
public enum UiTheme {
    LIGHT, DARK, AUTO;

    /** Resolves AUTO to a concrete theme. */
    public UiTheme resolve() {
        if (this != AUTO) return this;
        String forced = System.getProperty("chirp.theme", "");
        if ("dark".equalsIgnoreCase(forced)) return DARK;
        if ("light".equalsIgnoreCase(forced)) return LIGHT;
        return LIGHT;   // JavaFX has no reliable cross-platform OS theme query
    }

    /** Resolves this theme to a classpath URL string for the CSS stylesheet. */
    static String stylesheet(UiTheme theme) {
        UiTheme resolved = theme == null ? LIGHT : theme.resolve();
        String file = resolved == DARK ? "/chirp/ui/theme-dark.css" : "/chirp/ui/theme-light.css";
        var url = Ui.class.getResource(file);
        if (url == null) {
            throw new IllegalStateException("Missing theme stylesheet: " + file);
        }
        return url.toExternalForm();
    }
}
