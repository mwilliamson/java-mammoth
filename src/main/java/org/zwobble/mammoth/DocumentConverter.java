package org.zwobble.mammoth;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
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
import org.zwobble.mammoth.internal.styles.DefaultStyles;
import org.zwobble.mammoth.internal.util.Casts;
import org.zwobble.mammoth.internal.results.InternalResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.internal.docx.DocumentReader.readDocument;
import static org.zwobble.mammoth.internal.util.MammothLists.list;

public class DocumentConverter {
    private final DocumentToHtmlOptions options;

    public DocumentConverter() {
        this(DocumentToHtmlOptions.DEFAULT.addStyleMap(DefaultStyles.DEFAULT_STYLE_MAP));
    }

    private DocumentConverter(DocumentToHtmlOptions options) {
        this.options = options;
    }

    public DocumentConverter idPrefix(String idPrefix) {
        return new DocumentConverter(options.idPrefix(idPrefix));
    }

    public DocumentConverter preserveEmptyParagraphs() {
        return new DocumentConverter(options.preserveEmptyParagraphs());
    }

    public DocumentConverter addStyleMap(String styleMap) {
        return new DocumentConverter(options.addStyleMap(styleMap));
    }

    public Result<String> convertToHtml(InputStream stream) throws IOException {
        return withDocxFile(stream, zipFile ->
            convertToHtml(Optional.empty(), zipFile));
    }

    public Result<String> convertToHtml(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            convertToHtml(Optional.of(file.toPath()), zipFile));
    }

    private InternalResult<String> convertToHtml(Optional<Path> path, DocxFile zipFile) {
        return readDocument(path, zipFile)
            .flatMap(nodes -> DocumentToHtml.convertToHtml(nodes, options))
            .map(Html::stripEmpty)
            .map(Html::collapse)
            .map(Html::write);
    }

    public Result<String> extractRawText(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            readDocument(Optional.of(file.toPath()), zipFile)
                .map(DocumentConverter::extractRawTextOfChildren));
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
        return String.join("", Iterables.transform(nodes, node -> extractRawText(node)));
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
