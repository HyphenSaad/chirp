package chirp.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Search field — a text field with an inline clear (×) button.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SearchField.of("Search titles…")
 *     .onChange(q -> filterTable(q))
 *     .fullWidth();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a plain text field, see {@link TextField}. For the composite
 * with label and error, see {@link Field}.</p>
 *
 * @see TextField
 * @see Field
 * @since 1.0.0
 */
public class SearchField extends StackPane {

    private final TextField field = TextField.of();
    private final Button clearButton;
    private Consumer<String> onChange;

    private SearchField() {
        field.setStyle("-fx-padding: 8 32 8 12;");

        clearButton = new Button("✕");
        clearButton.setFocusTraversable(false);
        clearButton.setVisible(false);
        clearButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #71717a;" +
                        "-fx-padding: 2 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;");
        StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(clearButton, new Insets(0, 2, 0, 0));
        clearButton.setOnAction(e -> field.setText(""));

        getChildren().addAll(field, clearButton);

        field.textProperty().addListener((obs, o, n) -> {
            clearButton.setVisible(n != null && !n.isEmpty());
            if (onChange != null) onChange.accept(n == null ? "" : n);
        });
    }

    /**
     * Creates an empty search field.
     *
     * @return a new search field
     * @see #of(String)
     * @since 1.0.0
     */
    public static SearchField of() { return new SearchField(); }

    /**
     * Creates a search field with a placeholder.
     *
     * @param placeholder the placeholder text
     * @return a new search field
     * @see #of()
     * @since 1.0.0
     */
    public static SearchField of(String placeholder) {
        SearchField sf = new SearchField();
        sf.field.setPromptText(placeholder == null ? "" : placeholder);
        return sf;
    }

    /**
     * Registers a listener for search text changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public SearchField onChange(Consumer<String> consumer) { this.onChange = consumer; return this; }

    /**
     * Makes the search field fill the available horizontal space.
     *
     * @return this field
     * @since 1.0.0
     */
    public SearchField fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        field.setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /**
     * Returns the current search text.
     *
     * @return the text
     * @since 1.0.0
     */
    public String text() { return field.getText() == null ? "" : field.getText(); }

    /**
     * Sets the search text programmatically.
     *
     * @param t the text
     * @return this field
     * @since 1.0.0
     */
    public SearchField text(String t) { field.setText(t == null ? "" : t); return this; }
}
