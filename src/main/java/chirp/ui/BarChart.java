package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Vertical bar chart.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * BarChart.of()
 *     .add("Q1", 120)
 *     .add("Q2", 180)
 *     .add("Q3", 150)
 *     .add("Q4", 210)
 *     .title("Quarterly Sales");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a stacked version, see {@link StackedBarChart}. For a line
 * chart, see {@link LineChart}.</p>
 *
 * @see StackedBarChart
 * @see LineChart
 * @since 1.0.0
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

    /**
     * Creates an empty bar chart.
     *
     * @return a new bar chart
     * @since 1.0.0
     */
    public static BarChart of() { return new BarChart(new CategoryAxis(), new NumberAxis()); }

    /**
     * Adds a bar with the given category label and value.
     *
     * @param category the category label
     * @param value the bar height
     * @return this chart, for chaining
     * @since 1.0.0
     */
    public BarChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    /**
     * Sets the x-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public BarChart xLabel(String label) {
        ((CategoryAxis) getXAxis()).setLabel(label);
        return this;
    }

    /**
     * Sets the y-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public BarChart yLabel(String label) {
        ((NumberAxis) getYAxis()).setLabel(label);
        return this;
    }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public BarChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public BarChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public BarChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
