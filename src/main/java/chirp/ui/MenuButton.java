package chirp.ui;

/**
 * Button with an attached dropdown menu.
 *
 * <pre>{@code
 * MenuButton mb = MenuButton.primary("Actions")
 *     .item("Save",   () -> Console.success("saved"))
 *     .itemDanger("Delete", () -> Console.warn("deleted"));
 * }</pre>
 */
public class MenuButton extends javafx.scene.control.MenuButton {

    private MenuButton(String text) { super(text == null ? "" : text); }

    public static MenuButton of(String text) { return new MenuButton(text); }
    public static MenuButton primary(String text) { return new MenuButton(text).variant("primary"); }
    public static MenuButton secondary(String text) { return new MenuButton(text).variant("secondary"); }

    public MenuButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    public MenuButton item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    /** Item with red text — for destructive actions. */
    public MenuButton itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    public MenuButton separator() {
        getItems().add(new javafx.scene.control.SeparatorMenuItem());
        return this;
    }
}
