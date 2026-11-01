package chirp.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Strict, safe parser for {@code .env} files.
 *
 * <h2>Naming</h2>
 * <p>{@code load / read / parse / open / fromFile}; {@code get / value /
 * lookup / read} (single-key); {@code getOrDefault / defaultValue / orElse};
 * {@code has / contains / exists}; {@code all / allEntries / map / asMap}.</p>
 *
 * <h2>Supported syntax</h2>
 * <ul>
 *   <li>{@code KEY=VALUE} — plain value</li>
 *   <li>{@code KEY="quoted value"} — quoted; {@code \n}, {@code \t}, {@code \"}, {@code \\} escapes</li>
 *   <li>{@code KEY='literal value'} — single-quoted; no escapes</li>
 *   <li>{@code # comment} — lines starting with {@code #} are ignored</li>
 * </ul>
 *
 * <p>Keys must match {@code [A-Za-z_][A-Za-z0-9_]*}. Invalid lines throw
 * {@link EmailException}.</p>
 */
public final class EnvLoader {

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private EnvLoader() {}

    // ============================================================== load

    public static Map<String, String> load(String path) {
        if (path == null || path.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV file path is required");
        }
        return load(Path.of(path));
    }
    public static Map<String, String> read(String path) { return load(path); }
    public static Map<String, String> parse(String path) { return load(path); }
    public static Map<String, String> open(String path) { return load(path); }
    public static Map<String, String> fromFile(String path) { return load(path); }

    public static Map<String, String> load(Path path) {
        if (path == null) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV file path is required");
        }
        if (!Files.exists(path)) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV file not found: " + path.toAbsolutePath());
        }
        if (!Files.isRegularFile(path)) {
            throw new EmailException(EmailException.Reason.VALIDATION,
                    "ENV path is not a regular file: " + path.toAbsolutePath());
        }
        try {
            return loadFromString(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new EmailException(EmailException.Reason.TRANSPORT,
                    "Cannot read ENV file: " + path.toAbsolutePath(), e);
        }
    }

    /** Parses .env content directly — useful for tests and inline config. */
    public static Map<String, String> loadFromString(String content) {
        Map<String, String> result = new LinkedHashMap<>();
        if (content == null) return Collections.unmodifiableMap(result);

        String[] lines = content.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;

            int eq = line.indexOf('=');
            if (eq < 1) {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Invalid .env line " + (i + 1) + ": no '=' found: " + raw);
            }

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            if (!KEY_PATTERN.matcher(key).matches()) {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Invalid .env key at line " + (i + 1) + ": '" + key
                                + "' (must match [A-Za-z_][A-Za-z0-9_]*)");
            }

            result.put(key, parseValue(value, i + 1, raw));
        }
        return Collections.unmodifiableMap(result);
    }
    public static Map<String, String> fromString(String content) { return loadFromString(content); }

    private static String parseValue(String raw, int lineNum, String original) {
        if (raw.isEmpty()) return "";
        char first = raw.charAt(0);
        if (first == '"') {
            if (raw.length() < 2 || raw.charAt(raw.length() - 1) != '"') {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Unterminated double-quoted value at .env line " + lineNum + ": " + original);
            }
            return unescape(raw.substring(1, raw.length() - 1));
        }
        if (first == '\'') {
            if (raw.length() < 2 || raw.charAt(raw.length() - 1) != '\'') {
                throw new EmailException(EmailException.Reason.VALIDATION,
                        "Unterminated single-quoted value at .env line " + lineNum + ": " + original);
            }
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) { sb.append(c); continue; }
            char next = s.charAt(++i);
            switch (next) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case '\\' -> sb.append('\\');
                case '"' -> sb.append('"');
                default -> {
                    sb.append('\\');
                    sb.append(next);
                }
            }
        }
        return sb.toString();
    }

    // ============================================================== get

    public static String get(String path, String key) {
        Map<String, String> env = load(path);
        if (key == null || key.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV key name is required");
        }
        if (!env.containsKey(key)) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "Key '" + key + "' not found in ENV file " + path
                            + ". Available: " + env.keySet());
        }
        String value = env.get(key);
        if (value == null || value.isEmpty()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV value for key '" + key + "' is empty");
        }
        return value;
    }
    public static String value(String path, String key) { return get(path, key); }
    public static String lookup(String path, String key) { return get(path, key); }

    /** Like {@link #get} but returns {@code fallback} instead of throwing. */
    public static String getOrDefault(String path, String key, String fallback) {
        try {
            return get(path, key);
        } catch (EmailException e) {
            return fallback;
        }
    }
    public static String defaultValue(String path, String key, String fallback) {
        return getOrDefault(path, key, fallback);
    }
    public static String orElse(String path, String key, String fallback) { return getOrDefault(path, key, fallback); }

    // ============================================================== has

    public static boolean has(String path, String key) {
        if (path == null || key == null) return false;
        try {
            Map<String, String> env = load(path);
            return env.containsKey(key) && env.get(key) != null && !env.get(key).isEmpty();
        } catch (EmailException e) {
            return false;
        }
    }
    public static boolean contains(String path, String key) { return has(path, key); }
    public static boolean exists(String path, String key) { return has(path, key); }

    // ============================================================== all

    public static Map<String, String> all(String path) {
        return load(path);
    }
    public static Map<String, String> allEntries(String path) { return all(path); }
    public static Map<String, String> map(String path) { return all(path); }
    public static Map<String, String> asMap(String path) { return all(path); }
}
