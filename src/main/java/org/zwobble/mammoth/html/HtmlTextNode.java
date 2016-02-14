package org.zwobble.mammoth.html;

public class HtmlTextNode implements HtmlNode {
    private final String value;

    public HtmlTextNode(String  value) {
        this.value = value;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append(HtmlWriter.escapeText(value));
    }
}
