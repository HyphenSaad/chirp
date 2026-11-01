package chirp.ui;

import javafx.scene.Node;

/**
 * Tabbed navigation — each tab holds its own content.
 *
 * <pre>{@code
 * TabPane tabs = TabPane.of()
 *     .add("Profile", profileContent)
 *     .add("Settings", settingsContent);
 * }</pre>
 */
public class TabPane extends javafx.scene.control.TabPane {

    private TabPane() {}

    public static TabPane of() { return new TabPane(); }

    public TabPane add(String title, Node content) {
        javafx.scene.control.Tab tab = new javafx.scene.control.Tab(title, content);
        tab.setClosable(false);
        getTabs().add(tab);
        return this;
    }

    public TabPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
