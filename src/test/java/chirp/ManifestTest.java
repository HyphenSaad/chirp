package chirp;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Structural manifest — verifies that every expected class and every facade
 * method is present.
 */
class ManifestTest {

    // ============================================================== classes

    private static final List<String> EXPECTED_CLASSES = List.of(
            "chirp.Chirp",
            "chirp.ChirpNotStartedException",
            "chirp.config.Config",

            "chirp.console.Console",
            "chirp.console.Debug",
            "chirp.console.Input",
            "chirp.console.Loader",
            "chirp.console.Menu",
            "chirp.console.Messenger",
            "chirp.console.Separator",
            "chirp.console.input.BooleanReader",
            "chirp.console.input.CharReader",
            "chirp.console.input.DateReader",
            "chirp.console.input.DateTimeReader",
            "chirp.console.input.DoubleReader",
            "chirp.console.input.IntReader",
            "chirp.console.input.StringReader",
            "chirp.console.input.TimeReader",

            "chirp.collection.ArrayChain",
            "chirp.collection.ArraysUtil",
            "chirp.collection.Filter",
            "chirp.collection.Lists",
            "chirp.collection.Page",
            "chirp.collection.Pagination",
            "chirp.collection.Search",

            "chirp.datetime.DateChain",
            "chirp.datetime.DateTime",

            "chirp.decorator.Decorator",
            "chirp.decorator.Retry",
            "chirp.decorator.Timed",

            "chirp.format.Format",
            "chirp.format.Formatter",

            "chirp.http.Http",
            "chirp.http.HttpException",
            "chirp.http.HttpListener",
            "chirp.http.HttpMethod",
            "chirp.http.HttpRequest",
            "chirp.http.HttpResponse",
            "chirp.http.Spinner",

            "chirp.internal.Ansi",
            "chirp.internal.Capabilities",
            "chirp.internal.EaseException",
            "chirp.internal.Glyphs",
            "chirp.internal.Logger",
            "chirp.internal.Strict",
            "chirp.internal.Trace",
            "chirp.internal.ValidationCategory",

            "chirp.io.CsvFile",
            "chirp.io.FileIO",
            "chirp.io.JsonFile",
            "chirp.io.TextFile",
            "chirp.io.TextFileIO",

            "chirp.json.Json",
            "chirp.json.JsonArray",
            "chirp.json.JsonBoolean",
            "chirp.json.JsonException",
            "chirp.json.JsonNull",
            "chirp.json.JsonNumber",
            "chirp.json.JsonObject",
            "chirp.json.JsonParser",
            "chirp.json.JsonString",
            "chirp.json.JsonValue",
            "chirp.json.JsonWriter",

            "chirp.mail.Email",
            "chirp.mail.EmailException",
            "chirp.mail.EmailListener",
            "chirp.mail.EmailRequest",
            "chirp.mail.EmailResult",
            "chirp.mail.EnvLoader",
            "chirp.mail.HtmlTemplate",
            "chirp.mail.PasswordStrategy",
            "chirp.mail.SmtpClient",

            "chirp.number.NumberChain",
            "chirp.number.Numbers",

            "chirp.session.Session",

            "chirp.startup.Banner",
            "chirp.startup.ChirpLifecycle",
            "chirp.startup.StartupConfig",

            "chirp.stats.Stats",
            "chirp.stats.StatsListener",
            "chirp.stats.StatsSampler",

            "chirp.text.Check",
            "chirp.text.Regex",
            "chirp.text.Text",
            "chirp.text.TextChain",

            "chirp.ui.Ui",
            "chirp.ui.UiApp",
            "chirp.ui.UiTheme",
            "chirp.ui.UiStyle",
            "chirp.ui.UiIntegration",
            "chirp.ui.FormBuilder",
            "chirp.ui.Stack",
            "chirp.ui.Row",
            "chirp.ui.Label",
            "chirp.ui.Button",
            "chirp.ui.TextField",
            "chirp.ui.PasswordField",
            "chirp.ui.TextArea",
            "chirp.ui.Field",
            "chirp.ui.Checkbox",
            "chirp.ui.Select",
            "chirp.ui.RadioGroup",
            "chirp.ui.Slider",
            "chirp.ui.Spinner",
            "chirp.ui.DatePicker",
            "chirp.ui.TimePicker",
            "chirp.ui.FileField",
            "chirp.ui.ColorPicker",
            "chirp.ui.Hyperlink",
            "chirp.ui.ProgressBar",
            "chirp.ui.ProgressIndicator",
            "chirp.ui.Separator",
            "chirp.ui.ImageView",
            "chirp.ui.Table",
            "chirp.ui.TabPane",
            "chirp.ui.Accordion",
            "chirp.ui.ListView",
            "chirp.ui.TreeView",
            "chirp.ui.MenuBar",
            "chirp.ui.ToolBar",
            "chirp.ui.SplitPane",
            "chirp.ui.KeyBindings",
            "chirp.ui.PieChart",
            "chirp.ui.BarChart",
            "chirp.ui.LineChart",
            "chirp.ui.AreaChart",
            "chirp.ui.ScatterChart",
            "chirp.ui.StackedBarChart",
            "chirp.ui.ToggleButton",
            "chirp.ui.MenuButton",
            "chirp.ui.SplitMenuButton",
            "chirp.ui.ContextMenu",
            "chirp.ui.Tooltip",
            "chirp.ui.SearchField",
            "chirp.ui.TitledPane",
            "chirp.ui.TreeTableView",
            "chirp.ui.Rating",
            "chirp.ui.Badge",
            "chirp.ui.Chip",
            "chirp.ui.Card",
            "chirp.ui.Breadcrumb",
            "chirp.ui.Pagination",

            "chirp.validation.Rule",
            "chirp.validation.RuleBuilder",
            "chirp.validation.Rules",
            "chirp.validation.Validate",
            "chirp.validation.ValidationResult");

