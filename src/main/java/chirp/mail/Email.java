package chirp.mail;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * Static facade for the mail module — factories for {@link EmailRequest}
 * builders, one-shot send helpers, and SMTP connectivity tests.
 *
 * <p>Two ways to send an email:</p>
 * <ul>
 *   <li><b>Fluent</b> — {@link #builder()} returns a {@link EmailRequest}
 *       you configure (host, from, to, subject, body, password source)
 *       and then call {@code .send()} on.</li>
 *   <li><b>One-shot</b> — {@link #sendSimple(String, String, String, String, String, String)}
 *       and {@link #sendWithEnv(String, String, String, String, String, String, String)}
 *       take everything as arguments and send in one call.</li>
 * </ul>
 *
 * <h2>Recommended way — fluent builder with env password</h2>
 * <pre>{@code
 * Email.builder()
 *     .host("smtp.gmail.com")
 *     .from("me@gmail.com")
 *     .to("you@example.com")
 *     .subject("Hello")
 *     .text("Hi there!")
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")
 *     .send();
 * }</pre>
 *
 * <p>The password is <b>not</b> in the source code — it comes from the
 * {@code .env} file, which is usually gitignored. This is the single
 * biggest security habit a student can pick up.</p>
 *
 * <h2>Key-value facade</h2>
 * <p>{@link #send(Object...)} accepts alternating {@link Key} → value
 * pairs, so the same configuration can be passed as a variadic:</p>
 * <pre>{@code
 * Email.send(
 *     Email.Key.HOST, "smtp.gmail.com",
 *     Email.Key.FROM, "me@gmail.com",
 *     Email.Key.TO, "you@example.com",
 *     Email.Key.SUBJECT, "Hello",
 *     Email.Key.TEXT, "Hi there!",
 *     Email.Key.PASSWORD, "app-password");
 * }</pre>
 * <p>Unknown keys and wrong value types are silently skipped, so the
 * call is resilient to partial input. Missing required keys become an
 * {@link EmailResult} with {@code success=false} — the method never
 * throws.</p>
 *
 * <h2>Password strategies</h2>
 * <p>See {@link PasswordStrategy} for the three options. In order of
 * preference: {@code ENV} (from a {@code .env} file), {@code PROMPT}
 * (from the terminal at send time), {@code DIRECT} (in the source code —
 * discouraged). Direct passwords print a warning.</p>
 *
 * <h2>Connectivity test</h2>
 * <p>{@link #canConnect(String, int)} opens a TCP socket to the SMTP
 * server to verify it's reachable. It does <b>not</b> test credentials —
 * a full SMTP session would be needed for that (use
 * {@link EmailRequest#testOrThrow()} which does attempt a login).</p>
 *
 * <h2>Related</h2>
 * <p>For the fluent request builder, see {@link EmailRequest}. For the
 * outcome record, see {@link EmailResult}. For the exception, see
 * {@link EmailException}.</p>
 *
 * @see EmailRequest
 * @see EmailResult
 * @see EmailException
 * @see EnvLoader
 * @see PasswordStrategy
 * @since 1.0.0
 */
public final class Email {

    private Email() {}

    // ============================================================== key enum

    /**
     * Key enum for {@link #send(Object...)} — every configuration option
     * in one IDE-autocompletable enum.
     *
     * <p>Keys whose value is a list ({@code TO}, {@code CC}, {@code BCC},
     * {@code ATTACH}) accept either a {@code String}, a {@code String[]},
     * or a {@code List<?>}. Multiple occurrences accumulate.</p>
     *
     * @since 1.0.0
     */
    public enum Key {
        /** SMTP server hostname. */
        HOST,
        /** SMTP port (default: 587 for STARTTLS, 465 for SSL). */
        PORT,
        /** Use SMTPS (implicit TLS) on port 465. */
        SSL,
        /** Use STARTTLS on port 587. */
        STARTTLS,
        /** Sender address. */
        FROM,
        /** Sender display name. */
        SENDER_NAME,
        /** SMTP username (usually the from address). */
        USERNAME,
        /** Environment file to read the username from. */
        USERNAME_ENV_FILE,
        /** Environment key for the username. */
        USERNAME_ENV_KEY,
        /** Recipient address (may be repeated). */
        TO,
        /** CC address (may be repeated). */
        CC,
        /** BCC address (may be repeated). */
        BCC,
        /** Reply-To address. */
        REPLY_TO,
        /** Reply-To display name. */
        REPLY_TO_NAME,
        /** Return-Path (bounce) address. */
        BOUNCE_TO,
        /** Email subject. */
        SUBJECT,
        /** Plain-text body. */
        TEXT,
        /** HTML body. */
        HTML,
        /** HTML template path or {@link HtmlTemplate} object. */
        HTML_TEMPLATE,
        /** Attachment path (may be repeated). */
        ATTACH,
        /** Signature text appended to the body. */
        SIGNATURE,
        /** SMTP password (DIRECT strategy — discouraged). */
        PASSWORD,
        /** The password strategy to use. */
        PASSWORD_STRATEGY,
        /** Environment file path for the password. */
        ENV_FILE,
        /** Environment key for the password. */
        ENV_KEY,
        /** SMTP connection timeout in seconds. */
        TIMEOUT_SECONDS,
        /** Show SMTP dialogue when verbose. */
        VERBOSE,
        /** Build the message but don't send. */
        DRY_RUN,
        /** Show a spinner during send (default: on in interactive mode). */
        LOADER,
        /** X-Priority header (1 = highest, 5 = lowest). */
        PRIORITY,
        /** Number of retry attempts on failure. */
        RETRY_ATTEMPTS
    }

    // ============================================================== factories

    /**
     * Opens a fluent {@link EmailRequest} builder.
     *
     * @return a new empty builder
     * @see #compose()
     * @see #create()
     * @see #mail()
     * @see #message()
     * @see #newEmail()
     * @since 1.0.0
     */
    public static EmailRequest builder() { return new EmailRequest(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new empty builder
     * @see #builder()
     * @since 1.0.0
     */
    public static EmailRequest compose() { return new EmailRequest(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new empty builder
     * @see #builder()
     * @since 1.0.0
     */
    public static EmailRequest create() { return new EmailRequest(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new empty builder
     * @see #builder()
     * @since 1.0.0
     */
    public static EmailRequest mail() { return new EmailRequest(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new empty builder
     * @see #builder()
     * @since 1.0.0
     */
    public static EmailRequest message() { return new EmailRequest(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new empty builder
     * @see #builder()
     * @since 1.0.0
     */
    public static EmailRequest newEmail() { return new EmailRequest(); }

    // ============================================================== one-shot send

    /**
     * Sends an email from alternating {@link Key} → value pairs.
     *
     * <p>Missing required keys produce an {@link EmailResult} with
     * {@code success=false} — the method never throws.
     * {@link Key#RETRY_ATTEMPTS} &gt; 1 wraps the send in
     * {@link EmailRequest#sendWithRetry(int)}.</p>
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return the send outcome
     * @see #sendEmail(Object...)
     * @see #sendMail(Object...)
     * @see #deliver(Object...)
     * @see EmailRequest#send()
     * @since 1.0.0
     */
    public static EmailResult send(Object... keyValues) {
        EmailRequest req = new EmailRequest();
        try {
            Integer retries = applyKeyValues(req, keyValues);
            if (retries != null && retries > 1) return req.sendWithRetry(retries);
            return req.send();
        } catch (EmailException e) {
            return EmailResult.failure(e.getMessage());
        }
    }

    /**
     * Alias of {@link #send(Object...)} — see that method for full
     * documentation and examples.
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return the send outcome
     * @see #send(Object...)
     * @since 1.0.0
     */
    public static EmailResult sendEmail(Object... keyValues) { return send(keyValues); }

    /**
     * Alias of {@link #send(Object...)} — see that method for full
     * documentation and examples.
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return the send outcome
     * @see #send(Object...)
     * @since 1.0.0
     */
    public static EmailResult sendMail(Object... keyValues) { return send(keyValues); }

    /**
     * Alias of {@link #send(Object...)} — see that method for full
     * documentation and examples.
     *
     * @param keyValues alternating {@link Key} → value pairs
     * @return the send outcome
     * @see #send(Object...)
     * @since 1.0.0
     */
    public static EmailResult deliver(Object... keyValues) { return send(keyValues); }

    /**
     * Sends an email using a map of configuration. Equivalent to
     * {@link #send(Object...)} with the map flattened to key-value pairs.
     *
     * @param config the configuration map (null → failure result)
     * @return the send outcome
     * @see #send(Object...)
     * @since 1.0.0
     */
    public static EmailResult send(Map<Key, Object> config) {
        if (config == null) return EmailResult.failure("Configuration is required");
        Object[] flat = new Object[config.size() * 2];
        int i = 0;
        for (Map.Entry<Key, Object> e : config.entrySet()) {
            flat[i++] = e.getKey();
            flat[i++] = e.getValue();
        }
        return send(flat);
    }

    // ============================================================== simple overloads

    /**
     * Simplest overload — direct password. Generates a warning (password
     * in source code is a security anti-pattern).
     *
     * <p>Use {@link #sendWithEnv} or the fluent builder with
     * {@code .passwordFromEnv(...)} in production.</p>
     *
     * @param host the SMTP host
     * @param from the sender address
     * @param to the recipient address
     * @param subject the subject
     * @param body the body text
     * @param password the SMTP password
     * @return the send outcome
     * @see #sendWithEnv(String, String, String, String, String, String, String)
     * @see #quickSend(String, String, String, String, String, String)
     * @see #sendWithPassword(String, String, String, String, String, String)
     * @since 1.0.0
     */
    public static EmailResult sendSimple(String host, String from, String to,
            String subject, String body, String password) {
        return builder()
                .host(host)
                .from(from)
                .to(to)
                .subject(subject)
                .text(body)
                .password(password)
                .send();
    }

    /**
     * Alias of {@link #sendSimple(String, String, String, String, String, String)}
     * — see that method for full documentation.
     *
     * @param host the SMTP host
     * @param from the sender
     * @param to the recipient
     * @param subject the subject
     * @param body the body text
     * @param password the password
     * @return the send outcome
     * @see #sendSimple(String, String, String, String, String, String)
     * @since 1.0.0
     */
    public static EmailResult quickSend(String host, String from, String to,
            String subject, String body, String password) {
        return sendSimple(host, from, to, subject, body, password);
    }

    /**
     * Alias of {@link #sendSimple(String, String, String, String, String, String)}
     * — see that method for full documentation.
     *
     * @param host the SMTP host
     * @param from the sender
     * @param to the recipient
     * @param subject the subject
     * @param body the body text
     * @param password the password
     * @return the send outcome
     * @see #sendSimple(String, String, String, String, String, String)
     * @since 1.0.0
     */
    public static EmailResult sendWithPassword(String host, String from, String to,
            String subject, String body, String password) {
        return sendSimple(host, from, to, subject, body, password);
    }

    /**
     * Recommended overload — env password. Reads the password from a
     * {@code .env} file at send time.
     *
     * <pre>{@code
     * Email.sendWithEnv("smtp.gmail.com",
     *     "me@gmail.com", "you@example.com",
     *     "Hello", "Hi there!",
     *     ".env", "SMTP_PASSWORD");
     * }</pre>
     *
     * @param host the SMTP host
     * @param from the sender address
     * @param to the recipient address
     * @param subject the subject
     * @param body the body text
     * @param envFile the .env file path
     * @param envKey the key in the .env file containing the password
     * @return the send outcome
     * @see #sendUsingEnv(String, String, String, String, String, String, String)
     * @see EnvLoader
     * @since 1.0.0
     */
    public static EmailResult sendWithEnv(String host, String from, String to,
            String subject, String body,
            String envFile, String envKey) {
        return builder()
                .host(host)
                .from(from)
                .to(to)
                .subject(subject)
                .text(body)
                .passwordFromEnv(envFile, envKey)
                .send();
    }

    /**
     * Alias of {@link #sendWithEnv(String, String, String, String, String, String, String)}
     * — see that method for full documentation and examples.
     *
     * @param host the SMTP host
     * @param from the sender
     * @param to the recipient
     * @param subject the subject
     * @param body the body text
     * @param envFile the .env file path
     * @param envKey the key in the .env file
     * @return the send outcome
     * @see #sendWithEnv(String, String, String, String, String, String, String)
     * @since 1.0.0
     */
    public static EmailResult sendUsingEnv(String host, String from, String to,
            String subject, String body,
            String envFile, String envKey) {
        return sendWithEnv(host, from, to, subject, body, envFile, envKey);
    }

    // ============================================================== connectivity

    /**
     * Tests whether an SMTP server is reachable by opening a TCP socket
     * with the default 3-second timeout. Does <b>not</b> test credentials
     * — a successful connection only proves the host is up and the port
     * is open.
     *
     * @param host the SMTP hostname (may be null → false)
     * @param port the SMTP port (0 or negative → false)
     * @return {@code true} if a socket connects successfully
     * @see #canConnect(String, int, int)
     * @since 1.0.0
     */
    public static boolean canConnect(String host, int port) {
        return canConnect(host, port, 3000);
    }

    /**
     * Alias of {@link #canConnect(String, int)} — see that method for
     * full documentation.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @return {@code true} if reachable
     * @see #canConnect(String, int)
     * @since 1.0.0
     */
    public static boolean testServer(String host, int port) { return canConnect(host, port); }

    /**
     * Alias of {@link #canConnect(String, int)} — see that method for
     * full documentation.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @return {@code true} if reachable
     * @see #canConnect(String, int)
     * @since 1.0.0
     */
    public static boolean pingServer(String host, int port) { return canConnect(host, port); }

    /**
     * Alias of {@link #canConnect(String, int)} — see that method for
     * full documentation.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @return {@code true} if reachable
     * @see #canConnect(String, int)
     * @since 1.0.0
     */
    public static boolean checkServer(String host, int port) { return canConnect(host, port); }

    /**
     * Tests whether the SMTP server is reachable within the specified
     * timeout.
     *
     * @param host the SMTP hostname
     * @param port the SMTP port
     * @param timeoutMs the connect timeout in milliseconds
     * @return {@code true} if a socket connects successfully
     * @see #canConnect(String, int)
     * @since 1.0.0
     */
    public static boolean canConnect(String host, int port, int timeoutMs) {
        if (host == null || port <= 0) return false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.max(100, timeoutMs));
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    // ============================================================== key-value applier

    private static Integer applyKeyValues(EmailRequest req, Object... kv) {
        if (kv == null) return null;
        Integer retries = null;

        for (int i = 0; i < kv.length; i += 2) {
            Object key = kv[i];
            if (!(key instanceof Key k)) {
                if (key != null) {
                    throw new EmailException(EmailException.Reason.VALIDATION,
                            "Invalid key at position " + i + " — expected Email.Key, got "
                                    + key.getClass().getSimpleName());
                }
                continue;
            }
            Object val = (i + 1 < kv.length) ? kv[i + 1] : null;

            switch (k) {
                case HOST -> req.host(castString(val, k));
                case PORT -> req.port(castInt(val, k));
                case SSL -> req.ssl(castBool(val));
                case STARTTLS -> req.starttls(castBool(val));
                case FROM -> req.from(castString(val, k));
                case SENDER_NAME -> req.senderName(castString(val, k));
                case USERNAME -> req.username(castString(val, k));
                case USERNAME_ENV_FILE -> req.usernameFromEnv(castString(val, k), req.envKey());
                case USERNAME_ENV_KEY -> req.usernameFromEnv(req.envPath(), castString(val, k));
                case TO -> applyList(val, req::to);
                case CC -> applyList(val, req::cc);
                case BCC -> applyList(val, req::bcc);
                case REPLY_TO -> req.replyTo(castString(val, k));
                case REPLY_TO_NAME -> req.replyToName(castString(val, k));
                case BOUNCE_TO -> req.bounceTo(castString(val, k));
                case SUBJECT -> req.subject(castString(val, k));
                case TEXT -> req.text(castString(val, k));
                case HTML -> req.html(castString(val, k));
                case HTML_TEMPLATE -> {
                    if (val instanceof HtmlTemplate t) req.htmlTemplate(t);
                    else if (val instanceof String path) req.template(path);
                }
                case ATTACH -> applyList(val, req::attach);
                case SIGNATURE -> req.signature(castString(val, k));
                case PASSWORD -> req.password(castString(val, k));
                case PASSWORD_STRATEGY -> {
                    if (val instanceof PasswordStrategy s) req.passwordFrom(s);
                }
                case ENV_FILE -> req.passwordFromEnv(castString(val, k), req.envKey());
                case ENV_KEY -> req.passwordFromEnv(req.envPath(), castString(val, k));
                case TIMEOUT_SECONDS -> req.timeoutSeconds(castInt(val, k));
                case VERBOSE -> req.verbose(castBool(val));
                case DRY_RUN -> req.dryRun(castBool(val));
                case LOADER -> req.loader(castBool(val));
                case PRIORITY -> req.priority(castInt(val, k));
                case RETRY_ATTEMPTS -> retries = castInt(val, k);
            }
        }
        return retries;
    }

    private static String castString(Object val, Key key) {
        if (val == null) return null;
        if (val instanceof String s) return s;
        throw new EmailException(EmailException.Reason.VALIDATION,
                "Key " + key + " expects String but got " + val.getClass().getSimpleName());
    }

    private static int castInt(Object val, Key key) {
        if (val instanceof Number n) return n.intValue();
        if (val == null) return 0;
        throw new EmailException(EmailException.Reason.VALIDATION,
                "Key " + key + " expects Integer but got " + val.getClass().getSimpleName());
    }

    private static boolean castBool(Object val) {
        return val instanceof Boolean b && b;
    }

    private static void applyList(Object val, java.util.function.Consumer<String> adder) {
        if (val instanceof String[] arr) for (String s : arr) adder.accept(s);
        else if (val instanceof java.util.List<?> list) {
            for (Object o : list) if (o != null) adder.accept(o.toString());
        } else if (val instanceof String s) adder.accept(s);
    }
}
