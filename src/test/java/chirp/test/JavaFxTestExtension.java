package chirp.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * JUnit 5 extension that starts the JavaFX toolkit once per JVM and runs
 * every test method in {@code chirp.ui.*} on the FX application thread.
 *
 * <p>
 * <b>Why this exists:</b> JavaFX controls cannot be constructed until
 * {@code Platform.startup(...)} has been called. Trying to build a
 * {@code javafx.scene.control.Label} without the toolkit throws
 * {@code ExceptionInInitializerError: Toolkit not initialized}. This
 * extension starts the toolkit before any test class runs, so control
 * construction — even from a plain JUnit test — succeeds.
 * </p>
 *
 * <p>
 * <b>How it's registered:</b> Via {@code META-INF/services}, so it applies
 * to every test class automatically — no {@code @ExtendWith} annotations are
 * needed on individual test classes.
 * </p>
 *
 * <p>
 * <b>Threading:</b> Tests in the {@code chirp.ui.*} packages are wrapped in
 * {@code Platform.runLater} and executed synchronously on the FX thread.
 * All other tests run on their original thread. This keeps the UI tests
 * correct without slowing down the 500+ non-UI tests.
 * </p>
 *
 * <p>
 * <b>Cleanup:</b> A JVM shutdown hook calls {@code Platform.exit()} when
 * the test fork finishes — prevents the toolkit threads from hanging the
 * surefire JVM at exit.
 * </p>
 */
public class JavaFxTestExtension implements BeforeAllCallback, AfterAllCallback, InvocationInterceptor {

    private static final Object LOCK = new Object();
    private static volatile boolean toolkitStarted = false;

    // ------------------------------------------------------------ start toolkit

    @Override
    public void beforeAll(ExtensionContext context) {
        startToolkitOnce();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Do nothing — toolkit is reused across test classes.
        // Shutdown is handled by the JVM shutdown hook.
    }

    private static void startToolkitOnce() {
        if (toolkitStarted)
            return;
        synchronized (LOCK) {
            if (toolkitStarted)
                return;
            try {
                CountDownLatch latch = new CountDownLatch(1);
                try {
                    Platform.startup(() -> {
                        // Keep the toolkit alive across test classes — some
                        // tests create Stages that may be transiently closed.
                        Platform.setImplicitExit(false);
                        latch.countDown();
                    });
                } catch (IllegalStateException alreadyRunning) {
                    latch.countDown();
                }
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "JavaFX toolkit did not start within 10 seconds");
                }
                toolkitStarted = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while starting JavaFX", e);
            }
        }
    }

    // ------------------------------------------------------------ run test on FX
    // thread

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        if (isUiTest(extensionContext)) {
            runOnFxThread(invocation);
        } else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        if (isUiTest(extensionContext)) {
            runOnFxThread(invocation);
        } else {
            invocation.proceed();
        }
    }

    private static boolean isUiTest(ExtensionContext ctx) {
        String name = ctx.getRequiredTestClass().getName();
        return name.startsWith("chirp.ui.");
    }

    private static void runOnFxThread(Invocation<Void> invocation) throws Throwable {
        if (Platform.isFxApplicationThread()) {
            invocation.proceed();
            return;
        }
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return null;
        });
        Platform.runLater(task);
        try {
            task.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re && re.getCause() != null) {
                throw re.getCause();
            }
            throw cause != null ? cause : e;
        }
    }
}
