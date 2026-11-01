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
 * <p>Reads a standard {@code KEY=VALUE} file and returns a map of the
 * entries. Used by {@link PasswordStrategy#ENV} to fetch the SMTP
 * password without hardcoding it in source code.</p>
 *
 * <h2>Supported syntax</h2>
 * <ul>
 *   <li>{@code KEY=VALUE} — plain value</li>
 *   <li>{@code KEY="quoted value"} — quoted; {@code \n}, {@code \t},
 *       {@code \"}, {@code \\} escapes are processed</li>
 *   <li>{@code KEY='literal value'} — single-quoted; no escapes</li>
 *   <li>{@code # comment} — lines starting with {@code #} are ignored</li>
 *   <li>Blank lines are ignored</li>
 * </ul>
 *
 * <h2>Validation</h2>
 * <p>Keys must match {@code [A-Za-z_][A-Za-z0-9_]*}. Invalid keys,
 * unterminated quotes, and lines without an {@code =} all throw
 * {@link EmailException} with the line number — this is the "fail fast
 * on malformed config" choice, because a silently-skipped line would
 * lead to a mysterious authentication failure later.</p>
 *
 * <h2>Example — the common case</h2>
 * <pre>{@code
 * // .env file:
 * SMTP_HOST=smtp.gmail.com
 * SMTP_USER=me@gmail.com
 * SMTP_PASSWORD=app-specific-password
 *
 * // In Java:
 * Map<String, String> env = EnvLoader.load(".env");
 * String host = env.get("SMTP_HOST");
 * String user = env.get("SMTP_USER");
 * }</pre>
 *
 * <h2>Example — the strict getter</h2>
 * <pre>{@code
 * // Throws with a helpful message if the key is missing or empty.
 * String password = EnvLoader.get(".env", "SMTP_PASSWORD");
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the mail-send configuration that uses this loader, see
 * {@link EmailRequest#passwordFromEnv(String, String)}. For the strategy
 * enum, see {@link PasswordStrategy}.</p>
 *
 * @see EmailRequest#passwordFromEnv(String, String)
 * @see PasswordStrategy#ENV
 * @since 1.0.0
 */
public final class EnvLoader {

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private EnvLoader() {}

    // ============================================================== load

    /**
     * Reads and parses the given {@code .env} file.
     *
     * <p>Throws {@link EmailException} on missing file, unreadable file,
     * or malformed content. Never returns null — returns an empty
     * (unmodifiable) map for an empty file.</p>
     *
     * @param path the {@code .env} file path (may be null → throws)
     * @return the parsed entries, unmodifiable
     * @throws EmailException if the file is missing, unreadable, or
     * malformed
     * @see #read(String)
     * @see #parse(String)
     * @see #open(String)
     * @see #fromFile(String)
     * @since 1.0.0
     */
    public static Map<String, String> load(String path) {
        if (path == null || path.isBlank()) {
            throw new EmailException(EmailException.Reason.MISSING_CONFIG,
                    "ENV file path is required");
        }
        return load(Path.of(path));
    }

    /**
     * Alias of {@link #load(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed entries
     * @see #load(String)
     * @since 1.0.0
     */
    public static Map<String, String> read(String path) { return load(path); }

    /**
     * Alias of {@link #load(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed entries
     * @see #load(String)
     * @since 1.0.0
     */
    public static Map<String, String> parse(String path) { return load(path); }

    /**
     * Alias of {@link #load(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed entries
     * @see #load(String)
     * @since 1.0.0
     */
    public static Map<String, String> open(String path) { return load(path); }

    /**
     * Alias of {@link #load(String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @return the parsed entries
     * @see #load(String)
     * @since 1.0.0
     */
    public static Map<String, String> fromFile(String path) { return load(path); }

    /**
     * Reads and parses the given {@code .env} file path.
     *
     * @param path the file path (may be null → throws)
     * @return the parsed entries, unmodifiable
     * @throws EmailException if the file is missing, unreadable, or
     * malformed
     * @see #load(String)
     * @since 1.0.0
     */
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

    /**
     * Parses {@code .env} content directly from a string. Useful for
     * tests and inline configuration.
     *
     * @param content the {@code .env} text (null → empty map)
     * @return the parsed entries, unmodifiable
     * @throws EmailException if the content is malformed
     * @see #load(String)
     * @see #fromString(String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #loadFromString(String)} — see that method for
     * full documentation.
     *
     * @param content the {@code .env} text
     * @return the parsed entries
     * @see #loadFromString(String)
     * @since 1.0.0
     */
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

    /**
     * Reads a required key from the file. Throws {@link EmailException}
     * with a helpful message if the file is missing, the key is missing,
     * or the value is empty.
     *
     * <pre>{@code
     * String password = EnvLoader.get(".env", "SMTP_PASSWORD");
     * }</pre>
     *
     * @param path the file path (may be null → throws)
     * @param key the key to look up (may be null or blank → throws)
     * @return the value
     * @throws EmailException if the file, key, or value is missing or empty
     * @see #value(String, String)
     * @see #lookup(String, String)
     * @see #getOrDefault(String, String, String)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #get(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @param key the key
     * @return the value
     * @see #get(String, String)
     * @since 1.0.0
     */
    public static String value(String path, String key) { return get(path, key); }

    /**
     * Alias of {@link #get(String, String)} — see that method for full
     * documentation and examples.
     *
     * @param path the file path
     * @param key the key
     * @return the value
     * @see #get(String, String)
     * @since 1.0.0
     */
    public static String lookup(String path, String key) { return get(path, key); }

    /**
     * Reads a key from the file, returning {@code fallback} if the file,
     * key, or value is missing.
     *
     * @param path the file path
     * @param key the key
     * @param fallback the fallback value
     * @return the value, or the fallback
     * @see #defaultValue(String, String, String)
     * @see #orElse(String, String, String)
     * @see #get(String, String)
     * @since 1.0.0
     */
    public static String getOrDefault(String path, String key, String fallback) {
        try {
            return get(path, key);
        } catch (EmailException e) {
            return fallback;
        }
    }

    /**
     * Alias of {@link #getOrDefault(String, String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param key the key
     * @param fallback the fallback value
     * @return the value, or the fallback
     * @see #getOrDefault(String, String, String)
     * @since 1.0.0
     */
    public static String defaultValue(String path, String key, String fallback) {
        return getOrDefault(path, key, fallback);
    }

    /**
     * Alias of {@link #getOrDefault(String, String, String)} — see that
     * method for full documentation.
     *
     * @param path the file path
     * @param key the key
     * @param fallback the fallback value
     * @return the value, or the fallback
     * @see #getOrDefault(String, String, String)
     * @since 1.0.0
     */
    public static String orElse(String path, String key, String fallback) {
        return getOrDefault(path, key, fallback);
    }

    // ============================================================== has

    /**
     * Checks whether the file exists, contains the key, and the value is
     * non-empty. Returns {@code false} on any failure — never throws.
     *
     * @param path the file path
     * @param key the key
     * @return {@code true} if the key is present with a non-empty value
     * @see #contains(String, String)
     * @see #exists(String, String)
     * @since 1.0.0
     */
    public static boolean has(String path, String key) {
        if (path == null || key == null) return false;
        try {
            Map<String, String> env = load(path);
            return env.containsKey(key) && env.get(key) != null && !env.get(key).isEmpty();
        } catch (EmailException e) {
            return false;
        }
    }

    /**
     * Alias of {@link #has(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param key the key
     * @return {@code true} if present with a non-empty value
     * @see #has(String, String)
     * @since 1.0.0
     */
    public static boolean contains(String path, String key) { return has(path, key); }

    /**
     * Alias of {@link #has(String, String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @param key the key
     * @return {@code true} if present with a non-empty value
     * @see #has(String, String)
     * @since 1.0.0
     */
    public static boolean exists(String path, String key) { return has(path, key); }

    // ============================================================== all

    /**
     * Returns all entries in the file as a map.
     *
     * @param path the file path
     * @return the entries
     * @see #allEntries(String)
     * @see #map(String)
     * @see #asMap(String)
     * @since 1.0.0
     */
    public static Map<String, String> all(String path) {
        return load(path);
    }

    /**
     * Alias of {@link #all(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the entries
     * @see #all(String)
     * @since 1.0.0
     */
    public static Map<String, String> allEntries(String path) { return all(path); }

    /**
     * Alias of {@link #all(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the entries
     * @see #all(String)
     * @since 1.0.0
     */
    public static Map<String, String> map(String path) { return all(path); }

    /**
     * Alias of {@link #all(String)} — see that method for full
     * documentation.
     *
     * @param path the file path
     * @return the entries
     * @see #all(String)
     * @since 1.0.0
     */
    public static Map<String, String> asMap(String path) { return all(path); }
}
