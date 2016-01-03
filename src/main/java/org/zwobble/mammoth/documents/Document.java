package org.zwobble.mammoth.documents;

import java.util.List;

public class Document {
    private final List<DocumentElement> children;

    public Document(List<DocumentElement> children) {
        this.children = children;
    }
}
