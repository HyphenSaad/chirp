package chirp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Fluent vertical layout — a {@link VBox} with a chainable gap,
 * padding, alignment, and add API.
 *
 * <p>Almost every Chirp UI application is a {@code Stack} of components
 * with a consistent gap between them:</p>
 * <pre>{@code
 * Stack root = Stack.v(16).padding(24)
 *     .add(Label.heading("Title"))
 *     .add(Label.of("Description"))
 *     .add(Button.primary("OK"));
 * }</pre>
 *
 * <h2>What this wrapper adds over {@code new VBox(gap)}</h2>
 * <ul>
 *   <li>Chaining — every setter returns the stack, so calls compose</li>
 *   <li>Null-safe {@code add} — null children are ignored, which
 *       makes "conditionally include a component" a one-liner</li>
 *   <li>Semantic factories — {@link #v()} (vertical) is more readable
 *       than {@code new VBox()}, and pairs with {@link Row#h()}</li>
 *   <li>Named setters — {@code .gap()} reads better than
 *       {@code .setSpacing()}</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For horizontal layouts, see {@link Row}. For the field composite
 * that often sits inside a stack, see {@link Field}. For a pre-built
 * form, see {@link FormBuilder}.</p>
 *
 * @see Row
 * @see Field
 * @see FormBuilder
 * @since 1.0.0
 */
public class Stack extends VBox {

    private Stack(double gap) { super(gap); }

    // ============================================================== factories

    /**
     * Creates a vertical stack with an 8-pixel gap between children.
     *
     * @return a new stack
     * @see #v(double)
     * @see #vertical()
     * @see #of()
     * @since 1.0.0
     */
    public static Stack v() { return new Stack(8); }

    /**
     * Creates a vertical stack with the given gap between children.
     *
     * @param gap the gap in pixels
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack v(double gap) { return new Stack(gap); }

    /**
     * Alias of {@link #v()} — see that method for full documentation
     * and examples.
     *
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack vertical() { return v(); }

    /**
     * Alias of {@link #v(double)} — see that method for full
     * documentation and examples.
     *
     * @param g the gap in pixels
     * @return a new stack
     * @see #v(double)
     * @since 1.0.0
     */
    public static Stack vertical(double g) { return v(g); }

    /**
     * Alias of {@link #v()} — see that method for full documentation
     * and examples.
     *
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack column() { return v(); }

    /**
     * Alias of {@link #v(double)} — see that method for full
     * documentation and examples.
     *
     * @param g the gap in pixels
     * @return a new stack
     * @see #v(double)
     * @since 1.0.0
     */
    public static Stack column(double g) { return v(g); }

    /**
     * Alias of {@link #v()} — see that method for full documentation
     * and examples.
     *
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack stack() { return v(); }

    /**
     * Alias of {@link #v(double)} — see that method for full
     * documentation and examples.
     *
     * @param g the gap in pixels
     * @return a new stack
     * @see #v(double)
     * @since 1.0.0
     */
    public static Stack stack(double g) { return v(g); }

    /**
     * Alias of {@link #v()} — see that method for full documentation
     * and examples.
     *
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack of() { return v(); }

    /**
     * Alias of {@link #v(double)} — see that method for full
     * documentation and examples.
     *
     * @param gap the gap in pixels
     * @return a new stack
     * @see #v(double)
     * @since 1.0.0
     */
    public static Stack of(double gap) { return v(gap); }

    /**
     * Alias of {@link #v()} — see that method for full documentation
     * and examples.
     *
     * @return a new stack
     * @see #v()
     * @since 1.0.0
     */
    public static Stack create() { return v(); }

    // ============================================================== gap

    /**
     * Sets the gap between children.
     *
     * @param g the gap in pixels
     * @return this stack
     * @since 1.0.0
     */
    public Stack gap(double g) { setSpacing(g); return this; }

    /**
     * Alias of {@link #gap(double)} — see that method for full
     * documentation.
     *
     * @param g the gap in pixels
     * @return this stack
     * @see #gap(double)
     * @since 1.0.0
     */
    public Stack spacing(double g) { return gap(g); }

    /**
     * Alias of {@link #gap(double)} — see that method for full
     * documentation.
     *
     * @param g the gap in pixels
     * @return this stack
     * @see #gap(double)
     * @since 1.0.0
     */
    public Stack space(double g) { return gap(g); }

    // ============================================================== padding

    /**
     * Sets uniform padding on all four sides.
     *
     * @param p the padding in pixels
     * @return this stack
     * @see #padding(double, double)
     * @since 1.0.0
     */
    public Stack padding(double p) {
        setPadding(new Insets(p, p, p, p));
        return this;
    }

    /**
     * Alias of {@link #padding(double)} — see that method for full
     * documentation.
     *
     * @param p the padding in pixels
     * @return this stack
     * @see #padding(double)
     * @since 1.0.0
     */
    public Stack pad(double p) { return padding(p); }

    /**
     * Alias of {@link #padding(double)} — see that method for full
     * documentation.
     *
     * @param p the padding in pixels
     * @return this stack
     * @see #padding(double)
     * @since 1.0.0
     */
    public Stack inset(double p) { return padding(p); }

    /**
     * Sets symmetric horizontal and vertical padding.
     *
     * @param h horizontal padding in pixels
     * @param v vertical padding in pixels
     * @return this stack
     * @since 1.0.0
     */
    public Stack padding(double h, double v) {
        setPadding(new Insets(v, h, v, h));
        return this;
    }

    /**
     * Sets padding individually for each side.
     *
     * @param top top padding in pixels
     * @param right right padding in pixels
     * @param bottom bottom padding in pixels
     * @param left left padding in pixels
     * @return this stack
     * @since 1.0.0
     */
    public Stack padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    // ============================================================== alignment

    /**
     * Sets the alignment of children within the stack.
     *
     * @param pos the alignment
     * @return this stack
     * @see javafx.geometry.Pos
     * @since 1.0.0
     */
    public Stack align(Pos pos) { setAlignment(pos); return this; }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this stack
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Stack alignment(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this stack
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Stack position(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} — see that method for full
     * documentation.
     *
     * @param p the alignment
     * @return this stack
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Stack pos(Pos p) { return align(p); }

    /**
     * Alias of {@link #align(Pos)} with {@code Pos.CENTER} — see that
     * method for full documentation.
     *
     * @return this stack
     * @see #align(Pos)
     * @since 1.0.0
     */
    public Stack centered() { return align(Pos.CENTER); }

    /**
     * Alias of {@link #centered()} — see that method for full
     * documentation.
     *
     * @return this stack
     * @see #centered()
     * @since 1.0.0
     */
    public Stack center() { return align(Pos.CENTER); }

    // ============================================================== add

    /**
     * Adds children to the stack. Null elements are silently ignored,
     * so "conditionally include a component" is a one-liner:
     * <pre>{@code
     * stack.add(Label.of("Always"));
     * stack.add(admin ? adminPanel : null);   // ignored when not admin
     * }</pre>
     *
     * @param nodes the children to add (null array or null elements → ignored)
     * @return this stack
     * @see #append(Node...)
     * @see #include(Node...)
     * @since 1.0.0
     */
    public Stack add(Node... nodes) {
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
     * @return this stack
     * @see #add(Node...)
     * @since 1.0.0
     */
    public Stack append(Node... nodes) { return add(nodes); }

    /**
     * Alias of {@link #add(Node...)} — see that method for full
     * documentation and examples.
     *
     * @param nodes the children to add
     * @return this stack
     * @see #add(Node...)
     * @since 1.0.0
     */
    public Stack include(Node... nodes) { return add(nodes); }

    /**
     * Alias of {@link #add(Node...)} — see that method for full
     * documentation and examples.
     *
     * @param nodes the children to add
     * @return this stack
     * @see #add(Node...)
     * @since 1.0.0
     */
    public Stack child(Node... nodes) { return add(nodes); }

    // ============================================================== grow

    /**
     * Makes the given child grow vertically to fill the remaining space
     * — the standard way to attach a "flexible middle" region:
     * <pre>{@code
     * stack.add(Label.heading("Top"));
     * stack.grow(scrollableContent);   // takes remaining vertical space
     * stack.add(Button.primary("Bottom"));
     * }</pre>
     *
     * @param child the child to grow (null → no-op)
     * @return this stack
     * @see #stretch(Node)
     * @see #fill(Node)
     * @since 1.0.0
     */
    public Stack grow(Node child) {
        VBox.setVgrow(child, javafx.scene.layout.Priority.ALWAYS);
        return this;
    }

    /**
     * Alias of {@link #grow(Node)} — see that method for full
     * documentation and examples.
     *
     * @param child the child to grow
     * @return this stack
     * @see #grow(Node)
     * @since 1.0.0
     */
    public Stack stretch(Node child) { return grow(child); }

    /**
     * Alias of {@link #grow(Node)} — see that method for full
     * documentation and examples.
     *
     * @param child the child to grow
     * @return this stack
     * @see #grow(Node)
     * @since 1.0.0
     */
    public Stack fill(Node child) { return grow(child); }
}
