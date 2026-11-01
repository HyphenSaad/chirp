package chirp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Horizontal layout with fluent gap and padding.
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code h / horizontal / row / line}; {@code of / create}.</p>
 *
 * <p>Setters: {@code gap / spacing / space}; {@code padding / pad / inset};
 * {@code align / alignment / position / pos}; {@code centered / center};
 * {@code spaceBetween / spread / distribute}; {@code add / append / include}.</p>
 */
public class Row extends HBox {

    private Row(double gap) {
        super(gap);
        setAlignment(Pos.CENTER_LEFT);
    }

    // ============================================================== factories

    public static Row h() { return new Row(8); }
    public static Row h(double gap) { return new Row(gap); }
    public static Row horizontal() { return h(); }
    public static Row horizontal(double g) { return h(g); }
    public static Row row() { return h(); }
    public static Row row(double g) { return h(g); }
    public static Row line() { return h(); }
    public static Row of() { return h(); }
    public static Row of(double gap) { return h(gap); }
    public static Row create() { return h(); }

    // ============================================================== gap

    public Row gap(double g) { setSpacing(g); return this; }
    public Row spacing(double g) { return gap(g); }
    public Row space(double g) { return gap(g); }

    // ============================================================== padding

    public Row padding(double p) {
        setPadding(new Insets(p, p, p, p));
        return this;
    }
    public Row pad(double p) { return padding(p); }
    public Row inset(double p) { return padding(p); }

    public Row padding(double h, double v) {
        setPadding(new Insets(v, h, v, h));
        return this;
    }

    // ============================================================== alignment

    public Row align(Pos pos) { setAlignment(pos); return this; }
    public Row alignment(Pos p) { return align(p); }
    public Row position(Pos p) { return align(p); }
    public Row pos(Pos p) { return align(p); }
    public Row centered() { return align(Pos.CENTER); }
    public Row center() { return align(Pos.CENTER); }
    public Row spaceBetween() { return align(Pos.CENTER_LEFT); }
    public Row spread() { return align(Pos.CENTER_LEFT); }
    public Row distribute() { return align(Pos.CENTER_LEFT); }

    // ============================================================== add

    public Row add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getChildren().add(n);
        }
        return this;
    }
    public Row append(Node... nodes) { return add(nodes); }
    public Row include(Node... nodes) { return add(nodes); }

    // ============================================================== full width

    public Row fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
    public Row grow() { return fullWidth(); }
    public Row expand() { return fullWidth(); }

    public Row spacing(int i) { setSpacing(i); return this; }
}
