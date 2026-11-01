package chirp.decorator;

import chirp.config.Config;
import chirp.internal.Trace;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Fluent retry policy.
 *
 * <p>Executes a {@code Runnable} or {@code Supplier} and retries it up to
 * {@link Builder#attempts(int) N attempts} on failure. Supports fixed or
 * exponential backoff with jitter, optional filtering by exception type,
 * and interruption-aware sleeps.</p>
 *
 * <pre>{@code
 * // Simple — 3 attempts, 500ms fixed backoff
 * Object result = Retry.of(3).execute(() -> fetch());
 *
 * // With backoff and filter
 * Retry.builder()
 *     .attempts(5)
 *     .backoff(Duration.ofMillis(250))
 *     .exponential(true)
 *     .onlyOn(IOException.class)
 *     .label("network call")
 *     .build()
 *     .execute(() -> fetch());
 *
 * // Runnable form — no return value
 * Retry.of(3).run(() -> save());
 * }</pre>
 *
 * <p>All methods are null-safe and never throw {@code NullPointerException}.
 * Exceptions that survive all retries are rethrown unchanged for the caller
 * to handle. Interrupts are re-interrupted and rethrown wrapped in
 * {@link RetryInterruptedException}.</p>
 *
 * <h2>Jitter</h2>
 * <p>Backoff jitter spreads retries across multiple clients so a bad server
 * does not receive a synchronised retry storm. Jitter uses
 * {@link ThreadLocalRandom#current()} — it is a timing spread, not a
 * security primitive.</p>
 */
public final class Retry {

    /** Key enum for the key-value API. */
    public enum Key { ATTEMPTS, BACKOFF_MS, EXPONENTIAL, JITTER, ONLY_ON, LABEL }

    /** Thrown when a retry is interrupted during a backoff sleep. */
    public static final class RetryInterruptedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public RetryInterruptedException(String m, Throwable c) { super(m, c); }
    }

    private final int attempts;
    private final Duration backoff;
    private final boolean exponential;
    private final double jitter;    // 0.0 – 1.0, fraction of the backoff applied randomly
    private final Set<Class<? extends Throwable>> onlyOn;
    private final String label;

    private Retry(Builder b) {
        this.attempts = Math.max(1, b.attempts);
        this.backoff = b.backoff == null || b.backoff.isNegative() ? Duration.ZERO : b.backoff;
        this.exponential = b.exponential;
        this.jitter = Math.max(0.0, Math.min(1.0, b.jitter));
        this.onlyOn = b.onlyOn.isEmpty() ? null : Set.copyOf(b.onlyOn);
        this.label = b.label == null ? "operation" : b.label;
    }

    // -------------------------------------------------------------- factories

    /** Retry {@code attempts} times with zero backoff. */
    public static Retry of(int attempts) {
        return builder().attempts(attempts).build();
    }

    /** Retry {@code attempts} times with a fixed {@code backoff} between attempts. */
    public static Retry of(int attempts, Duration backoff) {
        return builder().attempts(attempts).backoff(backoff).build();
    }

    /** Convenience — 3 attempts, 500ms fixed backoff. */
    public static Retry defaults() {
        return of(3, Duration.ofMillis(500));
    }

    public static Builder builder() { return new Builder(); }

    /** Key-value factory. Unknown keys and wrong value types are silently skipped. */
    public static Retry from(Object... keyValues) {
        Builder b = builder();
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case ATTEMPTS -> {
                        if (v instanceof Integer n) b.attempts(n);
                    }
                    case BACKOFF_MS -> {
                        if (v instanceof Integer n) b.backoff(Duration.ofMillis(n));
                    }
                    case EXPONENTIAL -> {
                        if (v instanceof Boolean bool) b.exponential(bool);
                    }
                    case JITTER -> {
                        if (v instanceof Number n) b.jitter(n.doubleValue());
                    }
                    case LABEL -> {
                        if (v instanceof String s) b.label(s);
                    }
                    case ONLY_ON -> {
                        if (v instanceof Class<?>[] arr) {
                            @SuppressWarnings("unchecked")
                            Class<? extends Throwable>[] classes = (Class<? extends Throwable>[]) arr;
                            b.onlyOn(classes);
                        }
                    }
                }
            }
        }
        return b.build();
    }

    // -------------------------------------------------------------- terminals

    /** Executes a {@code Supplier}, retrying on failure. Returns the last successful value. */
    public <T> T execute(Supplier<T> supplier) {
        if (supplier == null) return null;
        Throwable last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                if (attempt > 1 && Config.isTraceEnabled()) {
                    Trace.debug("decorator", "retry {} attempt {}/{}", label, attempt, attempts);
                }
                return supplier.get();
            } catch (Throwable t) {
                last = t;
                if (!shouldRetry(t, attempt)) {
                    rethrowUnchecked(t);
                }
                if (attempt < attempts) {
                    sleepBackoff(attempt);
                }
            }
        }
        // All attempts failed
        rethrowUnchecked(last);
        return null; // unreachable
    }

    /** Executes a {@code Runnable}, retrying on failure. */
    public void run(Runnable action) {
        if (action == null) return;
        execute(() -> { action.run(); return null; });
    }

    // -------------------------------------------------------------- internals

    private boolean shouldRetry(Throwable t, int attempt) {
        if (attempt >= attempts) return false;
        if (onlyOn == null) return true;      // no filter → retry everything
        for (Class<? extends Throwable> c : onlyOn) {
            if (c.isInstance(t)) return true;
        }
        return false;
    }

    private void sleepBackoff(int attempt) {
        long baseMs = backoff.toMillis();
        if (baseMs <= 0) return;

        long waitMs = exponential ? baseMs * (1L << Math.min(attempt - 1, 20)) : baseMs;

        // Jitter: apply ±(jitter * 100)% as a random offset
        if (jitter > 0) {
            double delta = waitMs * jitter * (ThreadLocalRandom.current().nextDouble() * 2 - 1);
            waitMs = Math.max(0, waitMs + (long) delta);
        }

        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(
                    "Retry interrupted during backoff for '" + label + "'", e);
        }
    }

    private static void rethrowUnchecked(Throwable t) {
        if (t instanceof RuntimeException re) throw re;
        if (t instanceof Error err) throw err;
        // Checked exception inside Supplier — wrap
        throw new RuntimeException(t);
    }

    // -------------------------------------------------------------- builder

    public static final class Builder {
        private int attempts = 3;
        private Duration backoff = Duration.ofMillis(500);
        private boolean exponential = false;
        private double jitter = 0.0;
        private Set<Class<? extends Throwable>> onlyOn = new HashSet<>();
        private String label = "operation";

        // Aliases for attempts
        public Builder attempts(int n) { this.attempts = Math.max(1, n); return this; }
        public Builder maxAttempts(int n) { return attempts(n); }
        public Builder retries(int n) { return attempts(n); }
        public Builder tries(int n) { return attempts(n); }

        // Aliases for backoff
        public Builder backoff(Duration d) { if (d != null && !d.isNegative()) this.backoff = d; return this; }
        public Builder backoffMs(long ms) { return backoff(Duration.ofMillis(Math.max(0, ms))); }
        public Builder waitBetween(Duration d) { return backoff(d); }
        public Builder delay(Duration d) { return backoff(d); }

        // Exponential
        public Builder exponential(boolean v) { this.exponential = v; return this; }
        public Builder exponential() { return exponential(true); }
        public Builder linear() { return exponential(false); }

        // Jitter
        public Builder jitter(double fraction) {
            this.jitter = Math.max(0.0, Math.min(1.0, fraction));
            return this;
        }

        // Exception filters
        /** Null arrays and null entries are skipped (Set.copyOf in the constructor rejects nulls). */
        @SafeVarargs
        public final Builder onlyOn(Class<? extends Throwable>... classes) {
            if (classes != null) {
                for (Class<? extends Throwable> c : classes) if (c != null) onlyOn.add(c);
            }
            return this;
        }
        /** Alias of {@link #onlyOn}; forwarding the array is safe because onlyOn only reads it. */
        @SafeVarargs
        @SuppressWarnings("varargs")
        public final Builder onlyRetrying(Class<? extends Throwable>... classes) { return onlyOn(classes); }

        // Label
        public Builder label(String l) { if (l != null) this.label = l; return this; }
        public Builder name(String l) { return label(l); }

        // Key-value setter
        public Builder set(Key key, Object value) {
            if (key == null || value == null) return this;
            switch (key) {
                case ATTEMPTS -> {
                    if (value instanceof Integer n) attempts(n);
                }
                case BACKOFF_MS -> {
                    if (value instanceof Integer n) backoffMs(n);
                }
                case EXPONENTIAL -> {
                    if (value instanceof Boolean b) exponential(b);
                }
                case JITTER -> {
                    if (value instanceof Number n) jitter(n.doubleValue());
                }
                case LABEL -> {
                    if (value instanceof String s) label(s);
                }
                case ONLY_ON -> {
                    if (value instanceof Class<?>[] arr) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Throwable>[] classes = (Class<? extends Throwable>[]) arr;
                        onlyOn(classes);
                    }
                }
            }
            return this;
        }

        public Retry build() { return new Retry(this); }
    }
}
