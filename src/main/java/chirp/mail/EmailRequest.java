package chirp.mail;

import chirp.console.Console;
import chirp.console.Loader;
import chirp.internal.Logger;
import chirp.internal.Trace;
import chirp.session.Session;
import chirp.text.Check;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fluent email request. Every setter returns {@code this}; call
 * {@link #send()} (graceful) or {@link #sendOrThrow()} (throws on failure)
 * to dispatch.
 *
 * <h2>Naming</h2>
 * <p>Every method has 2–4 aliases. {@code host / server / smtpHost / mailServer};
 * {@code from / sender / fromAddress}; {@code to / recipient / addRecipient};
 * {@code subject / title}; {@code text / body / plain / bodyText};
 * {@code html / htmlBody / bodyHtml}; {@code template / htmlTemplate};
 * {@code attach / attachFile / includeFile};
 * {@code password / withPassword}; {@code promptPassword / askPassword};
 * {@code passwordFromEnv / envPassword};
 * {@code send / deliver / dispatch}; {@code sendOrThrow / deliverOrThrow};
 * {@code preview / mimeMessage / asMime}.</p>
 *
 * <h2>Listener exception policy</h2>
 * <p>{@link EmailListener} callbacks are best-effort: a broken listener
 * must never break the send. Exceptions from listeners are logged via
 * {@link Trace#warn(String, String)} so they surface when tracing is
 * enabled, and are otherwise ignored.</p>
 *
 * <h2>Transport errors</h2>
 * <p>{@link SmtpClient#send} declares {@code throws IOException}. It is
 * caught at the call site and rethrown as {@link EmailException} with
 * {@link EmailException.Reason#TRANSPORT}, so the caller sees a single
 * failure category regardless of whether the problem was DNS, TLS, or
 * SMTP-level rejection.</p>
 */
public final class EmailRequest {

    // ---------------------------------------------------------- SMTP transport
    private String host;
    private int port = -1;
    private boolean ssl;
    private boolean starttls;

    // ---------------------------------------------------------- from / to
    private String from;
    private String senderName;
    private String username;
    private String usernameEnvPath;
    private String usernameEnvKey;
    private final List<String> to = new ArrayList<>();
    private final List<String> cc = new ArrayList<>();
    private final List<String> bcc = new ArrayList<>();
    private String replyTo;
    private String replyToName;
    private String bounceTo;
    private int priority = 3;
    private String subject = "";

    // ---------------------------------------------------------- body
    private String text;
    private String html;
    private HtmlTemplate htmlTemplate;
    private String signature;

    // ---------------------------------------------------------- attachments
    private record Attachment(String filename, byte[] bytes) {}
    private final List<Attachment> attachments = new ArrayList<>();

    // ---------------------------------------------------------- password
    private PasswordStrategy strategy;
    private String password;
    private String envPath;
    private String envKey;

    // ---------------------------------------------------------- extra headers
    private final Map<String, String> extraHeaders = new LinkedHashMap<>();

    // ---------------------------------------------------------- behaviour
    private Duration timeout = Duration.ofSeconds(30);
    private boolean verbose;
    // null = automatic: show the spinner only in interactive console mode (Chirp.start() with console messages on)
    private Boolean loaderEnabled;
    private boolean dryRun;
    private EmailListener listener;

    // ============================================================== getters (for tests)

    public String host() { return host; }
    public int port() { return port; }
    public boolean ssl() { return ssl; }
    public boolean starttls() { return starttls; }
    public String from() { return from; }
    public List<String> to() { return List.copyOf(to); }
    public List<String> cc() { return List.copyOf(cc); }
    public List<String> bcc() { return List.copyOf(bcc); }
    public String subject() { return subject; }
    public String text() { return text; }
    public String html() { return html; }
    public PasswordStrategy strategy() { return strategy; }
    public boolean dryRun() { return dryRun; }
    public boolean verbose() { return verbose; }
    public EmailListener listener() { return listener; }
    public String envPath() { return envPath; }
    public String envKey() { return envKey; }

    // ============================================================== SMTP server

    public EmailRequest host(String host) { this.host = host; return this; }
    public EmailRequest server(String host) { return host(host); }
    public EmailRequest smtpHost(String host) { return host(host); }
    public EmailRequest mailServer(String host) { return host(host); }

    public EmailRequest port(int port) { this.port = port; return this; }
    public EmailRequest smtpPort(int port) { return port(port); }

    public EmailRequest ssl(boolean enabled) { this.ssl = enabled; return this; }
    public EmailRequest useSsl() { return ssl(true); }
    public EmailRequest withSsl() { return ssl(true); }

    public EmailRequest starttls(boolean enabled) { this.starttls = enabled; return this; }
    public EmailRequest useStarttls() { return starttls(true); }
    public EmailRequest withStarttls() { return starttls(true); }

    // ============================================================== sender

    public EmailRequest from(String address) { this.from = address; return this; }
    public EmailRequest sender(String address) { return from(address); }
    public EmailRequest fromAddress(String address) { return from(address); }

    public EmailRequest senderName(String name) { this.senderName = name; return this; }
    public EmailRequest fromName(String name) { return senderName(name); }
    public EmailRequest senderDisplayName(String name) { return senderName(name); }

    public EmailRequest username(String user) { this.username = user; return this; }
    public EmailRequest user(String user) { return username(user); }

    public EmailRequest usernameFromEnv(String path, String key) {
        this.usernameEnvPath = path;
        this.usernameEnvKey = key;
        return this;
    }
    public EmailRequest envUsername(String path, String key) { return usernameFromEnv(path, key); }

    // ============================================================== recipients

    public EmailRequest to(String address) { if (address != null) to.add(address); return this; }
    public EmailRequest to(String... addresses) {
        if (addresses != null) for (String a : addresses) to(a);
        return this;
    }
    public EmailRequest to(List<String> addresses) { if (addresses != null) to.addAll(addresses); return this; }
    public EmailRequest recipient(String address) { return to(address); }
    public EmailRequest addRecipient(String address) { return to(address); }

    public EmailRequest cc(String address) { if (address != null) cc.add(address); return this; }
    public EmailRequest cc(String... addresses) {
        if (addresses != null) for (String a : addresses) cc(a);
        return this;
    }

    public EmailRequest bcc(String address) { if (address != null) bcc.add(address); return this; }
    public EmailRequest bcc(String... addresses) {
        if (addresses != null) for (String a : addresses) bcc(a);
        return this;
    }

    public EmailRequest replyTo(String address) { this.replyTo = address; return this; }
    public EmailRequest replyToName(String name) { this.replyToName = name; return this; }
    public EmailRequest replyToDisplayName(String n) { return replyToName(n); }

    public EmailRequest bounceTo(String address) { this.bounceTo = address; return this; }
    public EmailRequest returnPath(String address) { return bounceTo(address); }

    // ============================================================== priority

    public EmailRequest priority(int level) {
        this.priority = Math.max(1, Math.min(5, level));
        return this;
    }
    public EmailRequest highPriority() { return priority(1); }
    public EmailRequest normalPriority() { return priority(3); }
    public EmailRequest lowPriority() { return priority(5); }

    // ============================================================== extra headers

    public EmailRequest header(String name, String value) {
        if (name != null && value != null) extraHeaders.put(name, value);
        return this;
    }
    public EmailRequest addHeader(String name, String value) { return header(name, value); }
    public EmailRequest withHeader(String name, String value) { return header(name, value); }

    // ============================================================== subject

    public EmailRequest subject(String s) { this.subject = s == null ? "" : s; return this; }
    public EmailRequest title(String s) { return subject(s); }

    // ============================================================== body

    public EmailRequest text(String body) { this.text = body; return this; }
    public EmailRequest body(String body) { return text(body); }
    public EmailRequest plain(String body) { return text(body); }
    public EmailRequest bodyText(String body) { return text(body); }

    public EmailRequest html(String body) { this.html = body; return this; }
    public EmailRequest htmlBody(String body) { return html(body); }
    public EmailRequest bodyHtml(String body) { return html(body); }

    public EmailRequest htmlTemplate(HtmlTemplate t) { this.htmlTemplate = t; return this; }
    public EmailRequest template(HtmlTemplate t) { return htmlTemplate(t); }
    public EmailRequest template(String path) { return htmlTemplate(HtmlTemplate.of(path)); }

    // ============================================================== signature

    public EmailRequest signature(String sig) { this.signature = sig; return this; }
    public EmailRequest appendSignature(String sig) { this.signature = sig; return this; }

    /** Builds a signature from {@link Session#me()} — name, program, roll, email. */
    public EmailRequest signatureFromSession() {
        Session s = Session.me();
        StringBuilder sb = new StringBuilder();
        if (s.name() != null) sb.append(s.name()).append('\n');
        if (s.rollNumber() != null || s.degree() != null) {
            if (s.rollNumber() != null) sb.append("Roll: ").append(s.rollNumber());
            if (s.degree() != null) {
                if (sb.length() > 0 && s.rollNumber() != null) sb.append(" | ");
                sb.append("Program: ").append(s.degree());
            }
            sb.append('\n');
        }
        if (s.email() != null) sb.append(s.email()).append('\n');
        this.signature = sb.toString().trim();
        return this;
    }
    public EmailRequest sessionSignature() { return signatureFromSession(); }

    // ============================================================== attachments

    public EmailRequest attach(String path) {
        if (path == null) return this;
        Path p = Path.of(path);
        if (!Files.exists(p)) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Attachment not found: " + p.toAbsolutePath());
        }
        if (!Files.isRegularFile(p)) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Attachment is not a regular file: " + p.toAbsolutePath());
        }
        try {
            byte[] data = Files.readAllBytes(p);
            attachments.add(new Attachment(p.getFileName().toString(), data));
        } catch (java.io.IOException e) {
            throw new EmailException(EmailException.Reason.TRANSPORT,
                    "Cannot read attachment: " + path, e);
        }
        return this;
    }
    public EmailRequest attachFile(String path) { return attach(path); }
    public EmailRequest includeFile(String path) { return attach(path); }

    public EmailRequest attach(File file) {
        return file == null ? this : attach(file.getAbsolutePath());
    }

    public EmailRequest attach(String filename, byte[] data) {
        if (filename == null || data == null) return this;
        attachments.add(new Attachment(filename, data.clone()));
        return this;
    }
    public EmailRequest attachBytes(String filename, byte[] data) { return attach(filename, data); }

    /** Attaches every file (not directory) inside {@code path}. */
    public EmailRequest attachMany(List<String> paths) {
        if (paths != null) for (String p : paths) attach(p);
        return this;
    }
    public EmailRequest attachAll(List<String> paths) { return attachMany(paths); }

    /** Attaches all regular files inside a directory. */
    public EmailRequest attachDirectory(String path) {
        if (path == null) return this;
        Path dir = Path.of(path);
        if (!Files.isDirectory(dir)) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "Not a directory: " + path);
        }
        try (var stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(p -> attach(p.toString()));
        } catch (java.io.IOException e) {
            throw new EmailException(EmailException.Reason.TRANSPORT,
                    "Cannot read directory: " + path, e);
        }
        return this;
    }
    public EmailRequest attachDir(String path) { return attachDirectory(path); }

    /** Removes all attachments added so far. */
    public EmailRequest noAttachments() {
        attachments.clear();
        return this;
    }
    public EmailRequest clearAttachments() { return noAttachments(); }
    public EmailRequest removeAttachments() { return noAttachments(); }

    public int attachmentCount() { return attachments.size(); }

    // ============================================================== password

    public EmailRequest password(String direct) {
        this.password = direct;
        this.strategy = PasswordStrategy.DIRECT;
        return this;
    }
    public EmailRequest withPassword(String direct) { return password(direct); }

    public EmailRequest promptPassword() { this.strategy = PasswordStrategy.PROMPT; return this; }
    public EmailRequest askPassword() { return promptPassword(); }

    public EmailRequest passwordFromEnv(String path, String key) {
        this.strategy = PasswordStrategy.ENV;
        this.envPath = path;
        this.envKey = key;
        return this;
    }
    public EmailRequest envPassword(String path, String key) { return passwordFromEnv(path, key); }

    public EmailRequest passwordFrom(PasswordStrategy s) { this.strategy = s; return this; }
    public EmailRequest passwordFrom(PasswordStrategy s, String path, String key) {
        this.strategy = s;
        if (s == PasswordStrategy.ENV) { this.envPath = path; this.envKey = key; }
        if (s == PasswordStrategy.DIRECT) { this.password = path; }
        return this;
    }

    // ============================================================== behaviour

    public EmailRequest timeout(Duration d) { if (d != null && !d.isNegative()) this.timeout = d; return this; }
    public EmailRequest timeoutSeconds(int s) { return timeout(Duration.ofSeconds(Math.max(1, s))); }

    public EmailRequest verbose(boolean v) { this.verbose = v; return this; }
    public EmailRequest withVerbose() { return verbose(true); }
    public EmailRequest noVerbose() { return verbose(false); }
    public EmailRequest log() { return verbose(true); }
    public EmailRequest logs() { return verbose(true); }

    public EmailRequest loader(boolean v) { this.loaderEnabled = v; return this; }
    public EmailRequest withLoader() { return loader(true); }
    public EmailRequest noLoader() { return loader(false); }

    public EmailRequest dryRun(boolean v) { this.dryRun = v; return this; }
    public EmailRequest preview() { return dryRun(true); }

    public EmailRequest listener(EmailListener l) { this.listener = l; return this; }
    public EmailRequest onEvent(EmailListener l) { return listener(l); }

    // ============================================================== send

    public EmailResult send() { return doSend(false); }

    /**
     * Shared send pipeline. When {@code throwOnFailure} is set, the failure is
     * rethrown (after listener/loader notification) instead of being folded
     * into an {@link EmailResult}, so the original {@link EmailException.Reason}
     * survives for {@link #sendOrThrow()} callers.
     */
    private EmailResult doSend(boolean throwOnFailure) {
        long start = System.currentTimeMillis();
        Loader loader = null;
        try {
            validate();

            notifyStart();

            String resolvedPassword = resolvePassword();
            String resolvedUsername = resolveUsername();
            String rawMessage = buildMimeMessage();

            if (dryRun) {
                EmailResult r = new EmailResult(true,
                        "Dry run — message built, not sent",
                        System.currentTimeMillis() - start,
                        rawMessage, allRecipients(), List.of());
                notifyComplete(r);
                return r;
            }

            if (loaderEnabled != null ? loaderEnabled : Logger.hasConsoleSink()) {
                loader = Loader.start("Sending email to " + allRecipients().size() + " recipient(s)");
            }

            notifyConnect();

            List<String> smtpLog = new ArrayList<>();
            SmtpClient client = new SmtpClient(host, port, ssl, starttls,
                    resolvedUsername != null ? resolvedUsername : from,
                    resolvedPassword, verbose ? listener : null, smtpLog);

            try {
                client.send(from, allRecipients(), rawMessage);
            } catch (java.io.IOException e) {
                // SmtpClient declares `throws IOException`; wrap it so the outer
                // catch chain sees a single EmailException category and the caller
                // gets a precise TRANSPORT reason.
                throw EmailException.transport("SMTP transport failed: " + e.getMessage(), e);
            } finally {
                notifyDisconnect();
            }

            if (loader != null) loader.success("Email sent");

            EmailResult r = new EmailResult(true,
                    "Sent to " + allRecipients().size() + " recipient(s)",
                    System.currentTimeMillis() - start,
                    rawMessage, allRecipients(), List.copyOf(smtpLog));
            notifyDelivered(r);
            notifyComplete(r);
            return r;

        } catch (EmailException e) {
            if (loader != null) loader.error(e.getMessage());
            notifyError(e);
            if (throwOnFailure) throw e;
            return new EmailResult(false, e.getMessage(),
                    System.currentTimeMillis() - start, "", allRecipients(), List.of());
        } catch (RuntimeException e) {
            if (loader != null) loader.error(e.getMessage());
            notifyError(e);
            if (throwOnFailure) throw new EmailException("Send failed: " + e.getMessage(), e);
            return new EmailResult(false, "Send failed: " + e.getMessage(),
                    System.currentTimeMillis() - start, "", allRecipients(), List.of());
        } finally {
            if (loader != null) loader.stop();
        }
    }
    public EmailResult deliver() { return send(); }
    public EmailResult dispatch() { return send(); }

    public EmailResult sendOrThrow() { return doSend(true); }
    public EmailResult deliverOrThrow() { return sendOrThrow(); }

    /** Retries {@code send()} up to N times on failure, with 500ms backoff. */
    public EmailResult sendWithRetry(int attempts) {
        return sendWithRetry(attempts, Duration.ofMillis(500));
    }

    /** Retries with a custom delay. */
    public EmailResult sendWithRetry(int attempts, Duration delay) {
        int maxAttempts = Math.max(1, attempts);
        Duration d = delay == null || delay.isNegative() ? Duration.ZERO : delay;
        EmailResult last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            last = send();
            if (last.isSuccess()) return last;
            if (i < maxAttempts) {
                notifyRetry(i, new EmailException(last.message()));
                if (!d.isZero()) {
                    try {
                        Thread.sleep(d.toMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return last;
    }
    public EmailResult retry(int attempts) { return sendWithRetry(attempts); }
    public EmailResult sendWithRetries(int attempts) { return sendWithRetry(attempts); }

    /** Returns the built MIME message as a string — throws if config is invalid. */
    public String previewMimeMessage() {
        validate();
        return buildMimeMessage();
    }
    public String mimeMessage() { return previewMimeMessage(); }
    public String asMime() { return previewMimeMessage(); }

    /** Returns the built MIME message or empty if config is invalid. */
    public Optional<String> previewIfValid() {
        try {
            validate();
            return Optional.of(buildMimeMessage());
        } catch (EmailException e) {
            return Optional.empty();
        }
    }

    // ============================================================== listener notifications

    private void notifyStart() {
        if (listener == null) return;
        try {
            listener.onStart(this);
        } catch (RuntimeException e) {
            Trace.warn("email", "onStart listener threw: " + e.getMessage());
        }
    }
    private void notifyConnect() {
        if (listener == null) return;
        try {
            listener.onConnect(host, port);
        } catch (RuntimeException e) {
            Trace.warn("email", "onConnect listener threw: " + e.getMessage());
        }
    }
    private void notifyDisconnect() {
        if (listener == null) return;
        try {
            listener.onDisconnect();
        } catch (RuntimeException e) {
            Trace.warn("email", "onDisconnect listener threw: " + e.getMessage());
        }
    }
    private void notifyDelivered(EmailResult r) {
        if (listener == null) return;
        try {
            listener.onDelivered(r);
        } catch (RuntimeException e) {
            Trace.warn("email", "onDelivered listener threw: " + e.getMessage());
        }
    }
    private void notifyComplete(EmailResult r) {
        if (listener == null) return;
        try {
            listener.onComplete(r);
        } catch (RuntimeException e) {
            Trace.warn("email", "onComplete listener threw: " + e.getMessage());
        }
    }
    private void notifyError(Exception error) {
        if (listener == null) return;
        try {
            listener.onError(error);
        } catch (RuntimeException e) {
            Trace.warn("email", "onError listener threw: " + e.getMessage());
        }
    }
    private void notifyRetry(int attempt, Exception cause) {
        if (listener == null) return;
        try {
            listener.onRetry(attempt, cause);
        } catch (RuntimeException e) {
            Trace.warn("email", "onRetry listener threw: " + e.getMessage());
        }
    }

    // ============================================================== SMTP connectivity test

    /**
     * Opens a TCP socket to {@code (host, port)} to verify connectivity.
     * Does <b>not</b> verify credentials — that requires a full SMTP session.
     */
    public boolean test() {
        if (host == null || port <= 0) return false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeout.toMillis());
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }
    public boolean canConnect() { return test(); }
    public boolean testConnection() { return test(); }

    public void testOrThrow() {
        validate();
        if (!test()) {
            throw new EmailException(EmailException.Reason.TRANSPORT,
                    "Cannot reach SMTP server at " + host + ":" + port);
        }
    }

    // ============================================================== validation

    private void validate() {
        if (host == null || host.isBlank())
            throw EmailException.missingConfig("SMTP host is required");
        if (from == null || from.isBlank())
            throw EmailException.missingConfig("Sender (from) is required");
        if (allRecipients().isEmpty())
            throw EmailException.missingConfig("At least one recipient (to) is required");
        if (!Check.isEmail(from))
            throw EmailException.validation("From is not a valid email: " + from);

        if (port <= 0) {
            port = ssl ? 465 : 587;
            if (!ssl && !starttls) starttls = true;
        }
        if (port == 465 && !ssl) ssl = true;
        if (port == 587 && !ssl && !starttls) starttls = true;

        if (text == null && html == null && htmlTemplate == null) {
            throw EmailException.missingConfig(
                    "Email body is required — set .text(...), .html(...), or .template(...)");
        }

        if (strategy == null) strategy = PasswordStrategy.ENV;
    }

    // ============================================================== password resolution

    private String resolvePassword() {
        return switch (strategy) {
            case DIRECT -> {
                if (password == null || password.isBlank()) {
                    throw EmailException.missingConfig(
                            "DIRECT strategy requires a password — call .password(\"...\")");
                }
                warnDirectPassword();
                yield password;
            }
            case PROMPT -> promptForPassword();
            case ENV -> {
                if (envKey == null || envKey.isBlank()) {
                    throw EmailException.missingConfig(
                            "ENV strategy requires a key — call .passwordFromEnv(\".env\", \"SMTP_PASSWORD\")");
                }
                String path = (envPath == null || envPath.isBlank()) ? ".env" : envPath;
                yield EnvLoader.get(path, envKey);
            }
        };
    }

    private String resolveUsername() {
        if (usernameEnvPath != null && usernameEnvKey != null) {
            try {
                return EnvLoader.get(usernameEnvPath, usernameEnvKey);
            } catch (EmailException e) {
                Trace.warn("email", "usernameFromEnv failed, falling back to explicit username: " + e.getMessage());
                return username;
            }
        }
        return username;
    }

    private void warnDirectPassword() {
        try {
            Console.warn("DIRECT password is discouraged — consider ENV or PROMPT");
        } catch (chirp.ChirpNotStartedException e) {
            // Before Chirp.start() — that's fine, warnDirectPassword is a hint.
            Trace.debug("email", "Console not started yet; skipping DIRECT-password warning");
        } catch (RuntimeException e) {
            Trace.warn("email", "DIRECT-password warning failed: " + e.getMessage());
        }
    }

    private String promptForPassword() {
        try {
            char[] chars = System.console() == null
                    ? null
                    : System.console().readPassword("SMTP password: ");
            if (chars != null) return new String(chars).trim();
        } catch (RuntimeException e) {
            Trace.warn("email", "console.readPassword failed, falling back to stdin: " + e.getMessage());
        }
        try {
            return chirp.console.Input.readRaw("SMTP password").trim();
        } catch (RuntimeException e) {
            throw EmailException.missingConfig("Cannot prompt for password: " + e.getMessage());
        }
    }

    // ============================================================== MIME builder

    private String buildMimeMessage() {
        String boundary = "CHIRP_" + UUID.randomUUID().toString().replace("-", "");

        StringBuilder sb = new StringBuilder();
        header(sb, "Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        header(sb, "From", formatAddress(from, senderName));
        header(sb, "To", formatAddresses(to));
        if (!cc.isEmpty()) header(sb, "Cc", formatAddresses(cc));
        if (replyTo != null) header(sb, "Reply-To", formatAddress(replyTo, replyToName));
        if (bounceTo != null) header(sb, "Return-Path", "<" + bounceTo + ">");
        if (priority != 3) header(sb, "X-Priority", String.valueOf(priority));
        for (Map.Entry<String, String> e : extraHeaders.entrySet()) {
            header(sb, e.getKey(), e.getValue());
        }
        header(sb, "Subject", encodeSubject(subject));
        header(sb, "Message-ID", "<" + UUID.randomUUID() + "@chirp.local>");
        header(sb, "MIME-Version", "1.0");

        String finalHtml = resolveHtml();
        String finalText = resolveText();
        boolean hasAttachments = !attachments.isEmpty();
        boolean hasBothBodies = finalText != null && finalHtml != null;

        if (hasAttachments) {
            header(sb, "Content-Type", "multipart/mixed; boundary=\"" + boundary + "\"");
            sb.append("\r\n");
            appendBodyPart(sb, boundary, finalHtml, finalText, hasBothBodies);
            for (Attachment a : attachments) appendAttachmentPart(sb, boundary, a);
            sb.append("--").append(boundary).append("--\r\n");
        } else if (hasBothBodies) {
            header(sb, "Content-Type", "multipart/alternative; boundary=\"" + boundary + "\"");
            sb.append("\r\n");
            appendPart(sb, boundary, "text/plain; charset=\"UTF-8\"", finalText);
            appendPart(sb, boundary, "text/html; charset=UTF-8", finalHtml);
            sb.append("--").append(boundary).append("--\r\n");
        } else {
            String body = finalHtml != null ? finalHtml : finalText;
            String type = finalHtml != null ? "text/html" : "text/plain";
            header(sb, "Content-Type", type + "; charset=\"UTF-8\"");
            header(sb, "Content-Transfer-Encoding", "8bit");
            sb.append("\r\n").append(body == null ? "" : body);
        }
        return sb.toString();
    }

    private String resolveHtml() {
        String base = htmlTemplate != null ? htmlTemplate.render() : html;
        if (base == null) return null;
        if (signature == null || signature.isBlank()) return base;
        String sigHtml = signature.replace("\n", "<br>");
        return base + "\n<br><br>--\n<p>" + sigHtml + "</p>";
    }

    private String resolveText() {
        if (text == null) return null;
        if (signature == null || signature.isBlank()) return text;
        return text + "\n\n--\n" + signature;
    }

    private void appendBodyPart(StringBuilder sb, String boundary, String finalHtml,
            String finalText, boolean hasBothBodies) {
        if (hasBothBodies) {
            String innerBoundary = boundary + "_alt";
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Type: multipart/alternative; boundary=\"")
                    .append(innerBoundary).append("\"\r\n\r\n");
            appendPart(sb, innerBoundary, "text/plain; charset=UTF-8", finalText);
            appendPart(sb, innerBoundary, "text/html; charset=UTF-8", finalHtml);
            sb.append("--").append(innerBoundary).append("--\r\n");
        } else {
            String body = finalHtml != null ? finalHtml : finalText;
            String type = finalHtml != null ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8";
            appendPart(sb, boundary, type, body);
        }
    }

    private void appendPart(StringBuilder sb, String boundary, String contentType, String content) {
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
        sb.append(content == null ? "" : content).append("\r\n");
    }

    private void appendAttachmentPart(StringBuilder sb, String boundary, Attachment a) {
        String encoded = Base64.getMimeEncoder().encodeToString(a.bytes());
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Type: application/octet-stream\r\n");
        sb.append("Content-Transfer-Encoding: base64\r\n");
        sb.append("Content-Disposition: attachment; filename=\"").append(a.filename()).append("\"\r\n\r\n");
        sb.append(encoded).append("\r\n");
    }

    private static void header(StringBuilder sb, String name, String value) {
        String safe = value == null ? "" : value.replaceAll("[\\r\\n]", " ");
        sb.append(name).append(": ").append(safe).append("\r\n");
    }

    private static String formatAddress(String email, String name) {
        if (name == null || name.isBlank()) return "<" + email + ">";
        return encodeHeader(name) + " <" + email + ">";
    }

    private static String formatAddresses(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addresses.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("<").append(addresses.get(i)).append(">");
        }
        return sb.toString();
    }

    private static String encodeSubject(String s) {
        if (s == null) return "";
        boolean ascii = s.chars().allMatch(c -> c >= 0x20 && c < 0x7F);
        if (ascii) return s.replaceAll("[\\r\\n]", " ");
        String encoded = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + encoded + "?=";
    }

    private static String encodeHeader(String text) {
        boolean ascii = text.chars().allMatch(c -> c >= 0x20 && c < 0x7F);
        if (ascii) return "\"" + text.replace("\"", "\\\"") + "\"";
        String encoded = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + encoded + "?=";
    }

    private List<String> allRecipients() {
        List<String> all = new ArrayList<>(to);
        all.addAll(cc);
        all.addAll(bcc);
        return all;
    }

    @Override
    public String toString() {
        return "EmailRequest[" + host + ":" + port + " from=" + from + " to=" + to.size() + "]";
    }
}
