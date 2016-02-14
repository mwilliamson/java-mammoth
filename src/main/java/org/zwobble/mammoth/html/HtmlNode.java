package org.zwobble.mammoth.html;

public interface HtmlNode {
    void visit(Visitor visitor);

    interface Visitor {
        void visit(HtmlElement element);
        void visit(HtmlSelfClosingElement element);
        void visit(HtmlTextNode node);
    }
}
