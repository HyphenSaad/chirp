# Arrays and Validation

Working with arrays (1D, 2D, 3D) and validating data.

---

## 1. Array basics

### Without Chirp

```java
int[] arr = {5, 3, 8, 1, 9, 2};

// Sum
int sum = 0;
for (int n : arr) sum += n;

// Max
int max = arr[0];
for (int i = 1; i < arr.length; i++) {
    if (arr[i] > max) max = arr[i];
}

// Min
int min = arr[0];
for (int i = 1; i < arr.length; i++) {
    if (arr[i] < min) min = arr[i];
}

// Reverse
int[] reversed = new int[arr.length];
for (int i = 0; i < arr.length; i++) {
    reversed[i] = arr[arr.length - 1 - i];
}

// Sort
int[] sorted = arr.clone();
java.util.Arrays.sort(sorted);

// Distinct
java.util.Set<Integer> set = new java.util.LinkedHashSet<>();
for (int n : arr) set.add(n);
int[] distinct = set.stream().mapToInt(Integer::intValue).toArray();

// Contains
boolean contains = false;
for (int n : arr) {
    if (n == 8) { contains = true; break; }
}
```

Every operation is a for-loop. `contains`, `indexOf`, `max`, `min` — all
require hand-written loops. Every student writes them differently.

### With Chirp

```java
import static chirp.Chirp.*;

Chirp.start();

int[] arr = {5, 3, 8, 1, 9, 2};

println("sum      = " + sum(arr));                   // 28
println("max      = " + max(arr));                   // 9
println("min      = " + min(arr));                   // 1
printArray(reverse(arr));                             // [2, 9, 1, 8, 3, 5]
printArray(sorted(arr));                              // [1, 2, 3, 5, 8, 9]
printArray(distinct(arr));                            // [5, 3, 8, 1, 9, 2] (already distinct)
println("contains 8 = " + contains(arr, 8));          // true
```

Every method is null-safe and empty-safe:

```java
sum(null);              // 0 (no exception)
max(new int[0]);        // 0 (no exception)
contains(null, 5);      // false
reverse(null);          // empty array
```

---

## 2. Array transformations

### Without Chirp

```java
int[] arr = {5, 3, 8, 1, 9, 2};

// Filter — keep values > 2
List<Integer> filtered = new ArrayList<>();
for (int n : arr) if (n > 2) filtered.add(n);
int[] result1 = filtered.stream().mapToInt(Integer::intValue).toArray();

// Map — multiply by 2
int[] result2 = new int[arr.length];
for (int i = 0; i < arr.length; i++) result2[i] = arr[i] * 2;

// Take first 3
int[] result3 = new int[Math.min(3, arr.length)];
System.arraycopy(arr, 0, result3, 0, result3.length);

// Drop first 2
int[] result4 = new int[arr.length - 2];
System.arraycopy(arr, 2, result4, 0, result4.length);

// Rotate right by 2
int n = arr.length;
int shift = 2 % n;
int[] result5 = new int[n];
for (int i = 0; i < n; i++) {
    result5[(i + shift) % n] = arr[i];
}
```

### With Chirp — static

```java
import static chirp.Chirp.*;

int[] arr = {5, 3, 8, 1, 9, 2};

int[] filtered = filter(arr, n -> n > 2);
int[] mapped   = map(arr, n -> n * 2);
int[] taken    = take(arr, 3);
int[] dropped  = drop(arr, 2);
int[] rotated  = rotateRight(arr, 2);
```

### With Chirp — fluent chain

```java
int[] result = arrayChain(arr)
        .filter(n -> n > 2)
        .sorted()
        .take(3)
        .toArray();
// {3, 5, 8}
```

The chain is **immutable** — the original array is unchanged.

---

## 3. Set operations

### Without Chirp

```java
int[] a = {1, 2, 3, 4, 5};
int[] b = {3, 4, 5, 6, 7};

// Intersection
Set<Integer> setB = new HashSet<>();
for (int v : b) setB.add(v);
List<Integer> intersection = new ArrayList<>();
for (int v : a) if (setB.contains(v)) intersection.add(v);

// Union
Set<Integer> union = new LinkedHashSet<>();
for (int v : a) union.add(v);
for (int v : b) union.add(v);

// Difference
Set<Integer> setA = new LinkedHashSet<>();
for (int v : a) setA.add(v);
for (int v : b) setA.remove(v);
```

