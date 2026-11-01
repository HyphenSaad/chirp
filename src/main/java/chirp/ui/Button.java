package chirp.ui;

/**
 * Fluent button.
 *
 * <p><b>Naming note:</b> {@code setDisabled} and {@code isDisabled} are
 * <b>final</b> in JavaFX's {@code Node}. This class provides
 * {@link #disabled(boolean)} and {@link #enabled(boolean)} as fluent aliases
 * that delegate to the built-in {@code setDisable} and use the built-in
 * {@code isDisable()} getter.</p>
 */
public class Button extends javafx.scene.control.Button {

    private Button(String text) { super(text == null ? "" : text); }

    // ============================================================== factories

    public static Button of(String text) { return new Button(text); }
    public static Button create(String text) { return new Button(text); }
    public static Button button(String text) { return new Button(text); }
    public static Button make(String text) { return new Button(text); }

    public static Button primary(String text) { return new Button(text).variant("primary"); }
    public static Button default_(String text) { return primary(text); }
    public static Button main(String text) { return primary(text); }

    public static Button secondary(String text) { return new Button(text).variant("secondary"); }
    public static Button alt(String text) { return secondary(text); }
    public static Button secondaryButton(String t) { return secondary(t); }

    public static Button success(String text) { return new Button(text).variant("success"); }
    public static Button ok(String text) { return success(text); }
    public static Button good(String text) { return success(text); }

    public static Button warning(String text) { return new Button(text).variant("warning"); }
    public static Button warn(String text) { return warning(text); }

    public static Button danger(String text) { return new Button(text).variant("danger"); }
    public static Button destructive(String text) { return danger(text); }
    public static Button remove(String text) { return danger(text); }

    public static Button info(String text) { return new Button(text).variant("info"); }

    public static Button outline(String text) { return new Button(text).variant("outline"); }
    public static Button outlined(String text) { return outline(text); }
    public static Button bordered(String text) { return outline(text); }

    public static Button ghost(String text) { return new Button(text).variant("ghost"); }
    public static Button invisible(String text) { return ghost(text); }

    public static Button link(String text) { return new Button(text).variant("link"); }
    public static Button textLink(String text) { return link(text); }

    // ============================================================== text

    public Button text(String t) { setText(t == null ? "" : t); return this; }
    public Button label(String t) { return text(t); }
    public Button title(String t) { return text(t); }

    // ============================================================== variant

    public Button variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }
    public Button style(String v) { return variant(v); }
    public Button kind(String v) { return variant(v); }

    // ============================================================== fill / outline

    public Button outline() {
        if (!getStyleClass().contains("outline")) getStyleClass().add("outline");
        return this;
    }
    public Button outlined() { return outline(); }
    public Button bordered() { return outline(); }

    public Button filled() {
        getStyleClass().remove("outline");
        return this;
    }
    public Button solid() { return filled(); }

    // ============================================================== click

    public Button onClick(Runnable r) {
        if (r != null) setOnAction(e -> r.run());
        return this;
    }
    public Button onPress(Runnable r) { return onClick(r); }
    public Button on(Runnable r) { return onClick(r); }
    public Button action(Runnable r) { return onClick(r); }
    public Button handle(Runnable r) { return onClick(r); }

    // ============================================================== enable / disable

    public Button disabled(boolean d) { setDisable(d); return this; }
    public Button enabled(boolean e) { setDisable(!e); return this; }
    public Button withDisabled(boolean d) { setDisable(d); return this; }
    public Button disable() { setDisable(true); return this; }
    public Button enable() { setDisable(false); return this; }

    // ============================================================== tooltip

    public Button tooltip(String t) {
        if (t != null) setTooltip(new javafx.scene.control.Tooltip(t));
        return this;
    }
    public Button hint(String t) { return tooltip(t); }
    public Button tip(String t) { return tooltip(t); }

    // ============================================================== size

    public Button width(double w) { setPrefWidth(w); return this; }
    public Button size(double w) { return width(w); }

    public Button fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }
    public Button grow() { return fullWidth(); }
    public Button expand() { return fullWidth(); }

    // ============================================================== predicates
    // (uses JavaFX's isDisable() getter — cannot override isDisabled())

    public boolean isPrimary() { return getStyleClass().contains("primary"); }
    public boolean isDanger() { return getStyleClass().contains("danger"); }
    public boolean isOutline() { return getStyleClass().contains("outline"); }
}
