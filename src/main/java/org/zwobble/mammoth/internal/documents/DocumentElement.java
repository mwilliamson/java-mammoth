package org.zwobble.mammoth.internal.documents;

public interface DocumentElement {
    <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context);
}
