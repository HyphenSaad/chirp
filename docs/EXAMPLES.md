---
render_with_liquid: false
---

{% raw %}

# Examples — Chirp vs. standard Java

Every feature shown **with** and **without** Chirp. Copy-paste ready.

---

## Table of contents

1. [Console output](#1-console-output)
2. [Validated input](#2-validated-input)
3. [Arrays](#3-arrays)
4. [Strings](#4-strings)
5. [Numbers](#5-numbers)
6. [Date and time](#6-date-and-time)
7. [Validation](#7-validation)
8. [Files](#8-files)
9. [JSON](#9-json)
10. [HTTP](#10-http)
11. [Email](#11-email)
12. [Desktop UI](#12-desktop-ui)
13. [Retry and error handling](#13-retry-and-error-handling)

---

## 1. Console output

### Without Chirp

```java
System.out.println("=== === === ===");
System.out.println("  Student Manager");
System.out.println("=== === === ===");

System.out.println("[OK] Saved");
System.out.println("[ERROR] Failed");
System.out.println("[WARN] Low memory");

System.out.println("\u001B[32m[OK]\u001B[0m Saved");
```

You have to:

- Manually build separator strings
- Remember to prefix with `[OK]` / `[ERROR]`
- Look up ANSI escape codes (`\u001B[32m` = green)
- Handle the case where the terminal doesn't support ANSI

### With Chirp

```java
title("Student Manager");           // cyan box

success("Saved");                    // green ✓
error("Failed");                     // red ✘
warn("Low memory");                  // yellow ⚠
info("Loaded 42 records");           // cyan ℹ

line();                              // 40-char separator
line(60);                            // custom width
line(Separator.Style.DOUBLE);        // ════

// Or full control
line(Separator.builder()
        .width(60)
        .style(Separator.Style.DOUBLE)
        .align(Separator.Align.CENTER)
        .label(" Section 2 ")
        .color(Separator.Color.CYAN)
        .build());
```

Colours are auto-detected — on terminals that don't support ANSI (old
Windows CMD), Chirp falls back to plain text with `[OK]` / `[ERROR]` prefixes.
Same code works everywhere.

---

## 2. Validated input

### Without Chirp

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);

// Integer with retry and range check
int age;
while (true) {
    System.out.print("Age (1-130): ");
    String line = sc.nextLine().trim();
    try {
        age = Integer.parseInt(line);
        if (age >= 1 && age <= 130) break;
        System.out.println("Out of range.");
    } catch (NumberFormatException e) {
        System.out.println("Not a number.");
    }
}

// Non-empty string with length check
String name;
while (true) {
    System.out.print("Name: ");
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

// Email with regex
String email;
while (true) {
    System.out.print("Email: ");
    email = sc.nextLine().trim();
    if (email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) break;
    System.out.println("Invalid email.");
}

// Yes/no
boolean confirm;
while (true) {
    System.out.print("Continue? (y/n) ");
    String line = sc.nextLine().trim().toLowerCase();
    if (line.equals("y") || line.equals("yes")) { confirm = true; break; }
    if (line.equals("n") || line.equals("no")) { confirm = false; break; }
    System.out.println("Please type y or n.");
}
```

### With Chirp — immediate

```java
int age = readInt("Age", 1, 130);
String name = readNonEmpty("Name");
String email = readEmail("Email");
boolean confirm = readYesNo("Continue");
```

### With Chirp — fluent

```java
int age = readInt()
        .label("Age")
        .between(1, 130)
        .help("Must be 1-130")
        .read();

String name = readString()
        .label("Name")
        .notBlank()
        .atLeast(3)
        .atMost(30)
        .skipFormatWarning()
        .read();

String email = readString()
        .label("Email")
        .email()
        .read();
```

All the retry logic, range checks, regex validation, error messages — gone.
Chirp handles every edge case (null, empty, non-numeric, out-of-range).

---

## 3. Arrays

### Without Chirp

```java
int[] arr = {5, 3, 8, 1, 9, 2};

int sum = 0;
for (int n : arr) sum += n;

int max = arr[0];
for (int i = 1; i < arr.length; i++) if (arr[i] > max) max = arr[i];

int min = arr[0];
for (int i = 1; i < arr.length; i++) if (arr[i] < min) min = arr[i];

int[] reversed = new int[arr.length];
for (int i = 0; i < arr.length; i++) reversed[i] = arr[arr.length - 1 - i];

int[] sorted = arr.clone();
java.util.Arrays.sort(sorted);

int[] distinct = Arrays.stream(arr).distinct().toArray();

int[] rotated = new int[arr.length];
int shift = 2 % arr.length;
for (int i = 0; i < arr.length; i++) rotated[(i + shift) % arr.length] = arr[i];
```

### With Chirp — static

```java
int[] arr = {5, 3, 8, 1, 9, 2};

println("sum      = " + sum(arr));
println("max      = " + max(arr));
println("min      = " + min(arr));
println("reversed = " + java.util.Arrays.toString(reverse(arr)));
println("sorted   = " + java.util.Arrays.toString(sorted(arr)));
println("distinct = " + java.util.Arrays.toString(distinct(arr)));
println("rotated  = " + java.util.Arrays.toString(rotateRight(arr, 2)));
```

### With Chirp — fluent

```java
int[] result = arrayChain(arr)
        .filter(v -> v > 2)
        .sorted()
        .take(3)
        .toArray();
// {3, 5, 8}
```

### 2D matrices

**Without Chirp**:

```java
int[][] grades = {{80, 90, 85}, {70, 75, 95}, {88, 92, 78}};

// Transpose
int[][] transposed = new int[grades[0].length][grades.length];
for (int r = 0; r < grades.length; r++) {
    for (int c = 0; c < grades[r].length; c++) {
        transposed[c][r] = grades[r][c];
    }
}

// Row sum
int rowSum = 0;
for (int v : grades[0]) rowSum += v;

// Column sum
int colSum = 0;
for (int r = 0; r < grades.length; r++) colSum += grades[r][1];

// Main diagonal
int[] diag = new int[3];
for (int i = 0; i < 3; i++) diag[i] = grades[i][i];
```

**With Chirp**:

```java
int[][] grades = {{80, 90, 85}, {70, 75, 95}, {88, 92, 78}};

int[][] transposed = transpose(grades);
int rowSum = sumRow(grades, 0);
int colSum = sumCol(grades, 1);
int[] diag = diagonal(grades);

printMatrix(grades);          // nicely aligned
```

---

## 4. Strings

### Without Chirp

```java
String s = "  hello world  ";

// Capitalize
String cap = s.trim().substring(0, 1).toUpperCase() + s.trim().substring(1).toLowerCase();

// Title case
String[] words = s.trim().split("\\s+");
StringBuilder sb = new StringBuilder();
for (String w : words) {
    sb.append(Character.toUpperCase(w.charAt(0)))
      .append(w.substring(1).toLowerCase())
      .append(' ');
}
String title = sb.toString().trim();

// Camel case
String[] parts = s.trim().split("\\s+");
StringBuilder camel = new StringBuilder(parts[0].toLowerCase());
for (int i = 1; i < parts.length; i++) {
    camel.append(Character.toUpperCase(parts[i].charAt(0)))
         .append(parts[i].substring(1).toLowerCase());
}

// Palindrome
String clean = s.toLowerCase().replaceAll("[^a-z0-9]", "");
boolean pal = clean.equals(new StringBuilder(clean).reverse().toString());

// Mask
String card = "1234567890123456";
String masked = "*".repeat(card.length() - 4) + card.substring(card.length() - 4);
```

### With Chirp

```java
String s = "  hello world  ";

String cap = Text.capitalize(s);         // "Hello world"
String title = Text.titleCase(s);        // "Hello World"
String camel = Text.toCamelCase(s);      // "helloWorld"
boolean pal = Text.isPalindrome(s);      // false

String card = "1234567890123456";
String masked = Text.mask(card, 4);      // "************3456"

String slug = Text.slugify("Hello World!");            // "hello-world"
String trunc = Text.truncate("very long text", 10);    // "very long…"
```

Fluent chaining:

```java
String result = Text.chain("  hello world  ")
        .trim()
        .titleCase()
        .append("!")
        .get();
// "Hello World!"
```

---

## 5. Numbers

### Without Chirp

```java
Random rnd = new Random();
int die = rnd.nextInt(6) + 1;

boolean isEven = n % 2 == 0;

boolean isPrime = n >= 2;
for (int i = 2; i * i <= n; i++) {
    if (n % i == 0) { isPrime = false; break; }
}

long factorial = 1;
for (int i = 2; i <= n; i++) factorial *= i;

int gcd = a;
int b2 = b;
while (b2 != 0) {
    int t = b2;
    b2 = gcd % b2;
    gcd = t;
}

int max = Math.max(a, b);
int clamped = Math.max(0, Math.min(100, n));

String formatted = String.format("%,.2f", 1234.5);
```

### With Chirp

```java
int die = random(1, 6);
boolean isEven = isEven(n);
boolean isPrime = isPrime(n);
long factorial = factorial(n);
int gcd = gcd(a, b);
int max = max(a, b);
int clamped = clamp(n, 0, 100);

String formatted = thousands(1234);           // "1,234"
String currency = Format.currency(1234.5);    // "$1,234.50"
String bytes = Numbers.fileSize(1024);        // "1.0 KB"
String ord = Numbers.ordinal(1);              // "1st"
String roman = Numbers.roman(4);              // "IV"
String abbrev = Numbers.abbreviate(1_500_000);  // "1.5M"
```

Fluent chaining:

```java
int result = Numbers.of(10)
        .add(5)
        .times(2)
        .clamp(0, 20)
        .asInt();       // 20
```

Statistics:

```java
Numbers.Statistics stats = Numbers.statistics(new int[]{1, 2, 3, 4, 5});
stats.average();   // 3.0
stats.max();       // 5
stats.stdDev();    // 1.414
stats.median();    // 3.0
```

---

## 6. Date and time

### Without Chirp

```java
import java.time.*;
import java.time.format.*;

// Parse with fallback formats
String input = "31/10/2026";
LocalDate date = null;
try {
    date = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
} catch (Exception e) {
    try {
        date = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } catch (Exception e2) {
        try {
            date = LocalDate.parse(input, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } catch (Exception e3) {
            System.out.println("Invalid date: " + input);
        }
    }
}

// Human-readable format
String human = date.format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));

// Business days between two dates
int businessDays = 0;
LocalDate cursor = LocalDate.of(2026, 10, 1);
LocalDate end = LocalDate.of(2026, 11, 1);
while (cursor.isBefore(end)) {
    DayOfWeek dow = cursor.getDayOfWeek();
    if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
        businessDays++;
    }
    cursor = cursor.plusDays(1);
}

// Age
LocalDate birthDate = LocalDate.of(2000, 1, 15);
int age = Period.between(birthDate, LocalDate.now()).getYears();

// Relative time
LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);
String relative;
long seconds = java.time.Duration.between(fiveMinAgo, LocalDateTime.now()).getSeconds();
if (seconds < 60) relative = "just now";
else if (seconds < 3600) relative = (seconds / 60) + "m ago";
else if (seconds < 86400) relative = (seconds / 3600) + "h ago";
else relative = (seconds / 86400) + "d ago";

// Snap to start of month
LocalDate startOfMonth = LocalDate.of(2026, 10, 15).withDayOfMonth(1);
```

### With Chirp

```java
import java.time.*;

// Parse — tries all common formats, null on failure
LocalDate date = DateTime.parseDate("31/10/2026");
LocalDate iso  = DateTime.parseDate("2026-10-31");
LocalDate alt  = DateTime.parseDate("Oct 31, 2026");

// Human-readable
String human = DateTime.humanize(date);
// "Saturday, October 31, 2026"

String shortHuman = DateTime.humanizeShort(date);
// "Oct 31, 2026"

// Business days
int businessDays = DateTime.businessDaysBetween(
        LocalDate.of(2026, 10, 1),
        LocalDate.of(2026, 11, 1));    // 21

// Age
int age = DateTime.age(LocalDate.of(2000, 1, 15));

// Relative time
String relative = DateTime.timeAgo(LocalDateTime.now().minusMinutes(5));
// "5m ago"

// Snapping
LocalDate startOfMonth = DateTime.startOfMonth(LocalDate.of(2026, 10, 15));
LocalDate endOfMonth   = DateTime.endOfMonth(LocalDate.of(2026, 10, 15));
LocalDate startOfWeek  = DateTime.startOfWeek(LocalDate.of(2026, 10, 15));
LocalDate startOfYear  = DateTime.startOfYear(LocalDate.of(2026, 10, 15));

// Arithmetic
LocalDate future = DateTime.addDays(date, 30);
LocalDate past   = DateTime.subtractMonths(date, 6);

// Difference
long days = DateTime.daysBetween(from, to);

// Validity
if (DateTime.isValidDate(2026, 2, 29)) { /* false — 2026 is not a leap year */ }
```

Fluent chaining with `DateChain`:

```java
LocalDate endOfNextMonth = DateTime.of(LocalDate.now())
        .addMonths(1)
        .endOfMonth()
        .asDate();
```

---

## 7. Validation

### Without Chirp

```java
// Check if a string is a valid email
boolean emailOk = email != null
        && email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

// Check if a string is a valid Pakistani CNIC (13 digits)
boolean cnicOk = cnic != null
        && cnic.replaceAll("\\D", "").length() == 13
        && cnic.matches("^[1-8]\\d{12}$");

// Check if a string is a strong password (8+ chars, upper+lower+digit)
boolean pwdOk = pwd != null
        && pwd.length() >= 8
        && pwd.matches(".*[A-Z].*")
        && pwd.matches(".*[a-z].*")
        && pwd.matches(".*[0-9].*");

// Multi-error form validation
List<String> errors = new ArrayList<>();
if (name == null || name.isBlank()) errors.add("Name is required");
if (name != null && (name.length() < 3 || name.length() > 50)) {
    errors.add("Name must be 3-50 characters");
}
if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
    errors.add("Invalid email");
}
if (age < 1 || age > 130) errors.add("Age must be 1-130");
if (!errors.isEmpty()) {
    for (String err : errors) System.err.println(err);
    return;
}
```

### With Chirp — simple checks

```java
if (Check.isEmail(email)) { /* ok */ }
if (Check.isPakistaniCnic(cnic)) { /* ok */ }
if (Check.isStrongPassword(pwd)) { /* ok */ }
if (Check.isPhone(phone)) { /* ok */ }
if (Check.isURL(website)) { /* ok */ }
if (Check.isIPAddress(ip)) { /* ok */ }
if (Check.isUUID(id)) { /* ok */ }
```

Every `Check` method returns `false` for `null` — no `NullPointerException`.

### With Chirp — multi-error validation

```java
ValidationResult result = Validate.that(name)
        .notBlank("Name is required")
        .minLength(3, "Name must be at least 3 characters")
        .maxLength(50, "Name must be at most 50 characters")
        .validate();

if (result.hasErrors()) {
    result.errors().forEach(this::error);
    return;
}
```

The validator **collects all errors** — it does not stop at the first one.

### With Chirp — labeled fields

```java
ValidationResult result = Validate.of("Email", email)
        .notBlank("required")
        .isEmail("invalid format")
        .validate();

// error: "Email: invalid format"
```

### With Chirp — named rules

```java
// Register a custom rule once
Rules.register(Rule.define("EMPLOYEE_ID")
        .stripNonDigits()
        .matches("^\\d{6}$")
        .message("Employee ID must be 6 digits")
        .build());

// Then use it anywhere
if (Check.is("123456", "EMPLOYEE_ID")) { /* ok */ }

// Or in a reader
String id = readString()
        .label("Employee ID")
        .rule("EMPLOYEE_ID")
        .notBlank()
        .skipLengthWarning()
        .read();

// Or in a validator
ValidationResult r = Validate.that(id)
        .is("EMPLOYEE_ID", "Must be 6 digits")
        .validate();
```

### Built-in rules

| Rule name          | Description                                          |
| ------------------ | ---------------------------------------------------- |
| `EMAIL`            | RFC 5322 subset email                                |
| `PHONE_INTL`       | International phone (7–15 digits)                    |
| `PK_MOBILE`        | Pakistani mobile (`0300-1234567`, `+92 300 1234567`) |
| `PK_CNIC`          | Pakistani CNIC (13 digits, province 1–8)             |
| `PK_LANDLINE`      | Pakistani landline                                   |
| `PK_POSTAL_CODE`   | 5-digit postal code                                  |
| `PK_PASSPORT`      | 2 letters + 7 digits                                 |
| `PK_NTN`           | 7-digit NTN                                          |
| `PK_IBAN`          | Pakistani IBAN (24 chars starting PK)                |
| `PK_VEHICLE_PLATE` | Vehicle plate                                        |
| `USERNAME`         | 3–20 letters/digits/underscore                       |
| `STRONG_PASSWORD`  | 8+ chars, upper+lower+digit                          |
| `URL`              | http / https / ftp URL                               |
| `UUID`             | Standard UUID                                        |
| `HEX_COLOR`        | `#RRGGBB`                                            |

---

## 8. Files

### Without Chirp — read a text file

```java
import java.io.*;
import java.nio.file.*;

// Read whole file
String content;
try {
    content = Files.readString(Paths.get("data.txt"));
} catch (IOException e) {
    System.out.println("Cannot read file");
    content = "";
}

// Read line by line with error handling
List<String> lines = new ArrayList<>();
try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {
        lines.add(line);
    }
} catch (IOException e) {
    System.out.println("Cannot read file");
}

// Write
try {
    Files.writeString(Paths.get("out.txt"), "hello");
} catch (IOException e) {
    System.out.println("Cannot write file");
}

// Append
try (FileWriter fw = new FileWriter("out.txt", true);
     BufferedWriter bw = new BufferedWriter(fw)) {
    bw.write("more content");
    bw.newLine();
} catch (IOException e) {
    System.out.println("Cannot append");
}
```

### With Chirp — text files

```java
import chirp.io.FileIO;

// Read whole file (returns "" on failure)
String content = FileIO.readAll("data.txt");

// Read line by line (returns empty list on failure)
List<String> lines = FileIO.readLines("data.txt");

// Write (returns true/false)
boolean ok = FileIO.write("out.txt", "hello");

// Append
FileIO.append("out.txt", "more content");

// Copy, delete, exists
FileIO.copy("a.txt", "b.txt");
FileIO.delete("a.txt");
if (FileIO.exists("b.txt")) { /* yes */ }
```

### With Chirp — fluent file wrapper

```java
import chirp.io.TextFile;

TextFile f = TextFile.of("data.txt");
f.write("hello");
f.appendLine("world");
f.appendLine("again");

List<String> lines = f.readLines();
List<String> first5 = f.head(5);
List<String> last5 = f.tail(5);
int count = f.countLines();
int words = f.countWords();

if (f.exists()) { /* yes */ }
f.delete();
```

### With Chirp — CSV files

```java
import chirp.io.CsvFile;

// Read raw rows
List<List<String>> rows = CsvFile.read("data.csv");

// Read as typed records (header-keyed maps)
List<Map<String, String>> records = CsvFile.readAsRecords("data.csv");

// Read as table with column access
CsvFile.CsvTable table = CsvFile.table("data.csv");
List<String> names = table.column("name");
CsvFile.CsvTable adults = table.filter(r ->
        Integer.parseInt(r.get("age")) >= 18);
CsvFile.CsvTable sorted = table.sortBy("name");

// Write
CsvFile.write("out.csv", rows);
CsvFile.append("out.csv", List.of("new", "row"));
CsvFile.writeRecords("out.csv", records, List.of("name", "age"));
```

Full RFC 4180 support — quoted fields with embedded commas and newlines
are handled correctly.

### With Chirp — JSON files

```java
import chirp.io.JsonFile;
import chirp.json.JsonObject;

// Read object
JsonObject config = JsonFile.readObject("config.json");
String name = config.getString("name");
int port = config.getInt("port");

// Write pretty
JsonFile.writePretty("out.json", config);

// Write compact
JsonFile.writeCompact("out.json", config);
```

### With Chirp — directory operations

```java
// Create (and any missing parents)
FileIO.createDirectory("data/exports");

// Count files (non-recursive)
int count = FileIO.countFiles("data");

// Count files (recursive)
int total = FileIO.countAllFiles("data");

// Directory size in bytes
long size = FileIO.directorySize("data");

// Find by glob
List<String> pdfs = FileIO.findFiles("docs", "*.pdf");
List<String> txts = FileIO.findFilesByExtension("docs", "txt");

// List subdirectories
List<String> subdirs = FileIO.listDirectories(".");

// Copy directory tree
FileIO.copyDirectory("src", "backup");

// Delete directory and everything inside
FileIO.deleteDirectory("old-data");
```

---

## 9. JSON

### Without Chirp

```java
// Requires external dependency (Jackson, Gson, org.json, etc.)

// Parse
String json = "{\"name\":\"Saad\",\"age\":20}";
ObjectMapper mapper = new ObjectMapper();
Map<String, Object> user = mapper.readValue(json, Map.class);
String name = (String) user.get("name");
int age = ((Number) user.get("age")).intValue();

// Build
Map<String, Object> data = new HashMap<>();
data.put("name", "Ali");
data.put("age", 25);
String out = mapper.writeValueAsString(data);

// Nested
String nested = "{\"user\":{\"name\":\"Saad\"},\"tags\":[\"a\",\"b\"]}";
JsonNode root = mapper.readTree(nested);
String nestedName = root.get("user").get("name").asText();
```

Every option requires an external JAR you have to download and add to
the classpath.

### With Chirp — parse

```java
import chirp.json.Json;
import chirp.json.JsonObject;

// Parse object
JsonObject user = Json.parseObject("{\"name\":\"Saad\",\"age\":20}");
String name = user.getString("name");         // "Saad"
int age = user.getInt("age");                  // 20

// Parse array
JsonArray tags = Json.parseArray("[\"a\",\"b\",\"c\"]");
int size = tags.size();                        // 3
String first = tags.getString(0);              // "a"

// Any value
JsonValue v = Json.parse("{\"nested\":{\"key\":42}}");
JsonObject obj = v.asObject();
int nested = obj.getObject("nested").getInt("key");

// With defaults (no NPE)
String city = user.getString("city", "Unknown");   // "Unknown"
int score = user.getInt("score", 0);               // 0
```

### With Chirp — build

```java
JsonObject built = Json.object()
        .put("name", "Ali")
        .put("age", 25)
        .put("active", true)
        .put("score", 92.5);

JsonArray tags = Json.array()
        .add("a")
        .add("b")
        .add("c");

// Nested
JsonObject profile = Json.object()
        .put("user", built)
        .put("tags", tags);
```

### With Chirp — serialize

```java
String compact = Json.write(built);
// {"name":"Ali","age":25,"active":true,"score":92.5}

String pretty = Json.pretty(built);
// {
//   "name": "Ali",
//   "age": 25,
//   "active": true,
//   "score": 92.5
// }
```

### With Chirp — validate

```java
if (Json.isValid(text)) { /* parseable */ }
```

Zero external dependencies. The parser is ~300 lines of hand-written
recursive-descent code that handles objects, arrays, strings with escapes,
numbers, booleans, and null.

---

## 10. HTTP

### Without Chirp — GET with query parameters

```java
import java.net.*;
import java.net.http.*;
import java.io.*;

HttpClient client = HttpClient.newBuilder()
        .connectTimeout(java.time.Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

String url = "https://api.example.com/users?page=1&size=10";
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(java.time.Duration.ofSeconds(30))
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + token)
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
    // parse JSON manually (see section 9)
} else {
    System.err.println("HTTP " + response.statusCode());
}
```

### With Chirp — GET

```java
import chirp.http.*;

HttpResponse r = Http.get("https://api.example.com/users")
        .query("page", "1")
        .query("size", "10")
        .header("Accept", "application/json")
        .bearer(token)
        .timeoutSeconds(30)
        .send();

if (r.isSuccess()) {
    JsonArray users = r.asJson().asArray();
    for (var u : users.asList()) {
        println(u.asObject().getString("name"));
    }
} else {
    error("HTTP " + r.status() + ": " + r.statusText());
}
```

### With Chirp — POST JSON

```java
HttpResponse r = Http.post("https://api.example.com/users")
        .json("{\"name\":\"Ali\",\"age\":25}")
        .send();

if (r.isSuccess()) {
    success("Created user: " + r.asObject().getString("id"));
}
```

### With Chirp — with spinner

```java
HttpResponse r = Http.get(url)
        .withSpinner()
        .send();
```

The spinner appears in the terminal, animates while the request is in
flight, and disappears when done. Perfect for slow requests.

### With Chirp — with retry

```java
HttpResponse r = Http.get(url)
        .sendWithRetry(3);      // 3 attempts, 500ms backoff
```

Retries only on transport failures (connection refused, DNS failure,
timeout). HTTP error responses (4xx/5xx) are returned unretried — you
usually want to see those.

### With Chirp — connectivity check

```java
if (!Http.isOnline()) {
    error("You need an internet connection.");
    return;
}

if (Http.canReach("api.example.com", 443)) {
    // server is reachable
}
```

### With Chirp — listener

```java
Http.get(url)
    .listener(new HttpListener() {
        @Override
        public void onStart(HttpRequest req) {
            println("→ " + req.method() + " " + req.url());
        }

        @Override
        public void onSuccess(HttpResponse resp) {
            println("← " + resp.status() + " in " + resp.durationMillis() + "ms");
        }

        @Override
        public void onFailure(HttpRequest req, Throwable t) {
            error("Failed: " + t.getMessage());
        }
    })
    .send();
```

### With Chirp — save raw response

```java
Http.get(url)
        .saveRaw("response.json")
        .send();
```

The raw body is written to disk regardless of status — useful for
debugging failed responses.

### HTTP verbs

```java
Http.get(url);
Http.post(url);
Http.put(url);
Http.delete(url);
Http.patch(url);
Http.head(url);
Http.options(url);
```

---

## 11. Email

### Without Chirp

```java
// Requires jakarta.mail + jakarta.activation JARs

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
    msg.setContent(
            "<html><body><p>Hi <b>Saad</b>!</p></body></html>",
            "text/html; charset=UTF-8");

    // Attachments require more boilerplate
    Multipart multipart = new MimeMultipart();
    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent("<p>Body</p>", "text/html");
    multipart.addBodyPart(htmlPart);

    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile("report.pdf");
    multipart.addBodyPart(attachmentPart);

    msg.setContent(multipart);
    Transport.send(msg);
    System.out.println("Sent");
} catch (MessagingException | IOException e) {
    System.err.println("Failed: " + e.getMessage());
}
```

### With Chirp — plain text

```java
import chirp.mail.Email;

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

if (r.isSuccess()) success("Sent in " + r.durationMs() + "ms");
```

### With Chirp — HTML body

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .html("<html><body><p>Hi <b>Saad</b>!</p></body></html>")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

### With Chirp — HTML template with placeholders

```java
import chirp.mail.HtmlTemplate;
import chirp.format.Format;

HtmlTemplate template = HtmlTemplate.of("templates/welcome.html")
        .placeholder("name", "Saad")
        .placeholder("amount", Format.currency(1234.5))
        .placeholder("date", DateTime.humanize(LocalDate.now()));

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Welcome")
        .template(template)
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

Template validation is **strict**:

- Every declared placeholder must appear in the file
- Every placeholder in the file must be declared
- Mismatch throws `EmailException` with a clear message before any SMTP
  connection is opened

### With Chirp — attachments

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Report")
        .text("See attached")
        .attach("reports/2026-Q4.pdf")
        .attach("reports/summary.xlsx")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

Attachment existence is validated **before** the SMTP connection opens.
Missing files throw `EmailException` immediately.

### With Chirp — password strategies

```java
// Recommended — read from .env file
.passwordFromEnv(".env", "SMTP_PASSWORD")

// Prompt at runtime (never stored anywhere)
.promptPassword()

// Direct (for quick tests — produces a warning)
.password("hardcoded-for-testing-only")
```

### With Chirp — dry run

```java
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

println(preview.rawMessage());
// Shows the full MIME message without contacting the server
```

### With Chirp — retry

```java
Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .sendWithRetry(3, Duration.ofMillis(500));
```

### With Chirp — signature from session

```java
Session.me()
        .name("Saad Mansoor")
        .rollNumber("2024-CS-001")
        .degree("BSAI")
        .email("saad@example.com");

Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Body")
        .signatureFromSession()
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

The signature block is appended to the text body with the name, roll
number, program, and email from `Session`.

### With Chirp — verbose logging

```java
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

Every SMTP dialog line (`C: EHLO ...`, `S: 250 ...`) is captured. Perfect
for debugging.

---

## 12. Desktop UI

### Without Chirp

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red;");
        nameError.setVisible(false);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red;");
        emailError.setVisible(false);

        Button submit = new Button("Submit");
        submit.setOnAction(e -> {
            boolean valid = true;

            if (nameField.getText().isBlank()) {
                nameError.setText("Name is required");
                nameError.setVisible(true);
                valid = false;
            } else {
                nameError.setVisible(false);
            }

            if (!emailField.getText().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                emailError.setText("Invalid email");
                emailError.setVisible(true);
                valid = false;
            } else {
                emailError.setVisible(false);
            }

            if (valid) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Hello, " + nameField.getText() + "!");
                alert.showAndWait();
            }
        });

        VBox nameBox = new VBox(4, nameField, nameError);
        VBox emailBox = new VBox(4, emailField, emailError);
        VBox root = new VBox(16, nameBox, emailBox, submit);
        root.setStyle("-fx-padding: 24;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Registration");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

You have to:

- Extend `Application`
- Manually create every control
- Wire up validation for every field
- Show/hide error labels
- Load a CSS stylesheet
- Build the entire scene graph by hand

### With Chirp — form

```java
import chirp.ui.*;
import chirp.ui.Label;

public class Main {
    public static void main(String[] args) {
        Ui.app(() -> {
            Field name = Field.of("Name").required().minLength(2);
            Field email = Field.of("Email").email();

            Button submit = Button.primary("Submit")
                    .onClick(() -> {
                        if (name.isValid() && email.isValid()) {
                            Ui.info("Success",
                                    "Hello, " + name.value() + "!");
                        }
                    });

            Ui.show("Registration", Stack.v(16).padding(24)
                    .add(Label.heading("Register"))
                    .add(name)
                    .add(email)
                    .add(submit));
        });
    }
}
```

Validation, error display, field layout, CSS — all automatic.

### With Chirp — FormBuilder

```java
Stack form = FormBuilder.create("Register")
        .field("name", Field.of("Name").required().minLength(2))
        .field("email", Field.of("Email").email())
        .submit("Register", values -> {
            Ui.info("Success",
                    "Welcome, " + values.get("name") + "!");
        })
        .reset("Clear")
        .build();

Ui.app(() -> Ui.show("Registration", form));
```

### With Chirp — card layout

```java
Ui.app(() -> {
    Card card = Card.of()
            .title("Welcome back")
            .subtitle("Sign in to continue")
            .body(
                    Field.of("Email").email(),
                    Field.password("Password").required(),
                    Row.h(8)
                            .add(Button.primary("Sign in"))
                            .add(Button.ghost("Forgot password?"))
            )
            .fullWidth();

    Ui.show("Sign In", Stack.v(24).padding(32).add(card));
});
```

### With Chirp — dialog helpers

```java
Ui.info("Success", "Item saved");
Ui.warning("Low memory", "Consider closing unused tabs");
Ui.error("Operation failed", "Database is unreachable");
Ui.success("Done", "All files were exported");

if (Ui.confirm("Delete", "This cannot be undone. Continue?")) {
    deleteItem();
}
```

### With Chirp — table

```java
Table table = Table.of("ID", "Name", "Email")
        .row(1, "Saad", "saad@example.com")
        .row(2, "Ali", "ali@example.com")
        .row(3, "Chan", "chan@example.com")
        .rowHeight(32)
        .height(32 * 3 + 30)     // 3 rows + header
        .fullWidth();

Ui.app(() -> Ui.show("Users", Stack.v(16).padding(16).add(table)));
```

### With Chirp — theme

```java
// Light (default)
Ui.app(() -> Ui.show("App", root));

// Dark
Ui.app(() -> {
    Ui.theme(UiTheme.DARK);
    Ui.show("App", root);
});

// Auto-detect (follows OS in Windows Terminal / macOS / modern Linux)
Ui.app(() -> {
    Ui.theme(UiTheme.AUTO);
    Ui.show("App", root);
});

// Override with system property
// java -Dchirp.theme=dark -cp ... Main
```

### With Chirp — global style

```java
UiStyle.apply(
        UiStyle.Key.FONT_SIZE, 16,
        UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");

Ui.app(() -> Ui.show("App", root));

// Or use a preset
UiStyle.compact();
UiStyle.largeText();
UiStyle.modernLight();
UiStyle.modernDark();
UiStyle.monospace();
```

---

## 13. Retry and error handling

### Without Chirp

```java
// Manual retry with backoff
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

// Manual timing
long start = System.nanoTime();
riskyOperation();
long elapsed = System.nanoTime() - start;
System.out.println("Took " + elapsed / 1_000_000 + "ms");

// Manual cache
Map<String, Object> cache = new HashMap<>();
Object cached = cache.get("key");
if (cached == null) {
    cached = expensiveCompute();
    cache.put("key", cached);
}

// Manual safe wrapper
String result;
try {
    result = riskyOperation();
} catch (Exception e) {
    result = "default";
}
```

### With Chirp — retry

```java
import chirp.decorator.Retry;
import chirp.decorator.Decorator;

// Simple — 3 attempts, 500ms backoff
String result = Decorator.retry(3, () -> fetchFromApi());

// Full control — exponential backoff, retry only IOException
String result2 = Retry.builder()
        .attempts(5)
        .backoffMs(250)
        .exponential()
        .jitter(0.1)
        .onlyOn(java.io.IOException.class)
        .label("fetch from API")
        .build()
        .execute(() -> fetchFromApi());

// Runnable form
Decorator.retry(3, () -> saveToDatabase());
```

### With Chirp — timing

```java
// Wrap a Runnable — returns Duration
Duration elapsed = Decorator.timed(() -> riskyOperation());

// Wrap a Supplier — returns value + duration
Timed<Integer> t = Decorator.timed(() -> expensiveCompute());
int value = t.value();
long ms = t.millis();
```

### With Chirp — safe wrapper

```java
// Supplier — returns Optional<T>
Optional<String> result = Decorator.safe(() -> riskyOperation());

if (result.isPresent()) {
    success("Got: " + result.get());
} else {
    info("Operation failed, using default");
}

// Runnable — returns boolean
boolean ok = Decorator.safe(() -> riskyVoidOperation());
```

### With Chirp — caching

```java
// Cache the result of an expensive call
String value = Decorator.cached("user:42", () -> fetchUser(42));

// Invalidate
Decorator.invalidate("user:42");

// Clear all
Decorator.clearCache();
```

### With Chirp — fallback

```java
// Return a fallback if the primary operation fails
String result = Decorator.withFallback(
        () -> primarySource(),
        "fallback-value");

// Chain two fallbacks
String result2 = Decorator.withFallback(
        () -> primarySource(),
        () -> secondarySource(),
        "last-resort");
```

### With Chirp — logging

```java
// Log entry and exit automatically
String result = Decorator.logged("compute", () -> expensiveCompute());
// [DEBUG] → compute
// [DEBUG] ← compute

// Same for Runnable
Decorator.logged("save", () -> save());
```

### Combined — retry + timing + logging

```java
Duration total = Decorator.run(
        () -> {
            String data = fetchFromApi();
            process(data);
            saveResult();
        },
        Decorator.Key.RETRIES, 3,
        Decorator.Key.BACKOFF_MS, 500,
        Decorator.Key.EXPONENTIAL, true,
        Decorator.Key.LABEL, "pipeline",
        Decorator.Key.MEASURE, true);
```

---

## Summary

Chirp gives you all of the above with **one import**:

```java
import static chirp.Chirp.*;
```

No external dependencies for data modules. JavaFX bundled for the UI.
Zero `NullPointerException` from bad input. Zero `IOException` escapes
from file operations. Zero `NumberFormatException` from bad user input.

Every module is opt-in, every method is null-safe, every error is
recoverable. Write less, ship more.
{% endraw %}
