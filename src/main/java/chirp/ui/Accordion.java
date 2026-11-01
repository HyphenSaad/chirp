package chirp.ui;

import javafx.scene.Node;

/**
 * Vertical stack of collapsible {@link TitledPane} sections — only one
 * section open at a time.
 *
 * <p>Use an accordion when you have 3+ sections and want the user to
 * focus on one at a time (settings panels, FAQ pages, multi-step
 * forms). For 2–3 sections that should all be visible, use a
 * {@link Stack} of {@link TitledPane}s instead.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Accordion.of()
 *     .add("General", generalPanel)
 *     .add("Advanced", advancedPanel)
 *     .add("About", aboutPanel)
 *     .fullWidth();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a single collapsible section, see {@link TitledPane}. For
 * side-by-side tabs, see {@link TabPane}. For a resizable two-panel
 * split, see {@link SplitPane}.</p>
 *
 * @see TitledPane
 * @see TabPane
 * @see SplitPane
 * @since 1.0.0
 */
public class Accordion extends javafx.scene.control.Accordion {

    private Accordion() {}

    /**
     * Creates an empty accordion.
     *
     * @return a new accordion
     * @since 1.0.0
     */
    public static Accordion of() { return new Accordion(); }

    /**
     * Adds a collapsible section with the given title and content.
     *
     * @param title the section title (null → "")
     * @param content the section content (null → no-op)
     * @return this accordion, for chaining
     * @since 1.0.0
     */
    public Accordion add(String title, Node content) {
        javafx.scene.control.TitledPane pane = new javafx.scene.control.TitledPane(title, content);
        pane.setAnimated(false);
        getPanes().add(pane);
        return this;
    }

    /**
     * Makes the accordion fill the available horizontal space.
     *
     * @return this accordion
     * @since 1.0.0
     */
    public Accordion fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
