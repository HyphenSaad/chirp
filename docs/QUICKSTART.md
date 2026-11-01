---
render_with_liquid: false
---

# Quick Start

Get from zero to a running program in **under five minutes**.

---

## Step 1 — Install Chirp

### For a plain Java project (VS Code / IntelliJ)

1. Download `chirp-1.0.0.jar` from the
   [latest release](https://github.com/hyphensaad/chirp/releases/latest).
2. Create a `lib/` folder next to your `src/` folder.
3. Place the JAR inside `lib/`.

Your project should look like:

```
MyProject/
├── lib/
│   └── chirp-1.0.0.jar
└── src/
    └── Main.java
```

### For VS Code

Create `.vscode/settings.json` in your project root:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.outputPath": "bin",
  "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

Reload VS Code (**Ctrl+Shift+P** → **Developer: Reload Window**).

### For IntelliJ IDEA

Right-click the `lib/` folder → **Add as Library**.

### For Maven

```xml
<dependency>
    <groupId>io.github.hyphensaad</groupId>
    <artifactId>chirp</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Step 2 — Write your first program

Create `src/Main.java`:

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

Run it. You should see:

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

---

## Step 3 — Explore the features

### Arrays

```java
int[] nums = {5, 3, 8, 1, 9, 2};
println("sum    = " + sum(nums));
println("max    = " + max(nums));
println("sorted = " + java.util.Arrays.toString(sorted(nums)));
```

Or with fluent chaining:

```java
String list = arrayChain(nums)
        .filter(n -> n > 2)
        .sorted()
        .join(", ");            // "3, 5, 8, 9"
```

### Formatting

```java
String greeting = fmt("Hello, {name}! You are {age}.", "name", "Saad", "age", 20);
println(greeting);

println(currency(1234.5));      // $1,234.50
println(percent(92.5));         // 92.50%
println(plural(3, "item", "items"));  // 3 items
```

### Files

```java
String content = FileIO.readAll("data.txt");
FileIO.write("output.txt", "hello");
List<String> lines = FileIO.readLines("input.txt");
```

### JSON

```java
import chirp.json.Json;

JsonObject user = Json.parseObject("{ \"name\": \"Saad\", \"age\": 20 }");
println(user.getString("name"));   // Saad
println(user.getInt("age"));       // 20
```

### HTTP

```java
HttpResponse r = Http.get("https://api.github.com/users/hyphensaad")
        .withSpinner()
        .send();

if (r.isSuccess()) {
    var user = r.asObject();
    success("User: " + user.getString("login"));
}
```

### Validation

```java
if (Check.isEmail(email)) {
    success("Valid email");
} else {
    error("Invalid email");
}

ValidationResult result = Validate.that(email)
        .notBlank("Email is required")
        .isEmail("That doesn't look like an email")
        .validate();

if (result.hasErrors()) {
    result.errors().forEach(this::error);
}
```

### Desktop UI

```java
import chirp.ui.*;

Ui.app(() -> {
    Field name = Field.of("Name").required();
    Label result = Label.of("");

    Button submit = Button.primary("Submit")
            .onClick(() -> {
                if (name.isValid()) {
                    result.text("Hello, " + name.value() + "!");
                }
            });

    Ui.show("Hello App", Stack.v(16).padding(24)
            .add(Label.heading("Welcome"), name, submit, result));
});
```

---

## Step 4 — Learn more

| Topic                    | Where                                 |
| ------------------------ | ------------------------------------- |
| All 40+ classes          | [API Reference](API_REFERENCE.md)     |
| Before/after comparisons | [Examples](EXAMPLES.md)               |
| JavaFX component catalog | [JavaFX Guide](JAVAFX_GUIDE.md)       |
| Common questions         | [FAQ](FAQ.md)                         |
| When things go wrong     | [Troubleshooting](TROUBLESHOOTING.md) |

---

## Notes

### About `Chirp.start()`

Calling `Chirp.start()` is **optional** but recommended when using:

- Console / Input methods (enables colours, `[STRICT]` hints, run summary)
- The `success()`, `error()`, `warn()`, `info()` helper methods

If you only use pure data manipulation (arrays, strings, dates, files), you
can skip `Chirp.start()` entirely and use Chirp as a plain library.

### About the warnings

If you see:

```
[STRICT] Reading 'Name' as string: no BLANK validation.
```

That's Chirp being helpful — it noticed that you didn't attach a validation
rule, which usually means the user could submit invalid input. Either add
`.notBlank()`, or call `.lenient()` to silence the warning, or turn off all
strict warnings globally with `Config.strictWarnings(false)`.

See [API Reference § Strict warnings](API_REFERENCE.md#strict-warnings).
