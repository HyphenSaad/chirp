package chirp.internal;

import chirp.config.Config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emits strict-mode warnings to stderr when a reader or an immediate method
 * is used without the validation rules that would normally be expected.
 *
 * <p><strong>Internal API — do not use directly.</strong></p>
 */
public final class Strict {

    private static final String PREFIX = "[STRICT] ";
    private static final Set<String> WARNED_ONCE = ConcurrentHashMap.newKeySet();

    private Strict() {}

    /** Clears the once-per-prompt throttle. Called by {@link Config#reset()}. */
    public static void reset() {
        WARNED_ONCE.clear();
    }

    public static void warnOnce(String type, String label, ValidationCategory... categories) {
        if (!Config.isStrictWarningsEnabled()) return;
        if (!WARNED_ONCE.add(type + ":" + label)) return;
        for (ValidationCategory cat : categories) {
            emit(type, label, cat);
        }
    }

    public static void emit(String readerType, String label, ValidationCategory category) {
        if (!Config.isStrictWarningsEnabled()) return;
        System.err.println(PREFIX + message(readerType, label, category));
    }

    private static String message(String readerType, String label, ValidationCategory category) {
        return switch (category) {
            case RANGE -> "Reading '" + label + "' as " + readerType
                    + ": no RANGE validation. Consider .between(min, max), .positive(), .min(n)/.max(n),"
                    + " or a typed reader like readInt(label, min, max).";
            case BLANK -> "Reading '" + label + "' as " + readerType
                    + ": no BLANK validation. Consider .notBlank() or readNonEmpty(label).";
            case LENGTH -> "Reading '" + label + "' as " + readerType
                    + ": no LENGTH validation. Consider .minLength(n) and/or .maxLength(n).";
            case FORMAT -> "Reading '" + label + "' as " + readerType
                    + ": no FORMAT validation. Consider .email(), .phone(), .url(), .regex(...), .oneOf(...),"
                    + " or a domain-specific reader.";
            case CUSTOM -> "Reading '" + label + "' as " + readerType
                    + ": no CUSTOM validation. Consider .custom(predicate, message).";
        };
    }
}
