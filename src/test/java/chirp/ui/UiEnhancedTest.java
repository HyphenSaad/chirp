package chirp.ui;

import chirp.validation.Rule;
import chirp.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;

import static org.junit.jupiter.api.Assertions.*;

class UiEnhancedTest {

    // ============================================================== UiApp key-value

    @Test
    void uiAppFromKeyValue() {
        UiApp app = UiApp.from(
                UiApp.Key.TITLE, "KeyTest",
                UiApp.Key.WIDTH, 500,
                UiApp.Key.HEIGHT, 400,
                UiApp.Key.DARK, true);
        assertEquals("KeyTest", app.title());
        assertEquals(500, app.width());
        assertEquals(400, app.height());
        assertEquals(UiTheme.DARK, app.theme());
    }

    @Test
    void uiAppFromNullPairsSafe() {
        UiApp app = UiApp.from((Object[]) null);
        assertEquals("Chirp", app.title());
    }

    @Test
    void uiAppFromWrongTypesSafe() {
        UiApp app = UiApp.from(
                UiApp.Key.WIDTH, "not a number",
                UiApp.Key.DARK, "not a boolean");
        assertEquals(900, app.width());
        assertEquals(UiTheme.AUTO, app.theme());
    }

    @Test
    void uiAppTitleAndSizeAliases() {
        UiApp a = UiApp.builder().windowTitle("W").dimensions(800, 600).build();
        assertEquals("W", a.windowTitle());
        assertEquals(800, a.width());
        assertEquals(600, a.height());

        UiApp b = UiApp.builder().caption("Cap").build();
        assertEquals("Cap", b.title());
    }

    @Test
    void uiAppFixedAndFlexible() {
        assertFalse(UiApp.builder().fixedSize().build().resizable());
        assertFalse(UiApp.builder().fixed().build().resizable());
        assertTrue(UiApp.builder().flexible().build().resizable());
    }

    // ============================================================== UiStyle presets

    @Test
    void uiStylePresets() {
        assertNotNull(UiStyle.modernLight());
        assertNotNull(UiStyle.modernDark());
        assertNotNull(UiStyle.compact());
        assertNotNull(UiStyle.largeText());
        assertNotNull(UiStyle.monospace());
        assertNotNull(UiStyle.defaultStyle());
    }

    @Test
    void uiStyleLargeTextIsBigger() {
        assertTrue(UiStyle.largeText().fontSize() > UiStyle.defaults().fontSize());
    }

    @Test
    void uiStyleCompactIsSmaller() {
        assertTrue(UiStyle.compact().fontSize() < UiStyle.defaults().fontSize());
    }

    @Test
    void uiStyleKeyValue() {
        UiStyle.apply(
                UiStyle.Key.FONT_SIZE, 18,
                UiStyle.Key.PRIMARY_COLOR, "#ff0000");
        assertEquals(18.0, UiStyle.current().fontSize(), 0.01);
        assertEquals("#ff0000", UiStyle.current().primaryColor());

        UiStyle.reset();
        assertEquals(14.0, UiStyle.current().fontSize(), 0.01);
    }

    // ============================================================== Button

    @Test
    void buttonFactoryAliases() {
        assertNotNull(Button.create("x"));
        assertNotNull(Button.button("x"));
        assertNotNull(Button.make("x"));
        assertNotNull(Button.default_("x"));
        assertNotNull(Button.main("x"));
        assertNotNull(Button.alt("x"));
        assertNotNull(Button.secondaryButton("x"));
        assertNotNull(Button.ok("x"));
        assertNotNull(Button.good("x"));
        assertNotNull(Button.warn("x"));
        assertNotNull(Button.destructive("x"));
        assertNotNull(Button.remove("x"));
    }

    @Test
    void buttonSettersAliases() {
        Button b = Button.primary("x").text("y");
        assertEquals("y", b.getText());
        b.label("z").title("w");
        assertEquals("w", b.getText());

        b.onClick(() -> {});
        b.onPress(() -> {});
        b.on(() -> {});
        b.action(() -> {});
        b.handle(() -> {});
    }

    @Test
    void buttonPredicates() {
        assertTrue(Button.primary("x").isPrimary());
        assertTrue(Button.danger("x").isDanger());
        Button outline = Button.primary("x").outline();
        assertTrue(outline.isOutline());
        outline.filled();
        assertFalse(outline.isOutline());

        assertFalse(Button.primary("x").isDisabled());
        assertTrue(Button.primary("x").disable().isDisabled());
    }

    // ============================================================== Label

    @Test
    void labelFactoryAliases() {
        assertNotNull(Label.create("x"));
        assertNotNull(Label.make("x"));
        assertNotNull(Label.empty());
        assertNotNull(Label.h1("x"));
        assertNotNull(Label.title("x"));
        assertNotNull(Label.h2("x"));
        assertNotNull(Label.dim("x"));
        assertNotNull(Label.err("x"));
        assertNotNull(Label.danger("x"));
        assertNotNull(Label.ok("x"));
        assertNotNull(Label.good("x"));
    }

