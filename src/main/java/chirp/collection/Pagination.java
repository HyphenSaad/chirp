package chirp.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination helpers — aliases for {@code paginate}.
 *
 * <p>{@code paginate / page / slice / chunkPage}; {@code firstPage};
 * {@code lastPage}; {@code allPages}; {@code empty}.</p>
 */
public final class Pagination {

    private Pagination() {}

    public static <T> Page<T> paginate(List<T> items, int page, int size) {
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int total = items == null ? 0 : items.size();

        if (total == 0) {
            return new Page<>(List.of(), safePage, safeSize, 0);
        }

        int from = safePage * safeSize;
        if (from >= total) {
            return new Page<>(List.of(), safePage, safeSize, total);
        }
        int to = Math.min(from + safeSize, total);
        return new Page<>(items.subList(from, to), safePage, safeSize, total);
    }
    public static <T> Page<T> page(List<T> items, int page, int size) { return paginate(items, page, size); }
    public static <T> Page<T> slice(List<T> items, int page, int size) { return paginate(items, page, size); }
    public static <T> Page<T> chunkPage(List<T> items, int page, int size) { return paginate(items, page, size); }

    public static <T> Page<T> firstPage(List<T> items, int size) {
        return paginate(items, 0, size);
    }
    public static <T> Page<T> pageOne(List<T> items, int size) { return firstPage(items, size); }

    public static <T> Page<T> lastPage(List<T> items, int size) {
        int safeSize = Math.max(1, size);
        int total = items == null ? 0 : items.size();
        int lastPageIndex = total == 0 ? 0 : (total - 1) / safeSize;
        return paginate(items, lastPageIndex, safeSize);
    }

    public static <T> List<Page<T>> allPages(List<T> items, int size) {
        int safeSize = Math.max(1, size);
        int total = items == null ? 0 : items.size();
        int pages = (int) Math.ceil((double) total / safeSize);

        List<Page<T>> result = new ArrayList<>(pages);
        for (int i = 0; i < pages; i++) {
            result.add(paginate(items, i, safeSize));
        }
        return result;
    }
    public static <T> List<Page<T>> everyPage(List<T> items, int size) { return allPages(items, size); }

    public static <T> Page<T> empty(int page, int size) {
        return new Page<>(List.of(), page, size, 0);
    }
}
