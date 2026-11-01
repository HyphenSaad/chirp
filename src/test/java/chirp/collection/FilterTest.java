package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterTest {

    @Test
    void whereKeepsMatches() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        assertEquals(List.of(2, 4), Filter.where(nums, n -> n % 2 == 0));
    }

    @Test
    void rejectDropsMatches() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        assertEquals(List.of(1, 3, 5), Filter.reject(nums, n -> n % 2 == 0));
    }

    @Test
    void emptyInput() {
        assertTrue(Filter.where(List.of(), n -> true).isEmpty());
        assertTrue(Filter.reject(List.of(), n -> false).isEmpty());
    }

    @Test
    void nullInput() {
        assertTrue(Filter.where(null, n -> true).isEmpty());
        assertTrue(Filter.reject(null, n -> true).isEmpty());
    }

    @Test
    void nonNullRemoves() {
        List<String> items = java.util.Arrays.asList("a", null, "b", null);
        assertEquals(List.of("a", "b"), Filter.nonNull(items));
    }

    @Test
    void distinctPreservesOrder() {
        assertEquals(List.of(1, 2, 3), Filter.distinct(List.of(1, 2, 1, 3, 2)));
    }

    @Test
    void anyMatch() {
        assertTrue(Filter.anyMatch(List.of(1, 2, 3), n -> n > 2));
        assertFalse(Filter.anyMatch(List.of(1, 2, 3), n -> n > 10));
    }

    @Test
    void allMatch() {
        assertTrue(Filter.allMatch(List.of(2, 4, 6), n -> n % 2 == 0));
        assertFalse(Filter.allMatch(List.of(2, 3, 4), n -> n % 2 == 0));
    }

    @Test
    void count() {
        assertEquals(2, Filter.count(List.of(1, 2, 3, 4), n -> n % 2 == 0));
    }

    @Test
    void extractorFilter() {
        record Person(String name, int age) {}
        List<Person> people = List.of(
                new Person("Ali", 20),
                new Person("Bob", 15),
                new Person("Chan", 30));
        List<Person> adults = Filter.where(people, Person::age, age -> age >= 18);
        assertEquals(2, adults.size());
    }
}
