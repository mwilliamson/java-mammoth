package org.zwobble.mammoth.internal;

import org.zwobble.mammoth.internal.styles.DefaultStyles;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.parsing.StyleMapParser;

public class DocumentToHtmlOptions {
    public static final DocumentToHtmlOptions DEFAULT = new DocumentToHtmlOptions("", false, StyleMap.EMPTY, false);

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;
    private final boolean disableDefaultStyleMap;

    public DocumentToHtmlOptions(String idPrefix, boolean preserveEmptyParagraphs, StyleMap styleMap, boolean disableDefaultStyleMap) {
        this.idPrefix = idPrefix;
        this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        this.styleMap = styleMap;
        this.disableDefaultStyleMap = disableDefaultStyleMap;
    }

    public DocumentToHtmlOptions idPrefix(String prefix) {
        return new DocumentToHtmlOptions(prefix, preserveEmptyParagraphs, styleMap, disableDefaultStyleMap);
    }

    public DocumentToHtmlOptions preserveEmptyParagraphs() {
        return new DocumentToHtmlOptions(idPrefix, true, styleMap, disableDefaultStyleMap);
    }

    public DocumentToHtmlOptions addStyleMap(String styleMap) {
        return addStyleMap(StyleMapParser.parse(styleMap));
    }

    public DocumentToHtmlOptions addStyleMap(StyleMap styleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, this.styleMap.update(styleMap), disableDefaultStyleMap);
    }

    public DocumentToHtmlOptions disableDefaultStyleMap() {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, true);
    }

    public String idPrefix() {
        return idPrefix;
    }

    public boolean shouldPreserveEmptyParagraphs() {
        return preserveEmptyParagraphs;
    }

    public StyleMap styleMap() {
        return disableDefaultStyleMap ? styleMap : DefaultStyles.DEFAULT_STYLE_MAP.update(styleMap);
    }
}
