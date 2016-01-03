package org.zwobble.mammoth.documents;

public interface DocumentElement {
    <T> T accept(DocumentElementVisitor<T> visitor);
}