### With Chirp

```java
import static chirp.Chirp.*;

int[] a = {1, 2, 3, 4, 5};
int[] b = {3, 4, 5, 6, 7};

printArray(intersection(a, b));   // [3, 4, 5]
printArray(union(a, b));          // [1, 2, 3, 4, 5, 6, 7]
printArray(difference(a, b));     // [1, 2]
```

---

## 4. 2D matrices

### Without Chirp

```java
int[][] grades = {
        {80, 90, 85},
        {70, 75, 95},
        {88, 92, 78}
};

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
for (int r = 0; r < grades.length; r++) {
    colSum += grades[r][1];
}

// Main diagonal
int[] diag = new int[Math.min(grades.length, grades[0].length)];
for (int i = 0; i < diag.length; i++) {
    diag[i] = grades[i][i];
}

// Flatten
List<Integer> flat = new ArrayList<>();
for (int[] row : grades) {
    for (int v : row) flat.add(v);
}

// Print nicely
for (int[] row : grades) {
    for (int v : row) {
        System.out.printf("%4d", v);
    }
    System.out.println();
}
```

### With Chirp

```java
import static chirp.Chirp.*;

int[][] grades = {
        {80, 90, 85},
        {70, 75, 95},
        {88, 92, 78}
};

printMatrix(grades);

int[][] t = transpose(grades);
printMatrix(t);

int rowSum = sumRow(grades, 0);     // 255
int colSum = sumCol(grades, 1);     // 257

int[] diag = diagonal(grades);      // {80, 75, 78}
printArray(diag);

int[] flat = flatten(grades);       // {80, 90, 85, 70, 75, 95, 88, 92, 78}
printArray(flat);
```

### Output

```
[80, 90, 85]
[70, 75, 95]
[88, 92, 78]

[80, 70, 88]
[90, 75, 92]
[85, 95, 78]

[80, 75, 78]
[80, 90, 85, 70, 75, 95, 88, 92, 78]
```

---

## 5. 3D arrays

### Without Chirp

```java
int[][][] cube = new int[3][3][3];
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        for (int k = 0; k < 3; k++) {
            cube[i][j][k] = 7;
        }
    }
}
```

### With Chirp

```java
import static chirp.Chirp.*;

int[][][] cube = ArraysUtil.cube(3, 7);   // 3×3×3 filled with 7

ArraysUtil.printCube(cube);
```

### Output

```
Layer 0:
[7, 7, 7]
[7, 7, 7]
[7, 7, 7]
Layer 1:
[7, 7, 7]
...
```

---

## 6. Simple validations with `Check`

### Without Chirp

```java
// Email
boolean isEmail = email != null
        && email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

// Phone
boolean isPhone = phone != null && phone.matches("^\\+?[0-9]{7,15}$");

// Pakistani CNIC (13 digits, province 1-8)
boolean isCnic = cnic != null
        && cnic.replaceAll("\\D", "").matches("^[1-8]\\d{12}$");

// Strong password
boolean isStrong = pwd != null
        && pwd.length() >= 8
        && pwd.matches(".*[A-Z].*")
        && pwd.matches(".*[a-z].*")
        && pwd.matches(".*[0-9].*");
```

You have to look up the regex every time.

### With Chirp

```java
import static chirp.Chirp.*;

if (isEmail(email))           { success("Email OK"); }
if (isPhone(phone))           { success("Phone OK"); }
if (isPakistaniCnic(cnic))    { success("CNIC OK"); }
if (isStrongPassword(pwd))    { success("Password OK"); }
if (isURL(website))           { success("URL OK"); }
if (isIPAddress(ip))          { success("IP OK"); }
if (isUUID(id))               { success("UUID OK"); }
```

Every method returns `false` for `null` — no `NullPointerException`.

---

## 7. Composable rules with `Rule`

### Without Chirp

