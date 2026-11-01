package chirp.ui;

import java.util.function.Consumer;
import javafx.scene.Parent;

/**
 * Immutable window configuration — the full-control alternative to
 * {@link Ui#show(String, Parent)}.
 *
 * <p>When {@code Ui.show("Title", root)} isn't enough — you need a
 * custom size, a specific theme, a scroll wrapper, or a close hook —
 * build a {@code UiApp} instead and pass it to
 * {@link Ui#show(UiApp)}.</p>
 *
 * <h2>What can be configured</h2>
 * <table border="1">
 *   <caption>UiApp configuration</caption>
 *   <tr><th>Setting</th><th>Default</th><th>Builder method</th></tr>
 *   <tr><td>Title</td><td>"Chirp"</td><td>{@code .title(...)}</td></tr>
 *   <tr><td>Size</td><td>900 × 600 (clamped to ≥ 320 × 240)</td>
 *       <td>{@code .size(w, h)}</td></tr>
 *   <tr><td>Theme</td><td>{@link UiTheme#AUTO}</td>
 *       <td>{@code .dark()}, {@code .light()}, {@code .auto()}</td></tr>
 *   <tr><td>Resizable</td><td>true</td>
 *       <td>{@code .resizable(false)} / {@code .fixedSize()}</td></tr>
 *   <tr><td>Scroll</td><td>true</td>
 *       <td>{@code .noScroll()}</td></tr>
 *   <tr><td>Close hook</td><td>none</td>
 *       <td>{@code .onClose(...)}</td></tr>
 *   <tr><td>Show hook</td><td>none</td>
 *       <td>{@code .onShow(...)}</td></tr>
 *   <tr><td>Content</td><td>none (required)</td>
 *       <td>{@code .content(...)}</td></tr>
 * </table>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * UiApp app = UiApp.builder()
 *     .title("Dashboard")
 *     .size(1200, 800)
 *     .dark()
 *     .fixedSize()
 *     .noScroll()
 *     .content(root)
 *     .onClose(() -> System.out.println("Dashboard closed"))
 *     .build();
 *
 * Ui.show(app);
 * }</pre>
 *
 * <h2>Two kinds of close hook — don't confuse them</h2>
 * <ul>
 *   <li>{@link Builder#onClose(Runnable)} on {@code UiApp} runs when
 *       <b>this window's close button</b> is clicked.</li>
 *   <li>{@link Ui#app(Runnable, Runnable)} on {@code Ui} registers a
 *       hook that runs at <b>JVM shutdown</b> — for the whole app.</li>
 * </ul>
 *
 * <h2>Two ways to build</h2>
 * <h3>Builder</h3>
 * <pre>{@code
 * UiApp app = UiApp.builder().title("X").content(root).build();
 * }</pre>
 * <h3>Key-value factory</h3>
 * <pre>{@code
 * UiApp app = UiApp.from(
 *     UiApp.Key.TITLE, "X",
 *     UiApp.Key.WIDTH, 800,
 *     UiApp.Key.HEIGHT, 600,
 *     UiApp.Key.DARK, true,
 *     UiApp.Key.CONTENT, root);
 * }</pre>
 * <p>The key-value form is useful when the configuration comes from a
 * file or a command line; the builder form is more discoverable.</p>
 *
 * <h2>Related</h2>
 * <p>For showing a window with a {@code UiApp}, see
 * {@link Ui#show(UiApp)} and {@link Ui#openWindow(UiApp)}. For the
 * theme enum, see {@link UiTheme}.</p>
 *
 * @see Ui#show(UiApp)
 * @see Ui#openWindow(UiApp)
 * @see UiTheme
 * @since 1.0.0
 */
public final class UiApp {

    /**
     * Keys for the key-value {@link #from(Object...)} factory. Use these
     * when the configuration comes from a source where strings are more
     * natural than method names (JSON, YAML, command-line args).
     *
     * @since 1.0.0
     */
    public enum Key {
        /** The window title — {@link String}. */
        TITLE,
        /** The window width in pixels — {@link Integer}. */
        WIDTH,
        /** The window height in pixels — {@link Integer}. */
        HEIGHT,
        /** The theme — {@link UiTheme}. */
        THEME,
        /** Whether the window is resizable — {@link Boolean}. */
        RESIZABLE,
        /** The root component — {@link javafx.scene.Parent}. */
        CONTENT,
        /** Whether to wrap the content in a scroll pane — {@link Boolean}. */
        SCROLL,
        /** Hook to run on window close — {@link Runnable}. */
        ON_CLOSE,
        /** Hook to run on window show — {@link Runnable}. */
        ON_SHOW,
        /** Whether to use the dark theme — {@link Boolean}. */
        DARK,
        /** Whether to use the light theme — {@link Boolean}. */
        LIGHT,
        /** Whether to auto-detect the theme — {@link Boolean}. */
        AUTO
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

