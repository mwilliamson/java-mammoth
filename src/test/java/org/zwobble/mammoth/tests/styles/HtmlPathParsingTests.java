package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.zwobble.mammoth.styles.HtmlPath;
import org.zwobble.mammoth.styles.HtmlPathElement;
import org.zwobble.mammoth.styles.StyleMapParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class HtmlPathParsingTests {
    @Test
    public void canParseEmptyPath() {
        assertThat(
            StyleMapParser.parseHtmlPath(""),
            deepEquals(HtmlPath.EMPTY));
    }

    @Test
    public void canReadSingleElement() {
        assertThat(
            StyleMapParser.parseHtmlPath("p"),
            deepEquals(HtmlPath.collapsibleElement("p")));
    }

    // TODO: choice

    @Test
    public void canReadNestedElements() {
        assertThat(
            StyleMapParser.parseHtmlPath("ul > li"),
            deepEquals(new HtmlPath(list(HtmlPathElement.collapsible("ul"), HtmlPathElement.collapsible("li")))));
    }

    @Test
    public void canReadClassOnElement() {
        assertThat(
            StyleMapParser.parseHtmlPath("p.tip"),
            deepEquals(HtmlPath.collapsibleElement("p", map("class", "tip"))));
    }

    @Test
    public void canReadMultipleClassesOnElement() {
        assertThat(
            StyleMapParser.parseHtmlPath("p.tip.help"),
            deepEquals(HtmlPath.collapsibleElement("p", map("class", "tip help"))));
    }

    @Test
    public void canReadWhenElementMustBeFresh() {
        assertThat(
            StyleMapParser.parseHtmlPath("p:fresh"),
            deepEquals(HtmlPath.element("p")));
    }
}
