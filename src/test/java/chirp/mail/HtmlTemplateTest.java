package chirp.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HtmlTemplateTest {

    @TempDir
    Path dir;

    private String write(String html) throws Exception {
        Path f = dir.resolve("template.html");
        Files.writeString(f, html);
        return f.toString();
    }

    // ---------------------------------------------------- rendering

    @Test
    void simpleSubstitution() {
        HtmlTemplate t = HtmlTemplate.raw("<p>Hello {{name}}</p>")
                .placeholder("name", "Saad");
        assertEquals("<p>Hello Saad</p>", t.render());
    }

    @Test
    void dollarPlaceholdersSupported() {
        HtmlTemplate t = HtmlTemplate.raw("<p>Hi ${name}</p>")
                .placeholder("name", "Ali");
        assertEquals("<p>Hi Ali</p>", t.render());
    }

    @Test
    void multipleSubstitutions() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}} and {{b}}</p>")
                .placeholder("a", "X")
                .placeholder("b", "Y");
        assertEquals("<p>X and Y</p>", t.render());
    }

    @Test
    void placeholdersCanRepeat() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{x}}/{{x}}</p>")
                .placeholder("x", "Q");
        assertEquals("<p>Q/Q</p>", t.render());
    }

    @Test
    void numericValueFormatted() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{amount}}</p>")
                .placeholder("amount", String.format("$%,.2f", 1234.5));
        assertTrue(t.render().contains("$1,234.50"));
    }

    // ---------------------------------------------------- validation

    @Test
    void declaredButNotPresentThrows() {
        HtmlTemplate t = HtmlTemplate.raw("<p>No placeholder</p>")
                .placeholder("missing", "x");
        assertThrows(EmailException.class, t::render);
    }

    @Test
    void undeclaredButPresentThrows() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{oops}}</p>");
        assertThrows(EmailException.class, t::render);
    }

    @Test
    void notHtmlThrows() {
        HtmlTemplate t = HtmlTemplate.raw("just plain text");
        assertThrows(EmailException.class, t::render);
    }

    @Test
    void emptyFileThrows() throws Exception {
        String path = write("");
        assertThrows(EmailException.class, () -> HtmlTemplate.of(path));
    }

    @Test
    void missingFileThrows() {
        assertThrows(EmailException.class,
                () -> HtmlTemplate.of(dir.resolve("nope.html").toString()));
    }

    // ---------------------------------------------------- discovery

    @Test
    void discoveredPlaceholders() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}{{b}}{{a}}</p>")
                .placeholder("a", "1").placeholder("b", "2");
        assertEquals(2, t.discovered().get("a"));
        assertEquals(1, t.discovered().get("b"));
    }

    @Test
    void placeholderNullValueIsEmptyString() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{x}}</p>").placeholder("x", null);
        assertEquals("<p></p>", t.render());
    }

    @Test
    void placeholderInvalidNameThrows() {
        assertThrows(EmailException.class,
                () -> HtmlTemplate.raw("<p>x</p>").placeholder("has space", "v"));
    }

    // ---------------------------------------------------- file source

    @Test
    void fileSourceRenders() throws Exception {
        String path = write("<html><body>Hello {{name}}</body></html>");
        HtmlTemplate t = HtmlTemplate.of(path).placeholder("name", "Saad");
        assertTrue(t.render().contains("Hello Saad"));
    }
}
