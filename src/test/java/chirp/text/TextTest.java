package chirp.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TextTest {

    @ParameterizedTest
    @CsvSource({
            "hello,     Hello",
            "HELLO,     Hello",
            "'  hello', Hello",
            "'',        ''"
    })
    void capitalize(String input, String expected) {
        assertEquals(expected, Text.capitalize(input));
    }

    @Test
    void capitalizeNull() { assertNull(Text.capitalize(null)); }

    @Test
    void titleCase() {
        assertEquals("Hello World", Text.titleCase("hello world"));
        assertEquals("Hello World", Text.titleCase("HELLO WORLD"));
    }

    @Test
    void camelCase() {
        assertEquals("helloWorld", Text.toCamelCase("hello world"));
        assertEquals("helloWorld", Text.toCamelCase("hello_world"));
        assertEquals("helloWorld", Text.toCamelCase("hello-world"));
    }

    @Test
    void pascalCase() {
        assertEquals("HelloWorld", Text.toPascalCase("hello world"));
    }

    @Test
    void snakeCase() {
        assertEquals("hello_world", Text.toSnakeCase("Hello World"));
    }

    @Test
    void kebabCase() {
        assertEquals("hello-world", Text.toKebabCase("Hello World"));
    }

    @Test
    void reverse() {
        assertEquals("cba", Text.reverse("abc"));
        assertNull(Text.reverse(null));
    }

    @Test
    void padLeft() { assertEquals("   hi", Text.padLeft("hi", 5)); }
    @Test
    void padRight() { assertEquals("hi   ", Text.padRight("hi", 5)); }
    @Test
    void center() { assertEquals(" hi  ", Text.center("hi", 5)); }

    @Test
    void truncate() {
        assertEquals("abc", Text.truncate("abc", 5));
        assertEquals("abcd…", Text.truncate("abcdefg", 5));
    }

    @Test
    void isPalindrome() {
        assertTrue(Text.isPalindrome("A man a plan a canal Panama"));
        assertFalse(Text.isPalindrome("hello"));
        assertFalse(Text.isPalindrome(""));
    }

    @Test
    void initials() {
        assertEquals("SM", Text.initials("saad mansoor"));
        assertEquals("", Text.initials(""));
    }

    @Test
    void slugify() {
        assertEquals("hello-world", Text.slugify("Hello World!"));
        assertEquals("a-b-c", Text.slugify("a b c"));
    }

    @Test
    void mask() {
        assertEquals("***4567", Text.mask("1234567", 4));
        assertEquals("****", Text.mask("1234", 0));
        assertEquals("****", Text.mask("1234", 4));
    }

    @Test
    void between() {
        assertEquals("middle", Text.between("start-middle-end", "start-", "-end"));
        assertNull(Text.between("nothing", "start-", "-end"));
    }

    @Test
    void countCharAndWords() {
        assertEquals(3, Text.countChar("banana", 'a'));
        assertEquals(3, Text.countWords("one two three"));
    }

    @Test
    void join() {
        assertEquals("a, b, c", Text.join(", ", "a", "b", "c"));
    }
}
