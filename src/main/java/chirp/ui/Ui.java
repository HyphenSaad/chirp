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
 * Entry point for Chirp's JavaFX UI.
 *
 * <h2>Naming</h2>
 * <p>{@code app / launch / start / run / boot}; {@code show / display /
 * present / open}; {@code openWindow / newWindow / secondaryWindow};
 * {@code alert / notify}; {@code info / information}; {@code warning / warn};
 * {@code error / fail}; {@code success / done / ok}; {@code confirm / ask /
 * confirmDialog / askUser}; {@code runAsync / background / async};
 * {@code runOnUiThread / onFxThread / onUi}.</p>
 *
 * <pre>{@code
 * Ui.app(() -> Ui.show("App", root));
 * }</pre>
 *
 * <h2>Lifecycle hooks</h2>
 * <p>{@link #app(Runnable, Runnable)} registers two hooks:</p>
 * <ul>
 *   <li>{@code onStart} runs on the JavaFX application thread once the toolkit
 *       is ready. Typical use: create a scene, call {@code Ui.show(...)}.</li>
 *   <li>{@code onClose} runs when the JVM shuts down (registered as a
 *       shutdown hook). Typical use: flush logs, save state. Runs on a
 *       background thread, not the FX thread.</li>
 * </ul>
 */
public final class Ui {

    private Ui() {}

    // ============================================================== entry point

    /** Starts JavaFX (once) and runs {@code entry} on the FX thread. */
    public static void app(Runnable entry) { launch(entry, null); }
    public static void launch(Runnable entry) { launch(entry, null); }
    public static void start(Runnable entry) { launch(entry, null); }
    public static void run(Runnable entry) { launch(entry, null); }
    public static void boot(Runnable entry) { launch(entry, null); }

    /** Full entry point with optional onStart / onClose hooks. */
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

    public static void style(Object... keyValues) { UiStyle.apply(keyValues); }
    public static void style(UiStyle style) { UiStyle.set(style); }
    public static void theme(Object... keyValues) { UiStyle.apply(keyValues); }
    public static UiStyle currentStyle() { return UiStyle.current(); }
    public static UiStyle style() { return UiStyle.current(); }

    // ============================================================== primary window

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
    public static void display(String title, Parent root) { show(title, root); }
    public static void present(String title, Parent root) { show(title, root); }
    public static void open(String title, Parent root) { show(title, root); }

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

    public static void show(String title, Consumer<Stack> contentBuilder) {
        requireFxThread();
        Stack s = Stack.v(16).padding(24);
        if (contentBuilder != null) contentBuilder.accept(s);
        show(title, s);
    }

    // ============================================================== secondary window

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
    public static Stage newWindow(String title, Parent root) { return openWindow(title, root); }
    public static Stage secondaryWindow(String title, Parent root) { return openWindow(title, root); }

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

    public static void alert(String title, String message) { showAlert(Alert.AlertType.INFORMATION, title, message); }
    public static void notify(String title, String message) { alert(title, message); }

    public static void info(String title, String message) { showAlert(Alert.AlertType.INFORMATION, title, message); }
    public static void information(String t, String m) { info(t, m); }

    public static void warning(String title, String message) { showAlert(Alert.AlertType.WARNING, title, message); }
    public static void warn(String title, String message) { warning(title, message); }

    public static void error(String title, String message) { showAlert(Alert.AlertType.ERROR, title, message); }
    public static void fail(String title, String message) { error(title, message); }

    public static void success(String title, String message) { showAlert(Alert.AlertType.INFORMATION, title, message); }
    public static void ok(String title, String message) { success(title, message); }
    public static void done(String title, String message) { success(title, message); }

    public static boolean confirm(String title, String message) { return ask(title, message); }
    public static boolean ask(String title, String message) { return askUser(title, message); }

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
    public static void background(Runnable task, Runnable onComplete) { runAsync(task, onComplete); }
    public static void async(Runnable task, Runnable onComplete) { runAsync(task, onComplete); }

    public static void runOnUiThread(Runnable task) {
        if (task == null) return;
        if (Platform.isFxApplicationThread()) task.run();
        else Platform.runLater(task);
    }
    public static void runOnFxThread(Runnable task) { runOnUiThread(task); }
    public static void onUi(Runnable task) { runOnUiThread(task); }
    public static void onFxThread(Runnable task) { runOnUiThread(task); }

    public static boolean isAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isUiThread() { return Platform.isFxApplicationThread(); }

    private static void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Ui.show / Ui.openWindow must be called on the JavaFX thread. " +
                            "Wrap your code in Ui.app(() -> { ... });");
        }
    }
}
