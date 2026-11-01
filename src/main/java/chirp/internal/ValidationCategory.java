package chirp.internal;

/**
 * The categories of validation a reader tracks for strict-mode
 * warnings — the five dimensions a fluent reader can be strict about.
 *
 * <p><b>Internal API.</b> Do not reference directly. The constants are
 * used by {@link Strict} to determine which warnings to emit and by the
 * fluent readers ({@link chirp.console.input.StringReader} and friends)
 * to track which checks the caller has applied.</p>
 *
 * <h2>The five categories</h2>
 * <table border="1">
 *   <caption>Category → what it represents → how to cover it</caption>
 *   <tr><th>Category</th><th>Represents</th><th>Covered by</th></tr>
 *   <tr><td>{@code RANGE}</td>
 *       <td>Value bounded between min and max</td>
 *       <td>{@code .between(...)}, {@code .min(...)}, {@code .max(...)},
 *           {@code .positive()}, ...</td></tr>
 *   <tr><td>{@code BLANK}</td>
 *       <td>Non-blank string</td>
 *       <td>{@code .notBlank()}, {@code .required()}</td></tr>
 *   <tr><td>{@code LENGTH}</td>
 *       <td>String length bounded</td>
 *       <td>{@code .minLength(n)}, {@code .maxLength(n)},
 *           {@code .lengthBetween(a, b)}</td></tr>
 *   <tr><td>{@code FORMAT}</td>
 *       <td>String matches a pattern</td>
 *       <td>{@code .email()}, {@code .phone()}, {@code .url()},
 *           {@code .matches(...)}, {@code .oneOf(...)},
 *           {@code .rule(...)}</td></tr>
 *   <tr><td>{@code CUSTOM}</td>
 *       <td>Custom predicate</td>
 *       <td>{@code .custom(predicate, error)}</td></tr>
 * </table>
 *
 * <h2>Per-reader applicability</h2>
 * <p>Not every reader cares about every category. A boolean reader has
 * no range, blank, length, or format dimension — it never warns. An int
 * reader cares only about range. A string reader cares about blank,
 * length, and format. The reader class documents which categories it
 * uses.</p>
 *
 * @see Strict
 * @see chirp.console.input.StringReader
 * @see chirp.console.input.IntReader
 * @since 1.0.0
 */
public enum ValidationCategory {
    /** A numeric or temporal value bounded between min and max. */
    RANGE,
    /** A string that must not be blank. */
    BLANK,
    /** A string whose length must be bounded. */
    LENGTH,
    /** A string that must match a pattern. */
    FORMAT,
    /** A string that must pass a custom predicate. */
    CUSTOM
}
