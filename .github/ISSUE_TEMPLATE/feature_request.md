---
name: Feature request
about: Suggest an idea or new capability for Chirp
title: "[Feature]: "
labels: ["enhancement", "needs-triage"]
assignees: []
---

## Problem

<!--
Describe the problem you are trying to solve. Focus on the *problem*,
not the *solution*. Example:
"I need to read a file line-by-line but skip the first N lines. Currently
I have to read all lines, then call subList — which loads the whole file
into memory."
-->

## Proposed solution

<!-- A sketch of what the API could look like. -->

```java
// Example of how you would call the new method
```

## Alternatives considered

<!--
What other approaches have you tried? What did you build in standard Java
to work around the limitation? Why was it painful?
-->

## Which module?

<!-- Check all that apply. -->

- [ ] `console` — output, input, menus, loaders
- [ ] `text` — strings, checks, regex
- [ ] `validation` — rules, validators
- [ ] `number` — numeric utilities
- [ ] `collection` — arrays, lists, search
- [ ] `datetime` — dates and times
- [ ] `format` — formatting
- [ ] `io` — files (text, CSV, JSON)
- [ ] `http` — HTTP client
- [ ] `mail` — email / SMTP
- [ ] `ui` — JavaFX toolkit
- [ ] `decorator` — retry, timing, caching
- [ ] Other: <!-- describe -->

## Scope check

Chirp has a narrow scope by design. Before submitting, please confirm:

- [ ] This cannot be achieved with the JDK alone without significant boilerplate
- [ ] This does not require a new external dependency
- [ ] This follows the [API standards](https://github.com/hyphensaad/chirp/blob/main/CONTRIBUTING.md#coding-standards) (null-safe, aliased, fluent where appropriate)
- [ ] I have read the [FAQ](https://github.com/hyphensaad/chirp/blob/main/docs/FAQ.md) and this is not already possible

## Additional context

<!-- Any other information, screenshots, links, or prior discussions. -->

## Are you willing to implement this?

- [ ] Yes, I would like to submit a PR
- [ ] No, but I would use it
- [ ] I would need guidance from the maintainers first
