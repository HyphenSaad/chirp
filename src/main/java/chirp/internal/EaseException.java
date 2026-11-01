package chirp.internal;

/**
 * The only unchecked exception the library ever throws. It signals a
 * programming error (e.g. stdin closed) — never a user mistake.
 *
 * <p><strong>Internal API — do not use directly.</strong></p>
 */
public final class EaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EaseException(String message) {
        super(message);
    }

    public EaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
