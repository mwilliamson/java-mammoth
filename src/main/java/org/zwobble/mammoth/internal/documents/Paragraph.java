package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Paragraph implements DocumentElement, HasChildren {
    private final Optional<Style> style;
    private final Optional<Alignment> alignment;
    private final Optional<NumberingLevel> numbering;
    private final ParagraphIndent indent;
    private final List<DocumentElement> children;

    public Paragraph(
        Optional<Style> style,
        Optional<Alignment> alignment,
        Optional<NumberingLevel> numbering,
        ParagraphIndent indent,
        List<DocumentElement> children
    ) {
        this.style = style;
        this.alignment = alignment;
        this.numbering = numbering;
        this.indent = indent;
        this.children = children;
    }

    public Optional<Style> getStyle() {
        return style;
    }

    public Optional<Alignment> getAlignment() {
        return alignment;
    }

    public Optional<NumberingLevel> getNumbering() {
        return numbering;
    }

    public ParagraphIndent getIndent() {
        return indent;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new Paragraph(this.style, this.numbering, this.indent, newChildren);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
