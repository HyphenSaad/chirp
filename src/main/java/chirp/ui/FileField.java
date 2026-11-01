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
 * File picker field — a text field showing the chosen path plus a Browse button.
 *
 * <pre>{@code
 * FileField ff = FileField.of()
 *     .title("Choose image")
 *     .imagesOnly()
 *     .onChange(file -> imageView.file(file));   // e.g. load into ImageView
 * }</pre>
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

    public static FileField of() { return new FileField(); }
    public static FileField openFile() { return new FileField(); }
    public static FileField imagePicker() { return new FileField().imagesOnly().title("Choose image"); }

    // ---------------------------------------------------------- configuration

    public FileField title(String title) {
        if (title != null) chooser.setTitle(title);
        return this;
    }

    public FileField initialDirectory(String path) {
        if (path != null) {
            File dir = new File(path);
            if (dir.isDirectory()) chooser.setInitialDirectory(dir);
        }
        return this;
    }

    public FileField imagesOnly() {
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Images",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        return this;
    }

    public FileField textOnly() {
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Text files", "*.txt", "*.md", "*.csv", "*.log"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        return this;
    }

    public FileField filter(String description, String... extensions) {
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extensions));
        return this;
    }

    public FileField onChange(Consumer<File> consumer) {
        this.onChange = consumer;
        return this;
    }

    public FileField disabled(boolean d) {
        setDisable(d);
        return this;
    }

    // ---------------------------------------------------------- accessors

    public File file() { return selectedFile; }

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
