package chirp.startup;

import chirp.ChirpNotStartedException;
import chirp.console.Console;
import chirp.console.Input;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    @BeforeEach
    void setup() {
        ChirpLifecycle.shutdown();
    }

    @AfterEach
    void teardown() {
        ChirpLifecycle.shutdown();
        Input.resetInputStream();
    }

    @Test
    void notStartedByDefault() {
        assertFalse(ChirpLifecycle.isStarted());
    }

    @Test
    void startMarksStarted() {
        ChirpLifecycle.start(StartupConfig.silent());
        assertTrue(ChirpLifecycle.isStarted());
    }

    @Test
    void startingTwiceIsNoOp() {
        ChirpLifecycle.start(StartupConfig.silent());
        assertDoesNotThrow(() -> ChirpLifecycle.start(StartupConfig.silent()));
        assertTrue(ChirpLifecycle.isStarted());
    }

    @Test
    void consoleSuccessRequiresStart() {
        assertThrows(ChirpNotStartedException.class, () -> Console.success("hi"));
    }

    @Test
    void consoleErrorRequiresStart() {
        assertThrows(ChirpNotStartedException.class, () -> Console.error("boom"));
    }

    @Test
    void consoleTitleRequiresStart() {
        assertThrows(ChirpNotStartedException.class, () -> Console.title("t"));
    }

    @Test
    void inputReadLineRequiresStart() {
        Input.setInputStream(new ByteArrayInputStream("x\n".getBytes(StandardCharsets.UTF_8)));
        assertThrows(ChirpNotStartedException.class, () -> Input.readLine("Name"));
    }

    @Test
    void inputFluentReaderRequiresStart() {
        assertThrows(ChirpNotStartedException.class, () -> Input.readInt().label("Age"));
    }

    @Test
    void plainPrintlnWorksWithoutStart() {
        assertDoesNotThrow(() -> Console.println("plain"));
    }

    @Test
    void consoleWorksAfterStart() {
        ChirpLifecycle.start(StartupConfig.silent());
        assertDoesNotThrow(() -> Console.success("ok"));
    }

    @Test
    void inputWorksAfterStart() {
        ChirpLifecycle.start(StartupConfig.silent());
        Input.setInputStream(new ByteArrayInputStream("Saad\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("Saad", Input.readLine("Name"));
    }

    @Test
    void exceptionMessageContainsStartHint() {
        ChirpNotStartedException ex = assertThrows(ChirpNotStartedException.class,
                () -> Console.success("hi"));
        assertTrue(ex.getMessage().contains("Chirp.start()"));
    }
}
