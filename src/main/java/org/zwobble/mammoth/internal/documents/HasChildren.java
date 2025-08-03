package org.zwobble.mammoth.internal.documents;

import java.util.List;

public interface HasChildren<T> extends HasGetChildren {
    T replaceChildren(List<DocumentElement> newChildren);
}
