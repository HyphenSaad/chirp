package chirp.ui;

import java.time.LocalTime;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;

/**
 * Time picker — hour + minute spinners with an AM/PM marker.
 *
 * <p>JavaFX has no built-in {@code TimePicker}; this one composes two
 * spinners and supports both 24-hour and 12-hour display modes. The return
 * value is always a {@link LocalTime}.</p>
 *
 * <pre>{@code
 * TimePicker tp = TimePicker.of()             // 24-hour format
 *     .value(LocalTime.of(14, 30))
 *     .onChange(t -> output.text("Time: " + t));
 *
 * TimePicker tp12 = TimePicker.of12Hour();    // 12-hour with AM/PM
 * }</pre>
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

    public static TimePicker of() { return new TimePicker(false); }
    public static TimePicker of24Hour() { return new TimePicker(false); }
    public static TimePicker of12Hour() { return new TimePicker(true); }
    public static TimePicker empty() { return new TimePicker(false); }

    // ---------------------------------------------------------- chaining

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

    public TimePicker onChange(Consumer<LocalTime> consumer) {
        this.onChange = consumer;
        return this;
    }

    public TimePicker disabled(boolean d) {
        setDisable(d);
        return this;
    }

    /** Toggle AM/PM (only meaningful in 12-hour mode). */
    public TimePicker toggleAmPm() {
        if (twelveHour) {
            amPmLabel.setText("AM".equals(amPmLabel.getText()) ? "PM" : "AM");
            if (onChange != null) onChange.accept(value());
        }
        return this;
    }

    // ---------------------------------------------------------- accessor

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
