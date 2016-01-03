package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import org.junit.Test;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.DocumentXmlReader;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodes;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.natpryce.makeiteasy.Property.newProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class DocumentXmlTests {
    private static final Property<HasChildren, List<DocumentElement>> CHILDREN = newProperty();

    private static final Instantiator<Paragraph> paragraph = new Instantiator<Paragraph>() {
        @Override
        public Paragraph instantiate(PropertyLookup<Paragraph> propertyLookup) {
            return new Paragraph(
                Optional.empty(),
                propertyLookup.valueOf(CHILDREN, ImmutableList.of()));
        }
    };

    @Test
    public void canReadTextWithinDocument() {
        XmlElement documentElement = element("w:document", ImmutableList.of(
            element("w:body", ImmutableList.of(
                element("w:p", ImmutableList.of(
                    element("w:r", ImmutableList.of(
                        element("w:t", ImmutableList.of(
                            XmlNodes.text("Hello!")))))))))));

        Document document = new DocumentXmlReader(new BodyXmlReader(null)).readElement(documentElement);

        assertThat(
            document,
            deepEquals(new Document(ImmutableList.of(
                make(a(paragraph, with(CHILDREN, ImmutableList.of(
                    new Run(ImmutableList.of(
                        new Text("Hello!")))))))))));
    }
}
