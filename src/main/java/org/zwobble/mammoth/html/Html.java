package org.zwobble.mammoth.html;

import org.zwobble.mammoth.util.MammothLists;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class Html {
    public static final HtmlNode FORCE_WRITE = HtmlForceWrite.FORCE_WRITE;

    public static String write(List<HtmlNode> nodes) {
        StringBuilder builder = new StringBuilder();
        nodes.forEach(node -> HtmlWriter.write(node, builder));
        return builder.toString();
    }

    public static HtmlNode text(String value) {
        return new HtmlTextNode(value);
    }

    public static HtmlNode element(String tagName) {
        return element(tagName, list());
    }

    public static HtmlNode element(String tagName, Map<String, String> attributes) {
        return element(tagName, attributes, list());
    }

    public static HtmlNode element(String tagName, List<HtmlNode> children) {
        return element(tagName, map(), children);
    }

    public static HtmlNode element(String tagName, Map<String, String> attributes, List<HtmlNode> children) {
        return new HtmlElement(tagName, attributes, children, false);
    }

    public static HtmlNode collapsibleElement(String tagName, List<HtmlNode> children) {
        return collapsibleElement(tagName, map(), children);
    }

    public static HtmlNode collapsibleElement(String tagName, Map<String, String> attributes, List<HtmlNode> children) {
        return new HtmlElement(tagName, attributes, children, true);
    }

    public static HtmlNode selfClosingElement(String tagName) {
        return selfClosingElement(tagName, map());
    }

    public static HtmlNode selfClosingElement(String tagName, Map<String, String> attributes) {
        return new HtmlSelfClosingElement(tagName, attributes);
    }

    public static List<HtmlNode> stripEmpty(List<HtmlNode> nodes) {
        return MammothLists.eagerFlatMap(nodes, node -> stripEmpty(node));
    }

    private static List<HtmlNode> stripEmpty(HtmlNode node) {
        return node.accept(new HtmlNode.Mapper<List<HtmlNode>>() {
            @Override
            public List<HtmlNode> visit(HtmlElement element) {
                List<HtmlNode> children = stripEmpty(element.getChildren());
                if (children.isEmpty()) {
                    return list();
                } else {
                    return list(new HtmlElement(
                        element.getTagName(),
                        element.getAttributes(),
                        children,
                        element.isCollapsible()));
                }
            }

            @Override
            public List<HtmlNode> visit(HtmlSelfClosingElement element) {
                return list(element);
            }

            @Override
            public List<HtmlNode> visit(HtmlTextNode node) {
                if (node.getValue().isEmpty()) {
                    return list();
                } else {
                    return list(node);
                }
            }

            @Override
            public List<HtmlNode> visit(HtmlForceWrite forceWrite) {
                return list(forceWrite);
            }
        });
    }

    public static List<HtmlNode> collapse(List<HtmlNode> nodes) {
        return nodes;
    }
}
