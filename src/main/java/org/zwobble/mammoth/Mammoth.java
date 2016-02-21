package org.zwobble.mammoth;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.HasChildren;
import org.zwobble.mammoth.documents.Paragraph;
import org.zwobble.mammoth.documents.Text;
import org.zwobble.mammoth.docx.DocxFile;
import org.zwobble.mammoth.docx.InMemoryDocxFile;
import org.zwobble.mammoth.docx.ZippedDocxFile;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.styles.*;
import org.zwobble.mammoth.util.Casts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.docx.DocumentReader.readDocument;
import static org.zwobble.mammoth.util.MammothLists.list;

public class Mammoth {
    public static class Options {
        public static final Options DEFAULT = new Options("", false);

        private final String idPrefix;
        private final boolean preserveEmptyParagraphs;

        public Options(String idPrefix, boolean preserveEmptyParagraphs) {
            this.idPrefix = idPrefix;
            this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        }

        public Options idPrefix(String prefix) {
            return new Options(prefix, preserveEmptyParagraphs);
        }

        public Options preserveEmptyParagraphs() {
            return new Options(idPrefix, true);
        }
    }

    private static final StyleMap DEFAULT_STYLE_MAP = StyleMap.builder()
        .mapParagraph(ParagraphMatcher.styleName("footnote text"), HtmlPath.element("p"))
        .mapRun(RunMatcher.styleName("footnote reference"), HtmlPath.EMPTY)
        .mapParagraph(ParagraphMatcher.styleName("endnote text"), HtmlPath.element("p"))
        .mapRun(RunMatcher.styleName("endnote reference"), HtmlPath.EMPTY)

        .mapParagraph(ParagraphMatcher.styleName("Footnote"), HtmlPath.element("p"))
        .mapRun(RunMatcher.styleName("Footnote anchor"), HtmlPath.EMPTY)
        .mapParagraph(ParagraphMatcher.styleName("Endnote"), HtmlPath.element("p"))
        .mapRun(RunMatcher.styleName("Endnote anchor"), HtmlPath.EMPTY)

        .mapParagraph(
            ParagraphMatcher.unorderedList("0"),
            new HtmlPath(list(
                HtmlPathElement.collapsible("ul"),
                HtmlPathElement.fresh("li"))))

        .mapRun(RunMatcher.styleName("Hyperlink"), HtmlPath.EMPTY)
        .mapParagraph(ParagraphMatcher.styleName("Normal"), HtmlPath.element("p"))
        .build();

    public static Result<String> convertToHtml(InputStream stream) throws IOException {
        return convertToHtml(stream, Options.DEFAULT);
    }

    public static Result<String> convertToHtml(InputStream stream, Options options) throws IOException {
        return withDocxFile(stream, zipFile ->
            convertToHtml(Optional.empty(), zipFile, options));
    }

    public static Result<String> convertToHtml(File file) throws IOException {
        return convertToHtml(file, Options.DEFAULT);
    }

    public static Result<String> convertToHtml(File file, Options options) throws IOException {
        return withDocxFile(file, zipFile ->
            convertToHtml(Optional.of(file.toPath()), zipFile, options));
    }

    private static Result<String> convertToHtml(Optional<Path> path, DocxFile zipFile, Options options) {
        return readDocument(path, zipFile)
            .flatMap(nodes -> DocumentConverter.convertToHtml(options.idPrefix, options.preserveEmptyParagraphs, DEFAULT_STYLE_MAP, nodes))
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
