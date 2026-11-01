package chirp.ui;

import chirp.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Declarative form builder — turn a list of fields into a submit-and-validate
 * form. Bridges the UI module with the input/validation modules so students
 * never need to wire "for each field, check … then show error … then clear …"
 * by hand.
 *
 * <pre>{@code
 * FormBuilder form = FormBuilder.create("Register")
 *     .field("name",   Field.of("Name").required().minLength(3))
 *     .field("email",  Field.of("Email").email())
 *     .field("age",    Field.of("Age").numeric())
 *     .submit("Register", values -> Console.success("Welcome " + values.get("name")))
 *     .reset("Clear");
 *
 * Stack root = form.build();
 * }</pre>
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code create / of / form / make}. Field: {@code field / addField /
 * add}. Submit: {@code submit / onSubmit / withSubmit / action}. Reset:
 * {@code reset / onReset / withReset}. Build: {@code build / render / root / asStack}.</p>
 *
 * <h2>Validation flow</h2>
 * <p>On submit, every field is validated (via its own validator) and any
 * that fail get the {@code invalid} CSS class. The callback fires only when
 * all fields pass. A summary error label appears at the top of the form
 * when validation fails.</p>
 */
public final class FormBuilder {

    private final String title;
    private final List<NamedField> fields = new ArrayList<>();

    private String submitLabel = "Submit";
    private String resetLabel = null;
    private Consumer<java.util.Map<String, String>> onSubmit;
    private Runnable onReset;

    /**
     * Always a valid Label instance. Reassigned by {@link #build()} on every
     * call so that "build once, add to a Scene, build again, add to another
     * Scene" works. The field-level initializer guarantees that
     * {@link #handleSubmit()} and {@link #handleReset()} cannot NPE, even if
     * a caller somehow manages to fire a handler before {@code build()} has
     * produced its first root.
     */
    private Label summaryLabel = Label.error("");

    private record NamedField(String name, Field field) {}

    private FormBuilder(String title) {
        this.title = title == null ? "" : title;
    }

    // ============================================================== factories

    public static FormBuilder create(String title) { return new FormBuilder(title); }
    public static FormBuilder of(String title) { return new FormBuilder(title); }
    public static FormBuilder form(String title) { return new FormBuilder(title); }
    public static FormBuilder make(String title) { return new FormBuilder(title); }

    // ============================================================== fields

    public FormBuilder field(String name, Field field) {
        if (name != null && field != null) fields.add(new NamedField(name, field));
        return this;
    }
    public FormBuilder addField(String name, Field field) { return field(name, field); }
    public FormBuilder add(String name, Field field) { return field(name, field); }

    // ============================================================== submit / reset

    public FormBuilder submit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        if (label != null) this.submitLabel = label;
        this.onSubmit = onSubmit;
        return this;
    }
    public FormBuilder onSubmit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }
    public FormBuilder withSubmit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }
    public FormBuilder action(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }

    public FormBuilder reset(String label, Runnable onReset) {
        if (label != null) this.resetLabel = label;
        this.onReset = onReset;
        return this;
    }
    public FormBuilder onReset(String label, Runnable onReset) { return reset(label, onReset); }
    public FormBuilder withReset(String label, Runnable onReset) { return reset(label, onReset); }

    // ============================================================== build

    public Stack build() {
        Stack root = Stack.v(14).padding(20);

        if (!title.isBlank()) root.add(Label.heading(title));

        summaryLabel = Label.error("");
        summaryLabel.setVisible(false);
        summaryLabel.setManaged(false);
        summaryLabel.wrap();
        root.add(summaryLabel);

        for (NamedField nf : fields) root.add(nf.field);

        Row buttons = Row.h(10);
        Button submitBtn = Button.primary(submitLabel).onClick(this::handleSubmit);
        buttons.add(submitBtn);

        if (resetLabel != null) {
            Button resetBtn = Button.outline(resetLabel).onClick(this::handleReset);
            buttons.add(resetBtn);
        }
        root.add(buttons);

        return root;
    }
    public Stack render() { return build(); }
    public Stack root() { return build(); }
    public Stack asStack() { return build(); }

    // ============================================================== internal

    private void handleSubmit() {
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        boolean allValid = true;

        for (NamedField nf : fields) {
            nf.field.input().getStyleClass().remove("invalid");
            String v = nf.field.value();
            values.put(nf.name(), v);
            if (!nf.field.isValid()) {
                nf.field.input().getStyleClass().add("invalid");
                allValid = false;
            }
        }

        if (!allValid) {
            summaryLabel.setText("Please fix the highlighted fields above.");
            summaryLabel.setVisible(true);
            summaryLabel.setManaged(true);
            return;
        }

        summaryLabel.setVisible(false);
        summaryLabel.setManaged(false);

        if (onSubmit != null) onSubmit.accept(values);
    }

    private void handleReset() {
        for (NamedField nf : fields) nf.field.value("");
        summaryLabel.setVisible(false);
        summaryLabel.setManaged(false);
        if (onReset != null) onReset.run();
    }

    // ============================================================== extras

    /** Returns all field values as an ordered map (no validation). */
    public java.util.Map<String, String> values() {
        java.util.Map<String, String> out = new java.util.LinkedHashMap<>();
        for (NamedField nf : fields) out.put(nf.name(), nf.field.value());
        return out;
    }

    /** Validates every field without submitting — returns the result. */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        for (NamedField nf : fields) {
            if (!nf.field.isValid()) {
                String msg = nf.field.error();
                errors.add(msg == null ? (nf.name() + ": invalid") : (nf.name() + ": " + msg));
            }
        }
        return new ValidationResult(errors);
    }
}
