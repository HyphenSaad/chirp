package chirp.ui;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Fluent date picker with a calendar popup.
 *
 * <p>Wraps {@link javafx.scene.control.DatePicker} with fluent setters.
 * The underlying JavaFX control already provides the calendar UI, the
 * date format, and the parse logic — this class only adds the fluent
 * setters and a value-change listener.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * DatePicker dob = DatePicker.of()
 *     .placeholder("Date of birth")
 *     .onChange(d -> System.out.println("Selected: " + d));
 * }</pre>
 *
 * <h2>Date format</h2>
 * <p>The displayed format is controlled by JavaFX's
 * {@code converter} property, which this wrapper does not override.
 * The default format matches the system locale — {@code M/d/yyyy} in
 * the US, {@code dd/MM/yyyy} in the UK, and so on. Call
 * {@code setConverter(...)} on the underlying control for a custom
 * format.</p>
 *
 * <h2>Related</h2>
 * <p>For time selection, see {@link TimePicker}. For date/time input
 * via text (with format flexibility), see
 * {@link chirp.console.Input#readDate()}.</p>
 *
 * @see TimePicker
 * @since 1.0.0
 */
public class DatePicker extends javafx.scene.control.DatePicker {

    private DatePicker() {}

    /**
     * Creates an empty date picker.
     *
     * @return a new date picker
     * @see #of(LocalDate)
     * @since 1.0.0
     */
    public static DatePicker of() { return new DatePicker(); }

    /**
     * Creates a date picker pre-selected to the given date.
     *
     * @param date the initial date (null → empty)
     * @return a new date picker
     * @see #of()
     * @since 1.0.0
     */
    public static DatePicker of(LocalDate date) { DatePicker p = new DatePicker(); p.setValue(date); return p; }

    /**
     * Sets the selected date.
     *
     * @param d the date (null → clears the selection)
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public DatePicker value(LocalDate d) { setValue(d); return this; }

    /**
     * Sets the placeholder text.
     *
     * @param p the placeholder (null → "")
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public DatePicker placeholder(String p) { setPromptText(p == null ? "" : p); return this; }

    /**
     * Enables or disables the picker.
     *
     * @param d whether to disable
     * @return this picker
     * @since 1.0.0
     */
    public DatePicker disabled(boolean d) { setDisable(d); return this; }

    /**
     * Makes the picker fill the available horizontal space.
     *
     * @return this picker
     * @since 1.0.0
     */
    public DatePicker fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Registers a listener for date changes.
     *
     * @param c the listener (null → no-op)
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public DatePicker onChange(Consumer<LocalDate> c) {
        if (c != null) valueProperty().addListener((obs, o, n) -> c.accept(n));
        return this;
    }
}