```java
// "Pakistani mobile" — multiple formats accepted
boolean isPkMobile(String s) {
    if (s == null) return false;
    String cleaned = s.replaceAll("[\\s\\-()]", "");
    return cleaned.matches("^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$");
}
```

### With Chirp — define once, use everywhere

```java
import chirp.validation.Rule;

Rule pkMobile = Rule.define("PK_MOBILE")
        .description("Pakistani mobile number")
        .stripAll("[\\s\\-()]")
        .matches("^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$")
        .message("Enter a valid Pakistani mobile number")
        .build();

if (pkMobile.test("0300-1234567")) { success("Valid"); }
if (pkMobile.test("+92 300 1234567")) { success("Valid"); }
if (!pkMobile.test("123")) { error("Invalid"); }
```

### Composite rules

```java
import chirp.validation.Rule;

Rule minLength = Rule.define("MIN_LEN").minLength(3).message("too short").build();
Rule noDigits = Rule.define("NO_DIGITS").matches("[^0-9]+").message("no digits").build();

// AND — must satisfy both
Rule combined = Rule.allOf(minLength, noDigits);
combined.test("abc");           // true
combined.test("ab");            // false (too short)
combined.test("abc1");          // false (has digit)

// OR — must satisfy at least one
Rule either = Rule.anyOf(minLength, noDigits);
either.test("ab");              // true (satisfies noDigits)
either.test("123");             // true (satisfies minLength)

// NOT
Rule notDigits = Rule.not(noDigits);
notDigits.test("abc");          // true
notDigits.test("123");          // false
```

### Register a rule for reuse

```java
import chirp.validation.Rules;

// Register once
Rules.register(Rule.define("EMPLOYEE_ID")
        .stripNonDigits()
        .matches("^\\d{6}$")
        .message("Employee ID must be 6 digits")
        .build());

// Use by name anywhere
Check.is("123456", "EMPLOYEE_ID");             // true
Check.is("123", "EMPLOYEE_ID");                // false

// Or in a fluent reader
String id = readString()
        .label("Employee ID")
        .rule("EMPLOYEE_ID")
        .notBlank()
        .skipLengthWarning()
        .read();
```

### Built-in rules

All of these are already registered by default:

```java
Rules.email().test("user@example.com");            // true
Rules.pakistaniMobile().test("0300-1234567");      // true
Rules.pakistaniCnic().test("35202-1234567-1");     // true
Rules.url().test("https://example.com");           // true
Rules.strongPassword().test("Abc12345!");          // true
```

---

## 8. Multi-error validation with `Validate`

### Without Chirp

```java
List<String> errors = new ArrayList<>();

if (name == null || name.isBlank()) {
    errors.add("Name is required");
}
if (name != null && (name.length() < 3 || name.length() > 50)) {
    errors.add("Name must be 3-50 characters");
}
if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
    errors.add("Invalid email");
}
if (age < 1 || age > 130) {
    errors.add("Age must be 1-130");
}

if (!errors.isEmpty()) {
    for (String err : errors) System.err.println(err);
    return;
}
```

You write one `if` per rule, one `errors.add` per rule, and the same
"collect all errors" logic every time.

### With Chirp — single field

```java
import chirp.validation.ValidationResult;
import chirp.validation.Validate;

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

### With Chirp — multiple fields, labeled

```java
ValidationResult nameResult = Validate.of("Name", name)
        .notBlank("required")
        .minLength(3, "must be at least 3 characters")
        .validate();

ValidationResult emailResult = Validate.of("Email", email)
        .notBlank("required")
        .isEmail("invalid format")
        .validate();

// Merge all errors
ValidationResult combined = nameResult.merge(emailResult);

