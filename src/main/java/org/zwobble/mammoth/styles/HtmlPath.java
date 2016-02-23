package org.zwobble.mammoth.styles;

import com.google.common.collect.Lists;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

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
        for (HtmlPathElement element : Lists.reverse(elements)) {
            nodes = element.wrap(nodes);
        }
        return nodes;
    }
}