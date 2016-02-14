package org.zwobble.mammoth.html;

public class HtmlForceWrite implements HtmlNode {
    public static final HtmlForceWrite FORCE_WRITE = new HtmlForceWrite();

    private HtmlForceWrite() {
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Mapper<T> visitor) {
        return visitor.visit(this);
    }
}
