package chirp.ui;

/**
 * Fluent button with semantic variants — {@code primary}, {@code secondary},
 * {@code success}, {@code danger}, {@code outline}, {@code ghost},
 * {@code link}.
 *
 * <p>Every variant applies a CSS class from the theme, so the button
 * gets the right colour, hover state, and (for outline) border without
 * the caller writing any CSS.</p>
 *
 * <h2>The seven variants</h2>
 * <table border="1">
 *   <caption>Semantic button variants</caption>
 *   <tr><th>Factory</th><th>Look</th><th>Use for</th></tr>
 *   <tr><td>{@link #primary(String)}</td>
 *       <td>Accent-coloured fill</td>
 *       <td>Main action ("Save", "Submit")</td></tr>
 *   <tr><td>{@link #secondary(String)}</td>
 *       <td>Grey fill</td>
 *       <td>Alternative action ("Cancel")</td></tr>
 *   <tr><td>{@link #success(String)}</td>
 *       <td>Green fill</td>
 *       <td>Confirm action</td></tr>
 *   <tr><td>{@link #warning(String)}</td>
 *       <td>Yellow fill</td>
 *       <td>Warning action</td></tr>
 *   <tr><td>{@link #danger(String)}</td>
 *       <td>Red fill</td>
 *       <td>Destructive action ("Delete")</td></tr>
 *   <tr><td>{@link #outline(String)}</td>
 *       <td>Border only, transparent fill</td>
 *       <td>Secondary action in dense UIs</td></tr>
 *   <tr><td>{@link #ghost(String)}</td>
 *       <td>No fill, no border</td>
 *       <td>Icon buttons, inline actions</td></tr>
 * </table>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Row buttons = Row.h(10)
 *     .add(Button.primary("Save").onClick(() -> save()))
 *     .add(Button.outline("Cancel").onClick(() -> cancel()))
 *     .add(Button.danger("Delete").onClick(() -> delete()));
 * }</pre>
 *
 * <h2>Fluent setter vs JavaFX setter</h2>
 * <p>{@code setDisabled} and {@code isDisabled} are {@code final} in
 * JavaFX's {@code Node}, so this class cannot override them. It provides
 * {@link #disabled(boolean)} and {@link #enabled(boolean)} as fluent
 * aliases — they delegate to the built-in {@code setDisable}, and
 * {@code isDisabled()} still works because it is the JavaFX getter.</p>
 *
 * <h2>Related</h2>
 * <p>For the underlying JavaFX button, see
 * {@link javafx.scene.control.Button}. For a button with a dropdown menu,
 * see {@link MenuButton} and {@link SplitMenuButton}. For a button that
 * toggles on click, see {@link ToggleButton}.</p>
 *
 * @see MenuButton
 * @see SplitMenuButton
 * @see ToggleButton
 * @since 1.0.0
 */
public class Button extends javafx.scene.control.Button {

    private Button(String text) { super(text == null ? "" : text); }

    // ============================================================== factories

