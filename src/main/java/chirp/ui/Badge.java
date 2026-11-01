package chirp.ui;

/**
 * Small pill-shaped status indicator.
 *
 * <pre>{@code
 * Badge.success("Active");
 * Badge.danger("Failed");
 * Badge.outline("Draft");
 * }</pre>
 */
public class Badge extends javafx.scene.control.Label {

    private Badge(String text) {
        super(text == null ? "" : text);
        getStyleClass().add("badge");
    }

    public static Badge of(String text) { return new Badge(text); }
    public static Badge primary(String text) { return new Badge(text).variant("primary"); }
    public static Badge success(String text) { return new Badge(text).variant("success"); }
    public static Badge warning(String text) { return new Badge(text).variant("warning"); }
    public static Badge danger(String text) { return new Badge(text).variant("danger"); }
    public static Badge info(String text) { return new Badge(text).variant("info"); }
    public static Badge outline(String text) { return new Badge(text).variant("outline"); }

    public Badge variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }
}
