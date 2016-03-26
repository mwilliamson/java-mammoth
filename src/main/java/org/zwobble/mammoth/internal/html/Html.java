package org.zwobble.mammoth.internal.html;

import org.zwobble.mammoth.internal.util.MammothLists;
import org.zwobble.mammoth.internal.util.MammothOptionals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.internal.util.Casts.tryCast;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothLists.tryGetLast;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;

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
        return new HtmlElement(list(tagName), attributes, children, false);
    }

    public static HtmlNode collapsibleElement(String tagName) {
        return collapsibleElement(list(tagName));
    }

    public static HtmlNode collapsibleElement(List<String> tagNames) {
        return collapsibleElement(tagNames, map(), list());
    }

    public static HtmlNode collapsibleElement(String tagName, List<HtmlNode> children) {
        return collapsibleElement(tagName, map(), children);
    }

    public static HtmlNode collapsibleElement(String tagName, Map<String, String> attributes, List<HtmlNode> children) {
        return collapsibleElement(list(tagName), attributes, children);
    }

    public static HtmlNode collapsibleElement(List<String> tagNames, Map<String, String> attributes, List<HtmlNode> children) {
        return new HtmlElement(tagNames, attributes, children, true);
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
                        element.getTagNames(),
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
        List<HtmlNode> collapsed = new ArrayList<>();

        for (HtmlNode node : nodes) {
            collapsingAdd(collapsed, node);
        }

        return collapsed;
    }

    private static void collapsingAdd(List<HtmlNode> collapsed, HtmlNode node) {
        HtmlNode collapsedNode = collapse(node);
        if (!tryCollapse(collapsed, collapsedNode)) {
            collapsed.add(collapsedNode);
        }
    }

    private static HtmlNode collapse(HtmlNode node) {
        return node.accept(new HtmlNode.Mapper<HtmlNode>() {
            @Override
            public HtmlNode visit(HtmlElement element) {
                return new HtmlElement(
                    element.getTagNames(),
                    element.getAttributes(),
                    collapse(element.getChildren()),
                    element.isCollapsible());
            }

            @Override
            public HtmlNode visit(HtmlSelfClosingElement element) {
                return element;
            }

            @Override
            public HtmlNode visit(HtmlTextNode node) {
                return node;
            }

            @Override
            public HtmlNode visit(HtmlForceWrite forceWrite) {
                return forceWrite;
            }
        });
    }

    private static boolean tryCollapse(List<HtmlNode> collapsed, HtmlNode node) {
        return MammothOptionals.map(
            tryGetLast(collapsed).flatMap(last -> tryCast(HtmlElement.class, last)),
            tryCast(HtmlElement.class, node),
            (last, next) -> {
                if (next.isCollapsible() && isMatch(last, next)) {
                    for (HtmlNode child : next.getChildren()) {
                        collapsingAdd(last.getChildren(), child);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        ).orElse(false);
    }

    private static boolean isMatch(HtmlElement first, HtmlElement second) {
        return second.getTagNames().contains(first.getTagName()) &&
            first.getAttributes().equals(second.getAttributes());
    }
}
