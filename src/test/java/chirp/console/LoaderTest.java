package chirp.console;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {

    // ---------------------------------------------------------- factories

    @Test
    void factoryAliases() {
        assertNotNull(Loader.start("test"));
        assertNotNull(Loader.begin("test"));
        assertNotNull(Loader.show("test"));
        assertNotNull(Loader.loading("test"));
        assertNotNull(Loader.spinner("test"));
        assertNotNull(Loader.busy("test"));
        assertNotNull(Loader.working("test"));
        assertNotNull(Loader.of("test"));
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
        Loader.start("test").stop();
    }

    @Test
    void silentFactory() {
        assertNotNull(Loader.silent());
    }

    // ---------------------------------------------------------- terminals

    @Test
    void terminalAliases() {
        assertDoesNotThrow(() -> Loader.start("t").stop());
        assertDoesNotThrow(() -> Loader.start("t").end());
        assertDoesNotThrow(() -> Loader.start("t").finish());
        assertDoesNotThrow(() -> Loader.start("t").cancel());
        assertDoesNotThrow(() -> Loader.start("t").halt());
    }

    @Test
    void stopIsIdempotent() {
        Loader l = Loader.start("test");
        l.stop();
        assertDoesNotThrow(l::stop);   // second call is a no-op
    }

    // ---------------------------------------------------------- result-print

    @Test
    void successAliases() {
        assertDoesNotThrow(() -> Loader.start("t").success("done"));
        assertDoesNotThrow(() -> Loader.start("t").ok("done"));
        assertDoesNotThrow(() -> Loader.start("t").good("done"));
    }

    @Test
    void errorAliases() {
        assertDoesNotThrow(() -> Loader.start("t").error("failed"));
        assertDoesNotThrow(() -> Loader.start("t").fail("failed"));
    }

    @Test
    void warnAliases() {
        assertDoesNotThrow(() -> Loader.start("t").warn("warning"));
        assertDoesNotThrow(() -> Loader.start("t").warning("warning"));
    }

    // ---------------------------------------------------------- withLoader

    @Test
    void withLoaderRunsAction() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Loader.withLoader("test", () -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void withLoaderSwallowsExceptionAfterLoading() {
        // The action throws — the loader reports the error and rethrows
        assertThrows(RuntimeException.class,
                () -> Loader.withLoader("test", () -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void withLoaderAliases() {
        AtomicBoolean ranA = new AtomicBoolean(false);
        AtomicBoolean ranB = new AtomicBoolean(false);
        Loader.run("a", () -> ranA.set(true));
        Loader.spin("b", () -> ranB.set(true));
        assertTrue(ranA.get());
        assertTrue(ranB.get());
    }
}
