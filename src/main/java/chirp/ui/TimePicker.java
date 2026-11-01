package chirp.ui;

import java.time.LocalTime;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;

/**
 * Time picker — hour + minute spinners with an optional AM/PM marker.
 *
 * <p>JavaFX does not ship a {@code TimePicker}, so Chirp composes one
 * from two numeric spinners (for hour and minute) plus a label for the
 * AM/PM marker in 12-hour mode. The value at the API boundary is a
 * {@link LocalTime}, so callers never touch the individual spinners.</p>
 *
 * <h2>12-hour vs 24-hour mode</h2>
 * <ul>
 *   <li>{@link #of()} / {@link #of24Hour()} — 24-hour mode. Hours are
 *       0–23, no AM/PM marker.</li>
 *   <li>{@link #of12Hour()} — 12-hour mode. Hours are 1–12 with an
 *       AM/PM marker. The value is still a {@code LocalTime}, so
 *       {@code 2:30 PM} becomes {@code 14:30} internally.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * TimePicker meeting = TimePicker.of()
 *     .value(LocalTime.of(14, 30))
 *     .onChange(t -> System.out.println("Selected: " + t));
 * }</pre>
 *
 * <h2>Value normalization</h2>
 * <p>In 12-hour mode, {@code 12:00 AM} becomes {@code 00:00} and
 * {@code 12:00 PM} becomes {@code 12:00} — the standard conversion. The
 * displayed hour is always in the 1–12 range and the AM/PM marker
 * indicates the half of day, but the underlying value is always the
 * correct 24-hour {@code LocalTime}.</p>
 *
 * <h2>Related</h2>
 * <p>For date selection, see {@link DatePicker}. For time input via
 * text (flexible parsing), see {@link chirp.console.Input#readTime()}.</p>
 *
 * @see DatePicker
 * @since 1.0.0
 */
public class TimePicker extends HBox {

    private final Spinner<Integer> hourSpinner;
    private final Spinner<Integer> minuteSpinner;
    private final Label amPmLabel = new Label("AM");
    private final boolean twelveHour;

    private Consumer<LocalTime> onChange;

    private TimePicker(boolean twelveHour) {
        super(6);
        this.twelveHour = twelveHour;
        setAlignment(Pos.CENTER_LEFT);

        int hourMin = twelveHour ? 1 : 0;
        int hourMax = twelveHour ? 12 : 23;
        int initial = twelveHour ? 12 : 0;

        hourSpinner = new Spinner<>(hourMin, hourMax, initial);
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(80);

        minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(80);

        Label colon = new Label(":");
        colon.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        getChildren().addAll(hourSpinner, colon, minuteSpinner);

        if (twelveHour) {
            amPmLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 6;");
            getChildren().add(amPmLabel);
        }

        Runnable notify = () -> {
            if (onChange != null) onChange.accept(value());
        };
        hourSpinner.valueProperty().addListener((obs, o, n) -> notify.run());
        minuteSpinner.valueProperty().addListener((obs, o, n) -> notify.run());
    }

    // ---------------------------------------------------------- factories

    /**
     * Creates a 24-hour time picker (no AM/PM marker).
     *
     * @return a new time picker
     * @see #of24Hour()
     * @see #of12Hour()
     * @since 1.0.0
     */
    public static TimePicker of() { return new TimePicker(false); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new 24-hour time picker
     * @see #of()
     * @since 1.0.0
     */
    public static TimePicker of24Hour() { return new TimePicker(false); }

    /**
     * Creates a 12-hour time picker with an AM/PM marker.
     *
     * @return a new 12-hour time picker
     * @see #of()
     * @since 1.0.0
     */
    public static TimePicker of12Hour() { return new TimePicker(true); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new empty 24-hour time picker
     * @see #of()
     * @since 1.0.0
     */
    public static TimePicker empty() { return new TimePicker(false); }

    // ---------------------------------------------------------- chaining

    /**
     * Sets the current time. Handles both 12-hour and 24-hour
     * conversion to the underlying spinners.
     *
     * @param t the time to set (null → no-op)
     * @return this picker, for chaining
     * @see #value()
     * @since 1.0.0
     */
    public TimePicker value(LocalTime t) {
        if (t == null) return this;
        if (twelveHour) {
            int h = t.getHour();
            boolean pm = h >= 12;
            int h12 = h % 12;
            if (h12 == 0) h12 = 12;
            hourSpinner.getValueFactory().setValue(h12);
            amPmLabel.setText(pm ? "PM" : "AM");
        } else {
            hourSpinner.getValueFactory().setValue(t.getHour());
        }
        minuteSpinner.getValueFactory().setValue(t.getMinute());
        return this;
    }

    /**
     * Registers a listener for time changes.
     *
     * @param consumer the listener (null → no-op)
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public TimePicker onChange(Consumer<LocalTime> consumer) {
        this.onChange = consumer;
        return this;
    }

    /**
     * Enables or disables the entire picker.
     *
     * @param d whether to disable
     * @return this picker
     * @since 1.0.0
     */
    public TimePicker disabled(boolean d) {
        setDisable(d);
        return this;
    }

    /**
     * Toggles the AM/PM marker. Only has an effect in 12-hour mode.
     *
     * @return this picker, for chaining
     * @since 1.0.0
     */
    public TimePicker toggleAmPm() {
        if (twelveHour) {
            amPmLabel.setText("AM".equals(amPmLabel.getText()) ? "PM" : "AM");
            if (onChange != null) onChange.accept(value());
        }
        return this;
    }

    // ---------------------------------------------------------- accessor

    /**
     * Returns the current time as a {@code LocalTime}. In 12-hour mode,
     * the AM/PM marker is applied to produce the correct 24-hour value.
     *
     * @return the current time
     * @see #value(LocalTime)
     * @since 1.0.0
     */
    public LocalTime value() {
        int h = hourSpinner.getValue() == null ? 0 : hourSpinner.getValue();
        int m = minuteSpinner.getValue() == null ? 0 : minuteSpinner.getValue();
        if (twelveHour) {
            boolean pm = "PM".equals(amPmLabel.getText());
            if (pm && h != 12) h += 12;
            if (!pm && h == 12) h = 0;
        }
        return LocalTime.of(h, m);
    }
}
