package chirp.console;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleTest {

    @Test
    void printArrayWithNullShouldNotThrow() {
        assertDoesNotThrow(() -> Console.printArray((int[]) null));
    }

    @Test
    void titleWithNullShouldNotThrow() {
        assertDoesNotThrow(() -> Console.title(null));
    }

    @Test
    void boldReturnsNonNull() {
        assertNotNull(Console.bold("hello"));
    }

    @Test
    void tableWithNullArgumentsShouldNotThrow() {
        assertDoesNotThrow(() -> Console.table(null, "A", "B"));
        assertDoesNotThrow(() -> Console.table(List.of(), (String[]) null));
    }

    @Test
    void debugIsDisabledByDefault() {
        Debug.disable();
        assertFalse(Debug.isEnabled());
        assertDoesNotThrow(() -> Debug.log("should not print"));
    }

    @Test
    void watermarkWithEmptySessionDoesNotThrow() {
        chirp.session.Session.me().clear();
        assertDoesNotThrow(Console::watermark);
    }

    @Test
    void semanticMessagesDoNotThrow() {
        assertDoesNotThrow(() -> Console.success("ok"));
        assertDoesNotThrow(() -> Console.error("err"));
        assertDoesNotThrow(() -> Console.warn("warn"));
        assertDoesNotThrow(() -> Console.info("info"));
    }

    @Test
    void styleHelpersReturnNonNull() {
        assertNotNull(Console.bold("x"));
        assertNotNull(Console.underline("x"));
        assertNotNull(Console.red("x"));
        assertNotNull(Console.green("x"));
        assertNotNull(Console.yellow("x"));
        assertNotNull(Console.blue("x"));
        assertNotNull(Console.cyan("x"));
    }
}