    /**
     * Returns the window title.
     *
     * @return the title (never null)
     * @see Builder#title(String)
     * @since 1.0.0
     */
    public String title() { return title; }

    /**
     * Alias of {@link #title()} — see that method for full
     * documentation.
     *
     * @return the title
     * @see #title()
     * @since 1.0.0
     */
    public String windowTitle() { return title; }

    /**
     * Returns the window width in pixels (clamped to ≥ 320).
     *
     * @return the width
     * @see Builder#size(int, int)
     * @since 1.0.0
     */
    public int width() { return width; }

    /**
     * Returns the window height in pixels (clamped to ≥ 240).
     *
     * @return the height
     * @see Builder#size(int, int)
     * @since 1.0.0
     */
    public int height() { return height; }

    /**
     * Returns the window theme.
     *
     * @return the theme (never null)
     * @see Builder#theme(UiTheme)
     * @since 1.0.0
     */
    public UiTheme theme() { return theme; }

    /**
     * Checks whether the window is resizable by the user.
     *
     * @return {@code true} if resizable
     * @see Builder#resizable(boolean)
     * @since 1.0.0
     */
    public boolean resizable() { return resizable; }

    /**
     * Returns the root content component, or null if not set.
     *
     * @return the content, or null
     * @see Builder#content(Parent)
     * @since 1.0.0
     */
    public Parent content() { return content; }

    /**
     * Alias of {@link #content()} — see that method for full
     * documentation.
     *
     * @return the content
     * @see #content()
     * @since 1.0.0
     */
    public Parent body() { return content; }

    /**
     * Alias of {@link #content()} — see that method for full
     * documentation.
     *
     * @return the content
     * @see #content()
     * @since 1.0.0
     */
    public Parent root() { return content; }

    /**
     * Checks whether the content is wrapped in a scroll pane.
     *
     * @return {@code true} if scrolled
     * @see Builder#noScroll()
     * @since 1.0.0
     */
    public boolean scroll() { return scroll; }

    /**
     * Returns the close hook, or null if none was set.
     *
     * @return the close hook
     * @see Builder#onClose(Runnable)
     * @since 1.0.0
     */
    public Runnable onClose() { return onClose; }

    /**
     * Returns the show hook, or null if none was set.
     *
     * @return the show hook
     * @see Builder#onShow(Runnable)
     * @since 1.0.0
     */
    public Runnable onShow() { return onShow; }

    // ============================================================== builder

