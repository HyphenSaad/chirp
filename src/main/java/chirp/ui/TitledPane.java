package chirp.ui;

import javafx.scene.Node;

/**
 * Single collapsible titled section.
 *
 * <pre>{@code
 * TitledPane tp = TitledPane.of("Advanced options", content).expanded(false);
 * }</pre>
 */
public class TitledPane extends javafx.scene.control.TitledPane {

    private TitledPane(String title, Node content) {
        super(title == null ? "" : title, content);
        setAnimated(false);
    }

    public static TitledPane of(String title, Node content) { return new TitledPane(title, content); }

    public TitledPane expanded(boolean e) { setExpanded(e); return this; }
    public TitledPane fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
