package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.documents.VerticalAlignment.SUBSCRIPT;
import static org.zwobble.mammoth.documents.VerticalAlignment.SUPERSCRIPT;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class DocumentConverterTests {
    // TODO: styles (paragraph, run, underline, strikethrough)

    @Test
    public void plainParagraphIsConvertedToPlainParagraph() {
        assertThat(
            convertToHtml(make(a(PARAGRAPH, with(CHILDREN, list(runWithText("Hello")))))),
            deepEquals(list(Html.element("p", list(Html.text("Hello"))))));
    }

    @Test
    public void multipleParagraphsInDocumentAreConvertedToMultipleParagraphs() {
        assertThat(
            documentConverter().convertToHtml(new Document(list(
                make(a(PARAGRAPH, with(CHILDREN, list(runWithText("Hello"))))),
                make(a(PARAGRAPH, with(CHILDREN, list(runWithText("there")))))))),

            deepEquals(list(
                Html.element("p", list(Html.text("Hello"))),
                Html.element("p", list(Html.text("there"))))));
    }

    @Test
    public void boldRunsAreWrappedInStrongTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(BOLD, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.element("strong", list(Html.text("Hello"))))));
    }

    @Test
    public void italicRunsAreWrappedInEmphasisTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(ITALIC, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.element("em", list(Html.text("Hello"))))));
    }

    @Test
    public void underliningIsIgnoredByDefault() {
        assertThat(
            convertToHtml(make(a(RUN, with(UNDERLINE, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.text("Hello"))));
    }

    @Test
    public void struckthroughRunsAreWrappedInStrikethroughTagsByDefault() {
        assertThat(
            convertToHtml(make(a(RUN, with(STRIKETHROUGH, true), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.element("s", list(Html.text("Hello"))))));
    }

    @Test
    public void superscriptRunsAreWrappedInSuperscriptTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(VERTICAL_ALIGNMENT, SUPERSCRIPT), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.element("sup", list(Html.text("Hello"))))));
    }

    @Test
    public void subscriptRunsAreWrappedInSubscriptTags() {
        assertThat(
            convertToHtml(make(a(RUN, with(VERTICAL_ALIGNMENT, SUBSCRIPT), with(CHILDREN, list(new Text("Hello")))))),
            deepEquals(list(Html.element("sub", list(Html.text("Hello"))))));
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
                    Html.element("td", list(Html.element("p", list(Html.text("Top left"))))),
                    Html.element("td", list(Html.element("p", list(Html.text("Top right"))))))),
                Html.element("tr", list(
                    Html.element("td", list(Html.element("p", list(Html.text("Bottom left"))))),
                    Html.element("td", list(Html.element("p", list(Html.text("Bottom right"))))))))))));
    }

    @Test
    public void hyperlinkWithHrefIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(Hyperlink.href("http://example.com", list(new Text("Hello")))),
            deepEquals(list(Html.element("a", map("href", "http://example.com"), list(Html.text("Hello"))))));
    }

    @Test
    public void hyperlinkWithInternalAnchorReferenceIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(Hyperlink.anchor("start", list(new Text("Hello")))),
            deepEquals(list(Html.element("a", map("href", "#doc-42-start"), list(Html.text("Hello"))))));
    }

    @Test
    public void bookmarksAreConvertedToAnchorsWithIds() {
        assertThat(
            convertToHtml(new Bookmark("start")),
            deepEquals(list(Html.element("a", map("id", "doc-42-start")))));
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

    private List<HtmlNode> convertToHtml(DocumentElement element) {
        return documentConverter().convertToHtml(element);
    }

    private DocumentConverter documentConverter() {
        return new DocumentConverter("doc-42");
    }
}
