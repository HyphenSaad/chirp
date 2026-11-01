package chirp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Fluent horizontal layout — a {@link HBox} with a chainable gap,
 * padding, alignment, and add API.
 *
 * <p>The horizontal twin of {@link Stack}. Use it for toolbars,
 * button rows, "save / cancel" pairs, and any layout where components
 * sit side by side:</p>
 * <pre>{@code
 * Row buttons = Row.h(10)
 *     .add(Button.primary("Save"), Button.outline("Cancel"));
 * }</pre>
 *
 * <h2>What this wrapper adds over {@code new HBox(gap)}</h2>
 * <p>Same pattern as {@link Stack}: chaining, null-safe add, semantic
 * factories, named setters. The two classes are intentionally
 * symmetric so moving between them is trivial.</p>
 *
 * <h2>Related</h2>
 * <p>For vertical layouts, see {@link Stack}. For a pre-built form
 * that uses {@code Row} for its button area, see {@link FormBuilder}.</p>
 *
 * @see Stack
 * @see FormBuilder
 * @since 1.0.0
 */
public class Row extends HBox {

    private Row(double gap) {
        super(gap);
        setAlignment(Pos.CENTER_LEFT);
    }

    // ============================================================== factories

    /**
     * Creates a horizontal row with an 8-pixel gap between children,
     * left-aligned.
     *
     * @return a new row
     * @see #h(double)
     * @see #horizontal()
     * @since 1.0.0
     */
    public static Row h() { return new Row(8); }

    /**
     * Creates a horizontal row with the given gap, left-aligned.
     *
     * @param gap the gap in pixels
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row h(double gap) { return new Row(gap); }

    /**
     * Alias of {@link #h()} — see that method for full documentation
     * and examples.
     *
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row horizontal() { return h(); }

    /**
     * Alias of {@link #h(double)} — see that method for full
     * documentation and examples.
     *
     * @param g the gap in pixels
     * @return a new row
     * @see #h(double)
     * @since 1.0.0
     */
    public static Row horizontal(double g) { return h(g); }

    /**
     * Alias of {@link #h()} — see that method for full documentation
     * and examples.
     *
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row row() { return h(); }

    /**
     * Alias of {@link #h(double)} — see that method for full
     * documentation and examples.
     *
     * @param g the gap in pixels
     * @return a new row
     * @see #h(double)
     * @since 1.0.0
     */
    public static Row row(double g) { return h(g); }

    /**
     * Alias of {@link #h()} — see that method for full documentation
     * and examples.
     *
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row line() { return h(); }

    /**
     * Alias of {@link #h()} — see that method for full documentation
     * and examples.
     *
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row of() { return h(); }

    /**
     * Alias of {@link #h(double)} — see that method for full
     * documentation and examples.
     *
     * @param gap the gap in pixels
     * @return a new row
     * @see #h(double)
     * @since 1.0.0
     */
    public static Row of(double gap) { return h(gap); }

    /**
     * Alias of {@link #h()} — see that method for full documentation
     * and examples.
     *
     * @return a new row
     * @see #h()
     * @since 1.0.0
     */
    public static Row create() { return h(); }

    // ============================================================== gap

    /**
     * Sets the gap between children.
     *
     * @param g the gap in pixels
     * @return this row
     * @since 1.0.0
     */
    public Row gap(double g) { setSpacing(g); return this; }

    /**
     * Alias of {@link #gap(double)} — see that method for full
     * documentation.
     *
     * @param g the gap in pixels
     * @return this row
     * @see #gap(double)
     * @since 1.0.0
     */
    public Row spacing(double g) { return gap(g); }

    /**
     * Alias of {@link #gap(double)} — see that method for full
     * documentation.
     *
     * @param g the gap in pixels
     * @return this row
     * @see #gap(double)
     * @since 1.0.0
     */
    public Row space(double g) { return gap(g); }

    // ============================================================== padding

