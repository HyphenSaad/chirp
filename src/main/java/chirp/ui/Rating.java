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
 * <h2>Example</h2>
 * <pre>{@code
 * Rating.of().value(3).onChange(v -> System.out.println("Rated: " + v));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a numeric rating input (e.g. spinner), see {@link Spinner}.</p>
 *
 * @see Spinner
 * @since 1.0.0
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

    /**
     * Creates a 5-star rating widget.
     *
     * @return a new rating widget
     * @see #of(int)
     * @since 1.0.0
     */
    public static Rating of() { return new Rating(5); }

    /**
     * Creates a rating widget with the given maximum number of stars.
     *
     * @param max the maximum number of stars
     * @return a new rating widget
     * @see #of()
     * @since 1.0.0
     */
    public static Rating of(int max) { return new Rating(max); }

    /**
     * Sets the current rating value.
     *
     * @param v the rating (0 to maxStars)
     * @return this widget, for chaining
     * @since 1.0.0
     */
    public Rating value(int v) { setValue(v); return this; }

    /**
     * Registers a listener for rating changes.
     *
     * @param c the listener (null → no-op)
     * @return this widget, for chaining
     * @since 1.0.0
     */
    public Rating onChange(Consumer<Integer> c) { this.onChange = c; return this; }

    /**
     * Returns the current rating value.
     *
     * @return the rating
     * @since 1.0.0
     */
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
