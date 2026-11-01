package chirp.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;

/**
 * Fluent dropdown (ComboBox wrapper) with optional search, validation,
 * and single-select.
 *
 * <h2>Searchable mode</h2>
 * <p>{@link #searchable()} turns on type-to-filter. As the user types in the
 * editor, the visible options filter to matching items. Selecting an option
 * hides the popup and resets the filter.</p>
 *
 * <h2>Why the guard flag</h2>
 * <p>An editable ComboBox has a notorious feedback loop: refiltering the items
 * can clear the selected value (because the value is no longer in the filtered
 * list), which triggers the skin to clear the editor text, which triggers our
 * text listener again, which refilters … <strong>StackOverflowError</strong>
 * or endless flicker. A single boolean {@code filtering} suppresses the
 * listener while we're the ones causing the change.</p>
 *
 * <pre>{@code
 * Select s = Select.of("Pakistan", "India", "Bangladesh", "Nepal")
 *     .searchable()
 *     .onChange(v -> Console.info("Picked: " + v));
 * }</pre>
 */
public class Select extends javafx.scene.control.ComboBox<String> {

    private final List<String> originalItems = new ArrayList<>();
    private FilteredList<String> filteredItems;
    private boolean required = false;

    /** Guard flag — true while we are the ones changing editor text or items. */
    private boolean filtering = false;

    private Select() {
        setEditable(false);
        valueProperty().addListener((obs, o, n) -> {
            if (required) runValidation();
        });
    }

    // ---------------------------------------------------------- factories

    public static Select of(String... items) {
        Select s = new Select();
        if (items != null) {
            s.originalItems.addAll(Arrays.asList(items));
            s.setItems(FXCollections.observableArrayList(s.originalItems));
        }
        return s;
    }

    public static Select of(List<String> items) {
        Select s = new Select();
        if (items != null) {
            s.originalItems.addAll(items);
            s.setItems(FXCollections.observableArrayList(s.originalItems));
        }
        return s;
    }

    // ---------------------------------------------------------- searchable

    /**
     * Enables type-to-filter. Idempotent — calling twice is safe.
     */
    public Select searchable() {
        if (filteredItems != null) return this;

        setEditable(true);
        filteredItems = new FilteredList<>(
                FXCollections.observableArrayList(originalItems), p -> true);
        setItems(filteredItems);
        setVisibleRowCount(10);

        // -- text changes filter items, with guard to break the feedback loop
        getEditor().textProperty().addListener((obs, oldV, newV) -> {
            if (filtering) return;
            String query = newV == null ? "" : newV;
            String lowerQuery = query.toLowerCase(Locale.ROOT);

            filtering = true;
            try {
                filteredItems.setPredicate(item -> lowerQuery.isBlank()
                        || (item != null && item.toLowerCase(Locale.ROOT).contains(lowerQuery)));

                if (!isShowing() && !lowerQuery.isBlank()) {
                    show();
                }
            } finally {
                filtering = false;
            }

            // The skin may have reset the editor text to "" while filtering.
            // Restore the user's query on the next pulse, guarded again.
            final String captured = query;
            Platform.runLater(() -> {
                if (filtering) return;                  // another event already in progress
                if (captured.equals(getEditor().getText())) return;   // already correct
                filtering = true;
                try {
                    getEditor().setText(captured);
                    getEditor().positionCaret(captured.length());
                } finally {
                    filtering = false;
                }
            });
        });

        // -- popup opens on editor click or when typing starts
        getEditor().setOnMouseClicked(e -> show());
        getEditor().focusedProperty().addListener((obs, o, n) -> {
            if (Boolean.TRUE.equals(n) && !isShowing()) show();
        });

        // -- when a value is picked: reset filter after the selection settles
        valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;                   // internal clear during filtering — ignore
            Platform.runLater(() -> {
                if (filteredItems == null) return;
                filtering = true;
                try {
                    filteredItems.setPredicate(p -> true);
                } finally {
                    filtering = false;
                }
                hide();
            });
        });

        return this;
    }

    // ---------------------------------------------------------- chaining

    public Select selected(String value) {
        if (value != null) setValue(value);
        return this;
    }

    public Select disabled(boolean d) { setDisable(d); return this; }

    public Select fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    public Select onChange(Consumer<String> consumer) {
        if (consumer != null) {
            valueProperty().addListener((obs, oldV, newV) -> consumer.accept(newV));
        }
        return this;
    }

    /** @return the current value, or {@code null}. */
    public String value() { return getValue(); }

    // ---------------------------------------------------------- validation

    /** Requires a non-null selection. */
    public Select required() {
        this.required = true;
        runValidation();
        return this;
    }

    public void runValidation() {
        if (!required) return;
        if (getValue() != null) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    public boolean isValid() { return !required || getValue() != null; }
}
