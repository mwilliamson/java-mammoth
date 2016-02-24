package org.zwobble.mammoth.internal;

import org.zwobble.mammoth.internal.styles.StyleMap;

public class DocumentToHtmlOptions {
    public static final DocumentToHtmlOptions DEFAULT = new DocumentToHtmlOptions("", false, StyleMap.EMPTY);

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;

    public DocumentToHtmlOptions(String idPrefix, boolean preserveEmptyParagraphs, StyleMap styleMap) {
        this.idPrefix = idPrefix;
        this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        this.styleMap = styleMap;
    }

    public DocumentToHtmlOptions idPrefix(String prefix) {
        return new DocumentToHtmlOptions(prefix, preserveEmptyParagraphs, styleMap);
    }

    public DocumentToHtmlOptions preserveEmptyParagraphs() {
        return new DocumentToHtmlOptions(idPrefix, true, styleMap);
    }

    public DocumentToHtmlOptions styleMap(StyleMap styleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap);
    }

    public String idPrefix() {
        return idPrefix;
    }

    public boolean shouldPreserveEmptyParagraphs() {
        return preserveEmptyParagraphs;
    }

    public StyleMap styleMap() {
        return styleMap;
    }
}
