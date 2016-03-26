package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.html.HtmlNode;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Lists.reversed;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlPath {
    public static final HtmlPath EMPTY = new HtmlPath(list());

    public static HtmlPath element(String tagName) {
        return element(tagName, map());
    }

    public static HtmlPath element(String tagName, Map<String, String> attributes) {
        return new HtmlPath(list(new HtmlPathElement(list(tagName), attributes, false)));
    }

    public static HtmlPath collapsibleElement(String tagName) {
        return collapsibleElement(tagName, map());
    }

    public static HtmlPath collapsibleElement(List<String> tagNames) {
        return collapsibleElement(tagNames, map());
    }

    public static HtmlPath collapsibleElement(String tagName, Map<String, String> attributes) {
        return collapsibleElement(list(tagName), attributes);
    }

    public static HtmlPath collapsibleElement(List<String> tagNames, Map<String, String> attributes) {
        return new HtmlPath(list(new HtmlPathElement(tagNames, attributes, true)));
    }

    private final List<HtmlPathElement> elements;

    public HtmlPath(List<HtmlPathElement> elements) {
        this.elements = elements;
    }

    public List<HtmlNode> wrap(List<HtmlNode> nodes) {
        for (HtmlPathElement element : reversed(elements)) {
            nodes = element.wrap(nodes);
        }
        return nodes;
    }
}
