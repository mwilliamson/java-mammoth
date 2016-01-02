package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNode;
import org.zwobble.mammoth.xml.XmlNodes;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.zwobble.mammoth.docx.BodyXml.readBodyXmlElement;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class BodyXmlTests {
    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = textXml("Hello!");
        assertThat(readBodyXmlElement(element), isTextElement("Hello!"));
    }

    @Test
    public void canReadTextWithinRun() {
        XmlElement element = runXml(ImmutableList.of(textXml("Hello!")));
        assertThat(
            readBodyXmlElement(element),
            isRun(run(text("Hello!"))));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(ImmutableList.of(runXml(ImmutableList.of(textXml("Hello!")))));
        assertThat(
            readBodyXmlElement(element),
            isParagraph(paragraph(run(text("Hello!")))));
    }

    @Test
    public void paragraphHasNoStyleIfItHasNoProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readBodyXmlElement(element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void paragraphHasStyleIdReadFromParagraphPropertiesIfPresent() {
        XmlElement element = paragraphXml(ImmutableList.of(
            element("w:pPr", ImmutableList.of(
                element("w:pStyle", ImmutableMap.of("w:val", "Heading1"))))));
        assertThat(
            readBodyXmlElement(element),
            hasStyle(Optional.of(new Style("Heading1", Optional.empty()))));
    }

    private XmlElement paragraphXml() {
        return paragraphXml(ImmutableList.of());
    }

    private XmlElement paragraphXml(List<XmlNode> children) {
        return element("w:p", children);
    }

    private XmlElement runXml(List<XmlNode> children) {
        return element("w:r", children);
    }

    private XmlElement textXml(String value) {
        return element("w:t", ImmutableList.of(XmlNodes.text(value)));
    }

    private Matcher<DocumentElement> isParagraph(ParagraphElement expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isRun(RunElement expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isTextElement(String value) {
        return new DeepReflectionMatcher<>(new TextElement(value));
    }

    private Matcher<? super DocumentElement> hasStyle(Optional<Style> expected) {
        return hasProperty("style", deepEquals(expected));
    }

    private ParagraphElement paragraph(DocumentElement... children) {
        return new ParagraphElement(Optional.empty(), asList(children));
    }

    private RunElement run(DocumentElement... children) {
        return new RunElement(asList(children));
    }

    private TextElement text(String value) {
        return new TextElement(value);
    }
}
