package chirp.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @BeforeEach
    void reset() { Session.me().clear(); }

    @Test
    void fullConfiguration() {
        Session.me()
                .name("Saad Mansoor")
                .rollNumber("2024-CS-001")
                .batch("2024")
                .degree("BSAI")
                .section("A")
                .email("saad@example.com");

        assertEquals("Saad Mansoor", Session.me().name());
        assertEquals("2024-CS-001", Session.me().rollNumber());
        assertEquals("2024", Session.me().batch());
        assertEquals("BSAI", Session.me().degree());
        assertEquals("A", Session.me().section());
        assertEquals("saad@example.com", Session.me().email());
    }

    @Test
    void aliasesWork() {
        Session.me().fullName("Ali").roll("R1").batchNumber("B1").degreeCode("CS");
        assertEquals("Ali", Session.me().studentName());
        assertEquals("R1", Session.me().rollNo());
        assertEquals("B1", Session.me().batch());
        assertEquals("CS", Session.me().program());
    }

    @Test
    void summarySkipsEmptyFields() {
        Session.me().name("Saad");
        String summary = Session.me().summary();
        assertTrue(summary.contains("Name: Saad"));
        assertFalse(summary.contains("Roll:"));
    }

    @Test
    void clearRemovesEverything() {
        Session.me().name("X").email("x@example.net");
        Session.me().clear();
        assertNull(Session.me().name());
        assertNull(Session.me().email());
        assertTrue(Session.me().asMap().isEmpty());
    }

    @Test
    void asMapOnlyHasPopulatedFields() {
        Session.me().name("Saad").rollNumber("001");
        var map = Session.me().asMap();
        assertEquals(2, map.size());
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("rollNumber"));
    }

    @Test
    void singletonIdentity() {
        assertSame(Session.me(), Session.get());
        assertSame(Session.me(), Session.of());
        assertSame(Session.me(), Session.current());
        assertSame(Session.me(), Session.configure());
    }
}
