package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Area chart — filled area under a line.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * AreaChart.of()
 *     .add("Q1", 120)
 *     .add("Q2", 180)
 *     .title("Growth");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a plain line, see {@link LineChart}.</p>
 *
 * @see LineChart
 * @since 1.0.0
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

    /**
     * Creates an empty area chart.
     *
     * @return a new area chart
     * @since 1.0.0
     */
    public static AreaChart of() { return new AreaChart(); }

    /**
     * Adds a point at the given category and value.
     *
     * @param category the category label
     * @param value the value
     * @return this chart, for chaining
     * @since 1.0.0
     */
    public AreaChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public AreaChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the x-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public AreaChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }

    /**
     * Sets the y-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public AreaChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public AreaChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public AreaChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
