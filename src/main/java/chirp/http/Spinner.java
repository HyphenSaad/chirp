package chirp.http;

import chirp.internal.Capabilities;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A tiny terminal spinner for HTTP requests that take time.
 *
 * <p>Runs on a daemon thread; writes {@code \r} + a frame character to
 * {@link System#out} so it overwrites itself in place. Uses Unicode
 * frames when the terminal supports them, ASCII {@code | / - \} frames
 * otherwise.</p>
 *
 * <h2>Synchronization</h2>
 * <p>A private {@link #PRINT_LOCK} serialises output between the spinner
 * thread and any other thread that needs to clear the line. Using
 * {@code System.out} as a monitor would globally expose the lock —
 * another class that synchronises on {@code System.out} could
 * accidentally hold it and cause a deadlock. The private lock avoids
 * that entirely.</p>
 *
 * <h2>Usage</h2>
 * <p>{@link HttpRequest} creates and stops spinners automatically when
 * {@code .withSpinner()} is set — students rarely construct one
 * directly.</p>
 *
 * <pre>{@code
 * Spinner s = Spinner.start("Fetching data");
 * try {
 *     // ... do the slow thing ...
 * } finally {
 *     s.stop();
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For a fuller-featured spinner with result-printing helpers, see
 * {@link chirp.console.Loader}.</p>
 *
 * @see chirp.console.Loader
 * @see HttpRequest#withSpinner()
 * @since 1.0.0
 */
public final class Spinner {

    private static final String[] UNICODE_FRAMES = {
            "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
    };
    private static final String[] ASCII_FRAMES = {"|", "/", "-", "\\"};

    /** Private lock — output serialization without exposing {@code System.out}. */
    private static final Object PRINT_LOCK = new Object();

    private final String label;
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private Spinner(String label) {
        this.label = label == null ? "" : label;
        this.thread = new Thread(this::loop, "chirp-http-spinner");
        this.thread.setDaemon(true);
    }

    /**
     * Starts a spinner with the given label and returns it. The spinner
     * runs on a daemon thread until {@link #stop()} is called.
     *
     * @param label the label to display next to the spinning frame (may
     * be null → empty)
     * @return the running spinner
     * @see #stop()
     * @since 1.0.0
     */
    public static Spinner start(String label) {
        Spinner spinner = new Spinner(label);
        spinner.thread.start();
        return spinner;
    }

    private void loop() {
        String[] frames = Capabilities.unicode() ? UNICODE_FRAMES : ASCII_FRAMES;
        int frame = 0;
        while (running.get()) {
            synchronized (PRINT_LOCK) {
                System.out.print("\r" + frames[frame % frames.length] + " " + label);
                System.out.flush();
            }
            frame++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Stops the spinner and clears its line from the terminal. Idempotent
     * — calling twice is safe.
     *
     * @see #start(String)
     * @since 1.0.0
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        try {
            thread.join(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        synchronized (PRINT_LOCK) {
            System.out.print("\r" + " ".repeat(Math.min(label.length() + 4, 200)) + "\r");
            System.out.flush();
        }
    }
}
