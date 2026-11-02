package chirp.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Detects — once, at class load — what the current terminal can do.
 *
 * <p>Unicode detection on Windows is the tricky one: Java 18+ reports
 * {@code sun.stdout.encoding=UTF-8} even when the console codepage is still
 * 437/1251, so UTF-8 bytes get rendered as mojibake. The only reliable check
 * is to ask Windows for its active console codepage via {@code chcp}. If the
 * codepage is not 65001, unicode is disabled even though Java thinks the
 * output stream is UTF-8.</p>
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
        // 1. Explicit override wins
        String override = System.getProperty("chirp.ansi");
        if (override != null) return Boolean.parseBoolean(override);

        // 2. NO_COLOR opt-out (per https://no-color.org/)
        if (System.getenv("NO_COLOR") != null) return false;

        // 3. Program-identifying environment variables
        if (System.getenv("WT_SESSION") != null) return true;                 // Windows Terminal
        if ("vscode".equalsIgnoreCase(System.getenv("TERM_PROGRAM"))) return true;
        if ("true".equalsIgnoreCase(System.getenv("ConEmuANSI"))) return true;
        String emulator = System.getenv("TERMINAL_EMULATOR");
        if (emulator != null && emulator.contains("JetBrains")) return true;

        // 4. Generic capability signals
        String colorTerm = System.getenv("COLORTERM");
        if (colorTerm != null) {
            String lower = colorTerm.toLowerCase(Locale.ROOT);
            if (lower.contains("truecolor") || lower.contains("24bit")) return true;
        }
        String term = System.getenv("TERM");
        if (term != null && !term.isBlank() && !"dumb".equalsIgnoreCase(term)) return true;

        // 5. OS fallback — Unix-family terminals default to ANSI-capable
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return !os.contains("win");
    }

    private static boolean detectUnicode() {
        // 1. Explicit property overrides come first
        String ascii = System.getProperty("chirp.ascii");
        if (ascii != null && Boolean.parseBoolean(ascii)) return false;

        String override = System.getProperty("chirp.unicode");
        if (override != null) return Boolean.parseBoolean(override);

        // 2. Platform-specific detection
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            // Windows: trust ONLY the console codepage, not sun.stdout.encoding
            return isWindowsUtf8();
        }

        // 3. Unix / macOS: sun.stdout.encoding is authoritative
        String stdoutEnc = System.getProperty("sun.stdout.encoding", "").toLowerCase(Locale.ROOT);
        if (!stdoutEnc.isEmpty()) return stdoutEnc.contains("utf");

        try {
            String name = Charset.defaultCharset().name().toLowerCase(Locale.ROOT);
            return name.contains("utf-8") || name.contains("utf8");
        } catch (RuntimeException ignored) {}

        // macOS and Linux terminals are UTF-8 by default
        return true;
    }

    /**
     * Runs {@code chcp} once and checks for codepage 65001 (UTF-8).
     * Cached at class load — safe to call repeatedly thereafter.
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
            return false; // safest fallback on Windows
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
