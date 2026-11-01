package chirp.ui;

/**
 * Fluent label.
 *
 * <h2>Factories (static)</h2>
 * <p>{@code of / create / make / empty};
 * {@code heading / h1 / title}; {@code subheading / h2};
 * {@code muted / dim}; {@code error / err / danger};
 * {@code success / ok / good}.</p>
 *
 * <p>Note: there is no static {@code label(String)} or {@code text(String)}
 * factory — those names are reserved for the instance setter, so
 * {@code Label.of("x").text("y").label("z")} is unambiguous.</p>
 *
 * <h2>Setters (instance)</h2>
 * <p>{@code text / value / label / setText}; {@code variant / style / kind};
 * {@code bold / strong}; {@code italic / emph}; {@code wrap / wrapText};
 * {@code fontSize / size / font}; {@code color / foreground}.</p>
 */
public class Label extends javafx.scene.control.Label {

    private Label(String text) { super(text == null ? "" : text); }

    // ============================================================== static factories

    public static Label of(String text) { return new Label(text); }
    public static Label create(String text) { return new Label(text); }
    public static Label make(String text) { return new Label(text); }
    public static Label empty() { return new Label(""); }

    public static Label heading(String text) { return new Label(text).variant("heading"); }
    public static Label h1(String text) { return heading(text); }
    public static Label title(String text) { return heading(text); }

    public static Label subheading(String text) { return new Label(text).variant("subheading"); }
    public static Label h2(String text) { return subheading(text); }

    public static Label muted(String text) { return new Label(text).variant("muted"); }
    public static Label dim(String text) { return muted(text); }

    public static Label error(String text) { return new Label(text).variant("error"); }
    public static Label err(String text) { return error(text); }
    public static Label danger(String text) { return error(text); }

    public static Label success(String text) { return new Label(text).variant("success"); }
    public static Label ok(String text) { return success(text); }
    public static Label good(String text) { return success(text); }

    // ============================================================== instance — text setters

    public Label text(String t) { setText(t == null ? "" : t); return this; }
    public Label value(String t) { return text(t); }
    public Label label(String t) { return text(t); }
    public Label setTextValue(String t) { return text(t); }

    // ============================================================== instance — variant

    public Label variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }
    public Label style(String v) { return variant(v); }
    public Label kind(String v) { return variant(v); }

    // ============================================================== instance — typography

    public Label bold() {
        setStyle("-fx-font-weight: bold;");
        return this;
    }
    public Label strong() { return bold(); }

    public Label italic() {
        setStyle("-fx-font-style: italic;");
        return this;
    }
    public Label emph() { return italic(); }

    public Label wrap() {
        setWrapText(true);
        return this;
    }
    public Label wrapText() { return wrap(); }
    public Label wrapping() { return wrap(); }

    public Label fontSize(double size) {
        setStyle("-fx-font-size: " + size + "px;");
        return this;
    }
    public Label size(double s) { return fontSize(s); }
    public Label font(double s) { return fontSize(s); }

    // ============================================================== instance — colour

    public Label color(String hex) {
        if (hex != null && !hex.isBlank()) setStyle("-fx-text-fill: " + hex + ";");
        return this;
    }
    public Label foreground(String hex) { return color(hex); }

    // ============================================================== predicates

    public boolean isHeading() { return getStyleClass().contains("heading"); }
    public boolean isSubheading() { return getStyleClass().contains("subheading"); }
    public boolean isMuted() { return getStyleClass().contains("muted"); }
    public boolean isError() { return getStyleClass().contains("error"); }
    public boolean isSuccess() { return getStyleClass().contains("success"); }
}
