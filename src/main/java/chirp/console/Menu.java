package chirp.console;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent interactive menu with separators, headers, and exit actions.
 *
 * <p>A {@code Menu} is a numbered list of labelled actions. When shown,
 * it renders the labels with numbered indices, reads the user's choice
 * (retrying on invalid input), and runs the corresponding action.</p>
 *
 * <h2>Two display modes</h2>
 *
 * <h3>1. {@code show()} — looping</h3>
 * <p>Renders the menu, runs the chosen action, then asks "Return to
 * menu?". Repeats until the user declines or chooses an exit entry.
 * Requires {@code Chirp.start()}.</p>
 *
 * <h3>2. {@code showOnce()} / {@code ask()} — single-shot</h3>
 * <p>Renders the menu and runs exactly one choice (or returns its
 * index for {@code ask()}). Useful when you want to control the loop
 * yourself.</p>
 *
 * <h2>Decorators</h2>
 * <p>Between items you can insert {@link #addSeparator() separators}
 * (rendered as {@code ----------}) and {@link #addHeader(String) headers}
 * (rendered as {@code == Header ==}) to visually group items:</p>
 * <pre>{@code
 * Menu m = new Menu("Bank Account");
 * m.add("Deposit",  this::deposit)
 *  .add("Withdraw", this::withdraw)
 *  .addSeparator()
 *  .add("Show balance", this::showBalance);
 * m.show();
 * }</pre>
 *
 * <h2>Exit handling</h2>
 * <p>{@link #addExit(String)} (or {@link #exit()} / {@link #withExit()})
 * adds a special entry that stops the loop when chosen. Its label must
 * be unique — the loop checks the label to decide whether to exit.</p>
 *
 * <h2>Example — complete menu</h2>
 * <pre>{@code
 * Menu menu = new Menu("Student Manager")
 *     .add("Add student",    () -> add())
 *     .add("Remove student", () -> remove())
 *     .add("List students",  () -> list())
 *     .addSeparator()
 *     .addExit("Quit");
 * menu.show();
 * }</pre>
 *
 * <h2>Related</h2>
 * <p>For one-shot prompted choices without a menu object, see
 * {@link Input#readChoice(String, String...)}.</p>
 *
 * @see Console
 * @see Input
 * @since 1.0.0
 */
public final class Menu {

    private final String title;
    private final List<String> labels = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private boolean hasExit;

    /**
     * Constructs a menu with the given title. Usually you should use
     * {@link #create(String)} instead.
     *
     * @param title the menu title (null → {@code "Menu"})
     * @see #create(String)
     * @since 1.0.0
     */
    public Menu(String title) {
        this.title = title != null ? title : "Menu";
    }

    /**
     * Factory — a menu with the given title.
     *
     * @param title the menu title
     * @return a new empty menu
     * @see #of(String)
     * @see #named(String)
     * @since 1.0.0
     */
    public static Menu create(String title) { return new Menu(title); }

    /**
     * Alias of {@link #create(String)} — see that method for full
     * documentation.
     *
     * @param title the menu title
     * @return a new empty menu
     * @see #create(String)
     * @since 1.0.0
     */
    public static Menu of(String title) { return new Menu(title); }

    /**
     * Alias of {@link #create(String)} — see that method for full
     * documentation.
     *
     * @param title the menu title
     * @return a new empty menu
     * @see #create(String)
     * @since 1.0.0
     */
    public static Menu named(String title) { return new Menu(title); }

    // ============================================================== add

    /**
     * Adds a numbered action to the menu.
     *
     * <p>Null arguments are silently ignored — this lets you conditionally
     * add options without an if-statement:</p>
     * <pre>{@code
     * Menu m = new Menu("Options")
     *     .add("View", () -> view())
     *     .add(isAdmin ? "Admin panel" : null, isAdmin ? this::adminPanel : null);
     * }</pre>
     *
     * @param label the menu label (null → no-op)
     * @param action the action to run when chosen (null → no-op)
     * @return this menu, for chaining
     * @see #item(String, Runnable)
     * @see #option(String, Runnable)
     * @see #withOption(String, Runnable)
     * @since 1.0.0
     */
    public Menu add(String label, Runnable action) {
        if (label == null || action == null) return this;
        labels.add(label);
        actions.add(action);
        return this;
    }

    /**
     * Alias of {@link #add(String, Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param label the menu label
     * @param action the action
     * @return this menu
     * @see #add(String, Runnable)
     * @since 1.0.0
     */
    public Menu item(String label, Runnable action) { return add(label, action); }

    /**
     * Alias of {@link #add(String, Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param label the menu label
     * @param action the action
     * @return this menu
     * @see #add(String, Runnable)
     * @since 1.0.0
     */
    public Menu option(String label, Runnable action) { return add(label, action); }

    /**
     * Alias of {@link #add(String, Runnable)} — see that method for full
     * documentation and examples.
     *
     * @param label the menu label
     * @param a the action
     * @return this menu
     * @see #add(String, Runnable)
     * @since 1.0.0
     */
    public Menu withOption(String label, Runnable a) { return add(label, a); }

    // ============================================================== separators

    /**
     * Adds a horizontal separator row. Rendered as {@code ----------}
     * and given a menu number (it is a valid choice but does nothing).
     *
     * @return this menu, for chaining
     * @see #separator()
     * @see #divider()
     * @see #rule()
     * @see #addHeader(String)
     * @since 1.0.0
     */
    public Menu addSeparator() {
        labels.add("---");
        actions.add(() -> {});
        return this;
    }

    /**
     * Alias of {@link #addSeparator()} — see that method for full
     * documentation.
     *
     * @return this menu
     * @see #addSeparator()
     * @since 1.0.0
     */
    public Menu separator() { return addSeparator(); }

    /**
     * Alias of {@link #addSeparator()} — see that method for full
     * documentation.
     *
     * @return this menu
     * @see #addSeparator()
     * @since 1.0.0
     */
    public Menu divider() { return addSeparator(); }

    /**
     * Alias of {@link #addSeparator()} — see that method for full
     * documentation.
     *
     * @return this menu
     * @see #addSeparator()
     * @since 1.0.0
     */
    public Menu rule() { return addSeparator(); }

    // ============================================================== headers

    /**
     * Adds a header row. Rendered as {@code == text ==} and given a
     * menu number (it is a valid choice but does nothing). Use it to
     * visually group items.
     *
     * @param text the header text (null → "== null ==")
     * @return this menu, for chaining
     * @see #header(String)
     * @see #group(String)
     * @see #addSeparator()
     * @since 1.0.0
     */
    public Menu addHeader(String text) {
        labels.add("== " + text + " ==");
        actions.add(() -> {});
        return this;
    }

    /**
     * Alias of {@link #addHeader(String)} — see that method for full
     * documentation.
     *
     * @param text the header text
     * @return this menu
     * @see #addHeader(String)
     * @since 1.0.0
     */
    public Menu header(String text) { return addHeader(text); }

    /**
     * Alias of {@link #addHeader(String)} — see that method for full
     * documentation.
     *
     * @param text the header text
     * @return this menu
     * @see #addHeader(String)
     * @since 1.0.0
     */
    public Menu group(String text) { return addHeader(text); }

    // ============================================================== exit

    /**
     * Adds a special exit entry. When chosen during {@link #show()}, the
     * loop stops. The label must be unique within the menu.
     *
     * <p>Only the <b>first</b> exit entry matters — subsequent calls to
     * this method add more exit-labelled entries but the loop only
     * stops when the first exit label is chosen. In practice, call it
     * once at the end of the menu.</p>
     *
     * @param label the exit label (null → {@code "Exit"})
     * @return this menu, for chaining
     * @see #exit()
     * @see #withExit()
     * @see #exitWith(String)
     * @since 1.0.0
     */
    public Menu addExit(String label) {
        hasExit = true;
        labels.add(label != null ? label : "Exit");
        actions.add(() -> {});
        return this;
    }

    /**
     * Alias of {@link #addExit(String)} with label {@code "Exit"} — see
     * that method for full documentation.
     *
     * @return this menu
     * @see #addExit(String)
     * @since 1.0.0
     */
    public Menu exit() { return addExit("Exit"); }

    /**
     * Alias of {@link #addExit(String)} with label {@code "Exit"} — see
     * that method for full documentation.
     *
     * @return this menu
     * @see #addExit(String)
     * @since 1.0.0
     */
    public Menu withExit() { return addExit("Exit"); }

    /**
     * Alias of {@link #addExit(String)} — see that method for full
     * documentation.
     *
     * @param l the exit label
     * @return this menu
     * @see #addExit(String)
     * @since 1.0.0
     */
    public Menu exitWith(String l) { return addExit(l); }

    // ============================================================== run

    /**
     * Renders the menu, reads one choice, and runs the corresponding
     * action — once. Does not loop, does not ask "Return to menu?".
     * Requires {@code Chirp.start()}.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #show()
     * @see #ask()
     * @see #displayOnce()
     * @since 1.0.0
     */
    public void showOnce() {
        render();
        int choice = Input.readInt("Your choice", 1, labels.size());
        invoke(choice - 1);
    }

    /**
     * Alias of {@link #showOnce()} — see that method for full
     * documentation.
     *
     * @see #showOnce()
     * @since 1.0.0
     */
    public void displayOnce() { showOnce(); }

    /**
     * Renders the menu and loops until the user chooses to exit or
     * declines "Return to menu?". Requires {@code Chirp.start()}.
     *
     * <p>If the menu has an exit entry, choosing it stops the loop.
     * Otherwise the user is asked "Return to menu?" after each action.</p>
     *
     * <p>If the menu has no items, prints a warning via
     * {@link Messenger#warn(String)} and returns immediately.</p>
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #showOnce()
     * @see #run()
     * @see #display()
     * @since 1.0.0
     */
    public void show() {
        if (labels.isEmpty()) {
            Messenger.warn("Menu '" + title + "' has no options.");
            return;
        }
        while (true) {
            render();
            int choice = Input.readInt("Your choice", 1, labels.size());
            invoke(choice - 1);
            if (hasExit && labels.get(choice - 1).equalsIgnoreCase("Exit")) return;
            if (!Input.readYesNo("Return to menu?")) return;
        }
    }

    /**
     * Alias of {@link #show()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #show()
     * @since 1.0.0
     */
    public void run() { show(); }

    /**
     * Alias of {@link #show()} — see that method for full documentation.
     *
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #show()
     * @since 1.0.0
     */
    public void display() { show(); }

    // ============================================================== ask

    /**
     * Renders the menu and returns the user's choice as a zero-based
     * index — <b>without</b> running the corresponding action. The
     * caller decides what to do with the index. Requires
     * {@code Chirp.start()}.
     *
     * <pre>{@code
     * Menu m = new Menu("Pick")
     *     .add("Option A", () -> {})
     *     .add("Option B", () -> {})
     *     .add("Option C", () -> {});
     *
     * int choice = m.ask();
     * switch (choice) {
     *     case 0 -> handleA();
     *     case 1 -> handleB();
     *     case 2 -> handleC();
     * }
     * }</pre>
     *
     * @return the zero-based index of the choice
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #askLabel()
     * @see #choose()
     * @since 1.0.0
     */
    public int ask() {
        render();
        return Input.readInt("Your choice", 1, labels.size()) - 1;
    }

    /**
     * Alias of {@link #ask()} — see that method for full documentation
     * and examples.
     *
     * @return the zero-based index of the choice
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #ask()
     * @since 1.0.0
     */
    public int prompt() { return ask(); }

    /**
     * Alias of {@link #ask()} — see that method for full documentation
     * and examples.
     *
     * @return the zero-based index of the choice
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #ask()
     * @since 1.0.0
     */
    public int choose() { return ask(); }

    /**
     * Renders the menu and returns the selected option's label. Requires
     * {@code Chirp.start()}.
     *
     * @return the selected label
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #ask()
     * @see #getChoice()
     * @see #selectedLabel()
     * @since 1.0.0
     */
    public String askLabel() {
        return labels.get(ask());
    }

    /**
     * Alias of {@link #askLabel()} — see that method for full
     * documentation.
     *
     * @return the selected label
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #askLabel()
     * @since 1.0.0
     */
    public String getChoice() { return askLabel(); }

    /**
     * Alias of {@link #askLabel()} — see that method for full
     * documentation.
     *
     * @return the selected label
     * @throws chirp.ChirpNotStartedException if Chirp is not started
     * @see #askLabel()
     * @since 1.0.0
     */
    public String selectedLabel() { return askLabel(); }

    // ============================================================== internals

    private void render() {
        Console.blank();
        Console.title(title);
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label.equals("---")) {
                Console.println("  ----------");
            } else {
                Console.println("  " + (i + 1) + ") " + label);
            }
        }
        Console.line();
    }

    private void invoke(int index) {
        try {
            actions.get(index).run();
        } catch (RuntimeException e) {
            Messenger.error("Action failed: " + e.getMessage());
        }
    }

    /**
     * Returns the number of entries (including separators, headers, and
     * exit — they are numbered like any other entry).
     *
     * @return the entry count
     * @see #count()
     * @since 1.0.0
     */
    public int size() { return labels.size(); }

    /**
     * Alias of {@link #size()} — see that method for full documentation.
     *
     * @return the entry count
     * @see #size()
     * @since 1.0.0
     */
    public int count() { return size(); }
}
