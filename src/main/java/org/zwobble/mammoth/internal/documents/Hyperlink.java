package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Hyperlink implements DocumentElement, HasChildren {
    public static Hyperlink href(String href, Optional<String> targetFrame, List<DocumentElement> children) {
        return new Hyperlink(Optional.of(href), Optional.empty(), targetFrame, children);
    }

    public static Hyperlink anchor(String anchor, Optional<String> targetFrame, List<DocumentElement> children) {
        return new Hyperlink(Optional.empty(), Optional.of(anchor), targetFrame, children);
    }

    private final Optional<String> href;
    private final Optional<String> anchor;
    private final Optional<String> targetFrame;
    private final List<DocumentElement> children;

    public Hyperlink(Optional<String> href, Optional<String> anchor, Optional<String> targetFrame, List<DocumentElement> children) {
        this.href = href;
        this.anchor = anchor;
        this.targetFrame = targetFrame;
        this.children = children;
    }

    public Optional<String> getHref() {
        return href;
    }

    public Optional<String> getAnchor() {
        return anchor;
    }

    public Optional<String> getTargetFrame() {
        return targetFrame;
    }

    @Override
    public List<DocumentElement> getChildren() {
        return children;
    }

    @Override
    public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
        return new Hyperlink(this.href, this.anchor, this.targetFrame, newChildren);
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
