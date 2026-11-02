package chirp.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract for {@link Session#isComplete()}, {@link Session#missingFields()},
 * and their aliases.
 */
class SessionCompleteTest {

    @BeforeEach
    void reset() { Session.me().clear(); }

    // ============================================================ isComplete

    @Test
    void emptySessionIsNotComplete() {
        assertFalse(Session.me().isComplete());
    }

    @Test
    void onlyNameIsNotComplete() {
        Session.me().name("Saad");
        assertFalse(Session.me().isComplete());
    }

    @Test
    void onlyNameAndRollIsNotComplete() {
        Session.me().name("Saad").rollNumber("2024-CS-001");
        assertFalse(Session.me().isComplete());
    }

    @Test
    void nameRollDegreeIsComplete() {
        Session.me()
                .name("Saad")
                .rollNumber("2024-CS-001")
                .degree("BSAI");
        assertTrue(Session.me().isComplete());
    }

    @Test
    void extraFieldsDontAffectCompleteness() {
        Session.me()
                .name("Saad")
                .rollNumber("2024-CS-001")
                .degree("BSAI")
                .batch("2024")
                .section("A")
                .email("saad@example.com");
        assertTrue(Session.me().isComplete());
    }

    @Test
    void blankNameIsNotComplete() {
        Session.me()
                .name("   ")
                .rollNumber("2024-CS-001")
                .degree("BSAI");
        assertFalse(Session.me().isComplete());
    }

    @Test
    void nullFieldIsNotComplete() {
        Session.me()
                .name("Saad")
                .rollNumber(null)
                .degree("BSAI");
        assertFalse(Session.me().isComplete());
    }

    @Test
    void aliasesMatchCanonical() {
        Session.me().name("Saad").rollNumber("001").degree("BSAI");
        assertEquals(Session.me().isComplete(), Session.me().isReady());
        assertEquals(Session.me().isComplete(), Session.me().hasProfile());
    }

    @Test
    void clearResetsCompleteness() {
        Session.me().name("Saad").rollNumber("001").degree("BSAI");
        assertTrue(Session.me().isComplete());
        Session.me().clear();
        assertFalse(Session.me().isComplete());
    }

    // ============================================================ missingFields

    @Test
    void emptySessionMissingAllThree() {
        assertEquals(
                List.of("name", "rollNumber", "degree"),
                Session.me().missingFields());
    }

    @Test
    void onlyNameMissingRollAndDegree() {
        Session.me().name("Saad");
        assertEquals(
                List.of("rollNumber", "degree"),
                Session.me().missingFields());
    }

    @Test
    void onlyNameAndRollMissingDegree() {
        Session.me().name("Saad").rollNumber("001");
        assertEquals(
                List.of("degree"),
                Session.me().missingFields());
    }

    @Test
    void onlyNameAndDegreeMissingRoll() {
        Session.me().name("Saad").degree("BSAI");
        assertEquals(
                List.of("rollNumber"),
                Session.me().missingFields());
    }

    @Test
    void completeSessionMissingNothing() {
        Session.me().name("Saad").rollNumber("001").degree("BSAI");
        assertTrue(Session.me().missingFields().isEmpty());
    }

    @Test
    void blankFieldIsCountedAsMissing() {
        Session.me().name("Saad").rollNumber("   ").degree("BSAI");
        assertEquals(List.of("rollNumber"), Session.me().missingFields());
    }

    @Test
    void missingFieldsIsImmutable() {
        List<String> missing = Session.me().missingFields();
        assertThrows(UnsupportedOperationException.class,
                () -> missing.add("extra"));
    }

    @Test
    void missingFieldsOrderIsStable() {
        List<String> missing = Session.me().missingFields();
        assertEquals(3, missing.size());
        assertEquals("name", missing.get(0));
        assertEquals("rollNumber", missing.get(1));
        assertEquals("degree", missing.get(2));
    }

    @Test
    void missingFieldsIsEmptyWhenComplete() {
        Session.me().name("Saad").rollNumber("001").degree("BSAI");
        assertEquals(List.of(), Session.me().missingFields());
    }
}
