package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.Mammoth;
import org.zwobble.mammoth.results.Result;

import java.io.File;

import static org.junit.Assert.assertThat;
import static org.zwobble.mammoth.results.Result.success;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class MammothTests {
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            deepEquals(success("<p>Walking on imported air</p>")));
    }

    @Test
    public void canReadFilesWithUtf8Bom() {
        assertThat(
            convertToHtml("utf8-bom.docx"),
            deepEquals(success("<p>This XML has a byte order mark.</p>")));
    }

    @Test
    public void emptyParagraphsAreIgnoredByDefault() {
        assertThat(
            convertToHtml("empty.docx"),
            deepEquals(success("")));
    }

    @Test
    public void emptyParagraphsArePreservedIfIgnoreEmptyParagraphsIsFalse() {
        assertThat(
            convertToHtml("empty.docx", Mammoth.Options.DEFAULT.preserveEmptyParagraphs()),
            deepEquals(success("<p></p>")));
    }

    @Test
    public void inlineImagesAreIncludedInOutput() {
        assertThat(
            convertToHtml("tiny-picture.docx"),
            deepEquals(success("<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>")));
    }

    @Test
    public void contentTypesAreRead() {
        assertThat(
            convertToHtml("tiny-picture-custom-content-type.docx"),
            deepEquals(success("<p><img src=\"data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>")));
    }

    @Test
    public void footnotesAreAppendedToText() {
        assertThat(
            convertToHtml("footnotes.docx", Mammoth.Options.DEFAULT.idPrefix("doc-42-")),
            deepEquals(success(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-footnote-2\" id=\"doc-42-footnote-ref-2\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> A tachyon walks into a bar. <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li>" +
                "<li id=\"doc-42-footnote-2\"><p> Fin. <a href=\"#doc-42-footnote-ref-2\">↑</a></p></li></ol>")));
    }

    @Test
    public void endNotesAreAppendedToText() {
        assertThat(
            convertToHtml("endnotes.docx", Mammoth.Options.DEFAULT.idPrefix("doc-42-")),
            deepEquals(success(
                "<p>Ouch" +
                "<sup><a href=\"#doc-42-endnote-2\" id=\"doc-42-endnote-ref-2\">[1]</a></sup>." +
                "<sup><a href=\"#doc-42-endnote-3\" id=\"doc-42-endnote-ref-3\">[2]</a></sup></p>" +
                "<ol><li id=\"doc-42-endnote-2\"><p> A tachyon walks into a bar. <a href=\"#doc-42-endnote-ref-2\">↑</a></p></li>" +
                "<li id=\"doc-42-endnote-3\"><p> Fin. <a href=\"#doc-42-endnote-ref-3\">↑</a></p></li></ol>")));
    }

    @Test
    public void relationshipsAreReadForEachFileContainingBodyXml() {
        assertThat(
            convertToHtml("footnote-hyperlink.docx", Mammoth.Options.DEFAULT.idPrefix("doc-42-")),
            deepEquals(success(
                "<p><sup><a href=\"#doc-42-footnote-1\" id=\"doc-42-footnote-ref-1\">[1]</a></sup></p>" +
                "<ol><li id=\"doc-42-footnote-1\"><p> <a href=\"http://www.example.com\">Example</a> <a href=\"#doc-42-footnote-ref-1\">↑</a></p></li></ol>")));
    }

    @Test
    public void canExtractRawText() {
        assertThat(
            Mammoth.extractRawText(TestData.file("simple-list.docx")),
            deepEquals(success("Apple\n\nBanana\n\n")));
    }

    private Result<String> convertToHtml(String name) {
        File file = TestData.file(name);
        return Mammoth.convertToHtml(file);
    }

    private Result<String> convertToHtml(String name, Mammoth.Options options) {
        File file = TestData.file(name);
        return Mammoth.convertToHtml(file, options);
    }
}
