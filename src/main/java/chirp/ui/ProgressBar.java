package chirp.ui;

/**
 * Fluent progress bar.
 *
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Determinate</b> — {@code ProgressBar.of(0.5)} shows 50% filled;
 *       {@code of()} starts at 0% ready for updates</li>
 *   <li><b>Indeterminate</b> — {@code ProgressBar.indeterminate()} shows an
 *       animated "loading" bar with no percentage</li>
 * </ul>
 *
 * <p>To switch an existing bar to indeterminate at runtime, pass a negative
 * value: {@code bar.value(-1)}. Any non-negative value switches it back to
 * determinate mode.</p>
 *
 * <pre>{@code
 * ProgressBar.of(0.75);         // 75% filled
 * ProgressBar.of();             // 0% filled (ready for updates)
 * ProgressBar.indeterminate();  // animated loading bar
 *
 * bar.value(0.4);               // → determinate 40%
 * bar.value(-1);                // → indeterminate
 * }</pre>
 */
public class ProgressBar extends javafx.scene.control.ProgressBar {

    /** Creates a determinate bar at 0%. */
    public static ProgressBar of() { return new ProgressBar(0); }

    /** Creates a determinate bar at the given progress (clamped to 0–1). */
    public static ProgressBar of(double value) {
        return new ProgressBar(clampDeterminate(value));
    }

    /**
     * Creates an indeterminate (animated "loading") bar.
     *
     * <p>Static factory — returns a new bar. To convert an existing bar,
     * use {@link #value(double) value(-1)}.</p>
     */
    public static ProgressBar indeterminate() { return new ProgressBar(-1); }

    public ProgressBar(double progress) { super(progress); }

    // ---------------------------------------------------------- chaining

    /**
     * Sets progress. Non-negative values become determinate (clamped 0–1);
     * negative values switch to indeterminate.
     */
    public ProgressBar value(double v) {
        setProgress(v < 0 ? -1 : clampDeterminate(v));
        return this;
    }

    /** Sets progress by percentage — 0 to 100. Values &lt;0 become indeterminate. */
    public ProgressBar percent(double pct) {
        return value(pct < 0 ? -1 : pct / 100.0);
    }

    /** Alias for {@link #value(double)} — expresses intent clearly. */
    public ProgressBar progress(double v) {
        return value(v);
    }

    /** Switches this bar to indeterminate (animated) mode. */
    public ProgressBar makeIndeterminate() {
        setProgress(-1);
        return this;
    }

    /** Makes the bar fill available horizontal space. */
    public ProgressBar fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /** @return current progress — 0 to 1, or -1 for indeterminate. */
    public double current() {
        return getProgress();
    }

    // Note: isIndeterminate() is inherited from javafx.scene.control.ProgressBar
    // and is declared `final`. It returns getProgress() < 0 — exactly the
    // behaviour we need. No override is required.

    // ---------------------------------------------------------- helpers

    private static double clampDeterminate(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
