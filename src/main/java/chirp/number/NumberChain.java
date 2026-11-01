package chirp.number;

import java.util.Locale;

/**
 * Immutable fluent arithmetic chainer.
 *
 * <p>{@code NumberChain} wraps a single {@code double} value. Every
 * arithmetic or transformation method returns a <b>new</b> chain wrapping
 * the computed value — so the chain itself is immutable and thread-safe.
 * Chaining is safe: you can pass a chain to another thread, or hold a
 * reference to an intermediate value, without any risk of it changing.</p>
 *
 * <h2>Why this class exists</h2>
 * <p>Chirp's {@link Numbers} class exposes each operation as a static
 * method, which is fine for a single call:</p>
 * <pre>{@code
 * int result = Numbers.clamp((10 + 5) * 2, 0, 20);
 * }</pre>
 * <p>…but quickly becomes unreadable with more operations. {@code
 * NumberChain} lets the same arithmetic read left-to-right, without
 * nested parentheses:</p>
 * <pre>{@code
 * int result = Numbers.of(10)
 *     .add(5)
 *     .times(2)
 *     .clamp(0, 20)
 *     .asInt();       // 20
 * }</pre>
 *
 * <h2>The Decorator pattern</h2>
 * <p>{@code NumberChain} is a live demonstration of the <b>Decorator</b>
 * design pattern. Each method wraps the current value with a new
 * computation, producing a new object — the previous chain is untouched.
 * The structure of</p>
 * <pre>{@code
 * Numbers.of(10).add(5).times(2)
 * }</pre>
 * <p>is conceptually three separate objects, each holding an immutable
 * {@code double}: {@code 10}, {@code 15}, {@code 30}. The chain links
 * them, but at no point is any value modified.</p>
 *
 * <h2>Why the internal store is {@code double}</h2>
 * <p>Internally the value is stored as a {@code double}, not an
 * {@code int}. This lets a single chain handle both int and double
 * arithmetic without a separate class per type. The trade-off is that
 * very large {@code long} values (above 2<sup>53</sup>) lose precision —
 * if you need exact {@code long} arithmetic on values above ~9 × 10<sup>15</sup>,
 * use {@link Numbers} directly instead of the chain.</p>
 *
 * <p>The {@link #asInt()} terminal truncates (it does not round); use
 * {@link #round()} before {@code asInt()} if you want rounding. The
 * {@link #asLong()} terminal behaves the same way for {@code long}.</p>
 *
 * <h2>Division by zero</h2>
 * <p>Unlike raw Java arithmetic, {@link #divide(int)} and
 * {@link #divide(double)} do <b>not</b> throw on a zero divisor. They
 * return a chain wrapping {@code 0} instead. This keeps chains total
 * (never throw) — a design choice matching the rest of the library.</p>
 *
 * <h2>Null safety</h2>
 * <p>Factories never return {@code null}. {@link #apply(java.util.function.DoubleUnaryOperator)}
 * ignores a {@code null} operator and returns the chain unchanged. All
 * terminal methods return a primitive — never {@code null}.</p>
 *
 * <h2>Example — a compounding interest calculation</h2>
 * <pre>{@code
 * double finalAmount = Numbers.of(principal)
 *     .times(1 + rate)
 *     .pow(years)
 *     .round(2)
 *     .asDouble();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the static helpers, see {@link Numbers}. For the fluent text
 * equivalent, see {@link chirp.text.TextChain}. For fluent array chains,
 * see {@link chirp.collection.ArrayChain}. For fluent date chains, see
 * {@link chirp.datetime.DateChain}.</p>
 *
 * @see Numbers
 * @see chirp.text.TextChain
 * @see chirp.collection.ArrayChain
 * @see chirp.datetime.DateChain
 * @since 1.0.0
 */
public final class NumberChain {

    private final double value;

    private NumberChain(double value) { this.value = value; }

    // ============================================================== factories

