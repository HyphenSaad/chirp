package chirp.text;

import chirp.validation.Rule;
import chirp.validation.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract for the library's person-name syntax:
 * {@link Check#isName(String)}, {@link Regex#name()}, and the
 * {@code NAME} rule registered in {@link Rules}.
 */
class NameValidationTest {

    static Stream<String> validNames() {
        return Stream.of(
                "Saad",
                "Saad Mansoor",
                "O'Brien",
                "Al-Rashid",
                "J. R. R. Tolkien",
                "Müller",
                "Ånne",
                "François",
                "李明",
                "Иван",
                "سعد",
                "अनिल",
                "Mary-Jane Watson",
                "van der Berg",
                "Sa'ad",
                "Jean-Luc Picard",
                "A",
                "A B C",
                "O'Brien-Smith");
    }

    static Stream<String> invalidNames() {
        return Stream.of(
                "",
                " ",
                "  ",
                "Saad123",
                "Saad!",
                "Saad  Mansoor",
                "  Saad",
                "Saad ",
                "-Saad",
                "Saad-",
                "O'-Brien",
                "123",
                "-",
                "'",
                "Sa'ad!B",
                "Saad_Mansoor",
                "Saad/Mansoor",
                "Saad\\Mansoor");
    }

    // ============================================================ Check.isName

    @ParameterizedTest
    @MethodSource("validNames")
    void acceptsRealisticNames(String name) {
        assertTrue(Check.isName(name), name);
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void rejectsMalformedNames(String name) {
        assertFalse(Check.isName(name), name);
    }

    @Test
    void nullIsNotAName() {
        assertFalse(Check.isName(null));
    }

    @Test
    void isPersonNameAlias() {
        assertEquals(Check.isName("Saad"), Check.isPersonName("Saad"));
        assertEquals(Check.isName("Saad123"), Check.isPersonName("Saad123"));
        assertEquals(Check.isName(null), Check.isPersonName(null));
    }

    @Test
    void unicodeScriptsAccepted() {
        assertTrue(Check.isName("李明"), "Chinese");
        assertTrue(Check.isName("Иван"), "Russian");
        assertTrue(Check.isName("سعد"), "Arabic");
        assertTrue(Check.isName("अनिल"), "Hindi (uses combining marks)");
        assertTrue(Check.isName("Müller"), "German umlaut");
        assertTrue(Check.isName("Ånne"), "Norwegian ring");
        assertTrue(Check.isName("François"), "French cedilla");
    }

    @Test
    void isAlphaIsStillAsciiOnly() {
        assertFalse(Check.isAlpha("Saad Mansoor"));
        assertFalse(Check.isAlpha("O'Brien"));
        assertFalse(Check.isAlpha("Müller"));
        assertTrue(Check.isAlpha("Saad"));

        assertTrue(Check.isName("Saad Mansoor"));
        assertTrue(Check.isName("O'Brien"));
        assertTrue(Check.isName("Müller"));
        assertTrue(Check.isName("Saad"));
    }

    // ============================================================ Regex.name

    @Test
    void regexNameMatchesCheckExactly() {
        validNames().forEach(n -> assertEquals(Check.isName(n), n.matches(Regex.name()), n));
        invalidNames().forEach(n -> assertEquals(Check.isName(n), n.matches(Regex.name()), n));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Saad", "Saad Mansoor", "O'Brien", "李明"})
    void regexIsAnchoredToWholeInput(String name) {
        // The prefix/suffix must be a character that CANNOT appear in a
        // valid name. "prefix " would be a false test — "prefix Saad"
        // is a valid two-word name, so `matches` correctly returns true.
        // A digit or underscore is unambiguously out-of-range, so any
        // match with one of those means the regex is not anchored.
        assertFalse(("1" + name).matches(Regex.name()), "digit prefix");
        assertFalse(("_" + name).matches(Regex.name()), "underscore prefix");
        assertFalse((name + "1").matches(Regex.name()), "digit suffix");
        assertFalse((name + "_").matches(Regex.name()), "underscore suffix");
    }

    // ============================================================ NAME rule

    @Test
    void nameRuleIsRegistered() {
        Rule rule = Rules.get("NAME");
        assertNotNull(rule);
        assertEquals("NAME", rule.name());
        assertTrue(Rules.has("NAME"));
    }

    @Test
    void nameRuleTrimsSurroundingWhitespace() {
        Rule rule = Rules.name();
        assertTrue(rule.test("  Saad  "));
        assertTrue(rule.test("Saad"));
        assertFalse(Check.isName("  Saad  "));
    }

    @Test
    void nameRuleStillRejectsMalformedContent() {
        Rule rule = Rules.name();
        assertFalse(rule.test("Saad123"));
        assertFalse(rule.test("Saad  Mansoor"));
        assertFalse(rule.test("O'-Brien"));
        assertFalse(rule.test("123"));
    }

    @Test
    void nameRuleHandlesUnicode() {
        Rule rule = Rules.name();
        assertTrue(rule.test("李明"));
        assertTrue(rule.test("Иван"));
        assertTrue(rule.test("Müller"));
        assertTrue(rule.test("अनिल"));
    }

    @Test
    void nameRuleRequiresNonBlank() {
        Rule rule = Rules.name();
        assertFalse(rule.test(""));
        assertFalse(rule.test("   "));
        assertFalse(rule.test(null));
    }

    @Test
    void nameRuleAliasesResolveToSameRule() {
        Rule canonical = Rules.name();
        Rule personName = Rules.personName();
        Rule nameRule = Rules.nameRule();
        assertEquals(canonical.name(), personName.name());
        assertEquals(canonical.name(), nameRule.name());
    }

    // ============================================================ Facade

    @Test
    void chirpFacadeExposesIsName() {
        assertTrue(chirp.Chirp.isName("Saad"));
        assertFalse(chirp.Chirp.isName("Saad123"));
        assertTrue(chirp.Chirp.isPersonName("Saad Mansoor"));
    }

    @Test
    void chirpFacadeExposesNameRule() {
        Rule rule = chirp.Chirp.nameRule();
        assertNotNull(rule);
        assertEquals("NAME", rule.name());
        assertTrue(rule.test("Saad"));
    }

    // ============================================================ Integration

    @Test
    void nameRuleIntegratesWithValidationApi() {
        assertTrue(chirp.Chirp.validate("Saad", "NAME").isValid());
        assertFalse(chirp.Chirp.validate("Saad123", "NAME").isValid());
    }

    @Test
    void checkIsNameWorksThroughRulesFacade() {
        assertTrue(Check.is("  Saad  ", "NAME"));
        assertFalse(Check.isName("  Saad  "));
    }
}
