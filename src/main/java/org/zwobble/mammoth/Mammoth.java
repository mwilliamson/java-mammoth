package org.zwobble.mammoth;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.zwobble.mammoth.internal.DocumentConverter;
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
import org.zwobble.mammoth.results.Result;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.internal.docx.DocumentReader.readDocument;
import static org.zwobble.mammoth.internal.util.MammothLists.list;

public class Mammoth {
    private final DocumentToHtmlOptions options;

    public Mammoth() {
        this(DocumentToHtmlOptions.DEFAULT.addStyleMap(DefaultStyles.DEFAULT_STYLE_MAP));
    }

    private Mammoth(DocumentToHtmlOptions options) {
        this.options = options;
    }

    public Mammoth idPrefix(String idPrefix) {
        return new Mammoth(options.idPrefix(idPrefix));
    }

    public Mammoth preserveEmptyParagraphs() {
        return new Mammoth(options.preserveEmptyParagraphs());
    }

    public Mammoth addStyleMap(String styleMap) {
        return new Mammoth(options.addStyleMap(styleMap));
    }

    public Result<String> convertToHtml(InputStream stream) throws IOException {
        return withDocxFile(stream, zipFile ->
            convertToHtml(Optional.empty(), zipFile));
    }

    public Result<String> convertToHtml(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            convertToHtml(Optional.of(file.toPath()), zipFile));
    }

    private Result<String> convertToHtml(Optional<Path> path, DocxFile zipFile) {
        return readDocument(path, zipFile)
            .flatMap(nodes -> DocumentConverter.convertToHtml(nodes, options))
            .map(Html::stripEmpty)
            .map(Html::collapse)
            .map(Html::write);
    }

    public static Result<String> extractRawText(File file) throws IOException {
        return withDocxFile(file, zipFile ->
            readDocument(Optional.of(file.toPath()), zipFile)
                .map(Mammoth::extractRawTextOfChildren));
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
