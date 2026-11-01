package chirp;

/**
 * Thrown when a Chirp feature is used before {@link chirp.Chirp#start()}
 * has been called.
 *
 * <p>Every decorated console method, every input method, and every
 * lifecycle-dependent operation requires Chirp to be started first. The
 * message embedded in this exception tells the student exactly what to
 * add — usually a single line, {@code Chirp.start()}, at the top of
 * their {@code main} method.</p>
 *
 * <h2>Typical message</h2>
 * <pre>{@code
 * Chirp has not been started yet.
 *
 * Add this as the FIRST line of your main method:
 *
 *     Chirp.start();
 *
 * Or with configuration:
 *
 *     Chirp.start(
 *         StartupConfig.builder()
 *             .noHeader()
 *             .withStats()
 *             .build()
 *     );
 *
 * Other entry points (all equivalent): init(), boot(), setup(),
 * begin(), initialize(). Learn more at:
 *     https://github.com/hyphensaad/chirp
 * }</pre>
 *
 * <h2>When it is thrown</h2>
 * <ul>
 *   <li>Any method on {@link chirp.console.Console} that decorates
 *       output — {@code Console.success(...)},
 *       {@code Console.title(...)}, {@code Console.line(...)}</li>
 *   <li>Any method on {@link chirp.console.Input} —
 *       {@code Input.readInt(...)}, {@code Input.readLine(...)},
 *       {@code Input.readDate(...)}</li>
 *   <li>Anything else guarded by
 *       {@link chirp.startup.ChirpLifecycle#requireStarted()}</li>
 * </ul>
 *
 * <p>Raw printing ({@code Console.println}, {@code Console.print}) and
 * style helpers ({@code Console.bold}, {@code Console.red}) work
 * without starting Chirp — they are safe to use in tests and in plain
 * library code.</p>
 *
 * <h2>How to fix it</h2>
 * <p>Add {@code Chirp.start();} as the first line of {@code main}. If
 * the exception fires from a method other than {@code main}, the start
 * call needs to happen earlier — usually in {@code main} or in a static
 * initializer that runs before anything touches Chirp.</p>
 *
 * <h2>Relationship to other exceptions</h2>
 * <p>Extends {@link IllegalStateException} because calling a method
 * before its required lifecycle stage is precisely a state error. It is
 * the only checked-in-spirit exception the library throws from user
 * code — everything else either returns a safe default or throws a
 * module-specific {@code RuntimeException} subclass
 * ({@code HttpException}, {@code EmailException},
 * {@code JsonException}, {@code EaseException}).</p>
 *
 * @see chirp.Chirp#start()
 * @see chirp.Chirp#isStarted()
 * @see chirp.startup.ChirpLifecycle#requireStarted()
 * @since 1.0.0
 */
public class ChirpNotStartedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message — usually the full STARTUP_HINT
     * text produced by {@link chirp.startup.ChirpLifecycle}
     * @since 1.0.0
     */
    public ChirpNotStartedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     * @since 1.0.0
     */
    public ChirpNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}
