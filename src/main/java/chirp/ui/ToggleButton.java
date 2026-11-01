package chirp.ui;

import java.util.function.Consumer;

/**
 * Toggle button — stays pressed when clicked, useful for radio-like groups
 * or on/off switches.
 *
 * <pre>{@code
 * ToggleGroup group = new ToggleGroup();
 * ToggleButton b1 = ToggleButton.primary("Bold").group(group);
 * ToggleButton b2 = ToggleButton.primary("Italic").group(group);
 * }</pre>
 */
public class ToggleButton extends javafx.scene.control.ToggleButton {

    private ToggleButton(String text) { super(text == null ? "" : text); }

    public static ToggleButton of(String text) { return new ToggleButton(text); }
    public static ToggleButton primary(String text) { return new ToggleButton(text).variant("primary"); }
    public static ToggleButton secondary(String text) { return new ToggleButton(text).variant("secondary"); }
    public static ToggleButton success(String text) { return new ToggleButton(text).variant("success"); }
    public static ToggleButton danger(String text) { return new ToggleButton(text).variant("danger"); }

    public ToggleButton variant(String v) {
        if (v != null && !v.isBlank()) getStyleClass().add(v);
        return this;
    }

    public ToggleButton selected(boolean s) { setSelected(s); return this; }
    public ToggleButton group(javafx.scene.control.ToggleGroup tg) { setToggleGroup(tg); return this; }
    public ToggleButton onClick(Runnable r) { if (r != null) setOnAction(e -> r.run()); return this; }

    public ToggleButton onChange(Consumer<Boolean> consumer) {
        if (consumer != null) selectedProperty().addListener((obs, o, n) -> consumer.accept(n));
        return this;
    }

    public ToggleButton fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
