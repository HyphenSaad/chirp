package chirp.console.input;

import chirp.console.Input;
import chirp.console.Messenger;
import chirp.internal.Trace;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fluent boolean reader. Uses {@link Input#readRaw(String)} internally.
 */
public final class BooleanReader {

    private String label = "Continue";
    private Boolean defaultValue;
    private String helpText;
    private Predicate<Boolean> customValidator;
    private String customError;
    private boolean lenient = false;

    private static final Set<String> TRUTHY = Set.of("y", "yes", "true", "1", "yeah", "yep", "ok");
    private static final Set<String> FALSY = Set.of("n", "no", "false", "0", "nah", "nope");

    private BooleanReader() {}
    private BooleanReader(String label) { if (label != null) this.label = label; }

    public static BooleanReader create() { return new BooleanReader(); }
    public static BooleanReader of() { return new BooleanReader(); }
    public static BooleanReader ask() { return new BooleanReader(); }
    public static BooleanReader prompt() { return new BooleanReader(); }
    public static BooleanReader prompt(String label) { return new BooleanReader(label); }

    public BooleanReader label(String label) {
        if (label != null) this.label = label;
        Trace.value("reader", "label", label);
        return this;
    }
    public BooleanReader message(String label) { return label(label); }
    public BooleanReader question(String label) { return label(label); }
    public BooleanReader withLabel(String label) { return label(label); }

    public BooleanReader defaultValue(boolean value) {
        this.defaultValue = value;
        Trace.value("reader", "defaultValue", value);
        return this;
    }
    public BooleanReader defaulting(boolean value) { return defaultValue(value); }
    public BooleanReader orElse(boolean value) { return defaultValue(value); }

    public BooleanReader help(String text) { this.helpText = text; return this; }

    public BooleanReader custom(Predicate<Boolean> validator, String error) {
        this.customValidator = validator;
        this.customError = error;
        Trace.debug("reader", "configure: custom validator");
        return this;
    }

    public BooleanReader lenient() { this.lenient = true; return this; }
    public BooleanReader noStrict() { return lenient(); }

    public boolean read() {
        Trace.enter("reader", "BooleanReader.read", "label=" + label);
        if (helpText != null) System.out.println(helpText);

        while (true) {
            String raw = Input.readRaw(label + " (y/n)").toLowerCase(Locale.ROOT);
            Trace.value("reader", "raw input", raw);

            if (raw.isEmpty() && defaultValue != null) {
                Trace.debug("reader", "using default value: {}", defaultValue);
                Trace.exit("reader", "BooleanReader.read", defaultValue);
                return defaultValue;
            }

            Boolean value = null;
            if (TRUTHY.contains(raw)) value = Boolean.TRUE;
            else if (FALSY.contains(raw)) value = Boolean.FALSE;

            if (value == null) {
                Trace.validation("reader", "boolean parse", false, "not y/yes/true/1 or n/no/false/0");
                Messenger.error("Please answer y or n.");
                continue;
            }
            if (customValidator != null && !customValidator.test(value)) {
                Trace.validation("reader", "custom", false, customError);
                Messenger.error(customError != null ? customError : "Invalid answer.");
                continue;
            }
            Trace.exit("reader", "BooleanReader.read", value);
            return value;
        }
    }

    public boolean get() { return read(); }
    public boolean value() { return read(); }
    public boolean parse() { return read(); }
}