    /**
     * Wraps the int in a new chain.
     *
     * @param n the initial value
     * @return a new {@code NumberChain} wrapping {@code n}
     * @see #of(double)
     * @see Numbers#of(int)
     * @since 1.0.0
     */
    public static NumberChain of(int n) { return new NumberChain(n); }

    /**
     * Wraps the double in a new chain.
     *
     * @param n the initial value
     * @return a new {@code NumberChain} wrapping {@code n}
     * @see #of(int)
     * @see Numbers#of(double)
     * @since 1.0.0
     */
    public static NumberChain of(double n) { return new NumberChain(n); }

    /**
     * Alias of {@link #of(int)} — see that method for full documentation.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(int)
     * @since 1.0.0
     */
    public static NumberChain from(int n) { return of(n); }

    /**
     * Alias of {@link #of(double)} — see that method for full
     * documentation.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(double)
     * @since 1.0.0
     */
    public static NumberChain from(double n) { return of(n); }

    /**
     * Alias of {@link #of(int)} — see that method for full documentation.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(int)
     * @since 1.0.0
     */
    public static NumberChain start(int n) { return of(n); }

    /**
     * Alias of {@link #of(double)} — see that method for full
     * documentation.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(double)
     * @since 1.0.0
     */
    public static NumberChain start(double n) { return of(n); }

    // ============================================================== arithmetic

    /**
     * Appends a step that adds {@code n} to the value.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #plus(int)
     * @see #and(int)
     * @see #sum(int)
     * @see #add(double)
     * @since 1.0.0
     */
    public NumberChain add(int n) { return new NumberChain(value + n); }

    /**
     * Appends a step that adds {@code n} to the value.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #add(int)
     * @since 1.0.0
     */
    public NumberChain add(double n) { return new NumberChain(value + n); }

    /**
     * Alias of {@link #add(int)} — see that method for full documentation.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #add(int)
     * @since 1.0.0
     */
    public NumberChain plus(int n) { return add(n); }

    /**
     * Alias of {@link #add(double)} — see that method for full
     * documentation.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #add(double)
     * @since 1.0.0
     */
    public NumberChain plus(double n) { return add(n); }

    /**
     * Alias of {@link #add(int)} — see that method for full documentation.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #add(int)
     * @since 1.0.0
     */
    public NumberChain sum(int n) { return add(n); }

    /**
     * Alias of {@link #add(int)} — see that method for full documentation.
     * Reads naturally as a continuation of a chain, e.g.
     * {@code Numbers.of(5).and(10)}.
     *
     * @param n the value to add
     * @return a new chain with the addition applied
     * @see #add(int)
     * @since 1.0.0
     */
    public NumberChain and(int n) { return add(n); }

    /**
     * Appends a step that subtracts {@code n} from the value.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #minus(int)
     * @see #sub(int)
     * @see #take(int)
     * @since 1.0.0
     */
    public NumberChain subtract(int n) { return new NumberChain(value - n); }

    /**
     * Appends a step that subtracts {@code n} from the value.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #subtract(int)
     * @since 1.0.0
     */
    public NumberChain subtract(double n) { return new NumberChain(value - n); }

    /**
     * Alias of {@link #subtract(int)} — see that method for full
     * documentation.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #subtract(int)
     * @since 1.0.0
     */
    public NumberChain minus(int n) { return subtract(n); }

    /**
     * Alias of {@link #subtract(double)} — see that method for full
     * documentation.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #subtract(double)
     * @since 1.0.0
     */
    public NumberChain minus(double n) { return subtract(n); }

    /**
     * Alias of {@link #subtract(int)} — see that method for full
     * documentation.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #subtract(int)
     * @since 1.0.0
     */
    public NumberChain sub(int n) { return subtract(n); }

    /**
     * Alias of {@link #subtract(int)} — see that method for full
     * documentation.
     *
     * @param n the value to subtract
     * @return a new chain with the subtraction applied
     * @see #subtract(int)
     * @since 1.0.0
     */
    public NumberChain take(int n) { return subtract(n); }

