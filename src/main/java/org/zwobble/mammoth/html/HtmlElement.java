package org.zwobble.mammoth.html;

import java.util.List;
import java.util.Map;

public class HtmlElement implements HtmlNode {
    private final String tagName;
    private final Map<String, String> attributes;
    private final List<HtmlNode> children;

    public HtmlElement(String tagName, Map<String, String> attributes, List<HtmlNode> children) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("<").append(tagName);

        HtmlWriter.generateAttributes(attributes, builder);

        builder.append(">");

        children.forEach(node -> node.write(builder));

        builder
            .append("</")
            .append(tagName)
            .append(">");
    }
}
