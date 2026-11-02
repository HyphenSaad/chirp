package chirp.ui;

/**
 * Fluent label — text display with semantic variants for headings,
 * subheadings, muted text, errors, and success messages.
 *
 * <h2>Static factories</h2>
 * <table border="1">
 *   <caption>Label variants</caption>
 *   <tr><th>Factory</th><th>Look</th><th>Use for</th></tr>
 *   <tr><td>{@link #of(String)}</td><td>Base text</td>
 *       <td>Regular paragraph text</td></tr>
 *   <tr><td>{@link #heading(String)}</td>
 *       <td>Large bold text</td><td>Section titles</td></tr>
 *   <tr><td>{@link #subheading(String)}</td>
 *       <td>Medium bold text</td><td>Subsections</td></tr>
 *   <tr><td>{@link #muted(String)}</td>
 *       <td>Dimmed text</td><td>Secondary information</td></tr>
 *   <tr><td>{@link #error(String)}</td>
 *       <td>Red text</td><td>Error messages</td></tr>
 *   <tr><td>{@link #success(String)}</td>
 *       <td>Green text</td><td>Success messages</td></tr>
 * </table>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Stack layout = Stack.v(8)
 *     .add(Label.heading("Results"))
 *     .add(Label.subheading("Section 1"))
 *     .add(Label.of("Regular text"))
 *     .add(Label.muted("Last updated 2 minutes ago"));
 * }</pre>
 *
 * <h2>Variant replacement vs. stacking</h2>
 * <p>The known variants are {@code heading}, {@code subheading},
 * {@code muted}, {@code error}, and {@code success}. Calling
 * {@link #variant(String)} with one of these <b>replaces</b> the
 * previous one, so a state transition like muted → error leaves only
 * {@code error} applied. For the rare case where you want to
 * <em>add</em> a variant without removing the existing one, use
 * {@link #addVariant(String)}.</p>
 *
 * <h2>Style merging</h2>
 * <p>{@link #bold()}, {@link #italic()}, {@link #fontSize(double)},
 * and {@link #color(String)} merge into the label's inline style
 * rather than overwriting it. {@code label.bold().color("#dc2626")}
 * produces both bold and red, in either order.</p>
 *
 * <h2>Related</h2>
 * <p>For editable text input, see {@link TextField}. For a hyperlink
 * styled like a label, see {@link Hyperlink}. For badges (small
 * pill-shaped status indicators), see {@link Badge}.</p>
 *
 * @see TextField
 * @see Hyperlink
 * @see Badge
 * @since 1.0.0
 */
public class Label extends javafx.scene.control.Label {

    /** The variant classes that {@link #variant(String)} replaces. */
    private static final String[] KNOWN_VARIANTS = {
            "heading", "subheading", "muted", "error", "success"
    };

    private Label(String text) { super(text == null ? "" : text); }

    // ============================================================== static factories

