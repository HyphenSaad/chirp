package chirp.console;

import chirp.internal.Ansi;
import chirp.internal.Capabilities;
import chirp.internal.Glyphs;
import chirp.internal.Trace;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A terminal spinner for operations that take noticeable time — HTTP
 * requests, SMTP sends, large file copies.
 *
 * <p>Runs on a daemon thread; writes {@code \r} + a frame character +
 * the label to {@link System#out} so the spinner overwrites itself in
 * place. Uses Unicode braille frames when the terminal supports them
 * ({@code ⠋ ⠙ ⠹ …}), ASCII {@code | / - \} frames otherwise.</p>
 *
 * <h2>Two patterns</h2>
 *
 * <h3>1. Manual — start, do work, stop (or success/error)</h3>
 * <pre>{@code
 * Loader l = Loader.start("Fetching data");
 * try {
 *     HttpResponse r = Http.get(url).send();
 *     l.success("Done in " + r.durationMs() + "ms");
 * } catch (Exception e) {
 *     l.error("Failed: " + e.getMessage());
 * }
 * }</pre>
 *
 * <h3>2. Automatic — {@code withLoader} wraps the work</h3>
 * <pre>{@code
 * Loader.withLoader("Fetching data", () -> {
 *     HttpResponse r = Http.get(url).send();
 *     // the loader prints success on exit, error on exception
 * });
 * }</pre>
 *
 * <p>Prefer the automatic form unless you need to inspect the outcome.
 * It handles the try/finally for you and cannot leave a spinner
 * running after an exception.</p>
 *
 * <h2>Synchronization</h2>
 * <p>A private {@link #PRINT_LOCK} serialises output between the spinner
 * thread and any thread calling {@link #success}, {@link #error}, or
 * {@link #warn}. Using a private lock (rather than {@code System.out})
 * means no other class can accidentally hold the monitor and cause a
 * deadlock.</p>
 *
 * <h2>Silent mode</h2>
 * <p>{@link #silent()} returns a loader with no visible output — useful
 * for tests and for code paths that must stay quiet. {@link #stop()}
 * and the result methods are all safe no-ops on a silent loader.</p>
 *
 * <h2>Related</h2>
 * <p>For progress <b>bars</b> (a percentage indicator rather than a
 * spinner), see {@link Console#progress(int, int)}. For the raw
 * lower-level spinner, see {@link chirp.http.Spinner} — {@code Loader}
 * is the higher-level, friendly version.</p>
 *
 * @see Console#progress(int, int)
 * @see chirp.http.Spinner
 * @since 1.0.0
 */
public final class Loader {

    private static final String[] UNICODE_FRAMES = {
            "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
    };
    private static final String[] ASCII_FRAMES = {"|", "/", "-", "\\"};

    /** Private lock — output serialization without exposing {@code System.out}. */
    private static final Object PRINT_LOCK = new Object();

    private final String label;
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private Loader(String label) {
        this.label = label == null ? "" : label;
        this.thread = new Thread(this::loop, "chirp-loader");
        this.thread.setDaemon(true);
    }

    // ============================================================== factories

    /**
     * Starts a spinner with the given label. The spinner runs on a
     * daemon thread until stopped.
     *
     * <p>Aliases: {@link #begin(String)}, {@link #show(String)},
     * {@link #loading(String)}, {@link #spinner(String)},
     * {@link #busy(String)}, {@link #working(String)}, {@link #of(String)}.</p>
     *
     * @param label the label to display next to the spinning frame (may
     * be null → empty)
     * @return the running loader
     * @see #silent()
     * @see #withLoader(String, Runnable)
     * @since 1.0.0
     */
    public static Loader start(String label) { Loader l = new Loader(label); l.thread.start(); return l; }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader begin(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader show(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader loading(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader spinner(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader busy(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader working(String label) { return start(label); }

    /**
     * Alias of {@link #start(String)} — see that method for full
     * documentation.
     *
     * @param label the label
     * @return the running loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader of(String label) { return start(label); }

    /**
     * Creates a loader with no visible output. All methods are safe
     * no-ops. Useful for tests and for quiet code paths.
     *
     * @return a silent loader
     * @see #start(String)
     * @since 1.0.0
     */
    public static Loader silent() { return new Loader(""); }

    // ============================================================== terminal

    private void loop() {
        String[] frames = Capabilities.unicode() ? UNICODE_FRAMES : ASCII_FRAMES;
        int i = 0;
        while (running.get()) {
            synchronized (PRINT_LOCK) {
                System.out.print("\r" + frames[i++ % frames.length] + " " + label);
                System.out.flush();
            }
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
     * <p>Aliases: {@link #end()}, {@link #finish()}, {@link #cancel()},
     * {@link #halt()}.</p>
     *
     * @see #success(String)
     * @see #error(String)
     * @see #warn(String)
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

    /**
     * Alias of {@link #stop()} — see that method for full documentation.
     *
     * @see #stop()
     * @since 1.0.0
     */
    public void end() { stop(); }

    /**
     * Alias of {@link #stop()} — see that method for full documentation.
     *
     * @see #stop()
     * @since 1.0.0
     */
    public void finish() { stop(); }

    /**
     * Alias of {@link #stop()} — see that method for full documentation.
     *
     * @see #stop()
     * @since 1.0.0
     */
    public void cancel() { stop(); }

    /**
     * Alias of {@link #stop()} — see that method for full documentation.
     *
     * @see #stop()
     * @since 1.0.0
     */
    public void halt() { stop(); }

    // ============================================================== result-print

    /**
     * Stops the spinner and prints a green success line with a ✓ prefix.
     *
     * <pre>{@code
     * Loader l = Loader.start("Uploading");
     * upload();
     * l.success("Uploaded 3 files");   // ✓ Uploaded 3 files
     * }</pre>
     *
     * @param msg the message to print
     * @see #stop()
     * @see #error(String)
     * @see #warn(String)
     * @see #ok(String)
     * @see #good(String)
     * @since 1.0.0
     */
    public void success(String msg) {
        stop();
        synchronized (PRINT_LOCK) {
            System.out.println(Ansi.green() + Glyphs.success() + " " + msg + Ansi.reset());
        }
    }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @see #success(String)
     * @since 1.0.0
     */
    public void ok(String msg) { success(msg); }

    /**
     * Alias of {@link #success(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @see #success(String)
     * @since 1.0.0
     */
    public void good(String msg) { success(msg); }

    /**
     * Stops the spinner and prints a red error line with a ✗ prefix.
     *
     * @param msg the message to print
     * @see #success(String)
     * @see #fail(String)
     * @since 1.0.0
     */
    public void error(String msg) {
        stop();
        synchronized (PRINT_LOCK) {
            System.out.println(Ansi.red() + Glyphs.error() + " " + msg + Ansi.reset());
        }
    }

    /**
     * Alias of {@link #error(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @see #error(String)
     * @since 1.0.0
     */
    public void fail(String msg) { error(msg); }

    /**
     * Stops the spinner and prints a yellow warning line with a ⚠ prefix.
     *
     * @param msg the message to print
     * @see #success(String)
     * @see #warning(String)
     * @since 1.0.0
     */
    public void warn(String msg) {
        stop();
        synchronized (PRINT_LOCK) {
            System.out.println(Ansi.yellow() + Glyphs.warn() + " " + msg + Ansi.reset());
        }
    }

    /**
     * Alias of {@link #warn(String)} — see that method for full
     * documentation.
     *
     * @param msg the message
     * @see #warn(String)
     * @since 1.0.0
     */
    public void warning(String msg) { warn(msg); }

    // ============================================================== helper

    /**
     * Runs {@code action} while a spinner is active, then prints a
     * success or error line depending on outcome.
     *
     * <p>If the action throws, the exception is logged via
     * {@link Trace} before being rethrown — so the caller gets both the
     * loader's error line <b>and</b> the stack trace. The spinner is
     * always stopped.</p>
     *
     * @param label the spinner label
     * @param action the action to run (null → no-op)
     * @throws RuntimeException if {@code action} throws
     * @see #run(String, Runnable)
     * @see #spin(String, Runnable)
     * @since 1.0.0
     */
    public static void withLoader(String label, Runnable action) {
        if (action == null) return;
        Loader l = start(label);
        try {
            action.run();
            l.success("Done");
        } catch (RuntimeException e) {
            l.error(e.getMessage() == null ? "Failed" : e.getMessage());
            Trace.warn("loader", "withLoader action threw: " + e.getMessage());
            throw e;
        } finally {
            l.stop();
        }
    }

    /**
     * Alias of {@link #withLoader(String, Runnable)} — see that method
     * for full documentation and examples.
     *
     * @param label the spinner label
     * @param action the action to run
     * @see #withLoader(String, Runnable)
     * @since 1.0.0
     */
    public static void run(String label, Runnable action) { withLoader(label, action); }

    /**
     * Alias of {@link #withLoader(String, Runnable)} — see that method
     * for full documentation and examples.
     *
     * @param label the spinner label
     * @param action the action to run
     * @see #withLoader(String, Runnable)
     * @since 1.0.0
     */
    public static void spin(String label, Runnable action) { withLoader(label, action); }
}
