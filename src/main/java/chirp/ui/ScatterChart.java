package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Scatter chart — numeric x and y.
 *
 * <pre>{@code
 * ScatterChart.of()
 *     .add(1, 2.3)
 *     .add(2, 4.5)
 *     .add(3, 3.8);
 * }</pre>
 */
public class ScatterChart extends javafx.scene.chart.ScatterChart<Number, Number> {

    private final XYChart.Series<Number, Number> series;

    private ScatterChart() {
        super(new NumberAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        setData(FXCollections.observableArrayList());
        getData().add(series);
        setLegendVisible(false);
        setAnimated(false);
    }

    public static ScatterChart of() { return new ScatterChart(); }

    public ScatterChart add(double x, double y) {
        series.getData().add(new XYChart.Data<>(x, y));
        return this;
    }

    public ScatterChart title(String title) { setTitle(title); return this; }
    public ScatterChart xLabel(String label) { ((NumberAxis) getXAxis()).setLabel(label); return this; }
    public ScatterChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }
    public ScatterChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public ScatterChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
