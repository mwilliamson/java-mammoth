package org.zwobble.mammoth.documents;

import java.util.List;

public class Document implements HasChildren {
    private final List<DocumentElement> children;

    public Document(List<DocumentElement> children) {
        this.children = children;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }
}
