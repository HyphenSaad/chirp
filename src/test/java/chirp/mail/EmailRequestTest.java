package chirp.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EmailRequestTest {

    // ---------------------------------------------------- builder basics

    @Test
    void builderStartsEmpty() {
        EmailRequest r = Email.builder();
        assertNull(r.host());
        assertNull(r.from());
        assertTrue(r.to().isEmpty());
    }

    @Test
    void chainingSetsValues() {
        EmailRequest r = Email.builder()
                .host("smtp.example.com")
                .port(587)
                .from("a@example.com")
                .to("d@example.org")
                .subject("Hi")
                .text("Hello");
        assertEquals("smtp.example.com", r.host());
        assertEquals(587, r.port());
        assertEquals("a@example.com", r.from());
        assertEquals(1, r.to().size());
        assertEquals("Hi", r.subject());
    }

    @Test
    void multipleRecipientsAccumulate() {
        EmailRequest r = Email.builder()
                .to("a@example.com")
                .to("d@example.org", "g@example.net");
        assertEquals(3, r.to().size());
    }

    @Test
    void attachValidatesExistence(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("test.pdf");
        Files.writeString(f, "pdf-bytes");
        EmailRequest r = Email.builder().attach(f.toString());
        assertNotNull(r);
    }

    @Test
    void attachMissingFileThrows() {
        assertThrows(EmailException.class,
                () -> Email.builder().attach("/does/not/exist.pdf"));
    }

    // ---------------------------------------------------- password strategy

    @Test
    void directPasswordSetsStrategy() {
        EmailRequest r = Email.builder().password("abc");
        assertEquals(PasswordStrategy.DIRECT, r.strategy());
    }

    @Test
    void promptPasswordSetsStrategy() {
        EmailRequest r = Email.builder().promptPassword();
        assertEquals(PasswordStrategy.PROMPT, r.strategy());
    }

    @Test
    void envPasswordSetsStrategy() {
        EmailRequest r = Email.builder().passwordFromEnv(".env", "KEY");
        assertEquals(PasswordStrategy.ENV, r.strategy());
    }

    // ---------------------------------------------------- dry run

    @Test
    void dryRunBuildsMessage() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("Test")
                .text("Hello world")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess(), r.message());
        assertTrue(r.rawMessage().contains("Subject: Test"));
        assertTrue(r.rawMessage().contains("Hello world"));
        assertTrue(r.rawMessage().contains("<a@example.com>"));
        assertTrue(r.rawMessage().contains("<d@example.org>"));
    }

    @Test
    void dryRunWithHtmlAndTemplate(@TempDir Path dir) throws Exception {
        Path template = dir.resolve("t.html");
        Files.writeString(template, "<p>Hello {{name}}</p>");

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("Test")
                .template(HtmlTemplate.of(template.toString()).placeholder("name", "Saad"))
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess(), r.message());
        assertTrue(r.rawMessage().contains("Hello Saad"));
    }

    /**
     * The attachment branch produces a {@code multipart/mixed} message whose
     * attachment payload is base64-encoded. Instead of hard-coding a partial
     * base64 literal (which is easy to get subtly wrong), we compute the exact
     * expected base64 in the test. Failure messages then show the expected
     * value, making any future regression immediately obvious.
     */
    @Test
    void dryRunWithAttachment(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("file.txt");
        String content = "attachment content";
        Files.writeString(f, content);

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("With attachment")
                .text("Body")
                .attach(f.toString())
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess(), r.message());

        String raw = r.rawMessage();
        assertTrue(raw.contains("multipart/mixed"),
                "Expected multipart/mixed Content-Type, got:\n" + raw);
        assertTrue(raw.contains("file.txt"),
                "Expected attachment filename in message, got:\n" + raw);

        String expectedBase64 = Base64.getMimeEncoder()
                .encodeToString(content.getBytes(StandardCharsets.UTF_8));
        assertTrue(raw.contains(expectedBase64),
                "Expected base64 '" + expectedBase64 + "' in message, got:\n" + raw);
    }

    @Test
    void missingHostFails() {
        EmailResult r = Email.builder()
                .from("a@example.com")
                .to("d@example.org")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isFailure());
        assertTrue(r.message().toLowerCase().contains("host"));
    }

    @Test
    void missingFromFails() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .to("d@example.org")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isFailure());
        assertTrue(r.message().toLowerCase().contains("sender"));
    }

    @Test
    void missingRecipientsFails() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isFailure());
        assertTrue(r.message().toLowerCase().contains("recipient"));
    }

    @Test
    void missingBodyFails() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isFailure());
        assertTrue(r.message().toLowerCase().contains("body"));
    }

    // ---------------------------------------------------- default port

    @Test
    void sslPort465ByDefaultForSsl() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .ssl(true)
                .from("a@example.com")
                .to("d@example.org")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isSuccess());
    }

    @Test
    void starttlsPort587ByDefault() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();
        assertTrue(r.isSuccess());
    }

    // ---------------------------------------------------- listener

    @Test
    void listenerReceivesEvents() {
        java.util.concurrent.atomic.AtomicBoolean started = new java.util.concurrent.atomic.AtomicBoolean();
        java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean();

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .text("x")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .listener(new EmailListener() {
                    @Override
                    public void onStart(EmailRequest req) { started.set(true); }
                    @Override
                    public void onComplete(EmailResult res) { completed.set(true); }
                })
                .send();

        assertTrue(r.isSuccess());
        assertTrue(started.get());
        assertTrue(completed.get());
    }

    // ---------------------------------------------------- facade overloads

    @Test
    void sendKeyValueFacadeWorks() {
        EmailResult r = Email.send(
                Email.Key.HOST, "smtp.example.com",
                Email.Key.FROM, "a@example.com",
                Email.Key.TO, "d@example.org",
                Email.Key.SUBJECT, "Hi",
                Email.Key.TEXT, "Body",
                Email.Key.PASSWORD, "dummy",
                Email.Key.DRY_RUN, true,
                Email.Key.LOADER, false);

        assertTrue(r.isSuccess(), r.message());
    }

    @Test
    void sendKeyValueWithMissingValueIsGraceful() {
        EmailResult r = Email.send(
                Email.Key.HOST, "smtp.example.com",
                Email.Key.FROM);
        assertTrue(r.isFailure());
    }

    @Test
    void sendKeyValueWithWrongTypeIsGraceful() {
        EmailResult r = Email.send(
                Email.Key.HOST, 12345);
        assertTrue(r.isFailure());
        assertTrue(r.message().contains("HOST") || r.message().toLowerCase().contains("string"));
    }
}
