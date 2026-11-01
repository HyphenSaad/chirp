package chirp.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Detects — once, at class load — what the current terminal can do.
 *
 * <p>Unicode detection on Windows uses two independent signals:</p>
 *
 * <ol>
 *   <li><b>{@code stdout.encoding} / {@code sun.stdout.encoding}</b> — the
 *       charset the JVM uses when writing to {@code System.out}. If it says
 *       UTF-8, the JVM produces correct UTF-8 bytes for every code point.</li>
 *   <li><b>{@code cmd /c chcp}</b> — the console codepage reported by a
 *       subprocess. Reliable on a real console, unreliable under ConPTY
 *       (VS Code terminal) where subprocesses may attach to a different
 *       console handle than the parent shell.</li>
 * </ol>
 *
 * <p>The two are combined: {@code stdout.encoding} is trusted first because
 * it is what actually determines the bytes Chirp emits; {@code chcp} is the
 * fallback when the property is missing or ambiguous.</p>
 *
 * <p>System property overrides:</p>
 * <ul>
 *   <li>{@code -Dchirp.ansi=true|false}   — force ANSI on or off</li>
 *   <li>{@code -Dchirp.unicode=true|false} — force unicode on or off</li>
 *   <li>{@code -Dchirp.ascii=true}        — force ASCII (shorthand for unicode=false)</li>
 * </ul>
 *
 * <p>All case-insensitive comparisons use {@link Locale#ROOT} so behaviour is
 * identical on a Turkish-locale machine.</p>
 */
public final class Capabilities {

    private static final boolean ANSI;
    private static final boolean UNICODE;
    private static final boolean TRUE_COLOR;

    static {
        ANSI = detectAnsi();
        UNICODE = detectUnicode();
        TRUE_COLOR = detectTrueColor();
    }

    private static volatile Boolean unicodeOverride = null;

    private Capabilities() {}

    public static boolean ansi() { return ANSI; }
    public static boolean trueColor() { return ANSI && TRUE_COLOR; }

    public static boolean unicode() {
        Boolean override = unicodeOverride;
        return override != null ? override : UNICODE;
    }

    /** Force unicode on ({@code true}), off ({@code false}), or auto ({@code null}). */
    public static void setUnicodeOverride(Boolean value) {
        unicodeOverride = value;
    }

    // ------------------------------------------------------------ detection

    private static boolean detectAnsi() {
        String override = System.getProperty("chirp.ansi");
        if (override != null) return Boolean.parseBoolean(override);
        if (System.getenv("NO_COLOR") != null) return false;
        if (System.getenv("WT_SESSION") != null) return true;              // Windows Terminal
        if ("true".equalsIgnoreCase(System.getenv("ConEmuANSI"))) return true;
        String emulator = System.getenv("TERMINAL_EMULATOR");
        if (emulator != null && emulator.contains("JetBrains")) return true;
        String term = System.getenv("TERM");
        if (term != null && !term.isBlank() && !"dumb".equalsIgnoreCase(term)) return true;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return !os.contains("win");
    }

    private static boolean detectUnicode() {
        // 1. Explicit property overrides come first
        String ascii = System.getProperty("chirp.ascii");
        if (ascii != null && Boolean.parseBoolean(ascii)) return false;

        String override = System.getProperty("chirp.unicode");
        if (override != null) return Boolean.parseBoolean(override);

        // 2. The JVM's stdout encoding is the primary signal. This is the
        //    charset used when Chirp writes to System.out, so it decides
        //    whether the bytes on screen are UTF-8 or not. On Windows this
        //    also survives the ConPTY case where the chcp subprocess sees a
        //    different console handle than the parent shell.
        String stdoutEnc = readStdoutEncoding();
        if (stdoutEnc.contains("utf")) return true;

        // 3. Windows: fall back to the chcp subprocess. Only reached when
        //    stdout encoding is unknown or non-UTF, so we're confirming a
        //    suspicion rather than overriding a positive signal.
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return isWindowsUtf8();
        }

        // 4. Non-Windows: stdout encoding is authoritative when set
        if (!stdoutEnc.isEmpty()) return false;

        // 5. Fallback: default charset
        try {
            String name = Charset.defaultCharset().name().toLowerCase(Locale.ROOT);
            return name.contains("utf-8") || name.contains("utf8");
        } catch (RuntimeException ignored) {}

        // macOS and Linux terminals are UTF-8 by default
        return true;
    }

    /**
     * Reads the JVM's stdout encoding. Tries the Java 18+ property
     * ({@code stdout.encoding}) first, then the older {@code sun.stdout.encoding}
     * backport. Returns lowercased text so {@code contains("utf")} works on
     * both {@code UTF-8} and {@code utf8}.
     */
    private static String readStdoutEncoding() {
        String enc = System.getProperty("stdout.encoding");
        if (enc == null || enc.isEmpty()) {
            enc = System.getProperty("sun.stdout.encoding", "");
        }
        return enc == null ? "" : enc.toLowerCase(Locale.ROOT);
    }

    /**
     * Runs {@code chcp} in a subprocess and checks for codepage 65001 (UTF-8).
     *
     * <p>Reliable on a real console window. Unreliable under ConPTY — see the
     * class-level Javadoc. When this returns {@code false} but
     * {@code stdout.encoding} says UTF-8, {@code detectUnicode()} trusts the
     * encoding property, not the subprocess.</p>
     */
    private static boolean isWindowsUtf8() {
        Process process = null;
        try {
            process = new ProcessBuilder("cmd", "/c", "chcp")
                    .redirectErrorStream(true)
                    .start();
            byte[] bytes = process.getInputStream().readAllBytes();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            String output = new String(bytes, StandardCharsets.ISO_8859_1);
            return output.contains("65001");
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private static boolean detectTrueColor() {
        String colorTerm = System.getenv("COLORTERM");
        if (colorTerm != null) {
            String lower = colorTerm.toLowerCase(Locale.ROOT);
            if (lower.contains("truecolor") || lower.contains("24bit")) return true;
        }
        String term = System.getenv("TERM");
        return term != null && term.toLowerCase(Locale.ROOT).contains("256color");
    }
}
