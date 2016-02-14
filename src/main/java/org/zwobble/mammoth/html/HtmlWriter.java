package org.zwobble.mammoth.html;

public class HtmlWriter {
    public static String escapeText(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String escapeAttributeValue(String value) {
        return escapeText(value).replace("\"", "&quot;");
    }
}