    /**
     * Creates a label with the given text.
     *
     * @param text the text (null → "")
     * @return a new label
     * @see #create(String)
     * @see #make(String)
     * @see #empty()
     * @since 1.0.0
     */
    public static Label of(String text) { return new Label(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new label
     * @see #of(String)
     * @since 1.0.0
     */
    public static Label create(String text) { return new Label(text); }

    /**
     * Alias of {@link #of(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new label
     * @see #of(String)
     * @since 1.0.0
     */
    public static Label make(String text) { return new Label(text); }

    /**
     * Creates an empty label. Useful as a placeholder that will be
     * filled in later.
     *
     * @return a new empty label
     * @since 1.0.0
     */
    public static Label empty() { return new Label(""); }

    /**
     * Creates a heading label — large, bold. For section titles.
     *
     * @param text the text
     * @return a new heading label
     * @see #h1(String)
     * @see #title(String)
     * @see #subheading(String)
     * @since 1.0.0
     */
    public static Label heading(String text) { return new Label(text).variant("heading"); }

    /**
     * Alias of {@link #heading(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new heading label
     * @see #heading(String)
     * @since 1.0.0
     */
    public static Label h1(String text) { return heading(text); }

    /**
     * Alias of {@link #heading(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new heading label
     * @see #heading(String)
     * @since 1.0.0
     */
    public static Label title(String text) { return heading(text); }

    /**
     * Creates a subheading — medium bold. For subsections.
     *
     * @param text the text
     * @return a new subheading label
     * @see #h2(String)
     * @see #heading(String)
     * @since 1.0.0
     */
    public static Label subheading(String text) { return new Label(text).variant("subheading"); }

    /**
     * Alias of {@link #subheading(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new subheading label
     * @see #subheading(String)
     * @since 1.0.0
     */
    public static Label h2(String text) { return subheading(text); }

    /**
     * Creates a muted (dimmed) label — for secondary information that
     * should not compete visually with the main content.
     *
     * @param text the text
     * @return a new muted label
     * @see #dim(String)
     * @since 1.0.0
     */
    public static Label muted(String text) { return new Label(text).variant("muted"); }

    /**
     * Alias of {@link #muted(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new muted label
     * @see #muted(String)
     * @since 1.0.0
     */
    public static Label dim(String text) { return muted(text); }

    /**
     * Creates an error label — red text. For validation error messages.
     *
     * @param text the text
     * @return a new error label
     * @see #err(String)
     * @see #danger(String)
     * @see #success(String)
     * @since 1.0.0
     */
    public static Label error(String text) { return new Label(text).variant("error"); }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new error label
     * @see #error(String)
     * @since 1.0.0
     */
    public static Label err(String text) { return error(text); }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new error label
     * @see #error(String)
     * @since 1.0.0
     */
    public static Label danger(String text) { return error(text); }

    /**
     * Creates a success label — green text. For confirmation messages.
     *
     * @param text the text
     * @return a new success label
     * @see #ok(String)
     * @see #good(String)
     * @see #error(String)
     * @since 1.0.0
     */
    public static Label success(String text) { return new Label(text).variant("success"); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new success label
     * @see #success(String)
     * @since 1.0.0
     */
    public static Label ok(String text) { return success(text); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param text the text
     * @return a new success label
     * @see #success(String)
     * @since 1.0.0
     */
    public static Label good(String text) { return success(text); }

    // ============================================================== instance — text setters

    /**
     * Sets the label text.
     *
     * @param t the text (null → "")
     * @return this label
     * @see #value(String)
     * @see #label(String)
     * @since 1.0.0
     */
    public Label text(String t) { setText(t == null ? "" : t); return this; }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this label
     * @see #text(String)
     * @since 1.0.0
     */
    public Label value(String t) { return text(t); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this label
     * @see #text(String)
     * @since 1.0.0
     */
    public Label label(String t) { return text(t); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param t the text
     * @return this label
     * @see #text(String)
     * @since 1.0.0
     */
    public Label setTextValue(String t) { return text(t); }

    // ============================================================== instance — variant

    /**
     * Sets the label's variant class, replacing any existing known
     * variant. The known variants are: {@code heading},
     * {@code subheading}, {@code muted}, {@code error}, {@code success}.
     *
     * <p>Calling {@code variant("success")} after {@code variant("error")}
     * removes {@code error} before adding {@code success} — the label
     * does <b>not</b> end up with both classes.</p>
     *
     * @param v the variant class name (null or blank → clears all known
     * variants but adds nothing)
     * @return this label
     * @see #addVariant(String)
     * @see #style(String)
     * @since 1.0.0
     */
    public Label variant(String v) {
        for (String known : KNOWN_VARIANTS) getStyleClass().remove(known);
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    /**
     * Alias of {@link #variant(String)} — see that method for full
     * documentation.
     *
     * @param v the variant class name
     * @return this label
     * @see #variant(String)
     * @since 1.0.0
     */
    public Label style(String v) { return variant(v); }

    /**
     * Alias of {@link #variant(String)} — see that method for full
     * documentation.
     *
     * @param v the variant class name
     * @return this label
     * @see #variant(String)
     * @since 1.0.0
     */
    public Label kind(String v) { return variant(v); }

    /**
     * Adds a variant class <b>without</b> removing the existing one.
     * Use this only when you explicitly want to combine variants — for
     * example, a heading that is also an error.
     *
     * @param v the class to add (null or blank → ignored)
     * @return this label
     * @see #variant(String)
     * @since 1.0.0
     */
    public Label addVariant(String v) {
        if (v != null && !v.isBlank() && !getStyleClass().contains(v)) {
            getStyleClass().add(v);
        }
        return this;
    }

    // ============================================================== instance — typography

    /**
     * Applies bold font weight via inline CSS. Merges with any existing
     * inline style.
     *
     * @return this label
     * @see #strong()
     * @since 1.0.0
     */
    public Label bold() { return addStyle("-fx-font-weight", "bold"); }

    /**
     * Alias of {@link #bold()} — see that method for full
     * documentation.
     *
     * @return this label
     * @see #bold()
     * @since 1.0.0
     */
    public Label strong() { return bold(); }

    /**
     * Applies italic font style via inline CSS. Merges with any existing
     * inline style.
     *
     * @return this label
     * @see #emph()
     * @since 1.0.0
     */
    public Label italic() { return addStyle("-fx-font-style", "italic"); }

    /**
     * Alias of {@link #italic()} — see that method for full
     * documentation.
     *
     * @return this label
     * @see #italic()
     * @since 1.0.0
     */
    public Label emph() { return italic(); }

    /**
     * Enables word wrap. Useful for long paragraphs and error
     * messages.
     *
     * @return this label
     * @see #wrapText()
     * @see #wrapping()
     * @since 1.0.0
     */
    public Label wrap() {
        setWrapText(true);
        return this;
    }

    /**
     * Alias of {@link #wrap()} — see that method for full
     * documentation.
     *
     * @return this label
     * @see #wrap()
     * @since 1.0.0
     */
    public Label wrapText() { return wrap(); }

    /**
     * Alias of {@link #wrap()} — see that method for full
     * documentation.
     *
     * @return this label
     * @see #wrap()
     * @since 1.0.0
     */
    public Label wrapping() { return wrap(); }

    /**
     * Sets the font size via inline CSS. Merges with any existing inline
     * style.
     *
     * @param size the font size in pixels
     * @return this label
     * @see #size(double)
     * @see #font(double)
     * @since 1.0.0
     */
    public Label fontSize(double size) { return addStyle("-fx-font-size", size + "px"); }

    /**
     * Alias of {@link #fontSize(double)} — see that method for full
     * documentation.
     *
     * @param s the font size
     * @return this label
     * @see #fontSize(double)
     * @since 1.0.0
     */
    public Label size(double s) { return fontSize(s); }

    /**
     * Alias of {@link #fontSize(double)} — see that method for full
     * documentation.
     *
     * @param s the font size
     * @return this label
     * @see #fontSize(double)
     * @since 1.0.0
     */
    public Label font(double s) { return fontSize(s); }

    // ============================================================== instance — colour

    /**
     * Sets the text colour via inline CSS. Merges with any existing
     * inline style.
     *
     * @param hex the hex colour (e.g. {@code "#ff0000"})
     * @return this label
     * @see #foreground(String)
     * @since 1.0.0
     */
    public Label color(String hex) {
        if (hex != null && !hex.isBlank()) addStyle("-fx-text-fill", hex);
        return this;
    }

    /**
     * Alias of {@link #color(String)} — see that method for full
     * documentation.
     *
     * @param hex the hex colour
     * @return this label
     * @see #color(String)
     * @since 1.0.0
     */
    public Label foreground(String hex) { return color(hex); }

    // ============================================================== predicates

    /**
     * Checks whether this label has the {@code heading} variant class.
     *
     * @return {@code true} if it is a heading
     * @since 1.0.0
     */
    public boolean isHeading() { return getStyleClass().contains("heading"); }

    /**
     * Checks whether this label has the {@code subheading} variant class.
     *
     * @return {@code true} if it is a subheading
     * @since 1.0.0
     */
    public boolean isSubheading() { return getStyleClass().contains("subheading"); }

    /**
     * Checks whether this label has the {@code muted} variant class.
     *
     * @return {@code true} if it is muted
     * @since 1.0.0
     */
    public boolean isMuted() { return getStyleClass().contains("muted"); }

    /**
     * Checks whether this label has the {@code error} variant class.
     *
     * @return {@code true} if it is an error label
     * @since 1.0.0
     */
    public boolean isError() { return getStyleClass().contains("error"); }

    /**
     * Checks whether this label has the {@code success} variant class.
     *
     * @return {@code true} if it is a success label
     * @since 1.0.0
     */
    public boolean isSuccess() { return getStyleClass().contains("success"); }

    // ============================================================== internal — style merging

    /**
     * Adds or replaces one inline style rule, preserving the rest. This
     * is what lets {@code bold().color(...)} produce both bold and
     * colour instead of one overwriting the other.
     */
    private Label addStyle(String property, String value) {
        java.util.Map<String, String> rules = new java.util.LinkedHashMap<>();
        String current = getStyle() == null ? "" : getStyle();
        for (String rule : current.split(";")) {
            rule = rule.trim();
            if (rule.isEmpty()) continue;
            int colon = rule.indexOf(':');
            if (colon > 0) {
                rules.put(rule.substring(0, colon).trim(),
                        rule.substring(colon + 1).trim());
            }
        }
        rules.put(property, value);
        StringBuilder sb = new StringBuilder();
        rules.forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
        setStyle(sb.toString().trim());
        return this;
    }
}
