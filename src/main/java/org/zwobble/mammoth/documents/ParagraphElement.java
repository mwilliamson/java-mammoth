package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class ParagraphElement implements DocumentElement, HasChildren {
    private final Optional<String> styleId;
    private final List<DocumentElement> children;

    public ParagraphElement(Optional<String> styleId, List<DocumentElement> children) {
        this.styleId = styleId;
        this.children = children;
    }

    public Optional<String> getStyleId() {
        return styleId;
    }

    public List<DocumentElement> getChildren() {
        return children;
    }
}
