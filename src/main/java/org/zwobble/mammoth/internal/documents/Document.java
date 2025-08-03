package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class Document implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;
    private final Notes notes;
    private final List<Comment> comments;

    public Document(List<DocumentElement> children, Notes notes, List<Comment> comments) {
        this.children = children;
        this.notes = notes;
        this.comments = comments;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new Document(newChildren, this.notes, this.comments);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }

    public Notes getNotes() {
        return notes;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
