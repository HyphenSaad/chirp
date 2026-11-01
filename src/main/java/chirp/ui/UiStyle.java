package chirp.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Global styling for every Chirp window — font size, font family, and
 * accent colour.
 *
 * <p>A singleton. Changing a value re-styles every <b>currently-open</b>
 * Chirp window in place; windows opened later use the current values.
 * This is how a preferences dialog can change the entire application's
 * look without rebuilding any scene.</p>
 *
 * <h2>What can be configured</h2>
 * <table border="1">
 *   <caption>UiStyle configuration</caption>
 *   <tr><th>Setting</th><th>Default</th><th>Range</th></tr>
 *   <tr><td>Font size</td><td>14px</td><td>clamped to 8–48px</td></tr>
 *   <tr><td>Font family</td><td>"Segoe UI"</td>
 *       <td>any font available on the system</td></tr>
 *   <tr><td>Primary colour</td><td>#6366f1 (indigo)</td>
 *       <td>any hex colour</td></tr>
 * </table>
 *
 * <h2>Six presets</h2>
 * <ul>
 *   <li>{@link #defaults()} — the baseline (14px Segoe UI indigo)</li>
 *   <li>{@link #modernLight()} — 15px purple accent</li>
 *   <li>{@link #modernDark()} — 15px brighter accent</li>
 *   <li>{@link #compact()} — 13px indigo</li>
 *   <li>{@link #largeText()} — 16px indigo (classroom projection)</li>
 *   <li>{@link #monospace()} — 14px Consolas teal (code-heavy UI)</li>
 * </ul>
 *
 * <h2>Two ways to apply</h2>
 *
 * <h3>1. Preset</h3>
 * <pre>{@code
 * UiStyle.set(UiStyle.modernDark());
 * }</pre>
 *
 * <h3>2. Key-value pairs</h3>
 * <pre>{@code
 * UiStyle.apply(
 *     UiStyle.Key.FONT_SIZE, 16,
 *     UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");
 * }</pre>
 *
 * <h2>Live re-styling</h2>
 * <p>Applying a style immediately updates every open Chirp window. A
 * simple preferences dialog that lets the user pick a larger font is
 * one line:</p>
 * <pre>{@code
 * sizeSlider.onChange(v -> UiStyle.apply(UiStyle.Key.FONT_SIZE, v));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the theme (light / dark / auto), see {@link UiTheme}. For the
 * window configuration object, see {@link UiApp}. For the facade
 * method, see {@link Ui#style(Object...)}.</p>
 *
 * @see UiTheme
 * @see UiApp
 * @see Ui#style(Object...)
 * @since 1.0.0
 */
public final class UiStyle {

    /**
     * Keys for the key-value API — use these with {@link #apply(Object...)}.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** The font size in pixels — {@link Number}. */
        FONT_SIZE,
        /** The font family name — {@link String}. */
        FONT_FAMILY,
        /** The primary accent colour as a hex string — {@link String}. */
        PRIMARY_COLOR,
        /** Reset to the default style — {@link Boolean}. */
        RESET
    }

    private static volatile UiStyle current = defaults();

    private final double fontSize;
    private final String fontFamily;
    private final String primaryColor;

    private UiStyle(Builder b) {
        this.fontSize = b.fontSize;
        this.fontFamily = b.fontFamily;
        this.primaryColor = b.primaryColor;
    }

    // ============================================================== defaults

    /**
     * The default style — 14px Segoe UI, indigo accent.
     *
     * @return the default style
     * @see #modernLight()
     * @see #modernDark()
     * @see #compact()
     * @see #largeText()
     * @see #monospace()
     * @since 1.0.0
     */
    public static UiStyle defaults() {
        return new UiStyle(builder()
                .fontSize(14)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }

    /**
     * Alias of {@link #defaults()} — see that method for full
     * documentation.
     *
     * @return the default style
     * @see #defaults()
     * @since 1.0.0
     */
    public static UiStyle default_() { return defaults(); }

    /**
     * Alias of {@link #defaults()} — see that method for full
     * documentation.
     *
     * @return the default style
     * @see #defaults()
     * @since 1.0.0
     */
    public static UiStyle defaultStyle() { return defaults(); }

    // ============================================================== presets

    /**
     * Preset — 15px Segoe UI, purple accent, brighter than the default.
     * Good for daytime use on modern displays.
     *
     * @return the preset style
     * @see #defaults()
     * @see #modernDark()
     * @since 1.0.0
     */
    public static UiStyle modernLight() {
        return new UiStyle(builder()
                .fontSize(15)
                .fontFamily("Segoe UI")
                .primaryColor("#8b5cf6"));
    }

    /**
     * Preset — 15px Segoe UI, bright indigo accent. Designed to pair
     * with {@link UiTheme#DARK}.
     *
     * @return the preset style
     * @see #modernLight()
     * @see UiTheme#DARK
     * @since 1.0.0
     */
    public static UiStyle modernDark() {
        return new UiStyle(builder()
                .fontSize(15)
                .fontFamily("Segoe UI")
                .primaryColor("#818cf8"));
    }

    /**
     * Preset — 13px Segoe UI, indigo accent. Good for data-dense
     * dashboards on smaller screens.
     *
     * @return the preset style
     * @see #defaults()
     * @see #largeText()
     * @since 1.0.0
     */
    public static UiStyle compact() {
        return new UiStyle(builder()
                .fontSize(13)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }

    /**
     * Preset — 16px Segoe UI, indigo accent. The size for classroom
     * projection and accessibility.
     *
     * @return the preset style
     * @see #compact()
     * @since 1.0.0
     */
    public static UiStyle largeText() {
        return new UiStyle(builder()
                .fontSize(16)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }

    /**
     * Preset — 14px Consolas, teal accent. For code-heavy UI where a
     * monospace base font is appropriate.
     *
     * @return the preset style
     * @see #defaults()
     * @since 1.0.0
     */
    public static UiStyle monospace() {
        return new UiStyle(builder()
                .fontSize(14)
                .fontFamily("Consolas")
                .primaryColor("#0d9488"));
    }

    // ============================================================== accessors

    /**
     * Returns the currently-active style. Windows opened from now on
     * will use it; existing windows have already been re-styled by the
     * previous {@code apply()} call.
     *
     * @return the current style (never null)
     * @see #set(UiStyle)
     * @since 1.0.0
     */
    public static UiStyle current() { return current; }

    /**
     * Applies the given style, re-styling every open Chirp window in
     * place.
     *
     * <p>Calling this is how a "Preferences → Font Size" slider changes
     * the running application's look in real time.</p>
     *
     * @param style the style to apply (null → no-op)
     * @see #apply(Object...)
     * @see #current()
     * @since 1.0.0
     */
    public static void set(UiStyle style) {
        if (style == null) return;
        current = style;
        for (Scene s : openChirpScenes()) current.applyToScene(s);
    }

    /**
     * Alias of {@link #set(UiStyle)} — see that method for full
     * documentation.
     *
     * @param style the style to apply
     * @see #set(UiStyle)
     * @since 1.0.0
     */
    public static void apply(UiStyle style) { set(style); }

    /**
     * Alias of {@link #set(UiStyle)} — see that method for full
     * documentation.
     *
     * @param style the style to apply
     * @see #set(UiStyle)
     * @since 1.0.0
     */
    public static void configure(UiStyle style) { set(style); }

    /**
     * Applies a style built from alternating {@link Key} → value pairs.
     * Only the keys that are set are changed; other aspects of the
     * current style are preserved.
     *
     * <pre>{@code
     * UiStyle.apply(
     *     UiStyle.Key.FONT_SIZE, 16,
     *     UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");
     * }</pre>
     *
     * @param keyValues alternating Key → value pairs
     * @see #apply(UiStyle)
     * @see #reset()
     * @since 1.0.0
     */
    public static void apply(Object... keyValues) {
        if (keyValues == null) return;
        Builder b = builderFrom(current);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (!(key instanceof Key k)) continue;
            if (k == Key.RESET && Boolean.TRUE.equals(value)) {
                set(defaults());
                return;
            }
            b.set(k, value);
        }
        set(b.build());
    }

    /**
     * Alias of {@link #apply(Object...)} — see that method for full
     * documentation.
     *
     * @param kv alternating Key → value pairs
     * @see #apply(Object...)
     * @since 1.0.0
     */
    public static void configure(Object... kv) { apply(kv); }

    /**
     * Resets to {@link #defaults()} — restores the baseline font size,
     * family, and accent colour, and re-styles open windows.
     *
     * @see #defaults()
     * @see #clear()
     * @since 1.0.0
     */
    public static void reset() { set(defaults()); }

    /**
     * Alias of {@link #reset()} — see that method for full
     * documentation.
     *
     * @see #reset()
     * @since 1.0.0
     */
    public static void clear() { reset(); }

    /**
     * Returns a fresh builder for constructing a custom style.
     *
     * @return a new builder
     * @see #defaults()
     * @see #set(UiStyle)
     * @since 1.0.0
     */
    public static Builder builder() { return new Builder(); }

    private static Builder builderFrom(UiStyle s) {
        return new Builder()
                .fontSize(s.fontSize)
                .fontFamily(s.fontFamily)
                .primaryColor(s.primaryColor);
    }

    // ============================================================== values

    /**
     * Returns the font size in pixels.
     *
     * @return the font size
     * @see #font()
     * @see #size()
     * @since 1.0.0
     */
    public double fontSize() { return fontSize; }

    /**
     * Alias of {@link #fontSize()} — see that method for full
     * documentation.
     *
     * @return the font size
     * @see #fontSize()
     * @since 1.0.0
     */
    public double font() { return fontSize; }

    /**
     * Alias of {@link #fontSize()} — see that method for full
     * documentation.
     *
     * @return the font size
     * @see #fontSize()
     * @since 1.0.0
     */
    public double size() { return fontSize; }

    /**
     * Returns the font family name.
     *
     * @return the font family
     * @see #family()
     * @see #typeface()
     * @since 1.0.0
     */
    public String fontFamily() { return fontFamily; }

    /**
     * Alias of {@link #fontFamily()} — see that method for full
     * documentation.
     *
     * @return the font family
     * @see #fontFamily()
     * @since 1.0.0
     */
    public String family() { return fontFamily; }

    /**
     * Alias of {@link #fontFamily()} — see that method for full
     * documentation.
     *
     * @return the font family
     * @see #fontFamily()
     * @since 1.0.0
     */
    public String typeface() { return fontFamily; }

    /**
     * Returns the primary accent colour as a hex string.
     *
     * @return the accent colour
     * @see #accent()
     * @see #accentColor()
     * @see #primary()
     * @since 1.0.0
     */
    public String primaryColor() { return primaryColor; }

    /**
     * Alias of {@link #primaryColor()} — see that method for full
     * documentation.
     *
     * @return the accent colour
     * @see #primaryColor()
     * @since 1.0.0
     */
    public String accent() { return primaryColor; }

    /**
     * Alias of {@link #primaryColor()} — see that method for full
     * documentation.
     *
     * @return the accent colour
     * @see #primaryColor()
     * @since 1.0.0
     */
    public String accentColor() { return primaryColor; }

    /**
     * Alias of {@link #primaryColor()} — see that method for full
     * documentation.
     *
     * @return the accent colour
     * @see #primaryColor()
     * @since 1.0.0
     */
    public String primary() { return primaryColor; }

    // ============================================================== apply

    /**
     * Applies this style to a specific scene. Public so custom scenes
     * (not opened through {@link Ui#show}) can opt in.
     *
     * @param scene the scene to style (null → no-op)
     * @since 1.0.0
     */
    public void applyToScene(Scene scene) {
        if (scene == null) return;
        Parent root = scene.getRoot();
        if (root == null) return;

        StringBuilder css = new StringBuilder();
        if (fontSize > 0) css.append("-fx-font-size: ").append(fontSize).append("px;");
        if (fontFamily != null && !fontFamily.isBlank())
            css.append("-fx-font-family: \"").append(fontFamily).append("\";");
        if (primaryColor != null && !primaryColor.isBlank()) {
            css.append("-fx-accent: ").append(primaryColor).append(';');
            css.append("-fx-focus-color: ").append(primaryColor).append(';');
            css.append("-fx-faint-focus-color: ").append(primaryColor).append("22;");
        }
        root.setStyle(css.toString());
    }

    // ============================================================== window tracking

    private static List<Scene> openChirpScenes() {
        List<Scene> scenes = new ArrayList<>();
        for (Window w : Window.getWindows()) {
            if (w == null) continue;
            Scene s = w.getScene();
            if (s == null) continue;
            if (Boolean.TRUE.equals(s.getProperties().get("chirp.ui"))) scenes.add(s);
        }
        return scenes;
    }

    // ============================================================== builder

    /**
     * Fluent builder for {@link UiStyle}. Every setter returns the
     * builder, so calls chain. Call {@link #build()} to get the
     * immutable style.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private double fontSize = 14;
        private String fontFamily = "Segoe UI";
        private String primaryColor = "#6366f1";

        /**
         * Sets the font size in pixels, clamped to {@code [8, 48]}.
         *
         * @param px the font size in pixels
         * @return this builder
         * @see #font(double)
         * @see #size(double)
         * @since 1.0.0
         */
        public Builder fontSize(double px) { this.fontSize = Math.max(8, Math.min(48, px)); return this; }

        /**
         * Alias of {@link #fontSize(double)} — see that method for full
         * documentation.
         *
         * @param px the font size
         * @return this builder
         * @see #fontSize(double)
         * @since 1.0.0
         */
        public Builder font(double px) { return fontSize(px); }

        /**
         * Alias of {@link #fontSize(double)} — see that method for full
         * documentation.
         *
         * @param px the font size
         * @return this builder
         * @see #fontSize(double)
         * @since 1.0.0
         */
        public Builder size(double px) { return fontSize(px); }

        /**
         * Sets the font family name. Use a family name that JavaFX
         * recognises on the target system — Segoe UI on Windows,
         * Helvetica on macOS, DejaVu Sans on Linux. Null or blank
         * values are ignored.
         *
         * @param family the font family name
         * @return this builder
         * @see #family(String)
         * @see #typeface(String)
         * @since 1.0.0
         */
        public Builder fontFamily(String family) {
            if (family != null && !family.isBlank()) this.fontFamily = family;
            return this;
        }

        /**
         * Alias of {@link #fontFamily(String)} — see that method for
         * full documentation.
         *
         * @param f the font family
         * @return this builder
         * @see #fontFamily(String)
         * @since 1.0.0
         */
        public Builder family(String f) { return fontFamily(f); }

        /**
         * Alias of {@link #fontFamily(String)} — see that method for
         * full documentation.
         *
         * @param f the font family
         * @return this builder
         * @see #fontFamily(String)
         * @since 1.0.0
         */
        public Builder typeface(String f) { return fontFamily(f); }

        /**
         * Sets the primary accent colour as a hex string (e.g.
         * {@code "#6366f1"}). Null or blank values are ignored.
         *
         * @param color the hex colour
         * @return this builder
         * @see #accent(String)
         * @see #accentColor(String)
         * @see #primary(String)
         * @since 1.0.0
         */
        public Builder primaryColor(String color) {
            if (color != null && !color.isBlank()) this.primaryColor = color;
            return this;
        }

        /**
         * Alias of {@link #primaryColor(String)} — see that method for
         * full documentation.
         *
         * @param c the hex colour
         * @return this builder
         * @see #primaryColor(String)
         * @since 1.0.0
         */
        public Builder accent(String c) { return primaryColor(c); }

        /**
         * Alias of {@link #primaryColor(String)} — see that method for
         * full documentation.
         *
         * @param c the hex colour
         * @return this builder
         * @see #primaryColor(String)
         * @since 1.0.0
         */
        public Builder accentColor(String c) { return primaryColor(c); }

        /**
         * Alias of {@link #primaryColor(String)} — see that method for
         * full documentation.
         *
         * @param c the hex colour
         * @return this builder
         * @see #primaryColor(String)
         * @since 1.0.0
         */
        public Builder primary(String c) { return primaryColor(c); }

        /**
         * Configures a single aspect via the {@link Key} enum.
         *
         * @param key the key (null → no-op)
         * @param value the value (null → no-op)
         * @return this builder
         * @since 1.0.0
         */
        public Builder set(Key key, Object value) {
            if (key == null || value == null) return this;
            switch (key) {
                case FONT_SIZE -> {
                    if (value instanceof Number n) fontSize(n.doubleValue());
                }
                case FONT_FAMILY -> {
                    if (value instanceof String s) fontFamily(s);
                }
                case PRIMARY_COLOR -> {
                    if (value instanceof String s) primaryColor(s);
                }
                case RESET -> {
                    if (Boolean.TRUE.equals(value)) {
                        fontSize = 14;
                        fontFamily = "Segoe UI";
                        primaryColor = "#6366f1";
                    }
                }
            }
            return this;
        }

        /**
         * Builds the immutable style.
         *
         * @return the built style
         * @since 1.0.0
         */
        public UiStyle build() { return new UiStyle(this); }
    }
}
