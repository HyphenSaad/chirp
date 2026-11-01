package chirp.ui;

/**
 * Fluent circular progress indicator.
 *
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Determinate</b> — {@code ProgressIndicator.of(0.65)} shows a 65% ring</li>
 *   <li><b>Indeterminate</b> — {@code ProgressIndicator.indeterminate()} shows
 *       an animated spinning ring</li>
 * </ul>
 *
 * <p>To switch an existing ring to indeterminate at runtime, pass a negative
 * value: {@code ring.value(-1)}.</p>
 *
 * <pre>{@code
 * ProgressIndicator.of(0.65).size(56);       // 65% ring
 * ProgressIndicator.indeterminate().size(48); // spinner
 *
 * ring.value(0.3);   // → 30%
 * ring.value(-1);    // → indeterminate
 * }</pre>
 */
public class ProgressIndicator extends javafx.scene.control.ProgressIndicator {

    private ProgressIndicator(double progress) { super(progress); }

    /** Determinate ring at 0%. */
    public static ProgressIndicator of() { return new ProgressIndicator(0); }

    /** Determinate ring at the given progress (clamped 0–1). */
    public static ProgressIndicator of(double v) {
        return new ProgressIndicator(clampDeterminate(v));
    }

    /** Animated indeterminate spinner. */
    public static ProgressIndicator indeterminate() {
        return new ProgressIndicator(-1);
    }

    // ---------------------------------------------------------- chaining

    /**
     * Sets progress. Non-negative values become determinate (clamped 0–1);
     * negative values switch to indeterminate.
     */
    public ProgressIndicator value(double v) {
        setProgress(v < 0 ? -1 : clampDeterminate(v));
        return this;
    }

    /** Sets progress by percentage — 0 to 100. Values &lt;0 become indeterminate. */
    public ProgressIndicator percent(double p) {
        return value(p < 0 ? -1 : p / 100.0);
    }

    /** Switches this ring to indeterminate (animated) mode. */
    public ProgressIndicator makeIndeterminate() {
        setProgress(-1);
        return this;
    }

    /** Sets the ring's square size in pixels. */
    public ProgressIndicator size(double px) {
        setPrefSize(px, px);
        return this;
    }

    /** @return current progress — 0 to 1, or -1 for indeterminate. */
    public double current() { return getProgress(); }

    // Note: isIndeterminate() is inherited from
    // javafx.scene.control.ProgressIndicator and is declared `final`.
    // It returns getProgress() < 0 — exactly the behaviour we need.
    // No override is required.

    // ---------------------------------------------------------- helpers

    private static double clampDeterminate(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
