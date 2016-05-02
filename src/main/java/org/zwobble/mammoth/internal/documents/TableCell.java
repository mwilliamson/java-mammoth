package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class TableCell implements DocumentElement, HasChildren {
    private final int colspan;
    private final List<DocumentElement> children;

    public TableCell(int colspan, List<DocumentElement> children) {
        this.children = children;
        this.colspan = colspan;
    }

    public int getColspan() {
        return colspan;
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
