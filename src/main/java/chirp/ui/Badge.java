package chirp.ui;

/**
 * Small pill-shaped status indicator — a one-word label with a coloured
 * background.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Badge.success("Active");
 * Badge.danger("Failed");
 * Badge.outline("Draft");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a removable tag, see {@link Chip}. For plain text, see
 * {@link Label}.</p>
 *
 * @see Chip
 * @see Label
 * @since 1.0.0
 */
public class Badge extends javafx.scene.control.Label {

    private Badge(String text) {
        super(text == null ? "" : text);
        getStyleClass().add("badge");
    }

    /**
     * Creates a badge with the given text (default styling).
     *
     * @param text the badge text
     * @return a new badge
     * @since 1.0.0
     */
    public static Badge of(String text) { return new Badge(text); }

    /**
     * Creates a primary (accent-coloured) badge.
     *
     * @param text the badge text
     * @return a new primary badge
     * @since 1.0.0
     */
    public static Badge primary(String text) { return new Badge(text).variant("primary"); }

    /**
     * Creates a success (green) badge.
     *
     * @param text the badge text
     * @return a new success badge
     * @since 1.0.0
     */
    public static Badge success(String text) { return new Badge(text).variant("success"); }

    /**
     * Creates a warning (yellow) badge.
     *
     * @param text the badge text
     * @return a new warning badge
     * @since 1.0.0
     */
    public static Badge warning(String text) { return new Badge(text).variant("warning"); }

    /**
     * Creates a danger (red) badge.
     *
     * @param text the badge text
     * @return a new danger badge
     * @since 1.0.0
     */
    public static Badge danger(String text) { return new Badge(text).variant("danger"); }

    /**
     * Creates an info (blue) badge.
     *
     * @param text the badge text
     * @return a new info badge
     * @since 1.0.0
     */
    public static Badge info(String text) { return new Badge(text).variant("info"); }

    /**
     * Creates an outline badge (border only, transparent fill).
     *
     * @param text the badge text
     * @return a new outline badge
     * @since 1.0.0
     */
    public static Badge outline(String text) { return new Badge(text).variant("outline"); }

    /**
     * Adds a CSS class for the given variant.
     *
     * @param v the variant class name (null or blank → no-op)
     * @return this badge, for chaining
     * @since 1.0.0
     */
    public Badge variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }
}
