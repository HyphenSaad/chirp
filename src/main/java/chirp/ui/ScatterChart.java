package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Scatter plot — numeric x and y, one dot per point.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ScatterChart.of()
 *     .add(1, 2.3)
 *     .add(2, 4.5)
 *     .add(3, 3.8);
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For lines between points, see {@link LineChart}.</p>
 *
 * @see LineChart
 * @since 1.0.0
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

    /**
     * Creates an empty scatter chart.
     *
     * @return a new scatter chart
     * @since 1.0.0
     */
    public static ScatterChart of() { return new ScatterChart(); }

    /**
     * Adds a point at (x, y).
     *
     * @param x the x value
     * @param y the y value
     * @return this chart, for chaining
     * @since 1.0.0
     */
    public ScatterChart add(double x, double y) {
        series.getData().add(new XYChart.Data<>(x, y));
        return this;
    }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public ScatterChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the x-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public ScatterChart xLabel(String label) { ((NumberAxis) getXAxis()).setLabel(label); return this; }

    /**
     * Sets the y-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public ScatterChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public ScatterChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public ScatterChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
