package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.html.HtmlElement;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.html.HtmlTag;

import java.util.List;
import java.util.function.Supplier;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class HtmlPathElement {
    private final HtmlTag tag;

    public HtmlPathElement(HtmlTag tag) {
        this.tag = tag;
    }

    public Supplier<List<HtmlNode>> wrap(Supplier<List<HtmlNode>> generateNodes) {
        return () -> wrapNodes(generateNodes.get());
    }

    private List<HtmlNode> wrapNodes(List<HtmlNode> nodes) {
        return list(new HtmlElement(tag, nodes));
    }
}
