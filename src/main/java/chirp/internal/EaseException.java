package chirp.internal;

/**
 * The only unchecked exception the library throws internally. Signals a
 * programming error (e.g. stdin closed unexpectedly, or a reader
 * receiving empty options) — never a user mistake.
 *
 * <p><b>Internal API.</b> Do not reference directly. Student code
 * interacts with the library's exceptions through
 * {@link chirp.ChirpNotStartedException} (lifecycle),
 * {@link chirp.http.HttpException} (HTTP), or
 * {@link chirp.json.JsonException} (JSON) — all of which extend
 * {@link RuntimeException} independently.</p>
 *
 * <h2>Why a separate type</h2>
 * <p>Every public method in the library catches its own exceptions and
 * returns a safe default. {@code EaseException} is the one signal that
 * <b>must</b> propagate — a broken stdin, a missing required argument
 * to a low-level primitive, or similar setup-level failure. Making it
 * a distinct type means the caller can branch on "was this a user
 * input error (already reported) or a setup error (needs a fix)?"</p>
 *
 * @see chirp.ChirpNotStartedException
 * @since 1.0.0
 */
public final class EaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ease exception with the given message.
     *
     * @param message the detail message
     * @since 1.0.0
     */
    public EaseException(String message) {
        super(message);
    }

    /**
     * Constructs a new ease exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     * @since 1.0.0
     */
    public EaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
