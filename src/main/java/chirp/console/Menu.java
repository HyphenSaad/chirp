package chirp.console;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent interactive menu with separators, headers, and exit actions.
 */
public final class Menu {

    private final String title;
    private final List<String> labels = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private boolean hasExit;

    public Menu(String title) {
        this.title = title != null ? title : "Menu";
    }

    public static Menu create(String title) { return new Menu(title); }
    public static Menu of(String title) { return new Menu(title); }
    public static Menu named(String title) { return new Menu(title); }

    // ============================================================== add

    public Menu add(String label, Runnable action) {
        if (label == null || action == null) return this;
        labels.add(label);
        actions.add(action);
        return this;
    }
    public Menu item(String label, Runnable action) { return add(label, action); }
    public Menu option(String label, Runnable action) { return add(label, action); }
    public Menu withOption(String label, Runnable a) { return add(label, a); }

    // ============================================================== separators

    public Menu addSeparator() {
        labels.add("---");
        actions.add(() -> {});
        return this;
    }
    public Menu separator() { return addSeparator(); }
    public Menu divider() { return addSeparator(); }
    public Menu rule() { return addSeparator(); }

    // ============================================================== headers

    public Menu addHeader(String text) {
        labels.add("== " + text + " ==");
        actions.add(() -> {});
        return this;
    }
    public Menu header(String text) { return addHeader(text); }
    public Menu group(String text) { return addHeader(text); }

    // ============================================================== exit

    public Menu addExit(String label) {
        hasExit = true;
        labels.add(label != null ? label : "Exit");
        actions.add(() -> {});
        return this;
    }
    public Menu exit() { return addExit("Exit"); }
    public Menu withExit() { return addExit("Exit"); }
    public Menu exitWith(String l) { return addExit(l); }

    // ============================================================== run

    public void showOnce() {
        render();
        int choice = Input.readInt("Your choice", 1, labels.size());
        invoke(choice - 1);
    }
    public void displayOnce() { showOnce(); }

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
    public void run() { show(); }
    public void display() { show(); }

    // ============================================================== ask

    public int ask() {
        render();
        return Input.readInt("Your choice", 1, labels.size()) - 1;
    }
    public int prompt() { return ask(); }
    public int choose() { return ask(); }

    public String askLabel() {
        return labels.get(ask());
    }
    public String getChoice() { return askLabel(); }
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
        } catch (Exception e) {
            Messenger.error("Action failed: " + e.getMessage());
        }
    }

    public int size() { return labels.size(); }
    public int count() { return size(); }
}
