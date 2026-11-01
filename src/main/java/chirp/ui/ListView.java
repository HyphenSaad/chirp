package chirp.ui;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;

/**
 * Scrollable list with single selection.
 *
 * <p><b>Height:</b> {@link #height(double)} sets both preferred and minimum
 * height, so the list is respected even when its parent layout (e.g. a VBox)
 * tries to shrink it.</p>
 */
public class ListView extends javafx.scene.control.ListView<String> {

    private ListView() {}

    public static ListView of(String... items) {
        ListView lv = new ListView();
        if (items != null) {
            lv.setItems(FXCollections.observableArrayList(Arrays.asList(items)));
        }
        return lv;
    }

    public static ListView of(List<String> items) {
        ListView lv = new ListView();
        if (items != null) lv.setItems(FXCollections.observableArrayList(items));
        return lv;
    }

    public ListView add(String item) {
        if (item != null) getItems().add(item);
        return this;
    }

    public ListView height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    public ListView fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public ListView onChange(Consumer<String> c) {
        if (c != null) {
            getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n != null) c.accept(n);
            });
        }
        return this;
    }
}
