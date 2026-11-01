package chirp.mail;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HtmlTemplateEnhancedTest {

    // ============================================================== factories aliases

    @Test
    void factoryAliases() {
        assertThrows(EmailException.class, () -> HtmlTemplate.of("path-not-checked"));
        // of() on missing path throws — verify the aliases exist on inline
        assertEquals("<p>X</p>", HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X").render());
        assertEquals("<p>X</p>", HtmlTemplate.inline("<p>{{a}}</p>").placeholder("a", "X").render());
    }

    @Test
    void ofMissingPathThrows() {
        assertThrows(EmailException.class, () -> HtmlTemplate.of(null));
        assertThrows(EmailException.class, () -> HtmlTemplate.of(""));
    }

    // ============================================================== placeholder aliases

    @Test
    void placeholderAliases() {
        assertEquals("<p>X</p>", HtmlTemplate.raw("<p>{{a}}</p>").set("a", "X").render());
        assertEquals("<p>X</p>", HtmlTemplate.raw("<p>{{a}}</p>").bind("a", "X").render());
        assertEquals("<p>X</p>", HtmlTemplate.raw("<p>{{a}}</p>").with("a", "X").render());
        assertEquals("<p>X</p>", HtmlTemplate.raw("<p>{{a}}</p>").fill("a", "X").render());
    }

    @Test
    void bulkPlaceholdersAliases() {
        Map<String, Object> values = Map.of("a", "1", "b", "2");
        String template = "<p>{{a}}{{b}}</p>";
        assertEquals("<p>12</p>", HtmlTemplate.raw(template).placeholders(values).render());
        assertEquals("<p>12</p>", HtmlTemplate.raw(template).setAll(values).render());
        assertEquals("<p>12</p>", HtmlTemplate.raw(template).bindAll(values).render());
        assertEquals("<p>12</p>", HtmlTemplate.raw(template).fillAll(values).render());
    }

    // ============================================================== render aliases

    @Test
    void renderAliases() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X");
        assertEquals("<p>X</p>", t.render());
        assertEquals("<p>X</p>", t.format());
        assertEquals("<p>X</p>", t.build());
        assertEquals("<p>X</p>", t.toString());
    }

    // ============================================================== safe render

    @Test
    void renderOrDefaultReturnsFallbackOnError() {
        HtmlTemplate t = HtmlTemplate.raw("not html");
        assertEquals("fallback", t.renderOrDefault("fallback"));
        assertEquals("", t.renderOrDefault(null));
    }

    @Test
    void renderOrDefaultReturnsRenderedOnSuccess() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X");
        assertEquals("<p>X</p>", t.renderOrDefault("fallback"));
    }

    @Test
    void renderIfValidReturnsOptional() {
        HtmlTemplate good = HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X");
        HtmlTemplate bad = HtmlTemplate.raw("<p>no placeholder</p>").placeholder("missing", "x");
        assertTrue(good.renderIfValid().isPresent());
        assertTrue(bad.renderIfValid().isEmpty());
    }

    @Test
    void tryRenderAlias() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X");
        assertEquals(t.renderIfValid(), t.tryRender());
    }

    // ============================================================== verify aliases

    @Test
    void verifyAliases() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}</p>").placeholder("a", "X");
        assertDoesNotThrow(t::verify);
        assertDoesNotThrow(t::validate);
        assertDoesNotThrow(t::check);
    }

    @Test
    void verifyThrowsForUndeclaredPlaceholder() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{x}}</p>");
        assertThrows(EmailException.class, t::verify);
    }

    // ============================================================== accessors

    @Test
    void rawSource() {
        String html = "<p>{{a}}</p>";
        assertEquals(html, HtmlTemplate.raw(html).source());
        assertEquals(html, HtmlTemplate.raw(html).raw());
    }

    @Test
    void placeholderDiscoveryAliases() {
        HtmlTemplate t = HtmlTemplate.raw("<p>{{a}}{{b}}</p>")
                .placeholder("a", "1").placeholder("b", "2");
        assertEquals(t.discovered(), t.placeholdersFound());
    }
}
