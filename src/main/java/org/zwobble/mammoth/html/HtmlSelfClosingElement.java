package org.zwobble.mammoth.html;

import java.util.Map;

public class HtmlSelfClosingElement implements HtmlNode {
    private final String tagName;
    private final Map<String, String> attributes;

    public HtmlSelfClosingElement(String tagName, Map<String, String> attributes) {
        this.tagName = tagName;
        this.attributes = attributes;
    }

    public String getTagName() {
        return tagName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }
}
