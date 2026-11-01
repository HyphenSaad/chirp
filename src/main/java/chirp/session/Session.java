package chirp.session;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The current student's profile — name, roll number, batch, degree, section,
 * and email. Configure once at program start; render in screenshots later.
 *
 * <pre>{@code
 * Session.me()
 *     .name("Saad Mansoor")
 *     .rollNumber("2024-CS-001")
 *     .batch("2024")
 *     .degree("BSAI")
 *     .section("A")
 *     .email("saad@example.com");
 *
 * Session.me().print();
 * }</pre>
 *
 * <p>All values are optional. The class is a singleton.</p>
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

    public static Session me() { return INSTANCE; }
    public static Session get() { return INSTANCE; }
    public static Session of() { return INSTANCE; }
    public static Session current() { return INSTANCE; }
    public static Session configure() { return INSTANCE; }
    public static Session instance() { return INSTANCE; }

    public Session name(String value) { this.name = value; return this; }
    public Session fullName(String value) { return name(value); }
    public Session studentName(String value) { return name(value); }
    public String name() { return name; }
    public String getName() { return name; }
    public String fullName() { return name; }
    public String studentName() { return name; }

    public Session rollNumber(String value) { this.rollNumber = value; return this; }
    public Session roll(String value) { return rollNumber(value); }
    public Session rollNo(String value) { return rollNumber(value); }
    public Session registrationNumber(String value) { return rollNumber(value); }
    public Session regNo(String value) { return rollNumber(value); }
    public String rollNumber() { return rollNumber; }
    public String getRollNumber() { return rollNumber; }
    public String roll() { return rollNumber; }
    public String rollNo() { return rollNumber; }

    public Session batch(String value) { this.batch = value; return this; }
    public Session batchNumber(String value) { return batch(value); }
    public String batch() { return batch; }
    public String getBatch() { return batch; }
    public String batchNumber() { return batch; }

    public Session degree(String value) { this.degree = value; return this; }
    public Session degreeCode(String value) { return degree(value); }
    public Session program(String value) { return degree(value); }
    public String degree() { return degree; }
    public String getDegree() { return degree; }
    public String degreeCode() { return degree; }
    public String program() { return degree; }

    public Session section(String value) { this.section = value; return this; }
    public Session classSection(String value) { return section(value); }
    public String section() { return section; }
    public String getSection() { return section; }
    public String classSection() { return section; }

    public Session email(String value) { this.email = value; return this; }
    public Session emailAddress(String value) { return email(value); }
    public String email() { return email; }
    public String getEmail() { return email; }
    public String emailAddress() { return email; }

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

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value).append('\n');
        }
    }

    public void print() { System.out.print(summary()); }

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

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }

    public void clear() {
        name = null;
        rollNumber = null;
        batch = null;
        degree = null;
        section = null;
        email = null;
    }
}
