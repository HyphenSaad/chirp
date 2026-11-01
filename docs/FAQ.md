---
render_with_liquid: false
---

# Frequently Asked Questions

Answers to questions we hear most often.

---

## Table of contents

- [General](#general)
- [Getting started](#getting-started)
- [Input and output](#input-and-output)
- [Validation](#validation)
- [Files and data](#files-and-data)
- [HTTP and email](#http-and-email)
- [Desktop UI](#desktop-ui)
- [Errors and warnings](#errors-and-warnings)
- [Performance](#performance)
- [Compatibility](#compatibility)

---

## General

### What is Chirp?

Chirp is a Java library that replaces the friction of standard Java with
fluent, safe, one-line APIs. It is designed to reduce boilerplate for
common tasks — reading input, formatting output, working with arrays and
files, calling APIs, sending email, and building desktop UIs — without
adding external dependencies.

### Is Chirp an alternative to Spring Boot?

No. Spring Boot is a framework for building large applications. Chirp is a
**utility library** for common tasks. They solve different problems. You
can use Chirp _inside_ a Spring Boot application for its data helpers, but
Chirp is not a framework.

### Is Chirp free?

Yes. Chirp is released under the MIT license. You can use it for any
purpose — personal, commercial, open-source.

### Who maintains Chirp?

Chirp is maintained by Saad Mansoor ([@hyphensaad](https://github.com/hyphensaad)).
Contributions are welcome — see [CONTRIBUTING.md](../CONTRIBUTING.md).

### Does Chirp work with Java 11?

No. Chirp requires **Java 17** or later. The library uses `record`, `sealed`,
and text blocks, which are only available in Java 17+.

### Does Chirp work with Java 21?

Yes. Chirp is compiled against Java 17 but runs fine on Java 21.

### Does Chirp work with Java 8?

No. Java 8 is not supported. Use Java 17 or later.

---

## Getting started

### How do I install Chirp?

Download `chirp-1.0.0.jar` from the
[latest release](https://github.com/hyphensaad/chirp/releases/latest) and
place it in your project's `lib/` folder. See [INSTALLATION.md](INSTALLATION.md)
for full instructions.

### Do I have to call `Chirp.start()`?

**No.** You only need `Chirp.start()` if you want:

- The startup banner
- Coloured console output
- `[STRICT]` validation hints
- The run summary at program exit

If you only use pure data functions (`Text`, `Numbers`, `ArraysUtil`,
`FileIO`, `Json`, `Http`), you can skip it and use Chirp as a plain
library.

### Why does the banner print at startup?

The banner is Chirp's way of showing that it is active. To suppress it:

```java
Chirp.start("noHeader");              // or "silent"
Chirp.start(StartupConfig.silent());  // or via config
```

### What is the difference between `readInt("Age")` and `readInt()`?

- **`readInt("Age")`** — **immediate** mode. Prints `Age: `, reads, parses,
  retries on failure, returns `int`.
- **`readInt()`** — **fluent** mode. Returns an `IntReader` you configure
  with `.label(...)`, `.between(...)`, `.read()`.

Both use the same parsing and retry logic. The fluent form lets you add
more validation without repeating the type.

### Can I use Chirp in a Maven project?

Yes:

```xml
<dependency>
    <groupId>io.github.hyphensaad</groupId>
    <artifactId>chirp</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Can I use Chirp in a Gradle project?

Yes:

```groovy
dependencies {
    implementation 'io.github.hyphensaad:chirp:1.0.0'
}
```

---

## Input and output

### Why does `Scanner` skip my next `nextLine()`?

This is the famous `Scanner` bug. `nextInt()` reads the number but leaves
the newline character in the buffer. The next `nextLine()` reads that
newline and returns an empty string.

**Chirp solves this** because every `Input` method uses `nextLine()` +
parse, never `nextInt()`. You never have to call `sc.nextLine()` to "clear
the buffer".

### Can I use `Scanner` alongside Chirp's `Input`?

We strongly recommend against it. `Scanner` and `Input` both consume from
`System.in`. Once `Scanner` has buffered input, `Input` will not see it
(and vice versa). Pick one and stick with it.

If you need a raw `Scanner` for a specific case, create it explicitly:

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
```

Just do not mix it with `chirp.console.Input`.

### Can I disable `[STRICT]` warnings?

Yes, three ways:

```java
// Per reader
readInt().lenient().read();

// Per category
readInt().skipRangeWarning().read();

// Globally
Config.strictWarnings(false);
```

### Why does `success("hi")` require `Chirp.start()`?

`Console.success` is a **decorated** method — it uses colours and glyphs.
Those features are set up by `Chirp.start()`. Using `Console.println` does
not require `start()` because it is a raw method.

### How do I turn off colours?

```java
// Programmatically
Config.colorEnabled(false);

// From the command line
java -Dchirp.ansi=false ...

// Via startup flag
Chirp.start("noColor");
```

Chirp also auto-detects: on terminals that don't support ANSI, colours are
already off.

### How do I change the table column separator?

Chirp uses box-drawing characters (`─` `│`) when Unicode is supported and
ASCII (`-` `|`) otherwise. There is currently no way to customise the
characters — they are chosen for maximum terminal compatibility.

### Can I print to a file instead of the console?

Not directly. Use `FileIO` or `TextFile` for file output:

```java
FileIO.write("out.txt", "hello");
```

Or redirect `System.out` before calling `Chirp.start()`.

---

## Validation

### What is the difference between `Check`, `Rule`, and `Validate`?

| Class      | Purpose                                                                     |
| ---------- | --------------------------------------------------------------------------- |
| `Check`    | **Single boolean check** — `Check.isEmail(s)` returns `true`/`false`        |
| `Rule`     | **Composable rule** — combine multiple checks, normalise input              |
| `Validate` | **Multi-error validator** — collects all errors, does not stop at the first |

Choose the simplest one that fits your case:

- Need a yes/no answer? → `Check`
- Need to reuse a rule across fields? → `Rule`
- Need to show all errors at once? → `Validate`

### Can I create my own validation rule?

Yes. Use `Rule.define(...)`:

```java
Rule myRule = Rule.define("MY_RULE")
        .stripNonDigits()
        .matches("^\\d{6}$")
        .message("Must be 6 digits")
        .build();

// Use it directly
if (myRule.test(input)) { ... }

// Or register it
Rules.register(myRule);
if (Check.is(input, "MY_RULE")) { ... }
```

### Can I extract the regex from a rule?

Partially. `Regex.email()` returns the regex for the built-in email rule.
For custom rules, you can inspect the rule's behaviour with `test()`, but
there is **no way to extract a regex from an arbitrary rule** — rules can
combine normalisers, predicate chains, and `Rule.allOf(...)` compositions
that don't reduce to a single regex.

For rules like `STRONG_PASSWORD` that cannot be expressed as a regex,
`Regex.strongPassword()` returns `null` and logs a warning.

### Why does `Check.isPakistaniCnic` require the leading digit to be 1–8?

Pakistani CNIC first digits encode provinces:

| First digit | Province             |
| ----------- | -------------------- |
| 1           | Khyber Pakhtunkhwa   |
| 2           | Sindh (interior)     |
| 3           | Punjab (central)     |
| 4           | Sindh (Karachi)      |
| 5           | Punjab (north)       |
| 6           | Balochistan          |
| 7           | Azad Jammu & Kashmir |
| 8           | Gilgit-Baltistan     |

Anything starting with 0 or 9 is invalid.

### Can I disable a specific validation warning?

Yes:

```java
readInt().skipRangeWarning().read();
readString().skipFormatWarning().read();
readString().skipLengthWarning().read();
readString().skipBlankWarning().read();
```

Or disable all strict warnings on a single reader:

```java
readInt().lenient().read();
```

---

## Files and data

### Does `FileIO.readAll` throw if the file doesn't exist?

No. It returns `""` and prints a friendly error message via
`chirp.internal.Logger`:

```
[ERROR] Cannot read no-such-file.txt
```

If you need to distinguish success from failure, check `FileIO.exists(path)`
first, or use `TextFile.exists()`.

### What charset does Chirp use for files?

**UTF-8** everywhere. Both reading and writing.

### Does CSV support quoted fields?

Yes, with full RFC 4180 support:

- Quoted fields (`"hello, world"`)
- Escaped quotes (`"she said ""hi"""`)
- Embedded newlines
- Empty cells
- Custom delimiters (`;`, `\t`)

### What about JSON files larger than 2 GB?

`JsonFile.read` reads the entire file into a `String`. Files larger than
about 2 GB will fail with `OutOfMemoryError`. For large JSON files, parse
streaming with a dedicated library.

### Can I write to a file atomically?

Not directly. `FileIO.write` uses `Files.writeString`, which does not
guarantee atomicity. For atomic writes:

```java
Path tmp = Files.createTempFile("chirp", ".tmp");
Files.writeString(tmp, content);
Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
```

---

## HTTP and email

### Does `Http` support HTTPS?

Yes, using the JDK's built-in trust store. Self-signed certificates will
fail; you would need to add them to the JDK trust store.

### Can I use `Http` for file uploads?

Not directly. `Http` supports JSON and form-encoded bodies. For
multipart/form-data (file uploads), use a dedicated HTTP client such as
`java.net.http` directly or a library like Apache HttpClient.

### Does `Http` follow redirects?

Yes, by default up to 5 hops.

### What does `Http.isOnline()` actually check?

It opens a TCP socket to one of three public DNS servers (1.1.1.1,
8.8.8.8, 208.67.222.222) on port 53 with a 1-second timeout. If any
succeeds, it returns `true`.

This is **best-effort** and can fail in restricted networks even if the
internet is up.

### Does `Email` support TLS?

Yes:

- **SMTPS** (implicit TLS): port 465 — use `.ssl(true)` or `.port(465)`
- **STARTTLS**: port 587 — use `.useStarttls()` or `.port(587)`

Port 465 auto-enables SSL; port 587 auto-enables STARTTLS. If you use a
different port, specify the encryption explicitly.

### Where should I put my SMTP password?

Chirp recommends **`.env` file** or **prompt at runtime**:

```java
// Recommended — from .env
.passwordFromEnv(".env", "SMTP_PASSWORD")

// Also good — prompt every time
.promptPassword()

// Discouraged — hardcoded
.password("hardcoded-password")
```

Direct passwords produce a warning. Never commit them to version control.

### Can I send attachments larger than X?

Theoretically there is no size limit — but SMTP servers typically reject
messages above 25 MB (including encoding overhead). Base64 encoding
increases size by 33%, so a 20 MB file becomes 26.7 MB in the message.
Practical limit: **15 MB**.

### Does Chirp store my sent emails?

No. `Email.builder().send()` sends the message and returns an
`EmailResult`. Nothing is logged to disk unless you enable `.verbose(true)`
(the SMTP dialog is captured in the result) or `.saveRaw(path)` (the MIME
message is saved to a file).

---

## Desktop UI

### Do I need JavaFX to use Chirp?

Only if you use the **UI module** (`chirp.ui.*`). The data modules
(`Text`, `Numbers`, `Files`, `Http`, `Email`) do not require JavaFX.

### The fat JAR already includes JavaFX — do I need the JavaFX SDK?

No. The `chirp-1.0.0.jar` from GitHub Releases bundles JavaFX for the
current platform. If you download it, you have everything.

### What JavaFX version does Chirp use?

JavaFX **21.0.1**.

### Can I use Chirp UI on Java 11?

No. Chirp UI requires JavaFX 21, which requires Java 17+. Use Java 17+.

### Why do I get "Toolkit not initialized" when I run my app?

This error occurs when you create UI components (e.g. `new Button(...)`)
before JavaFX is started. The fix is to run everything inside `Ui.app(...)`:

```java
public static void main(String[] args) {
    Ui.app(() -> {
        // All UI code goes here
        Ui.show("App", root);
    });
}
```

Alternatively, if you must create controls outside `Ui.app`, they will
throw at construction time. Wrap the construction inside `Ui.app`:

```java
Ui.app(() -> {
    Button b = Button.primary("Click me");   // ← correct
    // ...
});
```

### Why does my window disappear immediately?

JavaFX exits when the last window closes **unless** `Platform.setImplicitExit(false)`
is set. Chirp does not set this by default — closing the last window
closes the JVM. If you want the JVM to keep running, call
`Platform.setImplicitExit(false)` after `Ui.app`.

### How do I change the window size?

```java
Ui.show("Title", root);

// Or with UiApp:
Ui.show(UiApp.builder()
        .title("Title")
        .size(1024, 768)
        .content(root)
        .build());
```

### How do I disable the scroll pane in UiApp?

```java
UiApp.builder().noScroll().build();
```

### How do I customise the theme beyond light/dark?

Chirp uses CSS. You can override any CSS rule after `Ui.show` by loading
your own stylesheet:

```java
Ui.show("Title", root);
Scene scene = Stage.getWindows().get(0).getScene();
scene.getStylesheets().add("custom.css");
```

Load this **after** Chirp's built-in stylesheet to override it.

### Why can't I construct a `UiApp` directly with `new`?

`UiApp` has a private constructor — you must use `UiApp.builder()` or
`UiApp.from(...)`. This ensures all fields are set atomically and the
object is immutable.

---

## Errors and warnings

### What is `ChirpNotStartedException`?

Thrown when you use a Chirp feature that requires `Chirp.start()` before
calling `Chirp.start()`:

```java
Console.success("hi");         // throws ChirpNotStartedException

Chirp.start();
Console.success("hi");         // works
```

The fix is always the same: call `Chirp.start()` as the first line of your
`main` method.

### Why does the exit summary print when my program ends?

The exit summary is Chirp's **run statistics** — elapsed time, heap usage,
lines read, etc. It is printed by a JVM shutdown hook when `Stats` is
enabled (which `Chirp.start()` does by default).

To suppress it:

```java
Chirp.start("noStats");
```

Or globally:

```java
Config.stats(false);
```

Or via system property:

```bash
java -Dchirp.stats=false -cp ... Main
```

### Why do I see `[STRICT]` warnings?

Chirp is telling you that you used a reader without the validation rules
it expects (e.g. `readInt()` without `.between(...)`). The warning is a
**hint**, not an error — your code still works.

To silence:

```java
// Per reader
readInt().lenient().read();

// Globally
Config.strictWarnings(false);
```

See [API Reference § Strict warnings](API_REFERENCE.md#strict-warnings).

### Why does `FileIO.readAll` print an error message to the terminal?

When a file cannot be read, `FileIO.readAll` returns `""` and prints a
friendly message via `Messenger.error`. If you don't want the message,
check `FileIO.exists(path)` first, or wrap your call:

```java
String content = FileIO.exists("data.txt")
        ? FileIO.readAll("data.txt")
        : "default";
```

### Can I log Chirp errors to a file instead of the console?

Yes. Configure the JDK `System.Logger`:

```java
System.setProperty("java.util.logging.config.file",
        "my-logging.properties");
```

Chirp uses `System.getLogger("chirp")` when not in interactive mode.

---

## Performance

### Is Chirp slow?

No. Chirp adds **negligible overhead** over standard Java. Most methods
are thin wrappers or direct JDK calls. The heaviest overhead is in the
readers (each read uses `nextLine()` + parse) — but the total cost is
under 1 microsecond per read.

### Does `Http` cache responses?

No. Each `send()` makes a fresh network call. If you need caching, wrap it
in `Decorator.cached(...)`:

```java
String body = Decorator.cached("user:42",
        () -> Http.fetch(url).text());
```

### Does the stats sampler slow down my program?

Barely. The sampler thread runs at 1 Hz and each tick takes microseconds.
The overhead is under 0.01% CPU on any modern hardware. If you need zero
overhead, disable it:

```java
Chirp.start("noStats");
```

### Does `ArraysUtil.sum` use streams?

No. `ArraysUtil` methods use plain loops. This is deliberate:

- **Faster** than streams for short arrays (no stream pipeline overhead)
- **Simpler** stack traces
- **Easier to read** for learners

For arrays larger than 10,000 elements, streams and loops are equivalent.
For arrays smaller than 1,000, plain loops are faster.

---

## Compatibility

### Does Chirp work on Windows?

Yes. Full support for:

- Windows Terminal (ANSI, Unicode, TrueColor)
- PowerShell (ANSI on modern versions)
- CMD (no ANSI — Chirp auto-detects and disables colour)
- Git Bash (ANSI, Unicode)

### Does Chirp work on macOS?

Yes. Full support for Terminal.app and iTerm2.

### Does Chirp work on Linux?

Yes. Full support for GNOME Terminal, Konsole, Alacritty, and every other
modern terminal.

### Does Chirp work in IntelliJ IDEA?

Yes. Both the console output and the UI module work in IntelliJ.

### Does Chirp work in VS Code?

Yes. The console output works in the VS Code terminal. The UI module
requires JavaFX, which VS Code can run via the Java extension pack.

### Does Chirp work with GraalVM native image?

Not tested. GraalVM's closed-world assumption conflicts with Chirp's
reflection-based UI toolkit detection. It may work but is not officially
supported.

### Can I use Chirp with Kotlin?

Yes — any JVM language can use Chirp's Java API. Fluent chaining works
exactly the same.

### Can I use Chirp with Groovy?

Yes. Groovy's dynamic dispatch handles Chirp's static methods transparently.

---

## Contributing

### How do I contribute?

See [CONTRIBUTING.md](../CONTRIBUTING.md). All contributions are welcome —
bug reports, feature requests, documentation improvements, and code.

### Where do I report a bug?

Open an issue using the
[bug report template](https://github.com/hyphensaad/chirp/issues/new?template=bug_report.md).

### Where do I request a feature?

Open an issue using the
[feature request template](https://github.com/hyphensaad/chirp/issues/new?template=feature_request.md).

### Is there a Discord / Slack / chat?

Not at this time. Use GitHub Discussions for community questions.

---

## Still stuck?

- 📖 [Quick Start](QUICKSTART.md)
- 📚 [API Reference](API_REFERENCE.md)
- 💡 [Examples](EXAMPLES.md)
- 🔧 [Troubleshooting](TROUBLESHOOTING.md)
- 💬 [GitHub Discussions](https://github.com/hyphensaad/chirp/discussions)
- 🐛 [Report a bug](https://github.com/hyphensaad/chirp/issues/new?template=bug_report.md)
