import static chirp.Chirp.*;

/**
 * A starting point for your project.
 *
 * <p>This demo shows four areas where Chirp replaces significant boilerplate
 * in standard Java:</p>
 *
 * <ul>
 *   <li>Array aggregate helpers</li>
 *   <li>Formatting (currency, ordinal, plural, percent)</li>
 *   <li>Date/time humanisation</li>
 *   <li>Safe file reads</li>
 * </ul>
 *
 * <p>Edit this file to explore further. Every method demonstrated here has
 * many aliases — see the API Reference at
 * <a href="https://github.com/hyphensaad/chirp/blob/main/docs/API_REFERENCE.md">
 * github.com/hyphensaad/chirp</a>.</p>
 */
public class Main {

  public static void main(String[] args) {
    // One line to enable banner, colours, run summary, and lifecycle guard.
    Chirp.start();

    title("Welcome to Chirp");
    info("A friendly Java toolkit for building things faster.");
    println();

    demonstrateArrays();
    demonstrateFormatting();
    demonstrateDates();
    demonstrateFiles();

    println();
    success("Edit src/Main.java to get started.");
    println("Full documentation: https://github.com/hyphensaad/chirp");
  }

  // ------------------------------------------------------------------ arrays

  private static void demonstrateArrays() {
    println("Arrays:");
    int[] nums = { 5, 3, 8, 1, 9, 2, 4 };

    println("  input      = " + java.util.Arrays.toString(nums));
    println("  sum        = " + sum(nums));
    println("  max        = " + max(nums));
    println("  min        = " + min(nums));
    println("  average    = " + Format.decimal(average(nums), 2));
    println("  sorted     = " + java.util.Arrays.toString(sorted(nums)));
    println("  reversed   = " + java.util.Arrays.toString(reverse(nums)));
    println("  distinct   = " + java.util.Arrays.toString(distinct(nums)));
    println("  contains 8 = " + contains(nums, 8));
    println();

    // Fluent chain — immutable, composable.
    int[] even = arrayChain(nums)
        .filter(n -> n % 2 == 0)
        .sorted()
        .toArray();
    println("  even-only  = " + java.util.Arrays.toString(even));
    println();
  }

  // -------------------------------------------------------------- formatting

  private static void demonstrateFormatting() {
    println("Formatting:");

    println("  currency  = " + Format.currency(1234.5));
    println("  rupees    = " + Format.rupees(1234.5));
    println("  percent   = " + Format.percent(92.5));
    println("  ordinal   = " + Format.ordinal(42));
    println("  plural    = " + Format.plural(3, "apple", "apples"));
    println("  bytes     = " + Format.bytes(1536));
    println("  duration  = " + Format.duration(3_725_000L));
    println("  roman     = " + Format.roman(2026));

    // Template interpolation
    String message = Format.fmt(
        "Hello, {name}! You have {count} new messages.",
        "name", "Saad",
        "count", 3);
    println("  template  = " + message);

    // Fluent formatter
    String order = formatter("Order #{id} = {amount}")
        .with("id", 42)
        .with("amount", 1234.5)
        .padLeft("id", 6, '0')
        .currency("amount")
        .format();
    println("  fluent    = " + order);
    println();
  }

  // ------------------------------------------------------------------- dates

  private static void demonstrateDates() {
    println("Dates:");

    println("  today     = " + DateTime.humanize(DateTime.today()));
    println("  short     = " + DateTime.humanizeShort(DateTime.today()));
    println("  iso       = " + DateTime.formatISO(DateTime.today()));
    println("  3d ago    = " + DateTime.timeAgo(DateTime.today().minusDays(3)));
    println("  next week = " + DateTime.humanizeShort(DateTime.addDays(DateTime.today(), 7)));

    // Parse any of several formats
    String[] inputs = { "2026-10-31", "31/10/2026", "Oct 31, 2026" };
    for (String input : inputs) {
      var parsed = DateTime.parseDate(input);
      println("  parse \"" + input + "\" = " + DateTime.humanizeShort(parsed));
    }
    println();
  }

  // ------------------------------------------------------------------- files

  private static void demonstrateFiles() {
    println("Files:");
    println("  README exists = " + FileIO.exists("README.md"));
    println("  lib/ exists   = " + FileIO.isDirectory("lib"));

    // Safe write-then-read roundtrip
    String demoPath = "out/chirp-demo.txt";
    mkdir("out");
    if (writeFile(demoPath, "Hello from Chirp!")) {
      println("  wrote        = " + demoPath);
      println("  read back    = " + readFile(demoPath));
    }
    println();
  }
}