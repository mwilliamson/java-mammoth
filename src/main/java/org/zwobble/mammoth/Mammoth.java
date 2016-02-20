package org.zwobble.mammoth;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.HasChildren;
import org.zwobble.mammoth.documents.Paragraph;
import org.zwobble.mammoth.documents.Text;
import org.zwobble.mammoth.docx.DocxFile;
import org.zwobble.mammoth.docx.ZippedDocxFile;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.styles.StyleMap;
import org.zwobble.mammoth.util.Casts;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

    public static Result<String> convertToHtml(File file) {
        return convertToHtml(file, Options.DEFAULT);
    }

    public static Result<String> convertToHtml(File file, Options options) {
        return withDocxFile(file, zipFile ->
            readDocument(zipFile)
                .flatMap(nodes -> DocumentConverter.convertToHtml(options.idPrefix, options.preserveEmptyParagraphs, StyleMap.EMPTY, nodes))
                .map(Html::stripEmpty)
                .map(Html::collapse)
                .map(Html::write));
    }

    public static Result<String> extractRawText(File file) {
        return withDocxFile(file, zipFile ->
            readDocument(zipFile).map(Mammoth::extractRawTextOfChildren));
    }

    private static <T> T withDocxFile(File file, Function<DocxFile, T> function) {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            return function.apply(zipFile);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");
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
