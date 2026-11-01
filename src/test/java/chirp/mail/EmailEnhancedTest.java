package chirp.mail;

import chirp.session.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailEnhancedTest {

    @TempDir
    Path dir;

    @BeforeEach
    void reset() { Session.me().clear(); }
    @AfterEach
    void reset2() { Session.me().clear(); }

    // ============================================================== sender names

    @Test
    void senderNameAppearsInFrom() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .senderName("Saad Mansoor")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess());
        assertTrue(r.rawMessage().contains("From:"), r.rawMessage());
        assertTrue(r.rawMessage().contains("Saad Mansoor"), r.rawMessage());
        assertTrue(r.rawMessage().contains("<a@example.com>"), r.rawMessage());
    }

    @Test
    void replyToNameAppears() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .replyTo("noreply@example.com")
                .replyToName("Support Team")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess());
        assertTrue(r.rawMessage().contains("Reply-To:"), r.rawMessage());
        assertTrue(r.rawMessage().contains("Support Team"), r.rawMessage());
    }

    // ============================================================== priority / bounce / extra headers

    @Test
    void priorityAppears() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .highPriority()
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.isSuccess());
        assertTrue(r.rawMessage().contains("X-Priority: 1"), r.rawMessage());
    }

    @Test
    void normalPriorityOmitsHeader() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertFalse(r.rawMessage().contains("X-Priority"),
                "Normal priority should not emit X-Priority header");
    }

    @Test
    void bounceToAppearsAsReturnPath() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .bounceTo("bounce@example.com")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.rawMessage().contains("Return-Path:"), r.rawMessage());
        assertTrue(r.rawMessage().contains("bounce@example.com"), r.rawMessage());
    }

    @Test
    void extraHeadersAppear() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .addHeader("X-Custom", "custom-value")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.rawMessage().contains("X-Custom: custom-value"), r.rawMessage());
    }

    // ============================================================== signature

    @Test
    void signatureAppendedToText() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("Body text")
                .signature("Saad Mansoor\n+92 300 1234567")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        String raw = r.rawMessage();
        assertTrue(raw.contains("Body text"), raw);
        assertTrue(raw.contains("Saad Mansoor"), raw);
        assertTrue(raw.contains("+92 300 1234567"), raw);
    }

    @Test
    void signatureFromSession() {
        Session.me()
                .name("Saad Mansoor")
                .rollNumber("2024-CS-001")
                .degree("BSAI")
                .email("saad@example.com");

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("Body")
                .signatureFromSession()
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        String raw = r.rawMessage();
        assertTrue(raw.contains("Saad Mansoor"), raw);
        assertTrue(raw.contains("2024-CS-001"), raw);
        assertTrue(raw.contains("BSAI"), raw);
        assertTrue(raw.contains("saad@example.com"), raw);
    }

    // ============================================================== attachments

    @Test
    void attachMany() throws Exception {
        Path a = dir.resolve("a.txt");
        Path b = dir.resolve("b.txt");
        Files.writeString(a, "A");
        Files.writeString(b, "B");

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .attachMany(List.of(a.toString(), b.toString()))
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.rawMessage().contains("a.txt"), r.rawMessage());
        assertTrue(r.rawMessage().contains("b.txt"), r.rawMessage());
    }

    @Test
    void attachDirectory() throws Exception {
        Path d = dir.resolve("attachments");
        Files.createDirectories(d);
        Files.writeString(d.resolve("file1.txt"), "one");
        Files.writeString(d.resolve("file2.txt"), "two");

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .attachDirectory(d.toString())
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertTrue(r.rawMessage().contains("file1.txt"), r.rawMessage());
        assertTrue(r.rawMessage().contains("file2.txt"), r.rawMessage());
    }

    @Test
    void noAttachmentsClears() throws Exception {
        Path a = dir.resolve("a.txt");
        Files.writeString(a, "A");

        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .attach(a.toString())
                .noAttachments()
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .send();

        assertFalse(r.rawMessage().contains("a.txt"), r.rawMessage());
        assertFalse(r.rawMessage().contains("multipart/mixed"), r.rawMessage());
    }

    @Test
    void attachmentCount() throws Exception {
        Path a = dir.resolve("a.txt");
        Files.writeString(a, "A");
        EmailRequest r = Email.builder().attach(a.toString());
        assertEquals(1, r.attachmentCount());
        r.noAttachments();
        assertEquals(0, r.attachmentCount());
    }

    // ============================================================== preview

    @Test
    void previewIfValidReturnsMessage() {
        EmailRequest req = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy");

        assertTrue(req.previewIfValid().isPresent());
    }

    @Test
    void previewIfInvalidReturnsEmpty() {
        EmailRequest req = Email.builder()
                .from("a@example.com")
                .to("d@example.org")
                .text("y");   // no host

        assertTrue(req.previewIfValid().isEmpty());
    }

    @Test
    void previewThrowsWhenInvalid() {
        EmailRequest req = Email.builder();
        assertThrows(EmailException.class, req::previewMimeMessage);
    }

    // ============================================================== username from env

    @Test
    void usernameFromEnv() throws Exception {
        Path env = dir.resolve(".env");
        Files.writeString(env, "USER=me@example.com\nPASS=secret\n");

        EmailRequest req = Email.builder()
                .host("smtp.example.com")
                .from("me@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .usernameFromEnv(env.toString(), "USER")
                .passwordFromEnv(env.toString(), "PASS");

        assertNotNull(req);
    }

    // ============================================================== sendWithRetry

    @Test
    void sendWithRetrySucceedsOnFirstAttemptForDryRun() {
        EmailResult r = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .sendWithRetry(3);

        assertTrue(r.isSuccess());
    }

    @Test
    void sendWithRetryAliases() {
        EmailRequest req = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader();

        assertTrue(req.sendWithRetry(2).isSuccess());
        assertTrue(req.retry(2).isSuccess());
        assertTrue(req.sendWithRetries(2).isSuccess());
    }

    // ============================================================== connectivity

    @Test
    void testConnectionFailsForInvalidHost() {
        EmailRequest req = Email.builder()
                .host("no-such-host-xyz-12345.invalid")
                .port(25)
                .timeoutSeconds(1);

        assertFalse(req.test());
        assertFalse(req.canConnect());
        assertFalse(req.testConnection());
    }

    @Test
    void testOrThrowThrowsForInvalidHost() {
        EmailRequest req = Email.builder()
                .host("no-such-host-xyz-12345.invalid")
                .port(25)
                .from("a@example.com")
                .to("d@example.org")
                .text("y")
                .timeoutSeconds(1);

        assertThrows(EmailException.class, req::testOrThrow);
    }

    @Test
    void canConnectStatic() {
        assertFalse(Email.canConnect("no-such-host-xyz.invalid", 25));
        assertFalse(Email.canConnect(null, 25));
        assertFalse(Email.testServer("no-such-host.invalid", 25));
        assertFalse(Email.pingServer("no-such-host.invalid", 25));
    }

    // ============================================================== key-value facade

    @Test
    void keyValueSendWithNewKeys() {
        EmailResult r = Email.send(
                Email.Key.HOST, "smtp.example.com",
                Email.Key.FROM, "a@example.com",
                Email.Key.SENDER_NAME, "My App",
                Email.Key.TO, "d@example.org",
                Email.Key.SUBJECT, "Hi",
                Email.Key.TEXT, "Body",
                Email.Key.PASSWORD, "dummy",
                Email.Key.PRIORITY, 1,
                Email.Key.DRY_RUN, true,
                Email.Key.LOADER, false);

        assertTrue(r.isSuccess(), r.message());
        assertTrue(r.rawMessage().contains("X-Priority: 1"));
        assertTrue(r.rawMessage().contains("My App"));
    }

    @Test
    void keyValueWithRetries() {
        // 1 retry on a dry run still succeeds
        EmailResult r = Email.send(
                Email.Key.HOST, "smtp.example.com",
                Email.Key.FROM, "a@example.com",
                Email.Key.TO, "d@example.org",
                Email.Key.SUBJECT, "x",
                Email.Key.TEXT, "y",
                Email.Key.PASSWORD, "dummy",
                Email.Key.DRY_RUN, true,
                Email.Key.LOADER, false,
                Email.Key.RETRY_ATTEMPTS, 2);

        assertTrue(r.isSuccess());
    }

    // ============================================================== alias methods

    @Test
    void deliveryAliases() {
        EmailRequest req = Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader();

        assertTrue(req.deliver().isSuccess());
        assertTrue(req.dispatch().isSuccess());
    }

    @Test
    void factoryAliases() {
        assertNotNull(Email.newEmail());
        assertNotNull(Email.message());
        assertNotNull(Email.mail());
        assertNotNull(Email.compose());
        assertNotNull(Email.create());
    }

    // ============================================================== EmailException reasons

    @Test
    void validationReason() {
        EmailException e = assertThrows(EmailException.class,
                () -> Email.builder().attach("/nonexistent-file-xyz.pdf"));
        assertTrue(e.isValidationError());
        assertFalse(e.isTransportError());
    }

    @Test
    void missingConfigReason() {
        EmailException e = assertThrows(EmailException.class,
                () -> Email.builder().from("a@example.com").to("d@example.org").text("y").sendOrThrow());
        assertTrue(e.isMissingConfig());
    }
}
