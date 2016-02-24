package org.zwobble.mammoth.tests.html;

import org.junit.Test;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;

public class HtmlWriteTests {
    @Test
    public void textNodesAreWrittenAsPlainText() {
        assertEquals(
            "Dark Blue",
            write(Html.text("Dark Blue")));
    }

    @Test
    public void textNodesAreHtmlEscaped() {
        assertEquals(
            "&gt;&lt;&amp;",
            write(Html.text("><&")));
    }

    @Test
    public void doubleQuotesInTextNodesArentHtmlEscaped() {
        assertEquals(
            "&gt;&lt;&amp;\"",
            write(Html.text("><&\"")));
    }

    @Test
    public void canWriteSelfClosingElement() {
        assertEquals(
            "<br />",
            write(Html.selfClosingElement("br")));
    }

    @Test
    public void canWriteSelfClosingElementWithAttributes() {
        assertEquals(
            "<img class=\"external\" src=\"http://example.com\" />",
            write(Html.selfClosingElement("img", map("class", "external", "src", "http://example.com"))));
    }

    @Test
    public void canWriteElementWithNoChildren() {
        assertEquals(
            "<p></p>",
            write(Html.element("p")));
    }

    @Test
    public void canWriteElementWithChildren() {
        assertEquals(
            "<div><p></p><ul></ul></div>",
            write(Html.element("div", list(
                Html.element("p"),
                Html.element("ul")))));
    }

    @Test
    public void canWriteElementWithAttributes() {
        assertEquals(
            "<a class=\"external\" href=\"http://example.com\"></a>",
            write(Html.element("a", map("class", "external", "href", "http://example.com"))));
    }

    @Test
    public void attributeValuesAreEscaped() {
        assertEquals(
            "<a href=\"&gt;&lt;&amp;&quot;\"></a>",
            write(Html.element("a", map("href", "><&\""))));
    }

    private String write(HtmlNode node) {
        return Html.write(list(node));
    }
}