    @Test
    void labelTextAliases() {
        Label l = Label.of("a").text("b");
        assertEquals("b", l.getText());
        l.value("c");
        assertEquals("c", l.getText());
        l.label("d");
        assertEquals("d", l.getText());
    }

    @Test
    void labelPredicates() {
        assertTrue(Label.heading("x").isHeading());
        assertTrue(Label.subheading("x").isSubheading());
        assertTrue(Label.muted("x").isMuted());
        assertTrue(Label.error("x").isError());
        assertTrue(Label.success("x").isSuccess());
    }

    // ============================================================== TextField

    @Test
    void textFieldFactoryAliases() {
        assertNotNull(TextField.create());
        assertNotNull(TextField.empty());
        assertNotNull(TextField.textField());
        assertNotNull(TextField.textField("initial"));
    }

    @Test
    void textFieldValidatorsAliases() {
        TextField f = TextField.of().required();
        assertFalse(f.isValid());
        f.text("x");
        assertTrue(f.isValid());

        f = TextField.of().mandatory();
        assertFalse(f.isValid());

        assertFalse(TextField.of().email().text("bad").isValid());
        assertTrue(TextField.of().isEmail().text("a@b.co").isValid());
        assertTrue(TextField.of().mail().text("a@b.co").isValid());

        assertFalse(TextField.of().phone().text("abc").isValid());
        assertFalse(TextField.of().telephone().text("abc").isValid());

        assertFalse(TextField.of().url().text("not-a-url").isValid());
        assertFalse(TextField.of().link().text("not-a-url").isValid());

        assertTrue(TextField.of().numeric().text("42.5").isValid());
        assertTrue(TextField.of().number().text("42").isValid());
        assertTrue(TextField.of().decimal().text("42.5").isValid());

        assertFalse(TextField.of().minLength(5).text("abc").isValid());
        assertFalse(TextField.of().atLeast(5).text("abc").isValid());
        assertFalse(TextField.of().min(5).text("abc").isValid());

        assertFalse(TextField.of().maxLength(2).text("abc").isValid());
        assertFalse(TextField.of().atMost(2).text("abc").isValid());
        assertFalse(TextField.of().max(2).text("abc").isValid());
    }

    @Test
    void textFieldRuleByName() {
        TextField f = TextField.of().rule("EMAIL");
        f.text("user@example.com");
        assertTrue(f.isValid());
        f.text("not-email");
        assertFalse(f.isValid());
    }

    @Test
    void textFieldStatusAliases() {
        TextField f = TextField.of().required();
        assertFalse(f.isValid());
        assertFalse(f.valid());
        assertFalse(f.ok());
        assertFalse(f.check());
        assertFalse(f.verify());
        f.text("hello");
        assertTrue(f.check());
        assertTrue(f.verify());
    }

    @Test
    void textFieldErrorAliases() {
        TextField f = TextField.of().required().text("");
        assertEquals(f.error(), f.message());
        assertEquals(f.error(), f.getError());
    }

    // ============================================================== Stack / Row

    @Test
    void stackAliases() {
        Stack s = Stack.stack();
        assertEquals(0, s.getChildren().size());

        s.gap(10).spacing(10).space(10);
        assertEquals(10.0, s.getSpacing(), 0.01);

        s.pad(20).inset(20);
        s.align(Pos.CENTER).alignment(Pos.CENTER).position(Pos.CENTER).pos(Pos.CENTER);
        s.centered().center();
    }

    @Test
    void stackAddAliases() {
        Stack s = Stack.of();
        s.add(Label.of("a"));
        s.append(Label.of("b"));
        s.include(Label.of("c"));
        s.child(Label.of("d"));
        assertEquals(4, s.getChildren().size());
    }

    @Test
    void rowAliases() {
        Row r = Row.row();
        r.gap(8).spacing(8).space(8);
        r.pad(10).inset(10);
        r.align(Pos.CENTER).centered().center();

        r.add(Label.of("a")).append(Label.of("b")).include(Label.of("c"));
        assertEquals(3, r.getChildren().size());

        assertNotNull(Row.line());
        assertNotNull(Row.horizontal());
        assertNotNull(Row.of(6));
    }

    // ============================================================== Card

    @Test
    void cardFactoriesAndSetters() {
        Card c = Card.create().heading("Title").subheading("Subtitle");
        c.body(Label.of("Body"));
        c.actions(Button.primary("OK"));
        assertNotNull(c);
    }

    @Test
    void cardAliases() {
        Card c = Card.of().title("T").subtitle("S");
        c.content(Label.of("x")).add(Label.of("y")).children(Label.of("z")).with(Label.of("w"));
        c.footer(Button.primary("A")).actions(Button.primary("B")).buttons(Button.primary("C"));
        assertNotNull(c);
    }

