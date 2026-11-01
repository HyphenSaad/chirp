package chirp.ui;

import java.io.File;
import java.util.function.Consumer;
import javafx.scene.image.Image;

/**
 * Fluent image view — loads an image from a URL or local file, with
 * callbacks for progress, load completion, and error.
 *
 * <p>Loading is asynchronous by default — {@link #url(String)} returns
 * immediately and the image appears when the download completes. Attach
 * callbacks via {@link #onProgress(Consumer)}, {@link #onLoaded(Runnable)},
 * and {@link #onError(Runnable)} to react to the state.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ImageView.of()
 *     .url("https://picsum.photos/320/180")
 *     .size(320, 180)
 *     .onLoaded(() -> System.out.println("Loaded"));
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For picking a file with the OS dialog, see {@link FileField}.</p>
 *
 * @see FileField
 * @since 1.0.0
 */
public class ImageView extends javafx.scene.image.ImageView {

    private Consumer<Double> onProgress;
    private Runnable onLoaded;
    private Runnable onError;

    private ImageView() {
        setPreserveRatio(true);
        setSmooth(true);
    }

    /**
     * Creates an empty image view.
     *
     * @return a new image view
     * @see #of(String)
     * @see #local(String)
     * @since 1.0.0
     */
    public static ImageView of() { return new ImageView(); }

    /**
     * Creates an image view that loads the given URL.
     *
     * @param url the image URL
     * @return a new image view
     * @see #of()
     * @see #local(String)
     * @since 1.0.0
     */
    public static ImageView of(String url) { return new ImageView().url(url); }

    /**
     * Creates an image view that loads the given local file path.
     *
     * @param path the file path
     * @return a new image view
     * @see #of()
     * @since 1.0.0
     */
    public static ImageView local(String path) { return new ImageView().url("file:" + path); }

    /**
     * Loads an image from a local {@link File}. Uses a canonical
     * {@code file:} URI so spaces, unicode, and Windows drive letters
     * work correctly.
     *
     * @param file the file (null → no-op)
     * @return this view, for chaining
     * @since 1.0.0
     */
    public ImageView local(File file) {
        if (file != null) {
            url(file.toURI().toString());
        }
        return this;
    }

    /**
     * Loads an image from the given URL. The load is asynchronous.
     *
     * @param url the image URL (null or blank → no-op)
     * @return this view, for chaining
     * @since 1.0.0
     */
    public ImageView url(String url) {
        if (url == null || url.isBlank()) return this;
        try {
            Image img = new Image(url, true);

            img.progressProperty().addListener((obs, o, n) -> {
                double p = n == null ? 0 : n.doubleValue();
                if (onProgress != null) onProgress.accept(p);
                if (p >= 1.0 && !img.isError() && onLoaded != null) onLoaded.run();
            });
            img.errorProperty().addListener((obs, o, isError) -> {
                if (Boolean.TRUE.equals(isError) && onError != null) onError.run();
            });

            setImage(img);
        } catch (RuntimeException e) {
            if (onError != null) onError.run();
        }
        return this;
    }

    /**
     * Sets the display size.
     *
     * @param w the width in pixels
     * @param h the height in pixels
     * @return this view, for chaining
     * @since 1.0.0
     */
    public ImageView size(double w, double h) {
        setFitWidth(w);
        setFitHeight(h);
        return this;
    }

    /**
     * Sets the fit width.
     *
     * @param w the width in pixels
     * @return this view
     * @since 1.0.0
     */
    public ImageView fitWidth(double w) { setFitWidth(w); return this; }

    /**
     * Sets the fit height.
     *
     * @param h the height in pixels
     * @return this view
     * @since 1.0.0
     */
    public ImageView fitHeight(double h) { setFitHeight(h); return this; }

    /**
     * Registers a progress callback — receives a value from 0.0 to 1.0
     * as the image downloads.
     *
     * @param cb the callback (null → no-op)
     * @return this view
     * @since 1.0.0
     */
    public ImageView onProgress(Consumer<Double> cb) { this.onProgress = cb; return this; }

    /**
     * Registers a loaded callback — fires when the image finishes
     * downloading successfully.
     *
     * @param cb the callback (null → no-op)
     * @return this view
     * @since 1.0.0
     */
    public ImageView onLoaded(Runnable cb) { this.onLoaded = cb; return this; }

    /**
     * Registers an error callback — fires if the load fails.
     *
     * @param cb the callback (null → no-op)
     * @return this view
     * @since 1.0.0
     */
    public ImageView onError(Runnable cb) { this.onError = cb; return this; }

    /**
     * No-op — loading begins when {@link #url(String)} is called. Kept
     * for API symmetry.
     *
     * @return this view
     * @since 1.0.0
     */
    public ImageView load() {
        return this;
    }
}
