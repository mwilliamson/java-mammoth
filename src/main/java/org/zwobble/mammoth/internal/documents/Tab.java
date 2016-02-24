package org.zwobble.mammoth.internal.documents;

public class Tab implements DocumentElement {
    public static final Tab TAB = new Tab();

    private Tab() {
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
