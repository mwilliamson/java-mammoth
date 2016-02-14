package org.zwobble.mammoth.html;

public class HtmlSelfClosingElement implements HtmlNode {
    private final String tagName;

    public HtmlSelfClosingElement(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("<")
            .append(tagName)
            .append(" />");
    }
}