if (combined.hasErrors()) {
    combined.errors().forEach(this::error);
    return;
}
```

### Output

```
[ERROR] Name: required
[ERROR] Email: invalid format
```

Every error is prefixed with its field name automatically.

### Validation methods

| Method                              | Description             |
| ----------------------------------- | ----------------------- |
| `.notBlank(message)`                | Reject blank            |
| `.notNull(message)`                 | Reject null             |
| `.minLength(n, message)`            | Minimum length          |
| `.maxLength(n, message)`            | Maximum length          |
| `.lengthBetween(min, max, message)` | Range of lengths        |
| `.matches(regex, message)`          | Regex match             |
| `.isEmail(message)`                 | Email format            |
| `.isPhone(message)`                 | Phone format            |
| `.isNumeric(message)`               | Numeric format          |
| `.between(min, max, message)`       | Numeric range           |
| `.min(n, message)`                  | Numeric minimum         |
| `.max(n, message)`                  | Numeric maximum         |
| `.oneOf(message, options...)`       | One of given values     |
| `.is(ruleName, message)`            | Apply a registered rule |
| `.against(rule, message)`           | Apply a Rule object     |
| `.satisfies(predicate, message)`    | Custom predicate        |
| `.custom(condition, message)`       | Custom boolean check    |
| `.addError(message)`                | Manually add an error   |
| `.validate()`                       | Return the result       |

---

## 9. Complete example — Grade processor

A program that reads student scores, computes statistics, and validates
each score.

```java
import static chirp.Chirp.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Chirp.start();

        List<Integer> scores = new ArrayList<>();

        println("Enter student scores (0 to 100). Type -1 to finish.\n");

        while (true) {
            int score = readInt("Score", -1, 100);
            if (score == -1) break;
            scores.add(score);
        }

        if (scores.isEmpty()) {
            warn("No scores entered.");
            return;
        }

        int[] arr = Lists.toArray(scores);

        // Statistics
        title("Statistics");
        table(
                List.of(
                        new String[]{"Count",     String.valueOf(arr.length)},
                        new String[]{"Sum",       String.valueOf(sum(arr))},
                        new String[]{"Average",   Format.decimal(average(arr), 2)},
                        new String[]{"Maximum",   String.valueOf(max(arr))},
                        new String[]{"Minimum",   String.valueOf(min(arr))},
                        new String[]{"Median",    Format.decimal(Numbers.median(arr), 1)},
                        new String[]{"Std. dev",  Format.decimal(Numbers.stdDev(arr), 2)}),
                "Metric", "Value");

        // Grade distribution
        println();
        title("Grade Distribution");

        int a = countScores(arr, 90, 100);
        int b = countScores(arr, 80, 89);
        int c = countScores(arr, 70, 79);
        int d = countScores(arr, 60, 69);
        int f = countScores(arr, 0, 59);

        showBar("A (90-100)", a, arr.length);
        showBar("B (80-89)",  b, arr.length);
        showBar("C (70-79)",  c, arr.length);
        showBar("D (60-69)",  d, arr.length);
        showBar("F (0-59)",   f, arr.length);

        // Status messages
        println();
        double avg = average(arr);
        if (avg >= 90) success("Class average: excellent (" + Format.decimal(avg, 1) + ")");
        else if (avg >= 70) info("Class average: good (" + Format.decimal(avg, 1) + ")");
        else if (avg >= 50) warn("Class average: needs work (" + Format.decimal(avg, 1) + ")");
        else error("Class average: critical (" + Format.decimal(avg, 1) + ")");
    }

    private static int countScores(int[] arr, int min, int max) {
        int count = 0;
        for (int v : arr) {
            if (v >= min && v <= max) count++;
        }
        return count;
    }

    private static void showBar(String label, int count, int total) {
        if (total == 0) return;
        int width = (int) ((double) count / total * 30);
        String bar = "#".repeat(width);
        printf("%-15s %-30s (%d)%n", label, bar, count);
    }
}
```

### Sample session

```
========================================
  C H I R P
========================================

Enter student scores (0 to 100). Type -1 to finish.

Score: 85
Score: 92
Score: 78
Score: 95
Score: 88
Score: 72
Score: -1

========================================
  Statistics
========================================
Metric      Value
--------------------------------------------------
Count       6
Sum         510
Average     85.00
Maximum     95
Minimum     72
Median      86.5
Std. dev    8.35

========================================
  Grade Distribution
========================================
A (90-100)      ########                        (2)
B (80-89)       ############                    (3)
C (70-79)       ####                            (1)
D (60-69)                                       (0)
F (0-59)                                        (0)

ℹ Class average: good (85.0)
```

A program that would take 150+ lines in standard Java takes 80 with
Chirp — and the 80 lines are all logic, not boilerplate.
