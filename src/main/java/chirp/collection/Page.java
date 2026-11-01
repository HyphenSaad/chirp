package chirp.collection;

import java.util.List;

/**
 * One page of a paginated result.
 *
 * <p>Returned by {@link Pagination} methods. Carries the page's content
 * plus enough metadata to render a pager UI: total item count, total page
 * count, and whether the current page is the first or last.</p>
 *
 * <h2>Zero-based vs. one-based page numbers</h2>
 * <p>This is the second naming trap in this API:</p>
 * <ul>
 *   <li>{@link #page()}, {@link #number()}, {@link #index()} — <b>0-based</b>
 *       page index. The first page returns {@code 0}. Use these to
 *       compute sub-list offsets.</li>
 *   <li>{@link #pageNumber()}, {@link #oneBasedNumber()} — <b>1-based</b>
 *       page number. The first page returns {@code 1}. Use these in UI
 *       labels ("Page 1 of 3").</li>
 * </ul>
 * <p>Both accessors are provided precisely so callers never have to add
 * or subtract 1. If you find yourself writing {@code page.page() + 1} in
 * a UI, use {@code page.pageNumber()} instead.</p>
 *
 * <h2>What this class covers</h2>
 * <ul>
 *   <li><b>Content</b> — {@link #content()}, {@link #items()},
 *       {@link #data()}, {@link #getContent()}</li>
 *   <li><b>Page number</b> — {@link #page()} (0-based),
 *       {@link #pageNumber()} (1-based)</li>
 *   <li><b>Counts</b> — {@link #size()}, {@link #totalItems()},
 *       {@link #totalPages()}, {@link #numberOfElements()}</li>
 *   <li><b>Navigation</b> — {@link #isFirst()}, {@link #isLast()},
 *       {@link #hasNext()}, {@link #hasPrevious()},
 *       {@link #nextPage()}, {@link #previousPage()}</li>
 *   <li><b>Content state</b> — {@link #hasContent()}, {@link #isEmpty()}</li>
 * </ul>
 *
 * <h2>Immutability</h2>
 * <p>The content list is defensively copied in the constructor, so callers
 * cannot mutate the page after construction. The other fields are
 * primitive or value-like ({@code List.copyOf} produces an immutable
 * list).</p>
 *
 * <h2>Example — a simple pager UI</h2>
 * <pre>{@code
 * Page<Student> page = Pagination.paginate(students, currentPageIndex, 10);
 *
 * println("Showing " + page.numberOfElements()
 *       + " of " + page.totalItems() + " students");
 * println("Page " + page.pageNumber() + " of " + page.totalPages());
 *
 * for (Student s : page.items()) {
 *     printRow(s);
 * }
 *
 * if (page.hasPrevious()) println("[← Previous]");
 * if (page.hasNext())     println("[Next →]");
 * }</pre>
 *
 * @param <T> the element type
 * @see Pagination
 * @since 1.0.0
 */
