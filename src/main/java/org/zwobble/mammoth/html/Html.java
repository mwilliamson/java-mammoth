package org.zwobble.mammoth.html;

import java.util.List;

import static org.zwobble.mammoth.util.MammothLists.list;

public class Html {
    public static String write(List<HtmlNode> nodes) {
        StringBuilder builder = new StringBuilder();
        nodes.forEach(node -> node.write(builder));
        return builder.toString();
    }

    public static HtmlNode text(String value) {
        return new HtmlTextNode(value);
    }

    public static HtmlNode element(String tagName) {
        return element(tagName, list());
    }

    public static HtmlNode element(String tagName, List<HtmlNode> children) {
        return new HtmlElementNode(tagName, children);
    }

    public static HtmlNode selfClosingElement(String tagName) {
        return new HtmlSelfClosingElement(tagName);
    }
}
