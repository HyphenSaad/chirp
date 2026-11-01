# Chirp Starter

A ready-to-open project that gets you from zero to running Java code with
[Chirp](https://github.com/hyphensaad/chirp) in under five minutes.

---

## What's inside

```
.
├── .vscode/
│   ├── settings.json          ← classpath configuration
│   └── extensions.json        ← recommended VS Code extensions
├── lib/
│   └── README.md              ← where to put chirp-1.0.0.jar
└── src/
    └── Main.java              ← a working demo that showcases Chirp
```

No Maven. No Gradle. Just Java + a JAR.

---

## Quick start

### Step 1 — Download the JAR

Go to the [latest release](https://github.com/hyphensaad/chirp/releases/latest)
and download `chirp-1.0.0.jar`.

### Step 2 — Place it in `lib/`

```
lib/
└── chirp-1.0.0.jar
```

### Step 3 — Open this folder in VS Code

**File → Open Folder** → select the folder containing this README.

If you don't have VS Code, install it from
[code.visualstudio.com](https://code.visualstudio.com).

### Step 4 — Install the Java extension

When VS Code opens, it will recommend **Extension Pack for Java**. Click
**Install**.

Or install manually from the Extensions panel: search for "Extension Pack
for Java" by Microsoft.

### Step 5 — Run

Open `src/Main.java` and either:

- Press **F5**, or
- Click the **Run** button above `public static void main`, or
- Open a terminal and run:

  ```bash
  javac -cp lib/chirp-1.0.0.jar -d out src/Main.java
  java -cp "lib/chirp-1.0.0.jar:out" Main
  ```

You should see the Chirp banner, a demo of array helpers, formatting, and
date utilities.

---

## What the demo shows

`src/Main.java` demonstrates four Chirp features that would each require
significantly more code in standard Java:

| Feature                                   | Standard Java                                | With Chirp                                                          |
| ----------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------- |
| Array aggregate (sum, max, reverse, sort) | 4 separate loops                             | `sum(arr)`, `max(arr)`, `reverse(arr)`, `sorted(arr)`               |
| Currency / ordinal / plural formatting    | `String.format("%,.2f", ...)` + custom logic | `Format.currency(...)`, `Format.ordinal(...)`, `Format.plural(...)` |
| Human-readable dates                      | `DateTimeFormatter.ofPattern(...)` setup     | `DateTime.humanize(...)`, `DateTime.timeAgo(...)`                   |
| Safe file read                            | `try/catch (IOException)` at every call site | `FileIO.exists(...)` — never throws                                 |

Edit `Main.java` to explore further. Change the array, call new methods,
try new formats.

---

## Using a different IDE

### IntelliJ IDEA

1. **File → Open** → select this folder.
2. Right-click the `lib/` folder → **Add as Library**.
3. Open `src/Main.java` and press **Shift+F10**.

### Eclipse

1. **File → Import → Existing Projects into Workspace**.
2. Right-click the project → **Properties → Java Build Path → Libraries → Add JARs…**
3. Select `lib/chirp-1.0.0.jar`.

### Command line

```bash
javac -cp lib/chirp-1.0.0.jar -d out src/Main.java
java -cp "lib/chirp-1.0.0.jar:out" Main          # Linux / macOS
java -cp "lib\chirp-1.0.0.jar;out" Main          # Windows
```

---

## Adding your own files

Create any `.java` file under `src/` — VS Code compiles everything in that
folder automatically. Organize with sub-packages as needed:

```
src/
├── Main.java
├── models/
│   ├── User.java
│   └── Account.java
└── services/
    └── UserService.java
```

Then in `Main.java`:

```java
import models.User;
import services.UserService;
```

No additional configuration required.

---

## Building a desktop UI

The fat JAR (`chirp-1.0.0.jar`) includes JavaFX bundled. A minimal UI:

```java
import chirp.ui.Ui;
import chirp.ui.Label;
import chirp.ui.Stack;

public class GuiDemo {
    public static void main(String[] args) {
        Ui.app(() -> Ui.show("Hello",
                Stack.v(24).padding(32).centered()
                        .add(Label.heading("Hello, world!"))));
    }
}
```

Run from the command line to ensure JavaFX initializes correctly:

```bash
javac -cp lib/chirp-1.0.0.jar -d out src/GuiDemo.java
java -cp "lib/chirp-1.0.0.jar:out" GuiDemo
```

Full component catalog and examples in the
[JavaFX Guide](https://github.com/hyphensaad/chirp/blob/main/docs/JAVAFX_GUIDE.md).

---

## Configuration

`.vscode/settings.json` is already configured to:

- Use `src/` as the source path
- Compile to `out/`
- Add every JAR under `lib/` to the classpath

If you add another JAR, drop it into `lib/` and reload VS Code
(**Ctrl+Shift+P → Developer: Reload Window**).

---

## Updating Chirp

1. Delete the old JAR from `lib/`.
2. Download the new one.
3. Place it in `lib/`.
4. Reload VS Code.

`.vscode/settings.json` uses the wildcard pattern `lib/**/*.jar` — any JAR
in `lib/` is automatically on the classpath.

---

## Troubleshooting

### `cannot find symbol: class Chirp`

- Check `lib/chirp-1.0.0.jar` exists.
- Reload VS Code.
- Check the **Java** output panel for classpath errors.

### Colours show as `[31m` garbage

You are on a terminal that does not support ANSI (typically old Windows
CMD). Switch to Windows Terminal, or disable colours:

```bash
java -Dchirp.ansi=false -cp "lib/chirp-1.0.0.jar:out" Main
```

### `Toolkit not initialized` when running UI code

JavaFX requires the toolkit to start before any controls are constructed.
Wrap your UI code in `Ui.app(() -> { ... })`.

### Program exits immediately

For UI apps, the JVM exits when the last window closes. Add
`Platform.setImplicitExit(false)` after `Ui.app` to keep the JVM alive.

Full diagnostics: [Troubleshooting guide](https://github.com/hyphensaad/chirp/blob/main/docs/TROUBLESHOOTING.md).

---

## What's next

- [Quick Start](https://github.com/hyphensaad/chirp/blob/main/docs/QUICKSTART.md) — 5-minute tour of Chirp
- [Examples](https://github.com/hyphensaad/chirp/blob/main/docs/EXAMPLES.md) — before/after comparisons
- [API Reference](https://github.com/hyphensaad/chirp/blob/main/docs/API_REFERENCE.md) — every class and method
- [JavaFX Guide](https://github.com/hyphensaad/chirp/blob/main/docs/JAVAFX_GUIDE.md) — the full UI catalog

---

## License

This template is released under the MIT license. Copy it, modify it,
distribute it — no attribution required.
