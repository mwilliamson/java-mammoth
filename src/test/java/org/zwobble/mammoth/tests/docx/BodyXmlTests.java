package org.zwobble.mammoth.tests.docx;

import com.natpryce.makeiteasy.Maker;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.docx.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;
import org.zwobble.mammoth.internal.xml.XmlNodes;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.natpryce.makeiteasy.MakeItEasy.*;
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
import static org.zwobble.mammoth.tests.ResultMatchers.hasWarnings;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalResult;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.*;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.NUMBERING;

public class BodyXmlTests {

    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = textXml("Hello!");
        assertThat(readSuccess(a(bodyReader), element), isTextElement("Hello!"));
    }

    @Test
    public void canReadTextWithinRun() {
        XmlElement element = runXml(list(textXml("Hello!")));
        assertThat(
            readSuccess(a(bodyReader), element),
            isRun(run(text("Hello!"))));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(list(runXml(list(textXml("Hello!")))));
        assertThat(
            readSuccess(a(bodyReader), element),
            isParagraph(make(a(PARAGRAPH, with(CHILDREN, list(run(text("Hello!"))))))));
    }

    @Test
    public void paragraphHasNoStyleIfItHasNoProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(a(bodyReader), element),
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
            readSuccess(a(bodyReader, with(STYLES, styles)), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenParagraphStyleCannotBeFound() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));
        assertThat(
            read(a(bodyReader), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1", Optional.empty()))),
                list("Paragraph style with ID Heading1 was referenced but not defined in the document")));
    }

    @Test
    public void paragraphHasNoNumberingIfItHasNoNumberingProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(a(bodyReader), element),
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
            readSuccess(a(bodyReader, with(NUMBERING, numbering)), element),
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
            readSuccess(a(bodyReader, with(NUMBERING, numbering)), element),
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
            readSuccess(a(bodyReader, with(NUMBERING, numbering)), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void runHasNoStyleIfItHasNoProperties() {
        XmlElement element = runXml(list());
        assertThat(
            readSuccess(a(bodyReader), element),
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
            readSuccess(a(bodyReader, with(STYLES, styles)), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenRunStyleCannotBeFound() {
        XmlElement element = runXml(list(
            element("w:rPr", list(
                element("w:rStyle", map("w:val", "Heading1Char"))))));

        assertThat(
            read(a(bodyReader), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1Char", Optional.empty()))),
                list("Run style with ID Heading1Char was referenced but not defined in the document")));
    }

    @Test
    public void runIsNotBoldIfBoldElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("bold", equalTo(false)));
    }

    @Test
    public void runIsBoldIfBoldElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:b"));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("bold", equalTo(true)));
    }

    @Test
    public void runIsNotItalicIfItalicElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("italic", equalTo(false)));
    }

    @Test
    public void runIsItalicIfItalicElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:i"));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("italic", equalTo(true)));
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("underline", equalTo(false)));
    }

    @Test
    public void runIsUnderlinedIfUnderlineElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:u"));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("underline", equalTo(true)));
    }

    @Test
    public void runIsNotStruckthroughIfStrikethroughElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("strikethrough", equalTo(false)));
    }

    @Test
    public void runIsStruckthroughIfStrikethroughElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:strike"));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("strikethrough", equalTo(true)));
    }

    @Test
    public void runHasBaselineVerticalAlignmentIfVerticalAlignmentElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.BASELINE)));
    }

    @Test
    public void runIsSuperscriptIfVerticalAlignmentPropertyIsSetToSuperscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "superscript")));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUPERSCRIPT)));
    }

    @Test
    public void runIsSubscriptIfVerticalAlignmentPropertyIsSetToSubscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "subscript")));

        assertThat(
            readSuccess(a(bodyReader), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUBSCRIPT)));
    }

    @Test
    public void canReadTabElement() {
        XmlElement element = element("w:tab");

        assertThat(
            readSuccess(a(bodyReader), element),
            equalTo(Tab.TAB));
    }

    @Test
    public void brIsReadAsLineBreak() {
        XmlElement element = element("w:br");

        assertThat(
            readSuccess(a(bodyReader), element),
            equalTo(LineBreak.LINE_BREAK));
    }

    @Test
    public void warningOnBreaksThatArentLineBreaks() {
        XmlElement element = element("w:br", map("w:type", "page"));

        assertThat(
            readAll(a(bodyReader), element),
            isInternalResult(equalTo(list()), list("Unsupported break type: page")));
    }

    @Test
    public void canReadTableElements() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p")))))));

        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(new Table(list(
                new TableRow(list(
                    new TableCell(1, list(
                        make(a(PARAGRAPH))
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
            readSuccess(a(bodyReader), element),
            deepEquals(new Table(list(
                new TableRow(list(
                    new TableCell(2, list(
                        make(a(PARAGRAPH))
                    ))
                ))
            )))
        );
    }

    @Test
    public void hyperlinkIsReadIfItHasARelationshipId() {
        Relationships relationships = new Relationships(
            map("r42", new Relationship("http://example.com")));
        XmlElement element = element("w:hyperlink", map("r:id", "r42"), list(runXml(list())));
        assertThat(
            readSuccess(a(bodyReader, with(RELATIONSHIPS, relationships)), element),
            deepEquals(Hyperlink.href("http://example.com", list(make(a(RUN))))));
    }

    @Test
    public void hyperlinkIsReadIfItHasAnAnchorAttribute() {
        XmlElement element = element("w:hyperlink", map("w:anchor", "start"), list(runXml(list())));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(Hyperlink.anchor("start", list(make(a(RUN))))));
    }

    @Test
    public void hyperlinkIsIgnoredIfItDoesNotHaveARelationshipIdNorAnchor() {
        XmlElement element = element("w:hyperlink", list(runXml(list())));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(make(a(RUN))));
    }

    @Test
    public void goBackBookmarkIsIgnored() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "_GoBack"));
        assertThat(
            readAll(a(bodyReader), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void bookmarkStartIsReadIfNameIsNotGoBack() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "start"));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(new Bookmark("start")));
    }

    @Test
    public void footnoteReferenceHasIdRead() {
        XmlElement element = element("w:footnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(footnoteReference("4")));
    }

    @Test
    public void endnoteReferenceHasIdRead() {
        XmlElement element = element("w:endnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(endnoteReference("4")));
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
            make(a(PARAGRAPH, with(CHILDREN, list(
                make(a(RUN, with(CHILDREN, list(
                    new Text("[paragragh start]"))))),
                make(a(RUN, with(CHILDREN, list(
                    new Text("[paragragh end]"))))))))),
            make(a(PARAGRAPH, with(CHILDREN, list(
                make(a(RUN, with(CHILDREN, list(
                    new Text("[textbox-content]"))))))))));

        assertThat(
            readAll(a(bodyReader), paragraph),
            isInternalResult(deepEquals(expected), list()));
    }

    @Test
    public void canReadImagedataElementsWithIdAttribute() throws IOException {
        assertCanReadEmbeddedImage(image ->
            element("v:imagedata", map("r:id", image.relationshipId, "o:title", image.altText)));
    }

    @Test
    public void canReadInlinePictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            inlineImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    @Test
    public void canReadAnchoredPictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            anchoredImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    private void assertCanReadEmbeddedImage(Function<EmbeddedImage, XmlElement> generateXml) throws IOException {
        XmlElement element = generateXml.apply(new EmbeddedImage("rId5", "It's a hat"));
        Relationships relationships = new Relationships(map(
            "rId5", new Relationship("media/hat.png")));
        String imageBytes = "Not an image at all!";
        DocxFile file = InMemoryDocxFile.fromStrings(map("word/media/hat.png", imageBytes));

        Image image = (Image) readSuccess(
            a(bodyReader, with(RELATIONSHIPS, relationships), with(DOCX_FILE, file)),
            element);
        assertThat(image, allOf(
            hasProperty("altText", deepEquals(Optional.of("It's a hat"))),
            hasProperty("contentType", deepEquals(Optional.of("image/png")))));
        assertThat(
            toString(image.open()),
            equalTo(imageBytes));
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
        XmlElement element = inlineImageXml(embeddedBlipXml("rId5"), "");
        Relationships relationships = new Relationships(map(
            "rId5", new Relationship("media/hat.emf")));
        DocxFile file = InMemoryDocxFile.fromStrings(map("word/media/hat.emf", "Not an image at all!"));
        ContentTypes contentTypes = new ContentTypes(map("emf", "image/x-emf"), map());

        InternalResult<?> result = read(
            a(bodyReader,
                with(RELATIONSHIPS, relationships),
                with(DOCX_FILE, file),
                with(CONTENT_TYPES, contentTypes)),
            element);

        assertThat(
            result,
            hasWarnings(list("Image of type image/x-emf is unlikely to display in web browsers")));
    }

    @Test
    public void canReadLinkedPictures() throws IOException {
        XmlElement element = inlineImageXml(linkedBlipXml("rId5"), "");
        Relationships relationships = new Relationships(map(
            "rId5", new Relationship("file:///media/hat.png")));
        String imageBytes = "Not an image at all!";

        Image image = (Image) readSuccess(
            a(bodyReader,
                with(RELATIONSHIPS, relationships),
                with(FILE_READER, new InMemoryFileReader(map("file:///media/hat.png", imageBytes)))),
            element);

        assertThat(
            toString(image.open()),
            equalTo(imageBytes));
    }

    private XmlElement inlineImageXml(XmlElement blip, String description) {
        return element("w:drawing", list(
            element("wp:inline", imageXml(blip, description))));
    }

    private XmlElement anchoredImageXml(XmlElement blip, String description) {
        return element("w:drawing", list(
            element("wp:anchor", imageXml(blip, description))));
    }

    private List<XmlNode> imageXml(XmlElement blip, String description) {
        return list(
            element("wp:docPr", map("descr", description)),
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
            readSuccess(a(bodyReader), element),
            deepEquals(make(a(PARAGRAPH))));
    }

    @Test
    public void ignoredElementsAreIgnoredWithoutWarning() {
        assertIsIgnored("office-word:wrap");
        assertIsIgnored("v:shadow");
        assertIsIgnored("v:shapetype");
        assertIsIgnored("w:bookmarkEnd");
        assertIsIgnored("w:sectPr");
        assertIsIgnored("w:proofErr");
        assertIsIgnored("w:lastRenderedPageBreak");
        assertIsIgnored("w:commentRangeStart");
        assertIsIgnored("w:commentRangeEnd");
        assertIsIgnored("w:commentReference");
        assertIsIgnored("w:del");
        assertIsIgnored("w:footnoteRef");
        assertIsIgnored("w:endnoteRef");
        assertIsIgnored("w:pPr");
        assertIsIgnored("w:rPr");
        assertIsIgnored("w:tblPr");
        assertIsIgnored("w:tblGrid");
        assertIsIgnored("w:tcPr");
    }

    private void assertIsIgnored(String name) {
        XmlElement element = element(name, list(paragraphXml()));

        assertThat(
            readAll(a(bodyReader), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void unrecognisedElementsAreIgnoredWithWarning() {
        XmlElement element = element("w:huh");
        assertThat(
            readAll(a(bodyReader), element),
            isInternalResult(equalTo(list()), list("An unrecognised element was ignored: w:huh")));
    }

    @Test
    public void textNodesAreIgnoredWhenReadingChildren() {
        XmlElement element = runXml(list(XmlNodes.text("[text]")));
        assertThat(
            readSuccess(a(bodyReader), element),
            deepEquals(make(a(RUN))));
    }

    private static DocumentElement readSuccess(Maker<BodyXmlReader> reader, XmlElement element) {
        InternalResult<DocumentElement> result = read(reader, element);
        assertThat(result.getWarnings(), emptyIterable());
        return result.getValue();
    }

    private static InternalResult<DocumentElement> read(Maker<BodyXmlReader> reader, XmlElement element) {
        InternalResult<List<DocumentElement>> result = readAll(reader, element);
        assertThat(result.getValue(), Matchers.hasSize(1));
        return result.map(elements -> elements.get(0));
    }

    private static InternalResult<List<DocumentElement>> readAll(Maker<BodyXmlReader> reader, XmlElement element) {
        return reader.make().readElement(element).toResult();
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

    private XmlElement runXmlWithProperties(XmlNode... children) {
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

    private Run run(DocumentElement... children) {
        return make(a(RUN, with(CHILDREN, asList(children))));
    }

    private Text text(String value) {
        return new Text(value);
    }
}
