# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned

- Additional validators (ISBN, credit card, IBAN for more countries)
- `Pair<A, B>`, `Range<T>`, `Result<T>` value objects
- `Cache` module with TTL and eviction policies
- Rich `DataTable` with sorting, filtering, and pagination for JavaFX
- Multi-language support (`Locale`) in `Format` module

## [1.0.0] — 2026-10-31

Initial public release. Complete, production-ready library with:

### Added — Core

- **Single-import facade** — `import static chirp.Chirp.*;` for the entire API
- **Startup lifecycle** — `Chirp.start()` with banner, config, and stats
- **Configuration hub** — `Config` for colour, debug, strict warnings, stats
- **Session** — global user profile for screenshots and signatures
- **Statistics** — automatic run summary (elapsed, heap, CPU, lines read)

### Added — Console & Input

- **Console** — printing, semantic messages, tables, progress bars, separators
- **Separator** — 12 styles, alignment, colour, key-value factory
- **Input** — 7 aliases per method, fluent readers for every type
- **Fluent readers** — `IntReader`, `DoubleReader`, `StringReader`,
  `BooleanReader`, `CharReader`, `DateReader`, `TimeReader`, `DateTimeReader`
- **Strict warnings** — optional `[STRICT]` hints when validation rules are
  missing (silenced with `.lenient()` or `Config.strictWarnings(false)`)
- **Loader** — animated spinner with success/error terminals
- **Menu** — fluent interactive menu with separators, headers, exit

### Added — Data & Logic

- **Text** — null-safe manipulation with case conversions, masking,
  truncation, palindrome, similarity, and fluent `TextChain`
- **Check** — 40+ boolean validations including email (RFC 5322 subset),
  phone, URL, IP, UUID, password, Pakistani CNIC/mobile
- **Rule** — composable validation rules with `define/from/allOf/anyOf/atLeast`
- **Rules** — registry with 15 built-in rules and key-value registration
- **Validate** — multi-error validator with labeled fields
- **Numbers** — random, clamp, prime, factorial, fibonacci, gcd, lcm,
  digits, bases, statistics, roman numerals
- **NumberChain** — fluent numeric chain with 30+ operations
- **ArraysUtil** — 70+ methods for 1D/2D/3D arrays
- **ArrayChain** — fluent array chain with 25+ operations
- **Lists** — `List<Integer>` aggregate, transform, convert
- **Filter** — predicate filtering, grouping, partitioning
- **Search** — strict, loose, whole-word, fuzzy, regex, composite key-value
- **Pagination** — flexible page-based pagination with metadata
- **DateTime** — parse, format, arithmetic, snapping, business days, age,
  relative time, key-value configurable
- **DateChain** — fluent date chain
- **Format** — named-placeholder interpolation, currency, plural, ordinal,
  bytes, duration, CNIC/phone formatting
- **Formatter** — fluent template formatter with per-key transforms

### Added — I/O & Networking

- **FileIO** — safe file and directory operations
- **TextFile** — fluent file wrapper
- **TextFileIO** — text file operations in every mode (read/write/append/
  prepend/head/tail/grep/replace/slice/stats)
- **CsvFile** — RFC 4180 CSV with `CsvTable` view
- **JsonFile** — JSON file helpers
- **Json** — zero-dependency JSON parser/builder/writer (7 types)
- **Http** — fluent GET/POST/PUT/DELETE/PATCH with spinner, retry, listener,
  connectivity check, JSON response parsing
- **Email** — SMTP with STARTTLS/SSL, HTML templates with strict placeholder
  validation, file attachments, `.env` password strategies, retry

### Added — UI (JavaFX)

- **Ui** — entry point with `Ui.app(...)` and `Ui.show(...)`
- **UiApp** — window builder with theme, size, scroll, close hooks
- **UiStyle** — global font size, family, accent colour; presets for
  modern light/dark, compact, large text, monospace
- **UiTheme** — LIGHT / DARK / AUTO
- **UiIntegration** — bridges to `Rules`, `Check`, `ValidationResult`
- **FormBuilder** — declarative multi-field forms with validation
- **40+ components** — Label, Button, TextField, PasswordField, TextArea,
  Field (composite with counter/error), Checkbox, Select (searchable),
  RadioGroup, Slider, Spinner, DatePicker, TimePicker, FileField,
  ColorPicker, Hyperlink, ProgressBar, ProgressIndicator, Separator,
  ImageView, Table, TabPane, Accordion, TitledPane, ListView, TreeView,
  TreeTableView, MenuBar, ToolBar, SplitPane, Pagination, PieChart,
  BarChart, LineChart, AreaChart, ScatterChart, StackedBarChart,
  ToggleButton, MenuButton, SplitMenuButton, ContextMenu, Tooltip,
  SearchField, Rating, Badge, Chip, Card, Breadcrumb
- **KeyBindings** — descriptor-string key bindings (`"ctrl S"`, `"escape"`)

### Added — Decorators

- **Decorator** — timing, safe-wrapping, caching, logging, fallback
- **Retry** — fluent policy with attempts, backoff, exponential, jitter,
  exception filtering
- **Timed** — value + duration record

### Added — Cross-cutting

- **Trace** — internal debug tracer with levels and categories
- **Strict** — strict-warning engine
- **Capabilities** — terminal detection (ANSI, Unicode, TrueColor) with
  Windows codepage detection
- **Glyphs** — Unicode → ASCII fallback symbols

### Added — Tests

- **500+ unit tests** across 60+ test classes covering every module
- Parameterised tests for validators
- Mock-stdin tests for readers
- Strictness / trace integration tests
- Full manifest test verifying every class and facade method

### Quality

- **Formatting**: Spotless + Eclipse formatter (fails `mvn verify` on unformatted code)
- **Static analysis**: SpotBugs (max effort) + FindSecBugs + PMD (defect rules)
- **Coverage**: JaCoCo
- **CI**: GitHub Actions builds, tests, and verifies on every push
