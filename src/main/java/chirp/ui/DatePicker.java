package chirp.ui;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Calendar-popup date picker.
 *
 * <pre>{@code
 * DatePicker dob = DatePicker.of().placeholder("Date of birth");
 * }</pre>
 */
public class DatePicker extends javafx.scene.control.DatePicker {

    private DatePicker() {}

    public static DatePicker of() { return new DatePicker(); }
    public static DatePicker of(LocalDate date) { DatePicker p = new DatePicker(); p.setValue(date); return p; }

    public DatePicker value(LocalDate d) { setValue(d); return this; }
    public DatePicker placeholder(String p) { setPromptText(p == null ? "" : p); return this; }
    public DatePicker disabled(boolean d) { setDisable(d); return this; }
    public DatePicker fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public DatePicker onChange(Consumer<LocalDate> c) {
        if (c != null) valueProperty().addListener((obs, o, n) -> c.accept(n));
        return this;
    }
}
