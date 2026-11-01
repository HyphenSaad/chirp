package chirp.ui;

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Horizontal navigation trail — Home › Products › Laptops.
 *
 * <p>Each segment is a clickable hyperlink. The separators are static
 * {@code ›} labels. Clicking a segment calls the {@code onNavigate}
 * callback with that segment's text.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Breadcrumb.of()
 *     .add("Home")
 *     .add("Products")
 *     .add("Laptops")
 *     .onNavigate(segment -> navigate(segment));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a full menu bar, see {@link MenuBar}. For tabbed navigation,
 * see {@link TabPane}.</p>
 *
 * @see MenuBar
 * @see TabPane
 * @since 1.0.0
 */
public class Breadcrumb extends HBox {

    private Consumer<String> onNavigate;

    private Breadcrumb() {
        super(6);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("breadcrumb");
    }

    /**
     * Creates an empty breadcrumb.
     *
     * @return a new breadcrumb
     * @since 1.0.0
     */
    public static Breadcrumb of() { return new Breadcrumb(); }

    /**
     * Adds a segment. The segment is rendered as a clickable hyperlink
     * with a {@code ›} separator before it (except for the first).
     *
     * @param segment the segment label (null → no-op)
     * @return this breadcrumb, for chaining
     * @since 1.0.0
     */
    public Breadcrumb add(String segment) {
        if (segment == null) return this;
        if (!getChildren().isEmpty()) {
            Label sep = new Label("›");
            sep.getStyleClass().add("breadcrumb-separator");
            getChildren().add(sep);
        }
        Hyperlink link = new Hyperlink(segment);
        link.setOnAction(e -> { if (onNavigate != null) onNavigate.accept(segment); });
        getChildren().add(link);
        return this;
    }

    /**
     * Registers a listener for segment clicks.
     *
     * @param c the listener (null → no-op)
     * @return this breadcrumb, for chaining
     * @since 1.0.0
     */
    public Breadcrumb onNavigate(Consumer<String> c) { this.onNavigate = c; return this; }
}
