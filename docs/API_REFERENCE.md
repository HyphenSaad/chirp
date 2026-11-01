# Chirp API Reference

Comprehensive reference for every public class and method in Chirp.

**Version**: 1.0.0 | **Java**: 17+ | **JavaFX** (UI): 21

---

## Table of contents

- [Getting started](#getting-started)
- [Package index](#package-index)
- [`chirp.Chirp` — the facade](#chirpchirp--the-facade)
- [`chirp.console` — output and input](#chirpconsole--output-and-input)
- [`chirp.text` — strings and validation](#chirptext--strings-and-validation)
- [`chirp.validation` — composable rules](#chirpvalidation--composable-rules)
- [`chirp.number` — numeric utilities](#chirpnumber--numeric-utilities)
- [`chirp.collection` — arrays, lists, search](#chirpcollection--arrays-lists-search)
- [`chirp.datetime` — dates and times](#chirpdatetime--dates-and-times)
- [`chirp.format` — formatting](#chirpformat--formatting)
- [`chirp.io` — files (text, CSV, JSON)](#chirpio--files)
- [`chirp.json` — JSON](#chirpjson--json)
- [`chirp.http` — HTTP client](#chirphttp--http-client)
- [`chirp.mail` — email / SMTP](#chirpmail--email--smtp)
- [`chirp.decorator` — retry, timing, caching](#chirpdecorator--retry-timing-caching)
- [`chirp.session` — user profile](#chirpsession--user-profile)
- [`chirp.ui` — JavaFX toolkit](#chirpui--javafx-toolkit)
- [`chirp.config` — configuration](#chirpconfig--configuration)
- [`chirp.startup` — lifecycle](#chirpstartup--lifecycle)
- [`chirp.stats` — runtime statistics](#chirpstats--runtime-statistics)
- [Common patterns](#common-patterns)

---

## Getting started

```java
import static chirp.Chirp.*;
```

Every commonly-needed method is available through this single import.

---

## Package index

| Package               | Purpose                                                                               |
| --------------------- | ------------------------------------------------------------------------------------- |
| `chirp`               | The `Chirp` facade — everything in one import                                         |
| `chirp.console`       | Output, input, menus, loaders, separators                                             |
| `chirp.console.input` | Fluent readers (IntReader, StringReader, …)                                           |
| `chirp.text`          | `Text`, `Check`, `Regex`, `TextChain`                                                 |
| `chirp.validation`    | `Rule`, `RuleBuilder`, `Rules`, `Validate`, `ValidationResult`                        |
| `chirp.number`        | `Numbers`, `NumberChain`, statistics                                                  |
| `chirp.collection`    | `ArraysUtil`, `ArrayChain`, `Lists`, `Filter`, `Search`, `Pagination`                 |
| `chirp.datetime`      | `DateTime`, `DateChain`                                                               |
| `chirp.format`        | `Format`, `Formatter`                                                                 |
| `chirp.io`            | `FileIO`, `TextFileIO`, `TextFile`, `CsvFile`, `JsonFile`                             |
| `chirp.json`          | `Json`, `JsonObject`, `JsonArray`, `JsonValue`, `JsonParser`, `JsonWriter`            |
| `chirp.http`          | `Http`, `HttpRequest`, `HttpResponse`, `HttpMethod`, `HttpException`                  |
| `chirp.mail`          | `Email`, `EmailRequest`, `EmailResult`, `EmailException`, `HtmlTemplate`, `EnvLoader` |
| `chirp.decorator`     | `Decorator`, `Retry`, `Timed`                                                         |
| `chirp.session`       | `Session`                                                                             |
| `chirp.ui`            | `Ui`, `UiApp`, `UiTheme`, `UiStyle`, 40+ components                                   |
| `chirp.config`        | `Config`                                                                              |
| `chirp.startup`       | `Banner`, `ChirpLifecycle`, `StartupConfig`                                           |
| `chirp.stats`         | `Stats`, `StatsListener`, `StatsSampler`                                              |
| `chirp.internal`      | `Ansi`, `Capabilities`, `Trace`, `Glyphs`, etc.                                       |

---

## `chirp.Chirp` — the facade

The single-import entry point.

### Lifecycle

| Method                                   | Description                                                    |
| ---------------------------------------- | -------------------------------------------------------------- |
| `start()`                                | Start Chirp with default configuration                         |
| `start(StartupConfig)`                   | Start with explicit config                                     |
| `start(String... flags)`                 | Start with flags (`"noHeader"`, `"withStats"`, `"noColor"`, …) |
| `start(Consumer<StartupConfig.Builder>)` | Start with fluent config                                       |
| `init()`, `boot()`, `setup()`, `begin()` | Aliases for `start()`                                          |
| `isStarted()`                            | Whether Chirp has started                                      |

### Input

Every method has 5–8 aliases matching natural English.

| Canonical                     | Aliases                                                                                                                        | Returns              |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | -------------------- |
| `readInt(prompt)`             | `readInteger`, `takeInt`, `getInt`, `askInt`, `inputInt`, `promptInt`, `scanInt`, `readNumber`                                 | `int`                |
| `readInt(prompt, min, max)`   | `readInteger`, `takeInt`, …                                                                                                    | `int`                |
| `readInt()`                   | —                                                                                                                              | `IntReader`          |
| `readDouble(prompt)`          | `readDecimal`, `readFloat`, `takeDouble`, `getDouble`, `askDouble`                                                             | `double`             |
| `readLine(prompt)`            | `readString`, `readText`, `takeString`, `takeLine`, `getString`, `getLine`, `askLine`, `askString`, `inputLine`, `inputString` | `String`             |
| `readNonEmpty(prompt)`        | `readRequired`                                                                                                                 | `String`             |
| `readYesNo(prompt)`           | `readBool`, `readConfirm`, `confirm`, `askConfirm`, `askYesNo`                                                                 | `boolean`            |
| `readChar(prompt)`            | `readLetter`, `readSingleChar`, `takeChar`, `getChar`                                                                          | `char`               |
| `readEmail(prompt)`           | `askEmail`, `takeEmail`, `getEmail`                                                                                            | `String` (validated) |
| `readPhone(prompt)`           | `askPhone`                                                                                                                     | `String` (validated) |
| `readURL(prompt)`             | `readUrl`, `askURL`, `takeURL`                                                                                                 | `String` (validated) |
| `readDate(prompt)`            | —                                                                                                                              | `LocalDate`          |
| `readTime(prompt)`            | —                                                                                                                              | `LocalTime`          |
| `readDateTime(prompt)`        | —                                                                                                                              | `LocalDateTime`      |
| `readChoice(prompt, opts...)` | `choose`                                                                                                                       | `String`             |

### Output

| Method                                                            | Description          |
| ----------------------------------------------------------------- | -------------------- |
| `print(Object)` / `println(Object)` / `printf(String, Object...)` | Basic printing       |
| `success(msg)` / `ok()` / `done()`                                | Green ✓ message      |
| `error(msg)` / `fail()`                                           | Red ✘ message        |
| `warn(msg)` / `warning()`                                         | Yellow ⚠ message     |
| `info(msg)` / `note()`                                            | Cyan ℹ message       |
| `title(text)` / `heading()`                                       | Boxed header         |
| `line()` / `hr()`                                                 | Horizontal separator |
| `blank()` / `nl()`                                                | Empty line           |
| `table(rows, headers...)`                                         | Aligned table        |
| `progress(current, total)`                                        | Progress bar         |
| `printArray(arr)`                                                 | Any array type       |
| `printMatrix(m)`                                                  | 2D array             |

### Format & Date

See [Format](#chirpformat--formatting) and [DateTime](#chirpdatetime--dates-and-times) sections.

### Session & Rules

| Method               | Description             |
| -------------------- | ----------------------- |
| `session()`          | The global `Session`    |
| `getRule(name)`      | Fetch a validation rule |
| `requireRule(name)`  | Fetch or throw          |
| `registerRule(rule)` | Register a custom rule  |
| `hasRule(name)`      | Check if registered     |

### HTTP & Email

| Method                       | Description         |
| ---------------------------- | ------------------- |
| `get(url)` / `post(url)` / … | Fluent HTTP request |
| `fetch(url)`                 | Immediate GET       |
| `isOnline()`                 | Connectivity check  |
| `email()`                    | New `EmailRequest`  |
| `sendEmail(kv...)`           | Send via key-value  |

### Files

| Method                     | Description       |
| -------------------------- | ----------------- |
| `readFile(path)`           | Read text file    |
| `writeFile(path, content)` | Write text file   |
| `mkdir(path)`              | Create directory  |
| `countFiles(dir)`          | Count files       |
| `findFiles(dir, glob)`     | Glob search       |
| `readJson(path)`           | Parse JSON file   |
| `csvTable(path)`           | Read CSV as table |

---

## `chirp.console` — output and input

### `Console`

Static printing methods.

```java
import static chirp.console.Console.*;

println("hello");
success("saved");
error("failed");
title("Section");
line();
table(rows, "ID", "Name");
progress(50, 100);
```

**Semantic messages** — `success`, `error`, `warn`, `info` (plus aliases).
**Decorations** — `title`, `line`, `blank`, `separator()`.
**Styling** — `bold`, `underline`, `red`, `green`, `yellow`, `blue`, `cyan`,
`magenta`, `gray`.
**Arrays** — `printArray`, `printMatrix`.
**Tables** — `table(List<String[]>, String... headers)`.
**Progress** — `progress(current, total)`.

### `Input`

Immediate and fluent reading.

**Immediate mode**:

```java
int age = readInt("Age", 1, 130);
String name = readNonEmpty("Name");
```

**Fluent mode**:

```java
int age = readInt()
        .label("Age")
        .between(1, 130)
        .help("Must be 1–130")
        .read();
```

**Fluent readers**:

| Class            | Fluent methods                                                                                                                                                 |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `IntReader`      | `.label`, `.between`, `.min`, `.max`, `.positive`, `.even`, `.odd`, `.defaultValue`, `.help`, `.custom`, `.lenient`, `.read()`                                 |
| `DoubleReader`   | `.label`, `.between`, `.min`, `.max`, `.positive`, `.decimals`, `.read()`                                                                                      |
| `StringReader`   | `.label`, `.notBlank`, `.minLength`, `.maxLength`, `.lengthBetween`, `.email`, `.phone`, `.url`, `.regex`, `.oneOf`, `.rule`, `.custom`, `.lenient`, `.read()` |
| `BooleanReader`  | `.label`, `.defaultValue`, `.lenient`, `.read()`                                                                                                               |
| `CharReader`     | `.label`, `.oneOf`, `.lenient`, `.read()`                                                                                                                      |
| `DateReader`     | `.label`, `.min`, `.max`, `.between`, `.past`, `.future`, `.read()`                                                                                            |
| `TimeReader`     | `.label`, `.min`, `.max`, `.between`, `.read()`                                                                                                                |
| `DateTimeReader` | `.label`, `.min`, `.max`, `.between`, `.read()`                                                                                                                |

### `Menu`

```java
new Menu("Student Manager")
    .add("Add student", () -> addStudent())
    .add("List students", () -> listStudents())
    .separator()
    .add("Exit", () -> System.exit(0))
    .show();
```

### `Loader`

```java
Loader l = Loader.start("Sending email");
try {
    sendEmail();
    l.success("Sent");
} catch (Exception e) {
    l.error("Failed: " + e.getMessage());
}
```

### `Separator`

```java
Console.line();                          // default: 40 chars solid
Console.line(60);                        // 60 chars
Console.line(Separator.Style.DOUBLE);    // ════════════════
Console.line(60, Separator.Style.DASHED);

// Full control with builder
Console.line(Separator.builder()
    .width(60)
    .style(Separator.Style.DOUBLE)
    .align(Separator.Align.CENTER)
    .label(" Section ")
    .color(Separator.Color.CYAN)
    .build());
```

**Strict warnings** — the readers emit `[STRICT]` hints when validation is
missing. Silence with `.lenient()` per-reader, or
`Config.strictWarnings(false)` globally.

---

## `chirp.text` — strings and validation

### `Text` — null-safe string operations

| Method                                                         | Description               |
| -------------------------------------------------------------- | ------------------------- |
| `isBlank` / `isEmpty` / `isNotBlank`                           | Blank/empty checks        |
| `capitalize` / `titleCase`                                     | Case manipulation         |
| `toCamelCase` / `toPascalCase` / `toSnakeCase` / `toKebabCase` | Case conversion           |
| `swapCase` / `isUpperCase` / `isLowerCase`                     | Case checks               |
| `reverse` / `reverseWords`                                     | Reverse                   |
| `repeat(s, n)` / `padLeft` / `padRight` / `center`             | Layout                    |
| `truncate(s, max)` / `truncateMiddle`                          | Truncate                  |
| `countChar` / `countWords` / `countSubstrings`                 | Count                     |
| `isPalindrome` / `isAnagram`                                   | Palindrome / anagram      |
| `mask(s, visible)`                                             | Mask last N chars         |
| `slugify(s)`                                                   | URL-safe slug             |
| `between(s, start, end)`                                       | Extract substring         |
| `substringBefore` / `substringAfter`                           | Split on marker           |
| `removeSpaces` / `removePunctuation` / `removeDigits`          | Remove                    |
| `extractDigits` / `extractLetters`                             | Extract                   |
| `initials(name)`                                               | "Saad Mansoor" → "SM"     |
| `similarity(a, b)`                                             | Levenshtein-based 0.0–1.0 |
| `join(delimiter, parts...)`                                    | Join                      |
| `thatCheck(items, predicate)`                                  | Filter by Check           |

### `Check` — boolean validations

```java
if (Check.isEmail(input)) { ... }
if (Check.isPhone(input)) { ... }
if (Check.isURL(input)) { ... }
if (Check.isIPAddress(input)) { ... }
if (Check.isUUID(input)) { ... }
if (Check.isStrongPassword(input)) { ... }
if (Check.isPakistaniCnic(input)) { ... }
```

Full list: `isEmail`, `isPhone`, `isNumeric`, `isInteger`, `isAlpha`,
`isDigits`, `isAlphanumeric`, `isHexColor`, `isURL`, `isIPAddress`,
`isUUID`, `isUsername`, `isPakistaniPhone`, `isPakistaniCnic`,
`isStrongPassword`, `isVeryStrongPassword`, `isAdult`, `isValidGrade`,
`isLeapYear`, `inRange`, `isBlank`, `notBlank`, `isNull`, `notNull`,
`isJSON`, `isAnagram`, `isWeekend`, `isWeekday`.

**By rule name**:

```java
Check.is(value, "PK_MOBILE");
Check.is(value, rule);
```

### `Regex` — patterns as strings

```java
"user@example.com".matches(Regex.email());
"12345".matches(Regex.digits());
"+923001234567".matches(Regex.pakistaniMobile());
```

Full list: `email`, `phone`, `alpha`, `digits`, `numeric`, `alphanumeric`,
`username`, `hexColor`, `hexColorShort`, `slug`, `url`, `ipv4`, `ipv6`,
`macAddress`, `uuid`, `dateISO`, `dateDMY`, `dateMDY`, `time24Hour`,
`time12Hour`, `isoDateTime`, `creditCardVisa`, `creditCardMastercard`,
`creditCardAmex`, `iban`, `isbn10`, `isbn13`, `pakistaniMobile`,
`pakistaniCnic`, `pakistaniLandline`, `pakistaniPostalCode`,
`pakistaniPassport`, `pakistaniNtn`, `pakistaniIban`, `pakistaniVehiclePlate`,
`base64`, `htmlTag`, `latitude`, `longitude`.

### `TextChain` — fluent

```java
String result = Text.chain("hello world")
        .capitalize()
        .reverse()
        .append("!")
        .get();
// "Dlrow Olleh!"
```

---

## `chirp.validation` — composable rules

### `Rule` — one validation rule

```java
Rule pkMobile = Rule.define("PK_MOBILE")
        .description("Pakistani mobile")
        .stripAll("[\\s\\-()]")
        .matches("^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$")
        .message("Enter a valid Pakistani mobile.")
        .build();

pkMobile.test("0300-1234567");        // true
pkMobile.firstFailure("123");         // Optional["Enter a valid ..."]
```

**Composite**:

```java
Rule combined = Rule.allOf(rule1, rule2);       // AND
Rule either = Rule.anyOf(rule1, rule2);         // OR
Rule notR = Rule.not(rule1);                    // NOT
Rule atLeast2 = Rule.atLeast(2, r1, r2, r3);    // ≥2 of 3
```

**Fluent mutators**:

```java
rule.optional();     // allow blank
rule.required();     // require non-blank
rule.withMessage("New error");
```

### `Rules` — the registry

| Method           | Description          |
| ---------------- | -------------------- |
| `register(rule)` | Register a rule      |
| `get(name)`      | Fetch or `null`      |
| `require(name)`  | Fetch or throw       |
| `has(name)`      | Check existence      |
| `names()`        | All registered names |
| `reset()`        | Restore built-ins    |

**Built-in rules**: `EMAIL`, `PHONE_INTL`, `PK_MOBILE`, `PK_CNIC`,
`USERNAME`, `STRONG_PASSWORD`, `URL`, `UUID`, `HEX_COLOR`, `PK_LANDLINE`,
`PK_POSTAL_CODE`, `PK_PASSPORT`, `PK_NTN`, `PK_IBAN`, `PK_VEHICLE_PLATE`.

### `Validate` — multi-error validator

```java
ValidationResult r = Validate.that(input)
        .notBlank("Name is required")
        .minLength(3, "At least 3 characters")
        .maxLength(50, "At most 50 characters")
        .validate();

if (r.hasErrors()) {
    r.errors().forEach(System.err::println);
}
```

**Labeled fields**:

```java
ValidationResult r = Validate.of("Email", email)
        .notBlank("required")
        .isEmail("invalid")
        .validate();
// error: "Email: invalid"
```

### `ValidationResult`

```java
result.hasErrors();     // boolean
result.isValid();       // boolean
result.errors();        // List<String>
result.firstError();    // String or null
result.count();         // int
result.join("; ");      // String
```

---

## `chirp.number` — numeric utilities

### `Numbers`

| Category | Methods                                                                      |
| -------- | ---------------------------------------------------------------------------- |
| Random   | `random(min, max)`, `randomDouble`, `randomBoolean`, `randomItem(arr)`       |
| Clamp    | `clamp(v, min, max)`                                                         |
| Parity   | `isEven`, `isOdd`                                                            |
| Prime    | `isPrime`, `primesUpTo`, `nextPrime`                                         |
| Math     | `factorial`, `fibonacci`, `gcd`, `lcm`                                       |
| Digits   | `sumDigits`, `countDigits`, `reverseDigits`                                  |
| Base     | `toBinary`, `toHex`, `toOctal`, `fromBinary`, `fromHex`                      |
| Format   | `round`, `format`, `abbreviate`, `thousands`, `fileSize`, `ordinal`, `roman` |
| Parse    | `parseInt`, `parseDouble`                                                    |
| Range    | `range(start, end)`, `rangeInclusive`                                        |
| Stats    | `statistics(arr)`, `median`, `mode`, `stdDev`, `variance`                    |

### `NumberChain` — fluent

```java
int result = Numbers.of(10)
        .add(5)
        .times(2)
        .clamp(0, 20)
        .asInt();       // 20
```

---

## `chirp.collection` — arrays, lists, search

### `ArraysUtil` — 70+ methods

**Aggregate** — `sum`, `average`, `max`, `min`, `count`
**Search** — `contains`, `indexOf`, `lastIndexOf`, `hasDuplicates`
**Transform** — `reverse`, `sorted`, `sortedDescending`, `distinct`,
`rotateRight`, `rotateLeft`, `shuffle`, `subArray`, `take`, `drop`,
`concat`, `filter`, `map`
**Set** — `intersection`, `union`, `difference`
**Matrix** — `transpose`, `row`, `col`, `sumRow`, `sumCol`, `diagonal`,
`antiDiagonal`, `flatten`
**Create** — `intArray(size, value)`, `matrix(rows, cols, value)`, `zeros`,
`ones`, `rangeArray(start, end)`

Every method null-safe, empty-safe.

### `ArrayChain` — fluent

```java
int[] result = ArraysUtil.of(5, 3, 8, 1, 9)
        .filter(v -> v > 2)
        .sorted()
        .take(3)
        .toArray();
// {3, 5, 8}
```

### `Lists`

`sum`, `average`, `max`, `min`, `sorted`, `reverse`, `distinct`, `join`,
`toArray`, `fromArray`, `print`.

### `Filter`

`where(items, predicate)` / `filter` / `keep` / `select` / `all`
`reject` / `exclude` / `not` / `removeIf`
`where(items, extractor, predicate)`
`nonNull`, `distinct`, `anyMatch`, `allMatch`, `noneMatch`, `count`
`partition`, `groupBy`, `merge`, `flatten`

### `Search`

`contains` (case-insensitive) / `containsStrict`
`startsWith` / `endsWith`
`matches` (regex) / `find`
`wholeWord` (bounded)
`fuzzy(text, query, maxDistance)` / `levenshtein`
`filter(items, query)` / `search`

Composite:

```java
Search.matcher(text, query,
        Search.Key.CASE_SENSITIVE, true,
        Search.Key.WHOLE_WORD, true);
```

### `Pagination` / `Page`

```java
Page<Student> first = Pagination.paginate(students, 0, 10);
Page<Student> last = Pagination.lastPage(students, 10);
List<Page<Student>> all = Pagination.allPages(students, 10);
```

`Page` has `content()`, `page()`, `pageNumber()`, `totalPages()`,
`hasNext()`, `hasPrevious()`, `isFirst()`, `isLast()`.

---

## `chirp.datetime` — dates and times

### `DateTime` — static helpers

**Current** — `today()`, `yesterday()`, `tomorrow()`, `now()`,
`nowDate()`, `nowTime()`

**Parse** — `parseDate(text)`, `parseTime(text)`, `parseDateTime(text)` —
tries all common formats. Returns `null` on failure (never throws).

**Format** — `format(date, pattern)`, `humanize(date)`, `humanizeShort`,
`humanizeTime`, `humanizeDateTime`, `formatISO`

**Arithmetic** — `addDays`, `subtractDays`, `addMonths`, `addYears`,
`addBusinessDays`

**Snapping** — `startOfWeek`, `endOfWeek`, `startOfMonth`, `endOfMonth`,
`startOfYear`, `endOfYear`, `startOfQuarter`, `endOfQuarter`

**Diff** — `daysBetween`, `weeksBetween`, `monthsBetween`, `yearsBetween`,
`businessDaysBetween`, `age(birth)`

**Validity** — `isValidDate`, `isValidTime`, `isLeapYear`

**Compare** — `isBefore`, `isAfter`, `isSameDay`, `isSameMonth`,
`isSameYear`, `isSameWeek`

**Relative** — `timeAgo(date)` → "5m ago", "2h ago", "yesterday"

**Weekend** — `isWeekend`, `isWeekday`, custom weekend via key-value

### `DateChain` — fluent

```java
LocalDate eom = DateTime.of(today)
        .addMonths(1)
        .endOfMonth()
        .asDate();
```

---

## `chirp.format` — formatting

### `Format` — static helpers

**Interpolation** — `fmt(template, kv...)`
**Session** — `fmtWithSession(template)`
**Currency** — `currency(amount)`, `rupees(amount)`, `currencyOf(amount, symbol)`
**Percent** — `percent(value)`, `percent(value, places)`
**Decimal** — `decimal(value, places)`
**Plural** — `plural(count, singular, plural)`, `quantify`, `items`,
`files`, `lines`, `chars`
**List** — `list(items, delimiter)`, `list(items, delimiter, lastDelimiter)`
**Ordinal** — `ordinal(n)` → "1st"
**Roman** — `roman(n)` → "IV"
**Bytes** — `bytes(n)` → "1.5 KB"
**Duration** — `duration(millis)` → "1h 5m 30s"
**Domain** — `cnic("3520212345671")` → "35202-1234567-1",
`pakistaniPhone("0300-1234567")` → "+92 300 1234567"

**Named placeholders**:

```java
Format.fmt("Hello, {name}! You are {age}.", "name", "Ali", "age", 20);
```

### `Formatter` — fluent

```java
String message = Formatter
        .of("Dear {name}, order #{id} was {amount}, shipped {date}.")
        .with("name", "Saad")
        .with("id", 42)
        .with("amount", 1234.5)
        .with("date", LocalDate.now())
        .padLeft("id", 6, '0')
        .currency("amount")
        .date("date", "MMM d, yyyy")
        .format();
// "Dear Saad, order #000042 was $1,234.50, shipped Oct 31, 2026."
```

---

## `chirp.io` — files

### `FileIO` — simple file operations

`readAll`, `readLines`, `write`, `append`, `exists`, `delete`, `copy`,
`listFiles`, `countLines`, `createDirectory`, `deleteDirectory`,
`countFiles`, `directorySize`, `findFiles`, `findFilesByExtension`,
`copyDirectory`, `moveDirectory`, `rename`.

### `TextFileIO` — full text file modes

`readAll`, `readLines`, `head`, `tail`, `firstLine`, `lastLine`, `line(i)`,
`slice(from, to)`, `write`, `writeLines`, `append`, `appendLine`,
`prepend`, `grep`, `lineNumbersContaining`, `replaceInFile`, `countLines`,
`countWords`, `countChars`, `sizeInBytes`, `humanSize`, `exists`, `delete`,
`copy`, `move`.

### `TextFile` — fluent wrapper

```java
TextFile f = TextFile.of("data.txt");
f.write("hello");
f.appendLine("world");
List<String> lines = f.readLines();
int count = f.countLines();
```

### `CsvFile` — RFC 4180 CSV

```java
List<List<String>> rows = CsvFile.read("data.csv");
List<Map<String, String>> records = CsvFile.readAsRecords("data.csv");
CsvFile.CsvTable table = CsvFile.table("data.csv");

table.column("name");                              // all names
table.filter(r -> "A".equals(r.get("grade")));     // filter rows
table.sortBy("name");                              // sort

CsvFile.write("out.csv", rows);
CsvFile.append("out.csv", List.of("a", "b"));
CsvFile.writeRecords("out.csv", records, headers);
```

### `JsonFile` — JSON files

`read(path)`, `readObject(path)`, `readArray(path)`,
`write(path, value)`, `writePretty`, `writeCompact`, `isValid(path)`,
`merge(path1, path2)`.

---

## `chirp.json` — JSON

### `Json` — the facade

```java
JsonValue v = Json.parse(text);
JsonObject o = Json.parseObject(text);
JsonArray a = Json.parseArray(text);

JsonObject built = Json.object()
        .put("name", "Saad")
        .put("age", 20)
        .put("active", true);

String compact = Json.write(built);
String pretty = Json.pretty(built);
boolean valid = Json.isValid(text);
```

### Types

- `JsonValue` — sealed interface
- `JsonObject` — `getString`, `getInt`, `getLong`, `getDouble`, `getBoolean`,
  `getObject`, `getArray`, plus `getOrDefault(key, default)` overloads
- `JsonArray` — `get(index)`, `add(value)`, `size()`
- `JsonString` / `JsonNumber` / `JsonBoolean` / `JsonNull`

All types are null-safe and type-checked.

---

## `chirp.http` — HTTP client

### `Http` — factory

```java
HttpRequest r = Http.get("https://api.example.com")
        .header("Accept", "application/json")
        .bearer("token")
        .timeoutSeconds(10)
        .withSpinner()
        .send();
```

### `HttpRequest` — fluent

**Configuration** — `url`, `header`, `body`, `json`, `bearer`, `basicAuth`,
`query`, `timeoutSeconds`, `spinner`, `verbose`, `saveRaw`, `listener`

**Send** — `send()` (graceful), `sendOrThrow()` (throws),
`sendAsync()` (CompletableFuture), `sendWithRetry(attempts)`

### `HttpResponse` — read result

`isSuccess()`, `isError()`, `status()`, `body()`, `text()`, `json()`,
`asObject()`, `header(name)`, `durationMillis()`, `summary()`

### Connectivity

```java
Http.isOnline();        // best-effort DNS check
Http.canReach(host, port);
Http.ping(url);
```

### Listener

```java
Http.get(url).listener(new HttpListener() {
    @Override public void onStart(HttpRequest r) { }
    @Override public void onSuccess(HttpResponse r) { }
    @Override public void onFailure(HttpRequest r, Throwable t) { }
}).send();
```

---

## `chirp.mail` — email / SMTP

### `Email` — factory + key-value

```java
EmailResult result = Email.builder()
        .host("smtp.gmail.com")
        .port(587)
        .useStarttls()
        .from("me@gmail.com")
        .to("you@example.com")
        .subject("Hello")
        .text("Plain body")
        .html("<p>HTML body</p>")
        .template("templates/welcome.html")
        .attach("path/to/file.pdf")
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

### `EmailRequest` — fluent

**Configuration** — `host`, `port`, `ssl`, `starttls`, `from`, `senderName`,
`to`, `cc`, `bcc`, `replyTo`, `subject`, `text`, `html`, `template`,
`signature`, `attach`, `attachDirectory`, `username`, `usernameFromEnv`,
`priority`, `header`

**Password** — `password(direct)`, `promptPassword()`,
`passwordFromEnv(file, key)`

**Send** — `send()`, `sendOrThrow()`, `sendWithRetry(attempts)`,
`previewMimeMessage()`, `previewIfValid()`

**Test** — `test()`, `testOrThrow()`

### `EmailResult`

`isSuccess()`, `isFailure()`, `message()`, `durationMs()`, `rawMessage()`,
`recipients()`, `smtpLog()`

### `HtmlTemplate` — template with strict validation

```java
HtmlTemplate t = HtmlTemplate.of("templates/welcome.html")
        .placeholder("name", "Saad")
        .placeholder("amount", Format.currency(1234.5))
        .placeholder("date", DateTime.humanize(today));

String html = t.render();    // throws if placeholder mismatched
```

Template supports `{{name}}` and `${name}`. Declared placeholders **must**
exist in the file, and every placeholder in the file **must** be declared —
otherwise `render()` throws `EmailException` with a clear message.

### `EnvLoader` — `.env` parser

```java
Map<String, String> env = EnvLoader.load(".env");
String pwd = EnvLoader.get(".env", "SMTP_PASSWORD");
```

Supports plain `KEY=value`, quoted `KEY="value"` with escapes, comments
`# ...`, blank lines. Throws `EmailException` on malformed lines.

---

## `chirp.decorator` — retry, timing, caching

### `Decorator`

```java
Duration d = Decorator.timed(() -> longTask());
Timed<Integer> t = Decorator.timed(() -> expensive());

Optional<String> result = Decorator.safe(() -> riskyOperation());
boolean ok = Decorator.safe(() -> riskyVoidOperation());

String cached = Decorator.cached("key", () -> expensiveCompute());
Decorator.invalidate("key");
Decorator.clearCache();

Decorator.logged("tag", () -> action());
String value = Decorator.logged("tag", () -> compute());

Integer n = Decorator.withFallback(() -> riskyInt(), 0);
```

### `Retry`

```java
// Simple — 3 attempts, 500ms fixed backoff
String result = Decorator.retry(3, () -> fetch());

// Full control
Retry.builder()
    .attempts(5)
    .backoffMs(250)
    .exponential()
    .jitter(0.1)
    .onlyOn(IOException.class)
    .label("network call")
    .build()
    .execute(() -> fetch());
```

### `Timed` — result wrapper

```java
Timed<Integer> t = Decorator.timed(() -> compute());
t.value();       // the result
t.millis();      // duration
```

---

## `chirp.session` — user profile

```java
Session.me()
        .name("Saad Mansoor")
        .rollNumber("2024-CS-001")
        .batch("2024")
        .degree("BSAI")
        .section("A")
        .email("saad@example.com");

Session.me().print();          // rendered block
Session.me().summary();        // String
Session.me().asMap();          // Map
Session.me().clear();
```

`Console.watermark()` prints the Session block — useful for screenshot
watermarks.

---

## `chirp.ui` — JavaFX toolkit

### `Ui` — entry point

```java
Ui.app(() -> Ui.show("Title", rootComponent));
```

- `show(title, root)` — show a window (must be on FX thread)
- `show(UiApp)` — show a pre-built window
- `alert(info/warning/error/success)` — dialogs
- `confirm(title, message)` — yes/no dialog
- `runAsync(task, onComplete)` — run on background thread
- `runOnUiThread(action)` — schedule on FX thread

### `UiApp` — window builder

```java
UiApp app = UiApp.builder()
        .title("App")
        .size(1024, 768)
        .theme(UiTheme.DARK)
        .content(root)
        .onClose(() -> cleanup())
        .build();

Ui.show(app);
```

### `UiTheme` — LIGHT / DARK / AUTO

```java
UiApp.builder().theme(UiTheme.DARK).build();
```

`AUTO` resolves at `Ui.show()` time; override with `-Dchirp.theme=dark`.

### `UiStyle` — global style

```java
UiStyle.apply(
        UiStyle.Key.FONT_SIZE, 16,
        UiStyle.Key.PRIMARY_COLOR, "#8b5cf6");

UiStyle.modernLight();
UiStyle.modernDark();
UiStyle.compact();
UiStyle.largeText();
UiStyle.monospace();
```

### `Field` — composite field

```java
Field name = Field.of("Name").required().minLength(2).maxLength(50);
Field email = Field.of("Email").email();
Field pwd = Field.password("Password").minLength(8).hint("At least 8 chars");
Field bio = Field.textarea("Bio").rows(4);

String value = name.value();
boolean valid = name.isValid();
String error = name.error();
```

### Components (all `chirp.ui.*`)

`Label`, `Button`, `TextField`, `PasswordField`, `TextArea`, `Field`,
`Checkbox`, `Select` (searchable), `RadioGroup`, `Slider`, `Spinner`,
`DatePicker`, `TimePicker`, `FileField`, `ColorPicker`, `Hyperlink`,
`ProgressBar`, `ProgressIndicator`, `Separator`, `ImageView`, `Table`,
`TabPane`, `Accordion`, `TitledPane`, `ListView`, `TreeView`,
`TreeTableView`, `MenuBar`, `ToolBar`, `SplitPane`, `Pagination`,
`PieChart`, `BarChart`, `LineChart`, `AreaChart`, `ScatterChart`,
`StackedBarChart`, `ToggleButton`, `MenuButton`, `SplitMenuButton`,
`ContextMenu`, `Tooltip`, `SearchField`, `Rating`, `Badge`, `Chip`,
`Card`, `Breadcrumb`, `KeyBindings`, `Stack`, `Row`.

See [JavaFX Guide](JAVAFX_GUIDE.md) for details.

### `FormBuilder` — declarative forms

```java
Stack form = FormBuilder.create("Register")
        .field("name", Field.of("Name").required())
        .field("email", Field.of("Email").email())
        .submit("Register", values -> success("Registered: " + values.get("name")))
        .reset("Clear")
        .build();
```

### `UiIntegration` — bridge to validation

```java
Field f = UiIntegration.bindRule(Field.of("CNIC"), "PK_CNIC");
Stack errors = UiIntegration.errorsFrom(validationResult);
```

---

## `chirp.config` — configuration

Single entry point for colour, debug, strict warnings, stats, and trace.

```java
Config.colorEnabled(false);         // disable ANSI colour
Config.debug(true);                 // enable debug logs
Config.strictWarnings(false);       // silence [STRICT] hints
Config.stats(false);                // disable run summary
Config.trace(true);                 // enable internal tracing
Config.reset();                     // restore defaults
```

System properties override: `-Dchirp.ansi`, `-Dchirp.strict`, `-Dchirp.stats`,
`-Dchirp.trace`.

---

## `chirp.startup` — lifecycle

### `ChirpLifecycle`

Static lifecycle gate. Called indirectly via `Chirp.start()`.

- `isStarted()` — whether started
- `requireStarted()` — throws `ChirpNotStartedException` if not started

### `StartupConfig`

```java
StartupConfig.defaults();                   // banner, stats, colour
StartupConfig.silent();                     // no banner, stats, hints

StartupConfig.builder()
        .noHeader()
        .withStats()
        .noColor()
        .noUnicode()
        .headerText("CS-102")
        .build();
```

### `Banner`

Auto-rendered on `Chirp.start()`. Uses Unicode box-drawing characters with
graceful ASCII fallback.

---

## `chirp.stats` — runtime statistics

```java
Stats.enable();                     // enable (default when start() called)
Stats.reportNow();                  // print immediately
Stats.reset();

Stats.StatsSnapshot snap = Stats.snapshot();
snap.elapsedSeconds();
snap.heapUsedMegabytes();
snap.linesRead();

// Background sampling
StatsSampler sampler = ChirpLifecycle.sampler();
sampler.addListener(s -> println(s.heapUsedMegabytes() + " MB"));
```

---

## Common patterns

### Chained reading with validation

```java
String name = readString()
        .label("Name")
        .notBlank()
        .atLeast(3)
        .atMost(30)
        .skipFormatWarning()
        .read();
```

### Array transformation

```java
int[] result = arrayChain(nums)
        .filter(n -> n > 0)
        .sorted()
        .take(5)
        .toArray();
```

### HTTP + JSON

```java
HttpResponse r = Http.get("https://api.example.com/users")
        .withSpinner()
        .send();

if (r.isSuccess()) {
    JsonArray users = r.asJson().asArray();
    for (var u : users.asList()) {
        println(u.asObject().getString("name"));
    }
}
```

### Email with template

```java
HtmlTemplate t = HtmlTemplate.of("templates/welcome.html")
        .placeholder("name", user.getName())
        .placeholder("amount", Format.currency(balance));

EmailResult r = Email.builder()
        .host("smtp.gmail.com")
        .from("me@gmail.com")
        .to(user.getEmail())
        .subject("Welcome")
        .template(t)
        .passwordFromEnv(".env", "SMTP_PASSWORD")
        .send();
```

### Retry network call

```java
String data = Decorator.retry(3, () -> {
    HttpResponse r = Http.fetch(url);
    if (!r.isSuccess()) throw new RuntimeException("HTTP " + r.status());
    return r.text();
});
```

### Form validation

```java
ValidationResult result = Validate.that(email)
        .notBlank("Email is required")
        .isEmail("Invalid email")
        .validate();

if (result.hasErrors()) {
    result.errors().forEach(this::error);
}
```
