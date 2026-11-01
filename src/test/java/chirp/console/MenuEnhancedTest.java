package chirp.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuEnhancedTest {

    // ---------------------------------------------------------- factories

    @Test
    void factoryAliases() {
        assertNotNull(Menu.create("T"));
        assertNotNull(Menu.of("T"));
        assertNotNull(Menu.named("T"));
    }

    @Test
    void nullTitleDefaults() {
        Menu m = new Menu(null);
        assertEquals(0, m.size());
    }

    // ---------------------------------------------------------- add aliases

    @Test
    void addAliases() {
        Menu m = new Menu("T")
                .add("A", () -> {})
                .item("B", () -> {})
                .option("C", () -> {})
                .withOption("D", () -> {});
        assertEquals(4, m.size());
    }

    @Test
    void addNullIgnored() {
        Menu m = new Menu("T")
                .add(null, () -> {})
                .add("A", null);
        assertEquals(0, m.size());
    }

    // ---------------------------------------------------------- separators

    @Test
    void separatorAliases() {
        Menu m = new Menu("T")
                .addSeparator()
                .separator()
                .divider()
                .rule();
        assertEquals(4, m.size());
    }

    // ---------------------------------------------------------- headers

    @Test
    void headerAliases() {
        Menu m = new Menu("T")
                .addHeader("Group 1")
                .header("Group 2")
                .group("Group 3");
        assertEquals(3, m.size());
    }

    // ---------------------------------------------------------- exit

    @Test
    void exitAliases() {
        Menu m = new Menu("T")
                .addExit("Quit")
                .exit()
                .withExit()
                .exitWith("Leave");
        assertEquals(4, m.size());
    }

    // ---------------------------------------------------------- size

    @Test
    void sizeAndCount() {
        Menu m = new Menu("T").add("A", () -> {}).add("B", () -> {});
        assertEquals(2, m.size());
        assertEquals(2, m.count());
    }

    // ---------------------------------------------------------- nulls

    @Test
    void emptyMenuShowDoesNotThrow() {
        assertDoesNotThrow(() -> new Menu("T").show());
    }
}
