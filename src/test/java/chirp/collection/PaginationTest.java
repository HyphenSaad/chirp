package chirp.collection;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {

    private final List<Integer> data = IntStream.rangeClosed(1, 25).boxed().toList();

    @Test
    void firstPage() {
        Page<Integer> p = Pagination.paginate(data, 0, 10);
        assertEquals(10, p.numberOfElements());
        assertEquals(25, p.totalItems());
        assertEquals(3, p.totalPages());
        assertEquals(0, p.page());
        assertTrue(p.isFirst());
        assertFalse(p.isLast());
        assertTrue(p.hasNext());
        assertFalse(p.hasPrevious());
        assertEquals(1, p.content().get(0));
        assertEquals(10, p.content().get(9));
    }

    @Test
    void middlePage() {
        Page<Integer> p = Pagination.paginate(data, 1, 10);
        assertEquals(10, p.numberOfElements());
        assertEquals(11, p.content().get(0));
        assertEquals(20, p.content().get(9));
        assertTrue(p.hasNext());
        assertTrue(p.hasPrevious());
    }

    @Test
    void lastPage() {
        Page<Integer> p = Pagination.paginate(data, 2, 10);
        assertEquals(5, p.numberOfElements());
        assertEquals(21, p.content().get(0));
        assertEquals(25, p.content().get(4));
        assertTrue(p.isLast());
        assertFalse(p.hasNext());
    }

    @Test
    void outOfRangeReturnsEmpty() {
        Page<Integer> p = Pagination.paginate(data, 99, 10);
        assertEquals(0, p.numberOfElements());
        assertTrue(p.isEmpty());
        assertEquals(25, p.totalItems());
    }

    @Test
    void emptyInput() {
        Page<Integer> p = Pagination.paginate(List.of(), 0, 10);
        assertEquals(0, p.numberOfElements());
        assertEquals(0, p.totalItems());
        assertEquals(0, p.totalPages());
        assertTrue(p.isEmpty());
    }

    @Test
    void nullInput() {
        Page<Integer> p = Pagination.paginate(null, 0, 10);
        assertEquals(0, p.numberOfElements());
        assertEquals(0, p.totalItems());
    }

    @Test
    void firstAndLastHelpers() {
        assertEquals(1, Pagination.firstPage(data, 10).content().get(0));
        assertEquals(25, Pagination.lastPage(data, 10).content().get(4));
    }

    @Test
    void allPages() {
        List<Page<Integer>> pages = Pagination.allPages(data, 10);
        assertEquals(3, pages.size());
        assertEquals(10, pages.get(0).numberOfElements());
        assertEquals(10, pages.get(1).numberOfElements());
        assertEquals(5, pages.get(2).numberOfElements());
    }

    @Test
    void zeroSizeClampsToOne() {
        Page<Integer> p = Pagination.paginate(data, 0, 0);
        assertEquals(1, p.numberOfElements());
    }
}
