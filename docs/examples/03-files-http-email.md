---
render_with_liquid: false
---

{% raw %}

# Files, HTTP, and Email

Working with files, calling APIs, and sending email.

---

## 1. Reading a text file

### Without Chirp

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

String content;
try {
    content = Files.readString(Path.of("data.txt"));
} catch (IOException e) {
    System.err.println("Cannot read file: " + e.getMessage());
    content = "";
}

List<String> lines;
try {
    lines = Files.readAllLines(Path.of("data.txt"));
} catch (IOException e) {
    System.err.println("Cannot read file: " + e.getMessage());
    lines = List.of();
}
```

Checked `IOException` requires `try/catch` at every call site.

### With Chirp

```java
import static chirp.Chirp.*;

String content = FileIO.readAll("data.txt");         // "" on failure
List<String> lines = FileIO.readLines("data.txt");   // empty list on failure
```

No `try/catch`. No `IOException`. Friendly error message printed to the
console:

```
[ERROR] Cannot read data.txt
```

---

## 2. Writing files

### Without Chirp

```java
try {
    Files.writeString(Path.of("out.txt"), "hello");
} catch (IOException e) {
    System.err.println("Cannot write file");
}

try {
    Files.writeString(Path.of("out.txt"), " more",
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
} catch (IOException e) {
    System.err.println("Cannot append file");
}
```

### With Chirp

```java
import static chirp.Chirp.*;

FileIO.write("out.txt", "hello");              // true/false
FileIO.append("out.txt", " more");
```

Or with the fluent wrapper:

```java
import chirp.io.TextFile;

TextFile file = TextFile.of("out.txt");
file.write("hello");
file.appendLine(" more");
file.appendLine("and more");
```

---

## 3. Text file stats

### Without Chirp

```java
long lineCount = 0;
long wordCount = 0;
long charCount = 0;

try (var reader = Files.newBufferedReader(Path.of("data.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        lineCount++;
        charCount += line.length();
        if (!line.isBlank()) {
            wordCount += line.trim().split("\\s+").length;
        }
    }
} catch (IOException e) {
    System.err.println("Cannot read");
}
```

### With Chirp

```java
import chirp.io.TextFile;

TextFile file = TextFile.of("data.txt");
int lines = file.countLines();
int words = file.countWords();
int chars = file.countChars();
long size  = file.sizeInBytes();
String human = file.humanSize();       // "1.5 KB"
```

---

## 4. Search and replace in files

### Without Chirp

```java
List<String> matches = new ArrayList<>();
List<Integer> lineNumbers = new ArrayList<>();

try {
    List<String> lines = Files.readAllLines(Path.of("data.txt"));
    for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).contains("error")) {
            matches.add(lines.get(i));
            lineNumbers.add(i + 1);
        }
    }
} catch (IOException e) {
    e.printStackTrace();
}

// Replace
String content = Files.readString(Path.of("data.txt"));
String updated = content.replace("old", "new");
Files.writeString(Path.of("data.txt"), updated);
```

### With Chirp

```java
import chirp.io.TextFile;

