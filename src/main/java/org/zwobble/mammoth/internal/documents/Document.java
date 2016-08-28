package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class Document implements HasChildren {
    private final List<DocumentElement> children;
    private final Notes notes;
    private final List<Comment> comments;

    public Document(List<DocumentElement> children, Notes notes, List<Comment> comments) {
        this.children = children;
        this.notes = notes;
        this.comments = comments;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    public Notes getNotes() {
        return notes;
    }
}