public final class Page<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;

    /**
     * Constructs a page from its content and metadata.
     *
     * <p>Usually you should not construct a {@code Page} directly — use
     * {@link Pagination#paginate(List, int, int)} and its siblings, which
     * compute all the fields correctly.</p>
     *
     * <p>All arguments are sanitized: a {@code null} content list is
     * replaced by an empty immutable list; a negative {@code page} or
     * non-positive {@code size} is clamped to a safe minimum; a negative
     * {@code totalItems} becomes 0.</p>
     *
     * @param content the items on this page (may be null — treated as empty)
     * @param page the 0-based page index (clamped to ≥ 0)
     * @param size the page size (clamped to ≥ 1)
     * @param totalItems the total number of items across all pages (clamped to ≥ 0)
     * @see Pagination#paginate(List, int, int)
     * @since 1.0.0
     */
    public Page(List<T> content, int page, int size, long totalItems) {
        this.content = content == null ? List.of() : List.copyOf(content);
        this.page = Math.max(0, page);
        this.size = Math.max(1, size);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.size);
    }

    // ============================================================== content

    /**
     * Returns the items on this page as an immutable list.
     *
     * @return the page's items
     * @see #items()
     * @see #data()
     * @see #getContent()
     * @since 1.0.0
     */
    public List<T> content() { return content; }

    /**
     * Alias of {@link #content()} — see that method for full
     * documentation.
     *
     * @return the page's items
     * @see #content()
     * @since 1.0.0
     */
    public List<T> items() { return content; }

    /**
     * Alias of {@link #content()} — see that method for full
     * documentation.
     *
     * @return the page's items
     * @see #content()
     * @since 1.0.0
     */
    public List<T> data() { return content; }

    /**
     * Alias of {@link #content()} — see that method for full
     * documentation.
     *
     * @return the page's items
     * @see #content()
     * @since 1.0.0
     */
    public List<T> getContent() { return content; }

    // ============================================================== page number

    /**
     * Returns the 0-based page index. The first page returns {@code 0}.
     *
     * @return the 0-based page index
     * @see #pageNumber()
     * @see #number()
     * @see #index()
     * @since 1.0.0
     */
    public int page() { return page; }

    /**
     * Alias of {@link #page()} — see that method for full documentation.
     *
     * @return the 0-based page index
     * @see #page()
     * @since 1.0.0
     */
    public int number() { return page; }

    /**
     * Alias of {@link #page()} — see that method for full documentation.
     *
     * @return the 0-based page index
     * @see #page()
     * @since 1.0.0
     */
    public int index() { return page; }

    /**
     * Returns the 1-based page number. The first page returns {@code 1}.
     *
     * <p>Use this for UI labels ("Page 1 of 3") — {@link #page()} is
     * 0-based and would print "Page 0 of 3".</p>
     *
     * @return the 1-based page number
     * @see #page()
     * @see #oneBasedNumber()
     * @since 1.0.0
     */
    public int pageNumber() { return page + 1; }

    /**
     * Alias of {@link #pageNumber()} — see that method for full
     * documentation.
     *
     * @return the 1-based page number
     * @see #pageNumber()
     * @since 1.0.0
     */
    public int oneBasedNumber() { return page + 1; }

    // ============================================================== size / counts

    /**
     * Returns the configured page size (the maximum number of items per
     * page, not the number on this page).
     *
     * @return the page size
     * @see #numberOfElements()
     * @see #totalItems()
     * @since 1.0.0
     */
    public int size() { return size; }

    /**
     * Alias of {@link #size()} — see that method for full documentation.
     *
     * @return the page size
     * @see #size()
     * @since 1.0.0
     */
    public int pageSize() { return size; }

    /**
     * Returns the total number of items across all pages.
     *
     * @return the total item count
     * @see #total()
     * @see #itemCount()
     * @see #totalElements()
     * @see #numberOfElements()
     * @since 1.0.0
     */
    public long totalItems() { return totalItems; }

    /**
     * Alias of {@link #totalItems()} — see that method for full
     * documentation.
     *
     * @return the total item count
     * @see #totalItems()
     * @since 1.0.0
     */
    public long total() { return totalItems; }

    /**
     * Alias of {@link #totalItems()} — see that method for full
     * documentation.
     *
     * @return the total item count
     * @see #totalItems()
     * @since 1.0.0
     */
    public long itemCount() { return totalItems; }

    /**
     * Alias of {@link #totalItems()} — see that method for full
     * documentation.
     *
     * @return the total item count
     * @see #totalItems()
     * @since 1.0.0
     */
    public long totalElements() { return totalItems; }

    /**
     * Returns the total number of pages.
     *
     * <p>Computed as {@code ceil(totalItems / size)}. For an empty result
     * set, returns {@code 0} (not 1) — the "no pages" state is
     * distinguishable from the "one empty page" state.</p>
     *
     * @return the total page count
     * @see #pageCount()
     * @see #totalItems()
     * @since 1.0.0
     */
    public int totalPages() { return totalPages; }

    /**
     * Alias of {@link #totalPages()} — see that method for full
     * documentation.
     *
     * @return the total page count
     * @see #totalPages()
     * @since 1.0.0
     */
    public int pageCount() { return totalPages; }

    /**
     * Returns the number of items on <b>this</b> page. Compare with
     * {@link #size()}, which is the configured maximum per page.
     *
     * @return the number of items on this page
     * @see #size()
     * @see #totalItems()
     * @since 1.0.0
     */
    public int numberOfElements() { return content.size(); }

    /**
     * Alias of {@link #numberOfElements()} — see that method for full
     * documentation.
     *
     * @return the number of items on this page
     * @see #numberOfElements()
     * @since 1.0.0
     */
    public int elementCount() { return content.size(); }

    /**
     * Checks whether this page has any items.
     *
     * @return {@code true} if the page is not empty
     * @see #isEmpty()
     * @see #isNotEmpty()
     * @since 1.0.0
     */
    public boolean hasContent() { return !content.isEmpty(); }

    /**
     * Checks whether this page has zero items.
     *
     * @return {@code true} if the page is empty
     * @see #hasContent()
     * @see #isNotEmpty()
     * @since 1.0.0
     */
    public boolean isEmpty() { return content.isEmpty(); }

    /**
     * Checks whether this page has at least one item.
     *
     * @return {@code true} if the page is not empty
     * @see #isEmpty()
     * @since 1.0.0
     */
    public boolean isNotEmpty() { return !content.isEmpty(); }

    // ============================================================== navigation

    /**
     * Checks whether this is the first page (0-based index 0).
     *
     * @return {@code true} if the page index is 0
     * @see #isLast()
     * @see #hasPrevious()
     * @since 1.0.0
     */
    public boolean isFirst() { return page == 0; }

    /**
     * Checks whether this is the last page.
     *
     * @return {@code true} if the page index is {@code totalPages - 1},
     * or if there are no pages at all
     * @see #isFirst()
     * @see #hasNext()
     * @since 1.0.0
     */
    public boolean isLast() { return page >= totalPages - 1 || totalPages == 0; }

    /**
     * Checks whether there is a next page after this one.
     *
     * @return {@code true} if this page is not the last
     * @see #hasPrevious()
     * @see #nextPage()
     * @since 1.0.0
     */
    public boolean hasNext() { return page < totalPages - 1; }

    /**
     * Checks whether there is a previous page before this one.
     *
     * @return {@code true} if this page is not the first
     * @see #hasNext()
     * @see #previousPage()
     * @since 1.0.0
     */
    public boolean hasPrevious() { return page > 0; }

    /**
     * Returns the 0-based index of the next page. If this is already the
     * last page, returns the current index unchanged.
     *
     * @return the next page index, or the current index if already last
     * @see #hasNext()
     * @see #previousPage()
     * @see #nextIndex()
     * @since 1.0.0
     */
    public int nextPage() { return hasNext() ? page + 1 : page; }

    /**
     * Alias of {@link #nextPage()} — see that method for full
     * documentation.
     *
     * @return the next page index
     * @see #nextPage()
     * @since 1.0.0
     */
    public int nextIndex() { return nextPage(); }

    /**
     * Returns the 0-based index of the previous page. If this is already
     * the first page, returns the current index unchanged.
     *
     * @return the previous page index, or the current index if already first
     * @see #hasPrevious()
     * @see #nextPage()
     * @see #previousIndex()
     * @since 1.0.0
     */
    public int previousPage() { return hasPrevious() ? page - 1 : page; }

    /**
     * Alias of {@link #previousPage()} — see that method for full
     * documentation.
     *
     * @return the previous page index
     * @see #previousPage()
     * @since 1.0.0
     */
    public int previousIndex() { return previousPage(); }

    // ============================================================== display

    /**
     * Returns a short summary string — useful for debug output.
     *
     * @return a summary of the page metadata (not the content)
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "Page{" +
                "pageNumber=" + pageNumber() +
                ", size=" + size +
                ", numberOfElements=" + numberOfElements() +
                ", totalItems=" + totalItems +
                ", totalPages=" + totalPages +
                '}';
    }
}
