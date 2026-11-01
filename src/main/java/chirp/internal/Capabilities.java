package chirp.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Detects — once, at class load — what the current terminal supports:
 * ANSI colour, Unicode glyphs, and 24-bit true colour.
 *
 * <p><b>Internal API.</b> Do not use directly. Use
 * {@link chirp.config.Config#colorEnabled(boolean)},
 * {@link chirp.startup.StartupConfig.Builder#noUnicode()}, and the
 * {@link Glyphs} style selection instead. This class is the detection
 * layer that those user-facing toggles consult.</p>
 *
 * <h2>Why detection is nontrivial</h2>
 *
 * <h3>Windows Unicode is the hard case</h3>
 * <p>Java 18+ reports {@code sun.stdout.encoding=UTF-8} even when the
 * Windows console codepage is still 437 or 1251 — so UTF-8 bytes get
 * written correctly but rendered as mojibake. The only reliable check
 * is to ask Windows for its active console codepage via {@code chcp}
 * and verify it is 65001 (UTF-8). The {@link #isWindowsUtf8()} method
 * does this — it is why the class is not a simple one-liner.</p>
 *
 * <h3>ANSI</h3>
 * <p>On Unix, the presence of {@code TERM} (and its value not being
 * {@code "dumb"}) is enough. On Windows, the presence of
 * {@code WT_SESSION} (Windows Terminal) or {@code ConEmuANSI=ON}
 * indicates a modern terminal; a plain {@code cmd.exe} without those
 * is treated as non-ANSI. The {@code NO_COLOR} env var overrides
 * everything.</p>
 *
 * <h3>True colour</h3>
 * <p>{@code COLORTERM=truecolor} / {@code COLORTERM=24bit} or a
 * {@code TERM} value containing {@code 256color} enables 24-bit RGB
 * escapes. Absent those, {@link Ansi#rgb(int, int, int)} falls back
 * to no-op.</p>
 *
 * <h2>System-property overrides</h2>
 * <p>Every detection can be forced from the command line — useful for
 * tests and for bypassing the heuristics when they guess wrong:</p>
 * <ul>
 *   <li>{@code -Dchirp.ansi=true|false} — force ANSI on or off</li>
 *   <li>{@code -Dchirp.unicode=true|false} — force Unicode on or off</li>
 *   <li>{@code -Dchirp.ascii=true} — force ASCII (shorthand for
 *       unicode=false)</li>
 * </ul>
 *
 * <h2>Test hook</h2>
 * <p>{@link #setUnicodeOverride(Boolean)} overrides the Unicode result
 * at runtime — used by the library's own tests to exercise both code
 * paths without depending on the build machine's console.</p>
 *
 * <h2>Locale</h2>
 * <p>All case-insensitive comparisons use {@link Locale#ROOT} so
 * detection is identical on a Turkish-locale machine (where
 * {@code "I".toLowerCase()} would otherwise produce the dotless
 * {@code "ı"}).</p>
 *
 * @see Ansi
 * @see Glyphs
 * @see chirp.startup.StartupConfig
 * @since 1.0.0
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

    /**
     * Whether the terminal supports ANSI escape sequences. Detected once
     * at class load — this method reports the cached result.
     *
     * @return {@code true} if ANSI is supported
     * @see Ansi#isSupported()
     * @since 1.0.0
     */
    public static boolean ansi() { return ANSI; }

    /**
     * Whether the terminal supports 24-bit true-colour escape sequences.
     * Requires ANSI support in addition to the {@code COLORTERM} /
     * {@code TERM} signals.
     *
     * @return {@code true} if 24-bit RGB escapes will render correctly
     * @see Ansi#rgb(int, int, int)
     * @since 1.0.0
     */
    public static boolean trueColor() { return ANSI && TRUE_COLOR; }

    /**
     * Whether the terminal can render Unicode glyphs. Respects the
     * runtime override set by {@link #setUnicodeOverride(Boolean)} —
     * once the override is set, this returns it instead of the
     * class-load detection.
     *
     * @return {@code true} if Unicode glyphs are safe to emit
     * @see Glyphs
     * @see #setUnicodeOverride(Boolean)
     * @since 1.0.0
     */
    public static boolean unicode() {
        Boolean override = unicodeOverride;
        return override != null ? override : UNICODE;
    }

    /**
     * Forces the Unicode result on ({@code true}), off
     * ({@code false}), or back to the auto-detected value
     * ({@code null}). Intended for tests.
     *
     * @param value the override, or {@code null} to clear it
     * @since 1.0.0
     */
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
