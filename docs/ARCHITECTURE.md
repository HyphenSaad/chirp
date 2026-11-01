# Architecture

How Chirp is designed, and why.

---

## Table of contents

- [Guiding principles](#guiding-principles)
- [Layered structure](#layered-structure)
- [The facade](#the-facade)
- [Module independence](#module-independence)
- [Cross-cutting concerns](#cross-cutting-concerns)
- [Startup lifecycle](#startup-lifecycle)
- [Threading model](#threading-model)
- [Testing strategy](#testing-strategy)
- [Distribution](#distribution)

---

## Guiding principles

Chirp's architecture is shaped by seven principles, in this order of
priority. When principles conflict, the higher one wins.

### P1 — Zero-surprise API

A new user must be able to **guess** the method name and be right. If they
have to look it up, the API has failed.

Consequences:

- Method names match natural English (`readInt`, `sum`, `max`, `isEmail`)
- Aliases cover common synonyms (`readInteger` / `takeInt` / `getInt` /
  `askInt` / `inputInt`)
- Related methods live together (`Console`, `Input`, `Text`, `Check`)
- Return types are the most natural fit (`int`, `boolean`, `String` — never
  `Optional` for required reads)

### P2 — Never throw for user mistakes

If the user types "abc" when asked for a number, that is a user mistake,
not a library bug. The library should re-prompt, not throw
`NumberFormatException`.

Consequences:

- Every `Input` method loops until valid
- Every `FileIO` method returns a safe default on failure
- Every `Check` method returns `false` for `null`
- Every `Numbers.parseXxx` returns `null` on failure

The **only** unchecked exception the library throws is `EaseException` —
used for programming errors (e.g. stdin closed unexpectedly).

### P3 — Null-safe by default

No public method may throw `NullPointerException` for a `null` argument
unless the JDK itself does (e.g. `Objects.requireNonNull`).

Consequences:

- `Text.isBlank(null)` → `true`
- `Text.capitalize(null)` → `null`
- `ArraysUtil.sum(null)` → `0`
- `Check.isEmail(null)` → `false`
- `Console.println(null)` → prints `"null"`

### P4 — Progressive disclosure

The library is a **black box** for users, but a **glass box** for
maintainers. Every class is written as if it will be read — because some
users will read it.

Consequences:

- Every public method has Javadoc
- Every class has a package-level explanation
- Internal details are marked `internal`
- Source is shipped as a separate `-sources.jar`

### P5 — Fluent where it helps, static where it is simpler

Some operations are one-shot (`println`). Others need configuration
(`readInt().label("Age").between(1, 130).read()`). Use the right one.

Consequences:

- `Console.println` is static — no state to carry
- `IntReader`, `StringReader` are fluent — validate + retry is multi-step
- `Rule.define(...)` returns a `RuleBuilder` — the built `Rule` is
  immutable
- No fluent chain exceeds 5 calls without a line break

### P6 — Cross-platform, graceful degradation

Every feature works on Windows, macOS, and Linux. If a feature cannot be
supported, the library degrades gracefully.

Consequences:

- ANSI colours detected at startup; disabled on old Windows CMD
- Unicode detected via `chcp 65001` check; falls back to ASCII box-drawing
- File paths use `java.nio.Path` — no hardcoded separators
- Email encryption auto-detects: port 465 → SSL, port 587 → STARTTLS

### P7 — Zero external runtime dependencies

The JAR must be self-contained (except JavaFX for the UI module). No
Maven/Gradle setup required for users.

Consequences:

- JSON parser is hand-written (~300 lines)
- HTTP client uses JDK 11+ `java.net.http`
- SMTP client is a hand-written socket client
- CSV parser is hand-written (~100 lines)
- No Apache Commons, Guava, Jackson, Gson, Jakarta Mail, Jansi

---

## Layered structure

Chirp is organized in three layers:

```
┌───────────────────────────────────────────────────────────────┐
│  Layer 3 — Facade                                             │
│  chirp.Chirp                                                  │
│  (single-import, delegates to every module)                   │
├───────────────────────────────────────────────────────────────┤
│  Layer 2 — Feature Modules                                    │
│  console · text · number · collection · datetime · format      │
│  io · json · http · mail · decorator · session · ui            │
├───────────────────────────────────────────────────────────────┤
│  Layer 1 — Cross-cutting Infrastructure                       │
│  config · startup · stats · internal (Ansi, Trace, Glyphs)    │
└───────────────────────────────────────────────────────────────┘
```

### Layer 1 — Infrastructure

Classes that support every other module:

| Class                          | Purpose                                         |
| ------------------------------ | ----------------------------------------------- |
| `chirp.internal.Ansi`          | ANSI escape sequences with capability detection |
| `chirp.internal.Capabilities`  | Terminal detection (ANSI, Unicode, TrueColor)   |
| `chirp.internal.Glyphs`        | Unicode → ASCII fallback symbols                |
| `chirp.internal.Trace`         | Internal tracer (opt-in)                        |
| `chirp.internal.Strict`        | Strict-warning engine                           |
| `chirp.internal.Logger`        | Diagnostics channel                             |
| `chirp.internal.EaseException` | The only exception Chirp ever throws            |
| `chirp.config.Config`          | Central configuration hub                       |
| `chirp.startup.ChirpLifecycle` | Lifecycle gate (`Chirp.start()`)                |
| `chirp.stats.Stats`            | Runtime statistics                              |

Layer 1 has **no dependencies** on Layers 2 or 3.

### Layer 2 — Feature modules

The 13 feature modules are **independent** — you can use any subset without
pulling in the others.

| Module       | Depends on                                                                                                      |
| ------------ | --------------------------------------------------------------------------------------------------------------- |
| `console`    | `internal`, `session` (for watermark)                                                                           |
| `text`       | `internal`                                                                                                      |
| `number`     | `internal`, `format` (for `ordinal`)                                                                            |
| `collection` | `internal`, `console` (for `print`)                                                                             |
| `datetime`   | `internal`                                                                                                      |
| `format`     | `datetime`, `number`, `session`, `text`                                                                         |
| `io`         | `internal`, `console` (for `Messenger`), `json` (for `JsonFile`), `number` (for `humanSize`)                    |
| `json`       | `internal`                                                                                                      |
| `http`       | `internal`, `json` (for parsing), `io` (for `saveRaw`)                                                          |
| `mail`       | `internal`, `console` (for `Loader`), `io` (for `TextFileIO`), `session` (for signatures), `text` (for `Check`) |
| `decorator`  | `internal`, `config`                                                                                            |
| `session`    | _none_                                                                                                          |
| `ui`         | `internal`, `text`, `validation`, `session` (for signatures)                                                    |

Note: no cyclic dependencies. `text` never depends on `console`;
`console` depends on `text` for `Check`. All dependencies point downward
in the hierarchy.

### Layer 3 — Facade

`chirp.Chirp` is a **pure delegation class** with no logic of its own.
Every method forwards to the appropriate module.

```java
public static int readInt(String prompt) {
    return Input.readInt(prompt);
}

public static String currency(double amount) {
    return Format.currency(amount);
}
```

The facade's job is to make everything available through one import. It
does not add functionality. If you need to test something, test the
module — not the facade.

---

## The facade

`chirp.Chirp` is 1500+ lines of delegating methods. Here is the pattern:

```java
public static <ReturnType> <method>(<args>) {
    return <Module>.<method>(<args>);
}
```

Example for `readInt`:

```java
public static int readInt(String prompt)               { return Input.readInt(prompt); }
public static int readInt(String prompt, int min, int max) { return Input.readInt(prompt, min, max); }
public static IntReader readInt()                      { return Input.readInt(); }
```

Why so many aliases? Because users think in different words:

- `readInt("Age")`
- `readInteger("Age")`
- `takeInt("Age")`
- `getInt("Age")`
- `askInt("Age")`
- `inputInt("Age")`
- `promptInt("Age")`
- `scanInt("Age")`
- `readNumber("Age")`
- `readWhole("Age")`

Each is a one-line delegation. No logic duplicated. No maintenance burden
beyond keeping the names in sync.

---

## Module independence

The most important architectural rule: **modules do not depend on each
other in a cycle**. This makes the library testable, replaceable, and
understandable one module at a time.

### Dependency graph (acyclic)

```
session ─────────────────────────┐
    │                            │
    ▼                            │
internal ◄── config              │
    │                            │
    ├── text                     │
    │    │                       │
    │    ▼                       │
    ├── number ──► format        │
    │              │             │
    │              ▼             │
    ├── datetime   │             │
    │              │             │
    ├── json       │             │
    │              │             │
    ├── collection ─────► console│
    │                   │        │
    │                   ▼        │
    ├── io ◄── json, number      │
    │                   │        │
    ├── http ───────────┘        │
    │                            │
    ├── mail ◄── console, io, session, text
    │                            │
    ├── decorator ◄── config     │
    │                            │
    └── ui ◄── text, validation, session
```

No back-edges. No cycles.

### Practical consequence

You can use `chirp.text` alone (in a `.jar` containing only `Text`,
`Check`, `Regex`, and `internal`) without any console, files, or UI code.
This is important for:

- **Library reusability** — `Check.isEmail` is useful in any Java project
- **Testability** — `Text` tests do not load `Console` or `Input`
- **Bundle size** — the fat JAR is 3 MB, but a thin usage is much smaller

---

## Cross-cutting concerns

Three concerns appear in every module. Each has a dedicated infrastructure
class.

### Strict warnings

Emitted when a reader is used without expected validation rules.

```java
// User writes: readInt("Age")
// Chirp emits: [STRICT] Reading 'Age' as int: no RANGE validation.
```

Defined in `chirp.internal.Strict`. Categories:

- `RANGE` — numeric min/max
- `BLANK` — string non-empty
- `LENGTH` — string min/max length
- `FORMAT` — string regex/email/phone
- `CUSTOM` — user-defined predicate

Per-reader opt-out: `.lenient()` or `.skipRangeWarning()`.
Global opt-out: `Config.strictWarnings(false)`.

### Trace

Emitted when `Config.trace(true)` is set.

```java
// [TRACE] #0010 enter IntReader.read(label=Age, min=1, max=130)
// [DEBUG] #0011 value: parsed int = 25
// [DEBUG] #0012 validation: RANGE min(1) => PASS
// [TRACE] #0013 exit  IntReader.read => 25
```

Defined in `chirp.internal.Trace`. Four levels: TRACE, DEBUG, INFO, WARN.
Category filtering: `Config.traceOnly("reader", "rule")`.

### Diagnostics

Warnings and errors reported by modules that return a safe default (e.g.
`FileIO.readAll` returns "" on failure).

Defined in `chirp.internal.Logger`. Two modes:

- **Library mode** (default) — messages go to JDK `System.Logger("chirp")`
- **Interactive mode** (after `Chirp.start()`) — messages go to `Console.error`
  and `Console.warn` for friendlier output

---

## Startup lifecycle

`ChirpLifecycle` is a singleton state machine:

```
UNSTARTED ──start()──► STARTING ──started──► STARTED
    ▲                     │                     │
    │                     └──exception──► UNSTARTED
    │                                           │
    └───────── shutdown() ◄─────────────────────┘
```

### What `start()` does

1. **Load config** — `StartupConfig` (banner, stats, colour, unicode, hints)
2. **Install `Ansi`** — enable colour if supported and configured
3. **Install `Glyphs`** — pick Unicode style or ASCII fallback
4. **Render banner** — clear screen, print CHIRP artwork, version, author
5. **Start stats sampler** — background daemon thread at 1-second interval
6. **Install console sink** — route `Logger` output to `Console`
7. **Mark STARTED**

Steps 2–4 happen **before** the `STARTED` flag is set, so `Console.title`
during banner rendering does not throw `ChirpNotStartedException`. A
special `permitDuringStartup` flag allows the banner to use `Console`.

### What `shutdown()` does

1. Stop the stats sampler thread
2. Clear the console sink
3. Reset the state to `UNSTARTED`
4. Clear `startedAt`

`shutdown()` is used by tests to reset between test classes. Applications
never call it.

### Lifecycle guard

```java
public static void requireStarted() {
    if (state == State.STARTED || permitDuringStartup) return;
    throw new ChirpNotStartedException(STARTUP_HINT);
}
```

Every stateful operation (input, decorated console output) calls this.
Stateless operations (pure string functions, file reads, JSON parsing) do
not.

---

## Threading model

Chirp is **single-threaded by default**. Every operation happens on the
calling thread, synchronously.

### Background threads (daemon only)

Two places spawn threads:

1. **Stats sampler** — `chirp.stats.StatsSampler` runs at 1 Hz, samples
   memory / CPU / thread counts, notifies listeners. Daemon thread — does
   not keep the JVM alive.
2. **Loader** — `chirp.console.Loader` spins for its duration, then stops
   on `stop()`. Daemon thread — cannot block program exit.

Neither thread mutates application state. Neither throws to the caller.
Both are short-lived.

### Synchronisation

`chirp.internal.Trace` synchronises `System.err` writes to keep lines
atomic:

```java
synchronized (System.err) {
    System.err.println(rendered);
}
```

`Loader` and `Spinner` synchronise `System.out` for the same reason:

```java
synchronized (System.out) {
    System.out.print("\r" + frames[i] + " " + label);
    System.out.flush();
}
```

All other state (counters, caches) uses `AtomicLong`, `AtomicBoolean`, or
`ConcurrentHashMap` — no manual locks.

### Async helpers

`chirp.ui.Ui.runAsync(task, onComplete)` spawns a background thread for
long operations, then calls `Platform.runLater(onComplete)` when done.

`HttpRequest.sendAsync()` uses `CompletableFuture.supplyAsync(this::send)`
— callers get a `CompletableFuture<HttpResponse>` and can chain.

Neither requires the caller to manage threads.

---

## Testing strategy

Chirp has **500+ unit tests** across **60+ test classes**. Every public
method has at least one test; complex methods have branch coverage.

### Test categories

| Category          | Test classes                | Example                                                 |
| ----------------- | --------------------------- | ------------------------------------------------------- |
| **Unit**          | One per production class    | `ArraysUtilTest`, `TextTest`                            |
| **Alias parity**  | One per module with aliases | `TextAliasesTest`, `ArraysUtilAliasesTest`              |
| **Integration**   | Cross-module tests          | `TraceIntegrationTest`, `EmailListenerAndExceptionTest` |
| **Strictness**    | Reader behaviour            | `ReaderStrictnessTest` (40 tests)                       |
| **Trace**         | Tracer output               | `TraceTest`, `TraceVarargsTest`, `ReaderTraceTest`      |
| **Mock-stdin**    | Reader input handling       | `ReaderStrictnessTest` uses `ByteArrayInputStream`      |
| **Parameterised** | Validator edge cases        | `EmailValidationTest` (40+ addresses)                   |
| **Manifest**      | Structural integrity        | `ManifestTest` (every class + facade method present)    |

### Structural manifest

`ManifestTest` verifies **every expected class exists** and **every facade
method is present**. This catches:

- A file forgotten from a delivery
- A method accidentally removed during refactoring
- A rename that broke backward compatibility

```java
@Test
void everyExpectedClassIsPresent() {
    for (String name : EXPECTED_CLASSES) {
        assertDoesNotThrow(() -> Class.forName(name), "Missing class: " + name);
    }
}

@Test
void everyExpectedFacadeMethodIsPresent() {
    Method[] methods = Chirp.class.getDeclaredMethods();
    Set<String> present = new HashSet<>();
    for (Method m : methods) present.add(m.getName());

    for (String name : EXPECTED_FACADE_METHODS) {
        assertTrue(present.contains(name), "Missing facade method: " + name);
    }
}
```

### Headless testing

Chirp supports headless environments (CI servers, Docker). The UI module
requires JavaFX but tests use `JavaFxTestExtension` which starts the
toolkit before any UI test class runs.

Data modules (`Text`, `Numbers`, `Files`, `Http`, `Email`) run fully
headless.

### Quality gates in `mvn verify`

| Gate              | Fails build if                         |
| ----------------- | -------------------------------------- |
| Compiler warnings | Any `-Xlint:all` warning               |
| Tests             | Any test fails                         |
| Spotless          | Any file is not formatted              |
| SpotBugs          | Any bug at `effort=Max, threshold=Low` |
| PMD               | Any defect rule triggers               |

Coverage is tracked by JaCoCo but not enforced as a hard gate (to avoid
blocking PRs that add a method without a test).

---

## Distribution

Chirp is distributed in three forms from every GitHub release:

| Artifact                  | Content                          | Consumer                             |
| ------------------------- | -------------------------------- | ------------------------------------ |
| `chirp-1.0.0.jar`         | Fat JAR — Chirp + JavaFX bundled | End users, drop-in usage             |
| `chirp-1.0.0-thin.jar`    | Thin JAR — Chirp only            | Maven consumers (they manage JavaFX) |
| `chirp-1.0.0-sources.jar` | Sources                          | IDE navigation                       |

The `starter-template.zip` is a zipped version of `starter-template/`
containing a ready-to-open VS Code project with `.vscode/settings.json`
configured for `lib/**/*.jar`.

### Build pipeline

```
git push
    │
    ▼
GitHub Actions (build.yml)
    │
    ├── mvn clean package
    │     ├── javac -Xlint:all -Werror
    │     ├── 500+ tests
    │     └── package → fat JAR + thin JAR + sources
    │
    ├── mvn verify (quality gates)
    │     ├── Spotless format check
    │     ├── SpotBugs
    │     ├── PMD
    │     └── JaCoCo
    │
    └── upload artifacts
```

### Release pipeline

```
git tag v1.0.0
git push origin v1.0.0
    │
    ▼
GitHub Actions (release.yml)
    │
    ├── mvn clean package
    │
    └── Create GitHub Release with:
        ├── chirp-1.0.0.jar
        ├── chirp-1.0.0-thin.jar
        ├── chirp-1.0.0-sources.jar
        ├── chirp-1.0.0-javadoc.jar
        └── starter-template.zip
```

Users download the fat JAR (or the zip template) from the release page —
no Maven, no Gradle, no setup.

---

## Summary

Chirp is small, deliberate, and consistent:

- **One import** — `import static chirp.Chirp.*;`
- **Zero dependencies** for data modules
- **Graceful degradation** — colours, Unicode, features all fall back
- **Null-safe everywhere** — no `NullPointerException` from bad input
- **Structured for testability** — 500+ tests, structural manifest
- **Buildable, formattable, analysable** — compiler warnings fail the build
- **Distributed as JARs** — no build tool required for end users
