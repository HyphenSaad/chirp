package chirp.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RulesEnhancedTest {

    @BeforeEach
    void reset() { Rules.reset(); }

    // ---------------------------------------------------------- lookup aliases

    @Test
    void lookupAliases() {
        assertNotNull(Rules.get("EMAIL"));
        assertNotNull(Rules.find("EMAIL"));
        assertNotNull(Rules.lookup("EMAIL"));
        assertNotNull(Rules.rule("EMAIL"));
    }

    @Test
    void existsAliases() {
        assertTrue(Rules.has("EMAIL"));
        assertTrue(Rules.exists("EMAIL"));
        assertTrue(Rules.contains("EMAIL"));
        assertFalse(Rules.has("NOPE"));
    }

    @Test
    void namesAliases() {
        assertEquals(Rules.names(), Rules.listAll());
        assertEquals(Rules.names(), Rules.registered());
        assertEquals(Rules.names(), Rules.allNames());
    }

    // ---------------------------------------------------------- one-shot test

    @Test
    void oneShotTest() {
        assertTrue(Rules.test("user@example.com", "EMAIL"));
        assertFalse(Rules.test("not-an-email", "EMAIL"));
        assertTrue(Rules.check("user@example.com", "EMAIL"));
        assertTrue(Rules.is("user@example.com", "EMAIL"));
        assertFalse(Rules.test("anything", "UNKNOWN"));
    }

    // ---------------------------------------------------------- all

    @Test
    void allFetchesMultiple() {
        List<Rule> rules = Rules.all("EMAIL", "PK_MOBILE", "PK_CNIC");
        assertEquals(3, rules.size());
    }

    @Test
    void allThrowsOnMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> Rules.all("EMAIL", "DOES_NOT_EXIST"));
    }

    // ---------------------------------------------------------- built-in aliases

    @Test
    void emailAliases() {
        assertNotNull(Rules.email());
        assertNotNull(Rules.emailAddress());
        assertNotNull(Rules.emailRule());
    }

    @Test
    void pakistaniMobileAliases() {
        assertNotNull(Rules.pakistaniMobile());
        assertNotNull(Rules.pkMobile());
        assertNotNull(Rules.pkPhone());
        assertNotNull(Rules.pakistanMobile());
        assertNotNull(Rules.pkMobileNumber());
    }

    @Test
    void cnicAliases() {
        assertNotNull(Rules.pakistaniCnic());
        assertNotNull(Rules.pkCnic());
        assertNotNull(Rules.pkIdCard());
        assertNotNull(Rules.pakistanCnic());
    }

    @Test
    void landlineAliases() {
        assertNotNull(Rules.pakistaniLandline());
        assertNotNull(Rules.pkLandline());
        assertNotNull(Rules.pkPhoneLandline());
    }

    // ---------------------------------------------------------- key-value registration

    @Test
    void registerFromKeyValues() {
        Rule r = Rules.register(
                Rule.Key.NAME, "MY_CUSTOM",
                Rule.Key.PATTERN, "^[A-Z]+$",
                Rule.Key.MESSAGE, "uppercase only");
        assertNotNull(r);
        assertTrue(Rules.has("MY_CUSTOM"));
        assertTrue(Rules.test("ABC", "MY_CUSTOM"));
        assertFalse(Rules.test("abc", "MY_CUSTOM"));
    }

    @Test
    void defineFromKeyValues() {
        Rule r = Rules.define(
                Rule.Key.NAME, "ALSO_CUSTOM",
                Rule.Key.MIN_LENGTH, 3,
                Rule.Key.MESSAGE, "min 3");
        assertTrue(Rules.has("ALSO_CUSTOM"));
        assertTrue(r.test("abc"));
    }

    // ---------------------------------------------------------- reset

    @Test
    void resetRestoresBuiltins() {
        Rules.register(Rule.define("TEMP").matches(".*").message("x").build());
        assertTrue(Rules.has("TEMP"));
        Rules.reset();
        assertFalse(Rules.has("TEMP"));
        assertTrue(Rules.has("EMAIL"));
    }

    @Test
    void clearAlias() {
        Rules.register(Rule.define("TEMP").matches(".*").message("x").build());
        Rules.clear();
        assertFalse(Rules.has("TEMP"));
        assertTrue(Rules.has("EMAIL"));
    }
}
