package chirp.internal;

import chirp.config.Config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emits {@code [STRICT]} warnings on stderr when a fluent reader or an
 * immediate input method is used without the validation rules that
 * would normally be expected.
 *
 * <p><b>Internal API.</b> Do not use directly. This class is the
 * mechanism behind the familiar output a student sees:</p>
 * <pre>{@code
 * [STRICT] Reading 'Age' as int: no RANGE validation. Consider
 * .between(min, max), .positive(), .min(n)/.max(n), or a typed
 * reader like readInt(label, min, max).
 * }</pre>
 *
 * <h2>What it's for</h2>
 * <p>Strict mode is a teaching aid. A beginner who writes
 * {@code Input.readInt("Age")} without a range check is likely to ship
 * a bug — the reader accepts {@code -5} for age. Strict mode emits one
 * hint the first time the student uses the reader, pointing at the
 * fluent methods that would fix it.</p>
 *
 * <p>The hint is not an error. The program runs correctly. The hint is
 * a nudge, emitted once per {@code (type, label)} combination so it
 * never spams the console.</p>
 *
 * <h2>How to silence it</h2>
 * <ul>
 *   <li>Apply a range check — {@code .between(min, max)},
 *       {@code .positive()}, etc. The category becomes "covered" and
 *       the warning does not fire.</li>
 *   <li>Skip the specific category — {@code .skipRangeWarning()},
 *       {@code .skipFormatWarning()}, etc. For cases where the check
 *       genuinely does not apply.</li>
 *   <li>Mark the reader as lenient — {@code .lenient()} silences all
 *       strict warnings for that reader.</li>
 *   <li>Disable strict mode globally —
 *       {@link chirp.config.Config#disableStrictWarnings()} or
 *       {@code -Dchirp.strict=false}.</li>
 * </ul>
 *
 * <h2>Two methods: emit vs warnOnce</h2>
 * <ul>
 *   <li>{@link #emit(String, String, ValidationCategory)} — fires
 *       <b>every</b> time. Used by fluent readers, which manage their
 *       own once-per-instance throttle via an internal
 *       {@code warnedOnce} flag.</li>
 *   <li>{@link #warnOnce(String, String, ValidationCategory...)} —
 *       fires once per {@code (type, label)} combination. Used by
 *       immediate methods, which have no persistent instance.</li>
 * </ul>
 * <p>Both are no-ops when {@link Config#isStrictWarningsEnabled()}
 * returns false.</p>
 *
 * <h2>Throttle reset</h2>
 * <p>{@link #reset()} clears the {@code warnOnce} memory. Called by
 * {@link Config#reset()} and by tests that need to verify warning
 * behavior across multiple runs.</p>
 *
 * @see Config#isStrictWarningsEnabled()
 * @see Config#disableStrictWarnings()
 * @see chirp.console.input.StringReader
 * @see chirp.console.input.IntReader
 * @since 1.0.0
 */
public final class Strict {

    private static final String PREFIX = "[STRICT] ";
    private static final Set<String> WARNED_ONCE = ConcurrentHashMap.newKeySet();

    private Strict() {}

    /**
     * Clears the once-per-prompt throttle. Called by
     * {@link Config#reset()}.
     *
     * @since 1.0.0
     */
    public static void reset() {
        WARNED_ONCE.clear();
    }

    /**
     * Emits one warning per category the first time it is called with
     * a given {@code (type, label)} combination. Subsequent calls with
     * the same combination are silent until {@link #reset()}.
     *
     * <p>Used by immediate methods (e.g. {@code Input.readInt(prompt)})
     * which have no persistent instance to hold a {@code warnedOnce}
     * flag.</p>
     *
     * @param type the reader type — "int", "double", "string", "date",
     *             "time", "datetime"
     * @param label the field label (part of the throttle key)
     * @param categories the categories to warn about
     * @since 1.0.0
     */
    public static void warnOnce(String type, String label, ValidationCategory... categories) {
        if (!Config.isStrictWarningsEnabled()) return;
        if (!WARNED_ONCE.add(type + ":" + label)) return;
        for (ValidationCategory cat : categories) {
            emit(type, label, cat);
        }
    }

    /**
     * Emits a warning <b>immediately</b>, without any throttle. The
     * caller is responsible for ensuring it does not fire too often.
     *
     * <p>Used by fluent readers, which track their own per-instance
     * {@code warnedOnce} flag so multiple {@code .read()} calls on the
     * same reader produce one warning, not many.</p>
     *
     * @param readerType the reader type
     * @param label the field label
     * @param category the category to warn about
     * @since 1.0.0
     */
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
