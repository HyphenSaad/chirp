package chirp.ui;

import java.util.function.Consumer;

/**
 * Fluent slider for numeric range selection.
 *
 * <pre>{@code
 * Slider s = Slider.of(0, 100, 50).showTicks(true).fullWidth();
 * }</pre>
 */
public class Slider extends javafx.scene.control.Slider {

    private Slider() { super(0, 100, 50); }
    private Slider(double min, double max, double initial) { super(min, max, initial); }

    public static Slider of() { return new Slider(); }
    public static Slider of(double min, double max, double v) { return new Slider(min, max, v); }

    public Slider range(double min, double max) { setMin(min); setMax(max); return this; }
    public Slider value(double v) { setValue(v); return this; }
    public Slider showTicks(boolean show) { setShowTickMarks(show); setShowTickLabels(show); return this; }
    public Slider blockIncrement(double step) { setBlockIncrement(step); return this; }
    public Slider fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public Slider onChange(Consumer<Double> c) {
        if (c != null) valueProperty().addListener((obs, o, n) -> c.accept(n.doubleValue()));
        return this;
    }
}
