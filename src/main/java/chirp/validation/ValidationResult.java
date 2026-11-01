package chirp.validation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable result of a validation run.
 *
 * <p><b>Naming note:</b> {@code valid()}, {@code success()}, {@code empty()},
 * and {@code clean()} are <b>static factories</b> — they return a new
 * {@code ValidationResult} instance.</p>
 *
 * <p>The instance-status accessors are {@code isValid()}, {@code ok()},
 * {@code passed()}, {@code good()}, {@code isEmpty()} — these return
 * {@code boolean}.</p>
 *
 * <p>This is why {@code assertTrue(ValidationResult.valid())} does not
 * compile — {@code valid()} returns a {@code ValidationResult}, not a
 * {@code boolean}. Use {@code assertTrue(ValidationResult.valid().isValid())}
 * instead.</p>
 */
public final class ValidationResult {

    private final List<String> errors;

    public ValidationResult(List<String> errors) {
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }

    // ============================================================== static factories

    public static ValidationResult valid() { return new ValidationResult(List.of()); }
    public static ValidationResult success() { return new ValidationResult(List.of()); }
    public static ValidationResult empty() { return new ValidationResult(List.of()); }
    public static ValidationResult clean() { return new ValidationResult(List.of()); }

    public static ValidationResult of(String... messages) {
        return new ValidationResult(messages == null ? List.of() : List.of(messages));
    }
    public static ValidationResult errors(String... messages) { return of(messages); }

    // ============================================================== status

    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasError() { return hasErrors(); }
    public boolean isInvalid() { return !errors.isEmpty(); }
    public boolean isNotValid() { return !errors.isEmpty(); }
    public boolean failed() { return !errors.isEmpty(); }

    public boolean isValid() { return errors.isEmpty(); }
    public boolean ok() { return errors.isEmpty(); }
    public boolean passed() { return errors.isEmpty(); }
    public boolean good() { return errors.isEmpty(); }
    public boolean isEmpty() { return errors.isEmpty(); }

    // ============================================================== accessors

    public List<String> errors() { return Collections.unmodifiableList(errors); }
    public List<String> all() { return Collections.unmodifiableList(errors); }
    public List<String> messages() { return Collections.unmodifiableList(errors); }
    public List<String> asList() { return Collections.unmodifiableList(errors); }
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }

    public Stream<String> stream() { return errors.stream(); }
    public Stream<String> toStream() { return errors.stream(); }

    public int count() { return errors.size(); }
    public int errorCount() { return errors.size(); }
    public int size() { return errors.size(); }
    public int length() { return errors.size(); }

    public String firstError() { return errors.isEmpty() ? null : errors.get(0); }
    public String first() { return firstError(); }
    public String firstMessage() { return firstError(); }

    public String firstOrDefault(String fallback) {
        return errors.isEmpty() ? fallback : errors.get(0);
    }

    public String lastError() { return errors.isEmpty() ? null : errors.get(errors.size() - 1); }
    public String last() { return lastError(); }

    // ============================================================== formatting

    public String join(String delimiter) { return String.join(delimiter, errors); }
    public String joined(String delimiter) { return join(delimiter); }
    public String toLine() { return join("; "); }
    public String oneLine() { return join(" | "); }

    public void print(String prefix) {
        String p = prefix == null ? "[ERROR] " : prefix;
        for (String error : errors) System.out.println(p + error);
    }
    public void print() { print("[ERROR] "); }
    public void printAll() { print(); }

    // ============================================================== merge

    public ValidationResult merge(ValidationResult other) {
        if (other == null || other.isEmpty()) return this;
        if (this.isEmpty()) return other;
        List<String> combined = new java.util.ArrayList<>(errors);
        combined.addAll(other.errors);
        return new ValidationResult(combined);
    }
    public ValidationResult and(ValidationResult other) { return merge(other); }
    public ValidationResult plus(ValidationResult other) { return merge(other); }

    @Override
    public String toString() {
        return errors.isEmpty() ? "ValidationResult[valid]" : "ValidationResult" + errors;
    }
}
