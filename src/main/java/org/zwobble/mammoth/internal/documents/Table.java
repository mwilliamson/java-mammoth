package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Table implements DocumentElement, HasChildren {
    private final Optional<Style> style;
    private final List<DocumentElement> children;

    public Table(Optional<Style> style, List<DocumentElement> children) {
        this.style = style;
        this.children = children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new Table(this.style, newChildren);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
