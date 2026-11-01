package chirp.ui;

import java.util.function.IntFunction;
import javafx.scene.Node;

/**
 * Page navigation control — numbered page buttons.
 *
 * <p>Displays a horizontal set of page numbers, an "N of M" label, and
 * prev/next arrows. The actual page content is supplied via a
 * {@code pageFactory} function that maps a page index to a node.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Pagination.of(10).pageFactory(index ->
 *     Label.of("Content for page " + (index + 1))
 * );
 * }</pre>
 *
 * <h2>Data pagination vs UI pagination</h2>
 * <p>This is the UI control — it displays page numbers and asks the
 * caller for the content of each page. For slicing a list into pages,
 * use {@link chirp.collection.Pagination} (which produces a
 * {@link chirp.collection.Page} of items), then feed each page's
 * content into this control.</p>
 *
 * <h2>Related</h2>
 * <p>For data pagination, see {@link chirp.collection.Pagination}. For a
 * full scrolling list, see {@link ListView}.</p>
 *
 * @see chirp.collection.Pagination
 * @see ListView
 * @since 1.0.0
 */
public class Pagination extends javafx.scene.control.Pagination {

    private Pagination(int pages) { super(Math.max(1, pages)); }

    /**
     * Creates a pagination control with the given number of pages.
     *
     * @param pages the total page count (minimum 1)
     * @return a new pagination control
     * @since 1.0.0
     */
    public static Pagination of(int pages) { return new Pagination(pages); }

    /**
     * Sets the factory that produces content for each page.
     *
     * @param factory a function from page index (0-based) to a Node
     * @return this control, for chaining
     * @since 1.0.0
     */
    public Pagination pageFactory(IntFunction<Node> factory) {
        if (factory != null) setPageFactory(factory::apply);
        return this;
    }

    /**
     * Sets the current page (0-based).
     *
     * @param index the page index
     * @return this control, for chaining
     * @since 1.0.0
     */
    public Pagination currentPage(int index) { setCurrentPageIndex(Math.max(0, index)); return this; }

    /**
     * Returns the current page index (0-based).
     *
     * @return the index
     * @since 1.0.0
     */
    public int currentPage() { return getCurrentPageIndex(); }

    /**
     * Makes the control fill the available horizontal space.
     *
     * @return this control
     * @since 1.0.0
     */
    public Pagination fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
