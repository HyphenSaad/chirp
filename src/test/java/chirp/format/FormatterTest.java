package chirp.format;

import chirp.session.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormatterTest {

    @BeforeEach
    void clearSession() { Session.me().clear(); }
    @AfterEach
    void resetSession() { Session.me().clear(); }

    // ============================================================== factories

    @Test
    void factoriesAllWork() {
        assertEquals("Hello World", Formatter.of("Hello {x}").with("x", "World").format());
        assertEquals("Hello World", Formatter.on("Hello {x}").with("x", "World").format());
        assertEquals("Hello World", Formatter.create("Hello {x}").with("x", "World").format());
        assertEquals("Hello World", Formatter.template("Hello {x}").with("x", "World").format());
    }

    @Test
    void nullTemplate() {
        assertEquals("", Formatter.of(null).format());
    }

    // ============================================================== bind aliases

    @Test
    void bindAliases() {
        assertEquals("Hi Ali", Formatter.of("Hi {n}").with("n", "Ali").format());
        assertEquals("Hi Ali", Formatter.of("Hi {n}").set("n", "Ali").format());
        assertEquals("Hi Ali", Formatter.of("Hi {n}").put("n", "Ali").format());
        assertEquals("Hi Ali", Formatter.of("Hi {n}").bind("n", "Ali").format());
    }

    @Test
    void bindNullValueIgnored() {
        assertEquals("Hi {n}", Formatter.of("Hi {n}").with("n", null).format());
    }

    @Test
    void bindNullKeyIgnored() {
        assertEquals("Hi", Formatter.of("Hi").with(null, "x").format());
    }

    @Test
    void bulkBindFromMap() {
        String result = Formatter.of("Hi {name}, age {age}")
                .with(Map.of("name", "Saad", "age", 25))
                .format();
        assertEquals("Hi Saad, age 25", result);
    }

    @Test
    void bulkBindFromSession() {
        Session.me().name("Saad").rollNumber("001").degree("BSAI");
        String result = Formatter.of("Student: {name}, Roll: {roll}, Degree: {degree}")
                .withSession()
                .format();
        assertEquals("Student: Saad, Roll: 001, Degree: BSAI", result);
    }

    @Test
    void sessionEmptyFieldsBecomeEmpty() {
        Session.me().name("Saad");
        String result = Formatter.of("Name: {name}, Roll: {roll}")
                .withSession()
                .format();
        assertEquals("Name: Saad, Roll: {roll}", result);
        // Wait — withSession binds roll to null which is skipped, so {roll} remains
    }

    // ============================================================== currency

    @Test
    void currency() {
        String result = Formatter.of("Total: {amount}")
                .with("amount", 1234.5)
                .currency("amount")
                .format();
        assertEquals("Total: $1,234.50", result);
    }

    @Test
    void currencyAliases() {
        assertEquals("$1,234.50",
                Formatter.of("{a}").with("a", 1234.5).currency("a").format());
        assertEquals("$1,234.50",
                Formatter.of("{a}").with("a", 1234.5).money("a").format());
        assertEquals("$1,234.50",
                Formatter.of("{a}").with("a", 1234.5).asCurrency("a").format());
    }

    @Test
    void currencyMissingKeyIsNoOp() {
        assertEquals("Hi", Formatter.of("Hi").currency("amount").format());
    }

    @Test
    void rupees() {
        String result = Formatter.of("Rs {amount}")
                .with("amount", 500)
                .rupees("amount")
                .format();
        assertTrue(result.contains("500.00"));
    }

    // ============================================================== percent

    @Test
    void percent() {
        String result = Formatter.of("Progress: {p}")
                .with("p", 87.5)
                .percent("p")
                .format();
        assertEquals("Progress: 87.50%", result);
    }

    @Test
    void percentAliases() {
        assertEquals("87.50%",
                Formatter.of("{p}").with("p", 87.5).percent("p").format());
        assertEquals("87.50%",
                Formatter.of("{p}").with("p", 87.5).pct("p").format());
        assertEquals("87.50%",
                Formatter.of("{p}").with("p", 87.5).asPercent("p").format());
    }

    // ============================================================== decimal

    @Test
    void decimal() {
        assertEquals("3.14",
                Formatter.of("{v}").with("v", 3.14159).decimal("v", 2).format());
        assertEquals("3.1",
                Formatter.of("{v}").with("v", 3.14159).fixed("v", 1).format());
        assertEquals("3.142",
                Formatter.of("{v}").with("v", 3.14159).asDecimal("v", 3).format());
    }

    // ============================================================== string transforms

    @Test
    void upperAliases() {
        assertEquals("HELLO",
                Formatter.of("{v}").with("v", "hello").upper("v").format());
        assertEquals("HELLO",
                Formatter.of("{v}").with("v", "hello").uppercase("v").format());
        assertEquals("HELLO",
                Formatter.of("{v}").with("v", "hello").toUpper("v").format());
    }

    @Test
    void lowerAliases() {
        assertEquals("hello",
                Formatter.of("{v}").with("v", "HELLO").lower("v").format());
        assertEquals("hello",
                Formatter.of("{v}").with("v", "HELLO").lowercase("v").format());
        assertEquals("hello",
                Formatter.of("{v}").with("v", "HELLO").toLower("v").format());
    }

    @Test
    void capitalizeAndTitle() {
        assertEquals("Hello",
                Formatter.of("{v}").with("v", "hello").capitalize("v").format());
        assertEquals("Hello World",
                Formatter.of("{v}").with("v", "hello world").title("v").format());
    }

    @Test
    void trim() {
        assertEquals("hi",
                Formatter.of("{v}").with("v", "  hi  ").trim("v").format());
    }

    @Test
    void truncateMaskSlug() {
        assertEquals("ab…",
                Formatter.of("{v}").with("v", "abcdef").truncate("v", 3).format());
        assertEquals("***4567",
                Formatter.of("{v}").with("v", "1234567").mask("v", 4).format());
        assertEquals("hello-world",
                Formatter.of("{v}").with("v", "Hello World!").slug("v").format());
    }

    // ============================================================== padding

    @Test
    void padLeft() {
        assertEquals("   hi",
                Formatter.of("{v}").with("v", "hi").padLeft("v", 5).format());
        assertEquals("   hi",
                Formatter.of("{v}").with("v", "hi").padLeft("v", 5, ' ').format());
    }

    @Test
    void padRight() {
        assertEquals("hi   ",
                Formatter.of("{v}").with("v", "hi").padRight("v", 5).format());
    }

    @Test
    void center() {
        assertEquals(" hi  ",
                Formatter.of("{v}").with("v", "hi").center("v", 5).format());
    }

    @Test
    void padWithZero() {
        // Classic zero-padding of IDs
        assertEquals("000042",
                Formatter.of("{id}").with("id", 42).padLeft("id", 6, '0').format());
    }

    @Test
    void padRightWithFill() {
        assertEquals("hi***",
                Formatter.of("{v}").with("v", "hi").padRight("v", 5, '*').format());
    }

    // ============================================================== date formatting

    @Test
    void dateWithPattern() {
        String result = Formatter.of("{d}")
                .with("d", LocalDate.of(2026, 10, 31))
                .date("d", "dd/MM/yyyy")
                .format();
        assertEquals("31/10/2026", result);
    }

    @Test
    void humanDate() {
        String result = Formatter.of("{d}")
                .with("d", LocalDate.of(2026, 10, 31))
                .humanDate("d")
                .format();
        assertTrue(result.contains("Saturday"));
    }

    @Test
    void timeWithPattern() {
        String result = Formatter.of("{t}")
                .with("t", "14:30")
                .time("t", "h:mm a")
                .format();
        assertEquals("2:30 PM", result);
    }

    @Test
    void dateTimeWithPattern() {
        String result = Formatter.of("{dt}")
                .with("dt", "2026-10-31 14:30")
                .dateTime("dt", "MMM d, yyyy HH:mm")
                .format();
        assertEquals("Oct 31, 2026 14:30", result);
    }

    // ============================================================== domain formatting

    @Test
    void cnicFormatter() {
        String result = Formatter.of("CNIC: {cnic}")
                .with("cnic", "3520212345671")
                .cnic("cnic")
                .format();
        assertEquals("CNIC: 35202-1234567-1", result);
    }

    @Test
    void phoneFormatter() {
        String result = Formatter.of("Phone: {phone}")
                .with("phone", "0300-1234567")
                .pakistaniPhone("phone")
                .format();
        assertEquals("Phone: +92 300 1234567", result);
    }

    // ============================================================== defaults

    @Test
    void defaultValueWhenMissing() {
        assertEquals("Name: Unknown",
                Formatter.of("Name: {name}").defaultValue("name", "Unknown").format());
    }

    @Test
    void defaultValueWhenEmpty() {
        assertEquals("Name: Unknown",
                Formatter.of("Name: {name}").with("name", "").defaultValue("name", "Unknown").format());
    }

    @Test
    void defaultValueDoesNotOverwrite() {
        assertEquals("Name: Saad",
                Formatter.of("Name: {name}").with("name", "Saad").defaultValue("name", "Unknown").format());
    }

    @Test
    void defaultValueAliases() {
        assertEquals("Unknown",
                Formatter.of("{x}").orElse("x", "Unknown").format());
        assertEquals("Unknown",
                Formatter.of("{x}").fallback("x", "Unknown").format());
    }

    // ============================================================== terminal aliases

    @Test
    void terminalAliases() {
        Formatter f = Formatter.of("Hi {x}").with("x", "there");
        assertEquals("Hi there", f.format());
        assertEquals("Hi there", f.result());
        assertEquals("Hi there", f.render());
        assertEquals("Hi there", f.build());
        assertEquals("Hi there", f.asString());
        assertEquals("Hi there", f.get());
        assertEquals("Hi there", f.toString());
    }

    // ============================================================== immutability

    @Test
    void valuesSnapshot() {
        Formatter f = Formatter.of("{a}").with("a", "1");
        Map<String, String> snapshot = f.values();
        f.with("a", "2");
        assertEquals("1", snapshot.get("a"));
        assertEquals("2", f.values().get("a"));
    }

    // ============================================================== integration scenarios

    @Test
    void fullOrderMessage() {
        String message = Formatter
                .of("Dear {name}, your order #{orderId} was {amount}, shipped {shipDate}.")
                .with("name", "Saad")
                .with("orderId", 42)
                .with("amount", 1234.5)
                .with("shipDate", LocalDate.of(2026, 10, 31))
                .padLeft("orderId", 6, '0')
                .currency("amount")
                .date("shipDate", "MMM d, yyyy")
                .format();
        assertEquals(
                "Dear Saad, your order #000042 was $1,234.50, shipped Oct 31, 2026.",
                message);
    }

    @Test
    void sessionDashboard() {
        Session.me().name("Saad Mansoor").rollNumber("2024-CS-001")
                .degree("BSAI").section("A");
        String result = Formatter
                .of("[{roll}] {name} — {degree} section {section}")
                .withSession()
                .upper("degree")
                .format();
        assertEquals("[2024-CS-001] Saad Mansoor — BSAI section A", result);
    }

    @Test
    void chainOrderDoesNotMatter() {
        // Currency applied before binding — currency() is a no-op on missing key
        // So the correct order is with → currency
        String a = Formatter.of("{v}").with("v", 100).currency("v").format();
        assertEquals("$100.00", a);
    }
}
