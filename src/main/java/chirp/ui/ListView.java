package chirp.ui;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;

/**
 * Scrollable list with single selection.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ListView.of("Apple", "Banana", "Cherry")
 *     .height(200)
 *     .onChange(item -> System.out.println("Picked: " + item));
 * }</pre>
 *
 * <h2>Height</h2>
 * <p>{@link #height(double)} sets both the preferred and minimum height,
 * so the list is respected even when its parent (a VBox) tries to
 * shrink it.</p>
 *
 * <h2>Related</h2>
 * <p>For a table with columns, see {@link Table}. For a hierarchical
 * list, see {@link TreeView}.</p>
 *
 * @see Table
 * @see TreeView
 * @since 1.0.0
 */
public class ListView extends javafx.scene.control.ListView<String> {

    private ListView() {}

    /**
     * Creates a list view with the given items.
     *
     * @param items the items (null → empty)
     * @return a new list view
     * @see #of(List)
     * @since 1.0.0
     */
    public static ListView of(String... items) {
        ListView lv = new ListView();
        if (items != null) {
            lv.setItems(FXCollections.observableArrayList(Arrays.asList(items)));
        }
        return lv;
    }

    /**
     * Creates a list view from a list of items.
     *
     * @param items the items (null → empty)
     * @return a new list view
     * @see #of(String...)
     * @since 1.0.0
     */
    public static ListView of(List<String> items) {
        ListView lv = new ListView();
        if (items != null) lv.setItems(FXCollections.observableArrayList(items));
        return lv;
    }

    /**
     * Adds an item to the list.
     *
     * @param item the item (null → no-op)
     * @return this list view, for chaining
     * @since 1.0.0
     */
    public ListView add(String item) {
        if (item != null) getItems().add(item);
        return this;
    }

    /**
     * Returns the currently selected item, or {@code null}.
     *
     * <p>The standard {@code value()} getter for this control. Mirrors
     * the selection model — read it directly instead of going through
     * {@code getSelectionModel().getSelectedItem()}.</p>
     *
     * @return the selected item, or {@code null} when nothing is selected
     * @see #onChange(Consumer)
     * @since 1.0.0
     */
    public String value() { return getSelectionModel().getSelectedItem(); }

    /**
     * Sets the list height — both preferred and minimum.
     *
     * @param h the height in pixels
     * @return this list view
     * @since 1.0.0
     */
    public ListView height(double h) {
        setPrefHeight(h);
        setMinHeight(h);
        return this;
    }

    /**
     * Makes the list fill the available horizontal space.
     *
     * @return this list view
     * @since 1.0.0
     */
    public ListView fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a listener for selection changes.
     *
     * @param c the listener (null → no-op)
     * @return this list view
     * @since 1.0.0
     */
    public ListView onChange(Consumer<String> c) {
        if (c != null) {
            getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n != null) c.accept(n);
            });
        }
        return this;
    }
}
