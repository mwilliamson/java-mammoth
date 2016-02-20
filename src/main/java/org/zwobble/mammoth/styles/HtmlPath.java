package org.zwobble.mammoth.styles;

import com.google.common.collect.Lists;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;

public class HtmlPath {
    public static HtmlPathElement element(String tagName) {
        return new HtmlPathElement(tagName, false);
    }

    public static HtmlPathElement collapsibleElement(String tagName) {
        return new HtmlPathElement(tagName, true);
    }

    private final List<HtmlPathElement> elements;

    public HtmlPath(List<HtmlPathElement> elements) {
        this.elements = elements;
    }

    public List<HtmlNode> wrap(List<HtmlNode> nodes) {
        for (HtmlPathElement element : Lists.reverse(elements)) {
            nodes = element.wrap(nodes);
        }
        return nodes;
    }
}
