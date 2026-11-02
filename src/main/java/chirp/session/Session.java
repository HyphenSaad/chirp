package chirp.session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The current student's profile — name, roll number, batch, degree,
 * section, and email.
 *
 * <p>A singleton. Configure once at the top of {@code main()}, then
 * render in screenshots or via {@link chirp.console.Console#watermark()}
 * at the end of a run. All fields are optional — set only the ones you
 * want to appear.</p>
 *
 * <h2>The pattern</h2>
 * <pre>{@code
 * public static void main(String[] args) {
 *     Chirp.start();
 *
 *     Session.me()
 *         .name("Saad Mansoor")
 *         .rollNumber("2024-CS-001")
 *         .batch("2024")
 *         .degree("BSAI")
 *         .section("A")
 *         .email("saad@example.com");
 *
 *     // ... program logic ...
 *
 *     Console.watermark();   // prints the session info, ready for a screenshot
 * }
 * }</pre>
 *
 * <h2>What appears where</h2>
 * <ul>
 *   <li>{@link chirp.console.Console#watermark()} — prints the full
 *       session as a dim block at the end of output</li>
 *   <li>{@link chirp.format.Format#fmtWithSession(String)} —
 *       interpolates {@code {name}}, {@code {roll}}, {@code {batch}},
 *       {@code {degree}}, {@code {section}}, {@code {email}} into a
 *       template</li>
 *   <li>{@link chirp.mail.EmailRequest#signatureFromSession()} — builds
 *       an email signature from the session</li>
 * </ul>
 *
 * <h2>Singleton access</h2>
 * <p>{@link #me()} is the canonical accessor. Several aliases exist
 * ({@link #get()}, {@link #of()}, {@link #current()}, {@link #configure()},
 * {@link #instance()}) because different students reach for different
 * names. They all return the same instance.</p>
 *
 * <h2>Completeness</h2>
 * <p>{@link #isComplete()} reports whether the three <b>required</b>
 * fields — name, roll number, and degree — are set. Batch, section,
 * and email remain optional. Use it at the start of a demo to check the
 * student has filled in their identity before a report needs it:</p>
 *
 * <pre>{@code
 * if (!Session.me().isComplete()) {
 *     Console.warn("Set your Session fields: "
 *         + String.join(", ", Session.me().missingFields()));
 * }
 * }</pre>
 *
 * <h2>Clearing</h2>
 * <p>{@link #clear()} resets every field to null. Useful when running
 * multiple independent sessions in the same JVM (e.g. in tests).</p>
 *
 * <h2>Related</h2>
 * <p>For output helpers that consume the session, see
 * {@link chirp.console.Console#watermark()} and
 * {@link chirp.format.Format#fmtWithSession(String)}.</p>
 *
 * @see #isComplete()
 * @see #missingFields()
 * @see chirp.console.Console#watermark()
 * @see chirp.format.Format#fmtWithSession(String)
 * @since 1.0.0
 */
public final class Session {

    private static final Session INSTANCE = new Session();

    private String name;
    private String rollNumber;
    private String batch;
    private String degree;
    private String section;
    private String email;

    private Session() {}

    /**
     * Returns the singleton session instance.
     *
     * <p>Aliases: {@link #get()}, {@link #of()}, {@link #current()},
     * {@link #configure()}, {@link #instance()}.</p>
     *
     * @return the singleton instance
     * @see #get()
     * @since 1.0.0
     */
    public static Session me() { return INSTANCE; }

    /**
     * Alias of {@link #me()} — see that method for full documentation
     * and examples.
     *
     * @return the singleton instance
     * @see #me()
     * @since 1.0.0
     */
    public static Session get() { return INSTANCE; }

    /**
     * Alias of {@link #me()} — see that method for full documentation
     * and examples.
     *
     * @return the singleton instance
     * @see #me()
     * @since 1.0.0
     */
    public static Session of() { return INSTANCE; }

    /**
     * Alias of {@link #me()} — see that method for full documentation
     * and examples.
     *
     * @return the singleton instance
     * @see #me()
     * @since 1.0.0
     */
    public static Session current() { return INSTANCE; }

    /**
     * Alias of {@link #me()} — see that method for full documentation
     * and examples.
     *
     * @return the singleton instance
     * @see #me()
     * @since 1.0.0
     */
    public static Session configure() { return INSTANCE; }

    /**
     * Alias of {@link #me()} — see that method for full documentation
     * and examples.
     *
     * @return the singleton instance
     * @see #me()
     * @since 1.0.0
     */
    public static Session instance() { return INSTANCE; }

    /**
     * Sets the student's full name.
     *
     * @param value the name (may be null to clear)
     * @return this session, for chaining
     * @see #fullName(String)
     * @see #studentName(String)
     * @see #name()
     * @since 1.0.0
     */
    public Session name(String value) { this.name = value; return this; }

    /**
     * Alias of {@link #name(String)} — see that method for full
     * documentation.
     *
     * @param value the full name
     * @return this session
     * @see #name(String)
     * @since 1.0.0
     */
    public Session fullName(String value) { return name(value); }

    /**
     * Alias of {@link #name(String)} — see that method for full
     * documentation.
     *
     * @param value the full name
     * @return this session
     * @see #name(String)
     * @since 1.0.0
     */
    public Session studentName(String value) { return name(value); }

    /**
     * Returns the student's name, or null if not set.
     *
     * @return the name, or null
     * @see #name(String)
     * @since 1.0.0
     */
    public String name() { return name; }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the name, or null
     * @see #name()
     * @since 1.0.0
     */
    public String getName() { return name; }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the name, or null
     * @see #name()
     * @since 1.0.0
     */
    public String fullName() { return name; }

    /**
     * Alias of {@link #name()} — see that method for full documentation.
     *
     * @return the name, or null
     * @see #name()
     * @since 1.0.0
     */
    public String studentName() { return name; }

    /**
     * Sets the student's roll number / registration number.
     *
     * @param value the roll number (may be null to clear)
     * @return this session, for chaining
     * @see #roll(String)
     * @see #rollNo(String)
     * @see #registrationNumber(String)
     * @see #regNo(String)
     * @see #rollNumber()
     * @since 1.0.0
     */
    public Session rollNumber(String value) { this.rollNumber = value; return this; }

    /**
     * Alias of {@link #rollNumber(String)} — see that method for full
     * documentation.
     *
     * @param value the roll number
     * @return this session
     * @see #rollNumber(String)
     * @since 1.0.0
     */
    public Session roll(String value) { return rollNumber(value); }

    /**
     * Alias of {@link #rollNumber(String)} — see that method for full
     * documentation.
     *
     * @param value the roll number
     * @return this session
     * @see #rollNumber(String)
     * @since 1.0.0
     */
    public Session rollNo(String value) { return rollNumber(value); }

    /**
     * Alias of {@link #rollNumber(String)} — see that method for full
     * documentation.
     *
     * @param value the roll number
     * @return this session
     * @see #rollNumber(String)
     * @since 1.0.0
     */
    public Session registrationNumber(String value) { return rollNumber(value); }

    /**
     * Alias of {@link #rollNumber(String)} — see that method for full
     * documentation.
     *
     * @param value the roll number
     * @return this session
     * @see #rollNumber(String)
     * @since 1.0.0
     */
    public Session regNo(String value) { return rollNumber(value); }

    /**
     * Returns the roll number, or null if not set.
     *
     * @return the roll number, or null
     * @see #rollNumber(String)
     * @since 1.0.0
     */
    public String rollNumber() { return rollNumber; }

    /**
     * Alias of {@link #rollNumber()} — see that method for full
     * documentation.
     *
     * @return the roll number, or null
     * @see #rollNumber()
     * @since 1.0.0
     */
    public String getRollNumber() { return rollNumber; }

    /**
     * Alias of {@link #rollNumber()} — see that method for full
     * documentation.
     *
     * @return the roll number, or null
     * @see #rollNumber()
     * @since 1.0.0
     */
    public String roll() { return rollNumber; }

    /**
     * Alias of {@link #rollNumber()} — see that method for full
     * documentation.
     *
     * @return the roll number, or null
     * @see #rollNumber()
     * @since 1.0.0
     */
    public String rollNo() { return rollNumber; }

    /**
     * Sets the student's batch / admission year.
     *
     * @param value the batch (may be null to clear)
     * @return this session, for chaining
     * @see #batchNumber(String)
     * @see #batch()
     * @since 1.0.0
     */
    public Session batch(String value) { this.batch = value; return this; }

    /**
     * Alias of {@link #batch(String)} — see that method for full
     * documentation.
     *
     * @param value the batch
     * @return this session
     * @see #batch(String)
     * @since 1.0.0
     */
    public Session batchNumber(String value) { return batch(value); }

    /**
     * Returns the batch, or null if not set.
     *
     * @return the batch, or null
     * @see #batch(String)
     * @since 1.0.0
     */
    public String batch() { return batch; }

    /**
     * Alias of {@link #batch()} — see that method for full
     * documentation.
     *
     * @return the batch, or null
     * @see #batch()
     * @since 1.0.0
     */
    public String getBatch() { return batch; }

    /**
     * Alias of {@link #batch()} — see that method for full
     * documentation.
     *
     * @return the batch, or null
     * @see #batch()
     * @since 1.0.0
     */
    public String batchNumber() { return batch; }

    /**
     * Sets the student's degree / program code.
     *
     * @param value the degree (may be null to clear)
     * @return this session, for chaining
     * @see #degreeCode(String)
     * @see #program(String)
     * @see #degree()
     * @since 1.0.0
     */
    public Session degree(String value) { this.degree = value; return this; }

    /**
     * Alias of {@link #degree(String)} — see that method for full
     * documentation.
     *
     * @param value the degree
     * @return this session
     * @see #degree(String)
     * @since 1.0.0
     */
    public Session degreeCode(String value) { return degree(value); }

    /**
     * Alias of {@link #degree(String)} — see that method for full
     * documentation.
     *
     * @param value the degree
     * @return this session
     * @see #degree(String)
     * @since 1.0.0
     */
    public Session program(String value) { return degree(value); }

    /**
     * Returns the degree, or null if not set.
     *
     * @return the degree, or null
     * @see #degree(String)
     * @since 1.0.0
     */
    public String degree() { return degree; }

    /**
     * Alias of {@link #degree()} — see that method for full
     * documentation.
     *
     * @return the degree, or null
     * @see #degree()
     * @since 1.0.0
     */
    public String getDegree() { return degree; }

    /**
     * Alias of {@link #degree()} — see that method for full
     * documentation.
     *
     * @return the degree, or null
     * @see #degree()
     * @since 1.0.0
     */
    public String degreeCode() { return degree; }

    /**
     * Alias of {@link #degree()} — see that method for full
     * documentation.
     *
     * @return the degree, or null
     * @see #degree()
     * @since 1.0.0
     */
    public String program() { return degree; }

    /**
     * Sets the student's section.
     *
     * @param value the section (may be null to clear)
     * @return this session, for chaining
     * @see #classSection(String)
     * @see #section()
     * @since 1.0.0
     */
    public Session section(String value) { this.section = value; return this; }

    /**
     * Alias of {@link #section(String)} — see that method for full
     * documentation.
     *
     * @param value the section
     * @return this session
     * @see #section(String)
     * @since 1.0.0
     */
    public Session classSection(String value) { return section(value); }

    /**
     * Returns the section, or null if not set.
     *
     * @return the section, or null
     * @see #section(String)
     * @since 1.0.0
     */
    public String section() { return section; }

    /**
     * Alias of {@link #section()} — see that method for full
     * documentation.
     *
     * @return the section, or null
     * @see #section()
     * @since 1.0.0
     */
    public String getSection() { return section; }

    /**
     * Alias of {@link #section()} — see that method for full
     * documentation.
     *
     * @return the section, or null
     * @see #section()
     * @since 1.0.0
     */
    public String classSection() { return section; }

    /**
     * Sets the student's email address.
     *
     * @param value the email (may be null to clear)
     * @return this session, for chaining
     * @see #emailAddress(String)
     * @see #email()
     * @since 1.0.0
     */
    public Session email(String value) { this.email = value; return this; }

    /**
     * Alias of {@link #email(String)} — see that method for full
     * documentation.
     *
     * @param value the email
     * @return this session
     * @see #email(String)
     * @since 1.0.0
     */
    public Session emailAddress(String value) { return email(value); }

    /**
     * Returns the email address, or null if not set.
     *
     * @return the email, or null
     * @see #email(String)
     * @since 1.0.0
     */
    public String email() { return email; }

    /**
     * Alias of {@link #email()} — see that method for full
     * documentation.
     *
     * @return the email, or null
     * @see #email()
     * @since 1.0.0
     */
    public String getEmail() { return email; }

    /**
     * Alias of {@link #email()} — see that method for full
     * documentation.
     *
     * @return the email, or null
     * @see #email()
     * @since 1.0.0
     */
    public String emailAddress() { return email; }

    /**
     * Returns a multi-line summary of the session, formatted as
     * {@code "Field: value"} per line. Only populated fields are
     * included — an empty session returns an empty string.
     *
     * <p>This is what {@link chirp.console.Console#watermark()} renders
     * — dim, with a horizontal separator above and below.</p>
     *
     * <pre>{@code
     * Session.me().name("Saad").rollNumber("2024-CS-001");
     * System.out.println(Session.me().summary());
     * // Name: Saad
     * // Roll: 2024-CS-001
     * }</pre>
     *
     * @return the summary string
     * @see #asMap()
     * @see #print()
     * @see chirp.console.Console#watermark()
     * @since 1.0.0
     */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, "Name", name);
        appendIfPresent(sb, "Roll", rollNumber);
        appendIfPresent(sb, "Batch", batch);
        appendIfPresent(sb, "Degree", degree);
        appendIfPresent(sb, "Section", section);
        appendIfPresent(sb, "Email", email);
        return sb.toString();
    }

    /**
     * Prints the summary to {@code System.out}. Convenience for
     * {@code System.out.print(summary())}.
     *
     * @see #summary()
     * @since 1.0.0
     */
    public void print() { System.out.print(summary()); }

    /**
     * Returns the session as a map of field name → value. Only populated
     * fields are included.
     *
     * <pre>{@code
     * Session.me().name("Saad").rollNumber("2024-CS-001");
     * Map<String, String> fields = Session.me().asMap();
     * // {name=Saad, rollNumber=2024-CS-001}
     * }</pre>
     *
     * @return the fields as a map
     * @see #summary()
     * @since 1.0.0
     */
    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        putIfPresent(map, "name", name);
        putIfPresent(map, "rollNumber", rollNumber);
        putIfPresent(map, "batch", batch);
        putIfPresent(map, "degree", degree);
        putIfPresent(map, "section", section);
        putIfPresent(map, "email", email);
        return map;
    }

    /**
     * Resets every field to null. Useful when running multiple
     * independent sessions in the same JVM (e.g. in tests).
     *
     * @see #summary()
     * @see #asMap()
     * @since 1.0.0
     */
    public void clear() {
        name = null;
        rollNumber = null;
        batch = null;
        degree = null;
        section = null;
        email = null;
    }

    // ============================================================ completeness

    /**
     * Checks whether the required fields are set.
     *
     * <p>A session is <b>complete</b> when <b>name</b>, <b>roll number</b>,
     * and <b>degree</b> are all non-null and non-blank. Batch, section,
     * and email are optional and do not affect completeness.</p>
     *
     * <p>Use this at the start of a demo to detect a session that has not
     * yet been filled in. {@link #missingFields()} tells you exactly
     * which fields the user still has to set.</p>
     *
     * <pre>{@code
     * Session.me().name("Saad").rollNumber("2024-CS-001").degree("BSAI");
     * if (Session.me().isComplete()) {
     *     Console.success("Ready — your details will be in the report.");
     * } else {
     *     Console.warn("Set: " + Session.me().missingFields());
     * }
     * }</pre>
     *
     * @return {@code true} when name, roll number, and degree are all set;
     * {@code false} otherwise (including when any of them is blank or
     * null)
     * @see #missingFields()
     * @see #isReady()
     * @see #hasProfile()
     * @since 1.0.0
     */
    public boolean isComplete() {
        return isSet(name) && isSet(rollNumber) && isSet(degree);
    }

    /**
     * Alias of {@link #isComplete()} — see that method for full
     * documentation and examples.
     *
     * @return {@code true} when name, roll number, and degree are all set
     * @see #isComplete()
     * @since 1.0.0
     */
    public boolean isReady() { return isComplete(); }

    /**
     * Alias of {@link #isComplete()} — see that method for full
     * documentation and examples.
     *
     * @return {@code true} when name, roll number, and degree are all set
     * @see #isComplete()
     * @since 1.0.0
     */
    public boolean hasProfile() { return isComplete(); }

    /**
     * Returns the names of the required fields that are not yet set.
     *
     * <p>The returned list is empty when {@link #isComplete()} is
     * {@code true}. The order is fixed — {@code "name"},
     * {@code "rollNumber"}, {@code "degree"} — matching the same order
     * the setters appear in the class. The field names are exactly the
     * names of the setters, so a caller can turn them into a hint like
     * {@code "call .rollNumber(...) and .degree(...)"}.</p>
     *
     * <pre>{@code
     * List<String> missing = Session.me().missingFields();
     * if (!missing.isEmpty()) {
     *     Console.warn("Please set your Session " + missing + " first.");
     * }
     * }</pre>
     *
     * @return the missing field names in fixed order — never {@code null},
     * and unmodifiable
     * @see #isComplete()
     * @since 1.0.0
     */
    public List<String> missingFields() {
        List<String> missing = new ArrayList<>(3);
        if (!isSet(name)) missing.add("name");
        if (!isSet(rollNumber)) missing.add("rollNumber");
        if (!isSet(degree)) missing.add("degree");
        return List.copyOf(missing);
    }

    // ------------------------------------------------------------ helpers

    /**
     * A field counts as "set" when it is non-null and contains at least one
     * non-whitespace character. Used by {@link #isComplete()},
     * {@link #missingFields()}, and the summary/map renderers so all four
     * agree on what "set" means.
     */
    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (isSet(value)) {
            sb.append(label).append(": ").append(value).append('\n');
        }
    }

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (isSet(value)) map.put(key, value);
    }
}
