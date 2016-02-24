package org.zwobble.mammoth.internal.documents;

public interface DocumentElement {
    <T> T accept(DocumentElementVisitor<T> visitor);
}
