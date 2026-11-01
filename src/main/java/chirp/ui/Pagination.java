package chirp.ui;

import java.util.function.IntFunction;
import javafx.scene.Node;

/**
 * Pagination control — divides content into numbered pages.
 *
 * <pre>{@code
 * Pagination p = Pagination.of(10).pageFactory(index ->
 *     Label.of("Content for page " + (index + 1))
 * );
 * }</pre>
 */
public class Pagination extends javafx.scene.control.Pagination {

    private Pagination(int pages) { super(Math.max(1, pages)); }

    public static Pagination of(int pages) { return new Pagination(pages); }

    public Pagination pageFactory(IntFunction<Node> factory) {
        if (factory != null) setPageFactory(factory::apply);
        return this;
    }

    public Pagination currentPage(int index) { setCurrentPageIndex(Math.max(0, index)); return this; }
    public int currentPage() { return getCurrentPageIndex(); }
    public Pagination fullWidth() { setMaxWidth(Double.MAX_VALUE); return this; }
}
