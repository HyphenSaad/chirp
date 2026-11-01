package chirp.number;

import chirp.format.Format;
import chirp.internal.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Numeric helpers — null-safe, never throws for user input.
 *
 * <h2>Naming</h2>
 * <p>Every public method has 2–5 aliases: {@code random} / {@code rand} /
 * {@code randomInt} / {@code randomBetween}; {@code parseInt} / {@code toInt}
 * / {@code asInt} / {@code intOrNull}; {@code sum} / {@code total} /
 * {@code sumOf}; {@code mean} / {@code average} / {@code avg}.</p>
 *
 * <h2>Key-value (IDE autocomplete)</h2>
 * <p>Methods that accept multiple options (statistics, formatting, ranges)
 * take a varargs of alternating {@link Key} → value pairs. Missing values and
 * wrong types are silently skipped; the method's defaults apply.</p>
 *
 * <pre>{@code
 * Numbers.statistics(values, Numbers.Key.SAMPLE, true);   // sample stddev
 * Numbers.format(3.14159, Numbers.Key.DECIMALS, 2);       // "3.14"
 * Numbers.range(0, 10, Numbers.Key.STEP, 2);              // [0, 2, 4, 6, 8]
 * }</pre>
 *
 * <h2>Fluent chains</h2>
 * <p>{@link #of(int)} and {@link #of(double)} open a {@link NumberChain} —
 * immutable, composable, and a live demonstration of the Decorator pattern.</p>
 *
 * <pre>{@code
 * int result = Numbers.of(10).add(5).times(2).clamp(0, 20).asInt(); // 20
 * }</pre>
 */
public final class Numbers {

    private static final Random RANDOM = new Random();

    private Numbers() {}

    /** Key enum — for key-value APIs in this module. */
    public enum Key {
        DECIMALS, THOUSANDS, LOCALE, SAMPLE, STEP, INCLUSIVE
    }

    // ============================================================== random

    public static int random(int min, int max) {
        if (min > max) return min;
        return RANDOM.nextInt(max - min + 1) + min;
    }
    public static int rand(int min, int max) { return random(min, max); }
    public static int randomInt(int min, int max) { return random(min, max); }
    public static int randomBetween(int min, int max) { return random(min, max); }
    public static int randomFrom(int min, int max) { return random(min, max); }

    public static double randomDouble(double min, double max) {
        if (min > max) return min;
        return min + (max - min) * RANDOM.nextDouble();
    }
    public static double randDouble(double min, double max) { return randomDouble(min, max); }
    public static double randomFloat(double min, double max) { return randomDouble(min, max); }

    public static boolean randomBoolean() { return RANDOM.nextBoolean(); }
    public static boolean randomBool() { return randomBoolean(); }
    public static boolean flipCoin() { return randomBoolean(); }

    /** Random element from an array; returns null when empty. */
    public static <T> T randomItem(T[] items) {
        if (items == null || items.length == 0) return null;
        return items[random(0, items.length - 1)];
    }
    public static <T> T randomItem(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(random(0, items.size() - 1));
    }
    public static <T> T pick(T[] items) { return randomItem(items); }
    public static <T> T pick(List<T> items) { return randomItem(items); }

    public static int[] randomArray(int size, int min, int max) {
        if (size <= 0) return new int[0];
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = random(min, max);
        return arr;
    }
    public static List<Integer> randomList(int size, int min, int max) {
        if (size <= 0) return List.of();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(random(min, max));
        return list;
    }

    // ============================================================== clamping

    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static int limit(int value, int min, int max) { return clamp(value, min, max); }
    public static int bound(int value, int min, int max) { return clamp(value, min, max); }
    public static int constrain(int value, int min, int max) { return clamp(value, min, max); }
    public static int wrap(int value, int min, int max) {
        if (max <= min) return min;
        int range = max - min + 1;
        int mod = ((value - min) % range + range) % range;
        return min + mod;
    }

    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    public static double limit(double value, double min, double max) { return clamp(value, min, max); }

    // ============================================================== basics

    public static int abs(int n) { return Math.abs(n); }
    public static long abs(long n) { return Math.abs(n); }
    public static double abs(double n) { return Math.abs(n); }

    public static int sign(int n) { return Integer.signum(n); }
    public static int signum(int n) { return sign(n); }
    public static int signOf(int n) { return sign(n); }

    /** Always-positive modulus — unlike {@code %}, returns a non-negative result. */
    public static int mod(int a, int b) {
        if (b == 0) return 0;
        int r = a % b;
        return r < 0 ? r + Math.abs(b) : r;
    }
    public static int modulo(int a, int b) { return mod(a, b); }

    public static double pow(double base, double exponent) { return Math.pow(base, exponent); }
    public static double power(double base, double exponent) { return pow(base, exponent); }
    public static int powInt(int base, int exponent) { return (int) Math.pow(base, exponent); }

    public static double sqrt(double n) { return n < 0 ? 0 : Math.sqrt(n); }
    public static double squareRoot(double n) { return sqrt(n); }
    public static double cbrt(double n) { return Math.cbrt(n); }
    public static double cubeRoot(double n) { return cbrt(n); }

    public static double hypot(double a, double b) { return Math.hypot(a, b); }
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    public static double log(double n) { return n <= 0 ? 0 : Math.log(n); }
    public static double ln(double n) { return log(n); }
    public static double log10(double n) { return n <= 0 ? 0 : Math.log10(n); }
    public static double log2(double n) { return n <= 0 ? 0 : Math.log(n) / Math.log(2); }
    public static double logBase(double n, double base) {
        if (n <= 0 || base <= 0 || base == 1) return 0;
        return Math.log(n) / Math.log(base);
    }
    public static double exp(double n) { return Math.exp(n); }

    public static int floor(double n) { return (int) Math.floor(n); }
    public static int ceil(double n) { return (int) Math.ceil(n); }
    public static int roundToInt(double n) { return (int) Math.round(n); }
    public static long roundToLong(double n) { return Math.round(n); }

    public static boolean isInteger(Object value) {
        if (value instanceof Integer || value instanceof Long
                || value instanceof Short || value instanceof Byte)
            return true;
        if (value instanceof Double d) return d == Math.rint(d) && !Double.isInfinite(d);
        if (value instanceof Float f) return f == Math.rint(f) && !Float.isInfinite(f);
        return false;
    }
    public static boolean isWhole(Object value) { return isInteger(value); }

    // ============================================================== trigonometry

    public static double sin(double radians) { return Math.sin(radians); }
    public static double cos(double radians) { return Math.cos(radians); }
    public static double tan(double radians) { return Math.tan(radians); }
    public static double asin(double value) { return Math.asin(value); }
    public static double acos(double value) { return Math.acos(value); }
    public static double atan(double value) { return Math.atan(value); }
    public static double atan2(double y, double x) { return Math.atan2(y, x); }
    public static double toRadians(double degrees) { return Math.toRadians(degrees); }
    public static double toDegrees(double radians) { return Math.toDegrees(radians); }

    // ============================================================== parity

    public static boolean isEven(int n) { return n % 2 == 0; }
    public static boolean isOdd(int n) { return n % 2 != 0; }
    public static boolean even(int n) { return isEven(n); }
    public static boolean odd(int n) { return isOdd(n); }

    // ============================================================== number theory

    public static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n < 4) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; (long) i * i <= n; i += 2) if (n % i == 0) return false;
        return true;
    }
    public static boolean prime(int n) { return isPrime(n); }

    public static List<Integer> primesUpTo(int n) {
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) if (isPrime(i)) primes.add(i);
        return primes;
    }
    public static List<Integer> primes(int n) { return primesUpTo(n); }

    public static int nextPrime(int n) {
        int candidate = Math.max(2, n + 1);
        while (!isPrime(candidate)) candidate++;
        return candidate;
    }

    public static boolean isPerfectSquare(int n) {
        if (n < 0) return false;
        int root = (int) Math.sqrt(n);
        return root * root == n;
    }
    public static boolean isSquare(int n) { return isPerfectSquare(n); }

    public static boolean isPerfectCube(int n) {
        int cube = (int) Math.round(Math.cbrt(Math.abs(n)));
        return cube * cube * cube == Math.abs(n);
    }
    public static boolean isCube(int n) { return isPerfectCube(n); }

    /** Armstrong number: 153 = 1³ + 5³ + 3³. */
    public static boolean isArmstrong(int n) {
        if (n < 0) return false;
        int original = n;
        int digits = countDigits(n);
        int sum = 0;
        while (n > 0) { sum += (int) Math.pow(n % 10, digits); n /= 10; }
        return sum == original;
    }
    public static boolean isNarcissistic(int n) { return isArmstrong(n); }

    /** Palindromic number: 121, 12321, 11, 0. */
    public static boolean isPalindromeNumber(int n) {
        if (n < 0) return false;
        return n == reverseDigits(n);
    }
    public static boolean isNumericPalindrome(int n) { return isPalindromeNumber(n); }

    /** Perfect number: sum of proper divisors equals n. 6, 28, 496, 8128. */
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
    public static List<Integer> factors(int n) { return divisors(n); }

    public static int countDivisors(int n) { return divisors(n).size(); }
    public static int sumDivisors(int n) {
        int sum = 0;
        for (int d : divisors(n)) sum += d;
        return sum;
    }

    public static long factorial(int n) {
        if (n < 0) return 0;
        if (n > 20) throw new IllegalArgumentException("Factorial overflow for n > 20");
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
    public static long fact(int n) { return factorial(n); }

    public static int fibonacci(int n) {
        if (n < 0) return 0;
        if (n < 2) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) { int t = a + b; a = b; b = t; }
        return b;
    }
    public static int fib(int n) { return fibonacci(n); }

    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) { int t = b; b = a % b; a = t; }
        return a;
    }
    public static int hcf(int a, int b) { return gcd(a, b); }

    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a / gcd(a, b) * b);
    }

    // ============================================================== digits

    public static int sumDigits(int n) {
        n = Math.abs(n);
        int sum = 0;
        while (n > 0) { sum += n % 10; n /= 10; }
        return sum;
    }
    public static int digitSum(int n) { return sumDigits(n); }

    public static int productDigits(int n) {
        n = Math.abs(n);
        if (n == 0) return 0;
        int product = 1;
        while (n > 0) { product *= n % 10; n /= 10; }
        return product;
    }
    public static int digitProduct(int n) { return productDigits(n); }

    public static int countDigits(int n) {
        n = Math.abs(n);
        if (n == 0) return 1;
        int count = 0;
        while (n > 0) { count++; n /= 10; }
        return count;
    }
    public static int numDigits(int n) { return countDigits(n); }
    public static int digitCount(int n) { return countDigits(n); }

    public static int reverseDigits(int n) {
        boolean negative = n < 0;
        n = Math.abs(n);
        int result = 0;
        while (n > 0) { result = result * 10 + n % 10; n /= 10; }
        return negative ? -result : result;
    }
    public static int reverseNumber(int n) { return reverseDigits(n); }

    public static int firstDigit(int n) {
        n = Math.abs(n);
        while (n >= 10) n /= 10;
        return n;
    }
    public static int lastDigit(int n) { return Math.abs(n) % 10; }

    // ============================================================== bases

    public static String toBinary(int n) { return Integer.toBinaryString(n); }
    public static String binary(int n) { return toBinary(n); }
    public static String toHex(int n) { return Integer.toHexString(n); }
    public static String hex(int n) { return toHex(n); }
    public static String toOctal(int n) { return Integer.toOctalString(n); }
    public static String octal(int n) { return toOctal(n); }
    public static String toBase(int n, int base) {
        if (base < 2 || base > 36) return Integer.toString(n);
        return Integer.toString(n, base);
    }

    public static Integer fromBinary(String s) {
        try {
            return Integer.parseInt(s, 2);
        } catch (Exception e) {
            return null;
        }
    }
    public static Integer fromHex(String s) {
        try {
            return Integer.parseInt(s, 16);
        } catch (Exception e) {
            return null;
        }
    }
    public static Integer fromOctal(String s) {
        try {
            return Integer.parseInt(s, 8);
        } catch (Exception e) {
            return null;
        }
    }
    public static Integer fromBase(String s, int base) {
        if (s == null || base < 2 || base > 36) return null;
        try {
            return Integer.parseInt(s, base);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============================================================== parsing

    public static Integer parseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Integer toInt(String s) { return parseInt(s); }
    public static Integer asInt(String s) { return parseInt(s); }
    public static Integer intOrNull(String s) { return parseInt(s); }
    public static Integer tryParseInt(String s) { return parseInt(s); }

    public static Integer parseInt(String s, Integer defaultValue) {
        Integer result = parseInt(s);
        return result != null ? result : defaultValue;
    }
    public static int parseIntOr(String s, int defaultValue) {
        Integer result = parseInt(s);
        return result != null ? result : defaultValue;
    }

    public static Double parseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Double toDouble(String s) { return parseDouble(s); }
    public static Double asDouble(String s) { return parseDouble(s); }
    public static Double doubleOrNull(String s) { return parseDouble(s); }

    public static Double parseDouble(String s, Double defaultValue) {
        Double result = parseDouble(s);
        return result != null ? result : defaultValue;
    }
    public static double parseDoubleOr(String s, double defaultValue) {
        Double result = parseDouble(s);
        return result != null ? result : defaultValue;
    }

    public static Long parseLong(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Long toLong(String s) { return parseLong(s); }
    public static Long asLong(String s) { return parseLong(s); }

    // ============================================================== rounding / format

    public static double round(double value, int decimals) {
        if (decimals < 0) return value;
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
    public static double rounded(double value, int decimals) { return round(value, decimals); }

    public static String format(double value, int decimals) {
        return String.format(Locale.ENGLISH, "%." + decimals + "f", value);
    }
    public static String fixed(double value, int decimals) { return format(value, decimals); }

    /** Key-value format: {@code Numbers.format(v, Key.DECIMALS, 2, Key.THOUSANDS, true)}. */
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
     * Ordinal "1st", "2nd", "3rd", "4th" — 11/12/13 (and 111, 112, ...) take
     * "th"; negatives keep their sign ("-1st"). Aliased by {@link Format#ordinal(int)}.
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

    // ============================================================== abbreviations / human

    /** "1.5K", "2.3M", "4.1B", "5.2T" — compact number display. */
    public static String abbreviate(long n) {
        long abs = Math.abs(n);
        String sign = n < 0 ? "-" : "";
        if (abs < 1_000L) return sign + abs;
        if (abs < 1_000_000L) return sign + trim(abs / 1_000.0) + "K";
        if (abs < 1_000_000_000L) return sign + trim(abs / 1_000_000.0) + "M";
        if (abs < 1_000_000_000_000L) return sign + trim(abs / 1_000_000_000.0) + "B";
        return sign + trim(abs / 1_000_000_000_000.0) + "T";
    }
    public static String compact(long n) { return abbreviate(n); }
    public static String humanShort(long n) { return abbreviate(n); }

    /** "512 B", "1.0 KB", "1.5 MB", "2.3 GB", "1.7 TB". */
    public static String fileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024L) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        double value = bytes;
        int unit = -1;
        while (value >= 1024.0 && unit < units.length - 1) { value /= 1024.0; unit++; }
        return String.format(Locale.ENGLISH, "%.1f %s", value, units[unit]);
    }
    public static String humanSize(long bytes) { return fileSize(bytes); }
    public static String bytes(long bytes) { return fileSize(bytes); }

    /** Thousands separator: "1,234,567". */
    public static String thousands(long n) {
        return String.format(Locale.ENGLISH, "%,d", n);
    }
    public static String withCommas(long n) { return thousands(n); }
    public static String grouped(long n) { return thousands(n); }

    /** Roman numeral for 1–3999; input outside returns the decimal string. */
    public static String roman(int n) {
        if (n <= 0 || n > 3999) return Integer.toString(n);
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[n / 1000] + hundreds[(n % 1000) / 100]
                + tens[(n % 100) / 10] + ones[n % 10];
    }
    public static String toRoman(int n) { return roman(n); }

    private static String trim(double value) {
        if (value == Math.rint(value)) return Integer.toString((int) value);
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    // ============================================================== ranges

    public static List<Integer> range(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) result.add(i);
        return result;
    }
    public static List<Integer> to(int start, int end) { return range(start, end); }
    public static List<Integer> span(int start, int end) { return range(start, end); }
    public static List<Integer> upto(int start, int end) { return range(start, end); }

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

    public static List<Integer> rangeInclusive(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i++) result.add(i);
        return result;
    }
    public static List<Integer> rangeClosed(int start, int end) { return rangeInclusive(start, end); }

    public static int[] rangeArray(int start, int end) {
        return range(start, end).stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] rangeArray(int start, int end, int step) {
        return range(start, end, step).stream().mapToInt(Integer::intValue).toArray();
    }

    // ============================================================== aggregation (arrays)

    public static int sum(int[] values) {
        if (values == null) return 0;
        int total = 0;
        for (int v : values) total += v;
        return total;
    }
    public static int total(int[] values) { return sum(values); }
    public static int sumOf(int[] values) { return sum(values); }
    public static int sum(int[] values, int from, int to) {
        if (values == null) return 0;
        int lo = Math.max(0, from), hi = Math.min(values.length, to);
        int total = 0;
        for (int i = lo; i < hi; i++) total += values[i];
        return total;
    }

    public static double sum(double[] values) {
        if (values == null) return 0;
        double total = 0;
        for (double v : values) total += v;
        return total;
    }

    public static double average(int[] values) {
        if (values == null || values.length == 0) return 0;
        return (double) sum(values) / values.length;
    }
    public static double mean(int[] values) { return average(values); }
    public static double avg(int[] values) { return average(values); }
    public static double averageOf(int[] values) { return average(values); }

    public static int max(int[] values) {
        if (values == null || values.length == 0) return 0;
        int best = values[0];
        for (int v : values) if (v > best) best = v;
        return best;
    }
    public static int min(int[] values) {
        if (values == null || values.length == 0) return 0;
        int best = values[0];
        for (int v : values) if (v < best) best = v;
        return best;
    }
    public static int maxOf(int[] values) { return max(values); }
    public static int minOf(int[] values) { return min(values); }

    public static int count(int[] values, int target) {
        if (values == null) return 0;
        int n = 0;
        for (int v : values) if (v == target) n++;
        return n;
    }
    public static int countOf(int[] values, int target) { return count(values, target); }

    public static boolean contains(int[] values, int target) {
        if (values == null) return false;
        for (int v : values) if (v == target) return true;
        return false;
    }
    public static boolean has(int[] values, int target) { return contains(values, target); }

    // ============================================================== statistics

    public record Statistics(
            long count,
            long sum,
            double average,
            int min,
            int max,
            double stdDev,
            double median,
            int mode) {}

    public static Statistics statistics(int[] values) {
        return statistics(values, (Object[]) null);
    }

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

    public static double median(int[] values) {
        if (values == null || values.length == 0) return 0;
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        return (sorted.length % 2 == 1)
                ? sorted[sorted.length / 2]
                : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
    }

    public static int mode(int[] values) {
        return statistics(values).mode();
    }

    public static double standardDeviation(int[] values) {
        return statistics(values).stdDev();
    }
    public static double stdDev(int[] values) { return standardDeviation(values); }
    public static double sampleStdDev(int[] values) {
        return statistics(values, Key.SAMPLE, true).stdDev();
    }

    public static double variance(int[] values) {
        double sd = standardDeviation(values);
        return sd * sd;
    }
    public static double sampleVariance(int[] values) {
        double sd = sampleStdDev(values);
        return sd * sd;
    }

    // ============================================================== percent

    public static double percent(double part, double whole) {
        return whole == 0 ? 0 : part / whole * 100;
    }
    public static double percentage(double part, double whole) { return percent(part, whole); }
    public static double pct(double part, double whole) { return percent(part, whole); }

    public static double applyPercent(double value, double pct) {
        return value * pct / 100;
    }

    // ============================================================== chains

    public static NumberChain of(int n) { return NumberChain.of(n); }
    public static NumberChain of(double n) { return NumberChain.of(n); }
    public static NumberChain chain(int n) { return NumberChain.of(n); }
    public static NumberChain chain(double n) { return NumberChain.of(n); }
    public static NumberChain from(int n) { return NumberChain.of(n); }
    public static NumberChain from(double n) { return NumberChain.of(n); }
}
