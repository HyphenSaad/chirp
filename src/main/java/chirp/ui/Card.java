package chirp.ui;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Fluent card — a bordered container with optional header, body, and
 * footer areas.
 *
 * <p>Use a card to group related content visually — a summary widget, a
 * list item, a settings panel. Empty sections are hidden automatically,
 * so a card with only a body does not show an empty header gap.</p>
 *
 * <h2>Three areas</h2>
 * <ul>
 *   <li>{@link #title(String)} / {@link #subtitle(String)} — header</li>
 *   <li>{@link #body(Node...)} — main content</li>
 *   <li>{@link #footer(Node...)} / {@link #actions(Node...)} — bottom row
 *       (typically buttons)</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Card.of()
 *     .title("Account")
 *     .subtitle("Personal information")
 *     .body(Field.of("Name").required(), Field.of("Email").email())
 *     .actions(Button.primary("Save"), Button.outline("Cancel"));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a collapsible card, see {@link TitledPane}. For a full
 * container with tabs, see {@link TabPane}. For a simple layout without
 * borders, see {@link Stack}.</p>
 *
 * @see TitledPane
 * @see TabPane
 * @since 1.0.0
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

    /**
     * Creates an empty card — no header, no body, no footer.
     *
     * @return a new card
     * @see #create()
     * @see #card()
     * @see #make()
     * @see #panel()
     * @since 1.0.0
     */
    public static Card of() { return new Card(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new card
     * @see #of()
     * @since 1.0.0
     */
    public static Card create() { return new Card(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new card
     * @see #of()
     * @since 1.0.0
     */
    public static Card card() { return new Card(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new card
     * @see #of()
     * @since 1.0.0
     */
    public static Card make() { return new Card(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new card
     * @see #of()
     * @since 1.0.0
     */
    public static Card panel() { return new Card(); }

    // ============================================================== title / subtitle

    /**
     * Adds a title line to the header. Multiple calls stack multiple
     * titles (rare — usually one).
     *
     * @param text the title text (null or blank → no-op)
     * @return this card, for chaining
     * @see #heading(String)
     * @see #subtitle(String)
     * @since 1.0.0
     */
    public Card title(String text) {
        if (text != null && !text.isBlank()) {
            header.getChildren().add(Label.subheading(text));
            refreshVisibility();
        }
        return this;
    }

    /**
     * Alias of {@link #title(String)} — see that method for full
     * documentation.
     *
     * @param text the title text
     * @return this card
     * @see #title(String)
     * @since 1.0.0
     */
    public Card heading(String text) { return title(text); }

    /**
     * Adds a muted subtitle to the header.
     *
     * @param text the subtitle text (null or blank → no-op)
     * @return this card, for chaining
     * @see #title(String)
     * @since 1.0.0
     */
    public Card subtitle(String text) {
        if (text != null && !text.isBlank()) {
            header.getChildren().add(Label.muted(text));
            refreshVisibility();
        }
        return this;
    }

    /**
     * Alias of {@link #subtitle(String)} — see that method for full
     * documentation.
     *
     * @param text the subtitle text
     * @return this card
     * @see #subtitle(String)
     * @since 1.0.0
     */
    public Card subheading(String text) { return subtitle(text); }

    /**
     * Alias of {@link #subtitle(String)} — see that method for full
     * documentation.
     *
     * @param text the subtitle text
     * @return this card
     * @see #subtitle(String)
     * @since 1.0.0
     */
    public Card description(String text) { return subtitle(text); }

    // ============================================================== content / body

    /**
     * Adds nodes to the card body. Null nodes are ignored.
     *
     * @param nodes the body children
     * @return this card, for chaining
     * @see #body(Node...)
     * @see #add(Node...)
     * @see #with(Node...)
     * @since 1.0.0
     */
    public Card content(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) body.getChildren().add(n);
            refreshVisibility();
        }
        return this;
    }

    /**
     * Alias of {@link #content(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the body children
     * @return this card
     * @see #content(Node...)
     * @since 1.0.0
     */
    public Card body(Node... nodes) { return content(nodes); }

    /**
     * Alias of {@link #content(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the body children
     * @return this card
     * @see #content(Node...)
     * @since 1.0.0
     */
    public Card add(Node... nodes) { return content(nodes); }

    /**
     * Alias of {@link #content(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the body children
     * @return this card
     * @see #content(Node...)
     * @since 1.0.0
     */
    public Card children(Node... nodes) { return content(nodes); }

    /**
     * Alias of {@link #content(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the body children
     * @return this card
     * @see #content(Node...)
     * @since 1.0.0
     */
    public Card with(Node... nodes) { return content(nodes); }

    // ============================================================== footer / actions

    /**
     * Adds nodes to the card footer — typically buttons.
     *
     * @param nodes the footer children
     * @return this card, for chaining
     * @see #actions(Node...)
     * @see #buttons(Node...)
     * @since 1.0.0
     */
    public Card footer(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) footer.getChildren().add(n);
            refreshVisibility();
        }
        return this;
    }

    /**
     * Alias of {@link #footer(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the footer children
     * @return this card
     * @see #footer(Node...)
     * @since 1.0.0
     */
    public Card actions(Node... nodes) { return footer(nodes); }

    /**
     * Alias of {@link #footer(Node...)} — see that method for full
     * documentation.
     *
     * @param nodes the footer children
     * @return this card
     * @see #footer(Node...)
     * @since 1.0.0
     */
    public Card buttons(Node... nodes) { return footer(nodes); }

    /**
     * Makes the card fill the available horizontal space.
     *
     * @return this card
     * @since 1.0.0
     */
    public Card fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this card
     * @see #fullWidth()
     * @since 1.0.0
     */
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
