package org.zwobble.mammoth.internal.html;

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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Mapper<T> visitor) {
        return visitor.visit(this);
    }
}
