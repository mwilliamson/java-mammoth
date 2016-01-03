package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class Paragraph implements DocumentElement, HasChildren {
    private final Optional<Style> style;
    private final List<DocumentElement> children;

    public Paragraph(Optional<Style> style, List<DocumentElement> children) {
        this.style = style;
        this.children = children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
