package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;
import org.zwobble.mammoth.styles.HtmlPath;
import org.zwobble.mammoth.styles.StyleMap;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.documents.VerticalAlignment.SUBSCRIPT;
import static org.zwobble.mammoth.documents.VerticalAlignment.SUPERSCRIPT;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class DocumentConverterTests {
    // TODO: styles (paragraph, run)

    @Test
    public void plainParagraphIsConvertedToPlainParagraph() {
        assertThat(
            convertToHtml(make(a(PARAGRAPH, with(CHILDREN, list(runWithText("Hello")))))),
            deepEquals(list(Html.element("p", list(Html.text("Hello"))))));
    }

    @Test
    public void forceWriteIsInsertedIntoParagraphIfEmptyParagraphsShouldBePreserved() {
        assertThat(
            DocumentConverter.convertToHtml("", true, StyleMap.EMPTY, make(a(PARAGRAPH))),
            deepEquals(list(Html.element("p", list(Html.FORCE_WRITE)))));
    }

    @Test
    public void multipleParagraphsInDocumentAreConvertedToMultipleParagraphs() {
        assertThat(
            convertToHtml(new Document(list(
                make(a(PARAGRAPH, with(CHILDREN, list(runWithText("Hello"))))),
                make(a(PARAGRAPH, with(CHILDREN, list(runWithText("there")))))), Notes.EMPTY)),

            deepEquals(list(
                Html.element("p", list(Html.text("Hello"))),
                Html.element("p", list(Html.text("there"))))));
    }

    @Test
    public void styleMappingsUsingStyleIdsCanBeUsedToMapParagraphs() {
        assertThat(
            convertToHtml(
                make(a(PARAGRAPH, with(STYLE, Optional.of(new Style("TipsParagraph", Optional.empty()))))),
                StyleMap.builder().mapParagraph("TipsParagraph", HtmlPath.element("p", map("class", "tip"))).build()),

            deepEquals(list(Html.element("p", map("class", "tip")))));
    }

    @Test
    public void boldRunsAreWrappedInStrongTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(BOLD, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.collapsibleElement("strong", list(Html.text("Hello"))))));
    }

    @Test
    public void italicRunsAreWrappedInEmphasisTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(ITALIC, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.collapsibleElement("em", list(Html.text("Hello"))))));
    }

    @Test
    public void underliningIsIgnoredByDefault() {
        assertThat(
            convertToHtml(make(a(RUN, with(UNDERLINE, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.text("Hello"))));
    }

    @Test
    public void underliningCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                make(a(RUN, with(UNDERLINE, true), with(CHILDREN, list(new Text("Hello"))))),
                StyleMap.builder().underline(HtmlPath.element("em")).build()),

            deepEquals(list(Html.element("em", list(Html.text("Hello"))))));
    }

    @Test
    public void struckthroughRunsAreWrappedInStrikethroughTagsByDefault() {
        assertThat(
            convertToHtml(make(a(RUN, with(STRIKETHROUGH, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.collapsibleElement("s", list(Html.text("Hello"))))));
    }

    @Test
    public void struckthroughRunsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                make(a(RUN, with(STRIKETHROUGH, true), with(CHILDREN, list(new Text("Hello"))))),
                StyleMap.builder().strikethrough(HtmlPath.element("del")).build()),
            deepEquals(list(Html.element("del", list(Html.text("Hello"))))));
    }

    @Test
    public void superscriptRunsAreWrappedInSuperscriptTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(VERTICAL_ALIGNMENT, SUPERSCRIPT), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.collapsibleElement("sup", list(Html.text("Hello"))))));
    }

    @Test
    public void subscriptRunsAreWrappedInSubscriptTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(VERTICAL_ALIGNMENT, SUBSCRIPT), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.collapsibleElement("sub", list(Html.text("Hello"))))));
    }

    @Test
    public void tabIsConvertedToTabInHtmlText() {
        assertThat(
            convertToHtml(Tab.TAB),
            deepEquals(list(Html.text("\t"))));
    }

    @Test
    public void lineBreakIsConvertedToBreakElement() {
        assertThat(
            convertToHtml(LineBreak.LINE_BREAK),
            deepEquals(list(Html.selfClosingElement("br"))));
    }

    @Test
    public void tableIsConvertedToHtmlTable() {
        assertThat(
            convertToHtml(new Table(list(
                new TableRow(list(
                    new TableCell(list(paragraphWithText("Top left"))),
                    new TableCell(list(paragraphWithText("Top right"))))),
                new TableRow(list(
                    new TableCell(list(paragraphWithText("Bottom left"))),
                    new TableCell(list(paragraphWithText("Bottom right")))))))),

            deepEquals(list(Html.element("table", list(
                Html.element("tr", list(
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top left"))))),
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top right"))))))),
                Html.element("tr", list(
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Bottom left"))))),
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Bottom right"))))))))))));
    }

    @Test
    public void hyperlinkWithHrefIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(Hyperlink.href("http://example.com", list(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("a", map("href", "http://example.com"), list(Html.text("Hello"))))));
    }

    @Test
    public void hyperlinkWithInternalAnchorReferenceIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(Hyperlink.anchor("start", list(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("a", map("href", "#doc-42-start"), list(Html.text("Hello"))))));
    }

    @Test
    public void bookmarksAreConvertedToAnchorsWithIds() {
        assertThat(
            convertToHtml(new Bookmark("start")),
            deepEquals(list(Html.element("a", map("id", "doc-42-start"), list(Html.FORCE_WRITE)))));
    }

    @Test
    public void noteReferencesAreConvertedToLinksToReferenceBodyAfterMainBody() {
        Document document = new Document(
            list(make(a(PARAGRAPH, with(CHILDREN, list(
                runWithText("Knock knock"),
                make(a(RUN, with(CHILDREN, list(new NoteReference(NoteType.FOOTNOTE, "4")))))))))),
            new Notes(list(new Note(NoteType.FOOTNOTE, "4", list(paragraphWithText("Who's there?"))))));

        assertThat(
            convertToHtml(document),
            deepEquals(list(
                Html.element("p", list(
                    Html.text("Knock knock"),
                    Html.element("sup", list(
                        Html.element("a", map("href", "#doc-42-footnote-4", "id", "doc-42-footnote-ref-4"), list(Html.text("[1]"))))))),
                Html.element("ol", list(
                    Html.element("li", map("id", "doc-42-footnote-4"), list(
                        Html.element("p", list(
                            Html.text("Who's there?"))),
                        Html.collapsibleElement("p", list(
                            Html.text(" "),
                            Html.element("a", map("href", "#doc-42-footnote-ref-4"), list(Html.text("↑"))))))))))));
    }

    @Test
    public void noteReferencesAreConvertedWithSequentialNumbers() {
        Run run = make(a(RUN, with(CHILDREN, list(
            new NoteReference(NoteType.FOOTNOTE, "4"),
            new NoteReference(NoteType.FOOTNOTE, "7")))));

        assertThat(
            convertToHtml(run),
            deepEquals(list(
                Html.element("sup", list(
                    Html.element("a", map("href", "#doc-42-footnote-4", "id", "doc-42-footnote-ref-4"), list(Html.text("[1]"))))),
                Html.element("sup", list(
                    Html.element("a", map("href", "#doc-42-footnote-7", "id", "doc-42-footnote-ref-7"), list(Html.text("[2]"))))))));
    }

    @Test
    public void imagesAreConvertedToImageTagsWithDataUriByDefault() {
        Image image = new Image(
            Optional.empty(),
            Optional.of("image/png"),
            () -> new ByteArrayInputStream(new byte[]{97, 98, 99}));
        assertThat(
            convertToHtml(image),
            deepEquals(list(Html.selfClosingElement("img", map("src", "data:image/png;base64,YWJj")))));
    }

    @Test
    public void imagesHaveAltTagsIfAvailable() {
        Image image = new Image(
            Optional.of("It's a hat"),
            Optional.of("image/png"),
            () -> new ByteArrayInputStream(new byte[]{97, 98, 99}));
        assertThat(
            convertToHtml(image),
            contains(hasProperty("attributes", hasEntry("alt", "It's a hat"))));
    }

    private List<HtmlNode> convertToHtml(Document document) {
        return DocumentConverter.convertToHtml("doc-42-", false, StyleMap.EMPTY, document);
    }

    private List<HtmlNode> convertToHtml(DocumentElement element) {
        return convertToHtml(element, StyleMap.EMPTY);
    }

    private List<HtmlNode> convertToHtml(DocumentElement element, StyleMap styleMap) {
        return DocumentConverter.convertToHtml("doc-42-", false, styleMap, element);
    }
}