    /**
     * Appends a step that multiplies the value by {@code n}.
     *
     * @param n the multiplier
     * @return a new chain with the multiplication applied
     * @see #times(int)
     * @see #mul(int)
     * @since 1.0.0
     */
    public NumberChain multiply(int n) { return new NumberChain(value * n); }

    /**
     * Appends a step that multiplies the value by {@code n}.
     *
     * @param n the multiplier
     * @return a new chain with the multiplication applied
     * @see #multiply(int)
     * @since 1.0.0
     */
    public NumberChain multiply(double n) { return new NumberChain(value * n); }

    /**
     * Alias of {@link #multiply(int)} — see that method for full
     * documentation.
     *
     * @param n the multiplier
     * @return a new chain with the multiplication applied
     * @see #multiply(int)
     * @since 1.0.0
     */
    public NumberChain times(int n) { return multiply(n); }

    /**
     * Alias of {@link #multiply(double)} — see that method for full
     * documentation.
     *
     * @param n the multiplier
     * @return a new chain with the multiplication applied
     * @see #multiply(double)
     * @since 1.0.0
     */
    public NumberChain times(double n) { return multiply(n); }

    /**
     * Alias of {@link #multiply(int)} — see that method for full
     * documentation.
     *
     * @param n the multiplier
     * @return a new chain with the multiplication applied
     * @see #multiply(int)
     * @since 1.0.0
     */
    public NumberChain mul(int n) { return multiply(n); }

    /**
     * Appends a step that divides the value by {@code n}.
     *
     * <p><b>Division by zero</b>: returns a chain wrapping {@code 0}
     * instead of throwing. This keeps the chain total (never throws) — the
     * design intent is that a chain should always evaluate to something
     * usable. If you need to detect a zero divisor, check {@code n} before
     * calling this method.</p>
     *
     * @param n the divisor (may be 0 — returns 0)
     * @return a new chain with the division applied, or a chain wrapping
     * {@code 0} if {@code n == 0}
     * @see #divide(double)
     * @see #div(int)
     * @see #dividedBy(int)
     * @see #over(int)
     * @since 1.0.0
     */
    public NumberChain divide(int n) { return n == 0 ? new NumberChain(0) : new NumberChain(value / n); }

    /**
     * Appends a step that divides the value by {@code n}.
     *
     * <p>Division by zero returns {@code 0} — see {@link #divide(int)}
     * for the rationale.</p>
     *
     * @param n the divisor (may be 0 — returns 0)
     * @return a new chain with the division applied, or 0 if {@code n == 0}
     * @see #divide(int)
     * @since 1.0.0
     */
    public NumberChain divide(double n) { return n == 0 ? new NumberChain(0) : new NumberChain(value / n); }

    /**
     * Alias of {@link #divide(int)} — see that method for full
     * documentation.
     *
     * @param n the divisor
     * @return a new chain with the division applied
     * @see #divide(int)
     * @since 1.0.0
     */
    public NumberChain div(int n) { return divide(n); }

    /**
     * Alias of {@link #divide(int)} — see that method for full
     * documentation.
     *
     * @param n the divisor
     * @return a new chain with the division applied
     * @see #divide(int)
     * @since 1.0.0
     */
    public NumberChain dividedBy(int n) { return divide(n); }

    /**
     * Alias of {@link #divide(int)} — see that method for full
     * documentation.
     *
     * @param n the divisor
     * @return a new chain with the division applied
     * @see #divide(int)
     * @since 1.0.0
     */
    public NumberChain over(int n) { return divide(n); }

    /**
     * Appends a step that reduces the value modulo {@code n}, using
     * {@link Numbers#mod(int, int)} (always non-negative for positive
     * divisors).
     *
     * <p>A zero divisor leaves the value unchanged (no exception).</p>
     *
     * @param n the modulus (may be 0 — returns the current value)
     * @return a new chain with the modulo applied
     * @see #modulo(int)
     * @see #rem(int)
     * @see Numbers#mod(int, int)
     * @since 1.0.0
     */
    public NumberChain mod(int n) { return n == 0 ? this : new NumberChain(Numbers.mod((int) value, n)); }

