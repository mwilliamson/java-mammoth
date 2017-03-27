package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.images.ImageConverter;
import org.zwobble.mammoth.internal.styles.DefaultStyles;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.parsing.StyleMapParser;
import org.zwobble.mammoth.internal.util.Base64Encoding;

import static org.zwobble.mammoth.internal.util.Maps.map;

public class DocumentToHtmlOptions {
    public static final DocumentToHtmlOptions DEFAULT = new DocumentToHtmlOptions(
        "",
        false,
        StyleMap.EMPTY,
        StyleMap.EMPTY,
        false,
        false,
        image -> {
            String base64 = Base64Encoding.streamToBase64(image::getInputStream);
            String src = "data:" + image.getContentType() + ";base64," + base64;
            return map("src", src);
        }

    );

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;
    private final StyleMap embeddedStyleMap;
    private final boolean disableDefaultStyleMap;
    private final boolean disableEmbeddedStyleMap;
    private final ImageConverter.ImgElement imageConverter;

    public DocumentToHtmlOptions(
        String idPrefix,
        boolean preserveEmptyParagraphs,
        StyleMap styleMap,
        StyleMap embeddedStyleMap,
        boolean disableDefaultStyleMap,
        boolean disableEmbeddedStyleMap,
        ImageConverter.ImgElement imageConverter
    ) {
        this.idPrefix = idPrefix;
        this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        this.styleMap = styleMap;
        this.embeddedStyleMap = embeddedStyleMap;
        this.disableDefaultStyleMap = disableDefaultStyleMap;
        this.disableEmbeddedStyleMap = disableEmbeddedStyleMap;
        this.imageConverter = imageConverter;
    }

    public DocumentToHtmlOptions idPrefix(String prefix) {
        return new DocumentToHtmlOptions(prefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap, imageConverter);
    }

    public DocumentToHtmlOptions preserveEmptyParagraphs() {
        return new DocumentToHtmlOptions(idPrefix, true, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap, imageConverter);
    }

    public DocumentToHtmlOptions addStyleMap(String styleMap) {
        return addStyleMap(StyleMapParser.parse(styleMap));
    }

    public DocumentToHtmlOptions addStyleMap(StyleMap styleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, this.styleMap.update(styleMap), embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap, imageConverter);
    }

    public DocumentToHtmlOptions disableDefaultStyleMap() {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, true, disableEmbeddedStyleMap, imageConverter);
    }

    public DocumentToHtmlOptions disableEmbeddedStyleMap() {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, true, imageConverter);
    }

    public DocumentToHtmlOptions addEmbeddedStyleMap(StyleMap embeddedStyleMap) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap, imageConverter);
    }

    public DocumentToHtmlOptions imageConverter(ImageConverter.ImgElement imageConverter) {
        return new DocumentToHtmlOptions(idPrefix, preserveEmptyParagraphs, styleMap, embeddedStyleMap, disableDefaultStyleMap, disableEmbeddedStyleMap, imageConverter);
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

    public ImageConverter.ImgElement imageConverter() {
        return imageConverter;
    }
}
