package chirp.ui;

/**
 * Fluent circular progress indicator.
 *
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Determinate</b> — {@link #of(double)} creates a ring at the
 *       given percentage</li>
 *   <li><b>Indeterminate</b> — {@link #indeterminate()} creates an
 *       animated spinner ring</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ProgressIndicator.of(0.65).size(56);       // 65% ring
 * ProgressIndicator.indeterminate().size(48); // spinner
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the linear version, see {@link ProgressBar}.</p>
 *
 * @see ProgressBar
 * @since 1.0.0
 */
public class ProgressIndicator extends javafx.scene.control.ProgressIndicator {

    private ProgressIndicator(double progress) { super(progress); }

    /**
     * Creates a determinate ring at 0%.
     *
     * @return a new progress indicator
     * @see #of(double)
     * @see #indeterminate()
     * @since 1.0.0
     */
    public static ProgressIndicator of() { return new ProgressIndicator(0); }

    /**
     * Creates a determinate ring at the given progress (clamped to 0–1).
     *
     * @param v the initial progress
     * @return a new progress indicator
     * @see #of()
     * @since 1.0.0
     */
    public static ProgressIndicator of(double v) {
        return new ProgressIndicator(clampDeterminate(v));
    }

    /**
     * Creates an animated indeterminate spinner.
     *
     * @return a new indeterminate progress indicator
     * @since 1.0.0
     */
    public static ProgressIndicator indeterminate() {
        return new ProgressIndicator(-1);
    }

    /**
     * Sets the progress. Non-negative values become determinate (clamped
     * to 0–1); negative values switch to indeterminate.
     *
     * @param v the progress (-1 → indeterminate)
     * @return this indicator, for chaining
     * @since 1.0.0
     */
    public ProgressIndicator value(double v) {
        setProgress(v < 0 ? -1 : clampDeterminate(v));
        return this;
    }

    /**
     * Sets progress by percentage — 0 to 100. Values &lt; 0 become
     * indeterminate.
     *
     * @param p the percentage
     * @return this indicator
     * @since 1.0.0
     */
    public ProgressIndicator percent(double p) {
        return value(p < 0 ? -1 : p / 100.0);
    }

    /**
     * Switches to indeterminate mode.
     *
     * @return this indicator
     * @since 1.0.0
     */
    public ProgressIndicator makeIndeterminate() {
        setProgress(-1);
        return this;
    }

    /**
     * Sets the ring's square size.
     *
     * @param px the size in pixels
     * @return this indicator
     * @since 1.0.0
     */
    public ProgressIndicator size(double px) {
        setPrefSize(px, px);
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
