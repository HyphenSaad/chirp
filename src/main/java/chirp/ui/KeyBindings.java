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
 * Fluent key-binding registry — attach to a scene, every registered
 * binding fires when its key combination is pressed.
 *
 * <h2>Three ways to bind</h2>
 * <ul>
 *   <li><b>Descriptor string</b> — {@code bind("ctrl S", () -> save())},
 *       {@code bind("escape", () -> close())}</li>
 *   <li><b>KeyCode + modifiers</b> — {@code bind(KeyCode.S, () -> save(),
 *       KeyCombination.CONTROL_DOWN)}</li>
 *   <li><b>KeyCombination</b> — {@code bind(new KeyCodeCombination(...), ...)}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * KeyBindings kb = KeyBindings.of()
 *     .bind("ctrl S", () -> save())
 *     .bind("ctrl Z", () -> undo())
 *     .bind("escape", () -> close());
 *
 * // Apply to a scene after Ui.show():
 * kb.applyTo(stage.getScene());
 * }</pre>
 *
 * <h2>Descriptor format</h2>
 * <p>Space-separated tokens — modifiers first, then a single key.
 * Modifiers: {@code ctrl}, {@code shift}, {@code alt}, {@code meta} /
 * {@code cmd} / {@code command} / {@code win} / {@code windows}.
 * Keys: any {@link KeyCode} name ({@code S}, {@code Enter},
 * {@code Escape}, {@code F1}, …) or the aliases {@code esc},
 * {@code return}, {@code tab}, {@code space}, {@code del},
 * {@code backspace}, {@code up}, {@code down}, {@code left},
 * {@code right}, {@code home}, {@code end}, {@code pgup},
 * {@code pgdn}, {@code ins}, {@code +}, {@code -}.</p>
 *
 * <h2>Related</h2>
 * <p>For menu-driven actions, see {@link MenuBar}. For a button, see
 * {@link Button}.</p>
 *
 * @see MenuBar
 * @see Button
 * @since 1.0.0
 */
public final class KeyBindings {

    private final Map<KeyCombination, Runnable> bindings = new LinkedHashMap<>();

    private KeyBindings() {}

    /**
     * Creates an empty key-binding registry.
     *
     * @return a new registry
     * @see #create()
     * @since 1.0.0
     */
    public static KeyBindings of() { return new KeyBindings(); }

    /**
     * Alias of {@link #of()} — see that method for full documentation.
     *
     * @return a new registry
     * @see #of()
     * @since 1.0.0
     */
    public static KeyBindings create() { return new KeyBindings(); }

    /**
     * Binds using a descriptor string. See the class-level Javadoc for
     * the format.
     *
     * @param descriptor the descriptor (null → no-op)
     * @param action the action to run (null → no-op)
     * @return this registry, for chaining
     * @since 1.0.0
     */
    public KeyBindings bind(String descriptor, Runnable action) {
        if (descriptor == null || action == null) return this;
        KeyCombination combo = parseDescriptor(descriptor);
        if (combo != null) bindings.put(combo, action);
        return this;
    }

    /**
     * Binds a bare {@link KeyCode} with no modifiers.
     *
     * @param key the key
     * @param action the action
     * @return this registry
     * @since 1.0.0
     */
    public KeyBindings bind(KeyCode key, Runnable action) {
        if (key == null || action == null) return this;
        bindings.put(new KeyCodeCombination(key), action);
        return this;
    }

    /**
     * Binds a {@link KeyCode} with the given modifiers.
     *
     * @param key the key
     * @param action the action
     * @param modifiers the modifiers (may be empty)
     * @return this registry
     * @since 1.0.0
     */
    public KeyBindings bind(KeyCode key, Runnable action, KeyCombination.Modifier... modifiers) {
        if (key == null || action == null) return this;
        bindings.put(new KeyCodeCombination(key, modifiers), action);
        return this;
    }

    /**
     * Binds a pre-built {@link KeyCombination}.
     *
     * @param combination the combination
     * @param action the action
     * @return this registry
     * @since 1.0.0
     */
    public KeyBindings bind(KeyCombination combination, Runnable action) {
        if (combination == null || action == null) return this;
        bindings.put(combination, action);
        return this;
    }

    /**
     * Removes a binding previously registered by descriptor.
     *
     * @param descriptor the descriptor
     * @return this registry
     * @since 1.0.0
     */
    public KeyBindings unbind(String descriptor) {
        if (descriptor == null) return this;
        KeyCombination combo = parseDescriptor(descriptor);
        if (combo != null) bindings.remove(combo);
        return this;
    }

    /**
     * Returns the number of registered bindings.
     *
     * @return the count
     * @since 1.0.0
     */
    public int size() { return bindings.size(); }

    /**
     * Applies every registered binding to the given scene. Call this
     * after {@code Ui.show(...)} returns and the scene is available.
     *
     * @param scene the target scene (null → no-op)
     * @since 1.0.0
     */
    public void applyTo(Scene scene) {
        if (scene == null) return;
        for (Map.Entry<KeyCombination, Runnable> e : bindings.entrySet()) {
            scene.getAccelerators().put(e.getKey(), safe(e.getValue()));
        }
    }

    /**
     * Applies every registered binding to the given node's scene. If the
     * node is not yet attached to a scene, this method is a no-op —
     * call it after the node is shown, or use the scene overload
     * directly.
     *
     * @param node the target node (null → no-op)
     * @since 1.0.0
     */
    public void applyTo(Node node) {
        if (node == null) return;
        Scene scene = node.getScene();
        if (scene == null) return;
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
        }
        if (t.length() == 1) {
            char c = Character.toUpperCase(t.charAt(0));
            return KeyCode.getKeyCode(String.valueOf(c));
        }
        return null;
    }
}
