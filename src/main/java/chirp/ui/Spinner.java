package chirp.ui;

import java.util.function.Consumer;

/**
 * Numeric input with +/- increment buttons.
 *
 * <pre>{@code
 * Spinner age = Spinner.of(1, 120, 25);
 * }</pre>
 */
public class Spinner extends javafx.scene.control.Spinner<Integer> {

    private Spinner(int min, int max, int initial) {
        super(min, max, initial);
        setEditable(true);
    }

    public static Spinner of(int min, int max) { return new Spinner(min, max, min); }
    public static Spinner of(int min, int max, int initial) { return new Spinner(min, max, initial); }

    public int value() { return getValue() == null ? 0 : getValue(); }
    public Spinner value(int v) { getValueFactory().setValue(v); return this; }
    public Spinner fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public Spinner onChange(Consumer<Integer> c) {
        if (c != null) {
            valueProperty().addListener((obs, o, n) -> { if (n != null) c.accept(n); });
        }
        return this;
    }
}
