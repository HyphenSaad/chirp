package chirp.ui;

import javafx.scene.Node;

/**
 * Stack of collapsible titled panes — only one open at a time.
 *
 * <pre>{@code
 * Accordion a = Accordion.of()
 *     .add("Section 1", content1)
 *     .add("Section 2", content2);
 * }</pre>
 */
public class Accordion extends javafx.scene.control.Accordion {

    private Accordion() {}

    public static Accordion of() { return new Accordion(); }

    public Accordion add(String title, Node content) {
        javafx.scene.control.TitledPane pane = new javafx.scene.control.TitledPane(title, content);
        pane.setAnimated(false);
        getPanes().add(pane);
        return this;
    }

    public Accordion fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
