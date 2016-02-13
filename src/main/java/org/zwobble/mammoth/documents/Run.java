package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class Run implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;
    private final Optional<Style> style;

    public Run(Optional<Style> style, List<DocumentElement> children) {
        this.children = children;
        this.style = style;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
