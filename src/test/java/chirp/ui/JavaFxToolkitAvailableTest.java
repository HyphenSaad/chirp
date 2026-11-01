package chirp.ui;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity check — proves the {@code JavaFxTestExtension} started the toolkit
 * before any UI test ran.
 */
class JavaFxToolkitAvailableTest {

    @Test
    void toolkitIsStarted() {
        // If the extension ran correctly, we are on the FX thread
        assertTrue(Platform.isFxApplicationThread(),
                "UI tests must run on the JavaFX thread — the extension should have wrapped this");
    }

    @Test
    void canCreateControls() {
        assertDoesNotThrow(() -> Label.of("test"));
        assertDoesNotThrow(() -> Button.primary("test"));
        assertDoesNotThrow(() -> TextField.of().placeholder("test"));
    }
}
