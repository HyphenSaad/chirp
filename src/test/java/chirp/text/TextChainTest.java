package chirp.text;

import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class TextChainTest {

    // ---------------------------------------------------------- factories

    @Test
    void ofStringWraps() {
        assertEquals("hello", TextChain.of("hello").get());
    }

    @Test
    void ofSupplierLazy() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        TextChain c = TextChain.of(() -> {
            calls.incrementAndGet();
            return "value";
        });
        assertEquals(0, calls.get());
        assertEquals("value", c.get());
        assertEquals(1, calls.get());
    }

    @Test
    void nullValuePropagates() {
        assertNull(TextChain.of((String) null).get());
    }

    // ---------------------------------------------------------- transforms

    @Test
    void capitalizeChain() {
        assertEquals("Hello", TextChain.of("hello").capitalize().get());
    }

    @Test
    void chainedTransforms() {
        String result = TextChain.of("hello world")
                .titleCase()
                .reverse()
                .get();
        assertEquals("dlroW olleH", result);
    }

    @Test
    void appendChain() {
        assertEquals("hello!", TextChain.of("hello").append("!").get());
    }

    @Test
    void prependChain() {
        assertEquals("Hi hello", TextChain.of("hello").prepend("Hi ").get());
    }

    @Test
    void surroundChain() {
        assertEquals("[hello]", TextChain.of("hello").surround("[", "]").get());
    }

    @Test
    void replaceChain() {
        assertEquals("hi world", TextChain.of("hello world").replace("hello", "hi").get());
    }

    @Test
    void nullPropagatesThroughChain() {
        assertNull(TextChain.of((String) null).capitalize().append("!").get());
    }

    @Test
    void uppercaseLowercase() {
        assertEquals("HELLO", TextChain.of("hello").toUpperCase().get());
        assertEquals("hello", TextChain.of("HELLO").toLowerCase().get());
        assertEquals("HELLO", TextChain.of("hello").upper().get());
        assertEquals("hello", TextChain.of("HELLO").lower().get());
    }

    @Test
    void trimChain() {
        assertEquals("hello", TextChain.of("  hello  ").trim().get());
    }

    @Test
    void paddingChains() {
        assertEquals("   hi", TextChain.of("hi").padLeft(5).get());
        assertEquals("hi   ", TextChain.of("hi").padRight(5).get());
        assertEquals(" hi  ", TextChain.of("hi").center(5).get());
    }

    @Test
    void truncateChain() {
        assertEquals("ab…", TextChain.of("abcdef").truncate(3).get());
    }

    @Test
    void removeSpacesChain() {
        assertEquals("helloworld", TextChain.of("hello world").removeSpaces().get());
    }

    // ---------------------------------------------------------- custom step

    @Test
    void thenAppliesCustomOperator() {
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        assertEquals("HELLO!", TextChain.of("hello").then(shout).get());
    }

    @Test
    void thenAliases() {
        UnaryOperator<String> op = s -> s + "!";
        assertEquals("hi!", TextChain.of("hi").map(op).get());
        assertEquals("hi!", TextChain.of("hi").apply(op).get());
    }

    @Test
    void nullOperatorIsIgnored() {
        assertEquals("hi", TextChain.of("hi").then(null).get());
    }

    // ---------------------------------------------------------- terminal accessors

    @Test
    void allTerminalAccessors() {
        TextChain c = TextChain.of("hi");
        assertEquals("hi", c.get());
        assertEquals("hi", c.value());
        assertEquals("hi", c.result());
        assertEquals("hi", c.toString());
    }

    // ---------------------------------------------------------- integration

    @Test
    void integrationWithText() {
        // The chain is the same as building with Text directly
        String viaChain = TextChain.of("  hello world  ")
                .trim()
                .titleCase()
                .append("!")
                .get();
        assertEquals("Hello World!", viaChain);

        // Alternative via Text — same result
        String viaText = Text.titleCase("hello world") + "!";
        assertEquals(viaChain, viaText);
    }

    @Test
    void integrationFromTextFactory() {
        assertEquals("Hello", Text.chain("hello").capitalize().get());
        assertEquals("Hello", Text.of("hello").capitalize().get());
        assertEquals("Hello", Text.on("hello").capitalize().get());
    }
}
