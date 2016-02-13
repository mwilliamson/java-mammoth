package org.zwobble.mammoth.documents;

import java.util.List;

public class TableCell implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;

    public TableCell(List<DocumentElement> children) {
        this.children = children;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
