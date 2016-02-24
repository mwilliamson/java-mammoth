package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.zwobble.mammoth.internal.results.InternalResult.success;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.ResultMatchers.isResult;

public class MammothTests {
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() throws IOException {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            deepEquals(success("<p>Walking on imported air</p>")));
    }

    @Test
    public void canReadFilesWithUtf8Bom() throws IOException {
        assertThat(
            convertToHtml("utf8-bom.docx"),
            deepEquals(success("<p>This XML has a byte order mark.</p>")));
    }

    @Test
    public void emptyParagraphsAreIgnoredByDefault() throws IOException {
        assertThat(
            convertToHtml("empty.docx"),
            deepEquals(success("")));
    }

    @Test
    public void emptyParagraphsArePreservedIfIgnoreEmptyParagraphsIsFalse() throws IOException {
        assertThat(
            convertToHtml("empty.docx", mammoth -> mammoth.preserveEmptyParagraphs()),
            deepEquals(success("<p></p>")));
    }

    @Test
    public void simpleListIsConvertedToListElements() throws IOException {
        assertThat(
            convertToHtml("simple-list.docx"),
            deepEquals(success("<ul><li>Apple</li><li>Banana</li></ul>")));
    }

    @Test
    public void wordTablesAreConvertedToHtmlTables() throws IOException {
        assertThat(
            convertToHtml("tables.docx"),
            deepEquals(success(
                "<p>Above</p>" +
                "<table>" +
                "<tr><td><p>Top left</p></td><td><p>Top right</p></td></tr>" +
                "<tr><td><p>Bottom left</p></td><td><p>Bottom right</p></td></tr>" +
                "</table>" +
                "<p>Below</p>")));
    }

    @Test
    public void inlineImagesAreIncludedInOutput() throws IOException {
        assertThat(
            convertToHtml("tiny-picture.docx"),
            deepEquals(success("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>")));
    }

    @Test
    public void imagesStoredOutsideOfDocumentAreIncludedInOutput() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            Files.copy(TestData.file("tiny-picture.png").toPath(), tempDirectory.resolve("tiny-picture.png"));
            assertThat(
                new DocumentConverter().convertToHtml(documentPath.toFile()),
                deepEquals(success("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>")));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void warnIfDocumentHasImagesStoredOutsideOfDocumentWhenPathOfDocumentIsUnknown() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            assertThat(
                new DocumentConverter().convertToHtml(documentPath.toUri().toURL().openStream()),
                allOf(
                    hasProperty("value", equalTo("")),
                    hasProperty("warnings", contains(
                        startsWith("could not open external image 'tiny-picture.png': path of document is unknown, but is required for relative URI")))));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void warnIfImagesStoredOutsideOfDocumentAreNotFound() throws IOException {
        Path tempDirectory = Files.createTempDirectory("mammoth-");
        try {
            Path documentPath = tempDirectory.resolve("external-picture.docx");
            Files.copy(TestData.file("external-picture.docx").toPath(), documentPath);
            assertThat(
                new DocumentConverter().convertToHtml(documentPath.toFile()),
                allOf(
                    hasProperty("value", equalTo("")),
                    hasProperty("warnings", contains(
                        startsWith("could not open external image 'tiny-picture.png'")))));
        } finally {
            tempDirectory.toFile().delete();
        }
    }

    @Test
    public void contentTypesAreRead() throws IOException {
        assertThat(
            convertToHtml("tiny-picture-custom-content-type.docx"),
            deepEquals(success("<p><img src=\"data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>")));
    }

    @Test
    public void footnotesAreAppendedToText() throws IOException {
        assertThat(
            convertToHtml("footnotes.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            deepEquals(success(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-footnote-2\" id=\"doc-42-footnote-ref-2\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> A tachyon walks into a bar. <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li>" +
                "<li id=\"doc-42-footnote-2\"><p> Fin. <a href=\"#doc-42-footnote-ref-2\">↑</a></p></li></ol>")));
    }

    @Test
    public void endNotesAreAppendedToText() throws IOException {
        assertThat(
            convertToHtml("endnotes.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            deepEquals(success(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-endnote-2\" id=\"doc-42-endnote-ref-2\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-endnote-3\" id=\"doc-42-endnote-ref-3\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-endnote-2\"><p> A tachyon walks into a bar. <a href=\"#doc-42-endnote-ref-2\">↑</a></p></li>" +
                "<li id=\"doc-42-endnote-3\"><p> Fin. <a href=\"#doc-42-endnote-ref-3\">↑</a></p></li></ol>")));
    }

    @Test
    public void relationshipsAreReadForEachFileContainingBodyXml() throws IOException {
        assertThat(
            convertToHtml("footnote-hyperlink.docx", mammoth -> mammoth.idPrefix("doc-42-")),
            deepEquals(success(
                "<p><sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> <a href=\"http://www.example.com\">Example</a> <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li></ol>")));
    }

    @Test
    public void textBoxesAreRead() throws IOException {
        assertThat(
            convertToHtml("text-box.docx"),
            deepEquals(success("<p>Datum plane</p>")));
    }

    @Test
    public void canUseCustomStyleMap() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em")),
            deepEquals(success("<p><strong>The <em>Sunset</em> Tree</strong></p>")));
    }

    @Test
    public void mostRecentlyAddedStyleMapTakesPrecedence() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em").addStyleMap("u => strong")),
            deepEquals(success("<p><strong>The <strong>Sunset</strong> Tree</strong></p>")));
    }

    @Test
    public void rulesFromPreviouslyAddedStyleMapsStillTakeEffectIfNotOverriden() throws IOException {
        assertThat(
            convertToHtml("underline.docx", mammoth -> mammoth.addStyleMap("u => em").addStyleMap("s => del")),
            deepEquals(success("<p><strong>The <em>Sunset</em> Tree</strong></p>")));
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
    public void canExtractRawText() throws IOException {
        assertThat(
            new DocumentConverter().extractRawText(TestData.file("simple-list.docx")),
            deepEquals(success("Apple\n\nBanana\n\n")));
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
