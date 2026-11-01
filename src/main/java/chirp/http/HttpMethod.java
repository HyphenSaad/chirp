package chirp.http;

import java.util.Locale;

/**
 * HTTP verbs — every value supports IDE autocomplete.
 *
 * <p>Seven standard HTTP methods are provided. Each has three helper
 * predicates for use when chaining or configuring requests:</p>
 * <ul>
 *   <li>{@link #isSafe()} — GET, HEAD, OPTIONS. These should not modify
 *       server state.</li>
 *   <li>{@link #hasBody()} — POST, PUT, PATCH. These carry a request
 *       body by convention.</li>
 *   <li>{@link #returnsBody()} — everything except HEAD. HEAD returns
 *       the same headers as GET but no body.</li>
 * </ul>
 *
 * <h2>Parsing from a string</h2>
 * <p>{@link #parse(String)} is case-insensitive and returns
 * {@link #GET} for any unrecognised input — this makes it safe for
 * config-driven request construction where the verb might be misspelled.
 * Callers who need to distinguish "not a valid verb" from "GET" should
 * use {@link #valueOf(String)} (which throws) instead.</p>
 *
 * <pre>{@code
 * HttpMethod.parse("post");      // POST
 * HttpMethod.parse("DELETE");    // DELETE
 * HttpMethod.parse("unknown");   // GET
 * HttpMethod.from("Put");        // PUT
 * }</pre>
 *
 * <h2>Example — conditional body handling</h2>
 * <pre>{@code
 * HttpRequest req = Http.request(method, url);
 * if (method.hasBody() && payload != null) {
 *     req.json(payload);
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the request builder, see {@link HttpRequest}. For the response
 * record, see {@link HttpResponse}.</p>
 *
 * @see HttpRequest
 * @see HttpResponse
 * @since 1.0.0
 */
public enum HttpMethod {
    /** Retrieve data — no body. */
    GET,

    /** Submit data — body required. */
    POST,

    /** Replace data — body required. */
    PUT,

    /** Remove data — no body. */
    DELETE,

    /** Partially modify data — body required. */
    PATCH,

    /** Headers only, no body in response. */
    HEAD,

    /** Discover allowed methods. */
    OPTIONS;

    /**
     * Parses a string into an HTTP verb, case-insensitively. Returns
     * {@link #GET} for any unrecognised input.
     *
     * @param value the input (may be null → GET)
     * @return the parsed method, or GET for unknown input
     * @see #from(String)
     * @see #of(String)
     * @see #valueOfSafe(String)
     * @since 1.0.0
     */
    public static HttpMethod parse(String value) {
        if (value == null) return GET;
        try {
            return HttpMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }

    /**
     * Alias of {@link #parse(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the input
     * @return the parsed method, or GET
     * @see #parse(String)
     * @since 1.0.0
     */
    public static HttpMethod from(String value) { return parse(value); }

    /**
     * Alias of {@link #parse(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the input
     * @return the parsed method, or GET
     * @see #parse(String)
     * @since 1.0.0
     */
    public static HttpMethod of(String value) { return parse(value); }

    /**
     * Alias of {@link #parse(String)} — see that method for full
     * documentation and examples.
     *
     * @param value the input
     * @return the parsed method, or GET
     * @see #parse(String)
     * @since 1.0.0
     */
    public static HttpMethod valueOfSafe(String value) { return parse(value); }

    /**
     * Checks whether this method should not modify server state by
     * convention — GET, HEAD, OPTIONS.
     *
     * @return {@code true} for safe methods
     * @see #hasBody()
     * @since 1.0.0
     */
    public boolean isSafe() {
        return this == GET || this == HEAD || this == OPTIONS;
    }

    /**
     * Checks whether this method carries a request body by convention —
     * POST, PUT, PATCH.
     *
     * @return {@code true} for body-carrying methods
     * @see #isSafe()
     * @since 1.0.0
     */
    public boolean hasBody() {
        return this == POST || this == PUT || this == PATCH;
    }

    /**
     * Checks whether the server is expected to return a body — everything
     * except HEAD.
     *
     * @return {@code true} for methods that return a body
     * @since 1.0.0
     */
    public boolean returnsBody() {
        return this != HEAD;
    }
}
