package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Hyperlink implements DocumentElement, HasChildren {
    public static Hyperlink href(String href, List<DocumentElement> children) {
        return new Hyperlink(Optional.of(href), Optional.empty(), children);
    }

    public static Hyperlink anchor(String anchor, List<DocumentElement> children) {
        return new Hyperlink(Optional.empty(), Optional.of(anchor), children);
    }

    private final Optional<String> href;
    private final Optional<String> anchor;
    private final List<DocumentElement> children;

    private Hyperlink(Optional<String> href, Optional<String> anchor, List<DocumentElement> children) {
        this.href = href;
        this.anchor = anchor;
        this.children = children;
    }

    public Optional<String> getHref() {
        return href;
    }

    public Optional<String> getAnchor() {
        return anchor;
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
