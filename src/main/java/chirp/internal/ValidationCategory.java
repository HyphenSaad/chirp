package chirp.internal;

/**
 * The categories of validation a reader tracks for strict-mode warnings.
 *
 * <p><strong>Internal API — do not reference directly.</strong></p>
 */
public enum ValidationCategory {
    RANGE,
    BLANK,
    LENGTH,
    FORMAT,
    CUSTOM
}
