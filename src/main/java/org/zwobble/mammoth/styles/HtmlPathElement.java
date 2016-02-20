package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.html.HtmlElement;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class HtmlPathElement {
    public static HtmlPathElement fresh(String tagName) {
        return new HtmlPathElement(tagName, map(), false);
    }

    public static HtmlPathElement collapsible(String tagName) {
        return new HtmlPathElement(tagName, map(), true);
    }

    private final String tagName;
    private final Map<String, String> attributes;
    private final boolean isCollapsible;

    public HtmlPathElement(String tagName, Map<String, String> attributes, boolean isCollapsible) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.isCollapsible = isCollapsible;
    }

    public List<HtmlNode> wrap(List<HtmlNode> nodes) {
        return list(new HtmlElement(tagName, attributes, nodes, isCollapsible));
    }
}
