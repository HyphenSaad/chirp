package chirp.number;

import chirp.format.Format;
import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Numeric helpers — null-safe, never throws for user input.
 *
 * <p>This is the most heavily aliased class in the library. Every method
 * has 2–5 alternate names so a caller can use whichever verb reads most
 * naturally. All aliases are pure delegates — same behaviour, different
 * name — never separate implementations.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Random</b> — {@link #random(int, int)},
 *       {@link #randomDouble(double, double)},
 *       {@link #randomBoolean()}, {@link #randomItem(Object[])}</li>
 *   <li><b>Clamping</b> — {@link #clamp(int, int, int)},
 *       {@link #wrap(int, int, int)}</li>
 *   <li><b>Math</b> — {@link #pow(double, double)},
 *       {@link #sqrt(double)}, {@link #abs(int)},
 *       {@link #mod(int, int)}, trig functions</li>
 *   <li><b>Number theory</b> — {@link #isPrime(int)},
 *       {@link #primesUpTo(int)}, {@link #factorial(int)},
 *       {@link #fibonacci(int)}, {@link #gcd(int, int)}</li>
 *   <li><b>Digits</b> — {@link #sumDigits(int)},
 *       {@link #countDigits(int)}, {@link #reverseDigits(int)}</li>
 *   <li><b>Bases</b> — {@link #toBinary(int)}, {@link #toHex(int)},
 *       {@link #fromBinary(String)}</li>
 *   <li><b>Parsing</b> — {@link #parseInt(String)} (returns {@code null}
 *       on failure), {@link #parseDouble(String)},
 *       {@link #parseInt(String, Integer)}</li>
 *   <li><b>Formatting</b> — {@link #ordinal(int)},
 *       {@link #thousands(long)}, {@link #abbreviate(long)},
 *       {@link #fileSize(long)}, {@link #roman(int)}</li>
 *   <li><b>Ranges</b> — {@link #range(int, int)},
 *       {@link #rangeInclusive(int, int)}</li>
 *   <li><b>Aggregation</b> — {@link #sum(int[])},
 *       {@link #average(int[])}, {@link #max(int[])},
 *       {@link #min(int[])}</li>
 *   <li><b>Statistics</b> — {@link #statistics(int[])},
 *       {@link #median(int[])}, {@link #mode(int[])},
 *       {@link #stdDev(int[])}</li>
 *   <li><b>Percent</b> — {@link #percent(double, double)}</li>
 *   <li><b>Fluent chains</b> — {@link #of(int)} → {@link NumberChain}</li>
 * </ul>
 *
 * <h2>Null-safety contract</h2>
 * <ul>
 *   <li>Parsers return {@code null} on failure (or the provided default) —
 *       never throw</li>
 *   <li>Array aggregators ({@code sum}, {@code max}, …) return
 *       {@code 0} for {@code null} or empty arrays</li>
 *   <li>Search predicates ({@code contains}, {@code isInteger}) return
 *       {@code false} for {@code null}</li>
 *   <li>Ranges with reversed or empty bounds return empty lists</li>
 * </ul>
 *
 * <h2>Randomness</h2>
 * <p>{@link #random(int, int)}, {@link #randomDouble(double, double)}, and
 * {@link #randomBoolean()} use {@link java.util.concurrent.ThreadLocalRandom}.
 * That is the correct choice for non-cryptographic use cases (seed data,
 * pick-a-random-item helpers, exercise generation) — thread-safe,
 * contention-free, fast. It is <b>not</b> a cryptographic RNG; for tokens,
 * salts, or keys, use {@link java.security.SecureRandom} directly.</p>
 *
 * <h2>Example — a typical exercise loop</h2>
 * <pre>{@code
 * int target = Numbers.random(1, 100);
 * int guess;
 * int attempts = 0;
 * do {
 *     guess = Input.readInt("Your guess", 1, 100);
 *     attempts++;
 *     if (guess < target) println("Higher");
 *     else if (guess > target) println("Lower");
 * } while (guess != target);
 * println(Numbers.ordinal(attempts) + " attempt!");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For fluent numeric chains, see {@link NumberChain}. For formatting
 * (currency, percent, ordinals), see {@link Format}.</p>
 *
 * @see NumberChain
 * @see Format
 * @see chirp.collection.ArraysUtil
 * @since 1.0.0
 */
public final class Numbers {

    private Numbers() {}

    /**
     * Key enum for the key-value overloads in this module.
     *
     * <p>Only three overloads consume these — {@link #statistics(int[], Object...)},
     * {@link #format(double, Object...)}, and {@link #range(int, int, Object...)}.
     * All other methods take plain arguments.</p>
     *
     * @since 1.0.0
     */
    public enum Key {
        /** Number of decimal places for {@link #format(double, Object...)}. */
        DECIMALS,
        /** Whether {@link #format(double, Object...)} groups thousands. */
        THOUSANDS,
        /** The {@link Locale} for {@link #format(double, Object...)}. */
        LOCALE,
        /** Whether to compute sample std-dev (n−1) or population (n). */
        SAMPLE,
        /** Step size for {@link #range(int, int, Object...)}. */
        STEP,
        /** Currently unused; reserved for future inclusive-range overloads. */
        INCLUSIVE
    }

    // ============================================================== random

    /**
     * Returns a random integer in the inclusive range {@code [min, max]}.
     *
     * <p>If {@code min > max}, returns {@code min} unchanged (no exception,
     * no swap). This makes {@code random(a, b)} always safe to call.</p>
     *
     * <p>Uses {@link java.util.concurrent.ThreadLocalRandom} — thread-safe,
     * non-cryptographic. See the class-level "Randomness" note for details.</p>
     *
     * <pre>{@code
     * Numbers.random(1, 6);      // 1–6, like a die
     * Numbers.random(0, 100);    // 0–100 inclusive
     * Numbers.random(50, 10);    // 50 (invalid range → clamped)
     * }</pre>
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in {@code [min, max]}, or {@code min} if
     * {@code min > max}
     * @see #rand(int, int)
     * @see #randomInt(int, int)
     * @see #randomBetween(int, int)
     * @see #randomDouble(double, double)
     * @see #randomBoolean()
     * @since 1.0.0
     */
    public static int random(int min, int max) {
        if (min > max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Alias of {@link #random(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in {@code [min, max]}
     * @see #random(int, int)
     * @since 1.0.0
     */
    public static int rand(int min, int max) { return random(min, max); }

    /**
     * Alias of {@link #random(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in {@code [min, max]}
     * @see #random(int, int)
     * @since 1.0.0
     */
    public static int randomInt(int min, int max) { return random(min, max); }

    /**
     * Alias of {@link #random(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in {@code [min, max]}
     * @see #random(int, int)
     * @since 1.0.0
     */
    public static int randomBetween(int min, int max) { return random(min, max); }

    /**
     * Alias of {@link #random(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in {@code [min, max]}
     * @see #random(int, int)
     * @since 1.0.0
     */
    public static int randomFrom(int min, int max) { return random(min, max); }

    /**
     * Returns a random {@code double} in {@code [min, max)} — upper bound
     * exclusive, unlike {@link #random(int, int)} which is inclusive.
     *
     * <p>If {@code min > max}, returns {@code min} unchanged.</p>
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random {@code double} in {@code [min, max)}
     * @see #randDouble(double, double)
     * @see #random(int, int)
     * @since 1.0.0
     */
    public static double randomDouble(double min, double max) {
        if (min > max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Alias of {@link #randomDouble(double, double)} — see that method for
     * full documentation.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random {@code double} in {@code [min, max)}
     * @see #randomDouble(double, double)
     * @since 1.0.0
     */
    public static double randDouble(double min, double max) { return randomDouble(min, max); }

    /**
     * Alias of {@link #randomDouble(double, double)} — see that method for
     * full documentation.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random {@code double} in {@code [min, max)}
     * @see #randomDouble(double, double)
     * @since 1.0.0
     */
    public static double randomFloat(double min, double max) { return randomDouble(min, max); }

    /**
     * Returns a random boolean with 50/50 probability.
     *
     * @return a random boolean
     * @see #randomBool()
     * @see #flipCoin()
     * @since 1.0.0
     */
    public static boolean randomBoolean() { return ThreadLocalRandom.current().nextBoolean(); }

    /**
     * Alias of {@link #randomBoolean()} — see that method for full
     * documentation.
     *
     * @return a random boolean
     * @see #randomBoolean()
     * @since 1.0.0
     */
    public static boolean randomBool() { return randomBoolean(); }

    /**
     * Alias of {@link #randomBoolean()} — see that method for full
     * documentation.
     *
     * @return a random boolean
     * @see #randomBoolean()
     * @since 1.0.0
     */
    public static boolean flipCoin() { return randomBoolean(); }

    /**
     * Returns a random element from the array.
     *
     * @param items the array to pick from (may be null or empty)
     * @param <T> the element type
     * @return a random element, or {@code null} if {@code items} is null
     * or empty
     * @see #randomItem(List)
     * @see #pick(Object[])
     * @since 1.0.0
     */
    public static <T> T randomItem(T[] items) {
        if (items == null || items.length == 0) return null;
        return items[random(0, items.length - 1)];
    }

    /**
     * Returns a random element from the list.
     *
     * @param items the list to pick from (may be null or empty)
     * @param <T> the element type
     * @return a random element, or {@code null} if {@code items} is null
     * or empty
     * @see #randomItem(Object[])
     * @see #pick(List)
     * @since 1.0.0
     */
    public static <T> T randomItem(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(random(0, items.size() - 1));
    }

    /**
     * Alias of {@link #randomItem(Object[])} — see that method for full
     * documentation.
     *
     * @param items the array to pick from
     * @param <T> the element type
     * @return a random element
     * @see #randomItem(Object[])
     * @since 1.0.0
     */
    public static <T> T pick(T[] items) { return randomItem(items); }

    /**
     * Alias of {@link #randomItem(List)} — see that method for full
     * documentation.
     *
     * @param items the list to pick from
     * @param <T> the element type
     * @return a random element
     * @see #randomItem(List)
     * @since 1.0.0
     */
    public static <T> T pick(List<T> items) { return randomItem(items); }

    /**
     * Generates an array of {@code size} random ints in {@code [min, max]}.
     *
     * @param size the number of elements (return empty for size ≤ 0)
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return the generated array (never null)
     * @see #randomList(int, int, int)
     * @since 1.0.0
     */
    public static int[] randomArray(int size, int min, int max) {
        if (size <= 0) return new int[0];
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = random(min, max);
        return arr;
    }

    /**
     * Generates a list of {@code size} random ints in {@code [min, max]}.
     *
     * @param size the number of elements (return empty for size ≤ 0)
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return the generated list (never null)
     * @see #randomArray(int, int, int)
     * @since 1.0.0
     */
    public static List<Integer> randomList(int size, int min, int max) {
        if (size <= 0) return List.of();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(random(min, max));
        return list;
    }

    // ============================================================== clamping

    /**
     * Clamps the value into the inclusive range {@code [min, max]}.
     *
     * <pre>{@code
     * Numbers.clamp(50, 0, 100);    // 50
     * Numbers.clamp(150, 0, 100);   // 100
     * Numbers.clamp(-1, 0, 100);    // 0
     * }</pre>
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #limit(int, int, int)
     * @see #bound(int, int, int)
     * @see #constrain(int, int, int)
     * @see #wrap(int, int, int)
     * @since 1.0.0
     */
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    /**
     * Alias of {@link #clamp(int, int, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #clamp(int, int, int)
     * @since 1.0.0
     */
    public static int limit(int value, int min, int max) { return clamp(value, min, max); }

    /**
     * Alias of {@link #clamp(int, int, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #clamp(int, int, int)
     * @since 1.0.0
     */
    public static int bound(int value, int min, int max) { return clamp(value, min, max); }

    /**
     * Alias of {@link #clamp(int, int, int)} — see that method for full
     * documentation and examples.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #clamp(int, int, int)
     * @since 1.0.0
     */
    public static int constrain(int value, int min, int max) { return clamp(value, min, max); }

    /**
     * Wraps the value into the inclusive range {@code [min, max]},
     * wrapping around rather than clipping.
     *
     * <pre>{@code
     * Numbers.wrap(10, 0, 5);   // 4  (10 mod 6 = 4)
     * Numbers.wrap(11, 0, 5);   // 5
     * Numbers.wrap(-1, 0, 5);   // 5  (wraps from below)
     * }</pre>
     *
     * @param value the value to wrap
     * @param min the minimum allowed value
     * @param max the maximum allowed value (inclusive)
     * @return the wrapped value in {@code [min, max]}
     * @see #clamp(int, int, int)
     * @since 1.0.0
     */
    public static int wrap(int value, int min, int max) {
        if (max <= min) return min;
        int range = max - min + 1;
        int mod = ((value - min) % range + range) % range;
        return min + mod;
    }

    /**
     * Clamps the value into the inclusive range {@code [min, max]}.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #clamp(int, int, int)
     * @see #limit(double, double, double)
     * @since 1.0.0
     */
    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    /**
     * Alias of {@link #clamp(double, double, double)} — see that method for
     * full documentation.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     * @see #clamp(double, double, double)
     * @since 1.0.0
     */
    public static double limit(double value, double min, double max) { return clamp(value, min, max); }

    // ============================================================== basics

    /**
     * Returns the absolute value of the int.
     *
     * @param n the value
     * @return {@code |n|}
     * @see #abs(long)
     * @see #abs(double)
     * @since 1.0.0
     */
    public static int abs(int n) { return Math.abs(n); }

    /**
     * Returns the absolute value of the long.
     *
     * @param n the value
     * @return {@code |n|}
     * @see #abs(int)
     * @since 1.0.0
     */
    public static long abs(long n) { return Math.abs(n); }

    /**
     * Returns the absolute value of the double.
     *
     * @param n the value
     * @return {@code |n|}
     * @see #abs(int)
     * @since 1.0.0
     */
    public static double abs(double n) { return Math.abs(n); }

    /**
     * Returns the sign of the integer: {@code -1}, {@code 0}, or {@code 1}.
     *
     * @param n the value
     * @return the sign
     * @see #signum(int)
     * @see #signOf(int)
     * @since 1.0.0
     */
    public static int sign(int n) { return Integer.signum(n); }

    /**
     * Alias of {@link #sign(int)} — see that method for full
     * documentation.
     *
     * @param n the value
     * @return the sign
     * @see #sign(int)
     * @since 1.0.0
     */
    public static int signum(int n) { return sign(n); }

    /**
     * Alias of {@link #sign(int)} — see that method for full
     * documentation.
     *
     * @param n the value
     * @return the sign
     * @see #sign(int)
     * @since 1.0.0
     */
    public static int signOf(int n) { return sign(n); }

    /**
     * Returns the always-positive modulus — unlike {@code %}, the result
     * is never negative.
     *
     * <pre>{@code
     * Numbers.mod(7, 3);    // 1
     * Numbers.mod(-7, 3);   // 2  (Java's -7 % 3 = -1)
     * Numbers.mod(5, 0);    // 0  (safe division by zero)
     * }</pre>
     *
     * @param a the dividend
     * @param b the divisor (may be 0 — returns 0)
     * @return a non-negative remainder in {@code [0, |b|)}
     * @see #modulo(int, int)
     * @since 1.0.0
     */
    public static int mod(int a, int b) {
        if (b == 0) return 0;
        int r = a % b;
        return r < 0 ? r + Math.abs(b) : r;
    }

    /**
     * Alias of {@link #mod(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param a the dividend
     * @param b the divisor
     * @return the always-positive modulus
     * @see #mod(int, int)
     * @since 1.0.0
     */
    public static int modulo(int a, int b) { return mod(a, b); }

    /**
     * Returns {@code base} raised to the power of {@code exponent}.
     *
     * @param base the base
     * @param exponent the exponent
     * @return the result
     * @see #power(double, double)
     * @see #powInt(int, int)
     * @since 1.0.0
     */
    public static double pow(double base, double exponent) { return Math.pow(base, exponent); }

    /**
     * Alias of {@link #pow(double, double)} — see that method for full
     * documentation.
     *
     * @param base the base
     * @param exponent the exponent
     * @return the result
     * @see #pow(double, double)
     * @since 1.0.0
     */
    public static double power(double base, double exponent) { return pow(base, exponent); }

    /**
     * Returns {@code base} raised to the power of {@code exponent} as an
     * {@code int}.
     *
     * @param base the base
     * @param exponent the exponent (should be small — overflows fast)
     * @return the result truncated to int
     * @see #pow(double, double)
     * @since 1.0.0
     */
    public static int powInt(int base, int exponent) { return (int) Math.pow(base, exponent); }

    /**
     * Returns the square root of the value, or {@code 0} for negative
     * input. Never returns {@code NaN}.
     *
     * @param n the value
     * @return the non-negative square root, or {@code 0} for {@code n < 0}
     * @see #squareRoot(double)
     * @see #cbrt(double)
     * @since 1.0.0
     */
    public static double sqrt(double n) { return n < 0 ? 0 : Math.sqrt(n); }

    /**
     * Alias of {@link #sqrt(double)} — see that method for full
     * documentation.
     *
     * @param n the value
     * @return the non-negative square root
     * @see #sqrt(double)
     * @since 1.0.0
     */
    public static double squareRoot(double n) { return sqrt(n); }

    /**
     * Returns the cube root of the value. Works correctly for negative
     * inputs.
     *
     * @param n the value
     * @return the cube root
     * @see #cubeRoot(double)
     * @see #sqrt(double)
     * @since 1.0.0
     */
    public static double cbrt(double n) { return Math.cbrt(n); }

    /**
     * Alias of {@link #cbrt(double)} — see that method for full
     * documentation.
     *
     * @param n the value
     * @return the cube root
     * @see #cbrt(double)
     * @since 1.0.0
     */
    public static double cubeRoot(double n) { return cbrt(n); }

    /**
     * Returns the hypotenuse length for a right triangle with legs
     * {@code a} and {@code b} — {@code √(a² + b²)}, computed without
     * intermediate overflow.
     *
     * @param a the first leg
     * @param b the second leg
     * @return the hypotenuse
     * @see #distance(double, double, double, double)
     * @since 1.0.0
     */
    public static double hypot(double a, double b) { return Math.hypot(a, b); }

    /**
     * Returns the Euclidean distance between two points.
     *
     * <pre>{@code
     * Numbers.distance(0, 0, 3, 4);   // 5.0
     * }</pre>
     *
     * @param x1 the first point's x
     * @param y1 the first point's y
     * @param x2 the second point's x
     * @param y2 the second point's y
     * @return the distance
     * @see #hypot(double, double)
     * @since 1.0.0
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    /**
     * Returns the natural logarithm. Safe for non-positive input —
     * returns {@code 0} instead of {@code NaN} or {@code -Infinity}.
     *
     * @param n the value
     * @return the natural log, or {@code 0} for {@code n <= 0}
     * @see #ln(double)
     * @see #log10(double)
     * @see #log2(double)
     * @since 1.0.0
     */
    public static double log(double n) { return n <= 0 ? 0 : Math.log(n); }

    /**
     * Alias of {@link #log(double)} — see that method for full
     * documentation.
     *
     * @param n the value
     * @return the natural log, or 0 for non-positive input
     * @see #log(double)
     * @since 1.0.0
     */
    public static double ln(double n) { return log(n); }

    /**
     * Returns the base-10 logarithm, or {@code 0} for non-positive input.
     *
     * @param n the value
     * @return the log base 10, or 0 for {@code n <= 0}
     * @see #log(double)
     * @see #log2(double)
     * @since 1.0.0
     */
    public static double log10(double n) { return n <= 0 ? 0 : Math.log10(n); }

    /**
     * Returns the base-2 logarithm, or {@code 0} for non-positive input.
     *
     * @param n the value
     * @return the log base 2, or 0 for {@code n <= 0}
     * @see #log(double)
     * @see #logBase(double, double)
     * @since 1.0.0
     */
    public static double log2(double n) { return n <= 0 ? 0 : Math.log(n) / Math.log(2); }

    /**
     * Returns the logarithm of {@code n} to an arbitrary {@code base},
     * or {@code 0} for invalid inputs.
     *
     * @param n the value (must be &gt; 0)
     * @param base the logarithm base (must be &gt; 0 and ≠ 1)
     * @return the log of {@code n} base {@code base}, or 0 for invalid
     * inputs
     * @see #log(double)
     * @see #log2(double)
     * @see #log10(double)
     * @since 1.0.0
     */
    public static double logBase(double n, double base) {
        if (n <= 0 || base <= 0 || base == 1) return 0;
        return Math.log(n) / Math.log(base);
    }

    /**
     * Returns {@code e} raised to the power of {@code n}.
     *
     * @param n the exponent
     * @return {@code e^n}
     * @see #log(double)
     * @since 1.0.0
     */
    public static double exp(double n) { return Math.exp(n); }

    /**
     * Returns the largest integer ≤ {@code n} as an {@code int}.
     *
     * @param n the value
     * @return the floor
     * @see #ceil(double)
     * @see #roundToInt(double)
     * @since 1.0.0
     */
    public static int floor(double n) { return (int) Math.floor(n); }

    /**
     * Returns the smallest integer ≥ {@code n} as an {@code int}.
     *
     * @param n the value
     * @return the ceiling
     * @see #floor(double)
     * @see #roundToInt(double)
     * @since 1.0.0
     */
    public static int ceil(double n) { return (int) Math.ceil(n); }

    /**
     * Rounds the value to the nearest integer and returns it as an
     * {@code int} — half-up.
     *
     * @param n the value
     * @return the rounded int
     * @see #roundToLong(double)
     * @see #round(double, int)
     * @since 1.0.0
     */
    public static int roundToInt(double n) { return (int) Math.round(n); }

    /**
     * Rounds the value to the nearest integer and returns it as a
     * {@code long} — half-up. Useful for values that may exceed
     * {@code Integer.MAX_VALUE}.
     *
     * @param n the value
     * @return the rounded long
     * @see #roundToInt(double)
     * @see #round(double, int)
     * @since 1.0.0
     */
    public static long roundToLong(double n) { return Math.round(n); }

    /**
     * Checks whether the given object holds an integer-valued number —
     * an Integer, Long, Short, Byte, or a Double/Float whose value is
     * mathematically a whole number.
     *
     * <p>Note: {@code 42.0} returns {@code true}; {@code 42.5} returns
     * {@code false}. Numeric strings like {@code "42"} return
     * {@code false} — use {@link #parseInt(String)} for those.</p>
     *
     * @param value the value to check (may be null)
     * @return {@code true} if the value is mathematically a whole number
     * @see #isWhole(Object)
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static boolean isInteger(Object value) {
        if (value instanceof Integer || value instanceof Long
                || value instanceof Short || value instanceof Byte)
            return true;
        if (value instanceof Double d) return d == Math.rint(d) && !Double.isInfinite(d);
        if (value instanceof Float f) return f == Math.rint(f) && !Float.isInfinite(f);
        return false;
    }

    /**
     * Alias of {@link #isInteger(Object)} — see that method for full
     * documentation.
     *
     * @param value the value to check (may be null)
     * @return {@code true} if the value is mathematically whole
     * @see #isInteger(Object)
     * @since 1.0.0
     */
    public static boolean isWhole(Object value) { return isInteger(value); }

    // ============================================================== trigonometry

    /**
     * Returns the sine of an angle in radians.
     *
     * @param radians the angle
     * @return the sine
     * @see #toRadians(double)
     * @since 1.0.0
     */
    public static double sin(double radians) { return Math.sin(radians); }

    /**
     * Returns the cosine of an angle in radians.
     *
     * @param radians the angle
     * @return the cosine
     * @see #toRadians(double)
     * @since 1.0.0
     */
    public static double cos(double radians) { return Math.cos(radians); }

    /**
     * Returns the tangent of an angle in radians.
     *
     * @param radians the angle
     * @return the tangent
     * @see #toRadians(double)
     * @since 1.0.0
     */
    public static double tan(double radians) { return Math.tan(radians); }

    /**
     * Returns the arc-sine in the range {@code [-π/2, π/2]}.
     *
     * @param value the sine value in {@code [-1, 1]}
     * @return the angle in radians, or {@code NaN} for out-of-range input
     * @since 1.0.0
     */
    public static double asin(double value) { return Math.asin(value); }

    /**
     * Returns the arc-cosine in the range {@code [0, π]}.
     *
     * @param value the cosine value in {@code [-1, 1]}
     * @return the angle in radians, or {@code NaN} for out-of-range input
     * @since 1.0.0
     */
    public static double acos(double value) { return Math.acos(value); }

    /**
     * Returns the arc-tangent in the range {@code [-π/2, π/2]}.
     *
     * @param value the tangent value
     * @return the angle in radians
     * @see #atan2(double, double)
     * @since 1.0.0
     */
    public static double atan(double value) { return Math.atan(value); }

    /**
     * Returns the angle of the point {@code (x, y)} in radians,
     * resolving the quadrant correctly.
     *
     * @param y the y coordinate
     * @param x the x coordinate
     * @return the angle in radians in {@code [-π, π]}
     * @see #atan(double)
     * @since 1.0.0
     */
    public static double atan2(double y, double x) { return Math.atan2(y, x); }

    /**
     * Converts degrees to radians.
     *
     * @param degrees the angle in degrees
     * @return the angle in radians
     * @see #toDegrees(double)
     * @since 1.0.0
     */
    public static double toRadians(double degrees) { return Math.toRadians(degrees); }

    /**
     * Converts radians to degrees.
     *
     * @param radians the angle in radians
     * @return the angle in degrees
     * @see #toRadians(double)
     * @since 1.0.0
     */
    public static double toDegrees(double radians) { return Math.toDegrees(radians); }

    // ============================================================== parity

    /**
     * Checks whether the number is even.
     *
     * @param n the number
     * @return {@code true} if {@code n} is divisible by 2
     * @see #isOdd(int)
     * @see #even(int)
     * @since 1.0.0
     */
    public static boolean isEven(int n) { return n % 2 == 0; }

    /**
     * Checks whether the number is odd.
     *
     * @param n the number
     * @return {@code true} if {@code n} is not divisible by 2
     * @see #isEven(int)
     * @see #odd(int)
     * @since 1.0.0
     */
    public static boolean isOdd(int n) { return n % 2 != 0; }

    /**
     * Alias of {@link #isEven(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return {@code true} if {@code n} is even
     * @see #isEven(int)
     * @since 1.0.0
     */
    public static boolean even(int n) { return isEven(n); }

    /**
     * Alias of {@link #isOdd(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return {@code true} if {@code n} is odd
     * @see #isOdd(int)
     * @since 1.0.0
     */
    public static boolean odd(int n) { return isOdd(n); }

    // ============================================================== number theory

    /**
     * Checks whether the number is a prime.
     *
     * <p>Trial division up to {@code √n} — fast enough for any practical
     * input. Returns {@code false} for n &lt; 2 and for 1 (which is not
     * prime by definition).</p>
     *
     * <pre>{@code
     * Numbers.isPrime(7);   // true
     * Numbers.isPrime(2);   // true  (the only even prime)
     * Numbers.isPrime(9);   // false
     * Numbers.isPrime(1);   // false
     * }</pre>
     *
     * @param n the number to test
     * @return {@code true} if prime
     * @see #prime(int)
     * @see #primesUpTo(int)
     * @see #nextPrime(int)
     * @since 1.0.0
     */
    public static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n < 4) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; (long) i * i <= n; i += 2) if (n % i == 0) return false;
        return true;
    }

    /**
     * Alias of {@link #isPrime(int)} — see that method for full
     * documentation and examples.
     *
     * @param n the number to test
     * @return {@code true} if prime
     * @see #isPrime(int)
     * @since 1.0.0
     */
    public static boolean prime(int n) { return isPrime(n); }

    /**
     * Returns all prime numbers from 2 through {@code n} inclusive.
     *
     * <pre>{@code
     * Numbers.primesUpTo(10);   // [2, 3, 5, 7]
     * Numbers.primesUpTo(1);    // []
     * }</pre>
     *
     * @param n the upper bound (inclusive)
     * @return the list of primes, in ascending order (never null)
     * @see #isPrime(int)
     * @see #nextPrime(int)
     * @since 1.0.0
     */
    public static List<Integer> primesUpTo(int n) {
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) if (isPrime(i)) primes.add(i);
        return primes;
    }

    /**
     * Alias of {@link #primesUpTo(int)} — see that method for full
     * documentation and examples.
     *
     * @param n the upper bound (inclusive)
     * @return the list of primes up to {@code n}
     * @see #primesUpTo(int)
     * @since 1.0.0
     */
    public static List<Integer> primes(int n) { return primesUpTo(n); }

    /**
     * Returns the smallest prime strictly greater than {@code n}.
     *
     * @param n the starting point (exclusive)
     * @return the next prime
     * @see #isPrime(int)
     * @see #primesUpTo(int)
     * @since 1.0.0
     */
    public static int nextPrime(int n) {
        int candidate = Math.max(2, n + 1);
        while (!isPrime(candidate)) candidate++;
        return candidate;
    }

    /**
     * Checks whether the number is a perfect square (1, 4, 9, 16, 25, …).
     *
     * @param n the number to test
     * @return {@code true} if {@code n >= 0} and {@code √n} is an integer
     * @see #isSquare(int)
     * @see #isPerfectCube(int)
     * @since 1.0.0
     */
    public static boolean isPerfectSquare(int n) {
        if (n < 0) return false;
        int root = (int) Math.sqrt(n);
        return root * root == n;
    }

    /**
     * Alias of {@link #isPerfectSquare(int)} — see that method for full
     * documentation.
     *
     * @param n the number to test
     * @return {@code true} if {@code n} is a perfect square
     * @see #isPerfectSquare(int)
     * @since 1.0.0
     */
    public static boolean isSquare(int n) { return isPerfectSquare(n); }

    /**
     * Checks whether the number is a perfect cube (1, 8, 27, 64, 125, …).
     *
     * @param n the number to test
     * @return {@code true} if {@code |n|} is a perfect cube
     * @see #isCube(int)
     * @see #isPerfectSquare(int)
     * @since 1.0.0
     */
    public static boolean isPerfectCube(int n) {
        int cube = (int) Math.round(Math.cbrt(Math.abs(n)));
        return cube * cube * cube == Math.abs(n);
    }

    /**
     * Alias of {@link #isPerfectCube(int)} — see that method for full
     * documentation.
     *
     * @param n the number to test
     * @return {@code true} if {@code |n|} is a perfect cube
     * @see #isPerfectCube(int)
     * @since 1.0.0
     */
    public static boolean isCube(int n) { return isPerfectCube(n); }

    /**
     * Checks whether the number is an Armstrong (narcissistic) number —
     * an n-digit number equal to the sum of its digits each raised to the
     * n-th power.
     *
     * <pre>{@code
     * Numbers.isArmstrong(153);   // true  (1³ + 5³ + 3³ = 153)
     * Numbers.isArmstrong(370);   // true
     * Numbers.isArmstrong(100);   // false
     * }</pre>
     *
     * @param n the number to test (must be non-negative)
     * @return {@code true} if {@code n} is an Armstrong number
     * @see #isNarcissistic(int)
     * @see #isPalindromeNumber(int)
     * @since 1.0.0
     */
    public static boolean isArmstrong(int n) {
        if (n < 0) return false;
        int original = n;
        int digits = countDigits(n);
        int sum = 0;
        while (n > 0) { sum += (int) Math.pow(n % 10, digits); n /= 10; }
        return sum == original;
    }

    /**
     * Alias of {@link #isArmstrong(int)} — see that method for full
     * documentation.
     *
     * @param n the number to test
     * @return {@code true} if {@code n} is an Armstrong number
     * @see #isArmstrong(int)
     * @since 1.0.0
     */
    public static boolean isNarcissistic(int n) { return isArmstrong(n); }

    /**
     * Checks whether the number reads the same forwards and backwards.
     *
     * <pre>{@code
     * Numbers.isPalindromeNumber(121);    // true
     * Numbers.isPalindromeNumber(12321);  // true
     * Numbers.isPalindromeNumber(-121);   // false (negatives not palindromes)
     * }</pre>
     *
     * @param n the number to test
     * @return {@code true} if {@code n} is a non-negative palindrome
     * @see #isNumericPalindrome(int)
     * @see #reverseDigits(int)
     * @since 1.0.0
     */
    public static boolean isPalindromeNumber(int n) {
        if (n < 0) return false;
        return n == reverseDigits(n);
    }

    /**
     * Alias of {@link #isPalindromeNumber(int)} — see that method for full
     * documentation.
     *
     * @param n the number to test
     * @return {@code true} if {@code n} is a palindrome
     * @see #isPalindromeNumber(int)
     * @since 1.0.0
     */
    public static boolean isNumericPalindrome(int n) { return isPalindromeNumber(n); }

    /**
     * Checks whether the number is a perfect number — equal to the sum of
     * its proper divisors (excluding itself).
     *
     * <pre>{@code
     * Numbers.isPerfect(6);     // true  (1 + 2 + 3 = 6)
     * Numbers.isPerfect(28);    // true  (1 + 2 + 4 + 7 + 14 = 28)
     * Numbers.isPerfect(100);   // false
     * }</pre>
     *
     * @param n the number to test
     * @return {@code true} if {@code n} is a perfect number
     * @see #divisors(int)
     * @see #sumDivisors(int)
     * @since 1.0.0
     */
    public static boolean isPerfect(int n) {
        if (n < 2) return false;
        int sum = 1;
        for (int i = 2; (long) i * i <= n; i++) {
            if (n % i == 0) {
                sum += i;
                if (i * i != n) sum += n / i;
            }
        }
        return sum == n;
    }

    /**
     * Returns all positive divisors of the number, in ascending order.
     *
     * <pre>{@code
     * Numbers.divisors(12);   // [1, 2, 3, 4, 6, 12]
     * Numbers.divisors(0);    // []
     * Numbers.divisors(-5);   // []
     * }</pre>
     *
     * @param n the number to factor (must be positive)
     * @return the sorted list of divisors (never null)
     * @see #factors(int)
     * @see #countDivisors(int)
     * @see #sumDivisors(int)
     * @since 1.0.0
     */
    public static List<Integer> divisors(int n) {
        List<Integer> result = new ArrayList<>();
        if (n <= 0) return result;
        for (int i = 1; (long) i * i <= n; i++) {
            if (n % i == 0) {
                result.add(i);
                if (i * i != n) result.add(n / i);
            }
        }
        result.sort(null);
        return result;
    }

    /**
     * Alias of {@link #divisors(int)} — see that method for full
     * documentation.
     *
     * @param n the number to factor
     * @return the sorted list of divisors
     * @see #divisors(int)
     * @since 1.0.0
     */
    public static List<Integer> factors(int n) { return divisors(n); }

    /**
     * Returns the number of positive divisors of {@code n}.
     *
     * @param n the number to factor
     * @return the divisor count
     * @see #divisors(int)
     * @see #sumDivisors(int)
     * @since 1.0.0
     */
    public static int countDivisors(int n) { return divisors(n).size(); }

    /**
     * Returns the sum of all positive divisors of {@code n}, including
     * {@code n} itself.
     *
     * @param n the number to factor
     * @return the sum of divisors
     * @see #divisors(int)
     * @see #countDivisors(int)
     * @since 1.0.0
     */
    public static int sumDivisors(int n) {
        int sum = 0;
        for (int d : divisors(n)) sum += d;
        return sum;
    }

    /**
     * Returns {@code n!} — the factorial of {@code n}.
     *
     * <p>Returns {@code 0} for negative input, {@code 1} for
     * {@code n == 0}, and throws {@link IllegalArgumentException} for
     * {@code n > 20} because {@code 21!} overflows {@code long}.</p>
     *
     * @param n the number to factoralise
     * @return {@code n!}
     * @throws IllegalArgumentException if {@code n > 20}
     * @see #fact(int)
     * @see #fibonacci(int)
     * @since 1.0.0
     */
    public static long factorial(int n) {
        if (n < 0) return 0;
        if (n > 20) throw new IllegalArgumentException("Factorial overflow for n > 20");
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    /**
     * Alias of {@link #factorial(int)} — see that method for full
     * documentation.
     *
     * @param n the number to factoralise
     * @return {@code n!}
     * @see #factorial(int)
     * @since 1.0.0
     */
    public static long fact(int n) { return factorial(n); }

    /**
     * Returns the {@code n}-th Fibonacci number (0-indexed).
     *
     * <p>{@code F(0) = 0}, {@code F(1) = 1}, {@code F(n) = F(n-1) + F(n-2)}.
     * Uses iterative computation — {@code O(n)} time, {@code O(1)} space.</p>
     *
     * <pre>{@code
     * Numbers.fibonacci(0);    // 0
     * Numbers.fibonacci(1);    // 1
     * Numbers.fibonacci(10);   // 55
     * }</pre>
     *
     * @param n the index (negative returns 0)
     * @return the n-th Fibonacci number
     * @see #fib(int)
     * @see #factorial(int)
     * @since 1.0.0
     */
    public static int fibonacci(int n) {
        if (n < 0) return 0;
        if (n < 2) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) { int t = a + b; a = b; b = t; }
        return b;
    }

    /**
     * Alias of {@link #fibonacci(int)} — see that method for full
     * documentation.
     *
     * @param n the index
     * @return the n-th Fibonacci number
     * @see #fibonacci(int)
     * @since 1.0.0
     */
    public static int fib(int n) { return fibonacci(n); }

    /**
     * Returns the greatest common divisor of {@code a} and {@code b} —
     * the largest positive integer that divides both.
     *
     * <pre>{@code
     * Numbers.gcd(48, 18);   // 6
     * Numbers.gcd(-10, 15);  // 5  (uses absolute values)
     * Numbers.gcd(0, 5);     // 5
     * }</pre>
     *
     * @param a the first number
     * @param b the second number
     * @return the GCD (non-negative)
     * @see #hcf(int, int)
     * @see #lcm(int, int)
     * @since 1.0.0
     */
    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) { int t = b; b = a % b; a = t; }
        return a;
    }

    /**
     * Alias of {@link #gcd(int, int)} — see that method for full
     * documentation.
     *
     * @param a the first number
     * @param b the second number
     * @return the GCD
     * @see #gcd(int, int)
     * @since 1.0.0
     */
    public static int hcf(int a, int b) { return gcd(a, b); }

    /**
     * Returns the least common multiple of {@code a} and {@code b}.
     *
     * @param a the first number
     * @param b the second number
     * @return the LCM, or 0 if either argument is 0
     * @see #gcd(int, int)
     * @since 1.0.0
     */
    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a / gcd(a, b) * b);
    }

    // ============================================================== digits

    /**
     * Returns the sum of the digits of {@code |n|}.
     *
     * @param n the number
     * @return the digit sum (0 for {@code n == 0})
     * @see #digitSum(int)
     * @see #productDigits(int)
     * @since 1.0.0
     */
    public static int sumDigits(int n) {
        n = Math.abs(n);
        int sum = 0;
        while (n > 0) { sum += n % 10; n /= 10; }
        return sum;
    }

    /**
     * Alias of {@link #sumDigits(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the digit sum
     * @see #sumDigits(int)
     * @since 1.0.0
     */
    public static int digitSum(int n) { return sumDigits(n); }

    /**
     * Returns the product of the digits of {@code |n|}.
     *
     * @param n the number
     * @return the digit product (0 for {@code n == 0})
     * @see #digitProduct(int)
     * @see #sumDigits(int)
     * @since 1.0.0
     */
    public static int productDigits(int n) {
        n = Math.abs(n);
        if (n == 0) return 0;
        int product = 1;
        while (n > 0) { product *= n % 10; n /= 10; }
        return product;
    }

    /**
     * Alias of {@link #productDigits(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the digit product
     * @see #productDigits(int)
     * @since 1.0.0
     */
    public static int digitProduct(int n) { return productDigits(n); }

    /**
     * Returns the number of digits in {@code |n|}.
     *
     * @param n the number
     * @return the digit count (1 for {@code n == 0})
     * @see #numDigits(int)
     * @see #digitCount(int)
     * @since 1.0.0
     */
    public static int countDigits(int n) {
        n = Math.abs(n);
        if (n == 0) return 1;
        int count = 0;
        while (n > 0) { count++; n /= 10; }
        return count;
    }

    /**
     * Alias of {@link #countDigits(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the digit count
     * @see #countDigits(int)
     * @since 1.0.0
     */
    public static int numDigits(int n) { return countDigits(n); }

    /**
     * Alias of {@link #countDigits(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the digit count
     * @see #countDigits(int)
     * @since 1.0.0
     */
    public static int digitCount(int n) { return countDigits(n); }

    /**
     * Reverses the digits of the number.
     *
     * <pre>{@code
     * Numbers.reverseDigits(123);    // 321
     * Numbers.reverseDigits(-123);   // -321
     * Numbers.reverseDigits(120);    // 21
     * }</pre>
     *
     * @param n the number
     * @return the digit-reversed number
     * @see #reverseNumber(int)
     * @see #isPalindromeNumber(int)
     * @since 1.0.0
     */
    public static int reverseDigits(int n) {
        boolean negative = n < 0;
        n = Math.abs(n);
        int result = 0;
        while (n > 0) { result = result * 10 + n % 10; n /= 10; }
        return negative ? -result : result;
    }

    /**
     * Alias of {@link #reverseDigits(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the digit-reversed number
     * @see #reverseDigits(int)
     * @since 1.0.0
     */
    public static int reverseNumber(int n) { return reverseDigits(n); }

    /**
     * Returns the first (most significant) digit of {@code |n|}.
     *
     * @param n the number
     * @return the first digit
     * @see #lastDigit(int)
     * @since 1.0.0
     */
    public static int firstDigit(int n) {
        n = Math.abs(n);
        while (n >= 10) n /= 10;
        return n;
    }

    /**
     * Returns the last (least significant) digit of {@code |n|}.
     *
     * @param n the number
     * @return the last digit
     * @see #firstDigit(int)
     * @since 1.0.0
     */
    public static int lastDigit(int n) { return Math.abs(n) % 10; }

    // ============================================================== bases

    /**
     * Returns the binary (base-2) representation of {@code n}.
     *
     * @param n the number
     * @return the binary string (e.g. {@code "1111"} for 15)
     * @see #binary(int)
     * @see #fromBinary(String)
     * @since 1.0.0
     */
    public static String toBinary(int n) { return Integer.toBinaryString(n); }

    /**
     * Alias of {@link #toBinary(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the binary string
     * @see #toBinary(int)
     * @since 1.0.0
     */
    public static String binary(int n) { return toBinary(n); }

    /**
     * Returns the hexadecimal (base-16) representation of {@code n}.
     *
     * @param n the number
     * @return the lowercase hex string (e.g. {@code "f"} for 15)
     * @see #hex(int)
     * @see #fromHex(String)
     * @since 1.0.0
     */
    public static String toHex(int n) { return Integer.toHexString(n); }

    /**
     * Alias of {@link #toHex(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the hex string
     * @see #toHex(int)
     * @since 1.0.0
     */
    public static String hex(int n) { return toHex(n); }

    /**
     * Returns the octal (base-8) representation of {@code n}.
     *
     * @param n the number
     * @return the octal string
     * @see #octal(int)
     * @see #fromOctal(String)
     * @since 1.0.0
     */
    public static String toOctal(int n) { return Integer.toOctalString(n); }

    /**
     * Alias of {@link #toOctal(int)} — see that method for full
     * documentation.
     *
     * @param n the number
     * @return the octal string
     * @see #toOctal(int)
     * @since 1.0.0
     */
    public static String octal(int n) { return toOctal(n); }

    /**
     * Returns the representation of {@code n} in an arbitrary base 2–36.
     * Invalid bases fall back to base 10.
     *
     * @param n the number
     * @param base the target base (2–36)
     * @return the string representation
     * @see #fromBase(String, int)
     * @since 1.0.0
     */
    public static String toBase(int n, int base) {
        if (base < 2 || base > 36) return Integer.toString(n);
        return Integer.toString(n, base);
    }

    /**
     * Parses a binary string, returning {@code null} on failure.
     *
     * @param s the binary string (may be null)
     * @return the parsed integer, or {@code null} if unparseable
     * @see #toBinary(int)
     * @see #fromBase(String, int)
     * @since 1.0.0
     */
    public static Integer fromBinary(String s) {
        try {
            return Integer.parseInt(s, 2);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a hex string, returning {@code null} on failure.
     *
     * @param s the hex string (may be null)
     * @return the parsed integer, or {@code null} if unparseable
     * @see #toHex(int)
     * @see #fromBase(String, int)
     * @since 1.0.0
     */
    public static Integer fromHex(String s) {
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an octal string, returning {@code null} on failure.
     *
     * @param s the octal string (may be null)
     * @return the parsed integer, or {@code null} if unparseable
     * @see #toOctal(int)
     * @see #fromBase(String, int)
     * @since 1.0.0
     */
    public static Integer fromOctal(String s) {
        try {
            return Integer.parseInt(s, 8);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string in an arbitrary base 2–36, returning {@code null} on
     * failure or invalid base.
     *
     * @param s the string (may be null)
     * @param base the source base (2–36)
     * @return the parsed integer, or {@code null}
     * @see #toBase(int, int)
     * @since 1.0.0
     */
    public static Integer fromBase(String s, int base) {
        if (s == null || base < 2 || base > 36) return null;
        try {
            return Integer.parseInt(s, base);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============================================================== parsing

    /**
     * Parses an integer from the string, returning {@code null} on failure.
     *
     * <p>The string is trimmed before parsing. Rejects values that would
     * overflow {@code int}.</p>
     *
     * <pre>{@code
     * Numbers.parseInt("42");      // 42
     * Numbers.parseInt(" 42 ");    // 42
     * Numbers.parseInt("abc");     // null
     * Numbers.parseInt(null);      // null
     * }</pre>
     *
     * @param s the string to parse (may be null)
     * @return the parsed integer, or {@code null} if unparseable
     * @see #toInt(String)
     * @see #parseInt(String, Integer)
     * @see #parseIntOr(String, int)
     * @see #parseDouble(String)
     * @since 1.0.0
     */
    public static Integer parseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Alias of {@link #parseInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code null}
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Integer toInt(String s) { return parseInt(s); }

    /**
     * Alias of {@link #parseInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code null}
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Integer asInt(String s) { return parseInt(s); }

    /**
     * Alias of {@link #parseInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code null}
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Integer intOrNull(String s) { return parseInt(s); }

    /**
     * Alias of {@link #parseInt(String)} — see that method for full
     * documentation and examples.
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code null}
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Integer tryParseInt(String s) { return parseInt(s); }

    /**
     * Parses an integer, returning {@code defaultValue} on failure.
     *
     * @param s the string to parse (may be null)
     * @param defaultValue the value to return on parse failure (may be null)
     * @return the parsed integer, or {@code defaultValue}
     * @see #parseIntOr(String, int)
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Integer parseInt(String s, Integer defaultValue) {
        Integer result = parseInt(s);
        return result != null ? result : defaultValue;
    }

    /**
     * Parses an integer, returning {@code defaultValue} on failure (primitive
     * overload — never returns null).
     *
     * @param s the string to parse (may be null)
     * @param defaultValue the value to return on parse failure
     * @return the parsed integer, or {@code defaultValue}
     * @see #parseInt(String, Integer)
     * @since 1.0.0
     */
    public static int parseIntOr(String s, int defaultValue) {
        Integer result = parseInt(s);
        return result != null ? result : defaultValue;
    }

    /**
     * Parses a {@code double} from the string, returning {@code null} on
     * failure.
     *
     * <p>Accepts scientific notation, {@code "NaN"}, and
     * {@code "Infinity"} (matching {@link Double#parseDouble(String)}).</p>
     *
     * @param s the string to parse (may be null)
     * @return the parsed double, or {@code null}
     * @see #toDouble(String)
     * @see #parseDouble(String, Double)
     * @see #parseDoubleOr(String, double)
     * @see #parseInt(String)
     * @since 1.0.0
     */
    public static Double parseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Alias of {@link #parseDouble(String)} — see that method for full
     * documentation.
     *
     * @param s the string to parse
     * @return the parsed double, or {@code null}
     * @see #parseDouble(String)
     * @since 1.0.0
     */
    public static Double toDouble(String s) { return parseDouble(s); }

    /**
     * Alias of {@link #parseDouble(String)} — see that method for full
     * documentation.
     *
     * @param s the string to parse
     * @return the parsed double, or {@code null}
     * @see #parseDouble(String)
     * @since 1.0.0
     */
    public static Double asDouble(String s) { return parseDouble(s); }

    /**
     * Alias of {@link #parseDouble(String)} — see that method for full
     * documentation.
     *
     * @param s the string to parse
     * @return the parsed double, or {@code null}
     * @see #parseDouble(String)
     * @since 1.0.0
     */
    public static Double doubleOrNull(String s) { return parseDouble(s); }

    /**
     * Parses a {@code double}, returning {@code defaultValue} on failure.
     *
     * @param s the string to parse (may be null)
     * @param defaultValue the value to return on parse failure (may be null)
     * @return the parsed double, or {@code defaultValue}
     * @see #parseDouble(String)
     * @since 1.0.0
     */
    public static Double parseDouble(String s, Double defaultValue) {
        Double result = parseDouble(s);
        return result != null ? result : defaultValue;
    }

    /**
     * Parses a {@code double}, returning {@code defaultValue} on failure
     * (primitive overload — never returns null).
     *
     * @param s the string to parse (may be null)
     * @param defaultValue the value to return on parse failure
     * @return the parsed double, or {@code defaultValue}
     * @see #parseDouble(String, Double)
     * @since 1.0.0
     */
    public static double parseDoubleOr(String s, double defaultValue) {
        Double result = parseDouble(s);
        return result != null ? result : defaultValue;
    }

    /**
     * Parses a {@code long} from the string, returning {@code null} on
     * failure.
     *
     * @param s the string to parse (may be null)
     * @return the parsed long, or {@code null}
     * @see #toLong(String)
     * @see #asLong(String)
     * @since 1.0.0
     */
    public static Long parseLong(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Alias of {@link #parseLong(String)} — see that method for full
     * documentation.
     *
     * @param s the string to parse
     * @return the parsed long, or {@code null}
     * @see #parseLong(String)
     * @since 1.0.0
     */
    public static Long toLong(String s) { return parseLong(s); }

    /**
     * Alias of {@link #parseLong(String)} — see that method for full
     * documentation.
     *
     * @param s the string to parse
     * @return the parsed long, or {@code null}
     * @see #parseLong(String)
     * @since 1.0.0
     */
    public static Long asLong(String s) { return parseLong(s); }

    // ============================================================== rounding / format

    /**
     * Rounds the value to the given number of decimal places.
     *
     * <pre>{@code
     * Numbers.round(3.14159, 2);   // 3.14
     * Numbers.round(3.14159, 4);   // 3.1416
     * Numbers.round(3.14159, -1);  // 3.14159  (negative → unchanged)
     * }</pre>
     *
     * @param value the value to round
     * @param decimals the number of decimal places (negative → unchanged)
     * @return the rounded value
     * @see #rounded(double, int)
     * @see #format(double, int)
     * @since 1.0.0
     */
    public static double round(double value, int decimals) {
        if (decimals < 0) return value;
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    /**
     * Alias of {@link #round(double, int)} — see that method for full
     * documentation.
     *
     * @param value the value to round
     * @param decimals the number of decimal places
     * @return the rounded value
     * @see #round(double, int)
     * @since 1.0.0
     */
    public static double rounded(double value, int decimals) { return round(value, decimals); }

    /**
     * Formats the value with the given number of decimal places — always
     * uses the decimal point, never a locale-specific separator.
     *
     * <pre>{@code
     * Numbers.format(3.14159, 2);   // "3.14"
     * Numbers.format(3.0, 2);       // "3.00"
     * }</pre>
     *
     * @param value the value to format
     * @param decimals the number of decimal places
     * @return the formatted string
     * @see #fixed(double, int)
     * @see #format(double, Object...)
     * @since 1.0.0
     */
    public static String format(double value, int decimals) {
        return String.format(Locale.ENGLISH, "%." + decimals + "f", value);
    }

    /**
     * Alias of {@link #format(double, int)} — see that method for full
     * documentation.
     *
     * @param value the value to format
     * @param decimals the number of decimal places
     * @return the formatted string
     * @see #format(double, int)
     * @since 1.0.0
     */
    public static String fixed(double value, int decimals) { return format(value, decimals); }

    /**
     * Formats the value with key-value options — allows thousands
     * grouping and custom locale.
     *
     * <pre>{@code
     * Numbers.format(3.14159, Numbers.Key.DECIMALS, 2);
     * // "3.14"
     *
     * Numbers.format(1234.5,
     *     Numbers.Key.DECIMALS, 2, Numbers.Key.THOUSANDS, true);
     * // "1,234.50"
     * }</pre>
     *
     * <p>Null keys and wrong value types are ignored — the defaults
     * ({@code decimals=2}, {@code thousands=false},
     * {@code locale=ENGLISH}) apply.</p>
     *
     * @param value the value to format
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return the formatted string
     * @see #format(double, int)
     * @since 1.0.0
     */
    public static String format(double value, Object... keyValues) {
        int decimals = 2;
        boolean thousands = false;
        Locale locale = Locale.ENGLISH;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (!(keyValues[i] instanceof Key k)) continue;
                Object v = keyValues[i + 1];
                switch (k) {
                    case DECIMALS -> {
                        if (v instanceof Integer n && n >= 0) decimals = n;
                    }
                    case THOUSANDS -> {
                        if (v instanceof Boolean b) thousands = b;
                    }
                    case LOCALE -> {
                        if (v instanceof Locale l) locale = l;
                    }
                    default -> {
                        /* skip */ }
                }
            }
        }
        String pattern = (thousands ? "%,." : "%.") + decimals + "f";
        return String.format(locale, pattern, value);
    }

    /**
     * Returns the ordinal string for {@code n} — {@code "1st"},
     * {@code "2nd"}, {@code "3rd"}, {@code "4th"}, etc.
     *
     * <p>Handles the 11th/12th/13th exception correctly — and also the
     * 111th/112th/113th cases via the last-two-digits rule. Negatives
     * keep their sign.</p>
     *
     * <pre>{@code
     * Numbers.ordinal(1);    // "1st"
     * Numbers.ordinal(2);    // "2nd"
     * Numbers.ordinal(11);   // "11th"
     * Numbers.ordinal(21);   // "21st"
     * Numbers.ordinal(-1);   // "-1st"
     * }</pre>
     *
     * @param n the number
     * @return the ordinal string
     * @see Format#ordinal(int)
     * @since 1.0.0
     */
    public static String ordinal(int n) {
        int lastTwo = Math.abs(n % 100);
        if (lastTwo >= 11 && lastTwo <= 13) return n + "th";
        return switch (lastTwo % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    /**
     * Returns the abbreviated form — {@code "1.5K"}, {@code "2.3M"},
     * {@code "4.1B"}, {@code "5.2T"}.
     *
     * <pre>{@code
     * Numbers.abbreviate(999);         // "999"
     * Numbers.abbreviate(1_500);       // "1.5K"
     * Numbers.abbreviate(1_500_000);   // "1.5M"
     * Numbers.abbreviate(-1_500);      // "-1.5K"
     * }</pre>
     *
     * @param n the number to abbreviate
     * @return the abbreviated string
     * @see #compact(long)
     * @see #fileSize(long)
     * @since 1.0.0
     */
    public static String abbreviate(long n) {
        long abs = Math.abs(n);
        String sign = n < 0 ? "-" : "";
        if (abs < 1_000L) return sign + abs;
        if (abs < 1_000_000L) return sign + trim(abs / 1_000.0) + "K";
        if (abs < 1_000_000_000L) return sign + trim(abs / 1_000_000.0) + "M";
        if (abs < 1_000_000_000_000L) return sign + trim(abs / 1_000_000_000.0) + "B";
        return sign + trim(abs / 1_000_000_000_000.0) + "T";
    }

    /**
     * Alias of {@link #abbreviate(long)} — see that method for full
     * documentation and examples.
     *
     * @param n the number to abbreviate
     * @return the abbreviated string
     * @see #abbreviate(long)
     * @since 1.0.0
     */
    public static String compact(long n) { return abbreviate(n); }

    /**
     * Alias of {@link #abbreviate(long)} — see that method for full
     * documentation and examples.
     *
     * @param n the number to abbreviate
     * @return the abbreviated string
     * @see #abbreviate(long)
     * @since 1.0.0
     */
    public static String humanShort(long n) { return abbreviate(n); }

    /**
     * Returns the human-readable file size — {@code "512 B"},
     * {@code "1.0 KB"}, {@code "2.5 GB"}, etc. Uses 1024-based units
     * (KiB, MiB, …) with {@code "KB"}, {@code "MB"} labels.
     *
     * @param bytes the number of bytes (negative → {@code "0 B"})
     * @return the human-readable size
     * @see #humanSize(long)
     * @see #bytes(long)
     * @since 1.0.0
     */
    public static String fileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024L) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        double value = bytes;
        int unit = -1;
        while (value >= 1024.0 && unit < units.length - 1) { value /= 1024.0; unit++; }
        return String.format(Locale.ENGLISH, "%.1f %s", value, units[unit]);
    }

    /**
     * Alias of {@link #fileSize(long)} — see that method for full
     * documentation.
     *
     * @param bytes the number of bytes
     * @return the human-readable size
     * @see #fileSize(long)
     * @since 1.0.0
     */
    public static String humanSize(long bytes) { return fileSize(bytes); }

    /**
     * Alias of {@link #fileSize(long)} — see that method for full
     * documentation.
     *
     * @param bytes the number of bytes
     * @return the human-readable size
     * @see #fileSize(long)
     * @since 1.0.0
     */
    public static String bytes(long bytes) { return fileSize(bytes); }

    /**
     * Returns the number formatted with thousands separators using the
     * English locale — {@code "1,234,567"}.
     *
     * @param n the number to format
     * @return the grouped string
     * @see #withCommas(long)
     * @see #grouped(long)
     * @since 1.0.0
     */
    public static String thousands(long n) {
        return String.format(Locale.ENGLISH, "%,d", n);
    }

    /**
     * Alias of {@link #thousands(long)} — see that method for full
     * documentation.
     *
     * @param n the number to format
     * @return the grouped string
     * @see #thousands(long)
     * @since 1.0.0
     */
    public static String withCommas(long n) { return thousands(n); }

    /**
     * Alias of {@link #thousands(long)} — see that method for full
     * documentation.
     *
     * @param n the number to format
     * @return the grouped string
     * @see #thousands(long)
     * @since 1.0.0
     */
    public static String grouped(long n) { return thousands(n); }

    /**
     * Returns the Roman numeral for the integer.
     *
     * <p>Supports 1–3999. Input outside that range is returned as a
     * decimal string — this keeps the method total (never throws) at the
     * cost of {@code "4000"} not converting.</p>
     *
     * <pre>{@code
     * Numbers.roman(1);       // "I"
     * Numbers.roman(4);       // "IV"
     * Numbers.roman(1994);    // "MCMXCIV"
     * Numbers.roman(4000);    // "4000"
     * }</pre>
     *
     * @param n the number to convert
     * @return the Roman numeral, or the decimal string for out-of-range input
     * @see #toRoman(int)
     * @since 1.0.0
     */
    public static String roman(int n) {
        if (n <= 0 || n > 3999) return Integer.toString(n);
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[n / 1000] + hundreds[(n % 1000) / 100]
                + tens[(n % 100) / 10] + ones[n % 10];
    }

    /**
     * Alias of {@link #roman(int)} — see that method for full
     * documentation and examples.
     *
     * @param n the number to convert
     * @return the Roman numeral
     * @see #roman(int)
     * @since 1.0.0
     */
    public static String toRoman(int n) { return roman(n); }

    /**
     * Truncates a double to at most one decimal place for display inside
     * {@link #abbreviate(long)}. Package-private helper — not part of
     * public API.
     *
     * @param value the value to format
     * @return the trimmed string
     */
    private static String trim(double value) {
        if (value == Math.rint(value)) return Integer.toString((int) value);
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    // ============================================================== ranges

    /**
     * Returns a list of consecutive integers from {@code start} (inclusive)
     * to {@code end} (exclusive), step 1.
     *
     * <pre>{@code
     * Numbers.range(1, 5);    // [1, 2, 3, 4]
     * Numbers.range(5, 1);    // []
     * }</pre>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the list of values, empty if {@code start >= end}
     * @see #to(int, int)
     * @see #range(int, int, int)
     * @see #rangeInclusive(int, int)
     * @since 1.0.0
     */
    public static List<Integer> range(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) result.add(i);
        return result;
    }

    /**
     * Alias of {@link #range(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the list of values
     * @see #range(int, int)
     * @since 1.0.0
     */
    public static List<Integer> to(int start, int end) { return range(start, end); }

    /**
     * Alias of {@link #range(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the list of values
     * @see #range(int, int)
     * @since 1.0.0
     */
    public static List<Integer> span(int start, int end) { return range(start, end); }

    /**
     * Alias of {@link #range(int, int)} — see that method for full
     * documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the list of values
     * @see #range(int, int)
     * @since 1.0.0
     */
    public static List<Integer> upto(int start, int end) { return range(start, end); }

    /**
     * Returns a list of integers with a custom step size.
     *
     * <p>Negative steps are supported — {@code range(10, 0, -2)} returns
     * {@code [10, 8, 6, 4, 2]}. Step {@code 0} returns an empty list.</p>
     *
     * <pre>{@code
     * Numbers.range(0, 10, 2);    // [0, 2, 4, 6, 8]
     * Numbers.range(10, 0, -3);   // [10, 7, 4, 1]
     * Numbers.range(0, 10, 0);    // []
     * }</pre>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @param step the increment (must be non-zero)
     * @return the list of values
     * @see #range(int, int)
     * @since 1.0.0
     */
    public static List<Integer> range(int start, int end, int step) {
        List<Integer> result = new ArrayList<>();
        if (step == 0) return result;
        if (step > 0) {
            for (int i = start; i < end; i += step) result.add(i);
        } else {
            for (int i = start; i > end; i += step) result.add(i);
        }
        return result;
    }

    /**
     * Key-value overload of {@link #range(int, int, int)} — accepts
     * {@code Numbers.Key.STEP}.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return the list of values
     * @see #range(int, int, int)
     * @since 1.0.0
     */
    public static List<Integer> range(int start, int end, Object... keyValues) {
        int step = 1;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.STEP && keyValues[i + 1] instanceof Integer n) {
                    step = n;
                }
            }
        }
        return range(start, end, step);
    }

    /**
     * Returns a list of integers from {@code start} to {@code end},
     * <b>inclusive on both ends</b>.
     *
     * <pre>{@code
     * Numbers.rangeInclusive(1, 5);   // [1, 2, 3, 4, 5]
     * }</pre>
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (inclusive)
     * @return the list of values, empty if {@code start > end}
     * @see #rangeClosed(int, int)
     * @see #range(int, int)
     * @since 1.0.0
     */
    public static List<Integer> rangeInclusive(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i++) result.add(i);
        return result;
    }

    /**
     * Alias of {@link #rangeInclusive(int, int)} — see that method for
     * full documentation and examples.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (inclusive)
     * @return the list of values
     * @see #rangeInclusive(int, int)
     * @since 1.0.0
     */
    public static List<Integer> rangeClosed(int start, int end) { return rangeInclusive(start, end); }

    /**
     * Returns an {@code int[]} of consecutive integers from {@code start}
     * (inclusive) to {@code end} (exclusive).
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @return the array of values (never null)
     * @see #range(int, int)
     * @see #rangeArray(int, int, int)
     * @since 1.0.0
     */
    public static int[] rangeArray(int start, int end) {
        return range(start, end).stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Returns an {@code int[]} of consecutive integers from {@code start}
     * (inclusive) to {@code end} (exclusive), with custom step.
     *
     * @param start the starting value (inclusive)
     * @param end the ending value (exclusive)
     * @param step the increment
     * @return the array of values (never null)
     * @see #range(int, int, int)
     * @see #rangeArray(int, int)
     * @since 1.0.0
     */
    public static int[] rangeArray(int start, int end, int step) {
        return range(start, end, step).stream().mapToInt(Integer::intValue).toArray();
    }

    // ============================================================== aggregation (arrays)

    /**
     * Returns the sum of the array. Null or empty arrays return {@code 0}.
     *
     * @param values the array to sum (may be null)
     * @return the sum (0 for null or empty)
     * @see #total(int[])
     * @see #sumOf(int[])
     * @see #average(int[])
     * @since 1.0.0
     */
    public static int sum(int[] values) {
        if (values == null) return 0;
        int total = 0;
        for (int v : values) total += v;
        return total;
    }

    /**
     * Alias of {@link #sum(int[])} — see that method for full
     * documentation.
     *
     * @param values the array to sum
     * @return the sum
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static int total(int[] values) { return sum(values); }

    /**
     * Alias of {@link #sum(int[])} — see that method for full
     * documentation.
     *
     * @param values the array to sum
     * @return the sum
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static int sumOf(int[] values) { return sum(values); }

    /**
     * Returns the sum of a subrange of the array.
     *
     * @param values the array (may be null)
     * @param from the starting index (inclusive, clamped to 0)
     * @param to the ending index (exclusive, clamped to length)
     * @return the sum of the subrange
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static int sum(int[] values, int from, int to) {
        if (values == null) return 0;
        int lo = Math.max(0, from), hi = Math.min(values.length, to);
        int total = 0;
        for (int i = lo; i < hi; i++) total += values[i];
        return total;
    }

    /**
     * Returns the sum of the double array.
     *
     * @param values the array to sum (may be null)
     * @return the sum (0 for null or empty)
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static double sum(double[] values) {
        if (values == null) return 0;
        double total = 0;
        for (double v : values) total += v;
        return total;
    }

    /**
     * Returns the arithmetic mean of the array.
     *
     * @param values the array (may be null)
     * @return the mean, or 0 for null or empty arrays
     * @see #mean(int[])
     * @see #avg(int[])
     * @see #sum(int[])
     * @since 1.0.0
     */
    public static double average(int[] values) {
        if (values == null || values.length == 0) return 0;
        return (double) sum(values) / values.length;
    }

    /**
     * Alias of {@link #average(int[])} — see that method for full
     * documentation.
     *
     * @param values the array
     * @return the mean
     * @see #average(int[])
     * @since 1.0.0
     */
    public static double mean(int[] values) { return average(values); }

    /**
     * Alias of {@link #average(int[])} — see that method for full
     * documentation.
     *
     * @param values the array
     * @return the mean
     * @see #average(int[])
     * @since 1.0.0
     */
    public static double avg(int[] values) { return average(values); }

    /**
     * Alias of {@link #average(int[])} — see that method for full
     * documentation.
     *
     * @param values the array
     * @return the mean
     * @see #average(int[])
     * @since 1.0.0
     */
    public static double averageOf(int[] values) { return average(values); }

    /**
     * Returns the maximum value in the array.
     *
     * @param values the array (may be null)
     * @return the maximum, or 0 for null or empty arrays
     * @see #maxOf(int[])
     * @see #min(int[])
     * @since 1.0.0
     */
    public static int max(int[] values) {
        if (values == null || values.length == 0) return 0;
        int best = values[0];
        for (int v : values) if (v > best) best = v;
        return best;
    }

    /**
     * Returns the minimum value in the array.
     *
     * @param values the array (may be null)
     * @return the minimum, or 0 for null or empty arrays
     * @see #minOf(int[])
     * @see #max(int[])
     * @since 1.0.0
     */
    public static int min(int[] values) {
        if (values == null || values.length == 0) return 0;
        int best = values[0];
        for (int v : values) if (v < best) best = v;
        return best;
    }

    /**
     * Alias of {@link #max(int[])} — see that method for full
     * documentation.
     *
     * @param values the array
     * @return the maximum
     * @see #max(int[])
     * @since 1.0.0
     */
    public static int maxOf(int[] values) { return max(values); }

    /**
     * Alias of {@link #min(int[])} — see that method for full
     * documentation.
     *
     * @param values the array
     * @return the minimum
     * @see #min(int[])
     * @since 1.0.0
     */
    public static int minOf(int[] values) { return min(values); }

    /**
     * Counts occurrences of the target value in the array.
     *
     * @param values the array (may be null)
     * @param target the value to count
     * @return the count (0 for null array)
     * @see #countOf(int[], int)
     * @since 1.0.0
     */
    public static int count(int[] values, int target) {
        if (values == null) return 0;
        int n = 0;
        for (int v : values) if (v == target) n++;
        return n;
    }

    /**
     * Alias of {@link #count(int[], int)} — see that method for full
     * documentation.
     *
     * @param values the array
     * @param target the value to count
     * @return the count
     * @see #count(int[], int)
     * @since 1.0.0
     */
    public static int countOf(int[] values, int target) { return count(values, target); }

    /**
     * Checks whether the array contains the target value.
     *
     * @param values the array (may be null)
     * @param target the value to find
     * @return {@code true} if the value is present
     * @see #has(int[], int)
     * @see chirp.collection.ArraysUtil#contains(int[], int)
     * @since 1.0.0
     */
    public static boolean contains(int[] values, int target) {
        if (values == null) return false;
        for (int v : values) if (v == target) return true;
        return false;
    }

    /**
     * Alias of {@link #contains(int[], int)} — see that method for full
     * documentation.
     *
     * @param values the array
     * @param target the value to find
     * @return {@code true} if present
     * @see #contains(int[], int)
     * @since 1.0.0
     */
    public static boolean has(int[] values, int target) { return contains(values, target); }

    // ============================================================== statistics

    /**
     * Summary statistics for an array of ints.
     *
     * <p>All fields are computed in a single pass when possible. The
     * {@code stdDev} field uses the sample variance when constructed via
     * {@link #statistics(int[], Object...)} with {@code Key.SAMPLE=true},
     * and the population variance otherwise.</p>
     *
     * <p>The median is the middle value for odd-length arrays, or the
     * average of the two middle values for even-length arrays. The mode
     * is the most-frequent value; ties resolve to the smaller value.</p>
     *
     * @param count the number of values
     * @param sum the sum
     * @param average the arithmetic mean
     * @param min the minimum value
     * @param max the maximum value
     * @param stdDev the standard deviation
     * @param median the median value
     * @param mode the most-frequent value
     * @see #statistics(int[])
     * @see #median(int[])
     * @see #stdDev(int[])
     * @since 1.0.0
     */
    public record Statistics(
            long count,
            long sum,
            double average,
            int min,
            int max,
            double stdDev,
            double median,
            int mode) {}

    /**
     * Computes population statistics (std-dev divides by n) for the array.
     *
     * @param values the array (may be null)
     * @return the statistics (all-zero for null or empty)
     * @see #statistics(int[], Object...)
     * @since 1.0.0
     */
    public static Statistics statistics(int[] values) {
        return statistics(values, (Object[]) null);
    }

    /**
     * Computes statistics with key-value options — pass {@code Key.SAMPLE,
     * true} to use sample variance (divides by n−1 instead of n).
     *
     * @param values the array (may be null)
     * @param keyValues alternating {@link Key} → value pairs (may be null)
     * @return the statistics
     * @see #statistics(int[])
     * @since 1.0.0
     */
    public static Statistics statistics(int[] values, Object... keyValues) {
        if (values == null || values.length == 0) {
            return new Statistics(0L, 0L, 0.0, 0, 0, 0.0, 0.0, 0);
        }

        boolean sample = false;
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                if (keyValues[i] == Key.SAMPLE && keyValues[i + 1] instanceof Boolean b) sample = b;
            }
        }

        Trace.trace("numbers", "statistics(n={}, sample={})", values.length, sample);

        long sum = 0L;
        int min = values[0], max = values[0];
        Map<Integer, Integer> freq = new HashMap<>(values.length * 2);

        for (int v : values) {
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
            freq.merge(v, 1, Integer::sum);
        }

        double avg = (double) sum / values.length;

        double sq = 0.0;
        for (int v : values) {
            double d = v - avg;
            sq += d * d;
        }
        int denominator = sample && values.length > 1 ? values.length - 1 : values.length;
        double stdDev = Math.sqrt(sq / denominator);

        int[] sorted = values.clone();
        Arrays.sort(sorted);
        double median = (sorted.length % 2 == 1)
                ? sorted[sorted.length / 2]
                : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;

        int mode = sorted[0], bestCount = 0;
        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            int v = e.getKey(), c = e.getValue();
            if (c > bestCount || (c == bestCount && v < mode)) { mode = v; bestCount = c; }
        }

        return new Statistics(values.length, sum, avg, min, max, stdDev, median, mode);
    }

    /**
     * Returns the median of the array — the middle value for odd-length
     * arrays, or the average of the two middle values for even-length
     * arrays.
     *
     * @param values the array (may be null)
     * @return the median, or 0 for null or empty arrays
     * @see #statistics(int[])
     * @see #mode(int[])
     * @since 1.0.0
     */
    public static double median(int[] values) {
        if (values == null || values.length == 0) return 0;
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        return (sorted.length % 2 == 1)
                ? sorted[sorted.length / 2]
                : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
    }

    /**
     * Returns the mode of the array — the most-frequent value. Ties
     * resolve to the smaller value.
     *
     * @param values the array (may be null)
     * @return the mode, or 0 for null or empty arrays
     * @see #statistics(int[])
     * @see #median(int[])
     * @since 1.0.0
     */
    public static int mode(int[] values) {
        return statistics(values).mode();
    }

    /**
     * Returns the population standard deviation of the array.
     *
     * @param values the array (may be null)
     * @return the standard deviation (0 for null or empty)
     * @see #stdDev(int[])
     * @see #sampleStdDev(int[])
     * @see #variance(int[])
     * @since 1.0.0
     */
    public static double standardDeviation(int[] values) {
        return statistics(values).stdDev();
    }

    /**
     * Alias of {@link #standardDeviation(int[])} — see that method for
     * full documentation.
     *
     * @param values the array
     * @return the standard deviation
     * @see #standardDeviation(int[])
     * @since 1.0.0
     */
    public static double stdDev(int[] values) { return standardDeviation(values); }

    /**
     * Returns the sample standard deviation (divides by n−1) — the
     * correct estimator when the array represents a sample of a larger
     * population.
     *
     * @param values the array (may be null)
     * @return the sample std-dev (0 for null or fewer than 2 elements)
     * @see #stdDev(int[])
     * @see #sampleVariance(int[])
     * @since 1.0.0
     */
    public static double sampleStdDev(int[] values) {
        return statistics(values, Key.SAMPLE, true).stdDev();
    }

    /**
     * Returns the population variance — the square of the population
     * standard deviation.
     *
     * @param values the array (may be null)
     * @return the variance
     * @see #stdDev(int[])
     * @see #sampleVariance(int[])
     * @since 1.0.0
     */
    public static double variance(int[] values) {
        double sd = standardDeviation(values);
        return sd * sd;
    }

    /**
     * Returns the sample variance — the square of the sample standard
     * deviation.
     *
     * @param values the array (may be null)
     * @return the sample variance
     * @see #sampleStdDev(int[])
     * @since 1.0.0
     */
    public static double sampleVariance(int[] values) {
        double sd = sampleStdDev(values);
        return sd * sd;
    }

    // ============================================================== percent

    /**
     * Returns {@code part} as a percentage of {@code whole} — the value
     * ranges 0–100, not 0–1.
     *
     * <pre>{@code
     * Numbers.percent(25, 100);   // 25.0
     * Numbers.percent(1, 8);      // 12.5
     * Numbers.percent(5, 0);      // 0.0  (safe division by zero)
     * }</pre>
     *
     * @param part the numerator
     * @param whole the denominator (may be 0 — returns 0)
     * @return the percentage in {@code [0, 100]}
     * @see #percentage(double, double)
     * @see #pct(double, double)
     * @see #applyPercent(double, double)
     * @see Format#percent(double)
     * @since 1.0.0
     */
    public static double percent(double part, double whole) {
        return whole == 0 ? 0 : part / whole * 100;
    }

    /**
     * Alias of {@link #percent(double, double)} — see that method for full
     * documentation and examples.
     *
     * @param part the numerator
     * @param whole the denominator
     * @return the percentage
     * @see #percent(double, double)
     * @since 1.0.0
     */
    public static double percentage(double part, double whole) { return percent(part, whole); }

    /**
     * Alias of {@link #percent(double, double)} — see that method for full
     * documentation and examples.
     *
     * @param part the numerator
     * @param whole the denominator
     * @return the percentage
     * @see #percent(double, double)
     * @since 1.0.0
     */
    public static double pct(double part, double whole) { return percent(part, whole); }

    /**
     * Applies a percentage to a value — the inverse of {@link #percent(double, double)}.
     *
     * <pre>{@code
     * Numbers.applyPercent(100, 25);   // 25.0  (25% of 100)
     * Numbers.applyPercent(200, 10.5); // 21.0
     * }</pre>
     *
     * @param value the base value
     * @param pct the percentage (0–100)
     * @return the result of applying {@code pct}% to {@code value}
     * @see #percent(double, double)
     * @since 1.0.0
     */
    public static double applyPercent(double value, double pct) {
        return value * pct / 100;
    }

    // ============================================================== chains

    /**
     * Wraps the int in a fluent {@link NumberChain} for arithmetic
     * chaining.
     *
     * <pre>{@code
     * int result = Numbers.of(10)
     *     .add(5)
     *     .times(2)
     *     .clamp(0, 20)
     *     .asInt();       // 20
     * }</pre>
     *
     * @param n the initial value
     * @return a new {@code NumberChain} wrapping {@code n}
     * @see NumberChain
     * @see #of(double)
     * @see #chain(int)
     * @since 1.0.0
     */
    public static NumberChain of(int n) { return NumberChain.of(n); }

    /**
     * Wraps the double in a fluent {@link NumberChain} for arithmetic
     * chaining.
     *
     * @param n the initial value
     * @return a new {@code NumberChain} wrapping {@code n}
     * @see NumberChain
     * @see #of(int)
     * @since 1.0.0
     */
    public static NumberChain of(double n) { return NumberChain.of(n); }

    /**
     * Alias of {@link #of(int)} — see that method for full documentation
     * and examples.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(int)
     * @since 1.0.0
     */
    public static NumberChain chain(int n) { return NumberChain.of(n); }

    /**
     * Alias of {@link #of(double)} — see that method for full
     * documentation and examples.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(double)
     * @since 1.0.0
     */
    public static NumberChain chain(double n) { return NumberChain.of(n); }

    /**
     * Alias of {@link #of(int)} — see that method for full documentation
     * and examples.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(int)
     * @since 1.0.0
     */
    public static NumberChain from(int n) { return NumberChain.of(n); }

    /**
     * Alias of {@link #of(double)} — see that method for full
     * documentation and examples.
     *
     * @param n the initial value
     * @return a new {@code NumberChain}
     * @see #of(double)
     * @since 1.0.0
     */
    public static NumberChain from(double n) { return NumberChain.of(n); }
}
