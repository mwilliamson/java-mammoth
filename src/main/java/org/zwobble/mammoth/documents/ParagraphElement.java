package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class ParagraphElement implements DocumentElement, HasChildren {
    private final Optional<Style> style;
    private final List<DocumentElement> children;

    public ParagraphElement(Optional<Style> style, List<DocumentElement> children) {
        this.style = style;
        this.children = children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }
}
