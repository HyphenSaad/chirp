package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Vertical bar chart.
 *
 * <pre>{@code
 * BarChart bc = BarChart.of()
 *     .add("Q1", 120)
 *     .add("Q2", 180)
 *     .add("Q3", 150)
 *     .add("Q4", 210);
 * }</pre>
 */
public class BarChart extends javafx.scene.chart.BarChart<String, Number> {

    private final XYChart.Series<String, Number> series;

    private BarChart(CategoryAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);
        series = new XYChart.Series<>();
        setData(FXCollections.observableArrayList());
        getData().add(series);
        setLegendVisible(false);
        setAnimated(false);
    }

    public static BarChart of() { return new BarChart(new CategoryAxis(), new NumberAxis()); }

    public BarChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    public BarChart xLabel(String label) {
        ((CategoryAxis) getXAxis()).setLabel(label);
        return this;
    }

    public BarChart yLabel(String label) {
        ((NumberAxis) getYAxis()).setLabel(label);
        return this;
    }

    public BarChart title(String title) { setTitle(title); return this; }
    public BarChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public BarChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