TextFile file = TextFile.of("data.txt");
List<String> matches = file.grep("error");
List<Integer> lineNumbers = file.lineNumbersContaining("error");
file.replace("old", "new");
```

---

## 5. CSV files

### Without Chirp

```java
// Reading CSV with quoted fields is non-trivial
List<List<String>> rows = new ArrayList<>();
try (BufferedReader reader = Files.newBufferedReader(Path.of("data.csv"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        // This regex handles quoted commas, but only if the quotes are
        // balanced and there are no escaped quotes
        String[] cells = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        List<String> row = new ArrayList<>();
        for (String cell : cells) {
            String clean = cell.trim();
            if (clean.startsWith("\"") && clean.endsWith("\"")) {
                clean = clean.substring(1, clean.length() - 1)
                        .replace("\"\"", "\"");
            }
            row.add(clean);
        }
        rows.add(row);
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

The regex does not handle embedded newlines or escaped quotes robustly.

### With Chirp — read

```java
import chirp.io.CsvFile;

// Raw rows
List<List<String>> rows = CsvFile.read("data.csv");

// With headers as first row
List<String> headers = CsvFile.readHeaders("data.csv");
List<List<String>> dataRows = CsvFile.readWithHeaders("data.csv");

// As typed records (header-keyed maps)
List<Map<String, String>> records = CsvFile.readAsRecords("data.csv");
for (Map<String, String> record : records) {
    println(record.get("name") + " — " + record.get("score"));
}

// As a table with column access
CsvFile.CsvTable table = CsvFile.table("data.csv");
List<String> names = table.column("name");
CsvFile.CsvTable adults = table.filter(r ->
        Integer.parseInt(r.get("age")) >= 18);
CsvFile.CsvTable sorted = table.sortBy("name");
```

### With Chirp — write

```java
import chirp.io.CsvFile;

List<List<String>> rows = List.of(
        List.of("name", "score"),
        List.of("Saad", "95"),
        List.of("Ali", "88"));
CsvFile.write("out.csv", rows);

// Append a row
CsvFile.append("out.csv", List.of("Chan", "92"));
```

Full RFC 4180 support — quoted fields with embedded commas, embedded
newlines, and escaped quotes are all handled correctly.

---

## 6. JSON files

### Without Chirp

```java
// Requires external library (Jackson, Gson, org.json, etc.)

// Parse
ObjectMapper mapper = new ObjectMapper();
Map<String, Object> config = mapper.readValue(
        new File("config.json"), Map.class);
String name = (String) config.get("name");

// Write
Map<String, Object> data = new HashMap<>();
data.put("name", "Saad");
data.put("age", 20);
mapper.writeValue(new File("out.json"), data);
```

Needs the Jackson dependency + 3 additional JARs.

### With Chirp — read

```java
import chirp.io.JsonFile;
import chirp.json.JsonObject;

JsonObject config = JsonFile.readObject("config.json");
String name = config.getString("name");
int age = config.getInt("age");
String city = config.getString("city", "Unknown");    // with default
```

### With Chirp — write

```java
import chirp.io.JsonFile;
import chirp.json.JsonObject;

JsonObject data = new JsonObject()
        .put("name", "Saad")
        .put("age", 20)
        .put("active", true);

// Pretty-printed
JsonFile.writePretty("out.json", data);

// Compact
JsonFile.writeCompact("out.json", data);
```

### With Chirp — inline JSON

```java
import chirp.json.Json;

// Parse
JsonObject user = Json.parseObject("{\"name\":\"Saad\",\"age\":20}");
println(user.getString("name"));

// Build
JsonObject built = Json.object()
        .put("name", "Saad")
        .put("age", 20);

// Serialize
String text = Json.write(built);       // compact
String pretty = Json.pretty(built);    // indented
```

Zero external dependencies.

---

## 7. Directory operations

### Without Chirp

```java
// Create directory
try {
    Files.createDirectories(Path.of("data/exports"));
} catch (IOException e) {
    e.printStackTrace();
}

// Count files
long count;
try (Stream<Path> stream = Files.list(Path.of("data"))) {
    count = stream.filter(Files::isRegularFile).count();
} catch (IOException e) {
    count = 0;
}

// Find by extension
List<String> pdfs = new ArrayList<>();
try (Stream<Path> stream = Files.walk(Path.of("data"))) {
    stream.filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".pdf"))
          .forEach(p -> pdfs.add(p.toString()));
} catch (IOException e) {
    e.printStackTrace();
}

// Delete directory
try (Stream<Path> walk = Files.walk(Path.of("old-data"))) {
    walk.sorted(Comparator.reverseOrder())
        .forEach(p -> {
            try {
                Files.delete(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
} catch (IOException e) {
    e.printStackTrace();
}
```

### With Chirp

```java
import static chirp.Chirp.*;

// Create (with parents)
mkdir("data/exports");

// Count
int count = countFiles("data");
int total = countAllFiles("data");

// Size
long size = dirSize("data");

// Find
List<String> pdfs = findFilesByExtension("data", "pdf");
List<String> reports = findFiles("data", "report-*.pdf");

// List subdirectories
List<String> subdirs = listDirectories(".");

// Delete recursively
FileIO.deleteDirectory("old-data");

// Copy tree
FileIO.copyDirectory("src", "backup");

// Empty check
if (isEmptyDir("temp")) {
    info("Nothing to clean");
}
```

---

## 8. HTTP GET

### Without Chirp

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

HttpClient client = HttpClient.newBuilder()
        .connectTimeout(java.time.Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/users/hyphensaad"))
        .timeout(java.time.Duration.ofSeconds(30))
        .header("Accept", "application/json")
        .GET()
        .build();

HttpResponse<String> response;
try {
    response = client.send(request, HttpResponse.BodyHandlers.ofString());
} catch (IOException | InterruptedException e) {
    System.err.println("Request failed: " + e.getMessage());
    return;
}

if (response.statusCode() >= 200 && response.statusCode() < 300) {
    String body = response.body();
    // Parse JSON manually
}
```

### With Chirp

```java
import chirp.http.*;

HttpResponse r = Http.get("https://api.github.com/users/hyphensaad")
        .header("Accept", "application/json")
        .timeoutSeconds(30)
        .send();

if (r.isSuccess()) {
    var user = r.asObject();
    success("User: " + user.getString("login"));
    info("Repos: " + user.getInt("public_repos"));
} else {
    error("HTTP " + r.status() + ": " + r.statusText());
}
```

### With Chirp — spinner

```java
HttpResponse r = Http.get(url)
        .withSpinner()
        .send();
```

The spinner appears in the terminal while the request is in flight.

### With Chirp — retry

```java
HttpResponse r = Http.get(url)
        .sendWithRetry(3);      // 3 attempts, 500ms backoff
```

Retries only on transport failures. HTTP errors (4xx/5xx) are returned
unretried.

---

## 9. HTTP POST

### Without Chirp

```java
String body = "{\"name\":\"Ali\",\"age\":25}";

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/users"))
        .timeout(java.time.Duration.ofSeconds(30))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + token)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

HttpResponse<String> response;
try {
    response = client.send(request, HttpResponse.BodyHandlers.ofString());
} catch (IOException | InterruptedException e) {
    e.printStackTrace();
    return;
}
```

### With Chirp

```java
import chirp.http.*;

HttpResponse r = Http.post("https://api.example.com/users")
        .json("{\"name\":\"Ali\",\"age\":25}")
        .bearer(token)
        .send();

if (r.isSuccess()) {
    var created = r.asObject();
    success("Created user: " + created.getString("id"));
}
```

---

## 10. HTTP connectivity check

### Without Chirp

```java
boolean online = false;
try (java.net.Socket socket = new java.net.Socket()) {
    socket.connect(new java.net.InetSocketAddress("1.1.1.1", 53), 1000);
    online = true;
} catch (Exception e) {
    online = false;
}
```

### With Chirp

```java
import static chirp.Chirp.*;

if (!isOnline()) {
    error("You need an internet connection.");
    return;
}

if (canReach("api.example.com", 443)) {
    info("Server reachable");
}

if (ping("https://example.com")) {
    info("Site is up");
}
```

---

## 11. HTTP with JSON response

### Without Chirp

```java
// HTTP + JSON parsing requires Jackson or Gson
HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
ObjectMapper mapper = new ObjectMapper();
Map<String, Object> data = mapper.readValue(response.body(), Map.class);
String name = (String) data.get("name");
```

### With Chirp

```java
import chirp.http.*;

HttpResponse r = Http.get("https://api.example.com/users")
        .send();

if (r.isSuccess()) {
    JsonArray users = r.asJson().asArray();
    for (var u : users.asList()) {
        println(u.asObject().getString("name"));
    }
}
```

HTTP and JSON, both zero-dependency.

---

## 12. Email — basic send

### Without Chirp

```java
// Requires jakarta.mail + jakarta.activation (2 JARs)

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

Properties props = new Properties();
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "587");
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");

Session session = Session.getInstance(props, new Authenticator() {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
                "me@gmail.com",
                System.getenv("SMTP_PASSWORD"));
    }
});

