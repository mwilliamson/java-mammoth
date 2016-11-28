package org.zwobble.mammoth.internal.html;

import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.Optionals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zwobble.mammoth.internal.util.Casts.tryCast;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Lists.tryGetLast;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.util.Sets.set;

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
        return new HtmlElement(new HtmlTag(list(tagName), attributes, false, ""), children);
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
        return new HtmlElement(new HtmlTag(tagNames, attributes, true, ""), children);
    }

    public static List<HtmlNode> stripEmpty(List<HtmlNode> nodes) {
        return Lists.eagerFlatMap(nodes, node -> stripEmpty(node));
    }

    private static List<HtmlNode> stripEmpty(HtmlNode node) {
        return node.accept(new HtmlNode.Mapper<List<HtmlNode>>() {
            @Override
            public List<HtmlNode> visit(HtmlElement element) {
                List<HtmlNode> children = stripEmpty(element.getChildren());
                if (children.isEmpty() && !isVoidElement(element)) {
                    return list();
                } else {
                    return list(new HtmlElement(element.getTag(), children));
                }
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
                    element.getTag(),
                    collapse(element.getChildren())
                );
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
        return Optionals.map(
            tryGetLast(collapsed).flatMap(last -> tryCast(HtmlElement.class, last)),
            tryCast(HtmlElement.class, node),
            (last, next) -> {
                if (next.isCollapsible() && isMatch(last, next)) {
                    String separator = next.getSeparator();
                    if (!separator.isEmpty()) {
                        last.getChildren().add(Html.text(separator));
                    }
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

    static boolean isVoidElement(HtmlElement element) {
        return element.getChildren().isEmpty() && isVoidTag(element.getTagName());
    }

    private static final Set<String> VOID_TAG_NAMES = set("img", "br");

    private static boolean isVoidTag(String tagName) {
        return VOID_TAG_NAMES.contains(tagName);
    }
}
