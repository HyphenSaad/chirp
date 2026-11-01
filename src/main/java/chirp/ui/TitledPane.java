package chirp.ui;

import javafx.scene.Node;

/**
 * A single collapsible section — a title bar that toggles the
 * visibility of its content.
 *
 * <p>For a group of mutually-exclusive sections, use {@link Accordion}.
 * For a single standalone section, use this directly.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TitledPane.of("Advanced options", advancedPanel)
 *     .expanded(false)   // start collapsed
 *     .fullWidth();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For an accordion (only one open at a time), see
 * {@link Accordion}. For side-by-side tabs, see {@link TabPane}.</p>
 *
 * @see Accordion
 * @see TabPane
 * @since 1.0.0
 */
public class TitledPane extends javafx.scene.control.TitledPane {

    private TitledPane(String title, Node content) {
        super(title == null ? "" : title, content);
        setAnimated(false);
    }

    /**
     * Creates a titled pane with the given title and content.
     *
     * @param title the section title (null → "")
     * @param content the section content (null → empty)
     * @return a new titled pane
     * @since 1.0.0
     */
    public static TitledPane of(String title, Node content) { return new TitledPane(title, content); }

    /**
     * Sets the expanded state.
     *
     * @param e whether the section is expanded
     * @return this pane, for chaining
     * @since 1.0.0
     */
    public TitledPane expanded(boolean e) { setExpanded(e); return this; }

    /**
     * Makes the pane fill the available horizontal space.
     *
     * @return this pane
     * @since 1.0.0
     */
    public TitledPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
