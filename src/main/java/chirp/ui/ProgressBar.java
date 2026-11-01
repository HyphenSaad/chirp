package chirp.ui;

/**
 * Fluent horizontal progress bar.
 *
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Determinate</b> — {@link #of(double)} creates a bar at the
 *       given percentage; {@link #value(double)} sets progress from
 *       0.0 to 1.0</li>
 *   <li><b>Indeterminate</b> — {@link #indeterminate()} creates an
 *       animated "loading" bar with no percentage</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ProgressBar.of(0.4);          // 40% filled
 * ProgressBar.indeterminate();  // animated loading
 * bar.value(0.75);              // → 75% (determinate)
 * bar.value(-1);                // → indeterminate
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the circular version, see {@link ProgressIndicator}.</p>
 *
 * @see ProgressIndicator
 * @since 1.0.0
 */
public class ProgressBar extends javafx.scene.control.ProgressBar {

    /**
     * Creates a determinate bar at 0%.
     *
     * @return a new progress bar
     * @see #of(double)
     * @see #indeterminate()
     * @since 1.0.0
     */
    public static ProgressBar of() { return new ProgressBar(0); }

    /**
     * Creates a determinate bar at the given progress (clamped to 0–1).
     *
     * @param value the initial progress
     * @return a new progress bar
     * @see #of()
     * @since 1.0.0
     */
    public static ProgressBar of(double value) {
        return new ProgressBar(clampDeterminate(value));
    }

    /**
     * Creates an indeterminate (animated "loading") bar.
     *
     * @return a new indeterminate progress bar
     * @see #of()
     * @since 1.0.0
     */
    public static ProgressBar indeterminate() { return new ProgressBar(-1); }

    /**
     * Constructs a progress bar at the given progress. Usually use the
     * static factories instead.
     *
     * @param progress the initial progress (-1 → indeterminate)
     * @since 1.0.0
     */
    public ProgressBar(double progress) { super(progress); }

    /**
     * Sets the progress. Non-negative values become determinate (clamped
     * to 0–1); negative values switch to indeterminate.
     *
     * @param v the progress (-1 → indeterminate)
     * @return this bar, for chaining
     * @see #percent(double)
     * @see #makeIndeterminate()
     * @since 1.0.0
     */
    public ProgressBar value(double v) {
        setProgress(v < 0 ? -1 : clampDeterminate(v));
        return this;
    }

    /**
     * Sets progress by percentage — 0 to 100. Values &lt; 0 become
     * indeterminate.
     *
     * @param pct the percentage
     * @return this bar
     * @since 1.0.0
     */
    public ProgressBar percent(double pct) {
        return value(pct < 0 ? -1 : pct / 100.0);
    }

    /**
     * Alias of {@link #value(double)} — see that method for full
     * documentation.
     *
     * @param v the progress
     * @return this bar
     * @see #value(double)
     * @since 1.0.0
     */
    public ProgressBar progress(double v) { return value(v); }

    /**
     * Switches this bar to indeterminate (animated) mode.
     *
     * @return this bar
     * @since 1.0.0
     */
    public ProgressBar makeIndeterminate() {
        setProgress(-1);
        return this;
    }

    /**
     * Makes the bar fill the available horizontal space.
     *
     * @return this bar
     * @since 1.0.0
     */
    public ProgressBar fullWidth() {
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }

    /**
     * Returns the current progress — 0 to 1, or -1 for indeterminate.
     *
     * @return the progress
     * @since 1.0.0
     */
    public double current() { return getProgress(); }

    private static double clampDeterminate(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