    @Test
    void everyExpectedClassIsPresent() {
        for (String name : EXPECTED_CLASSES) {
            assertDoesNotThrow(() -> Class.forName(name),
                    "Missing class: " + name);
        }
    }

    // ============================================================== facade methods

    private static final List<String> EXPECTED_FACADE_METHODS = List.of(
            // lifecycle
            "start", "init", "boot", "setup", "begin", "initialize",
            "startChirp", "bootChirp", "beginChirp", "initChirp", "setupChirp", "initializeChirp",
            "isStarted", "hasStarted",

            // input — int
            "readInt", "readInteger", "takeInt", "getInt", "askInt", "inputInt",
            "promptInt", "scanInt", "readNumber", "readWhole", "readPositiveInt", "readNonNegativeInt",

            // input — double
            "readDouble", "readDecimal", "readFloat", "takeDouble", "getDouble", "askDouble",

            // input — string
            "readLine", "readString", "readText", "takeString", "takeLine", "getString",
            "getLine", "askLine", "askString", "inputLine", "inputString", "readNonEmpty", "readRequired",

            // input — boolean / char
            "readYesNo", "readBoolean", "readBool", "readConfirm", "confirm", "askConfirm", "askYesNo",
            "readChar", "readCharacter", "readLetter", "readSingleChar",

            // input — domain
            "readPassword", "readEmail", "readPhone", "readURL", "readUrl", "readChoice",
            "choose", "readChoiceIndex",

            // input — date / time
            "readDate", "readTime", "readDateTime",

            // output
            "print", "println", "printf", "success", "ok", "done", "error", "fail",
            "warn", "warning", "info", "note",
            "title", "heading", "header", "section", "line", "hr", "separator",
            "blank", "nl", "watermark", "stamp", "clearScreen", "clear", "cls",
            "bold", "underline", "red", "green", "yellow", "blue", "cyan", "magenta", "gray",
            "printArray", "printMatrix", "table", "grid", "progress", "bar",

            // format
            "fmt", "format", "fmtWithSession", "sessionFormat", "formatter",
            "currency", "dollars", "usd", "money", "rupees", "pkr", "currencyOf",
            "percent", "pct", "decimal", "fixed", "plural", "quantify", "items",
            "files", "lines", "chars", "list", "join", "ordinal", "nth", "roman",
            "bytes", "fileSize", "duration", "humanDuration", "cnic", "pakistaniPhone", "pkPhone",

            // date / time
            "today", "nowDate", "yesterday", "tomorrow", "nowTime", "now", "nowDateTime", "dateChain",
            "parseDate", "readDateValue", "dateFrom", "parseTime", "parseDateTime",
            "humanizeDate", "humanizeShortDate", "humanizeTime", "humanizeDateTime",
            "relativeDate", "relativeDateTime", "timeAgo",
            "formatDate", "formatTime", "formatDateTime", "formatDateISO",
            "addDays", "plusDays", "daysAfter", "subtractDays", "minusDays",
            "addMonths", "subtractMonths", "addYears", "subtractYears", "addBusinessDays",
            "startOfWeek", "endOfWeek", "startOfMonth", "endOfMonth",
            "startOfYear", "endOfYear", "startOfQuarter", "endOfQuarter",
            "daysBetween", "diffDays", "monthsBetween", "yearsBetween",
            "businessDaysBetween", "businessDaysUntil",
            "age", "calculateAge", "dayName", "monthName", "dayOfWeek", "dayOfYear",
            "quarter", "daysInMonth", "isWeekend", "isWeekday", "isToday", "isPast",
            "isFuture", "isSameMonth", "isSameYear", "isLeapYear",
            "minDate", "maxDate", "toEpochMillis", "fromEpochMillis",

            // numbers
            "numberChain", "numberOf", "random", "rand", "randomInt", "randomBetween",
            "randomDouble", "randomBoolean", "clamp", "wrap", "abs", "sign", "mod",
            "pow", "sqrt", "cbrt", "hypot", "log", "log10", "log2", "exp",
            "floor", "ceil", "roundToInt", "roundToLong",
            "sin", "cos", "tan", "toRadians", "toDegrees",
            "isEven", "isOdd", "isPrime", "primesUpTo", "nextPrime", "factorial", "fact",
            "fibonacci", "fib", "gcd", "lcm", "sumDigits", "productDigits",
            "countDigits", "reverseDigits", "firstDigit", "lastDigit",
            "toBinary", "toHex", "toOctal", "fromBinary", "fromHex",
            "isArmstrong", "isPalindromeNumber", "isPerfect", "isPerfectSquare", "isPerfectCube",
            "divisors", "countDivisors", "sumDivisors",
            "round", "numFmt", "parseInt", "parseDouble", "percentOf",
            "range", "rangeInclusive", "statistics", "median", "mode", "stdDev", "variance",
            "abbreviate", "thousands", "romanNumeral",

            // arrays
            "arrayChain", "arrayOf", "sum", "total", "average", "mean", "max", "largest",
            "min", "smallest", "count", "contains", "indexOf", "lastIndexOf",
            "hasDuplicates", "isSorted", "reverse", "sorted", "sortedDescending",
            "distinct", "rotateRight", "rotateLeft", "shuffle", "subArray", "take",
            "drop", "concat", "merge", "filter", "map",
            "intersection", "union", "difference",
            "transpose", "row", "col", "diagonal", "antiDiagonal", "flatten",
            "matrix", "intArray", "zeros", "ones", "rangeArray",

            // search / filter / page
            "containsIgnoreCase", "containsStrict", "startsWith", "endsWith",
            "wholeWord", "fuzzyMatch", "levenshtein",
            "where", "reject", "unique",
            "page", "firstPage", "lastPage", "allPages",

            // text
            "chain", "textChain", "isBlank", "notBlank", "isEmpty", "hasText",
            "capitalize", "capitalise", "titleCase", "toCamelCase", "toPascalCase",
            "toSnakeCase", "toKebabCase", "swapCase", "reverseText", "reverseWords",
            "truncate", "padLeft", "padRight", "center", "repeat",
            "isPalindrome", "isAnagram", "mask", "slugify", "slug", "similarity",
            "countWords", "initials", "removeSpaces", "removePunctuation",
            "extractDigits", "extractLetters", "substringBefore", "substringAfter",
            "filterByCheck",

            // checks
            "isEmail", "isPhone", "isURL", "isUrl", "isNumeric", "isInteger",
            "isAlpha", "isDigits", "isAlphanumeric", "isHexColor", "isIPAddress",
            "isUUID", "isUsername", "isPakistaniPhone", "isPakistaniCnic",
            "isStrongPassword", "isVeryStrongPassword", "isAdult", "isValidGrade",
            "isLeap", "isJSON", "isNumericPalindrome", "is",

            // decorators
            "timed", "measure", "safe", "cached", "invalidate", "clearCache",
            "logged", "withFallback", "fallback", "retry",

            // session / rules
            "session", "getRule", "requireRule", "registerRule", "hasRule",
            "pkMobile", "pkCnic", "validate",

            // http
            "get", "getRequest", "post", "put", "delete", "del", "patch", "request",
            "fetch", "fetchJson", "getJson", "postJson", "download",
            "isOnline", "hasInternet", "isConnected", "canReach", "ping",

            // email
            "email", "mail", "composeEmail", "sendEmail", "sendMail",
            "quickSend", "sendWithEnv", "canConnect",

            // file management
            "mkdir", "mkdirs", "createDirectory", "deleteDir", "removeDir",
            "countFiles", "countAllFiles", "dirSize",
            "findFiles", "findFilesByExtension", "isEmptyDir", "listDirectories",
            "readFile", "readFileLines", "writeFile", "appendFile",
            "textFile", "csvTable", "readJson",

            // console extras
            "loader", "menu",

            // misc
            "exit", "sleep");

    @Test
    void everyExpectedFacadeMethodIsPresent() {
        Method[] methods = Chirp.class.getDeclaredMethods();
        Set<String> present = new HashSet<>();
        for (Method m : methods) present.add(m.getName());

        for (String name : EXPECTED_FACADE_METHODS) {
            assertTrue(present.contains(name),
                    "Missing facade method: Chirp." + name + "()");
        }
    }
}
