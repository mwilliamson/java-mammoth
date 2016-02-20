package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.html.HtmlElement;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class HtmlPathElement {
    private final String tagName;
    private final boolean isCollapsible;

    public HtmlPathElement(String tagName, boolean isCollapsible) {
        this.tagName = tagName;
        this.isCollapsible = isCollapsible;
    }

    public List<HtmlNode> wrap(List<HtmlNode> nodes) {
        return list(new HtmlElement(tagName, map(), nodes, isCollapsible));
    }
}
