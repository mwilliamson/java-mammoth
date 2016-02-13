package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.documents.Run;
import org.zwobble.mammoth.documents.Text;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.DocumentXmlReader;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodes;

import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.results.Result.success;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.CHILDREN;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class DocumentXmlTests {
    private static final BodyXmlReader bodyReader = make(a(BodyXmlReaderMakers.bodyReader));

    @Test
    public void canReadTextWithinDocument() {
        XmlElement documentElement = element("w:document", list(
            element("w:body", list(
                element("w:p", list(
                    element("w:r", list(
                        element("w:t", list(
                            XmlNodes.text("Hello!")))))))))));

        Result<Document> document = new DocumentXmlReader(bodyReader).readElement(documentElement);

        assertThat(
            document,
            deepEquals(success(new Document(list(
                make(a(PARAGRAPH, with(CHILDREN, list(
                    new Run(Optional.empty(), list(
                        new Text("Hello!"))))))))))));
    }
}
