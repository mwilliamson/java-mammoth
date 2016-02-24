package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Paragraph implements DocumentElement, HasChildren {
    private final Optional<Style> style;
    private final Optional<NumberingLevel> numbering;
    private final List<DocumentElement> children;

    public Paragraph(Optional<Style> style, Optional<NumberingLevel> numbering, List<DocumentElement> children) {
        this.style = style;
        this.numbering = numbering;
        this.children = children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    public Optional<NumberingLevel> getNumbering() {
        return numbering;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