    /**
     * Sets uniform padding on all four sides.
     *
     * @param p the padding in pixels
     * @return this row
     * @since 1.0.0
     */
    public Row padding(double p) {
        setPadding(new Insets(p, p, p, p));
        return this;
    }

    /**
     * Alias of {@link #padding(double)} — see that method for full
     * documentation.
     *
     * @param p the padding in pixels
     * @return this row
     * @see #padding(double)
     * @since 1.0.0
     */
    public Row pad(double p) { return padding(p); }

    /**
     * Alias of {@link #padding(double)} — see that method for full
     * documentation.
     *
     * @param p the padding in pixels
     * @return this row
     * @see #padding(double)
     * @since 1.0.0
     */
    public Row inset(double p) { return padding(p); }

    /**
     * Sets symmetric horizontal and vertical padding.
     *
     * @param h horizontal padding in pixels
     * @param v vertical padding in pixels
     * @return this row
     * @since 1.0.0
     */
    public Row padding(double h, double v) {
        setPadding(new Insets(v, h, v, h));
        return this;
    }

    // ============================================================== alignment

    /**
     * Sets the alignment of children within the row.
     *
     * @param pos the alignment
     * @return this row
     * @see javafx.geometry.Pos
     * @since 1.0.0
     */
    public Row align(Pos pos) { setAlignment(pos); return this; }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this row
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Row alignment(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this row
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Row position(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this row
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Row pos(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} with {@code Pos.CENTER} — see that
     * method for full documentation.
     *
     * @return this row
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Row centered() { return align(Pos.CENTER); }

    /**
     * Alias of {@link #centered()} — see that method for full
     * documentation.
     *
     * @return this row
     * @see #centered()
     * @since 1.0.0
     */
    public Row center() { return align(Pos.CENTER); }

    /**
     * Sets the alignment to left-aligned with children evenly spread —
     * the common "toolbar with left and right groups" pattern.
     *
     * @return this row
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Row spaceBetween() { return align(Pos.CENTER_LEFT); }

    /**
     * Alias of {@link #spaceBetween()} — see that method for full
     * documentation.
     *
     * @return this row
     * @see #spaceBetween()
     * @since 1.0.0
     */
    public Row spread() { return align(Pos.CENTER_LEFT); }

    /**
     * Alias of {@link #spaceBetween()} — see that method for full
     * documentation.
     *
     * @return this row
     * @see #spaceBetween()
     * @since 1.0.0
     */
    public Row distribute() { return align(Pos.CENTER_LEFT); }

    // ============================================================== add

    /**
     * Adds children to the row. Null elements are silently ignored.
     *
     * @param nodes the children to add (null array or null elements → ignored)
     * @return this row
     * @see #append(Node...)
     * @see #include(Node...)
     * @since 1.0.0
     */
    public Row add(Node... nodes) {
        if (nodes != null) {
            for (Node n : nodes) if (n != null) getChildren().add(n);
        }
        return this;
    }

    /**
     * Alias of {@link #add(Node...)} — see that method for full
     * documentation and examples.
     *
     * @param nodes the children to add
     * @return this row
     * @see #add(Node...)
     * @since 1.0.0
     */
    public Row append(Node... nodes) { return add(nodes); }

    /**
     * Alias of {@link #add(Node...)} — see that method for full
     * documentation and examples.
     *
     * @param nodes the children to add
     * @return this row
     * @see #add(Node...)
     * @since 1.0.0
     */
    public Row include(Node... nodes) { return add(nodes); }

    // ============================================================== full width

    /**
     * Makes the row fill the available horizontal space.
     *
     * @return this row
     * @since 1.0.0
     */
    public Row fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this row
     * @see #fullWidth()
     * @since 1.0.0
     */
    public Row grow() { return fullWidth(); }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this row
     * @see #fullWidth()
     * @since 1.0.0
     */
    public Row expand() { return fullWidth(); }
}
