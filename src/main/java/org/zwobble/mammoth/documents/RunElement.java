package org.zwobble.mammoth.documents;

import java.util.List;

public class RunElement implements DocumentElement, HasChildren {
    private final List<DocumentElement> children;

    public RunElement(List<DocumentElement> children) {
        this.children = children;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }
}
