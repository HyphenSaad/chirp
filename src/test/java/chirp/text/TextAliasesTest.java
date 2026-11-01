package chirp.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that every alias produces the exact same result as its canonical
 * counterpart — proves R4 (multiple names) is enforced consistently.
 */
class TextAliasesTest {

    // ---------------------------------------------------------- capitalize family

    @Test
    void capitalizeAliases() {
        assertEquals(Text.capitalize("hello"), Text.capitalise("hello"));
        assertEquals(Text.capitalize("hello"), Text.toCapitalized("hello"));
        assertEquals(Text.capitalize("hello"), Text.upperFirst("hello"));
    }

    // ---------------------------------------------------------- titleCase family

    @Test
    void titleCaseAliases() {
        assertEquals(Text.titleCase("hello world"), Text.toTitleCase("hello world"));
        assertEquals(Text.titleCase("hello world"), Text.headlineCase("hello world"));
    }

    // ---------------------------------------------------------- camelCase family

    @Test
    void camelCaseAliases() {
        assertEquals(Text.toCamelCase("hello world"), Text.camelCase("hello world"));
        assertEquals(Text.toCamelCase("hello world"), Text.toCamel("hello world"));
    }

    // ---------------------------------------------------------- pascalCase family

    @Test
    void pascalCaseAliases() {
        assertEquals(Text.toPascalCase("hello world"), Text.pascalCase("hello world"));
        assertEquals(Text.toPascalCase("hello world"), Text.toClassName("hello world"));
    }

    // ---------------------------------------------------------- snakeCase family

    @Test
    void snakeCaseAliases() {
        assertEquals(Text.toSnakeCase("Hello World"), Text.snakeCase("Hello World"));
        assertEquals(Text.toSnakeCase("Hello World"), Text.toUnderscoreCase("Hello World"));
    }

    // ---------------------------------------------------------- kebabCase family

    @Test
    void kebabCaseAliases() {
        assertEquals(Text.toKebabCase("Hello World"), Text.kebabCase("Hello World"));
        assertEquals(Text.toKebabCase("Hello World"), Text.toDashCase("Hello World"));
    }

    // ---------------------------------------------------------- reverse family

    @Test
    void reverseAliases() {
        assertEquals(Text.reverse("abc"), Text.reverseText("abc"));
        assertEquals(Text.reverse("abc"), Text.flip("abc"));
    }

    // ---------------------------------------------------------- pad family

    @Test
    void padLeftAliases() {
        assertEquals(Text.padLeft("hi", 5), Text.leftPad("hi", 5));
        assertEquals(Text.padLeft("hi", 5), Text.padStart("hi", 5));
    }

    @Test
    void padRightAliases() {
        assertEquals(Text.padRight("hi", 5), Text.rightPad("hi", 5));
        assertEquals(Text.padRight("hi", 5), Text.padEnd("hi", 5));
    }

    @Test
    void centerAliases() {
        assertEquals(Text.center("hi", 5), Text.centre("hi", 5));
        assertEquals(Text.center("hi", 5), Text.padCenter("hi", 5));
    }

    // ---------------------------------------------------------- truncate family

    @Test
    void truncateAliases() {
        String s = "abcdefghij";
        assertEquals(Text.truncate(s, 5), Text.cut(s, 5));
        assertEquals(Text.truncate(s, 5), Text.shorten(s, 5));
        assertEquals(Text.truncate(s, 5), Text.abbreviate(s, 5));
    }

    // ---------------------------------------------------------- wrap family

    @Test
    void wrapAliases() {
        String s = "one two three four five six";
        assertEquals(Text.wrap(s, 10), Text.wrapText(s, 10));
        assertEquals(Text.wrap(s, 10), Text.wordWrap(s, 10));
    }

    // ---------------------------------------------------------- initials family

    @Test
    void initialsAliases() {
        assertEquals(Text.initials("saad mansoor"), Text.getInitials("saad mansoor"));
        assertEquals(Text.initials("saad mansoor"), Text.acronym("saad mansoor"));
    }

    // ---------------------------------------------------------- slugify family

    @Test
    void slugifyAliases() {
        assertEquals(Text.slugify("Hello World!"), Text.toSlug("Hello World!"));
    }

    // ---------------------------------------------------------- between family

    @Test
    void betweenAliases() {
        String s = "start-middle-end";
        assertEquals(Text.between(s, "start-", "-end"), Text.substringBetween(s, "start-", "-end"));
        assertEquals(Text.between(s, "start-", "-end"), Text.extractBetween(s, "start-", "-end"));
    }

    // ---------------------------------------------------------- countChar family

    @Test
    void countCharAliases() {
        assertEquals(Text.countChar("banana", 'a'), Text.countOccurrences("banana", 'a'));
    }

    // ---------------------------------------------------------- countWords family

    @Test
    void countWordsAliases() {
        assertEquals(Text.countWords("one two three"), Text.wordCount("one two three"));
    }

    // ---------------------------------------------------------- isEmpty family

    @Test
    void isBlankAliases() {
        assertTrue(Text.isBlank(null));
        assertFalse(Text.isNotBlank(null));
        assertTrue(Text.isBlank("  "));
        assertFalse(Text.hasText("  "));
    }

    // ---------------------------------------------------------- levenshtein family

    @Test
    void levenshteinAliases() {
        assertEquals(Text.levenshtein("kitten", "sitting"), Text.editDistance("kitten", "sitting"));
    }

    // ---------------------------------------------------------- removeSpaces family

    @Test
    void removeSpacesAliases() {
        assertEquals(Text.removeSpaces("a b c"), Text.removeWhitespace("a b c"));
        assertEquals(Text.removeSpaces("a b c"), Text.stripSpaces("a b c"));
    }
}
