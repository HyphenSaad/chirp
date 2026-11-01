---
render_with_liquid: false
---

{% raw %}

# Console and Input

Printing text, formatting output, reading validated input.

---

## 1. Print a message

### Without Chirp

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, world");
    }
}
```

### With Chirp

```java
import static chirp.Chirp.*;

public class Main {
    public static void main(String[] args) {
        Chirp.start();
        println("Hello, world");
    }
}
```

The `Chirp.start()` line enables colours and the run summary. For pure
printing, `Chirp.start()` is optional — `println` works without it.

---

## 2. Semantic messages

Print success, error, warning, and info messages with automatic colour
and prefix.

### Without Chirp

```java
System.out.println("\u001B[32m[OK]\u001B[0m Saved");
System.out.println("\u001B[31m[ERROR]\u001B[0m Failed");
System.out.println("\u001B[33m[WARN]\u001B[0m Low memory");
System.out.println("\u001B[36m[INFO]\u001B[0m Loaded 42 items");
```

You memorised the ANSI codes, hand-prefixed every message, and the codes
show up as garbage on terminals that do not support ANSI.

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

success("Saved");
error("Failed");
warn("Low memory");
info("Loaded 42 items");
```

### Output

```
✔ Saved
✘ Failed
⚠ Low memory
ℹ Loaded 42 items
```

On terminals that do not support ANSI, Chirp automatically falls back to:

```
[OK] Saved
[ERROR] Failed
[WARN] Low memory
[INFO] Loaded 42 items
```

Same code, both outputs.

---

## 3. Separators and titles

### Without Chirp

```java
String bar = "=".repeat(60);
System.out.println(bar);
System.out.println("  My Application");
System.out.println(bar);

System.out.println("-".repeat(60));
```

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

title("My Application");              // cyan box
line();                                // 40-char default
line(60);                              // custom width
line(Separator.Style.DOUBLE);          // ══════
line(Separator.Style.DASHED);          // ╌╌╌╌╌╌
```

### Full separator control

```java
import chirp.console.Separator;

line(Separator.builder()
        .width(60)
        .style(Separator.Style.DOUBLE)
        .align(Separator.Align.CENTER)
        .label(" Section 2 ")
        .color(Separator.Color.CYAN)
        .build());
```

### Output

```
========================================
  My Application
========================================
========================================
════════════════════════════════════════
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
════════════════════════════════════════
     Section 2
════════════════════════════════════════
```

---

## 4. Tables

### Without Chirp

```java
String[][] rows = {
        {"1", "Saad", "95"},
        {"2", "Ali", "88"},
        {"3", "Chan", "92"},
};
String[] headers = {"ID", "Name", "Score"};

// Compute column widths
int[] widths = new int[headers.length];
for (int i = 0; i < headers.length; i++) widths[i] = headers[i].length();
for (String[] row : rows) {
    for (int i = 0; i < row.length; i++) {
        widths[i] = Math.max(widths[i], row[i].length());
    }
}

// Print header
for (int i = 0; i < headers.length; i++) {
    System.out.printf("%-" + (widths[i] + 2) + "s", headers[i]);
}
System.out.println();

// Separator
for (int w : widths) System.out.print("-".repeat(w + 2));
System.out.println();

// Print rows
for (String[] row : rows) {
    for (int i = 0; i < row.length; i++) {
        System.out.printf("%-" + (widths[i] + 2) + "s", row[i]);
    }
    System.out.println();
}
```

Every time you print a table, you write the same width-computing and
padding logic.

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

table(
        List.of(
                new String[]{"1", "Saad", "95"},
                new String[]{"2", "Ali", "88"},
                new String[]{"3", "Chan", "92"}),
        "ID", "Name", "Score");
```

### Output

```
ID    Name    Score
-----------------------
1     Saad    95
2     Ali     88
3     Chan    92
```

Column widths computed automatically.

---

## 5. Progress bars

### Without Chirp

```java
for (int i = 0; i <= 100; i++) {
    int filled = i / 5;
    String bar = "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + i + "%";
    System.out.print("\r" + bar);
    Thread.sleep(50);
}
System.out.println();
```

You have to compute the filled segment, handle carriage return, and
handle the terminal without support for `\r`.

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

