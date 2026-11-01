package chirp.ui;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Container with header, body, and footer areas.
 *
 * <p>Empty sections are hidden automatically — no blank gaps ever appear.</p>
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code of / create / card / make / panel}.</p>
 *
 * <p>Setters: {@code title / heading}; {@code subtitle / subheading / description};
 * {@code content / body / add / children / with}; {@code footer / actions / buttons};
 * {@code fullWidth / grow}.</p>
 */
public class Card extends VBox {

    private final VBox header = new VBox(4);
    private final VBox body = new VBox(8);
    private final HBox footer = new HBox(8);

    private Card() {
        super(0);
        getStyleClass().add("card");
        header.getStyleClass().add("card-header");
        body.getStyleClass().add("card-body");
        footer.getStyleClass().add("card-footer");
        getChildren().addAll(header, body, footer);
        refreshVisibility();
    }

    // ============================================================== factories

    public static Card of() { return new Card(); }
    public static Card create() { return new Card(); }
    public static Card card() { return new Card(); }
    public static Card make() { return new Card(); }
    public static Card panel() { return new Card(); }

    // ============================================================== title / subtitle

    public Card title(String text) {
        if (text != null && !text.isBlank()) {
            header.getChildren().add(Label.subheading(text));
            refreshVisibility();
        }
        return this;
    }
    public Card heading(String text) { return title(text); }

    public Card subtitle(String text) {
        if (text != null && !text.isBlank()) {
            header.getChildren().add(Label.muted(text));
            refreshVisibility();
        }
        return this;
    }
    public Card subheading(String text) { return subtitle(text); }
    public Card description(String text) { return subtitle(text); }

    // ============================================================== content / body

    public Card content(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) body.getChildren().add(n);
            refreshVisibility();
        }
        return this;
    }
    public Card body(Node... nodes) { return content(nodes); }
    public Card add(Node... nodes) { return content(nodes); }
    public Card children(Node... nodes) { return content(nodes); }
    public Card with(Node... nodes) { return content(nodes); }

    // ============================================================== footer / actions

    public Card footer(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) footer.getChildren().add(n);
            refreshVisibility();
        }
        return this;
    }
    public Card actions(Node... nodes) { return footer(nodes); }
    public Card buttons(Node... nodes) { return footer(nodes); }

    // ============================================================== full width

    public Card fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
    public Card grow() { return fullWidth(); }

    private void refreshVisibility() {
        boolean h = !header.getChildren().isEmpty();
        header.setVisible(h);
        header.setManaged(h);

        boolean b = !body.getChildren().isEmpty();
        body.setVisible(b);
        body.setManaged(b);

        boolean f = !footer.getChildren().isEmpty();
        footer.setVisible(f);
        footer.setManaged(f);
    }
}
