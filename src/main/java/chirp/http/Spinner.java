package chirp.http;

import chirp.internal.Capabilities;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A tiny terminal spinner. Runs on a daemon thread; writes {@code \r} +
 * frame to {@link System#out} so it overwrites itself. Falls back to ASCII
 * frames when unicode is unavailable.
 */
public final class Spinner {

    private static final String[] UNICODE_FRAMES = {
            "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
    };
    private static final String[] ASCII_FRAMES = {"|", "/", "-", "\\"};

    private final String label;
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private Spinner(String label) {
        this.label = label == null ? "" : label;
        this.thread = new Thread(this::loop, "chirp-http-spinner");
        this.thread.setDaemon(true);
    }

    public static Spinner start(String label) {
        Spinner spinner = new Spinner(label);
        spinner.thread.start();
        return spinner;
    }

    private void loop() {
        String[] frames = Capabilities.unicode() ? UNICODE_FRAMES : ASCII_FRAMES;
        int frame = 0;
        while (running.get()) {
            synchronized (System.out) {
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
}
