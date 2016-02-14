package org.zwobble.mammoth.html;

import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.orderedBy;

public class HtmlWriter {
    public static String escapeText(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttributeValue(String value) {
        return escapeText(value).replace("\"", "&quot;");
    }

    public static void generateAttributes(Map<String, String> attributes, StringBuilder builder) {
        for (Map.Entry<String, String> attribute : orderedBy(attributes.entrySet(), Map.Entry::getKey)) {
            builder
                .append(" ")
                .append(attribute.getKey())
                .append("=\"")
                .append(HtmlWriter.escapeAttributeValue(attribute.getValue()))
                .append("\"");
        }
    }
}
