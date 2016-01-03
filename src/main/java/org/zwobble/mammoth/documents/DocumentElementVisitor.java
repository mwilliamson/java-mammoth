package org.zwobble.mammoth.documents;

public interface DocumentElementVisitor<T> {
    T visit(ParagraphElement paragraph);
    T visit(RunElement run);
    T visit(TextElement text);
}
