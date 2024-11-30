package org.zwobble.mammoth.tests.conversion;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.conversion.DocumentToHtml;
import org.zwobble.mammoth.internal.conversion.DocumentToHtmlOptions;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.styles.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.documents.VerticalAlignment.SUBSCRIPT;
import static org.zwobble.mammoth.internal.documents.VerticalAlignment.SUPERSCRIPT;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.util.Sets.set;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalSuccess;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;

public class DocumentToHtmlTests {
    @Test
    public void plainParagraphIsConvertedToPlainParagraph() {
        assertThat(
            convertToHtml(paragraph(withChildren(runWithText("Hello")))),
            deepEquals(list(Html.element("p", list(Html.text("Hello"))))));
    }

    @Test
    public void forceWriteIsInsertedIntoParagraphIfEmptyParagraphsShouldBePreserved() {
        DocumentToHtmlOptions options = DocumentToHtmlOptions.DEFAULT.preserveEmptyParagraphs();
        assertThat(
            DocumentToHtml.convertToHtml(paragraph(), options),
            isSuccess(list(Html.element("p", list(Html.FORCE_WRITE)))));
    }

    @Test
    public void multipleParagraphsInDocumentAreConvertedToMultipleParagraphs() {
        assertThat(
            convertToHtml(document(
                withChildren(
                    paragraph(withChildren(runWithText("Hello"))),
                    paragraph(withChildren(runWithText("there")))
                )
            )),

            deepEquals(list(
                Html.element("p", list(Html.text("Hello"))),
                Html.element("p", list(Html.text("there"))))));
    }

    @Test
    public void paragraphStyleMappingsCanBeUsedToMapParagraphs() {
        assertThat(
            convertToHtml(
                paragraph(withStyle(new Style("TipsParagraph", Optional.empty()))),
                StyleMap.builder()
                    .mapParagraph(
                        ParagraphMatcher.styleId("TipsParagraph"),
                        HtmlPath.element("p", map("class", "tip")))
                    .build()),

            deepEquals(list(Html.element("p", map("class", "tip")))));
    }

    @Test
    public void warningIfParagraphHasUnrecognisedStyle() {
        assertThat(
            convertToHtmlResult(
                paragraph(withStyle(new Style("TipsParagraph", Optional.of("Tips Paragraph"))))),

            deepEquals(new InternalResult<>(
                list(Html.element("p")),
                set("Unrecognised paragraph style: Tips Paragraph (Style ID: TipsParagraph)"))));
    }

    @Test
    public void runStyleMappingsCanBeUsedToMapRuns() {
        assertThat(
            convertToHtml(
                run(withStyle(new Style("TipsRun", Optional.empty()))),
                StyleMap.builder()
                    .mapRun(
                        RunMatcher.styleId("TipsRun"),
                        HtmlPath.element("span", map("class", "tip")))
                    .build()),

            deepEquals(list(Html.element("span", map("class", "tip")))));
    }

    @Test
    public void warningIfRunHasUnrecognisedStyle() {
        assertThat(
            convertToHtmlResult(
                run(
                    withStyle(new Style("TipsRun", Optional.of("Tips Run"))),
                    withChildren(new Text("Hello")))),

            deepEquals(new InternalResult<>(
                list(Html.text("Hello")),
                set("Unrecognised run style: Tips Run (Style ID: TipsRun)"))));
    }

