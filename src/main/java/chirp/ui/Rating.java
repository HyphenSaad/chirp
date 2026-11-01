package chirp.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Star rating widget — click a star to set the rating.
 *
 * <pre>{@code
 * Rating r = Rating.of().value(3).onChange(v -> output.text("Rated: " + v));
 * }</pre>
 */
public class Rating extends HBox {

    private final List<Label> stars = new ArrayList<>();
    private final int maxStars;
    private int value = 0;
    private Consumer<Integer> onChange;

    private Rating(int maxStars) {
        super(4);
        this.maxStars = Math.max(1, maxStars);
        setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < this.maxStars; i++) {
            final int rating = i + 1;
            Label star = new Label("☆");
            star.getStyleClass().add("rating-star");
            star.setOnMouseClicked(e -> setValue(rating));
            stars.add(star);
            getChildren().add(star);
        }
        render();
    }

    public static Rating of() { return new Rating(5); }
    public static Rating of(int max) { return new Rating(max); }

    public Rating value(int v) { setValue(v); return this; }
    public Rating onChange(Consumer<Integer> c) { this.onChange = c; return this; }
    public int value() { return value; }

    private void setValue(int v) {
        this.value = Math.max(0, Math.min(maxStars, v));
        render();
        if (onChange != null) onChange.accept(this.value);
    }

    private void render() {
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).setText(i < value ? "★" : "☆");
        }
    }
}
