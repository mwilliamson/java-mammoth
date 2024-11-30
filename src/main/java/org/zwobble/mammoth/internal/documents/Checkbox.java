package org.zwobble.mammoth.internal.documents;

public class Checkbox implements DocumentElement {
    private final boolean checked;

    public Checkbox(boolean checked) {
        this.checked = checked;
    }

    public boolean checked() {
        return this.checked;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
