package chirp.test;

import chirp.startup.ChirpLifecycle;
import chirp.startup.StartupConfig;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 auto-extension: ensures Chirp is started before any test runs.
 *
 * <p>Registered via {@code META-INF/services}, so every test class in the
 * project automatically gets a silent Chirp lifecycle — no test edits needed.
 * Uses {@link StartupConfig#silent()} so no banner is printed during tests.</p>
 */
public class ChirpAutoStart implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!ChirpLifecycle.isStarted()) {
            ChirpLifecycle.start(StartupConfig.silent());
        }
    }
}
