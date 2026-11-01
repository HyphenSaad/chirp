package chirp.http;

/**
 * HTTP verbs — every value supports IDE autocomplete.
 *
 * <p>Aliases: {@code parse / from / of / valueOfSafe}.</p>
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

    /** Case-insensitive lookup; returns GET for unknown inputs. */
    public static HttpMethod parse(String value) {
        if (value == null) return GET;
        try {
            return HttpMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }
    public static HttpMethod from(String value) { return parse(value); }
    public static HttpMethod of(String value) { return parse(value); }
    public static HttpMethod valueOfSafe(String value) { return parse(value); }

    /** True for verbs that should not modify server state. */
    public boolean isSafe() {
        return this == GET || this == HEAD || this == OPTIONS;
    }

    /** True for verbs that carry a body by convention. */
    public boolean hasBody() {
        return this == POST || this == PUT || this == PATCH;
    }

    /** True for verbs that typically return data. */
    public boolean returnsBody() {
        return this != HEAD;
    }
}
