package chirp.json;

/**
 * A JSON number. The raw token is preserved so that large IDs do not lose
 * precision when read and written back.
 *
 * <p><b>Validation:</b> {@link #JsonNumber(String)} validates the input by
 * a single linear pass over the string against the JSON grammar from
 * RFC 8259 §6. Using a regex would expose the constructor to catastrophic
 * backtracking on adversarial input; the manual parser is O(n) with no
 * backtracking. Behaviour is identical for all valid and invalid inputs.</p>
 */
public final class JsonNumber implements JsonValue {

    private final String raw;

    public JsonNumber(String raw) {
        if (!isValidJsonNumber(raw)) {
            throw new JsonException("Invalid JSON number: " + raw);
        }
        this.raw = raw;
    }

    public static JsonNumber of(long value) { return new JsonNumber(Long.toString(value)); }

    public static JsonNumber of(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new JsonException("JSON cannot represent " + value);
        }
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return new JsonNumber(Long.toString((long) value));
        }
        return new JsonNumber(Double.toString(value));
    }

    public String raw() { return raw; }

    @Override
    public double asDouble() { return Double.parseDouble(raw); }

    @Override
    public long asLong() { return (long) asDouble(); }

    @Override
    public int asInt() { return (int) asDouble(); }

    public boolean isInteger() {
        return raw.indexOf('.') < 0 && raw.indexOf('e') < 0 && raw.indexOf('E') < 0;
    }

    /**
     * JSON number grammar (RFC 8259 §6), single linear pass:
     * <pre>
     *   number = [ '-' ] int [ frac ] [ exp ]
     *   int    = '0' | digit1-9 *DIGIT
     *   frac   = '.' 1*DIGIT
     *   exp    = ('e' | 'E') [ '+' | '-' ] 1*DIGIT
     * </pre>
     *
     * @return {@code true} when {@code s} is a valid JSON number literal
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

    @Override
    public String toJson() { return raw; }
    @Override
    public String toString() { return raw; }
    @Override
    public boolean equals(Object other) {
        return other instanceof JsonNumber number && number.raw.equals(raw);
    }
    @Override
    public int hashCode() { return raw.hashCode(); }
}
