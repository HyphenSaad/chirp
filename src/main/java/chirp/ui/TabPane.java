package chirp.ui;

import javafx.scene.Node;

/**
 * Side-by-side tabs — each tab holds its own content, one visible at a
 * time.
 *
 * <p>The standard choice when you have 2+ independent views that the
 * user needs to switch between quickly (profile / settings / about,
 * dashboard / reports, etc.). Tabs are clickable and never closable by
 * default — the content is persistent state.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TabPane.of()
 *     .add("Profile", profilePanel)
 *     .add("Settings", settingsPanel)
 *     .add("About", aboutPanel)
 *     .fullWidth();
 * }</pre>
 *
 * <h2>Tabs vs accordion vs split</h2>
 * <ul>
 *   <li><b>Tabs</b> — top-level navigation, one view at a time</li>
 *   <li><b>Accordion</b> — collapsible sections, one open at a time</li>
 *   <li><b>SplitPane</b> — resizable side-by-side, both visible</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For collapsible sections, see {@link Accordion}. For resizable
 * split panels, see {@link SplitPane}. For navigation menus, see
 * {@link MenuBar}.</p>
 *
 * @see Accordion
 * @see SplitPane
 * @see MenuBar
 * @since 1.0.0
 */
public class TabPane extends javafx.scene.control.TabPane {

    private TabPane() {}

    /**
     * Creates an empty tab pane.
     *
     * @return a new tab pane
     * @since 1.0.0
     */
    public static TabPane of() { return new TabPane(); }

    /**
     * Adds a tab with the given title and content. Tabs are not closable
     * by default — the content is persistent state, not a document.
     *
     * @param title the tab title (null → "")
     * @param content the tab content (null → empty)
     * @return this pane, for chaining
     * @since 1.0.0
     */
    public TabPane add(String title, Node content) {
        javafx.scene.control.Tab tab = new javafx.scene.control.Tab(title, content);
        tab.setClosable(false);
        getTabs().add(tab);
        return this;
    }

    /**
     * Makes the tab pane fill the available horizontal space.
     *
     * @return this pane
     * @since 1.0.0
     */
    public TabPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
