package chirp.decorator;

import chirp.config.Config;
import chirp.internal.Trace;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Fluent retry policy — retries an operation up to N times on failure,
 * with optional backoff, exponential growth, jitter, and exception
 * filtering.
 *
 * <p>The {@link Retry} object is immutable and reusable. Configure it
 * once with {@link #builder()}, then call {@link #execute(Supplier)} or
 * {@link #run(Runnable)} as many times as you like — each call runs the
 * full attempt loop independently.</p>
 *
 * <h2>Example — the common case</h2>
 * <pre>{@code
 * // 3 attempts, 500ms backoff
 * String data = Retry.of(3).execute(() -> fetch());
 * }</pre>
 *
 * <h2>Example — with backoff and filter</h2>
 * <pre>{@code
 * Retry retry = Retry.builder()
 *     .attempts(5)
 *     .backoff(Duration.ofMillis(250))
 *     .exponential(true)
 *     .onlyOn(IOException.class)
 *     .label("network call")
 *     .build();
 *
 * String data = retry.execute(() -> fetch());
 * }</pre>
 *
 * <h2>What "failure" means</h2>
 * <p>Any exception thrown by the operation — including checked
 * exceptions wrapped in {@link RuntimeException}, which is how a
 * {@code Supplier} propagates them — triggers a retry. The
 * {@link Builder#onlyOn(Class[])} filter narrows this: only exceptions
 * of those types (or their subclasses) cause a retry. Non-matching
 * exceptions fail immediately on the first attempt.</p>
 *
 * <h2>Interrupts</h2>
 * <p>If the sleeping backoff is interrupted, {@link Retry} re-sets the
 * thread's interrupt flag and throws a
 * {@link RetryInterruptedException}. Callers who don't care can catch
 * it or let it propagate — the interrupt flag remains set, so higher
 * layers can act on it.</p>
 *
 * <h2>Thread safety</h2>
 * <p>{@link Retry} instances are immutable — the same instance can be
 * used from multiple threads concurrently.</p>
 *
 * <h2>Related</h2>
 * <p>For one-shot use, see {@link Decorator#retry(int, Supplier)} and
 * {@link Decorator#retry(Supplier)}.</p>
 *
 * @see Decorator
 * @see Decorator#retry(int)
 * @see Decorator#retry(Supplier)
 * @since 1.0.0
 */
public final class Retry {

    /**
     * Key enum for the key-value {@link #from(Object...)} API.
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Number of attempts — {@link Integer}. */
        ATTEMPTS,
        /** Backoff between attempts in ms — {@link Integer}. */
        BACKOFF_MS,
        /** Whether to use exponential backoff — {@link Boolean}. */
        EXPONENTIAL,
        /** Jitter fraction (0.0–1.0) — {@link Number}. */
        JITTER,
        /** Exception classes to retry on — {@code Class<? extends Throwable>[]}. */
        ONLY_ON,
        /** Label for logging — {@link String}. */
        LABEL
    }

    /**
     * Thrown when a retry is interrupted during a backoff sleep. The
     * thread's interrupt flag is re-set before this is thrown, so higher
     * layers see the interruption.
     *
     * @since 1.0.0
     */
    public static final class RetryInterruptedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Constructs the exception.
         *
         * @param m the message
         * @param c the underlying interrupt cause
         * @since 1.0.0
         */
        public RetryInterruptedException(String m, Throwable c) { super(m, c); }
    }

    private final int attempts;
    private final Duration backoff;
    private final boolean exponential;
    private final double jitter;
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

    /**
     * Creates a retry policy with the given number of attempts and zero
     * backoff.
     *
     * @param attempts the number of attempts (clamped to ≥ 1)
     * @return the retry policy
     * @see #of(int, Duration)
     * @see #defaults()
     * @see #builder()
     * @since 1.0.0
     */
    public static Retry of(int attempts) {
        return builder().attempts(attempts).build();
    }

    /**
     * Creates a retry policy with the given number of attempts and a
     * fixed backoff between attempts.
     *
     * @param attempts the number of attempts (clamped to ≥ 1)
     * @param backoff the wait between attempts (null or negative → zero)
     * @return the retry policy
     * @see #of(int)
     * @since 1.0.0
     */
    public static Retry of(int attempts, Duration backoff) {
        return builder().attempts(attempts).backoff(backoff).build();
    }

    /**
     * Returns the default retry policy — 3 attempts, 500ms fixed
     * backoff.
     *
     * @return the default retry policy
     * @see #of(int)
     * @since 1.0.0
     */
    public static Retry defaults() {
        return of(3, Duration.ofMillis(500));
    }

    /**
     * Returns a fresh builder for fluent configuration.
     *
     * @return a new builder
     * @see #from(Object...)
     * @since 1.0.0
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Constructs a retry policy from alternating {@link Key} → value
     * pairs. Unknown keys and wrong value types are silently skipped.
     *
     * <pre>{@code
     * Retry.from(
     *     Retry.Key.ATTEMPTS, 3,
     *     Retry.Key.BACKOFF_MS, 500,
     *     Retry.Key.LABEL, "fetch")
     *     .execute(() -> fetch());
     * }</pre>
     *
     * @param keyValues alternating Key → value pairs
     * @return the retry policy
     * @see #builder()
     * @since 1.0.0
     */
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

    /**
     * Executes a supplier, retrying on failure. Returns the first
     * successful value. If every attempt fails, the last exception is
     * rethrown.
     *
     * <p>If {@code onlyOn} is set, non-matching exceptions fail
     * immediately on the first attempt.</p>
     *
     * @param supplier the supplier to run (null → null)
     * @param <T> the return type
     * @return the value
     * @throws RuntimeException after all attempts fail
     * @throws RetryInterruptedException if the backoff sleep is interrupted
     * @see #run(Runnable)
     * @since 1.0.0
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
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

    /**
     * Executes a {@link Runnable}, retrying on failure. See
     * {@link #execute(Supplier)} for the retry rules.
     *
     * @param action the action to run (null → no-op)
     * @throws RuntimeException after all attempts fail
     * @throws RetryInterruptedException if the backoff sleep is interrupted
     * @see #execute(Supplier)
     * @since 1.0.0
     */
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

    /**
     * Fluent builder for {@link Retry}. Every setter returns the
     * builder, so calls chain. Call {@link #build()} to get the
     * immutable retry policy.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private int attempts = 3;
        private Duration backoff = Duration.ofMillis(500);
        private boolean exponential = false;
        private double jitter = 0.0;
        private Set<Class<? extends Throwable>> onlyOn = new HashSet<>();
        private String label = "operation";

        /**
         * Sets the number of attempts.
         *
         * @param n the attempts (clamped to ≥ 1)
         * @return this builder
         * @since 1.0.0
         */
        public Builder attempts(int n) { this.attempts = Math.max(1, n); return this; }

        /**
         * Alias of {@link #attempts(int)} — see that method for full
         * documentation.
         *
         * @param n the attempts
         * @return this builder
         * @see #attempts(int)
         * @since 1.0.0
         */
        public Builder maxAttempts(int n) { return attempts(n); }

        /**
         * Alias of {@link #attempts(int)} — see that method for full
         * documentation.
         *
         * @param n the attempts
         * @return this builder
         * @see #attempts(int)
         * @since 1.0.0
         */
        public Builder retries(int n) { return attempts(n); }

        /**
         * Alias of {@link #attempts(int)} — see that method for full
         * documentation.
         *
         * @param n the attempts
         * @return this builder
         * @see #attempts(int)
         * @since 1.0.0
         */
        public Builder tries(int n) { return attempts(n); }

        /**
         * Sets the backoff between attempts.
         *
         * @param d the backoff duration (null or negative → zero)
         * @return this builder
         * @since 1.0.0
         */
        public Builder backoff(Duration d) { if (d != null && !d.isNegative()) this.backoff = d; return this; }

        /**
         * Sets the backoff in milliseconds.
         *
         * @param ms the backoff in milliseconds (clamped to ≥ 0)
         * @return this builder
         * @see #backoff(Duration)
         * @since 1.0.0
         */
        public Builder backoffMs(long ms) { return backoff(Duration.ofMillis(Math.max(0, ms))); }

        /**
         * Alias of {@link #backoff(Duration)} — see that method for full
         * documentation.
         *
         * @param d the backoff
         * @return this builder
         * @see #backoff(Duration)
         * @since 1.0.0
         */
        public Builder waitBetween(Duration d) { return backoff(d); }

        /**
         * Alias of {@link #backoff(Duration)} — see that method for full
         * documentation.
         *
         * @param d the backoff
         * @return this builder
         * @see #backoff(Duration)
         * @since 1.0.0
         */
        public Builder delay(Duration d) { return backoff(d); }

        /**
         * Enables or disables exponential backoff. When enabled, the
         * wait doubles each attempt: {@code backoff * 2^(attempt-1)},
         * capped at 2^20 to prevent overflow.
         *
         * @param v whether to use exponential
         * @return this builder
         * @see #exponential()
         * @see #linear()
         * @since 1.0.0
         */
        public Builder exponential(boolean v) { this.exponential = v; return this; }

        /**
         * Alias of {@link #exponential(boolean)} with {@code true}.
         *
         * @return this builder
         * @see #exponential(boolean)
         * @since 1.0.0
         */
        public Builder exponential() { return exponential(true); }

        /**
         * Disables exponential backoff (linear).
         *
         * @return this builder
         * @see #exponential(boolean)
         * @since 1.0.0
         */
        public Builder linear() { return exponential(false); }

        /**
         * Adds jitter to the backoff — a random fraction of the wait,
         * capped at the given value. The actual wait becomes
         * {@code base * (1 ± jitter)}.
         *
         * <p>Spreads retries across a fleet of clients so they do not
         * retry in lockstep. Typical values: 0.1–0.5. Clamped to
         * {@code [0.0, 1.0]}.</p>
         *
         * @param fraction the jitter fraction (clamped)
         * @return this builder
         * @since 1.0.0
         */
        public Builder jitter(double fraction) {
            this.jitter = Math.max(0.0, Math.min(1.0, fraction));
            return this;
        }

        /**
         * Restricts retries to the given exception types. Non-matching
         * exceptions fail immediately on the first attempt.
         *
         * <p>Null arrays and null entries are skipped (the internal set
         * rejects nulls).</p>
         *
         * @param classes the exception classes to retry on
         * @return this builder
         * @see #onlyRetrying(Class[])
         * @since 1.0.0
         */
        @SafeVarargs
        public final Builder onlyOn(Class<? extends Throwable>... classes) {
            if (classes != null) {
                for (Class<? extends Throwable> c : classes) if (c != null) onlyOn.add(c);
            }
            return this;
        }

        /**
         * Alias of {@link #onlyOn(Class[])} — see that method for full
         * documentation.
         *
         * @param classes the exception classes
         * @return this builder
         * @see #onlyOn(Class[])
         * @since 1.0.0
         */
        @SafeVarargs
        @SuppressWarnings("varargs")
        public final Builder onlyRetrying(Class<? extends Throwable>... classes) { return onlyOn(classes); }

        /**
         * Sets the label used in trace output and interruption messages.
         *
         * @param l the label (null → "operation")
         * @return this builder
         * @see #name(String)
         * @since 1.0.0
         */
        public Builder label(String l) { if (l != null) this.label = l; return this; }

        /**
         * Alias of {@link #label(String)} — see that method for full
         * documentation.
         *
         * @param l the label
         * @return this builder
         * @see #label(String)
         * @since 1.0.0
         */
        public Builder name(String l) { return label(l); }

        /**
         * Configures a single aspect via the {@link Key} enum.
         *
         * @param key the key (null → no-op)
         * @param value the value (null → no-op)
         * @return this builder
         * @since 1.0.0
         */
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

        /**
         * Builds the immutable retry policy.
         *
         * @return the retry policy
         * @since 1.0.0
         */
        public Retry build() { return new Retry(this); }
    }
}
