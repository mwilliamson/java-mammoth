package org.zwobble.mammoth.documents;

import java.util.List;

public class Run implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;

    public Run(List<DocumentElement> children) {
        this.children = children;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
