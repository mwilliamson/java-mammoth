package org.zwobble.mammoth;

import org.zwobble.mammoth.images.ImageConverter;
import org.zwobble.mammoth.internal.InternalDocumentConverter;
import org.zwobble.mammoth.internal.conversion.DocumentToHtmlOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DocumentConverter {
    private final DocumentToHtmlOptions options;

    public DocumentConverter() {
        this(DocumentToHtmlOptions.DEFAULT);
    }

    private DocumentConverter(DocumentToHtmlOptions options) {
        this.options = options;
    }

    /**
     * A string to prepend to any generated IDs,
     * such as those used by bookmarks, footnotes and endnotes.
     * Defaults to the empty string.
     */
    public DocumentConverter idPrefix(String idPrefix) {
        return new DocumentConverter(options.idPrefix(idPrefix));
    }

    /**
     * By default, empty paragraphs are ignored.
     * Call this to preserve empty paragraphs in the output.
     */
    public DocumentConverter preserveEmptyParagraphs() {
        return new DocumentConverter(options.preserveEmptyParagraphs());
    }

    /**
     * Add a style map to specify the mapping of Word styles to HTML.
     * The most recently added style map has the greatest precedence.
     */
    public DocumentConverter addStyleMap(String styleMap) {
        return new DocumentConverter(options.addStyleMap(styleMap));
    }

    /**
     * By default, any added style maps are combined with the default style map.
     * Call this to stop using the default style map altogether.
     */
    public DocumentConverter disableDefaultStyleMap() {
        return new DocumentConverter(options.disableDefaultStyleMap());
    }

    /**
     * By default, if the document contains an embedded style map, then it is combined with the default style map.
     * Call this to ignore any embedded style maps.
     */
    public DocumentConverter disableEmbeddedStyleMap() {
        return new DocumentConverter(options.disableEmbeddedStyleMap());
    }

    /**
     * By default, images are converted to {@code <img>} elements with the source included inline in the {@code src} attribute.
     * Call this to change how images are converted.
     */
    public DocumentConverter imageConverter(ImageConverter.ImgElement imageConverter) {
        return new DocumentConverter(options.imageConverter(imageConverter));
    }

    /**
     * Converts {@code stream} into an HTML string.
     * Note that using this method instead of {@link #convertToHtml(File file)}
     * means that relative paths to other files, such as images, cannot be resolved.
     */
    public Result<String> convertToHtml(InputStream stream) throws IOException {
        return new InternalDocumentConverter(options).convertToHtml(stream).toResult();
    }

    /**
     * Converts {@code file} into an HTML string.
     */
    public Result<String> convertToHtml(File file) throws IOException {
        return new InternalDocumentConverter(options).convertToHtml(file).toResult();
    }

    /**
     * Extract the raw text of the document.
     * This will ignore all formatting in the document.
     * Each paragraph is followed by two newlines.
     */
    public Result<String> extractRawText(InputStream stream) throws IOException {
        return new InternalDocumentConverter(options).extractRawText(stream).toResult();
    }

    /**
     * Extract the raw text of the document.
     * This will ignore all formatting in the document.
     * Each paragraph is followed by two newlines.
     */
    public Result<String> extractRawText(File file) throws IOException {
        return new InternalDocumentConverter(options).extractRawText(file).toResult();
    }
}