    /**
     * Alias of {@link #mod(int)} — see that method for full documentation.
     *
     * @param n the modulus
     * @return a new chain with the modulo applied
     * @see #mod(int)
     * @since 1.0.0
     */
    public NumberChain modulo(int n) { return mod(n); }

    /**
     * Alias of {@link #mod(int)} — see that method for full documentation.
     *
     * @param n the modulus
     * @return a new chain with the modulo applied
     * @see #mod(int)
     * @since 1.0.0
     */
    public NumberChain rem(int n) { return mod(n); }

    /**
     * Appends a step that raises the value to {@code exponent}.
     *
     * @param exponent the exponent
     * @return a new chain with the power applied
     * @see #power(double)
     * @see #raisedTo(double)
     * @since 1.0.0
     */
    public NumberChain pow(double exponent) { return new NumberChain(Math.pow(value, exponent)); }

    /**
     * Alias of {@link #pow(double)} — see that method for full
     * documentation.
     *
     * @param exp the exponent
     * @return a new chain with the power applied
     * @see #pow(double)
     * @since 1.0.0
     */
    public NumberChain power(double exp) { return pow(exp); }

    /**
     * Alias of {@link #pow(double)} — see that method for full
     * documentation.
     *
     * @param exp the exponent
     * @return a new chain with the power applied
     * @see #pow(double)
     * @since 1.0.0
     */
    public NumberChain raisedTo(double exp) { return pow(exp); }

    // ============================================================== transforms

    /**
     * Appends a step that takes the absolute value.
     *
     * @return a new chain with the absolute value applied
     * @see #absolute()
     * @since 1.0.0
     */
    public NumberChain abs() { return new NumberChain(Math.abs(value)); }

    /**
     * Alias of {@link #abs()} — see that method for full documentation.
     *
     * @return a new chain with the absolute value applied
     * @see #abs()
     * @since 1.0.0
     */
    public NumberChain absolute() { return abs(); }

    /**
     * Appends a step that negates the value.
     *
     * @return a new chain with the sign flipped
     * @see #negative()
     * @see #flipSign()
     * @since 1.0.0
     */
    public NumberChain negate() { return new NumberChain(-value); }

    /**
     * Alias of {@link #negate()} — see that method for full documentation.
     *
     * @return a new chain with the sign flipped
     * @see #negate()
     * @since 1.0.0
     */
    public NumberChain negative() { return negate(); }

    /**
     * Alias of {@link #negate()} — see that method for full documentation.
     *
     * @return a new chain with the sign flipped
     * @see #negate()
     * @since 1.0.0
     */
    public NumberChain flipSign() { return negate(); }

    /**
     * Appends a step that takes the square root. Negative input returns
     * {@code 0} (matching {@link Numbers#sqrt(double)}).
     *
     * @return a new chain with the square root applied
     * @see #squareRoot()
     * @see Numbers#sqrt(double)
     * @since 1.0.0
     */
    public NumberChain sqrt() { return new NumberChain(Numbers.sqrt(value)); }

    /**
     * Alias of {@link #sqrt()} — see that method for full documentation.
     *
     * @return a new chain with the square root applied
     * @see #sqrt()
     * @since 1.0.0
     */
    public NumberChain squareRoot() { return sqrt(); }

    /**
     * Appends a step that rounds the value to the nearest integer
     * (half-up).
     *
     * @return a new chain with the rounded value
     * @see #round(int)
     * @see #rounded()
     * @since 1.0.0
     */
    public NumberChain round() { return new NumberChain(Math.round(value)); }

    /**
     * Appends a step that rounds the value to {@code decimals} decimal
     * places.
     *
     * @param decimals the number of decimal places
     * @return a new chain with the rounded value
     * @see #round()
     * @see Numbers#round(double, int)
     * @since 1.0.0
     */
    public NumberChain round(int decimals) { return new NumberChain(Numbers.round(value, decimals)); }

    /**
     * Alias of {@link #round()} — see that method for full documentation.
     *
     * @return a new chain with the rounded value
     * @see #round()
     * @since 1.0.0
     */
    public NumberChain rounded() { return round(); }

