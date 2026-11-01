package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Line chart — one series, category axis on x.
 *
 * <pre>{@code
 * LineChart.of()
 *     .add("Jan", 120)
 *     .add("Feb", 180)
 *     .title("Monthly Sales");
 * }</pre>
 */
public class LineChart extends javafx.scene.chart.LineChart<String, Number> {

    private final XYChart.Series<String, Number> series;

    private LineChart() {
        super(new CategoryAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        setData(FXCollections.observableArrayList());
        getData().add(series);
        setLegendVisible(false);
        setAnimated(false);
        setCreateSymbols(true);
    }

    public static LineChart of() { return new LineChart(); }

    public LineChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    public LineChart seriesName(String name) { series.setName(name); return this; }
    public LineChart title(String title) { setTitle(title); return this; }
    public LineChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }
    public LineChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }
    public LineChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public LineChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
