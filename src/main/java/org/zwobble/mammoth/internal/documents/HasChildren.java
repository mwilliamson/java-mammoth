package org.zwobble.mammoth.internal.documents;

import java.util.List;

public interface HasChildren {
    List<DocumentElement> getChildren();
    DocumentElement replaceChildren(List<DocumentElement> newChildren);
}
