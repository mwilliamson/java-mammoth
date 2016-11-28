package org.zwobble.mammoth.internal.documents;

public class Break implements DocumentElement {
    public enum Type {
        LINE,
        PAGE
    }

    public static final Break LINE_BREAK = new Break(Type.LINE);
    public static final Break PAGE_BREAK = new Break(Type.PAGE);

    private final Type type;

    private Break(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
