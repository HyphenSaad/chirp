package chirp.ui;

import chirp.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Declarative form builder — turn a list of named {@link Field}s into a
 * submit-and-validate form.
 *
 * <p>Instead of wiring "on submit, validate each field, collect the
 * values, show errors, clear if valid" by hand, declare the fields and
 * the callbacks; {@code FormBuilder} does the plumbing.</p>
 *
 * <h2>The pattern</h2>
 * <pre>{@code
 * FormBuilder form = FormBuilder.create("Register")
 *     .field("name",  Field.of("Name").required().minLength(3))
 *     .field("email", Field.of("Email").email())
 *     .field("age",   Field.of("Age").numeric())
 *     .submit("Register", values -> {
 *         // runs only when every field passes validation
 *         Console.success("Welcome " + values.get("name"));
 *     })
 *     .reset("Clear", () -> Console.info("Cleared"));
 *
 * Stack root = form.build();
 * Ui.show("Registration", root);
 * }</pre>
 *
 * <h2>What "submit" does</h2>
 * <p>When the submit button is clicked:</p>
 * <ol>
 *   <li>Every field's validator runs. Fields that fail get the
 *       {@code invalid} CSS class applied to their input.</li>
 *   <li>If any field failed, a red summary label appears at the top of
 *       the form: "Please fix the highlighted fields above."</li>
 *   <li>If every field passed, the summary is hidden and the
 *       {@code onSubmit} callback fires with the values as a
 *       {@code Map<String, String>}.</li>
 * </ol>
 *
 * <h2>Two ways to check without submitting</h2>
 * <p>Both are useful for "Save" buttons that do additional work, or for
 * showing a live validity indicator:</p>
 * <ul>
 *   <li>{@link #values()} — the field values as a map, no validation</li>
 *   <li>{@link #validate()} — a {@link ValidationResult} of every
 *       field's current state</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <p>For the field composite that this builder consumes, see
 * {@link Field}. For the declarative validation that fields use, see
 * {@link chirp.validation.Validate}. For the type-safe error rendering,
 * see {@link UiIntegration#errorsFrom(ValidationResult)}.</p>
 *
 * @see Field
 * @see UiIntegration
 * @see chirp.validation.Validate
 * @see chirp.validation.ValidationResult
 * @since 1.0.0
 */
public final class FormBuilder {

    private final String title;
    private final List<NamedField> fields = new ArrayList<>();

    private String submitLabel = "Submit";
    private String resetLabel = null;
    private Consumer<java.util.Map<String, String>> onSubmit;
    private Runnable onReset;

    /**
     * The summary label rendered at the top of the form when validation
     * fails. Always a valid {@code Label} instance — the field-level
     * initializer guarantees the handlers can never NPE, and
     * {@link #build()} reassigns it for each build so the same builder
     * can produce multiple forms without sharing state.
     */
    private Label summaryLabel = Label.error("");

    private record NamedField(String name, Field field) {}

    private FormBuilder(String title) {
        this.title = title == null ? "" : title;
    }

    // ============================================================== factories

    /**
     * Creates a new form with the given title. The title appears as a
     * heading at the top of the built form.
     *
     * <pre>{@code
     * FormBuilder form = FormBuilder.create("Register");
     * }</pre>
     *
     * @param title the form title (null → no title heading)
     * @return a new builder
     * @see #of(String)
     * @see #form(String)
     * @see #make(String)
     * @since 1.0.0
     */
    public static FormBuilder create(String title) { return new FormBuilder(title); }

    /**
     * Alias of {@link #create(String)} — see that method for full
     * documentation and examples.
     *
     * @param title the form title
     * @return a new builder
     * @see #create(String)
     * @since 1.0.0
     */
    public static FormBuilder of(String title) { return new FormBuilder(title); }

    /**
     * Alias of {@link #create(String)} — see that method for full
     * documentation and examples.
     *
     * @param title the form title
     * @return a new builder
     * @see #create(String)
     * @since 1.0.0
     */
    public static FormBuilder form(String title) { return new FormBuilder(title); }

    /**
     * Alias of {@link #create(String)} — see that method for full
     * documentation and examples.
     *
     * @param title the form title
     * @return a new builder
     * @see #create(String)
     * @since 1.0.0
     */
    public static FormBuilder make(String title) { return new FormBuilder(title); }

    // ============================================================== fields

    /**
     * Adds a named field to the form. The name is the key that will
     * appear in the values map passed to the submit callback.
     *
     * <p>Null arguments are silently ignored — this lets a form
     * conditionally include fields without an if-statement:</p>
     * <pre>{@code
     * form.field("email", Field.of("Email").email())
     *     .field(isAdmin ? "adminCode" : null,
     *            isAdmin ? Field.of("Admin Code").required() : null);
     * }</pre>
     *
     * @param name the field name (null → no-op)
     * @param field the field composite (null → no-op)
     * @return this builder, for chaining
     * @see #addField(String, Field)
     * @see #add(String, Field)
     * @since 1.0.0
     */
    public FormBuilder field(String name, Field field) {
        if (name != null && field != null) fields.add(new NamedField(name, field));
        return this;
    }

    /**
     * Alias of {@link #field(String, Field)} — see that method for full
     * documentation and examples.
     *
     * @param name the field name
     * @param field the field composite
     * @return this builder
     * @see #field(String, Field)
     * @since 1.0.0
     */
    public FormBuilder addField(String name, Field field) { return field(name, field); }

    /**
     * Alias of {@link #field(String, Field)} — see that method for full
     * documentation and examples.
     *
     * @param name the field name
     * @param field the field composite
     * @return this builder
     * @see #field(String, Field)
     * @since 1.0.0
     */
    public FormBuilder add(String name, Field field) { return field(name, field); }

    // ============================================================== submit / reset

    /**
     * Sets the submit button label and its callback. The callback
     * receives a {@code Map} from field name → current value, and fires
     * only when every field passes validation.
     *
     * @param label the submit button label (null → "Submit")
     * @param onSubmit the callback (null → no-op after validation)
     * @return this builder, for chaining
     * @see #onSubmit(String, Consumer)
     * @see #withSubmit(String, Consumer)
     * @see #action(String, Consumer)
     * @since 1.0.0
     */
    public FormBuilder submit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        if (label != null) this.submitLabel = label;
        this.onSubmit = onSubmit;
        return this;
    }

    /**
     * Alias of {@link #submit(String, Consumer)} — see that method for
     * full documentation and examples.
     *
     * @param label the submit button label
     * @param onSubmit the callback
     * @return this builder
     * @see #submit(String, Consumer)
     * @since 1.0.0
     */
    public FormBuilder onSubmit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }

    /**
     * Alias of {@link #submit(String, Consumer)} — see that method for
     * full documentation and examples.
     *
     * @param label the submit button label
     * @param onSubmit the callback
     * @return this builder
     * @see #submit(String, Consumer)
     * @since 1.0.0
     */
    public FormBuilder withSubmit(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }

    /**
     * Alias of {@link #submit(String, Consumer)} — see that method for
     * full documentation and examples.
     *
     * @param label the submit button label
     * @param onSubmit the callback
     * @return this builder
     * @see #submit(String, Consumer)
     * @since 1.0.0
     */
    public FormBuilder action(String label, Consumer<java.util.Map<String, String>> onSubmit) {
        return submit(label, onSubmit);
    }

    /**
     * Adds a reset button that clears every field and hides any
     * validation state. Not required — a form with no reset button
     * works fine.
     *
     * @param label the reset button label (null → no reset button)
     * @param onReset additional cleanup to run after clearing (may be null)
     * @return this builder, for chaining
     * @see #onReset(String, Runnable)
     * @see #withReset(String, Runnable)
     * @since 1.0.0
     */
    public FormBuilder reset(String label, Runnable onReset) {
        if (label != null) this.resetLabel = label;
        this.onReset = onReset;
        return this;
    }

    /**
     * Alias of {@link #reset(String, Runnable)} — see that method for
     * full documentation.
     *
     * @param label the reset button label
     * @param onReset the reset callback
     * @return this builder
     * @see #reset(String, Runnable)
     * @since 1.0.0
     */
    public FormBuilder onReset(String label, Runnable onReset) { return reset(label, onReset); }

    /**
     * Alias of {@link #reset(String, Runnable)} — see that method for
     * full documentation.
     *
     * @param label the reset button label
     * @param onReset the reset callback
     * @return this builder
     * @see #reset(String, Runnable)
     * @since 1.0.0
     */
    public FormBuilder withReset(String label, Runnable onReset) { return reset(label, onReset); }

    // ============================================================== build

    /**
     * Builds the form as a {@link Stack} — a vertical layout with the
     * title, summary label, fields, and buttons.
     *
     * <p>Calling {@code build()} multiple times on the same builder
     * produces multiple independent forms. Each call re-initialises the
     * summary label so the forms do not share validation state.</p>
     *
     * @return the built form
     * @see #render()
     * @see #root()
     * @see #asStack()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #build()} — see that method for full documentation
     * and examples.
     *
     * @return the built form
     * @see #build()
     * @since 1.0.0
     */
    public Stack render() { return build(); }

    /**
     * Alias of {@link #build()} — see that method for full documentation
     * and examples.
     *
     * @return the built form
     * @see #build()
     * @since 1.0.0
     */
    public Stack root() { return build(); }

    /**
     * Alias of {@link #build()} — see that method for full documentation
     * and examples.
     *
     * @return the built form
     * @see #build()
     * @since 1.0.0
     */
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

    /**
     * Returns the current value of every field as an ordered map, without
     * running validation.
     *
     * <p>Useful when the caller needs the values for purposes other than
     * submitting — e.g. for a "Save Draft" button that stores partially
     * valid data:</p>
     * <pre>{@code
     * Button saveDraft = Button.outline("Save Draft")
     *     .onClick(() -> saveDraft(form.values()));
     * }</pre>
     *
     * @return the field values by name
     * @see #validate()
     * @since 1.0.0
     */
    public java.util.Map<String, String> values() {
        java.util.Map<String, String> out = new java.util.LinkedHashMap<>();
        for (NamedField nf : fields) out.put(nf.name(), nf.field.value());
        return out;
    }

    /**
     * Validates every field and returns the aggregate result, without
     * submitting. Useful for a live validity indicator, or for a caller
     * that wants to do its own error display:
     *
     * <pre>{@code
     * ValidationResult result = form.validate();
     * if (result.hasErrors()) {
     *     root.add(UiIntegration.errorsFrom(result));
     * }
     * }</pre>
     *
     * @return the aggregate validation result
     * @see #values()
     * @see UiIntegration#errorsFrom(ValidationResult)
     * @since 1.0.0
     */
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
