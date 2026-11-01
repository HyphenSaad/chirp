package chirp.validation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable result of a validation run — the collected error messages.
 *
 * <h2>The naming trap: static factories vs. instance accessors</h2>
 * <p>This class has six names that all sound like they should return
 * booleans, but three of them return a {@code ValidationResult} object
 * and three return a {@code boolean}. The distinction matters because a
 * test like {@code assertTrue(result.valid())} will not compile — and a
 * test like {@code assertTrue(ValidationResult.valid())} will compile but
 * then fail the assertion at runtime (because a ValidationResult is
 * truthy in Java only if it were a boolean, which it is not).</p>
 *
 * <table border="1">
 *   <caption>Static factories vs. instance accessors</caption>
 *   <tr><th>Static factory</th><th>Instance accessor</th></tr>
 *   <tr><td>{@link #valid()}</td><td>{@link #isValid()}</td></tr>
 *   <tr><td>{@link #success()}</td><td>{@link #ok()}</td></tr>
 *   <tr><td>{@link #empty()}</td><td>{@link #isEmpty()}</td></tr>
 *   <tr><td>{@link #clean()}</td><td>—</td></tr>
 * </table>
 *
 * <pre>{@code
 * // ✅ Correct — the instance accessor returns a boolean
 * ValidationResult r = Validate.that("user@example.com")
 *     .isEmail("invalid")
 *     .validate();
 * if (r.isValid()) { /* ok *\/ }
 *
 * // ✅ Correct — the static factory returns an empty result object
 * ValidationResult noErrors = ValidationResult.valid();
 *
 * // ❌ Will not compile — valid() is a static factory, not an accessor
 * // boolean b = r.valid();
 *
 * // ❌ Will compile but always fail — a ValidationResult is never a boolean
 * // assertTrue(ValidationResult.valid());
 *
 * // ✅ Correct form of the same assertion
 * assertTrue(ValidationResult.valid().isValid());
 * }</pre>
 *
 * <h2>Immutability</h2>
 * <p>The error list is defensively copied in the constructor. The
 * returned list from {@link #errors()} is unmodifiable — callers cannot
 * mutate the result after construction.</p>
 *
 * <h2>Merging results</h2>
 * <p>{@link #merge(ValidationResult)} combines two results into one,
 * concatenating the errors. Useful when validating multiple fields and
 * wanting to show all errors at once.</p>
 *
 * <h2>Example — the standard validation flow</h2>
 * <pre>{@code
 * ValidationResult r = Validate.of("Email", email)
 *     .notBlank("required")
 *     .isEmail("invalid format")
 *     .validate();
 *
 * if (r.hasErrors()) {
 *     r.errors().forEach(this::error);   // shows "Email: required" or "Email: invalid format"
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the fluent validator that produces results, see
 * {@link Validate}. For single-rule testing, see {@link Rule}. For the
 * registry, see {@link Rules}.</p>
 *
 * @see Validate
 * @see Rule
 * @see Rules
 * @since 1.0.0
 */
public final class ValidationResult {

    private final List<String> errors;

    /**
     * Constructs a result from the given error list. {@code null} is
     * treated as an empty error list.
     *
     * <p>Usually you should not construct a result directly — use
     * {@link Validate#validate()} or the static factories
     * {@link #valid()} and {@link #of(String...)}.</p>
     *
     * @param errors the error list (may be null — treated as empty)
     * @see #valid()
     * @see #of(String...)
     * @see Validate#validate()
     * @since 1.0.0
     */
    public ValidationResult(List<String> errors) {
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }

    // ============================================================== static factories

    /**
     * Returns an empty (valid) result.
     *
     * <p>This is a <b>static factory</b>, not an instance accessor. For
     * the boolean "does this result have no errors" check, use
     * {@link #isValid()} on an instance.</p>
     *
     * @return a new empty {@code ValidationResult}
     * @see #success()
     * @see #empty()
     * @see #clean()
     * @see #isValid()
     * @since 1.0.0
     */
    public static ValidationResult valid() { return new ValidationResult(List.of()); }

    /**
     * Alias of {@link #valid()} — see that method for full documentation.
     *
     * @return a new empty {@code ValidationResult}
     * @see #valid()
     * @since 1.0.0
     */
    public static ValidationResult success() { return new ValidationResult(List.of()); }

    /**
     * Alias of {@link #valid()} — see that method for full documentation.
     *
     * @return a new empty {@code ValidationResult}
     * @see #valid()
     * @since 1.0.0
     */
    public static ValidationResult empty() { return new ValidationResult(List.of()); }

    /**
     * Alias of {@link #valid()} — see that method for full documentation.
     *
     * @return a new empty {@code ValidationResult}
     * @see #valid()
     * @since 1.0.0
     */
    public static ValidationResult clean() { return new ValidationResult(List.of()); }

    /**
     * Constructs a result from the given error messages.
     *
     * @param messages the error messages (may be null — treated as empty)
     * @return a new {@code ValidationResult} holding the errors
     * @see #errors(String...)
     * @see #valid()
     * @since 1.0.0
     */
    public static ValidationResult of(String... messages) {
        return new ValidationResult(messages == null ? List.of() : List.of(messages));
    }

    /**
     * Alias of {@link #of(String...)} — see that method for full
     * documentation.
     *
     * @param messages the error messages
     * @return a new {@code ValidationResult}
     * @see #of(String...)
     * @since 1.0.0
     */
    public static ValidationResult errors(String... messages) { return of(messages); }

    // ============================================================== status

    /**
     * Checks whether the result has any errors — the boolean accessor.
     *
     * @return {@code true} if any errors are present
     * @see #hasError()
     * @see #isInvalid()
     * @see #failed()
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean hasErrors() { return !errors.isEmpty(); }

    /**
     * Alias of {@link #hasErrors()} — see that method for full
     * documentation.
     *
     * @return {@code true} if any errors are present
     * @see #hasErrors()
     * @since 1.0.0
     */
    public boolean hasError() { return hasErrors(); }

    /**
     * Alias of {@link #hasErrors()} — see that method for full
     * documentation.
     *
     * @return {@code true} if any errors are present
     * @see #hasErrors()
     * @since 1.0.0
     */
    public boolean isInvalid() { return !errors.isEmpty(); }

    /**
     * Alias of {@link #hasErrors()} — see that method for full
     * documentation.
     *
     * @return {@code true} if any errors are present
     * @see #hasErrors()
     * @since 1.0.0
     */
    public boolean isNotValid() { return !errors.isEmpty(); }

    /**
     * Alias of {@link #hasErrors()} — see that method for full
     * documentation.
     *
     * @return {@code true} if any errors are present
     * @see #hasErrors()
     * @since 1.0.0
     */
    public boolean failed() { return !errors.isEmpty(); }

    /**
     * Checks whether the result is valid — no errors. The boolean
     * accessor.
     *
     * <p>For the static factory that creates an empty result, use
     * {@link #valid()} — a different method with a similar name.</p>
     *
     * @return {@code true} if no errors are present
     * @see #ok()
     * @see #passed()
     * @see #good()
     * @see #isEmpty()
     * @see #hasErrors()
     * @since 1.0.0
     */
    public boolean isValid() { return errors.isEmpty(); }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation.
     *
     * @return {@code true} if no errors
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean ok() { return errors.isEmpty(); }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation.
     *
     * @return {@code true} if no errors
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean passed() { return errors.isEmpty(); }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation.
     *
     * @return {@code true} if no errors
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean good() { return errors.isEmpty(); }

    /**
     * Alias of {@link #isValid()} — see that method for full
     * documentation. Note that this is an <b>instance accessor</b> —
     * {@link #empty()} (with empty parentheses after the class name) is
     * the static factory.
     *
     * @return {@code true} if no errors
     * @see #isValid()
     * @since 1.0.0
     */
    public boolean isEmpty() { return errors.isEmpty(); }

    // ============================================================== accessors

    /**
     * Returns the error messages as an unmodifiable list.
     *
     * @return the errors (never null)
     * @see #all()
     * @see #messages()
     * @see #asList()
     * @see #getErrors()
     * @since 1.0.0
     */
    public List<String> errors() { return Collections.unmodifiableList(errors); }

    /**
     * Alias of {@link #errors()} — see that method for full
     * documentation.
     *
     * @return the errors
     * @see #errors()
     * @since 1.0.0
     */
    public List<String> all() { return Collections.unmodifiableList(errors); }

    /**
     * Alias of {@link #errors()} — see that method for full
     * documentation.
     *
     * @return the errors
     * @see #errors()
     * @since 1.0.0
     */
    public List<String> messages() { return Collections.unmodifiableList(errors); }

    /**
     * Alias of {@link #errors()} — see that method for full
     * documentation.
     *
     * @return the errors
     * @see #errors()
     * @since 1.0.0
     */
    public List<String> asList() { return Collections.unmodifiableList(errors); }

    /**
     * Alias of {@link #errors()} — see that method for full
     * documentation.
     *
     * @return the errors
     * @see #errors()
     * @since 1.0.0
     */
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }

    /**
     * Returns a stream over the errors, for functional processing.
     *
     * @return the stream of error messages
     * @see #toStream()
     * @since 1.0.0
     */
    public Stream<String> stream() { return errors.stream(); }

    /**
     * Alias of {@link #stream()} — see that method for full
     * documentation.
     *
     * @return the stream of error messages
     * @see #stream()
     * @since 1.0.0
     */
    public Stream<String> toStream() { return errors.stream(); }

    /**
     * Returns the number of errors collected.
     *
     * @return the count
     * @see #errorCount()
     * @see #size()
     * @see #length()
     * @since 1.0.0
     */
    public int count() { return errors.size(); }

    /**
     * Alias of {@link #count()} — see that method for full documentation.
     *
     * @return the error count
     * @see #count()
     * @since 1.0.0
     */
    public int errorCount() { return errors.size(); }

    /**
     * Alias of {@link #count()} — see that method for full documentation.
     *
     * @return the error count
     * @see #count()
     * @since 1.0.0
     */
    public int size() { return errors.size(); }

    /**
     * Alias of {@link #count()} — see that method for full documentation.
     *
     * @return the error count
     * @see #count()
     * @since 1.0.0
     */
    public int length() { return errors.size(); }

    /**
     * Returns the first error message, or {@code null} if there are no
     * errors.
     *
     * @return the first error, or null
     * @see #first()
     * @see #firstMessage()
     * @see #firstOrDefault(String)
     * @see #lastError()
     * @since 1.0.0
     */
    public String firstError() { return errors.isEmpty() ? null : errors.get(0); }

    /**
     * Alias of {@link #firstError()} — see that method for full
     * documentation.
     *
     * @return the first error, or null
     * @see #firstError()
     * @since 1.0.0
     */
    public String first() { return firstError(); }

    /**
     * Alias of {@link #firstError()} — see that method for full
     * documentation.
     *
     * @return the first error, or null
     * @see #firstError()
     * @since 1.0.0
     */
    public String firstMessage() { return firstError(); }

    /**
     * Returns the first error, or {@code fallback} if there are no
     * errors.
     *
     * @param fallback the value to return when no errors are present
     * @return the first error or the fallback
     * @since 1.0.0
     */
    public String firstOrDefault(String fallback) {
        return errors.isEmpty() ? fallback : errors.get(0);
    }

    /**
     * Returns the last error message, or {@code null} if there are no
     * errors.
     *
     * @return the last error, or null
     * @see #last()
     * @see #firstError()
     * @since 1.0.0
     */
    public String lastError() { return errors.isEmpty() ? null : errors.get(errors.size() - 1); }

    /**
     * Alias of {@link #lastError()} — see that method for full
     * documentation.
     *
     * @return the last error, or null
     * @see #lastError()
     * @since 1.0.0
     */
    public String last() { return lastError(); }

    // ============================================================== formatting

    /**
     * Joins the errors with the given delimiter.
     *
     * @param delimiter the separator
     * @return the joined string
     * @see #joined(String)
     * @see #toLine()
     * @see #oneLine()
     * @since 1.0.0
     */
    public String join(String delimiter) { return String.join(delimiter, errors); }

    /**
     * Alias of {@link #join(String)} — see that method for full
     * documentation.
     *
     * @param delimiter the separator
     * @return the joined string
     * @see #join(String)
     * @since 1.0.0
     */
    public String joined(String delimiter) { return join(delimiter); }

    /**
     * Joins the errors with {@code "; "} — one-line form.
     *
     * @return the joined string
     * @see #join(String)
     * @since 1.0.0
     */
    public String toLine() { return join("; "); }

    /**
     * Joins the errors with {@code " | "} — alternative one-line form.
     *
     * @return the joined string
     * @see #join(String)
     * @since 1.0.0
     */
    public String oneLine() { return join(" | "); }

    /**
     * Prints each error to {@link System#out} with the given prefix.
     *
     * @param prefix the prefix for each line (null → {@code "[ERROR] "})
     * @see #print()
     * @see #printAll()
     * @since 1.0.0
     */
    public void print(String prefix) {
        String p = prefix == null ? "[ERROR] " : prefix;
        for (String error : errors) System.out.println(p + error);
    }

    /**
     * Prints each error to {@link System#out} with the default
     * {@code "[ERROR] "} prefix.
     *
     * @see #print(String)
     * @since 1.0.0
     */
    public void print() { print("[ERROR] "); }

    /**
     * Alias of {@link #print()} — see that method for full documentation.
     *
     * @see #print()
     * @since 1.0.0
     */
    public void printAll() { print(); }

    // ============================================================== merge

    /**
     * Merges the errors from the other result into this one, returning a
     * new combined result.
     *
     * <p>If either result is empty, the other is returned unchanged. Null
     * argument returns this result.</p>
     *
     * @param other the result to merge in (may be null)
     * @return a new combined result
     * @see #and(ValidationResult)
     * @see #plus(ValidationResult)
     * @since 1.0.0
     */
    public ValidationResult merge(ValidationResult other) {
        if (other == null || other.isEmpty()) return this;
        if (this.isEmpty()) return other;
        List<String> combined = new java.util.ArrayList<>(errors);
        combined.addAll(other.errors);
        return new ValidationResult(combined);
    }

    /**
     * Alias of {@link #merge(ValidationResult)} — see that method for full
     * documentation.
     *
     * @param other the result to merge in
     * @return a new combined result
     * @see #merge(ValidationResult)
     * @since 1.0.0
     */
    public ValidationResult and(ValidationResult other) { return merge(other); }

    /**
     * Alias of {@link #merge(ValidationResult)} — see that method for full
     * documentation.
     *
     * @param other the result to merge in
     * @return a new combined result
     * @see #merge(ValidationResult)
     * @since 1.0.0
     */
    public ValidationResult plus(ValidationResult other) { return merge(other); }

    /**
     * Returns a short representation — {@code "ValidationResult[valid]"}
     * for empty results, or the error list for non-empty.
     *
     * @return the representation
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return errors.isEmpty() ? "ValidationResult[valid]" : "ValidationResult" + errors;
    }
}
