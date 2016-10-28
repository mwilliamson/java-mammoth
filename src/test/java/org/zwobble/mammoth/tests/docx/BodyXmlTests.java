package org.zwobble.mammoth.tests.docx;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.docx.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.Maps;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;
import org.zwobble.mammoth.internal.xml.XmlNodes;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.documents.NoteReference.endnoteReference;
import static org.zwobble.mammoth.internal.documents.NoteReference.footnoteReference;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.util.Streams.toByteArray;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.ResultMatchers.*;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;
import static org.zwobble.mammoth.tests.docx.OfficeXmlBuilders.*;

public class BodyXmlTests {
    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = textXml("Hello!");
        assertThat(readSuccess(bodyReader(), element), isTextElement("Hello!"));
    }

    @Test
    public void canReadTextWithinRun() {
        XmlElement element = runXml(list(textXml("Hello!")));
        assertThat(
            readSuccess(bodyReader(), element),
            isRun(run(withChildren(text("Hello!")))));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(list(runXml(list(textXml("Hello!")))));
        assertThat(
            readSuccess(bodyReader(), element),
            isParagraph(paragraph(withChildren(run(withChildren(text("Hello!")))))));
    }

    @Test
    public void paragraphHasNoStyleIfItHasNoProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(bodyReader(), element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void whenParagraphHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));

        Style style = new Style("Heading1", Optional.of("Heading 1"));
        Styles styles = new Styles(
            map("Heading1", style),
            map());
        assertThat(
            readSuccess(bodyReader(styles), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenParagraphStyleCannotBeFound() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));
        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1", Optional.empty()))),
                list("Paragraph style with ID Heading1 was referenced but not defined in the document")));
    }

    @Test
    public void paragraphHasNoNumberingIfItHasNoNumberingProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(bodyReader(), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void paragraphHasNumberingPropertiesFromParagraphPropertiesIfPresent() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:ilvl", map("w:val", "1")),
                    element("w:numId", map("w:val", "42"))))))));

        Numbering numbering = new Numbering(map("42", map("1", NumberingLevel.ordered("1"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(NumberingLevel.ordered("1")));
    }

    @Test
    public void numberingPropertiesAreIgnoredIfLevelIsMissing() {
        // TODO: emit warning
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:numId", map("w:val", "42"))))))));

        Numbering numbering = new Numbering(map("42", map("1", NumberingLevel.ordered("1"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void numberingPropertiesAreIgnoredIfNumIdIsMissing() {
        // TODO: emit warning
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:ilvl", map("w:val", "1"))))))));

        Numbering numbering = new Numbering(map("42", map("1", NumberingLevel.ordered("1"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void runHasNoStyleIfItHasNoProperties() {
        XmlElement element = runXml(list());
        assertThat(
            readSuccess(bodyReader(), element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void whenRunHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = runXml(list(
            element("w:rPr", list(
                element("w:rStyle", map("w:val", "Heading1Char"))))));

        Style style = new Style("Heading1Char", Optional.of("Heading 1 Char"));
        Styles styles = new Styles(
            map(),
            map("Heading1Char", style));
        assertThat(
            readSuccess(bodyReader(styles), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenRunStyleCannotBeFound() {
        XmlElement element = runXml(list(
            element("w:rPr", list(
                element("w:rStyle", map("w:val", "Heading1Char"))))));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1Char", Optional.empty()))),
                list("Run style with ID Heading1Char was referenced but not defined in the document")));
    }

    @Test
    public void runIsNotBoldIfBoldElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("bold", equalTo(false)));
    }

    @Test
    public void runIsBoldIfBoldElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:b"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("bold", equalTo(true)));
    }

    @Test
    public void runIsNotItalicIfItalicElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("italic", equalTo(false)));
    }

    @Test
    public void runIsItalicIfItalicElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:i"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("italic", equalTo(true)));
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false)));
    }

    @Test
    public void runIsUnderlinedIfUnderlineElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:u"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(true)));
    }

    @Test
    public void runIsNotStruckthroughIfStrikethroughElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("strikethrough", equalTo(false)));
    }

    @Test
    public void runIsStruckthroughIfStrikethroughElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:strike"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("strikethrough", equalTo(true)));
    }

    @RunWith(Parameterized.class)
    public static class RunBooleanPropertyTests {
        @Parameterized.Parameters(name = "propertyName: {0}, tagName: {1}")
        public static Collection<Object[]> data() {
            return list(new Object[][] {
                {"bold", "w:b"}, {"underline", "w:u"}, {"italic", "w:i"}, {"strikethrough", "w:strike"}
            });
        }

        private final String propertyName;
        private final String tagName;

        public RunBooleanPropertyTests(String propertyName, String tagName) {
            this.propertyName = propertyName;
            this.tagName = tagName;
        }

        @Test
        public void runBooleanPropertyIsFalseIfElementIsPresentAndValIsFalse() {
            XmlElement element = runXmlWithProperties(element(tagName, map("w:val", "false")));

            assertThat(
                readSuccess(bodyReader(), element),
                hasProperty(propertyName, equalTo(false)));
        }

        @Test
        public void runBooleanPropertyIsFalseIfElementIsPresentAndValIs0() {
            XmlElement element = runXmlWithProperties(element(tagName, map("w:val", "0")));

            assertThat(
                readSuccess(bodyReader(), element),
                hasProperty(propertyName, equalTo(false)));
        }

        @Test
        public void runBooleanPropertyIsFalseIfElementIsPresentAndValIsTrue() {
            XmlElement element = runXmlWithProperties(element(tagName, map("w:val", "true")));

            assertThat(
                readSuccess(bodyReader(), element),
                hasProperty(propertyName, equalTo(true)));
        }

        @Test
        public void runBooleanPropertyIsFalseIfElementIsPresentAndValIs1() {
            XmlElement element = runXmlWithProperties(element(tagName, map("w:val", "1")));

            assertThat(
                readSuccess(bodyReader(), element),
                hasProperty(propertyName, equalTo(true)));
        }
    }

    @Test
    public void runHasBaselineVerticalAlignmentIfVerticalAlignmentElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.BASELINE)));
    }

    @Test
    public void runIsSuperscriptIfVerticalAlignmentPropertyIsSetToSuperscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "superscript")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUPERSCRIPT)));
    }

    @Test
    public void runIsSubscriptIfVerticalAlignmentPropertyIsSetToSubscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "subscript")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUBSCRIPT)));
    }

    @Test
    public void canReadTabElement() {
        XmlElement element = element("w:tab");

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Tab.TAB));
    }

    @Test
    public void brIsReadAsLineBreak() {
        XmlElement element = element("w:br");

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(LineBreak.LINE_BREAK));
    }

    @Test
    public void warningOnBreaksThatArentLineBreaks() {
        XmlElement element = element("w:br", map("w:type", "page"));

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("Unsupported break type: page")));
    }

    @Test
    public void canReadTableElements() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p")))))));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(
                    new TableCell(1, 1, list(
                        paragraph()
                    ))
                ))
            )))
        );
    }

    @Test
    public void gridspanIsReadAsColspanForTableCell() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:tcPr", list(
                        element("w:gridSpan", map("w:val", "2"))
                    )),
                    element("w:p")))))));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(
                    new TableCell(1, 2, list(
                        paragraph()
                    ))
                ))
            )))
        );
    }

    @Test
    public void vmergeIsReadAsRowspanForTableCell() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc()),
            wTr(wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc())
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(new TableCell(1, 1, list()))),
                new TableRow(list(new TableCell(3, 1, list()))),
                new TableRow(list()),
                new TableRow(list()),
                new TableRow(list(new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void vmergeWithoutValIsTreatedAsContinue() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(element("w:vMerge"))))
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(new TableCell(2, 1, list()))),
                new TableRow(list())
            )))
        );
    }

    @Test
    public void vmergeAccountsForCellsSpanningColumns() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(), wTc(), wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(wGridspan("2"))), wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(), wTc(), wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(), wTc(), wTc())
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()), new TableCell(3, 1, list()))),
                new TableRow(list(new TableCell(1, 2, list()))),
                new TableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()))),
                new TableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()), new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void noVerticalCellMergingIfMergedCellsDoNotLineUp() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(wTcPr(wGridspan("2"))), wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(), wTc(wTcPr(wVmerge("continue"))))
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Table(list(
                new TableRow(list(new TableCell(1, 2, list()), new TableCell(1, 1, list()))),
                new TableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void warningIfNonRowInTable() {
        XmlElement element = element("w:tbl", list(
            element("w:p")
        ));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                deepEquals(new Table(list(paragraph()))),
                list("unexpected non-row element in table, cell merging may be incorrect")
            )
        );
    }

    @Test
    public void warningIfNonCellInTableRow() {
        XmlElement element = element("w:tbl", list(
            wTr(element("w:p"))
        ));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                deepEquals(new Table(list(new TableRow(list(paragraph()))))),
                list("unexpected non-cell element in table row, cell merging may be incorrect")
            )
        );
    }

    @Test
    public void hyperlinkIsReadIfItHasARelationshipId() {
        Relationships relationships = new Relationships(
            map("r42", new Relationship("http://example.com")));
        XmlElement element = element("w:hyperlink", map("r:id", "r42"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(relationships), element),
            deepEquals(Hyperlink.href("http://example.com", list(run(withChildren())))));
    }

    @Test
    public void hyperlinkIsReadIfItHasAnAnchorAttribute() {
        XmlElement element = element("w:hyperlink", map("w:anchor", "start"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(Hyperlink.anchor("start", list(run(withChildren())))));
    }

    @Test
    public void hyperlinkIsIgnoredIfItDoesNotHaveARelationshipIdNorAnchor() {
        XmlElement element = element("w:hyperlink", list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(run(withChildren())));
    }

    @Test
    public void goBackBookmarkIsIgnored() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "_GoBack", "w:id", "7"));
        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void bookmarkStartIsReadIfNameIsNotGoBack() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "start", "w:id", "3"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new BookmarkStart("start")));
    }

    @Test
    public void readsAPairOfBookmarkStartAndEnd() {
        XmlElement startElement = element("w:bookmarkStart", map("w:name", "start", "w:id", "3"));
        XmlElement endElement = element("w:bookmarkEnd", map("w:id", "3"));
        XmlElement parent = new XmlElement("w:p", Maps.map(), Lists.list(startElement, endElement));
        assertThat(
                readSuccess(bodyReader(), parent),
                deepEquals(new Paragraph(
                        Optional.empty(),
                        Optional.empty(),
                        Lists.list(new BookmarkStart("start"), new BookmarkEnd("start")))));
    }

    @Test
    public void footnoteReferenceHasIdRead() {
        XmlElement element = element("w:footnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(footnoteReference("4")));
    }

    @Test
    public void endnoteReferenceHasIdRead() {
        XmlElement element = element("w:endnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(endnoteReference("4")));
    }

    @Test
    public void commentReferenceHasIdRead() {
        XmlElement element = element("w:commentReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new CommentReference("4")));
    }

    @Test
    public void textBoxesHaveContentAppendedAfterContainingParagraph() {
        XmlElement textBox = element("w:pict", list(
            element("v:shape", list(
                element("v:textbox", list(
                    element("w:txbxContent", list(
                        paragraphXml(list(
                            runXml(list(textXml("[textbox-content]")))))))))))));
        XmlElement paragraph = paragraphXml(list(
            runXml(list(textXml("[paragragh start]"))),
            runXml(list(textBox, textXml("[paragragh end]")))));

        List<DocumentElement> expected = list(
            paragraph(withChildren(
                run(withChildren(
                    new Text("[paragragh start]"))),
                run(withChildren(
                    new Text("[paragragh end]"))))),
            paragraph(withChildren(
                run(withChildren(
                    new Text("[textbox-content]"))))));

        assertThat(
            readAll(bodyReader(), paragraph),
            isInternalResult(deepEquals(expected), list()));
    }


    private static final String IMAGE_BYTES = "Not an image at all!";
    private static final String IMAGE_RELATIONSHIP_ID = "rId5";


    @Test
    public void canReadImagedataElementsWithIdAttribute() throws IOException {
        assertCanReadEmbeddedImage(image ->
            element("v:imagedata", map("r:id", image.relationshipId, "o:title", image.altText)));
    }

    @Test
    public void whenImagedataElementHasNoRelationshipIdThenItIsIgnoredWithWarning() throws IOException {
        XmlElement element = element("v:imagedata");

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("A v:imagedata element without a relationship ID was ignored")));
    }

    @Test
    public void canReadInlinePictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            inlineImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    @Test
    public void altTextTitleIsUsedIfAltTextDescriptionIsBlank() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.of(" "),
            Optional.of("It's a hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void altTextTitleIsUsedIfAltTextDescriptionIsMissing() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.empty(),
            Optional.of("It's a hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void altTextDescriptionIsPreferredToAltTextTitle() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.of("It's a hat"),
            Optional.of("hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void canReadAnchoredPictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            anchoredImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    private void assertCanReadEmbeddedImage(Function<EmbeddedImage, XmlElement> generateXml) throws IOException {
        XmlElement element = generateXml.apply(new EmbeddedImage(IMAGE_RELATIONSHIP_ID, "It's a hat"));
        Image image = readEmbeddedImage(element);
        assertThat(image, allOf(
            hasProperty("altText", deepEquals(Optional.of("It's a hat"))),
            hasProperty("contentType", deepEquals(Optional.of("image/png")))));
        assertThat(
            toString(image.open()),
            equalTo(IMAGE_BYTES));
    }

    private Image readEmbeddedImage(XmlElement element) {
        Relationships relationships = new Relationships(map(
            IMAGE_RELATIONSHIP_ID, new Relationship("media/hat.png")));
        DocxFile file = InMemoryDocxFile.fromStrings(map("word/media/hat.png", IMAGE_BYTES));

        return (Image) readSuccess(
            BodyXmlReaderMakers.bodyReader(relationships, file),
            element);
    }

    private static String toString(InputStream stream) throws IOException {
        return new String(toByteArray(stream), StandardCharsets.UTF_8);
    }

    private class EmbeddedImage {
        private final String relationshipId;
        private final String altText;

        public EmbeddedImage(String relationshipId, String altText) {
            this.relationshipId = relationshipId;
            this.altText = altText;
        }
    }

    @Test
    public void warningIfImageTypeIsUnsupportedByWebBrowsers() {
        XmlElement element = inlineImageXml(embeddedBlipXml(IMAGE_RELATIONSHIP_ID), "");
        Relationships relationships = new Relationships(map(
            IMAGE_RELATIONSHIP_ID, new Relationship("media/hat.emf")));
        DocxFile file = InMemoryDocxFile.fromStrings(map("word/media/hat.emf", IMAGE_BYTES));
        ContentTypes contentTypes = new ContentTypes(map("emf", "image/x-emf"), map());

        InternalResult<?> result = read(
            bodyReader(relationships, file, contentTypes),
            element);

        assertThat(
            result,
            hasWarnings(list("Image of type image/x-emf is unlikely to display in web browsers")));
    }

    @Test
    public void canReadLinkedPictures() throws IOException {
        XmlElement element = inlineImageXml(linkedBlipXml(IMAGE_RELATIONSHIP_ID), "");
        Relationships relationships = new Relationships(map(
            IMAGE_RELATIONSHIP_ID, new Relationship("file:///media/hat.png")));

        Image image = (Image) readSuccess(
            bodyReader(relationships, new InMemoryFileReader(map("file:///media/hat.png", IMAGE_BYTES))),
            element);

        assertThat(
            toString(image.open()),
            equalTo(IMAGE_BYTES));
    }

    private XmlElement inlineImageXml(XmlElement blip, String description) {
        return inlineImageXml(blip, Optional.of(description), Optional.empty());
    }

    private XmlElement inlineImageXml(XmlElement blip, Optional<String> description, Optional<String> title) {
        return element("w:drawing", list(
            element("wp:inline", imageXml(blip, description, title))));
    }

    private XmlElement anchoredImageXml(XmlElement blip, String description) {
        return element("w:drawing", list(
            element("wp:anchor", imageXml(blip, Optional.of(description), Optional.empty()))));
    }

    private List<XmlNode> imageXml(XmlElement blip, Optional<String> description, Optional<String> title) {
        Map<String, String> properties = new HashMap<>();
        description.ifPresent(value -> properties.put("descr", value));
        title.ifPresent(value -> properties.put("title", value));

        return list(
            element("wp:docPr", properties),
            element("a:graphic", list(
                element("a:graphicData", list(
                    element("pic:pic", list(
                        element("pic:blipFill", list(blip)))))))));
    }

    private XmlElement embeddedBlipXml(String relationshipId) {
        return blipXml(map("r:embed", relationshipId));
    }

    private XmlElement linkedBlipXml(String relationshipId) {
        return blipXml(map("r:link", relationshipId));
    }

    private XmlElement blipXml(Map<String, String> attributes) {
        return element("a:blip", attributes);
    }

    @Test
    public void sdtIsReadUsingSdtContent() throws IOException {
        XmlElement element = element("w:sdt", list(element("w:sdtContent", list(textXml("Blackdown")))));

        assertThat(
            readAll(bodyReader(), element),
            isInternalSuccess(deepEquals(list(text("Blackdown")))));
    }

    @Test
    public void appropriateElementsHaveTheirChildrenReadNormally() {
        assertChildrenAreReadNormally("w:ins");
        assertChildrenAreReadNormally("w:smartTag");
        assertChildrenAreReadNormally("w:drawing");
        assertChildrenAreReadNormally("v:roundrect");
        assertChildrenAreReadNormally("v:shape");
        assertChildrenAreReadNormally("v:textbox");
        assertChildrenAreReadNormally("w:txbxContent");
    }

    private void assertChildrenAreReadNormally(String name) {
        XmlElement element = element(name, list(paragraphXml()));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(paragraph()));
    }

    @Test
    public void ignoredElementsAreIgnoredWithoutWarning() {
        assertIsIgnored("office-word:wrap");
        assertIsIgnored("v:shadow");
        assertIsIgnored("v:shapetype");
        assertIsIgnored("w:sectPr");
        assertIsIgnored("w:proofErr");
        assertIsIgnored("w:lastRenderedPageBreak");
        assertIsIgnored("w:commentRangeStart");
        assertIsIgnored("w:commentRangeEnd");
        assertIsIgnored("w:del");
        assertIsIgnored("w:footnoteRef");
        assertIsIgnored("w:endnoteRef");
        assertIsIgnored("w:annotationRef");
        assertIsIgnored("w:pPr");
        assertIsIgnored("w:rPr");
        assertIsIgnored("w:tblPr");
        assertIsIgnored("w:tblGrid");
        assertIsIgnored("w:tcPr");
    }

    private void assertIsIgnored(String name) {
        XmlElement element = element(name, list(paragraphXml()));

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void unrecognisedElementsAreIgnoredWithWarning() {
        XmlElement element = element("w:huh");
        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("An unrecognised element was ignored: w:huh")));
    }

    @Test
    public void textNodesAreIgnoredWhenReadingChildren() {
        XmlElement element = runXml(list(XmlNodes.text("[text]")));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(run(withChildren())));
    }

    private static DocumentElement readSuccess(BodyXmlReader reader, XmlElement element) {
        InternalResult<DocumentElement> result = read(reader, element);
        assertThat(result.getWarnings(), emptyIterable());
        return result.getValue();
    }

    private static InternalResult<DocumentElement> read(BodyXmlReader reader, XmlElement element) {
        InternalResult<List<DocumentElement>> result = readAll(reader, element);
        assertThat(result.getValue(), Matchers.hasSize(1));
        return result.map(elements -> elements.get(0));
    }

    private static InternalResult<List<DocumentElement>> readAll(BodyXmlReader reader, XmlElement element) {
        return reader.readElement(element).toResult();
    }

    private XmlElement paragraphXml() {
        return paragraphXml(list());
    }

    private XmlElement paragraphXml(List<XmlNode> children) {
        return element("w:p", children);
    }

    private XmlElement runXml(List<XmlNode> children) {
        return element("w:r", children);
    }

    private static XmlElement runXmlWithProperties(XmlNode... children) {
        return element("w:r", list(element("w:rPr", asList(children))));
    }

    private XmlElement textXml(String value) {
        return element("w:t", list(XmlNodes.text(value)));
    }

    private Matcher<DocumentElement> isParagraph(Paragraph expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isRun(Run expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isTextElement(String value) {
        return new DeepReflectionMatcher<>(new Text(value));
    }

    private Matcher<? super DocumentElement> hasStyle(Optional<Style> expected) {
        return hasProperty("style", deepEquals(expected));
    }

    private Matcher<? super DocumentElement> hasNumbering(NumberingLevel expected) {
        return hasNumbering(Optional.of(expected));
    }

    private Matcher<? super DocumentElement> hasNumbering(Optional<NumberingLevel> expected) {
        return hasProperty("numbering", deepEquals(expected));
    }

    private Text text(String value) {
        return new Text(value);
    }
}
