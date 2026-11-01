package chirp.ui;

/**
 * Split button — the main part fires the primary action, the arrow opens
 * a dropdown of secondary actions.
 */
public class SplitMenuButton extends javafx.scene.control.SplitMenuButton {

    private SplitMenuButton(String text) { setText(text == null ? "" : text); }

    public static SplitMenuButton of(String text) { return new SplitMenuButton(text); }
    public static SplitMenuButton primary(String text) { return new SplitMenuButton(text).variant("primary"); }
    public static SplitMenuButton secondary(String text) { return new SplitMenuButton(text).variant("secondary"); }

    public SplitMenuButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    public SplitMenuButton item(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    public SplitMenuButton itemDanger(String label, Runnable action) {
        javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(label == null ? "" : label);
        mi.getStyleClass().add("danger");
        if (action != null) mi.setOnAction(e -> action.run());
        getItems().add(mi);
        return this;
    }

    public SplitMenuButton onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }
}