    @Test
    public void boldRunsAreWrappedInStrongTagsByDefault() {
        assertThat(
            convertToHtml(run(withBold(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("strong", list(Html.text("Hello"))))));
    }

    @Test
    public void boldRunsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withBold(true), withChildren(new Text("Hello"))),
                StyleMap.builder().bold(HtmlPath.element("em")).build()),
            deepEquals(list(Html.element("em", list(Html.text("Hello"))))));
    }

    @Test
    public void italicRunsAreWrappedInEmphasisTags() {
        assertThat(
            convertToHtml(run(withItalic(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("em", list(Html.text("Hello"))))));
    }

    @Test
    public void italicRunsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withItalic(true), withChildren(new Text("Hello"))),
                StyleMap.builder().italic(HtmlPath.element("strong")).build()),
            deepEquals(list(Html.element("strong", list(Html.text("Hello"))))));
    }

    @Test
    public void underliningIsIgnoredByDefault() {
        assertThat(
            convertToHtml(run(withUnderline(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.text("Hello"))));
    }

    @Test
    public void underliningCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withUnderline(true), withChildren(new Text("Hello"))),
                StyleMap.builder().underline(HtmlPath.element("em")).build()),

            deepEquals(list(Html.element("em", list(Html.text("Hello"))))));
    }

    @Test
    public void struckthroughRunsAreWrappedInStrikethroughTagsByDefault() {
        assertThat(
            convertToHtml(run(withStrikethrough(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("s", list(Html.text("Hello"))))));
    }

    @Test
    public void struckthroughRunsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withStrikethrough(true), withChildren(new Text("Hello"))),
                StyleMap.builder().strikethrough(HtmlPath.element("del")).build()),
            deepEquals(list(Html.element("del", list(Html.text("Hello"))))));
    }

    @Test
    public void allCapsIsIgnoredByDefault() {
        assertThat(
            convertToHtml(run(withAllCaps(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.text("Hello")))
        );
    }

    @Test
    public void allCapsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withAllCaps(true), withChildren(new Text("Hello"))),
                StyleMap.builder().allCaps(HtmlPath.element("span")).build()
            ),

            deepEquals(list(Html.element("span", list(Html.text("Hello")))))
        );
    }

    @Test
    public void smallCapsIsIgnoredByDefault() {
        assertThat(
            convertToHtml(run(withSmallCaps(true), withChildren(new Text("Hello")))),
            deepEquals(list(Html.text("Hello"))));
    }

    @Test
    public void smallCapsCanBeMappedUsingStyleMapping() {
        assertThat(
            convertToHtml(
                run(withSmallCaps(true), withChildren(new Text("Hello"))),
                StyleMap.builder().smallCaps(HtmlPath.element("span")).build()),

            deepEquals(list(Html.element("span", list(Html.text("Hello"))))));
    }

    @Test
    public void highlightedRunsAreIgnoredByDefault() {
        Run run = run(withHighlight("yellow"), withChildren(new Text("Hello")));

        List<HtmlNode> result = convertToHtml(run);

        assertThat(result, deepEquals(list(Html.text("Hello"))));
    }

    @Test
    public void highlightedRunsCanBeConfiguredWithStyleMappingForAllHighlights() {
        Run run = run(withHighlight("yellow"), withChildren(new Text("Hello")));
        StyleMap styleMap = StyleMap.builder()
            .mapHighlight(new HighlightMatcher(Optional.empty()), HtmlPath.element("mark"))
            .build();

        List<HtmlNode> result = convertToHtml(run, styleMap);

        assertThat(result, deepEquals(list(
            Html.element("mark", list(Html.text("Hello")))
        )));
    }

    @Test
    public void highlightedRunsCanBeConfiguredWithStyleMappingForSpecificHighlightColor() {
        Paragraph paragraph = paragraph(withChildren(
            run(withHighlight("yellow"), withChildren(new Text("Yellow"))),
            run(withHighlight("red"), withChildren(new Text("Red")))
        ));
        StyleMap styleMap = StyleMap.builder()
            .mapHighlight(new HighlightMatcher(Optional.of("yellow")), HtmlPath.element("mark", map("class", "yellow")))
            .mapHighlight(new HighlightMatcher(Optional.empty()), HtmlPath.element("mark"))
            .build();

        List<HtmlNode> result = convertToHtml(paragraph, styleMap);

        assertThat(result, deepEquals(list(
            Html.element("p", list(
                Html.element("mark", map("class", "yellow"), list(Html.text("Yellow"))),
                Html.element("mark", list(Html.text("Red")))
            ))
        )));
    }

    @Test
    public void superscriptRunsAreWrappedInSuperscriptTags() {
        assertThat(
            convertToHtml(run(withVerticalAlignment(SUPERSCRIPT), withChildren(new Text("Hello")))),
            deepEquals(list(Html.collapsibleElement("sup", list(Html.text("Hello"))))));
    }

    @Test
    public void subscriptRunsAreWrappedInSubscriptTags() {
        assertThat(
            convertToHtml(run(withVerticalAlignment(SUBSCRIPT), withChildren(new Text("Hello")))),
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
            convertToHtml(Break.LINE_BREAK),
            deepEquals(list(Html.element("br"))));
    }

    @Test
    public void breaksThatAreNotLineBreaksAreIgnored() {
        assertThat(
            convertToHtml(Break.PAGE_BREAK),
            deepEquals(list())
        );
    }

    @Test
    public void breaksCanBeMappedUsingStyleMappings() {
        assertThat(
            convertToHtml(
                run(withChildren(
                    Break.PAGE_BREAK,
                    Break.LINE_BREAK
                )),
                StyleMap.builder()
                    .mapBreak(BreakMatcher.PAGE_BREAK, HtmlPath.element("hr"))
                    .mapBreak(BreakMatcher.LINE_BREAK, HtmlPath.element("br", map("class", "line-break")))
                    .build()
            ),
            deepEquals(list(
                Html.element("hr"),
                Html.element("br", map("class", "line-break"))
            ))
        );
    }

    @Test
    public void tableIsConvertedToHtmlTable() {
        assertThat(
            convertToHtml(table(list(
                tableRow(list(
                    tableCell(withChildren(paragraphWithText("Top left"))),
                    tableCell(withChildren(paragraphWithText("Top right"))))),
                tableRow(list(
                    tableCell(withChildren(paragraphWithText("Bottom left"))),
                    tableCell(withChildren(paragraphWithText("Bottom right")))))))),

            deepEquals(list(Html.element("table", list(
                Html.element("tr", list(
                    Html.FORCE_WRITE,
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top left"))))),
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top right"))))))),
                Html.element("tr", list(
                    Html.FORCE_WRITE,
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Bottom left"))))),
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Bottom right"))))))))))));
    }

    @Test
    public void tableStyleMappingsCanBeUsedToMapTables() {
        assertThat(
            convertToHtml(
                table(list(), withStyle(new Style("TableNormal", Optional.of("Normal Table")))),
                StyleMap.builder()
                    .mapTable(
                        TableMatcher.styleName("Normal Table"),
                        HtmlPath.element("table", map("class", "normal-table"))
                    )
                    .build()
            ),

            deepEquals(list(Html.element("table", map("class", "normal-table"))))
        );
    }

    @Test
    public void headerRowsAreWrappedInThead() {
        assertThat(
            convertToHtml(table(list(
                tableRow(list(tableCell()), withIsHeader(true)),
                tableRow(list(tableCell()), withIsHeader(true)),
                tableRow(list(tableCell()), withIsHeader(false))
            ))),

            deepEquals(list(Html.element("table", list(
                Html.element("thead", list(
                    Html.element("tr", list(Html.FORCE_WRITE, Html.element("th", list(Html.FORCE_WRITE)))),
                    Html.element("tr", list(Html.FORCE_WRITE, Html.element("th", list(Html.FORCE_WRITE))))
                )),
                Html.element("tbody", list(
                    Html.element("tr", list(Html.FORCE_WRITE, Html.element("td", list(Html.FORCE_WRITE))))
                ))
            ))))
        );
    }

    @Test
    public void tbodyIsOmittedIfAllRowsAreHeaders() {
        assertThat(
            Html.stripEmpty(convertToHtml(table(list(
                tableRow(list(tableCell()), withIsHeader(true))
            )))),

            deepEquals(list(Html.element("table", list(
                Html.element("thead", list(
                    Html.element("tr", list(Html.FORCE_WRITE, Html.element("th", list(Html.FORCE_WRITE))))
                ))
            ))))
        );
    }

    @Test
    public void tableCellsAreWrittenWithColspanIfNotEqualToOne() {
        assertThat(
            convertToHtml(table(list(
                tableRow(list(
                    tableCell(
                        withChildren(paragraphWithText("Top left")),
                        withColspan(2)
                    ),
                    tableCell(withChildren(paragraphWithText("Top right")))))))),

            deepEquals(list(Html.element("table", list(
                Html.element("tr", list(
                    Html.FORCE_WRITE,
                    Html.element("td", map("colspan", "2"), list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top left"))))),
                    Html.element("td", list(Html.FORCE_WRITE, Html.element("p", list(Html.text("Top right")))))
                ))
            ))))
        );
    }

    @Test
    public void tableCellsAreWrittenWithRowspanIfNotEqualToOne() {
        assertThat(
            convertToHtml(table(list(
                tableRow(list(
                    tableCell(withRowspan(2))
                ))
            ))),
            deepEquals(list(Html.element("table", list(
                Html.element("tr", list(
                    Html.FORCE_WRITE,
                    Html.element("td", map("rowspan", "2"), list(Html.FORCE_WRITE))
                ))
            ))))
        );
    }

    @Test
    public void hyperlinkWithHrefIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(hyperlink(
                withHref("http://example.com"),
                withChildren(new Text("Hello"))
            )),
            deepEquals(list(Html.collapsibleElement("a", map("href", "http://example.com"), list(Html.text("Hello"))))));
    }

    @Test
    public void hyperlinkWithInternalAnchorReferenceIsConvertedToAnchorTag() {
        assertThat(
            convertToHtml(hyperlink(
                withAnchor("start"),
                withChildren(new Text("Hello"))
            )),
            deepEquals(list(Html.collapsibleElement("a", map("href", "#doc-42-start"), list(Html.text("Hello"))))));
    }

    @Test
    public void hyperlinkTargetFrameIsUsedAsAnchorTarget() {
        assertThat(
            convertToHtml(hyperlink(
                withAnchor("start"),
                withTargetFrame("_blank"),
                withChildren(new Text("Hello"))
            )),
            deepEquals(list(Html.collapsibleElement("a", map("href", "#doc-42-start", "target", "_blank"), list(Html.text("Hello"))))));
    }

    @Test
    public void uncheckedCheckboxIsConvertedToUncheckedCheckboxInput() {
        assertThat(
            convertToHtml(checkbox(false)),
            deepEquals(list(Html.element("input", map("type", "checkbox"))))
        );
    }

    @Test
    public void checkedCheckboxIsConvertedToCheckedCheckboxInput() {
        assertThat(
            convertToHtml(checkbox(true)),
            deepEquals(list(Html.element("input", map("type", "checkbox", "checked", "checked"))))
        );
    }

    @Test
    public void bookmarksAreConvertedToAnchorsWithIds() {
        assertThat(
            convertToHtml(new Bookmark("start")),
            deepEquals(list(Html.element("a", map("id", "doc-42-start"), list(Html.FORCE_WRITE)))));
    }

    @Test
    public void noteReferencesAreConvertedToLinksToReferenceBodyAfterMainBody() {
        Document document = document(withChildren(
            paragraph(withChildren(
                runWithText("Knock knock"),
                run(withChildren(new NoteReference(NoteType.FOOTNOTE, "4")))))),
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
        Run run = run(withChildren(
            new NoteReference(NoteType.FOOTNOTE, "4"),
            new NoteReference(NoteType.FOOTNOTE, "7")));

        assertThat(
            convertToHtml(run),
            deepEquals(list(
                Html.element("sup", list(
                    Html.element("a", map("href", "#doc-42-footnote-4", "id", "doc-42-footnote-ref-4"), list(Html.text("[1]"))))),
                Html.element("sup", list(
                    Html.element("a", map("href", "#doc-42-footnote-7", "id", "doc-42-footnote-ref-7"), list(Html.text("[2]"))))))));
    }

    @Test
    public void commentsAreIgnoredByDefault() {
        Document document = document(
            withChildren(
                paragraph(withChildren(
                    runWithText("Knock knock"),
                    run(withChildren(new CommentReference("4")))))),
            withComments(comment("4", list(paragraphWithText("Who's there?")))));

        assertThat(
            convertToHtml(document),
            deepEquals(list(
                Html.element("p", list(
                    Html.text("Knock knock"))))));
    }

    @Test
    public void commentReferencesAreLinkedToCommentAfterMainBody() {
        CommentReference reference = new CommentReference("4");
        Comment comment = new Comment(
            "4",
            list(paragraphWithText("Who's there?")),
            Optional.of("The Piemaker"),
            Optional.of("TP")
        );
        Document document = document(
            withChildren(
                paragraph(withChildren(
                    runWithText("Knock knock"),
                    run(withChildren(reference))))),
            withComments(comment)
        );

        StyleMap styleMap = StyleMap.builder().commentReference(HtmlPath.element("sup")).build();
        assertThat(
            convertToHtml(document, styleMap),
            deepEquals(list(
                Html.element("p", list(
                    Html.text("Knock knock"),
                    Html.element("sup", list(
                        Html.element("a", map("href", "#doc-42-comment-4", "id", "doc-42-comment-ref-4"), list(Html.text("[TP1]"))))))),
                Html.element("dl", list(
                    Html.element("dt", map("id", "doc-42-comment-4"), list(
                        Html.text("Comment [TP1]"))),
                    Html.element("dd", list(
                        Html.element("p", list(
                            Html.text("Who's there?"))),
                        Html.collapsibleElement("p", list(
                            Html.text(" "),
                            Html.element("a", map("href", "#doc-42-comment-ref-4"), list(Html.text("↑"))))))))))));
    }

    @Test
    public void imagesAreConvertedToImageTagsWithDataUriByDefault() {
        Image image = new Image(
            Optional.empty(),
            Optional.of("image/png"),
            () -> new ByteArrayInputStream(new byte[]{97, 98, 99}));
        assertThat(
            convertToHtml(image),
            deepEquals(list(Html.element("img", map("src", "data:image/png;base64,YWJj")))));
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
        return convertToHtml(document, StyleMap.EMPTY);
    }

    private List<HtmlNode> convertToHtml(Document document, StyleMap styleMap) {
        DocumentToHtmlOptions options = DocumentToHtmlOptions.DEFAULT
            .idPrefix("doc-42-")
            .addStyleMap(styleMap);
        InternalResult<List<HtmlNode>> result = DocumentToHtml.convertToHtml(document, options);
        assertThat(result.getWarnings(), emptyIterable());
        return result.getValue();
    }

    private List<HtmlNode> convertToHtml(DocumentElement element) {
        return convertToHtml(element, StyleMap.EMPTY);
    }

    private List<HtmlNode> convertToHtml(DocumentElement element, StyleMap styleMap) {
        InternalResult<List<HtmlNode>> result = convertToHtmlResult(element, styleMap);
        assertThat(result.getWarnings(), emptyIterable());
        return result.getValue();
    }

    private InternalResult<List<HtmlNode>> convertToHtmlResult(DocumentElement element) {
        return convertToHtmlResult(element, StyleMap.EMPTY);
    }

    private InternalResult<List<HtmlNode>> convertToHtmlResult(DocumentElement element, StyleMap styleMap) {
        DocumentToHtmlOptions options = DocumentToHtmlOptions.DEFAULT
            .idPrefix("doc-42-")
            .addStyleMap(styleMap);
        return DocumentToHtml.convertToHtml(element, options);
    }

    private Matcher<? super InternalResult<List<HtmlNode>>> isSuccess(List<HtmlNode> expected) {
        return isInternalSuccess(deepEquals(expected));
    }
}
