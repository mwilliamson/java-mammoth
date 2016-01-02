package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.ParagraphElement;
import org.zwobble.mammoth.documents.RunElement;
import org.zwobble.mammoth.documents.TextElement;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNode;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.docx.BodyXml.readBodyXmlElement;
import static org.zwobble.mammoth.xml.XmlNodes.element;
import static org.zwobble.mammoth.xml.XmlNodes.text;

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
            isRunElement(isTextElement("Hello!")));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(ImmutableList.of(runXml(ImmutableList.of(textXml("Hello!")))));
        assertThat(
            readBodyXmlElement(element),
            isParagraphElement(isRunElement(isTextElement("Hello!"))));
    }

    private XmlElement paragraphXml(List<XmlNode> children) {
        return element("w:p", children);
    }

    private XmlElement runXml(List<XmlNode> children) {
        return element("w:r", children);
    }

    private XmlElement textXml(String value) {
        return element("w:t", ImmutableList.of(text(value)));
    }

    private Matcher<DocumentElement> isParagraphElement(Matcher<DocumentElement> child) {
        return allOf(
            instanceOf(ParagraphElement.class),
            hasProperty("children", contains(child)));
    }

    private Matcher<DocumentElement> isRunElement(Matcher<DocumentElement> child) {
        return allOf(
            instanceOf(RunElement.class),
            hasProperty("children", contains(child)));
    }

    private Matcher<DocumentElement> isTextElement(String value) {
        return allOf(
            instanceOf(TextElement.class),
            hasProperty("value", equalTo(value)));
    }
}
