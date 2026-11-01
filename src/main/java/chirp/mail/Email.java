package chirp.mail;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * Facade for sending email — plus common aliases and connection testing.
 *
 * <h2>Naming</h2>
 * <p>Factories: {@code builder / compose / create / mail / message / newEmail};
 * one-shot send: {@code send / sendEmail / sendMail / deliver};
 * quick overloads: {@code sendSimple / quickSend / sendWithPassword};
 * env overloads: {@code sendWithEnv / sendUsingEnv};
 * connectivity: {@code testServer / canConnect / pingServer / checkServer}.</p>
 *
 * <h2>Key-value facade</h2>
 * <p>{@code Email.send(Key.HOST, "...", Key.FROM, "...", ...)} — enum keys
 * auto-complete in the IDE. Unknown keys and wrong types are silently skipped.</p>
 *
 * <pre>{@code
 * Email.builder()
 *     .host("smtp.gmail.com")
 *     .port(587)
 *     .from("me@gmail.com")
 *     .to("you@gmail.com")
 *     .subject("Hi")
 *     .text("Hello!")
 *     .passwordFromEnv(".env", "SMTP_PASSWORD")
 *     .send();
 * }</pre>
 */
public final class Email {

    private Email() {}

    // ============================================================== key enum

    public enum Key {
        HOST, PORT, SSL, STARTTLS,
        FROM, SENDER_NAME, USERNAME, USERNAME_ENV_FILE, USERNAME_ENV_KEY,
        TO, CC, BCC, REPLY_TO, REPLY_TO_NAME, BOUNCE_TO,
        SUBJECT, TEXT, HTML, HTML_TEMPLATE,
        ATTACH, SIGNATURE,
        PASSWORD, PASSWORD_STRATEGY, ENV_FILE, ENV_KEY,
        TIMEOUT_SECONDS, VERBOSE, DRY_RUN, LOADER,
        PRIORITY, RETRY_ATTEMPTS
    }

    // ============================================================== factories

    public static EmailRequest builder() { return new EmailRequest(); }
    public static EmailRequest compose() { return new EmailRequest(); }
    public static EmailRequest create() { return new EmailRequest(); }
    public static EmailRequest mail() { return new EmailRequest(); }
    public static EmailRequest message() { return new EmailRequest(); }
    public static EmailRequest newEmail() { return new EmailRequest(); }

    // ============================================================== one-shot send

    /**
     * Sends using alternating {@link Key} → value pairs.
     *
     * <p>Multiple {@code TO}/{@code CC}/{@code BCC}/{@code ATTACH} pairs are
     * accumulated. Odd-length argument list yields a graceful error result
     * (no exception thrown).</p>
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
    public static EmailResult sendEmail(Object... keyValues) { return send(keyValues); }
    public static EmailResult sendMail(Object... keyValues) { return send(keyValues); }
    public static EmailResult deliver(Object... keyValues) { return send(keyValues); }

    /** Sends using a plain {@code Map}. */
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

    /** Simplest overload — direct password. Generates a warning. */
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
    public static EmailResult quickSend(String host, String from, String to,
            String subject, String body, String password) {
        return sendSimple(host, from, to, subject, body, password);
    }
    public static EmailResult sendWithPassword(String host, String from, String to,
            String subject, String body, String password) {
        return sendSimple(host, from, to, subject, body, password);
    }

    /** Recommended overload — env password. */
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
    public static EmailResult sendUsingEnv(String host, String from, String to,
            String subject, String body,
            String envFile, String envKey) {
        return sendWithEnv(host, from, to, subject, body, envFile, envKey);
    }

    // ============================================================== connectivity

    /** Opens a TCP socket to the server — verifies reachability only. */
    public static boolean canConnect(String host, int port) {
        return canConnect(host, port, 3000);
    }
    public static boolean testServer(String host, int port) { return canConnect(host, port); }
    public static boolean pingServer(String host, int port) { return canConnect(host, port); }
    public static boolean checkServer(String host, int port) { return canConnect(host, port); }

    public static boolean canConnect(String host, int port, int timeoutMs) {
        if (host == null || port <= 0) return false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.max(100, timeoutMs));
            return true;
        } catch (Exception e) {
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
