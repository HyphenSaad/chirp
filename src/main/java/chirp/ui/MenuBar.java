package chirp.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Application menu bar — File / Edit / View / Help, with dropdowns.
 *
 * <p>The standard desktop-style menu. Each menu holds items, items fire
 * actions. On most platforms the menu bar appears at the top of the
 * window; on macOS it may be hoisted into the system menu bar by
 * JavaFX.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * MenuBar.of()
 *     .menu("File",
 *         MenuBar.item("New",  () -> newFile()),
 *         MenuBar.item("Open", () -> openFile()),
 *         MenuBar.separator(),
 *         MenuBar.item("Exit", () -> Platform.exit()))
 *     .menu("Help",
 *         MenuBar.item("About", () -> showAbout()));
 * }</pre>
 *
 * <h2>Static helpers</h2>
 * <p>{@link #item(String, Runnable)} and {@link #separator()} are static
 * factories for the menu item types — this makes the {@code .menu()}
 * call read as a list of items rather than four lines of
 * {@code new MenuItem(...) } boilerplate.</p>
 *
 * <h2>Related</h2>
 * <p>For a button-with-dropdown, see {@link MenuButton}. For a
 * right-click menu, see {@link ContextMenu}. For a persistent action
 * bar, see {@link ToolBar}.</p>
 *
 * @see MenuButton
 * @see ContextMenu
 * @see ToolBar
 * @since 1.0.0
 */
public class MenuBar extends javafx.scene.control.MenuBar {

    /**
     * Creates an empty menu bar.
     *
     * @return a new menu bar
     * @since 1.0.0
     */
    public static MenuBar of() { return new MenuBar(); }

    /**
     * Adds a menu with the given title and items.
     *
     * @param title the menu title (null → "")
     * @param items the menu items
     * @return this menu bar, for chaining
     * @see #item(String, Runnable)
     * @see #separator()
     * @since 1.0.0
     */
    public MenuBar menu(String title, MenuItem... items) {
        Menu m = new Menu(title == null ? "" : title);
        if (items != null) m.getItems().addAll(items);
        getMenus().add(m);
        return this;
    }

    /**
     * Factory — creates a menu item with the given text and click
     * action.
     *
     * @param text the item text (null → "")
     * @param action the click action (null → no-op)
     * @return a new menu item
     * @see #menu(String, MenuItem...)
     * @since 1.0.0
     */
    public static MenuItem item(String text, Runnable action) {
        MenuItem mi = new MenuItem(text == null ? "" : text);
        if (action != null) mi.setOnAction(e -> action.run());
        return mi;
    }

    /**
     * Factory — creates a menu item with no click action.
     *
     * @param text the item text
     * @return a new menu item
     * @see #item(String, Runnable)
     * @since 1.0.0
     */
    public static MenuItem item(String text) { return item(text, null); }

    /**
     * Factory — creates a horizontal separator between menu items.
     *
     * @return a new separator menu item
     * @since 1.0.0
     */
    public static javafx.scene.control.SeparatorMenuItem separator() {
        return new javafx.scene.control.SeparatorMenuItem();
    }
}
