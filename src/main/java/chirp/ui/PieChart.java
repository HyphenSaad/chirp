package chirp.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.PieChart.Data;

/**
 * Simple pie chart.
 *
 * <pre>{@code
 * PieChart pie = PieChart.of()
 *     .add("Java", 45)
 *     .add("Python", 30)
 *     .add("C++", 25);
 * }</pre>
 */
public class PieChart extends javafx.scene.chart.PieChart {

    private PieChart() { super(FXCollections.observableArrayList()); }

    public static PieChart of() { return new PieChart(); }

    public PieChart add(String label, double value) {
        getData().add(new Data(label, value));
        return this;
    }

    public PieChart title(String title) { setTitle(title); return this; }
    public PieChart height(double h) { setPrefHeight(h); setMinHeight(h); return this; }
    public PieChart fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
