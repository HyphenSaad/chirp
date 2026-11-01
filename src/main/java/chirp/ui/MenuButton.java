package chirp.ui;

/**
 * Button with an attached dropdown menu — the arrow opens the menu,
 * clicking the button text does nothing by default.
 *
 * <p>For a button whose main action fires and *also* has a dropdown
 * (different from {@code MenuButton}), use {@link SplitMenuButton}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * MenuButton.primary("Actions")
 *     .item("Save",   () -> save())
 *     .item("Export", () -> export())
 *     .separator()
 *     .itemDanger("Delete", () -> delete());
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a button that fires an action and has a dropdown, see
 * {@link SplitMenuButton}. For a top-of-window menu bar, see
 * {@link MenuBar}. For a right-click menu, see {@link ContextMenu}.</p>
 *
 * @see SplitMenuButton
 * @see MenuBar
 * @see ContextMenu
 * @since 1.0.0
 */
public class MenuButton extends javafx.scene.control.MenuButton {

    private MenuButton(String text) { super(text == null ? "" : text); }

    /**
     * Creates a plain menu button with the given text.
     *
     * @param text the button text (null → "")
     * @return a new menu button
     * @see #primary(String)
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static MenuButton of(String text) { return new MenuButton(text); }

    /**
     * Creates a primary (accent-coloured) menu button.
     *
     * @param text the button text
     * @return a new primary menu button
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static MenuButton primary(String text) { return new MenuButton(text).variant("primary"); }

    /**
     * Creates a secondary (grey) menu button.
     *
     * @param text the button text
     * @return a new secondary menu button
     * @see #primary(String)
     * @since 1.0.0
     */
    public static MenuButton secondary(String text) { return new MenuButton(text).variant("secondary"); }

    /**
     * Adds a CSS class for the given variant.
     *
     * @param v the variant class name (null or blank → no-op)
     * @return this button
     * @since 1.0.0
     */
    public MenuButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    /**
     * Adds a menu item with the given label and action.
     *
     * @param label the item label
     * @param action the click action (null → no-op)
     * @return this button, for chaining
     * @since 1.0.0
     */
    public MenuButton item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Adds a menu item with red styling — for destructive actions.
     *
     * @param label the item label
     * @param action the click action
     * @return this button, for chaining
     * @see #item(String, Runnable)
     * @since 1.0.0
     */
    public MenuButton itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /**
     * Adds a separator between menu items.
     *
     * @return this button, for chaining
     * @since 1.0.0
     */
    public MenuButton separator() {
        getItems().add(new javafx.scene.control.SeparatorMenuItem());
        return this;
    }
}
