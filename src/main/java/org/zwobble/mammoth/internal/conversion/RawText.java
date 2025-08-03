package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.internal.documents.*;

import java.util.List;

import static org.zwobble.mammoth.internal.util.Casts.tryCast;
import static org.zwobble.mammoth.internal.util.Iterables.lazyMap;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class RawText {
    public static String extractRawText(Document document) {
        return extractRawTextOfChildren(document);
    }

    private static <T> String extractRawTextOfChildren(HasChildren<T> parent) {
        return extractRawText(parent.getChildren());
    }

    private static String extractRawText(List<DocumentElement> nodes) {
        return String.join("", lazyMap(nodes, node -> extractRawText(node)));
    }

    public static String extractRawText(DocumentElement node) {
        if (node instanceof Text) {
            return ((Text) node).getValue();
        } else if (node instanceof Tab) {
            return "\t";
        } else {
            List<DocumentElement> children = tryCast(HasGetChildren.class, node)
                .map(HasGetChildren::getChildren)
                .orElse(list());
            String suffix = tryCast(Paragraph.class, node).map(paragraph -> "\n\n").orElse("");
            return extractRawText(children) + suffix;
        }
    }
}
