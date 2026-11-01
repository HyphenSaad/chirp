package chirp.console;

import chirp.internal.Ansi;
import chirp.internal.Capabilities;
import chirp.internal.Glyphs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight terminal spinner for operations that take time (HTTP, SMTP,
 * large file copies).
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code start / begin / show / loading / spinner / of / busy};
 * terminals: {@code stop / end / finish / done / cancel};
 * result printing: {@code success / ok / good}, {@code error / fail},
 * {@code warn / warning}.</p>
 *
 * <pre>{@code
 * Loader l = Loader.start("Sending email");
 * try { send(); l.success("Sent"); }
 * catch (Exception e) { l.error("Failed: " + e.getMessage()); }
 * }</pre>
 */
public final class Loader {

    private static final String[] UNICODE_FRAMES = {
            "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
    };
    private static final String[] ASCII_FRAMES = {"|", "/", "-", "\\"};

    private final String label;
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private Loader(String label) {
        this.label = label == null ? "" : label;
        this.thread = new Thread(this::loop, "chirp-loader");
        this.thread.setDaemon(true);
    }

    // ============================================================== factories

    public static Loader start(String label) { Loader l = new Loader(label); l.thread.start(); return l; }
    public static Loader begin(String label) { return start(label); }
    public static Loader show(String label) { return start(label); }
    public static Loader loading(String label) { return start(label); }
    public static Loader spinner(String label) { return start(label); }
    public static Loader busy(String label) { return start(label); }
    public static Loader working(String label) { return start(label); }
    public static Loader of(String label) { return start(label); }

    /** No-op loader — useful for tests and dry runs. */
    public static Loader silent() { return new Loader(""); }

    // ============================================================== terminal

    private void loop() {
        String[] frames = Capabilities.unicode() ? UNICODE_FRAMES : ASCII_FRAMES;
        int i = 0;
        while (running.get()) {
            synchronized (System.out) {
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

    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        try {
            thread.join(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        synchronized (System.out) {
            System.out.print("\r" + " ".repeat(Math.min(label.length() + 4, 200)) + "\r");
            System.out.flush();
        }
    }
    public void end() { stop(); }
    public void finish() { stop(); }
    public void cancel() { stop(); }
    public void halt() { stop(); }

    // ============================================================== result-print

    public void success(String msg) {
        stop();
        System.out.println(Ansi.green() + Glyphs.success() + " " + msg + Ansi.reset());
    }
    public void ok(String msg) { success(msg); }
    public void good(String msg) { success(msg); }

    public void error(String msg) {
        stop();
        System.out.println(Ansi.red() + Glyphs.error() + " " + msg + Ansi.reset());
    }
    public void fail(String msg) { error(msg); }

    public void warn(String msg) {
        stop();
        System.out.println(Ansi.yellow() + Glyphs.warn() + " " + msg + Ansi.reset());
    }
    public void warning(String msg) { warn(msg); }

    // ============================================================== helper

    /**
     * Runs {@code action} while a spinner is active, then prints a
     * success or error line depending on outcome.
     */
    public static void withLoader(String label, Runnable action) {
        if (action == null) return;
        Loader l = start(label);
        try {
            action.run();
            l.success("Done");
        } catch (RuntimeException e) {
            l.error(e.getMessage() == null ? "Failed" : e.getMessage());
            throw e;
        } finally {
            l.stop();
        }
    }
    public static void run(String label, Runnable action) { withLoader(label, action); }
    public static void spin(String label, Runnable action) { withLoader(label, action); }
}
