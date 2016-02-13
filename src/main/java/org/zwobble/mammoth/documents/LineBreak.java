package org.zwobble.mammoth.documents;

public class LineBreak implements DocumentElement {
    public static final LineBreak LINE_BREAK = new LineBreak();

    private LineBreak() {
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
