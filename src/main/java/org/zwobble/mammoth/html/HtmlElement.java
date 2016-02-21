package org.zwobble.mammoth.html;

import java.util.List;
import java.util.Map;

public class HtmlElement implements HtmlNode {
    private final List<String> tagNames;
    private final Map<String, String> attributes;
    private final List<HtmlNode> children;
    private final boolean collapsible;

    public HtmlElement(List<String> tagNames, Map<String, String> attributes, List<HtmlNode> children, boolean collapsible) {
        this.tagNames = tagNames;
        this.attributes = attributes;
        this.children = children;
        this.collapsible = collapsible;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public String getTagName() {
        return tagNames.get(0);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<HtmlNode> getChildren() {
        return children;
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Mapper<T> visitor) {
        return visitor.visit(this);
    }
}