    /**
     * Appends a step that takes the floor of the value.
     *
     * @return a new chain with the floor applied
     * @see #ceil()
     * @see #truncate()
     * @since 1.0.0
     */
    public NumberChain floor() { return new NumberChain(Math.floor(value)); }

    /**
     * Appends a step that takes the ceiling of the value.
     *
     * @return a new chain with the ceiling applied
     * @see #floor()
     * @since 1.0.0
     */
    public NumberChain ceil() { return new NumberChain(Math.ceil(value)); }

    /**
     * Appends a step that truncates the value toward zero — the integer
     * part, discarding any fractional portion.
     *
     * @return a new chain with the truncated value
     * @see #floor()
     * @see #round()
     * @since 1.0.0
     */
    public NumberChain truncate() { return new NumberChain((long) value); }

    /**
     * Appends a step that clamps the value into the inclusive range
     * {@code [min, max]}.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return a new chain with the clamped value
     * @see #clamp(double, double)
     * @see #limit(int, int)
     * @see #bound(int, int)
     * @see Numbers#clamp(int, int, int)
     * @since 1.0.0
     */
    public NumberChain clamp(int min, int max) { return new NumberChain(Numbers.clamp(value, min, max)); }

    /**
     * Appends a step that clamps the value into the inclusive range
     * {@code [min, max]}.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return a new chain with the clamped value
     * @see #clamp(int, int)
     * @since 1.0.0
     */
    public NumberChain clamp(double min, double max) { return new NumberChain(Numbers.clamp(value, min, max)); }

    /**
     * Alias of {@link #clamp(int, int)} — see that method for full
     * documentation.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return a new chain with the clamped value
     * @see #clamp(int, int)
     * @since 1.0.0
     */
    public NumberChain limit(int min, int max) { return clamp(min, max); }

    /**
     * Alias of {@link #clamp(int, int)} — see that method for full
     * documentation.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return a new chain with the clamped value
     * @see #clamp(int, int)
     * @since 1.0.0
     */
    public NumberChain bound(int min, int max) { return clamp(min, max); }

    /**
     * Appends a step that returns the smaller of the current value and
     * {@code n}.
     *
     * @param n the comparison value
     * @return a new chain with the smaller value
     * @see #max(int)
     * @since 1.0.0
     */
    public NumberChain min(int n) { return new NumberChain(Math.min(value, n)); }

    /**
     * Appends a step that returns the larger of the current value and
     * {@code n}.
     *
     * @param n the comparison value
     * @return a new chain with the larger value
     * @see #min(int)
     * @since 1.0.0
     */
    public NumberChain max(int n) { return new NumberChain(Math.max(value, n)); }

    /**
     * Appends an arbitrary transformation function. The function receives
     * the current value and returns a new one.
     *
     * <p>A {@code null} operator is silently ignored — the chain is
     * returned unchanged. This makes it safe to write code like
     * {@code chain.apply(someFn)} where {@code someFn} may be null.</p>
     *
     * @param op the function to apply (may be null)
     * @return a new chain with the function applied, or the same chain
     * if {@code op} is null
     * @see #map(java.util.function.DoubleUnaryOperator)
     * @see #then(java.util.function.DoubleUnaryOperator)
     * @since 1.0.0
     */
    public NumberChain apply(java.util.function.DoubleUnaryOperator op) {
        if (op == null) return this;
        return new NumberChain(op.applyAsDouble(value));
    }

    /**
     * Alias of {@link #apply(java.util.function.DoubleUnaryOperator)} —
     * see that method for full documentation.
     *
     * @param op the function to apply
     * @return a new chain with the function applied
     * @see #apply(java.util.function.DoubleUnaryOperator)
     * @since 1.0.0
     */
    public NumberChain map(java.util.function.DoubleUnaryOperator op) { return apply(op); }

