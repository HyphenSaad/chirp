package chirp.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Global styling for all Chirp UI windows — font size, family, and accent
 * colour. Applied live: changing a value re-styles every open window.
 *
 * <h2>Naming</h2>
 * <p>{@code set / apply / configure}; {@code fontSize / font / size};
 * {@code fontFamily / family / typeface}; {@code primaryColor / accent /
 * accentColor / primary}; {@code reset / clear / defaults}.</p>
 *
 * <h2>Key-value</h2>
 * <p>{@code UiStyle.apply(Key.FONT_SIZE, 16, Key.PRIMARY_COLOR, "#8b5cf6")}.</p>
 *
 * <h2>Presets</h2>
 * <p>{@code UiStyle.modernLight()}, {@code .modernDark()}, {@code .compact()},
 * {@code .largeText()} — ready-made styles.</p>
 */
public final class UiStyle {

    /** IDE-autocompletable keys for the key-value API. */
    public enum Key {
        FONT_SIZE,
        FONT_FAMILY,
        PRIMARY_COLOR,
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

    public static UiStyle defaults() {
        return new UiStyle(builder()
                .fontSize(14)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }
    public static UiStyle default_() { return defaults(); }
    public static UiStyle defaultStyle() { return defaults(); }

    // ============================================================== presets

    /** Larger fonts + purple accent — for readability on big screens. */
    public static UiStyle modernLight() {
        return new UiStyle(builder()
                .fontSize(15)
                .fontFamily("Segoe UI")
                .primaryColor("#8b5cf6"));
    }

    /** Dark-friendly palette with a brighter accent. */
    public static UiStyle modernDark() {
        return new UiStyle(builder()
                .fontSize(15)
                .fontFamily("Segoe UI")
                .primaryColor("#818cf8"));
    }

    /** Compact — smaller fonts and tighter accent. */
    public static UiStyle compact() {
        return new UiStyle(builder()
                .fontSize(13)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }

    /** Large text — 16px base, ideal for classroom projection. */
    public static UiStyle largeText() {
        return new UiStyle(builder()
                .fontSize(16)
                .fontFamily("Segoe UI")
                .primaryColor("#6366f1"));
    }

    /** Monospace — useful for code-heavy UIs. */
    public static UiStyle monospace() {
        return new UiStyle(builder()
                .fontSize(14)
                .fontFamily("Consolas")
                .primaryColor("#0d9488"));
    }

    // ============================================================== accessors

    public static UiStyle current() { return current; }

    public static void set(UiStyle style) {
        if (style == null) return;
        current = style;
        for (Scene s : openChirpScenes()) current.applyToScene(s);
    }
    public static void apply(UiStyle style) { set(style); }
    public static void configure(UiStyle style) { set(style); }

    /** Convenience — pass alternating {@link Key} → value pairs. */
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
    public static void configure(Object... kv) { apply(kv); }

    /** Resets to {@link #defaults()}. */
    public static void reset() { set(defaults()); }
    public static void clear() { reset(); }

    public static Builder builder() { return new Builder(); }

    private static Builder builderFrom(UiStyle s) {
        return new Builder()
                .fontSize(s.fontSize)
                .fontFamily(s.fontFamily)
                .primaryColor(s.primaryColor);
    }

    // ============================================================== values

    public double fontSize() { return fontSize; }
    public double font() { return fontSize; }
    public double size() { return fontSize; }
    public String fontFamily() { return fontFamily; }
    public String family() { return fontFamily; }
    public String typeface() { return fontFamily; }
    public String primaryColor() { return primaryColor; }
    public String accent() { return primaryColor; }
    public String accentColor() { return primaryColor; }
    public String primary() { return primaryColor; }

    // ============================================================== apply

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

    public static final class Builder {
        private double fontSize = 14;
        private String fontFamily = "Segoe UI";
        private String primaryColor = "#6366f1";

        public Builder fontSize(double px) { this.fontSize = Math.max(8, Math.min(48, px)); return this; }
        public Builder font(double px) { return fontSize(px); }
        public Builder size(double px) { return fontSize(px); }

        public Builder fontFamily(String family) {
            if (family != null && !family.isBlank()) this.fontFamily = family;
            return this;
        }
        public Builder family(String f) { return fontFamily(f); }
        public Builder typeface(String f) { return fontFamily(f); }

        public Builder primaryColor(String color) {
            if (color != null && !color.isBlank()) this.primaryColor = color;
            return this;
        }
        public Builder accent(String c) { return primaryColor(c); }
        public Builder accentColor(String c) { return primaryColor(c); }
        public Builder primary(String c) { return primaryColor(c); }

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

        public UiStyle build() { return new UiStyle(this); }
    }
}
