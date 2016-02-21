package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.html.HtmlElement;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class HtmlPathElement {
    public static HtmlPathElement fresh(String tagName) {
        return new HtmlPathElement(list(tagName), map(), false);
    }

    public static HtmlPathElement collapsible(String tagName) {
        return new HtmlPathElement(list(tagName), map(), true);
    }

    private final List<String> tagNames;
    private final Map<String, String> attributes;
    private final boolean isCollapsible;

    public HtmlPathElement(List<String> tagNames, Map<String, String> attributes, boolean isCollapsible) {
        this.tagNames = tagNames;
        this.attributes = attributes;
        this.isCollapsible = isCollapsible;
    }

    public List<HtmlNode> wrap(List<HtmlNode> nodes) {
        return list(new HtmlElement(tagNames, attributes, nodes, isCollapsible));
    }
}