for (int i = 0; i <= 100; i++) {
    progress(i, 100);
    sleep(50);
}
```

### Output (updating in place)

```
[##########----------] 50%
```

---

## 6. Immediate input

### Without Chirp — integer with retry and range

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
int age;
while (true) {
    System.out.print("Age: ");
    String line = sc.nextLine().trim();
    try {
        age = Integer.parseInt(line);
        if (age >= 1 && age <= 130) break;
        System.out.println("Out of range.");
    } catch (NumberFormatException e) {
        System.out.println("Not a number.");
    }
}
System.out.println("You are " + age);
```

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

int age = readInt("Age", 1, 130);
println("You are " + age);
```

The read **never throws** and **never skips lines**. On bad input, Chirp
re-prompts with a friendly message:

```
Age: abc
[ERROR] Please enter a whole number.
Age: 200
[ERROR] Please enter a value between 1 and 130.
Age: 25
You are 25
```

---

## 7. Fluent readers

When you need to configure the reader before reading, use the fluent form
(no argument).

### Without Chirp

```java
Scanner sc = new Scanner(System.in);
String name;
while (true) {
    System.out.print("Name (3-30 chars, non-empty): ");
    name = sc.nextLine().trim();
    if (name.isEmpty()) {
        System.out.println("Cannot be empty.");
        continue;
    }
    if (name.length() < 3 || name.length() > 30) {
        System.out.println("Must be 3-30 chars.");
        continue;
    }
    break;
}
```

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

String name = readString()
        .label("Name")
        .notBlank()
        .atLeast(3)
        .atMost(30)
        .skipFormatWarning()
        .read();

success("Hello, " + name + "!");
```

| Method                             | What it does                          |
| ---------------------------------- | ------------------------------------- |
| `.label("Name")`                   | Prompt text                           |
| `.notBlank()`                      | Reject empty/blank input              |
| `.atLeast(3)` / `.minLength(3)`    | Minimum length                        |
| `.atMost(30)` / `.maxLength(30)`   | Maximum length                        |
| `.email()` / `.phone()` / `.url()` | Format validation                     |
| `.regex(pattern)`                  | Custom regex                          |
| `.oneOf("a", "b", "c")`            | Allowed values                        |
| `.rule("PK_MOBILE")`               | Apply a registered validation rule    |
| `.help("Hint")`                    | Show a hint line before reading       |
| `.defaultValue("X")`               | Fallback if user presses Enter        |
| `.custom(predicate, msg)`          | Custom validation                     |
| `.read()`                          | Terminal — actually performs the read |

---

## 8. Aliases

Chirp does not force you to remember one specific name. Every method has
many aliases.

```java
// All of these do the same thing
int a = readInt("Age");
int b = readInteger("Age");
int c = takeInt("Age");
int d = getInt("Age");
int e = askInt("Age");
int f = inputInt("Age");
int g = promptInt("Age");
int h = scanInt("Age");

// All string reads
String s1 = readLine("Name");
String s2 = readString("Name");
String s3 = readText("Name");
String s4 = takeLine("Name");
String s5 = getString("Name");
String s6 = askString("Name");

// All yes/no reads
boolean y1 = readYesNo("Continue?");
boolean y2 = readConfirm("Continue?");
boolean y3 = confirm("Continue?");
boolean y4 = askConfirm("Continue?");
```

Pick whatever reads naturally to you.

---

## 9. Menus

### Without Chirp

```java
Scanner sc = new Scanner(System.in);
while (true) {
    System.out.println();
    System.out.println("=== Main Menu ===");
    System.out.println("1) Add item");
    System.out.println("2) List items");
    System.out.println("3) Exit");
    System.out.print("Choice: ");

    int choice;
    try {
        choice = Integer.parseInt(sc.nextLine().trim());
    } catch (NumberFormatException e) {
        System.out.println("Invalid choice.");
        continue;
    }

    switch (choice) {
        case 1: addItem();  break;
        case 2: listItems(); break;
        case 3: return;
        default: System.out.println("Invalid choice.");
    }
}
```

### With Chirp

```java
import chirp.console.Menu;

new Menu("Main Menu")
        .add("Add item",    () -> addItem())
        .add("List items",  () -> listItems())
        .separator()
        .add("Exit",        () -> System.exit(0))
        .show();
```

The menu handles: displaying, reading the choice, catching action
exceptions, re-displaying after each action, and honouring "Exit".

---

## 10. Loaders (spinners)

### Without Chirp

```java
// Manual spinner implementation
Thread spinner = new Thread(() -> {
    String[] frames = {"|", "/", "-", "\\"};
    int i = 0;
    while (!Thread.currentThread().isInterrupted()) {
        System.out.print("\r" + frames[i++ % 4]);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
    }
});
spinner.setDaemon(true);
spinner.start();

try {
    doWork();
} finally {
    spinner.interrupt();
    System.out.print("\r    \r");
}
```

### With Chirp

```java
import chirp.console.Loader;

Loader loader = Loader.start("Loading…");
try {
    doWork();
    loader.success("Done");
} catch (Exception e) {
    loader.error("Failed: " + e.getMessage());
}
```

Or with automatic cleanup:

```java
Loader.withLoader("Loading…", () -> doWork());
```

### Output while running

```
⠋ Loading…
```

### Output when done

```
✔ Done
```

---

## 11. Complete example — Profile printer

A small application that reads user info and prints it in a formatted card.

```java
import static chirp.Chirp.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Chirp.start();

        // Read validated input
        String name  = readString()
                .label("Full name")
                .notBlank()
                .atLeast(3)
                .atMost(100)
                .skipFormatWarning()
                .read();

        String email = readString()
                .label("Email")
                .email()
                .read();

        int age = readInt("Age", 1, 130);

        LocalDate dob = readDate("Date of birth");
        String bio = readString()
                .label("Short bio (optional)")
                .atMost(200)
                .skipBlankWarning()
                .skipFormatWarning()
                .defaultValue("")
                .read();

        // Print a formatted card
        println();
        line(60, Separator.Style.DOUBLE);
        println(bold("PROFILE CARD"));
        line(60, Separator.Style.DOUBLE);

        table(
                List.of(
                        new String[]{"Name",  name},
                        new String[]{"Email", email},
                        new String[]{"Age",   String.valueOf(age)},
                        new String[]{"DOB",   DateTime.humanize(dob)}),
                "Field", "Value");

        if (!bio.isEmpty()) {
            println();
            info("Bio: " + bio);
        }

        line(60, Separator.Style.DOUBLE);
        success("Profile complete for " + name);
    }
}
```

### Sample session

```
========================================
  C H I R P
  A friendly Java toolkit
  v1.0.0 | github.com/hyphensaad/chirp
========================================

Full name: Saad Mansoor
Email: saad@example.com
Age: 25
Date of birth: 2000-01-15
Short bio (optional): Software developer

════════════════════════════════════════════════════════════
PROFILE CARD
════════════════════════════════════════════════════════════
Field       Value
--------------------------------------------------------------
Name        Saad Mansoor
Email       saad@example.com
Age         25
DOB         Saturday, January 15, 2000

ℹ Bio: Software developer
════════════════════════════════════════════════════════════
✔ Profile complete for Saad Mansoor
```
{% endraw %}