    /**
     * Returns a fresh builder — the recommended way to construct a
     * {@code UiApp}.
     *
     * @return a new builder
     * @see #from(Object...)
     * @since 1.0.0
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new builder
     * @see #builder()
     * @since 1.0.0
     */
    public static Builder create() { return new Builder(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new builder
     * @see #builder()
     * @since 1.0.0
     */
    public static Builder newWindow() { return new Builder(); }

    /**
     * Alias of {@link #builder()} — see that method for full
     * documentation.
     *
     * @return a new builder
     * @see #builder()
     * @since 1.0.0
     */
    public static Builder app() { return new Builder(); }

    /**
     * Fluent builder for {@link UiApp}. Every setter returns the
     * builder, so calls chain. Call {@link #build()} to get the
     * immutable configuration.
     *
     * @since 1.0.0
     */
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

        /**
         * Sets the window title.
         *
         * @param t the title (null → "Chirp")
         * @return this builder
         * @see #windowTitle(String)
         * @since 1.0.0
         */
        public Builder title(String t) { this.title = t; return this; }

        /**
         * Alias of {@link #title(String)} — see that method for full
         * documentation.
         *
         * @param t the title
         * @return this builder
         * @see #title(String)
         * @since 1.0.0
         */
        public Builder windowTitle(String t) { return title(t); }

        /**
         * Alias of {@link #title(String)} — see that method for full
         * documentation.
         *
         * @param t the title
         * @return this builder
         * @see #title(String)
         * @since 1.0.0
         */
        public Builder caption(String t) { return title(t); }

        /**
         * Sets the window size in pixels. Values below 320×240 are
         * clamped by the constructor.
         *
         * @param w the width in pixels
         * @param h the height in pixels
         * @return this builder
         * @see #width(int)
         * @see #height(int)
         * @since 1.0.0
         */
        public Builder size(int w, int h) { this.width = w; this.height = h; return this; }

        /**
         * Alias of {@link #size(int, int)} — see that method for full
         * documentation.
         *
         * @param w the width
         * @param h the height
         * @return this builder
         * @see #size(int, int)
         * @since 1.0.0
         */
        public Builder dimensions(int w, int h) { return size(w, h); }

        /**
         * Sets the window width. Values below 320 are clamped by the
         * constructor.
         *
         * @param w the width in pixels
         * @return this builder
         * @see #size(int, int)
         * @since 1.0.0
         */
        public Builder width(int w) { this.width = w; return this; }

        /**
         * Sets the window height. Values below 240 are clamped by the
         * constructor.
         *
         * @param h the height in pixels
         * @return this builder
         * @see #size(int, int)
         * @since 1.0.0
         */
        public Builder height(int h) { this.height = h; return this; }

        /**
         * Sets the theme.
         *
         * @param t the theme (null → {@code AUTO})
         * @return this builder
         * @see #light()
         * @see #dark()
         * @see #auto()
         * @see UiTheme
         * @since 1.0.0
         */
        public Builder theme(UiTheme t) { this.theme = t; return this; }

        /**
         * Alias of {@link #theme(UiTheme)} — see that method for full
         * documentation.
         *
         * @param t the theme
         * @return this builder
         * @see #theme(UiTheme)
         * @since 1.0.0
         */
        public Builder style(UiTheme t) { return theme(t); }

        /**
         * Alias of {@link #theme(UiTheme)} with {@code LIGHT} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #theme(UiTheme)
         * @since 1.0.0
         */
        public Builder light() { return theme(UiTheme.LIGHT); }

        /**
         * Alias of {@link #theme(UiTheme)} with {@code DARK} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #theme(UiTheme)
         * @since 1.0.0
         */
        public Builder dark() { return theme(UiTheme.DARK); }

        /**
         * Alias of {@link #theme(UiTheme)} with {@code AUTO} — see that
         * method for full documentation.
         *
         * @return this builder
         * @see #theme(UiTheme)
         * @since 1.0.0
         */
        public Builder auto() { return theme(UiTheme.AUTO); }

        /**
         * Enables or disables user resizing. Default: {@code true}.
         *
         * @param r whether the window is resizable
         * @return this builder
         * @see #fixedSize()
         * @see #flexible()
         * @since 1.0.0
         */
        public Builder resizable(boolean r) { this.resizable = r; return this; }

        /**
         * Alias of {@link #resizable(boolean)} with {@code false} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #resizable(boolean)
         * @since 1.0.0
         */
        public Builder fixedSize() { return resizable(false); }

        /**
         * Alias of {@link #resizable(boolean)} with {@code false} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #resizable(boolean)
         * @since 1.0.0
         */
        public Builder fixed() { return resizable(false); }

        /**
         * Alias of {@link #resizable(boolean)} with {@code true} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #resizable(boolean)
         * @since 1.0.0
         */
        public Builder flexible() { return resizable(true); }

        /**
         * Sets a hook that runs when the user closes the window.
         *
         * <p><b>Not</b> the same as {@link Ui#app(Runnable, Runnable)}'s
         * close hook — that one runs at JVM shutdown. This one runs at
         * window close.</p>
         *
         * @param r the close hook (null → no-op)
         * @return this builder
         * @see #onCloseAction(Runnable)
         * @see #closeAction(Runnable)
         * @since 1.0.0
         */
        public Builder onClose(Runnable r) { this.onClose = r; return this; }

        /**
         * Alias of {@link #onClose(Runnable)} — see that method for full
         * documentation.
         *
         * @param r the close hook
         * @return this builder
         * @see #onClose(Runnable)
         * @since 1.0.0
         */
        public Builder onCloseAction(Runnable r) { return onClose(r); }

        /**
         * Alias of {@link #onClose(Runnable)} — see that method for full
         * documentation.
         *
         * @param r the close hook
         * @return this builder
         * @see #onClose(Runnable)
         * @since 1.0.0
         */
        public Builder closeAction(Runnable r) { return onClose(r); }

        /**
         * Sets a hook that runs when the window is shown (opened for the
         * first time, or re-shown after being hidden).
         *
         * @param r the show hook (null → no-op)
         * @return this builder
         * @see #onShowAction(Runnable)
         * @see #showAction(Runnable)
         * @since 1.0.0
         */
        public Builder onShow(Runnable r) { this.onShow = r; return this; }

        /**
         * Alias of {@link #onShow(Runnable)} — see that method for full
         * documentation.
         *
         * @param r the show hook
         * @return this builder
         * @see #onShow(Runnable)
         * @since 1.0.0
         */
        public Builder onShowAction(Runnable r) { return onShow(r); }

        /**
         * Alias of {@link #onShow(Runnable)} — see that method for full
         * documentation.
         *
         * @param r the show hook
         * @return this builder
         * @see #onShow(Runnable)
         * @since 1.0.0
         */
        public Builder showAction(Runnable r) { return onShow(r); }

        /**
         * Enables or disables wrapping the content in a scroll pane.
         * Default: {@code true}.
         *
         * @param s whether to scroll
         * @return this builder
         * @see #noScroll()
         * @since 1.0.0
         */
        public Builder scroll(boolean s) { this.scroll = s; return this; }

        /**
         * Alias of {@link #scroll(boolean)} — see that method for full
         * documentation.
         *
         * @param s whether to scroll
         * @return this builder
         * @see #scroll(boolean)
         * @since 1.0.0
         */
        public Builder scrolling(boolean s) { return scroll(s); }

        /**
         * Alias of {@link #scroll(boolean)} with {@code false} — see
         * that method for full documentation.
         *
         * @return this builder
         * @see #scroll(boolean)
         * @since 1.0.0
         */
        public Builder noScroll() { return scroll(false); }

        /**
         * Sets the root content component. Required — a {@code UiApp}
         * without content cannot be shown.
         *
         * @param node the root component (must not be null when shown)
         * @return this builder
         * @see #body(Parent)
         * @see #root(Parent)
         * @since 1.0.0
         */
        public Builder content(Parent node) {
            this.content = node;
            return this;
        }

        /**
         * Alias of {@link #content(Parent)} — see that method for full
         * documentation.
         *
         * @param node the root component
         * @return this builder
         * @see #content(Parent)
         * @since 1.0.0
         */
        public Builder body(Parent node) { return content(node); }

        /**
         * Alias of {@link #content(Parent)} — see that method for full
         * documentation.
         *
         * @param node the root component
         * @return this builder
         * @see #content(Parent)
         * @since 1.0.0
         */
        public Builder root(Parent node) { return content(node); }

        /**
         * Builds the content from a lambda that receives an empty
         * {@link Stack} — convenience for the common case where the
         * content is a vertical stack.
         *
         * @param builder the content builder (null → empty Stack)
         * @return this builder
         * @see #content(Parent)
         * @since 1.0.0
         */
        public Builder content(Consumer<Stack> builder) {
            Stack s = Stack.v(16).padding(24);
            if (builder != null) builder.accept(s);
            this.content = s;
            return this;
        }

        /**
         * Alias of {@link #content(Consumer)} — see that method for full
         * documentation.
         *
         * @param b the content builder
         * @return this builder
         * @see #content(Consumer)
         * @since 1.0.0
         */
        public Builder body(Consumer<Stack> b) { return content(b); }

        /**
         * Configures a single aspect via the {@link Key} enum.
         *
         * @param key the key (null → no-op)
         * @param value the value (null → no-op)
         * @return this builder
         * @since 1.0.0
         */
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

        /**
         * Builds the immutable {@code UiApp}.
         *
         * @return the built configuration
         * @since 1.0.0
         */
        public UiApp build() { return new UiApp(this); }
    }

    /**
     * Constructs a {@code UiApp} from alternating {@link Key} → value
     * pairs. Unknown keys and wrong value types are silently skipped.
     *
     * <pre>{@code
     * UiApp app = UiApp.from(
     *     UiApp.Key.TITLE, "Dashboard",
     *     UiApp.Key.WIDTH, 800,
     *     UiApp.Key.HEIGHT, 600,
     *     UiApp.Key.DARK, true,
     *     UiApp.Key.CONTENT, root);
     * }</pre>
     *
     * @param keyValues alternating Key → value pairs (null → default config)
     * @return the built configuration
     * @see #builder()
     * @since 1.0.0
     */
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
