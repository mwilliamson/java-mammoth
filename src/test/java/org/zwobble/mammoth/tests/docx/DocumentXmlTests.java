package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.documents.Run;
import org.zwobble.mammoth.documents.Text;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.DocumentXmlReader;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodes;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.CHILDREN;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class DocumentXmlTests {
    private static final BodyXmlReader bodyReader = make(a(BodyXmlReaderMakers.bodyReader));

    @Test
    public void canReadTextWithinDocument() {
        XmlElement documentElement = element("w:document", ImmutableList.of(
            element("w:body", ImmutableList.of(
                element("w:p", ImmutableList.of(
                    element("w:r", ImmutableList.of(
                        element("w:t", ImmutableList.of(
                            XmlNodes.text("Hello!")))))))))));

        Document document = new DocumentXmlReader(bodyReader).readElement(documentElement);

        assertThat(
            document,
            deepEquals(new Document(ImmutableList.of(
                make(a(PARAGRAPH, with(CHILDREN, ImmutableList.of(
                    new Run(ImmutableList.of(
                        new Text("Hello!")))))))))));
    }
}
