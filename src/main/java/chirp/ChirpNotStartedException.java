package chirp;

/**
 * Thrown when a Chirp feature is used before {@link chirp.Chirp#start()} has
 * been called. The message tells the student exactly what to do.
 */
public class ChirpNotStartedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public ChirpNotStartedException(String message) {
        super(message);
    }

    public ChirpNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}
