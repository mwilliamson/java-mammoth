package org.zwobble.mammoth.documents;

import java.util.List;
import java.util.Optional;

public class Hyperlink implements DocumentElement, HasChildren {
    public static Hyperlink href(String href, List<DocumentElement> children) {
        return new Hyperlink(Optional.of(href), children);
    }

    private final Optional<String> href;
    private final List<DocumentElement> children;

    public Hyperlink(Optional<String> href, List<DocumentElement> children) {
        this.href = href;
        this.children = children;
    }

    public Optional<String> getHref() {
        return href;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
