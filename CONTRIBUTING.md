# Contributing to Chirp

Thank you for your interest in improving Chirp! This project aims to keep
Java simple and friendly for everyone — from complete beginners writing
their first line of code to experienced developers building production tools.

Following these guidelines helps keep the library consistent, maintainable,
and pleasant to use.

---

## Table of contents

- [Code of Conduct](#code-of-conduct)
- [Before you contribute](#before-you-contribute)
- [Ways to contribute](#ways-to-contribute)
- [Development setup](#development-setup)
- [Contribution workflow](#contribution-workflow)
- [Coding standards](#coding-standards)
- [Testing](#testing)
- [Commit messages](#commit-messages)
- [Pull request process](#pull-request-process)
- [Reporting bugs](#reporting-bugs)
- [Requesting features](#requesting-features)

---

## Code of Conduct

By participating in this project you agree to abide by our
[Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

---

## Before you contribute

Chirp has a **narrow scope by design**. Before opening an issue or PR, please
make sure your idea fits the project's goals:

### ✅ In scope

- Simplifying common Java pain points (I/O, arrays, validation, dates, etc.)
- Null-safe, exception-safe wrappers around standard-library behaviour
- Consistent aliasing so users can think in their natural vocabulary
- Zero-external-runtime-dependency implementations
- JavaFX component wrappers with pre-styled defaults

### ❌ Out of scope

- Adding external library dependencies (Apache Commons, Guava, etc.)
- Replacing or hiding fundamental Java concepts from learners
- Features that require native libraries
- Domain-specific business logic (custom serializers, specific APIs)
- Anything that would make the library _harder_ to read or use

If in doubt, open an issue to discuss first.

---

## Ways to contribute

- **Report bugs** — see [Reporting bugs](#reporting-bugs)
- **Request features** — see [Requesting features](#requesting-features)
- **Improve documentation** — fix typos, clarify examples
- **Add tests** — increase coverage of edge cases
- **Fix bugs** — start with issues labelled `good first issue`
- **Add new methods** — follow the API standards below
- **Improve JavaFX theme** — CSS contributions welcome

---

## Development setup

### Prerequisites

- **JDK 17** or later
- **Maven 3.9+**
- **Git**

### Clone and build

```bash
git clone https://github.com/hyphensaad/chirp.git
cd chirp
mvn clean package
```

### Verify everything works

```bash
mvn verify
```

This runs:

- Compilation with `-Xlint:all -Werror` (fails on warnings)
- 500+ unit tests
- Spotless formatting check
- SpotBugs static analysis
- PMD static analysis
- JaCoCo coverage report

### Auto-format before committing

```bash
mvn spotless:apply
```

This formats all Java files to the project's standard.

### IDE setup

**IntelliJ IDEA:**

1. **File → Open** → select the `pom.xml` (Maven auto-detected)
2. Install the **Eclipse Code Formatter** plugin
3. **Settings → Eclipse Code Formatter** → import `config/eclipse-formatter.xml`

**VS Code:**

The project's `.vscode/settings.json` already configures the formatter.
Install the **Extension Pack for Java**.

---

## Contribution workflow

1. **Fork** the repository on GitHub
2. **Clone** your fork locally
3. **Create a branch**: `git checkout -b feature/my-feature` (or `fix/my-fix`)
4. **Make changes** following [Coding standards](#coding-standards)
5. **Add tests** for new behaviour
6. **Run**: `mvn verify` (must pass)
7. **Commit** with a clear message (see [Commit messages](#commit-messages))
8. **Push** to your fork
9. **Open a pull request** against `main`

---

## Coding standards

### Ground rules

1. **JDK 17 only.** No Java 18+ syntax, no pattern-matching switch, no sealed
   records. The build targets `release 17`.
2. **Zero runtime dependencies** (except JavaFX for the UI module). All
   functionality must come from the JDK.
3. **No public method may throw a JDK exception for user input mistakes.**
   Catch it, log a friendly message, return a safe default.
4. **No public method in `chirp.console.Input` may return `null`.** The
   re-prompting loop guarantees a valid value.
5. **Every public method must be null-safe** unless documented otherwise.
6. **Every public method must be unit-tested.**

### Naming conventions

| Concept         | Convention             | Example                     |
| --------------- | ---------------------- | --------------------------- |
| Read input      | `readXxx`              | `readInt`, `readEmail`      |
| Check (boolean) | `isXxx` / `hasXxx`     | `isEmail`, `hasLength`      |
| Transform       | verb or noun           | `reverse`, `sum`            |
| Print           | `printXxx`             | `printArray`, `printMatrix` |
| Format          | `fmt`, `currency`      | `fmt`, `currency`           |
| Fluent builder  | `.add`, `.of`, `.that` | `Menu.add`, `Validate.that` |

### Aliases

Where natural English has multiple spellings or synonyms, provide **2–4
aliases**. All aliases must have identical behaviour.

```java
// Good — three names for the same operation
public static int sum(int[] a)    { ... }
public static int total(int[] a)  { return sum(a); }
public static int sumOf(int[] a)  { return sum(a); }
```

Each alias should be a pure delegation — no duplicated logic.

### Fluent APIs

Every setter in a fluent chain must return `this` with the correct
covariant return type:

```java
public Button text(String t) { setText(t); return this; }
```

Terminal operations (methods that return a value) should not be fluent.

### Key-value APIs

Multi-option methods accept alternating enum-key / value pairs:

```java
public static Separator from(Object... keyValues) {
    Builder b = builder();
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
        if (!(keyValues[i] instanceof Key k)) continue;
        // ...
    }
    return b.build();
}
```

Handle gracefully: missing values, wrong types, and unknown keys must all be
silently ignored (not throw).

### Error handling

```java
// Good — safe default, no exception escapes
public static String readAll(String path) {
    try {
        return Files.readString(Path.of(path));
    } catch (IOException e) {
        Logger.error("Cannot read " + path);
        return "";
    }
}

// Bad — escapes to caller
public static String readAll(String path) throws IOException {
    return Files.readString(Path.of(path));
}
```

### Documentation

Every public method needs Javadoc with:

- A one-sentence summary
- `@param` for every parameter
- `@return` for the return value
- `@throws` only for library-thrown exceptions (rare)

```java
/**
 * Returns the sum of all elements in the array.
 *
 * @param array the input array; may be {@code null}
 * @return the sum, or {@code 0} if the array is {@code null} or empty
 */
public static int sum(int[] array) { ... }
```

### Comments

Use `//` for _why_ (rare). Avoid obvious `//` comments — the code should
speak for itself.

```java
// Good — explains non-obvious intent
// Spring Boot's ClassLoader conflicts with the JDK's ProcessBuilder on
// Windows, so we must use the fully qualified path.
Process p = new ProcessBuilder("cmd.exe", "/c", "cls");

// Bad — restates the code
int x = 5;   // set x to 5
```

---

## Testing

### Structure

- Each production class has a corresponding test class in the same package
  under `src/test/java/`.
- Tests use **JUnit 5** with `@Test`, `@ParameterizedTest`, and assertions.

### Coverage target

- **Every public method** must have at least one test.
- **Every branch** in complex methods must have a test.
- Edge cases (null, empty, negative, overflow) must be tested.

### Running tests

```bash
mvn test                                        # all tests
mvn test -Dtest=ArraysUtilTest                  # one class
mvn test -Dtest=ArraysUtilTest#sumBasic         # one method
mvn verify                                      # tests + quality gates
```

### Test naming

```java
@Test
void sumHandlesNull() { ... }

@Test
void sumHandlesEmpty() { ... }

@Test
void sumWithPositiveNumbers() { ... }
```

Use `someBehaviour_context_result` naming.

---

## Commit messages

Follow **Conventional Commits**:

```
<type>(<scope>): <subject>

<optional body>

<optional footer>
```

### Types

| Type       | Meaning                                                 |
| ---------- | ------------------------------------------------------- |
| `feat`     | New feature                                             |
| `fix`      | Bug fix                                                 |
| `docs`     | Documentation only                                      |
| `test`     | Adding or fixing tests                                  |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `perf`     | Performance improvement                                 |
| `chore`    | Build process, dependencies, tooling                    |
| `style`    | Code formatting (whitespace, missing semicolons)        |

### Examples

```
feat(string): add Text.slugify for URL-safe strings
fix(rules): correct CNIC province mapping for Sialkot
docs(readme): add before/after example for CSV reading
test(numbers): cover edge cases for factorial overflow
```

### Subject line rules

- **Imperative mood** — "add" not "added"
- **Lowercase** — "add feature" not "Add feature"
- **No period** at the end
- **Under 72 characters**

---

## Pull request process

### Before opening a PR

- [ ] Branch is up to date with `main` (`git pull upstream main`)
- [ ] `mvn verify` passes locally
- [ ] New behaviour has tests
- [ ] Public API additions have Javadoc
- [ ] No new dependencies added (except in `pom.xml` test scope)
- [ ] Commit messages follow Conventional Commits

### PR description template

```
## Summary

One-sentence description of what this PR does.

## Motivation

Why is this change needed? Reference any issue numbers.

## Changes

- Bullet list of specific changes
- ...

## Testing

How did you test this change?

## Breaking changes

Yes / No (if yes, describe migration path)

## Checklist

- [ ] `mvn verify` passes
- [ ] Tests added
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
```

### Review process

- At least one maintainer review required
- All CI checks must pass
- Reviewer may request changes; please respond to each comment
- Once approved, maintainer will merge

### Merging

- **Squash and merge** for feature branches
- The squash commit subject becomes the CHANGELOG entry
- Delete the branch after merging

---

## Reporting bugs

Open an issue using the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md).

Include:

1. **Chirp version** (from `chirp-1.0.0.jar` filename)
2. **Java version** (`java -version`)
3. **Operating system**
4. **Minimal reproducible example** (10 lines or fewer)
5. **Expected behaviour** vs **actual behaviour**
6. **Stack trace** if any

Before opening:

- Search existing issues
- Try the latest release
- Check [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)

---

## Requesting features

Open an issue using the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md).

Include:

1. **The problem you are trying to solve** (not just the solution)
2. **What you tried** in standard Java
3. **Why it was painful**
4. **A sketch of what the API could look like** (optional)
5. **Which module it belongs to** (Console, Text, Http, UI, etc.)

We will discuss the design before accepting a PR.

---

## Questions?

- Open a [Discussion](https://github.com/hyphensaad/chirp/discussions)
- Email the maintainer: `saad.mansoor@skt.umt.edu.pk`

---

Thank you for contributing to Chirp! 🐦
