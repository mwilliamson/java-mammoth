package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.parboiled.support.Var;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;
import org.zwobble.mammoth.internal.styles.parsing.Parsing;
import org.zwobble.mammoth.internal.styles.parsing.StyleMappingParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

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
            deepEquals(new HtmlPath(list(HtmlPathElement.collapsible("ul"), HtmlPathElement.collapsible("li")))));
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
    public void canReadWhenElementMustBeFresh() {
        assertThat(
            parseHtmlPath("p:fresh"),
            deepEquals(HtmlPath.element("p")));
    }

    private HtmlPath parseHtmlPath(String input) {
        Var<HtmlPath> path = new Var<>();
        Parsing.parse(StyleMappingParser.class, parser -> parser.HtmlPath(path), input);
        return path.get();
    }
}
