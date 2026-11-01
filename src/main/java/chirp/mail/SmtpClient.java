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
 * Minimal but correct SMTP client — supports SMTPS (port 465), STARTTLS
 * (port 587), and plain (port 25). Handles AUTH LOGIN, dot-stuffing,
 * multi-line responses, and CRLF discipline.
 *
 * <p><strong>Internal API.</strong> Students use {@link Email}.</p>
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

    void send(String from, List<String> to, String rawMessage) throws IOException {
        Socket socket = null;
        try {
            socket = ssl ? createSslSocket() : new Socket(host, port);
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
                socket = upgradeToSsl(socket);
                in = newReader(socket);
                out = newWriter(socket);
                command(out, "EHLO " + localHostname());
                expect(in, 250);
            }

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
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    if (log != null) log.add("C: socket close failed: " + e.getMessage());
                }
            }
        }
    }

    // ---------------------------------------------------- auth

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

    private Socket createSslSocket() throws IOException {
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return f.createSocket(host, port);
    }

    private Socket upgradeToSsl(Socket plain) throws IOException {
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return f.createSocket(plain, host, port, true);
    }

    private static String localHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "localhost";
        }
    }
}