try {
    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress("me@gmail.com"));
    msg.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse("you@example.com"));
    msg.setSubject("Hello");
    msg.setText("Hi Saad!");
    Transport.send(msg);
    System.out.println("Sent");
} catch (MessagingException e) {
    System.err.println("Failed: " + e.getMessage());
}
```

### With Chirp

```java
import chirp.mail.Email;
import chirp.mail.EmailResult;

EmailResult r = Email.builder()
        .host("smtp.gmail.com")
        .port(587)
        .useStarttls()
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Hi Saad!")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();

if (r.isSuccess()) {
    success("Sent in " + r.durationMs() + "ms");
} else {
    error("Failed: " + r.message());
}
```

Zero external dependencies. Password read from `.env`. Result object
reports success/failure.

---

## 13. Email — HTML body

### With Chirp

```java
import chirp.mail.Email;

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Welcome")
        .html("""
                <html>
                <body>
                    <h1>Welcome!</h1>
                    <p>Thank you for signing up.</p>
                </body>
                </html>
                """)
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

---

## 14. Email — HTML template with placeholders

### Create the template `templates/welcome.html`

```html
<!DOCTYPE html>
<html>
  <head>
    <style>
      body {
        font-family: Arial, sans-serif;
      }
      .header {
        background: #4caf50;
        color: white;
        padding: 20px;
      }
      .footer {
        color: #888;
        font-size: 12px;
        padding: 10px;
      }
    </style>
  </head>
  <body>
    <div class="header">
      <h1>Welcome, {{name}}!</h1>
    </div>
    <div>
      <p>Your order <strong>#{{orderId}}</strong> has shipped.</p>
      <p>Total: <strong>{{amount}}</strong></p>
      <p>Estimated delivery: {{delivery}}</p>
    </div>
    <div class="footer">Thank you for shopping with us.</div>
  </body>
</html>
```

