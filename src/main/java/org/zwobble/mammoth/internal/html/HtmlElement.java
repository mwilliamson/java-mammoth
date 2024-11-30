package org.zwobble.mammoth.internal.html;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zwobble.mammoth.internal.util.Sets.set;

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

    public String getSeparator() {
        return tag.getSeparator();
    }

    public boolean isVoid() {
        return children.isEmpty() && isVoidTag(getTagName());
    }

    private static final Set<String> VOID_TAG_NAMES = set("br", "hr", "img", "input");

    private static boolean isVoidTag(String tagName) {
        return VOID_TAG_NAMES.contains(tagName);
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
