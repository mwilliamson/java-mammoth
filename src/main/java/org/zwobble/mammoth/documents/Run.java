package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class Run implements DocumentElement, HasChildren {
    private final boolean isBold;
    private final Optional<Style> style;
    private final List<DocumentElement> children;

    public Run(boolean isBold, Optional<Style> style, List<DocumentElement> children) {
        this.isBold = isBold;
        this.children = children;
        this.style = style;
    }

    public boolean isBold() {
        return isBold;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
