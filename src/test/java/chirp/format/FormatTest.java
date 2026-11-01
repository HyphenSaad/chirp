package chirp.format;

import chirp.session.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {

    @AfterEach
    void clearSession() { Session.me().clear(); }

    @Test
    void fmtNamedPlaceholders() {
        assertEquals("Hello, Ali! You are 20.",
                Format.fmt("Hello, {name}! You are {age}.",
                        "name", "Ali", "age", 20));
    }

    @Test
    void fmtNullTemplate() {
        assertEquals("", Format.fmt(null, "k", "v"));
    }

    @Test
    void fmtWithMap() {
        assertEquals("Hi Bob", Format.fmt("Hi {n}", Map.of("n", "Bob")));
    }

    @Test
    void fmtWithSession() {
        Session.me().clear();
        Session.me().name("Saad").rollNumber("001");
        assertEquals("Saad (001)", Format.fmtWithSession("{name} ({roll})"));
    }

    @Test
    void plural() {
        assertEquals("1 item", Format.plural(1, "item", "items"));
        assertEquals("3 items", Format.plural(3, "item", "items"));
    }

    @Test
    void listWithLastDelimiter() {
        assertEquals("A, B, and C", Format.list(List.of("A", "B", "C"), ", ", ", and "));
    }

    @Test
    void listSingle() {
        assertEquals("A", Format.list(List.of("A"), ", ", ", and "));
    }

    @Test
    void currency() {
        assertTrue(Format.currency(1234.5).contains("1,234.50"));
    }

    @Test
    void rupees() {
        assertTrue(Format.rupees(1234.5).contains("1,234.50"));
    }

    @Test
    void cnic() {
        assertEquals("35202-1234567-1", Format.cnic("3520212345671"));
        assertEquals("incomplete", Format.cnic("incomplete"));
    }

    @Test
    void pakistaniPhone() {
        assertEquals("+92 300 1234567", Format.pakistaniPhone("0300-1234567"));
        assertEquals("+92 300 1234567", Format.pakistaniPhone("+92 300 1234567"));
        assertEquals("invalid", Format.pakistaniPhone("invalid"));
    }
}