### With Chirp

```java
import chirp.mail.Email;
import chirp.mail.HtmlTemplate;
import chirp.format.Format;
import java.time.LocalDate;

HtmlTemplate template = HtmlTemplate.of("templates/welcome.html")
        .placeholder("name", "Saad")
        .placeholder("orderId", 42)
        .placeholder("amount", Format.currency(1234.5))
        .placeholder("delivery", DateTime.humanize(LocalDate.now().plusDays(3)));

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Order shipped")
        .template(template)
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

### Strict template validation

The `.placeholder()` calls are **validated** against the template file:

- Every declared placeholder must appear in the file
- Every placeholder in the file must be declared
- Mismatch throws `EmailException` with a clear message **before** any SMTP connection opens

```
EmailException: Declared placeholder 'shippingDate' not found in
templates/welcome.html. Available in template: [name, orderId,
amount, delivery]
```

---

## 15. Email — attachments

### Without Chirp

```java
// Attachment handling requires 20+ lines of MimeBodyPart construction
Multipart multipart = new MimeMultipart();

MimeBodyPart htmlPart = new MimeBodyPart();
htmlPart.setContent("<p>Body</p>", "text/html; charset=UTF-8");
multipart.addBodyPart(htmlPart);

MimeBodyPart attachmentPart = new MimeBodyPart();
attachmentPart.attachFile("reports/q4.pdf");
multipart.addBodyPart(attachmentPart);

MimeBodyPart attachment2 = new MimeBodyPart();
attachment2.attachFile("reports/summary.xlsx");
multipart.addBodyPart(attachment2);

msg.setContent(multipart);
```

### With Chirp

```java
import chirp.mail.Email;

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Q4 Report")
        .text("Hi Saad,\n\nAttached is the Q4 report.\n\nRegards")
        .attach("reports/q4.pdf")
        .attach("reports/summary.xlsx")
        .attach("reports/data.csv")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

Attachment existence is validated **before** the SMTP connection opens.

---

## 16. Email — password strategies

Chirp supports three password strategies. Pick one explicitly.

### ENV — read from `.env` (recommended)

Create `.env` file:

```
SMTP_PASSWORD=your-app-password-here
```

Then:

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

The `.env` file must **not** be committed to version control. Add it to
`.gitignore`.

### PROMPT — prompt at runtime

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .promptPassword()          // asks once at send time
        .send();
```

Uses `System.console().readPassword()` when available (no echo), falls
back to `Input.readRaw()` when running in an IDE.

### DIRECT — hardcoded (discouraged)

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .password("hardcoded-password")    // emits a warning
        .send();
```

Produces a warning at send time:

```
[WARN] DIRECT password is discouraged — consider ENV or PROMPT
```

---

