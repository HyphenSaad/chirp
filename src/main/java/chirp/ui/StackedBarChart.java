package chirp.ui;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Stacked bar chart — multiple series stacked per category.
 *
 * <pre>{@code
 * StackedBarChart sbc = StackedBarChart.of();
 * sbc.series("2024").add("Q1", 100).add("Q2", 120);
 * sbc.series("2025").add("Q1", 140).add("Q2", 150);
 * }</pre>
 */
public class StackedBarChart extends javafx.scene.chart.StackedBarChart<String, Number> {

    private StackedBarChart() {
        super(new CategoryAxis(), new NumberAxis());
        setAnimated(false);
        setLegendVisible(true);
    }

    public static StackedBarChart of() { return new StackedBarChart(); }

    /** Starts a new series with the given name. Returns a small builder. */
    public Series series(String name) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(name);
        getData().add(s);
        return new Series(s);
    }

    public StackedBarChart title(String title) { setTitle(title); return this; }
    public StackedBarChart xLabel(String label) { ((CategoryAxis) getXAxis()).setLabel(label); return this; }
    public StackedBarChart yLabel(String label) { ((NumberAxis) getYAxis()).setLabel(label); return this; }
    public StackedBarChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public StackedBarChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    public static final class Series {
        private final XYChart.Series<String, Number> backing;
        Series(XYChart.Series<String, Number> backing) { this.backing = backing; }
        public Series add(String category, double value) {
            backing.getData().add(new XYChart.Data<>(category, value));
            return this;
        }
    }
}
