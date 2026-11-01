---
name: Bug report
about: Report a problem with Chirp
title: "[Bug]: "
labels: ["bug", "needs-triage"]
assignees: []
---

## Description

<!-- A clear and concise description of the bug. -->

## Reproducer

<!-- The smallest possible code that reproduces the bug. -->

```java
import static chirp.Chirp.*;

public class Reproducer {
    public static void main(String[] args) {
        Chirp.start();
        // ... your 5-10 lines that show the bug
    }
}
```

## Expected behaviour

<!-- What you expected to happen. -->

## Actual behaviour

<!-- What actually happened. Include any output, error messages, or stack traces. -->

```
<!-- paste the actual output or stack trace here -->
```

## Environment

- **Chirp version**: <!-- e.g. 1.0.0 — from the JAR filename -->
- **Java version**: <!-- output of `java -version` -->
- **Operating system**: <!-- e.g. Windows 11, macOS 14.2, Ubuntu 22.04 -->
- **Terminal / IDE**: <!-- e.g. Windows Terminal, IntelliJ IDEA 2024.1 -->

## Trace output

<!-- If the bug is a runtime issue, enable tracing and paste the output. -->

```java
Config.enableTrace();
Config.traceLevel(chirp.internal.Trace.Level.DEBUG);
// then run your reproducer
```

<!-- Paste the trace output (it goes to stderr) here, if applicable -->

```

```

## Additional context

<!-- Anything else that might help — screenshots, related issues, code samples, etc. -->

## Checklist

- [ ] I have searched existing issues and found no duplicate
- [ ] I am using the latest Chirp version from the [releases page](https://github.com/hyphensaad/chirp/releases)
- [ ] I have read the [Troubleshooting guide](https://github.com/hyphensaad/chirp/blob/main/docs/TROUBLESHOOTING.md)
- [ ] I have provided a minimal, self-contained reproducer
