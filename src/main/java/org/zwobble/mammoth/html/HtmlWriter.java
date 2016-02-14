package org.zwobble.mammoth.html;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.Map;

public class HtmlWriter {
    public static String escapeText(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttributeValue(String value) {
        return escapeText(value).replace("\"", "&quot;");
    }

    public static void generateAttributes(Map<String, String> attributes, StringBuilder builder) {
        List<Map.Entry<String, String>> sortedAttributes = orderBy(Map.Entry<String, String>::getKey)
            .sortedCopy(attributes.entrySet());

        for (Map.Entry<String, String> attribute : sortedAttributes) {
            builder
                .append(" ")
                .append(attribute.getKey())
                .append("=\"")
                .append(HtmlWriter.escapeAttributeValue(attribute.getValue()))
                .append("\"");
        }
    }

    private static <T, R extends Comparable<R>> Ordering<T> orderBy(Function<T, R> getKey) {
        return Ordering.from((first, second) -> getKey.apply(first).compareTo(getKey.apply(second)));
    }
}
