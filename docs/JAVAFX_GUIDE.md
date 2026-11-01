# Chirp JavaFX Guide

Complete guide to Chirp's JavaFX toolkit.

**Version**: 1.0.0 | **JavaFX**: 21 | **Java**: 17+

---

## Table of contents

- [Introduction](#introduction)
- [The Ui entry point](#the-ui-entry-point)
- [Theming](#theming)
- [Layout: Stack, Row, Card](#layout-stack-row-card)
- [Text: Label, Hyperlink](#text-label-hyperlink)
- [Buttons and actions](#buttons-and-actions)
- [Text inputs](#text-inputs)
- [Selection components](#selection-components)
- [Form validation](#form-validation)
- [Data: Table, ListView, TreeView](#data-table-listview-treeview)
- [Containers: TabPane, Accordion, SplitPane](#containers-tabpane-accordion-splitpane)
- [Charts](#charts)
- [Dialogs and menus](#dialogs-and-menus)
- [Progress and feedback](#progress-and-feedback)
- [Complete application](#complete-application)

---

## Introduction

Chirp's UI module provides **30+ pre-styled components** and **12 layout
utilities** on top of JavaFX, with three goals:

1. **Friendly defaults** — every component looks right out of the box
2. **Built-in theming** — light/dark with one line of code
3. **Validation integration** — fields know how to validate themselves

You still write JavaFX applications. You just write less of them.

### Before and after

**Without Chirp** — a sign-in form requires 40+ lines:

```java
public class Main extends Application {
    @Override
    public void start(Stage stage) {
        TextField email = new TextField();
        email.setPromptText("Email");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red;");
        emailError.setVisible(false);

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Password");

        Button submit = new Button("Sign in");
        submit.setOnAction(e -> {
            if (!email.getText().matches("...")) {
                emailError.setText("Invalid email");
                emailError.setVisible(true);
                return;
            }
            // ...
        });

        VBox root = new VBox(16,
                new Label("Sign in"),
                new VBox(4, email, emailError),
                pwd,
                submit);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add("/styles.css");
        stage.setScene(scene);
        stage.show();
    }
}
```

**With Chirp** — 15 lines:

```java
public class Main {
    public static void main(String[] args) {
        Ui.app(() -> {
            Field email = Field.of("Email").email();
            Field pwd = Field.password("Password").required();

            Button submit = Button.primary("Sign in")
                    .onClick(() -> {
                        if (email.isValid() && pwd.isValid()) {
                            // sign in
                        }
                    });

            Ui.show("Sign In", Stack.v(16).padding(24).centered()
                    .add(Label.heading("Sign in"))
                    .add(email)
                    .add(pwd)
                    .add(submit));
        });
    }
}
```

Validation, error display, theming, layout — all automatic.

---

## The Ui entry point

Every Chirp UI application starts with `Ui.app(...)`.

### Signature

```java
public static void app(Runnable entry);
```

The `entry` runs on the JavaFX application thread after the toolkit is
initialized.

### Basic usage

```java
import chirp.ui.*;

public class Main {
    public static void main(String[] args) {
        Ui.app(() -> {
            Ui.show("Hello", Stack.v(16).padding(24).centered()
                    .add(Label.heading("Hello, world!")));
        });
    }
}
```

### What `Ui.app` does

1. Starts the JavaFX toolkit (once per JVM)
2. Sets anti-aliasing hints
3. Runs your `entry` on the FX thread
4. Keeps the JVM alive until all windows close

### Show a window

```java
Ui.show(String title, Parent root);
```

Or with `UiApp`:

```java
UiApp app = UiApp.builder()
        .title("My App")
        .size(1024, 768)
        .theme(UiTheme.DARK)
        .content(root)
        .build();

Ui.show(app);
```

### UiApp builder

| Method                                                 | Description                | Default     |
| ------------------------------------------------------ | -------------------------- | ----------- |
| `.title(String)`                                       | Window title               | `"Chirp"`   |
| `.size(int w, int h)`                                  | Window size                | `900 × 600` |
| `.width(int)` / `.height(int)`                         | Individual dimensions      | —           |
| `.theme(UiTheme)` / `.dark()` / `.light()` / `.auto()` | Theme                      | `AUTO`      |
| `.resizable(boolean)` / `.fixedSize()`                 | Resizable                  | `true`      |
| `.scroll(boolean)` / `.noScroll()`                     | Wrap content in ScrollPane | `true`      |
| `.content(Parent)` / `.body(Parent)`                   | Root content               | —           |
| `.content(Consumer<Stack>)`                            | Content builder            | —           |
| `.onClose(Runnable)`                                   | Close hook                 | —           |
| `.onShow(Runnable)`                                    | Show hook                  | —           |

### Key-value construction

```java
UiApp app = UiApp.from(
        UiApp.Key.TITLE, "My App",
        UiApp.Key.WIDTH, 1024,
        UiApp.Key.HEIGHT, 768,
        UiApp.Key.DARK, true);
```

---

## Theming

### Built-in themes

```java
Ui.app(() -> {
    Ui.theme(UiTheme.DARK);        // force dark
    // or Ui.theme(UiTheme.LIGHT); // force light
    // or Ui.theme(UiTheme.AUTO);  // follow OS
    Ui.show("App", root);
});
```

`AUTO` resolves at `Ui.show()` time. Override with:

```bash
java -Dchirp.theme=dark -cp ... Main
```

### Global style

Adjust font and accent colour globally:

```java
UiStyle.apply(
        UiStyle.Key.FONT_SIZE, 16,
        UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");
```

### Presets

```java
UiStyle.compact();          // smaller fonts
UiStyle.largeText();        // larger fonts (16 px base)
UiStyle.modernLight();      // 15 px, purple accent
UiStyle.modernDark();       // 15 px, bright accent
UiStyle.monospace();        // Consolas, teal accent
```

### Custom CSS

The themes use these CSS classes:

**Layout**

- `.card`, `.card-header`, `.card-body`, `.card-footer`
- `.badge`, `.chip`, `.breadcrumb`, `.breadcrumb-separator`
- `.rating-star`

**Form**

- `.field-label`, `.field-error`, `.field-hint`, `.field-counter`
- `.password-toggle`

**Semantic**

- `.primary`, `.secondary`, `.success`, `.warning`, `.danger`, `.info`
- `.outline`, `.ghost`, `.link`
- `.invalid` (validation error state)

Load your own stylesheet after `Ui.show`:

```java
Ui.show("App", root);
Scene scene = Stage.getWindows().get(0).getScene();
scene.getStylesheets().add("custom.css");
```

```css
/* custom.css — override Chirp defaults */
.root {
  -fx-base: #1a1a1a;
  -fx-accent: #ff6b6b;
}

.button.primary {
  -fx-background-color: #ff6b6b;
}
```

---

## Layout: Stack, Row, Card

### Stack — vertical layout

```java
Stack.v(16).padding(24).centered()
        .add(Label.heading("Title"))
        .add(Button.primary("OK"));
```

| Method                                      | Description           |
| ------------------------------------------- | --------------------- |
| `Stack.v()` / `.v(double gap)`              | Vertical with gap     |
| `Stack.vertical()`, `.column()`, `.stack()` | Aliases               |
| `.gap(double)` / `.spacing(double)`         | Set gap               |
| `.padding(double)`                          | Uniform padding       |
| `.padding(h, v)` / `.padding(t, r, b, l)`   | Custom padding        |
| `.align(Pos)` / `.centered()`               | Alignment             |
| `.add(Node...)`                             | Add children          |
| `.grow(Node)`                               | Expand child to fill  |
| `.fullWidth()`                              | Fill horizontal space |

### Row — horizontal layout

```java
Row.h(8).padding(16).centered()
        .add(Label.of("Name:"))
        .add(TextField.of())
        .add(Button.primary("Search"));
```

Same API as `Stack`, just horizontal.

### Card — container with title, body, footer

```java
Card.of()
        .title("Welcome back")
        .subtitle("Sign in to continue")
        .body(
                Field.of("Email").email(),
                Field.password("Password").required())
        .footer(
                Button.primary("Sign in"),
                Button.ghost("Cancel"))
        .fullWidth();
```

Empty sections are automatically hidden — no blank gaps.

| Method                                   | Description           |
| ---------------------------------------- | --------------------- |
| `.title(String)` / `.heading(String)`    | Header title          |
| `.subtitle(String)`                      | Header subtitle       |
| `.body(Node...)` / `.content(Node...)`   | Body content          |
| `.footer(Node...)` / `.actions(Node...)` | Footer actions        |
| `.fullWidth()`                           | Fill horizontal space |

---

## Text: Label, Hyperlink

### Label

```java
Label.of("Plain text");
Label.heading("Large heading");
Label.subheading("Medium heading");
Label.muted("Subtle text");
Label.error("Error message");
Label.success("Success message");
```

| Method                  | Description           |
| ----------------------- | --------------------- |
| `.text(String)`         | Set text              |
| `.variant(String)`      | Apply a variant class |
| `.bold()` / `.italic()` | Bold / italic         |
| `.wrap()`               | Enable text wrapping  |
| `.fontSize(double)`     | Custom font size      |
| `.color(String)`        | Custom colour         |

### Hyperlink

```java
// Simple
Hyperlink.of("Click me");

// Open URL in browser
Hyperlink.to("Visit GitHub", "https://github.com/hyphensaad/chirp");

// Custom action
Hyperlink.of("Custom").onClick(() -> println("clicked"));
```

---

## Buttons and actions

### Button factories

```java
Button.primary("Save");         // filled, accent colour
Button.secondary("Cancel");     // filled, muted
Button.success("Confirm");      // green
Button.warning("Caution");      // yellow
Button.danger("Delete");        // red
Button.info("Info");            // blue
Button.outline("Border");       // bordered, transparent
Button.ghost("Invisible");      // transparent
Button.link("Link");            // text-only
```

### Chaining

```java
Button.primary("Save")
        .onClick(() -> save())
        .fullWidth()
        .disableIf(!canSave());
```

### Disable / enable

```java
Button b = Button.primary("Submit");
b.disable();         // disabled
b.enable();          // enabled
b.disabled(true);    // same as disable
b.enabled(true);     // same as enable
b.isDisabled();      // current state
```

### ToggleButton — sticky pressed state

```java
ToggleButton bold = ToggleButton.primary("Bold");
bold.onClick(() -> applyBold());

// Or as radio group (only one selected)
ToggleGroup group = new ToggleGroup();
ToggleButton b1 = ToggleButton.primary("A").group(group);
ToggleButton b2 = ToggleButton.primary("B").group(group);
```

### MenuButton — dropdown

```java
MenuButton.primary("Actions")
        .item("Save", () -> save())
        .item("Save as", () -> saveAs())
        .separator()
        .itemDanger("Delete", () -> delete());
```

### SplitMenuButton — action + dropdown

```java
SplitMenuButton.primary("Save")
        .onClick(() -> save())
        .item("Save as", () -> saveAs())
        .item("Save all", () -> saveAll());
```

---

## Text inputs

### TextField — single-line

```java
TextField.of()
        .placeholder("Enter name")
        .required()
        .minLength(3)
        .maxLength(50)
        .fullWidth();
```

### PasswordField — masked

```java
PasswordField.of()
        .placeholder("Password")
        .required()
        .minLength(8);
```

For a **show/hide toggle**, use `Field.password(...)`:

```java
Field pwd = Field.password("Password").required().minLength(8);
```

### TextArea — multi-line

```java
TextArea.of()
        .placeholder("Enter description")
        .rows(5)
        .required()
        .onSubmit(() -> save());       // Ctrl+Enter
```

### Field — composite field (recommended)

`Field` combines label + input + error + hint + counter in one component.

```java
Field name = Field.of("Name").required().minLength(2).maxLength(50);
Field email = Field.of("Email").email();
Field pwd = Field.password("Password").required().minLength(8);
Field bio = Field.textarea("Bio").rows(4);
```

| Method                              | Description                     |
| ----------------------------------- | ------------------------------- |
| `.required()`                       | Non-blank validation            |
| `.minLength(int)` / `.atLeast(int)` | Minimum length                  |
| `.maxLength(int)` / `.atMost(int)`  | Maximum length                  |
| `.email()`                          | Email validation                |
| `.phone()`                          | Phone validation                |
| `.url()`                            | URL validation                  |
| `.numeric()`                        | Numeric validation              |
| `.validate(Predicate, message)`     | Custom validator                |
| `.rule(Rule)` / `.rule(name)`       | Apply a validation rule         |
| `.placeholder(String)`              | Input placeholder               |
| `.hint(String)`                     | Hint text below field           |
| `.counter(boolean)`                 | Show character counter          |
| `.fullWidth()`                      | Fill horizontal space           |
| `.value()`                          | Get current value               |
| `.value(String)`                    | Set current value               |
| `.isValid()`                        | Current validation state        |
| `.error()`                          | Current error message (or null) |

---

## Selection components

### Checkbox

```java
Checkbox.of("Remember me").onChange(checked -> println(checked));
Checkbox.of("I agree").required();
```

### RadioGroup

```java
RadioGroup.of("Small", "Medium", "Large")
        .selected("Medium")
        .onChange(size -> println("Selected: " + size));
```

### Select — dropdown

```java
Select.of("Option A", "Option B", "Option C")
        .selected("Option A")
        .onChange(value -> println("Selected: " + value));

// Searchable
Select.of("Pakistan", "India", "Bangladesh", "Nepal")
        .searchable()
        .onChange(country -> println("Country: " + country));
```

### Slider

```java
Slider.of(0, 100, 50)
        .showTicks(true)
        .onChange(value -> println("Value: " + value))
        .fullWidth();
```

### Spinner — numeric stepper

```java
Spinner.of(1, 120, 25)                 // min, max, initial
        .onChange(value -> println("Age: " + value));
```

### DatePicker

```java
DatePicker.of()
        .value(LocalDate.now())
        .placeholder("Select date")
        .onChange(date -> println("Date: " + date));
```

### TimePicker

```java
TimePicker.of()                       // 24-hour
        .value(LocalTime.of(14, 30))
        .onChange(time -> println("Time: " + time));

TimePicker.of12Hour()                 // 12-hour with AM/PM
        .value(LocalTime.of(14, 30));
```

### ColorPicker

```java
ColorPicker.of(Color.web("#6366f1"))
        .onChange(color -> println("Color: " + ColorPicker.toHex(color)));
```

### FileField — file picker

```java
FileField.of()
        .title("Choose file")
        .imagesOnly()
        .onChange(file -> println("Selected: " + file.getAbsolutePath()));
```

---

## Form validation

### FormBuilder — declarative forms

```java
Stack form = FormBuilder.create("Register")
        .field("name", Field.of("Name").required().minLength(2))
        .field("email", Field.of("Email").email())
        .field("password", Field.password("Password").required().minLength(8))
        .submit("Register", values -> {
            Ui.info("Success", "Welcome, " + values.get("name") + "!");
        })
        .reset("Clear")
        .build();

Ui.app(() -> Ui.show("Registration", Stack.v(16).padding(24).add(form)));
```

`FormBuilder`:

- Runs **all** field validators when submit is clicked
- Shows all errors simultaneously
- Calls the submit callback only if all fields are valid
- Provides `values()` map with all field values
- Provides `validate()` returning a `ValidationResult`

### UiIntegration — bridge to Rules

```java
// Bind a Field to a registered rule
Field cnic = UiIntegration.bindRule(Field.of("CNIC"), "PK_CNIC");

// Bind a Field to a Rule object
Rule emailRule = Rules.email();
Field email = UiIntegration.bindRule(Field.of("Email"), emailRule);

// Create a Field directly with a rule
Field phone = UiIntegration.ruleField("Phone", "PK_MOBILE");

// Create a Field with an inline check
Field custom = UiIntegration.checkField(
        "Score",
        s -> s.matches("\\d+") && Integer.parseInt(s) <= 100,
        "Score must be 0-100");

// Bind a TextField (not a Field) to a check
TextField f = UiIntegration.bindCheck(
        TextField.of(), Check::isEmail, "Invalid email");

// Convert a ValidationResult to visible error labels
ValidationResult r = form.validate();
if (r.hasErrors()) {
    Stack errors = UiIntegration.errorsFrom(r);
    // add errors to your scene
}

// Or a one-line summary
Label summary = UiIntegration.summaryOf(r);
```

---

## Data: Table, ListView, TreeView

### Table

```java
Table table = Table.of("ID", "Name", "Email", "Score")
        .row(1, "Saad", "saad@example.com", 95)
        .row(2, "Ali", "ali@example.com", 88)
        .row(3, "Chan", "chan@example.com", 92)
        .rowHeight(32)
        .height(32 * 3 + 30)     // 3 rows + header
        .striped()
        .fullWidth();
```

| Method                           | Description             |
| -------------------------------- | ----------------------- |
| `Table.of(String... columns)`    | Create with columns     |
| `.row(Object...)`                | Add a row               |
| `.rows(List<? extends List<?>>)` | Add multiple rows       |
| `.clearAll()`                    | Clear all rows          |
| `.rowHeight(double)`             | Cell height in pixels   |
| `.height(double)`                | Total table height      |
| `.striped()`                     | Alternating row colours |
| `.fullWidth()`                   | Fill horizontal space   |
| `.selectedRow()`                 | Currently selected row  |
| `.rowCount()`                    | Number of rows          |

### ListView

```java
ListView.of("Alpha", "Beta", "Gamma", "Delta")
        .height(200)
        .onChange(item -> println("Selected: " + item))
        .fullWidth();
```

### TreeView — hierarchical data

```java
TreeView.of("Categories")
        .add("Fruits", "Apple")
        .add("Fruits", "Banana")
        .add("Fruits", "Cherry")
        .add("Vegetables", "Carrot")
        .add("Vegetables", "Broccoli")
        .add("Grains", "Rice")
        .add("Grains", "Wheat")
        .expandAll()
        .height(300)
        .onChange(item -> println("Selected: " + item));
```

### TreeTableView — hierarchical + tabular

```java
TreeTableView.of("Company")
        .add("Engineering", "Alice", "Backend")
        .add("Engineering", "Bob", "Frontend")
        .add("Sales", "Carol", "North")
        .add("Sales", "Dave", "South")
        .expandAll()
        .height(300);
```

---

## Containers: TabPane, Accordion, SplitPane

### TabPane

```java
TabPane.of()
        .add("Profile", Stack.v(16).padding(16)
                .add(Field.of("Name").required())
                .add(Field.of("Email").email()))
        .add("Settings", Stack.v(16).padding(16)
                .add(Checkbox.of("Enable notifications"))
                .add(Checkbox.of("Dark mode")))
        .add("About", Stack.v(16).padding(16)
                .add(Label.heading("My App"))
                .add(Label.muted("Version 1.0.0")));
```

### Accordion — collapsible sections

```java
Accordion.of()
        .add("General Settings",
                Stack.v(8).padding(12)
                        .add(Checkbox.of("Enable auto-save"))
                        .add(Checkbox.of("Show tooltips")))
        .add("Advanced",
                Stack.v(8).padding(12)
                        .add(Field.of("Cache size"))
                        .add(Field.of("Log level")))
        .fullWidth();
```

### TitledPane — single collapsible

```java
TitledPane.of("Advanced options",
                Stack.v(8).padding(12)
                        .add(Field.of("Max threads"))
                        .add(Field.of("Timeout (ms)")))
        .expanded(false)
        .fullWidth();
```

### SplitPane — resizable panes

```java
SplitPane.horizontal()
        .add(
                Stack.v(8).padding(16)
                        .add(Label.subheading("Files"))
                        .add(ListView.of("a.txt", "b.txt", "c.txt")),
                Stack.v(8).padding(16)
                        .add(Label.heading("Preview"))
                        .add(TextArea.of().rows(20)))
        .dividerPosition(0.3)
        .height(400)
        .fullWidth();
```

---

## Charts

All charts share the same fluent API.

### PieChart

```java
PieChart.of()
        .add("Java", 45)
        .add("Python", 30)
        .add("JavaScript", 20)
        .add("Other", 5)
        .title("Language usage")
        .height(300);
```

### BarChart

```java
BarChart.of()
        .add("Q1", 120)
        .add("Q2", 180)
        .add("Q3", 150)
        .add("Q4", 210)
        .xLabel("Quarter")
        .yLabel("Revenue")
        .title("2026 Revenue")
        .height(300);
```

### LineChart

```java
LineChart.of()
        .add("Jan", 120)
        .add("Feb", 135)
        .add("Mar", 150)
        .add("Apr", 145)
        .add("May", 180)
        .xLabel("Month")
        .yLabel("Sales")
        .title("Monthly Sales")
        .height(300);
```

### AreaChart — filled area under line

```java
AreaChart.of()
        .add("Week 1", 45)
        .add("Week 2", 62)
        .add("Week 3", 78)
        .add("Week 4", 95)
        .title("Growth")
        .height(300);
```

### ScatterChart — numeric x/y

```java
ScatterChart.of()
        .add(1.0, 2.3)
        .add(2.0, 4.5)
        .add(3.0, 3.8)
        .add(4.0, 5.2)
        .add(5.0, 6.1)
        .xLabel("X")
        .yLabel("Y")
        .height(300);
```

### StackedBarChart — multiple series

```java
StackedBarChart chart = StackedBarChart.of()
        .title("Yearly Comparison");
chart.series("2024")
        .add("Q1", 120)
        .add("Q2", 135)
        .add("Q3", 150)
        .add("Q4", 140);
chart.series("2025")
        .add("Q1", 140)
        .add("Q2", 160)
        .add("Q3", 175)
        .add("Q4", 190);
chart.height(300);
```

---

## Dialogs and menus

### Dialogs

```java
// Information
Ui.info("Success", "Saved successfully");

// Warning
Ui.warning("Low memory", "Consider closing unused tabs");

// Error
Ui.error("Failed", "Database unreachable");

// Success
Ui.success("Done", "All files exported");

// Confirmation — returns boolean
if (Ui.confirm("Delete", "This action cannot be undone. Continue?")) {
    deleteItem();
}
```

### MenuBar

```java
MenuBar menuBar = MenuBar.of()
        .menu("File",
                MenuBar.item("New", () -> newFile()),
                MenuBar.item("Open…", () -> openFile()),
                MenuBar.item("Save", () -> save()),
                MenuBar.separator(),
                MenuBar.item("Exit", () -> System.exit(0)))
        .menu("Edit",
                MenuBar.item("Undo", () -> undo()),
                MenuBar.item("Redo", () -> redo()),
                MenuBar.separator(),
                MenuBar.item("Cut", () -> cut()),
                MenuBar.item("Copy", () -> copy()),
                MenuBar.item("Paste", () -> paste()))
        .menu("Help",
                MenuBar.item("Documentation", () -> openDocs()),
                MenuBar.item("About", () -> showAbout()));
```

### ToolBar

```java
ToolBar.of()
        .add(
                Button.primary("Save").onClick(() -> save()),
                Button.secondary("Undo").onClick(() -> undo()),
                Button.secondary("Redo").onClick(() -> redo()),
                ToolBar.separator(),
                Button.primary("Delete").onClick(() -> delete()));
```

### ContextMenu — right-click menu

```java
ContextMenu menu = ContextMenu.of()
        .item("Cut", () -> cut())
        .item("Copy", () -> copy())
        .item("Paste", () -> paste())
        .separator()
        .itemDanger("Delete", () -> delete());

// Attach to a node
menu.attachTo(someNode);
```

### Tooltip

```java
Tooltip.of("Delete this item")
        .wrap()
        .showDelay(200)
        .attachTo(deleteButton);
```

---

## Progress and feedback

### ProgressBar

```java
// Determinate — 65% filled
ProgressBar.of(0.65).fullWidth();

// Indeterminate — animated
ProgressBar.indeterminate().fullWidth();

// Update at runtime
ProgressBar bar = ProgressBar.of();
bar.value(0.4);              // 40%
bar.percent(75);             // 75%
bar.value(-1);               // → indeterminate
```

### ProgressIndicator — circular

```java
ProgressIndicator.of(0.65).size(56);         // 65% ring
ProgressIndicator.indeterminate().size(48);  // spinner
```

### Loader — background loading overlay

```java
Loader loader = Loader.start("Loading data…");
Ui.runAsync(
        () -> fetchData(),
        () -> loader.stop());
```

Or with success/error:

```java
Loader loader = Loader.start("Saving…");
try {
    save();
    loader.success("Saved");
} catch (Exception e) {
    loader.error("Failed: " + e.getMessage());
}
```

### Badge — small status indicator

```java
Badge.primary("New");
Badge.success("Active");
Badge.warning("Pending");
Badge.danger("Failed");
Badge.info("Info");
Badge.outline("Draft");
```

### Chip — removable tag

```java
Chip chip = Chip.of("Java")
        .onRemove(() -> println("removed"));
```

### Rating — star rating

```java
Rating.of()
        .value(3)
        .onChange(rating -> println("Rated: " + rating));
```

### Breadcrumb

```java
Breadcrumb.of()
        .add("Home")
        .add("Products")
        .add("Laptops")
        .onNavigate(segment -> println("Navigate to " + segment));
```

---

## Complete application

A fully-working task manager with all the pieces.

### Structure

```
TaskManager/
├── src/
│   ├── Main.java
│   ├── Task.java
│   ├── TaskRepository.java
│   └── ui/
│       └── TaskView.java
└── lib/
    └── chirp-1.0.0.jar
```

### `Task.java`

```java
package app;

import java.time.LocalDateTime;

public class Task {
    private final long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;

    public Task(long id, String title, String description) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.description = description == null ? "" : description;
        this.createdAt = LocalDateTime.now();
    }

    public long getId()              { return id; }
    public String getTitle()         { return title; }
    public void setTitle(String t)   { this.title = t; }
    public String getDescription()   { return description; }
    public void setDescription(String d) { this.description = d; }
    public boolean isCompleted()     { return completed; }
    public void setCompleted(boolean c) { this.completed = c; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return (completed ? "✓ " : "○ ") + title;
    }
}
```

### `TaskRepository.java`

```java
package app;

import chirp.io.FileIO;
import chirp.io.JsonFile;
import chirp.json.JsonArray;
import chirp.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final String path;
    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 1;

    public TaskRepository(String path) {
        this.path = path;
        load();
    }

    public Task create(String title, String description) {
        Task task = new Task(nextId++, title, description);
        tasks.add(task);
        save();
        return task;
    }

    public void update(Task task) {
        save();
    }

    public void delete(Task task) {
        tasks.remove(task);
        save();
    }

    public List<Task> all() {
        return new ArrayList<>(tasks);
    }

    public List<Task> pending() {
        return tasks.stream().filter(t -> !t.isCompleted()).toList();
    }

    public List<Task> completed() {
        return tasks.stream().filter(Task::isCompleted).toList();
    }

    private void load() {
        if (!FileIO.exists(path)) return;

        JsonValue root = JsonFile.read(path);
        if (root == null || !root.isArray()) return;

        for (JsonValue item : root.asArray().asList()) {
            if (!item.isObject()) continue;
            JsonObject o = item.asObject();
            Task task = new Task(
                    o.getLong("id", 0),
                    o.getString("title", ""),
                    o.getString("description", ""));
            task.setCompleted(o.getBoolean("completed", false));
            tasks.add(task);
            if (task.getId() >= nextId) nextId = task.getId() + 1;
        }
    }

    private void save() {
        JsonArray array = new JsonArray();
        for (Task task : tasks) {
            array.add(new JsonObject()
                    .put("id", task.getId())
                    .put("title", task.getTitle())
                    .put("description", task.getDescription())
                    .put("completed", task.isCompleted()));
        }
        JsonFile.writePretty(path, array);
    }
}
```

### `TaskView.java`

```java
package app.ui;

import app.Task;
import app.TaskRepository;
import chirp.ui.*;
import chirp.ui.Label;

import java.util.List;

public class TaskView {

    private final TaskRepository repo;
    private final Table table = Table.of("Status", "Title", "Created").rowHeight(32);
    private final TextField searchField;

    public TaskView(TaskRepository repo) {
        this.repo = repo;
        this.searchField = TextField.of()
                .placeholder("Search tasks…")
                .onChange(this::refreshTable);
        searchField.setPrefWidth(400);
    }

    public Stack build() {
        refreshTable();

        Button addButton = Button.primary("+ New task")
                .onClick(this::showAddDialog);

        Button toggleButton = Button.secondary("Mark complete")
                .onClick(this::toggleSelected);

        Button deleteButton = Button.danger("Delete")
                .onClick(this::deleteSelected);

        Card card = Card.of()
                .title("Your tasks")
                .subtitle("Click a row to select, then use the buttons below.")
                .body(table)
                .footer(addButton, toggleButton, deleteButton)
                .fullWidth();

        return Stack.v(16).padding(24).fullWidth()
                .add(Label.heading("Task Manager"))
                .add(searchField)
                .add(card);
    }

    private void refreshTable() {
        table.clearAll();
        String query = searchField.getText();
        List<Task> tasks = query.isBlank() ? repo.all() : filterByQuery(query);
        for (Task task : tasks) {
            table.row(
                    task.isCompleted() ? "✓" : "○",
                    task.getTitle(),
                    task.getCreatedAt().toLocalDate().toString());
        }
    }

    private List<Task> filterByQuery(String query) {
        return repo.all().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    private void showAddDialog() {
        Field title = Field.of("Title").required().minLength(2);
        Field description = Field.textarea("Description").rows(3);

        DialogResult result = Dialog.of("New Task")
                .add(title, description)
                .confirm("Create", "Cancel")
                .show();

        if (result.isConfirmed() && title.isValid()) {
            repo.create(title.value(), description.value());
            refreshTable();
            Ui.info("Created", "Task added successfully.");
        }
    }

    private void toggleSelected() {
        List<String> selected = table.selectedRow();
        if (selected == null || selected.isEmpty()) {
            Ui.warning("No selection", "Please select a task first.");
            return;
        }

        String title = selected.get(1);
        repo.all().stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst()
                .ifPresent(t -> {
                    t.setCompleted(!t.isCompleted());
                    repo.update(t);
                    refreshTable();
                });
    }

    private void deleteSelected() {
        List<String> selected = table.selectedRow();
        if (selected == null || selected.isEmpty()) {
            Ui.warning("No selection", "Please select a task first.");
            return;
        }

        String title = selected.get(1);
        if (!Ui.confirm("Delete", "Delete '" + title + "'?")) return;

        repo.all().stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst()
                .ifPresent(t -> {
                    repo.delete(t);
                    refreshTable();
                });
    }
}
```

### `Main.java`

```java
package app;

import app.ui.TaskView;
import chirp.ui.Ui;
import chirp.ui.UiApp;
import chirp.ui.UiTheme;

public class Main {
    public static void main(String[] args) {
        TaskRepository repo = new TaskRepository("tasks.json");
        TaskView view = new TaskView(repo);

        Ui.app(() -> Ui.show(
                UiApp.builder()
                        .title("Task Manager")
                        .size(900, 700)
                        .theme(UiTheme.AUTO)
                        .content(view.build())
                        .build()));
    }
}
```

### What the app demonstrates

- **Chirp UI components** — Label, TextField, Button, Card, Table, Field
- **FileIO and JSON** — persist tasks to `tasks.json`
- **UiApp** — window with title, size, theme
- **Card + Stack + Table layout** — professional-looking UI without CSS
- **Ui.info / Ui.warning / Ui.confirm** — dialogs with one line
- **Field validation** — title field is required and must be at least 2 chars
- **Search filtering** — type in the search field, table updates live

### Build and run

```bash
# Compile
javac -cp lib/chirp-1.0.0.jar -d out \
      src/Main.java \
      src/Task.java \
      src/TaskRepository.java \
      src/ui/TaskView.java

# Run (Linux/macOS)
java -cp "lib/chirp-1.0.0.jar:out" app.Main

# Run (Windows)
java -cp "lib\chirp-1.0.0.jar;out" app.Main
```

The first run creates an empty `tasks.json` and shows the empty table. Add
tasks through the "+ New task" button. Close and reopen the app — tasks
persist.

---

## Complete component catalog

| Category       | Components                                                                                                      |
| -------------- | --------------------------------------------------------------------------------------------------------------- |
| **Layout**     | `Stack`, `Row`, `Card`                                                                                          |
| **Text**       | `Label`, `Hyperlink`                                                                                            |
| **Buttons**    | `Button`, `ToggleButton`, `MenuButton`, `SplitMenuButton`                                                       |
| **Input**      | `TextField`, `PasswordField`, `TextArea`, `Field`                                                               |
| **Selection**  | `Checkbox`, `RadioGroup`, `Select`, `Slider`, `Spinner`, `DatePicker`, `TimePicker`, `ColorPicker`, `FileField` |
| **Data**       | `Table`, `ListView`, `TreeView`, `TreeTableView`                                                                |
| **Containers** | `TabPane`, `Accordion`, `TitledPane`, `SplitPane`, `Pagination`                                                 |
| **Charts**     | `PieChart`, `BarChart`, `LineChart`, `AreaChart`, `ScatterChart`, `StackedBarChart`                             |
| **Navigation** | `MenuBar`, `ToolBar`, `ContextMenu`, `Breadcrumb`                                                               |
| **Feedback**   | `ProgressBar`, `ProgressIndicator`, `Loader`, `Badge`, `Chip`, `Rating`, `Tooltip`                              |
| **Forms**      | `FormBuilder`, `UiIntegration`, `Validate` (via `chirp.validation`)                                             |
| **Display**    | `ImageView`, `Separator`                                                                                        |
| **Advanced**   | `KeyBindings`, `UiStyle`, `UiTheme`                                                                             |

Every component is available via `chirp.ui.*`. Every component supports
fluent construction. Every component uses the active theme.
