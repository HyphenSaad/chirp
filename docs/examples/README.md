---
render_with_liquid: false
---

# Chirp Examples

Runnable, before-and-after examples for every feature of Chirp.

Each example file is **self-contained** — you can copy any code snippet
into a Java project with Chirp on the classpath and run it.

---

## Index

| File                                                       | What it covers                                                                          |
| ---------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| [01-console-and-input.md](01-console-and-input.md)         | Output, colours, tables, progress bars, immediate input, fluent readers, menus, loaders |
| [02-arrays-and-validation.md](02-arrays-and-validation.md) | Array operations, matrix helpers, `ArrayChain`, `Check`, `Rule`, `Validate`             |
| [03-files-http-email.md](03-files-http-email.md)           | Text files, CSV, JSON, HTTP, email, retry                                               |

---

## Conventions used

- **Before** — the code you would write in standard Java
- **After** — the same task with Chirp
- **Output** — the visible result (for programs that print to the console)

Every snippet is copy-paste ready. Every snippet compiles against
`chirp-1.0.0.jar`.

---

## Running the examples

### Option 1 — As a Chirp project

Set up any of the examples in an existing project:

```
MyProject/
├── lib/
│   └── chirp-1.0.0.jar
└── src/
    └── Main.java        ← copy the code here
```

Run with `javac -cp lib/chirp-1.0.0.jar src/Main.java` then
`java -cp lib/chirp-1.0.0.jar:src Main`.

### Option 2 — Quick test

Use `jshell` for quick experimentation:

```bash
jshell --class-path lib/chirp-1.0.0.jar

jshell> import static chirp.Chirp.*;
jshell> println("Hello, world");
Hello, world
jshell> println(sum(new int[]{1, 2, 3, 4}));
10
```

For the full flow (with startup banner and lifecycle), compile and run
`Main.java`:

```bash
javac -cp lib/chirp-1.0.0.jar src/Main.java
java -cp lib/chirp-1.0.0.jar:src Main
```

---

## The golden rule

**Every example starts with one import:**

```java
import static chirp.Chirp.*;
```

That gives you the entire API. If you need something the facade does not
expose (e.g. `CsvFile`, `TextFileIO`, `Json`), import the specific class:

```java
import chirp.io.CsvFile;
import chirp.io.TextFileIO;
import chirp.json.Json;
```

Everything else is one import.

---

## When things go wrong

If a snippet does not compile:

- Check the JAR is on the classpath
- Check the Java version is 17+
- Check the import — some snippets use classes not exposed by the facade

If a snippet compiles but throws at runtime:

- Check whether it needs `Chirp.start()` (console, input, decorated messages)
- Check the [Troubleshooting guide](../TROUBLESHOOTING.md)

---

## Contributing an example

Examples live in `docs/examples/`. To add one:

1. Create `NN-topic-name.md`
2. Follow the **Before / After / Output** structure
3. Add an entry to the index above
4. Ensure the snippet compiles against the current Chirp version

Examples are reviewed for clarity more than for brevity — a reader
learning Chirp should be able to run any snippet and understand the
result.
