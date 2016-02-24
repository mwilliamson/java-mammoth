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
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
