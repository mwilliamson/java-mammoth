package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class Document implements HasChildren {
    private final List<DocumentElement> children;
    private final Notes notes;

    public Document(List<DocumentElement> children, Notes notes) {
        this.children = children;
        this.notes = notes;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    public Notes getNotes() {
        return notes;
    }
}
