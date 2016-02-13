package org.zwobble.mammoth.tests.docx;

import com.natpryce.makeiteasy.Maker;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.Numbering;
import org.zwobble.mammoth.docx.Styles;
import org.zwobble.mammoth.results.Warning;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNode;
import org.zwobble.mammoth.xml.XmlNodes;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.zwobble.mammoth.results.Warning.warning;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.CHILDREN;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.*;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;
import static org.zwobble.mammoth.xml.XmlNodes.element;

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
    public void paragraphHasStyleIdReadFromParagraphPropertiesIfPresent() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));
        assertThat(
            read(a(bodyReader), element),
            isResult(
                hasStyle(Optional.of(new Style("Heading1", Optional.empty()))),
                list(warning("Paragraph style with ID Heading1 was referenced but not defined in the document"))));
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

    private static DocumentElement readSuccess(Maker<BodyXmlReader> reader, XmlElement element) {
        Result<DocumentElement> result = read(reader, element);
        assertThat(result.getWarnings(), deepEquals(list()));
        return result.getValue();
    }

    private static Result<DocumentElement> read(Maker<BodyXmlReader> reader, XmlElement element) {
        return reader.make().readElement(element).toResult()
            .map(elements -> elements.get(0));
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

    private <T> Matcher<Result<? extends T>> isResult(Matcher<T> valueMatcher, List<Warning> warnings) {
        return Matchers.allOf(
            hasProperty("value", valueMatcher),
            hasProperty("warnings", deepEquals(warnings)));
    }

    private Run run(DocumentElement... children) {
        return new Run(asList(children));
    }

    private Text text(String value) {
        return new Text(value);
    }
}
