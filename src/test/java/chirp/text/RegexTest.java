package chirp.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexTest {

    @Test
    void emailMatches() { assertTrue("user@example.com".matches(Regex.email())); }
    @Test
    void multiDomainEmail() { assertTrue("saad@umt.edu.pk".matches(Regex.email())); }
    @Test
    void digitsMatches() { assertTrue("12345".matches(Regex.digits())); }
    @Test
    void usernameMatches() { assertTrue("saad_mansoor".matches(Regex.username())); }
    @Test
    void hexMatches() { assertTrue("#FF00AA".matches(Regex.hexColor())); }
    @Test
    void urlMatches() { assertTrue("https://example.com".matches(Regex.url())); }

    @Test
    void pakistaniMobilePattern() {
        assertTrue("+923001234567".matches(Regex.pakistaniMobile()));
        assertTrue("03001234567".matches(Regex.pakistaniMobile()));
        assertTrue("00923001234567".matches(Regex.pakistaniMobile()));
    }

    @Test
    void pakistaniCnicPattern() {
        assertTrue("3520212345671".matches(Regex.pakistaniCnic()));
        assertFalse("0020212345671".matches(Regex.pakistaniCnic()));
    }

    @Test
    void strongPasswordReturnsNull() {
        assertNull(Regex.strongPassword());
    }

    @Test
    void veryStrongPasswordReturnsNull() {
        assertNull(Regex.veryStrongPassword());
    }
}
