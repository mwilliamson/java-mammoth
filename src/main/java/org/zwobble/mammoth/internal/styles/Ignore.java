package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.html.HtmlNode;

import java.util.List;
import java.util.function.Supplier;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class Ignore implements HtmlPath {
    @Override
    public Supplier<List<HtmlNode>> wrap(Supplier<List<HtmlNode>> generateNodes) {
        return () -> list();
    }
}