    /**
     * Alias of {@link #apply(java.util.function.DoubleUnaryOperator)} —
     * see that method for full documentation.
     *
     * @param op the function to apply
     * @return a new chain with the function applied
     * @see #apply(java.util.function.DoubleUnaryOperator)
     * @since 1.0.0
     */
    public NumberChain then(java.util.function.DoubleUnaryOperator op) { return apply(op); }

    // ============================================================== terminals

    /**
     * Returns the current value as a {@code double}.
     *
     * @return the chain's value
     * @see #value()
     * @see #result()
     * @see #get()
     * @see #asInt()
     * @see #asLong()
     * @since 1.0.0
     */
    public double asDouble() { return value; }

    /**
     * Alias of {@link #asDouble()} — see that method for full
     * documentation.
     *
     * @return the chain's value
     * @see #asDouble()
     * @since 1.0.0
     */
    public double value() { return value; }

    /**
     * Alias of {@link #asDouble()} — see that method for full
     * documentation.
     *
     * @return the chain's value
     * @see #asDouble()
     * @since 1.0.0
     */
    public double result() { return value; }

    /**
     * Alias of {@link #asDouble()} — see that method for full
     * documentation.
     *
     * @return the chain's value
     * @see #asDouble()
     * @since 1.0.0
     */
    public double get() { return value; }

    /**
     * Returns the current value truncated to an {@code int}.
     *
     * <p>Truncation, not rounding — {@code 3.9} becomes {@code 3}, not
     * {@code 4}. Use {@link #round()} before {@code asInt()} if you want
     * rounding.</p>
     *
     * @return the value truncated to int
     * @see #asDouble()
     * @see #intValue()
     * @see #toInt()
     * @since 1.0.0
     */
    public int asInt() { return (int) value; }

    /**
     * Alias of {@link #asInt()} — see that method for full documentation.
     *
     * @return the value truncated to int
     * @see #asInt()
     * @since 1.0.0
     */
    public int intValue() { return (int) value; }

    /**
     * Alias of {@link #asInt()} — see that method for full documentation.
     *
     * @return the value truncated to int
     * @see #asInt()
     * @since 1.0.0
     */
    public int toInt() { return (int) value; }

    /**
     * Returns the current value truncated to a {@code long}.
     *
     * <p>Truncation, not rounding. Values outside the {@code long} range
     * will overflow silently — pre-clamp if you need a bounded result.</p>
     *
     * @return the value truncated to long
     * @see #asDouble()
     * @see #longValue()
     * @see #toLong()
     * @since 1.0.0
     */
    public long asLong() { return (long) value; }

    /**
     * Alias of {@link #asLong()} — see that method for full documentation.
     *
     * @return the value truncated to long
     * @see #asLong()
     * @since 1.0.0
     */
    public long longValue() { return (long) value; }

    /**
     * Alias of {@link #asLong()} — see that method for full documentation.
     *
     * @return the value truncated to long
     * @see #asLong()
     * @since 1.0.0
     */
    public long toLong() { return (long) value; }

    // ============================================================== formatted terminals

    /**
     * Returns the value formatted with two decimal places.
     *
     * <pre>{@code
     * Numbers.of(3.14159).formatted();   // "3.14"
     * }</pre>
     *
     * @return the formatted string
     * @see #formatted(int)
     * @see Numbers#format(double, int)
     * @since 1.0.0
     */
    public String formatted() { return String.format(Locale.ENGLISH, "%.2f", value); }

    /**
     * Returns the value formatted with the given number of decimal places.
     *
     * @param decimals the number of decimal places
     * @return the formatted string
     * @see #formatted()
     * @see Numbers#format(double, int)
     * @since 1.0.0
     */
    public String formatted(int decimals) { return Numbers.format(value, decimals); }

    /**
     * Alias of {@link #formatted()} — see that method for full
     * documentation.
     *
     * @return the formatted string
     * @see #formatted()
     * @since 1.0.0
     */
    public String asString() { return formatted(); }

    /**
     * Alias of {@link #formatted()} — see that method for full
     * documentation.
     *
     * @return the formatted string
     * @see #formatted()
     * @since 1.0.0
     */
    @Override
    public String toString() { return formatted(); }

