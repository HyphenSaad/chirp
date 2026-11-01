package chirp.ui;

import java.io.File;
import java.util.function.Consumer;
import javafx.scene.image.Image;

/**
 * Fluent image view. Loads asynchronously by default.
 *
 * <pre>{@code
 * ImageView.of("https://picsum.photos/320/180").size(320, 180);
 * ImageView.of().local(new File("photo.jpg"));
 * ImageView.of().local("photo.jpg");   // static factory — also works
 * }</pre>
 */
public class ImageView extends javafx.scene.image.ImageView {

    private Consumer<Double> onProgress;
    private Runnable onLoaded;
    private Runnable onError;

    private ImageView() {
        setPreserveRatio(true);
        setSmooth(true);
    }

    // ---------------------------------------------------------- factories

    public static ImageView of() { return new ImageView(); }
    public static ImageView of(String url) { return new ImageView().url(url); }
    public static ImageView local(String path) { return new ImageView().url("file:" + path); }

    // ---------------------------------------------------------- chaining

    /** Loads the image from a local file — instance overload for {@link FileField} integration. */
    public ImageView local(File file) {
        if (file != null) {
            // toURI().toString() produces a canonical file: URI that handles
            // spaces, unicode, and Windows drive letters correctly.
            url(file.toURI().toString());
        }
        return this;
    }

    public ImageView url(String url) {
        if (url == null || url.isBlank()) return this;
        try {
            Image img = new Image(url, true);   // true = background loading

            img.progressProperty().addListener((obs, o, n) -> {
                double p = n == null ? 0 : n.doubleValue();
                if (onProgress != null) onProgress.accept(p);
                if (p >= 1.0 && !img.isError() && onLoaded != null) onLoaded.run();
            });
            img.errorProperty().addListener((obs, o, isError) -> {
                if (Boolean.TRUE.equals(isError) && onError != null) onError.run();
            });

            setImage(img);
        } catch (Exception e) {
            if (onError != null) onError.run();
        }
        return this;
    }

    public ImageView size(double w, double h) {
        setFitWidth(w);
        setFitHeight(h);
        return this;
    }
    public ImageView fitWidth(double w) { setFitWidth(w); return this; }
    public ImageView fitHeight(double h) { setFitHeight(h); return this; }

    public ImageView onProgress(Consumer<Double> cb) { this.onProgress = cb; return this; }
    public ImageView onLoaded(Runnable cb) { this.onLoaded = cb; return this; }
    public ImageView onError(Runnable cb) { this.onError = cb; return this; }

    /** No-op — loading begins when {@link #url(String)} is called. Kept for API symmetry. */
    public ImageView load() {
        return this;
    }
}
