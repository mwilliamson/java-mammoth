package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class Run implements DocumentElement, HasChildren {
    private final boolean isBold;
    private final boolean isItalic;
    private final Optional<Style> style;
    private final List<DocumentElement> children;

    public Run(
        boolean isBold,
        boolean isItalic,
        Optional<Style> style,
        List<DocumentElement> children)
    {
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.style = style;
        this.children = children;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalic() {
        return isItalic;
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
