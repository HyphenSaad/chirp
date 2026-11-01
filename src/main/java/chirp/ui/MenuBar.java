package chirp.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Application menu bar.
 *
 * <pre>{@code
 * MenuBar mb = MenuBar.of()
 *     .menu("File",
 *         MenuBar.item("New",  () -> Ui.info("File", "New")),
 *         MenuBar.item("Open", () -> Ui.info("File", "Open")))
 *     .menu("Help",
 *         MenuBar.item("About", () -> Ui.info("About", "Chirp")));
 * }</pre>
 */
public class MenuBar extends javafx.scene.control.MenuBar {

    public static MenuBar of() { return new MenuBar(); }

    public MenuBar menu(String title, MenuItem... items) {
        Menu m = new Menu(title == null ? "" : title);
        if (items != null) m.getItems().addAll(items);
        getMenus().add(m);
        return this;
    }

    /** Factory — creates a menu item with an optional action. */
    public static MenuItem item(String text, Runnable action) {
        MenuItem mi = new MenuItem(text == null ? "" : text);
        if (action != null) mi.setOnAction(e -> action.run());
        return mi;
    }

    public static MenuItem item(String text) { return item(text, null); }

    public static javafx.scene.control.SeparatorMenuItem separator() {
        return new javafx.scene.control.SeparatorMenuItem();
    }
}
