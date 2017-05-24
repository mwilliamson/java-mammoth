package org.zwobble.mammoth.internal.documents;

public class Tab implements DocumentElement {
    public static final Tab TAB = new Tab();

    private Tab() {
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
