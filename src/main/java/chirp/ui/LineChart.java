package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Line chart — one series, category axis on x.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * LineChart.of()
 *     .add("Jan", 120)
 *     .add("Feb", 180)
 *     .add("Mar", 150)
 *     .title("Monthly Sales");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a filled line, see {@link AreaChart}. For a scatter plot, see
 * {@link ScatterChart}.</p>
 *
 * @see AreaChart
 * @see ScatterChart
 * @since 1.0.0
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

    /**
     * Creates an empty line chart.
     *
     * @return a new line chart
     * @since 1.0.0
     */
    public static LineChart of() { return new LineChart(); }

    /**
     * Adds a point at the given category and value.
     *
     * @param category the category label
     * @param value the value
     * @return this chart, for chaining
     * @since 1.0.0
     */
    public LineChart add(String category, double value) {
        series.getData().add(new XYChart.Data<>(category, value));
        return this;
    }

    /**
     * Sets the series name (shown in the legend).
     *
     * @param name the series name
     * @return this chart
     * @since 1.0.0
     */
    public LineChart seriesName(String name) { series.setName(name); return this; }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public LineChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the x-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public LineChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }

    /**
     * Sets the y-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public LineChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public LineChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public LineChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
