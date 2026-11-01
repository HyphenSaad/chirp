package chirp.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.*;

/** The diagnostics channel: platform logger by default, console sink in interactive mode. */
class LoggerTest {

    @AfterEach
    void restoreLibraryMode() {
        Logger.clearConsoleSink();
    }

    @Test
    void withoutSinkMessagesReachThePlatformLogger() {
        Logger.clearConsoleSink();
        java.util.logging.Logger jul = java.util.logging.Logger.getLogger("chirp");
        List<LogRecord> records = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord r) { records.add(r); }
            @Override
            public void flush() {}
            @Override
            public void close() {}
        };
        boolean useParent = jul.getUseParentHandlers();
        jul.setUseParentHandlers(false);
        jul.addHandler(collector);
        try {
            Logger.warn("disk almost full");
            Logger.error("cannot read", new IOException("denied"));
            Logger.error("plain error");
        } finally {
            jul.removeHandler(collector);
            jul.setUseParentHandlers(useParent);
        }

        assertEquals(3, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        assertEquals("disk almost full", records.get(0).getMessage());
        assertEquals(Level.SEVERE, records.get(1).getLevel());
        assertEquals("cannot read", records.get(1).getMessage());
        assertInstanceOf(IOException.class, records.get(1).getThrown());
        assertEquals(Level.SEVERE, records.get(2).getLevel());
        assertNull(records.get(2).getThrown());
    }

    @Test
    void consoleSinkReceivesLevelAndMessageInsteadOfThePlatformLogger() {
        List<String> seen = new ArrayList<>();
        Logger.installConsoleSink((level, message) -> seen.add(level + ":" + message));
        assertTrue(Logger.hasConsoleSink());

        Logger.warn("careful");
        Logger.error("broken");
        Logger.error("with cause", new IllegalStateException("x"));
        Logger.warn(null);

        assertEquals(List.of("WARNING:careful", "ERROR:broken", "ERROR:with cause", "WARNING:"), seen);
    }

    @Test
    void failingSinkNeverPropagatesToTheCaller() {
        Logger.installConsoleSink((level, message) -> {
            throw new IllegalStateException("console unavailable");
        });
        assertDoesNotThrow(() -> Logger.error("still reported"));
        assertDoesNotThrow(() -> Logger.warn("still reported"));
    }

    @Test
    void clearingTheSinkRestoresLibraryMode() {
        Logger.installConsoleSink((level, message) -> {});
        Logger.clearConsoleSink();
        assertFalse(Logger.hasConsoleSink());
        assertDoesNotThrow(() -> Logger.warn(null));
    }
}
