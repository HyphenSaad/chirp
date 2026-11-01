---
render_with_liquid: false
---

{% raw %}

# Installation

Detailed installation guide for every platform and IDE.

---

## Prerequisites

| Requirement             | Minimum | Recommended |
| ----------------------- | ------- | ----------- |
| Java                    | JDK 17  | JDK 21      |
| JavaFX (UI module only) | 21      | 21          |
| Maven (build only)      | 3.9     | 3.9         |

Check your Java version:

```bash
java -version
```

If it says `17` or higher, you're good.

---

## Installation options

Chirp is distributed in three forms:

| Form            | Use case                                                   |
| --------------- | ---------------------------------------------------------- |
| **Fat JAR**     | Simplest — includes JavaFX bundled; drop in `lib/` and run |
| **Thin JAR**    | For Maven / Gradle projects that manage JavaFX themselves  |
| **Sources JAR** | For IDE navigation; not required at runtime                |

---

## Option A — Fat JAR (recommended for standalone projects)

**Step 1 — Download**

Go to [github.com/hyphensaad/chirp/releases/latest](https://github.com/hyphensaad/chirp/releases/latest) and download:

- `chirp-1.0.0.jar` — the fat JAR

**Step 2 — Place in `lib/`**

```
MyProject/
├── lib/
│   └── chirp-1.0.0.jar
└── src/
    └── Main.java
```

**Step 3 — Configure your IDE**

- **VS Code**: see [IDE setup for VS Code](#vs-code)
- **IntelliJ IDEA**: see [IDE setup for IntelliJ](#intellij-idea)
- **Command line**: see [Command line](#command-line)

The fat JAR includes JavaFX for the current platform — no additional JavaFX
download required.

---

## Option B — Thin JAR (Maven / Gradle projects)

**Step 1 — Add to your build file**

**Maven** (`pom.xml`):

```xml
<dependencies>
    <dependency>
        <groupId>io.github.hyphensaad</groupId>
        <artifactId>chirp</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
</dependencies>
```

**Gradle** (`build.gradle`):

```groovy
dependencies {
    implementation 'io.github.hyphensaad:chirp:1.0.0'
    implementation 'org.openjfx:javafx-controls:21.0.1'
}
```

**Step 2 — Import and use**

```java
import static chirp.Chirp.*;
```

---

## IDE setup

### VS Code

**Required extension**: [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

**Project structure**:

```
MyProject/
├── .vscode/
│   └── settings.json
├── lib/
│   └── chirp-1.0.0.jar
└── src/
    └── Main.java
```

**`.vscode/settings.json`**:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.outputPath": "bin",
  "java.project.referencedLibraries": ["lib/**/*.jar"],
  "files.encoding": "utf8"
}
```

**Verify**:

1. Reload VS Code (**Ctrl+Shift+P** → **Developer: Reload Window**).
2. Open `src/Main.java`.
3. Place cursor on `readInt` — F12 should navigate to the source.
4. Press **F5** to run.

### IntelliJ IDEA

**Step 1** — Open the project folder.

**Step 2** — Right-click the `lib/` folder → **Add as Library** → **OK**.

**Step 3** — Verify by typing `import chirp.Chirp;` — IntelliJ should resolve it.

**Step 4** — Run with **Shift+F10**.

### Eclipse

**Step 1** — Right-click the project → **Properties**.

**Step 2** — **Java Build Path → Libraries → Add JARs…**.

**Step 3** — Select `lib/chirp-1.0.0.jar`.

### Command line

```bash
# Compile
javac -cp lib/chirp-1.0.0.jar -d out src/Main.java

# Run (Linux/macOS)
java -cp "lib/chirp-1.0.0.jar:out" Main

# Run (Windows)
java -cp "lib\chirp-1.0.0.jar;out" Main
```

---

## JavaFX setup (UI module only)

If you use Chirp's **UI module** (`chirp.ui.*`), you need JavaFX 21 on the
classpath.

### With the fat JAR

**Nothing to do** — JavaFX is bundled.

### With the thin JAR

**Maven**:

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-graphics</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-base</artifactId>
    <version>21.0.1</version>
</dependency>
```

**Gradle**:

```groovy
dependencies {
    implementation "org.openjfx:javafx-controls:21.0.1"
    implementation "org.openjfx:javafx-graphics:21.0.1"
    implementation "org.openjfx:javafx-base:21.0.1"
}
```

**Manual download**:

Download the JavaFX SDK from [openjfx.io](https://openjfx.io) for your
platform and add the `lib/` folder to your classpath.

---

## Platform-specific notes

### Windows

- Use **Windows Terminal** or **PowerShell** — old CMD may not render colours
  or Unicode.
- If you see mojibake (`в•”в•ђ`), run `chcp 65001` in CMD first.
- The `chirp.ansi` property is auto-detected; set `-Dchirp.ansi=false` if
  colours misbehave.

### macOS

- Use **Terminal.app** or **iTerm2**.
- Full ANSI and Unicode support out of the box.

### Linux

- Use any modern terminal (GNOME Terminal, Konsole, Alacritty, etc.).
- Full ANSI and Unicode support out of the box.

### Headless / server environments

- The UI module requires a display. In headless environments, `Ui.show()`
  throws `UiException` with a clear message.
- Data modules (Text, Numbers, Collections, Files, HTTP, Email) work
  headless.

---

## Verifying your installation

Create `Test.java`:

```java
import static chirp.Chirp.*;

public class Test {
    public static void main(String[] args) {
        Chirp.start();
        success("Chirp is installed correctly!");
        println("Version: 1.0.0");
    }
}
```

Run it. If you see the banner and the green success message, you're good.

If not, see [Troubleshooting](TROUBLESHOOTING.md#installation-problems).

---

## Updating Chirp

1. **Delete** the old JAR from `lib/`.
2. **Download** the new JAR from GitHub Releases.
3. **Place** it in `lib/`.
4. **Reload** your IDE.

No configuration changes required — `.vscode/settings.json` uses
`lib/**/*.jar`, so it automatically picks up new versions.
{% endraw %}
