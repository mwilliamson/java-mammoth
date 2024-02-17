package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.parsing.HtmlPathParser;
import org.zwobble.mammoth.internal.styles.parsing.StyleMappingTokeniser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class HtmlPathParsingTests {
    @Test
    public void canParseEmptyPath() {
        assertThat(
            parseHtmlPath(""),
            deepEquals(HtmlPath.EMPTY));
    }

    @Test
    public void canReadSingleElement() {
        assertThat(
            parseHtmlPath("p"),
            deepEquals(HtmlPath.collapsibleElement("p")));
    }

    @Test
    public void canReadElementWithChoiceOfTagNames() {
        assertThat(
            parseHtmlPath("ul|ol"),
            deepEquals(HtmlPath.collapsibleElement(list("ul", "ol"))));

        assertThat(
            parseHtmlPath("ul|ol|p"),
            deepEquals(HtmlPath.collapsibleElement(list("ul", "ol", "p"))));
    }

    @Test
    public void canReadNestedElements() {
        assertThat(
            parseHtmlPath("ul > li"),
            deepEquals(HtmlPath.elements(
                HtmlElementBuilder.collapsible("ul").pathElement(),
                HtmlElementBuilder.collapsible("li").pathElement()
            )));
    }

    @Test
    public void canReadClassOnElement() {
        assertThat(
            parseHtmlPath("p.tip"),
            deepEquals(HtmlPath.collapsibleElement("p", map("class", "tip"))));
    }

    @Test
    public void canReadMultipleClassesOnElement() {
        assertThat(
            parseHtmlPath("p.tip.help"),
            deepEquals(HtmlPath.collapsibleElement("p", map("class", "tip help"))));
    }

    @Test
    public void canReadAttributeOnElement() {
        assertThat(
            parseHtmlPath("p[lang='fr']"),
            deepEquals(HtmlPath.collapsibleElement("p", map("lang", "fr"))));
    }

    @Test
    public void canReadMultipleAttributesOnElement() {
        assertThat(
            parseHtmlPath("p[lang='fr'][data-x='y']"),
            deepEquals(HtmlPath.collapsibleElement("p", map("lang", "fr", "data-x", "y"))));
    }

    @Test
    public void canReadWhenElementMustBeFresh() {
        assertThat(
            parseHtmlPath("p:fresh"),
            deepEquals(HtmlPath.element("p")));
    }

    @Test
    public void canReadSeparatorForElements() {
        assertThat(
            parseHtmlPath("p:separator('x')"),
            deepEquals(HtmlPath.elements(HtmlElementBuilder.collapsible("p").separator("x").pathElement()))
        );
    }

    @Test
    public void canReadIgnoreElement() {
        assertThat(
            parseHtmlPath("!"),
            deepEquals(HtmlPath.IGNORE)
        );
    }

    private HtmlPath parseHtmlPath(String input) {
        return HtmlPathParser.parse(StyleMappingTokeniser.tokenise(input));
    }
}
