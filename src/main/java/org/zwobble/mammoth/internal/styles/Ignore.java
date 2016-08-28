package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.html.HtmlNode;

import java.util.List;
import java.util.function.Supplier;

import static org.zwobble.mammoth.internal.util.Lists.list;

class Ignore implements HtmlPath {
    static final HtmlPath INSTANCE = new Ignore();

    private Ignore() {
    }

    @Override
    public Supplier<List<HtmlNode>> wrap(Supplier<List<HtmlNode>> generateNodes) {
        return () -> list();
    }
}