## 17. Email — dry run (preview MIME)

### With Chirp

```java
import chirp.mail.Email;

EmailResult preview = Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Test")
        .template(template)
        .attach("report.pdf")
        .dryRun(true)
        .noLoader()
        .send();

// preview.rawMessage() contains the full MIME message
println(preview.rawMessage());
```

No SMTP connection is opened. The full MIME message (headers + body +
base64 attachments) is built and returned.

---

## 18. Email — retry and verbose

### Retry

```java
import chirp.mail.Email;

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .sendWithRetry(3);                  // 3 attempts, 500ms backoff
```

### Verbose

```java
import chirp.mail.Email;
import chirp.mail.EmailListener;

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .verbose(true)
        .listener(new EmailListener() {
            @Override
            public void onSmtpEvent(String line) {
                println("SMTP: " + line);
            }
        })
        .send();
```

Every SMTP dialog line is captured:

```
SMTP: C: EHLO myhost
SMTP: S: 250-smtp.gmail.com
SMTP: S: 250-STARTTLS
SMTP: C: STARTTLS
SMTP: S: 220 Ready to start TLS
SMTP: C: EHLO myhost
SMTP: S: 250-smtp.gmail.com
SMTP: C: AUTH LOGIN
SMTP: S: 334 VXNlcm5hbWU6
SMTP: C: me@gmail.com
SMTP: S: 334 UGFzc3dvcmQ6
SMTP: C: [password redacted]
SMTP: S: 235 2.7.0 Accepted
SMTP: C: MAIL FROM:<me@gmail.com>
SMTP: S: 250 2.1.0 OK
SMTP: C: RCPT TO:<you@example.com>
SMTP: S: 250 2.1.5 OK
SMTP: C: DATA
SMTP: S: 354 Go ahead
SMTP: C: .
SMTP: S: 250 2.0.0 OK
SMTP: C: QUIT
SMTP: S: 221 2.0.0 closing connection
```

---

## 19. Retry and error handling

### Without Chirp

```java
// Manual retry
int attempts = 0;
int maxAttempts = 3;
long backoffMs = 500;
while (true) {
    try {
        String result = fetchFromApi();
        return result;
    } catch (IOException e) {
        attempts++;
        if (attempts >= maxAttempts) throw e;
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }
    }
}
```

### With Chirp

```java
import static chirp.Chirp.*;

// Simple — 3 attempts, 500ms backoff
String result = retry(3, () -> fetchFromApi());

// Full control — exponential backoff, retry only IOException
import chirp.decorator.Retry;

String result2 = Retry.builder()
        .attempts(5)
        .backoffMs(250)
        .exponential()
        .jitter(0.1)
        .onlyOn(java.io.IOException.class)
        .label("API call")
        .build()
        .execute(() -> fetchFromApi());
```

### Timing

```java
import static chirp.Chirp.*;
import chirp.decorator.Timed;
import java.time.Duration;

// Runnable — returns Duration
Duration elapsed = timed(() -> longOperation());
println("Took: " + Format.duration(elapsed));

// Supplier — returns value + Duration
Timed<Integer> t = timed(() -> expensiveCompute());
println("Result: " + t.value() + " in " + t.millis() + "ms");
```

### Safe wrapper

```java
import static chirp.Chirp.*;

// Returns Optional — empty on failure
Optional<String> result = safe(() -> riskyOperation());

if (result.isPresent()) {
    success("Got: " + result.get());
}

// Runnable form returns boolean
boolean ok = safe(() -> riskyVoidOperation());
```

### Caching

```java
import static chirp.Chirp.*;

// Cache result under a key
String user = cached("user:42", () -> fetchUser(42));

// Invalidate
invalidate("user:42");

// Clear all
clearCache();
```

### Fallback

```java
import static chirp.Chirp.*;

// Return fallback if primary fails
String value = withFallback(() -> primary(), "default");

// Chain two fallbacks
String value2 = withFallback(
        () -> primary(),
        () -> secondary(),
        "last-resort");
```

---

## 20. Complete example — Daily backup report

A program that fetches data from an API, saves it to a file, and emails
a summary.

