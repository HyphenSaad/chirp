package chirp.ui;

import javafx.collections.FXCollections;

/**
 * Simple pie chart.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * PieChart.of()
 *     .add("Java", 45)
 *     .add("Python", 30)
 *     .add("C++", 25)
 *     .title("Language Popularity");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For bar, line, or area charts, see {@link BarChart},
 * {@link LineChart}, {@link AreaChart}.</p>
 *
 * @see BarChart
 * @see LineChart
 * @since 1.0.0
 */
public class PieChart extends javafx.scene.chart.PieChart {

    private PieChart() { super(FXCollections.observableArrayList()); }

    /**
     * Creates an empty pie chart.
     *
     * @return a new pie chart
     * @since 1.0.0
     */
    public static PieChart of() { return new PieChart(); }

    /**
     * Adds a slice with the given label and value.
     *
     * @param label the slice label
     * @param value the slice value
     * @return this chart, for chaining
     * @since 1.0.0
     */
    public PieChart add(String label, double value) {
        getData().add(new Data(label, value));
        return this;
    }

    /**
     * Sets the chart title.
     *
     * @param title the title
     * @return this chart
     * @since 1.0.0
     */
    public PieChart title(String title) { setTitle(title); return this; }

    /**
     * Sets the chart height.
     *
     * @param h the height in pixels
     * @return this chart
     * @since 1.0.0
     */
    public PieChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }

    /**
     * Makes the chart fill the available horizontal space.
     *
     * @return this chart
     * @since 1.0.0
     */
    public PieChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
