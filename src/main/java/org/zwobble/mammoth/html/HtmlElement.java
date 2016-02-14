package org.zwobble.mammoth.html;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.Map;

public class HtmlElement implements HtmlNode {
    private final String tagName;
    private final Map<String, String> attributes;
    private final List<HtmlNode> children;

    public HtmlElement(String tagName, Map<String, String> attributes, List<HtmlNode> children) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("<").append(tagName);

        List<Map.Entry<String, String>> sortedAttributes = orderBy(Map.Entry<String, String>::getKey)
            .sortedCopy(attributes.entrySet());

        for (Map.Entry<String, String> attribute : sortedAttributes) {
            builder
                .append(" ")
                .append(attribute.getKey())
                .append("=\"")
                .append(attribute.getValue())
                .append("\"");
        }

        builder.append(">");

        children.forEach(node -> node.write(builder));

        builder
            .append("</")
            .append(tagName)
            .append(">");
    }

    private static <T, R extends Comparable<R>> Ordering<T> orderBy(Function<T, R> getKey) {
        return Ordering.from((first, second) -> getKey.apply(first).compareTo(getKey.apply(second)));
    }
}
