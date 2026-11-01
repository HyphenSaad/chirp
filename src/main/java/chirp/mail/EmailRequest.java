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
 * Fluent email request — configure once, send once.
 *
 * <p>Every configuration method returns {@code this}, so a request is
 * configured by chaining, then dispatched with {@link #send()} or one of
 * its variants. A request is <b>mutable</b> — reusing the same builder
 * for two sends reuses the accumulated configuration.</p>
 *
 * <h2>The typical send</h2>
 * <pre>{@code
 * EmailResult result = Email.builder()
 *     .host("smtp.gmail.com")                    // server
 *     .from("me@gmail.com")                      // sender
 *     .to("you@example.com")                     // recipient(s)
 *     .subject("Hello")                          // subject
 *     .text("Hi there!")                         // body
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")  // credentials
 *     .send();
 *
 * if (result.isSuccess()) {
 *     System.out.println("Sent in " + result.durationMs() + "ms");
 * } else {
 *     System.out.println("Failed: " + result.message());
 * }
 * }</pre>
 *
 * <h2>The four ways to send</h2>
 * <table border="1">
 *   <caption>Choose a send variant based on your error-handling style</caption>
 *   <tr><th>Method</th><th>On failure</th><th>Use when</th></tr>
 *   <tr><td>{@link #send()}</td>
 *       <td>Returns an {@link EmailResult} with {@code success=false}</td>
 *       <td>Standard — the caller checks the result</td></tr>
 *   <tr><td>{@link #sendOrThrow()}</td>
 *       <td>Throws {@link EmailException}</td>
 *       <td>Inside a stream pipeline or a method that can't return
 *           an error value</td></tr>
 *   <tr><td>{@link #sendWithRetry(int)}</td>
 *       <td>Retries then returns the last result</td>
 *       <td>Transient network conditions</td></tr>
 *   <tr><td>{@link #previewMimeMessage()}</td>
 *       <td>Throws on config error, otherwise returns the MIME</td>
 *       <td>Dry-run / debugging — never touches the network</td></tr>
 * </table>
 *
 * <h2>Password strategies</h2>
 * <p>See {@link PasswordStrategy} for the full comparison. In order of
 * preference:</p>
 * <ol>
 *   <li>{@link #passwordFromEnv(String, String) ENV} — read from a
 *       {@code .env} file (recommended)</li>
 *   <li>{@link #promptPassword() PROMPT} — ask the user at send time</li>
 *   <li>{@link #password(String) DIRECT} — in source code (discouraged;
 *       prints a warning)</li>
 * </ol>
 *
 * <h2>Attachments</h2>
 * <p>Use {@link #attach(String)} for a single file,
 * {@link #attachMany(List)} for several paths, or
 * {@link #attachDirectory(String)} for every regular file in a
 * directory. The MIME message is automatically built as
 * {@code multipart/mixed} when attachments are present. The filename
 * in the {@code Content-Disposition} header preserves the source file's
 * name; the path is not exposed.</p>
 *
 * <h2>HTML vs plain text</h2>
 * <p>If you set both {@link #text(String)} and {@link #html(String)},
 * the MIME message is built as {@code multipart/alternative} with both
 * parts — the recipient's email client chooses which to display (most
 * modern clients prefer HTML). If you set only one, the message is a
 * single-part {@code text/plain} or {@code text/html}.</p>
 *
 * <p>{@link #template(String)} (or {@link #htmlTemplate(HtmlTemplate)})
 * is a convenient way to load an HTML template from a file and render
 * it with placeholders — see {@link HtmlTemplate} for the placeholder
 * syntax.</p>
 *
 * <h2>Signatures</h2>
 * <p>{@link #signature(String)} appends a fixed signature to the body.
 * {@link #signatureFromSession()} builds a signature from the
 * {@link chirp.session.Session} — the student's name, roll number,
 * degree, and email. It is empty before any Session fields are set.</p>
 *
 * <h2>Listener notifications</h2>
 * <p>Attach a listener with {@link #listener(EmailListener)} to observe
 * the send lifecycle. See {@link EmailListener} for the full hook list.
 * Listener exceptions are logged and swallowed — a broken listener never
 * breaks the send.</p>
 *
 * <h2>Retries</h2>
 * <p>{@link #sendWithRetry(int)} retries on any failure, waiting 500ms
 * between attempts. Use {@link #sendWithRetry(int, Duration)} for a
 * custom delay. To distinguish retryable from non-retryable failures,
 * use {@link #sendOrThrow()} and check
 * {@link EmailException#isTransportError()}.</p>
 *
 * <h2>Dry run — the "did I configure this correctly?" mode</h2>
 * <p>{@link #dryRun(boolean)} (or {@link #preview()}) builds the full
 * MIME message but does <b>not</b> connect to the SMTP server. Use it
 * to check that the configuration is complete, then call
 * {@link #send()} for the real send:</p>
 * <pre>{@code
 * EmailResult preview = Email.builder()
 *     .host("smtp.gmail.com")
 *     .from("me@gmail.com")
 *     .to("you@example.com")
 *     .subject("Hello")
 *     .text("Hi!")
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")
 *     .preview()
 *     .send();
 *
 * if (preview.isSuccess()) {
 *     System.out.println("Configuration OK");
 *     System.out.println(preview.rawMessage());   // inspect the MIME
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the facade that opens builders, see {@link Email}. For the
 * outcome record, see {@link EmailResult}. For the exception, see
 * {@link EmailException}. For the .env reader, see {@link EnvLoader}.
 * For the template engine, see {@link HtmlTemplate}.</p>
 *
 * @see Email
 * @see EmailResult
 * @see EmailException
 * @see EmailListener
 * @see PasswordStrategy
 * @see EnvLoader
 * @see HtmlTemplate
 * @see SmtpClient
 * @since 1.0.0
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

    /**
     * Returns the configured SMTP host, or null if not set.
     *
     * @return the host, or null
     * @since 1.0.0
     */
    public String host() { return host; }

    /**
     * Returns the configured SMTP port, or {@code -1} if not set
     * (which means "pick the default for the transport").
     *
     * @return the port, or -1
     * @since 1.0.0
     */
    public int port() { return port; }

    /**
     * Checks whether implicit TLS (SMTPS) is enabled.
     *
     * @return true if SSL is enabled
     * @since 1.0.0
     */
    public boolean ssl() { return ssl; }

    /**
     * Checks whether STARTTLS upgrade is enabled.
     *
     * @return true if STARTTLS is enabled
     * @since 1.0.0
     */
    public boolean starttls() { return starttls; }

    /**
     * Returns the configured sender address, or null.
     *
     * @return the sender, or null
     * @since 1.0.0
     */
    public String from() { return from; }

    /**
     * Returns an unmodifiable copy of the recipient list.
     *
     * @return the To recipients
     * @since 1.0.0
     */
    public List<String> to() { return List.copyOf(to); }

    /**
     * Returns an unmodifiable copy of the CC list.
     *
     * @return the CC recipients
     * @since 1.0.0
     */
    public List<String> cc() { return List.copyOf(cc); }

    /**
     * Returns an unmodifiable copy of the BCC list.
     *
     * @return the BCC recipients
     * @since 1.0.0
     */
    public List<String> bcc() { return List.copyOf(bcc); }

    /**
     * Returns the configured subject, or empty string.
     *
     * @return the subject
     * @since 1.0.0
     */
    public String subject() { return subject; }

    /**
     * Returns the plain-text body, or null.
     *
     * @return the text body, or null
     * @since 1.0.0
     */
    public String text() { return text; }

    /**
     * Returns the HTML body, or null.
     *
     * @return the HTML body, or null
     * @since 1.0.0
     */
    public String html() { return html; }

    /**
     * Returns the configured password strategy, or null if not yet set.
     *
     * @return the strategy, or null
     * @since 1.0.0
     */
    public PasswordStrategy strategy() { return strategy; }

    /**
     * Checks whether dry-run mode is enabled.
     *
     * @return true if this is a dry run
     * @since 1.0.0
     */
    public boolean dryRun() { return dryRun; }

    /**
     * Checks whether verbose mode is enabled.
     *
     * @return true if verbose
     * @since 1.0.0
     */
    public boolean verbose() { return verbose; }

    /**
     * Returns the attached listener, or null.
     *
     * @return the listener, or null
     * @since 1.0.0
     */
    public EmailListener listener() { return listener; }

    /**
     * Returns the configured environment file path for the password, or
     * null if using a non-ENV strategy.
     *
     * @return the .env path, or null
     * @since 1.0.0
     */
    public String envPath() { return envPath; }

    /**
     * Returns the configured environment key for the password, or null
     * if using a non-ENV strategy.
     *
     * @return the .env key, or null
     * @since 1.0.0
     */
    public String envKey() { return envKey; }

    // ============================================================== SMTP server

    /**
     * Sets the SMTP server hostname.
     *
     * <pre>{@code
     * Email.builder().host("smtp.gmail.com")
     * }</pre>
     *
     * @param host the hostname
     * @return this request
     * @see #server(String)
     * @see #smtpHost(String)
     * @see #mailServer(String)
     * @since 1.0.0
     */
    public EmailRequest host(String host) { this.host = host; return this; }

    /**
     * Alias of {@link #host(String)} — see that method for full
     * documentation and examples.
     *
     * @param host the hostname
     * @return this request
     * @see #host(String)
     * @since 1.0.0
     */
    public EmailRequest server(String host) { return host(host); }

    /**
     * Alias of {@link #host(String)} — see that method for full
     * documentation and examples.
     *
     * @param host the hostname
     * @return this request
     * @see #host(String)
     * @since 1.0.0
     */
    public EmailRequest smtpHost(String host) { return host(host); }

    /**
     * Alias of {@link #host(String)} — see that method for full
     * documentation and examples.
     *
     * @param host the hostname
     * @return this request
     * @see #host(String)
     * @since 1.0.0
     */
    public EmailRequest mailServer(String host) { return host(host); }

    /**
     * Sets the SMTP port explicitly. If not set, {@link #send()} picks
     * 465 when SSL is enabled and 587 otherwise.
     *
     * @param port the SMTP port
     * @return this request
     * @see #smtpPort(int)
     * @since 1.0.0
     */
    public EmailRequest port(int port) { this.port = port; return this; }

    /**
     * Alias of {@link #port(int)} — see that method for full
     * documentation.
     *
     * @param port the SMTP port
     * @return this request
     * @see #port(int)
     * @since 1.0.0
     */
    public EmailRequest smtpPort(int port) { return port(port); }

    /**
     * Enables or disables implicit TLS (SMTPS). When enabled and no
     * port is set, port 465 is used.
     *
     * @param enabled whether to use SSL
     * @return this request
     * @see #useSsl()
     * @see #withSsl()
     * @see #starttls(boolean)
     * @since 1.0.0
     */
    public EmailRequest ssl(boolean enabled) { this.ssl = enabled; return this; }

    /**
     * Alias of {@link #ssl(boolean)} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #ssl(boolean)
     * @since 1.0.0
     */
    public EmailRequest useSsl() { return ssl(true); }

    /**
     * Alias of {@link #ssl(boolean)} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #ssl(boolean)
     * @since 1.0.0
     */
    public EmailRequest withSsl() { return ssl(true); }

    /**
     * Enables or disables STARTTLS upgrade. When enabled and no port is
     * set, port 587 is used.
     *
     * @param enabled whether to use STARTTLS
     * @return this request
     * @see #useStarttls()
     * @see #withStarttls()
     * @see #ssl(boolean)
     * @since 1.0.0
     */
    public EmailRequest starttls(boolean enabled) { this.starttls = enabled; return this; }

    /**
     * Alias of {@link #starttls(boolean)} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #starttls(boolean)
     * @since 1.0.0
     */
    public EmailRequest useStarttls() { return starttls(true); }

    /**
     * Alias of {@link #starttls(boolean)} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #starttls(boolean)
     * @since 1.0.0
     */
    public EmailRequest withStarttls() { return starttls(true); }

    // ============================================================== sender

    /**
     * Sets the sender (From) address. Required — send fails without it.
     *
     * @param address the sender email
     * @return this request
     * @see #sender(String)
     * @see #fromAddress(String)
     * @see #senderName(String)
     * @since 1.0.0
     */
    public EmailRequest from(String address) { this.from = address; return this; }

    /**
     * Alias of {@link #from(String)} — see that method for full
     * documentation.
     *
     * @param address the sender email
     * @return this request
     * @see #from(String)
     * @since 1.0.0
     */
    public EmailRequest sender(String address) { return from(address); }

    /**
     * Alias of {@link #from(String)} — see that method for full
     * documentation.
     *
     * @param address the sender email
     * @return this request
     * @see #from(String)
     * @since 1.0.0
     */
    public EmailRequest fromAddress(String address) { return from(address); }

    /**
     * Sets a display name for the sender. Appears as
     * {@code "Display Name <address>"} in email clients.
     *
     * @param name the display name (may be null)
     * @return this request
     * @see #fromName(String)
     * @see #senderDisplayName(String)
     * @since 1.0.0
     */
    public EmailRequest senderName(String name) { this.senderName = name; return this; }

    /**
     * Alias of {@link #senderName(String)} — see that method for full
     * documentation.
     *
     * @param name the display name
     * @return this request
     * @see #senderName(String)
     * @since 1.0.0
     */
    public EmailRequest fromName(String name) { return senderName(name); }

    /**
     * Alias of {@link #senderName(String)} — see that method for full
     * documentation.
     *
     * @param name the display name
     * @return this request
     * @see #senderName(String)
     * @since 1.0.0
     */
    public EmailRequest senderDisplayName(String name) { return senderName(name); }

    /**
     * Sets the SMTP username explicitly. If not set, the from address is
     * used as the username.
     *
     * @param user the username
     * @return this request
     * @see #usernameFromEnv(String, String)
     * @since 1.0.0
     */
    public EmailRequest username(String user) { this.username = user; return this; }

    /**
     * Alias of {@link #username(String)} — see that method for full
     * documentation.
     *
     * @param user the username
     * @return this request
     * @see #username(String)
     * @since 1.0.0
     */
    public EmailRequest user(String user) { return username(user); }

    /**
     * Reads the SMTP username from a {@code .env} file at send time.
     *
     * @param path the .env file path
     * @param key the key containing the username
     * @return this request
     * @see #username(String)
     * @see #envUsername(String, String)
     * @see EnvLoader
     * @since 1.0.0
     */
    public EmailRequest usernameFromEnv(String path, String key) {
        this.usernameEnvPath = path;
        this.usernameEnvKey = key;
        return this;
    }

    /**
     * Alias of {@link #usernameFromEnv(String, String)} — see that method
     * for full documentation.
     *
     * @param path the .env path
     * @param key the .env key
     * @return this request
     * @see #usernameFromEnv(String, String)
     * @since 1.0.0
     */
    public EmailRequest envUsername(String path, String key) { return usernameFromEnv(path, key); }

    // ============================================================== recipients

    /**
     * Adds one To recipient. Multiple calls accumulate — each additional
     * call adds another recipient, not a replacement.
     *
     * @param address the recipient email (null → no-op)
     * @return this request
     * @see #to(String...)
     * @see #to(List)
     * @see #cc(String)
     * @see #bcc(String)
     * @since 1.0.0
     */
    public EmailRequest to(String address) { if (address != null) to.add(address); return this; }

    /**
     * Adds multiple To recipients.
     *
     * @param addresses the recipient emails (null → no-op)
     * @return this request
     * @see #to(String)
     * @since 1.0.0
     */
    public EmailRequest to(String... addresses) {
        if (addresses != null) for (String a : addresses) to(a);
        return this;
    }

    /**
     * Adds all To recipients from a list.
     *
     * @param addresses the list of recipients (null → no-op)
     * @return this request
     * @see #to(String)
     * @since 1.0.0
     */
    public EmailRequest to(List<String> addresses) { if (addresses != null) to.addAll(addresses); return this; }

    /**
     * Alias of {@link #to(String)} — see that method for full
     * documentation.
     *
     * @param address the recipient email
     * @return this request
     * @see #to(String)
     * @since 1.0.0
     */
    public EmailRequest recipient(String address) { return to(address); }

    /**
     * Alias of {@link #to(String)} — see that method for full
     * documentation.
     *
     * @param address the recipient email
     * @return this request
     * @see #to(String)
     * @since 1.0.0
     */
    public EmailRequest addRecipient(String address) { return to(address); }

    /**
     * Adds one CC recipient.
     *
     * @param address the CC email (null → no-op)
     * @return this request
     * @see #to(String)
     * @see #bcc(String)
     * @since 1.0.0
     */
    public EmailRequest cc(String address) { if (address != null) cc.add(address); return this; }

    /**
     * Adds multiple CC recipients.
     *
     * @param addresses the CC emails (null → no-op)
     * @return this request
     * @see #cc(String)
     * @since 1.0.0
     */
    public EmailRequest cc(String... addresses) {
        if (addresses != null) for (String a : addresses) cc(a);
        return this;
    }

    /**
     * Adds one BCC recipient. BCC recipients are not visible to other
     * recipients (the {@code Bcc:} header is not included in the
     * transmitted MIME message, but they do receive the message via
     * SMTP RCPT).
     *
     * @param address the BCC email (null → no-op)
     * @return this request
     * @see #to(String)
     * @see #cc(String)
     * @since 1.0.0
     */
    public EmailRequest bcc(String address) { if (address != null) bcc.add(address); return this; }

    /**
     * Adds multiple BCC recipients.
     *
     * @param addresses the BCC emails (null → no-op)
     * @return this request
     * @see #bcc(String)
     * @since 1.0.0
     */
    public EmailRequest bcc(String... addresses) {
        if (addresses != null) for (String a : addresses) bcc(a);
        return this;
    }

    /**
     * Sets the Reply-To address — where replies go when the recipient
     * clicks "Reply". Defaults to the from address if not set.
     *
     * @param address the Reply-To email
     * @return this request
     * @see #replyToName(String)
     * @since 1.0.0
     */
    public EmailRequest replyTo(String address) { this.replyTo = address; return this; }

    /**
     * Sets a display name for the Reply-To address.
     *
     * @param name the display name
     * @return this request
     * @see #replyTo(String)
     * @see #replyToDisplayName(String)
     * @since 1.0.0
     */
    public EmailRequest replyToName(String name) { this.replyToName = name; return this; }

    /**
     * Alias of {@link #replyToName(String)} — see that method for full
     * documentation.
     *
     * @param n the display name
     * @return this request
     * @see #replyToName(String)
     * @since 1.0.0
     */
    public EmailRequest replyToDisplayName(String n) { return replyToName(n); }

    /**
     * Sets the Return-Path (bounce) address. Bounce notifications are
     * sent here when the recipient cannot receive the message.
     *
     * @param address the bounce email
     * @return this request
     * @see #returnPath(String)
     * @since 1.0.0
     */
    public EmailRequest bounceTo(String address) { this.bounceTo = address; return this; }

    /**
     * Alias of {@link #bounceTo(String)} — see that method for full
     * documentation.
     *
     * @param address the bounce email
     * @return this request
     * @see #bounceTo(String)
     * @since 1.0.0
     */
    public EmailRequest returnPath(String address) { return bounceTo(address); }

    // ============================================================== priority

    /**
     * Sets the {@code X-Priority} header — 1 (highest) to 5 (lowest).
     * Values outside the range are clamped.
     *
     * @param level the priority level
     * @return this request
     * @see #highPriority()
     * @see #normalPriority()
     * @see #lowPriority()
     * @since 1.0.0
     */
    public EmailRequest priority(int level) {
        this.priority = Math.max(1, Math.min(5, level));
        return this;
    }

    /**
     * Alias of {@link #priority(int)} with level 1 (highest).
     *
     * @return this request
     * @see #priority(int)
     * @since 1.0.0
     */
    public EmailRequest highPriority() { return priority(1); }

    /**
     * Alias of {@link #priority(int)} with level 3 (normal — the default).
     *
     * @return this request
     * @see #priority(int)
     * @since 1.0.0
     */
    public EmailRequest normalPriority() { return priority(3); }

    /**
     * Alias of {@link #priority(int)} with level 5 (lowest).
     *
     * @return this request
     * @see #priority(int)
     * @since 1.0.0
     */
    public EmailRequest lowPriority() { return priority(5); }

    // ============================================================== extra headers

    /**
     * Adds a custom MIME header. Useful for {@code X-...} headers or
     * for domain-specific metadata.
     *
     * @param name the header name (null → no-op)
     * @param value the header value (null → no-op)
     * @return this request
     * @see #addHeader(String, String)
     * @see #withHeader(String, String)
     * @since 1.0.0
     */
    public EmailRequest header(String name, String value) {
        if (name != null && value != null) extraHeaders.put(name, value);
        return this;
    }

    /**
     * Alias of {@link #header(String, String)} — see that method for
     * full documentation.
     *
     * @param name the header name
     * @param value the header value
     * @return this request
     * @see #header(String, String)
     * @since 1.0.0
     */
    public EmailRequest addHeader(String name, String value) { return header(name, value); }

    /**
     * Alias of {@link #header(String, String)} — see that method for
     * full documentation.
     *
     * @param name the header name
     * @param value the header value
     * @return this request
     * @see #header(String, String)
     * @since 1.0.0
     */
    public EmailRequest withHeader(String name, String value) { return header(name, value); }

    // ============================================================== subject

    /**
     * Sets the email subject. Non-ASCII characters are automatically
     * encoded as {@code =?UTF-8?B?...?=} in the MIME header.
     *
     * @param s the subject (null → empty)
     * @return this request
     * @see #title(String)
     * @since 1.0.0
     */
    public EmailRequest subject(String s) { this.subject = s == null ? "" : s; return this; }

    /**
     * Alias of {@link #subject(String)} — see that method for full
     * documentation.
     *
     * @param s the subject
     * @return this request
     * @see #subject(String)
     * @since 1.0.0
     */
    public EmailRequest title(String s) { return subject(s); }

    // ============================================================== body

    /**
     * Sets the plain-text body. If you also set {@link #html(String)},
     * the MIME message is built as {@code multipart/alternative} with
     * both parts.
     *
     * @param body the plain-text body
     * @return this request
     * @see #body(String)
     * @see #plain(String)
     * @see #bodyText(String)
     * @see #html(String)
     * @since 1.0.0
     */
    public EmailRequest text(String body) { this.text = body; return this; }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param body the body
     * @return this request
     * @see #text(String)
     * @since 1.0.0
     */
    public EmailRequest body(String body) { return text(body); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param body the body
     * @return this request
     * @see #text(String)
     * @since 1.0.0
     */
    public EmailRequest plain(String body) { return text(body); }

    /**
     * Alias of {@link #text(String)} — see that method for full
     * documentation.
     *
     * @param body the body
     * @return this request
     * @see #text(String)
     * @since 1.0.0
     */
    public EmailRequest bodyText(String body) { return text(body); }

    /**
     * Sets the HTML body. If you also set {@link #text(String)}, the
     * MIME message is built as {@code multipart/alternative} with both
     * parts — the recipient's client chooses which to display.
     *
     * @param body the HTML body
     * @return this request
     * @see #htmlBody(String)
     * @see #bodyHtml(String)
     * @see #text(String)
     * @see #template(String)
     * @since 1.0.0
     */
    public EmailRequest html(String body) { this.html = body; return this; }

    /**
     * Alias of {@link #html(String)} — see that method for full
     * documentation.
     *
     * @param body the HTML body
     * @return this request
     * @see #html(String)
     * @since 1.0.0
     */
    public EmailRequest htmlBody(String body) { return html(body); }

    /**
     * Alias of {@link #html(String)} — see that method for full
     * documentation.
     *
     * @param body the HTML body
     * @return this request
     * @see #html(String)
     * @since 1.0.0
     */
    public EmailRequest bodyHtml(String body) { return html(body); }

    /**
     * Sets an {@link HtmlTemplate} as the HTML body. The template is
     * rendered at send time — placeholders must be declared first via
     * {@link HtmlTemplate#placeholder(String, Object)}.
     *
     * @param t the template (null → no-op)
     * @return this request
     * @see #template(HtmlTemplate)
     * @see #template(String)
     * @see HtmlTemplate
     * @since 1.0.0
     */
    public EmailRequest htmlTemplate(HtmlTemplate t) { this.htmlTemplate = t; return this; }

    /**
     * Alias of {@link #htmlTemplate(HtmlTemplate)} — see that method for
     * full documentation.
     *
     * @param t the template
     * @return this request
     * @see #htmlTemplate(HtmlTemplate)
     * @since 1.0.0
     */
    public EmailRequest template(HtmlTemplate t) { return htmlTemplate(t); }

    /**
     * Loads an HTML template from a file. Equivalent to
     * {@code htmlTemplate(HtmlTemplate.of(path))}.
     *
     * @param path the template file path
     * @return this request
     * @see #htmlTemplate(HtmlTemplate)
     * @see HtmlTemplate#of(String)
     * @since 1.0.0
     */
    public EmailRequest template(String path) { return htmlTemplate(HtmlTemplate.of(path)); }

    // ============================================================== signature

    /**
     * Sets a signature that is appended to the body. For plain text, it
     * is separated by {@code "\n\n--\n"}; for HTML, it is rendered as
     * an HTML paragraph after a horizontal separator.
     *
     * @param sig the signature text (null → no-op)
     * @return this request
     * @see #appendSignature(String)
     * @see #signatureFromSession()
     * @since 1.0.0
     */
    public EmailRequest signature(String sig) { this.signature = sig; return this; }

    /**
     * Alias of {@link #signature(String)} — see that method for full
     * documentation.
     *
     * @param sig the signature text
     * @return this request
     * @see #signature(String)
     * @since 1.0.0
     */
    public EmailRequest appendSignature(String sig) { return signature(sig); }

    /**
     * Builds a signature from the current {@link chirp.session.Session} —
     * name, roll number, program, email.
     *
     * <p>Returns an empty string if the session fields are not set.
     * Typically called once at program startup after configuring the
     * Session, then reused for every email.</p>
     *
     * @return this request
     * @see #signature(String)
     * @see chirp.session.Session
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #signatureFromSession()} — see that method for
     * full documentation.
     *
     * @return this request
     * @see #signatureFromSession()
     * @since 1.0.0
     */
    public EmailRequest sessionSignature() { return signatureFromSession(); }

    // ============================================================== attachments

    /**
     * Attaches a file from the filesystem. The filename in the MIME
     * message is the file's basename; the full path is not exposed.
     *
     * @param path the file path (must exist and be a regular file)
     * @return this request
     * @throws EmailException if the file does not exist or cannot be read
     * @see #attach(File)
     * @see #attach(String, byte[])
     * @see #attachFile(String)
     * @see #includeFile(String)
     * @since 1.0.0
     */
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
            // Path.getFileName() returns null for root paths; fall back to the
            // full string. We already know it is a regular file (checked above),
            // so it cannot be a filesystem root, but the guard keeps SpotBugs
            // and the compiler honest.
            Path fileName = p.getFileName();
            String name = fileName == null ? p.toString() : fileName.toString();
            attachments.add(new Attachment(name, data));
        } catch (java.io.IOException e) {
            throw new EmailException(EmailException.Reason.TRANSPORT,
                    "Cannot read attachment: " + path, e);
        }
        return this;
    }

    /**
     * Alias of {@link #attach(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return this request
     * @see #attach(String)
     * @since 1.0.0
     */
    public EmailRequest attachFile(String path) { return attach(path); }

    /**
     * Alias of {@link #attach(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return this request
     * @see #attach(String)
     * @since 1.0.0
     */
    public EmailRequest includeFile(String path) { return attach(path); }

    /**
     * Attaches a {@link File}.
     *
     * @param file the file (null → no-op)
     * @return this request
     * @throws EmailException if the file does not exist or cannot be read
     * @see #attach(String)
     * @since 1.0.0
     */
    public EmailRequest attach(File file) {
        return file == null ? this : attach(file.getAbsolutePath());
    }

    /**
     * Attaches raw bytes with a given filename. Useful for generating
     * an attachment in memory (a PDF built from data, a report
     * serialised to bytes) without writing it to disk first.
     *
     * @param filename the filename that will appear in the email
     * @param data the raw bytes (null → no-op)
     * @return this request
     * @see #attachBytes(String, byte[])
     * @see #attach(String)
     * @since 1.0.0
     */
    public EmailRequest attach(String filename, byte[] data) {
        if (filename == null || data == null) return this;
        attachments.add(new Attachment(filename, data.clone()));
        return this;
    }

    /**
     * Alias of {@link #attach(String, byte[])} — see that method for
     * full documentation.
     *
     * @param filename the filename
     * @param data the bytes
     * @return this request
     * @see #attach(String, byte[])
     * @since 1.0.0
     */
    public EmailRequest attachBytes(String filename, byte[] data) { return attach(filename, data); }

    /**
     * Attaches every file path in the list.
     *
     * @param paths the paths to attach (null → no-op)
     * @return this request
     * @throws EmailException if any path does not exist or is not a
     * regular file
     * @see #attachAll(List)
     * @see #attach(String)
     * @since 1.0.0
     */
    public EmailRequest attachMany(List<String> paths) {
        if (paths != null) for (String p : paths) attach(p);
        return this;
    }

    /**
     * Alias of {@link #attachMany(List)} — see that method for full
     * documentation.
     *
     * @param paths the paths to attach
     * @return this request
     * @see #attachMany(List)
     * @since 1.0.0
     */
    public EmailRequest attachAll(List<String> paths) { return attachMany(paths); }

    /**
     * Attaches every regular file in a directory (non-recursive — files
     * in subdirectories are not included).
     *
     * @param path the directory path
     * @return this request
     * @throws EmailException if the path is not a directory
     * @see #attachDir(String)
     * @see #attach(String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #attachDirectory(String)} — see that method for
     * full documentation.
     *
     * @param path the directory path
     * @return this request
     * @see #attachDirectory(String)
     * @since 1.0.0
     */
    public EmailRequest attachDir(String path) { return attachDirectory(path); }

    /**
     * Removes all attachments.
     *
     * @return this request
     * @see #clearAttachments()
     * @see #removeAttachments()
     * @since 1.0.0
     */
    public EmailRequest noAttachments() {
        attachments.clear();
        return this;
    }

    /**
     * Alias of {@link #noAttachments()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #noAttachments()
     * @since 1.0.0
     */
    public EmailRequest clearAttachments() { return noAttachments(); }

    /**
     * Alias of {@link #noAttachments()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #noAttachments()
     * @since 1.0.0
     */
    public EmailRequest removeAttachments() { return noAttachments(); }

    /**
     * Returns the number of attachments currently queued.
     *
     * @return the attachment count
     * @since 1.0.0
     */
    public int attachmentCount() { return attachments.size(); }

    // ============================================================== password

    /**
     * Sets the SMTP password directly in code. <b>Discouraged</b> —
     * sends a warning at send time. Use {@link #passwordFromEnv} in
     * production.
     *
     * @param direct the password
     * @return this request
     * @see #withPassword(String)
     * @see #passwordFromEnv(String, String)
     * @see #promptPassword()
     * @since 1.0.0
     */
    public EmailRequest password(String direct) {
        this.password = direct;
        this.strategy = PasswordStrategy.DIRECT;
        return this;
    }

    /**
     * Alias of {@link #password(String)} — see that method for full
     * documentation.
     *
     * @param direct the password
     * @return this request
     * @see #password(String)
     * @since 1.0.0
     */
    public EmailRequest withPassword(String direct) { return password(direct); }

    /**
     * Prompts for the password at send time. Nothing touches the
     * filesystem — the user is asked via {@link System#console()} (or
     * stdin if no console is available).
     *
     * @return this request
     * @see #askPassword()
     * @see #passwordFromEnv(String, String)
     * @see PasswordStrategy#PROMPT
     * @since 1.0.0
     */
    public EmailRequest promptPassword() { this.strategy = PasswordStrategy.PROMPT; return this; }

    /**
     * Alias of {@link #promptPassword()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #promptPassword()
     * @since 1.0.0
     */
    public EmailRequest askPassword() { return promptPassword(); }

    /**
     * Reads the password from a {@code .env} file at send time. This is
     * the recommended strategy — the password never appears in source
     * code or in the git history.
     *
     * <pre>{@code
     * // .env file (gitignored):
     * SMTP_PASSWORD=app-specific-password
     *
     * // In code:
     * Email.builder()
     *     .host("smtp.gmail.com")
     *     // ... config ...
     *     .passwordFromEnv(".env", "SMTP_PASSWORD")
     *     .send();
     * }</pre>
     *
     * @param path the .env file path (defaults to {@code ".env"} if
     * blank when the send starts)
     * @param key the key containing the password (required)
     * @return this request
     * @see #envPassword(String, String)
     * @see #password(String)
     * @see EnvLoader
     * @see PasswordStrategy#ENV
     * @since 1.0.0
     */
    public EmailRequest passwordFromEnv(String path, String key) {
        this.strategy = PasswordStrategy.ENV;
        this.envPath = path;
        this.envKey = key;
        return this;
    }

    /**
     * Alias of {@link #passwordFromEnv(String, String)} — see that
     * method for full documentation and examples.
     *
     * @param path the .env path
     * @param key the .env key
     * @return this request
     * @see #passwordFromEnv(String, String)
     * @since 1.0.0
     */
    public EmailRequest envPassword(String path, String key) { return passwordFromEnv(path, key); }

    /**
     * Sets the password strategy alone, without the parameters. Useful
     * when the strategy is coming from configuration.
     *
     * @param s the strategy (null → no-op)
     * @return this request
     * @see #passwordFrom(PasswordStrategy, String, String)
     * @since 1.0.0
     */
    public EmailRequest passwordFrom(PasswordStrategy s) { this.strategy = s; return this; }

    /**
     * Sets the password strategy and its parameters in one call.
     *
     * @param s the strategy
     * @param path for {@code DIRECT}: the password; for {@code ENV}: the
     * .env path
     * @param key for {@code ENV}: the .env key
     * @return this request
     * @see #passwordFrom(PasswordStrategy)
     * @since 1.0.0
     */
    public EmailRequest passwordFrom(PasswordStrategy s, String path, String key) {
        this.strategy = s;
        if (s == PasswordStrategy.ENV) { this.envPath = path; this.envKey = key; }
        if (s == PasswordStrategy.DIRECT) { this.password = path; }
        return this;
    }

    // ============================================================== behaviour

    /**
     * Sets the SMTP connection timeout. Defaults to 30 seconds.
     *
     * @param d the timeout (null or negative → no-op)
     * @return this request
     * @see #timeoutSeconds(int)
     * @since 1.0.0
     */
    public EmailRequest timeout(Duration d) { if (d != null && !d.isNegative()) this.timeout = d; return this; }

    /**
     * Sets the SMTP connection timeout in seconds (clamped to ≥ 1).
     *
     * @param s the timeout in seconds
     * @return this request
     * @see #timeout(Duration)
     * @since 1.0.0
     */
    public EmailRequest timeoutSeconds(int s) { return timeout(Duration.ofSeconds(Math.max(1, s))); }

    /**
     * Enables verbose mode — the SMTP dialogue is forwarded to the
     * listener's {@link EmailListener#onSmtpEvent(String)} and captured
     * in {@link EmailResult#smtpLog()}.
     *
     * @param v whether to enable verbose
     * @return this request
     * @see #withVerbose()
     * @see #noVerbose()
     * @see #log()
     * @since 1.0.0
     */
    public EmailRequest verbose(boolean v) { this.verbose = v; return this; }

    /**
     * Enables verbose mode — see {@link #verbose(boolean)}.
     *
     * @return this request
     * @see #verbose(boolean)
     * @since 1.0.0
     */
    public EmailRequest withVerbose() { return verbose(true); }

    /**
     * Disables verbose mode — see {@link #verbose(boolean)}.
     *
     * @return this request
     * @see #verbose(boolean)
     * @since 1.0.0
     */
    public EmailRequest noVerbose() { return verbose(false); }

    /**
     * Alias of {@link #withVerbose()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #withVerbose()
     * @since 1.0.0
     */
    public EmailRequest log() { return verbose(true); }

    /**
     * Alias of {@link #withVerbose()} — see that method for full
     * documentation.
     *
     * @return this request
     * @see #withVerbose()
     * @since 1.0.0
     */
    public EmailRequest logs() { return verbose(true); }

    /**
     * Enables or disables the terminal spinner. When unset, the spinner
     * is shown automatically in interactive console mode (i.e. after
     * {@code Chirp.start()} with console messages on).
     *
     * @param v whether to show a spinner (null → automatic)
     * @return this request
     * @see #withLoader()
     * @see #noLoader()
     * @since 1.0.0
     */
    public EmailRequest loader(boolean v) { this.loaderEnabled = v; return this; }

    /**
     * Enables the terminal spinner — see {@link #loader(boolean)}.
     *
     * @return this request
     * @see #loader(boolean)
     * @since 1.0.0
     */
    public EmailRequest withLoader() { return loader(true); }

    /**
     * Disables the terminal spinner.
     *
     * @return this request
     * @see #loader(boolean)
     * @since 1.0.0
     */
    public EmailRequest noLoader() { return loader(false); }

    /**
     * Enables or disables dry-run mode. In dry-run mode, the MIME
     * message is built and returned but not sent — useful for
     * validating configuration before a real send.
     *
     * @param v whether to enable dry run
     * @return this request
     * @see #preview()
     * @see #previewMimeMessage()
     * @since 1.0.0
     */
    public EmailRequest dryRun(boolean v) { this.dryRun = v; return this; }

    /**
     * Alias of {@link #dryRun(boolean)} with {@code true} — see that
     * method for full documentation.
     *
     * @return this request
     * @see #dryRun(boolean)
     * @since 1.0.0
     */
    public EmailRequest preview() { return dryRun(true); }

    /**
     * Attaches a listener for send lifecycle events.
     *
     * @param l the listener (null → clears any previously attached listener)
     * @return this request
     * @see #onEvent(EmailListener)
     * @see EmailListener
     * @since 1.0.0
     */
    public EmailRequest listener(EmailListener l) { this.listener = l; return this; }

    /**
     * Alias of {@link #listener(EmailListener)} — see that method for
     * full documentation.
     *
     * @param l the listener
     * @return this request
     * @see #listener(EmailListener)
     * @since 1.0.0
     */
    public EmailRequest onEvent(EmailListener l) { return listener(l); }

    // ============================================================== send

    /**
     * Sends the email. Never throws — the failure is captured in the
     * returned {@link EmailResult}.
     *
     * <p>This is the primary send method. Use it when you want to
     * inspect the outcome:</p>
     * <pre>{@code
     * EmailResult result = request.send();
     * if (result.isSuccess()) {
     *     System.out.println("Sent in " + result.durationMs() + "ms");
     * } else {
     *     System.out.println("Failed: " + result.message());
     * }
     * }</pre>
     *
     * @return the send outcome
     * @see #sendOrThrow()
     * @see #sendWithRetry(int)
     * @see #deliver()
     * @see #dispatch()
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the send outcome
     * @see #send()
     * @since 1.0.0
     */
    public EmailResult deliver() { return send(); }

    /**
     * Alias of {@link #send()} — see that method for full documentation
     * and examples.
     *
     * @return the send outcome
     * @see #send()
     * @since 1.0.0
     */
    public EmailResult dispatch() { return send(); }

    /**
     * Sends the email, throwing {@link EmailException} on any failure.
     *
     * <p>Use this when the surrounding code can't handle a failure
     * value — for example in a stream pipeline:</p>
     * <pre>{@code
     * recipients.stream()
     *     .map(addr -> Email.builder().to(addr)...sendOrThrow())
     *     .forEach(r -> System.out.println("Sent in " + r.durationMs() + "ms"));
     * }</pre>
     *
     * @return the send outcome (success)
     * @throws EmailException if the send fails
     * @see #send()
     * @see #deliverOrThrow()
     * @since 1.0.0
     */
    public EmailResult sendOrThrow() { return doSend(true); }

    /**
     * Alias of {@link #sendOrThrow()} — see that method for full
     * documentation and examples.
     *
     * @return the send outcome
     * @throws EmailException if the send fails
     * @see #sendOrThrow()
     * @since 1.0.0
     */
    public EmailResult deliverOrThrow() { return sendOrThrow(); }

    /**
     * Sends with retries — retries on any failure, waiting 500ms
     * between attempts.
     *
     * <p>This is a simple retry — it does not distinguish transport
     * failures from validation failures. For granular retry control,
     * use {@link #sendOrThrow()} and check
     * {@link EmailException#isTransportError()}.</p>
     *
     * @param attempts the number of attempts (≥ 1)
     * @return the last send outcome
     * @see #sendWithRetry(int, Duration)
     * @see #retry(int)
     * @see #sendWithRetries(int)
     * @see #send()
     * @since 1.0.0
     */
    public EmailResult sendWithRetry(int attempts) {
        return sendWithRetry(attempts, Duration.ofMillis(500));
    }

    /**
     * Sends with retries and a custom delay between attempts.
     *
     * @param attempts the number of attempts (≥ 1)
     * @param delay the delay between attempts (null or negative → no delay)
     * @return the last send outcome
     * @see #sendWithRetry(int)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #sendWithRetry(int)} — see that method for full
     * documentation.
     *
     * @param attempts the number of attempts
     * @return the last send outcome
     * @see #sendWithRetry(int)
     * @since 1.0.0
     */
    public EmailResult retry(int attempts) { return sendWithRetry(attempts); }

    /**
     * Alias of {@link #sendWithRetry(int)} — see that method for full
     * documentation.
     *
     * @param attempts the number of attempts
     * @return the last send outcome
     * @see #sendWithRetry(int)
     * @since 1.0.0
     */
    public EmailResult sendWithRetries(int attempts) { return sendWithRetry(attempts); }

    /**
     * Builds and returns the raw MIME message <b>without sending it</b>.
     * Throws {@link EmailException} if the configuration is incomplete
     * (missing host, from, body, etc.).
     *
     * <p>Useful for:
     * <ul>
     *   <li>Debugging — inspect exactly what would be sent</li>
     *   <li>Testing — assert the message structure without network</li>
     *   <li>Dry run — validate config before the real send</li>
     * </ul>
     * </p>
     *
     * @return the full MIME message
     * @throws EmailException if configuration is invalid
     * @see #previewIfValid()
     * @see #dryRun(boolean)
     * @see #mimeMessage()
     * @since 1.0.0
     */
    public String previewMimeMessage() {
        validate();
        return buildMimeMessage();
    }

    /**
     * Alias of {@link #previewMimeMessage()} — see that method for full
     * documentation.
     *
     * @return the MIME message
     * @throws EmailException if configuration is invalid
     * @see #previewMimeMessage()
     * @since 1.0.0
     */
    public String mimeMessage() { return previewMimeMessage(); }

    /**
     * Alias of {@link #previewMimeMessage()} — see that method for full
     * documentation.
     *
     * @return the MIME message
     * @throws EmailException if configuration is invalid
     * @see #previewMimeMessage()
     * @since 1.0.0
     */
    public String asMime() { return previewMimeMessage(); }

    /**
     * Builds the MIME message, returning {@link Optional#empty()} on any
     * configuration error instead of throwing.
     *
     * @return the MIME message, or empty on error
     * @see #previewMimeMessage()
     * @since 1.0.0
     */
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
     * Opens a TCP socket to {@code (host, port)} to verify the server is
     * reachable. Does <b>not</b> verify credentials — that requires a full
     * SMTP session.
     *
     * <p>Useful before calling {@link #send()} to distinguish "the SMTP
     * server is down" from "my configuration is wrong".</p>
     *
     * @return {@code true} if the socket connects successfully
     * @see #canConnect()
     * @see #testConnection()
     * @see #testOrThrow()
     * @since 1.0.0
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

    /**
     * Alias of {@link #test()} — see that method for full documentation.
     *
     * @return {@code true} if reachable
     * @see #test()
     * @since 1.0.0
     */
    public boolean canConnect() { return test(); }

    /**
     * Alias of {@link #test()} — see that method for full documentation.
     *
     * @return {@code true} if reachable
     * @see #test()
     * @since 1.0.0
     */
    public boolean testConnection() { return test(); }

    /**
     * Verifies the configuration and connectivity — throws
     * {@link EmailException} if either is broken.
     *
     * @throws EmailException if the configuration is invalid or the
     * server is unreachable
     * @see #test()
     * @see #canConnect()
     * @since 1.0.0
     */
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

    /**
     * Returns a short summary of the request — {@code "EmailRequest[host:port from=x to=N]"}.
     *
     * @return the summary
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "EmailRequest[" + host + ":" + port + " from=" + from + " to=" + to.size() + "]";
    }
}
