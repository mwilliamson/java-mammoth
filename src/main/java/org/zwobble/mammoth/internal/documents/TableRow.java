package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class TableRow implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;
    private final boolean isHeader;

    public TableRow(List<DocumentElement> children, boolean isHeader) {
        this.children = children;
        this.isHeader = isHeader;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new TableRow(newChildren, this.isHeader);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }

    public boolean isHeader() {
        return isHeader;
    }
}
