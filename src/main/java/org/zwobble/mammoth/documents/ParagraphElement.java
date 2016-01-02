package org.zwobble.mammoth.documents;

import java.util.List;

public class ParagraphElement implements DocumentElement {
    private final List<DocumentElement> children;

    public ParagraphElement(List<DocumentElement> children) {
        this.children = children;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }
}
