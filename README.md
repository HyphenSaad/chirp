# Chirp 🐦

**A friendly Java toolkit for developers who want to write less boilerplate and focus on what matters.**

[![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](CHANGELOG.md)
[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/hyphensaad/chirp/actions)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-purple)](https://openjfx.io/)
[![Docs](https://img.shields.io/badge/docs-live-brightgreen)](https://hyphensaad.github.io/chirp/)

Chirp is a zero-boilerplate Java toolkit that replaces the friction of standard Java I/O, arrays, validation, date/time, networking, email, and desktop UI with **fluent, safe, one-line** APIs.

- **One import** — `import static chirp.Chirp.*;`
- **Zero runtime dependencies** (except JavaFX for the UI module)
- **Cross-platform** — Windows, macOS, Linux
- **Safe by default** — no `NullPointerException`, no `NumberFormatException` from bad input, no `IOException` from file access
- **Fluent where it helps, static where it is simpler**

---

## Table of contents

- [Why Chirp?](#why-chirp)
- [Before & after](#before--after)
- [Features](#features)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Documentation](#documentation)
- [Building from source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

---

## Why Chirp?

### The problem

Java is verbose. Common tasks that take 3–5 lines in modern languages take 10–20 lines in vanilla Java:

- Reading a validated integer from the console
- Iterating an array to find the max
- Reading a CSV file
- Formatting a date
- Calling an HTTP API
- Sending an email
- Building a desktop UI

Each of these requires specialised knowledge — `Scanner` quirks, checked exceptions, `String.format` placeholders, regex syntax — that has nothing to do with the actual problem you are solving.

### The solution

Chirp wraps all of that behind friendly, discoverable APIs:

| Standard Java pain                    | Chirp replaces it with                                |
| ------------------------------------- | ----------------------------------------------------- |
| `Scanner` skipping lines              | `int age = readInt("Age", 1, 130);`                   |
| 6-line array loop to find max         | `int max = max(arr);`                                 |
| try/catch for every file operation    | `String text = FileIO.readAll("file.txt");`           |
| `String.format("%s is %d", ...)`      | `fmt("{name} is {age}", "name", n, "age", a)`         |
| 4 lines to parse and format a date    | `DateTime.humanize(DateTime.parseDate("2026-10-31"))` |
| Apache HttpClient setup for a GET     | `HttpResponse r = Http.fetch(url);`                   |
| JavaMail configuration for SMTP       | `Email.builder().host(...).send();`                   |
| Swing boilerplate for a single button | `Button.primary("Save").onClick(...)`                 |

---

## Before & after

### Example 1 — Read a validated integer

**Without Chirp:**

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int age;
        while (true) {
            System.out.print("Age: ");
            String line = sc.nextLine();
            try {
                age = Integer.parseInt(line.trim());
                if (age < 1 || age > 130) {
                    System.out.println("Out of range. Try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Not a number. Try again.");
            }
        }
        System.out.println("You are " + age + " years old.");
    }
}
```

**With Chirp:**

```java
import static chirp.Chirp.*;

public class Main {
    public static void main(String[] args) {
        int age = readInt("Age", 1, 130);
        println("You are " + age + " years old.");
    }
}
```

The read is validated, out-of-range is rejected, non-numeric input is rejected, and the loop retries automatically.

---

### Example 2 — Read a validated string with custom rules

**Without Chirp:**

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String name;
        while (true) {
            System.out.print("Name: ");
            name = sc.nextLine();
            if (name.trim().isEmpty()) {
                System.out.println("Cannot be empty.");
                continue;
            }
            if (name.length() < 3 || name.length() > 30) {
                System.out.println("Must be 3 to 30 characters.");
                continue;
            }
            break;
        }
        System.out.println("Hello, " + name + "!");
    }
}
```

**With Chirp — immediate:**

```java
import static chirp.Chirp.*;

String name = readString()
        .label("Name")
        .notBlank()
        .atLeast(3)
        .atMost(30)
        .skipFormatWarning()
        .read();

success("Hello, " + name + "!");
```

---

### Example 3 — Array aggregate operations

**Without Chirp:**

```java
int[] arr = {5, 3, 8, 1, 9, 2};

int sum = 0;
for (int i = 0; i < arr.length; i++) sum += arr[i];

int max = arr[0];
for (int i = 1; i < arr.length; i++) if (arr[i] > max) max = arr[i];

int min = arr[0];
for (int i = 1; i < arr.length; i++) if (arr[i] < min) min = arr[i];

int[] reversed = new int[arr.length];
for (int i = 0; i < arr.length; i++) reversed[i] = arr[arr.length - 1 - i];

int[] sorted = arr.clone();
java.util.Arrays.sort(sorted);

System.out.println("sum = " + sum);
System.out.println("max = " + max);
System.out.println("min = " + min);
System.out.println("reversed = " + java.util.Arrays.toString(reversed));
System.out.println("sorted = " + java.util.Arrays.toString(sorted));
```

**With Chirp:**

```java
import static chirp.Chirp.*;

int[] arr = {5, 3, 8, 1, 9, 2};

println("sum = " + sum(arr));
println("max = " + max(arr));
println("min = " + min(arr));
printArray(reverse(arr));
printArray(sorted(arr));
```

Or with fluent chaining:

```java
String report = arrayChain(arr)
        .filter(v -> v > 2)
        .sorted()
        .join(", ");                       // "3, 5, 8, 9"
```

---

### Example 4 — Read a CSV file

**Without Chirp:**

```java
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("data.csv"));
        List<List<String>> rows = new ArrayList<>();
        for (String line : lines) {
            // Handle quoted commas — this regex is notoriously tricky
            rows.add(Arrays.asList(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")));
        }
        for (List<String> row : rows) {
            System.out.println(row);
        }
    }
}
```

**With Chirp:**

```java
import chirp.io.CsvFile;

for (var row : CsvFile.read("data.csv")) {
    println(row);
}
```

Or with typed records:

```java
CsvFile.CsvTable table = CsvFile.table("data.csv");
List<String> names = table.column("name");
CsvFile.CsvTable adults = table.filter(r -> Integer.parseInt(r.get("age")) >= 18);
```

---

### Example 5 — Call a JSON API

**Without Chirp:**

```java
import java.net.*;
import java.net.http.*;
import java.io.*;
import org.json.*;   // requires external dependency

public class Main {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/hyphensaad"))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        JSONObject obj = new JSONObject(resp.body());
        System.out.println("User: " + obj.getString("login"));
        System.out.println("Repos: " + obj.getInt("public_repos"));
    }
}
```

**With Chirp:**

```java
import static chirp.Chirp.*;

HttpResponse r = Http.get("https://api.github.com/users/hyphensaad")
        .withSpinner()
        .send();

if (r.isSuccess()) {
    var user = r.asObject();
    success("User: " + user.getString("login")
          + " — " + user.getInt("public_repos") + " repos");
}
```

No external dependencies. Error handling built-in. Spinner built-in.

---

### Example 6 — Send an email with HTML template

**Without Chirp:**

```java
// Requires jakarta.mail + activation JARs
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("me@example.com", System.getenv("SMTP_PASS"));
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("me@example.com"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("you@example.com"));
        msg.setSubject("Hello");
        msg.setContent("<p>Hi <b>Saad</b>!</p>", "text/html; charset=UTF-8");
        Transport.send(msg);
        System.out.println("Email sent");
    }
}
```

**With Chirp:**

```java
import chirp.mail.Email;

EmailResult result = Email.builder()
        .host("smtp.example.com")
        .from("me@example.com")
        .to("you@example.com")
        .subject("Hello")
        .template("templates/hello.html")     // auto-loaded and validated
        .passwordFromEnv(".env", "SMTP_PASS")
        .send();

if (result.isSuccess()) success("Email sent in " + result.durationMs() + "ms");
```

No external dependencies. Password pulled from `.env`. Template validated before sending. Loader shown during send.

---

### Example 7 — Build a desktop UI

**Without Chirp (JavaFX):**

```java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        Button submit = new Button("Submit");
        Label result = new Label();

        submit.setOnAction(e -> {
            if (nameField.getText().isEmpty()) {
                result.setText("Name is required");
                return;
            }
            result.setText("Hello, " + nameField.getText() + "!");
        });

        VBox root = new VBox(10, nameField, submit, result);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

**With Chirp:**

```java
import chirp.ui.*;
import chirp.ui.Label;

public class Main {
    public static void main(String[] args) {
        Ui.app(() -> {
            Field nameField = Field.of("Name").required();
            Label result = Label.of("");

            Button submit = Button.primary("Submit")
                    .onClick(() -> {
                        if (nameField.isValid()) {
                            result.text("Hello, " + nameField.value() + "!");
                        }
                    });

            Ui.show("Hello App", Stack.v(16).padding(24)
                    .add(Label.heading("Welcome"),
                         nameField,
                         submit,
                         result));
        });
    }
}
```

No `Application` subclass. No scene graph assembly. Theme applied automatically. Validation built-in.

---

## Features

| Module          | What it does                                                               |
| --------------- | -------------------------------------------------------------------------- |
| **Console**     | Printing, semantic messages, tables, progress bars, separators             |
| **Input**       | Prompted, looping, exception-free reading with fluent readers              |
| **Text**        | Null-safe string manipulation (case conversion, masking, palindrome, etc.) |
| **Check**       | Boolean validations (email, phone, URL, IP, password, Pakistani CNIC)      |
| **Validation**  | Composable rules with `Rule.define(...)`, multi-error `Validate` builder   |
| **Numbers**     | Random, clamp, prime, factorial, gcd, statistics, base conversion          |
| **Arrays**      | 1D/2D/3D helpers + fluent `ArrayChain`                                     |
| **Collections** | `List<Integer>` helpers, filtering, search, pagination                     |
| **DateTime**    | Parse, format, arithmetic, snapping, business days, age                    |
| **Format**      | Named-placeholder interpolation (`"{name}"`), currency, plural             |
| **Files**       | Safe text, CSV, JSON file I/O                                              |
| **JSON**        | Zero-dependency parser, builder, writer                                    |
| **HTTP**        | Fluent GET/POST/PUT/DELETE, JSON parsing, spinner, retry                   |
| **Email**       | SMTP with STARTTLS/SSL, HTML templates, attachments, `.env` passwords      |
| **UI (JavaFX)** | 30+ pre-styled components with validation built-in                         |
| **Decorators**  | Retry, timing, caching, safe-wrapping, fallback                            |
| **Session**     | Global user profile for screenshots / signatures                           |
| **Config**      | Colour, debug, strict warnings, stats — all from one place                 |
| **Startup**     | `Chirp.start()` banner and lifecycle                                       |
| **Stats**       | Automatic run summary (elapsed, heap, CPU, lines read)                     |
| **Trace**       | Internal tracing for debugging                                             |

---

## Installation

### Option 1 — Download the JAR (recommended)

Download `chirp-1.0.0.jar` from the [latest release](https://github.com/hyphensaad/chirp/releases/latest) and place it in your project's `lib/` folder:

```
MyProject/
├── lib/
│   └── chirp-1.0.0.jar
└── src/
    └── Main.java
```

**For VS Code**, `.vscode/settings.json` should contain:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.outputPath": "bin",
  "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

**For IntelliJ IDEA**: right-click `lib/` → **Add as Library**.

**For command line**:

```bash
javac -cp lib/chirp-1.0.0.jar -d out src/Main.java
java -cp lib/chirp-1.0.0.jar:out Main
```

### Option 2 — Maven

```xml
<dependency>
    <groupId>io.github.hyphensaad</groupId>
    <artifactId>chirp</artifactId>
    <version>1.0.0</version>
</dependency>
```

### JavaFX requirement

The UI module requires **JavaFX 21**. If you use Chirp's UI classes, add JavaFX to your classpath:

**Maven:**

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
```

**Manual**: download the JavaFX SDK from [openjfx.io](https://openjfx.io) and add the `lib/` folder to your classpath.

If you don't use the UI module, you don't need JavaFX at all.

See [INSTALLATION.md](docs/INSTALLATION.md) for detailed per-platform instructions.

---

## Quick start

```java
import static chirp.Chirp.*;

public class Main {
    public static void main(String[] args) {
        Chirp.start();

        String name = readLine()
                .label("Name")
                .notBlank()
                .atLeast(3)
                .read();

        int age = readInt("Age", 1, 130);

        success("Hello, " + name + "! You are " + age + " years old.");
    }
}
```

Run with Chirp on the classpath. You'll see:

```
  ┌────────────────────────────────────────────┐
  │                                            │
  │   C H I R P                                │
  │   A friendly Java toolkit                  │
  │                                            │
  │   v1.0.0 | github.com/hyphensaad/chirp     │
  │   Saad Mansoor (HyphenSaad)                │
  │                                            │
  └────────────────────────────────────────────┘

Name: Saad
Age: 25
✔ Hello, Saad! You are 25 years old.
```

See [QUICKSTART.md](docs/QUICKSTART.md) for a complete guide.

---

## Documentation

**📖 [Read the full documentation →](https://hyphensaad.github.io/chirp/)**

Or jump straight to a section:

- 🚀 [Quick Start](https://hyphensaad.github.io/chirp/QUICKSTART.html) — 5-minute tour
- 📦 [Installation](https://hyphensaad.github.io/chirp/INSTALLATION.html) — detailed setup per platform
- 📚 [API Reference](https://hyphensaad.github.io/chirp/API_REFERENCE.html) — every class and method
- 💡 [Examples](https://hyphensaad.github.io/chirp/EXAMPLES.html) — before/after comparisons
- 🏗️ [Architecture](https://hyphensaad.github.io/chirp/ARCHITECTURE.html) — how Chirp is designed
- 🎨 [JavaFX UI Guide](https://hyphensaad.github.io/chirp/JAVAFX_GUIDE.html) — full component catalog
- ❓ [FAQ](https://hyphensaad.github.io/chirp/FAQ.html) — common questions
- 🔧 [Troubleshooting](https://hyphensaad.github.io/chirp/TROUBLESHOOTING.html)
- 📖 [Full Javadoc](https://hyphensaad.github.io/chirp/javadoc/) — auto-generated class reference

---

## Building from source

```bash
git clone https://github.com/hyphensaad/chirp.git
cd chirp
mvn clean package
```

Output in `target/`:

| File                      | Purpose                                                |
| ------------------------- | ------------------------------------------------------ |
| `chirp-1.0.0.jar`         | Fat JAR (with JavaFX bundled) — hand this to end users |
| `chirp-1.0.0-thin.jar`    | Thin JAR (Chirp only) — for Maven consumers            |
| `chirp-1.0.0-sources.jar` | Sources                                                |
| `chirp-1.0.0-javadoc.jar` | API Javadoc                                            |

Run the test suite:

```bash
mvn test                    # tests only
mvn verify                  # tests + SpotBugs + PMD + Jacoco
mvn spotless:apply          # auto-format code
```

---

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

By participating in this project, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

---

## Author

**Saad Mansoor** ([@hyphensaad](https://github.com/hyphensaad))
📧 saad.mansoor@skt.umt.edu.pk
Lecturer, University of Management and Technology — Sialkot, Pakistan

---

## License

[MIT](LICENSE) © Saad Mansoor
