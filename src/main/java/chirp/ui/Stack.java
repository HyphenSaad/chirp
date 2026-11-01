package chirp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Vertical layout with fluent gap and padding.
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code v / vertical / column / stack}; {@code of / create}.</p>
 *
 * <p>Setters: {@code gap / spacing / space}; {@code padding / pad / inset};
 * {@code align / alignment / position / pos}; {@code centered / center};
 * {@code add / append / include / child}; {@code grow / stretch / fill}.</p>
 */
public class Stack extends VBox {

    private Stack(double gap) { super(gap); }

    // ============================================================== factories

    public static Stack v() { return new Stack(8); }
    public static Stack v(double gap) { return new Stack(gap); }
    public static Stack vertical() { return v(); }
    public static Stack vertical(double g) { return v(g); }
    public static Stack column() { return v(); }
    public static Stack column(double g) { return v(g); }
    public static Stack stack() { return v(); }
    public static Stack stack(double g) { return v(g); }
    public static Stack of() { return v(); }
    public static Stack of(double gap) { return v(gap); }
    public static Stack create() { return v(); }

    // ============================================================== gap

    public Stack gap(double g) { setSpacing(g); return this; }
    public Stack spacing(double g) { return gap(g); }
    public Stack space(double g) { return gap(g); }

    // ============================================================== padding

    public Stack padding(double p) {
        setPadding(new Insets(p, p, p, p));
        return this;
    }
    public Stack pad(double p) { return padding(p); }
    public Stack inset(double p) { return padding(p); }

    public Stack padding(double h, double v) {
        setPadding(new Insets(v, h, v, h));
        return this;
    }
    public Stack padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    // ============================================================== alignment

    public Stack align(Pos pos) { setAlignment(pos); return this; }
    public Stack alignment(Pos p) { return align(p); }
    public Stack position(Pos p) { return align(p); }
    public Stack pos(Pos p) { return align(p); }
    public Stack centered() { return align(Pos.CENTER); }
    public Stack center() { return align(Pos.CENTER); }

    // ============================================================== add

    public Stack add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getChildren().add(n);
        }
        return this;
    }
    public Stack append(Node... nodes) { return add(nodes); }
    public Stack include(Node... nodes) { return add(nodes); }
    public Stack child(Node... nodes) { return add(nodes); }

    // ============================================================== grow

    public Stack grow(Node child) {
        VBox.setVgrow(child, javafx.scene.layout.Priority.ALWAYS);
        return this;
    }
    public Stack stretch(Node child) { return grow(child); }
    public Stack fill(Node child) { return grow(child); }

    public Stack spacing(int i) { setSpacing(i); return this; }
}
