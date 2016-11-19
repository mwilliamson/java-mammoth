package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.internal.styles.DefaultStyles;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.parsing.StyleMapParser;

public class DocumentToHtmlOptions {
    public static final DocumentToHtmlOptions DEFAULT = new DocumentToHtmlOptions("", false, StyleMap.EMPTY, StyleMap.EMPTY, false, false);

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;
    private final StyleMap embeddedStyleMap;
    private final boolean disableDefaultStyleMap;
    private final boolean disableEmbeddedStyleMap;

    public DocumentToHtmlOptions(
        String idPrefix,
        boolean preserveEmptyParagraphs,
        StyleMap styleMap,
        StyleMap embeddedStyleMap,
        boolean disableDefaultStyleMap,
        boolean disableEmbeddedStyleMap
    ) {
        this.idPrefix = idPrefix;
        this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        this.styleMap = styleMap;
        this.embeddedStyleMap = embeddedStyleMap;
        this.disableDefaultStyleMap = disableDefaultStyleMap;
        this.disableEmbeddedStyleMap = disableEmbeddedStyleMap;
    }

    public DocumentToHtmlOptions idPrefix(String prefix) {
        return new DocumentToHtmlOptions(prefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap);
    }

    public DocumentToHtmlOptions preserveEmptyParagraphs() {
        return new DocumentToHtmlOptions(idPrefix, true, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap);
    }

    public DocumentToHtmlOptions addStyleMap(String styleMap) {
        return addStyleMap(StyleMapParser.parse(styleMap));
    }

    public DocumentToHtmlOptions addStyleMap(StyleMap styleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, this.styleMap.update(styleMap), embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap);
    }

    public DocumentToHtmlOptions disableDefaultStyleMap() {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, true, disableEmbeddedStyleMap);
    }

    public DocumentToHtmlOptions disableEmbeddedStyleMap() {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, true);
    }

    public DocumentToHtmlOptions addEmbeddedStyleMap(StyleMap embeddedStyleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap);
    }

    public String idPrefix() {
        return idPrefix;
    }

    public boolean shouldPreserveEmptyParagraphs() {
        return preserveEmptyParagraphs;
    }

    public StyleMap styleMap() {
        StyleMap styleMap = StyleMap.EMPTY;
        if (!disableDefaultStyleMap) {
            styleMap = styleMap.update(DefaultStyles.DEFAULT_STYLE_MAP);
        }
        if (!disableEmbeddedStyleMap) {
            styleMap = styleMap.update(embeddedStyleMap);
        }
        styleMap = styleMap.update(this.styleMap);
        return styleMap;
    }
}
