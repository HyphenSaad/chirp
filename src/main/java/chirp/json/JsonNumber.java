package chirp.json;

import java.util.regex.Pattern;

/**
 * A JSON number. The raw token is preserved so that large IDs do not lose
 * precision when read and written back.
 */
public final class JsonNumber implements JsonValue {

    private static final Pattern VALID = Pattern.compile("-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?");

    private final String raw;

    public JsonNumber(String raw) {
        if (raw == null || !VALID.matcher(raw).matches()) {
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
    public double asDouble() { return Double.parseDouble(raw); }
    public long asLong() { return (long) asDouble(); }
    public int asInt() { return (int) asDouble(); }
    public boolean isInteger() {
        return raw.indexOf('.') < 0 && raw.indexOf('e') < 0 && raw.indexOf('E') < 0;
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
