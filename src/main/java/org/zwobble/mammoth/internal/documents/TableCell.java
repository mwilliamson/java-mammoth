package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class TableCell implements DocumentElement, HasChildren {
    private final int rowspan;
    private final int colspan;
    private final List<DocumentElement> children;

    public TableCell(int rowspan, int colspan, List<DocumentElement> children) {
        this.rowspan = rowspan;
        this.children = children;
        this.colspan = colspan;
    }

    public int getColspan() {
        return colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new TableCell(this.rowspan, this.colspan, newChildren);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
