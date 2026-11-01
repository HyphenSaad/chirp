package chirp.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Removable tag — a label with an attached × button.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Chip chip = Chip.of("Java").onRemove(() -> chips.getChildren().remove(chip));
 * chips.add(chip);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a status indicator without removal, see {@link Badge}.</p>
 *
 * @see Badge
 * @since 1.0.0
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

    /**
     * Creates a chip with the given text.
     *
     * @param text the chip text
     * @return a new chip
     * @since 1.0.0
     */
    public static Chip of(String text) { return new Chip(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param t the chip text
     * @return a new chip
     * @see #of(String)
     * @since 1.0.0
     */
    public static Chip removable(String t) { return new Chip(t); }

    /**
     * Registers the callback fired when the × is clicked.
     *
     * @param r the callback (null → clicking × does nothing)
     * @return this chip, for chaining
     * @since 1.0.0
     */
    public Chip onRemove(Runnable r) { this.onRemove = r; return this; }

    /**
     * Returns the chip's text.
     *
     * @return the text
     * @since 1.0.0
     */
    public String text() { return textLabel.getText(); }
}
