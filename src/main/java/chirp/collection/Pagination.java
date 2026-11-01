package chirp.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * Slicing a list into pages of a fixed size.
 *
 * <p>Every method accepts {@code null} input and returns a valid
 * {@link Page} object (an empty page for a {@code null} list, not
 * {@code null}), so call sites never need to null-check before paging.</p>
 *
 * <h2>Zero-based page indexing</h2>
 * <p><b>Page numbers in this API are 0-based.</b> The first page is
 * {@code page(0)}, the second page is {@code page(1)}, and so on. This
 * mirrors {@link java.util.List#subList(int, int)} and the JDK's index
 * conventions.</p>
 *
 * <p>If your application or UI expects 1-based numbering, use
 * {@link Page#pageNumber()} — it returns the 0-based index plus one.
 * The two accessors exist precisely so both conventions are always
 * available without callers having to add or subtract.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li>{@link #paginate(List, int, int)} — slice any page from the list</li>
 *   <li>{@link #firstPage(List, int)} — the first page</li>
 *   <li>{@link #lastPage(List, int)} — the last page (however many items
 *       it holds)</li>
 *   <li>{@link #allPages(List, int)} — every page, as a list</li>
 *   <li>{@link #empty(int, int)} — an empty page at a specific index</li>
 * </ul>
 *
 * <h2>Aliases</h2>
 * <p>Every method has 2–3 aliases so the call site can read naturally:
 * {@code paginate / page / slice / chunkPage}, {@code firstPage / pageOne},
 * {@code allPages / everyPage}.</p>
 *
 * <h2>Null and empty input</h2>
 * <ul>
 *   <li>{@code null} list → empty page</li>
 *   <li>Empty list → empty page with 0 total items and 0 total pages</li>
 *   <li>Out-of-bounds page index → empty page with the correct total
 *       count (so a pager UI can render "page 99 of 3" without crashing)</li>
 *   <li>{@code size <= 0} → treated as size 1</li>
 *   <li>Negative page index → treated as page 0</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * List<Student> all = loadStudents();
 * int pageSize = 10;
 *
 * Page<Student> first = Pagination.paginate(all, 0, pageSize);
 * Page<Student> second = Pagination.page(all, 1, pageSize);
 * Page<Student> last  = Pagination.lastPage(all, pageSize);
 *
 * int totalPages = first.totalPages();
 * for (int i = 0; i < totalPages; i++) {
 *     Page<Student> page = Pagination.paginate(all, i, pageSize);
 *     renderPage(page);
 * }
 * }</pre>
 *
 * @see Page
 * @since 1.0.0
 */
public final class Pagination {

    private Pagination() {}

    /**
     * Returns the requested page from the list.
     *
     * <p>All bounds are clamped: a negative page index becomes 0, a
     * non-positive page size becomes 1, and an out-of-bounds page index
     * returns an empty page with the correct total count. Never throws.</p>
     *
     * @param items the list to paginate (may be null)
     * @param page the 0-based page index (negative → 0)
     * @param size the page size (≤ 0 → 1)
     * @param <T> the element type
     * @return a {@link Page} holding the slice
     * @see #page(List, int, int)
     * @see #slice(List, int, int)
     * @see #chunkPage(List, int, int)
     * @see #firstPage(List, int)
     * @see #lastPage(List, int)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #paginate(List, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param items the list to paginate
     * @param page the 0-based page index
     * @param size the page size
     * @param <T> the element type
     * @return a {@link Page}
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> page(List<T> items, int page, int size) { return paginate(items, page, size); }

    /**
     * Alias of {@link #paginate(List, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param items the list to paginate
     * @param page the 0-based page index
     * @param size the page size
     * @param <T> the element type
     * @return a {@link Page}
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> slice(List<T> items, int page, int size) { return paginate(items, page, size); }

    /**
     * Alias of {@link #paginate(List, int, int)} — see that method for
     * full documentation and examples.
     *
     * @param items the list to paginate
     * @param page the 0-based page index
     * @param size the page size
     * @param <T> the element type
     * @return a {@link Page}
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> chunkPage(List<T> items, int page, int size) { return paginate(items, page, size); }

    /**
     * Returns the first page.
     *
     * <p>Equivalent to {@code paginate(items, 0, size)}.</p>
     *
     * @param items the list (may be null)
     * @param size the page size
     * @param <T> the element type
     * @return the first page
     * @see #pageOne(List, int)
     * @see #lastPage(List, int)
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> firstPage(List<T> items, int size) {
        return paginate(items, 0, size);
    }

    /**
     * Alias of {@link #firstPage(List, int)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param size the page size
     * @param <T> the element type
     * @return the first page
     * @see #firstPage(List, int)
     * @since 1.0.0
     */
    public static <T> Page<T> pageOne(List<T> items, int size) { return firstPage(items, size); }

    /**
     * Returns the last page — the page that holds the final items of the
     * list.
     *
     * <p>For an empty list, returns the same as
     * {@code paginate(items, 0, size)} — an empty page.</p>
     *
     * @param items the list (may be null)
     * @param size the page size
     * @param <T> the element type
     * @return the last page
     * @see #firstPage(List, int)
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> lastPage(List<T> items, int size) {
        int safeSize = Math.max(1, size);
        int total = items == null ? 0 : items.size();
        int lastPageIndex = total == 0 ? 0 : (total - 1) / safeSize;
        return paginate(items, lastPageIndex, safeSize);
    }

    /**
     * Returns every page of the list as a single {@code List<Page<T>>}.
     *
     * <p>Useful when the caller needs to iterate all pages (e.g. to render
     * a compact "…" pager). For very large lists, prefer a
     * {@code for} loop over {@link #paginate} to avoid materialising every
     * page at once.</p>
     *
     * @param items the list (may be null)
     * @param size the page size
     * @param <T> the element type
     * @return the list of pages, empty for a null or empty input
     * @see #everyPage(List, int)
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
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

    /**
     * Alias of {@link #allPages(List, int)} — see that method for full
     * documentation and examples.
     *
     * @param items the list
     * @param size the page size
     * @param <T> the element type
     * @return the list of pages
     * @see #allPages(List, int)
     * @since 1.0.0
     */
    public static <T> List<Page<T>> everyPage(List<T> items, int size) { return allPages(items, size); }

    /**
     * Returns an empty page at the given page index with the given size.
     *
     * <p>Useful for pager UIs that need to render "no results" while
     * preserving the current page index (e.g. after a search yields
     * nothing).</p>
     *
     * @param page the 0-based page index (negative → 0)
     * @param size the page size (≤ 0 → 1)
     * @param <T> the element type
     * @return an empty page
     * @see #paginate(List, int, int)
     * @since 1.0.0
     */
    public static <T> Page<T> empty(int page, int size) {
        return new Page<>(List.of(), page, size, 0);
    }
}
