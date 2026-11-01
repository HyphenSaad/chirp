package chirp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the new components: RadioGroup, Table, and helpers.
 */
class NewComponentsTest {

    // ---------------------------------------------------------- RadioGroup

    @Test
    void radioGroupValueStartsNull() {
        assertNull(RadioGroup.of("A", "B").value());
    }

    @Test
    void radioGroupSelectedChain() {
        RadioGroup g = RadioGroup.of("A", "B", "C").selected("B");
        assertEquals("B", g.value());
    }

    @Test
    void radioGroupRequiredFailsInitially() {
        RadioGroup g = RadioGroup.of("A", "B").required();
        assertFalse(g.isValid());
        g.selected("A");
        assertTrue(g.isValid());
    }

    @Test
    void radioGroupNullOptionsIgnored() {
        assertDoesNotThrow(() -> RadioGroup.of((String[]) null));
    }

    // ---------------------------------------------------------- Table

    @Test
    void tableStartsEmpty() {
        assertEquals(0, Table.of("A", "B").rowCount());
    }

    @Test
    void tableAddRow() {
        Table t = Table.of("ID", "Name").row(1, "Alice");
        assertEquals(1, t.rowCount());
    }

    @Test
    void tableAddManyRows() {
        Table t = Table.of("A").row(1).row(2).row(3);
        assertEquals(3, t.rowCount());
    }

    @Test
    void tableClear() {
        Table t = Table.of("A").row(1).row(2).clearAll();
        assertEquals(0, t.rowCount());
    }

    @Test
    void tableNullColumnsIgnored() {
        assertDoesNotThrow(() -> Table.of((String[]) null));
    }

    @Test
    void tableShortRowPadded() {
        Table t = Table.of("A", "B", "C").row(1);
        assertEquals(1, t.rowCount());
        assertEquals(3, t.getItems().get(0).size());
    }

    // ---------------------------------------------------------- ImageView

    @Test
    void imageViewNullUrlIgnored() {
        assertDoesNotThrow(() -> ImageView.of().url(null));
    }

    @Test
    void imageViewSizeChain() {
        ImageView iv = ImageView.of().size(100, 100);
        assertEquals(100.0, iv.getFitWidth());
        assertEquals(100.0, iv.getFitHeight());
    }

    @Test
    void imageViewCallbacksAccepted() {
        ImageView iv = ImageView.of()
                .onLoaded(() -> {})
                .onError(() -> {})
                .onProgress(p -> {});
        assertNotNull(iv);
    }

    // ---------------------------------------------------------- dialogs (structure only)

    @Test
    void uiIsAvailable() {
        assertTrue(Ui.isAvailable());
    }

    @Test
    void themeStylesheetsExist() {
        assertNotNull(Ui.class.getResource("/chirp/ui/theme-light.css"));
        assertNotNull(Ui.class.getResource("/chirp/ui/theme-dark.css"));
    }

    // ---------------------------------------------------------- Stack / Row

    @Test
    void stackBuilds() {
        Stack s = Stack.v(10).padding(20)
                .add(Label.of("a"), Label.of("b"));
        assertEquals(2, s.getChildren().size());
    }

    @Test
    void rowBuilds() {
        Row r = Row.h(8).add(Label.of("a"), Button.primary("b"));
        assertEquals(2, r.getChildren().size());
    }

    @Test
    void stackNullChildrenIgnored() {
        Stack s = Stack.v().add((javafx.scene.Node[]) null);
        assertEquals(0, s.getChildren().size());
    }
}
