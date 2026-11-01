package chirp.mail;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Minimal but correct SMTP client — supports SMTPS (port 465),
 * STARTTLS (port 587), and plain (port 25).
 *
 * <p><strong>Internal API.</strong> Students use {@link Email}. This
 * class is package-private and its methods are not part of the public
 * contract — the shape may change between minor versions.</p>
 *
 * <h2>What it handles</h2>
 * <ul>
 *   <li>The SMTP dialogue: banner, EHLO, STARTTLS upgrade, AUTH LOGIN,
 *       MAIL FROM, RCPT TO (per recipient), DATA, dot-stuffing, QUIT</li>
 *   <li>Multi-line responses (the server sends a 3-digit code and a
 *       separator per line; the last line has a space in position 3)</li>
 *   <li>STARTTLS upgrade — the socket is re-wrapped in an SSLSocket and
 *       a fresh reader/writer pair is opened on the upgraded stream</li>
 *   <li>CRLF discipline — every command line ends in {@code \r\n}, dot
 *       lines have an extra {@code .} prefix (RFC 5321 §4.5.2)</li>
 *   <li>Idle timeout — 30 seconds, so a dead server does not hang the
 *       caller forever</li>
 * </ul>
 *
 * <h2>Resource lifecycle</h2>
 * <p>The raw socket is managed by the outer try-with-resources. On the
 * STARTTLS path, the layered SSL socket is opened in its own nested
 * try-with-resources with {@code autoClose=true}, so closing it also
 * closes the underlying raw socket. The reader/writer wrapping each
 * socket's streams are closed transitively by their socket's close.</p>
 *
 * <h2>Listener notifications</h2>
 * <p>If a {@link EmailListener} is provided, every SMTP dialogue line
 * (client or server) is forwarded to {@link EmailListener#onSmtpEvent(String)}.
 * This is how the {@link EmailResult#smtpLog()} gets populated. If the
 * listener throws, the exception is recorded as a
 * {@code "! listener threw on onSmtpEvent: ..."} line in the log — the
 * send continues.</p>
 *
 * <h2>Testing</h2>
 * <p>The transport is not unit-tested against a real SMTP server (that
 * would require credentials and network). It is exercised
 * end-to-end only by manual smoke tests. The string-building parts
 * (base64 auth, dot-stuffing, response parsing) are simple enough to
 * verify by inspection.</p>
 *
 * @see Email
 * @see EmailRequest
 * @see EmailResult
 * @see EmailListener
 * @since 1.0.0
 */
final class SmtpClient {

    private static final int TIMEOUT_MS = 30_000;

    private final String host;
    private final int port;
    private final boolean ssl;
    private final boolean starttls;
    private final String username;
    private final String password;
    private final EmailListener listener;
    private final List<String> log;

    /**
     * Constructs a client. All fields are final — the client is
     * single-use; create a new one per send.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @param ssl whether to use implicit TLS (SMTPS, port 465)
     * @param starttls whether to upgrade via STARTTLS (port 587)
     * @param username the SMTP username (may be null for no auth)
     * @param password the SMTP password (may be null for no auth)
     * @param listener the listener to notify (may be null)
     * @param log the list to append dialogue lines to (may be null)
     * @since 1.0.0
     */
    SmtpClient(String host, int port, boolean ssl, boolean starttls,
            String username, String password,
            EmailListener listener, List<String> log) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.starttls = starttls;
        this.username = username;
        this.password = password;
        this.listener = listener;
        this.log = log;
    }

    /**
     * Runs the full SMTP dialogue and delivers the message.
     *
     * <p>Sequence: connect, EHLO, optional STARTTLS upgrade, optional
     * AUTH LOGIN, MAIL FROM, RCPT TO per recipient, DATA, dot-stuffed
     * message, QUIT. Any unexpected response throws {@link IOException}
     * with the actual SMTP reply in the message, so the caller can see
     * exactly which step failed.</p>
     *
     * @param from the sender address (must be a valid SMTP envelope
     * sender — no display name)
     * @param to the recipient addresses
     * @param rawMessage the full MIME message (headers + body,
     * CRLF-separated)
     * @throws IOException if the connection fails, a response is
     * unexpected, or the server rejects the message
     * @since 1.0.0
     */
    @SuppressWarnings("PMD.CloseResource") // BufferedReader/Writer wrap the socket's streams; closing the socket closes them.
    void send(String from, List<String> to, String rawMessage) throws IOException {
        try (Socket socket = openSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            BufferedReader in = newReader(socket);
            PrintWriter out = newWriter(socket);

            expect(in, 220);                            // banner
            command(out, "EHLO " + localHostname());
            List<String> ehlo = expect(in, 250);

            if (starttls) {
                boolean supportsTls = ehlo.stream()
                        .anyMatch(l -> l.toUpperCase(Locale.ROOT).contains("STARTTLS"));
                if (!supportsTls) throw new IOException("Server does not advertise STARTTLS");
                command(out, "STARTTLS");
                expect(in, 220);

                // Layered SSL socket — created with autoClose=true, so closing
                // this socket closes the wrapped raw socket too. The outer
                // try-with-resources' socket.close() is then a harmless no-op.
                try (Socket upgraded = upgradeToSsl(socket)) {
                    BufferedReader upgradedIn = newReader(upgraded);
                    PrintWriter upgradedOut = newWriter(upgraded);
                    command(upgradedOut, "EHLO " + localHostname());
                    expect(upgradedIn, 250);
                    conversation(from, to, rawMessage, upgradedIn, upgradedOut);
                }
            } else {
                conversation(from, to, rawMessage, in, out);
            }
        }
    }

    /**
     * Opens the initial socket — SSL or plain, per configuration.
     *
     * @return a connected socket (SSL-wrapped if {@code ssl})
     * @throws IOException if the connection fails
     * @since 1.0.0
     */
    private Socket openSocket() throws IOException {
        if (ssl) {
            SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return f.createSocket(host, port);
        }
        return new Socket(host, port);
    }

    // ---------------------------------------------------- conversation

    /**
     * Runs the dialogue after connection setup is complete — auth, send,
     * quit. Extracted so the STARTTLS branch and the plain branch share
     * the same logic, with different reader/writer pairs.
     *
     * @param from the sender address
     * @param to the recipients
     * @param rawMessage the MIME message
     * @param in the reader for the current socket
     * @param out the writer for the current socket
     * @throws IOException if any step fails
     * @since 1.0.0
     */
    private void conversation(String from, List<String> to, String rawMessage,
            BufferedReader in, PrintWriter out) throws IOException {
        if (username != null && password != null) {
            authenticate(out, in);
        }

        command(out, "MAIL FROM:<" + from + ">");
        expect(in, 250);

        for (String recipient : to) {
            command(out, "RCPT TO:<" + recipient + ">");
            expectAny(in, 250, 251);
        }

        command(out, "DATA");
        expect(in, 354);
        writeData(out, rawMessage);
        command(out, ".");
        expect(in, 250);

        command(out, "QUIT");
        // 221 usually, but be lenient
    }

    // ---------------------------------------------------- auth

    /**
     * Performs AUTH LOGIN — sends the username and password as base64
     * in two separate commands, expecting a 334 prompt before each and
     * a 235 success at the end.
     *
     * @param out the writer
     * @param in the reader
     * @throws IOException if the server rejects the credentials or the
     * conversation breaks
     * @since 1.0.0
     */
    private void authenticate(PrintWriter out, BufferedReader in) throws IOException {
        command(out, "AUTH LOGIN");
        expect(in, 334);
        command(out, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
        expect(in, 334);
        command(out, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
        expect(in, 235);
    }

    // ---------------------------------------------------- low-level

    private BufferedReader newReader(Socket s) throws IOException {
        return new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
    }

    private PrintWriter newWriter(Socket s) throws IOException {
        return new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    private void command(PrintWriter out, String line) {
        log("C: " + line);
        out.print(line + "\r\n");
        out.flush();
    }

    private List<String> expect(BufferedReader in, int expected) throws IOException {
        List<String> lines = readResponse(in);
        int code = parseCode(lines);
        if (code != expected) {
            throw new IOException("Expected " + expected + " but got " + code + ": " + lines);
        }
        return lines;
    }

    private void expectAny(BufferedReader in, int... codes) throws IOException {
        List<String> lines = readResponse(in);
        int code = parseCode(lines);
        for (int c : codes) if (code == c) return;
        throw new IOException("Expected one of " + java.util.Arrays.toString(codes)
                + " but got " + code + ": " + lines);
    }

    private List<String> readResponse(BufferedReader in) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null) {
            log("S: " + line);
            lines.add(line);
            if (line.length() >= 4 && line.charAt(3) == ' ') break;
            if (line.length() < 4) break;
        }
        if (lines.isEmpty()) {
            throw new SocketTimeoutException("Empty SMTP response (timeout?)");
        }
        return lines;
    }

    private int parseCode(List<String> lines) {
        if (lines.isEmpty()) return -1;
        String first = lines.get(0);
        if (first.length() < 3) return -1;
        try {
            return Integer.parseInt(first.substring(0, 3));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void writeData(PrintWriter out, String rawMessage) {
        String normalized = rawMessage.replace("\r\n", "\n").replace("\r", "\n");
        for (String line : normalized.split("\n", -1)) {
            if (line.startsWith(".")) {
                out.print("." + line + "\r\n");
            } else {
                out.print(line + "\r\n");
            }
        }
        out.flush();
    }

    private void log(String line) {
        if (log != null) log.add(line);
        if (listener != null) {
            try {
                listener.onSmtpEvent(line);
            } catch (RuntimeException e) {
                // Listener is best-effort — record the failure in the SMTP log
                // so it does not vanish silently, but never break the send.
                if (log != null) log.add("! listener threw on onSmtpEvent: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------- sockets

    /**
     * Wraps the plain socket in an SSL socket for the STARTTLS upgrade.
     * The {@code autoClose=true} argument ensures that closing the SSL
     * socket also closes the underlying plain socket.
     *
     * @param plain the already-connected plain socket
     * @return the SSL-wrapped socket
     * @throws IOException if the TLS handshake fails
     * @since 1.0.0
     */
    private Socket upgradeToSsl(Socket plain) throws IOException {
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return f.createSocket(plain, host, port, true);
    }

    /**
     * Returns the local machine's hostname for the EHLO command, or
     * {@code "localhost"} if it cannot be determined.
     *
     * @return the hostname
     * @since 1.0.0
     */
    private static String localHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "localhost";
        }
    }
}
