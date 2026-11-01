package chirp.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CheckTest {

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a.b+c@x.co", "saad@umt.edu.pk"})
    void validEmails(String email) { assertTrue(Check.isEmail(email)); }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "@missing.com", "a@b", ""})
    void invalidEmails(String email) { assertFalse(Check.isEmail(email)); }

    @Test
    void emailNull() { assertFalse(Check.isEmail(null)); }

    @ParameterizedTest
    @ValueSource(strings = {"+9234567890", "03001234567"})
    void validPhones(String phone) { assertTrue(Check.isPhone(phone)); }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12", "123456789012345678"})
    void invalidPhones(String phone) { assertFalse(Check.isPhone(phone)); }

    @Test
    void strongPassword() {
        assertTrue(Check.isStrongPassword("Abc12345"));
        assertFalse(Check.isStrongPassword("weak"));
        assertFalse(Check.isStrongPassword(null));
    }

    @Test
    void veryStrongPassword() {
        assertTrue(Check.isVeryStrongPassword("Abc12345!"));
        assertFalse(Check.isVeryStrongPassword("Abc12345"));
    }

    @Test
    void gradeRange() {
        assertTrue(Check.isValidGrade(85));
        assertFalse(Check.isValidGrade(-1));
        assertFalse(Check.isValidGrade(101));
    }

    @Test
    void urlCheck() {
        assertTrue(Check.isURL("https://example.com"));
        assertTrue(Check.isURL("http://test.org/path"));
        assertFalse(Check.isURL("not-a-url"));
    }

    @Test
    void ipAddress() {
        assertTrue(Check.isIPAddress("192.168.1.1"));
        assertFalse(Check.isIPAddress("999.1.1.1"));
    }

    @Test
    void uuid() {
        assertTrue(Check.isUUID("550e8400-e29b-41d4-a716-446655440000"));
        assertFalse(Check.isUUID("not-a-uuid"));
    }

    @Test
    void leapYear() {
        assertTrue(Check.isLeapYear(2024));
        assertFalse(Check.isLeapYear(2023));
        assertFalse(Check.isLeapYear(1900));
        assertTrue(Check.isLeapYear(2000));
    }

    @Test
    void pakistaniNumbers() {
        assertTrue(Check.isPakistaniPhone("0300-1234567"));
        assertTrue(Check.isPakistaniPhone("+92 300 1234567"));
        assertTrue(Check.isPakistaniCnic("35202-1234567-1"));
        assertTrue(Check.isPakistaniCnic("3520212345671"));
    }

    @Test
    void ruleByName() {
        assertTrue(Check.is("0300-1234567", "PK_MOBILE"));
        assertTrue(Check.is("user@example.com", "EMAIL"));
        assertFalse(Check.is("bogus", "PK_CNIC"));
        assertFalse(Check.is("bogus", "DOES_NOT_EXIST"));
    }
}
