package org.zwobble.mammoth.internal.html;

public interface HtmlNode {
    void accept(Visitor visitor);
    <T> T accept(Mapper<T> visitor);

    interface Visitor {
        void visit(HtmlElement element);
        void visit(HtmlTextNode node);
        void visit(HtmlForceWrite forceWrite);
        void visit(HtmlBreakMergingFakeElement fakeElement);
    }

    interface Mapper<T> {
        T visit(HtmlElement element);
        T visit(HtmlTextNode node);
        T visit(HtmlForceWrite forceWrite);
        T visit(HtmlBreakMergingFakeElement fakeElement);
    }
}
