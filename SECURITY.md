# Security Policy

## Supported versions

| Version | Supported |
| ------- | --------- |
| 1.0.x   | ✅        |
| < 1.0   | ❌        |

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

Report suspected vulnerabilities privately by emailing:

**saad.mansoor@skt.umt.edu.pk**

Include:

1. **Description** of the vulnerability
2. **Steps to reproduce** or a proof-of-concept
3. **Impact** — what an attacker could do
4. **Affected versions**
5. **Suggested fix** (if you have one)

You will receive a response within **72 hours**. We aim to release a fix
within **14 days** for critical issues.

## Disclosure policy

- We will confirm receipt of your report within 72 hours.
- We will investigate and determine the impact.
- We will keep you informed of our progress.
- We will publicly acknowledge your contribution (unless you request
  anonymity) when the fix is released.

## Scope

In-scope:

- The Chirp library code in this repository
- The distributed JAR files

Out-of-scope:

- Vulnerabilities in Java or JavaFX themselves (report to Oracle/OpenJFX)
- Vulnerabilities in third-party dependencies of applications using Chirp
- Denial of service via intentionally malformed input to the library's own
  methods (Chirp is defensive against this; report if you find a bypass)

## Known limitations

Chirp is a utility library, not a security boundary. Specifically:

- **Email passwords** — `DIRECT` strategy stores passwords in memory. Use
  `ENV` or `PROMPT` for production.
- **HTTP requests** — Chirp does not enforce certificate pinning or custom
  TLS configuration beyond the JDK defaults.
- **File I/O** — Chirp does not sandbox file access. Path traversal is the
  caller's responsibility.

## Best practices for Chirp users

- Use `passwordFromEnv(...)` rather than `password(...)` for production.
- Always validate user input with `Check` and `Input` methods.
- Use `Http` over HTTPS (Chirp does not enforce this; the caller must).
- Never log sensitive data — Chirp's `Debug` and `Trace` are opt-in, so turn
  them off before shipping.

## Acknowledgements

We thank the following security researchers for responsibly disclosing
vulnerabilities:

_None yet._
