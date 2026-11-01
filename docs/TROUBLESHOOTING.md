---
render_with_liquid: false
---

# Troubleshooting

When things go wrong, this is where to look.

---

## Table of contents

- [Installation problems](#installation-problems)
- [Runtime problems](#runtime-problems)
- [Console and colours](#console-and-colours)
- [Input problems](#input-problems)
- [File problems](#file-problems)
- [HTTP problems](#http-problems)
- [Email problems](#email-problems)
- [UI problems](#ui-problems)
- [Build and test problems](#build-and-test-problems)
- [Diagnostic tools](#diagnostic-tools)

---

## Installation problems

### `cannot find symbol: class Chirp`

**Cause**: The Chirp JAR is not on your classpath.

**Fix**:

1. Make sure `chirp-1.0.0.jar` is in your `lib/` folder:

   ```
   MyProject/
   ├── lib/
   │   └── chirp-1.0.0.jar     ← must exist here
   └── src/
       └── Main.java
   ```

2. For VS Code, verify `.vscode/settings.json`:

   ```json
   {
     "java.project.sourcePaths": ["src"],
     "java.project.outputPath": "bin",
     "java.project.referencedLibraries": ["lib/**/*.jar"]
   }
   ```

3. Reload VS Code (**Ctrl+Shift+P** → **Developer: Reload Window**).

4. For IntelliJ, right-click `lib/` → **Add as Library**.

### `package chirp.Chirp does not exist`

**Cause**: Same as above — JAR is not on the classpath, or the JAR is
empty/corrupt.

**Fix**: Verify the JAR's checksum against the release page. If it does
not match, download it again.

### `UnsupportedClassVersionError: chirp/... has been compiled by a more recent version`

**Cause**: You are running on an older JDK than the JAR was compiled with.

**Fix**: Upgrade to **JDK 17** or later.

```bash
java -version
# Should print: openjdk version "17.0.x" or higher
```

### `Error: JavaFX runtime components are missing, and are required to run this application`

**Cause**: You are using the **thin JAR** but JavaFX is not on the classpath.

**Fix**: Either:

1. Use the **fat JAR** (`chirp-1.0.0.jar` from GitHub Releases — it includes JavaFX).
2. Add JavaFX to your classpath (see [INSTALLATION.md § JavaFX](INSTALLATION.md#javafx-setup-ui-module-only)).

### Build fails with `spotless:check` in `mvn verify`

**Cause**: Your changes are not formatted with the project's Eclipse
formatter config.

**Fix**:

```bash
mvn spotless:apply
```

Then commit the formatted files.

---

## Runtime problems

### `ChirpNotStartedException: Chirp has not been started yet`

**Cause**: You used a Chirp feature that requires `Chirp.start()` before
calling it.

**Fix**: Add this as the first line of your `main` method:

```java
Chirp.start();
```

The exception message contains this exact instruction — it appears in the
stack trace.

### Program exits immediately after starting

**Cause (UI apps)**: The JVM exits when the last JavaFX window closes.
If you close the only window, the JVM terminates.

**Fix**: Set `Platform.setImplicitExit(false)` after `Ui.app`:

```java
Ui.app(() -> {
    Platform.setImplicitExit(false);
    Ui.show("App", root);
});
```

Or keep a hidden window open:

```java
Stage hidden = new Stage();
hidden.setScene(new Scene(new StackPane(), 1, 1));
hidden.show();
```

### Program hangs at the end (does not exit)

**Cause (UI apps)**: A non-daemon JavaFX thread keeps the JVM alive.

**Fix**: Call `Platform.exit()` explicitly:

```java
stage.setOnCloseRequest(e -> {
    Platform.exit();
    System.exit(0);
});
```

Or ensure the JVM exits after the last window closes (default behavior).

### `OutOfMemoryError` when reading a large file

**Cause**: `FileIO.readAll` loads the entire file into memory.

**Fix**: Read line by line with `FileIO.readLines` and process one at a
time:

```java
for (String line : FileIO.readLines("big.csv")) {
    process(line);
}
```

Or use the streaming API for even larger files:

```java
try (Stream<String> lines = Files.lines(Path.of("big.csv"))) {
    lines.forEach(this::process);
}
```

---

## Console and colours

### Colours are not showing (`[31m` garbage)

**Cause**: Your terminal does not support ANSI, but Chirp thought it did.
This usually happens on old Windows CMD or PowerShell 5.1.

**Fix**:

1. Use **Windows Terminal** or **PowerShell 7+**.

2. Or disable colours explicitly:

   ```bash
   java -Dchirp.ansi=false -cp ... Main
   ```

3. Or programmatically:

   ```java
   Config.colorEnabled(false);
   ```

### Unicode box-drawing characters show as `в•”в•ђ`

**Cause**: Your terminal's codepage is not UTF-8. Common on Windows CMD
with codepage 437 or 1251.

**Fix**:

1. Switch to UTF-8 codepage in CMD:

   ```
   chcp 65001
   ```

2. Or use **Windows Terminal** (always UTF-8).

3. Or disable Unicode:

   ```bash
   java -Dchirp.ascii=true -cp ... Main
   ```

4. Or programmatically:

   ```java
   Chirp.start("ascii");
   ```

### Emojis show as `?` or boxes

**Cause**: Your terminal's font doesn't include emoji glyphs.

**Fix**:

1. Use a font with emoji support (e.g. Cascadia Code, Fira Code).
2. Change your terminal's font in preferences.
3. Use `-Dchirp.ascii=true` to avoid non-ASCII entirely.

### Garbage characters in the terminal

**Cause**: Same as above — ANSI or Unicode encoding mismatch.

**Fix**: Same as above. Add `-Dchirp.ansi=false -Dchirp.ascii=true` to your
launch command for guaranteed ASCII.

---

## Input problems

### Program does not respond to keyboard input

**Cause**: You are running the program from an IDE run button that does
not connect `System.in` to the terminal. Common in some IDE setups.

**Fix**:

1. In IntelliJ, ensure **Edit Configurations** → **Modify options** →
   **Emulate terminal in output console** is checked.
2. In VS Code, open a terminal and run `java -cp ... Main` instead of
   clicking the run button.

### Input is empty on first read

**Cause**: Something else consumed the first line — often a `Scanner` in
the same program.

**Fix**: Do not mix `Scanner` with `chirp.console.Input`. Pick one:

```java
// Good — all Chirp
int n = readInt("Number");
String name = readLine("Name");

// Bad — mixed
Scanner sc = new Scanner(System.in);
int n = sc.nextInt();
String name = readLine("Name");     // ← will be empty
```

### `NumberFormatException` on `readInt("Age")`

**Cause**: You are probably not using `chirp.console.Input.readInt` — you
are using `Integer.parseInt` directly.

**Fix**: Use Chirp's `readInt`:

```java
int age = readInt("Age");           // ← retries on bad input
// not
int age = Integer.parseInt(readLine("Age"));   // ← throws
```

### `[STRICT]` warnings shown on every input

**Cause**: You have not attached validation rules to the reader.

**Fix**: Add validation or silence the warning:

```java
// Add validation
readInt("Age", 1, 130);

// Or silence
readInt().lenient().read();

// Or globally
Config.strictWarnings(false);
```

---

## File problems

### `Cannot read data.txt` even though the file exists

**Cause**: The file exists **relative to the working directory**, and the
working directory is not where you think it is.

**Fix**: Check the current working directory:

```java
println(FileIO.workingDir());
```

The file `data.txt` is resolved relative to this directory. If your IDE
runs the program from `bin/`, then `data.txt` must be in `bin/` (not the
project root).

**Fix**: Use an absolute path, or copy the file to the correct directory.

### `Cannot write to /var/log/app.log` — permission denied

**Cause**: The process does not have write permission to that directory.

**Fix**: Write to a directory the process owns (e.g. user home):

```java
String logFile = FileIO.homeDir() + "/app.log";
FileIO.write(logFile, content);
```

### CSV file has extra quotes when read

**Cause**: The file was written by a program that double-escaped quotes.

**Fix**: Chirp handles RFC 4180 correctly. If a file is malformed, read
it as text and pre-process:

```java
String raw = FileIO.readAll("data.csv");
String cleaned = raw.replace("\"\"", "\"");   // collapse double-double quotes
List<List<String>> rows = CsvFile.parse(cleaned, ',');
```

### JSON file fails to parse

**Cause**: Malformed JSON, BOM (byte-order mark), or trailing commas.

**Fix**: Validate the file first:

```java
if (!JsonFile.isValid("data.json")) {
    error("Invalid JSON");
    return;
}
```

Or catch the exception:

```java
try {
    JsonValue v = Json.parse(FileIO.readAll("data.json"));
} catch (JsonException e) {
    error("Malformed JSON: " + e.getMessage());
}
```

### Files show as empty after writing

**Cause**: You forgot to call `close()` on a stream, or you're reading
before the write completes.

**Fix**: `FileIO.write` and `TextFileIO.write` are synchronous — the write
completes before the call returns. If you see empty files:

1. Check the write returned `true`:

   ```java
   if (!FileIO.write("data.txt", content)) {
       error("Write failed");
   }
   ```

2. Check the file was not deleted by another process.

3. Check you're looking at the right file (`FileIO.workingDir()`).

---

## HTTP problems

### `Request failed: Connection refused`

**Cause**: The server is not accepting connections on that port.

**Fix**:

1. Verify the URL and port:

   ```java
   println(Http.canReach("api.example.com", 443));   // true/false
   ```

2. Check your network connectivity:

   ```java
   println(Http.isOnline());
   ```

3. Check if the server is down.

### `Request failed: Unknown host`

**Cause**: DNS resolution failed — the host name does not exist or is not
resolvable.

**Fix**:

1. Check the host name in the URL.
2. Check your DNS configuration.
3. Try with a different DNS resolver.

### `HTTP 401 Unauthorized`

**Cause**: Your request did not include valid credentials.

**Fix**:

```java
Http.get(url)
        .bearer(token)               // for Bearer tokens
        .basicAuth("user", "pass")   // for Basic auth
        .send();
```

Or set the header directly:

```java
Http.get(url).header("Authorization", "Bearer " + token).send();
```

### `Request failed: timeout`

**Cause**: The server took longer than your timeout to respond.

**Fix**: Increase the timeout:

```java
Http.get(url).timeoutSeconds(60).send();
```

Or retry with a longer timeout:

```java
Http.get(url)
        .timeoutSeconds(60)
        .sendWithRetry(3);
```

### Response body is empty even though `status()` is 200

**Cause**: The server returned a body but sent `Content-Encoding: gzip`
and Chirp did not decode it.

**Fix**: Chirp's HTTP client handles gzip automatically via `java.net.http`.
If you still see empty bodies:

```java
Http.get(url)
        .header("Accept-Encoding", "identity")   // ask for plain
        .send();
```

### `Response body is X, expected JSON`

**Cause**: The server returned HTML (probably an error page) instead of
JSON.

**Fix**: Check the status code and content type:

```java
HttpResponse r = Http.get(url).send();
if (r.isSuccess()) {
    if (r.contentType().contains("json")) {
        var data = r.asObject();
    } else {
        error("Expected JSON, got " + r.contentType());
        println(r.text());      // see what the server actually returned
    }
}
```

---

## Email problems

### `Cannot reach SMTP server`

**Cause**: The host name or port is wrong, or the server is blocking
connections.

**Fix**:

```java
if (!Email.canConnect("smtp.gmail.com", 587)) {
    error("Cannot reach SMTP server");
}
```

Check:

1. Host name (e.g. `smtp.gmail.com` for Gmail)
2. Port (465 for SSL, 587 for STARTTLS, 25 for plain)
3. Firewall / VPN

### `SMTP authentication failed`

**Cause**: Bad credentials, or Gmail/Outlook require an app-specific
password.

**Fix**:

1. Check your password is correct (from `.env`, not hardcoded).
2. If using Gmail, enable 2-factor auth and generate an
   **app-specific password**.
3. If using Outlook, generate an **app password** in account settings.

### `Email body is required`

**Cause**: You called `.send()` without `.text(...)`, `.html(...)`, or
`.template(...)`.

**Fix**: Add a body:

```java
Email.builder()
        .host(...)
        .from(...)
        .to(...)
        .subject("Hi")
        .text("Hello!")           // ← body
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

### `Attachment not found`

**Cause**: The file path is wrong or the file was moved.

**Fix**:

```java
if (!FileIO.isFile("report.pdf")) {
    error("Attachment not found: " + new File("report.pdf").getAbsolutePath());
    return;
}
```

Chirp uses the **absolute path** when reporting the error — check that
path.

### `Declared placeholder 'X' not found in template`

**Cause**: You declared `{{name}}` but your HTML template does not contain
`{{name}}`.

**Fix**: Either:

1. Add `{{name}}` to the template, or
2. Remove the `.placeholder("name", ...)` declaration

Chirp is strict about this to catch typos — better to fail before sending
than to send an email with a missing placeholder.

### `Undeclared placeholder 'X' found in template`

**Cause**: Your HTML template contains `{{name}}` but you did not declare
`.placeholder("name", ...)`.

**Fix**: Add the declaration:

```java
HtmlTemplate.of("templates/welcome.html")
        .placeholder("name", "Saad")
        .placeholder("date", DateTime.humanize(LocalDate.now()));
```

---

## UI problems

### `Toolkit not initialized`

**Cause**: You created a UI control outside `Ui.app(...)`.

**Fix**: Move all UI construction inside `Ui.app`:

```java
public static void main(String[] args) {
    Ui.app(() -> {
        Button b = Button.primary("Click");   // ← correct
        // ...
    });
}
```

### `Cannot show a window in a headless environment`

**Cause**: You are running on a server without a display (Docker, CI).

**Fix**: Either:

1. Use only the data modules (skip the UI).
2. Provide a virtual display (Xvfb on Linux).
3. Use `xvfb-run java ...` in your CI.

### `Ui.show must be called on the JavaFX thread`

**Cause**: You called `Ui.show` outside `Ui.app`.

**Fix**:

```java
Ui.app(() -> Ui.show("Title", root));
```

Or if you already have an FX thread:

```java
Platform.runLater(() -> Ui.show("Title", root));
```

### UI is slow or laggy

**Cause**: Long-running operations are running on the FX thread, blocking
the UI.

**Fix**: Move long operations to a background thread:

```java
Ui.runAsync(
        () -> fetchData(),                    // background
        () -> updateUi(data));                // FX thread
```

### Window does not appear

**Cause**: JavaFX is initializing but the stage is not shown.

**Fix**: Verify `stage.show()` is called (Chirp's `Ui.show` does this
automatically). Check the console for exceptions.

### Styles not applied

**Cause**: Your custom stylesheet is loaded **before** Chirp's, so it is
overridden.

**Fix**: Load your stylesheet after `Ui.show`:

```java
Ui.show("Title", root);
Scene scene = Stage.getWindows().get(0).getScene();
scene.getStylesheets().add("custom.css");
```

---

## Build and test problems

### `mvn: command not found`

**Cause**: Maven is not installed or not on `PATH`.

**Fix**: Install Maven from [maven.apache.org](https://maven.apache.org/download.cgi)
and add it to your `PATH`.

### `[ERROR] Failed to execute goal ...: compile ...`

**Cause**: Compilation error in your code.

**Fix**: Read the full error message — it points to the exact line and
column. Common causes:

- Missing import
- Typo in method name
- Missing JAR on classpath
- Java version mismatch

### `Tests run: X, Failures: Y`

**Cause**: One or more tests failed.

**Fix**: Read the surefire report:

```
target/surefire-reports/
```

Open `TEST-*.xml` for the failing class. The full stack trace and assertion
message are there.

### `mvn verify` fails on `spotless:check`

**Cause**: Code is not formatted.

**Fix**:

```bash
mvn spotless:apply
```

Then commit the formatted files.

### `mvn verify` fails on `spotbugs:check`

**Cause**: SpotBugs found a bug in your code.

**Fix**: Read the report:

```bash
mvn spotbugs:gui
```

Or open `target/spotbugsXml.xml`. Fix the bug or (if it is a false
positive) add an exclusion in `config/spotbugs-exclude.xml` with a
comment explaining why.

### `mvn verify` fails on `pmd:check`

**Cause**: PMD found a defect in your code.

**Fix**: Read `target/pmd.xml` or `target/site/pmd.html`. Fix the issue
or add a `@SuppressWarnings("PMD.RuleName")` with a comment.

### Build is very slow

**Cause**: First build downloads dependencies.

**Fix**: Wait for the first build to finish. Subsequent builds use the
Maven local cache and are much faster.

To speed up:

```bash
mvn verify -DskipTests                   # skip tests
mvn verify -Dspotless.check.skip=true    # skip formatting check
mvn verify -Dpmd.skip=true               # skip PMD
mvn verify -Dspotbugs.skip=true          # skip SpotBugs
```

---

## Diagnostic tools

### Enable tracing

To see what Chirp is doing internally:

```java
Config.enableTrace();
Config.traceLevel(Trace.Level.DEBUG);       // or TRACE for more
Config.traceOnly("reader", "rule");         // only these categories
```

Output goes to `stderr` and shows every method call, validation decision,
and internal step.

### Enable debug logging

For `Debug.log(...)` calls from your own code:

```java
Config.enableDebug();
Debug.log("Processing: " + data);
```

### Report the diagnostics

When filing a bug report, include:

1. **Chirp version** — `chirp-1.0.0.jar` filename
2. **Java version** — `java -version`
3. **OS** — Windows / macOS / Linux
4. **Stack trace** — full, not truncated
5. **Reproducer** — 10 lines of code that trigger the bug
6. **Trace output** — if the bug is a runtime issue, enable trace and
   paste the output

The [bug report template](https://github.com/hyphensaad/chirp/issues/new?template=bug_report.md)
walks you through all of this.

### Check installed version

```java
println(chirp.Chirp.class.getPackage().getImplementationVersion());
```

Or check the JAR filename.

### Where does Chirp put temp files?

Chirp uses `FileIO.tempDir()` which returns `System.getProperty("java.io.tmpdir")`.
On Linux, this is usually `/tmp`. On Windows, `%TEMP%`. On macOS,
`$TMPDIR`.

---

## Still stuck?

- 📖 [FAQ](FAQ.md)
- 📚 [API Reference](API_REFERENCE.md)
- 💡 [Examples](EXAMPLES.md)
- 💬 [GitHub Discussions](https://github.com/hyphensaad/chirp/discussions)
- 🐛 [Report a bug](https://github.com/hyphensaad/chirp/issues/new?template=bug_report.md)
