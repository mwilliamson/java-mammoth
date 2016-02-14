package org.zwobble.mammoth.html;

import java.util.Map;

public class HtmlSelfClosingElement implements HtmlNode {
    private final String tagName;
    private final Map<String, String> attributes;

    public HtmlSelfClosingElement(String tagName, Map<String, String> attributes) {
        this.tagName = tagName;
        this.attributes = attributes;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("<").append(tagName);
        HtmlWriter.generateAttributes(attributes, builder);
        builder.append(" />");
    }
}
