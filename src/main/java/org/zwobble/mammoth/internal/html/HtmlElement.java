package org.zwobble.mammoth.internal.html;

import java.util.List;
import java.util.Map;

public class HtmlElement implements HtmlNode {
    private final HtmlTag tag;
    private final List<HtmlNode> children;

    public HtmlElement(HtmlTag tag, List<HtmlNode> children) {
        this.tag = tag;
        this.children = children;
    }

    public HtmlTag getTag() {
        return tag;
    }

    public List<String> getTagNames() {
        return tag.getTagNames();
    }

    public String getTagName() {
        return getTagNames().get(0);
    }

    public Map<String, String> getAttributes() {
        return tag.getAttributes();
    }

    public List<HtmlNode> getChildren() {
        return children;
    }

    public boolean isCollapsible() {
        return tag.isCollapsible();
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