    /**
     * Creates a plain button with the given text. Uses the default
     * theme styling — no semantic colour.
     *
     * @param text the button text (null → "")
     * @return a new button
     * @see #primary(String)
     * @see #secondary(String)
     * @see #create(String)
     * @see #make(String)
     * @since 1.0.0
     */
    public static Button of(String text) { return new Button(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new button
     * @see #of(String)
     * @since 1.0.0
     */
    public static Button create(String text) { return new Button(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new button
     * @see #of(String)
     * @since 1.0.0
     */
    public static Button button(String text) { return new Button(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new button
     * @see #of(String)
     * @since 1.0.0
     */
    public static Button make(String text) { return new Button(text); }

    /**
     * Creates a primary (accent-coloured) button. The main action.
     *
     * @param text the button text
     * @return a new primary button
     * @see #default_(String)
     * @see #main(String)
     * @since 1.0.0
     */
    public static Button primary(String text) { return new Button(text).variant("primary"); }

    /**
     * Alias of {@link #primary(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new primary button
     * @see #primary(String)
     * @since 1.0.0
     */
    public static Button default_(String text) { return primary(text); }

    /**
     * Alias of {@link #primary(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new primary button
     * @see #primary(String)
     * @since 1.0.0
     */
    public static Button main(String text) { return primary(text); }

    /**
     * Creates a secondary (grey) button. The alternative action.
     *
     * @param text the button text
     * @return a new secondary button
     * @see #alt(String)
     * @see #secondaryButton(String)
     * @since 1.0.0
     */
    public static Button secondary(String text) { return new Button(text).variant("secondary"); }

    /**
     * Alias of {@link #secondary(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new secondary button
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static Button alt(String text) { return secondary(text); }

    /**
     * Alias of {@link #secondary(String)} — see that method for full
     * documentation.
     *
     * @param t the button text
     * @return a new secondary button
     * @see #secondary(String)
     * @since 1.0.0
     */
    public static Button secondaryButton(String t) { return secondary(t); }

    /**
     * Creates a success (green) button. For a confirm action.
     *
     * @param text the button text
     * @return a new success button
     * @see #ok(String)
     * @see #good(String)
     * @since 1.0.0
     */
    public static Button success(String text) { return new Button(text).variant("success"); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new success button
     * @see #success(String)
     * @since 1.0.0
     */
    public static Button ok(String text) { return success(text); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new success button
     * @see #success(String)
     * @since 1.0.0
     */
    public static Button good(String text) { return success(text); }

    /**
     * Creates a warning (yellow) button.
     *
     * @param text the button text
     * @return a new warning button
     * @see #warn(String)
     * @since 1.0.0
     */
    public static Button warning(String text) { return new Button(text).variant("warning"); }

    /**
     * Alias of {@link #warning(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new warning button
     * @see #warning(String)
     * @since 1.0.0
     */
    public static Button warn(String text) { return warning(text); }

    /**
     * Creates a danger (red) button. For destructive actions — delete,
     * remove, reset.
     *
     * @param text the button text
     * @return a new danger button
     * @see #destructive(String)
     * @see #remove(String)
     * @since 1.0.0
     */
    public static Button danger(String text) { return new Button(text).variant("danger"); }

    /**
     * Alias of {@link #danger(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new danger button
     * @see #danger(String)
     * @since 1.0.0
     */
    public static Button destructive(String text) { return danger(text); }

    /**
     * Alias of {@link #danger(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new danger button
     * @see #danger(String)
     * @since 1.0.0
     */
    public static Button remove(String text) { return danger(text); }

    /**
     * Creates an info (blue) button.
     *
     * @param text the button text
     * @return a new info button
     * @since 1.0.0
     */
    public static Button info(String text) { return new Button(text).variant("info"); }

    /**
     * Creates an outline button — border only, no fill. Compact, works
     * well next to a filled primary button.
     *
     * @param text the button text
     * @return a new outline button
     * @see #outlined(String)
     * @see #bordered(String)
     * @since 1.0.0
     */
    public static Button outline(String text) { return new Button(text).variant("outline"); }

    /**
     * Alias of {@link #outline(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new outline button
     * @see #outline(String)
     * @since 1.0.0
     */
    public static Button outlined(String text) { return outline(text); }

    /**
     * Alias of {@link #outline(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new outline button
     * @see #outline(String)
     * @since 1.0.0
     */
    public static Button bordered(String text) { return outline(text); }

    /**
     * Creates a ghost button — no fill, no border, just text. The
     * button appears only on hover.
     *
     * @param text the button text
     * @return a new ghost button
     * @see #invisible(String)
     * @since 1.0.0
     */
    public static Button ghost(String text) { return new Button(text).variant("ghost"); }

    /**
     * Alias of {@link #ghost(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new ghost button
     * @see #ghost(String)
     * @since 1.0.0
     */
    public static Button invisible(String text) { return ghost(text); }

    /**
     * Creates a link-style button — no fill, no border, underlined
     * accent-coloured text. Behaves like a hyperlink but is a button.
     *
     * @param text the button text
     * @return a new link button
     * @see #textLink(String)
     * @see Hyperlink
     * @since 1.0.0
     */
    public static Button link(String text) { return new Button(text).variant("link"); }

    /**
     * Alias of {@link #link(String)} — see that method for full
     * documentation.
     *
     * @param text the button text
     * @return a new link button
     * @see #link(String)
     * @since 1.0.0
     */
    public static Button textLink(String text) { return link(text); }

    // ============================================================== text

    /**
     * Sets the button text.
     *
     * @param t the text (null → "")
     * @return this button
     * @see #label(String)
     * @see #title(String)
     * @since 1.0.0
     */
    public Button text(String t) { setText(t == null ? "" : t); return this; }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this button
     * @see #text(String)
     * @since 1.0.0
     */
    public Button label(String t) { return text(t); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this button
     * @see #text(String)
     * @since 1.0.0
     */
    public Button title(String t) { return text(t); }

    // ============================================================== variant

    /**
     * Adds a CSS class for the given variant. Usually the variants are
     * applied via the static factories ({@link #primary(String)},
     * {@link #danger(String)}, etc.), but this method is available for
     * custom variants.
     *
     * @param v the variant CSS class name (null or blank → no-op)
     * @return this button
     * @since 1.0.0
     */
    public Button variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    /**
     * Alias of {@link #variant(String)} — see that method for full
     * documentation.
     *
     * @param v the variant class name
     * @return this button
     * @see #variant(String)
     * @since 1.0.0
     */
    public Button style(String v) { return variant(v); }

    /**
     * Alias of {@link #variant(String)} — see that method for full
     * documentation.
     *
     * @param v the variant class name
     * @return this button
     * @see #variant(String)
     * @since 1.0.0
     */
    public Button kind(String v) { return variant(v); }

    // ============================================================== fill / outline

    /**
     * Adds the {@code outline} CSS class (border-only look).
     *
     * @return this button
     * @see #outlined()
     * @see #bordered()
     * @see #filled()
     * @since 1.0.0
     */
    public Button outline() {
        if (!getStyleClass().contains("outline")) getStyleClass().add("outline");
        return this;
    }

    /**
     * Alias of {@link #outline()} — see that method for full
     * documentation.
     *
     * @return this button
     * @see #outline()
     * @since 1.0.0
     */
    public Button outlined() { return outline(); }

    /**
     * Alias of {@link #outline()} — see that method for full
     * documentation.
     *
     * @return this button
     * @see #outline()
     * @since 1.0.0
     */
    public Button bordered() { return outline(); }

    /**
     * Removes the {@code outline} CSS class, restoring the filled look.
     *
     * @return this button
     * @see #outline()
     * @since 1.0.0
     */
    public Button filled() {
        getStyleClass().remove("outline");
        return this;
    }

    /**
     * Alias of {@link #filled()} — see that method for full
     * documentation.
     *
     * @return this button
     * @see #filled()
     * @since 1.0.0
     */
    public Button solid() { return filled(); }

    // ============================================================== click

    /**
     * Registers a click handler.
     *
     * <pre>{@code
     * Button.primary("Save").onClick(() -> save());
     * }</pre>
     *
     * @param r the click handler (null → no-op)
     * @return this button
     * @see #onPress(Runnable)
     * @see #on(Runnable)
     * @see #action(Runnable)
     * @see #handle(Runnable)
     * @since 1.0.0
     */
    public Button onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }

    /**
     * Alias of {@link #onClick(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param r the click handler
     * @return this button
     * @see #onClick(Runnable)
     * @since 1.0.0
     */
    public Button onPress(Runnable r) { return onClick(r); }

    /**
     * Alias of {@link #onClick(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param r the click handler
     * @return this button
     * @see #onClick(Runnable)
     * @since 1.0.0
     */
    public Button on(Runnable r) { return onClick(r); }

    /**
     * Alias of {@link #onClick(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param r the click handler
     * @return this button
     * @see #onClick(Runnable)
     * @since 1.0.0
     */
    public Button action(Runnable r) { return onClick(r); }

    /**
     * Alias of {@link #onClick(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param r the click handler
     * @return this button
     * @see #onClick(Runnable)
     * @since 1.0.0
     */
    public Button handle(Runnable r) { return onClick(r); }

    // ============================================================== enable / disable

    /**
     * Enables or disables the button — the fluent alias for
     * {@link javafx.scene.Node#setDisable(boolean)}, which is final and
     * cannot be overridden.
     *
     * @param d whether to disable
     * @return this button
     * @see #enabled(boolean)
     * @see #disable()
     * @see #enable()
     * @since 1.0.0
     */
    public Button disabled(boolean d) { setDisable(d); return this; }

    /**
     * Alias of {@link #disabled(boolean)} with the inverse — see that
     * method for full documentation.
     *
     * @param e whether to enable
     * @return this button
     * @see #disabled(boolean)
     * @since 1.0.0
     */
    public Button enabled(boolean e) { setDisable(!e); return this; }

    /**
     * Alias of {@link #disabled(boolean)} — see that method for full
     * documentation.
     *
     * @param d whether to disable
     * @return this button
     * @see #disabled(boolean)
     * @since 1.0.0
     */
    public Button withDisabled(boolean d) { setDisable(d); return this; }

    /**
     * Disables the button — see {@link #disabled(boolean)}.
     *
     * @return this button
     * @see #disabled(boolean)
     * @since 1.0.0
     */
    public Button disable() { setDisable(true); return this; }

    /**
     * Enables the button — see {@link #disabled(boolean)}.
     *
     * @return this button
     * @see #disabled(boolean)
     * @since 1.0.0
     */
    public Button enable() { setDisable(false); return this; }

    // ============================================================== tooltip

    /**
     * Attaches a tooltip.
     *
     * @param t the tooltip text (null → no-op)
     * @return this button
     * @see #hint(String)
     * @see #tip(String)
     * @see Tooltip
     * @since 1.0.0
     */
    public Button tooltip(String t) {
        if (t != null) setTooltip(new javafx.scene.control.Tooltip(t));
        return this;
    }

    /**
     * Alias of {@link #tooltip(String)} — see that method for full
     * documentation.
     *
     * @param t the tooltip text
     * @return this button
     * @see #tooltip(String)
     * @since 1.0.0
     */
    public Button hint(String t) { return tooltip(t); }

    /**
     * Alias of {@link #tooltip(String)} — see that method for full
     * documentation.
     *
     * @param t the tooltip text
     * @return this button
     * @see #tooltip(String)
     * @since 1.0.0
     */
    public Button tip(String t) { return tooltip(t); }

    // ============================================================== size

    /**
     * Sets the preferred width.
     *
     * @param w the width in pixels
     * @return this button
     * @see #size(double)
     * @since 1.0.0
     */
    public Button width(double w) { setPrefWidth(w); return this; }

    /**
     * Alias of {@link #width(double)} — see that method for full
     * documentation.
     *
     * @param w the width in pixels
     * @return this button
     * @see #width(double)
     * @since 1.0.0
     */
    public Button size(double w) { return width(w); }

    /**
     * Makes the button fill the available horizontal space.
     *
     * @return this button
     * @see #grow()
     * @see #expand()
     * @since 1.0.0
     */
    public Button fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this button
     * @see #fullWidth()
     * @since 1.0.0
     */
    public Button grow() { return fullWidth(); }

    /**
     * Alias of {@link #fullWidth()} — see that method for full
     * documentation.
     *
     * @return this button
     * @see #fullWidth()
     * @since 1.0.0
     */
    public Button expand() { return fullWidth(); }

    // ============================================================== predicates

    /**
     * Checks whether this button has the {@code primary} variant class.
     *
     * @return {@code true} if it is a primary button
     * @since 1.0.0
     */
    public boolean isPrimary() { return getStyleClass().contains("primary"); }

    /**
     * Checks whether this button has the {@code danger} variant class.
     *
     * @return {@code true} if it is a danger button
     * @since 1.0.0
     */
    public boolean isDanger() { return getStyleClass().contains("danger"); }

    /**
     * Checks whether this button has the {@code outline} class applied.
     *
     * @return {@code true} if the button is outlined
     * @since 1.0.0
     */
    public boolean isOutline() { return getStyleClass().contains("outline"); }
}
