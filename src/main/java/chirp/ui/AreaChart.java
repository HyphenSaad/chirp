package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Area chart — filled area under the line.
 *
 * <pre>{@code
 * AreaChart.of()
 *     .add("Q1", 120)
 *     .add("Q2", 180)
 *     .title("Growth");
 * }</pre>
 */
public class AreaChart extends javafx.scene.chart.AreaChart<String, Number> {

    private final XYChart.Series<String, Number> series;

    private AreaChart() {
        super(new CategoryAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        setData(FXCollections.observableArrayList());
        getData().add(series);
        setLegendVisible(false);
        setAnimated(false);
    }

    public static AreaChart of() { return new AreaChart(); }

    public AreaChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    public AreaChart title(String title) { setTitle(title); return this; }
    public AreaChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }
    public AreaChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }
    public AreaChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public AreaChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
