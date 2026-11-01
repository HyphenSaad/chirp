package chirp.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RulesTest {

    @BeforeEach
    void reset() { Rules.reset(); }

    @Test
    void pakistaniMobileVariants() {
        Rule pk = Rules.pakistaniMobile();
        assertTrue(pk.test("03001234567"));
        assertTrue(pk.test("0300-1234567"));
        assertTrue(pk.test("0300 1234567"));
        assertTrue(pk.test("+92 300 1234567"));
        assertTrue(pk.test("+923001234567"));
        assertTrue(pk.test("00923001234567"));
        assertTrue(pk.test("923001234567"));
    }

    @Test
    void pakistaniMobileRejects() {
        Rule pk = Rules.pakistaniMobile();
        assertFalse(pk.test("03123456"));       // too short
        assertFalse(pk.test("05001234567"));    // wrong prefix (fixed line)
        assertFalse(pk.test("+92 400 1234567")); // wrong operator prefix 40
        assertFalse(pk.test("abc"));
    }

    @Test
    void pakistaniCnic() {
        Rule cnic = Rules.pakistaniCnic();
        assertTrue(cnic.test("35202-1234567-1"));
        assertTrue(cnic.test("3520212345671"));
        assertTrue(cnic.test("  35202 1234567 1  "));
        assertFalse(cnic.test("0020212345671")); // starts with 0 (invalid province)
        assertFalse(cnic.test("123"));
    }

    @Test
    void cnicProvince() {
        assertEquals("Punjab", Rules.cnicProvince("35202-1234567-1"));
        assertEquals("Sindh", Rules.cnicProvince("42201-1234567-2"));
        assertEquals("Khyber Pakhtunkhwa", Rules.cnicProvince("17301-1234567-3"));
        assertNull(Rules.cnicProvince("00000-0000000-0"));
    }

    @Test
    void registerAndLookup() {
        Rule custom = Rule.define("MY_RULE").matches("[A-Z]+").message("upper only").build();
        Rules.register(custom);

        assertTrue(Rules.has("MY_RULE"));
        assertNotNull(Rules.get("MY_RULE"));
        assertTrue(Rules.get("MY_RULE").test("ABC"));
        assertFalse(Rules.get("MY_RULE").test("abc"));
    }

    @Test
    void requireMissingThrows() {
        assertThrows(IllegalArgumentException.class, () -> Rules.require("DOES_NOT_EXIST"));
    }

    @Test
    void namesIncludesBuiltins() {
        assertTrue(Rules.names().contains("PK_MOBILE"));
        assertTrue(Rules.names().contains("PK_CNIC"));
        assertTrue(Rules.names().contains("EMAIL"));
    }
}
