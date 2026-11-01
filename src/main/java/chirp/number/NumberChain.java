package chirp.number;

import java.util.Locale;

/**
 * Immutable fluent numeric chain — demonstrates <b>both</b> the
 * <b>Decorator pattern</b> (every operation returns a new chain wrapping the
 * previous) and the <b>Builder pattern</b> (the terminal {@code asInt()},
 * {@code asDouble()}, {@code formatted()}, etc. finalise the chain).
 *
 * <p>Internal value is a {@code double}, so the chain handles both int and
 * double math. {@link #asInt()} truncates; callers who need exact integer
 * arithmetic for very large values should use {@link Numbers} directly.</p>
 *
 * <h2>Aliases per operation</h2>
 * <ul>
 *   <li>{@code add} / {@code plus} / {@code and} / {@code sum}</li>
 *   <li>{@code subtract} / {@code minus} / {@code sub} / {@code take}</li>
 *   <li>{@code multiply} / {@code times} / {@code mul}</li>
 *   <li>{@code divide} / {@code div} / {@code dividedBy} / {@code over}</li>
 *   <li>{@code mod} / {@code modulo} / {@code rem}</li>
 *   <li>{@code pow} / {@code power} / {@code raisedTo}</li>
 * </ul>
 *
 * <pre>{@code
 * int result = Numbers.of(10).add(5).times(2).clamp(0, 20).asInt(); // 20
 * String s  = Numbers.of(3.14159).round(2).formatted();             // "3.14"
 * }</pre>
 */
public final class NumberChain {

    private final double value;

    private NumberChain(double value) { this.value = value; }

    // ============================================================== factories

    public static NumberChain of(int n) { return new NumberChain(n); }
    public static NumberChain of(double n) { return new NumberChain(n); }
    public static NumberChain from(int n) { return of(n); }
    public static NumberChain from(double n) { return of(n); }
    public static NumberChain start(int n) { return of(n); }
    public static NumberChain start(double n) { return of(n); }

    // ============================================================== arithmetic

    public NumberChain add(int n) { return new NumberChain(value + n); }
    public NumberChain add(double n) { return new NumberChain(value + n); }
    public NumberChain plus(int n) { return add(n); }
    public NumberChain plus(double n) { return add(n); }
    public NumberChain sum(int n) { return add(n); }
    public NumberChain and(int n) { return add(n); }

    public NumberChain subtract(int n) { return new NumberChain(value - n); }
    public NumberChain subtract(double n) { return new NumberChain(value - n); }
    public NumberChain minus(int n) { return subtract(n); }
    public NumberChain minus(double n) { return subtract(n); }
    public NumberChain sub(int n) { return subtract(n); }
    public NumberChain take(int n) { return subtract(n); }

    public NumberChain multiply(int n) { return new NumberChain(value * n); }
    public NumberChain multiply(double n) { return new NumberChain(value * n); }
    public NumberChain times(int n) { return multiply(n); }
    public NumberChain times(double n) { return multiply(n); }
    public NumberChain mul(int n) { return multiply(n); }

    public NumberChain divide(int n) { return n == 0 ? new NumberChain(0) : new NumberChain(value / n); }
    public NumberChain divide(double n) { return n == 0 ? new NumberChain(0) : new NumberChain(value / n); }
    public NumberChain div(int n) { return divide(n); }
    public NumberChain dividedBy(int n) { return divide(n); }
    public NumberChain over(int n) { return divide(n); }

    public NumberChain mod(int n) { return n == 0 ? this : new NumberChain(Numbers.mod((int) value, n)); }
    public NumberChain modulo(int n) { return mod(n); }
    public NumberChain rem(int n) { return mod(n); }

    public NumberChain pow(double exponent) { return new NumberChain(Math.pow(value, exponent)); }
    public NumberChain power(double exp) { return pow(exp); }
    public NumberChain raisedTo(double exp) { return pow(exp); }

    // ============================================================== transforms

    public NumberChain abs() { return new NumberChain(Math.abs(value)); }
    public NumberChain absolute() { return abs(); }
    public NumberChain negate() { return new NumberChain(-value); }
    public NumberChain negative() { return negate(); }
    public NumberChain flipSign() { return negate(); }

    public NumberChain sqrt() { return new NumberChain(Numbers.sqrt(value)); }
    public NumberChain squareRoot() { return sqrt(); }

    public NumberChain round() { return new NumberChain(Math.round(value)); }
    public NumberChain round(int decimals) { return new NumberChain(Numbers.round(value, decimals)); }
    public NumberChain rounded() { return round(); }

    public NumberChain floor() { return new NumberChain(Math.floor(value)); }
    public NumberChain ceil() { return new NumberChain(Math.ceil(value)); }
    public NumberChain truncate() { return new NumberChain((long) value); }

    public NumberChain clamp(int min, int max) { return new NumberChain(Numbers.clamp(value, min, max)); }
    public NumberChain clamp(double min, double max) { return new NumberChain(Numbers.clamp(value, min, max)); }
    public NumberChain limit(int min, int max) { return clamp(min, max); }
    public NumberChain bound(int min, int max) { return clamp(min, max); }

    public NumberChain min(int n) { return new NumberChain(Math.min(value, n)); }
    public NumberChain max(int n) { return new NumberChain(Math.max(value, n)); }

    public NumberChain apply(java.util.function.DoubleUnaryOperator op) {
        if (op == null) return this;
        return new NumberChain(op.applyAsDouble(value));
    }
    public NumberChain map(java.util.function.DoubleUnaryOperator op) { return apply(op); }
    public NumberChain then(java.util.function.DoubleUnaryOperator op) { return apply(op); }

    // ============================================================== terminals

    public double asDouble() { return value; }
    public double value() { return value; }
    public double result() { return value; }
    public double get() { return value; }

    public int asInt() { return (int) value; }
    public int intValue() { return (int) value; }
    public int toInt() { return (int) value; }

    public long asLong() { return (long) value; }
    public long longValue() { return (long) value; }
    public long toLong() { return (long) value; }

    // ============================================================== formatted terminals

    public String formatted() { return String.format(Locale.ENGLISH, "%.2f", value); }
    public String formatted(int decimals) { return Numbers.format(value, decimals); }
    public String asString() { return formatted(); }
    public String toString() { return formatted(); }

    public String toOrdinal() { return Numbers.ordinal((int) value); }
    public String toRoman() { return Numbers.roman((int) value); }
    public String toThousands() { return Numbers.thousands((long) value); }
    public String toBinary() { return Numbers.toBinary((int) value); }
    public String toHex() { return Numbers.toHex((int) value); }
    public String toAbbreviated() { return Numbers.abbreviate((long) value); }

    // ============================================================== predicates

    public boolean isEven() { return Numbers.isEven((int) value); }
    public boolean isOdd() { return Numbers.isOdd((int) value); }
    public boolean isPositive() { return value > 0; }
    public boolean isNegative() { return value < 0; }
    public boolean isZero() { return value == 0; }
    public boolean isPrime() { return Numbers.isPrime((int) value); }
    public boolean isInteger() { return value == Math.rint(value) && !Double.isInfinite(value); }
}
