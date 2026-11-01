package chirp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static chirp.Chirp.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests proving that a single {@code import static chirp.Chirp.*;}
 * surfaces the most commonly-needed helpers.
 */
class ChirpFacadeTest {

    @Test
    void arraysFlow() {
        int[] nums = {3, 1, 4, 1, 5, 9, 2, 6};
        assertEquals(31, sum(nums));
        assertEquals(9, max(nums));
        assertEquals(1, min(nums));
        assertArrayEquals(new int[]{1, 1, 2, 3, 4, 5, 6, 9}, sorted(nums));
        assertTrue(hasDuplicates(nums));
    }

    @Test
    void formattingFlow() {
        assertEquals("Hello, Ali! You are 20.",
                fmt("Hello, {name}! You are {age}.", "name", "Ali", "age", 20));
        assertEquals("1 item", plural(1, "item", "items"));
        assertEquals("5 items", plural(5, "item", "items"));
        assertEquals("A, B, and C",
                list(List.of("A", "B", "C"), ", ", ", and "));
    }

    @Test
    void numbersFlow() {
        assertTrue(isPrime(7));
        assertFalse(isEven(7));
        assertEquals(120L, factorial(5));
        assertEquals(6, gcd(48, 18));
        assertEquals(List.of(1, 2, 3, 4), range(1, 5));
    }

    @Test
    void textFlow() {
        assertEquals("Hello World", titleCase("hello world"));
        assertEquals("HelloWorld", toPascalCase("hello world"));
        assertEquals("helloWorld", toCamelCase("hello_world"));
        assertTrue(isPalindrome("A man a plan a canal Panama"));
    }

    @Test
    void checksFlow() {
        assertTrue(isEmail("user@example.com"));
        assertTrue(isPhone("+923001234567"));
        assertTrue(isPakistaniPhone("0300-1234567"));
        assertTrue(isPakistaniCnic("35202-1234567-1"));
        assertTrue(isStrongPassword("Abc12345!"));
    }

    @Test
    void rulesFlow() {
        assertTrue(is("0300-1234567", "PK_MOBILE"));
        assertTrue(is("35202-1234567-1", "PK_CNIC"));
        assertTrue(hasRule("EMAIL"));
        assertNotNull(getRule("EMAIL"));
        assertNotNull(pkMobile());
        assertNotNull(pkCnic());
    }

    @Test
    void formatDomainHelpers() {
        assertEquals("35202-1234567-1", cnic("3520212345671"));
        assertEquals("+92 300 1234567", pakistaniPhone("0300-1234567"));
    }

    @Test
    void sessionFlow() {
        session().clear();
        session().name("Saad").rollNumber("2024-CS-001");
        assertEquals("Saad", session().name());
        assertEquals("2024-CS-001", session().rollNumber());
        session().clear();
    }
}
