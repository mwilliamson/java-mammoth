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
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public boolean isHeader() {
        return isHeader;
    }
}
