package chirp.ui;

/**
 * Split button — the main part fires the primary action, the arrow opens
 * a dropdown of secondary actions.
 *
 * <p>Use a split button when there is a "default" action that fires on
 * click and a small set of alternatives in the dropdown: "Save" (click)
 * with "Save As…" and "Save All" in the dropdown, or "Reply" with
 * "Reply All" and "Forward".</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SplitMenuButton.primary("Save")
 *     .onClick(() -> save())
 *     .item("Save As…",  () -> saveAs())
 *     .item("Save All", () -> saveAll())
 *     .itemDanger("Discard", () -> discard());
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a button with *only* a dropdown (no primary action), see
 * {@link MenuButton}. For a plain button, see {@link Button}.</p>
 *
 * @see MenuButton
 * @see Button
 * @since 1.0.0
 */
public class SplitMenuButton extends javafx.scene.control.SplitMenuButton {

    private SplitMenuButton(String text) { setText(text == null ? "" : text); }

    /**
     * Creates a plain split menu button with the given text.
     *
     * @param text the button text (null → "")
     * @return a new split menu button
     * @see #primary(String)
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static SplitMenuButton of(String text) { return new SplitMenuButton(text); }

    /**
     * Creates a primary (accent-coloured) split menu button.
     *
     * @param text the button text
     * @return a new primary split menu button
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static SplitMenuButton primary(String text) { return new SplitMenuButton(text).variant("primary"); }

    /**
     * Creates a secondary (grey) split menu button.
     *
     * @param text the button text
     * @return a new secondary split menu button
     * @see #primary(String)
     * @since 1.0.0
     */
    public static SplitMenuButton secondary(String text) { return new SplitMenuButton(text).variant("secondary"); }

    /**
     * Adds a CSS class for the given variant.
     *
     * @param v the variant class name (null or blank → no-op)
     * @return this button
     * @since 1.0.0
     */
    public SplitMenuButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    /**
     * Adds a dropdown item.
     *
     * @param label the item label
     * @param action the click action (null → no-op)
     * @return this button, for chaining
     * @since 1.0.0
     */
    public SplitMenuButton item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Adds a dropdown item with red styling — for destructive actions.
     *
     * @param label the item label
     * @param action the click action
     * @return this button, for chaining
     * @see #item(String, Runnable)
     * @since 1.0.0
     */
    public SplitMenuButton itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Registers the primary action — fires when the main button
     * (not the arrow) is clicked.
     *
     * @param r the primary action (null → no-op)
     * @return this button, for chaining
     * @since 1.0.0
     */
    public SplitMenuButton onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }
}
