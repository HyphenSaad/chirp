package chirp.text;

import chirp.validation.Rule;
import chirp.validation.Rules;
import chirp.validation.Validate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Strict contract for the library's single email syntax check
 * ({@link Check#isEmail(String)}), shared by {@link Regex#email()}, the
 * {@code EMAIL} rule and {@link Validate#isEmail(String)}.
 */
class EmailValidationTest {

    static Stream<String> validAddresses() {
        return Stream.of(
                "user@example.com",
                "first.last@example.com",
                "user+tag@example.com",
                "o'brien@example.ie",
                "x_y-z@sub.example.co.uk",
                "UPPER@EXAMPLE.COM",
                "1234567890@example.com",
                "user@123.example.com",
                "user@a-b.example.com",
                "!#$%&'*+/=?^_`{|}~-@example.org",
                "a.b+c@x.co",
                "saad@umt.edu.pk",
                "user@xn--bcher-kva.example",
                "user@example.xn--p1ai");
    }

    static Stream<String> invalidAddresses() {
        return Stream.of(
                "",
                " ",
                "plainaddress",
                "@example.com",
                "user@",
                "user@example",
                "user@localhost",
                "a@b",
                "a@b.c",
                "user@example.c",
                "user@example.c0m",
                "user@example.123",
                "user@1.2.3.4",
                "user@[192.168.0.1]",
                ".user@example.com",
                "user.@example.com",
                "us..er@example.com",
                "user@.example.com",
                "user@example..com",
                "user@example.com.",
                "user@-example.com",
                "user@example-.com",
                "user@exa_mple.com",
                "user name@example.com",
                "user@exa mple.com",
                "\"quoted\"@example.com",
                "user@@example.com",
                "user@example@example.com",
                "user@example,com",
                "user@example.com;other@example.com",
                " user@example.com",
                "user@example.com ",
                "user@example.com\n",
                "user@example.com\r\nBcc: victim@example.com",
                "üser@example.com",
                "user@exämple.com",
                "user@example.xn--",
                "user@example.xn--p1ai-");
    }

    @ParameterizedTest
    @MethodSource("validAddresses")
    void acceptsRealisticAddresses(String address) {
        assertTrue(Check.isEmail(address), address);
    }

    @ParameterizedTest
    @MethodSource("invalidAddresses")
    void rejectsMalformedAddresses(String address) {
        assertFalse(Check.isEmail(address), address);
    }

    @Test
    void nullIsNotAnEmail() {
        assertFalse(Check.isEmail(null));
    }

    // ---------------------------------------------------------------- length limits

    @Test
    void localPartIsLimitedTo64Characters() {
        assertTrue(Check.isEmail("a".repeat(64) + "@example.com"));
        assertFalse(Check.isEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    void domainLabelIsLimitedTo63Characters() {
        assertTrue(Check.isEmail("user@" + "a".repeat(63) + ".com"));
        assertFalse(Check.isEmail("user@" + "a".repeat(64) + ".com"));
    }

    @Test
    void topLevelDomainNeedsTwoTo63Letters() {
        assertFalse(Check.isEmail("user@example.c"));
        assertTrue(Check.isEmail("user@example.co"));
        assertTrue(Check.isEmail("user@example." + "a".repeat(63)));
        assertFalse(Check.isEmail("user@example." + "a".repeat(64)));
    }

    @Test
    void wholeAddressIsLimitedTo254Characters() {
        String local = "a".repeat(64);
        String twoFullLabels = "b".repeat(63) + "." + "c".repeat(63) + ".";
        String at254 = local + "@" + twoFullLabels + "d".repeat(57) + ".com";
        String at255 = local + "@" + twoFullLabels + "d".repeat(58) + ".com";
        assertEquals(254, at254.length());
        assertEquals(255, at255.length());
        assertTrue(Check.isEmail(at254));
        assertFalse(Check.isEmail(at255));
    }

    @Test
    void adversarialInputIsRejectedQuickly() {
        String hugeLocalPart = "a".repeat(100_000) + "@example.com";
        String manyLabels = "user@" + "a.".repeat(50_000) + "com";
        String hyphenRun = "user@" + "a-".repeat(50_000) + "!";
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertFalse(Check.isEmail(hugeLocalPart));
            assertFalse(Check.isEmail(manyLabels));
            assertFalse(Check.isEmail(hyphenRun));
        });
    }

    // ---------------------------------------------------------------- one definition everywhere

    static Stream<String> allAddresses() {
        return Stream.concat(validAddresses(), invalidAddresses());
    }

    @Test
    void aliasesShareTheSameCheck() {
        allAddresses().forEach(a -> {
            boolean expected = Check.isEmail(a);
            assertEquals(expected, Check.looksLikeEmail(a), a);
            assertEquals(expected, Check.validEmail(a), a);
            assertEquals(expected, Check.isEmailAddress(a), a);
        });
    }

    @ParameterizedTest
    @MethodSource("allAddresses")
    void regexEmailMatchesExactlyLikeCheck(String address) {
        assertEquals(Check.isEmail(address), address.matches(Regex.email()), address);
    }

    @ParameterizedTest
    @MethodSource("allAddresses")
    void emailRuleAgreesWithCheckAfterTrimming(String address) {
        Rule rule = Rules.get("EMAIL");
        assertNotNull(rule);
        assertEquals(Check.isEmail(address.trim()), rule.test(address), address);
    }

    @ParameterizedTest
    @MethodSource("allAddresses")
    void validateAgreesWithCheck(String address) {
        assertEquals(Check.isEmail(address),
                Validate.that(address).isEmail("invalid").validate().isValid(), address);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a.b+c@x.co"})
    void regexIsAnchoredToTheWholeInput(String address) {
        assertFalse(("prefix " + address).matches(Regex.email()));
        assertFalse((address + " suffix").matches(Regex.email()));
    }
}
