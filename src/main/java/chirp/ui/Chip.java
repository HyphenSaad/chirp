package chirp.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Removable tag ("chip").
 *
 * <pre>{@code
 * Chip chip = Chip.of("Java").onRemove(() -> list.getChildren().remove(chip));
 * }</pre>
 */
public class Chip extends HBox {

    private final Label textLabel;
    private final Button removeButton;
    private Runnable onRemove;

    private Chip(String text) {
        super(4);
        setAlignment(Pos.CENTER);
        getStyleClass().add("chip");

        textLabel = new Label(text == null ? "" : text);
        removeButton = new Button("×");
        removeButton.setFocusTraversable(false);
        removeButton.getStyleClass().add("chip-remove");
        removeButton.setOnAction(e -> {
            if (onRemove != null) onRemove.run();
        });

        getChildren().addAll(textLabel, removeButton);
    }

    public static Chip of(String text) { return new Chip(text); }
    public static Chip removable(String t) { return new Chip(t); }

    public Chip onRemove(Runnable r) { this.onRemove = r; return this; }
    public String text() { return textLabel.getText(); }
}
