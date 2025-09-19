package org.zwobble.mammoth.tests;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.images.ImageConverter;
import org.zwobble.mammoth.internal.archives.InMemoryArchive;
import org.zwobble.mammoth.internal.docx.EmbeddedStyleMap;
import org.zwobble.mammoth.internal.styles.parsing.ParseException;
import org.zwobble.mammoth.internal.util.Base64Encoding;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.tests.ResultMatchers.isResult;
import static org.zwobble.mammoth.tests.ResultMatchers.isSuccess;
import static org.zwobble.mammoth.tests.util.MammothAsserts.assertThrows;

public class MammothTests {
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() throws IOException {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            isSuccess("<p>Walking on imported air</p>"));
    }

    @Test
    public void canReadFilesWithUtf8Bom() throws IOException {
        assertThat(
            convertToHtml("utf8-bom.docx"),
            isSuccess("<p>This XML has a byte order mark.</p>"));
    }

    @Test
    public void emptyParagraphsAreIgnoredByDefault() throws IOException {
        assertThat(
            convertToHtml("empty.docx"),
            isSuccess(""));
    }

    @Test
    public void emptyParagraphsArePreservedIfIgnoreEmptyParagraphsIsFalse() throws IOException {
        assertThat(
            convertToHtml("empty.docx", mammoth -> mammoth.preserveEmptyParagraphs()),
            isSuccess("<p></p>"));
    }

    @Test
    public void simpleListIsConvertedToListElements() throws IOException {
        assertThat(
            convertToHtml("simple-list.docx"),
            isSuccess("<ul><li>Apple</li><li>Banana</li></ul>"));
    }

    @Test
    public void wordTablesAreConvertedToHtmlTables() throws IOException {
        assertThat(
            convertToHtml("tables.docx"),
            isSuccess(
                "<p>Above</p>" +
                "<table>" +
                "<tr><td><p>Top left</p></td><td><p>Top right</p></td></tr>" +
                "<tr><td><p>Bottom left</p></td><td><p>Bottom right</p></td></tr>" +
                "</table>" +
                "<p>Below</p>"));
    }

    @Test
    public void inlineImagesReferencedByPathRelativeToPartAreIncludedInOutput() throws IOException {
        assertThat(
            convertToHtml("tiny-picture.docx"),
            isSuccess("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>"));
    }

    @Test
    public void inlineImagesReferencedByPathRelativeToBaseAreIncludedInOutput() throws IOException {
        assertThat(
            convertToHtml("tiny-picture-target-base-relative.docx"),
            isSuccess("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>"));
    }

    @Test
    public void whenExternalFileAccessIsEnabledThenImagesStoredOutsideOfDocumentAreIncludedInOutput() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            Files.copy(TestData.file("tiny-picture.png").toPath(), tempDirectory.resolve("tiny-picture.png"));
            assertThat(
                new DocumentConverter()
                    .enableExternalFileAccess()
                    .convertToHtml(documentPath.toFile()),
                isSuccess("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>"));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void whenExternalFileAccessIsEnabledThenWarnIfDocumentHasImagesStoredOutsideOfDocumentWhenPathOfDocumentIsUnknown() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            assertThat(
                new DocumentConverter()
                    .enableExternalFileAccess()
                    .convertToHtml(documentPath.toUri().toURL().openStream()),
                allOf(
                    hasProperty("value", equalTo("")),
                    hasProperty("warnings", contains(
                        startsWith("could not open external image 'tiny-picture.png': path of document is unknown, but is required for relative URI")))));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void givenExternalFileAccessIsDisabledByDefaultThenWarnIfDocumentHasImagesStoredOutsideOfDocument() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            Files.copy(TestData.file("tiny-picture.png").toPath(), tempDirectory.resolve("tiny-picture.png"));
            assertThat(
                new DocumentConverter().convertToHtml(documentPath.toFile()),
                allOf(
                    hasProperty("value", equalTo("")),
                    hasProperty("warnings", contains(
                        equalTo("could not open external image 'tiny-picture.png': external file access is disabled")
                    ))
                )
            );
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void whenExternalFileAccessIsEnabledWarnIfImagesStoredOutsideOfDocumentAreNotFound() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            assertThat(
                new DocumentConverter()
                    .enableExternalFileAccess()
                    .convertToHtml(documentPath.toFile()),
                allOf(
                    hasProperty("value", equalTo("")),
                    hasProperty("warnings", contains(
                        startsWith("could not open external image 'tiny-picture.png'")))));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void imageConversionCanBeCustomised() throws IOException {
        ImageConverter.ImgElement imageConverter = image -> {
            String base64 = Base64Encoding.streamToBase64(image::getInputStream);
            String src = base64.substring(0, 2) + "," + image.getContentType();
            return map("src", src);
        };
        assertThat(
            convertToHtml("tiny-picture.docx", converter -> converter.imageConverter(imageConverter)),
            isSuccess("<p><img src=\"iV,image/png\" /></p>")
        );
    }

    @Test
    public void contentTypesAreRead() throws IOException {
        assertThat(
            convertToHtml("tiny-picture-custom-content-type.docx"),
            isSuccess("<p><img src=\"data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>"));
    }

    @Test
    public void footnotesAreAppendedToText() throws IOException {
        assertThat(
            convertToHtml("footnotes.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            isSuccess(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-footnote-2\" id=\"doc-42-footnote-ref-2\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> A tachyon walks into a bar. <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li>" +
                "<li id=\"doc-42-footnote-2\"><p> Fin. <a href=\"#doc-42-footnote-ref-2\">↑</a></p></li></ol>"));
    }

    @Test
    public void endNotesAreAppendedToText() throws IOException {
        assertThat(
            convertToHtml("endnotes.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            isSuccess(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-endnote-2\" id=\"doc-42-endnote-ref-2\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-endnote-3\" id=\"doc-42-endnote-ref-3\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-endnote-2\"><p> A tachyon walks into a bar. <a href=\"#doc-42-endnote-ref-2\">↑</a></p></li>" +
                "<li id=\"doc-42-endnote-3\"><p> Fin. <a href=\"#doc-42-endnote-ref-3\">↑</a></p></li></ol>"));
    }

    @Test
    public void whenStyleMappingIsDefinedForCommentReferencesThenCommentsAreIncluded() throws IOException {
        assertThat(
            convertToHtml("comments.docx", mammoth -> mammoth.idPrefix("doc-42-").addStyleMap("comment-reference => sup")),
            isSuccess(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-comment-0\" id=\"doc-42-comment-ref-0\">[MW1]</a></sup>." +
                "<sup><a href=\"#doc-42-comment-2\" id=\"doc-42-comment-ref-2\">[MW2]</a></sup></p>" +
                "<dl><dt id=\"doc-42-comment-0\">Comment [MW1]</dt><dd><p>A tachyon walks into a bar. <a href=\"#doc-42-comment-ref-0\">↑</a></p></dd>" +
                "<dt id=\"doc-42-comment-2\">Comment [MW2]</dt><dd><p>Fin. <a href=\"#doc-42-comment-ref-2\">↑</a></p></dd></dl>"
            ));
    }

    @Test
    public void relationshipsAreReadForEachFileContainingBodyXml() throws IOException {
        assertThat(
            convertToHtml("footnote-hyperlink.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            isSuccess(
                "<p><sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> <a href=\"http://www.example.com\">Example</a> <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li></ol>"));
    }

    @Test
    public void textBoxesAreRead() throws IOException {
        assertThat(
            convertToHtml("text-box.docx"),
            isSuccess("<p>Datum plane</p>"));
    }

    @Test
    public void canUseCustomStyleMap() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em")),
            isSuccess("<p><strong>The <em>Sunset</em> Tree</strong></p>"));
    }

    @Test
    public void mostRecentlyAddedStyleMapTakesPrecedence() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em").addStyleMap("u => strong")),
            isSuccess("<p><strong>The <strong>Sunset</strong> Tree</strong></p>"));
    }

    @Test
    public void rulesFromPreviouslyAddedStyleMapsStillTakeEffectIfNotOverriden() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em").addStyleMap("strike => del")),
            isSuccess("<p><strong>The <em>Sunset</em> Tree</strong></p>"));
    }

    @Test
    public void errorIsRaisedIfStyleMapCannotBeParsed() throws IOException {
        RuntimeException exception = assertThrows(
            ParseException.class,
            () -> new DocumentConverter().addStyleMap("p =>\np[style-name=] =>"));
        assertThat(
            exception.getMessage(),
            equalTo(
                "error reading style map at line 2, character 14: expected token of type STRING but was of type SYMBOL\n\n" +
                "p[style-name=] =>\n" +
                "             ^"));
    }

    @Test
    public void canDisableDefaultStyleMap() throws IOException {
        assertThat(
            convertToHtml("simple-list.docx", mammoth -> mammoth.disableDefaultStyleMap()),
            isResult(
                equalTo("<p>Apple</p><p>Banana</p>"),
                list("Unrecognised paragraph style: List Paragraph (Style ID: ListParagraph)")));
    }

    @Test
    public void embeddedStyleMapIsUsedIfPresent() throws IOException {
        assertThat(
            convertToHtml("embedded-style-map.docx"),
            isSuccess("<h1>Walking on imported air</h1>")
        );
    }

    @Test
    public void explicitStyleMapTakesPrecedenceOverEmbeddedStyleMap() throws IOException {
        assertThat(
            convertToHtml("embedded-style-map.docx", options -> options.addStyleMap("p => p")),
            isSuccess("<p>Walking on imported air</p>")
        );
    }

    @Test
    public void explicitStyleMapIsCombinedWithEmbeddedStyleMap() throws IOException {
        assertThat(
            convertToHtml("embedded-style-map.docx", options -> options.addStyleMap("r => strong")),
            isSuccess("<h1><strong>Walking on imported air</strong></h1>")
        );
    }

    @Test
    public void embeddedStyleMapsCanBeDisabled() throws IOException {
        assertThat(
            convertToHtml("embedded-style-map.docx", options -> options.disableEmbeddedStyleMap()),
            isSuccess("<p>Walking on imported air</p>")
        );
    }

    @Test
    public void embeddedStyleMapCanBeWrittenAndThenRead() throws IOException {
        InMemoryArchive archive = InMemoryArchive.fromStream(new FileInputStream(TestData.file("single-paragraph.docx")));
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");

        assertThat(
            new DocumentConverter().convertToHtml(new ByteArrayInputStream(archive.toByteArray())),
            isSuccess("<h1>Walking on imported air</h1>")
        );
    }

    @Test
    public void canExtractRawTextFromFile() throws IOException {
        assertThat(
            new DocumentConverter().extractRawText(TestData.file("simple-list.docx")),
            isSuccess("Apple\n\nBanana\n\n"));
    }

    @Test
    public void canExtractRawTextFromStream() throws IOException {
        assertThat(
            new DocumentConverter().extractRawText(TestData.file("simple-list.docx").toURI().toURL().openStream()),
            isSuccess("Apple\n\nBanana\n\n"));
    }

    @Test
    public void canReadStrictFormat() throws IOException {
        assertThat(
            convertToHtml("strict-format.docx"),
            isSuccess("<p>Test</p>")
        );
    }

    private Result<String> convertToHtml(String name) throws IOException {
        File file = TestData.file(name);
        return new DocumentConverter().convertToHtml(file);
    }

    private Result<String> convertToHtml(String name, Function<DocumentConverter, DocumentConverter> configure) throws IOException {
        File file = TestData.file(name);
        return configure.apply(new DocumentConverter()).convertToHtml(file);
    }
}
