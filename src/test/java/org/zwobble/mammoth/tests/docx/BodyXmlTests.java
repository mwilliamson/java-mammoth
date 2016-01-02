package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.TextElement;
import org.zwobble.mammoth.xml.XmlElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.docx.BodyXml.readBodyXmlElement;
import static org.zwobble.mammoth.xml.XmlNodes.element;
import static org.zwobble.mammoth.xml.XmlNodes.text;

public class BodyXmlTests {
    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = element("w:t", ImmutableList.of(text("Hello!")));
        assertThat(readBodyXmlElement(element), isTextElement("Hello!"));
    }

    private Matcher<DocumentElement> isTextElement(String value) {
        return allOf(
            instanceOf(TextElement.class),
            hasProperty("value", equalTo(value)));
    }
}
