package chirp.collection;

import java.util.List;

/**
 * One page of results.
 *
 * <h2>Naming</h2>
 * <p>{@code content / items / data}; {@code pageNumber / number / index};
 * {@code totalItems / total / itemCount}; {@code totalPages};
 * {@code hasNext / hasPrevious / isFirst / isLast};
 * {@code nextPage / nextIndex}; {@code previousPage / previousIndex}.</p>
 */
public final class Page<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;

    public Page(List<T> content, int page, int size, long totalItems) {
        this.content = content == null ? List.of() : List.copyOf(content);
        this.page = Math.max(0, page);
        this.size = Math.max(1, size);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.size);
    }

    // ============================================================== content

    public List<T> content() { return content; }
    public List<T> items() { return content; }
    public List<T> data() { return content; }
    public List<T> getContent() { return content; }

    // ============================================================== page number

    public int page() { return page; }         // 0-based
    public int number() { return page; }
    public int index() { return page; }
    public int pageNumber() { return page + 1; }     // 1-based
    public int oneBasedNumber() { return page + 1; }

    // ============================================================== size / counts

    public int size() { return size; }
    public int pageSize() { return size; }
    public long totalItems() { return totalItems; }
    public long total() { return totalItems; }
    public long itemCount() { return totalItems; }
    public long totalElements() { return totalItems; }

    public int totalPages() { return totalPages; }
    public int pageCount() { return totalPages; }

    public int numberOfElements() { return content.size(); }
    public int elementCount() { return content.size(); }

    public boolean hasContent() { return !content.isEmpty(); }
    public boolean isEmpty() { return content.isEmpty(); }
    public boolean isNotEmpty() { return !content.isEmpty(); }

    // ============================================================== navigation

    public boolean isFirst() { return page == 0; }
    public boolean isLast() { return page >= totalPages - 1 || totalPages == 0; }
    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }

    public int nextPage() { return hasNext() ? page + 1 : page; }
    public int nextIndex() { return nextPage(); }
    public int previousPage() { return hasPrevious() ? page - 1 : page; }
    public int previousIndex() { return previousPage(); }

    // ============================================================== display

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
