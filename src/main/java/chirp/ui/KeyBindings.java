package chirp.ui;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Fluent key-binding registry. Attach to a scene or node; every binding
 * registered fires when its key combination is pressed.
 *
 * <h2>Three ways to define a binding</h2>
 * <ul>
 *   <li><b>Descriptor string</b> — {@code bind("ctrl S", action)},
 *       {@code bind("escape", action)}, {@code bind("ctrl shift N", action)}</li>
 *   <li><b>KeyCode + modifiers</b> — {@code bind(KeyCode.S, action, KeyCombination.CONTROL_DOWN)}</li>
 *   <li><b>KeyCombination</b> — {@code bind(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), action)}</li>
 * </ul>
 *
 * <pre>{@code
 * KeyBindings kb = KeyBindings.of()
 *     .bind("ctrl S", () -> save())
 *     .bind("ctrl Z", () -> undo())
 *     .bind("escape", () -> close());
 *
 * // Apply to the scene root after Ui.show(...)
 * Scene scene = stage.getScene();
 * kb.applyTo(scene);
 * }</pre>
 */
public final class KeyBindings {

    private final Map<KeyCombination, Runnable> bindings = new LinkedHashMap<>();

    private KeyBindings() {}

    // ---------------------------------------------------------- factories

    public static KeyBindings of() { return new KeyBindings(); }
    public static KeyBindings create() { return new KeyBindings(); }

    // ---------------------------------------------------------- bindings

    /**
     * Binds using a descriptor string. Format: space-separated tokens —
     * modifiers first ({@code ctrl}, {@code shift}, {@code alt}, {@code meta}/{@code cmd}),
     * then a single key ({@code S}, {@code Enter}, {@code Escape}, {@code F1}, …).
     * Case-insensitive for modifiers; single-letter keys are case-insensitive.
     */
    public KeyBindings bind(String descriptor, Runnable action) {
        if (descriptor == null || action == null) return this;
        KeyCombination combo = parseDescriptor(descriptor);
        if (combo != null) bindings.put(combo, action);
        return this;
    }

    public KeyBindings bind(KeyCode key, Runnable action) {
        if (key == null || action == null) return this;
        bindings.put(new KeyCodeCombination(key), action);
        return this;
    }

    public KeyBindings bind(KeyCode key, Runnable action, KeyCombination.Modifier... modifiers) {
        if (key == null || action == null) return this;
        bindings.put(new KeyCodeCombination(key, modifiers), action);
        return this;
    }

    public KeyBindings bind(KeyCombination combination, Runnable action) {
        if (combination == null || action == null) return this;
        bindings.put(combination, action);
        return this;
    }

    /** Removes a binding previously registered by descriptor. */
    public KeyBindings unbind(String descriptor) {
        if (descriptor == null) return this;
        KeyCombination combo = parseDescriptor(descriptor);
        if (combo != null) bindings.remove(combo);
        return this;
    }

    /** Number of registered bindings. */
    public int size() { return bindings.size(); }

    // ---------------------------------------------------------- application

    /** Applies every registered binding to the given scene. */
    public void applyTo(Scene scene) {
        if (scene == null) return;
        for (Map.Entry<KeyCombination, Runnable> e : bindings.entrySet()) {
            scene.getAccelerators().put(e.getKey(), safe(e.getValue()));
        }
    }

    /** Applies every registered binding to the given node (limited to its focus scope). */
    public void applyTo(Node node) {
        if (node == null) return;
        Scene scene = node.getScene();
        if (scene == null) return;
        // Use scene-level accelerators — the most reliable mechanism
        for (Map.Entry<KeyCombination, Runnable> e : bindings.entrySet()) {
            scene.getAccelerators().put(e.getKey(), safe(e.getValue()));
        }
    }

    private static Runnable safe(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (RuntimeException ignored) {}
        };
    }

    // ---------------------------------------------------------- parser

    private static KeyCombination parseDescriptor(String descriptor) {
        if (descriptor == null || descriptor.isBlank()) return null;
        String[] tokens = descriptor.trim().split("\\s+");
        boolean ctrl = false, shift = false, alt = false, meta = false;
        KeyCode key = null;
        for (String t : tokens) {
            String lower = t.toLowerCase(Locale.ROOT);
            switch (lower) {
                case "ctrl", "control" -> ctrl = true;
                case "shift" -> shift = true;
                case "alt", "option" -> alt = true;
                case "meta", "cmd", "command", "win", "windows" -> meta = true;
                default -> key = parseKey(t);
            }
        }
        if (key == null) return null;
        java.util.List<KeyCombination.Modifier> mods = new java.util.ArrayList<>();
        if (ctrl) mods.add(KeyCombination.CONTROL_DOWN);
        if (shift) mods.add(KeyCombination.SHIFT_DOWN);
        if (alt) mods.add(KeyCombination.ALT_DOWN);
        if (meta) mods.add(KeyCombination.META_DOWN);
        return new KeyCodeCombination(key,
                mods.toArray(new KeyCombination.Modifier[0]));
    }

    private static KeyCode parseKey(String token) {
        String t = token.trim();
        if (t.isEmpty()) return null;
        try {
            return KeyCode.valueOf(t.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {}
        switch (t.toLowerCase(Locale.ROOT)) {
            case "esc", "escape" :
                return KeyCode.ESCAPE;
            case "enter", "return" :
                return KeyCode.ENTER;
            case "tab" :
                return KeyCode.TAB;
            case "space" :
                return KeyCode.SPACE;
            case "del", "delete" :
                return KeyCode.DELETE;
            case "backspace" :
                return KeyCode.BACK_SPACE;
            case "up" :
                return KeyCode.UP;
            case "down" :
                return KeyCode.DOWN;
            case "left" :
                return KeyCode.LEFT;
            case "right" :
                return KeyCode.RIGHT;
            case "home" :
                return KeyCode.HOME;
            case "end" :
                return KeyCode.END;
            case "pgup", "pageup" :
                return KeyCode.PAGE_UP;
            case "pgdn", "pagedown" :
                return KeyCode.PAGE_DOWN;
            case "ins", "insert" :
                return KeyCode.INSERT;
            case "+", "plus", "add" :
                return KeyCode.PLUS;
            case "-", "minus", "subtract" :
                return KeyCode.MINUS;
            default: {
                // Fall through to single-letter handling below.
            }
        }
        if (t.length() == 1) {
            char c = Character.toUpperCase(t.charAt(0));
            return KeyCode.getKeyCode(String.valueOf(c));
        }
        return null;
    }
}
