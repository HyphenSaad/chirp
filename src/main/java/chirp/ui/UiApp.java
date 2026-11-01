package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.Parent;

/**
 * Immutable configuration for a Chirp window.
 *
 * <h2>Naming</h2>
 * <p>{@code title / windowTitle / caption}; {@code size / dimensions / width /
 * height}; {@code theme / style / dark / light / auto}; {@code resizable /
 * fixed / flexible}; {@code scroll / noScroll / scrolling};
 * {@code onClose / onCloseAction / closeAction}; {@code onShow / onShowAction};
 * {@code content / body / root / view}.</p>
 *
 * <h2>Key-value</h2>
 * <p>{@code UiApp.from(Key.TITLE, "x", Key.WIDTH, 800, Key.HEIGHT, 600,
 * Key.DARK, true)} — enum keys for autocomplete, missing/wrong types skipped.</p>
 */
public final class UiApp {

    /** IDE-autocompletable configuration keys for key-value construction. */
    public enum Key {
        TITLE, WIDTH, HEIGHT, THEME, RESIZABLE, CONTENT, SCROLL, ON_CLOSE, ON_SHOW,
        DARK, LIGHT, AUTO
    }

    private final String title;
    private final int width;
    private final int height;
    private final UiTheme theme;
    private final boolean resizable;
    private final Parent content;
    private final boolean scroll;
    private final Runnable onClose;
    private final Runnable onShow;

    private UiApp(Builder b) {
        this.title = b.title == null ? "Chirp" : b.title;
        this.width = Math.max(320, b.width);
        this.height = Math.max(240, b.height);
        this.theme = b.theme == null ? UiTheme.AUTO : b.theme;
        this.resizable = b.resizable;
        this.content = b.content;
        this.scroll = b.scroll;
        this.onClose = b.onClose;
        this.onShow = b.onShow;
    }

    public String title() { return title; }
    public String windowTitle() { return title; }
    public int width() { return width; }
    public int height() { return height; }
    public UiTheme theme() { return theme; }
    public boolean resizable() { return resizable; }
    public Parent content() { return content; }
    public Parent body() { return content; }
    public Parent root() { return content; }
    public boolean scroll() { return scroll; }
    public Runnable onClose() { return onClose; }
    public Runnable onShow() { return onShow; }

    // ============================================================== builder

    public static Builder builder() { return new Builder(); }
    public static Builder create() { return new Builder(); }
    public static Builder newWindow() { return new Builder(); }
    public static Builder app() { return new Builder(); }

    public static final class Builder {
        private String title = "Chirp";
        private int width = 900;
        private int height = 600;
        private UiTheme theme = UiTheme.AUTO;
        private boolean resizable = true;
        private Parent content;
        private boolean scroll = true;
        private Runnable onClose;
        private Runnable onShow;

        // Title
        public Builder title(String t) { this.title = t; return this; }
        public Builder windowTitle(String t) { return title(t); }
        public Builder caption(String t) { return title(t); }

        // Size
        public Builder size(int w, int h) { this.width = w; this.height = h; return this; }
        public Builder dimensions(int w, int h) { return size(w, h); }
        public Builder width(int w) { this.width = w; return this; }
        public Builder height(int h) { this.height = h; return this; }

        // Theme
        public Builder theme(UiTheme t) { this.theme = t; return this; }
        public Builder style(UiTheme t) { return theme(t); }
        public Builder light() { return theme(UiTheme.LIGHT); }
        public Builder dark() { return theme(UiTheme.DARK); }
        public Builder auto() { return theme(UiTheme.AUTO); }

        // Resizable
        public Builder resizable(boolean r) { this.resizable = r; return this; }
        public Builder fixedSize() { return resizable(false); }
        public Builder fixed() { return resizable(false); }
        public Builder flexible() { return resizable(true); }

        // Lifecycle
        public Builder onClose(Runnable r) { this.onClose = r; return this; }
        public Builder onCloseAction(Runnable r) { return onClose(r); }
        public Builder closeAction(Runnable r) { return onClose(r); }
        public Builder onShow(Runnable r) { this.onShow = r; return this; }
        public Builder onShowAction(Runnable r) { return onShow(r); }
        public Builder showAction(Runnable r) { return onShow(r); }

        // Scroll
        public Builder scroll(boolean s) { this.scroll = s; return this; }
        public Builder scrolling(boolean s) { return scroll(s); }
        public Builder noScroll() { return scroll(false); }

        // Content
        public Builder content(Parent node) {
            this.content = node;
            return this;
        }
        public Builder body(Parent node) { return content(node); }
        public Builder root(Parent node) { return content(node); }

        public Builder content(Consumer<Stack> builder) {
            Stack s = Stack.v(16).padding(24);
            if (builder != null) builder.accept(s);
            this.content = s;
            return this;
        }
        public Builder body(Consumer<Stack> b) { return content(b); }

        // Key-value setter
        public Builder set(Key key, Object value) {
            if (key == null || value == null) return this;
            switch (key) {
                case TITLE -> {
                    if (value instanceof String s) title = s;
                }
                case WIDTH -> {
                    if (value instanceof Integer i) width = i;
                }
                case HEIGHT -> {
                    if (value instanceof Integer i) height = i;
                }
                case THEME -> {
                    if (value instanceof UiTheme t) theme = t;
                }
                case RESIZABLE -> {
                    if (value instanceof Boolean b) resizable = b;
                }
                case SCROLL -> {
                    if (value instanceof Boolean b) scroll = b;
                }
                case ON_CLOSE -> {
                    if (value instanceof Runnable r) onClose = r;
                }
                case ON_SHOW -> {
                    if (value instanceof Runnable r) onShow = r;
                }
                case DARK -> {
                    if (Boolean.TRUE.equals(value)) theme = UiTheme.DARK;
                }
                case LIGHT -> {
                    if (Boolean.TRUE.equals(value)) theme = UiTheme.LIGHT;
                }
                case AUTO -> {
                    if (Boolean.TRUE.equals(value)) theme = UiTheme.AUTO;
                }
                case CONTENT -> {
                    if (value instanceof Parent p) content = p;
                    else if (value instanceof Consumer<?> c) {
                        @SuppressWarnings("unchecked")
                        Consumer<Stack> cb = (Consumer<Stack>) c;
                        content(cb);
                    }
                }
            }
            return this;
        }

        public UiApp build() { return new UiApp(this); }
    }

    /** Builds a UiApp from alternating {@link Key} → value pairs. */
    public static UiApp from(Object... keyValues) {
        Builder b = builder();
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                Object key = keyValues[i];
                Object value = keyValues[i + 1];
                if (key instanceof Key k) b.set(k, value);
            }
        }
        return b.build();
    }
}
