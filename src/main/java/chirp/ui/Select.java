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
 * Fluent dropdown — a {@link javafx.scene.control.ComboBox} wrapper with
 * optional type-to-search mode and single-selection validation.
 *
 * <h2>What this wrapper adds over {@code new ComboBox<>()}</h2>
 * <ul>
 *   <li>Chaining — every setter returns {@code this}</li>
 *   <li>Type-to-search mode via {@link #searchable()}</li>
 *   <li>Built-in {@link #required()} validation</li>
 *   <li>Fluent factories that take varargs or a list</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Select country = Select.of("Pakistan", "India", "Bangladesh", "Nepal")
 *     .searchable()
 *     .required()
 *     .onChange(v -> System.out.println("Picked: " + v));
 * }</pre>
 *
 * <h2>The searchable mode and its feedback loop</h2>
 * <p>{@link #searchable()} turns the dropdown into an editable
 * ComboBox whose editor filters the visible items as the user types.
 * The naive implementation is famous for a feedback loop:</p>
 * <ol>
 *   <li>User types → our text listener refilters the item list</li>
 *   <li>Refiltering can clear the currently-selected value (it is no
 *       longer in the filtered set)</li>
 *   <li>JavaFX's ComboBox skin reacts to the cleared value by clearing
 *       the editor text</li>
 *   <li>Clearing the editor text fires our text listener again — back
 *       to step 1</li>
 * </ol>
 * <p>The result is an endless loop or flicker. The implementation here
 * uses a single boolean guard ({@code filtering}) that is set to
 * {@code true} while we are the ones mutating state, which suppresses
 * the listener's own reaction. It is the standard fix for this
 * problem, and it is why the class is not just
 * {@code return new ComboBox<>()}.</p>
 *
 * <h2>Related</h2>
 * <p>For a radio-button group (single-selection from a small fixed
 * set), see {@link RadioGroup}. For a free-text field with
 * autocomplete (not a dropdown), see {@link TextField}.</p>
 *
 * @see RadioGroup
 * @see TextField
 * @since 1.0.0
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

    /**
     * Creates a dropdown with the given options.
     *
     * @param items the option strings (null → empty dropdown)
     * @return a new dropdown
     * @see #of(List)
     * @since 1.0.0
     */
    public static Select of(String... items) {
        Select s = new Select();
        if (items != null) {
            s.originalItems.addAll(Arrays.asList(items));
            s.setItems(FXCollections.observableArrayList(s.originalItems));
        }
        return s;
    }

    /**
     * Creates a dropdown from a list of options.
     *
     * @param items the options (null → empty dropdown)
     * @return a new dropdown
     * @see #of(String...)
     * @since 1.0.0
     */
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
     * Enables type-to-filter. As the user types in the editor, the
     * visible options filter to those that contain the query
     * (case-insensitive). Selecting an option resets the filter and
     * hides the popup.
     *
     * <p>Idempotent — calling twice is safe and does not double-wrap the
     * filter.</p>
     *
     * <p>See the class-level Javadoc for the feedback loop this method
     * is carefully written to avoid.</p>
     *
     * @return this dropdown, for chaining
     * @since 1.0.0
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

    /**
     * Sets the selected value.
     *
     * @param value the value to select (null → no-op)
     * @return this dropdown, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public Select selected(String value) {
        if (value != null) setValue(value);
        return this;
    }

    /**
     * Enables or disables the dropdown.
     *
     * @param d whether to disable
     * @return this dropdown
     * @since 1.0.0
     */
    public Select disabled(boolean d) { setDisable(d); return this; }

    /**
     * Makes the dropdown fill the available horizontal space.
     *
     * @return this dropdown
     * @since 1.0.0
     */
    public Select fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /**
     * Registers a listener for value changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this dropdown, for chaining
     * @since 1.0.0
     */
    public Select onChange(Consumer<String> consumer) {
        if (consumer != null) {
            valueProperty().addListener((obs, oldV, newV) -> consumer.accept(newV));
        }
        return this;
    }

    /**
     * Returns the current value, or null if nothing is selected.
     *
     * @return the value, or null
     * @see #selected(String)
     * @since 1.0.0
     */
    public String value() { return getValue(); }

    // ---------------------------------------------------------- validation

    /**
     * Requires a non-null selection.
     *
     * @return this dropdown, for chaining
     * @since 1.0.0
     */
    public Select required() {
        this.required = true;
        runValidation();
        return this;
    }

    /**
     * Runs the validator now.
     *
     * @since 1.0.0
     */
    public void runValidation() {
        if (!required) return;
        if (getValue() != null) {
            getStyleClass().remove("invalid");
        } else if (!getStyleClass().contains("invalid")) {
            getStyleClass().add("invalid");
        }
    }

    /**
     * Checks whether the current selection passes the validator.
     *
     * @return {@code true} if valid (or no validator)
     * @since 1.0.0
     */
    public boolean isValid() { return !required || getValue() != null; }
}
