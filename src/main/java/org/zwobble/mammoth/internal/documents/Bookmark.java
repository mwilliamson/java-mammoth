package org.zwobble.mammoth.internal.documents;

public class Bookmark implements DocumentElement {
    private final String name;

    public Bookmark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
