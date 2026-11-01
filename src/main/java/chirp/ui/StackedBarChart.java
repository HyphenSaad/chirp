package chirp.ui;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Stacked bar chart — multiple series stacked per category.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * StackedBarChart sbc = StackedBarChart.of();
 * sbc.series("2024").add("Q1", 100).add("Q2", 120);
 * sbc.series("2025").add("Q1", 140).add("Q2", 150);
 * }</pre>
 *
 * <h2>Series builder</h2>
 * <p>{@link #series(String)} starts a new named series and returns a
 * small builder for adding bars — one series per group.</p>
 *
 * <h2>Related</h2>
 * <p>For a non-stacked bar chart, see {@link BarChart}.</p>
 *
 * @see BarChart
 * @since 1.0.0
 */
public class StackedBarChart extends javafx.scene.chart.StackedBarChart<String, Number> {

    private StackedBarChart() {
        super(new CategoryAxis(), new NumberAxis());
        setAnimated(false);
        setLegendVisible(true);
    }

    /**
     * Creates an empty stacked bar chart.
     *
     * @return a new stacked bar chart
     * @since 1.0.0
     */
    public static StackedBarChart of() { return new StackedBarChart(); }

    /**
     * Starts a new named series. Bars added via the returned builder
     * are stacked on top of each other per category.
     *
     * @param name the series name (shown in the legend)
     * @return a series builder
     * @since 1.0.0
     */
    public Series series(String name) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(name);
        getData().add(s);
        return new Series(s);
    }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public StackedBarChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the x-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public StackedBarChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }

    /**
     * Sets the y-axis label.
     *
     * @param label the label
     * @return this chart
     * @since 1.0.0
     */
    public StackedBarChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public StackedBarChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public StackedBarChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Small builder for adding bars to one series.
     *
     * @since 1.0.0
     */
    public static final class Series {
        private final XYChart.Series<String, Number> backing;
        Series(XYChart.Series<String, Number> backing) { this.backing = backing; }

        /**
         * Adds a stacked bar at the given category with the given value.
         *
         * @param category the category label
         * @param value the value
         * @return this builder, for chaining
         * @since 1.0.0
         */
        public Series add(String category, double value) {
            backing.getData().add(new XYChart.Data<>(category, value));
            return this;
        }
    }
}
