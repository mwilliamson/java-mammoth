package org.zwobble.mammoth.html;

import java.util.List;

public class HtmlElementNode implements HtmlNode {
    private final String tagName;
    private final List<HtmlNode> children;

    public HtmlElementNode(String tagName, List<HtmlNode> children) {
        this.tagName = tagName;
        this.children = children;
    }

    @Override
    public void write(StringBuilder builder) {
        builder
            .append("<")
            .append(tagName)
            .append(">");

        children.forEach(node -> node.write(builder));

        builder
            .append("</")
            .append(tagName)
            .append(">");
    }
}
