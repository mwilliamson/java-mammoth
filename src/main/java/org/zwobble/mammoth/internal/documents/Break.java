package org.zwobble.mammoth.internal.documents;

public class Break implements DocumentElement {
    public enum Type {
        LINE,
        PAGE,
        COLUMN
    }

    public static final Break LINE_BREAK = new Break(Type.LINE);
    public static final Break PAGE_BREAK = new Break(Type.PAGE);
    public static final Break COLUMN_BREAK = new Break(Type.COLUMN);

    private final Type type;

    private Break(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