    /**
     * Returns the value as an ordinal string — {@code "1st"},
     * {@code "2nd"}, etc. Delegates to {@link Numbers#ordinal(int)}.
     *
     * @return the ordinal string
     * @see Numbers#ordinal(int)
     * @since 1.0.0
     */
    public String toOrdinal() { return Numbers.ordinal((int) value); }

    /**
     * Returns the value as a Roman numeral. Delegates to
     * {@link Numbers#roman(int)}. Values outside 1–3999 return the
     * decimal string.
     *
     * @return the Roman numeral
     * @see Numbers#roman(int)
     * @since 1.0.0
     */
    public String toRoman() { return Numbers.roman((int) value); }

    /**
     * Returns the value with thousands separators. Delegates to
     * {@link Numbers#thousands(long)}.
     *
     * <pre>{@code
     * Numbers.of(1234).toThousands();   // "1,234"
     * }</pre>
     *
     * @return the grouped string
     * @see Numbers#thousands(long)
     * @since 1.0.0
     */
    public String toThousands() { return Numbers.thousands((long) value); }

    /**
     * Returns the value as a binary string. Delegates to
     * {@link Numbers#toBinary(int)}.
     *
     * @return the binary representation
     * @see Numbers#toBinary(int)
     * @since 1.0.0
     */
    public String toBinary() { return Numbers.toBinary((int) value); }

    /**
     * Returns the value as a hexadecimal string. Delegates to
     * {@link Numbers#toHex(int)}.
     *
     * @return the hex representation
     * @see Numbers#toHex(int)
     * @since 1.0.0
     */
    public String toHex() { return Numbers.toHex((int) value); }

    /**
     * Returns the value in abbreviated form — {@code "1.5K"},
     * {@code "2.3M"}, etc. Delegates to {@link Numbers#abbreviate(long)}.
     *
     * @return the abbreviated string
     * @see Numbers#abbreviate(long)
     * @since 1.0.0
     */
    public String toAbbreviated() { return Numbers.abbreviate((long) value); }

    // ============================================================== predicates

    /**
     * Checks whether the value is even (must be a whole number — the
     * fractional part is truncated first).
     *
     * @return {@code true} if the truncated value is even
     * @see #isOdd()
     * @see Numbers#isEven(int)
     * @since 1.0.0
     */
    public boolean isEven() { return Numbers.isEven((int) value); }

    /**
     * Checks whether the value is odd (must be a whole number — the
     * fractional part is truncated first).
     *
     * @return {@code true} if the truncated value is odd
     * @see #isEven()
     * @see Numbers#isOdd(int)
     * @since 1.0.0
     */
    public boolean isOdd() { return Numbers.isOdd((int) value); }

    /**
     * Checks whether the value is greater than 0.
     *
     * @return {@code true} if the value is positive
     * @see #isNegative()
     * @see #isZero()
     * @since 1.0.0
     */
    public boolean isPositive() { return value > 0; }

    /**
     * Checks whether the value is less than 0.
     *
     * @return {@code true} if the value is negative
     * @see #isPositive()
     * @see #isZero()
     * @since 1.0.0
     */
    public boolean isNegative() { return value < 0; }

    /**
     * Checks whether the value is exactly 0.
     *
     * @return {@code true} if the value is zero
     * @see #isPositive()
     * @see #isNegative()
     * @since 1.0.0
     */
    public boolean isZero() { return value == 0; }

    /**
     * Checks whether the value is a prime (must be a whole number — the
     * fractional part is truncated first).
     *
     * @return {@code true} if the truncated value is prime
     * @see Numbers#isPrime(int)
     * @since 1.0.0
     */
    public boolean isPrime() { return Numbers.isPrime((int) value); }

    /**
     * Checks whether the value is a whole number (has no fractional
     * part) and is finite.
     *
     * @return {@code true} if the value equals its own rounding and is
     * not infinite
     * @see Numbers#isInteger(Object)
     * @since 1.0.0
     */
    public boolean isInteger() { return value == Math.rint(value) && !Double.isInfinite(value); }
}
