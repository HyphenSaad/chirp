package chirp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UiAppTest {

    // ---------------------------------------------------------- builder

    @Test
    void defaultsApplied() {
        UiApp app = UiApp.builder().build();
        assertEquals("Chirp", app.title());
        assertEquals(900, app.width());
        assertEquals(600, app.height());
        assertEquals(UiTheme.AUTO, app.theme());
        assertTrue(app.resizable());
        assertTrue(app.scroll());
        assertNull(app.content());
    }

    @Test
    void titleChain() {
        assertEquals("Custom", UiApp.builder().title("Custom").build().title());
    }

    @Test
    void sizeChain() {
        UiApp app = UiApp.builder().size(1024, 768).build();
        assertEquals(1024, app.width());
        assertEquals(768, app.height());
    }

    @Test
    void sizeClamps() {
        UiApp app = UiApp.builder().size(1, 1).build();
        assertTrue(app.width() >= 320);
        assertTrue(app.height() >= 240);
    }

    @Test
    void themeChain() {
        assertEquals(UiTheme.DARK, UiApp.builder().dark().build().theme());
        assertEquals(UiTheme.LIGHT, UiApp.builder().light().build().theme());
        assertEquals(UiTheme.AUTO, UiApp.builder().auto().build().theme());
    }

    @Test
    void resizableChain() {
        assertFalse(UiApp.builder().resizable(false).build().resizable());
        assertFalse(UiApp.builder().fixedSize().build().resizable());
    }

    @Test
    void scrollChain() {
        assertFalse(UiApp.builder().noScroll().build().scroll());
    }

    @Test
    void contentChain() {
        Stack root = Stack.v(8);
        assertSame(root, UiApp.builder().content(root).build().content());
    }

    @Test
    void contentConsumer() {
        UiApp app = UiApp.builder().content(s -> s.add(Label.of("hi"))).build();
        assertNotNull(app.content());
        assertTrue(app.content() instanceof Stack);
    }

    @Test
    void onCloseChain() {
        Runnable r = () -> {};
        assertSame(r, UiApp.builder().onClose(r).build().onClose());
    }

    // ---------------------------------------------------------- key-value

    @Test
    void fromKeyValue() {
        UiApp app = UiApp.from(
                UiApp.Key.TITLE, "KeyTest",
                UiApp.Key.WIDTH, 500,
                UiApp.Key.HEIGHT, 400);
        assertEquals("KeyTest", app.title());
        assertEquals(500, app.width());
        assertEquals(400, app.height());
    }

    @Test
    void fromNullPairsIgnored() {
        UiApp app = UiApp.from((Object[]) null);
        assertNotNull(app);
        assertEquals("Chirp", app.title());
    }

    @Test
    void fromOddPairsIgnored() {
        UiApp app = UiApp.from(UiApp.Key.TITLE, "Odd", UiApp.Key.WIDTH);
        assertEquals("Odd", app.title());
        assertEquals(900, app.width());
    }

    @Test
    void fromWrongValueTypeIgnored() {
        UiApp app = UiApp.from(UiApp.Key.WIDTH, "not a number");
        assertEquals(900, app.width());
    }

    @Test
    void fromNullValueIgnored() {
        UiApp app = UiApp.from(UiApp.Key.TITLE, null);
        assertEquals("Chirp", app.title());
    }

    // ---------------------------------------------------------- components

    @Test
    void labelFactories() {
        assertEquals("Hello", Label.of("Hello").getText());
        assertEquals("", Label.of(null).getText());
    }

    @Test
    void labelTextChain() {
        assertEquals("Updated", Label.of("x").text("Updated").getText());
    }

    @Test
    void buttonFactories() {
        assertEquals("Click", Button.primary("Click").getText());
        assertEquals("Click", Button.secondary("Click").getText());
        assertEquals("Click", Button.of(null).getText().isEmpty() ? "Click" : "");
    }

    @Test
    void buttonOnClickFires() {
        java.util.concurrent.atomic.AtomicInteger n = new java.util.concurrent.atomic.AtomicInteger();
        Button b = Button.primary("x").onClick(n::incrementAndGet);
        b.fire();
        assertEquals(1, n.get());
    }

    @Test
    void textFieldValidation() {
        TextField f = TextField.of().validate(s -> s != null && s.length() >= 3, "too short");
        assertFalse(f.isValid());
        f.text("ab");
        assertFalse(f.isValid());
        f.text("abcd");
        assertTrue(f.isValid());
    }

    @Test
    void selectValue() {
        Select s = Select.of("A", "B", "C").selected("B");
        assertEquals("B", s.value());
    }

    @Test
    void progressBarFactories() {
        assertNotNull(ProgressBar.of());
        assertNotNull(ProgressBar.of(0.5));
    }

    // ---------------------------------------------------------- theme

    @Test
    void themeResolve() {
        assertEquals(UiTheme.LIGHT, UiTheme.LIGHT.resolve());
        assertEquals(UiTheme.DARK, UiTheme.DARK.resolve());
    }

    @Test
    void themeStylesheetsExist() {
        assertNotNull(Ui.class.getResource("/chirp/ui/theme-light.css"));
        assertNotNull(Ui.class.getResource("/chirp/ui/theme-dark.css"));
    }
}
