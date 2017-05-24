package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class Table implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;

    public Table(List<DocumentElement> children) {
        this.children = children;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