    // ============================================================== FormBuilder

    @Test
    void formBuilderBuildsStack() {
        Stack s = FormBuilder.create("Register")
                .field("name", Field.of("Name").required())
                .field("email", Field.of("Email").email())
                .submit("Register", values -> {})
                .build();
        assertNotNull(s);
        assertTrue(s.getChildren().size() >= 4);
    }

    @Test
    void formBuilderValidateReturnsErrors() {
        FormBuilder form = FormBuilder.create("Test")
                .field("name", Field.of("Name").required())
                .field("email", Field.of("Email").email());
        ValidationResult result = form.validate();
        assertTrue(result.hasErrors());
        assertTrue(result.count() >= 2);
    }

    @Test
    void formBuilderValidFieldsReturnEmptyResult() {
        FormBuilder form = FormBuilder.create("Test")
                .field("name", Field.of("Name").required().value("Saad"))
                .field("email", Field.of("Email").email().value("a@b.co"));
        ValidationResult result = form.validate();
        assertFalse(result.hasErrors(), result.join(", "));
    }

    @Test
    void formBuilderValues() {
        FormBuilder form = FormBuilder.create("Test")
                .field("name", Field.of("Name").value("Saad"))
                .field("age", Field.of("Age").value("25"));
        Map<String, String> values = form.values();
        assertEquals("Saad", values.get("name"));
        assertEquals("25", values.get("age"));
    }

    // ============================================================== UiIntegration

    @Test
    void bindRuleByName() {
        Field f = UiIntegration.bindRule(Field.of("Email"), "EMAIL");
        f.value("user@example.com");
        assertTrue(f.isValid());
        f.value("bad");
        assertFalse(f.isValid());
    }

    @Test
    void bindRuleAliases() {
        Field f = UiIntegration.applyRule(Field.of("Email"), "EMAIL");
        f.value("user@example.com");
        assertTrue(f.isValid());

        Field f2 = UiIntegration.bindToRule(Field.of("Email"), "EMAIL");
        f2.value("user@example.com");
        assertTrue(f2.isValid());
    }

    @Test
    void ruleField() {
        Field f = UiIntegration.ruleField("Email", "EMAIL");
        f.value("user@example.com");
        assertTrue(f.isValid());

        assertNotNull(UiIntegration.createRuleField("Email", "EMAIL"));
    }

    @Test
    void checkField() {
        Field f = UiIntegration.checkField("Age", s -> s != null && s.length() > 0, "Required");
        f.value("");
        assertFalse(f.isValid());
        f.value("20");
        assertTrue(f.isValid());
    }

    @Test
    void checkFieldAlias() {
        Field f = UiIntegration.fieldFromCheck("Email", chirp.text.Check::isEmail, "Invalid");
        f.value("a@b.co");
        assertTrue(f.isValid());
    }

    @Test
    void errorsFromResult() {
        ValidationResult result = new ValidationResult(
                List.of("Error 1", "Error 2", "Error 3"));
        Stack errors = UiIntegration.errorsFrom(result);
        assertEquals(3, errors.getChildren().size());

        Stack empty = UiIntegration.errorsFrom(new ValidationResult(List.of()));
        assertEquals(0, empty.getChildren().size());

        assertEquals(0, UiIntegration.errorsFrom(null).getChildren().size());
    }

    @Test
    void errorsFromAliases() {
        ValidationResult result = new ValidationResult(List.of("err"));
        assertEquals(UiIntegration.errorsFrom(result).getChildren().size(),
                UiIntegration.resultToLabels(result).getChildren().size());
        assertEquals(UiIntegration.errorsFrom(result).getChildren().size(),
                UiIntegration.showErrors(result).getChildren().size());
    }

    @Test
    void summaryOf() {
        ValidationResult valid = new ValidationResult(List.of());
        ValidationResult invalid = new ValidationResult(List.of("err1", "err2"));

        assertEquals("", UiIntegration.summaryOf(valid).getText());
        assertEquals("err1", UiIntegration.summaryOf(invalid).getText());
        assertEquals("", UiIntegration.summaryOf(null).getText());
    }

    @Test
    void bindCheck() {
        TextField field = UiIntegration.bindCheck(TextField.of(),
                chirp.text.Check::isEmail, "Invalid email");
        field.text("a@b.co");
        assertTrue(field.isValid());

        TextField f2 = UiIntegration.applyCheck(TextField.of(),
                chirp.text.Check::isPhone, "Invalid phone");
        f2.text("+923001234567");
        assertTrue(f2.isValid());
    }

    @Test
    void nullSafety() {
        assertNull(UiIntegration.bindRule((Field) null, "EMAIL"));
        assertNull(UiIntegration.bindRule((Field) null, (Rule) null));
        assertNotNull(UiIntegration.errorsFrom(null));
        assertNotNull(UiIntegration.summaryOf(null));
    }
}