```java
import static chirp.Chirp.*;
import chirp.format.Format;
import chirp.http.HttpResponse;
import chirp.mail.Email;
import chirp.mail.EmailResult;
import chirp.mail.HtmlTemplate;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Chirp.start();

        // 1. Check connectivity
        if (!isOnline()) {
            error("No internet connection. Aborting backup.");
            return;
        }

        // 2. Fetch data from API
        info("Fetching data from API...");
        HttpResponse response = Http.get("https://api.example.com/daily-stats")
                .bearer(System.getenv("API_TOKEN"))
                .withSpinner()
                .sendWithRetry(3);

        if (!response.isSuccess()) {
            error("API returned " + response.status() + ": " + response.statusText());
            return;
        }

        // 3. Save raw data to file
        String filename = "backups/daily-" + LocalDate.now() + ".json";
        mkdir("backups");
        writeFile(filename, response.text());
        success("Saved raw data to " + filename);

        // 4. Parse and extract stats
        var stats = response.asObject();
        int totalUsers = stats.getInt("total_users");
        int newUsers = stats.getInt("new_users_today");
        int activeUsers = stats.getInt("active_today");
        double revenue = stats.getDouble("revenue_today");

        // 5. Build summary email
        HtmlTemplate template = HtmlTemplate.of("templates/daily-report.html")
                .placeholder("date", DateTime.humanize(LocalDate.now()))
                .placeholder("totalUsers", Format.thousands(totalUsers))
                .placeholder("newUsers", Format.thousands(newUsers))
                .placeholder("activeUsers", Format.thousands(activeUsers))
                .placeholder("revenue", Format.currency(revenue));

        // 6. Send email
        info("Sending summary email...");
        EmailResult emailResult = Email.builder()
                .host("smtp.example.com")
                .port(587)
                .useStarttls()
                .from("noreply@example.com")
                .to("admin@example.com")
                .subject("Daily backup report — " + LocalDate.now())
                .template(template)
                .attach(filename)
                .passwordFromEnv(".env", "SMTP_PASSWORD")
                .send();

        if (emailResult.isSuccess()) {
            success("Backup email sent in " + emailResult.durationMs() + "ms");
        } else {
            error("Email failed: " + emailResult.message());
            warn("Raw data is still saved at " + filename);
        }

        // 7. Summary
        println();
        line(50);
        table(
                List.of(
                        new String[]{"API call",  "OK (" + response.durationMillis() + "ms)"},
                        new String[]{"Raw file",  filename},
                        new String[]{"Email",     emailResult.isSuccess() ? "Sent" : "Failed"},
                        new String[]{"Total time","(automatic via Stats)"}),
                "Step", "Result");
        line(50);
    }
}
```

### What this demonstrates

- **HTTP with retry** — one line for three attempts
- **File management** — `mkdir`, `writeFile` with no `try/catch`
- **JSON parsing** — `response.asObject()` with typed accessors
- **HTML template** — validated before send
- **Email with attachment** — one line per file
- **Password from `.env`** — never hardcoded
- **Table output** — aligned column display
- **Run summary** — automatic on exit via `Chirp.start()`

### Sample run

```
========================================
  C H I R P
  A friendly Java toolkit
  v1.0.0 | github.com/hyphensaad/chirp
========================================

[INFO]  Fetching data from API...
⠋ Fetching data from API...
[OK]    Saved raw data to backups/daily-2026-10-31.json
[INFO]  Sending summary email...
⠋ Sending email to 1 recipient(s)
[OK]    Backup email sent in 2341ms

==================================================
Step          Result
--------------------------------------------------
API call      OK (1250ms)
Raw file      backups/daily-2026-10-31.json
Email         Sent
Total time    (automatic via Stats)
==================================================

====================================================
 Chirp run summary
====================================================
 Started        : Oct 31, 2026 2:15:32 PM
 Elapsed        : 3.602 s
 Heap used      : 28.4 MB / 380.0 MB (max 6076.0 MB)
 Process CPU    : 6.2 %
 Lines read     : 0
 Characters read: 0
 Read calls     : 0
====================================================
```

Every step is one line of Chirp. The full program handles: connectivity
check, retry, file management, JSON, HTML templating, email, spinners,
attachments, and shutdown summary. In standard Java, this would be 400+
lines across multiple files.
{% endraw %}
