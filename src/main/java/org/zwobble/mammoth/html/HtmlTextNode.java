package org.zwobble.mammoth.html;

public class HtmlTextNode implements HtmlNode {
    private final String value;

    public HtmlTextNode(String  value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }
}
