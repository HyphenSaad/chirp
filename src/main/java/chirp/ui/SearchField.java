package chirp.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Search field — a text field with an inline clear button.
 *
 * <pre>{@code
 * SearchField sf = SearchField.of("Search titles…")
 *     .onChange(q -> filterTable(q))
 *     .fullWidth();
 * }</pre>
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

    public static SearchField of() { return new SearchField(); }
    public static SearchField of(String placeholder) {
        SearchField sf = new SearchField();
        sf.field.setPromptText(placeholder == null ? "" : placeholder);
        return sf;
    }

    public SearchField onChange(Consumer<String> consumer) { this.onChange = consumer; return this; }

    public SearchField fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        field.setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    public String text() { return field.getText() == null ? "" : field.getText(); }
    public SearchField text(String t) { field.setText(t == null ? "" : t); return this; }
}
