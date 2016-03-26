package org.zwobble.mammoth;

import org.zwobble.mammoth.internal.DocumentToHtml;
import org.zwobble.mammoth.internal.DocumentToHtmlOptions;
import org.zwobble.mammoth.internal.documents.DocumentElement;
import org.zwobble.mammoth.internal.documents.HasChildren;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Text;
import org.zwobble.mammoth.internal.docx.DocxFile;
import org.zwobble.mammoth.internal.docx.InMemoryDocxFile;
import org.zwobble.mammoth.internal.docx.ZippedDocxFile;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.Casts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.internal.docx.DocumentReader.readDocument;
import static org.zwobble.mammoth.internal.util.Iterables.lazyMap;
import static org.zwobble.mammoth.internal.util.Lists.list;

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
     * Converts {@code stream} into an HTML string.
     * Note that using this method instead of {@link #convertToHtml(File file)}
     * means that relative paths to other files, such as images, cannot be resolved.
     */
    public Result<String> convertToHtml(InputStream stream) throws IOException {
        return withDocxFile(stream, zipFile ->
            convertToHtml(Optional.empty(), zipFile)).toResult();
    }

    /**
     * Converts {@code file} into an HTML string.
     */
    public Result<String> convertToHtml(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            convertToHtml(Optional.of(file.toPath()), zipFile)).toResult();
    }

    private InternalResult<String> convertToHtml(Optional<Path> path, DocxFile zipFile) {
        return readDocument(path, zipFile)
            .flatMap(nodes -> DocumentToHtml.convertToHtml(nodes, options))
            .map(Html::stripEmpty)
            .map(Html::collapse)
            .map(Html::write);
    }

    /**
     * Extract the raw text of the document.
     * This will ignore all formatting in the document.
     * Each paragraph is followed by two newlines.
     */
    public Result<String> extractRawText(InputStream stream) throws IOException {
        return withDocxFile(stream, zipFile ->
            extractRawText(Optional.empty(), zipFile)).toResult();
    }

    /**
     * Extract the raw text of the document.
     * This will ignore all formatting in the document.
     * Each paragraph is followed by two newlines.
     */
    public Result<String> extractRawText(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            extractRawText(Optional.of(file.toPath()), zipFile)).toResult();
    }

    private InternalResult<String> extractRawText(Optional<Path> path, DocxFile zipFile) {
        return readDocument(path, zipFile)
            .map(DocumentConverter::extractRawTextOfChildren);
    }

    private static <T> T withDocxFile(File file, Function<DocxFile, T> function) throws IOException {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            return function.apply(zipFile);
        }
    }

    private static <T> T withDocxFile(InputStream stream, Function<DocxFile, T> function) throws IOException {
        try (DocxFile zipFile = InMemoryDocxFile.fromStream(stream)) {
            return function.apply(zipFile);
        }
    }

    private static String extractRawTextOfChildren(HasChildren parent) {
        return extractRawText(parent.getChildren());
    }

    private static String extractRawText(List<DocumentElement> nodes) {
        return String.join("", lazyMap(nodes, node -> extractRawText(node)));
    }

    private static String extractRawText(DocumentElement node) {
        return Casts.tryCast(Text.class, node)
            .map(Text::getValue)
            .orElseGet(() -> {
                List<DocumentElement> children = Casts.tryCast(HasChildren.class, node)
                    .map(HasChildren::getChildren)
                    .orElse(list());
                String suffix = node instanceof Paragraph ? "\n\n" : "";
                return extractRawText(children) + suffix;
            });
    }
}
