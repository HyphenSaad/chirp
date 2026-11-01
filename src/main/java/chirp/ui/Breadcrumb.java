package chirp.ui;

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Horizontal navigation trail.
 *
 * <pre>{@code
 * Breadcrumb.of()
 *     .add("Home")
 *     .add("Products")
 *     .add("Laptops")
 *     .onNavigate(segment -> output.text("Navigate to " + segment));
 * }</pre>
 */
public class Breadcrumb extends HBox {

    private Consumer<String> onNavigate;

    private Breadcrumb() {
        super(6);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("breadcrumb");
    }

    public static Breadcrumb of() { return new Breadcrumb(); }

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

    public Breadcrumb onNavigate(Consumer<String> c) { this.onNavigate = c; return this; }
}
