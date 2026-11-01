package chirp.ui;

/**
 * The three window themes — {@code LIGHT}, {@code DARK}, and
 * {@code AUTO}.
 *
 * <p>Every window is styled by one of these. {@code LIGHT} and
 * {@code DARK} load a specific CSS file; {@code AUTO} picks one based
 * on the operating system's current theme where possible, and falls
 * back to {@code LIGHT} otherwise.</p>
 *
 * <h2>{@code AUTO} resolution</h2>
 * <p>{@code AUTO} resolves to {@code DARK} if the JVM was launched
 * with {@code -Dchirp.theme=dark}, to {@code LIGHT} if launched with
 * {@code -Dchirp.theme=light}, and to {@code LIGHT} otherwise. A real
 * OS-theme query is not available portably from JavaFX, so the
 * fallback is LIGHT rather than guessing.</p>
 *
 * <h2>How a window picks its theme</h2>
 * <ul>
 *   <li>{@link Ui#show(String, javafx.scene.Parent)} — uses
 *       {@code AUTO}</li>
 *   <li>{@link Ui#show(UiApp)} — uses {@link UiApp#theme()}, which is
 *       {@code AUTO} unless the builder set it</li>
 *   <li>{@link UiApp.Builder#dark()} / {@code .light()} / {@code .auto()}
 *       — set it explicitly</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For the styling system (fonts, accent colours), see
 * {@link UiStyle}. For the window configuration object, see
 * {@link UiApp}.</p>
 *
 * @see UiStyle
 * @see UiApp
 * @see UiApp.Builder#dark()
 * @see UiApp.Builder#light()
 * @since 1.0.0
 */
public enum UiTheme {
    /** Light theme. */
    LIGHT,

    /** Dark theme. */
    DARK,

    /** Automatic — resolves via {@code -Dchirp.theme} property or falls back to LIGHT. */
    AUTO;

    /**
     * Resolves this theme to a concrete one — {@code AUTO} becomes
     * {@code DARK} or {@code LIGHT}, the others return themselves.
     *
     * @return the resolved theme
     * @since 1.0.0
     */
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
