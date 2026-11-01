package chirp.mail;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EmailListenerAndExceptionTest {

    // ============================================================== email listener defaults

    @Test
    void everyHookHasDefaultNoOp() {
        EmailListener noop = new EmailListener() {};
        EmailRequest req = new EmailRequest();
        EmailResult result = new EmailResult(true, "OK", 0L, "", List.of(), List.of());

        assertDoesNotThrow(() -> noop.onStart(req));
        assertDoesNotThrow(() -> noop.onSmtpEvent("250 OK"));
        assertDoesNotThrow(() -> noop.onComplete(result));
        assertDoesNotThrow(() -> noop.onError(new Exception()));
        assertDoesNotThrow(() -> noop.onConnect("localhost", 25));
        assertDoesNotThrow(noop::onDisconnect);
        assertDoesNotThrow(() -> noop.onDelivered(result));
        assertDoesNotThrow(() -> noop.onBounce(result));
        assertDoesNotThrow(() -> noop.onRetry(1, new Exception()));
    }

    @Test
    void listenerReceivesStartAndCompleteOnDryRun() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean delivered = new AtomicBoolean(false);
        AtomicBoolean completed = new AtomicBoolean(false);

        Email.builder()
                .host("smtp.example.com")
                .from("a@example.com")
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .listener(new EmailListener() {
                    @Override
                    public void onStart(EmailRequest r) { started.set(true); }
                    @Override
                    public void onDelivered(EmailResult r) { delivered.set(true); }
                    @Override
                    public void onComplete(EmailResult r) { completed.set(true); }
                })
                .send();

        assertTrue(started.get());
        assertTrue(completed.get());
    }

    @Test
    void listenerReceivesErrorForValidationFailure() {
        AtomicBoolean errored = new AtomicBoolean(false);

        Email.builder()
                .from("a@example.com")     // no host → validation error
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .listener(new EmailListener() {
                    @Override
                    public void onError(Exception e) { errored.set(true); }
                })
                .send();

        assertTrue(errored.get());
    }

    @Test
    void retryHookIsCalledOnFailure() {
        AtomicInteger retries = new AtomicInteger();

        Email.builder()
                .from("a@example.com")    // no host → always fails
                .to("d@example.org")
                .subject("x")
                .text("y")
                .password("dummy")
                .dryRun(true)
                .noLoader()
                .listener(new EmailListener() {
                    @Override
                    public void onRetry(int attempt, Exception cause) {
                        retries.incrementAndGet();
                    }
                })
                .sendWithRetry(3, Duration.ZERO);

        // Retry attempts: 3 → onRetry called twice (between attempt 1→2 and 2→3)
        assertTrue(retries.get() >= 2, "Expected ≥2 retries, got " + retries.get());
    }

    // ============================================================== email result aliases

    @Test
    void resultStatusAliases() {
        EmailResult ok = new EmailResult(true, "OK", 100L, "", List.of(), List.of());
        assertTrue(ok.isSuccess());
        assertTrue(ok.isOk());
        assertTrue(ok.succeeded());
        assertTrue(ok.ok());
        assertTrue(ok.good());
        assertFalse(ok.isFailed());

        EmailResult bad = new EmailResult(false, "bad", 0L, "", List.of(), List.of());
        assertFalse(bad.isSuccess());
        assertTrue(bad.isFailed());
        assertTrue(bad.failed());
        assertTrue(bad.bad());
    }

    @Test
    void resultAccessors() {
        EmailResult r = new EmailResult(true, "sent", 42L,
                "MIME content",
                List.of("a@example.com", "d@example.org"),
                List.of("250 OK", "354"));

        assertEquals(42L, r.elapsed());
        assertEquals(42L, r.took());
        assertEquals("MIME content", r.mime());
        assertEquals("MIME content", r.raw());
        assertEquals("MIME content", r.asMime());
        assertTrue(r.hasMime());
        assertEquals(2, r.recipientCount());
        assertEquals("a@example.com", r.firstRecipient());
        assertEquals(2, r.smtpLineCount());
        assertTrue(r.hasLog());
        assertTrue(r.logged());
        assertEquals(List.of("250 OK", "354"), r.log());
        assertEquals(List.of("250 OK", "354"), r.dialog());
    }

    @Test
    void resultFailureFactory() {
        EmailResult r = EmailResult.failure("oops");
        assertTrue(r.isFailed());
        assertEquals("oops", r.message());
        assertEquals(0, r.recipientCount());
    }

    // ============================================================== email exception reasons

    @Test
    void allReasonFactoriesWork() {
        assertEquals(EmailException.Reason.TRANSPORT, EmailException.transport("x", null).reason());
        assertEquals(EmailException.Reason.AUTH, EmailException.auth("x").reason());
        assertEquals(EmailException.Reason.VALIDATION, EmailException.validation("x").reason());
        assertEquals(EmailException.Reason.MISSING_CONFIG, EmailException.missingConfig("x").reason());
    }

    @Test
    void predicatesWork() {
        assertTrue(EmailException.transport("x", null).isTransportError());
        assertTrue(EmailException.transport("x", null).isNetworkError());
        assertTrue(EmailException.auth("x").isAuthError());
        assertTrue(EmailException.auth("x").isAuthenticationError());
        assertTrue(EmailException.validation("x").isValidationError());
        assertTrue(EmailException.validation("x").isInvalid());
        assertTrue(EmailException.missingConfig("x").isMissingConfig());
        assertTrue(EmailException.missingConfig("x").isMissingConfiguration());
    }

    @Test
    void defaultConstructorUsesUnknown() {
        EmailException e = new EmailException("msg");
        assertEquals(EmailException.Reason.UNKNOWN, e.reason());
        assertFalse(e.isTransportError());
        assertFalse(e.isAuthError());
    }
}
