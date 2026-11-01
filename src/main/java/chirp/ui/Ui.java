package chirp.ui;

import chirp.internal.Trace;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Entry point for Chirp's JavaFX UI — window creation, dialogs, and the
 * FX-thread helpers every non-trivial application needs.
 *
 * <p>Chirp's UI module is a fluent wrapper around JavaFX. Everything a
 * student needs to build a window, show an alert, or run a background
 * task without blocking the UI is here — the rest of the {@code ui}
 * package is individual control wrappers ({@link Button},
 * {@link Label}, {@link TextField}, etc.).</p>
 *
 * <h2>The pattern: {@code app()} + {@code show()}</h2>
 * <p>Every JavaFX application must start on the FX application thread.
 * The simplest correct invocation is a single lambda passed to
 * {@link #app(Runnable)}:</p>
 * <pre>{@code
 * public static void main(String[] args) {
 *     Ui.app(() -> {
 *         Stack root = Stack.v(16).padding(24)
 *             .add(Label.heading("Hello"))
 *             .add(Label.of("Welcome to Chirp"));
 *         Ui.show("My App", root);
 *     });
 * }
 * }</pre>
 * <p>Everything inside the lambda runs on the FX thread. Everything
 * outside (the static code before/after {@code Ui.app(...)}) runs on
 * the main thread. This is the whole threading model, and it never
 * needs to be thought about again for simple applications.</p>
 *
 * <h2>What this class covers</h2>
 * <table border="1">
 *   <caption>The five responsibilities of {@code Ui}</caption>
 *   <tr><th>Category</th><th>Methods</th></tr>
 *   <tr><td>Entry point</td>
 *       <td>{@link #app(Runnable)}, {@link #app(Runnable, Runnable)},
 *           {@link #launch(Runnable)}, {@link #start(Runnable)},
 *           {@link #run(Runnable)}, {@link #boot(Runnable)}</td></tr>
 *   <tr><td>Primary window</td>
 *       <td>{@link #show(String, Parent)}, {@link #show(UiApp)},
 *           {@link #show(String, Consumer)}</td></tr>
 *   <tr><td>Secondary window</td>
 *       <td>{@link #openWindow(String, Parent)}, {@link #openWindow(UiApp)}</td></tr>
 *   <tr><td>Dialogs</td>
 *       <td>{@link #alert}, {@link #info}, {@link #warning},
 *           {@link #error}, {@link #success}, {@link #confirm}</td></tr>
 *   <tr><td>Thread helpers</td>
 *       <td>{@link #runAsync(Runnable, Runnable)},
 *           {@link #runOnUiThread(Runnable)}</td></tr>
 * </table>
 *
 * <h2>Two kinds of lifecycle hooks — don't confuse them</h2>
 * <p>{@link #app(Runnable, Runnable)} registers two hooks:</p>
 * <ul>
 *   <li>{@code onStart} — runs once on the FX thread when the toolkit
 *       starts. This is where you build the UI.</li>
 *   <li>{@code onClose} — runs at <b>JVM shutdown</b> (registered as a
 *       shutdown hook). Typical use: flush logs, save state.</li>
 * </ul>
 * <p>{@link UiApp#builder()} also has an {@code onClose} hook — that one
 * runs when the <b>window's close button</b> is clicked. Two hooks,
 * two purposes.</p>
 *
 * <h2>Dialogs work from any thread</h2>
 * <p>{@link #info} and friends marshal the call onto the FX thread if
 * needed. A background task can safely call {@code Ui.error("...")}
 * from its worker thread and the dialog will appear correctly.</p>
 *
 * <h2>Example — a complete application</h2>
 * <pre>{@code
 * public static void main(String[] args) {
 *     Ui.app(
 *         () -> {
 *             Field name = Field.of("Name").required().fullWidth();
 *             Button greet = Button.primary("Greet").onClick(() ->
 *                 Ui.info("Hello", "Welcome, " + name.value() + "!"));
 *
 *             Stack root = Stack.v(16).padding(24)
 *                 .add(Label.heading("Greeting App"))
 *                 .add(name)
 *                 .add(greet);
 *
 *             Ui.show("Greeting", root);
 *         },
 *         () -> System.out.println("Application closed"));
 * }
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For the immutable window configuration, see {@link UiApp}. For the
 * global styling that applies to every window, see {@link UiStyle}.
 * For theme selection, see {@link UiTheme}. For the field layout
 * helpers, see {@link Stack} and {@link Row}.</p>
 *
 * @see UiApp
 * @see UiStyle
 * @see UiTheme
 * @see Field
 * @see Stack
 * @see Row
 * @since 1.0.0
 */
public final class Ui {

    private Ui() {}

    // ============================================================== entry point

    /**
     * Starts JavaFX (once per JVM) and runs {@code entry} on the FX
     * application thread.
     *
     * <p>This is the single entry point for the vast majority of Chirp
     * UI applications. Inside the lambda, every UI method works
     * normally; outside, only the async helpers can be safely called.</p>
     *
     * <pre>{@code
     * Ui.app(() -> Ui.show("App", root));
     * }</pre>
     *
     * @param entry the application entry code (null → no-op launch)
     * @see #app(Runnable, Runnable)
     * @see #launch(Runnable)
     * @see #start(Runnable)
     * @see #run(Runnable)
     * @see #boot(Runnable)
     * @since 1.0.0
     */
    public static void app(Runnable entry) { launch(entry, null); }

    /**
     * Alias of {@link #app(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param entry the application entry code
     * @see #app(Runnable)
     * @since 1.0.0
     */
    public static void launch(Runnable entry) { launch(entry, null); }

    /**
     * Alias of {@link #app(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param entry the application entry code
     * @see #app(Runnable)
     * @since 1.0.0
     */
    public static void start(Runnable entry) { launch(entry, null); }

    /**
     * Alias of {@link #app(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param entry the application entry code
     * @see #app(Runnable)
     * @since 1.0.0
     */
    public static void run(Runnable entry) { launch(entry, null); }

    /**
     * Alias of {@link #app(Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param entry the application entry code
     * @see #app(Runnable)
     * @since 1.0.0
     */
    public static void boot(Runnable entry) { launch(entry, null); }

    /**
     * Starts JavaFX and runs two lifecycle hooks — one when the toolkit
     * is ready, one at JVM shutdown.
     *
     * <p>Use this when the application needs to clean up on exit —
     * flush a log, close a database, save state. The close hook runs on
     * a background thread (registered as a JVM shutdown hook), not on
     * the FX thread.</p>
     *
     * @param onStart the code to run once the FX toolkit is ready
     * @param onClose the code to run at JVM shutdown (may be null)
     * @see #app(Runnable)
     * @since 1.0.0
     */
    public static void app(Runnable onStart, Runnable onClose) {
        launch(onStart, onClose);
    }

    private static void launch(Runnable onStart, Runnable onClose) {
        Runnable wrapped = () -> {
            if (onStart == null) return;
            try {
                onStart.run();
            } catch (RuntimeException t) {
                // The user's own startup code threw. Print the stack trace —
                // it is their application to debug, not the library.
                t.printStackTrace();
            }
        };
        try {
            Platform.startup(wrapped);
        } catch (IllegalStateException alreadyRunning) {
            Platform.runLater(wrapped);
        }
        if (onClose != null) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    onClose.run();
                } catch (RuntimeException t) {
                    t.printStackTrace();
                }
            }, "chirp-ui-onclose"));
        }
    }

    // ============================================================== style

    /**
     * Applies global styling via key-value pairs. Equivalent to
     * {@link UiStyle#apply(Object...)} — updates every currently-open
     * Chirp window in place.
     *
     * <pre>{@code
     * Ui.style(
     *     UiStyle.Key.FONT_SIZE, 16,
     *     UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");
     * }</pre>
     *
     * @param keyValues alternating {@link UiStyle.Key} → value pairs
     * @see UiStyle#apply(Object...)
     * @see UiStyle
     * @since 1.0.0
     */
    public static void style(Object... keyValues) { UiStyle.apply(keyValues); }

    /**
     * Applies an entire pre-built {@link UiStyle} — see
     * {@link UiStyle#set(UiStyle)}.
     *
     * @param style the style to apply (null → no-op)
     * @see UiStyle#set(UiStyle)
     * @since 1.0.0
     */
    public static void style(UiStyle style) { UiStyle.set(style); }

    /**
     * Alias of {@link #style(Object...)} — see that method for full
     * documentation and examples.
     *
     * @param keyValues alternating {@link UiStyle.Key} → value pairs
     * @see #style(Object...)
     * @since 1.0.0
     */
    public static void theme(Object... keyValues) { UiStyle.apply(keyValues); }

    /**
     * Returns the current global style — matches
     * {@link UiStyle#current()}.
     *
     * @return the current style
     * @see UiStyle#current()
     * @since 1.0.0
     */
    public static UiStyle currentStyle() { return UiStyle.current(); }

    /**
     * Returns the current global style — matches
     * {@link UiStyle#current()}.
     *
     * @return the current style
     * @see UiStyle#current()
     * @since 1.0.0
     */
    public static UiStyle style() { return UiStyle.current(); }

    // ============================================================== primary window

    /**
     * Creates and shows a window with the given title and root component.
     * Must be called from the FX application thread — inside a
     * {@link #app(Runnable)} lambda or a {@link #runOnUiThread(Runnable)}.
     *
     * <p>Uses the default window size (900×600), applies the current
     * {@link UiStyle} and the {@link UiTheme#AUTO} theme. For full
     * control over size, theme, scroll, or close hooks, use
     * {@link #show(UiApp)} instead.</p>
     *
     * @param title the window title (null → "Chirp")
     * @param root the root component (must not be null)
     * @throws IllegalArgumentException if {@code root} is null
     * @throws IllegalStateException if not called from the FX thread
     * @see #show(UiApp)
     * @see #display(String, Parent)
     * @see #present(String, Parent)
     * @see #open(String, Parent)
     * @since 1.0.0
     */
    public static void show(String title, Parent root) {
        requireFxThread();
        if (root == null) throw new IllegalArgumentException("root component is required");
        Scene scene = new Scene(root, 900, 600);
        scene.getProperties().put("chirp.ui", true);
        scene.getStylesheets().add(UiTheme.stylesheet(UiTheme.AUTO));
        UiStyle.current().applyToScene(scene);

        Stage stage = new Stage();
        stage.setTitle(title == null ? "Chirp" : title);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Alias of {@link #show(String, Parent)} — see that method for full
     * documentation and examples.
     *
     * @param title the window title
     * @param root the root component
     * @see #show(String, Parent)
     * @since 1.0.0
     */
    public static void display(String title, Parent root) { show(title, root); }

    /**
     * Alias of {@link #show(String, Parent)} — see that method for full
     * documentation and examples.
     *
     * @param title the window title
     * @param root the root component
     * @see #show(String, Parent)
     * @since 1.0.0
     */
    public static void present(String title, Parent root) { show(title, root); }

    /**
     * Alias of {@link #show(String, Parent)} — see that method for full
     * documentation and examples.
     *
     * @param title the window title
     * @param root the root component
     * @see #show(String, Parent)
     * @since 1.0.0
     */
    public static void open(String title, Parent root) { show(title, root); }

    /**
     * Creates and shows a window from an immutable {@link UiApp}
     * configuration. This is the full-control variant — size, theme,
     * scroll behaviour, close hook, and content are all decided by the
     * {@code UiApp} object.
     *
     * <pre>{@code
     * UiApp app = UiApp.builder()
     *     .title("Dashboard")
     *     .size(1200, 800)
     *     .dark()
     *     .content(root)
     *     .build();
     *
     * Ui.show(app);
     * }</pre>
     *
     * @param app the window configuration (must not be null)
     * @throws IllegalArgumentException if {@code app} is null
     * @throws IllegalStateException if not called from the FX thread
     * @see UiApp
     * @see UiApp.Builder
     * @since 1.0.0
     */
    public static void show(UiApp app) {
        requireFxThread();
        if (app == null) throw new IllegalArgumentException("UiApp is required");
        Parent root = app.content();
        if (root == null) {
            throw new IllegalStateException(
                    "UiApp.content is required — call .content(...) on the builder.");
        }
        Stage stage = buildStage(app);
        stage.show();
    }

    /**
     * Creates and shows a window whose content is built by a lambda that
     * receives an empty {@link Stack}. Convenience for the common case
     * where the content is a vertical stack of components.
     *
     * <pre>{@code
     * Ui.show("Title", stack -> stack
     *     .add(Label.heading("Hello"))
     *     .add(Button.primary("OK").onClick(() -> {})));
     * }</pre>
     *
     * @param title the window title
     * @param contentBuilder the content builder (null → empty window)
     * @throws IllegalStateException if not called from the FX thread
     * @since 1.0.0
     */
    public static void show(String title, Consumer<Stack> contentBuilder) {
        requireFxThread();
        Stack s = Stack.v(16).padding(24);
        if (contentBuilder != null) contentBuilder.accept(s);
        show(title, s);
    }

    // ============================================================== secondary window

    /**
     * Opens a secondary window — a new stage with the given title and
     * root, without closing the primary window. Returns the {@link Stage}
     * so the caller can close it later or store it for reference.
     *
     * @param title the window title (null → "Window")
     * @param root the root component (must not be null)
     * @return the opened stage
     * @throws IllegalArgumentException if {@code root} is null
     * @throws IllegalStateException if not called from the FX thread
     * @see #openWindow(UiApp)
     * @since 1.0.0
     */
    public static Stage openWindow(String title, Parent root) {
        requireFxThread();
        if (root == null) throw new IllegalArgumentException("root component is required");
        UiApp app = UiApp.builder()
                .title(title == null ? "Window" : title)
                .content(root)
                .build();
        Stage stage = buildStage(app);
        stage.show();
        return stage;
    }

    /**
     * Alias of {@link #openWindow(String, Parent)} — see that method for
     * full documentation.
     *
     * @param title the window title
     * @param root the root component
     * @return the opened stage
     * @see #openWindow(String, Parent)
     * @since 1.0.0
     */
    public static Stage newWindow(String title, Parent root) { return openWindow(title, root); }

    /**
     * Alias of {@link #openWindow(String, Parent)} — see that method for
     * full documentation.
     *
     * @param title the window title
     * @param root the root component
     * @return the opened stage
     * @see #openWindow(String, Parent)
     * @since 1.0.0
     */
    public static Stage secondaryWindow(String title, Parent root) { return openWindow(title, root); }

    /**
     * Opens a secondary window from an immutable {@link UiApp} config —
     * the full-control variant of {@link #openWindow(String, Parent)}.
     *
     * @param app the window configuration (must not be null)
     * @return the opened stage
     * @throws IllegalArgumentException if {@code app} is null
     * @throws IllegalStateException if not called from the FX thread
     * @see #openWindow(String, Parent)
     * @since 1.0.0
     */
    public static Stage openWindow(UiApp app) {
        requireFxThread();
        if (app == null) throw new IllegalArgumentException("UiApp is required");
        Stage stage = buildStage(app);
        stage.show();
        return stage;
    }

    private static Stage buildStage(UiApp app) {
        Parent root = app.content();
        if (app.scroll()) {
            javafx.scene.control.ScrollPane sc = new javafx.scene.control.ScrollPane(root);
            sc.setFitToWidth(true);
            sc.setFitToHeight(true);
            sc.setStyle("-fx-background-color: transparent;");
            root = sc;
        }

        Scene scene = new Scene(root, app.width(), app.height());
        scene.getProperties().put("chirp.ui", true);
        scene.getStylesheets().add(UiTheme.stylesheet(app.theme()));
        UiStyle.current().applyToScene(scene);

        Stage stage = new Stage();
        stage.setTitle(app.title());
        stage.setResizable(app.resizable());
        stage.setScene(scene);

        if (app.onClose() != null) {
            stage.setOnCloseRequest(e -> {
                try {
                    app.onClose().run();
                } catch (RuntimeException ex) {
                    Trace.warn("ui", "onClose hook threw: " + ex.getMessage());
                }
            });
        }
        if (app.onShow() != null) {
            stage.setOnShowing(e -> {
                try {
                    app.onShow().run();
                } catch (RuntimeException ex) {
                    Trace.warn("ui", "onShow hook threw: " + ex.getMessage());
                }
            });
        }
        return stage;
    }

    // ============================================================== dialogs — aliases

    /**
     * Shows a modal information dialog. Works from any thread — the call
     * is marshalled to the FX thread if needed.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #info(String, String)
     * @see #warning(String, String)
     * @see #error(String, String)
     * @see #confirm(String, String)
     * @since 1.0.0
     */
    public static void alert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Alias of {@link #alert(String, String)} — see that method for full
     * documentation.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #alert(String, String)
     * @since 1.0.0
     */
    public static void notify(String title, String message) { alert(title, message); }

    /**
     * Shows a modal information dialog — the canonical informational
     * dialog.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #alert(String, String)
     * @since 1.0.0
     */
    public static void info(String title, String message) { showAlert(Alert.AlertType.INFORMATION, title, message); }

    /**
     * Alias of {@link #info(String, String)} — see that method for full
     * documentation.
     *
     * @param t the dialog title
     * @param m the dialog message
     * @see #info(String, String)
     * @since 1.0.0
     */
    public static void information(String t, String m) { info(t, m); }

    /**
     * Shows a modal warning dialog.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #alert(String, String)
     * @since 1.0.0
     */
    public static void warning(String title, String message) { showAlert(Alert.AlertType.WARNING, title, message); }

    /**
     * Alias of {@link #warning(String, String)} — see that method for
     * full documentation.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #warning(String, String)
     * @since 1.0.0
     */
    public static void warn(String title, String message) { warning(title, message); }

    /**
     * Shows a modal error dialog.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #alert(String, String)
     * @since 1.0.0
     */
    public static void error(String title, String message) { showAlert(Alert.AlertType.ERROR, title, message); }

    /**
     * Alias of {@link #error(String, String)} — see that method for full
     * documentation.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #error(String, String)
     * @since 1.0.0
     */
    public static void fail(String title, String message) { error(title, message); }

    /**
     * Shows a modal success dialog (an information dialog with the same
     * content — the semantic name is the value).
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #alert(String, String)
     * @since 1.0.0
     */
    public static void success(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Alias of {@link #success(String, String)} — see that method for
     * full documentation.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #success(String, String)
     * @since 1.0.0
     */
    public static void ok(String title, String message) { success(title, message); }

    /**
     * Alias of {@link #success(String, String)} — see that method for
     * full documentation.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @see #success(String, String)
     * @since 1.0.0
     */
    public static void done(String title, String message) { success(title, message); }

    /**
     * Shows a modal confirmation dialog and returns {@code true} if the
     * user clicked OK. Works from any thread — the call blocks until
     * the dialog is dismissed.
     *
     * <pre>{@code
     * if (Ui.confirm("Delete?", "This cannot be undone.")) {
     *     deleteItem();
     * }
     * }</pre>
     *
     * @param title the dialog title
     * @param message the dialog message
     * @return {@code true} if OK was clicked, {@code false} otherwise
     * @see #ask(String, String)
     * @see #askUser(String, String)
     * @see #confirmDialog(String, String)
     * @since 1.0.0
     */
    public static boolean confirm(String title, String message) { return ask(title, message); }

    /**
     * Alias of {@link #confirm(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @return {@code true} if OK was clicked
     * @see #confirm(String, String)
     * @since 1.0.0
     */
    public static boolean ask(String title, String message) { return askUser(title, message); }

    /**
     * Alias of {@link #confirm(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @return {@code true} if OK was clicked
     * @see #confirm(String, String)
     * @since 1.0.0
     */
    public static boolean askUser(String title, String message) {
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        Runnable run = () -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle(title == null ? "" : title);
            a.setHeaderText(null);
            a.setContentText(message == null ? "" : message);
            Optional<ButtonType> opt = a.showAndWait();
            result.set(opt.isPresent() && opt.get() == ButtonType.OK);
        };
        if (Platform.isFxApplicationThread()) run.run();
        else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> { run.run(); latch.countDown(); });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return result.get();
    }

    /**
     * Alias of {@link #confirm(String, String)} — see that method for
     * full documentation and examples.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @return {@code true} if OK was clicked
     * @see #confirm(String, String)
     * @since 1.0.0
     */
    public static boolean confirmDialog(String title, String message) { return confirm(title, message); }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Runnable r = () -> {
            Alert a = new Alert(type);
            a.setTitle(title == null ? "" : title);
            a.setHeaderText(null);
            a.setContentText(message == null ? "" : message);
            a.showAndWait();
        };
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    // ============================================================== async helpers

    /**
     * Runs a task on a background thread, then runs {@code onComplete}
     * on the FX thread. The background task must not touch UI state
     * directly — use {@code onComplete} for that.
     *
     * <pre>{@code
     * Ui.runAsync(
     *     () -> { Thread.sleep(2000); data = fetchSlowly(); },
     *     () -> output.text(data));
     * }</pre>
     *
     * @param task the background task (null → no-op)
     * @param onComplete the FX-thread callback (null → nothing runs after)
     * @see #runOnUiThread(Runnable)
     * @since 1.0.0
     */
    public static void runAsync(Runnable task, Runnable onComplete) {
        Thread t = new Thread(() -> {
            try {
                if (task != null) task.run();
            } finally {
                if (onComplete != null) Platform.runLater(onComplete);
            }
        }, "chirp-ui-async");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Alias of {@link #runAsync(Runnable, Runnable)} — see that method
     * for full documentation and examples.
     *
     * @param task the background task
     * @param onComplete the FX-thread callback
     * @see #runAsync(Runnable, Runnable)
     * @since 1.0.0
     */
    public static void background(Runnable task, Runnable onComplete) { runAsync(task, onComplete); }

    /**
     * Alias of {@link #runAsync(Runnable, Runnable)} — see that method
     * for full documentation and examples.
     *
     * @param task the background task
     * @param onComplete the FX-thread callback
     * @see #runAsync(Runnable, Runnable)
     * @since 1.0.0
     */
    public static void async(Runnable task, Runnable onComplete) { runAsync(task, onComplete); }

    /**
     * Runs a task on the FX application thread. If already on the FX
     * thread, runs immediately; otherwise marshals the call via
     * {@link javafx.application.Platform#runLater(Runnable)}.
     *
     * <p>The standard way to safely touch UI state from a background
     * callback — e.g. from an HTTP listener's {@code onComplete}:</p>
     * <pre>{@code
     * Http.get(url).sendAsync().thenAccept(r ->
     *     Ui.runOnUiThread(() -> output.text(r.text())));
     * }</pre>
     *
     * @param task the task to run on the FX thread (null → no-op)
     * @see #runAsync(Runnable, Runnable)
     * @since 1.0.0
     */
    public static void runOnUiThread(Runnable task) {
        if (task == null) return;
        if (Platform.isFxApplicationThread()) task.run();
        else Platform.runLater(task);
    }

    /**
     * Alias of {@link #runOnUiThread(Runnable)} — see that method for
     * full documentation and examples.
     *
     * @param task the task to run on the FX thread
     * @see #runOnUiThread(Runnable)
     * @since 1.0.0
     */
    public static void runOnFxThread(Runnable task) { runOnUiThread(task); }

    /**
     * Alias of {@link #runOnUiThread(Runnable)} — see that method for
     * full documentation and examples.
     *
     * @param task the task to run on the FX thread
     * @see #runOnUiThread(Runnable)
     * @since 1.0.0
     */
    public static void onUi(Runnable task) { runOnUiThread(task); }

    /**
     * Alias of {@link #runOnUiThread(Runnable)} — see that method for
     * full documentation and examples.
     *
     * @param task the task to run on the FX thread
     * @see #runOnUiThread(Runnable)
     * @since 1.0.0
     */
    public static void onFxThread(Runnable task) { runOnUiThread(task); }

    /**
     * Checks whether the JavaFX runtime is available on the classpath.
     *
     * <p>Returns {@code false} if the {@code javafx.application.Platform}
     * class cannot be loaded — for example in a headless server
     * environment or a Maven build without the JavaFX dependency.</p>
     *
     * @return {@code true} if JavaFX is available
     * @since 1.0.0
     */
    public static boolean isAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether the current thread is the FX application thread.
     *
     * @return {@code true} if on the FX thread
     * @since 1.0.0
     */
    public static boolean isUiThread() { return Platform.isFxApplicationThread(); }

    private static void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Ui.show / Ui.openWindow must be called on the JavaFX thread. " +
                            "Wrap your code in Ui.app(() -> { ... });");
        }
    }
}
