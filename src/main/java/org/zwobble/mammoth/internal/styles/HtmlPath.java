package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.html.HtmlNode;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

public interface HtmlPath {
    HtmlPath EMPTY = new HtmlPathElements(list());
    HtmlPath IGNORE = Ignore.INSTANCE;

    static HtmlPath elements(HtmlPathElement... elements) {
        return new HtmlPathElements(asList(elements));
    }

    static HtmlPath element(String tagName) {
        return element(tagName, map());
    }

    static HtmlPath element(String tagName, Map<String, String> attributes) {
        return new HtmlPathElements(list(new HtmlPathElement(list(tagName), attributes, false)));
    }

    static HtmlPath collapsibleElement(String tagName) {
        return collapsibleElement(tagName, map());
    }

    static HtmlPath collapsibleElement(List<String> tagNames) {
        return collapsibleElement(tagNames, map());
    }

    static HtmlPath collapsibleElement(String tagName, Map<String, String> attributes) {
        return collapsibleElement(list(tagName), attributes);
    }

    static HtmlPath collapsibleElement(List<String> tagNames, Map<String, String> attributes) {
        return new HtmlPathElements(list(new HtmlPathElement(tagNames, attributes, true)));
    }

    Supplier<List<HtmlNode>> wrap(Supplier<List<HtmlNode>> generateNodes);
}
