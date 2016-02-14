package org.zwobble.mammoth.tests.html;

import org.junit.Test;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.util.MammothLists.list;

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

    private String write(HtmlNode node) {
        return Html.write(list(node));
    }
}
