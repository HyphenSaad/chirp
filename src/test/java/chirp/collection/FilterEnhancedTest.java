package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase-4 filter tests — `all` returns a filtered `List`; `allMatch` is the
 * boolean "everything matches" check. The two live in separate namespaces.
 */
class FilterEnhancedTest {

    // ============================================================== filter-returning aliases

    @Test
    void filterAliases() {
        List<Integer> list = List.of(1, 2, 3, 4);
        List<Integer> expected = Filter.where(list, n -> n % 2 == 0);
        assertListEquals(expected, Filter.filter(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.keep(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.select(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.all(list, n -> n % 2 == 0));   // ← all is List-returning
        assertListEquals(expected, Filter.apply(list, n -> n % 2 == 0));
    }

    @Test
    void rejectAliases() {
        List<Integer> list = List.of(1, 2, 3, 4);
        List<Integer> expected = Filter.reject(list, n -> n % 2 == 0);
        assertListEquals(expected, Filter.exclude(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.not(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.removeIf(list, n -> n % 2 == 0));
        assertListEquals(expected, Filter.drop(list, n -> n % 2 == 0));
    }

    // ============================================================== extractor

    @Test
    void whereField() {
        record Person(String name, int age) {}
        List<Person> people = List.of(
                new Person("Ali", 20),
                new Person("Bob", 15),
                new Person("Chan", 30));
        List<Person> adults = Filter.where(people, Person::age, age -> age >= 18);
        assertEquals(2, adults.size());
        assertEquals(adults, Filter.whereField(people, Person::age, age -> age >= 18));
        assertEquals(adults, Filter.filterBy(people, Person::age, age -> age >= 18));
    }

    // ============================================================== set-like

    @Test
    void nonNullAliases() {
        List<String> items = Arrays.asList("a", null, "b", null);
        assertEquals(List.of("a", "b"), Filter.nonNull(items));
        assertEquals(List.of("a", "b"), Filter.notNull(items));
        assertEquals(List.of("a", "b"), Filter.removeNulls(items));
    }

    @Test
    void distinctAliases() {
        List<Integer> list = List.of(1, 2, 1, 3, 2);
        assertEquals(List.of(1, 2, 3), Filter.distinct(list));
        assertEquals(List.of(1, 2, 3), Filter.unique(list));
        assertEquals(List.of(1, 2, 3), Filter.dedupe(list));
    }

    // ============================================================== boolean aliases

    @Test
    void anyAllNone() {
        List<Integer> list = List.of(1, 2, 3, 4);
        // Boolean checks — *Match / any / none
        assertTrue(Filter.anyMatch(list, n -> n > 3));
        assertTrue(Filter.any(list, n -> n > 3));
        assertTrue(Filter.allMatch(list, n -> n > 0));
        assertTrue(Filter.everyMatch(list, n -> n > 0));
        assertTrue(Filter.allSatisfy(list, n -> n > 0));
        assertTrue(Filter.noneMatch(list, n -> n > 10));
        assertTrue(Filter.none(list, n -> n > 10));
    }

    @Test
    void countAliases() {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        assertEquals(2, Filter.count(list, n -> n % 2 == 0));
        assertEquals(2, Filter.countWhere(list, n -> n % 2 == 0));
    }

    // ============================================================== first / last

    @Test
    void firstLast() {
        List<Integer> list = List.of(10, 20, 30);
        assertEquals(10, Filter.first(list));
        assertEquals(30, Filter.last(list));
        assertEquals(10, Filter.firstOr(List.of(), 10));
        assertEquals(30, Filter.lastOr(List.of(), 30));
        assertNull(Filter.first(List.of()));
        assertNull(Filter.last(List.of()));
    }

    @Test
    void firstMatch() {
        List<Integer> list = List.of(1, 3, 4, 6);
        assertEquals(4, Filter.firstMatch(list, n -> n % 2 == 0));
        assertEquals(4, Filter.find(list, n -> n % 2 == 0));
        assertEquals(4, Filter.firstWhere(list, n -> n % 2 == 0));
        assertNull(Filter.firstMatch(list, n -> n > 100));
    }

    @Test
    void limitAndSkip() {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        assertEquals(List.of(1, 2), Filter.limit(list, 2));
        assertEquals(List.of(1, 2), Filter.take(list, 2));
        assertEquals(List.of(1, 2), Filter.firstN(list, 2));
        assertEquals(List.of(4, 5), Filter.skip(list, 3));
        assertEquals(List.of(4, 5), Filter.dropN(list, 3));
    }

    // ============================================================== partition

    @Test
    void partition() {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        var result = Filter.partition(list, n -> n % 2 == 0);
        assertEquals(List.of(2, 4), result.matching());
        assertEquals(List.of(1, 3, 5), result.nonMatching());
        assertEquals(List.of(2, 4), result.matchingItems());
        assertEquals(List.of(1, 3, 5), result.nonMatchingItems());
    }

    @Test
    void partitionAlias() {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        var p1 = Filter.partition(list, n -> n > 2);
        var p2 = Filter.split(list, n -> n > 2);
        assertEquals(p1.matching(), p2.matching());
        assertEquals(p1.nonMatching(), p2.nonMatching());
    }

    // ============================================================== grouping

    @Test
    void groupBy() {
        List<String> words = List.of("apple", "banana", "apricot", "blueberry");
        Map<Character, List<String>> byFirstLetter = Filter.groupBy(words, s -> s.charAt(0));
        assertEquals(2, byFirstLetter.size());
        assertEquals(List.of("apple", "apricot"), byFirstLetter.get('a'));
        assertEquals(List.of("banana", "blueberry"), byFirstLetter.get('b'));
    }

    @Test
    void groupByAlias() {
        List<String> words = List.of("a", "b", "ab");
        Map<Integer, List<String>> byLength = Filter.group(words, String::length);
        assertNotNull(byLength);
        assertEquals(byLength, Filter.groupByKey(words, String::length));
    }

    // ============================================================== merge / flatten

    @Test
    void merge() {
        List<Integer> a = List.of(1, 2);
        List<Integer> b = List.of(3, 4);
        assertEquals(List.of(1, 2, 3, 4), Filter.merge(a, b));
        assertEquals(List.of(1, 2, 3, 4), Filter.concat(a, b));
    }

    @Test
    void flatten() {
        List<List<Integer>> nested = List.of(
                List.of(1, 2),
                List.of(3, 4, 5));
        assertEquals(List.of(1, 2, 3, 4, 5), Filter.flatten(nested));
        assertEquals(List.of(1, 2, 3, 4, 5), Filter.flat(nested));
    }

    // ============================================================== null safety

    @Test
    void nullSafety() {
        assertTrue(Filter.where(null, x -> true).isEmpty());
        assertTrue(Filter.reject(null, x -> true).isEmpty());
        assertTrue(Filter.nonNull(null).isEmpty());
        assertTrue(Filter.distinct(null).isEmpty());
        assertEquals(0, Filter.count(null, x -> true));
    }

    // ============================================================== helper

    private static <T> void assertListEquals(List<T> a, List<T> b) {
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) assertEquals(a.get(i), b.get(i));
    }
}
