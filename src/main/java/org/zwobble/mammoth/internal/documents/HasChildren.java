package org.zwobble.mammoth.internal.documents;

import java.util.List;

public interface HasChildren<T> {
    List<DocumentElement> getChildren();
    T replaceChildren(List<DocumentElement> newChildren);
}
