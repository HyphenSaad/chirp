package chirp.json;

/**
 * A JSON number.
 *
 * <p>The raw textual form is preserved so that large integers (e.g. a
 * 19-digit user ID) round-trip exactly, without the precision loss that
 * would happen if the value were parsed to {@code double} first.
 * {@link #asDouble()} converts on demand; {@link #raw()} returns the
 * original text.</p>
 *
 * <h2>Validation</h2>
 * <p>The constructor validates the input against the JSON number grammar
 * from RFC 8259 §6 using a single linear pass. A regex would work, but
 * pathological input like {@code "1" + "0".repeat(100_000)} could
 * trigger catastrophic backtracking; the manual parser is O(n) with no
 * backtracking, so it's safe for adversarial input.</p>
 *
 * <h2>Related</h2>
 * <p>For the base value type, see {@link JsonValue}. For the parser and
 * serialiser entry points, see {@link Json}.</p>
 *
 * @see JsonValue
 * @see Json
 * @since 1.0.0
 */
public final class JsonNumber implements JsonValue {

    private final String raw;

    /**
     * Constructs a JSON number from its raw text. The text must be a
     * valid JSON number literal.
     *
     * @param raw the raw text (non-null, must match the JSON number grammar)
     * @throws JsonException if {@code raw} is not a valid JSON number
     * @see #of(long)
     * @see #of(double)
     * @since 1.0.0
     */
    public JsonNumber(String raw) {
        if (!isValidJsonNumber(raw)) {
            throw new JsonException("Invalid JSON number: " + raw);
        }
        this.raw = raw;
    }

    /**
     * Factory — a JSON number from a long.
     *
     * @param value the value
     * @return a new {@code JsonNumber}
     * @since 1.0.0
     */
    public static JsonNumber of(long value) { return new JsonNumber(Long.toString(value)); }

    /**
     * Factory — a JSON number from a double.
     *
     * <p>Integral doubles within {@code long} range are serialised
     * without a decimal point ({@code 42.0} → {@code "42"}) to match the
     * JSON convention that integers and decimals are distinct but
     * compatible. Non-integral doubles serialise as their
     * {@link Double#toString(double)} form.</p>
     *
     * @param value the value (must be finite)
     * @return a new {@code JsonNumber}
     * @throws JsonException if {@code value} is {@code NaN} or infinite —
     * JSON has no representation for these
     * @since 1.0.0
     */
    public static JsonNumber of(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new JsonException("JSON cannot represent " + value);
        }
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return new JsonNumber(Long.toString((long) value));
        }
        return new JsonNumber(Double.toString(value));
    }

    /**
     * Returns the raw textual form, exactly as it was parsed or
     * constructed.
     *
     * @return the raw text
     * @see #asDouble()
     * @since 1.0.0
     */
    public String raw() { return raw; }

    /**
     * Returns the numeric value as a double.
     *
     * @return the double value
     * @since 1.0.0
     */
    public double asDouble() { return Double.parseDouble(raw); }

    /**
     * Returns the numeric value as a long, truncating any fractional
     * part.
     *
     * @return the long value
     * @since 1.0.0
     */
    public long asLong() { return (long) asDouble(); }

    /**
     * Returns the numeric value as an int, truncating any fractional
     * part.
     *
     * @return the int value
     * @since 1.0.0
     */
    public int asInt() { return (int) asDouble(); }

    /**
     * Checks whether the raw form looks like an integer (no decimal
     * point, no exponent).
     *
     * @return {@code true} if the number is integral
     * @since 1.0.0
     */
    public boolean isInteger() {
        return raw.indexOf('.') < 0 && raw.indexOf('e') < 0 && raw.indexOf('E') < 0;
    }

    /**
     * Validates the input against the JSON number grammar (RFC 8259 §6)
     * using a single linear pass. Package-private helper — not part of
     * the public API.
     *
     * @param s the string to check (may be null)
     * @return {@code true} if {@code s} is a valid JSON number literal
     * @since 1.0.0
     */
    static boolean isValidJsonNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        int n = s.length();

        // Optional leading minus
        if (s.charAt(i) == '-') {
            i++;
            if (i == n) return false;
        }

        // Integer part: "0" alone, or a non-zero digit followed by digits
        if (s.charAt(i) == '0') {
            i++;
        } else if (s.charAt(i) >= '1' && s.charAt(i) <= '9') {
            i++;
            while (i < n && s.charAt(i) >= '0' && s.charAt(i) <= '9') i++;
        } else {
            return false;
        }

        // Optional fractional part: '.' followed by at least one digit
        if (i < n && s.charAt(i) == '.') {
            i++;
            int start = i;
            while (i < n && s.charAt(i) >= '0' && s.charAt(i) <= '9') i++;
            if (i == start) return false;
        }

        // Optional exponent: 'e' / 'E' followed by optional sign and at least one digit
        if (i < n && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
            i++;
            if (i < n && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
            int start = i;
            while (i < n && s.charAt(i) >= '0' && s.charAt(i) <= '9') i++;
            if (i == start) return false;
        }

        return i == n;
    }

    /**
     * Returns the raw text — matching {@link #raw()}.
     *
     * @return the raw text
     * @since 1.0.0
     */
    @Override
    public String toJson() { return raw; }

    /**
     * Returns the raw text — matching {@link #raw()}.
     *
     * @return the raw text
     * @since 1.0.0
     */
    @Override
    public String toString() { return raw; }

    /**
     * Two JSON numbers are equal when their raw text is equal — this
     * preserves the numeric form ({@code 1.0} is not equal to {@code 1}).
     *
     * @param other the number to compare
     * @return {@code true} if the raw text matches
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonNumber number && number.raw.equals(raw);
    }

    /**
     * Hash code matching {@link #equals(Object)}.
     *
     * @return the hash code
     * @since 1.0.0
     */
    @Override
    public int hashCode() { return raw.hashCode(); }
}
