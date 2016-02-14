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

    public String getTagName() {
        return tagName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<HtmlNode> getChildren() {
        return children;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }
}
