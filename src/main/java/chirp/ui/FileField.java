package chirp.ui;

import java.io.File;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

/**
 * File picker field — a text field showing the chosen path plus a
 * "Browse…" button that opens the system file dialog.
 *
 * <p>JavaFX's {@link FileChooser} is a dialog, not a control — it must
 * be invoked from a button click, and it returns a {@code File} rather
 * than holding state. {@code FileField} composes the two into a single
 * always-visible component that holds the selection.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FileField ff = FileField.of()
 *     .title("Choose an image")
 *     .imagesOnly()
 *     .onChange(file -> imageView.file(file));
 * }</pre>
 *
 * <h2>Filters</h2>
 * <p>Three built-in filters are available:</p>
 * <ul>
 *   <li>{@link #imagesOnly()} — {@code *.png *.jpg *.jpeg *.gif *.bmp *.webp}</li>
 *   <li>{@link #textOnly()} — {@code *.txt *.md *.csv *.log} plus {@code *.*}</li>
 *   <li>{@link #filter(String, String...)} — a custom filter</li>
 * </ul>
 * <p>Multiple filters can be combined — the dialog shows them in a
 * dropdown.</p>
 *
 * <h2>Reading the selection</h2>
 * <p>{@link #file()} returns the currently-selected {@code File}, or
 * {@code null} if nothing has been picked. {@link #file(File)} sets
 * the selection programmatically.</p>
 *
 * <h2>Related</h2>
 * <p>For displaying the picked file (images, previews), see
 * {@link ImageView}. For reading the file's contents once picked, see
 * {@link chirp.io.FileIO} or {@link chirp.io.TextFileIO}.</p>
 *
 * @see ImageView
 * @see chirp.io.FileIO
 * @see chirp.io.TextFileIO
 * @since 1.0.0
 */
public class FileField extends HBox {

    private final TextField pathField = new TextField();
    private final Button browseButton = new Button("Browse…");
    private final FileChooser chooser = new FileChooser();

    private File selectedFile;
    private Consumer<File> onChange;

    private FileField() {
        super(8);
        setAlignment(Pos.CENTER_LEFT);
        pathField.setEditable(false);
        pathField.setPromptText("No file selected");
        HBox.setHgrow(pathField, Priority.ALWAYS);

        browseButton.getStyleClass().add("secondary");
        browseButton.setOnAction(e -> openChooser());

        getChildren().addAll(pathField, browseButton);
    }

    // ---------------------------------------------------------- factories

    /**
     * Creates an empty file field with the default "All files" filter
     * (no filter — the dialog shows everything).
     *
     * @return a new file field
     * @see #openFile()
     * @see #imagePicker()
     * @since 1.0.0
     */
    public static FileField of() { return new FileField(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new file field
     * @see #of()
     * @since 1.0.0
     */
    public static FileField openFile() { return new FileField(); }

    /**
     * Creates a file field pre-configured for image selection (filter
     * restricted to common image extensions).
     *
     * @return a new image file field
     * @see #of()
     * @see #imagesOnly()
     * @since 1.0.0
     */
    public static FileField imagePicker() { return new FileField().imagesOnly().title("Choose image"); }

    // ---------------------------------------------------------- configuration

    /**
     * Sets the dialog title.
     *
     * @param title the title (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public FileField title(String title) {
        if (title != null) chooser.setTitle(title);
        return this;
    }

    /**
     * Sets the initial directory when the dialog opens.
     *
     * @param path the directory path (null or non-directory → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public FileField initialDirectory(String path) {
        if (path != null) {
            File dir = new File(path);
            if (dir.isDirectory()) chooser.setInitialDirectory(dir);
        }
        return this;
    }

    /**
     * Restricts the dialog to image files.
     *
     * @return this field, for chaining
     * @see #textOnly()
     * @see #filter(String, String...)
     * @since 1.0.0
     */
    public FileField imagesOnly() {
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Images",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        return this;
    }

    /**
     * Restricts the dialog to common text extensions plus "All files".
     *
     * @return this field, for chaining
     * @see #imagesOnly()
     * @since 1.0.0
     */
    public FileField textOnly() {
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Text files", "*.txt", "*.md", "*.csv", "*.log"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        return this;
    }

    /**
     * Adds a custom filter to the dialog. Existing filters are preserved.
     *
     * @param description the filter description shown in the dialog
     * @param extensions the extensions (e.g. {@code "*.json"})
     * @return this field, for chaining
     * @since 1.0.0
     */
    public FileField filter(String description, String... extensions) {
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extensions));
        return this;
    }

    /**
     * Registers a listener for file selection.
     *
     * @param consumer the listener (null → no-op)
     * @return this field, for chaining
     * @since 1.0.0
     */
    public FileField onChange(Consumer<File> consumer) {
        this.onChange = consumer;
        return this;
    }

    /**
     * Enables or disables the field.
     *
     * @param d whether to disable
     * @return this field
     * @since 1.0.0
     */
    public FileField disabled(boolean d) {
        setDisable(d);
        return this;
    }

    // ---------------------------------------------------------- accessors

    /**
     * Returns the currently-selected file, or null if nothing has been
     * picked.
     *
     * @return the file, or null
     * @see #file(File)
     * @since 1.0.0
     */
    public File file() { return selectedFile; }

    /**
     * Sets the selected file programmatically (as if the user had picked
     * it). Triggers the change listener.
     *
     * @param f the file (null → clears the selection)
     * @return this field, for chaining
     * @see #file()
     * @since 1.0.0
     */
    public FileField file(File f) {
        this.selectedFile = f;
        pathField.setText(f == null ? "" : f.getAbsolutePath());
        if (onChange != null) onChange.accept(f);
        return this;
    }

    // ---------------------------------------------------------- internal

    private void openChooser() {
        File f = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (f != null) file(f);
    }
}
